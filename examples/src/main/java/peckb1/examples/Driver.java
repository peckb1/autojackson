package peckb1.examples;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import peckb1.examples.model.RecursiveConcrete;
import peckb1.examples.model.RecursiveConcrete_AutoJacksonDeserializer;
import peckb1.examples.model.RecursiveVariable;
import peckb1.examples.model.RecursiveVariable_AutoJacksonDeserializer;
import peckb1.examples.model.TopLevel;
import peckb1.examples.model.TopLevel_AutoJacksonDeserializer;
import peckb1.examples.model.variables.Boober;
import peckb1.examples.model.variables.Boober_AutoJacksonDeserializer;
import peckb1.examples.model.variables.Gobo;
import peckb1.examples.model.variables.Gobo_AutoJacksonDeserializer;
import peckb1.examples.model.variables.Mokey;
import peckb1.examples.model.variables.Mokey_AutoJacksonDeserializer;
import peckb1.examples.model.variables.Red;
import peckb1.examples.model.variables.Red_AutoJacksonDeserializer;
import peckb1.examples.model.variables.Wembley;
import peckb1.examples.model.variables.Wembley_AutoJacksonDeserializer;

import java.io.File;
import java.io.IOException;

public class Driver {

    public static void main(String[] args) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        SimpleModule deserialzationModule = new SimpleModule();
        // TODO create single configuration which contains at least the visibility and deserializers
        deserialzationModule.addDeserializer(TopLevel.class, new TopLevel_AutoJacksonDeserializer());
        deserialzationModule.addDeserializer(RecursiveConcrete.class, new RecursiveConcrete_AutoJacksonDeserializer());
        deserialzationModule.addDeserializer(RecursiveVariable.class, new RecursiveVariable_AutoJacksonDeserializer());
        deserialzationModule.addDeserializer(Boober.class, new Boober_AutoJacksonDeserializer());
        deserialzationModule.addDeserializer(Gobo.class, new Gobo_AutoJacksonDeserializer());
        deserialzationModule.addDeserializer(Mokey.class, new Mokey_AutoJacksonDeserializer());
        deserialzationModule.addDeserializer(Red.class, new Red_AutoJacksonDeserializer());
        deserialzationModule.addDeserializer(Wembley.class, new Wembley_AutoJacksonDeserializer());
        objectMapper.registerModule(deserialzationModule);

        objectMapper.registerModule(new JavaTimeModule()); // maybe require user to specify
        objectMapper.registerModule(new Jdk8Module()); // maybe require the user to specify
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);

        File topLevel01File = new File("examples/resources/top_level_01.json");
        File topLevel02File = new File("examples/resources/top_level_02.json");
        File topLevel03File = new File("examples/resources/top_level_03.json");

        TopLevel topLevel01 = objectMapper.readValue(topLevel01File, TopLevel.class);
//        TopLevel topLevel02 = objectMapper.readValue(topLevel02File, TopLevel.class);
//        TopLevel topLevel03 = objectMapper.readValue(topLevel03File, TopLevel.class);

        String topLevel01String = objectMapper.writeValueAsString(topLevel01);
//        System.out.println(objectMapper.writeValueAsString(topLevel02));
//        System.out.println(objectMapper.writeValueAsString(topLevel03));

        TopLevel topLevel01Again = objectMapper.readValue(topLevel01File, TopLevel.class);

        String topLevel01StringAgain = objectMapper.writeValueAsString(topLevel01Again);

        System.out.println(topLevel01);
        System.out.println(topLevel01String);
        System.out.println(topLevel01Again);
        System.out.println(topLevel01StringAgain);
    }

}
