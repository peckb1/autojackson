package peckb1.processor.util;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import java.io.IOException;

import static com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility.NONE;
import static com.fasterxml.jackson.annotation.PropertyAccessor.ALL;
import static peckb1.processor.util.DeserializerCreator.DESERIALIZER_CLASS_NAME_SUFFIX;

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

        MethodSpec.Builder moduleGenerator = MethodSpec.methodBuilder("configureObjectMapper")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(ParameterSpec.builder(ObjectMapper.class, "objectMapper").build())
                .addStatement("$T deserialzationModule = new $T()", SimpleModule.class, SimpleModule.class);

        interfaces.forEach(element -> {
            String deserializationPackage = ClassName.get(element).packageName();
            ClassName deserializerClassName = ClassName.get(deserializationPackage, element.getSimpleName() + DESERIALIZER_CLASS_NAME_SUFFIX);
            moduleGenerator.addStatement("deserialzationModule.addDeserializer($T.class, new $T())", element, deserializerClassName);
        });

        moduleGenerator
                .addStatement("objectMapper.registerModule(deserialzationModule)")
                .addStatement("objectMapper.setVisibility($T.$L, $T.$L)", PropertyAccessor.class, ALL, Visibility.class, NONE)
                .build();

        TypeSpec setupClass = TypeSpec
                .classBuilder("AutoJacksonSetup")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(constructor)
                .addMethod(moduleGenerator.build())
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
