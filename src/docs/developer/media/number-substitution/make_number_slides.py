#!/usr/bin/env python3
# *************************************************************************
#  OmegaT - Computer Assisted Translation (CAT) tool
#           with fuzzy matching, translation memory, keyword search,
#           glossaries, and translation leveraging into updated projects.
#
#  Copyright (C) 2026 Stephan Pakebusch
#                Home page: https://www.omegat.org/
#                Support center: https://omegat.org/support
#
#  This file is part of OmegaT.
#
#  OmegaT is free software: you can redistribute it and/or modify
#  it under the terms of the GNU General Public License as published by
#  the Free Software Foundation, either version 3 of the License, or
#  (at your option) any later version.
#
#  OmegaT is distributed in the hope that it will be useful,
#  but WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program.  If not, see <https://www.gnu.org/licenses/>.
# *************************************************************************
"""Generate the two dev-doc slides on fuzzy-match number substitution.

Run from this directory to regenerate both SVGs in place:

    python3 make_number_slides.py

The OmegaT logo paths are read from the project's images/OmegaT.svg, so the
script must see the repository root above it (or OMEGAT_LOGO pointing at that
file).

@author stephan.pakebusch at zollsoft.de
"""
import html
import os
import re
import sys

HERE = os.path.dirname(os.path.abspath(__file__))


def find_logo():
    """The project's images/OmegaT.svg, looked up above this script."""
    env = os.environ.get("OMEGAT_LOGO")
    if env:
        return env
    directory = HERE
    for _ in range(8):
        candidate = os.path.join(directory, "images", "OmegaT.svg")
        if os.path.isfile(candidate):
            return candidate
        parent = os.path.dirname(directory)
        if parent == directory:
            break
        directory = parent
    sys.exit("images/OmegaT.svg not found; set OMEGAT_LOGO to its path")


def load_logo(x, y, size):
    """The project logo file embedded verbatim as a nested svg element.

    Nothing inside images/OmegaT.svg is rewritten - its gradients, filters and
    viewBox travel along - only the XML declaration is dropped (a nested
    element must not carry one) and the root tag gets the position and display
    size it is placed at.
    """
    svg = open(find_logo()).read()
    svg = re.sub(r"^\s*<\?xml[^>]*\?>\s*", "", svg)
    root = re.match(r"<svg\b[^>]*>", svg, re.S)
    if not root:
        sys.exit("images/OmegaT.svg does not start with an svg element")
    tag = root.group()
    tag = re.sub(r'\swidth="[^"]*"', "", tag, count=1)
    tag = re.sub(r'\sheight="[^"]*"', "", tag, count=1)
    tag = tag[: -1].rstrip() + f'\n   x="{x}" y="{y}" width="{size}" height="{size}">'
    return tag + svg[root.end():].rstrip()


LOGO = load_logo(26, 12, 48)

INK = "#1e282c"
GREY = "#5f6b71"
PANEL = "#f4f6f7"
BORDER = "#c8d0d4"
RED = "#ef3b39"
GREEN = "#2e6b4f"
AMBER = "#b35c00"
AMBER_BG = "#fff4e5"
BLUE = "#1f5673"
BLUE_BG = "#eef4f8"

HEADER = """<!--
  {desc}

  **************************************************************************
  OmegaT - Computer Assisted Translation (CAT) tool
           with fuzzy matching, translation memory, keyword search,
           glossaries, and translation leveraging into updated projects.

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
  **************************************************************************
-->"""


def esc(s):
    return html.escape(s, quote=False)


def text(x, y, s, size=12.5, fill=INK, weight="normal", anchor="start", family=None,
         style=""):
    fam = ' font-family="Menlo, Consolas, monospace"' if family == "mono" else ""
    return (f'<text x="{x}" y="{y}" font-size="{size}" fill="{fill}" '
            f'font-weight="{weight}" text-anchor="{anchor}"{fam}'
            f'{(" style=\"" + style + "\"") if style else ""}>{esc(s)}</text>')


def classbox(x, y, w, title, stereotype, members, accent=BLUE, bg="#ffffff",
             bar_bg=BLUE_BG, note=None, mono_from=0):
    """UML-ish class box: title bar, stereotype, member lines."""
    line_h = 17.5
    bar_h = 36 if stereotype else 24
    h = bar_h + 8 + line_h * len(members) + (24 if note else 6)
    out = [f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="6" fill="{bg}" '
           f'stroke="{accent}" stroke-width="1.6"/>',
           f'<path d="M{x} {y + bar_h} H{x + w}" stroke="{accent}" stroke-width="1.2"/>',
           f'<rect x="{x}" y="{y}" width="{w}" height="{bar_h}" rx="6" fill="{bar_bg}"/>',
           f'<path d="M{x} {y + bar_h} H{x + w}" stroke="{accent}" stroke-width="1.2"/>',
           text(x + 12, y + (22 if stereotype else 17), title, 14.5, accent, "bold")]
    if stereotype:
        out.append(text(x + 12, y + 32, stereotype, 10.5, GREY))
    ty = y + bar_h + 20
    for i, m in enumerate(members):
        fam = "mono" if i >= mono_from else None
        col = INK if not m.startswith("//") else GREY
        out.append(text(x + 12, ty, m, 11.8, col, family=fam))
        ty += line_h
    if note:
        out.append(text(x + 12, ty + 6, note, 11, GREY))
    return "\n".join(out), h


def step(x, y, w, n, title, detail, accent=INK, bg=PANEL, h=52):
    out = [f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="8" fill="{bg}" '
           f'stroke="{BORDER}" stroke-width="1.2"/>',
           f'<circle cx="{x + 20}" cy="{y + h / 2}" r="12" fill="{accent}"/>',
           text(x + 20, y + h / 2 + 4.5, str(n), 12.5, "#ffffff", "bold", "middle"),
           text(x + 42, y + 21, title, 13, INK, "bold")]
    if detail:
        out.append(text(x + 42, y + 39, detail, 11.5, GREY, family="mono"))
    return "\n".join(out)


def arrow(x1, y1, x2, y2, color=INK, width=1.4, dash=None, marker="arrow"):
    d = f' stroke-dasharray="{dash}"' if dash else ""
    return (f'<path d="M{x1} {y1} L{x2} {y2}" stroke="{color}" stroke-width="{width}" '
            f'fill="none"{d} marker-end="url(#{marker})"/>')


def callout(x, y, w, label, lines, accent):
    h = 24 + 16 * len(lines)
    out = [f'<rect x="{x}" y="{y}" width="{w}" height="{h}" rx="6" '
           f'fill="{"#fdecec" if accent == RED else "#e9f5ee"}" stroke="{accent}" '
           f'stroke-width="1.2"/>',
           text(x + 10, y + 17, label, 11.5, accent, "bold")]
    ly = y + 33
    for ln in lines:
        out.append(text(x + 10, ly, ln, 11, INK, family="mono"))
        ly += 16
    return "\n".join(out), h


def footer():
    """The two mandated grey licence lines, one text run each."""
    l1 = ("2026 - This file is part of OmegaT, released under the GNU General Public "
          "License version 3 or (at your option) any later version.")
    l2 = ("OmegaT is distributed WITHOUT ANY WARRANTY, without even the implied warranty "
          "of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE - see the GNU General "
          "Public License for more details.")
    return "\n".join([text(32, 676, l1, 11, GREY),
                      text(32, 691, l2, 11, GREY)])


def logo():
    return LOGO


def defs():
    return f'''<defs>
    <marker id="arrow" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7"
            markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="{INK}"/>
    </marker>
    <marker id="arrowAmber" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7"
            markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="{AMBER}"/>
    </marker>
    <marker id="arrowGrey" viewBox="0 0 10 10" refX="9" refY="5" markerWidth="7"
            markerHeight="7" orient="auto-start-reverse">
      <path d="M0 0 L10 5 L0 10 z" fill="{GREY}"/>
    </marker>
  </defs>'''


def slide(desc, title, subtitle, body):
    return f'''{HEADER.format(desc=desc)}
<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 1200 700" width="1200"
     height="700" font-family="Helvetica, Arial, sans-serif">
  {defs()}
  <rect width="1200" height="700" fill="#ffffff"/>
  {logo()}
  {text(90, 40, title, 25, INK, "bold")}
  {text(90, 61, subtitle, 12.5, GREY)}
  <path d="M32 74 H1168" stroke="{BORDER}" stroke-width="1"/>
  {body}
  <path d="M32 655 H1168" stroke="{BORDER}" stroke-width="1"/>
  {footer()}
</svg>
'''


# ---------------------------------------------------------------- slide 1: before
b = []
b.append(text(32, 100, "PARTICIPANTS", 11.5, GREY, "bold"))
b.append(text(528, 100, "FLOW OF ONE NUMBER TOKEN", 11.5, GREY, "bold"))

box, h = classbox(32, 112, 468, "MatchesTextArea", "org.omegat.gui.matches",
                  ["+ substituteNumbers(source, srcMatch, trgMatch) : String",
                   "- isNumber(String) : boolean",
                   "- normalizeDigitWidth(List<String>) : List<String>",
                   "- toDigitWidthOf(number, template) : String",
                   "- mapIndices(List, List) : Map<Integer,Integer>"],
                  accent=BLUE, mono_from=0)
b.append(box)
y2 = 112 + h + 22
box2, h2 = classbox(32, y2, 468, "ITokenizer", "org.omegat.tokenizer",
                    ["+ tokenizeVerbatimToStrings(String) : String[]"],
                    accent=BLUE, mono_from=0,
                    note="word-break tokens, punctuation and spaces included")
b.append(box2)
y3 = y2 + h2 + 22
box3, h3 = classbox(32, y3, 468, "java.lang.Integer / java.lang.Double", "JDK",
                    ["Integer.parseInt(String)", "Double.parseDouble(String)"],
                    accent=GREY, bar_bg="#eef0f1", mono_from=0,
                    note="the whole numeric knowledge of the feature")
b.append(box3)
y4 = y3 + h3 + 18
b.append(f'<rect x="32" y="{y4}" width="468" height="54" rx="6" fill="#fdecec" '
         f'stroke="{RED}" stroke-width="1.2"/>')
b.append(text(46, y4 + 21, "No numeral library involved.", 12.5, RED, "bold"))
b.append(text(46, y4 + 39,
              "Numbers are strings; equality is string equality.", 11.5, INK))
b.append(arrow(266, 112 + h, 266, y3 - 4, GREY, 1.3, "4 3", "arrowGrey"))

sx, sw = 528, 640
steps = [
    ("tokenize all three texts", "sourceMatch / targetMatch / source"),
    ("keep the number tokens", "filter(isNumber)  ->  parseInt, parseDouble"),
    ("normalize digit width", "fullwidth -> halfwidth  (RFE #1193)"),
    ("gate: counts equal, string SETS equal", "else: return targetMatch unchanged, silently"),
    ("pair by string equality", "mapIndices(srcMatch, trgMatch)"),
    ("rebuild target, adopt digit width", "toDigitWidthOf(sourceNumber, targetToken)"),
]
sy = 112
for i, (t, d) in enumerate(steps, 1):
    b.append(step(sx, sy, sw, i, t, d, accent=INK))
    if i < len(steps):
        b.append(arrow(sx + 20, sy + 52, sx + 20, sy + 68, GREY, 1.3, None, "arrowGrey"))
    sy += 68

co, ch = callout(sx, sy + 6, sw, "Consequences",
                 ["IV   XII   十二   ⅙      not numbers at all -> silently left as text",
                  "1000  vs  1.000     sets differ -> nothing is substituted",
                  "٩ -> latin target      inserted verbatim, script of the target ignored",
                  "3-cycle reordering    pairs by the inverse permutation -> wrong numbers"],
                 RED)
b.append(co)

s1 = slide("Fuzzy match number substitution BEFORE the numeral-parser upgrade: "
           "participating classes and the flow of a single number token.",
           "Fuzzy match number substitution - before",
           "org.omegat.gui.matches.MatchesTextArea.substituteNumbers() as shipped: "
           "ASCII decimal numbers, compared as strings",
           "\n  ".join(b))

# ----------------------------------------------------------------- slide 2: after
a = []
a.append(text(32, 100, "PARTICIPANTS", 11.5, GREY, "bold"))
a.append(text(528, 100, "FLOW OF ONE NUMBER TOKEN", 11.5, GREY, "bold"))

box, h = classbox(32, 112, 468, "MatchesTextArea", "org.omegat.gui.matches",
                  ["+ substituteNumbers(source, srcMatch, trgMatch) : String",
                   "- numberValue(String) : Rational",
                   "- renderLike(sourceNumber, targetToken) : String",
                   "- mapIndices(List, List) : Map<Integer,Integer>"],
                  accent=BLUE, mono_from=0)
a.append(box)
y2 = 112 + h + 16
box2, h2 = classbox(32, y2, 468, "FindMatches", "org.omegat.core.statistics - PR #2155",
                    ["- mapNumberTokens(String, boolean) : Token[]"],
                    accent=BLUE, mono_from=0,
                    note="matching side: same rule, so scores and inserts agree")
a.append(box2)
y3 = y2 + h2 + 16
box3, h3 = classbox(32, y3, 468, "NumeralValueParser", "org.omegat.util - OmegaT code",
                    ["+ parseTokenValue(String) : Optional<Rational>",
                     "+ parseTokenWhole(String) : Optional<BigInteger>",
                     "  the caller decides whether Latin-letter Roman counts",
                     "  Rational: exact num/den, no rounding"],
                    accent=GREEN, bar_bg="#e9f5ee", mono_from=0,
                    note="gate: decimal digits, letter numerals, Han; Roman on request")
a.append(box3)
a.append(arrow(150, 112 + h, 150, y3 - 4, GREY, 1.3, None, "arrowGrey"))
a.append(arrow(380, y2 + h2, 380, y3 - 4, GREY, 1.3, None, "arrowGrey"))

y4 = y3 + h3 + 34
a.append(f'<rect x="32" y="{y4}" width="468" height="112" rx="8" fill="{AMBER_BG}" '
         f'stroke="{AMBER}" stroke-width="2.2" stroke-dasharray="7 4"/>')
a.append(text(46, y4 + 22, "com.ibm.icu:icu4j  78.3     (icu4j.jar)", 14, AMBER, "bold"))
a.append(text(46, y4 + 38, "EXTERNAL LIBRARY - NOT OmegaT CODE", 10.5, AMBER, "bold"))
a.append(text(46, y4 + 58, "RuleBasedNumberFormat", 11.8, INK, family="mono"))
a.append(text(212, y4 + 58, "Roman, Han, Ethiopic, Greek, Hebrew ...", 11, GREY))
a.append(text(46, y4 + 74, "Normalizer2", 11.8, INK, family="mono"))
a.append(text(212, y4 + 74, "NFKC folding of single-codepoint numerals", 11, GREY))
a.append(text(46, y4 + 90, "UCharacter", 11.8, INK, family="mono"))
a.append(text(212, y4 + 90, "Unicode numeric values of Nl / No code points", 11, GREY))
a.append(text(46, y4 + 105, "every numeral rule set is maintained upstream by the ICU "
                            "project (Unicode Consortium)", 10.5, AMBER))
a.append(arrow(266, y3 + h3, 266, y4 - 8, AMBER, 2.2, None, "arrowAmber"))
a.append(text(276, y4 - 14, "all numeral parsing is delegated",
              10.5, AMBER, "bold"))

sx, sw = 528, 640
steps = [
    ("tokenize all three texts", "unchanged: ITokenizer.tokenizeVerbatimToStrings"),
    ("value of each token", "numberValue -> NumeralValueParser.parseTokenValue"),
    ("gate: counts equal, value MULTISETS equal", "XII and 12 are the same number now"),
    ("pair by value, target -> source", "total mapping, duplicates included"),
    ("render like the target token", "digit script of the template token"),
]
sy = 112
for i, (t, d) in enumerate(steps, 1):
    acc = GREEN if i in (2, 5) else INK
    a.append(step(sx, sy, sw, i, t, d, accent=acc))
    if i < len(steps):
        a.append(arrow(sx + 20, sy + 52, sx + 20, sy + 68, GREY, 1.3, None, "arrowGrey"))
    sy += 68

a.append(arrow(sx - 6, 112 + 68 + 26, 504, y4 + 30, AMBER, 2.0, "6 4", "arrowAmber"))
a.append(arrow(sx - 6, 112 + 4 * 68 + 26, 504, y4 + 52, AMBER, 2.0, "6 4", "arrowAmber"))

co, ch = callout(sx, sy + 10, sw, "Gained",
                 ["Kapitel 7  ->  Chapter Ⅳ in TM        inserts Chapter 7",
                  "٩ into a latin target                  inserts 9, not ٩",
                  "Ⅻ  十二  1.5  ５  ٩              compared by value",
                  "3-cycle reordering, 1 1 2 vs 1 2 2    paired correctly, no crash"],
                 GREEN)
a.append(co)
a.append(text(sx, sy + 10 + ch + 22,
              "Unchanged on purpose: still all-or-nothing, still under the existing "
              "\"Attempt to replace fuzzy match numbers\" option.", 11, GREY))

s2 = slide("Fuzzy match number substitution AFTER the numeral-parser upgrade: "
           "participating classes, the flow of a single number token, and the "
           "external ICU library the numeral formatters come from.",
           "Fuzzy match number substitution - after",
           "the same entry point, but numbers are values: one gated rule, shared with "
           "the matching side, all numeral systems parsed by ICU",
           "\n  ".join(a))

DEST = os.environ.get("SLIDE_DEST", ".")
for name, content in (("number-substitution-before.svg", s1),
                      ("number-substitution-after.svg", s2)):
    path = os.path.join(DEST, name)
    with open(path, "w") as f:
        f.write(content)
    print("wrote", path, len(content), "bytes")
