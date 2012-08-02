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
import java.util.TimeZone;
/**
 *
 * @author Ricardo Mantilla
 */
public class SimulationToAsciiFileTibebu extends java.lang.Object implements Runnable{
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    String resOrder;
    int[] resLocations;
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
    public SimulationToAsciiFileTibebu(String resOrder,int[] resLocations,int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(resOrder,resLocations, x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileTibebu(String resOrder,int[] resLocations,int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate,int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(resOrder,resLocations,x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,infiltRate,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileTibebu(String resOrder,int[] resLocations,int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(resOrder,resLocations,x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,routingType,outputDirectory,routingParams);
    }
    public SimulationToAsciiFileTibebu(String resOrderOR,int[] resLocationsOR,int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
                                 hydroScalingAPI.io.MetaRaster md, float rainIntensityOR, float rainDurationOR, java.io.File stormFileOR ,hydroScalingAPI.io.MetaRaster infiltMetaRasterOR, float infiltRateOR, int routingTypeOR, java.io.File outputDirectoryOR,java.util.Hashtable rP) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        resOrder=resOrderOR;
        resLocations=resLocationsOR;
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
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.ReservoirsManager rm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.ReservoirsManager(thisNetworkGeom,linksStructure,resLocations);
//        rm.setReservoirLocations(new int[]{98,498,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328});
//        rm.setReservoirLocations(resLocations);
        
//        hydroScalingAPI.modules.rainfallRunoffModel.objects.ReservoirsManager_2 rm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.ReservoirsManager_2(thisNetworkGeom,linksStructure);
        
        
//        //Mapping link ids to locations in space
//        System.out.println(linksStructure.getResSimID(752, 385)-1);
//        
//        System.out.println(linksStructure.getResSimID(758, 382)-1);
//        System.out.println(linksStructure.getResSimID(752, 386)-1);
//        System.out.println(linksStructure.getResSimID(744, 384)-1);
//        System.out.println(linksStructure.getResSimID(745, 383)-1);
//        
//        
//        System.out.println(linksStructure.getResSimID(754, 386)-1);
//        System.out.println(linksStructure.getResSimID(778, 368)-1);
        //New Reservoir location
//        System.out.println(linksStructure.getResSimID(766, 378)-1);
//        System.out.println(linksStructure.getResSimID(767, 378)-1);
//        System.out.println(linksStructure.getResSimID(758, 382)-1);
//        System.out.println(linksStructure.getResSimID(759, 384)-1);
//        System.out.println(linksStructure.getResSimID(771, 374)-1);
//        System.out.println(linksStructure.getResSimID(1549, 139)-1);
//        System.out.println(linksStructure.getResSimID(1556, 135)-1);
//        System.out.println(linksStructure.getResSimID(1559, 132)-1);
//        System.out.println(linksStructure.getResSimID(1562, 131)-1);
//        System.out.println(linksStructure.getResSimID(1570, 127)-1);
         
//        System.out.println(linksStructure.getResSimID(1169, 249)-1);
//        System.out.println(linksStructure.getResSimID(1170, 246)-1);
//        System.out.println(linksStructure.getResSimID(1171, 242)-1);
//        System.out.println();
//        System.out.println(linksStructure.getResSimID(1153, 237)-1);
//        System.out.println(linksStructure.getResSimID(1160, 237)-1);
//        System.out.println(linksStructure.getResSimID(1171, 241)-1);
//        System.exit(0);
//        
//        System.out.println(linksStructure.getResSimID(1161, 159)-1);
//        System.out.println(linksStructure.getResSimID(1417, 102)-1);
//        System.out.println("x="+linksStructure.contactsArray[564]%metaDatos.getNumCols()+" y="+linksStructure.contactsArray[564]/metaDatos.getNumCols());
//        System.out.println("x="+linksStructure.contactsArray[163]%metaDatos.getNumCols()+" y="+linksStructure.contactsArray[163]/metaDatos.getNumCols());
//        System.exit(0);
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
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+"_"+v_o+"_storages_Order_"+resOrder+".csv");//theFile=new java.io.File(Directory+demName+"_"+x+"_"+y++"-"+storm.stormName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_"+widthCoeff+"_"+widthExponent+"_"+widthStdDev+"_"+chezyCoeff+"_"+chezyExponent+".dat");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltMetaRaster.getLocationMeta().getName().substring(0,infiltMetaRaster.getLocationMeta().getName().lastIndexOf(".metaVHC"))+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+"_storages.csv");
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
        newfile.write("Link Length [km^2],");
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
                newfile.write(thisNetworkGeom.Length(linksStructure.completeStreamLinksArray[i])+",");
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
        
//        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay_Reservoirs thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay_Reservoirs(linksStructure,thisHillsInfo,thisNetworkGeom,routingType,rm);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay_Reservoirs thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay_Reservoirs(linksStructure,thisHillsInfo,thisNetworkGeom,routingType,rm);

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
        double extraSimTime=60D*Math.pow(2.0D,(basinOrder-1));
        
        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
                System.out.println("Time"+","+"Outlet Discharge"+","+"Reservoir"+","+"ReservoirUP1"+","+"ReservoirUP2"+","+"ReservoirDown"+","+"DownUp4"+","+"DownUp2"); //Tibebu
                rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
                //rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,10,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
//            java.util.Date interTime=new java.util.Date();
//            System.out.println("Intermedia Time:"+interTime.toString());
//            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            
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
        newfile.write("Maximum Storage [m^3/s],");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
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
        
        //Here create your code to read the storages file and create a corresponding discharges file
        
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
            int [] resLoc_ord_3 = new int [] {37,87,90,73,163,212,144,295,300,325,340,362,370,395,418,435,566,571,584,590,599,475,622,637,558,752,953,1399,1026,1185,1169,1214,1267,1276,1296,1358,1363,1386,1409,1447,1459,1523,1510,1528,1533,1561,1479,1598,1638,1658,1795,2052,1896,1918,1933,1941,1972,1994,2034,2066,2133,2183,2272,2287,2292,2297,2330,2359,2381,2555,2647,2663,2670,3297,2913,2930,3055,3014,3070,3034,3080,3094,3188,3243,3257,3429,3373,3463,3656,3689,3706,3710,3834,4206,3908,3944,3955,3967,3969,3982,3988,4231,3600,4015,4184,4374,4335,4381,4401,4413,4580,4597,4622,4842,4688,4648,4720,4773,4800,4820,4957,4969,4995,5039,5065,4809,5128,5173,5200,5221,5304,5336,5390,5451,5474,5504,5556,5617,5633,5642,5733,5746,5839,5894,6054,5974,5989,6068,6078,6124,6192,6239,6279,6337,6354};
            int [] resLoc_ord_3_and_4 = new int [] {37,87,90,73,163,212,144,295,300,325,340,362,370,395,418,435,566,571,584,590,599,475,622,637,558,752,953,1399,1026,1185,1169,1214,1267,1276,1296,1358,1363,1386,1409,1447,1459,1523,1510,1528,1533,1561,1479,1598,1638,1658,1795,2052,1896,1918,1933,1941,1972,1994,2034,2066,2133,2183,2272,2287,2292,2297,2330,2359,2381,2555,2647,2663,2670,3297,2913,2930,3055,3014,3070,3034,3080,3094,3188,3243,3257,3429,3373,3463,3656,3689,3706,3710,3834,4206,3908,3944,3955,3967,3969,3982,3988,4231,3600,4015,4184,4374,4335,4381,4401,4413,4580,4597,4622,4842,4688,4648,4720,4773,4800,4820,4957,4969,4995,5039,5065,4809,5128,5173,5200,5221,5304,5336,5390,5451,5474,5504,5556,5617,5633,5642,5733,5746,5839,5894,6054,5974,5989,6068,6078,6124,6192,6239,6279,6337,6354,98,498,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc_ord_5 = new int [] {828,899,2754,2952,3174,3768,4816,4877};
            int [] resLoc_no_btm5 = new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,2564,2708,5066,2788,3221,3606,3889,3932,4220,4325,4942,4982,5840,5378,5447,5721,6175,6328};
            int [] resLoc   = new int[]{4325,5721,6328,6175,714,3221,4942,5447,5378,2788,3889,1619,1235,2142,4982};
            int [] resLoc23res = new int[]{98,658,714,1235,1619,2091,2142,2564,2708,5066,2788,3221,3606,3889,4220,4325,4942,4982,5378,5447,5721,6175,6328};
            int [] resLoc1 =new int[]{498,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc2 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc3 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc4 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc5 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc6 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc7 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc8 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc9 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc10 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc11 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc12 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc13 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc14 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc15 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc16 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc17 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc18 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc19 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc20 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc21 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc22 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc23 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc24 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc25 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc26 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc27 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc28 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc29 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc30 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc31 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc32 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            int [] resLoc33 =new int[]{98,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
//            {98,498,658,714,1235,1568,1619,1765,2091,2142,2215,3071,2564,2708,5066,2788,2804,3221,3606,3889,3932,4220,4325,4821,4942,4982,5840,5291,5378,5447,5721,6175,6328};
            
            
            subMain5(args,resLoc_ord_3);   //Case Clear Creek June 3 to 7

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain5(String args[],int[]resLoc) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("C:/CuencasDataBases/ClearCreek_Database/Rasters/Topography/ClearCreek/NED_00159011.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("C:/CuencasDataBases/ClearCreek_Database/Rasters/Topography/ClearCreek/NED_00159011.dir"));
        
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
        routingParams.put("v_o", 0.6f);
 
        routingParams.put("v_h", 0.01f);

        
        //Routing type: 2 is constant velocity v=vo, 5 variable v=vo q^l1 A^l2
        
//        new SimulationToAsciiFileTibebu(778, 368, matDirs, magnitudes, metaModif, 10000.0f, 15.0f, 30.0f, 2, new java.io.File("C:/CuencasDataBases/ClearCreek_Database/Results"), routingParams).executeSimulation();
        new SimulationToAsciiFileTibebu("3",resLoc,1570,127, matDirs, magnitudes, metaModif, 100.0f, 15.0f, 30.0f, 2, new java.io.File("C:/CuencasDataBases/ClearCreek_Database/Results"), routingParams).executeSimulation();
        //new SimulationToAsciiFileTibebu(1570, 127, matDirs, magnitudes, metaModif, 100.0f, 15.0f, 30.0f, 5, new java.io.File("C:/CuencasDataBases/ClearCreek_Database/Results"), routingParams).executeSimulation();

        System.exit(0);       
        
    }
}

