package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IContentReciever;
import cj.studio.openport.annotations.CjOpenport;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

class OpenportContentRecieverAdapter implements IContentReciever {
    OpenportCommand openportCommand;
    Circuit circuit;
    Frame frame;
    IOpenportContentReciever target;
    private DefaultOpenportMethod openportMethod;

    public OpenportContentRecieverAdapter(IOpenportContentReciever target, OpenportCommand openportCommand, Circuit circuit) {
        this.circuit = circuit;
        this.openportCommand = openportCommand;
        this.target = target;
        this.openportMethod = new DefaultOpenportMethod(openportCommand);
    }


    @Override
    public void recieve(byte[] b, int pos, int length) throws CircuitException {
        target.ondataRecieve(b, pos, length);
    }


    @Override
    public void begin(Frame frame) {
        this.frame = frame;
        target.ondataBegin(openportMethod, frame);
    }

    @Override
    public void done(byte[] b, int pos, int length) throws CircuitException {
        target.ondataDone(b, pos, length);
        target.oninvoke(openportMethod, frame, circuit);
        if(this.openportCommand.afterInvoker!=null){
            this.openportCommand.afterInvoker.doAfter(openportMethod.getMethodName(),openportMethod.getOpenportAnnotation(),frame,circuit);
        }
    }

}

class DefaultOpenportMethod implements IOpenportMethod {

    private final List<MethodParameter> parameters;
    private final Method invoker;
    private final Object[] parametersArgsValues;
    private final Class<?> appyReturnType;
    private Object target;
    
    public DefaultOpenportMethod(OpenportCommand openportCommand) {
        this.parameters = openportCommand.parameters;
        this.invoker = openportCommand.method;
        this.target = openportCommand.openportService;
        parametersArgsValues = new Object[openportCommand.parameters.size()];
        this.appyReturnType=openportCommand.applyReturnType;
    }

    /**
     * 实际生效的返回值类型<br>这是与配置的返回值类型比较
     * @return
     */
    @Override
    public Class<?> getAppyReturnType() {
        return appyReturnType;
    }

    @Override
    public String getMethodName() {
        return invoker.getName();
    }

    @Override
    public List<MethodParameter> getParameters() {
        return parameters;
    }
    @Override
    public CjOpenport getOpenportAnnotation(){
        return this.invoker.getAnnotation(CjOpenport.class);
    }
    @Override
    public Object getTarget() {
        return target;
    }


    @Override
    public Object[] getParametersArgsValues() {
        return parametersArgsValues;
    }

    @Override
    public Object invoke() throws IllegalAccessException, IllegalArgumentException,
            InvocationTargetException {
        return invoker.invoke(target, parametersArgsValues);
    }
}