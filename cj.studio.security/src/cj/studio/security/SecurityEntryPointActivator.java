package cj.studio.security;

import cj.studio.ecm.IEntryPointActivator;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.context.IElement;
/**
 * 安全服务检查器
 * @author caroceanjofers
 *
 */
public class SecurityEntryPointActivator implements IEntryPointActivator {
	ISecurityServiceContainer container;
	@Override
	public void activate(IServiceSite site, IElement args) {
		container=new DefaultSecurityServiceContainer(site);
	}

	@Override
	public void inactivate(IServiceSite site) {
		container.dispose();
		
	}

}
