package com.soonphe.timber.codegen.dto;

/**
 * Dto生成注解
 *
 * @author soonphe
 * @since 1.0
 */
public @interface GenDto {

    String pkgName();

    String sourcePath() default "src/main/java";

    boolean overrideSource() default false;
}
