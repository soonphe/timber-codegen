package com.soonphe.timber.codegen.vo;

import com.google.auto.service.AutoService;
import com.soonphe.timber.codegen.BaseGenProcessor;
import com.squareup.javapoet.*;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import java.util.Objects;
import java.util.Set;

/**
 * 处理注解 @GenVo Processor
 *
 * @author soonphe
 * @since 1.0
 */
@AutoService(Processor.class)
public class GenVoProcessor extends BaseGenProcessor<GenVo> {

    private static final String PREFIX = "Base";
    private static final String SUFFIX = "Vo";

    public GenVoProcessor() {
        super(GenVo.class);
    }

    @Override
    protected void genCode(TypeElement e, RoundEnvironment roundEnvironment) {
        /**
         * getEnclosedElements：返回直接在此类或接口中声明的字段、方法、构造函数和成员类型。这包括任何（隐式）默认构造函数以及枚举类型的隐式值和valueOf方法。
         * 过滤所有非@IgnoreVo注解的元素
         */
        Set<VariableElement> variableElements = filterFields(e.getEnclosedElements(),
                p -> Objects.isNull(p.getAnnotation(IgnoreVo.class)) && !voIgnore(p));
        //父类属性添加
//        if (Objects.nonNull(getSuperClass(e))) {
//            Set<VariableElement> parentElements = filterFields(getSuperClass(e).getEnclosedElements(),
//                    p -> Objects.isNull(p.getAnnotation(IgnoreVo.class)));
//            variableElements.addAll(parentElements);
//        }
        String packageName = e.getAnnotation(GenVo.class).pkgName();
        String pathStr = e.getAnnotation(GenVo.class).sourcePath();
        boolean override = e.getAnnotation(GenVo.class).overrideSource();
        boolean isJpa = e.getAnnotation(GenVo.class).jpa();
        String className = PREFIX + e.getSimpleName() + SUFFIX;
        String sourceName = e.getSimpleName() + SUFFIX;
        TypeSpec.Builder typeSpecBuilder = null;

        /**
         * 方法清单——有参构造器：参数，修饰符
         */
        MethodSpec.Builder constructorSpecBuilder = MethodSpec.constructorBuilder()
                .addParameter(TypeName.get(e.asType()), "source")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super(source)");
        /**
         * 判断注解参数，执行自定义业务逻辑
         */
        if (!isJpa) {
            System.out.println("not isJpa");
        }
        /**
         * 类清单：父类(可继承class)，修饰符,构造器方法，注解(可以多个)
         * @Data 注解在类上；生成无参构造方法，属性的set/get方法，还提供了equals、canEqual、hashCode、toString 方法
         * @AllArgsConstructor 完整参数构造方法
         */
        typeSpecBuilder = TypeSpec.classBuilder(className)
//            .superclass(AbstractBaseJdbcVo.class)
                .addModifiers(Modifier.PUBLIC)
//            .addMethod(MethodSpec.constructorBuilder()
//                    .addModifiers(Modifier.PROTECTED).build())
//            .addMethod(constructorSpecBuilder.build())
                .addAnnotation(ApiModel.class)
                .addAnnotation(Data.class)
                .addAnnotation(AllArgsConstructor.class);

        /**
         * 生成器源码清单generated source——父类(可继承class)，修饰符,构造器方法，注解(可以多个)
         */
        TypeSpec.Builder sourceTypeSpec = TypeSpec.classBuilder(sourceName)
                .superclass(ClassName.get(packageName, className))
                .addModifiers(Modifier.PUBLIC)
//            .addMethod(MethodSpec.constructorBuilder()
//                    .addModifiers(Modifier.PUBLIC).build())
//            .addMethod(constructorSpecBuilder.build())
                .addAnnotation(ApiModel.class)
                .addAnnotation(Data.class);
        /**
         * 变量遍历——添加get-set方法
         */
        for (VariableElement ve : variableElements) {
            TypeName typeName;
            //查询指定注解并处理
//      if (Objects.nonNull(ve.getAnnotation(TypeConverter.class))) {
//        typeName = ClassName.bestGuess(ve.getAnnotation(TypeConverter.class).toTypeFullName());
//      } else {
            typeName = TypeName.get(ve.asType());
//      }
            //构造类属性
            FieldSpec.Builder fieldSpec = FieldSpec
                    .builder(typeName, ve.getSimpleName().toString(), Modifier.PRIVATE);
//              .addAnnotation(AnnotationSpec.builder(FieldDesc.class)
//                      .addMember("name", "$S", getFieldDesc(ve))
//                      .build());
            //类添加属性字段
            typeSpecBuilder.addField(fieldSpec.build());
//      //添加set、get方法
//      String fieldName =
//          ve.getSimpleName().toString().substring(0, 1).toUpperCase() + ve.getSimpleName()
//              .toString().substring(1);
//      //getMethod setMethod
//      MethodSpec.Builder getMethod = MethodSpec.methodBuilder("get" + fieldName)
//          .returns(TypeName.get(ve.asType()))
//          .addModifiers(Modifier.PUBLIC)
//          .addStatement("return $L", ve.getSimpleName().toString());
//      //setMethod setMethod
//      MethodSpec.Builder setMethod = MethodSpec.methodBuilder("set" + fieldName)
//          .returns(void.class)
//          .addModifiers(Modifier.PUBLIC)
//          .addParameter(TypeName.get(ve.asType()), ve.getSimpleName().toString())
//          .addStatement("this.$L = $L", ve.getSimpleName().toString(),ve.getSimpleName().toString());
//      typeSpecBuilder.addMethod(getMethod.build());
//      typeSpecBuilder.addMethod(setMethod.build());
            //constructor 添加参数语句
//      constructorSpecBuilder.addStatement("this.set$L(source.get$L())", fieldName, fieldName);
        }
        /**
         * generate class
         */
        genJavaFileToTarget(packageName, typeSpecBuilder);
        /**
         * generate java file，可以省略，如有需求直接继承拓展 class 类即可
         */
//    genJavaFile(packageName, pathStr, sourceTypeSpec, override);
    }

  /**
   * 过滤指定类型的Element
   * @param p
   * @return
   */
    private boolean voIgnore(Element p) {
        return false;
    }
}
