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
 * createInfiltrationMap.java
 *
 * Created on May 27, 2004, 4:50 PM
 */

package hydroScalingAPI.examples.artifitialFields;

/**
 *
 * @author Ricardo Mantilla
 */
public class createInfiltrationMapFurey {
    
    /** Creates a new instance of createInfiltrationMap */
    public createInfiltrationMapFurey(java.io.File baseMetaDEM, java.io.File inputInfo, java.io.File outputDir) throws java.io.IOException{
        
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(baseMetaDEM);
        java.io.File originalFile=metaData.getLocationMeta();
        
        metaData.setLocationBinaryFile(new java.io.File(originalFile.getParent()+"/"+originalFile.getName().substring(0,originalFile.getName().lastIndexOf("."))+".dir"));
        metaData.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaData).getByte();
        
        java.io.File logFile=new java.io.File(originalFile.getParent()+"/"+originalFile.getName().substring(0,originalFile.getName().lastIndexOf("."))+".log");
        hydroScalingAPI.io.BasinsLogReader subBasinsInfo=new hydroScalingAPI.io.BasinsLogReader(logFile);
        
        String[] basinNames=subBasinsInfo.getPresetBasins();
        
        java.util.Hashtable basinsVector=new java.util.Hashtable();
        for(int i=0;i<basinNames.length;i++){
            String[] basinLabel=basinNames[i].split(" ; ");
            String xlabel=(basinLabel[0].split(", "))[0];
            String ylabel=(basinLabel[0].split(", "))[1];
            int matX=Integer.parseInt(xlabel.substring(2).trim());
            int matY=Integer.parseInt(ylabel.substring(2).trim());
            String basinName=basinLabel[1];
            hydroScalingAPI.util.geomorphology.objects.Basin basin=new hydroScalingAPI.util.geomorphology.objects.Basin(matX,matY,matDirs,metaData);
            basinsVector.put(basinName.trim(),basin);
            System.out.println(basinName);
            
        }
        
        float evID=0.001f;
        
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(inputInfo));
        String fullLine;
        fullLine=fileMeta.readLine();
        fullLine=fileMeta.readLine();
        while (fullLine != null) {
            java.util.Vector availableBasins=new java.util.Vector();
            fullLine=fileMeta.readLine();
            while (fullLine != null && !fullLine.substring(0, 5).equalsIgnoreCase("Event")) {
                availableBasins.add(fullLine);
                fullLine=fileMeta.readLine();
            }
            fullLine=fileMeta.readLine();
            String[] infiltrationInfo=new String[availableBasins.size()];
            for(int i=0;i<infiltrationInfo.length;i++){
                infiltrationInfo[i]=(String)availableBasins.get(i);
            }
            java.util.Arrays.sort(infiltrationInfo);
            
            float[][] newMatrix=new float[metaData.getNumRows()][metaData.getNumCols()];
            
            for(int i=infiltrationInfo.length-1;i>-1;i--){
                hydroScalingAPI.util.geomorphology.objects.Basin basin=(hydroScalingAPI.util.geomorphology.objects.Basin)basinsVector.get(infiltrationInfo[i].substring(53,59).trim());
                int[][] xysBasin=basin.getXYBasin();
                System.out.println(infiltrationInfo[i]);
                
                float meanInfiltrationRate=Float.valueOf(infiltrationInfo[i].substring(246,262)).floatValue();
                
                for(int j=0;j<xysBasin[0].length;j++){
                    newMatrix[xysBasin[1][j]][xysBasin[0][j]]=meanInfiltrationRate;
                    //newMatrix[xysBasin[1][j]][xysBasin[0][j]]=outputFlow/inputFlow;
                }
            }
            
            String thisOutputDir=outputDir.getPath()+java.io.File.separator+"event"+Float.toString(evID).substring(2,4);
        
            new java.io.File(thisOutputDir).mkdirs();
            createMetaFile(new java.io.File(thisOutputDir),"infiltrationRate_event"+Float.toString(evID).substring(2,4),metaData);

            java.io.File saveFile=new java.io.File(thisOutputDir+java.io.File.separator+"infiltrationRate_event"+Float.toString(evID).substring(2,4)+".vhc");
            java.io.DataOutputStream writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));

            for (int yc=0;yc<metaData.getNumRows();yc++) {
                for (int xc=0;xc<metaData.getNumCols();xc++) {
                    writer.writeFloat(newMatrix[yc][xc]);;
                }
            }
            writer.close();
            
            evID+=0.01;
            
        }
        
        fileMeta.close();
        
    }
    
    public void createMetaFile(java.io.File directory, String newMetaName, hydroScalingAPI.io.MetaRaster originalMeta) {
          try{          
              java.io.File saveFile=new java.io.File(directory.getPath()+java.io.File.separator+newMetaName+".metaVHC");
              java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(saveFile)); 
              writer.println("[Name]");
              writer.println("Infiltration Map");
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
              writer.println("fixed");
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
            new createInfiltrationMapFurey(new java.io.File("/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM"),
        			      new java.io.File("/home/furey/Data/goodwin_ms/watbal_nointer_stf_minus_stfmin/Hydrographs/unnestingSummary.txt"),
                                      new java.io.File("/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/infiltrationRates"));
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
    }
    
}
