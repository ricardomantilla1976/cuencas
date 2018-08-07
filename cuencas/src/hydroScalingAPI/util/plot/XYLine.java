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

import java.awt.Color;
import java.awt.Graphics;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

class XYLine
{

    
    XYLine(double[][] datos, String s, Color color1, Color color2, int i, boolean flag,double falt)
    {
        lineColor = Color.black;
        symbolColor = Color.black;
        size = 3;
        sizeWidth = 6;
        firstColumnLabelsFlag = false;
        label = s;
        lineColor = color1;
        symbolColor = color2;
        type = i;
        firstColumnLabelsFlag = flag;
        faltante = falt;
        points = new Vector(2500, 2500);
        readTagData(datos);
    }


    private void readTagData(double[][] datos)
    {
        String s2 = new String("");
        for(int i = 0;i<datos[0].length; i++)
            if(datos[1][i] != faltante)
                addPoint(datos[0][i], datos[1][i], s2);
        if(points.size() > 0)
            minmax();
    }

    public void draw(Graphics g, XYAxis xyaxis)
    {
        Graphics g1 = g.create();
        int i = 0;
        int j = 0;
        int k1 = xyaxis.plotXstart();
        int l1 = xyaxis.plotXend();
        int i2 = xyaxis.plotYstart();
        int j2 = xyaxis.plotYend();
        int k2 = xyaxis.plotWidth();
        int l2 = xyaxis.plotHeight();
        axis = xyaxis;
        double d4 = xyaxis.xmin();
        double d5 = xyaxis.xmax();
        double d6 = xyaxis.ymin();
        double d7 = xyaxis.ymax();
        double d8 = d5 - d4;
        double d9 = d7 - d6;
        boolean flag = true;
        g1.clipRect(k1 + 1, j2 + 1, k2 - 2, l2 - 2);
        double d10 = (double)k1 + (double)k2 * ((0.0D - d4) / d8);
        double d11 = (double)j2 + ((double)l2 - (double)l2 * ((0.0D - d6) / d9));
        int i3 = (int)d10;
        int j3 = (int)d11;
        if(d11 > 10000D)
            j3 = 10000;
        if(d10 > 10000D)
            i3 = 10000;
        if(d11 < -10000D)
            j3 = -10000;
        if(d10 < -10000D)
            i3 = -10000;
        g1.setColor(lineColor);
        for(Enumeration enumeration = points.elements(); enumeration.hasMoreElements();)
        {   
            int k = j;
            int i1 = i;
            XYPoint xypoint = (XYPoint)enumeration.nextElement();
            double d = (double)k1 + (double)k2 * ((xypoint.x() - d4) / d8);
            double d2 = (double)j2 + ((double)l2 - (double)l2 * ((xypoint.y() - d6) / d9));
            j = (int)d;
            i = (int)d2;
            if(d2 > 10000D)
                i = 10000;
            if(d > 10000D)
                j = 10000;
            if(d2 < -10000D)
                i = -10000;
            if(d < -10000D)
                j = -10000;
            if(flag)
            {
                flag = false;
                k = j;
                i1 = i;
            }
            if((j <= l1 || k <= l1) && (j >= k1 || k >= k1) && (i <= i2 || i1 <= i2) && (i >= j2 || i1 >= j2) && (type == 0 || type == 1 || type == 5 || type == 7 || type == 9 || type == 51 || type == 55))
                g1.drawLine(k, i1, j, i);
        }

        g1.setColor(symbolColor);
        for(Enumeration enumeration1 = points.elements(); enumeration1.hasMoreElements();)
        {
            int l = j;
            int j1 = i;
            XYPoint xypoint1 = (XYPoint)enumeration1.nextElement();
            double d1 = (double)k1 + (double)k2 * ((xypoint1.x() - d4) / d8);
            double d3 = (double)j2 + ((double)l2 - (double)l2 * ((xypoint1.y() - d6) / d9));
            j = (int)d1;
            i = (int)d3;
            if(d3 > 10000D)
                i = 10000;
            if(d1 > 10000D)
                j = 10000;
            if(d3 < -10000D)
                i = -10000;
            if(d1 < -10000D)
                j = -10000;
            if(flag)
            {
                flag = false;
                l = j;
                j1 = i;
            }
            if((j <= l1 || l <= l1) && (j >= k1 || l >= k1) && (i <= i2 || j1 <= i2) && (i >= j2 || j1 >= j2))
            {
                if(type == 1)
                    g1.fillOval(j - size, i - size, sizeWidth, sizeWidth);
                if(type == 2)
                    g1.fillOval(j - size, i - size, sizeWidth, sizeWidth);
                if(type == 5)
                    g1.fillRect(j - size, i - size, sizeWidth, sizeWidth);
                if(type == 6)
                    g1.fillRect(j - size, i - size, sizeWidth, sizeWidth);
                if(type == 7)
                {
                    g1.drawLine(j - size, i, j + size, i);
                    g1.drawLine(j, i - size, j, i + size);
                }
                if(type == 8)
                {
                    g1.drawLine(j - size, i, j + size, i);
                    g1.drawLine(j, i - size, j, i + size);
                }
                if(type == 9)
                {
                    g1.drawLine(j - size, i - size, j + size, i + size);
                    g1.drawLine(j - size, i + size, j + size, i - size);
                }
                if(type == 10)
                {
                    g1.drawLine(j - size, i - size, j + size, i + size);
                    g1.drawLine(j - size, i + size, j + size, i - size);
                }
                if(type == 11)
                    g1.drawLine(j, i, j, i);
                if(type == 51)
                    g1.drawOval(j - size, i - size, sizeWidth, sizeWidth);
                if(type == 52)
                    g1.drawOval(j - size, i - size, sizeWidth, sizeWidth);
                if(type == 55)
                    g1.drawRect(j - size, i - size, sizeWidth, sizeWidth);
                if(type == 56)
                    g1.drawRect(j - size, i - size, sizeWidth, sizeWidth);
            }
            if((j <= l1 || l <= l1) && (j >= k1 || l >= k1))
            {
                if(type == 3)
                    g1.fillRect(j - size, i, sizeWidth, i2 - (i - size));
                if(type == 4)
                    if(i - j3 < 0)
                        g1.fillRect(j - size, i, sizeWidth, j3 - i);
                    else
                        g1.fillRect(j - size, j3, sizeWidth, i - j3);
            }
        }

    }

    public void minmax()
    {
        XYPoint xypoint = (XYPoint)points.elementAt(0);
        xmaximum = xypoint.x();
        xminimum = xypoint.x();
        ymaximum = xypoint.y();
        yminimum = xypoint.y();
        if(points.size() < 2)
            return;
        for(Enumeration enumeration = points.elements(); enumeration.hasMoreElements();)
        {
            XYPoint xypoint1 = (XYPoint)enumeration.nextElement();
            if(xminimum > xypoint1.x())
                xminimum = xypoint1.x();
            if(yminimum > xypoint1.y())
                yminimum = xypoint1.y();
            if(xmaximum < xypoint1.x())
                xmaximum = xypoint1.x();
            if(ymaximum < xypoint1.y())
                ymaximum = xypoint1.y();
        }

        xminimumAxis = xminimum - (xmaximum - xminimum) * 0.10000000000000001D;
        xmaximumAxis = xmaximum + (xmaximum - xminimum) * 0.10000000000000001D;
        yminimumAxis = yminimum - (ymaximum - yminimum) * 0.10000000000000001D;
        ymaximumAxis = ymaximum + (ymaximum - yminimum) * 0.10000000000000001D;
        if(xminimum == xmaximum)
        {
            xminimumAxis = xminimum - 1.0D;
            xmaximumAxis = xmaximum + 1.0D;
        }
        if(yminimum == ymaximum)
        {
            yminimumAxis = yminimum - 1.0D;
            ymaximumAxis = ymaximum + 1.0D;
        }
    }

    public void drawIndex(Graphics g, int i, int j, Color color1)
    {
        g.setColor(lineColor);
        g.fill3DRect(i, j, 8, 8, true);
        g.setColor(color1);
        g.drawString(label, i + 12, j + 8);
    }

    public void eraseIndex(Graphics g, int i, int j)
    {
        g.setColor(axis.backgroundColor());
        g.fillRect(i, j, 8, 8);
        g.drawString(label, i + 12, j + 8);
    }

    public void addPoint(double d, double d1, String s)
    {
        points.addElement(new XYPoint(d, d1, s));
    }

    public int pointCount()
    {
        return points.size();
    }

    public String pointLabel(int i)
    {
        XYPoint xypoint = (XYPoint)points.elementAt(i);
        return xypoint.labelString();
    }

    public String label()
    {
        return label;
    }

    public Color color()
    {
        return lineColor;
    }

    public double xmin()
    {
        return xminimum;
    }

    public double xmax()
    {
        return xmaximum;
    }

    public double ymin()
    {
        return yminimum;
    }

    public double ymax()
    {
        return ymaximum;
    }

    public double xminAxis()
    {
        return xminimumAxis;
    }

    public double xmaxAxis()
    {
        return xmaximumAxis;
    }

    public double yminAxis()
    {
        return yminimumAxis;
    }

    public double ymaxAxis()
    {
        return ymaximumAxis;
    }

    public void symbolSize(int i)
    {
        sizeWidth = i;
        if(sizeWidth < 1)
            sizeWidth = 1;
        size = sizeWidth / 2;
    }

    public long getLastModified()
    {
        return lastModified;
    }

    protected String label;
    protected Color lineColor;
    protected Color symbolColor;
    protected double xmaximum;
    protected double xminimum;
    protected double ymaximum;
    protected double yminimum;
    protected double faltante;
    protected double xmaximumAxis;
    protected double xminimumAxis;
    protected double ymaximumAxis;
    protected double yminimumAxis;
    protected Vector points;
    protected int type;
    protected XYAxis axis;
    protected int size;
    protected int sizeWidth;
    protected boolean firstColumnLabelsFlag;
    long lastModified;
    URL dataSourceURL;
}
