package cj.studio.openport;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;

class MyParameterizedType implements ParameterizedType {
    Type[] elementType;
    Class<?> rawType;

    public MyParameterizedType(Class<?> rawType, Type[] elementType, String onmessage) {
        this.elementType = elementType;
        this.rawType = rawType;
        if (Map.class.isAssignableFrom(rawType)) {
            if (elementType != null && elementType[0] != Void.class && elementType.length != 2) {
                throw new RuntimeException("缺少Map及其派生类的元素Key和value的类型声明,在：" + onmessage);
            }
        }
        if (Collection.class.isAssignableFrom(rawType)) {
            if (elementType != null && elementType[0] != Void.class && elementType.length != 1) {
                throw new RuntimeException("缺少Collection及其派生类的元素类型声明,在：" + onmessage);
            }
        }
    }

    @Override
    public Type[] getActualTypeArguments() {
        return elementType;
    }

    @Override
    public Type getRawType() {
        return rawType;
    }

    @Override
    public Type getOwnerType() {
        return rawType.getDeclaringClass();
    }
}