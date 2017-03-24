package peckb1.processor.util;

import com.google.common.base.CaseFormat;
import peckb1.processor.AutoJackson;
import peckb1.processor.AutoJackson.Type;
import peckb1.processor.Named;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

public final class ProcessorUtil {

    private final static String USUAL_ACCESSOR_PREFIX = "get";

    private final Elements elementUtils;
    private final Messager messager;

    public ProcessorUtil(Elements elementUtils, Messager messager) {
        this.elementUtils = elementUtils;
        this.messager = messager;
    }

    /**
     * Fetch the TypeElement for the value of our {@link Type#value()} stored
     * within our annotation
     *
     * @param annotation The annotation to fetch the value for
     * @return The TypeElement representing the class stored as our value
     */
    public TypeElement getTypeElement(AutoJackson annotation) {
        try {
            Class<?> clazz = annotation.type().value();
            return this.elementUtils.getTypeElement(clazz.getCanonicalName());
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            return (TypeElement) classTypeMirror.asElement();
        }
    }

    /**
     * logs an error to our messenger
     *
     * @param e    The element the error applies to
     * @param msg  The message string to report
     * @param args Any arguments to fill out the message with
     */
    public void error(Element e, String msg, Object... args) {
        this.messager.printMessage(Diagnostic.Kind.ERROR, String.format(msg, args), e);
    }

    public String createMemberVariableName(ExecutableElement method) {
        Named namedAnnotation = method.getAnnotation(Named.class);
        if (namedAnnotation == null) {
            // convert method name to a variable name
            String methodName = method.getSimpleName().toString();
            if (methodName.startsWith(USUAL_ACCESSOR_PREFIX)) {
                String substring = methodName.substring(USUAL_ACCESSOR_PREFIX.length(), methodName.length());
                return CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_CAMEL, substring);
            } else {
                return methodName;
            }
        } else {
            return namedAnnotation.value();
        }
    }
}
