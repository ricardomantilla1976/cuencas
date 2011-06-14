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
 * ParallelSimulationToFile.java
 *
 * Created on January 27, 2009, 8:00 AM
 */
package hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion;

import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class ParallelSimulationToFileHelium extends java.lang.Object {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    public int threadsRunning = 0;
    java.util.Hashtable<String, Boolean> procNames = new java.util.Hashtable<String, Boolean>();
    private java.util.Calendar zeroSimulationTime, endingSimulationTime;
    float lam1, lam2, v_o;
    int dynaIndex;
    String WorkD;

    /** Creates new ParallelSimulationToFile */
    public ParallelSimulationToFileHelium() {
    }

    public ParallelSimulationToFileHelium(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, int dscale, String WD) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, horOrders, md, rainIntensity, rainDuration, null, null, null, infiltRate, routingType, routingParams, outputDirectory, zST, eST, dscale, WD);
    }

    public ParallelSimulationToFileHelium(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, java.io.File PotEVPTFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, int dscale, String WD) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, horOrders, md, 0.0f, 0.0f, stormFile,  PotEVPTFile, infiltMetaRaster, 0.0f, routingType, routingParams, outputDirectory, zST, eST, dscale, WD);
    }

    public ParallelSimulationToFileHelium(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, java.io.File PotEVPTFile, float infiltRate, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, int dscale, String WD) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, horOrders, md, 0.0f, 0.0f, stormFile, PotEVPTFile, null, infiltRate, routingType, routingParams, outputDirectory, zST, eST, dscale, WD);
    }

    public ParallelSimulationToFileHelium(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, java.io.File stormFile, java.io.File PotEVPTFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, float infiltRate, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, int dscale, String WD) throws java.io.IOException, VisADException {

        zeroSimulationTime = zST;
        endingSimulationTime = eST;
        matDir = direcc;
        metaDatos = md;

        lam1 = ((Float) routingParams.get("lambda1")).floatValue();
        lam2 = ((Float) routingParams.get("lambda2")).floatValue();
        v_o = ((Float) routingParams.get("v_o")).floatValue();
        WorkD = WD;

        java.io.File Workdir = new java.io.File(WorkD);

        java.io.File[] files = Workdir.listFiles();
        for (java.io.File file : files) {
            file.delete();
        }
        System.out.println("delete all files from " + WorkD);
        String idd = WorkD.substring(WorkD.lastIndexOf("s") + 1, WorkD.length() - 1);
        System.out.println("idd" + idd);

        int dynaIndex = 0;

        //System.exit(0);
        if (routingParams.get("dynaIndex") != null) {
            dynaIndex = ((Integer) routingParams.get("dynaIndex")).intValue();
        }
        System.out.println("main - dynaindex=" + dynaIndex);

        //Fractioning

        System.out.println("In parallel " + routingParams.values());
        System.out.println("In parallel " + routingParams.keySet());


        java.util.Hashtable<String, Integer> processList = new java.util.Hashtable();
        java.util.Hashtable<Integer, Integer> topoMapping = new java.util.Hashtable();

        hydroScalingAPI.util.geomorphology.objects.Basin laCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDir, metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(laCuenca, metaDatos, matDir);
        hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRsnGen = new hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition(mylinksAnalysis);

        float[][] linkAreas = mylinksAnalysis.getVarValues(0);
        float[][] linkLenghts = mylinksAnalysis.getVarValues(1);

        int decompScale = dscale;

        int[][] headsTails = myRsnGen.getHeadsAndTails(decompScale);
        int[][] connectionTopology = myRsnGen.getPrunedConnectionStructure(decompScale);
        int[] connectionLinks = myRsnGen.getConnectingLinks(decompScale);
        float[][] dToO = mylinksAnalysis.getDistancesToOutlet();

        int ncols = md.getNumCols();

        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(9);
        System.out.println("Connection topology length " + connectionTopology.length);

        for (int i = 0; i < connectionTopology.length; i++) {
            processList.put(labelFormat.format(dToO[0][connectionLinks[i]]) + "-" + labelFormat.format(i), i);
            topoMapping.put(connectionLinks[i], i);
        }

        // Sort hashtable.
        java.util.Vector v = new java.util.Vector(processList.keySet());
        java.util.Collections.sort(v);
        java.util.Collections.reverse(v);

        for (Iterator it = v.iterator(); it.hasNext();) {
            String key = (String) it.next();
            Integer val = (Integer) processList.get(key);
            System.out.println("Key: " + key + "     Val: " + val);
        }

        Thread[] activeThreads = new Thread[connectionTopology.length];
        hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ExternalTileToFileHelium[] externalExecutors = new hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ExternalTileToFileHelium[connectionTopology.length];

        float maxDtoO = Float.parseFloat(((String) v.get(0)).substring(0, 9));




        for (int i = 0; i < headsTails[0].length; i++) {
            int xOutlet = headsTails[0][i] % ncols;
            int yOutlet = headsTails[0][i] / ncols;

            int xSource = headsTails[2][i] % ncols;
            int ySource = headsTails[2][i] / ncols;

            if (headsTails[3][i] == 0) {
                xSource = -1;
                ySource = -1;
            }

            System.out.println("Process " + i + " " + xOutlet + " " + yOutlet + " " + xSource + " " + ySource);
            System.out.println(java.util.Arrays.toString(connectionTopology[i]));

            String connectionString = "C";
            String correctionString = "F";


            int[] connectionIDs = new int[connectionTopology[i].length];

            for (int j = 0; j < connectionTopology[i].length; j++) {
                System.out.print(topoMapping.get(connectionTopology[i][j]) + " ");
                connectionIDs[j] = headsTails[0][topoMapping.get(connectionTopology[i][j])];
                connectionString += "," + connectionIDs[j];
            }
            System.out.println();

            //Setting up threads

            float[] corrections = new float[2];

            int connectingLink = mylinksAnalysis.getLinkIDbyHead(xSource, ySource);
            if (connectingLink != -1) {
                corrections[0] = linkAreas[0][connectingLink];
                corrections[1] = linkLenghts[0][connectingLink];
                correctionString += "," + corrections[0] + "," + corrections[1];
            }
            System.out.println(java.util.Arrays.toString(connectionIDs));
            System.out.println(java.util.Arrays.toString(corrections));

            externalExecutors[i] = new hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ExternalTileToFileHelium("El " + i + "-" + idd, WorkD, md.getLocationMeta().getAbsolutePath(), xOutlet, yOutlet, xSource, ySource, decompScale, routingType, lam1, lam2, v_o, stormFile.getAbsolutePath(), PotEVPTFile.getAbsolutePath(), infiltRate, outputDirectory.getAbsolutePath(), connectionString, correctionString, this, zeroSimulationTime.getTimeInMillis(), endingSimulationTime.getTimeInMillis(), dynaIndex, routingParams);
        }

        boolean allNodesDone = true;

        java.util.Date startTime = new java.util.Date();
        System.out.println("Start Time - Total:" + startTime.toString());
        System.out.println("Parallel Code Begins");

        float currentDtoO = maxDtoO;
        //int LimProcesses=400;
        // System.exit(0);
        int count = 0;
        int nprocer = 0;
        do {
            System.out.print(">> Processes on Hold: ");
            System.out.println("externalExecutors.length = " + externalExecutors.length + "\n");
            for (int i = 0; i < externalExecutors.length; i++) {

                int xOutlet = headsTails[0][i] % ncols;
                int yOutlet = headsTails[0][i] / ncols;

                if (new java.io.File(outputDirectory.getAbsolutePath() + "/Tile_" + xOutlet + "_" + yOutlet + ".done").exists()) {
                    externalExecutors[i].completed = true;
                    externalExecutors[i].executing = false;

                } else {
                    externalExecutors[i].check_in_s++;
                }

                int indexProc = processList.get((String) v.get(i));
                System.out.println("indexProc = " + indexProc + " ");

                System.out.println("completed = " + externalExecutors[indexProc].completed + "  executing = " + externalExecutors[indexProc].executing + "\n");
                if (externalExecutors[indexProc].completed == false && externalExecutors[indexProc].executing == false) {
                    boolean required = true;
                    for (int j = 0; j < connectionTopology[indexProc].length; j++) {
                        required &= externalExecutors[((Integer) topoMapping.get(connectionTopology[indexProc][j])).intValue()].completed;
                    }
                    if (required) {

                        //simThreads[indexProc].executeSimulation();

                        System.out.println();
                        System.out.println(">> Process " + indexProc + " is being launched");
                        externalExecutors[indexProc].executeCommand();
                        count = 0;
                        externalExecutors[indexProc].executing = true;

                    } else {
                        System.out.print(">> Process " + indexProc + " didnt reach requirements");
                    }
                } else if (externalExecutors[indexProc].completed == true) {
                    activeThreads[indexProc] = null;
                }
            }

            System.out.print(">> Running");
            for (int i = 0; i < externalExecutors.length; i++) {
                if (externalExecutors[i].executing) {
                    System.out.print(", " + i);
                }
            }
            System.out.println();
            // wait for ten second
            new visad.util.Delay(10000);
            count = count + 1;
//            if (count > 10 * 36) {
//                System.out.println("More than " + 10 * 36 * 10000 / 3600 + "waiting --- " + " check processes that already run");
//                for (int i = 0; i < externalExecutors.length; i++) {
//
//                    externalExecutors[i].executing = false;
//                    externalExecutors[i].completed = false;
//                    System.out.println("Did not Run - start over" + i + "\n");
//                }
//                count = 0;
//            }
            allNodesDone = true;
            for (int i = 0; i < externalExecutors.length; i++) {
                allNodesDone &= externalExecutors[i].completed;
            }
            //System.exit(1);
        } while (!allNodesDone);

        System.out.println("Parallel Code Ends");
        java.util.Date endTime = new java.util.Date();
        System.out.println("End Time Total:" + endTime.toString());
        System.out.println("Parallel Running Time Total:" + routingParams.toString());
        System.out.println("Parallel Running Time Total:" + (.001 * (endTime.getTime() - startTime.getTime())) + " seconds");

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {


        try {
            subMainALL(args);
            //subMainDEM_TTime(args);
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v) {
            System.out.print(v);
            System.exit(0);
        }

        System.exit(0);

    }

    //////////CLEARCREEK1_AGU/////////////
    public static void subMainALL(String args[]) throws java.io.IOException, VisADException {

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};


        String WorkDir = "/scratch/qsubBin/tempScripts/";
        java.io.File WorkDirectory = new java.io.File(WorkDir);
        int n = 1;
        while (WorkDirectory.exists() == true) {
            //System.out.println("file exists" + WorkDir);
            n = n + 1;
            WorkDir = "/scratch/qsubBin/tempScripts" + n + "/";
            WorkDirectory = new java.io.File(WorkDir);
        }

        System.out.println("file does not exist, Create: " + WorkDir);

        WorkDirectory.mkdirs();


        String[] AllSimName = {"90DEMUSGS"};
        
        String[] AllRain = {"3IowaCity2002"};
        //,"3CedarRapids2005","3CedarRapids2004","3CedarRapids2003","3CedarRapids2002","3CedarRapids2007","3CedarRapids2008","3CedarRapids2009"
        
        String Direc="MultipleYears6";
       
        //"3ClearCreekBOhour2_2", ,"3ClearCreekBOSTAGEIV_2"
        int HillT=12;
        int flaghill=22;

        int nsim = AllSimName.length;

        int nbas = AllRain.length;

        for (int i = 0; i < nsim; i++) {
            for (int ib = 0; ib < nbas; ib++) {

                System.out.println("Running BASIN " + AllSimName[i]);
                System.out.println("Running BASIN " + AllRain[ib]);

                String SimName = AllSimName[i];
                String BasinName = AllRain[ib];
                java.io.File outputDirectory;

                // DEFINE DEM
                String[] StringDEM = {"error", "error", "error"};
                StringDEM = defineDEMxy(BasinName, SimName);
                System.out.println("StringDEM = " + StringDEM[0]);
                System.out.println("x = " + StringDEM[1] + "    y" + StringDEM[2]);
                String DEM = StringDEM[0];
                int xOut = Integer.parseInt(StringDEM[1]);//   .getInteger(StringDEM[1]);
                int yOut = Integer.parseInt(StringDEM[2]);

                //System.exit(1);

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

                //DEFINE DISCRETIZATION FOR PARALLEL CODE

                int disc = 4;
                disc = definedisc(BasinName, SimName);
                System.out.println("disc =  " + disc);
                // DEFINE THE STORM FILE
                java.io.File stormFile;
                java.io.File PotEVPTFile;
                
                
                java.util.Hashtable routingParams = new java.util.Hashtable();

                stormFile = new java.io.File(definestorm(BasinName, SimName));
                PotEVPTFile = new java.io.File(definePotEVPT(BasinName, SimName));

                // DEFINE THE INITIAL TIME OF THE SIMULATION
                java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);


                // DEFINE THE FINAL TIME OF THE SIMULATION
                java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);

                if (BasinName.indexOf("Adv") >= 0) {
                    if (BasinName.indexOf("May") >= 0) {
                        alphaSimulationTime.set(2009, 4, 13, 0, 0, 0);
                        omegaSimulationTime.set(2009, 4, 22, 0, 0, 0);
                    }
                    if (BasinName.indexOf("Mar") >= 0) {
                        alphaSimulationTime.set(2009, 2, 6, 0, 0, 0);
                        omegaSimulationTime.set(2009, 2, 15, 0, 0, 0);
                    }
                    if (BasinName.indexOf("Jun") >= 0) {
                        alphaSimulationTime.set(2009, 5, 9, 0, 0, 0);
                        omegaSimulationTime.set(2009, 5, 30, 0, 0, 0);
                    }
                }


                int index_2 = BasinName.indexOf("20");

                if (BasinName.indexOf("20") > 0) {
                    String timeStamp = BasinName.substring(index_2, index_2 + 4);
                    int yr = java.lang.Integer.parseInt(timeStamp);
                    alphaSimulationTime.set(yr, 4, 01, 0, 0, 0);// from may 01
                    omegaSimulationTime.set(yr, 7, 10, 0, 0, 0);// to agost 10
                    if (BasinName.indexOf("test") > 0) {
                        alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);// from may 01
                        omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);
                        stormFile = new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/ascii2008VHCtest/NEXRAD_BC.metaVHC");
                    }

                }

                if (BasinName.indexOf("BO") > 0) {
                    alphaSimulationTime.set(2002, 7, 20, 0, 0, 0);// from may 01
                    omegaSimulationTime.set(2002, 7, 26, 0, 0, 0);
                }

                alphaSimulationTime.getTimeInMillis();
                omegaSimulationTime.getTimeInMillis();
                System.out.println("storm = " + stormFile.getAbsolutePath());

                routingParams.put("v_o", 0.88f);
                routingParams.put("lambda1", 0.25f);
                routingParams.put("lambda2", -0.15f);

                routingParams.put("widthCoeff", 1.0f);
                routingParams.put("widthExponent", 0.4f);
                routingParams.put("widthStdDev", 0.0f);
                routingParams.put("chezyCoeff", 14.2f);
                routingParams.put("chezyExponent", -1 / 3.0f);
                routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("vssub", 1.0f);
                routingParams.put("SoilMoisture", 2.f);
                routingParams.put("lambdaSCSMethod", 0.0f);
                routingParams.put("Vconst", 0.5f); // CHANGE IN THE NETWORKEQUATION CLA
                routingParams.put("P5Condition", -9.0f); // Porcentage of the soil filled up with water
                routingParams.put("ReserVolume", 0.0f); // m3/km2 of reservation
                routingParams.put("ReserVolumetType", 1.0f); // reservoir position:
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:
                routingParams.put("PondedWater", 0.0f);
                routingParams.put("dynaIndex", 3);
                //               Initial condition
                routingParams.put("PorcHSaturated", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("PorcPhiUnsat", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("BaseFlowCoef", 0.0f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("BaseFlowExp", 0.40f); // define a constant number or -9.9 for vel=f(land Cover)

//C1111111 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0


                float[] BasinFlagAr = {0.0f};
                for (float BF : BasinFlagAr) {

                    float bflag = BF;

                    routingParams.put("Basin_sim", bflag); // Cedar river - define Land cover and soil parameter
                    //float[] vsAr = {0.0002f,0.0005f};
                    float[] vsAr = {};
                    //-8 is with average VS
                    //-9is with minimal VS
                    float[] PH = {0.0f};
                    float[] Phi = {0.0f};
                    float[] IaAre = {0.4f};
                    float[] IaArc = {0.0f};
                    float[] l1Ar = {0.15f};
                    //float[] rcAr = {-9.f};
                    //float[] vhAr = {-9.f};

                    float[] rcAr = {-9.f};
                    float[] vhAr = {-9.f};
                    float[] Coefvo = {1.0f};
                    float[] HSAr = {0.f};
                    float[] CteSAr = {-9f};
                    float[] VostdAr = {0.0f};
                    float[] kf1Ar = {1.0f};
                    float[] EVAr = {1.0f};




                    for (float l11 : l1Ar) {
                        for (float ev : EVAr) {
                        for (float vo1 : Coefvo) {
                            float cvo = vo1;
                            float l1 = l11;
                            float l2 = -0.82f * l11 + 0.1025f;
                            float vo = cvo * (0.42f - 0.29f * l11 - 3.1f * l2);
                            l2=0.05f;
                            vo=cvo *0.18f;

                            for (float Kf1 : kf1Ar) {

                                for (float ia1e : IaAre) {
                                    float iae = ia1e;
                                    for (float ia1c : IaArc) {
                                        float iac = ia1c;

                                        for (float rc1 : rcAr) {
                                            float rc = rc1;
                                            for (float vh1 : vhAr) {
                                                float vh = vh1;
                                                for (float hs1 : HSAr) {
                                                    float hs = hs1;
                                                    for (float cts1 : CteSAr) {
                                                        float cts = cts1;


                                                        for (float in1 : PH) {
                                                            float p1 = in1;
                                                            for (float in2 : Phi) {
                                                                float p2 = in2;

                                                                for (float voo : vsAr) {
                                                                    float vsub = voo;

                                                                    for (float Vostd : VostdAr) {
                                                                        
                                                                        routingParams.put("CQflood", 8.0f); // reservoir position:
                                                                        
                                                                        routingParams.put("EQflood", 0.51f); // reservoir position:
                                                          
                                                                        //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                                        if (rc >= 0.f) {
                                                                            if(flaghill==1){
                                                                            routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                            routingParams.put("RunoffCoefficient", rc);} // reservoir position:
                                                                            else {
                                                                            routingParams.put("HillT", 22);  // check NetworkEquationsLuciana.java for definitions
                                                                            routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                            }  
                                                                        } else {
                                                                            routingParams.put("HillT", HillT);
                                                                            routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                                        }                                                         //                         routingParams.put("lambda1", l11);
                                                                        routingParams.put("EVcoef", ev); // reservoir position:
                
                                                                        routingParams.put("Vostd", Vostd);
                                                                        routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                                        routingParams.put("v_o", vo);
                                                                        routingParams.put("lambda1", l11);
                                                                        routingParams.put("lambda2", l2);
                                                                        routingParams.put("PorcHSaturated", p1);
                                                                        routingParams.put("vssub", vsub);
                                                                        routingParams.put("PorcPhiUnsat", p2);
                                                                        routingParams.put("lambdaSCSMethod", iae);
                                                                        
                                                                        routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                                        routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)
                                                                        routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                                                                        routingParams.put("hillshapeparamflag", hs);
                                                                        routingParams.put("floodplaincte", Kf1);  // check NetworkEquationsLuciana.java for definitions
                                                                        routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions

                                                                        if (vh == -8) {
                                                                routingParams.put("HillVelocityT", 2);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("vrunoff", vh);
                                                            } else if (vh == -9) {
                                                                routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("vrunoff", vh);
                                                            } else {
                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                            }
                                                            if (vh == -7) {
                                                                 routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("vrunoff", vh);
                                                                routingParams.put("HillT", 0);
                                                                routingParams.put("RunoffCoefficient", rc); 
                                                            }
                                                            

                                                                        //routingParams.put("RunoffCoefficient", rc); // reservoir position:ccc
                                                                        // + "/BFExp" + ((Float) routingParams.get("BaseFlowExp")).floatValue()
                                                                        //                                                                     + "/BFCoef" + ((Float) routingParams.get("BaseFlowCoef")).floatValue()

                                                                        // outputDirectory = new java.io.File("/scratch/results_cuencas/Helium_version/" + BasinName + "/" + SimName + "/" + bflag + "/"

                                                                        outputDirectory = new java.io.File("/scratch/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"                                                                         
                                                                                + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                                                + "CQf_" + ((Float) routingParams.get("CQflood")).floatValue() + "/"
                                                                                + "EV_" + ((Float) routingParams.get("EVcoef")).floatValue() + "/"                                                            
                                                                                + "Vostd_" + ((Float) routingParams.get("Vostd")).floatValue()
                                                                                + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                                                + "/RC" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                                                                                + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                                                + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                                                + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                                                                                + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                                                + "/hh_" + ((Float) routingParams.get("hillshapeparamflag")).floatValue()
                                                                                + "/kf1_" + ((Float) routingParams.get("floodplaincte")).floatValue()
                                                                                + "/vo_" + ((Float) routingParams.get("v_o")).floatValue() + "_" + ((Float) routingParams.get("lambda1")).floatValue() + "_" + ((Float) routingParams.get("lambda2")).floatValue());
                                                                        //FAST TEST
                                                                        //outputDirectory = new java.io.File("/scratch/luciana/Parallel/testHeliumversion/");
                                                                        // alphaSimulationTime.set(2010, 0, 0, 0, 0, 0);
                                                                        // omegaSimulationTime.set(2010, 0, 2, 0, 0, 0);
                                                                        // stormFile = new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/thesis2/60mm/15min/vhc/60mm15min.metaVHC");

                                                                        int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                                        outputDirectory.mkdirs();

                                                                        new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);

                                                                        //System.exit(0);
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
                        }
                    }
                    float[] vsAr2 = {-9.f};
                     float[] PH2 = {0.0f};
                    float[] Phi2 = {0.0f};
                    float[] IaAre2 = {0.02f};

                    float[] IaArc2 = {0.0f};
                    //float[] IaArc = {0.0f,0.1f,1.0f};

                    float[] voAr2 = {0.6f};

                    //float[] rcAr2 = {-9.f};
                    //float[] vhAr2 = {-9.f};

                    float[] rcAr2 = {-9.f};
                    float[] vhAr2 = {-6.f};


                    float[] HSAr2 = {0.f};
                    float[] CteSAr2 = {-9.f};
                    float[] EVAr2 = {1.0f};

                    //                   float l1 = l11;
                    //                   float l2 = -0.82f * l11 + 0.1025f;
                    //                   float vo = 0.42f - 0.29f * l11 - 3.1f * l2;
         for (float cts1 : CteSAr2) {
                                                        float cts = cts1;
                      for (float rc1 : rcAr2) {
                                                float rc = rc1;                       
                           for (float voo : vsAr2) {
                        float vsub = voo;
                        for (float ev1 : EVAr2) {
                        for (float vh1 : vhAr2) {
                            float vh = vh1;

                            for (float in1 : PH2) {
                                float p1 = in1;
                                for (float in2 : Phi2) {
                                    float p2 = in2;
                                    for (float ia1e : IaAre2) {
                                        float iae = ia1e;
                                        for (float ia1c : IaArc2) {
                                            float iac = ia1c;

                                          
                                                for (float hs1 : HSAr2) {
                                                    float hs = hs1;
                                                        for (float vo1 : voAr2) {
                                                            float vo = vo1;
                                                     
                                                            //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
    routingParams.put("CQflood", 4.0f); // reservoir position:
                                                             
                                                                      
                                                           routingParams.put("EQflood", 0.51f); // reservoir position:
                                                          
                                                            routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                            routingParams.put("v_o", vo);
                                                            routingParams.put("hillshapeparamflag", hs);
                                                            routingParams.put("HillT", HillT);  // check NetworkEquationsLuciana.java for definitions
                                                            routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions

                                                              if (rc >= 0.f) {
                                                                            if(flaghill==1){
                                                                            routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                            routingParams.put("RunoffCoefficient", rc);} // reservoir position:
                                                                            else {
                                                                            routingParams.put("HillT", 22);  // check NetworkEquationsLuciana.java for definitions
                                                                            routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                            }  
                                                                      } else {
                                                                routingParams.put("HillT", HillT);
                                                                routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                            }                                                         //                         routingParams.put("lambda1", l11);
                                                              routingParams.put("EVcoef", ev1); // reservoir position:
                
                                                            //                         routingParams.put("lambda2", l2);
                                                            routingParams.put("PorcHSaturated", p1);
                                                            routingParams.put("vssub", vsub);
                                                            routingParams.put("PorcPhiUnsat", p2);
                                                            routingParams.put("lambdaSCSMethod", iae);
                                                            routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                            routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)

                                                            if (vh == -8) {
                                                                routingParams.put("HillVelocityT", 2);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("vrunoff", vh);
                                                            } else if (vh == -6) {
                                                                routingParams.put("HillVelocityT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("vrunoff", vh);
                                                            }
                                                            
                                                            else if (vh == -9) {
                                                                routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("vrunoff", vh);
                                                            } else if (vh == -10) {
                                                                routingParams.put("HillVelocityT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("vrunoff", vh);
                                                            } else {
                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                            }
                                                            if (vh == -7) {
                                                                 routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                routingParams.put("HillT", 0);
                                                                routingParams.put("RunoffCoefficient", rc); 
                                                            }
                                                            
                                                            
                                                            routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions
                                                            routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                            outputDirectory = new java.io.File("/scratch/results_cuencas/" + Direc + "/"  + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                    + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                    + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                    + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                                    + "CQf_" + ((Float) routingParams.get("CQflood")).floatValue() + "/"
                                                                    + "EV_" + ((Float) routingParams.get("EVcoef")).floatValue() + "/"                                                            
                                                                    + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                                    + "/RC" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                                                                    + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                                    + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                                    + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                                                                    + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                                    + "/hh_" + ((Float) routingParams.get("hillshapeparamflag")).floatValue()
                                                                    + "/vcte_" + ((Float) routingParams.get("v_o")).floatValue());
if(HillT==11){
                                                                     outputDirectory = new java.io.File("/scratch/results_cuencas/" + Direc + "/"  + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                    + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                    + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/" 
                                                                    + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()+ "/"
                                                                    + "LSCS_" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue() + "/"                                                            
                                                                    + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                                    + "/RC" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                                                                    + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                                    + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                                    + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                                                                    + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                                    + "/hh_" + ((Float) routingParams.get("hillshapeparamflag")).floatValue()
                                                                    + "/vcte_" + ((Float) routingParams.get("v_o")).floatValue());
                                                        }
//                                            + "RR_" + ((Float) routingParams.get("RunoffCoefficient")).floatValue() +"/"
                                                            // + "VHILL_" + ((Float) routingParams.get("vrunoff")).floatValue()

                                                            int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                            outputDirectory.mkdirs();
                                                            new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile,0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);
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
            }
        }
    }

    public static String[] defineDEMxy(String BasinName, String SimName) {

        // DEFINE THE DEM and x and y
        int xOut = 2817;
        int yOut = 713; //90METERDEMClear Creek - coralville
        String[] OUTPUT = {"error", "errorx", "errory"};
        if (SimName.contains("ASTER")) {
            OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM";
            xOut = 1596;
            yOut = 298;
        }
        if (SimName.equals("5DEMLIDAR")) {
            xOut = 8052;
            yOut = 497;
            OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/5meters/5meterc1.metaDEM";
        }
        if (SimName.equals("10DEMLIDAR")) {
            xOut = 4025;
            yOut = 244;
            OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM";
        }
        if (SimName.equals("20DEMLIDAR")) {
            xOut = 2013;
            yOut = 122;
            OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/20meters/20meterc1.metaDEM";
        }
        if (SimName.equals("30DEMLIDAR")) {
            xOut = 1341;
            yOut = 82;
            OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/30meters/30meterc1.metaDEM";
        }
        if (SimName.equals("60DEMLIDAR")) {
            xOut = 670;
            yOut = 41;
            OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/60meters/60meterc1.metaDEM";
        }
        if (SimName.equals("90DEMLIDAR")) {
            xOut = 447;
            yOut = 27;
            OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM";
        }
        if (SimName.equals("90DEMUSGS")) {
            xOut = 2817;
            yOut = 713;
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (SimName.equals("30DEMUSGS")) {
            xOut = 1541;
            yOut = 92;
            OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM";
        }

        if (SimName.equals("10DEMUSGS")) {
            xOut = 4624;
            yOut = 278;
            OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM";
        }

        if (BasinName.indexOf("Cedar") >= 0) {
            
            xOut = 2734;
            yOut = 1069; //Cedar Rapids
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
            
        }

        if (BasinName.indexOf("Waverly") >= 0) {
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
            xOut = 1770;
            yOut = 1987;
        }

        if (BasinName.indexOf("Iowa") >= 0) {
            xOut = 2885;
            yOut = 690;
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }


        if (BasinName.indexOf("Turkey") >= 0) {

            xOut = 3053;
            yOut = 2123;
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (BasinName.indexOf("Volga") >= 0) {
            xOut = 3091;
            yOut = 2004;
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (BasinName.indexOf("Hoover") >= 0) {
            xOut = 3113;
            yOut = 705;
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if(SimName.indexOf("90DEMUSGS")>=0 && SimName.indexOf("Prun5")>=0)
        {
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun5/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
             if(SimName.indexOf("90DEMUSGS")>=0 && SimName.indexOf("Prun6")>=0)
        {
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun6/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
             if(SimName.indexOf("90DEMUSGS")>=0 && SimName.indexOf("Prun7")>=0)
        {
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun7/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
               if(SimName.indexOf("90DEMUSGS")>=0 && SimName.indexOf("Prun8")>=0)
        {
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun8/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
               
        
                
        if (SimName.indexOf("120DEMUSGS") >= 0) {
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs120m.metaDEM";
            if (BasinName.indexOf("Clear") >= 0) {
                xOut = 2113;
                yOut = 535;
            }
            if (BasinName.indexOf("Iowa") >= 0) {
                xOut = 2164;
                yOut = 517;
            }

            if (BasinName.indexOf("Cedar") >= 0) {
                xOut = 2050;
                yOut = 802;
            }
        }


        if (SimName.indexOf("180DEMUSGS") >= 0) {
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs180m.metaDEM";
            if (BasinName.indexOf("Clear") >= 0) {
                xOut = 1409;
                yOut = 356;
            }
            if (BasinName.indexOf("Cedar") >= 0) {
                xOut = 1367;
                yOut = 534;
            }

            if (BasinName.indexOf("Iowa") >= 0) {
                xOut = 1443;
                yOut = 345;
            }

        }

        if (SimName.indexOf("90DEMASTER") >= 0) {
            OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/30meterASTER/90master.metaDEM";
            if (BasinName.indexOf("Clear") >= 0) {
                xOut = 2807;
                yOut = 643;
            }
            if (BasinName.indexOf("Cedar") >= 0) {
                xOut = 2723;
                yOut = 997;
            }

            if (BasinName.indexOf("Iowa") >= 0) {
                xOut = 2873;
                yOut = 617;
            }

        }

        if (SimName.indexOf("30DEMUSGS") >= 0) {

            if (BasinName.indexOf("Clear") >= 0) {
                OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM";
                xOut = 8288;
                yOut = 1029;
            }

            if (BasinName.indexOf("Cedar") >= 0) {
                OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM";
                xOut = 7875;
                yOut = 1361;
            }

            if (BasinName.indexOf("Iowa") >= 0) {
                OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM";
                xOut = 8443;
                yOut = 1029;

            }
            
               if (BasinName.indexOf("IowaUps") >= 0) {
                OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM";
                xOut = 5505;
                yOut = 1871;

            }

            if (BasinName.indexOf("Waverly") >= 0) {
                OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM";
                xOut = 5468;
                yOut = 3237;
            }

        }
        OUTPUT[1] = Integer.toString(xOut);
        OUTPUT[2] = Integer.toString(yOut);

        System.out.println("OUTPUT[0] = " + OUTPUT[0]);
        System.out.println("OUTPUT[1] = " + OUTPUT[1]);
        System.out.println("OUTPUT[2] = " + OUTPUT[2]);
        return OUTPUT;
    }

    public static int definedisc(String BasinName, String SimName) {
        int disc = 4;
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 5;
        }


        if (BasinName.indexOf("Turkey") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 4;
        }



        if (BasinName.indexOf("Volga") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 4;
        }

        if (BasinName.indexOf("Water") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 4;
        }

        if (BasinName.indexOf("Iowa") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 4;
        }

        if (BasinName.indexOf("Janes") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 4;
        }

     

        if (BasinName.indexOf("disc4") > 0) {
            disc = 4;
        }
        
         if (BasinName.indexOf("disc3") > 0) {
            disc = 3;
        }

        if (BasinName.indexOf("Hoover") > 0) {
            disc = 2;
        }
        
        if(BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun8")>=0) disc=5;
        if(BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun7")>=0) disc=4;
        if(BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun6")>=0) disc=3;
        if(BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun5")>=0) disc=2;
        if(BasinName.indexOf("Clear") > 0 && SimName.indexOf("Prun8")>=0) disc=3;
        if(BasinName.indexOf("Clear") > 0 && SimName.indexOf("Prun7")>=0) disc=2;
        
        return disc;
    }

    public static String definestorm(String BasinName, String SimName) {
        String stormFile = "error";
        stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC";
        //1 space and 180 time
        if (BasinName.indexOf("1_180") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/180min/Time/Bin/hydroNexrad.metaVHC";
        }
        //6 space and 15 time
        if (BasinName.indexOf("6_15") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/15min/Time/Bin/hydroNexrad.metaVHC";
        }
        
         if (BasinName.indexOf("15_360") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/360min/Time/Bin/hydroNexrad.metaVHC";
        }
        //6 space and 180 tim
        if (BasinName.indexOf("6_180") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("15_15") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/15min/Time/Bin/hydroNexrad.metaVHC";
        }

        if (BasinName.indexOf("15_180") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/180min/Time/Bin/hydroNexrad.metaVHC";
        }

        if (BasinName.indexOf("6_180") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("1_60") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/60min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("2_60") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/2/60min/Time/Bin/hydroNexrad.metaVHC";
        }

        if (BasinName.indexOf("6_60") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/60min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("15_60") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/60min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("ori") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("PERS") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/PERSIANN/May29Jun17/PERSIANN_3h.metaVHC";
        }
        if (BasinName.indexOf("TRMM") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/RC1.00/bin/rain17/prec.metaVHC";
        }
        if (BasinName.indexOf("NOBIASCORR") > 0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/DataGenNov_2010/Radar_merged_Vhc/NEXRAD_BC.metaVHC";
        }
        if (BasinName.indexOf("BO") > 0) {
            if(BasinName.indexOf("hour")>0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/Bo_events/20Aug2002_24Aug2002VHC/NEXRAD_BC.metaVHC";
            }
            if(BasinName.indexOf("hour2")>0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/Bo_events/20Aug2002_24Aug2002VHC2/NEXRAD_BC.metaVHC";
            }
            
            if(BasinName.indexOf("5min")>0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/Bo_events/Rain5minVHCFixed/NEXRAD_BC.metaVHC";
            }
            if(BasinName.indexOf("STAGEIV")>0) {
            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/Bo_events/RadarSTAGEIV/NEXRAD_BC.metaVHC";
            }
        }
        if (BasinName.indexOf("Adv") > 0) {
            if (BasinName.indexOf("May") > 0) {

                stormFile = "/scratch/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/May2009_AdvectionVHC/NEXRAD_BC.metaVHC";
                if (BasinName.indexOf("No") > 0) {
                    stormFile = "/scratch/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/May2009_NoAdvectionVHC/NEXRAD_BC.metaVHC";
                }
            }
            if (BasinName.indexOf("Mar") > 0) {
                stormFile = "/scratch/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_AdvectionVHC/NEXRAD_BC.metaVHC";
                if (BasinName.indexOf("No") > 0) {
                    stormFile = "/scratch/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_NoAdvectionVHC/NEXRAD_BC.metaVHC";
                }
            }
            if (BasinName.indexOf("Jun") > 0) {
                stormFile = "/scratch/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/Jun2009_AdvectionVHC/NEXRAD_BC.metaVHC";
                if (BasinName.indexOf("No") > 0) {
                    stormFile = "/scratch/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/Jun2009_NoAdvectionVHC/NEXRAD_BC.metaVHC";
                }
            }
        }

        //stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC";
        int index_2 = BasinName.indexOf("20");

        if (BasinName.indexOf("20") > 0) {
            String timeStamp = BasinName.substring(index_2, index_2 + 4);
            int yr = java.lang.Integer.parseInt(timeStamp);

            stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/" + yr + "VHC/NEXRAD_BC.metaVHC";

            if (BasinName.indexOf("PER") > 0) {
                stormFile = "//scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/PERSIANN/vhc_sim_period/" + yr + "/PERSIANN_3h.metaVHC";
            }
            System.out.println("year  " + yr);

//
        }
        return stormFile;

    }
    
       public static String definePotEVPT(String BasinName, String SimName) {
 
        String PotEVPTFile = "error";
        PotEVPTFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/8DaysData/Data4Sim2/PET/2008Short/IowaPET.metaVHC";
       
        if (BasinName.indexOf("BO") > 0) {
            if(BasinName.indexOf("hour")>0) {
            PotEVPTFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/8DaysData/Data4Sim2/PET/2008Short/IowaPET.metaVHC";
            }
            
        }
        
     

        //stormFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC";
        int index_2 = BasinName.indexOf("20");

        if (BasinName.indexOf("20") > 0) {
            String timeStamp = BasinName.substring(index_2, index_2 + 4);
            int yr = java.lang.Integer.parseInt(timeStamp);

            PotEVPTFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/8DaysData/Data4Sim2/PET/"+yr+"/IowaPET.metaVHC";

            System.out.println("year  " + yr);

//
        }
        return PotEVPTFile;

    }
}
