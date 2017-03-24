package peckb1.processor.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import peckb1.processor.Named;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class ImplementationCreator {

    public final static String CLASS_IMPLEMENTATION_NAME_SUFFIX = "_AutoJacksonImpl";

    private final Types typeUtils;
    private final Elements elementUtils;
    private final Filer filer;
    private final ProcessorUtil processorUtil;

    public ImplementationCreator(Types typeUtils, Elements elementUtils, Filer filer, ProcessorUtil processorUtil) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.filer = filer;
        this.processorUtil = processorUtil;
    }

    public void implementInterface(TypeElement typeElement) {
        Name className = typeElement.getSimpleName();

        // class level annotation to only deserialize existent objects
        AnnotationSpec nonEmptyJsonAnnotation = AnnotationSpec.builder(JsonInclude.class)
                .addMember("value", "$T.$L", Include.class, Include.NON_EMPTY)
                .build();

        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(className + CLASS_IMPLEMENTATION_NAME_SUFFIX)
                .addSuperinterface(ClassName.get(typeElement))
                .addAnnotation(nonEmptyJsonAnnotation)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        Stream<? extends ExecutableElement> methodsStream = loadMethodsToImplement(typeElement);
        methodsStream.forEach(method -> {
            TypeMirror returnType = method.getReturnType();
            TypeName returnTypeName = ClassName.get(returnType);
            String memberVariableName = this.processorUtil.createMemberVariableName(method);
            String constantName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, memberVariableName);
            AnnotationSpec jsonPropertyAnnotation = createJsonPropertyAnnotation(returnType, constantName);

            classBuilder.addMethod(MethodSpec.overriding(method)
                    .addStatement("return $L", memberVariableName)
                    .build());
            classBuilder.addField(FieldSpec.builder(returnTypeName, memberVariableName, Modifier.PRIVATE, Modifier.FINAL)
                    .addAnnotation(jsonPropertyAnnotation)
                    .build());
            classBuilder.addField(FieldSpec.builder(String.class, constantName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", memberVariableName)
                    .build());

            constructorBuilder.addStatement("this.$L = $L", memberVariableName, memberVariableName);
            constructorBuilder.addParameter(ParameterSpec.builder(returnTypeName, memberVariableName)
                    .addAnnotation(jsonPropertyAnnotation)
                    .build());
        });

        TypeSpec typeSpec = classBuilder.addMethod(constructorBuilder.build()).build();

        PackageElement packageElement = this.elementUtils.getPackageOf(typeElement);
        JavaFile javaFile = JavaFile
                .builder(packageElement.getQualifiedName().toString(), typeSpec)
                .build();

        try {
            javaFile.writeTo(this.filer);
        } catch (IOException e) {
            this.processorUtil.error(typeElement, e.getMessage());
        }
    }

    private AnnotationSpec createJsonPropertyAnnotation(TypeMirror returnType, String constantName) {
        TypeMirror optional = this.elementUtils.getTypeElement(Optional.class.getCanonicalName()).asType();
        TypeMirror erasure = this.typeUtils.erasure(returnType);
        boolean optionalItem = this.typeUtils.isAssignable(optional, erasure);

        AnnotationSpec.Builder builder = AnnotationSpec.builder(JsonProperty.class).addMember("value", constantName);

        if (!optionalItem) {
            builder.addMember("required", "true");
        }

        return builder.build();
    }

    /**
     * Loads the methods of our main interface, and any interfaces that it implements
     *
     * @param typeElement The element to load methods from
     * @return A Stream of ExecutableElement objects to create implementations for
     */
    private Stream<? extends ExecutableElement> loadMethodsToImplement(TypeElement typeElement) {
        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        Stream<? extends Element> parentInterfaceElements = interfaces.stream().flatMap(interfaceTypeMirror -> {
            Element element = this.typeUtils.asElement(interfaceTypeMirror);
            return element.getEnclosedElements().stream();
        });

        return Stream.concat(typeElement.getEnclosedElements().stream(), parentInterfaceElements)
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .map(element -> (ExecutableElement) element);
    }
}
