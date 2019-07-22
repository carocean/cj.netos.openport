package cj.studio.security.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import cj.studio.security.DefaultAccessControlStrategy;
import cj.studio.security.IAccessControlStrategy;
import cj.studio.security.Right;
import cj.studio.security.TokenIn;

/**
 * 将接口或接口方法声明为许可<br>
 * 该方法在注解类时依赖于cjservice注解，它将服务名声明为受保证地址<br>
 * 
 * 注意：该注解声明的安全服务接口必须派生于ISecurityService接口
 * 
 * @author caroceanjofers
 *
 */
@Target(value = { ElementType.TYPE,ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CjPermission {
	String who() default "*";

	Right right() default Right.allow;

	TokenIn tokenIn() default TokenIn.none;

	String checkTokenName() default "cjtoken";
	/**
	 * 访问控制策略。
	 * @return
	 */
	Class<? extends IAccessControlStrategy> acs() default DefaultAccessControlStrategy.class;
	String usage();
}
