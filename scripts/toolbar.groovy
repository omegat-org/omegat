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

scriptsDir.list().each{ f ->
	if ( f.find(/\.groovy$/) )
	{
		console.println("button \"" + f + "\"")
		def item = new ScriptItem(f, scriptsDir);
		def button = new JButton()
		button.setText(item.name)
		button.setToolTipText(item.description)
		//button.addMouseListener(null)
		toolBar.add(button)
	}
}

toolBar.setPreferredSize(new Dimension(24, 140));
toolBar.setVisible(true);

// XXX Add a SCRIPTTOOLBAR each time it is called. 
mainWindow.addDockable(new DockableScrollPane("SCRIPTTOOLBAR", title, toolBar, true));

