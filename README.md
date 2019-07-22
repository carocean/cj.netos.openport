# 开放口
- 用于向终端或第三方开放受限资源
- 未来将接入服务注册中心(mic)，由服务注册中心统一管理各个开放口的访问控制权限

## 用法
- cj.studio.sceurity-1.x.jar放入项目的cj.refrences
- 注册活动器cj.studio.openport.SecurityEntryPointActivator到项目Assembly.json
- 项目中声明valve并派生于SecurityInputValve
- 使用注解@CjPermission,@CjPermissionParameter
- 打印许可，派生于SecurityServiceAPI
- 对许可服务的访问优先于webview

	说明：Permission将服务和方法说明成“服务拥有什么许可”，许可概念类似于司法厅向社会生产单位发放许可执照一样概念，像卫生许可证，授予你之后方可经营，并限制你不能经营一些东西。
	用法比如：MyService,声明为：@CjPermission(acl={'allow *.role','deny *.user '},checkTokenName="cjtoken")，意为：myService对所有人授预充许权限的许可。

	缺点：该机制属于硬编码的服务编排机制。在运行期无法改变许可权限，所以也无法支持将来在微服务注册中心统一管理服务访问控制权限。但好处是这种硬包装访问控制开发起来非常简单，而且像stub存根一样，可以将api显式暴露给第三方开发者。

	当然，未来可以将该安全机制作为基本定义，而优先微服务注册中心的定义。只有当微服务注册中心没有定义到该服务的访问控制权限才采用基本定义。这就很好的将两套机制融合。
	只有具备了基本行走的能力才可以跳高，所以二者并未冲突。
	这个缺点的描述充满矛盾，是的，自然界中充满了矛盾二元论，人类也是在逻辑悖论中进步的。