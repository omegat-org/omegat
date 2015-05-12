#!/bin/sh

set -e

SCRIPT_ROOT="$(ruby -e "puts File.expand_path(\"$(dirname "$0")\")")"

INKSCAPE="$(which inkscape)"
PNGCRUSH="$(which pngcrush)"

[ -z "$INKSCAPE" ] && echo "Make sure inkscape is on your path." && exit 1
[ -z "$PNGCRUSH" ] && echo "Make sure pngcrush is on your path." && exit 1

SVG_FILE="${SCRIPT_ROOT}/OmegaT.svg"
[ ! -f "$SVG_FILE" ] && echo "Can't find SVG to convert." && exit 1

SIZES="32 64 128 256"
# 512px version is taken from website:
# http://briac.net/omegat-logo/

for SIZE in $SIZES; do
    OUTFILE="${SCRIPT_ROOT}/OmegaT.iconset/icon_${SIZE}x${SIZE}.png"
    inkscape --export-area-page \
             --export-width=$SIZE \
             --export-height=$SIZE \
             --export-png="$OUTFILE" \
             --file="$SVG_FILE"
    pngcrush -ow "$OUTFILE"
done

# The system likes 64x64 to be named 32x32@2x for some reason.
mv "${SCRIPT_ROOT}/OmegaT.iconset/icon_64x64.png" \
   "${SCRIPT_ROOT}/OmegaT.iconset/icon_32x32@2x.png"

iconutil --convert icns "${SCRIPT_ROOT}/OmegaT.iconset"
cp "${SCRIPT_ROOT}/OmegaT.icns" "${SCRIPT_ROOT}/../release/mac-specific/OmegaT.app/Contents/Resources/"
