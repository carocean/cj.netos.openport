package cj.netos.openport.program.portimpl;

import cj.netos.openport.program.portface.IUAACPort;
import cj.netos.openport.program.portface.IUCPort;
import cj.netos.openport.program.portface.TestArg;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.client.Openports;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@CjService(name = "/uaacport.service")
public class UAACPort implements IUAACPort {
    @Override
    public Map<Integer, TestArg> getAcl(List<TestArg> list, List<TestArg> set, Map<Integer, TestArg> map) throws CircuitException {

        IUCPort iucPort = Openports.open(IUCPort.class, "ports://openport.com/openport/ucport", "xx");
        TestArg arg=new TestArg();
        arg.setAge(100);
        arg.setName("tom");
        List<TestArg> test2=iucPort.test2(arg,new BigDecimal("2000.00"));
        iucPort.authenticate("auth.password", "netos.nettest", "dog", "1343", 83283L);

        Openports.closeCurrent();
        return map;
    }
}
