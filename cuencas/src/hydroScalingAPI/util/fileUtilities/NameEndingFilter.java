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
public class NameEndingFilter extends Object implements java.io.FileFilter{
    
    String myName;
    String myEnding;
    
    /**
     * Creates new NameEndingFilter
     * @param end The desired file name ending (last letters in the extension)
     * @param name The base name for the group of files
     */
    public NameEndingFilter(String name,String end) {
        myName=name;
        myEnding=end.toLowerCase();
    }
    
    /**
     * The accept criteria based on the file name
     * @param file The file to compare with
     * @return A boolean flag indicating sucess or failure of the test
     */
    public boolean accept(java.io.File file) {
        int posPeriod=(int)Math.max(file.getName().lastIndexOf("."),0);
        return file.getName().substring(posPeriod).toLowerCase().lastIndexOf(myEnding) != -1 && 
               file.getName().lastIndexOf(myName+".") != -1 &&
               !file.getName().substring(posPeriod+1).toLowerCase().equalsIgnoreCase("tri") &&
               !file.getName().substring(posPeriod+1).toLowerCase().equalsIgnoreCase("edges") &&
               !file.getName().substring(posPeriod+1).toLowerCase().equalsIgnoreCase("nodes");
    }
    
}
