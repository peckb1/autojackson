package peckb1.processor;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;

/**
 * <p>
 * Annotation denoting that a given method should not have the json value
 * auto populated by the name of the method, but should use this value instead
 * </p>
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
 * </p>
 */
@Target(METHOD)
public @interface Named {

    String value();

}
