package com.github.peckb1.processor.util;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.peckb1.processor.AutoJackson;
import com.google.common.base.CaseFormat;
import com.squareup.javapoet.AnnotationSpec;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeVariableName;

import javax.annotation.processing.Filer;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A creator which implements interfaces annotated with {@link AutoJackson}
 * with the needed Jackson annotations to facilitate serialization and deserialization
 * through an {@link com.fasterxml.jackson.databind.ObjectMapper}
 */
public class ImplementationCreator {

    final static String CLASS_IMPLEMENTATION_NAME_SUFFIX = "_AutoJacksonImpl";

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

    /**
     * Creates a Java source file at the same package level of the given
     * {@link TypeElement} interface which implements it and contains all
     * of the needed Jackson {@link JsonProperty} annotations for serialization
     * and deserialization.
     *
     * @param typeElement The interface to create the implementation for
     */
    public void implementInterface(TypeElement typeElement) {
        Name className = typeElement.getSimpleName();

        // class level annotation to only deserialize existent objects
        AnnotationSpec nonEmptyJsonAnnotation = AnnotationSpec.builder(JsonInclude.class)
                .addMember("value", "$T.$L", Include.class, Include.NON_EMPTY)
                .build();

        List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
        Iterable<TypeVariableName> typeVariableNames = typeParameters.stream()
                .filter(tp -> tp.asType().getKind() == TypeKind.TYPEVAR)
                .map(tp -> (TypeVariable) tp.asType())
                .map(TypeVariableName::get)
                .collect(Collectors.toList());

        TypeSpec.Builder classBuilder = TypeSpec
                .classBuilder(className + CLASS_IMPLEMENTATION_NAME_SUFFIX)
                .addSuperinterface(ClassName.get(typeElement))
                .addAnnotation(nonEmptyJsonAnnotation)
                .addTypeVariables(typeVariableNames)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        Set<MethodDetail> methods = loadMethodDetails(typeElement);
        methods.forEach(methodDetail -> {
            // for each accessor method we need to create, let's populate our class
            ExecutableElement method = methodDetail.getElement();
            TypeMirror returnType = methodDetail.getDifferentReturnType().orElse(method.getReturnType());
            TypeName returnTypeName = ClassName.get(returnType);
            String memberVariableName = this.processorUtil.createMemberVariableName(method);
            String constantName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, memberVariableName);

            // create the annotation used by our constructor parameter and member variable
            AnnotationSpec jsonPropertyAnnotation = createJsonPropertyAnnotation(returnType, constantName);

            // add the actual method itself
            classBuilder.addMethod(MethodSpec.overriding(method)
                    .returns(TypeName.get(returnType))
                    .addStatement("return $L", memberVariableName)
                    .build());
            // create the field that the method will return
            classBuilder.addField(FieldSpec.builder(returnTypeName, memberVariableName, Modifier.PRIVATE, Modifier.FINAL)
                    .addAnnotation(jsonPropertyAnnotation)
                    .build());
            // create the constant used by our annotations
            classBuilder.addField(FieldSpec.builder(String.class, constantName, Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL)
                    .initializer("$S", memberVariableName)
                    .build());
            // add a parameter to our constructor which sets our new field, for our new method
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

    private Set<MethodDetail> loadMethodDetails(TypeElement typeElement) {
        Set<MethodDetail> methodDetails = new TreeSet<>();

        // load the methods as part of this type element
        loadMethods(typeElement)
                .filter(this::isValidMethod)
                .forEach(method -> methodDetails.add(new MethodDetail(method)));

        // and check for methods from our parent
        typeElement.getInterfaces().stream()
                .filter(tm -> tm.getKind() == TypeKind.DECLARED)
                .map(this::loadParentMethodDetails)
                .forEach(details -> details.forEach(methodDetails::add));

        return methodDetails;
    }

    private Set<MethodDetail> loadParentMethodDetails(TypeMirror mirror) {
        Set<MethodDetail> methodDetails = new TreeSet<>();

        // swap the types of our mirror so we can load our data
        DeclaredType declaredType = (DeclaredType) mirror;
        TypeElement typeElement = (TypeElement) (declaredType).asElement();

        // load the methods as part of this type element
        loadMethods(typeElement)
                .filter(this::isValidMethod)
                .forEach(method -> methodDetails.add(checkParentMethod(declaredType, method)));

        // and check for methods from our parent
        typeElement.getInterfaces().stream()
                .filter(tm -> tm.getKind() == TypeKind.DECLARED)
                .map(this::loadParentMethodDetails)
                .forEach(details -> details.forEach(methodDetails::add));

        return methodDetails;
    }

    private MethodDetail checkParentMethod(DeclaredType declaredType, ExecutableElement method) {
        // check to see if we have any type parameters at all, if we don't then we can just create
        // the standard method
        TypeElement typeElement = (TypeElement) (declaredType).asElement();
        List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
        if (typeParameters.isEmpty()) {
            new MethodDetail(method);
        }

        // otherwise we need to check if our return type is actually one of our generic parameters
        // first we need to check if our method return type matches one of our Type Parameters
        Optional<? extends TypeParameterElement> element = typeParameters.stream()
                .filter(parameterElement -> parameterElement.asType().getKind().equals(method.getReturnType().getKind()))
                .findFirst();

        return element.map(matchingType -> {
            final List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();
            final int parameterIndex = typeParameters.indexOf(matchingType);
            final TypeMirror returnType;

            if (typeArguments.size() > parameterIndex) {
                returnType = typeArguments.get(parameterIndex);
            } else {
                returnType = ((TypeVariable) matchingType.asType()).getUpperBound();
            }

            return new MethodDetail(method, Optional.of(returnType));
        }).orElse(new MethodDetail(method));
    }

    private Stream<ExecutableElement> loadMethods(TypeElement typeElement) {
        return typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e);
    }

    private boolean isValidMethod(ExecutableElement method) {
        boolean error = true;
        List<? extends VariableElement> methodParameters = method.getParameters();
        if (!methodParameters.isEmpty()) {
            this.processorUtil.error(method, "Methods inside AutoJackson classes should not have method parameters.");
            error = false;
        }
        List<? extends TypeParameterElement> methodTypeParameters = method.getTypeParameters();
        if (!methodTypeParameters.isEmpty()) {
            this.processorUtil.error(method, "Methods inside AutoJackson methods should not have type parameters.");
            error = false;
        }
        return error;
    }

    /**
     * Creates the {@link JsonProperty} annotation to apply to a parameter, or member variable
     *
     * @param returnType   The return type of the item (to check for {@link Optional})
     * @param constantName The name of the constant which holds our {@link JsonProperty#value()}
     * @return An Annotation that can be applied using JavaPoet
     */
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
}
