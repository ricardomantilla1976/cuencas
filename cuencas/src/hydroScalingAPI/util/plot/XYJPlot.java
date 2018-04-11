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
 * XYJPlot.java
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
import java.net.URL;
import java.util.Enumeration;
import java.util.Vector;
import java.awt.event.MouseEvent;

public class XYJPlot {
    
    public XYJPlot(Component p,double[][] data,String Title,String xLabel,String yLabel,double falt,Color c,int t){
        this(p,null);
        title(Title);
        xTitle(xLabel);
        yTitle(yLabel);
        addDoubles(data,c,t,falt);
        create();
    }
    public XYJPlot(Component p,String Title,String xLabel,String yLabel){
        this(p,null);
        title(Title);
        xTitle(xLabel);
        yTitle(yLabel);
        create();
    }
    
    public XYJPlot(Component p,double[][] datos,String Title,String xLabel,String yLabel,double falt){
        this(p,datos,Title,xLabel,yLabel,falt,Color.blue,0);
    }
    
    public XYJPlot(Component p,double[][] datosx,double[][] datosy,String[] leg,String Title,String xLabel,String yLabel,double falt,Color c,int t){
        this(p,null);
        title(Title);
        xTitle(xLabel);
        yTitle(yLabel);
        for(int l=0;l<Math.min(Math.min(datosx.length,datosy.length),leg.length);l++)
            addDoubles(new double[][]{datosx[l],datosy[l]},Color.blue,0,leg[l],6,null,falt);
        axis.setTipo(t);
        create();
        legend.show(true);
    }
    public XYJPlot(Component p,double[][] datosx,double[][] datosy,String[] leg,String Title,String xLabel,String yLabel,double falt){
        this(p,datosx,datosy,leg,Title,xLabel,yLabel,falt,Color.blue,0);
    }
    
    public XYJPlot(Component p,double[] xdatos,double[] ydatos,String Title,String xLabel,String yLabel,double falt){
        this(p,new double[][]{xdatos,ydatos},Title,xLabel,yLabel,falt,Color.blue,0);
    }
    
    public XYJPlot(Component p,double[] xdatos,double[] ydatos,String Title,String xLabel,String yLabel,double falt,Color c,int t){
        this(p,new double[][]{xdatos,ydatos},Title,xLabel,yLabel,falt,c,t);
    }
    
    
    //ciclo anual
    public XYJPlot(Component p,double[] datos,String[] labels,String Title,String xLabel,String yLabel,double falt,Color c,int t){
        this(p,labels);
        title(Title);
        xTitle(xLabel);
        yTitle(yLabel);
        double [][] dat = new double [2][datos.length];
        dat[1] = datos;
        for(int i=0;i< datos.length;i++)
            dat[0][i] = (double)i;
        tipo = t;
        axis.setTipo(t);
        addDoubles(dat,c, t,falt);
        create();
    }
    
    public XYJPlot(Component p,double[] datos,String Title,String xLabel,String yLabel,double falt){
        this(p,datos,new String[]{"Ene","Feb","Mar","Abr","May","Jun","Jul","Agt","Sep","Oct","Nov","Dic"},Title,xLabel,yLabel,falt,Color.blue,1);
    }
    
    public XYJPlot(Component p,double[] datos,String Title,String xLabel,String yLabel,double falt,Color c,int t){
        this(p,datos,new String[]{"Ene","Feb","Mar","Abr","May","Jun","Jul","Agt","Sep","Oct","Nov","Dic"},Title,xLabel,yLabel,falt,c,t);
    }
    
    public XYJPlot(Component p,double[] datos,String[] labels,String Title,String xLabel,String yLabel,double falt){
        this(p,datos,labels,Title,xLabel,yLabel,falt,Color.blue,1);
    }
    
    
    
    public XYJPlot(Component p, String []labels) {
        parent = p;
        numberOfPlots = 0;
        xminimum = -100D;
        xmaximum = 900D;
        yminimum = -50D;
        ymaximum = 125D;
        oldString = "";
        xyFont = new Font("Dialog", 0, 10);
        shiftWasSelected = false;
        dataFromTags = false;
        firstColumnLabelsFlag = false;
        xLabelsVerticalFlag = false;
        w = 0;
        h = 0;
        axis = new XYAxis(0, 0, w, h, parent,tipo,labels);
        axis.axisColor(Color.black);
        //axis.labelColor(Color.black);
        legend = new XYLegend(axis.plotXend() - 50, axis.plotYend() + 10, 15);
        colors = new Vector(20);
        colors.addElement(Color.blue);
        colors.addElement(new Color(0xff0000));
        colors.addElement(new Color(0xff8000));
        colors.addElement(new Color(52224));
        colors.addElement(new Color(56797));
        colors.addElement(new Color(0xffff00));
        colors.addElement(new Color(0xff00ff));
        colors.addElement(new Color(0xdddddd));
        colors.addElement(new Color(0x444444));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        colors.addElement(new Color(65535));
        dataDoubles = new Vector();
        dataFaltantes = new Vector();
        dataColors = new Vector();
        symbolColors = new Vector();
        dataLabels = new Vector();
        dataTypes = new Vector();
        dataSymbolSizes = new Vector();
        parent.addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentResized(java.awt.event.ComponentEvent evt) {
                w = parent.getWidth();
                h = parent.getHeight();
                axis.setDimension(w,h);
            }
        }
        );
        parent.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                MousePressed(evt);
            }
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                MouseReleased(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                MouseExited(evt);
            }
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                MouseEntered(evt);
            }
        }
        );
        parent.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
            public void mouseMoved(java.awt.event.MouseEvent evt) {
                MouseMoved(evt);
            }
            public void mouseDragged(java.awt.event.MouseEvent evt) {
                MouseDragged(evt);
            }
        }
        );
        
    }
    
    public void create() {
        lines = new Vector(numberOfPlots);
        for(int i = 0; i < numberOfPlots; i++) {
            double[][] datos;
            String s = "";
            datos = (double[][])dataDoubles.elementAt(i);
            double faltante = ((Double)dataFaltantes.elementAt(i)).doubleValue();
            s = (String)dataLabels.elementAt(i);
            Integer integer = (Integer)dataTypes.elementAt(i);
            Integer integer1 = (Integer)dataSymbolSizes.elementAt(i);
            Color color = (Color)dataColors.elementAt(i);
            if(color == null)
                if(i < colors.size())
                    color = (Color)colors.elementAt(i);
                else
                    color = Color.red;
            Color color1 = (Color)symbolColors.elementAt(i);
            if(color1 == null)
                color1 = color;
            line = new XYLine(datos,s, color, color1, integer.intValue(), firstColumnLabelsFlag,faltante);
            line.symbolSize(integer1.intValue());
            if(i == 0) {
                xminimum = line.xminAxis();
                xmaximum = line.xmaxAxis();
                yminimum = line.yminAxis();
                ymaximum = line.ymaxAxis();
            } else {
                xminimum = Math.min(xminimum, line.xminAxis());
                xmaximum = Math.max(xmaximum, line.xmaxAxis());
                yminimum = Math.min(yminimum, line.yminAxis());
                ymaximum = Math.max(ymaximum, line.ymaxAxis());
            }
            lines.addElement(line);
        }
        
        axis.xmin(xminimum);
        axis.xmax(xmaximum);
        axis.ymin(yminimum);
        axis.ymax(ymaximum);
        axis.setData(lines);
    }
    
    public void paint(Graphics g) {
        w = parent.getWidth();
        h = parent.getHeight();
        axis.setDimension(w,h);
        gx = g;
        axis.graphics(gx);
        axis.draw();
        for(Enumeration enumeration = lines.elements(); enumeration.hasMoreElements(); line.draw(gx, axis))
            line = (XYLine)enumeration.nextElement();
        
        legend.draw(gx, lines);
        //gx.drawRect(100,100,100,100);
    }
    
    public void MouseMoved(java.awt.event.MouseEvent event){
        gx=parent.getGraphics();
        int i = event.getX();
        int j = event.getY();
        double d2 = axis.xmax() - axis.xmin();
        double d = (double)((float)(i - axis.plotXstart()) / (float)axis.plotWidth()) * d2 + axis.xmin();
        double d3 = axis.ymax() - axis.ymin();
        double d1 = (double)((float)(axis.plotYstart() - j) / (float)axis.plotHeight()) * d3 + axis.ymin();
        Float float1 = new Float((float)d);
        String s = new String(float1.toString());
        float1 = new Float((float)d1);
        String s1 = new String(float1.toString());
        String s2 = "X: " + s + "   Y: " + s1;
        if(oldString.compareTo(s2) != 0) {
            gx.setFont(xyFont);
            gx.setColor(axis.backgroundColor());
            gx.drawString(oldString, axis.width() / 2, axis.plotYend() - 2);
            oldString = s2;
            gx.setColor(axis.axisColor());
            gx.drawString(s2, axis.width() / 2, axis.plotYend() - 2);
        }
        
    }
    public void MouseDragged(java.awt.event.MouseEvent event){
        gx=parent.getGraphics();
        int i = event.getX();
        int j = event.getY();
        if(event.isShiftDown() || shiftWasSelected) {
            if(legend.show()) {
                legend.eraseOutline(gx);
                legend.x(legend.x() + (i - oldx) + first_x);
                legend.y(legend.y() + (j - oldy) + first_y);
                oldx = legend.x();
                oldy = legend.y();
                legend.eraseOutline(gx);
            }
        } else {
            gx.setXORMode(Color.white);
            //            gx.drawRect(first_x, first_y, oldx - first_x, oldy - first_y);
            gx.drawLine(first_x,first_y,first_x,oldy);
            gx.drawLine(first_x,first_y,oldx,first_y);
            gx.drawLine(oldx,oldy,oldx,first_y);
            gx.drawLine(oldx,oldy,first_x,oldy);
            
            //            gx.setColor(Color.green);
            /*if(i < first_x)
                i = first_x;
            if(j < first_y)
                j = first_y;*/
            //gx.drawRect(first_x, first_y, i - first_x, j - first_y);
            oldx = i;
            oldy = j;
            gx.drawLine(first_x,first_y,first_x,oldy);
            gx.drawLine(first_x,first_y,oldx,first_y);
            gx.drawLine(oldx,oldy,oldx,first_y);
            gx.drawLine(oldx,oldy,first_x,oldy);
            
        }
    }
    
    public void MouseExited(java.awt.event.MouseEvent event){
        gx=parent.getGraphics();
        Color color = gx.getColor();
        Font font = gx.getFont();
        gx.setFont(xyFont);
        gx.setColor(axis.backgroundColor());
        gx.drawString(oldString, axis.width() / 2, axis.plotYend() - 2);
        gx.setColor(color);
        gx.setFont(font);
        parent.setCursor(new java.awt.Cursor(0));
        
    }
    public void MouseEntered(java.awt.event.MouseEvent event){
        parent.setCursor(new java.awt.Cursor(1));
        
    }
    
    public void MousePressed(java.awt.event.MouseEvent event){
        gx=parent.getGraphics();
        int i = event.getX();
        int j = event.getY();
        if(event.isShiftDown()) {
            if(legend.show()) {
                oldx = legend.x();
                oldy = legend.y();
                first_x = legend.x() - i;
                first_y = legend.y() - j;
                gx.setColor(Color.black);
                legend.eraseOutline(gx);
                shiftWasSelected = true;
            }
        } else {
            oldx = first_x = i;
            oldy = first_y = j;
            gx.setFont(xyFont);
            gx.setColor(axis.backgroundColor());
            gx.drawString(oldString, axis.width() / 2, axis.plotYend() - 2);
            gx.setColor(Color.black);
        }
    }
    
    
    public void MouseReleased(java.awt.event.MouseEvent event){
        gx=parent.getGraphics();
        int i = event.getX();
        int j = event.getY();
        if(event.isShiftDown() || shiftWasSelected) {
            if(legend.show()) {
                paint(gx);
                shiftWasSelected = false;
            }
        } else {
            gx.setXORMode(Color.white);
            gx.drawRect(first_x, first_y, oldx - first_x, oldy - first_y);
            gx.drawLine(first_x,first_y,first_x,oldy);
            gx.drawLine(first_x,first_y,oldx,first_y);
            gx.drawLine(oldx,oldy,oldx,first_y);
            gx.drawLine(oldx,oldy,first_x,oldy);
            
            
            gx.setPaintMode();
            if(first_x < i && first_y < j) {
                double d4 = axis.xmax() - axis.xmin();
                double d = (double)((float)(first_x - axis.plotXstart()) / (float)axis.plotWidth()) * d4 + axis.xmin();
                double d1 = (double)((float)(i - axis.plotXstart()) / (float)axis.plotWidth()) * d4 + axis.xmin();
                double d5 = axis.ymax() - axis.ymin();
                double d3 = (double)((float)(axis.plotYstart() - first_y) / (float)axis.plotHeight()) * d5 + axis.ymin();
                double d2 = (double)((float)(axis.plotYstart() - j) / (float)axis.plotHeight()) * d5 + axis.ymin();
                axis.xmin(d);
                axis.xmax(d1);
                axis.ymin(d2);
                axis.ymax(d3);
            } else if(first_x == i && first_y == j) {
                axis.xmin(xminimum);
                axis.xmax(xmaximum);
                axis.ymin(yminimum);
                axis.ymax(ymaximum);
            } else{
                double d4 = axis.xmax() - axis.xmin();
                double d = (double)((float)(Math.min(first_x,i) - axis.plotXstart()) / (float)axis.plotWidth()) * d4 + axis.xmin();
                double d1 = (double)((float)(Math.max(i,first_x) - axis.plotXstart()) / (float)axis.plotWidth()) * d4 + axis.xmin();
                double d5 = axis.ymax() - axis.ymin();
                double d3 = (double)((float)(axis.plotYstart() - Math.min(first_y,j)) / (float)axis.plotHeight()) * d5 + axis.ymin();
                double d2 = (double)((float)(axis.plotYstart() - Math.max(j,first_y)) / (float)axis.plotHeight()) * d5 + axis.ymin();
                axis.xmin(d);
                axis.xmax(d1);
                axis.ymin(d2);
                axis.ymax(d3);
                
            }
            
            paint(gx);
        }
    }
    
    
    public boolean keyDown(Event event, int i) {
        gx=parent.getGraphics();
        if(legend.show())
            switch(i) {
                default:
                    break;
                    
                case 1005:
                    legend.y(legend.y() + 1);
                    if(event.shiftDown())
                        legend.y(legend.y() + 10);
                    paint(gx);
                    break;
                    
                case 1004:
                    legend.y(legend.y() - 1);
                    if(event.shiftDown())
                        legend.y(legend.y() - 10);
                    paint(gx);
                    break;
                    
                case 1006:
                    legend.x(legend.x() - 1);
                    if(event.shiftDown())
                        legend.x(legend.x() - 10);
                    paint(gx);
                    break;
                    
                case 1007:
                    legend.x(legend.x() + 1);
                    if(event.shiftDown())
                        legend.x(legend.x() + 10);
                    paint(gx);
                    break;
            }
            return true;
    }
    
    public void labelXFontSize(int i) {
        axis.labelXFontSize(i);
    }
    
    public void labelYFontSize(int i) {
        axis.labelYFontSize(i);
    }
    
    public void titleFontSize(int i) {
        axis.titleFontSize(i);
    }
    
    public void axisFontSize(int i) {
        axis.axisFontSize(i);
    }
    
    public void labelColor(Color color) {
        axis.labelColor(color);
    }
    
    public void axisColor(Color color) {
        axis.axisColor(color);
    }
    
    public void gridColor(Color color) {
        axis.gridColor(color);
    }
    
    public void backgroundColor(Color color) {
        axis.backgroundColor(color);
    }
    
    public void openAxis(boolean flag) {
        axis.openAxis(flag);
    }
    
    public void gridX(boolean flag) {
        axis.gridX(flag);
    }
    
    public void gridY(boolean flag) {
        axis.gridY(flag);
    }
    
    public void legendShow(boolean flag) {
        legend.show(flag);
    }
    
    public void title(String s) {
        axis.title(s);
    }
    
    public void xTitle(String s) {
        axis.xTitle(s);
    }
    
    public void yTitle(String s) {
        axis.yTitle(s);
    }
    
    public void legendX(int i) {
        legend.x(i);
    }
    
    public void legendY(int i) {
        legend.y(i);
    }
    
    public void legendIncrement(int i) {
        legend.increment(i);
    }
    
    public void legendFontSize(int i) {
        legend.fontSize(i);
    }
    
    public void numberOfPlots(int i) {
        numberOfPlots = i;
    }
    
    public int numberOfPlots() {
        return numberOfPlots;
    }
    
    public void graphics(Graphics g) {
        gx = g.create();
    }
    
    public void deleteAll(){
        numberOfPlots = 0;
        dataDoubles.removeAllElements();
        dataFaltantes.removeAllElements();
        dataColors.removeAllElements();
        dataLabels.removeAllElements();
        symbolColors.removeAllElements();
        dataTypes.removeAllElements();
        dataSymbolSizes.removeAllElements();
    }
    public void addDoubles(double[][] d,Color c,int type,String label,int dSymbolSize,Color sColor,double faltante){
        numberOfPlots++;
        dataDoubles.addElement(d);
        dataFaltantes.addElement(new Double(faltante));
        dataColors.addElement(c);
        dataLabels.addElement(label);
        symbolColors.addElement(sColor);
        dataTypes.addElement(new Integer(type));
        dataSymbolSizes.addElement(new Integer(dSymbolSize));
    }
    public void addDoubles(double[][] d){
        addDoubles(d,null,0,"",6,null,Double.POSITIVE_INFINITY);
    }
    public void addDoubles(double[][] d,Color c){
        addDoubles(d,c,0,"",6,null,Double.POSITIVE_INFINITY);
    }
    public void addDoubles(double[][] d,Color c,int tipo,double faltante){
        addDoubles(d,c,tipo,"",6,null,faltante);
    }
    public void addDoubles(double[][] d,int tipo){
        addDoubles(d,null,tipo,"",6,null,Double.POSITIVE_INFINITY);
    }
    
    public void setXRange(double min,double max){
        xminimum = min;
        xmaximum = max;
        axis.xmin(min);
        axis.xmax(max);
    }
    public void setXmax(double max){
        xmaximum = max;
        axis.xmax(max);
    }
    public void setXmin(double min){
        xminimum = min;
        axis.xmin(min);
    }
    public void setYmax(double max){
        ymaximum = max;
        axis.ymax(max);
    }
    public void setYmin(double min){
        yminimum = min;
        axis.ymin(min);
    }
    public void setYRange(double min,double max){
        yminimum = min;
        ymaximum = max;
        axis.ymin(min);
        axis.ymax(max);
    }
    
    
    public void firstColumnLabels(boolean flag) {
        axis.firstColumnLabels(flag);
        firstColumnLabelsFlag = flag;
    }
    
    public void xLabelsVertical(boolean flag) {
        axis.xLabelsVerticalFlag(flag);
        xLabelsVerticalFlag = flag;
    }
    
    protected int w;
    protected int h;
    protected Component parent;
    protected XYAxis axis;
    protected XYLine line;
    protected XYLegend legend;
    protected int numberOfPlots;
    protected int first_x;
    protected int first_y;
    protected int oldx;
    protected int oldy;
    protected Graphics gx;
    protected Vector lines;
    protected Vector colors;
    protected Vector dataDoubles;
    protected Vector dataFaltantes;
    protected Vector dataColors;
    protected Vector dataLabels;
    protected Vector symbolColors;
    protected Vector dataTypes;
    protected Vector dataSymbolSizes;
    protected double xminimum;
    protected double xmaximum;
    protected double yminimum;
    protected double ymaximum;
    protected String oldString;
    protected Font xyFont;
    protected boolean shiftWasSelected;
    protected boolean dataFromTags;
    protected boolean firstColumnLabelsFlag;
    protected boolean xLabelsVerticalFlag;
    protected int tipo;
}
