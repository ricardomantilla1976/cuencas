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
 * DotFilter.java
 *
 * Created on 3 de diciembre de 2000, 17:20
 */

package hydroScalingAPI.util.fileUtilities;

/**
 * An implementation of the {@link java.io.FileFilter} that allows files with a
 * predetermined extension
 * @author Ricardo Mantilla
 */
public class DotFilter extends javax.swing.filechooser.FileFilter implements java.io.FileFilter{

    String[] myExt;
    
    /**
     * Creates new DotFilter
     * @param ext The desired extension or a comma separated lists of acceptable extensions.
     * The filter asumes that the extension is the string after the last occurence of a
     * period in the file name.  Thus extension cannot have periods embedded.
     */
    public DotFilter(String ext) {
        myExt=ext.toLowerCase().split(",");
    }

    /**
     * The accept criteria based on the file name
     * @param p1 The file to filter
     * @return true if and only if the file contains one of the extensions allowed
     */
    public boolean accept(final java.io.File p1) {
        boolean extExists=false;
        for(int i=0;i<myExt.length;i++) extExists|=p1.getName().toLowerCase().lastIndexOf("."+myExt[i]) != -1;
        return extExists;
    }
    
    public String getDescription(){
        return null;
    }
}
