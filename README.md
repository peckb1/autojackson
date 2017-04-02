# AutoJackson
Annotations to avoid manually adding Jackson annotaions to your model classes
to facilitate JSON serialization and deserialization.

[![Build Status](https://travis-ci.org/peckb1/autojackson.svg?branch=master)](https://travis-ci.org/peckb1/autojackson)

## Annotations

#### `@AutoJackson`
Annotation placed on interfaces to automatically generate the needed
deserializers and classes needed to create concrete instances of
those interfaces. An `AutoJackson.Type` can be given to the annotation
when more than one class needs to implement a given interface.

#### `@AutoJacksonTypeClass`
When needing to have multiple concrete implementation of an interface,
the class passed into the `AutoJackson.Type` needs to have an accessor
annotated with this annotation. This is required to know which method 
will return the Class definition for the interface that matches the 
enumeration value for the class passed in to the `AutoJackson.Type`.

See the usage below for an example

#### `@Named`
Annotation placed on methods of the interface to expliclty controll the
JSON key used for that item. If not provided a method in the form of
`getSuperItem()` is expected, and would default to `superItem`.

## Usage after applying annotations

#### `AutoJacksonSetup.Java`
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
deserialzationModule.addDeserializer(Muppet.class, new Muppet_AutoJacksonDeserializer());
deserialzationModule.addDeserializer(SillyCreature.class, new SillyCreature_AutoJacksonDeserializer());
objectMapper.registerModule(deserialzationModule);
```


### Example
Given the example JSON document:
```json
{
  "fraggles" : [
    {
      "name" : "BOOBER",
      "hairColour" : "blue",
      "hat" : true,
      "job" : {
        "occupation" : "Actor",
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


AutoJackson annotations could be used rather than the Jackson annotations

---
###### AutoJackson `FraggleList.java`
```java
@AutoJackson
public interface FraggleList {
    List<Fraggle> getFraggles();
}
```
###### Base Jackson `FraggleList.java`
```java
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
```
---
###### AutoJackson `Fraggle.java`
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
###### Base Jackson `Fraggle.java`
```java
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
```
---
###### AutoJackson `Job.java`
```java
@AutoJackson
public interface Job {
    String getOccupation();
    int getDaysWorked();
}
```
###### Base Jackson `Job.java`
```java
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
```
---
###### AutoJackson `Wembley.java`
```java
@AutoJackson
public interface Wembley { }
```
###### Base Jackson `Wembley.java`
```java
public class Wembley extends Fraggle {
    public Wembley(@JsonProperty(value = HAIR_COLOUR_KEY, required = true) String hairColour,
                   @JsonProperty(value = HAT_KEY, required = true) Boolean wearsHats,
                   @JsonProperty(value = JOB_KEY) Optional<Job> job) {
        super(hairColour, wearsHats, job);
    }

    @Override
    public FraggleName getFraggleName() {
        return FraggleName.WEMBLEY;
    }
}
```
---
###### AutoJackson `Boober.java`
```java
@AutoJackson
public interface Boober { }
```
###### Base Jackson `Boober.java`
```java
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
```
---
### Additional Examples
See the [packaged example models](/examples/src/main/java/peckb1/examples/auto) for a more complex example.

---
### Getting Autojackson

The `autojackson-processor` package contains the annotations and processor code that must be included.

With Maven:

```xml
<dependency>
    <groupId>com.github.peckb1</groupId>
    <artifactId>autojackson-processor</artifactId>
    <version>1.0.0</version>
</dependency>
```

With Gradle:

```groovy
compile 'com.github.peckb1:autojackson-processor:1.0.0'
```

Snapshots of the development version are available in [Sonatype's `snapshots` repository](https://oss.sonatype.org/content/repositories/snapshots/).
