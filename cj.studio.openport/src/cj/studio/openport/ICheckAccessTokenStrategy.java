package cj.studio.openport;

import cj.studio.ecm.IServiceSite;

/**
 * 检查访问令牌，开发者可以直接实现此接口。<br>
 *     用法:
 * <pre>
 * </pre>
 */
public interface ICheckAccessTokenStrategy {
    void init(IServiceSite site);

    /**
     * 检查令牌，并反回令牌信息<br>
     * @param appCheckSecuritySession 如果有app验证注解则不为空，否则为空
     * @param portsurl 口服务地址，是无上下文的地址
     * @param methodName 方法名
     * @param accessToken 要验证的
     * @return
     * @throws CheckAccessTokenException 如果验证不通过则抛出异常
     */
    ISecuritySession checkAccessToken(ISecuritySession appCheckSecuritySession,String portsurl, String methodName, String accessToken) throws CheckAccessTokenException;
}
