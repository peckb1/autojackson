package peckb1.examples.base;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Job {
    private static final String OCCUPATION_KEY = "occupation";
    private static final String DAYS_WORKED_KEY = "daysWorked";

    @JsonProperty(value = OCCUPATION_KEY, required = true)
    private final String occupation;

    @JsonProperty(value = DAYS_WORKED_KEY, required = true)
    private final int daysWorked;

    public Job(@JsonProperty(value = OCCUPATION_KEY, required = true) String occupation,
               @JsonProperty(value = DAYS_WORKED_KEY, required = true) int daysWorked) {
        this.occupation = occupation;
        this.daysWorked = daysWorked;
    }

    public String getOccupation() {
        return this.occupation;
    }

    public int getDaysWorked() {
        return this.daysWorked;
    }
}
