package test.example;

import io.bothy.tog.annotations.Builder;
import test.example.PersonBuilder;
import java.util.List;

@Builder
public record Person(String name, int age, List<String> pets) {
    public static PersonBuilder builder() {
        return PersonBuilder.builder();
    }
}
