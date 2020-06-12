package cj.studio.openport;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IContentReciever;
import cj.studio.ecm.net.io.MemoryContentReciever;
import cj.studio.ecm.parser.JsonMapValueParser;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.util.ExceptionPrinter;
import cj.ultimate.gson2.com.google.gson.Gson;
import cj.ultimate.gson2.com.google.gson.JsonElement;
import cj.ultimate.gson2.com.google.gson.JsonObject;
import cj.ultimate.gson2.com.google.gson.reflect.TypeToken;
import cj.ultimate.util.StringUtil;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.util.*;

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
        if (reciever == null) {
            reciever = new MemoryContentReciever();
        }
        reciever.begin(frame);

    }

    /**
     * 创建接收器。默认使用MemoryContentReciever，派生类可以覆盖它。
     *
     * @return
     */
    protected IContentReciever onCreateReciever() {
        return new MemoryContentReciever();
    }

    @Override
    public void ondataDone(byte[] b, int pos, int length) throws CircuitException {
        reciever.done(b, pos, length);
    }

    @Override
    public void oninvoke(IOpenportMethod openportMethod, ISecuritySession iSecuritySession, Frame frame, Circuit circuit) {
        doinvoke(openportMethod, iSecuritySession, frame, circuit);
    }

    public static void main(String... args) {
        Map<String, Object> jmap = new HashMap<>();
        jmap.put("a", 2532.00283883838399);
        jmap.put("b", "z");
        Map<String, Object> child = new HashMap<>();
        child.put("d", 53.32);
        jmap.put("c", new Gson().toJson(child));
        jmap.put("e", child);
        String json = new Gson().toJson(jmap);
        JsonElement e = new Gson().fromJson(json, JsonElement.class);
        if (!(e instanceof JsonObject)) {
            return;
        }
        JsonObject object = (JsonObject) e;
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            String key = entry.getKey();
            JsonElement jsonElement = entry.getValue();
            if (jsonElement == null) {
                map.put(key, "");
                continue;
            }
            String v = jsonElement.toString();
            if (jsonElement.isJsonPrimitive()) {
                v = new Gson().fromJson(v, String.class);
            }
            map.put(key, v);
        }
        System.out.println(map);
    }

    protected Map<String, String> contentToMap(String json) {
//        CJSystem.logging().info(String.format("-----%s %s", json,json.length()));
        if (StringUtil.isEmpty(json)||"null".equals(json)) {
            return new HashMap<>();
        }
        JsonElement e = new Gson().fromJson(json, JsonElement.class);
        if (!(e instanceof JsonObject)) {
            throw new EcmException("参数格式错误，必须是Map对象");
        }
        JsonObject object = (JsonObject) e;
        Map<String, String> map = new HashMap<>();
        for (Map.Entry<String, JsonElement> entry : object.entrySet()) {
            //内容参数的值类型即可是字串也可是对象
            String key = entry.getKey();
            JsonElement jsonElement = entry.getValue();
            if (jsonElement == null) {
                map.put(key, "");
                continue;
            }
            String v = jsonElement.toString();
            if (jsonElement.isJsonPrimitive()) {
                v = new Gson().fromJson(v, String.class);
            }
            map.put(key, v);
        }
        return map;
    }

    /**
     * 该类通过内存内容接收器接收参数并执行，派生类可用该类
     *
     * @param openportMethod
     * @param frame
     * @param circuit
     */
    protected void doinvoke(IOpenportMethod openportMethod, ISecuritySession iSecuritySession, Frame frame, Circuit circuit) {
        Object[] args = openportMethod.getParametersArgsValues();
        MemoryContentReciever reciever = (MemoryContentReciever) this.reciever;
        try {
            Map<String, String> contentMap = null;
            if (frame.content().revcievedBytes() > 0) {

                String json = new String(reciever.readFully());
                contentMap = contentToMap(json);
            }
            for (int i = 0; i < openportMethod.getParameters().size(); i++) {
                MethodParameter p = openportMethod.getParameters().get(i);
                if (ISecuritySession.class.isAssignableFrom(p.getApplyType())) {
                    args[i] = iSecuritySession;
                    continue;
                }
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
            doResponse(openportMethod, circuit, result, rc);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            ExceptionPrinter printer = new ExceptionPrinter();
            printer.printException(e, circuit);
        } catch (Throwable e) {
            ExceptionPrinter printer = new ExceptionPrinter();
            printer.printException(e, circuit);
        }

    }

    private void doResponse(IOpenportMethod openportMethod, Circuit circuit, Object result, ResponseClient<?> rc) {


        Class<?> dataType = openportMethod.getAppyReturnType();
        String[] dataElements = null;
        String dataText = "";

        if (dataType != null && !void.class.equals(dataType) && !Void.class.equals(dataType)) {//有返回值类型，则计算元素可能的类型
            dataText = new Gson().toJson(result);//有返回值则生成文本
            CjOpenport openport = openportMethod.getOpenportAnnotation();
            Class<?>[] defElementTypes = openport.elementType();
            if (defElementTypes != null && defElementTypes.length > 0 && !defElementTypes[0].equals(Void.class)) {//看看是否有配置的类型
                dataElements = new String[defElementTypes.length];
                for (int i = 0; i < dataElements.length; i++
                ) {
                    dataElements[i] = defElementTypes[i].getName();
                }
            } else {//没有配置的类型则只能尝试在现在的返回值中发现类型

                if (Collection.class.isAssignableFrom(dataType)) {
                    Collection<?> col = (Collection<?>) result;
                    if (col != null && !col.isEmpty()) {
                        Object obj = null;
                        for (Object o : col) {
                            obj = o;
                            break;
                        }
                        dataElements = new String[]{obj.getClass().getName()};
                    } else {
                        dataElements = new String[]{Void.class.getName()};
                    }
                }
                if (Map.class.isAssignableFrom(dataType)) {
                    @SuppressWarnings("unchecked")
                    Map<Object, Object> map = (Map<Object, Object>) result;
                    if (map != null && !map.isEmpty()) {
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
        }

        rc.status = 200;
        rc.message = "ok";
        rc.dataType = dataType == null ? Void.class.getName() : dataType.getName();
        rc.dataText = dataText;
        if (dataElements != null && dataElements.length > 0) {
            rc.dataElementTypes = dataElements;
        }
        rc.endtime = System.currentTimeMillis();
        String json = new Gson().toJson(rc);
        circuit.content().writeBytes(json.getBytes());

    }

    private Object reflactValue(Object v, MethodParameter p) throws CircuitException {
        if (v == null || StringUtil.isEmpty(v + "")) {
            if (p.applyType.isPrimitive()) {
                throw new CircuitException("500", "必须为基本型赋值");
            }
            return null;
        }
        if (p.applyType.equals(String.class)) {
            return v;
        }
        Class<?>[] eleType = p.parameterAnnotation.elementType();
        if (eleType == null || eleType[0] == Void.class) {
            return new Gson().fromJson(v + "", p.applyType);
        }
        if (Collection.class.isAssignableFrom(p.parameter.getType()) || Map.class.isAssignableFrom(p.parameter.getType())) {
            if (p.parameterAnnotation.type() != Void.class && !p.parameter.getType().isAssignableFrom(p.parameterAnnotation.type())) {
                throw new EcmException(String.format("参数注解CjPermissionParameter声明的类型不是参数类型或其派生类型。在：%s", p.position));
            }
        }
        MyParameterizedType pti = new MyParameterizedType(p.applyType, eleType, p.position);
        return new Gson().fromJson(v + "", pti);
    }
}
