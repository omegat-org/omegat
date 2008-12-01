/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Thomas Huriaux
               2008 Martin Fleurke
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.filters2.po;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.OStrings;

/**
 * Filter to support po files (in various encodings).
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Thomas Huriaux
 * @author Martin Fleurke
 */
public class PoFilter extends AbstractFilter
{

    public String getFileFormatName()
    {
        return OStrings.getString("POFILTER_FILTER_NAME");
    }

    public Instance[] getDefaultInstances()
    {
        return new Instance[]
            {
                new Instance("*.po"),                                           // NOI18N
                new Instance("*.pot")                                           // NOI18N
            };
    }

    public boolean isSourceEncodingVariable()
    {
        return true;
    }

    public boolean isTargetEncodingVariable()
    {
        return true;
    }

    public void processFile(BufferedReader in, BufferedWriter out)
            throws IOException
    {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch!=0xFEFF)
            in.reset();

        processPoFile(in, out);
    }

    private void processPoFile(BufferedReader in, Writer out)
            throws IOException
    {
        StringBuffer nontrans = new StringBuffer();
        StringBuffer trans = new StringBuffer();
        StringBuffer trans_plural = new StringBuffer();
        String s;
        boolean msgid = false;
        boolean msgstr = false;
        boolean msgid_plural = false;
        boolean msgstr_plural = false;
        boolean potential_header = false;
        boolean header = false;
        boolean nowrap = false;
        while( (s=in.readLine())!=null )
        {
            // Removing the fuzzy markers, as it has no meanings after being
            // processed by omegat
            if (s.matches("#, fuzzy"))                                          // NOI18N
            {
                continue;
            }
            else if (s.matches("#,.* fuzzy.*"))                                 // NOI18N
            {
                s=s.replaceAll("(.*), fuzzy(.*)", "$1$2");                      // NOI18N
            }

            //FSM for po files
            if (s.matches("#,.* no-wrap.*"))                                    // NOI18N
            {
                //Read the no-wrap comment, indicating that the creator of
                //the po-file did not want long messages to be wrapped on 
                //multiple lines.
                //See §5.6.2 no-wrap of 
                //http://docs.oasis-open.org/xliff/v1.2/xliff-profile-po/xliff-profile-po-1.2-cd02.html 
                //for an example.
                nowrap = true;
                //Keep the comment in the target file.
                nontrans.append(s);
                nontrans.append("\n");                                          // NOI18N
            }
            else if (s.matches("msgid \"\""))                                   // NOI18N
            {
                //Read empty id, which could be special po-header.
                //Adding this to output without further processing
                potential_header = true;
                msgid = true;
                nontrans.append(s+"\n");                                        // NOI18N
            }
            else if (s.matches("msgid \".*\""))                                 // NOI18N
            {
                //Read id.
                //Adding contents to to-be-translated buffer (for writing 
                //translation later on) and adding the id to output without 
                //further processing
                msgid = true;
                trans.append(s.replaceAll("msgid \"(.*)\"","$1"));              // NOI18N
                nontrans.append(s+"\n");                                        // NOI18N
            }
            else if ((s.matches("\"(.*)\"")) && msgid)                          // NOI18N
            {
                //Read next part of id.
                //Adding contents to to-be-translated buffer (for writing 
                //translation later on) and adding the id to output without 
                //further processing
                //It can't be special po-header (which is only the empty id)
                potential_header = false;
                trans.append(s.replaceAll("\"(.*)\"","$1"));                    // NOI18N
                nontrans.append(s);
                nontrans.append("\n");                                          // NOI18N
            }
            else if (s.matches("\"(.*)\"") && msgid_plural)                     // NOI18N
            {
                //Read next part of plural id.
                //Adding contents to to-be-translated-plural buffer (for writing 
                // translation later on) and adding the id to output without 
                //further processing
                //It can't be special po-header (which is only the empty id)
                potential_header = false;
                trans_plural.append(s.replaceAll("\"(.*)\"","$1"));             // NOI18N
                nontrans.append(s);
                nontrans.append("\n");                                          // NOI18N
            }
            else if (s.matches("msgid_plural \".*\""))                          // NOI18N
            {
                //Read plural id.
                //Adding contents to to-be-translated-plural buffer (for writing 
                //translation later on) and adding the id to output without 
                //further processing
                //It can't be special po-header (which is only the empty id) 
                //nor singular-id.
                potential_header = false;
                msgid = false;
                msgid_plural = true;
                trans_plural.append(s.replaceAll("msgid_plural \"(.*)\"","$1"));// NOI18N
                nontrans.append(s+"\n");                                        // NOI18N
            }
            else if (s.matches("msgstr \".*\""))                                // NOI18N
            {
                //Read first line of translation, or of the special po-header contents
                //It can't be an id anymore.
                msgid = false;
                if (potential_header)
                {
                    //A po-header is an empty id and a non-empty message string, 
                    //of which the first line is empty
                    //the string is added to the translation, so it can be 
                    //changed. But the first line should be empty, so we skip 
                    //adding the first line, and instead add it to the output 
                    //immediately.
                    header = true;
                    msgstr = true;
                    nontrans.append(s);
                    nontrans.append("\n");                                      // NOI18N
                }
                else
                {
                    //Translation found.
                    msgstr = true;
                    //We don't use the existing translation, we make a new 
                    //translation
                    //First, we output the comments and message header that were 
                    //read before.
                    //Second, we output the msgstr line with a new translation
                    nontrans.append("msgstr ");                                 // NOI18N
                    out.write(nontrans.toString());
                    nontrans.setLength(0);

                    out.write(privateProcessEntry(trans.toString(), nowrap));
                    trans.setLength(0);
                    out.write("\n");                                            // NOI18N
                }
            }
            else if ((s.matches("\"(.*)\"")) && (msgstr || msgstr_plural) )     // NOI18N
            {
                if (header)
                {
                    //Another po-header line. The special po-header is added to 
                    //the to-be-translated buffer.
                    //It is translated when the entire header is read. 
                    //(when an empty line is read, see below)
                    trans.append(s.replaceAll("\"(.*)\"","$1"));                // NOI18N
                } else
                {
                  //Next part of translation found.
                  //We don't use the existing translation, and have already 
                  //written the new translation
                  //This line is therefore ignored
                }
            }
            else if (s.matches("msgstr\\[0\\] \".*\""))                         // NOI18N
            {
                //Read first line of the first plural translation
                //Previously we could have been reading a plural msg id, but now 
                //it clearly has finished.
                msgid_plural = false;
                msgstr = true;

                //We don't use the existing translation, we make a new translation
                //First, we output the comments and message header that were 
                //read before.
                //Second, we output the msgstr line with a new translation
                nontrans.append("msgstr[0] ");                                  // NOI18N
                out.write(nontrans.toString());
                nontrans.setLength(0);

                out.write(privateProcessEntry(trans.toString(), nowrap));
                trans.setLength(0);
                out.write("\n");                                                // NOI18N
            }
            else if (s.matches("msgstr\\[1\\] \".*\""))                         // NOI18N
            {
                //Read first line of the second plural translation
                //Previously we could be reading a first message string, but now 
                //we started already reading the second msg string
                msgstr = false;
                msgstr_plural = true;
                //We don't use the existing translation, we make a new translation
                //We output the new translation in a msgstr[1] line.
                nontrans.append("msgstr[1] ");                                  // NOI18N
                out.write(nontrans.toString());
                nontrans.setLength(0);

                out.write(privateProcessEntry(trans_plural.toString(), nowrap));
                trans_plural.setLength(0);
                out.write("\n");                                                // NOI18N
            }
            else if (header)
            {
                //Read something unknown (not a msg id, msg str or special 
                //comment line) e.g. an empty line that separates two messages, 
                //or a comment-line indicating a new message
                //This indicates the end of the po-header.
                header = false;
                potential_header = false;
                msgstr = false;

                //Output the current buffer (containing 'msgid ""\n')
                out.write(nontrans.toString());
                nontrans.setLength(0);
                //Write header as msgstr. The translation should contain a 
                //couple of lines.
                //Each line has to be written on a separate line within quotes, 
                //else it is an invalid po-header.
                //This is taken care of by privateProcessEntry
                String headerString = privateProcessEntry(trans.toString(), false);
                out.write(headerString);
                trans.setLength(0);
                nontrans.append(s);
                nontrans.append("\n");                                          // NOI18N
            }
            else
            {
                //Read something unknown (not a msg id, msg str or special 
                //comment line) e.g. an empty line that separates two messages, 
                //or a comment-line indicating a new message

                //Initialize state machine
                nowrap = false;
                msgstr = false;
                msgstr_plural = false;
                potential_header = false;
                //Append this empty or comment line to output.
                nontrans.append(s);
                nontrans.append("\n");                                          // NOI18N
            }
        }
        //output remaining buffers
        if( nontrans.length()>0 )
            out.write(nontrans.toString());
        if( trans.length()>0 )
            out.write(privateProcessEntry(trans.toString(), nowrap));
        if( trans_plural.length()>0 )
          out.write(privateProcessEntry(trans_plural.toString(), nowrap));
    }

    /**
     * Private processEntry to do pre- and postprocessing.<br>
     * The given entry is interpreted to a string (e.g. escaped quotes are unescaped, 
     * '\n' is translated into newline character, '\t' into tab character.)
     * then translated and then returned as a PO-string-notation (e.g. double 
     * quotes escaped, newline characters represented as '\n' and surrounded by 
     * double quotes, possibly split up over multiple lines)<Br>
     * Long translations are not split up over multiple lines as some PO editors do, 
     * but when there are newline characters in a translation, it is split up at 
     * the newline markers.<Br>
     * If the nowrap parameter is true, a translation that exists of multiple 
     * lines starts with an empty string-line to left-align all lines. 
     * [With nowrap set to true, long lines are also never wrapped (except for 
     * at newline characters), but that was already not done without nowrap.]
     * [ 1869069 ] Escape support for PO
     * @param entry The entire source text, without it's surrounding double quotes, 
     * but otherwise not-interpreted
     * @param nowrap gives indication if the translation should not be wrapped
     * over multiple lines and all lines be left-aligned. 
     * @return The translated entry, within double quotes on each line 
     * (thus ready to be printed to target file immediately)
     **/
    private String privateProcessEntry(String entry, boolean nowrap)
    {
        // Removes escapes from quotes. ( \" becomes " unless the \
        // was escaped itself.) The number of preceding slashes before \"
        // should not be odd, else the \ is escaped and not part of \".
        // The regex is: no backslash before an optional even number
        // of backslashes before \". Replace only the \" with " and keep the
        // other escaped backslashes )
        entry = entry.replaceAll("(?<!\\\\)((\\\\\\\\)*)\\\\\"", "$1\"");       // NOI18N
        // Interprets newline sequence, except when preceded by \
        // \n becomes Linefeed, unless the \ was escaped itself.
        // The number of preceding slashes before \n should not be  odd,
        // else the \ is escaped and not part of \n.
        // The regex is: no backslash before an optional even number of
        // backslashes before \n. Replace only the \n with <newline> and keep
        // the other escaped backslashes.
        entry = entry.replaceAll("(?<!\\\\)((\\\\\\\\)*)\\\\n", "$1\n");        // NOI18N
        //same for \t, the tab character
        entry = entry.replaceAll("(?<!\\\\)((\\\\\\\\)*)\\\\t", "$1\t");        // NOI18N
        // Interprets newline sequence at the beginning of a line
        entry = entry.replaceAll("^\\\\n", "\\\n");                             // NOI18N
        // Removes escape from backslash
        entry = entry.replace("\\\\", "\\");                                    // NOI18N

        //Do real translation
        String translation = processEntry(entry);

        // Escapes backslash
        translation = translation.replace("\\", "\\\\");                        // NOI18N
        // Adds escapes to quotes. ( " becomes \" )
        translation = translation.replace("\"", "\\\"");                        // NOI18N

        /* Normally, long lines are wrapped at 'output page width', 
         * which defaults to ?76?,
         * and always at newlines.
         * IF the no-wrap indicator is present, long lines should not be wrapped,
         * except on newline characters, in which case the first line should be empty,
         * so that the different lines are aligned the same.
         * OmegaT < 2.0 has never wrapped any line, and it is quite useless when 
         * the po-file is not edited with a plain-text-editor.
         * But it is simple to wrap at least at newline characters (which is 
         * necessary for the translation of the po-header anyway)
         * We can also honor the no-wrap instruction at least by letting the 
         * first line of a multi-line translation not be on the same line 
         * as 'msgstr'.
         */
        // Interprets newline chars. 'blah<br>blah' becomes 'blah\n"<br>"blah'
        translation = translation.replace("\n", "\\n\"\n\"");                   // NOI18N
        //don't make empty new line at the end (in case the last 'blah' is empty string)
        if (translation.endsWith("\"\n\""))                                     // NOI18N
        {
            translation = translation.substring(0,translation.length()-3);
        }
        if (nowrap && translation.contains("\n")) {                             // NOI18N
            //start with empty string, to align all lines of translation
            translation = "\"\n\""+translation;                                 // NOI18N
        }
        // Interprets tab chars. 'blah<tab>blah' becomes 'blah\tblah' 
        //(<tab> representing the tab character '\u0009')
        translation = translation.replace("\t", "\\t");                         // NOI18N
        return "\""+translation+"\"";                                           // NOI18N
    }
}