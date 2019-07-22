package cj.studio.openport;

import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.annotation.CjServiceSite;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.studio.gateway.socket.pipeline.IIPipeline;
import cj.studio.openport.util.ExceptionPrinter;

public abstract class SecurityInputValve implements IAnnotationInputValve {
	ISecurityServiceContainer container;
	@CjServiceSite
	IServiceSite site;

	@Override
	public void onActive(String inputName, IIPipeline pipeline) throws CircuitException {
		if (container == null) {
			container = (ISecurityServiceContainer) site.getService("$.security.container");
		}
		pipeline.nextOnActive(inputName, this);

	}

	@Override
	public void flow(Object request, Object response, IIPipeline pipeline) throws CircuitException {
		if (!(request instanceof Frame)) {
			return;
		}
		Frame frame = (Frame) request;
		if (!container.matchesAndSelectKey(frame)) {
			pipeline.nextFlow(request, response, this);
			return;
		}
		Circuit circuit = (Circuit) response;
		try {
			container.invokeService(frame, circuit);
		} catch (Throwable e) {
			ExceptionPrinter printer=new ExceptionPrinter();
			printer.printException(e, circuit);
		}
	}

	

	@Override
	public void onInactive(String inputName, IIPipeline pipeline) throws CircuitException {
		pipeline.nextOnInactive(inputName, this);
	}

	@Override
	public int getSort() {
		return 0;
	}

	
}
