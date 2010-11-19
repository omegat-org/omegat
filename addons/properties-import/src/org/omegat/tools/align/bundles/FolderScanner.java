/**************************************************************************
 OmegaT Addon - Import of legacy translations of Java(TM) Resource Bundles
 Copyright (C) 2004-05  Maxym Mykhalchuk
                        mihmax@gmail.com

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

package org.omegat.tools.align.bundles;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;

/**
 * Class that recursively scans the directories and
 * passes found files for scan with FileScanner.
 *
 * @author  Maxym Mykhalchuk
 */
public class FolderScanner
{
    private String folder;
    private String locale;
    private TMXSaver saver;
    
    /** Creates a new instance of FolderScanner */
    public FolderScanner(String folder, String locale, TMXSaver saver)
    {
        this.folder = folder;
        this.locale = locale;
        this.saver = saver;
    }
    
    public void scan() throws IOException
    {
        final File thisfolder = new File(folder);
        
        File childfolders[] = thisfolder.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                return pathname.isDirectory();
            }
        }
        );
        for(int i=0; i<childfolders.length; i++)
            new FolderScanner(childfolders[i].getAbsolutePath(), locale, saver).scan();
        
        File childfiles[] = thisfolder.listFiles(new FileFilter()
        {
            public boolean accept(File pathname)
            {
                if( !pathname.isDirectory() )
                {
                    String fullname = pathname.getName();
                    if( fullname.lastIndexOf('.')<0 )
                        return false;
                    String name = fullname.substring(0, fullname.lastIndexOf('.'));
                    String ext  = fullname.substring(fullname.lastIndexOf('.')+1);
                    
                    return name.indexOf('_')<0 && ext.equalsIgnoreCase("properties");       
                }
                else
                    return false;
            }
        }
        );
        for(int i=0; i<childfiles.length; i++)
            new FileScanner(childfiles[i].getAbsolutePath(), locale, saver).scan();
        
    }
    
}
