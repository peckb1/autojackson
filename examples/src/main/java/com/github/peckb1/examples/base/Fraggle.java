package com.github.peckb1.examples.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.github.peckb1.examples.base.fraggles.Wembley;
import com.github.peckb1.examples.base.fraggles.Boober;

import java.util.Optional;

@JsonTypeInfo(use = Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "name") // not compile time checked
@JsonSubTypes({
        @Type(value = Wembley.class, name = "WEMBLEY"), // not compile time checked
        @Type(value = Boober.class, name = "BOOBER")    // not compile time checked
})
public abstract class Fraggle {
    protected static final String HAIR_COLOUR_KEY = "hairColour";
    protected static final String HAT_KEY = "hat";
    protected static final String JOB_KEY = "job";

    @JsonProperty(value = HAIR_COLOUR_KEY, required = true)
    private final String hairColour;

    @JsonProperty(value = HAT_KEY, required = true)
    private final Boolean wearsHats;

    @JsonProperty(value = JOB_KEY)
    private final Optional<Job> job;

    protected Fraggle(String hairColour, Boolean wearsHats, Optional<Job> job) {
        this.hairColour = hairColour;
        this.wearsHats = wearsHats;
        this.job = job;
    }

    @JsonIgnore
    public abstract FraggleName getFraggleName();

    public String getHairColour() {
        return this.hairColour;
    }

    public Boolean wearsHats() {
        return this.wearsHats;
    }

    public Optional<Job> getJob() {
        return this.job;
    }

    public enum FraggleName {
        WEMBLEY, BOOBER
    }
}
