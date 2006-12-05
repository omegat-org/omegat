/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2000-2006 Keith Godfrey, Maxym Mykhalchuk, and Henry Pijffers
               Home page: http://www.omegat.org/omegat/omegat.html
               Support center: http://groups.yahoo.com/group/OmegaT/

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
**************************************************************************/

package org.omegat.util;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.Random;

/**
  * A collection of methods to make logging things easier.
  *
  * @author Henry Pijffers (henry.pijffers@saxnot.com)
 */
public class Log {
    /**
      * Name of the log file
      */
    private final static String FILE_LOG = "log.txt";                           // NOI18N

    /**
      * Returns the path to the log file.
      */
    public static String getLogLocation() {
        return StaticUtils.getConfigDir() + FILE_LOG;
    }

    private static PrintWriter log = getLogWriter();
    /**
      * Returns a writer for writing to the log.
      *
      * Do not use this writer to write to the log,
      * unless none of the logging methods are
      * suitable to your needs.
      */
    public static PrintWriter getLogWriter() {
        SessionPrintStream logStream = null;
        if (log == null) {
            try {
                // create a new session print stream + writer for the log file
                logStream = new SessionPrintStream(new PrintStream(
                    new FileOutputStream(StaticUtils.getConfigDir() + FILE_LOG, true)));
                log = new PrintWriter(new OutputStreamWriter(logStream, "UTF-8"));
            }
            catch(Exception e) {
                // in case we cannot create a log file on dist,
                // redirect to system out
                // HP: No, don't redirect, otherwise everything will be written to System.out twice!
                // HP: People using this method should know that what is written to the Writer, is
                // HP: not automatically written to System.out also
                //log = new SessionPrintStream(System.out);
            }

            // also encapsulate the system out in a session print stream
            SessionPrintStream sessionOut = new SessionPrintStream(System.out);
            System.setOut(sessionOut);

            // copy the session ID from the log stream
            if (log != null)
                sessionOut.setSessionID(logStream.getSessionID());
        }

        return log;
    }

    /**
     * Logs what otherwise would go to System.out
     */
    public static void log(String s)
    {
        // Write to log
        try
        {
            log.println(s);
            log.flush();
        }
        catch( Exception e ) // log may be null, or some I/O error occurred
        {
            // doing nothing
        }

        // Write to System.out
        System.out.println(s);
        System.out.flush();
    }

    /**
      * Logs a message, retrieved from the resource bundle.
      *
      * @param key The key of the message in the resource bundle.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logRB(String key) {
        logRB(key, null);
    }

    /**
      * Logs a message, retrieved from the resource bundle.
      *
      * @param key        The key of the message in the resource bundle.
      * @param parameters Parameters for the message. These are inserted by
      *                   using StaticUtils.format.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logRB(String key, Object[] parameters) {
        // Retrieve the message
        String message = OStrings.getString(key);

        // Format the message, if there are parameters
        if (parameters != null)
            message = StaticUtils.format(message, parameters);

        // Write the message to the log
        log(message);
    }

    /**
      * Logs an Exception or Error.
      *
      * To the log are written:
      * - The class name of the Exception or Error
      * - The message, if any
      * - The stack trace
      *
      * @param throwable The exception or error to log
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void log(Throwable throwable) {
        // Log the throwable class
        log(throwable.getClass().getName() + ":");

        // Log the message, if any
        String message = throwable.getMessage();
        if ((message != null) && (message.length() > 0))
            log(message);

        // Log the stack trace
        StringWriter stackTrace = new StringWriter();
        throwable.printStackTrace(new PrintWriter(stackTrace));
        log(stackTrace.toString());
    }

    /**
      * Writes a warning message to the log (to be retrieved from the
      * resource bundle), preceded by a lie containing the warning ID.
      *
      * While the warning message can be localised, the warning ID should not,
      * so developers can determine what warning was given by looking at the
      * warning ID, instead of trying to interpret localised messages.
      *
      * @param id The key of the warning message in the resource bundle
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logWarningRB(String key) {
        // Retrieve the waring message
        String message = OStrings.getString(key);

        // Log it
        logWarning(key, message, null);
    }

    /**
      * Writes a warning message to the log (to be retrieved from the
      * resource bundle), preceded by a line containing the warning ID.
      *
      * While the warning message can be localised, the warning ID should not,
      * so developers can determine what warning was given by looking at the
      * warning ID, instead of trying to interpret localised messages.
      *
      * @param key        The key of the warning message in the resource bundle
      * @param parameters Parameters for the warning message. These are
      *                   inserted by using StaticUtils.format.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logWarningRB(String key, Object[] parameters) {
        // Retrieve the warning message
        String message = OStrings.getString(key);

        // Log it
        logWarning(key, message, parameters);
    }

    /**
      * Writes the specified warning message to the log, preceded by a line
      * containing the warning ID.
      *
      * While the warning message can be localised, the warning ID should not,
      * so developers can determine what warning was given by looking at the
      * warning ID, instead of trying to interpret localised messages.
      *
      * @param id      An identification string for the warning
      * @param message The actual warning message
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logWarning(String id, String message) {
        logWarning(id, message, null);
    }

    /**
      * Writes the specified warning message to the log, preceded by a line
      * containing the warning ID.
      *
      * While the warning message can be localised, the warning ID should not,
      * so developers can determine what warning was given by looking at the
      * warning ID, instead of trying to interpret localised messages.
      *
      * @param id         An identification string for the warning
      * @param message    The actual warning message
      * @param parameters Parameters for the warning message. These are
      *                   inserted by using StaticUtils.format.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logWarning(String id, String message, Object[] parameters) {
        logIdentifiableMessage(
            StaticUtils.format(OStrings.getString("SU_LOG_WARNING_ID"),
                               new Object[] {id}),
            message,
            parameters);
    }

    /**
      * Writes an error message to the log (to be retrieved from the
      * resource bundle), preceded by a line containing the error ID.
      *
      * While the error message can be localised, the error ID should not,
      * so developers can determine what error was given by looking at the
      * error ID, instead of trying to interpret localised messages.
      *
      * @param key The key of the error message in the resource bundle
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logErrorRB(String key) {
        // Retrieve and log the error message
        String message = OStrings.getString(key);

        // Log it
        logError(key, message, null);
    }

    /**
      * Writes an error message to the log (to be retrieved from the
      * resource bundle), preceded by a line containing the error ID.
      *
      * While the error message can be localised, the error ID should not,
      * so developers can determine what error was given by looking at the
      * error ID, instead of trying to interpret localised messages.
      *
      * @param id         The key of the error message in the resource bundle
      * @param parameters Parameters for the error message. These are
      *                   inserted by using StaticUtils.format.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logErrorRB(String key, Object[] parameters) {
        // Retrieve the error message
        String message = OStrings.getString(key);

        // Log it
        logError(key, message, parameters);
    }

    /**
      * Writes the specified error message to the log, preceded by a line
      * containing the error ID.
      *
      * While the error message can be localised, the error ID should not,
      * so developers can determine what error was given by looking at the
      * error ID, instead of trying to interpret localised messages.
      *
      * @param id      An identification string for the error
      * @param message The actual error message
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logError(String id, String message) {
        logError(id, message, null);
    }

    /**
      * Writes the specified error message to the log, preceded by a line
      * containing the error ID.
      *
      * While the error message can be localised, the error ID should not,
      * so developers can determine what error was given by looking at the
      * error ID, instead of trying to interpret localised messages.
      *
      * @param id         An identification string for the error
      * @param message    The actual error message
      * @param parameters Parameters for the error message. These are
      *                   inserted by using StaticUtils.format.
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    public static void logError(String id, String message, Object[] parameters) {
        logIdentifiableMessage(
            StaticUtils.format(OStrings.getString("SU_LOG_ERROR_ID"),
                               new Object[] {id}),
            message,
            parameters);
    }

    /**
      * Writes the specified message to the log, preced by a line containing
      * an identification for the message.
      *
      * While the message can be localised, the ID should not, so developers
      * can determine what message was given by looking at the error ID,
      * instead of trying to interpret localised messages.
      *
      * @param idLine     The identification line for the message
      * @param message    The actual message
      * @param parameters Parameters for the message. These are
      *                   inserted by using StaticUtils.format.
      */
    protected static void logIdentifiableMessage(String idLine, String message, Object[] parameters) {
        // First write the ID line to the log
        log(idLine);

        // Format the message, if there are parameters
        if (parameters != null)
            message = StaticUtils.format(message, parameters);

        // Write the message to the log
        log(message);
    }

    /**
      * Print stream that writes a session ID before each line of output
      *
      * @author Henry Pijffers (henry.pijffers@saxnot.com)
      */
    private static class SessionPrintStream extends PrintStream {

        /**
          * Print stream to write all output to.
          */
        //private PrintStream out;

        /**
          * Session ID
          */
        String sessionID;

        /**
          * Indicates whether the last character output was a newline.
          */
        private boolean lastIsNewline = false;

        /**
          * Constructs a new SessionPrintStream
          *
          * @param out The print stream to write all output to
          */
        public SessionPrintStream(PrintStream out) {
            super(out);

            // get a positive random number
            Random generator = new Random();
            generator.setSeed(System.currentTimeMillis()); // use current time as seed
            int random = Math.abs(generator.nextInt());

            // convert the number to string, 5 chars max, pad with zero's if necessary
            sessionID = String.valueOf(random);
            if (sessionID.length() > 5)
                sessionID = sessionID.substring(0, 5);
            else if (sessionID.length() < 5)
                for (int i = 5; i > sessionID.length(); i++)
                    sessionID = "0" + sessionID;
        }

        /**
          * Retrieves the session ID.
          *
          * @return The session ID of this SessionPrintStream
          */
        public String getSessionID() {
            return sessionID;
        }

        /**
          * Overrides the generated session ID.
          *
          * @param sessionID The session ID to use
          */
        public void setSessionID(String sessionID) {
            this.sessionID = sessionID;
        }

        /**
          * Writes the session ID to the output stream when at the start of a new line.
          */
        void printSessionID() {
            printSessionID(false);
        }

        /**
          * Writes the session ID to the output stream when at the start of a new line.
          *
          * @param forceWrite When true, the session ID is always writen,
          *                   even if not at the start of a new line.
          */
        void printSessionID(boolean forceWrite) {
            if (forceWrite || lastIsNewline)
                super.print(sessionID + ": ");
        }

        public void print(boolean b) {
            print(String.valueOf(b));
        }

        public void print(char c) {
            print(String.valueOf(c));
        }

        public void print(char[] s) {
            for (int i = 0; i < s.length; i++)
                print(s[i]);
        }

        public void print(double d) {
            print(String.valueOf(d));
        }

        public void print(float f) {
            print(String.valueOf(f));
        }

        public void print(int i) {
            print(String.valueOf(i));
        }

        public void print(long l) {
            print(String.valueOf(l));
        }

        public void print(Object o) {
            print(String.valueOf(o));
        }

        public void print(String s) {
            if (s == null)
                s = "null";
            byte[] bytes = s.getBytes();
            for (int i = 0; i < bytes.length; i++)
                write((int)bytes[i]);
        }

        public void println() {
            printSessionID();
            super.println();
            lastIsNewline = true;
        }

        public void println(boolean b) {
            print(b);
            println();
        }

        public void println(char c) {
            print(c);
            println();
        }

        public void println(char[] s) {
            print(s);
            println();
        }

        public void println(double d) {
            print(d);
            println();
        }

        public void println(float f) {
            print(f);
            println();
        }

        public void println(int i) {
            print(i);
            println();
        }

        public void println(long l) {
            print(l);
            println();
        }

        public void println(Object o) {
            print(o);
            println();
        }

        public void println(String s) {
            print(s);
            println();
        }

        public void write(int b) {
            printSessionID();
            super.write(b);
            lastIsNewline = (((char)b) == '\n');
        }

    } // SessionPrintStream

} // Log
