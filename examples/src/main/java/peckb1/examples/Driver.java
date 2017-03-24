package peckb1.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import peckb1.autojackson.AutoJacksonSetup;
import peckb1.examples.model.TopLevel;

import java.io.File;
import java.io.IOException;

public class Driver {

    // TODO make tests out of this for more than just visual validation
    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        AutoJacksonSetup.configureObjectMapper(objectMapper);

        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.registerModule(new Jdk8Module());

        File topLevel01File = new File("examples/resources/simple_model.json");
        File topLevel02File = new File("examples/resources/top_level_02.json");
        File topLevel03File = new File("examples/resources/top_level_03.json");

        TopLevel topLevel01 = objectMapper.readValue(topLevel01File, TopLevel.class);
        TopLevel topLevel02 = objectMapper.readValue(topLevel02File, TopLevel.class);
        TopLevel topLevel03 = objectMapper.readValue(topLevel03File, TopLevel.class);

        String topLevel01String = objectMapper.writeValueAsString(topLevel01);
        String topLevel02String = objectMapper.writeValueAsString(topLevel02);
        String topLevel03String = objectMapper.writeValueAsString(topLevel03);

        TopLevel topLevel01Again = objectMapper.readValue(topLevel01String, TopLevel.class);
        TopLevel topLevel02Again = objectMapper.readValue(topLevel02String, TopLevel.class);
        TopLevel topLevel03Again = objectMapper.readValue(topLevel03String, TopLevel.class);

        String topLevel01StringAgain = objectMapper.writeValueAsString(topLevel01Again);
        String topLevel02StringAgain = objectMapper.writeValueAsString(topLevel02Again);
        String topLevel03StringAgain = objectMapper.writeValueAsString(topLevel03Again);

        System.out.println(topLevel01);
        System.out.println(topLevel01String);
        System.out.println(topLevel01Again);
        System.out.println(topLevel01StringAgain);

        System.out.println(topLevel02);
        System.out.println(topLevel02String);
        System.out.println(topLevel02Again);
        System.out.println(topLevel02StringAgain);

        System.out.println(topLevel03);
        System.out.println(topLevel03String);
        System.out.println(topLevel03Again);
        System.out.println(topLevel03StringAgain);
    }

}
