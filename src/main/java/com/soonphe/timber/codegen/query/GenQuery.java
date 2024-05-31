package com.soonphe.timber.codegen.query;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Query转换生成注解
 *
 * @author soonphe
 * @since 1.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface GenQuery {

  String pkgName();

  String sourcePath() default "src/main/java";

  boolean overrideSource() default false;
}
