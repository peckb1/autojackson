package peckb1.examples.model.fraggles;

import peckb1.examples.model.Fraggle;
import peckb1.processor.AutoJackson;
import peckb1.processor.Named;

@AutoJackson
public interface Boober extends Fraggle {

    @Named("superstitions") long getNumberOfSuperstitions();

}
