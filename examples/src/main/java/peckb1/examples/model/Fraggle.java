package peckb1.examples.model;

import peckb1.examples.model.Fraggle.FraggleName;
import peckb1.examples.model.fraggles.Boober;
import peckb1.examples.model.fraggles.Gobo;
import peckb1.examples.model.fraggles.Mokey;
import peckb1.examples.model.fraggles.Red;
import peckb1.examples.model.fraggles.Wembley;
import peckb1.processor.AutoJackson;
import peckb1.processor.AutoJacksonTypeClass;
import peckb1.processor.Named;

import java.util.Optional;

@AutoJackson(type = @AutoJackson.Type(FraggleName.class))
public interface Fraggle {

    FraggleName getName();
    Integer getAge();
    @Named("occupation") String getJob();
    Optional<Fraggle> getRoommate();

    enum FraggleName {
        GOBO(Gobo.class),
        MOKEY(Mokey.class),
        WEMBLEY(Wembley.class),
        BOOBER(Boober.class),
        RED(Red.class);

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
