/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// This program change the resolution of NEXRAD (ASCII) data
// the initial spatial resolution is 60 sec = 1 min
// the initial temporal resolution is 15 min
// the goal is evaluate the influence of rainfall data resolution in peak flow forecast
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

public class Cras_space_time extends Object {

    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  float[][]        Finalmatrix;
    private  float[][]        FinalmatrixBias;
    private  int              columns,rows;
    private  int              Finalcolumns,Finalrows;
    private  float            IniResolution,FinalResolution;

    public Cras_space_time(java.io.File inputFile, java.io.File outputFile, java.io.File outputFileASC, java.io.File outputFileB, java.io.File outputFileASCB, int numRow, int numCol, float IniRes, float FinalRes,int Frow,int Fcol) throws java.io.IOException {

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
        FinalmatrixBias = new float[Finalrows][Finalcolumns];
        // skip the headlines
        //System.out.println("numCol = "+ numCol +"numLIN = "+ numRow );
        for (int i=0;i<11;i++)
        {linea = buffer.readLine();
        }
        float summatrix =0.f;
        int llc=0;
      int sum1 = 0;
      int sumtotal1=0;
        for (int i=0;i<rows;i++) {

            linea = buffer.readLine();

            tokens = new StringTokenizer(linea);
            for (int j=0;j<columns;j++) {
                try{
                matrix[i][j] = new Float(tokens.nextToken()).floatValue();
                matrix[i][j] = 0.56f*matrix[i][j]; // remove the bias - specific for Iowa data
                sumtotal1=sumtotal1+1;
                if(matrix[i][j]>=0)
                {summatrix=summatrix+matrix[i][j];
                sum1=sum1+1;}
               // System.out.println("i "+i+"j = "+j+"tokens.nextToken()).floatValue() = "+matrix[i][j] );
                } catch (NumberFormatException NFE){
                    matrix[i][j] = -9999;

                    System.out.println("exception");
                }

            }
        }


                //System.out.println("sai do loopw = ");
        buffer.close();
        float resolutitime=FinalResolution/IniResolution;
        int nInt= (int) java.lang.Math.round(resolutitime);
        //System.out.println("resolutitime - space= "+ resolutitime +"nInt= "+ nInt);

        fileName=inputFile.getName();
        String fileBinoutputDir=outputFile.getAbsolutePath();
        String fileASCDir=outputFileASC.getAbsolutePath();
        String fileBinoutputDirB=outputFileB.getAbsolutePath();
        String fileASCDirB=outputFileASCB.getAbsolutePath();

        float value = 0;
        int sum2 = 0;
        int sumtotal2=0;
        //System.out.println("CALCULATION PROGRAM - Fcolumns = " +Finalcolumns + "Frows = "+Finalrows);
        int firstelemrow,firstelemcolumn;
        for (int i=0;i<Finalrows;i++) {
           for (int j=0;j<Finalcolumns;j++) {
               firstelemrow=nInt*i;
               firstelemcolumn=nInt*j;
               //System.out.println("firstelemrow k = "+firstelemrow + "firstelemcolumn" + firstelemcolumn);
               //System.out.println("firstelemrow="+firstelemrow+"firstelemcolumn"+firstelemcolumn);
               int lastelemrow=firstelemrow+nInt;
               int lastelemcolumn=firstelemcolumn+nInt;
               if(firstelemrow>(rows-nInt))lastelemrow=rows;
               if(firstelemcolumn>(columns-nInt))lastelemcolumn=columns;
               for (int k=firstelemrow;k<lastelemrow;k++) {
                   for (int l=firstelemcolumn;l<lastelemcolumn;l++) {

                     if (matrix[k][l]>=0)
                     {value = value + matrix[k][l];
                      sum2=sum2+1;
                     //System.out.println("finalrow= "+i+"finalcol= "+j+"inirow= "+k+"    inicol= "+l+"    value = "+value+"    sum = "+sum);
                     }
                   }
               }

               if(sum2!=0) FinalmatrixBias[i][j]=value/sum2;
               else FinalmatrixBias[i][j]=0;
               //System.out.println("----------Finalmatrix[i][j] = "+Finalmatrix[i][j]);
               //System.out.println("problem");
               sumtotal2=0;
               sum2 = 0;
               value=0;
           }
        }

        float sumFinalMatrix=0.0f;
        int sum3=0;
        int sumtotal3=0;
        for (int i=0;i<Finalrows;i++) {
           for (int j=0;j<Finalcolumns;j++) {
               if(FinalmatrixBias[i][j]>=0)
               {sumFinalMatrix=sumFinalMatrix+FinalmatrixBias[i][j];
                sum3=sum3+1;
               }
           }
        }

        /////// summmary of matrix (initial resolution) and matrix aggregated in space


        String SumFileName=outputFileASC.getAbsolutePath().substring(0,outputFileASC.getAbsolutePath().lastIndexOf("space"))+"summary_space.asc";
        String FileName=outputFileASC.getAbsolutePath().substring(outputFileASC.getAbsolutePath().lastIndexOf("space"));


        FileWriter out = new FileWriter(SumFileName,true);
        BufferedWriter newfile = new BufferedWriter(out);
        float bias=(summatrix/sum1)/(sumFinalMatrix/sum3);
        float sumFinalMatrix4=0.0f;
        int sum4=0;
        int sumtotal4=0;
        for (int i=0;i<Finalrows;i++) {
           for (int j=0;j<Finalcolumns;j++) {
               if(FinalmatrixBias[i][j]>=0)
               {Finalmatrix[i][j]=FinalmatrixBias[i][j]*bias;
                sumFinalMatrix4=sumFinalMatrix4+Finalmatrix[i][j];
                sum4=sum4+1;
               }
           }
        }


        ///////////WRITING A SUMMARY OF THE DATA IN SPACE////////////////////////////
        //newfile.write("Information "+"OriSoma_Total_matrix "+ "OriNumElem>0 "+ "NumElem" +"OriaveFinalMatrix "+"tranSoma_Total_matrix "+ "tranNumElem "+"tranaveFinalMatrix ");
        newfile.write(FileName+" "+summatrix+ " " + " " + sum1 + " " + summatrix/sum1 + " " + sumFinalMatrix + " " + " " + sum3 + " " + sumFinalMatrix/sum3 + " " + bias + " " + sumFinalMatrix4+ " " + sum4 + " " + sumFinalMatrix4/sum4 +"\n");
        newfile.close();

        newfilebinary(new java.io.File(fileBinoutputDirB),"biased");
        newfileASC(new java.io.File(fileASCDirB),"biased");
        newfilebinary(new java.io.File(fileBinoutputDir),"no_biased");
        newfileASC(new java.io.File(fileASCDir),"no_biased");
    }

    private void newfilebinary(java.io.File BinaryFile,String info) throws java.io.IOException{

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

        private void newfileASC(java.io.File AscFile,String info) throws java.io.IOException{


        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";

        outputDir = new java.io.FileOutputStream(AscFile);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);


        newfile.write("ncols "+Finalcolumns+retorno);
        newfile.write("nrows "+Finalrows+retorno);
        //newfile.write("xllcorner "+"-97.63333"+retorno);// basin mode
       // newfile.write("yllcorner "+"37.300000"+retorno);//basin mode
        //newfile.write("xllcorner "+"-100.06"+retorno);// radar mode
       // newfile.write("yllcorner "+"35.600000"+retorno);//radar mode
        newfile.write("xllcorner "+"-93.966667"+retorno);// Iowa river
        newfile.write("yllcorner "+"40.966667"+retorno);//Iowa river
        int cellsize = (int) java.lang.Math.round(FinalResolution/60);
        newfile.write("cellsize "+cellsize+retorno);
        newfile.write("NODATA_value  "+"-99.0"+retorno);

     //   System.out.println("OUTPUT PROGRAM - Fcolumns = " +Finalcolumns + "Frows = "+Finalrows);
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

    public static void createMetaFile(File directory,int newsresol,int newtresol,int Finalrows,int Finalcolumns) {
        try{
            File saveFile=new File(directory.getPath()+File.separator+"/bin/"+"H00070802_R1504_G_.metaVHC");
           // String F=directory.getPath()+File.separator+"/bin/"+"prec.metaVHC";
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
           // System.out.println("file = "+ F);
           // System.out.println("in metafile function new_s_res = "+ newsresol+"new_t_res = "+newtresol+"Frows = "+Finalrows + "Fcolumns = "+Finalcolumns);
            writer.println("[Name]");
           // writer.println("Precipitation Radar Data From KICT - basin mode");
            writer.println("KARX00070802_C11015W00G_15_01JUN2008_000000.out, KMPX00070802_C11015W00G_15_01JUN2008_000000.out, KDMX00070802_C11015W00G_15_01JUN2008_000000.out, KDVN00070802_C11015W00G_15_01JUN2008_000000.out");
            //writer.println("Precipitation Radar Data From KICT");
            //writer.println("Precipitation Radar Data From KTLX");
            //writer.println("Precipitation Radar Data From KINX");
            writer.println("[Southernmost Latitude]");
            //writer.println("37:38:00.00 N"); // KICT radar
            //writer.println("35:36:00.00 N"); // KICT radar
            writer.println("40:58:00.00 N"); // Iowa River
            //writer.println("33:13:00.00 N"); // KTLX radar
            //writer.println("34:07:00.00 N"); // KINX radar
            writer.println("[Westernmost Longitude]");

            //writer.println("97:18:00.00 W"); // KICT basin
            //writer.println("97:18:00.00 W"); // KICT radar
            writer.println("93:58:00.00 W"); //Iowa River
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
            writer.println(newtresol+"-minutes");
            writer.println("[Units]");
            writer.println("mm");
            writer.println("[Information]");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KTLX");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KINX");
           // writer.println("Precipitation data downloaded from NEXRAD - radar mode - KICT");
            writer.println("Precipitation data downloaded from NEXRAD - provided by Ricardo - aggregated by Luciana");
            writer.close();
        } catch (IOException bs) {
            System.out.println("Error composing metafile: "+bs);
        }

    }

         public static String Outfilename(String fileName, String type) {
        //System.out.println("fileName");
            System.out.println(fileName);
            String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
            String[] months2={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};
            String monthString;

        String[] timeStamp = new String[5];


        //int ini= filelen-20;
        System.out.println(fileName.substring(23,26));
        timeStamp[0]=fileName.substring(23,27); // year
        timeStamp[1]=fileName.substring(20,23);  // month
        monthString=months[0];
        for (int ii=0;ii<=11;ii++)
        {        if(timeStamp[1].compareTo(months2[ii])==0)
                 {monthString=months[ii];}
        }

        timeStamp[2]=fileName.substring(18,20);  // day
        timeStamp[3]=fileName.substring(28,30); // hour
        timeStamp[4]=fileName.substring(30,32); // min

//      String monthString=months[(Integer.parseInt(timeStamp[1])-1)];

        String vhcFilename="H00070802_R1504_G_."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        //System.out.println(" to "+vhcFilename);
        if (type.equals("Bin")){vhcFilename="H00070802_R1504_G_."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";}
        else {vhcFilename="H00070802_R1504_G_."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".asc";}
   //     System.out.println("vhcFilename: "+ vhcFilename +" ano = " + timeStamp[0]+" mes = " + timeStamp[1]+" day = " + timeStamp[2]+" hour = " + timeStamp[3]+" min = " + timeStamp[4]);
  //      System.out.println(" to "+vhcFilename);

        return vhcFilename;
    }



public static void main(String[] args) throws java.io.IOException{
  /*****DEFINE PARAMETERS*******/
        int NDecRes=1;    // number of time we would like to reduce the spatial resolution
        int orig_s_res=60; //original spatial resolution in ArcSec
        int ini_row=184;   // number of rows in the original file
        int ini_col=199;   // number of columns in the original file
        int orig_t_res=15; // Initial time resolution in minutes
        int new_t_res=15;


  /*****DEFINE DESIRED RESOLUTION IN SPACE (ARCMIN) AND TIME (MIN)*******/
        int[] space = {1,6,15};
        //int[] space = {1};
        int[] time = {60,180,1440};
        //int[] time = {180};

        float missing = 0.0f;

     for (int is : space)
     {
     NDecRes=is;
      for (int it : time)
      {
            new_t_res=it;
 /*****DEFINE THE FOLDER WITH NEXRAD DATA AND OUTPUT FOLDER*******/

        File folder_nexrad = new File("E:/CUENCAS/CedarRapids/Rainfall/Radar_Data/JUN");
        //File folder_nexrad = new File("C:/CUENCAS/Whitewater_database/scale_rainfall_study/walnut_river/may_event/NEXRAD/");
        String OutputDir="E:/CUENCAS/CedarRapids/Rainfall/resolution_study/Results_new/WithGeomBias/"+NDecRes+"/";
        new File(OutputDir).mkdirs();
        OutputDir=OutputDir+"/"+new_t_res+"min/";
        new File(OutputDir).mkdirs();
        String OutputDirs=OutputDir+"/space/";
        String OutputDirt=OutputDir+"/Time/";
        new File(OutputDirs).mkdirs();
        new File(OutputDirs+"Asc").mkdirs();
        new File(OutputDirs+"Bin").mkdirs();
        new File(OutputDirt).mkdirs();
        new File(OutputDirt+"asc").mkdirs();
        new File(OutputDirt+"Bin").mkdirs();

       String OutputDirB="E:/CUENCAS/CedarRapids/Rainfall/resolution_study/Results_new/WithoutGeomBias/"+NDecRes+"/";
        new File(OutputDirB).mkdirs();
        OutputDirB=OutputDirB+"/"+new_t_res+"min/";
        new File(OutputDirB).mkdirs();
        String OutputDirsB=OutputDirB+"/space/";
        String OutputDirtB=OutputDirB+"/Time/";
        new File(OutputDirsB).mkdirs();
        new File(OutputDirsB+"Asc").mkdirs();
        new File(OutputDirsB+"Bin").mkdirs();
        new File(OutputDirtB).mkdirs();
        new File(OutputDirtB+"asc").mkdirs();
        new File(OutputDirtB+"Bin").mkdirs();

 /*****CHANGE RESOLUTION IN SPACE*******/

        int Fcolumns = (int) java.lang.Math.ceil((double)ini_col/(double)NDecRes);
        int Frows =  (int) java.lang.Math.ceil((double)ini_row/(double)NDecRes);
        //System.out.println("ini_row" + ini_row + "  NDecRes" + NDecRes + "  Frows = "+Frows);
        int new_s_res=NDecRes*orig_s_res;
        java.io.File AsciiFile;


      try{
        String filesummary=OutputDir+"/summary_space.asc";
        //System.out.println("filesummary = "+filesummary);
        FileWriter out = new FileWriter(filesummary);
        BufferedWriter newfile = new BufferedWriter(out);

        ///////////WRITING A SUMMARY OF THE DATA IN SPACE////////////////////////////
        //newfile.write("Information "+"OriSoma_Total_matrix "+ "OriNumElem "+"OriaveFinalMatrix "+"tranSoma_Total_matrix "+ "tranNumElem "+"tranaveFinalMatrix ");
        newfile.write("FileName"+" "+"summatrix"+ " " + " " + "sum" + " " + "summatrix/sum" + " " + "sumFinalMatrix" + " " + " " + "sum3" + " " + "sumFinalMatrix/sum3" +"\n");
        newfile.close();

  	ArrayList<File> files = Cras_space_time.getFileList(folder_nexrad);
  	Iterator i = files.iterator();

        createMetaFile(new java.io.File(OutputDirs),new_s_res,orig_t_res,Frows,Fcolumns);
        createMetaFile(new java.io.File(OutputDirsB),new_s_res,orig_t_res,Frows,Fcolumns);

  	  while (i.hasNext()){
            File temp = (File) i.next();
            String FileAscIn =temp.getAbsolutePath();

            AsciiFile = new java.io.File(FileAscIn);
            //System.out.println("AsciiFile = "+AsciiFile);
            String fileName=AsciiFile.getPath().substring(AsciiFile.getPath().lastIndexOf(File.separator)+1);
             System.out.println("break point" + OutputDirs+"/bin/"+Outfilename(fileName,"Bin"));
            File BinaryOutName = new java.io.File(OutputDirs+"/bin/"+Outfilename(fileName,"Bin"));
            File ASCOutName = new java.io.File(OutputDirs+"/asc/"+Outfilename(fileName,"Asc"));
            File BinaryOutNameB = new java.io.File(OutputDirsB+"/bin/"+Outfilename(fileName,"Bin"));
            File ASCOutNameB = new java.io.File(OutputDirsB+"/asc/"+Outfilename(fileName,"Asc"));
              try
              {//System.err.println("ENTERING SPACE TIME LOOP:");
                // System.out.println(FileAscIn + "space " + NDecRes);
                  new Cras_space_time(AsciiFile,BinaryOutName,ASCOutName,BinaryOutNameB,ASCOutNameB,ini_row,ini_col,orig_s_res,new_s_res,Frows,Fcolumns);
              } catch (Exception IOE){
                 System.err.print(IOE);
                 System.exit(0);
              }
          }
        } catch (IOException e){
            System.err.println("problem creating file list:");
            e.printStackTrace();
        }


 /*****CHANGE RESOLUTION IN TIME - WITH BIAS*******/

        File folder_bin = new File(OutputDirs+"/asc/"+"H00070802_R1504_G_.metaVHC");
        System.out.println("folder_bin = "+folder_bin);
 	java.io.File directorio=folder_bin.getParentFile();
        System.out.println("directorio = "+directorio);
        String baseName=folder_bin.getName().substring(0,folder_bin.getName().lastIndexOf(".")-1);
       // System.out.println("OutputDirs = "+directorio.getParent()+"     \n baseName"+baseName);
        hydroScalingAPI.util.fileUtilities.NameDotFilter myFiltro=new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseName,"asc");
        java.io.File[] lasQueSi=directorio.listFiles(myFiltro);
        //System.out.println("File list="+lasQueSi[1].getName());
        //System.out.println("File list="+lasQueSi[2].getName());
        hydroScalingAPI.util.fileUtilities.ChronoFile[] arCron=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSi.length];
        //System.out.println("length = "+lasQueSi.length);
        arCron=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSi.length]; // create an array with files (size equal to the number of files)
        for (int i=0;i<lasQueSi.length;i++)
        {//System.out.println("i = "+i+" File list="+lasQueSi[i].getName());
            arCron[i]=new hydroScalingAPI.util.fileUtilities.ChronoFile(lasQueSi[i],baseName);
           //System.out.println("i = "+i+" File list="+lasQueSi[i].getName());
        } // atribut the files to the array arcron

        // for (int i=0;i<lasQueSi.length;i++) {System.out.println("File list="+arCron[i].fileName.getName());}


        java.util.Arrays.sort(arCron); // sort the files
        createMetaFile(new java.io.File(OutputDirt),new_s_res,new_t_res,Frows,Fcolumns);
        //System.out.println("before time = "+OutputDirt);
             try {
          // System.out.println("OutputDirt = "+OutputDirt+OutputDirt+Frows+Fcolumns+new_s_res+orig_t_res+new_t_res+missing);

           String filesummary=OutputDir+"/summary_time.asc";
           //System.out.println("filesummary = "+filesummary);
           FileWriter out = new FileWriter(filesummary);
           BufferedWriter newfile = new BufferedWriter(out);

           ///////////WRITING A SUMMARY OF THE DATA IN SPACE////////////////////////////
           //newfile.write("Information "+"OriSoma_Total_matrix "+ "OriNumElem "+"OriaveFinalMatrix "+"tranSoma_Total_matrix "+ "tranNumElem "+"tranaveFinalMatrix ");
            //newfile.write("FileName"+" "+"summatrix"+ " " + " " + "sum" + " " + "summatrix/sum" + " " + "sumFinalMatrix" + " " + " " + "sum3" + " " + "sumFinalMatrix/sum3" +"\n");
           newfile.close();

             new HydroNexradRes_time(arCron,OutputDirt,Frows,Fcolumns,new_s_res,orig_t_res,new_t_res,missing);

             } catch (Exception IOE){
                 System.err.print(IOE);
                 System.exit(0);
             }
 System.out.println("after time = "+OutputDirs);

  /*****CHANGE RESOLUTION IN TIME*******/
        //System.out.println("OutputDirs = "+OutputDirs);
        File folder_binB = new File(OutputDirsB+"/asc/"+"H00070802_R1504_G_.metaVHC");

 	java.io.File directorioB=folder_bin.getParentFile();

        String baseNameB=folder_binB.getName().substring(0,folder_binB.getName().lastIndexOf(".")-1);
       // System.out.println("OutputDirs = "+directorio.getParent()+"     \n baseName"+baseName);
        hydroScalingAPI.util.fileUtilities.NameDotFilter myFiltroB=new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseNameB,"asc");
        java.io.File[] lasQueSiB=directorioB.listFiles(myFiltroB);
        //System.out.println("File list="+lasQueSi[1].getName());
        //System.out.println("File list="+lasQueSi[2].getName());
        hydroScalingAPI.util.fileUtilities.ChronoFile[] arCronB=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSiB.length];
        //System.out.println("length = "+lasQueSi.length);
        arCronB=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSiB.length]; // create an array with files (size equal to the number of files)
        for (int i=0;i<lasQueSiB.length;i++)
        {//System.out.println("i = "+i+" File list="+lasQueSi[i].getName());
            arCronB[i]=new hydroScalingAPI.util.fileUtilities.ChronoFile(lasQueSiB[i],baseNameB);
           //System.out.println("i = "+i+" File list="+lasQueSi[i].getName());
        } // atribut the files to the array arcron

        // for (int i=0;i<lasQueSi.length;i++) {System.out.println("File list="+arCron[i].fileName.getName());}


        java.util.Arrays.sort(arCronB); // sort the files
        createMetaFile(new java.io.File(OutputDirtB),new_s_res,new_t_res,Frows,Fcolumns);
        //System.out.println("before time = "+OutputDirt);
             try {
          // System.out.println("OutputDirt = "+OutputDirt+OutputDirt+Frows+Fcolumns+new_s_res+orig_t_res+new_t_res+missing);

           String filesummaryB=OutputDirB+"/summary_timeB.asc";
           //System.out.println("filesummary = "+filesummary);
           FileWriter out = new FileWriter(filesummaryB);
           BufferedWriter newfile = new BufferedWriter(out);

           ///////////WRITING A SUMMARY OF THE DATA IN SPACE////////////////////////////
           //newfile.write("Information "+"OriSoma_Total_matrix "+ "OriNumElem "+"OriaveFinalMatrix "+"tranSoma_Total_matrix "+ "tranNumElem "+"tranaveFinalMatrix ");
            //newfile.write("FileName"+" "+"summatrix"+ " " + " " + "sum" + " " + "summatrix/sum" + " " + "sumFinalMatrix" + " " + " " + "sum3" + " " + "sumFinalMatrix/sum3" +"\n");
           newfile.close();

             new HydroNexradRes_time(arCronB,OutputDirtB,Frows,Fcolumns,new_s_res,orig_t_res,new_t_res,missing);

             } catch (Exception IOE){
                 System.err.print(IOE);
                 System.exit(0);
             }
 System.out.println("after time = "+OutputDirsB);

        }
     }
}

}




