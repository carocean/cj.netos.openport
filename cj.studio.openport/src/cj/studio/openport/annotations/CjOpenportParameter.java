package cj.studio.openport.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cj.studio.openport.InRequest;

/**
 * 许可用法，用于注解许可服务的方法参数及返回值.<br>
 * 使用该注解必须使用CjPermission注解方法
 * 
 * @author caroceanjofers
 *
 */
@Target(value = { ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface CjOpenportParameter {
	/**
	 * 参数名
	 * @return
	 */
	String name();

	/**
	 * 参数放在请求的哪里
	 * @return
	 */
	InRequest in() default InRequest.parameter;

	/**
	 * 参数显式类型
	 * @return
	 */
	Class<?> type() default Void.class;
	/**
	 * 集合元素的类型。只所以用数组表示，是因为Map的一个元素有key类型和value类型两种
	 * @return
	 */
	Class<?>[] elementType() default Void.class;

	String usage();
	/**
	 * 样例文件名<br>
	 * 建议用样例数据告知开发者参数的输入格式<br>
	 *     相对于site/web/目录
	 * @return
	 */
	String simpleModelFile() default "";
	/**
	 * 参数默认值<br>
	 *     参数默认值仅供api界面使用的初始值
	 */

	String defaultValue() default "";
}
