/*
 * FileIntegratedManager.java
 *
 * Created on March 20, 2007, 8:18 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.tRIBS_io.objects;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 *
 * @author Ricardo Mantilla
 */
public class FileIntegratedManager {
    
    private java.util.Hashtable availableIntegrated;
    
    /** Creates a new instance of FileIntegratedManager */
    public FileIntegratedManager(java.io.File[] pInt,int nv) {
        availableIntegrated=new java.util.Hashtable();
        FileIntegrated ic=new FileIntegrated(pInt[0],nv);
        availableIntegrated.put("Initial Condition",ic);
        FileIntegrated fs=new FileIntegrated(pInt[1],nv);
        availableIntegrated.put("Final State",fs);
    }
    
    public float[] getValues(Object theKey,int varIndex){
        FileIntegrated theNode=(FileIntegrated)availableIntegrated.get(theKey);
        return theNode.getValues(varIndex);
    }
    
    public Object[] getKeys(){
        Object[] toReturn=availableIntegrated.keySet().toArray();
        java.util.Arrays.sort(toReturn);
        return toReturn;
    }
    
    public void clearData(){
        Object[] toReturn=availableIntegrated.keySet().toArray();
        for (Object theKey : toReturn) {
            FileIntegrated theNode=(FileIntegrated)availableIntegrated.get(theKey);
            theNode.clearData();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FileIntegratedManager(new java.io.File[] {new java.io.File("/Users/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/voronoi/smallbasin.0000_00i")},10);
    }
    
}

class FileIntegrated{
    
    private java.io.File pathToIntegrated;
    private int numVoi;
    private float[][] integData;
    private int countTimes=-1;
    private boolean fullyLoaded=false;
    
    
    /** Creates a new instance of FileIntegratedManager */
    public FileIntegrated(java.io.File pInt,int nv) {
        pathToIntegrated=pInt;
        numVoi=nv;
    }
    
    private void loadFile(){
        try {
            
            java.io.BufferedReader fileInteg = new java.io.BufferedReader(new java.io.FileReader(pathToIntegrated));
            String fullLine;

            fullLine=fileInteg.readLine();
            String[] elements=fullLine.split(",");
            countTimes=elements.length;
            
            if(!elements[0].equalsIgnoreCase("ID")){
                fileInteg.close();
                fileInteg = new java.io.BufferedReader(new java.io.FileReader(pathToIntegrated));
            }
            
            integData=new float[countTimes][numVoi];
            
            for (int i = 0; i < numVoi; i++) {
                fullLine=fileInteg.readLine();
                elements=fullLine.split(",");
                for (int j = 0; j < integData.length; j++) {
                    integData[j][i]=Float.parseFloat(elements[j]);
                }
            }
            
            fileInteg.close();
            fullyLoaded=true;
            System.out.println(">>>> "+pathToIntegrated+" has been fully loaded");
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public float[] getValues(int varIndex){
        if(!fullyLoaded){
            loadFile();
        }
        return integData[varIndex];
    }
    
    public void clearData(){
        integData=null;
        fullyLoaded=false;
        System.out.println(">>>> "+pathToIntegrated+" has been unloaded");
    }
}


