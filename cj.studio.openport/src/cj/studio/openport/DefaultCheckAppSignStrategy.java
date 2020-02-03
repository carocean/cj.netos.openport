package cj.studio.openport;

import cj.studio.ecm.IServiceSite;

public class DefaultCheckAppSignStrategy implements ICheckAppSignStrategy {
    @Override
    public void checkAppSign(String portsurl, String methodName, String appId, String appKey, String nonce, String sign) {
        return;
    }

    @Override
    public void init(IServiceSite site) {

    }
}
