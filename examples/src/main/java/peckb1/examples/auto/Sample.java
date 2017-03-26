package peckb1.examples.auto;

import peckb1.processor.AutoJackson;
import peckb1.processor.Named;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@AutoJackson
public interface Sample {

    // some primitives
    String getString();
    @Named("intPrimitive") int getInt();
    @Named("booleanPrimitive") boolean getBoolean();

    // an enumeration
    Muppet getMuppet();

    // some base classes
    Fraggle getFraggle();
    Gorg getGorg();

    // lists & arrays
    List<Muppet> getMuppetList();
    Muppet[] getMuppetArray();
    List<Fraggle> getFraggleList();
    Fraggle[] getFraggleArray();
    List<Gorg> getGorgList();
    Gorg[] getGorgArray();

    // timestamps
    @Named("start") Instant getStartTime();
    @Named("end") Date getEndTime();

    // Optionals
    Optional<String> getOptionalString();
    Optional<Fraggle> getOptionalFraggle();
    Optional<List<Fraggle>> getOptionalFraggleList();
    Optional<Gorg> getOptionalGorg();
    Optional<Gorg[]> getOptionalGorgArray();

}
