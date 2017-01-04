/////////////////////////////////////////////////////////
//  Bare Bones Browser Launch                          //
//  Version 3.1 (June 6, 2010)                         //
//  By Dem Pilafian                                    //
//  Supports:                                          //
//     Mac OS X, GNU/Linux, Unix, Windows XP/Vista/7   //
//  Example Usage:                                     //
//     String url = "http://www.centerkey.com/";       //
//     BareBonesBrowserLaunch.openURL(url);            //
//  Public Domain Software -- Free to Use as You Like  //
/////////////////////////////////////////////////////////
package com.hiohiohio.omegat.plugin.externalfinder.util;

import javax.swing.JOptionPane;
import java.util.Arrays;

public class BareBonesBrowserLaunch {

    static final String[] browsers = {"google-chrome", "firefox", "opera",
        "epiphany", "konqueror", "conkeror", "midori", "kazehakase", "mozilla"};
    static final String errMsg = "Error attempting to launch web browser";

    public static void openURL(final String url) {
        try {  //attempt to use Desktop library from JDK 1.6+
            Class<?> d = Class.forName("java.awt.Desktop");
            d.getDeclaredMethod("browse", new Class[]{java.net.URI.class}).invoke(
                    d.getDeclaredMethod("getDesktop").invoke(null),
                    new Object[]{java.net.URI.create(url)});
            //above code mimicks:  java.awt.Desktop.getDesktop().browse()
        } catch (Exception ignore) {  //library not available or failed
            final String osName = System.getProperty("os.name");
            try {
                if (osName.startsWith("Mac OS")) {
                    Class.forName("com.apple.eio.FileManager").getDeclaredMethod(
                            "openURL", new Class[]{String.class}).invoke(null,
                                    new Object[]{url});
                } else if (osName.startsWith("Windows")) {
                    Runtime.getRuntime().exec(
                            "rundll32 url.dll,FileProtocolHandler " + url);
                } else { //assume Unix or Linux
                    String browser = null;
                    for (String b : browsers) {
                        if (browser == null && Runtime.getRuntime().exec(new String[]{"which", b}).getInputStream().read() != -1) {
                            Runtime.getRuntime().exec(new String[]{browser = b, url});
                        }
                    }
                    if (browser == null) {
                        throw new Exception(Arrays.toString(browsers));
                    }
                }
            } catch (Exception e) {
                JOptionPane.showMessageDialog(null, errMsg + "\n" + e.toString());
            }
        }
    }
}
