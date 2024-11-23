package test.example;

import io.bothy.tog.Builder;
import test.example.MenuBuilder;

import java.util.ArrayList;
import java.util.List;

public record Menu(String name, List<String> entries) {
    @Builder
    public Menu(String name) {
        this(name, List.of());
    }

    public static MenuBuilder builder() {
        return MenuBuilder.builder();
    }
}
