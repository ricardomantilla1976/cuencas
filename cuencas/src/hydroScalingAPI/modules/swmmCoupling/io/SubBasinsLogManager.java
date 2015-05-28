/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.swmmCoupling.io;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Used to manage with SubBasin log files
 * @author A. D. L. Zanchetta
 */
public class SubBasinsLogManager{
    private ArrayList<HashMap> allSubBasins;
    private File logFile;
    
    /**
     * Create a log manager object already related to a proper log file
     * @param logFile_arg Already existing sub basin log file with .sublog extention
     * @throws IOException When it was not possible to open given log file
     */
    public SubBasinsLogManager(File logFile_arg) throws IOException{
        BufferedReader buffReader;
        HashMap currentSubBasin;
        String fullLine;
        
        // basic check: file must have .subLog to be opened
        if (logFile_arg == null) throw (new IOException());
        if (!logFile_arg.getName().trim().endsWith("subLog")){
            throw (new IOException());
        }
        
        this.logFile = logFile_arg;
        
        // 
        buffReader = new BufferedReader(new FileReader(logFile_arg));
        this.allSubBasins = new ArrayList<HashMap>();
        
        //
        fullLine = buffReader.readLine();
        while (fullLine != null) {
            currentSubBasin = SubBasinsLogManager.parseLine(fullLine);
            this.allSubBasins.add(currentSubBasin);
            fullLine=buffReader.readLine();
        }
        
        buffReader.close();
    }
    
    // line structure:
    //  x:000, y:000; BasinCode:000; xIn: 000, yIn: 000; xIn: 000, yIn: 000
    /**
     * 
     * @param subBasinLine_arg
     * @return HashMap with 'x', 'y', 'Basin Code', 'xIn_1', 'yIn_1', `'xIn_2'...
     */
    public static HashMap parseLine(String subBasinLine_arg){
        int countSplit1, countSplit2, countInlets, countAddeds;
        String[] splited1, splited2;
        HashMap returnHash;
        
        // basic check
        if (subBasinLine_arg == null) return (null);
        
        returnHash = new HashMap();
        
        //
        countInlets = 1;
        splited1 = subBasinLine_arg.split(";");
        for(countSplit1 = 0; countSplit1 < splited1.length; countSplit1++){
            if (splited1[countSplit1].contains("x:")){
                splited2 = splited1[countSplit1].trim().split(",|:");
                for(countSplit2 = 0; countSplit2 < splited2.length; countSplit2++){
                    if (splited2[countSplit2].trim().equalsIgnoreCase("x")){
                        returnHash.put("x", Integer.parseInt(splited2[countSplit2 + 1].trim()));
                        returnHash.put("y", Integer.parseInt(splited2[countSplit2 + 3].trim()));
                    }
                }
            } else if (splited1[countSplit1].contains("Basin Code")) {
                splited2 = splited1[countSplit1].trim().split(" ");
                returnHash.put("Basin Code", splited2[2]);
            } else if (splited1[countSplit1].contains("xIn_")) {
                splited2 = splited1[countSplit1].trim().split(",|:");
                for(countSplit2 = 0; countSplit2 < splited2.length; countSplit2++){
                    if (splited2[countSplit2].trim().contains("xIn")){
                        returnHash.put("xIn_" + countInlets, 
                                       Integer.parseInt(splited2[countSplit2 + 1].trim()));
                        returnHash.put("yIn_" + countInlets, 
                                       Integer.parseInt(splited2[countSplit2 + 3].trim()));
                        countInlets ++;
                    }
                }
            }
        }
        
        return (returnHash);
    }
    
    /**
     * Transform Hashed data in a String object
     * @param subBasinHash_arg
     * @return String object of given Hashed data, in format; <br />
     *      x:000, y:000; BasinCode:000; xIn: 000, yIn: 000; xIn: 000, yIn: 000
     */
    public static String parseHash(HashMap subBasinHash_arg){
        Integer currentX, currentY;
        String returnString;
        int countInlet;
        
        // basic check
        if (subBasinHash_arg == null) return (null);
        
        returnString = "";
        
        // add outlet point data
        returnString += "x:" + subBasinHash_arg.get("x") + " , ";
        returnString += "y:" + subBasinHash_arg.get("y") + " ; ";
        
        // add Basin Code
        returnString += " Basin Code " + 0 + " ; ";
        
        // add each inlet point
        countInlet = 1;
        while(true){
            // get current inlet
            currentX = (Integer)subBasinHash_arg.get("xIn_" + countInlet);
            currentY = (Integer)subBasinHash_arg.get("yIn_" + countInlet);
            
            if ((currentX == null) || (currentY == null)){
                break;
            } else {
                if (countInlet > 1) returnString += ", ";
                returnString += "xIn_" + countInlet + ": " + currentX + ", ";
                returnString += "yIn_" + countInlet + ": " + currentY;
            }
            
            countInlet++;
        }
        
        return (returnString);
    }
    
    /**
     * Get all internal SubBasins descriptions and returns as a HashMap object
     * @return 
     */
    public HashMap[] getAllBasins(){
        Iterator<HashMap> allHashesIt;
        HashMap[] returnHash;
        int countHashes;
        
        returnHash = new HashMap[this.allSubBasins.size()];
        allHashesIt = this.allSubBasins.iterator();
        
        countHashes = 0;
        while(allHashesIt.hasNext()){
            returnHash[countHashes] = allHashesIt.next();
            
            countHashes++;
        }
        
        return(returnHash);
    }
    
    /**
     * Get a description of contributing SubBasins
     * @return A vector of size N, where N is the number of contributing SubBasins related to present SubBasin
     */
    public String[] getPresetSubBasins(){
        Iterator<HashMap> allHashesIt;
        String[] returnVector;
        HashMap currentHash;
        int countHashes;
        
        returnVector = new String[this.allSubBasins.size()];
        allHashesIt = this.allSubBasins.iterator();
        
        countHashes = 0;
        while(allHashesIt.hasNext()){
            currentHash = allHashesIt.next();
            returnVector[countHashes] = SubBasinsLogManager.parseHash(currentHash);
            countHashes++;
        }
        
        return(returnVector);
    }
    
    /**
     * Add new subbasin data to object and to file
     * @param addedSubBasin_arg SubBasin to be added data
     * @return TRUE if everything was right with file writing, FALSE otherwise
     */
    public boolean addNewSubBasin(HashMap addedSubBasin_arg){
        boolean subBasinAlreadExists;
        
        if (addedSubBasin_arg == null) return (false);
        
        // check if alread exists
        subBasinAlreadExists = this.subBasinAlreadExists(addedSubBasin_arg);
        if (subBasinAlreadExists) return (false);
        
        // add to list and flush list to file
        this.allSubBasins.add(addedSubBasin_arg);
        try{
            return (this.flushToFile());
        } catch (IOException exp) {
            
        }
        
        return (false);
    }
    
    /**
     * 
     * @param addedSubBasin_arg
     * @return TRUE if it was possible to update or add given SubBasin. FALSE otherwise.
     */
    public boolean addOrUpdateSubBasin(HashMap addedSubBasin_arg){
        int currentPosition;
        
        // basic check
        if (addedSubBasin_arg == null) return (false);
        
        // delete previous reference if it exists
        currentPosition = this.searchSubBasin(addedSubBasin_arg);
        if (currentPosition != (-1)){
            this.allSubBasins.remove(currentPosition);
        }
        
        // add item to end of list and flush to file
        this.allSubBasins.add(addedSubBasin_arg);
        try{
            return(this.flushToFile());
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
        }
        
        return (false);
    }
    
    /**
     * 
     * @param addedSubBasin_arg
     * @return 
     */
    public boolean addNewSubBasin(String addedSubBasin_arg){
        HashMap addedHash;
        
        addedHash = SubBasinsLogManager.parseLine(addedSubBasin_arg);
        
        return(this.addNewSubBasin(addedHash));
    }
    
    /**
     * 
     * @param subBasinHash_arg
     * @return 
     */
    public boolean subBasinAlreadExists(HashMap subBasinHash_arg){
        int hashIndex;
        
        hashIndex = this.searchSubBasin(subBasinHash_arg);
        
        if(hashIndex != (-1)){
            return (true);
        } else {
            return (false);
        }
    }
    
    /**
     * Verify is there is a contributing basin or subbasin related to present subbasin
     * @param subBasinHash_arg HashMap containing descriptions about searched subbasin
     * @return SubBasin index position if found, (-1) otherwise
     */
    public int searchSubBasin(HashMap subBasinHash_arg){
        Integer searchedX, searchedY;
        
        // basic check
        if(subBasinHash_arg == null) return (-1);
        
        // get inlet data and basic check
        searchedX = (Integer)subBasinHash_arg.get("x");
        searchedY = (Integer)subBasinHash_arg.get("y");
        if ((searchedX==null)||(searchedY==null)) return (-1);
        
        // 
        return (this.searchSubBasin(searchedX, searchedY));
    }
    
    /**
     * Verify is there is a contributing basin or subbasin related to present subbasin
     * @param x_arg X value of current searched basin outlet
     * @param y_arg Y value of current searched basin outlet
     * @return A non-negative number if searched subbasin was found, (-1) otherwise
     */
    public int searchSubBasin(int x_arg, int y_arg){
        Integer curHashX, curHashY;
        Iterator<HashMap> hashIt;
        HashMap currentHash;
        int countPos;
        
        // 
        countPos = 0;
        hashIt = this.allSubBasins.iterator();
        while(hashIt.hasNext()){
            currentHash = hashIt.next();
            curHashX = (Integer)currentHash.get("x");
            curHashY = (Integer)currentHash.get("y");
            
            if ( (curHashX.compareTo(x_arg) == 0) && 
                    (curHashY.compareTo(y_arg) == 0) ) {
                return (countPos);
            } else {
                countPos++;
            }
        }
        
        return (-1);
    }
    
    /**
     * Verify is there is a contributing basin or subbasin related to present subbasin
     * @param xOutlet_arg X value of current searched basin outlet
     * @param yOutlet_arg Y value of current searched basin outlet
     * @return Hashmap object describing subbasin if found, NULL otherwise
     */
    public HashMap getSubBasin(int xOutlet_arg, int yOutlet_arg){
        int subBasinIndex;
        
        subBasinIndex = this.searchSubBasin(xOutlet_arg, yOutlet_arg);
        if (subBasinIndex != (-1)) {
            return (this.allSubBasins.get(subBasinIndex));
        } else {
            return (null);
        }
    }
    
    /**
     * Write all data stored in this object to proper SubBasin file
     * @return TRUE if everything was ok with file writing, FALSE otherwise
     * @throws IOException 
     */
    private boolean flushToFile() throws IOException {
        OutputStreamWriter fileWriter;
        BufferedOutputStream buffWriter;
        FileOutputStream filOutStream;
        HashMap currentHash;
        String currentLine;
        
        // open writers
        filOutStream = new FileOutputStream(this.logFile);
        buffWriter = new BufferedOutputStream(filOutStream);
        fileWriter = new OutputStreamWriter(buffWriter);
        
        //
        for(int i=0; i < this.allSubBasins.size(); i++){
            currentHash = this.allSubBasins.get(i);
            currentLine = SubBasinsLogManager.parseHash(currentHash);
            fileWriter.write(currentLine + "\n");
        }
        
        // close writers
        fileWriter.close();
        buffWriter.close();
        filOutStream.close();
        
        return (true);
    }
}
