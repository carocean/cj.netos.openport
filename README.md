# 开放口
- 用于向终端或第三方开放受限资源(openAPI 该功能在spring中没有找到对应组件）
- 它不仅可以作为前置，还可以用它开发后台api，它的使用比做纯后台微服务的springboot和stub微服务机制更为简单
- 它有着比spring swagger 更为友好的api调试界面
## 图：

- api界面，能直接测试（有图，加载慢，稍候...）

    ![欢迎页](https://github.com/carocean/cj.netos.openport/blob/master/documents/welcome.png)
    ![口服务Api详情](https://github.com/carocean/cj.netos.openport/blob/master/documents/viewports.png)
    ![查看返回值和参数样本数据](https://github.com/carocean/cj.netos.openport/blob/master/documents/viewsimple.png)
    ![运行测试](https://github.com/carocean/cj.netos.openport/blob/master/documents/runtest.png)


- 网关控制台窗口（有图，加载慢，稍候...）
    ![网关控制台信息](https://github.com/carocean/cj.netos.openport/blob/master/documents/cmd.png)

## 用法
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
```
>>>>>>monitor: "cj.studio.openport.client.DefaultOpenportsServicesMonitor",

```
- 项目中声明valve并派生于OpenportInputValve
- 使用注解@CjOpenports,@CjOpenport,@CjOpenportParameter
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
```
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

                Openports.closeCurrent();
                return map;
            }
        }
```
- 对开放口服务的访问优先于webview

	说明：CjOpenport将服务和方法说明成开放口，即然要开放也要能保护隐私，因此其内隐含Permission概念，Permission为许可，许可概念类似于司法厅向社会生产单位发放许可执照一样概念，像卫生许可证，授予你之后方可经营，并限制你不能经营一些东西。
	用法比如：MyService,声明为：@CjOpenPort(acl={'allow *.role','deny *.user '},checkTokenName='cjtoken')，意为：myService对所有人授预充许权限的许可。

	缺点：该机制属于硬编码的服务编排机制。在运行期无法改变许可权限，所以也无法支持将来在微服务注册中心统一管理服务访问控制权限。但好处是这种硬包装访问控制开发起来非常简单，而且像stub存根一样，可以将api显式暴露给第三方开发者。

	当然，未来可以将该安全机制作为基本定义，而优先微服务注册中心的定义。只有当微服务注册中心没有定义到该服务的访问控制权限才采用基本定义。这就很好的将两套机制融合。
	只有具备了基本行走的能力才可以跳高，所以二者并未冲突。
	这个缺点的描述充满矛盾，是的，自然界中充满了矛盾二元论，人类也是在逻辑悖论中进步的。
	
	
