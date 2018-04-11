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

public class SimulationToFileFurey_april07 extends java.lang.Object {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;


    /** Creates new simulationsRep3 */
    public SimulationToFileFurey_april07(String [] sim_metadata, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate) throws java.io.IOException, VisADException{

        int x = java.lang.Integer.parseInt(sim_metadata[3]);
        int y = java.lang.Integer.parseInt(sim_metadata[4]);
        matDir=direcc;
        metaDatos=md;

        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);

        System.out.println(stormFile);

        System.out.println("Loading Storm ...");

        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        if (!storm.isCompleted()) return;

        thisHillsInfo.setStormManager(storm);

        String output_path=sim_metadata[2];
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));
        String fileSuffix=".NOdat";

        if (java.lang.Integer.parseInt(sim_metadata[24]) == 0) fileSuffix = ".dat";
        if (java.lang.Integer.parseInt(sim_metadata[24]) == 1)  fileSuffix = "_allLinks.dat";

        java.io.File archivo=new java.io.File(output_path+demName+"_"+storm.stormName()+"_IR"+infiltRate+fileSuffix);
        System.out.println(output_path+demName+"_"+storm.stormName()+"_IR"+infiltRate+fileSuffix);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(archivo);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.DataOutputStream newfile = new java.io.DataOutputStream(bufferout);

        switch (java.lang.Integer.parseInt(sim_metadata[24])) {

            case 0:      // Complete Stream Links Sampled

            System.out.println("Complete Links will be Sampled");

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
               break;

            case 1:         // All Links Sampled

            System.out.println("All Links will be Sampled");

            newfile.writeInt(linksStructure.contactsArray.length);
            newfile.writeInt(linksStructure.completeStreamLinksArray.length);

            for (int i=0;i<linksStructure.contactsArray.length;i++){
               newfile.writeInt(i);
            }
            for (int i=0;i<linksStructure.contactsArray.length;i++){
              newfile.writeFloat((float) thisNetworkGeom.upStreamArea(i));
            }
            for (int i=0;i<linksStructure.contactsArray.length;i++){
               newfile.writeFloat((float) thisNetworkGeom.linkOrder(i));
            }
            for (int i=0;i<linksStructure.contactsArray.length;i++){
               newfile.writeFloat((float) thisNetworkGeom.Slope(i));
            }
               break;

        }


        int nLi = linksStructure.contactsArray.length;
        System.out.println("nLi ="+nLi);

        /* Define HG relationships for widths and chezy values. */
       float width_coe = java.lang.Float.parseFloat(sim_metadata[17]);
       float width_exp = java.lang.Float.parseFloat(sim_metadata[18]);
       float constvelocity = java.lang.Float.parseFloat(sim_metadata[21]);
       float chezy_coe = java.lang.Float.parseFloat(sim_metadata[22]);
       float chezy_exp = java.lang.Float.parseFloat(sim_metadata[23]);
       float[] width_params = { width_coe, width_exp } ;
       float[] chezy_params = { chezy_coe, chezy_exp } ;
       thisNetworkGeom.setWidthsHG( width_params[0], width_params[1] ,0) ;
       thisNetworkGeom.setVqParams(constvelocity,0,0,0);
       //thisNetworkGeom.setConstantVelHG( constvelocity ) ;
       thisNetworkGeom.setCheziHG( chezy_params[0], chezy_params[1]) ;

        /*  DEFINE INITIAL CONDITIONS  for NetworkEquations_S0S2.java .*/
        // Below, initial_qs is not defined because inital_s2 determines
        // initial_qs in NetworkEquations
        double[] initialCondition=new double[4*nLi];
        double initial_s0_depth, initial_s0, initial_s1, initial_s2, initial_link_depth, initial_link_width, initial_q;
        float hillslopeLength;
        float[][] areasHillArray=thisHillsInfo.getAreasArray();
        float[][] linkLengths=linksStructure.getVarValues(1);   // [1][n] ** HERE varValues are link lengths ... rename ... linkLengths[0][j]

        for (int i=0;i<nLi;i++){
            initial_link_depth =  java.lang.Float.parseFloat(sim_metadata[16]);
            initial_link_width = 0.0; //thisNetworkGeom.Width(i);
            initial_s0_depth = java.lang.Float.parseFloat(sim_metadata[12]);  // m
            initial_s0 = initial_s0_depth*(areasHillArray[0][i]*1e6);  // m^3
            initial_s2 = thisHillsInfo.S2max(i)* java.lang.Double.parseDouble(sim_metadata[14]); //*0.0, *0.2;
            //initial_q  = (initial_link_depth*areasHillArray[0][i]*1e6) * (   constvelocity  / (linkLengths[0][i]*1e3)   ) ;       // m^3/s .. For infinite vh .... q = S*(v/l) where v in m/s =  java.lang.Float.parseFloat(sim_metadata[21])
            initial_q  = (initial_link_depth*linkLengths[0][i]*1e3*initial_link_width) * (   constvelocity /(linkLengths[0][i]*1e3)   );  // initial_q = 0 here
            initialCondition[i] = initial_q;                   //  m^3/s, for link discharge
            hillslopeLength = ((areasHillArray[0][i]*1e6f) / (linkLengths[0][i] *1e3f)) / 2.0f ;    // length on one hillslope (one side of link) is half of link length
            initialCondition[i+(2*nLi)]= initial_s0 * (0.01*constvelocity/hillslopeLength   );  // m^3/s, for s0 zone discharge
            initialCondition[i+(3*nLi)]= initial_s2;      //for s2 zone volume
        }

        /*  DEFINE INITIAL CONDITIONS  for NetworkEquations_S1S2.java .*/
        // Below, initial_qs is not defined because inital_s2 determines
        // initial_qs in NetworkEquations
        /* double[] initialCondition=new double[4*nLi];
        double initial_s0_depth, initial_s0, initial_s1, initial_s2, initial_link_depth, initial_link_width, initial_q;
        float[][] areasHillArray=thisHillsInfo.getAreasArray();
        for (int i=0;i<nLi;i++){
            initial_link_depth =  java.lang.Float.parseFloat(sim_metadata[16]);
            initial_link_width = 0.0;
            initial_s1 = thisHillsInfo.S2max(i)* java.lang.Double.parseDouble(sim_metadata[13]); //*0.0, *0.2;
            initial_s2 = thisHillsInfo.S2max(i)* java.lang.Double.parseDouble(sim_metadata[14]); //*0.0, *0.2;
            //initial_q  = thisNetworkGeom.upStreamArea(i)*0.0D; //*0.0;*0.01,*0.1
            //initial_q  = Math.pow(thisNetworkGeom.upStreamArea(i)*0.0175,3.0); //*0.0;*0.01,*0.1
            initial_q  = (initial_link_depth*linkLengths[0][i]*1e3*initial_link_width) * (   constvelocity /(linkLengths[0][i]*1e3)   );
            initialCondition[i] = initial_q;                   //  m^3/s, for link discharge
            initialCondition[i+(2*nLi)]= initial_s1;    //for s1 zone volume
            initialCondition[i+(3*nLi)]= initial_s2;      //for s2 zone volume
        } */

        /* Define RKF resoution */
        float rkf_resolution =  java.lang.Float.parseFloat(sim_metadata[8]);

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
        networkspecs.write("HG width-area params : coeff =" + width_params[0] + ", expon =" + width_params[1]+'\n');
        networkspecs.close();

        // GOODWIN CREEK info used in IDL water balance program
        // Contact point (x,y) values used below; includes gauge 1 but not basin outlet.
        // These are coords for pixel before link joins others downstream.
        // Link ID used to access connections structure = resSimID -1
        FileWriter sg_IDvals = new FileWriter(output_path + "sg_arrayIDs.txt");
        sg_IDvals.write("Basin, resSimul ID for flow at outlet, Area (km^2)" + '\n'+'\n');
        for(int i=0;i<14;i++){
            int[] sg_nums= {1,2,3,4,5,6,7,8,9,10,11,12,13,14};
            // IDS from GCEW extracted prior to 16 April 2007
            int[][] xyval = {{55,114},{111,180},{204,219},{196,209},{256,227}, //sgs 1 - 5
            {240,256},{243,199},{325,249},{325,250},{319,225},{344,233},                //sgs 6-11
            {369,239},{127,187},{172,191}};                                     //sgs 12-14
            // IDS from GCEW extracted on 16 April 2007
            //int[][] xyval = {{54,114},{111,180},{204,219},{198,209},{257,226}, //sgs 1 - 5
            //{240,256},{241,199},{326,249},{326,250},{322,226},{345,236},                //sgs 6-11
            //{372,240},{129,187},{171,191}};                                     //sgs 12-14
            int resSimID = linksStructure.getResSimID(xyval[i][0],xyval[i][1]);
            //System.out.println(resSimID);
            sg_IDvals.write(sg_nums[i] + "   " + resSimID + "  " + thisNetworkGeom.upStreamArea(resSimID-1) + '\n');
            //System.out.println(linksStructure.contactsArray(xyval[i][0],xyval[i][1]).length);   //contactsArray.length
        }
        sg_IDvals.close();

        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/4.0));
        System.out.println("Inicia simulacion RKF");


        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_S0S2 thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_S0S2(linksStructure,thisHillsInfo,thisNetworkGeom,null);
        //hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_S1S2 thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_S1S2(linksStructure,thisHillsInfo,thisNetworkGeom);

        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,rkf_resolution,10/60.);

        int numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());

        System.out.println("Num files ="+(numPeriods+1));
        newfile.writeInt(numPeriods);

        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        //*System.out.println("Initial time " + storm.stormInitialTimeInMinutes());

        //SET STORM CONDITIONS
        // Do not fix time, or else precitation becomes zero via workings of *.getPrec class. (ie. no ... double storm_initialTime = 0.0d)
        double storm_initialTime = storm.stormInitialTimeInMinutes();  // Uses current time, so it changes between simulations
        double timestep_res, norain_duration ;

        //OUTPUT DURING RAINFALL - DISCHARGE [m3/s] AND STORAGE [m3] VALUES WRITTEN TO FILE
        //Duration is determined by the rainfall file used in subMain2,Time step (resolution) is 1 minute ... when RecordResolutionInMinutes(),1.0
        //NOTE: jumpsRunToFile is a method in RKF class.
        //Need to set incremental time in jumpsRunToFile (eg. 5 min, or 15 min)
        timestep_res = java.lang.Double.parseDouble(sim_metadata[5]);     //eg. 5 min
        for (int k=0;k<numPeriods;k++) {
            rainRunoffRaining.jumpsRunToFile(storm_initialTime+k*storm.stormRecordResolutionInMinutes(),storm_initialTime+(k+1)*storm.stormRecordResolutionInMinutes(),timestep_res,initialCondition,newfile);
            initialCondition=rainRunoffRaining.finalCond;
            rainRunoffRaining.setBasicTimeStep(10/60.);     //1/6 min = 10 sec.
        }
	java.util.Date interTime=new java.util.Date();
        System.out.println("Intermedia Time:"+interTime.toString());
	System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");

        //OUTPUT AFTER RAINFALL - DISCHARGE [m3/s] AND STORAGE [m3] VALUES WRITTEN TO FILE
        //Duration is 400 minutes, Time step (resolution) is 10 minutes ... when +400,10
        timestep_res = java.lang.Double.parseDouble(sim_metadata[6]);           //eg. 10
        norain_duration = java.lang.Double.parseDouble(sim_metadata[7]);      //eg. 400
        System.out.println(timestep_res + "  "+ norain_duration);
        initialCondition=rainRunoffRaining.finalCond;
        rainRunoffRaining.jumpsRunToFile(storm_initialTime+numPeriods*storm.stormRecordResolutionInMinutes(),storm_initialTime+(numPeriods*5)+norain_duration,timestep_res,initialCondition,newfile);

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

        //System.out.println(numPeriods);
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

        String [] sim_metadata = new String[25];
        int count = 0;
        boolean eof = false;
        String metafile_path = "/data/cora3/furey/cuencas_simoutput/goodwin_ms/26jun06_instant_KQvarl_tests/latest_simulation/SimsToFileFurey.meta";
        System.out.println("Accessing " + metafile_path);

        try{
        java.io.File metafile=new java.io.File(metafile_path);
        java.io.BufferedReader input  = new java.io.BufferedReader(new java.io.FileReader(metafile));
        while(!eof){
              String line0=input.readLine();
              if (line0==null) {
                    eof=true;
                    System.out.println("Finished reading simulation metafile");
               }
               else {
                    if (count < 25) {             //(line0.charAt(0)=='[')
                    String line1=input.readLine();
                    String line2=input.readLine();
                    //System.out.println(line1);
                    //System.out.println(line2);
                    sim_metadata[count] = line1;
                    count=count+1;
                   }
               }
        }
        input.close();
        } catch (java.io.IOException IOE){
            System.err.println("An Error has ocurred while reading metafile");
            System.err.println(IOE);
        }

        String topo_path =sim_metadata[0];
        String rain_path = sim_metadata[1];

        java.io.File theFile=new java.io.File(topo_path);

        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));

        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        //hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();

        java.io.File stormFile;

        // INTERPOLATED DATA NAMES:
        //int[] event_num={1};
        //int[] event_num={21,32,36,47,48,49,63,66};  //events with R^2=0.99
        //int[] event_num={8,29,35,52}; //events with R^2=0.98,0.97
        //int[] event_num = new int[72];
        //int[] event_num = {21}; //,16,17,18,19,20};
        //for (int count=0;count<72;count++) events[count]=count+1;
        //int[] event_num={5,6,7,8};
        //int[] event_num={11}; //,12,13,14,15};
        /*
        int s_event = 1;
        int e_event=  20;
        String[] event_nums = new String[e_event-s_event+1];
        for(int i=s_event;i<e_event+1;i++) {
            if ( i<10 ) event_nums[i-s_event] = "00"+java.lang.Integer.toString( i );
            if (( i>9 )  &&  (i<100 )) event_nums[i-s_event] = "0"+java.lang.Integer.toString( i );
            if ( i>99 ) event_nums[i-s_event] = java.lang.Integer.toString( i );
        }
        */
        //System.out.println(event_nums[0]);
        //System.out.println(event_nums[1]);

        // ARTIFICIAL GAUSS DATA NAMES:
        //String[] event_type= {"_sd0p01_coe1p00","_sd0p05_coe1p00","_sd0p10_coe1p00","_sd0p20_coe1p00", "_sd0p30_coe1p00","_sd0p40_coe1p00","_sd0p01_coe0p25","_sd0p05_coe0p85","_sd0p10_coe0p95"}; // in order of event number
        //String[] event_type={"_sd0p05_coe1p00_L","_sd0p20_coe1p00_L","_sd0p40_coe1p00_L"};
        //String[] event_type={"_sd0p05_coe1p00_R","_sd0p20_coe1p00_R","_sd0p40_coe1p00_R"};
        //String[] event_type={"_sd1p00_coe2p85","_sd2p00_coe2p85","_sd4p00_coe2p85","_sd6p00_coe2p85"};
        //String[] event_type={"_sd1p00_coe1p42","_sd2p00_coe1p42","_sd4p00_coe1p42","_sd6p00_coe1p42"};
        //String[] event_type={"_sd2p00_coe1p42" };  //{"_sd2p00_coe1p42","_sd2p00_coe2p85","_sd2p00_coe4p27","_sd2p00_coe5p70","_sd2p00_coe7p12"};
        //String[] event_type={"_sd6p00_coe7p12" };  //{"_sd6p00_coe1p42","_sd6p00_coe2p85","_sd6p00_coe4p27","_sd6p00_coe5p70","_sd6p00_coe7p12"};
        // ARTIFICIAL PULSE DATA NAMES:
        //String[] event_type={"_005_001"};  // 005 = 5 mm/hr rate, 001 = 1 min duration
        //String[] event_type={"_120_001"};  // 120 = 120 mm/hr = 2 mm/min, 001 = 1 min duration
        String[] event_type={"_0p0_010"};  //  0 mm/10sec rate, 010 = 10 sec duration
        //String[] event_type={"_720_010"};  // 1 mm/10sec rate, 010= 10 sec duration

        // CREATE stormFile AND RUN SIMULATION ...
        //String evNUM,evStamp;
        //for (int i=0;i<event_nums.length;i++){
        for (int i=0;i<1;i++){
            // INTERPOLATED DATA NAMES:
            //int evID = event_num[i];
            //evNUM=(""+(evID/100.+0.001)).substring(2,4);
            //evNUM=(""+(evID/1000.+0.0001)).substring(2,5);   // events as 001 instead of 01
            //evStamp="_ev"+evNUM;
            //System.out.println("Event number ="+evNUM+",   Stamp ="+evStamp);
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_interpolated"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_meanrgts"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_meanfdts"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+event_nums[i]+"/precipitation_interpolated_ev"+event_nums[i]+".metaVHC");
            // ARTIFICIAL GAUSS DATA NAMES:
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+gauss_types[eventsid]+"/precipitation_gaussian"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+gauss_types[eventsid]+"/precipitation_gaussianL"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+gauss_types[eventsid]+"/precipitation_gaussianR"+evStamp+".metaVHC");
             // ARTIFICIAL PULSE DATA NAMES:
            stormFile=new java.io.File(rain_path+"precipitation_shortpulse"+event_type[0]+".metaVHC");

            System.out.println(stormFile);
            new SimulationToFileFurey_april07(sim_metadata,matDirs,magnitudes,metaModif,stormFile,0f);

        }

        System.exit(0);

    }

}
