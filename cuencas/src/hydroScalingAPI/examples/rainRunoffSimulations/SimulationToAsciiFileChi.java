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
public class SimulationToAsciiFileChi extends java.lang.Object implements Runnable{
    
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
    public SimulationToAsciiFileChi(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileChi(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate,int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileChi(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileChi(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
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
        java.io.FileOutputStream salida;
        java.io.BufferedOutputStream bufferout;
        java.io.OutputStreamWriter newfile;
        
//        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN.wfs.csv");
//        System.out.println("Writing Width Functions - "+theFile);
//        salida = new java.io.FileOutputStream(theFile);
//        bufferout = new java.io.BufferedOutputStream(salida);
//        newfile = new java.io.OutputStreamWriter(bufferout);
//        
//        double[][] wfs=linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray,0);
//        
//        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
//            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0){
//                newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+",");
//                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+",");
//                newfile.write("\n");
//            }
//        }
//      
//        newfile.close();
//        bufferout.close();
        
        if(infiltMetaRaster == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+(String)routingParams.get("caseName")+"_"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+"_"+v_o+".csv");//theFile=new java.io.File(Directory+demName+"_"+x+"_"+y++"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+widthCoeff+"_"+widthExponent+"_"+widthStdDev+"_"+chezyCoeff+"_"+chezyExponent+".dat");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+(String)routingParams.get("caseName")+"_"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltMetaRaster.getLocationMeta().getName().substring(0,infiltMetaRaster.getLocationMeta().getName().lastIndexOf(".metaVHC"))+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+".csv");
        System.out.println(theFile);
        
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        
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
            newfile.write(linksStructure.contactsArray[i]+",");
        }
        
        
        newfile.write("\n\n\n");
        newfile.write("Results of flow simulations in your basin");
        
        newfile.write("\n");
        newfile.write("Time,");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.write("Link-"+i+",");
        }
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelayPlusLinearHillslopeChi thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelayPlusLinearHillslopeChi(linksStructure,thisHillsInfo,thisNetworkGeom,routingType,routingParams);

//        RESETING INFILTRATION TO 0 AFTER ARGUMENT HAS BEEN PASSED TO THE CONSTRUCTOR
//        infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,0.0f);
//        thisHillsInfo.setInfManager(infilMan);


        double[] initialCondition=new double[linksStructure.contactsArray.length*3];
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.008*thisNetworkGeom.upStreamArea(i);
            initialCondition[i+linksStructure.contactsArray.length]=0.0;
            initialCondition[i+2*linksStructure.contactsArray.length]=20;  //20 mm provide a flux in the order of magnitude to the flux per unit area
        }

        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/2.0));
        System.out.println("Inicia simulacion RKF");
        
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-2,10/60.);
        
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
        
        int[] linkIDsGauges=(int[])routingParams.get("linkIdsStat");
        double[][] runoffChangePoints=(double[][])routingParams.get("changesInRunoffCoeff");
        
        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
                rainRunoffRaining.jumpsRunToCompleteAsciiFileChi(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToCompleteAsciiFileChi(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+extraSimTime,storm.stormRecordResolutionInMinutes(),initialCondition,newfile,linksStructure,thisNetworkGeom);
            
        } else {
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period "+(k+1)+" of "+numPeriods);
                
                double currentTimeInSim=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
                
                for(int ll=0;ll<runoffChangePoints.length;ll+=2) {
                    
                    if(currentTimeInSim>=runoffChangePoints[ll][0]) {
                        System.out.println("Changing Runoff Coefficient HERE "+ll+ " currentTime = "+currentTimeInSim+" changeTime = "+runoffChangePoints[ll][0]);
                        thisBasinEqSys.resetRunoffCoefficients(linkIDsGauges, runoffChangePoints[ll+1]);
                    }
                }
                
                rainRunoffRaining.jumpsRunToCompleteAsciiFileChi(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),storm.stormRecordResolutionInMinutes(),initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToCompleteAsciiFileChi(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+extraSimTime,storm.stormRecordResolutionInMinutes(),initialCondition,newfile,linksStructure,thisNetworkGeom);
        }
        
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        
        double[] maximumsAchieved=rainRunoffRaining.getMaximumAchieved();
        
        newfile.write("\n");
        newfile.write("\n");
        newfile.write("Maximum Discharge [m^3/s],");
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(maximumsAchieved[i]+",");
        }
        
        System.out.println("Inicia escritura de Resultados");
        newfile.write("\n");
        newfile.write("\n");

        double[] myRain=new double[linksStructure.contactsArray.length];

        newfile.write("Time,Mean Areal Rainfall[mm/hr]\n");

        for (int k=0;k<numPeriods;k++) {
            //System.out.println("Initiating time step "+k);
            double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();

            newfile.write(currTime+",");
            for (int i=0;i<linksStructure.contactsArray.length;i++){
                myRain[i]=thisHillsInfo.precipitation(i,currTime);
            }

            float meanValueToPrint=new hydroScalingAPI.util.statistics.Stats(myRain).meanValue;
            newfile.write(meanValueToPrint+",");
            
            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currTime * 60. * 1000.0));
            System.out.println(thisDate.getTime()+","+meanValueToPrint);


            newfile.write("\n");
        }
        newfile.write("\n");
        System.out.println("Done Writing Precipitations");
        
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
            
            //subMain4_1(args);   //Case Clear Creek (5m Resolution) June 3 to 7
            //subMain5_1(args);   //Case Squaw Creek (5m Resolution) Artrificial Storms
            //subMain5_2(args);   //Case Squaw Creek (5m Resolution) May 25 to June 5, 2013
            //subMain5_3(args);   //Case Squaw Creek (5m Resolution) Aug 8 to Aug 11, 2010
            subMain6_1(args);   //Case Nishnabotna Atlantic (5m Resolution) Artrificial Storms
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain4_1(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/burned/burneddem5cc.asc.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/burned/burneddem5cc.asc.dir"));
        
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
        routingParams.put("v_o", 0.20f);
        routingParams.put("v_h", 0.05f);
        
        routingParams.put("caseName","TT4");
        routingParams.put("stationID",  new int[] {     11,   9,   7,    8,   10,    6,    5,    1,   2,   4,   3});
        routingParams.put("linkIdsStat",new int[] {   5978,1306,7135,11179,10449,10408,12604,10247,7720,2347,2280});
        routingParams.put("runoffCoeff",new double[] {0.50,0.50,0.50, 0.50, 0.50, 0.50, 0.50, 0.90,0.90,0.90,0.90});
        
        
        
        
        int x=10989; int y=440; //Full Basin
        //int x=4031; int y=1281; //Sub Basin (~6km^2)


        java.io.File stormFile;

        stormFile=new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Hydrology/rainfall/KDVN/KDVN.metaVHC");
        float infil=0.0f;

        new SimulationToAsciiFileChi(x, y, matDirs, magnitudes, metaModif, stormFile, infil, 5, new java.io.File("/Users/ricardo/simulationResults/ClearCreek/"), routingParams).executeSimulation();
        
        System.exit(0);
        
    }
    
    public static void subMain5_1(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Topography/5meter/ames_dem_10.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Topography/5meter/ames_dem_10.dir"));
        
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
        routingParams.put("v_o", 0.30f);
        routingParams.put("v_h", 0.05f);
        
        routingParams.put("caseName","BaseCase_50_");
        routingParams.put("stationID",  new int[] {});
        routingParams.put("linkIdsStat",new int[] {});
        routingParams.put("runoffCoeff",new double[]{});
        
        
        int x=7055, y= 1242; //Full Basin
        
        float infil=0.0f;
        
        new SimulationToAsciiFileChi(x, y, matDirs, magnitudes, metaModif, 400,15, infil, 5, new java.io.File("/Users/ricardo/simulationResults/SquawCreek/"), routingParams).executeSimulation();
        System.exit(0);
        
    }
    
    public static void subMain5_2(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Topography/5meter/ames_dem_10.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Topography/5meter/ames_dem_10.dir"));
        
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
        routingParams.put("v_o", 0.30f);
        routingParams.put("v_h", 0.05f);
        
        routingParams.put("caseName","VariableRC_evap3_10_00_10_60_00_25_05_");
        routingParams.put("stationID",  new int[] {    22,  21,   25,   20,  19,  18,  14,  13,  17,  15,  16,   24,   12,   11,    9,   10,    8,   23,    7,    5,    6,    4,    1,    2,    3});
        routingParams.put("linkIdsStat",new int[] { 15245,1398,15224,15200,4604,6543,9764,9710,6479,6452,5435,15158,16835,15106,18279,13047,14425,28299,28270,22972,27696,28185,32352,33851,31771});
        routingParams.put("runoffCoeff",new double[]{0.35,0.35, 0.35, 0.35,0.35,0.35,0.35,0.35,0.35,0.35,0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35});
        
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        java.util.Calendar date=java.util.Calendar.getInstance(); date.clear(); 
        date.setTimeZone(tz);
        
        //RUNOFF TIME CHANGES IN """UTC"""
        
        date.set(2013, 4, 24, 23, 45, 0);
        double change1=date.getTimeInMillis()/1000./60.;
        date.set(2013, 4, 26, 00, 00, 0);
        double change2=date.getTimeInMillis()/1000./60.;
        date.set(2013, 4, 26, 10, 00, 0);
        double change3=date.getTimeInMillis()/1000./60.;
        date.set(2013, 4, 27, 00, 00, 0);
        double change4=date.getTimeInMillis()/1000./60.;
        date.set(2013, 4, 28, 00, 00, 0);
        double change5=date.getTimeInMillis()/1000./60.;
        date.set(2013, 4, 29, 00, 00, 0);
        double change6=date.getTimeInMillis()/1000./60.;
        date.set(2013, 4, 30, 00, 00, 0);
        double change7=date.getTimeInMillis()/1000./60.;
       
        
        
        //routingParams.put("stationID",  new int[] {                        22,  21,  25,  20,  19,  18,  14,  13,  17,  15,  16,  24,  12,  11,   9,  10,   8,  23,   7,   5,   6,   4,   1,   2,   3});
        routingParams.put("changesInRunoffCoeff",new double[][]{{change1},{0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10},
                                                                {change2},{0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00},
                                                                {change3},{0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10,0.10},        
                                                                {change4},{0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60,0.60},        
                                                                {change5},{0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00},        
                                                                {change6},{0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25,0.25},        
                                                                {change7},{0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05,0.05},        
                                                               });
        
        
        
        int x=7055, y= 1242; //Full Basin
        
        float infil=0.0f;
        
        java.io.File stormFile;

        stormFile=new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Hydrology/rainfall/mainStorm2013/ifcProduct.metaVHC");
        
        new SimulationToAsciiFileChi(x, y, matDirs, magnitudes, metaModif, stormFile, infil, 5, new java.io.File("/Users/ricardo/simulationResults/SquawCreek/"), routingParams).executeSimulation();
        System.exit(0);
        
    }
    
    public static void subMain5_3(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Topography/5meter/ames_dem_10.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Topography/5meter/ames_dem_10.dir"));
        
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
        routingParams.put("v_o", 0.30f);
        routingParams.put("v_h", 0.05f);
        
        routingParams.put("caseName","VariableRC_Case2010_evap3_05_00_70_");
        routingParams.put("stationID",  new int[] {    22,  21,   25,   20,  19,  18,  14,  13,  17,  15,  16,   24,   12,   11,    9,   10,    8,   23,    7,    5,    6,    4,    1,    2,    3});
        routingParams.put("linkIdsStat",new int[] { 15245,1398,15224,15200,4604,6543,9764,9710,6479,6452,5435,15158,16835,15106,18279,13047,14425,28299,28270,22972,27696,28185,32352,33851,31771});
        routingParams.put("runoffCoeff",new double[]{0.35,0.35, 0.35, 0.35,0.35,0.35,0.35,0.35,0.35,0.35,0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35, 0.35});
        
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        java.util.Calendar date=java.util.Calendar.getInstance(); date.clear(); 
        date.setTimeZone(tz);
        
        //RUNOFF TIME CHANGES IN """UTC"""
        
        date.set(2010, 7, 8, 0, 0, 0);
        double change1=date.getTimeInMillis()/1000./60.;
        date.set(2010, 7, 10, 3, 0, 0);
        double change2=date.getTimeInMillis()/1000./60.;
        date.set(2010, 7, 10, 15, 0, 0);
        double change3=date.getTimeInMillis()/1000./60.;
        
        //routingParams.put("stationID",  new int[] {                        22,  21,  25,  20,  19,  18,  14,  13,  17,  15,  16,  24,  12,  11,   9,  10,   8,  23,   7,   5,   6,   4,   1,   2,   3});
        routingParams.put("changesInRunoffCoeff",new double[][]{{change1},{0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50,0.50},
                                                                {change2},{0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00,0.00},
                                                                {change3},{0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70,0.70},        
                                                               });
        
        int x=7055, y= 1242; //Full Basin
        
        float infil=0.0f;
        
        java.io.File stormFile;

        stormFile=new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Hydrology/rainfall/mainStorm2010/ifcProduct.metaVHC");
        
        new SimulationToAsciiFileChi(x, y, matDirs, magnitudes, metaModif, stormFile, infil, 5, new java.io.File("/Users/ricardo/simulationResults/SquawCreek/"), routingParams).executeSimulation();
        System.exit(0);
        
    }
    
    public static void subMain6_1(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/Nishnabotna_Atlantic/Rasters/Topography/5meters/upatlantic.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Nishnabotna_Atlantic/Rasters/Topography/5meters/upatlantic.dir"));
        
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
        routingParams.put("v_o", 0.30f);
        routingParams.put("v_h", 0.05f);
        
        routingParams.put("caseName","BaseCase_50_");
        routingParams.put("stationID",  new int[] {});
        routingParams.put("linkIdsStat",new int[] {});
        routingParams.put("runoffCoeff",new double[]{});
        
        
        int x=3334, y= 259; //Full Basin
        
        float infil=0.0f;
        
        new SimulationToAsciiFileChi(x, y, matDirs, magnitudes, metaModif, 400,15, infil, 5, new java.io.File("/Users/ricardo/simulationResults/NishnabotnaAtlantic/"), routingParams).executeSimulation();
        System.exit(0);
        
    }

}

