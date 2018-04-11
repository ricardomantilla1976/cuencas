/*
 * curvatureAnalysis.java
 *
 * Created on March 13, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.artifitialFields;

import ij.util.Java2;
import visad.*;
import java.rmi.RemoteException;

/**
 *
 * @author jesusgomez
 */


public class curvatureAnalysis {

    
public int xB, yB;
public hydroScalingAPI.io.MetaRaster  metaOrig;
public hydroScalingAPI.io.DataRaster  dataOrig;
public hydroScalingAPI.util.geomorphology.objects.Basin basinOrig;   
public java.util.Hashtable hypsoData;
public String path;

public float[][] DEM;
public byte[][] DIR;
public int[][] MAG;
public float[][] GDO;
public byte [][] HOR;
public double[][] SLP;
public float [][] AREA;
public float[][] dToNearChannel;
public float [][] hNearChannel;
public int [][] iNearChannel;
public int [][] jNearChannel;
public float [][] fakeSLP;

public float Dmn = -1;
public float Dmh = -1;
public float Hmn = -1;
public float Hmh = -1;
public byte [][] maskHill;
public int[][] toMark;
public float[][] fakeDEM;
public float[][] refA;
public float[][] refS;

public java.util.Vector DrainageDensity = new java.util.Vector();
public java.util.Vector AreaBasin = new java.util.Vector();
    
    
    /** Creates a new instance of curvatureAnalysis */
    public curvatureAnalysis(int xBasin, int yBasin, hydroScalingAPI.io.MetaRaster metaDEM_Orig, hydroScalingAPI.io.DataRaster dataDEM_Orig, float[] b1 , float[] b2)throws java.io.IOException {
        
        metaOrig = metaDEM_Orig;
        dataOrig = dataDEM_Orig;
        xB = xBasin;
        yB = yBasin;
        hypsoData = new java.util.Hashtable();
        
        System.out.println("**** Original DEM ready!!! ****");
        
        java.io.File fileMeta =  metaOrig.getLocationMeta();
        
        path = fileMeta.getPath().substring(0,fileMeta.getPath().lastIndexOf("."));

        getInitialParameters();

        System.out.println("**** Starting scale analysis  ****");
        
        //getScaleAnalysis();
        //System.exit(0);
        System.out.println("**** Scale analysis finished ****");
        
        getAreaSlope();
        
//        getSyntheticDEM_AS(10f,10f);
//        System.exit(0);

        System.out.println("**** Initial Parameters Obtained ****");
        
        System.out.println("**** Starting iteration process  ****");
              
        getCurvatureAnalysis(b1,b2);
        
        System.out.println("**** Iteration process finished ****");

    }
    
    public void getInitialParameters()throws java.io.IOException{
    
        //Read DEM derived maps
        
        metaOrig.setLocationBinaryFile(new java.io.File(path+".corrDEM"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
        DEM = new hydroScalingAPI.io.DataRaster(metaOrig).getFloat();
        metaOrig.setLocationBinaryFile(new java.io.File(path+".dir"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
        DIR = new hydroScalingAPI.io.DataRaster(metaOrig).getByte();
        metaOrig.setLocationBinaryFile(new java.io.File(path+".magn"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".magn"));
        MAG = new hydroScalingAPI.io.DataRaster(metaOrig).getInt();
        metaOrig.setLocationBinaryFile(new java.io.File(path+".gdo"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".gdo"));
        GDO = new hydroScalingAPI.io.DataRaster(metaOrig).getFloat();

        metaOrig.setLocationBinaryFile(new java.io.File(path+".horton"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".horton"));
        HOR = new hydroScalingAPI.io.DataRaster(metaOrig).getByte();
        metaOrig.setLocationBinaryFile(new java.io.File(path+".slope"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".slope"));
        SLP = new hydroScalingAPI.io.DataRaster(metaOrig).getDouble();
        metaOrig.setLocationBinaryFile(new java.io.File(path+".areas"));
        metaOrig.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".areas"));
        AREA = new hydroScalingAPI.io.DataRaster(metaOrig).getFloat();
        
        basinOrig = new hydroScalingAPI.util.geomorphology.objects.Basin(xB,yB,DIR,metaOrig);
        String folderName = "b1_-1_b2_-1";
        java.util.Hashtable hyp = basinOrig.getHypCurve();
        hypsoData.put(folderName,hyp);
        printHypsoCurve(folderName,hyp);
        
         //Calculates distance to nearest stream
        dToNearChannel=new float[DEM.length][DEM[0].length];
        hNearChannel=new float[DEM.length][DEM[0].length];
        iNearChannel=new int[DEM.length][DEM[0].length];
        jNearChannel=new int[DEM.length][DEM[0].length];
        fakeDEM=new float[DEM.length][DEM[0].length];
        fakeSLP=new float[DEM.length][DEM[0].length];
        refA=new float[DEM.length][DEM[0].length];
        refS=new float[DEM.length][DEM[0].length];
        
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            
            fakeDEM[i][j] = DEM[i][j];
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
                hNearChannel[i][j]=DEM[iPn][jPn];
                iNearChannel[i][j] = iPn;
                jNearChannel[i][j] = jPn;
            }
                
        }
        
        getConstrainsTopography(); 
        getStandardParameters();
        
    }
    
    
    public void getSyntheticDEM(float b1,float b2)throws java.io.IOException{
    
        //Calculates a synthetic DEM
        //b1 Network
        //b2 Hillslope
        
        for(int i=1;i<DEM.length-1;i++) for(int j=1;j<DEM[0].length-1;j++){
            if(maskHill[i][j]!=1){
                
                fakeDEM[i][j]=DEM[i][j];
            
            }else if(MAG[i][j] <= 0){
                
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
                fakeDEM[i][j]=(float)(Hmh*Math.pow(dToNearChannel[i][j]/Dmh,b2))+(float)(DEM[yB][xB] + Hmn*Math.pow((GDO[iPn][jPn]-GDO[yB][xB])/Dmn,b1));
            } else{
                fakeDEM[i][j]=(float)(DEM[yB][xB] + Hmn*Math.pow((GDO[i][j]-GDO[yB][xB])/Dmn,b1));
            }
        }
        
        String folderName = "b1_"+Float.toString(b1)+"_b2_"+Float.toString(b2);

        java.io.File out = new java.io.File(metaOrig.getLocationMeta().getParent()+"/"+folderName);
        
        hydroScalingAPI.io.MetaRaster metaOut=new hydroScalingAPI.io.MetaRaster(metaOrig);
        java.io.File saveFile;
        
        
        out.mkdirs();
        
        metaOut.setFormat("Float");
        metaOut.setLocationMeta(new java.io.File(metaOrig.getLocationMeta().getParent()+"/"+folderName+"/"+folderName+".metaDEM"));
        saveFile=new java.io.File(metaOrig.getLocationMeta().getParent()+"/"+folderName+"/"+folderName+".dem");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        java.io.DataOutputStream writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(fakeDEM[i][j]);
        }
        
        writer.close();
        
        
        new hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule(metaOut, new hydroScalingAPI.io.DataRaster(metaOut));
        
        
        String path_out = metaOut.getLocationMeta().getPath().substring(0,metaOut.getLocationMeta().getPath().lastIndexOf("."));

        metaOut.setLocationBinaryFile(new java.io.File(path_out+".dir"));
        metaOut.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
        byte [][] DIR_out = new hydroScalingAPI.io.DataRaster(metaOut).getByte();
        metaOut.setLocationBinaryFile(new java.io.File(saveFile.getPath()));
        metaOut.setFormat("Float");

        
        hydroScalingAPI.util.geomorphology.objects.Basin bas = new hydroScalingAPI.util.geomorphology.objects.Basin(xB,yB,DIR_out,metaOut);
        java.util.Hashtable hyp = bas.getHypCurve();
        hypsoData.put(folderName,hyp);
        printHypsoCurve(folderName,hyp);
      
       
    }
 
    public void getSyntheticDEM_AS(float m1,float m2)throws java.io.IOException{
    
        //Calculates a synthetic DEM
        //b1 Network
        //b2 Hillslope
        
        
        //exponent for the river network S = Sout * ( A /Aout )^m1 relationship
        //exponent for the hillslope h = Hmax * (A/Dmax)^m2 relationship
        
        float So=0.01f;//(float)SLP[yB][xB];
        float Ao=AREA[yB][xB];
        fakeDEM[yB][xB] = DEM[yB][xB];
        
        java.util.Vector idsBasin=new java.util.Vector();
        toMark = new int[1][]; toMark[0]=new int[] {xB,yB};
        idsBasin.add(toMark[0]);
        
//        It will mark the river network with the relationship Slope-Area
        
//        while(toMark != null){
//            markBasin(DIR, idsBasin,toMark,Ao,So,m1,AREA,GDO,MAG);
//        }
//        
//        It will mark the hillslopes using the ridges as reference
        
        for(int i = 1; i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            if(maskHill[i][j]==1 && MAG[i][j]<0){
                
                byte count = 0;
                for(int k=0; k <= 8; k++){
                    if(DIR[i+(k/3)-1][j+(k%3)-1] == 9-k)
                        count+=1;
                }
                
                if(count == 0){
                    followRidge(i,j,m2);
                }
            }
        
        }
        
        
//////////////////////////////////////////////////////////////////////////////////////////////////////////////        
        
        
        String folderName = "b1_"+Float.toString(m1)+"_b2_"+Float.toString(m2);

        java.io.File out = new java.io.File(metaOrig.getLocationMeta().getParent()+"/"+folderName);
        
        hydroScalingAPI.io.MetaRaster metaOut=new hydroScalingAPI.io.MetaRaster(metaOrig);
        java.io.File saveFile;
        
        
        out.mkdirs();
        
        metaOut.setFormat("Float");
        metaOut.setLocationMeta(new java.io.File(metaOrig.getLocationMeta().getParent()+"/"+folderName+"/"+folderName+".metaDEM"));
        saveFile=new java.io.File(metaOrig.getLocationMeta().getParent()+"/"+folderName+"/"+folderName+".dem");
        metaOut.setLocationBinaryFile(saveFile);
        metaOut.writeMetaRaster(metaOut.getLocationMeta());
        
        java.io.DataOutputStream writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            writer.writeFloat(fakeDEM[i][j]);
        }
        
        writer.close();

        new hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule(metaOut, new hydroScalingAPI.io.DataRaster(metaOut));
        
        
        String path_out = metaOut.getLocationMeta().getPath().substring(0,metaOut.getLocationMeta().getPath().lastIndexOf("."));

        metaOut.setLocationBinaryFile(new java.io.File(path_out+".dir"));
        metaOut.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
        byte [][] DIR_out = new hydroScalingAPI.io.DataRaster(metaOut).getByte();
        metaOut.setLocationBinaryFile(new java.io.File(saveFile.getPath()));

        metaOut.setLocationBinaryFile(new java.io.File(path_out+".areas"));
        metaOut.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".areas"));       
        float [][] AREA_out = new hydroScalingAPI.io.DataRaster(metaOut).getFloat();
        metaOut.setLocationBinaryFile(new java.io.File(saveFile.getPath()));
        metaOut.setFormat("Float");

        
        hydroScalingAPI.util.geomorphology.objects.Basin bas = new hydroScalingAPI.util.geomorphology.objects.Basin(xB,yB,DIR_out,metaOut);
        
        DrainageDensity.addElement(getDD(bas,metaOut,DIR_out));
        AreaBasin.addElement(AREA_out[yB][xB]);
        
        java.util.Hashtable hyp = bas.getHypCurve();
        hypsoData.put(folderName,hyp);
        printHypsoCurve(folderName,hyp);
        
        
        float f = Float.valueOf(metaOrig.getMissing()).floatValue();
        byte[][] mask_syn = bas.getBasinMask(); 
        writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));
        
        for(int i=0;i<DEM.length;i++) for(int j=0;j<DEM[0].length;j++){
            
            if(mask_syn[i][j] != 1){
                writer.writeFloat(f);
            }else{
                writer.writeFloat(fakeDEM[i][j]);
            }
        }
        
        writer.close();       
      
       
    } 
    
    private void markBasin(byte [][] fullDirMatrix, java.util.Vector idsBasin,int[][] ijs,float Ao,float So,float m1,float[][] ARE, float[][] GDO,int[][] MAG){
        java.util.Vector tribsVector=new java.util.Vector();
        float DeltaH = 0f;
        float slope = 0f;
        for(int incoming=0;incoming<ijs.length;incoming++){
            int j=ijs[incoming][0];
            int i=ijs[incoming][1];
            for (int k=0; k <= 8; k++){
                if (MAG[i+(k/3)-1][j+(k%3)-1]>0 && fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k){
                    
                    tribsVector.add(new int[] {j+(k%3)-1,i+(k/3)-1});
                    slope = refS[i+(k/3)-1][j+(k%3)-1]*(float)Math.pow(AREA[i+(k/3)-1][j+(k%3)-1]/refA[i+(k/3)-1][j+(k%3)-1],m1);
                    slope = So*(float)Math.pow(AREA[i+(k/3)-1][j+(k%3)-1]/Ao,m1);
              
              
                    fakeSLP[i+(k/3)-1][j+(k%3)-1] = slope;
                            
                    DeltaH = slope*(GDO[i+(k/3)-1][j+(k%3)-1]-GDO[i][j])*1000.0f;
                    
                    fakeDEM[i+(k/3)-1][j+(k%3)-1] = fakeDEM[i][j]+DeltaH; 

                    System.out.println("j = " + (j+(k%3)-1) + " i = " + (i+(k/3)-1)+ " delta = " + DeltaH + " Area = " + AREA[i+(k/3)-1][j+(k%3)-1] + " H = " + fakeDEM[i+(k/3)-1][j+(k%3)-1]
                           + " HOR = " + HOR[i+(k/3)-1][j+(k%3)-1] + " Dis = " + (GDO[i+(k/3)-1][j+(k%3)-1]-GDO[yB][xB])*1000.0f
                            + " SLP = " + slope);
                }
            }
        }
        
        int countTribs=tribsVector.size();
        if(countTribs != 0){
            toMark=new int[countTribs][2];
            for(int k=0;k<countTribs;k++){
                toMark[k]=(int[])tribsVector.get(k);
                idsBasin.add(toMark[k]);
            }
        } else {
            toMark=null;
        }
    }   
    
    private void followRidge(int i, int j,float m2){
        
        float Hmax = DEM[i][j] - hNearChannel[i][j];
        float Dmax = dToNearChannel[i][j];
        
        fakeDEM[i][j]=(float)(Hmax*Math.pow(dToNearChannel[i][j]/Dmax,m2)) + fakeDEM[iNearChannel[i][j]][jNearChannel[i][j]];
        
        int iPn = i-1+(DIR[i][j]-1)/3;
        int jPn = j-1+(DIR[i][j]-1)%3;

        int iPv=i;
        int jPv=j;

        while(MAG[iPn][jPn] <0 && DIR[iPn][jPn] !=0){
       
            iPn = iPv-1+(DIR[iPv][jPv]-1)/3;
            jPn = jPv-1+(DIR[iPv][jPv]-1)%3;
            iPv=iPn;
            jPv=jPn;
            
            fakeDEM[iPn][jPn]=(float)(Hmax*Math.pow(dToNearChannel[iPn][jPn]/Dmax,m2)) + fakeDEM[iNearChannel[i][j]][jNearChannel[i][j]];
        }

    }
    
    public void getConstrainsTopography()throws java.io.IOException{
        
        maskHill = basinOrig.getBasinMask();
        
        int ncol = metaOrig.getNumCols(); 
        int nrow = metaOrig.getNumRows();
        
        
        for(int i = 0; i<nrow; i++){
            for(int j = 0; j<ncol; j++){
                
                float d = GDO[i][j] - GDO[yB][xB];
                float h = DEM[i][j] - hNearChannel[i][j];
                
                if(MAG[i][j]>0 && maskHill[i][j]==1 && d>Dmn)
                    Dmn = d;

                if(MAG[i][j]>0 && maskHill[i][j]==1 && (DEM[i][j] - DEM[yB][xB])>Hmn)
                    Hmn = (DEM[i][j] - DEM[yB][xB]);
                
                if(maskHill[i][j]==1 && MAG[i][j]<=0 && dToNearChannel[i][j]>Dmh)
                    Dmh = dToNearChannel[i][j];

                if(maskHill[i][j]==1 && MAG[i][j]<=0 && h>Hmh)
                    Hmh = h;
            
            }
        
        }
        
    }
    
    public void printHypsoCurve(String iteration, java.util.Hashtable hyp)throws java.io.IOException{
    

        java.io.File saveFile = new java.io.File(metaOrig.getLocationMeta().getParent() + "/" + iteration + ".txt");   
        java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(saveFile));

        
        float [] areas =  (float [])hyp.get("areas");
        float [] elevs =  (float [])hyp.get("elevs");
        float [] density =  (float [])hyp.get("density");
        hydroScalingAPI.util.statistics.Stats s = (hydroScalingAPI.util.statistics.Stats) hyp.get("stat");
        float integral = ((Float)hyp.get("integral")).floatValue();
        float dsk = ((Float)hyp.get("dsk")).floatValue();
        float dkur = ((Float)hyp.get("dkur")).floatValue();
        
        writer.write(s.meanValue + "\n");
        writer.write(s.standardDeviation + "\n");
        writer.write(s.kurtosis + "\n");
        writer.write(s.skewness + "\n");
        writer.write(integral + "\n");
        
        for(int i=0;i<areas.length;i++)
            writer.write(areas[i] + "\n");
        for(int i=0;i<elevs.length;i++)
            writer.write(elevs[i] + "\n");
        writer.write(dkur + "\n");
        writer.write(dsk + "\n");
        for(int i=0;i<elevs.length;i++)
            writer.write(density[i] + "\n");
        writer.close();
        
    }
    
    
    public void printHypsoCurveScale(String iteration, java.util.Hashtable hyp, float a)throws java.io.IOException{
    

        java.io.File saveFile = new java.io.File(metaOrig.getLocationMeta().getParent() + "/" + iteration + ".txt");   
        java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(saveFile));

        
        float [] areas =  (float [])hyp.get("areas");
        float [] elevs =  (float [])hyp.get("elevs");
        float [] density =  (float [])hyp.get("density");
        hydroScalingAPI.util.statistics.Stats s = (hydroScalingAPI.util.statistics.Stats) hyp.get("stat");
        float integral = ((Float)hyp.get("integral")).floatValue();
        float dsk = ((Float)hyp.get("dsk")).floatValue();
        float dkur = ((Float)hyp.get("dkur")).floatValue();
        
        writer.write(s.meanValue + "\n");
        writer.write(s.standardDeviation + "\n");
        writer.write(s.kurtosis + "\n");
        writer.write(s.skewness + "\n");
        writer.write(integral + "\n");
        
        for(int i=0;i<areas.length;i++)
            writer.write(areas[i] + "\n");
        for(int i=0;i<elevs.length;i++)
            writer.write(elevs[i] + "\n");
        writer.write(dkur + "\n");
        writer.write(dsk + "\n");
        for(int i=0;i<elevs.length;i++)
            writer.write(density[i] + "\n");
        writer.write(a + "\n");
        writer.close();
        
    }    
    
    public void getCurvatureAnalysis(float[] b1, float[] b2)throws java.io.IOException{
    
        for(int i = 0; i<b1.length;i++) for(int j = 0; j<b2.length; j++){
            getSyntheticDEM_AS(b1[i],b2[j]);
        }
        
        java.io.File saveFile = new java.io.File(metaOrig.getLocationMeta().getParent() + "/BasinsParameters.txt");   
        java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(saveFile));
        
        for(int i = 0; i<b1.length;i++) for(int j = 0; j<b2.length; j++){
            writer.write(b1[i] + "\t"+b2[j]+"\t"+ AreaBasin.get(i*b2.length + j)+ "\t"+DrainageDensity.get(i*b2.length + j)+ "\n");
        }

        writer.close();        
            
    }
    
    
    public void ensayo()throws java.io.IOException{
        
        maskHill = basinOrig.getBasinMask();
        
        int ncol = metaOrig.getNumCols(); 
        int nrow = metaOrig.getNumRows();
        
        java.util.Vector net = new java.util.Vector();
        java.util.Vector hill = new java.util.Vector();
        
        for(int i = 0; i<nrow; i++){
            for(int j = 0; j<ncol; j++){
                
                float d = GDO[i][j] - GDO[yB][xB];
                float hh = DEM[i][j] - hNearChannel[i][j];
                float hn = DEM[i][j] - DEM[yB][xB];
                
                if(MAG[i][j]>0 && maskHill[i][j]==1)
                    net.addElement(new float[]{d,hn});
                if(MAG[i][j]<=0 && maskHill[i][j]==1)
                    hill.addElement(new float[]{dToNearChannel[i][j],hh});
            
            }
        
        } 
        
        
        java.io.File saveFile = new java.io.File(metaOrig.getLocationMeta().getParent() + "/" +"hillslopeAnalysisMogollon.txt");   
        java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(saveFile));
       
        for(int i=0;i<hill.size();i++){
            float[] tmp = (float[])hill.get(i);
            writer.write(tmp[0]+ "\t"+ tmp[1] + "\n");
        }
                     
        writer.close();    
        
        saveFile = new java.io.File(metaOrig.getLocationMeta().getParent() + "/" +"networkAnalysisMogollon.txt");   
        writer = new java.io.BufferedWriter(new java.io.FileWriter(saveFile));
       
        for(int i=0;i<net.size();i++){
            float[] tmp = (float[])net.get(i);
            writer.write(tmp[0]+ "\t"+ tmp[1] + "\n");
        }
                     
        writer.close();         
        
    }

    public void getAreaSlope()throws java.io.IOException{
        
        maskHill = basinOrig.getBasinMask();
        
        int ncol = metaOrig.getNumCols(); 
        int nrow = metaOrig.getNumRows();
        
        java.util.Vector net = new java.util.Vector();
        java.util.Vector hill = new java.util.Vector();
        
        for(int i = 0; i<nrow; i++){
            for(int j = 0; j<ncol; j++){
                
                float d = GDO[i][j] - GDO[yB][xB];
                float hh = DEM[i][j] - hNearChannel[i][j];
                float hn = DEM[i][j] - DEM[yB][xB];
                float area = AREA[i][j];
                float slope = (float)SLP[i][j];
                float hor = (float)HOR[i][j];
                
                if(MAG[i][j]>0 && maskHill[i][j]==1)
                    net.addElement(new float[]{d,hn,area,slope,hor});
                if(MAG[i][j]<=0 && maskHill[i][j]==1)
                    hill.addElement(new float[]{dToNearChannel[i][j],hh,area,slope,hor});
            
            }
        
        } 
        
        
        java.io.File saveFile = new java.io.File(metaOrig.getLocationMeta().getParent() + "/" +"hillslopeAnalysisMogollon.txt");   
        java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.FileWriter(saveFile));
       
        for(int i=0;i<hill.size();i++){
            float[] tmp = (float[])hill.get(i);
            writer.write(tmp[0]+ "\t"+ tmp[1]+ "\t"+ tmp[2]+ "\t"+ tmp[3]+ "\t"+ tmp[4] + "\n");
        }
                     
        writer.close();    
        
        saveFile = new java.io.File(metaOrig.getLocationMeta().getParent() + "/" +"networkAnalysisMogollon.txt");   
        writer = new java.io.BufferedWriter(new java.io.FileWriter(saveFile));
       
        for(int i=0;i<net.size();i++){
            float[] tmp = (float[])net.get(i);
            writer.write(tmp[0]+ "\t"+ tmp[1]+ "\t"+ tmp[2]+ "\t"+ tmp[3]+ "\t"+ tmp[4] + "\n");
        }
                     
        writer.close();         
        
    } 
    
    public float getDD( hydroScalingAPI.util.geomorphology.objects.Basin myCuenca,hydroScalingAPI.io.MetaRaster  metaDatos,byte[][]matDir)throws java.io.IOException{
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis myLinksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
            float[][] linkAccumL=myLinksStructure.getVarValues(5);
            float[][] linkAccumA=myLinksStructure.getVarValues(2);
            
            float[][] linkDD=new float[2][linkAccumL[0].length];
            for(int i=0;i<linkAccumL[0].length;i++){
                linkDD[0][i]=(float)Math.log(linkAccumA[0][i]);
                linkDD[1][i]=linkAccumL[0][i]/linkAccumA[0][i];
                linkAccumA[0][i]=linkDD[0][i];
            }
            
            
            int outletID=myLinksStructure.getOutletID();
            float ddMeasuredValue=linkAccumL[0][outletID]/(float)Math.exp(linkAccumA[0][outletID]);
                   
        
        return ddMeasuredValue; 
    }
    
    
    public void getScaleAnalysis()throws java.io.IOException{

        hydroScalingAPI.util.geomorphology.objects.HortonAnalysis myHortonStructure=new hydroScalingAPI.util.geomorphology.objects.HortonAnalysis(basinOrig, metaOrig, DIR);
        
 
        for(int i=0;i<myHortonStructure.contactsArray.length;i++) {
            for(int j=0;j<myHortonStructure.contactsArray[i].length;j++) {
                int xxx=(int)(myHortonStructure.contactsArray[i][j]%metaOrig.getNumCols());
                int yyy=(int)(myHortonStructure.contactsArray[i][j]/metaOrig.getNumCols());
                hydroScalingAPI.util.geomorphology.objects.Basin theCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(xxx,yyy,DIR,metaOrig);
                
                String folderName = "h_" + (i+1) + "_id_"+(j+1) + "_i_" + yyy + "_j_" + xxx;
                java.util.Hashtable hyp = theCuenca.getHypCurve();
                printHypsoCurveScale(folderName,hyp,AREA[yyy][xxx]);
                System.out.print(theCuenca.getXYBasin()[0].length+" ");
            }
            System.out.println(myHortonStructure.contactsArray[i].length);
        }      
        
//                DrainageDensity.addElement(getDD(bas,metaOut,DIR_out));
//        AreaBasin.addElement(AREA_out[yB][xB]);

    
    }
    
    
    public void getStandardParameters(){
        
        java.util.Vector idsBasin=new java.util.Vector();
        int [][] ijs = new int[1][]; ijs[0]=new int[] {xB,yB};
        idsBasin.add(ijs[0]);
        refA[yB][xB] = AREA[yB][xB];
        refS[yB][xB] = (float)SLP[yB][xB];
        
        while(ijs != null){

            
                java.util.Vector tribsVector=new java.util.Vector();

                for(int incoming=0;incoming<ijs.length;incoming++){
                    int j=ijs[incoming][0];
                    int i=ijs[incoming][1];
                    for (int k=0; k <= 8; k++){
                        if (MAG[i+(k/3)-1][j+(k%3)-1]>0 && DIR[i+(k/3)-1][j+(k%3)-1] == 9-k){

                            tribsVector.add(new int[] {j+(k%3)-1,i+(k/3)-1});
                            
                            if(HOR[i+(k/3)-1][j+(k%3)-1] == HOR[i][j]){
                                refA[i+(k/3)-1][j+(k%3)-1] = refA[i][j];
                                refS[i+(k/3)-1][j+(k%3)-1] = refS[i][j];
                            }else{
                                refA[i+(k/3)-1][j+(k%3)-1] = AREA[i+(k/3)-1][j+(k%3)-1];
                                refS[i+(k/3)-1][j+(k%3)-1] = (float)SLP[i+(k/3)-1][j+(k%3)-1];
                                
                            }
                        }
                    }
                }

                int countTribs=tribsVector.size();
                if(countTribs != 0){
                    ijs=new int[countTribs][2];
                    for(int k=0;k<countTribs;k++){
                        ijs[k]=(int[])tribsVector.get(k);
                        idsBasin.add(ijs[k]);
                    }
                } else {
                    ijs=null;
                }            
        }
    
    
    }
    
    public void getRidges() throws RemoteException, VisADException, java.io.IOException{
    
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis myLinksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(basinOrig,metaOrig, DIR);
        
        hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRSNAnalysis=new hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition(myLinksStructure);

        int[][] hillSlopesMask=basinOrig.getEncapsulatedHillslopesMask(DIR,myRSNAnalysis,1); // ridges corresponds to the basin order. In this case it is 1
        
    }
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            
//            args=new String[] { "/Users/jesusgomez/Documents/ensayo/walnutGulchUpdated.metaDEM",
//                                 "/Users/jesusgomez/Documents/ensayo/walnutGulchUpdated.dem"};
//            int x = 193;
//            int y = 281;

            // For mac   
//            args=new String[] { "/Users/jesusgomez/Documents/ensayo_Mogollon/mogollon.metaDEM",
//                                 "/Users/jesusgomez/Documents/ensayo_Mogollon/mogollon.dem"};
            
            // For Windows
                    
//            args=new String[] { "C:/5_temp/Running_Mogollon_040307/mogollon.metaDEM",
//                                 "C:/5_temp/Running_Mogollon_040307/mogollon.dem"};
                    
                    

               
//            args=new String[] { "/Users/jesusgomez/Documents/ensayo_Mogollon/mogollon.metaDEM",
//                                 "/Users/jesusgomez/Documents/ensayo_Mogollon/mogollon.dem"};
//            args=new String[] { "/Users/jesusgomez/Documents/ensayo_Mogollon_2/mogollon.metaDEM",
//                                 "/Users/jesusgomez/Documents/ensayo_Mogollon_2/mogollon.dem"};
            

//            int x = 275; mogollon
//            int y = 311;
            
            
//            args=new String[] { "C:/5_temp/Running_WalnutGulch_040307/walnutGulchUpdated.metaDEM",
//                                 "C:/5_temp/Running_WalnutGulch_040307/walnutGulchUpdated.dem"};
            args=new String[] { "//Mantilla/hidrosigdatabases/Gila River DB/Rasters/Topography/1_ArcSec/mogollon.metaDEM",
                                 "//Mantilla/hidrosigdatabases/Gila River DB/Rasters/Topography/1_ArcSec/mogollon.dem"};
            
            
            
            int x = 689;//308;//302;
            int y = 483;//323;//318;
             
            hydroScalingAPI.io.MetaRaster metaRaster= new hydroScalingAPI.io.MetaRaster(new java.io.File(args[0]));
            metaRaster.setLocationBinaryFile(new java.io.File(args[1]));
            hydroScalingAPI.io.DataRaster datosRaster = new hydroScalingAPI.io.DataRaster(metaRaster);

//            float [] b1 = {-0.8f, -0.6f,-0.5f,-0.4f,-0.2f, 0f, 1f, 5f, 10f};
            float [] b1 = {-1f};
            float [] b2 = {0.1f, 0.2f, 0.5f, 1f, 2f, 5f, 10f};
            new curvatureAnalysis(x,y,metaRaster,datosRaster,b1,b2);
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
    }
}   
