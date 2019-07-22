package cj.netos.openport.program.portface;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.ISecurityService;
import cj.studio.openport.TokenIn;
import cj.studio.openport.annotations.CjPermission;
import cj.studio.openport.annotations.CjPermissionParameter;

@CjPermission(usage = "")
public interface IUCPort extends ISecurityService {
	@CjPermission(usage = "")
	void authenticate(@CjPermissionParameter(name = "authName", usage = "xxx") String authName,
			@CjPermissionParameter(name = "tenant", usage = "xxx") String tenant,
			@CjPermissionParameter(name = "principals", usage = "xxx") String principals,
			@CjPermissionParameter(name = "password", usage = "xxx") String password,
			@CjPermissionParameter(name = "ttlMillis", usage = "xxx") long ttlMillis)throws CircuitException;

	@CjPermission(usage = "",tokenIn = TokenIn.headersOfRequest,acl = {"allow *.role"})
	Map<Integer,TestArg> test(@CjPermissionParameter(name = "list",type = LinkedList.class, elementType = TestArg.class, usage = "xxx") List<TestArg> list,
			@CjPermissionParameter(name = "set", elementType = TestArg.class, usage = "xxx") List<TestArg> set,
			@CjPermissionParameter(name = "map",type = TreeMap.class, elementType = {Integer.class,TestArg.class}, usage = "xxx") Map<Integer, TestArg> map)
			throws CircuitException;
}
