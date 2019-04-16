package org.omegat.util;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;

import javax.swing.plaf.TextUI;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

@SuppressWarnings("deprecation")
public class Java8Compat {

    public static int getMenuShortcutKeyMaskEx() {
        int mask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMask();
        switch (mask) {
        case KeyEvent.CTRL_MASK:
            return KeyEvent.CTRL_DOWN_MASK;
        case KeyEvent.META_MASK:
            return KeyEvent.META_DOWN_MASK;
        default:
            return mask;
        }
    }

    public static Rectangle modelToView(JTextComponent comp, int pos) throws BadLocationException {
        return comp.modelToView(pos);
    }

    public static Rectangle modelToView(TextUI ui, JTextComponent comp, int pos) throws BadLocationException {
        return ui.modelToView(comp, pos);
    }

    public static int viewToModel(JTextComponent comp, Point pt) {
        return comp.viewToModel(pt);
    }
}
