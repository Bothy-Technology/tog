package io.bothy.tog;

import com.google.auto.service.AutoService;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.TypeSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Set;

@SupportedAnnotationTypes("io.bothy.tog.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class BuilderAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(
            final Set<? extends TypeElement> annotations,
            final RoundEnvironment roundEnv
    ) {

        for (final var annotation : annotations) {
            final var annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (final var annotatedElement : annotatedElements) {
                final var builderAnnotation = annotatedElement.getAnnotation(Builder.class);
                final var targetClassName = annotatedElement.getSimpleName();
                final var builderTypeSpec = TypeSpec.classBuilder(targetClassName + "Builder")
                        .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                        .build();

                final var targetPackage = processingEnv.getElementUtils().getPackageOf(annotatedElement);

                final var targetPackageName = targetPackage.getQualifiedName().toString();

                final var builderJavaFile = JavaFile.builder(targetPackageName, builderTypeSpec)
                        .build();


                try {
                    builderJavaFile.writeTo(processingEnv.getFiler());
                } catch (final IOException e) {
                    throw new UncheckedIOException(e);
                }
            }
        }

        return false;
    }
}
