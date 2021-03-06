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
 * DiscreteDistribution.java
 *
 * Created on July 11, 2005, 9:57 AM
 */

package hydroScalingAPI.util.probability;

/**
 * A basic description of a discrete distribution
 * @author Ricardo Mantilla
 */
public interface ScaleDependentDiscreteDistribution {
    
    /**
     * Returns a random value with a given distribution
     * @param parameter indicating the scale
     * @return A random value
     */
    public int sample(int param);
    
}
