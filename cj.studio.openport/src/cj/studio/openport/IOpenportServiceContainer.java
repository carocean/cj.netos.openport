package cj.studio.openport;

import cj.studio.ecm.IServiceProvider;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.ultimate.IDisposable;

public interface IOpenportServiceContainer extends IDisposable, IOpenportPrinter, IServiceProvider {

    boolean matchesAndSelectKey(Frame frame) throws CircuitException;

    void invokeService(Frame frame, Circuit circuit) throws CircuitException;


}
