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
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.STATIC;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
import com.google.common.base.Converter;
import com.google.common.base.Joiner;
import com.palantir.javapoet.ClassName;
import com.palantir.javapoet.JavaFile;
import com.palantir.javapoet.MethodSpec;
import com.palantir.javapoet.TypeName;
import com.palantir.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.TypeElement;

@SupportedAnnotationTypes("io.bothy.tog.Builder")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class BuilderAnnotationProcessor extends AbstractProcessor {

    public static final Converter<String, String> LOWER_CAMEL_TO_UPPER_CAMEL =
            CaseFormat.LOWER_CAMEL.converterTo(CaseFormat.UPPER_CAMEL);

    @Override
    public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {

        final var elements = processingEnv.getElementUtils();

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

                final var targetPackage = elements.getPackageOf(annotatedElement);
                final var targetPackageName = targetPackage.getQualifiedName().toString();

                final var interfaces = new ArrayList<TypeSpec>();
                final var fieldNames = new ArrayList<String>();

                final var builderClassName = targetClassName + "Builder";

                var returnType = ClassName.get(targetPackageName, builderClassName, "Build");

                for (final var recordComponentElement : recordComponents.reversed()) {
                    final var fieldName = recordComponentElement.getSimpleName();
                    final var fieldType = recordComponentElement.asType();
                    final var upperCamelCaseFieldName = LOWER_CAMEL_TO_UPPER_CAMEL.convert(fieldName.toString());
                    final var withInterfaceName =
                            ClassName.get(targetPackageName, builderClassName, "With" + upperCamelCaseFieldName);
                    final var withInterface = TypeSpec.interfaceBuilder(withInterfaceName)
                            .addModifiers(PUBLIC)
                            .addMethod(MethodSpec.methodBuilder("with" + upperCamelCaseFieldName)
                                    .addModifiers(PUBLIC, ABSTRACT)
                                    .addParameter(TypeName.get(fieldType), fieldName.toString())
                                    .returns(returnType)
                                    .build())
                            .build();
                    returnType = withInterfaceName;
                    interfaces.add(withInterface);
                    fieldNames.addFirst(fieldName.toString());
                }

                final var builderCallChain = Joiner.on(" -> ").join(fieldNames);
                final var constructorArgs = Joiner.on(", ").join(fieldNames);

                final var builderFactoryMethod = MethodSpec.methodBuilder("builder")
                        .addModifiers(PUBLIC, STATIC)
                        .returns(returnType)
                        .addCode(
                                """
                                return $1L -> () -> new $2L($3L);
                                """,
                                builderCallChain,
                                targetClassName,
                                constructorArgs)
                        .build();

                final var builderClassSpec = TypeSpec.classBuilder(builderClassName)
                        .addModifiers(PUBLIC, FINAL)
                        .addMethod(builderFactoryMethod)
                        .addTypes(interfaces.reversed())
                        .addType(buildInterfaceSpec)
                        .build();

                final var builderJavaFile =
                        JavaFile.builder(targetPackageName, builderClassSpec).build();

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
