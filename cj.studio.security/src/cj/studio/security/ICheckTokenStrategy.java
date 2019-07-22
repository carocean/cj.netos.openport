package cj.studio.security;

import java.util.Map;

import cj.studio.ecm.IServiceSite;

public interface ICheckTokenStrategy {
	void init(IServiceSite site);

	Map<String, Object> checkToken(String token) throws CheckTokenException;
}
