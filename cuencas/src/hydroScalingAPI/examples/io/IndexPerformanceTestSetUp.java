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

import java.sql.*;

public class IndexPerformanceTestSetUp{

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

    java.io.File dirOut=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/IndexEvaluation/");

    String KMLsPath, OutputPath;

    int forecastHorizon=500;
    int dicretizationOfHour=3;

    int nColsMP,nRowsMP,maxIndex;
    double minLonMP,minLatMP,lonResMP,latResMP;
    int [][] matrizPintada;
    float[] counters;
    int[] nextLinkArray;

    int[][] dbID_linkID;

    float[] peakIndex;


    public IndexPerformanceTestSetUp() throws IOException{

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

        System.out.println(">> Creating Counters");

        counters=new float[maxIndex];

        for (int i=0;i<nRowsMP;i++){
            for (int j=0;j<nColsMP;j++){
                if(matrizPintada[i][j]>0) counters[matrizPintada[i][j]-1]++;
            }
        }

        System.out.println(">> Loading Flood Index");

        peakIndex=new float[nextLinkArray.length];

        dataPath=new java.io.FileInputStream("/Users/ricardo/rawData/IowaConnectivity/indexReferenceValue.xdr");
        dataBuffer=new java.io.BufferedInputStream(dataPath);
        dataDataStream=new java.io.DataInputStream(dataBuffer);

        for (int i = 0; i < nextLinkArray.length; i++) {
            peakIndex[i] = dataDataStream.readFloat();
        }

        dataBuffer.close();
        dataDataStream.close();



    }

    public void ExecuteIndexReset() throws java.io.IOException{


        System.out.println(">> Reseting Rainfall Remapping and Current Convolution Files");

        System.out.println(">> Loading Rainfall Files List");

        URLConnection urlConn = null;
        URL file;
        GZIPInputStream gzis;
        InputStreamReader xover;
        BufferedReader is;
        String line;
        int posApr1st=0, fn=0;

        java.util.Vector<String> availableMapsOfRain=new java.util.Vector<String>();

        file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/archive.txt");
        urlConn = file.openConnection();


        xover = new InputStreamReader(urlConn.getInputStream());
        is = new BufferedReader(xover);

        line = is.readLine();
        
        while(line != null){
            if(line.equalsIgnoreCase("H99999999_R6007_G_01APR2011_000000.out.gz")) posApr1st=fn;
            availableMapsOfRain.add(line);
            line = is.readLine(); fn++;
        }

        is.close();
        xover.close();
        
        System.out.println(">> Reading High Quality Rainfall Data and Remapping Rainfall to Hillslopes");

        new java.io.File(dirOut.getPath()+"/RemappedRainfall/").mkdirs();

        int numMaps=availableMapsOfRain.size();

        forecastHorizon=numMaps-posApr1st;

        matrix_rain=new float[numRow_Rain][numCol_Rain];

        float[] maxWidthFunction=new float[maxIndex];

        int kk=0;

        for (int ff = numMaps-forecastHorizon; ff < numMaps; ff++) {

            String mostRecentFile=availableMapsOfRain.get(ff);

            System.out.println(">> Opening connection: "+"http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/"+mostRecentFile);
            file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/"+mostRecentFile);
            
            try {
                urlConn = file.openConnection();
                
                System.out.println(">> Loading File # "+(ff-(numMaps-forecastHorizon)));

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
                
                line = is.readLine();
                
                if(line != null) if(line.indexOf("data") != -1) line=is.readLine(); //# no data value: -99.0

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
            } catch (IOException iOException) {
                System.out.println(">> File is missing... Aborting remapping process");
            }


            

            System.out.println(">> Remapping File # "+(ff-(numMaps-forecastHorizon)));

            float[] accumulators=new float[maxIndex];

            for (int i=0;i<nRowsMP;i++){
                for (int j=0;j<nColsMP;j++){

                    int iData=(int)(((i*latResMP+minLatMP)-minLat_Rain)/matRes_Rain);
                    int jData=(int)(((j*lonResMP+minLonMP)-minLon_Rain)/matRes_Rain);

                    if(matrizPintada[i][j]>0) if(matrix_rain[iData][jData]>0) accumulators[matrizPintada[i][j]-1]+=matrix_rain[iData][jData];
                }
            }

            for (int i=0;i<maxIndex;i++) accumulators[i]/=counters[i];

            System.out.println(">> Writting Remapped Rainfall from File # "+kk);

            outputDir = new FileOutputStream(dirOut.getPath()+"/RemappedRainfall/rain"+kk);
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i=0;i<maxIndex;i++){
                newOutputStream.writeFloat(accumulators[i]);
            }

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

            kk++;
            
        }

        int totalConvolutionPeriod=kk*dicretizationOfHour;

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

        for(int j=1;j<totalConvolutionPeriod*2;j++){

            float[] currentValues=new float[nextLinkArray.length];
            float[] previousValues=new float[nextLinkArray.length];

            System.out.println(">> Creating Convolution File # "+j);

            if(j<totalConvolutionPeriod){

                dataPath=new java.io.FileInputStream(dirOut.getPath()+"/RemappedRainfall/rain"+j/dicretizationOfHour);
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

    }

    public void ExecuteIndexUpdate() throws IOException {

        try{

            URLConnection urlConn = null;
            URL file;
            GZIPInputStream gzis;
            InputStreamReader xover;
            BufferedReader is;
            String line;

            file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/latest.txt");
            urlConn = file.openConnection();


            xover = new InputStreamReader(urlConn.getInputStream());
            is = new BufferedReader(xover);

            String mostRecentFile = is.readLine();

            is.close();
            xover.close();

            int kk=forecastHorizon;

            System.out.println(">> Opening connection: "+"http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/"+mostRecentFile);
            file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/"+mostRecentFile);
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

                    if(matrizPintada[i][j]>0) if(matrix_rain[iData][jData]>0) accumulators[matrizPintada[i][j]-1]+=matrix_rain[iData][jData];
                }
            }

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

            System.out.println(">> Updating Convolution Files");

            kk=forecastHorizon*dicretizationOfHour;

            new java.io.File(dirOut.getPath()+"/ConvolutionFiles/").mkdirs();

            for(int j=kk;j<forecastHorizon*dicretizationOfHour*2;j++){

                float[] currentValues=new float[nextLinkArray.length];
                float[] previousValues=new float[nextLinkArray.length];

                System.out.println(">> Updating Convolution File # "+j);

                if(j<dicretizationOfHour*(forecastHorizon+1)){

                    dataPath=new java.io.FileInputStream(dirOut.getPath()+"/RemappedRainfall/rain"+j/dicretizationOfHour);
                    dataBuffer=new java.io.BufferedInputStream(dataPath);
                    dataDataStream=new java.io.DataInputStream(dataBuffer);

                    for (int i = 0; i < nextLinkArray.length; i++) currentValues[i]=dataDataStream.readFloat()/(float)dicretizationOfHour;

                    dataBuffer.close();
                    dataDataStream.close();

                    System.out.println(">> Leyo Lluvia!!");
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

                for (int i = 0; i < nextLinkArray.length; i++) newOutputStream.writeFloat(currentValues[i]);

                newOutputStream.close();
                bufferout.close();
                outputDir.close();

            }

            for(int i=0;i<dicretizationOfHour;i++){

                System.out.println(">> Deleting Convolution File # "+i);

                new java.io.File(dirOut.getPath()+"/ConvolutionFiles/convol-"+i).delete();

            }

            for(int i=dicretizationOfHour;i<forecastHorizon*dicretizationOfHour*2;i++){

                System.out.println(">> Translating Convolution File # "+i+" to Convolution File "+(i-dicretizationOfHour));

                new java.io.File(dirOut.getPath()+"/ConvolutionFiles/convol-"+i).renameTo(new java.io.File(dirOut.getPath()+"/ConvolutionFiles/convol-"+(i-dicretizationOfHour)));

            }

            for(int ll=forecastHorizon*dicretizationOfHour*2-dicretizationOfHour;ll<forecastHorizon*dicretizationOfHour*2;ll++){

                System.out.println(">> Zeroing Convolution File # "+ll);

                outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+ll);
                bufferout=new BufferedOutputStream(outputDir);
                newOutputStream=new DataOutputStream(bufferout);

                for (int i=0;i<maxIndex;i++){
                    newOutputStream.writeFloat(0.0f);
                }

                newOutputStream.close();
                bufferout.close();
                outputDir.close();

            }

        }
        catch(MalformedURLException e){
           e.printStackTrace();
        }

        int[] peakLocations=new int[nextLinkArray.length];
        float[] peakValues=new float[nextLinkArray.length];

        for(int j=(forecastHorizon-12)*dicretizationOfHour;j<(forecastHorizon+24*5)*dicretizationOfHour;j++){

            System.out.println(">> Checking Convolution File # "+j);

            dataPath=new java.io.FileInputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+j);
            dataBuffer=new java.io.BufferedInputStream(dataPath);
            dataDataStream=new java.io.DataInputStream(dataBuffer);

            for (int i = 0; i < nextLinkArray.length; i++) {
                float val1=dataDataStream.readFloat();
                if(val1 > peakValues[i]*peakIndex[i]){
                    peakValues[i] = val1/peakIndex[i];
                    peakLocations[i]=j;
                }
            }

            dataBuffer.close();
            dataDataStream.close();

        }

        //Loading the Postgresql driver

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }

        //Determine if a connection can be established from this machine

        String url = "jdbc:postgresql://ut.iihr.uiowa.edu/afali_tmp";
        java.util.Properties props = new java.util.Properties();
        props.setProperty("user","afali");
        props.setProperty("password","");
        props.setProperty("loginTimeout","2");

        try {

            Connection conn = DriverManager.getConnection(url, props);

            System.out.println(">> Obtaining Object's list");

            Statement st = conn.createStatement();

            ResultSet rs;

            rs = st.executeQuery("SELECT count(*) FROM pois_adv");
            rs.next();
            int rowCount = rs.getInt(1);

            dbID_linkID=new int[2][rowCount];

            rs = st.executeQuery("SELECT id,link_id FROM pois_adv");
            java.util.Vector avaObjects=new java.util.Vector<String>();
            for(int kk=0;kk<rowCount;kk++) {
                rs.next();
                dbID_linkID[0][kk]=rs.getInt(1);
                dbID_linkID[1][kk]=rs.getInt(2)-2;
            }
            rs.close();

            st = conn.createStatement();

            int forecast=0;
            for(int kk=0;kk<rowCount;kk++) {
                if(dbID_linkID[1][kk] < peakValues.length){
                    forecast=peakValues[dbID_linkID[1][kk]]<0.8?0:peakValues[dbID_linkID[1][kk]]>1.2?2:1;
                    st.addBatch("UPDATE pois_adv SET forecast="+forecast+", forecast_time=now(), forecast_index="+peakValues[dbID_linkID[1][kk]]+" WHERE id="+dbID_linkID[0][kk]);
                }
            }

            st.executeBatch();

            conn.close();
            
            System.out.println(">> Database IFIS-1 Update Successfull");
            
            java.util.Date endTime = new java.util.Date();
            System.out.println(">> Update time Time: " + endTime.toString());



        } catch (SQLException ex) {
            System.out.println("Connection to UT server failed");
            ex.printStackTrace();
        }
        
        url = "jdbc:postgresql://ut.iihr.uiowa.edu/ifis";
        props = new java.util.Properties();
        props.setProperty("user","ifc_user");
        props.setProperty("password","ifc.iihr2008");
        props.setProperty("loginTimeout","2");

        try {

            Connection conn = DriverManager.getConnection(url, props);

            System.out.println(">> Obtaining Object's list");

            Statement st = conn.createStatement();

            ResultSet rs;

            rs = st.executeQuery("SELECT count(*) FROM pois_adv");
            rs.next();
            int rowCount = rs.getInt(1);

            dbID_linkID=new int[2][rowCount];

            rs = st.executeQuery("SELECT id,link_id FROM pois_adv");
            java.util.Vector avaObjects=new java.util.Vector<String>();
            for(int kk=0;kk<rowCount;kk++) {
                rs.next();
                dbID_linkID[0][kk]=rs.getInt(1);
                dbID_linkID[1][kk]=rs.getInt(2)-2;
            }
            rs.close();

            st = conn.createStatement();

            int forecast=0;
            for(int kk=0;kk<rowCount;kk++) {
                if(dbID_linkID[1][kk] < peakValues.length){
                    forecast=peakValues[dbID_linkID[1][kk]]<0.8?0:peakValues[dbID_linkID[1][kk]]>1.2?2:1;
                    st.addBatch("UPDATE pois_adv SET forecast="+forecast+", forecast_time=now(), forecast_index="+peakValues[dbID_linkID[1][kk]]+" WHERE id="+dbID_linkID[0][kk]);
                }
            }

            st.executeBatch();

            conn.close();
            
            System.out.println(">> Database IFIS-2 Update Successfull");
            
            java.util.Date endTime = new java.util.Date();
            System.out.println(">> Update time Time: " + endTime.toString());



        } catch (SQLException ex) {
            System.out.println("Connection to UT server failed");
            ex.printStackTrace();
        }
        
    }

    public void CreateIndex() throws IOException {

        try{

            float[] accumulators=new float[maxIndex];
            java.util.Arrays.fill(accumulators,1.0f);

            float[] maxWidthFunction=new float[maxIndex];

            System.out.println(">> Creating Index Convolution Files");

            new java.io.File(dirOut.getPath()+"/IndexConvolution/").mkdirs();

            System.out.println(">> Updating Convolution File # 0");

            outputDir = new FileOutputStream(dirOut.getPath()+"/IndexConvolution/convol-0");
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i = 0; i < nextLinkArray.length; i++) newOutputStream.writeFloat(accumulators[i]);

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

            for(int j=1;j<forecastHorizon*dicretizationOfHour*2;j++){

                float[] currentValues=new float[nextLinkArray.length];
                float[] previousValues=new float[nextLinkArray.length];

                System.out.println(">> Creating Index Convolution File # "+j);

                dataPath=new java.io.FileInputStream(dirOut.getPath()+"/IndexConvolution/convol-"+(j-1));
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

                outputDir = new FileOutputStream(dirOut.getPath()+"/IndexConvolution/convol-"+j);
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

        }
        catch(MalformedURLException e){
           e.printStackTrace();
        }


    }

    public static void main(String[] args) throws IOException {

        IndexPerformanceTestSetUp bigScript=new IndexPerformanceTestSetUp();
        bigScript.ExecuteIndexReset();
        
        System.exit(0);
        
        if(args[0].equalsIgnoreCase("reset")) bigScript.ExecuteIndexReset();
        if(args[0].equalsIgnoreCase("update")) bigScript.ExecuteIndexUpdate();
        if(args[0].equalsIgnoreCase("index")) bigScript.CreateIndex();
        
        System.exit(0);

    }

}

