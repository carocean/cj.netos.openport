package cj.studio.openport;

import cj.studio.ecm.IServiceSite;

public class DefaultAccessControlStrategy implements IAccessControlStrategy {

	@Override
	public void init(IServiceSite site) {
	}

	@Override
	public boolean isInvisible(Acl acl) {
		if (acl == null || acl.isEmpty())
			return true;
		for (int i = 0; i < acl.invisibleCount(); i++) {
			Ace ace = acl.invisible(i);
			if ("*".equals(ace.whois()) || "*.user".equals(ace.whois()) || "*.role".equals(ace.whois())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void checkRight(TokenInfo tokenInfo, Acl acl) throws CheckRightException {
		// 检查权限
		
		if (acl.hasRoleOnInvisibles(tokenInfo.getRoles()) ||acl.hasUserOnInvisibles(tokenInfo.getUser())) {
			throw new CheckRightException("801",
					String.format("服务方法对访问者%s %s不可见", tokenInfo.getUser(), tokenInfo.getRoles()));
		}
		if (acl.hasRoleOnDenys(tokenInfo.getRoles()) ||acl.hasUserOnDenys(tokenInfo.getUser())) {
			throw new CheckRightException("801",
					String.format("服务方法对访问者%s %s拒绝", tokenInfo.getUser(), tokenInfo.getRoles()));
		}
		if (!acl.hasRoleOnAllows(tokenInfo.getRoles()) &&!acl.hasUserOnAllows(tokenInfo.getUser())) {
			throw new CheckRightException("801",
					String.format("服务方法对访问者%s %s未许充", tokenInfo.getUser(), tokenInfo.getRoles()));
		}
		// 下面为空即为可往后执行
	}

}
