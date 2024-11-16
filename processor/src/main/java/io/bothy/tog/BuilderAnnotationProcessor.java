package io.bothy.tog;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

@SupportedAnnotationTypes("io.bothy.tog.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class BuilderAnnotationProcessor extends AbstractProcessor {

    public static final Converter<String, String> LOWER_CAMEL_TO_UPPER_CAMEL = CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL);

    @Override
    public boolean process(
            final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv
    ) {

        final var elements = processingEnv.getElementUtils();
        final var types = processingEnv.getTypeUtils();

        for (final var annotation : annotations) {
            final var annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (final var annotatedElement : annotatedElements) {

                final var targetTypeMirror = annotatedElement.asType();
                final var targetClassName = annotatedElement.getSimpleName();

                final var buildInterfaceClassName = ClassName.bestGuess("Build");
                final var buildInterfaceSpec = TypeSpec.interfaceBuilder(buildInterfaceClassName)
                        .addModifiers(PUBLIC)
                        .addMethod(MethodSpec.methodBuilder("build")
                                .addModifiers(PUBLIC, ABSTRACT)
                                .returns(TypeName.get(targetTypeMirror))
                                .build())
                        .build();

                final var typeElement = (TypeElement) annotatedElement;
                final List<? extends RecordComponentElement> recordComponents = typeElement.getRecordComponents();

//                final var imports = new HashSet<String>();
//
//                for (final var recordComponentElement : recordComponents.reversed()) {
//
//                }


                final RecordComponentElement recordComponentElement = recordComponents.get(0);
                final var fieldType = recordComponentElement.asType();

                final var fieldName = recordComponentElement.getSimpleName();
                final var upperCamelCaseFieldName = LOWER_CAMEL_TO_UPPER_CAMEL.convert(fieldName.toString());

                final var withInterfaceName = ClassName.bestGuess("With" + upperCamelCaseFieldName);
                final var withInterface = TypeSpec.interfaceBuilder(withInterfaceName)
                        .addModifiers(PUBLIC)
                        .addMethod(MethodSpec.methodBuilder("with" + upperCamelCaseFieldName)
                                .addModifiers(PUBLIC, ABSTRACT)
                                .addParameter(TypeName.get(fieldType), fieldName.toString())
                                .returns(buildInterfaceClassName)
                                .build())
                        .build();

                final var builderFactoryMethod = MethodSpec.methodBuilder("builder")
                        .addModifiers(PUBLIC, STATIC)
                        .returns(withInterfaceName)
                        .addCode("""
                                return $1L -> () -> new $2L($1L);
                                """, fieldName, targetClassName)
                        .build();

                final var builderClassSpec = TypeSpec.classBuilder(targetClassName + "Builder")
                        .addModifiers(PUBLIC, FINAL)
                        .addMethod(builderFactoryMethod)
                        .addType(withInterface)
                        .addType(buildInterfaceSpec)
                        .build();

                final var targetPackage = elements.getPackageOf(annotatedElement);

                final var targetPackageName = targetPackage.getQualifiedName().toString();

                final var builderJavaFile = JavaFile.builder(targetPackageName, builderClassSpec)
                        .build();

                try {
                    builderJavaFile.writeTo(processingEnv.getFiler());
                } catch (final IOException e) {
                    processingEnv.getMessager().printError("IO Error writing file: " + e.getMessage());
                }
            }
        }

        return false;
    }
}
