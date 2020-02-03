package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;

public interface IOpenportBeforeInvoker {
    void doBefore(boolean isForbiddenCheckAccessToken, ISecuritySession securitySession, Frame frame, Circuit circuit) throws CircuitException;

}
