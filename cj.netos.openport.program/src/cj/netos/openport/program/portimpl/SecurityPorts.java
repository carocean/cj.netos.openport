package cj.netos.openport.program.portimpl;

import cj.netos.openport.program.portface.ISecurityPorts;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecuritySession;

import java.util.Map;
@CjService(name = "/test/security")
public class SecurityPorts implements ISecurityPorts {
    @Override
    public String login(String accountName, String password) throws CircuitException {
        System.out.println("---login--"+accountName+"  "+password);
        return null;
    }

    @Override
    public Map<String, String> refreshToken(String refreshToken) throws CircuitException {
        System.out.println("---refreshToken--"+refreshToken);
        return null;
    }

    @Override
    public void testProtect(int a, boolean b, Map<String, String> c, ISecuritySession iSecuritySession) {
        System.out.println("---testProtect--"+ iSecuritySession);
    }

    @Override
    public void testPublic(int a, boolean b, Map<String, String> c) {
        System.out.println("---testPublic--");
    }
}
