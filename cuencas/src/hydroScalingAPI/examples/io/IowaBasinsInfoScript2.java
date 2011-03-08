/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.io;

/**
 *
 * @author Ricardo Mantilla
 */
import java.io.*;
import java.util.zip.*;
import java.net.*;

public class IowaBasinsInfoScript2{

    java.io.FileInputStream dataPath;
    java.io.BufferedInputStream dataBuffer;
    java.io.DataInputStream dataDataStream;

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

    int forecastHorizon=50;

    int nColsMP,nRowsMP,maxIndex;
    double minLonMP,minLatMP,lonResMP,latResMP;
    int [][] matrizPintada;
    float[] counters;
    int[][] nextLinkArray;


    public IowaBasinsInfoScript2() throws IOException{

        System.out.println(">> Loading Next Link Array");

        java.io.File basinsLog=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/NextHillslopeIowa.csv");

        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(basinsLog));
        int numLinks=Integer.valueOf(fileMeta.readLine());
        nextLinkArray=new int[numLinks][];
        for (int i = 0; i < nextLinkArray.length; i++) {
            String[] elemsNL=fileMeta.readLine().split(",");
            if(elemsNL.length > 1) {
                nextLinkArray[i]=new int[elemsNL.length-1];
                for (int j = 0; j < nextLinkArray[i].length; j++) nextLinkArray[i][j]=Integer.valueOf(elemsNL[j+1]);
            }
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

    public void Reset() throws java.io.IOException{

        System.out.println(">> Reseting Rainfall Remapping and Current Convolution Files");
        
        System.out.println(">> Loading Rainfall Files List");

        URLConnection urlConn = null;
        URL file;
        GZIPInputStream gzis;
        InputStreamReader xover;
        BufferedReader is;
        String line;

        java.util.Vector<String> availableMapsOfRain=new java.util.Vector<String>();

        file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/quality/60/archive.txt");
        urlConn = file.openConnection();


        xover = new InputStreamReader(urlConn.getInputStream());
        is = new BufferedReader(xover);

        line = is.readLine();

        while(line != null){
            availableMapsOfRain.add(line);
            line = is.readLine();
            if(line.equalsIgnoreCase("H99999999_R6006_G_14AUG2010_000000.out.gz")) break;
        }

        is.close();
        xover.close();


        System.out.println(">> Reading High Quality Rainfall Data and Remapping Rainfall to Hillslopes");

        new java.io.File(dirOut.getPath()+"/RemappedRainfall/").mkdirs();

        int numMaps=availableMapsOfRain.size();

        matrix_rain=new float[numRow_Rain][numCol_Rain];

        int kk=0;

        for (int ff = numMaps-forecastHorizon; ff < numMaps; ff++) {

            String mostRecentFile=availableMapsOfRain.get(ff);

            System.out.println(">> Opening connection: "+"http://s-iihr57.iihr.uiowa.edu/ricardo/quality/60/"+mostRecentFile);
            file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/quality/60/"+mostRecentFile);
            urlConn = file.openConnection();


            System.out.println(">> Loading File # "+kk);

            gzis = new GZIPInputStream(urlConn.getInputStream());
            xover = new InputStreamReader(gzis);
            is = new BufferedReader(xover);

            is.readLine();//# file name: "H99999999_R6003_G_31MAR2010_221000.out
            is.readLine();//# Accumulation map [mm]
            is.readLine();//# Accumulation time [sec]: 3300
            is.readLine();//# number of columns: 1741
            is.readLine();//# number of rows: 1057
            is.readLine();//# grid: LATLON
            is.readLine();//# upper-left LATLONcorner(x,y): 6924 5409
            is.readLine();//# xllcorner [lon]: -97.154167
            is.readLine();//# yllcorner [lat]: 40.133331
            is.readLine();//# cellsize [dec deg]: 0.004167
            is.readLine();//# no data value: -99.0

            line = is.readLine();

            if(line == null) {
                System.out.println(">> File is empty... Aborting remapping process");
            } else {

                for (int i = numRow_Rain-1; i >= 0; i--) {

                    java.util.StringTokenizer linarray = new java.util.StringTokenizer(line);


                    for (int j = 0; j < numCol_Rain; j++) {

                        float f = 0;
                        try {
                            matrix_rain[i][j] = Float.valueOf(linarray.nextToken()).floatValue();
                        } catch (NumberFormatException nfe) {
                            System.out.println("NFE" + nfe.getMessage());
                        }

                    }

                    line = is.readLine();

                }
            }

            is.close();
            xover.close();
            gzis.close();
            
            System.out.println(">> Remapping File # "+kk);

            float[] accumulators=new float[maxIndex];

            for (int i=0;i<nRowsMP;i++){
                for (int j=0;j<nColsMP;j++){

                    int iData=(int)(((i*latResMP+minLatMP)-minLat_Rain)/matRes_Rain);
                    int jData=(int)(((j*lonResMP+minLonMP)-minLon_Rain)/matRes_Rain);

                    if(matrizPintada[i][j]>0) if(matrix_rain[iData][jData]!=-99) accumulators[matrizPintada[i][j]-1]+=matrix_rain[iData][jData];
                }
            }
            
            for (int i=0;i<maxIndex;i++) accumulators[i]/=counters[i];

            System.out.println(">> Writting Remapped Rainfall from File # "+kk);

            outputDir = new FileOutputStream(dirOut.getPath()+"/RemappedRainfall/rain"+kk);
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i=0;i<maxIndex;i++){
                accumulators[i]/=counters[i];
                newOutputStream.writeFloat(accumulators[i]);
            }

            newOutputStream.close();
            bufferout.close();
            outputDir.close();
            
            kk++;

        }

        System.out.println(">> Remapping of Rainfall Files Completed");

        System.out.println(">> Creating Convolution Files");

        new java.io.File(dirOut.getPath()+"/ConvolutionFiles/").mkdirs();

        System.out.println(">> Creating Convolution File # "+0);

        java.nio.channels.FileChannel inChannel = new
            FileInputStream(new java.io.File(dirOut.getPath()+"/RemappedRainfall/rain0")).getChannel();
        java.nio.channels.FileChannel outChannel = new
            FileOutputStream(new java.io.File(dirOut.getPath()+"/ConvolutionFiles/convol-"+0)).getChannel();

        inChannel.transferTo(0, inChannel.size(),
                outChannel);

        if (inChannel != null) inChannel.close();
        if (outChannel != null) outChannel.close();

        for(int j=1;j<forecastHorizon*2;j++){

            float[] currentValues=new float[nextLinkArray.length];
            float[] aggregatedValues=new float[nextLinkArray.length];
            float[] previousValues=new float[nextLinkArray.length];

            System.out.println(">> Creating Convolution File # "+j);

            if(j<forecastHorizon){

                dataPath=new java.io.FileInputStream(dirOut.getPath()+"/RemappedRainfall/rain"+j);
                dataBuffer=new java.io.BufferedInputStream(dataPath);
                dataDataStream=new java.io.DataInputStream(dataBuffer);

                for (int i = 0; i < nextLinkArray.length; i++) currentValues[i]=dataDataStream.readFloat();

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
                if(nextLinkArray[i] != null) {
                    aggregatedValues[nextLinkArray[i][nextLinkArray[i].length-1]]+=currentValues[i];
                    for (int k = 0; k < nextLinkArray[i].length-1; k++) aggregatedValues[nextLinkArray[i][nextLinkArray[i].length-1]]+=currentValues[nextLinkArray[i][k]];
                    aggregatedValues[nextLinkArray[i][nextLinkArray[i].length-1]]+=previousValues[i];
                }
            }

            outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+j);
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i = 0; i < nextLinkArray.length; i++) newOutputStream.writeFloat(aggregatedValues[i]);

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

        }

    }

//    public void Update() throws IOException {
//
//        try{
//
//            URLConnection urlConn = null;
//            URL file;
//            GZIPInputStream gzis;
//            InputStreamReader xover;
//            BufferedReader is;
//            String line;
//
//            file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/latest.txt");
//            urlConn = file.openConnection();
//
//
//            xover = new InputStreamReader(urlConn.getInputStream());
//            is = new BufferedReader(xover);
//
//            String mostRecentFile = is.readLine();
//
//            is.close();
//            xover.close();
//
//            int kk=forecastHorizon-1;
//
//            System.out.println(">> Opening connection: "+"http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/"+mostRecentFile);
//            file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/"+mostRecentFile);
//            urlConn = file.openConnection();
//
//
//            System.out.println(">> Loading File # "+kk);
//
//            gzis = new GZIPInputStream(urlConn.getInputStream());
//            xover = new InputStreamReader(gzis);
//            is = new BufferedReader(xover);
//
//            is.readLine();//# file name: "H99999999_R6003_G_31MAR2010_221000.out
//            is.readLine();//# Accumulation map [mm]
//            is.readLine();//# Accumulation time [sec]: 3300
//            is.readLine();//# number of columns: 1741
//            is.readLine();//# number of rows: 1057
//            is.readLine();//# grid: LATLON
//            is.readLine();//# upper-left LATLONcorner(x,y): 6924 5409
//            is.readLine();//# xllcorner [lon]: -97.154167
//            is.readLine();//# yllcorner [lat]: 40.133331
//            is.readLine();//# cellsize [dec deg]: 0.004167
//            is.readLine();//# no data value: -99.0
//
//            line = is.readLine();
//
//            if(line == null) {
//                System.out.println(">> File is empty... Aborting remapping process");
//            } else {
//
//                for (int i = numRow_Rain-1; i >= 0; i--) {
//
//                    java.util.StringTokenizer linarray = new java.util.StringTokenizer(line);
//
//
//                    for (int j = 0; j < numCol_Rain; j++) {
//
//                        float f = 0;
//                        try {
//                            matrix_rain[i][j] = Float.valueOf(linarray.nextToken()).floatValue();
//                        } catch (NumberFormatException nfe) {
//                            System.out.println("NFE" + nfe.getMessage());
//                        }
//
//                    }
//
//                    line = is.readLine();
//
//                }
//            }
//
//            is.close();
//            xover.close();
//            gzis.close();
//
//            System.out.println(">> Remapping File # "+kk);
//
//            float[] accumulators=new float[maxIndex];
//
//            for (int i=0;i<nRowsMP;i++){
//                for (int j=0;j<nColsMP;j++){
//
//                    int iData=(int)(((i*latResMP+minLatMP)-minLat_Rain)/matRes_Rain);
//                    int jData=(int)(((j*lonResMP+minLonMP)-minLon_Rain)/matRes_Rain);
//
//                    if(matrizPintada[i][j]>0) if(matrix_rain[iData][jData]!=-99) accumulators[matrizPintada[i][j]-1]+=matrix_rain[iData][jData];
//                }
//            }
//
//            for (int i=0;i<maxIndex;i++) accumulators[i]/=counters[i];
//
//            System.out.println(">> Moving Rainfall from File # "+kk);
//
//            moveThisRainfall(accumulators,kk);
//
//            System.out.println(">> Writting Remapped Rainfall from File # "+kk);
//
//            outputDir = new FileOutputStream(dirOut.getPath()+"/RemappedRainfall/rain"+kk);
//            bufferout=new BufferedOutputStream(outputDir);
//            newOutputStream=new DataOutputStream(bufferout);
//
//            for (int i=0;i<maxIndex;i++){
//                accumulators[i]/=counters[i];
//                newOutputStream.writeFloat(accumulators[i]);
//            }
//
//            newOutputStream.close();
//            bufferout.close();
//            outputDir.close();
//
//            System.out.println(">> Updating Convolution Files");
//
//            new java.io.File(dirOut.getPath()+"/ConvolutionFiles/").mkdirs();
//
//            for(int i=1;i<forecastHorizon*2;i++){
//
//                System.out.println(">> Updating Convolution File # "+i);
//
//                java.nio.channels.FileChannel inChannel = new
//                    FileInputStream(new java.io.File(dirOut.getPath()+"/ConvolutionFiles/convol-"+i)).getChannel();
//                java.nio.channels.FileChannel outChannel = new
//                    FileOutputStream(new java.io.File(dirOut.getPath()+"/ConvolutionFiles/convol-"+(i-1))).getChannel();
//
//                inChannel.transferTo(0, inChannel.size(),
//                        outChannel);
//
//                if (inChannel != null) inChannel.close();
//                if (outChannel != null) outChannel.close();
//
//            }
//
//            outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+(forecastHorizon*2-1));
//            bufferout=new BufferedOutputStream(outputDir);
//            newOutputStream=new DataOutputStream(bufferout);
//
//            for (int i=0;i<maxIndex;i++){
//                newOutputStream.writeFloat(0.0f);
//            }
//
//            newOutputStream.close();
//            bufferout.close();
//            outputDir.close();
//
//
//        }
//        catch(MalformedURLException e){
//           e.printStackTrace();
//        }
//
//    }

    public static void main(String[] args) throws IOException {

        IowaBasinsInfoScript2 bigScript=new IowaBasinsInfoScript2();
        bigScript.Reset();
        //bigScript.Update();
        System.exit(0);

    }

}

