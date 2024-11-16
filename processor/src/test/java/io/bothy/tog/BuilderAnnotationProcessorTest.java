package io.bothy.tog;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

import com.google.testing.compile.JavaFileObjects;
import java.util.List;
import org.junit.jupiter.api.Test;

class BuilderAnnotationProcessorTest {

    @Test
    void canCreateARecordBuilderWithOneField() {
        final var recordSource = JavaFileObjects.forSourceString(
                "test.example.Person",
                """
                package test.example;

                import io.bothy.tog.Builder;

                @Builder
                record Person(String name) {
                  public static PersonBuilder builder() {
                    return new PersonBuilder();
                  }
                }
                """);

        final var compilation = javac().withProcessors(new BuilderAnnotationProcessor())
                .withOptions(List.of())
                .compile(recordSource);

        assertThat(compilation).succeeded();
        assertThat(compilation)
                .generatedSourceFile("test.example.PersonBuilder")
                .hasSourceEquivalentTo(
                        JavaFileObjects.forSourceString(
                                "test.example.Person",
                                """
                        package test.example;

                        import java.lang.String;

                        public final class PersonBuilder {

                          public static WithName builder() {
                            return name -> () -> new Person(name);
                          }

                          public interface WithName {
                            Build withName(String name);
                          }

                          public interface Build {
                            Person build();
                          }
                        }
                        """));
    }
}
