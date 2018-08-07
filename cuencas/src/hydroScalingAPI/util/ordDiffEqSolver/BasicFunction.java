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
 * basicFunction.java
 *
 * Created on May 25, 2001, 11:29 AM
 */

package hydroScalingAPI.util.ordDiffEqSolver;

/**
 * An abstract interface that describes a Function (In the mathematical sense)
 * @author Ricardo Mantilla
 */
public interface BasicFunction{
    
    /**
     * The result from evaluating a float multivariate function
     * @param input The values where the function is to be evaluated
     * @return The value of the function
     */
    public float[] eval(float[] input);
    
    /**
     * The result from evaluating a float multivariate function at time t
     * @param input The values where the function is to be evaluated
     * @param time The time at which the function is to be evaluated
     * @return The value of the function
     */
    public float[] eval(float[] input, float time);
    
    /**
     * The result from evaluating a double multivariate function
     * @param input The values where the function is to be evaluated
     * @return The value of the function
     */
    public double[] eval(double[] input);
    
    /**
     * The result from evaluating a double multivariate function at time t
     * @param input The values where the function is to be evaluated
     * @param time The time at which the function is to be evaluated
     * @return The value of the function
     */
    public double[] eval(double[] input, double time);

}

