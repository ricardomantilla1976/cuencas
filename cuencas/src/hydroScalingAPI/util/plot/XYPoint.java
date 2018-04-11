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
 * plot1.java
 *
 * Created on 23 de julio de 2001, 03:48 PM
 */

package hydroScalingAPI.util.plot;

/**
 *
 * @author Olver Hernandez
 * @version
 */

class XYPoint
{

    XYPoint(double d, double d1, String s)
    {
        xValue = d;
        yValue = d1;
        labelString = s;
    }

    public double x()
    {
        return xValue;
    }

    public double y()
    {
        return yValue;
    }

    public String labelString()
    {
        return labelString;
    }

    protected double xValue;
    protected double yValue;
    protected String labelString;
}
