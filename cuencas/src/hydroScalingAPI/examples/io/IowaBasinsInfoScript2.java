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

    int forecastHorizon=10;

    int nColsMP,nRowsMP,maxIndex;
    double minLonMP,minLatMP,lonResMP,latResMP;
    int [][] matrizPintada;
    float[] counters;
    int[] nextLinkArray;


    public IowaBasinsInfoScript2() throws IOException{
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

        System.out.println(">> Loading Next Link Array");

        java.io.File basinsLog=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/NextHillslopeIowa.csv");

        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(basinsLog));
        int numLinks=Integer.valueOf(fileMeta.readLine());
        nextLinkArray=new int[numLinks];
        for (int i = 0; i < nextLinkArray.length; i++) {
            nextLinkArray[i]=Integer.valueOf(fileMeta.readLine());

        }

        fileMeta.close();

    }

    public void Reset() throws java.io.IOException{

        System.out.println(">> Reseting Rainfall Remapping and Current Convolution Files");
        
        System.out.println(">> Creating Empty Convolution Files");
        
        new java.io.File(dirOut.getPath()+"/ConvolutionFiles/").mkdirs();

        outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/convol-0");
        bufferout=new BufferedOutputStream(outputDir);
        newOutputStream=new DataOutputStream(bufferout);

        for (int i=0;i<maxIndex;i++){
            newOutputStream.writeFloat(0.0f);
        }

        newOutputStream.close();
        bufferout.close();
        outputDir.close();

        for(int i=1;i<forecastHorizon*2;i++){

            System.out.println(">> Creating Convolution File # "+i);

            java.nio.channels.FileChannel inChannel = new
                FileInputStream(new java.io.File(dirOut.getPath()+"/ConvolutionFiles/convol-0")).getChannel();
            java.nio.channels.FileChannel outChannel = new
                FileOutputStream(new java.io.File(dirOut.getPath()+"/ConvolutionFiles/convol-"+i)).getChannel();

            inChannel.transferTo(0, inChannel.size(),
                    outChannel);

            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();

        }

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

                    if(matrizPintada[i][j]>0) if(matrix_rain[iData][jData]!=-9999) accumulators[matrizPintada[i][j]-1]+=matrix_rain[iData][jData];
                }
            }
            
            for (int i=0;i<maxIndex;i++) accumulators[i]/=counters[i];

            System.out.println(">> Moving Rainfall from File # "+kk);

            moveThisRainfall(accumulators,kk);

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

        


        System.out.println(">> Rainfall Accumulation Files Ready");

    }

    public void moveThisRainfall(float[] accumulators,int kk) throws IOException {

        float[] currentValues=new float[accumulators.length];
        
        dataPath=new java.io.FileInputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+kk);
        dataBuffer=new java.io.BufferedInputStream(dataPath);
        dataDataStream=new java.io.DataInputStream(dataBuffer);

        for (int i = 0; i < accumulators.length; i++) currentValues[i]=dataDataStream.readFloat();


        dataBuffer.close();
        dataDataStream.close();

        outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+kk);
        bufferout=new BufferedOutputStream(outputDir);
        newOutputStream=new DataOutputStream(bufferout);

        for (int i = 0; i < accumulators.length; i++) newOutputStream.writeFloat(accumulators[i]+currentValues[i]);

        newOutputStream.close();
        bufferout.close();
        outputDir.close();
        
        int[] nextLinkOneHour=(int[])nextLinkArray.clone();
        for(int j=kk+1;j<forecastHorizon*2;j++){

            System.out.println(">> >> To Convolution File # "+j);

            dataPath=new java.io.FileInputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+j);
            dataBuffer=new java.io.BufferedInputStream(dataPath);
            dataDataStream=new java.io.DataInputStream(dataBuffer);

            for (int i = 0; i < accumulators.length; i++) currentValues[i]=dataDataStream.readFloat();


            dataBuffer.close();
            dataDataStream.close();

            float[] updatedValues=new float[accumulators.length];
            for (int i = 0; i < accumulators.length; i++) {
                if(nextLinkOneHour[i] != -1) updatedValues[nextLinkOneHour[i]]=updatedValues[nextLinkOneHour[i]]+accumulators[i];
            }


            outputDir = new FileOutputStream(dirOut.getPath()+"/ConvolutionFiles/convol-"+j);
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i = 0; i < accumulators.length; i++) newOutputStream.writeFloat(updatedValues[i]);

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

            nextLinkOneHour=nextLinkOneHour(nextLinkOneHour);
        }
    }

    private int[] nextLinkOneHour(int[] nextLinkOneHour){

        //This method needs to be rewriten to give back the hillslope one hour away from the current hillslope

        for (int i = 0; i < nextLinkOneHour.length; i++) {
            if(nextLinkOneHour[i] != -1) nextLinkOneHour[i]=nextLinkArray[nextLinkOneHour[i]];

        }
        return nextLinkArray;
    }

    public void ReloadRealTimeRainfall() throws IOException {

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

            System.out.println(">> Opening connection: "+"http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/"+mostRecentFile);
            file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/speed/60/"+mostRecentFile);
            urlConn = file.openConnection();


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

            System.out.println(">> Reading Real-Time Rainfall Data");

            matrix_rain = new float[numRow_Rain][numCol_Rain];
            
            for (int i = numRow_Rain-1; i >= 0; i--) {

                line = is.readLine();

                if(line == null) {
                    System.out.println(line);
                    System.out.println(">> File is empty... Aborting update process");
                    return;
                }
                line=line.trim();
                java.util.StringTokenizer linarray = new java.util.StringTokenizer(line);


                for (int j = 0; j < numCol_Rain; j++) {

                    float f = 0;
                    try {
                        f = Float.valueOf(linarray.nextToken()).floatValue();
                    } catch (NumberFormatException nfe) {
                        System.out.println("NFE" + nfe.getMessage());
                    }

                    if(f > 0.0 && f < 60.0) matrix_rain[i][j] = f;

                }
            }

            is.close();
            xover.close();
            gzis.close();

            System.out.println(">> Matrix Read!");

            System.out.println(">> Updating Accumulations Files");

            float[][] dataFloat=new float[numRow_Rain][numCol_Rain];

            for (int k=500;k>1;k--){

                System.out.println(">> Working on File # "+k);

                dataPath=new java.io.FileInputStream("/Users/ricardo/rawData/RainfallAccumulations/Current/accum"+(k-1));
                dataBuffer=new java.io.BufferedInputStream(dataPath);
                dataDataStream=new java.io.DataInputStream(dataBuffer);

                for (int i=0;i<numRow_Rain;i++) for(int j=0;j<numCol_Rain;j++) {
                    dataFloat[i][j]=dataDataStream.readFloat();
                }


                dataBuffer.close();
                dataDataStream.close();

                outputDir = new FileOutputStream(dirOut.getPath()+"/Current/accum"+k);
                bufferout=new BufferedOutputStream(outputDir);
                newOutputStream=new DataOutputStream(bufferout);

                for (int i=0;i<numRow_Rain;i++) for(int j=0;j<numCol_Rain;j++) {
                    newOutputStream.writeFloat(dataFloat[i][j]+matrix_rain[i][j]);
                }

                newOutputStream.close();
                bufferout.close();
                outputDir.close();

            }

             System.out.println(">> Working on File # 1");

            outputDir = new FileOutputStream(dirOut.getPath()+"/Current/accum1");
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i=0;i<numRow_Rain;i++) for(int j=0;j<numCol_Rain;j++) {
               newOutputStream.writeFloat(matrix_rain[i][j]);
            }

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

            System.out.println(">> Accumulations Files Update Completed");

            System.out.println(">> Writing Accumulations Files to Web Format");

            WebWriter wr1=new WebWriter(mostRecentFile);
            wr1.run();

            System.out.println(">> Writing Accumulations Files to Web Format completed");

        }
        catch(MalformedURLException e){
           e.printStackTrace();
        }

    }

    public void RecreateKMLs() throws IOException {

        System.out.println(">> Recreating KMLs");


        new File(OutputPath).mkdirs();
        new File(OutputPath+"/polygons/").mkdirs();
        new File(OutputPath+"/networks/").mkdirs();

        File dir = new File(KMLsPath);

        File[] files = dir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
        File[] inCity1,inCity2,inCity3,inCity4;

        String tableRadek="City Name;River Name;Latitude;Longitude;File Polygon;File Network;Westernmost Longitude;Southernmost Longitude;Easternmost Longitude;Northernmost Latitude"+ret;
        
        for (int i = 0; i < files.length; i++) {
        
            inCity1 = files[i].listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter("Basin","txt.gz"));
            inCity2 = files[i].listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter("Divide","kml"));
            inCity3 = files[i].listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter("RiverNetworkLowRes","kml"));
            inCity4 = files[i].listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter("InfoFile","txt"));

            for (int j = 0; j < inCity1.length; j++) {

                FileInputStream fin;
                GZIPInputStream gzis;
                InputStreamReader xover;
                BufferedReader is;

                fin = new FileInputStream(inCity4[j]);
                xover = new InputStreamReader(fin);
                is = new BufferedReader(xover);

                is.readLine();

                double latitude=hydroScalingAPI.tools.DMSToDegrees.getDegrees(is.readLine().split(">")[2].trim());
                double longitude=hydroScalingAPI.tools.DMSToDegrees.getDegrees(is.readLine().split(">")[2].trim());

                is.readLine();
                is.readLine();

                double responseTime=Double.parseDouble(is.readLine().split(">")[2].trim());

                is.close();
                xover.close();
                fin.close();

                long fileIndex=Math.max(1L,Math.round(responseTime));

                System.out.println(">> Recreating file "+inCity2[j].getName());
                System.out.println(">> >> /Users/ricardo/rawData/RainfallAccumulations/Current/accum"+fileIndex);

                dataPath=new java.io.FileInputStream("/Users/ricardo/rawData/RainfallAccumulations/Current/accum"+fileIndex);
                dataBuffer=new java.io.BufferedInputStream(dataPath);
                dataDataStream=new java.io.DataInputStream(dataBuffer);

                for (int ii=0;ii<numRow_Rain;ii++) for(int jj=0;jj<numCol_Rain;jj++) {
                    matrix_rain[ii][jj]=dataDataStream.readFloat();
                    //matrix[ii][jj] = (float)Math.random()+(float)ii/(float)numRow_Rain*10.0f;
                }


                dataBuffer.close();
                dataDataStream.close();

                float averageValue = 0.0f;
                float averageValueTimeMachine = 0.0f;
                float numElements = 0.0f;

                java.io.RandomAccessFile[] accumFiles=new java.io.RandomAccessFile[500];
                for (int k = 0; k < accumFiles.length; k++) {
                    java.io.File rutaQuantity=new java.io.File("/Users/ricardo/rawData/RainfallAccumulations/Current/accum"+(k+1));
                    accumFiles[k]=new java.io.RandomAccessFile(rutaQuantity,"r");
                }

                //Open the File for reading

                fin = new FileInputStream(inCity1[j]);
                gzis = new GZIPInputStream(fin);
                xover = new InputStreamReader(gzis);
                is = new BufferedReader(xover);

                String line2;
                String line3;
                String linarray2[] = null;

                line3 = is.readLine();

                float minX=Float.MAX_VALUE;
                float minY=Float.MAX_VALUE;
                float maxX=-Float.MAX_VALUE;
                float maxY=-Float.MAX_VALUE;

                while ((line2 = is.readLine()) != null) {
                    linarray2 = line2.split(",");

                    float xLon=Float.parseFloat(linarray2[0].trim());
                    float yLat=Float.parseFloat(linarray2[1].trim());
                    int tTime=(int)Math.floor(Float.parseFloat(linarray2[2].trim())*1000/0.75/3600.0)+1;

                    int xxx = (int)((xLon-minLon_Rain)/matRes_Rain);
                    int yyy = (int)((yLat-minLat_Rain)/matRes_Rain);

                    averageValue += matrix_rain[yyy][xxx];
                    numElements++;

                    minX=Math.min(minX, xLon);
                    minY=Math.min(minY, yLat);
                    maxX=Math.max(maxX, xLon);
                    maxY=Math.max(maxY, yLat);

                    if(numElements%10==0){

                        if(tTime > 1){
                            accumFiles[tTime].seek(4*(yyy*matrix_rain[0].length+xxx));
                            accumFiles[tTime-1].seek(4*(yyy*matrix_rain[0].length+xxx));
                            averageValueTimeMachine+=(float) (accumFiles[tTime].readFloat()-accumFiles[tTime-1].readFloat());
                        } else {
                            accumFiles[tTime].seek(4*(yyy*matrix_rain[0].length+xxx));
                            averageValueTimeMachine+=(float) accumFiles[tTime].readFloat();
                        }

                    }

                }

                averageValue/=numElements;
                averageValueTimeMachine/=(numElements/10);

                double averageValueInches=averageValue*0.039370079;

                averageValue=Math.round(averageValue*100)/100.0f;
                averageValueTimeMachine=Math.round(averageValueTimeMachine*100)/100.0f;

                double responseTimeMinutes=responseTime*60;

                double alpha = 0.0,Tr=0.0;

                if(averageValue > 0){
                    alpha=Math.pow( 1/ averageValueInches * 0.587 *0.5865 * Math.exp(0.0077*Math.pow(Math.log(responseTimeMinutes),3) - 0.1566*Math.pow(Math.log(responseTimeMinutes),2) + 1.2369*Math.log(responseTimeMinutes) - 2.4084),4.06);
                    alpha=Math.min(1,alpha);
                    Tr = 1/ alpha / 12;
                }

                averageValueInches=Math.round(averageValueInches*100)/100.0;
                
                is.close();
                xover.close();
                gzis.close();
                fin.close();

                for (int k = 0; k < accumFiles.length; k++) {
                    accumFiles[k].close();
                }

                System.out.println(">> >> Rates for this basin "+"Rain Rate: "+averageValue+" [mm] "+averageValueTimeMachine+" [mm] ");

                String[] uniqueIdentifier=inCity1[j].getName().split("_");
                String[] cityName=uniqueIdentifier[1].split(" \\(");
                String[] riverName=cityName[1].split("\\)");

                String webSafeName=cityName[0].replaceAll(" ", "_").toLowerCase()+"_"+riverName[0].replaceAll(" ", "_").replaceAll(",","").toLowerCase();

                fin = new FileInputStream(inCity2[j]);
                xover = new InputStreamReader(fin);
                is = new BufferedReader(xover);

                fileSalida=new java.io.File(OutputPath+"/polygons/"+webSafeName+".kmz");
                String polyWebAddress="http://www.iihr.uiowa.edu/~ricardo/temp/iowa_basins_data/"+new java.io.File(OutputPath).getName()+"/polygons/"+webSafeName+".kmz";

                outputDir = new java.io.FileOutputStream(fileSalida);
                java.util.zip.ZipOutputStream outputComprim=new java.util.zip.ZipOutputStream(outputDir);
                bufferout=new java.io.BufferedOutputStream(outputComprim);

                outputComprim.putNextEntry(new ZipEntry(webSafeName+".kml"));

                while ((line2 = is.readLine()) != null) {

                    if(line2.equalsIgnoreCase("<PolyStyle><color>e6ffffff</color></PolyStyle>")){

                        //Green
                        if(averageValueTimeMachine > 0.00) line2="<PolyStyle><color>be22c122</color></PolyStyle>";
                        //Orange
                        if(averageValueTimeMachine > 1.00) line2="<PolyStyle><color>be0062ff</color></PolyStyle>";
                        //Red
                        if(averageValueTimeMachine > 2.00) line2="<PolyStyle><color>be2023ff</color></PolyStyle>";
                        
                    }

                    if(line2.startsWith("   <Placemark><description>") && line2.endsWith("<styleUrl>#Point</styleUrl>")){
                        int indOfString=line2.indexOf("]]></description>");
                        line2=line2.substring(0, indOfString)+"<br><br><b>Accumulated Rainfall over Residence Time</b><br>"+averageValue+" mm ("+averageValueInches+" in)<br>"+"<b>Return Period</b><br>"+Math.max(1,(int)Math.round(Tr))+" years <br>"+"<b>Flood Index</b><br>"+averageValueTimeMachine+"<br><br>]]></description><styleUrl>#Point</styleUrl>";
                    }

                    if(line2.startsWith("<href>http://weather.iihr.uiowa.edu/ifc/graphics/dots32/real00dot.png</href>")){

                        //Green
                        if(averageValueTimeMachine > 0.00) line2="<href>http://weather.iihr.uiowa.edu/ifc/graphics/dots32/l1.png</href>";
                        //Blue
                        if(averageValueTimeMachine > 1.00) line2="<href>http://weather.iihr.uiowa.edu/ifc/graphics/dots32/l2.png</href>";
                        //Yellow
                        if(averageValueTimeMachine > 2.00) line2="<href>http://weather.iihr.uiowa.edu/ifc/graphics/dots32/l3.png</href>";
                        //Orange
                        if(averageValueTimeMachine > 3.00) line2="<href>http://weather.iihr.uiowa.edu/ifc/graphics/dots32/l4.png</href>";
                        //Red
                        if(averageValueTimeMachine > 4.00) line2="<href>http://weather.iihr.uiowa.edu/ifc/graphics/dots32/l5.png</href>";

                    }

                    bufferout.write(line2.getBytes());

                }

                bufferout.close();
                outputComprim.close();
                outputDir.close();

                System.out.println(">> >> Calculations for this basin "+"Rain Rate: "+averageValueInches+" [in]"+" Response Time "+responseTime+" [hr] Frequency "+alpha+" [*] Return Period "+Tr+" [months]");

                is.close();
                xover.close();
                fin.close();

                fin = new FileInputStream(inCity3[j]);
                xover = new InputStreamReader(fin);
                is = new BufferedReader(xover);


                fileSalida=new java.io.File(OutputPath+"/networks/"+webSafeName+".kmz");
                String netWebAddress="http://www.iihr.uiowa.edu/~ricardo/temp/iowa_basins_data/"+new java.io.File(OutputPath).getName()+"/networks/"+webSafeName+".kmz";

                outputDir = new java.io.FileOutputStream(fileSalida);
                outputComprim=new java.util.zip.ZipOutputStream(outputDir);
                bufferout=new java.io.BufferedOutputStream(outputComprim);

                outputComprim.putNextEntry(new ZipEntry(webSafeName+".kml"));

                while ((line2 = is.readLine()) != null) {

                    bufferout.write(line2.getBytes());

                }

                bufferout.close();
                outputComprim.close();
                outputDir.close();

                is.close();
                xover.close();
                fin.close();

                tableRadek+=cityName[0]+";"+riverName[0]+";"+latitude+";"+longitude+";"+polyWebAddress+";"+netWebAddress+";"+minX+";"+minY+";"+maxX+";"+maxY+ret;

            }

        }

        System.out.println(">> KMLs updated");

        System.out.println(">> Updating Description Table");

        fileSalida=new java.io.File(OutputPath+"/description.txt");
        outputDir = new java.io.FileOutputStream(fileSalida);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);

        newfile.write(tableRadek);

        newfile.close();
        bufferout.close();
        outputDir.close();

        System.out.println(">> Current Update Completed");
    }

    public static void main(String[] args) throws IOException {

        new IowaBasinsInfoScript2().Reset();
        System.exit(0);

    }

}

