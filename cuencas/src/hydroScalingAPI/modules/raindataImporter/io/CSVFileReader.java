/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.rainDataImporter.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Very simple class that reads a CSV file. It may be 'on the fly' or entirely.
 * @author A. D. L. Zanchetta
 */
public class CSVFileReader {
    
    public static String[][] readEntireFile(File csvFile_arg){
        return (CSVFileReader.readEntireFile(csvFile_arg, ';'));
    }
    
    /**
     * Read entire file and store its contents on a single variable
     * @param csvFile_arg CSV file to be read
     * @param attributeSeparator_arg Character that represents column separator on csvFile_arg file
     * @return Matrix of format with items sorted such as [line][column]
     */
    public static String[][] readEntireFile(File csvFile_arg, char attributeSeparator_arg){
        Iterator<String[]> tempStorageIt;
        ArrayList<String[]> tempStorage;
        BufferedReader inputBuffReader;
        FileReader inputFileReader;
        String[] currentLineVals;
        String[][] returnObject;
        String currentLine;
        int countColumn;
        int countRows;
        int numColums;
        
        // basic check
        if(csvFile_arg == null) return (null);
        if((!csvFile_arg.exists()) || (!csvFile_arg.canRead())) return (null);
        
        // open file readers
        try{
            inputFileReader = new FileReader(csvFile_arg);
            inputBuffReader = new BufferedReader(inputFileReader);
        } catch (FileNotFoundException exp) {
            System.err.println("FileNotFoundException: " + exp.getMessage());
            return (null);
        }
        
        // create temporary object
        tempStorage = new ArrayList<String[]>();
        numColums = -1;

        // read line by line
        try{
            while ((currentLine = inputBuffReader.readLine()) != null) {
                
                // getting not-empty lines
                if (currentLine.trim().length() == 0) continue;
                currentLineVals = currentLine.trim().split(attributeSeparator_arg + "");
                
                // check number of colums
                if (numColums == -1){
                    numColums = currentLineVals.length;
                } else {
                    if (currentLineVals.length != numColums){
                        System.err.println("CVSwrongNumberOfCols: " + csvFile_arg.getName());
                        break;
                    }
                }
                
                // add to temporary ArrayList
                tempStorage.add(currentLineVals);
            }
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
        }

        // close file reader
        try{
            inputBuffReader.close();
            inputFileReader.close();
        } catch (IOException exp) {
            System.err.println("IOException: " + exp);
        }
        
        // convert from ArrayList to Vector with [numElements][numColumns]
        returnObject = new String[tempStorage.size()][numColums];
        tempStorageIt = tempStorage.iterator();
        countRows = 0;
        while(tempStorageIt.hasNext()){
            currentLineVals = tempStorageIt.next();
            for(countColumn = 0; countColumn < numColums; countColumn++){
                returnObject[countRows][countColumn] = currentLineVals[countColumn];
            }
            countRows++;
        }
        
        return (returnObject);
    }
}
