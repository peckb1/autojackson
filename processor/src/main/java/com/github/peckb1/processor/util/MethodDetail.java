package com.github.peckb1.processor.util;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.TypeMirror;
import java.util.Optional;

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

    public ExecutableElement getElement() {
        return element;
    }

    public Optional<TypeMirror> getDifferentReturnType() {
        return differentReturnType;
    }

    @Override
    public int compareTo(MethodDetail o) {
        return this.element.getSimpleName().toString().compareTo(o.element.getSimpleName().toString());
    }
}