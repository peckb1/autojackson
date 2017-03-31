package com.github.peckb1.examples.auto;

import com.github.peckb1.processor.AutoJackson;

import java.util.Optional;

@AutoJackson
public interface Gorg {

    Integer getAge();
    String getName();
    Optional<Gorg> getChild();

}
