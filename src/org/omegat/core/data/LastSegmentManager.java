/**************************************************************************
 OmegaT - Computer Assisted Translation (CAT) tool 
          with fuzzy matching, translation memory, keyword search, 
          glossaries, and translation leveraging into updated projects.

 Copyright (C) 2014      Briac Pilpre
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
package org.omegat.core.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Properties;
import java.util.logging.Logger;

import org.omegat.core.Core;
import org.omegat.core.data.IProject.FileInfo;
import org.omegat.gui.editor.IEditor;
import org.omegat.util.Log;
import org.omegat.util.OConsts;

public class LastSegmentManager {
	private static final Logger LOGGER = Logger.getLogger(LastSegmentManager.class.getName());

	private static final String LAST_ENTRY_SRC = "LAST_ENTRY_SRC";
	private static final String LAST_ENTRY_FILE = "LAST_ENTRY_FILE";
	private static final String LAST_ENTRY_NUMBER = "LAST_ENTRY_NUMBER";
	private static final String LAST_ENTRY_OFFSET = "LAST_ENTRY_OFFSET";
	
	private static File getLastEntryFile() {
		return new File(Core.getProject().getProjectProperties().getProjectInternal(), OConsts.LAST_ENTRY_NUMBER);
	}
	
	/** Save current entry position for repositioning on reload (RFE#35). This method is called from RealProject during saveProject(boolean) */
	public static void saveLastSegment() {
		Properties prop = new Properties();
		IEditor editor = Core.getEditor();
		
		int lastEntryNumber = editor.getCurrentEntryNumber();
		String currentFile = editor.getCurrentFile();
		
		if (currentFile == null)
		{
			// Project has no files, no need to save.
			return;
		}
		
		
		prop.put(LAST_ENTRY_NUMBER, Integer.toString(lastEntryNumber, 10));
		prop.put(LAST_ENTRY_FILE, currentFile);
		prop.put(LAST_ENTRY_SRC, editor.getCurrentEntry().getSrcText());
		
		// Won't work in EditorController.saveProject - editor commands are in SwingThread
//		int fileIndex = fileIndex(editor.getCurrentFile());
//		editor.gotoFile(fileIndex);
//		int fileFirstSegment = lastEntryNumber - editor.getCurrentEntryNumber();
//		prop.put(LAST_ENTRY_OFFSET, Integer.toString(fileFirstSegment));

		try {
			FileOutputStream fos = new FileOutputStream(getLastEntryFile());
			prop.store(fos, null);
			fos.close();
		} catch (IOException e) {
			Log.logDebug(LOGGER, "Could not write the last entry number: {0}", e.getMessage());
		}
	}
	
    /**
     * Jump to last edited entry
     */
	public static void restoreLastSegment(IEditor editor) {
		File lastEntryFile = getLastEntryFile();

		if (! lastEntryFile.exists())
		{
			return;
		}

		Properties prop = new Properties();
		try {
			prop.load(new FileInputStream(lastEntryFile));
		} catch (IOException e) {
			Log.logDebug(LOGGER, "Could not load last segment info", e.getMessage());
			return;
		}

		Core.getMainWindow().showStatusMessageRB("MW_JUMPING_LAST_ENTRY");
		int lastEntryNumber = 1;
		try {
			String lastEntry = prop.getProperty(LAST_ENTRY_NUMBER, "1");
			lastEntryNumber = Integer.parseInt(lastEntry, 10);
		} catch (Exception e) {
			Log.logDebug(LOGGER, "Cannot jump to last entry #" + lastEntryNumber + ":" + e.getMessage());
		}
		Log.logDebug(LOGGER, "Jumping to last entry #" + lastEntryNumber + ".");

		List<SourceTextEntry> allEntries = Core.getProject().getAllEntries();

		if (allEntries.size() < lastEntryNumber)
		{
			Log.logDebug(LOGGER, "Not enough segments to jump to " + lastEntryNumber);
			Core.getMainWindow().showStatusMessageRB(null);
			return;
		}

		SourceTextEntry propEntry = allEntries.get(lastEntryNumber - 1);

		String lastFile = prop.getProperty(LAST_ENTRY_FILE, "");
		String lastSrc = prop.getProperty(LAST_ENTRY_SRC, "");
		
		// Best case scenario, segment matches src (if it doesn't match filename, it's still okay)
		if (propEntry.getSrcText().equals(lastSrc))
		{
			gotoEntry(propEntry.entryNum(), editor);
			Core.getMainWindow().showStatusMessageRB(null);
			return;
		}

		// Check to see if the source and file match
		Log.logDebug(LOGGER, "Last entry #" + lastEntryNumber + " mismatch (file \"" + lastFile + "\", src \"" + lastSrc + "\")" );

		int fileIndex = fileIndex(lastFile);

		if (fileIndex == -1)
		{
			Log.logDebug(LOGGER, "File \"" + lastFile + "\" is not in the project anymore.");
			Core.getMainWindow().showStatusMessageRB(null);
			return;
		}

		
		// We landed in the right file, just not the right segment
		List<SourceTextEntry> fileEntries = Core.getProject().getProjectFiles().get(fileIndex).entries;
		for (SourceTextEntry entry : fileEntries)
		{
			if (entry.getSrcText().equals(lastSrc))
			{
				Log.logDebug(LOGGER, "Found a matching entry in the right file.");
				gotoEntry(entry.entryNum(), editor);
				return;
			}
		}
		
		// Things look bad, nothing in the matching file. Look in all the project or quit ?
		for (SourceTextEntry entry : allEntries)
		{
			if (entry.getSrcText().equals(lastSrc))
			{
				Log.logDebug(LOGGER, "Found a matching entry in the wrong file.");
				gotoEntry(entry.entryNum(), editor);
				return;
			}
		}

		Core.getMainWindow().showStatusMessageRB(null);
	}
	
	/** Get the project index given its name, returns -1 if the file is not found */
	private static int fileIndex(String filename)
	{

		int fileIndex = 0;
		for (FileInfo file : Core.getProject().getProjectFiles())
		{
			if (file.filePath.equals(filename))
			{
				return fileIndex; 
			}
			fileIndex++;
		}
		
		return -1;
	}
	
	private static void gotoEntry(int num, IEditor editor)
	{
		editor.gotoEntry(num);
		Core.getMainWindow().showStatusMessageRB(null);
		return;
	}
	
}
