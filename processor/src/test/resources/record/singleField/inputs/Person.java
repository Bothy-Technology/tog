package test.example;

import io.bothy.tog.annotations.Builder;
import test.example.PersonBuilder;

@Builder
public record Person(String name) {
    public static PersonBuilder builder() {
        return PersonBuilder.builder();
    }
}
