package peckb1.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;
import peckb1.processor.util.ComplexDeserializerCreator;
import peckb1.processor.util.ImplementationCreator;
import peckb1.processor.util.ProcessorUtil;
import peckb1.processor.util.SetupCreator;
import peckb1.processor.util.SimpleDeserializerCreator;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
public class AutoJacksonProcessor extends AbstractProcessor {

    private ImplementationCreator implementationCreator;
    private ProcessorUtil processorUtil;
    private SimpleDeserializerCreator simpleDeserializerCreator;
    private ComplexDeserializerCreator complexDeserializerCreator;
    private SetupCreator setupCreator;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        final Types typeUtils = processingEnv.getTypeUtils();
        final Elements elementUtils = processingEnv.getElementUtils();
        final Filer filer = processingEnv.getFiler();
        final Messager messager = processingEnv.getMessager();

        this.processorUtil = new ProcessorUtil(elementUtils, messager);
        this.implementationCreator = new ImplementationCreator(typeUtils, elementUtils, filer, this.processorUtil);
        this.simpleDeserializerCreator = new SimpleDeserializerCreator(typeUtils, elementUtils, filer, this.processorUtil);
        this.complexDeserializerCreator = new ComplexDeserializerCreator(typeUtils, elementUtils, filer, this.processorUtil);
        this.setupCreator = new SetupCreator(filer, this.processorUtil);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> annotations = Sets.newHashSet();
        annotations.add(AutoJackson.class.getCanonicalName());
        return annotations;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        ImmutableList.Builder<TypeElement> interfacesWithDeserializers = ImmutableList.builder();

        // Iterate over each 'item' annotated with the AutoJackson class
        roundEnv.getElementsAnnotatedWith(AutoJackson.class).forEach(element -> {
            if (isInvalidAutoJacksonInterface(element)) {
                // bail out from this element. Any errors should have been reported by isInvalid
                return;
            }

            TypeElement typeElement = (TypeElement) element; // cast our interface to a `TypeElement`
            interfacesWithDeserializers.add(typeElement); // add it to our main list for the setup class creation

            // check the type of objects we need to create
            AutoJackson annotation = typeElement.getAnnotation(AutoJackson.class);
            if (isSimpleClass(annotation)) {
                this.implementationCreator.implementInterface(typeElement);
                this.simpleDeserializerCreator.createDeserializer(typeElement);
            } else {
                this.complexDeserializerCreator.createDeserializer(typeElement);
            }
        });

        ImmutableList<TypeElement> interfaces = interfacesWithDeserializers.build();
        if (!interfaces.isEmpty()) {
            this.setupCreator.createSetupClass(interfaces);
        }

        // return false, in case someone else ALSO wants to do something with our annotation
        return false;
    }

    /**
     * Checks if a given {@link AutoJackson.Type} was specified by the user, and we
     * have a more complex scenario. Or if {@link NoTypeEnum} was left (the default)
     * and we have a simple case to handle.
     *
     * @param annotation The annotation to check the type of
     * @return true if the annotation does not have a custom type, false if it does
     */
    private boolean isSimpleClass(AutoJackson annotation) {
        String annotationValueName = this.processorUtil.getTypeElement(annotation).getQualifiedName().toString();
        String defaultName = NoTypeEnum.class.getCanonicalName();
        return annotationValueName.equals(defaultName);
    }

    /**
     * Check if an element is invalid or not. Any errors must be reported by this method
     *
     * @param element The element to check
     * @return true if the element is invalid, false if valid
     */
    private boolean isInvalidAutoJacksonInterface(Element element) {
        boolean invalid = false;
        // Check if an interface  has been annotated with @Factory
        if (element.getKind() != ElementKind.INTERFACE) {
            this.processorUtil.error(element, "Only interfaces can be annotated with @%s", AutoJackson.class.getSimpleName());
            invalid = true;
        }
        return invalid;
    }

//    @Override
//    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
//        // Itearate over all @AutoJackson annotated elements
//        ImmutableList.Builder<TypeElement> ourElementsBuilder = ImmutableList.builder();
//
//        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(AutoJackson.class)) {
//
//            // Check if an interface  has been annotated with @Factory
//            if (annotatedElement.getKind() != ElementKind.INTERFACE) {
//                error(annotatedElement, "Only interfaces can be annotated with @%s", AutoJackson.class.getSimpleName());
//                continue;
//            }
//
//            // We can cast it, because we know that it of ElementKind.INTERFACE
//            TypeElement typeElement = (TypeElement) annotatedElement;
//            ourElementsBuilder.add(typeElement);
//
//            // grab our annotation to get at the details
//            AutoJackson annotation = typeElement.getAnnotation(AutoJackson.class);
//
//            // handle concrete types, or classes which have multiple types
//            String annotationTypeName = loadClassName(annotation);
//            if (annotationTypeName.equals(NoTypeEnum.class.getCanonicalName())) {
//                generateDefaultImplementation(typeElement);
//                generateCustomDeserializer(typeElement, generateSimpleDeserializerLogic(typeElement));
//            } else {
//                generateCustomDeserializer(typeElement, generateTypeDeserializerLogic(typeElement));
//            }
//
//        }
//
//        ImmutableList<TypeElement> ourElements = ourElementsBuilder.build();
//        if (!ourElements.isEmpty()) {
//            generateDeserializerModule(ourElements);
//        }
//
//        return false;
//    }
//
//    private void generateDeserializerModule(ImmutableList<TypeElement> ourElements) {
//        Builder moduleBuilder = TypeSpec
//                .classBuilder("AutoJacksonSetup")
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
//
//        MethodSpec constructor = MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PRIVATE)
//                .build();
//
//        MethodSpec.Builder moduleGenerator = MethodSpec.methodBuilder("configureObjectMapper")
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .addParameter(ParameterSpec.builder(ObjectMapper.class, "objectMapper").build())
//                .addStatement("$T deserialzationModule = new $T()", SimpleModule.class, SimpleModule.class);
//
//        ourElements.forEach(element -> {
//            moduleGenerator.addStatement("deserialzationModule.addDeserializer($T.$L, new $L_$L())",
//                    element, "class", element, "AutoJacksonDeserializer");
//        });
//        moduleGenerator
//                .addStatement("objectMapper.registerModule(deserialzationModule)")
//                .addStatement("objectMapper.setVisibility($T.$L, $T.$L)", PropertyAccessor.class, PropertyAccessor.ALL, JsonAutoDetect.Visibility.class, Visibility.NONE)
//                .build();
//
//        moduleBuilder
//                .addMethod(constructor)
//                .addMethod(moduleGenerator.build());
//        // create the javaFile object
//        JavaFile javaFile = JavaFile
//                .builder("peckb1.autojackson", moduleBuilder.build())
//                .build();
//
//        // and write that file to disc
//        try {
//            javaFile.writeTo(this.filer);
//        } catch (IOException e) {
//            error(null, e.getMessage());
//        }
//    }
//
//    private Consumer<MethodSpec.Builder> generateTypeDeserializerLogic(TypeElement typeElement) {
//        AutoJackson annotation = typeElement.getAnnotation(AutoJackson.class);
//        TypeElement enumTypeElement;
//        try {
//            Class<?> clazz = annotation.type().value();
//            enumTypeElement = this.elementUtils.getTypeElement(clazz.getCanonicalName());
//        } catch (MirroredTypeException mte) {
//            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
//            enumTypeElement = (TypeElement) classTypeMirror.asElement();
//        }
//
//        final TypeElement finalEnumElement = enumTypeElement;
//
//        if (enumTypeElement.getKind() != ElementKind.ENUM) {
//            error(enumTypeElement, "The type of object for %s must be an enum", AutoJackson.Type.class.getSimpleName());
//            return method -> method.addStatement("return null");
//        }
//
//        ImmutableList.Builder<Element> constantElementsBuilder = ImmutableList.builder();
//        ImmutableList.Builder<ExecutableElement> accessorMethodBuilder = ImmutableList.builder();
//
//        List<? extends Element> enclosedElements = enumTypeElement.getEnclosedElements();
//        enclosedElements.forEach(enclosedElement -> {
//            if (enclosedElement.getKind() == ElementKind.ENUM_CONSTANT) {
//                constantElementsBuilder.add(enclosedElement);
//            }
//            if (enclosedElement.getAnnotation(AutoJacksonTypeClass.class) != null && enclosedElement.getKind() == ElementKind.METHOD) {
//                accessorMethodBuilder.add((ExecutableElement) enclosedElement);
//            }
//        });
//
//        ImmutableList<Element> enumValueElements = constantElementsBuilder.build();
//        ImmutableList<ExecutableElement> enumAccessorElement = accessorMethodBuilder.build();
//
//        if (enumValueElements.isEmpty()) {
//            error(enumTypeElement, "The enum inside the type must have some values");
//            return method -> method.addStatement("return null");
//        }
//
//        if (enumAccessorElement.size() != 1) {
//            error(enumTypeElement, "No accessor method inside enumeration with %s annotaiton", AutoJacksonTypeClass.class);
//            return method -> method.addStatement("return null");
//        }
//
//        return method -> {
//            method.addStatement("$T codec = jp.getCodec()", ObjectCodec.class)
//                    .addStatement("$T rootNode = codec.readTree(jp)", JsonNode.class)
//                    .addStatement("$T typeNode = rootNode.get(\"type\")", JsonNode.class) // TODO get "type" from the correct spot
//                    .beginControlFlow("if (typeNode == null)")
//                    .addStatement("$T javaType = dc.constructType($T.$L)", JavaType.class, finalEnumElement, "class")
//                    // TODO finalEnumElement in the `throw` line below is incorrect
//                    .addStatement("throw new $T(jp, String.format(\"%s not present\", $T.$L.$L), javaType, null)", InvalidTypeIdException.class, finalEnumElement, "class", "getSimpleName()")
//                    .endControlFlow()
//                    .addStatement("$T type = codec.treeToValue(typeNode, $T.$L)", finalEnumElement, finalEnumElement, "class")
//                    .beginControlFlow("switch (type)");
//
//            enumValueElements.forEach(enumValueElement -> {
//                method.addCode("case $L:\n", enumValueElement);
//                method.addStatement("return codec.treeToValue(rootNode, $T.$L.$L)", finalEnumElement, enumValueElement, enumAccessorElement.get(0));
//            });
//
//            method.endControlFlow()
//                    .addStatement("return null");
//        };
//    }
//
//    private Consumer<MethodSpec.Builder> generateSimpleDeserializerLogic(TypeElement typeElement) {
//
//        Name className = typeElement.getSimpleName();
//
//        return method -> method
//                .addStatement("$T codec = jp.getCodec()", ObjectCodec.class)
//                .addStatement("$T rootNode = codec.readTree(jp)", JsonNode.class)
//                .addStatement("return codec.treeToValue(rootNode, $L_AutoJacksonImpl.$L)", className, "class");
//    }
//
//    private void generateCustomDeserializer(TypeElement typeElement, Consumer<MethodSpec.Builder> addLogicConsumer) {
//        Name className = typeElement.getSimpleName();
//
//
//        DeclaredType declaredType = this.typeUtils.getDeclaredType(typeElement);
//        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(StdDeserializer.class), TypeName.get(declaredType));
//
//        Builder deserializationBuilder = TypeSpec
//                .classBuilder(String.format("%s_AutoJacksonDeserializer", className))
//                .superclass(parameterizedTypeName)
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
//
//        MethodSpec constructor = MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PUBLIC)
//                .addStatement("super($T.$L)", typeElement, "class")
//                .build();
//
//        MethodSpec.Builder deserializeBuilder = MethodSpec.methodBuilder("deserialize")
//                .addParameter(ParameterSpec.builder(JsonParser.class, "jp").build())
//                .addParameter(ParameterSpec.builder(DeserializationContext.class, "dc").build())
//                .addAnnotation(Override.class)
//                .addException(ClassName.get(IOException.class))
//                .addException(ClassName.get(JsonProcessingException.class))
//                .returns(TypeName.get(declaredType))
//                .addModifiers(Modifier.PUBLIC);
//
//        addLogicConsumer.accept(deserializeBuilder);
//
//        deserializationBuilder
//                .addMethod(constructor)
//                .addMethod(deserializeBuilder.build());
//
//        // create the javaFile object
//        PackageElement packageElement = this.elementUtils.getPackageOf(typeElement);
//        JavaFile javaFile = JavaFile
//                .builder(packageElement.getQualifiedName().toString(), deserializationBuilder.build())
//                .build();
//
//        // and write that file to disc
//        try {
//            javaFile.writeTo(this.filer);
//        } catch (IOException e) {
//            error(typeElement, e.getMessage());
//        }
//    }
//
//    private void generateDefaultImplementation(TypeElement typeElement) {
//        Name className = typeElement.getSimpleName();
//
//        AnnotationSpec nonEmptyJsonAnnotation = AnnotationSpec.builder(JsonInclude.class)
//                .addMember("value", "$T.$L", Include.class, Include.NON_EMPTY)
//                .build();
//
//        // create the class that implements our interface
//        Builder autoJacksonDefaultBuilder = TypeSpec
//                .classBuilder(String.format("%s_AutoJacksonImpl", className))
//                .addSuperinterface(ClassName.get(typeElement))
//                .addAnnotation(nonEmptyJsonAnnotation)
//                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);
//
//        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
//        Stream<? extends Element> interfaceElements = interfaces.stream().flatMap(interfaceTypeMirror -> {
//            Element element = this.typeUtils.asElement(interfaceTypeMirror);
//            return element.getEnclosedElements().stream();
//        });
//
//        // setup the member variables
//        List<Variable> memberVariables = Stream.concat(interfaceElements, typeElement.getEnclosedElements().stream())
//                .map(enclosedElement -> {
//                    // check that the element is a method of the interface
//                    if (enclosedElement.getKind() == ElementKind.METHOD) {
//                        return Optional.of(implementMethodUsingMemberVariable((ExecutableElement) enclosedElement, autoJacksonDefaultBuilder));
//                    } else {
//                        return Optional.<Variable>empty();
//                    }
//                })
//                .filter(Optional::isPresent)
//                .map(Optional::get)
//                .collect(Collectors.toList());
//
//        // create the constructor that assigns those member variables
//        autoJacksonDefaultBuilder.addMethod(createConstructor(memberVariables));
//
//        // create the javaFile object
//        PackageElement packageElement = this.elementUtils.getPackageOf(typeElement);
//        JavaFile javaFile = JavaFile
//                .builder(packageElement.getQualifiedName().toString(), autoJacksonDefaultBuilder.build())
//                .build();
//
//        // and write that file to disc
//        try {
//            javaFile.writeTo(this.filer);
//        } catch (IOException e) {
//            error(typeElement, e.getMessage());
//        }
//    }
//
//    private MethodSpec createConstructor(List<Variable> memberVariables) {
//        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
//                .addModifiers(Modifier.PUBLIC);
//
//        memberVariables.forEach(memberVariable -> {
//            TypeName typeName = memberVariable.getTypeName();
//            String variableName = memberVariable.getVariableName();
//
//            String staticName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, variableName);
//
//            AnnotationSpec.Builder jacksonAnnotationBuilder = AnnotationSpec.builder(JsonProperty.class)
//                    .addMember("value", "$L", staticName);
//
//            if (memberVariable.isRequired()) {
//                jacksonAnnotationBuilder.addMember("required", "true");
//            }
//
//            AnnotationSpec jacksonAnnotation = jacksonAnnotationBuilder.build();
//
//            ParameterSpec parameter = ParameterSpec.builder(typeName, variableName)
//                    .addAnnotation(jacksonAnnotation)
//                    .build();
//
//            constructorBuilder.addParameter(parameter);
//            constructorBuilder.addStatement("this.$N = $N", variableName, variableName);
//        });
//
//        return constructorBuilder.build();
//    }
//
//    private Variable implementMethodUsingMemberVariable(ExecutableElement method, Builder autoJacksonDefaultBuilder) {
//        // load the return type to set the type of the member variable
//        TypeName returnTypeName = ClassName.get(method.getReturnType());
//
//        // load the name of the member variable
//        final String memberVariableName;
//        Named namedAnnotation = method.getAnnotation(Named.class);
//        if (namedAnnotation != null) {
//            memberVariableName = namedAnnotation.value(); // the error message for an invalid name is verbose enough to not manually check
//        } else {
//            String methodName = method.getSimpleName().toString();
//            if (!methodName.startsWith("get")) {
//                error(method, "Method name %s must be in the format 'getXXX()' if no %s parameter is given", methodName, Named.class);
//            }
//            memberVariableName = methodName.substring("get".length(), methodName.length()).toLowerCase();
//        }
//
//        String staticName = CaseFormat.UPPER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, memberVariableName);
//
//        AnnotationSpec.Builder jacksonAnnotationBuilder = AnnotationSpec.builder(JsonProperty.class)
//                .addMember("value", "$L", staticName);
//
//        TypeMirror optional = this.elementUtils.getTypeElement(Optional.class.getCanonicalName()).asType();
//        TypeMirror erasure = this.typeUtils.erasure(method.getReturnType());
//        boolean optionalItem = this.typeUtils.isAssignable(optional, erasure);
//        if (!optionalItem) {
//            jacksonAnnotationBuilder.addMember("required", "true");
//        }
//
//        AnnotationSpec jacksonAnnotation = jacksonAnnotationBuilder.build();
//
//        // create the override accessor method
//        MethodSpec implementedMethod = MethodSpec.overriding(method)
//                .addStatement("return this.$N", memberVariableName)
//                .build();
//
//        // create the field
//        FieldSpec field = FieldSpec.builder(returnTypeName, memberVariableName, Modifier.PRIVATE, Modifier.FINAL)
//                .addAnnotation(jacksonAnnotation)
//                .build();
//
//
//        // create the field
//        FieldSpec constant = FieldSpec.builder(String.class, staticName, Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
//                .initializer("$S", memberVariableName)
//                .build();
//
//        // add the field and method which will use the new field
//        autoJacksonDefaultBuilder
//                .addField(constant)
//                .addField(field)
//                .addMethod(implementedMethod);
//
//        return new Variable(returnTypeName, memberVariableName, !optionalItem);
//    }
//
//    private String loadClassName(AutoJackson annotation) {
//        try {
//            Class<?> clazz = annotation.type().value();
//            return clazz.getCanonicalName();
//        } catch (MirroredTypeException mte) {
//            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
//            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
//            return classTypeElement.getQualifiedName().toString();
//        }
//    }
//
//    private void error(Element e, String msg, Object... args) {
//        messager.printMessage(
//                Diagnostic.Kind.ERROR,
//                String.format(msg, args),
//                e);
//    }
//
//    private class Variable {
//        private final TypeName typeName;
//        private final String variableName;
//        private final boolean required;
//
//        private Variable(TypeName typeName, String variableName, boolean required) {
//            this.typeName = typeName;
//            this.variableName = variableName;
//            this.required = required;
//        }
//
//        private String getVariableName() {
//            return this.variableName;
//        }
//
//        private TypeName getTypeName() {
//            return this.typeName;
//        }
//
//        private boolean isRequired() {
//            return this.required;
//        }
//    }
}
