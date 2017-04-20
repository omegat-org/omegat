/* :name=Adapt standard tags :description=Adapt standard tags when Replace with Match command invoked
 * 
 *  The workaround by script for RFE #841:
 *  Adapt tags to match target 
 *  http://sourceforge.net/p/omegat/feature-requests/841/
 *
 *         |    Editor    |     Match
 *  -------+--------------+--------------
 *  Source | <a1>foo</a1> | <a9>foo</a9>
 *  -------+--------------+--------------
 *  Target | <a9>bar</a9> | <a9>bar</a9>
 *                | <--adapt
 *           <a1>bar</a1>
 *
 *  Note:
 *  This script does NOT cover user defined custom tags. Only take OmegaT standard tags.
 *
 * @author  Yu Tang
 * @date    2017-03-11
 * @version 0.3.11
 */

package me.goat.groovy.scripting

import groovy.text.Template
import org.codehaus.groovy.runtime.GStringImpl
import org.omegat.core.Core
import org.omegat.core.data.ExternalTMX
import org.omegat.core.matching.NearString
import org.omegat.util.TagUtil.TagType

import javax.swing.SwingUtilities

import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import java.util.regex.Matcher

import static org.omegat.util.PatternConsts.OMEGAT_TAG
import static org.omegat.util.PatternConsts.OMEGAT_TAG_DECOMPILE

def menuItem = mainWindow.mainMenu.editOverwriteTranslationMenuItem

RemoveOldTagAdapters(menuItem) // just in case
TagAdapter adapter = new TagAdapter()
//test(adapter)
menuItem.addActionListener adapter
"adapt_tags_to_match_target script is available in current session."

class TagAdapter implements ActionListener {

    static private List<Closure> adapters = [this.&adaptCurrentSourceHasNoTags,
                                              this.&adaptEachTags1to1,
                                              this.&adaptTagBlocks,
                                              this.&removeNonExistentTags]

    @Override
    void actionPerformed(ActionEvent actionEvent) {
        SwingUtilities.invokeLater({
            NearString near = Core.matcher.activeMatch
            if (near != null) {
                def currentTranslation = Core.editor.currentTranslation
                def currentSource = Core.editor.currentEntry.srcText
                //def matchTranslation = near.translation
                def matchSource = near.source

                String adapted = adapt(currentSource, matchSource, currentTranslation)

                // replace an edit text with a tag adapted text
                if (currentTranslation != adapted) {
                    replaceEditText(adapted, near)
                }
            }
        } as Runnable)
    }

    static void replaceEditText(String adapted, NearString near) {
        if (near.comesFrom == NearString.MATCH_SOURCE.TM
                && ExternalTMX.isInPath(new File(Core.project.projectProperties.TMRoot, 'mt'),
                new File(near.projs[0]))) {
            Core.editor.replaceEditTextAndMark adapted
        } else {
            Core.editor.replaceEditText adapted
        }
        Core.editor.requestFocus()
    }

    static String adapt(String currentSource, String matchSource, String currentTranslation) {
        String result = null

        use(TagStringCategory) {
            if (currentTranslation.hasStandardTags()) {
                result = adapters.findResult { Closure adapter ->
                    adapter(currentSource,matchSource, currentTranslation)
                }
            }
        }

        result ?: currentTranslation
    }

    static private Map<String, String> toMap(List<String> keys, List<String> values) {
        [keys, values].transpose().flatten().toSpreadMap()
    }

    /**
     * Adapter for the case of the current source has no tags.
     *
     *         |    Editor    |     Match
     *  -------+--------------+--------------
     *  Source |     foo      | <a9>foo</a9>
     *  -------+--------------+--------------
     *  Target | <a9>bar</a9> | <a9>bar</a9>
     *                | <--adapt
     *               bar
     *
     * @return an adjusted translation or null
     */
    static private String adaptCurrentSourceHasNoTags(String currentSource, String matchSource, String currentTranslation) {
        if (! currentSource.hasStandardTags()) {
            currentTranslation.replaceAll(OMEGAT_TAG, "")
        }
    }

    /**
     * Adapter for the case of 1-to-1 strictly tag matching.
     *
     *         |     Editor     |     Match
     *  -------+----------------+--------------
     *  Source | <a1>foo</a1>   | <a9>a foo</a9>
     *  -------+----------------+--------------
     *  Target | <a9>a bar</a9> | <a9>a bar</a9>
     *                 | <--adapt
     *           <a1>a bar</a1>
     *
     * @return an adjusted translation or null
     */
    static private String adaptEachTags1to1(String currentSource, String matchSource, String currentTranslation) {
        List<String> keys = matchSource.tags
        List<String> values = currentSource.tags

        if (! correspondExactly(keys, values)) {
            return null
        }

        Map<String, String> map = toMap(keys, values)
        def taggedString = new TagTemplate(currentTranslation)
        taggedString.make(map)
    }

    /**
     * Check if tags1 list corresponds exactly to tags2 list.
     * They must be;
     *     1. not empty
     *     2. same size
     *     3. same tag type order
     *     4. same tag name order
     *
     * Examples)
     *     correspondExactly([<a1>, <s2>, </s2>, </a1>],
     *                       [<a6>, <s7>, </s7>, </a6>]) // true
     *     correspondExactly([], [])                     // false (empty list)
     *     correspondExactly([<a1>, <s2>, </s2>, </a1>],
     *                       [<a1>, <s2>, </s2>])        // false (size un-match)
     *     correspondExactly([<a1>, <a2>, </a2>, </a1>],
     *                       [<a1>, <a2/>, <a2>, </a1>]) // false (type order un-match)
     *     correspondExactly([<a1>, <s2>, </s2>, </a1>],
     *                       [<a1>, <s2>, </a1>, </s2>]) // false (name order un-match)
     * @param tags1
     * @param tags2
     * @return
     */
    static private boolean correspondExactly(List<String> tags1, List<String> tags2) {
        // Empty or different sizes?
        if (tags1.isEmpty() || tags2.isEmpty() || tags1.size() != tags2.size()) {
            return false
        }

        // same order for tag types and tag names?
        Map t1IndexOf, t2IndexOf
        t1IndexOf = [:].withDefault { t1IndexOf.size() }
        t2IndexOf = [:].withDefault { t2IndexOf.size() }

        [tags1, tags2].transpose().every { String tag1, String tag2 ->
            (tag1.type == tag2.type) && (t1IndexOf[tag1.name] == t2IndexOf[tag2.name])
        }
    }

    /**
     * Adapter for the case of the tag block matching between current source and match source.
     *
     *         |       Editor       |     Match
     *  -------+--------------------+-------------------
     *  Source |    <a1>foo</a1>    | <a9><f10/>foo</a9>
     *  -------+--------------------+-------------------
     *  Target | <a9><f10/>bar</a9> | <a9><f10/>bar</a9>
     *                   | <--adapt
     *              <a1>bar</a1>
     *
     * @return an adjusted translation or null
     */
    static private String adaptTagBlocks(String currentSource, String matchSource, String currentTranslation) {
        if (currentSource.withoutTags != matchSource.withoutTags) {
            return null
        }

        // Check if currentSource tag blocks corresponds exactly to matchSource tag blocks.
        Map<Integer, String> currentSourceTagBlockInfos = currentSource.tagBlockInfos
        Map<Integer, String> matchSourceTagBlockInfos = matchSource.tagBlockInfos
        if (! matchSourceTagBlockInfos.keySet().containsAll(currentSourceTagBlockInfos.keySet())) {
            return null
        }

        // create a recursive Map from matchSource to currentSource
        Map<String, String> map = toFlexMap(matchSourceTagBlockInfos, currentSourceTagBlockInfos)

        // create a template and apply the Map
        def taggedString = new TagTemplate(currentTranslation, { Matcher tag, String beforeTag, int prevTagEnd ->
            if (!delegate._binding.isEmpty() && tag.start() == prevTagEnd) {
                // a continuous tag in a block
                delegate._binding << (delegate._binding.pop() + tag.group())
            } else {
                // begin new tag block
                delegate._strings << beforeTag
                delegate._binding << tag.group()
            }
        })
        taggedString.make(map)
    }

    /**
     * Transform KeyMap and ValueMap into a FlexMap.
     *
     * ex. map == [a:x, b:y, c:z]
     *     map[a]   //-> x
     *     map[ab]  //-> xy
     *     map[cba] //-> zyx
     *
     * @param keyBlockInfos
     * @param valueBlockInfos
     * @return
     */
    static private Map<String, String> toFlexMap(Map<Integer, String> keyBlockInfos, Map<Integer, String> valueBlockInfos) {

        Map<String, String> map = new FlexMap()
        keyBlockInfos.each {Integer k, String v ->
            map[v] = valueBlockInfos[k] // the value can be empty string if it's not found in current source.
        }
        map
    }

    /**
     * Adapter for removing non-existent tags from the current translation against current source.
     *
     *         |    Editor    |     Match
     *  -------+--------------+--------------
     *  Source |     foo      | <a1>foo</a1>
     *  -------+--------------+--------------
     *  Target | <a1>bar</a1> | <a1>bar</a1>
     *                | <--adapt
     *              bar
     *
     * @return an adjusted translation or null
     */
    static private String removeNonExistentTags(String currentSource, String matchSource, String currentTranslation) {
        if (! currentTranslation.hasStandardTags()) {
            return null
        }

        // A Map: key and value pair has the same tag string
        Map<String, String> map = currentSource.tags.collectEntries([:].withDefault {""}) {
            [(it):it]
        }

        // create a template and apply the Map
        def taggedString = new TagTemplate(currentTranslation)
        taggedString.make(map)
    }
}

@Category(String)
class TagStringCategory {

    boolean hasStandardTags() {
        OMEGAT_TAG.matcher(this).find()
    }

    List<String> getTags() {
        this.findAll(OMEGAT_TAG)
    }

    String getWithoutTags() {
        this.replaceAll(OMEGAT_TAG, "")
    }

    TagType getType() {
        this[-2] == "/" ? TagType.SINGLE :
                this[1] == "/" ? TagType.END :
                        TagType.START
    }

    String getName() {
        def m = OMEGAT_TAG_DECOMPILE.matcher(this)
        m.matches() ? m.group(2) + m.group(3) : this
    }

    /**
     * Extract tag blocks and their relative positions for the display text.
     * @return A map constructed by block positions as keys and tag block text as values
     */
    Map<Integer, String> getTagBlockInfos() {
        Map<Integer, String> result = [:].withDefault {""}
        def (int position, int prevTagEnd) = [0, 0]
        Matcher tag = OMEGAT_TAG.matcher(this)

        while (tag.find()) {
            if (!result.isEmpty() && tag.start() == prevTagEnd) {
                // a continuous tag in a block
                result[position] += tag.group()
            } else {
                // begin new tag block
                position += tag.start() - prevTagEnd  // update current position
                result[position] = tag.group()
            }
            prevTagEnd = tag.end()
        }

        result
    }

    /** Output a string to the console in Scripting window for debug. */
    void print() {
        org.omegat.gui.scripting.ScriptingWindow.window.logResult(this)
    }
}

/**
 * Represents a String which contains OmegaT standard tags as placeholders.
 */
class TagTemplate implements Template {
    final List<String> _strings = []
    final List<String> _binding = []

    TagTemplate(String text) {
        this(text, { Matcher tag, String beforeTag ->
            delegate._strings << beforeTag
            delegate._binding << tag.group()
        })
    }

    TagTemplate(String text, Closure tagProcessor) {

        tagProcessor.delegate = this
        tagProcessor.resolveStrategy = Closure.DELEGATE_ONLY

        Matcher tag = OMEGAT_TAG.matcher(text)
        int prevTagEnd = 0
        while (tag.find()) {
            String beforeTag = text[prevTagEnd..<tag.start()]
            switch (tagProcessor.maximumNumberOfParameters) {
                case 2: tagProcessor(tag, beforeTag);             break
                case 3: tagProcessor(tag, beforeTag, prevTagEnd); break
                default: throw new IllegalArgumentException("Class TagTemplate constructor is applicable for 2 or 3 arguments Closure.")
            }
            prevTagEnd = tag.end()
        }

        if (prevTagEnd < text.size()) {
            _strings << text[prevTagEnd..<text.size()]
        }
    }

    @Override
    public String toString() {
        getGString()
    }

    @Override
    public Writable make() {
        getGString()
    }

    @Override
    public Writable make(Map binding) {
        new GStringImpl(_binding.collect{ binding[it] } as Object[], _strings as String[])
    }

    private GString getGString() {
        new GStringImpl(_binding as Object[], _strings as String[])
    }

    List<String> getValues() {
        _binding.clone() as List<String>
    }
}

class FlexMap extends HashMap<String, String> {

    @Override
    String put(String s, String s2) {
        String result = super.put(s, s2)

        // split tags and put them
        List<String> keyTags = s.tags
        if (keyTags.size() > 1) {
            List<String> valueTags = s2.tags
            if (keyTags.size() == valueTags.size()) {
                [keyTags, valueTags].transpose().each {k, v -> super.put(k, v)}
            }
        }

        return result
    }

    @Override
    String get(Object o) {
        List<String> nextSearchTags = o.toString().tags
        List<String> result = []
        List<String> searchTags = nextSearchTags

        while(! searchTags.isEmpty()) {
            String value = super.get(searchTags.join(""))

            switch(true) {
            // found
                case value != null:
                    result << value
            // not found: give it up
                case searchTags.size() == 1:
                    nextSearchTags = nextSearchTags.drop(searchTags.size())
                    searchTags = nextSearchTags
                    break
            // not found: reduce searchTags and continue
                default:
                    searchTags = searchTags.dropRight(1)
            }
        }

        return result ? result.join("") : ""
    }
}

void RemoveOldTagAdapters(menuItem) {
    menuItem.actionListeners.findAll {
        // it instanceof TagAdapter // each time script executions generates different classes even if they share the same name
        it.class.name == TagAdapter.name
    }.each {
        menuItem.removeActionListener it
    }
}

void test(TagAdapter adapter) {
    console.println ">> Run all test cases."

    // OK: 1-to-1 tag adapting
    assert "<s1><a2>Lorem</a2> ipsum!</s1>" == adapter.adapt(
            "<s1><a2>Hello</s1> World!</a2>",         // Current Source
            "<s4><a5>Hello</s4> World!</a5>",         // Match Translation
            "<s4><a5>Lorem</a5> ipsum!</s4>")    // Current Translation

    // OK: source has no tags, all tags are removed from translation
    assert "Lorem ipsum!" == adapter.adapt(
            "Hello World!",
            "<s4></a4>Hello<s5> World!</a5>",
            "<s4><a5>Lorem</a5> ipsum!</s4>")

    // NG: different text, tag type un-match, all tags which do not exist in source are removed from translation
    assert "Lorem sit ipsum!" == adapter.adapt(
            "<s1><a2>Hello</s1> World!</a2>",
            "<s4></s4>Hello<a5> My World!</a5>",
            "<s4><a5>Lorem</a5> sit ipsum!</s4>")

    // NG: different text, tag name un-match, all tags which do not exist in source are removed from translation
    assert "Lorem sit ipsum!" == adapter.adapt(
            "<s1><a2>Hello</s1> World!</a2>",
            "<s4><a5>Hello</s5> My World!</a4>",
            "<s4><a5>Lorem</a5> sit ipsum!</s4>")

    // OK: same text, tag block replacing (1)
    assert "<s1><a2>Lorem</a2> ipsum!</s1>" == adapter.adapt(
            "<s1><a2>Hello</s1> World!</a2>",
            "<s4><a5>Hello</s4> World!</a5>",
            "<s4><a5>Lorem</a5> ipsum!</s4>")

    // OK: same text, tag block replacing (2)
    assert "<s1><a2></a2>Lorem ipsum!</s1>" == adapter.adapt(
            "<s1><a2>Hello</s1> World!</a2>",
            "<s4><a5><i6/>Hello</s4> World!</a5>",
            "<s4><a5><i6/></a5>Lorem ipsum!</s4>")

    // OK: same text, tag block replace but isolated tags
    // (i.e. they exists only in match source) will be removed
    assert "<s1><a2></a2>Lorem ipsum!</s1>" == adapter.adapt(
            "<s1><a2>Hello</s1> World!</a2>",
            "<s4><a5><i6/>Hello</s4> <i7/>World!</a5>",
            "<s4><a5><i6/></a5>Lorem <i7/>ipsum!</s4>")

    // Partially adaptation: same text, same numbers of tag block.
    // Some tag block is divided in current translation and
    // we can NOT trace their 1-to-1 paths.
    // Maybe you need to insert missing tags manually.
    assert "Lorem ipsum</a2>!</s1>" == adapter.adapt(
            "<s1><a2>Hello</a2> World!</s1>",
            "<s5><i6/><a7>Hello</a7> World!</s5>",
            "<s5><i6/>Lorem <a7>ipsum</a7>!</s5>")

    // OK: same text, same numbers of tag block.
    // Some tag block is divided in current translation and
    // we can trace their 1-to-1 paths.
    assert "<s1>Lorem <a2>ipsum</a2>!</s1>" == adapter.adapt(
            "<s1><a2>Hello</a2> World!</s1>",
            "<s5><a6>Hello</a6><i7/> World!</s5>",
            "<s5>Lorem <a6>ipsum</a6><i7/>!</s5>")

    console.println ">> Done. All green."
}
