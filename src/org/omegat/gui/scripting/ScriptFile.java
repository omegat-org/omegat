package org.omegat.gui.scripting;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.omegat.util.LFileCopy;
import org.omegat.util.LinebreakPreservingReader;
import org.omegat.util.OConsts;

/**
 * An abstract representation of script file.
 * The content is treated as UTF-8.
 */
class ScriptFile extends File {
	private static final long serialVersionUID = -8439864913521910527L;

	private final String BOM = "\uFEFF";
    private boolean startsWithBOM = false;
    private String lineBreak = System.getProperty("line.separator");

    public ScriptFile(String pathname) {
        super(pathname);
    }

    public ScriptFile(File parent, String child) {
        super(parent, child);
    }

    public String getText() throws FileNotFoundException, IOException {
        String ret = "";
        LinebreakPreservingReader lpin = null;
        try {
            lpin = getUTF8LinebreakPreservingReader(this);
            StringBuilder sb = new StringBuilder();
            String s = lpin.readLine();
            startsWithBOM = s.startsWith(BOM);
            if (startsWithBOM) {
                s = s.substring(1);  // eat BOM
            }
            while (s != null) {
                sb.append(s);
                String br = lpin.getLinebreak();
                if (! br.isEmpty()) {
                    lineBreak = br;
                    sb.append('\n');
                }
                s = lpin.readLine();
            }
            ret = sb.toString();
        } finally {
            if (lpin != null) {
                try {
                    lpin.close();
                } catch (IOException ex) {
                    // Eat exception silently
                }
            }
        }
        return ret;
    }

    private LinebreakPreservingReader getUTF8LinebreakPreservingReader(File file) throws FileNotFoundException, UnsupportedEncodingException {
        InputStream is = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(is, OConsts.UTF8);
        BufferedReader in = new BufferedReader(isr);
        return new LinebreakPreservingReader(in);
    }

    public void setText(String text) throws UnsupportedEncodingException, IOException {
        text = text.replaceAll("\n", lineBreak);
        if (startsWithBOM) {
            text = BOM + text;
        }

        InputStream is = new ByteArrayInputStream(text.getBytes(OConsts.UTF8));
        LFileCopy.copy(is, this);
    }
}