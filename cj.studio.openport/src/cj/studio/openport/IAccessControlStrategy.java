package cj.studio.openport;

import cj.studio.ecm.IServiceSite;

public interface IAccessControlStrategy {
	void init(IServiceSite site);
	boolean isInvisible(Acl acl);
	void checkRight(TokenInfo tokenInfo, Acl acl) throws CheckRightException;

}
