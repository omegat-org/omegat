/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool
          with fuzzy matching, translation memory, keyword search,
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey and Maxym Mykhalchuk
               2008 Martin Fleurke, Alex Buloichik, Didier Briel
               2009 Didier Briel
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.filters3.xml;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import javax.xml.parsers.SAXParser;

import org.omegat.filters2.TranslationException;
import org.omegat.filters3.Attribute;
import org.omegat.filters3.Element;
import org.omegat.filters3.Entry;
import org.omegat.filters3.Tag;
import org.omegat.util.OStrings;
import org.omegat.util.StaticUtils;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ext.DeclHandler;
import org.xml.sax.ext.LexicalHandler;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The part of XML filter that actually does the job.
 * This class is called back by SAXParser.
 *
 * @author Maxym Mykhalchuk
 * @author Martin Fleurke
 * @author Didier Briel
 */
class Handler extends DefaultHandler implements LexicalHandler, DeclHandler
{
    private SAXParser parser;
    private Translator translator;
    private XMLDialect dialect;
    private File inFile;
    private String inEncoding;
    private File outFile;
    private String outEncoding;

    /** Main file writer to write translated text to. */
    private BufferedWriter mainWriter;
    /** Current writer for an external included file. */
    private BufferedWriter extWriter = null;

    /**
     * Returns current writer we should write into.
     * If we're in main file, returns {@link #mainWriter},
     * else (if we're writing external file) returns {@link #extWriter}.
     */
    private BufferedWriter currWriter()
    {
        if (extWriter!=null)
            return extWriter;
        else
            return mainWriter;
    }

    /** Currently parsed external entity that has its own writer. */
    private Entity extEntity = null;

    /** Current entry that collects normal text. */
    Entry entry;
    /** Stack of entries that collect out-of-turn text. */
    Stack<Entry> outofturnEntries = new Stack<Entry>();
    /** Current entry that collects the text surrounded by intact tag. */
    Entry intacttagEntry = null;
    /** Keep the attributes of an intact tag. */
    org.omegat.filters3.Attributes intacttagAttributes = null;
    /** Keep the attributes of paragraph tags. */
    Stack<org.omegat.filters3.Attributes> paragraphTagAttributes =
            new Stack<org.omegat.filters3.Attributes>();
    /** Keep the attributes of preformat tags. */
    Stack<org.omegat.filters3.Attributes> preformatTagAttributes =
            new Stack<org.omegat.filters3.Attributes>();

    /** Current entry that collects the text surrounded by intact tag. */
    String intacttagName = null;
    /** Names of possible paragraph tags. */
    Stack<String> paragraphTagName = new Stack<String>();
    /** Names of possible preformat tags. */
    Stack<String> preformatTagName = new Stack<String>();
    /** Name of the current variable translatable tag */
    Stack<String> translatableTagName = new Stack<String>();

    /** Now we collect out-of-turn entry. */
    private boolean collectingOutOfTurnText()
    {
        return !outofturnEntries.empty();
    }

    /** Now we collect intact text. */
    private boolean collectingIntactText()
    {
        return intacttagEntry!=null;
    }

    private boolean isTranslatableTag(){
        return !translatableTagName.empty();
    }

    /**
     * Returns current entry we collect text into.
     * If we collect normal text, returns {@link #entry},
     * else returns {@link #outofturnEntry}.
     */
    private Entry currEntry()
    {
        if (collectingIntactText())
            return intacttagEntry;
        else if (collectingOutOfTurnText())
            return outofturnEntries.peek();
        else
            return entry;
    }

    /**
     * External entities declared in source file.
     * Each entry is of type {@link Entity}.
     */
    private List<Entity> externalEntities = new ArrayList<Entity>();

    /**
     * Internal entities declared in source file.
     * A {@link Map} from {@link String}/entity name/ to {@link Entity}.
     */
    private Map<String,Entity> internalEntities = new HashMap<String, Entity>();
    /** Internal entity just started. */
    private Entity internalEntityStarted = null;

    /** Currently collected text is wrapped in CDATA section. */
    private boolean inCDATA = false;

    /** Whether we're curren */
    //private boolean inPreformattingTag = false;

    /** SAX parser encountered DTD declaration, so probably it will parse DTD
     * next, but some nice things may happen before. */
    private DTD dtd = null;
    /** SAX parser parses DTD -- we don't extract translatable text from there */
    private boolean inDTD = false;

    /**
     * External files this handler has processed, because
     * they were included into main file.
     * Each entry is of type {@link File}.
     */
    private List<File> processedFiles = new ArrayList<File>();
    /**
     * Returns external files this handler has processed, because
     * they were included into main file. Each entry is {@link File}.
     */
    public List<File> getProcessedFiles()
    {
        if (processedFiles.size()>0)
            return processedFiles;
        else
            return null;
    }

    /** Throws a nice error message when SAX parser encounders fastal error. */
    private void reportFatalError(SAXParseException e) throws SAXException, MalformedURLException, URISyntaxException
    {
        int linenum = e.getLineNumber();
        String filename;
        if (e.getSystemId()!=null)
        {
            File errorfile = new File(inFile.getParentFile(), localizeSystemId(e.getSystemId()));
            if (errorfile.exists())
                filename = errorfile.getAbsolutePath();
            else
                filename = inFile.getAbsolutePath();
        }
        else
            filename = inFile.getAbsolutePath();
        throw new SAXException("\n"+StaticUtils.format(e.getMessage()+"\n" +       // NOI18N
                OStrings.getString("XML_FATAL_ERROR"),
                new Object[] {filename, new Integer(linenum)}));
    }

    /**
     * Creates a new instance of Handler
     */
    public Handler(SAXParser parser, Translator translator, XMLDialect dialect,
            File inFile, String inEncoding, File outFile, String outEncoding)
            throws IOException
    {
        this.parser = parser;
        this.translator = translator;
        this.dialect = dialect;
        this.inFile = inFile;
        this.inEncoding = inEncoding;
        this.outFile = outFile;
        this.outEncoding = outEncoding;
        this.mainWriter = translator.createWriter(outFile, outEncoding);
    }

    private static final String START_JARSCHEMA = "jar:";                       // NOI18N
    private static final String START_FILESCHEMA = "file:";                     // NOI18N

    //////////////////////////////////////////////////////////////////////////
    // Utility methods
    //////////////////////////////////////////////////////////////////////////

    private String sourceFolderAbsolutePath = null;
    /** Returns source folder of the main file with trailing '/' (File.separator). */
    private String getSourceFolderAbsolutePath() {
        if (sourceFolderAbsolutePath == null) {
            String res = inFile.getAbsoluteFile().getParent();
            try {
                res = inFile.getCanonicalFile().getParent();
            } catch (IOException ex) {
            }
            if (res.charAt(res.length() - 1) != File.separatorChar) {
                res = res + File.separatorChar;
            }
            sourceFolderAbsolutePath = res;
        }
        return sourceFolderAbsolutePath;
    }

    /** Makes System ID not an absolute, but a relative one. */
    private String localizeSystemId(String systemId) throws URISyntaxException, MalformedURLException
    {
        if (systemId.startsWith(START_FILESCHEMA))
        {
            File thisOutFile=new File(new URL(systemId).toURI());
            String thisOutPath=thisOutFile.getAbsolutePath();

            if (thisOutPath.startsWith(getSourceFolderAbsolutePath())) {
                return thisOutPath.substring(getSourceFolderAbsolutePath().length());
            }
        }
        return systemId;
    }

    /** Whether the file with given systemId is in source folder. */
    private boolean isInSource(String systemId) throws URISyntaxException, MalformedURLException
    {
        if (systemId.startsWith(START_FILESCHEMA))
        {
            File thisOutFile=new File(new URL(systemId).toURI());
            if (thisOutFile.getAbsolutePath().startsWith(getSourceFolderAbsolutePath())) {
                return true;
            }
        }
        return false;
    }

    /** Finds external entity by publicId and systemId. */
    private Entity findExternalEntity(String publicId, String systemId)
    {
        if (publicId==null && systemId==null)
            return null;
        for (Entity entity : externalEntities)
        {
            if (entity.isInternal())
                continue;
            if (StaticUtils.equal(publicId, entity.getPublicId()) &&
                    StaticUtils.equal(systemId, entity.getSystemId()))
                return entity;
        }
        return null;
    }


    /**
     * Is called when the entity starts.
     * Tries to find out whether it's an internal entity, and if so,
     * turns on the trigger to queue entity, and not the text it represents,
     * in {@link #characters(char[],int,int)}.
     */
    private void doStartEntity(String name)
    {
        if (inDTD)
            return;
        internalEntityStarted = internalEntities.get(name);
    }

    /**
     * Is called when the entity is ended.
     * Tries to find out whether it's an external entity
     * we created a writer for, and if so,
     * closes the writer and nulls the entity.
     */
    private void doEndEntity(String name) throws SAXException, TranslationException, IOException
    {
        if (inDTD || extEntity==null)
            return;
        if (extEntity.getName().equals(name))
        {
            extEntity=null;
            translateAndFlush();
            extWriter.close();
            extWriter = null;
            mainWriter.write('&'+name+';');
        }
    }

    /** Resolves external entity and creates a new writer if it's an included file. */
    public InputSource doResolve(String publicId, String systemId) throws SAXException, TranslationException, IOException, URISyntaxException
    {
        if (dtd!=null &&
                StaticUtils.equal(publicId, dtd.getPublicId()) &&
                ( StaticUtils.equal(systemId, dtd.getSystemId()) ||
                  StaticUtils.equal(localizeSystemId(systemId), dtd.getSystemId()) ))
        {
            inDTD = true;
        }

        if ( systemId!=null &&
                (systemId.startsWith(START_JARSCHEMA) ||
                 systemId.startsWith(START_FILESCHEMA)) )
        {
            InputSource entity = new InputSource(systemId);
            // checking if f
            if (systemId.startsWith(START_FILESCHEMA))
            {
                if (!new File(new URI(systemId)).exists())
                    entity = null;
                }

            if (entity!=null)
            {
                if (!inDTD && outFile!=null && isInSource(systemId) && extEntity==null)
                {
                    extEntity = findExternalEntity(publicId, localizeSystemId(systemId));
                    if (extEntity!=null)
                    {
                        // if we resolved a new entity, and:
                        //  1. it's not a DTD
                        //  2. it's in project's source folder
                        //  3. it's not during project load
                        // then it's an external file, and we need to
                        // write it as an external file
                        translateAndFlush();
                        File extFile = new File(outFile.getParentFile(), localizeSystemId(systemId));
                        processedFiles.add(new File(inFile.getParent(), localizeSystemId(systemId)));
                        extWriter = translator.createWriter(extFile, outEncoding);
                        extWriter.write("<?xml version=\"1.0\"?>\n");           // NOI18N
                    }
                }
                return entity;
            }
            else
                return new InputSource(new java.io.StringReader(new String()));
        }
        else
        {
            InputSource source = dialect.resolveEntity(publicId, systemId);
            if (source!=null)
                return source;
            else
                return new InputSource(new java.io.StringReader(new String()));
        }
    }

    private void queueText(String s)
    {
        if (internalEntityStarted!=null && s.equals(internalEntityStarted.getValue()))
            currEntry().add(new XMLEntityText(internalEntityStarted));
        else
        {
            boolean added = false;
            if (currEntry().size()>0)
            {
                Element elem = currEntry().get(currEntry().size()-1);
                if (elem instanceof XMLText)
                {
                    XMLText text = (XMLText) elem;
                    if (text.isInCDATA()==inCDATA)
                    {
                        currEntry().remove(currEntry().size()-1);
                        currEntry().add(new XMLText(text.getText()+s, inCDATA));
                        added = true;
                    }
                }
            }
            if (!added)
                currEntry().add(new XMLText(s, inCDATA));
        }
    }
    private void queueTag(String tag, Attributes attributes)
    {
        Tag xmltag;
        XMLIntactTag intacttag = null;
        setTranslatableTag(tag, XMLUtils.convertAttributes(attributes));
        if (!collectingIntactText() &&
                isIntactTag(tag, XMLUtils.convertAttributes(attributes)))
        {
            intacttag = new XMLIntactTag(tag, getShortcut(tag), attributes);
            xmltag = intacttag;
            intacttagName = tag;
            intacttagAttributes = XMLUtils.convertAttributes(attributes);
        }
        else 
            xmltag = new XMLTag(tag, getShortcut(tag), Tag.TYPE_BEGIN, attributes);

            currEntry().add(xmltag);

        if (intacttag!=null)
            intacttagEntry = intacttag.getIntactContents();

        if (!collectingIntactText())
        {
                for (int i=0; i<xmltag.getAttributes().size(); i++)
                {
                    Attribute attr = xmltag.getAttributes().get(i);
                    if ((dialect.getTranslatableAttributes().contains(attr.getName())
                         ||
                         dialect.getTranslatableTagAttributes().containsPair(tag, attr.getName())
                        )
                       && dialect.validateTranslatableTagAttribute(tag, attr.getName(), xmltag.getAttributes() )
                       )
                    {
                        attr.setValue(translator.translate(attr.getValue()));
                    }
                }
            }
        }
    private void queueEndTag(String tag)
    {
        int len = currEntry().size();
        if (len>0 && (currEntry().get(len-1) instanceof XMLTag) &&
                (((XMLTag)currEntry().get(len-1)).getTag().equals(tag) &&
                ((XMLTag)currEntry().get(len-1)).getType()==Tag.TYPE_BEGIN) &&
                !isClosingTagRequired()) {
            ((XMLTag)currEntry().get(len-1)).setType(Tag.TYPE_ALONE);
        }
        else {
            currEntry().add(new XMLTag(tag, getShortcut(tag), Tag.TYPE_END, null));
        }
    }
    private void queueComment(String comment)
    {
        currEntry().add(new Comment(comment));
    }
    private void queueProcessingInstruction(String data, String target){
        currEntry().add(new ProcessingInstruction(data, target));
    }

    private void queueDTD(DTD dtd)
    {
        currEntry().add(dtd);
    }

    /** Is called when the tag is started. */
    private void start(String tag, Attributes attributes) throws SAXException, TranslationException
    {
        if (isOutOfTurnTag(tag))
        {
            XMLOutOfTurnTag ootTag = new XMLOutOfTurnTag(tag, getShortcut(tag), attributes);
            currEntry().add(ootTag);
            outofturnEntries.push(ootTag.getEntry());
        }
        else {
            if (isParagraphTag(tag, XMLUtils.convertAttributes(attributes)) &&
                !collectingOutOfTurnText() && !collectingIntactText()) {
                    translateAndFlush();
            }
            queueTag(tag, attributes);
        }
    }

    /** Is called when the tag is ended. */
    private void end(String tag) throws SAXException, TranslationException
    {
        if (collectingIntactText() && isIntactTag(tag, null)
                && tag.equals(intacttagName))
        {
            intacttagEntry = null;
            intacttagName = null;
            intacttagAttributes = null;
        }
        else if (collectingOutOfTurnText() && isOutOfTurnTag(tag))
        {
            translateButDontFlash();
            outofturnEntries.pop();
        }
        else
        {
            queueEndTag(tag);
            if (isParagraphTag(tag) && !collectingOutOfTurnText()
                && !collectingIntactText())
                translateAndFlush();
        }
        removeTranslatableTag();
    }

    /**
     * One of the main methods of the XML filter:
     * it collects all the data, adjusts it, and sends for translation.
     * @see #translateAndFlush()
     */
    private void translateButDontFlash() throws TranslationException
    {
        if (currEntry().size()==0)
            return;

        String src = currEntry().sourceToShortcut();
        Element lead = currEntry().get(0);
        String translation = src;
        if ( (lead instanceof Tag) &&
             isPreformattingTag(((Tag)lead).getTag(),
             ((Tag)lead).getAttributes()) &&
             isTranslatableTag()
           ) {
            translation = translator.translate(src);
        } else {
            String compressed = StaticUtils.compressSpaces(src);
            if (isTranslatableTag())
                translation = translator.translate(compressed);
            // untranslated is written out uncompressed
            if( compressed.equals(translation) )
                translation = src;
        }

        currEntry().setTranslation(translation);
    }

    /**
     * One of the main methods of the XML filter:
     * it collects all the data, adjusts it, sends for translation,
     * writes out the translated data and clears the entry.
     * @see #translateButDontFlash()
     */
    private void translateAndFlush() throws SAXException, TranslationException
    {
        translateButDontFlash();
        try
        {
            currWriter().write(currEntry().translationToOriginal());
        }
        catch(IOException e)
        {
            throw new SAXException(e);
        }
        currEntry().clear();
    }

    ///////////////////////////////////////////////////////////////////////////
    // Dialect Helper methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Returns whether the tag starts a new paragraph.
     * Preformatting tags are also considered to start a new paragraph,
     * so if {@link #isPreformattingTag(String)} returns true,
     * this method will also return true.
     */
    private boolean isParagraphTag(String tag,
                                   org.omegat.filters3.Attributes atts) {
        paragraphTagName.push(tag);
        paragraphTagAttributes.push(atts);
        preformatTagName.push(tag);
        preformatTagAttributes.push(atts);

        if ((dialect.getParagraphTags()!=null &&
             dialect.getParagraphTags().contains(tag))
            || isPreformattingTag(tag, atts))
            return true;
        else 
            return dialect.validateParagraphTag(tag, atts);
    }

    /**
     * Returns whether the tag starts a new paragraph.
     * It is called at the end of an element (&lt;/mrk&gt;), and thus
     * doesn't provide attributes. Those are restored from the
     * paragraphTagAttributes stack.
     * @param tag A tag
     * @return <code>true</code> or <code>false</false>
     */
    private boolean isParagraphTag(String tag) {
        if ((dialect.getParagraphTags()!=null &&
                 dialect.getParagraphTags().contains(tag))
                || isPreformattingTag(tag))
            return true;
        else {
            org.omegat.filters3.Attributes atts = null;
            if (tag.equals(paragraphTagName.lastElement())) {
                paragraphTagName.peek();
                atts = paragraphTagAttributes.peek(); // Restore attributes
            }
            return dialect.validateParagraphTag(tag, atts);
        }
    }

    /** Returns whether the tag surrounds preformatted block of text.
     * @param tag A tag
     * @return <code>true</code> or <code>false</false>
     */
    private boolean isPreformattingTag(String tag, 
                                       org.omegat.filters3.Attributes atts) {
        if (dialect.getPreformatTags()!=null &&
            dialect.getPreformatTags().contains(tag))
            return true;
        else
            return dialect.validatePreformatTag(tag, atts);
    }

   /** Returns whether the tag surrounds preformatted block of text.
     * It is called at the end of an element (&lt;/mrk&gt;), and thus
     * doesn't provide attributes. Those are restored from the
     * preformatTagAttributes stack.
     * @param tag A tag
     * @return <code>true</code> or <code>false</false>
     */
    private boolean isPreformattingTag(String tag) {
        if (dialect.getPreformatTags()!=null &&
            dialect.getPreformatTags().contains(tag))
            return true;
        else {
            org.omegat.filters3.Attributes atts = null;
            if (tag.equals(preformatTagName.lastElement())) {
                preformatTagName.peek();
                atts = preformatTagAttributes.peek(); // Restore attributes
            }
            return dialect.validatePreformatTag(tag, atts);
        }

    }

    /** Returns whether the tag surrounds intact block of text which we 
     * shouldn't translate.
     */
    private boolean isIntactTag(String tag, org.omegat.filters3.Attributes atts)
    {
        if (dialect.getIntactTags()!=null &&
            dialect.getIntactTags().contains(tag))
            return true;
        else {
            if (atts == null) {
                if (tag.equals(intacttagName))
                   atts = intacttagAttributes; // Restore attributes
            }

            return dialect.validateIntactTag(tag, atts);
        }
    }

    /**
     * If we are not inside a translatable tag, and if the dialect says
     * the new one is translatable, add the new tag to the stack
     * @param tag The current opening tag
     * @param atts The attributes of the current tag
     */
    // TODO: The concept works only perfectly if the first tag with 
    // translatable content inside the translatable tag is a paragraph
    // tag
    void setTranslatableTag(String tag,
            org.omegat.filters3.Attributes atts) {
        
        if ( !isTranslatableTag() ) { // If stack is empty
             if (dialect.validateTranslatableTag(tag, atts) )
                 translatableTagName.push(tag);
        } else {
            translatableTagName.push(tag);
        }
    }

    /**
     * Remove a tag from the stack of translatable tags
     */
    void removeTranslatableTag() {
        if ( isTranslatableTag() ) // If there is something in the stack
            translatableTagName.pop(); // Remove it
    }


    private boolean isClosingTagRequired(){
        return dialect.getClosingTagRequired();
    }

    /** Returns whether we face out of turn tag we should collect separately. */
    private boolean isOutOfTurnTag(String tag)
    {
        return dialect.getOutOfTurnTags()!=null && dialect.getOutOfTurnTags().contains(tag);
    }

    /** Returns a shortcut for a tag. Queries dialect first, else returns null. */
    private String getShortcut(String tag)
    {
        if (dialect.getShortcuts()!=null)
            return dialect.getShortcuts().get(tag);
        else
            return null;
    }

    //////////////////////////////////////////////////////////////////////////
    // Callback methods
    //////////////////////////////////////////////////////////////////////////

    /**
     * Resolves an external entity.
     */
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException
    {
        try
        {
            return doResolve(publicId, systemId);
        }
        catch (URISyntaxException e)
        {
            throw new SAXException(e);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
        catch (TranslationException e)
        {
            throw new SAXException(e);
        }
    }

    /** Receive notification of the start of an element. */
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        try
        {
            start(qName, attributes);
        }
        catch (TranslationException e)
        {
            throw new SAXException(e);
        }
    }


    /** Receive notification of the end of an element. */
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        try
        {
            end(qName);
        }
        catch (TranslationException e)
        {
            throw new SAXException(e);
        }
    }

    /** Receive notification of character data inside an element. */
    public void characters(char[] ch, int start, int length) throws SAXException
    {
        if (inDTD)
            return;
        queueText(new String(ch, start, length));
    }
    /** Receive notification of ignorable whitespace in element content. */
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException
    {
        if (inDTD)
            return;
        queueText(new String(ch, start, length));
    }
    /** Receive notification of an XML comment anywhere in the document. */
    public void comment(char[] ch, int start, int length) throws SAXException
    {
        if (inDTD)
            return;
        queueComment(new String(ch, start, length));
    }

    /** Receive notification of an XML processing instruction
     * anywhere in the document.
     */
    public void processingInstruction(String target, String data) throws SAXException {
        if (inDTD)
            return;
        queueProcessingInstruction(target, data);
    }
    /** Receive notification of the beginning of the document. */
    public void startDocument() throws SAXException
    {
        try
        {
            mainWriter.write("<?xml version=\"1.0\"?>\n");                      // NOI18N
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }

        entry = new Entry();
    }
    /** Receive notification of the end of the document. */
    public void endDocument() throws SAXException
    {
        try
        {
            translateAndFlush();
            if (extWriter!=null)
            {
                extWriter.close();
                extWriter = null;
            }
            translateAndFlush();
            currWriter().close();
        }
        catch (TranslationException e)
        {
            throw new SAXException(e);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Report a fatal XML parsing error.
     * Is used to provide feedback.
     */
    public void fatalError(org.xml.sax.SAXParseException e) throws SAXException
    {
        try {
            reportFatalError(e);
        } catch (MalformedURLException ex) {
            throw new SAXException(ex);
        } catch (URISyntaxException ex) {
            throw new SAXException(ex);
        }
    }

    /**
     * Report the start of DTD declarations, if any.
     */
    public void startDTD(String name, String publicId, String systemId) throws SAXException
    {
        dtd = new DTD(name, publicId, systemId);
    }

    /**
     * Report the end of DTD declarations.
     * Queues the DTD declaration with all the entities declared.
     */
    public void endDTD() throws SAXException
    {
        queueDTD(dtd);
        inDTD = false;
        dtd = null;
    }

    /**
     * Report the start of a CDATA section.
     */
    public void startCDATA() throws SAXException
    {
        inCDATA = true;
    }
    /**
     * Report the end of a CDATA section.
     */
    public void endCDATA() throws SAXException
    {
        inCDATA = false;
    }

    /** Not used: Report the beginning of some internal and external XML entities. */
    public void startEntity(String name) throws SAXException
    {
        doStartEntity(name);
    }

    /**
     * Report the end of an entity.
     *
     * @param name The name of the entity that is ending.
     * @exception SAXException The application may raise an exception.
     * @see #startEntity
     */
    public void endEntity(String name) throws SAXException
    {
        try
        {
            doEndEntity(name);
        }
        catch (IOException e)
        {
            throw new SAXException(e);
        }
        catch (TranslationException e)
        {
            throw new SAXException(e);
        }
    }

    /**
     * Report an internal entity declaration.
     */
    public void internalEntityDecl(String name, String value) throws SAXException
    {
        if (inDTD)
            return;
        Entity entity = new Entity(name, value);
        internalEntities.put(name, entity);
        dtd.addEntity(entity);
    }

    /**
     * Report a parsed external entity declaration.
     */
    public void externalEntityDecl(String name, String publicId, String systemId) throws SAXException
    {
        if (inDTD)
            return;
        try {
            Entity entity = new Entity(name, publicId,
                    localizeSystemId(systemId));
            if (isInSource(systemId))
                externalEntities.add(entity);
            dtd.addEntity(entity);
        } catch (MalformedURLException ex) {
            throw new SAXException(ex);
        } catch (URISyntaxException ex) {
            throw new SAXException(ex);
        }
    }

    ///////////////////////////////////////////////////////////////////////////
    // unused callbacks
    ///////////////////////////////////////////////////////////////////////////

    /** Not used: An element type declaration. */
    public void elementDecl(String name, String model) { }

    /** Not used: An attribute type declaration. */
    public void attributeDecl(String eName, String aName, String type, String valueDefault, String value) {}
}
