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
import java.util.TimeZone;
/**
 *
 * @author Luciana Cunha
 */
public class SimulationToFileSerialMay2012 extends java.lang.Object implements Runnable {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir, hortonOrders;
    int x, y;
    int[][] magnitudes;
    float rainIntensity;
    float rainDuration;
    java.io.File stormFile;
    java.io.File PotEVPTFile;
    java.io.File SNOWMeltFile;
    java.io.File SNOWSWEFile;
    java.io.File SOILMFile;
    hydroScalingAPI.io.MetaRaster infiltMetaRaster;
    float infiltRate;
    int routingType, HillType, HillVelType;
    java.io.File outputDirectory;
    java.util.Hashtable routingParams;
    int basinOrder;
    private java.util.Calendar zeroSimulationTime;
    private java.util.Calendar finalSimulationTime;
    // Add for the SCS
    int greenroof;
    java.io.File LandUseFile;
    java.io.File SoilFile;
    java.io.File SoilHydFile;
    java.io.File Swa150File;
    float LandUseFileFlag;
    float SoilFileFlag;
    float IniCondition;
    int writeorder;

    /** Creates new simulationsRep3 */
    public SimulationToFileSerialMay2012(int xx,
            int yy,
            byte[][] direcc,
            int[][] magnitudesOR,
            byte[][] horOrders,
            hydroScalingAPI.io.MetaRaster md,
            java.io.File stormFileOR,
            java.io.File PotEVPTFileOR,
            java.io.File SNOWMeltFileOR,
            java.io.File SNOWSWEFileOR,
            java.io.File SOILMFileOR,
            float infiltRateOR,
            int routingTypeOR,
            java.util.Hashtable rP,
            java.io.File outputDirectoryOR,
            long zST, long fST, int writeorderOR) throws java.io.IOException, VisADException {




        zeroSimulationTime = java.util.Calendar.getInstance();
        zeroSimulationTime.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        zeroSimulationTime.setTimeInMillis(zST);

        finalSimulationTime = java.util.Calendar.getInstance();
         finalSimulationTime.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        finalSimulationTime.setTimeInMillis(fST);

        matDir = direcc;
        metaDatos = md;

        x = xx;
        y = yy;


        magnitudes = magnitudesOR;
        hortonOrders = horOrders;

        stormFile = stormFileOR;
        PotEVPTFile = PotEVPTFileOR;
        SNOWMeltFile = SNOWMeltFileOR;
        SNOWSWEFile = SNOWSWEFileOR;
        SOILMFile = SOILMFileOR;

        infiltRate = infiltRateOR;
        routingType = routingTypeOR;
        outputDirectory = outputDirectoryOR;
        routingParams = rP;
        writeorder = writeorderOR;


    }

    public void executeSimulation() throws java.io.IOException, VisADException {

        //Here an example of rainfall-runoff in action
        System.out.println("Start executSimulation \n");
        java.util.Date startTime = new java.util.Date();
        System.out.println("Start Tile 1," + x + "," + y + "," + startTime.toString());
        System.out.println("Running Time:" + "0.0" + " seconds");
        java.io.File theFile;
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/Tile_" + x + "_" + y + ".done");
        System.out.println(theFile);

        if (theFile.exists()) {

            //ATTENTION
            //The followng print statement announces the completion of the program.
            //DO NOT modify!  It tells the queue manager that the process can be
            //safely killed.
            System.out.println("Termina escritura de Resultados" + x + "," + y);
            return;
        }


        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDir, metaDatos);



        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);

        basinOrder = linksStructure.getBasinOrder();

        String demName = metaDatos.getLocationBinaryFile().getName().substring(0, metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        java.io.File theFile2 = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "_" + x + "_" + y + ".complete.csv");
        java.io.OutputStreamWriter compnewfile = new java.io.OutputStreamWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(theFile2)));
        java.util.Date interTime = new java.util.Date();
        System.out.println("Starting Writing complete file," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
        System.out.println("basinOrder," + basinOrder);



        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            compnewfile.write(linksStructure.completeStreamLinksArray[i] + ",");
        }
        compnewfile.close();

        interTime = new java.util.Date();
        System.out.println("Finish Writing complete file," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");



        interTime = new java.util.Date();
        System.out.println("Define network geometry," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);


        interTime = new java.util.Date();
        System.out.println("Define parameters," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
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
        float PoundedW = 0.0f;
        double Tolerance = 10e-3;
        int outflag = 2;

        if (routingParams.get("ETolerance") != null) {
            Tolerance = (double) ((Float) routingParams.get("ETolerance")).floatValue();
        }

        if (routingParams.get("OutFlag") != null) {
            outflag = (int) ((Float) routingParams.get("OutFlag")).floatValue();
        }

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
        if (routingParams.get("PondedWater") != null) {
            PoundedW = ((Float) routingParams.get("PondedWater")).floatValue();
        }
        float kf1 = 0.1f;
        if (routingParams.get("floodplaincte") != null) {
            kf1 = ((Float) routingParams.get("floodplaincte")).floatValue();
        }
        System.out.println("lam1 " + lam1 + "lam2" + lam2 + "v_o" + v_o);
        float Outflag = 0.f;
        if (routingParams.get("Outflag") != null) {
            kf1 = ((Float) routingParams.get("Outflag")).floatValue();
        }

        // Modified SCS


        System.out.println("routingtype \n");
        //System.out.println("hilltype" + ((Float)routingParams.get("HillType")).floatValue());
        //System.out.println("HillVelocityT" + ((Float)routingParams.get("HillVelocityT")).floatValue());
        //System.out.println("RoutingT" + ((Float)routingParams.get("RoutingT")).floatValue());
        //float ht=((Float)routingParams.get("HillT")).floatValue();  // check NetworkEquationsLuciana.java for definitions
        //float hvt=((Float)routingParams.get("HillVelocityT")).floatValue(); // check NetworkEquationsLuciana.java for definitions
        float P5 = 0.0f;
        if (routingParams.get("P5Condition") != null) {
            ((Float) routingParams.get("P5Condition")).floatValue();
        }
        IniCondition = 0.0f;
        if (routingParams.get("InitialCondition") != null) {
            IniCondition = ((Float) routingParams.get("InitialCondition")).floatValue();
        } // % of the saturated soil
        float lambdaSCS = 0.0f;
        if (routingParams.get("lambdaSCSMethod") != null) {
            lambdaSCS = ((Float) routingParams.get("lambdaSCSMethod")).floatValue();
        }

        //float rt=((Float)routingParams.get("RoutingT")).floatValue(); // check NetworkEquationsLuciana.java for definitions     

        HillType = 4;
        if (routingParams.get("HillT") != null) {
            HillType = (int) ((Float) routingParams.get("HillT")).floatValue();

        }
        HillVelType = 3;
        if (routingParams.get("HillVelocityT") != null) {
            HillVelType = (int) ((Float) routingParams.get("HillVelocityT")).floatValue();
        }
        routingType = 5;
        if (routingParams.get("RoutingT") != null) {
            routingType = (int) ((Float) routingParams.get("RoutingT")).floatValue();
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


        interTime = new java.util.Date();
        System.out.println("Define thishillsinfo," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);



        interTime = new java.util.Date();
        System.out.println("Start storm," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        System.out.println("STORM STRING    " + stormFile.toString() + "   " + stormFile.toString().contains("Constant"));


        if (stormFile.toString().contains("Constant")) {

            if (routingParams.get("rainIntensity") != null) {
                rainIntensity = ((Float) routingParams.get("rainIntensity")).floatValue();
            }
            System.out.println("rainIntensity   " + rainIntensity);
            if (routingParams.get("rainDuration") != null) {
                rainDuration = ((Float) routingParams.get("rainDuration")).floatValue();
            }
            System.out.println("rainDuration   " + rainDuration);
            //rainIntensity
            storm = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure, rainIntensity, rainDuration);
        } else {

            storm = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
            storm.setStormInitialTime(zeroSimulationTime);
            storm.setStormFinalTime(finalSimulationTime);

        }
        if (!storm.isCompleted()) {
            System.out.println("Problem with Storm");
            return;
        }
        interTime = new java.util.Date();
        System.out.println("Finish Storm,start EVPT" + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager PotEVPT;
        hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager EVPT;


        float numPeriodstest = Math.round(((float) storm.stormFinalTimeInMinutes() - (float) storm.stormInitialTimeInMinutes()) / (float) storm.stormRecordResolutionInMinutes());
        //System.out.println("BEFOREnumPeriodstest"+numPeriodstest+" Final" + storm.stormFinalTimeInMinutes() +"Initial" + (float) storm.stormInitialTimeInMinutes() + "res=" + (float) storm.stormRecordResolutionInMinutes());
        System.out.println("PotEVPTFile STRING    " + PotEVPTFile.toString() + "   " + PotEVPTFile.toString().contains("Constant"));

        if (PotEVPTFile == null || PotEVPTFile.toString().contains("null")) {
            PotEVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager(linksStructure, 0, 10, storm.stormInitialTime());
            EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(linksStructure, 0, 10);

        } else if (PotEVPTFile.toString().contains("Constant")) {
            System.out.println("Pot Evap Constant");
            float EVPTIntensity = 0.0f;
            if (routingParams.get("EVPTIntensity") != null) {
                EVPTIntensity = ((Float) routingParams.get("EVPTIntensity")).floatValue();
            }
            System.out.println("Pot Evap Constant " + EVPTIntensity);
            float EVPTDuration = 0.0f;
            if (routingParams.get("EVPTDuration") != null) {
                EVPTDuration = ((Float) routingParams.get("EVPTDuration")).floatValue();

            }
            System.out.println("Pot Evap Constant " + EVPTDuration);
            //rainIntensity
            System.out.println("storm.stormInitialTime()" + storm.stormInitialTime().getTimeInMillis());
            System.out.println("zeroSimulationTime" + zeroSimulationTime.getTimeInMillis());
            PotEVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager(linksStructure, EVPTIntensity, EVPTDuration, zeroSimulationTime);

        } else {
            System.out.println("EVPT is not constant");
            PotEVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager(PotEVPTFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
            //System.out.println(PotEVPTFile.getAbsolutePath().replace("PET", "ET"));       
            java.io.File evpt = new java.io.File(PotEVPTFile.getAbsolutePath().replace("PET", "ET"));
            if (evpt.exists()) {

                System.out.println("file exists  " + PotEVPTFile.getAbsolutePath().replace("PET", "ET"));

                EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(new java.io.File(PotEVPTFile.getAbsolutePath().replace("PET", "ET")), myCuenca, linksStructure, metaDatos, matDir, magnitudes);
                if (!EVPT.isCompleted()) {
                    System.out.println("Problem with EVPT");
                    return;
                }
                thisHillsInfo.setEVPTManager(EVPT);
            }

        }



        hydroScalingAPI.modules.rainfallRunoffModel.objects.SnowMeltManager SNOW;
        interTime = new java.util.Date();
        System.out.println("Finish EVPT, start Snowmelt" + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        if (SNOWMeltFile == null || SNOWMeltFile.toString().contains("null")) {
            SNOW = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SnowMeltManager(linksStructure, 0, 10);
            //EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(linksStructure, rainIntensity, rainDuration);       
        } else {
            System.out.println("SNOW is not constant");
            SNOW = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SnowMeltManager(SNOWMeltFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
            //System.out.println(PotEVPTFile.getAbsolutePath().replace("PET", "ET"));       
            //EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(new java.io.File(PotEVPTFile.getAbsolutePath().replace("PET", "ET")), myCuenca, linksStructure, metaDatos, matDir, magnitudes);

        }

        hydroScalingAPI.modules.rainfallRunoffModel.objects.SnowCoverManager SNOWCover;
        interTime = new java.util.Date();
        System.out.println("SNOWSWEFile STRING    " + SNOWSWEFile.toString() + "   " + SNOWMeltFile.toString().contains("Constant"));


        if (SNOWSWEFile == null || SNOWSWEFile.toString().contains("null")) {
            SNOWCover = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SnowCoverManager(linksStructure, 0, 10);
            //EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(linksStructure, rainIntensity, rainDuration);       
        } else {
            System.out.println("SNOW is not constant");
            SNOWCover = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SnowCoverManager(SNOWSWEFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
            //System.out.println(PotEVPTFile.getAbsolutePath().replace("PET", "ET"));       
            //EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(new java.io.File(PotEVPTFile.getAbsolutePath().replace("PET", "ET")), myCuenca, linksStructure, metaDatos, matDir, magnitudes);

        }
        interTime = new java.util.Date();
        System.out.println("Finish SNOW, start SOILM" + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager SOILM;
        System.out.println("SOILMFile STRING    " + SOILMFile.toString() + "   " + SOILMFile.toString().contains("Constant") + "value initial " + IniHSatPorc);


        if (SOILMFile == null || SOILMFile.toString().contains("null") || IniUnSatPorc >= 0) {
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(linksStructure, 0, 10);
            //EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(linksStructure, rainIntensity, rainDuration);

        } else {
            System.out.println("SOIL is not constant");
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(SOILMFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
            System.out.println(SOILMFile.getAbsolutePath());
            //EVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.EVPTManager(new java.io.File(PotEVPTFile.getAbsolutePath().replace("PET", "ET")), myCuenca, linksStructure, metaDatos, matDir, magnitudes);

        }
        interTime = new java.util.Date();
        System.out.println("Finish SOILM" + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        interTime = new java.util.Date();
        System.out.println("Finished to read all datasets," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        //PotEVPT.setPotEVPTInitialTime(zeroSimulationTime);
        //IowaPET.000000.06.March.2003.vhc
        //EVPT.setEVPTInitialTime(zeroSimulationTime);


        if (!PotEVPT.isCompleted()) {
            System.out.println("Problem with Pot EVPT");
            return;
        }


        if (!SNOW.isCompleted()) {
            System.out.println("Problem with SNOW");
            return;
        }

        if (!SNOWCover.isCompleted()) {
            System.out.println("Problem with SNOW Cover");
            return;
        }
        if (!SOILM.isCompleted()) {
            System.out.println("Problem with SOILM");
            return;
        }

        System.out.println("EVPT INITIAL TIME  " + PotEVPT.PotEVPTInitialTime());
        System.out.println("EVPT RES TIME  " + PotEVPT.PotEVPTRecordResolutionInMinutes());
        System.out.println("EVPT FINAL TIME  " + PotEVPT.PotEVPTFinalTimeInMinutes());
//System.out.println("STORM INITIAL TIME  " +  storm.stormInitialTime());

//        System.out.println("RESOL min  "+EVPT.EVPTRecordResolutionInMinutes());

        thisHillsInfo.setStormManager(storm);

        interTime = new java.util.Date();
        System.out.println("Start setting all database (EVPT, STORM, Snow, SOIL," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        thisHillsInfo.setPotEVPTManager(PotEVPT);
        thisHillsInfo.setSnowMeltManager(SNOW);
        thisHillsInfo.setSnowCoverManager(SNOWCover);
        thisHillsInfo.setSoilMoistureManager(SOILM);

        interTime = new java.util.Date();

        System.out.println("Done setting all database (EVPT, STORM, Snow, SOIL," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infilMan = new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure, 0.0f);

        if (infiltMetaRaster == null) {
            infilMan = new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure, infiltRate);
        } else {
            infilMan = new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(myCuenca, linksStructure, infiltMetaRaster, matDir, magnitudes);
        }

        thisHillsInfo.setInfManager(infilMan);


        interTime = new java.util.Date();
        System.out.println("FSet Infil manager," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


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



        interTime = new java.util.Date();
        System.out.println("Generate the infiltration," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        if (infiltMetaRaster == null) {
            theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "_" + x + "_" + y + "-" + storm.stormName() + "-IR_" + infiltRate + "-Routing_" + routingString + "_params_" + lam1 + "_" + lam2 + "_" + v_o + ".csv");//theFile=new java.io.File(Directory+demName+"_"+x+"_"+y++"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+widthCoeff+"_"+widthExponent+"_"+widthStdDev+"_"+chezyCoeff+"_"+chezyExponent+".dat");
        } else {
            theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "_" + x + "_" + y + "-" + storm.stormName() + "-IR_" + infiltMetaRaster.getLocationMeta().getName().substring(0, infiltMetaRaster.getLocationMeta().getName().lastIndexOf(".metaVHC")) + "-Routing_" + routingString + "_params_" + lam1 + "_" + lam2 + "QP.csv");
        }


        interTime = new java.util.Date();
        System.out.println("Generate the name of the file," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");




        interTime = new java.util.Date();
        System.out.println("Define Land cover info," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        int BasinFlag = 0;
        if (routingParams.get("Basin_sim") != null) {
            BasinFlag = (int) ((Float) routingParams.get("Basin_sim")).floatValue();
        }
        String LandUse = "Error";
        String SoilData = "Error";
        String SoilHydData = "Error";
        String Soil150SWAData = "Error";
        // did not find a way to pass that without change ExternalTileToFile
        // This has to be fixed
        if (BasinFlag == 0) {
            LandUse = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            SoilData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }

        if (BasinFlag == 1) {
            LandUse = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/glomod90.metaVHC";
            SoilData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 2) {
            LandUse = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverBaseline_90_2.metaVHC";
            SoilData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 3) {
            LandUse = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverRestoringPastureTo10percentForest_90_2.metaVHC";
            SoilData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 4) {
            LandUse = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverBaseline_90_2.metaVHC";
            SoilData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 25) {
            LandUse = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverTranfrom_25_90_2.metaVHC";
            SoilData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 50) {
            LandUse = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverTranfrom_50_90_2.metaVHC";
            SoilData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 75) {
            LandUse = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverTranfrom_75_90_2.metaVHC";
            SoilData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }
        System.out.println("BasinFlag" + BasinFlag);

        if (BasinFlag == 200) { // this is for shallehills
            LandUse = "/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Hydrology/landcoverproj.metaVHC";
            SoilData = "/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Hydrology/soilhydtype.metaVHC";
            SoilHydData = "/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Hydrology/hydcond.metaVHC";
            Soil150SWAData = "/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Hydrology/swe.metaVHC";
        }

        System.out.println("LandUse  " + LandUse + "\n");
        System.out.println("SoilData  " + SoilData + "\n");
        System.out.println("Start SCS processes \n");
        LandUseFile = new java.io.File(LandUse);
        SoilFile = new java.io.File(SoilData);
        SoilHydFile = new java.io.File(SoilHydData);
        Swa150File = new java.io.File(Soil150SWAData);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
        java.io.File DEMFile = metaDatos.getLocationMeta();

        interTime = new java.util.Date();
        System.out.println("Create SCSObj," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile, SoilHydFile, Swa150File, myCuenca, linksStructure, metaDatos, matDir, magnitudes, HillShapeFlag);

        interTime = new java.util.Date();
        System.out.println("Set MAnager," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        thisHillsInfo.setSCSManager(SCSObj);


        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

        java.io.File theFile1 = new java.io.File(theFile.getAbsolutePath() + ".Outlet.csv");
        java.io.FileOutputStream salida1 = new java.io.FileOutputStream(theFile1);
        java.io.BufferedOutputStream bufferout1 = new java.io.BufferedOutputStream(salida1);
        java.io.OutputStreamWriter newfile1 = new java.io.OutputStreamWriter(bufferout1);

        java.io.File theFile3 = new java.io.File(theFile.getAbsolutePath() + ".Storage.csv");
        java.io.FileOutputStream salida3 = new java.io.FileOutputStream(theFile3);
        java.io.BufferedOutputStream bufferout3 = new java.io.BufferedOutputStream(salida3);
        java.io.OutputStreamWriter newfile3 = new java.io.OutputStreamWriter(bufferout3);

        java.io.File theFile4 = new java.io.File(theFile.getAbsolutePath() + ".Others.csv");
        java.io.FileOutputStream salida4 = new java.io.FileOutputStream(theFile4);
        java.io.BufferedOutputStream bufferout4 = new java.io.BufferedOutputStream(salida4);
        java.io.OutputStreamWriter newfile4 = new java.io.OutputStreamWriter(bufferout4);

        java.io.File theFile5 = new java.io.File(theFile.getAbsolutePath() + ".Others.csv");
        java.io.FileOutputStream salida5 = new java.io.FileOutputStream(theFile5);
        java.io.BufferedOutputStream bufferout5 = new java.io.BufferedOutputStream(salida5);
        java.io.OutputStreamWriter newfile5 = new java.io.OutputStreamWriter(bufferout5);

        DecimalFormat df = new DecimalFormat("###");
        DecimalFormat df1 = new DecimalFormat("###.#");
        DecimalFormat df2 = new DecimalFormat("###.##");
        DecimalFormat df3 = new DecimalFormat("###.###");
        DecimalFormat df4 = new DecimalFormat("###.####");
        DecimalFormat df10 = new DecimalFormat("###.##########");

        System.out.println("Writing output," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        newfile.write("Information on order Streams\n");
        newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("Link #,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write("Link-" + df.format(i) + ",");
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
        newfile.write("SWA150 [mm],");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(thisHillsInfo.SWA150(i)) + ",");
        }


        newfile.write("\n");
        newfile.write("Ave Hyd Cond [mm/h],");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(thisHillsInfo.AveHydCond(i)) + ",");
        }

        newfile.write("\n");
        newfile.write("Hill Relief [m],");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(thisHillsInfo.HillRelief(i)) + ",");
        }
        newfile.write("\n");
        newfile.write("Link Outlet ID,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df.format(i) + ",");
        }
        System.out.println("Calculating distance to the outlet 1," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        System.out.println("Writing distance to the outlet 1");
        newfile.write("\n");
        newfile.write("Distance to outlet,");
        float[][] bigDtoO = linksStructure.getDistancesToOutlet();
        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            //newfile.write(df2.format(bigDtoO[1][i]) + ",");

            newfile.write(df4.format(thisHillsInfo.HillRelief(i)) + ",");
        }
        System.out.println("Writing river length");
        newfile.write("\n");
        newfile.write("River length,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df2.format(thisNetworkGeom.Length(i)) + ",");
        }

        System.out.println("Writing SLOPE");
        newfile.write("\n");
        newfile.write("Slope,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df2.format(thisHillsInfo.getHillslope(i)) + ",");
        }


        newfile.write("\n\n\n");
        newfile.write("Results of flow simulations in your basin");

        newfile.write("\n");
        newfile.write("Time,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write("Link-" + df.format(i) + ",");
        }




        interTime = new java.util.Date();
        System.out.println("Finish file," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");



        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_AllMethodSerialMay_2012 thisBasinEqSys = new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_AllMethodSerialMay_2012(linksStructure, thisHillsInfo, thisNetworkGeom, routingParams);


        interTime = new java.util.Date();
        System.out.println("Finish equationobj,start to set up IC" + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        int NLI = linksStructure.contactsArray.length;
        double[] initialCondition;

        int numPeriods = Math.round(((float) storm.stormFinalTimeInMinutes() - (float) storm.stormInitialTimeInMinutes()) / (float) storm.stormRecordResolutionInMinutes());
        int numPeriods1 = Math.round(((float) storm.stormFinalTimeInMinutes() - (float) storm.stormInitialTimeInMinutes()) / (float) storm.stormRecordResolutionInMinutes());

        System.out.println("period storm final " + (float) storm.stormFinalTimeInMinutes() + "storm start" + (float) storm.stormInitialTimeInMinutes());
        int numPeriods2 = Math.round(((float) finalSimulationTime.getTimeInMillis() / (1000 * 60) - (float) zeroSimulationTime.getTimeInMillis() / (1000 * 60)) / (float) storm.stormRecordResolutionInMinutes());
        System.out.println("period simu final " + (float) finalSimulationTime.getTimeInMillis() / (1000 * 60) + "simu start" + (float) zeroSimulationTime.getTimeInMillis() / (1000 * 60));
//
//                
//        for (int k = 0; k < numPeriods; k++) {
//            double currTime = PotEVPT.PotEVPTInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();
//            System.out.println(df2.format(currTime) + ",");
//            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//                System.out.println(" value"+df2.format(thisHillsInfo.PotEVPT(i, currTime)) + ",");
//                //newfile.write(df2.format(thisHillsInfo.precipitationacum(i, currTime)) + ",");
//
//            }
//
//            System.out.println("\n");
//
//        }


//System.exit(1);

        //int numPeriods = Math.round(((float) storm.stormFinalTimeInMinutes() - (float) storm.stormInitialTimeInMinutes()) / (float) storm.stormRecordResolutionInMinutes());

        if (numPeriods1 > numPeriods2) {
            numPeriods = numPeriods2;
        }
        System.out.println("period 1 " + numPeriods + "period2" + numPeriods2);
        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        thisDate.setTimeInMillis((long) (storm.stormInitialTimeInMinutes() * 60. * 1000.0));
        System.out.println(thisDate.getTime());


        initialCondition = new double[NLI * 9];
//            if(HillType==7 || HillType==8) initialCondition = new double[NLI * 5];
//            else if (HillType==9) initialCondition = new double[NLI * 6];
//            else initialCondition = new double[NLI * 4];

        System.out.println("Start Initial condition," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
        float HD = -9.f;
        if (routingParams.get("ConstSoilStorage") != null) {
            System.out.println("Start Initial" + routingParams.get("ConstSoilStorage"));

            HD = ((Float) routingParams.get("ConstSoilStorage")).floatValue();
        }
        System.out.println("ConstSoilStorage" + HD);




        for (int i = 0; i < NLI; i++) {


            initialCondition[i] = BaseFlowCoef * Math.pow(thisNetworkGeom.upStreamArea(i), BaseFlowExp);
            initialCondition[i + 1 * NLI] = PoundedW;
            ////   float Wtable=0;
//            if
//     float PorcAreaCalc=100.f;
//     float h=0;
//     float a=
//     PorcAreaCalc=0.0f;
//         while(Math.abs(PorcArea-PorcAreaCalc)<0.05) {
//             h=h+0.01f;
//             PorcAreaCalc=thisHillsInfo.basinHillSlopesInfo.getArea_ReliefParam(i, 0);
//         }
            //initialCondition[i + 2 * NLI] =FindWaterTable(IniHSatPorc,thisHillsInfo.HillRelief(i));
            double INS = thisHillsInfo.SWA150(i);
            if (HD == -1) {
                INS = thisHillsInfo.SCS_S1(i);
            } else if (HD == -2) {
                INS = thisHillsInfo.SCS_S2(i);
            } else if (HD == -3) {
                INS = thisHillsInfo.SWA150(i);
            }

            if (IniHSatPorc >= 0) {
                if (thisHillsInfo.HillRelief(i) > 0) {
                    initialCondition[i + 2 * NLI] = (float) INS + 5 + IniHSatPorc * thisHillsInfo.HillRelief(i) * 1000; // [mm] maximum storage - considering basin was dry
                } else {
                    initialCondition[i + 2 * NLI] = (float) INS; // [mm] maximum storage - considering basin was dry
                }
            }

            // else{
            //     double IniC=SOILM.getSOILM040OnHillslope(i, zeroSimulationTime);
            //     initialCondition[i + 2 * NLI] = (float) INS+5+IniHSatPorc*(IniC/100)*1000;; // [mm] maximum storage - considering basin was dry
            // }
            if (IniUnSatPorc >= 0) {
                if (thisHillsInfo.HillRelief(i) > 0) {
                    initialCondition[i + 3 * NLI] = IniUnSatPorc;
                } else {
                    initialCondition[i + 3 * NLI] = 0.0001;
                }
            } else {
                double IniC = thisHillsInfo.SoilMoisture(i, 0);

                if (IniC <= 0) {
                    int k = 0;
                    while (IniC <= 0 && k < numPeriods) {
                        k = k + 1;
                        double currTime = storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();
                        IniC = thisHillsInfo.SoilMoisture(i, currTime);
                    }
                }
                if (IniC <= 0) {
                    initialCondition[i + 3 * NLI] = 50.;
                }

                if (thisHillsInfo.HillRelief(i) > 0) {
                    initialCondition[i + 3 * NLI] = IniC / 100.;
                    if (initialCondition[i + 3 * NLI] <= 0) {
                        initialCondition[i + 3 * NLI] = 0.5;
                    }
                } else {
                    initialCondition[i + 3 * NLI] = 0.0;
                }

            }

            initialCondition[i + 4 * NLI] = 0.0;
            initialCondition[i + 5 * NLI] = 0.0;
            initialCondition[i + 6 * NLI] = 0.0;
            initialCondition[i + 7 * NLI] = 0.0;
            initialCondition[i + 8 * NLI] = 0.0;
            //initialCondition[i + 9 * NLI] = 0.0;
            //System.out.println("initialCondition[i]"+initialCondition[i]+"initialCondition[i + 1 * NLI]"+initialCondition[i + 1 * NLI]+"initialCondition[i + 2 * NLI]"+initialCondition[i + 2 * NLI]+"initialCondition[i + 3 * NLI]"+initialCondition[i + 3 * NLI]);

        }

        System.out.println("Finish Initial condition," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        interTime = new java.util.Date();
        System.out.println("Create diff equation," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        System.out.println("Number of Links on this simulation: " + NLI);
        System.out.println("Inicia simulacion RKF");

        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining = new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys, Tolerance, 10 / 60.);



        double outputTimeStep = storm.stormRecordResolutionInMinutes();
        // double extraSimTime=240D*Math.pow(1.5D,(basinOrder-1));
        System.out.println("numPeriods" + numPeriods);

        newfile1.write("TimeStep:" + outputTimeStep + "\n");
        newfile1.write("Time (minutes), Discharge [m3/s] \n");
        newfile3.write("TimeStep:" + outputTimeStep + "\n");
        newfile3.write("Time (minutes), Surface Storage [mm], Soil Storage [mm] \n");
        newfile4.write("TimeStep:" + outputTimeStep + "\n");
        newfile4.write("Time (minutes), Evaporation, Qp_l, Qs_l,Storages \n");
        interTime = new java.util.Date();
        System.out.println("Start loop," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        for (int k = 0; k < numPeriods; k++) {
            System.out.println("Period " + (k + 1) + " of " + numPeriods);
            rainRunoffRaining.jumpsRunCompleteToAsciiFileSCSSerial(storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes(), storm.stormInitialTimeInMinutes() + (k + 1) * storm.stormRecordResolutionInMinutes(), outputTimeStep, initialCondition, newfile, linksStructure, thisNetworkGeom, newfile1, newfile3, newfile4, newfile5, writeorder);
            // Parallel - rainRunoffRaining.jumpsRunCompleteToAsciiFileSCS(storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes(), storm.stormInitialTimeInMinutes() + (k + 1) * storm.stormRecordResolutionInMinutes(), outputTimeStep, initialCondition, newfile, linksStructure, thisNetworkGeom, newfile1, newfile3, newfile4, outflag);
            initialCondition = rainRunoffRaining.finalCond;
            rainRunoffRaining.setBasicTimeStep(10 / 60.);

        }


        interTime = new java.util.Date();
        System.out.println("Running Time:" + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
        System.out.println("Finish first part," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
        //outputTimeStep=5*Math.pow(2.0D,(basinOrder-1));

        outputTimeStep = 2 * Math.pow(1.5D, (basinOrder - 1));
        if (outputTimeStep > 60) {
            outputTimeStep = 60;
        }
        rainRunoffRaining.jumpsRunCompleteToAsciiFileSCSSerial(storm.stormInitialTimeInMinutes() + numPeriods * storm.stormRecordResolutionInMinutes(), finalSimulationTime.getTimeInMillis() / 1000.0 / 60.0, outputTimeStep, initialCondition, newfile, linksStructure, thisNetworkGeom, newfile1, newfile3, newfile4, newfile5, writeorder);
        // Parallel - rainRunoffRaining.jumpsRunCompleteToAsciiFileSCS(storm.stormInitialTimeInMinutes() + numPeriods * storm.stormRecordResolutionInMinutes(), finalSimulationTime.getTimeInMillis() / 1000.0 / 60.0, outputTimeStep, initialCondition, newfile, linksStructure, thisNetworkGeom, newfile1, newfile3, newfile4, outflag);
        newfile1.close();
        bufferout1.close();

        newfile3.close();
        bufferout3.close();
        newfile4.close();
        bufferout4.close();
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime = new java.util.Date();
        System.out.println("End Time:" + endTime.toString());
        System.out.println("Running Time:" + (.001 * (endTime.getTime() - startTime.getTime())) + " seconds");
        System.out.println("Finish second part," + x + "," + y + "," + (.001 * (endTime.getTime() - startTime.getTime())) + " seconds");

        double[] maximumsAchieved = rainRunoffRaining.getMaximumAchieved();
        double[] timeToMaximumsAchieved = rainRunoffRaining.getTimeToMaximumAchieved();

        newfile.write("\n");
        newfile.write("\n");
        newfile.write("Maximum Discharge [m^3/s],");
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
        newfile.write("\n");
        newfile.write("Maximum surface storage [mm],");

        for (int i = linksStructure.contactsArray.length; i < 2 * linksStructure.contactsArray.length; i++) {
            if (Double.isNaN(maximumsAchieved[i])) {
                maximumsAchieved[i] = -9.9;
            }
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }

        newfile.write("\n");
        newfile.write("Maximum water table [mm],");


        for (int i = 2 * linksStructure.contactsArray.length; i < 3 * linksStructure.contactsArray.length; i++) {
            if (Double.isNaN(maximumsAchieved[i])) {
                maximumsAchieved[i] = -9.9;
            }
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }

        newfile.write("\n");
        newfile.write("Maximum theta [%],");


        for (int i = 3 * linksStructure.contactsArray.length; i < 4 * linksStructure.contactsArray.length; i++) {
            if (Double.isNaN(maximumsAchieved[i])) {
                maximumsAchieved[i] = -9.9;
            }
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }
        newfile.write("\n");

        newfile.write("Evaporation [mm],");
        for (int i = 4 * linksStructure.contactsArray.length; i < 5 * linksStructure.contactsArray.length; i++) {
            if (Double.isNaN(maximumsAchieved[i])) {
                maximumsAchieved[i] = -9.9;
            }
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }
        newfile.write("\n");

        newfile.write("Precip [mm],");
        for (int i = 5 * linksStructure.contactsArray.length; i < 6 * linksStructure.contactsArray.length; i++) {
            if (Double.isNaN(maximumsAchieved[i])) {
                maximumsAchieved[i] = -9.9;
            }
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }


        newfile.write("\n");

        newfile.write("Overland flow [mm],");
        for (int i = 6 * linksStructure.contactsArray.length; i < 7 * linksStructure.contactsArray.length; i++) {
            if (Double.isNaN(maximumsAchieved[i])) {
                maximumsAchieved[i] = -9.9;
            }
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }
        newfile.write("\n");

        newfile.write("Baseflow [mm],");
        for (int i = 7 * linksStructure.contactsArray.length; i < 8 * linksStructure.contactsArray.length; i++) {
            if (Double.isNaN(maximumsAchieved[i])) {
                maximumsAchieved[i] = -9.9;
            }
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }

        newfile.write("\n");

        newfile.write("Snow [mm],");
        for (int i = 8 * linksStructure.contactsArray.length; i < 9 * linksStructure.contactsArray.length; i++) {
            if (Double.isNaN(maximumsAchieved[i])) {
                maximumsAchieved[i] = -9.9;
            }
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }

        newfile.write("\n");
        newfile.write("\n");

        newfile.write("Precipitation Rates [mm/hr],");

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
        for (int k = 0; k < numPeriods; k++) {
            double currTime = storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();
            newfile.write(df2.format(currTime) + ",");
            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
                newfile.write(df2.format(thisHillsInfo.precipitation(i, currTime)) + ",");
                //newfile.write(df2.format(thisHillsInfo.precipitationacum(i, currTime)) + ",");

            }

            newfile.write("\n");

        }

        newfile.write("Evaporation \n");
        for (int k = 0; k < numPeriods; k++) {
            double currTime = PotEVPT.PotEVPTInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();
            newfile.write(df2.format(currTime) + ",");
            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
                newfile.write(df2.format(thisHillsInfo.PotEVPT(i, currTime)) + ",");
                //newfile.write(df2.format(thisHillsInfo.precipitationacum(i, currTime)) + ",");

            }

            newfile.write("\n");

        }

        newfile.write("SnowMelt \n");
        for (int k = 0; k < numPeriods; k++) {
            double currTime = PotEVPT.PotEVPTInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();
            newfile.write(df2.format(currTime) + ",");
            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
                newfile.write(df2.format(thisHillsInfo.SnowMelt(i, currTime)) + ",");
                //newfile.write(df2.format(thisHillsInfo.precipitationacum(i, currTime)) + ",");

            }

            newfile.write("\n");

        }

        newfile.close();
        bufferout.close();

        interTime = new java.util.Date();
        System.out.println("Finish writing precipitation," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        //ATTENTION
        //The followng print statement announces the completion of the program.
        //DO NOT modify!  It tells the queue manager that the process can be
        //safely killed.
        System.out.println("Termina escritura de Resultados");
        return;


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


            //subMain_Bo(args);
            subMain_Bo2(args);
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v) {
            System.out.print(v);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void subMain_Bo(String args[]) throws java.io.IOException, VisADException {
        System.out.println("start parameters definition");
        // Define DEM and outlet of the basin
        String DEM = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        int xOut = 2817; // ClearCreek
        int yOut = 713;// ClearCreek

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

        // Define Rainfall
        int yr = 2008;
        //String stormFileSrt = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/shortTEST/NEXRAD_BC.metaVHC";
        //for the long term simulaton - 
        String stormFileSrt = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/" + yr + "04TO09/NEXRAD_BC.metaVHC";
        java.io.File stormFile = new java.io.File(stormFileSrt);
        java.io.File PotEVPTFile = new java.io.File("null");
        java.io.File SNOWMeltFile = new java.io.File("null");
        java.io.File SNOWSWEFile = new java.io.File("null");
        java.io.File SOILMFile = new java.io.File("null");


        java.util.Hashtable routingParams = new java.util.Hashtable();

        // Define land use

        int writeorder = 1; // wrhite results for links higher than disc


        // DEFINE THE INITIAL AND FINAL TIME OF THE SIMULATION

        java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
        java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
        alphaSimulationTime.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        omegaSimulationTime.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        // Long term simulations
        alphaSimulationTime.set(yr, 2, 01, 0, 0, 0);// from mar 03,01
        omegaSimulationTime.set(yr, 9, 01, 0, 0, 0);// from oct 10,01
        //short test
        //alphaSimulationTime.set(yr, 5, 12, 0, 0, 0);// from mar 03,01
        //omegaSimulationTime.set(yr, 5, 14, 0, 0, 0);// from oct 10,01
        long BegTime = alphaSimulationTime.getTimeInMillis();
        long EndTime = omegaSimulationTime.getTimeInMillis();

        // parameters previously defined by Ricardo, not used for Luciana
        routingParams.put("widthCoeff", 1.0f);
        routingParams.put("widthExponent", 0.4f);
        routingParams.put("widthStdDev", 0.0f);
        routingParams.put("chezyCoeff", 14.2f);
        routingParams.put("chezyExponent", -1 / 3.0f);
        routingParams.put("SoilMoisture", 2.f);
        routingParams.put("lambdaSCSMethod", 0.0f);
        routingParams.put("Vconst", 0.5f); // CHANGE IN THE NETWORKEQUATION CLA
        routingParams.put("P5Condition", -9.0f); // Porcentage of the soil filled up with water
        routingParams.put("ReserVolume", 0.0f); // m3/km2 of reservation
        routingParams.put("ReserVolumetType", 1.0f); // reservoir position:
        routingParams.put("dynaIndex", 3);
        routingParams.put("BaseFlowCoef", 0.0f); // 
        routingParams.put("BaseFlowExp", 0.40f); // 
        routingParams.put("OutFlag", 2f);
        routingParams.put("CQflood", 4.0f); // 
        routingParams.put("EQflood", 0.51f); // reservoir position:
        routingParams.put("hillshapeparamflag", 0.0f);
        // parameters that are used in the model
        routingParams.put("vrunoff", -9.f); // define a constant hillslope velocity or -9.9 for non-linear vel=f(land Cover)
        routingParams.put("vssub", -9.f); // define a constant hydraulic conductivity or -9.9 for non-linear ksat=f(based on soil properties)
        routingParams.put("RunoffCoefficient", -9f); //define a constant value for RC or -9.9 for space-time RC
        routingParams.put("MinPondedWater", 0.0f);
        routingParams.put("PorcHSaturated", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("PorcPhiUnsat", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("KunsatCte", 1.0f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("ETolerance", 0.001f);
        routingParams.put("EVcoef", 1.0f);
        routingParams.put("Coefvh", 1.0f);
        routingParams.put("Coefks", 1.0f);
        routingParams.put("CoefAi", 1.0f);
        routingParams.put("ConstSoilStorage", -9.0f);  // check NetworkEquationsLuciana.java for definitions
        // Velocity parameters 
        float v_o_data = 0.75f;
        float coefCorr = 0.5f;
        float v_o = 0.75f;
        routingParams.put("v_o", coefCorr * v_o_data);
        routingParams.put("lambda1", 0.25f);
        routingParams.put("lambda2", -0.15f);
        // Define the model to run
        routingParams.put("HillT", 20262f);  // define the hillslope model - check NetworkEquationsLuciana.java for definitions
        routingParams.put("HillVelocityT", 7f); // check NetworkEquationsLuciana.java for definitions
        routingParams.put("RoutingT", 5f); // 5 for nonlinear and 2 for constant
        routingParams.put("KunsatModelType", 0f);
        System.out.println("finish parameters definition");
////       // for constant runoff coefficient with hillslope delay
////                routingParams.put("HillT", 1);  
////                routingParams.put("vrunoff", -9.f); // define a constant hillslope velocity or -9.9 for non-linear vel=f(land Cover)
////                routingParams.put("RunoffCoefficient", 0.5f);
////                
////       /////////////////////////////////////////////////         
////       // for constant runoff coefficient without hillslope delay
////           
////                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
////                routingParams.put("RunoffCoefficient", 0.5);


        java.io.File outputDirectory;
        System.out.println("define output directory");
        outputDirectory = new java.io.File("/scratch/Users/rmantill/ClearCreekBo/"
                + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                + "RoutT_" + ((Float) routingParams.get("RoutingT")).floatValue() + "/"
                + "HillT_" + ((Float) routingParams.get("HillT")).floatValue() + "/"
                + "HillVelT_" + ((Float) routingParams.get("HillVelocityT")).floatValue() + "/"
                + "/KuT_" + ((Float) routingParams.get("KunsatModelType")).floatValue() + "/"
                + "EV_" + ((Float) routingParams.get("EVcoef")).floatValue() + "/"
                + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                + "/Cks_" + ((Float) routingParams.get("Coefks")).floatValue()
                + "/KuC_" + ((Float) routingParams.get("KunsatCte")).floatValue()
                + "/RC" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                + "/MinPon_" + ((Float) routingParams.get("MinPondedWater")).floatValue()
                + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                + "/Cvh_" + ((Float) routingParams.get("Coefvh")).floatValue()
                + "/vo_" + ((Float) routingParams.get("v_o")).floatValue() + "_" + ((Float) routingParams.get("lambda1")).floatValue() + "_" + ((Float) routingParams.get("lambda2")).floatValue());
        System.out.println(outputDirectory);

        int rrt = (int) ((Float) routingParams.get("RoutingT")).floatValue();
        outputDirectory.mkdirs();
        java.util.Date StartTime = new java.util.Date();
        System.out.println("Start Simulation");
        new SimulationToFileSerialMay2012(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, SNOWMeltFile, SNOWSWEFile, SOILMFile, 0.0f, rrt, routingParams, outputDirectory, BegTime, EndTime, writeorder).executeSimulation();


        //                 new SimulationToFileSerialVersion(xOut, yOut, matDirs, magnitudes, metaModif, stormFile, 0.0f, outputDirectory, new java.io.File(LandUse), new java.io.File(SoilData),new java.io.File(Soil150SWAData), WriteOrder, routingParams).executeSimulation();



    }

    public static void subMain_Bo2(String args[]) throws java.io.IOException, VisADException {
        System.out.println("start parameters definition");
        // Define DEM and outlet of the basin
        String DEM = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        int xOut = 2817; // ClearCreek
        int yOut = 713;// ClearCreek
        //String StringDEM = hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ParallelSimulationToFileHelium.defineDEMxy("ClearCreek", "90DEMLIDAR");
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

        // Define Rainfall
        int yr = 2008;
        //String stormFileSrt = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/shortTEST/NEXRAD_BC.metaVHC";
        //for the long term simulaton - 
        String stormFileSrt = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/" + yr + "04TO09/NEXRAD_BC.metaVHC";
        java.io.File stormFile = new java.io.File(stormFileSrt);
        java.io.File PotEVPTFile = new java.io.File("/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/8DaysData/Data4SimLong/PET/" + yr + "/IowaPET.metaVHC");
        java.io.File SNOWMeltFile = new java.io.File("null");
        java.io.File SNOWSWEFile = new java.io.File("null");
        java.io.File SOILMFile = new java.io.File("null");
        


        java.util.Hashtable routingParams = new java.util.Hashtable();

        // Define land use

        int writeorder = 1; // wrhite results for links higher than disc


        // DEFINE THE INITIAL AND FINAL TIME OF THE SIMULATION

        java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
         alphaSimulationTime.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
      
        java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
         omegaSimulationTime.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
      
        // Long term simulations
        alphaSimulationTime.set(yr, 2, 01, 0, 0, 0);// from mar 03,01
        omegaSimulationTime.set(yr, 9, 01, 0, 0, 0);// from oct 10,01
        //short test
        //alphaSimulationTime.set(yr, 5, 12, 0, 0, 0);// from mar 03,01
        //omegaSimulationTime.set(yr, 5, 14, 0, 0, 0);// from oct 10,01
        long BegTime = alphaSimulationTime.getTimeInMillis();
        long EndTime = omegaSimulationTime.getTimeInMillis();

        // parameters previously defined by Ricardo, not used for Luciana - do not worry about it!
        routingParams.put("widthCoeff", 1.0f);
        routingParams.put("widthExponent", 0.4f);
        routingParams.put("widthStdDev", 0.0f);
        routingParams.put("chezyCoeff", 14.2f);
        routingParams.put("chezyExponent", -1 / 3.0f);
        routingParams.put("SoilMoisture", 2.f);
        routingParams.put("lambdaSCSMethod", 0.0f);
        routingParams.put("Vconst", 0.5f); // CHANGE IN THE NETWORKEQUATION CLA
        routingParams.put("P5Condition", -9.0f); // Porcentage of the soil filled up with water
        routingParams.put("ReserVolume", 0.0f); // m3/km2 of reservation
        routingParams.put("ReserVolumetType", 1.0f); // reservoir position:
        routingParams.put("dynaIndex", 3);
        routingParams.put("BaseFlowCoef", 0.0f); // 
        routingParams.put("BaseFlowExp", 0.40f); // 
        routingParams.put("OutFlag", 2f);
        routingParams.put("CQflood", 4.0f); // 
        routingParams.put("EQflood", 0.51f); // reservoir position:
        routingParams.put("hillshapeparamflag", 0.0f);

        // Define the model to run
        // based on the files that have all the equations (NetworkEquations_AllMethodSerialMay_2012
        routingParams.put("HillT", 20262f);  // check NetworkEquations_AllMethodSerialMay_2012 for definitions
        routingParams.put("HillVelocityT", 7f); // check NetworkEquations_AllMethodSerialMay_2012 for definitions
        routingParams.put("RoutingT", 5f); // 5 for nonlinear and 2 for constant
        routingParams.put("KunsatModelType", 0f);

        // parameters that are used in the model 
        routingParams.put("vrunoff", -9.f); // define a constant hillslope velocity or -9.9 for non-linear vel=f(land Cover)
        routingParams.put("vssub", -9.f); // define a constant hydraulic conductivity or -9.9 for non-linear ksat=f(based on soil properties)
        routingParams.put("RunoffCoefficient", -9f); //define a constant value for RC or -9.9 for space-time RC
        routingParams.put("MinPondedWater", 0.0f);
        routingParams.put("PorcHSaturated", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("PorcPhiUnsat", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("KunsatCte", 1.0f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("ETolerance", 0.001f);
        routingParams.put("EVcoef", 1.0f);
        routingParams.put("Coefvh", 1.0f);
        routingParams.put("Coefks", 1.0f);
        routingParams.put("CoefAi", 1.0f);
        routingParams.put("ConstSoilStorage", -9.0f);  // check NetworkEquationsLuciana.java for definitions
        // Velocity parameters 
        float v_o_data = 0.75f;
        float coefCorr = 0.5f;
        float v_o = 0.75f;
        routingParams.put("v_o", coefCorr * v_o_data);
        routingParams.put("lambda1", 0.25f);
        routingParams.put("lambda2", -0.15f);
        System.out.println("finish parameters definition");
////       // for constant runoff coefficient with hillslope delay
////                routingParams.put("HillT", 1);  
////                routingParams.put("vrunoff", -9.f); // define a constant hillslope velocity or -9.9 for non-linear vel=f(land Cover)
////                routingParams.put("RunoffCoefficient", 0.5f);
////                
////       /////////////////////////////////////////////////         
////       // for constant runoff coefficient without hillslope delay
////           
////                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
////                routingParams.put("RunoffCoefficient", 0.5);


        java.io.File outputDirectory;
        System.out.println("define output directory");
        outputDirectory = new java.io.File("/scratch/Users/rmantill/ClearCreekBo/"
                + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                + "RoutT_" + ((Float) routingParams.get("RoutingT")).floatValue() + "/"
                + "HillT_" + ((Float) routingParams.get("HillT")).floatValue() + "/"
                + "HillVelT_" + ((Float) routingParams.get("HillVelocityT")).floatValue() + "/"
                + "/KuT_" + ((Float) routingParams.get("KunsatModelType")).floatValue() + "/"
                + "EV_" + ((Float) routingParams.get("EVcoef")).floatValue() + "/"
                + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                + "/Cks_" + ((Float) routingParams.get("Coefks")).floatValue()
                + "/KuC_" + ((Float) routingParams.get("KunsatCte")).floatValue()
                + "/RC" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                + "/MinPon_" + ((Float) routingParams.get("MinPondedWater")).floatValue()
                + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                + "/Cvh_" + ((Float) routingParams.get("Coefvh")).floatValue()
                + "/vo_" + ((Float) routingParams.get("v_o")).floatValue() + "_" + ((Float) routingParams.get("lambda1")).floatValue() + "_" + ((Float) routingParams.get("lambda2")).floatValue());
        System.out.println(outputDirectory);

        int rrt = (int) ((Float) routingParams.get("RoutingT")).floatValue();
        outputDirectory.mkdirs();
        java.util.Date StartTime = new java.util.Date();
        System.out.println("Start Simulation");
        new SimulationToFileSerialMay2012(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, SNOWMeltFile, SNOWSWEFile, SOILMFile, 0.0f, rrt, routingParams, outputDirectory, BegTime, EndTime, writeorder).executeSimulation();


        //                 new SimulationToFileSerialVersion(xOut, yOut, matDirs, magnitudes, metaModif, stormFile, 0.0f, outputDirectory, new java.io.File(LandUse), new java.io.File(SoilData),new java.io.File(Soil150SWAData), WriteOrder, routingParams).executeSimulation();



    }
}
