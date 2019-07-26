package cj.studio.openport;

import cj.studio.ecm.IServiceSite;
import cj.studio.openport.annotations.CjOpenport;
import cj.ultimate.util.StringUtil;

/**
 * 空令牌检查器，如果令牌为空则报异常<br>
 * 默认为所有用户*，所有角色*,所有用户可看成一个用户为*号的特殊用户，角色*也是一特殊角色用于代表所有角色。
 * <br>
 * 该类也没提供什么功能，如果需要实现自己的验证，可直接从接口ICheckTokenStrategy实现
 *
 * @author caroceanjofers
 */
public class EmptyTokenCheckerStrategy implements ICheckTokenStrategy {

    @Override
    public void init(IServiceSite site) {
    }

    @Override
    public TokenInfo checkToken(String portsurl,String methodName,CjOpenport openport, String token) throws CheckTokenException {
        if(openport.tokenIn()==TokenIn.nope){//放行
            TokenInfo ti = new TokenInfo("*");
            ti.getRoles().add("*");
            return ti;
        }
        //注掉原因：让令牌检查器决定为空时如何处理吧，否则内核要求上层开发者必输令牌岂不是太没天理了。
        if (StringUtil.isEmpty(token)) {
            throw new CheckTokenException("801", String.format("令牌为空，拒绝访问"));
        }
        TokenInfo ti = new TokenInfo("*");
        ti.getRoles().add("*");
        return ti;
    }

}
