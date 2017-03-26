package peckb1.examples.auto.fraggles;

import peckb1.examples.auto.Fraggle;
import peckb1.processor.AutoJackson;
import peckb1.processor.Named;

@AutoJackson
public interface Gobo extends Fraggle {

    @Named("postcards") int getFetchedPostcards();

}
