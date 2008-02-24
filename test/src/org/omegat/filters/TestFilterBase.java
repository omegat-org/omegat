package org.omegat.filters;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.IParseCallback;
import org.omegat.util.LFileCopy;

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

    protected void translateText(AbstractFilter filter, String filename) throws Exception {
	filter.setParseCallback(new IParseCallback() {
	    public String processEntry(String entry) {
		return entry;
	    }
	});
	File outFile = new File(System.getProperty("java.io.tmpdir"), "OmegaT filter test");
	outFile.getParentFile().mkdir();
	filter.processFile(new File(filename), null, outFile, null);
	compare(new File(filename), outFile);
    }

    protected void compare(File f1, File f2) throws Exception {
	ByteArrayOutputStream d1 = new ByteArrayOutputStream();
	LFileCopy.copy(f1, d1);

	ByteArrayOutputStream d2 = new ByteArrayOutputStream();
	LFileCopy.copy(f2, d2);

	assertEquals(d1.size(), d2.size());
	byte[] a1 = d1.toByteArray();
	byte[] a2 = d2.toByteArray();
	for (int i = 0; i < d1.size(); i++) {
	    assertEquals(a1[i], a2[i]);
	}
    }
}
