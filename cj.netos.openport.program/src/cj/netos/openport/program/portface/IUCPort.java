package cj.netos.openport.program.portface;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cj.studio.ecm.net.CircuitException;
import cj.studio.security.ISecurityService;
import cj.studio.security.annotation.CjPermission;
import cj.studio.security.annotation.CjPermissionParameter;

@CjPermission(usage = "")
public interface IUCPort extends ISecurityService {
	@CjPermission(usage = "")
	void authenticate(@CjPermissionParameter(name = "authName", usage = "xxx") String authName,
			@CjPermissionParameter(name = "tenant", usage = "xxx") String tenant,
			@CjPermissionParameter(name = "principals", usage = "xxx") String principals,
			@CjPermissionParameter(name = "password", usage = "xxx") String password,
			@CjPermissionParameter(name = "ttlMillis", usage = "xxx") long ttlMillis)throws CircuitException;

	@CjPermission(usage = "")
	Map<Integer,TestArg> test(@CjPermissionParameter(name = "list",type = LinkedList.class, elementType = TestArg.class, usage = "xxx") List<TestArg> list,
			@CjPermissionParameter(name = "set", elementType = TestArg.class, usage = "xxx") List<TestArg> set,
			@CjPermissionParameter(name = "map",type = TreeMap.class, elementType = {Integer.class,TestArg.class}, usage = "xxx") Map<Integer, TestArg> map)
			throws CircuitException;
}
