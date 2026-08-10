/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2007 Zoltan Bartko
               2011 John Moran
               2012 Alex Buloichik, Jean-Christophe Helary, Didier Briel, Thomas Cordonnier, Aaron Madlon-Kay
               2013 Zoltan Bartko, Aaron Madlon-Kay
               2014 Aaron Madlon-Kay
               2015 Yu Tang
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

package org.omegat.gui.matches;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.Caret;
import javax.swing.text.StyledDocument;

import org.omegat.core.Core;
import org.omegat.core.data.DataUtils;
import org.omegat.core.data.SourceTextEntry;
import org.omegat.core.data.StringData;
import org.omegat.core.data.TMXEntry;
import org.omegat.core.matching.DiffDriver.TextRun;
import org.omegat.core.matching.NearString;
import org.omegat.core.matching.NearString.SORT_KEY;
import org.omegat.core.matching.NearString.ScoresComparator;
import org.omegat.gui.common.EntryInfoThreadPane;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.preferences.PreferencesWindowController;
import org.omegat.gui.preferences.view.TMMatchesPreferencesController;
import org.omegat.gui.shortcuts.PropertiesShortcuts;
import org.omegat.tokenizer.ITokenizer;
import org.omegat.util.NumeralValueParser;
import org.omegat.util.NumeralValueParser.Rational;
import org.omegat.util.OConsts;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;
import org.omegat.util.StringUtil;
import org.omegat.util.Token;
import org.omegat.util.gui.DragTargetOverlay;
import org.omegat.util.gui.DragTargetOverlay.FileDropInfo;
import org.omegat.util.gui.IPaneMenu;
import org.omegat.util.gui.StaticUIUtils;
import org.omegat.util.gui.Styles;
import org.omegat.util.gui.UIThreadsUtil;

/**
 * This is a Match pane, that displays fuzzy matches.
 *
 * @author Keith Godfrey
 * @author Maxym Mykhalchuk
 * @author Zoltan Bartko
 * @author John Moran
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Jean-Christophe Helary
 * @author Didier Briel
 * @author Aaron Madlon-Kay
 * @author Yu Tang
 */
@SuppressWarnings("serial")
public class MatchesTextArea extends EntryInfoThreadPane<List<NearString>> implements IMatcher, IPaneMenu {

    private static final String EXPLANATION = OStrings.getString("GUI_MATCHWINDOW_explanation");

    private static final AttributeSet ATTRIBUTES_EMPTY = Styles.createAttributeSet(null, null, null, null);
    private static final AttributeSet ATTRIBUTES_CHANGED = Styles
            .createAttributeSet(Styles.EditorColor.COLOR_MATCHES_CHANGED.getColor(), null, null, null);
    private static final AttributeSet ATTRIBUTES_UNCHANGED = Styles
            .createAttributeSet(Styles.EditorColor.COLOR_MATCHES_UNCHANGED.getColor(), null, null, null);
    private static final AttributeSet ATTRIBUTES_SELECTED = Styles.createAttributeSet(null, null, true, null);
    private static final AttributeSet ATTRIBUTES_DELETED_ACTIVE = Styles.createAttributeSet(
            Styles.EditorColor.COLOR_MATCHES_DEL_ACTIVE.getColor(), null, true, null, true, null);
    private static final AttributeSet ATTRIBUTES_DELETED_INACTIVE = Styles.createAttributeSet(
            Styles.EditorColor.COLOR_MATCHES_DEL_INACTIVE.getColor(), null, null, null, true, null);
    private static final AttributeSet ATTRIBUTES_INSERTED_ACTIVE = Styles.createAttributeSet(
            Styles.EditorColor.COLOR_MATCHES_INS_ACTIVE.getColor(), null, true, null, null, true);
    private static final AttributeSet ATTRIBUTES_INSERTED_INACTIVE = Styles.createAttributeSet(
            Styles.EditorColor.COLOR_MATCHES_INS_INACTIVE.getColor(), null, null, null, null, true);

    private final DockableScrollPane scrollPane;
    private final List<NearString> matches = new ArrayList<>();

    private final List<Integer> delimiters = new ArrayList<>();
    private final List<Integer> sourcePos = new ArrayList<>();
    private final List<Map<Integer, List<TextRun>>> diffInfos = new ArrayList<>();
    private int activeMatch = -1;

    /** Creates new form MatchGlossaryPane */
    public MatchesTextArea(IMainWindow mw) {
        super(true);
        setName("matches_pane");

        String title = OStrings.getString("GUI_MATCHWINDOW_SUBWINDOWTITLE_Fuzzy_Matches");
        scrollPane = new DockableScrollPane("MATCHES", title, this, true);
        mw.addDockable(scrollPane);

        setEditable(false);
        StaticUIUtils.makeCaretAlwaysVisible(this);
        this.setText(EXPLANATION);
        setMinimumSize(new Dimension(100, 50));

        addMouseListener(mouseListener);
        // per-match score tooltips (#465)
        ToolTipManager.sharedInstance().registerComponent(this);

        DragTargetOverlay.apply(this, new FileDropInfo(false) {
            @Override
            public String getImportDestination() {
                return Core.getProject().getProjectProperties().getTMRoot();
            }

            @Override
            public boolean acceptFile(File pathname) {
                return pathname.getName().toLowerCase(Locale.ENGLISH).endsWith(OConsts.TMX_EXTENSION);
            }

            @Override
            public String getOverlayMessage() {
                return OStrings.getString("DND_ADD_TM_FILE");
            }

            @Override
            public boolean canAcceptDrop() {
                return Core.getProject().isProjectLoaded();
            }

            @Override
            public Component getComponentToOverlay() {
                return scrollPane;
            }
        });
    }

    @Override
    public void onEntryActivated(SourceTextEntry newEntry) {
        scrollPane.stopNotifying();
        super.onEntryActivated(newEntry);
    }

    @Override
    protected void startSearchThread(SourceTextEntry newEntry) {
        new FindMatchesThread(MatchesTextArea.this, Core.getProject(), newEntry).start();
    }

    /**
     * Sets the list of fuzzy matches to show in the pane. Each element of the
     * list should be an instance of {@link NearString}.
     */
    @Override
    protected void setFoundResult(final SourceTextEntry se, List<NearString> newMatches) {
        UIThreadsUtil.mustBeSwingThread();

        List<NearString> oldMatches = new ArrayList<>(matches);

        clear();

        if (newMatches == null) {
            firePropertyChange("matches", oldMatches, newMatches);
            return;
        }

        if (!newMatches.isEmpty() && Preferences.isPreference(Preferences.NOTIFY_FUZZY_MATCHES)) {
            scrollPane.notify(true);
        }

        NearString.SORT_KEY key = Preferences.getPreferenceEnumDefault(Preferences.EXT_TMX_SORT_KEY,
                SORT_KEY.SCORE);
        newMatches.sort(Comparator.comparing(ns -> ns.scores[0], new ScoresComparator(key).reversed()));

        matches.addAll(newMatches);
        delimiters.add(0);
        StringBuilder displayBuffer = new StringBuilder();

        MatchesVarExpansion template = new MatchesVarExpansion(Preferences.getPreferenceDefault(
                Preferences.EXT_TMX_MATCH_TEMPLATE, MatchesVarExpansion.DEFAULT_TEMPLATE));

        for (int i = 0; i < newMatches.size(); i++) {
            NearString match = newMatches.get(i);
            MatchesVarExpansion.Result result = template.apply(match, i + 1);
            displayBuffer.append(result.text);
            sourcePos.add(result.sourcePos);
            diffInfos.add(result.diffInfo);

            if (i < (newMatches.size() - 1)) {
                displayBuffer.append("\n\n");
            }
            delimiters.add(displayBuffer.length());
        }

        setText(displayBuffer.toString());
        setActiveMatch(0);
        firePropertyChange("matches", oldMatches, newMatches);

        checkForReplaceTranslation();
    }

    @Override
    protected void onProjectOpen() {
        clear();
    }

    @Override
    protected void onProjectClose() {
        clear();
        setText(EXPLANATION);
        // We clean the ATTRIBUTE_SELECTED style set by the last displayed match
        StyledDocument doc = (StyledDocument) getDocument();
        doc.setCharacterAttributes(0, doc.getLength(), ATTRIBUTES_EMPTY, true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NearString getActiveMatch() {
        UIThreadsUtil.mustBeSwingThread();

        if (activeMatch < 0 || activeMatch >= matches.size()) {
            return null;
        } else {
            return matches.get(activeMatch);
        }
    }

    /**
     * Attempts to substitute numbers in a match with numbers from the source
     * segment. For substitution to be done, the number of numbers must be the
     * same between source and matches, and the numbers must be the same between
     * the source match and the target match. The order of the numbers can be
     * different between the source match and the target match. Numbers will be
     * substituted at the correct location.
     *
     * @param source
     *            The source segment
     * @param sourceMatch
     *            The source of the match
     * @param targetMatch
     *            The target of the match
     * @return The target match with numbers possibly substituted
     */
    @Override
    public String substituteNumbers(String source, String sourceMatch, String targetMatch) {
        ITokenizer sourceTok = Core.getProject().getSourceTokenizer();
        ITokenizer targetTok = Core.getProject().getTargetTokenizer();
        return substituteNumbers(source, sourceMatch, targetMatch, sourceTok, targetTok);
    }

    static String substituteNumbers(String source, String sourceMatch, String targetMatch,
            ITokenizer sourceTok, ITokenizer targetTok) {

        List<String> sourceMatchNumbers = Stream.of(sourceTok.tokenizeVerbatimToStrings(sourceMatch))
                .filter(MatchesTextArea::isNumber).collect(Collectors.toList());

        String[] targetTokens = targetTok.tokenizeVerbatimToStrings(targetMatch);
        List<String> targetMatchNumbers = Stream.of(targetTokens).filter(MatchesTextArea::isNumber)
                .collect(Collectors.toList());

        List<String> sourceNumbers = Stream.of(sourceTok.tokenizeVerbatimToStrings(source))
                .filter(MatchesTextArea::isNumber).collect(Collectors.toList());

        // Compare and pair the numbers by their value, not by their spelling, so
        // that the same number written differently counts as the same number:
        // decimal digits of any script including full-width ones (feature
        // request #1193) and Han numerals. What counts as one number is the
        // tokenizer's decision, so a percentage or a dotted date, which arrive
        // as a single token without a value, are out of reach here.
        List<Rational> sourceMatchValues = values(sourceMatchNumbers);
        List<Rational> targetMatchValues = values(targetMatchNumbers);

        if (sourceMatchNumbers.size() != sourceNumbers.size()
                || sourceMatchNumbers.size() != targetMatchNumbers.size()
                || !sameNumbers(sourceMatchValues, targetMatchValues)) {
            return targetMatch;
        }

        // Map each target number to the source number belonging there. Asking in
        // that direction keeps the mapping correct for any permutation of the
        // numbers, and complete for repeated numbers.
        Map<Integer, Integer> locationMap = mapIndices(targetMatchValues, sourceMatchValues);

        // Substitute new numbers in the target match. The inserted number adopts
        // the notation of the target token it replaces, so the target match's own
        // convention is preserved.
        StringBuilder result = new StringBuilder();
        int i = 0;
        for (String tok : targetTokens) {
            if (isNumber(tok)) {
                result.append(renderLike(sourceNumbers.get(locationMap.get(i)), tok));
                i++;
            } else {
                result.append(tok);
            }
        }

        return result.toString();
    }

    /** The values of number tokens that {@link #isNumber} accepted. */
    private static List<Rational> values(List<String> numbers) {
        return numbers.stream()
                .map(n -> NumeralValueParser.parseTokenValue(n, ALLOW_ROMAN).orElseThrow())
                .collect(Collectors.toList());
    }

    /**
     * Whether both lists hold the same numbers with the same multiplicities.
     * Comparing as multisets rather than as sets keeps a repeated number from
     * standing in for a missing one, which would leave a target number without a
     * counterpart.
     */
    private static boolean sameNumbers(List<Rational> first, List<Rational> second) {
        Map<Rational, Integer> pending = new HashMap<>();
        first.forEach(value -> pending.merge(value, 1, Integer::sum));
        for (Rational value : second) {
            Integer count = pending.get(value);
            if (count == null) {
                return false;
            }
            if (count == 1) {
                pending.remove(value);
            } else {
                pending.put(value, count - 1);
            }
        }
        return pending.isEmpty();
    }

    /**
     * Render the given source number in the digit script of the target token it
     * replaces, which is what makes a full-width target keep full-width digits
     * (feature request #1193). A target written in a system this code cannot
     * write back, a Tamil numeral for one, receives plain digits instead: the
     * right number in a foreign notation beats the wrong number in the right
     * one.
     */
    private static String renderLike(String number, String template) {
        int zero = digitZeroOf(template);
        if (zero < 0) {
            // The target writes a numeral system, not digits: write the source
            // value in that system, whatever system the source itself uses.
            Optional<NumeralValueParser.Rational> sourceValue = NumeralValueParser.parseTokenValue(number,
                    ALLOW_ROMAN);
            if (sourceValue.isPresent()) {
                if (BigInteger.ONE.equals(sourceValue.get().denominator())) {
                    Optional<String> rendered = NumeralValueParser
                            .renderInSystemOf(sourceValue.get().numerator(), template);
                    if (rendered.isPresent()) {
                        return rendered.get();
                    }
                } else {
                    // A fraction value: a vulgar-fraction target receives the
                    // matching precomposed glyph; a value without a glyph is
                    // spelled out, because the right number in a foreign
                    // notation beats the target's own, wrong one.
                    Optional<String> glyph = NumeralValueParser
                            .renderVulgarFractionOf(sourceValue.get(), template);
                    return glyph.orElseGet(() -> sourceValue.get().numerator() + "/"
                            + sourceValue.get().denominator());
                }
            }
            return StringUtil.normalizeWidth(number);
        }
        if (!hasDecimalDigit(number)) {
            // The source number is written in a non-digit system (a Han
            // numeral, a sign numeral such as cuneiform or Ethiopic) while
            // the target writes digits: spell the whole value out.
            Optional<NumeralValueParser.Rational> value = NumeralValueParser.parseTokenValue(number,
                    ALLOW_ROMAN);
            if (value.isPresent()) {
                if (BigInteger.ONE.equals(value.get().denominator())) {
                    return toDigitScript(value.get().numerator().toString(), zero);
                }
                // A fractional sign value is spelled out as a digit fraction
                // in the target's digit script.
                return toDigitScript(value.get().numerator() + "/" + value.get().denominator(), zero);
            }
        }
        return toDigitScript(number, zero);
    }

    private static boolean hasDecimalDigit(String text) {
        for (int i = 0; i < text.length();) {
            int cp = text.codePointAt(i);
            if (Character.digit(cp, 10) >= 0) {
                return true;
            }
            i += Character.charCount(cp);
        }
        return false;
    }

    /**
     * The code point of digit zero in the script of the template's first decimal
     * digit, or -1 when the template holds no decimal digit.
     */
    private static int digitZeroOf(String template) {
        for (int i = 0; i < template.length();) {
            int cp = template.codePointAt(i);
            int digit = Character.digit(cp, 10);
            if (digit >= 0) {
                return cp - digit;
            }
            i += Character.charCount(cp);
        }
        return -1;
    }

    /** The number with every decimal digit rewritten in the given digit script. */
    private static String toDigitScript(String number, int zero) {
        StringBuilder sb = new StringBuilder(number.length());
        for (int i = 0; i < number.length();) {
            int cp = number.codePointAt(i);
            int digit = Character.digit(cp, 10);
            sb.appendCodePoint(digit < 0 ? cp : zero + digit);
            i += Character.charCount(cp);
        }
        return sb.toString();
    }

    /**
     * Whether a Roman numeral written with Latin letters counts as a number when
     * inserting a match. It does not: "I", "V", "X", "MIX" and "DIV" are ordinary
     * uppercase words at least as often as they are numbers, and here a false
     * positive does not cost a few similarity points, it rewrites the text the
     * translator is handed. Roman numerals written with the dedicated code points
     * are unambiguous and remain numbers.
     */
    private static final boolean ALLOW_ROMAN = false;

    /**
     * Determine whether the given string is a number: a token that may be read
     * as one and has an exact value. Decimal digits of every script, Han
     * numerals, decimals and fractions are recognized; ordinary words are not.
     *
     * @param text
     *            A string
     * @return True if the string represents a number
     */
    private static boolean isNumber(String text) {
        return NumeralValueParser.parseTokenValue(text, ALLOW_ROMAN).isPresent();
    }

    /**
     * Create a mapping of indices of equivalent items in the given list.
     * Handles duplicated items correctly.
     *
     * @param source
     *            Source list
     * @param target
     *            Target list
     * @return Map of indices from source list to target list
     * @throws IllegalArgumentException
     *             If the lists are not the same size
     */
    private static Map<Integer, Integer> mapIndices(List<?> source, List<?> target) {
        if (source.size() != target.size()) {
            throw new IllegalArgumentException("Lists must be the same size");
        }
        Map<Integer, Integer> result = new HashMap<>();

        for (int i = 0; i < source.size(); i++) {
            for (int j = 0; j < target.size(); j++) {
                Object src = source.get(i);
                Object trg = target.get(j);
                if ((src == trg || src.equals(trg)) && !result.values().contains(j)) {
                    result.put(i, j);
                    break;
                }
            }
        }
        return result;
    }

    /**
     * if WORKFLOW_OPTION "Insert best fuzzy match into target field" is set
     *
     * RFE "Option: Insert best match (80%+) into target field"
     *
     * @see <a href="https://sourceforge.net/p/omegat/feature-requests/33/">RFE
     *      #33</a>
     */
    private void checkForReplaceTranslation() {
        if (matches.isEmpty()) {
            return;
        }
        if (Preferences.isPreference(Preferences.BEST_MATCH_INSERT)) {
            int percentage = Preferences.getPreferenceDefault(Preferences.BEST_MATCH_MINIMAL_SIMILARITY,
                    Preferences.BEST_MATCH_MINIMAL_SIMILARITY_DEFAULT);
            NearString thebest = matches.get(0);
            if (thebest.scores[0].score >= percentage) {
                SourceTextEntry currentEntry = Core.getEditor().getCurrentEntry();
                TMXEntry te = Core.getProject().getTranslationInfo(currentEntry);
                if (te != null && !te.isTranslated()) {
                    String prefix = "";

                    if (!Preferences.getPreference(Preferences.BEST_MATCH_EXPLANATORY_TEXT).isEmpty()) {
                        prefix = Preferences.getPreferenceDefault(Preferences.BEST_MATCH_EXPLANATORY_TEXT,
                                OStrings.getString("WF_DEFAULT_PREFIX"));
                    }

                    String translation = thebest.translation;
                    if (Preferences.isPreference(Preferences.CONVERT_NUMBERS)) {
                        translation = substituteNumbers(currentEntry.getSrcText(), thebest.source,
                                thebest.translation);
                    }

                    if (DataUtils.isFromMTMemory(thebest)) {
                        Core.getEditor().replaceEditTextAndMark(prefix + translation, "TM:[tm/mt]");
                    } else {
                        Core.getEditor().replaceEditText(prefix + translation, "TM: auto");
                    }
                }
            }
        }
    }

    /**
     * Sets the index of an active match. It basically highlights the fuzzy
     * match string selected. (numbers start from 0)
     */
    @Override
    public void setActiveMatch(int activeMatch) {
        UIThreadsUtil.mustBeSwingThread();

        if (activeMatch < 0 || activeMatch >= matches.size() || this.activeMatch == activeMatch) {
            return;
        }

        final int oldActiveMatch = this.activeMatch;
        this.activeMatch = activeMatch;

        StyledDocument doc = (StyledDocument) getDocument();
        doc.setCharacterAttributes(0, doc.getLength(), ATTRIBUTES_EMPTY, true);

        int start = delimiters.get(activeMatch);
        int end = delimiters.get(activeMatch + 1);

        NearString match = matches.get(activeMatch);
        // List tokens = match.str.getSrcTokenList();
        ITokenizer tokenizer = Core.getProject().getSourceTokenizer();
        if (tokenizer == null) {
            return;
        }

        // Apply sourceText styling
        if (sourcePos.get(activeMatch) != -1) {
            Token[] tokens = tokenizer.tokenizeVerbatim(match.source);
            // fix for bug 1586397
            byte[] attributes = match.attr;
            for (int i = 0; i < tokens.length; i++) {
                Token token = tokens[i];
                int tokstart = start + sourcePos.get(activeMatch) + token.getOffset();
                int toklength = token.getLength();
                if ((attributes[i] & StringData.UNIQ) != 0) {
                    doc.setCharacterAttributes(tokstart, toklength, ATTRIBUTES_CHANGED, false);
                } else if ((attributes[i] & StringData.PAIR) != 0) {
                    doc.setCharacterAttributes(tokstart, toklength, ATTRIBUTES_UNCHANGED, false);
                }
            }
        }

        // Apply diff styling to ALL diffs, with colors only for activeMatch
        // Iterate through (up to) 5 fuzzy matches
        for (int i = 0; i < diffInfos.size(); i++) {
            Map<Integer, List<TextRun>> diffInfo = diffInfos.get(i);
            // Iterate through each diff variant (${diff}, ${diffReversed}, ...)
            for (Entry<Integer, List<TextRun>> e : diffInfo.entrySet()) {
                int diffPos = e.getKey();
                if (diffPos != -1) {
                    // Iterate through each style chunk (added or deleted)
                    for (TextRun r : e.getValue()) {
                        int tokstart = delimiters.get(i) + diffPos + r.start;
                        switch (r.type) {
                        case DELETE:
                            doc.setCharacterAttributes(tokstart, r.length,
                                    i == activeMatch ? ATTRIBUTES_DELETED_ACTIVE
                                            : ATTRIBUTES_DELETED_INACTIVE,
                                    false);
                            break;
                        case INSERT:
                            doc.setCharacterAttributes(tokstart, r.length,
                                    i == activeMatch ? ATTRIBUTES_INSERTED_ACTIVE
                                            : ATTRIBUTES_INSERTED_INACTIVE,
                                    false);
                            break;
                        case NOCHANGE:
                            // Nothing
                        }
                    }
                }
            }
        }

        doc.setCharacterAttributes(start, end - start, ATTRIBUTES_SELECTED, false);
        setCaretPosition(end - 2); // two newlines
        final int fstart = start;
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setCaretPosition(fstart);
            }
        });

        firePropertyChange("activeMatch", oldActiveMatch, activeMatch);
    }

    /** Clears up the pane. */
    @Override
    public void clear() {
        super.clear();
        activeMatch = -1;
        matches.clear();
        delimiters.clear();
        sourcePos.clear();
        diffInfos.clear();
    }

    /** The index of the match block at the given point, or -1 when there is none. */
    private int matchIndexAt(Point p) {
        if (matches == null || matches.isEmpty()) {
            return -1;
        }
        int matchIndex = -1;
        int mousepos = viewToModel2D(p);
        for (int i = 0; i < delimiters.size() - 1; i++) {
            int start = delimiters.get(i);
            int end = delimiters.get(i + 1);
            if (mousepos >= start && mousepos < end) {
                matchIndex = i;
                break;
            }
        }
        if (matchIndex == -1) {
            matchIndex = delimiters.size() - 1;
        }
        if (matchIndex >= matches.size()) {
            return -1;
        }
        return matchIndex;
    }

    @Override
    public String getToolTipText(MouseEvent event) {
        int index = matchIndexAt(event.getPoint());
        if (index < 0) {
            return null;
        }
        SourceTextEntry entry = currentlyProcessedEntry;
        return MatchScoresTooltip.render(index + 1, matches.get(index),
                entry == null ? null : entry.getSrcText());
    }

    /** Dismiss delay to restore when the mouse leaves the pane. */
    private int savedTooltipDismissDelay;

    protected final transient MouseListener mouseListener = new MouseAdapter() {
        @Override
        public void mouseEntered(MouseEvent e) {
            // Score tooltips carry several lines; keep them up long enough
            // to read, and restore the global delay on exit.
            savedTooltipDismissDelay = ToolTipManager.sharedInstance().getDismissDelay();
            ToolTipManager.sharedInstance().setDismissDelay(60_000);
        }

        @Override
        public void mouseExited(MouseEvent e) {
            ToolTipManager.sharedInstance().setDismissDelay(savedTooltipDismissDelay);
        }

        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() > 1) {
                setActiveMatch(getClickedItem(e.getPoint()));
            }
        }

        @Override
        public void mousePressed(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e.getPoint());
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.isPopupTrigger()) {
                doPopup(e.getPoint());
            }
        }

        private int getClickedItem(Point p) {
            return matchIndexAt(p);
        }

        private void doPopup(Point p) {
            int clickedItem = getClickedItem(p);
            if (clickedItem == -1) {
                return;
            }
            JPopupMenu popup = new JPopupMenu();
            populateContextMenu(popup, clickedItem);
            popup.show(MatchesTextArea.this, p.x, p.y);
        }
    };

    private void populateContextMenu(JPopupMenu popup, final int index) {
        boolean hasMatches = Core.getProject().isProjectLoaded() && index >= 0 && index < matches.size();
        if (hasMatches) {
            NearString m = matches.get(index);
            if (m.projs.length > 1) {
                JMenuItem item = popup.add(OStrings.getString("MATCHES_PROJECTS"));
                item.setEnabled(false);
                for (int i = 0; i < m.projs.length; i++) {
                    String proj = m.projs[i];
                    StringBuilder b = new StringBuilder();
                    if (proj.equals("")) {
                        b.append(OStrings.getString("MATCHES_THIS_PROJECT"));
                    } else {
                        b.append(proj);
                    }
                    b.append(" ");
                    b.append(m.scores[i].toString());
                    JMenuItem pItem = popup.add(b.toString());
                    pItem.setEnabled(false);
                }
                popup.addSeparator();
            }
        }

        JMenuItem item = popup.add(OStrings.getString("MATCHES_INSERT"));
        item.addActionListener(new ActionListener() {
            // the action: insert this match
            @Override
            public void actionPerformed(ActionEvent e) {
                if (StringUtil.isEmpty(getSelectedText())) {
                    setActiveMatch(index);
                }
                Core.getMainWindow().getMainMenu().invokeAction("editInsertTranslationMenuItem", 0);
            }
        });
        item.setEnabled(hasMatches);

        item = popup.add(OStrings.getString("MATCHES_REPLACE"));
        item.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (StringUtil.isEmpty(getSelectedText())) {
                    setActiveMatch(index);
                }
                Core.getMainWindow().getMainMenu().invokeAction("editOverwriteTranslationMenuItem", 0);
            }
        });
        item.setEnabled(hasMatches);

        popup.addSeparator();

        item = popup.add(OStrings.getString("MATCHES_GO_TO_SEGMENT_SOURCE"));
        item.setEnabled(hasMatches);

        if (hasMatches) {
            final NearString ns = matches.get(index);
            String proj = ns.projs[0];

            if (StringUtil.isEmpty(proj)) {
                item.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Core.getEditor().gotoEntry(ns.source, ns.key);
                    }
                });
            } else {
                item.setEnabled(false);
            }
        }
    }

    /**
     * Make the next match active
     */
    @Override
    public void setNextActiveMatch() {
        if (activeMatch < matches.size() - 1) {
            setActiveMatch(activeMatch + 1);
        }
    }

    /**
     * Make the previous match active
     */
    @Override
    public void setPrevActiveMatch() {
        if (activeMatch > 0) {
            setActiveMatch(activeMatch - 1);
        }
    }

    @Override
    public void populatePaneMenu(JPopupMenu menu) {
        populateContextMenu(menu, activeMatch);
        menu.addSeparator();
        final JMenuItem notify = new JCheckBoxMenuItem(
                OStrings.getString("GUI_MATCHWINDOW_SETTINGS_NOTIFICATIONS"));
        notify.setSelected(Preferences.isPreference(Preferences.NOTIFY_FUZZY_MATCHES));
        notify.addActionListener(
                e -> Preferences.setPreference(Preferences.NOTIFY_FUZZY_MATCHES, notify.isSelected()));
        menu.add(notify);
        menu.addSeparator();
        final JMenuItem prefs = new JMenuItem(OStrings.getString("MATCHES_OPEN_PREFERENCES"));
        prefs.addActionListener(e -> new PreferencesWindowController()
                .show(Core.getMainWindow().getApplicationFrame(), TMMatchesPreferencesController.class));
        menu.add(prefs);
    }

    @Override
    protected void processKeyEvent(KeyEvent e) {
        KeyStroke s = KeyStroke.getKeyStrokeForEvent(e);
        if (s.equals(PropertiesShortcuts.getEditorShortcuts().getKeyStroke("editorContextMenu"))) {
            JPopupMenu popup = new JPopupMenu();
            populateContextMenu(popup, activeMatch);
            Caret caret = getCaret();
            Point p = caret == null ? getMousePosition() : caret.getMagicCaretPosition();
            popup.show(this, (int) p.getX(), (int) p.getY());
            e.consume();
        }
        super.processKeyEvent(e);
    }
}
