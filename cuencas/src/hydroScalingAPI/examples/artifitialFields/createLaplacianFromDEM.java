/*
 * createLaplacianFromDEM.java
 *
 * Created on January 30, 2007, 1:26 PM
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
public class createLaplacianFromDEM {
    
    int[][] toMark;
    
    /** Creates a new instance of createLaplacianFromDEM */
    public createLaplacianFromDEM(int xOut, int yOut,java.io.File metaFile, java.io.File outputDir) throws java.io.IOException{
        
        outputDir.mkdirs();
        
        //Read DEM derived maps
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(metaFile);
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".corrDEM"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
        float[][] DEM=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".dir"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
        byte[][] DIR=new hydroScalingAPI.io.DataRaster(metaData).getByte();
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".magn"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".magn"));
        int[][] MAG=new hydroScalingAPI.io.DataRaster(metaData).getInt();
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".gdo"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".gdo"));
        float[][] GDO=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        metaData.setLocationBinaryFile(new java.io.File(metaFile.getPath().substring(0,metaFile.getPath().lastIndexOf("."))+".areas"));
        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".areas"));
        float[][] ARE=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(xOut,yOut,DIR,metaData);
        int[][] xyBas=myCuenca.getXYBasin();
        
        //Creates a basin mask file
        float[][] BasMask=new float[DEM.length][DEM[0].length];
        
        for(int i=1;i<xyBas[0].length-1;i++){
            BasMask[xyBas[1][i]][xyBas[0][i]]=1;
        }
        hydroScalingAPI.io.MetaRaster metaOut=new hydroScalingAPI.io.MetaRaster(metaData);
        java.io.File saveFile;
        
        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_BasinMask.metaVHC"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_BasinMask.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        java.io.DataOutputStream writer;
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(BasMask[i][j]);
        }
        
        writer.close();
        
        //Calculates laplacian
        float[][] LAP=new float[DEM.length][DEM[0].length];
        
        float counterZeros=0;
        
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            if(DIR[i][j] == 1 || DIR[i][j] == 3 ||DIR[i][j] == 7 ||DIR[i][j] == 9)
                LAP[i][j]=(DEM[i-1][j]-2*DEM[i][j]+DEM[i+1][j])/30.0f+(DEM[i][j-1]-2*DEM[i][j]+DEM[i][j+1])/30.0f;
            else
                LAP[i][j]=(DEM[i-1][j-1]-2*DEM[i][j]+DEM[i+1][j+1])/30.0f+(DEM[i-1][j+1]-2*DEM[i][j]+DEM[i+1][j-1])/30.0f;
            if(Math.abs(LAP[i][j]) < 1e-4) {
                LAP[i][j]=0;
                counterZeros++;
            }
            if(MAG[i][j] > 0) LAP[i][j]=1;
        }
        System.out.println(counterZeros+" "+(counterZeros/(float)(DEM.length*DEM[0].length)));
        metaOut=new hydroScalingAPI.io.MetaRaster(metaData);
        
        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_LAPLACIAN.metaVHC"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_LAPLACIAN.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(LAP[i][j]);
        }
        
        writer.close();
        
        //Calculates # of incoming links
        int[][] INC=new int[DEM.length][DEM[0].length];
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            int llegan=0;
            for (int k=0; k <= 8; k++){
                if (DIR[i+(k/3)-1][j+(k%3)-1]==9-k)
                    llegan++;
            }
            INC[i][j]=Math.min(llegan,1);
        }
        
        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_INCOMING.metaVHC"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_INCOMING.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Integer");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeInt(INC[i][j]);
        }
        
        writer.close();
        
        hydroScalingAPI.util.statistics.Stats statLap=new hydroScalingAPI.util.statistics.Stats(LAP);
        System.out.println(statLap.toString());
        
        //Calculates distance to nearest stream
        float[][] dToNearChannel=new float[DEM.length][DEM[0].length];
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            if(MAG[i][j] <= 0){
                int iPn = i-1+(DIR[i][j]-1)/3;
                int jPn = j-1+(DIR[i][j]-1)%3;
                
                int iPv=i;
                int jPv=j;
                
                while(MAG[iPn][jPn] <=0 && DIR[iPn][jPn] !=0){
                    iPn = iPv-1+(DIR[iPv][jPv]-1)/3;
                    jPn = jPv-1+(DIR[iPv][jPv]-1)%3;
                    iPv=iPn;
                    jPv=jPn;
                }
                dToNearChannel[i][j]=GDO[i][j]-GDO[iPn][jPn];
                
            }
                
        }

        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_DistToChannel.metaVHC"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_DistToChannel.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(dToNearChannel[i][j]);
        }
        
        writer.close();
        
        //Calculates distance to outlet along channels
        float[][] dAlongChannel=new float[DEM.length][DEM[0].length];
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            if(MAG[i][j] <= 0){
                dAlongChannel[i][j]=GDO[i][j]-dToNearChannel[i][j];
            } else{
                dAlongChannel[i][j]=GDO[i][j];
            }
        }
        
        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_DistAlongChannel.metaVHC"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_DistAlongChannel.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(dAlongChannel[i][j]);
        }
        
        writer.close();
        

        //Calculates elevation drop to nearest stream
        float[][] DropToNearChannel=new float[DEM.length][DEM[0].length];
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            if(MAG[i][j] <= 0){
                int iPn = i-1+(DIR[i][j]-1)/3;
                int jPn = j-1+(DIR[i][j]-1)%3;
                
                int iPv=i;
                int jPv=j;
                
                while(MAG[iPn][jPn] <=0 && DIR[iPn][jPn] !=0){
                    iPn = iPv-1+(DIR[iPv][jPv]-1)/3;
                    jPn = jPv-1+(DIR[iPv][jPv]-1)%3;
                    iPv=iPn;
                    jPv=jPn;
                }
                DropToNearChannel[i][j]=DEM[i][j]-DEM[iPn][jPn];
                
            }
                
        }

        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_DropToChannel.metaVHC"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_DropToChannel.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(DropToNearChannel[i][j]);
        }
        
        writer.close();
        
        //Calculates curvature pameter, volume parameter and relief parameter
        float[][] curvatParam=new float[DEM.length][DEM[0].length];
        float[][] volumeParam=new float[DEM.length][DEM[0].length];
        float[][] reliefParam=new float[DEM.length][DEM[0].length];
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            int llegan=0;
            for (int k=0; k <= 8; k++){
                if (DIR[i+(k/3)-1][j+(k%3)-1]==9-k)
                    llegan++;
            }
            
            if(llegan == 0){
                
                java.util.Vector vDists=new java.util.Vector();
                java.util.Vector vDrops=new java.util.Vector();
                java.util.Vector vIndexes=new java.util.Vector();
                
                vDists.add(dToNearChannel[i][j]);
                vDrops.add(DropToNearChannel[i][j]);
                vIndexes.add(new int[] {i,j});
                
                int iPn = i-1+(DIR[i][j]-1)/3;
                int jPn = j-1+(DIR[i][j]-1)%3;
                
                int iPv=i;
                int jPv=j;
                
                while(MAG[iPn][jPn] <=0 && DIR[iPn][jPn] !=0){
                    vDists.add(dToNearChannel[iPn][jPn]);
                    vDrops.add(DropToNearChannel[iPn][jPn]);
                    vIndexes.add(new int[] {iPn,jPn});
                    
                    iPn = iPv-1+(DIR[iPv][jPv]-1)/3;
                    jPn = jPv-1+(DIR[iPv][jPv]-1)%3;
                    iPv=iPn;
                    jPv=jPn;
                }
                int pathSize=vDists.size();
                
                float[] Xs=new float[pathSize];
                float[] Ys=new float[pathSize];
                
                for(int k=0;k<pathSize;k++){
                    Xs[k]=((Float)vDists.get(k)).floatValue();
                    Ys[k]=((Float)vDrops.get(k)).floatValue();
                    //System.out.println(">>>"+Xs[k]+" "+Ys[k]);
                }
                
                hydroScalingAPI.util.statistics.Stats analysisX=new hydroScalingAPI.util.statistics.Stats(Xs);
                hydroScalingAPI.util.statistics.Stats analysisY=new hydroScalingAPI.util.statistics.Stats(Ys);
                for(int k=0;k<pathSize;k++){
                    Xs[k]/=analysisX.maxValue;
                    Xs[k]=(float)Math.log(Xs[k]);
                    Ys[k]/=analysisY.maxValue;
                    Ys[k]=(float)Math.log(Ys[k]);
                }
                
                float exponent=1.0f;
                if(Xs.length > 1){
                    java.util.Hashtable paramsReg=hydroScalingAPI.util.statistics.Regressions.LinearRegressionCrossAtZero(Xs,Ys);
                
                    exponent=((Double)paramsReg.get("slope")).floatValue();
                }
                
                //System.out.println(">"+exponent);
                //if(BasMask[i][j] == 1 && Xs.length > 10) System.exit(0);

                //if(BasMask[i][j] == 1) System.out.println(">"+analysisY.maxValue);
                
                for(int k=0;k<pathSize;k++){
                    int[] pos=(int[])vIndexes.get(k);
                    if(Xs.length > 1){
                        if(!Float.isNaN(exponent)){
                            curvatParam[pos[0]][pos[1]]=exponent;
                        } else{
                            
                        }
                    } else {
                        curvatParam[pos[0]][pos[1]]=0;
                    }
                    volumeParam[pos[0]][pos[1]]=(float)Math.exp(analysisY.total);
                    reliefParam[pos[0]][pos[1]]=(float)Math.exp(analysisY.maxValue);
                    
                }
//                if(Math.random() > 0.99) {
//                    System.out.println(">"+analysis.yStats.maxValue+" "+analysis.slope);
//                    for(int k=0;k<pathSize;k++){
//                        System.out.println(">>>"+Xs[k]+" "+Ys[k]);
//                    }
//                }
            }
                
        }

        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_HillslopeCurvature.metaVHC"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_HillslopeCurvature.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(curvatParam[i][j]);
        }
        
        writer.close();
        
        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_HillslopeVolume.metaVHC"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_HillslopeVolume.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(volumeParam[i][j]);
        }
        
        writer.close();
        
        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_HillslopeRelief.metaVHC"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_HillslopeRelief.vhc");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(reliefParam[i][j]);
        }
        
        writer.close();
        
        //Calculates an artificial DEM
        float m1=-0.5f; //exponent for the S/So=(A/Ao)^m1 relationship
        float So=0.01f;
        float Ao=ARE[yOut][xOut];
        float m2=0.5f;  //exponent for the h/hmax=(d/dmax)^m2 relationship
        
        float[][] fakeDEM=new float[DEM.length][DEM[0].length];
        
        java.util.Vector idsBasin=new java.util.Vector();
        
        toMark=new int[1][]; toMark[0]=new int[] {xOut,yOut};
        fakeDEM[yOut][xOut]=DEM[yOut][xOut];
        idsBasin.add(toMark[0]);
        while(toMark != null){
            markBasin(DIR, idsBasin,toMark,fakeDEM,Ao,So,m1,ARE,GDO,MAG);
        }
        
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            if(MAG[i][j] <= 0 && BasMask[i][j] ==1){
                int iPn = i-1+(DIR[i][j]-1)/3;
                int jPn = j-1+(DIR[i][j]-1)%3;
                
                int iPv=i;
                int jPv=j;
                
                while(MAG[iPn][jPn] <=0 && DIR[iPn][jPn] !=0){
                    iPn = iPv-1+(DIR[iPv][jPv]-1)/3;
                    jPn = jPv-1+(DIR[iPv][jPv]-1)%3;
                    iPv=iPn;
                    jPv=jPn;
                }
                fakeDEM[i][j]=(float)(40*Math.pow(dToNearChannel[i][j]/0.2,m2))+fakeDEM[iPn][jPn];
            } else {
                fakeDEM[i][j]=DEM[i][j];
            }
        }
        
        metaOut.setLocationMeta(new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_FakeDEM.metaDEM"));
        saveFile=new java.io.File(outputDir+"/"+metaData.getLocationMeta().getName().substring(0,metaData.getLocationMeta().getName().lastIndexOf("."))+"_FakeDEM.dem");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.setFormat("Float");
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(fakeDEM[i][j]);
        }
        
        writer.close();
        
        
    }
    
    private void markBasin(byte [][] fullDirMatrix, java.util.Vector idsBasin,int[][] ijs, float[][] fakeDEM,float Ao,float So,float m1,float[][] ARE, float[][] GDO,int[][] MAG){
        java.util.Vector tribsVector=new java.util.Vector();
        
        for(int incoming=0;incoming<ijs.length;incoming++){
            int j=ijs[incoming][0];
            int i=ijs[incoming][1];
            for (int k=0; k <= 8; k++){
                if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k && MAG[i+(k/3)-1][j+(k%3)-1]>0){
                    tribsVector.add(new int[] {j+(k%3)-1,i+(k/3)-1});
                    
                    float DeltaH=So*(float)Math.pow(ARE[i+(k/3)-1][j+(k%3)-1]/Ao,m1)*(GDO[i+(k/3)-1][j+(k%3)-1]-GDO[i][j])*1000.0f;
                    
                    System.out.println(DeltaH+" "+(GDO[i+(k/3)-1][j+(k%3)-1]-GDO[i][j]));
                    
                    fakeDEM[i+(k/3)-1][j+(k%3)-1]=fakeDEM[i][j]+DeltaH;
                    //getBasin(i+(k/3)-1,j+(k%3)-1, fullDirMatrix,idsBasin);
                }
            }
        }
        int countTribs=tribsVector.size();
        //System.out.println(countTribs);
        if(countTribs != 0){
            toMark=new int[countTribs][2];
            for(int k=0;k<countTribs;k++){
                toMark[k]=(int[])tribsVector.get(k);
                idsBasin.add(toMark[k]);
                //System.out.println(">>"+toMark[k][0]+" "+toMark[k][1]);
            }
        } else {
            toMark=null;
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            new createLaplacianFromDEM(85,42,
                                       new java.io.File("/hidrosigDataBases/Test_DB/Rasters/Topography/58447060.metaDEM"),
                                       new java.io.File("/hidrosigDataBases/Test_DB/Rasters/Hydrology/DerivedQuantities/"));
            
//            new createLaplacianFromDEM(new java.io.File("/hidrosigDataBases/Smallbasin_DB/Rasters/Topography/1_Arcsec/NED_06075640.metaDEM"),
//                                       new java.io.File("/hidrosigDataBases/Smallbasin_DB/Rasters/Hydrology/DerivedQuantities/"));
            
//            new createLaplacianFromDEM(new java.io.File("/hidrosigDataBases/Smallbasin_DB/Rasters/Topography/0.3_Arcsec/89893806.metaDEM"),
//                                       new java.io.File("/hidrosigDataBases/Smallbasin_DB/Rasters/Hydrology/DerivedQuantities/"));
            
//              new createLaplacianFromDEM(new java.io.File("/hidrosigDataBases/Rio Salado DB/Rasters/Topography/NED_26084992.metaDEM"),
//                                       new java.io.File("/hidrosigDataBases/Rio Salado DB/Rasters/Hydrology/DerivedQuantities/"));
              
//              new createLaplacianFromDEM(new java.io.File("/hidrosigDataBases/Gila River DB/Rasters/Topography/1_ArcSec/mogollon.metaDEM"),
//                                       new java.io.File("/hidrosigDataBases/Gila River DB/Rasters/Hydrology/DerivedQuantities/"));
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
    }
    
}
