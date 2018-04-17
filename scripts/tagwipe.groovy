/* :name=Tagwipe :description=Remove tags from docx documents
 *
 * This is a Groovy port of the DGT-OmegaT tagwipe.pl (version 20170914)
 * http://185.13.37.79/?q=node/35
 *
 * It converts all docx documents in the source file and create new one (with
 * the extension .out.docx).
 *
 * @author  Briac Pilpré
 * @date    2018-04-13
 * @version 0.1.0
 */
import java.util.zip.*;
import java.util.regex.*;

// TODO - GUI to select level and beautify
beautify = false;
level = 1;

// Debug print each Word paragraph before/after cleanup.
debug = false;

def prop = project.projectProperties
if (!prop) {
    console.println("No project loaded.");
    return;
}

files = project.projectFiles;
for (i in 0 ..< files.size()) {
  def projectFile = files[i];
  if (projectFile.filePath.matches(/(?i).*(?<!\.out)\.docx$/)) {
    def inputFile = new File(prop.getProjectRoot() + "/source/" + projectFile.filePath);
    console.println("Tagwipe $inputFile");
    tagwipe(inputFile.getAbsolutePath())
  }
}


def tagwipe(inputFile) {
    def outDocx = inputFile.replaceAll(/(?i)\.(docx)$/, '.out.$1');

    console.println "Opening input file '$inputFile'"
    def zipIn  = new ZipFile(inputFile)

    console.println "Creating output file '$outDocx'"
    def zipOut = new ZipOutputStream(new FileOutputStream(outDocx));

    for (ZipEntry entry : zipIn.entries()) {
        // Only clean endnotes, footer and document files
        if (! entry.getName().matches(/.*\b(endnotes|foot.+?|document).xml$/)) {
            // Just copy the file as is
            zipOut.putNextEntry(new ZipEntry(entry.getName()));
            def is = zipIn.getInputStream(entry);
            def buf = new byte[1024];
            def len;
            while((len = is.read(buf)) >= 0) {
                zipOut.write(buf, 0, len);
            }
        }
        else {
            console.println("Cleaning entry '" + entry.getName() + "'...");

            def is = zipIn.getInputStream(entry)
            def br = new InputStreamReader(is, 'UTF-8');

            StringBuilder sb = new StringBuilder();
                br.eachLine(0) { line ->
                    paras = line.split("(?<=</w:p>)");
                    paras.each { p ->
                        if (debug) console.println("IN\t$p");

                        p = after(transform(before(p)));

                        if (debug) console.println("OUT\t$p");

                        sb.append(p);
                    }
                };

            zipOut.putNextEntry(new ZipEntry(entry.getName()));
            zipOut << sb.toString().getBytes('UTF8')
            zipOut.closeEntry();
        }
    }

    zipIn.close()
    zipOut.close()
}

def  before(p) {

    def skip;
    skip = 1
    if ( p =~ /<wps:txbx>/ ) {
        skip = true;
    }
    else if ( p =~ /<\/wps:txbx>/ ) {
        skip = false;
    }

//  if (level >= 2) {
//      next if m{<w:pStyle w:val="TOC[^"]+"/>};                        // suppress TOC
//  }

    p = p.replaceAll(/(\n|\t)+</, "<");                                 // flatten file
    p = p.replaceAll(/<w:r [^>]*>/, "<w:r>");                           // flat runs
    p = p.replaceAll(/<w:t [^>]*>/, "<w:t>");                           // flat text

    // protect http://www.liquid-technologies.com/XML/EscapingData.aspx
    p = p.replaceAll(/(?m)&(lt|gt|amp);/, '&$1#');

    // clean ...
    // http://www.datypic.com/sc/ooxml/e-w_rPr-2.html

    if (level >= 0) {
        p = p.replaceAll(/<w:lastRenderedPageBreak\/>/, "");
    }
    if (level >= 1 ) {
        //_clean suppress only inside "run"
        p = _clean(p, '<w:snapToGrid w:val="0"/>');
        p = _clean(p, '<w:szCs w:val="24"/>');                          // suppress Font Size when 24 (=default?)
        p = _clean(p, '<w:sz w:val="24"/>');                            // suppress Font Size when 24 (=default?)
        p = _clean(p, '<w:kern w:val="\\d+"/>');                        // suppress kerning attribute
        p = _clean(p, '<w:color w:val="000000"/>');                     // suppress color when black (or default)
        p = _clean(p, '<w:color w:val="auto"/>');                       // suppress color when "auto"

        p = _clean(p, '<w:u w:color="000000"/>');                       // suppress color when black (or default)
        p = _clean(p, '<w:u w:color="auto"/>');                         // suppress color when "auto"

        p = _clean(p, '<w:bdr w:val="nil"/>');

        p = _clean(p, '<w:w w:val=".."/>');                             // suppress compressed or expanded text
//      p = _clean(p, '<w:rFonts w:eastAsia="[^>]+"/>');                // avoid asian attribute for fonts
        p = _clean(p, '<w:spacing w:val="[^"]+"/>');                    // suppress space spacing

        p = p.replaceAll(/(?m)<[^<]+?w:val="nil"\/>/, "");

        p = p.replaceAll(/<w:lang[^>]*?>/, "");
        p = p.replaceAll(/<w:noProof\/>/, "");
        p = p.replaceAll(/<w:proofErr w:type="\w+?"\/>/,"");            // avoid proofing error definition
        p = p.replaceAll(/s\{ w:rsidR="[^"]+"/, "");                    // remove revision IDs
        p = p.replaceAll(/ w:rsidRPr="[^"]+"/, "");                     //
        p = p.replaceAll(/ w:rsidP="[^"]+"/, "");                       //
        p = p.replaceAll(/><\/w:t>/, "> </w:t>");                       // add a space into a blank text

        p = p.replaceAll(/(?m)<w:rStyle w:val="(hps|x\d+)"\/>/, "");    // suppress referenced char styles "hps" or "x1" ... "x9999"
        p = p.replaceAll(/(?m)<w:bookmark(Start|End)[^<>]+?\/>/, "");

    }
    if (level >= 2) {
        //p = p.replaceAll(/<w:rFonts[^<>]+?\/>/, "");                  // suppress Font attribute

        p = p.replaceAll(/ w:eastAsia="[^>]+"/, "");
        p = p.replaceAll(/ w:cs="[^>]+"/, "");
        p = p.replaceAll(/<w:szCs[^<>]+>/, "");
        p = p.replaceAll(/<w:bCs\/>/, "");
        p = p.replaceAll(/<w:iCs\/>/, "");

        p = p.replaceAll(/<w:noBreakHyphen\/>/, "<w:t>-</w:t>");        // replace non breaking Hyphen with normal "-"
        p = p.replaceAll(/<w:softHyphen\/>/, "");                       // replace Soft Hyphen with nothing
        p = p.replaceAll(/(?m)<w:smartTag[^>]+>/, "");                  // suppress SmartTags
        p = p.replaceAll(/(?m)<\/w:smartTag>/, "");
    }
    if (level >= 3) {
        p = p.replaceAll(/(?m)<w:comment[^<>]+>/, "");                  // suppress Comments
        p = p.replaceAll(/(?m)<w:rStyle w:val="CommentReference"\/>/, "");
    }
    if (level >= 4) {
        p = p.replaceAll(/(?m)<w:hyperlink[^<>]+>/, "");                // suppress Hyperlinks (transform they to normal text)
        p = p.replaceAll(/(?m)<\/w:hyperlink>/, "");
    }
    if (level >= 5) {
        p = _clean(p, '<w:vanish/>');                                   // suppress hidden text
        p = _clean(p, '<w:shadow/>');                                   // suppress shadow text
        p = _clean(p, '<w:color[^<>]+>');                               // suppress every color in text
        p = _clean(p, '<w:highlight w:val="[^"]+?"/>');                 // suppress highlight
    }
    if (level >= 6) {
        p = _clean(p, '<w:rFonts[^<>]+?/>');                            // avoid attribute for fonts
        p = _clean(p, '<w:sz[^<>]+?/>');                                // suppress Font Size
    }
    if (level >= 7) {
        p = p.replaceAll(/<w:rPr>(?:(?:(?!w:rStyle).)*?)<\/w:rPr>/, "");     // remove everything is not rStyle
//      s{<w:r>(?:(?:(?!<w:t>).)*?)<w:t>}{<w:r><w:t>}g;
//      s{</w:r>(?:(?:(?!<w:r>).)*?)<w:r>}{</w:r><w:r>}g
    }
    if (level >= 8) {
        p = p.replaceAll(/<w:rPr>(?:(?:(?!<\/w:rPr>).)*?)<\/w:rPr>/, "");   // remove every attribute
    }

    p = p.replace(/<w:rPr><\/w:rPr>/, "");                              // suppress empty run properties
    p = p.replace(/(?m)<w:r><\/w:r>/, "");                              // supress empty runs

//  // normalize ... <w:br/> and <w:tab/> have their own run
//  s{(<w:r>(?:(?!<w:t>).)*)(<w:br/>|<w:tab/>)<w:t>}{$1$2</w:r>$1<w:t>}g if $skip == 0;

    p = p.replaceAll(/<w:r><w:tab\/><\/w:r><w:r><w:rPr><w:rStyle w:val="FootnoteReference"\/><\/w:rPr><w:footnoteRef\/><\/w:r>/,
        "<w:r><w:tab/></w:r>");

    return p;
}


def transform(p) {
//  p = p.replaceAll(/<\/w:t><\/w:r><w:r><w:noBreakHyphen\/><\/w:r>/, "-</w:t></w:r>");

    p = p.replaceAll(/<w:noBreakHyphen\/><w:t>/, "<w:t>-");             //replace non breaking Hyphen with normal "-"
    p = p.replaceAll(/<w:softHyphen\/>/,"");                            //replace Soft Hyphen with nothing

    //compress tags
    def rxCompressTags = /.*(<w:r>(?:<w:rPr>(?:<w:[^>]+>)*<\/w:rPr>)?<w:t>)([^>]+)(<\/w:t><\/w:r>)\s*(\1).*/;
    while ( p.matches(rxCompressTags) ) {
        p = p.replaceAll(/(<w:r>(?:<w:rPr>(?:<w:[^>]+>)*<\/w:rPr>)?<w:t>)([^>]+)(<\/w:t><\/w:r>)\s*(\1)/, '$1$2');
    }

    //next line moves footnote reference before [\s.!?;:]
    p = p.replaceAll(
        /([.!?;:]\s*(<\/w:t><\/w:r>))\s*(<w:r><w:rPr><w:rStyle w:val="FootnoteReference"\/>(<[^>]+?>)*<\/w:rPr><w:footnoteReference w:id="[^"]+?"\/><\/w:r>)/,
        '$2$3<w:r><w:t>$1'
    );

    //next line moves footnotes surounded by brackets [] {} () before [\s.!?;:]
    p = p.replaceAll(
        /([\s;:!?.])([\[\(\{]<\/w:t><\/w:r>\s*<w:r><w:rPr><w:rStyle w:val="FootnoteReference"\/>(?:<[^>]+?>)*<\/w:rPr><w:footnoteReference w:id="[^"]+?"\/><\/w:r>\s*<w:r>.+?<w:t[^>]*>[\]\)\}])/,
        '$2$1'
    );

//  while ( s{</w:t></w:r><w:r><w:t>([ ,.?!:"]+)</w:t>}{$1<\/w:t>}mx ) {};  //attach ([ ,.?!:"] to preceding run

    return p;
}


def after(p) {
    p = p.replaceAll(/<w:t>/, '<w:t xml:space="preserve">');
    p = p.replaceAll(/(<w:document)/, '\n$1');

    // unprotect http://www.liquid-technologies.com/XML/EscapingData.aspx
    p = p.replaceAll(/&(lt|gt|amp)#/, '&$1;');

    if (beautify) {
        p = p.replaceAll(/(<w:p |<w:p>|<\/w:p>)/, '\n$1')
            .replaceAll(/(<w:r>|<w:r w[^<>]+?>)/, '\n\t$1')
            .replaceAll(/(<w:body>)/, '\n$1')
            .replaceAll(/(<.w:docu[^<>]+?>)/, '\n$1')
            .replaceAll(/(<.w:body>)/, '\n$1')
            .replaceAll(/<\/w:tc><w:tc>/, '\n</w:tc>\n<w:tc>')
            .replaceAll(/(<\/w:tc><\/w:tr>)/, '\n$1\n')
            .replaceAll(/(<w:tbl>)/, '\n$1')
            ;
    }

    return p;
}

def _clean(p, pat) {
    return Pattern
        .compile("(?m)(<w:r><w:rPr>.*?)$pat(.*?</w:rPr>)")
        .matcher(p)
        .replaceAll('$1$2');
}
