package cj.studio.openport;

import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IContentReciever;
import cj.studio.ecm.net.io.MemoryContentReciever;
import cj.studio.openport.util.ExceptionPrinter;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import cj.ultimate.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 默认使用MemoryContentReciever接收器
 */
public class DefaultOpenportContentReciever implements IOpenportContentReciever {
    IContentReciever reciever;
    IOpenportMethod openportMethod;

    @Override
    public void ondataRecieve(byte[] b, int pos, int length) throws CircuitException {
        reciever.recieve(b, pos, length);
    }

    @Override
    public void ondataBegin(IOpenportMethod openportMethod, Frame frame) {
        this.openportMethod = openportMethod;
        reciever = onCreateReciever();
        if(reciever==null){
            reciever= new MemoryContentReciever();
        }
        reciever.begin(frame);

    }

    /**
     * 创建接收器。默认使用MemoryContentReciever，派生类可以覆盖它。
     * @return
     */
    protected IContentReciever onCreateReciever() {
       return  new MemoryContentReciever();
    }

    @Override
    public void ondataDone(byte[] b, int pos, int length) throws CircuitException {
        reciever.done(b, pos, length);
    }

    @Override
    public void oninvoke(IOpenportMethod openportMethod, Frame frame, Circuit circuit) {
        doinvoke(openportMethod, frame, circuit);
    }

    /**
     * 该类通过内存内容接收器接收参数并执行，派生类可用该类
     * @param openportMethod
     * @param frame
     * @param circuit
     */
    protected void doinvoke(IOpenportMethod openportMethod, Frame frame, Circuit circuit) {
        Object[] args = openportMethod.getParametersArgsValues();
        MemoryContentReciever reciever = (MemoryContentReciever) this.reciever;
        try {
            Map<String, Object> contentMap = null;
            if (frame.content().revcievedBytes() > 0) {
                String json = new String(reciever.readFully());
                contentMap = new Gson().fromJson(json, new TypeToken<Map<String, Object>>() {
                }.getType());
            }
            for (int i = 0; i < openportMethod.getParameters().size(); i++) {
                MethodParameter p = openportMethod.getParameters().get(i);
                switch (p.parameterAnnotation.in()) {
                    case header:
                        Object v = frame.head(p.parameterAnnotation.name());
                        args[i] = reflactValue(v, p);
                        args[i] = reflactValue(v, p);
                        break;
                    case parameter:
                        v = frame.parameter(p.parameterAnnotation.name());
                        if (!StringUtil.isEmpty((String) v)) {
                            try {
                                v = URLDecoder.decode((String) v, "utf-8");
                            } catch (UnsupportedEncodingException e) {
                            }
                        }
                        args[i] = reflactValue(v, p);
                        break;
                    case content:
                        if (contentMap != null) {
                            v = contentMap.get(p.parameterAnnotation.name());
                            args[i] = reflactValue(v, p);
                        }
                        break;
                }
            }
            ResponseClient<?> rc = new ResponseClient<>();
            Object result = openportMethod.invoke();
            doResponse(circuit, result, rc);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            ExceptionPrinter printer = new ExceptionPrinter();
            printer.printException(e, circuit);
        } catch (Throwable e) {
            ExceptionPrinter printer = new ExceptionPrinter();
            printer.printException(e, circuit);
        }

    }

    private void doResponse(Circuit circuit, Object result, ResponseClient<?> rc) {
        Class<?> dataType = null;
        String[] dataElements = null;
        String datastr = "";
        if (result == null) {
            dataType = Void.class;
        } else {
            dataType = result.getClass();
            datastr = new Gson().toJson(result);
            if (Collection.class.isAssignableFrom(dataType)) {
                Collection<?> col = (Collection<?>) result;
                if (!col.isEmpty()) {
                    Object obj = null;
                    for (Object o : col) {
                        obj = o;
                        break;
                    }
                    dataElements = new String[]{obj.getClass().getName()};
                }
            }
            if (Map.class.isAssignableFrom(dataType)) {
                @SuppressWarnings("unchecked")
                Map<Object, Object> map = (Map<Object, Object>) result;
                if (!map.isEmpty()) {
                    Set<Map.Entry<Object, Object>> set = map.entrySet();
                    Map.Entry<Object, Object> entry = null;
                    for (Map.Entry<Object, Object> _entry : set) {
                        entry = _entry;
                        break;
                    }
                    dataElements = new String[]{entry.getKey().getClass().getName(),
                            entry.getValue().getClass().getName()};
                }

            }
        }
        rc.status = 200;
        rc.message = "ok";
        rc.dataType = dataType.getName();
        rc.dataText = datastr;
        rc.endtime = System.currentTimeMillis();
        String json = new Gson().toJson(rc);
        circuit.content().writeBytes(json.getBytes());

    }

    private Object reflactValue(Object v, MethodParameter p) throws CircuitException {
        if (v == null || StringUtil.isEmpty(v + "")) {
            if (p.useType.isPrimitive()) {
                throw new CircuitException("500", "必须为基本型赋值");
            }
            return null;
        }
        if (p.useType.equals(String.class)) {
            return v;
        }
        Class<?>[] eleType = p.parameterAnnotation.elementType();
        if (eleType == null || eleType[0] == Void.class) {
            return new Gson().fromJson(v + "", p.useType);
        }
        if (Collection.class.isAssignableFrom(p.parameter.getType()) || Map.class.isAssignableFrom(p.parameter.getType())) {
            if (p.parameterAnnotation.type() != Void.class && !p.parameter.getType().isAssignableFrom(p.parameterAnnotation.type())) {
                throw new EcmException(String.format("参数注解CjPermissionParameter声明的类型不是参数类型或其派生类型。在：%s", p.position));
            }
        }
        MyParameterizedType pti = new MyParameterizedType(p.useType, eleType, p.position);
        return new Gson().fromJson(v + "", pti);
    }
}
