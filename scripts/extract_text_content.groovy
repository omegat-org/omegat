/* :name=Extract Text Content :description=Extracts the content of the projects in a single text file (one line per segment).
 * see RFE#1282 Extracts the content of the projects to text file - https://sourceforge.net/p/omegat/feature-requests/182/
 * 
 * @author  Briac Pilpré
 * @author Didier Briel
 * @date    2018-06-22
 * @version 0.3
 */
import org.omegat.core.Core;
import org.omegat.util.StringUtil;
import static javax.swing.JOptionPane.*;

def gui() {

  // abort if a project is not opened yet
  def prop = project.projectProperties
  if (!prop) {
    showMessageDialog null, res.getString("noProjectMsg"), res.getString("noProject"), INFORMATION_MESSAGE
    return
  }

  def root = prop.projectRoot;

  def srcTextFile = new File(root, 'project_source_content.txt');
  def tgtTextFile = new File(root, 'project_target_content.txt');
  def timeStamp   = new Date();
  def nl = System.getProperty('line.separator')

  console.println(StringUtil.format(res.getString("sourceTextFile"), srcTextFile.absolutePath));
  console.println(StringUtil.format(res.getString("targetTextFile"), tgtTextFile.absolutePath));

  srcTextFile.write("## OmegaT Source Text Export (" + timeStamp + ")" + nl, "UTF-8");
  tgtTextFile.write("## OmegaT Target Text Export (" + timeStamp + ")" + nl, "UTF-8");

  def files = project.projectFiles;
  for (i in 0 ..< files.size()) {
    fi = files[i]

    srcTextFile.append(nl + "## OmegaT File:" + fi.filePath.toString() + nl, "UTF-8");
    tgtTextFile.append(nl + "## OmegaT File:" + fi.filePath.toString() + nl, "UTF-8");

    for (j in 0 ..< fi.entries.size()) {
      ste = fi.entries[j]
      source = ste.getSrcText()
      srcTextFile.append(source + nl, "UTF-8");

      target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;
      if (target) {
        tgtTextFile.append(target + nl, "UTF-8");
      }
    }
    
    //console.println(source + "\t" + target);
  }
}

