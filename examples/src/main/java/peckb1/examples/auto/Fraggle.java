package peckb1.examples.auto;

import peckb1.examples.auto.Fraggle.FraggleName;
import peckb1.examples.auto.fraggles.Boober;
import peckb1.examples.auto.fraggles.Gobo;
import peckb1.examples.auto.fraggles.Mokey;
import peckb1.examples.auto.fraggles.Red;
import peckb1.examples.auto.fraggles.Wembley;
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
