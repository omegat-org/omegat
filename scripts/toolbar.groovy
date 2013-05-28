import javax.swing.JToolBar;
import javax.swing.JButton;
import java.awt.Dimension;
import org.omegat.gui.main.DockableScrollPane;

def title = "Script ToolBar";
def toolBar = new JToolBar("Still draggable");

def button = new JButton();
button.setText("Hello OmegaT");
toolBar.add(button);
toolBar.setPreferredSize(new Dimension(24, 140));
toolBar.setVisible(true);

// XXX Add a SCRIPTTOOLBAR each time it is called. 
mainWindow.addDockable(new DockableScrollPane("SCRIPTTOOLBAR", title, toolBar, true));

