/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.swmmCoupling.objects;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;

/*
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.DirectoryFileFilter;
import org.apache.commons.io.filefilter.RegexFileFilter;
*/

/**
 *
 * @author A. D. L. Zanchetta
 */
public abstract class DatabaseFilesManager {
    
    // constants
    private static final String MDEM_EXTENTION = "metaDEM";
    private static final String RAIN_EXTENTION = "metaVHC";
    private static final String POLY_EXTENTION = "poly";
    private static final String SLOG_EXTENTION = "subLog";
    
    public static File[] loadAvailablePolygonFiles(File databaseDirectory_arg){
        String polygonDirectoryPath;
        File polygonDirectory;
        
        // get hydrological directory
        polygonDirectoryPath = DatabaseFilesManager.getPolygDirectoryPath(databaseDirectory_arg);
        if (polygonDirectoryPath == null) return (null);
        polygonDirectory = new File(polygonDirectoryPath);
        
        // read all available rain files
        return (DatabaseFilesManager.loadFilesInSubDirectoryRec(polygonDirectory, 
                                                                DatabaseFilesManager.POLY_EXTENTION));
    }
    
    /**
     * Get all meta rain files
     * @param databaseDirectory_arg
     * @return List of metaVHC files inside hydrological/Raster directory
     */
    public static File[] loadAvailableRainFiles(File databaseDirectory_arg){
        String hydrologicalDirectoryPath;
        File hydrologicalDirectory;
        
        // get hydrological directory
        hydrologicalDirectoryPath = DatabaseFilesManager.getHydroDirectoryPath(databaseDirectory_arg);
        if (hydrologicalDirectoryPath == null) return (null);
        hydrologicalDirectory = new File(hydrologicalDirectoryPath);
        
        // read all available rain files
        return (DatabaseFilesManager.loadFilesInSubDirectoryRec(hydrologicalDirectory, 
                                                                DatabaseFilesManager.RAIN_EXTENTION));
    }
    
    public static File[] loadAvailableMetaDEMFiles(File databaseDirectory_arg){
        String topologicalDirectoryPath;
        File topologicalDirectory;
        
        // get topological directory
        topologicalDirectoryPath = DatabaseFilesManager.getTopoDirectoryPath(databaseDirectory_arg);
        if (topologicalDirectoryPath == null) return (null);
        topologicalDirectory = new File(topologicalDirectoryPath);
        
        // read all available metaDEM files
        return (DatabaseFilesManager.loadFilesInSubDirectoryRec(topologicalDirectory, 
                                                                DatabaseFilesManager.MDEM_EXTENTION));
    }
    
    
    public static File loadAvailableRainFile(File databaseDirectory_arg,
                                             String fileNameStartWith_arg){
        File[] allAvailableRainFiles;
        String curFilename;
        int count;
        
        allAvailableRainFiles = DatabaseFilesManager.loadAvailableRainFiles(databaseDirectory_arg);
        if(allAvailableRainFiles == null) return (null);
        
        for(count=0; count < allAvailableRainFiles.length; count++){
            curFilename = allAvailableRainFiles[count].getName();
            if(curFilename.startsWith(fileNameStartWith_arg)) return (allAvailableRainFiles[count]);
        }
        
        return (null);
    }
    
    public static File loadAvailableSubBasinLogFile(File metaDemFile_arg){
        String metaDemFilePath, subLogFilePath;
        File subLogFileFile;
        
        if ((metaDemFile_arg == null) || (metaDemFile_arg.isDirectory())){
            return (null);
        }
        
        metaDemFilePath = metaDemFile_arg.getPath();
        subLogFilePath = metaDemFilePath.replace(DatabaseFilesManager.MDEM_EXTENTION, 
                                                 DatabaseFilesManager.SLOG_EXTENTION);
        
        subLogFileFile = new File(subLogFilePath);
        
        if (subLogFileFile.exists()){
            return (subLogFileFile);
        } else {
            return (null);
        }
    }
    
    public static String getPolygDirectoryPath(File databaseDirectory_arg){
        String returnPath;
        
        // basic check
        if (databaseDirectory_arg == null) return (null);
        
        // building path
        returnPath = databaseDirectory_arg.getPath();
        returnPath += File.separator + "Polygons" + File.separator;
        
        return (returnPath);
    }
    
    /**
     * Define directory path of hydrological data
     * @param databaseDirectory_arg Database directory
     * @return Path for directory if it was possible to determine, NULL otherwise
     */
    public static String getHydroDirectoryPath(File databaseDirectory_arg){
        String returnPath;
        
        // basic check
        if (databaseDirectory_arg == null) return (null);
        
        // building path
        returnPath = databaseDirectory_arg.getPath();
        returnPath += File.separator + "Rasters" + File.separator + "Hydrology" + File.separator;
        
        return (returnPath);
    }
    
    public static String getTopoDirectoryPath(File databaseDirectory_arg){
        String returnPath;
        
        // basic check
        if (databaseDirectory_arg == null) return (null);
        
        // building path
        returnPath = databaseDirectory_arg.getPath();
        returnPath += File.separator + "Rasters" + File.separator + "Topography" + File.separator;
        
        return (returnPath);
    }
    
    private File[] loadFilesInSubDirectoryRec(File databaseDirectory_arg,
                                              String subDirPath_arg,
                                              String fileExtention_arg){
        String subDirectoryFullPath;
        File subDirectory;
        Collection files;
        Iterator filesIt;
        File[] retVec;
        int count;
        
        // basic check
        if (databaseDirectory_arg == null) return (null);
        
        // get absolute path for directoy
        subDirectoryFullPath = databaseDirectory_arg.getPath() + subDirPath_arg;
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
    
    private static File[] loadFilesInSubDirectoryRec(File readDirectory_arg,
                                                     String fileExtention_arg){
        Collection files;
        Iterator filesIt;
        File[] retVec;
        int count;
        
        // list all internal files recursively
        files = FileUtils.listFiles(readDirectory_arg, 
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
}
