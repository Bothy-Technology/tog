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

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

class BuilderAnnotationProcessorTest {

    @Test
    void canCreateARecordBuilderWithOneField() {
        assertInputProducesExpectedOutput(
                "record/singleField/inputs/Person.java",
                "record/singleField/expected/PersonBuilder.java",
                "test.example.PersonBuilder");
    }

    @Test
    void canCreateARecordBuilderWithMultipleFields() {
        assertInputProducesExpectedOutput(
                "record/multiField/inputs/Person.java",
                "record/multiField/expected/PersonBuilder.java",
                "test.example.PersonBuilder");
    }

    @Test
    void canCreateARecordBuilderFromASecondaryRecordConstructor() {
        assertInputProducesExpectedOutput(
                "record/secondaryConstructor/inputs/Menu.java",
                "record/secondaryConstructor/expected/MenuBuilder.java",
                "test.example.MenuBuilder");
    }

    @Test
    void canCreateABuilderFromAClassConstructor() {
        assertInputProducesExpectedOutput(
                "class/constructor/inputs/Menu.java",
                "class/constructor/expected/MenuBuilder.java",
                "test.example.MenuBuilder");
    }

    @Test
    void canCreateARecordBuilderFromARecordNestedInAClass() {
        assertInputProducesExpectedOutput(
                "record/nested/inputs/Organisation.java",
                "record/nested/expected/OrganisationDepartmentPersonBuilder.java",
                "test.example.OrganisationDepartmentPersonBuilder");
    }

    @Test
    void compilationFailsForAnnotatedClass() {
        final var source = JavaFileObjects.forSourceString(
                "foo.bar.Invalid",
                """
                package foo.bar;

                import io.bothy.tog.annotations.Builder;

                @Builder
                class Invalid {

                  Invalid(String bob) {}
                }
                """);

        final var compilation =
                javac().withProcessors(new BuilderAnnotationProcessor()).compile(source);

        assertThat(compilation).failed();

        assertThat(compilation)
                .hadErrorContaining(
                        "@io.bothy.tog.annotations.Builder is only applicable to records and constructors.");
    }

    private static void assertInputProducesExpectedOutput(
            final String inputJavaResourceName,
            final String expectedJavaResourceName,
            final String expectedJavaQualifiedClassName) {
        final var source = JavaFileObjects.forResource(inputJavaResourceName);
        final var expectedOutputSource = JavaFileObjects.forResource(expectedJavaResourceName);

        final var compilation =
                javac().withProcessors(new BuilderAnnotationProcessor()).compile(source);

        assertThat(compilation).succeeded();

        assertThat(compilation)
                .generatedSourceFile(expectedJavaQualifiedClassName)
                .hasSourceEquivalentTo(expectedOutputSource);
    }
}
