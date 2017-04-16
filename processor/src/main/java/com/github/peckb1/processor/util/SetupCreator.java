package com.github.peckb1.processor.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.github.peckb1.processor.AutoJackson;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import java.io.IOException;
import java.util.List;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.PropertyAccessor.ALL;
import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.github.peckb1.processor.util.DeserializerCreator.DESERIALIZER_CLASS_NAME_SUFFIX;

/**
 * Creates a single class that can be used to setup an {@link ObjectMapper}
 * with the deserializers and Jackson settings needed to allow the
 * {@link AutoJackson} to function correctly.
 * <p>
 * The class created is only a helper method to avoid the boiler plate of
 * adding in each deserializer created for each class annotated with
 * our {@link AutoJackson} annotation. But the steps
 * performed inside can be done manually by the user if wanted.
 */
public class SetupCreator {

    private final Filer filer;
    private final ProcessorUtil processorUtil;

    public SetupCreator(Filer filer, ProcessorUtil processorUtil) {
        this.filer = filer;
        this.processorUtil = processorUtil;
    }

    public void createSetupClass(ImmutableList<TypeElement> interfaces) {

        MethodSpec constructor = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .build();

        MethodSpec.Builder configurationMethodBuilder = MethodSpec.methodBuilder("configureObjectMapper")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(ObjectMapper.class, "objectMapper").build())
                .addStatement("$T deserialzationModule = new $T()", SimpleModule.class, SimpleModule.class);

        interfaces.forEach(element -> {
            String deserializationPackage = ClassName.get(element).packageName();
            ClassName deserializerClassName = ClassName.get(deserializationPackage, element.getSimpleName() + DESERIALIZER_CLASS_NAME_SUFFIX);
            if (element.getTypeParameters().isEmpty()) {
                configurationMethodBuilder.addStatement("deserialzationModule.addDeserializer($T.class, new $T())", element, deserializerClassName);
            } else {
                configurationMethodBuilder.addStatement("deserialzationModule.addDeserializer($L.class, new $T())", element.getQualifiedName(), deserializerClassName);
            }
        });

        configurationMethodBuilder
                .addStatement("objectMapper.registerModule(deserialzationModule)")
                .addStatement("objectMapper.setVisibility($T.$L, $T.$L)", PropertyAccessor.class, ALL, Visibility.class, NONE)
                .addStatement("objectMapper.configure($T.$L, $L)", DeserializationFeature.class, FAIL_ON_UNKNOWN_PROPERTIES, false)
                .build();

        TypeSpec setupClass = TypeSpec
                .classBuilder("AutoJacksonSetup")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .addMethod(configurationMethodBuilder.build())
                .build();

        JavaFile javaFile = JavaFile
                .builder("peckb1.autojackson", setupClass)
                .build();

        try {
            javaFile.writeTo(this.filer);
        } catch (IOException e) {
            this.processorUtil.error(null, e.getMessage());
        }
    }

}
