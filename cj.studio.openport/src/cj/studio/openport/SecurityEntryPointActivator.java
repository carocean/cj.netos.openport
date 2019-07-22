package cj.studio.openport;

import cj.studio.ecm.EcmException;
import cj.studio.ecm.IEntryPointActivator;
import cj.studio.ecm.IServiceSite;
import cj.studio.ecm.context.IElement;
import cj.studio.ecm.context.IProperty;
import cj.ultimate.util.StringUtil;

/**
 * 安全服务检查器
 * 
 * @author caroceanjofers
 *
 */
public class SecurityEntryPointActivator implements IEntryPointActivator {
	ISecurityServiceContainer container;

	@Override
	public void activate(IServiceSite site, IElement args) {
		IProperty acs = (IProperty) args.getNode("accessControlStrategy");
		IProperty cts = (IProperty) args.getNode("checkTokenStrategy");
		String acsclassStr = "";
		if (acs != null) {
			acsclassStr = cts.getValue().getName();
		}
		if (StringUtil.isEmpty(acsclassStr)) {
			acsclassStr = DefaultAccessControlStrategy.class.getName();
		}
		String ctsclassStr = "";
		if (cts != null) {
			ctsclassStr = cts.getValue().getName();
		}
		if (StringUtil.isEmpty(ctsclassStr)) {
			ctsclassStr = DefaultCheckTokenStrategy.class.getName();
		}
		try {
			IAccessControlStrategy aclstrategy = (IAccessControlStrategy) Class.forName(acsclassStr).newInstance();
			ICheckTokenStrategy ctstrategy = (ICheckTokenStrategy) Class.forName(ctsclassStr).newInstance();
			container = new DefaultSecurityServiceContainer(site, aclstrategy, ctstrategy);
		} catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
			throw new EcmException(e);
		}
	}

	@Override
	public void inactivate(IServiceSite site) {
		container.dispose();

	}

}
