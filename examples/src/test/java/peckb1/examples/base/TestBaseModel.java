package peckb1.examples.base;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import peckb1.autojackson.AutoJacksonSetup;
import peckb1.examples.base.Fraggle.FraggleName;
import peckb1.examples.base.fraggles.Boober;
import peckb1.examples.base.fraggles.Wembley;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class TestBaseModel {

    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new Jdk8Module());
        this.objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        this.objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.NONE);
        AutoJacksonSetup.configureObjectMapper(this.objectMapper);
    }

    @Test
    public void testSimpleModel() throws IOException {
        File baseModelFile = new File("resources/base_model.json");
        FraggleList fraggleList = this.objectMapper.readValue(baseModelFile, FraggleList.class);

        checkFraggleList(fraggleList);

        String fraggleListJson = this.objectMapper.writeValueAsString(fraggleList);
        FraggleList fraggleListAgain = this.objectMapper.readValue(fraggleListJson, FraggleList.class);

        checkFraggleList(fraggleListAgain);
    }

    private void checkFraggleList(FraggleList fraggleListAgain) {
        List<Fraggle> fraggles = fraggleListAgain.getFraggles();
        checkBoober(fraggles.get(0));
        checkWembley(fraggles.get(1));
    }

    private void checkWembley(Fraggle fraggle) {
        Assert.assertTrue(fraggle instanceof Wembley);
        Assert.assertEquals(FraggleName.WEMBLEY, fraggle.getFraggleName());
        Assert.assertEquals("yellow", fraggle.getHairColour());
        Assert.assertEquals(false, fraggle.wearsHats());
        Assert.assertFalse(fraggle.getJob().isPresent());
    }

    private void checkBoober(Fraggle fraggle) {
        Assert.assertTrue(fraggle instanceof Boober);
        Assert.assertEquals(FraggleName.BOOBER, fraggle.getFraggleName());
        Assert.assertEquals("blue", fraggle.getHairColour());
        Assert.assertEquals(true, fraggle.wearsHats());
        Assert.assertTrue(fraggle.getJob().isPresent());
        Job job = fraggle.getJob().get();
        checkActorJob(job);
    }

    private void checkActorJob(Job job) {
        Assert.assertEquals(0, job.getDaysWorked());
        Assert.assertEquals("Actor", job.getOccupation());
    }

}
