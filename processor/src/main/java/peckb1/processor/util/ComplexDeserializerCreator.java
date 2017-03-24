package peckb1.processor.util;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.InvalidTypeIdException;
import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import peckb1.processor.AutoJackson;
import peckb1.processor.AutoJacksonTypeClass;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ComplexDeserializerCreator extends DeserializerCreator {

    public ComplexDeserializerCreator(Types typeUtils, Elements elementUtils, Filer filer, ProcessorUtil processorUtil) {
        super(typeUtils, elementUtils, filer, processorUtil);
    }

    @Override
    protected MethodSpec implementDeserializeMethod(TypeElement typeElement, Builder deserializeMethodBuilder) {
        Optional<DeserializationConstructs> constructs = loadConstructs(typeElement);
        if (!constructs.isPresent()) {
            return deserializeMethodBuilder.addStatement("return null").build();
        }

        TypeElement enumTypeElement = constructs.get().getEnumTypeElement();
        ImmutableList<Element> enumValueElements = constructs.get().getEnumValueElements();
        ExecutableElement enumValueAccessorMethod = constructs.get().getEnumValueAccessorMethod();
        ExecutableElement enumInstanceAccessorMethod = constructs.get().getEnumInstanceAccessorMethod();

        String memberVariableName = this.processorUtil.createMemberVariableName(enumValueAccessorMethod);

        deserializeMethodBuilder.addStatement("$T codec = $L.getCodec()", ObjectCodec.class, JSON_PARSER_PARAMETER_NAME)
                .addStatement("$T rootNode = codec.readTree($L)", JsonNode.class, JSON_PARSER_PARAMETER_NAME)
                .addStatement("$T typeNode = rootNode.get($S)", JsonNode.class, memberVariableName)
                .beginControlFlow("if (typeNode == null)")
                .addStatement("$T javaType = $L.constructType($T.class)", JavaType.class, DESERIALIZATION_CONTEXT_PARAMETER_NAME, enumTypeElement)
                .addStatement("throw new $T($L, \"$L not present\", javaType, null)", InvalidTypeIdException.class, JSON_PARSER_PARAMETER_NAME, memberVariableName)
                .endControlFlow()
                .addStatement("$T type = codec.treeToValue(typeNode, $T.$L)", enumTypeElement, enumTypeElement, "class")
                .beginControlFlow("switch (type)");

        enumValueElements.forEach(enumValueElement -> deserializeMethodBuilder
                .beginControlFlow("case $L:", enumValueElement)
                .addStatement("return codec.treeToValue(rootNode, $T.$L.$L)", enumTypeElement, enumValueElement, enumInstanceAccessorMethod)
                .endControlFlow());

        return deserializeMethodBuilder.beginControlFlow("default :")
                .addStatement("return null")
                .endControlFlow()
                .endControlFlow()
                .build();
    }

    private Optional<DeserializationConstructs> loadConstructs(TypeElement typeElement) {
        TypeElement enumTypeElement = this.processorUtil.getTypeElement(typeElement.getAnnotation(AutoJackson.class));

        Optional<ExecutableElement> methodReturningEnum = loadEnumMethod(typeElement, enumTypeElement);
        if (!methodReturningEnum.isPresent()) {
            this.processorUtil.error(typeElement, "There must be a single method returning %s inside %s", enumTypeElement, typeElement);
            return Optional.empty();
        }

        if (enumTypeElement.getKind() != ElementKind.ENUM) {
            this.processorUtil.error(enumTypeElement, "The type of object for %s must be an enum", AutoJackson.Type.class.getSimpleName());
            return Optional.empty();
        }

        ImmutableList.Builder<Element> constantElementsBuilder = ImmutableList.builder();
        ImmutableList.Builder<ExecutableElement> accessorMethodBuilder = ImmutableList.builder();

        List<? extends Element> enclosedElements = enumTypeElement.getEnclosedElements();
        enclosedElements.forEach(enclosedElement -> {
            if (enclosedElement.getKind() == ElementKind.ENUM_CONSTANT) {
                constantElementsBuilder.add(enclosedElement);
            }
            if (enclosedElement.getAnnotation(AutoJacksonTypeClass.class) != null && enclosedElement.getKind() == ElementKind.METHOD) {
                accessorMethodBuilder.add((ExecutableElement) enclosedElement);
            }
        });

        ImmutableList<Element> enumValueElements = constantElementsBuilder.build();
        ImmutableList<ExecutableElement> enumAccessorElement = accessorMethodBuilder.build();

        if (enumValueElements.isEmpty()) {
            this.processorUtil.error(enumTypeElement, "The enum inside the type must have some values");
            return Optional.empty();
        }

        if (enumAccessorElement.size() != 1) {
            this.processorUtil.error(enumTypeElement, "No accessor method inside enumeration with %s annotaiton", AutoJacksonTypeClass.class);
            return Optional.empty();
        }

        return Optional.of(new DeserializationConstructs(enumTypeElement, enumValueElements, methodReturningEnum.get(), enumAccessorElement.get(0)));
    }

    /**
     * <p>
     * Loads the method from inside our class needing a custom deserializer which
     * is used to find out the type of class needed.
     * </p><p>
     * For example if the following class was given as the TypeElement, and FraggleName was given
     * as the enumTypeElement then `FraggleName getName()` would be the ExecutableElement returned
     * </p>
     * {@code
     *
     * @param typeElement     The class containing the methods to return
     * @param enumTypeElement The return type to look for in the method
     * @return An Optional containing the single method returning the given type, if only 1 is found.
     * @AutoJackson(type = @AutoJackson.Type(FraggleName.class))
     * public interface Fraggle {
     * FraggleName getName();
     * @Named("occupation") String getJob();
     * enum FraggleName {
     * ...
     * }
     * }
     * }
     */
    private Optional<ExecutableElement> loadEnumMethod(TypeElement typeElement, TypeElement enumTypeElement) {
        List<ExecutableElement> methodsReturningEnum = typeElement.getEnclosedElements().stream()
                .filter(element -> element.getKind() == ElementKind.METHOD)
                .map(element -> (ExecutableElement) element)
                .filter(executableElement -> this.typeUtils.isSameType(executableElement.getReturnType(), enumTypeElement.asType()))
                .collect(Collectors.toList());

        if (methodsReturningEnum.size() != 1) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(methodsReturningEnum.get(0));
        }
    }

    /**
     * A helper class to allow our validation method to return the necessary constructs
     * needed to build the complex deserializer class
     */
    private static class DeserializationConstructs {

        private final TypeElement enumTypeElement;
        private final ImmutableList<Element> enumValueElements;
        private final ExecutableElement enumValueAccessorMethod;
        private final ExecutableElement enumInstanceAccessorMethod;

        private DeserializationConstructs(TypeElement enumTypeElement,
                                          ImmutableList<Element> enumValueElements,
                                          ExecutableElement enumValueAccessorMethod,
                                          ExecutableElement enumInstanceAccessorMethod) {
            this.enumTypeElement = enumTypeElement;
            this.enumValueElements = enumValueElements;
            this.enumValueAccessorMethod = enumValueAccessorMethod;
            this.enumInstanceAccessorMethod = enumInstanceAccessorMethod;
        }

        private ExecutableElement getEnumInstanceAccessorMethod() {
            return this.enumInstanceAccessorMethod;
        }

        private TypeElement getEnumTypeElement() {
            return this.enumTypeElement;
        }

        private ImmutableList<Element> getEnumValueElements() {
            return this.enumValueElements;
        }

        private ExecutableElement getEnumValueAccessorMethod() {
            return this.enumValueAccessorMethod;
        }
    }
}
