package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;

public interface IOpenportAfterInvoker {
    void doAfter(ISecuritySession iSecuritySession, Frame frame, Circuit circuit) throws CircuitException;

}
