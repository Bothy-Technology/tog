package test.example;

import java.lang.String;

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
