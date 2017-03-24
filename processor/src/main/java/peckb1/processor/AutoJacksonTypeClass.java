package peckb1.processor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * <p>
 * A marker annotation used to specify the method which will return
 * the concrete class meant to implement a particular interface with
 * a particular type
 * </p>
 * For Example
 * <pre>
 * {@code @AutoJackson(type = @AutoJackson.Type(FraggleName.class))
 *   public interface Fraggle {
 *
 *       FraggleName getName();
 *
 *       enum FraggleName {
 *           GOBO(Gobo.class);
 *
 *           private final Class<? extends Fraggle> fraggleClass;
 *
 *           FraggleName(Class<? extends Fraggle> fraggleClass) {
 *               this.fraggleClass = fraggleClass;
 *           }
 *
 *          @code @AutoJacksonTypeClass
 *           public Class<? extends Fraggle> getFraggleClass() {
 *               return fraggleClass;
 *           }
 *       }
 *  }
 * }
 * </pre>
 * Uses the annotation to denote that getFraggleClass() is the method which
 * returns the particular class implementing the interface for each enum type
 */
@Target(ElementType.METHOD)
public @interface AutoJacksonTypeClass { }
