package test.example;

import java.lang.String;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.BuilderAnnotationProcessor")
public interface MenuBuilder {

    static MenuBuilder builder() {
        return name -> entries -> () -> new test.example.Menu(name, entries);
    }

    WithEntries withName(String name);

    interface WithEntries {
        Build withEntries(List<String> entries);
    }

    interface Build {
        Menu build();
    }
}
