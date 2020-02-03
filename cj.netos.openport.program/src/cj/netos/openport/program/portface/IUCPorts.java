package cj.netos.openport.program.portface;

import cj.netos.openport.program.MyOpenportContentReciever;
import cj.netos.openport.program.args.TestArg;
import cj.studio.ecm.CJSystem;
import cj.studio.ecm.net.Circuit;
import cj.studio.ecm.net.CircuitException;
import cj.studio.ecm.net.Frame;
import cj.studio.openport.*;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@CjOpenports(usage = "用户中心的ports")
public interface IUCPorts extends IOpenportService {
    @CjOpenport(usage = "认证 port", command = "post", tokenIn = AccessTokenIn.nope)
    String authenticate(@CjOpenportParameter(name = "authName", defaultValue = "auth.password", usage = "认证器名") String authName,
                        @CjOpenportParameter(name = "tenant", defaultValue = "netos.nettest", usage = "租户") String tenant,
                        @CjOpenportParameter(name = "principals", defaultValue = "wangdd", in = PKeyInRequest.header, usage = "当事人", simpleModelFile = "principals.json") String principals,
                        @CjOpenportParameter(name = "password", defaultValue = "1234", in = PKeyInRequest.content, usage = "密码") String password,
                        @CjOpenportParameter(name = "ttlMillis", usage = "过期毫秒数", defaultValue = "188383774949292") long ttlMillis) throws CircuitException;

    @CjOpenport(usage = "测试", tokenIn = AccessTokenIn.headersOfRequest)
    Map<Integer, TestArg> test(@CjOpenportParameter(name = "list", type = LinkedList.class, elementType = TestArg.class, usage = "吃了没") List<TestArg> list,
                               @CjOpenportParameter(name = "set", elementType = TestArg.class, usage = "哈，这个好") List<TestArg> set,
                               @CjOpenportParameter(name = "map", type = TreeMap.class, elementType = {Integer.class, TestArg.class}, usage = "型啥哩") Map<Integer, TestArg> map)
            throws CircuitException;

    @CjOpenport(usage = "测试2", reciever = MyOpenportContentReciever.class, tokenIn = AccessTokenIn.headersOfRequest,  command = "post", type = LinkedList.class, beforeInvoker = TestInvoker.class, afterInvoker = TestInvoker.class)
    List<TestArg> test2(@CjOpenportParameter(name = "arg", usage = "列下", in = PKeyInRequest.content, defaultValue = "{\"name\":\"cj\",\"age\":23}") TestArg arg, @CjOpenportParameter(name = "v", usage = "中", defaultValue = "5.2") BigDecimal v);

    class TestInvoker implements IOpenportBeforeInvoker, IOpenportAfterInvoker {
        @Override
        public void doAfter(ISecuritySession iSecuritySession, Frame frame, Circuit circuit) throws CircuitException {
            CJSystem.logging().info(getClass(),String.format("doAfter: %s | %s", iSecuritySession,frame));
        }

        @Override
        public void doBefore(boolean isForbidden, ISecuritySession iSecuritySession, Frame frame, Circuit circuit) {
            CJSystem.logging().info(getClass(),String.format("doBefore:  %s | %s", iSecuritySession,frame));
        }
    }
}
