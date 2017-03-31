package com.github.peckb1.processor;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

/**
 * <p>
 * Our main annotation which if applied to an interface will create
 * a custom deserializer, and possibly the needed base implementation
 * class to serialize to, and deserialize from JSON data using
 * Jackson deserialization.
 * </p>
 * For Example:
 * <pre>
 * {@code @AutoJackson
 *   public interface Steak {
 *      @code @Named("done") Boolean isDone();
 *   }
 * }
 * </pre>
 * Would create a class similar to:
 * <pre>
 * {@code @JsonInclude(JsonInclude.Include.NON_EMPTY)
 *   public final class Steak_AutoJacksonImpl implements Steak {
 *
 *       private static final String DONE = "done";
 *
 *      @code @JsonProperty(value = DONE, required = true)
 *       private final Boolean done;
 *
 *       public Steak_AutoJacksonImpl(@JsonProperty(value = DONE, required = true) Boolean done) {
 *           this.age = done;
 *       }
 *
 *       public Boolean isDone() {
 *           return this.done;
 *       }
 *
 *   }
 * }
 * </pre>
 * And a deserializer which can be registered with an ObjectMapper that will use the
 * concrete class to create instances of the original annotated instance.
 */
@Target(TYPE)
public @interface AutoJackson {

    Type type() default @Type(value = NoTypeEnum.class);

    @interface Type {

        Class<? extends Enum> value();

    }
}
