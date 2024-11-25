/*
 * Copyright © 2024 Neil Richard Green
 *
 * This file is part of Tog.
 *
 * Tog is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Tog is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see
 * <https://www.gnu.org/licenses/>.
 */
package io.bothy.tog.processor;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.collect.Lists;
import com.palantir.javapoet.AnnotationSpec;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Generated;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

@SupportedAnnotationTypes("io.bothy.tog.annotations.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@AutoService(Processor.class)
public final class BuilderAnnotationProcessor extends AbstractProcessor {

    private static final Converter<String, String> LOWER_CAMEL_TO_UPPER_CAMEL =
            CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL);

    private static final AnnotationSpec GENERATED_ANNOTATION = AnnotationSpec.builder(Generated.class)
            .addMember("value", "$S", BuilderAnnotationProcessor.class.getName())
            .build();

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        final var elements = processingEnv.getElementUtils();
        final var messager = processingEnv.getMessager();

        for (final var annotation : annotations) {
            final var annotatedElements = roundEnv.getElementsAnnotatedWith(annotation);
            for (final var annotatedElement : annotatedElements) {

                final BuilderTarget builderTarget;

                if (annotatedElement instanceof ExecutableElement executableElement
                        && executableElement.getKind() == ElementKind.CONSTRUCTOR) {

                    builderTarget = new ConstructorBuilderTarget(executableElement);
                } else if (annotatedElement instanceof TypeElement typeElement
                        && typeElement.getKind() == ElementKind.RECORD) {

                    builderTarget = new RecordBuilderTarget(typeElement);
                } else {
                    messager.printMessage(
                            Diagnostic.Kind.ERROR,
                            "@io.bothy.tog.annotations.Builder is only applicable to records and constructors.",
                            annotatedElement);
                    continue;
                }

                final var targetPackage = elements.getPackageOf(annotatedElement);
                final var targetPackageName = targetPackage.getQualifiedName().toString();

                final var builderClassName = builderInterfaceName(annotatedElement, targetPackageName);

                final var buildInterfaceClassName = builderClassName.nestedClass("Build");
                final var buildInterfaceSpec = TypeSpec.interfaceBuilder(buildInterfaceClassName)
                        .addModifiers(PUBLIC, STATIC)
                        .addMethod(MethodSpec.methodBuilder("build")
                                .addModifiers(PUBLIC, ABSTRACT)
                                .returns(
                                        TypeName.get(builderTarget.targetClass().asType()))
                                .build())
                        .build();

                final var builderFields = builderTarget.fields();

                var nextStageTypeName = buildInterfaceClassName;
                final var interfaces = new ArrayList<TypeSpec>();
                for (final var field : Lists.reverse(builderFields.subList(1, builderFields.size()))) {

                    final var withInterfaceName = withStageInterfaceName(field, builderClassName);

                    final var withInterface = TypeSpec.interfaceBuilder(withInterfaceName)
                            .addModifiers(PUBLIC, STATIC)
                            .addMethod(withStageMethod(field, nextStageTypeName))
                            .build();
                    nextStageTypeName = withInterfaceName;
                    interfaces.add(withInterface);
                }
                final var firstStageMethod = withStageMethod(builderFields.get(0), nextStageTypeName);

                final var builderFactoryMethod = builderFactoryMethod(builderTarget, builderClassName);

                final var builderClassSpec = TypeSpec.interfaceBuilder(builderClassName)
                        .addModifiers(PUBLIC)
                        .addAnnotation(GENERATED_ANNOTATION)
                        .addMethod(builderFactoryMethod)
                        .addMethod(firstStageMethod)
                        .addTypes(Lists.reverse(interfaces))
                        .addType(buildInterfaceSpec)
                        .build();

                final var builderJavaFile =
                        JavaFile.builder(targetPackageName, builderClassSpec).build();

                try {
                    builderJavaFile.writeTo(processingEnv.getFiler());
                } catch (final IOException e) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "IO Error writing file: " + e.getMessage());
                }
            }
        }

        return false;
    }

    private static ClassName withStageInterfaceName(final BuilderField field, final ClassName builderClassName) {
        final var upperCamelCaseFieldName =
                LOWER_CAMEL_TO_UPPER_CAMEL.convert(field.fieldName().toString());
        return builderClassName.nestedClass("With" + upperCamelCaseFieldName);
    }

    private static MethodSpec builderFactoryMethod(
            final BuilderTarget builderTarget, final ClassName builderClassName) {
        final var builderCallChain =
                builderTarget.fields().stream().map(BuilderField::fieldName).collect(Collectors.joining(" -> "));
        final var constructorArgs =
                builderTarget.fields().stream().map(BuilderField::fieldName).collect(Collectors.joining(", "));
        return MethodSpec.methodBuilder("builder")
                .addModifiers(PUBLIC, STATIC)
                .returns(builderClassName)
                .addCode(
                        """
                                return $1L -> () -> new $2L($3L);
                                """,
                        builderCallChain,
                        builderTarget.targetClass(),
                        constructorArgs)
                .build();
    }

    private static ClassName builderInterfaceName(final Element annotatedElement, final String targetPackageName) {
        final var typeHierarchy = Stream.iterate(annotatedElement, Objects::nonNull, Element::getEnclosingElement)
                .filter(elt -> elt.getKind().isClass() || elt.getKind().isInterface())
                .toList();

        final var builderInterfaceName =
                Lists.reverse(typeHierarchy).stream()
                                .map(Element::getSimpleName)
                                .collect(Collectors.joining()) + "Builder";

        return ClassName.get(targetPackageName, builderInterfaceName);
    }

    private static MethodSpec withStageMethod(final BuilderField current, final TypeName nextStageType) {
        final var fieldName = current.fieldName().toString();
        final var methodName = "with%s".formatted(LOWER_CAMEL_TO_UPPER_CAMEL.convert(fieldName));
        final var parameterTypeName = TypeName.get(current.fieldType());
        return MethodSpec.methodBuilder(methodName)
                .addModifiers(PUBLIC, ABSTRACT)
                .addParameter(parameterTypeName, fieldName)
                .returns(nextStageType)
                .build();
    }
}
