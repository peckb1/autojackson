package com.github.peckb1.processor;

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
 * <code>{@literal @}AutoJackson(type = {@literal @}AutoJackson.Type(FraggleName.class))
 *  public interface Fraggle {
 *
 *      FraggleName getName();
 *
 *      enum FraggleName {
 *          GOBO(Gobo.class);
 *
 *          private final Class&lt;? extends Fraggle&gt; fraggleClass;
 *
 *          FraggleName(Class&lt;? extends Fraggle&gt; fraggleClass) {
 *              this.fraggleClass = fraggleClass;
 *          }
 *
 *         {@literal @}AutoJacksonTypeClass
 *          public Class&lt;? extends Fraggle&gt; getFraggleClass() {
 *              return fraggleClass;
 *          }
 *      }
 *  }
 * </code>
 * </pre>
 * Uses the annotation to denote that getFraggleClass() is the method which
 * returns the particular class implementing the interface for each enum type
 */
@Target(ElementType.METHOD)
public @interface AutoJacksonTypeClass { }
