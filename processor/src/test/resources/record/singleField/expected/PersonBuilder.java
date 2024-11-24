package test.example;

import java.lang.String;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.processor.BuilderAnnotationProcessor")
public interface PersonBuilder {

    static PersonBuilder builder() {
        return name -> () -> new test.example.Person(name);
    }

    Build withName(String name);

    interface Build {
        Person build();
    }
}
