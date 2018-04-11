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
import java.util.TimeZone;

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
    private java.util.Calendar zeroSimulationTime, endingSimulationTime;
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
                if (externalExecutors[indexProc].completed == false && externalExecutors[indexProc].executing == false)
                {
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

        java.util.Vector<String> theNodes = new java.util.Vector<String>();


        try {
            java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(args[0])));
            String fullLine;
            fullLine = fileMeta.readLine();
            int avaNodes = Integer.parseInt(fullLine.split(" ")[1]);
            for (int i = 0; i < avaNodes - 1; i++) {
                theNodes.add(fullLine.split(" ")[0] + "_" + i);
            }
            fullLine = fileMeta.readLine();
            while (fullLine != null) {
                System.out.println(fullLine);
                avaNodes = Integer.parseInt(fullLine.split(" ")[1]);
                for (int i = 0; i < avaNodes; i++) {
                    theNodes.add(fullLine.split(" ")[0] + "_" + i);
                }
                fullLine = fileMeta.readLine();
            }
            fileMeta.close();
        } catch (IOException ex) {
            Logger.getLogger(ParallelSimulationToFile.class.getName()).log(Level.SEVERE, null, ex);
        }

        Object[] argsObj = theNodes.toArray();
        args = new String[argsObj.length];
        for (int i = 0; i < argsObj.length; i++) {
            args[i] = (String) argsObj[i];
        }
        // tp test
//        args = new String[1];
//        args[0]=("compute-5-149.local_1");
        System.out.println(java.util.Arrays.toString(args));

        try {
           // subMainALL(args);
           subMainDEM_TTime(args);
            //subMainLUCIANAHGstudy(args);
            //subMainEvent(args);
//            subMainMaria(args);
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

        java.util.Hashtable<String, Boolean> myNodeNames = new java.util.Hashtable<String, Boolean>();
        for (int j = 0; j < args.length; j++) {
            myNodeNames.put(args[j], false);
        }

        int numNodes = myNodeNames.size();

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};

        String[] AllSimName = {"90DEMUSGS"};

        String[] AllRain = {"3CCreekMarAdvdisc4","3CCreekMarNoAdvdisc4"};

//,"3VolgaNOBIASCORR"
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
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM";
                    xOut = 1596;
                    yOut = 298;
                }
                if (SimName.equals("5DEMLIDAR")) {
                    xOut = 8052;
                    yOut = 497;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/5meters/5meterc1.metaDEM";
                }
                if (SimName.equals("10DEMLIDAR")) {
                    xOut = 4025;
                    yOut = 244;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM";
                }
                if (SimName.equals("20DEMLIDAR")) {
                    xOut = 2013;
                    yOut = 122;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/20meters/20meterc1.metaDEM";
                }
                if (SimName.equals("30DEMLIDAR")) {
                    xOut = 1341;
                    yOut = 82;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30meters/30meterc1.metaDEM";
                }
                if (SimName.equals("60DEMLIDAR")) {
                    xOut = 670;
                    yOut = 41;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/60meters/60meterc1.metaDEM";
                }
                if (SimName.equals("90DEMLIDAR")) {
                    xOut = 447;
                    yOut = 27;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM";
                }
                if (SimName.equals("90DEMUSGS")) {
                    xOut = 2817;
                    yOut = 713;
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                if (SimName.equals("30DEMUSGS")) {
                    xOut = 1541;
                    yOut = 92;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM";
                }

                if (SimName.equals("10DEMUSGS")) {
                    xOut = 4624;
                    yOut = 278;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM";
                }

                if (BasinName.indexOf("Cedar") >= 0) {
                    xOut = 2734;
                    yOut = 1069; //Cedar Rapids
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }


                if (BasinName.indexOf("Iowa") >= 0) {
                    xOut = 2885;
                    yOut = 690;
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }


                if (BasinName.indexOf("Turkey") >= 0) {

                    xOut = 3053;
                    yOut = 2123;
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                if (BasinName.indexOf("Volga") >= 0) {
                    xOut = 3091;
                    yOut = 2004;
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                java.io.File theFile = new java.io.File(DEM);
                //java.io.File theFile=new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
//        java.io.File theFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM");
//         int xOut = 4025; int yOut = 244;//10METERDEMClear Creek - coralville
//        java.io.File theFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM");
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

                //stormFile=new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

                // DEFINE THE INITIAL TIME OF THE SIMULATION
                java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);


                // DEFINE THE FINAL TIME OF THE SIMULATION
                java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);

                if(BasinName.indexOf("Adv")>=0) {
                  if(BasinName.indexOf("May")>=0) {
                    alphaSimulationTime.set(2009, 4, 13, 0, 0, 0);
                    omegaSimulationTime.set(2009, 4, 22, 0, 0, 0);}
                  if(BasinName.indexOf("Mar")>=0) {
                    alphaSimulationTime.set(2009, 2, 6, 0, 0, 0);
                    omegaSimulationTime.set(2009, 2, 15, 0, 0, 0);}
                  if(BasinName.indexOf("Jun")>=0) {
                    alphaSimulationTime.set(2009, 5, 9, 0, 0, 0);
                    omegaSimulationTime.set(2009, 5, 30, 0, 0, 0);}
                  }


                java.io.File outputDirectory;


                //int xOut =  1341;int yOut =  82;//30METERDEMClear Creek - coralville


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

                if (BasinName.indexOf("Clear") > 0 && BasinName.indexOf("2009") > 0) {
                    disc = 3;
                }

                if (BasinName.indexOf("disc4") > 0) {
                    disc = 4;
                }
//        int xOut = 2734;
//        int yOut = 1069; //Cedar Rapids
//        int disc=5;
//        String BasinName="3CedarRapids";
                //1 space and 15 time
                stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC");
                //1 space and 180 time
                if (BasinName.indexOf("1_180") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/180min/Time/Bin/hydroNexrad.metaVHC");
                }
                //6 space and 15 time
                if (BasinName.indexOf("6_15") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/15min/Time/Bin/hydroNexrad.metaVHC");
                }
                //6 space and 180 tim
                if (BasinName.indexOf("6_180") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("15_15") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/15min/Time/Bin/hydroNexrad.metaVHC");
                }

                if (BasinName.indexOf("15_180") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/180min/Time/Bin/hydroNexrad.metaVHC");
                }

                if (BasinName.indexOf("6_180") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("1_60") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/60min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("2_60") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/2/60min/Time/Bin/hydroNexrad.metaVHC");
                }

                if (BasinName.indexOf("6_60") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/60min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("15_60") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/60min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("ori") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("PERS") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/PERSIANN/May29Jun17/PERSIANN_3h.metaVHC");
                }
                if (BasinName.indexOf("TRMM") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/RC1.00/bin/rain17/prec.metaVHC");
                }
                if (BasinName.indexOf("NOBIASCORR") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/DataGenNov_2010/Radar_merged_Vhc/NEXRAD_BC.metaVHC");
                }

                if(BasinName.indexOf("Adv")>0) {
                  if(BasinName.indexOf("May")>0) {

                   stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/May2009_AdvectionVHC/NEXRAD_BC.metaVHC");
                    if(BasinName.indexOf("No")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/May2009_NoAdvectionVHC/NEXRAD_BC.metaVHC");
                    }}
                  if(BasinName.indexOf("Mar")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_AdvectionVHC/NEXRAD_BC.metaVHC");
                    if(BasinName.indexOf("No")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_NoAdvectionVHC/NEXRAD_BC.metaVHC");
                    }}
                   if(BasinName.indexOf("Jun")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/Jun2009_AdvectionVHC/NEXRAD_BC.metaVHC");
                    if(BasinName.indexOf("No")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/Jun2009_NoAdvectionVHC/NEXRAD_BC.metaVHC");
                    }}
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

                    routingParams.put("Basin_sim",bflag); // Cedar river - define Land cover and soil parameter
                    float[] vsAr = {0.0005f};
                    //float[] vsAr = {0.0001f,0.0005f,0.001f,0.005f,0.01f};
                    float[] PH = {0.0f,0.25f,0.5f,0.75f};
                    float[] Phi = {0.0f,0.25f,0.5f};
                    float[] IaAre = {1.0f};

                    float[] IaArc = {0.0f};
                    //float[] IaArc = {0.0f,0.1f,1.0f};
                    float[] l1Ar = {0.25f};

                    float[] rcAr = {0.5f};
                    float[] vhAr = {25.f};
                    float[] Coefvo = {0.5f};
                    for (float l11 : l1Ar) {

                        for (float vo1 : Coefvo) {
                            float cvo = vo1;
                            float l1 = l11;
                            float l2 = -0.82f * l11 + 0.1025f;
                            float vo = cvo * (0.42f - 0.29f * l11 - 3.1f * l2);

                            for (float voo : vsAr) {
                                float vsub = voo;
                                for (float in1 : PH) {
                                    float p1 = in1;
                                    for (float in2 : Phi) {
                                        float p2 = in2;
                                        for (float ia1e : IaAre) {
                                            float iae = ia1e;
                                            for (float ia1c : IaArc) {
                                                float iac = ia1c;

                                                for (float rc1 : rcAr) {
                                                    float rc = rc1;
                                                    for (float vh1 : vhAr) {
                                                        float vh = vh1;
                                                        //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
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
                                                        routingParams.put("HillT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                        routingParams.put("HillVelocityT", 4);  // check NetworkEquationsLuciana.java for definitions
                                                        routingParams.put("ConstSoilStorage", -9.f);  // check NetworkEquationsLuciana.java for definitions

                                                        routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                        //routingParams.put("RunoffCoefficient", rc); // reservoir position:
                                                       
                                                        outputDirectory = new java.io.File("/Users/rmantill/luciana/Parallel/Res_Jan_2011_M5_3/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                                + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                                + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                                + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                                + "/BFExp" + ((Float) routingParams.get("BaseFlowExp")).floatValue()
                                                                + "/BFCoef" + ((Float) routingParams.get("BaseFlowCoef")).floatValue()
                                                                + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                                + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                                + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                                + "/SCS_" + ((Float) routingParams.get("ConstSoilStorage")).floatValue()
                                                                + "/vh_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                                + "/vo_" + ((Float) routingParams.get("v_o")).floatValue()
                                                                + "/l1_" + ((Float) routingParams.get("lambda1")).floatValue()
                                                                + "/l2_" + ((Float) routingParams.get("lambda2")).floatValue());

//                                           
                                                        int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                        outputDirectory.mkdirs();
                                                        new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);


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
                    //float[] vsAr = {0.0001f,0.0005f,0.001f,0.005f,0.01f};
                    float[] PH2 = {0.0f,0.5f};
                    float[] Phi2 = {0.0f,0.5f};
                    float[] IaAre2 = {1.0f};

                    float[] IaArc2 = {0.0f};
                    //float[] IaArc = {0.0f,0.1f,1.0f};

                    float[] voAr2 = {0.6f,0.8f,1.0f};
                    float[] rcAr2 = {0.5f};
                    float[] vhAr2 = {10.f,50.f};

                    for (float vo1 : voAr2) {
                        float vo = vo1;
                        //                   float l1 = l11;
                        //                   float l2 = -0.82f * l11 + 0.1025f;
                        //                   float vo = 0.42f - 0.29f * l11 - 3.1f * l2;

                        for (float voo : vsAr2) {
                            float vsub = voo;
                            for (float in1 : PH2) {
                                float p1 = in1;
                                for (float in2 : Phi2) {
                                    float p2 = in2;
                                    for (float ia1e : IaAre2) {
                                        float iae = ia1e;
                                        for (float ia1c : IaArc2) {
                                            float iac = ia1c;

                                            for (float rc1 : rcAr2) {
                                                float rc = rc1;
                                                for (float vh1 : vhAr2) {
                                                    float vh = vh1;
                                                    //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                    routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                    routingParams.put("v_o", vo);
                                                    //                         routingParams.put("lambda1", l11);
                                                    //                         routingParams.put("lambda2", l2);
                                                    routingParams.put("PorcHSaturated", p1);
                                                    routingParams.put("vssub", vsub);
                                                    routingParams.put("PorcPhiUnsat", p2);
                                                    routingParams.put("lambdaSCSMethod", iae);
                                                    routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                    routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)
                                                    routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                                                    routingParams.put("HillT", 5);  // check NetworkEquationsLuciana.java for definitions
                                                    routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                    routingParams.put("ConstSoilStorage", -9.f);  // check NetworkEquationsLuciana.java for definitions
                                                    routingParams.put("vrunoff", vh); // define a constant number or -9.9 for vel=f(land Cover)
                                                    outputDirectory = new java.io.File("/Users/rmantill/luciana/Parallel/Res_Jan_2011_M5_3/" + BasinName + "/" + SimName + "/" + bflag + "/"
                                                            + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                            + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                            + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                            + "/BFExp" + ((Float) routingParams.get("BaseFlowExp")).floatValue()
                                                            + "/BFCoef" + ((Float) routingParams.get("BaseFlowCoef")).floatValue()
                                                            + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                            + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                            + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                            + "/vhill_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                            + "/vcte_" + ((Float) routingParams.get("v_o")).floatValue());

//                                            + "RR_" + ((Float) routingParams.get("RunoffCoefficient")).floatValue() +"/"
                                                    // + "VHILL_" + ((Float) routingParams.get("vrunoff")).floatValue()

                                                    int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                    outputDirectory.mkdirs();
                                                    new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

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

    public static void subMainLUCIANAHGstudy(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String, Boolean> myNodeNames = new java.util.Hashtable<String, Boolean>();
        for (int j = 0; j < args.length; j++) {
            myNodeNames.put(args[j], false);
        }

        int numNodes = myNodeNames.size();

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};

        String[] AllSimName = {"90DEMUSGS"};

        String[] AllRain = {"3CedarRiver"};

        // DEFINE THE DEM

        java.io.File theFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
        //java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
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

        int disc = 5;


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
        routingParams.put("RunoffCoefficient", 1.0f); // reservoir position:
        routingParams.put("ConstSoilStorage", 130.f);  // check NetworkEquationsLuciana.java for definitions
        int rrt = ((Integer) routingParams.get("RoutingT")).intValue();



        routingParams.put("Vconst", 1.0f); // CHANGE IN THE NETWORKEQUATION CLA

        routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
        routingParams.put("HillT", 0);  // check NetworkEquationsLuciana.java for definitions
        routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions

        routingParams.put("vrunoff", 50.0f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("RunoffCoefficient", 0.5f); // reservoir position:


        // DEFINE THE INITIAL TIME OF THE SIMULATION
        java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
        alphaSimulationTime.set(2010, 0, 1, 0, 0, 0);


        // DEFINE THE FINAL TIME OF THE SIMULATION
        java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
        omegaSimulationTime.set(2010, 0, 6, 0, 0, 0);


        // DEFINE THE STORM FILE - IF PRECIPITATION IS NOT CONSTANT
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May29toJune26/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

        java.util.Calendar zeroSimulationTime = java.util.Calendar.getInstance();
        // DEFINE THE INITIAL TIME OF THE SIMULATION

        java.io.File outputDirectory;

//        for (float vd = 1.00f; vd <= 14.0f; vd+=2.0f) {
//
//        float rt=25/(1*24);

//int xOut=2817;
//        int yOut=713; //Clear Creek - coralville
        int xOut = 2734;
        int yOut = 1069;


        int[] Volume = {60};
        int[] Duration = {15, 120};

        for (int vol : Volume) {
            for (int dur : Duration) {

                float[] lam12 = {0.15f, 0.30f, 0.45f};

                float[] lam22 = new float[lam12.length];
                float[] v_o2 = new float[lam12.length];
                //lam22 = new float[lam12.length];


                float[] vostAr2 = {0.0f};
                for (float l1 : lam12) {
                    float L1 = l1;
                    float L2 = -0.82f * l1 + 0.1025f;
                    float VO = 0.42f - 0.29f * L1 - 3.1f * L2;
                    for (float vst : vostAr2) {
                        float VST = vst;


                        // stormFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/AggregatedEventHydroNexrad/2/60min/Time/Bin/H00070802_R1504_G_.metaVHC");
                        stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/thesis2/" + vol + "mm/" + dur + "min/vhc/" + vol + "mm" + dur + "min.metaVHC");
                        routingParams.put("Vostd", VST);
                        routingParams.put("lambda1", L1);
                        routingParams.put("lambda2", L2);
                        routingParams.put("v_o", VO);
                        routingParams.put("dynaIndex", 3);
                        rrt = ((Integer) routingParams.get("RoutingT")).intValue();

                        outputDirectory = new java.io.File("/Users/rmantill/luciana/Parallel/veloc_study_V3/3CedarRiverRoutT" + rrt + "/" + vol + "mm_" + dur + "min" + "/" + VO + "/" + L1 + "/" + L2 + "/" + VST + "/");
                        outputDirectory.mkdirs();


                        new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);


                    }
                }
            }
        }
    }


 public static void subMainDEM_TTime(String args[]) throws java.io.IOException, VisADException {

        java.util.Hashtable<String, Boolean> myNodeNames = new java.util.Hashtable<String, Boolean>();
        for (int j = 0; j < args.length; j++) {
            myNodeNames.put(args[j], false);
        }

        int numNodes = myNodeNames.size();

//          String[] AllSimName = {"30DEMUSGS","10DEMLIDAR","ASTER",};

        String[] AllSimName = {"90DEMLIDAR"};

        String[] AllRain = {"3ClearCreekTTime"};

//,"3VolgaNOBIASCORR"
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
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM";
                    xOut = 1596;
                    yOut = 298;
                }
                if (SimName.equals("5DEMLIDAR")) {
                    xOut = 8052;
                    yOut = 497;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/5meters/5meterc1.metaDEM";
                }
                if (SimName.equals("10DEMLIDAR")) {
                    xOut = 4025;
                    yOut = 244;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM";
                }
                if (SimName.equals("20DEMLIDAR")) {
                    xOut = 2013;
                    yOut = 122;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/20meters/20meterc1.metaDEM";
                }
                if (SimName.equals("30DEMLIDAR")) {
                    xOut = 1341;
                    yOut = 82;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30meters/30meterc1.metaDEM";
                }
                if (SimName.equals("60DEMLIDAR")) {
                    xOut = 670;
                    yOut = 41;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/60meters/60meterc1.metaDEM";
                }
                if (SimName.equals("90DEMLIDAR")) {
                    xOut = 447;
                    yOut = 27;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM";
                }
                if (SimName.equals("90DEMUSGS")) {
                    xOut = 2817;
                    yOut = 713;
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                if (SimName.equals("30DEMUSGS")) {
                    xOut = 1541;
                    yOut = 92;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM";
                }

                if (SimName.equals("10DEMUSGS")) {
                    xOut = 4624;
                    yOut = 278;
                    DEM = "/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM";
                }

                if (BasinName.indexOf("Cedar") >= 0) {
                    xOut = 2734;
                    yOut = 1069; //Cedar Rapids
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }


                if (BasinName.indexOf("Iowa") >= 0) {
                    xOut = 2885;
                    yOut = 690;
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }


                if (BasinName.indexOf("Turkey") >= 0) {

                    xOut = 3053;
                    yOut = 2123;
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                if (BasinName.indexOf("Volga") >= 0) {
                    xOut = 3091;
                    yOut = 2004;
                    DEM = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                java.io.File theFile = new java.io.File(DEM);
                //java.io.File theFile=new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
//        java.io.File theFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM");
//         int xOut = 4025; int yOut = 244;//10METERDEMClear Creek - coralville
//        java.io.File theFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM");
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

                //stormFile=new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");

                // DEFINE THE INITIAL TIME OF THE SIMULATION
                java.util.Calendar alphaSimulationTime = java.util.Calendar.getInstance();
                alphaSimulationTime.set(2008, 4, 29, 0, 0, 0);


                // DEFINE THE FINAL TIME OF THE SIMULATION
                java.util.Calendar omegaSimulationTime = java.util.Calendar.getInstance();
                omegaSimulationTime.set(2008, 6, 2, 0, 0, 0);

            
                if(BasinName.indexOf("TTime")>=0) {
                    alphaSimulationTime.set(2010, 0, 1, 0, 0, 0);
                    omegaSimulationTime.set(2010, 0, 3, 0, 0, 0);}



                java.io.File outputDirectory;


                //int xOut =  1341;int yOut =  82;//30METERDEMClear Creek - coralville


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

                if (BasinName.indexOf("Clear") > 0 && BasinName.indexOf("2009") > 0) {
                    disc = 3;
                }

                if (BasinName.indexOf("disc4") > 0) {
                    disc = 4;
                }
//        int xOut = 2734;
//        int yOut = 1069; //Cedar Rapids
//        int disc=5;
//        String BasinName="3CedarRapids";
                //1 space and 15 time
                stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/15min/Time/Bin/hydroNexrad.metaVHC");
                //1 space and 180 time
                if (BasinName.indexOf("1_180") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/180min/Time/Bin/hydroNexrad.metaVHC");
                }
                //6 space and 15 time
                if (BasinName.indexOf("6_15") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/15min/Time/Bin/hydroNexrad.metaVHC");
                }
                //6 space and 180 tim
                if (BasinName.indexOf("6_180") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("15_15") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/15min/Time/Bin/hydroNexrad.metaVHC");
                }

                if (BasinName.indexOf("15_180") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/180min/Time/Bin/hydroNexrad.metaVHC");
                }

                if (BasinName.indexOf("6_180") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/180min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("1_60") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/1/60min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("2_60") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/2/60min/Time/Bin/hydroNexrad.metaVHC");
                }

                if (BasinName.indexOf("6_60") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/6/60min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("15_60") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/Agreeg_Hydronexrad_v3/WithoutGeomBias/15/60min/Time/Bin/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("ori") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/May29toJune26/hydroNexrad.metaVHC");
                }
                if (BasinName.indexOf("PERS") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/PERSIANN/May29Jun17/PERSIANN_3h.metaVHC");
                }
                if (BasinName.indexOf("TRMM") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/SatelliteData/RC1.00/bin/rain17/prec.metaVHC");
                }
                if (BasinName.indexOf("NOBIASCORR") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/DataGenNov_2010/Radar_merged_Vhc/NEXRAD_BC.metaVHC");
                }

                if (BasinName.indexOf("TTime") > 0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/constant/thesis2/0mm/15min/vhc/0mm15min.metaVHC");
                }

                if(BasinName.indexOf("2009")>0) {
                  if(BasinName.indexOf("May")>0) {

                   stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/May2009_AdvectionVHC/NEXRAD_BC.metaVHC");
                    if(BasinName.indexOf("No")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/May2009_NoAdvectionVHC/NEXRAD_BC.metaVHC");
                    }}
                  if(BasinName.indexOf("Mar")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_AdvectionVHC/NEXRAD_BC.metaVHC");
                    if(BasinName.indexOf("No")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_NoAdvectionVHC/NEXRAD_BC.metaVHC");
                    }}
                   if(BasinName.indexOf("Jun")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/Jun2009_AdvectionVHC/NEXRAD_BC.metaVHC");
                    if(BasinName.indexOf("No")>0) {
                    stormFile = new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/Jun2009_NoAdvectionVHC/NEXRAD_BC.metaVHC");
                    }}
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
                routingParams.put("PondedWater", 25.4f); // surface initial condition [mm]:

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

                    routingParams.put("Basin_sim",bflag); // Cedar river - define Land cover and soil parameter

                    float[] vsAr2 = {0.001f};
                    //float[] vsAr = {0.0001f,0.0005f,0.001f,0.005f,0.01f};
                    float[] PH2 = {0.0f};
                    float[] Phi2 = {0.0f};
                    float[] IaAre2 = {1.0f};

                    float[] IaArc2 = {0.0f};
                    //float[] IaArc = {0.0f,0.1f,1.0f};

                    float[] voAr2 = {2.0f,0.5f}; // in m/s
                    float[] rcAr2 = {0.5f};
                    float[] vhAr2 = {0.01f*3600.f,0.02f*3600f,0.05f*3600f,1.0f*3600f,}; // in m/h

                    for (float vo1 : voAr2) {
                        float vo = vo1;
                        //                   float l1 = l11;
                        //                   float l2 = -0.82f * l11 + 0.1025f;
                        //                   float vo = 0.42f - 0.29f * l11 - 3.1f * l2;

                        for (float voo : vsAr2) {
                            float vsub = voo;
                            for (float in1 : PH2) {
                                float p1 = in1;
                                for (float in2 : Phi2) {
                                    float p2 = in2;
                                    for (float ia1e : IaAre2) {
                                        float iae = ia1e;
                                        for (float ia1c : IaArc2) {
                                            float iac = ia1c;

//                                            for (float rc1 : rcAr2) {
//                                                float rc = rc1;
                                                for (float vh1 : vhAr2) {
                                                    float vh = vh1;
                                                    //C999999 RT=5 (cte vel). HillT=3 , HillVel=1 (hill veloc cte), SCS method - spattially constant
                                                    routingParams.put("Vconst", vo); // CHANGE IN THE NETWORKEQUATION CLA
                                                    routingParams.put("v_o", vo);
                                                    routingParams.put("vrunoff", vh);
                                                    //                         routingParams.put("lambda1", l11);
                                                    //                         routingParams.put("lambda2", l2);
                                                    routingParams.put("PorcHSaturated", p1);
                                                    routingParams.put("vssub", vsub);
                                                    routingParams.put("PorcPhiUnsat", p2);
                                                    routingParams.put("lambdaSCSMethod", iae);
                                                    routingParams.put("BaseFlowCoef", iac); // define a constant number or -9.9 for vel=f(land Cover)
                                                    routingParams.put("BaseFlowExp", iae); // define a constant number or -9.9 for vel=f(land Cover)
                                                    routingParams.put("RoutingT", 2); // check NetworkEquationsLuciana.java for definitions
                                                    routingParams.put("HillT", 1);  // check NetworkEquationsLuciana.java for definitions
                                                    routingParams.put("HillVelocityT", 0);  // check NetworkEquationsLuciana.java for definitions
                                                    routingParams.put("ConstSoilStorage", -9.f);  // check NetworkEquationsLuciana.java for definitions

                                                    outputDirectory = new java.io.File("/Users/rmantill/luciana/Parallel/Res_Jan_2011_M5_3/" + BasinName + "/" + SimName + "/" + BF + "/"
                                                            + "RoutT_" + ((Integer) routingParams.get("RoutingT")).intValue() + "/"
                                                            + "HillT_" + ((Integer) routingParams.get("HillT")).intValue() + "/"
                                                            + "HillVelT_" + ((Integer) routingParams.get("HillVelocityT")).intValue()
                                                            + "/BFExp" + ((Float) routingParams.get("BaseFlowExp")).floatValue()
                                                            + "/BFCoef" + ((Float) routingParams.get("BaseFlowCoef")).floatValue()
                                                            + "/VS" + ((Float) routingParams.get("vssub")).floatValue()
                                                            + "/UnsO" + ((Float) routingParams.get("PorcPhiUnsat")).floatValue()
                                                            + "/PH" + ((Float) routingParams.get("PorcHSaturated")).floatValue()
                                                            + "/vhill_" + ((Float) routingParams.get("vrunoff")).floatValue()
                                                            + "/vcte_" + ((Float) routingParams.get("v_o")).floatValue());

//                                            + "RR_" + ((Float) routingParams.get("RunoffCoefficient")).floatValue() +"/"
                                                    // + "VHILL_" + ((Float) routingParams.get("vrunoff")).floatValue()

                                                    int rrt = ((Integer) routingParams.get("RoutingT")).intValue();
                                                    outputDirectory.mkdirs();
                                                    new ParallelSimulationToFile(xOut, yOut, matDirs, magnitudes, horOrders, metaModif, stormFile, 0.0f, rrt, routingParams, outputDirectory, alphaSimulationTime, omegaSimulationTime, myNodeNames, numNodes, disc);

                                                

                                            
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }}
        }
}

    
 
