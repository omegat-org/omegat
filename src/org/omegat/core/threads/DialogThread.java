package org.omegat.core.threads;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: maxym
 * Date: 31.10.2004
 * Time: 2:26:21
 * To change this template use File | Settings | File Templates.
 */
class DialogThread extends Thread
{
    public DialogThread(JFrame win)
    {
        m_win = win;
    }

    public void run()
    {
        m_win.setVisible(true);
    }

    private JFrame m_win;
}
