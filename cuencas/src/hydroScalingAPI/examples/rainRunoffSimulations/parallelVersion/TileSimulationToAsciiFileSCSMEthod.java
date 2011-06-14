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
package hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion;

import java.io.IOException;
import visad.*;
import java.text.DecimalFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo Mantilla
 */
public class TileSimulationToAsciiFileSCSMEthod extends java.lang.Object implements Runnable {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir, hortonOrders;
    int x, y, xH, yH, scale;
    int[][] magnitudes;
    float rainIntensity;
    float rainDuration;
    java.io.File stormFile;
    java.io.File PotEVPTFile;
    hydroScalingAPI.io.MetaRaster infiltMetaRaster;
    float infiltRate;
    int routingType, HillType, HillVelType;
    java.io.File outputDirectory;
    java.util.Hashtable routingParams;
    int[] usConnections;
    int basinOrder;
    float[] corrections;
    private java.util.Calendar zeroSimulationTime;
    private java.util.Calendar finalSimulationTime;

    // Add for the SCS
    int greenroof;
    java.io.File GreenFile;
    java.io.File LandUseFile;
    java.io.File SoilFile;
    float LandUseFileFlag;
    float SoilFileFlag;
    float IniCondition;

    public TileSimulationToAsciiFileSCSMEthod(int xx,
            int yy,
            int xxHH,
            int yyHH,
            int scaleO,
            byte[][] direcc,
            int[][] magnitudesOR,
            byte[][] horOrders,
            hydroScalingAPI.io.MetaRaster md,
            float rainIntensityOR,
            float rainDurationOR,
            java.io.File stormFileOR,
            java.io.File PotEVPTFileOR,
            hydroScalingAPI.io.MetaRaster infiltMetaRasterOR,
            float infiltRateOR,
            int routingTypeOR,
            java.util.Hashtable rP,
            java.io.File outputDirectoryOR,
            int[] connectionsO,
            float[] correctionsO,
            long zST,long fST) throws java.io.IOException, VisADException {




        zeroSimulationTime = java.util.Calendar.getInstance();
        zeroSimulationTime.setTimeInMillis(zST);

        finalSimulationTime = java.util.Calendar.getInstance();
        finalSimulationTime.setTimeInMillis(fST);

        matDir = direcc;
        metaDatos = md;

        x = xx;
        y = yy;
        xH = xxHH;
        yH = yyHH;
        scale = scaleO;

        magnitudes = magnitudesOR;
        hortonOrders = horOrders;
        rainIntensity = rainIntensityOR;
        rainDuration = rainDurationOR;
        stormFile = stormFileOR;
        PotEVPTFile = PotEVPTFileOR;
        infiltMetaRaster = infiltMetaRasterOR;
        infiltRate = infiltRateOR;
        routingType = routingTypeOR;
        outputDirectory = outputDirectoryOR;
        routingParams = rP;
        usConnections = connectionsO;

        corrections = correctionsO;

    }

    public void executeSimulation() throws java.io.IOException, VisADException {

        //Here an example of rainfall-runoff in action
        System.out.println("Start executSimulation \n");
        java.util.Date startTime = new java.util.Date();
        System.out.println("Start Tile 1," + x + "," + y + "," + startTime.toString());
        System.out.println("Running Time:" + "0.0" + " seconds");
        java.io.File theFile;
        theFile=new java.io.File(outputDirectory.getAbsolutePath() + "/Tile_"+x+"_"+y+".done");
        System.out.println(theFile);

        if (theFile.exists()) {

            //ATTENTION
            //The followng print statement announces the completion of the program.
            //DO NOT modify!  It tells the queue manager that the process can be
            //safely killed.
            System.out.println("Termina escritura de Resultados"  + x + "," + y);
            writeReportFile(outputDirectory.getAbsolutePath(),x,y);
            return;
        }


        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDir, metaDatos);

        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile myTileActual = new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile(x, y, xH, yH, matDir, hortonOrders, metaDatos, scale);
        myCuenca.setXYBasin(myTileActual.getXYRsnTile());


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
        float widthCoeff=0.0f;
        float widthExponent =0.0f;
        float widthStdDev=0.0f;
        float chezyCoeff = 0.0f;
        float chezyExponent = 0.0f;
        float lam1 =0.0f;
        float lam2 = 0.0f;
        float v_o = 0.0f;
        float IniHSatPorc =0.0f;
        float IniUnSatPorc = 0.0f;
        float BaseFlowCoef =0.0f;
        float BaseFlowExp = 0.0f;
        float PoundedW = 0.0f;

        if (routingParams.get("widthCoeff") != null)  widthCoeff= ((Float) routingParams.get("widthCoeff")).floatValue();
        if (routingParams.get("widthExponent") != null)   widthExponent = ((Float) routingParams.get("widthExponent")).floatValue();
        if (routingParams.get("widthStdDev") != null)   widthStdDev = ((Float) routingParams.get("widthStdDev")).floatValue();

        if (routingParams.get("chezyCoeff") != null)   chezyCoeff = ((Float) routingParams.get("chezyCoeff")).floatValue();
        if (routingParams.get("chezyExponent") != null)   chezyExponent = ((Float) routingParams.get("chezyExponent")).floatValue();

        thisNetworkGeom.setWidthsHG(widthCoeff, widthExponent, widthStdDev);
        thisNetworkGeom.setCheziHG(chezyCoeff, chezyExponent);

        if (routingParams.get("lambda1") != null)  lam1 = ((Float) routingParams.get("lambda1")).floatValue();
        if (routingParams.get("lambda2") != null)   lam2 = ((Float) routingParams.get("lambda2")).floatValue();

        if (routingParams.get("v_o") != null)   v_o = ((Float) routingParams.get("v_o")).floatValue();

        if (routingParams.get("PorcHSaturated") != null)   IniHSatPorc = ((Float) routingParams.get("PorcHSaturated")).floatValue();
        if (routingParams.get("PorcPhiUnsat") != null)   IniUnSatPorc = ((Float) routingParams.get("PorcPhiUnsat")).floatValue();

        if (routingParams.get("BaseFlowCoef")!= null)   BaseFlowCoef = ((Float) routingParams.get("BaseFlowCoef")).floatValue();
        if (routingParams.get("BaseFlowExp")!= null)   BaseFlowExp = ((Float) routingParams.get("BaseFlowExp")).floatValue();
        if (routingParams.get("PondedWater")!= null)  PoundedW=((Float) routingParams.get("PondedWater")).floatValue();
        float kf1=0.1f;
        if (routingParams.get("floodplaincte")!= null) kf1= ((Float) routingParams.get("floodplaincte")).floatValue();
        //System.out.println("lam1 " + lam1 +"lam2" + lam2 + "v_o" + v_o);

        // Modified SCS


        //System.out.println("routingtype \n");
        //System.out.println("hilltype" + ((Float)routingParams.get("HillType")).floatValue());
        //System.out.println("HillVelocityT" + ((Float)routingParams.get("HillVelocityT")).floatValue());
        //System.out.println("RoutingT" + ((Float)routingParams.get("RoutingT")).floatValue());
        //float ht=((Float)routingParams.get("HillT")).floatValue();  // check NetworkEquationsLuciana.java for definitions
        //float hvt=((Float)routingParams.get("HillVelocityT")).floatValue(); // check NetworkEquationsLuciana.java for definitions
        float P5 = 0.0f;
        if (routingParams.get("P5Condition") != null) {((Float) routingParams.get("P5Condition")).floatValue();}
        IniCondition=0.0f;
        if (routingParams.get("InitialCondition") != null) {IniCondition = ((Float) routingParams.get("InitialCondition")).floatValue();} // % of the saturated soil
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
        float v0std=0.f;
         if (routingParams.get("Vostd") != null) { v0std = ((Float) routingParams.get("Vostd")).floatValue();}
         


        thisNetworkGeom.setVqParams(v_o, v0std, lam1, lam2);


        interTime = new java.util.Date();
        System.out.println("Define thishillsinfo," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);



        interTime = new java.util.Date();
        System.out.println("Start storm," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;


        if (stormFile == null) {
            storm = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure, rainIntensity, rainDuration);
        } else {
            storm = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
        }

       hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager PotEVPT;


        if (PotEVPTFile == null) {
            PotEVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager(linksStructure, rainIntensity, rainDuration);
        } else {
            PotEVPT = new hydroScalingAPI.modules.rainfallRunoffModel.objects.PotEVPTManager(PotEVPTFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
        }
        
        
        interTime = new java.util.Date();
        System.out.println("Storm manager," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        storm.setStormInitialTime(zeroSimulationTime);
        PotEVPT.setPotEVPTInitialTime(zeroSimulationTime);
        if (!storm.isCompleted()) {
            return;
        }
        if (!PotEVPT.isCompleted()) {
            return;
        }

        thisHillsInfo.setStormManager(storm);
          interTime = new java.util.Date();
        System.out.println("Set Storm manager," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
 
         thisHillsInfo.setPotEVPTManager(PotEVPT);

        interTime = new java.util.Date();
        System.out.println("Set PotEVPT manager");


        hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infilMan = new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure, 0.0f);

        if (infiltMetaRaster == null) {
            infilMan = new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure, infiltRate);
        } else {
            infilMan = new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(myCuenca, linksStructure, infiltMetaRaster, matDir, magnitudes);
        }

        thisHillsInfo.setInfManager(infilMan);


        interTime = new java.util.Date();
        System.out.println("FSet Infil manager," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        int connectingLink = linksStructure.getLinkIDbyHead(xH, yH);

        if (connectingLink != -1) {
            thisHillsInfo.setArea(connectingLink, corrections[0]);
            thisNetworkGeom.setLength(connectingLink, corrections[1]);
        }
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
        if (routingParams.get("Basin_sim") != null) { BasinFlag = (int) ((Float) routingParams.get("Basin_sim")).floatValue();}
        String LandUse = "Error";
        String SoilData = "Error";

        // did not find a way to pass that without change ExternalTileToFile
        // This has to be fixed
        if (BasinFlag == 0) {
            LandUse = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            SoilData = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }

        if (BasinFlag == 1) {
            LandUse = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/glomod90.metaVHC";
            SoilData = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }


        if (BasinFlag == 2) {
            LandUse = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverRestoringCropsTo10percentGrassland_90_2.metaVHC";
            SoilData = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }


        if (BasinFlag == 3) {
            LandUse = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverRestoringPastureTo10percentForest_90_2.metaVHC";
            SoilData = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }


        if (BasinFlag == 4) {
            LandUse = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverBaseline_90_2.metaVHC";
            SoilData = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }
        System.out.println("LandUse  " + LandUse + "\n");
        System.out.println("SoilData  " + SoilData + "\n");
        System.out.println("Start SCS processes \n");
        LandUseFile = new java.io.File(LandUse);
        SoilFile = new java.io.File(SoilData);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
        java.io.File DEMFile = metaDatos.getLocationMeta();

        interTime = new java.util.Date();
        System.out.println("Beg SCSObj," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes,HillShapeFlag);

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

        DecimalFormat df = new DecimalFormat("###");
        DecimalFormat df1 = new DecimalFormat("###.#");
        DecimalFormat df2 = new DecimalFormat("###.##");
        DecimalFormat df3 = new DecimalFormat("###.###");
        DecimalFormat df10 = new DecimalFormat("###.##########");

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
        newfile.write("SCS 1 [mm],");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(thisHillsInfo.SCS_S1(i)) + ",");
        }

        
     newfile.write("\n");
        newfile.write("Maximum inf rate 1 [mm/h],");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df3.format(thisHillsInfo.MaxInfRate(i)) + ",");
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
        System.out.println("Writing distance to the outlet 1");
        newfile.write("\n");
        newfile.write("Distance to outlet,");
        float[][] bigDtoO = linksStructure.getDistancesToOutlet();
        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df2.format(bigDtoO[1][i]) + ",");
        }
        System.out.println("Writing driver length");
        newfile.write("\n");
        newfile.write("River length,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df2.format(thisNetworkGeom.Length(i)) + ",");
        }
        
        System.out.println("Writing TOTAL RELIEF");
        newfile.write("\n");
        newfile.write("Total relief,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df2.format(thisHillsInfo.HillRelief(i)) + ",");
        }


        newfile.write("\n\n\n");
        newfile.write("Results of flow simulations in your basin");

        newfile.write("\n");
        newfile.write("Time,");

        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write("Link-" + df.format(i) + ",");
        }


        java.io.File[] filesToAdd = new java.io.File[usConnections.length];
        for (int i = 0; i < filesToAdd.length; i++) {
            String[] file1 = theFile.getAbsolutePath().split(demName + "_" + x + "_" + y + "-");
            int xCon = usConnections[i] % metaDatos.getNumCols();
            int yCon = usConnections[i] / metaDatos.getNumCols();
            filesToAdd[i] = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "_" + xCon + "_" + yCon + "-" + file1[1] + ".Outlet.csv");
        }

        interTime = new java.util.Date();
        System.out.println("Finish file," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");



        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquationsJune12_2011 thisBasinEqSys = new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquationsJune12_2011(linksStructure, thisHillsInfo, thisNetworkGeom, connectingLink, filesToAdd, routingParams);


        interTime = new java.util.Date();
        System.out.println("Finish equationobj," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");


        int NLI = linksStructure.contactsArray.length;
        double[] initialCondition;


   
            initialCondition = new double[NLI * 10];
//            if(HillType==7 || HillType==8) initialCondition = new double[NLI * 5];
//            else if (HillType==9) initialCondition = new double[NLI * 6];
//            else initialCondition = new double[NLI * 4];
      
        System.out.println("Start Initial condition");

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
            initialCondition[i + 2 * NLI] = 0.0;
            initialCondition[i + 3 * NLI] = IniUnSatPorc;
            initialCondition[i + 4 * NLI] = 0.0;
            initialCondition[i + 5 * NLI] = 0.0;
            initialCondition[i + 6 * NLI] = 0.0;
            initialCondition[i + 7 * NLI] = 0.0;
            initialCondition[i + 8 * NLI] = 0.0;
            initialCondition[i + 9 * NLI] = 0.0;
            //System.out.println("initialCondition[i]"+initialCondition[i]+"initialCondition[i + 1 * NLI]"+initialCondition[i + 1 * NLI]+"initialCondition[i + 2 * NLI]"+initialCondition[i + 2 * NLI]+"initialCondition[i + 3 * NLI]"+initialCondition[i + 3 * NLI]);
  
        }


        interTime = new java.util.Date();
        System.out.println("Create diff equation,," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");

        System.out.println("Number of Links on this simulation: " + NLI);
        System.out.println("Inicia simulacion RKF");

        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining = new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys, 1e-3, 10/60.);

        int numPeriods = Math.round(((float) storm.stormFinalTimeInMinutes() - (float) storm.stormInitialTimeInMinutes()) / (float) storm.stormRecordResolutionInMinutes());

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (storm.stormInitialTimeInMinutes() * 60. * 1000.0));
        System.out.println(thisDate.getTime());

  
         double outputTimeStep=storm.stormRecordResolutionInMinutes();
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
                rainRunoffRaining.jumpsRunCompleteToAsciiFileSCS(storm.stormInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes(), storm.stormInitialTimeInMinutes() + (k + 1) * storm.stormRecordResolutionInMinutes(), outputTimeStep, initialCondition, newfile, linksStructure, thisNetworkGeom, newfile1, newfile3,newfile4);
            initialCondition=rainRunoffRaining.finalCond;
            rainRunoffRaining.setBasicTimeStep(10 / 60.);
        }


        interTime = new java.util.Date();
        System.out.println("Running Time:" + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
        System.out.println("Finish first part," + x + "," + y + "," + (.001 * (interTime.getTime() - startTime.getTime())) + " seconds");
        //outputTimeStep=5*Math.pow(2.0D,(basinOrder-1));

        outputTimeStep=2*Math.pow(1.5D,(basinOrder-1));
        if(outputTimeStep>60) outputTimeStep=60;
            rainRunoffRaining.jumpsRunCompleteToAsciiFileSCS(storm.stormInitialTimeInMinutes() + numPeriods * storm.stormRecordResolutionInMinutes(), finalSimulationTime.getTimeInMillis()/1000.0/60.0, outputTimeStep, initialCondition, newfile, linksStructure, thisNetworkGeom, newfile1, newfile3, newfile4);
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
            if(Double.isNaN(maximumsAchieved[i])) maximumsAchieved[i]=-9.9;
            newfile.write(df3.format(maximumsAchieved[i]) + ",");
        }
        newfile.write("\n");
        newfile.write("Time to Maximum Discharge [minutes],");
        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
            newfile.write(df2.format(timeToMaximumsAchieved[i]) + ",");
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
                newfile.write(df2.format(thisHillsInfo.precipitation(i,currTime))+",");
                //newfile.write(df2.format(thisHillsInfo.precipitationacum(i, currTime)) + ",");

            }

            newfile.write("\n");

        }
        
        newfile.write("Evaporation \n");
        for (int k = 0; k < numPeriods; k++) {
            double currTime = PotEVPT.PotEVPTInitialTimeInMinutes() + k * storm.stormRecordResolutionInMinutes();
            newfile.write(df2.format(currTime) + ",");
            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
                newfile.write(df2.format(thisHillsInfo.PotEVPT(i,currTime))+",");
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
        writeReportFile(outputDirectory.getAbsolutePath(),x,y);
        return;
      


    }

    private void writeReportFile(String outputDir, int x, int y) throws java.io.IOException{
        new java.io.File(outputDir+"/Tile_"+x+"_"+y+".done").createNewFile();
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

            subMain0(args);
            
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v) {
            System.out.print(v);
            System.exit(0);
        }

    }

    public static void subMain0(String args[]) throws java.io.IOException, VisADException {


        java.io.File theFile = new java.io.File(args[0]);
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
        java.io.File PotEVPTFile;
        java.util.Hashtable routingParams = new java.util.Hashtable();
        
        int routingType = Integer.parseInt(args[6]);

        // args 16 contain the param table in the following format
        //{lambda2=-0.1, lambda1=0.2, widthStdDev=0.0, widthExponent=0.4, widthCoeff=1.0, P5Condition=-9.0, v_o=0.3, vssub=1.0, vrunoff=250.0, ReserVolume=0.0, RunoffCoefficient=1.0, HillVelocityT=3, Vconst=0.5, Basin_sim=0.0, InitialCondition=0.8, chezyExponent=-0.33333334, lambdaSCSMethod=0.05, chezyCoeff=14.2, HillT=4, SoilMoisture=2.0, dynaIndex=3, ReserVolumetType=1.0, RoutingT=5}]


//        int maxsize=args[16].length();
//        System.out.println("maxsize " + maxsize);
//        System.out.println("args[16] " + args[16]);
//        int indexend=2;
//        int indexbeg=1; // jump{
//        while (indexend<maxsize)
//        {
//            int indexegual=args[16].indexOf("=",indexbeg);
//            int indexcomma=args[16].indexOf(",",indexegual);
//            String str=args[16].substring(indexbeg,(indexegual-1));
//            String num=args[16].substring((indexegual+1),(indexcomma-1));
//            System.out.println("str " + str);
//
//            float num_f=Float.parseFloat(num);
//            System.out.println("num_f " + num_f);
//            routingParams.put(str,num_f);
//            indexbeg=indexcomma+1;
//            indexend=indexbeg+1;
//        }


        int num_ele = args.length;
        //System.out.println("num_elem  = " + args.length);
        int ig=0;
       // while (ig<num_ele) {System.out.println("args[" + ig + "] " + args[ig]); ig=ig+1;}

        //System.out.println("args[18].length()   = " +args[18].length());

        if (args[18].length() > 0) {
            int indexegual = args[18].indexOf("=", 1);
            int indexcomma = args[18].indexOf(",", indexegual);
            String str = args[18].substring(1, (indexegual));
            String num = args[18].substring((indexegual + 1), (indexcomma));
           // System.out.println("str " + str);
            float num_f = Float.parseFloat(num);
           // System.out.println(str + "    num_f " + num_f + "\n");
            routingParams.put(str, num_f);
        }

        ig = 19;
        while (ig < (num_ele - 1)) {
            int indexegual = args[ig].indexOf("=", 0);
            int indexcomma = args[ig].indexOf(",", indexegual);
            String str = args[ig].substring(0, (indexegual));
            String num = args[ig].substring((indexegual + 1), (indexcomma));
            //System.out.println("str " + str);
            float num_f = Float.parseFloat(num);
            //System.out.println(str + "    num_f " + num_f + "\n");
            routingParams.put(str, num_f);
            ig = ig + 1;
        }

        if (args[num_ele - 1].length() > 0) {
            int indexegual = args[num_ele - 1].indexOf("=", 1);
            int indexcomma = args[num_ele - 1].indexOf("}", indexegual);
            String str = args[num_ele - 1].substring(0, (indexegual));
            String num = args[num_ele - 1].substring((indexegual + 1), (indexcomma));
            //System.out.println("str " + str);
            float num_f = Float.parseFloat(num);
            //System.out.println(str + "    num_f " + num_f + "\n");
            //System.out.println("num_f " + num_f);
            routingParams.put(str, num_f);
        }
       // System.out.println("before define values using  = " + routingParams.toString());


        stormFile = new java.io.File(args[10]);
        PotEVPTFile = new java.io.File(args[11]);
       //System.out.println("stormFile  " + stormFile.getAbsolutePath());
       //System.out.println("PotEVPTFile  " + PotEVPTFile.getAbsolutePath());
        float infilRate = Float.parseFloat(args[12]);
//System.out.println("infilRate  " +  infilRate);
        java.io.File outputDirectory = new java.io.File(args[13]);
//System.out.println("outputDirectory  " +  outputDirectory.getAbsolutePath());
        int[] connO = new int[0];
        float[] corrO = new float[0];

        if (!args[14].equalsIgnoreCase("C")) {

            args[14] = args[14].substring(2);
            args[15] = args[15].substring(2);

            //System.out.println(args[14]);
            //System.out.println(args[15]);

            String[] conn = args[14].split(",");
            String[] corr = args[15].split(",");

           // System.out.println(java.util.Arrays.toString(conn));
           // System.out.println(java.util.Arrays.toString(corr));

            connO = new int[conn.length];
            for (int i = 0; i < connO.length; i++) {
                connO[i] = Integer.parseInt(conn[i].trim());
            }
            corrO = new float[corr.length];
            for (int i = 0; i < corrO.length; i++) {
                corrO[i] = Float.parseFloat(corr[i].trim());
            }
        }

        // read param files to change these values

        new TileSimulationToAsciiFileSCSMEthod(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), matDirs, magnitudes, horOrders, metaModif, 0.0f, 0.0f, stormFile, PotEVPTFile, null, infilRate, routingType, routingParams, outputDirectory, connO, corrO, Long.parseLong(args[16]),Long.parseLong(args[17])).executeSimulation();

    }

    
}
