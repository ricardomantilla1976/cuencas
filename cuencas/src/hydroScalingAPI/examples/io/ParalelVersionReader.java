/* To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.io;

import java.io.*;
import java.text.DecimalFormat;
/**f
 *
 * @author pmandapa
 */
import java.io.*;
import java.util.*;

public class ParalelVersionReader extends Object {

    private String[] variables = new String[8];
    private String[] metaInfo = new String[12];
    private String fileName;
    int nhydro;
    int ntime;
    String[] IDJ;
    float[][] hydrographs;
    float[][] Storage2;
    float[][] Storage1;
    float[][] Storage0;
    float[] RRTime;
    float[][] Rain;
    float[] nelem;
    float[][] HTime;
    int[] xxx;
    int[] yyy;
    float[] hydroorder;
    float[] hydroarea;
    float[] hydrolat;
    float[] hydrolong;
    // Link information from entire basin
    int[] Linkcode;
    int[] Linkorder;
    float[] Linkarea;
    float[] LinkupsLength;
    float[] LinkdOutlet;
    int[] LinkX;
    int[] LinkY;
    float[] Linklat;
    float[] Linklong;
    float maxdistOutlet;
    float maxupsarea;
    int nlinksT;
    //private  int              columns,rows;
    float[][] AvePrec;
    float[][] accum;
    float[] maxtime;
    float[] coveragetime;
    float[] avetime;
    float[] BCM;
    float[] PCM;
    float[] distMaxtime;
    float[][] RTime;
    int[] nelemDO;
    //int     nert;
    //int[]   xxxS;
    //int[]   yyyS;
    int nelemtime;
    float[] LinknHyd;
    //float[] LinknRain;
    float[] Linkn;
    int[] XLinkn;
    int[] YLinkn;
    float[] CodeL;
    float[] UpsArea;
    float[] HortonO;
    float[] xxFile;
    float[] yyFile;
    float[] Qmax;
    float[] Tmax;
    int nlinks;
    float[] DistOutlet;
    float[] meanRainS;
    float[] accumRainS;
    float[] maxRainS;
    float[] TmaxRainS;
    float[] CoverageS;
    int nlS;
    int ntimestep;
    int NC;
    int NL;
    float RES;

    public ParalelVersionReader(java.io.File InputFile, java.io.File LinkAnal, String ident, java.io.File OutFile)
            throws java.io.IOException {


        java.io.File[] lasQueSi = InputFile.listFiles();

        if (lasQueSi == null && lasQueSi.length<55) {
            System.out.println("NO FILES");
            return;
        }

        // info for the T and Q max
        //nLinksT - approximate number of total links
        int nest = 80000;
        Linkcode = new int[nest];
        Linkorder = new int[nest];
        Linkarea = new float[nest];
        LinkupsLength = new float[nest];
        LinkdOutlet = new float[nest];
        LinkX = new int[nest];
        LinkY = new int[nest];
        Linklat = new float[nest];
        Linklong = new float[nest];



        LinkAnalysisFile(LinkAnal);
        System.out.println("READ LINK ANALYSES, n links = " + nlinksT);
        int noutlet = (int) (lasQueSi.length / 3) + 20; // approximated number of outlets
        //System.out.println("nlinksT = " + nlinksT + "noutlet = " + noutlet);
        UpsArea = new float[nlinksT + 1000];
        HortonO = new float[nlinksT + 1000];
        xxFile = new float[nlinksT + 1000];
        yyFile = new float[nlinksT + 1000];
        Qmax = new float[nlinksT + 1000];
        Tmax = new float[nlinksT + 1000];
        DistOutlet = new float[nlinksT + 1000];
        // info prec space
        ntimestep = 14000;
        Linkn = new float[nlinksT + 1000];
        CodeL = new float[nlinksT + 1000];
        XLinkn = new int[nlinksT + 1000];
        YLinkn = new int[nlinksT + 1000];
        meanRainS = new float[nlinksT + 1000];
        accumRainS = new float[nlinksT + 1000];
        maxRainS = new float[nlinksT + 1000];
        TmaxRainS = new float[nlinksT + 1000];
        CoverageS = new float[nlinksT + 1000];
        RRTime = new float[ntimestep];
//         nlS=0;
        // info for the hydrographs - Outlet file
//         nelem=new float[1000];
        HTime = new float[noutlet][ntimestep];
        xxx = new int[noutlet];
        yyy = new int[noutlet];
        hydroorder = new float[noutlet];
        hydroarea = new float[noutlet];
        hydrolat = new float[noutlet];
        hydrolong = new float[noutlet];
        nhydro = 0;

        hydrographs = new float[noutlet][ntimestep];
        Storage0 = new float[noutlet][ntimestep];
        Storage1 = new float[noutlet][ntimestep];
        Storage2 = new float[noutlet][ntimestep];
        LinknHyd = new float[noutlet]; // = x+3848*y for iowa and cedar river
//         LinknRain = new float[1000]; // = x+3848*y for iowa and cedar river
        // info for precipitation - time
//         RTime=new float[2000][4000];
//         xxxS=new int[1000];
//         yyyS=new int[1000];

        int ndistoutlet = (int) Math.ceil(maxdistOutlet / 10);
        //System.out.println("\n" + "max dist outlet = " + maxdistOutlet + "n group - " + ndistoutlet + "\n");
        Rain = new float[ndistoutlet][ntimestep];

        accum = new float[ndistoutlet][ntimestep];
        AvePrec = new float[ndistoutlet][ntimestep];
        avetime = new float[ntimestep];
        maxtime = new float[ntimestep];
        coveragetime = new float[ntimestep];
        PCM = new float[ntimestep];
        BCM = new float[ntimestep];
        distMaxtime = new float[ntimestep];
        nelemDO = new int[ndistoutlet];
        //
        for (int i = 0; i < ntimestep; i++) {
            avetime[i] = 0.0f;
            maxtime[i] = 0.0f;
            coveragetime[i] = 0.0f;
            PCM[i] = 0.0f;
            BCM[i] = 0.0f;
            distMaxtime[i] = 0.0f;
            for (int j = 0; j < ndistoutlet; j++) {
                accum[j][i] = 0.0f;
                AvePrec[j][i] = 0.0f;
            }
        }

        //nert=0;
        // Open Link analysis file
        nelemtime = 0;

        //IDJ = new String[200000];
        nlinks = 0;

        ntime = 0;
        double Tinit = 20200320;
        //double Tinit=2.10387600106333*Math.pow(10, 7);
        System.out.println("lenght - " + lasQueSi.length);
        for (int i = 0; i < lasQueSi.length; i++) {
            //System.out.println("lasQueSi[i] - " + lasQueSi[i]);

            int flag = checkfile(lasQueSi[i]);
            if (flag == -1) {
                readFile1(lasQueSi[i], Tinit);
            }
            if (flag == 1) {
                readFile2(lasQueSi[i], Tinit);
            }

            //       if(flag==5) readFile3(lasQueSi[i],Tinit);
        }

        //GenerateStatisticsRain();

        DecimalFormat df = new DecimalFormat("###");
        DecimalFormat df1 = new DecimalFormat("###.#");
        DecimalFormat df2 = new DecimalFormat("###.##");
        DecimalFormat df3 = new DecimalFormat("###.###");



        String FileName = lasQueSi[0].getName();
        java.io.File theFile;

        theFile = new java.io.File(OutFile + "/statistics_links" + ident + ".csv");
        //System.out.println("Writing disc1 - " + theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        newfile.write("Linkcode," + "order," + "area," + "Qmax," + "Tmax," + "X," + "Y,"
                + "distoutlet," + "accumrain," + "meanrain," + "maxrain," + "Timemaxrain,"
                + "coverage" + "\n");
        for (int ii = 0; ii < nlinks; ii++) {
            //newfile.write(HortonO[ii]+","+UpsArea[ii]+","+Qmax[ii]+","+Tmax[ii]+","+IDJ[ii]+"\n");
            newfile.write(df.format(CodeL[ii]) + "," + df.format(HortonO[ii]) + "," + df2.format(UpsArea[ii]) + "," + df2.format(Qmax[ii]) + "," + df2.format(Tmax[ii])
                    + "," + LinkX[ii] + "," + LinkY[ii] + "," + df2.format(DistOutlet[ii]) + "," + df2.format(accumRainS[ii])
                    + "," + df2.format(meanRainS[ii]) + "," + df2.format(maxRainS[ii]) + "," + df2.format(TmaxRainS[ii])
                    + "," + df2.format(CoverageS[ii]) + "," + df.format(xxFile[ii]) + "," + df.format(yyFile[ii]) + "\n");
        }
        newfile.close();
        bufferout.close();

        // write output file


        theFile = new java.io.File(OutFile + "/hydrograph" + ident + ".csv");
        //System.out.println("Writing disc1 - " + theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        int j = 0;
        //IOWA
        //int[] hyd = {44,136,1,550,600,650,700,nhydro-1};
        //Cedar River


        for (int it = 0; it < nhydro; it++) {
            newfile.write(LinknHyd[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydroorder[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydroarea[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydrolat[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydrolong[it] + ",");
        }
        newfile.write("\n");

        for (int it = 0; it < nhydro; it++) {
            newfile.write(xxx[it] + ",");
        }
        newfile.write("\n");

        for (int it = 0; it < nhydro; it++) {
            newfile.write(yyy[it] + ",");
        }
        newfile.write("\n");

        for (int ii = 0; ii < ntime; ii++) {
            //for (int jj=0;jj<nhydro;jj++){
            for (int it = 0; it < nhydro; it++) {
                newfile.write(df2.format(hydrographs[it][ii]) + ",");
            }
            newfile.write("\n");
        }
        newfile.close();
        bufferout.close();


        theFile = new java.io.File(OutFile + "/Time_hydrograph" + ident + ".csv");
        //System.out.println("Writing disc1 - " + theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        j = 0;
        //IOWA
        //int[] hyd = {44,136,1,550,600,650,700,nhydro-1};
        //Cedar River
        for (int it = 0; it < nhydro; it++) {
            newfile.write(LinknHyd[it] + ",");
        }
        newfile.write("\n");
        for (int ii = 0; ii < ntime; ii++) {
            //for (int jj=0;jj<nhydro;jj++){
            for (int it = 0; it < nhydro; it++) {
                newfile.write(HTime[it][ii] + ",");
            }
            newfile.write("\n");
        }
        newfile.close();
        bufferout.close();


        theFile = new java.io.File(OutFile + "/Storage1" + ident + ".csv");
        //System.out.println("Writing disc1 - " + theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        j = 0;
        //IOWA
        //int[] hyd = {44,136,1,550,600,650,700,nhydro-1};
        //Cedar River


        for (int it = 0; it < nhydro; it++) {
            newfile.write(LinknHyd[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydroorder[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydroarea[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydrolat[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydrolong[it] + ",");
        }
        newfile.write("\n");
        for (int ii = 0; ii < ntime; ii++) {
            //for (int jj=0;jj<nhydro;jj++){
            for (int it = 0; it < nhydro; it++) {
                newfile.write(df2.format(Storage1[it][ii]) + ",");
            }
            newfile.write("\n");
        }
        newfile.close();
        bufferout.close();

        theFile = new java.io.File(OutFile + "/Storage0" + ident + ".csv");
        //System.out.println("Writing disc1 - " + theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        j = 0;
        //IOWA
        //int[] hyd = {44,136,1,550,600,650,700,nhydro-1};
        //Cedar River


        for (int it = 0; it < nhydro; it++) {
            newfile.write(LinknHyd[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydroorder[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydroarea[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydrolat[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydrolong[it] + ",");
        }
        newfile.write("\n");
        for (int ii = 0; ii < ntime; ii++) {
            //for (int jj=0;jj<nhydro;jj++){
            for (int it = 0; it < nhydro; it++) {
                newfile.write(df2.format(Storage0[it][ii]) + ",");
            }
            newfile.write("\n");
        }
        newfile.close();
        bufferout.close();


        theFile = new java.io.File(OutFile + "/Storage2" + ident + ".csv");
        //System.out.println("Writing Storage2 - " + theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        j = 0;
        //IOWA
        //int[] hyd = {44,136,1,550,600,650,700,nhydro-1};
        //Cedar River


        for (int it = 0; it < nhydro; it++) {
            newfile.write(LinknHyd[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydroorder[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydroarea[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydrolat[it] + ",");
        }
        newfile.write("\n");
        for (int it = 0; it < nhydro; it++) {
            newfile.write(hydrolong[it] + ",");
        }
        newfile.write("\n");



        for (int ii = 0; ii < ntime; ii++) {
            //for (int jj=0;jj<nhydro;jj++){
            for (int it = 0; it < nhydro; it++) {
                newfile.write(df2.format(Storage2[it][ii]) + ",");
            }
            newfile.write("\n");
        }
        newfile.close();
        bufferout.close();

        theFile = new java.io.File(OutFile + "/statistics_time" + ident + ".csv");
        //System.out.println("Writing disc1 - " + theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);

        newfile.write("timeID," + "aveRtime," + "maxRtime," + "distMAxOutl," + "Coverage," + "PCM," + "\n");

        for (int it = 0; it < nelemtime; it++) {
            avetime[it] = avetime[it] / nlinks;
            coveragetime[it] = coveragetime[it] / nlinks;
            PCM[it] =-9.9f;
            if (avetime[it] > 0 && BCM[it]>0) {
                PCM[it] = PCM[it] / (BCM[it] * avetime[it]);
            } else {
                PCM[it] = -9.9f;
            }
            newfile.write(df2.format(RRTime[it]) + "," + df2.format(avetime[it]) + "," + df2.format(maxtime[it]) + "," + df2.format(distMaxtime[it]) + "," + df2.format(coveragetime[it]) + "," + df2.format(PCM[it]) + "\n");
        }

        newfile.close();
        bufferout.close();

        theFile = new java.io.File(OutFile + "/statistics_dist_outlet" + ident + ".csv");
        //System.out.println("Writing disc1 - " + theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        newfile.write("1,");
        for (int ig = 1; ig < ndistoutlet; ig++) {
            newfile.write(ig * 10 + ",");
        }
        newfile.write("\n");
        newfile.write("2,");
        for (int ig = 0; ig < ndistoutlet; ig++) {
            newfile.write(nelemDO[ig] + ",");
        }
        newfile.write("\n");
        System.out.println("ndistoutlet - " + ndistoutlet + "  nelemtime" + nelemtime);
        for (int it = 0; it < nelemtime; it++) {
            newfile.write(RRTime[it] + ",");
            for (int ig = 0; ig < ndistoutlet; ig++) {
                float temp = 0.0f;
                if (nelemDO[ig] > 0) {
                    temp = (accum[ig][it] / nelemDO[ig]);
                } else {
                    temp = 0.0f;
                }
                newfile.write(df2.format(temp) + ",");
            }
            newfile.write("\n");
        }

        newfile.close();
        bufferout.close();
    }

    public void LinkAnalysisFile(java.io.File LinkAnal) throws java.io.IOException {

        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;

        //System.out.println("InputFile" + LinkAnal);
        ruta = new FileReader(LinkAnal);
        buffer = new BufferedReader(ruta);
        String data = buffer.readLine();
        nlinksT = 0;
        maxdistOutlet = 0;
        maxupsarea = 0;
        while (data != null) {
            //System.out.println("data" + data);
            tokens = new StringTokenizer(data, ",");
            String temp = new String(tokens.nextToken());

            Linkcode[nlinksT] = new Integer(tokens.nextToken());
            //System.out.println("Linkcode[nlinksT]" + Linkcode[nlinksT]);
            float tempf = new Float(tokens.nextToken());
            Linkorder[nlinksT] = (int) Math.ceil(tempf);
            //System.out.println("Linkorder[nlinksT]" + Linkorder[nlinksT]);
            Linkarea[nlinksT] = new Float(tokens.nextToken());
            //System.out.println("Linkarea[nlinksT]" + Linkarea[nlinksT]);
            LinkupsLength[nlinksT] = new Float(tokens.nextToken());
            LinkdOutlet[nlinksT] = new Float(tokens.nextToken());
            //System.out.println("LinkX[nlinksT]" + LinkX[nlinksT]);
            LinkX[nlinksT] = new Integer(tokens.nextToken());
            LinkY[nlinksT] = new Integer(tokens.nextToken());
            Linklat[nlinksT] = new Float(tokens.nextToken());
            Linklong[nlinksT] = new Float(tokens.nextToken());

            NC = new Integer(tokens.nextToken());
            NL = new Integer(tokens.nextToken());
            RES = new Float(tokens.nextToken());
            if (LinkdOutlet[nlinksT] > maxdistOutlet) {
                maxdistOutlet = LinkdOutlet[nlinksT];
            }
            if (Linkarea[nlinksT] > maxupsarea) {
                maxupsarea = Linkarea[nlinksT];
            }
            nlinksT = nlinksT + 1;
            data = buffer.readLine();

        }
        //System.out.println("\n" + "nlinksT = " + nlinksT + "\n");
    }

    public void readFile1(java.io.File InputFile, double Ti) throws java.io.IOException {


        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;
        String linea = null, basura, nexttoken;

        System.out.println("InputFile" + InputFile);
        ruta = new FileReader(InputFile);

        buffer = new BufferedReader(ruta);

        String data = buffer.readLine(); // JUMP 3 LINES IN THE BEGINING
        if(data==null) return;

        data = buffer.readLine();
        data = buffer.readLine();
        data = buffer.readLine(); // Horton
        //READ Horton order
        //System.out.println("data" + data);
        if (data.length() > 0) {
            tokens = new StringTokenizer(data, ",");
        } else {
            return;
        }
        String temp = new String(tokens.nextToken());

        String filename = InputFile.getName();
        String FileDir = InputFile.getParent();
        int xx = 0;
        int yy = 0;
        //System.out.println(filename);
        if (filename.contains("ned_1")) {
            int i1 = filename.indexOf("_") + 1;
            int i2 = filename.indexOf("_", i1);
            int i3 = filename.indexOf("_", i1) + 1;
            int i4 = filename.indexOf("_", i3);
            int i5 = filename.indexOf("_", i3) + 1;
            int i6 = filename.indexOf("-", i1);

            xx = Integer.valueOf(filename.substring(i3, i4));
            System.out.println(xx + "xx");
            yy = Integer.valueOf(filename.substring(i5, i6));
            System.out.println(yy + "yy");
        } else {
            int i1 = filename.indexOf("_") + 1;
            int i2 = filename.indexOf("_", i1);
            int i3 = filename.indexOf("_", i1) + 1;
            int i4 = filename.indexOf("-", i1);

            xx = Integer.valueOf(filename.substring(i1, i2));
            yy = Integer.valueOf(filename.substring(i3, i4));
        }

    if (filename.contains("ned_1_3")) {
            int ii=filename.lastIndexOf("ned_1_3");

            int i3 = filename.indexOf("_", ii) + 1;
            int i4 = filename.indexOf("_", i3);
            int i5 = filename.indexOf("_", i3) + 1;
            int i6 = filename.indexOf("-", i5);

            xx = Integer.valueOf(filename.substring(i3, i4));
            System.out.println(xx + "xx");
            yy = Integer.valueOf(filename.substring(i5, i6));
            System.out.println(yy + "yy");
        } else {
            int i1 = filename.indexOf("_") + 1;
            int i2 = filename.indexOf("_", i1);
            int i3 = filename.indexOf("_", i1) + 1;
            int i4 = filename.indexOf("-", i1);

            xx = Integer.valueOf(filename.substring(i1, i2));
            yy = Integer.valueOf(filename.substring(i3, i4));
        }
        
        int ii = nlinks;
        //int fileid=InputFile.getName().indexOf("_");
        //int fileid2=InputFile.getName().indexOf("_",fileid+1);
        // System.out.println(InputFile.getName()+"fileid - "+fileid + "fileid2  " + fileid2);
        // System.out.println(InputFile.getName()+"fileid - "+fileid + "fileid2  " + fileid2 + "   " + InputFile.getName().substring(fileid+1,fileid2));
        //String tt=InputFile.getName().substring(fileid+1,fileid2);
        while (tokens.hasMoreTokens()) {
            
            
            HortonO[ii] = new Float(tokens.nextToken());
         //   System.out.println("ii   " + ii + " HortonO[ii] " +HortonO[ii]);
            xxFile[ii] = xx;
            yyFile[ii] = yy;
            //IDJ[ii]="1";
            ii = ii + 1;
        }
        data = buffer.readLine();
        //System.out.println("upstream area" + data);
        //READ Horton order
        if (data.length() > 0) {
            tokens = new StringTokenizer(data, ",");
        } else {
            return;
        }
        temp = new String(tokens.nextToken());
        ii = nlinks;
        while (tokens.hasMoreTokens()) {
            UpsArea[ii] = new Float(tokens.nextToken());
            ii = ii + 1;
        }

        for (int j = 0; j < 9; j++) {
            data = buffer.readLine();
        }
        //System.out.println("area" + data);
        //READ Upstream Area
        if (data!=null) {
            tokens = new StringTokenizer(data, ",");
          

        } else {
            return;
        }
        temp = new String(tokens.nextToken());
        ii = nlinks;

        while (tokens.hasMoreTokens()) {
            String tt = tokens.nextToken();
            Qmax[ii] = new Float(tt);
//            try{
//                Qmax[ii] = new Float(tt);
//            }
//            catch(java.lang.NumberFormatException nfe)
//            {
//                Qmax[ii] =-9.9f;
//            }
//            
            // System.out.println(ii+"Qmax[ii]" + Qmax[ii]);
            ii = ii + 1;

        }
        //     System.out.println("out Q loop");
        data = buffer.readLine();
        //   System.out.println("data.length()" + data.length());
        if (data.length() > 0) {
            tokens = new StringTokenizer(data, ",");
        } else {
            return;
        }
        //   System.out.println("Time" + data);
        temp = new String(tokens.nextToken());
        ii = nlinks;


        while (tokens.hasMoreTokens()) {
            float TTemp = new Float(tokens.nextToken());
            Tmax[ii] = TTemp - (float) Ti;
            ii = ii + 1;
        }

        // READ RAIN
        data = buffer.readLine();
        data = buffer.readLine();
        data = buffer.readLine();
        // Read link info
        if (data.length() > 0) {
            tokens = new StringTokenizer(data, ",");
        } else {
            return;
        }
        temp = new String(tokens.nextToken());
        ii = nlinks;

        while (tokens.hasMoreTokens()) {
            Linkn[ii] = new Float(tokens.nextToken());
            ii = ii + 1;
        }
        data = buffer.readLine();
        //System.out.println("Xlinks" + data);
        // Read link X
        tokens = new StringTokenizer(data, ",");
        temp = new String(tokens.nextToken());

        ii = nlinks;

        while (tokens.hasMoreTokens()) {
            XLinkn[ii] = new Integer(tokens.nextToken());
            ii = ii + 1;
        }

        data = buffer.readLine();
        //System.out.println("Ylinks" + data);
        if (data.length() > 0) {
            tokens = new StringTokenizer(data, ",");
        } else {
            return;
        }
        // Read link Y
        tokens = new StringTokenizer(data, ",");
        temp = new String(tokens.nextToken());
        //ii=nlinks;

        for (int i = nlinks; i < ii; i++) {
            YLinkn[i] = new Integer(tokens.nextToken());
        }


        data = buffer.readLine();

        // Read link Y
        int iif = 0;
        int jt = 0;
        float[] tempRRtime = new float[10000];
        int nel = ii - nlinks + 10;
        float[][] tempRain = new float[nel][10000];
 //       System.out.println("before rain" + data);

        while (data != null && !data.isEmpty()) {
//System.out.println("in loop" +nel+"             " +  data);
            tokens = new StringTokenizer(data, ",");
            double TTemp = 0;
            RRTime[jt] = new Float(tokens.nextToken());
            tempRRtime[jt] = RRTime[jt];
            iif = 0;

            while (tokens.hasMoreTokens()) {
                tempRain[iif][jt] = new Float(tokens.nextToken());
                iif = iif + 1;
            }
            jt = jt + 1;
            data = buffer.readLine();
        }

        nelemtime = jt;

        // Statistics per link!!!!!
        for (int il = nlinks; il < ii; il++) {//find the distance to outlet

            int its = 0;
            int ndogroup = -99;

            while (its < nlinksT) {
                if (XLinkn[il] == LinkX[its] && YLinkn[il] == LinkY[its]) {
                    DistOutlet[il] = LinkdOutlet[its];
                    CodeL[il] = Linkcode[its];
                    its = nlinksT;
                }
                its = its + 1;
            }
            ndogroup = (int) Math.floor(DistOutlet[il] / 10);
            if (ndogroup >= 0) {
                nelemDO[ndogroup] = nelemDO[ndogroup] + 1;
            }
            accumRainS[il] = 0;
            meanRainS[il] = 0;
            maxRainS[il] = 0;
            CoverageS[il] = 0;
            // use its to find relative position in the rainfall vector
            its = il - nlinks;
            for (int it = 0; it < nelemtime; it++) {
                accumRainS[il] = accumRainS[il] + (tempRain[its][it]*(RRTime[jt]/60));
                meanRainS[il] = meanRainS[il] + tempRain[its][it];
                if (maxRainS[il] < tempRain[its][it]) {
                    maxRainS[il] = tempRain[its][it];
                }
                TmaxRainS[il] = tempRRtime[it];
                if (tempRain[its][it] > 0.01) {
                    CoverageS[il] = CoverageS[il] + 1;
                }
                if (ndogroup >= 0) {
                    accum[ndogroup][it] = accum[ndogroup][it] + tempRain[its][it];
                }
            }
            if (nelemtime > 0) {
                meanRainS[il] = meanRainS[il] / nelemtime;
            }
            if (nelemtime > 0) {
                CoverageS[il] = CoverageS[il] / nelemtime;
            } else {CoverageS[il]=0;}
            // Statistics per group of dist to outlet and time

        }
        //System.out.println("start statistics for time");
        // Statistics per time step
        for (int it = 0; it < nelemtime; it++) {

            for (int il = nlinks; il < ii; il++) {
                int its = il - nlinks;
                avetime[it] = avetime[it] + tempRain[its][it];
                if (maxtime[it] < tempRain[its][it]) {
                    maxtime[it] = tempRain[its][it];
                    distMaxtime[it] = DistOutlet[il];
                }

                if (tempRain[its][it] > 0.01) {
                    coveragetime[it] = coveragetime[it] + 1;
                }
                PCM[it] = PCM[it] + DistOutlet[il] * tempRain[its][it];
                BCM[it] = BCM[it] + DistOutlet[il];
            }

        }
        //System.out.println("nlinks initial  " + nlinks + "nlinks final " + ii + "difference" + (ii - nlinks));
        nlinks = ii;

        ruta.close();
        buffer.close();
    }

    public void readFile2(java.io.File InputFile, double Ti) throws java.io.IOException {

        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens = null;
        String linea = null, basura, nexttoken;
        int linejump = 2;
        ruta = new FileReader(InputFile);
        buffer = new BufferedReader(ruta);
        String data = "test"; // JUMP 1 LINE IN THE BEGINING
        data = buffer.readLine(); // JUMP 2 LINE IN THE BEGINING
        if(data==null) return;
        data = buffer.readLine(); // JUMP 2 LINE IN THE BEGINING
        String temp;

        String filename = InputFile.getName();
        String FileDir = InputFile.getParent();
        System.out.println("InputFile" + InputFile);


        int ii = nhydro;


        int yy = 0;
        //System.out.println(filename);
        if (filename.contains("ned_1")) {
            int i1 = filename.indexOf("_") + 1;
            int i2 = filename.indexOf("_", i1);
            int i3 = filename.indexOf("_", i1) + 1;
            int i4 = filename.indexOf("_", i3);
            int i5 = filename.indexOf("_", i3) + 1;
            int i6 = filename.indexOf("-", i1);

            // check if there is an E

            xxx[ii] = Integer.valueOf(filename.substring(i3, i4));
            yyy[ii] = Integer.valueOf(filename.substring(i5, i6));

        } else {
            int i1 = filename.indexOf("_") + 1;
            int i2 = filename.indexOf("_", i1);
            int i3 = filename.indexOf("_", i1) + 1;
            int i4 = filename.indexOf("-", i1);

            xxx[ii] = Integer.valueOf(filename.substring(i1, i2));
            yyy[ii] = Integer.valueOf(filename.substring(i3, i4));
        }




        LinknHyd[ii] = xxx[ii] + yyy[ii] * NC;
        int flag = 0;
        int i = 0;
        while (flag == 0) {   //System.out.println("i  " + i+"LinknHyd[ii]   " + LinknHyd[ii] + "Linkcode[i]   " +Linkcode[i]);
            if (Linkcode[i] == LinknHyd[ii]) {
                hydroorder[ii] = Linkorder[i];
                hydroarea[ii] = Linkarea[i];
                hydrolat[ii] = Linklat[i];
                hydrolong[ii] = Linklong[i];
                flag = 1;
            }
            i = i + 1;
            if (i == Linkcode.length && flag == 0) {
                hydroorder[ii] = -9;
                hydroarea[ii] = -9.9f;
                hydrolat[ii] = -9.9f;
                hydrolong[ii] = -9.9f;
                flag = 1;
                break;
            }
        }
        int ll=0;
        //System.out.println("LinknHyd[ii]   " + LinknHyd[ii] + "xxx   " + xxx[ii] + "yyy    " + yyy[ii]);
        int j = 0;
        data = buffer.readLine();
        //READ Horton order
        if (flag == 0) {
           // System.out.println("Didnt find link   " + LinknHyd[ii]);
        }
        if (flag == 1) {
            while (data != null) {
                ll=ll+1;
 //System.out.println(data + "   " + ll);
                double number1;
                double number2;
                tokens = new StringTokenizer(data, ",");
                if(tokens.hasMoreTokens()){
                temp = new String(tokens.nextToken());
                double TTemp;
                
                if (temp.indexOf("E") < 1) {
                    TTemp = Double.valueOf(temp);
                    TTemp = TTemp - (Ti);
                } else {
                    number1 = Double.valueOf(temp.substring(0, temp.indexOf("E")));
                    //System.out.println(ii + "number1" +number1+ "j" + j);
                    number2 = Double.valueOf(temp.substring((temp.indexOf("E") + 1), temp.length()));
                    //System.out.println(ii + "number2" +number2+ "j" + j);
                    TTemp = number1 * Math.pow(10, number2);
                    TTemp = (TTemp - Ti);
                }
//System.out.println(ii + "temp" +temp+ "j" + j);
                HTime[ii][j] = (float) TTemp;

                // check if there is an E
                if(tokens.hasMoreTokens()){
                temp = new String(tokens.nextToken());
               // System.out.println(HTime[ii][j] + ii + "  temp" +temp+ "   j" + j);
                if (temp.indexOf("E") < 1) {
                    hydrographs[ii][j] = Float.valueOf(temp);
                } else {
                    number1 = Double.valueOf(temp.substring(0, temp.indexOf("E")));
                    number2 = Double.valueOf(temp.substring((temp.indexOf("E") + 1), temp.length()));
                    hydrographs[ii][j] = (float) (number1 * Math.pow(10, number2));
                }

                for (int cc = 0; cc <= linejump; cc++) {
                    data = buffer.readLine();
                }
                
                j = j + 1;
                }
                }
                data = buffer.readLine();
                if (j >= ntimestep) {
                    data = null;
                }
//System.out.println(ii + "temp" +temp+ "j" + j);

            }
            nhydro = nhydro + 1;
            ntime = Math.max(ntime, j);
            ruta.close();
            buffer.close();

            String FilenameStorage = filename.replace("Outlet", "Storage");
            ruta = new FileReader(FileDir + "/" + FilenameStorage);
            buffer = new BufferedReader(ruta);

            data = buffer.readLine(); // JUMP 2 LINE IN THE BEGINING
            data = buffer.readLine(); // JUMP 2 LINE IN THE BEGINING
            data = buffer.readLine(); // DATA LINE
            j = 0;

            while (data != null) {

                double number1;
                double number2;
               //System.out.println(data);
                tokens = new StringTokenizer(data, ","); // jump time stamp
                if(tokens.hasMoreTokens()){
                temp = new String(tokens.nextToken()); // jump time stamp
                if(tokens.hasMoreTokens()){
                temp = new String(tokens.nextToken());

                if (temp.indexOf("E") < 1) {
                    Storage0[ii][j] = Float.valueOf(temp);
                } else {
                    number1 = Double.valueOf(temp.substring(0, temp.indexOf("E")));
                    number2 = Double.valueOf(temp.substring((temp.indexOf("E") + 1), temp.length()));
                    Storage0[ii][j] = (float) (number1 * Math.pow(10, number2));
                }
                if(tokens.hasMoreTokens()){
                temp = new String(tokens.nextToken());
                //System.out.println(temp);

                if (temp.indexOf("E") < 1) {
                    Storage1[ii][j] = Float.valueOf(temp);
                } else {
                    number1 = Double.valueOf(temp.substring(0, temp.indexOf("E")));
                    number2 = Double.valueOf(temp.substring((temp.indexOf("E") + 1), temp.length()));
                    Storage1[ii][j] = (float) (number1 * Math.pow(10, number2));
                }
                if(tokens.hasMoreTokens()){
                temp = new String(tokens.nextToken());

                if (temp.indexOf("E") < 1) {
                    Storage2[ii][j] = Float.valueOf(temp);
                } else {
                    number1 = Double.valueOf(temp.substring(0, temp.indexOf("E")));
                    number2 = Double.valueOf(temp.substring((temp.indexOf("E") + 1), temp.length()));
                    Storage2[ii][j] = (float) (number1 * Math.pow(10, number2));
                }

               
                j = j + 1;
}}}}
                 for (int cc = 0; cc <= linejump; cc++) {
                    data = buffer.readLine();
                }
                data = buffer.readLine();

                if (j >= ntimestep) {
                    data = null;
                }
            }
        }




    }

//      public void GenerateStatisticsRain() throws java.io.IOException{
//
//          java.io.FileReader ruta;
//          java.io.BufferedReader buffer;
//
//          java.util.StringTokenizer tokens;
//          String linea=null, basura, nexttoken;
//          //System.out.println("InputFile" + InputFile);
//          ruta = new FileReader(InputFile);
//          buffer=new BufferedReader(ruta);
//          String data = buffer.readLine();
//          for (int i=0;i<6;i++) data = buffer.readLine(); // JUMP 1+5 LINES IN THE BEGINING
//          data = buffer.readLine(); // Read x
//
//          //System.out.println("data" + data);
//          tokens = new StringTokenizer(data,",");
//          String temp=new String(tokens.nextToken()); // header
//          //System.out.println("temp   " + temp);
//
//          float[] xx=new float[20000];
//          float[] yy=new float[20000];
//
//          int irel=0;
//          while (tokens.hasMoreTokens())
//            {
//            xx[irel]=new Float(tokens.nextToken());
//            //IDJ[ii]="1";
//            irel=irel+1;
//            }
//            data = buffer.readLine();
//           //READ Horton order
//           tokens = new StringTokenizer(data,",");
//           temp=new String(tokens.nextToken());
//            irel=0;
//           while (tokens.hasMoreTokens())
//             {
//             yy[irel]=new Float(tokens.nextToken());
//             irel=irel+1;
//             }
//           int ii=nlS;
//           for (int j=0;j<xx.length;j++)
//           {Linkn[ii] = xx[j]+yy[j]*3848;
//           ii=ii+1;
//           }
//           data = buffer.readLine(); // jump distance to outlet
//           data = buffer.readLine(); // meanRainfall
//
//           tokens = new StringTokenizer(data,",");
//           temp=new String(tokens.nextToken());
//           ii=nlS;
//           while (tokens.hasMoreTokens())
//             {
//             meanRainS[ii]=new Float(tokens.nextToken());
//             ii=ii+1;
//             }
//
//           data = buffer.readLine(); // accumRainfall
//           tokens = new StringTokenizer(data,",");
//           temp=new String(tokens.nextToken());
//           ii=nlS;
//           while (tokens.hasMoreTokens())
//             {
//             accumRainS[ii]=new Float(tokens.nextToken());
//             ii=ii+1;
//             }
//
//           data = buffer.readLine(); // maxRainfall
//           tokens = new StringTokenizer(data,",");
//           temp=new String(tokens.nextToken());
//           ii=nlS;
//           while (tokens.hasMoreTokens())
//             {
//            maxRainS[ii]=new Float(tokens.nextToken());
//             ii=ii+1;
//             }
//
//           data = buffer.readLine(); // maxRainfall
//           tokens = new StringTokenizer(data,",");
//           temp=new String(tokens.nextToken());
//           ii=nlS;
//           while (tokens.hasMoreTokens())
//             {
//            float ttemp=new Float(tokens.nextToken());
//            TmaxRainS[ii]=ttemp-(float)Ti;
//            ii=ii+1;
//             }
//
//
//           data = buffer.readLine(); // CoverageS
//           tokens = new StringTokenizer(data,",");
//           temp=new String(tokens.nextToken());
//           ii=nlS;
//           while (tokens.hasMoreTokens())
//             {
//            CoverageS[ii]=new Float(tokens.nextToken());
//            ii=ii+1;
//             }
//
//          nlS=ii;
//
//          String filename=InputFile.getName();
//          int i1=filename.indexOf("_")+1;
//          int i2=filename.indexOf("_",i1)-1;
//          int i3=filename.indexOf("_",i1)+1;
//          int i4=filename.indexOf("-",i1)-1;
//
//
//          xxxS[nert] = Integer.valueOf(filename.substring(i1,i2));
//          yyyS[nert] = Integer.valueOf(filename.substring(i3,i4));
//
//          LinknRain[nert]=xxxS[nert]+yyyS[nert]*3848;
//
//
//          for (int i=0;i<3;i++)   data = buffer.readLine();
//           data = buffer.readLine(); // read first line of
//          //READ Horton order
//           int it=0;
//           System.out.print("nert" + nert +"\n");
//           while (data != null)
//           {  //System.out.print("it" + it);
//               tokens = new StringTokenizer(data,",");
//               float tt=new Float(tokens.nextToken());
//               RTime[nert][it]=tt-(float)Ti;
//               Coverage[nert][it]=new Float(tokens.nextToken());
//               AvePrec[nert][it]=new Float(tokens.nextToken());
//               Stddev[nert][it]=new Float(tokens.nextToken());
//               max[nert][it]=new Float(tokens.nextToken());
//               accum[nert][it]=new Float(tokens.nextToken());
//               PCM[nert][it]=new Float(tokens.nextToken());
//               BCM[nert][it]=new Float(tokens.nextToken());
//               data = buffer.readLine(); // read first line of
//               it=it+1;
//           }
//           nelemtime=it;
//           nert=nert+1;
//
//    }
//
    public static int checkfile(java.io.File OutputFile) throws java.io.IOException {

        String FileName = OutputFile.getName();

        int flag = FileName.indexOf("Outlet");
        if (flag != -1) {
            flag = 1;
        }
        if (flag == -1) {
            flag = FileName.indexOf("complete");
            if (flag != -1) {
                flag = 2;
            }
        }
        if (flag == -1) {
            flag = FileName.indexOf("sumary");
            if (flag != -1) {
                flag = 3;
            }
        }
        if (flag == -1) {
            flag = FileName.indexOf("rainfall");
            if (flag != -1) {
                flag = 4;
            }
        }
        if (flag == -1) {
            flag = FileName.indexOf("Prec");
            if (flag != -1) {
                flag = 5;
            }
        }
        if (flag == -1) {
            flag = FileName.indexOf("hydrograph");
            if (flag != -1) {
                flag = 3;
            }
        }
        if (flag == -1) {
            flag = FileName.indexOf("sumrainT");
            if (flag != -1) {
                flag = 3;
            }
        }
        if (flag == -1) {
            flag = FileName.indexOf("sumrainS");
            if (flag != -1) {
                flag = 3;
            }
        }

        if (flag == -1) {
            flag = FileName.indexOf("Storage");
            if (flag != -1) {
                flag = 6;
            }
        }

        if (flag == -1) {
            flag = FileName.indexOf("Tile_");
            if (flag != -1) {
                flag = 7;
            }
        }


        return flag;
    }

    public static ArrayList<File> getFileList(File dir) throws FileNotFoundException {
        ArrayList<File> result = new ArrayList<File>();
        File[] files = dir.listFiles();
        List<File> tempfiles = Arrays.asList(files);
        for (File file : files) {
            result.add(file);
        }
        return result;
    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {


        //mainparam(args);
        //mainRES(args);
        //mainClearCleek(args);
        mainHelium(args);
    }

  

    public static void mainHelium(String args[]) {



        int nc = 3848; //ClearCreek -90 metere USGS

        java.util.Hashtable routingParams = new java.util.Hashtable();
//String[] AllSimName = {"90DEMUSGS"};
        String[] AllSimName = {"90DEMUSGS"};

        String[] AllRain = {"3Turkey_2"};

        int nsim = AllSimName.length;
        int nbas = AllRain.length;

        for (int i = 0; i < nsim; i++) {
            for (int ib = 0; ib < nbas; ib++) {

                System.out.println("Running BASIN " + AllSimName[i]);
                System.out.println("Running BASIN " + AllRain[ib]);

                String SimName = AllSimName[i];
                String BasinName = AllRain[ib];
                File LinkAnalysisFile = new File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/linksAnalyses/linksInfo2885_690.csv"); // Iowa city
                LinkAnalysisFile = new File(defineLinkFile(BasinName,SimName));
                System.out.println( LinkAnalysisFile.getAbsoluteFile());

                File outfolder = new File("/scratch/results_cuencas/MultipleYears/resultsall/" +BasinName + "/" + SimName + "/");
//File outfolder = new File("/Users/rmantill/luciana/Parallel/ALL_MODELS4/results/");
                outfolder.mkdirs();


                final Collection<File> all = new ArrayList<File>();
                //addFilesRecursively(new File("/scratch/results_cuencas/EGU/"+BasinName + "/" + SimName + "/"), all);

               addFilesRecursively(new File("/scratch/results_cuencas/MultipleYears/"+BasinName + "/" + SimName + "/0.0/RoutT_2/"), all);
               //scratch/results_cuencas/EGU/3CedarRapids/90DEMUSGS/0.0/RoutT_2/HillT_0/HillVelT_4/VS0.0010/RC0.9/UnsO0.5/PH0.0/SCS_-9.0/vh_-9.0/hh_0.0
               //addFilesRecursively(new File("/Users/rmantill/luciana/Parallel/Helium_version/"+ BasinName + "/" + SimName + "/"), all);
               //addFilesRecursively(new File("//Users/rmantill/luciana/Parallel/Res_Jan_2011_M5_3/3ClearCreek2009MarchAdvdisc4/"), all);
                
                String[] ListDir = new String[all.size()];
                System.out.println("all size= " + all.size() + "\n");
                int t = 0;
                for (Iterator iterator = all.iterator(); iterator.hasNext();) {
                    File file = (File) iterator.next();
                    if (file.isDirectory()) {
                        ListDir[t] = file.toString();
                        t = t + 1;
                    }

                }

System.out.println("n directories" +t);
                for (int idd = 0; idd < t; idd++) {

System.out.println("directories" +idd + "ListDir[idd]" +ListDir[idd]);

                    File folder = new java.io.File(ListDir[idd]);

                    if (folder.exists()) {
                        FilenameFilter only = new OnlyExt("csv");
                        String[] folStrfilter = folder.list(only);
                        if (folStrfilter.length > 0) {
                            System.out.println("File n= " + folStrfilter.length + "\n");
                            String[] folStr = folder.list();
                            int Str = folStr.length;
                            System.out.println(idd + "    " + ListDir[idd] + "File n= " + Str + "\n");
                    
                        //try {
                            String str = ListDir[idd].replace( '/', '_' );
                            //String str2=str.substring(str.indexOf("Rout"));
                           System.out.println(str);
                            String str2=str.substring(str.indexOf("RoutT")-4);
                           System.out.println("IDENTIFIER" +str2 + "\n");
                          try{
                           new ParalelVersionReader(folder, LinkAnalysisFile, str2, outfolder);
                           } catch(java.io.IOException IOE){


                        //} catch (java.io.IOException IOE) {
                            System.out.print(IOE);
                            //System.exit(0);
                        }
                    
                        }
                    

                    }
                }



//                File folder = new java.io.File("/Users/rmantill/luciana/Parallel/ALL_MODELS4/" + BasinName + "/" + SimName + "/"
//                        + "RoutT_2/"
//                        + "/HillT_0/"
//                        + "/HillVelT_0"
//                        + "/RR_1");
//////                if (folder.exists()) {
//////                    String[] folStr = folder.list();
//////                    int Str = folStr.length;
//////                    if (Str > 0) {
//////                        outfolder = new File("/Users/rmantill/luciana/Parallel/ALL_MODELS4/" + "results/" + BasinName + "/" + SimName + "/");
//////                        outfolder.mkdirs();
//////
//////                        try {
//////                            String str = "1RoutT_2HillT_0HillVelT_0_RR_1";
//////                            new ParalelVersionReader(folder, LinkAnalysisFile, str, outfolder);
//////                        } catch (java.io.IOException IOE) {
//////                            System.out.print(IOE);
//////                            System.exit(0);
//////                        }
//////                    }
//////                }

            }
        }
    }

    private static void addFilesRecursively(File file, Collection<File> all) {
        final File[] children = file.listFiles();
        if (children != null) {
            for (File child : children) {
                all.add(child);
                addFilesRecursively(child, all);
            }
        }
    }

    public static class OnlyExt implements FilenameFilter {

        String ext;

        public OnlyExt(String ext) {
            this.ext = "." + ext;
        }

        public boolean accept(File dir, String name) {
            return name.endsWith(ext);
        }
    }

     public static String defineLinkFile(String BasinName,String SimName){

      // DEFINE THE DEM and x and y
                int xOut = 2817;
                int yOut = 713; //90METERDEMClear Creek - coralville
                String[] OUTPUT = {"error","errorx","errory"};
                if (SimName.contains("ASTER")) {
                    OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM";
                    xOut = 1596;
                    yOut = 298;
                }
                if (SimName.equals("5DEMLIDAR")) {
                    xOut = 8052;
                    yOut = 497;
                    OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/5meters/5meterc1.metaDEM";
                }
                if (SimName.equals("10DEMLIDAR")) {
                    xOut = 4025;
                    yOut = 244;
                   OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/10meters/10meterc1.metaDEM";
                }
                if (SimName.equals("20DEMLIDAR")) {
                    xOut = 2013;
                    yOut = 122;
                    OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/20meters/20meterc1.metaDEM";
                }
                if (SimName.equals("30DEMLIDAR")) {
                    xOut = 1341;
                    yOut = 82;
                    OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/30meters/30meterc1.metaDEM";
                }
                if (SimName.equals("60DEMLIDAR")) {
                    xOut = 670;
                    yOut = 41;
                    OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/60meters/60meterc1.metaDEM";
                }
                if (SimName.equals("90DEMLIDAR")) {
                    xOut = 447;
                    yOut = 27;
                    OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM";
                }
                if (SimName.equals("90DEMUSGS")) {
                    xOut = 2817;
                    yOut = 713;
                    OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                if (SimName.equals("30DEMUSGS")) {
                    xOut = 1541;
                    yOut = 92;
                    OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM";
                }

                if (SimName.equals("10DEMUSGS")) {
                    xOut = 4624;
                    yOut = 278;
                    OUTPUT[0] = "/scratch/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM";
                }

                if (BasinName.indexOf("Cedar") >= 0) {
                    xOut = 2734;
                    yOut = 1069; //Cedar Rapids
                    OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }


                if (BasinName.indexOf("Iowa") >= 0) {
                    xOut = 2885;
                    yOut = 690;
                    OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }


                if (BasinName.indexOf("Turkey") >= 0) {

                    xOut = 3053;
                    yOut = 2123;
                    OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                if (BasinName.indexOf("Volga") >= 0) {
                    xOut = 3091;
                    yOut = 2004;
                    OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                 if (SimName.indexOf("120DEMUSGS") >= 0) {
                     OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs120m.metaDEM";
                     if (BasinName.indexOf("Clear") >= 0) {
                    xOut = 2113;
                    yOut = 535;
                    }
                     if (BasinName.indexOf("Iowa") >= 0) {
                    xOut = 2164;
                    yOut = 517;
                    }

                     if (BasinName.indexOf("Cedar") >= 0) {
                    xOut = 2050;
                    yOut = 802;
                    }
                }


            if (SimName.indexOf("180DEMUSGS") >= 0) {
                     OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs180m.metaDEM";
                     if (BasinName.indexOf("Clear") >= 0) {
                    xOut = 1409;
                    yOut = 356;
                    }
                       if (BasinName.indexOf("Cedar") >= 0) {
                    xOut = 1367;
                    yOut = 534;
                    }

                     if (BasinName.indexOf("Iowa") >= 0) {
                    xOut = 1443;
                    yOut = 345;
                    }

                    }

                if (BasinName.indexOf("Hoover") >= 0) {
                    xOut = 3113;
                    yOut = 705;
                    OUTPUT[0] = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM";
                }

                String NFile = "/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/linksAnalyses/linksInfo" +xOut+"_"+yOut+".csv";


                          System.out.println("NFile = " + NFile);

      return NFile;
           }
}
