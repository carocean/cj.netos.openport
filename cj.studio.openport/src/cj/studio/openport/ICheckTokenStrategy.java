package cj.studio.openport;

import cj.studio.ecm.IServiceSite;

public interface ICheckTokenStrategy {
	void init(IServiceSite site);

	TokenInfo checkToken(String token) throws CheckTokenException;
}
