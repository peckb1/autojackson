package com.github.peckb1.processor.util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

/**
 * A wrapper object which contains the standard {@link ExecutableElement} which defines
 * a method during annotation processing, as well as an optional return type should the
 * {@link TypeMirror} object inside the base method be a generic variable, rather than
 * a concrete class that can be instantiated.
 */
public class MethodDetail implements Comparable<MethodDetail> {

    private final ExecutableElement element;
    private final Optional<TypeMirror> differentReturnType;

    MethodDetail(ExecutableElement element) {
        this(element, Optional.empty());
    }

    MethodDetail(ExecutableElement element, Optional<TypeMirror> differentReturnType) {
        this.element = element;
        this.differentReturnType = differentReturnType;
    }

    ExecutableElement getElement() {
        return element;
    }

    Optional<TypeMirror> getDifferentReturnType() {
        return differentReturnType;
    }

    @Override
    public int compareTo(MethodDetail o) {
        return this.element.getSimpleName().toString().compareTo(o.element.getSimpleName().toString());
    }
}