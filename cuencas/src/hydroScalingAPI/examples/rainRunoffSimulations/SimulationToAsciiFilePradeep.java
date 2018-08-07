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


package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class SimulationToAsciiFilePradeep extends java.lang.Object implements Runnable{
    
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
    int resolution;
    java.io.File outputDirectory;
    
    /** Creates new simulationsRep3 */
    public SimulationToAsciiFilePradeep(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, int resolution, float infiltRate, int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,resolution,null,infiltRate,routingType,outputDirectory);
    }
    public SimulationToAsciiFilePradeep(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, int resolution, float infiltRate,int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,resolution,null,0.0f,routingType,outputDirectory);
    }
    public SimulationToAsciiFilePradeep(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, int resolution, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int routingType, java.io.File outputDirectory) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,resolution,infiltMetaRaster,0.0f,routingType,outputDirectory);
    }
    public SimulationToAsciiFilePradeep(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
                                 hydroScalingAPI.io.MetaRaster md, float rainIntensityOR, float rainDurationOR, java.io.File stormFileOR ,int resolutionOR, hydroScalingAPI.io.MetaRaster infiltMetaRasterOR, float infiltRateOR, int routingTypeOR, java.io.File outputDirectoryOR) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        rainIntensity=rainIntensityOR;
        rainDuration=rainDurationOR;
        stormFile=stormFileOR;
        resolution=resolutionOR;
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
        thisNetworkGeom.setWidthsHG(1.0f, 0.4f,0.0f);
        
        float chezLawExpon=-1/3f;
        float chezLawCoeff=200f/(float)Math.pow(0.000357911,chezLawExpon);
        thisNetworkGeom.setCheziHG(chezLawCoeff, chezLawExpon);
        
        //PARAMETERS FOR GK ROUTING
        float lam1=0.3f; //0.3
        float lam2=-0.1f;//-0.1
        float vo=(float)(1.0/Math.pow(200,lam1)/Math.pow(1100,lam2));
        thisNetworkGeom.setVqParams(vo,0.0f,lam1,lam2);
        
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
              
        if(stormFile == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-INT_"+rainIntensity+"-DUR_"+rainDuration+"-IR_"+infiltRate+"-Routing_"+routingString+chezLawExpon+".hydrographs");
        else
            //theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-KICT"+stormFile.getName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_SDS_03_"+resolution+"_dat.hydrographs");
            //theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"UnifNoise"+stormFile.getName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_60.0_10.0_dat.hydrographs");
            //theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-IR_"+infiltRate+"_"+routingString+"PED"+resolution+".hydrographs");
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-IR_"+infiltRate+"_"+routingString+"Data.hydrographs");
        
        System.out.println(theFile);
        
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        // Indices corresponding to the maximum areas fo each Horton order
        int[] ind = new int [10];
        ind[0] = 1454; ind[1] = 1976; ind[2] = 4259; ind[3] = 4193; ind[4] = 87; ind[5] = 1788; ind[6] = 2397; ind[7] = 191;
        
        newfile.write("Time\t");
        for (int i=0;i<8;i++){
             newfile.write("Link-"+linksStructure.completeStreamLinksArray[ind[i]]+"\t");
        }
              
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_ChannelLosses thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_ChannelLosses(linksStructure,thisHillsInfo,thisNetworkGeom,routingType);
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
                //rainRunoffRaining.jumpsRunToAsciiFileTabs(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
                rainRunoffRaining.jumpsRunToAsciiFileTabs(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,5,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            rainRunoffRaining.jumpsRunToAsciiFileTabs(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+8000,5,initialCondition,newfile,linksStructure,thisNetworkGeom);
            
        } else {
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period "+(k+1)+" of "+numPeriods);
                rainRunoffRaining.jumpsRunToAsciiFileTabs(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),5,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }
            
            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");            
            rainRunoffRaining.jumpsRunToAsciiFileTabs(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+8000,5,initialCondition,newfile,linksStructure,thisNetworkGeom);
        }
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
       
        // Printing the Horton orders, areas and peak flows
        java.io.File peakFile;              
        if(stormFile == null)
            peakFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-INT_"+rainIntensity+"-DUR_"+rainDuration+"-IR_"+infiltRate+"-Routing_"+routingString+chezLawExpon+".peaks");
        else
            //peakFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-KICT"+stormFile.getName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_SDS_03_"+resolution+"_dat.hydrographs");
            //peakFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"UnifNoise"+stormFile.getName()+"-IR_"+infiltRate+"-Routing_"+routingString+"_params_60.0_10.0_dat.peaks");
            //peakFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-IR_"+infiltRate+"_"+routingString+"PED"+resolution+".peaks");
            peakFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-IR_"+infiltRate+"_"+routingString+"Data.peaks");
        System.out.println(peakFile);
        
        java.io.FileOutputStream fout = new java.io.FileOutputStream(peakFile);
        java.io.BufferedOutputStream buffer = new java.io.BufferedOutputStream(fout);
        java.io.OutputStreamWriter fileOut = new java.io.OutputStreamWriter(buffer);      

        double[] maximumsAchieved=rainRunoffRaining.getMaximumAchieved();
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
             fileOut.write("Link-"+linksStructure.completeStreamLinksArray[i]+"\t");        
             fileOut.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+"\t");
             fileOut.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+"\t");
             fileOut.write(maximumsAchieved[linksStructure.completeStreamLinksArray[i]]+"\n");
        }        
        fileOut.close();
        buffer.close();
        
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
            subMain2(args);   //Case for Walnut Gulch
            //subMain3(args);     //Case Upper Rio Puerco
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain2(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/u/ac/pmandapa/cuencasDatabases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
        //java.io.File theFile=new java.io.File("C:/Documents and Settings/pmandapa/My Documents/CuencasDatabases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/u/ac/pmandapa/cuencasDatabases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.dir"));
        //metaModif.setLocationBinaryFile(new java.io.File("C:/Documents and Settings/pmandapa/My Documents/CuencasDatabases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
       //hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        //new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  50,360,0.0f,5,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/Cuencas/GKRouting/Sc1IntensityORDuration")).executeSimulation();
        //new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  5,120,1,0.0f,2,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/Cuencas/CVRouting/Sc1IntensityORDuration")).executeSimulation();
        
        String m = "";
        String resStr = "";
        for (int i=0;i<1;i++) 
        {
            int ii = i+1;            
            if (ii < 10)
            {
                m = "00"+ii;
            }
            if (ii >= 10 && ii < 100)
            {
                m = "0"+ii;
            }
            if (ii == 100)
            {
                m = String.valueOf(ii);
            }
            /*for (int j=0;j<5;j++)
            {
                int res = (int) Math.pow(2,j);
                if (res < 10)
                {
                    resStr = "0"+res;
                }
                else
                {
                    resStr = String.valueOf(res);
                }
                java.io.File stormFile;
                stormFile=new java.io.File("C:/Documents and Settings/pmandapa/My Documents/ForCuencas/BinKICT_2007_05_06t07_"+resStr+"_SDS"+m+"/prec.metaVHC");
                new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  stormFile,res,0.0f,5,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/RainfallDownscaling/Venugopal/KICT_2007_05_06t07/SDS_"+m)).executeSimulation();
            }*/
            java.io.File stormFile;
            stormFile=new java.io.File("/u/ac/pmandapa/ForCuencas/RRErrors/BinKICT_2006_07_26t27/Data/prec.metaVHC");
            new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  stormFile,ii,0.0f,2,new java.io.File("/u/ac/pmandapa/Results/Cuencas/CVRouting/RRErrors/KICT_2006_07_26t27")).executeSimulation();
            //stormFile=new java.io.File("C:/Documents and Settings/pmandapa/My Documents/ForCuencas/AWR/BinScUnifNoise_60.0/Case"+ii+"/prec.metaVHC");
            //new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  stormFile,ii,0.0f,2,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/Cuencas/CVRouting/ScUnifNoise/Case"+ii)).executeSimulation();
            //new SimulationToAsciiFilePradeep(1063,496,matDirs,magnitudes,metaModif,  stormFile,0.0f,2,new java.io.File("E:/Documents and Settings/pmandapa/My Documents/Research/Cuencas/CVRouting/KICT_2007_09_15t15")).executeSimulation();
            
        }
    }    
}
