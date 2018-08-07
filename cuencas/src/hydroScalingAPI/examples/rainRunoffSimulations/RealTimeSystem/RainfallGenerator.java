/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.rainRunoffSimulations.RealTimeSystem;

import java.io.IOException;
import java.util.Date;

/**
 *
 * @author ricardo
 */
public class RainfallGenerator {

    private boolean newFileCreated;

    private String rainDir="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/real_time/fakeFields/";

    public RainfallGenerator(String stringFileDate){

        int ncols = 921;
        int nrows = 921;
        double xllcorner = -93.342854;
        double yllcorner = 39.539695;
        double cellsize = 0.005248104;
        float nodata_value = -9999;

        try {

            java.io.File outputDir=new java.io.File(rainDir);
            java.io.File[] existentFiles=outputDir.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("vhc"));

            for (int i = 0; i < existentFiles.length; i++) {
                existentFiles[i].delete();
            }

            java.io.File outputBinaryFile = new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/real_time/fakeFields/prec."+stringFileDate+".vhc");

            java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));


            for (int i = 0; i < nrows; i++) {
                for (int j = 0; j < ncols; j++) {
                    rasterBuffer.writeFloat((float) Math.random() * 1000);
                    //rasterBuffer.writeFloat(10);
                }
            }

            rasterBuffer.close();

            newFileCreated=true;

        } catch (IOException iOException) {
            System.err.println(iOException.getStackTrace());
        }


    }

    public boolean gotField(){
        return newFileCreated;
    }

    public java.io.File getPathToRain(){
        return new java.io.File(rainDir+"/prec.metaVHC");
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new RainfallGenerator("xxxxxx");
    }

}
