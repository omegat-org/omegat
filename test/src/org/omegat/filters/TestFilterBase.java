package org.omegat.filters;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.IParseCallback;

import junit.framework.TestCase;

/**
 * Base class for test filter parsing.
 * 
 * @author Alex Buloichik <alex73mail@gmail.com>
 */
public abstract class TestFilterBase extends TestCase {
    protected List<String> parse(AbstractFilter filter, String filename) throws Exception {
	final List<String> result = new ArrayList<String>();

	filter.setParseCallback(new IParseCallback() {
	    public String processEntry(String entry) {
		if (entry.length() > 0)
		    result.add(entry);
		return entry;
	    }
	});
	filter.processFile(new File(filename), null, null, null);

	return result;
    }
}
