# Tog
Simple, useful utilities.

## What's in a name?
tog - /tʰok/
1. lift, raise, rear, haul, pick up, hoist
2. build, erect

## Usage
### Configure your build system

[Maven](#apache-maven)

### Use it
Annotate a class with `@Builder`.
```java
import io.bothy.tog.annotations.Builder;

@Builder
public record Person(String name, int age) {
    public static PersonBulder builder() {
        return PersonBuilder.builder();
    }
}
```
Use the builder:
```java

class Example {
    static void doStuff() {
        final var bob = Person.builder()
                .withName("Bob")
                .withAge(37)
                .build();
    }
}
```

### Apache Maven
Add the annotations as a compile time dependency.
```xml
<dependencies>
    ...
    <dependency>
        <groupId>io.bothy.tog</groupId>
        <artifactId>annotations</artifactId>
        <version>0.2.0</version>
    </dependency>
    ...
</dependencies>
```

Add the annotation processor to the compiler config.
```xml
<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>io.bothy.tog</groupId>
                            <artifactId>processor</artifactId>
                            <version>0.2.0</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>
        </plugins>
    </pluginManagement>
</build>
```
