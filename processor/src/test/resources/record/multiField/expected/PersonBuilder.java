package test.example;

import java.lang.String;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.BuilderAnnotationProcessor")
public final class PersonBuilder {

    public static WithName builder() {
        return name -> age -> pets -> () -> new test.example.Person(name, age, pets);
    }

    public interface WithName {
        WithAge withName(String name);
    }

    public interface WithAge {
        WithPets withAge(int age);
    }

    public interface WithPets {
        Build withPets(List<String> pets);
    }

    public interface Build {
        Person build();
    }
}
