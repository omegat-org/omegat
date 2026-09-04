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

"""Render the open OmegaT feature requests as a clickable thematic map.

Reads a local cache of the SourceForge feature-request tracker and writes
ticket-map.svg next to this script. The cache directory must contain one
tickets/<num>.json per ticket, each holding the REST representation from
https://sourceforge.net/rest/p/omegat/feature-requests/<num> (ticket fields
plus discussion_thread.posts). Only tickets whose status is open or
open-postponed are drawn.

Usage:
    python3 generate_ticket_map.py <cache-dir>

The map has two storeys. Below the legend sits a miniature mosaic overview
(cluster tiles packed densely into bands, tile height following the ticket
count) whose boxes carry only the ticket number and the badges, followed by
top-12 tables (upvotes, comments, oldest, newest); clicking a
miniature box or table row scrolls to the same box in the full-size map
further down (an embedded two-line script suppresses the navigation, so the
browser history stays untouched). In
the full-size map every box shows the title and a description snippet and
links to the tracker page (opens in a new tab). Both storeys share the same
tooltips. The fill colour encodes the ticket age in one-year steps; badges
show upvotes and comment count. Boxes are grouped into keyword-derived theme
clusters; the first rule that matches the title wins, then the labels, then
the description.
"""

import glob
import json
import os
import re
import sys
from datetime import date
from xml.sax.saxutils import escape, quoteattr

BOX_W, BOX_H = 260, 96
GAP_X, GAP_Y = 14, 12
PER_ROW = 7
MARGIN = 40
CLUSTER_PAD = 14
CANVAS_W = MARGIN * 2 + PER_ROW * (BOX_W + GAP_X) - GAP_X + CLUSTER_PAD * 2

MINI_W, MINI_H = 66, 20
MINI_GAP = 11

# One accent colour per cluster (by CLUSTERS order, last one for Miscellaneous):
# cluster container frames and, in the overview, extra outer frames on tickets
# that also match further clusters.
ACCENTS = ["#1f77b4", "#d62728", "#2ca02c", "#9467bd", "#8c564b", "#e377c2",
           "#17becf", "#bcbd22", "#ff7f0e", "#386cb0", "#a6761d", "#e7298a",
           "#66a61e", "#7570b3", "#d95f02", "#1b9e77", "#666666"]

# Age colour ramp, one step per year of ticket age: anchor stops (year, colour),
# linearly interpolated and clamped at the last stop.
AGE_STOPS = [(0, "9ede73"), (5, "f7e463"), (10, "f5b25e"), (15, "ee8578"), (20, "b784c9")]
AGE_MAX = AGE_STOPS[-1][0]

# First matching rule wins; each rule is tried on the title of every ticket
# first, then on the labels, then on the description.
CLUSTERS = [
    ("Team projects & repositories",
     r"\bteam\b|repositor|\bgit\b|\bsvn\b|\bssh\b|commit|version control"),
    ("Alignment",
     r"\balign"),
    ("Glossaries & terminology",
     r"glossar|terminolog|\btbx\b|thesaurus|tematres|taas"),
    ("Dictionaries",
     r"dictionar|stardict|epwing|lingvo"),
    ("Machine translation",
     r"machine translat|\bmt\b|google translate|deepl|apertium|mymemory|yandex|microsoft translator"),
    ("Translation memory & matches",
     r"translation memor|\btmx?\b|fuzzy|match|leverag|enforce|penalt|alternative translation|auto.?populat"),
    ("Spelling, tags & QA",
     r"spell|languagetool|language checker|\bqa\b|quality|issue|validat|\btag\b|tags\b|typograph"),
    ("Statistics & counting",
     r"statist|word count|character count|\bcount\b|progress|report"),
    ("Search & replace",
     r"search|\bfind\b|replace|concordance|regex|regular expression"),
    ("File filters & formats",
     r"filter|xliff|\bpo\b|\bdocx?\b|xlsx|excel|\bodt\b|\bods\b|openoffice|libreoffice|markdown|html|\bxml\b|json|yaml|properties|resx|sdlxliff|\bttx\b|idml|indesign|framemaker|\bmif\b|latex|\btex\b|\bsrt\b|subtitle|\bcsv\b|\bdtd\b|invantive|ddoc|okapi|\brc\b|format"),
    ("Editor & segmentation",
     r"editor|segment|cursor|caret|typing|autocomplet|auto.?complet|autotext|insert|overwrite|undo|paragraph|whitespace|line break|capitali[sz]"),
    ("Scripting, plugins & automation",
     r"script|plugin|\bapi\b|command.?line|\bcli\b|console|automat|jython|groovy|batch"),
    ("Project handling & files",
     r"project propert|project file|file list|source file|target file|folder|directory|omegat\.project|\bsave\b|backup|archive"),
    ("UI, windows & usability",
     r"window|pane|dock|font|colou?r|theme|toolbar|menu|shortcut|keyboard|layout|icon|\bui\b|user interface|display|view|zoom|notification|accessib|voiceover"),
    ("Documentation & localisation",
     r"documentation|manual|tutorial|localis|localiz|l10n|website"),
    ("Installation & platform",
     r"install|\bmac\b|macos|windows|linux|\bjava\b|\bjre\b|update|launcher|startup|notari|signed"),
]
FALLBACK = "Miscellaneous"


def age_colour(years):
    y = min(int(years), AGE_MAX)
    for (y0, c0), (y1, c1) in zip(AGE_STOPS, AGE_STOPS[1:]):
        if y <= y1:
            t = (y - y0) / (y1 - y0)
            rgb = [round(int(c0[i:i + 2], 16) * (1 - t) + int(c1[i:i + 2], 16) * t)
                   for i in (0, 2, 4)]
            return "#%02x%02x%02x" % tuple(rgb)
    return "#" + AGE_STOPS[-1][1]


def darken(colour, factor=0.62):
    return "#%02x%02x%02x" % tuple(round(int(colour[i:i + 2], 16) * factor) for i in (1, 3, 5))


def wrap(text, chars, max_lines):
    words, lines, cur = text.split(), [], ""
    for w in words:
        cand = (cur + " " + w).strip()
        if len(cand) <= chars:
            cur = cand
            continue
        lines.append(cur)
        cur = w
        if len(lines) == max_lines:
            break
    if cur and len(lines) < max_lines:
        lines.append(cur)
    if words and len(" ".join(lines)) < len(" ".join(words)):
        lines[-1] = lines[-1][: chars - 1].rstrip() + "…"
    return lines


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
    tag = tag[:-1].rstrip() + f'\n   x="{x}" y="{y}" width="{size}" height="{size}">'
    return tag + svg[root.end():].rstrip()


def clean_snippet(markdown_text):
    t = re.sub(r"~~~+.*?~~~+|```.*?```", " ", markdown_text, flags=re.S)
    t = re.sub(r"https?://\S+", "", t)
    t = re.sub(r"[\\*_`>#\[\]()|]", " ", t)
    return " ".join(t.split())


def classify(ticket):
    """Return (primary cluster, other matching clusters in rule order)."""
    title = ticket["summary"].lower()
    labels = " ".join(ticket.get("labels", [])).lower()
    desc = (ticket.get("description") or "").lower()[:600]
    primary = None
    for source in (title, labels, desc):
        for name, pattern in CLUSTERS:
            if source and re.search(pattern, source):
                primary = name
                break
        if primary:
            break
    everything = f"{title} {labels} {desc}"
    others = [name for name, pattern in CLUSTERS
              if name != primary and re.search(pattern, everything)]
    return primary or FALLBACK, others


def badge_at(x, y, text, fill, stroke, height, font, per_char):
    w = 8 + per_char * len(text)
    return (f'<rect x="{x}" y="{y}" width="{w}" height="{height}" rx="{height / 2}" '
            f'fill="{fill}" stroke="{stroke}" stroke-width="0.6"/>'
            f'<text x="{x + w / 2}" y="{y + height * 0.75}" font-size="{font}" '
            f'text-anchor="middle" fill="#1a1a1a">{escape(text)}</text>', w)


def badges(t, right, top, height, font, per_char):
    parts, x = [], right
    if t["comments"]:
        w = 8 + per_char * len(f'💬{t["comments"]}')
        b, _ = badge_at(x - w, top, f'💬{t["comments"]}', "#eceff1", "#90a4ae", height, font, per_char)
        parts.append(b)
        x -= w + 3
    if t.get("votes_up"):
        w = 8 + per_char * len(f'▲{t["votes_up"]}')
        b, _ = badge_at(x - w, top, f'▲{t["votes_up"]}', "#bbdefb", "#5d99c6", height, font, per_char)
        parts.append(b)
    return "".join(parts)


def badges_left(t, left, top, height, font, per_char):
    """Upvote and comment pills drawn left to right; returns (svg, width used)."""
    parts, x = [], left
    if t.get("votes_up"):
        b, w = badge_at(x, top, f'▲{t["votes_up"]}', "#bbdefb", "#5d99c6", height, font, per_char)
        parts.append(b)
        x += w + 3
    if t["comments"]:
        b, w = badge_at(x, top, f'💬{t["comments"]}', "#eceff1", "#90a4ae", height, font, per_char)
        parts.append(b)
        x += w + 3
    return "".join(parts), x - left


def main():
    if len(sys.argv) != 2 or not os.path.isdir(os.path.join(sys.argv[1], "tickets")):
        sys.exit("usage: generate_ticket_map.py <cache-dir with tickets/<num>.json>")
    tickets, newest = [], date(1970, 1, 1)
    for path in glob.glob(os.path.join(sys.argv[1], "tickets", "*.json")):
        with open(path) as fh:
            t = json.load(fh)["ticket"]
        if t["status"] not in ("open", "open-postponed"):
            continue
        t["created"] = date.fromisoformat(t["created_date"][:10])
        t["comments"] = len(t.get("discussion_thread", {}).get("posts", []))
        newest = max(newest, date.fromisoformat((t.get("mod_date") or t["created_date"])[:10]))
        tickets.append(t)
    today = newest
    clusters = {}
    for t in sorted(tickets, key=lambda t: -t["ticket_num"]):
        primary, others = classify(t)
        t["others"] = others
        t["years"] = (today - t["created"]).days / 365.25
        t["fill"] = age_colour(t["years"])
        snippet = clean_snippet(t.get("description") or "")
        t["snippet"] = snippet
        cats = " ".join(f"[{c}]" for c in [primary] + others)
        t["tooltip"] = f'#{t["ticket_num"]} ({t["created"]}) {t["summary"]}\n{cats}\n{snippet[:400]}'
        clusters.setdefault(primary, []).append(t)
    ordered = sorted(clusters.items(), key=lambda kv: (kv[0] == FALLBACK, -len(kv[1])))
    accent = {name: ACCENTS[min(i, len(ACCENTS) - 1)]
              for i, (name, _p) in enumerate(CLUSTERS)}
    accent[FALLBACK] = ACCENTS[-1]

    def sums(members):
        return {"comments": sum(t["comments"] for t in members),
                "votes_up": sum(t.get("votes_up") or 0 for t in members)}

    # Miniature overview as a densely packed mosaic: every cluster tile is one
    # band wide (a fixed number of boxes per row) and exactly as tall as its
    # grid needs; tiles land on the currently shortest of four bands, largest
    # cluster first. Tile area therefore only approximates the ticket share —
    # in exchange the overview has hardly any holes.
    mini, views, y = [], [], 168
    mini.append(f'<text x="{MARGIN}" y="{y - 10}" font-size="17" font-weight="bold" '
                f'fill="#222222">Overview — click a box to zoom to its full entry</text>')
    pitch_x, pitch_y = MINI_W + MINI_GAP, MINI_H + MINI_GAP
    BANDS, BAND_COLS = 4, 6
    band_w = 20 + BAND_COLS * pitch_x - MINI_GAP
    band_gap = (CANVAS_W - 2 * MARGIN - BANDS * band_w) / (BANDS - 1)
    cursors = [y] * BANDS
    for name, members in ordered:
        rows = (len(members) + BAND_COLS - 1) // BAND_COLS
        tile_h = 26 + rows * pitch_y - MINI_GAP + 10
        band = min(range(BANDS), key=lambda b: cursors[b])
        rx = round(MARGIN + band * (band_w + band_gap), 1)
        ry = cursors[band]
        cursors[band] = ry + tile_h + 12
        total = sums(members)
        mini.append(f'<g><title>{escape(name)} — {len(members)} tickets, '
                    f'{total["votes_up"]} upvotes, {total["comments"]} comments</title>')
        mini.append(f'<rect x="{rx}" y="{ry}" width="{band_w}" height="{tile_h}" '
                    f'rx="7" fill="#f7f7f4" stroke="{accent[name]}" stroke-width="1.6"/>')
        mini.append(f'<rect x="{rx + 7}" y="{ry + 6}" width="10" height="10" rx="2" '
                    f'fill="{accent[name]}"/>')
        hb, hw = badges_left(total, rx + 22, ry + 5, 12, 7.5, 5.2)
        mini.append(hb)
        label = f'{name} ({len(members)})'
        avail = int((band_w - 22 - hw - 10) / 6.4)
        if len(label) > avail:
            label = label[: max(1, avail - 1)].rstrip() + "…"
        mini.append(f'<text x="{rx + 24 + hw}" y="{ry + 15}" font-size="11.5" '
                    f'font-weight="bold" fill="#444444">{escape(label)}</text></g>')
        for i, t in enumerate(members):
            bx = round(rx + 10 + (i % BAND_COLS) * pitch_x, 1)
            by = round(ry + 26 + (i // BAND_COLS) * pitch_y, 1)
            rings = "".join(
                f'<rect x="{bx - 2 - 2 * k}" y="{by - 2 - 2 * k}" width="{MINI_W + 4 + 4 * k}" '
                f'height="{MINI_H + 4 + 4 * k}" rx="{5 + 2 * k}" fill="none" '
                f'stroke="{accent[other]}" stroke-width="1.3"/>'
                for k, other in enumerate(t["others"][:2]))
            mini.append(
                f'<a href="#t{t["ticket_num"]}"><g><title>{escape(t["tooltip"])}</title>'
                f'<rect x="{bx}" y="{by}" width="{MINI_W}" height="{MINI_H}" rx="4" '
                f'fill="{t["fill"]}" stroke="{darken(t["fill"])}" stroke-width="0.9"/>' + rings
                + f'<text x="{bx + 4}" y="{by + 14}" font-size="8.5" font-weight="bold" '
                f'fill="#222222">#{t["ticket_num"]}</text>'
                + badges(t, bx + MINI_W - 3, by + 4, 12, 7.5, 5.2) + '</g></a>')
    y = round(max(cursors) + 14, 1)

    # Top-12 tables between the two storeys; rows jump to the full-size boxes.
    top_tables = [
        ("Top 12 by upvotes",
         sorted(tickets, key=lambda t: (-(t.get("votes_up") or 0), -t["ticket_num"]))[:12],
         lambda t: f'▲ {t.get("votes_up") or 0}'),
        ("Top 12 by comments",
         sorted(tickets, key=lambda t: (-t["comments"], -t["ticket_num"]))[:12],
         lambda t: f'💬 {t["comments"]}'),
        ("Top 12 oldest",
         sorted(tickets, key=lambda t: (t["created"], t["ticket_num"]))[:12],
         lambda t: str(t["created"])),
        ("Top 12 newest",
         sorted(tickets, key=lambda t: (t["created"], t["ticket_num"]), reverse=True)[:12],
         lambda t: str(t["created"])),
    ]
    row_h = 21
    tbl_h = 32 + 12 * row_h + 6
    for k, (title, top, value) in enumerate(top_tables):
        rx = round(MARGIN + k * (band_w + band_gap), 1)
        mini.append(f'<rect x="{rx}" y="{y}" width="{band_w}" height="{tbl_h}" rx="7" '
                    f'fill="#f7f7f4" stroke="#c8c8c2"/>')
        mini.append(f'<text x="{rx + 10}" y="{y + 20}" font-size="13.5" font-weight="bold" '
                    f'fill="#333333">{escape(title)}</text>')
        for j, t in enumerate(top):
            ty = y + 30 + j * row_h
            label = t["summary"]
            if len(label) > 62:
                label = label[:61].rstrip() + "…"
            mini.append(
                f'<a href="#t{t["ticket_num"]}"><g><title>{escape(t["tooltip"])}</title>'
                f'<rect x="{rx + 6}" y="{ty}" width="{band_w - 12}" height="{row_h - 3}" rx="4" '
                f'fill="{t["fill"]}" stroke="{darken(t["fill"])}" stroke-width="0.6"/>'
                f'<text x="{rx + 12}" y="{ty + 13}" font-size="9.5" font-weight="bold" '
                f'fill="#222222">#{t["ticket_num"]}</text>'
                f'<text x="{rx + 52}" y="{ty + 13}" font-size="9" fill="#222222">{escape(value(t))}</text>'
                f'<text x="{rx + 112}" y="{ty + 13}" font-size="9" fill="#333333">{escape(label)}</text>'
                f'</g></a>')
    y = round(y + tbl_h + 20, 1)

    # Full-size map.
    body = []
    y += 18
    body.append(f'<text x="{MARGIN}" y="{y + 4}" font-size="17" font-weight="bold" '
                f'fill="#222222">Full map — click a box to open the ticket on SourceForge</text>')
    y += 16
    for name, members in ordered:
        rows = (len(members) + PER_ROW - 1) // PER_ROW
        c_h = 46 + rows * (BOX_H + GAP_Y) - GAP_Y + CLUSTER_PAD
        body.append(f'<rect x="{MARGIN}" y="{y}" width="{CANVAS_W - 2 * MARGIN}" height="{c_h}" '
                    f'rx="10" fill="#f7f7f4" stroke="{accent[name]}" stroke-width="2"/>')
        body.append(f'<rect x="{MARGIN + CLUSTER_PAD}" y="{y + 14}" width="16" height="16" rx="3" '
                    f'fill="{accent[name]}"/>')
        hb, hw = badges_left(sums(members), MARGIN + CLUSTER_PAD + 24, y + 14, 16, 10.5, 7.4)
        body.append(hb)
        body.append(f'<text x="{MARGIN + CLUSTER_PAD + 28 + hw}" y="{y + 30}" font-size="21" '
                    f'font-weight="bold" fill="#222222">{escape(name)}'
                    f'<tspan font-weight="normal" fill="#666666" font-size="16">  ({len(members)})</tspan></text>')
        for i, t in enumerate(members):
            bx = MARGIN + CLUSTER_PAD + (i % PER_ROW) * (BOX_W + GAP_X)
            by = y + 46 + (i // PER_ROW) * (BOX_H + GAP_Y)
            url = f'https://sourceforge.net/p/omegat/feature-requests/{t["ticket_num"]}/'
            box = [f'<a href={quoteattr(url)} target="_blank">',
                   f'<g id="t{t["ticket_num"]}"><title>{escape(t["tooltip"])}</title>',
                   f'<rect x="{bx}" y="{by}" width="{BOX_W}" height="{BOX_H}" rx="6" '
                   f'fill="{t["fill"]}" stroke="{darken(t["fill"])}" stroke-width="1.2"/>',
                   f'<text x="{bx + 8}" y="{by + 15}" font-size="10.5" font-weight="bold" '
                   f'fill="#333333">#{t["ticket_num"]}</text>',
                   badges(t, bx + BOX_W - 6, by + 4, 14, 9.5, 7)]
            for j, line in enumerate(wrap(t["summary"], 40, 2)):
                box.append(f'<text x="{bx + 8}" y="{by + 31 + j * 13}" font-size="11" '
                           f'font-weight="bold" fill="#111111">{escape(line)}</text>')
            for j, line in enumerate(wrap(t["snippet"], 52, 3)):
                box.append(f'<text x="{bx + 8}" y="{by + 61 + j * 11}" font-size="8.5" '
                           f'fill="#333333">{escape(line)}</text>')
            box.append('</g></a>')
            body.append("".join(box))
        y += c_h + 26

    legend = [load_logo(MARGIN, 12, 48),
              f'<text x="{MARGIN + 62}" y="52" font-size="30" font-weight="bold" fill="#222222">'
              f'OmegaT — open feature requests, clustered by theme</text>',
              f'<text x="{MARGIN}" y="80" font-size="14" fill="#555555">{len(tickets)} open tickets, '
              f'data as of {today}. Box colour = ticket age in one-year steps. Hover for the full '
              f'title and summary. Clicking an overview or table entry scrolls to the full-size box '
              f'further down without touching the browser history.</text>',
              f'<text x="{MARGIN}" y="99" font-size="14" fill="#555555">'
              f'Each cluster has an accent colour; extra outer frames on an overview box mean the '
              f'ticket also matches those clusters. Cluster headers sum the badges of their tickets.</text>']
    lx, ly = MARGIN, 116
    legend.append(f'<text x="{lx}" y="{ly + 11}" font-size="12" fill="#333333">age in years:</text>')
    lx += 82
    for yy in range(AGE_MAX + 1):
        c = age_colour(yy)
        legend.append(f'<rect x="{lx}" y="{ly}" width="17" height="14" fill="{c}" stroke="{darken(c)}" '
                      f'stroke-width="0.5"/>')
        if yy % 5 == 0:
            legend.append(f'<text x="{lx + 8.5}" y="{ly + 28}" font-size="11" text-anchor="middle" '
                          f'fill="#333333">{yy}{"+" if yy == AGE_MAX else ""}</text>')
        lx += 17
    lx += 30
    legend.append(f'<rect x="{lx}" y="{ly}" width="34" height="14" rx="7" fill="#bbdefb" stroke="#5d99c6"/>'
                  f'<text x="{lx + 17}" y="{ly + 11}" font-size="9.5" text-anchor="middle">▲ 3</text>'
                  f'<text x="{lx + 40}" y="{ly + 11.5}" font-size="12" fill="#333333">upvotes</text>')
    lx += 105
    legend.append(f'<rect x="{lx}" y="{ly}" width="34" height="14" rx="7" fill="#eceff1" stroke="#90a4ae"/>'
                  f'<text x="{lx + 17}" y="{ly + 11}" font-size="9.5" text-anchor="middle">💬 5</text>'
                  f'<text x="{lx + 40}" y="{ly + 11.5}" font-size="12" fill="#333333">comments</text>')

    # Internal jumps scroll within the document instead of navigating, so they
    # leave the browser history alone; external ticket links keep the default.
    script = ('<script><![CDATA[\n'
              'document.documentElement.addEventListener("click", function (e) {\n'
              '  var a = e.target.closest ? e.target.closest("a") : null;\n'
              '  if (!a) return;\n'
              '  var href = a.getAttribute("href") || "";\n'
              '  if (href.charAt(0) !== "#") return;\n'
              '  e.preventDefault();\n'
              '  var target = document.getElementById(href.slice(1));\n'
              '  if (target) target.scrollIntoView({block: "center", inline: "center"});\n'
              '});\n'
              ']]></script>')
    height = y + 52
    footer = (f'<text x="{MARGIN}" y="{height - 29}" font-size="11" fill="#777777">Generated by '
              f'generate_ticket_map.py from the SourceForge tracker '
              f'(https://sourceforge.net/p/omegat/feature-requests/). This file is part of OmegaT, '
              f'released under the GNU General Public License version 3 or (at your option) any '
              f'later version.</text>'
              f'<text x="{MARGIN}" y="{height - 14}" font-size="11" fill="#777777">OmegaT is '
              f'distributed WITHOUT ANY WARRANTY, without even the implied warranty of '
              f'MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE - see the GNU General Public '
              f'License for more details.</text>')
    svg = (f'<?xml version="1.0" encoding="UTF-8"?>\n'
           f'<!--\n  OmegaT - Computer Assisted Translation (CAT) tool\n'
           f'  Copyright (C) 2026 OmegaT contributors\n'
           f'  This file is part of OmegaT, released under the GNU General Public License,\n'
           f'  version 3 or (at your option) any later version. It is distributed WITHOUT\n'
           f'  ANY WARRANTY. See https://www.gnu.org/licenses/ for details.\n-->\n'
           f'<svg xmlns="http://www.w3.org/2000/svg" width="{CANVAS_W}" height="{height}" '
           f'viewBox="0 0 {CANVAS_W} {height}" font-family="Helvetica, Arial, sans-serif">\n'
           f'<rect width="{CANVAS_W}" height="{height}" fill="#ffffff"/>\n'
           + "\n".join(views + legend + mini + body) + "\n" + footer + "\n" + script + "\n</svg>\n")
    out = os.path.join(os.path.dirname(os.path.abspath(__file__)), "ticket-map.svg")
    with open(out, "w") as fh:
        fh.write(svg)
    print(f"{out}: {len(tickets)} tickets in {len(ordered)} clusters, "
          f"{CANVAS_W}x{height}px, {os.path.getsize(out) // 1024} KiB")


if __name__ == "__main__":
    main()
