package com.github.peckb1.examples.auto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.peckb1.examples.auto.fraggles.Boober;
import com.github.peckb1.examples.auto.fraggles.Gobo;
import com.github.peckb1.examples.auto.fraggles.Mokey;
import com.github.peckb1.examples.auto.muppeteers.DaveGoelz;
import com.github.peckb1.examples.auto.muppeteers.JerryNelson;
import com.github.peckb1.examples.auto.muppeteers.KathrynMullen;
import com.github.peckb1.examples.auto.muppeteers.SteveWhitmire;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import peckb1.autojackson.AutoJacksonSetup;
import com.github.peckb1.examples.auto.Fraggle.FraggleName;
import com.github.peckb1.examples.auto.fraggles.Wembley;

import java.io.File;
import java.io.IOException;
import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestModel {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.registerModule(new Jdk8Module());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        AutoJacksonSetup.configureObjectMapper(this.objectMapper);
    }

    @Test
    public void testSimpleModel() throws IOException {
        File simpleModelFile = new File("resources/auto_model.json");
        Sample sample = this.objectMapper.readValue(simpleModelFile, Sample.class);

        checkSample(sample);

        String jsonData = this.objectMapper.writeValueAsString(sample);
        Sample sampleAgain = this.objectMapper.readValue(jsonData, Sample.class);

        checkSample(sampleAgain);
    }

    private void checkSample(Sample sample) throws IOException {
        assertEquals("A String", sample.getString());
        assertEquals(1, sample.getInt());
        assertEquals(true, sample.getBoolean());

        checkGobo(sample.getFraggle());

        List<Fraggle> fraggleList = sample.getFraggleList();
        assertEquals(2, fraggleList.size());
        checkMokey(fraggleList.get(0));
        checkBoober(fraggleList.get(1));

        Fraggle[] fraggleArray = sample.getFraggleArray();
        assertEquals(2, fraggleArray.length);
        checkBoober(fraggleArray[0]);
        checkMokey(fraggleArray[1]);

        assertEquals(Muppet.GONZO, sample.getMuppet());
        List<Muppet> muppetList = sample.getMuppetList();
        assertEquals(2, muppetList.size());
        assertEquals(Muppet.MISS_PIGGY, muppetList.get(0));
        assertEquals(Muppet.KERMIT, muppetList.get(1));

        Muppet[] muppetArray = sample.getMuppetArray();
        assertEquals(2, muppetArray.length);
        assertEquals(Muppet.KERMIT, muppetArray[0]);
        assertEquals(Muppet.MISS_PIGGY, muppetArray[1]);

        checkJunior(sample.getGorg());
        List<Gorg> gorgList = sample.getGorgList();
        assertEquals(2, gorgList.size());
        checkKing(gorgList.get(0));
        checkQueen(gorgList.get(1));

        Gorg[] gorgArray = sample.getGorgArray();
        assertEquals(2, gorgArray.length);
        checkQueen(gorgArray[0]);
        checkKing(gorgArray[1]);

        assertEquals(Instant.parse("2000-01-01T00:00:00Z"), sample.getStartTime());
        assertEquals(Date.from(Instant.parse("2001-01-01T00:00:00Z")), sample.getEndTime());
    }

    private void checkQueen(Gorg gorg) {
        assertEquals("Queen", gorg.getName());
        assertEquals(38, (int) gorg.getAge());
        assertTrue(gorg.getChild().isPresent());
        gorg.getChild().ifPresent(this::checkJunior);
    }

    private void checkKing(Gorg gorg) {
        assertEquals("King", gorg.getName());
        assertEquals(42, (int) gorg.getAge());
        assertTrue(gorg.getChild().isPresent());
        gorg.getChild().ifPresent(this::checkJunior);
    }

    private void checkJunior(Gorg gorg) {
        assertEquals("Junior", gorg.getName());
        assertEquals(15, (int) gorg.getAge());
        assertFalse(gorg.getChild().isPresent());
    }

    private void checkBoober(Fraggle fraggle) {
        assertEquals(FraggleName.BOOBER, fraggle.getName());
        assertEquals(11, (int) fraggle.getAge());
        assertEquals("clothes washer", fraggle.getJob());
        Muppeteer muppeteer = fraggle.getMuppeteer();
        assertTrue(muppeteer instanceof DaveGoelz);
        assertEquals("Dave Goelz", muppeteer.getName());
        assertEquals(9001, ((Boober) fraggle).getNumberOfSuperstitions());
        assertFalse(fraggle.getRoommate().isPresent());
    }

    private void checkMokey(Fraggle fraggle) {
        assertEquals(FraggleName.MOKEY, fraggle.getName());
        assertEquals(11, (int) fraggle.getAge());
        assertEquals("radish picker", fraggle.getJob());
        assertEquals(500, ((Mokey) fraggle).getRadishesPicked());
        assertFalse(fraggle.getRoommate().isPresent());
        Muppeteer muppeteer = fraggle.getMuppeteer();
        assertFalse(muppeteer instanceof KathrynMullen);
        assertEquals("Kathryn Mullen", muppeteer.getName());
    }

    private void checkGobo(Fraggle fraggle) throws IOException {
        assertEquals(FraggleName.GOBO, fraggle.getName());
        assertEquals(10, (int) fraggle.getAge());
        assertEquals("singer", fraggle.getJob());
        Muppeteer muppeteer = fraggle.getMuppeteer();
        assertTrue(muppeteer instanceof JerryNelson);
        assertEquals("Jerry Nelson", muppeteer.getName());
        assertEquals(20, ((Gobo) fraggle).getFetchedPostcards());
        Map map = ((Gobo) fraggle).getMap();
        assertEquals(1, map.size());
        assertEquals(4, ((Gobo) fraggle).getX());
        assertEquals(123, map.get("abc"));

        Optional<Fraggle> roommate = fraggle.getRoommate();
        assertTrue(roommate.isPresent());
        roommate.ifPresent(this::checkWembley);
    }

    private void checkWembley(Fraggle fraggle) {
        assertEquals(FraggleName.WEMBLEY, fraggle.getName());
        assertEquals(9, (int) fraggle.getAge());
        assertEquals("fire truck siren", fraggle.getJob());
        Muppeteer muppeteer = fraggle.getMuppeteer();
        assertTrue(muppeteer instanceof SteveWhitmire);
        assertEquals("Steve Whitmire", muppeteer.getName());
        assertEquals(15, ((Wembley) fraggle).getNumberOfFiresPutOut());
        assertFalse(fraggle.getRoommate().isPresent());
    }

}
