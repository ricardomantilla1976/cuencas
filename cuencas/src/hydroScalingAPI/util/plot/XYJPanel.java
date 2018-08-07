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
 * XYJPanel.java
 *
 * Created on 24 de julio de 2001, 04:07 PM
 */

package hydroScalingAPI.util.plot;

/**
 *
 * @author Olver Hernandez
 */
public class XYJPanel extends javax.swing.JPanel {
    
    /** Creates new form XYJPanel */
    XYJPlot Pplot;
    
    public XYJPanel(String Title,String xLabel,String yLabel){
        initComponents();
        Pplot = new XYJPlot(this,Title,xLabel,yLabel);
        
    }
    
    
    public XYJPanel(double[][] datos,String Title,String xLabel,String yLabel,double falt){
        initComponents();
        Pplot = new XYJPlot(this,datos,Title,xLabel,yLabel,falt);
        
    }
    public XYJPanel(double[] datos,String Title,String xLabel,String yLabel,double falt){
        initComponents();
        Pplot = new XYJPlot(this,datos,Title,xLabel,yLabel,falt);
        
    }
    public XYJPanel(double[] datos,String [] labels,String Title,String xLabel,String yLabel,double falt){
        initComponents();
        Pplot = new XYJPlot(this,datos,labels,Title,xLabel,yLabel,falt);
        
    }
    public XYJPanel(double[] datosx,double[] datosy,String Title,String xLabel,String yLabel,double falt){
        initComponents();
        Pplot = new XYJPlot(this,datosx,datosy,Title,xLabel,yLabel,falt);
        
    }
    public XYJPanel(double[][] datosx,double[][] datosy,String[] leg,String Title,String xLabel,String yLabel,double falt){
        initComponents();
        Pplot = new XYJPlot(this,datosx,datosy,leg,Title,xLabel,yLabel,falt);
        
    }
    
    public XYJPanel(double[][] datos,String Title,String xLabel,String yLabel,double falt,java.awt.Color c,int t){
        initComponents();
        Pplot = new XYJPlot(this,datos,Title,xLabel,yLabel,falt,c,t);
        
    }
    public XYJPanel(double[] datos,String Title,String xLabel,String yLabel,double falt,java.awt.Color c,int t){
        initComponents();
        Pplot = new XYJPlot(this,datos,Title,xLabel,yLabel,falt,c,t);
        
    }
    public XYJPanel(double[] datos, String [] labels,String Title,String xLabel,String yLabel,double falt,java.awt.Color c,int t){
        initComponents();
        Pplot = new XYJPlot(this,datos,labels,Title,xLabel,yLabel,falt,c,t);
        
    }
    public XYJPanel(double[] datosx,double[] datosy,String Title,String xLabel,String yLabel,double falt,java.awt.Color c,int t){
        initComponents();
        Pplot = new XYJPlot(this,datosx,datosy,Title,xLabel,yLabel,falt,c,t);
        
    }
    public XYJPanel(double[][] datosx,double[][] datosy,String[] leg,String Title,String xLabel,String yLabel,double falt,java.awt.Color c,int t){
        initComponents();
        Pplot = new XYJPlot(this,datosx,datosy,leg,Title,xLabel,yLabel,falt,c,t);
        
    }
    
    public void paint(java.awt.Graphics g){
        Pplot.paint(g);
    }
    
    public void setXRange(double min,double max){
        Pplot.setXRange(min,max);
    }
    
    public void setYRange(double min,double max){
        Pplot.setYRange(min,max);
    }
    public void setXMin(double min){
        Pplot.setXmin(min);
    }
    public void setXMax(double max){
        Pplot.setXmax(max);
    }
    public void setYMin(double min){
        Pplot.setYmin(min);
    }
    public void setYMax(double max){
        Pplot.setYmax(max);
    }
    public void addDatos(double[] datosx,double[] datosy,double falt){
        Pplot.addDoubles(new double[][]{datosx,datosy},java.awt.Color.blue,0,falt);
        Pplot.create();
        repaint();
    }
    public void addDatos(double[] datosy,double falt){
        double [][]datos = new double[2][datosy.length];
        datos[1]=datosy;
        for(int i=0;i< datos[1].length;i++)
            datos[0][i] = (double)i;
        Pplot.addDoubles(datos,java.awt.Color.blue,1,falt);
        Pplot.create();
        repaint();
    }
    
    public void addDatos(float[] datosx,float[] datosy,double falt,java.awt.Color c,int t){
        double[][] tranformedDatos=new double[2][datosx.length];
        for (int i = 0; i < datosx.length; i++) {
            tranformedDatos[0][i]=datosx[i];
            tranformedDatos[1][i]=datosy[i];
        }
        addDatos(tranformedDatos[0],tranformedDatos[1],falt,c,t);
    }
    
    public void addDatos(double[] datosx,double[] datosy,double falt,java.awt.Color c,int t){
        Pplot.addDoubles(new double[][]{datosx,datosy},c,t,falt);
        Pplot.create();
        repaint();
    }
    
    public void addDatos(double[] datosx,double[] datosy,double falt,java.awt.Color c,int t,java.awt.Color sColor,int sTam){
        Pplot.addDoubles(new double[][]{datosx,datosy},c,t,"",sTam,sColor,falt);
        Pplot.create();
        repaint();
    }
    
    public void removeAll(){
        Pplot.deleteAll();
        Pplot.create();
        repaint();
    }
    public java.awt.image.BufferedImage getImage(){
        java.awt.image.BufferedImage i = (java.awt.image.BufferedImage)createImage(getWidth(),getHeight());
        paint(i.getGraphics());
        return i;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        setLayout (new java.awt.BorderLayout ());

    }//GEN-END:initComponents
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
    
}
