package peckb1.processor.util;

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.JsonNode;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.MethodSpec.Builder;
import peckb1.processor.AutoJackson;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

/**
 * A {@link DeserializerCreator} meant for simple interfaces annotated
 * with {@link peckb1.processor.AutoJackson} that do not specify any
 * classes inside the {@link AutoJackson#type()} field.
 */
public class SimpleDeserializerCreator extends DeserializerCreator {

    public SimpleDeserializerCreator(Types typeUtils, Elements elementUtils, Filer filer, ProcessorUtil processorUtil) {
        super(typeUtils, elementUtils, filer, processorUtil);
    }

    @Override
    protected MethodSpec implementDeserializeMethod(TypeElement typeElement, Builder deserializeMethodBuilder) {
        Name className = typeElement.getSimpleName();

        return deserializeMethodBuilder.addStatement("$T codec = $L.getCodec()", ObjectCodec.class, JSON_PARSER_PARAMETER_NAME)
                .addStatement("$T rootNode = codec.readTree($L)", JsonNode.class, JSON_PARSER_PARAMETER_NAME)
                .addStatement("return codec.treeToValue(rootNode, $L$L.class)", className, ImplementationCreator.CLASS_IMPLEMENTATION_NAME_SUFFIX)
                .build();
    }

}
