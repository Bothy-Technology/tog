package test.example;

import io.bothy.tog.Builder;
import test.example.MenuBuilder.WithName;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private final String name;
    private final List<String> entries;

    @Builder
    public Menu(final String name, final List<String> entries) {
        this.name = name;
        this.entries = entries;
    }

    public static WithName builder() {
        return MenuBuilder.builder();
    }
}
