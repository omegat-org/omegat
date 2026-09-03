/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2010 Alex Buloichik, Aaron Madlon-Kay
               2012 Aaron Madlon-Kay
               2014 Briac Pilpre
               2015 Aaron Madlon-Kay
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

package org.omegat.util.gui;

import java.awt.Color;
import java.awt.Component;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import javax.swing.UIManager;
import javax.swing.text.AttributeSet;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import org.jspecify.annotations.Nullable;
import org.omegat.util.Log;
import org.omegat.util.OStrings;
import org.omegat.util.Preferences;

/**
 * Static attributes for text.
 *
 * @author Alex Buloichik (alex73mail@gmail.com)
 * @author Aaron Madlon-Kay
 * @author Briac Pilpre
 */
public final class Styles {

    private Styles() {
    }

    /**
     * Attribute key under which a text span stores the palette entry its
     * foreground is bound to. Views resolve the entry when they paint, so a
     * palette change takes effect with a plain repaint instead of a document
     * rebuild — the basis for instantaneous colour switching on documents of
     * any size. A dedicated key object cannot collide with string-valued
     * attribute keys, mirroring how {@code StyleConstants} defines its keys.
     */
    public static final Object EDITOR_COLOR_FOREGROUND = new BindingKey("boundForeground");

    /** Background counterpart of {@link #EDITOR_COLOR_FOREGROUND}. */
    public static final Object EDITOR_COLOR_BACKGROUND = new BindingKey("boundBackground");

    private static final class BindingKey {
        private final String name;

        private BindingKey(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "OmegaT." + name;
        }
    }

    /**
     * Apply the current editor base colors (foreground and background) to a
     * component. Consumers reacting to an
     * {@link org.omegat.core.events.IColorsChangedEventListener} event should
     * call this and repaint, rather than re-reading the colors themselves, so
     * a live color change is reflected everywhere without a restart.
     */
    public static void applyColors(Component component) {
        component.setForeground(EditorColor.COLOR_FOREGROUND.getColor());
        component.setBackground(EditorColor.COLOR_BACKGROUND.getColor());
    }

    /**
     * Configurable text style flags of {@link EditorColor} entries.
     */
    public enum TextStyle {
        BOLD, ITALIC, STRIKETHROUGH, UNDERLINE
    }

    /**
     * The configurable application colors.
     * <p>
     * Every entry names the UIManager key under which the installed look and
     * feel provides its default. The default is looked up on every read
     * instead of being captured once at class-load time, so entries stay
     * correct when the theme is applied after this class loads or is switched
     * at runtime. Every entry carries a built-in fallback that mirrors the
     * bundled light color scheme, so a color resolves even under a look
     * and feel that defines no OmegaT keys; the bundled themes default
     * text-like entries to their own base foreground and surface-like
     * entries to their own base background, which renders exactly like the
     * former "inherit" behavior.
     * <p>
     * Entries can carry intrinsic default {@link TextStyle} flags (e.g.
     * struck-through deleted match text); user configuration overrides per
     * flag.
     */
    public enum EditorColor {
        /**
         * Background color.
         * <p>
         * Also used for EditorPane.background
         */
        COLOR_BACKGROUND("TextPane.background", "#ffffff"),
        /**
         * Foreground color.
         */
        COLOR_FOREGROUND("TextPane.foreground", "#000000"),
        /**
         * Active source text background.
         */
        COLOR_ACTIVE_SOURCE("OmegaT.activeSource", "#c0ffc0"),
        /**
         * Active source text foreground.
         */
        COLOR_ACTIVE_SOURCE_FG("OmegaT.activeSourceForeground", "#000000"),
        /**
         * Active target text background.
         */
        COLOR_ACTIVE_TARGET("OmegaT.activeTarget", "#ffffff"),
        /**
         * Active target text foreground.
         */
        COLOR_ACTIVE_TARGET_FG("OmegaT.activeTargetForeground", "#000000"),
        /**
         * Segment marker foreground color.
         */
        COLOR_SEGMENT_MARKER_FG("OmegaT.segmentMarkerForeground", "#000000"),
        /**
         * Segment marker background color.
         */
        COLOR_SEGMENT_MARKER_BG("OmegaT.segmentMarkerBackground", "#ffffff"),
        /**
         * source text background.
         */
        COLOR_SOURCE("OmegaT.source", "#c0ffc0"),
        /**
         * source text foreground.
         */
        COLOR_SOURCE_FG("OmegaT.sourceForeground", "#000000"),
        /**
         * noted segment background.
         */
        COLOR_NOTED("OmegaT.noted", "#c0ffff"),
        /**
         * noted segment foreground.
         */
        COLOR_NOTED_FG("OmegaT.notedForeground", "#000000"),
        /**
         * untranslated segment background.
         */
        COLOR_UNTRANSLATED("OmegaT.untranslated", "#c0c0ff"),
        /**
         * untranslated segment foreground.
         */
        COLOR_UNTRANSLATED_FG("OmegaT.untranslatedForeground", "#000000"),
        /**
         * translated segment background.
         */
        COLOR_TRANSLATED("OmegaT.translated", "#ffff99"),
        /**
         * translated segment text.
         */
        COLOR_TRANSLATED_FG("OmegaT.translatedForeground", "#000000"),
        /**
         * non unique entry text.
         */
        COLOR_NON_UNIQUE("OmegaT.nonUnique", "#666666"),
        /**
         * non unique entry background. Defaults to the theme's subtle
         * alternating hilite, so repeated segments are tinted out of the box.
         */
        COLOR_NON_UNIQUE_BG("OmegaT.nonUniqueBackground", "#f5f5f5"),
        /**
         * Modification information background.
         */
        COLOR_MOD_INFO("OmegaT.modInfo", "#ffffff"),
        /**
         * Modification information text.
         */
        COLOR_MOD_INFO_FG("OmegaT.modInfoForeground", "#000000"),
        /**
         * Tags placeholder color.
         */
        COLOR_PLACEHOLDER("OmegaT.placeholder", "#6b6b6b"),
        /**
         * Flagged text target color.
         */
        COLOR_REMOVETEXT_TARGET("OmegaT.removeTextTarget", "#db0000"),
        /**
         * Non-breakable space character background.
         */
        COLOR_NBSP("OmegaT.nbsp", "#888888"),
        /**
         * White space marker background color.
         */
        COLOR_WHITESPACE("OmegaT.whiteSpace", "#808080"),
        /**
         * Bidirectional control characters background color.
         */
        COLOR_BIDIMARKERS("OmegaT.bidiMarkers", "#c80000"),
        /**
         * Paragraph start delimitation background color.
         */
        COLOR_PARAGRAPH_START("OmegaT.paragraphStart", "#888888"),
        /**
         * The background color of a segment comes from MT memory.
         */
        COLOR_MARK_COMES_FROM_TM_MT("OmegaT.markComesFromTmMt", "#fa8072"),
        /**
         * The background color of a segment comes from ICE memory.
         */
        COLOR_MARK_COMES_FROM_TM_XICE("OmegaT.markComesFromTmXice", "#af76df"),
        /**
         * The background color of a segment comes from 100% memory.
         */
        COLOR_MARK_COMES_FROM_TM_X100PC("OmegaT.markComesFromTmX100pc", "#ff9408"),
        /**
         * The background color of a segment comes from auto memory.
         */
        COLOR_MARK_COMES_FROM_TM_XAUTO("OmegaT.markComesFromTmXauto", "#ffd596"),
        /**
         * The background color of a segment comes from enforced memory.
         */
        COLOR_MARK_COMES_FROM_TM_XENFORCED("OmegaT.markComesFromTmXenforced", "#ffccff"),
        /**
         * Alternative translation highlight color.
         */
        COLOR_MARK_ALT_TRANSLATION("OmegaT.markAltTranslations", "#33ffff"),
        /**
         * Replace background color.
         */
        COLOR_REPLACE("OmegaT.replace", "#0000ff"),
        /**
         * Language checker suggestion highlight color.
         */
        COLOR_LANGUAGE_TOOLS("OmegaT.languageTools", "#0000ff"),
        /**
         * Glossary matches highlight color.
         */
        COLOR_TRANSTIPS("OmegaT.transTips", "#0000ff"),
        /**
         * Spellcheck suggestion highlight color.
         */
        COLOR_SPELLCHECK("OmegaT.spellCheck", "#ff0000"),
        /**
         * Terminology suggestion highlight color.
         */
        COLOR_TERMINOLOGY("OmegaT.terminology", "#b84c00"),
        /**
         * Matches changed words background color.
         */
        COLOR_MATCHES_CHANGED("OmegaT.matchesChanged", "#0000ff"),
        /**
         * Matches unchanged words background color.
         */
        COLOR_MATCHES_UNCHANGED("OmegaT.matchesUnchanged", "#007a00"),
        /**
         * Glossary source text color (used as the foreground of glossary hits).
         */
        COLOR_GLOSSARY_SOURCE("OmegaT.glossarySource", "#000000"),
        /**
         * Glossary target text color (used as the foreground of glossary hits).
         */
        COLOR_GLOSSARY_TARGET("OmegaT.glossaryTarget", "#000000"),
        /**
         * Glossary note text color (used as the foreground of glossary hits).
         */
        COLOR_GLOSSARY_NOTE("OmegaT.glossaryNote", "#000000"),
        /**
         * Matches deleted active text color (struck-through foreground).
         */
        COLOR_MATCHES_DEL_ACTIVE("OmegaT.matchesDelActive", "#ff3399", TextStyle.BOLD,
                TextStyle.STRIKETHROUGH),
        /**
         * Matches deleted inactive text color (struck-through foreground).
         */
        COLOR_MATCHES_DEL_INACTIVE("OmegaT.matchesDelInactive", "#ff0000", TextStyle.STRIKETHROUGH),
        /**
         * Matches inserted active text color (underlined foreground).
         */
        COLOR_MATCHES_INS_ACTIVE("OmegaT.matchesInsActive", "#0000ff", TextStyle.BOLD,
                TextStyle.UNDERLINE),
        /**
         * Matches inserted inactive text color (underlined foreground).
         */
        COLOR_MATCHES_INS_INACTIVE("OmegaT.matchesInsInactive", "#6c6c6c", TextStyle.UNDERLINE),
        /**
         * Hyperlink highlight color.
         */
        COLOR_HYPERLINK("OmegaT.hyperlink", "#0000ff"),
        /**
         * Search found mark highlight color.
         */
        COLOR_SEARCH_FOUND_MARK("OmegaT.searchFoundMark", "#0000ff"),
        /**
         * Search replace mark highlight color.
         */
        COLOR_SEARCH_REPLACE_MARK("OmegaT.searchReplaceMark", "#995c00"),
        /**
         * Notification (steady) color.
         */
        COLOR_NOTIFICATION_MIN("OmegaT.notificationMin", "#fff2d4"),
        /**
         * Notification (flash) color.
         */
        COLOR_NOTIFICATION_MAX("OmegaT.notificationMax", "#ff9900"),
        /**
         * Aligner "accepted" group color.
         */
        COLOR_ALIGNER_ACCEPTED("OmegaT.alignerAccepted", "#15bb45"),
        /**
         * Aligner "needs review" group color.
         */
        COLOR_ALIGNER_NEEDSREVIEW("OmegaT.alignerNeedsReview", "#ff0000"),
        /**
         * Aligner highlight color.
         */
        COLOR_ALIGNER_HIGHLIGHT("OmegaT.alignerHighlight", "#ffff00"),
        /**
         * Aligner table row highlight color.
         */
        COLOR_ALIGNER_TABLE_ROW_HIGHLIGHT("OmegaT.alignerTableRowHighlight", "#c8c8c8"),
        /**
         * Source Files low progress color.
         */
        COLOR_PROJECT_FILES_PROGRESS_LOW("OmegaT.projectFilesProgressLow", "#f0b8b4"),
        /**
         * Source Files high progress color.
         */
        COLOR_PROJECT_FILES_PROGRESS_HIGH("OmegaT.projectFilesProgressHigh", "#b7d7b7"),
        /**
         * Source Files complete progress color.
         */
        COLOR_PROJECT_FILES_PROGRESS_COMPLETE("OmegaT.projectFilesProgressComplete", "#b8ccf0"),
        /**
         * Machine translation selected match highlight.
         */
        COLOR_MACHINETRANSLATE_SELECTED_HIGHLIGHT("OmegaT.machinetranslateSelectedHighlight", "#ffff00");

        private static final String DEFAULT_COLOR = "__DEFAULT__";

        private final String displayName;
        private final String uiManagerKey;
        private final Color fallbackColor;
        private final EnumSet<TextStyle> defaultTextStyle;
        private @Nullable Color color;
        private EnumSet<TextStyle> textStyle = EnumSet.noneOf(TextStyle.class);

        /**
         * A color whose default is provided by the installed look and feel
         * under {@code uiManagerKey}, with {@code fallbackHex} taking over
         * when no theme defines the key. Optional {@code defaultTextStyle}
         * flags: intrinsic marker text style.
         */
        EditorColor(String uiManagerKey, String fallbackHex, TextStyle... defaultTextStyle) {
            this(uiManagerKey, Color.decode(fallbackHex), defaultTextStyle);
        }

        EditorColor(String uiManagerKey, Color fallbackColor, TextStyle... defaultTextStyle) {
            this.displayName = OStrings.getString(name());
            this.uiManagerKey = uiManagerKey;
            this.fallbackColor = fallbackColor;
            this.defaultTextStyle = EnumSet.noneOf(TextStyle.class);
            Collections.addAll(this.defaultTextStyle, defaultTextStyle);
            setColorFromPreference();
            setTextStyleFromPreference();
        }

        private void setColorFromPreference() {
            if (!Preferences.isInitialized()) {
                // Standalone look-and-feel usage (theme preview tools, doc
                // generators) loads this enum before the application
                // initializes preferences; themed defaults still resolve,
                // only user overrides cannot apply.
                return;
            }
            String prefColor = Preferences.getPreferenceDefault(name(), null);
            if (prefColor != null && !DEFAULT_COLOR.equals(prefColor)) {
                try {
                    this.color = Color.decode(prefColor);
                } catch (NumberFormatException e) {
                    Log.logWarningRB("PREFS_COLOR_VALUE_PARSE_ERROR", displayName, prefColor);
                }
            }
        }

        /**
         * The color currently in effect as {@code #rrggbb}.
         */
        public String toHex() {
            Color c = getColor();
            return String.format("#%02x%02x%02x", c.getRed(), c.getGreen(), c.getBlue());
        }

        /**
         * The color currently in effect: the user-configured color if one
         * is set, otherwise {@link #getDefault()}.
         */
        public Color getColor() {
            return color != null ? color : getDefault();
        }

        /**
         * The default for the installed look and feel: the UIManager value
         * under {@link #getUIManagerKey()} when the theme defines the key,
         * otherwise the built-in fallback.
         */
        public Color getDefault() {
            Color themed = UIManager.getColor(uiManagerKey);
            return themed != null ? themed : fallbackColor;
        }

        /**
         * The UIManager key under which themes provide the default for this
         * color. Theme classes should reference this accessor instead of
         * repeating the string literal, so a key typo cannot silently
         * disconnect a theme from the color it means to style.
         */
        public String getUIManagerKey() {
            return uiManagerKey;
        }

        public String getDisplayName() {
            return displayName;
        }

        /**
         * Set the user-configured color. Passing {@code null} — or a color
         * equal to the current default — resets the entry, so it keeps
         * following the (theme-dependent) default from then on.
         */
        public void setColor(@Nullable Color newColor) {
            if (newColor == null || newColor.equals(getDefault())) {
                color = null;
                Preferences.setPreference(name(), DEFAULT_COLOR);
            } else {
                color = newColor;
                Preferences.setPreference(name(), toHex());
            }
        }

        private String textStylePrefKey(TextStyle flag) {
            return name() + "_TEXT_STYLE_" + flag.name();
        }

        private void setTextStyleFromPreference() {
            if (!Preferences.isInitialized()) {
                // Standalone guard as in setColorFromPreference: intrinsic
                // defaults apply, user overrides cannot.
                textStyle = EnumSet.copyOf(defaultTextStyle);
                return;
            }
            textStyle = EnumSet.noneOf(TextStyle.class);
            for (TextStyle flag : TextStyle.values()) {
                // Sentinel or absent key = flag follows intrinsic default
                // (color pattern). existsPreference guard needed:
                // getPreferenceDefault writes absent keys back, would pin
                // defaults into user's prefs file.
                boolean on = defaultTextStyle.contains(flag);
                String key = textStylePrefKey(flag);
                if (Preferences.existsPreference(key)) {
                    String pref = Preferences.getPreferenceDefault(key, "");
                    if (!pref.isEmpty() && !DEFAULT_COLOR.equals(pref)) {
                        on = Boolean.parseBoolean(pref);
                    }
                }
                if (on) {
                    textStyle.add(flag);
                }
            }
        }

        /** Whether flag is currently in effect. */
        public boolean is(TextStyle flag) {
            return textStyle.contains(flag);
        }

        /** Text style flags currently in effect. */
        public Set<TextStyle> getTextStyle() {
            return Collections.unmodifiableSet(textStyle);
        }

        /** Intrinsic default text style flags. */
        public Set<TextStyle> getDefaultTextStyle() {
            return Collections.unmodifiableSet(defaultTextStyle);
        }

        /**
         * Configure the text style rendered for text this colour marks. The
         * flags are additive: they never remove bold/italic that a view
         * option or a marker itself requests.
         */
        public void setTextStyle(Set<TextStyle> newStyle) {
            EnumSet<TextStyle> style = EnumSet.noneOf(TextStyle.class);
            style.addAll(newStyle);
            if (style.equals(textStyle)) {
                return;
            }
            textStyle = style;
            for (TextStyle flag : TextStyle.values()) {
                // Flags matching intrinsic default store sentinel: entry
                // keeps following changed defaults (as setColor).
                boolean on = style.contains(flag);
                Preferences.setPreference(textStylePrefKey(flag),
                        on == defaultTextStyle.contains(flag) ? DEFAULT_COLOR : Boolean.toString(on));
            }
        }

        /**
         * Whether entry's call sites render configured style flags; only
         * such entries expose editable flag cells in colours table.
         */
        public boolean isTextStyleable() {
            return TEXT_STYLEABLE.contains(this);
        }

        // Entries with wired call sites: editor segment states
        // (EditorSettings), match pane attributes (MatchesTextArea). Extend
        // together with call sites.
        private static final Set<EditorColor> TEXT_STYLEABLE = EnumSet.of(
                COLOR_ACTIVE_SOURCE, COLOR_ACTIVE_TARGET,
                COLOR_SOURCE, COLOR_NOTED, COLOR_UNTRANSLATED, COLOR_TRANSLATED,
                COLOR_NON_UNIQUE, COLOR_PLACEHOLDER, COLOR_REMOVETEXT_TARGET,
                COLOR_MATCHES_CHANGED, COLOR_MATCHES_UNCHANGED,
                COLOR_MATCHES_INS_ACTIVE, COLOR_MATCHES_INS_INACTIVE,
                COLOR_MATCHES_DEL_ACTIVE, COLOR_MATCHES_DEL_INACTIVE);
    }

    /**
     * Construct required attributes set.
     * <p>
     * Since we need many attributes combinations, it's not a good idea to have
     * variable to each attribute set. There is no sense to store created
     * attributes in the cache, because calculate hash for cache requires about
     * 2-3 time more than create attributes set from scratch.
     * <p>
     * 1000000 attributes creation requires about 305 ms - it's enough fast.
     */
    public static AttributeSet createAttributeSet(@Nullable Color foregroundColor, @Nullable Color backgroundColor,
                                                  @Nullable Boolean bold, @Nullable Boolean italic) {
        MutableAttributeSet r = new SimpleAttributeSet();
        if (foregroundColor != null) {
            StyleConstants.setForeground(r, foregroundColor);
        }

        if (backgroundColor != null) {
            StyleConstants.setBackground(r, backgroundColor);
        }
        if (bold != null) {
            StyleConstants.setBold(r, bold);
        }
        if (italic != null) {
            StyleConstants.setItalic(r, italic);
        }
        return r;
    }

    public static AttributeSet createAttributeSet(@Nullable Color foregroundColor, @Nullable Color backgroundColor,
                                                  @Nullable Boolean bold, @Nullable Boolean italic,
                                                  @Nullable Boolean strikethrough, @Nullable Boolean underline) {

        MutableAttributeSet r = (MutableAttributeSet) createAttributeSet(foregroundColor, backgroundColor,
                bold, italic);

        if (strikethrough != null) {
            StyleConstants.setStrikeThrough(r, strikethrough);
        }
        if (underline != null) {
            StyleConstants.setUnderline(r, underline);
        }

        return r;
    }

    /**
     * Construct an attribute set whose colors stay bound to the given palette
     * entries. The colors currently in effect are baked in under the plain
     * {@link StyleConstants} keys for consumers that read those directly; the
     * entries themselves ride along under {@link #EDITOR_COLOR_FOREGROUND}
     * and {@link #EDITOR_COLOR_BACKGROUND}, so painting resolves the palette
     * at draw time and a palette change needs no re-attribution.
     */
    public static AttributeSet createBoundAttributeSet(@Nullable EditorColor foreground,
            @Nullable EditorColor background, @Nullable Boolean bold, @Nullable Boolean italic) {
        MutableAttributeSet r = (MutableAttributeSet) createAttributeSet(
                foreground == null ? null : foreground.getColor(),
                background == null ? null : background.getColor(), bold, italic);
        if (foreground != null) {
            r.addAttribute(EDITOR_COLOR_FOREGROUND, foreground);
        }
        if (background != null) {
            r.addAttribute(EDITOR_COLOR_BACKGROUND, background);
        }
        return r;
    }

    /**
     * The color currently in effect for the palette entry the attributes bind
     * their foreground to, or null when the attributes carry no binding.
     */
    public static @Nullable Color resolveBoundForeground(AttributeSet attributes) {
        return resolveBound(attributes, EDITOR_COLOR_FOREGROUND);
    }

    /**
     * The color currently in effect for the palette entry the attributes bind
     * their background to, or null when the attributes carry no binding.
     */
    public static @Nullable Color resolveBoundBackground(AttributeSet attributes) {
        return resolveBound(attributes, EDITOR_COLOR_BACKGROUND);
    }

    private static @Nullable Color resolveBound(AttributeSet attributes, Object key) {
        Object bound = attributes.getAttribute(key);
        return bound instanceof EditorColor ? ((EditorColor) bound).getColor() : null;
    }

    /**
     * Overlay the text style configured for the given colour entry onto an
     * attribute set. Additive: flags that are off leave the base attributes
     * untouched, so view options and marker-specific styling keep working.
     */
    public static AttributeSet overlayTextStyle(@Nullable EditorColor style, AttributeSet base) {
        if (style == null || style.textStyle.isEmpty()) {
            return base;
        }
        MutableAttributeSet r = new SimpleAttributeSet();
        r.addAttributes(base);
        if (style.is(TextStyle.BOLD)) {
            StyleConstants.setBold(r, true);
        }
        if (style.is(TextStyle.ITALIC)) {
            StyleConstants.setItalic(r, true);
        }
        if (style.is(TextStyle.STRIKETHROUGH)) {
            StyleConstants.setStrikeThrough(r, true);
        }
        if (style.is(TextStyle.UNDERLINE)) {
            StyleConstants.setUnderline(r, true);
        }
        return r;
    }

    /**
     * Foreground attribute set of entry: current colour plus configured
     * text style flags.
     */
    public static AttributeSet createStyledAttributeSet(EditorColor foreground) {
        return overlayTextStyle(foreground, createAttributeSet(foreground.getColor(), null, null, null));
    }
}
