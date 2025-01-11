package cj.netos.openport.program.portface;

import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.AccessTokenIn;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportAppSecurity;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

import java.util.HashMap;
import java.util.Map;

@CjOpenports(usage = "测试安全机制")
public interface ISecurityPorts {
    @CjOpenportAppSecurity(usage = "返回accessToken")
    @CjOpenport(tokenIn = AccessTokenIn.headersOfRequest, usage = "用户委托第三方app以登录，返回访问令牌")
    public String login(ISecuritySession securitySession,@CjOpenportParameter(usage = "登录账号名", name = "accountName")String accountName,
                        @CjOpenportParameter(usage = "登录密码", name = "password")String password) throws CircuitException;

    @CjOpenportAppSecurity()
    @CjOpenport(tokenIn = AccessTokenIn.nope, usage = "用户委托第三方app以生成新的访问令牌，返回包括：新的accessToken,下一次的refreshToken,等等")
    public Map<String, String> refreshToken(@CjOpenportParameter(usage = "上传上一次的刷新令牌", name = "refreshToken") String refreshToken) throws CircuitException;

    @CjOpenport(usage = "普通地受accessToken保护的方法")
    public void testProtect(@CjOpenportParameter(usage = "a", name = "a") int a,
                            @CjOpenportParameter(usage = "b", name = "b") boolean b,
                            @CjOpenportParameter(usage = "c", name = "c", type = HashMap.class, elementType = {String.class, String.class}) Map<String, String> c,
                            //用于接收安全会话信息，该参数对外不可见
                            ISecuritySession iSecuritySession);

    @CjOpenport(tokenIn = AccessTokenIn.nope, usage = "开放的方法")
    public void testPublic(@CjOpenportParameter(usage = "a", name = "a") int a,
                           @CjOpenportParameter(usage = "b", name = "b") boolean b,
                           @CjOpenportParameter(usage = "c", name = "c", type = HashMap.class, elementType = {String.class, String.class}) Map<String, String> c);
}
