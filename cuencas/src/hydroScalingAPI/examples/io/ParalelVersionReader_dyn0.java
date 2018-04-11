/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.io;
import java.io.*;
import java.text.DecimalFormat;
/**
 *
 * @author pmandapa
 */

import java.io.*;
import java.util.*;
 
public class ParalelVersionReader_dyn0 extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    float[]          UpsArea;
    float[]          HortonO;
    float[]          Qmax;
    float[]          Tmax;
    int              nlinks;
    int              nhydro;
    int              ntime;
    String[]         IDJ;
    float[][]        hydrographs;
    
    float[]          nelem;
    float[][]        HTime;
    int[]           xxx;
    int[]           yyy;
    
    private  int              columns,rows;
    float[][] Coverage;
    float[][] AvePrec;
    float[][] Stddev;
    float[][] max;
    float[][] accum;
    float[][] PCM;
    float[][] BCM;
    float[][] RTime;
    int     nert;
    int[]   xxxS;
    int[]   yyyS;
    int nelemtime;

    float[] LinknHyd;
    float[] LinknRain;
    float[] Linkn;

        float[]  meanRainS;
        float[]  accumRainS;
        float[]  maxRainS;
        float[]  TmaxRainS;
        float[]  CoverageS;
        int nlS;

    public ParalelVersionReader_dyn0(java.io.File InputFile,String ident,java.io.File OutFile)
            throws java.io.IOException {
        

         java.io.File[] lasQueSi=InputFile.listFiles();
         // info for the T and Q max
         UpsArea = new float[200000];
         HortonO = new float[200000];
         Qmax = new float[200000];
         Tmax = new float[200000];

         // info prec space
         Linkn = new float[200000];
         meanRainS = new float[200000];
         accumRainS = new float[200000];
         maxRainS = new float[200000];
         TmaxRainS = new float[200000];
         CoverageS = new float[200000];
         nlS=0;
         // info for the hydrographs - Outlet file
         nelem=new float[1000];
         HTime=new float[2000][4000];
         xxx=new int[1000];
         yyy=new int[1000];
         nhydro=0;
         hydrographs=new float[2000][4000];
         LinknHyd = new float[1000]; // = x+3848*y for iowa and cedar river
         LinknRain = new float[1000]; // = x+3848*y for iowa and cedar river
         // info for precipitation - time
         RTime=new float[2000][4000];
         xxxS=new int[1000];
         yyyS=new int[1000];

         Coverage=new float[2000][4000];
         AvePrec=new float[2000][4000];
         Stddev=new float[2000][4000];
         max=new float[2000][4000];
         accum=new float[2000][4000];
         PCM=new float[2000][4000];
         BCM=new float[2000][4000];
         nert=0;
         nelemtime=0;

         //IDJ = new String[200000];
         nlinks=0;
         
         ntime=0;
         double Tinit=20200620;
         //double Tinit=2.10387600106333*Math.pow(10, 7);
         System.out.println("lenght - "+lasQueSi.length);
         for (int i=0;i<lasQueSi.length;i++)
             {
             System.out.println("lasQueSi[i] - "+lasQueSi[i]);


                 int flag = checkfile(lasQueSi[i]);
                 if(flag==-1) readFile1(lasQueSi[i],Tinit);
                 if(flag==1) readFile2(lasQueSi[i],Tinit);
                 if(flag==5) readFile3(lasQueSi[i],Tinit);
             }
        DecimalFormat df = new DecimalFormat("###");
        DecimalFormat df1 = new DecimalFormat("###.#");
        DecimalFormat df2 = new DecimalFormat("###.##");
        DecimalFormat df3 = new DecimalFormat("###.###");


         
         String FileName=lasQueSi[0].getName();
         java.io.File theFile;
         theFile = new java.io.File(OutFile + "/sumary"+ident+".csv");
         //theFile=new java.io.File(InputFile+"/sumary.csv");
         System.out.println("Writing disc1 - "+theFile);

         java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
         java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
         java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
         newfile.write("order,"+"area,"+"Qmax,"+ "Tmax" + "fileID"+"\n");
         for (int ii=0;ii<nlinks;ii++){
             //newfile.write(HortonO[ii]+","+UpsArea[ii]+","+Qmax[ii]+","+Tmax[ii]+","+IDJ[ii]+"\n");
         newfile.write(HortonO[ii]+","+UpsArea[ii]+","+df2.format(Qmax[ii])+","+df2.format(Tmax[ii])+"\n");
         }
         newfile.close();
         bufferout.close();

         // write output file



         theFile = new java.io.File(OutFile + "/hydrograph"+ident+".csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         int j=0;
         //IOWA
         //int[] hyd = {44,136,1,550,600,650,700,nhydro-1};
         //Cedar River
         
         
         for (int it=0;it<nhydro;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<ntime;ii++){
           //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nhydro;it++)
           {
            newfile.write(df2.format(hydrographs[it][ii])+",");
           }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();

theFile = new java.io.File(OutFile + "/Time_hydrographh"+ident+".csv");
          //theFile=new java.io.File(InputFile+"/Time_hydrograph.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         j=0;
         //IOWA
         //int[] hyd = {44,136,1,550,600,650,700,nhydro-1};
         //Cedar River
         for (int it=0;it<nhydro;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<ntime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nhydro;it++)
           {newfile.write(HTime[it][ii]+","); }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();

         //theFile=new java.io.File(InputFile+"/rainfallhill.csv");
         theFile = new java.io.File(OutFile + "/rainfallhill"+ident+".csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int i=0;i<nlS;i++)
         {newfile.write(Linkn[i]+","+meanRainS[i]+","+accumRainS[i]+","+maxRainS[i]+","+
                  TmaxRainS[i]+","+CoverageS[i]+"\n");
         }


         newfile.close();
         bufferout.close();
         theFile = new java.io.File(OutFile + "/rainfallSTTime"+ident+".csv");
         //theFile=new java.io.File(InputFile+"/rainfallSTTime.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int it=0;it<nert;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<nelemtime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nert;it++)
           {newfile.write(RTime[it][ii]+","); }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();

        theFile = new java.io.File(OutFile + "/rainfallSTCov"+ident+".csv");
         //theFile=new java.io.File(InputFile+"/rainfallSTCov.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int it=0;it<nert;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<nelemtime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nert;it++)
           {newfile.write(Coverage[it][ii]+","); }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();
         theFile = new java.io.File(OutFile + "/rainfallSTAvePrec"+ident+".csv");
         //theFile=new java.io.File(InputFile+"/rainfallSTAvePrec.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int it=0;it<nert;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<nelemtime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nert;it++)
           {newfile.write(AvePrec[it][ii]+","); }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();
theFile = new java.io.File(OutFile + "/rainfallSTddev"+ident+".csv");
         //theFile=new java.io.File(InputFile+"/rainfallSTddev.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int it=0;it<nert;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<nelemtime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nert;it++)
           {newfile.write(Stddev[it][ii]+","); }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();
theFile = new java.io.File(OutFile + "/rainfallSTMax"+ident+".csv");
         //theFile=new java.io.File(InputFile+"/rainfallSTMax.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int it=0;it<nert;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<nelemtime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nert;it++)
           {newfile.write(max[it][ii]+","); }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();
theFile = new java.io.File(OutFile + "/rainfallSTAccum"+ident+".csv");
         //theFile=new java.io.File(InputFile+"/rainfallSTAccum.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int it=0;it<nert;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<nelemtime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nert;it++)
           {newfile.write(accum[it][ii]+","); }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();
theFile = new java.io.File(OutFile + "/rainfallSTPCMm"+ident+".csv");
         //theFile=new java.io.File(InputFile+"/rainfallSTPCM.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int it=0;it<nert;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<nelemtime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nert;it++)
           {newfile.write(PCM[it][ii]+","); }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();
theFile = new java.io.File(OutFile + "/rainfallSTBCM"+ident+".csv");
         //theFile=new java.io.File(InputFile+"/rainfallSTBCM.csv");
         System.out.println("Writing disc1 - "+theFile);

         salida = new java.io.FileOutputStream(theFile);
         bufferout = new java.io.BufferedOutputStream(salida);
         newfile = new java.io.OutputStreamWriter(bufferout);
         for (int it=0;it<nert;it++) newfile.write(LinknHyd[it]+",");
         newfile.write("\n");
         for (int ii=0;ii<nelemtime;ii++){
            //for (int jj=0;jj<nhydro;jj++){
           for (int it=0;it<nert;it++)
           {newfile.write(BCM[it][ii]+","); }
          newfile.write("\n");
         }
         newfile.close();
         bufferout.close();

         // write output file

    }
    
      public void readFile1(java.io.File InputFile,double Ti) throws java.io.IOException{

          java.io.FileReader ruta;
          java.io.BufferedReader buffer;

          java.util.StringTokenizer tokens;
          String linea=null, basura, nexttoken;
          //System.out.println("InputFile" + InputFile);
          ruta = new FileReader(InputFile);
          buffer=new BufferedReader(ruta);
          String data = buffer.readLine(); // JUMP 3 LINES IN THE BEGINING
          data = buffer.readLine();
          data = buffer.readLine();
          data = buffer.readLine(); // Horton
          //READ Horton order
          //System.out.println("data" + data);
          tokens = new StringTokenizer(data,",");
          String temp=new String(tokens.nextToken());
          //System.out.println("temp   " + temp);
          int ii=nlinks;
          //int fileid=InputFile.getName().indexOf("_");
          //int fileid2=InputFile.getName().indexOf("_",fileid+1);
         // System.out.println(InputFile.getName()+"fileid - "+fileid + "fileid2  " + fileid2);
         // System.out.println(InputFile.getName()+"fileid - "+fileid + "fileid2  " + fileid2 + "   " + InputFile.getName().substring(fileid+1,fileid2));
          //String tt=InputFile.getName().substring(fileid+1,fileid2);
          while (tokens.hasMoreTokens())
            {
            HortonO[ii]=new Float(tokens.nextToken());
            //IDJ[ii]="1";
            ii=ii+1;
            }
            data = buffer.readLine();
           //READ Horton order
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlinks;
           while (tokens.hasMoreTokens())
             {
             UpsArea[ii]=new Float(tokens.nextToken());
             ii=ii+1;
             }
           for (int j=0;j<7;j++) data = buffer.readLine();
           //READ Upstream Area
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlinks;
           while (tokens.hasMoreTokens())
             {
             float number1;
             float number2;
             temp=new String(tokens.nextToken());
             if(temp.indexOf("E")<1) {Qmax[ii]=Float.valueOf(temp);}
             else {
             number1=Float.valueOf(temp.substring(0,temp.indexOf("E")));
             number2=Float.valueOf(temp.substring((temp.indexOf("E")+1),temp.length()));
             float TTemp=number1 * (float)Math.pow(10,number2);
             Qmax[ii]=TTemp;
             }
             ii=ii+1;
           }
           data = buffer.readLine();
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlinks;
           

           while (tokens.hasMoreTokens())
             {
             double number1;
             double number2;
             temp=new String(tokens.nextToken());
             double TTemp=0;
             if(temp.indexOf("E")<1) {TTemp=Double.valueOf(temp);}
             else {
             number1=Double.valueOf(temp.substring(0,temp.indexOf("E")));
             number2=Double.valueOf(temp.substring((temp.indexOf("E")+1),temp.length()));
             TTemp=number1 * Math.pow(10,number2);
             }

             //TTemp=(TTemp-(float)Ti)/(24*60);
             //float TTemp=new Float(tokens.nextToken());
             Tmax[ii]=(float)TTemp-(float)Ti;
             ii=ii+1;
             }
             nlinks=ii;
           // READ RAIN
//           data = buffer.readLine();
//           data = buffer.readLine();
//           data = buffer.readLine();
//           int j=0;
//
//           while (data != null)
//             {
//             double number1;
//             double number2;
//             tokens = new StringTokenizer(data,",");
//
//             temp=new String(tokens.nextToken());
//             double TTemp=0;
//             if(temp.indexOf("E")<1) {TTemp=Double.valueOf(temp);}
//             else {
//             number1=Double.valueOf(temp.substring(0,temp.indexOf("E")));
//             number2=Double.valueOf(temp.substring((temp.indexOf("E")+1),temp.length()));
//             TTemp=number1 * Math.pow(10,number2);}
//
//             TTemp=(TTemp-Tinit)/(24*60);
//             RTime[j]=(float) TTemp;
//             while (tokens.hasMoreTokens())
//             {
//              AvePrec[j]=AvePrec[j]+new Float(tokens.nextToken());
//              nelem[j]=nelem[j]+1;
//             }
//
//             j=j+1;
//             data = buffer.readLine();
//             if(j>=8000)data =null;
//
//             }
//

    }

      public void readFile2(java.io.File InputFile,double Ti) throws java.io.IOException{

          java.io.FileReader ruta;
          java.io.BufferedReader buffer;

          java.util.StringTokenizer tokens = null;
          String linea=null, basura, nexttoken;

          ruta = new FileReader(InputFile);
          buffer=new BufferedReader(ruta);
          String data = "test"; // JUMP 1 LINE IN THE BEGINING
          data = buffer.readLine(); // JUMP 2 LINE IN THE BEGINING
          data = buffer.readLine(); // JUMP 2 LINE IN THE BEGINING
          String temp;

          String filename=InputFile.getName();
          int i1=filename.indexOf("_")+1;
          int i2=filename.indexOf("_",i1)-1;
          int i3=filename.indexOf("_",i1)+1;
          int i4=filename.indexOf("-",i1)-1;

          int ii=nhydro;
          xxx[ii] = Integer.valueOf(filename.substring(i1,i2));
          yyy[ii] = Integer.valueOf(filename.substring(i3,i4));

          LinknHyd[ii]=xxx[ii]+yyy[ii]*3848;

           int j=0;
              data = buffer.readLine();
          //READ Horton order

           while (data != null)
           {
               double number1;
               double number2;
               tokens = new StringTokenizer(data,",");

                temp=new String(tokens.nextToken());
               double TTemp;
               if(temp.indexOf("E")<1) {TTemp=Double.valueOf(temp);
               TTemp=TTemp-(Ti);
               }
               else {

               number1=Double.valueOf(temp.substring(0,temp.indexOf("E")));
               number2=Double.valueOf(temp.substring((temp.indexOf("E")+1),temp.length()));
               TTemp=number1 * Math.pow(10,number2);
               TTemp=(TTemp-Ti);}

               HTime[ii][j]=(float)TTemp;
               
               // check if there is an E

             temp=new String(tokens.nextToken());
             if(temp.indexOf("E")<1) {hydrographs[ii][j]=Float.valueOf(temp);}
             else {
              number1=Double.valueOf(temp.substring(0,temp.indexOf("E")));
              number2=Double.valueOf(temp.substring((temp.indexOf("E")+1),temp.length()));
              hydrographs[ii][j]=(float)(number1 * Math.pow(10,number2));}

                j=j+1;
                data = buffer.readLine();
                if(j>=1000)data =null;
//System.out.println(ii + "temp" +temp+ "j" + j);
           
           }
            nhydro=nhydro+1;
            ntime=Math.max(ntime,j);

}



      public void readFile3(java.io.File InputFile,double Ti) throws java.io.IOException{

          java.io.FileReader ruta;
          java.io.BufferedReader buffer;

          java.util.StringTokenizer tokens;
          String linea=null, basura, nexttoken;
          //System.out.println("InputFile" + InputFile);
          ruta = new FileReader(InputFile);
          buffer=new BufferedReader(ruta);
          String data = buffer.readLine();
          for (int i=0;i<6;i++) data = buffer.readLine(); // JUMP 1+5 LINES IN THE BEGINING
          data = buffer.readLine(); // Read x

          //System.out.println("data" + data);
          tokens = new StringTokenizer(data,",");
          String temp=new String(tokens.nextToken()); // header
          //System.out.println("temp   " + temp);

          float[] xx=new float[20000];
          float[] yy=new float[20000];

          int irel=0;
          while (tokens.hasMoreTokens())
            {
            xx[irel]=new Float(tokens.nextToken());
            //IDJ[ii]="1";
            irel=irel+1;
            }
            data = buffer.readLine();
           //READ Horton order
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
            irel=0;
           while (tokens.hasMoreTokens())
             {
             yy[irel]=new Float(tokens.nextToken());
             irel=irel+1;
             }
           int ii=nlS;
           for (int j=0;j<xx.length;j++)
           {Linkn[ii] = xx[j]+yy[j]*3848;
           ii=ii+1;
           }
           data = buffer.readLine(); // jump distance to outlet
           data = buffer.readLine(); // meanRainfall

           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlS;
           while (tokens.hasMoreTokens())
             {
             meanRainS[ii]=new Float(tokens.nextToken());
             ii=ii+1;
             }

           data = buffer.readLine(); // accumRainfall
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlS;
           while (tokens.hasMoreTokens())
             {
             accumRainS[ii]=new Float(tokens.nextToken());
             ii=ii+1;
             }

           data = buffer.readLine(); // maxRainfall
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlS;
           while (tokens.hasMoreTokens())
             {
            maxRainS[ii]=new Float(tokens.nextToken());
             ii=ii+1;
             }

           data = buffer.readLine(); // maxRainfall
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlS;
           while (tokens.hasMoreTokens())
             {
            float ttemp=new Float(tokens.nextToken());
            TmaxRainS[ii]=ttemp-(float)Ti;
            ii=ii+1;
             }


           data = buffer.readLine(); // CoverageS
           tokens = new StringTokenizer(data,",");
           temp=new String(tokens.nextToken());
           ii=nlS;
           while (tokens.hasMoreTokens())
             {
            CoverageS[ii]=new Float(tokens.nextToken());
            ii=ii+1;
             }

          nlS=ii;

          String filename=InputFile.getName();
          int i1=filename.indexOf("_")+1;
          int i2=filename.indexOf("_",i1)-1;
          int i3=filename.indexOf("_",i1)+1;
          int i4=filename.indexOf("-",i1)-1;


          xxxS[nert] = Integer.valueOf(filename.substring(i1,i2));
          yyyS[nert] = Integer.valueOf(filename.substring(i3,i4));

          LinknRain[nert]=xxxS[nert]+yyyS[nert]*3848;


          for (int i=0;i<3;i++)   data = buffer.readLine();
           data = buffer.readLine(); // read first line of
          //READ Horton order
           int it=0;
           System.out.print("nert" + nert +"\n");
           while (data != null)
           {  //System.out.print("it" + it);
               tokens = new StringTokenizer(data,",");
               float tt=new Float(tokens.nextToken());
               RTime[nert][it]=tt-(float)Ti;
               Coverage[nert][it]=new Float(tokens.nextToken());
               AvePrec[nert][it]=new Float(tokens.nextToken());
               Stddev[nert][it]=new Float(tokens.nextToken());
               max[nert][it]=new Float(tokens.nextToken());
               accum[nert][it]=new Float(tokens.nextToken());
               PCM[nert][it]=new Float(tokens.nextToken());
               BCM[nert][it]=new Float(tokens.nextToken());
               data = buffer.readLine(); // read first line of
               it=it+1;
           }
           nelemtime=it;
           nert=nert+1;

    }


public static int checkfile(java.io.File OutputFile) throws java.io.IOException{

        String FileName=OutputFile.getName();

        int flag=FileName.indexOf("Outlet");
        if(flag!=-1) flag=1;
        if(flag==-1) 
           {flag=FileName.indexOf("complete");   
            if (flag!=-1) flag=2;}
        if(flag==-1) 
           {flag=FileName.indexOf("sumary");   
            if (flag!=-1) flag=3;}
        if(flag==-1)
           {flag=FileName.indexOf("rainfall");
            if (flag!=-1) flag=4;}
        if(flag==-1)
           {flag=FileName.indexOf("Prec");
            if (flag!=-1) flag=5;}
        if(flag==-1)
           {flag=FileName.indexOf("hydrograph");
            if (flag!=-1) flag=3;}
        if(flag==-1)
           {flag=FileName.indexOf("sumrainT");
            if (flag!=-1) flag=3;}
        if(flag==-1)
           {flag=FileName.indexOf("sumrainS");
            if (flag!=-1) flag=3;}

                 if (flag == -1) {
            flag = FileName.indexOf("Tile_");
            if (flag != -1) {
                flag = 7;
            }
        }

        return flag;
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
    
    public static void main(String[] args) throws IOException {
      
        float[] lamb1 = {0.2f};
        //int[] time = {};
        
        float[][] matrix_temp;
        float NDecRes;    // number of time we would like to reduce the spatial resolution
        float new_t_res;

int[] PrecAr = {120,15};
        float[] lam2 = {-0.16f};
        float[] lam1 = {0.26f};
        float[] v_o = {0.88f};
        //float[] vostAr = {0.17f,0.0f,0.3f};
        float[] vostAr = {0.17f};

        for (float vo : v_o) {
            float VO = vo;
            for (float l2 : lam2) {
                float L2 = l2;
                for (float l1 : lam1) {
                    float L1 = l1;
                for (int pr : PrecAr) {
                    int PR = pr;
                    for (float vst : vostAr) {
                    float VST = vst;
           // File folder = new File("D:/CUENCAS/CedarRapids/simulation/RadarAgreg_v2/CedarRapids_SatRC1.00/AveragedIowaRiver_0.4_0.2_-0.1/");
            File folder = new File("/usr/home/rmantill/luciana/Parallel/veloc_studyOriginal/3CedarRiver/" + PR + "/"+ VO + "/" + L1 + "/" + L2 + "/"+ VST + "/");
            File outfolder = new File("/usr/home/rmantill/luciana/Parallel/veloc_studyOriginal/3CedarRiver/results/");
            outfolder.mkdirs();
            System.out.println("folder - "+folder);
            System.out.print(folder);
            try{
            String str = "Hg_PR" + PR + "VO"+ VO + "L1" + L1 + "L2" + L2 + "VST"+ VST;
            new ParalelVersionReader_dyn0(folder,str,outfolder);
            } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);}
           }
                }
                }
            }
        }


    }

}




