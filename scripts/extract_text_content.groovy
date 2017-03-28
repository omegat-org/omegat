/* :name=Extract Text Content :description=Extracts the content of the projects in a single text file (one line per segment).
 * see RFE#1282 Extracts the content of the projects to text file - https://sourceforge.net/p/omegat/feature-requests/182/
 * 
 * @author  Briac Pilpré
 * @date    2017-03-22
 * @version 0.1
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

  console.println(StringUtil.format(res.getString("sourceTextFile"), srcTextFile.absolutePath));
  console.println(StringUtil.format(res.getString("targetTextFile"), tgtTextFile.absolutePath));

  srcTextFile.write("## OmegaT Source Text Export (" + timeStamp + ")\n");
  tgtTextFile.write("## OmegaT Target Text Export (" + timeStamp + ")\n");

  def files = project.projectFiles;
  for (i in 0 ..< files.size()) {
    fi = files[i]

    srcTextFile << "\n## OmegaT File:" + fi.filePath.toString() + "\n";
    tgtTextFile << "\n## OmegaT File:" + fi.filePath.toString() + "\n";

    for (j in 0 ..< fi.entries.size()) {
      ste = fi.entries[j]
      source = ste.getSrcText()
      srcTextFile << source + "\n";

      target = project.getTranslationInfo(ste) ? project.getTranslationInfo(ste).translation : null;
      if (target) {
        tgtTextFile << target + "\n";
      }
    }
    
    //console.println(source + "\t" + target);
  }
}

