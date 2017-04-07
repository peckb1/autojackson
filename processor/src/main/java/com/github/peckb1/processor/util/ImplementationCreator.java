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
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

        Set<MethodDetail> methodDetails = loadMethodDetails(typeElement);

        methodDetails.forEach(methodDetail -> {
            ExecutableElement method = methodDetail.element;
            TypeMirror returnType = methodDetail.differentReturnType.orElse(method.getReturnType());
            TypeName returnTypeName = ClassName.get(returnType);
            String memberVariableName = this.processorUtil.createMemberVariableName(method);
            String constantName = CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_UNDERSCORE, memberVariableName);
            AnnotationSpec jsonPropertyAnnotation = createJsonPropertyAnnotation(returnType, constantName);

            classBuilder.addMethod(MethodSpec.overriding(method)
                    .returns(TypeName.get(returnType))
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

    private Set<MethodDetail> loadMethodDetails(TypeElement typeElement) {
        Set<MethodDetail> methodDetails = new TreeSet<>();

        // load the methods as part of this type element
        List<ExecutableElement> myMethods = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());

        for (ExecutableElement method : myMethods) {
            if (isValidMethod(method)) {
                methodDetails.add(new MethodDetail(method));
            }
        }

        // and check for methods from our parent
        typeElement.getInterfaces().stream()
                .filter(tm -> tm.getKind() == TypeKind.DECLARED)
                .map(this::loadParentMethodDetails)
                .forEach(details -> details.forEach(methodDetails::add));

        return methodDetails;
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

    private Set<MethodDetail> loadParentMethodDetails(TypeMirror mirror) {
        DeclaredType declaredType = (DeclaredType) mirror;
        TypeElement typeElement = (TypeElement) (declaredType).asElement();

        List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

        Set<MethodDetail> methodDetails = new TreeSet<>();

        // load the methods as part of this type element
        List<ExecutableElement> myMethods = typeElement.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.METHOD)
                .map(e -> (ExecutableElement) e)
                .collect(Collectors.toList());

        for (ExecutableElement method : myMethods) {
            if (!isValidMethod(method)) {
                continue;
            }

            List<? extends TypeParameterElement> typeParameters = typeElement.getTypeParameters();
            if (typeParameters.isEmpty()) {
                methodDetails.add(new MethodDetail(method));
                continue;
            }

            Predicate<? super TypeParameterElement> predicate = tpe ->
                    tpe.asType().getKind() == TypeKind.TYPEVAR && tpe.asType().getKind().equals(method.getReturnType().getKind());

            Map<Boolean, List<TypeParameterElement>> methods = typeParameters.stream().collect(Collectors.partitioningBy(predicate));

            methods.forEach((customReturnType, typeParameterElements) -> {
                if (typeParameterElements.isEmpty()) {
                    return;
                }
                if (customReturnType) {
                    typeParameterElements.forEach(tpe -> {
                        final TypeMirror returnType;
                        int parameterIndex = typeParameters.indexOf(tpe);
                        if (typeArguments.size() > parameterIndex) {
                            returnType = typeArguments.get(parameterIndex);
                        } else {
                            returnType = ((TypeVariable) tpe.asType()).getUpperBound();
                        }
                        methodDetails.add(new MethodDetail(method, Optional.of(returnType)));
                    });
                } else {
                    methodDetails.add(new MethodDetail(method));
                }
            });
        }

        // and check for methods from our parent
        typeElement.getInterfaces().stream()
                .filter(tm -> tm.getKind() == TypeKind.DECLARED)
                .map(this::loadParentMethodDetails)
                .forEach(details -> details.forEach(methodDetails::add));

        return methodDetails;
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

    private class MethodDetail implements Comparable<MethodDetail> {

        private final ExecutableElement element;
        private final Optional<TypeMirror> differentReturnType;

        private MethodDetail(ExecutableElement element) {
            this(element, Optional.empty());
        }

        private MethodDetail(ExecutableElement element, Optional<TypeMirror> differentReturnType) {
            this.element = element;
            this.differentReturnType = differentReturnType;
        }

        @Override
        public int compareTo(MethodDetail o) {
            return this.element.getSimpleName().toString().compareTo(o.element.getSimpleName().toString());
        }
    }
}
