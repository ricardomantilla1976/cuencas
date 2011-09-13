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
 * This class creates files that are appropriate as infiltration maps for a
 * rainfall runoff simulation.
 * @author Ricardo Mantilla
 */
public class createInfiltrationMapsGoodwin {
    
    /**
     * Creates a new instance of createInfiltrationMap
     * @param baseMetaDEM Takes a file pointing to an existing MetaDEM
     * @param inputInfo 
     * @param outputDir 
     * @throws java.io.IOException 
     */
    public createInfiltrationMapsGoodwin(java.io.File baseMetaDEM, java.io.File outputDir) throws java.io.IOException{
        
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(baseMetaDEM);
        java.io.File originalFile=metaData.getLocationMeta();
        
        metaData.setLocationBinaryFile(new java.io.File(originalFile.getParent()+"/"+originalFile.getName().substring(0,originalFile.getName().lastIndexOf("."))+".dir"));
        metaData.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaData).getByte();
        
        java.io.File logFile=new java.io.File(originalFile.getParent()+"/"+originalFile.getName().substring(0,originalFile.getName().lastIndexOf("."))+".log");
        hydroScalingAPI.io.BasinsLogReader subBasinsInfo=new hydroScalingAPI.io.BasinsLogReader(logFile);
        
        String[] basinNames=subBasinsInfo.getPresetBasins();
        
        java.util.Vector basinsVector=new java.util.Vector();
        for(int i=0;i<basinNames.length;i++){
            String[] basinLabel=basinNames[i].split(" ; ");
            String xlabel=(basinLabel[0].split(", "))[0];
            String ylabel=(basinLabel[0].split(", "))[1];
            int matX=Integer.parseInt(xlabel.substring(2).trim());
            int matY=Integer.parseInt(ylabel.substring(2).trim());
            String basinName=basinLabel[1];
            hydroScalingAPI.util.geomorphology.objects.Basin basin=new hydroScalingAPI.util.geomorphology.objects.Basin(matX,matY,matDirs,metaData);
            
            basinsVector.add(basin);
        }
        
        int[][] indexMatrix=new int[metaData.getNumRows()][metaData.getNumCols()];

        for(int i=0;i<basinsVector.size();i++){
            hydroScalingAPI.util.geomorphology.objects.Basin basin=(hydroScalingAPI.util.geomorphology.objects.Basin)basinsVector.get(i);
            int[][] xysBasin=basin.getXYBasin();
            for(int j=0;j<xysBasin[0].length;j++){
                indexMatrix[xysBasin[1][j]][xysBasin[0][j]]=i+1;
            }
        }

        String thisOutputDir=outputDir.getPath()+java.io.File.separator+"subcatchments";

        new java.io.File(thisOutputDir).mkdirs();
        createMetaFile(new java.io.File(thisOutputDir),"subcatchments",metaData);

        java.io.File saveFile=new java.io.File(thisOutputDir+java.io.File.separator+"subcatchments.vhc");
        java.io.DataOutputStream writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));

        for (int yc=0;yc<metaData.getNumRows();yc++) {
            for (int xc=0;xc<metaData.getNumCols();xc++) {
                writer.writeFloat(indexMatrix[yc][xc]);;
            }
        }
        writer.close();

        
        float evID=0.001f;
        

//        double[] eventMeans=new double[] {   8.5133
//                                            ,3.2245
//                                            ,3.4759
//                                            ,11.4284
//                                            ,12.0000
//                                            ,3.5585
//                                            ,2.5470
//                                            ,3.9977
//                                            ,5.5425
//                                            ,2.5270
//                                            ,0.1064
//                                            ,5.5264
//                                            ,7.2106};
        
        //double[] eventMeans=new double[] {   1.23163,      10.2299,      3.24752,      5.19256,      3.43474,      4.38115,      3.03689,      5.65192,      3.86047,      7.44868,      2.81207,     0.966777,      4.14928,      5.05825};
        double[] eventMeans=new double[] {   3.56861,      3.56861,3.56861,3.56861,3.56861,3.56861,3.56861,3.56861,3.56861,3.56861,3.56861,3.56861,3.56861};
        
        
        
        hydroScalingAPI.util.geomorphology.objects.Basin basin=(hydroScalingAPI.util.geomorphology.objects.Basin)basinsVector.get(0);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(basin, metaData, matDirs);
        hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRsnGen = new hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition(mylinksAnalysis);

        int level=1;
        
        int[][] headsTails=myRsnGen.getHeadsAndTails(level);
        int[][] matrixHillslopes=basin.getHillslopesMask(matDirs, myRsnGen, level);
        
        hydroScalingAPI.util.probability.LogGaussianDistribution ranGenerator=new hydroScalingAPI.util.probability.LogGaussianDistribution(0.0f,0.66f);
        java.util.Random rn=new java.util.Random();
        
        int numSimulations=10;
        
        for (int k = 0; k < numSimulations; k++) {
            
            float[] randomLn=new float[headsTails[0].length];
            for (int i = 0; i < randomLn.length; i++) {
                randomLn[i]=(float) Math.exp(rn.nextGaussian()*0.66);
            }
            
            float[][] newMatrix=new float[metaData.getNumRows()][metaData.getNumCols()];
            
            for(int i=0;i<newMatrix.length;i++){
                for(int j=0;j<newMatrix[0].length;j++){
                    if(indexMatrix[i][j] > 0) newMatrix[i][j]=(float)eventMeans[indexMatrix[i][j]-1];//*randomLn[matrixHillslopes[i][j]-1];
                    //newMatrix[i][j]=17;
                }
            }
            
            thisOutputDir=outputDir.getPath();//+java.io.File.separator+"event"+Float.toString(evID).substring(2,4);
        
            new java.io.File(thisOutputDir).mkdirs();
            createMetaFile(new java.io.File(thisOutputDir),"infiltrationRate_event"+Float.toString(evID).substring(2,4),metaData);

            saveFile=new java.io.File(thisOutputDir+java.io.File.separator+"infiltrationRate_event"+Float.toString(evID).substring(2,4)+".vhc");
            writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));

            for (int yc=0;yc<metaData.getNumRows();yc++) {
                for (int xc=0;xc<metaData.getNumCols();xc++) {
                    writer.writeFloat(newMatrix[yc][xc]);;
                }
            }
            writer.close();
            
            evID+=0.01;
            
        }
      
        
    }
    
    /**
     * Writes a metafile with the information needed for the infiltration map created
     * using information from an external program.
     * @param directory 
     * @param newMetaName 
     * @param originalMeta 
     */
    public void createMetaFile(java.io.File directory, String newMetaName, hydroScalingAPI.io.MetaRaster originalMeta) {
          try{          
              java.io.File saveFile=new java.io.File(directory.getPath()+java.io.File.separator+newMetaName+".metaVHC");
              java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(saveFile)); 
              writer.println("[Name]");
              writer.println("Map Generated for Infiltration");
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
              writer.println("Infiltration map designed to conserve mass from events");
              writer.close();
         } catch (java.io.IOException bs) {
             System.out.println("Error composing metafile: "+bs);
         }

     }
    
    /**
     * Executes the program
     * @param args The programs takes no arguments
     */
    public static void main(String[] args) {
        try{
            new createInfiltrationMapsGoodwin(
                                      new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM"),
        			      new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/infiltrationRates/simulated"));
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
    }
    
}
