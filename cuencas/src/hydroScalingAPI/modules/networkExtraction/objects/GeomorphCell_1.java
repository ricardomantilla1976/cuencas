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
public class GeomorphCell_1 extends Object {
    /**
     * The status of the cell acording the the moving downstream algorithm.
     */
    public int status;
    /**
     * Indicates if this cell is a topologic point of change (a junction)
     */
    public boolean pcambio;
    /**
     * The Strahler order of this cell
     */
    public int orden;
    /**
     * The number of network points that drain into this location
     */
    public int llegan_red;
    
    /**
     * Creates a new GeomophCell_1 object
     * @param sstat The initial status of the cell
     */
    public GeomorphCell_1(int sstat) {
        status = sstat;
    }
    
}
