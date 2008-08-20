/*
 * XYPanel.java
 *
 * Created on 24 de julio de 2001, 03:59 PM
 */

package hydroScalingAPI.util.plot;

/**
 *
 * @author Olver Hernandez
 */
public class XYPanel extends java.awt.Panel   {

    /** Creates new form XYPanel */
    XYPlot Pplot;
    public int w;
    public int h;
    
    
    public XYPanel(int W,int H,String Title,String xLabel,String yLabel){
        initComponents ();
        w=W;
        h=H;
        Pplot = new XYPlot(this,Title,xLabel,yLabel);
    }
    public XYPanel(int W,int H,double[][] datos,String Title,String xLabel,String yLabel,double falt){
        initComponents ();
        w=W;
        h=H;
        Pplot = new XYPlot(this,datos,Title,xLabel,yLabel,falt);
        
    }
    public XYPanel(int W,int H,double[] datos,String Title,String xLabel,String yLabel,double falt){
        initComponents ();
        w=W;
        h=H;
        Pplot = new XYPlot(this,datos,Title,xLabel,yLabel,falt);
        
    }
    public XYPanel(int W,int H,double[] datosx,double[] datosy,String Title,String xLabel,String yLabel,double falt){
        initComponents ();
        w=W;
        h=H;
        Pplot = new XYPlot(this,datosx,datosy,Title,xLabel,yLabel,falt);
        
    }
    public XYPanel(int W,int H,double[][] datosx,double[][] datosy,String[] leg,String Title,String xLabel,String yLabel,double falt){
        initComponents ();
        w=W;
        h=H;
        Pplot = new XYPlot(this,datosx,datosy,leg,Title,xLabel,yLabel,falt);
        
    }

    public XYPanel(int W,int H,double[][] datos,String Title,String xLabel,String yLabel,double falt,java.awt.Color c,int t){
        initComponents ();
        w=W;
        h=H;
        Pplot = new XYPlot(this,datos,Title,xLabel,yLabel,falt,c,t);
        
    }
    public XYPanel(int W,int H,double[] datos,String Title,String xLabel,String yLabel,double falt,java.awt.Color c,int t){
        initComponents ();
        w=W;
        h=H;
        Pplot = new XYPlot(this,datos,Title,xLabel,yLabel,falt,c,t);
        
    }
    public XYPanel(int W,int H,double[] datosx,double[] datosy,String Title,String xLabel,String yLabel,double falt,java.awt.Color c,int t){
        initComponents ();
        w=W;
        h=H;
        Pplot = new XYPlot(this,datosx,datosy,Title,xLabel,yLabel,falt,c,t);
        
    }
    public XYPanel(int W,int H,double[][] datosx,double[][] datosy,String[] leg,String Title,String xLabel,String yLabel,double falt,java.awt.Color c,int t){
        initComponents ();
        w=W;
        h=H;
        Pplot = new XYPlot(this,datosx,datosy,leg,Title,xLabel,yLabel,falt,c,t);
        
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
    public void addDatos(double[] datosx,double[] datosy,double falt,java.awt.Color c,int t){
        Pplot.addDoubles(new double[][]{datosx,datosy},c,t,falt);
        Pplot.create();
    }
    
    public void addDatos(double[] datosx,double[] datosy,double falt,java.awt.Color c,int t,java.awt.Color sColor,int sTam){
        Pplot.addDoubles(new double[][]{datosx,datosy},c,t,"",sTam,sColor,falt);
        Pplot.create();
    }
 
    

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents

        setLayout(new java.awt.BorderLayout());

    }//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}