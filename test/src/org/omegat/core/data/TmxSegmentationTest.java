package org.omegat.core.data;

import java.io.File;
import java.util.TreeMap;

import junit.framework.Assert;
import junit.framework.TestCase;

import org.junit.Test;
import org.omegat.core.segmentation.SRX;
import org.omegat.core.segmentation.Segmenter;
import org.omegat.util.Language;

public class TmxSegmentationTest extends TestCase {
    @Test
    public void testProjectTMX() throws Exception {
        Segmenter.srx = SRX.getSRX();

        ProjectProperties props = new ProjectProperties();
        props.setSupportDefaultTranslations(true);
        props.setSourceLanguage(new Language("en"));
        props.setTargetLanguage(new Language("fr"));
        props.setSentenceSegmentingEnabled(true);
        ProjectTMX tmx = new ProjectTMX(props, new File("test/data/tmx/resegmenting.tmx"),
                new ProjectTMX.CheckOrphanedCallback() {
                    public boolean existSourceInProject(String src) {
                        return true;
                    }

                    public boolean existEntryInProject(EntryKey key) {
                        return true;
                    }
                }, new TreeMap<EntryKey, TMXEntry>());

        Assert.assertEquals(2, tmx.translationDefault.size());
        Assert.assertEquals("Ceci est un test.", tmx.translationDefault.get("This is test.").translation);
        Assert.assertEquals("Juste un test.", tmx.translationDefault.get("Just a test.").translation);
    }

    @Test
    public void testExternalTMX() throws Exception {
        Segmenter.srx = SRX.getSRX();

        ProjectProperties props = new ProjectProperties();
        props.setSupportDefaultTranslations(true);
        props.setSourceLanguage(new Language("en"));
        props.setTargetLanguage(new Language("fr"));
        props.setSentenceSegmentingEnabled(true);

        ExternalTMX tmx = new ExternalTMX(props, new File("test/data/tmx/resegmenting.tmx"), false, false);

        Assert.assertEquals(2, tmx.getEntries().size());
        Assert.assertEquals("This is test.", tmx.getEntries().get(0).source);
        Assert.assertEquals("Ceci est un test.", tmx.getEntries().get(0).translation);
        Assert.assertEquals("Just a test.", tmx.getEntries().get(1).source);
        Assert.assertEquals("Juste un test.", tmx.getEntries().get(1).translation);
    }
}
