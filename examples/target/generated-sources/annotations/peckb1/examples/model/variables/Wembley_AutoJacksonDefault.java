package peckb1.examples.model.variables;

import java.lang.Integer;
import java.lang.String;
import java.util.Optional;
import peckb1.examples.model.RecursiveVariable;

public final class Wembley_AutoJacksonDefault implements Wembley {
  private final RecursiveVariable.RecursiveVariableType type;

  private final Integer value;

  private final String name;

  private final Optional<RecursiveVariable> child;

  private final int fires;

  public Wembley_AutoJacksonDefault(RecursiveVariable.RecursiveVariableType type, Integer value,
      String name, Optional<RecursiveVariable> child, int fires) {
    this.type = type;
    this.value = value;
    this.name = name;
    this.child = child;
    this.fires = fires;
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

  public int getNumberOfFiresPutOut() {
    return this.fires;
  }
}
