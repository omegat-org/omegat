package org.omegat.filters;

import org.omegat.filters2.text.bundles.ResourceBundleFilter;

public class ResourceBundleFilterTest  extends TestFilterBase {
    public void testParse() throws Exception {
	parse(new ResourceBundleFilter(), "test/data/filters/resourceBundle/file-ResourceBundleFilter.properties");
    }
    public void testTranslate() throws Exception {
	translateText(new ResourceBundleFilter(), "test/data/filters/resourceBundle/file-ResourceBundleFilter.properties");
    }
}
