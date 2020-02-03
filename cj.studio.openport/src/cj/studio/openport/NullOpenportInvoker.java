package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;

/**
 * 空类，表示在Openport注解时为空.因此并不被执行
 */
public class NullOpenportInvoker implements IOpenportBeforeInvoker,IOpenportAfterInvoker{
    @Override
    public void doAfter(ISecuritySession iSecuritySession, Frame frame, Circuit circuit) throws CircuitException {

    }

    @Override
    public void doBefore(boolean isForbiddenCheckAccessToken, ISecuritySession iSecuritySession, Frame frame, Circuit circuit) throws CircuitException {

    }
}
