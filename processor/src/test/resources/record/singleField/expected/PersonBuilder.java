package test.example;

import java.lang.String;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.BuilderAnnotationProcessor")
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
