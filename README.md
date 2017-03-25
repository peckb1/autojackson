# AutoJackson
Annotations to avoid manually adding Jackson annotaions to your model classes
to facilitate JSON serialization and deserialization.


### Annotations

##### `@AutoJackson`
Annotation placed on interfaces to automatically generate the needed
deserializers and classes needed to create concrete instances of
those interfaces. An `AutoJackson.Type` can be given to the annotation
when more than one class needs to implement a given interface.

##### `@AutoJacksonTypeClass`
When needing to have multiple concrete implementation of an interface,
the class passed into the `AutoJackson.Type` needs to have an accessor
annotated with this annotation. This is required to know which method 
will return the Class definition for the interface that matches the 
enumeration value for the class passed in to the `AutoJackson.Type`.

See the usage below for an example

##### `@Named`
Annotation placed on methods of the interface to expliclty controll the
JSON key used for that item. If not provided a method in the form of
`getSuperItem()` is expected, and would default to `superItem`.

### Setup

##### `AutoJacksonSetup.Java`
After all the classes have been annotated and compiled an additional
class is created to avoid the boiler plate of adding each of the 
Jackson deserializers to the `ObjectMapper` used for deserializtion.
```
ObjectMapper objectMapper = ...
AutoJacksonSetup.configureObjectMapper( objectMapper );
```
Using the configuration avoids having to add each custom deserializer
manually, such as:
```
ObjectMapper objectMapper = ...
SimpleModule deserialzationModule = new SimpleModule();
deserialzationModule.addDeserializer(Fraggle.class, new Fraggle_AutoJacksonDeserializer());
deserialzationModule.addDeserializer(Muppet.class, new Fraggle_AutoJacksonDeserializer());
deserialzationModule.addDeserializer(SillyCreature.class, new Fraggle_AutoJacksonDeserializer());
objectMapper.registerModule(deserialzationModule);
```


### Usage
Given the example JSON document
```json
{
  "fraggles" : [
    {
      "name" : "BOOBER",
      "hairColour" : "blue",
      "hat" : true,
      "job" : {
        "ocupation" : "Actor",
        "daysWorked" : 0
      }
    },
    {
      "name" : "WEMBLEY",
      "hairColour" : "yellow",
      "hat" : false
    }
  ]
}
```

A set of interfaces would need to be created and annotated
```java
@AutoJackson
public interface FraggleList {
    List<Fraggle> getFraggles();
}
```
```java
@AutoJackson
public interface Fraggle {
    FraggleName getName();
    String getHairColour();
    @Named("hat") Boolean wearsHats();
    Optional<Job> getJob();
    
    enum FraggleName {
        WEMBLEY(Wembley.class),
        BOOBER(Boober.class),

        private final Class<? extends Fraggle> fraggleClass;

        FraggleName(Class<? extends Fraggle> fraggleClass) {
            this.fraggleClass = fraggleClass;
        }

        @AutoJacksonTypeClass
        public Class<? extends Fraggle> getFraggleClass() {
            return fraggleClass;
        }
    }
}
```
```java
@AutoJackson
public interface Job {
    String getOccupation();
    int getDaysWorked();
}
```
```java
@AutoJackson
public interface Wembley { }
```
```java
@AutoJackson
public interface Boober { }
```

And then to start using it configure a Jackson `ObjectMapper` and start serializing/deserializing
```java
ObjectMapper.objectMapper = new ObjectMapper();
AutoJacksonSetup.configureObjectMapper(this.objectMapper);

String jsonData = ...
FraggleList fraggleList = objectMapper.readValue(jsonData, FraggleList.class);
```

#### Additional Examples
See the [packaged example models](/examples/src/main/java/peckb1/examples/model) for a more complex example.
