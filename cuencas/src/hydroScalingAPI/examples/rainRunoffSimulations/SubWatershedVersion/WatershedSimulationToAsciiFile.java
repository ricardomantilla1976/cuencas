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


/*
 * simulationsRep3.java
 *
 * Created on December 6, 2001, 10:34 AM
 */

package hydroScalingAPI.examples.rainRunoffSimulations.SubWatershedVersion;

import hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.*;
import hydroScalingAPI.util.geomorphology.objects.LinksAnalysis;
import java.util.TimeZone;
import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class WatershedSimulationToAsciiFile extends java.lang.Object implements Runnable{
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir,hortonOrders;
    
    int x,y;
    int[][] xxyy_bounds;
    java.io.File[] bounds_files;
    
    int[][] magnitudes;
    float rainIntensity;
    float rainDuration;
    java.io.File stormFile;
    hydroScalingAPI.io.MetaRaster infiltMetaRaster;
    float infiltRate;
    int routingType;
    java.io.File outputDirectory;
    java.util.Hashtable routingParams;
    
    int[] usConnections;
    int basinOrder;
    float[] corrections;

    private java.util.Calendar zeroSimulationTime;

    public WatershedSimulationToAsciiFile(   int xx, 
                                        int yy, 
                                        int[][] xxyy,
                                        java.io.File[] filesI,
                                        byte[][] direcc,
                                        int[][] magnitudesOR,
                                        byte[][] horOrders, 
                                        hydroScalingAPI.io.MetaRaster md, 
                                        float rainIntensityOR, 
                                        float rainDurationOR, 
                                        java.io.File stormFileOR ,
                                        hydroScalingAPI.io.MetaRaster infiltMetaRasterOR, 
                                        float infiltRateOR, 
                                        int routingTypeOR, 
                                        java.util.Hashtable rP,
                                        java.io.File outputDirectoryOR,
                                        java.util.Calendar zST) throws java.io.IOException, VisADException{



        zeroSimulationTime=zST;
        
        matDir=direcc;
        metaDatos=md;
        
        x=xx;
        y=yy;
        xxyy_bounds=xxyy;
        
        bounds_files=filesI;
        
        magnitudes=magnitudesOR;
        hortonOrders=horOrders;
        rainIntensity=rainIntensityOR;
        rainDuration=rainDurationOR;
        stormFile=stormFileOR;
        infiltMetaRaster=infiltMetaRasterOR;
        infiltRate=infiltRateOR;
        routingType=routingTypeOR;
        outputDirectory=outputDirectoryOR;
        routingParams=rP;
    }
    
    public void executeSimulation() throws java.io.IOException, VisADException{
        
	//Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.Watershed myWatershed=new hydroScalingAPI.util.geomorphology.objects.Watershed(x,y,xxyy_bounds,matDir,metaDatos);
        
        myCuenca.setXYBasin(myWatershed.getXYBasin());
        
        //FIX AREAS FOR THREAD OF WATER
        
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        basinOrder=linksStructure.getBasinOrder();
        
        //UNCOMMENT THESE LINES TO IDENTIFY LINK LOCATIONS AND QUIT
        //System.out.println(linksStructure.getResSimID(1033,534));
        //System.exit(0);
        

        String demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        java.io.File theFile2=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+".complete.csv");
        java.io.OutputStreamWriter compnewfile = new java.io.OutputStreamWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(theFile2)));
        for(int i=0;i<linksStructure.completeStreamLinksArray.length;i++) compnewfile.write(linksStructure.completeStreamLinksArray[i]+",");

        compnewfile.close();

        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        
        float widthCoeff=((Float)routingParams.get("widthCoeff")).floatValue();
        float widthExponent=((Float)routingParams.get("widthExponent")).floatValue();
        float widthStdDev=((Float)routingParams.get("widthStdDev")).floatValue();
        
        float chezyCoeff=((Float)routingParams.get("chezyCoeff")).floatValue();
        float chezyExponent=((Float)routingParams.get("chezyExponent")).floatValue();
        
        thisNetworkGeom.setWidthsHG(widthCoeff,widthExponent,widthStdDev);
        thisNetworkGeom.setCheziHG(chezyCoeff, chezyExponent);
        
        float lam1=((Float)routingParams.get("lambda1")).floatValue();
        float lam2=((Float)routingParams.get("lambda2")).floatValue();

        float v_o=((Float)routingParams.get("v_o")).floatValue();

        thisNetworkGeom.setVqParams(v_o,0.0f,lam1,lam2);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        //System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        
        if(stormFile == null)
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure,rainIntensity,rainDuration);
        else
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);


        storm.setStormInitialTime(zeroSimulationTime);
        if (!storm.isCompleted()) return;
        
        thisHillsInfo.setStormManager(storm);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,0.0f);
        
        if(infiltMetaRaster == null)
            infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,infiltRate);
        else
            infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(myCuenca,linksStructure,infiltMetaRaster,matDir,magnitudes);
        
        thisHillsInfo.setInfManager(infilMan);
        
        /*
            Escribo en un theFile lo siguiente:
                    Numero de links
                    Numero de links Completos
                    lista de Links Completos
                    Area aguas arriba de los Links Completos
                    Orden de los Links Completos
                    maximos de la WF para los links completos
                    Longitud simulacion
                    Resulatdos

         */
        String routingString="";
        switch (routingType) {
            case 0:     routingString="VC";
                        break;
            case 1:     routingString="CC";
                        break;
            case 2:     routingString="CV";
                        break;
            case 3:     routingString="CM";
                        break;
            case 4:     routingString="VM";
                        break;
            case 5:     routingString="GK";
                        break;
        }
        
        java.io.File theFile;
        
        if(infiltMetaRaster == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+"_"+v_o+".csv");//theFile=new java.io.File(Directory+demName+"_"+x+"_"+y++"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+widthCoeff+"_"+widthExponent+"_"+widthStdDev+"_"+chezyCoeff+"_"+chezyExponent+".dat");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltMetaRaster.getLocationMeta().getName().substring(0,infiltMetaRaster.getLocationMeta().getName().lastIndexOf(".metaVHC"))+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+".csv");
        
        System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        java.io.File theFile1=new java.io.File(theFile.getAbsolutePath()+".Outlet.csv");
        java.io.FileOutputStream salida1 = new java.io.FileOutputStream(theFile1);
        java.io.BufferedOutputStream bufferout1 = new java.io.BufferedOutputStream(salida1);
        java.io.OutputStreamWriter newfile1 = new java.io.OutputStreamWriter(bufferout1);
        
        
        newfile.write("Information on Complete order Streams\n");
        newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("Link #,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write("Link-"+i+",");
        }
        
        newfile.write("\n");
        newfile.write("Horton Order,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(thisNetworkGeom.linkOrder(i)+",");
        }
        
        newfile.write("\n");
        newfile.write("Upstream Area [km^2],");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(thisNetworkGeom.upStreamArea(i)+",");
        }
        
        newfile.write("\n");
        newfile.write("Link Outlet ID,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(i+",");
        }
        
        
        newfile.write("\n\n\n");
        newfile.write("Results of flow simulations in your basin");
        
        newfile.write("\n");
        newfile.write("Time,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write("Link-"+i+",");
        }
        
        usConnections=new int[xxyy_bounds.length];
        for (int i = 0; i < xxyy_bounds.length; i++) {
            usConnections[i]=linksStructure.getLinkIDbyHead(xxyy_bounds[i][0], xxyy_bounds[i][1]);
            System.out.println(usConnections[i]);
        }
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.WatershedNetworkEquations_HillDelay thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.WatershedNetworkEquations_HillDelay(linksStructure,thisHillsInfo,thisNetworkGeom,routingType,usConnections,bounds_files);
        double[] initialCondition=new double[linksStructure.contactsArray.length*2];
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.05*thisNetworkGeom.upStreamArea(i);
            initialCondition[i+linksStructure.contactsArray.length]=0.0;
        }
        
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/2.0));
        System.out.println("Inicia simulacion RKF");
        
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-3,10/60.);
        
        int numPeriods = Math.round(((float)storm.stormFinalTimeInMinutes()-(float)storm.stormInitialTimeInMinutes())/(float)storm.stormRecordResolutionInMinutes());

        
        TimeZone tz = TimeZone.getTimeZone("UTC");
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.clear();
        thisDate.setTimeZone(tz);
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        
        double outputTimeStep=storm.stormRecordResolutionInMinutes();
        double extraSimTime=1*24*60;
        
        newfile1.write("TimeStep:" + outputTimeStep+"\n");
        newfile1.write("Time (minutes), Discharge [m3/s] \n");
        
        
        for (int k=0;k<numPeriods;k++) {
            System.out.println("Period "+(k+1)+" of "+numPeriods);
            rainRunoffRaining.jumpsRunCompleteToAsciiFile(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),outputTimeStep,initialCondition,newfile,linksStructure,thisNetworkGeom,newfile1);
            initialCondition=rainRunoffRaining.finalCond;
            rainRunoffRaining.setBasicTimeStep(10/60.);
        }

        java.util.Date interTime=new java.util.Date();
        System.out.println("Intermedia Time:"+interTime.toString());
        System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");


        outputTimeStep=30;//5*Math.pow(2.0D,(basinOrder-1));
        rainRunoffRaining.jumpsRunCompleteToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+extraSimTime,outputTimeStep,initialCondition,newfile,linksStructure,thisNetworkGeom,newfile1);
        
        newfile1.close();
        bufferout1.close();

        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        
        double[] maximumsAchieved=rainRunoffRaining.getMaximumAchieved();
        double[] timeToMaximumsAchieved=rainRunoffRaining.getTimeToMaximumAchieved();
        
        newfile.write("\n");
        newfile.write("\n");
        newfile.write("Maximum Discharge [m^3/s],");
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(maximumsAchieved[i]+",");
        }
        newfile.write("\n");
        newfile.write("Time to Maximum Discharge [minutes],");
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(timeToMaximumsAchieved[i]+",");
        }
        newfile.write("\n");
        newfile.write("\n");

        newfile.write("Precipitation Rates [mm/hr],");

        newfile.write("\n");

        for (int k=0;k<numPeriods;k++) {
            double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
            newfile.write(currTime+",");
            for (int i=0;i<linksStructure.contactsArray.length;i++){

                newfile.write(thisHillsInfo.precipitation(i,currTime)+",");

            }

            newfile.write("\n");

        }

        System.out.println("Inicia escritura de Resultados");
        
        newfile.close();
        bufferout.close();
        

    }

    private void writeReportFile(String outputDir, int x, int y) throws java.io.IOException{
        new java.io.File(outputDir+"/Tile_"+x+"_"+y+".done").createNewFile();
    }

    
    public void run(){
        
        try{
            executeSimulation();
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
        } catch (VisADException v){
            System.out.print(v);
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            //subMain1(args);  //Main case with three boundary conditions
            subMain2(args);  //Test case with boundary condition on English River
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
    }
    
    public static void subMain1(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaCityToLoneTreeWatershed/irsim30m.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        
        routingParams.put("v_o",0.5f);
        
        stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJune/EventIowaJune8thNoon/hydroNexrad.metaVHC");
        
        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        zeroSimulationTime.setTimeZone(tz);
        zeroSimulationTime.set(2008,5, 8, 10, 15, 00);

        java.io.File outputDirectory=new java.io.File("/Users/ricardo/simulationResults/Parallel/IowaRiverBetweenICandLT/");
        outputDirectory.mkdirs();
        
        int[][] xxyyb=new int[][] {{912,1063},{677,907},{366,476}}; // IC, OM, ER
        java.io.File[] boundaryCondFiles=new java.io.File[]
                    
                    {
                        new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaCityToLoneTreeWatershed/ICSim.txt"),
                        new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaCityToLoneTreeWatershed/OMSim.txt"),
                        new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaCityToLoneTreeWatershed/ERSim.txt")
                    } ;
        
        new WatershedSimulationToAsciiFile(1115,327,xxyyb,boundaryCondFiles,matDirs,magnitudes,horOrders,metaModif,20.0f,5.0f,stormFile,null,0.0f,2,routingParams,outputDirectory,zeroSimulationTime).executeSimulation();
            
    }
    
    public static void subMain2(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaCityToLoneTreeWatershed/irsim30m.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaModif.setFormat("Byte");
        byte [][] horOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        java.io.File stormFile;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        
        routingParams.put("v_o",0.5f);
        
        stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/HyperResolution/Prod3_5min1.0/StormJune8th/HydroNexradRef.metaVHC");
        
        java.util.Calendar zeroSimulationTime=java.util.Calendar.getInstance();
        TimeZone tz = TimeZone.getTimeZone("UTC");
        zeroSimulationTime.setTimeZone(tz);
        zeroSimulationTime.set(2008,5, 8, 10, 15, 00);
        
        java.io.File outputDirectory=new java.io.File("/Users/ricardo/simulationResults/Parallel/IowaRiverBetweenICandLT/");
        outputDirectory.mkdirs();
        
        int[][] xxyyb=new int[][] {{366,476}}; // IC, OM, ER
        java.io.File[] boundaryCondFiles=new java.io.File[]
                    
                    {
                        new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaCityToLoneTreeWatershed/ERSim.txt")
                    } ;
        
        new WatershedSimulationToAsciiFile(388,477,xxyyb,boundaryCondFiles,matDirs,magnitudes,horOrders,metaModif,20.0f,5.0f,stormFile,null,0.0f,2,routingParams,outputDirectory,zeroSimulationTime).executeSimulation();
        //new WatershedSimulationToAsciiFile(1033,534,xxyyb,boundaryCondFiles,matDirs,magnitudes,horOrders,metaModif,20.0f,5.0f,stormFile,null,0.0f,2,routingParams,outputDirectory,zeroSimulationTime).executeSimulation();
            
    }

}
