package com.github.peckb1.examples.base.fraggles;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.peckb1.examples.base.Fraggle;
import com.github.peckb1.examples.base.Job;

import java.util.Optional;

public class Boober extends Fraggle {
    public Boober(@JsonProperty(value = HAIR_COLOUR_KEY, required = true) String hairColour,
                  @JsonProperty(value = HAT_KEY, required = true) Boolean wearsHats,
                  @JsonProperty(value = JOB_KEY) Optional<Job> job) {
        super(hairColour, wearsHats, job);
    }

    @Override
    public FraggleName getFraggleName() {
        return FraggleName.BOOBER;
    }
}
