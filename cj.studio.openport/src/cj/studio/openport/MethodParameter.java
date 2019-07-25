package cj.studio.openport;

import cj.studio.ecm.CJSystem;
import cj.studio.openport.annotations.CjOpenportParameter;

import java.lang.reflect.Parameter;

public class MethodParameter {
    Class<?> applyType;//与配置的类型对比实际生效的参数类型
    Parameter parameter;
    String position;
    CjOpenportParameter parameterAnnotation;
    public MethodParameter(String position, Parameter p,  Class<?> runType) {
        parameterAnnotation = p.getAnnotation(CjOpenportParameter.class);
        this.parameter = p;
        if (parameterAnnotation.type() != null) {
            if (runType.isAssignableFrom(parameterAnnotation.type())) {
                applyType = parameterAnnotation.type();
            } else {
                applyType = runType;
            }
        } else {
            applyType = runType;
        }
        CJSystem.logging().info(String.format("\t\t\t\t参数：%s %s %s", parameterAnnotation.name(), applyType.getName(), parameterAnnotation.in().name()));
    }

    /**
     * 物理参数与配置参数综合，最后启用的参数类型
     * @return
     */
    public Class<?> getApplyType() {
        return applyType;
    }

    public String getPosition() {
        return position;
    }

    public CjOpenportParameter getParameterAnnotation() {
        return parameterAnnotation;
    }

    public Parameter getParameter() {
        return parameter;
    }
}