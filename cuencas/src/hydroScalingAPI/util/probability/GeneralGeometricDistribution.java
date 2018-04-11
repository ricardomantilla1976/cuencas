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
 * GeneralGeometricDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 * An implementation of {@link hydroScalingAPI.util.probability.DiscreteDistribution}
 * using a generalized geometric distribution P(i)=b*c^i, i>1 and P(0)=1-sum(P(i))
 * @author Ricardo Mantilla
 */
public class GeneralGeometricDistribution implements hydroScalingAPI.util.probability.DiscreteDistribution{
    
    private double coeff, base;
    private int minValue;
    
    private double p0;
    
    /**
     * Creates a new instance of GeneralGeometricDistribution
     * @param theCoeff The probability of success
     * @param theBase The probability of failure
     * @param theMinVal The lower limit of the random variable
     */
    public GeneralGeometricDistribution(double theCoeff, double theBase, int theMinVal) {
        
        /*Results for Walnut Creek
         *
         *For interior generators
         *0.59062127 - theBase
         *0.25756657 - theCoeff
         *
         *For exterior generators
         *0.57253316 - theBase
         *0.19803630 - theCoeff
         *
        */

        
        coeff=theCoeff;
        base=theBase;
        minValue=theMinVal;
        
        p0=1-coeff/(1-base);
        
        System.out.println(p0);
        
    }
    
    /**
     * Returns a random value that follows a generalized geometric distribution
     * @return A random value
     */
    public int sample() {
        double ranNum=Math.random();
        if (ranNum > p0) return minValue;
        return (int)Math.floor(Math.log(ranNum/coeff)/Math.log(base)+minValue+1);
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GeneralGeometricDistribution myUD=new GeneralGeometricDistribution(0.25756657,0.59062127,0);
        for(int i=0;i<10000;i++) {
            System.out.print(myUD.sample()+",");
            if(i % 100 == 0) System.out.println("$");
        }
        System.out.println(myUD.sample()+"$");
    }
    
}
