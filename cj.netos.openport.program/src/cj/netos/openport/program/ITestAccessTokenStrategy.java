package cj.netos.openport.program;

import cj.studio.ecm.IServiceSite;
import cj.studio.openport.CheckAccessTokenException;
import cj.studio.openport.ICheckAccessTokenStrategy;
import cj.studio.openport.ISecuritySession;

public class ITestAccessTokenStrategy implements ICheckAccessTokenStrategy {
    @Override
    public void init(IServiceSite site) {

    }

    @Override
    public ISecuritySession checkAccessToken(ISecuritySession securitySession,String portsurl, String methodName, String accessToken) throws CheckAccessTokenException {
        return null;
    }
}
