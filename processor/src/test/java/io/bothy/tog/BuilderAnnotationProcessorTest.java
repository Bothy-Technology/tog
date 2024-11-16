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
