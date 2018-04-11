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
 * ExtensionToFormat.java
 *
 * Created on June 28, 2003, 11:44 AM
 */

package hydroScalingAPI.tools;

/**
 * An abstract class to assign formats to file extension
 * @author Ricardo Mantilla
 */
public abstract class ExtensionToFormat {
    
    /**
     * Given a file extension return the associated format.  Available extensions
     * are:<br>
     * <p>.areas</p>
     * <p>.corrDEM</p>
     * <p>.dir</p>
     * <p>.dtopo</p>
     * <p>.gdo</p>
     * <p>.horton</p>
     * <p>.lcp</p>
     * <p>.ltc</p>
     * <p>.mcd</p>
     * <p>.magn</p>
     * <p>.redRas</p>
     * <p>.slope</p>
     * <p>.tdo</p>
     * <p>.tcd</p>
     * @param extension The extension of interest
     * @return The associated format
     */
    public static String getFormat(String extension){
        java.util.Hashtable extToFormat=new java.util.Hashtable();

        extToFormat.put(".areas","Float");
        extToFormat.put(".corrDEM","Double");
        extToFormat.put(".dir","Byte");
        extToFormat.put(".dtopo","Integer");
        extToFormat.put(".gdo","Float");
        extToFormat.put(".horton","Byte");
        extToFormat.put(".lcp","Float");
        extToFormat.put(".ltc","Float");
        extToFormat.put(".mcd","Float");
        extToFormat.put(".magn","Integer");
        extToFormat.put(".redRas","Byte");
        extToFormat.put(".slope","Double");
        extToFormat.put(".tdo","Integer");
        extToFormat.put(".tcd","Float");
        
        return (String)extToFormat.get(extension);
    }
    
}
