package test.example;

import java.lang.String;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.processor.BuilderAnnotationProcessor")
public interface PersonBuilder {

    static PersonBuilder builder() {
        return name -> age -> pets -> () -> new test.example.Person(name, age, pets);
    }

    WithAge withName(String name);

    interface WithAge {
        WithPets withAge(int age);
    }

    interface WithPets {
        Build withPets(List<String> pets);
    }

    interface Build {
        Person build();
    }
}
