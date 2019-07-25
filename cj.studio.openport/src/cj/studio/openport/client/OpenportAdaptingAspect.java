package cj.studio.openport.client;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IInputChannel;
import cj.studio.ecm.net.io.MemoryContentReciever;
import cj.studio.ecm.net.io.MemoryInputChannel;
import cj.studio.ecm.net.io.MemoryOutputChannel;
import cj.studio.gateway.socket.pipeline.IOutputer;
import cj.studio.gateway.socket.util.SocketContants;
import cj.studio.openport.ResponseClient;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

class OpenportAdaptingAspect implements IAdaptingAspect {
    Class<?> openportInterface;
    IOutputer outputer;
    String portsUrl;
    String token;

    private boolean isClosed() {
        if(outputer==null)return true;
        return outputer.isDisposed();
    }

    @Override
    public void init(IOutputer outputer, Class<?> openportInterface, String portsUrl, String token) {
        this.openportInterface = openportInterface;
        this.outputer = outputer;
        this.portsUrl = portsUrl;
        this.token = token;
    }

    @Override
    public Object invoke(Object adapter, Method method, Object[] args) throws Throwable {
        if(method.getName().equals("__$_is_$_closed_$_outputer___")){
            return isClosed();
        }
        Class<?>[] argsType = method.getParameterTypes();
        Method openportMethod = null;
        try {
            openportMethod = openportInterface.getMethod(method.getName(), argsType);//就是让抛NoSuchMethodException异常，这样告诉开发者接口不匹配。
        } catch (NoSuchMethodException e) {
            throw new NoSuchMethodException("调用的方法在开放接口中不存在。在：" + method);
        }
        CjOpenport openport = openportMethod.getAnnotation(CjOpenport.class);//有注解了就知道怎么装入了
        if (openport == null) {
            throw new CircuitException("405", "接口方法没有CjOpenport注解。在：" + openportMethod);
        }
        //下面就是调用outputer发请求了
        IInputChannel ic = new MemoryInputChannel();
        String frame_line = String.format("%s %s %s", openport.command(), portsUrl, openport.protocol());
        Frame frame = new Frame(ic, frame_line);
        MemoryContentReciever mcr = new MemoryContentReciever();
        frame.content().accept(mcr);
        ic.begin(frame);
        frame.head(SocketContants.__frame_Head_Rest_Command, openportMethod.getName());
        switch (openport.tokenIn()) {
            case headersOfRequest:
                frame.head(openport.checkTokenName(), token);
                break;
            case parametersOfRequest:
                frame.parameter(openport.checkTokenName(), token);
                break;
            case nope:
                break;
        }
        Map<String, Object> content = null;
        Parameter[] parameters = openportMethod.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter p = parameters[i];
            CjOpenportParameter openportParameter = p.getAnnotation(CjOpenportParameter.class);
            switch (openportParameter.in()) {
                case header:
                    String v = convertValue(args[i]);
                    frame.head(openportParameter.name(), v);
                    break;
                case parameter:
                    v = convertValue(args[i]);
                    frame.parameter(openportParameter.name(), v);
                    break;
                case content:
                    if (content == null) {
                        content = new HashMap<>();
                    }
                    content.put(openportParameter.name(), args[i]);
                    break;
            }
        }
        if (content != null && !content.isEmpty()) {
            String json = new String(new Gson().toJson(content));
            byte[] b = json.getBytes();
            ic.done(b, 0, b.length);
        } else {
            ic.done(new byte[0], 0, 0);
        }
        MemoryOutputChannel oc = new MemoryOutputChannel();
        String circuit_line = String.format("%s 200 ok", openport.protocol());
        Circuit circuit = new Circuit(oc, circuit_line);

        outputer.send(frame, circuit);

        int state = Integer.valueOf(circuit.status());
        if (state >= 400) {
            throw new CircuitException(circuit.status(), String.format("远端网络异常：%s", circuit.message()));
        }
        circuit.content().close();
        byte[] b = oc.readFully();
        if (b == null || b.length == 0) {
            throw new CircuitException(circuit.status(), String.format("远端无响应内容：%s", circuit.message()));
        }
        String json = new String(b);
        ResponseClient rc = ResponseClient.createFromJson(json);
        if (rc.getStatus() != 200) {
            throw new CircuitException(rc.getStatus() + "", String.format("远端服务错误：%s", rc.getMessage()));
        }
        return rc.getData(openportInterface.getClassLoader());
    }

    private String convertValue(Object arg) {
        if (arg == null) return null;
        if (arg instanceof String) {
            return (String) arg;
        }

        return new Gson().toJson(arg);
    }

}
