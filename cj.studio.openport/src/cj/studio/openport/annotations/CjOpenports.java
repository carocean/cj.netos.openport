package cj.studio.openport.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 开放口服务声明,表示一组开放口
 * 将接口声明为开放口<br>
 * 该方法在注解类时依赖于cjservice注解，它将服务名声明为受保证地址<br>
 *
 * 注意：该注解声明的安全服务接口必须派生于ISecurityService接口
 *
 * @author caroceanjofers
 *
 */
@Target(value = { ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface CjOpenports {
    String usage();

    /**
     * 本开放口用到的样例数据目录
     * @return
     */
    String simpleHome() default "";
}
