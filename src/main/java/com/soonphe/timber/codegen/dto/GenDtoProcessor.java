package com.soonphe.timber.codegen.dto;

import com.google.auto.service.AutoService;
import com.soonphe.timber.codegen.BaseGenProcessor;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import lombok.Data;

/**
 * 处理注解 @GenDto Processor
 *
 * @author soonphe
 * @since 1.0
 */
@AutoService(Processor.class)
public class GenDtoProcessor extends BaseGenProcessor<GenDto> {

  private static final String PREFIX = "Base";
  private static final String SUFFIX = "Dto";

  public GenDtoProcessor() {
    super(GenDto.class);
  }

  @Override
  protected void genCode(TypeElement e, RoundEnvironment roundEnvironment) {
    Set<VariableElement> variableElements = filterFields(e.getEnclosedElements(),
        p -> Objects.isNull(p.getAnnotation(IgnoreDto.class)) && !dtoIgnore(p));
    String packageName = e.getAnnotation(GenDto.class).pkgName();
    String pathStr = e.getAnnotation(GenDto.class).sourcePath();
    boolean override = e.getAnnotation(GenDto.class).overrideSource();
    String className = PREFIX + e.getSimpleName() + SUFFIX;
    String sourceName = e.getSimpleName() + SUFFIX;
    TypeSpec.Builder typeSpecBuilder = TypeSpec.classBuilder(className)
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(ApiModel.class)
        .addAnnotation(Data.class);
    for (VariableElement ve : variableElements) {
      FieldSpec.Builder fieldSpec = FieldSpec
          .builder(TypeName.get(ve.asType()), ve.getSimpleName().toString(), Modifier.PRIVATE)
          .addAnnotation(AnnotationSpec.builder(ApiModelProperty.class)
              .addMember("value", "$S", getFieldDesc(ve))
              .build());
      String fieldName =
          ve.getSimpleName().toString().substring(0, 1).toUpperCase() + ve.getSimpleName()
              .toString().substring(1);
      MethodSpec.Builder getMethod = MethodSpec.methodBuilder("get" + fieldName)
          .returns(TypeName.get(ve.asType()))
          .addModifiers(Modifier.PUBLIC)
          .addStatement("return $L", ve.getSimpleName().toString());
      MethodSpec.Builder setMethod = MethodSpec.methodBuilder("set" + fieldName)
          .returns(void.class)
          .addModifiers(Modifier.PUBLIC)
          .addParameter(TypeName.get(ve.asType()), ve.getSimpleName().toString())
          .addStatement("this.$L = $L", ve.getSimpleName().toString(),ve.getSimpleName().toString());
      typeSpecBuilder.addField(fieldSpec.build());
      typeSpecBuilder.addMethod(setMethod.build());
      typeSpecBuilder.addMethod(getMethod.build());
    }
    TypeSpec.Builder source = TypeSpec.classBuilder(sourceName)
        .superclass(ClassName.get(packageName, className))
        .addModifiers(Modifier.PUBLIC)
        .addAnnotation(ApiModel.class)
        .addAnnotation(Data.class);
    /**
     * generate base class
     */
    genJavaFileToTarget(packageName, typeSpecBuilder);
    /**
     * generate source file
     */
    genJavaFile(packageName, pathStr, source,override);
  }


  private boolean dtoIgnore(Element ve) {
    return dtoIgnoreFieldTypes.contains(TypeName.get(ve.asType())) || ve.getModifiers()
        .contains(Modifier.STATIC);
  }

  static final List<TypeName> dtoIgnoreFieldTypes;

  static {
    dtoIgnoreFieldTypes = new ArrayList<>();
    dtoIgnoreFieldTypes.add(TypeName.get(Date.class));
    dtoIgnoreFieldTypes.add(TypeName.get(LocalDateTime.class));
  }
}
