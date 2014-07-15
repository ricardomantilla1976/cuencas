/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.dataAnalysis;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ricardo
 */
public class CalculateSurfaceArea {
    
    public CalculateSurfaceArea(){
        try {
            java.io.File theFile=new java.io.File("/CuencasDataBases/ShaleCreek/Rasters/Topography/shalehills.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/ShaleCreek/Rasters/Topography/shalehills.dem"));
            float [][] matDem=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/ShaleCreek/Rasters/Topography/shalehills.horton"));
            metaModif.setFormat("Byte");
            byte [][] matDemChannels=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/ShaleCreek/Rasters/Topography/shalehills.dir"));
            metaModif.setFormat("Byte");
            byte [][] matDir=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            
            theFile=new java.io.File("/CuencasDataBases/ShaleCreek/Rasters/Hydrology/shalehills_BasinMask.metaVHC");
            metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/ShaleCreek/Rasters/Hydrology/shalehills_BasinMask.vhc"));
            
            byte [][] matDemMask=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            int k=0;
            System.out.println("FID,Longitude,Latitude,Elevation,Channel,Direction");
            for (int i = 0; i < matDem.length; i++) {
                for (int j = 0; j < matDem[0].length; j++) {
                    double lat=i*metaModif.getResLat()/3600.0+metaModif.getMinLat();
                    double lon=j*metaModif.getResLon()/3600.0+metaModif.getMinLon();
                    //if(matDemMask[i][j] > 0) System.out.println("["+lon+","+lat+","+matDem[i][j]+"],$");
                    if(matDemMask[i][j] == 1) 
                        System.out.println((k++)+","+lon+","+lat+","+matDem[i][j]+","+matDemChannels[i][j]+","+matDir[i][j]);
                }
            }
            
            
        } catch (IOException ex) {
            Logger.getLogger(CalculateSurfaceArea.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new CalculateSurfaceArea();
    }
}
