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
 * properties are used by the NetworkExtraction Module to make decision during the
 * D8 algorithm
 * @author Jorge Mario Ramirez
 */
public class GeomorphCell_0 extends Object implements Comparable{
    
    /**
     * The row index of the location
     */
    public int i ;
    /**
     * The column index of the location
     */
    public int j ;
    /**
     * The variable that will serve to establis the order critieria for this object. 
     * Usually the location elevation.
     */
    public double var_to_compare;
    /**
     * The elevation of the location
     */
    public double height ;
    /**
     * The minimum elevation of the adyacent cells (8 neighboors)
     */
    public double min_ady;
    
    private double distopit;
    private int i_romper;
    private int j_romper;
    
    /**
     * Creates an instance of the GeomorphCell_0 class
     * @param ii The row number of the location
     * @param jj The column number of the location
     * @param ccota The elevation of the location
     * @param mmin_ady The minimum elevation of the adjacents cell
     */
    public GeomorphCell_0(int ii, int jj, double ccota, double mmin_ady){
        i = ii;
        j = jj;
        height = ccota;
        var_to_compare = ccota;
        min_ady =  mmin_ady;
    }
    //ESTE COMPARADOR ORGANIZA LAS CELDAS DE MENOR A MAYOR SEGUN SU COTA
    /**
     * The comparison criteria for the GeomorphCell_0
     * @param c1 The GeomorphCell_0 to be compared with
     * @return A negative integer, zero, or a positive integer as this object is less than, equal
     * to, or greater than the specified object.
     */
    public int compareTo(java.lang.Object c1) {
        int comp;
        GeomorphCell_0 thisGeomorphCell_0 =(GeomorphCell_0)c1;
        comp = (int)(( var_to_compare - thisGeomorphCell_0.var_to_compare)/Math.abs(var_to_compare - thisGeomorphCell_0.var_to_compare));
        return comp;
    }
    
    /**
     * The euclidian distance to another cell
     * @param cell The reference cell
     * @return A double with the distance in cartesian units
     */
    public double euclid_distance(GeomorphCell_0 cell){
        return Math.sqrt(Math.pow(this.i - cell.i,2) + Math.pow(this.j - cell.j,2));
    }
    
    /**
     * Finds the shortest distance between the cell and the cells in the Pit
     * @param pitcells The reference Pit
     */
    public void findDisToPit(Object[] pitcells){
        
        
        for (int k=0; k< pitcells.length ; k++){
            GeomorphCell_0 thisCell=(GeomorphCell_0)pitcells[k];
            thisCell.var_to_compare = euclid_distance(thisCell);
        }
        java.util.Arrays.sort(pitcells);
        distopit =((GeomorphCell_0)pitcells[0]).var_to_compare;
        var_to_compare=distopit;
        i_romper=((GeomorphCell_0)pitcells[0]).i;
        j_romper=((GeomorphCell_0)pitcells[0]).j;
        
    }
    
}
