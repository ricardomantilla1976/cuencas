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
 * using a binary distribution P(a) = p and P(b)= 1-p
 * @author Ricardo Mantilla
 */
public class ScaleDependentSequenceDistribution implements hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution{
    
    private int[] sequence;
    
    /**
     * Creates a new instance of BinaryDistribution
     * @param n0 The first possible outcome
     * @param n1 The second possible outcome
     * @param p0 The probability to return the first possible outcome
     */
    public ScaleDependentSequenceDistribution(int[] seq) {
        sequence=seq;
    }
    
    /**
     * Returns a random value that follows a binary distribution
     * @return A random value
     */
    public int sample(int scale) {
        return sequence[scale];
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        int[] sequence={1,2,3,2,1,2,3,2,1,2,3,2,1,2,3};
        ScaleDependentSequenceDistribution myUD=new ScaleDependentSequenceDistribution(sequence);
        for(int i=0;i<10;i++)
            System.out.println(myUD.sample(1));
    }
    
}
