/* :name=Adapt standard tags :description=Adapt standard tags when Replace with Match command invoked
 * 
 *  The workaround by script for RFE #841:
 *  Adapt tags to match target 
 *  http://sourceforge.net/p/omegat/feature-requests/841/
 *
 * @author  Yu Tang
 * @date    2015-08-19
 * @version 0.2.1
 */

import org.omegat.core.Core
import org.omegat.core.data.ExternalTMX
import org.omegat.core.matching.NearString
import org.omegat.gui.main.MainWindowMenu
import org.omegat.util.Preferences
import org.omegat.util.StringUtil

import javax.swing.*
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.regex.Matcher

import static org.omegat.util.PatternConsts.OMEGAT_TAG_DECOMPILE

enum TagType { START, END, SINGLE }

String substituteNumbersInStandardTag(String source, String sourceMatch, String targetMatch) {
    def sourceTagList = getStandardTagList(source)
    def sourceMatchTagList = getStandardTagList(sourceMatch)
    def targetMatchTagList = getStandardTagList(targetMatch)

    // check tag size
    if (sourceMatchTagList.size() != targetMatchTagList.size() || //Not the same number of tags
        sourceMatchTagList.size() != sourceTagList.size()) {
        return targetMatch
    }

    // check if sourceTagList and sourceMatchTagList have same TagType order
    if (!isSameTagTypeOrder(sourceTagList, sourceMatchTagList)) {
        return targetMatch
    }

    // get transposed tag string list for replacing
    def transposedTagList = getTransposedTagList(sourceTagList, sourceMatchTagList, targetMatchTagList)

    // build new target string with transposed tags
    buildTargetString(targetMatch, targetMatchTagList, transposedTagList)
}

def getStandardTagList(String source) {
    def result = []
    Matcher tag = OMEGAT_TAG_DECOMPILE.matcher(source)
    while (tag.find()) {
        String tagSource = tag.group()
        String name = tag.group(2) + tag.group(3)
        int offset = tag.start()
        def info = createTagString(tagSource, name, offset)
        result << info
    }
    result
}

def createTagString(String source, String name, int offset) {
    String tag = source
    tag.metaClass.name = name
    tag.metaClass.offset = offset
    tag.metaClass.getType = { ->
        if (delegate.size() < 4 || (!delegate.startsWith("<") && !delegate.endsWith(">"))) {
            return TagType.SINGLE
        }

        if (delegate.startsWith("</")) {
            return TagType.END
        } else if (delegate.endsWith("/>")) {
            return TagType.SINGLE
        }

        return TagType.START
    }
    tag
}

boolean isSameTagTypeOrder(List tagList1, List tagList2) {
    def names1 = []
    def names2 = []
    for (int i = 0; i < tagList1.size(); i++) {
        def tagInfo1 = tagList1[i]
        def tagInfo2 = tagList2[i]
        names1 << tagInfo1.name
        names2 << tagInfo2.name
        if (tagInfo1.type != tagInfo2.type || names1.indexOf(tagInfo1.name) != names2.indexOf(tagInfo2.name)) {
            return false
        }
    }
    true
}

def getTransposedTagList(List sourceTagList, List sourceMatchTagList, List targetMatchTagList) {
    targetMatchTagList.collect { tag ->
        int i = sourceMatchTagList.indexOf(tag)
        if (i == -1) {
            throw new RuntimeException("Tag '$tag' is not found in source of match.")
        }
        sourceTagList[i]
    }
}

String buildTargetString(String targetMatch, List targetMatchTagList, List transposedTagList) {
    def result = []
    int from = 0
    targetMatchTagList.eachWithIndex { tag, index ->
        result << targetMatch[from..<tag.offset] // the string before tag
        result << transposedTagList[index]       // tag
        from = tag.offset + tag.size()
    }
    if (from < targetMatch.size()) {
        result << targetMatch[from..<targetMatch.size()] // the string after tag
    }

    result.join ''
}

def menuItem = mainWindow.mainMenu.editOverwriteTranslationMenuItem
menuItem.addActionListener { ActionEvent e ->
    if (StringUtil.isEmpty(mainWindow.selectedTextInMatcher) && Preferences.isPreference(Preferences.CONVERT_NUMBERS)) {
        SwingUtilities.invokeLater({
            def near = Core.matcher.activeMatch
            if (near != null) {
                def currentTranslation = editor.currentTranslation
                def processedTranslation = substituteNumbersInStandardTag(editor.currentEntry.srcText, near.source, currentTranslation)
                if (currentTranslation != processedTranslation) {
                    if (near.comesFrom == NearString.MATCH_SOURCE.TM
                            && ExternalTMX.isInPath(new File(Core.project.projectProperties.TMRoot, 'mt'),
                                    new File(near.projs[0]))) {
                        editor.replaceEditTextAndMark processedTranslation
                    } else {
                        editor.replaceEditText processedTranslation
                    }
                    editor.requestFocus()
                }
            }
        } as Runnable)
    }
} as ActionListener
"adapt_tags_to_match_target script is available in current session."