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
 * simulationsRep3.java
 *
 * Created on December 6, 2001, 10:34 AM
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;
import java.io.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class PrecipGridToHillslopes extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
   
            
    /** Creates new simulationsRep3 */
    public PrecipGridToHillslopes(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, java.io.File newstormFile) throws java.io.IOException, VisADException{
        matDir=direcc;
        metaDatos=md;
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        System.out.println(stormFile);
        
        System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        if (!storm.isCompleted()) return;
        
        thisHillsInfo.setStormManager(storm);
                
        /* Walnut Gulch, AZ output path ...*/
        //String output_path="/home/furey/HSJ/walnut_az/simulations/";
        /* Goodwin Creek, MS output path ...*/
        //String output_path="/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/interpolated_events/05min_ts/";
        
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));
        
        //java.io.File archivo=new java.io.File(output_path+demName+"_"+storm.stormName()+"_IR_"+infiltRate+".dat");
        System.out.println(newstormFile);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(newstormFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.DataOutputStream newfile = new java.io.DataOutputStream(bufferout);
       
         
        java.util.Date startTime=new java.util.Date();
        
        
       //hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-1,10/60.);
        newfile.writeInt(linksStructure.contactsArray.length);
        
        int numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
        System.out.println("Num files ="+(numPeriods+1));
                  
        System.out.println("Inicia escritura de Resultados");
        
       // OUTPUT TO FILE - precip ts values for each hillslope
        System.out.println("Inicia escritura de Precipitaciones");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(thisHillsInfo.Area(i));
        }
        
        newfile.writeInt(numPeriods);
        
        for (int k=0;k<numPeriods;k++) {
            double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
            
            for (int i=0;i<linksStructure.contactsArray.length;i++){  
                newfile.writeDouble(thisHillsInfo.precipitation(i,currTime));
            }
        }
        
        System.out.println("Termina escritura de Precipitaciones");
        for (int i=0;i<linksStructure.connectionsArray.length;i++){
            newfile.writeInt(linksStructure.connectionsArray[i].length);
            if (linksStructure.connectionsArray[i].length > 0) {
                for (int jj=0;jj<linksStructure.connectionsArray[i].length;jj++){
                    newfile.writeInt(linksStructure.connectionsArray[i][jj]);
                }
            }
        }
        
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina escritura de Resultados");
        
        
        
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
        try{
            
            //Real Rain on Walnut Gulch
            subMain1(args);
            
            //Multifractal Rain on Peano
            //subMain2(args);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        
    }
    
    public static void subMain1 (String args[]) throws java.io.IOException, VisADException {
        
        /* Goodwin Creek, MS ... */
        String topo_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM";
        //String topo_path = "/hidrosigDataBases/DataBaseNSF_Project/BDRaster/Topografia/1_ArcSec/goodwin_MS/goodwinMS.metaMDT";
        //String rain_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/interpolated_events/05min_ts/";
        //String rain_path = "/hidrosigDataBases/DataBaseNSF_Project/BDRaster/Hidrologia/precipitation/storm/dataOverGoodwinCreek/05min_ts/";  
        String rain_path ="/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/interpolated_events/05min_ts/";
        String output_path = "/home/furey/Data/goodwin_ms/rg_hillslopets/events_good8195_auto/";
        
        java.io.File theFile=new java.io.File(topo_path);

        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        java.io.File newstormFile;
        
        /* OUTLET COORDS EXAMINED BELOW (x,y) ...
           WALNUT GULCH, AZ ... (210,330),(855,194),(736,401)
           Goodwin Creek, MS ...  (44,111) */
        
        //int[] events={21,32,36,47,48,49,63,66};  //events with R^2=0.99
        //int[] events={8,29,35,52}; //events with R^2=0.98,0.97
        int[] events={1,2,3,4,5,6,7,8,9,10};
        //int[] events={5,6,7,8};
        //int[] events={11}; //,12,13,14,15};
        //String[] gauss_types=
        //    {"_sd0p01_coe1p00","_sd0p05_coe1p00","_sd0p10_coe1p00","_sd0p20_coe1p00",
        //   "_sd0p30_coe1p00","_sd0p40_coe1p00","_sd0p01_coe0p25","_sd0p05_coe0p85","_sd0p10_coe0p95"}; // in order of event number
        //String[] gauss_types={"_sd0p05_coe1p00_L","_sd0p20_coe1p00_L","_sd0p40_coe1p00_L"};
        //String[] gauss_types={"_sd0p05_coe1p00_R","_sd0p20_coe1p00_R","_sd0p40_coe1p00_R"};
        //String[] gauss_types={"_sd1p00_coe2p85","_sd2p00_coe2p85","_sd4p00_coe2p85","_sd6p00_coe2p85"};
        //String[] gauss_types={"_sd1p00_coe1p42","_sd2p00_coe1p42","_sd4p00_coe1p42","_sd6p00_coe1p42"};
        //String[] gauss_types={"_sd2p00_coe1p42" };  //{"_sd2p00_coe1p42","_sd2p00_coe2p85","_sd2p00_coe4p27","_sd2p00_coe5p70","_sd2p00_coe7p12"};
        //String[] gauss_types={"_sd6p00_coe7p12" };  //{"_sd6p00_coe1p42","_sd6p00_coe2p85","_sd6p00_coe4p27","_sd6p00_coe5p70","_sd6p00_coe7p12"};
        String evNUM,evStamp;
        for (int eventsid=0;eventsid<events.length;eventsid++){
            int evID = events[eventsid];
            evNUM=(""+(evID/100.+0.001)).substring(2,4);
            evStamp="_ev"+evNUM;
            System.out.println("Event number ="+evNUM);
            
            stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_interpolated"+evStamp+".metaVHC");
            newstormFile = new java.io.File(output_path+"/precipitation_ovr_hills_ev"+evNUM+".dat");
            
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_meanrgts"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_meanfdts"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+gauss_types[eventsid]+"/precipitation_gaussian"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+gauss_types[eventsid]+"/precipitation_gaussianL"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+gauss_types[eventsid]+"/precipitation_gaussianR"+evStamp+".metaVHC");
            //new simulationsRep4(210,330,matDirs,matDirsPruned,magnitudes,metaModif,stormFile,0.0f);
            //new simulationsRep4(210,330,matDirs,matDirsPruned,magnitudes,metaModif,stormFile,50.0f);
            //new SimulationToFileFurey(310,378,matDirs,magnitudes,metaModif,stormFile,0.0f);
            new PrecipGridToHillslopes(44,111,matDirs,magnitudes,metaModif,stormFile,newstormFile);
            
            
        }
        
    }
    
}
