package test.example;

import java.lang.String;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.processor.BuilderAnnotationProcessor")
@io.bothy.tog.annotations.Generated(generator = "io.bothy.tog.processor.BuilderAnnotationProcessor")
public interface MenuBuilder {

    static MenuBuilder builder() {
        return name -> () -> new test.example.Menu(name);
    }

    Build withName(String name);

    interface Build {
        Menu build();
    }
}
