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


/* Practice Simulation File
 * simulationsRep3.java
 *
 * Created on Novemeber 21, 2005, 9:30 AM
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;
import java.io.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class SimulationToMobleyFile extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    /** Creates new simulationsRep3 */
    //1) Allows for a specified rain intensity and duration over entire basin (basic infiltration)
    public SimulationToMobleyFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,outputDirectory);
    }
    //2) Allows for an inputed storm file (basic infiltration)
    public SimulationToMobleyFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate,int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,0.0f,routingType,outputDirectory);
    }
    //3) Allows for an inputed storm file (with inputed infiltration file)
    public SimulationToMobleyFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,outputDirectory);
    }
    //4) Allows for a specified rain intensity and duration over entire basin (with inputed infiltration file)
    //??? Why input a storm file and rainintensity,duration ???
    public SimulationToMobleyFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, java.io.File stormFile ,hydroScalingAPI.io.MetaRaster infiltMetaRaster, float infiltRate, int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        //Rainfall-Runoff model example
        //Basin Info
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        //Links Analysis Info
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        //Links Info
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        //???What are these parameters for???//
        thisNetworkGeom.setWidthsHG(5.6f, 0.46f,0.0f);
        //???What are these parameters for???//       
        thisNetworkGeom.setCheziHG(14.2f, -1/3.0f);
        
        //Hill Slopes Info
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
       
        //Print out the storm file
        System.out.println(stormFile);    
        
        //Print "Loading Storm.." while the storm file processes
        System.out.println("Loading Storm ...");
        
        //Storm Manager Info       
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        
        //If there is no storm file, use link structure, intensity, and duration to rain evenly over entire basin
        if(stormFile == null)
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure,rainIntensity,rainDuration);
        //Else, load the storm
        else
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        
        //???Is this command to hold program until the storm is completed???///
        if (!storm.isCompleted()) return;
        
        thisHillsInfo.setStormManager(storm);     
            /*
                Write to the file the following:
                        Number of Links
                        Number of Links completed
                        List the links completed
                        Areas of the links completed
                        Orders of the links completed
                        Maximum of the WF for the links completed
                        Length of Simulation
                        Results     
             */
        
        //Procure the binary file for the basin's DEM
        //Why am I getting an error here?
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));

        //???What do each of the routing types mean?, and do I need them?
        //???Peter did not include them in his simulation
        String routingString="";
        switch (routingType) {
            case 0:     routingString="VC";
                        break;
            case 1:     routingString="CC";
                        break;
            case 2:     routingString="CV";
                        break;
        }
        
        //???What does this do?
        java.io.File theFile;
        
        //What is the best way to designate an output path?
        
        //Output path (Alternative 1) (Peter)
        //String output_path="/home/jmobley/Research/Data/Rainfall_Runoff_Simulation/";
        
        //Output Path (Alternative 2) (Ricardo)
        //If there is no storm file, designate 'theFile' by intensity, duration, infiltration, and routing
        if(stormFile == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-INT_"+rainIntensity+"-DUR_"+rainDuration+"-IR_"+infiltRate+"-Routing_"+routingString+".csv");
        //If there is a storm file, designate 'theFile' by stormFile, infiltration, and routing
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-"+stormFile.getName()+"-IR_"+infiltRate+"-Routing_"+routingString+".csv");

        //???      
        System.out.println(theFile);
        
        //Output path (Alternative 3) (Peter)
        //???What is the difference between saving above to 'theFile' and saving to 'archivo'?
        //java.io.File archivo=new java.io.File(output_path+demName+"_"+storm.stormName()+"_IR_"+infiltRate+".dat");
        //System.out.println(output_path+demName+"_"+storm.stormName()+"_IR_"+infiltRate+".dat");
        
        //Create ...
        //???WHat does 'salida' mean?
        //???Why not :
        //java.io.FileOutputStream bufferout = new java.io.FileOutputStream(theFile);
        //java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        //Screen information
        //???Where is this information being written?
        //???What is the difference between 'println' and 'write' commands?
        newfile.write("Information on Complete order Streams\n");
        newfile.write("Links at the bottom of complete streams are:\n");
        
        //Link number       
        newfile.write("Link #,");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
        }
        //Space
        newfile.write("\n");
        
        //Horton Order
        newfile.write("Horton Order,");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+",");
        }
        //Space
        newfile.write("\n");
        
        //Upstream Area
        newfile.write("Upstream Area [km^2],");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
	    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+",");
        }
        //Space
        newfile.write("\n");
        
        //Link Outlet ID
        newfile.write("Link Outlet ID,");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
	    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]+",");
        }
        //Spaces    
        newfile.write("\n\n\n");
        
        //Results
        newfile.write("Results of flow simulations in your basin");
        //Space        
        newfile.write("\n");
        
        //Time
        newfile.write("Time,"); 
        //???This looks just like the 'Link Number' above.  Is this a mistake?
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
        }
        
        //Basic Geophysical Analysis
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_FlowOnly thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_FlowOnly(linksStructure,thisHillsInfo,thisNetworkGeom,routingType);
        double[] initialCondition=new double[linksStructure.contactsArray.length*2];
        
        float[][] areasHillArray=thisHillsInfo.getAreasArray();
        float[][] linkLengths=linksStructure.getVarValues(1);   // [1][n]  //???What does the (1) stand for?
        //double ic_sum = 0.0f;  //???What does this mean?
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            //???Why is the initial condition 1 versus ( areasHillArray[0][i]*1.*1e3 ) / ( linkLengths[0][i] *1e3 )?
            initialCondition[i]=0.0;            
            //initialCondition[i]=( areasHillArray[0][i]*1.*1e3 ) / ( linkLengths[0][i] *1e3 )  ;
            //System.out.println(areasHillArray[0][i]);
            initialCondition[i+linksStructure.contactsArray.length]=1;
            //System.out.println{"Sum of initial " + ic_sum};
            //ic_sum = ic_sum + initialCondition[i] ;
        }
        //System.out.println("Sum of initial q = " + ic_sum);
        
        //Initial Information Output
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/2.0));
        System.out.println("Initial RKF Simulation");
        
        //Implement Runge-Kutta ODE solver
        //???What do the variables in (thisBasinEqSys,1e-3,10/60.) denote?
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-3,10/60.);
        
        //???What is numPeriods?
        int numPeriods = 1;
        
        //If there is no storm file, use rain duration to establish numPeriods
        if(stormFile == null)
            numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/rainDuration);
        //If there is a storm file, use 'stormRecordResolutionInMinutes' to establish numPeriods        
        else
            numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
        
        //Record date
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        //Convert initial storm time to milliseconds
        //???Why convert to milliseconds?
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        //Post time
        System.out.println(thisDate.getTime());
        
        //If there is no storm file: 
        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
                //???Please explain next line...
                rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                //???Why 10/60 ?
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            //Inform user of the present time, as well as the running time for the simulation
	    java.util.Date interTime=new java.util.Date();
            //???Why print out an intermediate time?
            System.out.println("Intermediate Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            //???Please explain next line...       
            rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+500,1,initialCondition,newfile,linksStructure,thisNetworkGeom);
 
        //If there is a storm file:          
        } else {
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period "+(k+1)+" of "+numPeriods);
                //What is the difference between the next line and the former line in the 'if(stormFile == null){' loop??
                rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),1,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            //Inform user of the present time, as well as the running time for the simulation
	    java.util.Date interTime=new java.util.Date();
            System.out.println("Intermediate Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
        
            rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+500,1,initialCondition,newfile,linksStructure,thisNetworkGeom);
        }
        //Inform user that the RKF simulation is finished, as well as giving the end and running times
        System.out.println("RKF Simulation Completed"); //"Termina simulacion RKF"
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        System.out.println("Initial Writing of the Results"); //"Inicia escritura de Resultados"
        
        newfile.close();
        bufferout.close();
        
        System.out.println("Final Writing of the Results"); //"Termina escritura de Resultados"
        
        
    }//Test the program with example
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            subMain4(args);   //The test case for TestDem
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain4(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/FractalTrees_database/Rasters/Topography/man-vis/man-vis.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/FractalTrees_database/Rasters/Topography/man-vis/man-vis.dir"));
        
        //java.io.File theFile=new java.io.File("/hidrosigDataBases/Test_DB/Rasters/Topography/58447060.metaDEM");
        //hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        //metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Test_DB/Rasters/Topography/58447060.dir"));
        
        //java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
        //hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        //metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
        
        //java.io.File theFile=new java.io.File("/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM");
        //hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        //metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        //new SimulationToAsciiFile(44,111,matDirs,magnitudes,metaModif,0,1,0.0f,0,new java.io.File("/tmp/"));
        new SimulationToMobleyFile(8,8,matDirs,magnitudes,metaModif,0,1,0.0f,2,new java.io.File("/tmp/"));
        
        System.exit(0);
        
        //new SimulationToAsciiFile(85,42,matDirs,magnitudes,metaModif,50.0f,5.0f,0.0f,2,new java.io.File("/tmp/"));
        //new SimulationToAsciiFile(289,136,matDirs,magnitudes,metaModif,50.0f,5.0f,0.0f,2,new java.io.File("/tmp/"));
        
        //java.io.File stormFile;
        //stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/WCuniform_45_10.metaVHC");
        //new SimulationToAsciiFile(289,136,matDirs,magnitudes,metaModif,stormFile,0.0f,2,new java.io.File("/tmp/"));
        
        //new SimulationToAsciiFile(777, 324,matDirs,magnitudes,metaModif,10.0f,5.0f,0.0f,2,new java.io.File("/tmp/"));
        
        java.io.File stormFile;
        stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_06/precipitation_interpolated_ev06.metaVHC");
        new SimulationToMobleyFile(777, 324,matDirs,magnitudes,metaModif,stormFile,0.0f,0,new java.io.File("/tmp/"));
        
        stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_10/precipitation_interpolated_ev10.metaVHC");
        new SimulationToMobleyFile(777, 324,matDirs,magnitudes,metaModif,stormFile,0.0f,0,new java.io.File("/tmp/"));
        
    }
        
}
