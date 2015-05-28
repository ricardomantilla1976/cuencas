/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.swmmCoupling.objects;

import hydroScalingAPI.io.DataRaster;
import hydroScalingAPI.io.MetaRaster;
import java.io.IOException;

/**
 * Contains methods to be included in MetaRaster object
 * @author A. D. L. Zanchetta
 */
public abstract class MetaRasterTool {
    
    public static int getXfromID(MetaRaster metaRaster_arg, int ID_arg){
        return (ID_arg % metaRaster_arg.getNumCols());
    }
    
    public static int getYfromID(MetaRaster metaRaster_arg, int ID_arg){
        return (ID_arg / metaRaster_arg.getNumCols());
    }
    
    public static double getLatFromY(MetaRaster metaRaster_arg, int yCon){
        return (metaRaster_arg.getMinLat() + yCon * metaRaster_arg.getResLat()/3600.0);
    }
    
    public static double getLatFromID(MetaRaster metaRaster_arg, int ID){
        return (MetaRasterTool.getLatFromY(metaRaster_arg, 
                                           MetaRasterTool.getYfromID(metaRaster_arg, ID)));
    }
    
    public static double getLonFromX(MetaRaster metaRaster_arg, int xCon){
        return (metaRaster_arg.getMinLon() + ((xCon * metaRaster_arg.getResLon())/3600.d));
    }
    
    public static double getLonFromID(MetaRaster metaRaster_arg, int ID){
        return (MetaRasterTool.getLonFromX(metaRaster_arg, 
                                           MetaRasterTool.getXfromID(metaRaster_arg, ID)));
    }

    /**
     * 
     * @param longitude_arg
     * @return 
     */
    public static int getXfromLongitude(MetaRaster mRaster_arg, 
                                        double longitude_arg){
        double demMinLon, demResLon;
        int returnX;
        
        demMinLon = mRaster_arg.getMinLon();
        demResLon = mRaster_arg.getResLon();
        
        //returnX = (int)(((longitude_arg - this.getMinLon())/this.getResLon())*3600.d);
        returnX = (int)Math.round(((longitude_arg - demMinLon)/demResLon)*3600.d);
    
        return (returnX);
    }
    
    /**
     * 
     * @param latitude_arg
     * @return 
     */
    public static int getYfromLatitude(MetaRaster mRaster_arg, double latitude_arg){
        double demMinLat, demResLat;
        int returnY;
        
        demMinLat = mRaster_arg.getMinLat();
        demResLat = mRaster_arg.getResLat();
        
        returnY = (int)Math.round((latitude_arg - demMinLat)/demResLat * 3600.0d);
        
        return (returnY);
    }
    
    /**
     * 
     * @param longitude_arg
     * @param latitude_arg
     * @return 
     */
    public static int getIdFromLongLat(MetaRaster mRaster_arg, float longitude_arg, float latitude_arg){
        int givenX, givenY;
        int returnId;
            
        // getting coordinates
        givenX = MetaRasterTool.getXfromLongitude(mRaster_arg, longitude_arg);
        givenY = MetaRasterTool.getYfromLatitude(mRaster_arg, latitude_arg);
            
        return (MetaRasterTool.getIdFromXandY(mRaster_arg, givenX, givenY));
    }
    
    /**
     * 
     * @param x_arg
     * @param y_arg
     * @return 
     */
    public static int getIdFromXandY(MetaRaster mRaster_arg, int x_arg, int y_arg){
        int demNumCols;
        int returnId;
            
        // getting coordinates
        demNumCols = mRaster_arg.getNumCols();
        returnId = (demNumCols * y_arg) + x_arg;
            
        return (returnId);
    }
    
    /**
     * 
     * @param id1_arg
     * @param id2_arg
     * @return 
     */
    public static double getPixelDistanceFromIDs(MetaRaster mRaster_arg, int id1_arg, int id2_arg){
        int X1, Y1;
        int X2, Y2;
        double rets;
        
        //
        X1 = MetaRasterTool.getXfromID(mRaster_arg, id1_arg);
        Y1 = MetaRasterTool.getYfromID(mRaster_arg, id1_arg);
        
        //
        X2 = MetaRasterTool.getXfromID(mRaster_arg, id2_arg);
        Y2 = MetaRasterTool.getYfromID(mRaster_arg, id2_arg);
        
        rets = Math.sqrt(Math.pow((X2 - X1), 2) + Math.pow((Y2 - Y1), 2));
        
        return (rets);
    }
    
    public static byte[][] getLinksMask(MetaRaster mRaster_arg){
        String pathToRasterNetwork;
        byte [][] rasterNetwork;
        
        pathToRasterNetwork = mRaster_arg.getLocationBinaryFile().getPath();
        pathToRasterNetwork=pathToRasterNetwork.subSequence(0, pathToRasterNetwork.lastIndexOf("."))+".redRas";
        mRaster_arg.setLocationBinaryFile(new java.io.File(pathToRasterNetwork));
        mRaster_arg.setFormat("Byte");

        try{
            rasterNetwork = new DataRaster(mRaster_arg).getByte();
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            rasterNetwork = null;
        }
        
        return (rasterNetwork);
    }
}
