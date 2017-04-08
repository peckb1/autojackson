package com.github.peckb1.examples.auto.fraggles;

import com.github.peckb1.examples.auto.Fraggle;
import com.github.peckb1.examples.auto.muppeteers.KarenPrell;
import com.github.peckb1.processor.AutoJackson;
import com.github.peckb1.processor.Named;

@AutoJackson
public interface Red extends Fraggle<KarenPrell> {

    @Named("dives") int getDives();

}
