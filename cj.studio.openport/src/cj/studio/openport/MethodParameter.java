package cj.studio.openport;

import cj.studio.ecm.CJSystem;
import cj.studio.openport.annotations.CjOpenportParameter;

import java.lang.reflect.Parameter;

public class MethodParameter {
    Class<?> useType;
    Parameter parameter;
    String position;
    CjOpenportParameter parameterAnnotation;
    public MethodParameter(String position, Parameter p,  Class<?> runType) {
        parameterAnnotation = p.getAnnotation(CjOpenportParameter.class);
        this.parameter = p;
        if (parameterAnnotation.type() != null) {
            if (runType.isAssignableFrom(parameterAnnotation.type())) {
                useType = parameterAnnotation.type();
            } else {
                useType = runType;
            }
        } else {
            useType = runType;
        }
        CJSystem.logging().info(String.format("\t\t\t\t参数：%s %s %s", parameterAnnotation.name(), useType.getName(), parameterAnnotation.in().name()));
    }

    public Class<?> getUseType() {
        return useType;
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