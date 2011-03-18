/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.io;

import java.io.*;

/**
 *
 * @author ricardo
 */
public class ReadSnowData {

    java.io.File fileSalida;
    java.io.FileOutputStream        outputDir;
    java.io.OutputStreamWriter      newfile;
    java.io.DataOutputStream        newOutputStream;
    java.io.BufferedOutputStream    bufferout;
    String                          ret="\n";
    
    /**
     * The elements to be included in the MetaFile
     */
    public String[] parameters= {
                                    "[Name]",
                                    "[Southernmost Latitude]",
                                    "[Westernmost Longitude]",
                                    "[Longitudinal Resolution (ArcSec)]",
                                    "[Latitudinal Resolution (ArcSec)]",
                                    "[# Columns]",
                                    "[# Rows]",
                                    "[Format]",
                                    "[Missing]",
                                    "[Temporal Resolution]",
                                    "[Units]",
                                    "[Information]"
                                };
    public ReadSnowData() throws java.io.IOException {
        
        java.io.File theFile=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/linksMask.metaVHC");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/Users/ricardo/rawData/IowaConnectivity/linksMask.vhc"));
        int [][] matrizPintada=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        int nColsMP=metaModif.getNumCols();
        int nRowsMP=metaModif.getNumRows();
        double minLonMP=metaModif.getMinLon();
        double minLatMP=metaModif.getMinLat();
        double lonResMP=metaModif.getResLon()/3600.0;
        double latResMP=metaModif.getResLat()/3600.0;

        int maxIndex=0;

        for (int i=0;i<nRowsMP;i++){
            for (int j=0;j<nColsMP;j++){
                maxIndex=Math.max(matrizPintada[i][j],maxIndex);
            }
        }

        float[] counters=new float[maxIndex];

        for (int i=0;i<nRowsMP;i++){
            for (int j=0;j<nColsMP;j++){
                if(matrizPintada[i][j]>0) counters[matrizPintada[i][j]-1]++;
            }
        }
        
        java.io.File dirOut=new java.io.File("/Users/ricardo/rawData/SnowData/hourly-snow-melt-US");
        hydroScalingAPI.util.fileUtilities.DotFilter myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("bil");
        java.io.File[] lasQueSi=dirOut.listFiles(myFiltro);

        java.util.Arrays.sort(lasQueSi);

        int kk=0;
        
//        for (int jj = 0; jj < lasQueSi.length; jj++) {
//
//            System.out.print(lasQueSi[kk]+",");
//
//            java.io.File dataInputFile=lasQueSi[kk];
//
//            java.io.DataInputStream dataBuffer = new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(dataInputFile)));
//
//            int nCols,nRows,nBytes;
//            double minLat,minLon,latRes,lonRes;
//            String minLatStr,minLonStr;
//
//
//            nBytes=16;
//            nCols=6935;
//            nRows=3351;
//            latRes=0.00833333333333300;
//            lonRes=0.00833333333333300;
//            minLon=-124.729583333332;
//            minLat=52.8704166666656-nRows*latRes;
//
//            minLonStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLon,1);
//            minLatStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLat,0);
//
//            float[][] data=new float[nRows][nCols];
//
//            if (nBytes == 16){
//                for (int i=nRows-1;i>=0;i--){
//                    for (int j=0;j<nCols;j++){
//                        int value =dataBuffer.readShort();
//
//                        data[i][j] = value;
//
//                    }
//                }
//            }
//            dataBuffer.close();
//
//            float[] accumulators=new float[maxIndex];
//
//            for (int i=0;i<nRowsMP;i++){
//                for (int j=0;j<nColsMP;j++){
//
//                    int iData=(int)(((i*latResMP+minLatMP)-minLat)/latRes);
//                    int jData=(int)(((j*lonResMP+minLonMP)-minLon)/lonRes);
//
//                    if(matrizPintada[i][j]>0) if(data[iData][jData]!=-9999) accumulators[matrizPintada[i][j]-1]+=data[iData][jData];
//                }
//            }
//
//            for (int i = 0; i < accumulators.length; i++) accumulators[i]/=(counters[i]*100);
//
//            hydroScalingAPI.util.statistics.Stats infoVals=new hydroScalingAPI.util.statistics.Stats(accumulators);
//
//            System.out.println("snow"+kk+","+infoVals.maxValue+","+infoVals.minValue+","+infoVals.meanValue+","+infoVals.standardDeviation);
//
//            new java.io.File(dirOut.getPath()+"/RemappedSnow/").mkdirs();
//
//            outputDir = new FileOutputStream(dirOut.getPath()+"/RemappedSnow/snow"+kk);
//            bufferout=new BufferedOutputStream(outputDir);
//            newOutputStream=new DataOutputStream(bufferout);
//
//            for (int i=0;i<maxIndex;i++){
//                newOutputStream.writeFloat(accumulators[i]);
//            }
//
//            newOutputStream.close();
//            bufferout.close();
//            outputDir.close();
//
//            kk++;
//
//        }

        dirOut=new java.io.File("/Users/ricardo/rawData/SnowData/hourly-snow-melt-IA");
        lasQueSi=dirOut.listFiles(myFiltro);

        java.util.Arrays.sort(lasQueSi);

        for (int jj = 0; jj < lasQueSi.length; jj++) {

            System.out.print(lasQueSi[jj]+",");

            java.io.File dataInputFile=lasQueSi[jj];

            java.io.DataInputStream dataBuffer = new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(dataInputFile)));

            int nCols,nRows,nBytes;
            double minLat,minLon,latRes,lonRes;
            String minLatStr,minLonStr;


            nBytes=16;
            nCols=780;
            nRows=377;
            latRes=0.00833333333333300;
            lonRes=0.00833333333333300;
            minLon=-96.6379166666664;
            minLat=43.5037499999993-nRows*latRes;

            minLonStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLon,1);
            minLatStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLat,0);

            float[][] data=new float[nRows][nCols];

            if (nBytes == 16){
                for (int i=nRows-1;i>=0;i--){
                    for (int j=0;j<nCols;j++){
                        int value =dataBuffer.readShort();

                        data[i][j] = value;

                    }
                }
            }
            dataBuffer.close();

//            float[] accumulators=new float[maxIndex];
//
//            for (int i=0;i<nRowsMP;i++){
//                for (int j=0;j<nColsMP;j++){
//
//                    int iData=(int)(((i*latResMP+minLatMP)-minLat)/latRes);
//                    int jData=(int)(((j*lonResMP+minLonMP)-minLon)/lonRes);
//
//                    if(matrizPintada[i][j]>0) if(data[iData][jData]!=-9999) accumulators[matrizPintada[i][j]-1]+=data[iData][jData];
//                }
//            }
//
//            for (int i = 0; i < accumulators.length; i++) accumulators[i]/=(counters[i]*100);
//
//            hydroScalingAPI.util.statistics.Stats infoVals=new hydroScalingAPI.util.statistics.Stats(accumulators);
//
//            System.out.println("snow"+kk+","+infoVals.maxValue+","+infoVals.minValue+","+infoVals.meanValue+","+infoVals.standardDeviation);
//
//            new java.io.File(dirOut.getPath()+"/RemappedSnow/").mkdirs();
//
//            outputDir = new FileOutputStream(dirOut.getPath()+"/RemappedSnow/snow"+kk);
//            bufferout=new BufferedOutputStream(outputDir);
//            newOutputStream=new DataOutputStream(bufferout);
//
//            for (int i=0;i<maxIndex;i++){
//                newOutputStream.writeFloat(accumulators[i]);
//            }
//
//            newOutputStream.close();
//            bufferout.close();
//            outputDir.close();

            kk++;

            java.io.File outputDirectory=new java.io.File("/Users/ricardo/rawData/SnowData/");

            String fileBinSalida=outputDirectory.getPath()+"/"+dataInputFile.getName()+".vhc";
            String fileAscSalida=outputDirectory.getPath()+"/"+dataInputFile.getName()+".metaVHC";

            java.io.File outputMetaFile=new java.io.File(fileAscSalida);
            java.io.File outputBinaryFile=new java.io.File(fileBinSalida);

            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));

            metaBuffer.write(parameters[0]+"\n");
            metaBuffer.write("Data from The National Map Seamless Data Distribution System"+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[1]+"\n");
            metaBuffer.write(minLatStr+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[2]+"\n");
            metaBuffer.write(minLonStr+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[3]+"\n");
            metaBuffer.write(lonRes*3600+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[4]+"\n");
            metaBuffer.write(latRes*3600+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[5]+"\n");
            metaBuffer.write(nCols+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[6]+"\n");
            metaBuffer.write(nRows+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[7]+"\n");
            metaBuffer.write("float"+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[8]+"\n");
            metaBuffer.write("-9999"+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[9]+"\n");
            metaBuffer.write("fix"+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[10]+"\n");
            metaBuffer.write("m"+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[11]+"\n");
            metaBuffer.write("This Data comes from the USGS DEMs database."+"\n");
            metaBuffer.write("\n");

            metaBuffer.close();

            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    rasterBuffer.writeFloat(data[i][j]);
                }
            }

            rasterBuffer.close();

            System.exit(0);

        }



    }

    /**
     * This method tests this class
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try{
            new hydroScalingAPI.examples.io.ReadSnowData();
        }catch(java.io.IOException ioe){
            System.err.println("error");
            ioe.printStackTrace();
        }

    }

}
