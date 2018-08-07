/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// This program change the resolution of NEXRAD (ASCII) data
// the initial resolution is 60 sec = 1 min
// the goal is avaliate the influence of rainfall data resolution in peak flow forecast
// 
package hydroScalingAPI.io;
import java.io.*;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
/**
 *
 * @author pmandapa
 */

import java.util.*;
 
public class HydroNexradRes_space extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  float[][]        Finalmatrix;
    private  int              columns,rows; 
    private  int              Finalcolumns,Finalrows; 
    private  float            IniResolution,FinalResolution; 
    
    public HydroNexradRes_space(java.io.File inputFile, java.io.File outputFile, java.io.File outputFileASC, int numRow, int numCol, float IniRes, float FinalRes,int Frow,int Fcol) throws java.io.IOException {
        
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        
        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);         
        
        columns = numCol;
        rows = numRow;        
        IniResolution=IniRes;
        FinalResolution=FinalRes;
        matrix = new float[rows][columns];
        Finalcolumns = Fcol;
        Finalrows =  Frow;
        Finalmatrix = new float[Finalrows][Finalcolumns];
        // skip the headlines
        for (int i=0;i<11;i++) buffer.readLine();
        
        for (int i=0;i<rows;i++) {
            
            linea = buffer.readLine();
        
            tokens = new StringTokenizer(linea);
            for (int j=0;j<columns;j++) {
                try{
                    matrix[i][j] = new Float(tokens.nextToken()).floatValue();
                } catch (NumberFormatException NFE){
                    matrix[i][j] = -9999;
                }
            }
        }
        buffer.close();
        float resolutitime=FinalResolution/IniResolution;
        int nInt= (int) java.lang.Math.round(resolutitime);
        
        
        fileName=inputFile.getName();
        String fileBinoutputDir=outputFile.getAbsolutePath();
        String fileASCDir=outputFileASC.getAbsolutePath();
                
        
        float value = 0;
        int sum = 0;
        
        int firstelemrow,firstelemcolumn;
        for (int i=0;i<Finalrows;i++) {
           for (int j=0;j<Finalcolumns;j++) { 
               firstelemrow=nInt*i;
               firstelemcolumn=nInt*j;
              // System.out.println("firstelemrow="+firstelemrow+"firstelemcolumn"+firstelemcolumn); 
               if(firstelemrow>(rows-nInt))firstelemrow=rows-nInt;
               if(firstelemcolumn>(columns-nInt))firstelemcolumn=columns-nInt;
               for (int k=firstelemrow;k<firstelemrow+nInt;k++) {
                   for (int l=firstelemcolumn;l<firstelemcolumn+nInt;l++) {
        //System.out.println("k = "+k+" and l = "+l+"matrix[k][l]= "+matrix[k][l]); 
                     if (matrix[k][l]>=0) 
                     {value = value + matrix[k][l];
                     sum=sum+1;
                     }
                   }
               }
          //     System.out.println("value = "+value+"sum = "+sum); 
               
               if(sum!=0) Finalmatrix[i][j]=value/sum;
               else Finalmatrix[i][j]=-99;
               //System.out.println("problem"); 
               sum = 0;
               value=0;
           }
        }
        //System.out.println("PAssei loop of Final matrix"); 
        //System.out.println("Create the matrix data"); 
        newfilebinary(new java.io.File(fileBinoutputDir));   
        newfileASC(new java.io.File(fileASCDir));   
    }
    
    private void newfilebinary(java.io.File BinaryFile) throws java.io.IOException{
        
        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;
        
        outputDir = new FileOutputStream(BinaryFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);
 
      for (int i=Finalrows-1;i>-1;i--) for (int j=0;j<Finalcolumns;j++) {
            newfile.writeFloat(Finalmatrix[i][j]);
        }
        newfile.close();
        bufferout.close();
        outputDir.close();
    }
    
        private void newfileASC(java.io.File AscFile) throws java.io.IOException{
        
        
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";
        
        outputDir = new java.io.FileOutputStream(AscFile);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);

           
        newfile.write("ncols "+Finalcolumns+retorno);
        newfile.write("nrows "+Finalrows+retorno);
        newfile.write("xllcorner "+"-100.066667"+retorno);
        newfile.write("yllcorner "+"35.600000"+retorno);
        int cellsize = (int) FinalResolution/60;
        newfile.write("cellsize "+cellsize+retorno);
        newfile.write("NODATA_value  "+"-99.0"+retorno);
        

           for (int i=0;i<Finalrows;i++) {
            for (int j=0;j<Finalcolumns;j++) {
                
                    newfile.write(Finalmatrix[i][j]+" ");
                }           
            newfile.write(retorno);
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
    
    public static void createMetaFile(File directory,int newresol,int Finalrows,int Finalcolumns) {
        try{
            File saveFile=new File(directory.getPath()+File.separator+"/bin/"+"prec.metaVHC");
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
            writer.println("[Name]");
            writer.println("Precipitation Radar Data From KICT");
            //writer.println("Precipitation Radar Data From KTLX");
            //writer.println("Precipitation Radar Data From KINX");
            writer.println("[Southernmost Latitude]");
            writer.println("35:36:00.00 N"); // KICT radar
            //writer.println("33:13:00.00 N"); // KTLX radar
            //writer.println("34:07:00.00 N"); // KINX radar
            writer.println("[Westernmost Longitude]");
            writer.println("100:04:00.00 W"); // KICT radar
            //writer.println("99:19:00.00 W"); // KTLX radar
            //writer.println("98:08:00.00 W");// KINX radar
            writer.println("[Longitudinal Resolution (ArcSec)]");
            writer.println(newresol);
            writer.println("[Latitudinal Resolution (ArcSec)]");
            writer.println(newresol);
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
            writer.println("60-minutes");
            writer.println("[Units]");
            writer.println("mm");
            writer.println("[Information]");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KTLX");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KINX");
            writer.println("Precipitation data downloaded from NEXRAD - radar mode - KICT");
            writer.close();
        } catch (IOException bs) {
            System.out.println("Error composing metafile: "+bs);
        }
        
    }
  
        public static String BiOutfilename(String fileName) {
        System.out.println("fileName");
            System.out.println(fileName);
            String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
            String[] months2={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};      
            String monthString;
            
        String[] timeStamp = new String[5];
        timeStamp[0]=fileName.substring(32,36); // year
        timeStamp[1]=fileName.substring(29,32);  // month
        monthString=months[0];
        for (int ii=0;ii<=11;ii++)
        {        if(timeStamp[1].compareTo(months2[ii])==0)                 
                 {monthString=months[ii];}
        }
        
        timeStamp[2]=fileName.substring(27,29);  // day
        timeStamp[3]=fileName.substring(37,39); // hour 
        timeStamp[4]=fileName.substring(39,41); // min
//      String monthString=months[(Integer.parseInt(timeStamp[1])-1)];
        
        String vhcFilename="prec."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        System.out.println(" to "+vhcFilename);
       return vhcFilename;
    }

    public static void main(String[] args) throws java.io.IOException{
        
        java.io.File AsciiFile;
        File folder = new File("C:/CUENCAS/Whitewater_database/Rasters/Hydrology/storms/observed_events/Eventmay/nexrad");
        String OutputDir="C:/CUENCAS/Whitewater_database/Rasters/Hydrology/storms/observed_events/Eventmay/dec15";       
 	// SPECIFY INPUT??
        int NDecRes=15;
        int orig_res=60;
        int ini_row=249;
        int ini_col=315;
        int Fcolumns = (int) java.lang.Math.floor(ini_col/NDecRes);
        int Frows =  (int) java.lang.Math.floor(ini_row/NDecRes);
        System.out.println("Fcolumns = " + Fcolumns + "Frows = "+Frows);
        int newres=NDecRes*orig_res;
        
        try{
	ArrayList<File> files = HydroNexradRes_space.getFileList(folder);
	Iterator i = files.iterator();
      
        createMetaFile(new java.io.File(OutputDir),newres,Frows,Fcolumns);
        
	while (i.hasNext()){
            File temp = (File) i.next();
            
            String FileAscIn =temp.getAbsolutePath();
            AsciiFile = new java.io.File(FileAscIn);
            
  ///////////////////////////////
  /// Define the name of the output file          
       String fileName=AsciiFile.getPath().substring(AsciiFile.getPath().lastIndexOf(File.separator)+1);
       System.out.println(temp.getAbsolutePath());           
       File BinaryOutName = new java.io.File(OutputDir+"/bin/"+BiOutfilename(fileName));
       File ASCOutName = new java.io.File(OutputDir+"/asc/"+fileName);
  ////////////////////////////////////////
            try {  
                 // Number of columns, rows, original and final resolution in seconds
                 new HydroNexradRes_space(AsciiFile,BinaryOutName,ASCOutName,ini_row,ini_col,orig_res,newres,Frows,Fcolumns);
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




