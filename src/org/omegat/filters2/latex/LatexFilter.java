/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Thomas Huriaux
               2008 Martin Fleurke
               2009 Arno Peters
               2011 Didier Briel
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

package org.omegat.filters2.latex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.Log;
import org.omegat.util.OStrings;

/**
 * Filter to support LaTeX files.
 * 
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Thomas Huriaux
 * @author Martin Fleurke
 * @author Arno Peters
 * @author Didier Briel
 */
public class LatexFilter extends AbstractFilter {

    public String getFileFormatName() {
        return OStrings.getString("LATEXFILTER_FILTER_NAME");
    }

    public Instance[] getDefaultInstances() {
        return new Instance[] { new Instance("*.tex"), new Instance("*.latex"), };
    }

    public boolean isSourceEncodingVariable() {
        return true;
    }

    public boolean isTargetEncodingVariable() {
        return true;
    }
    
    @Override
    protected boolean requirePrevNextFields() {
        return true;
    }

    public void processFile(BufferedReader in, BufferedWriter out) throws IOException {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch != 0xFEFF)
            in.reset();

        init();

        processLatexFile(in, out);
    }

    private int findStringCategory(String c) {
        if (c.equals("\\")) {
            return 0;
        } else if (c.equals("{")) {
            return 1;
        } else if (c.equals("}")) {
            return 2;
        } else if (c.equals("$")) {
            return 3;
        } else if (c.equals("&")) {
            return 4;
        } else if (c.equals("\n")) {
            return 5;
        } else if (c.equals("#")) {
            return 6;
        } else if (c.equals("^")) {
            return 7;
        } else if (c.equals("_")) {
            return 8;
        } else if (c.equals("\000")) {
            return 9;
        } else if (c.matches("[ \t]")) {
            return 10;
        } else if (c.matches("[a-zA-Z]")) {
            return 11;
        } else if (c.equals("~")) {
            return 13;
        } else if (c.equals("%")) {
            return 14;
        }

        return 12;
    }

    /**
     * Processes a LaTeX document
     * 
     * @param in
     *            Source document
     * @param out
     *            Target document
     * @throws java.io.IOException
     */
    private void processLatexFile(BufferedReader in, Writer out) throws IOException {
        StringBuffer par = new StringBuffer();
        String s;
        StringBuffer comment = new StringBuffer();

        LinkedList<String> commands = new LinkedList<String>();

        /**
         * Possible states: N: beginning of a new line M: middle S: skipping
         * blanks
         */
        String state;
        while ((s = in.readLine()) != null) {
            String[] c = s.split("");
            state = "N";

            int idx = 1;
            while (idx < c.length) {
                String cidx = c[idx];
                int cat = findStringCategory(cidx);

                if (cat == 0) {
                    /* parse control sequence */
                    StringBuffer cmd = new StringBuffer();
                    cmd.append(cidx);
                    idx++;
                    while (idx < c.length) {
                        String cmdc = c[idx];
                        if (findStringCategory(cmdc) == 11) {
                            cmd.append(cmdc);
                        } else if (cmd.length() == 1) {
                            cmd.append(cmdc);
                            state = "M";
                            break;
                        } else {
                            idx--;
                            // state = "S";
                            state = "M";
                            break;
                        }
                        idx++;
                    }

                    if (!commands.contains(cmd.toString()))
                        commands.add(cmd.toString());
                    par.append(cmd);
                } else if (cat == 4) {
                    /* table column separator */
                    out.write(processParagraph(commands, par.toString()));
                    out.write("&");
                    par.setLength(0);
                    // System.out.println(commands);
                    commands.clear();
                } else if (cat == 10) {
                    if (state.equals("M")) {
                        state = "S";
                        par.append(cidx);
                    }
                } else if (cat == 14) {
                    /* parse comment */
                    comment.append(cidx);
                    idx++;
                    while (idx < c.length) {
                        String commentc = c[idx];
                        comment.append(commentc);
                        idx++;
                    }
                } else {
                    state = "M";
                    par.append(cidx);
                }

                idx++;
            }

            /* at the end of the line */
            if (state.equals("N")) {
                /* \par */
                if (par.length() > 0) {
                    out.write(processParagraph(commands, par.toString()));
                    out.write("\n\n");
                    par.setLength(0);
                }
                // System.out.println(commands);
                commands.clear();
                if (comment.length() > 0) { // If there is a comment, write it
                     out.write(comment.toString());
                     out.write("\n");
                     comment.setLength(0);
                }
            } else if (state.equals("M")) {
                par.append(" ");
            }
        }

        // output remaining buffers
        if (par.length() > 0)
            out.write(processParagraph(commands, par.toString()));

    }

    private String substituteUnicode(String par) {
        par = par.replaceAll("\\\\\\\\", "<br0>");
        par = par.replaceAll("\\{?\\\\ss\\}?", "\u00df");
        par = par.replaceAll("\\{?\\\\glqq\\}?(\\{\\})?", "\u301f");
        par = par.replaceAll("\\{?\\\\grqq\\}?(\\{\\})?", "\u301d");
        par = par.replaceAll("\\{?\\\\glq\\}?(\\{\\})?", "\u201a");
        par = par.replaceAll("\\{?\\\\grq\\}?(\\{\\})?", "\u2018");
        par = par.replaceAll("\\\\%", "%");
        par = par.replaceAll("\\\\-", "\u00ad");
        par = par.replaceAll("\\\\,", "\u2009");
        par = par.replaceAll("~", "\u00a0");
        return par;
    }

    private String resubstituteTex(String par) {
        par = par.replaceAll("\u00a0", "~");
        par = par.replaceAll("\u2009", "\\\\,");
        par = par.replaceAll("\u00ad", "\\\\-");
        par = par.replaceAll("%", "\\\\%");
        par = par.replaceAll("<br0>", "\\\\\\\\");
        return par;
    }

    private LinkedList<String> oneArgNoText = new LinkedList<String>();
    private LinkedList<String> oneArgInlineText = new LinkedList<String>();
    private LinkedList<String> oneArgParText = new LinkedList<String>();

    private void init() {
        oneArgNoText.add("\\begin");
        oneArgNoText.add("\\end");
        oneArgNoText.add("\\cite");
        oneArgNoText.add("\\label");
        oneArgNoText.add("\\ref");
        oneArgNoText.add("\\pageref");
        oneArgNoText.add("\\pagestyle");
        oneArgNoText.add("\\thispagestyle");
        oneArgNoText.add("\\vspace");
        oneArgNoText.add("\\hspace");
        oneArgNoText.add("\\vskip");
        oneArgNoText.add("\\hskip");
        oneArgNoText.add("\\put");
        oneArgNoText.add("\\includegraphics");
        oneArgNoText.add("\\documentclass");
        oneArgNoText.add("\\usepackage");

        oneArgInlineText.add("\\emph");
        oneArgInlineText.add("\\textbf");
        oneArgInlineText.add("\\texttt");
        oneArgInlineText.add("\\textsf");
        oneArgInlineText.add("\\textit");
        oneArgInlineText.add("\\hbox");
        oneArgInlineText.add("\\mbox");
        oneArgInlineText.add("\\vbox");

        oneArgParText.add("\\typeout");
        oneArgParText.add("\\footnote");
        oneArgParText.add("\\author");
        oneArgParText.add("\\index");
        oneArgParText.add("\\title");
        oneArgParText.add("\\Chapter");
        oneArgParText.add("\\chapter");
        oneArgParText.add("\\section");
    }

    private String replaceOneArgNoText(LinkedList<String[]> substituted, LinkedList<String> commands,
            String par) {
        int counter = 0;

        for (Iterator<String> it = commands.iterator(); it.hasNext();) {
            String command = it.next();

            StringBuffer sb = new StringBuffer();

            if (oneArgNoText.contains(command)) {
                String find = ("\\" + command + "\\*?" + "(" + "\\[" + "[^\\]]*" + "\\]" + // opt
                                                                                           // []
                                                                                           // arg
                        "|" + "\\(" + "[^\\)]*" + "\\)" + // opt () arg
                        ")?\\s*" + "\\{" + "[^\\}]*+" + "\\}");

                Pattern p = Pattern.compile(find);
                Matcher m = p.matcher(par);
                while (m.find()) {
                    String replace = "<n" + String.valueOf(counter) + ">";
                    String[] subst = { reHarden(m.group(0)), reHarden(replace) };
                    substituted.addFirst(subst);
                    m.appendReplacement(sb, replace);
                    counter++;
                }
                m.appendTail(sb);

                par = sb.toString();
            }
        }
        return par;
    }

    private String replaceOneArgInlineText(LinkedList<String[]> substituted, LinkedList<String> commands,
            String par) {
        int counter = 0;

        for (Iterator<String> it = commands.iterator(); it.hasNext();) {
            String command = it.next();

            StringBuffer sb = new StringBuffer();

            if (oneArgInlineText.contains(command)) {
                String find = ("(" + "\\" + command + "\\s*" + "\\{" + ")" + "(" + "[^\\}]*+" + ")" + "\\}");

                Pattern p = Pattern.compile(find);
                Matcher m = p.matcher(par);
                while (m.find()) {
                    String preReplace = "<i" + String.valueOf(counter) + ">";
                    String postReplace = "</i" + String.valueOf(counter) + ">";

                    String[] s1 = { reHarden(m.group(1)), reHarden(preReplace) };
                    substituted.addFirst(s1);

                    String[] s2 = { reHarden("}"), reHarden(postReplace) };
                    substituted.addFirst(s2);

                    String replace = (preReplace + "$2" + postReplace);
                    m.appendReplacement(sb, replace);
                    counter++;
                }
                m.appendTail(sb);

                par = sb.toString();
            }
        }
        return par;
    }

    private String replaceOneArgParText(LinkedList<String[]> substituted, LinkedList<String> commands,
            String par) {
        int counter = 0;

        for (Iterator<String> it = commands.iterator(); it.hasNext();) {
            String command = it.next();

            StringBuffer sb = new StringBuffer();

            if (oneArgParText.contains(command)) {
                String find = ("(" + "\\" + command + "\\*?\\s*" + ")" + "\\{" + "(" + "[^\\}]*+" + ")" + "\\}");

                Pattern p = Pattern.compile(find);
                Matcher m = p.matcher(par);
                while (m.find()) {
                    String replace = "<p" + String.valueOf(counter) + ">";
                    String content = "";
                    if (m.group(2) != null)
                        content = processParagraph(commands, m.group(2));

                    String[] subst = { reHarden(m.group(1) + "{" + content + "}"), reHarden(replace) };

                    substituted.addFirst(subst);
                    m.appendReplacement(sb, replace);
                    counter++;
                }
                m.appendTail(sb);

                par = sb.toString();
            }
        }
        return par;
    }

    private String replaceUnknownCommand(LinkedList<String[]> substituted, LinkedList<String> commands,
            String par) {
        int counter = 0;

        for (Iterator<String> it = commands.iterator(); it.hasNext();) {
            String command = it.next();

            if (command.equals("\\\\") || command.equals("\\{") || command.equals("\\["))
                // continue;
                command = "\\" + command;

            StringBuffer sb = new StringBuffer();
            String find = "\\" + command;

            try {
                Pattern p = Pattern.compile(find);
                Matcher m = p.matcher(par);
                while (m.find()) {
                    String replace = "<u" + String.valueOf(counter) + ">";
                    String[] subst = { reHarden(m.group(0)), reHarden(replace) };
                    substituted.addFirst(subst);
                    m.appendReplacement(sb, replace);
                    counter++;
                }
                m.appendTail(sb);

                par = sb.toString();
             } catch (java.util.regex.PatternSyntaxException e) {
               //TODO: understand the exceptions
               Log.log("LaTeX PatternSyntaxException: " + e.getMessage());
               Log.log(command);
            }

        }
        return par;
    }

    private String reHarden(String re) {
        re = re.replaceAll("\\\\", "\\\\\\\\"); // replace \ with \\
        re = re.replaceAll("\\[", "\\\\[");
        re = re.replaceAll("\\^", "\\\\^");
        re = re.replaceAll("\\$", "\\\\\\$");
        re = re.replaceAll("\\{", "\\\\{");
        return re;
    }

    private String processParagraph(LinkedList<String> commands, String par) {
        LinkedList<String[]> substituted = new LinkedList<String[]>();

        par = substituteUnicode(par);

        par = replaceOneArgNoText(substituted, commands, par);
        par = replaceOneArgInlineText(substituted, commands, par);
        par = replaceOneArgParText(substituted, commands, par);
        par = replaceUnknownCommand(substituted, commands, par);

        String find = ("^((\\s*</?[nipu]\\d+>\\s*)*)" + "(.*?)" + "((\\s*</?[nipu]\\d+>\\s*)*)$");
        Pattern p = Pattern.compile(find);
        Matcher m = p.matcher(par);
        if (m.find()) {
            par = "";
            if (m.group(1) != null)
                par += m.group(1);
            if (m.group(3) != null)
                par += processEntry(m.group(3));
            if (m.group(4) != null)
                par += m.group(4);
        }

        par = resubstituteTex(par);

        ListIterator<String[]> it = substituted.listIterator();
        while (it.hasNext()) {
            String[] subst = it.next();
            par = par.replaceAll(subst[1], subst[0]);
        }

        return par;
    }

}
