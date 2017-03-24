package peckb1.examples.model.fraggles;

import peckb1.examples.model.Fraggle;
import peckb1.processor.AutoJackson;
import peckb1.processor.Named;

@AutoJackson
public interface Mokey extends Fraggle {

    @Named("radishes") int getRadishesPicked();

}
