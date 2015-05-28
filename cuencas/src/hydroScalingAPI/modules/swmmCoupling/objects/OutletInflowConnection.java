/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.swmmCoupling.objects;

/**
 *
 * @author A. D. L. Zanchetta
 */
public class OutletInflowConnection {
    private final int xOutlet, yOutlet;
    private final String nodeId;
    private final String inflowTitle;
    private double[][] inflowData; 
    
    public OutletInflowConnection(int xVal_arg, 
                                  int yVal_arg, 
                                  String nodeId_arg, 
                                  String inflowTitle_arg){
        this.xOutlet = xVal_arg;
        this.yOutlet = yVal_arg;
        this.nodeId = nodeId_arg;
        this.inflowTitle = inflowTitle_arg;
    }
    
    public int getX(){
        return (this.xOutlet);
    }
    
    public int getY(){
        return (this.yOutlet);
    }

    public String getNodeId() {
        return (nodeId);
    }
    
    public String getTimeSeriesTitle(){
        return (this.inflowTitle);
    }
    
    public void setTimeSeriesTitle(double[][] newTimeserie_arg){
        this.inflowData = newTimeserie_arg;
    }
    
    public double[][] getTimeSeriesData(){
        return (this.inflowData);
    }
    
    @Override
    public String toString(){
        return("[" + this.xOutlet + ", " + this.yOutlet + "] -> ["+this.nodeId+"] as '" + this.inflowTitle + "'");
    }
}
