package test.example;

import java.lang.String;
import javax.annotation.processing.Generated;

@Generated("io.bothy.tog.BuilderAnnotationProcessor")
public interface OrganisationDepartmentPersonBuilder {

    static OrganisationDepartmentPersonBuilder builder() {
        return name -> () -> new test.example.Organisation.Department.Person(name);
    }

    Build withName(String name);

    interface Build {
        Organisation.Department.Person build();
    }
}
