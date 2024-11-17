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
