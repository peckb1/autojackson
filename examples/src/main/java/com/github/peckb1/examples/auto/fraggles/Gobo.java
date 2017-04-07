package com.github.peckb1.examples.auto.fraggles;

import com.github.peckb1.examples.auto.Fraggle;
import com.github.peckb1.examples.auto.muppeteers.JerryNelson;
import com.github.peckb1.processor.AutoJackson;
import com.github.peckb1.processor.Named;

import java.io.IOException;
import java.util.Map;

@AutoJackson
public interface Gobo<X extends Number> extends Fraggle<JerryNelson> {

    @Named("postcards") int getFetchedPostcards();

    X getX();

    @Named("zed") Map getMap() throws IOException;
}
