package peckb1.examples.model;

import peckb1.processor.AutoJackson;

import java.util.Optional;

@AutoJackson
public interface Gorg {

    Integer getAge();
    String getName();
    Optional<Gorg> getChild();

}
