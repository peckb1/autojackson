# AutoJackson
Annotations to avoid manually adding Jackson annotaions to your model classes
to facilitate JSON serialization and deserialization.


### Annotations
##### @AutoJackson
TODO
##### @AutoJacksonTypeClass
TODO
##### @Named
Annotation placed on methods of the interface to expliclty controll the
JSON key used for that item. If not provided a method in the form of
`getSuperItem()` is expected, and would default to `superItem`.

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
