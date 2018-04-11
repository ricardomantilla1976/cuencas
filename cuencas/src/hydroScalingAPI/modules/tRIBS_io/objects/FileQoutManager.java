/*
 * FileQoutManager.java
 *
 * Created on March 20, 2007, 8:17 AM
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
public class FileQoutManager {
    
    private java.util.Hashtable availableGauges;
    private int outletNode;
    /** Creates a new instance of FileQoutManager */
    public FileQoutManager(java.io.File[] avGauges,int oN) {
        
        outletNode=oN;
        availableGauges=new java.util.Hashtable();
        for (int i = 0; i < avGauges.length; i++) {
            availableGauges.put(avGauges[i].getName().substring(avGauges[i].getName().lastIndexOf("_")+1,avGauges[i].getName().lastIndexOf(".")),new FileQout(avGauges[i]));
        }

    }
    
    public double getMaxTime(){
        double[] time=getTime("Outlet");
        return time[time.length-1];
    }
    
    public double[] getTime(Object theKey){
        FileQout theNode=(FileQout)availableGauges.get(theKey);
        return theNode.getTime();
    }
    
    public double[] getSeries(Object theKey,int index){
        FileQout theNode=(FileQout)availableGauges.get(theKey);
        return theNode.getSeries(index);
    }
    
    public Object[] getKeys(){
        Object[] toReturn=availableGauges.keySet().toArray();
        java.util.Arrays.sort(toReturn);
        return toReturn;
    }
    
    public int[] getLocationIndexes(String baseName){
        Object[] keys=getKeys();
        int[] indexes=new int[keys.length];
        for (int i = 0; i < indexes.length-1; i++) {
            String key=keys[i].toString();
            indexes[i]=Integer.parseInt(key);
        }
        indexes[indexes.length-1]=outletNode;
        return indexes;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        java.io.File[] files = {new java.io.File("/home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/hyd/smallbasin_Outlet.qout"),
                                new java.io.File("/home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/hyd/smallbasin_5614.qout"),
                                new java.io.File("/home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/hyd/smallbasin_5549.qout")};
        FileQoutManager test = new FileQoutManager(files,1745);
        System.out.println(""+test.getLocationIndexes("base")[0]);
    }
    
}

class FileQout{
    
    private java.io.File pathToQ;
    private int countTimes=-1;
    private double[][] QhData;
    
    public FileQout(java.io.File qf) {
        
        pathToQ=qf;
        try {
            
            java.io.BufferedReader fileQout = new java.io.BufferedReader(new java.io.FileReader(pathToQ));
            String fullLine;
            do{
                fullLine=fileQout.readLine();
                countTimes++;
            } while (fullLine!=null);
            fileQout.close();
            
            fileQout = new java.io.BufferedReader(new java.io.FileReader(pathToQ));

            fullLine=fileQout.readLine();
            String[] elements=fullLine.split("\t");
            
            if(elements[0].equalsIgnoreCase("1-Time,hr")){
                countTimes--;
            } else {
                fileQout.close();
                fileQout = new java.io.BufferedReader(new java.io.FileReader(pathToQ));
            }
            
            QhData=new double[3][countTimes];
            
            for (int i = 0; i < countTimes; i++) {
                fullLine=fileQout.readLine();
                elements=fullLine.split("\t");
                for (int j = 0; j < QhData.length; j++) {
                    QhData[j][i]=Float.parseFloat(elements[j]);
                }
            }
            
            fileQout.close();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public double[] getTime(){
        return QhData[0];
    }
    
    public double[] getSeries(int index){
        return QhData[index];
    }
}
