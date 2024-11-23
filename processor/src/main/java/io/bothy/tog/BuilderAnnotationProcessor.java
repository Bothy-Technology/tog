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
package io.bothy.tog;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
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

@SupportedAnnotationTypes("io.bothy.tog.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class BuilderAnnotationProcessor extends AbstractProcessor {

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
                    messager.printError(
                            "@io.bothy.tog.Builder is only applicable to records and constructors.", annotatedElement);
                    continue;
                }

                final var targetPackage = elements.getPackageOf(annotatedElement);
                final var targetPackageName = targetPackage.getQualifiedName().toString();

                final var builderCallChain = builderTarget.fields().stream()
                        .map(BuilderField::fieldName)
                        .collect(Collectors.joining(" -> "));

                final var constructorArgs = builderTarget.fields().stream()
                        .map(BuilderField::fieldName)
                        .collect(Collectors.joining(", "));

                final var builderClassName = getBuilderClassName(annotatedElement, targetPackageName);

                final var buildInterfaceClassName = builderClassName.nestedClass("Build");
                final var buildInterfaceSpec = TypeSpec.interfaceBuilder(buildInterfaceClassName)
                        .addModifiers(PUBLIC, STATIC)
                        .addMethod(MethodSpec.methodBuilder("build")
                                .addModifiers(PUBLIC, ABSTRACT)
                                .returns(
                                        TypeName.get(builderTarget.targetClass().asType()))
                                .build())
                        .build();

                var returnType = buildInterfaceClassName;

                final var interfaces = new ArrayList<TypeSpec>();
                final var builderFields = builderTarget.fields();
                for (final var field :
                        builderFields.subList(1, builderFields.size()).reversed()) {

                    final var upperCamelCaseFieldName =
                            LOWER_CAMEL_TO_UPPER_CAMEL.convert(field.fieldName().toString());
                    final var withInterfaceName = builderClassName.nestedClass("With" + upperCamelCaseFieldName);
                    final var withInterface = TypeSpec.interfaceBuilder(withInterfaceName)
                            .addModifiers(PUBLIC, STATIC)
                            .addMethod(MethodSpec.methodBuilder("with" + upperCamelCaseFieldName)
                                    .addModifiers(PUBLIC, ABSTRACT)
                                    .addParameter(
                                            TypeName.get(field.fieldType()),
                                            field.fieldName().toString())
                                    .returns(returnType)
                                    .build())
                            .build();
                    returnType = withInterfaceName;
                    interfaces.add(withInterface);
                }

                final var first = builderFields.getFirst();
                final var upperCamelCaseFieldName =
                        LOWER_CAMEL_TO_UPPER_CAMEL.convert(first.fieldName().toString());
                final var firstMethod = MethodSpec.methodBuilder("with" + upperCamelCaseFieldName)
                        .addModifiers(PUBLIC, ABSTRACT)
                        .addParameter(
                                TypeName.get(first.fieldType()),
                                first.fieldName().toString())
                        .returns(returnType)
                        .build();

                final var builderFactoryMethod = MethodSpec.methodBuilder("builder")
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

                final var builderClassSpec = TypeSpec.interfaceBuilder(builderClassName)
                        .addModifiers(PUBLIC)
                        .addAnnotation(GENERATED_ANNOTATION)
                        .addMethod(builderFactoryMethod)
                        .addMethod(firstMethod)
                        .addTypes(interfaces.reversed())
                        .addType(buildInterfaceSpec)
                        .build();

                final var builderJavaFile =
                        JavaFile.builder(targetPackageName, builderClassSpec).build();

                try {
                    builderJavaFile.writeTo(processingEnv.getFiler());
                } catch (final IOException e) {
                    messager.printError("IO Error writing file: " + e.getMessage());
                }
            }
        }

        return false;
    }

    private static ClassName getBuilderClassName(final Element annotatedElement, final String targetPackageName) {
        final var typeHierarchy = Stream.iterate(annotatedElement, Objects::nonNull, Element::getEnclosingElement)
                .filter(elt -> elt.getKind().isClass() || elt.getKind().isInterface())
                .toList();

        final var builderClassName =
                typeHierarchy.reversed().stream().map(Element::getSimpleName).collect(Collectors.joining()) + "Builder";

        return ClassName.get(targetPackageName, builderClassName);
    }
}
