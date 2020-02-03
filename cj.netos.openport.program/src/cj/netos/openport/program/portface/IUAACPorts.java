package cj.netos.openport.program.portface;

import cj.netos.openport.program.args.TestArg;
import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.AccessTokenIn;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
@CjOpenports(usage = "统一授权访问中心")
public interface IUAACPorts extends IOpenportService {
    @CjOpenport(usage = "测试了", tokenIn = AccessTokenIn.headersOfRequest,  simpleModelFile = "/models/getAcl_return.json")
    Map<Integer, TestArg> getAcl(@CjOpenportParameter(name = "list", simpleModelFile = "/models/list_parameter.json",defaultValue = "[{\"name\":\"cj\",\"age\":23},{\"name\":\"zxt\",\"age\":20}]",type = LinkedList.class, elementType = TestArg.class, usage = "吃了没") List<TestArg> list,
                                 @CjOpenportParameter(name = "set",defaultValue = "[{\"name\":\"cj\",\"age\":23},{\"name\":\"zxt\",\"age\":20}]",elementType = TestArg.class, usage = "哈，这个好") List<TestArg> set,
                                 @CjOpenportParameter(name = "map",defaultValue = "{23:{\"name\":\"cj\",\"age\":23},20:{\"name\":\"zxt\",\"age\":20}}",type = TreeMap.class, elementType = {Integer.class, TestArg.class}, usage = "型啥哩") Map<Integer, TestArg> map)
            throws CircuitException;
}
