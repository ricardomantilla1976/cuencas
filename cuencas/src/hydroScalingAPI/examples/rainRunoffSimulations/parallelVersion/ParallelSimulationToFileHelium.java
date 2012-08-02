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
 * This program sets the parameters to run the simulations in the helium cluster
 * and manages which sub-basins have been ran and which still have dependencies
 * depending on the structure of the river network
 * 
 * The following parameters are set:
 * - The watershed (x,y,DEM) - see function defineDEM
 * - Rainfall input - see function definestorm
 * - Discretization order for the sub-parallel processes - see function definedisc
 * - Potential evapotranspiration - see function definePotEVPT
 * - Snow melt - see function defineSNOW
 * - soil moisture - see function define SOILM
 * 
 * Land cover and soil properties are defined in the TileSimulationToAsciiFileSCSMEthod 
 * - runs with different land cover are set by the parameter "Basin_sim"
 * Once the 
 * (this should be changed and it should be define here too)
 * 
 * Created on January 27, 2009, 8:00 AM
 */
package hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion;

import java.util.Iterator;
import visad.*;
import java.util.TimeZone;

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
        this(x, y, direcc, magnitudes, horOrders, md, rainIntensity, rainDuration, null, null, null, null, null, null, infiltRate, routingType, routingParams, outputDirectory, zST, eST, dscale, WD);
    }

    public ParallelSimulationToFileHelium(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, java.io.File PotEVPTFile, java.io.File SNOWMeltFile, java.io.File SWEFile, java.io.File SOILMFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, int dscale, String WD) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, horOrders, md, 0.0f, 0.0f, stormFile, PotEVPTFile, SNOWMeltFile, SWEFile, SOILMFile, infiltMetaRaster, 0.0f, routingType, routingParams, outputDirectory, zST, eST, dscale, WD);
    }

    public ParallelSimulationToFileHelium(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, java.io.File PotEVPTFile, java.io.File SNOWMeltFile, java.io.File SWEFile, java.io.File SOILMFile, float infiltRate, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, int dscale, String WD) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, horOrders, md, 0.0f, 0.0f, stormFile, PotEVPTFile, SNOWMeltFile, SWEFile, SOILMFile, null, infiltRate, routingType, routingParams, outputDirectory, zST, eST, dscale, WD);
    }

    public ParallelSimulationToFileHelium(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, java.io.File stormFile, java.io.File PotEVPTFile, java.io.File SNOWMeltFile, java.io.File SWEFile, java.io.File SOILMFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, float infiltRate, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, int dscale, String WD) throws java.io.IOException, VisADException {
        
        
        zeroSimulationTime = zST;
        endingSimulationTime = eST;
        matDir = direcc;
        metaDatos = md;

        lam1 = ((Float) routingParams.get("lambda1")).floatValue();
        lam2 = ((Float) routingParams.get("lambda2")).floatValue();
        v_o = ((Float) routingParams.get("v_o")).floatValue();
        WorkD = WD;

        java.io.File Workdir = new java.io.File(WorkD);
        System.out.println("Work file " + WorkD);
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
        
        // Creates one process for each sub-basin
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

            externalExecutors[i] = new hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ExternalTileToFileHelium("El " + i + "-" + idd, WorkD, md.getLocationMeta().getAbsolutePath(), xOutlet, yOutlet, xSource, ySource, decompScale, routingType, lam1, lam2, v_o, stormFile.getAbsolutePath(), PotEVPTFile.getAbsolutePath(), SNOWMeltFile.getAbsolutePath(), SWEFile.getAbsolutePath(), SOILMFile.getAbsolutePath(), infiltRate, outputDirectory.getAbsolutePath(), connectionString, correctionString, this, zeroSimulationTime.getTimeInMillis(), endingSimulationTime.getTimeInMillis(), dynaIndex, routingParams);
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
    public static void main(String args[]) throws Exception {


        try {
           //subMainALL(args); // run all the years, or the 2008 event     
          //subMainRadar(args);
            //subMainRadar3(args); // run simulations for 2008 using the sampling rainfall
            //subMainSintetic(args);
            subMainSintetic2(args); // run simulations with synthetic rainfall
            

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
    public static void subMainALL(String args[]) throws java.io.IOException, VisADException, Exception {

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};


        String WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts/";
        java.io.File WorkDirectory = new java.io.File(WorkDir);
        int n = 1;
        while (WorkDirectory.exists() == true) {
            //System.out.println("file exists" + WorkDir);
            n = n + 1;
            WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts" + n + "/";
            WorkDirectory = new java.io.File(WorkDir);
        }

        System.out.println("file does not exist, Create: " + WorkDir);

        WorkDirectory.mkdirs();


        String[] AllSimName = {"90DEMUSGS"};
         //"250DEMUSGS","500DEMUSGS",
   //"1000DEMUSGSCA","2000DEMUSGSCA","5000DEMUSGSCA","10000DEMUSGSCA","nldaDEMUSGSCA","90DEMUSGSCA","120DEMUSGSCA","30DEMUSGSCA","180DEMUSGSCA","250DEMUSGSCA","500DEMUSGSCA",
//   "1000DEMUSGS","2000DEMUSGS","5000DEMUSGS","10000DEMUSGS","nldaDEMUSGS","90DEMUSGS","120DEMUSGS","30DEMUSGS","180DEMUSGS",
//   "10DEMLIDAR","20DEMLIDAR","30DEMLIDAR","60DEMLIDAR","90DEMLIDAR",
//        "5DEMLIDARCA","10DEMLIDARCA","20DEMLIDARCA","30DEMLIDARCA","60DEMLIDARCA","90DEMLIDARCA",
//        "90DEMUSGS","90DEMASTER","90DEMSRTM","90DEMUSGSPrun8","90DEMUSGSPrun7",
//   "90DEMUSGSPrun6","90DEMUSGSPrun5","120DEMUSGS","150DEMUSGS","180DEMUSGS",
   
        //String[] AllRain = {"3IowaCity2006Long_0200SNLDA",
        //    "3CedarRapids2006Long_0200SNLDA"};
        //String[] AllRain = {"shalehills"};
        int iy=2008;
//        String[] AllRain = { 
//"3CedarRapids2008Event_0200SNLDAHN1_60BR",
//"3CedarRapids2008Event_0200SNLDAHN1_60BR2",
//"3CedarRapids2008Event_0200SNLDAHN2_60BR",
// "3CedarRapids2008Event_0200SNLDANWS3", 
//"3CedarRapids2008Event_0200SNLDAHN3_60BR",
//"3CedarRapids2008Event_0200SNLDA",
//        "3CedarRapids2008Event_0200SNLDACMORPH",
//"3CedarRapids2008Event_0200SNLDAPERSIANN"
//        };
//String[] AllRain = { "3CedarRapids" +iy+"Long_0200SNLDA","3IowaCity" +iy+"Long_0200SNLDA","3Garber" +iy+"Long_0200SNLDA"};
        String[] AllRain = {"3IowaCity2008Event_0200SNLDA"};
 
            
//        String[] AllRain = { "3CedarRapids2008Long_0200SNLDA",
//                "3CedarRapids2008Long_0200SNLDAPERSIANN"
//                ,"3CedarRapids2008Long_0200SNLDACMORPH"
//                ,"3CedarRapids2008Long_0200SNLDATRMM"
//                ,"3CedarRapids2008Long_0200SNLDANLDASPrec"
//                ,"3CedarRapids2008Long_0200SNLDANWS2"};



        //String[] AllRain = {"3ClearCreek2008Event_0200SNLDAHN3_60BR"};
        //"3ClearCreek2005Long_0200SNLDA","3ClearCreek2006Long_0200SNLDA","3ClearCreek2007Long_0200SNLDA"};
//String[] AllRain = {"3ClearCreek2008Long_0200"};

        String Direc = "MultipleYearsLongDifModels2";
        Direc = "MultipleYearsLong40";

        //Direc = "MultipleDataset2008_v4";
        //Direc = "TestShalehills";
        java.util.Hashtable routingParams = new java.util.Hashtable();
        routingParams.put("Outflag", 2);

        //"3ClearCreekBOhour2_2", ,"3ClearCreekBOSTAGEIV_2"



        int nsim = AllSimName.length;

        int nbas = AllRain.length;
        int[] HillAr = {20262};
        for (int HillT : HillAr) {

            int flaghill = HillT;

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

                    System.out.println(BasinName);
                    System.out.println(SimName);

                    stormFile = new java.io.File(definestorm(BasinName, SimName));
                    System.out.println(BasinName);
                    System.out.println(SimName);
                    System.out.println(stormFile.toString());

                    //System.exit(1);
                    // DEFINE THE INITIAL TIME OF THE SIMULATION
                    java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
                    java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                    alphaSimulationTime.setTimeZone(tz);
                    alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);
                    

                    // DEFINE THE FINAL TIME OF THE SIMULATION
                    java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                    alphaSimulationTime.setTimeZone(tz);
                    omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);
                    
                    if (BasinName.indexOf("hills") >= 0) {

                        alphaSimulationTime.set(1974, 7, 1, 0, 0, 0);
                        omegaSimulationTime.set(1974, 8, 30, 0, 0, 0);
                    }
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
                        if (BasinName.indexOf("test") > 0 || BasinName.indexOf("Event") > 0) {
                            alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);// from may 01
                            omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);
                        }
                        if (BasinName.indexOf("Long") > 0) {
                            alphaSimulationTime.set(yr, 2, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 9, 01, 0, 0, 0);// from oct 10,01
                        }
                        if (BasinName.indexOf("Time1") > 0) {
                            alphaSimulationTime.set(yr, 3, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 5, 01, 0, 0, 0);// from oct 10,01
                        }
                        if (BasinName.indexOf("Time2") > 0) {
                            alphaSimulationTime.set(yr, 5, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 7, 01, 0, 0, 0);// from oct 10,01
                        }
                        if (BasinName.indexOf("Time3") > 0) {
                            alphaSimulationTime.set(yr, 7, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 9, 01, 0, 0, 0);// from oct 10,01
                        }

                    }

                    if (BasinName.indexOf("TEST") > 0) {
                        alphaSimulationTime.set(2008, 2, 01, 0, 0, 0);// from may 01
                        omegaSimulationTime.set(2008, 2, 10, 0, 0, 0);
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
                    routingParams.put("KunsatCte", 1.0f); // define a constant number or -9.9 for vel=f(land Cover)
//C1111111 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0
                    routingParams.put("ETolerance", 0.001f);
                    routingParams.put("OutFlag", 2);

                    float[] BasinFlagAr = {0.0f};
                    int[] PotEvapFlagAr = {1};

                    // Flag equal to 1 - NLDAS
                    // flag equal to 2 - MODIS - mandatory if using evaporation instead of potential evapo (HillT>3000)
                    for (float BF : BasinFlagAr) {
                        for (int PF : PotEvapFlagAr) {
                            int PotEvapFlag = PF;

                            PotEVPTFile = new java.io.File(definePotEVPT(BasinName, SimName, PotEvapFlag));
                            String[] Snow = defineSNOW(BasinName, SimName);
                            java.io.File SNOWMeltFile;
                            SNOWMeltFile = new java.io.File(Snow[0]);
                            java.io.File SWEFile;
                            SWEFile = new java.io.File(Snow[1]);
                            java.io.File SOILMFile;
                            SOILMFile = new java.io.File(defineSOILM(BasinName, SimName));

                            if (HillT == 0 || HillT == 1) {
                                SOILMFile = new java.io.File("null");
                                PotEVPTFile = new java.io.File("null");
                            }
                            float bflag = BF;

                            routingParams.put("Basin_sim", bflag); // Cedar river - define Land cover and soil parameter
                            //float[] vsAr = {0.0002f,0.0005f};
                            float[] vsAr = {-9.0f};
                            //-8 is with average VS
                            //-9is with minimal VS
                            float[] PH = {0.0f};
                            float[] Phi = {-9.0f};
                            //float[] Coefvo = {0.4f,0.8f,0.65f};
                            float[] Coefvo = {0.6f};

                           // if (BasinName.indexOf("Event") > 0) {
                           //     Coefvo[0] = 0.6f;
                           // }
                            if (BasinName.indexOf("Garber") > 0) {
                                Coefvo[0] = 0.8f;
                            }
                            System.out.println("Coefvo[0]" + Coefvo[0]);
                            //System.exit(1);
                            float[] coefvhAr = {1.0f};
                            float[] coefAiAr = {1.0f};
                            float[] coefksAr = {1.f};
                            float[] KunsatCAr = {5.0f};
                            float[] MinPoundAr = {0.0f};
                            float[] CteSAr = {-3.0f};
                            float[] IaAre = {0.4f};
                            float[] IaArc = {0.0f};
                            int[] VParamSet = {3};
                            int[] KunsatMAr = {0};
                            //float[] rcAr = {-9.f};
                            //float[] vhAr = {-9.f};

                            float[] rcAr = {-9.0f};
                            float[] vhAr = {-71.f};

                            float[] HSAr = {0.f};

                            float[] VostdAr = {0.0f};
                            float[] kf1Ar = {1.0f};
                            float[] EVAr = {1.0f};
                            for (float MinPound : MinPoundAr) {
                                for (float KunsatC : KunsatCAr) {



                                    for (float coefks : coefksAr) {
                                        for (float coefvh : coefvhAr) {

                                            for (float ev : EVAr) {
                                                for (float vo1 : Coefvo) {
                                                    for (int VP : VParamSet) {
                                                        for (float CoefAi : coefAiAr) {
                                                            for (int KunsatM : KunsatMAr) {
                                                             
                                                                float cvo = vo1;
                                                                float l1 = 0.15f;
                                                                float l2 = -0.82f * l1 + 0.1025f;
                                                                float vo = cvo * (0.42f - 0.29f * l1 - 3.1f * l2);

                                                                l2 = 0.05f;
                                                                vo = cvo * 0.21f;

                                                                if (VP == 1) {
                                                                    l1 = 0.15f;
                                                                    l2 = 0.05f;
                                                                    vo = cvo * 0.21f;
                                                                } else if (VP == 2) {
                                                                    l1 = 0.3f;
                                                                    l2 = -0.15f;
                                                                    vo = cvo * 0.75f;
                                                                } else if (VP == 3) {
                                                                    l1 = 0.2f;
                                                                    l2 = -0.15f;
                                                                    vo = cvo * 0.75f;
                                                                } else if (VP == 51) {
                                                                    l1 = 0.2f;
                                                                    l2 = -0.15f;
                                                                    vo = cvo * 0.75f;    
                                                                    
                                                                } else if (VP == 4) {
                                                                    l1 = 0.24f;
                                                                    l2 = -0.16f;
                                                                    vo = cvo * 0.9f;
                                                                } else if (VP == 5) {
                                                                    l1 = 0.35f;
                                                                    l2 = -0.11f;
                                                                    vo = cvo * 0.44f;
                                                                } else if (VP == 6) {
                                                                    l1 = 0.2f;
                                                                    l2 = 0.0f;
                                                                    vo = cvo * 0.28f;
                                                                } else if (VP == 7) {
                                                                    vo = cvo * 0.64f;
                                                                    l1 = 0.24f;
                                                                    l2 = -0.12f;

                                                                } else if (VP == 8) {
                                                                    vo = cvo * 0.75f;
                                                                    l1 = 0.27f;
                                                                    l2 = -0.15f;
                                                                } else if (VP == 10) {
                                                                    vo = cvo * 0.32f;
                                                                    l1 = 0.10f;
                                                                    l2 = 0.0f;
                                                                
                                                                } else if (VP == 11) {
                                                                    vo = cvo * 0.83f;
                                                                    l1 = 0.14f;
                                                                    l2 = -0.08f;
                                                                }
                                                                
                                                        


                                                                if (BasinName.indexOf("GarberNL2") > 0) {
                                                                    l1 = 0.32f;
                                                                    l2 = -0.12f;
                                                                    vo = cvo * 0.64f;
                                                                }
                                                                for (float cts1 : CteSAr) {
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

                                                                                            float cts = cts1;




                                                                                            for (float in1 : PH) {
                                                                                                float p1 = in1;
                                                                                                for (float in2 : Phi) {
                                                                                                    float p2 = in2;
                                                                                                    for (float voo : vsAr) {
                                                                                                        float vsub = voo;
                                                                                                        for (float Vostd : VostdAr) {

                                                                                                            routingParams.put("KunsatCte", KunsatC); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                            routingParams.put("v_o", vo);
                                                                                                            routingParams.put("lambda1", l1);
                                                                                                            routingParams.put("lambda2", l2);
                                                                                                            routingParams.put("CQflood", 4.0f); // reservoir position:

                                                                                                            //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                                                                            routingParams.put("CQflood", 4.0f); // reservoir position:
                                                                                                            routingParams.put("MinPondedWater", MinPound);

                                                                                                            routingParams.put("EQflood", 0.51f); // reservoir position:

                                                                                                            routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                                                                            routingParams.put("v_o", vo);
                                                                                                            routingParams.put("hillshapeparamflag", hs);
                                                                                                            routingParams.put("HillT", HillT);  // check NetworkEquationsLuciana.java for definitions
                                                                                                            routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                                                                                                            if (VP == 5) {
                                                                                                                routingParams.put("RoutingT", 8); // check NetworkEquationsLuciana.java for definitions
                                                                                                            }
                                                                                                            if (VP == 4) {
                                                                                                                routingParams.put("RoutingT", 7); // check NetworkEquationsLuciana.java for definitions
                                                                                                            }
                                                                                                            if (VP == 10) {
                                                                                                                routingParams.put("RoutingT", 10); // check NetworkEquationsLuciana.java for definitions
                                                                                                            }
                                                                                                             if (VP == 11) {
                                                                                                                routingParams.put("RoutingT", 11); // check NetworkEquationsLuciana.java for definitions
                                                                                                            }
                                                                                                            if (VP == 51) {
                                                                                                                routingParams.put("RoutingT", 51); // check NetworkEquationsLuciana.java for definitions
                                                                                                            }
                                                                                                            routingParams.put("KunsatModelType", KunsatM);
                                                                                                            //                         routingParams.put("lambda1", l11);
                                                                                                            routingParams.put("EVcoef", ev); // reservoir position:

                                                                                                            //                         routingParams.put("lambda2", l2);
                                                                                                            routingParams.put("PorcHSaturated", p1);
                                                                                                            routingParams.put("vssub", vsub);
                                                                                                            routingParams.put("PorcPhiUnsat", p2);
                                                                                                            routingParams.put("lambdaSCSMethod", iae);
                                                                                                            routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                            routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)

                                                                                                            routingParams.put("Coefvh", 1.0f);
                                                                                                            routingParams.put("Coefks", coefks);
                                                                                                            routingParams.put("CoefAi", CoefAi);

                                                                                                            if (rc >= 0.f) {
                                                                                                                if (flaghill == 1) {
                                                                                                                    routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                                                    routingParams.put("PorcPhiUnsat", 0.0f);
                                                                                                                    routingParams.put("KunsatCte", -9.9f); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                                    routingParams.put("PorcHSaturated", 0.0f);
                                                                                                                } // reservoir position:
                                                                                                                else if (flaghill == 0) {
                                                                                                                    routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("HillVelocityT", 0);
                                                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                                                } // reservoir position:
                                                                                                                else {
                                                                                                                    routingParams.put("HillT", 22);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                                                                }
                                                                                                            } else {
                                                                                                                routingParams.put("HillT", HillT);
                                                                                                                routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                                                                            }


                                                                                                            if (HillT == 0) {
                                                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", 0);
                                                                                                                routingParams.put("Coefvh", 1.0f);
                                                                                                            }
                                                                                                            if (vh > 0) {

                                                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", 1.0f);

                                                                                                            } else if (vh == -6) {
                                                                                                                routingParams.put("HillVelocityT", 6);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -1) {
                                                                                                                routingParams.put("HillVelocityT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                            } else if (vh == -7) {
                                                                                                                routingParams.put("HillVelocityT", 7);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -71) {
                                                                                                                routingParams.put("HillVelocityT", 71);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -8) {
                                                                                                                routingParams.put("HillVelocityT", 8);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -9) {
                                                                                                                routingParams.put("HillVelocityT", 9);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -4) {
                                                                                                                routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                            } else if (vh == -5) {
                                                                                                                routingParams.put("HillVelocityT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else {
                                                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                            }


                                                                                                            routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions
                                                                                                            routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)


                                                                                                            //routingParams.put("RunoffCoefficient", rc); // reservoir position:ccc
                                                                                                            // + "/BFExp" + ((Float) routingParams.get("BaseFlowExp")).floatValue()
                                                                                                            //                                                                     + "/BFCoef" + ((Float) routingParams.get("BaseFlowCoef")).floatValue()

                                                                                                            outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/Helium_version/" + BasinName + "/" + SimName + "/" + bflag + "/");
                                                                                                            System.out.println("after");
                                                                                                            outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                                                    + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                                                                                                                    + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                                                                    + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                                                    + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                                                                                    + "/KuT_" + ((Integer) routingParams.get("KunsatModelType")).intValue()
                                                                                                                    + "/PEF_" + PotEvapFlag + "/"
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


                                                                                                            //FAST TEST
                                                                                                            //outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/luciana/Parallel/testHeliumversion/");
                                                                                                            // alphaSimulationTime.set(2010, 0, 0, 0, 0, 0);
                                                                                                            // omegaSimulationTime.set(2010, 0, 2, 0, 0, 0);
                                                                                                            // stormFile = new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/thesis2/60mm/15min/vhc/60mm15min.metaVHC");

                                                                                                            int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                                                                            outputDirectory.mkdirs();
                                                                                                            java.util.Date StartTime = new java.util.Date();
                                                                                                            new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, SNOWMeltFile, SWEFile, SOILMFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);

//                                                                                java.io.File outfolder = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/results/" + BasinName + "/" + SimName + "/");
//                                                                                outfolder.mkdirs(); 
//                                                                                java.util.Date EndTime = new java.util.Date();
//                                                                                java.io.FileWriter Fstream=new java.io.FileWriter("/nfsscratch/Users/rmantill/FinishedSimul.log",true);
//                                                                                  java.io.BufferedWriter out = new java.io.BufferedWriter(Fstream);
//                                                                                  
//                                                                                out.write("Simulation finished \n");
//                                                                                   out.write(outputDirectory.toString()+"\n");
//                                                                                   out.write(outfolder.toString()+"\n");
//                                                                                   out.write(routingParams.toString() + "\n");
//                                                                                   out.write("Starting time" + StartTime.toString() + "\n");
//                                                                                   out.write("End time" + EndTime.toString() + "\n");

//                                                                                try {
//                                                                                    java.io.File LinkAnalysisFile = new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/linksAnalyses/linksInfo2885_690.csv"); // Iowa city
//                                                                                    LinkAnalysisFile = new java.io.File(hydroScalingAPI.examples.io.ParalelVersionReader.defineLinkFile(BasinName, SimName));
//                                                                                    System.out.println(LinkAnalysisFile.getAbsoluteFile());
//                                                                                    String str = outputDirectory.toString().replace('/', '_');
//                                                                                    //String str2=str.substring(str.indexOf("Rout"));
//                                                                                    System.out.println(str);
//                                                                                    String str2 = str.substring(str.indexOf("RoutT") - 13);
//                                                                                    new hydroScalingAPI.examples.io.ParalelVersionReader(outputDirectory, LinkAnalysisFile, str2, outfolder);
//                                                                                    ///nfsscratch/Users/rmantill/results_cuencas/MultipleYearsLong2/results/3Garber2002Long/90DEMUSGS.zip
//                                                                                   String ZipFolder=outfolder.toString().replace(BasinName + "/" + SimName, BasinName + "_" + SimName + ".zip");
//                                                                                   hydroScalingAPI.examples.io.ParalelVersionReader.zipFolder(outfolder.toString(), ZipFolder);
//                                                                                   out.write("RUN Parallel Version and Create Zip \n" + (outfolder.toString()+".zip"));
//                                                                                } catch (java.io.IOException IOE) {
//                                                                                   out.write("DID NOT run Parallel Version \n");
//                                                                                }

////                                                                                    out.close();
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
                                            }
                                        }
                                    }
                                }
                                float[] vsAr2 = {};
                                float[] PH2 = {0.0f};
                                float[] Phi2 = {0.0f};
                                float[] IaAre2 = {0.02f};
                                float[] Coefvo2 = {1.0f};
                                float[] coefvhAr2 = {1.f};
                                float[] coefksAr2 = {0.1f};
                                float[] IaArc2 = {0.0f};
                                //float[] IaArc = {0.0f,0.1f,1.0f};

           

                                //float[] rcAr2 = {-9.f};
                                //float[] vhAr2 = {-9.f};

                                float[] rcAr2 = {1.0f,0.9f};
                                float[] vhAr2 = {36.f};
                                float[] voAr2 = {1.0f};

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

                                        for (float ev1 : EVAr2) {
                                            for (float vh1 : vhAr2) {
                                                float vh = vh1;

                                                for (float in1 : PH2) {
                                                    float p1 = in1;
                                                    for (float in2 : Phi2) {
                                                        float p2 = in2;
                                                        for (float voo : vsAr2) {
                                                            float vsub = voo;

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
                                                                            routingParams.put("OutFlag", 0);

                                                                            routingParams.put("EQflood", 0.51f); // reservoir position:

                                                                            routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                                            routingParams.put("v_o", vo);
                                                                            routingParams.put("hillshapeparamflag", hs);
                                                                            routingParams.put("HillT", HillT);  // check NetworkEquationsLuciana.java for definitions
                                                                            routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions

                                                                            //                         routingParams.put("lambda1", l11);
                                                                            routingParams.put("EVcoef", ev1); // reservoir position:

                                                                            //                         routingParams.put("lambda2", l2);

                                                                            routingParams.put("PorcHSaturated", p1);
                                                                            routingParams.put("vssub", vsub);
                                                                            routingParams.put("PorcPhiUnsat", p2);
                                                                            routingParams.put("lambdaSCSMethod", iae);
                                                                            routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                                            routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)
                                                                            float coefvh = 1.0f;
                                                                            if (rc >= 0.f) {
                                                                                if (flaghill == 1) {
                                                                                    routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                    routingParams.put("PorcPhiUnsat", 0.0f);
                                                                                    routingParams.put("KunsatCte", -9.9f); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                    routingParams.put("PorcHSaturated", 0.0f);
                                                                                } // reservoir position:
                                                                                else if (flaghill == 0) {
                                                                                    routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                    routingParams.put("HillVelocityT", 0);
                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                } // reservoir position:
                                                                                else {
                                                                                    routingParams.put("HillT", 22);  // check NetworkEquationsLuciana.java for definitions
                                                                                    routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                                }
                                                                            } else {
                                                                                routingParams.put("HillT", HillT);
                                                                                routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                                            }


                                                                            if (HillT == 0) {
                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", 0);
                                                                                routingParams.put("Coefvh", 1.0);
                                                                            }
                                                                            if (vh > 0) {

                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                                routingParams.put("Coefvh", 1.0);
                                                                            } else if (vh == -6) {
                                                                                routingParams.put("HillVelocityT", 6);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                            } else if (vh == -1) {
                                                                                routingParams.put("HillVelocityT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                            } else if (vh == -7) {
                                                                                routingParams.put("HillVelocityT", 7);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                                routingParams.put("Coefvh", coefvh);
                                                                            } else if (vh == -8) {
                                                                                routingParams.put("HillVelocityT", 8);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                                routingParams.put("Coefvh", coefvh);
                                                                            } else if (vh == -9) {
                                                                                routingParams.put("HillVelocityT", 9);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                                routingParams.put("Coefvh", coefvh);
                                                                            } else if (vh == -4) {
                                                                                routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                            } else if (vh == -5) {
                                                                                routingParams.put("HillVelocityT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                            } else {
                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                                            }

                                                                            routingParams.put("Coefks", 1.0f);
                                                                            routingParams.put("Coefvh", 1.0f);



                                                                            routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions
                                                                            routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)


                                                                            outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                    + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                                                                                    + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                                    + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                    + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                                                    + "CQf_" + ((Float) routingParams.get("CQflood")).floatValue() + "/"
                                                                                    + "EV_" + ((Float) routingParams.get("EVcoef")).floatValue() + "/"
                                                                                    + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                                                    + "/Cks_" + ((Float) routingParams.get("Coefks")).floatValue()
                                                                                    + "/RC" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                                                                                    + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                                                    + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                                                    + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                                                                                    + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                                                    + "/Cvh_" + ((Float) routingParams.get("Coefvh")).floatValue()
                                                                                    + "/vcte_" + ((Float) routingParams.get("v_o")).floatValue());


                                                                            if (HillT == 11) {
                                                                                outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                        + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                                                                                        + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                                        + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                        + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue() + "/"
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


                                                                            new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, SNOWMeltFile, SWEFile, SOILMFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);
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
            }
        }
    }  public static void subMainRadar2(String args[]) throws java.io.IOException, VisADException, Exception {

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};


        String WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts/";
        java.io.File WorkDirectory = new java.io.File(WorkDir);
        int n = 1;
        while (WorkDirectory.exists() == true) {
            //System.out.println("file exists" + WorkDir);
            n = n + 1;
            WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts" + n + "/";
            WorkDirectory = new java.io.File(WorkDir);
        }

        System.out.println("file does not exist, Create: " + WorkDir);

        WorkDirectory.mkdirs();


        String[] AllSimName = {"90DEMUSGSPrun7"};
        //String[] AllRain = {"3IowaCity2006Long_0200SNLDA",
        //    "3CedarRapids2006Long_0200SNLDA"};
        //String[] AllRain = {"shalehills"};
        String[] AllRain = {"3CedarRapids2008Event_0200SNLDA"};
//        String[] AllRain = { "3CedarRapids2008Long_0200SNLDA",
//                "3CedarRapids2008Long_0200SNLDAPERSIANN"
//                ,"3CedarRapids2008Long_0200SNLDACMORPH"
//                ,"3CedarRapids2008Long_0200SNLDATRMM"
//                ,"3CedarRapids2008Long_0200SNLDANLDASPrec"
//                ,"3CedarRapids2008Long_0200SNLDANWS2"};



        //String[] AllRain = {"3ClearCreek2008Event_0200SNLDAHN3_60BR"};
        //"3ClearCreek2005Long_0200SNLDA","3ClearCreek2006Long_0200SNLDA","3ClearCreek2007Long_0200SNLDA"};
//String[] AllRain = {"3ClearCreek2008Long_0200"};

        String Direc = "/StudySatBasedonHN3/Res3/";


        //Direc = "MultipleDataset2008_v4";
        //Direc = "TestShalehills";
        java.util.Hashtable routingParams = new java.util.Hashtable();
        routingParams.put("Outflag", 2);

        //"3ClearCreekBOhour2_2", ,"3ClearCreekBOSTAGEIV_2"



        int nsim = AllSimName.length;

        int nbas = AllRain.length;
        int[] HillAr = {20262};
        for (int HillT : HillAr) {

            int flaghill = HillT;

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

                    System.out.println(BasinName);
                    System.out.println(SimName);

                    stormFile = new java.io.File(definestorm(BasinName, SimName));
                    System.out.println(BasinName);
                    System.out.println(SimName);
                    System.out.println(stormFile.toString());

                 
                    // DEFINE THE INITIAL TIME OF THE SIMULATION
                     java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
                    java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                    alphaSimulationTime.setTimeZone(tz);
                    alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);


                    // DEFINE THE FINAL TIME OF THE SIMULATION
                    java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                    omegaSimulationTime.setTimeZone(tz);
                    omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);

                    if (BasinName.indexOf("hills") >= 0) {

                        alphaSimulationTime.set(1974, 7, 1, 0, 0, 0);
                        omegaSimulationTime.set(1974, 8, 30, 0, 0, 0);
                    }
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
                        if (BasinName.indexOf("test") > 0 || BasinName.indexOf("Event") > 0) {
                            alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);// from may 01
                            omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);
                        }
                        if (BasinName.indexOf("Long") > 0) {
                            alphaSimulationTime.set(yr, 2, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 9, 01, 0, 0, 0);// from oct 10,01
                        }
                        if (BasinName.indexOf("Time1") > 0) {
                            alphaSimulationTime.set(yr, 3, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 5, 01, 0, 0, 0);// from oct 10,01
                        }
                        if (BasinName.indexOf("Time2") > 0) {
                            alphaSimulationTime.set(yr, 5, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 7, 01, 0, 0, 0);// from oct 10,01
                        }
                        if (BasinName.indexOf("Time3") > 0) {
                            alphaSimulationTime.set(yr, 7, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 9, 01, 0, 0, 0);// from oct 10,01
                        }

                    }

                    if (BasinName.indexOf("TEST") > 0) {
                        alphaSimulationTime.set(2008, 2, 01, 0, 0, 0);// from may 01
                        omegaSimulationTime.set(2008, 2, 10, 0, 0, 0);
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
                    routingParams.put("KunsatCte", 1.0f); // define a constant number or -9.9 for vel=f(land Cover)
//C1111111 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0
                    routingParams.put("ETolerance", 0.001f);
                    routingParams.put("OutFlag", 2);

                    int[] Space = {2,4,8,12,15};
                    int[] Time= {15,30,60,120,180,1440};
                    
                    float[] BasinFlagAr = {0.0f};
                    int[] PotEvapFlagAr = {1};

                    // Flag equal to 1 - NLDAS
                    // flag equal to 2 - MODIS - mandatory if using evaporation instead of potential evapo (HillT>3000)
                    for (int SP : Space) {
                        for (int TM : Time) {
                    
                    
                    for (float BF : BasinFlagAr) {
                        for (int PF : PotEvapFlagAr) {
                            int PotEvapFlag = PF;

                            PotEVPTFile = new java.io.File(definePotEVPT(BasinName, SimName, PotEvapFlag));
                            String[] Snow = defineSNOW(BasinName, SimName);
                            java.io.File SNOWMeltFile;
                            SNOWMeltFile = new java.io.File(Snow[0]);
                            java.io.File SWEFile;
                            SWEFile = new java.io.File(Snow[1]);
                            java.io.File SOILMFile;
                            SOILMFile = new java.io.File(defineSOILM(BasinName, SimName));

                            if (HillT == 0 || HillT == 1) {
                                SOILMFile = new java.io.File("null");
                                PotEVPTFile = new java.io.File("null");
                                SNOWMeltFile = new java.io.File("null");
                                SWEFile = new java.io.File("null");
                            }
                            float bflag = BF;

                            routingParams.put("Basin_sim", bflag); // Cedar river - define Land cover and soil parameter
                            //float[] vsAr = {0.0002f,0.0005f};
                            float[] vsAr = {-9.f};
                            //-8 is with average VS
                            //-9is with minimal VS
                            float[] PH = {0.0f};
                            float[] Phi = {-9.0f};
                            //float[] Coefvo = {0.4f,0.8f,0.65f};
                            float[] Coefvo = {0.65f};

                            if (BasinName.indexOf("Event") > 0) {
                                Coefvo[0] = 0.6f;
                            }
                            if (BasinName.indexOf("Garber") > 0) {
                                Coefvo[0] = 0.8f;
                            }
                            System.out.println("Coefvo[0]" + Coefvo[0]);
                            //System.exit(1);
                            float[] coefvhAr = {1.0f};
                            float[] coefAiAr = {1.0f};
                            float[] coefksAr = {1.f};
                            float[] KunsatCAr = {5.f};
                            float[] MinPoundAr = {0.0f};
                            float[] CteSAr = {-3.0f};
                            float[] IaAre = {0.4f};
                            float[] IaArc = {0.0f};
                            int[] VParamSet = {3};
                            int[] KunsatMAr = {0};
                            //float[] rcAr = {-9.f};
                            //float[] vhAr = {-9.f};

                            float[] rcAr = {-9.0f};
                            float[] vhAr = {-6.f};

                            float[] HSAr = {0.f};

                            float[] VostdAr = {0.0f};
                            float[] kf1Ar = {1.0f};
                            float[] EVAr = {1.0f};
                            for (float MinPound : MinPoundAr) {
                                for (float KunsatC : KunsatCAr) {



                                    for (float coefks : coefksAr) {
                                        for (float coefvh : coefvhAr) {

                                            for (float ev : EVAr) {
                                                for (float vo1 : Coefvo) {
                                                    for (int VP : VParamSet) {
                                                        for (float CoefAi : coefAiAr) {
                                                            for (int KunsatM : KunsatMAr) {


                                                                float cvo = vo1;
                                                                float l1 = 0.15f;
                                                                float l2 = -0.82f * l1 + 0.1025f;
                                                                float vo = cvo * (0.42f - 0.29f * l1 - 3.1f * l2);

                                                                l2 = 0.05f;
                                                                vo = cvo * 0.21f;

                                                                if (VP == 1) {
                                                                    l1 = 0.15f;
                                                                    l2 = 0.05f;
                                                                    vo = cvo * 0.21f;
                                                                } else if (VP == 2) {
                                                                    l1 = 0.3f;
                                                                    l2 = -0.15f;
                                                                    vo = cvo * 0.75f;
                                                                } else if (VP == 3) {
                                                                    l1 = 0.2f;
                                                                    l2 = -0.15f;
                                                                    vo = cvo * 0.75f;
                                                                } else if (VP == 4) {
                                                                    l1 = 0.24f;
                                                                    l2 = -0.16f;
                                                                    vo = cvo * 0.9f;
                                                                } else if (VP == 5) {
                                                                    l1 = 0.35f;
                                                                    l2 = -0.11f;
                                                                    vo = cvo * 0.44f;
                                                                } else if (VP == 6) {
                                                                    l1 = 0.2f;
                                                                    l2 = 0.0f;
                                                                    vo = cvo * 0.28f;
                                                                } else if (VP == 7) {
                                                                    vo = cvo * 0.64f;
                                                                    l1 = 0.24f;
                                                                    l2 = -0.12f;

                                                                } else if (VP == 8) {
                                                                    vo = cvo * 0.75f;
                                                                    l1 = 0.27f;
                                                                    l2 = -0.15f;
                                                                }




                                                                if (BasinName.indexOf("GarberNL2") > 0) {
                                                                    l1 = 0.32f;
                                                                    l2 = -0.12f;
                                                                    vo = cvo * 0.64f;
                                                                }
                                                                for (float cts1 : CteSAr) {
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

                                                                                            float cts = cts1;




                                                                                            for (float in1 : PH) {
                                                                                                float p1 = in1;
                                                                                                for (float in2 : Phi) {
                                                                                                    float p2 = in2;
                                                                                                    for (float voo : vsAr) {
                                                                                                        float vsub = voo;
                                                                                                        for (float Vostd : VostdAr) {

                                                                                                            routingParams.put("KunsatCte", KunsatC); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                            routingParams.put("v_o", vo);
                                                                                                            routingParams.put("lambda1", l1);
                                                                                                            routingParams.put("lambda2", l2);
                                                                                                            routingParams.put("CQflood", 4.0f); // reservoir position:

                                                                                                            //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                                                                            routingParams.put("CQflood", 4.0f); // reservoir position:
                                                                                                            routingParams.put("MinPondedWater", MinPound);

                                                                                                            routingParams.put("EQflood", 0.51f); // reservoir position:

                                                                                                            routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                                                                            routingParams.put("v_o", vo);
                                                                                                            routingParams.put("hillshapeparamflag", hs);
                                                                                                            routingParams.put("HillT", HillT);  // check NetworkEquationsLuciana.java for definitions
                                                                                                            routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                                                                                                            if (VP == 5) {
                                                                                                                routingParams.put("RoutingT", 8); // check NetworkEquationsLuciana.java for definitions
                                                                                                            }
                                                                                                            if (VP == 4) {
                                                                                                                routingParams.put("RoutingT", 7); // check NetworkEquationsLuciana.java for definitions
                                                                                                            }
                                                                                                            routingParams.put("KunsatModelType", KunsatM);
                                                                                                            //                         routingParams.put("lambda1", l11);
                                                                                                            routingParams.put("EVcoef", ev); // reservoir position:

                                                                                                            //                         routingParams.put("lambda2", l2);
                                                                                                            routingParams.put("PorcHSaturated", p1);
                                                                                                            routingParams.put("vssub", vsub);
                                                                                                            routingParams.put("PorcPhiUnsat", p2);
                                                                                                            routingParams.put("lambdaSCSMethod", iae);
                                                                                                            routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                            routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)

                                                                                                            routingParams.put("Coefvh", 1.0f);
                                                                                                            routingParams.put("Coefks", coefks);
                                                                                                            routingParams.put("CoefAi", CoefAi);

                                                                                                            if (rc >= 0.f) {
                                                                                                                if (flaghill == 1) {
                                                                                                                    routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                                                    routingParams.put("PorcPhiUnsat", 0.0f);
                                                                                                                    routingParams.put("KunsatCte", -9.9f); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                                    routingParams.put("PorcHSaturated", 0.0f);
                                                                                                                } // reservoir position:
                                                                                                                else if (flaghill == 0) {
                                                                                                                    routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("HillVelocityT", 0);
                                                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                                                } // reservoir position:
                                                                                                                else {
                                                                                                                    routingParams.put("HillT", 22);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                                                                }
                                                                                                            } else {
                                                                                                                routingParams.put("HillT", HillT);
                                                                                                                routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                                                                            }


                                                                                                            if (HillT == 0) {
                                                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", 0);
                                                                                                                routingParams.put("Coefvh", 1.0f);
                                                                                                            }
                                                                                                            if (vh > 0) {

                                                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", 1.0f);

                                                                                                            } else if (vh == -6) {
                                                                                                                routingParams.put("HillVelocityT", 6);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -1) {
                                                                                                                routingParams.put("HillVelocityT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                            } else if (vh == -7) {
                                                                                                                routingParams.put("HillVelocityT", 7);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -71) {
                                                                                                                routingParams.put("HillVelocityT", 71);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -8) {
                                                                                                                routingParams.put("HillVelocityT", 8);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -9) {
                                                                                                                routingParams.put("HillVelocityT", 9);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -4) {
                                                                                                                routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                            } else if (vh == -5) {
                                                                                                                routingParams.put("HillVelocityT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else {
                                                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                            }


                                                                                                            routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions
                                                                                                            routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)


                                                                                                            //routingParams.put("RunoffCoefficient", rc); // reservoir position:ccc
                                                                                                            // + "/BFExp" + ((Float) routingParams.get("BaseFlowExp")).floatValue()
                                                                                                            //                                                                     + "/BFCoef" + ((Float) routingParams.get("BaseFlowCoef")).floatValue()

                                                                                                            outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/Helium_version/" + BasinName + "/" + SimName + "/" + bflag + "/");
                                                                                                            System.out.println("after");
                                                                                                            outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                                                    
                                                                                                                    + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                                                                                                                    + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                                                                    + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                                                    + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                                                                                    + "/SP" + SP + "TM" +TM 
                                                                                                                    + "/KuT_" + ((Integer) routingParams.get("KunsatModelType")).intValue()
                                                                                                                    + "/PEF_" + PotEvapFlag + "/"
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


                                                                                                            //FAST TEST
                                                                                                            //outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/luciana/Parallel/testHeliumversion/");
                                                                                                            // alphaSimulationTime.set(2010, 0, 0, 0, 0, 0);
                                                                                                            // omegaSimulationTime.set(2010, 0, 2, 0, 0, 0);
                                                                                                             stormFile = new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/ResErrorFromNH3_5/"+SP+"/Time/"+ TM +"/Bin/MPE_IOWA_ST4.metaVHC");

                                                                                                            int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                                                                            outputDirectory.mkdirs();
                                                                                                            java.util.Date StartTime = new java.util.Date();
                                                                                                            new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, SNOWMeltFile, SWEFile, SOILMFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);

//                                                                                java.io.File outfolder = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/results/" + BasinName + "/" + SimName + "/");
//                                                                                outfolder.mkdirs(); 
//                                                                                java.util.Date EndTime = new java.util.Date();
//                                                                                java.io.FileWriter Fstream=new java.io.FileWriter("/nfsscratch/Users/rmantill/FinishedSimul.log",true);
//                                                                                  java.io.BufferedWriter out = new java.io.BufferedWriter(Fstream);
//                                                                                  
//                                                                                out.write("Simulation finished \n");
//                                                                                   out.write(outputDirectory.toString()+"\n");
//                                                                                   out.write(outfolder.toString()+"\n");
//                                                                                   out.write(routingParams.toString() + "\n");
//                                                                                   out.write("Starting time" + StartTime.toString() + "\n");
//                                                                                   out.write("End time" + EndTime.toString() + "\n");

//                                                                                try {
//                                                                                    java.io.File LinkAnalysisFile = new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/linksAnalyses/linksInfo2885_690.csv"); // Iowa city
//                                                                                    LinkAnalysisFile = new java.io.File(hydroScalingAPI.examples.io.ParalelVersionReader.defineLinkFile(BasinName, SimName));
//                                                                                    System.out.println(LinkAnalysisFile.getAbsoluteFile());
//                                                                                    String str = outputDirectory.toString().replace('/', '_');
//                                                                                    //String str2=str.substring(str.indexOf("Rout"));
//                                                                                    System.out.println(str);
//                                                                                    String str2 = str.substring(str.indexOf("RoutT") - 13);
//                                                                                    new hydroScalingAPI.examples.io.ParalelVersionReader(outputDirectory, LinkAnalysisFile, str2, outfolder);
//                                                                                    ///nfsscratch/Users/rmantill/results_cuencas/MultipleYearsLong2/results/3Garber2002Long/90DEMUSGS.zip
//                                                                                   String ZipFolder=outfolder.toString().replace(BasinName + "/" + SimName, BasinName + "_" + SimName + ".zip");
//                                                                                   hydroScalingAPI.examples.io.ParalelVersionReader.zipFolder(outfolder.toString(), ZipFolder);
//                                                                                   out.write("RUN Parallel Version and Create Zip \n" + (outfolder.toString()+".zip"));
//                                                                                } catch (java.io.IOException IOE) {
//                                                                                   out.write("DID NOT run Parallel Version \n");
//                                                                                }

////                                                                                    out.close();
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

    public static void subMainRadar3(String args[]) throws java.io.IOException, VisADException, Exception {

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};


        String WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts/";
        java.io.File WorkDirectory = new java.io.File(WorkDir);
        int n = 1;
        while (WorkDirectory.exists() == true) {
            //System.out.println("file exists" + WorkDir);
            n = n + 1;
            WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts" + n + "/";
            WorkDirectory = new java.io.File(WorkDir);
        }

        System.out.println("file does not exist, Create: " + WorkDir);

        WorkDirectory.mkdirs();


        String[] AllSimName = {"90DEMUSGSPrun7"};
        //String[] AllRain = {"3IowaCity2006Long_0200SNLDA",
        //    "3CedarRapids2006Long_0200SNLDA"};
        //String[] AllRain = {"shalehills"};
        String[] AllRain = {"3CedarRapids2008Event_0200SNLDA"};
//        String[] AllRain = { "3CedarRapids2008Long_0200SNLDA",
//                "3CedarRapids2008Long_0200SNLDAPERSIANN"
//                ,"3CedarRapids2008Long_0200SNLDACMORPH"
//                ,"3CedarRapids2008Long_0200SNLDATRMM"
//                ,"3CedarRapids2008Long_0200SNLDANLDASPrec"
//                ,"3CedarRapids2008Long_0200SNLDANWS2"};



        //String[] AllRain = {"3ClearCreek2008Event_0200SNLDAHN3_60BR"};
        //"3ClearCreek2005Long_0200SNLDA","3ClearCreek2006Long_0200SNLDA","3ClearCreek2007Long_0200SNLDA"};
//String[] AllRain = {"3ClearCreek2008Long_0200"};

        String Direc = "/StudySatBasedonHN3/Samplig2/nfiles2/";


        //Direc = "MultipleDataset2008_v4";
        //Direc = "TestShalehills";
        java.util.Hashtable routingParams = new java.util.Hashtable();
        routingParams.put("Outflag", 2);

        //"3ClearCreekBOhour2_2", ,"3ClearCreekBOSTAGEIV_2"



        int nsim = AllSimName.length;

        int nbas = AllRain.length;
        int[] HillAr = {20262};
        for (int HillT : HillAr) {

            int flaghill = HillT;

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

                    System.out.println(BasinName);
                    System.out.println(SimName);

                    stormFile = new java.io.File(definestorm(BasinName, SimName));
                    System.out.println(BasinName);
                    System.out.println(SimName);
                    System.out.println(stormFile.toString());

                    //System.exit(1);
                    // DEFINE THE INITIAL TIME OF THE SIMULATION
                    java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
                    java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                    alphaSimulationTime.setTimeZone(tz);
                    alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);


                    // DEFINE THE FINAL TIME OF THE SIMULATION
                    java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                    omegaSimulationTime.setTimeZone(tz);
                    omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);

                    if (BasinName.indexOf("hills") >= 0) {

                        alphaSimulationTime.set(1974, 7, 1, 0, 0, 0);
                        omegaSimulationTime.set(1974, 8, 30, 0, 0, 0);
                    }
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
                        if (BasinName.indexOf("test") > 0 || BasinName.indexOf("Event") > 0) {
                            alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);// from may 01
                            omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);
                        }
                        if (BasinName.indexOf("Long") > 0) {
                            alphaSimulationTime.set(yr, 2, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 9, 01, 0, 0, 0);// from oct 10,01
                        }
                        if (BasinName.indexOf("Time1") > 0) {
                            alphaSimulationTime.set(yr, 3, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 5, 01, 0, 0, 0);// from oct 10,01
                        }
                        if (BasinName.indexOf("Time2") > 0) {
                            alphaSimulationTime.set(yr, 5, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 7, 01, 0, 0, 0);// from oct 10,01
                        }
                        if (BasinName.indexOf("Time3") > 0) {
                            alphaSimulationTime.set(yr, 7, 01, 0, 0, 0);// from mar 03,01
                            omegaSimulationTime.set(yr, 9, 01, 0, 0, 0);// from oct 10,01
                        }

                    }

                    if (BasinName.indexOf("TEST") > 0) {
                        alphaSimulationTime.set(2008, 2, 01, 0, 0, 0);// from may 01
                        omegaSimulationTime.set(2008, 2, 10, 0, 0, 0);
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
                    routingParams.put("KunsatCte", 1.0f); // define a constant number or -9.9 for vel=f(land Cover)
//C1111111 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0
                    routingParams.put("ETolerance", 0.001f);
                    routingParams.put("OutFlag", 2);

                  
                    
                    float[] BasinFlagAr = {0.0f};
                    int[] PotEvapFlagAr = {1};

                    // Flag equal to 1 - NLDAS
                    // flag equal to 2 - MODIS - mandatory if using evaporation instead of potential evapo (HillT>3000)
                   
                    
                    
                    for (float BF : BasinFlagAr) {
                        for (int PF : PotEvapFlagAr) {
                            int PotEvapFlag = PF;

                            PotEVPTFile = new java.io.File(definePotEVPT(BasinName, SimName, PotEvapFlag));
                            String[] Snow = defineSNOW(BasinName, SimName);
                            java.io.File SNOWMeltFile;
                            SNOWMeltFile = new java.io.File(Snow[0]);
                            java.io.File SWEFile;
                            SWEFile = new java.io.File(Snow[1]);
                            java.io.File SOILMFile;
                            SOILMFile = new java.io.File(defineSOILM(BasinName, SimName));

                            if (HillT == 0 || HillT == 1) {
                                SOILMFile = new java.io.File("null");
                                PotEVPTFile = new java.io.File("null");
                                SNOWMeltFile = new java.io.File("null");
                                SWEFile = new java.io.File("null");
                            }
                            float bflag = BF;

                            routingParams.put("Basin_sim", bflag); // Cedar river - define Land cover and soil parameter
                            //float[] vsAr = {0.0002f,0.0005f};
                            float[] vsAr = {-9.f};
                            //-8 is with average VS
                            //-9is with minimal VS
                            float[] PH = {0.0f};
                            float[] Phi = {-9.0f};
                            //float[] Coefvo = {0.4f,0.8f,0.65f};
                            float[] Coefvo = {0.65f};

                            if (BasinName.indexOf("Event") > 0) {
                                Coefvo[0] = 0.6f;
                            }
                            if (BasinName.indexOf("Garber") > 0) {
                                Coefvo[0] = 0.8f;
                            }
                            System.out.println("Coefvo[0]" + Coefvo[0]);
                            //System.exit(1);
                            float[] coefvhAr = {1.0f};
                            float[] coefAiAr = {1.0f};
                            float[] coefksAr = {1.f};
                            float[] KunsatCAr = {5.f};
                            float[] MinPoundAr = {0.0f};
                            float[] CteSAr = {-3.0f};
                            float[] IaAre = {0.4f};
                            float[] IaArc = {0.0f};
                            int[] VParamSet = {3};
                            int[] KunsatMAr = {0};
                            //float[] rcAr = {-9.f};
                            //float[] vhAr = {-9.f};

                            float[] rcAr = {-9.0f};
                            float[] vhAr = {-6.f};

                            float[] HSAr = {0.f};

                            float[] VostdAr = {0.0f};
                            float[] kf1Ar = {1.0f};
                            float[] EVAr = {1.0f};
                            for (float MinPound : MinPoundAr) {
                                for (float KunsatC : KunsatCAr) {



                                    for (float coefks : coefksAr) {
                                        for (float coefvh : coefvhAr) {

                                            for (float ev : EVAr) {
                                                for (float vo1 : Coefvo) {
                                                    for (int VP : VParamSet) {
                                                        for (float CoefAi : coefAiAr) {
                                                            for (int KunsatM : KunsatMAr) {


                                                                float cvo = vo1;
                                                                float l1 = 0.15f;
                                                                float l2 = -0.82f * l1 + 0.1025f;
                                                                float vo = cvo * (0.42f - 0.29f * l1 - 3.1f * l2);

                                                                l2 = 0.05f;
                                                                vo = cvo * 0.21f;

                                                                if (VP == 1) {
                                                                    l1 = 0.15f;
                                                                    l2 = 0.05f;
                                                                    vo = cvo * 0.21f;
                                                                } else if (VP == 2) {
                                                                    l1 = 0.3f;
                                                                    l2 = -0.15f;
                                                                    vo = cvo * 0.75f;
                                                                } else if (VP == 3) {
                                                                    l1 = 0.2f;
                                                                    l2 = -0.15f;
                                                                    vo = cvo * 0.75f;
                                                                } else if (VP == 4) {
                                                                    l1 = 0.24f;
                                                                    l2 = -0.16f;
                                                                    vo = cvo * 0.9f;
                                                                } else if (VP == 5) {
                                                                    l1 = 0.35f;
                                                                    l2 = -0.11f;
                                                                    vo = cvo * 0.44f;
                                                                } else if (VP == 6) {
                                                                    l1 = 0.2f;
                                                                    l2 = 0.0f;
                                                                    vo = cvo * 0.28f;
                                                                } else if (VP == 7) {
                                                                    vo = cvo * 0.64f;
                                                                    l1 = 0.24f;
                                                                    l2 = -0.12f;

                                                                } else if (VP == 8) {
                                                                    vo = cvo * 0.75f;
                                                                    l1 = 0.27f;
                                                                    l2 = -0.15f;
                                                                }




                                                                if (BasinName.indexOf("GarberNL2") > 0) {
                                                                    l1 = 0.32f;
                                                                    l2 = -0.12f;
                                                                    vo = cvo * 0.64f;
                                                                }
                                                                for (float cts1 : CteSAr) {
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

                                                                                            float cts = cts1;




                                                                                            for (float in1 : PH) {
                                                                                                float p1 = in1;
                                                                                                for (float in2 : Phi) {
                                                                                                    float p2 = in2;
                                                                                                    for (float voo : vsAr) {
                                                                                                        float vsub = voo;
                                                                                                        for (float Vostd : VostdAr) {
                                                                                                            for (int iss = 20; iss < 30 ; iss++) {

                                                                                                            routingParams.put("KunsatCte", KunsatC); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                            routingParams.put("v_o", vo);
                                                                                                            routingParams.put("lambda1", l1);
                                                                                                            routingParams.put("lambda2", l2);
                                                                                                            routingParams.put("CQflood", 4.0f); // reservoir position:

                                                                                                            //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                                                                            routingParams.put("CQflood", 4.0f); // reservoir position:
                                                                                                            routingParams.put("MinPondedWater", MinPound);

                                                                                                            routingParams.put("EQflood", 0.51f); // reservoir position:

                                                                                                            routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                                                                            routingParams.put("v_o", vo);
                                                                                                            routingParams.put("hillshapeparamflag", hs);
                                                                                                            routingParams.put("HillT", HillT);  // check NetworkEquationsLuciana.java for definitions
                                                                                                            routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                                                                                                            if (VP == 5) {
                                                                                                                routingParams.put("RoutingT", 8); // check NetworkEquationsLuciana.java for definitions
                                                                                                            }
                                                                                                            if (VP == 4) {
                                                                                                                routingParams.put("RoutingT", 7); // check NetworkEquationsLuciana.java for definitions
                                                                                                            }
                                                                                                            routingParams.put("KunsatModelType", KunsatM);
                                                                                                            //                         routingParams.put("lambda1", l11);
                                                                                                            routingParams.put("EVcoef", ev); // reservoir position:

                                                                                                            //                         routingParams.put("lambda2", l2);
                                                                                                            routingParams.put("PorcHSaturated", p1);
                                                                                                            routingParams.put("vssub", vsub);
                                                                                                            routingParams.put("PorcPhiUnsat", p2);
                                                                                                            routingParams.put("lambdaSCSMethod", iae);
                                                                                                            routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                            routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)

                                                                                                            routingParams.put("Coefvh", 1.0f);
                                                                                                            routingParams.put("Coefks", coefks);
                                                                                                            routingParams.put("CoefAi", CoefAi);

                                                                                                            if (rc >= 0.f) {
                                                                                                                if (flaghill == 1) {
                                                                                                                    routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                                                    routingParams.put("PorcPhiUnsat", 0.0f);
                                                                                                                    routingParams.put("KunsatCte", -9.9f); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                                    routingParams.put("PorcHSaturated", 0.0f);
                                                                                                                } // reservoir position:
                                                                                                                else if (flaghill == 0) {
                                                                                                                    routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("HillVelocityT", 0);
                                                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                                                } // reservoir position:
                                                                                                                else {
                                                                                                                    routingParams.put("HillT", 22);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                                                                }
                                                                                                            } else {
                                                                                                                routingParams.put("HillT", HillT);
                                                                                                                routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                                                                            }


                                                                                                            if (HillT == 0) {
                                                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", 0);
                                                                                                                routingParams.put("Coefvh", 1.0f);
                                                                                                            }
                                                                                                            if (vh > 0) {

                                                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", 1.0f);

                                                                                                            } else if (vh == -6) {
                                                                                                                routingParams.put("HillVelocityT", 6);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -1) {
                                                                                                                routingParams.put("HillVelocityT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                            } else if (vh == -7) {
                                                                                                                routingParams.put("HillVelocityT", 7);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -71) {
                                                                                                                routingParams.put("HillVelocityT", 71);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -8) {
                                                                                                                routingParams.put("HillVelocityT", 8);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -9) {
                                                                                                                routingParams.put("HillVelocityT", 9);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else if (vh == -4) {
                                                                                                                routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                            } else if (vh == -5) {
                                                                                                                routingParams.put("HillVelocityT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh);
                                                                                                                routingParams.put("Coefvh", coefvh);
                                                                                                            } else {
                                                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                            }


                                                                                                            routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions
                                                                                                            routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)


                                                                                                            //routingParams.put("RunoffCoefficient", rc); // reservoir position:ccc
                                                                                                                // + "/BFExp" + ((Float) routingParams.get("BaseFlowExp")).floatValue()
                                                                                                                //                                                                     + "/BFCoef" + ((Float) routingParams.get("BaseFlowCoef")).floatValue()
                                                                                                                SNOWMeltFile = new java.io.File("null");
                                                                                                                 SWEFile = new java.io.File("null");
                            
                                                                                                                outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/Helium_version/" + BasinName + "/" + SimName + "/" + bflag + "/");
                                                                                                                System.out.println("after");
                                                                                                                outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                                                        + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                                                                                                                        + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                                                                    + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                                                    + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                                                                                    + "/Sample" + iss 
                                                                                                                    + "/KuT_" + ((Integer) routingParams.get("KunsatModelType")).intValue()
                                                                                                                    + "/PEF_" + PotEvapFlag + "/"
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


                                                                                                            //FAST TEST
                                                                                                            //outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/luciana/Parallel/testHeliumversion/");
                                                                                                            // alphaSimulationTime.set(2010, 0, 0, 0, 0, 0);
                                                                                                            // omegaSimulationTime.set(2010, 0, 2, 0, 0, 0);
                                                                                                             stormFile = new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/SamplingErrorNH3_5/nfiles2/" +iss+"/HydroNexradRef.metaVHC");

                                                                                                            int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                                                                            outputDirectory.mkdirs();
                                                                                                            java.util.Date StartTime = new java.util.Date();
                                                                                                            new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, SNOWMeltFile, SWEFile, SOILMFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);

//                                                                                java.io.File outfolder = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/results/" + BasinName + "/" + SimName + "/");
//                                                                                outfolder.mkdirs(); 
//                                                                                java.util.Date EndTime = new java.util.Date();
//                                                                                java.io.FileWriter Fstream=new java.io.FileWriter("/nfsscratch/Users/rmantill/FinishedSimul.log",true);
//                                                                                  java.io.BufferedWriter out = new java.io.BufferedWriter(Fstream);
//                                                                                  
//                                                                                out.write("Simulation finished \n");
//                                                                                   out.write(outputDirectory.toString()+"\n");
//                                                                                   out.write(outfolder.toString()+"\n");
//                                                                                   out.write(routingParams.toString() + "\n");
//                                                                                   out.write("Starting time" + StartTime.toString() + "\n");
//                                                                                   out.write("End time" + EndTime.toString() + "\n");

//                                                                                try {
//                                                                                    java.io.File LinkAnalysisFile = new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/linksAnalyses/linksInfo2885_690.csv"); // Iowa city
//                                                                                    LinkAnalysisFile = new java.io.File(hydroScalingAPI.examples.io.ParalelVersionReader.defineLinkFile(BasinName, SimName));
//                                                                                    System.out.println(LinkAnalysisFile.getAbsoluteFile());
//                                                                                    String str = outputDirectory.toString().replace('/', '_');
//                                                                                    //String str2=str.substring(str.indexOf("Rout"));
//                                                                                    System.out.println(str);
//                                                                                    String str2 = str.substring(str.indexOf("RoutT") - 13);
//                                                                                    new hydroScalingAPI.examples.io.ParalelVersionReader(outputDirectory, LinkAnalysisFile, str2, outfolder);
//                                                                                    ///nfsscratch/Users/rmantill/results_cuencas/MultipleYearsLong2/results/3Garber2002Long/90DEMUSGS.zip
//                                                                                   String ZipFolder=outfolder.toString().replace(BasinName + "/" + SimName, BasinName + "_" + SimName + ".zip");
//                                                                                   hydroScalingAPI.examples.io.ParalelVersionReader.zipFolder(outfolder.toString(), ZipFolder);
//                                                                                   out.write("RUN Parallel Version and Create Zip \n" + (outfolder.toString()+".zip"));
//                                                                                } catch (java.io.IOException IOE) {
//                                                                                   out.write("DID NOT run Parallel Version \n");
//                                                                                }

////                                                                                    out.close();
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
    

    public static void subMainSintetic2(String args[]) throws java.io.IOException, VisADException {
//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};


        String WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts/";
        java.io.File WorkDirectory = new java.io.File(WorkDir);
        int n = 1;
        while (WorkDirectory.exists() == true) {
            //System.out.println("file exists" + WorkDir);
            n = n + 1;
            WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts" + n + "/";
            WorkDirectory = new java.io.File(WorkDir);
        }

        System.out.println("file does not exist, Create: " + WorkDir);

        WorkDirectory.mkdirs();


        String[] AllSimName = {"500DEMUSGS","1000DEMUSGS"};
    // ,"120DEMUSGS","180DEMUSGS","250DEMUSGS","500DEMUSGS" 
   //"1000DEMUSGS","2000DEMUSGS","5000DEMUSGS","10000DEMUSGS",
    //    "nldaDEMUSGS","90DEMUSGS","120DEMUSGS","180DEMUSGS",
    //    "250DEMUSGS","500DEMUSGS"
   //     "30DEMUSGS","30DEMUSGSCA"
        String[] AllRain = {"3CedarRapids"};

        String Direc = "SyntheticDEM2";
        //Direc = "TestShalehills";
        java.util.Hashtable routingParams = new java.util.Hashtable();
        routingParams.put("Outflag", 2);

        //"3ClearCreekBOhour2_2", ,"3ClearCreekBOSTAGEIV_2"
        int HillT = 20262;
        int flaghill = 20262;


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

                System.out.println(BasinName);
                System.out.println(SimName);

                stormFile = new java.io.File(definestorm(BasinName, SimName));
                //System.exit(1);
                // DEFINE THE INITIAL TIME OF THE SIMULATION
                java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
                java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                alphaSimulationTime.setTimeZone(tz);
                alphaSimulationTime.set(1971, 6, 1, 0, 0, 0);


                // DEFINE THE FINAL TIME OF THE SIMULATION
                java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                omegaSimulationTime.setTimeZone(tz);
                omegaSimulationTime.set(1971, 6, 3, 6, 0, 0);





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
                routingParams.put("KunsatCte", 1.0f); // define a constant number or -9.9 for vel=f(land Cover)
//C1111111 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0
                routingParams.put("ETolerance", 0.001f);
                routingParams.put("OutFlag", 2);

                float[] Vol = {30f, 60f, 120f};
                float[] Dur = {60f};

                float[] BasinFlagAr = {0.0f};
                int[] PotEvapFlagAr = {1};
                for (float BF : BasinFlagAr) {
                    for (int PF : PotEvapFlagAr) {
                        int PotEvapFlag = PF;

                        PotEVPTFile = new java.io.File(definePotEVPT(BasinName, SimName, PotEvapFlag));

                        java.io.File SNOWMeltFile;
                        java.io.File SWEFile;
                        java.io.File SOILMFile;
                        SOILMFile = new java.io.File("null");
                        PotEVPTFile = new java.io.File("Constant");
                        SNOWMeltFile = new java.io.File("null");
                        SWEFile = new java.io.File("null");
                        routingParams.put("EVPTIntensity",0.1f);
                        float EvptDur=(omegaSimulationTime.getTimeInMillis()-alphaSimulationTime.getTimeInMillis())/(1000.f*60.f);
                        System.out.println("EvptDur  put \n\n"+EvptDur);
                        routingParams.put("EVPTDuration",EvptDur);
                        float bflag = BF;
                        
                        routingParams.put("Basin_sim", bflag); // Cedar river - define Land cover and soil parameter
                        //float[] vsAr = {0.0002f,0.0005f};
                        float[] vsAr = {-9.f};
                        //-8 is with average VS
                        //-9is with minimal VS
                        float[] PH = {0.0f};
                        float[] Phi = {0.3f,0.0f,0.6f};
                        float[] Coefvo = {0.6f};
                        float[] coefvhAr = {1.0f};
                        float[] coefAiAr = {1.0f};
                        float[] coefksAr = {1.f};
                        float[] KunsatCAr = {5.f};
                        float[] MinPoundAr = {0.0f};
                        float[] CteSAr = {-3.0f};
                        float[] IaAre = {0.4f};
                        float[] IaArc = {0.0f};
                        int[] VParamSet = {3};
                        int[] KunsatMAr = {0};
                        //float[] rcAr = {-9.f};
                        //float[] vhAr = {-9.f};

                        float[] rcAr = {-9.0f};
                        float[] vhAr = {-6.f};

                        float[] HSAr = {0.f};

                        float[] VostdAr = {0.0f};
                        float[] kf1Ar = {1.0f};
                        float[] EVAr = {1.0f};
                        for (float MinPound : MinPoundAr) {
                            for (float KunsatC : KunsatCAr) {



                                for (float coefks : coefksAr) {
                                    for (float coefvh : coefvhAr) {

                                        for (float ev : EVAr) {
                                            for (float vo1 : Coefvo) {
                                                for (int VP : VParamSet) {
                                                    for (float CoefAi : coefAiAr) {
                                                        for (int KunsatM : KunsatMAr) {


                                                            float cvo = vo1;
                                                            float l1 = 0.15f;
                                                            float l2 = -0.82f * l1 + 0.1025f;
                                                            float vo = cvo * (0.42f - 0.29f * l1 - 3.1f * l2);

                                                            l2 = 0.05f;
                                                            vo = cvo * 0.21f;
                                                            if (VP == 3) {
                                                                l1 = 0.2f;
                                                                l2 = -0.15f;
                                                                vo = cvo * 0.75f;
                                                            }


                                                            for (float cts1 : CteSAr) {
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

                                                                                        float cts = cts1;




                                                                                        for (float in1 : PH) {
                                                                                            float p1 = in1;
                                                                                            for (float in2 : Phi) {
                                                                                                float p2 = in2;
                                                                                                for (float voo : vsAr) {
                                                                                                    float vsub = voo;
                                                                                                    for (float Vostd : VostdAr) {
                                                                                                        for (float dur : Dur) {
                                                                                                            for (float vol : Vol) {
                                                                                                                float intensity = vol / (dur / 60f);
                                                                                                                java.util.Date StartTime = new java.util.Date();

                                                                                                                routingParams.put("rainIntensity", intensity);
                                                                                                                routingParams.put("rainDuration", dur);



                                                                                                                String storm = "Constant";
                                                                                                                stormFile = new java.io.File(storm);

                                                                                                                routingParams.put("KunsatCte", KunsatC); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                                routingParams.put("v_o", vo);
                                                                                                                routingParams.put("lambda1", l1);
                                                                                                                routingParams.put("lambda2", l2);
                                                                                                                routingParams.put("CQflood", 4.0f); // reservoir position:

                                                                                                                //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                                                                                routingParams.put("CQflood", 4.0f); // reservoir position:
                                                                                                                routingParams.put("MinPondedWater", MinPound);

                                                                                                                routingParams.put("EQflood", 0.51f); // reservoir position:

                                                                                                                routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                                                                                routingParams.put("v_o", vo);
                                                                                                                routingParams.put("hillshapeparamflag", hs);
                                                                                                                routingParams.put("HillT", HillT);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                                                                                                              
                                                                                                                routingParams.put("KunsatModelType", KunsatM);
                                                                                                                //                         routingParams.put("lambda1", l11);
                                                                                                                routingParams.put("EVcoef", ev); // reservoir position:
                                                                                                                
                                                                                                                //                         routingParams.put("lambda2", l2);
                                                                                                                routingParams.put("PorcHSaturated", p1);
                                                                                                                routingParams.put("vssub", vsub);
                                                                                                                routingParams.put("PorcPhiUnsat", p2);
                                                                                                                routingParams.put("lambdaSCSMethod", iae);
                                                                                                                routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                                routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)

                                                                                                                routingParams.put("Coefvh", 1.0f);
                                                                                                                routingParams.put("Coefks", coefks);
                                                                                                                routingParams.put("CoefAi", CoefAi);

                                                                                                                if (rc >= 0.f) {
                                                                                                                    if (flaghill == 1) {
                                                                                                                        routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                        routingParams.put("RunoffCoefficient", rc);
                                                                                                                        routingParams.put("PorcPhiUnsat", 0.0f);
                                                                                                                        routingParams.put("KunsatCte", -9.9f); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                                        routingParams.put("PorcHSaturated", 0.0f);
                                                                                                                    } // reservoir position:
                                                                                                                    else if (flaghill == 0) {
                                                                                                                        routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                        routingParams.put("HillVelocityT", 0);
                                                                                                                        routingParams.put("RunoffCoefficient", rc);
                                                                                                                    } // reservoir position:
                                                                                                                    else {
                                                                                                                        routingParams.put("HillT", 22);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                        routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                                                                    }
                                                                                                                } else {
                                                                                                                    routingParams.put("HillT", HillT);
                                                                                                                    routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                                                                                }


                                                                                                                if (HillT == 0) {
                                                                                                                    routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", 0);
                                                                                                                    routingParams.put("Coefvh", 1.0f);
                                                                                                                    routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                                                                }
                                                                                                                
                                                                                                                if (vh > 0) {

                                                                                                                    routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                                    routingParams.put("Coefvh", 1.0f);

                                                                                                                } else if (vh == -6) {
                                                                                                                    routingParams.put("HillVelocityT", 6);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                                    routingParams.put("Coefvh", coefvh);
                                                                                                                } else if (vh == -1) {
                                                                                                                    routingParams.put("HillVelocityT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                                } else if (vh == -7) {
                                                                                                                    routingParams.put("HillVelocityT", 7);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                                    routingParams.put("Coefvh", coefvh);
                                                                                                                } else if (vh == -71) {
                                                                                                                    routingParams.put("HillVelocityT", 71);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                                    routingParams.put("Coefvh", coefvh);
                                                                                                                } else if (vh == -8) {
                                                                                                                    routingParams.put("HillVelocityT", 8);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                                    routingParams.put("Coefvh", coefvh);
                                                                                                                } else if (vh == -9) {
                                                                                                                    routingParams.put("HillVelocityT", 9);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                                    routingParams.put("Coefvh", coefvh);
                                                                                                                } else if (vh == -4) {
                                                                                                                    routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                                } else if (vh == -5) {
                                                                                                                    routingParams.put("HillVelocityT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                                    routingParams.put("Coefvh", coefvh);
                                                                                                                } else {
                                                                                                                    routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                    routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                                }


                                                                                                                routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions
                                                                                                                routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)


                                                                                                                //routingParams.put("RunoffCoefficient", rc); // reservoir position:ccc
                                                                                                                // + "/BFExp" + ((Float) routingParams.get("BaseFlowExp")).floatValue()
                                                                                                                //                                                                     + "/BFCoef" + ((Float) routingParams.get("BaseFlowCoef")).floatValue()

                                                                                                                outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/Helium_version/" + BasinName + "/" + SimName + "/" + bflag + "/");
                                                                                                                System.out.println("after");
                                                                                                                outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                                                        + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                                                                                                                        + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                                                                        + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                                                        + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                                                                                        + "Vol_" + vol + "Dur_" + dur + "/"
                                                                                                                        + "/RC_" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                                                                                                                        + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                                                                                        + "/Cks_" + ((Float) routingParams.get("Coefks")).floatValue()
                                                                                                                        + "/KuC_" + ((Float) routingParams.get("KunsatCte")).floatValue()
                                                                                                                        + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                                                                                        + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                                                                                        + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                                                                                        + "/Cvh_" + ((Float) routingParams.get("Coefvh")).floatValue()
                                                                                                                        + "/vo_" + ((Float) routingParams.get("v_o")).floatValue() + "_" + ((Float) routingParams.get("lambda1")).floatValue() + "_" + ((Float) routingParams.get("lambda2")).floatValue());


                                                                                                                //FAST TEST
                                                                                                                //outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/luciana/Parallel/testHeliumversion/");
                                                                                                                // alphaSimulationTime.set(2010, 0, 0, 0, 0, 0);
                                                                                                                // omegaSimulationTime.set(2010, 0, 2, 0, 0, 0);
                                                                                                                // stormFile = new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/thesis2/60mm/15min/vhc/60mm15min.metaVHC");

                                                                                                                int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                                                                                outputDirectory.mkdirs();

                                                                                                                new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, SNOWMeltFile, SWEFile, SOILMFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);

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

    public static void subMainSintetic(String args[]) throws java.io.IOException, VisADException {

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};


        String WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts/";
        java.io.File WorkDirectory = new java.io.File(WorkDir);
        int n = 1;
        while (WorkDirectory.exists() == true) {
            //System.out.println("file exists" + WorkDir);
            n = n + 1;
            WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts" + n + "/";
            WorkDirectory = new java.io.File(WorkDir);
        }

        System.out.println("file does not exist, Create: " + WorkDir);

        WorkDirectory.mkdirs();


        // String[] AllSimName = {"90DEMUSGS","30DEMASTER","30DEMUSGS",
        //                        "120DEMASTER","150DEMUSGS","180DEMUSGS"};

        String[] AllSimName = {"90DEMUSGS"};

        String[] AllRain = {"3CedarRapids"};

        String Direc = "SyntheticRainfall7";
        java.util.Hashtable routingParams = new java.util.Hashtable();
        routingParams.put("Outflag", 2);

        //"3ClearCreekBOhour2_2", ,"3ClearCreekBOSTAGEIV_2"
        int HillT = 1;
        int flaghill = 1;

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



                // DEFINE THE INITIAL TIME OF THE SIMULATION
                java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
                java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                alphaSimulationTime.setTimeZone(tz);
                alphaSimulationTime.set(1971, 6, 1, 6, 0, 0);


                // DEFINE THE FINAL TIME OF THE SIMULATION
                java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                omegaSimulationTime.setTimeZone(tz);
                omegaSimulationTime.set(1971, 6, 10, 6, 0, 0);



                alphaSimulationTime.getTimeInMillis();
                omegaSimulationTime.getTimeInMillis();


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
                routingParams.put("ETolerance", 0.001f);
                routingParams.put("OutFlag", 0);


                float[] Vol = {30f, 60f, 120f};
                float[] Dur = {30f, 60f, 120f};

                float[] BasinFlagAr = {0.0f};
                for (float BF : BasinFlagAr) {


                    float bflag = BF;

                    routingParams.put("Basin_sim", bflag); // Cedar river - define Land cover and soil parameter
                    //float[] vsAr = {0.0002f,0.0005f};
                    float[] vsAr = {};

                    float[] PH = {0.0f};
                    float[] Phi = {0.5f};
                    float[] IaAre = {0.0f};
                    float[] IaArc = {0.0f};

                    //float[] rcAr = {-9.f};
                    //float[] vhAr = {-9.f};
                    float[] Coefvo = {0.7f};
                    float[] coefvhAr = {1.0f};
                    float[] coefksAr = {10.f};

                    float[] rcAr = {-9.f};
                    float[] vhAr = {-7.0f};

                    float[] HSAr = {0.0f};
                    float[] CteSAr = {-9.0f};
                    float[] VostdAr = {0.0f};
                    float[] kf1Ar = {1.0f};
                    float[] EVAr = {1.0f};
                    int[] VParamSet = {2};



                    for (float coefks : coefksAr) {
                        for (float coefvh : coefvhAr) {
                            for (float ev : EVAr) {
                                for (float vo1 : Coefvo) {
                                    for (int VP : VParamSet) {
                                        float cvo = vo1;
                                        float l1 = 0.15f;
                                        float l2 = -0.82f * l1 + 0.1025f;
                                        float vo = cvo * (0.42f - 0.29f * l1 - 3.1f * l2);

                                        l2 = 0.05f;
                                        vo = cvo * 0.21f;


                                        if (VP == 1) {
                                            l1 = 0.15f;
                                            l2 = 0.05f;
                                            vo = cvo * 0.21f;
                                        } else if (VP == 2) {
                                            l1 = 0.3f;
                                            l2 = -0.15f;
                                            vo = cvo * 0.75f;
                                        } else if (VP == 3) {
                                            l1 = 0.2f;
                                            l2 = -0.15f;
                                            vo = cvo * 0.75f;
                                        } else if (VP == 4) {
                                            l1 = 0.24f;
                                            l2 = -0.16f;
                                            vo = cvo * 0.9f;
                                        } else if (VP == 5) {
                                            l1 = 0.35f;
                                            l2 = -0.11f;
                                            vo = cvo * 0.44f;
                                        } else if (VP == 6) {
                                            l1 = 0.2f;
                                            l2 = 0.0f;
                                            vo = cvo * 0.28f;
                                        }
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
                                                                    for (float voo : vsAr) {
                                                                        float vsub = voo;
                                                                        for (float in1 : PH) {
                                                                            float p1 = in1;
                                                                            for (float in2 : Phi) {
                                                                                float p2 = in2;
                                                                                for (float Vostd : VostdAr) {
                                                                                    for (float dur : Dur) {
                                                                                        for (float vol : Vol) {
                                                                                            float intensity = vol / (dur / 60f);
                                                                                            java.util.Date StartTime = new java.util.Date();

                                                                                            routingParams.put("rainIntensity", intensity);
                                                                                            routingParams.put("rainDuration", dur);
                                                                                            String storm = "Constant";
                                                                                            //String storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/thesis2/" + vol + "mm/" + dur + "min/vhc/" + vol + "mm" + dur + "min.metaVHC";
                                                                                            stormFile = new java.io.File(storm);
                                                                                            System.out.println("storm = " + storm);
                                                                                            String PotEVPT = "Constant";
                                                                                            PotEVPTFile = new java.io.File(PotEVPT);
                                                                                            routingParams.put("EVPTIntensity", 0.0f);
                                                                                            routingParams.put("EVPTDuration", dur);

                                                                                            routingParams.put("KunsatCte", 20.f); // define a constant number or -9.9 for vel=f(land Cover)


                                                                                            routingParams.put("EVPTIntensity", 0.0f);
                                                                                            routingParams.put("EVPTDuration", dur);

                                                                                            routingParams.put("v_o", vo);
                                                                                            routingParams.put("lambda1", l1);
                                                                                            routingParams.put("lambda2", l2);
                                                                                            routingParams.put("CQflood", 4.0f); // reservoir position:

                                                                                            //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                                                            routingParams.put("CQflood", 4.0f); // reservoir position:


                                                                                            routingParams.put("EQflood", 0.51f); // reservoir position:

                                                                                            routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                                                            routingParams.put("v_o", vo);
                                                                                            routingParams.put("hillshapeparamflag", hs);
                                                                                            routingParams.put("HillT", HillT);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions

                                                                                            //                         routingParams.put("lambda1", l11);
                                                                                            routingParams.put("EVcoef", 1.0f); // reservoir position:

                                                                                            //                         routingParams.put("lambda2", l2);
                                                                                            routingParams.put("PorcHSaturated", p1);
                                                                                            routingParams.put("vssub", vsub);
                                                                                            routingParams.put("PorcPhiUnsat", p2);
                                                                                            routingParams.put("lambdaSCSMethod", iae);
                                                                                            routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                            routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                            if (rc >= 0.f) {
                                                                                                if (flaghill == 1) {
                                                                                                    routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                                } // reservoir position:
                                                                                                else if (flaghill == 0) {
                                                                                                    routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("HillVelocityT", 0);
                                                                                                    routingParams.put("RunoffCoefficient", rc);
                                                                                                } // reservoir position:
                                                                                                else {
                                                                                                    routingParams.put("HillT", 134);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                                                }
                                                                                            } else {
                                                                                                routingParams.put("HillT", HillT);
                                                                                                routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                                                            }

                                                                                            if (HillT > 0) {
                                                                                                if (vh == -2) {
                                                                                                    routingParams.put("HillVelocityT", 2);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                } else if (vh == -6) {
                                                                                                    routingParams.put("HillVelocityT", 6);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                } else if (vh == -1) {
                                                                                                    routingParams.put("HillVelocityT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                } else if (vh == -4) {
                                                                                                    routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                } else if (vh == -5) {
                                                                                                    routingParams.put("HillVelocityT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                } else if (vh == -7) {
                                                                                                    routingParams.put("HillVelocityT", 7);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("vrunoff", vh);
                                                                                                    routingParams.put("Coefvh", coefvh);
                                                                                                } else {
                                                                                                    routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                                    routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                                }
                                                                                            }


                                                                                            routingParams.put("Coefks", coefks);
                                                                                            routingParams.put("Coefvh", coefvh);



                                                                                            routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)


                                                                                            outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                                    + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                                                                                                    + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                                                    + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                                    + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue() + "/"
                                                                                                    + "Vol_" + vol + "Dur_" + dur + "/" + "EVPTI_" + +((Float) routingParams.get("EVPTIntensity")).floatValue() + "/"
                                                                                                    + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                                                                    + "/Cks_" + ((Float) routingParams.get("Coefks")).floatValue()
                                                                                                    + "/RC" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                                                                                                    + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                                                                    + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                                                                    + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                                                                                                    + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                                                                    + "/Cvh_" + ((Float) routingParams.get("Coefvh")).floatValue()
                                                                                                    + "/vo_" + ((Float) routingParams.get("v_o")).floatValue() + "_" + ((Float) routingParams.get("lambda1")).floatValue() + "_" + ((Float) routingParams.get("lambda2")).floatValue());

                                                                                            int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                                                            outputDirectory.mkdirs();
                                                                                            System.out.println(storm);
                                                                                            //System.exit(1); 

                                                                                            //FAST TEST
                                                                                            //outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/luciana/Parallel/testHeliumversion/");
                                                                                            // alphaSimulationTime.set(2010, 0, 0, 0, 0, 0);
                                                                                            // omegaSimulationTime.set(2010, 0, 2, 0, 0, 0);
                                                                                            // stormFile = new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/thesis2/60mm/15min/vhc/60mm15min.metaVHC");


                                                                                            java.io.File SNOWMeltFile;
                                                                                            SNOWMeltFile = new java.io.File("null");
                                                                                            java.io.File SWEFile;
                                                                                            SWEFile = new java.io.File("null");
                                                                                            java.io.File SOILMFile;
                                                                                            SOILMFile = new java.io.File("null");
                                                                                            java.io.File outfolder = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/results/" + BasinName + "/" + SimName + "/");
                                                                                            outfolder.mkdirs();


                                                                                            new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, SNOWMeltFile, SWEFile, SOILMFile, PotEVPTFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);
                                                                                            java.util.Date EndTime = new java.util.Date();
                                                                                            java.io.FileWriter Fstream = new java.io.FileWriter("/nfsscratch/Users/rmantill/FinishedSimul.log", true);
                                                                                            java.io.BufferedWriter out = new java.io.BufferedWriter(Fstream);

                                                                                            out.write("Simulation finished \n");
                                                                                            out.write(outputDirectory.toString() + "\n");
                                                                                            out.write(outfolder.toString() + "\n");
                                                                                            out.write(routingParams.toString() + "\n");
                                                                                            out.write("Starting time" + StartTime.toString() + "\n");
                                                                                            out.write("End time" + EndTime.toString() + "\n");
                                                                                            out.write("DID NOT run Parallel Version \n");


                                                                                            out.close();
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
                                }
                            }
                        }
                    }

                    float[] vsAr2 = {-9.9f};
                    float[] PH2 = {0.0f};
                    float[] Phi2 = {0.0f};
                    float[] IaAre2 = {0.0f};
                    float[] IaArc2 = {0.0f};
                    float[] voAr2 = {1.0f};
                    float[] rcAr2 = {1.0f};
                    float[] vhAr2 = {36.f};
                    float[] HSAr2 = {0.f};
                    float[] CteSAr2 = {-9.f};
                    float[] EVAr2 = {1.0f};

                    for (float voo : vsAr2) {
                        float vsub = voo;
                        for (float cts1 : CteSAr2) {
                            float cts = cts1;
                            for (float rc1 : rcAr2) {
                                float rc = rc1;

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
                                                                for (float dur : Dur) {
                                                                    for (float vol : Vol) {
                                                                        float intensity = vol / (dur / 60f);
                                                                        routingParams.put("rainIntensity", intensity);
                                                                        routingParams.put("rainDuration", dur);
                                                                        String storm = "Constant";
                                                                        //String storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/thesis2/" + vol + "mm/" + dur + "min/vhc/" + vol + "mm" + dur + "min.metaVHC";
                                                                        stormFile = new java.io.File(storm);
                                                                        System.out.println("storm = " + storm);
                                                                        String PotEVPT = "Constant";
                                                                        PotEVPTFile = new java.io.File(PotEVPT);
                                                                        routingParams.put("EVPTIntensity", 0.0f);
                                                                        routingParams.put("EVPTDuration", dur);
                                                                        PotEVPTFile = new java.io.File(PotEVPT);
                                                                        //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                                        routingParams.put("CQflood", 4.0f); // reservoir position:


                                                                        routingParams.put("EQflood", 0.51f); // reservoir position:

                                                                        routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                                        routingParams.put("v_o", vo);
                                                                        routingParams.put("hillshapeparamflag", hs);
                                                                        routingParams.put("HillT", HillT);  // check NetworkEquationsLuciana.java for definitions
                                                                        routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions

                                                                        //                         routingParams.put("lambda1", l11);
                                                                        routingParams.put("EVcoef", ev1); // reservoir position:

                                                                        //                         routingParams.put("lambda2", l2);
                                                                        routingParams.put("PorcHSaturated", p1);
                                                                        routingParams.put("vssub", vsub);
                                                                        routingParams.put("PorcPhiUnsat", p2);
                                                                        routingParams.put("lambdaSCSMethod", iae);
                                                                        routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                                        routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)
                                                                        if (rc >= 0.f) {
                                                                            if (flaghill == 1) {
                                                                                routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("RunoffCoefficient", rc);
                                                                            } // reservoir position:
                                                                            else if (flaghill == 0) {
                                                                                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("HillVelocityT", 0);
                                                                                routingParams.put("RunoffCoefficient", rc);
                                                                            } // reservoir position:
                                                                            else {
                                                                                routingParams.put("HillT", 22);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                            }
                                                                        } else {
                                                                            routingParams.put("HillT", HillT);
                                                                            routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                                        }

                                                                        if (HillT > 0) {
                                                                            if (vh == -2) {
                                                                                routingParams.put("HillVelocityT", 2);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                            } else if (vh == -6) {
                                                                                routingParams.put("HillVelocityT", 6);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                            } else if (vh == -1) {
                                                                                routingParams.put("HillVelocityT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                            } else if (vh == -4) {
                                                                                routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                            } else if (vh == -5) {
                                                                                routingParams.put("HillVelocityT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh);
                                                                            } else {
                                                                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                                            }
                                                                        }


                                                                        routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions
                                                                        routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                                        outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                + "ET" + ((Float) routingParams.get("ETolerance")).floatValue() + "/"
                                                                                + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                                + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue() + "/"
                                                                                + "Vol_" + vol + "Dur_" + dur + "/" + "EVPTI_" + +((Float) routingParams.get("EVPTIntensity")).floatValue() + "/"
                                                                                + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                                                + "/RC" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                                                                                + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                                                + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                                                + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                                                                                + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                                                + "/hh_" + ((Float) routingParams.get("hillshapeparamflag")).floatValue()
                                                                                + "/vcte_" + ((Float) routingParams.get("v_o")).floatValue());

//                                            + "RR_" + ((Float) routingParams.get("RunoffCoefficient")).floatValue() +"/"
                                                                        // + "VHILL_" + ((Float) routingParams.get("vrunoff")).floatValue()

                                                                        int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                                        outputDirectory.mkdirs();

                                                                        java.io.File SNOWMeltFile;
                                                                        SNOWMeltFile = new java.io.File("null");
                                                                        java.io.File SWEFile;
                                                                        SWEFile = new java.io.File("null");
                                                                        java.io.File SOILMFile;
                                                                        SOILMFile = new java.io.File("null");
                                                                        new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, SNOWMeltFile, SWEFile, SOILMFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);
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
        }
    }

    public static void subMainRadar(String args[]) throws java.io.IOException, VisADException {

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};


        String WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts/";
        java.io.File WorkDirectory = new java.io.File(WorkDir);
        int n = 1;
        while (WorkDirectory.exists() == true) {
            //System.out.println("file exists" + WorkDir);
            n = n + 1;
            WorkDir = "/nfsscratch/Users/rmantill/qsubBin/tempScripts" + n + "/";
            WorkDirectory = new java.io.File(WorkDir);
        }

        System.out.println("file does not exist, Create: " + WorkDir);

        WorkDirectory.mkdirs();

        // String[] AllSimName = {"90DEMUSGS","30DEMASTER","30DEMUSGS",
        //                        "120DEMASTER","150DEMUSGS","180DEMUSGS"};

        String[] AllSimName = {"90DEMUSGS"};

        String[] AllRain = {"3CedarRapids"};
        //String Direc = "/RadarErrorRunsHN15BR/";
        String Direc = "/RadarErrorRunsPED3More/";
        //Direc = "/RadarErrorRunsPED8/";
        java.util.Hashtable routingParams = new java.util.Hashtable();
        routingParams.put("Outflag", 0);
        //"3ClearCreekBOhour2_2", ,"3ClearCreekBOSTAGEIV_2"
        int HillT = 134;
        int flaghill = 134;

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
                //disc=1;
                System.out.println("disc =  " + disc);
                // DEFINE THE STORM FILE
                java.io.File stormFile;
                java.io.File PotEVPTFile;


                // DEFINE THE INITIAL TIME OF THE SIMULATION
               java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
                java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                alphaSimulationTime.setTimeZone(tz);
                alphaSimulationTime.set(2008, 4, 28, 0, 0, 0);


                // DEFINE THE FINAL TIME OF THE SIMULATION
                java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                omegaSimulationTime.setTimeZone(tz);
                omegaSimulationTime.set(2008, 6, 05, 0, 0, 0);



                alphaSimulationTime.getTimeInMillis();
                omegaSimulationTime.getTimeInMillis();


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
                routingParams.put("Vcnst", 0.5f); // CHANGE IN THE NETWORKEQUATION CLA
                routingParams.put("P5Condition", -9.0f); // Porcentage of the soil filled up with water
                routingParams.put("ReserVolume", 0.0f); // m3/km2 of reservation
                routingParams.put("ReserVolumetType", 1.0f); // reservoir position:
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:
                routingParams.put("PondedWater", 0.0f);
                routingParams.put("MinPondedWater", 0.0f);

                routingParams.put("dynaIndex", 3);
                //               Initial condition
                routingParams.put("PorcHSaturated", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("PorcPhiUnsat", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("BaseFlowCoef", 0.0f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("BaseFlowExp", 0.40f); // define a constant number or -9.9 for vel=f(land Cover)

//C1111111 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0
                routingParams.put("ETolerance", 0.001f);
                routingParams.put("OutFlag", 2);

                float[] BasinFlagAr = {0.0f};
                for (float BF : BasinFlagAr) {


                    float bflag = BF;
                    //90DEMUSGS_0.0_R5H134HV7_IR_0_EV_1.0_VS-9.0_Cks0.1_Cvh2.0_RC-9.0_UnsO0.4_PH0.0_SCS_-2.0_vh_-7.0_vo_0.168_0.15_0.05     _2008_6

                    routingParams.put("Basin_sim", bflag); // Cedar river - define Land cover and soil parameter
                    //float[] vsAr = {0.0002f,0.0005f};
                    float[] vsAr = {-9.f};
                    //-8 is with average VS
                    //-9is with minimal VS
                    float[] PH = {0.0f};
                    float[] Phi = {0.4f};
                    float[] IaAre = {0.8f};
                    float[] IaArc = {0.0f};
                    float[] l1Ar = {0.15f};
                    //float[] rcAr = {-9.f};
                    //float[] vhAr = {-9.f};

                    float[] rcAr = {-9.f};
                    float[] vhAr = {-71.f};

                    float cv = 0;
                    if (BasinName.indexOf("Cedar") > 0) {
                        cv = 0.65f;
                    }
                    if (BasinName.indexOf("Iowa") > 0) {
                        cv = 0.80f;
                    }

                    float[] Coefvo = {cv};

                    float[] HSAr = {0.f};
                    float[] CteSAr = {-2.f};
                    float[] VostdAr = {0.0f};
                    float[] kf1Ar = {1.0f};
                    float[] EVAr = {1.0f};

                    float[] coefvhAr = {2.0f};
                    float[] coefksAr = {0.1f};

                    int[] VParamSet = {1};
                    //float[] rcAr = {-9.f};
                    //float[] vhAr = {-9.f
                    for (float coefvh : coefvhAr) {
                        for (float coefks : coefksAr) {
                            for (float l11 : l1Ar) {
                                for (float ev : EVAr) {
                                    for (float vo1 : Coefvo) {
                                        float cvo = vo1;
                                        float l1 = l11;
                                        float l2 = -0.82f * l11 + 0.1025f;
                                        float vo = cvo * (0.42f - 0.29f * l11 - 3.1f * l2);
                                        l2 = 0.05f;
                                        vo = cvo * 0.21f;
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
                                                                    for (float voo : vsAr) {
                                                                        float vsub = voo;
                                                                        for (float in1 : PH) {
                                                                            float p1 = in1;
                                                                            for (float in2 : Phi) {
                                                                                float p2 = in2;
                                                                                for (int iens = 48; iens <51; iens++) {
                                                                                    routingParams.put("Coefvh", coefvh);
                                                                                    routingParams.put("Coefks", coefks);
                                                                                    String ens = "error";
                                                                                    if (iens == 0 || iens == 51) {
                                                                                        ens = "MPE_IOWA_ST4.";
                                                                                    } else if (iens > 0 && iens < 10) {
                                                                                        ens = "PED_0" + iens + "_MPE_IOWA_ST4.";
                                                                                    } else {
                                                                                        ens = "PED_" + iens + "_MPE_IOWA_ST4.";
                                                                                    }
                                                                                    String storm = "error";
                                                                                    
                                                                                    storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PEDVHC3More/" + iens + "/" + ens + "metaVHC";

                                                                                    if (iens == 0) {
                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/OriginalVHC/" + ens + "metaVHC";
                                                                                    }
                                                                                    if (iens == 51) {
                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/AfterBiasVHC/" + ens + "metaVHC";
                                                                                    }
                                                                                    if (BasinName.contains("ALL")) {

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/DataGenNov_2010/Animation/NEXRAD_BC.metaVHC";
                                                                                    }
                                                                                    if (iens == 52) { // run for 2 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/WithoutGeomBias/2/60min/Time/Bin/MPE_IOWA_ST4.metaVHC";
                                                                                    }
                                                                                    if (iens == 53) { // run for 3 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/WithoutGeomBias/3/60min/Time/Bin/MPE_IOWA_ST4.metaVHC";
                                                                                    }

                                                                                    if (iens == 54) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/WithoutGeomBias/4/60min/Time/Bin/MPE_IOWA_ST4.metaVHC";
                                                                                    }

                                                                                    if (iens == 55) { // run for 2 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/WithoutGeomBias/2/180min/Time/Bin/MPE_IOWA_ST4.metaVHC";
                                                                                    }
                                                                                    if (iens == 56) { // run for 3 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/WithoutGeomBias/3/180min/Time/Bin/MPE_IOWA_ST4.metaVHC";
                                                                                    }

                                                                                    if (iens == 57) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/WithoutGeomBias/4/180min/Time/Bin/MPE_IOWA_ST4.metaVHC";
                                                                                    }

                                                                                    if (iens == 58) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/WithoutGeomBias/5/60min/Time/Bin/MPE_IOWA_ST4.metaVHC";
                                                                                    }

                                                                                    if (iens == 59) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/WithoutGeomBias/5/180min/Time/Bin/MPE_IOWA_ST4.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("Sampling")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/SamplingError/Sample1xdt3hour/" + iens + "/MPE_IOWA_ST4.metaVHC";
                                                                                    }


                                                                                    if (Direc.contains("Samp5min")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/SamplingError/Sample1xdt3hour/" + iens + "/MPE_IOWA_ST4.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("RadarErrorRunsHN60")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_Nexrad/Rain60VHCSim/HydroNexradRef.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("RadarErrorRunsHN15")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_Nexrad/Rain15VHCSim/HydroNexradRef.metaVHC";
                                                                                    }
                                                                                    if (Direc.contains("RadarErrorRunsHN60BR")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_Nexrad/Rain60VHCBiasRem0.66/HydroNexradRef.metaVHC";
                                                                                    }
                                                                                    if (Direc.contains("RadarErrorRunsHN15BR")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_Nexrad/Rain15VHCBiasRem0.7/HydroNexradRef.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("RadarErrorRunsHN15BRProd2")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_NexradProd2/Rain15VHCBiasRem0.7/HydroNexradRef.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("RadarErrorRunsHN60BRProd2")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_NexradProd2/Rain60VHCBiasRem0.7/HydroNexradRef.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("RadarErrorRunsHN15BRProd3")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_NexradProd3/15minVHC/HydroNexradRef.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("RadarErrorRunsHN60BRProd3")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_NexradProd3/60minVHC/HydroNexradRef.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("PERSIANN")) { // run for 4 times resolution in space

                                                                                        storm = "//nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Satellite/PERSIANNVHC/m6s4.metaVHC";
                                                                                    }
                                                                                    if (Direc.contains("CMORPH")) { // run for 4 times resolution in space

                                                                                        storm = "//nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Satellite/CMORPHVHC/Iowa_CMORPH.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("TRMM")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/TRMM_V6VHC/2008/Iowa_TRMM_V6.metaVHC";
                                                                                    }
                                                                                    if (Direc.contains("TRMMv2")) { // run for 4 times resolution in space

                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/3B42TRMM2008/3B42.metaVHC";
                                                                                    }

                                                                                    if (Direc.contains("TRMMv3")) { // run for 4 times resolution in space
                                                                                        storm = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/Mult33B42TRMM2008/3B42.metaVHC";
                                                                                    }



                                                                                    stormFile = new java.io.File(storm);
                                                                                    System.out.println("storm = " + storm);
                                                                                    String PotEVPT = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/8DaysData/Data4Sim3/PET/2008Short/IowaPET.metaVHC";
                                                                                    int PotEvapFlag = 2; // 1 - MODIS, 2 _ NLDAS   
                                                                                    PotEVPTFile = new java.io.File(definePotEVPT(BasinName, SimName, PotEvapFlag));
                                                                                    routingParams.put("v_o", vo);
                                                                                    routingParams.put("lambda1", l1);
                                                                                    routingParams.put("lambda2", l2);
                                                                                    routingParams.put("CQflood", 4.0f); // reservoir position:

                                                                                    //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                                                    routingParams.put("CQflood", 4.0f); // reservoir position:


                                                                                    routingParams.put("EQflood", 0.51f); // reservoir position:

                                                                                    routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                                                    routingParams.put("v_o", vo);
                                                                                    routingParams.put("hillshapeparamflag", hs);
                                                                                    routingParams.put("HillT", HillT);  // check NetworkEquationsLuciana.java for definitions
                                                                                    routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions

                                                                                    //                         routingParams.put("lambda1", l11);
                                                                                    routingParams.put("EVcoef", ev); // reservoir position:

                                                                                    //                         routingParams.put("lambda2", l2);
                                                                                    routingParams.put("PorcHSaturated", p1);
                                                                                    routingParams.put("vssub", vsub);
                                                                                    routingParams.put("PorcPhiUnsat", p2);
                                                                                    routingParams.put("lambdaSCSMethod", iae);
                                                                                    routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                    routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                    if (rc >= 0.f) {
                                                                                        if (flaghill == 1) {
                                                                                            routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("RunoffCoefficient", rc);
                                                                                        } // reservoir position:
                                                                                        else if (flaghill == 0) {
                                                                                            routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("HillVelocityT", 0);
                                                                                            routingParams.put("RunoffCoefficient", rc);
                                                                                        } // reservoir position:
                                                                                        else {
                                                                                            routingParams.put("HillT", 22);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("RunoffCoefficient", rc); // reservoir position:  
                                                                                        }
                                                                                    } else {
                                                                                        routingParams.put("HillT", HillT);
                                                                                        routingParams.put("RunoffCoefficient", -9.f); // reservoir position:
                                                                                    }

                                                                                    if (HillT > 0) {
                                                                                        if (vh == -2) {
                                                                                            routingParams.put("HillVelocityT", 2);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("vrunoff", vh);
                                                                                        } else if (vh == -7) {
                                                                                            routingParams.put("HillVelocityT", 7);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("vrunoff", vh);
                                                                                        } else if (vh == -6) {
                                                                                            routingParams.put("HillVelocityT", 6);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("vrunoff", vh);
                                                                                        } else if (vh == -1) {
                                                                                            routingParams.put("HillVelocityT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("vrunoff", vh);
                                                                                        } else if (vh == -4) {
                                                                                            routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("vrunoff", vh);
                                                                                        } else if (vh == -5) {
                                                                                            routingParams.put("HillVelocityT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("vrunoff", vh);
                                                                                         } else if (vh == -71) {
                                                                                            routingParams.put("HillVelocityT", 71);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("vrunoff", vh);   
                                                                                        } else {
                                                                                            routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                                                            routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                                                        }

                                                                                    }

                                                                                    routingParams.put("ConstSoilStorage", cts);  // check NetworkEquationsLuciana.java for definitions
                                                                                    routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)


                                                                                    //routingParams.put("RunoffCoefficient", rc); // reservoir position:ccc
                                                                                    // + "/BFExp" + ((Float) routingParams.get("BaseFlowExp")).floatValue()
                                                                                    //                                                                     + "/BFCoef" + ((Float) routingParams.get("BaseFlowCoef")).floatValue()

                                                                                    // outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/Helium_version/" + BasinName + "/" + SimName + "/" + bflag + "/"

                                                                                    outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/results_cuencas/" + Direc + "/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                                            + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                                            + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                                            + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                                                            + "/IR_" + iens + "/"
                                                                                            + "/EV_" + ((Float) routingParams.get("EVcoef")).floatValue()
                                                                                            + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                                                            + "/Cks" + ((Float) routingParams.get("Coefks")).floatValue()
                                                                                            + "/Cvh" + ((Float) routingParams.get("Coefvh")).floatValue()
                                                                                            + "/RC" + ((Float) routingParams.get("RunoffCoefficient")).floatValue()
                                                                                            + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                                                            + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                                                            + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                                                                                            + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                                                            + "/vo_" + ((Float) routingParams.get("v_o")).floatValue() + "_" + ((Float) routingParams.get("lambda1")).floatValue() + "_" + ((Float) routingParams.get("lambda2")).floatValue());

                                                                                    int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                                                    outputDirectory.mkdirs();
                                                                                    System.out.println(storm);
                                                                                    String[] Snow = defineSNOW(BasinName, SimName);
                                                                                    java.io.File SNOWMeltFile;
                                                                                    SNOWMeltFile = new java.io.File(Snow[0]);
                                                                                    java.io.File SWEFile;
                                                                                    SWEFile = new java.io.File(Snow[1]);
                                                                                    java.io.File SOILMFile;
                                                                                    SOILMFile = new java.io.File("null");

                                                                                    new ParallelSimulationToFileHelium(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, PotEVPTFile, SNOWMeltFile, SWEFile, SOILMFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, disc, WorkDir);

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
        if (BasinName.indexOf("Clear") >= 0) {
        if (SimName.contains("ASTER")) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM";
            xOut = 1596;
            yOut = 298;
        }
        
        if (SimName.equals("5DEMLIDAR")) {
            xOut = 8052;
            yOut = 497;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/5meters/5meterc1.metaDEM";
        }
        if (SimName.equals("10DEMLIDAR")) {
            xOut = 4025;
            yOut = 244;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM";
        }
        if (SimName.equals("20DEMLIDAR")) {
            xOut = 2013;
            yOut = 122;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/20meters/20meterc1.metaDEM";
        }
        if (SimName.equals("30DEMLIDAR")) {
            xOut = 1341;
            yOut = 82;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30meters/30meterc1.metaDEM";
        }
        if (SimName.equals("60DEMLIDAR")) {
            xOut = 670;
            yOut = 41;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/60meters/60meterc1.metaDEM";
        }
        if (SimName.equals("90DEMLIDAR")) {
            xOut = 447;
            yOut = 27;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM";
        }
        
        
        if (SimName.equals("5DEMLIDARCA")) {
            xOut = 8052;
            yOut = 497;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/5metersCA/5meterc1.metaDEM";
        }
        if (SimName.equals("10DEMLIDARCA")) {
            xOut = 4025;
            yOut = 244;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10metersCA/10meterc1.metaDEM";
        }
        if (SimName.equals("20DEMLIDARCA")) {
            xOut = 2013;
            yOut = 122;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/20metersCA/20meterc1.metaDEM";
        }
        if (SimName.equals("30DEMLIDARCA")) {
            xOut = 1341;
            yOut = 82;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30metersCA/30meterc1.metaDEM";
        }
        if (SimName.equals("60DEMLIDARCA")) {
            xOut = 670;
            yOut = 41;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/60metersCA/60meterc1.metaDEM";
        }
        if (SimName.equals("90DEMLIDARCA")) {
            xOut = 447;
            yOut = 27;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90metersCA/90meterc1.metaDEM";
        }
        
        if (SimName.equals("90DEMUSGS")) {
            xOut = 2817;
            yOut = 713;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (SimName.equals("30DEMUSGS")) {
            xOut = 1541;
            yOut = 92;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM";
        }

        if (SimName.equals("10DEMUSGS")) {
            xOut = 4624;
            yOut = 278;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM";
        }
        }
        if (BasinName.indexOf("Cedar") >= 0) {

            xOut = 2734;
            yOut = 1069; //Cedar Rapids
            
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";

        }

        if (BasinName.indexOf("Waverly") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
            xOut = 1770;
            yOut = 1987;
        }


        if (BasinName.indexOf("Marengo") >= 0) {
            xOut = 2256;
            yOut = 876;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
        if (BasinName.indexOf("Iowa") >= 0) {
            xOut = 2885;
            yOut = 690;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (BasinName.indexOf("Garber") >= 0) {
            xOut = 3217;
            yOut = 1989;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }


        if (BasinName.indexOf("Turkey") >= 0) {

            xOut = 3053;
            yOut = 2123;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (BasinName.indexOf("Volga") >= 0) {
            xOut = 3091;
            yOut = 2004;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (BasinName.indexOf("Hoover") >= 0) {
            xOut = 3113;
            yOut = 705;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

         if (SimName.indexOf("90DEMUSGS") >= 0 && SimName.indexOf("Prun1") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun1/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
 
         if (SimName.indexOf("90DEMUSGS") >= 0 && SimName.indexOf("Prun3") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun3/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
 
       if (SimName.indexOf("90DEMUSGS") >= 0 && SimName.indexOf("Prun4") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun4/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
 
        if (SimName.indexOf("90DEMUSGS") >= 0 && SimName.indexOf("Prun5") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun5/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
        if (SimName.indexOf("90DEMUSGS") >= 0 && SimName.indexOf("Prun6") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun6/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
        if (SimName.indexOf("90DEMUSGS") >= 0 && SimName.indexOf("Prun7") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun7/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }
        if (SimName.indexOf("90DEMUSGS") >= 0 && SimName.indexOf("Prun8") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun8/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }



        if (SimName.indexOf("120DEMUSGS") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs120m.metaDEM";
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
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs180m.metaDEM";
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



        if (SimName.indexOf("150DEMUSGS") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs150m.metaDEM";
            if (BasinName.indexOf("Clear") >= 0) {
                xOut = 1690;
                yOut = 428;
            }
            if (BasinName.indexOf("Cedar") >= 0) {
                xOut = 1639;
                yOut = 641;
            }

            if (BasinName.indexOf("Iowa") >= 0) {
                xOut = 1731;
                yOut = 413;
            }

        }
         if (SimName.indexOf("USGS") >= 0 && BasinName.indexOf("Iowa") >= 0) { 
        if (SimName.indexOf("250DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs250.metaDEM";
             xOut = 1039;
             yOut = 249;
            }
            
           if (SimName.indexOf("500DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs500.metaDEM";
             xOut = 519;
             yOut = 124;
            }
              if (SimName.indexOf("1000DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs1000.metaDEM";
             xOut = 259;
             yOut = 62;
            }
                if (SimName.indexOf("5000DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs5000.metaDEM";
             xOut = 49;
             yOut = 15;
            }
             if (SimName.indexOf("10000DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs10000.metaDEM";
             xOut = 23;
             yOut = 7;
            }
                 if (SimName.indexOf("nldaDEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgsnlda.metaDEM";
             xOut = 40;
             yOut = 13;
            }}
         
           if (SimName.indexOf("USGS") >= 0 && BasinName.indexOf("Cedar") >= 0) { 
        if (SimName.indexOf("250DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs250.metaDEM";
             xOut = 983;
             yOut = 385;
            }
            
           if (SimName.indexOf("500DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs500.metaDEM";
             xOut = 492;
             yOut = 193;
            }
              if (SimName.indexOf("1000DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs1000.metaDEM";
             xOut = 246;
             yOut = 97;
            }
                if (SimName.indexOf("5000DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs5000.metaDEM";
             xOut = 49;
             yOut = 18;
            }
             if (SimName.indexOf("10000DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs10000.metaDEM";
             xOut = 24;
             yOut = 9;
            }
                 if (SimName.indexOf("nldaDEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgsnlda.metaDEM";
             xOut = 42;
             yOut = 16;
            }}

        if (SimName.indexOf("90DEMASTER") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/30meterASTER/90master.metaDEM";
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

        if (SimName.indexOf("ASTER") >= 0 && BasinName.indexOf("Cedar") >= 0) {
        if (SimName.indexOf("30DEM") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/30meterASTER/ce30maf.metaDEM";
            
                xOut = 8124;
                yOut = 510;
        }
        if (SimName.indexOf("60DEM") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/30meterASTER/60master.metaDEM";
            
                xOut = 4084;
                yOut = 1494;
        }
        
        if (SimName.indexOf("90DEM") >= 0) {
               OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/30meterASTER/90master.metaDEM";
               xOut = 2723;
                yOut = 997;
        }
         if (SimName.indexOf("120DEM") >= 0) {
               OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/30meterASTER/120master.metaDEM";
               xOut = 2042;
                yOut = 747;
        }
        }
        
          if (SimName.indexOf("ASTER") >= 0 && BasinName.indexOf("Clear") >= 0) {
        
        if (SimName.indexOf("60DEM") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/30meterASTER/60master.metaDEM";
            
                xOut = 4211;
                yOut = 962;
        }
        
        if (SimName.indexOf("90DEM") >= 0) {
               OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/30meterASTER/90master.metaDEM";
               xOut = 2808;
                yOut = 642;
        }
         if (SimName.indexOf("120DEM") >= 0) {
               OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/30meterASTER/120master.metaDEM";
               xOut = 2106;
                yOut = 480;
        }
        }
        
        if (SimName.indexOf("30DEMUSGS") >= 0) {

            if (BasinName.indexOf("Clear") >= 0) {
                OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM";
                xOut = 8288;
                yOut = 1029;
            }

            if (BasinName.indexOf("Cedar") >= 0) {
                OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM";
                xOut = 7875;
                yOut = 1361;
            }

            if (BasinName.indexOf("Iowa") >= 0) {
                OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM";
                xOut = 8443;
                yOut = 1029;

            }

            if (BasinName.indexOf("IowaUps") >= 0) {
                OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM";
                xOut = 5505;
                yOut = 1871;

            }

            if (BasinName.indexOf("Waverly") >= 0) {
                OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM";
                xOut = 5468;
                yOut = 3237;
            }

        }
        
        if (SimName.indexOf("90DEMSRTM") >= 0) {

            if (BasinName.indexOf("Cedar") >= 0) {
   
        OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/SRTM/srtm3arc.metaDEM";
             xOut = 2801;
                yOut = 1167;
            }
         if (BasinName.indexOf("Clear") >= 0) {
   
        OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/SRTM/srtm3arc.metaDEM";
             xOut = 2887;
                yOut = 813;
            }
        }
        
        if (SimName.indexOf("USGSCA") >= 0) {
               
             if (SimName.indexOf("30DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/CedarRiver.metaDEM";
             xOut = 7875;
             yOut = 1361;
            }
             if (SimName.indexOf("90DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/AveragedIowaRiverAtColumbusJunctions.metaDEM";
             xOut = 2734;
             yOut = 1069; //Cedar Rapids
            }
             if (SimName.indexOf("120DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/usgs120m.metaDEM";
                xOut = 2050;
                yOut = 802;
            }
             if (SimName.indexOf("150DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/usgs150m.metaDEM";
             xOut = 1639;
             yOut = 641;
            }
            if (SimName.indexOf("180DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/usgs180m.metaDEM";
             xOut = 1367;
             yOut = 534;
            }
             if (SimName.indexOf("250DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/usgs250.metaDEM";
             xOut = 983;
             yOut = 385;
            }
            if (SimName.indexOf("500DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/usgs500.metaDEM";
             xOut = 492;
             yOut = 193;
            }
              if (SimName.indexOf("1000DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/usgs1000.metaDEM";
             xOut = 246;
             yOut = 96;
            }
                if (SimName.indexOf("5000DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/usgs5000.metaDEM";
             xOut = 49;
             yOut = 18;
            }
             if (SimName.indexOf("10000DEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/usgs10000.metaDEM";
             xOut = 24;
             yOut = 9;
            }
                 if (SimName.indexOf("nldaDEM") >= 0) {
             OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS_CA/usgsnlda.metaDEM";
             xOut = 42;
             yOut = 16;
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

        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("SRTM") > 0) {
            disc = 5;
        }
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("30DEMUSGS") > 0) {
            disc = 3;
        }
        if (BasinName.indexOf("Turkey") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 5;
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

        if (BasinName.indexOf("Marengo") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 4;
        }

        if (BasinName.indexOf("Janes") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 4;
        }

        if (BasinName.indexOf("Garber") > 0 && SimName.indexOf("USGS") > 0) {
            disc = 5;
        }
        

        if (BasinName.indexOf("disc4") > 0) {
            disc = 4;
        }

        if (BasinName.indexOf("disc3") > 0) {
            disc = 3;
        }

        if (BasinName.indexOf("Hoover") > 0) {
            disc = 1;
        }
        if (BasinName.indexOf("Garber") > 0 && SimName.indexOf("Prun7") >= 0) {
            disc = 4;
        }
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun8") >= 0) {
            disc = 5;
        }
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun7") >= 0) {
            disc = 4;
        }
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun6") >= 0) {
            disc = 3;
        }
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun5") >= 0) {
            disc = 2;
        }
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun4") >= 0) {
            disc = 1;
        }
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun3") >= 0) {
            disc = 1;
        }
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun2") >= 0) {
            disc = 1;
        }
        if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("Prun1") >= 0) {
            disc = 1;
        }

        if (BasinName.indexOf("Iowa") > 0 && (SimName.indexOf("Prun8") >= 0 || SimName.indexOf("Prun7") >= 0)) {
            disc = 3;
        }

        if (BasinName.indexOf("Iowa") > 0 && SimName.indexOf("Prun6") >= 0) {
            disc = 2;
        }
        if (BasinName.indexOf("Iowa") > 0 && (SimName.indexOf("Prun5") >= 0)) {
            disc = 2;
        }
        if (BasinName.indexOf("Iowa") > 0 && (SimName.indexOf("Prun1") >= 0 || SimName.indexOf("Prun2") >= 0 || SimName.indexOf("Prun3") >= 0 || SimName.indexOf("Prun4") >= 0)) {
            disc = 1;
        }

        if (BasinName.indexOf("Clear") > 0) {
            disc = 3;
        }

        if (BasinName.indexOf("Clear") > 0 && SimName.indexOf("Prun5") >= 0) {
            disc = 1;
        }

        if (BasinName.indexOf("Clear") > 0 && SimName.indexOf("Prun4") >= 0) {
            disc = 1;
        }
        if (BasinName.indexOf("Clear") > 0 && SimName.indexOf("Prun6") >= 0) {
            disc = 1;
        }


        if (BasinName.indexOf("Clear") > 0 && SimName.indexOf("Prun8") >= 0) {
            disc = 1;
        }
        if (BasinName.indexOf("Clear") > 0 && SimName.indexOf("Prun7") >= 0) {
            disc = 1;
        }
        if (BasinName.indexOf("Shalehills") >= 0) {
            disc = 1;
        }
         if (BasinName.indexOf("250") > 0 || BasinName.indexOf("nlda") > 0 || SimName.indexOf("00") >= 0) {
            disc = 1;
        }

        return disc;
    }

    public static String definestorm(String BasinName, String SimName) {
        String stormFile = "error";
        stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC";
        //1 space and 180 time
        if (BasinName.indexOf("1_180") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/180min/Time/Bin/hydroNexrad.metaVHC";
        }
        //6 space and 15 time
        if (BasinName.indexOf("6_15") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/15min/Time/Bin/hydroNexrad.metaVHC";
        }

        if (BasinName.indexOf("15_360") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/360min/Time/Bin/hydroNexrad.metaVHC";
        }
        //6 space and 180 tim
        if (BasinName.indexOf("6_180") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("15_15") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/15min/Time/Bin/hydroNexrad.metaVHC";
        }

        if (BasinName.indexOf("15_180") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/180min/Time/Bin/hydroNexrad.metaVHC";
        }

        if (BasinName.indexOf("6_180") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("1_60") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/60min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("2_60") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/2/60min/Time/Bin/hydroNexrad.metaVHC";
        }

        if (BasinName.indexOf("6_60") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/60min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("15_60") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/60min/Time/Bin/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("ori") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC";
        }
        if (BasinName.indexOf("PERS") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/PERSIANN/May29Jun17/PERSIANN_3h.metaVHC";
        }
        if (BasinName.indexOf("TRMM") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/RC1.00/bin/rain17/prec.metaVHC";
        }
        if (BasinName.contains("TRMMv2")) { // run for 4 times resolution in space

            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/3B42TRMM2008/3B42.metaVHC";
        }

        if (BasinName.contains("TRMMv3")) { // run for 4 times resolution in space
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/Mult33B42TRMM2008/3B42.metaVHC";
        }


        if (BasinName.indexOf("NOBIASCORR") > 0) {
            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/DataGenNov_2010/Radar_merged_Vhc/NEXRAD_BC.metaVHC";
        }
        if (BasinName.indexOf("BO") > 0) {
            if (BasinName.indexOf("hour") > 0) {
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/Bo_events/20Aug2002_24Aug2002VHC/NEXRAD_BC.metaVHC";
            }
            if (BasinName.indexOf("hour2") > 0) {
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/Bo_events/20Aug2002_24Aug2002VHC2/NEXRAD_BC.metaVHC";
            }

            if (BasinName.indexOf("5min") > 0) {
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/Bo_events/Rain5minVHCFixed/NEXRAD_BC.metaVHC";
            }
            if (BasinName.indexOf("STAGEIV") > 0) {
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/Bo_events/RadarSTAGEIV/NEXRAD_BC.metaVHC";
            }
        }
        if (BasinName.indexOf("Adv") > 0) {
            if (BasinName.indexOf("May") > 0) {

                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/May2009_AdvectionVHC/NEXRAD_BC.metaVHC";
                if (BasinName.indexOf("No") > 0) {
                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/May2009_NoAdvectionVHC/NEXRAD_BC.metaVHC";
                }
            }
            if (BasinName.indexOf("Mar") > 0) {
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_AdvectionVHC/NEXRAD_BC.metaVHC";
                if (BasinName.indexOf("No") > 0) {
                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_NoAdvectionVHC/NEXRAD_BC.metaVHC";
                }
            }
            if (BasinName.indexOf("Jun") > 0) {
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/Jun2009_AdvectionVHC/NEXRAD_BC.metaVHC";
                if (BasinName.indexOf("No") > 0) {
                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/Jun2009_NoAdvectionVHC/NEXRAD_BC.metaVHC";
                }
            }
        }

        //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC";
        int index_2 = BasinName.indexOf("20");

        if (BasinName.indexOf("20") > 0) {
            String timeStamp = BasinName.substring(index_2, index_2 + 4);
            int yr = java.lang.Integer.parseInt(timeStamp);

            stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/" + yr + "VHC/NEXRAD_BC.metaVHC";
            if (BasinName.indexOf("Long") > 0 || BasinName.indexOf("Time") > 0) {
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/" + yr + "04TO09/NEXRAD_BC.metaVHC";
                System.out.println("eLong" + stormFile);
                //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/" + yr + "VHCAug/NEXRAD_BC.metaVHC";
            }
            
             if (BasinName.indexOf("Event") > 0 && BasinName.indexOf("2008") > 0) {
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/2008VHCEvent/NEXRAD_BC.metaVHC";
                System.out.println("Long" + stormFile);
                //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/" + yr + "VHCAug/NEXRAD_BC.metaVHC";
            }

            if (BasinName.indexOf("Long") > 0 && BasinName.indexOf("NWS") > 0) {

                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/NWS_MAP/After_NWSMAP" + yr + "/NWS_MAP.metaVHC";
            }


            if (BasinName.indexOf("Long") > 0 && BasinName.indexOf("NWS2") > 0) {

                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/NWS_MAP/After2_NWSMAP" + yr + "/NWS_MAP.metaVHC";
            }
            
             if (BasinName.indexOf("Long") > 0 && BasinName.indexOf("NWS3") > 0) {

                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/NWS_MAP/After3_NWSMAP" + yr + "/NWS_MAP.metaVHC";
            }

            if (BasinName.indexOf("NLDASPrec") > 0) {

                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/NLDASPrec/PREC_VHC/" + yr + "/APCP.metaVHC";
            }


            if (BasinName.indexOf("PER") > 0) {
                //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/PERSIANN/vhc_sim_period/" + yr + "/PERSIANN_3h.metaVHC";
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/PERSIANNVHC/" + yr + "/m6s4.metaVHC";

            }

            if (BasinName.indexOf("CMORPH") > 0) {
                //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/PERSIANN/vhc_sim_period/" + yr + "/PERSIANN_3h.metaVHC";
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/CMORPHVHC/" + yr + "/Iowa_CMORPH.metaVHC";

            }

            if (BasinName.indexOf("TRMM") > 0) {
                //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/PERSIANN/vhc_sim_period/" + yr + "/PERSIANN_3h.metaVHC";
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/TRMM_V6VHC/" + yr + "/Iowa_TRMM_V6.metaVHC";

            }

            if (BasinName.indexOf("Event") > 0 || BasinName.indexOf("test") > 0) {


                //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/PERSIANN/vhc_sim_period/" + yr + "/PERSIANN_3h.metaVHC";
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PaperBongChul/StageIV1.0/HydroNexradRef.metaVHC";

                if (BasinName.contains("HN1_60")) { // run for 4 times resolution in space

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PaperBongChul/Prod1VHC1.0/HydroNexradRef.metaVHC";
                }

                if (BasinName.contains("HN1_15")) { // run for 4 times resolution in space

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_Nexrad/Rain15VHCSim/HydroNexradRef.metaVHC";
                }
                if (BasinName.contains("HN1_60BR")) { // run for 4 times resolution in space

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PaperBongChul/Prod1VHC0.7/HydroNexradRef.metaVHC";
                }
                if (BasinName.contains("HN1_60BR2")) { // run for 4 times resolution in space

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PaperBongChul/Prod1VHC0.83/HydroNexradRef.metaVHC";
                }


                if (BasinName.contains("HN1_15BR")) { // run for 4 times resolution in space

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_Nexrad/Rain15VHCBiasRem0.7/HydroNexradRef.metaVHC";
                }

                if (BasinName.contains("HN2_60BR")) { // run for 4 times resolution in space

                    stormFile = "//nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PaperBongChul/Prod2VHC0.7/HydroNexradRef.metaVHC";
                }
                if (BasinName.contains("HN2_15BR")) { // run for 4 times resolution in space

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_NexradProd2/Rain15VHCBiasRem0.7/HydroNexradRef.metaVHC";
                }

                if (BasinName.contains("HN3_15BR")) { // run for 4 times resolution in space

                    stormFile = "//nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PaperBongChul/Prod3VHC1.0/HydroNexradRef.metaVHC";
                }
                if (BasinName.contains("HN3_5BR")) { // run for 4 times resolution in space

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PaperBongChul/Prod3_5min1.0/HydroNexradRef.metaVHC";
                }
                if (BasinName.contains("HN3_60BR")) { // run for 4 times resolution in space

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_NexradProd3/60minVHC/HydroNexradRef.metaVHC";
                }

                if (BasinName.contains("PERSIANN")) { // run for 4 times resolution in space

                    stormFile = "//nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Satellite/PERSIANNVHC/m6s4.metaVHC";
                }
                if (BasinName.contains("CMORPH")) { // run for 4 times resolution in space

                    stormFile = "//nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Satellite/CMORPHVHC/Iowa_CMORPH.metaVHC";
                }

                if (BasinName.contains("TRMMv2")) { // run for 4 times resolution in space

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/3B42TRMM2008/3B42.metaVHC";
                }

                if (BasinName.contains("TRMMv3")) { // run for 4 times resolution in space
                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/Mult33B42TRMM2008/3B42.metaVHC";
                }

                if (BasinName.indexOf("NWS") > 0) {

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/NWS_MAP/After_NWSMAP" + yr + "/NWS_MAP.metaVHC";
                }
                
                if (BasinName.indexOf("NWS3") > 0) {

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/NWS_MAP/After3_NWSMAP" + yr + "/NWS_MAP.metaVHC";
                }
                if (BasinName.indexOf("NLDASPrec") > 0) {

                    stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/NLDASPrec/PREC_VHC/" + yr + "/APCP.metaVHC";
                }


            }


            if (BasinName.indexOf("hills") >= 0) {
                stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Shalehills/Rasters/Hydrology/1974Event/1974Event.metaVHC";
            }
            System.out.println("year  " + yr);

//
        }
        return stormFile;

    }

    public static String definePotEVPT(String BasinName, String SimName, int flag) {

        String PotEVPTFile = "error";
        PotEVPTFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/8DaysData/Data4Sim3/PET/2008Short/IowaPET.metaVHC";
        if (BasinName.indexOf("BO") > 0) {
            if (BasinName.indexOf("hour") > 0) {
                PotEVPTFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/8DaysData/Data4Sim3/PET/2008Short/IowaPET.metaVHC";
            }

        }


        //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC";
        int index_2 = BasinName.indexOf("20");

        if (BasinName.indexOf("20") > 0) {
            String timeStamp = BasinName.substring(index_2, index_2 + 4);
            int yr = java.lang.Integer.parseInt(timeStamp);

            PotEVPTFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_MOD16/8DaysData/Data4SimLong/PET/" + yr + "/IowaPET.metaVHC";

            System.out.println("year  " + yr);
            if (flag == 1) {
                PotEVPTFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_NLDAS/Data4Sim03_09/" + yr + "/PEVAP.metaVHC";
            }
            if (flag == 1 && BasinName.indexOf("Event") > 0) {
                PotEVPTFile = "//nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/EVPT_NLDAS/Data4Sim03_09/Event2008/PEVAP.metaVHC";
            }
//

        }

        return PotEVPTFile;


    }

    public static String[] defineSNOW(String BasinName, String SimName) {

        String SNOWMeltFile = "error";
        SNOWMeltFile = "null";
        String SWEFile = "error";
        SWEFile = "null";



        //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC";
        int index_2 = BasinName.indexOf("20");

        if (BasinName.indexOf("20") > 0) {
            String timeStamp = BasinName.substring(index_2, index_2 + 4);
            int yr = java.lang.Integer.parseInt(timeStamp);

            if (BasinName.indexOf("SNLDA") > 0) {
                SNOWMeltFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/Snow/" + yr + "/SNOM.metaVHC";
                SWEFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/SnowAMSR/SWE1dayMar/" + yr + "/AMSR_E_L3_SWE.metaVHC";
            } else {

                SNOWMeltFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/SnowAMSR/SnowMelt1dayMar/" + yr + "/AMSR_E_L3_Snowmelt.metaVHC";
                SWEFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/SnowAMSR/SWE1dayMar/" + yr + "/AMSR_E_L3_SWE.metaVHC";
            }
            if (yr == 2002) {
                SNOWMeltFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/Snow/" + yr + "/SNOM.metaVHC";
                SWEFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/SnowAMSR/SWE1dayMar/" + yr + "/AMSR_E_L3_SWE.metaVHC";
            }
        }


        String[] result = {"null", "null"};
        result[0] = SNOWMeltFile;
        result[1] = SWEFile;
        return result;


    }

    public static String defineSOILM(String BasinName, String SimName) {

        String SNOWMeltFile = "error";
        SNOWMeltFile = "null";


        //stormFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC";
        int index_2 = BasinName.indexOf("20");

        if (BasinName.indexOf("20") > 0) {
            String timeStamp = BasinName.substring(index_2, index_2 + 4);
            int yr = java.lang.Integer.parseInt(timeStamp);

            SNOWMeltFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/MSTAV0-40/" + yr + "/MSTAV:0-40.metaVHC";
            if (BasinName.indexOf("_0200") > 0) {
                SNOWMeltFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/MSTAV0-200/" + yr + "SIM/MSTAV:0-200.metaVHC";
            }
        }

         if (BasinName.indexOf("20") > 0) {
                 SNOWMeltFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/MSTAV0-200/2008SIMEvent/MSTAV:0-200.metaVHC"; 
         }
        return SNOWMeltFile;


    }

    public static String defineLinkFile(String BasinName, String SimName) {

        // DEFINE THE DEM and x and y
        int xOut = 2817;
        int yOut = 713; //90METERDEMClear Creek - coralville
        String[] OUTPUT = {"error", "errorx", "errory"};
        if (SimName.contains("ASTER")) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM";
            xOut = 1596;
            yOut = 298;
        }
        if (SimName.equals("5DEMLIDAR")) {
            xOut = 8052;
            yOut = 497;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/5meters/5meterc1.metaDEM";
        }
        if (SimName.equals("10DEMLIDAR")) {
            xOut = 4025;
            yOut = 244;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM";
        }
        if (SimName.equals("20DEMLIDAR")) {
            xOut = 2013;
            yOut = 122;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/20meters/20meterc1.metaDEM";
        }
        if (SimName.equals("30DEMLIDAR")) {
            xOut = 1341;
            yOut = 82;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30meters/30meterc1.metaDEM";
        }
        if (SimName.equals("60DEMLIDAR")) {
            xOut = 670;
            yOut = 41;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/60meters/60meterc1.metaDEM";
        }
        if (SimName.equals("90DEMLIDAR")) {
            xOut = 447;
            yOut = 27;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM";
        }
        if (SimName.equals("90DEMUSGS")) {
            xOut = 2817;
            yOut = 713;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (SimName.equals("30DEMUSGS")) {
            xOut = 1541;
            yOut = 92;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM";
        }

        if (SimName.equals("10DEMUSGS")) {
            xOut = 4624;
            yOut = 278;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM";
        }

        if (BasinName.indexOf("Cedar") >= 0) {
            xOut = 2734;
            yOut = 1069; //Cedar Rapids
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }


        if (BasinName.indexOf("Iowa") >= 0) {
            xOut = 2885;
            yOut = 690;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (BasinName.indexOf("Marengo") >= 0) {
            xOut = 2256;
            yOut = 876;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }


        if (BasinName.indexOf("Turkey") >= 0) {

            xOut = 3053;
            yOut = 2123;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (BasinName.indexOf("Volga") >= 0) {
            xOut = 3091;
            yOut = 2004;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (BasinName.indexOf("Garber") >= 0) {
            xOut = 3217;
            yOut = 1989;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (SimName.indexOf("120DEMUSGS") >= 0) {
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs120m.metaDEM";
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
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs180m.metaDEM";
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

        if (BasinName.indexOf("Hoover") >= 0) {
            xOut = 3113;
            yOut = 705;
            OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
        }

        if (SimName.indexOf("90DEMSRTM") >= 0) {

            if (BasinName.indexOf("Cedar") >= 0) {

                OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/SRTM/srtm3arc.metaDEM";
                xOut = 2801;
                yOut = 1167;
            }
            
            if (BasinName.indexOf("Clear") >= 0) {

                OUTPUT[0] = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/SRTM/srtm3arc.metaDEM";
                xOut = 2884;
                yOut = 816;
            }
        }

        String NFile = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/linksAnalyses/linksInfo" + xOut + "_" + yOut + ".csv";


        System.out.println("NFile = " + NFile);

        return NFile;
    }
}