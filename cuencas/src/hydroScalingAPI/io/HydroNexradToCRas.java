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
 
public class HydroNexradToCRas extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  int              columns,rows; 
    
    public HydroNexradToCRas(java.io.File inputFile, java.io.File outputFile, int numRow, int numCol) throws java.io.IOException {
        
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
        System.out.println("test2");
        for (int i=0;i<rows;i++) {
            
            linea = buffer.readLine();

            if(linea==null) {
                System.out.println("linear is null");
                for (int j=0;j<columns;j++) {
                    matrix[i][j] = -99.00f;
                }}

            else {
            tokens = new StringTokenizer(linea);
            for (int j=0;j<columns;j++) {
                try{
                    matrix[i][j] = new Float(tokens.nextToken()).floatValue();
                    matrix[i][j]=matrix[i][j]*4;
                } catch (NumberFormatException NFE){
                    matrix[i][j] = -99.00f;
                }
            }}
        }
        buffer.close();
        System.out.println("test3");
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
        
        for (int i=rows-1;i>-1;i--) for (int j=0;j<columns;j++) {
            newfile.writeFloat(matrix[i][j]);
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
    
 public static void createMetaFile(File directory, int newsresol, int newtresol, int Finalrows, int Finalcolumns) {
        try {
            File saveFile = new File(directory.getPath() + File.separator + "NEXRAD_BC.metaVHC");
            System.out.print(saveFile);
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
            writer.println("41:38:30.00 N"); // Iowa River
            //writer.println("33:13:00.00 N"); // KTLX radar
            //writer.println("34:07:00.00 N"); // KINX radar
            writer.println("[Westernmost Longitude]");

            //writer.println("97:18:00.00 W"); // KICT basin
            //writer.println("97:18:00.00 W"); // KICT radar
            writer.println("92:02:30.00 W"); //Iowa River
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
       
        timeStamp[0]=fileName.substring(23,27); // year
        System.out.println(timeStamp[0]);
        timeStamp[1]=fileName.substring(20,23);  // month
        System.out.println(timeStamp[1]);
        monthString=months[0];
        for (int ii=0;ii<=11;ii++)
        {        if(timeStamp[1].compareTo(months2[ii])==0)                 
                 {monthString=months[ii];}
        }
        
        timeStamp[2]=fileName.substring(18,20);  // day
        System.out.println(timeStamp[2]);
        timeStamp[3]=fileName.substring(28,30); // hour
        System.out.println(timeStamp[3]);
        timeStamp[4]=fileName.substring(30,32); // min

        System.out.println(timeStamp[4]);//      String monthString=months[(Integer.parseInt(timeStamp[1])-1)];
        
        String vhcFilename="NEXRAD_BC."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        System.out.println(" to "+vhcFilename);
       return vhcFilename;
    }
        
    public static void main(String[] args) throws java.io.IOException{
        
        java.io.File AsciiFile;
        File folder = new File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_Advection");
        
 	try{
	ArrayList<File> files = HydroNexradToCRas.getFileList(folder);
	Iterator i = files.iterator();
        
        String OutputDir="/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Hydrology/event_2009/March2009_AdvectionVHC";
        new File(OutputDir).mkdirs();
        int nr=37;
        int ncol=133;
                 createMetaFile(new java.io.File(OutputDir), 15, 15, 37,133);
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
                 new HydroNexradToCRas(AsciiFile,BinaryOutName,37,133);
             } catch (Exception IOE){
                 System.err.print(IOE);
                 System.exit(0);
             }
        }
        } catch (IOException e){
            System.err.println("problem creating file list:");
            e.printStackTrace();
        }
    }    
 }




