package org.omegat.gui.scripting;

import java.awt.Component;

import javax.swing.JMenuBar;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public abstract class AbstractScriptEditor extends JTextArea {
    public abstract void setHighlighting(String extension);
    public abstract void enhanceMenu(JMenuBar mb);
    public abstract void initLayout(ScriptingWindow scriptingWindow);
    public abstract Component getPanel();
    // XXX setText(String s) does not seem to work directly on the subclasses ?
    public abstract JTextArea getTextArea();
}
