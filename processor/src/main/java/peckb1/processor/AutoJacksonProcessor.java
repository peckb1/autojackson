package peckb1.processor;

import com.google.auto.service.AutoService;
import com.google.common.collect.Sets;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.TypeSpec.Builder;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutoService(Processor.class)
public class AutoJacksonProcessor extends AbstractProcessor {

    private Types typeUtils;
    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.typeUtils = processingEnv.getTypeUtils();
        this.elementUtils = processingEnv.getElementUtils();
        this.filer = processingEnv.getFiler();
        this.messager = processingEnv.getMessager();
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
        // Itearate over all @AutoJackson annotated elements
        for (Element annotatedElement : roundEnv.getElementsAnnotatedWith(AutoJackson.class)) {

            // Check if an interface  has been annotated with @Factory
            if (annotatedElement.getKind() != ElementKind.INTERFACE) {
                error(annotatedElement, "Only interfaces can be annotated with @%s", AutoJackson.class.getSimpleName());
                continue;
            }

            // We can cast it, because we know that it of ElementKind.INTERFACE
            TypeElement typeElement = (TypeElement) annotatedElement;

            // grab our annotation to get at the details
            AutoJackson annotation = typeElement.getAnnotation(AutoJackson.class);

            // handle concrete types, or classes which have multiple types
            String annotationTypeName = loadClassName(annotation);
            if (annotationTypeName.equals(NoTypeEnum.class.getCanonicalName())) {
                generateDefaultImplementation(typeElement);
            } else {
                System.out.println("Non concrete for " + typeElement.getSimpleName());
            }
        }

        return false;
    }

    private void generateDefaultImplementation(TypeElement typeElement) {
        Name className = typeElement.getSimpleName();

        // create the class that implements our interface
        Builder autoJacksonDefaultBuilder = TypeSpec
                .classBuilder(String.format("%s_AutoJacksonDefault", className))
                .addSuperinterface(ClassName.get(typeElement))
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        List<? extends TypeMirror> interfaces = typeElement.getInterfaces();
        System.out.println();
        System.out.println(typeElement);
        Stream<? extends Element> interfaceElements = interfaces.stream().flatMap(interfaceTypeMirror -> {
            System.out.println(interfaceTypeMirror);
            Element element = this.typeUtils.asElement(interfaceTypeMirror);
            return element.getEnclosedElements().stream();
        });

        // setup the member variables
        List<Variable> memberVariables = Stream.concat(interfaceElements, typeElement.getEnclosedElements().stream())
                .map(enclosedElement -> {
                    // check that the element is a method of the interface
                    if (enclosedElement.getKind() == ElementKind.METHOD) {
                        return Optional.of(implementMethodUsingMemberVariable((ExecutableElement) enclosedElement, autoJacksonDefaultBuilder));
                    } else {
                        return Optional.<Variable>empty();
                    }
                })
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());

        // create the constructor that assigns those member variables
        autoJacksonDefaultBuilder.addMethod(createConstructor(memberVariables, autoJacksonDefaultBuilder));

        // create the javaFile object
        PackageElement packageElement = this.elementUtils.getPackageOf(typeElement);
        JavaFile javaFile = JavaFile
                .builder(packageElement.getQualifiedName().toString(), autoJacksonDefaultBuilder.build())
                .build();

        // and write that file to disc
        try {
            javaFile.writeTo(this.filer);
        } catch (IOException e) {
            error(typeElement, e.getMessage());
        }
    }

    private MethodSpec createConstructor(List<Variable> memberVariables, Builder autoJacksonDefaultBuilder) {
        MethodSpec.Builder constructorBuilder = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PUBLIC);

        memberVariables.forEach(memberVariable -> {
            TypeName typeName = memberVariable.getTypeName();
            String variableName = memberVariable.getVariableName();
            constructorBuilder.addParameter(typeName, variableName);
            constructorBuilder.addStatement("this.$N = $N", variableName, variableName);
        });

        return constructorBuilder.build();
    }

    private Variable implementMethodUsingMemberVariable(ExecutableElement method, Builder autoJacksonDefaultBuilder) {
        // load the return type to set the type of the member variable
        TypeName returnTypeName = ClassName.get(method.getReturnType());

        // load the name of the member variable
        final String memberVariableName;
        Named namedAnnotation = method.getAnnotation(Named.class);
        if (namedAnnotation != null) {
            memberVariableName = namedAnnotation.value(); // TODO validate `namedAnnotation.value()` is a valid member variable name
        } else {
            String methodName = method.getSimpleName().toString();
            memberVariableName = methodName.substring("get".length(), methodName.length()).toLowerCase(); // TODO validate `method.getSimpleName()` is in the format `getXXX()`
        }

        // create the accessor method
        MethodSpec implementedMethod = MethodSpec.methodBuilder(method.getSimpleName().toString())
                .addModifiers(Modifier.PUBLIC)
                .returns(returnTypeName)
                .addStatement("return this.$N", memberVariableName)
                .build();

        // add the field and method which will use the new member variable
        autoJacksonDefaultBuilder
                .addField(returnTypeName, memberVariableName, Modifier.PRIVATE, Modifier.FINAL)
                .addMethod(implementedMethod);

        return new Variable(returnTypeName, memberVariableName);
    }

    private String loadClassName(AutoJackson annotation) {
        try {
            Class<?> clazz = annotation.type().value();
            return clazz.getCanonicalName();
        } catch (MirroredTypeException mte) {
            DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
            TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
            return classTypeElement.getQualifiedName().toString();
        }
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }

    private class Variable {
        private final TypeName typeName;
        private final String variableName;

        private Variable(TypeName typeName, String variableName) {
            this.typeName = typeName;
            this.variableName = variableName;
        }

        private String getVariableName() {
            return variableName;
        }

        private TypeName getTypeName() {
            return typeName;
        }
    }
}
