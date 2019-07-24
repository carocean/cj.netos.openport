package cj.netos.openport.program.portface;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.InRequest;
import cj.studio.openport.TokenIn;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

@CjOpenports(usage = "这是演示用的")
public interface IUCPort extends IOpenportService {
	@CjOpenport(usage = "认证吧",command = "post")
	void authenticate(@CjOpenportParameter(name = "authName", usage = "认证器名") String authName,
			@CjOpenportParameter(name = "tenant", usage = "租户") String tenant,
			@CjOpenportParameter(name = "principals",in = InRequest.header, usage = "当事人", simpleModelFile = "principals.json") String principals,
			@CjOpenportParameter(name = "password",in=InRequest.content, usage = "密码") String password,
			@CjOpenportParameter(name = "ttlMillis", usage = "过期毫秒数",defaultValue = "188383774949292") long ttlMillis)throws CircuitException;

	@CjOpenport(usage = "测试了",tokenIn = TokenIn.headersOfRequest,acl = {"allow *.role"})
	Map<Integer,TestArg> test(@CjOpenportParameter(name = "list",type = LinkedList.class, elementType = TestArg.class, usage = "吃了没") List<TestArg> list,
			@CjOpenportParameter(name = "set", elementType = TestArg.class, usage = "哈，这个好") List<TestArg> set,
			@CjOpenportParameter(name = "map",type = TreeMap.class, elementType = {Integer.class,TestArg.class}, usage = "型啥哩") Map<Integer, TestArg> map)
			throws CircuitException;
}
