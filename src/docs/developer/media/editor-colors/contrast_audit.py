#!/usr/bin/env python3
# OmegaT - Computer Assisted Translation (CAT) tool
#          with fuzzy matching, translation memory, keyword search,
#          glossaries, and translation leveraging into updated projects.
#
# Copyright (C) 2026 OmegaT contributors
#               Home page: https://www.omegat.org/
#               Support center: https://omegat.org/support
#
# This file is part of OmegaT.
#
# OmegaT is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# OmegaT is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with this program.  If not, see <https://www.gnu.org/licenses/>.
"""WCAG 2.2 contrast audit over the effective EditorColor values of a theme.

Input: one CSV from ColorDump.java (see generator header for the invocation).
Pairs audited:
- text rendered in the base foreground on top of every background fill
  (segment state fills, match-origin marks, mod info, active segments):
  WCAG SC 1.4.3, threshold 4.5:1;
- colours used as text foregrounds (match diff, glossary, terminology,
  search marks, hyperlink, placeholder, removed text, mod info) against the
  base background: SC 1.4.3, threshold 4.5:1;
- pen/glyph markers (spell check, language tools, transtips, whitespace,
  bidi, paragraph start) against the base background: SC 1.4.11 non-text,
  threshold 3:1.
Fill-vs-background deltas are reported as informational only: state fills are
deliberately subtle and are being complemented by per-marker text styles
(ADR 2026003 stage 1) rather than forced to 3:1. The nbsp and replace
highlights render as translucent washes (TransparentHighlightPainter), so the
informational section reports the base foreground against their alpha blend
over the base background rather than the raw value.
Known gap: the spell check, language tools and transtips pens double as text
foregrounds in the Issues window detail pane, where they stay below 4.5:1;
tracked separately rather than audited here.
"""
import sys

TEXT = 4.5
NONTEXT = 3.0

# entry -> (kind, threshold); background of the comparison is COLOR_BACKGROUND
# except where noted via (kind, threshold, background-entry).
PAIRS = {
    "COLOR_FOREGROUND": ("text-on-bg", TEXT),
    "COLOR_ACTIVE_SOURCE_FG": ("text", TEXT, "COLOR_ACTIVE_SOURCE"),
    "COLOR_ACTIVE_TARGET_FG": ("text", TEXT, "COLOR_ACTIVE_TARGET"),
    "COLOR_SEGMENT_MARKER_FG": ("text", TEXT, "COLOR_SEGMENT_MARKER_BG"),
    "COLOR_SOURCE_FG": ("text", TEXT, "COLOR_SOURCE"),
    "COLOR_NOTED_FG": ("text", TEXT, "COLOR_NOTED"),
    "COLOR_UNTRANSLATED_FG": ("text", TEXT, "COLOR_UNTRANSLATED"),
    "COLOR_TRANSLATED_FG": ("text", TEXT, "COLOR_TRANSLATED"),
    "COLOR_NON_UNIQUE": ("text", TEXT, "COLOR_NON_UNIQUE_BG"),
    "COLOR_MOD_INFO_FG": ("text", TEXT, "COLOR_MOD_INFO"),
    "COLOR_GLOSSARY_SOURCE": ("text", TEXT),
    "COLOR_GLOSSARY_TARGET": ("text", TEXT),
    "COLOR_GLOSSARY_NOTE": ("text", TEXT),
    "COLOR_MATCHES_CHANGED": ("text", TEXT),
    "COLOR_MATCHES_UNCHANGED": ("text", TEXT),
    "COLOR_MATCHES_INS_ACTIVE": ("text", TEXT),
    "COLOR_MATCHES_INS_INACTIVE": ("text", TEXT),
    "COLOR_MATCHES_DEL_ACTIVE": ("text", TEXT),
    "COLOR_MATCHES_DEL_INACTIVE": ("text", TEXT),
    "COLOR_HYPERLINK": ("text", TEXT),
    "COLOR_SEARCH_FOUND_MARK": ("text", TEXT),
    "COLOR_SEARCH_REPLACE_MARK": ("text", TEXT),
    "COLOR_PLACEHOLDER": ("text", TEXT),
    "COLOR_REMOVETEXT_TARGET": ("text", TEXT),
    "COLOR_TERMINOLOGY": ("text", TEXT),
    "COLOR_SPELLCHECK": ("pen", NONTEXT),
    "COLOR_LANGUAGE_TOOLS": ("pen", NONTEXT),
    "COLOR_TRANSTIPS": ("pen", NONTEXT),
    "COLOR_WHITESPACE": ("pen", NONTEXT),
    "COLOR_BIDIMARKERS": ("pen", NONTEXT),
    "COLOR_PARAGRAPH_START": ("pen", NONTEXT),
}

FILLS = ["COLOR_ACTIVE_SOURCE", "COLOR_ACTIVE_TARGET", "COLOR_SOURCE", "COLOR_NOTED",
         "COLOR_UNTRANSLATED", "COLOR_TRANSLATED", "COLOR_NON_UNIQUE_BG", "COLOR_MOD_INFO",
         "COLOR_MARK_COMES_FROM_TM_MT", "COLOR_MARK_COMES_FROM_TM_XICE",
         "COLOR_MARK_COMES_FROM_TM_X100PC", "COLOR_MARK_COMES_FROM_TM_XAUTO",
         "COLOR_MARK_COMES_FROM_TM_XENFORCED", "COLOR_MARK_ALT_TRANSLATION"]

# translucent highlight washes: rendered as value blended over the base
# background with the painter's alpha (NBSPMarker 0.5, ReplaceMarker 0.4)
WASHES = {"COLOR_NBSP": 0.5, "COLOR_REPLACE": 0.4}

def luminance(hexval):
    def channel(c):
        c = c / 255.0
        return c / 12.92 if c <= 0.04045 else ((c + 0.055) / 1.055) ** 2.4
    r, g, b = (int(hexval[i:i + 2], 16) for i in (0, 2, 4))
    return 0.2126 * channel(r) + 0.7152 * channel(g) + 0.0722 * channel(b)

def ratio(a, b):
    la, lb = luminance(a), luminance(b)
    hi, lo = max(la, lb), min(la, lb)
    return (hi + 0.05) / (lo + 0.05)

def blend(top, base, alpha):
    channels = [int(round(int(top[i:i + 2], 16) * alpha + int(base[i:i + 2], 16) * (1 - alpha)))
                for i in (0, 2, 4)]
    return "%02x%02x%02x" % tuple(channels)

def main():
    values = {}
    with open(sys.argv[1]) as f:
        for line in f:
            if line.strip():
                name, _key, value = line.strip().split(",")
                if not value:
                    print("SKIP %-38s no value in dump" % name)
                    continue
                values[name] = value[:6]
    bg = values["COLOR_BACKGROUND"]
    fg = values["COLOR_FOREGROUND"]
    failures = 0
    for name, spec in PAIRS.items():
        threshold = spec[1]
        against = values[spec[2]] if len(spec) > 2 else bg
        subject = fg if spec[0] == "text-on-bg" else values[name]
        r = ratio(subject, against)
        status = "OK  " if r >= threshold else "FAIL"
        if r < threshold:
            failures += 1
        print("%s %-38s %5.2f:1 (need %.1f) vs #%s" % (status, name, r, threshold, against))
    print("-- informational: base foreground legibility on state fills (need %.1f)" % TEXT)
    for name in FILLS:
        r = ratio(fg, values[name])
        print("%s %-38s %5.2f:1" % ("OK  " if r >= TEXT else "info", name, r))
    print("-- informational: base foreground legibility on translucent washes (need %.1f)" % TEXT)
    for name, alpha in WASHES.items():
        washed = blend(values[name], bg, alpha)
        r = ratio(fg, washed)
        print("%s %-38s %5.2f:1 (blend #%s)" % ("OK  " if r >= TEXT else "info", name, r, washed))
    print("%d failure(s)" % failures)
    return 1 if failures else 0

if __name__ == "__main__":
    sys.exit(main())
