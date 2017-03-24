package peckb1.processor.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.IOException;

abstract class DeserializerCreator {

    private final static String DESERIALIZER_CLASS_NAME_SUFFIX = "_AutoJacksonDeserializer";

    final static String JSON_PARSER_PARAMETER_NAME = "jsonParser";
    final static String DESERIALIZATION_CONTEXT_PARAMETER_NAME = "context";

    final Types typeUtils;
    final Elements elementUtils;
    final Filer filer;
    final ProcessorUtil processorUtil;

    DeserializerCreator(Types typeUtils, Elements elementUtils, Filer filer, ProcessorUtil processorUtil) {
        this.typeUtils = typeUtils;
        this.elementUtils = elementUtils;
        this.filer = filer;
        this.processorUtil = processorUtil;
    }

    public void createDeserializer(TypeElement typeElement) {
        Name className = typeElement.getSimpleName();

        DeclaredType declaredType = this.typeUtils.getDeclaredType(typeElement);
        ClassName stdDeserializer = ClassName.get(StdDeserializer.class);
        TypeName deserializerType = TypeName.get(declaredType);
        ParameterizedTypeName parameterizedDeserializer = ParameterizedTypeName.get(stdDeserializer, deserializerType);

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC)
                .addStatement("super($T.class)", typeElement)
                .build();

        ParameterSpec jsonParserParameter = ParameterSpec.builder(JsonParser.class, JSON_PARSER_PARAMETER_NAME)
                .build();
        ParameterSpec deserializationContextParameter = ParameterSpec.builder(DeserializationContext.class, DESERIALIZATION_CONTEXT_PARAMETER_NAME)
                .build();

        MethodSpec.Builder deserializeMethodBuilder = MethodSpec.methodBuilder("deserialize")
                .addAnnotation(Override.class)
                .addParameter(jsonParserParameter)
                .addParameter(deserializationContextParameter)
                .addException(ClassName.get(IOException.class))
                .addException(ClassName.get(JsonProcessingException.class))
                .returns(TypeName.get(declaredType))
                .addModifiers(Modifier.PUBLIC);

        MethodSpec deserializeMethod = implementDeserializeMethod(typeElement, deserializeMethodBuilder);

        TypeSpec deserializationClass = TypeSpec
                .classBuilder(className + DESERIALIZER_CLASS_NAME_SUFFIX)
                .superclass(parameterizedDeserializer)
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .addMethod(deserializeMethod)
                .build();

        PackageElement packageElement = this.elementUtils.getPackageOf(typeElement);
        JavaFile javaFile = JavaFile
                .builder(packageElement.getQualifiedName().toString(), deserializationClass)
                .build();

        // and write that file to disc
        try {
            javaFile.writeTo(this.filer);
        } catch (IOException e) {
            this.processorUtil.error(typeElement, e.getMessage());
        }
    }

    protected abstract MethodSpec implementDeserializeMethod(TypeElement typeElement, Builder deserializeMethodBuilder);
}
