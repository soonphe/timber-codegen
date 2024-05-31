package com.soonphe.timber.codegen;

/**
 * 属性字段描述注解
 *
 * @author soonphe
 * @since 1.0
 */
public @interface FieldDesc {
    String name() default "";
}
