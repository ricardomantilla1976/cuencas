/*
 * MoreDEMDerivatedMaps.java
 *
 * Created on March 13, 2007, 11:54 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.artifitialFields;

/**
 * Creates some derived maps using the DEM as reference:  The laplacian of the
 * elevations field, the number of convergent cells, the map of distances to the
 * nearest stream, the map of drops from point to nearest stream base, and finally
 * an artifitial DEM
 * @author Ricardo Mantilla
 */
public class ModifyDEMareas {
    
    /** Creates a new instance of createLaplacianFromDEM */
    public ModifyDEMareas(int xOut, int yOut, int[][] xxyyb,float[] areas,java.io.File metaFile) throws java.io.IOException{
        
        //Read DEM derived maps
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(metaFile);
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".areas.original"));
        metaData.setFormat("float");
        float[][] upAreas=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".ltc.original"));
        metaData.setFormat("float");
        float[][] upLength=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".dir"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
        byte[][] DIR=new hydroScalingAPI.io.DataRaster(metaData).getByte();
        
        float[] lenghts=new float[areas.length];
        
        
        System.out.println("Current Outlet Area = "+upAreas[yOut][xOut]);
        
        for (int k = 0; k < xxyyb.length; k++) {
            
            int i=xxyyb[k][1]; //y
            int j=xxyyb[k][0]; //x
            
            int iPv=i;
            int jPv=j;
            
            int iPn = i-1+(DIR[i][j]-1)/3;
            int jPn = j-1+(DIR[i][j]-1)%3;

            areas[k]-=upAreas[i][j];
            lenghts[k]-=upLength[i][j]-30/100.0;
            
            System.out.println(lenghts[k]);
            
            while(upAreas[iPv][jPv] > -1){
                upAreas[iPv][jPv]+=areas[k];
                upLength[iPv][jPv]+=lenghts[k];
                
                iPn = iPv-1+(DIR[iPv][jPv]-1)/3;
                jPn = jPv-1+(DIR[iPv][jPv]-1)%3;
                
                iPv=iPn; jPv=jPn;
                
            }
            
        }
        
        System.out.println("Modified Outlet Area = "+upAreas[yOut][xOut]);
        
        java.io.File saveFile=new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".areas");
        
        java.io.DataOutputStream writer;
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<upAreas.length;i++) for(int j=0;j<upAreas[0].length;j++){
            writer.writeFloat(upAreas[i][j]);
        }
        
        writer.close();
        
        saveFile=new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".ltc");
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<upLength.length;i++) for(int j=0;j<upLength[0].length;j++){
            writer.writeFloat(upLength[i][j]);
        }
        
        writer.close();
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
//        System.out.println(11118.87-8471.89-1486.66-520.59);
//        System.exit(0);

        
        try{
            
            int outletX=1115;
            int outletY=327;
            
            int[][] xxyyb=new int[][] {{912,1063},{677,907},{366,476}}; // IC, OM, ER
            float[] areas=new float[] {8471.89f,520.59f,1486.66f};
            
            new ModifyDEMareas(outletX,outletY,xxyyb,areas,new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaCityToLoneTreeWatershed/irsim30m.metaDEM"));
            
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
    }
    
}

