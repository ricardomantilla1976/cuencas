/*
 * simulationsRep3.java
 *
 * Created on December 6, 2001, 10:34 AM
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

/**
 *
 * @author  ricardo
 * @version 
 */

import visad.*;
import java.io.*;

public class SimulationToFileFurey extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
   
            
    /** Creates new simulationsRep3 */
    public SimulationToFileFurey(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate) throws java.io.IOException, VisADException{
        matDir=direcc;
        metaDatos=md;
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        //CHEZY VELOCITY ...
        //hydroScalingAPI.modules.rainfallRunoffModel.objects.ChezyVelocities vchannels=new hydroScalingAPI.modules.rainfallRunoffModel.objects.ChezyVelocities(linksStructure,thisHillsInfo,thisNetworkGeom);    
        //System.out.println(vchannels);
        
        System.out.println(stormFile);
        
        System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        if (!storm.isCompleted()) return;
        
        thisHillsInfo.setStormManager(storm);
                
        /*
                Escribo en un archivo lo siguiente: 
                        Numero de links
                        Numero de links Completos
                        lista de Links Completos
                        Area aguas arriba de los Links Completos
                        Orden de los Links Completos
                        maximos de la WF para los links completos
                        Longitud simulacion
                        Resulatdos
                
                */
        
        /* Walnut Gulch, AZ output path ...*/
        //String output_path="/home/furey/HSJ/walnut_az/simulations/";
        /* Goodwin Creek, MS output path ...*/
        //String output_path="/home/furey/HSJ/goodwin_ms/23jan04_gauss_KQvarl_tests/01min_rain_steps/sims_01min_2p0/";
        //String output_path="/home/furey/HSJ/goodwin_ms/08mar04_gauss_Chezy_tests/01min_rain_steps/sims_01min/var_runoffrat_uniform/";
        //String output_path="/home/furey/HSJ/goodwin_ms/sims_KQvarl_evs05min/";
        //String output_path="/home/furey/HSJ/goodwin_ms/sims_KQvarl_evs05min_x0p5/";
        //String output_path="/home/furey/HSJ/goodwin_ms/sims_KQvarl_gauss05min/";
        
        //String output_path="/home/furey/Data/goodwin_ms/rain_interp_hillts/";
        //String output_path="/home/furey/Data/goodwin_ms/rg_interpfield_hillts/events_good8195_auto/";
        
        //String output_path="/home/furey/HSJ/goodwin_ms/08jun04_wb_tests/05min_rain_steps/sims_01min/";

        //String output_path="/home/furey/Sims_Analysis/HSJ/goodwin_ms/09may05_instant_KQvarl_tests/01min_rain_pulse/sims_01min/rain_equals_runoff/";
        //String output_path="/home/furey/Sims_Analysis/HSJ/goodwin_ms/15jun05_instant_KQvarl_tests/10sec_rain_pulse/sims_01min/rain_equals_runoff/velocity_0p5mps/latest_simulation/";
        //String output_path="/home/furey/Sims_Analysis/HSJ/goodwin_ms/20jun05_instant_Mannings_tests/10sec_rain_pulse/sims_01min/rain_equals_runoff/latest_simulation/";
        //String output_path="/home/furey/Sims_Analysis/HSJ/goodwin_ms/24jun05_instant_Chezy_tests/10sec_rain_pulse/sims_01min/rain_equals_runoff/latest_simulation/";
        String output_path="/data/phlebas1/furey_data/HSJ/goodwin_ms/24may06_proposalexample/event021_dated15may83/sims_05min/rain_equals_runoff/latest_simulation/";
        
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));
        
        java.io.File archivo=new java.io.File(output_path+demName+"_"+storm.stormName()+"_IR_"+infiltRate+".dat");
        System.out.println(output_path+demName+"_"+storm.stormName()+"_IR_"+infiltRate+".dat");
        java.io.FileOutputStream salida = new java.io.FileOutputStream(archivo);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.DataOutputStream newfile = new java.io.DataOutputStream(bufferout);
        
        newfile.writeInt(linksStructure.contactsArray.length);
        newfile.writeInt(linksStructure.completeStreamLinksArray.length);
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            newfile.writeInt(linksStructure.completeStreamLinksArray[i]);
        }
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            newfile.writeFloat((float) thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i]));
        }
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            newfile.writeFloat((float) thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]));
        }
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            newfile.writeFloat((float) thisNetworkGeom.Slope(linksStructure.completeStreamLinksArray[i]));
        }
        
        System.out.println("Starting Width Functions Calculations");
        
        hydroScalingAPI.util.geomorphology.objects.Basin itsCuenca;
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis itsLinksStructure;
        // metric==0 geometric, metric==1 topologic
        int thisX,thisY,metric=0,numBins;
      
        float binsize=1;
        double[][] laWFunc;
        float[][] linkLengths=linksStructure.getVarValues(1);   // [1][n] ** HERE varValues are link lengths ... rename ... linkLengths[0][j] is length
        
        if (metric == 0) {    
            binsize=new hydroScalingAPI.util.statistics.Stats(linkLengths).meanValue;
        }
        if (metric == 1) {
            for (int i=0;i<linkLengths.length;i++) linkLengths[0][i] = 1;
        }

        RealType numLinks= RealType.getRealType("numLinks"), distanceToOut = RealType.getRealType("distanceToOut");
        FlatField vals_ff_W,hist;
        Linear1DSet binsSet;

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if (linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]] > 1) {
                /*thisX=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]%metaDatos.getNumCols();
                thisY=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]/metaDatos.getNumCols();
                itsCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(thisX,thisY,matDir,metaDatos);
                itsLinksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(itsCuenca, metaDatos, matDir);
                double[][] wFunc=itsLinksStructure.getDistancesToOutlet();
                java.util.Arrays.sort(wFunc[metric]);

                float[][] gWFunc=new float[1][wFunc[metric].length];

                for (int j=0;j<wFunc[0].length;j++)
                    gWFunc[0][j]=(float) wFunc[metric][j];

                vals_ff_W = new FlatField( new FunctionType(distanceToOut, numLinks), new Linear1DSet(distanceToOut,0,gWFunc[0].length,gWFunc[0].length));
                vals_ff_W.setSamples( gWFunc );

                numBins=(int) (gWFunc[0][gWFunc[0].length-1]/binsize)+1;
                
                //bin number as distance
                binsSet = new Linear1DSet(numLinks,binsize, gWFunc[0][gWFunc[0].length-1]+binsize,numBins);

                //bin number, and distance to outlet
                hist = visad.math.Histogram.makeHistogram(vals_ff_W, binsSet);

                laWFunc=hist.getValues();

                hydroScalingAPI.tools.Stats widthStat=new hydroScalingAPI.tools.Stats(laWFunc[0]);
                float[] maxWF={widthStat.maxValue,linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]]};
                newfile.writeFloat(maxWF[0]);
                newfile.writeFloat(maxWF[1]);*/

                newfile.writeFloat(1.0f);
                newfile.writeFloat(1.0f);
            } else {
                newfile.writeFloat(1.0f);
                newfile.writeFloat(1.0f);
            }
        }
        
        System.out.println("Termina calculo de WFs");
        
        
        //hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_S1S2 thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_S1S2(linksStructure,thisHillsInfo,thisNetworkGeom);
        //double[] initialCondition=new double[linksStructure.contactsArray.length*2];
        
        
        /* PF ADDITION - START ... */
        
        int nLi = linksStructure.contactsArray.length;
        System.out.println("nLi ="+nLi);
        
        /* Define HG relationships for widths and chezy values. */
       float[] hgwidth_params = { 5.6f, 0.46f } ;         // Goodwin Creek coeff, exp for widths
       float[] hgchezy_params = { 14.2f, -1/3.f } ;       // General coeff, exp for Chezy 
       thisNetworkGeom.setWidthsHG( hgwidth_params[0], hgwidth_params[1] ,0) ;
       thisNetworkGeom.setCheziHG( hgchezy_params[0], hgchezy_params[1]) ;     
       
        /* Define initial channel conditions.*/
        // Below, initial_qs is not defined because inital_s2 determines
        // initial_qs in NetworkEquations
        double[] initialCondition=new double[4*nLi];
        double initial_q, initial_excdepth, initial_s0, initial_s1, initial_s2;
        float[][] areasHillArray=thisHillsInfo.getAreasArray();
              
        for (int i=0;i<nLi;i++){
            //initial_q  = thisNetworkGeom.upStreamArea(i)*0.0D; //*0.0;*0.01,*0.1
            initial_excdepth = 0.0;  // m ... equals 1mm,  used 0.004
            initial_s0 = areasHillArray[0][i]*1e6*initial_excdepth;  // m^3
            initial_q  = initial_s0 / ( linkLengths[0][i]*1e3 ) ;       // m^3/s ... q = S*(v/l) where v=1m/s
            //initial_q  = Math.pow(thisNetworkGeom.upStreamArea(i)*0.0175,3.0); //*0.0;*0.01,*0.1
            initial_s1 = thisHillsInfo.S2max(i)*0.0D; //*0.0, *0.2; 
            initial_s2 = thisHillsInfo.S2max(i)*0.0D; //*0.0, *0.2;
            initialCondition[i] = initial_q;  //for link discharge
            initialCondition[i+(2*nLi)]= initial_s1;  //for s1 zone volume
            initialCondition[i+(3*nLi)]= initial_s2;  //for s2 zone volume
        }
        
        /* Define RKF resoution */
        float rkf_resolution = 1E-3f ;
        
        /* Make file specs_hillslope.txt */
        int j=-1;
        do{
            j++ ;
        } while( thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[j]) != 1 );
        
        double order = thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[j]);
        int id = linksStructure.completeStreamLinksArray[j];
        
        FileWriter hillspecs = new FileWriter(output_path + "specs_hillslope.txt");
        hillspecs.write( '\n' + "Specs for the Order-1 Hillslope :" + '\n' + '\n');
        hillspecs.write("Hillslope ID = " + id + '\n');
        hillspecs.write("Area [m3] =" + (thisHillsInfo.Area(id)*1e6) + '\n');
        hillspecs.write("K sat [m/hr] = " + thisHillsInfo.Ks(id) + '\n');
        hillspecs.write("Mst exp = " + thisHillsInfo.MstExp(id) + '\n');
        hillspecs.write("Rec param [1/hr] = " + thisHillsInfo.RecParam(id) + '\n');
        hillspecs.write("S2 param [1/m3] = " + thisHillsInfo.S2Param(id) + '\n');
        hillspecs.write("S2 max [m3] = " + thisHillsInfo.S2max(id) + '\n');
        hillspecs.write("ET rate [m/hr] = " + thisHillsInfo.ETrate(id) + '\n');
        hillspecs.write("Initial discharge [m3/s] =" + initialCondition[id] + '\n');
        hillspecs.write("Initial s1 value [m3] =" + initialCondition[id+(2*linksStructure.contactsArray.length)] + '\n');
        hillspecs.write("Initial s2 value [m3] =" + initialCondition[id+(3*linksStructure.contactsArray.length)] + '\n');
        hillspecs.write("Initial moisture =" + 
            initialCondition[id+(2*linksStructure.contactsArray.length)]/
            (thisHillsInfo.S2max(id)-initialCondition[id+(3*linksStructure.contactsArray.length)]) + '\n');
        hillspecs.close();
        
        /* Make file specs_network.txt */
        double width_tot, length_tot, slope_tot;
        width_tot = length_tot = slope_tot = 0.0;
        
        for (int i=0;i<nLi;i++){
                width_tot += thisNetworkGeom.Width(i);
                length_tot += thisNetworkGeom.Length(i);
                slope_tot += thisNetworkGeom.Slope(i);
                //System.out.println("way");
            }   
        
        FileWriter networkspecs = new FileWriter(output_path + "specs_network.txt");
        networkspecs.write( '\n' + "Specs for basin network :" + '\n' + '\n');
        networkspecs.write("Number of links = " + nLi + '\n');
        networkspecs.write("Mean link width = " + (width_tot/nLi) + '\n');
        networkspecs.write("Mean link length = " + (length_tot/nLi) + '\n');
        networkspecs.write("Mean link slope = " + (slope_tot/nLi) + '\n'+'\n'+'\n');
        networkspecs.write("HG width-area params : coeff =" + hgwidth_params[0] + ", expon =" + hgwidth_params[1]+'\n');
        networkspecs.close();
        
        /* Make specs_channel.txt */
        FileWriter channelspecs = new FileWriter(output_path + "specs_channel.txt");
        channelspecs.write("HG chezy-slope params : coeff =" + hgchezy_params[0] + ", expon =" + hgchezy_params[1]+'\n');
        channelspecs.close();
        
        /* Make specs_rkf.txt */
        FileWriter rkfspecs = new FileWriter(output_path + "specs_rkf.txt");
        rkfspecs.write("RKF Resolution = " + rkf_resolution + '\n');
        rkfspecs.close();
        
        // Additional info for GOODWIN CREEK ...Contact point (x,y) values used below, except for basin outlet.
        FileWriter sg_IDvals = new FileWriter(output_path + "sg_arrayIDs.txt");
        sg_IDvals.write("Basin, resSimul ID for flow at outlet, Area (km^2)" + '\n'+'\n');
        for(int i=0;i<14;i++){
            int[] sg_nums= {1,2,3,4,5,6,7,8,9,10,11,12,13,14};
            int[][] xyval = {{55,114},{111,180},{204,219},{196,209},{256,227}, //sgs 1 - 5
            {240,256},{243,199},{325,249},{325,250},{319,225},{344,233},                //sgs 6-11
            {369,239},{127,187},{172,191}};                                     //sgs 12-14
            int resSimID = linksStructure.getResSimID(xyval[i][0],xyval[i][1]);
            sg_IDvals.write(sg_nums[i] + "   " + resSimID + "  " + thisNetworkGeom.upStreamArea(resSimID-1) + '\n');
            //System.out.println(linksStructure.contactsArray(xyval[i][0],xyval[i][1]).length);   //contactsArray.length
        }
        sg_IDvals.close();
        
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/4.0));
        System.out.println("Inicia simulacion RKF");
        
        /* PF ADDITION - ... END */
        
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_S1S2 thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_S1S2(linksStructure,thisHillsInfo,thisNetworkGeom);
        
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,rkf_resolution,10/60.);
        
        int numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
        
        System.out.println("Num files ="+(numPeriods+1));
        newfile.writeInt(numPeriods);
        
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        //*System.out.println("Initial time " + storm.stormInitialTimeInMinutes());
        
        //SET INITIAL STORM TIME
        //double storm_initialTime = storm.stormInitialTimeInMinutes();  // Uses current time, so it changes between simulations
        double storm_initialTime = 0.0d ;                                                       // Uses fixed time
        
        //OUTPUT DURING RAINFALL - DISCHARGE [m3/s] AND STORAGE [m3] VALUES WRITTEN TO FILE
        //Duration is determined by the rainfall file used in subMain2,Time step (resolution) is 1 minute ... when RecordResolutionInMinutes(),1.0
        //NOTE: jumpsRunToFile is a method in RKF class.
        //Need to set incremental time in jumpsRunToFile (eg. 5 min, or 15 min)
        for (int k=0;k<numPeriods;k++) {
            //System.out.println("File "+k);
            //System.out.println(storm.stormRecordResolutionInMinutes());
            rainRunoffRaining.jumpsRunToFile(storm_initialTime+k*storm.stormRecordResolutionInMinutes(),storm_initialTime+(k+1)*storm.stormRecordResolutionInMinutes(),1.0,initialCondition,newfile);
            initialCondition=rainRunoffRaining.finalCond;
            rainRunoffRaining.setBasicTimeStep(10/60.);
        }
	java.util.Date interTime=new java.util.Date();
        System.out.println("Intermedia Time:"+interTime.toString());
	System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
        
        //OUTPUT AFTER RAINFALL - DISCHARGE [m3/s] AND STORAGE [m3] VALUES WRITTEN TO FILE
        //Duration is 400 minutes, Time step (resolution) is 10 minutes ... when +400,10
        initialCondition=rainRunoffRaining.finalCond;
        rainRunoffRaining.jumpsRunToFile(storm_initialTime+numPeriods*storm.stormRecordResolutionInMinutes(),storm_initialTime+(numPeriods*5)+500.,1,initialCondition,newfile);
        
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        System.out.println("Inicia escritura de Resultados");
        
        
       // OUTPUT TO FILE - precip ts values for each hillslope
        System.out.println("Inicia escritura de Precipitaciones");
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(thisHillsInfo.Area(i));
        }
        
        newfile.writeInt(numPeriods);
        
        for (int k=0;k<numPeriods;k++) {
            double currTime=storm_initialTime+k*storm.stormRecordResolutionInMinutes();
            
            for (int i=0;i<linksStructure.contactsArray.length;i++){
                
                newfile.writeDouble(thisHillsInfo.precipitation(i,currTime));
                
            }
            
        }
        
        System.out.println("Termina escritura de Precipitaciones");
        for (int i=0;i<linksStructure.connectionsArray.length;i++){
            newfile.writeInt(linksStructure.connectionsArray[i].length);
            if (linksStructure.connectionsArray[i].length > 0) {
                for (int jj=0;jj<linksStructure.connectionsArray[i].length;jj++){
                    newfile.writeInt(linksStructure.connectionsArray[i][jj]);
                }
            }
        }
        
        
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina escritura de Resultados");
        
    
        
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
        try{
            
            //Real Rain on Walnut Gulch
            subMain1(args);
            
            //Multifractal Rain on Peano
            //subMain2(args);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        
    }
    
    public static void subMain1 (String args[]) throws java.io.IOException, VisADException {
        
        /* Goodwin Creek, MS ... */
        //String topo_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM";
        //String topo_path = "/hidrosigDataBases/DataBaseNSF_Project/BDRaster/Topografia/1_ArcSec/goodwin_MS/goodwinMS.metaMDT";
        String topo_path = "/data/phlebas1/cuencas_databases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM";
        
        //String rain_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/interpolated_events/05min_ts/";
        //String rain_path = "/hidrosigDataBases/DataBaseNSF_Project/BDRaster/Hidrologia/precipitation/storm/dataOverGoodwinCreek/05min_ts/";  
        //String rain_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/interpolated_events/05min_ts/";
        //String rain_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/shortpulse_events/01min/";
        //String rain_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/shortpulse_events/10sec/";
        String rain_path = "/data/phlebas1/cuencas_databases/Goodwin_Creek_MS_database/Rasters/Hydrology/precipitation/storms/interpolated_events/05min_ts/";
        
        java.io.File theFile=new java.io.File(topo_path);

        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        java.io.File stormFile;
        
        /* OUTLET COORDS EXAMINED BELOW (x,y) ...
           WALNUT GULCH, AZ ... (210,330),(855,194),(736,401)
           Goodwin Creek, MS ...  (44,111) */
        
        //int[] events={21,32,36,47,48,49,63,66};  //events with R^2=0.99
        //int[] events={8,29,35,52}; //events with R^2=0.98,0.97
        //int[] events={1,2,};
        //int[] events = new int[72];
        int[] events = {21}; //,16,17,18,19,20};
        //for (int count=0;count<72;count++) events[count]=count+1;
        //int[] events={5,6,7,8};
        //int[] events={11}; //,12,13,14,15};
        //String[] gauss_types=
        //    {"_sd0p01_coe1p00","_sd0p05_coe1p00","_sd0p10_coe1p00","_sd0p20_coe1p00",
        //   "_sd0p30_coe1p00","_sd0p40_coe1p00","_sd0p01_coe0p25","_sd0p05_coe0p85","_sd0p10_coe0p95"}; // in order of event number
        //String[] gauss_types={"_sd0p05_coe1p00_L","_sd0p20_coe1p00_L","_sd0p40_coe1p00_L"};
        //String[] gauss_types={"_sd0p05_coe1p00_R","_sd0p20_coe1p00_R","_sd0p40_coe1p00_R"};
        //String[] gauss_types={"_sd1p00_coe2p85","_sd2p00_coe2p85","_sd4p00_coe2p85","_sd6p00_coe2p85"};
        //String[] gauss_types={"_sd1p00_coe1p42","_sd2p00_coe1p42","_sd4p00_coe1p42","_sd6p00_coe1p42"};
        //String[] gauss_types={"_sd2p00_coe1p42" };  //{"_sd2p00_coe1p42","_sd2p00_coe2p85","_sd2p00_coe4p27","_sd2p00_coe5p70","_sd2p00_coe7p12"};
        //String[] gauss_types={"_sd6p00_coe7p12" };  //{"_sd6p00_coe1p42","_sd6p00_coe2p85","_sd6p00_coe4p27","_sd6p00_coe5p70","_sd6p00_coe7p12"};
        //String[] pulse_types={"_005_001"};  // 005 = 5 mm/hr rate, 001 = 1 min duration
        //String[] pulse_types={"_0p0_010"};  // 030 = 30 mm/hr rate, 001 = 1 min duration
        //String[] pulse_types={"_120_001"};  // 120 = 120 mm/hr = 2 mm/min, 001 = 1 min duration
        
        String evNUM,evStamp;
        for (int eventsid=0;eventsid<events.length;eventsid++){
        //for (int eventsid=0;eventsid<1;eventsid++){
            int evID = events[eventsid];
            //evNUM=(""+(evID/100.+0.001)).substring(2,4);
            evNUM=(""+(evID/1000.+0.0001)).substring(2,5);
            evStamp="_ev"+evNUM;
            System.out.println("Event number ="+evNUM+" Stamp ="+evStamp);
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_interpolated"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_meanrgts"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_meanfdts"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+gauss_types[eventsid]+"/precipitation_gaussian"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+gauss_types[eventsid]+"/precipitation_gaussianL"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+gauss_types[eventsid]+"/precipitation_gaussianR"+evStamp+".metaVHC");
            stormFile=new java.io.File(rain_path+"events_singpeakB_rain01"+"/event_"+evNUM+"/precipitation_interpolated"+evStamp+".metaVHC");
            System.out.println(rain_path+"events_singpeakB_rain01"+"/event_"+evNUM+"/precipitation_interpolated"+evStamp+".metaVHC");
            
            //System.out.println(rain_path+"precipitation_shortpulse"+pulse_types[0]+".metaVHC");
            //stormFile=new java.io.File(rain_path+"precipitation_shortpulse"+pulse_types[0]+".metaVHC");
            
            //new simulationsRep4(210,330,matDirs,matDirsPruned,magnitudes,metaModif,stormFile,0.0f);
            //new simulationsRep4(210,330,matDirs,matDirsPruned,magnitudes,metaModif,stormFile,50.0f);
            //new SimulationToFileFurey(310,378,matDirs,magnitudes,metaModif,stormFile,0.0f);
            new SimulationToFileFurey(44,111,matDirs,magnitudes,metaModif,stormFile,0.0f);
            
        }
        
        System.exit(0);
        
    }
    
}
