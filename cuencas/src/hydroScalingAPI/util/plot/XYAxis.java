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
import java.awt.image.*;
import java.io.PrintStream;
import java.util.Vector;

class XYAxis {
    
    XYAxis(int i, int j, int k, int l, Component component, int t, String [] lab) {
        labels = lab;
        tipo = t;
        deBug = false;
        leftMargin = 75;
        rightMargin = 50;
        topMargin = 50;
        bottomMargin = 60;
        axisColor = Color.black;
        labelColor = Color.black;
        backgroundColor = Color.white;
        gridColor = new Color(0.8F, 0.8F, 0.8F);
        ticLength = 6;
        labelLength = 10;
        gridX = false;
        gridY = false;
        openAxis = true;
        xmax = 10D;
        ymax = 10D;
        xLabel = 2D;
        yLabel = 2D;
        xTic = 1.0D;
        yTic = 1.0D;
        isVisible = false;
        hasGraphicsBeenSet = false;
        title = "";
        xtitle = "X-Axis";
        ytitle = "Y-Axis";
        titleFontSize = 18;
        titleFontStyle = 1;
        titleFontFamily = "Dialog";
        titleFont = new Font(titleFontFamily, titleFontStyle, titleFontSize);
        axisFontSize = 14;
        axisFontStyle = 1;
        axisFontFamily = "Dialog";
        axisFont = new Font(axisFontFamily, axisFontStyle, axisFontSize);
        labelYFontSize = 14;
        labelYFontStyle = Font.PLAIN;
        labelYFontFamily = "Dialog";
        labelYFont = new Font(labelYFontFamily, labelYFontStyle, labelYFontSize);
        labelXFontSize = 14;
        labelXFontStyle = Font.PLAIN;
        labelXFontFamily = "Dialog";
        labelXFont = new Font(labelXFontFamily, labelXFontStyle, labelXFontSize);
        firstColumnLabelsFlag = false;
        oldYString = "";
        xLabelsVerticalFlag = false;
        x = i;
        y = j;
        w = k;
        h = l;
        pxs = x + leftMargin;
        pxe = w - rightMargin;
        pys = (y + h) - bottomMargin;
        pye = y + topMargin;
        pxw = pxe - pxs;
        pyw = pys - pye;
        currentComponent = component;
        vertLabels = new Vector();
        vertImages = new Vector();
    }
    
    public void setDimension(int Width,int Height){
        w= Width;
        h= Height;
        pxe = w - rightMargin;
        pys = (y + h) - bottomMargin;
        pxw = pxe - pxs;
        pyw = pys - pye;
    }
    
    private void calcXLabels() {
        xlabtic = new LabelAndTics(xmin, xmax);
        xLabel = xlabtic.label();
        xTic = xlabtic.tic();
    }
    
    private void calcYLabels() {
        ylabtic = new LabelAndTics(ymin, ymax);
        yLabel = ylabtic.label();
        yTic = ylabtic.tic();
    }
    public void setTipo(int t){
        tipo = t;
    }
    private int[] rot90Pixels(int ai[], int i, int j, Color color, Color color1) {
        ColorModel colormodel = ColorModel.getRGBdefault();
        int ai1[] = null;
        if(i * j == ai.length) {
            ai1 = new int[i * j];
            int k = 0;
            int l = 0;
            int i1 = (i - 1) * j;
            for(; k < i * j; k++) {
                int j1 = ai[k];
                if(colormodel.getBlue(j1) == 0 && colormodel.getGreen(j1) == 0 && colormodel.getRed(j1) == 0) {
                    j1 = color.getRGB();
                } else {
                    float f = (float)colormodel.getBlue(j1) / 256F;
                    int k1 = (int)((float)color1.getBlue() * f) + (int)((double)(float)color.getBlue() * (1.0D - (double)f));
                    int l1 = (int)((float)color1.getGreen() * f) + (int)((double)(float)color.getGreen() * (1.0D - (double)f));
                    int i2 = (int)((float)color1.getRed() * f) + (int)((double)(float)color.getRed() * (1.0D - (double)f));
                    j1 = k1 | l1 << 8 | i2 << 16 | 0xff000000;
                }
                ai1[i1] = j1;
                i1 -= j;
                if(i1 < 0) {
                    l++;
                    i1 = (i - 1) * j + l;
                }
            }
            
        }
        return ai1;
    }
    
    private void drawVerticalString(int i, int j, int k, String s, String s1, Color color, Color color1,
            Font font) {
        int l = appFM.stringWidth(s);
        int i1 = appFM.getHeight();
        int j1 = appFM.getMaxDescent();
        if(i < vertImages.size())
            vertImage = (Image)vertImages.elementAt(i);
        else
            vertImage = null;
        if(s1.compareTo(s) != 0 && deBug)
            System.out.println("STRINGMATCH:   newString=\"" + s + "\"  Len=" + s.length() + "  previousString=\"" + s1 + "\"  Len=" + s1.length() + "  " + s1.compareTo(s));
        if(vertImage == null && deBug)
            System.out.println("NULL:          Index=" + i + "  VertImage=" + vertImage);
        if(s1.compareTo(s) != 0 || vertImage == null) {
            if(i1 != 0 && l != 0) {
                Image image = currentComponent.createImage(l, i1);
                Graphics g1 = image.getGraphics();
                if(deBug)
                    System.out.println("BackgroundColor=" + color);
                g1.setColor(Color.black);
                g1.fillRect(0, 0, l, i1);
                g1.setColor(Color.white);
                g1.setFont(font);
                g1.drawString(s, 0, i1 - j1);
                vertImage = null;
                if(deBug)
                    System.out.println("Creating new Image, vertImageSize=" + vertImages.size());
                int ai[] = new int[l * i1];
                PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, l, i1, ai, 0, l);
                try {
                    pixelgrabber.grabPixels();
                    vertImage = currentComponent.createImage(new MemoryImageSource(i1, l, rot90Pixels(ai, l, i1, color, color1), 0, i1));
                } catch(InterruptedException interruptedexception) {
                    interruptedexception.printStackTrace();
                }
                if(vertImage != null) {
                    g.drawImage(vertImage, j - i1 / 2, k, currentComponent);
                    if(i < vertImages.size()) {
                        vertImages.setElementAt(vertImage, i);
                        return;
                    } else {
                        vertImages.addElement(vertImage);
                        return;
                    }
                }
            }
        } else {
            g.drawImage(vertImage, j - i1 / 2, k, currentComponent);
        }
    }
    
    public void draw() {
/*        g.setColor(Color.blue);
        g.drawString("mal",200,50);*/
        double d = xmax - xmin;
        double d1 = ymax - ymin;
        calcXLabels();
        calcYLabels();
        isVisible = true;
        g.setColor(backgroundColor);
        g.fillRect(x, y, w, h);
        g.setColor(axisColor);
        
        g.drawRect(pxs,pye,pxw,pyw);
        g.drawRect(pxs+1,pye+1,pxw-2,pyw-2);
        
        g.setFont(titleFont);
        appFM = g.getFontMetrics();
        g.drawString(title, (pxs + pxw / 2) - appFM.stringWidth(title) / 2, 3 + appFM.getAscent());
        g.setFont(axisFont);
        appFM = g.getFontMetrics();
        g.drawString(xtitle, (pxs + pxw / 2) - appFM.stringWidth(xtitle) / 2, h - appFM.getDescent() - 5);
        int i3 = appFM.getHeight() + 5;
        if(oldYString != ytitle) {
            textWidth = appFM.stringWidth(ytitle);
            int j3 = appFM.getHeight();
            int l3 = appFM.getMaxDescent();
            if(j3 != 0 && textWidth != 0) {
                Image image = currentComponent.createImage(textWidth, j3);
                Graphics g1 = image.getGraphics();
                g1.setColor(Color.black);
                g1.fillRect(0, 0, textWidth, j3);
                g1.setColor(Color.white);
                g1.setFont(axisFont);
                g1.drawString(ytitle, 0, j3 - l3);
                rotImage = null;
                int ai[] = new int[textWidth * j3];
                PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, textWidth, j3, ai, 0, textWidth);
                try {
                    pixelgrabber.grabPixels();
                    rotImage = currentComponent.createImage(new MemoryImageSource(j3, textWidth, rot90Pixels(ai, textWidth, j3, backgroundColor, axisColor), 0, j3));
                } catch(InterruptedException interruptedexception) {
                    interruptedexception.printStackTrace();
                }
                if(rotImage != null)
                    g.drawImage(rotImage, 3, (pye + pyw / 2) - textWidth / 2, currentComponent);
                oldYString = ytitle;
            }
        } else
            if(rotImage != null)
                g.drawImage(rotImage, 3, (pye + pyw / 2) - textWidth / 2, currentComponent);
        appFM = g.getFontMetrics();
        if(pxw < 400 && d / xLabel + 1.0D > 6D) {
            xLabel *= 2D;
            xTic *= 2D;
        }
        if(pyw < 250 && d1 / yLabel + 1.0D > 6D) {
            yLabel *= 2D;
            yTic *= 2D;
        }
        if(d1 / yLabel + 1.0D < 4D) {
            yLabel /= 2D;
            yTic /= 2D;
        }
        if(d / xLabel + 1.0D < 4D) {
            xLabel /= 2D;
            xTic /= 2D;
        }
        XYLine xyline;
        try{xyline = (XYLine)lines.elementAt(0);}catch(Exception ex){return;}
        int l2 = xyline.pointCount();
        if(firstColumnLabelsFlag) {
            if(xLabel < 1.0D)
                xLabel = 1.0D;
            if(xTic < 1.0D)
                xTic = 1.0D;
        }
        int j1 = (int)(d / xLabel + 1.0D);
        int k1 = (int)(xmin / xLabel);
        int k3 = 1;
        for(int i = k1; i <= j1 + k1; i++) {
            int l1 = (int)((double)pxs + (double)pxw * (((double)i * xLabel - xmin) / d));
            if(deBug)
                System.out.println("x istep=" + l1);
            if(l1 >= pxs && l1 <= pxe && (firstColumnLabelsFlag && (double)i * xLabel >= 0.0D && (double)i * xLabel < (double)l2 || !firstColumnLabelsFlag)) {
                g.setColor(labelColor);
                g.setFont(labelXFont);
                appFM = g.getFontMetrics();
                Float float1 = new Float(xLabel * (double)(float)i);
                String s;
                if(!firstColumnLabelsFlag) {
                    if(tipo == 0){
                        s = new String(float1.toString());
                        if(s.endsWith(".0"))
                            s = s.substring(0, s.length() - 2);
                    } else{ //if(tipo == 1)
                        float tmp =float1.floatValue() - (float)(float1.intValue());
                        if(tmp == 0){
                            try{
                                s = labels[float1.intValue()];
                            }catch(ArrayIndexOutOfBoundsException ex){
                                s = "";
                            }
                        } else
                            s="";
                    }
                } else {
                    s = xyline.pointLabel((int)xLabel * i);
                    
                }
                if(xLabelsVerticalFlag) {
                    if(deBug)
                        System.out.println("labelCount=" + k3);
                    if(deBug)
                        System.out.println("vertLabels size=" + vertLabels.size());
                    if(vertLabels.size() < k3)
                        vertLabels.addElement("empty");
                    drawVerticalString(k3 - 1, l1, pys + 16, s, (String)vertLabels.elementAt(k3 - 1), backgroundColor, labelColor, labelXFont);
                    vertLabels.setElementAt(s, k3 - 1);
                    if(appFM.stringWidth(s) > bottomMargin - i3 - 16) {
                        bottomMargin = appFM.stringWidth(s) + i3 + 16;
                        pxs = x + leftMargin;
                        pxe = w - rightMargin;
                        pys = (y + h) - bottomMargin;
                        pye = y + topMargin;
                        pxw = pxe - pxs;
                        pyw = pys - pye;
                    }
                } else {
                    g.drawString(s, l1 - appFM.stringWidth(s) / 2, pys + appFM.getAscent() + 16);
                }
                k3++;
                g.setColor(java.awt.Color.gray);
                g.drawLine(l1,pys,l1,pye);
                g.setColor(axisColor);
            }
        }
        
        if(deBug) {
            for(int j = 0; j < vertLabels.size(); j++)
                System.out.println("vertLabels" + j + "=" + (String)vertLabels.elementAt(j));
            
        }
        j1 = (int)(d1 / yLabel + 1.0D);
        k1 = (int)(ymin / yLabel);
        boolean flag = false;
        for(int k = k1; k <= j1 + k1; k++) {
            int i2 = (int)((double)pys - (double)pyw * (((double)k * yLabel - ymin) / d1));
            if(deBug)
                System.out.println("y istep=" + i2);
            if(i2 >= pye && i2 <= pys) {
                g.setColor(labelColor);
                g.setFont(labelYFont);
                appFM = g.getFontMetrics();
                Float float2 = new Float(yLabel * (double)(float)k);
                String s1 = new String(float2.toString());
                if(s1.endsWith(".0"))
                    s1 = s1.substring(0, s1.length() - 2);
                g.drawString(s1, pxs - 14 - appFM.stringWidth(s1), i2 + appFM.getAscent() / 3);
                
                g.setColor(java.awt.Color.gray);
                g.drawLine(pxs,i2,pxe,i2);
                g.setColor(axisColor);
            }
        }
        
        j1 = (int)(d / xTic + 1.0D);
        k1 = (int)(xmin / xTic);
        flag = false;
        for(int l = k1; l <= j1 + k1; l++) {
            int j2 = (int)((double)pxs + (double)pxw * (((double)l * xTic - xmin) / d));
            if(j2 >= pxs && j2 <= pxe) {
                g.setColor(axisColor);
                //g.drawLine(j2, pys, j2, pys + ticLength);
                g.drawLine(j2, pys, j2, pys - ticLength);
                g.drawLine(j2, pye, j2, pye + ticLength);
            }
        }
        
        j1 = (int)(d1 / yTic + 1.0D);
        k1 = (int)(ymin / yTic);
        flag = false;
        for(int i1 = k1; i1 <= j1 + k1; i1++) {
            int k2 = (int)((double)pys - (double)pyw * (((double)i1 * yTic - ymin) / d1));
            if(k2 >= pye && k2 <= pys) {
                g.setColor(axisColor);
                g.drawLine(pxs, k2, pxs + ticLength, k2);
                g.drawLine(pxe, k2, pxe - ticLength, k2);
            }
        }
        
    }
    
    public void Update() {
        if(isVisible)
            draw();
    }
    
    public int x() {
        return x;
    }
    
    public int y() {
        return y;
    }
    
    public int width() {
        return w;
    }
    
    public int height() {
        return h;
    }
    
    public int rightMargin() {
        return rightMargin;
    }
    
    public int leftMargin() {
        return leftMargin;
    }
    
    public int topMargin() {
        return topMargin;
    }
    
    public int bottomMargin() {
        return bottomMargin;
    }
    
    public int plotWidth() {
        return pxw;
    }
    
    public int plotHeight() {
        return pyw;
    }
    
    public int plotXstart() {
        return pxs;
    }
    
    public int plotXend() {
        return pxe;
    }
    
    public int plotYstart() {
        return pys;
    }
    
    public int plotYend() {
        return pye;
    }
    
    public Color axisColor() {
        return axisColor;
    }
    
    public void axisColor(Color color) {
        axisColor = color;
    }
    
    public Color labelColor() {
        return labelColor;
    }
    
    public void labelColor(Color color) {
        labelColor = color;
    }
    
    public Color gridColor() {
        return gridColor;
    }
    
    public void gridColor(Color color) {
        gridColor = color;
    }
    
    public void backgroundColor(Color color) {
        backgroundColor = color;
    }
    
    public Color backgroundColor() {
        return backgroundColor;
    }
    
    public String labelXFontFamily() {
        return labelXFontFamily;
    }
    
    public void labelXFontFamily(String s) {
        labelXFontFamily = s;
        labelXFont = new Font(labelXFontFamily, labelXFontStyle, labelXFontSize);
    }
    
    public int labelXFontSize() {
        return labelXFontSize;
    }
    
    public void labelXFontSize(int i) {
        labelXFontSize = i;
        labelXFont = new Font(labelXFontFamily, labelXFontStyle, labelXFontSize);
    }
    
    public int labelXFontStyle() {
        return labelXFontStyle;
    }
    
    public void labelXFontStyle(int i) {
        labelXFontStyle = i;
        labelXFont = new Font(labelXFontFamily, labelXFontStyle, labelXFontSize);
    }
    
    public String labelYFontFamily() {
        return labelYFontFamily;
    }
    
    public void labelYFontFamily(String s) {
        labelYFontFamily = s;
        labelYFont = new Font(labelYFontFamily, labelYFontStyle, labelYFontSize);
    }
    
    public int labelYFontSize() {
        return labelYFontSize;
    }
    
    public void labelYFontSize(int i) {
        labelYFontSize = i;
        labelYFont = new Font(labelYFontFamily, labelYFontStyle, labelYFontSize);
    }
    
    public int labelYFontStyle() {
        return labelYFontStyle;
    }
    
    public void labelYFontStyle(int i) {
        labelYFontStyle = i;
        labelYFont = new Font(labelYFontFamily, labelYFontStyle, labelYFontSize);
    }
    
    public Font labelYFont() {
        return labelYFont;
    }
    
    public String axisFontFamily() {
        return axisFontFamily;
    }
    
    public void axisFontFamily(String s) {
        axisFontFamily = s;
        axisFont = new Font(axisFontFamily, axisFontStyle, axisFontSize);
    }
    
    public int axisFontSize() {
        return axisFontSize;
    }
    
    public void axisFontSize(int i) {
        axisFontSize = i;
        axisFont = new Font(axisFontFamily, axisFontStyle, axisFontSize);
    }
    
    public int axisFontStyle() {
        return axisFontStyle;
    }
    
    public void axisFontStyle(int i) {
        axisFontStyle = i;
        axisFont = new Font(axisFontFamily, axisFontStyle, axisFontSize);
    }
    
    public String titleFontFamily() {
        return titleFontFamily;
    }
    
    public void titleFontFamily(String s) {
        titleFontFamily = s;
        titleFont = new Font(titleFontFamily, titleFontStyle, titleFontSize);
    }
    
    public int titleFontSize() {
        return titleFontSize;
    }
    
    public void titleFontSize(int i) {
        titleFontSize = i;
        titleFont = new Font(titleFontFamily, titleFontStyle, titleFontSize);
    }
    
    public int titleFontStyle() {
        return titleFontStyle;
    }
    
    public void titleFontStyle(int i) {
        titleFontStyle = i;
        titleFont = new Font(titleFontFamily, titleFontStyle, titleFontSize);
    }
    
    public int ticLength() {
        return ticLength;
    }
    
    public void ticLength(int i) {
        ticLength = i;
    }
    
    public int labelLength() {
        return labelLength;
    }
    
    public void labelLength(int i) {
        labelLength = i;
    }
    
    public boolean gridY() {
        return gridY;
    }
    
    public boolean gridX() {
        return gridX;
    }
    
    public void gridY(boolean flag) {
        gridY = flag;
    }
    
    public void gridX(boolean flag) {
        gridX = flag;
    }
    
    public boolean openAxis() {
        return openAxis;
    }
    
    public void openAxis(boolean flag) {
        openAxis = flag;
    }
    
    public double xmin() {
        return xmin;
    }
    
    public void xmin(double d) {
        xmin = d;
        calcXLabels();
    }
    
    public double ymin() {
        return ymin;
    }
    
    public void ymin(double d) {
        ymin = d;
        calcYLabels();
    }
    
    public double xmax() {
        return xmax;
    }
    
    public void xmax(double d) {
        xmax = d;
        calcXLabels();
    }
    
    public double ymax() {
        return ymax;
    }
    
    public void ymax(double d) {
        ymax = d;
        calcYLabels();
    }
    
    public boolean isVisible() {
        return isVisible;
    }
    
    public boolean hasGraphicsBeenSet() {
        return hasGraphicsBeenSet;
    }
    
    public Graphics graphics() {
        return g;
    }
    
    public void graphics(Graphics g1) {
        g = g1;
        hasGraphicsBeenSet = true;
    }
    
    public void title(String s) {
        title = s;
    }
    
    public void xTitle(String s) {
        xtitle = s;
    }
    
    public void yTitle(String s) {
        ytitle = s;
    }
    
    public void firstColumnLabels(boolean flag) {
        firstColumnLabelsFlag = flag;
    }
    
    public void xLabelsVerticalFlag(boolean flag) {
        xLabelsVerticalFlag = flag;
    }
    
    public void setData(Vector vector) {
        lines = vector;
    }
    
    private boolean deBug;
    private int x;
    private int y;
    private int w;
    private int h;
    private int pxs;
    private int pxe;
    private int leftMargin;
    private int rightMargin;
    private int pxw;
    private int pys;
    private int pye;
    private int topMargin;
    private int bottomMargin;
    private int pyw;
    private Color axisColor;
    private Color labelColor;
    private Color backgroundColor;
    private Color gridColor;
    private int ticLength;
    private int labelLength;
    private boolean gridX;
    private boolean gridY;
    private boolean openAxis;
    private double xmin;
    private double xmax;
    private double ymin;
    private double ymax;
    private double xLabel;
    private double yLabel;
    private double xTic;
    private double yTic;
    private boolean isVisible;
    private Graphics g;
    private boolean hasGraphicsBeenSet;
    private FontMetrics appFM;
    private String title;
    private String xtitle;
    private String ytitle;
    private int titleFontSize;
    private int titleFontStyle;
    private String titleFontFamily;
    private Font titleFont;
    private int axisFontSize;
    private int axisFontStyle;
    private String axisFontFamily;
    private Font axisFont;
    private int labelYFontSize;
    private int labelYFontStyle;
    private String labelYFontFamily;
    private Font labelYFont;
    private int labelXFontSize;
    private int labelXFontStyle;
    private String labelXFontFamily;
    private Font labelXFont;
    private LabelAndTics xlabtic;
    private LabelAndTics ylabtic;
    private boolean firstColumnLabelsFlag;
    private Vector lines;
    private Image rotImage;
    private int textWidth;
    private String oldYString;
    private Component currentComponent;
    private boolean xLabelsVerticalFlag;
    private Vector vertLabels;
    private Vector vertImages;
    private Image vertImage;
    private int tipo;
    private String [] labels;
}
