/*
 * simulationsRep3.java
 *
 * Created on December 6, 2001, 10:34 AM
 */

package hydroScalingAPI.examples.rainRunoffSimulations;
import java.util.TimeZone;
/**
 *
 * @author  ricardo
 * @version 
 */

import visad.*;
import java.io.*;

public class HillslopeRainToFileFurey extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
   
            
    /** Creates new simulationsRep3 */
    public HillslopeRainToFileFurey(String [] sim_metadata, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate) throws java.io.IOException, VisADException{
        
        int x = java.lang.Integer.parseInt(sim_metadata[3]);
        int y = java.lang.Integer.parseInt(sim_metadata[4]);
        matDir=direcc;
        metaDatos=md;
        
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        System.out.println(stormFile);
        System.out.println("Loading Storm ...");
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        if (!storm.isCompleted()) return;
        
        thisHillsInfo.setStormManager(storm);

        String output_path="/data/cora3/furey/cuencas_siminput_hillslopes/goodwin_ms/storms/interpolated_events/05min_ts/events_singpeakB_rain01/";
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));
        String fileSuffix=".dat";

                
        // GOODWIN CREEK info used in IDL water balance program
        // Contact point (x,y) values used below, except for basin outlet.
        // These are coords for pixel before link joins others downstream.
        FileWriter sg_IDvals = new FileWriter(output_path + "sg_arrayIDs.txt");
        sg_IDvals.write("Basin, resSimul ID for flow at outlet, Area (km^2)" + '\n'+'\n');
        for(int i=0;i<14;i++){
            int[] sg_nums= {1,2,3,4,5,6,7,8,9,10,11,12,13,14};
            // IDS from GCEW extracted prior to 16 April 2007
            int[][] xyval = {{55,114},{111,180},{204,219},{196,209},{256,227}, //sgs 1 - 5
            {240,256},{243,199},{325,249},{325,250},{319,225},{344,233},                //sgs 6-11
            {369,239},{127,187},{172,191}};                                     //sgs 12-14
            int resSimID = linksStructure.getResSimID(xyval[i][0],xyval[i][1]);
            sg_IDvals.write(sg_nums[i] + "   " + resSimID + "  " + thisNetworkGeom.upStreamArea(resSimID-1) + '\n');
        }
        sg_IDvals.close();
        
        
        java.io.File archivo=new java.io.File(output_path+demName+"_"+storm.stormName()+fileSuffix);
        System.out.println(output_path+demName+"_"+storm.stormName()+fileSuffix);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(archivo);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.DataOutputStream newfile = new java.io.DataOutputStream(bufferout);
            
        newfile.writeInt(linksStructure.contactsArray.length);
        
        //java.util.Calendar thisDate=java.util.Calendar.getInstance();
        //thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        //System.out.println(thisDate.getTime());
        //*System.out.println("Initial time " + storm.stormInitialTimeInMinutes());
        
        //SET STORM CONDITIONS
        // Do not fix time, or else precitation becomes zero via workings of *.getPrec class. (ie. no ... double storm_initialTime = 0.0d)
        //double storm_initialTime = storm.stormInitialTimeInMinutes();  // Uses current time, so it changes between simulations
        //double timestep_res, norain_duration ;
        
       // OUTPUT TO FILE - precip ts values for each hillslope
        System.out.println("Inicia escritura de Precipitaciones");   
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            newfile.writeDouble(thisHillsInfo.Area(i));
        }
        double storm_initialTime = storm.stormInitialTimeInMinutes();  // Uses current time, so it changes between simulations
        newfile.writeDouble(storm_initialTime);
        int numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());   
        newfile.writeInt(numPeriods); 
        System.out.println("Num files ="+(numPeriods+1));
        for (int k=0;k<numPeriods;k++) {
            double currTime=storm_initialTime+k*storm.stormRecordResolutionInMinutes();
            for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.writeDouble(thisHillsInfo.precipitation(i,currTime));     
            }    
        }
        System.out.println("Termina escritura de Precipitaciones");
        
        // OUTPUT TO FILE - connections structure
        System.out.println("Inicia escritura de Connection Structure");   
        for (int i=0;i<linksStructure.connectionsArray.length;i++){
            newfile.writeInt(linksStructure.connectionsArray[i].length);
            if (linksStructure.connectionsArray[i].length > 0) {
                for (int jj=0;jj<linksStructure.connectionsArray[i].length;jj++){
                    newfile.writeInt(linksStructure.connectionsArray[i][jj]);
                }
            }
        }
        System.out.println("Termina escritura de Connection Structure");
        
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
        int s_event = 1;
        int e_event=  148;
        String[] event_nums = new String[e_event-s_event+1];
        for(int i=s_event;i<e_event+1;i++) {
            if ( i<10 ) event_nums[i-s_event] = "00"+java.lang.Integer.toString( i );
            if (( i>9 )  &&  (i<100 )) event_nums[i-s_event] = "0"+java.lang.Integer.toString( i );
            if ( i>99 ) event_nums[i-s_event] = java.lang.Integer.toString( i );
        }

        
        // CREATE stormFile AND RUN SIMULATION ...
        //String evNUM,evStamp;
        for (int i=0;i<event_nums.length;i++){
            // INTERPOLATED DATA NAMES:
            //int evID = event_num[i];
            //evNUM=(""+(evID/100.+0.001)).substring(2,4);
            //evNUM=(""+(evID/1000.+0.0001)).substring(2,5);   // events as 001 instead of 01
            //evStamp="_ev"+evNUM;
            //System.out.println("Event number ="+evNUM+",   Stamp ="+evStamp);
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_interpolated"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_meanrgts"+evStamp+".metaVHC");
            //stormFile=new java.io.File(rain_path+"event_"+evNUM+"/precipitation_meanfdts"+evStamp+".metaVHC");
            stormFile=new java.io.File(rain_path+"event_"+event_nums[i]+"/precipitation_interpolated_ev"+event_nums[i]+".metaVHC");

            System.out.println(stormFile);
            new HillslopeRainToFileFurey(sim_metadata,matDirs,magnitudes,metaModif,stormFile,0f);
         
        }
        
        System.exit(0);
        
    }
    
}
