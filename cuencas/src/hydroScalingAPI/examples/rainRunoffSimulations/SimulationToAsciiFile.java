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

package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class SimulationToAsciiFile extends java.lang.Object implements Runnable{
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    int x;
    int y;
    int[][] magnitudes;
    float rainIntensity;
    float rainDuration;
    java.io.File stormFile;
    hydroScalingAPI.io.MetaRaster infiltMetaRaster;
    float infiltRate;
    int routingType;
    java.io.File outputDirectory;
    
    /** Creates new simulationsRep3 */
    public SimulationToAsciiFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,outputDirectory);
    }
    public SimulationToAsciiFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate,int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,0.0f,routingType,outputDirectory);
    }
    public SimulationToAsciiFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,outputDirectory);
    }
    public SimulationToAsciiFile(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
                                 hydroScalingAPI.io.MetaRaster md, float rainIntensityOR, float rainDurationOR, java.io.File stormFileOR ,hydroScalingAPI.io.MetaRaster infiltMetaRasterOR, float infiltRateOR, int routingTypeOR, java.io.File outputDirectoryOR) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        rainIntensity=rainIntensityOR;
        rainDuration=rainDurationOR;
        stormFile=stormFileOR;
        infiltMetaRaster=infiltMetaRasterOR;
        infiltRate=infiltRateOR;
        routingType=routingTypeOR;
        outputDirectory=outputDirectoryOR;
        
    }
    
    public void executeSimulation() throws java.io.IOException, VisADException{
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        //thisNetworkGeom.setWidthsHG(5.6f, 0.46f);
        //thisNetworkGeom.setChezysHG(14.2f, -1/3.0f);
        thisNetworkGeom.setWidthsHG(1.0f, 0.3f,0.0f);
        thisNetworkGeom.setCheziHG(14.2f, -1/6.0f);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        
        if(stormFile == null)
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure,rainIntensity,rainDuration);
        else
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        
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
        String demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
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
        }
        
        java.io.File theFile;
        
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+infiltRate+".wfs.csv");
        System.out.println("Writing Width Functions - "+theFile);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        double[][] wfs=linksStructure.getTopologicWidthFunctions(linksStructure.completeStreamLinksArray);
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
                newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+",");
                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+",");
                newfile.write("\n");
            }
        }
        
        newfile.close();
        bufferout.close();
        
        
        
        if(stormFile == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-INT_"+rainIntensity+"-DUR_"+rainDuration+"-IR_"+infiltRate+"-Routing_"+routingString+".csv");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-"+stormFile.getName()+"-IR_"+infiltRate+"-Routing_"+routingString+".csv");
        
        System.out.println(theFile);
        
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        
        newfile.write("Information on Complete order Streams\n");
        newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("Link #,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
        }
        
        newfile.write("\n");
        newfile.write("Horton Order,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+",");
        }
        
        newfile.write("\n");
        newfile.write("Upstream Area [km^2],");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+",");
        }
        
        newfile.write("\n");
        newfile.write("Link Outlet ID,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write(linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]+",");
        }
        
        
        newfile.write("\n\n\n");
        newfile.write("Results of flow simulations in your basin");
        
        newfile.write("\n");
        newfile.write("Time,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
        }
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_Simple thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_Simple(linksStructure,thisHillsInfo,thisNetworkGeom,routingType);
        double[] initialCondition=new double[linksStructure.contactsArray.length*2];
        
        float[][] areasHillArray=thisHillsInfo.getAreasArray();
        float[][] linkLengths=linksStructure.getVarValues(1);   // [1][n]
        //double ic_sum = 0.0f;
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.0;
            //initialCondition[i]=( areasHillArray[0][i]*1.*1e3 ) / ( linkLengths[0][i] *1e3 )  ;//0.0;
            //initialCondition[i]=0.07*thisNetworkGeom.upStreamArea(i);//0.0;//
            //System.out.println(areasHillArray[0][i]);
            initialCondition[i+linksStructure.contactsArray.length]=1;
            //System.out.println{"Sum of initial " + ic_sum};
            //ic_sum = ic_sum + initialCondition[i] ;
        }
        //System.out.println("Sum of initial q = " + ic_sum);
        
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/2.0));
        System.out.println("Inicia simulacion RKF");
        
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-3,10/60.);
        
        int numPeriods = 1;
        
        if(stormFile == null)
            numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/rainDuration);
        else
            numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
        
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        
        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+5000,10,initialCondition,newfile,linksStructure,thisNetworkGeom);
            
        } else {
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period "+(k+1)+" of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),1,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+500,5,initialCondition,newfile,linksStructure,thisNetworkGeom);
        }
        
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        System.out.println("Inicia escritura de Resultados");
        
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina escritura de Resultados");
        
        
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
            
            //subMain0(args);   //To Run as a external program from shell
            //subMain1(args);   //The test case for TestDem
            subMain2(args);     //Case for Walnut Gulch
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain0(String args[]) throws java.io.IOException, VisADException {
        
        //java.util.StringTokenizer tokenizer = new java.util.StringTokenizer("B_26	1110  462	B_26");
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(args[0]);
        String path = "/hidrosigDataBases/Continental_US_database/Rasters/";
        String filepath = path + "Topography/Dd_Basins_30_ArcSec/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String filename = filepath + "/" + tokenizer.nextToken();
        
        java.io.File theFile=new java.io.File(filename + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(filename + ".dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        new SimulationToAsciiFile(x_outlet,y_outlet,matDirs,magnitudes,metaModif,50,1,0.0f,1,new java.io.File("/home/ricardo/workFiles/myWorkingStuff/MateriasDoctorado/PhD_Thesis/results/flowSimulations/realBasins")).executeSimulation();
        new SimulationToAsciiFile(x_outlet,y_outlet,matDirs,magnitudes,metaModif,50,1,0.0f,3,new java.io.File("/home/ricardo/workFiles/myWorkingStuff/MateriasDoctorado/PhD_Thesis/results/flowSimulations/realBasins"));
        
        System.exit(0);
        
    }
    
    public static void subMain1(String args[]) throws java.io.IOException, VisADException {
        
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
        new SimulationToAsciiFile(32,32,matDirs,magnitudes,metaModif,6,10,0.0f,2,new java.io.File("/tmp/")).executeSimulation();
        new SimulationToAsciiFile(32,32,matDirs,magnitudes,metaModif,6,10,0.0f,1,new java.io.File("/tmp/")).executeSimulation();
        
        System.exit(0);
        
        //new SimulationToAsciiFile(85,42,matDirs,magnitudes,metaModif,50.0f,5.0f,0.0f,2,new java.io.File("/tmp/"));
        //new SimulationToAsciiFile(289,136,matDirs,magnitudes,metaModif,50.0f,5.0f,0.0f,2,new java.io.File("/tmp/"));
        
        //java.io.File stormFile;
        //stormFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/WCuniform_45_10.metaVHC");
        //new SimulationToAsciiFile(289,136,matDirs,magnitudes,metaModif,stormFile,0.0f,2,new java.io.File("/tmp/"));
        
        //new SimulationToAsciiFile(777, 324,matDirs,magnitudes,metaModif,10.0f,5.0f,0.0f,2,new java.io.File("/tmp/"));
        
        java.io.File stormFile;
        stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_06/precipitation_interpolated_ev06.metaVHC");
        new SimulationToAsciiFile(777, 324,matDirs,magnitudes,metaModif,stormFile,0.0f,0,new java.io.File("/tmp/"));
        
        stormFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_10/precipitation_interpolated_ev10.metaVHC");
        new SimulationToAsciiFile(777, 324,matDirs,magnitudes,metaModif,stormFile,0.0f,0,new java.io.File("/tmp/"));
        
    }
    
    public static void subMain2(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/ORIGINAL/walnutGulchUpdated.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/ORIGINAL/walnutGulchUpdated.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        new SimulationToAsciiFile(82,260,matDirs,magnitudes,metaModif,150, 2,3.0f,2,new java.io.File("/tmp/")).executeSimulation();
        new SimulationToAsciiFile(82,260,matDirs,magnitudes,metaModif, 50, 6,3.0f,2,new java.io.File("/tmp/")).executeSimulation();
        new SimulationToAsciiFile(82,260,matDirs,magnitudes,metaModif, 10,30,3.0f,2,new java.io.File("/tmp/")).executeSimulation();
        new SimulationToAsciiFile(82,260,matDirs,magnitudes,metaModif,  5,60,3.0f,2,new java.io.File("/tmp/")).executeSimulation();
        
    }
    
}
