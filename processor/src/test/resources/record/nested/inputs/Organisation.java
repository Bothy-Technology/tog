package test.example;

import io.bothy.tog.Builder;
import test.example.OrganisationDepartmentPersonBuilder.WithName;

public class Organisation {

    public static class Department {

        @Builder
        public record Person(String name) {
            public static WithName builder() {
                return OrganisationDepartmentPersonBuilder.builder();
            }
        }

    }

}
