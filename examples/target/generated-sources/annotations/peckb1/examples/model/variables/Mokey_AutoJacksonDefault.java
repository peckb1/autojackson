package peckb1.examples.model.variables;

import java.lang.Integer;
import java.lang.String;
import java.util.Optional;
import peckb1.examples.model.RecursiveVariable;

public final class Mokey_AutoJacksonDefault implements Mokey {
  private final RecursiveVariable.RecursiveVariableType type;

  private final Integer value;

  private final String name;

  private final Optional<RecursiveVariable> child;

  private final int radishes;

  public Mokey_AutoJacksonDefault(RecursiveVariable.RecursiveVariableType type, Integer value,
      String name, Optional<RecursiveVariable> child, int radishes) {
    this.type = type;
    this.value = value;
    this.name = name;
    this.child = child;
    this.radishes = radishes;
  }

  public RecursiveVariable.RecursiveVariableType getType() {
    return this.type;
  }

  public Integer getValue() {
    return this.value;
  }

  public String getName() {
    return this.name;
  }

  public Optional<RecursiveVariable> getChild() {
    return this.child;
  }

  public int getRadishesPicked() {
    return this.radishes;
  }
}
