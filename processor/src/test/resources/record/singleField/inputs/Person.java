package test.example;

import io.bothy.tog.Builder;
import test.example.PersonBuilder.WithName;

@Builder
public record Person(String name) {
    public static WithName builder() {
        return PersonBuilder.builder();
    }
}
