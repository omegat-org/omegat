package org.omegat.core.dictionaries;

import java.io.File;

import org.omegat.core.TestCore;

public class StarDictTest extends TestCore {
    public void testStarDict() throws Exception {
        StarDict s = new StarDict(new File("test/data/dicts/latin-francais.ifo"));
        s.readHeader();
    }
}
