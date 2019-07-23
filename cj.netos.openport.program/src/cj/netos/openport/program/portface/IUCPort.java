package cj.netos.openport.program.portface;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.TokenIn;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;

@CjOpenport(usage = "")
public interface IUCPort extends IOpenportService {
	@CjOpenport(usage = "")
	void authenticate(@CjOpenportParameter(name = "authName", usage = "xxx") String authName,
			@CjOpenportParameter(name = "tenant", usage = "xxx") String tenant,
			@CjOpenportParameter(name = "principals", usage = "xxx") String principals,
			@CjOpenportParameter(name = "password", usage = "xxx") String password,
			@CjOpenportParameter(name = "ttlMillis", usage = "xxx") long ttlMillis)throws CircuitException;

	@CjOpenport(usage = "",tokenIn = TokenIn.headersOfRequest,acl = {"allow *.role"})
	Map<Integer,TestArg> test(@CjOpenportParameter(name = "list",type = LinkedList.class, elementType = TestArg.class, usage = "xxx") List<TestArg> list,
			@CjOpenportParameter(name = "set", elementType = TestArg.class, usage = "xxx") List<TestArg> set,
			@CjOpenportParameter(name = "map",type = TreeMap.class, elementType = {Integer.class,TestArg.class}, usage = "xxx") Map<Integer, TestArg> map)
			throws CircuitException;
}
