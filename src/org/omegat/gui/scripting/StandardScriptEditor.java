package org.omegat.gui.scripting;

import java.awt.Component;
import java.awt.Font;

import javax.swing.JMenuBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class StandardScriptEditor extends AbstractScriptEditor {
    private JTextArea m_scriptEditor;
    private JScrollPane m_scrollPaneEditor;

    @Override
    public void setHighlighting(String extension) {
        
    }

    @Override
    public void enhanceMenu(JMenuBar menubar) {

    }

    @Override
    public void initLayout(ScriptingWindow scriptingWindow) {
        m_scriptEditor = new JTextArea();
        
        m_scriptEditor.setFont(new Font(Font.MONOSPACED, Font.PLAIN,
                m_scriptEditor.getFont().getSize()));
        m_scrollPaneEditor = new JScrollPane(m_scriptEditor);

    }

    @Override
    public Component getPanel() {
        return m_scrollPaneEditor;
    }

    @Override
    public JTextArea getTextArea() {
        return m_scriptEditor;
    }
    
}
