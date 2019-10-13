package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.Frame;
import cj.studio.openport.annotations.CjOpenport;

public interface IOpenportAfterInvoker {
    void doAfter(String methodName, CjOpenport openport, Frame frame, Circuit circuit);

}
