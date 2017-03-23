package peckb1.processor;

import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;

@Target(TYPE)
public @interface AutoJackson {

    Type type() default @Type(value = NoTypeEnum.class);

    @interface Type {

        Class<? extends Enum> value();

    }
}
