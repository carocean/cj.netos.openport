Core Ecosystem for Intelligent Open Platforms

Overview:
CJ-OpenPorts, built on Gateway and ECM development frameworks, integrates key capabilities such as Platform Open Ports, Microservice Hard Orchestration, API Open Engine, and Frontend Machine. It provides a unified, intelligent ecosystem for modern distributed systems.

Key Concepts and Enhanced Capabilities:

 1. Platform Open Ports: Establishes a unified platform for resource sharing, enabling seamless integration with terminals and third-party systems.
 2. Microservice Hard Orchestration: Achieves high-performance execution through precise microservice linkage orchestration.
 3. API Open Engine: Simplifies development with user-friendly APIs and testing interfaces.
 4. Frontend Machine: Ensures seamless frontend-backend collaboration for efficient service delivery.

Summary:
CJ-OpenPorts is an intelligent open platform tailored for modern distributed architectures. Leveraging Gateway and ECM frameworks, it integrates platform-level open ports, microservice hard orchestration, API open engine, and frontend machine capabilities. By streamlining resource integration and enabling secure, flexible service connections, it simplifies microservice development and management. The platform excels in providing a unified ecosystem that enhances collaboration, orchestration, and delivery efficiency. With its intelligent orchestration and open-service capabilities, CJ-OpenPorts establishes itself as a leading solution for distributed systems, offering robust foundational support for enterprise-grade applications.

# Open API

	•	Used to expose restricted resources to terminals or third parties (OpenAPI; this feature does not have a corresponding component in Spring).
	•	It can serve not only as a frontend but also be used to develop backend APIs. Its usage is simpler than pure backend microservices built with Spring Boot and stub microservice mechanisms.
	•	It offers a more user-friendly API debugging interface than Spring Swagger.
	•	When deployed in a Docker or Kubernetes cluster, it is recommended to integrate with MIC (Microservice Management Center), which supports remote deployment and remote control of microservices.
	•	Based on the ECM development framework, it seamlessly integrates with cj.studio.gateway2 to form a microservice cluster.
## Picture：

- API interface, can be tested directly (image, loading slowly, please wait…)

    ![welcome](https://github.com/carocean/cj.netos.openport/blob/master/documents/welcome.png)
    ![API Service Details](https://github.com/carocean/cj.netos.openport/blob/master/documents/viewports.png)
    ![View Return Values and Sample Parameters](https://github.com/carocean/cj.netos.openport/blob/master/documents/viewsimple.png)
    ![Run Test](https://github.com/carocean/cj.netos.openport/blob/master/documents/runtest.png)


- Gateway Console Window (image, loading slowly, please wait…)
    ![Gateway Console Information](https://github.com/carocean/cj.netos.openport/blob/master/documents/cmd.png)

Usage

Client Calls Remote Ports

	•	One method is by using third-party clients like HttpClient, DIA, etc. Please refer to the descriptions in the OpenAPI interface. Note: The Rest-StubFace header or parameter is no longer required in the Openport request.
	•	Another method is by using Gateway2 applications to call remote ports, which can be done either through directly using the Openports helper class or via service-injection calls.

			Directly Using Openports

	•	Simply configure the service container monitor in the project’s Assembly.json.
``` json
    monitor: "cj.studio.openport.client.DefaultOpenportsServicesMonitor",
```
- Then in the code snippet, you can write it like this:
``` java
            IUCPorts iucPort = Openports.open(IUCPorts.class, "ports://openport.com/openport/uc.ports", "xx");
           
           List<TestArg> test2=iucPort.test2(arg,new BigDecimal("2000.00"));
           
            TestArg arg = new TestArg();
                      arg.setAge(100);
                      arg.setName("tom");
           iucPort.test2(arg,new BigDecimal("2000.00"));
   
           Openports.close();//内核会安全关闭，此处关不关都无所谓

```
		>>> Service Injection Call

-	The jar cj.studio.openport-1.x.jar needs to be placed in the project’s cj.references.
-	The chip plugin needs to be added in the Assembly.json:
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
- You can then use it as follows:
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
    You can also use a request adapter to call remote ports:

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
### Ports Service Development
	•	Place cj.studio.openport-1.x.jar in the project’s cj.references.
	•	Register the activator cj.studio.openport.OpenportEntryPointActivator in the project’s Assembly.json.
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
- Use the annotations @CjOpenports, @CjOpenport, and @CjOpenportParameter to declare your port interface. This interface must inherit from IOpenportService, for example:
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
		For the development of frontend microservices, the client and server functionalities are often integrated together. The frontend wraps several backend port services and provides an open API to the mobile client. For details, refer to the cj.netos.openport code.

	•	Access to the open port service takes priority over the webview.
Note: The CjOpenport API can be protected, and there are three protection modes for the API methods: app signature verification, accessToken protection, and completely open.
App Signature Verification:
- Requires annotation: @CjOpenportAppSecurity(usage = "Return accessToken")
- Must implement the interface: ICheckAppSignStrategy
- You can define the method parameter ISecuritySession securitySession to use the security session.
AccessToken Protection:
- Only methods annotated with @CjOpenport.
- Must implement the interface: ICheckAccessTokenStrategy
- You can define the method parameter ISecuritySession securitySession to use the security session.
Completely Open:
- Use AccessTokenIn.nope for completely open: @CjOpenport(tokenIn = AccessTokenIn.nope)

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
