/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2006 Thomas Huriaux
               2008 Martin Fleurke
               2009 Arno Peters
               2011 Didier Briel
               2014 Adiel Mittmann
               2017 Didier Briel
               2023 Hiroshi Miura
               Home page: https://www.omegat.org/
               Support center: https://omegat.org/support

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <https://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters2.latex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.omegat.core.Core;
import org.omegat.filters2.AbstractFilter;
import org.omegat.filters2.Instance;
import org.omegat.util.LinebreakPreservingReader;
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
 * @author Adiel Mittmann
 * @author Hiroshi Miura
 */
public class LatexFilter extends AbstractFilter {

    /**
     * Register plugin into OmegaT.
     */
    public static void loadPlugins() {
        Core.registerFilterClass(LatexFilter.class);
    }

    public static void unloadPlugins() {
    }

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

    @Override
    public void processFile(BufferedReader in, BufferedWriter out, org.omegat.filters2.FilterContext fc)
            throws IOException {
        // BOM (byte order mark) bugfix
        in.mark(1);
        int ch = in.read();
        if (ch != 0xFEFF) {
            in.reset();
        }
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

    private String lineBreak;

    /**
     * Processes a LaTeX document.
     *
     * @param in
     *            Source document
     * @param out
     *            Target document
     */
    private void processLatexFile(BufferedReader in, Writer out) throws IOException {
        try (LinebreakPreservingReader lpin = new LinebreakPreservingReader(in)) {
            StringBuilder par = new StringBuilder();
            String s;
            StringBuilder comment = new StringBuilder();

            List<String> commands = new LinkedList<>();

            /*
             * Possible states: N: beginning of a new line M: middle S: skipping
             * blanks
             */
            String state;
            while ((s = lpin.readLine()) != null) {
                lineBreak = lpin.getLinebreak();
                // String[] c = s.split(""); In Java 8, that line gave a first
                // empty element, so it was replaced with the
                // following lines, and idx below was started at 0 instead of 1
                String[] c;
                if (!s.isEmpty()) {
                    c = s.split("(?!^)");
                } else {
                    c = new String[0];
                }
                state = "N";

                int idx = 0;
                while (idx < c.length) {
                    String cidx = c[idx];
                    int cat = findStringCategory(cidx);

                    if (cat == 0) {
                        /* parse control sequence */
                        StringBuilder cmd = new StringBuilder();
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

                        if (!commands.contains(cmd.toString())) {
                            commands.add(cmd.toString());
                        }
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
                        out.write(lineBreak);
                        out.write(lineBreak);
                        par.setLength(0);
                    }
                    // System.out.println(commands);
                    commands.clear();
                    if (comment.length() > 0) { // If there is a comment, write
                                                // it
                        out.write(comment.toString());
                        out.write(lineBreak);
                        comment.setLength(0);
                    }
                } else if (state.equals("M")) {
                    par.append(" ");
                }
            }

            // output remaining buffers
            if (par.length() > 0) {
                out.write(processParagraph(commands, par.toString()));
            }
        }
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

    private final List<String> oneArgNoText = new LinkedList<>();
    private final List<String> oneArgInlineText = new LinkedList<>();
    private final List<String> oneArgParText = new LinkedList<>();
    private final List<String> parBreakCommand = new LinkedList<>();

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
        oneArgNoText.add("\\documentstyle");

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
        oneArgParText.add("\\section*");
        oneArgParText.add("\\section");

        parBreakCommand.add("\\item");
        parBreakCommand.add("\\newcommand");
        parBreakCommand.add("\\renewcommand");
        parBreakCommand.add("\\maketitle");
        parBreakCommand.add("\\maketitle");
        parBreakCommand.add("\\addcontentsline");
    }

    private String replaceOneArgNoText(LinkedList<String[]> substituted, List<String> commands, String par) {
        int counter = 0;

        for (String command : commands) {
            StringBuilder sb = new StringBuilder();

            if (oneArgNoText.contains(command)) {
                // opt [] arg
                // opt () arg
                String find = (String.format("\\%s\\*?(\\[[^\\]]*\\]|\\([^\\)]*\\))?\\s*\\{[^\\}]*+\\}",
                        command));

                Pattern p = Pattern.compile(find);
                Matcher m = p.matcher(par);
                while (m.find()) {
                    String replace = "<n" + counter + ">";
                    String[] subst = { reHarden(lineBreak + m.group(0)), reHarden(replace) };
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

    private String replaceOneArgInlineText(LinkedList<String[]> substituted, List<String> commands,
            String par) {
        int counter = 0;

        for (String command : commands) {
            StringBuilder sb = new StringBuilder();

            if (oneArgInlineText.contains(command)) {
                String find = String.format("(\\%s\\s*\\{)([^\\}]*+)\\}", command);

                Pattern p = Pattern.compile(find);
                Matcher m = p.matcher(par);
                while (m.find()) {
                    String preReplace = "<i" + counter + ">";
                    String postReplace = "</i" + counter + ">";

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

    private String replaceOneArgParText(LinkedList<String[]> substituted, List<String> commands, String par) {
        int counter = 0;

        for (String command : commands) {
            StringBuilder sb = new StringBuilder();

            if (oneArgParText.contains(command)) {
                String find = String.format("(\\%s\\*?\\s*)\\{([^}]*+)\\}", command);

                Pattern p = Pattern.compile(find);
                Matcher m = p.matcher(par);
                while (m.find()) {
                    String replace = "<p" + counter + ">";
                    String content = "";
                    if (m.group(2) != null) {
                        content = processParagraph(commands, m.group(2));
                    }
                    String[] subst = { reHarden(lineBreak + m.group(1) + "{" + content + "}"), reHarden(replace) };

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

    private String replaceUnknownCommand(LinkedList<String[]> substituted, List<String> commands,
            String par) {
        int counter = 0;

        for (String command : commands) {
            if (command.equals("\\\\") || command.equals("\\{") || command.equals("\\[")
                    || command.equals("\\|")) {
                // continue;
                command = String.format("\\%s", command);
            }

            StringBuilder sb = new StringBuilder();
            String find = String.format("\\%s", command);

            try {
                Pattern p = Pattern.compile(find);
                Matcher m = p.matcher(par);
                while (m.find()) {
                    String replace = "<u" + counter + ">";
                    String[] subst = { reHarden(m.group(0)), reHarden(replace) };
                    substituted.addFirst(subst);
                    m.appendReplacement(sb, replace);
                    counter++;
                }
                m.appendTail(sb);

                par = sb.toString();
            } catch (java.util.regex.PatternSyntaxException e) {
                // TODO: understand the exceptions
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

    private String processParagraph(List<String> commands, String par) {
        LinkedList<String[]> substituted = new LinkedList<>();

        par = substituteUnicode(par);
        par = replaceParBreakCommand(substituted, commands, par);

        par = replaceOneArgNoText(substituted, commands, par);
        par = replaceOneArgInlineText(substituted, commands, par);
        par = replaceOneArgParText(substituted, commands, par);
        par = replaceUnknownCommand(substituted, commands, par);

        String find = ("^((\\s*</?[nipu]\\d+>\\s*)*)" + "(.*?)" + "((\\s*</?[nipu]\\d+>\\s*)*)$");
        Pattern p = Pattern.compile(find);
        Matcher m = p.matcher(par);
        if (m.find()) {
            par = "";
            if (m.group(1) != null) {
                par += m.group(1);
            }
            if (m.group(3) != null) {
                par += processEntry(m.group(3));
            }
            if (m.group(4) != null) {
                par += m.group(4);
            }
        }

        par = resubstituteTex(par);

        for (final String[] subst : substituted) {
            par = par.replaceAll(subst[1], subst[0]);
        }

        return par;
    }

    private String replaceParBreakCommand(LinkedList<String[]> substituted, List<String> commands,
            String par) {
        int counter = 0;
        String tmp = par;

        for (String command : commands) {
            StringBuilder sb = new StringBuilder();

            if (parBreakCommand.contains(command)) {
                String find = String.format(".*(\\%s)", command, command);

                Pattern p = Pattern.compile(find);
                Matcher m = p.matcher(tmp);
                int lastStart = 0;
                while (m.find()) {
                    String replace = "<r" + counter + ">";
                    String content = processParagraph(commands, tmp.substring(0, m.start(1)));
                    String[] subst = { reHarden(content + lineBreak + m.group(1)), reHarden(replace) };
                    substituted.addFirst(subst);
                    m.appendReplacement(sb, replace);
                    counter++;
                }
                m.appendTail(sb);
                tmp = sb.toString();
            }
        }
        return tmp;
    }

}
