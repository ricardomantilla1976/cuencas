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


package hydroScalingAPI.examples.conversion;

import java.lang.*;
import java.io.*;
import geotransform.coords.*;
import geotransform.ellipsoids.*;
import geotransform.transforms.*;

/**
 * Test of the geotransform package
 * @author Ricardo Mantilla
 */
public class conv_gdc_utm
{
    static final int MAX_POINTS = 1; // total number of points
    
    /**
     * 
     * @param argv 
     * @throws java.io.IOException 
     */
    public static void main(String argv[]) throws IOException
    {
       
        int i; // iterator
	double lat, lon;
	String s1, s2;
	DataInput d = new DataInputStream(System.in);

        // Gdc_Coord_3d gdc[] = new Gdc_Coord_3d[MAX_POINTS]; // these need to be the same length.
        // Utm_Coord_3d utm[] = new Utm_Coord_3d[MAX_POINTS];
        
	Gdc_Coord_3d gdc_point = new Gdc_Coord_3d();
	Utm_Coord_3d utm_point = new Utm_Coord_3d();
        
        Gdc_To_Utm_Converter.Init(new WE_Ellipsoid());        

	while (gdc_point.latitude != -999.0) {
	  System.out.print("Latitude? ");
	  System.out.flush();
	  s1 = d.readLine();
	  System.out.print("Longitude? ");
	  System.out.flush();
	  s2 = d.readLine();
  
	  gdc_point = new Gdc_Coord_3d(Double.valueOf(s1).doubleValue(), Double.valueOf(s2).doubleValue(),100.0);
	  // convert the points.
     
	  Gdc_To_Utm_Converter.Convert(gdc_point,utm_point); // with points
        
	  System.out.println("\nGdc.latitude: " + gdc_point.latitude);
	  System.out.println("Gdc.longitude: " + gdc_point.longitude);
	  System.out.println("Gdc.elevation: " + gdc_point.elevation);
        
	  System.out.println("\nUtm.x: " + utm_point.x);
	  System.out.println("Utm.y: " + utm_point.y);
	  System.out.println("Utm.z: " + utm_point.z);
	  System.out.println("Utm.zone: " + utm_point.zone);
	  System.out.println("Utm.hemisphere_north: " + utm_point.hemisphere_north);
	} // while
    } // end main
}// end test
