
package hydroScalingAPI.modules.swmmCoupling.objects;

import adlzanchetta.cuencasTools.cuencasCsvFileReader;
import hydroScalingAPI.examples.rainRunoffSimulations.SimulationToAsciiFile;
import hydroScalingAPI.modules.rainDataImporter.io.CSVFileReader;
import hydroScalingAPI.io.DataRaster;
import hydroScalingAPI.io.MetaNetwork;
import hydroScalingAPI.io.MetaPolygon;
import hydroScalingAPI.io.MetaRaster;
import hydroScalingAPI.io.swmmCoupling.PrecipitationWriter;
import hydroScalingAPI.modules.swmmCoupling.io.SubBasinsLogManager;
import hydroScalingAPI.modules.swmmCoupling.util.TopologicalSolver;
import hydroScalingAPI.modules.swmmCoupling.io.metaPolygonUrban.MetaPolygonUrban;
import hydroScalingAPI.modules.swmmCoupling.objects.CsvImporterBackground;
import hydroScalingAPI.modules.swmmCoupling.objects.DatabaseFilesManager;
import hydroScalingAPI.modules.swmmCoupling.util.geomorphology.objects.SubBasin;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
/*
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
*/
import visad.VisADException;

/**
 *
 * @author A. D. L. Zanchetta
 */
public class SwmmFileSketcher {
    private File polygonFile;
    private File metaDemFile;
    private File suBaLogFile;
    private File outputTmpDirectory;
    private File outputSwmmInpFile;
    private File inputCsvRainFile;
    private File inputVhcRainFile;
    private File databaseDirectory;
    private float infiltrationIntensity; // in mm/h
    private float channelFlowVelocity;   // in m/s
    private float hillslopeFlowVelocity; // in m/s
    private ArrayList<HashMap> addedBasins;
    
    /**
     * Constructor used only for ensure that all initial variables are null
     */
    public SwmmFileSketcher(){
        this.polygonFile = null;
        this.metaDemFile = null;
        this.suBaLogFile = null;
        this.outputSwmmInpFile = null;
        this.outputTmpDirectory = null;
        this.inputCsvRainFile = null;
        this.inputVhcRainFile = null;
        this.databaseDirectory = null;
        this.addedBasins = new ArrayList<HashMap>();
    }

    public void setInputCsvRainFile(File inputCsvRainFile) {
        this.inputCsvRainFile = inputCsvRainFile;
    }

    public void setInputVhcRainFile(File inputVhcRainFile) {
        this.inputVhcRainFile = inputVhcRainFile;
    }
    
    public void setPolygonFile(File polygonFile) {
        this.polygonFile = polygonFile;
    }

    public void setMetaDemFile(File metaDemFile) {
        this.metaDemFile = metaDemFile;
    }

    public void setSuBaLogFile(File suBaLogFile) {
        this.suBaLogFile = suBaLogFile;
    }

    public void setOutputTmpDirectory(File outputTmpDirectory) {
        this.outputTmpDirectory = outputTmpDirectory;
    }

    public void setOutputSwmmInpFile(File outputSwmmInpFile) {
        this.outputSwmmInpFile = outputSwmmInpFile;
    }

    public void setDatabaseDirectory(File databaseDirectory) {
        this.databaseDirectory = databaseDirectory;
    }

    public void setInfiltrationIntensity(float infiltrationIntensity) {
        this.infiltrationIntensity = infiltrationIntensity;
    }

    public void setChannelFlowVelocity(float channelFlowVelocity) {
        this.channelFlowVelocity = channelFlowVelocity;
    }

    public void setHillslopeFlowVelocity(float hillslopeFlowVelocity) {
        this.hillslopeFlowVelocity = hillslopeFlowVelocity;
    }
    
    public void addBasin(int x_arg, int y_arg){
        HashMap newHash;
        
        newHash = new HashMap();
        newHash.put("x", x_arg);
        newHash.put("y", y_arg);
        
        this.addedBasins.add(newHash);
    }
    
    public boolean importPolygon(File kmlFile_arg, String polygonId_arg) throws IOException{
        String outputPolyFilePath;
        File outputPolyFileFile;
        MetaPolygon newPol;
        MetaRaster mRaster;
        
        // basic check
        if((kmlFile_arg == null) || (!kmlFile_arg.exists())) return (false);
        if(polygonId_arg == null) return (false);
        
        // new polygon
        newPol = new MetaPolygon();
        
        // determine output polygon file path
        outputPolyFilePath = this.databaseDirectory.getPath();
        outputPolyFilePath += File.separator + "Polygons" + File.separator;
        outputPolyFilePath += polygonId_arg + ".poly";
        outputPolyFileFile = new File(outputPolyFilePath);
        
        // load meta raster
        mRaster = new MetaRaster(this.metaDemFile);
        mRaster.setLocationBinaryFile(this.getDemFile());
        
        // if everithing is all right, open kml file
        if (!MetaPolygonTools.readKmlFile(newPol, kmlFile_arg, 
                                          polygonId_arg, mRaster)){
            System.err.println("Nao foi possivel ler o arquivo");
            return (false);
        }
        
        // write content to file
        newPol.writePolygon(outputPolyFileFile);
        
        // everything was ok, return TRUE
        return(true);
    }
    
    public boolean generateInpFile() throws IOException{
        MetaRaster mDemRaster, magnRaster, direRaster;
        File binMagFile, binDirFile, binDemFile, rainFile;
        SubBasin[] autoSubBasin, addedSubBasin;
        MetaPolygonUrban polygonUrban;
        TopologicalSolver topoSolv;
        SubBasin[] allSubBasin;
        byte[][] dirMatrix;
        
        // 1
        // 1.1 - getting files which will be worked on
        // 1.2 - 
        // 2 -
        
        // basic check
        // TODO
        
        // 1.1 - geting files
        binDemFile = this.getDemFile();
        binMagFile = this.getMgnFile();
        binDirFile = this.getDirFile();
        this.suBaLogFile = this.getSuBFile();
        
        // 1.2
        try{
            mDemRaster = new MetaRaster(this.metaDemFile);
            mDemRaster.setLocationBinaryFile(binDemFile);
            //mDemRaster.setFormat("Integer");
        
            magnRaster = new MetaRaster(this.metaDemFile);
            magnRaster.setLocationBinaryFile(binMagFile);
            magnRaster.setFormat("Integer");
        } catch (IOException exp) {
            System.err.println("IOException: " + exp);
            return (false);
        }
        direRaster = MetaPolygonUrban.getMetaRasterDirections(binDirFile,
                                                              this.metaDemFile);
        dirMatrix = MetaPolygonUrban.getDirectionMatrix(direRaster);
        
        // import csv rain file if it is possible
        if (this.inputCsvRainFile != null){
            if (!this.importCSVrainfile(mDemRaster, this.inputCsvRainFile)){
                System.err.println("Filed to open '" + this.inputCsvRainFile.getAbsolutePath() + "' file.");
                return (false);
            } else {
                // if it was able to generate rain file, load it
                this.inputVhcRainFile = DatabaseFilesManager.loadAvailableRainFile(databaseDirectory, 
                                                                                   "temporary");
            }
        }
        
        // 2
        polygonUrban = new MetaPolygonUrban(this.polygonFile);
        autoSubBasin = SwmmFileSketcher.loadSubBasinsFromFile(this.suBaLogFile,
                                                                mDemRaster, 
                                                                dirMatrix);      
        addedSubBasin = this.loadAddedSubbasin(mDemRaster, dirMatrix);
        allSubBasin = this.mergeBasinLists(autoSubBasin, addedSubBasin);
        
        SwmmFileSketcher.reshapeSubBasins(allSubBasin, polygonUrban,
                                            dirMatrix, direRaster);
        topoSolv = SwmmFileSketcher.connectObjectsC(allSubBasin, 
                                                      polygonUrban, 
                                                      mDemRaster);
        
        SwmmFileSketcher.runCuencasForSubBasins(allSubBasin, 
                                                  polygonUrban,
                                                  topoSolv, 
                                                  this.outputTmpDirectory,
                                                  mDemRaster, 
                                                  magnRaster,
                                                  dirMatrix, 
                                                  this.inputVhcRainFile,
                                                  this.infiltrationIntensity,
                                                  this.channelFlowVelocity,
                                                  this.hillslopeFlowVelocity);
        
        SwmmFileSketcher.readCuencasCsvFiles(this.outputTmpDirectory, 
                                               polygonUrban, 
                                               mDemRaster);
        
        // add rain data
        float[][] tmpRemove;
        tmpRemove = SwmmFileSketcher.readCuencasRain(this.outputTmpDirectory);
        
        //tmpRemove = new float[2][2];
        //tmpRemove[0][0] = 1f;
        //tmpRemove[0][1] = 0.1f;
        //tmpRemove[1][0] = 3f;
        //tmpRemove[1][1] = 0.3f;
        
        polygonUrban.setRainData(tmpRemove);
        
        // write accumulated data to SWMM inp file
        polygonUrban.exportToSwmmFile(this.outputSwmmInpFile, true);
        
        return (true);
    }
    
    private File getSuBFile(){
        return (this.getTopoFileByExtention("subLog"));
    }
    
    private File getSuBFile(boolean forceCreation_arg){
        return (this.getTopoFileByExtention("subLog", forceCreation_arg));
    }
    
    private File getDirFile(){
        return (this.getTopoFileByExtention("dir"));
    }
    
    private File getMgnFile(){
        return (this.getTopoFileByExtention("magn"));
    }
    
    private File getDemFile(){
        return (this.getTopoFileByExtention("dem"));
    }
    
    private File getTopoFileByExtention(String extention_arg){
        return (this.getTopoFileByExtention(extention_arg, false));
    }
    
    private File getTopoFileByExtention(String extention_arg, 
                                        boolean forceCreation_arg){
        String metaDemFilePath, newFilePath;
        File newFile;
        
        if (this.metaDemFile == null) return (null);
        
        metaDemFilePath = this.metaDemFile.getAbsolutePath();
        newFilePath = metaDemFilePath.replace(".metaDEM", "."+extention_arg);
        
        newFile = new File(newFilePath);
        
        // create new file if necessary
        if (!forceCreation_arg){
            if (newFile.exists()) return (newFile);
            else return (null);
        } else {
            if (!newFile.exists()){
                try{
                    if (!newFile.createNewFile()) return (null);
                } catch (IOException exp) {
                    System.err.println("Exception: " + exp.getMessage());
                    return (null);
                }
            }
            return (newFile);
        }
    }
    
    /**************************************/
    
    private static SubBasin[] loadSubBasinsFromFile(File subBasins_arg,
                                                   MetaRaster mRaster,
                                                   byte[][] dirMatrix) throws IOException{
        SubBasinsLogManager subBasinsManager;
        int count, countIn, curInletID;
        String inletXstr, inletYstr;
        HashMap[] allSubBasinsHash;
        Integer inletX, inletY;
        SubBasin[] allSubBasin;
        SubBasin curSubBasin;
        HashMap currentHash;
        subBasinsManager = new SubBasinsLogManager(subBasins_arg);
        allSubBasinsHash = subBasinsManager.getAllBasins();
        
        if ((allSubBasinsHash == null) || (allSubBasinsHash.length == 0)) {
            return (null);
        }
        
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
    
    private SubBasin[] loadAddedSubbasin(MetaRaster mRaster, byte[][] dirMatrix){
        SubBasin[] returnSubBasins;
        Iterator<HashMap> basinsIt;
        SubBasin currentSubBasin;
        HashMap currentHashMap;
        int count;
        
        // initial position
        returnSubBasins = new SubBasin[this.addedBasins.size()];
        count = 0;
        basinsIt = this.addedBasins.iterator();
        
        // iterating
        while(basinsIt.hasNext()){
            currentHashMap = basinsIt.next();
            currentSubBasin = new SubBasin((Integer)currentHashMap.get("x"), 
                                           (Integer)currentHashMap.get("y"), 
                                           dirMatrix, mRaster);
            returnSubBasins[count] = currentSubBasin;
            count++;
        }
        
        return(returnSubBasins);
    }
    
    private static void reshapeSubBasins(SubBasin[] allSubBasins_arg,
                                         MetaPolygonUrban metaPolygon_arg,
                                         byte[][] directionMatrix_arg,
                                         MetaRaster mDirRaster){
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
        int[][] altitudeMatrix;
        int curH, curX, curY, curID;
        SubBasin curSubBasin;
        int[] inletIDs;
        int count;
        
        altitudeMatrix = MetaPolygonUrban.getAltitudeMatrix(mRaster_arg);
        
        for(count = 0; count < allSubBasins_arg.length; count++){
            curSubBasin = allSubBasins_arg[count];
            
            inletIDs = curSubBasin.getInletIDs();
            
            // verify type of subBasin by its anylets
            if ((inletIDs == null) || (inletIDs.length == 0)){
                // no inlets: treat as a tributary basin
                curX = curSubBasin.getX();
                curY = curSubBasin.getY();
                latitude = MetaRasterTool.getLatFromY(mRaster_arg, curY);
                longitude = MetaRasterTool.getLonFromX(mRaster_arg, curX);
                curH = 0;
                if(altitudeMatrix != null){
                    if(altitudeMatrix.length > curY){
                        if(altitudeMatrix[curY].length > curX){
                            curH = altitudeMatrix[curY][curX];
                        }
                    }
                }
                metaPolygon_arg.addNewInlet(latitude, longitude, curH);
            } else {
                // has inlets: treat as receiver basin
                latitude = MetaRasterTool.getLatFromID(mRaster_arg, inletIDs[0]);
                longitude = MetaRasterTool.getLonFromID(mRaster_arg, inletIDs[0]);
                curH = 0;
                if(altitudeMatrix != null){
                    curID = MetaRasterTool.getIdFromLongLat(mRaster_arg,
                                                           (float)longitude, 
                                                           (float)latitude);
                    curX = MetaRasterTool.getXfromID(mRaster_arg, curID);
                    curY = MetaRasterTool.getXfromID(mRaster_arg, curID);
                    if(altitudeMatrix.length > curY){
                        if(altitudeMatrix[curY].length > curX){
                            curH = altitudeMatrix[curY][curX];
                        }
                    }
                }
                metaPolygon_arg.addNewOutlet(latitude, longitude, curH);
            }
        }
        
        topoSolv = SwmmFileSketcher.connectObjects(allSubBasins_arg, 
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
    
    public void remakeSubLogFile() throws IOException{
        MetaRaster metaDirRaster, metaDemRaster;
        SubBasinsLogManager subBasinsManager;
        SubBasin[] subBasinVec, subBasinVec2;
        MetaPolygonUrban polygonUrban;
        File binDirFile, binDemFile;
        MetaNetwork metaNetwork;
        byte[][] dirMatrix;
        int curInletID;
        
        try{
            polygonUrban = new MetaPolygonUrban(this.polygonFile);
        } catch (IOException exp) {
            System.err.println("");
            return;
        }
        
        binDemFile = this.getDemFile();
        binDirFile = this.getDirFile();
        this.suBaLogFile = this.getSuBFile(true);
        
        // 1.2
        try{
            metaDemRaster = new MetaRaster(this.metaDemFile);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp);
            return;
        }
        metaDirRaster = MetaPolygonUrban.getMetaRasterDirections(binDirFile,
                                                                 this.metaDemFile);
        dirMatrix = MetaPolygonUrban.getDirectionMatrix(metaDirRaster);
        
        metaDemRaster.setLocationBinaryFile(binDemFile);
        try{
            metaNetwork = new MetaNetwork(metaDemRaster);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp);
            return;
        }
        
        // frank
        //if(!polygonUrban.identifyBorderPointsByMask(metaDirRaster, dirMatrix)){
        if(!polygonUrban.identifyBorderPointsMixed(metaDirRaster, dirMatrix)){
            System.out.println("FAIL identifying border points by mask.");
        }
        subBasinVec2 = polygonUrban.getAllTributarySubBasins(metaDemRaster, 
                                                             dirMatrix, 
                                                             metaNetwork);
        subBasinsManager = new SubBasinsLogManager(this.suBaLogFile);
        for(curInletID = 0; curInletID < subBasinVec2.length; curInletID++){
            SubBasin tmpSubBas;
            tmpSubBas = subBasinVec2[curInletID];
            if (subBasinsManager.addOrUpdateSubBasin(
                    subBasinVec2[curInletID].getHashedData(metaDemRaster))){
                System.out.println("_SUCCESS add ["+subBasinVec2[curInletID].getX()+", "+subBasinVec2[curInletID].getY()+"] inlet as subbasin ("+curInletID+") .");
            } else {
                System.out.println("__FAILED add ["+subBasinVec2[curInletID].getX()+", "+subBasinVec2[curInletID].getY()+"] inlet as subbasin ("+curInletID+").");
            }
        }
        

        
        /*
        
        // correct
        final int xBiggerBasin = 671;
        final int yBiggerBasin = 629;
        SubBasin masterBiggerBasin;
        masterBiggerBasin = new SubBasin(xBiggerBasin, yBiggerBasin,
                                         dirMatrix, metaDemRaster);
        if (!polygonUrban.identifyBorderPointsMixed(masterBiggerBasin, 
                                                    metaDirRaster, 
                                                    dirMatrix)){
            System.out.println("FAIL identifying border points.");
        }
        
        // create other subbasins with outlet in polygonInlets
        subBasinVec = polygonUrban.getAllTributarySubBasins(metaDemRaster, 
                                                            dirMatrix, 
                                                            metaNetwork);
        
        */
        
        System.out.print("");
    }
    
    private static void runCuencasForSubBasins(SubBasin[] allSubBasin_arg,
                                               MetaPolygonUrban metaPolyUrb_arg,
                                               TopologicalSolver topoSolv_arg,
                                               File outputDir_arg,
                                               MetaRaster mRaster, 
                                               MetaRaster magnRaster_arg,
                                               byte[][] directionMatrix_arg,
                                               File rainMetaVHCFile_arg,
                                               float infiltrationIntensity_arg,
                                               float channelFlowVelocity_arg,
                                               float hillslopeFlowVelocity_arg){
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
                
                // 
                if (theExecSeq[count] == metaPolyUrb_arg.getId()) {
                    System.out.println(": is a MetaPolyUrb");
                    continue;
                }
                
                // search if current evaluated is SubBasin
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
                
                
                routingParams = MetaPolygonUrban.buildSimulationParams(
                                                    channelFlowVelocity_arg,
                                                    hillslopeFlowVelocity_arg);
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
                                                          rainMetaVHCFile_arg,
                                                          infiltrationIntensity_arg,
                                                          5,
                                                          outputDir_arg,
                                                          routingParams);
                    
                    /*
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
                    */
                    
                    try{
                        simulator.executeSimulation();
                    } catch (Exception exp) {
                        System.err.println("Exception: " + exp.getMessage());
                    }
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
    
    /**
     * 
     * @param csvFilesDir_arg
     * @param metaPolygon_arg
     * @param mRaster_arg 
     */
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
                    System.err.println("Exception: " + exp.getMessage());
                } catch (IOException exp) {
                    System.err.println("Exception: " + exp.getMessage());
                }
            }
        }
    }
    
    /**
     * 
     * @param csvFilesDir_arg
     * @return 
     */
    private static float[][] readCuencasRain(File csvFilesDir_arg) throws FileNotFoundException, IOException{
        File[] allGeneratedFiles;
        float[][] resultMatrix;
        int[] xyBasin;
        int count;
        
        if ((csvFilesDir_arg == null) || (!csvFilesDir_arg.isDirectory())) return (null);
            
        allGeneratedFiles = csvFilesDir_arg.listFiles();
        
        for(count = 0; count < allGeneratedFiles.length; count++){
            xyBasin = MetaPolygonUrban.getOutputCuencasFileBasin(allGeneratedFiles[count]);
            if (xyBasin != null){
                resultMatrix = cuencasCsvFileReader.readRainData(allGeneratedFiles[count]);
                return (resultMatrix);
            }
        }
        return (null);
    }
    
    /**
     * Read a CSV File with certain format and generate a temporary.metaVHC file
     * @param demMetaRaster_arg
     * @param csvFile_arg
     * @return 
     */
    private boolean importCSVrainfile(MetaRaster demMetaRaster_arg,
                                      File csvFile_arg){
        CsvImporterBackground importerBackground;
        PrecipitationWriter precFileWriter;
        String[][] csvContent;
        
        // read CSV brute content
        csvContent = CSVFileReader.readEntireFile(csvFile_arg, ';');
        
        // preparing file writer
        precFileWriter = new PrecipitationWriter(demMetaRaster_arg);
        precFileWriter.setDataInterval(PrecipitationWriter.INTERVAL_MIN, 15);
        precFileWriter.setInputDateFormat("dd/MM/yyyy hh:mm");
        precFileWriter.setPrecipitationTitle("temporary");
        
        // prepare for running on back ground
        importerBackground = new CsvImporterBackground(precFileWriter, 
                                                       null);
        importerBackground.setDataSequence(csvContent);
        importerBackground.setExampleMetaRaster(demMetaRaster_arg);
        importerBackground.execute();
        
        return(true);
    }
    
    private File[] loadFilesInSubDirectoryRec(String subDirPath_arg,
                                              String fileExtention_arg){
        String subDirectoryFullPath;
        File subDirectory;
        Collection files;
        Iterator filesIt;
        File[] retVec;
        int count;
        
        // basic check
        if (this.databaseDirectory == null) return (null);
        
        // get absolute path for directoy
        subDirectoryFullPath = this.databaseDirectory.getPath() + subDirPath_arg;
        subDirectory = new File(subDirectoryFullPath);
        
        // list all internal files recursively
        files = FileUtils.listFiles(subDirectory, 
                                    new RegexFileFilter(".*" + fileExtention_arg), 
                                    DirectoryFileFilter.DIRECTORY);
        
        // convert obtained list into a vector
        if (files != null){
            retVec = new File[files.size()];
            filesIt = files.iterator();
            count = 0;
            while(filesIt.hasNext()){
                retVec[count] = (File)filesIt.next();
                count++;
            }
        } else {
            retVec = null;
        }
        
        return(retVec);
    }
    
    private SubBasin[] mergeBasinLists(SubBasin[] list1, SubBasin[] list2){
        SubBasin[] returnList;
        int count, countRet;
        
        returnList = new SubBasin[list1.length + list2.length];
        countRet = 0;
        
        for(count = 0; count < list1.length; count++){
            returnList[countRet] = list1[count];
            countRet++;
        }
        
        for(count = 0; count < list2.length; count++){
            returnList[countRet] = list2[count];
            countRet++;
        }
        
        return (returnList);
    }
}
