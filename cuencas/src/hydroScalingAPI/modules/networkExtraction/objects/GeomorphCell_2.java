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


package hydroScalingAPI.modules.networkExtraction.objects;

/**
 * A cell object that associates properites to a location in the DEM.  The
 * properties are used by the GetGeomorphology algorithms.
 * @author Jorge Mario Ramirez
 */
public class GeomorphCell_2 extends Object {
    /**
     * The longest channel lenght up th this point
     */
    public double lcp;
    /**
     * The magnitude of the location
     */
    public int magn;
    /**
     * The total channel lenght up to this location
     */
    public double ltc;
    /**
     * The longes topologic distance up to this point (topologic diameter)
     */
    public int d_topo;
    /**
     * The total drop along the channels
     */
    public double tcd;
    /**
     * The maximum drop along the channels
     */
    public double mcd;
    
}
