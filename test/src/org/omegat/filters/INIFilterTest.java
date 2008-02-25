package org.omegat.filters;

import org.omegat.filters2.text.ini.INIFilter;

public class INIFilterTest extends TestFilterBase {
    public void testParse() throws Exception {
	parse(new INIFilter(), "test/data/filters/ini/file-INIFilter.ini");
    }

    public void testTranslate() throws Exception {
	translateText(new INIFilter(), "test/data/filters/ini/file-INIFilter.ini");
    }
}
