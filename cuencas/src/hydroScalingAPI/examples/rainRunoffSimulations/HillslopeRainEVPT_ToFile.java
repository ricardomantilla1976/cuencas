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
    public HillslopeRainEVPT_ToFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, java.io.File OutputDir) throws java.io.IOException, VisADException {

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

        //hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager EVPT;
        //EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(EVPTFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
        //if (!EVPT.isCompleted()) {
        //    return;
        //}
        //thisHillsInfo.setEVPTManager(EVPT);
        //System.out.println("Loading PotEVPT ...");
        //hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager PotEVPT;
        //PotEVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager(PotEVPTFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
        //if (!PotEVPT.isCompleted()) {
        //    return;
       // }
        //thisHillsInfo.setPotEVPTManager(PotEVPT);
    DecimalFormat df3 = new DecimalFormat("###.###");
   
        System.out.println("Finsihed loading data");

        String demName = md.getLocationBinaryFile().getName().substring(0, md.getLocationBinaryFile().getName().lastIndexOf("."));
        System.out.println(OutputDir + demName + "_" + x + "_" + y + "-" + storm.stormName() + "_"+"rainfall.csv");
        java.io.File theFile = new java.io.File(OutputDir + "rainfall.csv");
        //java.io.File theFile2 = new java.io.File(OutputDir +  "EVPT.csv");
        //java.io.File theFile3 = new java.io.File(OutputDir +  "PotEVPT.csv");
        java.io.File theFile4 = new java.io.File(OutputDir +  "Summary.csv");
        System.out.println(theFile);




        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
//        java.io.FileOutputStream salida2 = new java.io.FileOutputStream(theFile2);
//        java.io.BufferedOutputStream bufferout2 = new java.io.BufferedOutputStream(salida2);
//        java.io.OutputStreamWriter newfile2 = new java.io.OutputStreamWriter(bufferout2);
//        java.io.FileOutputStream salida3 = new java.io.FileOutputStream(theFile3);
//        java.io.BufferedOutputStream bufferout3 = new java.io.BufferedOutputStream(salida3);
//        java.io.OutputStreamWriter newfile3 = new java.io.OutputStreamWriter(bufferout3);
        java.io.FileOutputStream salida4 = new java.io.FileOutputStream(theFile4);
        java.io.BufferedOutputStream bufferout4 = new java.io.BufferedOutputStream(salida4);
        java.io.OutputStreamWriter newfile4 = new java.io.OutputStreamWriter(bufferout4);

        newfile.write(linksStructure.contactsArray.length + "\n");
        //newfile2.write(linksStructure.contactsArray.length + "\n");
        //newfile3.write(linksStructure.contactsArray.length + "\n");
        System.out.println("Writing Total Hillslope Areas");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(thisHillsInfo.Area(i)) + ",");
            //newfile2.write(df3.format(thisHillsInfo.Area(i)) + ",");
            //newfile3.write(df3.format(thisHillsInfo.Area(i)) + ",");
        }

        newfile.write("\n");
        //newfile2.write("\n");
        //newfile3.write("\n");


        System.out.println("Writing Link Magnitude");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(linksStructure.magnitudeArray[i]) + ",");
            //newfile2.write(df3.format(linksStructure.magnitudeArray[i]) + ",");
            //newfile3.write(df3.format(linksStructure.magnitudeArray[i]) + ",");
        }

        newfile.write("\n");
        //newfile2.write("\n");
        //newfile3.write("\n");

        System.out.println("Writing Distance to Outlet");
        float[][] dToOutlet = linksStructure.getDistancesToOutlet();
        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(dToOutlet[1][i]) + ",");
            //newfile2.write(df3.format(dToOutlet[1][i]) + ",");
            //newfile3.write(df3.format(dToOutlet[1][i]) + ",");
        }
        newfile.write("\n");
        //newfile2.write("\n");
        //newfile3.write("\n");
        System.out.println("Writing Precipitations");

        int numPeriods = (int) ((storm.stormFinalTimeInMinutes() - storm.stormInitialTimeInMinutes()) / storm.stormRecordResolutionInMinutes());
        newfile.write(numPeriods + "\n");
        //newfile2.write(numPeriods + "\n");
        //newfile3.write(numPeriods + "\n");

        java.util.Date startTime1 = new java.util.Date();

        double[] myRain = new double[linksStructure.contactsArray.length];
        //double[] myEVPT = new double[linksStructure.contactsArray.length];
        //double[] myPotEVPT = new double[linksStructure.contactsArray.length];
        //double TOTALEVPT = 0;
        //double TOTALPotEVPT = 0;
        double AVERain = 0;
        double MAXRain=0;
        for (int k = 0; k < numPeriods; k++) {
            //System.out.println("Initiating time step "+k);
            double currTime = storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();

//            System.out.print(currTime + ",");

            newfile.write(currTime + ",");
            //newfile2.write(currTime + ",");
            //newfile3.write(currTime + ",");
            newfile4.write(currTime + ",");
            AVERain=0;
            MAXRain=0;
            
            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
                newfile.write(df3.format(thisHillsInfo.precipitation(i, currTime)) + ",");
                AVERain=AVERain+thisHillsInfo.precipitation(i, currTime);
                if(thisHillsInfo.precipitation(i, currTime)>MAXRain)
                    MAXRain=thisHillsInfo.precipitation(i, currTime);
                //newfile2.write(df3.format(thisHillsInfo.EVPT(i, currTime)) + ",");
                //newfile3.write(df3.format(thisHillsInfo.PotEVPT(i, currTime)) + ",");
                //myEVPT[i] = thisHillsInfo.EVPT(i, currTime);
                //myPotEVPT[i] = thisHillsInfo.PotEVPT(i, currTime);
            }
            AVERain=AVERain/linksStructure.contactsArray.length;
            newfile.write(df3.format(AVERain)+","+ MAXRain + "\n");
                
           //TOTALEVPT = TOTALEVPT+new hydroScalingAPI.util.statistics.Stats(myEVPT).meanValue;
           //TOTALPotEVPT = TOTALPotEVPT+new hydroScalingAPI.util.statistics.Stats(myPotEVPT).meanValue;
            
            newfile.write("\n");
            //newfile2.write("\n");
            //newfile3.write("\n");
           
            //newfile4.write(df3.format(new hydroScalingAPI.util.statistics.Stats(myRain).meanValue) + ","+ df3.format(new hydroScalingAPI.util.statistics.Stats(myEVPT).meanValue) +","+ df3.format(new hydroScalingAPI.util.statistics.Stats(myPotEVPT).meanValue)+","+
            //df3.format(new hydroScalingAPI.util.statistics.Stats(myRain).standardDeviation) + ","+ df3.format(new hydroScalingAPI.util.statistics.Stats(myEVPT).standardDeviation) +","+ df3.format(new hydroScalingAPI.util.statistics.Stats(myPotEVPT).standardDeviation) +"\n");
//            System.out.print("myRain  " + new hydroScalingAPI.util.statistics.Stats(myRain).meanValue + ", "+ new hydroScalingAPI.util.statistics.Stats(myRain).maxValue +", "+ new hydroScalingAPI.util.statistics.Stats(myRain).total);
//            System.out.print("EVPT  " + new hydroScalingAPI.util.statistics.Stats(myEVPT).meanValue + ", "+ new hydroScalingAPI.util.statistics.Stats(myEVPT).maxValue +", "+ new hydroScalingAPI.util.statistics.Stats(myEVPT).total);
//            System.out.print("myPotEVPT  " + new hydroScalingAPI.util.statistics.Stats(myPotEVPT).meanValue + ", "+ new hydroScalingAPI.util.statistics.Stats(myPotEVPT).maxValue +", "+ new hydroScalingAPI.util.statistics.Stats(myPotEVPT).total);
//           
//            System.out.println();
        }
        
          //newfile4.write("0.0,"+TOTALRain + ","+ TOTALEVPT +","+ TOTALPotEVPT+"\n");
       
        System.out.print("TOTALRain  " + AVERain);
           
       
        java.util.Date endTime1 = new java.util.Date();
        System.out.println("Time Getting " + (linksStructure.contactsArray.length * numPeriods) + " records :" + ((endTime1.getTime() - startTime1.getTime())) + " milliseconds");

        System.out.println("Done Writing Precipitations");

        newfile.close();
        bufferout.close();
        //newfile2.close();
        //bufferout2.close();
        //newfile3.close();
        //bufferout3.close();

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


            java.io.File theFile = new java.io.File("/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));

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

                  int [] x = new int[36];
        int [] y= new int[36];
        int [] Code = new int[36];
        //x[1]=3124; y[1]=234; Code[1]=9999999; 
//        x[0]=2646; y[0]=762; Code[0]=5454220; 
//x[1]=2817; y[1]=713; Code[1]=5454300; 
//x[2]=2949; y[2]=741; Code[2]=5454000; 
//x[1]=2256; y[1]=876; Code[1]=5453100; 
//x[4]=1312; y[4]=1112; Code[4]=5451700; 
//x[5]=2858; y[5]=742; Code[5]=5454090; 
//x[6]=2115; y[6]=801; Code[6]=5453000; 
//x[7]=1871; y[7]=903; Code[7]=5452200; 
//x[8]=2885; y[8]=690; Code[8]=5454500; 
//x[9]=2796; y[9]=629; Code[9]=5455100; 
//x[10]=2958; y[10]=410; Code[10]=5455700; 
//x[11]=3186; y[11]=392; Code[11]=5465000; 
//x[12]=3316; y[12]=116; Code[12]=5465500; 
x[1]=2734; y[1]=1069 ; Code[12]=05464500;
//x[14]=1770; y[14]=1987; Code[14]=5458300; 
//x[15]=2676; y[15]=465; Code[15]=5455500; 
//x[16]=2900; y[16]=768; Code[16]=5453520; 
//x[17]=1765; y[17]=981; Code[17]=5451900; 
//x[18]=1245; y[18]=1181; Code[18]=5451500; 
//x[19]=951; y[19]=1479; Code[19]=5451210; 
//x[20]=3113; y[20]=705; Code[20]=5464942; 
//x[21]=1978; y[21]=1403; Code[21]=5464220; 
//x[22]=1779; y[22]=1591; Code[22]=5463500; 
//x[23]=1932; y[23]=1695; Code[23]=5464000; 
//x[24]=1798; y[24]=1750; Code[24]=5463050; 
//x[25]=1590; y[25]=1789; Code[25]=5463000; 
//x[26]=1682; y[26]=1858; Code[26]=5458900; 
//x[27]=1634; y[27]=1956; Code[27]=5462000; 
//x[28]=1775; y[28]=1879; Code[28]=5458500; 
//x[29]=903; y[29]=2499; Code[29]=5459500; 
//x[30]=1526; y[30]=2376; Code[30]=5457700; 
//x[31]=1730; y[31]=2341; Code[31]=5458000; 
//x[32]=1164; y[32]=3066; Code[32]=5457000; 
//x[33]=1741; y[33]=1831; Code[33]=5462000; 
//x[34]=3053; y[34]=2123; Code[34]=5412020; 
//x[31]=1730; y[31]=2341; Code[31]=5458000; 
//x[32]=1164; y[32]=3066; Code[32]=5457000; 
//x[33]=1741; y[33]=1831; Code[33]=5462000; 
//x[34]=3053; y[34]=2123; Code[34]=5412020; 
 //FINISHCode[34]=5412400;   
 //FINISHCode[34]=5411850;  
 //FINISHCode[34]=5412500;      
        for (int i=1;i<x.length-1;i++) {
            System.out.println(" x " + x[i] + "  y  " +y[i]);
        
            String Dir="/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/MatrizPintada/";
  
             for (int ped = 1; ped <= 1; ped++) {

            for (int ir = 1; ir <= 1; ir++) {

             int xOut=x[i];
             int yOut=y[i]; 
             int CodeStation=Code[i]; 
//                    int xOut = 2817;
//                    int yOut = 713;
                 
                      
                       String ens = "error";
                      if (ir == 0 || ir == 51) {
                        ens = "MPE_IOWA_ST4.";
                       } else if (ir > 0 && ir < 10) {
                       ens = "PED_0" + ir + "_MPE_IOWA_ST4.";
                        } else {
                         ens = "PED_" + ir + "_MPE_IOWA_ST4.";
                       }
                       String storm = "error";
              storm = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PEDVHC" +ped+"/" + ir + "/" + ens + "metaVHC";
                                                                     
                    stormFile = new java.io.File(storm);
                    System.out.println(stormFile.getAbsolutePath());
                
                    outputDirectory = new java.io.File("/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/RainAnalysis/PEDVHC" +ped+"/" + ir + "/" + xOut + "_" + yOut);
                    outputDirectory.mkdirs();
                    //stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/hydroNexrad.metaVHC");
             new HillslopeRainEVPT_ToFile(xOut, yOut, matDirs, magnitudes, metaModif, stormFile,outputDirectory);
            }
            }
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
