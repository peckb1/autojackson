package peckb1.examples.auto.fraggles;

import peckb1.examples.auto.Fraggle;
import peckb1.processor.AutoJackson;
import peckb1.processor.Named;

@AutoJackson
public interface Boober extends Fraggle {

    @Named("superstitions") long getNumberOfSuperstitions();

}
