/*
CUENCAS is a River Network Oriented GIS
Copyright (C) 2005  Ricardo Mantilla

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


/*
 * NameDotFilter.java
 *
 * Created on June 15, 2003, 2:05 PM
 */

package hydroScalingAPI.util.fileUtilities;

/**
 * An implementation of the {@link java.io.FileFilter} that allows files with a
 * predetermined extension and a predetermined initial label (e.g. corresponds to the wildcard
 * name*ext)
 * @author Ricardo Mantilla
 */
public class NameDotFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter{
    
    String myName;
    String myExt;
    
    /**
     * Creates new NameDotFilter
     * @param ext The desired extension
     * @param name The base name for the group of files
     */
    public NameDotFilter(String name,String ext) {
        myName=name;
        myExt=ext.toLowerCase();
    }
    
    /**
     * The accept criteria based on the file name
     * @param file The file to compare with
     * @return A boolean flag indicating sucess or failure of the test
     */
    public boolean accept(java.io.File file) {
        return file.getName().toLowerCase().lastIndexOf("."+myExt) != -1 && 
               file.getName().lastIndexOf(myName) != -1;
    }
    
    public String getDescription(){
        return null;
    }
    
}
