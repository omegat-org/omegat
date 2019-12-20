/* :name=Tagwipe :description=Remove tags from docx documents
 *
 * This is a Groovy port of the DGT-OmegaT tagwipe.pl (version 20170914)
 * http://185.13.37.79/?q=node/35
 *
 * It converts all docx documents in the source file and create new one (with
 * the extension .out.docx).
 *
 * @author  Briac Pilpré (groovy port of the original perl script, new GUI)
 * @author  Thomas Cordonnier (original perl version with Windows and Linux wrappers)
 * @author  Kos Ivantsov (minor fixes, L10N-ability)
 * @date    2018-04-17
 * @version 0.1.5
 */
import java.util.zip.ZipFile
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import java.util.regex.Pattern
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import org.omegat.core.Core
import org.omegat.util.Preferences
import org.omegat.util.StaticUtils
import org.omegat.util.StringUtil

import static javax.swing.JOptionPane.*
import static org.omegat.util.Platform.*

utils = (StringUtil.getMethods().toString().findAll("format")) ? StringUtil : StaticUtils

title = res.getString("name")

String.metaClass.alert = { ->
        showMessageDialog null, delegate, title, INFORMATION_MESSAGE
        false
}

prop = project.projectProperties
if (!prop) {
    message = res.getString("noProject")
    console.clear()
    console.println(title + "\n" + "="*title.size() + "\n" + message)
    message.alert()
    return
}

if (project.getAllEntries().isEmpty()) {
    message = res.getString("emptyProject")
    console.clear()
    console.println(title + "\n" + "="*title.size() + "\n" + message)
    message.alert()
    return
}

new TagwipeDialog(console, project, editor, res, utils).setVisible(true)

class TagwipeDialog extends javax.swing.JFrame {

def beautify = false
def level = 1

// If true, convert all the Docx in the project, otherwise just the current one.
def convertAll = true

// If false, no backup of the original files are saved.
def backupFiles = true

// Debug print each Word paragraph before/after cleanup.
def debug = false
def tagwipeDir = null

def prop = null
def console = null
def project = null
def editor = null
def res
def utils
def preferences

def levelDescriptions = [
    res.getString("lvl0"),
    res.getString("lvl1"),
    res.getString("lvl2"),
    res.getString("lvl3"),
    res.getString("lvl4"),
    res.getString("lvl5"),
    res.getString("lvl6"),
    res.getString("lvl7"),
    res.getString("lvl8"),
    res.getString("lvl9")
    ]


    private void buttonTagwipeActionPerformed(java.awt.event.ActionEvent evt) {
        prop = project.projectProperties

        if (backupFiles) {
            // Directory where the original files will be backuped
            tagwipeDir = new File(prop.getProjectRoot(), "tagwipe")
            tagwipeDir.mkdirs()
        }

       console.clear()
       console.println(res.getString("name") + "\n" + '='*res.getString("name").size() )
       console.println(res.getString("selOptions"))
       console.println("  beautify: " + beautify)
       console.println("  level: " + level)
       console.println("  backupFiles: " + backupFiles)
       console.println("  convertAll: " + convertAll)
       console.println("")
       tagWipeAction()
    }


def tagWipeAction() {

    def files
    if (convertAll) {
        files = project.projectFiles
    }
    else {
        files = project.projectFiles.subList(editor.@displayedFileIndex, editor.@displayedFileIndex + 1)
    }

    def selfiles = files.filePath.grep(~/(?i).*\.docx$/)
    def filemsg

    if ( selfiles.size() == 0) {
    	   filemsg = "noDocx"
    } else {
        filemsg = ( selfiles.size() == 1 ) ? "curDocx" : "allDocx"
    }
    console.println(res.getString(filemsg))

    for (i in 0 ..< files.size()) {
      def projectFile = files[i]
      if (projectFile.filePath.matches(/(?i).*(?<!\.out)\.docx$/)) {
        def inputFile = new File(prop.getSourceRoot() + "/" + projectFile.filePath)
        console.println(utils.format(res.getString("runTagwipe"), inputFile))
        tagwipe(inputFile.getAbsolutePath())

        moveDocx(projectFile.filePath)
        console.println("")
      }
    }

    savePreferences()
    if (filemsg == "noDocx" ) {
        org.omegat.core.Core.mainWindow.statusLabel.setText(res.getString(filemsg))
        Timer timer = new Timer().schedule({
            org.omegat.core.Core.mainWindow.statusLabel.setText(null)
        } as TimerTask, 10000)
        dispose()
    } else {
        console.println(res.getString("finish"))
        org.omegat.core.Core.mainWindow.statusLabel.setText(res.getString("finish"))
        Timer timer = new Timer().schedule({
            org.omegat.core.Core.mainWindow.statusLabel.setText(null)
            org.omegat.gui.main.ProjectUICommands.projectReload()
        } as TimerTask, 500)
        dispose()
    }
}

def tagwipe(inputFile) {
    def infomsg
    def outDocx = inputFile.replaceAll(/(?i)\.(docx)$/, '.out.$1')

    infomsg = utils.format(res.getString("openFile"), inputFile)
    console.println(infomsg)
    def zipIn  = new ZipFile(inputFile)

    infomsg = utils.format(res.getString("createOut"), outDocx)
    console.println(infomsg)
    def zipOut = new ZipOutputStream(new FileOutputStream(outDocx))

    for (ZipEntry entry : zipIn.entries()) {
        // Only clean endnotes, footer and document files
        if (! entry.getName().matches(/.*\b(endnotes|foot.+?|document).xml$/)) {
            // Just copy the file as is
            zipOut.putNextEntry(new ZipEntry(entry.getName()))
            def is = zipIn.getInputStream(entry)
            def buf = new byte[1024]
            def len
            while((len = is.read(buf)) >= 0) {
                zipOut.write(buf, 0, len)
            }
        }
        else {
        	  infomsg = utils.format(res.getString("cleanEntry"), entry.getName())
            console.println(infomsg)

            def is = zipIn.getInputStream(entry)
            def br = new InputStreamReader(is, 'UTF-8')

            StringBuilder sb = new StringBuilder()
                br.eachLine(0) { line ->
                    def paras = line.split("(?<=</w:p>)")
                    paras.each { p ->
                        if (debug) console.println("IN\t$p")

                        p = after(transform(before(p)))

                        if (debug) console.println("OUT\t$p")

                        sb.append(p)
                    }
                }

            zipOut.putNextEntry(new ZipEntry(entry.getName()))
            zipOut << sb.toString().getBytes('UTF8')
            zipOut.closeEntry()
        }
    }

    zipIn.close()
    zipOut.close()
}

def moveDocx(inputFile) {
    def infomsg
    def outDocx = inputFile.replaceAll(/(?i)\.(docx)$/, '.out.$1')

    def oldDocx = new File(prop.getSourceRoot() + "/" + inputFile).getAbsolutePath()
    def cleanDocx = new File(prop.getSourceRoot() + "/" + outDocx)

    if (backupFiles) {
      def backupDocx = new File(tagwipeDir.getAbsolutePath() + "/" + inputFile)
      backupDocx.getParentFile().mkdirs()
      infomsg=utils.format(res.getString("moveOrig"), oldDocx, backupDocx)
      console.println(infomsg)
      Files.move(Paths.get(oldDocx), Paths.get(backupDocx.getAbsolutePath()), StandardCopyOption.REPLACE_EXISTING)
    }

    infomsg=utils.format(res.getString("moveClean"), cleanDocx, oldDocx)
    console.println(infomsg)
    Files.move(Paths.get(cleanDocx.getAbsolutePath()), Paths.get(oldDocx), StandardCopyOption.REPLACE_EXISTING)
}

def  before(p) {

    def skip
    skip = 1
    if ( p =~ /<wps:txbx>/ ) {
        skip = true
    }
    else if ( p =~ /<\/wps:txbx>/ ) {
        skip = false
    }

//  if (level >= 2) {
//      next if m{<w:pStyle w:val="TOC[^"]+"/>}                        // suppress TOC
//  }

    p = p.replaceAll(/(\n|\t)+</, "<")                                 // flatten file
    p = p.replaceAll(/<w:r [^>]*>/, "<w:r>")                           // flat runs
    p = p.replaceAll(/<w:t [^>]*>/, "<w:t>")                           // flat text

    // protect http://www.liquid-technologies.com/XML/EscapingData.aspx
    p = p.replaceAll(/(?m)&(lt|gt|amp);/, '&$1#')

    // clean ...
    // http://www.datypic.com/sc/ooxml/e-w_rPr-2.html

    if (level >= 0) {
        p = p.replaceAll(/<w:lastRenderedPageBreak\/>/, "")
    }
    if (level >= 1 ) {
        //_clean suppress only inside "run"
        p = _clean(p, '<w:snapToGrid w:val="0"/>')
        p = _clean(p, '<w:szCs w:val="24"/>')                          // suppress Font Size when 24 (=default?)
        p = _clean(p, '<w:sz w:val="24"/>')                            // suppress Font Size when 24 (=default?)
        p = _clean(p, '<w:kern w:val="\\d+"/>')                        // suppress kerning attribute
        p = _clean(p, '<w:color w:val="000000"/>')                     // suppress color when black (or default)
        p = _clean(p, '<w:color w:val="auto"/>')                       // suppress color when "auto"

        p = _clean(p, '<w:u w:color="000000"/>')                       // suppress color when black (or default)
        p = _clean(p, '<w:u w:color="auto"/>')                         // suppress color when "auto"

        p = _clean(p, '<w:bdr w:val="nil"/>')

        p = _clean(p, '<w:w w:val=".."/>')                             // suppress compressed or expanded text
//      p = _clean(p, '<w:rFonts w:eastAsia="[^>]+"/>')                // avoid asian attribute for fonts
        p = _clean(p, '<w:spacing w:val="[^"]+"/>')                    // suppress space spacing

        p = p.replaceAll(/(?m)<[^<]+?w:val="nil"\/>/, "")

        p = p.replaceAll(/<w:lang[^>]*?>/, "")
        p = p.replaceAll(/<w:noProof\/>/, "")
        p = p.replaceAll(/<w:proofErr w:type="\w+?"\/>/,"")            // avoid proofing error definition
        p = p.replaceAll(/s\{ w:rsidR="[^"]+"/, "")                    // remove revision IDs
        p = p.replaceAll(/ w:rsidRPr="[^"]+"/, "")                     //
        p = p.replaceAll(/ w:rsidP="[^"]+"/, "")                       //
        p = p.replaceAll(/><\/w:t>/, "> </w:t>")                       // add a space into a blank text

        p = p.replaceAll(/(?m)<w:rStyle w:val="(hps|x\d+)"\/>/, "")    // suppress referenced char styles "hps" or "x1" ... "x9999"
        p = p.replaceAll(/(?m)<w:bookmark(Start|End)[^<>]+?\/>/, "")

    }
    if (level >= 2) {
        //p = p.replaceAll(/<w:rFonts[^<>]+?\/>/, "")                  // suppress Font attribute

        p = p.replaceAll(/ w:eastAsia="[^>]+"/, "")
        p = p.replaceAll(/ w:cs="[^>]+"/, "")
        p = p.replaceAll(/<w:szCs[^<>]+>/, "")
        p = p.replaceAll(/<w:bCs\/>/, "")
        p = p.replaceAll(/<w:iCs\/>/, "")

        p = p.replaceAll(/<w:noBreakHyphen\/>/, "<w:t>-</w:t>")        // replace non breaking Hyphen with normal "-"
        p = p.replaceAll(/<w:softHyphen\/>/, "")                       // replace Soft Hyphen with nothing
        p = p.replaceAll(/(?m)<w:smartTag[^>]+>/, "")                  // suppress SmartTags
        p = p.replaceAll(/(?m)<\/w:smartTag>/, "")
    }
    if (level >= 3) {
        p = p.replaceAll(/(?m)<w:comment[^<>]+>/, "")                  // suppress Comments
        p = p.replaceAll(/(?m)<w:rStyle w:val="CommentReference"\/>/, "")
    }
    if (level >= 4) {
        p = p.replaceAll(/(?m)<w:hyperlink[^<>]+>/, "")                // suppress Hyperlinks (transform they to normal text)
        p = p.replaceAll(/(?m)<\/w:hyperlink>/, "")
    }
    if (level >= 5) {
        p = _clean(p, '<w:vanish/>')                                   // suppress hidden text
        p = _clean(p, '<w:shadow/>')                                   // suppress shadow text
        p = _clean(p, '<w:color[^<>]+>')                               // suppress every color in text
        p = _clean(p, '<w:highlight w:val="[^"]+?"/>')                 // suppress highlight
    }
    if (level >= 6) {
        p = _clean(p, '<w:rFonts[^<>]+?/>')                            // avoid attribute for fonts
        p = _clean(p, '<w:sz[^<>]+?/>')                                // suppress Font Size
    }
    if (level >= 7) {
        p = p.replaceAll(/<w:rPr>(?:(?:(?!w:rStyle).)*?)<\/w:rPr>/, "")     // remove everything is not rStyle
//      s{<w:r>(?:(?:(?!<w:t>).)*?)<w:t>}{<w:r><w:t>}g
//      s{</w:r>(?:(?:(?!<w:r>).)*?)<w:r>}{</w:r><w:r>}g
    }
    if (level >= 8) {
        p = p.replaceAll(/<w:rPr>(?:(?:(?!<\/w:rPr>).)*?)<\/w:rPr>/, "")   // remove every attribute
    }

    p = p.replace(/<w:rPr><\/w:rPr>/, "")                              // suppress empty run properties
    p = p.replace(/(?m)<w:r><\/w:r>/, "")                              // supress empty runs

//  // normalize ... <w:br/> and <w:tab/> have their own run
//  s{(<w:r>(?:(?!<w:t>).)*)(<w:br/>|<w:tab/>)<w:t>}{$1$2</w:r>$1<w:t>}g if $skip == 0

    p = p.replaceAll(/<w:r><w:tab\/><\/w:r><w:r><w:rPr><w:rStyle w:val="FootnoteReference"\/><\/w:rPr><w:footnoteRef\/><\/w:r>/,
        "<w:r><w:tab/></w:r>")

    return p
}


def transform(p) {
//  p = p.replaceAll(/<\/w:t><\/w:r><w:r><w:noBreakHyphen\/><\/w:r>/, "-</w:t></w:r>")

    p = p.replaceAll(/<w:noBreakHyphen\/><w:t>/, "<w:t>-")             //replace non breaking Hyphen with normal "-"
    p = p.replaceAll(/<w:softHyphen\/>/,"")                            //replace Soft Hyphen with nothing

    //compress tags
    def rxCompressTags = /.*(<w:r>(?:<w:rPr>(?:<w:[^>]+>)*<\/w:rPr>)?<w:t>)([^>]+)(<\/w:t><\/w:r>)\s*(\1).*/
    while ( p.matches(rxCompressTags) ) {
        p = p.replaceAll(/(<w:r>(?:<w:rPr>(?:<w:[^>]+>)*<\/w:rPr>)?<w:t>)([^>]+)(<\/w:t><\/w:r>)\s*(\1)/, '$1$2')
    }

    //next line moves footnote reference before [\s.!?;:]
    p = p.replaceAll(
        /([.!?;:]\s*(<\/w:t><\/w:r>))\s*(<w:r><w:rPr><w:rStyle w:val="FootnoteReference"\/>(<[^>]+?>)*<\/w:rPr><w:footnoteReference w:id="[^"]+?"\/><\/w:r>)/,
        '$2$3<w:r><w:t>$1'
    )

    //next line moves footnotes surounded by brackets [] {} () before [\s.!?;:]
    p = p.replaceAll(
        /([\s;:!?.])([\[\(\{]<\/w:t><\/w:r>\s*<w:r><w:rPr><w:rStyle w:val="FootnoteReference"\/>(?:<[^>]+?>)*<\/w:rPr><w:footnoteReference w:id="[^"]+?"\/><\/w:r>\s*<w:r>.+?<w:t[^>]*>[\]\)\}])/,
        '$2$1'
    )

//  while ( s{</w:t></w:r><w:r><w:t>([ ,.?!:"]+)</w:t>}{$1<\/w:t>}mx ) {}  //attach ([ ,.?!:"] to preceding run

    return p
}


def after(p) {
    p = p.replaceAll(/<w:t>/, '<w:t xml:space="preserve">')
    p = p.replaceAll(/(<w:document)/, '\n$1')

    // unprotect http://www.liquid-technologies.com/XML/EscapingData.aspx
    p = p.replaceAll(/&(lt|gt|amp)#/, '&$1;')

    if (beautify) {
        p = p.replaceAll(/(<w:p |<w:p>|<\/w:p>)/, '\n$1')
            .replaceAll(/(<w:r>|<w:r w[^<>]+?>)/, '\n\t$1')
            .replaceAll(/(<w:body>)/, '\n$1')
            .replaceAll(/(<.w:docu[^<>]+?>)/, '\n$1')
            .replaceAll(/(<.w:body>)/, '\n$1')
            .replaceAll(/<\/w:tc><w:tc>/, '\n</w:tc>\n<w:tc>')
            .replaceAll(/(<\/w:tc><\/w:tr>)/, '\n$1\n')
            .replaceAll(/(<w:tbl>)/, '\n$1')
            
    }

    return p
}

def _clean(p, pat) {
    return Pattern
        .compile("(?m)(<w:r><w:rPr>.*?)$pat(.*?</w:rPr>)")
        .matcher(p)
        .replaceAll('$1$2')
}

    private javax.swing.JButton buttonCancel
    private javax.swing.JButton buttonTagwipe
    private javax.swing.ButtonGroup buttongroupConvert
    private javax.swing.JCheckBox checkboxBackup
    private javax.swing.JCheckBox checkboxBeautify
    private javax.swing.JLabel labelBackup
    private javax.swing.JLabel labelBeautify
    private javax.swing.JLabel labelConvert
    private javax.swing.JLabel labelLevel
    private javax.swing.JLabel labelLevelDescription
    private javax.swing.JLabel labelLevelSelected
    private javax.swing.JLabel labelTitle
    private javax.swing.JRadioButton radioAllDocx
    private javax.swing.JRadioButton radioCurrentDocx
    private javax.swing.JSlider sliderLevel

    public TagwipeDialog(Object console, Object project, Object editor, Object res, Object utils) {
        this.project = project
        this.console = console
        this.editor = editor
        this.res = res
        this.utils = utils

        preferences = new java.util.Properties()

        loadPreferences()

        initComponents()

        labelLevelDescription.setVisible(false)
    }


    private loadPreferences() {
      def currentDir = Preferences.getPreference(Preferences.SCRIPTS_DIRECTORY)
      def prefsFile = new File(currentDir + "/properties/tagwipe.prefs.properties")

      if (! prefsFile.exists() ) {
          return
      }

     try {
          prefsFile.withInputStream { 
              stream -> preferences.load(stream) 
          }
      }
      catch (IOException e) {
      	console.println("Error when loading preferences: " + e.message);
      }

      beautify = Boolean.parseBoolean(preferences["beautify"])
      level = Integer.parseInt(preferences["level"])
      convertAll = Boolean.parseBoolean(preferences["convertAll"])
      backupFiles = Boolean.parseBoolean(preferences["backupFiles"])
      debug = Boolean.parseBoolean(preferences["debug"])
    }

    private savePreferences() {
      def currentDir = Preferences.getPreference(Preferences.SCRIPTS_DIRECTORY)
      preferences.setProperty("beautify", ""+beautify)
      preferences.setProperty("level", ""+level)
      preferences.setProperty("convertAll", ""+convertAll)
      preferences.setProperty("backupFiles", ""+backupFiles)
      preferences.setProperty("debug", ""+debug)

      try {
          preferences.store(new File(currentDir + "/properties/tagwipe.prefs.properties").newWriter(), " Tagwipe preferences")
      }
      catch (IOException e) {
      	console.println("Error when storing preferences: " + e.message);
      }
    }

    private void initComponents() {
        setTitle("Tagwipe")

        buttongroupConvert = new javax.swing.ButtonGroup()
        labelTitle = new javax.swing.JLabel()
        labelLevel = new javax.swing.JLabel()
        labelBeautify = new javax.swing.JLabel()
        labelConvert = new javax.swing.JLabel()
        labelBackup = new javax.swing.JLabel()
        checkboxBeautify = new javax.swing.JCheckBox()
        sliderLevel = new javax.swing.JSlider()
        labelLevelSelected = new javax.swing.JLabel()
        radioAllDocx = new javax.swing.JRadioButton()
        radioCurrentDocx = new javax.swing.JRadioButton()
        checkboxBackup = new javax.swing.JCheckBox()
        buttonTagwipe = new javax.swing.JButton()
        buttonCancel = new javax.swing.JButton()
        labelLevelDescription = new javax.swing.JLabel()

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE)

        labelTitle.setFont(labelTitle.getFont().deriveFont(labelTitle.getFont().getStyle() | java.awt.Font.BOLD))
        labelTitle.setText(res.getString("name"))

        labelLevel.setText(res.getString("lblLevel"))

        labelBeautify.setText(res.getString("lblBeautify"))

        labelConvert.setText(res.getString("lblConvert"))

        labelBackup.setText(res.getString("lblBackup"))

        checkboxBeautify.setText(res.getString("btnBeautify"))
        checkboxBeautify.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkboxBeautifyStateChanged(evt)
            }
        })
        checkboxBeautify.setSelected(beautify)

        sliderLevel.setMaximum(8)
        sliderLevel.setPaintTicks(true)
        sliderLevel.setSnapToTicks(true)
        sliderLevel.setValue(level)
        sliderLevel.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderLevelStateChanged(evt)
            }
        })

        labelLevelSelected.setText(Integer.toString(level))

        buttongroupConvert.add(radioAllDocx)
        radioAllDocx.setSelected(true)
        radioAllDocx.setText(res.getString("btnAllDocx"))
        radioAllDocx.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                radioAllDocxStateChanged(evt)
            }
        })

        buttongroupConvert.add(radioCurrentDocx)
        radioCurrentDocx.setText(res.getString("btnCurDocx"))

        radioAllDocx.setSelected(convertAll)
        radioCurrentDocx.setSelected(!convertAll)

        checkboxBackup.setSelected(backupFiles)
        checkboxBackup.setText(res.getString("btnBackup"))
        checkboxBackup.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                checkboxBackupStateChanged(evt)
            }
        })

        buttonTagwipe.setText(res.getString("lblName"))
        buttonTagwipe.setToolTipText("")
        buttonTagwipe.setMinimumSize(new java.awt.Dimension(60, 25))
        buttonTagwipe.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonTagwipeActionPerformed(evt)
            }
        })

        buttonCancel.setText(res.getString("btnCancel"))
        buttonCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCancelActionPerformed(evt)
            }
        })
        labelLevelDescription.setText(levelDescriptions[level])

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane())
        getContentPane().setLayout(layout)
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(33, 33, 33)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(labelConvert)
                                    .addComponent(labelBackup))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(checkboxBackup)
                                            .addComponent(radioCurrentDocx))
                                        .addGap(1, 201, Short.MAX_VALUE))
                                    .addComponent(radioAllDocx, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelBeautify)
                                .addGap(18, 18, 18)
                                .addComponent(checkboxBeautify, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(labelLevel)
                                .addGap(34, 34, 34)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(sliderLevel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(labelLevelSelected)
                                        .addGap(34, 34, 34))
                                    .addComponent(labelLevelDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelTitle))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(buttonCancel, javax.swing.GroupLayout.PREFERRED_SIZE, 92, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(buttonTagwipe, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        )
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(labelTitle)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(7, 7, 7)
                        .addComponent(labelLevel))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(labelLevelSelected)
                        .addComponent(sliderLevel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(labelLevelDescription)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(buttonTagwipe, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(buttonCancel))
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGap(28, 28, 28)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelBeautify)
                            .addComponent(checkboxBeautify))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelConvert)
                            .addComponent(radioAllDocx))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(radioCurrentDocx)
                        .addGap(10, 10, 10)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelBackup)
                            .addComponent(checkboxBackup))
                        .addContainerGap(64, Short.MAX_VALUE))))
        )

        pack()
        setLocationRelativeTo(null)
    }

    private void buttonCancelActionPerformed(java.awt.event.ActionEvent evt) {
        dispose()
    }

    private void sliderLevelStateChanged(javax.swing.event.ChangeEvent evt) {
        labelLevelSelected.setText(Integer.toString(sliderLevel.getValue()))
        level = sliderLevel.getValue()
        labelLevelDescription.setText(levelDescriptions[sliderLevel.getValue()])
    }

    private void checkboxBeautifyStateChanged(javax.swing.event.ChangeEvent evt) {
        beautify = checkboxBeautify.isSelected()
    }

    private void checkboxBackupStateChanged(javax.swing.event.ChangeEvent evt) {
        backupFiles = checkboxBackup.isSelected()
    }

    private void radioAllDocxStateChanged(javax.swing.event.ChangeEvent evt) {
        convertAll = radioAllDocx.isSelected()
    }

    private void radioCurrentDocxStateChanged(javax.swing.event.ChangeEvent evt) {
        convertAll = radioAllDocx.isSelected()
    }

}
