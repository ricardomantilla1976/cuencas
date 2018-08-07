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

import java.awt.*;
import java.util.Enumeration;
import java.util.Vector;

class XYLegend
{

    XYLegend(int i, int j, int k)
    {
        textColor = Color.black;
        legendFontSize = 14;
        legendFontStyle = 0;
        legendFontFamily = "Dialog";
        legendFont = new Font(legendFontFamily, legendFontStyle, legendFontSize);
        show = false;
        legendX = i;
        legendY = j;
        legendIncrement = k;
    }

    public void draw(Graphics g, Vector vector)
    {
        g.setPaintMode();
        drawLegend(g, vector, false);
    }

    public void erase(Graphics g, Vector vector)
    {
        g.setPaintMode();
        drawLegend(g, vector, true);
    }

    private void drawLegend(Graphics g, Vector vector, boolean flag)
    {
        if(show)
        {
            int i = 0;
            g.setFont(legendFont);
            for(Enumeration enumeration = vector.elements(); enumeration.hasMoreElements();)
            {
                XYLine xyline = (XYLine)enumeration.nextElement();
                if(flag)
                    xyline.eraseIndex(g, legendX, legendY + i * legendIncrement);
                else
                    xyline.drawIndex(g, legendX, legendY + i * legendIncrement, textColor);
                legendH = (i + 1) * legendIncrement;
                legendW = Math.max(legendW, g.getFontMetrics().stringWidth(xyline.label()) + 12 + 6);
                i++;
            }

        }
    }

    public void drawOutline(Graphics g)
    {
        if(show)
        {
            g.setPaintMode();
            g.drawRect(legendX - 3, legendY - 3, legendW, legendH);
        }
    }

    public void eraseOutline(Graphics g)
    {
        g.setXORMode(Color.white);
        g.drawRect(legendX - 3, legendY - 3, legendW, legendH);
        g.setPaintMode();
    }

    public void x(int i)
    {
        legendX = i;
    }

    public void y(int i)
    {
        legendY = i;
    }

    public void increment(int i)
    {
        legendIncrement = i;
    }

    public int x()
    {
        return legendX;
    }

    public int y()
    {
        return legendY;
    }

    public int w()
    {
        return legendW;
    }

    public int h()
    {
        return legendH;
    }

    public int increment()
    {
        return legendIncrement;
    }

    public void show(boolean flag)
    {
        show = flag;
    }

    public boolean show()
    {
        return show;
    }

    public void textColor(Color color)
    {
        textColor = color;
    }

    public int fontSize()
    {
        return legendFontSize;
    }

    public void fontSize(int i)
    {
        legendFontSize = i;
        legendFont = new Font(legendFontFamily, legendFontStyle, legendFontSize);
    }

    public int fontStyle()
    {
        return legendFontStyle;
    }

    public void fontStyle(int i)
    {
        legendFontStyle = i;
        legendFont = new Font(legendFontFamily, legendFontStyle, legendFontSize);
    }

    public Font font()
    {
        return legendFont;
    }

    private Color textColor;
    private int legendX;
    private int legendY;
    private int legendIncrement;
    private int legendW;
    private int legendH;
    private int legendFontSize;
    private int legendFontStyle;
    private String legendFontFamily;
    private Font legendFont;
    private boolean show;
}
