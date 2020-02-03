package cj.studio.openport.annotations;

import cj.studio.openport.*;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * app授信机制保护起来的方法<br>
 * 一般为登录方法或处理刷新令牌（refreshToken）的方法<br>
 * 必须结合CjOpenport注解<br>
 *     注意：该注解使得CjOpenport注解中的tokenIn配置无效
 *
 * @author caroceanjofers
 * @see ICheckAppSignStrategy 需要实现该接口以实现应用签名认证，并在Assembly.json中注册
 */
@Target(value = { ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CjOpenportAppSecurity {
	/**
	 * 应用id放在哪里
	 * @return
	 */
	SKeyInRequest appIDIn() default SKeyInRequest.header;

	/**
	 * 应用的key
	 * @return
	 */
	SKeyInRequest appKeyIn() default SKeyInRequest.header;

	/**
	 * 与sign有关的随机串是什么
	 * @return
	 */
	SKeyInRequest nonceIn() default SKeyInRequest.header;

	/**
	 * 用appSecret生成的签名
	 * @return
	 */
	SKeyInRequest signIn() default SKeyInRequest.header;

	/**
	 * appid在请求中的键名
	 * @return
	 */
	String appIDName() default "App-Id";
	/**
	 * appKey在请求中的键名
	 * @return
	 */
	String appKeyName() default "App-Key";
	/**
	 * nonce在请求中的键名
	 * @return
	 */
	String nonceName() default "App-Nonce";
	/**
	 * sign在请求中的键名
	 * @return
	 */
	String signName() default "App-Sign";

	/**
	 * 用法
	 * @return
	 */
	String usage() default "";
}
