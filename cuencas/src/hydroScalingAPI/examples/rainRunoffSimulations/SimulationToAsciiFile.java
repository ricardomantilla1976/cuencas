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
    java.util.Hashtable routingParams;
    
    /** Creates new simulationsRep3 */
    public SimulationToAsciiFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate,int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFile(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFile(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
                                 hydroScalingAPI.io.MetaRaster md, float rainIntensityOR, float rainDurationOR, java.io.File stormFileOR ,hydroScalingAPI.io.MetaRaster infiltMetaRasterOR, float infiltRateOR, int routingTypeOR, java.io.File outputDirectoryOR,java.util.Hashtable rP) throws java.io.IOException, VisADException{
        
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
        routingParams=rP;
        
        
        
    }
    
    public void executeSimulation() throws java.io.IOException, VisADException{
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
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
            case 5:     routingString="GK";
                        break;
        }
        
        java.io.File theFile;
        
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN.wfs.csv");
        System.out.println("Writing Width Functions - "+theFile);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        double[][] wfs=linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray,0);
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0){
                newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+",");
                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+",");
                newfile.write("\n");
            }
        }
      
        newfile.close();
        bufferout.close();
        
        if(infiltMetaRaster == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+"_"+v_o+".csv");//theFile=new java.io.File(Directory+demName+"_"+x+"_"+y++"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+widthCoeff+"_"+widthExponent+"_"+widthStdDev+"_"+chezyCoeff+"_"+chezyExponent+".dat");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltMetaRaster.getLocationMeta().getName().substring(0,infiltMetaRaster.getLocationMeta().getName().lastIndexOf(".metaVHC"))+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+".csv");
        System.out.println(theFile);
        
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        
        newfile.write("Information on Complete order Streams\n");
        newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("Link #,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
        }
        
        newfile.write("\n");
        newfile.write("Horton Order,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+",");
        }
        
        newfile.write("\n");
        newfile.write("Upstream Area [km^2],");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+",");
        }
        
        newfile.write("\n");
        newfile.write("Link Outlet ID,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
                newfile.write(linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]+",");
        }
        
        
        newfile.write("\n\n\n");
        newfile.write("Results of flow simulations in your basin");
        
        newfile.write("\n");
        newfile.write("Time,");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
        }
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay(linksStructure,thisHillsInfo,thisNetworkGeom,routingType);

        //RESETING INFILTRATION TO 0 AFTER ARGUMENT HAS BEEN PASSED TO THE CONSTRUCTOR
//        infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,0.0f);
//        thisHillsInfo.setInfManager(infilMan);


        double[] initialCondition=new double[linksStructure.contactsArray.length*2];
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.0;
            initialCondition[i+linksStructure.contactsArray.length]=0.0;
        }
        
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

        int basinOrder=linksStructure.getBasinOrder();
        double extraSimTime=120D*Math.pow(2.0D,(basinOrder-1));
        
        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
                //rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,10,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+extraSimTime,60,initialCondition,newfile,linksStructure,thisNetworkGeom);
            
        } else {
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period "+(k+1)+" of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),storm.stormRecordResolutionInMinutes(),initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+extraSimTime,60,initialCondition,newfile,linksStructure,thisNetworkGeom);
        }
        
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        
        double[] maximumsAchieved=rainRunoffRaining.getMaximumAchieved();
        
        newfile.write("\n");
        newfile.write("\n");
        newfile.write("Maximum Discharge [m^3/s],");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
                newfile.write(maximumsAchieved[linksStructure.completeStreamLinksArray[i]]+",");
        }
        
        System.out.println("Inicia escritura de Resultados");
        newfile.write("\n");
        
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
            //subMain2(args);   //Case for Walnut Gulch
            //subMain3(args);   //Case Upper Rio Puerco
            //subMain4(args);   //Case Whitewater
            //subMain5(args);   //Case Clear Creek June 3 to 7

            //subMain6(args);   //Case Squaw Creek April 20 to May 10

            subMain7(args);   //Case Goodwin Creek
            
            
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
        String path = "/CuencasDataBases/Continental_US_database/Rasters/";
        String filepath = path + "Topography/Dd_Basins_30_ArcSec/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String filename = filepath + "/" + tokenizer.nextToken();
        
        java.io.File theFile=new java.io.File(filename + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(filename + ".dir"));
        
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.5f);
        routingParams.put("lambda2",-0.1f);
        
        new SimulationToAsciiFile(x_outlet,y_outlet,matDirs,magnitudes,metaModif,50,1,0.0f,1,new java.io.File("/home/ricardo/workFiles/myWorkingStuff/MateriasDoctorado/PhD_Thesis/results/flowSimulations/realBasins"),routingParams).executeSimulation();
        
        System.exit(0);
        
    }
    
    public static void subMain1(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/FractalTrees_database/Rasters/Topography/man-vis/man-vis.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/FractalTrees_database/Rasters/Topography/man-vis/man-vis.dir"));
        
        //java.io.File theFile=new java.io.File("/CuencasDataBases/Test_DB/Rasters/Topography/58447060.metaDEM");
        //hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        //metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Test_DB/Rasters/Topography/58447060.dir"));
        
        //java.io.File theFile=new java.io.File("/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
        //hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        //metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
        
        //java.io.File theFile=new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM");
        //hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        //metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.dir"));
        
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.5f);
        routingParams.put("lambda2",-0.1f);
        
        
        //new SimulationToAsciiFile(44,111,matDirs,magnitudes,metaModif,0,1,0.0f,0,new java.io.File("/tmp/"));
        new SimulationToAsciiFile(32,32,matDirs,magnitudes,metaModif,6,10,0.0f,2,new java.io.File("/tmp/"),routingParams).executeSimulation();
        new SimulationToAsciiFile(32,32,matDirs,magnitudes,metaModif,6,10,0.0f,1,new java.io.File("/tmp/"),routingParams).executeSimulation();
        
        System.exit(0);
        
        //new SimulationToAsciiFile(85,42,matDirs,magnitudes,metaModif,50.0f,5.0f,0.0f,2,new java.io.File("/tmp/"));
        //new SimulationToAsciiFile(289,136,matDirs,magnitudes,metaModif,50.0f,5.0f,0.0f,2,new java.io.File("/tmp/"));
        
        //java.io.File stormFile;
        //stormFile=new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/WCuniform_45_10.metaVHC");
        //new SimulationToAsciiFile(289,136,matDirs,magnitudes,metaModif,stormFile,0.0f,2,new java.io.File("/tmp/"));
        
        //new SimulationToAsciiFile(777, 324,matDirs,magnitudes,metaModif,10.0f,5.0f,0.0f,2,new java.io.File("/tmp/"));
        
        java.io.File stormFile;
        stormFile=new java.io.File("/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_06/precipitation_interpolated_ev06.metaVHC");
        new SimulationToAsciiFile(777, 324,matDirs,magnitudes,metaModif,stormFile,0.0f,0,new java.io.File("/tmp/"),routingParams);
        
        stormFile=new java.io.File("/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/event_10/precipitation_interpolated_ev10.metaVHC");
        new SimulationToAsciiFile(777, 324,matDirs,magnitudes,metaModif,stormFile,0.0f,2,new java.io.File("/tmp/"),routingParams);
        
    }
    
    public static void subMain2(String args[]) throws java.io.IOException, VisADException {
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        
        routingParams.put("v_o",0.5f);
        
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
        
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        //new SimulationToAsciiFile(420,303,matDirs,magnitudes,metaModif,150, 2,3.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/")).executeSimulation();
        //new SimulationToAsciiFile(194,281,matDirs,magnitudes,metaModif, 50, 6,3.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/")).executeSimulation();
        //new SimulationToAsciiFile(194,281,matDirs,magnitudes,metaModif, 10,30,3.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/")).executeSimulation();
        //new SimulationToAsciiFile(194,281,matDirs,magnitudes,metaModif,  5,60,3.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/")).executeSimulation();

        new SimulationToAsciiFile(194,281,matDirs,magnitudes,metaModif,  20,5,0.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/"),routingParams).executeSimulation();
    }
    
    public static void subMain3(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/Upper Rio Puerco DB/Rasters/Topography/1_ArcSec/NED_54212683.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Upper Rio Puerco DB/Rasters/Topography/1_ArcSec/NED_54212683.dir"));
        
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.5f);
        routingParams.put("lambda2",-0.1f);
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        new SimulationToAsciiFile(381, 221,matDirs,magnitudes,metaModif,new java.io.File("/CuencasDataBases/Upper Rio Puerco DB/Rasters/Hydrology/Rainfall/nexrad_prec.metaVHC"),0.0f,0,new java.io.File("/tmp/"),routingParams).executeSimulation();
        
    }
    
    public static void subMain4(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.dir"));
        
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        //new SimulationToAsciiFile(194,281,matDirs,magnitudes,metaModif,  5,60,3.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/")).executeSimulation();
        
        //new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  25,120,0.0f,5,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/Cuencas/GKRouting/ScChannelVelocityGK")).executeSimulation();
        //new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  25,5,0.0f,2,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/Cuencas/CVRouting/Sc1IntensityOrDuration")).executeSimulation();
        
        //new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  stormFile,0.0f,2,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/Cuencas/CVRouting/ScGaussNoise")).executeSimulation();
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        
        
        java.io.File stormFile;
        stormFile=new java.io.File("/CuencasDataBases/Whitewater_database/Rasters/Hydrology/storms/simulated_events/uniform_030_120.metaVHC");
        new SimulationToAsciiFile(1063,496,matDirs,magnitudes,metaModif,stormFile,0.0f,2,new java.io.File("/home/ricardo/simulationResults/walnutGulch/"),routingParams).executeSimulation();
    }
    
    public static void subMain5(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.dir"));
        
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.2f);
        routingParams.put("lambda2",-0.1f);
        routingParams.put("v_o", 0.4f);
        
        java.io.File stormFile;

        stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJune/EventIowaJune8thNoon/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May15toJune11/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/June3toJune5/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/June3toJune5/hydroNexrad.metaVHC");
        
        float infil=0.0f;

        new SimulationToAsciiFile(1570, 127, matDirs, magnitudes, metaModif, stormFile, infil, 5, new java.io.File("/Users/ricardo/simulationResults/ClearCreek/"), routingParams).executeSimulation();
        
        System.exit(0);
        
        for (double lam1 = 3; lam1 <= 3; lam1 += 1) {
            routingParams.put("lambda1", (float)(lam1/10.0));
            for (double lam2 = -1; lam2 <= -1; lam2 += 1.0) {

                routingParams.put("lambda2", (float)(lam2/10.0));
                
                for (float intV = 3.5f; intV <= 3.5f; intV += 1.0) {
                    float v_o=(float)(intV/10.0f/Math.pow(10,lam1/10.0)/Math.pow(254,lam2/10.0));
                    routingParams.put("v_o", v_o);
                    new SimulationToAsciiFile(1570, 127, matDirs, magnitudes, metaModif, stormFile, infil, 5, new java.io.File("/home/ricardo/simulationResults/ClearCreek/"), routingParams).executeSimulation();
                }
            }
        }
    }

    public static void subMain6(String args[]) throws java.io.IOException, VisADException {

        java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/Squaw Creek At Ames/NED_86024003.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/Squaw Creek At Ames/NED_86024003.dir"));

        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.2f);
        routingParams.put("lambda2",-0.1f);
        routingParams.put("v_o", 0.4f);

        java.io.File stormFile;

        stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJune/EventIowaJune8thNoon/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/May15toJune11/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneMPE/June3toJune5/hydroNexrad.metaVHC");
        //stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventIowaJuneHydroNEXRAD/June3toJune5/hydroNexrad.metaVHC");

        float infil=0.0f;

        //new SimulationToAsciiFile(1425, 349, matDirs, magnitudes, metaModif, 20,2, infil, 5, new java.io.File("/Users/ricardo/simulationResults/SquawCreek/"), routingParams).executeSimulation();

        //routingParams.put("v_o", 0.75f);
        //new SimulationToAsciiFile(1425, 349, matDirs, magnitudes, metaModif, 40,60, infil, 2, new java.io.File("/Users/ricardo/simulationResults/SquawCreek/"), routingParams).executeSimulation();


        stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventOverAmes/hydroNexrad.metaVHC");
        routingParams.put("v_o", 0.5f);
        new SimulationToAsciiFile(1306, 376, matDirs, magnitudes, metaModif, stormFile, infil, 2, new java.io.File("/Users/ricardo/simulationResults/SquawCreek/"), routingParams).executeSimulation();

//        stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/EventOverAmes/hydroNexrad.metaVHC");
//        routingParams.put("lambda1",0.2f);
//        routingParams.put("lambda2",-0.1f);
//        routingParams.put("v_o", 0.3f);
//        new SimulationToAsciiFile(1425, 349, matDirs, magnitudes, metaModif, stormFile, infil, 5, new java.io.File("/Users/ricardo/simulationResults/SquawCreek/"), routingParams).executeSimulation();

        System.exit(0);

    }

    public static void subMain7(String args[]) throws java.io.IOException, VisADException {

        java.io.File theFile=new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.dir"));

        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);

        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);

        routingParams.put("lambda1",0.2f);
        routingParams.put("lambda2",-0.1f);
        routingParams.put("v_o", 0.6f);

        java.io.File stormFile;

        //int[] event_num={1,11,106,16,22};
        //int[] event_num={53,59,68,71,84,129};

        int iniEvent=111;
        int finEvent=148;

        int[] event_num=new int[finEvent-iniEvent+1];

        int k=0;
        for (int i=iniEvent;i<=finEvent;i++) {
            event_num[k]=i;
            k++;
        }

//        int[] event_num={35};
        
        for (int i : event_num) {

            float infil=(float)infiltrations[i-1];

            String evNUM=(""+(i/1000.+0.0001)).substring(2,5);

            stormFile=new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/interpolated_events/05min_ts/events_singpeakB_rain01/event_"+evNUM+"/precipitation_interpolated_ev"+evNUM+".metaVHC");

            new SimulationToAsciiFile(44, 111, matDirs, magnitudes, metaModif, stormFile, infil, 5, new java.io.File("/Users/ricardo/simulationResults/GoodwinCreek/"), routingParams).executeSimulation();

        }

        System.exit(0);

    }

    //IN REALITY THIS ARRAY HAS RUNOFF RATIOS
//    private static double[] infiltrations=new double[] {     0.07724756
//                                                            ,0.10953417
//                                                            ,0.33706571
//                                                            ,0.71919333
//                                                            ,0.21198217
//                                                            ,0.17742071
//                                                            ,0.32927257
//                                                            ,0.10072381
//                                                            ,0.33320432
//                                                            ,0.10780727
//                                                            ,0.77583847
//                                                            ,0.61808003
//                                                            ,0.32435947
//                                                            ,0.013346601
//                                                            ,0.077590556
//                                                            ,0.045476436
//                                                            ,0.33935376
//                                                            ,0.45190639
//                                                            ,0.075672156
//                                                            ,0.58942145
//                                                            ,0.61739498
//                                                            ,0.86654161
//                                                            ,0.078307831
//                                                            ,0.019882341
//                                                            ,0.035783536
//                                                            ,0.3712743
//                                                            ,0.39382936
//                                                            ,0.36982782
//                                                            ,0.5399591
//                                                            ,0.58031456
//                                                            ,0.18931844
//                                                            ,0.087458558
//                                                            ,0.071209938
//                                                            ,0.24677096
//                                                            ,0.1391884
//                                                            ,0.50823709
//                                                            ,0.2364711
//                                                            ,0.060608549
//                                                            ,0.24906881
//                                                            ,0.10356452
//                                                            ,0.5116073
//                                                            ,0.5170792
//                                                            ,0.18665013
//                                                            ,0.66951188
//                                                            ,0.58971526
//                                                            ,0.037564783
//                                                            ,0.24723482
//                                                            ,0.4669762
//                                                            ,0.41350909
//                                                            ,0.07924045
//                                                            ,0.1083013
//                                                            ,0.1606652
//                                                            ,0.56636311
//                                                            ,0.10130292
//                                                            ,0.051041081
//                                                            ,0.046430075
//                                                            ,0.064073368
//                                                            ,0.30501412
//                                                            ,0.33181276
//                                                            ,0.14013581
//                                                            ,0.20477644
//                                                            ,0.46036301
//                                                            ,0.008177902
//                                                            ,0.085099038
//                                                            ,0.23260847
//                                                            ,0.11266753
//                                                            ,0.28417451
//                                                            ,0.68002735
//                                                            ,0.01525943
//                                                            ,0.13920905
//                                                            ,0.46366286
//                                                            ,0.36887308
//                                                            ,0.56919295
//                                                            ,0.3594853
//                                                            ,0.048497057
//                                                            ,0.16110166
//                                                            ,0.025641213
//                                                            ,0.053919533
//                                                            ,0.15139724
//                                                            ,0.046862912
//                                                            ,0.52606357
//                                                            ,0.23163407
//                                                            ,0.095559791
//                                                            ,0.73421667
//                                                            ,0.1861118
//                                                            ,0.40231573
//                                                            ,0.099565918
//                                                            ,0.48567434
//                                                            ,0.54891204
//                                                            ,0.16927291
//                                                            ,0.092128688
//                                                            ,0.58848389
//                                                            ,0.24174118
//                                                            ,0.41382559
//                                                            ,0.74301666
//                                                            ,0.71525088
//                                                            ,0.33131465
//                                                            ,0.070862202
//                                                            ,0.029319838
//                                                            ,0.42616183
//                                                            ,0.43141227
//                                                            ,0.007957282
//                                                            ,0.057073542
//                                                            ,0.05246259
//                                                            ,0.09774254
//                                                            ,0.3329389
//                                                            ,0.19556194
//                                                            ,0.87261273
//                                                            ,0.46461029
//                                                            ,0.04704011
//                                                            ,0.44564824
//                                                            ,0.22481504
//                                                            ,0.69080607
//                                                            ,0.07570279
//                                                            ,0.25153587
//                                                            ,0.10236985
//                                                            ,0.49350843
//                                                            ,0.59936967
//                                                            ,0.092870763
//                                                            ,0.048401088
//                                                            ,0.26083202
//                                                            ,0.47711748
//                                                            ,0.36540159
//                                                            ,0.17687327
//                                                            ,0.054990137
//                                                            ,0.17019416
//                                                            ,0.28735706
//                                                            ,0.28780642
//                                                            ,0.03340968
//                                                            ,0.056356534
//                                                            ,0.16259258
//                                                            ,0.092903887
//                                                            ,0.35418711
//                                                            ,0.19318676
//                                                            ,0.44293796
//                                                            ,0.27070104
//                                                            ,0.3088698
//                                                            ,0.29016355
//                                                            ,0.28990248
//                                                            ,0.050279485
//                                                            ,0.30943992
//                                                            ,0.2726476
//                                                            ,0.73027448
//                                                            ,0.72008722
//                                                            ,0.23480231
//                                                            ,0.11048045
//                                                            ,0.2652157
//                                                            ,0.24202392};

    private static double[] infiltrations=new double[] {     4.23584
                                                            ,44.5312
                                                            ,5.17578
                                                            ,0.976562
                                                            ,31.6406
                                                            ,34.375
                                                            ,21.875
                                                            ,23.4375
                                                            ,4.29688
                                                            ,3.27148
                                                            ,0.878906
                                                            ,0.537109
                                                            ,0.854492
                                                            ,3.74146
                                                            ,11.9141
                                                            ,4.57764
                                                            ,8.59375
                                                            ,3.80859
                                                            ,6.98242
                                                            ,4.29688
                                                            ,8.20312
                                                            ,1.5625
                                                            ,14.2578
                                                            ,10.5957
                                                            ,29.1016
                                                            ,13.6719
                                                            ,6.25
                                                            ,1.85547
                                                            ,1.51367
                                                            ,1.26953
                                                            ,2.02637
                                                            ,13.8672
                                                            ,8.64258
                                                            ,4.93164
                                                            ,8.49609
                                                            ,12.8906
                                                            ,7.32422
                                                            ,10.5469
                                                            ,21.4844
                                                            ,18.75
                                                            ,4.88281
                                                            ,1.51367
                                                            ,1.85547
                                                            ,0.683594
                                                            ,2.92969
                                                            ,7.86133
                                                            ,7.8125
                                                            ,10.9375
                                                            ,4.78516
                                                            ,49.2188
                                                            ,25.9766
                                                            ,32.4219
                                                            ,12.1094
                                                            ,38.6719
                                                            ,24.6094
                                                            ,17.0898
                                                            ,4.98047
                                                            ,15.4297
                                                            ,17.1875
                                                            ,60.1562
                                                            ,51.5625
                                                            ,2.09961
                                                            ,4.80957
                                                            ,2.75879
                                                            ,3.85742
                                                            ,1.75781
                                                            ,5.27344
                                                            ,2.14844
                                                            ,18.2617
                                                            ,16.7969
                                                            ,6.44531
                                                            ,5.66406
                                                            ,2.19727
                                                            ,2.29492
                                                            ,19.9219
                                                            ,35.3516
                                                            ,85.1562
                                                            ,28.9062
                                                            ,32.4219
                                                            ,11.8164
                                                            ,3.51562
                                                            ,8.88672
                                                            ,42.5781
                                                            ,2.73438
                                                            ,4.05273
                                                            ,7.03125
                                                            ,19.1406
                                                            ,21.0938
                                                            ,15.625
                                                            ,7.12891
                                                            ,4.07715
                                                            ,2.05078
                                                            ,2.53906
                                                            ,1.46484
                                                            ,1.07422
                                                            ,1.26953
                                                            ,2.39258
                                                            ,26.7578
                                                            ,4.11377
                                                            ,13.2812
                                                            ,4.29688
                                                            ,17.2852
                                                            ,81.25
                                                            ,4.3457
                                                            ,16.4062
                                                            ,20.3125
                                                            ,3.51562
                                                            ,0.585938
                                                            ,10.9375
                                                            ,4.29688
                                                            ,11.7188
                                                            ,3.32031
                                                            ,1.51367
                                                            ,3.58887
                                                            ,26.9531
                                                            ,46.875
                                                            ,2.00195
                                                            ,1.46484
                                                            ,5.95703
                                                            ,2.87476
                                                            ,4.58984
                                                            ,0.976562
                                                            ,2.63672
                                                            ,2.58789
                                                            ,6.64062
                                                            ,11.9141
                                                            ,13.8672
                                                            ,28.125
                                                            ,30.4688
                                                            ,6.88477
                                                            ,2.49023
                                                            ,3.88184
                                                            ,6.25
                                                            ,0.854492
                                                            ,2.49023
                                                            ,1.66016
                                                            ,4.00391
                                                            ,4.98047
                                                            ,8.39844
                                                            ,42.5781
                                                            ,4.49219
                                                            ,3.51562
                                                            ,1.07422
                                                            ,1.26953
                                                            ,13.2812
                                                            ,17.9688
                                                            ,2.85645
                                                            ,31.25};


}

