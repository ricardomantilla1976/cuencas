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
 * GaussianDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 * An implementation of {@link hydroScalingAPI.util.probability.ContinuousDistribution}
 * using a gaussian distribution
 * @author Ricardo Mantilla
 */
public class GaussianDistribution implements hydroScalingAPI.util.probability.ContinuousDistribution{
    
    private float mean, stdev;
    private java.util.Random rn;
    
    /**
     * Creates a new instance of GaussianDistribution
     * @param m The mean
     * @param s The standard deviation
     */
    public GaussianDistribution(float m,float s) {
        mean=m;
        stdev=s;
        rn=new java.util.Random();
    }
    
    /**
     * Returns a random value that follows a gaussian distribution
     * @return A random value
     */
    public float sample() {
        return (float) (rn.nextGaussian()*stdev+mean);
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        GaussianDistribution myUD=new GaussianDistribution(2,4);
        for(int i=0;i<10;i++)
            System.out.println(myUD.sample());
    }
    
}
