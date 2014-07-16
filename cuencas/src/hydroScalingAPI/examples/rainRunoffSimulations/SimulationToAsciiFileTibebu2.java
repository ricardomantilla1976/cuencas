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
public class SimulationToAsciiFileTibebu2 extends java.lang.Object implements Runnable{
    
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
    public SimulationToAsciiFileTibebu2(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileTibebu2(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate,int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileTibebu2(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileTibebu2(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
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
        double extraSimTime=240D*Math.pow(2.0D,(basinOrder-1));
        
        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
//                rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
                rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,10,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
            rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+extraSimTime,5,initialCondition,newfile,linksStructure,thisNetworkGeom);
            
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
            
//            float [] intensity = new float []{4.44f,5.52f,6.36f,7.8f,9.0f,10.32f};
//            float intensity = 3.84f;
//            float duration = 10.0f;
           
//                subMain5(args,25.4f*intensity,duration);
//                subMain5(args,25.4f*4.44f,5,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");
//                subMain5(args,25.4f*3.84f,10,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");
//                subMain5(args,25.4f*3.32f,15,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");   
//                subMain5(args,25.4f*2.26f,30,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");   
//                subMain5(args,25.4f*1.44f,60,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");   
//                subMain5(args,25.4f*0.89f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");   
//                subMain5(args,25.4f*0.65f,180,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");   
//                subMain5(args,25.4f*0.38f,360,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");
//                subMain5(args,25.4f*0.22f,720,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");
//                subMain5(args,25.4f*0.16f,1080,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");   
//                subMain5(args,25.4f*0.13f,1440,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");   
//                subMain5(args,25.4f*0.07f,2880,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");   
//                subMain5(args,25.4f*0.05f,4320,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");   
//                subMain5(args,25.4f*0.03f,7200,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");
//                subMain5(args,25.4f*0.02f,14400,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/");
//                
//                subMain5(args,25.4f*5.52f,5,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");
//                subMain5(args,25.4f*4.86f,10,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");
//                subMain5(args,25.4f*4.16f,15,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");   
//                subMain5(args,25.4f*2.84f,30,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");   
//                subMain5(args,25.4f*1.80f,60,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");   
//                subMain5(args,25.4f*1.12f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");   
//                subMain5(args,25.4f*0.82f,180,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");   
//                subMain5(args,25.4f*0.48f,360,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");
//                subMain5(args,25.4f*0.28f,720,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");
//                subMain5(args,25.4f*0.2f,1080,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");   
//                subMain5(args,25.4f*0.16f,1440,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");   
//                subMain5(args,25.4f*0.09f,2880,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");   
//                subMain5(args,25.4f*0.06f,4320,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");   
//                subMain5(args,25.4f*0.04f,7200,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");
//                subMain5(args,25.4f*0.03f,14400,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/");
//                
//                subMain5(args,25.4f*6.36f,5,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");
//                subMain5(args,25.4f*5.58f,10,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");
//                subMain5(args,25.4f*4.80f,15,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");   
//                subMain5(args,25.4f*3.28f,30,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");   
//                subMain5(args,25.4f*2.09f,60,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");   
//                subMain5(args,25.4f*1.29f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");   
//                subMain5(args,25.4f*0.95f,180,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");   
//                subMain5(args,25.4f*0.56f,360,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");
//                subMain5(args,25.4f*0.32f,720,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");
//                subMain5(args,25.4f*0.23f,1080,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");   
//                subMain5(args,25.4f*0.19f,1440,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");   
//                subMain5(args,25.4f*0.11f,2880,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");   
//                subMain5(args,25.4f*0.07f,4320,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");   
//                subMain5(args,25.4f*0.05f,7200,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");
//                subMain5(args,25.4f*0.03f,14400,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/");
//                
//                subMain5(args,25.4f*7.80f,5,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");
//                subMain5(args,25.4f*6.84f,10,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");
//                subMain5(args,25.4f*5.84f,15,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");   
//                subMain5(args,25.4f*4.02f,30,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");   
//                subMain5(args,25.4f*2.55f,60,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");   
//                subMain5(args,25.4f*1.57f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");   
//                subMain5(args,25.4f*1.16f,180,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");   
//                subMain5(args,25.4f*0.68f,360,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");
//                subMain5(args,25.4f*0.39f,720,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");
//                subMain5(args,25.4f*0.28f,1080,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");   
//                subMain5(args,25.4f*0.23f,1440,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");   
//                subMain5(args,25.4f*0.13f,2880,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");   
//                subMain5(args,25.4f*0.09f,4320,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");   
//                subMain5(args,25.4f*0.06f,7200,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");
//                subMain5(args,25.4f*0.03f,14400,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/");
//                
//                subMain5(args,25.4f*9.00f,5,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");
//                subMain5(args,25.4f*7.86f,10,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");
//                subMain5(args,25.4f*6.76f,15,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");   
//                subMain5(args,25.4f*4.62f,30,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");   
//                subMain5(args,25.4f*2.94f,60,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");   
//                subMain5(args,25.4f*1.81f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");   
//                subMain5(args,25.4f*1.33f,180,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");   
//                subMain5(args,25.4f*0.78f,360,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");
//                subMain5(args,25.4f*0.45f,720,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");
//                subMain5(args,25.4f*0.33f,1080,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");   
//                subMain5(args,25.4f*0.26f,1440,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");   
//                subMain5(args,25.4f*0.14f,2880,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");   
//                subMain5(args,25.4f*0.1f,4320,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");   
//                subMain5(args,25.4f*0.07f,7200,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");
//                subMain5(args,25.4f*0.04f,14400,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/");
//                
//                subMain5(args,25.4f*10.32f,5,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");
//                subMain5(args,25.4f*9.00f,10,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");
//                subMain5(args,25.4f*7.72f,15,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");   
//                subMain5(args,25.4f*5.28f,30,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");   
//                subMain5(args,25.4f*3.35f,60,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");   
//                subMain5(args,25.4f*2.07f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");   
//                subMain5(args,25.4f*1.52f,180,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");   
//                subMain5(args,25.4f*0.89f,360,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");
//                subMain5(args,25.4f*0.52f,720,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");
//                subMain5(args,25.4f*0.37f,1080,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");   
//                subMain5(args,25.4f*0.3f,1440,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");   
//                subMain5(args,25.4f*0.16f,2880,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");   
//                subMain5(args,25.4f*0.12f,4320,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");   
//                subMain5(args,25.4f*0.08f,7200,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");
//                subMain5(args,25.4f*0.04f,14400,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/");
            
                subMain5(args,25.4f*(0.52f*12f*60f/5f),5,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");
                subMain5(args,25.4f*(0.52f*12f*60f/10f),10,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");
                subMain5(args,25.4f*(0.52f*12f*60f/15f),15,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");   
                subMain5(args,25.4f*(0.52f*12f*60f/30f),30,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");   
                subMain5(args,25.4f*(0.52f*12f*60f/60f),60,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");   
                subMain5(args,25.4f*(0.52f*12f*60f/120f),120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");   
                subMain5(args,25.4f*(0.52f*12f*60f/180f),180,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");   
                subMain5(args,25.4f*(0.52f*12f*60f/360f),360,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");
                subMain5(args,25.4f*(0.52f*12f*60f/720f),720,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");
                subMain5(args,25.4f*(0.52f*12f*60f/1080f),1080,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");   
                subMain5(args,25.4f*(0.52f*12f*60f/1440f),1440,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");   
                subMain5(args,25.4f*(0.52f*12f*60f/2880f),2880,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");   
                subMain5(args,25.4f*(0.52f*12f*60f/4320f),4320,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");   
                subMain5(args,25.4f*(0.52f*12f*60f/7200f),7200,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");
                subMain5(args,25.4f*(0.52f*12f*60f/14400f),14400,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVo/");
            
            
//Const Duration
//                subMain5(args,1f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/");
//                subMain5(args,5f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/");
//                subMain5(args,10,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/");   
//                subMain5(args,15f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/");   
//                subMain5(args,30f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/");   
//                subMain5(args,45f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/");   
//                subMain5(args,60f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/"); 
//                subMain5(args,90f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/"); 
//                subMain5(args,120f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/"); 
//                subMain5(args,150f,120,"C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/"); 
                      
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
//        System.exit(0);
        
    }
    
        
    
    
    public static void subMain5(String args[],float intensity,float duration, String outLoc) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("C:/CuencasDataBases/OldMansCreek_Database/Rasters/Topography/NED_18879939.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("C:/CuencasDataBases/OldMansCreek_Database/Rasters/Topography/NED_18879939.dir"));
        
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
        
        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        routingParams.put("v_o", 0.1f);

        routingParams.put("v_h", 0.01f);


        //Routing type: 2 is constant velocity v=vo, 5 variable v=vo q^l1 A^l2       

        new SimulationToAsciiFileTibebu2(2308, 199, matDirs, magnitudes, metaModif, intensity, duration, 0.0f, 2, new java.io.File(outLoc), routingParams).executeSimulation();


        
    }

    


}

