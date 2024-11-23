package test.example;

import java.lang.String;
import java.util.List;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.BuilderAnnotationProcessor")
public final class MenuBuilder {

    public static WithName builder() {
        return name -> entries -> () -> new test.example.Menu(name, entries);
    }

    public interface WithName {
        WithEntries withName(String name);
    }

    public interface WithEntries {
        Build withEntries(List<String> entries);
    }

    public interface Build {
        Menu build();
    }
}
