package cj.studio.openport.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cj.studio.openport.TokenIn;

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
	 * invisible * 表示方法不可见也不可用<br>
	 * allow * 为方法默认权限
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
	String command() default "get";

	String protocol() default "http/1.1";

}
