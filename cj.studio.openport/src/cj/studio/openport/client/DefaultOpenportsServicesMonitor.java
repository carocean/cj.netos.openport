package cj.studio.openport.client;

import cj.studio.ecm.CJSystem;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.context.IServiceContainerMonitor;


public class DefaultOpenportsServicesMonitor implements IServiceContainerMonitor {
    @Override
    public final void onBeforeRefresh(IServiceSite site) {
        Openports openports = createOpenports(site);
        if (openports == null) {
            openports = new Openports(site);
        }
        CJSystem.logging().info(getClass(), "Openports 初始化完成");
    }

    protected Openports createOpenports(IServiceSite site) {
        return null;
    }

    @Override
    public void onAfterRefresh(IServiceSite site) {

    }
}
