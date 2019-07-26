package cj.studio.openport;

import cj.studio.ecm.IServiceSite;
import cj.studio.openport.annotations.CjOpenport;

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
     * 检查令牌，并反回令牌信息<br>
     * @param portsurl 口服务地址，是无上下文的地址
     * @param methodName 方法名
     * @param openport 里面有token的配置
     * @param token 要验证的
     * @return
     * @throws CheckTokenException
     */
    TokenInfo checkToken(String portsurl,String methodName,CjOpenport openport, String token) throws CheckTokenException;
}
