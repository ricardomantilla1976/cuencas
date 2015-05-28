package hydroScalingAPI.io.swmmCoupling;

import hydroScalingAPI.io.MetaRaster;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class for writing raster-hydrological files (.vhc and .metaVHC file formats)
 * @author A. D. L. Zanchetta
 */
public class PrecipitationWriter implements Runnable{
    public static final int PIXEL_SINGLEPIXEL = 1;
    public static final int PIXEL_MULTIPIXEL = 2;
    
    public static final int INTERVAL_SEC = 1;
    public static final int INTERVAL_MIN = 2;
    public static final int INTERVAL_HOUR = 3;
    public static final int INTERVAL_DAY = 4;
    public static final int INTERVAL_MONTH = 5;
    public static final int INTERVAL_YEAR = 6;
    
    public static final String INTERVAL_SEC_DESC = "Seconds";
    public static final String INTERVAL_MIN_DESC = "Minutes";
    public static final String INTERVAL_HOUR_DESC = "Hours";
    public static final String INTERVAL_DAY_DESC = "Days";
    public static final String INTERVAL_MONTH_DESC = "Months";
    public static final String INTERVAL_YEAR_DESC = "Years";
    
    public static final String defaultInputDateFormat = "";
    public static final String defaultPrecipitationTitle = "precipitation";
    
    private static final String FILENAME_SEC = "HHmmss.dd.MM.yyyy";
    private static final String FILENAME_MIN = "HHmmss.dd.MM.yyyy";
    private static final String FILENAME_HOUR = "HHmmss.dd.MM.yyyy";
    private static final String FILENAME_DAY = "dd.MM.yyyy";
    private static final String FILENAME_MONTH = "MM.yyyy";
    private static final String FILENAME_YEAR = "yyyy";
    private static final String FILENAME_EXTENTION = "vhc";
    
    private int numCol, numRow;              // dimentions of source MetaRaster
    private int intervalLabel;               // if interval value is given in days, hours...
    private int intervalValue;               // numeric value of interval
    private String dateInputFormat;          //
    private String precipitationTitle;       //
    private String datasetRootDirectory;     //
    private int pixelDiscretization = 1;     //
    
    //private String[][] timeSequence_forThread;
    //private MetaRaster metaDEM_forThread;
    private ProgressListener ownListener;

    
    /**
     * Get all time interval unit options
     * @return All time interval unit options
     */
    public static TimeIntervalOption[] getAllTimeIntervalOptions(){
        TimeIntervalOption[] returnVector;
        
        returnVector = new TimeIntervalOption[6];
        
        returnVector[0] = new TimeIntervalOption(PrecipitationWriter.INTERVAL_SEC, PrecipitationWriter.INTERVAL_SEC_DESC);
        returnVector[1] = new TimeIntervalOption(PrecipitationWriter.INTERVAL_MIN, PrecipitationWriter.INTERVAL_MIN_DESC);
        returnVector[2] = new TimeIntervalOption(PrecipitationWriter.INTERVAL_HOUR, PrecipitationWriter.INTERVAL_HOUR_DESC);
        returnVector[3] = new TimeIntervalOption(PrecipitationWriter.INTERVAL_DAY, PrecipitationWriter.INTERVAL_DAY_DESC);
        returnVector[4] = new TimeIntervalOption(PrecipitationWriter.INTERVAL_MONTH, PrecipitationWriter.INTERVAL_MONTH_DESC);
        returnVector[5] = new TimeIntervalOption(PrecipitationWriter.INTERVAL_YEAR, PrecipitationWriter.INTERVAL_YEAR_DESC);
        
        return (returnVector);
    }
    
    /**
     * Get English human-understandable description for a given time interval numeric label
     * @param intervalLabel_arg Time interval numeric label
     * @return English human-understandable description. For instance: "seconds"
     */
    public static String getTimeIntervalDescription(int intervalLabel_arg){
        switch (intervalLabel_arg){
            case PrecipitationWriter.INTERVAL_SEC:
                return (PrecipitationWriter.INTERVAL_SEC_DESC);
            case PrecipitationWriter.INTERVAL_MIN:
                return (PrecipitationWriter.INTERVAL_MIN_DESC);
            case PrecipitationWriter.INTERVAL_HOUR:
                return (PrecipitationWriter.INTERVAL_HOUR_DESC);
            case PrecipitationWriter.INTERVAL_DAY:
                return (PrecipitationWriter.INTERVAL_DAY_DESC);
            case PrecipitationWriter.INTERVAL_MONTH:
                return (PrecipitationWriter.INTERVAL_MONTH_DESC);
            case PrecipitationWriter.INTERVAL_YEAR:
                return (PrecipitationWriter.INTERVAL_YEAR_DESC);
            default:
                return (null);
        }
    }
    
    /**
     * Object builder
     * @param relatedMetaDEM_arg MetaRaster data containing target matrix to which will be imported the rain
     */
    public PrecipitationWriter(MetaRaster relatedMetaDEM_arg){
        this(relatedMetaDEM_arg.getNumCols(), relatedMetaDEM_arg.getNumRows());
        this.setDatasetRootDirectory(relatedMetaDEM_arg);
    }
    
    /**
     * Object builder
     * @param numCols_arg Number of columns of data matrix to which rain will be imported
     * @param numRows_arg Number of rows of data matrix to which rain will be imported
     */
    public PrecipitationWriter(int numCols_arg, int numRows_arg){
        this.numCol = numCols_arg;
        this.numRow = numRows_arg;
        this.precipitationTitle = PrecipitationWriter.defaultPrecipitationTitle;
        this.datasetRootDirectory = null;
        this.ownListener = null;
    }
    
    /**
     * 
     * @param newPixelInterval_arg Positive new pixel data interval
     * @return TRUE if given pixel interval value is valid and proper attribute was modified, FALSE otherwise
     */
    public boolean setPixelInterval(int newPixelInterval_arg){
        
        // basic check: pixel interval must be positive
        if (newPixelInterval_arg <= 0) return false;
        
        this.pixelDiscretization = newPixelInterval_arg;
        return true;
    }
    
    /**
     * Define constant time interval in which rain matrixes will be generated
     * @param intervalLabel_arg Time interval label
     * @param intervalValue_arg Time interval value
     * @return TRUE if given parameters are valid and proper attributes were changed, FALSE otherwise
     */
    public boolean setDataInterval(TimeIntervalOption intervalLabel_arg, 
                                   int intervalValue_arg){
        // basic check
        if (intervalLabel_arg == null) return (false);
        if (intervalValue_arg <= 0) return (false);
        
        return (this.setDataInterval(intervalLabel_arg.getTimeIntervalLabel(), 
                                     intervalValue_arg));
    }
    
    /**
     * 
     * @param intervalLabel_arg Must be PrecipitationWriter.INTERVAL_SEC, PrecipitationWriter.INTERVAL_MIN, PrecipitationWriter.INTERVAL_HOUR or PrecipitationWriter.INTERVAL_DAY
     * @param intervalValue_arg 
     * @return TRUE if it was possible to set, FALSE otherwise 
     */
    public boolean setDataInterval(int intervalLabel_arg, int intervalValue_arg){
        if ( (intervalLabel_arg >= PrecipitationWriter.INTERVAL_SEC) && 
             (intervalLabel_arg <= PrecipitationWriter.INTERVAL_YEAR)){
            this.intervalLabel = intervalLabel_arg;
            this.intervalValue = intervalValue_arg;
            return (true);
        } else {
            return (false);
        }
    }
    
    /**
     * 
     * @param precTitle_arg New precipitation name
     * @return TRUE if new precipitation name is valid, FALSE otherwise
     */
    public boolean setPrecipitationTitle(String precTitle_arg){
        
        // basic check
        if ((precTitle_arg == null) || (precTitle_arg.trim().length() == 0)){
            return (false);
        }
        
        this.precipitationTitle = precTitle_arg;
        
        return (true);
    }
    
    /**
     * Define date format used
     * @param inputDateFormat_arg A String similar to PrecipitationWriter.defaultInputDateFormat
     */
    public void setInputDateFormat(String inputDateFormat_arg){
        this.dateInputFormat = inputDateFormat_arg;
    }

    /**
     * Update internal attribute from a MetaRaster containing data
     * @param metaRaster_arg MetaRaster to which will be generated rain files
     */
    public final void setDatasetRootDirectory(MetaRaster metaRaster_arg) {
        String metaRasterPath;
        File metaRasterFile;
        String pathDivisor;
        
        // basic check
        if(metaRaster_arg == null) return;
        
        // get file path
        metaRasterFile = metaRaster_arg.getLocationMeta();
        metaRasterPath = metaRasterFile.getAbsolutePath();
        
        // remove specific part of path
        pathDivisor = "Rasters";
        this.datasetRootDirectory = metaRasterPath.split(pathDivisor)[0];
    }
    
    /**
     * Set dataset root directory value for internal attribute
     * @param datasetRootDirectory New attribute for dataset directory path
     */
    public void setDatasetRootDirectory(String datasetRootDirectory) {
        this.datasetRootDirectory = datasetRootDirectory;
    }
    
    /**
     * Set object to be the proper listener, usually containing a Progress Bar
     * @param ownListener 
     */
    public void setOwnListener(ProgressListener ownListener) {
        this.ownListener = ownListener;
    }
    
    /**
     * Write one .metaVHC file and N .vhc files, where N is the number of registers
     * @param timeSequence_arg A matrix of size [n][2], where 'n' is the number of elements and [n][0] is the date and [n][1] is the respective rain data
     * @param metaDEM_arg A MetaRaster of selected dataset
     * @return TRUE if creation was possible, FALSE otherwise
     */
    public boolean writeTimeSequence(String[][] timeSequence_arg, 
                                     MetaRaster metaDEM_arg){
        boolean allSuccess, currentSuccess;
        String floatFormatted;
        String metaFilePath;
        int countTimes;
        File metaFile;
                
        // basic check
        if (timeSequence_arg == null) return (false);
        
        // write MetaRaster file
        metaFilePath = this.defineDataMetaFilePath();
        metaFile = new File(metaFilePath);
        if (!this.writeFileMetaVHC(metaFile, metaDEM_arg)){
            return (false);
        }
        
        // set beggining of listener, if it exists
        if (this.ownListener != null) {
            this.ownListener.onCreate(0, timeSequence_arg.length - 1);
        }
        
        // write a file for each register
        allSuccess = true;
        for(countTimes = 0; countTimes < timeSequence_arg.length; countTimes++){
            // notify listener, if it exists
            if (this.ownListener != null){
                this.ownListener.onChangeTo(countTimes);
            }
            
            // transform comma-separated for dot-separated numbers
            floatFormatted = timeSequence_arg[countTimes][1].replace(',', '.');
            
            // write in file and update return flag
            currentSuccess = this.writeFile(timeSequence_arg[countTimes][0], 
                                            Float.parseFloat(floatFormatted));
            allSuccess = (allSuccess && currentSuccess);
        }
        
        // respond to listener, if it has
        if (this.ownListener != null){
            this.ownListener.onFinish(allSuccess);
        }
        
        return (allSuccess);
    }
    
    private String defineDataFileDirectoryPath(){
        String returnPath;
        
        // basic checks
        if (this.datasetRootDirectory == null) return (null);
        
        // build entire file path
        if (this.datasetRootDirectory.endsWith(File.separator)){
            returnPath = this.datasetRootDirectory;
        } else {
            returnPath = this.datasetRootDirectory + File.separator;
        }
        returnPath += "Rasters" + File.separator;
        returnPath += "Hydrology" + File.separator;
        returnPath += "storms" + File.separator;
        returnPath += this.precipitationTitle + File.separator;
        
        return(returnPath);
    }
    
    /**
     * Define entire file path for a .vhc file
     * @param fileDate_arg
     * @return Entire file path for new file
     */
    private String defineDataFilePath(String fileDate_arg){
        String returnPath;
        
        returnPath = this.defineDataFileDirectoryPath();
        returnPath += this.defineDataFileName(fileDate_arg);
        
        // 
        return(returnPath);
    }
    
    private String defineDataMetaFilePath(){
        String returnPath;
        
        returnPath = this.defineDataFileDirectoryPath();
        returnPath += this.precipitationTitle + ".metaVHC";
        
        // 
        return(returnPath);
    }
    
    /**
     * Define a .vhc file name according to its date
     * @param fileDate_arg Date in input format defined by internal variable
     * @return 
     */
    private String defineDataFileName(String fileDate_arg){
        SimpleDateFormat dateFormatterOutput;
        SimpleDateFormat dateFormatterInput;
        String fileName;
        Date dataDate;
        
        // basic check
        if (this.dateInputFormat == null) return (null);
        
        // create input date reader and try to read date
        dateFormatterInput = new SimpleDateFormat(this.dateInputFormat);
        try{
            dataDate = dateFormatterInput.parse(fileDate_arg);
        } catch (ParseException exp) {
            System.err.println("ParseException: " + exp.getMessage());
            return (null);
        }
        
        // create output date definer
        switch (this.intervalLabel){
            case (PrecipitationWriter.INTERVAL_SEC):
                dateFormatterOutput = new SimpleDateFormat(PrecipitationWriter.FILENAME_SEC);
                break;
            case (PrecipitationWriter.INTERVAL_MIN):
                dateFormatterOutput = new SimpleDateFormat(PrecipitationWriter.FILENAME_MIN);
                break;
            case (PrecipitationWriter.INTERVAL_HOUR):
                dateFormatterOutput = new SimpleDateFormat(PrecipitationWriter.FILENAME_HOUR);
                break;
            case (PrecipitationWriter.INTERVAL_DAY):
                dateFormatterOutput = new SimpleDateFormat(PrecipitationWriter.FILENAME_DAY);
                break;
            case (PrecipitationWriter.INTERVAL_MONTH):
                dateFormatterOutput = new SimpleDateFormat(PrecipitationWriter.FILENAME_MONTH);
                break;
            case (PrecipitationWriter.INTERVAL_YEAR):
                dateFormatterOutput = new SimpleDateFormat(PrecipitationWriter.FILENAME_YEAR);
                break;
            default:
                return (null);
        }
        
        // create filename
        fileName = this.precipitationTitle + ".";
        fileName += dateFormatterOutput.format(dataDate) + ".";
        fileName += FILENAME_EXTENTION;
        
        // change numeric month for month name
        fileName = PrecipitationWriterAux.adaptToWordedMonth(fileName);
        
        return(fileName);
    }
    
    private boolean writeFileMetaVHC(File fileDestiny_arg, MetaRaster metaRaster_arg) {
        //DataOutputStream outputRasterBuffer;
        PrintWriter outputRasterBuffer;
        String intervalDescription, timeResolution;
        File parentFile;
        
        // basic check
        if (fileDestiny_arg == null) return (false);
        if (metaRaster_arg == null) return (false);
        
        // remove previous file if necessary
        if (fileDestiny_arg.exists()) fileDestiny_arg.delete();
        
        // prepare time resolution variable
        intervalDescription = PrecipitationWriter.getTimeIntervalDescription(this.intervalLabel);
        if (intervalDescription == null){
            intervalDescription = "";
        } else {
            intervalDescription = intervalDescription.toLowerCase();
        }
        timeResolution = this.intervalValue + "-" + intervalDescription;
        
        // create parent directory estructure if necessary
        parentFile = fileDestiny_arg.getParentFile();
        if (!parentFile.exists()){
            if (!parentFile.mkdirs()){
                System.err.println("Unable to create file structures.");
                return (false);
            }
        }
        
        // create file and open file writer
        try{
            if (!fileDestiny_arg.createNewFile()){
                return (false);
            }
            //outputRasterBuffer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(fileDestiny_arg)));
            outputRasterBuffer = new PrintWriter(new FileWriter(fileDestiny_arg));
        } catch (FileNotFoundException exp) {
            System.err.println("FileNotFoundException: " + exp.getMessage());
            return (false);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }
        
        // write infos
        //try{
            /*
            outputRasterBuffer.writeChars("[Name]" + '\n');
            outputRasterBuffer.writeChars("Precipitation Radar Data From KICT" + '\n');
            outputRasterBuffer.writeChars("[Southernmost Latitude]" + '\n');
            outputRasterBuffer.writeChars(metaRaster_arg.getProperty("[Southernmost Latitude]") + '\n');
            outputRasterBuffer.writeChars("[Westernmost Longitude]" + '\n');
            outputRasterBuffer.writeChars(metaRaster_arg.getProperty("[Westernmost Longitude]") + '\n');
            outputRasterBuffer.writeChars("[Longitudinal Resolution (ArcSec)]" + '\n');
            outputRasterBuffer.writeChars(Double.toString(metaRaster_arg.getResLon()) + '\n');
            outputRasterBuffer.writeChars("[Latitudinal Resolution (ArcSec)]" + '\n');
            outputRasterBuffer.writeChars(Double.toString(metaRaster_arg.getResLat()) + '\n');
            outputRasterBuffer.writeChars("[# Columns]" + '\n');
            outputRasterBuffer.writeChars(Integer.toString(metaRaster_arg.getNumCols()) + '\n');
            outputRasterBuffer.writeChars("[# Rows]" + '\n');
            outputRasterBuffer.writeChars(Integer.toString(metaRaster_arg.getNumRows()) + '\n');
            outputRasterBuffer.writeChars("[Format]" + '\n');
            outputRasterBuffer.writeChars("Float" + '\n');
            outputRasterBuffer.writeChars("[Missing]" + '\n');
            outputRasterBuffer.writeChars("-99" + '\n');
            outputRasterBuffer.writeChars("[Temporal Resolution]" + '\n');
            outputRasterBuffer.writeChars(timeResolution + '\n');
            outputRasterBuffer.writeChars("[Units]" + '\n');
            outputRasterBuffer.writeChars("mm/h" + '\n');
            outputRasterBuffer.writeChars("[Information]" + '\n');
            outputRasterBuffer.writeChars("Precipitation data from the KICT radar near Wichita, KS");
            */
            
            outputRasterBuffer.println("[Name]");
            outputRasterBuffer.println("Precipitation Radar Data From KICT");
            outputRasterBuffer.println("[Southernmost Latitude]");
            outputRasterBuffer.println(metaRaster_arg.getProperty("[Southernmost Latitude]"));
            outputRasterBuffer.println("[Westernmost Longitude]");
            outputRasterBuffer.println(metaRaster_arg.getProperty("[Westernmost Longitude]"));
            if (this.pixelDiscretization == PrecipitationWriter.PIXEL_MULTIPIXEL){
                outputRasterBuffer.println("[Longitudinal Resolution (ArcSec)]");
                outputRasterBuffer.println(Double.toString(metaRaster_arg.getResLon()));
                outputRasterBuffer.println("[Latitudinal Resolution (ArcSec)]");
                outputRasterBuffer.println(Double.toString(metaRaster_arg.getResLat()));
                outputRasterBuffer.println("[# Columns]");
                outputRasterBuffer.println(Integer.toString(metaRaster_arg.getNumCols()));
                outputRasterBuffer.println("[# Rows]");
                outputRasterBuffer.println(Integer.toString(metaRaster_arg.getNumRows()));
            } else if (this.pixelDiscretization == PrecipitationWriter.PIXEL_SINGLEPIXEL) {
                outputRasterBuffer.println("[Longitudinal Resolution (ArcSec)]");
                outputRasterBuffer.println(Double.toString(metaRaster_arg.getResLon() * metaRaster_arg.getNumCols() * 2));
                outputRasterBuffer.println("[Latitudinal Resolution (ArcSec)]");
                outputRasterBuffer.println(Double.toString(metaRaster_arg.getResLat() * metaRaster_arg.getNumRows() * 2));
                outputRasterBuffer.println("[# Columns]");
                outputRasterBuffer.println(Integer.toString(1));
                outputRasterBuffer.println("[# Rows]");
                outputRasterBuffer.println(Integer.toString(1));
            }
            outputRasterBuffer.println("[Format]");
            outputRasterBuffer.println("Float");
            outputRasterBuffer.println("[Missing]");
            outputRasterBuffer.println("-99");
            outputRasterBuffer.println("[Temporal Resolution]");
            outputRasterBuffer.println(timeResolution);
            outputRasterBuffer.println("[Units]");
            outputRasterBuffer.println("mm/h");
            outputRasterBuffer.println("[Information]");
            outputRasterBuffer.println("Precipitation data from the KICT radar near Wichita, KS");
            
        //} catch (IOException exp) {
        //    System.err.println("IOException: " + exp.getMessage());
        //    return (false);
        //}
        
        //try{
            outputRasterBuffer.flush();
            outputRasterBuffer.close();
        //} catch (IOException exp) {
        //    System.err.println("IOException: " + exp.getMessage());
        //}
        
        return (true);
    }
    
    /**
     * Create a single spatially uniform precipitation file
     * @param time_arg Time of rain data (in 'original-brute of CSV file' format)
     * @param precipitation_arg Rain intensity (in mm)
     * @return TRUE if it was possible to create and write file, FALSE otherwise
     */
    private boolean writeFile(String time_arg, float precipitation_arg){
        DataOutputStream outputRasterBuffer;
        int countRows, countCols;
        float precipitationRate;             // value in mm/hr
        String outputFilePath;
        File outputFile;
        File parentFile;
        
        // define output file
        outputFilePath = this.defineDataFilePath(time_arg);
        outputFile = new File(outputFilePath);
        
        // remove previous file if it exists
        if (outputFile.exists()) outputFile.delete();
        
        // create parent directory estructure if necessary
        parentFile = outputFile.getParentFile();
        if (!parentFile.exists()){
            if (!parentFile.mkdirs()){
                System.err.println("Unable to create file structures.");
                return (false);
            }
        }
        
        // try to create the output file
        try{
            if (!outputFile.createNewFile()) return (false);
        } catch(IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }
        
        // open file writer
        try{
            outputRasterBuffer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
        } catch (FileNotFoundException exp){
            System.err.println("FileNotFoundException: " + exp.getMessage());
            return (false);
        }
        
        // convert accumulated precipitation (mm) to precipitation rate (mm/h)
        if (this.intervalLabel == PrecipitationWriter.INTERVAL_HOUR){
            System.out.println();
            precipitationRate = precipitation_arg;
        } else if (this.intervalLabel == PrecipitationWriter.INTERVAL_MIN) {
            System.out.println();
            precipitationRate = ((precipitation_arg * 60)/this.intervalValue); 
        } else {
            // TODO - make it properly
            System.out.println();
            precipitationRate = precipitation_arg;
        }
        
        // write in file
        try{
            if (this.pixelDiscretization == PrecipitationWriter.PIXEL_MULTIPIXEL){
                for (countRows = 0; countRows < this.numRow; countRows++) {
                    for (countCols = 0; countCols < this.numCol; countCols++) {
                        outputRasterBuffer.writeFloat(precipitationRate);
                    }
                }
            } else if (this.pixelDiscretization == PrecipitationWriter.PIXEL_SINGLEPIXEL) {
                outputRasterBuffer.writeFloat(precipitationRate);
            }
        } catch (IOException exp) {
            System.err.println("IOException:" + exp.getMessage());
            return (false);
        }
        
        // close file writer
        try{
            outputRasterBuffer.flush();
            outputRasterBuffer.close();
        } catch (IOException exp) {
            System.err.println("IOException:" + exp.getMessage());
        }
        
        return (true);
    }

    public void run() {
        //this.writeTimeSequence(this.timeSequence_forThread, 
        //                       this.metaDEM_forThread);
    }
}
