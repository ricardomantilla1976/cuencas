/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.io;
import java.io.*;
/**
 *
 * @author pmandapa
 */

import java.io.*;
import java.util.*;
 
public class HydroNexradToCRasCropped extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  int              columns,rows;
    private  int              finicol,finirow;

    private  int              fendcol,fendrow;


    public HydroNexradToCRasCropped(java.io.File inputFile, java.io.File outputFile, int numRow, int numCol, int iniRow, int endRow, int iniCol,int endCol) throws java.io.IOException {
        
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        //System.out.println("test1");
        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);         
        
        columns = numCol;
        rows = numRow;
             
        finirow = iniRow;
        finicol = iniCol;
        int fnrow=endRow-iniRow;
        fendrow=endRow;
        fendcol=endCol;
        matrix = new float[rows][columns];
        
        
        for (int i=0;i<6;i++) buffer.readLine();
        //System.out.println("test2");


        for (int i=0;i<rows;i++) {
            
            linea = buffer.readLine();

            if(linea==null) {
                
                   for (int j=0;j<columns;j++) {
                    matrix[i][j] = -99.00f;
                    System.out.println(matrix[i][j]);
                }}

            else {
            tokens = new StringTokenizer(linea);
            for (int j=0;j<columns;j++) {
                try{
                    matrix[i][j] = new Float(tokens.nextToken()).floatValue();
                    
                } catch (NumberFormatException NFE){
                    matrix[i][j] = -99.00f;
                }
            }}
        }
        buffer.close();
        //System.out.println("test3");
        fileName=inputFile.getName();
        String fileBinoutputDir=outputFile.getAbsolutePath();
                
        newfilebinary(new java.io.File(fileBinoutputDir));        
    }
    
    private void newfilebinary(java.io.File BinaryFile) throws java.io.IOException{
        
        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;
        
        outputDir = new FileOutputStream(BinaryFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);

        for (int i=fendrow-2;i>finirow-2;i--) for (int j=finicol;j<fendcol;j++) {
            newfile.writeFloat(matrix[i][j]);
            if(matrix[i][j]>200) System.out.println("row"+i+"column"+j+ "value" + matrix[i][j] + "file" +outputDir.toString());
        }
        newfile.close();
        bufferout.close();
        outputDir.close();

        
    }
    
    public static ArrayList<File> getFileList(File dir) throws FileNotFoundException{
        ArrayList<File> result = new ArrayList<File>();
        File[] files = dir.listFiles();
        List<File> tempfiles = Arrays.asList(files);
        for(File file : files){
            result.add(file);
        }        
        return result;
    }
    
 public static void createMetaFile(File directory, int newsresol, int newtresol, int Finalrows, int Finalcolumns,double lat,double longitude) {
        try {
            File saveFile = new File(directory.getPath() + File.separator + "NEXRAD_BC.metaVHC");
            System.out.print(saveFile+"\n");
            // String F=directory.getPath()+File.separator+"/bin/"+"prec.metaVHC";
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
            // System.out.println("file = "+ F);
            // System.out.println("in metafile function new_s_res = "+ newsresol+"new_t_res = "+newtresol+"Frows = "+Finalrows + "Fcolumns = "+Finalcolumns);
            writer.println("[Name]");
            // writer.println("Precipitation Radar Data From KICT - basin mode");
            writer.println("PRECIPITATION PROVIDED BY BONG CHUL _ RADAR PAPER");
            //writer.println("Precipitation Radar Data From KICT");
            //writer.println("Precipitation Radar Data From KTLX");
            //writer.println("Precipitation Radar Data From KINX");
            writer.println("[Southernmost Latitude]");
            //writer.println("37:38:00.00 N"); // KICT radar
            //writer.println("35:36:00.00 N"); // KICT radar
            //writer.println("41:38:30.00 N"); // Iowa River

            int latdegree=(int)Math.floor(lat);

            int latmin=(int) Math.round((lat-(double)latdegree)*60);

            writer.println(latdegree + ":"+latmin+ ":00.00 N"); // Iowa River
            //writer.println("33:13:00.00 N"); // KTLX radar
            //writer.println("34:07:00.00 N"); // KINX radar
            writer.println("[Westernmost Longitude]");
         int longdegree=(int)Math.floor(Math.abs(longitude));
            int longmin=(int) Math.round((Math.abs(longitude)-(double)longdegree)*60);
 System.out.println(latdegree + ":"+latmin+ ":00.00 N");
            //writer.println("97:18:00.00 W"); // KICT basin
            //writer.println("97:18:00.00 W"); // KICT radar
            writer.println(longdegree + ":"+longmin+ ":00.00 W"); //Iowa River
   System.out.println(longdegree + ":"+longmin+ ":00.00 W");
            //writer.println("98:08:00.00 W");// KINX radar
            // writer.println("100:04:00.00 W"); // KICT radar
            //writer.println("99:19:00.00 W"); // KTLX radar
            //writer.println("98:08:00.00 W");// KINX radar
            writer.println("[Longitudinal Resolution (ArcSec)]");
            writer.println(newsresol);
            writer.println("[Latitudinal Resolution (ArcSec)]");
            writer.println(newsresol);
            writer.println("[# Columns]");
            //writer.println("305");// KTLX radar
            writer.println(Finalcolumns);// KINX radar
            writer.println("[# Rows]");
            //writer.println("250");// KTLX radar
            writer.println(Finalrows);// KINX radar
            writer.println("[Format]");
            writer.println("Float");
            writer.println("[Missing]");
            writer.println("-99.00");
            writer.println("[Temporal Resolution]");
            writer.println(newtresol + "-minutes");
            writer.println("[Units]");
            writer.println("mm/h");
            writer.println("[Information]");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KTLX");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KINX");
            // writer.println("Precipitation data downloaded from NEXRAD - radar mode - KICT");
            writer.println("Precipitation data provided by BongChul-Nov2010");
            writer.close();
        } catch (IOException bs) {
            System.out.println("Error composing metafile: " + bs);
        }

    }
        public static String Outfilename(String fileName) {
        System.out.println("fileName");
            System.out.println(fileName);
            String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
            String[] months2={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};      
            String monthString;
            
        String[] timeStamp = new String[5];
       
//        timeStamp[0]=fileName.substring(23,27); // year
//        System.out.println(timeStamp[0]);
//        timeStamp[1]=fileName.substring(20,23);  // month
//        System.out.println(timeStamp[1]);
//        monthString=months[0];
//        for (int ii=0;ii<=11;ii++)
//        {        if(timeStamp[1].compareTo(months2[ii])==0)
//                 {monthString=months[ii];}
//        }
//
//        timeStamp[2]=fileName.substring(18,20);  // day
//        System.out.println(timeStamp[2]);
//        timeStamp[3]=fileName.substring(28,30); // hour
//        System.out.println(timeStamp[3]);
//        timeStamp[4]=fileName.substring(30,32); // min

              timeStamp[0]=fileName.substring(13,17); // year
        System.out.println("year"+timeStamp[0]);
        timeStamp[1]=fileName.substring(17,19);  // month
        int mon=java.lang.Integer.parseInt(timeStamp[1]);
        //System.out.println(timeStamp[1]);
        monthString=months[mon-1];
//        monthString=months[0];
//        for (int ii=0;ii<=11;ii++)
//        {        if(timeStamp[1].compareTo(months2[ii])==0)
//                 {monthString=months[ii];}
//        }
//
        timeStamp[2]=fileName.substring(19,21);  // day
       System.out.println("day"+timeStamp[2]);
        timeStamp[3]=fileName.substring(21,23); // hour
        System.out.println("hour"+timeStamp[3]);
        timeStamp[4]="00"; // min

        //System.out.println(timeStamp[4]);//      String monthString=months[(Integer.parseInt(timeStamp[1])-1)];
        
        String vhcFilename="NEXRAD_BC."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        //System.out.println(" to "+vhcFilename);
       return vhcFilename;
    }
        
    public static void main(String[] args) throws java.io.IOException{

        for (int iy=2008;iy<=2008;iy++){
        java.io.File AsciiFile;
       // File folder = new File("/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010RadarLowRes/1996/");
        File folder = new File("/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/ascii"+ iy+ "hourly/");
System.out.println("Folder"+folder.getAbsolutePath());
 	try{
	ArrayList<File> files = HydroNexradToCRasCropped.getFileList(folder);
	Iterator i = files.iterator();
        
//        String OutputDir="/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010RadarLowRes/1996VHC/";         String OutputDir="/Users/rmantill";
        String OutputDir=("/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010/Radar_StageIV/"+ iy+ "VHC_ALLCrop2/");

        new File(OutputDir).mkdirs();
        int o_nr=264;
        int o_ncol=435;
        double o_latS=40.133333;
        double o_longW=-97.183333;
        double res=60./3600.;
        double o_latN=o_latS+(double)o_nr*res;
        double o_longE=o_longW+(double)o_ncol*res;

        double f_latS=40.15;
        double f_latN=44.;
        double f_longE=-90.;
        double f_longW=-94.;

        int inirow=(int)Math.abs(Math.floor((o_latN-f_latN)/res));
        int finalrow=(int)Math.abs(Math.floor((o_latN-f_latS)/res));

        int inicol=(int)Math.abs(Math.floor((o_longW-f_longW)/res));
        int finalcol=(int)Math.abs(Math.floor((o_longW-f_longE)/res));

        int f_nr=finalrow-inirow;
        int f_ncol=finalcol-inicol;
        double ff_latS=o_latS+res*(double)(o_nr-finalrow);
        double ff_longW=o_longW+res*(double)(inicol);

        System.out.println("Outdir"+OutputDir);

 System.out.println("f_nr=" + f_nr +"      f_ncol"+f_ncol);


        System.out.println("o_latN" + o_latN +"o_longE"+o_longE + "res" + res);
           System.out.println("inirow" + inirow +"finalrow"+finalrow);
    System.out.println("inirow" + inicol +"finalrow"+finalcol);
    System.out.println("ff_latS" + ff_latS +"   ff_longW  "+ff_longW);

       // System.exit(0);
        
                 createMetaFile(new java.io.File(OutputDir), 60, 60, f_nr,f_ncol,ff_latS,ff_longW);
        
        
                 while (i.hasNext()){

            File temp = (File) i.next();
            
            String FileAscIn =temp.getAbsolutePath();
            AsciiFile = new java.io.File(FileAscIn);
            
  ///////////////////////////////
  /// Define the name of the output file          
        String fileName=AsciiFile.getPath().substring(AsciiFile.getPath().lastIndexOf(File.separator)+1);
       System.out.println(temp.getAbsolutePath());           
        File BinaryOutName = new java.io.File(OutputDir+File.separator+Outfilename(fileName));
        System.out.println(BinaryOutName.getName());
        ////////////////////////////////////////
            try {  
                 System.out.println(temp.getAbsolutePath());
if(BinaryOutName.getAbsolutePath().indexOf("Jun")>0){
                 new HydroNexradToCRasCropped(AsciiFile,BinaryOutName,o_nr,o_ncol,inirow,finalrow,inicol,finalcol);
            }
            } catch (Exception IOE) {
                 System.err.print(IOE);
                 System.exit(0);
             }
        }
        } catch (IOException e){
            System.err.println("problem creating file list:");
            e.printStackTrace();
        }
    }    }
 }




