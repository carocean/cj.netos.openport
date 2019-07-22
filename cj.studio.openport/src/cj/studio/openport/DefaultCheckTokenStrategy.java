package cj.studio.openport;

import cj.studio.ecm.IServiceSite;
/**
 * 默认为所有用户*，所有角色*,所有用户可看成一个用户为*号的特殊用户，角色*也是一特殊角色用于代表所有角色。
 * <br>
 *     该类也没提供什么功能，如果需要实现自己的验证，可直接从接口ICheckTokenStrategy实现
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
		ti.getRoles().add("*");
		return ti;
	}

}
