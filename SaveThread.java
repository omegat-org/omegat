//-------------------------------------------------------------------------
//  
//  SaveThread.java - 
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
//  Build date:  23Feb2002
//  Copyright (C) 2002, Keith Godfrey
//  aurora@coastside.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.lang.*;

class SaveThread extends Thread
{
	public SaveThread()
	{
//System.out.println("creating save thread");
		setName("Save thread");
		m_timeToDie = false;
		m_saveDuration = 300000;	// 5 minutes
	}

	public void run()
	{
		try
		{
			sleep(m_saveDuration);
		}
		catch (InterruptedException e2)
		{
			;	// let it pass
		}
		
		while (m_timeToDie == false)
		{
			CommandThread.core.save();
			try 
			{
				sleep(m_saveDuration);
			}
			catch (InterruptedException e)
			{
				;	// this is OK
			}
		}
	}

	public void signalStop()	{ m_timeToDie = true;	}
	
	public void	setSaveDuration(int microSeconds)
	{
		// save interfal less than 15 seconds not necessary
		if (microSeconds <= 15000)
			m_saveDuration = 15000;
		else 
			m_saveDuration = microSeconds;
	}

	protected boolean	m_timeToDie;
	protected int		m_saveDuration;
}
