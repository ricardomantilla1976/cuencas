/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.io;

/**
 * The purpose of this program is to find the pixel xy-coordinate that serves as the
 * location of a stream gauge in a basin. The target location is a lat-lon coordinate read from a USGS
 * file of streamflow data. The xy-coordinates found for a given gauge are written to a .log.auto file for
 * the basin. A name or reference (e.g. gauge id) follows each xy-coordinate put in the log file.
 * A group of possible xy-coordinates are found if a single coordinate is not resolved.
 * If more than one xy-coordinate is found for a given gauge, a suffix "- alternate" follows the gauge name
 * for those coordinates that are not the best choice determined by the algorithm. Ultimately, it is up
 * to the user to determine which xy-coordinate is the most accurate.
 * @author Furey
 */

import java.io.*;

public class streamgaugeXYToLogFile {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;

    public streamgaugeXYToLogFile(String nameOfDEM, String pathToTopography, int x, int y, byte[][] direcc, hydroScalingAPI.io.MetaRaster md, String[][] gaugeInfo) throws java.io.IOException{
        matDir=direcc;
        metaDatos=md;
        // (x,y) coordinate belows is for the largest basin within the DEM
        System.out.println("Accessing basin data ...");
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);

        System.out.println("Basin Order = "+linksStructure.getBasinOrder());

        // Assess gauge Info locations
        float[][] lonlatBasin=myCuenca.getLonLatBasin();
        int[][] xyBasin=myCuenca.getXYBasin();
        double lonGauge, latGauge;
        double[] lonDifference=new double[lonlatBasin[0].length];
        double minLonDifference;
        double[] latDifference=new double[lonlatBasin[1].length];
        double minLatDifference;
        int colOfMatch=-1;
        int[][] xyClosest=new int[2][1];
        int[][] xyPatch; //=new int[2][81];  // represents a 9x9 array where center will be xyClosest
        int patchID;
        int patchWidthFactor;
        int patchWidth;
        int[] xyResSimID;
        int xyNumber;
        int[][] xySelected;
        int xySelectedNumber;
        float[][] areas;
        float[][] xyArea;
        float areaGauge;
        float[][] areaDifference;
        float areaDifferenceSum;
        float minAreaDifference;
        int[][] idSortedArea;
        String outputToLogFile;
        for (int i=0;i<gaugeInfo[0].length;i++) {  //i=0;i<gaugeInfo[0].length, i=7;i<8
            System.out.println("WORKING ON GAUGE: "+gaugeInfo[0][i]);
            lonGauge=hydroScalingAPI.tools.DMSToDegrees.getDegrees(gaugeInfo[2][i]);
            latGauge=hydroScalingAPI.tools.DMSToDegrees.getDegrees(gaugeInfo[1][i]);
            // Find closest latlon ...
            minLonDifference=1.0; // 1 degree
            minLatDifference=1.0; // 1 degree
            for (int j=0;j<lonlatBasin[0].length;j++){
                lonDifference[j]=Math.abs(lonGauge-lonlatBasin[0][j]);
                if (lonDifference[j]<minLonDifference) minLonDifference=lonDifference[j];
                latDifference[j]=Math.abs(latGauge-lonlatBasin[1][j]);
                if (latDifference[j]<minLatDifference) minLatDifference=latDifference[j];
            }
            for (int j=0;j<lonlatBasin[0].length;j++){
                if (lonDifference[j]==minLonDifference && latDifference[j]==minLatDifference) colOfMatch=j;
            }
            // Find xy of closest latlon ...
            xyClosest[0][0]=xyBasin[0][colOfMatch]; // x, center of patch
            xyClosest[1][0]=xyBasin[1][colOfMatch]; // y, center of patch
            // Make patch of xy values   ******
            xySelectedNumber=0;
            patchWidthFactor=1;
            patchID=-1;
            while (xySelectedNumber==0){
                patchWidthFactor=patchWidthFactor+2;
                patchWidth=3*patchWidthFactor;
                xyPatch=new int[2][patchWidth*patchWidth];
                for (int jy=0;jy<patchWidth;jy++){
                    for (int jx=0;jx<patchWidth;jx++){
                        patchID=jx+(patchWidth*jy);
                        xyPatch[0][patchID]=xyClosest[0][0]-(int)((patchWidth-1)/2.0)+jx;
                        xyPatch[1][patchID]=xyClosest[1][0]-(int)((patchWidth-1)/2.0)+jy;
                    }
                }
                // Keep xy pairs associated with a link, ie. where a ressimID exists
                //   If ressimID exists, then (x,y) is a Contact point of a link. A Contact point refers to the pixel located on the link before it joins others downstream.
                //   linksStructure.getResSimID can be used for any xy-pair, not just xy-pair of outlet of myCuenca
                //   Array id of link data is (resSimID-1)...CHECK
                // SLOW FROM HERE ...
                xyResSimID=new int[xyPatch[0].length];
                xyNumber=0;
                for (int j=0;j<xyPatch[0].length;j++) {
                    xyResSimID[j]=linksStructure.getResSimID(xyPatch[0][j],xyPatch[1][j]);
                    if (xyResSimID[j]!=-1) xyNumber=xyNumber+1;
                }
                // Collapse xyPatch to selected xy pairs and find their drainage areas
                xySelected = new int[2][xyNumber];
                xyArea = new float[1][xyNumber];
                int xyCount=0;
                areas=linksStructure.getVarValues(2); // drainage areas of all links in myCuenca, [1][n] array
                for (int j=0;j<xyPatch[0].length;j++){
                    if (xyResSimID[j]!=-1){
                        xySelected[0][xyCount]=xyPatch[0][j]; // x chosen
                        xySelected[1][xyCount]=xyPatch[1][j]; // y chosen
                        xyArea[0][xyCount]=areas[0][xyResSimID[j]-1];
                        xyCount=xyCount+1;
                    }
                }
                // ... TO HERE
                // Write xy-coordinates to .log.auto file
                areaDifference=new float[1][xyNumber];
                idSortedArea=new int[1][xyNumber];
                if (gaugeInfo[3][i]==null) {    // 'null' condition occurs if drainage area is NOT recorded in the gauge data file
                    System.out.println("Gauge area = none recorded");
                    FileWriter logfile = new FileWriter(pathToTopography+nameOfDEM+".log.auto",true); // 'true' appends data
                    for (int j=0;j<xyNumber;j++){
                        if (xySelected[0][idSortedArea[0][j]]!=-1){
                            xySelectedNumber=xySelectedNumber+1;
                            outputToLogFile=
                                    "x: "+Integer.toString(xySelected[0][idSortedArea[0][j]])+
                                    ", y: "+Integer.toString(xySelected[1][idSortedArea[0][j]])+
                                    " ; Basin Code "+gaugeInfo[0][i]+" - no recorded area"+'\n'; // B for Best
                            logfile.write(outputToLogFile);
                        }
                    }
                    logfile.close();
                } else {                        // get ids of sorted areas for writing in order of closest to farthest
                    areaGauge=Float.parseFloat(gaugeInfo[3][i]); // in mi^2
                    areaGauge=2.589988f*areaGauge; // in km^2
                    System.out.println("Gauge area = "+areaGauge);
                    System.out.println("Assessing coordinates in patch ...");
                    areaDifferenceSum=0;
                    for (int j=0;j<xyNumber;j++) {
                        areaDifference[0][j]=Math.abs(xyArea[0][j]-areaGauge);
                        areaDifferenceSum=areaDifferenceSum+areaDifference[0][j];
                    }
                    for (int k=0;k<xyNumber;k++){
                        minAreaDifference=areaDifferenceSum;
                        for (int j=0;j<xyNumber;j++){
                            if (areaDifference[0][j]<minAreaDifference && areaDifference[0][j]!=-1) {
                                minAreaDifference=areaDifference[0][j];
                                idSortedArea[0][k]=j;
                            }
                        }
                        // System.out.println("Area Difference = "+areaDifference[0][idSortedArea[0][k]]);
                        areaDifference[0][idSortedArea[0][k]]=-1;
                    }
                    // Remove xy pairs that are outside the range that is +-25% of gauge area
                    for (int j=0;j<xyNumber;j++){
                        if (xyArea[0][j]<(areaGauge*0.75f) || xyArea[0][j]>(areaGauge*1.25f)){
                            xySelected[0][j]=-1;
                            xySelected[1][j]=-1;
                        }
                    }
                    FileWriter logfile = new FileWriter(pathToTopography+nameOfDEM+".log.auto",true); // 'true' appends data
                    for (int j=0;j<xyNumber;j++){
                        if (xySelected[0][idSortedArea[0][j]]!=-1 && j==0){
                            xySelectedNumber=xySelectedNumber+1;
                            outputToLogFile=
                                    "x: "+Integer.toString(xySelected[0][idSortedArea[0][j]])+
                                    ", y: "+Integer.toString(xySelected[1][idSortedArea[0][j]])+
                                    " ; Basin Code "+gaugeInfo[0][i]+'\n'; // Best choice
                            logfile.write(outputToLogFile);
                        }
                        if (xySelected[0][idSortedArea[0][j]]!=-1 && j!=0){
                            xySelectedNumber=xySelectedNumber+1;
                            outputToLogFile=
                                    "x: "+Integer.toString(xySelected[0][idSortedArea[0][j]])+
                                    ", y: "+Integer.toString(xySelected[1][idSortedArea[0][j]])+
                                    " ; Basin Code "+gaugeInfo[0][i]+" - alternate"+'\n'; // Alternate choice
                            logfile.write(outputToLogFile);
                        }
                    }
                    logfile.close();
                }
                System.out.println("Number of Selected xy pairs = "+xySelectedNumber);
                // Write to file indicating NO LOCATION FOUND.
                // This seems to occur only when the location is outside of the parent basin
                // but within the DEM boundary.
                if (xySelectedNumber==0 && patchWidthFactor==13){   // xy-coordinate not found!
                    System.out.println("XY-COORDINATE NOT FOUND");
                    FileWriter logfile = new FileWriter(pathToTopography+nameOfDEM+".log.auto",true); // 'true' appends data
                    outputToLogFile= "x: -1"+", y: -1"+
                                     " ; Basin Code "+gaugeInfo[0][i]+" - no coordinate found"+'\n'; // B for Best
                    logfile.write(outputToLogFile);
                    logfile.close();
                    xySelectedNumber=-1;
                }
            }
        }

        System.out.println("Done: The .log.auto file is now created");
    }
    /**
     * @param args the command line arguments
     */


    public static void main(String[] args) throws java.io.IOException {
        // TODO code application logic here

        // Get information about the DEM
        /*String nameOfDEM="clearwater"; //prefix of topography files in cuencas
        String pathToTopography="/Users/Furey/Analysis_Data/Cuencas_databases/Clearwater_ID_database/Rasters/Topography/1_ArcSec_USGS/";
        String pathToGaugeData="/Users/Furey/Analysis_Data/PNW_Streamflows/AvgFlows_PacNW/170603 Clearwater/";
        String nameOfBasin="13343000 Outlet";
        int x=726, y=3660; // xy-coordinate of parent basin
        */

        // Get information about the DEM
        String nameOfDEM="lewis"; //prefix of topography files in cuencas
        String pathToTopography="/Users/Furey/Analysis_Data/Cuencas_databases/Lewis_WA_database/Rasters/Topography/1_ArcSec_USGS/";
        String pathToGaugeData="/Users/Furey/Analysis_Data/PNW_Streamflows/AvgFlows_PacNW/170800 Lower Columbia/";
        String nameOfBasin="Lewis Outlet";
        int x=166, y=783; // xy-coordinate of parent basin

        // Write Outlet coordinate to .log.auto file
        FileWriter logfile = new FileWriter(pathToTopography+nameOfDEM+".log.auto");
        logfile.write("x: "+Integer.toString(x)+", y: "+Integer.toString(y)+" ; Basin Code "+nameOfBasin+'\n');
        logfile.close();

        // Get metaModif and matDirs
        // Find boundaries of the DEM to remove any gauges outside it.
        java.io.File theFile=new java.io.File(pathToTopography+nameOfDEM+".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        double minlat = metaModif.getMinLat();  // southernmost
        double maxlat = minlat+(metaModif.getNumRows()*metaModif.getResLat())/3600;
        double minlon = metaModif.getMinLon();  // westernmost
        double maxlon = minlon+(metaModif.getNumCols()*metaModif.getResLon())/3600;
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        // Get names of gauge files, each containing data for a gauge
        File dir = new File(pathToGaugeData);
        String[] dirlist = dir.list();
        int nFiles=0;
        for (int i=0;i<dirlist.length;i++){
            int number=0;
            for (int j=0;j<10;j++) if (dirlist[i].substring(0,1).equals(Integer.toString(j))) number=1;  // first char is number
            if (dirlist[i].indexOf(" ")==8 && number==1) nFiles=nFiles+1;  // eight numbers for all gauges
            else dirlist[i]="NA";
        }
        String[] filenames = new String[nFiles];
        int namesID=0;
        for (int i=0;i<dirlist.length;i++) {
            if (dirlist[i].equals("NA"));  // do nothing if true
            else {
                filenames[namesID]=dirlist[i];
                namesID++;
            }
        }

        // Read first 15 lines of each file
        // Put gauge name, lat-lon, and drainage area into gaugeInfo
        // Remove gauge if it matches the nameOfBasin
        String[][] gaugeInfo = new String[4][filenames.length];
        String[][] gaugeInfoTemp = gaugeInfo;
        int repeatGaugeID=-1;
        int gaugeInfoID;
        if (filenames == null) {
           // Either dir does not exist or is not a directory
        } else {
            for (int i=0; i<filenames.length; i++) {
                System.out.println(filenames[i]);
                try{
                java.io.File datafile=new java.io.File(pathToGaugeData+filenames[i]);
                java.io.BufferedReader input=new java.io.BufferedReader(new java.io.FileReader(datafile));
                for (int j=0; j<15; j++){
                    String line=input.readLine();
                    if (line.indexOf("USGS")!=-1) {
                        gaugeInfo[0][i]=line.substring(line.indexOf(" "),line.indexOf(" ",line.indexOf(" ")+1)).trim();
                        if (gaugeInfo[0][i].equals(nameOfBasin.substring(0,nameOfBasin.indexOf(" ")).trim())) repeatGaugeID=i;
                    }
                    if (line.indexOf("latitude")!=-1) {
                        gaugeInfo[1][i]=line.substring(1,10).trim()+" "+line.substring(0,1); // Put in form "47:30:30 N"
                    }
                    if (line.indexOf("longitude")!=-1) {
                        gaugeInfo[2][i]=line.substring(1,10).trim()+" "+line.substring(0,1); // Put in form "114:30:30 W"
                    }
                    if (line.indexOf("area")!=-1) {
                        int comma=line.indexOf(",");
                        if (comma!=-1) gaugeInfo[3][i]=line.substring(0,line.indexOf(",")).trim();
                        else gaugeInfo[3][i]=line.substring(0,line.indexOf(" ")).trim();
                    }       
                }
                input.close();
                } catch (java.io.IOException IOE){
                    System.err.println("An Error has ocurred while reading metafile");
                    System.err.println(IOE);
                }        
            }
        }
        // Flag those gauges that are outside of DEM boundary
        int outCount=0;
        for (int i=0;i<gaugeInfo[0].length;i++){
            double latGauge=hydroScalingAPI.tools.DMSToDegrees.getDegrees(gaugeInfo[1][i]);
            double lonGauge=hydroScalingAPI.tools.DMSToDegrees.getDegrees(gaugeInfo[2][i]);
            if (latGauge<minlat || latGauge>maxlat || lonGauge<minlon || lonGauge>maxlon){
                gaugeInfo[0][i]="outsideDEM";
                outCount=outCount+1;
            }
        }

        // Initialize and fill new gaugeInfo array
        gaugeInfoTemp=gaugeInfo;
        if (repeatGaugeID==-1){
            gaugeInfo=new String[4][filenames.length-outCount];  // Exclude gauges outside of DEM
        } else {
            gaugeInfo=new String[4][filenames.length-1-outCount];  // Exclude Outlet gauge and gauges outside of DEM
        } 
        gaugeInfoID=0;
        for (int i=0;i<gaugeInfoTemp[0].length;i++){
            if (i!=repeatGaugeID && gaugeInfoTemp[0][i].compareTo("outsideDEM")!=0) {
                gaugeInfo[0][gaugeInfoID]=gaugeInfoTemp[0][i];
                gaugeInfo[1][gaugeInfoID]=gaugeInfoTemp[1][i];
                gaugeInfo[2][gaugeInfoID]=gaugeInfoTemp[2][i];
                gaugeInfo[3][gaugeInfoID]=gaugeInfoTemp[3][i];
                gaugeInfoID++;
            }
        }

        // For Testing ...
        /*for (int k=0;k<gaugeInfo[0].length;k++){
            System.out.println(gaugeInfo[0][k]);
            System.out.println(gaugeInfo[1][k]);
            System.out.println(gaugeInfo[2][k]);
            System.out.println(gaugeInfo[3][k]);
        }
        System.out.println("Number of gauges : "+gaugeInfo[0].length);*/

        // Below, we require the coordinate of the largest basin within the DEM
        new streamgaugeXYToLogFile(nameOfDEM,pathToTopography,x,y,matDirs,metaModif,gaugeInfo);
    }

}
