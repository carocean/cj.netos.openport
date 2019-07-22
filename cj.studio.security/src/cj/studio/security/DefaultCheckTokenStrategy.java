package cj.studio.security;

import java.util.Map;

import cj.studio.ecm.IServiceSite;

public class DefaultCheckTokenStrategy implements ICheckTokenStrategy {

	@Override
	public void init(IServiceSite site) {
	}
	
	@Override
	public Map<String, Object> checkToken(String token) throws CheckTokenException {
		System.out.println("验证token:" + token);
		return null;
	}

}
