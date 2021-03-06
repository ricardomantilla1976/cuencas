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
 * This funcion was modified by luciana to include a different methods to estimate runoff
 * and superficial velocity in the hillslope
 * 02/12/2009 - it uses the SCSManager, that estimates Curve Number for each hillslope
 * The runoff production method includes
 * Hilltype
= 0 ; runoff = precipitation (wiht or without delay)
= 1 ; SCS Method - explicitly acount for soil moiusture condition
= 2 ; Mishra - Singh Method - modified SCS method that implicity accounts for for soil moiusture condition
 * Created on April, 2009
 */
package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;
import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

/**
 *
 * @author Luciana Cunha
 */
public class SimulationToFileSerialVersion extends java.lang.Object implements Runnable {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir, hortonOrders;
    int x, y, xH, yH, scale;
    int[][] magnitudes;
    float rainIntensity;
    float rainDuration;
    java.io.File stormFile;
    hydroScalingAPI.io.MetaRaster infiltMetaRaster;
    float infiltRate;
    int routingType, HillType, HillVelType;
    java.io.File outputDirectory;
    java.util.Hashtable routingParams;
    int[] usConnections;
    int basinOrder;
    float[] corrections;
    private java.util.Calendar zeroSimulationTime;
    // Add for the SCS
    int greenroof;
    java.io.File GreenFile;
    java.io.File LandUseFile;
    java.io.File SoilFile;
    float LandUseFileFlag;
    float SoilFileFlag;
    float IniCondition;
    int writeorder;
    java.io.File SoilHydFile;
     java.io.File SoilSwa150File;

    /** Creates new simulationsRep3 */
    public SimulationToFileSerialVersion(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, java.io.File outputDirectory, float LandUseFileFlag, float SoilFileFlag,java.io.File SoilSwa150File, int writeorder, java.util.Hashtable routingParams) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, md, rainIntensity, rainDuration, null, null, infiltRate, 0, null, outputDirectory, LandUseFileFlag, null, SoilFileFlag, null,SoilSwa150File, writeorder, routingParams);
    }

    public SimulationToFileSerialVersion(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, java.io.File outputDirectory, float LandUseFileFlag, float SoilFileFlag,java.io.File SoilSwa150File, int writeorder, java.util.Hashtable routingParams) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, md, 0.0f, 0.0f, stormFile, null, infiltRate, 0, null, outputDirectory, LandUseFileFlag, null, SoilFileFlag, null,SoilSwa150File, writeorder, routingParams);
    }

    public SimulationToFileSerialVersion(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, java.io.File outputDirectory, java.io.File LandUseFile, java.io.File SoilFile,java.io.File SoilSwa150File,  int writeorder,java.util.Hashtable routingParams) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, md, 0.0f, 0.0f, stormFile, null, infiltRate, 0, null, outputDirectory, 0.0f, LandUseFile, 0.0f, SoilFile,SoilSwa150File, writeorder, routingParams);
    }

    public SimulationToFileSerialVersion(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, int greenroof, java.io.File GreenFile, java.io.File outputDirectory, java.io.File LandUseFile, java.io.File SoilFile,java.io.File SoilSwa150File, int writeorder, java.util.Hashtable routingParams) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, md, 0.0f, 0.0f, stormFile, null, infiltRate, greenroof, GreenFile, outputDirectory, 0.0f, LandUseFile, 0.0f, SoilFile,SoilSwa150File, writeorder, routingParams);
    }

    public SimulationToFileSerialVersion(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, java.io.File outputDirectory, java.io.File LandUseFile, java.io.File SoilFile,java.io.File SoilSwa150File, int writeorder, java.util.Hashtable routingParams) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, md, rainIntensity, rainDuration, null, null, infiltRate, 0, null, outputDirectory, 0.0f, LandUseFile, 0.0f, SoilFile,SoilSwa150File, writeorder, routingParams);
    }

    public SimulationToFileSerialVersion(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int greenroof, java.io.File GreenFile, java.io.File outputDirectory, float LandUseFileFlag, java.io.File LandUseFile, float SoilFileFlag, java.io.File SoilFile,java.io.File SoilSwa150File, int writeorder, java.util.Hashtable routingParams) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, md, 0.0f, 0.0f, stormFile, infiltMetaRaster, 0.0f, greenroof, GreenFile, outputDirectory, LandUseFileFlag, null, SoilFileFlag, null,SoilSwa150File, writeorder, routingParams);
    }

    public SimulationToFileSerialVersion(int xx, int yy, byte[][] direcc, int[][] magnitudesOR,
            hydroScalingAPI.io.MetaRaster md, float rainIntensityOR, float rainDurationOR,
            java.io.File stormFileOR, hydroScalingAPI.io.MetaRaster infiltMetaRasterOR,
            float infiltRateOR, int GreenRoofOptionOR, java.io.File GreenFileOR,
            java.io.File outputDirectoryOR,
            float LandUseFileFlagOR, java.io.File LandUseFileOR,
            float SoilFileFlagOR, java.io.File SoilFileOR,
            java.io.File SoilSwa150FileOR,
            int WriteOrderOR,
            java.util.Hashtable rP)
            throws java.io.IOException, VisADException {


        matDir = direcc;
        metaDatos = md;
        x = xx;
        y = yy;
        magnitudes = magnitudesOR;
        rainIntensity = rainIntensityOR;
        rainDuration = rainDurationOR;
        stormFile = stormFileOR;
        infiltMetaRaster = infiltMetaRasterOR;
        infiltRate = infiltRateOR;
        SoilFile = SoilFileOR;
        SoilFileFlag = SoilFileFlagOR;
        LandUseFile = LandUseFileOR;
        LandUseFileFlag = LandUseFileFlagOR;
        outputDirectory = outputDirectoryOR;
        routingParams = rP;
        greenroof = GreenRoofOptionOR;
        GreenFile = GreenFileOR;
        writeorder = WriteOrderOR;
    }

    public void executeSimulation() throws java.io.IOException, VisADException {

        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDir, metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        java.util.Date interTime = new java.util.Date();
        java.util.Date startTime = new java.util.Date();
        float widthCoeff = 0.0f;
        float widthExponent = 0.0f;
        float widthStdDev = 0.0f;
        float chezyCoeff = 0.0f;
        float chezyExponent = 0.0f;
        float lam1 = 0.0f;
        float lam2 = 0.0f;
        float v_o = 0.0f;
        float IniHSatPorc = 0.0f;
        float IniUnSatPorc = 0.0f;
        float BaseFlowCoef = 0.0f;
        float BaseFlowExp = 0.0f;
        if (routingParams.get("widthCoeff") != null) {
            widthCoeff = ((Float) routingParams.get("widthCoeff")).floatValue();
        }
        if (routingParams.get("widthExponent") != null) {
            widthExponent = ((Float) routingParams.get("widthExponent")).floatValue();
        }
        if (routingParams.get("widthStdDev") != null) {
            widthStdDev = ((Float) routingParams.get("widthStdDev")).floatValue();
        }

        if (routingParams.get("chezyCoeff") != null) {
            chezyCoeff = ((Float) routingParams.get("chezyCoeff")).floatValue();
        }
        if (routingParams.get("chezyExponent") != null) {
            chezyExponent = ((Float) routingParams.get("chezyExponent")).floatValue();
        }

        thisNetworkGeom.setWidthsHG(widthCoeff, widthExponent, widthStdDev);
        thisNetworkGeom.setCheziHG(chezyCoeff, chezyExponent);

        if (routingParams.get("lambda1") != null) {
            lam1 = ((Float) routingParams.get("lambda1")).floatValue();
        }
        if (routingParams.get("lambda2") != null) {
            lam2 = ((Float) routingParams.get("lambda2")).floatValue();
        }

        if (routingParams.get("v_o") != null) {
            v_o = ((Float) routingParams.get("v_o")).floatValue();
        }

        if (routingParams.get("PorcHSaturated") != null) {
            IniHSatPorc = ((Float) routingParams.get("PorcHSaturated")).floatValue();
        }
        if (routingParams.get("PorcPhiUnsat") != null) {
            IniUnSatPorc = ((Float) routingParams.get("PorcPhiUnsat")).floatValue();
        }

        if (routingParams.get("BaseFlowCoef") != null) {
            BaseFlowCoef = ((Float) routingParams.get("BaseFlowCoef")).floatValue();
        }
        if (routingParams.get("BaseFlowExp") != null) {
            BaseFlowExp = ((Float) routingParams.get("BaseFlowExp")).floatValue();
        }

        float P5 = 0.0f;
        if (routingParams.get("P5Condition") != null) {
            ((Float) routingParams.get("P5Condition")).floatValue();
        }
        IniCondition = 0.0f;
        if (routingParams.get("InitialCondition") != null) {
            IniCondition = ((Float) routingParams.get("InitialCondition")).floatValue();
        } // % of the saturated soil
        float lambdaSCS = ((Float) routingParams.get("lambdaSCSMethod")).floatValue();


        //float rt=((Float)routingParams.get("RoutingT")).floatValue(); // check NetworkEquationsLuciana.java for definitions
        routingType = 5;
        if (routingParams.get("RoutingT") != null) {
            routingType = (int) ((Float) routingParams.get("RoutingT")).floatValue();
        }

        HillType = 4;
        if (routingParams.get("HillT") != null) {
            HillType = (int) ((Float) routingParams.get("HillT")).floatValue();
        }
        HillVelType = 3;
        if (routingParams.get("HillVelocityT") != null) {
            HillVelType = (int) ((Float) routingParams.get("HillVelocityT")).floatValue();
        }

          int HillShapeFlag = 0;
        if (routingParams.get("hillshapeparamflag") != null) {
            HillShapeFlag = (int) ((Float) routingParams.get("hillshapeparamflag")).floatValue();
        }


        float vconst = ((Float) routingParams.get("Vconst")).floatValue();
        float vsub = ((Float) routingParams.get("vssub")).floatValue();
        float vrun = ((Float) routingParams.get("vrunoff")).floatValue();
        float SM = ((Float) routingParams.get("SoilMoisture")).floatValue();
        if (routingType == 2) {
            v_o = vconst;
        }
        float v0std = 0.f;
        if (routingParams.get("Vostd") != null) {
            v0std = ((Float) routingParams.get("Vostd")).floatValue();
        }
        thisNetworkGeom.setVqParams(v_o, v0std, lam1, lam2);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        /////////////SET LAND USE INFORMATION AND GENERATE COLOR CODED WIDTH FUNCTION////////////////////////////
        System.out.println("Loading lAND USE ...");
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager LandUse;
        LandUse = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager(LandUseFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
        thisHillsInfo.setLandUseManager(LandUse);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
        java.io.File DEMFile = metaDatos.getLocationMeta();
        float hillshape = 0.f;
        if (routingParams.get("hillshapeparamflag") != null) {
            hillshape = ((Float) routingParams.get("hillshapeparamflag")).floatValue();
        }
        String SoilHydData = "Error";
        SoilHydFile= new java.io.File(SoilHydData);
        SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile,SoilHydFile,SoilSwa150File, myCuenca, linksStructure, metaDatos, matDir, magnitudes, hillshape);
        thisHillsInfo.setSCSManager(SCSObj);
        java.text.DecimalFormat fourPlaces = new java.text.DecimalFormat("0.0000");


        //////////////////////////////////////
        System.out.println("Start to run Width function");
        java.io.File theFile;

        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "LinksInfo" + ".csv");

        System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        int nLi = linksStructure.connectionsArray.length;
        double[] Area_length = new double[linksStructure.contactsArray.length];
        System.out.println("Open the file");
        newfile.write("1 ");
        double RC = -9.9;
        double max_rel = 0;
        float[][] Vo = new float[1][linksStructure.contactsArray.length];
        float[][] Go = new float[1][linksStructure.contactsArray.length];
        // Atributing volume of reservation
        System.out.println("Volume");

        newfile.write("link,HillRelief,Area,Length,UstArea,Slope,SCS_S2,K_NRCS,Vh,param_a,param_b,param_c,param_d ");
        for (int i = 0; i < nLi; i++) {
            //if(thisNetworkGeom.linkOrder(i) > 1){
            newfile.write(i + ",");
            newfile.write(thisHillsInfo.HillRelief(i) + ",");
            newfile.write(thisHillsInfo.Area(i) + ",");
            newfile.write(thisNetworkGeom.Length(i) + ",");
            newfile.write(thisNetworkGeom.upStreamArea(i) + ",");
            newfile.write(thisHillsInfo.getHillslope(i) + ",");
            newfile.write(thisHillsInfo.SCS_S2(i) + ",");
            newfile.write(thisHillsInfo.Hill_K_NRCS(i) + ",");
            double tempVh = thisHillsInfo.Hill_K_NRCS(i) * (Math.pow(thisHillsInfo.getHillslope(i) * 100, 2)) * 0.3048;
            newfile.write(tempVh + ",");
            newfile.write(thisHillsInfo.getArea_ReliefParam(i, 0) + ",");
            newfile.write(thisHillsInfo.getArea_ReliefParam(i, 1) + ",");
            newfile.write(thisHillsInfo.getArea_ReliefParam(i, 2) + ",");
            newfile.write(thisHillsInfo.getArea_ReliefParam(i, 3) + ",");

//
//
//            newfile.write(thisNetworkGeom.Slope(i) + ",");
//            newfile.write(thisNetworkGeom.Length(i) + ",");
//            newfile.write(thisHillsInfo.Area(i) + ",");
//            newfile.write(thisNetworkGeom.upStreamTotalLength(i) + " ");
//
//            double slope = Math.max(thisNetworkGeom.Slope(i), 0.005);
//            double vr = (thisHillsInfo.Hill_K_NRCS(i)) * Math.pow(slope, 0.5) * 100 * 0.3048;
//            newfile.write(vr + " ");
//            double dist = thisHillsInfo.Area(i) * 1000000 * 0.5 / (thisNetworkGeom.Length(i)); //(m)
//            newfile.write(dist + " ");
//            newfile.write(dist / vr + " ");
//            newfile.write(thisHillsInfo.LandUse(i) + " ");
//            newfile.write(thisHillsInfo.SCS_CN2(i) + " ");
//            newfile.write(thisHillsInfo.SCS_IA2(i) + " ");
//            newfile.write(thisHillsInfo.SCS_S2(i) + " ");

//               newfile.write(thisHillsInfo.SCS_S2(i)+" ");
//               newfile.write(thisHillsInfo.SCS_IA3(i)+" ");
//               newfile.write(thisHillsInfo.SCS_S3(i)+" ");
            newfile.write("\n");
            Area_length[i] = thisHillsInfo.Area(i) * 1000000 / thisNetworkGeom.Length(i);
            max_rel = Math.max(max_rel, Area_length[i]);

            //}
        }

        //_stop calculate maximum recession time

        double tim_run = max_rel / vrun; //hour
        double tim_run_sub = max_rel / (5 * vsub); //at least 20% of sub flow contribute to the discharge
        System.out.println("Termina escritura de Links info");

        newfile.close();
        bufferout.close();
        //////////////////////////////////////////

        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;

        if (stormFile == null) {
            storm = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure, rainIntensity, rainDuration);
        } else {
            storm = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
        }

        if (!storm.isCompleted()) {
            return;
        }

        thisHillsInfo.setStormManager(storm);

        // WRITE PRECIPITATION FILE
        if (stormFile == null) {
            theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "precipitation_zero" + ".csv");
        } else {
            theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "precipitation_" + stormFile.getName() + ".csv");
        }

        String PrecipFile = theFile.getAbsolutePath();

        System.out.println(theFile);

        //int writeorder=2;

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);

        newfile.write("1,");

        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                newfile.write(linksStructure.completeStreamLinksArray[i] + ",");
            }
        }

        newfile.write("\n");
        newfile.write("2,");

        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                newfile.write(fourPlaces.format(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])) + ",");
            }
        }
        newfile.write("\n");

        int numPeriods = 1;

        if (stormFile == null) {
            numPeriods = (int) ((storm.stormFinalTimeInMinutes() - storm.stormInitialTimeInMinutes()) / rainDuration);
        } else {
            //numPeriods = (int) ((storm.stormFinalTimeInMinutes() - storm.stormInitialTimeInMinutes()) / storm.stormRecordResolutionInMinutes());
            numPeriods = Math.round(((float) storm.stormFinalTimeInMinutes() - (float) storm.stormInitialTimeInMinutes()) / (float) storm.stormRecordResolutionInMinutes());
        }

        for (int k = 0; k < numPeriods; k++) {

            double currTime = storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();

            java.util.Calendar thisDate1 = java.util.Calendar.getInstance();
            thisDate1.setTimeInMillis((long) (currTime * 60. * 1000.0));

            newfile.write(currTime + ",");
            for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                    newfile.write(fourPlaces.format((thisHillsInfo.precipitation(linksStructure.completeStreamLinksArray[i], currTime))) + ",");
                }

            }
            newfile.write("\n");
        }

        System.out.println("Termina escritura de precipitation");

        newfile.close();
        bufferout.close();

        hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infilMan = new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure, 0.0f);

        if (infiltMetaRaster == null) {
            infilMan = new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure, infiltRate);
        } else {
            infilMan = new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(myCuenca, linksStructure, infiltMetaRaster, matDir, magnitudes);
        }

        thisHillsInfo.setInfManager(infilMan);

        /*
        Escribo en un theFile lo siguiente:
        Numero de links
        Numero de links Completos
        lista de Links Completos
        Area aguas arriba de los Links Completos
        Orden de los Links Completos
        maximos de la WF para los links completos
        Longitud simulacion
        Resulatdos

         */

        String demName = metaDatos.getLocationBinaryFile().getName().substring(0, metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        String routingString = "";
        switch (routingType) {
            case 0:
                routingString = "VC";
                break;
            case 1:
                routingString = "CC";
                break;
            case 2:
                routingString = "CV";
                break;
            case 3:
                routingString = "CM";
                break;
            case 4:
                routingString = "VM";
                break;
            case 5:
                routingString = "GK";
                break;
        }


        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "-SN.wfs.csv");
        System.out.println("Writing Width Functions - " + theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);

        double[][] wfs = linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray, 0);

        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1) {
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) + "," + linksStructure.completeStreamLinksArray[i] + ",");
                for (int j = 0; j < wfs[i].length; j++) {
                    newfile.write(wfs[i][j] + ",");
                }
                newfile.write("\n");
            }
        }

        newfile.close();
        bufferout.close();


        if (infiltMetaRaster == null) {
            theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "_" + x + "_" + y + "-" + stormFile.getName() + "-IR_" + infiltRate + "-Routing_" + routingString + "_params_" + lam1 + "_" + lam2 + "_" + v_o + ".csv");//theFile=new java.io.File(Directory+demName+"_"+x+"_"+y++"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+widthCoeff+"_"+widthExponent+"_"+widthStdDev+"_"+chezyCoeff+"_"+chezyExponent+".dat");
        } else {
            theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "_" + x + "_" + y + "-" + stormFile.getName() + "-IR_" + infiltMetaRaster.getLocationMeta().getName().substring(0, infiltMetaRaster.getLocationMeta().getName().lastIndexOf(".metaVHC")) + "-Routing_" + routingString + "_params_" + lam1 + "_" + lam2 + ".csv");
        }
        System.out.println(theFile);
//
//        if (theFile.exists()) {
//            //ATTENTION
//            //The followng print statement announces the completion of the program.
//            //DO NOT modify!  It tells the queue manager that the process can be
//            //safely killed.
//            System.out.println("Termina escritura de Resultados" + x + "," + y);
//            //writeReportFile(outputDirectory.getAbsolutePath(),x,y);
//            return;
//        }
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);

        java.io.File theFile1 = new java.io.File(theFile.getAbsolutePath() + ".Outlet.csv");
        java.io.FileOutputStream salida1 = new java.io.FileOutputStream(theFile1);
        java.io.BufferedOutputStream bufferout1 = new java.io.BufferedOutputStream(salida1);
        java.io.OutputStreamWriter newfile1 = new java.io.OutputStreamWriter(bufferout1);

        java.io.File theFile2 = new java.io.File(theFile.getAbsolutePath() + ".Storage.csv");
        java.io.FileOutputStream salida2 = new java.io.FileOutputStream(theFile2);
        java.io.BufferedOutputStream bufferout2 = new java.io.BufferedOutputStream(salida2);
        java.io.OutputStreamWriter newfile2 = new java.io.OutputStreamWriter(bufferout2);

        java.io.File theFilerc = new java.io.File(theFile.getAbsolutePath() + ".runoff.csv");
        java.io.FileOutputStream salidarc = new java.io.FileOutputStream(theFilerc);
        java.io.BufferedOutputStream bufferoutrc = new java.io.BufferedOutputStream(salidarc);
        java.io.OutputStreamWriter newfilerc = new java.io.OutputStreamWriter(bufferoutrc);


        java.io.File theFile_aux = new java.io.File(theFile.getAbsolutePath() + ".auxiliar.csv");
        java.io.FileOutputStream salida_aux = new java.io.FileOutputStream(theFile_aux);
        java.io.BufferedOutputStream bufferout_aux = new java.io.BufferedOutputStream(salida_aux);
        java.io.OutputStreamWriter newfile_aux = new java.io.OutputStreamWriter(bufferout_aux);

        DecimalFormat df = new DecimalFormat("###");
        DecimalFormat df1 = new DecimalFormat("###.#");
        DecimalFormat df2 = new DecimalFormat("###.##");
        DecimalFormat df3 = new DecimalFormat("###.###");
        DecimalFormat df10 = new DecimalFormat("###.##########");

        //newfile.write("Information on order Streams\n");
        //newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("Link #,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df.format(i) + ",");
        }

        newfile.write("\n");
        newfile.write("Horton Order,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df.format(thisNetworkGeom.linkOrder(i)) + ",");
        }

        newfile.write("\n");
        newfile.write("Upstream Area [km^2],");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(thisNetworkGeom.upStreamArea(i)) + ",");
        }


        newfile.write("\n");
        newfile.write("Link Outlet ID,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df.format(i) + ",");
        }
        //System.out.println("Writing distance to the outlet 1");
        newfile.write("\n");
        newfile.write("Distance to outlet,");
        float[][] bigDtoO = linksStructure.getDistancesToOutlet();
        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df2.format(bigDtoO[1][i]) + ",");
        }
        //System.out.println("Writing driver length");
        newfile.write("\n");
        newfile.write("River length,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df2.format(thisNetworkGeom.Length(i)) + ",");
        }
        //newfile.write("\n");

        //newfile.write("\n\n\n");
        //newfile.write("Results of flow simulations in your basin");

//        newfile.write("\n");
//        newfile.write("Link,");
//
//        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//            newfile.write(df.format(i) + ",");
//        }




        interTime = new java.util.Date();
        System.out.println("Finish file," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
        java.io.File[] filesToAdd = new java.io.File[0];
        //hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_SerialVersion thisBasinEqSys = new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_SerialVersion(linksStructure, thisHillsInfo, thisNetworkGeom, -1, filesToAdd, routingParams);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_AllMethodSeriial thisBasinEqSys = new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_AllMethodSeriial(linksStructure, thisHillsInfo, thisNetworkGeom, -1, filesToAdd, routingParams);

        //      hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay(linksStructure,thisHillsInfo,thisNetworkGeom,routingType);
        int nstates = linksStructure.contactsArray.length * 2;

        int NLI = linksStructure.contactsArray.length;
        double[] initialCondition;

        initialCondition = new double[NLI * 14];
        System.out.println("Start Initial condition");

        for (int i = 0; i < NLI; i++) {
            initialCondition[i] = BaseFlowCoef * Math.pow(thisNetworkGeom.upStreamArea(i), BaseFlowExp);
            //initialCondition[i] = 1.0;
            initialCondition[i + 1 * NLI] = 0.0;
            initialCondition[i + 2 * NLI] = IniHSatPorc * 1000 * thisHillsInfo.HillRelief(i);
            initialCondition[i + 3 * NLI] = IniUnSatPorc;
            initialCondition[i + 4 * NLI] = 0.0;
            initialCondition[i + 5 * NLI] = 0.0;
            initialCondition[i + 6 * NLI] = 0.0;
            initialCondition[i + 7 * NLI] = 0.0;
            initialCondition[i + 8 * NLI] = 0.0;
            initialCondition[i + 9 * NLI] = 0.0;
            initialCondition[i + 10 * NLI] = 0.0;
            initialCondition[i + 11 * NLI] = 0.0;
            initialCondition[i + 12 * NLI] = 0.0;
            initialCondition[i + 13 * NLI] = 0.0;

        }


        interTime = new java.util.Date();
        System.out.println("Create diff equation,," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        System.out.println("Number of Links on this simulation: " + NLI);
        System.out.println("Inicia simulacion RKF");


        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining = new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys, 1e-3, 10 / 60.);



        //int numPeriods = Math.round(((float) storm.stormFinalTimeInMinutes() - (float) storm.stormInitialTimeInMinutes()) / (float) storm.stormRecordResolutionInMinutes());

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (storm.stormInitialTimeInMinutes() * 60. * 1000.0));
        System.out.println(thisDate.getTime());


        double outputTimeStep = storm.stormRecordResolutionInMinutes();
        System.out.println("storm.stormRecordResolutionInMinutes()" + storm.stormRecordResolutionInMinutes());
        // double extraSimTime=240D*Math.pow(1.5D,(basinOrder-1));
        //double extraSimTime = 300D * Math.pow(1.5D, (9 - 1));
        //double extraSimTime=50D*Math.pow(1.5D,(9-1));
        double extraSimTime = 4 * 24 * 60; // 2 days
        // original - double extraSimTime=240D*Math.pow(2.0D,(basinOrder-1));
        System.out.println("numPeriods" + numPeriods);

        newfile1.write("link" + ",");

        //int writeorder = linksStructure.basinOrder - 2;
        //writeorder = Math.max(1, writeorder);

        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                newfile1.write(linksStructure.completeStreamLinksArray[i] + ",");
            }
        }
        newfile1.write("\n");

        newfile1.write("order" + ",");


        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                newfile1.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) + ",");
            }
        }
        newfile1.write("\n");

        newfile1.write("Area" + ",");


        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                newfile1.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i]) + ",");
            }
        }
        newfile1.write("\n");
        newfile1.write("AreaHill" + ",");

        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                newfile1.write(thisHillsInfo.Area(linksStructure.completeStreamLinksArray[i]) + ",");
            }
        }

        newfile1.write("\n");
        newfile1.write("HillRelief" + ",");

        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                newfile1.write(thisHillsInfo.HillRelief(linksStructure.completeStreamLinksArray[i]) + ",");
            }
        }

       newfile1.write("\n");
        newfile1.write("HillLength" + ",");

        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                newfile1.write(thisNetworkGeom.Length(linksStructure.completeStreamLinksArray[i])+ ",");
            }
        }

        newfile1.write("\n");
        newfile1.write("SCS" + ",");

        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= writeorder) {
                newfile1.write(thisHillsInfo.SCS_S2(linksStructure.completeStreamLinksArray[i]) + ",");
            }
        }




        newfile1.write("\n");

//        newfile1.write("TimeStep:" + outputTimeStep + "\n");
//        newfile1.write("Time (minutes), Discharge [m3/s] \n");
//        newfile3.write("TimeStep:" + outputTimeStep + "\n");
//        newfile3.write("Time (minutes), Surface Storage [mm], Soil Storage [mm] \n");

        interTime = new java.util.Date();
        System.out.println("Start loop," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        for (int k = 0; k < numPeriods; k++) {
            System.out.println("Period " + (k + 1) + " of " + numPeriods);

            rainRunoffRaining.jumpsRunCompleteToAsciiFileSCSSerial(storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes(), storm.stormInitialTimeInMinutes() + (k + 1) * storm.stormRecordResolutionInMinutes(), outputTimeStep, initialCondition, newfile, linksStructure, thisNetworkGeom, newfile1, newfile2, newfilerc, newfile_aux, writeorder);

            initialCondition = rainRunoffRaining.finalCond;
            rainRunoffRaining.setBasicTimeStep(10 / 60.);
        }


        interTime = new java.util.Date();
        System.out.println("Running Time:" + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
        System.out.println("Finish first part," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
        //outputTimeStep=5*Math.pow(2.0D,(basinOrder-1));
        outputTimeStep = 5 * Math.pow(1.5D, (basinOrder - 1));
        if (outputTimeStep > 60) {
            outputTimeStep = 60;
        }
        rainRunoffRaining.jumpsRunCompleteToAsciiFileSCSSerial(storm.stormInitialTimeInMinutes() + numPeriods * storm.stormRecordResolutionInMinutes(), (storm.stormInitialTimeInMinutes() + (numPeriods + 1) * storm.stormRecordResolutionInMinutes()) + extraSimTime, outputTimeStep, initialCondition, newfile, linksStructure, thisNetworkGeom, newfile1, newfile2, newfilerc, newfile_aux, writeorder);







        System.out.println("Termina simulacion RKF");
        java.util.Date endTime = new java.util.Date();
        System.out.println("End Time:" + endTime.toString());
        System.out.println("Running Time:" + (.001 * (endTime.getTime() - startTime.getTime())) + " seconds");
        newfile1.close();
        bufferout1.close();
        newfile2.close();
        bufferout2.close();
        newfilerc.close();
        bufferoutrc.close();
        newfile_aux.close();
        bufferout_aux.close();

        //   double[] maximumsAchieved=rainRunoffRaining.getMaximumAchieved();

        //newfile.write("\n");
        //newfile.write("\n");
        //newfile.write("Maximum Discharge [m^3/s],");
        //for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
        //        newfile.write(maximumsAchieved[linksStructure.completeStreamLinksArray[i]]+",");
        // }


        //  newfile.close();
        //  bufferout.close();

        System.out.println("Termina escritura de Resultados");


        //  Simulation log file
//        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "logfile.txt");
//        System.out.println("Writing the log file - " + theFile);

//        salida = new java.io.FileOutputStream(theFile);
//        bufferout = new java.io.BufferedOutputStream(salida);
//        newfile = new java.io.OutputStreamWriter(bufferout);
//        newfile.write(demName + " demName \n");
//
//        newfile.write(outputDirectory.getAbsolutePath() +  " Output_directory \n");
//        newfile.write(DischargeFile +  " Discharge_File \n");
//        newfile.write(RunoffFile +  " Precipitation_File \n");
//        newfile.write(RunoffFile +  " Runoff_File \n");
//        newfile.write(SubFile +  " Sub_File \n");
//        newfile.write(PrecFile +  " AcumPrec_File \n");
//        newfile.write(S0File +  " S0_File \n");
//        newfile.write(S1File +  " S1_File \n");
//        if(stormFile == null) {newfile.write(rainIntensity + " rain_insensity \n");
//         newfile.write(rainDuration + " rain_duration \n");
//         }
//         else {newfile.write(stormFile.getAbsolutePath() + " Precipitation_File \n");}
//         if(infiltMetaRaster == null) {newfile.write(infiltRate + " infiltRate \n");}
//         else {newfile.write(infiltMetaRaster.getLocationMeta().getAbsolutePath() + " infiltrationfile \n");}
//
//         newfile.write(routingType + " routing type \n");
//         if(routingType==2) newfile.write(routingType + " routing type \n");
//         if (routingType==5)
//         {
//         newfile.write(lam1 + " lambda1 \n");
//         newfile.write(lam2 + " lambda2 \n");
//         newfile.write(v_o + " vo \n");
//         }
//
//         newfile.write(HillType + " Hill type \n");
//        newfile.close();
//        bufferout.close();

        //newfile.write("\n");
        newfile.write("\n");
        newfile.write("Maximum Discharge [m^3/s],");

        double[] maximumsAchieved = rainRunoffRaining.getMaximumAchieved();
        double[] timeToMaximumsAchieved = rainRunoffRaining.getTimeToMaximumAchieved();

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            if (Double.isNaN(maximumsAchieved[i])) {
                maximumsAchieved[i] = -9.9;
            }
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }
        newfile.write("\n");
        newfile.write("Time to Maximum Discharge [minutes],");
        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df2.format(timeToMaximumsAchieved[i]) + ",");
        }
        //newfile.write("\n");
        newfile.write("\n");


        //newfile.write("Precipitation Rates [mm/hr],");

        newfile.write("\n");
        newfile.write("Link,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df.format(linksStructure.contactsArray[i]) + ",");

        }

        newfile.write("\n");
        newfile.write("Link Outlet X,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            int xCon = linksStructure.contactsArray[i] % metaDatos.getNumCols();
            newfile.write(df.format(xCon) + ",");
        }

        newfile.write("\n");
        newfile.write("Link Outlet Y,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            int yCon = linksStructure.contactsArray[i] / metaDatos.getNumCols();
            newfile.write(df.format(yCon) + ",");
        }
        newfile.write("\n");
//        for (int k = 0; k < numPeriods; k++) {
//            double currTime = storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();
//            newfile.write(df2.format(currTime) + ",");
//            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//                //newfile.write(df2.format(thisHillsInfo.precipitationacum(i,currTime))+",");
//                newfile.write(df2.format(thisHillsInfo.precipitationacum(i, currTime)) + ",");
//
//            }
//
//            newfile.write("\n");
//
//        }

        newfile.close();
        bufferout.close();
    }

    public static void Gen_format(String Log) throws IOException {
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        ruta = new FileReader(new java.io.File(Log));
        buffer = new BufferedReader(ruta);

        String data = buffer.readLine();
        data = buffer.readLine();
        data = buffer.readLine();
        String Disc = data.substring(0, data.lastIndexOf(" Discharge_File"));
        System.out.println("Disc = " + Disc);
        data = buffer.readLine();
        String Prec = data.substring(0, data.lastIndexOf(" Precipitation_File"));
        System.out.println("Prec = " + Prec);

        GenerateFilesPrec(Prec);
        GenerateFilesDisc(Disc);


    }

    public static void GenerateFilesPrec(String Precip) throws IOException {

        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;
        String linea = null, basura, nexttoken;

        ruta = new FileReader(new java.io.File(Precip));
        buffer = new BufferedReader(ruta);
        String data = buffer.readLine();

        double[] max = new double[20000];
        double[] aver = new double[20000];
        double[] dev = new double[20000];
        double[] min = new double[20000];
        double[][] matrix = new double[1000][20000];
        //String [] column1 = new String[10000];
        int il = 0;
        int ic = 0;
        int icc = 0;
        while (data != null) {
            tokens = new StringTokenizer(data, ",");

            while (tokens.hasMoreTokens()) {
                matrix[il][ic] = new Double(tokens.nextToken());
                //if (il==0) link[ic]=new Double(tokens.nextToken());
                //if (il==1) area[ic]=new Double(tokens.nextToken());
                //if(il>1)prec[il-2][ic]=new Double(tokens.nextToken());
                ic = ic + 1;
            }
            icc = ic;
            ic = 0;
            il = il + 1;
            data = buffer.readLine();
        }
        ic = icc;
        int count = 0;
        for (int i = 2; i < (il); i++) {
            for (int c = 1; c < ic; c++) {
                if (matrix[i][c] >= 0) {
                    aver[i] = aver[i] + matrix[i][c];
                    count = count + 1;
                }
                if (max[i] < matrix[i][c]) {
                    max[i] = matrix[i][c];
                }
                if (min[i] > matrix[i][c] && matrix[i][c] > 0) {
                    min[i] = matrix[i][c];
                }
            }
            if (count > 0) {
                aver[i] = aver[i] / count;
            } else {
                aver[i] = -99.0;
            }
            count = 0;
        }
        count = 0;
        for (int i = 2; i < (il); i++) {
            for (int c = 1; c < ic; c++) {
                dev[i] = dev[i] + (matrix[i][c] - aver[i]) * (matrix[i][c] - aver[i]);
                count = count + 1;
            }

            if (count > 0) {
                dev[i] = Math.sqrt(dev[i] / count);
            } else {
                aver[i] = -99.0;
            }
        }

        java.io.File theFile;

        theFile = new java.io.File(Precip.substring(0, Precip.lastIndexOf(".")) + ".asc");
        System.out.println("Writing Prec Functions - " + theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        newfile.write("time " + "aver_prec " + "max_prec " + "min[i]_prec " + "dev[i]_prec \n");

        matrix[il] = matrix[il - 1];
        for (int i = 2; i < (il); i++) {
            double time1 = (matrix[i][0] - matrix[2][0]) / (24 * 60);
            double time2 = (matrix[i + 1][0] - matrix[2][0]) / (24 * 60);
            double time = time1 - (1 / (24 * 60 * 60));
            newfile.write(time + " " + "0.0000" + " " + "0.0000" + " " + "0.0000" + " " + "0.0000" + "\n");
            newfile.write(time + " " + aver[i] + " " + max[i] + " " + min[i] + " " + dev[i] + "\n");
            time = time2 - (2 / (24 * 60 * 60));
            newfile.write(time2 + " " + aver[i] + " " + max[i] + " " + min[i] + " " + dev[i] + "\n");
        }
        newfile.close();
        bufferout.close();

    }

    public static void GenerateFilesDisc(String Disc) throws IOException {

        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;
        String linea = null, basura, nexttoken;

        ruta = new FileReader(new java.io.File(Disc));
        buffer = new BufferedReader(ruta);
        String data = buffer.readLine();

        double[][] matrix = new double[1000][20000];
        double[] Qmax = new double[20000];
        double[] timemax = new double[20000];
        double maxq = 0;

        int il = 0;
        int icc = 0;
        int ic = 0;

        while (data != null) {
            tokens = new StringTokenizer(data, ",");
            while (tokens.hasMoreTokens()) {
                matrix[il][ic] = new Double(tokens.nextToken());
                ic = ic + 1;
                //System.out.println("ic - "+ic+"  il - "+il + "matrix[il][ic]" + matrix[il][ic]);
            }
            icc = ic;
            ic = 0;
            il = il + 1;
            data = buffer.readLine();
        }

        ic = icc;
        il = il - 1; // just because I have the error
        System.out.println("ic - " + ic + "  il - " + il);

        int[] IDindex = new int[10];
        int[] IDorder = new int[10];
        double[] IDarea = new double[10];
        String[] IDname = new String[10];

        for (int c = 2; c < (ic); c++) {
            if (matrix[0][c] == 1462) {
                IDindex[0] = c;
                IDname[0] = "Outlet";
                IDorder[05] = (int) matrix[1][c];
                IDarea[0] = matrix[2][c];
            }
            if (matrix[0][c] == 2611) {
                IDindex[1] = c;
                IDname[1] = "Mc_Alpine";
                IDorder[1] = (int) matrix[1][c];
                IDarea[1] = matrix[2][c];
            }
            if (matrix[0][c] == 3129) {
                IDindex[2] = c;
                IDname[2] = "Mc_Alpine";
                IDorder[2] = (int) matrix[1][c];
                IDarea[2] = matrix[2][c];
            }
            if (matrix[0][c] == 12518) {
                IDindex[3] = c;
                IDname[3] = "Sugar";
                IDorder[3] = (int) matrix[1][c];
                IDarea[3] = matrix[2][c];
            }
            if (matrix[0][c] == 8236) {
                IDindex[4] = c;
                IDname[4] = "Little_Sugar";
                IDorder[4] = (int) matrix[1][c];
                IDarea[4] = matrix[2][c];
            }
            if (matrix[0][c] == 4249) {
                IDindex[5] = c;
                IDname[5] = "Non_Urbanized";
                IDorder[5] = (int) matrix[1][c];
                IDarea[5] = matrix[2][c];
            }
        }


        int count = 0;
        int maxorder = 0;
        int indexmax = 0;
        for (int c = 1; c < ic; c++) {
            Qmax[c] = 0;
            timemax[c] = 0;
        }
        for (int i = 3; i < (il); i++) {

            for (int c = 1; c < ic; c++) {
                if (maxorder < matrix[1][c]) {
                    maxorder = (int) matrix[1][c];
                }
                if (maxq < matrix[i][c]) {
                    maxq = matrix[i][c];
                    indexmax = c;
                }


                if (Qmax[c] < matrix[i][c]) {
                    Qmax[c] = matrix[i][c];
                    timemax[c] = (matrix[i][0] - matrix[4][0]) / (24 * 60);
                }
            }

        }
        for (int c = 1; c < ic; c++) {
            System.out.println("c - " + c + "  Qmax[c] - " + Qmax[c]);
        }
        java.io.File theFile;

        theFile = new java.io.File(Disc.substring(0, Disc.lastIndexOf(".")) + "_Qmax.asc");
        System.out.println("Writing disc1 - " + theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        newfile.write("order " + "area " + "Qmax " + "time to peak \n");
        for (int c = 1; c < (ic); c++) {
            newfile.write(matrix[1][c] + " " + matrix[2][c] + " " + Qmax[c] + " " + timemax[c] + "\n");
        }
        newfile.close();
        bufferout.close();


        theFile = new java.io.File(Disc.substring(0, Disc.lastIndexOf(".")) + "hydrog.asc");
        System.out.println("Writing disc1 - " + theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);

        newfile = new java.io.OutputStreamWriter(bufferout);
        newfile.write("time " + IDname[0] + " " + IDname[1] + " " + IDname[2] + " " + IDname[3] + " " + IDname[4] + " " + IDname[5] + " " + " \n");
        newfile.write("area " + IDarea[0] + " " + IDarea[1] + " " + IDarea[2] + " " + IDarea[3] + " " + IDarea[4] + " " + IDarea[5] + " " + " \n");
        newfile.write("order " + IDorder[0] + " " + IDorder[1] + " " + IDorder[2] + " " + IDorder[3] + " " + IDorder[4] + " " + IDorder[5] + " " + " \n");
        for (int i = 3; i < (il); i++) {
            double time = (matrix[i][0] - matrix[3][0]) / (24 * 60);
            newfile.write(time + " " + matrix[i][IDindex[0]] + " " + matrix[i][IDindex[1]] + matrix[i][IDindex[2]] + " "
                    + " " + matrix[i][IDindex[3]] + " " + matrix[i][IDindex[4]] + " " + matrix[i][IDindex[5]] + "\n");
        }
        newfile.close();
        bufferout.close();

        double[] Qmaxaver = new double[maxorder + 1];
        double[] Qmaxdev = new double[maxorder + 1];
        int[] nelem = new int[maxorder + 1];

        for (int io = 1; io <= maxorder; io++) {
            count = 0;
            for (int c = 1; c < ic; c++) {
                if (matrix[1][c] == io) {
                    Qmaxaver[io] = Qmaxaver[io] + Qmax[c];
                    count = count + 1;
                }
            }
            Qmaxaver[io] = Qmaxaver[io] / count;
            nelem[io] = count;
        }

        for (int io = 1; io <= maxorder; io++) {
            count = 0;
            for (int c = 1; c < ic; c++) {
                if (matrix[1][c] == io) {
                    Qmaxdev[io] = Qmaxdev[io] + ((Qmax[c] - Qmaxaver[io]) * (Qmax[c] - Qmaxaver[io]));
                }
            }
            Qmaxdev[io] = Math.sqrt(Qmaxaver[io] / nelem[io]);
        }


        theFile = new java.io.File(Disc.substring(0, Disc.lastIndexOf(".")) + "_Qmaxstat.asc");
        System.out.println("Writing disc2 - " + theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        newfile.write("order " + "nelements " + "Qmaxave " + "Qmaxstd " + "\n");
        for (int io = 2; io <= maxorder; io++) {
            newfile.write(io + " " + nelem[io] + " " + Qmaxaver[io] + " " + Qmaxdev[io] + "\n");
        }
        newfile.close();
        bufferout.close();

    }

    public void run() {

        try {
            executeSimulation();
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
        } catch (VisADException v) {
            System.out.print(v);
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {

            //subMain1(args);   //11140102 - Blue River
            // subMain2(args);   //11110103 - Illinois, Arkansas
            //subMain3(args);   //11070208 - Elk River Near Tiff
            //subMain4(args);   //Clear Creek
            //subMain5(args);   //Whitewater radar
            //subMain6(args);   //Whitewate sat
            subMainManning(args);
            //Reservoir(args);
            //GreenRoof(args);   //Charlotte radar
            //genfiles(args);
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v) {
            System.out.print(v);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void subMainManning(String args[]) throws java.io.IOException, VisADException {

        ///// DEM DATA /////

        String[] AllSimName = {"90DEMUSGS"};

        String[] AllRain = {"TestBasin"};

        int[] Volume = {60};
        int[] Duration = {60};
        //int[] Volume = {180};
        //int[] Duration = {1440};

        int nsim = AllSimName.length;
        int nbas = AllRain.length;

        for (int i = 0; i < nsim; i++) {
            for (int ib = 0; ib < nbas; ib++) {
                for (int vol : Volume) {
                    for (int dur : Duration) {
                        System.out.println("Running BASIN " + AllSimName[i]);
                        System.out.println("Running BASIN " + AllRain[ib]);

                        String SimName = AllSimName[i];
                        String BasinName = AllRain[ib];
                        String DEM = "error";
                        // DEFINE THE DEM


                        DEM = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                        int xOut = 2817; // ClearCreek
                        int yOut = 713;// ClearCreek
                        if (SimName.equals("30DEMLIDAR")) {
                            xOut = 1341;
                            yOut = 82;
                            DEM = "/Users/luciana-cunha/Documents/CuencasDataBases/ClearCreek/Rasters/Topography/30meters/30meterc1.metaDEM";
                        }
                        if (SimName.equals("30DEMUSGS")) {
                            xOut = 1541;
                            yOut = 92;
                            DEM = "/Users/luciana-cunha/Documents/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM";
                        }

                        if (BasinName.indexOf("edar") > 0) {
                            System.out.println("Running FOR CEDAR RIVER ");
                            xOut = 2734;
                            yOut = 1069; //Cedar Rapids
                            DEM = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                        }


//                        if (BasinName.indexOf("Test") > 0) {
//                            System.out.println("Running FOR HillslopeTest ");
//                            xOut = 2372;
//                            yOut = 791; //Cedar Rapids
//                            DEM = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
//                        }
//
//
                        if (BasinName.indexOf("TestBasin") >= 0) {
                            System.out.println("Running FOR HillslopeTest ");
                            xOut = 2858;
                            yOut = 742; //Cedar Rapids
                            DEM = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                        }
                        if (BasinName.indexOf("loo") >= 0) {
                            System.out.println("Running FOR WATERLOO ");
                            xOut = 1932;
                            yOut = 1695; //Waterloo
                            DEM = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                        }

                        if (BasinName.indexOf("ville") >= 0) {
                            System.out.println("Running FOR Janesville ");
                            xOut = 1775;
                            yOut = 1879; //Waterloo
                            DEM = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                        }
                        System.out.println("xOut" + xOut + "yOut" + yOut);
                        //xOut = 2949; // Rapid Creek
                        //yOut = 741; // Rapid Creek
                        //xOut = 2734;
                        //yOut = 1069; //Cedar Rapids
                        //xOut=2885; //Iowa River at Iowa City
                        //yOut=690; // Iowa River at Iowa City

                        java.io.File theFile = new java.io.File(DEM);
                        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
                        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".dir"));
                        metaModif.setFormat("Byte");
                        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

                        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
                        metaModif.setFormat("Integer");
                        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

                        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".horton"));
                        metaModif.setFormat("Byte");
                        byte[][] horOrders = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

                        java.io.File stormFile;
                        java.util.Hashtable routingParams = new java.util.Hashtable();
                        stormFile = new java.io.File("/Users/luciana-cunha/Documents/Rainfall_synthetic/thesis2/" + vol + "mm/" + dur + "min/vhc/" + vol + "mm" + dur + "min.metaVHC");
                        //stormFile = new java.io.File("/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune17/hydroNexrad.metaVHC");


                        String LandUse = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
                        String SoilData = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";

                        System.out.println("LandUse  " + LandUse + "\n");
                        System.out.println("SoilData  " + SoilData + "\n");
                        System.out.println("Start SCS processes \n");

                        routingParams.put("widthCoeff", 1.0f);
                        routingParams.put("widthExponent", 0.4f);
                        routingParams.put("widthStdDev", 0.0f);
                        routingParams.put("chezyCoeff", 14.2f);
                        routingParams.put("chezyExponent", -1 / 3.0f);
                        routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)
                        routingParams.put("vssub", 1.0f);
                        routingParams.put("SoilMoisture", 2.f);
                        routingParams.put("lambdaSCSMethod", 0.0f);
                        routingParams.put("Vconst", 1.0f); // CHANGE IN THE NETWORKEQUATION CLA
                        routingParams.put("P5Condition", -9.0f); // Porcentage of the soil filled up with water
                        routingParams.put("ReserVolume", 0.0f); // m3/km2 of reservation
                        routingParams.put("ReserVolumetType", 1.0f); // reservoir position:
                        routingParams.put("RunoffCoefficient", 0.0f); // reservoir position:
                        routingParams.put("Basin_sim", 0.0f); // Cedar river - define Land cover and soil parameter
                        routingParams.put("dynaIndex", 3.f);
                        // Initial condition
                        routingParams.put("PorcHSaturated", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                        routingParams.put("PorcPhiUnsat", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                        routingParams.put("BaseFlowCoef", 0.0f); // define a constant number or -9.9 for vel=f(land Cover)
                        routingParams.put("BaseFlowExp", 0.40f); // define a constant number or -9.9 for vel=f(land Cover)
                        routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:

                        java.io.File outputDirectory = new java.io.File("/null/");

                        ////                   outputDirectory.mkdirs();
                        //                 new SimulationToFileSerialVersion(xOut, yOut, matDirs, magnitudes, metaModif, stormFile, 0.0f, outputDirectory, new java.io.File(LandUse), new java.io.File(SoilData), routingParams).executeSimulation();
                        routingParams.put("lambdaSCSMethod", 0.02f);


                        routingParams.put("RoutingT", 5.f); // check NetworkEquationsLuciana.java for definitions
                        routingParams.put("HillT", 6.f);  // check NetworkEquationsLuciana.java for definitions
                        routingParams.put("HillVelocityT", 4.f);

                        float[] vsAr = {0.001f};
                        float[] HSAr = {1.f};
                        float[] SAr = {-9.f};
                        float[] PH = {0.0f};
                        float[] Phi = {0.0f};
                        float[] l1Ar = {0.25f};
                        float[] vocAr = {1.0f};
                        float[] vhillAr = {50.f};

//                                 float[] vsAr = {0.0001f,0.0005f,0.001f};
//                        float[] HSAr = {0.f,1.f,2.f,3.f,4.f,5.f};
//                        float[] SAr = {-9.f,10.f,50.f,100.f,150.f,300.f};
//                        float[] PH = {0.0f,0.5f,1.0f};
//                        float[] Phi = {0.0f,0.5f,1.0f};
//                        float[] l1Ar = {0.1f,0.2f,0.3f};
//                        float[] vocAr = {1.0f};
//                        float[] vhillAr = {-9.f,10.f,50.f,100.f,200.f};
//
                        for (float voc : vocAr) {

                        for (float l11 : l1Ar) {
//                            float l1 = l11;
//                            float l2 = -0.82f * l1 + 0.1025f;
//                            float vo = voc*(0.42f - 0.29f * l1 - 3.1f * l2);

                            float l1 = l11;
                            float l2 = -0.1f;
                            float vo = 0.66f;

                            for (float vhill : vhillAr) {

                                if (vhill == -9) {
                                    routingParams.put("HillVelocityT", 4.f);
                                } else {
                                    routingParams.put("vrunoff", vhill); // define a constant number or -9.9 for vel=f(land Cover)
                                    routingParams.put("HillVelocityT", 0.f);
                                }
                            System.out.print("vh" + vhill);

                            routingParams.put("v_o", vo);
                            routingParams.put("lambda1", l1);
                            routingParams.put("lambda2", l2);

                            for (float voo : vsAr) {
                                float vsub = voo;
                                routingParams.put("vssub", vsub);

                                for (float in1 : PH) {
                                    float p1 = in1;
                                    routingParams.put("PorcHSaturated", p1);

                                    for (float in2 : Phi) {
                                        float p2 = in2;
                                        routingParams.put("PorcPhiUnsat", p2);

                                        for (float hs1 : HSAr) {
                                            float hs = hs1;
                                            routingParams.put("hillshapeparamflag", hs);

                                            for (float s1 : SAr) {
                                                float s = s1;
                                                routingParams.put("ConstSoilStorage", s);  // check NetworkEquationsLuciana.java for definitions

                                                //0 use the param specified by the DEM
                                                //1 model concave 1 (0+0.6h-1.6h^2+2.0*h^3)
                                                //2 model concave 2 (0+0.5h-0.1h^2+0.6*h^3)
                                                //3 model linear (h)
                                                //4 model convex 1 (0+2.3h-2.1h^2+0.8*h^3)
                                                //5 model convex 2 (0+3.5h-4.4h^2+1.9*h^3)
                                                // check NetworkEquationsLuciana.java for definitions


                                                outputDirectory = new java.io.File("/Users/luciana-cunha/Documents/CUENCAS_results/ModelComp_invEq3/Const_Precipitation/" + BasinName + "/" + SimName + "/"
                                                        + "/Vol_" + vol + "Dur_" + dur
                                                        + "Rout_" + ((Float) routingParams.get("RoutingT")).floatValue() + "HillT_" + ((Float) routingParams.get("HillT")).floatValue() + "HVel_" + ((Float) routingParams.get("HillVelocityT")).floatValue()
                                                        + "VS_" + ((Float) routingParams.get("vssub")).floatValue()
                                                        + "HS_" + ((Float) routingParams.get("hillshapeparamflag")).floatValue()
                                                        + "SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                                                        + "PH_" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                        + "UnsO_" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                        + "L1_" + ((Float) routingParams.get("lambda1")).floatValue()
                                                        + "vco_" + voc
                                                        + "vhill_" + vhill);


                                                outputDirectory.mkdirs();
                                                int WriteOrder = 1;
                                                
                                                String Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
                            
  
                                                new SimulationToFileSerialVersion(xOut, yOut, matDirs, magnitudes, metaModif, stormFile, 0.0f, outputDirectory, new java.io.File(LandUse), new java.io.File(SoilData),new java.io.File(Soil150SWAData), WriteOrder, routingParams).executeSimulation();

                                                java.io.File theSummary;

                                                theSummary = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "SimulationParam" + ".txt");

                                                System.out.println(theSummary);

                                                java.io.FileOutputStream salidaSum = new java.io.FileOutputStream(theSummary);
                                                java.io.BufferedOutputStream bufferoutSum = new java.io.BufferedOutputStream(salidaSum);
                                                java.io.OutputStreamWriter newfileSum = new java.io.OutputStreamWriter(bufferoutSum);

                                                newfileSum.write(routingParams.toString() + " ");


                                                newfileSum.close();
                                                bufferoutSum.close();
                                            }}
                                            }
                                        }
                                    }
                                }
                            }
                        }

                    }
                }
            }

        }

    }
}
