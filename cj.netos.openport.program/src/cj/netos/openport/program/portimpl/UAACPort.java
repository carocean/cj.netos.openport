package cj.netos.openport.program.portimpl;

import cj.netos.openport.program.portface.IUAACPort;
import cj.netos.openport.program.portface.TestArg;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ResponseClient;

import java.util.List;
import java.util.Map;
@CjService(name="/uaacport.service")
public class UAACPort implements IUAACPort {
    @Override
    public Map<Integer, TestArg> getAcl(List<TestArg> list, List<TestArg> set, Map<Integer, TestArg> map) throws CircuitException {

        System.out.println(String.format("--------list---%s", list));
        System.out.println(String.format("--------set---%s", set));
        System.out.println(String.format("--------map---%s", map));
//		try {
//			throw new InvocationTargetException(new CircuitException("800", "我操"));
//		} catch (Exception e) {
//			throw new EcmException(new  InvocationTargetException(e));
//		}
        ResponseClient<Map<Integer, TestArg>> rc = new ResponseClient<>();
        rc.fromJson(
                "{\"status\":200,\"message\":\"ok\",\"dataText\":\"{\\\"20\\\":{\\\"name\\\":\\\"zxt\\\",\\\"age\\\":20},\\\"23\\\":{\\\"name\\\":\\\"cj\\\",\\\"age\\\":23}}\",\"dataType\":\"java.util.TreeMap\",\"dataElementTypes\":[\"java.lang.Integer\",\"cj.netos.openport.program.portface.TestArg\"]}");
        Map<Integer, TestArg> data = rc.getData(this.getClass().getClassLoader());
        System.out.println("+++++++++" + data);
        return map;
    }
}
