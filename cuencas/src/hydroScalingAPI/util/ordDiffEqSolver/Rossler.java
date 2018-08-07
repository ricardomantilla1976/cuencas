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
 * Rossler.java
 *
 * Created on May 25, 2001, 11:45 AM
 */

package hydroScalingAPI.util.ordDiffEqSolver;

/**
 * A sample implementation of the {@link hydroScalingAPI.util.ordDiffEqSolver.BasicFunction}
 * with the equations for the Rossler attractor (See Chaos literature).  Also see examples
 * {@link hydroScalingAPI.examples.ordDiffEqns}
 * @author Ricardo Mantilla
 */
public class Rossler implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {
    float[] parameters;
    /**
     * Creates new Rossler
     * @param a The parameter a
     * @param b The parameter b 
     * @param c The parameter c
     */
    public Rossler(float a,float b,float c) {
        parameters=new float[3];
        parameters[0]=a;
        parameters[1]=b;
        parameters[2]=c;
        
    }

    /**
     * The float version of the Lorenz equations
     * @param input The values where the function is to be evaluated
     * @return The value of the function
     */
    public float[] eval(float[] input) {
        float[] result=new float[input.length];
        result[0]=-(input[1]+input[2]);
        result[1]=input[0]+parameters[0]*input[1];
        result[2]=parameters[1]+input[2]*(input[0]-parameters[2]);
        return result;
    }
    
    /**
     * The float version of the Lorenz equations since eqns are time independent
     * @param input The values where the function is to be evaluated
     * @param time The time at which the function is to be evaluated
     * @return The value of the function
     */
    public float[] eval(float[] input, float time) {
        return eval(input);
    }
    
    /**
     * The double version of the Lorenz equations
     * @param input The values where the function is to be evaluated
     * @return The value of the function
     */
    public double[] eval(double[] input) {
        double[] result=new double[input.length];
        result[0]=-(input[1]+input[2]);
        result[1]=input[0]+parameters[0]*input[1];
        result[2]=parameters[1]+input[2]*(input[0]-parameters[2]);
        return result;
    }
    
    /**
     * The double version of the Lorenz equations since eqns are time independent
     * @param input The values where the function is to be evaluated
     * @param time The time at which the function is to be evaluated
     * @return The value of the function
     */
    public double[] eval(double[] input, double time) {
        return eval(input);
    }
    
}
