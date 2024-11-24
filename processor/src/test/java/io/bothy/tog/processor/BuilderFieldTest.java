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
package io.bothy.tog.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import javax.lang.model.element.Name;
import javax.lang.model.element.RecordComponentElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.TypeMirror;
import org.junit.jupiter.api.Test;

class BuilderFieldTest {

    @Test
    void canCreateFromRecordComponentElement() {
        final var recordComponentElement = mock(RecordComponentElement.class);
        final var name = mock(Name.class);
        final var type = mock(TypeMirror.class);
        when(recordComponentElement.getSimpleName()).thenReturn(name);
        when(recordComponentElement.asType()).thenReturn(type);

        final var builderField = BuilderField.from(recordComponentElement);

        assertThat(builderField).extracting(BuilderField::fieldName).isSameAs(name);
        assertThat(builderField).extracting(BuilderField::fieldType).isSameAs(type);
    }

    @Test
    void canCreateFromVariableElement() {
        final var variableElement = mock(VariableElement.class);
        final var name = mock(Name.class);
        final var type = mock(TypeMirror.class);
        when(variableElement.getSimpleName()).thenReturn(name);
        when(variableElement.asType()).thenReturn(type);

        final var builderField = BuilderField.from(variableElement);

        assertThat(builderField).extracting(BuilderField::fieldName).isSameAs(name);
        assertThat(builderField).extracting(BuilderField::fieldType).isSameAs(type);
    }
}
