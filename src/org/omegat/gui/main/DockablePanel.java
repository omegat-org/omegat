package org.omegat.gui.main;

import java.awt.Component;

import javax.swing.JPanel;

import com.vlsolutions.swing.docking.DockKey;
import com.vlsolutions.swing.docking.Dockable;
import com.vlsolutions.swing.docking.DockingConstants;

/**
 * Dockable JPanel for a docking library.
 * 
 * @author Alex Buloichik (alex73mail@gmail.com)
 */
@SuppressWarnings("serial")
public class DockablePanel extends JPanel implements Dockable {
    private final DockKey dockKey;

    public DockablePanel(String key, String name, boolean detouchable) {
        dockKey = new DockKey(key, name, null, null, DockingConstants.HIDE_BOTTOM);
        dockKey.setFloatEnabled(detouchable);
    }

    @Override
    public DockKey getDockKey() {
        return dockKey;
    }

    @Override
    public Component getComponent() {
        return this;
    }

    /** Updates the tool tip text of the docking pane. */
    @Override
    public void setToolTipText(String text) {
        dockKey.setTooltip(text);
    }

    /** Updates the name of the docking pane. */
    @Override
    public void setName(String name) {
        dockKey.setName(name);
    }
}
