package peckb1.examples.model;

import peckb1.processor.AutoJackson;
import peckb1.processor.Named;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@AutoJackson
public interface TopLevel {

    // ----------- primitives -----------
    @Named("mp01") String getMandatoryPrimitive01();
    @Named("mp02") long getMandatoryPrimitive02();
    @Named("op01") Optional<Integer> getOptionalPrimitive01();
    @Named("op02") Optional<Boolean> getOptionalPrimitive02();

    // ----------- enumerations -----------
    @Named("fe") FirstEnum getFirstEnum();
    @Named("se") Optional<SecondEnum> getSecondEnum();

    // ----------- timestamps -----------
    @Named("ft") Instant getFirstTime();
    @Named("st") Date getSecondTime();

    // ----------- model classes -----------
    @Named("mc") RecursiveConcrete getConcrete01();
    @Named("mv") RecursiveVariable getVariable01();
    @Named("oc") Optional<RecursiveConcrete> getConcrete02();
    @Named("ov") Optional<RecursiveVariable> getVariable02();

    // ----------- lists -----------
    @Named("c_list") List<RecursiveConcrete> getConcreteList();
    @Named("v_list") Optional<List<RecursiveVariable>> getVariableList();
    @Named("e_list") List<SecondEnum> getEnumList();
    @Named("p_list") Optional<List<String>> getStringList();

    // ----------- arrays -----------
    @Named("c_array") Optional<RecursiveConcrete[]> getConcreteArray();
    @Named("v_array") RecursiveVariable[] getVariableArray();
    @Named("e_array") Optional<SecondEnum[]> getEnumArray();
    @Named("p_array") String[] getStringArray();

}
