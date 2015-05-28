package hydroScalingAPI.modules.rainDataImporter.objects;

import hydroScalingAPI.io.BasinsLogReader;
import hydroScalingAPI.io.MetaNetwork;
import hydroScalingAPI.io.MetaRaster;
import java.io.File;
import java.io.FilenameFilter;
import hydroScalingAPI.mainGUI.ParentGUI;
import hydroScalingAPI.mainGUI.objects.GUI_InfoManager;
import hydroScalingAPI.mainGUI.objects.GaugesManager;
import java.io.IOException;
import java.util.Hashtable;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

/**
 * Class with general auxiliary methods for dealing with CUENCAS objects
 * @author A. D. L. Zanchetta
 */
public abstract class SwmmCuencasLibrary {
    public static final TimeZone defaultTimeZone = TimeZone.getTimeZone("UTC");
    
    public static final int UNFORCED = 0;
    public static final int FORCE_NEW = 1;
    public static final int FORCE_UPDATE = 2;
    
    private static ParentGUI parentGUI;
    private static final String[] extension={  ".dem",
                                  ".corrDEM",
                                  ".dir",
                                  ".areas",
                                  ".horton",
                                  ".lcp",
                                  ".ltc",
                                  ".gdo",
                                  ".tdo",
                                  ".mcd",
                                  ".tcd",
                                  ".magn",
                                  ".slope",
                                  ".dtopo",
                                  ".redRas"};
                                  
    private static final String[] derivedName={     " Digital Elevation Model",
                                      " Fixed Elevation Model",
                                      " Drainage Directions",
                                      " Accumulated Area",
                                      " Horton Numbers",
                                      " Longest Channel Length",
                                      " Total Channels Length",
                                      " Geometric Distance to Border",
                                      " Topologic Distance to Border",
                                      " Maximum Channel Drop",
                                      " Total Channels Drop",
                                      " Magnitude",
                                      " Gradient Value",
                                      " Topologic Diameter",
                                      " Raster Drainage Network"};
    
    public static void setParentGUI(ParentGUI parentGUI_arg){
        SwmmCuencasLibrary.parentGUI = parentGUI_arg;
    }
    
    public static ParentGUI getParentGUI(){
        return (SwmmCuencasLibrary.parentGUI);
    }
    
    public static String[] getDatasetTitles(){
        String[] allDatasetTitles;
        File[] allMetaDEMs;
        String currentName;
        int count;
        
        // basic check
        if (SwmmCuencasLibrary.parentGUI == null) return (null);
        
        allMetaDEMs = SwmmCuencasLibrary.getMetaDEMs();
        
        // basic check
        if (allMetaDEMs == null) return (null);
        
        // getting all filenames
        allDatasetTitles = new String[allMetaDEMs.length];
        for(count = 0; count < allMetaDEMs.length; count++){
            currentName = allMetaDEMs[count].getName();
            if (currentName.endsWith(".metaDEM")){
                allDatasetTitles[count] = currentName.substring(0, currentName.length() - 8);
            } else {
                allDatasetTitles[count] = currentName;
            }
            System.out.println("[" + count + "] " + allDatasetTitles[count]);
        }
        
        return(allDatasetTitles);
    }
    
    /**
     * Search all .metaDEM files located inside current open database
     * @return Array of all .metaDEM files located inside current open database
     */
    public static File[] getMetaDEMs(){
        File demsDirectory;
        FilenameFilter metaDEMsFileFilter;
        hydroScalingAPI.mainGUI.objects.GUI_InfoManager infoManager;
        
        // basic check
        if (SwmmCuencasLibrary.parentGUI == null) return (null);
        
        infoManager = SwmmCuencasLibrary.parentGUI.getInfoManager();
        //this.parentGUI.get
        
        // basic check
        if (infoManager == null) return (null);
        
        demsDirectory = infoManager.dataBaseRastersDemPath;
        
        // basic check
        if ((demsDirectory == null) || (!demsDirectory.exists()) || (!demsDirectory.isDirectory()))
            return (null);
        
        // create file filter
        metaDEMsFileFilter = new FilenameFilter() {
            public boolean accept(File directory, String fileName) {
                return fileName.endsWith(".metaDEM");
            }
        };
        
        return (demsDirectory.listFiles(metaDEMsFileFilter));
    }
    
    public static String getDatasetReport(String datasetName_arg){
        Object[][] returnedMatrix;
        
        returnedMatrix = SwmmCuencasLibrary.getDatasetReportMatrix(datasetName_arg);
        
        return (SwmmCuencasLibrary.getDatasetReport(returnedMatrix));
    }
    
    /**
     * Convert content in matrix to a single String separated in lines.
     * @param datasetReportMatrix_arg Matrix returned by getDatasetReportMatrix
     * @return String with content if given Matrix is valid, NULL otherwise
     */
    public static String getDatasetReport(Object[][] datasetReportMatrix_arg){
        String textValue;
        int count;
        
        // basic checks
        if (datasetReportMatrix_arg == null) return (null);
        if ((datasetReportMatrix_arg.length > 0) && 
            (datasetReportMatrix_arg[0].length < 2)){
                return (null);
        }
        
        textValue = "";
        
        // add each element as a line
        for(count = 0; count < datasetReportMatrix_arg.length; count++){
            textValue += "-" + datasetReportMatrix_arg[count][0];
            textValue += ": " + datasetReportMatrix_arg[count][1];
            if (count != (datasetReportMatrix_arg.length - 1)){
                textValue += '\n';
            }
        }
        
        return(textValue);
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return Vector of String and Boolean dimensions Nx2
     */
    public static Object[][] getDatasetReportMatrix(String datasetName_arg){
        Object[][] returnMatrix;
        File currentFile;
        
        // basic checks
        if (datasetName_arg == null) return (null);
        
        // allocs space
        returnMatrix = new Object[3][2];
        
        // check if there is Meta DEM file
        currentFile = SwmmCuencasLibrary.getMetaDEMFile(datasetName_arg);
        returnMatrix[0][0] = "MetaDEM";
        if(currentFile != null){
            returnMatrix[0][1] = true;
        } else {
            returnMatrix[0][1] = false;
        }
        
        // check if there is DEM file
        currentFile = SwmmCuencasLibrary.getDEMFile(datasetName_arg);
        returnMatrix[1][0] = "DEM";
        if(currentFile != null){
            returnMatrix[1][1] = true;
        } else {
            returnMatrix[1][1] = false;
        }
        
        // check if there is Basin file
        currentFile = SwmmCuencasLibrary.getBasinLogFile(datasetName_arg);
        returnMatrix[2][0] = "Basin log";
        if(currentFile != null){
            returnMatrix[2][1] = true;
        } else {
            returnMatrix[2][1] = false;
        }
        
        return (returnMatrix);
    }
    
    public static MetaRaster getMetaRaster(String datasetName_arg){
        MetaRaster returnObject;
        File metaDemFile;
        File binaryFile;
        
        metaDemFile = SwmmCuencasLibrary.getMetaDEMFile(datasetName_arg);
        
        // basic check
        if (metaDemFile == null) return (null);
        
        try{
            returnObject = new MetaRaster(metaDemFile);
        } catch(IOException exp) {
            System.out.println("Exception getting MetaRaster DEM object: " + exp.getMessage());
            return (null);
        }
        
        // set data comming from DEM file
        binaryFile = SwmmCuencasLibrary.getDEMFile(datasetName_arg);
        returnObject.setLocationBinaryFile(binaryFile);
        
        return (returnObject);
    }
    
    public static MetaRaster getMetaRasterDirections(String datasetName_arg){
        MetaRaster returnObject;
        File metaDemFile;
        File binaryFile;
        
        metaDemFile = SwmmCuencasLibrary.getMetaDEMFile(datasetName_arg);
        
        // basic check
        if (metaDemFile == null) return (null);
        
        try{
            returnObject = new MetaRaster(metaDemFile);
        } catch(IOException exp) {
            System.out.println("Exception getting MetaRaster DEM object: " + exp.getMessage());
            return (null);
        }
        
        // set data as being from flow direction file
        binaryFile = SwmmCuencasLibrary.getDirFile(datasetName_arg);
        returnObject.setLocationBinaryFile(binaryFile);
        returnObject.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
        
        return (returnObject);
    }
    
    public static MetaRaster getMetaRasterMagnetudes(String datasetName_arg){
        MetaRaster returnObject;
        File metaDemFile;
        File binaryFile;
        
        metaDemFile = SwmmCuencasLibrary.getMetaDEMFile(datasetName_arg);
        
        // basic check
        if (metaDemFile == null) return (null);
        
        try{
            returnObject = new MetaRaster(metaDemFile);
        } catch(IOException exp) {
            System.out.println("Exception getting MetaRaster DEM object: " + exp.getMessage());
            return (null);
        }
        
        // set data as being from flow direction file
        binaryFile = SwmmCuencasLibrary.getMagnFile(datasetName_arg);
        returnObject.setLocationBinaryFile(binaryFile);
        returnObject.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".magn"));
        
        return (returnObject);
    }
    
    public static MetaRaster getMetaRasterElevations(String datasetName_arg){
        MetaRaster returnObject;
        File metaDemFile;
        File binaryFile;
        
        metaDemFile = SwmmCuencasLibrary.getMetaDEMFile(datasetName_arg);
        
        // basic check
        if (metaDemFile == null) return (null);
        
        try{
            returnObject = new MetaRaster(metaDemFile);
        } catch(IOException exp) {
            System.out.println("Exception getting MetaRaster DEM object: " + exp.getMessage());
            return (null);
        }
        
        // set data as being from elevations file
        binaryFile = SwmmCuencasLibrary.getElevationFile(datasetName_arg);
        returnObject.setLocationBinaryFile(binaryFile);
        returnObject.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
        
        return (returnObject);
    }
    
    public static MetaNetwork getMetaNetwork(String datasetName_arg){
        MetaNetwork returnObject;
        MetaRaster metaData;
        
        metaData = SwmmCuencasLibrary.getMetaRaster(datasetName_arg);
        
        // basic check
        if (metaData == null) return (null);
        
        try{
            returnObject = new MetaNetwork(metaData);
        } catch(IOException exp) {
            System.out.println("Exception getting MetaRaster network object: " + exp.getMessage());
            return (null);
        }
        
        return (returnObject);
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return 
     */
    public static File getMetaDEMFile(String datasetName_arg){
        return (SwmmCuencasLibrary.getFileWithExtension(datasetName_arg,
                                                        ".metaDEM"));
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return 
     */
    public static File getElevationFile(String datasetName_arg){
        return (SwmmCuencasLibrary.getFileWithExtension(datasetName_arg,
                                                        ".corrDEM"));
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return 
     */
    public static File getDEMFile(String datasetName_arg){
        return (SwmmCuencasLibrary.getFileWithExtension(datasetName_arg,
                                                        ".dem"));
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return 
     */
    public static File getDirFile(String datasetName_arg){
        return (SwmmCuencasLibrary.getFileWithExtension(datasetName_arg,
                                                        ".dir"));
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return 
     */
    public static File getMagnFile(String datasetName_arg){
        return (SwmmCuencasLibrary.getFileWithExtension(datasetName_arg,
                                                        ".magn"));
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return 
     */
    public static File getBasinLogFile(String datasetName_arg){
        return (SwmmCuencasLibrary.getFileWithExtension(datasetName_arg,
                                                        ".log"));
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return 
     */
    public static byte[][] getDirectionMatrix(String datasetName_arg){
        MetaRaster directionRaster;
        
        directionRaster = SwmmCuencasLibrary.getMetaRasterDirections(datasetName_arg);
        
        return (SwmmCuencasLibrary.getDirectionMatrix(directionRaster));
    }
    
    /**
     * 
     * @param directionRaster_arg
     * @return 
     */
    public static byte[][] getDirectionMatrix(MetaRaster directionRaster_arg){
        byte[][] directionMatrix;
        
        // basic check
        if(directionRaster_arg == null) return (null);

        try{
            directionMatrix = new hydroScalingAPI.io.DataRaster(directionRaster_arg).getByte();
        } catch (IOException exp){
            System.err.println("IOException: " + exp);
            return (null);
        }
        
        return (directionMatrix);
    }
    
    public static int[][] getMagnetudeMatrix(String datasetName_arg){
        MetaRaster magnetudesRaster;
        int [][] magnitudes;
        
        magnetudesRaster = SwmmCuencasLibrary.getMetaRasterMagnetudes(datasetName_arg);
        
        //metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        //metaModif.setFormat("Integer");
        
        try{
            magnitudes=new hydroScalingAPI.io.DataRaster(magnetudesRaster).getInt();
            return (magnitudes);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (null);
        }
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return 
     */
    public static BasinsLogReader getBasinLogReader(String datasetName_arg){
        BasinsLogReader returnObject;
        File basinsLogFile;
        
        basinsLogFile = SwmmCuencasLibrary.getBasinLogFile(datasetName_arg);
        try{
            returnObject = new BasinsLogReader(basinsLogFile);
        } catch (IOException exp){
            System.err.println("IOException: " + exp.getMessage());
            return (null);
        }
        
        return (returnObject);
    }
    
    public static File getGaugesDirectory(){
        return (SwmmCuencasLibrary.getGaugesDirectory(false));
    }
    
    /**
     * 
     * @param forceCreation_arg
     * @return 
     */
    public static File getGaugesDirectory(boolean forceCreation_arg){
        GUI_InfoManager infomanager;
        
        // basic check
        if (SwmmCuencasLibrary.parentGUI == null) return (null);
        
        // get Information Manager object reference and basic check it
        infomanager = SwmmCuencasLibrary.parentGUI.getInfoManager();
        if (infomanager == null) return (null);
        
        // if must not create directory and there is no directory, dont do it
        if ((!forceCreation_arg) && (!infomanager.dataBaseSitesGaugesExists))
            return (null);
        
        // create directory if necessary
        if (forceCreation_arg && (!infomanager.dataBaseSitesGaugesExists))
            infomanager.dataBaseSitesGaugesPath.mkdir();
        
        //
        return (infomanager.dataBaseSitesGaugesPath);
    }
    
    /**
     * 
     * @param datasetName_arg
     * @return 
     */
    public static GaugesManager getGaugesManager(String datasetName_arg){
        //hydroScalingAPI.mainGUI.objects.GUI_InfoManager infoManager;
        GaugesManager returnObject;
        
        // basic check
        if (SwmmCuencasLibrary.parentGUI == null) return (null);
        
        returnObject = SwmmCuencasLibrary.parentGUI.getGaugesManager();
        
        return (returnObject);
    }
    
    /**
     * 
     * @param datasetName_arg
     * @param extension_arg
     * @return 
     */
    private static File getFileWithExtension(String datasetName_arg, 
                                             String extension_arg){
        hydroScalingAPI.mainGUI.objects.GUI_InfoManager infoManager;
        String metaDEMFilePath;
        File demsDirectory;
        File metaDEMFile;
        
        // basic check
        if (SwmmCuencasLibrary.parentGUI == null) return (null);
        
        infoManager = SwmmCuencasLibrary.parentGUI.getInfoManager();
        
        // basic check
        if (infoManager == null) return (null);
        
        demsDirectory = infoManager.dataBaseRastersDemPath;
        
        // basic check
        if (demsDirectory == null) return (null);
        
        // getting file
        metaDEMFilePath = demsDirectory.getAbsolutePath();
        metaDEMFilePath += File.separator;
        metaDEMFilePath += datasetName_arg;
        if (metaDEMFilePath != null){
            metaDEMFilePath += extension_arg;
        }
        metaDEMFile = new File(metaDEMFilePath);
        
        // returning if file exists
        if (metaDEMFile.exists()){
            return (metaDEMFile);
        } else {
            return (null);
        }
    }
    
    /**
     * 
     * @param basinLabel_arg
     * @return 
     */
    public static int[] getXandYfromBasinLabel(String basinLabel_arg){
        String[] basinLabelSplited;
        String xLabel, yLabel;
        int[] returnVetor;
        int matX, matY;
        
        // spliting
        basinLabelSplited = basinLabel_arg.split(" ; ");
        xLabel=(basinLabelSplited[0].split(", "))[0];
        yLabel=(basinLabelSplited[0].split(", "))[1];
        
        // getting X and Y numerical values
        matX=Integer.parseInt(xLabel.substring(2).trim());
        matY=Integer.parseInt(yLabel.substring(2).trim());
        
        // storing in single variable
        returnVetor = new int[2];
        returnVetor[0] = matX;
        returnVetor[1] = matY;
        
        return(returnVetor);
    }
    
    /**
     * Get values X and Y contained in a ID from a MetaNetwork object
     * @param ID_arg ID to be broke
     * @param metaData_arg MetaRader with any binary file with right size
     * @return An int[2]{X, Y} if possible doing conversion, NULL otherwise
     */
    public static int[] getXandYfromId(int ID_arg, MetaRaster metaData_arg){
        int demNumCols;
        int i, j;
           
        // basic check
        if (metaData_arg == null) return (null);
            
        // getting coordinates
        demNumCols = metaData_arg.getNumCols();
        j = ID_arg/demNumCols;
        i = ID_arg%demNumCols;
            
        return (new int[]{i,j});
    }
    
    public static int getIdFromXandY(int x_arg, int y_arg, MetaRaster metaData_arg){
        int demNumCols;
        int returnId;
           
        // basic check
        if (metaData_arg == null) return (-1);
            
        // getting coordinates
        demNumCols = metaData_arg.getNumCols();
        returnId = (demNumCols * y_arg) + x_arg;
            
        return (returnId);
    }
    
    public static Hashtable getRelatedMaps(String datasetName_arg){
        java.util.Hashtable nameToFile = new java.util.Hashtable();
        hydroScalingAPI.mainGUI.objects.GUI_InfoManager infoManager;
        File demDir;
        String fileName, fileDirPath, filePath;
        
        infoManager = SwmmCuencasLibrary.parentGUI.getInfoManager();
        demDir = infoManager.dataBaseRastersDemPath;
        fileDirPath = demDir.getAbsolutePath();
        
        for (int i=0;i<derivedName.length;i++){
            fileName = datasetName_arg + extension[i];
            filePath = fileDirPath + File.separator + fileName;
            nameToFile.put(i, filePath);
            //nameToFile.put((String)elemDeriv.get(i),
            //               demPath+"/"+demName+extension[i]);
        }
        
        return nameToFile;
    }
    
    /**
     * 
     * @param basinLabel_arg
     * @return 
     */
    public static long getCodefromBasinLabel(String basinLabel_arg){
        String[] basinLabelSplited;
        String codeLabel;
        long returnCode;
        
        // spliting
        basinLabelSplited = basinLabel_arg.split(" ; ");
        
        // basic check
        if (basinLabelSplited.length < 2) return (-1);

        // getting code
        codeLabel = basinLabelSplited[1].replaceAll("Basin Code ", "").trim();
        if (codeLabel.contains("E")){
            codeLabel = codeLabel.split("E")[0];
        }
        returnCode = Long.parseLong(codeLabel);
        
        return(returnCode);
    }
    
    public static String getDatasetName(MetaRaster raster_arg){
        String[] dividedPoint;
        File internalFile;
        
        // basic check
        if(raster_arg == null) return (null);
        
        // get internal file and check it
        internalFile = raster_arg.getLocationMeta();
        if (internalFile == null) return (null);
        
        dividedPoint = internalFile.getName().split("\\.");
        return (dividedPoint[0]);
    }
    
    public static String getXandYhumanCoordinates(int[] xyValues_arg){
        // basic check
        if (xyValues_arg == null) return (null);
        
        return ("["+ xyValues_arg[0] +", "+ xyValues_arg[1] +"]");
    }
    
    public static String getXandYhumanCoordinatesFromID(int id_arg, 
                                                        MetaRaster metaRaster_arg){
        int[] xyValues;
        
        xyValues = SwmmCuencasLibrary.getXandYfromId(id_arg, metaRaster_arg);
        
        return(SwmmCuencasLibrary.getXandYhumanCoordinates(xyValues));
    }
    
    /**
     * Verify if given ID is inside given basin
     * @param idVerified_arg headID, contactID or tailID to be evaluated
     * @param basinMask_arg 
     * @param basinRaster_arg
     * @return TRUE if given point is inside basin, FALSE otherwise
     */
    public static boolean isIDinsideBasin(int idVerified_arg, 
                                          byte[][] basinMask_arg,
                                          MetaRaster basinRaster_arg){
        int[] xyValues;
        
        // basic check
        if(basinMask_arg == null) return (false);
        
        // getting X and Y values and basic checking it
        xyValues = SwmmCuencasLibrary.getXandYfromId(idVerified_arg, basinRaster_arg);
        if (xyValues == null) return (false);
        
        // primary check - matrix size
        if ( (xyValues[0] >= basinMask_arg.length) || 
             (xyValues[1] >= basinMask_arg[0].length) ){
            return (false);
        }
        
        // real check
        if(basinMask_arg[xyValues[0]][xyValues[1]] == 1) return (true);
        else return (false);
    }
    
    /**
     * Verify if given byte matrix is a valid flow directions data
     * @param dirMat_arg Byte matrix to be evaluated
     * @return TRUE if it is a valid flow direction matrix, FALSE otherwise
     */
    public static boolean isValidDirectionMatrix(byte[][] dirMat_arg){
        int count1, count2;
        if (dirMat_arg == null) return (false);
        
        for(count1 = 0; count1 < dirMat_arg.length; count1++){
            if (dirMat_arg[count1] != null){
                for(count2 = 0; count2 < dirMat_arg[count1].length; count2++){
                    if ( (dirMat_arg[count1][count2] > 10) || 
                         (dirMat_arg[count1][count2] < 0) ){
                        System.err.println("Invalid value '" + dirMat_arg[count1][count2] + "' in direction matrix.");
                        return (false);
                    }
                }
            } else {
                System.err.println("Null line in direction matrix.");
                return (false);
            }    
        }
        System.out.println("Valid direction matrix.");
        return (true);
    }
    
    /**
     * 
     * @param timeStamp_arg
     * @return String in "dd/MM/yyyy hh:mm:ss" format
     */
    public static String getHumanDate(long timeStamp_arg){
        java.util.Date dateTmp;
        
        dateTmp = new java.util.Date(timeStamp_arg);
        return(SwmmCuencasLibrary.getHumanDate(dateTmp));
    }
    
    /**
     * 
     * @param timeStamp_arg
     * @return String in "dd/MM/yyyy hh:mm:ss" format
     */
    public static String getHumanDate(java.util.Date date_arg){
        SimpleDateFormat formatter;
        String humanFormat;
        
        humanFormat = "dd/MM/yyyy HH:mm:ss";
        formatter = new SimpleDateFormat(humanFormat);
        
        TimeZone removeTZ = formatter.getTimeZone();
        
        formatter.setTimeZone(SwmmCuencasLibrary.defaultTimeZone);
        
        return(formatter.format(date_arg));
    }
    
    public static float[] breakIDtoLatLng(int ID_arg, MetaRaster metaData_arg){
        
        int[] breakedID;
            
        breakedID = SwmmCuencasLibrary.getXandYfromId(ID_arg, metaData_arg);
            
        // basic check
        if (breakedID == null) return (null);
        
        // 
        return (SwmmCuencasLibrary.getLatLngFromYX(breakedID[0],
                                                   breakedID[1],
                                                   metaData_arg));
    }
    
    public static float[] getLatLngFromYX(int y_arg, int x_arg, 
                                          MetaRaster metaData_arg){
        float[] returnPair;
        double demMinLon, demMinLat;
        double demResLon, demResLat;
        float lat, lon;
        
        // basic check
        if (metaData_arg == null) return (null);
        
        // getting extreme points
        demMinLon = metaData_arg.getMinLon();
        demMinLat = metaData_arg.getMinLat();
        demResLon = metaData_arg.getResLon();
        demResLat = metaData_arg.getResLat();
            
        // getting latitude and longitude informations
        lat=(float)(y_arg*demResLat/3600.+demMinLat+0.5*demResLat/3600.);
        lon=(float)(x_arg*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
        
        // setting return vector
        returnPair = new float[2];
        returnPair[0] = lat;
        returnPair[1] = lon;
        
        return(returnPair);
    }
    
    public static float[] getXYFromLatLng(double lat_arg, double lng_arg, 
                                          MetaRaster metaData_arg){
        float[] returnPair;
        double demMinLon, demMinLat;
        double demResLon, demResLat;
        float lat, lon;
        
        // basic check
        if (metaData_arg == null) return (null);
        
        // getting extreme points
        demMinLon = metaData_arg.getMinLon();
        demMinLat = metaData_arg.getMinLat();
        demResLon = metaData_arg.getResLon();
        demResLat = metaData_arg.getResLat();
            
        // getting latitude and longitude informations
        lat=(float)(lat_arg*demResLat/3600.+demMinLat+0.5*demResLat/3600.);
        lon=(float)(lng_arg*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
        
        // setting return vector
        returnPair = new float[2];
        returnPair[0] = lat;
        returnPair[1] = lon;
        
        return(returnPair);
    }
    
    /**
     * 
     * @param mask_arg
     * @return Positive number of '1' values in given mask if possible to count, (-1) otherwise
     */
    public static int countMaskPixels(byte[][] mask_arg){
        int currX, currY, countElements;
        
        // basic check
        if (mask_arg == null) return (-1);
        
        // all pixels iguals to 1 on maskAdded_arg are set to 1 on maskMaster_arg
        countElements = 0;
        for(currX = 0; currX < mask_arg.length; currX++){
            for(currY = 0; currY < mask_arg[0].length; currY++){
                if(mask_arg[currX][currY] == 1){
                    countElements++;
                }
            }
        }
        
        return (countElements);
    }
    
    public static int getIndexInTable(double[] table_arg, double value_arg){
        int count;
        
        if (table_arg == null) return (-1);
        
        for(count = 0; count < table_arg.length; count++){
            if (table_arg[count] == value_arg){
                return (count);
            } else if (count < (table_arg.length - 1)) {
                if ( (table_arg[count] < value_arg) && 
                     (table_arg[count + 1] > value_arg)){
                    // TODO - verify if next index is better
                    return (count);
                }
            }
        }
        
        //SwmmCuencasLibrary.getLatLngFromYX(count, count, null)
        
        return (table_arg.length - 1);
    }
}
