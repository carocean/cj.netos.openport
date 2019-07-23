package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;

public interface IOpenportAPIController {
    void flow(Frame frame, Circuit circuit)throws CircuitException;
    boolean matchesAPI(Frame frame);
}
