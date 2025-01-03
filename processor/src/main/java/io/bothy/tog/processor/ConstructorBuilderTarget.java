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
package io.bothy.tog.processor;

import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;

final class ConstructorBuilderTarget implements BuilderTarget {

    private final List<BuilderField> builderFields;
    private final Element targetClass;

    ConstructorBuilderTarget(final ExecutableElement executableElement) {
        assert executableElement.getKind() == ElementKind.CONSTRUCTOR;

        builderFields = executableElement.getParameters().stream()
                .map(BuilderField::from)
                .toList();

        targetClass = executableElement.getEnclosingElement();
    }

    @Override
    public Element targetClass() {
        return targetClass;
    }

    @Override
    public List<BuilderField> fields() {
        return builderFields;
    }
}
