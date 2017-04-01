package com.github.peckb1.processor;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * Annotation denoting that a given method should not have the json value
 * auto populated by the name of the method, but should use this value instead
 * <p>
 * For example:
 * <pre>
 * {@code @Named("startTime") long getStartTimeMillis()}
 * </pre>
 * would create a json key called
 * <pre>
 * {@code startTime}
 * </pre>
 * rather than the default
 * <pre>
 * {@code startTimeMillis}
 * </pre>
 */
@Target(METHOD)
public @interface Named {

    String value();

}
