package peckb1.examples.model;

import peckb1.examples.model.RecursiveVariable.RecursiveVariableType;
import peckb1.examples.model.variables.Boober;
import peckb1.examples.model.variables.Gobo;
import peckb1.examples.model.variables.Mokey;
import peckb1.examples.model.variables.Red;
import peckb1.examples.model.variables.Wembley;
import peckb1.processor.AutoJackson;
import peckb1.processor.AutoJacksonTypeClass;
import peckb1.processor.Named;

import java.util.Optional;

@AutoJackson(type = @AutoJackson.Type(RecursiveVariableType.class))
public interface RecursiveVariable {

    RecursiveVariableType getType();

    Integer getValue();

    String getName();

    @Named("roommate") Optional<RecursiveVariable> getChild();

    enum RecursiveVariableType {
        GOBO(Gobo.class),
        MOKEY(Mokey.class),
        WEMBLEY(Wembley.class),
        BOOBER(Boober.class),
        RED(Red.class);

        private final Class<? extends RecursiveVariable> variableClass;

        RecursiveVariableType(Class<? extends RecursiveVariable> variableClass) {
            this.variableClass = variableClass;
        }

        @AutoJacksonTypeClass
        public Class<? extends RecursiveVariable> getVariableClass() {
            return variableClass;
        }
    }
}
