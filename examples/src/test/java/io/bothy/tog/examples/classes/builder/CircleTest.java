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
package io.bothy.tog.examples.classes.builder;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CircleTest {

    @Test
    void testCircle() {
        final var circle = Circle.builder()
                .withRadius(10.0)
                .withCenter(Point.builder().withX(2.3).withY(4.5).build())
                .build();

        assertThat(circle).extracting(Circle::getRadius).isEqualTo(10.0);
        assertThat(circle).extracting(Circle::getCenter).extracting(Point::getX).isEqualTo(2.3);
        assertThat(circle).extracting(Circle::getCenter).extracting(Point::getY).isEqualTo(4.5);
    }
}
