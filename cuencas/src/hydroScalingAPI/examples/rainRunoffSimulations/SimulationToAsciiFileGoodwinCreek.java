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
public class SimulationToAsciiFileGoodwinCreek extends java.lang.Object implements Runnable{
    
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
    public SimulationToAsciiFileGoodwinCreek(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileGoodwinCreek(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate,int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileGoodwinCreek(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileGoodwinCreek(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
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

        thisHillsInfo.setHillslopeVh(((Float)routingParams.get("v_h")).floatValue());
        
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

//        //RESETING INFILTRATION TO 0 AFTER ARGUMENT HAS BEEN PASSED TO THE CONSTRUCTOR
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
        
        //Information related to specific links in the simulation that are gauged
        
        int[][] xyval = {{55,114},{111,180},{204,219},{196,209},{256,227}, //sgs 1 - 5
          {240,256},{243,199},{325,249},{325,250},{319,225},{344,233},                //sgs 6-11
          {369,239},{127,187},{172,191}};

        int[] resSimID = new int[xyval.length];
        for(int i=0;i<xyval.length;i++){
            resSimID[i] = linksStructure.getResSimID(xyval[i][0],xyval[i][1]);
            //sg_IDvals.write(sg_nums[i] + "   " + resSimID[i] + "  " + thisNetworkGeom.upStreamArea(resSimID[i]-1) + '\n');
            //System.out.println(resSimID);
            //System.out.println(linksStructure.contactsArray(xyval[i][0],xyval[i][1]).length);   //contactsArray.length
        }
        
//        double[] myRain=new double[linksStructure.contactsArray.length];
//
//        for (int k=0;k<numPeriods;k++) {
//            //System.out.println("Initiating time step "+k);
//            double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
//
//            System.out.print(currTime+",");
//            for (int i=0;i<linksStructure.contactsArray.length;i++){
//                myRain[i]=thisHillsInfo.precipitation(i,currTime);
//            }
//            System.out.print(new hydroScalingAPI.util.statistics.Stats(myRain).meanValue+",");
//            System.out.print("\n");
//        }
//        System.out.print("\n");


        
        java.io.File theFile_L;
        
        theFile_L=new java.io.File(theFile.getAbsolutePath()+".local.csv");
        java.io.FileOutputStream salida_L = new java.io.FileOutputStream(theFile_L);
        java.io.BufferedOutputStream bufferout_L = new java.io.BufferedOutputStream(salida_L);
        java.io.OutputStreamWriter newfile_L = new java.io.OutputStreamWriter(bufferout_L);

        newfile_L.write("Results of flow simulations in your basin");

        newfile_L.write("\n");
        newfile_L.write("Time,");

        for (int i : resSimID) {
            newfile_L.write("Link-"+(i-1)+",");
        }

        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFilePlusLocations(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom,resSimID,newfile_L);
                //rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,10,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToAsciiFilePlusLocations(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+extraSimTime,60,initialCondition,newfile,linksStructure,thisNetworkGeom,resSimID,newfile_L);
            
        } else {
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period "+(k+1)+" of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFilePlusLocations(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),storm.stormRecordResolutionInMinutes(),initialCondition,newfile,linksStructure,thisNetworkGeom,resSimID,newfile_L);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToAsciiFilePlusLocations(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+extraSimTime,60,initialCondition,newfile,linksStructure,thisNetworkGeom,resSimID,newfile_L);
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
        
        newfile.write("\n");

        newfile_L.write("\n");
        newfile_L.write("\n");
        newfile_L.write("Maximum Discharge [m^3/s],");
        for (int i : resSimID) newfile_L.write(maximumsAchieved[i-1]+",");

        newfile_L.write("\n");
        
        newfile.close();
        bufferout.close();

        newfile_L.close();
        bufferout_L.close();

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

        routingParams.put("lambda1",0.247677f);
        routingParams.put("lambda2",-0.136595f);
        routingParams.put("v_o", 0.605935f);
        routingParams.put("v_h", 0.05f);


        java.io.File stormFile;

        //int[] event_num={1,11,20,106,16,22};
        //int[] event_num={53,59,68,71,84,129};

//        int iniEvent=114;
//        int finEvent=148;
//
//        int[] event_num=new int[finEvent-iniEvent+1];
//
//        int k=0;
//        for (int i=iniEvent;i<=finEvent;i++) {
//            event_num[k]=i;
//            k++;
//        }

        int[] event_num={20,11};
        
        for (int i : event_num) {

            float infil=(float)infiltrations[i-1];

            String evNUM=(""+(i/1000.+0.0001)).substring(2,5);

            stormFile=new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/interpolated_events/05min_ts/events_singpeakB_rain01/event_"+evNUM+"/precipitation_interpolated_ev"+evNUM+".metaVHC");

            //routingParams.put("v_o", 1.0f);
            //infil=0.0f;

            new SimulationToAsciiFileGoodwinCreek(44, 111, matDirs, magnitudes, metaModif, stormFile, infil, 5, new java.io.File("/Users/ricardo/simulationResults/GoodwinCreek/"), routingParams).executeSimulation();

        }

        System.exit(0);

    }

//    //IN REALITY THIS ARRAY HAS RUNOFF RATIOS
//    private static double[] infiltrations=new double[] {        0.077022071,
//                                                                0.34012493,
//                                                                0.333511691,
//                                                                0.716385188,
//                                                                0.20588632,
//                                                                0.177444245,
//                                                                0.328270834,
//                                                                0.10120517,
//                                                                0.332646378,
//                                                                0.106678441,
//                                                                0.774447255,
//                                                                0.615064835,
//                                                                0.319330615,
//                                                                0.01336562,
//                                                                0.077079363,
//                                                                0.044391986,
//                                                                0.339509116,
//                                                                0.450247728,
//                                                                0.075753272,
//                                                                0.590036175,
//                                                                0.616538687,
//                                                                0.866002865,
//                                                                0.07800495,
//                                                                0.019630104,
//                                                                0.036202253,
//                                                                0.375114432,
//                                                                0.400599008,
//                                                                0.367944328,
//                                                                0.538504229,
//                                                                0.579145505,
//                                                                0.187790544,
//                                                                0.085957997,
//                                                                0.069133093,
//                                                                0.247932036,
//                                                                0.133938066,
//                                                                0.508117959,
//                                                                0.234489554,
//                                                                0.057404023,
//                                                                0.246224337,
//                                                                0.12452904,
//                                                                0.510958983,
//                                                                0.514918041,
//                                                                0.185754099,
//                                                                0.667300503,
//                                                                0.628651591,
//                                                                0.038739517,
//                                                                0.246131896,
//                                                                0.466040922,
//                                                                0.412816165,
//                                                                0.080090839,
//                                                                0.108656627,
//                                                                0.160761293,
//                                                                0.566938325,
//                                                                0.101381712,
//                                                                0.051022133,
//                                                                0.045647676,
//                                                                0.064408884,
//                                                                0.307115064,
//                                                                0.331998629,
//                                                                0.135476629,
//                                                                0.205425676,
//                                                                0.45826406,
//                                                                0.007913502,
//                                                                0.082581954,
//                                                                0.231444048,
//                                                                0.109859193,
//                                                                0.283012258,
//                                                                0.680048683,
//                                                                0.014680208,
//                                                                0.138982251,
//                                                                0.463436895,
//                                                                0.371659048,
//                                                                0.561747255,
//                                                                0.356436745,
//                                                                0.061217948,
//                                                                0.160903834,
//                                                                0.02660134,
//                                                                0.053967182,
//                                                                0.15160992,
//                                                                0.045605686,
//                                                                0.524097075,
//                                                                0.230003667,
//                                                                0.096489359,
//                                                                0.734083373,
//                                                                0.183424361,
//                                                                0.401482167,
//                                                                0.099468118,
//                                                                0.486882603,
//                                                                0.551205681,
//                                                                0.169131416,
//                                                                0.090518576,
//                                                                0.586988501,
//                                                                0.239485384,
//                                                                0.4100776,
//                                                                0.741049274,
//                                                                0.747517553,
//                                                                0.329335291,
//                                                                0.071597809,
//                                                                0.028197039,
//                                                                0.425152372,
//                                                                0.429820152,
//                                                                0.007882444,
//                                                                0.05689351,
//                                                                0.052307742,
//                                                                0.098823154,
//                                                                0.332689718,
//                                                                0.195057369,
//                                                                0.875087886,
//                                                                0.460854797,
//                                                                0.045373679,
//                                                                0.44236926,
//                                                                0.221973544,
//                                                                0.689785119,
//                                                                0.074815707,
//                                                                0.251705177,
//                                                                0.103389072,
//                                                                0.492598832,
//                                                                0.596336503,
//                                                                0.090646317,
//                                                                0.047177175,
//                                                                0.268372051,
//                                                                0.473798993,
//                                                                0.363178177,
//                                                                0.172820994,
//                                                                0.054021429,
//                                                                0.171357025,
//                                                                0.287116482,
//                                                                0.290760052,
//                                                                0.033227418,
//                                                                0.055569819,
//                                                                0.161583032,
//                                                                0.091773909,
//                                                                0.351084013,
//                                                                0.192698688,
//                                                                0.44127816,
//                                                                0.26674327,
//                                                                0.310187835,
//                                                                0.289029867,
//                                                                0.290058672,
//                                                                0.050237899,
//                                                                0.307326753,
//                                                                0.271074713,
//                                                                0.727726257,
//                                                                0.717162667,
//                                                                0.233157709,
//                                                                0.10934099,
//                                                                0.261735946,
//                                                                0.232595453
//};

    private static double[] infiltrations=new double[] {    4.23431,
                                                            44.6808,
                                                            5.17731,
                                                            0.979614,
                                                            31.7566,
                                                            34.2407,
                                                            21.7773,
                                                            23.5138,
                                                            4.32892,
                                                            3.27435,
                                                            0.91095,
                                                            0.53215,
                                                            0.860977,
                                                            3.74193,
                                                            11.9415,
                                                            4.57764,
                                                            8.47168,
                                                            3.82843,
                                                            6.97975,
                                                            4.37622,
                                                            8.19702,
                                                            1.474,
                                                            14.286,
                                                            10.5927,
                                                            29.1428,
                                                            13.7177,
                                                            6.18744,
                                                            1.85966,
                                                            1.49155,
                                                            1.25923,
                                                            2.02103,
                                                            13.8268,
                                                            8.65555,
                                                            4.92935,
                                                            8.49304,
                                                            12.8723,
                                                            7.31659,
                                                            10.5057,
                                                            21.3531,
                                                            18.8309,
                                                            4.8111,
                                                            1.49269,
                                                            1.85966,
                                                            0.680161,
                                                            2.89841,
                                                            7.87239,
                                                            7.81555,
                                                            10.8826,
                                                            4.75769,
                                                            49.2249,
                                                            25.9903,
                                                            32.3303,
                                                            12.0941,
                                                            38.7665,
                                                            24.5956,
                                                            17.0433,
                                                            4.99077,
                                                            15.39,
                                                            17.2531,
                                                            60.2295,
                                                            51.3367,
                                                            2.08282,
                                                            4.80852,
                                                            2.75822,
                                                            3.84979,
                                                            1.75047,
                                                            5.29556,
                                                            2.15836,
                                                            18.251,
                                                            16.9067,
                                                            6.46057,
                                                            5.70831,
                                                            2.19727,
                                                            2.28958,
                                                            19.8685,
                                                            35.3088,
                                                            85.1135,
                                                            28.9948,
                                                            32.489,
                                                            11.7996,
                                                            3.47748,
                                                            8.84399,
                                                            42.6056,
                                                            2.67334,
                                                            4.06303,
                                                            7.10449,
                                                            19.1071,
                                                            20.9961,
                                                            15.4907,
                                                            7.16782,
                                                            4.07848,
                                                            2.03247,
                                                            2.52342,
                                                            1.46217,
                                                            1.10016,
                                                            1.28479,
                                                            2.41013,
                                                            26.7303,
                                                            4.11425,
                                                            13.2019,
                                                            4.24957,
                                                            17.2569,
                                                            81.1462,
                                                            4.35867,
                                                            16.449,
                                                            20.3735,
                                                            3.5202,
                                                            0.616455,
                                                            11.0779,
                                                            4.29649,
                                                            11.8744,
                                                            3.33099,
                                                            1.51672,
                                                            3.58257,
                                                            27.124,
                                                            47.0337,
                                                            2.01111,
                                                            1.48849,
                                                            5.94826,
                                                            2.87457,
                                                            4.56085,
                                                            0.976181,
                                                            2.61917,
                                                            2.57988,
                                                            6.66122,
                                                            11.9095,
                                                            13.884,
                                                            28.1128,
                                                            30.5214,
                                                            6.87141,
                                                            2.48775,
                                                            3.88393,
                                                            6.28967,
                                                            0.858879,
                                                            2.47917,
                                                            1.65863,
                                                            3.98064,
                                                            4.98962,
                                                            8.35266,
                                                            42.4911,
                                                            4.47006,
                                                            3.51639,
                                                            1.0788,
                                                            1.24664,
                                                            13.2904,
                                                            17.9016,
                                                            2.85931,
                                                            31.1493};


}

