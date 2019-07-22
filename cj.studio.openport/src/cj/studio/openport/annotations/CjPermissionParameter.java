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
public @interface CjPermissionParameter {
	String name();

	InRequest in() default InRequest.parameter;

	Class<?> type() default Void.class;
	/**
	 * 集合元素的类型。只所以用数组表示，是因为Map的一个元素有key类型和value类型两种
	 * @return
	 */
	Class<?>[] elementType() default Void.class;

	String usage();
}
