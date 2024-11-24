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
 * You should have received a copy of the GNU General Public License along with Foobar. If not, see
 * <https://www.gnu.org/licenses/>.
 */
package io.bothy.tog.examples.records.secondaryconstructor;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class RegionTest {

    @Test
    void canBuildRegion() {
        final var france = Region.builder().withName("France").build();

        assertThat(france).extracting("name").isEqualTo("France");
        assertThat(france).extracting("shortcode").isEqualTo("Fr");
    }
}