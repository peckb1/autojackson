package peckb1.examples;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import peckb1.examples.model.TopLevel;
import peckb1.examples.model.TopLevel_AutoJacksonDeserializer;

import java.io.File;
import java.io.IOException;

public class Driver {

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        SimpleModule deserialzationModule = new SimpleModule();
        deserialzationModule.addDeserializer(TopLevel.class, new TopLevel_AutoJacksonDeserializer());
        objectMapper.registerModule(deserialzationModule);

        File topLevel01File = new File("examples/resources/top_level_01.json");
        File topLevel02File = new File("examples/resources/top_level_02.json");
        File topLevel03File = new File("examples/resources/top_level_03.json");

        TopLevel topLevel01 = objectMapper.readValue(topLevel01File, TopLevel.class);
        TopLevel topLevel02 = objectMapper.readValue(topLevel02File, TopLevel.class);
        TopLevel topLevel03 = objectMapper.readValue(topLevel03File, TopLevel.class);

        System.out.println(objectMapper.writeValueAsString(topLevel01));
        System.out.println(objectMapper.writeValueAsString(topLevel02));
        System.out.println(objectMapper.writeValueAsString(topLevel03));
    }

}
