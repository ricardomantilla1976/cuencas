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
 * read_and_plot.java
 *
 * Created on March 7, 2003, 3:25 PM
 */

package hydroScalingAPI.examples.netCDF;

/**
 *
 * @author Ricardo Mantilla
 */
public class read_and_plot {
    
    /** Creates a new instance of read_and_plot */
    public read_and_plot() {
        
        visad.Field imagesNC = null;
        
        try {
            imagesNC = (visad.Field) (new visad.data.netcdf.Plain().open("/home/ricardo/temp/firstTest.nc"));
        } catch (java.io.IOException exc) {
            String s =  "To run this example, the images.nc file must be "
                        +"present in\nyour visad/examples directory."
                        +"You can obtain this file from:\n"
                        +"  ftp://ftp.ssec.wisc.edu/pub/visad-2.0/images.nc.Z";
            System.out.println(s);
            System.exit(0);
        } catch (visad.VisADException VisE) {
            System.err.println(VisE);
        }
        
        System.out.println(imagesNC);
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        new read_and_plot();
        
    }
    
}
