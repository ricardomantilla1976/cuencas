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

class LabelAndTics
{

    LabelAndTics(double d, double d1)
    {
        minimum = d;
        maximum = d1;
        calculate();
    }

    public void calculate()
    {
        range = maximum - minimum;
        double d = 10D;
        int i = 0;
        if(range < 0.0D)
            range = range * -1D;
        if(range == 0.0D)
            label_value = 1.0D;
        else
        if(range >= 1.0D)
        {
            for(; range > 1.0D; range = range / 10D)
                i++;

            if(range > 0.5D)
                label_value = Math.pow(d, i - 1);
            else
            if(range > 0.14999999999999999D)
                label_value = 5D * Math.pow(d, i - 2);
            else
            if(range > 0.11D)
                label_value = 2D * Math.pow(d, i - 2);
            else
                label_value = Math.pow(d, i - 2);
        } else
        {
            for(; range < 1.0D; range = range * 10D)
                i++;

            if(range > 5D)
                label_value = Math.pow(d, -i);
            else
            if(range > 1.5D)
                label_value = 5D * Math.pow(d, -i - 1);
            else
            if(range > 1.1000000000000001D)
                label_value = 2D * Math.pow(d, -i - 1);
            else
                label_value = Math.pow(d, -i - 1);
        }
        tic_value = label_value / 5D;
    }

    public double max()
    {
        return maximum;
    }

    public void max(double d)
    {
        maximum = d;
        calculate();
    }

    public double min()
    {
        return minimum;
    }

    public void min(double d)
    {
        minimum = d;
        calculate();
    }

    public double label()
    {
        return label_value;
    }

    public double tic()
    {
        return tic_value;
    }

    private double minimum;
    private double maximum;
    private double label_value;
    private double tic_value;
    private double range;
}
