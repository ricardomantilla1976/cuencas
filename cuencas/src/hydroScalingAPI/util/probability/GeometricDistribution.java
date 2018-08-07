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
 * UniformDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 * An implementation of {@link hydroScalingAPI.util.probability.DiscreteDistribution}
 * using a geometric distribution P(i)=p*(1-p)^i
 * @author Ricardo Mantilla
 */
public class GeometricDistribution implements hydroScalingAPI.util.probability.DiscreteDistribution{
    
    private double base;
    private int minValue;
    
    /**
     * Creates a new instance of GeometricDistribution
     * @param theBase The probability of success
     * @param theMinVal The lower limit of the random variable
     */
    public GeometricDistribution(double theBase, int theMinVal) {
        
        base=theBase;
        minValue=theMinVal;
        
    }
    
    /**
     * Returns a random value that follows a geometric distribution
     * @return A random value
     */
    public int sample() {
        double ranNum=Math.random();
        return (int)Math.floor(Math.log(ranNum)/Math.log(1-base))+minValue;
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GeometricDistribution myUD=new GeometricDistribution(0.5,0);
        for(int i=0;i<100;i++) {
            System.out.println(myUD.sample());//+",");
            //if(i % 100 == 0) System.out.println("$");
        }
        //System.out.println(myUD.sample()+"$");
    }
    
}
