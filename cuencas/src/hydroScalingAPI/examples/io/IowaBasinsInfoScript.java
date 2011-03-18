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

public class IowaBasinsInfoScript{

    java.io.FileInputStream dataPath;
    java.io.BufferedInputStream dataBuffer;
    java.io.DataInputStream dataDataStream;

    float[][] matrix = new float[1057][1741];

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

    java.io.File dirOut=new java.io.File("/Users/ricardo/rawData/RainfallAccumulations");

    String KMLsPath, OutputPath;


    public IowaBasinsInfoScript(int action) throws IOException{
        switch (action){
            case 0:Reset(); break;
            case 1:ReloadRealTimeRainfall(); break;
        }

    }

    public IowaBasinsInfoScript(String KMLsPath1,String OutputPath1) throws IOException {
        KMLsPath=KMLsPath1;
        OutputPath=OutputPath1;

        RecreateKMLs();
        System.out.println(">> Waiting for next real-time rainfall field");
        
        System.out.println(">> Process Completed");
        
    }

    public void Reset() throws java.io.IOException{

        String[] badFiles={
//                            "what"};
//                            "H99999999_R6007_G_06APR2010_060000.out.gz",
//                            "H99999999_R6003_G_05APR2010_160000.out.gz",
//                            "H99999999_R6003_G_05APR2010_154000.out.gz",
                            "H99999999_R6007_G_03APR2010_120000.out.gz",
                            "H99999999_R6007_G_03APR2010_060000.out.gz",
                            "H99999999_R6007_G_03APR2010_010000.out.gz",
                            "H99999999_R6003_G_02APR2010_110500.out.gz",
                            "H99999999_R6004_G_02APR2010_090500.out.gz",
                            "H99999999_R6007_G_02APR2010_080500.out.gz",
                            "H99999999_R6007_G_02APR2010_070500.out.gz",
                            "H99999999_R6007_G_02APR2010_060500.out.gz",
                            "H99999999_R6007_G_02APR2010_050500.out.gz",
                            "H99999999_R6002_G_02APR2010_040500.out.gz",
                            "H99999999_R6003_G_02APR2010_030500.out.gz",
                            "H99999999_R6007_G_01APR2010_200500.out.gz",
                            "H99999999_R6007_G_01APR2010_131000.out.gz",
                            "H99999999_R6005_G_01APR2010_101000.out.gz",
                            "H99999999_R6002_G_01APR2010_091000.out.gz",
                            "H99999999_R6003_G_01APR2010_031000.out.gz",
                            "H99999999_R6003_G_31MAR2010_221000.out.gz",
                            "H99999999_R6007_G_31MAR2010_201000.out.gz",
                            "H99999999_R6003_G_31MAR2010_091000.out.gz",
                            "H99999999_R6007_G_31MAR2010_081000.out.gz",
                            "H99999999_R6007_G_31MAR2010_071000.out.gz",
                            "H99999999_R6003_G_30MAR2010_221000.out.gz",
                            "H99999999_R6004_G_30MAR2010_211000.out.gz",
                            "H99999999_R6007_G_30MAR2010_201000.out.gz"};

        System.out.println(">> Reseting Rainfall Accumulation Files");

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
            boolean inList=false;
            for (int i = 0; i < badFiles.length; i++) inList|=line.equalsIgnoreCase(badFiles[i]);
            if(!inList)availableMapsOfRain.add(line);
            line = is.readLine();
        }

        is.close();
        xover.close();

        int numMaps=availableMapsOfRain.size();

        int kk=1;

        matrix=new float[numRow_Rain][numCol_Rain];

        for (int ff = numMaps-1; ff >= 0; ff--) {

            String mostRecentFile=availableMapsOfRain.get(ff);

            System.out.println(">> Opening connection: "+"http://s-iihr57.iihr.uiowa.edu/ricardo/quality/60/"+mostRecentFile);
            file = new URL("http://s-iihr57.iihr.uiowa.edu/ricardo/quality/60/"+mostRecentFile);
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

            System.out.println(">> Reading High Quality Rainfall Data");

            line = is.readLine();

            if(line == null) {
                System.out.println(">> File is empty... Aborting update process");

            } else {

                for (int i = numRow_Rain-1; i >= 0; i--) {

                    java.util.StringTokenizer linarray = new java.util.StringTokenizer(line);


                    for (int j = 0; j < numCol_Rain; j++) {

                        float f = 0;
                        try {
                            f = Float.valueOf(linarray.nextToken()).floatValue();
                        } catch (NumberFormatException nfe) {
                            System.out.println("NFE" + nfe.getMessage());
                        }

                        if(f > 0.0 && f < 100.0) matrix[i][j] += f;

                    }

                    line = is.readLine();

                }
            }

            is.close();
            xover.close();
            gzis.close();
            
            System.out.println(">> Working on File # "+kk);

            outputDir = new FileOutputStream(dirOut.getPath()+"/Current/accum"+kk);
            bufferout=new BufferedOutputStream(outputDir);
            newOutputStream=new DataOutputStream(bufferout);

            for (int i=0;i<numRow_Rain;i++) for(int j=0;j<numCol_Rain;j++) {
                newOutputStream.writeFloat(matrix[i][j]);
            }

            newOutputStream.close();
            bufferout.close();
            outputDir.close();

            kk++;

            if(kk == 500) break;

        }

        for(int i=kk+1;i<=500;i++){

            System.out.println(">> Working on File # "+i);

            java.nio.channels.FileChannel inChannel = new
                FileInputStream(new java.io.File(dirOut.getPath()+"/Current/accum"+(kk-1))).getChannel();
            java.nio.channels.FileChannel outChannel = new
                FileOutputStream(new java.io.File(dirOut.getPath()+"/Current/accum"+i)).getChannel();

            inChannel.transferTo(0, inChannel.size(),
                    outChannel);

            if (inChannel != null) inChannel.close();
            if (outChannel != null) outChannel.close();

        }


        System.out.println(">> Rainfall Accumulation Files Ready");

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

            matrix = new float[numRow_Rain][numCol_Rain];
            
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

                    if(f > 0.0 && f < 60.0) matrix[i][j] = f;

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
                    newOutputStream.writeFloat(dataFloat[i][j]+matrix[i][j]);
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
               newOutputStream.writeFloat(matrix[i][j]);
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

        String tableRadek="City Name (River Name);Latitude;Longitude;File Polygon;File Network;Westernmost Longitude;Southernmost Longitude;Easternmost Longitude;Northernmost Latitude;Upstream Area [km^2];Response Time [hr]"+ret;
        
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

                double UpstreamArea=Double.parseDouble(is.readLine().split("b>")[2].trim());

                is.readLine();

                double responseTime=Double.parseDouble(is.readLine().split("b>")[2].trim());

                is.close();
                xover.close();
                fin.close();

                long fileIndex=Math.max(1L,Math.round(responseTime));

                System.out.println(">> Recreating file "+inCity2[j].getName());
                System.out.println(">> >> /Users/ricardo/rawData/RainfallAccumulations/Current/accum"+fileIndex);

                dataPath=new java.io.FileInputStream("/Users/ricardo/rawData/RainfallAccumulations/Current/accum"+fileIndex);
                dataBuffer=new java.io.BufferedInputStream(dataPath);
                dataDataStream=new java.io.DataInputStream(dataBuffer);
//
//                for (int ii=0;ii<numRow_Rain;ii++) for(int jj=0;jj<numCol_Rain;jj++) {
//                    matrix[ii][jj]=dataDataStream.readFloat();
//                    //matrix[ii][jj] = (float)Math.random()+(float)ii/(float)numRow_Rain*10.0f;
//                }


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

                //********** LINES ADDED TO WRITE LOOKUP TABLES FOR EACH BASIN

                fileSalida=new java.io.File("/Users/ricardo/temp/masks/"+inCity1[j].getName()+".txt");

                outputDir = new java.io.FileOutputStream(fileSalida);
                bufferout=new java.io.BufferedOutputStream(outputDir);
                newfile=new java.io.OutputStreamWriter(bufferout);


                String line2;
                String line3;
                String linarray2[] = null;

                line3 = is.readLine();

                float minX=Float.MAX_VALUE;
                float minY=Float.MAX_VALUE;
                float maxX=-Float.MAX_VALUE;
                float maxY=-Float.MAX_VALUE;

//                while ((line2 = is.readLine()) != null) {
//                    linarray2 = line2.split(",");
//
//                    float xLon=Float.parseFloat(linarray2[0].trim());
//                    float yLat=Float.parseFloat(linarray2[1].trim());
//                    int tTime=(int)Math.floor(Float.parseFloat(linarray2[2].trim())*1000/0.75/3600.0)+1;
//
//                    int xxx = (int)((xLon-minLon_Rain)/matRes_Rain);
//                    int yyy = (int)((yLat-minLat_Rain)/matRes_Rain);
//
//                    newfile.write(""+xxx+" "+yyy+"\n");
//
//                    averageValue += matrix[yyy][xxx];
//                    numElements++;
//
//                    minX=Math.min(minX, xLon);
//                    minY=Math.min(minY, yLat);
//                    maxX=Math.max(maxX, xLon);
//                    maxY=Math.max(maxY, yLat);
//
//                    if(numElements%10==0){
//
//                        if(tTime > 1){
//                            accumFiles[tTime].seek(4*(yyy*matrix[0].length+xxx));
//                            accumFiles[tTime-1].seek(4*(yyy*matrix[0].length+xxx));
//                            averageValueTimeMachine+=(float) (accumFiles[tTime].readFloat()-accumFiles[tTime-1].readFloat());
//                        } else {
//                            accumFiles[tTime].seek(4*(yyy*matrix[0].length+xxx));
//                            averageValueTimeMachine+=(float) accumFiles[tTime].readFloat();
//                        }
//
//                    }
//
//                }

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

                newfile.close();
                bufferout.close();
                outputDir.close();
                
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
                String polyWebAddress="http://www.iihr.uiowa.edu/~ricardo/temp/iowa_basins_data1/"+new java.io.File(OutputPath).getName()+"/polygons/"+webSafeName+".kmz";

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
                String netWebAddress="http://www.iihr.uiowa.edu/~ricardo/temp/iowa_basins_data1/"+new java.io.File(OutputPath).getName()+"/networks/"+webSafeName+".kmz";

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

                tableRadek+=cityName[0]+" ("+riverName[0]+");"+latitude+";"+longitude+";"+polyWebAddress+";"+netWebAddress+";"+minX+";"+minY+";"+maxX+";"+maxY+";"+UpstreamArea+";"+responseTime+ret;

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

        String[] command;
        java.lang.Process localProcess0;

        if(args.length == 0) {

            command=new String[] {  System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
                                    "-Xmx1500m",
                                    "-Xrs",
                                    "-cp",
                                    System.getProperty("java.class.path"),
                                    "hydroScalingAPI.examples.io.IowaBasinsInfoScript",
                                    "reload-rain"};
//            localProcess0=java.lang.Runtime.getRuntime().exec(command);
//
//            String concat0="";
//
//            boolean monitor0=true;
//
//            while(monitor0){
//                String s1=new String(new byte[] {Byte.parseByte(""+localProcess0.getInputStream().read())});
//                concat0+=s1;
//                if(s1.equalsIgnoreCase("\n")) {
//                    System.out.print("Processor 0: "+concat0);
//                    if(concat0.substring(0, Math.min(54,concat0.length())).equalsIgnoreCase(">> Writing Accumulations Files to Web Format completed")) monitor0=false;
//                    concat0="";
//                }
//            }

            String[][] kmlInAndOut=new String[][] {
                                                   {"/Users/ricardo/rawData/BasinMasks/usgs_gauges/","/Volumes/ricardo/temp/iowa_basins_data1/usgs_gauges/"},
//                                                   {"/Users/ricardo/rawData/BasinMasks/large_cities/","/Volumes/ricardo/temp/iowa_basins_data1/large_cities/"},
//                                                   {"/Users/ricardo/rawData/BasinMasks/medium_cities/","/Volumes/ricardo/temp/iowa_basins_data1/medium_cities/"},
//                                                   {"/Users/ricardo/rawData/BasinMasks/small_cities/","/Volumes/ricardo/temp/iowa_basins_data1/small_cities/"},
//                                                   {"/Users/ricardo/rawData/BasinMasks/ifc_sensors/","/Volumes/ricardo/temp/iowa_basins_data1/ifc_sensors/"}
                                                  };


            java.lang.Process[] localProcess=new java.lang.Process[kmlInAndOut.length];
            String[] concat=new String[kmlInAndOut.length];
            boolean[] monitor= new boolean[kmlInAndOut.length]; java.util.Arrays.fill(monitor, true);

            for (int i = 0; i < kmlInAndOut.length; i++) {
                command=new String[] {  System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
                                    "-Xmx1500m",
                                    "-Xrs",
                                    "-cp",
                                    System.getProperty("java.class.path"),
                                    "hydroScalingAPI.examples.io.IowaBasinsInfoScript",
                                    kmlInAndOut[i][0],
                                    kmlInAndOut[i][1]};

                localProcess[i]=java.lang.Runtime.getRuntime().exec(command);
            }

            boolean keepGoing=true;

            while(keepGoing){
                keepGoing=false;
                for (int i = 0; i < monitor.length; i++) {
                    String s1=new String(new byte[] {Byte.parseByte(""+localProcess[i].getInputStream().read())});
                    concat[i]+=s1;
                    if(s1.equalsIgnoreCase("\n") && monitor[i]) {
                        System.out.print("Processor "+i+": "+concat[i]);
                        if(concat[i].substring(0, Math.min(20,concat[i].length())).equalsIgnoreCase(">> Process Completed")) monitor[i]=false;
                        concat[i]="";
                    }
                    keepGoing|=monitor[i];
                }

            }
            System.exit(0);

        } else {

            if(args[0].equalsIgnoreCase("reset")){
                new IowaBasinsInfoScript(0);
                System.exit(0);
            }

            if(args[0].equalsIgnoreCase("reload-rain")){
                new IowaBasinsInfoScript(1);
                System.exit(0);
            }

            new IowaBasinsInfoScript(args[0],args[1]);

            System.exit(0);
        }

    }

}






class WebWriter implements Runnable {

    java.io.FileInputStream dataPath;
    java.io.BufferedInputStream dataBuffer;
    java.io.DataInputStream dataDataStream;

    String ret = "\n";
    String timeStamp="";

    String KMLsPath, OutputPath;


    int numCol_Rain=1741;
    int numRow_Rain=1057;

    public WebWriter(String ts) {

        timeStamp=ts;

    }

    public void run() { // thread dies when finished
        try {
            System.out.println(">> Rewriting Accumulations Files for Web Display");

            OutputPath = "/Volumes/ricardo/temp/iowa_basins_data1/";
            new java.io.File(OutputPath + "/accumulations/").mkdirs();

            float[][] dataFloat=new float[numRow_Rain][numCol_Rain];

            for (int k = 1; k <= 500; k++) {

                if(k == 1 || k == 3 || k == 6 || k == 24 || k == 168){

                    dataPath = new java.io.FileInputStream("/Users/ricardo/rawData/RainfallAccumulations/Current/accum" + k);
                    dataBuffer = new java.io.BufferedInputStream(dataPath);
                    dataDataStream = new java.io.DataInputStream(dataBuffer);

                    for (int i = 0; i < numRow_Rain; i++) {
                        for (int j = 0; j < numCol_Rain; j++) {
                            dataFloat[i][j] = dataDataStream.readFloat();
                        }
                    }


                    dataBuffer.close();
                    dataDataStream.close();

                    System.out.println(OutputPath + "/accumulations/accum" + k + ".out.gz");

                    java.io.FileOutputStream outputLocal=new java.io.FileOutputStream(new java.io.File(OutputPath + "/accumulations/accum" + k + ".out.gz"));
                    java.util.zip.GZIPOutputStream outputComprim=new java.util.zip.GZIPOutputStream(outputLocal);
                    java.io.BufferedWriter newfile = new java.io.BufferedWriter(new java.io.OutputStreamWriter(outputComprim));

                    int nc = numCol_Rain;
                    int nr = numRow_Rain;

                    float missing = -99.99f;
                    String retorno = "\n";

                    newfile.write("# file name: "+timeStamp + retorno);
                    newfile.write("# Accumulation map [mm] " + retorno);
                    newfile.write("# Accumulation time [sec]: 3600" + retorno);
                    newfile.write("# number of columns: 1741" + retorno);
                    newfile.write("# number of rows: 1057" + retorno);
                    newfile.write("# grid: LATLON " + retorno);
                    newfile.write("# upper-left LATLONcorner(x,y): 6924 5409" + retorno);
                    newfile.write("# xllcorner [lon]: -97.154167" + retorno);
                    newfile.write("# yllcorner [lat]: 40.133331" + retorno);
                    newfile.write("# cellsize [dec deg]: 0.004167" + retorno);
                    newfile.write("# no data value: -99.0" + retorno);


                    for (int i = (nr - 1); i >= 0; i--) {
                        for (int j = 0; j < nc; j++) {
                            if (dataFloat[i][j] == missing) {
                                newfile.write(missing + " ");
                            } else {
                                newfile.write(dataFloat[i][j] + " ");
                            }
                        }
                        newfile.write(retorno);
                    }

                    newfile.close();
                    outputComprim.close();
                    outputLocal.close();
                }

            }
        } catch (IOException iOException) {
        }
    }
}
