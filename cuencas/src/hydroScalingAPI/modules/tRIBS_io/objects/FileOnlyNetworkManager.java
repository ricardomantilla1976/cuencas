/*
 * FileOnlyNetworkManager.java
 *
 * Created on October 23, 2007, 9:33 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.tRIBS_io.objects;

/**
 *
 * @author Ricardo Mantilla
 */
public class FileOnlyNetworkManager {
    
    public float[][] netProperties;
    
    /** Creates a new instance of FileOnlyNetworkManager */
    public FileOnlyNetworkManager(java.io.File pathToTriang,int nv,String baseName) throws java.io.IOException{
        
        netProperties=new float[4][nv];
        for (int i = 0; i < netProperties.length; i++) {
            java.util.Arrays.fill(netProperties[i],Float.NaN);
        }
        
        System.out.println(">>>> Reading _width");
        java.io.File nodeFile=new java.io.File(pathToTriang.getPath()+"/"+baseName+"_width");
        java.io.BufferedReader bufferNodes = new java.io.BufferedReader(new java.io.FileReader(nodeFile));
        String fullLine;
        fullLine=bufferNodes.readLine();
        fullLine=bufferNodes.readLine();
        while(fullLine != null){
            String[] lineData=fullLine.split("\t");
            int index=Integer.parseInt(lineData[0]);
            if(index < nv){
                if(lineData.length == 6){
                    netProperties[0][index]=Float.parseFloat(lineData[3]);
                    netProperties[1][index]=Float.parseFloat(lineData[4]);
                    netProperties[2][index]=Float.parseFloat(lineData[5]);
                }
            }
            fullLine=bufferNodes.readLine();
        }
        bufferNodes.close();
        
        System.out.println(">>>> Reading _areas");
        nodeFile=new java.io.File(pathToTriang.getPath()+"/"+baseName+"_area");
        bufferNodes = new java.io.BufferedReader(new java.io.FileReader(nodeFile));
        fullLine=bufferNodes.readLine();
        fullLine=bufferNodes.readLine();
        while(fullLine != null){
            String[] lineData=fullLine.split("\t");
            int index=Integer.parseInt(lineData[0]);
            if(index < nv){
                netProperties[3][index]=Float.parseFloat(lineData[3]);
            }
            fullLine=bufferNodes.readLine();
        }
        bufferNodes.close();
        
    }
    
    public float[] getValues(Object theKey,int varIndex){
        return netProperties[varIndex];
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
