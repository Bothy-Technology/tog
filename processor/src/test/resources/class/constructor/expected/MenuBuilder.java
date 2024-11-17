package test.example;

import java.lang.String;
import java.util.List;

public final class MenuBuilder {

    public static WithName builder() {
        return name -> entries -> () -> new Menu(name, entries);
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
