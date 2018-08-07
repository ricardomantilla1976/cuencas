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
 * UniformDiscreteDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 * An implementation of {@link hydroScalingAPI.util.probability.DiscreteDistribution}
 * using a uniform distribution P(i)=i/invervalSize
 * @author Ricardo Mantilla
 */
public class UniformDiscreteDistribution implements hydroScalingAPI.util.probability.DiscreteDistribution{
    
    private int lowerLimit, upperLimit;
    
    /**
     * Creates a new instance of UniformDiscreteDistribution
     * @param ll The lower limit of the random variable
     * @param ul The upper limit of the random variable
     */
    public UniformDiscreteDistribution(int ll,int ul) {
        lowerLimit=ll;
        upperLimit=ul;
    }
    
    /**
     * Returns a random value that follows a discrete uniform distribution
     * @return A random value
     */
    public int sample() {
        return (int) (Math.random()*(upperLimit-lowerLimit+1)+lowerLimit);
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        UniformDiscreteDistribution myUD=new UniformDiscreteDistribution(0,10);
        for(int i=0;i<10;i++)
            System.out.println(myUD.sample());
    }
    
}
