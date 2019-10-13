package cj.studio.openport.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cj.studio.openport.*;

/**
 * 口声明
 * 接口方法声明为口<br>
 * 该方法在注解类时依赖于cjservice注解，它将服务名声明为受保证地址<br>
 * 
 *
 * @author caroceanjofers
 *
 */
@Target(value = { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CjOpenport {
	/**
	 * 访问控制列表<br>
	 * 默认为：充许所有访问者只要对token验证通过的,token未验证通过的则永远被拒绝<br>
	 * 写法：<br>
	 * allow cj.user 表示为充许用户为cj的访问；<br>
	 * allow test.role 充许所有角色为test的访问 ,<br>
	 * deny zxt.user 拒绝用户为zxt的访问<br>
	 * invisible *.* 表示方法不可见也不可用<br>
	 * allow *.* 为方法默认权限
	 * <pre>
	 *
	 *
	 * </pre>
	 * @return
	 */
	String[] acl() default "allow *.*";

	/**
	 * 令牌在请求的哪里
	 * @return
	 */
	TokenIn tokenIn() default TokenIn.headersOfRequest;

	/**
	 * 令牌的名字，在请求中令牌的键
	 * @return
	 */
	String checkTokenName() default "cjtoken";

	String usage();

	/**
	 * 声明响应代码<br>
	 * 声明响应代码主要用于描述可能发生的异常类型，是给使用者看的，并不替换原始异常，它仅是对原始异常的说明。<br>
	 * 写法：<br>
	 * CjOpenport(responseStatus={"500 可能是内部哪错了.","200 说明成功,非200表示出错"})
	 * 
	 * @return
	 */
	String[] responseStatus() default { "200 ok" };

	/**
	 * 样例文件名<br>
	 * 建议用样例数据告知开发者口的返回数据格式<br>
	 *     相对于site/web/目录
	 * @return
	 */
	String simpleModelFile() default "";

	/**
	 * 请求命令，支持get|post命令
	 * @return
	 */
	String command() default "get";

	String protocol() default "http/1.1";

	/**
	 * 自定义接收器，
	 * <pre>
	 * 接收器要实现两种能力：
	 * 1.接收请求数据
	 * 2.完成对方法参数的赋值，并将返回值写给客户端。注意在写晌应时尽量使用 @see ResponseClient响应格式，否则将与该开放口框架的下游生态不兼容。
	 *
	 * 系统默认采用了 @see DefaultOpenportContentReciever 作为接收存，该接收器使用了 MemoryContentReciever 作为请求解析器，
	 * 很明显它不支持大文件的上传和下载，如果您需要让openport实现大数据传输，就需要定义您自己的IOpenportContentReciever。
	 * 注：ecm系统API中提供了请求处理的基本内容，包括用于FormData解析的MultipartFormContentReciever，它支持无限文件大小传输，
	 * XwwwFormUrlencodedContentReciever，通过使用上述内容接收器，可让您实现IOpenportContentReciever更简单
	 * </pre>
	 * @return
	 */
	Class<? extends IOpenportContentReciever> reciever() default DefaultOpenportContentReciever.class;
	/**
	 * 返回值的显式类型
	 * @return
	 */
	Class<?> type() default Void.class;
	/**
	 * 返回值如果是集合则声明其元素的类型<br>
	 * 集合元素的类型。只所以用数组表示，是因为Map的一个元素有key类型和value类型两种
	 * @return
	 */
	Class<?>[] elementType() default Void.class;

	/**
	 * 在方法执行前拦截,该方法可以获取到解析后的令牌
	 * @return
	 */
	Class<? extends IOpenportBeforeInvoker> beforeInvoker() default NullOpenportInvoker.class;

	/**
	 * 在方法执行后拦截
	 * @return
	 */
	Class<? extends IOpenportAfterInvoker> afterInvoker() default NullOpenportInvoker.class;
}
