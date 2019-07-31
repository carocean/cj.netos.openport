package cj.studio.openport.client;

import cj.studio.ecm.*;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.context.IServiceContainerMonitor;
import cj.studio.ecm.net.CircuitException;
import cj.studio.gateway.socket.pipeline.IAnnotationInputValve;
import cj.studio.gateway.socket.pipeline.IIPipeline;


public class DefaultOpenportsServicesMonitor implements IServiceContainerMonitor {
    @Override
    public final void onBeforeRefresh(IServiceSite site) {
        Openports openports = createOpenports(site);
        if (openports == null) {
            openports = new Openports(site);
        }
        IServiceProvider provider = (IServiceProvider) site.getService("$.cj.studio.gateway.app");
        IServiceSite appSite = (IServiceSite) provider.getService("$.app.site");

        IAnnotationInputValve closedvalve = new OpenportCloserAnnotationInputValve();
        CjService closedservice = OpenportCloserAnnotationInputValve.class.getAnnotation(CjService.class);
        appSite.addService(closedservice.name(), closedvalve);

        CJSystem.logging().info(getClass(), "Openports 初始化完成");
    }

    protected Openports createOpenports(IServiceSite site) {
        return null;
    }

    @Override
    public void onAfterRefresh(IServiceSite site) {

    }


    @CjService(name = "___$____openportCloserAnnotationInputValve", scope = Scope.multiton)
    final class OpenportCloserAnnotationInputValve implements IAnnotationInputValve, IRuntimeServiceCreator {
        @Override
        public final int getSort() {
            return 0;
        }

        @Override
        public Object create() {
            return new OpenportCloserAnnotationInputValve();
        }

        @Override
        public void onActive(String inputName, IIPipeline pipeline) throws CircuitException {
            pipeline.nextOnActive(inputName, this);
        }

        @Override
        public void flow(Object request, Object response, IIPipeline pipeline) throws CircuitException {
            try {
                pipeline.nextFlow(request, response, this);
            } catch (Exception e) {
                throw e;
            } finally {
                Openports.close();
            }
        }

        @Override
        public void onInactive(String inputName, IIPipeline pipeline) throws CircuitException {
            pipeline.nextOnInactive(inputName, this);
        }
    }
}
