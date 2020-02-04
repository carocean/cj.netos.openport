package cj.netos.openport.program;

import cj.studio.ecm.IServiceSite;
import cj.studio.openport.CheckAppSignException;
import cj.studio.openport.ICheckAppSignStrategy;
import cj.studio.openport.ISecuritySession;

public class ITestAppSignStrategy implements ICheckAppSignStrategy {
    @Override
    public ISecuritySession checkAppSign(String portsurl, String methodName, String appId, String appKey, String nonce, String sign) throws CheckAppSignException {
        return null;
    }

    @Override
    public void init(IServiceSite site) {

    }
}
