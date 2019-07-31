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
- 配置服务容器监视器（在Assembly.json）：
``` json
    monitor: "cj.studio.openport.client.DefaultOpenportsServicesMonitor",

```
- 项目中声明valve并派生于OpenportInputValve
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
   
       @CjOpenport(usage = "测试", tokenIn = TokenIn.headersOfRequest, acl = {"allow *.role"})
       Map<Integer, TestArg> test(@CjOpenportParameter(name = "list", type = LinkedList.class, elementType = TestArg.class, usage = "吃了没") List<TestArg> list,
                                  @CjOpenportParameter(name = "set", elementType = TestArg.class, usage = "哈，这个好") List<TestArg> set,
                                  @CjOpenportParameter(name = "map", type = TreeMap.class, elementType = {Integer.class, TestArg.class}, usage = "型啥哩") Map<Integer, TestArg> map)
               throws CircuitException;
   
       @CjOpenport(usage = "测试2",reciever = MyOpenportContentReciever.class, tokenIn = TokenIn.headersOfRequest, acl = {"allow *.role"}, command = "post", type = LinkedList.class)
       List<TestArg> test2(@CjOpenportParameter(name = "arg", usage = "列下", in = InRequest.content, defaultValue = "{\"name\":\"cj\",\"age\":23}") TestArg arg, @CjOpenportParameter(name = "v", usage = "中", defaultValue = "5.2") BigDecimal v);
   }


```
- 如果你想与服务容器无缝集成在一起使用，就像使用普通服务一样去使用远程服务对象，则只需在Assembly.json加入插件：
``` json
    
    {
            name:"$openports",插件名可以任意起，但在容器中用时要用到此名，见下：
            class:"cj.studio.openport.client.OpenportChipPlugin",
            parameters:{参数中将要用到的远程服务对象openports全配在此，获取服务时按接口调用
              ports:"{'openportInterface':'cj.studio.openport.client.IRequestAdapter','remoteOpenportsUrl':'ports://usercenter.com/uc/authentication.service','token':''}"
            }
          }
```
    例：
``` java
    @CjService(name = "/ucport")
    public class UCPort implements IUCPort {
        @CjServiceRef(refByName = "$openports.cj.studio.openport.client.IRequestAdapter")//$openports是前面配置的插件名。自动注入该服务，IRequestAdapter是调用任意接口的形式，当然可以返回具体接口对象，见后：
        IRequestAdapter requestAdapter;
        @Override
        public String authenticate(String authName, String tenant, String principals, String password, long ttlMillis)
                throws CircuitException {
            String retvalue=requestAdapter.request("get","http/1.1", new HashMap() {
                {
                    put("Rest-StubFace", "cj.studio.backend.uc.stub.IAuthenticationStub");
                    put("Rest-Command", "authenticate");
                    put("cjtoken", "xxx");
                }
            }, new HashMap() {
                {
                    put("authName", "auth.password");
                    put("tenant", "netos.nettest");
                    put("principals", "cj");
                    put("password", "11");
                    put("ttlMillis", "188383774949292");
                }
            }, null);

            Map<String,String> response=new Gson().fromJson(retvalue,new TypeToken<HashMap<String,String>>(){}.getType());
            if(!"200".equals(response.get("status"))){
                throw new CircuitException(response.get("status"),"uc响应错误："+response.get("message"));
            }
            return response.get("result");
        }
```

    具体接口例：
``` java
        @CjService(name = "/uaacport.service")
        public class UAACPort implements IUAACPort {
            @Override
            public Map<Integer, TestArg> getAcl(List<TestArg> list, List<TestArg> set, Map<Integer, TestArg> map) throws CircuitException {
                //这是直接使用Openports帮助类的用法，当然也可使用前面讲解的插件法。
                //此处演示了具体接口的用法，IUCPort是远程服务接口，例程为了调试方便在同一网关开了两个http server，分别代表前后端
                IUCPort iucPort = Openports.open(IUCPort.class, "ports://openport.com/openport/ucport", "xx");
                TestArg arg=new TestArg();
                arg.setAge(100);
                arg.setName("tom");
                List<TestArg> test2=iucPort.test2(arg,new BigDecimal("2000.00"));
                iucPort.authenticate("auth.password", "netos.nettest", "dog", "1343", 83283L);

                Openports.close();
                return map;
            }
        }
```
- 对开放口服务的访问优先于webview

	说明：CjOpenport将服务和方法说明成开放口，即然要开放也要能保护隐私，因此其内隐含Permission概念，Permission为许可，许可概念类似于司法厅向社会生产单位发放许可执照一样概念，像卫生许可证，授予你之后方可经营，并限制你不能经营一些东西。
	用法比如：MyService,声明为：@CjOpenPort(acl={'allow *.role','deny *.user '},checkTokenName='cjtoken')，意为：myService对所有人授预拒绝权限的许可。

	缺点：该机制属于硬编码的服务编排机制。在运行期无法改变许可权限，所以也无法支持将来在微服务注册中心统一管理服务访问控制权限。但好处是这种硬包装访问控制开发起来非常简单，而且像stub存根一样，可以将api显式暴露给第三方开发者。

	当然，未来可以将该安全机制作为基本定义，而优先微服务注册中心的定义。只有当微服务注册中心没有定义到该服务的访问控制权限才采用基本定义。这就很好的将两套机制融合。
	只有具备了基本行走的能力才可以跳高，所以二者并未冲突。
	这个缺点的描述充满矛盾，是的，自然界中充满了矛盾二元论，人类也是在逻辑悖论中进步的。
	
	
