package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.ecm.net.IContentReciever;

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
    }


    @Override
    public void recieve(byte[] b, int pos, int length) throws CircuitException {
        target.ondataRecieve(b, pos, length);
    }


    @Override
    public void begin(Frame frame) {
        this.frame = frame;
        this.openportMethod = new DefaultOpenportMethod(openportCommand);
        target.ondataBegin(openportMethod, frame);
    }

    @Override
    public void done(byte[] b, int pos, int length) throws CircuitException {
        target.ondataDone(b, pos, length);
        target.oninvoke(openportCommand.openportService, openportMethod, frame, circuit);
    }

}

class DefaultOpenportMethod implements IOpenportMethod {

    private final List<MethodParameter> parameters;
    private final Method invoker;
    private final Object[] parametersArgsValues;
    private Object target;

    public DefaultOpenportMethod(OpenportCommand openportCommand) {
        this.parameters = openportCommand.parameters;
        this.invoker = openportCommand.method;
        this.target = openportCommand.openportService;
        parametersArgsValues = new Object[openportCommand.parameters.size()];
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
    public Object getTarget() {
        return target;
    }

    /**
     * 请解析请求并填充它。它是开放口方法的参数值
     *
     * @return
     */
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