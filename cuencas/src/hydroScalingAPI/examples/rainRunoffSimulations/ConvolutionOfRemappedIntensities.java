/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.rainRunoffSimulations;

/**
 *
 * @author Ricardo Mantilla
 */
import hydroScalingAPI.examples.io.*;
import java.io.*;
import java.util.zip.*;
import java.net.*;

import java.sql.*;

public class ConvolutionOfRemappedIntensities{

    java.io.FileInputStream dataPath,dataPath2;
    java.io.BufferedInputStream dataBuffer,dataBuffer2;
    java.io.DataInputStream dataDataStream,dataDataStream2;

    float[][] matrix_rain = new float[1057][1741];

    int numCol_Rain=1741;
    int numRow_Rain=1057;

    double minLon_Rain=-97.154167;
    double minLat_Rain=40.133331;
    double matRes_Rain=0.004167;

    int numCol_DEM=7000;
    int numRow_DEM=4645;

    double minLon_DEM=-97.57256944;
    double minLat_DEM=39.67363889;
    double matRes_DEM=0.001179728;

    java.io.File fileSalida;
    java.io.FileOutputStream        outputDir;
    java.io.OutputStreamWriter      newfile;
    java.io.DataOutputStream        newOutputStream;
    java.io.BufferedOutputStream    bufferout;
    String                          ret="\n";

    java.io.File dirOut=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/");

    String KMLsPath, OutputPath;

    int forecastHorizon=500;
    int dicretizationOfHour=3;

    int nColsMP,nRowsMP,maxIndex;
    double minLonMP,minLatMP,lonResMP,latResMP;
    int [][] matrizPintada;
    float[] counters;
    int[] nextLinkArray;

    int[][] dbID_linkID;


    public ConvolutionOfRemappedIntensities() throws IOException{

        System.out.println(">> Loading Next Link Array");

        java.io.File basinsLog=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/NextHillslopeIowa.csv");

        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(basinsLog));
        int numLinks=Integer.valueOf(fileMeta.readLine());
        nextLinkArray=new int[numLinks];
        for (int i = 0; i < nextLinkArray.length; i++) {
            nextLinkArray[i]=Integer.valueOf(fileMeta.readLine());
        }

        fileMeta.close();

        System.out.println(">> Loading Hillslope Mask Files");

        java.io.File theFile=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/linksMask.metaVHC");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/Users/ricardo/rawData/IowaConnectivity/linksMask.vhc"));
        matrizPintada=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        nColsMP=metaModif.getNumCols();
        nRowsMP=metaModif.getNumRows();
        minLonMP=metaModif.getMinLon();
        minLatMP=metaModif.getMinLat();
        lonResMP=metaModif.getResLon()/3600.0;
        latResMP=metaModif.getResLat()/3600.0;

        maxIndex=0;

        for (int i=0;i<nRowsMP;i++){
            for (int j=0;j<nColsMP;j++){
                maxIndex=Math.max(matrizPintada[i][j],maxIndex);
            }
        }

        counters=new float[maxIndex];

        for (int i=0;i<nRowsMP;i++){
            for (int j=0;j<nColsMP;j++){
                if(matrizPintada[i][j]>0) counters[matrizPintada[i][j]-1]++;
            }
        }

    }

    public void ExecuteIndexReset(java.io.File dirOut) throws java.io.IOException{


        java.io.File[] lasQueSi=new java.io.File(dirOut.getPath()+"/RemappedRain/").listFiles();
        float[] maxWidthFunction=new float[maxIndex];


        int initialConvolutionTime=2160*dicretizationOfHour;
        int totalConvolutionPeriod=8015*dicretizationOfHour;//lasQueSi.length*dicretizationOfHour;


        System.out.println(">> Creating Convolution Files");

        new java.io.File(dirOut.getPath()+"/ConvolutionFiles/").mkdirs();

        System.out.println(">> Creating Convolution File # "+initialConvolutionTime);

        java.nio.channels.FileChannel inChannel = new
            FileInputStream(new java.io.File(dirOut.getPath()+"/RemappedRain/rain"+initialConvolutionTime)).getChannel();
        java.nio.channels.FileChannel outChannel = new
            FileOutputStream(new java.io.File(dirOut.getPath()+"/ConvolutionFiles/convol-"+initialConvolutionTime)).getChannel();

        inChannel.transferTo(0, inChannel.size(),
                outChannel);

        if (inChannel != null) inChannel.close();
        if (outChannel != null) outChannel.close();

        for(int j=initialConvolutionTime+1;j<totalConvolutionPeriod;j++){
        
            float[] currentValues=new float[nextLinkArray.length];
            float[] previousValues=new float[nextLinkArray.length];

            System.out.println(">> Creating Convolution File # "+j);

            if(j<totalConvolutionPeriod){

                dataPath=new java.io.FileInputStream(dirOut.getPath()+"/RemappedRain/rain"+j/dicretizationOfHour);
                dataBuffer=new java.io.BufferedInputStream(dataPath);
                dataDataStream=new java.io.DataInputStream(dataBuffer);

                for (int i = 0; i < nextLinkArray.length; i++) currentValues[i]=dataDataStream.readFloat()/(float)dicretizationOfHour;

                dataBuffer.close();
                dataDataStream.close();
            }

            dataPath=new java.io.FileInputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+(j-1));
            dataBuffer=new java.io.BufferedInputStream(dataPath);
            dataDataStream=new java.io.DataInputStream(dataBuffer);

            for (int i = 0; i < nextLinkArray.length; i++) previousValues[i]=dataDataStream.readFloat();


            dataBuffer.close();
            dataDataStream.close();


            for (int i = 0; i < nextLinkArray.length; i++) {
                if(nextLinkArray[i] != -1) {
                    currentValues[nextLinkArray[i]]+=previousValues[i];
                }
            }
            
            outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+j);
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i = 0; i < nextLinkArray.length; i++) {
                newOutputStream.writeFloat(currentValues[i]);
                maxWidthFunction[i]=Math.max(currentValues[i], maxWidthFunction[i]);
            }

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

        }

        outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/maxConvol");
        bufferout=new BufferedOutputStream(outputDir);
        newOutputStream=new DataOutputStream(bufferout);

        for (int i = 0; i < nextLinkArray.length; i++) {
            newOutputStream.writeFloat(maxWidthFunction[i]);
        }

        newOutputStream.close();
        bufferout.close();
        outputDir.close();

        maxWidthFunction=new float[maxIndex];

        System.out.println(">> Creating Counting Convolution Files");

        new java.io.File(dirOut.getPath()+"/ConvolutionFiles/").mkdirs();

        System.out.println(">> Creating Counting Convolution File # "+initialConvolutionTime);

        dataPath=new java.io.FileInputStream(dirOut.getPath()+"/RemappedRain/rain"+initialConvolutionTime);
        dataBuffer=new java.io.BufferedInputStream(dataPath);
        dataDataStream=new java.io.DataInputStream(dataBuffer);

        outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/countConvol-"+initialConvolutionTime);
        bufferout=new BufferedOutputStream(outputDir);
        newOutputStream=new DataOutputStream(bufferout);

        for (int i = 0; i < nextLinkArray.length; i++) newOutputStream.writeFloat(dataDataStream.readFloat()>0?1:0);

        newOutputStream.close();
        bufferout.close();
        outputDir.close();

        dataBuffer.close();
        dataDataStream.close();

        for(int j=initialConvolutionTime+1;j<totalConvolutionPeriod;j++){

            float[] currentValues=new float[nextLinkArray.length];
            float[] previousValues=new float[nextLinkArray.length];

            System.out.println(">> Creating Counting Convolution File # "+j);

            if(j<totalConvolutionPeriod){

                dataPath=new java.io.FileInputStream(dirOut.getPath()+"/RemappedRain/rain"+j/dicretizationOfHour);
                dataBuffer=new java.io.BufferedInputStream(dataPath);
                dataDataStream=new java.io.DataInputStream(dataBuffer);

                for (int i = 0; i < nextLinkArray.length; i++) currentValues[i]=dataDataStream.readFloat()>0?1:0;

                dataBuffer.close();
                dataDataStream.close();
            }

            dataPath=new java.io.FileInputStream(dirOut.getPath()+"/ConvolutionFiles/countConvol-"+(j-1));
            dataBuffer=new java.io.BufferedInputStream(dataPath);
            dataDataStream=new java.io.DataInputStream(dataBuffer);

            for (int i = 0; i < nextLinkArray.length; i++) previousValues[i]=dataDataStream.readFloat();


            dataBuffer.close();
            dataDataStream.close();


            for (int i = 0; i < nextLinkArray.length; i++) {
                if(nextLinkArray[i] != -1) {
                    currentValues[nextLinkArray[i]]+=previousValues[i];
                }
            }

            outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/countConvol-"+j);
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i = 0; i < nextLinkArray.length; i++) {
                newOutputStream.writeFloat(currentValues[i]);
                maxWidthFunction[i]=Math.max(currentValues[i], maxWidthFunction[i]);
            }

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

        }

        outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/maxCountConvol");
        bufferout=new BufferedOutputStream(outputDir);
        newOutputStream=new DataOutputStream(bufferout);

        for (int i = 0; i < nextLinkArray.length; i++) {
            newOutputStream.writeFloat(maxWidthFunction[i]);
        }

        newOutputStream.close();
        bufferout.close();
        outputDir.close();



    }

    public static void main(String[] args) throws IOException {

        ConvolutionOfRemappedIntensities bigScript=new ConvolutionOfRemappedIntensities();
        bigScript.ExecuteIndexReset(new java.io.File("/Users/ricardo/rawData/NCEP_Level_IV_Iowa/ascii2010hourly/"));
    }

}

