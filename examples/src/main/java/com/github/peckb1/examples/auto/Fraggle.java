package com.github.peckb1.examples.auto;

import com.github.peckb1.examples.auto.fraggles.Boober;
import com.github.peckb1.examples.auto.fraggles.Gobo;
import com.github.peckb1.examples.auto.fraggles.Mokey;
import com.github.peckb1.examples.auto.fraggles.Wembley;
import com.github.peckb1.examples.auto.Fraggle.FraggleName;
import com.github.peckb1.examples.auto.fraggles.Red;
import com.github.peckb1.processor.AutoJackson;
import com.github.peckb1.processor.AutoJacksonTypeClass;
import com.github.peckb1.processor.Named;

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
