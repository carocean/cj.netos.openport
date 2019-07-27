package cj.studio.openport.client;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IInputChannel;
import cj.studio.ecm.net.io.MemoryContentReciever;
import cj.studio.ecm.net.io.MemoryInputChannel;
import cj.studio.ecm.net.io.MemoryOutputChannel;
import cj.studio.gateway.socket.pipeline.IOutputSelector;
import cj.studio.gateway.socket.pipeline.IOutputer;
import cj.ultimate.gson2.com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class RequestAdapterAspect implements IAdaptingAspect {
    IOutputSelector selector;
    String dest;
    ThreadLocal<Map<String, IOutputer>> local;
    String portsUrl;
    String token;


    @Override
    public void init(ThreadLocal<Map<String, IOutputer>> local, IOutputSelector selector, Class<?> openportInterface, String dest, String portsUrl, String token) {
        this.portsUrl = portsUrl;
        this.token = token;
        this.selector = selector;
        this.dest = dest;
        this.local=local;
    }

    @Override
    public Object invoke(Object adapter, Method method, Object[] args) throws Throwable {
        if (!"request".equals(method.getName())) {
            return null;
        }
        Map<String, IOutputer> outmap = local.get();
        if (outmap == null) {
            outmap = new HashMap<>();
            local.set(outmap);
        }
        try {
            IOutputer out = outmap.get(this.dest);
            if (out == null) {
                out = selector.select(this.dest);
                outmap.put(this.dest, out);
                return doRequest(out, adapter, method, args);
            }
            if (out.isDisposed()) {
                outmap.remove(this.dest);
                out = selector.select(this.dest);
                outmap.put(this.dest, out);
                return doRequest(out, adapter, method, args);
            }
            return doRequest(out, adapter, method, args);
        } catch (Exception e) {
            throw e;
        }
    }

    private Object doRequest(IOutputer outputer,Object adapter, Method method, Object[] args) throws Throwable {
        //String command, String protocol, Map<String, String> headers, Map<String, String> parameters, byte[] data
        String command = (String) args[0];
        String protocol = (String) args[1];
        Map<String, String> headers = (Map<String, String>) args[2];
        Map<String, String> parameters = (Map<String, String>) args[3];
        byte[] data = (byte[]) args[4];

        //下面就是调用outputer发请求了
        IInputChannel ic = new MemoryInputChannel();
        String frame_line = String.format("%s %s %s",command,portsUrl,protocol);
        Frame frame = new Frame(ic, frame_line);
        MemoryContentReciever mcr = new MemoryContentReciever();
        frame.content().accept(mcr);
        ic.begin(frame);
        frame.head("cjtoken",token);//为默认。该token放到这个位置是让开发者的可以覆盖它（如果在头中也赋了同键的cjtoken）
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                frame.head(entry.getKey(), entry.getValue());
            }
        }

        if (parameters != null) {
            for (Map.Entry<String, String> entry : parameters.entrySet()) {
                frame.parameter(entry.getKey(), entry.getValue());
            }
        }
        if (data != null && data.length != 0) {
            String json = new String(new Gson().toJson(data));
            byte[] b = json.getBytes();
            ic.done(b, 0, b.length);
        } else {
            ic.done(new byte[0], 0, 0);
        }
        MemoryOutputChannel oc = new MemoryOutputChannel();
        String circuit_line = String.format("%s 200 ok", frame.protocol());
        Circuit circuit = new Circuit(oc, circuit_line);

        outputer.send(frame, circuit);

        int state = Integer.valueOf(circuit.status());
        if (state >= 400) {
            throw new CircuitException(circuit.status(), String.format("远端网络异常：%s", circuit.message()));
        }
        circuit.content().close();
        byte[] b = oc.readFully();
        if (b == null || b.length == 0) {
            return null;
        }
        String json = new String(b);
        return json;
    }

}
