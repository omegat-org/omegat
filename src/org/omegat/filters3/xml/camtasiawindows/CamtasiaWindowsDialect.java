/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2009 Didier Briel
               2011 Guido Leenders, Didier Briel
               2012 Guido Leenders
 
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

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
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.filters3.xml.camtasiawindows;

import java.util.regex.Pattern;

import org.omegat.filters3.xml.DefaultXMLDialect;

/**
 * This class specifies the Camtasia for Windows XML Dialect of camproj files.
 * 
 * @author Guido Leenders
 * @author Didier Briel
 */
public class CamtasiaWindowsDialect extends DefaultXMLDialect {
    public static final Pattern CAMW_ROOT_TAG = Pattern.compile("Project_Data");

    public CamtasiaWindowsDialect() {
        defineConstraint(CONSTRAINT_ROOT, CAMW_ROOT_TAG);
        
        defineParagraphTags(new String[] {
            "Caption",
            "RichTextHTML",
            "strOverlayRichText",
            "Text",
            "TitleName",
            "RichText",
            "Value" /* Metadata. */,
            "Project_Notes",
            "JumpURL"
        });
        
        defineIntactTags(new String[] {
            "Accel_cmd",
            "Accel_fVirt",
            "Action",
            "AddTextDropShadow",
            "AlwaysDisplay",
            "AudioClickReduction",
            "AudioClickSensitivity",
            "AudioClippingReduction",
            "AudioCompressionOn",
            "AudioCompressionPreset",
            "AudioCurAttack",
            "AudioCurGain",
            "AudioCurRatio",
            "AudioCurRelease",
            "AudioCurThreshold",
            "AudioCustAttack",
            "AudioCustGain",
            "AudioCustRatio",
            "AudioCustRelease",
            "AudioCustThreshold",
            "AudioGlobalBypass",
            "AudioNoiseReduction",
            "AudioNoiseSensitivity",
            "AudioTraining",
            "AudioVocalEnhancement",
            "AutoSaveFile",
            "AutoSizeCallouts",
            "BGColor",
            "BGImage",
            "BGType",
            "BackgroundColor",
            "BackgroundOpacity",
            "BookmarkTime",
            "BookmarkUniqueID",
            "BorderState",
            "CanFlip_X",
            "CanFlip_X180",
            "CanFlip_X270",
            "CanFlip_X90",
            "AutoSizeClip",
            "TitleClipHeight",
            "CanRotate_180",
            "CanRotate_270",
            "CanRotate_90",
            "CaptionAlignment",
            "CaptionFont",
            "CaptionFontSize",
            "ClipBin_Array",
            "ClipID",
            "ColorMask",
            "CreateMarker",
            "CursorKeyframe_Array",
            "DShowControl_ClipMap",
            "DShowControl_Edit_Array",
            "DefaultFontSize",
            "DisableFading",
            "DockPIP",
            "Duration",
            "EnableBackground",
            "End",
            "FadeIn",
            "FadeInEffect",
            "FadeOut",
            "FadeOutEffect",
            "FieldArrayKey",
            "FillState",
            "FlipOnX",
            "FlipOnY",
            "FontSize",
            "FriendlyName",
            "GotoTime",
            "Height",
            "Hotspot_Info",
            "ID",
            "ImagePath",
            "ImagePath",
            "Image_Object",
            "IndentLevel",
            "KeepAspectRatio",
            "Line",
            "MaxCaptionLength",
            "NoiseFilterInfo",
            "Opacity",
            "OpenURLInNewWindow",
            "OverlayCaptions",
            "OverlayFlags",
            "OverlayID",
            "OverlayName",
            "Overlay_ClipImageAbove_Object",
            "Overlay_KeyStrokeImageAbove_Object",
            "PIP_Array",
            "PauseAtEnd",
            "PowerPointFilename",
            "PowerPointProject",
            "PreserveSize",
            "ProjectID",
            /*"Project_MetaData",*/
            /*"Project_Notes",*/
            "Project_Settings",
            "QuestionGroup_Array",
            "RectDest_Height",
            "RectDest_Width",
            "RectDest_X",
            "RectDest_Y",
            "RotationAngle",
            "ShadowBlur",
            "ShadowOpacity",
            "ShadowSize_X",
            "ShadowSize_Y",
            "ShowCaptions",
            "SmoothScale",
            "Start",
            "StaticTitle",
            "Style",
            "TextBottomIndent",
            "TextColor",
            "TextLeftIndent",
            "TextRectDest_Height",
            "TextRectDest_Width",
            "TextRectDest_X",
            "TextRectDest_Y",
            "TextRightIndent",
            "TextShadowBlur",
            "TextShadowOpacity",
            "TextShadowSize_X",
            "TextShadowSize_Y",
            "TextTopIndent",
            "TextTransparencyFromCallout",
            "TextVJust",
            "TextVJust",
            "Time",
            "TitleClipWidth",
            "TrackID",
            "Type",
            "UI_Layout",
            "UniqueID",
            "UseAsMarker",
            "UseDropShadow",
            "UseTColor",
            "UseTextDropShadow",
            "VectorGrow",
            "VectorProps",
            "WhiteColorMask",
            "Width",
            "XGrow",
            "YGrow",
            "ZoomPanHints",
            "Zoom_Array",
            "borderWidth",
            "clrShadow",
            "clrTextShadow",
            "clrTransparent",
            "cutoutPercentage",
            "cutoutScaling",
            "figure",
            "reshapers",
            "roundedRectRadius",
            "seamY",
            "shapeConstraints",
            "svg",
            "textBoundaries"
        });

      // XML fragments can contains tags you want to remain recognizable. Specify them here.
      // defineShortcut("ProjectWidth", "ProjectWidth");

    }

}
