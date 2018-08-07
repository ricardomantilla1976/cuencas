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
 * SelectWhitewatersDLGs.java
 *
 * Created on March 30, 2004, 11:01 AM
 */

package hydroScalingAPI.examples.dlgs;

/**
 *
 * @author Ricardo Mantilla
 */
public class SelectWhitewatersDLGs {
    
    /** Creates a new instance of SelectWhitewatersDLGs */
    public SelectWhitewatersDLGs(String location) {
        
        java.io.File[] filesToLookAt=new java.io.File(location).listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("dlg"));
        
        for(int i=0;i<filesToLookAt.length;i++){
            try{
                hydroScalingAPI.io.MetaDLG metaDLG1=new hydroScalingAPI.io.MetaDLG (filesToLookAt[i].getPath(),new java.awt.geom.Rectangle2D.Double(-97-19/60.,37+39/60.,1,1));
            } catch (java.io.IOException IOE){
                System.err.println("BAD FILE: "+filesToLookAt[i].getName());
            } catch (visad.VisADException vie){
                System.err.println("BAD FILE: "+filesToLookAt[i].getName());
            }
        }
        
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String locationDir="/home/ricardo/garbage/gisdasc.kgs.ukans.edu/gisdata/dlg/dlg_100k/roads";
        new SelectWhitewatersDLGs(locationDir);
    }
    
}
