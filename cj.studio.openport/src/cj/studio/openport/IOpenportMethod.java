package cj.studio.openport;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface IOpenportMethod {
    String getMethodName();

    List<MethodParameter> getParameters();
    Object getTarget();
    Object[] getParametersArgsValues();
    Object invoke() throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException;
}
