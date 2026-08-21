#!/usr/bin/env python3
"""
nullmarked_coverage.py - report JSpecify @NullMarked coverage over a Java tree.

Counts top-level types and classifies each as:

  package-marked : its package has a package-info.java carrying @NullMarked
  class-marked   : the type declaration itself carries @NullMarked
  unmarked       : neither

@NullUnmarked is honoured at both levels and subtracts from coverage.

Usage:
    python3 tool/nullmarked_coverage.py /path/to/omegat
    python3 tool/nullmarked_coverage.py . --source-set main
    python3 tool/nullmarked_coverage.py . --by-package --source-set main
    python3 tool/nullmarked_coverage.py . --csv coverage.csv
    python3 tool/nullmarked_coverage.py . --list-unmarked --source-set main

No third-party dependencies.
"""

import argparse
import csv
import os
import re
import sys
from collections import defaultdict

TYPE_KEYWORDS = ("class", "interface", "enum", "record")

# A type declaration at brace depth 0. '@interface' is matched via the '@?'.
TYPE_DECL_RE = re.compile(
    r"\b(?:public|protected|private|abstract|final|static|strictfp|sealed|"
    r"non-sealed)\s+(?:[\w\s-]*?\s+)?(@?(?:class|interface|enum|record))\s+(\w+)"
    r"|^\s*(@?(?:class|interface|enum|record))\s+(\w+)",
    re.MULTILINE,
)

PACKAGE_RE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)


def strip_comments_and_strings(src):
    """Blank out comments and string/char literals, preserving offsets.

    Newlines are kept so that line numbers and brace positions stay aligned.
    """
    out = []
    i = 0
    n = len(src)
    while i < n:
        c = src[i]
        nxt = src[i + 1] if i + 1 < n else ""

        if c == "/" and nxt == "/":
            while i < n and src[i] != "\n":
                out.append(" ")
                i += 1
            continue

        if c == "/" and nxt == "*":
            out.append("  ")
            i += 2
            while i < n and not (src[i] == "*" and i + 1 < n and src[i + 1] == "/"):
                out.append("\n" if src[i] == "\n" else " ")
                i += 1
            if i < n:
                out.append("  ")
                i += 2
            continue

        if c in ('"', "'"):
            # Text block """ ... """
            if c == '"' and src[i:i + 3] == '"""':
                out.append("   ")
                i += 3
                while i < n and src[i:i + 3] != '"""':
                    out.append("\n" if src[i] == "\n" else " ")
                    i += 1
                if i < n:
                    out.append("   ")
                    i += 3
                continue
            quote = c
            out.append(" ")
            i += 1
            while i < n and src[i] != quote:
                if src[i] == "\\" and i + 1 < n:
                    out.append("  ")
                    i += 2
                    continue
                out.append("\n" if src[i] == "\n" else " ")
                i += 1
            if i < n:
                out.append(" ")
                i += 1
            continue

        out.append(c)
        i += 1

    return "".join(out)


def brace_depths(src):
    """Return a list mapping each offset to the brace depth at that offset."""
    depths = [0] * (len(src) + 1)
    depth = 0
    for idx, ch in enumerate(src):
        depths[idx] = depth
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth = max(0, depth - 1)
    depths[len(src)] = depth
    return depths


def preceding_annotations(src, decl_start):
    """Collect annotation names immediately preceding a declaration.

    Walks back to the previous ';', '}' or start of file, which bounds the
    modifier/annotation block of the declaration.
    """
    cut = 0
    for boundary in (";", "}"):
        pos = src.rfind(boundary, 0, decl_start)
        if pos > cut:
            cut = pos + 1
    segment = src[cut:decl_start]
    return set(re.findall(r"@(\w+)", segment))


def find_top_level_types(stripped):
    """Yield (type_name, kind, is_null_marked, is_null_unmarked)."""
    depths = brace_depths(stripped)
    results = []
    for m in TYPE_DECL_RE.finditer(stripped):
        kind = m.group(1) or m.group(3)
        name = m.group(2) or m.group(4)
        if not kind or not name:
            continue
        start = m.start()
        if depths[start] != 0:
            continue
        anns = preceding_annotations(stripped, start)
        results.append(
            (name, kind.lstrip("@"), "NullMarked" in anns, "NullUnmarked" in anns)
        )
    return results


def code_lines(stripped):
    return sum(1 for line in stripped.splitlines() if line.strip())


def detect_module(path, root):
    """Nearest ancestor directory containing a Gradle build file."""
    d = os.path.dirname(os.path.abspath(path))
    root = os.path.abspath(root)
    while d.startswith(root) and len(d) >= len(root):
        for build in ("build.gradle", "build.gradle.kts"):
            if os.path.exists(os.path.join(d, build)):
                rel = os.path.relpath(d, root)
                return "." if rel == "." else rel
        parent = os.path.dirname(d)
        if parent == d:
            break
        d = parent
    return "?"


def detect_source_set(path, root):
    """Source set name from a .../src/<set>/java/... path."""
    parts = os.path.relpath(os.path.abspath(path), os.path.abspath(root)).split(os.sep)
    for i, p in enumerate(parts):
        if p == "src" and i + 1 < len(parts):
            nxt = parts[i + 1]
            return "main" if nxt == "java" else nxt
    return "?"


def scan(root, source_sets=None, skip_dirs=("build", ".git", ".gradle", "out")):
    """Walk the tree and return (records, package_marks)."""
    package_marks = {}   # (module, source_set, package) -> "marked"/"unmarked"
    files = []

    for dirpath, dirnames, filenames in os.walk(root):
        dirnames[:] = [d for d in dirnames if d not in skip_dirs]
        for fn in filenames:
            if fn.endswith(".java"):
                files.append(os.path.join(dirpath, fn))

    # Pass 1: package-info.java establishes package-level marking.
    for path in files:
        if os.path.basename(path) != "package-info.java":
            continue
        try:
            raw = open(path, encoding="utf-8", errors="replace").read()
        except OSError as e:
            print(f"warning: cannot read {path}: {e}", file=sys.stderr)
            continue
        stripped = strip_comments_and_strings(raw)
        pm = PACKAGE_RE.search(stripped)
        if not pm:
            continue
        key = (detect_module(path, root), detect_source_set(path, root), pm.group(1))
        anns = set(re.findall(r"@(\w+)", stripped))
        if "NullUnmarked" in anns:
            package_marks[key] = "unmarked"
        elif "NullMarked" in anns:
            package_marks[key] = "marked"
        else:
            package_marks[key] = "unmarked"

    # Pass 2: classify every top-level type.
    records = []
    for path in files:
        if os.path.basename(path) == "package-info.java":
            continue
        source_set = detect_source_set(path, root)
        if source_sets and source_set not in source_sets:
            continue
        try:
            raw = open(path, encoding="utf-8", errors="replace").read()
        except OSError as e:
            print(f"warning: cannot read {path}: {e}", file=sys.stderr)
            continue

        stripped = strip_comments_and_strings(raw)
        pm = PACKAGE_RE.search(stripped)
        package = pm.group(1) if pm else "(default)"
        module = detect_module(path, root)
        loc = code_lines(stripped)

        pkg_marked = package_marks.get((module, source_set, package)) == "marked"
        types = find_top_level_types(stripped)

        if not types:
            continue

        # Attribute file LOC to the first top-level type to avoid double counting.
        for idx, (name, kind, cls_marked, cls_unmarked) in enumerate(types):
            if cls_unmarked:
                status = "unmarked"
            elif cls_marked:
                status = "class-marked"
            elif pkg_marked:
                status = "package-marked"
            else:
                status = "unmarked"
            records.append(
                {
                    "module": module,
                    "source_set": source_set,
                    "package": package,
                    "file": os.path.relpath(path, root),
                    "type": name,
                    "kind": kind,
                    "status": status,
                    "loc": loc if idx == 0 else 0,
                }
            )

    return records, package_marks


def aggregate(records, key_fields):
    agg = defaultdict(
        lambda: {"package-marked": 0, "class-marked": 0, "unmarked": 0, "loc": 0,
                 "loc_unmarked": 0}
    )
    for r in records:
        key = tuple(r[f] for f in key_fields)
        agg[key][r["status"]] += 1
        agg[key]["loc"] += r["loc"]
        if r["status"] == "unmarked":
            agg[key]["loc_unmarked"] += r["loc"]
    return agg


def print_table(agg, key_label, min_types=0):
    rows = []
    for key, v in agg.items():
        marked = v["package-marked"] + v["class-marked"]
        total = marked + v["unmarked"]
        if total < min_types:
            continue
        pct = (marked / total * 100) if total else 0.0
        rows.append((" / ".join(key), total, marked, v["unmarked"], pct,
                     v["loc"], v["loc_unmarked"]))

    rows.sort(key=lambda r: (-r[3], -r[1]))

    width = max([len(r[0]) for r in rows] + [len(key_label)])
    header = (f"{key_label:<{width}}  {'types':>6} {'marked':>7} {'unmk':>6} "
              f"{'cov%':>6} {'LOC':>8} {'LOC unmk':>9}")
    print(header)
    print("-" * len(header))
    for name, total, marked, unmarked, pct, loc, loc_unmarked in rows:
        print(f"{name:<{width}}  {total:>6} {marked:>7} {unmarked:>6} "
              f"{pct:>5.1f}% {loc:>8} {loc_unmarked:>9}")
    print("-" * len(header))

    t_total = sum(r[1] for r in rows)
    t_marked = sum(r[2] for r in rows)
    t_unmarked = sum(r[3] for r in rows)
    t_loc = sum(r[5] for r in rows)
    t_loc_unmarked = sum(r[6] for r in rows)
    t_pct = (t_marked / t_total * 100) if t_total else 0.0
    print(f"{'TOTAL':<{width}}  {t_total:>6} {t_marked:>7} {t_unmarked:>6} "
          f"{t_pct:>5.1f}% {t_loc:>8} {t_loc_unmarked:>9}")


def main():
    ap = argparse.ArgumentParser(
        description="Report JSpecify @NullMarked coverage over a Java source tree."
    )
    ap.add_argument("root", help="repository root")
    ap.add_argument("--source-set", action="append", dest="source_sets",
                    help="restrict to a source set (main, test, testAcceptance); "
                         "repeatable")
    ap.add_argument("--by-package", action="store_true",
                    help="break down per package instead of per module")
    ap.add_argument("--min-types", type=int, default=0,
                    help="hide rows with fewer than N top-level types")
    ap.add_argument("--csv", metavar="FILE",
                    help="write the per-type records to CSV")
    ap.add_argument("--list-unmarked", action="store_true",
                    help="list unmarked types, largest files first")
    args = ap.parse_args()

    if not os.path.isdir(args.root):
        sys.exit(f"error: not a directory: {args.root}")

    records, package_marks = scan(args.root, source_sets=args.source_sets)

    if not records:
        sys.exit("error: no Java types found; check the path and --source-set")

    if args.by_package:
        agg = aggregate(records, ["module", "source_set", "package"])
        label = "module / set / package"
    else:
        agg = aggregate(records, ["module", "source_set"])
        label = "module / source set"

    print_table(agg, label, min_types=args.min_types)

    marked_pkgs = sum(1 for v in package_marks.values() if v == "marked")
    print(f"\npackage-info.java files: {len(package_marks)} "
          f"({marked_pkgs} carrying @NullMarked)")
    print("class-marked types: "
          f"{sum(1 for r in records if r['status'] == 'class-marked')}")

    if args.list_unmarked:
        print("\nUnmarked types (largest files first):")
        unmarked = [r for r in records if r["status"] == "unmarked"]
        seen = set()
        for r in sorted(unmarked, key=lambda r: -r["loc"]):
            if r["file"] in seen:
                continue
            seen.add(r["file"])
            print(f"  {r['loc']:>6}  {r['file']}")

    if args.csv:
        with open(args.csv, "w", newline="", encoding="utf-8") as fh:
            w = csv.DictWriter(
                fh,
                fieldnames=["module", "source_set", "package", "file", "type",
                            "kind", "status", "loc"],
            )
            w.writeheader()
            w.writerows(records)
        print(f"\nwrote {len(records)} records to {args.csv}")


if __name__ == "__main__":
    main()
