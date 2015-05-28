/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.swmmCoupling.io.metaPolygonUrban;

import hydroScalingAPI.io.MetaRaster;
import hydroScalingAPI.modules.swmmCoupling.objects.MetaRasterTool;
import hydroScalingAPI.tools.DegreesToDMS;

/**
 *
 * @author A. D. L. Zanchetta
 */
class Anylet {
    protected int ID;
    protected double latitude;
    protected double longitude;
    private int altitude;           // above sea level, in meter
    private int type;
    protected double[][] flowSerie; //[N][2], [n][0]:timestamp, [n][1]:flow
      
    public Anylet(double lat_arg, double lng_arg){
        this(lat_arg, lng_arg, MetaPolygonUrban.UNKNOW_LABEL);
    }
        
    public Anylet(double lat_arg, double lng_arg, int type_arg){
        this(lat_arg, lng_arg, 0, type_arg);
    }
    
    public Anylet(double lat_arg, double lng_arg, int altitude_arg, int type_arg){
        this.latitude = lat_arg;
        this.longitude = lng_arg;
        this.altitude = altitude_arg;
        this.type = type_arg;
        this.ID = 0;
    }
        
    public double getLatitude(){
        return(this.latitude);
    }
        
    public double getLongitude(){
        return(this.longitude);
    }

    public int getAltitude() {
        return altitude;
    }
        
    public int getID(){
        return(this.ID);
    }

    public int getType() {
        return type;
    }
        
    public void setID(int newId_arg){
        this.ID = newId_arg;
    }
        
    public void setFlowSerie(double[][] newFlow_arg){
        this.flowSerie = newFlow_arg;
    }
        
    public void setType(int newType_arg){
            if (newType_arg == MetaPolygonUrban.INLET_LABEL){
                this.type = newType_arg;
            } else if (newType_arg == MetaPolygonUrban.OUTLET_LABEL) {
                this.type = newType_arg;
            } else {
                this.type = MetaPolygonUrban.UNKNOW_LABEL;
            }
    }
        
    @Override
    public String toString(){
            String retStr;
            
            retStr = "Lat/Long: ";
            retStr += DegreesToDMS.getprettyString(this.latitude, 0);
            retStr += " / ";
            retStr += DegreesToDMS.getprettyString(this.longitude, 1);
            retStr += " is an ";
            
            if (this.type == MetaPolygonUrban.INLET_LABEL) {
                retStr += "INLET.";
            } else if (this.type == MetaPolygonUrban.OUTLET_LABEL) {
                retStr += "OUTLET.";
            } else {
                retStr += "UNKNOW TYPE.";
            }
            
            return (retStr);
    }
        
    public String toString(MetaRaster mRaster_arg){
        String retStr;
            
        retStr = "X/Y: ";
        retStr += MetaRasterTool.getXfromLongitude(mRaster_arg, this.longitude);
        retStr += " / ";
        retStr += MetaRasterTool.getYfromLatitude(mRaster_arg, this.latitude);
        retStr += " is an ";
            
        if (this.type == MetaPolygonUrban.INLET_LABEL) {
            retStr += "INLET.";
        } else if (this.type == MetaPolygonUrban.OUTLET_LABEL) {
            retStr += "OUTLET.";
        } else {
            retStr += "UNKNOW TYPE.";
        }
            
        return (retStr);
    }
}
