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
import javax.tools.JavaFileObject;
import org.junit.jupiter.api.Test;

class BuilderAnnotationProcessorTest {

    @Test
    void canCreateARecordBuilderWithOneField() {
        JavaFileObject source = JavaFileObjects.forResource("singleFieldRecord/inputs/Person.java");

        final var compilation =
                javac().withProcessors(new BuilderAnnotationProcessor()).compile(source);

        assertThat(compilation).succeeded();

        final var expectedSource = JavaFileObjects.forResource("singleFieldRecord/expected/PersonBuilder.java");
        assertThat(compilation)
                .generatedSourceFile("test.example.PersonBuilder")
                .hasSourceEquivalentTo(expectedSource);
    }

    @Test
    void canCreateARecordBuilderWithMultipleFields() {
        JavaFileObject source = JavaFileObjects.forResource("multiFieldRecord/inputs/Person.java");

        final var compilation =
                javac().withProcessors(new BuilderAnnotationProcessor()).compile(source);

        assertThat(compilation).succeeded();

        final var expectedSource = JavaFileObjects.forResource("multiFieldRecord/expected/PersonBuilder.java");
        assertThat(compilation)
                .generatedSourceFile("test.example.PersonBuilder")
                .hasSourceEquivalentTo(expectedSource);
    }
}
