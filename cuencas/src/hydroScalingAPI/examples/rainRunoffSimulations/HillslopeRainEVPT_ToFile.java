/*
 * HillslopeRainEVPT_ToFile.java
 *
 * Created on December 21, 2007, 9:52 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;
import java.io.IOException;
import java.text.DecimalFormat;

/**
 *
 * @author Ricardo Mantilla
 */
public class HillslopeRainEVPT_ToFile {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;

    /** Creates a new instance of HillslopeRainEVPT_ToFile */
    public HillslopeRainEVPT_ToFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, java.io.File EVPTFile, java.io.File PotEVPTFile, java.io.File OutputDir) throws java.io.IOException, VisADException {

        matDir = direcc;
        metaDatos = md;

        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDir, metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);

        System.out.println("Loading Storm ..." + stormFile.getAbsolutePath());

        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        storm = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);

        if (!storm.isCompleted()) {
            return;
        }
        thisHillsInfo.setStormManager(storm);
        System.out.println("Loading EVPT ...");

        hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager EVPT;
        EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(EVPTFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
        if (!EVPT.isCompleted()) {
            return;
        }
        thisHillsInfo.setEVPTManager(EVPT);
        System.out.println("Loading PotEVPT ...");
        hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager PotEVPT;
        PotEVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager(PotEVPTFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
        if (!PotEVPT.isCompleted()) {
            return;
        }
        thisHillsInfo.setPotEVPTManager(PotEVPT);
    DecimalFormat df3 = new DecimalFormat("###.###");
   
        System.out.println("Finsihed loading data");

        String demName = md.getLocationBinaryFile().getName().substring(0, md.getLocationBinaryFile().getName().lastIndexOf("."));
        System.out.println(OutputDir + demName + "_" + x + "_" + y + "-" + storm.stormName() + "_" + EVPT.EVPTName() + "rainfall.csv");
        java.io.File theFile = new java.io.File(OutputDir + "rainfall.csv");
        java.io.File theFile2 = new java.io.File(OutputDir +  "EVPT.csv");
        java.io.File theFile3 = new java.io.File(OutputDir +  "PotEVPT.csv");
        java.io.File theFile4 = new java.io.File(OutputDir +  "Summary.csv");
        System.out.println(theFile);




        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        java.io.FileOutputStream salida2 = new java.io.FileOutputStream(theFile2);
        java.io.BufferedOutputStream bufferout2 = new java.io.BufferedOutputStream(salida2);
        java.io.OutputStreamWriter newfile2 = new java.io.OutputStreamWriter(bufferout2);
        java.io.FileOutputStream salida3 = new java.io.FileOutputStream(theFile3);
        java.io.BufferedOutputStream bufferout3 = new java.io.BufferedOutputStream(salida3);
        java.io.OutputStreamWriter newfile3 = new java.io.OutputStreamWriter(bufferout3);
        java.io.FileOutputStream salida4 = new java.io.FileOutputStream(theFile4);
        java.io.BufferedOutputStream bufferout4 = new java.io.BufferedOutputStream(salida4);
        java.io.OutputStreamWriter newfile4 = new java.io.OutputStreamWriter(bufferout4);

        newfile.write(linksStructure.contactsArray.length + "\n");
        newfile2.write(linksStructure.contactsArray.length + "\n");
        newfile3.write(linksStructure.contactsArray.length + "\n");
        System.out.println("Writing Total Hillslope Areas");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(thisHillsInfo.Area(i)) + ",");
            newfile2.write(df3.format(thisHillsInfo.Area(i)) + ",");
            newfile3.write(df3.format(thisHillsInfo.Area(i)) + ",");
        }

        newfile.write("\n");
        newfile2.write("\n");
        newfile3.write("\n");


        System.out.println("Writing Link Magnitude");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(linksStructure.magnitudeArray[i]) + ",");
            newfile2.write(df3.format(linksStructure.magnitudeArray[i]) + ",");
            newfile3.write(df3.format(linksStructure.magnitudeArray[i]) + ",");
        }

        newfile.write("\n");
        newfile2.write("\n");
        newfile3.write("\n");

        System.out.println("Writing Distance to Outlet");
        float[][] dToOutlet = linksStructure.getDistancesToOutlet();
        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(dToOutlet[1][i]) + ",");
            newfile2.write(df3.format(dToOutlet[1][i]) + ",");
            newfile3.write(df3.format(dToOutlet[1][i]) + ",");
        }
        newfile.write("\n");
        newfile2.write("\n");
        newfile3.write("\n");
        System.out.println("Writing Precipitations");

        int numPeriods = (int) ((storm.stormFinalTimeInMinutes() - storm.stormInitialTimeInMinutes()) / storm.stormRecordResolutionInMinutes());
        newfile.write(numPeriods + "\n");
        newfile2.write(numPeriods + "\n");
        newfile3.write(numPeriods + "\n");

        java.util.Date startTime1 = new java.util.Date();

        double[] myRain = new double[linksStructure.contactsArray.length];
        double[] myEVPT = new double[linksStructure.contactsArray.length];
        double[] myPotEVPT = new double[linksStructure.contactsArray.length];
        double TOTALEVPT = 0;
        double TOTALPotEVPT = 0;
        double TOTALRain = 0;

        for (int k = 0; k < numPeriods; k++) {
            //System.out.println("Initiating time step "+k);
            double currTime = storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();

//            System.out.print(currTime + ",");

            newfile.write(currTime + ",");
            newfile2.write(currTime + ",");
            newfile3.write(currTime + ",");
            newfile4.write(currTime + ",");
            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
                newfile.write(df3.format(thisHillsInfo.precipitation(i, currTime)) + ",");
                newfile2.write(df3.format(thisHillsInfo.EVPT(i, currTime)) + ",");
                newfile3.write(df3.format(thisHillsInfo.PotEVPT(i, currTime)) + ",");
                myRain[i] = thisHillsInfo.precipitation(i, currTime);
                myEVPT[i] = thisHillsInfo.EVPT(i, currTime);
                myPotEVPT[i] = thisHillsInfo.PotEVPT(i, currTime);
            }
            
           TOTALEVPT = TOTALEVPT+new hydroScalingAPI.util.statistics.Stats(myEVPT).meanValue;
           TOTALPotEVPT = TOTALPotEVPT+new hydroScalingAPI.util.statistics.Stats(myPotEVPT).meanValue;
           TOTALRain = TOTALRain+new hydroScalingAPI.util.statistics.Stats(myRain).meanValue;
            
            newfile.write("\n");
            newfile2.write("\n");
            newfile3.write("\n");
           
            newfile4.write(df3.format(new hydroScalingAPI.util.statistics.Stats(myRain).meanValue) + ","+ df3.format(new hydroScalingAPI.util.statistics.Stats(myEVPT).meanValue) +","+ df3.format(new hydroScalingAPI.util.statistics.Stats(myPotEVPT).meanValue)+","+
                    df3.format(new hydroScalingAPI.util.statistics.Stats(myRain).standardDeviation) + ","+ df3.format(new hydroScalingAPI.util.statistics.Stats(myEVPT).standardDeviation) +","+ df3.format(new hydroScalingAPI.util.statistics.Stats(myPotEVPT).standardDeviation) +"\n");
//            System.out.print("myRain  " + new hydroScalingAPI.util.statistics.Stats(myRain).meanValue + ", "+ new hydroScalingAPI.util.statistics.Stats(myRain).maxValue +", "+ new hydroScalingAPI.util.statistics.Stats(myRain).total);
//            System.out.print("EVPT  " + new hydroScalingAPI.util.statistics.Stats(myEVPT).meanValue + ", "+ new hydroScalingAPI.util.statistics.Stats(myEVPT).maxValue +", "+ new hydroScalingAPI.util.statistics.Stats(myEVPT).total);
//            System.out.print("myPotEVPT  " + new hydroScalingAPI.util.statistics.Stats(myPotEVPT).meanValue + ", "+ new hydroScalingAPI.util.statistics.Stats(myPotEVPT).maxValue +", "+ new hydroScalingAPI.util.statistics.Stats(myPotEVPT).total);
//           
//            System.out.println();
        }
        
          //newfile4.write("0.0,"+TOTALRain + ","+ TOTALEVPT +","+ TOTALPotEVPT+"\n");
       
        System.out.print("TOTALRain  " + TOTALRain + ", TOTALEVPT"+ TOTALEVPT +", TOTALPotEVPT"+ TOTALPotEVPT);
           
       
        java.util.Date endTime1 = new java.util.Date();
        System.out.println("Time Getting " + (linksStructure.contactsArray.length * numPeriods) + " records :" + ((endTime1.getTime() - startTime1.getTime())) + " milliseconds");

        System.out.println("Done Writing Precipitations");

        newfile.close();
        bufferout.close();
        newfile2.close();
        bufferout2.close();
        newfile3.close();
        bufferout3.close();

        System.out.println("File Completed");
    }

    public static void main(String args[]) {
        //main0(args); //The mogollon test case
        //main1(args); //Whitewater 15-minute Rain
        //main2(args); //Nexrad Whitewater
        main3(args); //Nexrad MPE Iowa River Basins
        //main4(args); //Nexrad MPE Iowa River Basins
        //main5(args);  //Raingauge derived fields over Goodwin Creek by Peter Furey
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




        try {


            java.io.File theFile = new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));

            metaModif.setFormat("Byte");
            byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
            metaModif.setFormat("Integer");
            int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            String[] months = {"January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"};
            java.io.File stormFile;
            java.io.File EVPTFile;
            java.io.File PotEVPTFile;
            java.io.File outputDirectory;

            for (int iy = 2002; iy < 2010; iy++) {

             int xOut=2734;
             int yOut=1069; 
//                    int xOut = 2817;
//                    int yOut = 713;
                 
                    
                    stormFile = new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIVDaily/" + iy + "/PrecIowa.metaVHC");
                    System.out.println(stormFile.getAbsolutePath());
                    EVPTFile = new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/VHC/" + iy + "/ET/IowaET.metaVHC");
                    PotEVPTFile = new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/VHC/" + iy + "/PET/IowaPET.metaVHC");

                    outputDirectory = new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/Waterbalance/results/" + xOut + "_" + yOut + "/" + iy + "/");
                    outputDirectory.mkdirs();
                    //stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/hydroNexrad.metaVHC");

                    //new HillslopeRainEVPT_ToFile(2646, 762,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(2949, 741,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(2885, 690,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(2817, 713,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1312, 1112,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(2858, 742,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(2115, 801,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1765, 981,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1871, 903,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(2796, 629,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(2958, 410,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
              new HillslopeRainEVPT_ToFile(xOut, yOut, matDirs, magnitudes, metaModif, stormFile, EVPTFile, PotEVPTFile, outputDirectory);
//            new HillslopeRainEVPT_ToFile(1770, 1987,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(2256, 876,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(3186, 392,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(3316, 116,matDirs,magnitudes,metaModif,stormFile,EVPTFile,PotEVPTFile,outputDirectory);
//
//
//            new HillslopeRainEVPT_ToFile(2676, 465,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(2900, 768,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1245, 1181,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(951, 1479,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(3113, 705,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1978, 1403,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1779, 1591,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1932, 1695,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1590, 1789,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1682, 1858,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1634, 1956,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1775, 1879,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(903, 2499,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1526, 2376,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1730, 2341,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
//            new HillslopeRainEVPT_ToFile(1164, 3066,matDirs,magnitudes,metaModif,stormFile,EVPTFile);
                
            }

        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v) {
            System.out.print(v);
            System.exit(0);
        }

    }
}
