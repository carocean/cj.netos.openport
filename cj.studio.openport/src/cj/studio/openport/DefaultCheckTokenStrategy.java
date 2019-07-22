package cj.studio.openport;

import cj.studio.ecm.IServiceSite;
/**
 * 默认为所有用户*.user,所有用户可看成一个用户，只是为这个特殊用户专门授权
 * @author caroceanjofers
 *
 */
public class DefaultCheckTokenStrategy implements ICheckTokenStrategy {

	@Override
	public void init(IServiceSite site) {
	}

	@Override
	public TokenInfo checkToken(String token) throws CheckTokenException {
		TokenInfo ti= new TokenInfo("*");
		ti.getRoles().add("user");
		return ti;
	}

}
