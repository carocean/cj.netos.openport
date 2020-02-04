package cj.studio.openport;

import cj.studio.ecm.IServiceSite;

/**
 * app授信验证
 */
public interface ICheckAppSignStrategy {
    /**
     * 验证第三方app签名。<br>
     *     用法：
     *     <pre>
     *     </pre>
     * @param portsurl
     * @param methodName
     * @param appId 平台颁发的app标识
     * @param appKey 平台颁发的应用key
     * @param nonce 客户端生成sign时的随机数
     * @param sign 客户端使用appSceret生成的签名文本
     * @return 返回ISecuritySession对象，其principal属性为appid
     * @throws CheckAppSignException 验证不能过则返回异常，拒绝是801，缺少参数是804
     */
    ISecuritySession checkAppSign(String portsurl, String methodName, String appId, String appKey, String nonce, String sign)throws CheckAppSignException;

    void init(IServiceSite site);

}
