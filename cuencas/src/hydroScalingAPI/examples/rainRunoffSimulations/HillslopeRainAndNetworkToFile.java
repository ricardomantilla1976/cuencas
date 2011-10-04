/*
 * HillslopeRainAndNetworkToFile.java
 *
 * Created on December 21, 2007, 9:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class HillslopeRainAndNetworkToFile {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    /** Creates a new instance of HillslopeRainAndNetworkToFile */
    public HillslopeRainAndNetworkToFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        if (!storm.isCompleted()) return;
        
        thisHillsInfo.setStormManager(storm);
        
        String Directory="/Users/ricardo/simulationResults/GoodwinCreek/Rainfall/";
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));
        java.io.File theFile=new java.io.File(Directory+demName+"_"+x+"_"+y+"-"+storm.stormName()+".dat");
        System.out.println(theFile);
        
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.DataOutputStream newfile = new java.io.DataOutputStream(bufferout);
        
        newfile.writeInt(linksStructure.contactsArray.length);
        
        System.out.println("Writing Total Hillslope Areas");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(thisHillsInfo.Area(i));
        }
        
        System.out.println("Writing Link Magnitude");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(linksStructure.magnitudeArray[i]);
        }
        
        System.out.println("Writing Distance to Outlet");
        float[][] dToOutlet=linksStructure.getDistancesToOutlet();
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(dToOutlet[1][i]);
        }
        
        System.out.println("Writing Precipitations");
        
        int numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
        newfile.writeInt(numPeriods);
        
        java.util.Date startTime1=new java.util.Date();

        double[] myRain=new double[linksStructure.contactsArray.length];

        for (int k=0;k<numPeriods;k++) {
            //System.out.println("Initiating time step "+k);
            double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
            
            System.out.print(currTime+",");

            newfile.writeDouble(currTime);
            for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.writeDouble(thisHillsInfo.precipitation(i,currTime));
                myRain[i]=thisHillsInfo.precipitation(i,currTime);
            }
            System.out.print(new hydroScalingAPI.util.statistics.Stats(myRain).meanValue+",");
            System.out.println();
        }
        java.util.Date endTime1=new java.util.Date();
        System.out.println("Time Getting "+(linksStructure.contactsArray.length*numPeriods)+" records :"+((endTime1.getTime()-startTime1.getTime()))+" milliseconds");
        
        System.out.println("Done Writing Precipitations");

        newfile.close();
        bufferout.close();
        
        System.out.println("File Completed");
    }
    
    public static void main(String args[]) {
        //main0(args); //The mogollon test case
        //main1(args); //Whitewater 15-minute Rain
        //main2(args); //Nexrad Whitewater
        //main3(args); //Nexrad MPE Iowa River Basins
        //main4(args); //Nexrad MPE Iowa River Basins
        main5(args);  //Raingauge derived fields over Goodwin Creek by Peter Furey
    }
    
    public static void main0(String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/CuencasDataBases/Gila_River_DB/Rasters/Topography/1_ArcSec/mogollon.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Gila_River_DB/Rasters/Topography/1_ArcSec/mogollon.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;
            stormFile=new java.io.File("/CuencasDataBases/Gila_River_DB/Rasters/Hydrology/NexradPrecipitation/wholeSummer2003/nexrad_prec.metaVHC");

            new HillslopeRainAndNetworkToFile(282, 298,matDirs,magnitudes,metaModif,stormFile);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
    }
    
    public static void main1(String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;
            stormFile=new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Hydrology/storms/observed_events/EventLuc/prec.metaVHC");

        
            new HillslopeRainAndNetworkToFile(1063,496,matDirs,magnitudes,metaModif,stormFile);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
    }
    
    public static void main2(String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;
            stormFile=new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Hydrology/storms/observed_events/EventLuc/prec.metaVHC");

        
            new HillslopeRainAndNetworkToFile(1063,496,matDirs,magnitudes,metaModif,stormFile);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
    }
    
    public static void main3(String args[]) {
        
        
//x: 2817, y: 713 ; Basin Code 05454300 Clear Creek near Coralville, IA
//x: 2646, y: 762 ; Basin Code 05454220 Clear Creek near Oxford, IA
//x: 2949, y: 741 ; Basin Code 05454000 Rapid Creek near Iowa City, IA
//x: 2256, y: 876 ; Basin Code 05453100 Iowa River at Marengo, IA
//x: 1312, y: 1112 ; Basin Code 05451700 Timber Creek near Marshalltown, IA
//x: 2858, y: 742 ; Basin Code 05454090 Muddy Creek at Coralville, IA
//x: 2115, y: 801 ; Basin Code 05453000 Big Bear Creek at Ladora, IA
//x: 1765, y: 981 ; Basin Code 05451900 Richland Creek near Haven, IA
//x: 1871, y: 903 ; Basin Code 05452200 Walnut Creek near Hartwick, IA
//x: 2885, y: 690 ; Basin Code 05454500 Iowa River at Iowa City, IA
//x: 2796, y: 629 ; Basin Code 05455100 Old Mans Creek near Iowa City, IA
//x: 2958, y: 410 ; Basin Code 05455700 Iowa River near Lone Tree, IA
//x: 3186, y: 392 ; Basin Code 05465000 Cedar River near Conesville, IA
//x: 3316, y: 116 ; Basin Code 05465500 Iowa River at Wapello, IA
//x: 2734, y: 1069 ; Basin Code 05464500 Cedar River at Cedar Rapids, IA
//x: 1770, y: 1987 ; Basin Code 05458300 Cedar River at Waverly, IA
//x: 2676, y: 465 ; Basin Code 05455500 English River at Kalona, IA
//x: 2900, y: 768 ; Basin Code 05453520 Iowa River below Coralville Dam nr Coralville, IA
//x: 1245, y: 1181 ; Basin Code 05451500 Iowa River at Marshalltown, IA
//x: 951, y: 1479 ; Basin Code 05451210 South Fork Iowa River NE of New Providence, IA
//x: 3113, y: 705 ; Basin Code 05464942 Hoover Cr at Hoover Nat Hist Site, West Branch, IA
//x: 1978, y: 1403 ; Basin Code 05464220 Wolf Creek near Dysart, IA
//x: 1779, y: 1591 ; Basin Code 05463500 Black Hawk Creek at Hudson, IA
//x: 1932, y: 1695 ; Basin Code 05464000 Cedar River at Waterloo, IA
//x: 1590, y: 1789 ; Basin Code 05463000 Beaver Creek at New Hartford, IA
//x: 1682, y: 1858 ; Basin Code 05458900 West Fork Cedar River at Finchford, IA
//x: 1634, y: 1956 ; Basin Code 05462000 Shell Rock River at Shell Rock, IA
//x: 1775, y: 1879 ; Basin Code 05458500 Cedar River at Janesville, IA
//x: 903, y: 2499 ; Basin Code 05459500 Winnebago River at Mason City, IA
//x: 1526, y: 2376 ; Basin Code 05457700 Cedar River at Charles City, IA
//x: 1730, y: 2341 ; Basin Code 05458000 Little Cedar River near Ionia, IA
//x: 1164, y: 3066 ; Basin Code 05457000 Cedar River near Austin, MN

        

        
        try{
            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;
            stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/hydroNexrad.metaVHC");
            //stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/hydroNexrad.metaVHC");
        
            //new HillslopeRainAndNetworkToFile(2646, 762,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2949, 741,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2885, 690,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2817, 713,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1312, 1112,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2858, 742,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2115, 801,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1765, 981,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1871, 903,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2796, 629,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2958, 410,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2734, 1069,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1770, 1987,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2256, 876,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(3186, 392,matDirs,magnitudes,metaModif,stormFile);
            new HillslopeRainAndNetworkToFile(3316, 116,matDirs,magnitudes,metaModif,stormFile);
//
//
//            new HillslopeRainAndNetworkToFile(2676, 465,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(2900, 768,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1245, 1181,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(951, 1479,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(3113, 705,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1978, 1403,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1779, 1591,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1932, 1695,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1590, 1789,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1682, 1858,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1634, 1956,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1775, 1879,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(903, 2499,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1526, 2376,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1730, 2341,matDirs,magnitudes,metaModif,stormFile);
//            new HillslopeRainAndNetworkToFile(1164, 3066,matDirs,magnitudes,metaModif,stormFile);
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
    }

    public static void main4(String args[]) {

        try{
            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/Squaw Creek At Ames/NED_86024003.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/Squaw Creek At Ames/NED_86024003.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;
            stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/OverSquawCreek/hydroNexrad.metaVHC");

            new HillslopeRainAndNetworkToFile(1425, 349,matDirs,magnitudes,metaModif,stormFile);

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }

    }

    public static void main5(String args[]){

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;

            int iniEvent=3;
            int finEvent=3;

            int[] event_num=new int[finEvent-iniEvent+1];

            int k=0;
            for (int i=iniEvent;i<=finEvent;i++) {
                event_num[k]=i;
                k++;
            }

            for (int i : event_num) {

                String evNUM=(""+(i/1000.+0.0001)).substring(2,5);

                stormFile=new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/interpolated_events/05min_ts/events_singpeakB_rain01/event_"+evNUM+"/precipitation_interpolated_ev"+evNUM+".metaVHC");

                new HillslopeRainAndNetworkToFile(44, 111, matDirs, magnitudes, metaModif, stormFile);

            }

            System.exit(0);
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
    }

}
