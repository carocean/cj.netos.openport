package cj.studio.security;

import java.util.Map;

import cj.studio.ecm.IServiceSite;

public interface IAccessControlStrategy {
	void init(IServiceSite site);
	boolean isInvisible(String[] acl);
	void checkRight(Map<String, Object> tokenInfo, String[] acl) throws CheckRightException;

}
