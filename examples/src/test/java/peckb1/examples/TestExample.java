package peckb1.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.Before;
import org.junit.Test;
import peckb1.autojackson.AutoJacksonSetup;
import peckb1.examples.model.TopLevel;

import java.io.File;
import java.io.IOException;

public class TestExample {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.registerModule(new Jdk8Module());
        AutoJacksonSetup.configureObjectMapper(this.objectMapper);
    }

    @Test
    public void testSimpleModel() throws IOException {
        File simpleModelFile = new File("examples/resources/simple_model.json");
        TopLevel topLevel = this.objectMapper.readValue(simpleModelFile, TopLevel.class);


    }

}
