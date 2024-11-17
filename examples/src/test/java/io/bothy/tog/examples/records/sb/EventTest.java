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
package io.bothy.tog.examples.records.sb;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class EventTest {

    @Test
    void canCreateEventWithNestedValues() {
        final var event = Event.builder()
                .withName("Red vs Blue")
                .withMarkets(List.of(Market.builder()
                        .withName("H2H")
                        .withSelections(List.of(
                                Selection.builder().withName("Red").build(),
                                Selection.builder().withName("Blue").build()))
                        .build()))
                .build();

        assertThat(event)
                .isEqualTo(new Event(
                        "Red vs Blue",
                        List.of(new Market("H2H", List.of(new Selection("Red"), new Selection("Blue"))))));
    }
}
