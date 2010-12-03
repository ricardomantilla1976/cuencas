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
public class ParallelSimulationToFile extends java.lang.Object {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    private int simulProcess = 2;
    public int threadsRunning = 0;
    java.util.Hashtable<String, Boolean> compNodeNames = new java.util.Hashtable<String, Boolean>();
    private java.util.Calendar zeroSimulationTime,endingSimulationTime;
    float lam1, lam2, v_o;
    int dynaIndex;

    /** Creates new ParallelSimulationToFile */
    public ParallelSimulationToFile() {
    }

    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, java.util.Hashtable MyCnodes, int numNodes, int dscale) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, horOrders, md, rainIntensity, rainDuration, null, null, infiltRate, routingType, routingParams, outputDirectory, zST, eST, MyCnodes, numNodes, dscale);
    }

    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, java.util.Hashtable MyCnodes, int numNodes, int dscale) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, horOrders, md, 0.0f, 0.0f, stormFile, infiltMetaRaster, 0.0f, routingType, routingParams, outputDirectory, zST, eST, MyCnodes, numNodes, dscale);
    }

    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, java.util.Hashtable MyCnodes, int numNodes, int dscale) throws java.io.IOException, VisADException {
        this(x, y, direcc, magnitudes, horOrders, md, 0.0f, 0.0f, stormFile, null, infiltRate, routingType, routingParams, outputDirectory, zST, eST, MyCnodes, numNodes, dscale);
    }

    public ParallelSimulationToFile(int x, int y, byte[][] direcc, int[][] magnitudes, byte[][] horOrders, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, float infiltRate, int routingType, java.util.Hashtable routingParams, java.io.File outputDirectory, java.util.Calendar zST, java.util.Calendar eST, java.util.Hashtable MyCnodes, int numNodes, int dscale) throws java.io.IOException, VisADException {

        zeroSimulationTime = zST;
        endingSimulationTime = eST;
        matDir = direcc;
        metaDatos = md;

        lam1 = ((Float) routingParams.get("lambda1")).floatValue();
        lam2 = ((Float) routingParams.get("lambda2")).floatValue();
        v_o = ((Float) routingParams.get("v_o")).floatValue();
        int dynaIndex = 0;

        if (routingParams.get("dynaIndex") != null) {
            dynaIndex = ((Integer) routingParams.get("dynaIndex")).intValue();
        }
        System.out.println("main - dynaindex=" + dynaIndex);
        compNodeNames = MyCnodes;
        simulProcess = numNodes;

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
        hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ExternalTileToFile[] externalExecutors = new hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ExternalTileToFile[connectionTopology.length];

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

            externalExecutors[i] = new hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ExternalTileToFile("Element " + i, md.getLocationMeta().getAbsolutePath(), xOutlet, yOutlet, xSource, ySource, decompScale, routingType, lam1, lam2, v_o, stormFile.getAbsolutePath(), infiltRate, outputDirectory.getAbsolutePath(), connectionString, correctionString, this, zeroSimulationTime.getTimeInMillis(), endingSimulationTime.getTimeInMillis(), dynaIndex, routingParams);
        }

        boolean allNodesDone = true;

        java.util.Date startTime = new java.util.Date();
        System.out.println("Start Time - Total:" + startTime.toString());
        System.out.println("Parallel Code Begins");

        float currentDtoO = maxDtoO;

        // System.exit(0);

        do {
            System.out.print(">> Processes on Hold: ");
            System.out.println("externalExecutors.length = " + externalExecutors.length + "\n");
            for (int i = 0; i < externalExecutors.length; i++) {
                int indexProc = processList.get((String) v.get(i));
                System.out.println("indexProc = " + indexProc + "\n");
                float thisDtoO = Float.parseFloat(((String) v.get(i)).substring(0, 9));
                System.out.println("externalExecutors[indexProc].completed = " + externalExecutors[indexProc].completed + "\n");
                if (externalExecutors[indexProc].completed == false && externalExecutors[indexProc].executing == false) {
                    boolean required = true;
                    for (int j = 0; j < connectionTopology[indexProc].length; j++) {
                        required &= externalExecutors[((Integer) topoMapping.get(connectionTopology[indexProc][j])).intValue()].completed;
                    }
                    if (required) {

                        //simThreads[indexProc].executeSimulation();

                        String cn = findFreeNode();

                        System.out.println();
                        System.out.println(">> Process " + indexProc + " is being launched");
                        externalExecutors[indexProc].setComputingNode(cn, indexProc);
                        activeThreads[indexProc] = new Thread(externalExecutors[indexProc]);
                        externalExecutors[indexProc].executing = true;
                        compNodeNames.put(cn, true);

                        threadsRunning++;
                        activeThreads[indexProc].start();
                        currentDtoO = Math.min(currentDtoO, thisDtoO);
                        if (threadsRunning == simulProcess) {
                            break;
                        }
                    } else {
                        System.out.print(indexProc + ",");
                    }
                } else if (externalExecutors[indexProc].completed == true) {
                    activeThreads[indexProc] = null;
                }
            }
            System.out.println();
            int counterSeconds = 0;
            while (threadsRunning == simulProcess) {
                System.out.println(">>>>>  CURRENTLY RUNNING " + threadsRunning + " THREADS. Out of " + simulProcess + ".Percentage Completed: " + ((1 - currentDtoO / maxDtoO) * 100) + "%");
                new visad.util.Delay(1000);
            }

            //System.exit(0);

            System.out.println(">>>>>  CURRENTLY RUNNING " + threadsRunning + " THREADS. Out of " + simulProcess + ". Percentage Completed: " + ((1 - currentDtoO / maxDtoO) * 100) + "%");

            System.out.print(">> Running");
            for (int i = 0; i < externalExecutors.length; i++) {
                if (externalExecutors[i].executing) {
                    System.out.print(", " + i);
                }
            }
            System.out.println();

            new visad.util.Delay(1000);

            allNodesDone = true;
            for (int i = 0; i < externalExecutors.length; i++) {
                allNodesDone &= externalExecutors[i].completed;
            }

        } while (!allNodesDone);

        System.out.println("Parallel Code Ends");
        java.util.Date endTime = new java.util.Date();
        System.out.println("End Time Total:" + endTime.toString());
        System.out.println("Parallel Running Time Total:" + routingParams.toString());
        System.out.println("Parallel Running Time Total:" + (.001 * (endTime.getTime() - startTime.getTime())) + " seconds");

    }

    private String findFreeNode() {
        for (Iterator it = compNodeNames.keySet().iterator(); it.hasNext();) {
            String keyName = (String) it.next();
            Boolean nodeState = (Boolean) compNodeNames.get(keyName);
            if (!nodeState.booleanValue()) {
                return keyName;
            }
        }
        return "NA";
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {


        System.out.println("The scheduler is being launched!");
        System.out.println("List of Available nodes:");
        System.out.println(java.util.Arrays.toString(args));

        java.util.Vector<String> theNodes=new java.util.Vector<String>();


        try {
            java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(args[0])));
            String fullLine;
            fullLine=fileMeta.readLine();
            int avaNodes=Integer.parseInt(fullLine.split(" ")[1]);
            for (int i = 0; i < avaNodes-1; i++) theNodes.add(fullLine.split(" ")[0]+"_"+i);
            fullLine=fileMeta.readLine();
            while (fullLine != null) {
                System.out.println(fullLine);
                avaNodes=Integer.parseInt(fullLine.split(" ")[1]);
                for (int i = 0; i < avaNodes; i++) theNodes.add(fullLine.split(" ")[0]+"_"+i);
                fullLine=fileMeta.readLine();
            }
            fileMeta.close();
        } catch (IOException ex) {
            Logger.getLogger(ParallelSimulationToFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        Object[] argsObj=theNodes.toArray();
        args=new String[argsObj.length];
        for (int i = 0; i < argsObj.length; i++) {
            args[i]=(String)argsObj[i];
        }

        System.out.println(java.util.Arrays.toString(args));

        try {
            //subMainEvent(args);
            subMainMaria(args);
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v) {
            System.out.print(v);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void subMainEvent(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String, Boolean> myNodeNames = new java.util.Hashtable<String, Boolean>();
        for (int j = 0; j < args.length; j++) {
            myNodeNames.put( args[j], false);
        }

        int numNodes = myNodeNames.size();

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};

        String[] AllSimName = {"90DEMLIDAR",};

        String[] AllRain = {"3ClearCreek_TRMM"};

        //  "3ClearCreek_6_15",
//            "3ClearCreek_15_15",
//            "3ClearCreek_1_180",
//            "3ClearCreek_6_180",
//            "3ClearCreek_15_180",
//            "3ClearCreek_PERSIAN",

//           String[] AllRain = {
//            "3CedarRiver_TRMM",
//            "3CedarRiver_1_15"
//            };

        //String[] AllRain = {"3ClearCreek_PERSIAN"};

        int nsim = AllSimName.length;
        int nbas = AllRain.length;

        for (int i = 0; i < nsim; i++) {
            for (int ib = 0; ib < nbas; ib++) {

                System.out.println("Running BASIN " + AllSimName[i]);
                System.out.println("Running BASIN " + AllRain[ib]);

                String SimName = AllSimName[i];
                String BasinName = AllRain[ib];
                String DEM = "error";
                
                // DEFINE THE DEM
                int xOut = 2817;
                int yOut = 713; //90METERDEMClear Creek - coralville

                if (SimName.equals("ASTER")) {
                    DEM = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM";
                    xOut = 1596;
                    yOut = 298;
                }
                if (SimName.equals("5DEMLIDAR")) {
                    xOut = 8052;
                    yOut = 497;
                    DEM = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/5meters/5meterc1.metaDEM";
                }
                if (SimName.equals("10DEMLIDAR")) {
                    xOut = 4025;
                    yOut = 244;
                    DEM = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM";
                }
                if (SimName.equals("20DEMLIDAR")) {
                    xOut = 2013;
                    yOut = 122;
                    DEM = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/20meters/20meterc1.metaDEM";
                }
                if (SimName.equals("30DEMLIDAR")) {
                    xOut = 1341;
                    yOut = 82;
                    DEM = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30meters/30meterc1.metaDEM";
                }
                if (SimName.equals("60DEMLIDAR")) {
                    xOut = 670;
                    yOut = 41;
                    DEM = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/60meters/60meterc1.metaDEM";
                }
                if (SimName.equals("90DEMLIDAR")) {
                    xOut = 447;
                    yOut = 27;
                    DEM = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM";
                }
                if (SimName.equals("90DEMUSGS")) {
                    xOut = 2817;
                    yOut = 713;
                    DEM = "/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                if (SimName.equals("30DEMUSGS")) {
                    xOut = 1541;
                    yOut = 92;
                    DEM = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM";
                }

                if (SimName.equals("10DEMUSGS")) {
                    xOut = 4624;
                    yOut = 278;
                    DEM = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM";
                }

                if (BasinName.indexOf("Cedar") > 0) {
                    xOut = 2734;
                    yOut = 1069; //Cedar Rapids
                    DEM = "/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                java.io.File theFile = new java.io.File(DEM);
                //java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
//        java.io.File theFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM");
//         int xOut = 4025; int yOut = 244;//10METERDEMClear Creek - coralville
//        java.io.File theFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM");
//  int xOut =  447; int yOut = 27;//90METERDEMClear Creek - coralville
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


                // DEFINE THE STORM FILE - IF PRECIPITATION IS NOT CONSTANT

                //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

                // DEFINE THE INITIAL TIME OF THE SIMULATION
                java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);


                // DEFINE THE FINAL TIME OF THE SIMULATION
                java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);

                java.io.File outputDirectory;


                //int xOut =  1341;int yOut =  82;//30METERDEMClear Creek - coralville

                int disc = 4;
                if (BasinName.indexOf("Cedar") > 0 && SimName.indexOf("USGS") > 0) {
                    disc = 5;
                }
//        int xOut = 2734;
//        int yOut = 1069; //Cedar Rapids
//        int disc=5;
//        String BasinName="3CedarRapids";
                //1 space and 15 time
                stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC");
                //1 space and 180 time
                if (BasinName.indexOf("1_180") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/180min/Time/Bin/hydroNexrad.metaVHC");
                }
                //6 space and 15 time
                if (BasinName.indexOf("6_15") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/15min/Time/Bin/hydroNexrad.metaVHC");
                }
                //6 space and 180 tim
                if (BasinName.indexOf("6_180") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("15_15") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/15min/Time/Bin/hydroNexrad.metaVHC");
                }

                if (BasinName.indexOf("15_180") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/180min/Time/Bin/hydroNexrad.metaVHC");
                }

                if (BasinName.indexOf("6_180") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("1_60") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/60min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("6_60") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/60min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("15_60") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/60min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("ori") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("PERS") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/PERSIANN/May29Jun17/PERSIANN_3h.metaVHC");
                }
                if (BasinName.indexOf("TRMM") > 0) {
                    stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/RC1.00/bin/rain17/prec.metaVHC");
                }


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
                routingParams.put("Basin_sim", 0.0f); // Cedar river - define Land cover and soil parameter
                routingParams.put("dynaIndex", 3);
                // Initial condition
                routingParams.put("PorcHSaturated", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("PorcPhiUnsat", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("BaseFlowCoef", 0.0f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("BaseFlowExp", 0.40f); // define a constant number or -9.9 for vel=f(land Cover)

//C1111111 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0
                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 0.0f); // reservoir position:

                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                        + "RoutT_2/"
                        + "/HillT_0/"
                        + "/HillVelT_0"
                        + "/RR_1");


                int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                // outputDirectory.mkdirs();
                //   new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C2222222 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=0.5
                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:

                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                        + "RoutT_2/"
                        + "/HillT_0/"
                        + "/HillVelT_0"
                        + "/RR_0.5");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //             outputDirectory.mkdirs();
                //             new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C333333 RT=2 (cte vel). HillT=1 (const runoff), HillVel=1 (cte hill delay), RR=0.5
                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:
                routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)
                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                        + "RoutT_2/"
                        + "/HillT_1/"
                        + "/HillVelT_0_v50"
                        + "/RR_0.5");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //              outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C444444 RT=2 (cte vel). HillT=1 (const runoff), HillVel=3 (no hill delay), RR=0.5
                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 3);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:

                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                        + "RoutT_2/"
                        + "/HillT_1/"
                        + "/HillVelT_3"
                        + "/RR_0.5");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //            outputDirectory.mkdirs();
                //            new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C555555 RT=5 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0
                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 0.0f); // reservoir position:

                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                        + "RoutT_5/"
                        + "/HillT_0/"
                        + "/HillVelT_0"
                        + "/RR_1");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //                    outputDirectory.mkdirs();
                //new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C666666 RT=5 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=0.5
                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:

                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                        + "RoutT_5/"
                        + "/HillT_0/"
                        + "/HillVelT_0"
                        + "/RR_0.5lambda0.4/");
//routingParams.put("v_o", 0.4f);
//                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
//                outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C7777777 RT=5 (cte vel). HillT=1 (const runoff), HillVel=0 (cte hill delay), RR=0.5
                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:
                routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)
                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                        + "RoutT_5/"
                        + "/HillT_1/"
                        + "/HillVelT_0_v50"
                        + "/RR_0.5");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //              outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C8888888 RT=5 (cte vel). HillT=1 (const runoff), HillVel=3 (no hill delay), RR=0.5
                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 3);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:

                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                        + "RoutT_5/"
                        + "/HillT_1/"
                        + "/HillVelT_3"
                        + "/RR_0.5");

                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //           outputDirectory.mkdirs();
                //           new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);



                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/TestCC/");

                java.io.File theFile1 = new java.io.File(outputDirectory + "/Param.csv");
                java.io.FileOutputStream salida1 = new java.io.FileOutputStream(theFile1);
                java.io.BufferedOutputStream bufferout1 = new java.io.BufferedOutputStream(salida1);
                java.io.OutputStreamWriter newfile1 = new java.io.OutputStreamWriter(bufferout1);

                newfile1.write(outputDirectory + "\n");
                newfile1.write("xOut=" + xOut + ",yOut" + yOut + "\n");
                newfile1.write(stormFile + "\n");
                newfile1.write(routingParams.toString());
                newfile1.close();
                bufferout1.close();
                float[] vsAr = {0.0025f};
                float[] PH = {0.1f};
                float[] Phi = {0.1f};
                float[] IaAr = {0.02f};

                for (float voo : vsAr) {
                    float vsub = voo;
                    for (float in1 : PH) {
                        float p1 = in1;
                        for (float in2 : Phi) {
                            float p2 = in2;
                            for (float ia1 : IaAr) {
                                float ia = ia1;
//C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant

                                routingParams.put("PorcHSaturated", p1);
                                routingParams.put("vssub", vsub);
                                routingParams.put("PorcPhiUnsat", p2);
                                routingParams.put("lambdaSCSMethod", ia);
                                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("ConstSoilStorage", 130.f);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)


                                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                                        + "Rout_2Hill_3CteRC130_HVelCte50/"
                                        + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
                                        + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                        + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                        + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());



                                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                //              outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C10 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant

                                routingParams.put("PorcHSaturated", p1);
                                routingParams.put("vssub", vsub);
                                routingParams.put("PorcPhiUnsat", p2);
                                routingParams.put("lambdaSCSMethod", ia);
                                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("ConstSoilStorage", 130.f);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)


                                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                                        + "Rout_5Hill_3CteRC130_HVelCte50/"
                                        + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
                                        + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                        + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                        + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());




                                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                //              outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

////C11 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
//
//                    routingParams.put("PorcHSaturated", p1);
//                    routingParams.put("vssub", vsub);
//                    routingParams.put("PorcPhiUnsat", p2);
//                    routingParams.put("lambdaSCSMethod", ia);
//                    routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("HillVelocityT", 3);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("ConstSoilStorage",130.f);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)
//
//
//                    outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/"+ BasinName + "/"+SimName+"/"
//                            + "Rout_5Hill_3CteRC130_HVel3/"
//                            + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
//                            + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
//                            + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
//                            + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());
//
//
//
//                    outputDirectory.mkdirs();
//                    rrt = ((Integer) routingParams.get("RoutingT")).intValue();
//
//                    new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);


//C12 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - distributed
                                routingParams.put("PorcHSaturated", p1);
                                routingParams.put("vssub", vsub);
                                routingParams.put("PorcPhiUnsat", p2);
                                routingParams.put("lambdaSCSMethod", ia);
                                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("ConstSoilStorage", -9.f);  // check NetworkEquationsLuciana.java for definitions



                                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                                        + "Rout_2Hill_3VarRC_HVelCte50/"
                                        + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
                                        + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                        + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                        + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());




                                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                //            outputDirectory.mkdirs();
                                //             new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);


//C13 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - distributed
//                      routingParams.put("PorcHSaturated", p1);
//                    routingParams.put("vssub", vsub);
//                    routingParams.put("PorcPhiUnsat", p2);
//                    routingParams.put("lambdaSCSMethod", ia);
//                    routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("ConstSoilStorage",-9.f);  // check NetworkEquationsLuciana.java for definitions
//
//
//
//                    outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/"+ BasinName + "/"+SimName+"/"
//                            + "Rout_5Hill_3VarRC_HVelCte50/"
//                            + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
//                            + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
//                            + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
//                            + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());
//
//
//
//                    outputDirectory.mkdirs();
//                    rrt = ((Integer) routingParams.get("RoutingT")).intValue();
//
//                    new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C14 RT=5 (cte vel). HillT=3 , HillVel=3 (hill veloc cte), SCS method - distributed
                                routingParams.put("PorcHSaturated", p1);
                                routingParams.put("vssub", vsub);
                                routingParams.put("PorcPhiUnsat", p2);
                                routingParams.put("lambdaSCSMethod", ia);
                                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillVelocityT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("ConstSoilStorage", -9);  // check NetworkEquationsLuciana.java for definitions


                                outputDirectory = new java.io.File("/usr/home/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
                                        + "Rout_5Hill_3/"
                                        + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
                                        + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                        + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                        + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());

                                routingParams.put("v_o", 0.88f);
                                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                outputDirectory.mkdirs();
                                new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);


                            }
                        }
                    }
                }
            }
        }
    }

    public static void subMainMaria(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String, Boolean> myNodeNames = new java.util.Hashtable<String, Boolean>();
        for (int j = 0; j < args.length; j++) {
            myNodeNames.put( args[j], false);
        }

        int numNodes = myNodeNames.size();



//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};

        String[] AllSimName = {"90DEMUSGS",};

        String[] AllRain = {"3ClearCreek"};

        //  "3ClearCreek_6_15",
//            "3ClearCreek_15_15",
//            "3ClearCreek_1_180",
//            "3ClearCreek_6_180",
//            "3ClearCreek_15_180",
//            "3ClearCreek_PERSIAN",

//           String[] AllRain = {
//            "3CedarRiver_TRMM",
//            "3CedarRiver_1_15"
//            };

        //String[] AllRain = {"3ClearCreek_PERSIAN"};

        int nsim = AllSimName.length;
        int nbas = AllRain.length;

        for (int i = 0; i < nsim; i++) {
            for (int ib = 0; ib < nbas; ib++) {

                System.out.println("Running BASIN " + AllSimName[i]);
                System.out.println("Running BASIN " + AllRain[ib]);

                String SimName = AllSimName[i];
                String BasinName = AllRain[ib];
                String DEM = "error";
                
                // DEFINE THE DEM
                int xOut = 2817;
                int yOut = 713; //90METERDEMClear Creek - coralville


                // This is clear creek
                    xOut = 447;
                    yOut = 27;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM";




                java.io.File theFile = new java.io.File(DEM);
                //java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
//        java.io.File theFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM");
//         int xOut = 4025; int yOut = 244;//10METERDEMClear Creek - coralville
//        java.io.File theFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM");
//  int xOut =  447; int yOut = 27;//90METERDEMClear Creek - coralville
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


                // DEFINE THE STORM FILE - IF PRECIPITATION IS NOT CONSTANT

                //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

                // DEFINE THE INITIAL TIME OF THE SIMULATION
                java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);


                // DEFINE THE FINAL TIME OF THE SIMULATION
                java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);

                java.io.File outputDirectory;
//
                //int xOut =  1341;int yOut =  82;//30METERDEMClear Creek - coralville

                   int disc = 3;

//        int xOut = 2734;
//        int yOut = 1069; //Cedar Rapids
//        int disc=5;
//        String BasinName="3CedarRapids";
                //1 space and 15 time
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/test/hydroNexrad.metaVHC");



                routingParams.put("v_o", 0.63f);
                routingParams.put("lambda1", 0.32f);
                routingParams.put("lambda2", -0.13f);

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
                routingParams.put("Basin_sim", 0.0f); // Cedar river - define Land cover and soil parameter
                routingParams.put("dynaIndex", 3);
                // Initial condition
                routingParams.put("PorcHSaturated", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("PorcPhiUnsat", 0.8f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("BaseFlowCoef", 0.0f); // define a constant number or -9.9 for vel=f(land Cover)
                routingParams.put("BaseFlowExp", 0.40f); // define a constant number or -9.9 for vel=f(land Cover)

//C1111111 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0
                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 0.0f); // reservoir position:

                outputDirectory = new java.io.File("/Users/rmantill/Documents/Results_ClearCreek_test/" + BasinName + "/" + SimName + "/"
                        + "RoutT_2/"
                        + "/HillT_0/"
                        + "/HillVelT_0"
                        + "/RR_1");

                routingParams.put("v_o", 0.63f);
                int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                 outputDirectory.mkdirs();
                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

                 System.exit(0);

//C2222222 RT=2 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=0.5
                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:

                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                        + "RoutT_2/"
                        + "/HillT_0/"
                        + "/HillVelT_0"
                        + "/RR_0.5");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //             outputDirectory.mkdirs();
                //             new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C333333 RT=2 (cte vel). HillT=1 (const runoff), HillVel=1 (cte hill delay), RR=0.5
                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:
                routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)
                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                        + "RoutT_2/"
                        + "/HillT_1/"
                        + "/HillVelT_0_v50"
                        + "/RR_0.5");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //              outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C444444 RT=2 (cte vel). HillT=1 (const runoff), HillVel=3 (no hill delay), RR=0.5
                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 3);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:

                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                        + "RoutT_2/"
                        + "/HillT_1/"
                        + "/HillVelT_3"
                        + "/RR_0.5");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //            outputDirectory.mkdirs();
                //            new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C555555 RT=5 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=1.0
                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 0.0f); // reservoir position:

                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                        + "RoutT_5/"
                        + "/HillT_0/"
                        + "/HillVelT_0"
                        + "/RR_1");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //                    outputDirectory.mkdirs();
                //new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C666666 RT=5 (cte vel). HillT=0 (const runoff), HillVel=0 (no hill delay), RR=0.5
                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:

                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                        + "RoutT_5/"
                        + "/HillT_0/"
                        + "/HillVelT_0"
                        + "/RR_0.5lambda0.4/");
//routingParams.put("v_o", 0.4f);
//                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
//                outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C7777777 RT=5 (cte vel). HillT=1 (const runoff), HillVel=0 (cte hill delay), RR=0.5
                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:
                routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)
                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                        + "RoutT_5/"
                        + "/HillT_1/"
                        + "/HillVelT_0_v50"
                        + "/RR_0.5");
                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //              outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C8888888 RT=5 (cte vel). HillT=1 (const runoff), HillVel=3 (no hill delay), RR=0.5
                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("HillVelocityT", 3);  // check NetworkEquationsLuciana.java for definitions
                routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:

                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                        + "RoutT_5/"
                        + "/HillT_1/"
                        + "/HillVelT_3"
                        + "/RR_0.5");

                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                //           outputDirectory.mkdirs();
                //           new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);



                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/");

                java.io.File theFile1 = new java.io.File(outputDirectory + "/Param.csv");
                java.io.FileOutputStream salida1 = new java.io.FileOutputStream(theFile1);
                java.io.BufferedOutputStream bufferout1 = new java.io.BufferedOutputStream(salida1);
                java.io.OutputStreamWriter newfile1 = new java.io.OutputStreamWriter(bufferout1);

                newfile1.write(outputDirectory + "\n");
                newfile1.write("xOut=" + xOut + ",yOut" + yOut + "\n");
                newfile1.write(stormFile + "\n");
                newfile1.write(routingParams.toString());
                newfile1.close();
                bufferout1.close();
                float[] vsAr = {0.0025f};
                float[] PH = {0.1f};
                float[] Phi = {0.1f};
                float[] IaAr = {0.02f};

                for (float voo : vsAr) {
                    float vsub = voo;
                    for (float in1 : PH) {
                        float p1 = in1;
                        for (float in2 : Phi) {
                            float p2 = in2;
                            for (float ia1 : IaAr) {
                                float ia = ia1;
//C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant

                                routingParams.put("PorcHSaturated", p1);
                                routingParams.put("vssub", vsub);
                                routingParams.put("PorcPhiUnsat", p2);
                                routingParams.put("lambdaSCSMethod", ia);
                                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("ConstSoilStorage", 130.f);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)


                                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                                        + "Rout_2Hill_3CteRC130_HVelCte50/"
                                        + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
                                        + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                        + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                        + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());



                                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                //              outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C10 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant

                                routingParams.put("PorcHSaturated", p1);
                                routingParams.put("vssub", vsub);
                                routingParams.put("PorcPhiUnsat", p2);
                                routingParams.put("lambdaSCSMethod", ia);
                                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("ConstSoilStorage", 130.f);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)


                                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                                        + "Rout_5Hill_3CteRC130_HVelCte50/"
                                        + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
                                        + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                        + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                        + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());




                                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                //              outputDirectory.mkdirs();
//                 new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

////C11 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
//
//                    routingParams.put("PorcHSaturated", p1);
//                    routingParams.put("vssub", vsub);
//                    routingParams.put("PorcPhiUnsat", p2);
//                    routingParams.put("lambdaSCSMethod", ia);
//                    routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("HillVelocityT", 3);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("ConstSoilStorage",130.f);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("vrunoff", 50.f); // define a constant number or -9.9 for vel=f(land Cover)
//
//
//                    outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/"+ BasinName + "/"+SimName+"/"
//                            + "Rout_5Hill_3CteRC130_HVel3/"
//                            + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
//                            + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
//                            + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
//                            + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());
//
//
//
//                    outputDirectory.mkdirs();
//                    rrt = ((Integer) routingParams.get("RoutingT")).intValue();
//
//                    new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);


//C12 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - distributed
                                routingParams.put("PorcHSaturated", p1);
                                routingParams.put("vssub", vsub);
                                routingParams.put("PorcPhiUnsat", p2);
                                routingParams.put("lambdaSCSMethod", ia);
                                routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("ConstSoilStorage", -9.f);  // check NetworkEquationsLuciana.java for definitions



                                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                                        + "Rout_2Hill_3VarRC_HVelCte50/"
                                        + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
                                        + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                        + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                        + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());




                                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                //            outputDirectory.mkdirs();
                                //             new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);


//C13 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - distributed
//                      routingParams.put("PorcHSaturated", p1);
//                    routingParams.put("vssub", vsub);
//                    routingParams.put("PorcPhiUnsat", p2);
//                    routingParams.put("lambdaSCSMethod", ia);
//                    routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
//                    routingParams.put("ConstSoilStorage",-9.f);  // check NetworkEquationsLuciana.java for definitions
//
//
//
//                    outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/"+ BasinName + "/"+SimName+"/"
//                            + "Rout_5Hill_3VarRC_HVelCte50/"
//                            + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
//                            + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
//                            + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
//                            + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());
//
//
//
//                    outputDirectory.mkdirs();
//                    rrt = ((Integer) routingParams.get("RoutingT")).intValue();
//
//                    new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

//C14 RT=5 (cte vel). HillT=3 , HillVel=3 (hill veloc cte), SCS method - distributed
                                routingParams.put("PorcHSaturated", p1);
                                routingParams.put("vssub", vsub);
                                routingParams.put("PorcPhiUnsat", p2);
                                routingParams.put("lambdaSCSMethod", ia);
                                routingParams.put("RoutingT", 5); // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("HillVelocityT", 3);  // check NetworkEquationsLuciana.java for definitions
                                routingParams.put("ConstSoilStorage", -9);  // check NetworkEquationsLuciana.java for definitions


                                outputDirectory = new java.io.File("/Users/luciana/Documents/Results_Turkey/" + BasinName + "/" + SimName + "/"
                                        + "Rout_5Hill_3/"
                                        + "/IA" + ((Float) routingParams.get("lambdaSCSMethod")).floatValue()
                                        + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                        + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                        + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue());

                                routingParams.put("v_o", 0.88f);
                                rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                   //             outputDirectory.mkdirs();
                   //             new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);


                            }
                        }
                    }
                }
            }
        }
    }
}
