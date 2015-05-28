/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.swmmCoupling.io.metaPolygonUrban;

import adlzanchetta.cuencasTools.cuencasCsvFileReader;
import hydroScalingAPI.examples.rainRunoffSimulations.SimulationToAsciiFile;
import hydroScalingAPI.io.DataRaster;
import hydroScalingAPI.io.MetaNetwork;
import hydroScalingAPI.io.MetaRaster;
import hydroScalingAPI.modules.swmmCoupling.io.SubBasinsLogManager;
import hydroScalingAPI.modules.swmmCoupling.objects.MetaRasterTool;
import hydroScalingAPI.modules.swmmCoupling.util.TopologicalSolver;
import hydroScalingAPI.modules.swmmCoupling.util.geomorphology.objects.SubBasin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import visad.VisADException;

/**
 * Class used for testing functions only. Can be ignored for general proposes
 * @author A. D. L. Zanchetta
 */
public class MainTesting {
    
    /**
     * Mains groups
     * - main - read subBasinLog, build connections and shapes, identify order, run AND write .inp file
     * - main03- evaluate regEx
     * - main02- from subBasinLog to set of hydrological results
     * - main01- from imported poly, to subBasinLog and defined sequence
     */
    
    /**
     * 
     * @param args 
     */
    public static void mainX(String[] args) {
        String polyPath, subBasinsPath, outputDirPath, swmmInpPath;
        File polyFile, subBasinsFile, outputDirFile, swmmInpFile;
        File metaRasterFile, binMagFile, binDirFile;
        String metaDemPath, binMagPath, binDirPath;
        TopologicalSolver topoSolv;
        
        MetaRaster magnRaster, dirRaster, mRaster;
        MetaPolygonUrban polygonUrban;
        SubBasin[] allSubBasin;
        byte[][] dirMatrix;
                
        // 0.1
        polyPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Polygons\\CaldasSimplify3_form.poly";
        metaDemPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.metaDEM";
        binDirPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.dir";
        binMagPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.magn";
        subBasinsPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.subLog";
        
        // 0.2
        outputDirPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\7_output";
        swmmInpPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\7_output\\caldas07.inp";
        
        // 0 - define file location
        // 0.1 - input files
        // 0.2 - output files
        // 1 - loading basicals
        // 1.1 - initiate files
        // 1.2 - load basic files
        // 2 - run basic steps
        
        // 1.1
        polyFile = new File(polyPath);
        subBasinsFile = new File(subBasinsPath);
        outputDirFile = new File(outputDirPath);
        swmmInpFile = new File(swmmInpPath);
        metaRasterFile = new File(metaDemPath);
        binMagFile = new File(binMagPath);
        binDirFile = new File(binDirPath);
        
        // 1.2
        try{
            mRaster = new MetaRaster(metaRasterFile);
        
            magnRaster = new MetaRaster(metaRasterFile);
            magnRaster.setLocationBinaryFile(binMagFile);
            magnRaster.setFormat("Integer");
        } catch (IOException exp) {
            System.err.println("IOException: " + exp);
            return;
        }
        dirRaster = MetaPolygonUrban.getMetaRasterDirections(binDirFile,
                                                             metaRasterFile);
        dirMatrix = MetaPolygonUrban.getDirectionMatrix(dirRaster);
        
        // 2
        polygonUrban = MainTesting.loadUrbanPolygon(polyFile);
        allSubBasin = MainTesting.loadSubBasinsFromFile(subBasinsFile, 
                                                        mRaster, 
                                                        dirMatrix);
        
        MainTesting.reshapeSubBasins(allSubBasin, polygonUrban, dirMatrix, 
                                     mRaster, mRaster);
        topoSolv = MainTesting.connectObjectsC(allSubBasin, 
                                               polygonUrban, 
                                               mRaster);
        MainTesting.runCuencasForSubBasins(allSubBasin, polygonUrban,
                                           topoSolv, outputDirFile,
                                           mRaster, magnRaster,
                                           dirMatrix);
        
        MainTesting.readCuencasCsvFiles(outputDirFile, polygonUrban, mRaster);
        
        polygonUrban.exportToSwmmFile(swmmInpFile, true);
    }
    
    public static void main03(String[] args) {
        String fileNameEvalOk, fileNameEvalNo;
        int[] curResult;
        
        fileNameEvalOk = "dem_691_532-UniformEvent_INT_1.5_DUR_60.0-IR_0.5-Routing_GK_params_0.3_-0.1_0.6.csv";
        fileNameEvalNo = "dem-SN.wfs.csv";
        
        curResult = MetaPolygonUrban.getOutputCuencasFileBasin(fileNameEvalOk);
        if (curResult != null){
            System.out.println("It is x: " + curResult[0] + " and y: " + curResult[1]);
        }
        curResult = MetaPolygonUrban.getOutputCuencasFileBasin(fileNameEvalNo);
        if (curResult != null){
            System.out.println("It is x: " + curResult[0] + " and y: " + curResult[1]);
        }
    }
    
    
    public static void main02(String[] args) {
        String subBasinsLogPath, metarasterPath;
        String outputDirPath, directionsPath, magnetudesPath;
        File subBasinsLogFile, metaRasterFile, magnetudesFile;
        File outputDir, directionsFile;
        SubBasinsLogManager subBasinsManager;
        MetaRaster mRaster, dirRaster, magnRaster;
        HashMap[] allSubBasinsHash;
        File[] allGeneratedFiles;
        SubBasin[] allSubBasin;
        SubBasin curSubBasin;
        HashMap currentHash;
        Hashtable modParams;
        byte[][] dirMatrix;
        int[][] magnitudes;
        int count;
        
        // 0 - load basic variables
        // 0.1 - define file paths
        // 0.2 - open files
        // 0.3 - open objects
        // 1 - get SubBasins from SubBasins log
        // 2 - for each subbasin
        // 2.1 - run hydrological model
        // 3 - read all csv files in outout directory
        
        // 0.1
        subBasinsLogPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.subLog";
        metarasterPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.metaDEM";
        directionsPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.dir";
        magnetudesPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.magn";
        outputDirPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\4_output";
        
        // 0.2
        metaRasterFile = new File(metarasterPath);
        subBasinsLogFile = new File(subBasinsLogPath);
        directionsFile = new File(directionsPath);
        magnetudesFile = new File(magnetudesPath);
        outputDir = new File(outputDirPath);
        
        // 0.3
        try{
            mRaster = new MetaRaster(metaRasterFile);
            mRaster.setLocationBinaryFile(outputDir);
            
            magnRaster = new MetaRaster(metaRasterFile);
            magnRaster.setLocationBinaryFile(magnetudesFile);
            magnRaster.setFormat("Integer");    
            magnitudes = new DataRaster(magnRaster).getInt();
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return;
        }
        dirRaster = MetaPolygonUrban.getMetaRasterDirections(directionsFile,
                                                             metaRasterFile);
        dirMatrix = MetaPolygonUrban.getDirectionMatrix(dirRaster);
        
        // 1
        try{
            subBasinsManager = new SubBasinsLogManager(subBasinsLogFile);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return;
        }
        allSubBasinsHash = subBasinsManager.getAllBasins();
        
        // 2
        allSubBasin = new SubBasin[allSubBasinsHash.length];
        for(count = 0; count < allSubBasinsHash.length; count++){
            currentHash = allSubBasinsHash[count];
            curSubBasin = new SubBasin(currentHash, dirMatrix, mRaster);
            allSubBasin[count] = curSubBasin;
            
            // 2.1
            SimulationToAsciiFile simulator;
            modParams = MetaPolygonUrban.buildSimulationParams();
            /*
            public SimulationToAsciiFile(int x, int y, 
                                         byte[][] direcc, 
                                         int[][] magnitudes, 
                                         hydroScalingAPI.io.MetaRaster md, 
                                         float rainIntensity, 
                                         float rainDuration, 
                                         float infiltRate, 
                                         int routingType, 
                                         File outputDirectory,
                                         Hashtable routingParams);
                                         */
            
            try{
                simulator = new SimulationToAsciiFile(curSubBasin.getX(),
                                                      curSubBasin.getY(),
                                                      dirMatrix,
                                                      magnitudes,
                                                      magnRaster,
                                                      1.5f,
                                                      60.0f,
                                                      0.5f,
                                                      5,
                                                      outputDir,
                                                      modParams);
                simulator.executeSimulation();
            } catch (IOException exp) {
                System.err.println("IOException: " + exp.getMessage());
                return;
            } catch (VisADException exp) {
                System.err.println("VisADException: " + exp.getMessage());
                return;
            }
        }
        
        // 3
        allGeneratedFiles = outputDir.listFiles();
        double[][] resultMatrix;
        int[] xyBasin;
        for(count = 0; count < allSubBasinsHash.length; count++){
            xyBasin = MetaPolygonUrban.getOutputCuencasFileBasin(allGeneratedFiles[count]);
            if (xyBasin != null){
                try{
                    resultMatrix = cuencasCsvFileReader.readCSVdischargeFile(allGeneratedFiles[count]);
                } catch (FileNotFoundException exp) {
                    System.err.println("Exception: " + exp.getMessage());
                } catch (IOException exp) {
                    System.err.println("Exception: " + exp.getMessage());
                }
            }
        }
        
        System.out.println("GOT LUCKY!");
    }
    
    /**
     * This main is aims to read an existing polygon, identify its crossing points, build subBasin log file and define a execution sequence
     * @param args 
     */
    public static void main(String[] args) {
        File metaRasterFile, rasterBinFile, polygonFile, directionsFile;
        String rasterBinFilePath, directionsFilePath, subBasinsLogPath;
        String polygonFilePath, metarasterFilePath;
        SubBasinsLogManager subBasinsManager;
        Iterator<SubBasin> allSubBasinIt;
        ArrayList<SubBasin> allSubBasin;
        MetaRaster mRaster, dirRaster;
        String inletXstr, inletYstr;
        HashMap[] allSubBasinsHash;
        SubBasin masterBiggerBasin;
        TopologicalSolver topoSolv;
        MetaPolygonUrban newPol;
        MetaNetwork metaNetwork;
        Integer inletX, inletY;
        SubBasin[] subBasinVec;
        File subBasinsLogFile;
        SubBasin curSubBasin;
        HashMap currentHash;
        byte[][] dirMatrix;
        int count, countIn;
        int curInletID;
        
        // variables to define
        final int xBiggerBasin = 671;
        final int yBiggerBasin = 629;
        
        // mains parameters
        // WINDOWS
        //polygonFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Polygons\\Bello_form.poly";
        polygonFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Polygons\\CaldasSimplify3_form.poly";
        metarasterFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.metaDEM";
        rasterBinFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.dem";
        directionsFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.dir";
        subBasinsLogPath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\bases_datos\\db_cuencas_valAburra\\Rasters\\Topography\\dem.subLog";
        
        // LINUX
        //polygonFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Polygons/Bello_form.poly";
        //metarasterFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Rasters/Topography/dem.metaDEM";
        //rasterBinFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Rasters/Topography/dem.dem";
        //directionsFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Rasters/Topography/dem.dir";
        //inpSwmmFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto_swmmAcople/inputs_swmm/bello01_test.inp";
        
        // open first files
        polygonFile = new File(polygonFilePath);
        metaRasterFile = new File(metarasterFilePath);
        rasterBinFile = new File(rasterBinFilePath);
        subBasinsLogFile = new File(subBasinsLogPath);
        
        // load polygon, meta raster, basins log manager
        try{
            newPol = new MetaPolygonUrban(polygonFile);
            mRaster = new MetaRaster (metaRasterFile);
            mRaster.setLocationBinaryFile(rasterBinFile);
            metaNetwork = new MetaNetwork(mRaster);
            subBasinsManager = new SubBasinsLogManager(subBasinsLogFile);
            
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return;
        }
        
        // open directions raster
        directionsFile = new File(directionsFilePath);
        dirRaster = MetaPolygonUrban.getMetaRasterDirections(directionsFile,
                                                             metaRasterFile);
        dirMatrix = MetaPolygonUrban.getDirectionMatrix(dirRaster);
        
        // identify border points from polygon
        /*
        if (!newPol.identifyBorderPointsByLinks(mRaster, dirMatrix)){
            System.out.println("FAIL identifying border points.");
        }
        */
        
        // identify bortder points on polygon based only in the bigger basin
        masterBiggerBasin = new SubBasin(xBiggerBasin, yBiggerBasin,
                                         dirMatrix, mRaster);
        
        //if(!newPol.identifyBorderPointsByMask(mRaster, dirMatrix)){
        //    System.out.println("FAIL identifying border points by mask.");
        //}
        if (!newPol.identifyBorderPointsMixed(masterBiggerBasin, dirRaster, dirMatrix)){
            System.out.println("FAIL identifying border points.");
        }
        
        // create other subbasins with outlet in polygonInlets
        subBasinVec = newPol.getAllTributarySubBasins(mRaster, dirMatrix, 
                                                      metaNetwork);
        
        // for each tributary subbasin found, add to subbasins manager file
        for(curInletID = 0; curInletID < subBasinVec.length; curInletID++){
            SubBasin tmpSubBas;
            tmpSubBas = subBasinVec[curInletID];
            if (subBasinsManager.addOrUpdateSubBasin(
                    subBasinVec[curInletID].getHashedData(mRaster))){
                System.out.println("_SUCCESS add ["+subBasinVec[curInletID].getX()+", "+subBasinVec[curInletID].getY()+"] inlet as subbasin ("+curInletID+") .");
            } else {
                System.out.println("__FAILED add ["+subBasinVec[curInletID].getX()+", "+subBasinVec[curInletID].getY()+"] inlet as subbasin ("+curInletID+").");
            }
        }
        
        // load sub basins from subbasin file and open topo solver
        allSubBasinsHash = subBasinsManager.getAllBasins();
        topoSolv = new TopologicalSolver();
        allSubBasin = new ArrayList<SubBasin>();
        
        // for each subbasin
        // 1 - open blank object
        // 2 - load inlets from file
        // 3 - redefine basin shape
        // 4 - remove polygon from current basin
        // 5 - write basin in SubBasinLogFile
        // 6 - add to topological solver
        for(count = 0; count < allSubBasinsHash.length; count++){
            // 1
            currentHash = allSubBasinsHash[count];
            curSubBasin = new SubBasin((Integer)currentHash.get("x"), 
                                       (Integer)currentHash.get("y"), 
                                       dirMatrix, mRaster);
            
            // 2
            countIn = 1;
            do{
                // prepare labels
                inletXstr = "xIn_" + countIn;
                inletYstr = "yIn_" + countIn;
                
                // qet integer values
                inletX = (Integer)currentHash.get(inletXstr);
                inletY = (Integer)currentHash.get(inletYstr);
                
                // 'zuera' never stops but have limit
                if((inletX == null) || (inletY == null)) break;
                
                // add inlet
                curInletID = MetaRasterTool.getIdFromXandY(mRaster, inletX, inletY);
                curSubBasin.addInletID(curInletID);
                
                countIn++;
            } while(true);
            
            // 3
            if (!curSubBasin.reshapeBasin(dirMatrix, mRaster)){
                System.out.println("FAIL reshaping");
            } else {
                System.out.println("SUCCESS reshaping");
            }
            
            // 4
            if (!curSubBasin.extract(newPol, dirMatrix, dirRaster)){
                System.out.println("FAIL extracting");
            } else {
                System.out.println("SUCCESS extracting");
            }
            
            // 5
            if (!subBasinsManager.addOrUpdateSubBasin(curSubBasin.getHashedData(mRaster))){
                System.out.println("FAIL adding or updating");
            } else {
                System.out.println("SUCCESS adding or updating");
            }
            
            // 6
            topoSolv.addSubBasin(curSubBasin);
            
            // 7
            allSubBasin.add(curSubBasin);
        }
        
        // 1 - add urban polygon
        // 2 - establishLinkage between elements
        // 3 - see results
        
        // 1
        topoSolv.addPolygonUrban(newPol);
        
        // 2
        topoSolv.establishLinkage(mRaster);
        
        // 3
        int[] theExecSeq = topoSolv.getExecutionSequence();
        
        if (theExecSeq == null) {
            // lead with failured situation
            
            ArrayList<Comparable> allIso = topoSolv.getIsolatedes();
            Comparable curComp;
            if(allIso == null){
                System.err.println("Impossible to determine isolated nodes.");
            } else if (allIso.isEmpty()) {
                 System.err.println("Not found any isolated nodes.");
            } else {
                Iterator<Comparable> isolatedNodesIt;
                isolatedNodesIt = allIso.iterator();
                System.err.print("Isolateds ("+ allIso.size() +"): ");
                while(isolatedNodesIt.hasNext()){
                    curComp = isolatedNodesIt.next();
                    System.err.print("{" + MetaRasterTool.getXfromID(mRaster, (Integer)curComp) + ", " + MetaRasterTool.getYfromID(mRaster, (Integer)curComp) + "}");
                    if (isolatedNodesIt.hasNext()) System.err.print("; ");
                }
                System.err.println("");
            }
        } else {
            // lead with success situation - show order
            Object curObj;
            
            // show each ordered element, and for each search if it is an 
            //   subbasin or a urbanPolygon
            System.out.println("Executing sequence:");
            for(count = 0; count < theExecSeq.length; count++){
                boolean objFound;
                
                objFound = false;
                System.out.print(count + " - " + theExecSeq[count]);
                
                if (theExecSeq[count] == newPol.getId()) {
                    System.out.println(": is a MetaPolyUrb");
                    continue;
                }
                
                allSubBasinIt = allSubBasin.iterator();
                while(allSubBasinIt.hasNext()){
                    curSubBasin = allSubBasinIt.next();
                    if (curSubBasin.getOutletID() == theExecSeq[count]){
                        System.out.println(": is a SubBasin (" + "x:" + curSubBasin.getX() + ", y:" + curSubBasin.getY() + ")");
                        objFound = true;
                        break;
                    }
                }
                
                if (!objFound){
                    System.out.println(": it is an UFO.");
                }
            }
        }
        
        // call topological solver - OFF
    }
    
    /**
     * 
     * @param args 
     */
    public static void main_old02(String[] args) {
        File metaRasterFile, rasterBinFile, polygonFile, directionsFile;
        String rasterBinFilePath, directionsFilePath, subBasinsLogPath;
        String polygonFilePath, metarasterFilePath;
        SubBasinsLogManager subBasinsManager;
        MetaRaster mRaster, dirRaster;
        String inletXstr, inletYstr;
        HashMap[] allSubBasinsHash;
        MetaPolygonUrban newPol;
        MetaNetwork metaNetwork;
        Integer inletX, inletY;
        SubBasin[] subBasinVec;
        File subBasinsLogFile;
        SubBasin curSubBasin;
        HashMap currentHash;
        byte[][] dirMatrix;
        int count, countIn;
        int curInletID;
        
        // mains parameters
        // WINDOWS
        polygonFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Polygons\\Bello_form.poly";
        metarasterFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Rasters\\Topography\\dem.metaDEM";
        rasterBinFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Rasters\\Topography\\dem.dem";
        directionsFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Rasters\\Topography\\dem.dir";
        subBasinsLogPath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Rasters\\Topography\\dem.subLog";
        
        // LINUX
        //polygonFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Polygons/Bello_form.poly";
        //metarasterFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Rasters/Topography/dem.metaDEM";
        //rasterBinFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Rasters/Topography/dem.dem";
        //directionsFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Rasters/Topography/dem.dir";
        //inpSwmmFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto_swmmAcople/inputs_swmm/bello01_test.inp";
        
        // open first files
        polygonFile = new File(polygonFilePath);
        metaRasterFile = new File(metarasterFilePath);
        rasterBinFile = new File(rasterBinFilePath);
        subBasinsLogFile = new File(subBasinsLogPath);
        
        // load polygon, meta raster, basins log manager
        try{
            newPol = new MetaPolygonUrban(polygonFile);
            mRaster = new MetaRaster (metaRasterFile);
            mRaster.setLocationBinaryFile(rasterBinFile);
            subBasinsManager = new SubBasinsLogManager(subBasinsLogFile);
            metaNetwork = new MetaNetwork(mRaster);
            
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return;
        }
        
        // open directions raster
        directionsFile = new File(directionsFilePath);
        dirRaster = MetaPolygonUrban.getMetaRasterDirections(directionsFile,
                                                             metaRasterFile);
        dirMatrix = MetaPolygonUrban.getDirectionMatrix(dirRaster);
        
        // identify border points from polygon
        /*
        if (!newPol.identifyBorderPointsByLinks(mRaster, dirMatrix)){
            System.out.println("FAIL identifying border points.");
        }
        */
        
        // load sub basins
        allSubBasinsHash = subBasinsManager.getAllBasins();
        
        // if dont have any subBasin, identify points by links
        if(allSubBasinsHash.length == 0){
            newPol.identifyBorderPointsByLinks(mRaster, dirMatrix);
        }
        
        // if have any basin, get by mixed method
        for(count = 0; count < allSubBasinsHash.length; count++){
            currentHash = allSubBasinsHash[count];
            curSubBasin = new SubBasin((Integer)currentHash.get("x"), 
                                       (Integer)currentHash.get("y"), 
                                       dirMatrix, mRaster);
            
            subBasinVec = new SubBasin[]{curSubBasin}; 
            
            //newPol.identifyBorderPointsByCrossing(tempVec, mRaster);
            newPol.identifyBorderPointsMixed(subBasinVec, 
                                             mRaster, 
                                             dirMatrix);
            
            
            // load inlets from file
            countIn = 1;
            do{
                // prepare labels
                inletXstr = "xIn_" + countIn;
                inletYstr = "yIn_" + countIn;
                
                // qet integer values
                inletX = (Integer)currentHash.get(inletXstr);
                inletY = (Integer)currentHash.get(inletYstr);
                
                // 'zuera' never stops but have limit
                if((inletX == null) || (inletY == null)) break;
                
                // add inlet
                curInletID = MetaRasterTool.getIdFromXandY(mRaster, inletX, inletY);
                curSubBasin.addInletID(curInletID);
                
                countIn++;
            } while(true);
            
            // resize area
            if (!curSubBasin.reshapeBasin(dirMatrix, mRaster)){
                System.out.println("FAIL reshaping");
            } else {
                System.out.println("SUCCESS reshaping");
            }
            
            // remove polygon from current basin
            if (!curSubBasin.extract(newPol, dirMatrix, dirRaster)){
                System.out.println("FAIL extracting");
            } else {
                System.out.println("SUCCESS extracting");
            }
            
            // write basin in SubBasinLogFile
            if (!subBasinsManager.addOrUpdateSubBasin(curSubBasin.getHashedData(mRaster))){
                System.out.println("FAIL adding or updating");
            } else {
                System.out.println("SUCCESS adding or updating");
            }
        }
        
        // create other subbasins with outlet in polygonInlets
        subBasinVec = newPol.getAllTributarySubBasins(mRaster, 
                                                      dirMatrix, 
                                                      metaNetwork);
        for(curInletID = 0; curInletID < subBasinVec.length; curInletID++){
            subBasinsManager.addOrUpdateSubBasin(
                    subBasinVec[curInletID].getHashedData(mRaster));
        }
        
        // TODO - remove checking print
        //System.out.println(newPol.toString(false, true));
    }
    
    /**
     * Used to write a new .inp file from a .poly file
     * @param args 
     */
    public static void main_old01(String[] args) {
        File metaRasterFile, rasterBinFile, polygonFile, directionsFile;
        String rasterBinFilePath, directionsFilePath, inpSwmmFilePath;
        String polygonFilePath, metarasterFilePath;
        MetaRaster mRaster, dirRaster;
        MetaPolygonUrban newPol;
        byte[][] dirMatrix;
        File inpSwmmFile;
        
        // mains parameters
        // WINDOWS
        polygonFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Polygons\\Bello_form.poly";
        metarasterFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Rasters\\Topography\\dem.metaDEM";
        rasterBinFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Rasters\\Topography\\dem.dem";
        directionsFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto\\bases_datos\\db_cuenbas_08_valAburra\\Rasters\\Topography\\dem.dir";
        inpSwmmFilePath = "D:\\andre\\trabalho\\2013_udem\\proyecto_swmmAcople\\inputs_swmm\\bello01_test.inp";
        
        // LINUX
        //polygonFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Polygons/Bello_form.poly";
        //metarasterFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Rasters/Topography/dem.metaDEM";
        //rasterBinFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Rasters/Topography/dem.dem";
        //directionsFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto/bases_datos/db_cuenbas_08_valAburra/Rasters/Topography/dem.dir";
        //inpSwmmFilePath = "/media/DATA/andre/trabalho/2013_udem/proyecto_swmmAcople/inputs_swmm/bello01_test.inp";
        
        // open first files
        polygonFile = new File(polygonFilePath);
        metaRasterFile = new File(metarasterFilePath);
        rasterBinFile = new File(rasterBinFilePath);
        
        // load polygon and meta raster
        try{
            newPol = new MetaPolygonUrban(polygonFile);
            mRaster = new MetaRaster (metaRasterFile);
            mRaster.setLocationBinaryFile(rasterBinFile);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return;
        }
        
        // open second files
        directionsFile = new File(directionsFilePath);
        inpSwmmFile = new File(inpSwmmFilePath);
        
        dirRaster = MetaPolygonUrban.getMetaRasterDirections(directionsFile,
                                                             metaRasterFile);
        dirMatrix = MetaPolygonUrban.getDirectionMatrix(dirRaster);
        
        //
        if (!newPol.identifyBorderPointsByLinks(mRaster, dirMatrix)){
            System.out.println("FAIL identifying border points.");
        }
        
        // TODO - remove checking print
        System.out.println(newPol.toString(false, true));
        
        // print SWMM (.inp) file
        newPol.exportToSwmmFile(inpSwmmFile, true);
    }
    
    
    
    
    /***********************************************************************/
    /***********************************************************************/
    /***************** METHODS TO BE USED WISELY ***************************/
    /***********************************************************************/
    /***********************************************************************/
    
    private static MetaPolygonUrban loadUrbanPolygon(File poly_arg){
        MetaPolygonUrban newPol;
        try{
            newPol = new MetaPolygonUrban(poly_arg);
            return (newPol);
        } catch (IOException exp){
            System.err.println();
            return (null);
        }
    }
    
    private static SubBasin[] loadSubBasinsFromFile(File subBasins_arg,
                                                   MetaRaster mRaster,
                                                   byte[][] dirMatrix){
        SubBasinsLogManager subBasinsManager;
        int count, countIn, curInletID;
        String inletXstr, inletYstr;
        HashMap[] allSubBasinsHash;
        Integer inletX, inletY;
        SubBasin[] allSubBasin;
        SubBasin curSubBasin;
        HashMap currentHash;
        
        try{
            subBasinsManager = new SubBasinsLogManager(subBasins_arg);
            allSubBasinsHash = subBasinsManager.getAllBasins();
        }catch (IOException exp) {
            System.err.println();
            return(null);
        }
        
        if ((allSubBasinsHash == null) || (allSubBasinsHash.length == 0)) return (null);
        
        allSubBasin = new SubBasin[allSubBasinsHash.length];
        for(count = 0; count < allSubBasinsHash.length; count++){
            
            currentHash = allSubBasinsHash[count];
            curSubBasin = new SubBasin((Integer)currentHash.get("x"), 
                                       (Integer)currentHash.get("y"), 
                                       dirMatrix, mRaster);
            
            // 2
            countIn = 1;
            do{
                // prepare labels
                inletXstr = "xIn_" + countIn;
                inletYstr = "yIn_" + countIn;
                
                // qet integer values
                inletX = (Integer)currentHash.get(inletXstr);
                inletY = (Integer)currentHash.get(inletYstr);
                
                // 'zuera' never stops but have limit
                if((inletX == null) || (inletY == null)) break;
                
                // add inlet
                curInletID = MetaRasterTool.getIdFromXandY(mRaster, inletX, inletY);
                curSubBasin.addInletID(curInletID);
                
                countIn++;
            } while(true);
            
            allSubBasin[count] = curSubBasin;
        }
        
        return(allSubBasin);
    }
    
    private static void reshapeSubBasins(SubBasin[] allSubBasins_arg,
                                         MetaPolygonUrban metaPolygon_arg,
                                         byte[][] directionMatrix_arg,
                                         MetaRaster mDirRaster,
                                         MetaRaster mDemRaster){
        int count;
        SubBasin curSubBasin;
        
        for(count = 0; count < allSubBasins_arg.length; count++){
            // 4
            curSubBasin = allSubBasins_arg[count];
            if (!curSubBasin.extract(metaPolygon_arg, 
                                     directionMatrix_arg, 
                                     mDirRaster)){
                System.out.println("FAIL extracting");
            } else {
                System.out.println("SUCCESS extracting");
            }
        }
    }
    
    /**
     * Method to be used to force Unfilled basins to be input to a UrbanPolygon and filled to be its output
     * @param allSubBasins_arg
     * @param metaPolygon_arg
     * @param mRaster_arg
     * @return 
     */
    private static TopologicalSolver connectObjectsC(SubBasin[] allSubBasins_arg,
                                                     MetaPolygonUrban metaPolygon_arg,
                                                     MetaRaster mRaster_arg){
        TopologicalSolver topoSolv;
        double latitude, longitude;
        SubBasin curSubBasin;
        int[] inletIDs;
        int count;
        
        for(count = 0; count < allSubBasins_arg.length; count++){
            curSubBasin = allSubBasins_arg[count];
            
            inletIDs = curSubBasin.getInletIDs();
            
            // verify type of subBasin by its anylets
            if ((inletIDs == null) || (inletIDs.length == 0)){
                // no inlets: treat as a tributary basin
                latitude = MetaRasterTool.getLatFromY(mRaster_arg, curSubBasin.getY());
                longitude = MetaRasterTool.getLonFromX(mRaster_arg, curSubBasin.getX());
                metaPolygon_arg.addNewInlet(latitude, longitude);
            } else {
                // has inlets: treat as receiver basin
                latitude = MetaRasterTool.getLatFromID(mRaster_arg, inletIDs[0]);
                longitude = MetaRasterTool.getLonFromID(mRaster_arg, inletIDs[0]);
                metaPolygon_arg.addNewOutlet(latitude, longitude);
            }
        }
        
        topoSolv = MainTesting.connectObjects(allSubBasins_arg, 
                                              metaPolygon_arg, 
                                              mRaster_arg);
        
        return(topoSolv);
    }
    
    /**
     * Method to be used to fill a TopologicalSolver when MetaPolygonUrban already have its anylets filled <br />
     * (just after SubBasin extraction from DEM)
     * @param allSubBasins_arg
     * @param metaPolygon_arg
     * @param mRaster_arg
     * @return 
     */
    private static TopologicalSolver connectObjects(SubBasin[] allSubBasins_arg,
                                                    MetaPolygonUrban metaPolygon_arg,
                                                    MetaRaster mRaster_arg){
        TopologicalSolver topoSolv;
        int count;
        
        topoSolv = new TopologicalSolver();
        topoSolv.addPolygonUrban(metaPolygon_arg);
        for(count = 0; count < allSubBasins_arg.length; count++){
            topoSolv.addSubBasin(allSubBasins_arg[count]);
        }
        topoSolv.establishLinkage(mRaster_arg);
        return (topoSolv);
    }
    
    private static void runCuencasForSubBasins(SubBasin[] allSubBasin_arg,
                                               MetaPolygonUrban metaPolyUrb_arg,
                                               TopologicalSolver topoSolv_arg,
                                               File outputDir_arg,
                                               MetaRaster mRaster, 
                                               MetaRaster magnRaster_arg,
                                               byte[][] directionMatrix_arg){
        SimulationToAsciiFile simulator;
        int count, countSubBasin;
        Hashtable routingParams;
        SubBasin curSubBasin;
        int[][] magnitudes;
        int[] theExecSeq;
        
        // define execution sequence
        theExecSeq = topoSolv_arg.getExecutionSequence();
        
        if (theExecSeq == null) {
            // lead with failured situation
            
            ArrayList<Comparable> allIso = topoSolv_arg.getIsolatedes();
            Comparable curComp;
            if(allIso == null){
                System.err.println("Impossible to determine isolated nodes.");
            } else if (allIso.isEmpty()) {
                 System.err.println("Not found any isolated nodes.");
            } else {
                Iterator<Comparable> isolatedNodesIt;
                isolatedNodesIt = allIso.iterator();
                System.err.print("Isolateds ("+ allIso.size() +"): ");
                while(isolatedNodesIt.hasNext()){
                    curComp = isolatedNodesIt.next();
                    System.err.print("{" + MetaRasterTool.getXfromID(mRaster, (Integer)curComp) + ", " + MetaRasterTool.getYfromID(mRaster, (Integer)curComp) + "}");
                    if (isolatedNodesIt.hasNext()) System.err.print("; ");
                }
                System.err.println("");
            }
        } else {
            // lead with success situation - show order
            try{
                magnitudes = new DataRaster(magnRaster_arg).getInt();
            } catch (IOException exp) {
                System.err.println("IOException: " + exp.getMessage());
                return;
            }
            
            // for each ordered element, if it is a SubBasin, run simulation
            System.out.println("Executing sequence:");
            for(count = 0; count < theExecSeq.length; count++){
                boolean objFound;
                
                objFound = false;
                System.out.print(count + " - " + theExecSeq[count]);
                
                if (theExecSeq[count] == metaPolyUrb_arg.getId()) {
                    System.out.println(": is a MetaPolyUrb");
                    continue;
                }
                
                // search if is SubBasin
                curSubBasin = null;
                for(countSubBasin = 0; countSubBasin < allSubBasin_arg.length; countSubBasin++){
                    curSubBasin = allSubBasin_arg[countSubBasin];
                    if (curSubBasin.getOutletID() == theExecSeq[count]){
                        System.out.println(": is a SubBasin (" + "x:" + curSubBasin.getX() + ", y:" + curSubBasin.getY() + ")");
                        objFound = true;
                        break;
                    }
                }
                
                if ((!objFound)||(curSubBasin == null)) continue;
                
                
                routingParams = MetaPolygonUrban.buildSimulationParams();
                /*
                public SimulationToAsciiFile(int x, int y, 
                                             byte[][] direcc, 
                                             int[][] magnitudes, 
                                             hydroScalingAPI.io.MetaRaster md, 
                                             float rainIntensity, 
                                             float rainDuration, 
                                             float infiltRate, 
                                             int routingType, 
                                            File outputDirectory,
                                            Hashtable routingParams);
                                            */
            
                try{
                    simulator = new SimulationToAsciiFile(curSubBasin.getX(),
                                                          curSubBasin.getY(),
                                                          directionMatrix_arg,
                                                          magnitudes,
                                                          magnRaster_arg,
                                                          1.5f,
                                                          60.0f,
                                                          0.5f,
                                                          5,
                                                          outputDir_arg,
                                                          routingParams);
                    simulator.executeSimulation();
                } catch (IOException exp) {
                    System.err.println("IOException: " + exp.getMessage());
                    return;
                } catch (VisADException exp) {
                    System.err.println("VisADException: " + exp.getMessage());
                    return;
                }
            }
        }
    }
    
    private static void readCuencasCsvFiles(File csvFilesDir_arg, 
                                            MetaPolygonUrban metaPolygon_arg,
                                            MetaRaster mRaster_arg){
        File[] allGeneratedFiles;
        double[][] resultMatrix;
        int[] xyBasin;
        int count;
        
        allGeneratedFiles = csvFilesDir_arg.listFiles();
        
        for(count = 0; count < allGeneratedFiles.length; count++){
            xyBasin = MetaPolygonUrban.getOutputCuencasFileBasin(allGeneratedFiles[count]);
            if (xyBasin != null){
                try{
                    resultMatrix = cuencasCsvFileReader.readCSVdischargeFile(allGeneratedFiles[count]);
                    metaPolygon_arg.setFlowResgister(xyBasin[0], xyBasin[1], 
                                                     resultMatrix, 
                                                     mRaster_arg);
                } catch (FileNotFoundException exp) {
                    System.err.println("FileNotFoundException: " + exp.getMessage());
                    return;
                } catch (IOException exp) {
                    System.err.println("IOException: " + exp.getMessage());
                    return;
                }
            }
        }
    }
}
