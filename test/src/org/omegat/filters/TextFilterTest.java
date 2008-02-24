package org.omegat.filters;

import java.util.List;

import org.junit.Test;
import org.omegat.filters2.text.TextFilter;
import org.omegat.filters2.text.TextOptions;

public class TextFilterTest extends TestFilterBase {
    @Test
    public void testTextFilterParsing() throws Exception {
	List<String> entries = parse(new TextFilter(), "test/data/filters/text/text1.txt");
	assertEquals("First entry\r\n", entries.get(0));
    }
    public void testTranslate() throws Exception {
	translateText(new TextFilter(), "test/data/filters/text/text1.txt");
    }

    @Test
    public void testParseNeverBreak() throws Exception {
	checkFile(TextOptions.SEGMENT_NEVER, 1, 1);
    }

    @Test
    public void testParseEmptyLinesBreak() throws Exception {
	checkFile(TextOptions.SEGMENT_EMPTYLINES, 3, 1);
    }

    @Test
    public void testParseLinesBreak() throws Exception {
	checkFile(TextOptions.SEGMENT_BREAKS, 3, 3);
    }

    protected void checkFile(int segValue, int count1, int count2) throws Exception {
	TextOptions options = new TextOptions();
	options.setSegmentOn(segValue);
	TextFilter filter = new TextFilter();
	filter.setOptions(options);

	List<String> entries = parse(filter, "test/data/filters/text/file-TextFilter.txt");
	assertEquals(count1, entries.size());

	entries = parse(filter, "test/data/filters/text/file-TextFilter-noemptylines.txt");
	assertEquals(count2, entries.size());
    }
}
