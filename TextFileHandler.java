//-------------------------------------------------------------------------
//  
//  TextFileHandler.java - 
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
//  Build date:  8Mar2003
//  Copyright (C) 2002, Keith Godfrey
//  keithgodfrey@users.sourceforge.net
//  907.223.2039
//  
//  OmegaT comes with ABSOLUTELY NO WARRANTY
//  This is free software, and you are welcome to redistribute it
//  under certain conditions; see 'gpl.txt' for details
//
//-------------------------------------------------------------------------

import java.io.*;
import java.util.*;

class TextFileHandler extends FileHandler
{
	public TextFileHandler()
	{
		super("textfile", "txt");
	}

	public TextFileHandler(String type, String ext)
	{
		super(type, ext);
	}

	public void doLoad() throws IOException
	{ 
		throw new IOException("unsupported function"); 
	}

	// NOTE dengue code change - review at future date
	public String translateFileEncoding()
	{
		String code = "ISO-8859-1";
		String type = type();
		if (type.equals("textfile-latin1"))
			code = "ISO-8859-1";
		else if (type.equals("textfile-latin2"))
			code = "ISO-8859-2";
		else if (type.equals("textfile-utf8"))
			code = "UTF8";
//		else if (m_type.equals("textfile-shiftjis"))
//			code = "SHIFT-JIS";

		return code;
	}

	// create output stream - allow stream to have access to source file
	//  if necessary
	public BufferedReader createInputStream(String infile)
			throws IOException
	{
		FileInputStream fis = new FileInputStream(infile);
		//InputStreamReader isr = new InputStreamReader(fis);
		String code = translateFileEncoding();
		InputStreamReader isr = new InputStreamReader(fis, code);
		BufferedReader br = new BufferedReader(isr);
		return br;
	}

	public BufferedWriter createOutputStream(String infile, String outfile)
			throws IOException
	{
		FileOutputStream fos = new FileOutputStream(outfile);
		//OutputStreamWriter osw = new OutputStreamWriter(fos);
		String code = translateFileEncoding();
		OutputStreamWriter osw = new OutputStreamWriter(fos, code);
		BufferedWriter bw = new BufferedWriter(osw);
		return bw;
	}

	public void load(String file) throws IOException
	{
		int i;
		String t;
		String srcText = "";
		String s;
		String nonTrans = "";
		int ctr = 0;

		m_in = createInputStream(file);
//		BufferedReader in = new BufferedReader(new FileReader(file));
//		while ((s = m_in.readLine()) != null)
		while ((s = getNextLine()) != null)
		{
			ctr++;
			t = s.trim();
			if (t.length() == 0)
			{
				nonTrans += s + "\n";
				continue;
			}
			srcText = s;
			
			if (m_outFile != null)
			{
				m_outFile.write(nonTrans);
				nonTrans = "";
			}
			processEntry(srcText, file);
			nonTrans += "\n";
		}
		if ((m_outFile != null) && (nonTrans.compareTo("") != 0))
		{
			m_outFile.write(nonTrans);
		}
	}

///////////////////////////////////////////////////////////
	
	public static void main(String[] args)
	{
		TextFileHandler txt = new TextFileHandler();
		CommandThread.core = new CommandThread(null);
		txt.setTestMode(true);
		try 
		{
			txt.load("src/glos1.txt");
		}
		catch (IOException e)
		{
			System.out.println("failed to open test file");
		}
	}
}
