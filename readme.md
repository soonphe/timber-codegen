# timber-codegen

## 项目介绍（Project Introduction）
代码自动生成项目，主要针对自定义注解生成

### 业务功能模块（functional module）
本项目主要使用自定义注解生成代码：

项目使用依赖
```
        <dependency>
            <groupId>com.squareup</groupId>
            <artifactId>javapoet</artifactId>
            <version>1.11.1</version>
        </dependency>
        <dependency>
            <groupId>com.google.auto.service</groupId>
            <artifactId>auto-service</artifactId>
            <version>1.0-rc2</version>
        </dependency>
```
- auto-service：为注解自动注册模块
- javapoet：为代码生成模块

### 定义注解
关于注解定义：略

#### 注解处理器
关于注解处理器：`BaseGenProcessor`
必须要做的两步
- **继承虚处理器AbstractProcessor**
- **实现init、process、getSupportedAnnotationTypes、getSupportedSourceVersion方法**

- getSupportedAnnotationTypes()：标识这个注解处理器是注册给哪个注解的,这里指定集合类名即可
- getSupportedSourceVersion():用来指定你使用的Java版本
- init():初始化
- process()：匹配类或接口文件，调用自定义生成代码——genCode(e, roundEnv);
```
  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return Sets.newHashSet(processAnnotation.getCanonicalName());
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    this.filer = processingEnv.getFiler();
    this.types = processingEnv.getTypeUtils();
    this.elements = processingEnv.getElementUtils();
    this.messager = processingEnv.getMessager();
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    //获取class中所有的Element对象：PackageElement、TypeElement、VariableElement、ExecutableElement、TypeParameterElement
    Set<Element> annotatedClass = roundEnv.getElementsAnnotatedWith(processAnnotation);
    //识别所有的类或接口
    for (TypeElement e : ElementFilter.typesIn(annotatedClass)) {
      genCode(e, roundEnv);
    }
    return false;
  }
```

**关于注解处理器注册**
- 注册方式一：手动注册

  在使用注解处理器需要先声明，步骤：
    1. 需要在 processors 库的 main 目录下新建 resources 资源文件夹；
    2. 在 resources文件夹下建立 META-INF/services 目录文件夹
    3. 在文件java.lang.Process中增加注册器路径名： com.starcor.processor.router.RouterProcessor

- 方式二：自动注册

  google提供了一个注册处理器的库AutoService。帮助将要编译的处理器进行编译。
```
        <dependency>
            <groupId>com.google.auto.service</groupId>
            <artifactId>auto-service</artifactId>
            <version>1.0-rc2</version>
        </dependency>
        
        gradle:
        compile ‘com.google.auto.service:auto-service:1.0-rc2’
```  
然后在processor中使用注解，会在编译时自动生成META-INF/services 文件夹：
```
@AutoService(Processor.class)
```

#### 生成文件
实现代码生成方法，生成java源文件或target文件
```
@Override
  protected void genCode(TypeElement e, RoundEnvironment roundEnvironment) {
    //过滤指定属性
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
```


### 快速开始
下载此项目并运行以下命令：
```
mvn clean install
```

主要提供了以下注解：
GenVo

使用示例：
引入本项目jar包，在实体类上使用注解，打包即可自动生成代码
```
@GenVo(pkgName = "com.timber.codegen.test.vo")
public class Cat {

  private String name;
}
```