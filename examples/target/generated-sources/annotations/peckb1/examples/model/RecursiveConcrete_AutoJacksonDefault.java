package peckb1.examples.model;

import java.lang.Integer;
import java.lang.String;
import java.util.Optional;

public final class RecursiveConcrete_AutoJacksonDefault implements RecursiveConcrete {
  private final Integer value;

  private final String name;

  private final Optional<RecursiveConcrete> child;

  public RecursiveConcrete_AutoJacksonDefault(Integer value, String name,
      Optional<RecursiveConcrete> child) {
    this.value = value;
    this.name = name;
    this.child = child;
  }

  public Integer getValue() {
    return this.value;
  }

  public String getName() {
    return this.name;
  }

  public Optional<RecursiveConcrete> getChild() {
    return this.child;
  }
}
