package com.soonphe.timber.codegen.mapper;

import com.google.auto.service.AutoService;
import com.soonphe.timber.codegen.BaseGenProcessor;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.TypeSpec;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * 处理注解 @GenMapper Processor
 *
 * @author soonphe
 * @since 1.0
 */
@AutoService(Processor.class)
public class GenMapperProcessor extends BaseGenProcessor<GenMapper> {

  private static final String SUFFIX = "Mapper";

  public GenMapperProcessor() {
    super(GenMapper.class);
  }

  @Override
  protected void genCode(TypeElement e, RoundEnvironment roundEnvironment) {
    String className = e.getSimpleName() + SUFFIX;
    String packageName = e.getAnnotation(GenMapper.class).pkgName();
    String pathStr = e.getAnnotation(GenMapper.class).sourcePath();
    boolean override = e.getAnnotation(GenMapper.class).overrideSource();
    TypeSpec.Builder typeSpecBuilder = TypeSpec.interfaceBuilder(className)
        .addAnnotation(Mapper.class)
        .addModifiers(Modifier.PUBLIC);
    FieldSpec instance = FieldSpec
        .builder(ClassName.get(packageName, className), "INSTANCE")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
        .initializer("$T.getMapper($T.class)",
            Mappers.class, ClassName.get(packageName, className))
        .build();
    typeSpecBuilder.addField(instance);
    genJavaFile(packageName, pathStr, typeSpecBuilder,override);
  }
}
