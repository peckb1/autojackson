package peckb1.examples.base;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class FraggleList {
    private static final String FRAGGLES_KEY = "fraggles";
    
    @JsonProperty(value = FRAGGLES_KEY, required = true)
    private final List<Fraggle> fraggles;
    
    public FraggleList(@JsonProperty(value = FRAGGLES_KEY, required = true) List<Fraggle> fraggles) {
        this.fraggles = fraggles;
    }
    
    public List<Fraggle> getFraggles() {
        return this.fraggles;
    }
}