package cj.studio.openport;

import cj.studio.ecm.IServiceSite;

/**
 * 检查令牌，开发者可以直接实现此接口。<br>
 *     用法:
 * <pre>
 *     一般是连接到远程认证中心验证令牌
 *     可以通过site获取rest来访问后端微服务认证中心
 * </pre>
 */
public interface ICheckTokenStrategy {
    void init(IServiceSite site);

    /**
     * 检查令牌，并反回令牌信息
     * @param token 要验证的
     * @return
     * @throws CheckTokenException
     */
    TokenInfo checkToken(String token) throws CheckTokenException;
}
