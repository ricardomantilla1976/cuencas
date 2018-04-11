/*
CUENCAS is a River Network Oriented GIS
Copyright (C) 2005  Ricardo Mantilla

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


/*
 * createRandomPrecipitationMap.java
 *
 * Created on May 27, 2004, 4:50 PM
 */

package hydroScalingAPI.examples.artifitialFields;

/**
 *
 * @author Ricardo Mantilla
 */
public class createRandomPrecipitationMap {
    
    /** Creates a new instance of createInfiltrationMap */
    public createRandomPrecipitationMap(float mean, float stDeviation,java.io.File baseMetaDEM, java.io.File outputDir) throws java.io.IOException{
        
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(baseMetaDEM);
        java.io.File originalFile=metaData.getLocationMeta();
        
            float[][] newMatrix=new float[metaData.getNumRows()][metaData.getNumCols()];
            
            for(int i=0;i<newMatrix.length;i++){
                for(int j=0;j<newMatrix[0].length-1;j+=2){
                    
                    //Here we implement the Polar-Marsaglia method of Gausian random number generation
                    float V1 = (float)(2.*Math.random()-1);
                    float V2 = (float)(2.*Math.random()-1);
                    while (V1*V1+V2*V2>1)
                    {
                      V1 = (float)(2.*Math.random()-1);
                      V2 = (float)(2.*Math.random()-1); 
                    }
                    float C = (float)Math.sqrt(-2*Math.log(V1*V1+V2*V2)/(V1*V1+V2*V2));
                    float X1 = C*V1; //~N(0,1)
                    float X2 = C*V2;//~N(0,1)
                    // Here we transform the N(0,1) to N(mean, stDeviation) and populate our rainfall matrix
                    newMatrix[i][j]=mean+stDeviation*X1;
                    newMatrix[i][j+1]=mean+stDeviation*X2;
                
            }
            
            String thisOutputDir=outputDir.getPath()+java.io.File.separator+"eventFake";
        
            new java.io.File(thisOutputDir).mkdirs();
            createMetaFile(new java.io.File(thisOutputDir),"precipRandom",metaData);

            java.io.File saveFile=new java.io.File(thisOutputDir+java.io.File.separator+"precipRandom.060000.01.July.1971.vhc");
            java.io.DataOutputStream writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));

            for (int yc=0;yc<metaData.getNumRows();yc++) {
                for (int xc=0;xc<metaData.getNumCols();xc++) {
                    writer.writeFloat(newMatrix[yc][xc]);;
                }
            }
            writer.close();
            
    }
    }
    
    public void createMetaFile(java.io.File directory, String newMetaName, hydroScalingAPI.io.MetaRaster originalMeta) {
          try{          
              java.io.File saveFile=new java.io.File(directory.getPath()+java.io.File.separator+newMetaName+".metaVHC");
              java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(saveFile)); 
              writer.println("[Name]");
              writer.println("Precipitation Radar Data From KICT");
              writer.println(""); 
              writer.println("[Southernmost Latitude]"); 
              writer.println(hydroScalingAPI.tools.DegreesToDMS.getprettyString(originalMeta.getMinLat(),0));
              writer.println(""); 
              writer.println("[Westernmost Longitude]");         
              writer.println(hydroScalingAPI.tools.DegreesToDMS.getprettyString(originalMeta.getMinLon(),1));
              writer.println(""); 
              writer.println("[Longitudinal Resolution (ArcSec)]");
              writer.println(originalMeta.getResLat());
              writer.println(""); 
              writer.println("[Latitudinal Resolution (ArcSec)]");
              writer.println(originalMeta.getResLon());
              writer.println(""); 
              writer.println("[# Columns]");
              writer.println(originalMeta.getNumCols());
              writer.println(""); 
              writer.println("[# Rows]");
              writer.println(originalMeta.getNumRows());
              writer.println(""); 
              writer.println("[Format]");
              writer.println("Float");
              writer.println(""); 
              writer.println("[Missing]");
              writer.println("0");
              writer.println(""); 
              writer.println("[Temporal Resolution]");
              writer.println("15-minutes"); //This is the duration of the storm
              writer.println(""); 
              writer.println("[Units]");
              writer.println("mm/h");
              writer.println(""); 
              writer.println("[Information]");
              writer.println("Infiltration map desicgned to conserve mass from events");
              writer.close();
         } catch (java.io.IOException bs) {
             System.out.println("Error composing metafile: "+bs);
         }

     }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            new createRandomPrecipitationMap(100.f,2.f,new java.io.File("C:/CuencasDataBases/ClearCreek_Database/Rasters/Topography/ClearCreek/NED_00159011.metaDEM"),
                                            new java.io.File("C:/CuencasDataBases/ClearCreek_Database/Rasters/Hydrology/Rainfall"));
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
    }
    
}

