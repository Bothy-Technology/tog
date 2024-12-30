/*
 * Copyright © 2024 Neil Richard Green
 *
 * This file is part of Tog.
 *
 * Tog is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * Tog is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with Tog. If not, see
 * <https://www.gnu.org/licenses/>.
 */
package io.bothy.tog.examples.records.basic;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PersonTest {

    @Test
    void canBuildPerson() {
        final var person = Person.builder()
                .withName("John Doe")
                .withAge(62)
                .withAdult(true)
                .withAddress("Toulouse Street")
                .build();

        assertThat(person).extracting("name").isEqualTo("John Doe");
        assertThat(person).extracting("age").isEqualTo(62);
        assertThat(person).extracting("adult").isEqualTo(true);
        assertThat(person).extracting("address").isEqualTo("Toulouse Street");
    }
}
