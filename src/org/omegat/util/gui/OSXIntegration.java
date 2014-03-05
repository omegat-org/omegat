/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014 Aaron Madlon-Kay
               Home page: http://www.omegat.org/
               Support center: http://groups.yahoo.com/group/OmegaT/

 This file is part of OmegaT.

 OmegaT is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 OmegaT is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **************************************************************************/

package org.omegat.util.gui;

import java.awt.event.ActionListener;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.omegat.util.Log;
import org.omegat.util.Preferences;

/**
 * This class uses reflection to set Mac OS X-specific integration hooks.
 * 
 * @author Aaron Madlon-Kay
 */
public class OSXIntegration {
    
    private static Class<?> appClass;
    private static Object app;

    public static void init() {
        try {
            // Set quit strategy:
            //   app.setQuitStrategy(com.apple.eawt.QuitStrategy.CLOSE_ALL_WINDOWS);
            Class<?> strategyClass = Class.forName("com.apple.eawt.QuitStrategy");
            Method setQuitStrategy = getAppClass().getDeclaredMethod("setQuitStrategy", strategyClass);
            setQuitStrategy.invoke(getApp(), strategyClass.getField("CLOSE_ALL_WINDOWS").get(null));
            // Prevent sudden termination:
            //   app.disableSuddenTermination();
            Method disableTerm = getAppClass().getDeclaredMethod("disableSuddenTermination");
            disableTerm.invoke(getApp());
        } catch (Exception ex) {
            Log.log(ex);
        }
    }
    
    public static void setAboutHandler(final ActionListener al) {
        try {
            // Handler must implement com.apple.eawt.AboutHandler interface.
            Class<?> aboutHandlerClass = Class.forName("com.apple.eawt.AboutHandler");
            InvocationHandler ih = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                        throws Throwable {
                    if (method.getName().equals("handleAbout")) {
                        // Respond to handleAbout(com.apple.eawt.AppEvent.AboutEvent)
                        al.actionPerformed(null);
                    }
                    return null;
                }
            };
            Object handler = Proxy.newProxyInstance(OSXIntegration.class.getClassLoader(),
                    new Class<?>[] { aboutHandlerClass }, ih);
            // Set handler:
            //   app.setAboutHandler(handler);
            Method setAboutHandler = getAppClass().getDeclaredMethod("setAboutHandler", aboutHandlerClass);
            setAboutHandler.invoke(getApp(), handler);
        } catch (Exception ex) {
            Log.log(ex);
        }
    }
    
    public static void setQuitHandler(final ActionListener al) {
        try {
            // Handler must implement com.apple.eawt.QuitHandler interface.
            Class<?> quitHandlerClass = Class.forName("com.apple.eawt.QuitHandler");
            InvocationHandler ih = new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args)
                    throws Throwable {
                    if (method.getName().equals("handleQuitRequestWith")) {
                        Class<?> quitResponseClass = Class.forName("com.apple.eawt.QuitResponse");
                        if (args != null && args.length > 1 && quitResponseClass.isInstance(args[1]) &&
                                Preferences.isPreference(Preferences.ALWAYS_CONFIRM_QUIT)) {
                            // Respond to handleQuitRequestWith(com.apple.eawt.AppEvent.QuitEvent,
                            //   com.apple.eawt.QuitResponse)
                            // Cancel the quit because OmegaT will prompt:
                            //   arg1.cancelQuit();
                            Method cancelQuit = quitResponseClass.getDeclaredMethod("cancelQuit");
                            cancelQuit.invoke(args[1]);
                        }
                        al.actionPerformed(null);
                    }
                    return null;
                }
            };
            Object handler = Proxy.newProxyInstance(OSXIntegration.class.getClassLoader(),
                    new Class<?>[] { quitHandlerClass }, ih);
            // Set handler:
            //   app.setAboutHandler(handler);
            Method setQuitHandler = getAppClass().getDeclaredMethod("setQuitHandler", quitHandlerClass);
            setQuitHandler.invoke(getApp(), handler);
        } catch (Exception ex) {
            Log.log(ex);
        }
    }
    
    private static Class<?> getAppClass() throws Exception {
        if (appClass == null) {
            appClass = Class.forName("com.apple.eawt.Application");
        }
        return appClass;
    }
    
    private static Object getApp() throws Exception {
        if (app == null) {
            Method getApp = getAppClass().getDeclaredMethod("getApplication");
            app = getApp.invoke(null);
        }
        return app;
    }
}
