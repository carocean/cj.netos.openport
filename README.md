# 开放口
- 用于向终端或第三方开放受限资源(openAPI 该功能在spring中没有找到对应组件）
- 它不仅可以作为前置，还可以用它开发后台api，它的使用比做纯后台微服务的springboot和stub微服务机制更为简单
- 它有着比spring swagger 更为友好的api调试界面
- 在部署到docker k8s集群中时，建议结合mic（微服务管理中心），它支持微服务的远程部署和远程控制。
## 图：

- api界面，能直接测试（有图，加载慢，稍候...）

    ![欢迎页](https://github.com/carocean/cj.netos.openport/blob/master/documents/welcome.png)
    ![口服务Api详情](https://github.com/carocean/cj.netos.openport/blob/master/documents/viewports.png)
    ![查看返回值和参数样本数据](https://github.com/carocean/cj.netos.openport/blob/master/documents/viewsimple.png)
    ![运行测试](https://github.com/carocean/cj.netos.openport/blob/master/documents/runtest.png)


- 网关控制台窗口（有图，加载慢，稍候...）
    ![网关控制台信息](https://github.com/carocean/cj.netos.openport/blob/master/documents/cmd.png)

## 用法
### 客户端调用远程ports
- 一种是通过httpclient,dia等第三方客户端调用，请参考开放api界面中的描述，注：openport的请求头或参数中不再需要：Rest-StubFace
- 一种是通过网关2应用调远程ports，又分为直接使用Openports帮助类和服务注入式调用。
>>> 直接使用Openports
- 只需在项目的Assembly.json中配置服务容器监视器。 
``` json
    monitor: "cj.studio.openport.client.DefaultOpenportsServicesMonitor",
```
- 然后在代码段里即可像下面这样写：
``` java
            IUCPorts iucPort = Openports.open(IUCPorts.class, "ports://openport.com/openport/uc.ports", "xx");
           
           List<TestArg> test2=iucPort.test2(arg,new BigDecimal("2000.00"));
           
            TestArg arg = new TestArg();
                      arg.setAge(100);
                      arg.setName("tom");
           iucPort.test2(arg,new BigDecimal("2000.00"));
   
           Openports.close();//内核会安全关闭，此处关不关都无所谓

```
>>> 服务注入式调用
- 需要jar: cj.studio.openport-1.x.jar放入项目的cj.refrences
- 需要在Assembly.json中添加芯片插件：
``` json
       plugins: [
         {
           name:"$openports",//名字是任意起
           class:"cj.studio.openport.client.OpenportChipPlugin",//插件类必须
           parameters:{//下面是接口和远程服务地址配置
             ports:"[{'openportInterface':'cj.studio.openport.client.IRequestAdapter','remoteOpenportsUrl':'ports://usercenter.com/uc/authentication.service','token':''},{'openportInterface':'cj.netos.microapp.ports.IGberaSearcherPorts','remoteOpenportsUrl':'rest://gbera.com/microapp/searcher.ports','token':'xx'}]"
           }
         }
       ],

```
- 然后即可按下面方法使用：
``` java
@CjService(name="xxx")
public class GberaSearcherFrontendPorts implements IGberaSearcherFrontendPorts {
       @CjServiceRef(refByName = "$openports.cj.netos.microapp.ports.IGberaSearcherPorts")//$openports是您配置的插件名，后面是在插件中配置的接口
       IGberaSearcherPorts gberaSearcherPorts;
   
       @Override
       public List<UpdateCommand> checkMicroAppVersions(List<MicroappVersion> versions) throws CircuitException {
           return gberaSearcherPorts.checkMicroAppVersions(versions);
       }
   
       @Override
       public Microapp getMicroAppInfo(String microappname) throws CircuitException {
           return gberaSearcherPorts.getMicroAppInfo(microappname);
       }
   
       @Override
       public List<String> listMicroAppVersion(String appnameWithoutVersion) throws CircuitException {
           return gberaSearcherPorts.listMicroAppVersion(appnameWithoutVersion);
       }
   
       @Override
       public MicroPortal getMicroPortalInfo(String microportal) throws CircuitException {
           return gberaSearcherPorts.getMicroPortalInfo(microportal);
       }
   }


```

``` java
    也可以使用请求适配器调用远程ports：

    @CjServiceRef(refByName = "$openports.cj.studio.openport.client.IRequestAdapter")//IRequestAdapter是请求适配器
    IRequestAdapter requestAdapter;
    @Override
    public String authenticate(String authName, String tenant, String principals, String password, long ttlMillis)
            throws CircuitException {
        String retvalue=requestAdapter.request("get","http/1.1", new HashMap<String,String>() {
            {
                put("Rest-StubFace", "cj.studio.backend.uc.stub.IAuthenticationStub");
                put("Rest-Command", "authenticate");
                put("cjtoken", "xxx");
            }
        }, new HashMap<String,String>() {
            {
                put("authName", authName);
                put("tenant",tenant);
                put("principals", principals);
                put("password", password);
                put("ttlMillis", ttlMillis+"");//注意，map的值必须转换为String类型
            }
        }, null);

        Map<String,String> response=new Gson().fromJson(retvalue,new TypeToken<HashMap<String,String>>(){}.getType());
        if(!"200".equals(response.get("status"))){
            throw new CircuitException(response.get("status"),"uc响应错误："+response.get("message"));
        }
        return response.get("result");
    }

```
### ports服务开发
- cj.studio.openport-1.x.jar放入项目的cj.refrences
- 注册活动器cj.studio.openport.OpenportEntryPointActivator到项目Assembly.json
``` json
    activators: [
          {
            name: '口服务活动器',
            class: 'cj.studio.openport.OpenportEntryPointActivator',
            parameters: {
              "publicAPIPath": '/portsapi',这是开放的api页面地址
              accessControlStrategy: '',这是自定义的访问控制策略
              checkTokenStrategy: '' 这是自定义的验证令牌的策略
            }
          }
        ]
```
- 使用注解@CjOpenports,@CjOpenport,@CjOpenportParameter,声明您的port接口，该接口必须派生于IOpenportService，例：
``` java
    @CjOpenports(usage = "用户中心的ports")
   public interface IUCPorts extends IOpenportService {
       @CjOpenport(usage = "认证 port", command = "post",tokenIn = TokenIn.nope)
       String authenticate(@CjOpenportParameter(name = "authName", defaultValue = "auth.password", usage = "认证器名") String authName,
                           @CjOpenportParameter(name = "tenant", defaultValue = "netos.nettest", usage = "租户") String tenant,
                           @CjOpenportParameter(name = "principals", defaultValue = "wangdd", in = InRequest.header, usage = "当事人", simpleModelFile = "principals.json") String principals,
                           @CjOpenportParameter(name = "password", defaultValue = "1234", in = InRequest.content, usage = "密码") String password,
                           @CjOpenportParameter(name = "ttlMillis", usage = "过期毫秒数", defaultValue = "188383774949292") long ttlMillis) throws CircuitException;
   
       @CjOpenport(usage = "测试", tokenIn = TokenIn.headersOfRequest})
       Map<Integer, TestArg> test(@CjOpenportParameter(name = "list", type = LinkedList.class, elementType = TestArg.class, usage = "吃了没") List<TestArg> list,
                                  @CjOpenportParameter(name = "set", elementType = TestArg.class, usage = "哈，这个好") List<TestArg> set,
                                  @CjOpenportParameter(name = "map", type = TreeMap.class, elementType = {Integer.class, TestArg.class}, usage = "型啥哩") Map<Integer, TestArg> map)
               throws CircuitException;
   
       @CjOpenport(usage = "测试2",reciever = MyOpenportContentReciever.class, tokenIn = TokenIn.headersOfRequest, command = "post", type = LinkedList.class)
       List<TestArg> test2(@CjOpenportParameter(name = "arg", usage = "列下", in = InRequest.content, defaultValue = "{\"name\":\"cj\",\"age\":23}") TestArg arg, @CjOpenportParameter(name = "v", usage = "中", defaultValue = "5.2") BigDecimal v);
   }


```
>> 对于前置微服务的开发，往往是将客端和服端功能的功能整合在一起，即由前置将几个后置的ports服务包装了，向移动端提供开放api。具体参见cj.netos.openport 代码。

- 对开放口服务的访问优先于webview

	说明：CjOpenport的api可受到保护，api方法的保护模式有三种：app签名验证、accessToken保护、完全开放。
	app签名验证:
	  - 需要注解：@CjOpenportAppSecurity(usage = "返回accessToken")
      - 需实现接口：ICheckAppSignStrategy
      - 可以定义方法参数ISecuritySession securitySession,以使用安全会话
	accessToken保护:
	  - 仅用@CjOpenport注解的方法
	  - 需实现接口：ICheckAccessTokenStrategy
	  - 可以定义方法参数ISecuritySession securitySession,以使用安全会话
	完全开放：
	  - 使用 AccessTokenIn.nope为完全开放： @CjOpenport(tokenIn = AccessTokenIn.nope

```java

package cj.netos.openport.program.portface;

import cj.studio.ecm.net.CircuitException;
import cj.studio.openport.AccessTokenIn;
import cj.studio.openport.IOpenportService;
import cj.studio.openport.ISecuritySession;
import cj.studio.openport.annotations.CjOpenport;
import cj.studio.openport.annotations.CjOpenportAppSecurity;
import cj.studio.openport.annotations.CjOpenportParameter;
import cj.studio.openport.annotations.CjOpenports;

import java.util.HashMap;
import java.util.Map;

@CjOpenports(usage = "测试安全机制")
public interface ISecurityPorts extends IOpenportService {
    @CjOpenportAppSecurity(usage = "返回accessToken")
    @CjOpenport(tokenIn = AccessTokenIn.nope, usage = "用户委托第三方app以登录，返回访问令牌")
    public String login(ISecuritySession securitySession,@CjOpenportParameter(usage = "登录账号名", name = "accountName")String accountName,
                        @CjOpenportParameter(usage = "登录密码", name = "password")String password) throws CircuitException;

    @CjOpenportAppSecurity()
    @CjOpenport(tokenIn = AccessTokenIn.nope, usage = "用户委托第三方app以生成新的访问令牌，返回包括：新的accessToken,下一次的refreshToken,等等")
    public Map<String, String> refreshToken(@CjOpenportParameter(usage = "上传上一次的刷新令牌", name = "refreshToken") String refreshToken) throws CircuitException;

    @CjOpenport(usage = "普通地受accessToken保护的方法")
    public void testProtect(@CjOpenportParameter(usage = "a", name = "a") int a,
                            @CjOpenportParameter(usage = "b", name = "b") boolean b,
                            @CjOpenportParameter(usage = "c", name = "c", type = HashMap.class, elementType = {String.class, String.class}) Map<String, String> c,
                            //用于接收安全会话信息，该参数对外不可见
                            ISecuritySession iSecuritySession);

    @CjOpenport(tokenIn = AccessTokenIn.nope, usage = "开放的方法")
    public void testPublic(@CjOpenportParameter(usage = "a", name = "a") int a,
                           @CjOpenportParameter(usage = "b", name = "b") boolean b,
                           @CjOpenportParameter(usage = "c", name = "c", type = HashMap.class, elementType = {String.class, String.class}) Map<String, String> c);
}


```