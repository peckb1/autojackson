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

}
