/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.swmmCoupling.objects;

import adlzanchetta.cuencasTools.cuencasCsvFileReader;
import adlzanchetta.cuencasTools.cuencasCsvFileInterpreter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.SimpleTimeZone;

/**
 * This class mix the content in a CUENCAS directory with a SWMM input file
 * @author A. D. L. Zanchetta
 */
public class OutletInlexMixer {
    
    public static void main(String[] args){
        String inputCuencasDirPath = "C:\\Users\\Worker\\Desktop\\deleteCuencas";
        String inputSwmmFilePath = "C:\\Users\\Worker\\Desktop\\gregorioX.inp";
        String outputSwmmFilePath = "C:\\Users\\Worker\\Desktop\\gregorioX2.inp";
        ArrayList<OutletInflowConnection> allConnections;
        
        File inputCuencasDir = new File(inputCuencasDirPath);
        File inputSwmmFile = new File(inputSwmmFilePath);
        File outputSwmmFile = new File(outputSwmmFilePath);
        
        allConnections = new ArrayList<OutletInflowConnection>();
        allConnections.add(new OutletInflowConnection(885, 401, "JP5", "TheInflow"));
        allConnections.add(new OutletInflowConnection(885, 491, "JP1", "TheSecInflow"));
        
        OutletInlexMixer.mix(allConnections, 
                             inputSwmmFile, 
                             inputCuencasDir,
                             outputSwmmFile,
                             false);
    }
    
    /**
     * 
     * @param allConnections_arg
     * @param inputSWMM_arg
     * @param inputCUENCASdir_arg
     * @param outputSWMM_arg
     * @return 
     */
    public static boolean mix(List<OutletInflowConnection> allConnections_arg,
                              File inputSWMM_arg, 
                              File inputCUENCASdir_arg, 
                              File outputSWMM_arg,
                              boolean runNotFoundBasins_arg){
        Iterator<OutletInflowConnection> allConnectionsIt;
        OutletInflowConnection curConnection;
        double[][] curResultMatrix;
        String newSwmmFileContent;
        File curOutputFile;
        
        // basic checks
        if (allConnections_arg == null) return (false);
        if (inputSWMM_arg == null) return (false);
        if (inputCUENCASdir_arg == null) return (false);
        if (outputSWMM_arg == null) return (false);
        
        // algorithmn:
        // 1 - for each connection
        // 1.1 - open respective output file
        // 1.2 - read output flow data
        // 2 - build output content
        // 3 - write output content
        
        // 1
        allConnectionsIt = allConnections_arg.iterator();
        while (allConnectionsIt.hasNext()){
            curConnection = allConnectionsIt.next();
            
            // 1.1
            curOutputFile = OutletInlexMixer.getOutputFile(curConnection.getX(), 
                                                           curConnection.getY(), 
                                                           inputCUENCASdir_arg);
            
            try{
                curResultMatrix = cuencasCsvFileReader.readCSVdischargeFile(curOutputFile);
                curConnection.setTimeSeriesTitle(curResultMatrix);
            } catch (FileNotFoundException exp) {
                System.err.printf("FileNotFoundException: " + exp.getMessage());
            } catch (IOException exp) {
                System.err.printf("IOException: " + exp.getMessage());
            }
        }
        
        try{
            // TODO - remove this test obj
            curConnection = allConnections_arg.get(0);
            
            // 2
            newSwmmFileContent = OutletInlexMixer.addContentToFile(inputSWMM_arg, 
                                                                   allConnections_arg);
            
            // 3
            OutletInlexMixer.writeNewInputFile(outputSWMM_arg, newSwmmFileContent);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }
        
        return (true);
    }
    
    /**
     * Find output file for given watershed
     * @param x_arg
     * @param y_arg
     * @param outputDir_arg Directory into which CUENCAS wrote its results
     * @return Reference to file found
     */
    private static File getOutputFile(int x_arg, int y_arg, File outputDir_arg){
        String curFileName;
        String regularExp;
        File currentFile;
        File[] allFiles;
        int count;
        
        // basic check
        if ((outputDir_arg == null) || (!outputDir_arg.isDirectory())) return (null);
        
        // regex must match: TEXT_NUMBER_NUMBER-TEXT.csv
        regularExp = ".*_"+x_arg+"_"+y_arg+"-.*";
        
        // check file by file if is correct one
        allFiles = outputDir_arg.listFiles();
        for(count = 0; count < allFiles.length; count++){
            currentFile = allFiles[count];
            curFileName = currentFile.getName();
            if (curFileName.matches(regularExp)){
                return (currentFile);
            }
        }
        
        // file not found, return NULL
        return (null);
    }
    
    /**
     * Create content of new file containing all elements of a original INP file with additional inlet flow data
     * @param swmmInput_arg Original INP file
     * @param allCon_arg All data to be added to file
     * @return Content to be write at the new INP file
     * @throws IOException 
     */
    private static String addContentToFile(File swmmInput_arg, 
                                           List<OutletInflowConnection> allCon_arg) throws IOException{
        boolean flagInflow, flagTimeserie, alreadAdded;
        String line, allFileContent;
        boolean[] conAlreadyPrinted;
        double[][] timeSerieData;
        BufferedReader br;
        FileReader fr;
        int countData;
        
        // 0 - set initial value for variables
        // 1 - open file reader
        // 2 - read line by line
        // 2.1 - if line starts TIMESERIES, add new TIMESERIE
        // 2.2 - if line starts INFLOWS, add new INFLOW reference
        
        // 0
        flagInflow = false;
        flagTimeserie = false;
        conAlreadyPrinted = new boolean[allCon_arg.size()];
        for(countData = 0; countData < conAlreadyPrinted.length; countData++){
            conAlreadyPrinted[countData] = false;
        }
        
        // 1
        fr = new FileReader(swmmInput_arg);
        br = new BufferedReader(fr);
        
        // 2
        allFileContent = "";
        while ((line = br.readLine()) != null) {
            
            alreadAdded = false;
            
            // add inflow indication
            // 2.1
            if (flagInflow){
                String lineUpdated;
                lineUpdated = OutletInlexMixer.updateNodeLine(line, 
                                                              allCon_arg,
                                                              conAlreadyPrinted);
                allFileContent += lineUpdated;
                alreadAdded = true;
            }
            if (line.contains("[INFLOWS]")) {
                flagInflow = true;
            } else if(line.trim().matches("\\[.*\\]")) {
                flagInflow = false;
            }
            
            // add timeserie data
            // 2.2
            if (flagTimeserie && (!line.trim().startsWith(";;"))){
                allFileContent += OutletInlexMixer.updateInflowData(allCon_arg);
                flagTimeserie = false;
            }
            if (line.contains("[TIMESERIES]")){
                flagTimeserie = true;
            } else if(line.matches("\\[.*\\]")) {
                flagTimeserie = false;
            }
            
            if (!alreadAdded){
                allFileContent += line + '\n';
            }
        }
        br.close();
        
        return (allFileContent);
    }
    
    /**
     * Create new [INFLOWS] line with connection to Timeserie to substitute proposed one
     * @param theLine_arg Current inflows line
     * @param allCon_arg Data of all connections
     * @return A new line with proper connections
     */
    private static String updateNodeLine(String theLine_arg,
                                         List<OutletInflowConnection> allCon_arg,
                                         boolean[] allConPrin_arg){
        Iterator<OutletInflowConnection> allConIt;
        OutletInflowConnection currentConnection;
        String newLine;
        int count;
        
        count = 0;
        
        if (!theLine_arg.trim().equalsIgnoreCase("")){
            // if line is not empty, try to substring it
            allConIt = allCon_arg.iterator();
            while(allConIt.hasNext()){
                currentConnection = allConIt.next();
                if (theLine_arg.trim().startsWith(currentConnection.getNodeId() + " ")){
                    newLine = currentConnection.getNodeId() + "   FLOW   ";
                    newLine += currentConnection.getTimeSeriesTitle();
                    newLine += "   FLOW   1.0   1.0   1" + '\n';
                    allConPrin_arg[count] = true;
                    return (newLine);
                }
                count++;
            }
        } else {
            // if line is empty, print all still not printed
            for(count = 0; count < allConPrin_arg.length; count++){
                if(allConPrin_arg[count] == false){
                    currentConnection = allCon_arg.get(count);
                    newLine = currentConnection.getNodeId() + "   FLOW   ";
                    newLine += currentConnection.getTimeSeriesTitle();
                    newLine += "   FLOW   1.0   1.0   1" + '\n';
                    allConPrin_arg[count] = true;
                    return (newLine);
                }
            }
        }
        
        return (theLine_arg + '\n');
    }
    
    private static String updateInflowData(List<OutletInflowConnection> allCon_arg){
        Iterator<OutletInflowConnection> curConIt;
        OutletInflowConnection curCon;
        String lastDate, lastTime;
        double[][] timeSerieData;
        String dateStr, hourStr;
        String retString;
        int countData;
        
        retString = "";
        curConIt = allCon_arg.iterator();
        while(curConIt.hasNext()){
            curCon = curConIt.next();
            timeSerieData = curCon.getTimeSeriesData();
            if (timeSerieData != null){
                lastDate = "";
                lastTime = "";
                for(countData = 0; countData < timeSerieData.length; countData++) {
                    
                    dateStr = cuencasCsvFileInterpreter.getDateFromTime((float)timeSerieData[countData][0]);
                    
                    // TODO beg - remove check
                    float timeStampMs = (float)timeSerieData[countData][0] * 60 * 1000;
                    System.out.println("Date (min): " + (float)timeSerieData[countData][0]);
                    System.out.println("Date (sec): " + timeStampMs);
                    Date newDate = new Date();
                    newDate.setTime((long)timeStampMs);
                    DateFormat theDateFormat;
                    newDate.toGMTString();
                    theDateFormat = DateFormat.getInstance();
                    theDateFormat.getTimeZone();
                    System.out.println("Pure: " + newDate);
                    System.out.println("GMT : " + newDate.toGMTString());
                    java.text.SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    java.util.Calendar cal = Calendar.getInstance(new SimpleTimeZone(0, "GMT"));
                    format.setCalendar(cal);
                    System.out.println("GMT formated : " + format.format(newDate));
                    // TODO end - remove check
                    
                    hourStr = cuencasCsvFileInterpreter.getTimeFromTime((float)timeSerieData[countData][0]);
                    if ((lastDate.trim().equalsIgnoreCase(dateStr.trim())) && 
                            lastTime.trim().equalsIgnoreCase(hourStr.trim())){
                        continue;
                    }
                    
                    lastDate = dateStr;
                    lastTime = hourStr;
                    
                    retString += curCon.getTimeSeriesTitle() + "   ";
                    retString += dateStr + "   ";
                    retString += hourStr + "   ";
                    retString += timeSerieData[countData][1] + "   ";
                    retString += '\n';
                }
                retString += '\n';
            }
        }
        return(retString);
    }
    
    /**
     * 
     * @param file_arg
     * @param content_arg
     * @return
     * @throws FileNotFoundException 
     */
    private static boolean writeNewInputFile(File file_arg, String content_arg) throws IOException{
        PrintWriter writer;
        
        // basic check
        if (file_arg == null) return (false);
        
        if (!file_arg.exists()){
            file_arg.createNewFile();
        }
        
        try{
            writer = new PrintWriter(file_arg, "UTF-8");
            writer.print(content_arg);
            writer.close();
        } catch (UnsupportedEncodingException exp) {
            System.err.println("UnsupportedEncodingException: " + exp.getMessage());
            return false;
        } catch (FileNotFoundException exp) {
            System.err.println("FileNotFoundException: " + exp.getMessage());
            return false;
        }
        
        return (true);
    }
}
