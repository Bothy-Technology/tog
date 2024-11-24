package test.example;

import io.bothy.tog.annotations.Builder;
import test.example.OrganisationDepartmentPersonBuilder;

public class Organisation {

    public static class Department {

        @Builder
        public record Person(String name) {
            public static OrganisationDepartmentPersonBuilder builder() {
                return OrganisationDepartmentPersonBuilder.builder();
            }
        }

    }

}
