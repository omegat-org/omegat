#!/bin/sh

set -e

SCRIPT_ROOT="$(ruby -e "puts File.expand_path(\"$(dirname "$0")\")")"

INKSCAPE="$(which inkscape)"
PNGCRUSH="$(which pngcrush)"

[ -z "$INKSCAPE" ] && echo "Make sure inkscape is on your path." && exit 1
[ -z "$PNGCRUSH" ] && echo "Make sure pngcrush is on your path." && exit 1

SVG_FILE="${SCRIPT_ROOT}/OmegaT.svg"
[ ! -f "$SVG_FILE" ] && echo "Can't find SVG to convert." && exit 1

SIZES="16 32 64 128 256 512 1024"

for SIZE in $SIZES; do
    OUTFILE="${SCRIPT_ROOT}/OmegaT.iconset/icon_${SIZE}x${SIZE}.png"
    inkscape "$SVG_FILE" \
		--export-type="png" \
		--export-area-page \
		--export-width=$SIZE \
		--export-height=$SIZE \
		--export-filename="$OUTFILE" \

    pngcrush -ow "$OUTFILE"
done

cp "${SCRIPT_ROOT}/OmegaT.iconset/icon_32x32.png" "${SCRIPT_ROOT}/OmegaT.iconset/icon_16x16@2x.png"
cp "${SCRIPT_ROOT}/OmegaT.iconset/icon_64x64.png" "${SCRIPT_ROOT}/OmegaT.iconset/icon_32x32@2x.png"
cp "${SCRIPT_ROOT}/OmegaT.iconset/icon_128x128.png" "${SCRIPT_ROOT}/OmegaT.iconset/icon_64x64@2x.png"
cp "${SCRIPT_ROOT}/OmegaT.iconset/icon_256x256.png" "${SCRIPT_ROOT}/OmegaT.iconset/icon_128x128@2x.png"
cp "${SCRIPT_ROOT}/OmegaT.iconset/icon_512x512.png" "${SCRIPT_ROOT}/OmegaT.iconset/icon_256x256@2x.png"
mv "${SCRIPT_ROOT}/OmegaT.iconset/icon_1024x1024.png" "${SCRIPT_ROOT}/OmegaT.iconset/icon_512x512@2x.png"

iconutil --convert icns "${SCRIPT_ROOT}/OmegaT.iconset"
cp "${SCRIPT_ROOT}/OmegaT.icns" "${SCRIPT_ROOT}/../release/mac-specific/OmegaT.app/Contents/Resources/"
