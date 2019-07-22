package cj.studio.openport;

import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.ultimate.IDisposable;

public interface ISecurityServiceContainer extends IDisposable {

	boolean matchesAndSelectKey(Frame frame) throws CircuitException;

	void invokeService(Frame frame,Circuit circuit)throws CircuitException;

}
