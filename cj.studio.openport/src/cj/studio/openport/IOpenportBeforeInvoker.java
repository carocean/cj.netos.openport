package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.Frame;
import cj.studio.openport.annotations.CjOpenport;

public interface IOpenportBeforeInvoker {
    void doBefore(String methodName, CjOpenport openport, TokenInfo tokenInfo, Frame frame, Circuit circuit);

}
