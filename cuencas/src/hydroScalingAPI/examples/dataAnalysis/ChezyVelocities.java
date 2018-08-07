/*
CUENCAS is a River Network Oriented GIS
Copyright (C) 2005  Ricardo Mantilla

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/



package hydroScalingAPI.examples.dataAnalysis;

//import java.io.*;
import hydroScalingAPI.modules.rainfallRunoffModel.objects.*;
import visad.*;

/**
 *
 * @author Peter Furey
 */
public class ChezyVelocities{
    
    // These lines assign names to objects but do not create them.
    hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;
    
    java.io.FileInputStream dataPath;
    java.io.BufferedInputStream dataBuffer;
    java.io.DataInputStream dataDataStream;
    
    double [] dataDouble, qdataDouble;
    double[][] discharges, discharges_sub;
    double discharge_max;
    double [][] velocities;
    double velocity_max;
    double total_velocity;
    int imax, jmax, jend;
    //String theFile ;
    
    // Constructor ...
    public ChezyVelocities(int x, int y, byte[][] matDir, hydroScalingAPI.io.MetaRaster metaDatos, String filename) {
        
        try {
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        linksConectionStruct=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir); 
        basinHillSlopesInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo( linksConectionStruct);
        linksHydraulicInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo( linksConectionStruct);
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        double[][] simDischarges = readStreamFlow(filename);
        double[][] simDischarges_sub = prepDischargesInfo(simDischarges);
        getVelocityInfo(simDischarges_sub);
    }
    
    
    // Read file to get array of simulated discharge values ...
    private double[][] readStreamFlow(String path) {
        
        try{
            
            dataPath=new java.io.FileInputStream(path);
            dataBuffer=new java.io.BufferedInputStream(dataPath);
            dataDataStream=new java.io.DataInputStream(dataBuffer);
            
            // Loop for skipping network data in file (eg. num links, num complete streams, ... )
            System.out.println("Reading simulation data file ...");
            int numCS = linksConectionStruct.completeStreamLinksArray.length;
            int numskip1 = 2+numCS;
            int numskip2 = 5*numCS;
            for (int i=0;i<numskip1;i++) dataDataStream.readInt();
            for (int i=0;i<numskip2;i++) dataDataStream.readFloat();
            
            // Number of periods and columns (link values)
            int numPeriods =dataDataStream.readInt();
            int nLi = linksConectionStruct.contactsArray.length;
            int numCols = (int) (4.*nLi)+1 ;
            dataDouble = new double[numCols];
            qdataDouble = new double[nLi];
            //System.out.println(numPeriods); //correct
            
            // Create discharge vector ...
            java.util.Vector data_vector = new java.util.Vector(nLi, nLi);
            
            // Cycle through simulated discharge data, put into vector ...
            // Discharges during rainfall ...
            for (int i=0;i<numPeriods;i++){
                int numSteps = dataDataStream.readInt();
                //System.out.println(numSteps+"steps");
                for(int j=0;j<numSteps;j++) {       // Loop through time
                    for(int k=0;k<numCols;k++) dataDouble[k]= dataDataStream.readDouble();
                    //System.out.println(Math.IEEEremainder((double) j, 2d));
                    if ( Math.IEEEremainder((double) j, 2d) == 0.0) { 
                    for(int k=0;k<nLi;k++) qdataDouble[k]=dataDouble[k+1];
                    for(int k=0;k<nLi;k++) data_vector.add( String.valueOf(qdataDouble[k]) );  // q for all links at a given time
                    }
                }
            }       
            // Discharges after rainfall ...
           int numSteps = dataDataStream.readInt();
           //System.out.println(numSteps);
           for(int j=0;j<numSteps;j++) {        // Loop through time
                    for(int k=0;k<numCols;k++) dataDouble[k]= dataDataStream.readDouble();
                    //System.out.println(Math.IEEEremainder((double) j, 2d));
                    if ( Math.IEEEremainder((double) j, 2d) == 0.0) { 
                    for(int k=0;k<nLi;k++) qdataDouble[k]=dataDouble[k+1];
                    for(int k=0;k<nLi;k++) data_vector.add( String.valueOf(qdataDouble[k]) );  // q for all links at a given time
                    }
           }
           
            // Make discharges array ....
            float size = data_vector.size();
            float ncols = (float) nLi;
            float nrows = size/ncols;
            discharges = new double[(int) ncols][(int) nrows];
            //System.out.println(ncols);
            //System.out.println(nrows);       
            System.out.println("Getting discharge values ...");
            for(int j=0;j<nrows;j++){               // Loop through time
                for (int i=0;i<ncols;i++){          // Loop through links
                    int id = (int) (i+(j*ncols));
                    String discharge_str = (String) data_vector.get(id);
                    float discharge_val = Float.parseFloat( discharge_str);                     // discharge = m^3/s
                    discharges[i][j]=discharge_val;
                }
                // System.out.println("Working on time step "+(j+1)+" of "+nrows);
            }
            
        dataBuffer.close();
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        return discharges ;  
    }
    
    // Find peak discharge and an its link and time id. 
    // Find time at which values go below 0.1 m^3/s for this link after peak.
    // Remove all discharge values (for all links) after this time.
    private double[][] prepDischargesInfo(double[][] discharges) {
        
        int ncols = discharges.length;
        int nrows = discharges[0].length;
        System.out.println("Finding peak discharge ...");
        // Get ids of max discharge ...
        for(int j=0;j<nrows;j++){                    // Loop through time
                for (int i=0;i<ncols;i++){          // Loop through links
                    if (( j==0) & (i==0)) {
                        discharge_max=discharges[i][j];
                        imax = i;
                        jmax = j;
                    }
                    if (( j>0) | (i>0)) {
                        discharge_max = Math.max(discharge_max, discharges[i][j]);
                        if (discharge_max == discharges[i][j]) {
                            imax = i;
                            jmax = j;
                        }
                    }
                 }
        }
       // Get id of discharge cutoff time ....
        for(int k=jmax;k<nrows;k++){         // Move ahead through time starting from time of max peak
            if (discharges[imax][k] < 0.1) jend = k;     // Assign cutoff to 0.1 m^3/s
            //System.out.println(discharges[imax][k]);
            if (jend == k) k=nrows;
        }
        // Truncate discharge data for all links at cutoff time ...
        discharges_sub = new double[ncols][jend];
        for(int j=0;j<jend;j++){                    // Loop through time
                for (int i=0;i<ncols;i++){          // Loop through links
                    discharges_sub[i][j] = discharges[i][j];
                }
        }
        System.out.println(nrows);
        System.out.println(jend);
        System.out.println(discharge_max);
        return discharges_sub;
       
    }
    
    // Use simulated dicharges array to evaluate simulated velocities ....
    private void getVelocityInfo(double[][] discharges_sub) {
        
        int ncols = discharges_sub.length;
        int nrows = discharges_sub[0].length;
        // System.out.println(nrows);
        velocities = new double[ncols][nrows];
        System.out.println("Getting velocity values ...");
        for(int j=0;j<nrows;j++){
                for (int i=0;i<ncols;i++){         // Loop through links
                    velocities[i][j] = Math.pow(linksHydraulicInfo.Chezi(i),2/3.)*
                       Math.pow(linksHydraulicInfo.Slope(i),1/3.)*
                       Math.pow(linksHydraulicInfo.Width(i),-1/3.)*
                       Math.pow(discharges_sub[i][j],1/3.);
                    total_velocity = total_velocity+ velocities[i][j];
                    if (( j==0) & (i==0)) velocity_max=velocities[i][j];
                    if (( j>0) | (i>0)) velocity_max = Math.max(velocity_max, velocities[i][j]);
                 }
                // System.out.println("Working on time step "+(j+1)+" of "+nrows);
        }
       System.out.println("Velocity info ....");
       System.out.println("-  Mean velocity (m/s) = "+(total_velocity/(nrows*ncols)));
       System.out.println("-  Max velocity (m/s) = "+ velocity_max);              
       //total_velocity = total_velocity+ velocities[i][j];
        return ;
    }
    
    /** Creates a new instance of Class */
    public static void main(String[ ] arguments) throws java.io.IOException, VisADException {
            
        /* Goodwin Creek, MS ... */
        String topo_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM";
        String simdata_path="/home/furey/HSJ/goodwin_ms/27jan04_gauss_Chezy_tests/01min_rain_steps/sims_01min/";
        
        java.io.File theFile=new java.io.File(topo_path);
        
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        int[] events={1,2,3,4};
        //int[] events={5,6,7,8};
        //String[] gauss_types=
        //    {"_sd0p01_coe1p00","_sd0p05_coe1p00","_sd0p10_coe1p00","_sd0p20_coe1p00",
        //   "_sd0p30_coe1p00","_sd0p40_coe1p00","_sd0p01_coe0p25","_sd0p05_coe0p85","_sd0p10_coe0p95"}; // in order of event number
        //String[] gauss_types={"_sd0p05_coe1p00_L","_sd0p20_coe1p00_L","_sd0p40_coe1p00_L"};
        //String[] gauss_types={"_sd0p05_coe1p00_R","_sd0p20_coe1p00_R","_sd0p40_coe1p00_R"};
        // String[] gauss_types={"_sd1p00_coe2p85","_sd2p00_coe2p85","_sd4p00_coe2p85","_sd6p00_coe2p85"};
        String evNUM,evStamp;
        
        
        for (int eventsid=0;eventsid<events.length;eventsid++){
            int evID = events[eventsid];
            evNUM=(""+(evID/100.+0.001)).substring(2,4);
            evStamp="_ev"+evNUM;
            System.out.println("Event number ="+evNUM);
            System.out.println(simdata_path+"goodwinCreek-nov03_precipitation_gaussian"+evStamp+"_IR_0.0.dat");
            new ChezyVelocities(44,111,matDirs,metaModif,simdata_path+"goodwinCreek-nov03_precipitation_gaussian"+evStamp+"_IR_0.0.dat");
        }
        System.out.println("");
        System.out.println("Finished");
    }
}

