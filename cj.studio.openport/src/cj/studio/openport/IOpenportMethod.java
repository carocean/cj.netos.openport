package cj.studio.openport;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public interface IOpenportMethod {
    String getMethodName();

    /**
     * 获取方法配置参数
     * @return
     */
    List<MethodParameter> getParameters();

    /**
     * 可能是开放服务
     * @return
     */
    Object getTarget();

    /**
     * 请解析请求并填充它。它是开放口方法的参数值
     *
     * @return
     */
    Object[] getParametersArgsValues();

    /**
     * 执行该方法
     * @return
     * @throws IllegalAccessException
     * @throws IllegalArgumentException
     * @throws InvocationTargetException
     */
    Object invoke() throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException;
}
