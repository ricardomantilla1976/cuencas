/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

// This function is used by the HydroNexradRes_space_time.java program
//
package hydroScalingAPI.io;
import java.io.*;
import java.io.FileOutputStream;
import java.io.DataOutputStream;
import java.text.DecimalFormat;
import java.util.TimeZone;


/**
 *
 * @author pmandapa
 */

import java.util.*;

public class HydroNexradRes_time extends Object {


    private  hydroScalingAPI.util.fileUtilities.ChronoFile[] ListFiles;
    private  float[][][]      matrixtime; // [time][row][col]
    private  float[][][]      Finalmatrix;// [time][row][col]
    private  int              columns,rows;
    private  int              spatialres;
    private  float            IniResolution,FinalResolution;

    public HydroNexradRes_time(hydroScalingAPI.util.fileUtilities.ChronoFile[] inputFile, String OutDir, int numRow, int numCol, int spatial, float IniRes, float FinalRes,float missing) throws java.io.IOException {


        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        String fileNa;
        ListFiles=inputFile;
        columns = numCol;
        rows = numRow;
        IniResolution=IniRes;
        spatialres=spatial;
        FinalResolution=FinalRes;
        //System.out.println("inputFile = "+inputFile+"columns"+columns+"rows "+rows );
        float restime= FinalResolution/IniResolution;
        int nInt= (int) java.lang.Math.round(restime);
        //System.out.println("point 1 = " + nInt + "restime"+restime);

        double initialtime=checktime(ListFiles[0].fileName.getName());
        //System.out.println(ListFiles[(ListFiles.length-1)].fileName.getName());
        double finaltime=checktime(ListFiles[(ListFiles.length-1)].fileName.getName());
         //System.out.println("initialtime "+initialtime + "finaltime" + finaltime);
        int maxt=(int) java.lang.Math.ceil(((finaltime-initialtime)/IniResolution)+1);
        matrixtime = new float[maxt][rows][columns];
        int Finalmaxt=(int) java.lang.Math.ceil(((finaltime-initialtime)/FinalResolution)+1);
        Finalmatrix = new float[Finalmaxt][rows][columns];

 //  System.out.println("point 1 = ");
        int count=0; // accumulates the number of files
        int index=0; // indicates the initial time interval
        int tcount=0;

   //System.out.println("point 2 = "+maxt+"rows"+rows+"columns"+columns);
        // Initialize matrixtime[][][]
        for (int it=0;it<maxt;it++){
            for (int i=0;i<rows;i++){
              for (int j=0;j<columns;j++) {
                  matrixtime[it][i][j]=-99;
              }
            }
        }
   //System.out.println("finish loop = ");
// Read all the files and attribute values to matrixtime[][][]
       System.out.println("Start to read the files"+"ListFiles.length = " + ListFiles.length);

        double[] timestamp = new double[ListFiles.length];
        float sumtime,sum;

        for(int il=0;il<ListFiles.length;il++){
        sum=0;
        sumtime=0;
             timestamp[il]=checktime(ListFiles[il].fileName.getName());

             System.out.println("il"+il+" File name:"+ListFiles[il].fileName.getName());
             int tt=(int)((timestamp[il]-initialtime)/IniResolution);
             System.out.println("calculation tt =" + tt +"timestamp[il] = " +timestamp[il]);
             ruta = new FileReader(ListFiles[count].fileName);

             buffer=new BufferedReader(ruta);
                    // READ THE FILES
             for (int h=0;h<6;h++) buffer.readLine();

             for (int i=0;i<rows;i++) {

             linea = buffer.readLine();
             tokens = new StringTokenizer(linea);
                for (int j=0;j<columns;j++) {
                    try{matrixtime[tt][i][j] = new Float(tokens.nextToken()).floatValue(); //accumulation in 15 minutes
                    if(matrixtime[tt][i][j]>0) {sumtime=sumtime+matrixtime[tt][i][j];
                    sum=sum+1;}

                    } catch (NumberFormatException NFE){matrixtime[tt][i][j] = -9999;}
                //if(matrixtime[tt][i][j]>=0) matrixtime[tt][i][j]=matrixtime[tt][i][j]*(60/IniResolution); //rain rate in mm/hour
                if(matrixtime[tt][i][j]>=0) matrixtime[tt][i][j]=matrixtime[tt][i][j]; //rain rate in mm/hour
                }
              }
             System.out.println("sumtime = " +sumtime);
                    count++;
                    buffer.close();
        }
        System.out.println("point 3 = ");
        int nInteraction=(int) java.lang.Math.floor((finaltime-initialtime)/FinalResolution);
        int step=(int) java.lang.Math.floor(FinalResolution/IniResolution);
        System.out.println("nInteraction t =" + nInteraction + "step =" + step);
        sum=0;
        sumtime=0;
        String SumFileName=OutDir.substring(0,OutDir.lastIndexOf("Time"))+"/summary_time.asc";
        FileWriter out = new FileWriter(SumFileName,true);
        BufferedWriter newfile = new BufferedWriter(out);
        int times=0;
        float suminfo=0;
        int summ=0;
        for (int ni=0;ni<nInteraction;ni++)
        {
        // System.out.println("ni =" + ni);
// calculate the final values
            for (int i=0;i<rows;i++) {
            for (int j=0;j<columns;j++) {
              for (int ns=0;ns<step;ns++) {
                int t=ni*step+ns;
                times=t;
                if(matrixtime[t][i][j]>=0)
                     {Finalmatrix[ni][i][j]=Finalmatrix[ni][i][j]+matrixtime[t][i][j];
                     tcount=tcount+1;
                     }
               }
             //if(tcount>0) Finalmatrix[ni][i][j]=Finalmatrix[ni][i][j]/tcount;    // average rain rate for the period
             if(tcount>0) Finalmatrix[ni][i][j]=Finalmatrix[ni][i][j]/step;    // average rain rate for the period
             else Finalmatrix[ni][i][j]=-99;
             if(Finalmatrix[ni][i][j]>=0)
             {    suminfo=suminfo+Finalmatrix[ni][i][j];
                  summ=summ+1;
             }
             tcount=0;
             }
          }
        newfile.write(ni+ " " + times +" "+ suminfo + " " + summ +" "+suminfo/summ + "\n");
        suminfo=0;
        summ=0;
        }


        newfile.close();

       System.out.println("Start to generate the outputs");
        for (int ni=0;ni<nInteraction;ni++)
        {
            double timeident=(int)java.lang.Math.floor(initialtime+(ni*FinalResolution));


            String filenameBin = Outputname(timeident,"Bin");
            String filenameAsc = Outputname(timeident,"asc");
            File fileBinoutputDir = new java.io.File(OutDir+"/Bin/"+filenameBin);
            File fileASCDir = new java.io.File(OutDir+"/asc/"+filenameAsc);
            newfilebinary(fileBinoutputDir,ni,missing);
            newfileASC(fileASCDir,ni,missing);
        }
 }

    private String Outputname(double Timeidentif, String type){

    String[] months2={"January","February","March","April","May","June","July","August","September","October","November","December"};

    DecimalFormat df = new DecimalFormat("00");
    DecimalFormat df2 = new DecimalFormat("0000");
    String monthString;
    double[] time = new double[5];
    long timmilsec = (long)Timeidentif*(1000*60);

    java.util.Calendar date=java.util.Calendar.getInstance();
    date.clear();
    java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
    date.setTimeZone(tz);  /// CHECK RICARDO
    

    date.setTimeInMillis(timmilsec);

    time[0]=date.get(Calendar.YEAR);
    time[1]=date.get(Calendar.MONTH);
    time[2]=date.get(Calendar.DATE);
    time[3]=date.get(Calendar.HOUR_OF_DAY);
    time[4]=date.get(Calendar.MINUTE);

    monthString="err";
        for (int ii=0;ii<=11;ii++)
        {
            if(time[1]==(ii))
                 {monthString=months2[ii];}
        }

    String vhcFilename;
        System.out.println("vhcFilename=MPE_IOWA_ST4."+df.format(time[3])+df.format(time[4])+"00."+df.format(time[2])+"."+monthString+"."+df2.format(time[0])+".asc");
    if (type.equals("Bin")){vhcFilename="MPE_IOWA_ST4."+df.format(time[3])+df.format(time[4])+"00."+df.format(time[2])+"."+monthString+"."+df2.format(time[0])+".vhc";}
    else {vhcFilename="MPE_IOWA_ST4."+df.format(time[3])+df.format(time[4])+"00."+df.format(time[2])+"."+monthString+"."+df2.format(time[0])+".asc";}
    return vhcFilename;
    }

   private double checktime(String fileName){
   long TimeMill;

   String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
   String[] months2={"Jan","Feb","Mar","Apr","May","June","July","Aug","Sep","Oct","Nov","Dec"};

   String[] timeStamp = new String[6];
   int[] time = new int[6];
   String baseName=fileName.substring(fileName.indexOf("."),fileName.lastIndexOf("."));
   System.out.println(" baseName "+baseName);


    timeStamp[2]=baseName.substring(8,10);  // day
    System.out.println(" DAY "+timeStamp[2]);
    time[2]=Integer.parseInt(timeStamp[2]);

    timeStamp[3]=baseName.substring(1,3); // hour
   System.out.println(" HOUR "+timeStamp[3]);
    time[3]=Integer.parseInt(timeStamp[3]);
    timeStamp[4]=baseName.substring(3,5); // min
 System.out.println(" min "+timeStamp[4]);
    time[4]=Integer.parseInt(timeStamp[4]);
    timeStamp[5]=baseName.substring(5,7); //seg
 System.out.println(" sec "+timeStamp[5]);
    time[5]=Integer.parseInt(timeStamp[5]);

    timeStamp[1]=baseName.substring(11,baseName.lastIndexOf("."));
  System.out.println(" month "+timeStamp[1]+"basename = "+baseName + "length= "+baseName.length());
    int leng=baseName.length();
    timeStamp[0]=baseName.substring(leng-4,leng); // year
  System.out.println(" year "+timeStamp[0]);
    time[0]=Integer.parseInt(timeStamp[0]);
   //timeStamp[1]=fileName.substring(29,33);  // month

   for (int ii=0;ii<11;ii++)
        {        if(timeStamp[1].compareTo(months[ii])==0)
                 {time[1]=ii;}
        }




    java.util.Calendar date=java.util.Calendar.getInstance();
    date.clear();
    java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
    date.setTimeZone(tz);  /// CHECK RICARDO
        date.set(time[0], time[1], time[2], time[3], time[4], time[5]);
        double timemin =date.getTimeInMillis()/1000./60.;
  System.out.println(fileName+"---- year "+timeStamp[0]+ " month  "+timeStamp[1]+ " DAY  "+timeStamp[2]+ " HOUR  "+timeStamp[3]+ " MIN  "+timeStamp[4]);
    return timemin;
    }

    private void newfilebinary(java.io.File BinaryFile,int ni,float missing) throws java.io.IOException{

        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;


        outputDir = new FileOutputStream(BinaryFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);

      for (int i=rows-1;i>-1;i--) for (int j=0;j<columns;j++) {
          if (Finalmatrix[ni][i][j]>=0)  newfile.writeFloat(Finalmatrix[ni][i][j]);
           else newfile.writeFloat(missing);
        }
        newfile.close();
        bufferout.close();
        outputDir.close();
    }

        private void newfileASC(java.io.File AscFile, int ni,float missing) throws java.io.IOException{


        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";

        outputDir = new java.io.FileOutputStream(AscFile);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);

         newfile.write("ncols "+columns+retorno);
        newfile.write("nrows "+rows+retorno);
        //newfile.write("xllcorner "+"-97.63333"+retorno);// basin mode
        //newfile.write("yllcorner "+"37.300000"+retorno);//basin mode
        newfile.write("xllcorner "+"-93.93337"+retorno);// Iowa river
        newfile.write("yllcorner "+"40.98333"+retorno);//Iowa river
        //newfile.write("xllcorner "+"-100.066667"+retorno);// radar mode
        //newfile.write("yllcorner "+"35.600000"+retorno);//radar mode
        int cellsize = (int) java.lang.Math.round(FinalResolution/60);
        newfile.write("cellsize "+cellsize+retorno);
        newfile.write("NODATA_value  "+"-99.0"+retorno);


           for (int i=0;i<rows;i++) {
            for (int j=0;j<columns;j++) {
                if (Finalmatrix[ni][i][j]>=0)  newfile.write(Finalmatrix[ni][i][j]+" ");
                else newfile.write(missing+" ");
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
        for(File file : files){
            result.add(file);
        }
        return result;
    }


        public static String Outfilename(String fileName, String type) {

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

        String vhcFilename="H00070802_R1504_G_"+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        System.out.println(" to "+vhcFilename);
        if (type.equals("Bin")) {vhcFilename="H00070802_R1504_G_"+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";}
        else {vhcFilename="H00070802_R1504_G_"+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".asc";}
        System.out.println(" to "+vhcFilename);

        return vhcFilename;
    }

}





