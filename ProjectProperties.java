//-------------------------------------------------------------------------
//  
//  ProjectProperties.java - 
//  
//  Copyright (C) 2002, Keith Godfrey
//  
//  This program is free software; you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation; either version 2 of the License, or
//  (at your option) any later version.
//  
//  This program is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//  
//  You should have received a copy of the GNU General Public License
//  along with this program; if not, write to the Free Software
//  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
//  
//  Build date:  4Dec2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
//import javax.swing.filechooser.*;
import java.io.*;
import java.lang.*;

// creates a dialog where project properties are entered and/or modified
class ProjectProperties extends JFrame 
{
	public String getProjFile()		{ return m_projFile;	}
	// 'base' is project file without .proj extension
	public String getProjFileBase()		{ return m_projBase;	}
	public String getProjName()		{ return m_projName;	}
	public String getProjRoot()		{ return m_projRoot;	}
	public String getSrcRoot()		{ return m_srcRoot;		}
	public String getLocRoot()		{ return m_locRoot;		}
	public String getGlosRoot()		{ return m_glosRoot;	}
	public String getTMRoot()		{ return m_tmRoot;		}
	public String getInternal()		{ return m_projInternal;	}

	public void reset()
	{
		m_projName = "";
		m_projRoot = "";
		m_projInternal = "";
		m_srcRoot = "";
		m_locRoot = "";
		m_glosRoot = "";
		m_tmRoot = "";
	}

	public boolean loadExisting() throws IOException, InterruptedIOException
	{
		reset();

		// select existing project file - open it
		JFileChooser d = new JFileChooser();
//		d.setCurrentDirectory(new File(OConsts.DEFAULT_PROJROOT));
//		d.setSelectedFile(new File(OConsts.DEFAULT_PROJNAME + 
//					OConsts.PROJ_EXTENSION));
		int res = d.showOpenDialog(this);
		if (res == JFileChooser.CANCEL_OPTION)
			throw new InterruptedIOException();
		File fp = d.getSelectedFile();
		if (fp == null)
			return false;
		else
			return readProjFile(fp);
	}

	public boolean createNew()
	{
		// new project window; create project file
		MNewProject newProj = new MNewProject(this, null, 0);
		if (newProj.dialogCancelled())
		{
			newProj.dispose();
			return false;
		}
		newProj.show();
		newProj.dispose();
		return m_dialogOK;
	}

	public void buildProjFile() throws IOException
	{
		LTabFileReader tab = new LTabFileReader();
		tab.addLine(OConsts.PROJ_FILE_IDENT + "\tDO NOT EDIT THIS LINE");
		tab.addLine(Integer.toString(OConsts.PROJ_CUR_VERSION) 
						+ "\tDO NOT EDIT THIS LINE");

		tab.addLine("Project name:\t" + m_projName);
		tab.addLine("Project root directory:\t" + m_projRoot);
		tab.addLine("OmegaT private directory:\t" + m_projInternal);
		tab.addLine("Source file root directory:\t" + m_srcRoot);
		tab.addLine("Translated file root directory:\t" + m_locRoot);
		tab.addLine("Glossary file directory:\t" + m_glosRoot);
		tab.addLine("Translation memory directory:\t" + m_tmRoot);
		tab.write(m_projFile);
	}

	protected boolean readProjFile(File fp) throws IOException
	{
		int i;
		int x;
		LTabFileReader tab = new LTabFileReader();
		tab.load(fp);
		String str = tab.get(0,0);
		if (str.compareTo(OConsts.PROJ_FILE_IDENT) != 0)
		{
			throw new IOException("unrecognized project file");
		}
		str = tab.get(1, 0);
		if (Integer.parseInt(str) == OConsts.PROJ_CUR_VERSION)
		{
			m_projFile	= fp.getAbsolutePath();
			m_projBase	= m_projFile.substring(0, m_projFile.lastIndexOf("."));
			m_projName	= tab.get(2,1);
			m_projRoot	= tab.get(3,1);
			m_projInternal	= tab.get(4,1);
			m_srcRoot	= tab.get(5,1);
			m_locRoot	= tab.get(6,1);
			m_glosRoot	= tab.get(7,1);
			m_tmRoot	= tab.get(8,1);
		}
		else
			throw new IOException("unsupported version");
	
		if (verifyProject() == false)
		{
			// something wrong with the project - display open dialog
			//  to fix it
			MNewProject prj = new MNewProject(this, m_projFile, 1);

			// continue  until user fixes problem or cancels
			boolean abort = false;
			while (true)
			{
				prj.show();
				if (m_dialogOK == false)
				{
					abort = true;
					break;
				}
				if (verifyProject() == true)
				{
					buildProjFile();
					break;
				}
			}
			prj.dispose();
			if (abort == true)
			{
				m_projRoot = "";
				m_projInternal = "";
				m_srcRoot = "";
				m_locRoot = "";
				m_glosRoot = "";
				m_tmRoot = "";
				return false;
			}
		}
		return true;
	}

	protected boolean verifyProject() throws IOException
	{
		// now see if these directories are where they're suposed to be
		File prj = new File(m_projRoot);
		File internal = new File(m_projInternal);
		File src = new File(m_srcRoot);
		File loc = new File(m_locRoot);
		File gls = new File(m_glosRoot);
		File tmx = new File(m_tmRoot);
		boolean changed = false;


		if (prj.exists() == false)
		{
			// try looking in position relative to project file
			File fp = new File(m_projFile);
			m_projRoot = fp.getParent();
			if (m_projRoot.endsWith(File.separator) == false)
				m_projRoot += File.separator;
			changed = true;
		}

		if (internal.exists() == false)
		{
			// reset path to be relative to proj root
			m_projInternal = setNewProjRoot(m_projInternal);
			internal = new File(m_projInternal);
			changed = true;
		}

		if (src.exists() == false)
		{
			// reset path to be relative to proj root
			m_srcRoot = setNewProjRoot(m_srcRoot);
			src = new File(m_srcRoot);
			changed = true;
		}

		if (loc.exists() == false)
		{
			// reset path to be relative to proj root
			m_locRoot = setNewProjRoot(m_locRoot);
			loc = new File(m_locRoot);
			changed = true;
		}

		if (gls.exists() == false)
		{
			// reset path to be relative to proj root
			m_glosRoot = setNewProjRoot(m_glosRoot);
			gls = new File(m_glosRoot);
			changed = true;
		}

		if (tmx.exists() == false)
		{
			// reset path to be relative to proj root
			m_tmRoot = setNewProjRoot(m_tmRoot);
			tmx = new File(m_tmRoot);
			changed = true;
		}

		if (src.exists() && loc.exists() && gls.exists() && 
					tmx.exists() && internal.exists())
		{
			if (changed == true)
			{
				buildProjFile();
			}
			return true;
		}
		
		return false;
	}

	// move designated directory to default position relative to proj directory
	protected String setNewProjRoot(String subdir)
	{
		// reset path to be relative to proj root
		if (subdir.endsWith(File.separator) == true)
			subdir = subdir.substring(0, subdir.length()-1);
		subdir = m_projRoot + subdir.substring(
						subdir.lastIndexOf(File.separator)+1);
		if (subdir.endsWith(File.separator) == false)
			subdir += File.separator;
		return subdir;
	}

	class MNewProject extends JDialog 
	{
		// msg: 0 for no message, 1 for bad project message
		public MNewProject(JFrame par, String projFileName, int msg)
		{
			super(par, true);
			m_dialogOK = false;
			setSize(650, 500);

			m_projRoot = "";
			m_projInternal = "";
			m_projName = "";
			m_srcRoot = "";
			m_locRoot = "";
			m_glosRoot = "";
			m_tmRoot = "";
			m_message = msg;

			m_browseTarget = 0;

			m_messageLabel = new JLabel();
			Box bMes = Box.createHorizontalBox();
			bMes.add(m_messageLabel);
			bMes.add(Box.createHorizontalGlue());


			m_projNameLabel = new JLabel();
			Box bName = Box.createHorizontalBox();
			bName.add(m_projNameLabel);
			bName.add(Box.createHorizontalGlue());
			m_projNameField = new JTextField();

			m_projRootLabel = new JLabel();
			Box bProj= Box.createHorizontalBox();
			bProj.add(m_projRootLabel);
			bProj.add(Box.createHorizontalGlue());
			m_projRootField = new JTextField();
			m_projRootField.setEditable(false);

			m_projInternalLabel = new JLabel();
			Box bInternal= Box.createHorizontalBox();
			bInternal.add(m_projInternalLabel);
			bInternal.add(Box.createHorizontalGlue());
			m_projInternalField = new JTextField();
			m_projInternalField.setEditable(false);

			m_srcRootLabel = new JLabel();
			Box bSrc = Box.createHorizontalBox();
			bSrc.add(m_srcRootLabel);
			bSrc.add(Box.createHorizontalGlue());
			m_srcBrowse = new JButton();
			bSrc.add(m_srcBrowse);
			m_srcRootField = new JTextField();
			m_srcRootField.setEditable(false);

			m_locRootLabel = new JLabel();
			Box bLoc = Box.createHorizontalBox();
			bLoc.add(m_locRootLabel);
			bLoc.add(Box.createHorizontalGlue());
			m_locBrowse = new JButton();
			bLoc.add(m_locBrowse);
			m_locRootField = new JTextField();
			m_locRootField.setEditable(false);

			m_glosRootLabel = new JLabel();
			Box bGlos = Box.createHorizontalBox();
			bGlos.add(m_glosRootLabel);
			bGlos.add(Box.createHorizontalGlue());
			m_glosBrowse = new JButton();
			bGlos.add(m_glosBrowse);
			m_glosRootField = new JTextField();
			m_glosRootField.setEditable(false);

			m_tmRootLabel = new JLabel();
			Box bTM = Box.createHorizontalBox();
			bTM.add(m_tmRootLabel);
			bTM.add(Box.createHorizontalGlue());
			m_tmBrowse = new JButton();
			bTM.add(m_tmBrowse);
			m_tmRootField = new JTextField();
			m_tmRootField.setEditable(false);

			m_okButton = new JButton();
			m_cancelButton = new JButton();

			Box b = Box.createVerticalBox();
			b.add(bMes);
			b.add(bName);
			b.add(m_projNameField);
			b.add(bProj);
			b.add(m_projRootField);
			b.add(bInternal);
			b.add(m_projInternalField);
			b.add(bSrc);
			b.add(m_srcRootField);
			b.add(bLoc);
			b.add(m_locRootField);
			b.add(bGlos);
			b.add(m_glosRootField);
			b.add(bTM);
			b.add(m_tmRootField);
			getContentPane().add(b, "North");
			Box b2 = Box.createHorizontalBox();
			b2.add(Box.createHorizontalGlue());
			b2.add(m_cancelButton);
			b2.add(Box.createHorizontalStrut(5));
			b2.add(m_okButton);
			getContentPane().add(b2, "South");

			m_okButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doOK();
				}
			});

			m_cancelButton.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					doCancel();
				}
			});
	
			m_srcBrowse.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_browseTarget = 1;
					doBrowseDirectoy();
				}
			});
	
			m_locBrowse.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_browseTarget = 2;
					doBrowseDirectoy();
				}
			});
	
			m_glosBrowse.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_browseTarget = 3;
					doBrowseDirectoy();
				}
			});
	
			m_tmBrowse.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					m_browseTarget = 4;
					doBrowseDirectoy();
				}
			});
	
			// if no project file specified, ask user to select one
			if (projFileName == null)
			{
				// have user select the project file name and where he/she
				//  wants to put it.  use that information to derive the
				//  location for project, source and loc directories
				// open save dialog
				JFileChooser jfc = new JFileChooser();
				String label;
				label = OStrings.PP_SAVE_PROJECT_FILE;
				jfc.setDialogTitle(label);
				
				int val = jfc.showOpenDialog(this);
				if (val == JFileChooser.APPROVE_OPTION)
				{
					m_projFile = jfc.getSelectedFile().getAbsolutePath();
					// if no filename specified, consider same as cancel
					if ((m_projFile == null) || (m_projFile.equals("")))
					{
						m_dialogCancelled = true;
						return;
					}
					if (m_projFile.endsWith(OConsts.PROJ_EXTENSION) == false)
					{
						m_projFile += OConsts.PROJ_EXTENSION;
					}
				}
				else
				{
					m_dialogCancelled = true;
					return;
				}
				File projDir = jfc.getCurrentDirectory();
				m_projRoot = projDir.getAbsolutePath();
				m_projRoot = verifyPathString(m_projRoot, "");
			}
			else
			{
				m_projFile = projFileName;
				m_projRoot = m_projFile.substring(0, 
								m_projFile.lastIndexOf(File.separator));
				m_projRoot = verifyPathString(m_projRoot, "");
			}
			m_projBase = m_projFile.substring(0, 
								m_projFile.lastIndexOf("."));
			m_projInternal = m_projRoot + OConsts.DEFAULT_INTERNAL;
			m_srcRoot = m_projRoot + OConsts.DEFAULT_SRC;
			m_locRoot = m_projRoot + OConsts.DEFAULT_LOC;
			m_glosRoot = m_projRoot + OConsts.DEFAULT_GLOS;
			m_tmRoot = m_projRoot + OConsts.DEFAULT_TM;
			m_projRootField.setText(m_projRoot);
			m_projInternalField.setText(m_projInternal);
			m_srcRootField.setText(m_srcRoot);
			m_locRootField.setText(m_locRoot);
			m_glosRootField.setText(m_glosRoot);
			m_tmRootField.setText(m_tmRoot);

			updateUIText();
		}

		// make sure path not null and make sure ends with '/'
		private String verifyPathString(String pth, String location)
		{
			if ((pth == null) || (pth.equals("")))
			{
				pth = m_projRoot + location;
			}
			if (pth.endsWith(File.separator) == false)
				return pth + File.separator;
			else
				return pth;
		}

		private void doBrowseDirectoy()
		{
			String title = "";
			switch (m_browseTarget)
			{
				case 1:
					title = OStrings.PP_BROWSE_TITLE_SOURCE;
					break;

				case 2:
					title = OStrings.PP_BROWSE_TITLE_TARGET;
					break;

				case 3:
					title = OStrings.PP_BROWSE_TITLE_GLOS;
					break;

				case 4:
					title = OStrings.PP_BROWSE_TITLE_TM;
					break;

				default:
					return;
			};
			JFileChooser browser = new JFileChooser();
			String str = OStrings.PP_BUTTON_SELECT;
			browser.setApproveButtonText(str);
			browser.setDialogTitle(title);
			browser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

			int res = browser.showOpenDialog(this);
			File dir = browser.getSelectedFile();
			if (dir == null)
				return;

			str = dir.getAbsolutePath() + File.separator;
			// reset appropriate path
			switch (m_browseTarget)
			{
				case 1:
					m_srcRoot = str;
					m_srcRootField.setText(m_srcRoot);
					break;

				case 2:
					m_locRoot = str;
					m_locRootField.setText(m_locRoot);
					break;

				case 3:
					m_glosRoot = str;
					m_glosRootField.setText(m_glosRoot);
					break;

				case 4:
					m_tmRoot = str;
					m_tmRootField.setText(m_tmRoot);
					break;

			};
		}

		private void doOK()
		{
			String str;

			m_projName = m_projNameField.getText();
			if (m_projName.compareTo("") == 0)
			{	
				m_projName = OStrings.PP_DEFAULT_PROJECT_NAME;
			}

			m_projRoot = m_projRootField.getText();
			if (m_projRoot.compareTo("") == 0)
			{
				File fl = new File(m_projFile);
				m_projRoot = fl.getParent();
			}
			m_projRoot = verifyPathString(m_projRoot, "");

			m_projInternal = m_projInternalField.getText();
			m_projInternal = verifyPathString(m_projInternal, 
											OConsts.DEFAULT_INTERNAL);

			m_srcRoot = m_srcRootField.getText();
			m_srcRoot = verifyPathString(m_srcRoot, OConsts.DEFAULT_SRC);

			m_locRoot = m_locRootField.getText();
			m_locRoot = verifyPathString(m_locRoot, OConsts.DEFAULT_LOC);

			m_glosRoot = m_glosRootField.getText();
			m_glosRoot = verifyPathString(m_glosRoot, OConsts.DEFAULT_GLOS);

			m_tmRoot = m_tmRootField.getText();
			m_tmRoot = verifyPathString(m_tmRoot, OConsts.DEFAULT_TM);

			m_dialogOK = true;
			hide();

			m_browseTarget = 0;
		}

		private void doCancel()
		{
			m_dialogOK = false;
			hide();
		}

		public void updateUIText()
		{
			String str;

			str = OStrings.PP_CREATE_PROJ;
			setTitle(str);
	
			if (m_message == 0)
				m_messageLabel.setText("");
			else if (m_message == 1)
			{
				str = OStrings.PP_MESSAGE_BADPROJ;
				m_messageLabel.setText(str);
			}

			str = OStrings.PP_PROJECT_NAME;
			m_projNameLabel.setText(str);
	
			str = OStrings.PP_PROJ_ROOT;
			m_projRootLabel.setText(str);

			str = OStrings.PP_PROJ_INTERNAL;
			m_projInternalLabel.setText(str);

			str = OStrings.PP_SRC_ROOT;
			m_srcRootLabel.setText(str);

			str = OStrings.PP_BUTTON_BROWSE_SRC;
			m_srcBrowse.setText(str);
			
			str = OStrings.PP_LOC_ROOT;
			m_locRootLabel.setText(str);
			str = OStrings.PP_BUTTON_BROWSE_TAR;
			m_locBrowse.setText(str);
			
			str = OStrings.PP_GLOS_ROOT;
			m_glosRootLabel.setText(str);
			str = OStrings.PP_BUTTON_BROWSE_GL;
			m_glosBrowse.setText(str);
			
			str = OStrings.PP_TM_ROOT;
			m_tmRootLabel.setText(str);
			str = OStrings.PP_BUTTON_BROWSE_TM;
			m_tmBrowse.setText(str);
			
			m_okButton.setText(OStrings.PP_BUTTON_OK);

			m_cancelButton.setText(OStrings.PP_BUTTON_CANCEL);
		}

		public boolean dialogCancelled()	{ return m_dialogCancelled;	}
		private boolean		m_dialogCancelled;

		private int				m_browseTarget;

		public JLabel		m_messageLabel;
		public int			m_message;

		public JLabel		m_projRootLabel;
		public JTextField	m_projRootField;

		public JLabel		m_projInternalLabel;
		public JTextField	m_projInternalField;

		public JLabel		m_projNameLabel;
		public JTextField	m_projNameField;

		public JLabel		m_srcRootLabel;
		public JTextField	m_srcRootField;
		public JButton		m_srcBrowse;

		public JLabel		m_locRootLabel;
		public JTextField	m_locRootField;
		public JButton		m_locBrowse;

		public JLabel		m_glosRootLabel;
		public JTextField	m_glosRootField;
		public JButton		m_glosBrowse;

		public JLabel		m_tmRootLabel;
		public JTextField	m_tmRootField;
		public JButton		m_tmBrowse;

		public JButton		m_okButton;
		public JButton		m_cancelButton;
	}

	private String	m_projName;
	private String	m_projFile;
	private String	m_projBase;
	private String	m_projRoot;
	private String	m_projInternal;
	private String	m_srcRoot;
	private String	m_locRoot;
	private String	m_glosRoot;
	private String	m_tmRoot;
	
	protected boolean m_dialogOK;

//////////////////////////////////////////////////////////

	public static void main(String[] s)
	{
		ProjectProperties pp = new ProjectProperties();
		pp.createNew();
	}
}
