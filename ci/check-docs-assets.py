#!/usr/bin/env python3
# Guard for the developer documentation published on Read the Docs:
# every image under src/docs/developer must be referenced by at least one
# Markdown page there, so no unlinked media blobs accumulate in the
# repository (see PR #2255 and 48.DeveloperDocumentation.md).
#
# Standard library only; run from the repository root:
#   python3 ci/check-docs-assets.py
# Exit code 0 when every image is referenced, 1 with a list of orphans.
#
# Recognised reference forms, matching what 48.DeveloperDocumentation.md
# prescribes: plain Markdown images/links "](path)" and raw HTML
# <img src="path">. MyST image directives and reference-style links are
# deliberately not supported.

import pathlib
import re
import sys

DOCS_DIR = pathlib.Path(__file__).resolve().parent.parent / "src" / "docs" / "developer"
IMAGE_SUFFIXES = {".svg", ".png", ".jpg", ".jpeg", ".gif"}


def main():
    images = [p for p in DOCS_DIR.rglob("*")
              if p.suffix.lower() in IMAGE_SUFFIXES and "_build" not in p.parts]
    referenced = set()
    for page in DOCS_DIR.rglob("*.md"):
        if "_build" in page.parts:
            continue
        text = page.read_text(encoding="utf-8", errors="replace")
        # Markdown images/links "](path)" and raw HTML <img src="path">.
        for match in re.findall(r"\]\(([^)\s]+)\)|<img[^>]+src=[\"']([^\"']+)[\"']", text):
            for target in match:
                if target:
                    referenced.add((page.parent / target.split("#")[0]).resolve())
    orphans = [img for img in images if img.resolve() not in referenced]
    if orphans:
        print("Unreferenced documentation images (add a Markdown reference or remove the file):")
        for img in sorted(orphans):
            print("  " + str(img.relative_to(DOCS_DIR.parent.parent.parent)))
        return 1
    print("check-docs-assets: %d images, all referenced." % len(images))
    return 0


if __name__ == "__main__":
    sys.exit(main())
