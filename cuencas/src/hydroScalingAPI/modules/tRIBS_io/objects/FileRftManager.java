/*
 * FileRftManager.java
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
 * Reads the contents of the Rft file

 * @author ricardo
 */
public class FileRftManager {
    
    private java.io.File pathToRft;
    private double[][] RftData;
    private int countTimes=-1;
    
    /** Creates a new instance of FileMrfManager */
    public FileRftManager(java.io.File pRft,double maxTime) {
        
        pathToRft=pRft;
        try {
            
            java.io.BufferedReader fileRft = new java.io.BufferedReader(new java.io.FileReader(pathToRft));
            String fullLine;
            double theTime=0.0;
            fullLine=fileRft.readLine(); countTimes++;
            fullLine=fileRft.readLine(); countTimes++;
            do{
                fullLine=fileRft.readLine();
                if(fullLine == null) break;
                countTimes++;
                theTime=Double.parseDouble(fullLine.split("\t")[0]);
            } while (theTime < maxTime);
            fileRft.close();
            
            fileRft = new java.io.BufferedReader(new java.io.FileReader(pathToRft));

            fullLine=fileRft.readLine();
            String[] elements=fullLine.split("\t");
            
            if(elements[0].equalsIgnoreCase("time")){
                fullLine=fileRft.readLine();
                countTimes-=2;
            } else {
                fileRft.close();
                fileRft = new java.io.BufferedReader(new java.io.FileReader(pathToRft));
            }
            
            RftData=new double[5][countTimes];
            
            for (int i = 0; i < countTimes; i++) {
                fullLine=fileRft.readLine();
                elements=fullLine.split("\t");
                int pointPos=elements[0].indexOf(".");
                RftData[0][i]=Float.parseFloat(elements[0].substring(0,pointPos))+Float.parseFloat(elements[0].substring(pointPos+1))/60.0f;
                for (int j = 1; j < RftData.length; j++) {
                    RftData[j][i]=Float.parseFloat(elements[j]);
                }
            }
            
            fileRft.close();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public double[] getTime(){
        return RftData[0];
    }
    
    public double[] getSeries(int index){
        return RftData[index];
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FileRftManager(new java.io.File("/home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/hyd/smallbasin0018_00.rft"),8);
    }
}
