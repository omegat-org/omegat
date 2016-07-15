import javax.swing.JToolBar;
import javax.swing.JButton;

import java.awt.Dimension;
import java.awt.image.RescaleOp;

import org.omegat.util.Preferences;
import org.omegat.gui.main.DockableScrollPane;
import org.omegat.gui.scripting.ScriptItem;

import java.io.File;

def title = "Scripts";
def toolBar = new JToolBar("Still draggable");

def scriptsDir = new File(Preferences.getPreferenceDefault(Preferences.SCRIPTS_DIRECTORY, "."));

scriptsDir.listFiles().each{ f ->
	if ( f.getPath().endsWith('.groovy') )
	{
		console.println("button \"" + f + "\"")
		def item = new ScriptItem(f);
		def button = new JButton()
		button.setText(item.scriptName)
		button.setToolTipText(item.description)
		//button.addMouseListener(null)
		toolBar.add(button)
	}
}

toolBar.setPreferredSize(new Dimension(24, 140));
toolBar.setVisible(true);

// XXX Add a SCRIPTTOOLBAR each time it is called. 
mainWindow.addDockable(new DockableScrollPane("SCRIPTTOOLBAR", title, toolBar, true));

