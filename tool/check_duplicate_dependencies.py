#!/usr/bin/env python3
# Guards source distribution against redundant third-party jars.
#
# Policy:
# * multiple versions of one library inside one provided-libs directory: error
# * version skew of one library across core and module directories: error,
#   unless listed in baseline file (legacy skews scheduled for reduction)
# * baseline entry without matching skew: error, entry must be removed
#   (ratchet: resolved skews must leave baseline; additions need reason)
# * identical jar name in both directories: allowed, module packaging skips
#   jars shipped by application by file name
# * expected directory missing: error, protects against layout moves
#
# Maintenance: new legitimate library whose name parses wrong -> extend
# ALIASES; legacy skew impossible to fix right now -> add line with reason
# to baseline file.

import argparse
import re
import sys
from collections import defaultdict
from pathlib import Path

# Distinct libraries sharing artifact file name prefix.
# Pattern applies to full jar file name; first match wins.
ALIASES = [
    (re.compile(r"^annotations-4(\.\d+)*\.jar$"), "android-annotations"),
    (re.compile(r"^annotations-\d+(\.\d+)*\.jar$"), "jetbrains-annotations"),
]

VERSION_SPLIT = re.compile(r"-(?=\d)")


def library_name(jar_name):
    for pattern, name in ALIASES:
        if pattern.match(jar_name):
            return name
    return VERSION_SPLIT.split(jar_name[: -len(".jar")], maxsplit=1)[0]


def scan(directory):
    libs = defaultdict(list)
    for jar in sorted(directory.glob("*.jar")):
        libs[library_name(jar.name)].append(jar.name)
    return libs


def read_baseline(path):
    if not path.is_file():
        print(f"Note: baseline file {path} not found, treating as empty.")
        return set()
    entries = set()
    for line in path.read_text(encoding="utf-8").splitlines():
        entry = line.split("#", 1)[0].strip()
        if entry:
            entries.add(entry)
    return entries


def main():
    parser = argparse.ArgumentParser(
        description="Check source distribution provided libs for redundant jars.")
    parser.add_argument("dist_root", type=Path,
                        help="root of installed source distribution")
    parser.add_argument("--baseline", type=Path,
                        default=Path(__file__).with_name("known_dependency_skews.txt"),
                        help="file listing tolerated legacy cross-directory skews")
    args = parser.parse_args()

    core_dir = args.dist_root / "lib" / "provided" / "core"
    module_dir = args.dist_root / "lib" / "provided" / "module"
    errors = []

    for directory in (core_dir, module_dir):
        if not directory.is_dir():
            errors.append(f"expected directory missing: {directory} (layout moved?)")
    if errors:
        report(errors)
        return 1

    core, module = scan(core_dir), scan(module_dir)

    for label, libs in (("core", core), ("module", module)):
        for name, jars in sorted(libs.items()):
            if len(jars) > 1:
                errors.append(
                    f"multiple versions of '{name}' in {label}: {', '.join(jars)}")

    baseline = read_baseline(args.baseline)
    skews = set()
    for name in sorted(core.keys() & module.keys()):
        if core[name] != module[name]:
            skews.add(name)
            if name not in baseline:
                errors.append(
                    f"version skew of '{name}': core has {', '.join(core[name])}, "
                    f"module has {', '.join(module[name])}")

    for name in sorted(baseline - skews):
        errors.append(
            f"stale baseline entry '{name}': skew resolved (good news) — "
            f"remove line from {args.baseline.name}")

    tolerated = sorted(baseline & skews)
    if tolerated:
        print(f"Tolerated legacy skews ({len(tolerated)}): {', '.join(tolerated)}")

    if errors:
        report(errors)
        return 1
    print(f"OK: {sum(len(j) for j in core.values())} core jars, "
          f"{sum(len(j) for j in module.values())} module jars, no new redundancy.")
    return 0


def report(errors):
    for error in errors:
        print(f"Error: {error}", file=sys.stderr)
    print(f"{len(errors)} problem(s) found.", file=sys.stderr)


if __name__ == "__main__":
    sys.exit(main())
