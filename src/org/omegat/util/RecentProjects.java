package org.omegat.util;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.JMenuItem;

import org.omegat.core.Core;
import org.omegat.core.data.ProjectFactory;
import org.omegat.gui.main.IMainWindow;
import org.omegat.gui.main.ProjectUICommands;

public class RecentProjects implements Iterable<String> {
	private final ArrayList<String> recentProjects = new ArrayList<String>();
	private final int mostRecentProjectSize;
	private final JMenuItem projectOpenRecentMenuItem;

	public RecentProjects(JMenuItem projectOpenRecentMenuItem) {
		if (projectOpenRecentMenuItem == null) {
			IMainWindow mainWindow = Core.getMainWindow();
			if (mainWindow == null) {
				throw new IllegalArgumentException(
						"Cannot initialize Recent Menu Items without a Main Window");
			}
			projectOpenRecentMenuItem = mainWindow.getMainMenu()
					.getProjectRecentMenuItem();
		}

		this.mostRecentProjectSize = Preferences.getPreferenceDefault(
				Preferences.MOST_RECENT_PROJECTS_SIZE, 5);
		this.projectOpenRecentMenuItem = projectOpenRecentMenuItem;
		loadFromPrefs();
	}

	public RecentProjects() {
		this(null);
	}

	@Override
	public Iterator<String> iterator() {
		return Collections.unmodifiableCollection(recentProjects).iterator();
	}

	public void saveToPrefs() {
		for (int i = 0; i < mostRecentProjectSize; i++) {
			Preferences.setPreference(Preferences.MOST_RECENT_PROJECTS_PREFIX
					+ i, recentProjects.get(i));
		}

		Preferences.save();
	}

	public void loadFromPrefs() {
		for (int i = 0; i < mostRecentProjectSize; i++) {
			String projectKey = Preferences.MOST_RECENT_PROJECTS_PREFIX + i;

			if (!Preferences.existsPreference(projectKey)) {
				break;
			}

			add(Preferences.getPreference(projectKey));
		}
	}

	public void updateMenu() {
		if (projectOpenRecentMenuItem == null && Core.getMainWindow() == null) {
			return;
		}

		JMenuItem recentMenu = projectOpenRecentMenuItem;

		recentMenu.removeAll();

		Iterator<String> it = recentProjects.iterator();
		while (it.hasNext()) {
			final String recentProject = it.next();

			JMenuItem recentProjectMenuItem = new JMenuItem(recentProject);
			recentProjectMenuItem.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent event) {
					Core.getProject().saveProject();
					ProjectFactory.closeProject();
					ProjectUICommands.projectOpen(new File(recentProject));
				}
			});

			recentMenu.add(recentProjectMenuItem);
		}

	}

	public void add(String element) {
		recentProjects.remove(element);
		recentProjects.add(0, element);

		// Shrink the list to match the desired size.
		while (recentProjects.size() > mostRecentProjectSize) {
			recentProjects.remove(recentProjects.size() - 1);
		}
	}

}
