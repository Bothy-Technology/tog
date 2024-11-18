package test.example;

import java.lang.String;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.BuilderAnnotationProcessor")
public final class MenuBuilder {

    public static WithName builder() {
        return name -> () -> new Menu(name);
    }

    public interface WithName {
        Build withName(String name);
    }

    public interface Build {
        Menu build();
    }
}
