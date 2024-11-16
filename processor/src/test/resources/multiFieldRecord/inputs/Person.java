package test.example;

import io.bothy.tog.Builder;
import test.example.PersonBuilder.WithName;
import java.util.List;

@Builder
public record Person(String name, int age, List<String> pets) {
    public static WithName builder() {
        return PersonBuilder.builder();
    }
}
