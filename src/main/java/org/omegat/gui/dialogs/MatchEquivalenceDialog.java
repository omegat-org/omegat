/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2026 Stephan Pakebusch
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

package org.omegat.gui.dialogs;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.StringSelection;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.Scrollable;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.jspecify.annotations.Nullable;
import org.openide.awt.Mnemonics;

import org.omegat.core.matching.MatchEquivalence;
import org.omegat.util.NumeralValueParser;
import org.omegat.util.OStrings;
import org.omegat.util.gui.StaticUIUtils;

/**
 * Dialog listing the character equivalence classes used by fuzzy matching
 * (feature request #1681). Every class shows a checkbox (all active by
 * default) and its complete character inventory; a shared test area folds two
 * sample texts with the currently checked classes and reports whether they
 * compare as equal. The dialog also hosts the project option to compare
 * numbers by value across the numeral systems.
 *
 * @author stephan.pakebusch at zollsoft.de
 */
@SuppressWarnings("serial")
public final class MatchEquivalenceDialog extends JDialog {

    /**
     * Prefill of the test area: the same mixed-language sentence in the
     * typographic and the plain-keyboard writing, exercising every class
     * (curly and CJK quotes, guillemets with narrow no-break space,
     * apostrophes, dash, no-break space, soft hyphen).
     */
    static final String TEST_SAMPLE_TYPOGRAPHIC = "He said \u201cl\u2019\u00e9t\u00e9\u201d, "
            + "\u00ab\u202foui\u202f\u00bb and \u300cはい\u300d \u2014 \u2019s morgens "
            + "10\u00a0% dis\u00adcount on pages 3\u20134";
    static final String TEST_SAMPLE_PLAIN = "He said \"l'\u00e9t\u00e9\", \" oui \" and \"はい\" - "
            + "'s morgens 10 % discount on pages 3-4";

    /**
     * Prefill pairs of the test area, cycled by the example button: the
     * classic sample plus consolidated multilingual pairs, together covering
     * every character class and the number systems. The pure character pairs
     * come first and fold equal even without the number option; the last
     * pair uses Latin-letter Roman numerals and only compares equal with the
     * Roman sub-option.
     */
    static final String[][] EXAMPLES = {
            { TEST_SAMPLE_TYPOGRAPHIC, TEST_SAMPLE_PLAIN },
            // French spacing, guillemets, German low quotes around Cyrillic,
            // CJK corner brackets, em dash
            { "Il a dit\u00a0: \u00ab\u202fbonjour\u202f\u00bb \u2014 \u201e\u0434\u0430\u201c und \u300cこんにちは\u300d",
                    "Il a dit : \" bonjour \" - \"\u0434\u0430\" und \"こんにちは\"" },
            // soft hyphens, narrow no-break space before the percent sign
            { "co\u00ado\u00adp\u00e9ration 100\u202f% \u2014 \u00abfin\u00bb",
                    "coop\u00e9ration 100 % - \"fin\"" },
            // typographic quotes around fullwidth digits, guillemets around
            // Persian digits
            { "\u201c\uff11\uff12\uff13\uff14\uff15\u201d und \u00ab\u06f1\u06f2\u06f3\u00bb",
                    "\"12345\" und \"123\"" },
            // no-break space, Han numeral vs dedicated Roman code point, en
            // dash inside a word, Arabic-Indic digits
            { "Chapter\u00a0\u5341\u4e8c well\u2013known \u0664\u0662",
                    "Chapter \u216b well-known 42" },
            // soft hyphen inside a word, Thai digits, thin space, vulgar
            // fraction vs decimal
            { "dis\u00adcount \u0e57\u0e57\u0e57 Seite\u2009\u00bd",
                    "discount 777 Seite 0.5" },
            // Latin-letter Roman (needs the sub-option), em dash, Ethiopic ten
            { "MMXXVI \u2014 \u1372", "2026 - 10" },
    };

    private final Map<MatchEquivalence, JCheckBox> checkboxes = new EnumMap<>(MatchEquivalence.class);
    private final JCheckBox matchNumbersCheckBox = new JCheckBox();
    private final JCheckBox matchNumbersRomanCheckBox = new JCheckBox();
    private final JTextField testFieldA = new JTextField(30);
    private final JTextField testFieldB = new JTextField(30);
    private final JLabel testResult = new JLabel(" ");
    private int exampleIndex;
    private @Nullable Result result;
    private @Nullable Set<MatchEquivalence> cachedActive;
    private @Nullable Map<Integer, String> cachedFoldMap;


    /**
     * The two dynamically rendered pairs: long numbers with the grouping and
     * decimal separators of the project's source (text A) and target (text
     * B) locales.
     */
    static List<String[]> localeExamples(@Nullable Locale source, @Nullable Locale target) {
        List<String[]> examples = new ArrayList<>();
        if (source != null && target != null) {
            NumberFormat src = NumberFormat.getIntegerInstance(source);
            NumberFormat trg = NumberFormat.getIntegerInstance(target);
            addWhenComparable(examples, source, target,
                    new String[] { src.format(1234567890L), trg.format(1234567890L) });
            NumberFormat srcDec = NumberFormat.getNumberInstance(source);
            NumberFormat trgDec = NumberFormat.getNumberInstance(target);
            srcDec.setMinimumFractionDigits(2);
            trgDec.setMinimumFractionDigits(2);
            addWhenComparable(examples, source, target,
                    new String[] { srcDec.format(9876543.21), trgDec.format(9876543.21) });
        }
        return examples;
    }

    /**
     * Guards the dynamic pairs: a locale whose number shape the comparator
     * cannot resolve yet must not ship an example that reads "different".
     */
    private static void addWhenComparable(List<String[]> examples, Locale source, Locale target,
            String[] pair) {
        if (numbersCompareEqual(pair[0], pair[1], MatchEquivalence.all(), true, false, source, target)) {
            examples.add(pair);
        }
    }

    /** Every example pair: the shipped seven plus the two locale pairs. */
    static List<String[]> allExamples(@Nullable Locale source, @Nullable Locale target) {
        List<String[]> examples = new ArrayList<>(Arrays.asList(EXAMPLES));
        examples.addAll(localeExamples(source, target));
        return examples;
    }

    /** What the dialog was answered with: the disabled classes and the number options. */
    public static final class Result {
        private final Set<MatchEquivalence> disabled;
        private final boolean matchNumbers;
        private final boolean matchNumbersRoman;

        Result(Set<MatchEquivalence> disabled, boolean matchNumbers, boolean matchNumbersRoman) {
            EnumSet<MatchEquivalence> copy = EnumSet.noneOf(MatchEquivalence.class);
            copy.addAll(disabled);
            this.disabled = copy;
            this.matchNumbers = matchNumbers;
            this.matchNumbersRoman = matchNumbersRoman;
        }

        public Set<MatchEquivalence> getDisabled() {
            return EnumSet.copyOf(disabled);
        }

        public boolean isMatchNumbers() {
            return matchNumbers;
        }

        public boolean isMatchNumbersRoman() {
            return matchNumbersRoman;
        }
    }

    private final @Nullable Locale sourceLocale;
    private final @Nullable Locale targetLocale;

    private MatchEquivalenceDialog(Window parent, Set<MatchEquivalence> disabled, boolean matchNumbers,
            boolean matchNumbersRoman, @Nullable Locale sourceLocale, @Nullable Locale targetLocale) {
        super(parent, OStrings.getString("MATCH_EQUIVALENCE_DIALOG_TITLE"),
                Dialog.ModalityType.APPLICATION_MODAL);
        this.sourceLocale = sourceLocale;
        this.targetLocale = targetLocale;
        StaticUIUtils.setEscapeClosable(this);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel classesPanel = new WidthTrackingPanel();
        classesPanel.setLayout(new BoxLayout(classesPanel, BoxLayout.Y_AXIS));
        classesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));
        JLabel nfcNote = new JLabel(OStrings.getString("MATCH_EQUIVALENCE_NFC_NOTE"));
        nfcNote.setAlignmentX(LEFT_ALIGNMENT);
        classesPanel.add(nfcNote);
        for (MatchEquivalence eq : MatchEquivalence.values()) {
            classesPanel.add(buildClassPanel(eq, disabled));
        }
        addNumberOptions(classesPanel, matchNumbers, matchNumbersRoman);

        JPanel testPanel = new JPanel(new GridBagLayout());
        testPanel.setBorder(BorderFactory.createTitledBorder(
                OStrings.getString("MATCH_EQUIVALENCE_TEST_TITLE")));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.anchor = GridBagConstraints.LINE_START;
        gbc.gridx = 0;
        gbc.gridy = 0;
        testPanel.add(new JLabel(OStrings.getString("MATCH_EQUIVALENCE_TEST_TEXT_A")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        testPanel.add(testFieldA, gbc);
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        testPanel.add(new JLabel(OStrings.getString("MATCH_EQUIVALENCE_TEST_TEXT_B")), gbc);
        gbc.gridx = 1;
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        testPanel.add(testFieldB, gbc);
        gbc.gridx = 2;
        gbc.gridy = 0;
        gbc.weightx = 0;
        gbc.fill = GridBagConstraints.NONE;
        gbc.gridheight = 2;
        JButton exampleButton = new JButton();
        Mnemonics.setLocalizedText(exampleButton, OStrings.getString("MATCH_EQUIVALENCE_EXAMPLE"));
        exampleButton.setName("match_equivalence_example_button");
        List<String[]> examples = allExamples(sourceLocale, targetLocale);
        exampleButton.setToolTipText(
                OStrings.getString("MATCH_EQUIVALENCE_EXAMPLE_TOOLTIP", examples.size()));
        exampleButton.addActionListener(e -> {
            exampleIndex = (exampleIndex + 1) % examples.size();
            testFieldA.setText(examples.get(exampleIndex)[0]);
            testFieldB.setText(examples.get(exampleIndex)[1]);
        });
        testPanel.add(exampleButton, gbc);
        gbc.gridheight = 1;
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        testPanel.add(testResult, gbc);
        DocumentListener testListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updateTestResult();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateTestResult();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updateTestResult();
            }
        };
        testFieldA.setText(TEST_SAMPLE_TYPOGRAPHIC);
        testFieldB.setText(TEST_SAMPLE_PLAIN);
        testFieldA.getDocument().addDocumentListener(testListener);
        testFieldB.getDocument().addDocumentListener(testListener);

        JButton okButton = new JButton();
        Mnemonics.setLocalizedText(okButton, OStrings.getString("BUTTON_OK"));
        okButton.setName("match_equivalence_ok_button");
        okButton.addActionListener(e -> {
            result = new Result(getDisabledFromCheckboxes(), matchNumbersCheckBox.isSelected(),
                    matchNumbersRomanCheckBox.isSelected());
            dispose();
        });
        JButton cancelButton = new JButton();
        Mnemonics.setLocalizedText(cancelButton, OStrings.getString("BUTTON_CANCEL"));
        cancelButton.setName("match_equivalence_cancel_button");
        cancelButton.addActionListener(e -> dispose());
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);
        getRootPane().setDefaultButton(okButton);

        JPanel south = new JPanel();
        south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
        south.add(testPanel);
        south.add(buttonPanel);

        getContentPane().add(new JScrollPane(classesPanel), BorderLayout.CENTER);
        getContentPane().add(south, BorderLayout.SOUTH);
        classesPanel.add(Box.createVerticalGlue());
        updateTestResult();
        pack();
        setSize(680, 490);
        setLocationRelativeTo(parent);
    }

    /**
     * Shows the dialog; returns the new set of disabled classes and the
     * number options, or null when cancelled.
     */
    public static @Nullable Result show(Window parent, Set<MatchEquivalence> disabled,
            boolean matchNumbers, boolean matchNumbersRoman, @Nullable Locale sourceLocale,
            @Nullable Locale targetLocale) {
        MatchEquivalenceDialog dialog = new MatchEquivalenceDialog(parent, disabled, matchNumbers,
                matchNumbersRoman, sourceLocale, targetLocale);
        dialog.setVisible(true);
        return dialog.result;
    }

    private EnumSet<MatchEquivalence> getDisabledFromCheckboxes() {
        EnumSet<MatchEquivalence> disabled = EnumSet.noneOf(MatchEquivalence.class);
        checkboxes.forEach((eq, checkbox) -> {
            if (!checkbox.isSelected()) {
                disabled.add(eq);
            }
        });
        return disabled;
    }

    /** Fold map of the given class set, rebuilt only when the set changes. */
    private Map<Integer, String> foldMapFor(Set<MatchEquivalence> active) {
        Map<Integer, String> foldMap = cachedFoldMap;
        if (foldMap == null || !active.equals(cachedActive)) {
            foldMap = MatchEquivalence.buildFoldMap(active);
            cachedFoldMap = foldMap;
            cachedActive = active.isEmpty() ? EnumSet.noneOf(MatchEquivalence.class)
                    : EnumSet.copyOf(active);
        }
        return foldMap;
    }

    private void updateTestResult() {
        Set<MatchEquivalence> active = EnumSet.complementOf(getDisabledFromCheckboxes());
        boolean equal = numbersCompareEqual(testFieldA.getText(), testFieldB.getText(),
                foldMapFor(active), matchNumbersCheckBox.isSelected(),
                matchNumbersRomanCheckBox.isSelected(), sourceLocale, targetLocale);
        testResult.setText(OStrings.getString(
                equal ? "MATCH_EQUIVALENCE_TEST_EQUAL" : "MATCH_EQUIVALENCE_TEST_DIFFERENT"));
    }

    /**
     * Comparison of the test area: both texts fold with the active classes,
     * then compare token by token; with the numbers option, two tokens that
     * read as numbers compare by value instead of by characters. Mirroring
     * the tokenized matching, runs of plain whitespace compare as one
     * separator.
     */
    static boolean numbersCompareEqual(String a, String b, Set<MatchEquivalence> active,
            boolean numbers, boolean roman, @Nullable Locale sourceLocale,
            @Nullable Locale targetLocale) {
        return numbersCompareEqual(a, b, MatchEquivalence.buildFoldMap(active), numbers, roman,
                sourceLocale, targetLocale);
    }

    static boolean numbersCompareEqual(String a, String b, Map<Integer, String> foldMap,
            boolean numbers, boolean roman, @Nullable Locale sourceLocale,
            @Nullable Locale targetLocale) {
        String[] tokensA = MatchEquivalence.fold(a, foldMap).trim().split("\\s+");
        String[] tokensB = MatchEquivalence.fold(b, foldMap).trim().split("\\s+");
        if (tokensA.length != tokensB.length) {
            return false;
        }
        for (int i = 0; i < tokensA.length; i++) {
            if (tokensA[i].equals(tokensB[i])) {
                continue;
            }
            if (!numbers) {
                return false;
            }
            // Text A reads with the source locale's separators, text B with
            // the target locale's, so "1.234,5" and "1,234.5" compare equal
            // between a German source and an English target. When the whole
            // tokens do not read as numbers, common folded affixes (quotes
            // around a number) come off and the cores compare instead.
            NumeralValueParser.Rational va = NumeralValueParser
                    .parseTokenValueLocalized(tokensA[i], roman, sourceLocale).orElse(null);
            NumeralValueParser.Rational vb = NumeralValueParser
                    .parseTokenValueLocalized(tokensB[i], roman, targetLocale).orElse(null);
            if (va == null || vb == null) {
                String[] cores = trimCommonAffixes(tokensA[i], tokensB[i]);
                va = NumeralValueParser.parseTokenValueLocalized(cores[0], roman, sourceLocale)
                        .orElse(null);
                vb = NumeralValueParser.parseTokenValueLocalized(cores[1], roman, targetLocale)
                        .orElse(null);
            }
            if (va == null || vb == null || va.compareTo(vb) != 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Strips the longest common prefix and suffix from both tokens, so a
     * number wrapped in already-folded punctuation still reaches the value
     * comparison as its bare core.
     */
    private static String[] trimCommonAffixes(String a, String b) {
        int prefix = 0;
        while (prefix < a.length() && prefix < b.length() && a.charAt(prefix) == b.charAt(prefix)) {
            prefix++;
        }
        int endA = a.length();
        int endB = b.length();
        while (endA > prefix && endB > prefix && a.charAt(endA - 1) == b.charAt(endB - 1)) {
            endA--;
            endB--;
        }
        return new String[] { a.substring(prefix, endA), b.substring(prefix, endB) };
    }

    /**
     * Scroll view that always takes the viewport's width, so wrapping text
     * children re-wrap when the dialog is resized; vertical scrolling stays.
     */
    @SuppressWarnings("serial")
    private static final class WidthTrackingPanel extends JPanel implements Scrollable {
        @Override
        public Dimension getPreferredScrollableViewportSize() {
            return getPreferredSize();
        }

        @Override
        public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 16;
        }

        @Override
        public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
            return 64;
        }

        @Override
        public boolean getScrollableTracksViewportWidth() {
            return true;
        }

        @Override
        public boolean getScrollableTracksViewportHeight() {
            return false;
        }
    }

    /**
     * Context menu of a class label or group line: copies the selection (or
     * the whole line) and, as the successor of the removed copy buttons, the
     * complete replacement table of the class.
     */
    private static JPopupMenu replacementsCopyMenu(String[] memberLines, @Nullable JTextArea line) {
        JPopupMenu menu = new JPopupMenu();
        if (line != null) {
            JMenuItem copyLine = new JMenuItem(OStrings.getString("CCP_COPY"));
            copyLine.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                    .setContents(new StringSelection(
                            line.getSelectedText() != null ? line.getSelectedText() : line.getText()),
                            null));
            menu.add(copyLine);
        }
        JMenuItem copyAll = new JMenuItem(
                OStrings.getString("MATCH_EQUIVALENCE_COPY_ALL", memberLines.length));
        copyAll.addActionListener(e -> Toolkit.getDefaultToolkit().getSystemClipboard()
                .setContents(new StringSelection(String.join("\n", memberLines)), null));
        menu.add(copyAll);
        return menu;
    }

    /**
     * Checkbox and the per-group summary lines of one class. The full member
     * inventory lives in the context menus (copy of the replacement table)
     * and in the per-character tooltips of the summary lines, not in a table.
     */
    private JPanel buildClassPanel(MatchEquivalence eq, Set<MatchEquivalence> disabled) {
        JCheckBox checkbox = new JCheckBox(eq.getLocalizedName(), !disabled.contains(eq));
        checkbox.setName("match_equivalence_" + eq.getId() + "_cb");
        checkbox.addActionListener(e -> updateTestResult());
        checkboxes.put(eq, checkbox);

        String[] memberLines = describeMembers(eq);
        // The full replacement table travels through the context menu of the
        // class label and of its group lines, not through a button.
        checkbox.setComponentPopupMenu(replacementsCopyMenu(memberLines, null));

        JPanel classPanel = new JPanel();
        classPanel.setLayout(new BoxLayout(classPanel, BoxLayout.Y_AXIS));
        checkbox.setAlignmentX(LEFT_ALIGNMENT);
        classPanel.add(checkbox);
        for (GroupLine group : describeGroupLines(eq)) {
            JTextArea groupLine = new GroupLineArea(group, eq);
            groupLine.setEditable(false);
            groupLine.setOpaque(false);
            groupLine.setFont(new Font(Font.MONOSPACED, Font.PLAIN, groupLine.getFont().getSize()));
            groupLine.setBorder(BorderFactory.createEmptyBorder(0, 24, 2, 0));
            groupLine.setAlignmentX(LEFT_ALIGNMENT);
            groupLine.setFocusTraversalKeysEnabled(true);
            groupLine.setComponentPopupMenu(replacementsCopyMenu(memberLines, groupLine));
            classPanel.add(groupLine);
        }
        classPanel.setAlignmentX(LEFT_ALIGNMENT);
        classPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        return classPanel;
    }

    /** One summary line with the member behind every space-separated token. */
    static final class GroupLine {
        private final String text;
        private final List<Integer> memberCps;

        GroupLine(String text, List<Integer> memberCps) {
            this.text = text;
            this.memberCps = memberCps;
        }

        String getText() {
            return text;
        }

        List<Integer> getMemberCps() {
            return memberCps;
        }

        /** The member code point of the token containing the text offset, or -1. */
        int memberAt(int offset) {
            if (offset < 0 || offset >= text.length()) {
                return -1;
            }
            int token = 0;
            for (int i = 0; i < offset; i++) {
                if (text.charAt(i) == ' ') {
                    token++;
                }
            }
            return text.charAt(offset) == ' ' || token >= memberCps.size() ? -1 : memberCps.get(token);
        }
    }

    /** Summary line whose characters answer with their own replacement tooltip. */
    @SuppressWarnings("serial")
    private static final class GroupLineArea extends JTextArea {
        private final GroupLine group;
        private final MatchEquivalence eq;

        GroupLineArea(GroupLine group, MatchEquivalence eq) {
            super(group.getText());
            this.group = group;
            this.eq = eq;
            // Long lines (the invisibles list) wrap instead of being cut off
            // by the width-tracking panel; model offsets keep the tooltips
            // correct across wrapped rows.
            setLineWrap(true);
            setWrapStyleWord(true);
            javax.swing.ToolTipManager.sharedInstance().registerComponent(this);
        }

        @Override
        public @Nullable String getToolTipText(java.awt.event.MouseEvent event) {
            int cp = group.memberAt(viewToModel2D(event.getPoint()));
            if (cp < 0) {
                return null;
            }
            String replacement = eq.getMembers().get(cp);
            return replacement == null ? null : describeMember(cp, replacement);
        }
    }

    /**
     * The number options compare values instead of folding character lists,
     * so they carry no inventory and do not take part in the character test
     * area. The Roman sub-option only applies while number matching is on.
     */
    private void addNumberOptions(JPanel classesPanel, boolean matchNumbers, boolean matchNumbersRoman) {
        Mnemonics.setLocalizedText(matchNumbersCheckBox,
                OStrings.getString("MATCH_EQUIVALENCE_NUMBERS"));
        // The explanation lives in the tooltip, wrapped by the html body
        // width, keeping the window itself uncluttered.
        matchNumbersCheckBox.setToolTipText("<html><body style='width: 380px'>"
                + OStrings.getString("MATCH_EQUIVALENCE_NUMBERS_TOOLTIP") + "</body></html>");
        matchNumbersCheckBox.setSelected(matchNumbers);
        matchNumbersCheckBox.setName("match_equivalence_numbers_cb");
        matchNumbersCheckBox.setAlignmentX(LEFT_ALIGNMENT);
        classesPanel.add(matchNumbersCheckBox);

        Mnemonics.setLocalizedText(matchNumbersRomanCheckBox,
                OStrings.getString("MATCH_EQUIVALENCE_NUMBERS_ROMAN"));
        matchNumbersRomanCheckBox.setSelected(matchNumbersRoman);
        matchNumbersRomanCheckBox.setEnabled(matchNumbers);
        matchNumbersRomanCheckBox.setName("match_equivalence_numbers_roman_cb");
        matchNumbersRomanCheckBox.setAlignmentX(LEFT_ALIGNMENT);
        matchNumbersRomanCheckBox.setBorder(BorderFactory.createEmptyBorder(0, 24, 0, 0));
        classesPanel.add(matchNumbersRomanCheckBox);
        matchNumbersCheckBox.addActionListener(e -> {
            matchNumbersRomanCheckBox.setEnabled(matchNumbersCheckBox.isSelected());
            updateTestResult();
        });
        matchNumbersRomanCheckBox.addActionListener(e -> updateTestResult());
    }

    /**
     * One display line per member: glyph and code point, name, then the
     * replacement target as glyph and code point(s) so the direction of the
     * folding stays visible.
     */
    static String[] describeMembers(MatchEquivalence eq) {
        return eq.getMembers().entrySet().stream()
                .map(entry -> describeMember(entry.getKey(), entry.getValue()))
                .toArray(String[]::new);
    }

    /** One member as glyph, code point, name and replacement target. */
    static String describeMember(int cp, String replacement) {
        String glyph = new String(Character.toChars(cp));
        String name = Character.getName(cp);
        if (name == null) {
            name = "?";
        }
        String target = replacement.isEmpty() ? OStrings.getString("MATCH_EQUIVALENCE_REMOVED")
                : replacement + "  " + codePointsOf(replacement);
        return String.format(Locale.ROOT, "%s  U+%04X  %s  →  %s", glyph, cp, name, target);
    }

    /** "U+XXXX"-form of every code point of the given replacement string. */
    private static String codePointsOf(String s) {
        return s.codePoints().mapToObj(cp -> String.format(Locale.ROOT, "U+%04X", cp))
                .collect(Collectors.joining(" "));
    }

    /**
     * One line per group of inter-compatible members, e.g. {@code " “ ” „}
     * and {@code ' ‘ ’} as two lines of the quotes class. Members without a
     * visible glyph (spaces, invisible formatting) show their code point
     * instead, so the space and invisible classes still carry information.
     */
    static List<String> describeGroups(MatchEquivalence eq) {
        return describeGroupLines(eq).stream().map(GroupLine::getText).collect(Collectors.toList());
    }

    /** The group lines with the member behind every token, for the tooltips. */
    static List<GroupLine> describeGroupLines(MatchEquivalence eq) {
        Map<String, List<Integer>> groups = new LinkedHashMap<>();
        eq.getMembers().forEach((cp, replacement) -> groups
                .computeIfAbsent(replacement, k -> new ArrayList<>()).add(cp));
        return groups.values().stream()
                .map(cps -> new GroupLine(
                        cps.stream().map(MatchEquivalenceDialog::visibleForm)
                                .collect(Collectors.joining(" ")),
                        cps))
                .collect(Collectors.toList());
    }

    /** The glyph itself, or the code point for characters with no visible glyph. */
    private static String visibleForm(int cp) {
        if (Character.isWhitespace(cp) || Character.isSpaceChar(cp)
                || Character.getType(cp) == Character.FORMAT) {
            return String.format(Locale.ROOT, "U+%04X", cp);
        }
        return new String(Character.toChars(cp));
    }

}
