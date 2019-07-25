package cj.netos.openport.program.portface;

import java.math.BigDecimal;
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
    @CjOpenport(usage = "认证吧", command = "post")
    void authenticate(@CjOpenportParameter(name = "authName", defaultValue = "auth.password", usage = "认证器名") String authName,
                      @CjOpenportParameter(name = "tenant", defaultValue = "netos.nettest", usage = "租户") String tenant,
                      @CjOpenportParameter(name = "principals", defaultValue = "wangdd", in = InRequest.header, usage = "当事人", simpleModelFile = "principals.json") String principals,
                      @CjOpenportParameter(name = "password", defaultValue = "1234", in = InRequest.content, usage = "密码") String password,
                      @CjOpenportParameter(name = "ttlMillis", usage = "过期毫秒数", defaultValue = "188383774949292") long ttlMillis) throws CircuitException;

    @CjOpenport(usage = "测试了", tokenIn = TokenIn.headersOfRequest, acl = {"allow *.role"})
    Map<Integer, TestArg> test(@CjOpenportParameter(name = "list", type = LinkedList.class, elementType = TestArg.class, usage = "吃了没") List<TestArg> list,
                               @CjOpenportParameter(name = "set", elementType = TestArg.class, usage = "哈，这个好") List<TestArg> set,
                               @CjOpenportParameter(name = "map", type = TreeMap.class, elementType = {Integer.class, TestArg.class}, usage = "型啥哩") Map<Integer, TestArg> map)
            throws CircuitException;

    @CjOpenport(usage = "测试了", tokenIn = TokenIn.headersOfRequest, acl = {"allow *.role"},command = "post" ,type = LinkedList.class)
    List<TestArg> test2(@CjOpenportParameter(name = "arg", usage = "列下",in = InRequest.content) TestArg arg, @CjOpenportParameter(name = "v",  usage = "中") BigDecimal v);
}
