package com.soonphe.timber.codegen.vo;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Vo生成注解
 *
 * @author soonphe
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GenVo {

    /**
     * 包名
     * @return
     */
    String pkgName();

    /**
     * 源码路径
     * @return
     */
    String sourcePath() default "src/main/java";

    /**
     * 是否重复覆盖源代码
     * @return
     */
    boolean overrideSource() default false;

    /**
     * 是否jpa数据源
     * @return
     */
    boolean jpa() default false;
}
