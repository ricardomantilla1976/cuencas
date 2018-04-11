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
 * LogGaussianDistribution.java
 *
 * Created on July 11, 2005, 10:00 AM
 */

package hydroScalingAPI.util.probability;

/**
 * An implementation of {@link hydroScalingAPI.util.probability.ContinuousDistribution}
 * using a log-gaussian distribution
 * @author Ricardo Mantilla
 */
public class LogGaussianDistribution implements hydroScalingAPI.util.probability.ContinuousDistribution{
    
    private float mean, stdev;
    private java.util.Random rn;
    
    /**
     * Creates a new instance of LogGaussianDistribution
     * @param m The mean
     * @param s The standard deviation
     */
    public LogGaussianDistribution(float m,float s) {
        s=(float)Math.pow(s,2);
        stdev=(float)Math.log(1+s/Math.pow(m,2));
        mean=(float)(Math.log(m)-stdev/2.0);
        stdev=(float)Math.sqrt(stdev);
        rn=new java.util.Random();
        
    }
    
    /**
     * Returns a random value that follows a log-gaussian distribution
     * @return A random value
     */
    public float sample() {
        return (float) Math.exp(rn.nextGaussian()*stdev+mean);
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        LogGaussianDistribution myUD=new LogGaussianDistribution(0.1f,0.2f);
        float numSamples=10000;
        float total=0;
        float total2=0;
        for(int i=0;i<numSamples;i++){
            float val=myUD.sample();
            total+=val;
            total2+=Math.pow(val,2);
        }
        System.out.println(total/numSamples);
        System.out.println(Math.sqrt(total2/numSamples-Math.pow(total/numSamples,2)));
    }
    
}
