/*
 * FileMrfManager.java
 *
 * Created on March 20, 2007, 8:03 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.tRIBS_io.objects;

import java.io.FileNotFoundException;
import java.io.IOException;

import java.security.*;
import java.math.*;

/**
 * @author ricardo
 */
public class FileMrfManager {
    
    private java.io.File pathToMRF;
    private double[][] mrfData;
    private int countTimes=-1;
    
    /** Creates a new instance of FileMrfManager */
    public FileMrfManager(java.io.File pMrf,double maxTime) {
        
        pathToMRF=pMrf;
        try {
            
            java.io.BufferedReader fileMrf = new java.io.BufferedReader(new java.io.FileReader(pathToMRF));
            String fullLine;
            double theTime=0.0;
            fullLine=fileMrf.readLine(); countTimes++;
            fullLine=fileMrf.readLine(); countTimes++;
            do{
                fullLine=fileMrf.readLine();
                if(fullLine == null) break;
                countTimes++;
                theTime=Double.parseDouble(fullLine.split("\t")[0]);
            }while (theTime < maxTime);
            fileMrf.close();
            
            fileMrf = new java.io.BufferedReader(new java.io.FileReader(pathToMRF));

            fullLine=fileMrf.readLine();
            String[] elements=fullLine.split("\t");
            
            if(elements[0].equalsIgnoreCase("time")){
                fullLine=fileMrf.readLine();
                countTimes-=2;
            } else {
                fileMrf.close();
                fileMrf = new java.io.BufferedReader(new java.io.FileReader(pathToMRF));
            }
            
            mrfData=new double[13][countTimes];
            
            for (int i = 0; i < countTimes; i++) {
                fullLine=fileMrf.readLine();
                elements=fullLine.split("\t");
                int pointPos=elements[0].indexOf(".");
                mrfData[0][i]=Float.parseFloat(elements[0].substring(0,pointPos))+Float.parseFloat(elements[0].substring(pointPos+1))/60.0f;
                for (int j = 1; j < mrfData.length; j++) {
                    mrfData[j][i]=Float.parseFloat(elements[j]);
                }
            }
            
            fileMrf.close();
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }
    
    public double[] getTime(){
        return mrfData[0];
    }
    
    public double[] getSeries(int index){
        return mrfData[index];
    }
    
    public String SimulationMD5(){
        String s="TheEntireMRF:";
        for (int i = 0; i<mrfData.length; i++) {
            for (int j = 0; j < mrfData[i].length; j+=10) {
                s+=""+mrfData[i][j];
            }
        
        }
        
        MessageDigest m;
        try {
            m = MessageDigest.getInstance("MD5");
            m.update(s.getBytes(),0,s.length());
            return new BigInteger(1,m.digest()).toString(16);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
            return null;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new FileMrfManager(new java.io.File("/Users/ricardo/workFiles/tribsWork/sampleTribs/JEMEZ_TRIBS_RUM/Output/hyd/jm0009_00.mrf"),8);
    }
    
}
