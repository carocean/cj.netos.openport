package cj.netos.openport.program.portimpl;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

import cj.netos.openport.program.portface.IUCPort;
import cj.netos.openport.program.portface.TestArg;
import cj.studio.ecm.EcmException;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;

@CjService(name = "/ucport")
public class UCPort implements IUCPort {

	@Override
	public void authenticate(String authName, String tenant, String principals, String password, long ttlMillis) {
		System.out
				.println(String.format("-----------%s %s %s %s %s", authName, tenant, principals, password, ttlMillis));
	}

	@Override
	public int test(List<TestArg> list, List<TestArg> set, Map<Integer, TestArg> map) throws CircuitException {
		System.out.println(String.format("--------list---%s", list));
		System.out.println(String.format("--------set---%s", set));
		System.out.println(String.format("--------map---%s", map));
		try {
			throw new InvocationTargetException(new CircuitException("800", "我操"));
		} catch (Exception e) {
			throw new EcmException(new  InvocationTargetException(e));
		}
	}

}
