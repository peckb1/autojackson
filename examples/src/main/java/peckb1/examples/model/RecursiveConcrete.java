package peckb1.examples.model;

import peckb1.processor.AutoJackson;

import java.util.Optional;

@AutoJackson
public interface RecursiveConcrete {

    Integer getValue();
    String getName();
    Optional<RecursiveConcrete> getChild();

}
