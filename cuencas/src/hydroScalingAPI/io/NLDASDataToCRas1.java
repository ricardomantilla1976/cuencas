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

public class NLDASDataToCRas1 extends Object {

    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrixORI;
    private  float[][]        matrixNEW;
    private  int              columns,rows;

    public NLDASDataToCRas1(String variable,java.io.File inputFile, java.io.File outputDir, double Nlin,double Slin,double Ecol,double Wcol,int NL,int NC) throws java.io.IOException {

        // run command to generate a temporary asc file with the data
        // read temp asc
        // select data that we want Ncol to Scol and Elin to Wlin
        // create matrix and VHC file
        // generate metafile 
        
     
        
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;

        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);
         
        
        matrixORI = new float[NL][NC];
        linea = buffer.readLine();
        System.out.println("first " +linea);
       int ncount=0;
       float sum=0;
        for(int il=0;il<NL;il++){ 
         for(int ic=0;ic<NC;ic++){
           ncount=ncount+1;
            linea = buffer.readLine();
            // tokens = new StringTokenizer(linea);
            //System.out.println(linea);
                try{
                    if(!linea.contains("9.999e+20"))
                    {   
                    matrixORI[il][ic] = new Float(linea).floatValue();
                     //System.out.println("il   " + il + " ic  "+ic + "value  " + matrixORI[il][ic]);
                    //sum=sum+matrixORI[il][ic];
                    
                    }else {matrixORI[il][ic] = -9.0f;}
                    
                } catch (NumberFormatException NFE){
                    matrixORI[il][ic] = -9.0f;
                }
                
                
            }
         
     
        }
        System.out.println("ncount  " +ncount);
        buffer.close();
        
       fileName=Outfilename(inputFile.toString(),variable);
       
       System.out.println("fileName " +fileName);
        String fileBinoutputDir=outputDir.getPath()+"/"+fileName;
        
        newfilebinary(new java.io.File(fileBinoutputDir),Nlin,Slin,Ecol,Wcol);
    }

    private void newfilebinary(java.io.File BinaryFile,double Nlin,double Slin,double Ecol,double Wcol) throws java.io.IOException{

        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;

        outputDir = new FileOutputStream(BinaryFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);
        System.out.println("n linhas   " +((int)(Nlin)-(int)(Slin)));
        System.out.println("n col   " +((int)(Ecol)-(int)Wcol));
        for (int i=(int)(Slin);i<=(int)(Nlin);i++) for (int j=(int)(Wcol);j<=(int)Ecol;j++) {
            //System.out.println("i " +i +"  j " + j +"    matrix  "+matrixORI[i][j]);
            newfile.writeFloat(matrixORI[i][j]);
            //if(matrixORI[i][j]>0) System.out.println("i " +i +"  j " + j +"  value  " + matrixORI[i][j]);
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

    public static void main(String[] args) throws java.io.IOException{

        String variable = args[0];
        String InputFolder =args[1];
        
        //String variableOutput= args[0];
        String year= args[2];
        java.text.DecimalFormat format1 = new java.text.DecimalFormat("000");
        double LatSouth=40.0;
        double LatNorth=45.0;
        double LongWest=-97.0;
        double LongEast=-89.0;
//          double LatSouth=25.063000;
//        double LatNorth=52.938000;
//        double LongWest=-124.938000;
//        double LongEast=-67.063000;
        
        //Input Limits:
        double Slat=25.000000;
        double Nlat=52.000000;
        double res=0.125000;
        double Wlong=-125.00000;
        double Elong=-67.00000;
        int ncol=464;
        int nlin=224;
        int nxny=103936;
        
        double Nlin=Math.floor((LatNorth-Slat)/res);
        double Slin=Math.floor((LatSouth-Slat)/res);
        double Wcol=Math.floor((LongWest-Wlong)/res);
        double Ecol=Math.floor((LongEast-Wlong)/res);
        
        double FinLatSouth=Slat+Slin*res;
        double FinLatNorth=Slat+Nlin*res;
        double FinLongEast=Ecol*res+Wlong;
        double FinLongWest=Wcol*res+Wlong;
        
        double Fncol=Ecol-Wcol+1;
        double Fnlin=Nlin-Slin+1;
        System.out.println("Fncol " + Fncol +" Fnlin " +Fnlin);
        System.out.println("Slin " + Slin +" FinLatSouth " +FinLatSouth);
        System.out.println("Nlin " + Nlin +" FinLatNorth " +FinLatNorth);
        System.out.println("Wcol " + Wcol +" FinLongWest "+FinLongWest);
        System.out.println("Ecol " + Ecol +" FinLongEast " +FinLongEast);
        
        int flaginfo=1;
        //System.exit(1);
        
        // 1 - FORA - precipitation - APCP
        // 2 - FORA - potential evaporation - PEVAP
        // 3 - MODEL RESULTS - snow melt - SNOM
        // 4 - MODEL RESULTS - soil moisture content - 0:200 - SOILM:0-200
        // 5 - MODEL RESULTS - plant canopy surface water - CNWAT:sfc 
        String strRootdir="/Groups/IFC/NLDAS_Data/"+InputFolder+"/" +year +"/";
        System.out.println("strRootdir " + strRootdir);
        String Outfile=strRootdir.replace("ASC", "VHC");
        (new File(Outfile)).mkdirs();
        
                String ens=".txt";
           
                ArrayList<File> files = HydroNexradToCRas.getFileList(new File(strRootdir),ens);
	        System.out.println("strRootdir files.size()"+files.size());
                Iterator i = files.iterator();
                //System.exit(0);
                
	        while (i.hasNext()){
                    File temp = (File) i.next();
                    System.out.println(temp.getName());
                      //new java.io.File AsciiFile = java.io.File(FileAscIn);
                    try {
                        
                        
                  System.out.println(temp +"    -    " +Outfile);
                  
                    new NLDASDataToCRas1(variable,temp,new java.io.File(Outfile),Nlin,Slin,Ecol,Wcol,nlin,ncol);
                    } catch (Exception IOE){
                        System.out.print(IOE);
                        System.exit(0);
                    }
               }
         double ressec=res*3600;   
         createMetaFile(new java.io.File(Outfile),variable, ressec, 60, Fncol,Fnlin,FinLatSouth,Math.abs(FinLongWest));      
      
    }
    
    public static void createMetaFile(File directory, String variable, double nresol, double tresol, double Finalcol, double Finallin,double Slat,double Wlong) {
        try {
            File saveFile = new File(directory.getPath() + File.separator + variable + ".metaVHC");
            System.out.print(saveFile);
      
            // String F=directory.getPath()+File.separator+"/bin/"+"prec.metaVHC";
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
             System.out.println("file = "+ saveFile);
            // System.out.println("in metafile function new_s_res = "+ newsresol+"new_t_res = "+newtresol+"Frows = "+Finalrows + "Fcolumns = "+Finalcolumns);
            writer.println("[Name]");
            // writer.println("Precipitation Radar Data From KICT - basin mode");
            writer.println(variable+" FROM NLDAS");
             writer.println("[Southernmost Latitude]");
            int value1=(int)Math.floor(Slat);
            int value2=(int)Math.floor((Slat-value1)*60);
            int value3=(int)Math.floor((((Slat-value1)*60)-value2)*60);
            writer.println(value1+":"+value2+":"+value3+" N"); // Iowa River
                  writer.println("[Westernmost Longitude]");
            
            value1=(int)Math.floor(Wlong);
            value2=(int)Math.floor((Wlong-value1)*60);
            value3=(int)Math.floor((((Wlong-value1)*60)-value2)*60);
            
            writer.println(value1+":"+value2+":"+value3+" W"); 
             //writer.println("98:08:00.00 W");// KINX radar
            // writer.println("100:04:00.00 W"); // KICT radar
            //writer.println("99:19:00.00 W"); // KTLX radar
            //writer.println("98:08:00.00 W");// KINX radar
            writer.println("[Longitudinal Resolution (ArcSec)]");
            writer.println(nresol);
            writer.println("[Latitudinal Resolution (ArcSec)]");
            writer.println(nresol);
            writer.println("[# Columns]");
            //writer.println("305");// KTLX radar
            writer.println((int)Finalcol);// KINX radar
            writer.println("[# Rows]");
            //writer.println("250");// KTLX radar
            writer.println((int)Finallin);// KINX radar
            writer.println("[Format]");
            writer.println("Float");
            writer.println("[Missing]");
            writer.println("-9.0");
            writer.println("[Temporal Resolution]");
            writer.println((int)tresol + "-minutes");
            writer.println("[Units]");
            writer.println("mm/h");
            writer.println("[Information]");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KTLX");
            //writer.println("Precipitation data downloaded from NEXRAD - radar mode - KINX");
            // writer.println("Precipitation data downloaded from NEXRAD - radar mode - KICT");
            writer.println("Precipitation obtaiend in NLDAS");
            writer.close();
        } catch (IOException bs) {
            System.out.println("Error composing metafile: " + bs);
        }

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
 
public static String Outfilename(String fileName,String ident) {
        System.out.println("fileName - FROM");
            System.out.println(fileName);
            String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
            String[] months2={"JAN","FEB","MAR","APR","MAY","JUN","JUL","AUG","SEP","OCT","NOV","DEC"};      
            String monthString;
            
        String[] timeStamp = new String[6];
       
         int index=fileName.lastIndexOf("H.A") + 3;
         //System.out.println("index    =     " + index);
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
        timeStamp[3]=fileName.substring(index+9,index+11); // hour
        System.out.println(timeStamp[3]);
        timeStamp[4]="00"; // min

        System.out.println(timeStamp[4]);//      String monthString=months[(Integer.parseInt(timeStamp[1])-1)];
        timeStamp[5]="00"; // seg
        String vhcFilename=ident+"."+timeStamp[3]+timeStamp[4]+"00"+"."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        System.out.println(" to "+vhcFilename);
       return vhcFilename;
    }
  



}

 
    




