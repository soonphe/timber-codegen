package com.soonphe.timber.codegen.mapper;

/**
 * Mapper转换生成注解
 *
 * @author soonphe
 * @since 1.0
 */
public @interface GenMapper {

    String pkgName();

    String sourcePath() default "src/main/java";

    boolean overrideSource() default false;
}
