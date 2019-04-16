package org.omegat.util;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;

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
}
