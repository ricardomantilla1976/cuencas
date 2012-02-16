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
public class ReadRainfallData {

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
    public ReadRainfallData(String dirIn) throws java.io.IOException {
        
        java.io.File theFile=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/linksMask_CedarRiver30m.metaVHC");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/Users/ricardo/rawData/IowaConnectivity/linksMask_CedarRiver30m.vhc"));
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
        
        java.io.File dirOut=new java.io.File(dirIn);
        hydroScalingAPI.util.fileUtilities.DotFilter myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("gz");
        java.io.File[] lasQueSi=dirOut.listFiles(myFiltro);

        java.util.Arrays.sort(lasQueSi);
        
        for (int kk = 1440; kk < 7296; kk++) {

            System.out.print(lasQueSi[kk]+",");

            java.io.File dataInputFile=lasQueSi[kk];

            java.io.FileInputStream inputLocal=new java.io.FileInputStream(dataInputFile);
            java.util.zip.GZIPInputStream inputComprim=new java.util.zip.GZIPInputStream(inputLocal);
            java.io.BufferedReader dataBuffer = new java.io.BufferedReader(new java.io.InputStreamReader(inputComprim));

            int nCols,nRows;
            double minLat,minLon,latRes,lonRes;
            String minLatStr,minLonStr;


            nCols=435;
            nRows=264;
            latRes=0.0166666667;
            lonRes=0.0166666667;
            minLon=-97.183333;
            minLat=40.133333;

            minLonStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLon,1);
            minLatStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLat,0);

            float[][] data=new float[nRows][nCols];
            java.util.StringTokenizer tokens;
            String linea=null;
            for (int i=0;i<6;i++) {
                linea = dataBuffer.readLine();
            }

            for (int i=nRows-1;i>=0;i--){
                linea = dataBuffer.readLine();
                tokens = new java.util.StringTokenizer(linea);
                for (int j=0;j<nCols;j++) {
                    try{
                        data[i][j] = new Float(tokens.nextToken()).floatValue();
                    } catch (NumberFormatException NFE){
                        data[i][j] = -99;
                    }
                }
            }
            dataBuffer.close();

            float[] accumulators=new float[maxIndex];

            for (int i=0;i<nRowsMP;i++){
                for (int j=0;j<nColsMP;j++){

                    int iData=(int)(((i*latResMP+minLatMP)-minLat)/latRes);
                    int jData=(int)(((j*lonResMP+minLonMP)-minLon)/lonRes);

                    if(iData>=nRows) iData=nRows-1;

                    if(matrizPintada[i][j]>0) if(data[iData][jData]!=-99) accumulators[matrizPintada[i][j]-1]+=data[iData][jData];
                }
            }

            for (int i = 0; i < accumulators.length; i++) accumulators[i]/=counters[i];
            
            hydroScalingAPI.util.statistics.Stats infoVals=new hydroScalingAPI.util.statistics.Stats(accumulators);

            System.out.println("rain"+kk+","+infoVals.maxValue+","+infoVals.minValue+","+infoVals.meanValue+","+infoVals.standardDeviation);

            new java.io.File(dirOut.getPath()+"/RemappedRain/").mkdirs();
            
            //if(kk>)

            outputDir = new FileOutputStream(dirOut.getPath()+"/RemappedRain/rain"+kk);
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i=0;i<maxIndex;i++){
                newOutputStream.writeFloat(accumulators[i]);
            }

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

        }

        System.exit(0);


//        java.io.File outputDirectory=new java.io.File("/Users/ricardo/rawData/snow/");
//
//        String fileBinSalida=outputDirectory.getPath()+"/"+dataInputFile.getName()+".vhc";
//        String fileAscSalida=outputDirectory.getPath()+"/"+dataInputFile.getName()+".metaVHC";
//
//        java.io.File outputMetaFile=new java.io.File(fileAscSalida);
//        java.io.File outputBinaryFile=new java.io.File(fileBinSalida);
//
//        java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
//        java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));
//
//        metaBuffer.write(parameters[0]+"\n");
//        metaBuffer.write("Data from The National Map Seamless Data Distribution System"+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[1]+"\n");
//        metaBuffer.write(minLatStr+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[2]+"\n");
//        metaBuffer.write(minLonStr+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[3]+"\n");
//        metaBuffer.write(lonRes+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[4]+"\n");
//        metaBuffer.write(latRes+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[5]+"\n");
//        metaBuffer.write(nCols+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[6]+"\n");
//        metaBuffer.write(nRows+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[7]+"\n");
//        metaBuffer.write("float"+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[8]+"\n");
//        metaBuffer.write("-9999"+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[9]+"\n");
//        metaBuffer.write("fix"+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[10]+"\n");
//        metaBuffer.write("m"+"\n");
//        metaBuffer.write("\n");
//        metaBuffer.write(parameters[11]+"\n");
//        metaBuffer.write("This Data comes from the USGS DEMs database."+"\n");
//        metaBuffer.write("\n");
//
//        metaBuffer.close();
//
//        for (int i=0;i<nRows;i++){
//            for (int j=0;j<nCols;j++){
//                rasterBuffer.writeFloat(data[i][j]);
//            }
//        }
//
//        rasterBuffer.close();
        
    }

    /**
     * This method tests this class
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        try{
            new hydroScalingAPI.examples.io.ReadRainfallData("/Users/ricardo/rawData/NCEP_Level_IV_Iowa/ascii2010hourly/");
        }catch(java.io.IOException ioe){
            System.err.println("error");
            ioe.printStackTrace();
        }

    }

}
