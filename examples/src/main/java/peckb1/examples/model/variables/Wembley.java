package peckb1.examples.model.variables;

import peckb1.examples.model.RecursiveVariable;
import peckb1.processor.AutoJackson;
import peckb1.processor.Named;

@AutoJackson
public interface Wembley extends RecursiveVariable {

    @Named("fires") Integer getNumberOfFiresPutOut();

}
