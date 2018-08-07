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
 
public class HydroNexradToCRasBongChul extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  int              columns,rows; 
    
    public HydroNexradToCRasBongChul(java.io.File inputFile, java.io.File outputFile, int numRow, int numCol,float coef) throws java.io.IOException {
        
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        System.out.println("test1");
        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);         
      
        columns = numCol;
        rows = numRow;        
        matrix = new float[rows][columns];
        
        for (int i=0;i<11;i++) buffer.readLine();
        System.out.println("test2" + " coef " + coef);
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
                    matrix[i][j]=matrix[i][j];
                } catch (NumberFormatException NFE){
                    matrix[i][j] = -99.00f;
                }
            }}
        }
        buffer.close();
        System.out.println("test3");
        fileName=inputFile.getName();
        String fileBinoutputDir=outputFile.getAbsolutePath();
                
        newfilebinary(new java.io.File(fileBinoutputDir),coef);        
    }
    
    private void newfilebinary(java.io.File BinaryFile,float coef) throws java.io.IOException{
        
        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;
        
        outputDir = new FileOutputStream(BinaryFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);
       System.out.println("cief" +coef);

        for (int i=rows-1;i>-1;i--) for (int j=0;j<columns;j++) {
            float value=0.f;
            if(matrix[i][j]>=0.f) value=coef*matrix[i][j];
            else value=matrix[i][j];
            newfile.writeFloat(value);
            if(matrix[i][j]>100 || matrix[i][j]<-99 )
            {
                System.out.println("row" + i + "column" + j + "value" + matrix[i][j] + "file" + outputDir.toString());
            }
            }
        newfile.close();
        bufferout.close();
        outputDir.close();
    }
    
    public static ArrayList<File> getFileList(File dir, String fil) throws FileNotFoundException{
        ArrayList<File> result = new ArrayList<File>();
        File[] files = dir.listFiles();
        List<File> tempfiles = Arrays.asList(files);
        for(File file : files){
            if(file.getAbsoluteFile().toString().contains(fil)) result.add(file);          
        }        
        return result;
    }
    
 public static void createMetaFile(File directory, int newsresol, int newtresol, int Finalrows, int Finalcolumns,String rain) {
        try {
            File saveFile = new File(directory.getPath() + File.separator +rain+ "metaVHC");
            System.out.print(saveFile);
      
            // String F=directory.getPath()+File.separator+"/bin/"+"prec.metaVHC";
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
            // System.out.println("file = "+ F);
            // System.out.println("in metafile function new_s_res = "+ newsresol+"new_t_res = "+newtresol+"Frows = "+Finalrows + "Fcolumns = "+Finalcolumns);
            writer.println("[Name]");
            // writer.println("Precipitation Radar Data From KICT - basin mode");
            writer.println("PRECIPITATION PROVIDED BY BONG CHULL - PRODUCT1");
            //writer.println("Precipitation Radar Data From KICT");
            //writer.println("Precipitation Radar Data From KTLX");
            //writer.println("Precipitation Radar Data From KINX");
            writer.println("[Southernmost Latitude]");
            //writer.println("37:38:00.00 N"); // KICT radar
            //writer.println("35:36:00.00 N"); // KICT radar
            //writer.println("41:38:30.00 N"); // Iowa River
            writer.println("40:59:00.00 N"); // Iowa River
            //writer.println("33:13:00.00 N"); // KTLX radar
            //writer.println("34:07:00.00 N"); // KINX radar
            writer.println("[Westernmost Longitude]");

            //writer.println("97:18:00.00 W"); // KICT basin
            //writer.println("97:18:00.00 W"); // KICT radar
            writer.println("93:56:00.00 W"); //Iowa River
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
            writer.println("-99.0");
            writer.println("[Temporal Resolution]");
            writer.println(newtresol + "-minutes");
            writer.println("[Units]");
            writer.println("mm/h");
            writer.println("[Information]");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KTLX");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KINX");
            // writer.println("Precipitation data downloaded from NEXRAD - radar mode - KICT");
            writer.println("Precipitation data provided by Pradeep");
            writer.close();
        } catch (IOException bs) {
            System.out.println("Error composing metafile: " + bs);
        }

    }
        public static String Outfilename(String fileName,String ident) {
        System.out.println("fileName - FROM");
            System.out.println(fileName);
            String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
            String[] months2={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};      
            String monthString;
            
        String[] timeStamp = new String[6];
       
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
         int index=fileName.lastIndexOf("ST4.") + 4;
         System.out.println("index    =     " + index);
              timeStamp[0]=fileName.substring(index,index+4); // year
        System.out.println(timeStamp[0]);
        timeStamp[1]=fileName.substring(index+4,index+6);  // month
        int mon=java.lang.Integer.parseInt(timeStamp[1]);
        System.out.println(timeStamp[1]);
        monthString=months[mon-1];
//        monthString=months[0];
//        for (int ii=0;ii<=11;ii++)
//        {        if(timeStamp[1].compareTo(months2[ii])==0)
//                 {monthString=months[ii];}
//        }

        timeStamp[2]=fileName.substring(index+6,index+8);  // day
        System.out.println(timeStamp[2]);
        timeStamp[3]=fileName.substring(index+8,index+10); // hour
        System.out.println(timeStamp[3]);
        timeStamp[4]="00"; // min

        System.out.println(timeStamp[4]);//      String monthString=months[(Integer.parseInt(timeStamp[1])-1)];
        timeStamp[5]="00"; // seg
        String vhcFilename=ident+timeStamp[3]+timeStamp[4]+"00"+"."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        System.out.println(" to "+vhcFilename);
       return vhcFilename;
    }
        
        public static String Outfilename2(String fileName,String ident) {
        System.out.println("fileName - FROM");
            System.out.println(fileName);
            String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
            String[] months2={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};      
            String monthString;
            
        String[] timeStamp = new String[6];
       
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
        System.out.println(fileName); 
        int index=fileName.indexOf("_G_") + 3;
         System.out.println("index    =     " + index);
         //H99999999_R6004_G_16APR2008_080000.out
        
        timeStamp[2]=fileName.substring(index,index+2);  // day
        System.out.println("day "+timeStamp[2]);
        timeStamp[1]=fileName.substring(index+2,index+5);  // month
        System.out.println("month "+timeStamp[1]);
        monthString=months[0];
        for (int ii=0;ii<=11;ii++)
        {        if(timeStamp[1].compareTo(months2[ii])==0)
                 {monthString=months[ii];}
        }
        System.out.println("month2 "+monthString);
         timeStamp[0]=fileName.substring(index+5,index+9); // year
        System.out.println(timeStamp[0]);
        
        System.out.println(timeStamp[1]);

timeStamp[3]=fileName.substring(index+10,index+12); // hour
        System.out.println(timeStamp[3]);
        timeStamp[4]=fileName.substring(index+12,index+14); // min

        System.out.println(timeStamp[4]);//      String monthString=months[(Integer.parseInt(timeStamp[1])-1)];
        timeStamp[5]="00"; // seg
        String vhcFilename=ident+timeStamp[3]+timeStamp[4]+"00"+"."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        System.out.println(" to "+vhcFilename);
       return vhcFilename;
    }
        
         public static void main(String args[]) throws Exception {


        try {
            subMainALL(args);
    
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        
        }

        System.exit(0);

    }
        
    public static void subMainALL(String[] args) throws java.io.IOException{
        
        java.io.File AsciiFile;
       // File folder = new File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010RadarLowRes/1996/");
        
        String ens="HydroNexradRef."; 
        int TRes=5;
        float bias=1.0f;
        float coef=(60.f/(float)TRes)*bias;
        //float coef=12.0f;
        
        System.out.println("Coef " + coef);
        //System.exit(1);
        
         java.io.File folder = new java.io.File("/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/Hydro_NexradProd3/5min/");
       
        System.err.println(folder.toString());
	ArrayList<File> files = HydroNexradToCRasBongChul.getFileList(folder,"");
	Iterator i = files.iterator();
        
//        String OutputDir="/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010RadarLowRes/1996VHC/";         String OutputDir="/Users/rmantill";
        java.io.File outputDirectory = new java.io.File("/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/RadarErrorSim/PaperBongChul/Prod3_5min"+bias+"/");
       
        outputDirectory.mkdirs();
        
         
        int nr=184;
        int ncol=199;
                 createMetaFile(outputDirectory, 60, TRes, nr,ncol,ens);
	while (i.hasNext()){
            File temp = (File) i.next();
            
            String FileAscIn =temp.getAbsolutePath();
            AsciiFile = new java.io.File(FileAscIn);
     
  ///////////////////////////////
  /// Define the name of the output file          
        String fileName=AsciiFile.getPath().substring(AsciiFile.getPath().lastIndexOf(File.separator)+1);
       System.out.println(temp.getAbsolutePath());           
        File BinaryOutName = new java.io.File(outputDirectory.getAbsoluteFile().toString()+File.separator+Outfilename2(fileName,ens));
        System.out.println(BinaryOutName.getName());
        ////////////////////////////////////////
            try {  
                 System.out.println(temp.getAbsolutePath());
                 new HydroNexradToCRasBongChul(AsciiFile,BinaryOutName,nr,ncol,coef);
             } catch (Exception IOE){
                 System.err.print(IOE);
                 System.exit(0);
             }
        
         
    }    
    }
 }




