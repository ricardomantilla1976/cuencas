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
 * NewZealandHGAnalysis.java
 *
 * Created on March 10, 2005, 10:54 AM
 */

package hydroScalingAPI.examples.dataAnalysis;

/**
 *
 * @author Ricardo Mantilla
 */
public class NewZealandHGAnalysis {
    
    private hydroScalingAPI.mainGUI.objects.GUI_InfoManager localInfoManager;
    private hydroScalingAPI.util.database.DataBaseEngine localDatabaseEngine;
    private hydroScalingAPI.mainGUI.objects.LocationsManager localLocationsManager;
    
    /** Creates a new instance of NewZealandHGAnalysis */
    public NewZealandHGAnalysis() throws visad.VisADException, java.io.IOException{
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/Ashley_NZ_database/Rasters/Topography/ashley.metaDEM");
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(theFile);
        String formatoOriginal=metaData.getFormat();
        
        metaData.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Ashley_NZ_database/Rasters/Topography/ashley.areas"));
        metaData.setFormat("Float");
        float[][] matAreas=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        
        metaData.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Ashley_NZ_database/Rasters/Topography/ashley.horton"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".horton"));
        byte[][] matDirs=new hydroScalingAPI.io.DataRaster(metaData).getByte();
        
        
        localInfoManager=new hydroScalingAPI.mainGUI.objects.GUI_InfoManager();
        localDatabaseEngine=new hydroScalingAPI.util.database.DataBaseEngine();
        localLocationsManager=new hydroScalingAPI.mainGUI.objects.LocationsManager(localInfoManager,localDatabaseEngine);
        
        while(!localLocationsManager.isLoaded()){
            new visad.util.Delay(1000);
            System.out.print("/");
        }
        System.out.println("/");
        java.util.Vector theVector=localLocationsManager.getNames();
        
        System.out.println("Location Code, Stream Order, Basin Area [Km2], Measured Flow [L/s], Channel Width [m], Channel Depth [m], Channel Velocity [m/s], Channel Slope [*]");
        
        for (int i=0;i<theVector.size();i++) {
            String identif=theVector.get(i).toString();
            //System.out.println(identif);
            String code=identif.substring(0, identif.lastIndexOf("."));
            String type=identif.substring(identif.lastIndexOf(".")+1);
            hydroScalingAPI.io.MetaLocation myLocation = localLocationsManager.getLocation(code,type);
            visad.RealTuple coord=myLocation.getPositionTuple();
            double longitudeMeasurement=coord.getValues()[0];
            double latitudeMeasurement=coord.getValues()[1];
            
            int MatX=(int) ((longitudeMeasurement -metaData.getMinLon())/(float) metaData.getResLon()*3600.0f);
            int MatY=(int) ((latitudeMeasurement -metaData.getMinLat())/(float) metaData.getResLat()*3600.0f);
            
            String information=myLocation.getInformation();
            
            String flowValue=information.substring(information.indexOf("FLOW [L/s]:")+12,information.indexOf("\nXS AREA"));
            String widthValue=information.substring(information.indexOf("WIDTH [m]:")+11,information.indexOf("\nDEPTH"));
            String depthValue=information.substring(information.indexOf("DEPTH [m]:")+11,information.indexOf("\nVEL"));
            String velocValue=information.substring(information.indexOf("VEL [m/s]:")+11,information.indexOf("\nWAT_TEMP"));
            String slopeValue=information.substring(information.indexOf("RSLOPE:")+9);
            
            System.out.println(myLocation.getProperty("[site name]")+","+matDirs[MatY][MatX]+","+matAreas[MatY][MatX]+","+flowValue+","+widthValue+","+depthValue+","+velocValue+","+slopeValue);
            
        }
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            new NewZealandHGAnalysis();
        } catch (java.io.IOException ioe){
            System.err.println("Failed running NewZealandHGAnalysis");
            System.err.println(ioe);
        } catch (visad.VisADException vie){
            System.err.println("Failed running NewZealandHGAnalysis");
            System.err.println(vie);
        }
    }
    
}
