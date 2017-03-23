package peckb1.examples.model;

import java.lang.Boolean;
import java.lang.Integer;
import java.lang.String;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public final class TopLevel_AutoJacksonDefault implements TopLevel {
  private final String mp01;

  private final long mp02;

  private final Optional<Integer> op01;

  private final Optional<Boolean> op03;

  private final FirstEnum fe;

  private final Optional<SecondEnum> se;

  private final Instant ft;

  private final Date st;

  private final RecursiveConcrete mc;

  private final RecursiveVariable mv;

  private final Optional<RecursiveConcrete> oc;

  private final Optional<RecursiveVariable> ov;

  private final List<RecursiveConcrete> c_list;

  private final List<RecursiveVariable> v_list;

  private final List<SecondEnum> e_list;

  private final List<String> p_list;

  private final RecursiveConcrete[] c_array;

  private final RecursiveVariable[] v_array;

  private final SecondEnum[] e_array;

  private final String[] p_array;

  public TopLevel_AutoJacksonDefault(String mp01, long mp02, Optional<Integer> op01,
      Optional<Boolean> op03, FirstEnum fe, Optional<SecondEnum> se, Instant ft, Date st,
      RecursiveConcrete mc, RecursiveVariable mv, Optional<RecursiveConcrete> oc,
      Optional<RecursiveVariable> ov, List<RecursiveConcrete> c_list,
      List<RecursiveVariable> v_list, List<SecondEnum> e_list, List<String> p_list,
      RecursiveConcrete[] c_array, RecursiveVariable[] v_array, SecondEnum[] e_array,
      String[] p_array) {
    this.mp01 = mp01;
    this.mp02 = mp02;
    this.op01 = op01;
    this.op03 = op03;
    this.fe = fe;
    this.se = se;
    this.ft = ft;
    this.st = st;
    this.mc = mc;
    this.mv = mv;
    this.oc = oc;
    this.ov = ov;
    this.c_list = c_list;
    this.v_list = v_list;
    this.e_list = e_list;
    this.p_list = p_list;
    this.c_array = c_array;
    this.v_array = v_array;
    this.e_array = e_array;
    this.p_array = p_array;
  }

  public String getMandatoryPrimitive01() {
    return this.mp01;
  }

  public long getMandatoryPrimitive02() {
    return this.mp02;
  }

  public Optional<Integer> getOptionalPrimitive01() {
    return this.op01;
  }

  public Optional<Boolean> getOptionalPrimitive02() {
    return this.op03;
  }

  public FirstEnum getFirstEnum() {
    return this.fe;
  }

  public Optional<SecondEnum> getSecondEnum() {
    return this.se;
  }

  public Instant getFirstTime() {
    return this.ft;
  }

  public Date getSecondTime() {
    return this.st;
  }

  public RecursiveConcrete getConcrete01() {
    return this.mc;
  }

  public RecursiveVariable getVariable01() {
    return this.mv;
  }

  public Optional<RecursiveConcrete> getConcrete02() {
    return this.oc;
  }

  public Optional<RecursiveVariable> getVariable02() {
    return this.ov;
  }

  public List<RecursiveConcrete> getConcreteList() {
    return this.c_list;
  }

  public List<RecursiveVariable> getVariableList() {
    return this.v_list;
  }

  public List<SecondEnum> getEnumList() {
    return this.e_list;
  }

  public List<String> getStringList() {
    return this.p_list;
  }

  public RecursiveConcrete[] getConcreteArray() {
    return this.c_array;
  }

  public RecursiveVariable[] getVariableArray() {
    return this.v_array;
  }

  public SecondEnum[] getEnumArray() {
    return this.e_array;
  }

  public String[] getStringArray() {
    return this.p_array;
  }
}
