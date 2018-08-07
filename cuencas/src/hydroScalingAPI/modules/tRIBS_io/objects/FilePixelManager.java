/*
 * FilePixelManager.java
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
public class FilePixelManager {
    
    private java.util.Hashtable availablePixel;
    
    
    /** Creates a new instance of FilePixelManager */
    public FilePixelManager(java.io.File[] pInt) {
        availablePixel=new java.util.Hashtable();
        for (int i = 0; i < pInt.length; i++) {
            FilePixel fd=new FilePixel(pInt[i]);
            fd.start();
            availablePixel.put(pInt[i].getName(),fd);
        }
    }
    
    public double[] getTime(Object theKey){
        FilePixel theNode=(FilePixel)availablePixel.get(theKey);
        return theNode.getTime();
    }
    
    public double[] getSeries(Object theKey,int index){
        FilePixel theNode=(FilePixel)availablePixel.get(theKey);
        return theNode.getSeries(index);
    }
    
    public Object[] getKeys(){
        Object[] toReturn=availablePixel.keySet().toArray();
        java.util.Arrays.sort(toReturn);
        return toReturn;
    }
    
    public int[] getLocationIndexes(String baseName){
        Object[] keys=getKeys();
        int[] indexes=new int[keys.length];
        for (int i = 0; i < indexes.length; i++) {
            String key=keys[i].toString();
            key=key.substring(baseName.length());
            key=key.split(".pixel")[0];
            indexes[i]=Integer.parseInt(key);
        }
        return indexes;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FilePixelManager(new java.io.File[] {new java.io.File("/Users/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN_ORIG/Output/voronoi/smallbasin1156.pixel")});
    }
    
}

class FilePixel extends Thread{
    
    private java.io.File pathToP;
    private int countTimes=-1;
    private double[][] QhData;
    private boolean fullyLoaded=false;
    
    
    /** Creates a new instance of FileIntegratedManager */
    public FilePixel(java.io.File pInt) {
        pathToP=pInt;
        try {
            
            java.io.BufferedReader fileQout = new java.io.BufferedReader(new java.io.FileReader(pathToP));
            String fullLine;
            do{
                fullLine=fileQout.readLine();
                countTimes++;
            } while (fullLine!=null);
            fileQout.close();
            
            fileQout = new java.io.BufferedReader(new java.io.FileReader(pathToP));

            fullLine=fileQout.readLine();
            for(int kk=0;kk<10;kk++) fullLine=fullLine.replaceAll("  "," ");
            String[] elements=fullLine.split(" ");
            if(elements[0].equalsIgnoreCase("1-NodeID")){
                countTimes--;
            } else {
                fileQout.close();
                fileQout = new java.io.BufferedReader(new java.io.FileReader(pathToP));
            }
            QhData=new double[49][countTimes];
            
            for (int i = 0; i < countTimes; i++) {
                fullLine=fileQout.readLine();
                for(int kk=0;kk<10;kk++) fullLine=fullLine.replaceAll("  "," ");
                fullLine=fullLine.substring(1);
                elements=fullLine.split(" ");
                for (int j = 0; j < QhData.length; j++) {
                    QhData[j][i]=Float.parseFloat(elements[j]);
                }
            }
            
            fileQout.close();
            
            fullyLoaded=true;
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    public void run(){
        
    }
    
    public double[] getTime(){
        return QhData[1];
    }
    
    public double[] getSeries(int index){
        return QhData[index];
    }
    
    
}
