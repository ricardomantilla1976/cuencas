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
 * Pair.java
 *
 * Created on March 21, 2005, 12:16 PM
 */

package hydroScalingAPI.tools;

/**
 * An auxiliary object composed by two properties.  It allows to sort a group of
 * this objects using property1
 * @author Peter Furey
 */
public class Pair implements Comparable{

    /**
     * The master property
     */
    public float property1;
    /**
     * The secondary property
     */
    public float property2;
    
    
    /**
     * Creates a new instance of Pair
     * @param a The value to assign to the master property
     * @param b The value to assign to the secondary property
     */
    public Pair(float a, float b) {
        property1=a;
        property2=b;
    }
    
    /**
     * The compareTo implementation for the Pair object
     * @param o The object to be compared against
     * @return A negative integer, zero, or a positive integer as this object is less than, equal
     * to, or greater than the specified object.
     */
    public int compareTo(Object o) {
        
        Pair newPair=(Pair)o;
        
        if (this.property1 > newPair.property1) return 1;
        if (this.property1 < newPair.property1) return -1;
        if (this.property1 == newPair.property1) return 0;
        
        return 0;
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args ) {
        
        Pair[] mypairs=new Pair[5];
        
        //mypairs[0]=new Pair(2,40);
        //mypairs[1]=new Pair(5,50);
        //mypairs[2]=new Pair(3,40);
        //mypairs[3]=new Pair(1,30);
        //mypairs[4]=new Pair(9,40);
        
        //for(int i=0;i<5;i++) System.out.println(mypairs[i].property1+" "+mypairs[i].property2);
        
        java.util.Arrays.sort(mypairs);
        
        //for(int i=0;i<5;i++) System.out.println(mypairs[i].property1+" "+mypairs[i].property2);
        
    }
    
    
    
}
