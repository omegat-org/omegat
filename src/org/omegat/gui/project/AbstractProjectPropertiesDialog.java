package org.omegat.gui.project;

import org.omegat.core.data.ProjectProperties;
import org.omegat.util.OStrings;

import javax.swing.JDialog;
import java.awt.Frame;

abstract class AbstractProjectPropertiesDialog extends JDialog {

    protected final ProjectConfigMode mode;
    protected final ProjectProperties props;
    protected boolean cancelled = true;

    AbstractProjectPropertiesDialog(Frame parent, boolean modal, ProjectProperties props, ProjectConfigMode mode) {
        super(parent, modal);
        this.mode = mode;
        this.props = props;
    }

    void showDialog() {
        setVisible(true);
    }

    boolean isCancelled() {
        return cancelled;
    }


    protected void updateUIText() {
        switch (mode) {
        case NEW_PROJECT:
            setTitle(OStrings.getString("PP_CREATE_PROJ"));
            break;
        case RESOLVE_DIRS:
            setTitle(OStrings.getString("PP_OPEN_PROJ"));
            break;
        case EDIT_PROJECT:
            setTitle(OStrings.getString("PP_EDIT_PROJECT"));
            break;
        }
    }

}