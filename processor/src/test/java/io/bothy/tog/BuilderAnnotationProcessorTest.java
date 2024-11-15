package io.bothy.tog;

import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

class BuilderAnnotationProcessorTest {


    @Test
    void canCreateABuilderWithOneField() {
        final var recordSource = JavaFileObjects.forSourceString("test.example.Person", """
                package test.example;
                
                import io.bothy.tog.Builder;
                
                @Builder
                record Person(String name) {
                }
                """);

        final var compilation = javac()
                .withProcessors(new BuilderAnnotationProcessor())
                .compile(recordSource);

        assertThat(compilation).succeeded();
        assertThat(compilation).generatedSourceFile("test.example.PersonBuilder");
    }
}
