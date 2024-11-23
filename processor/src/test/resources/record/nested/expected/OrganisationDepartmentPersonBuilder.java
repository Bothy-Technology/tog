package test.example;

import java.lang.String;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.BuilderAnnotationProcessor")
public final class OrganisationDepartmentPersonBuilder {

    public static WithName builder() {
        return name -> () -> new test.example.Organisation.Department.Person(name);
    }

    public interface WithName {
        Build withName(String name);
    }

    public interface Build {
        Organisation.Department.Person build();
    }
}
