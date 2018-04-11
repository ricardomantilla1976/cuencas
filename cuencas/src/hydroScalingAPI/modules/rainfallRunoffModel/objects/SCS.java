/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import flanagan.analysis.Stat;

/**
 *
 * @author lcunha
 */
public class SCS {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    private float[][] areasHillArray;
    int x;
    int y;
    int[][] magnitudes;
    java.io.File DemFile;
    java.io.File LandUseFile;
    java.io.File SoilDataFile;
    java.io.File SoilHydDataFile;
    java.io.File SoilSwa150File;
    java.io.File outputDirectory;

    public SCS(int xx, int yy, byte[][] direcc, int[][] magnitudesOR,
            hydroScalingAPI.io.MetaRaster md, java.io.File DemFileOR, java.io.File LandUseFileOR, java.io.File SoilDataFileOR, java.io.File SoilHydDataFileOR, java.io.File SoilSwa150FileOR, java.io.File outputDirectoryOR) throws java.io.IOException {

        matDir = direcc;
        metaDatos = md;

        x = xx;
        y = yy;

        DemFile = DemFileOR;
        magnitudes = magnitudesOR;
        LandUseFile = LandUseFileOR;
        SoilDataFile = SoilDataFileOR;
        SoilHydDataFile = SoilHydDataFileOR;
        SoilSwa150File = SoilSwa150FileOR;
        outputDirectory = outputDirectoryOR;
    }

    public void ExecuteSCS() throws java.io.IOException {

        System.out.println("Start to run SCS \n");
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDir, metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;

        SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DemFile, LandUseFile, SoilDataFile, SoilHydDataFile,SoilSwa150File, myCuenca, linksStructure, metaDatos, matDir, magnitudes,0);

        thisHillsInfo.setSCSManager(SCSObj);
         
        java.io.File theFileSum;
        theFileSum = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "Summary" + ".csv");

        System.out.println(theFileSum);

        java.io.FileOutputStream salidaSum = new java.io.FileOutputStream(theFileSum);
        java.io.BufferedOutputStream bufferoutSum = new java.io.BufferedOutputStream(salidaSum);
        java.io.OutputStreamWriter newfileSum = new java.io.OutputStreamWriter(bufferoutSum);


        //////////////////////////////////////
        System.out.println("Start to run Width function - -" );
        java.io.File theFile;
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "SCSall" + ".csv");

        System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        newfileSum.write("maxorder,averaLength,aveArea,aveSlope,"
                + "aveRelief,PorConcHill,PorConvHill,PorlinearHill,PorPlan,"
                + "aveManning,aveCN2,aveHydCond \n");//1
        int linkID = 0;
        int maxorder=0;double aveLength=0;double stdLength=0;double aveArea=0;double stdArea=0;
        double aveSlopeC=0;double aveSlopeH=0;double stdSlope=0;double aveRelief=0;double stdRelief=0;double aveManning=0;
        double aveHydCond=0; double aveCN2=0; double aveSWA150=0;
        double aveSCS1=0;double aveSCS2=0;
        
        newfileSum.write(linksStructure.getBasinOrder()+",");
        
        /*Some particular links relevant to Wlanut Gulch, AZ
        linkID=linksStructure.getResSimID(822,964);
        //System.out.println("Little Hope Creek:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        System.out.println("Little Hope Creek:    "+linkID);
        linkID=linksStructure.getResSimID(95,815);
        System.out.println("Little Sugar Creek:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
         */

        int nLi = linksStructure.contactsArray.length;
        double[] Area_length = new double[linksStructure.contactsArray.length];
        System.out.println("Open the file ------- " + nLi);
       

        double RC = -9.9;
        double max_rel = 0;
        System.out.println("n column:    " + metaDatos.getNumCols());
        System.out.println("n line:    " + metaDatos.getNumRows());

        //  HEADER
        newfile.write("matriz_pin,");//1
        newfile.write("order,");//2
        newfile.write("length[m],");//3
        newfile.write("TotalLength,");//4
        newfile.write("mainchannellength[m],");
        newfile.write("HillArea[km2],");//5 // hillslope area (km2 - I think)
        newfile.write("UpsArea[km2],");       //6
        newfile.write("AvehillBasedSlope1,");//7a
        newfile.write("AvehillBasedSlope2,");//7b
        newfile.write("NetworkSlope,");//8
        newfile.write("HillRelief,");//9
        newfile.write("Hchannel,");//10
        newfile.write("HillRelief,");//11
        newfile.write("HillReliefMin,");//12
        newfile.write("HillReliefMax,");//13
        newfile.write("FormParam,");//14
        newfile.write("AverManning,");//15
        newfile.write("minHillBasedManning,");//16
        newfile.write("maxHillBasedManning,");//17
        newfile.write("getAverK_NRCS,");//18
        newfile.write("LandUseSCS,");//19
        newfile.write("Soil_CN1(i),");//20
        newfile.write("SCS_S1(i),");//21
        newfile.write("Soil_CN2(i),");//22
        newfile.write("SCS_S2(i),");//23
        newfile.write("Soil_CN3(i),");//22
        newfile.write("SCS_S3(i),");//23
        newfile.write("Soil_SWA(i),");//22
        newfile.write("dist[meters],");//24
        newfile.write("vh1_1,");//25
        newfile.write("vh1_10,");//26
        newfile.write("vh1_25,");//27
        newfile.write("vh2,");//28
        newfile.write("LC,");//29
        newfile.write("vh3,");//30
        newfile.write("vh4,");//31
        newfile.write("vh5,");//33

        newfile.write("vh4time,");//34
        newfile.write("format_flag,");//35
      newfile.write("term1,");//27
        newfile.write("term2,");//27
        newfile.write("term3,");//27
        newfile.write("term4,");//27
        newfile.write("HydConductivity,");//27

        newfile.write("\n");
        //
        double numconcave=0;
        double numconvex=0;
        double numplan=0;
        double numlinear=0;
        System.out.println( "basin area  "+thisNetworkGeom.basinArea()+ "NLi  "+nLi+"  ave area "+thisNetworkGeom.basinArea()/nLi);
        int nLi2=0;
        for (int i = 0; i < nLi; i++) {
            //if(thisNetworkGeom.linkOrder(i) > 1){
            int matrix_pin = i + 1;

            /*int yy=(int)Math.floor(linksStructure.contactsArray[i]/metaDatos.getNumCols());
            double temp=(double)metaDatos.getNumCols()*((linksStructure.contactsArray[i]/(double)metaDatos.getNumCols())-(double)yy);
            int xx=(int)(temp);
            newfile.write(xx+",");
            newfile.write(yy+",");*/
            newfile.write(matrix_pin + ","); //1
            newfile.write(thisNetworkGeom.linkOrder(i) + ","); //2
            newfile.write(thisNetworkGeom.Length(i) + ",");//3
            aveLength=aveLength+thisNetworkGeom.Length(i) ;
            newfile.write(thisNetworkGeom.upStreamTotalLength(i) + ",");//4
            newfile.write(thisNetworkGeom.mainChannelLength(i)+","); //5
            newfile.write(thisHillsInfo.Area(i) + ",");//5 // hillslope area (km2 - I think)
            //System.out.println(i + "  HArea  " + thisHillsInfo.Area(i));
            aveArea=aveArea+thisHillsInfo.Area(i);
            newfile.write(thisNetworkGeom.upStreamArea(i) + ",");       //6
            newfile.write(SCSObj.getavehillBasedSlopeMet1(i) + ",");//7a
            newfile.write(SCSObj.getavehillBasedSlopeMet2(i) + ",");//7b
            newfile.write(thisNetworkGeom.Slope(i) + ",");//8
            if(thisNetworkGeom.Slope(i)<=0){
              System.out.println( "Hill" +SCSObj.getHillRelief(i) + "    thisNetworkGeom.Length(i) "+ thisNetworkGeom.Length(i)) ;  
            }
            if(thisNetworkGeom.Slope(i)>0 || thisNetworkGeom.Slope(i)<5){
            aveSlopeC=aveSlopeC+thisNetworkGeom.Slope(i) ;
            aveSlopeH=aveSlopeH+thisHillsInfo.getHillslope(i);
            nLi2=nLi2+1;
            //System.out.println("SlopeC"+ thisNetworkGeom.Slope(i)+"  thisHillsInfo.getHillslope(i)  " + thisHillsInfo.getHillslope(i)+"   A(i)  " +thisHillsInfo.Area(i) + "L  " +thisNetworkGeom.Length(i));
            }
            double coef=2*thisNetworkGeom.Length(i)*thisHillsInfo.SCS_S2(i)*1e-6;
            
            //System.out.println(  "coefarea  " +coef + "Area(i)  " +thisHillsInfo.Area(i)*0.05);


            double Hchannel = thisNetworkGeom.Slope(i) * thisNetworkGeom.Length(i);
            double HillRelief = SCSObj.getHillRelief(i) - Hchannel;
            //System.out.println( "Hill" +SCSObj.getHillRelief(i) + "    Hchannel"+ Hchannel);
            double FormParam = (HillRelief) * 2 * thisNetworkGeom.Length(i) / (thisHillsInfo.Area(i) * 1000000);
            newfile.write(HillRelief + ",");//9
            newfile.write(Hchannel + ",");//10
            newfile.write(SCSObj.getHillRelief(i) + ",");//11
            aveRelief=aveRelief+SCSObj.getHillRelief(i);
             //System.out.println( "test1 ");
          
            if(SCSObj.getHillRelief(i)==0)
                 numplan=numplan+1;
            newfile.write(SCSObj.getHillReliefMin(i) + ",");//12
            newfile.write(SCSObj.getHillReliefMax(i) + ",");//13
            newfile.write(FormParam + ","); //14
             //System.out.println( "test2 ");
            newfile.write(SCSObj.getAverManning(i) + ","); //15
             aveManning=aveManning+SCSObj.getAverManning(i);
            newfile.write(SCSObj.getminHillBasedManning(i) + ","); //16
            newfile.write(SCSObj.getmaxHillBasedManning(i) + ","); //17
            // System.out.println( "test3 ");
             //System.out.println( "SCSObj.getAverK_NRCS(i) " +SCSObj.getAverK_NRCS(i) );
           //System.out.println( "thisHillsInfo.LandUseSCS(i) " +thisHillsInfo.LandUseSCS(i) );
             newfile.write(SCSObj.getAverK_NRCS(i) + ","); //18
            
             //System.out.println( "test4 ");
            newfile.write(thisHillsInfo.LandUseSCS(i) + ","); //19
             //System.out.println( "test5 ");
            newfile.write(thisHillsInfo.SCS_CN1(i) + ","); //20
             // System.out.println( "test6 ");
            newfile.write(thisHillsInfo.SCS_S1(i) + ","); //21
            newfile.write(thisHillsInfo.SCS_CN2(i) + ","); //22
            newfile.write(thisHillsInfo.SCS_S2(i) + ","); //23
            newfile.write(thisHillsInfo.SCS_CN3(i) + ","); //22
              //System.out.println( "test7 ");
            newfile.write(thisHillsInfo.SCS_S3(i) + ","); //23
            newfile.write(thisHillsInfo.SWA150(i) + ","); //23
            double dist = thisHillsInfo.Area(i) * 1000000 * 0.5 / (thisNetworkGeom.Length(i)); //(m)
            //double tim_run=dist/100; //hour
            newfile.write(dist + ",");  //24
            //System.out.println("SCS_S1(i)      " + thisHillsInfo.SCS_S1(i) +"  SCS_S2 (i)      " + thisHillsInfo.SCS_S2(i));
            //System.out.println("SWA150(i)      " + thisHillsInfo.SWA150(i) );
             aveCN2=aveCN2+thisHillsInfo.SCS_CN2(i) ;
            aveSCS2=aveSCS2+thisHillsInfo.SCS_S2(i) ;
            aveSCS1=aveSCS1+thisHillsInfo.SCS_S1(i) ;
            aveSWA150=aveSWA150+thisHillsInfo.SWA150(i) ;
      
            
            //System.out.println("1/SCSObj.getAverManning(i):    " + 1/SCSObj.getAverManning(i));
            //System.out.println("SCSObj.getavehillBasedSlope(i):    " + Math.pow(SCSObj.getavehillBasedSlopeMet1(i)/10000, 0.5));
            //System.out.println("Math.pow(1/ 1000, (2 / 3)):    " + Math.pow(1.0/ 1000.0, (2.0 / 3.0)));
            //System.out.println("Math.pow(1/ 1000, (2 / 3)):    " + Math.pow(25.0/ 1000.0, (2.0 / 3.0)));
           //System.out.println("Math.pow(1/ 1000, (2 / 3)):    " + Math.pow(50.0/ 1000.0, (2.0 / 3.0)));
            
            double vH1_1 = 0.6*(1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlopeMet1(i), 0.5) * Math.pow(1.0/ 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
            newfile.write(vH1_1 + ",");//25
            double vH1_10 = 0.6*(1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlopeMet1(i), 0.5) * Math.pow(10.0/ 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
            newfile.write(vH1_10 + ",");//26
            double vH1_25 =0.6*(1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlopeMet1(i), 0.5) * Math.pow(25.0/ 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
            newfile.write(vH1_25 + ",");//27
//System.out.println(thisHillsInfo.LandUseSCS(i) + "  mann  "+SCSObj.getAverManning(i) + "     "+vH1_1 +    "       vH1_10   " + vH1_10 + "    vH1_25   " + vH1_25);
           double VH2=-9;
           String LC=null;
                if (thisHillsInfo.LandUseSCS(i) == 0) {
                    VH2 = 500.0;
                    LC="water";
                } else if (thisHillsInfo.LandUseSCS(i)== 1) {
                    VH2 = 250.0;
                    LC="urbanArea";
                } else if (thisHillsInfo.LandUseSCS(i) == 2) {
                    VH2 = 100.0;
                    LC="baren soil";// baren soil
                } else if (thisHillsInfo.LandUseSCS(i) == 3) {
                    VH2 = 10.0;
                    LC="Forest";// Forest
                } else if (thisHillsInfo.LandUseSCS(i) == 4) {
                    VH2 = 100.0;
                    LC="Shrubland";// Shrubland
                } else if (thisHillsInfo.LandUseSCS(i) == 5) {
                    VH2 = 20.0;
                    LC="Non_natural_woody_Orchards";// Non-natural woody/Orchards
                } else if (thisHillsInfo.LandUseSCS(i) == 6) {
                    VH2 = 100.0;
                    LC="Grassland";// Grassland
                } else if (thisHillsInfo.LandUseSCS(i) == 7) {
                    VH2 = 20.0;
                    LC="RowCrops";// Row Crops
                } else if (thisHillsInfo.LandUseSCS(i) == 8) {
                    VH2 = 100.0;
                    LC="Pasture_Small_Grains";// Pasture/Small Grains
                } else if (thisHillsInfo.LandUseSCS(i) == 9) {
                    VH2 = 50.0;
                    LC="Wetland";

                }

            newfile.write(VH2 + ",");//28
            newfile.write(LC + ",");//29
            
            double vH3 = (SCSObj.getAverK_NRCS(i))  * Math.pow((SCSObj.getavehillBasedSlopeMet1(i)*100), 0.5)  * 0.3048;
            newfile.write(vH3 + ",");//30
            double vH4 = (SCSObj.getAverK_NRCS(i)) * Math.pow((SCSObj.getavehillBasedSlopeMet1(i)/100), 0.5)  * 0.3048*3600;
            newfile.write(vH4 + ",");//31
            double vH5 = (SCSObj.getAverK_NRCS(i))* Math.pow((SCSObj.getavehillBasedSlopeMet1(i)), 0.5) * 0.3048*3600; //(m/h)
            double tt4 = dist / vH4;
            newfile.write(vH5 + ",");//32
            newfile.write(tt4 + ",");//33

            double format;
            format = SCSObj.getHillReliefMin(i) + ((SCSObj.getHillReliefMax(i) - SCSObj.getHillReliefMin(i)) / 2);
            double format_flag = -9.9;
            if (format < SCSObj.getHillReliefAve(i)) {
                format_flag = 1;
                numconcave=numconcave+1;
            }
            if (format > SCSObj.getHillReliefAve(i)) {
                format_flag = -1;
                numconvex=numconvex+1;
            }
            if (format == SCSObj.getHillReliefAve(i)) {
                format_flag = 0;
                numlinear=numlinear+1;
            }
            
            newfile.write(format_flag + ","); //34
            //System.out.println("LC   " + LC + "   Slope"+ SCSObj.getavehillBasedSlope(i)); //33
            //System.out.println("Manning         - vH1_1=" + vH1_1 + ", vH1_2="+vH1_10 + ", vH1_25="+vH1_10); //33
            //System.out.println("Constant for LC - vH2=" + VH2); //33
            //System.out.println("Hillvel3        - vH3=" + vH3); //33
            //System.out.println("Hillvel4        - vH4=" + vH4); //33
            //System.out.println("Hillvel correct - vH5=" + vH5); //33
            
            newfile.write(SCSObj.getTerm(i,0)+",");//25
            newfile.write(SCSObj.getTerm(i,1)+",");//26
            newfile.write(SCSObj.getTerm(i,2)+",");//26
            newfile.write(SCSObj.getTerm(i,3)+",");//26
            newfile.write(thisHillsInfo.AveHydCond(i) +",");//26
             aveHydCond=aveHydCond+thisHillsInfo.AveHydCond(i) ;
            //System.out.println(" Hy Cond"+thisHillsInfo.AveHydCond(i)+",");//28
            double acum=SCSObj.getHillReliefPorc(i,0);
            newfile.write(acum+",");//29
            acum=acum+SCSObj.getHillReliefPorc(i,1);
            newfile.write(acum+",");//29
            acum=acum+SCSObj.getHillReliefPorc(i,2);
            newfile.write(acum+",");//29
            acum=acum+SCSObj.getHillReliefPorc(i,3);
            newfile.write(acum+",");//31
            acum=acum+SCSObj.getHillReliefPorc(i,4);
            newfile.write(acum+",");//32



            newfile.write("\n");


        }
        System.out.println("aveSlopeC  "+aveSlopeC+"  nLi  " +nLi);
        System.out.println("aveSlopeC  "+aveSlopeC+"  nLi2  " +nLi2);
        aveLength=aveLength/nLi;
        aveArea=aveArea/nLi;
        aveSlopeC=aveSlopeC/nLi2;
                
        aveSlopeH=aveSlopeH/nLi;
        aveRelief=aveRelief/nLi;
        aveManning=aveManning/nLi;
        aveCN2=aveCN2/nLi;
        aveSCS2=aveSCS2/nLi;
        aveSCS1=aveSCS1/nLi;
        aveSWA150=aveSWA150/nLi;
        aveHydCond=aveHydCond/nLi;
        numconcave=numconcave*100/nLi;
        numconvex=numconvex*100/nLi;
        numlinear=numlinear*100/nLi;
        numplan=numplan*100/nLi;
        //System.out.println("aveArea " + aveArea + " aveSCS1 " + aveSCS1+ " aveSCS2 " + aveSCS2+ " aveSWA150 " + aveSWA150);
        //System.exit(0);
         newfileSum.write(aveLength+","+aveArea+","+aveSlopeC+","+aveSlopeH+","+
                aveRelief+","+numconcave+","+numconvex+","+numlinear+","+numplan+","+
                aveManning+","+aveCN2+","+aveHydCond +"\n");//1
         
           System.out.println("Termina escritura de LandUse \n");

        newfile.close();
        bufferout.close();
        newfileSum.close();
        bufferoutSum.close();

        System.out.println("Start to run Width function");

        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "SCScompleteO" + ".csv");

        System.out.println(theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);



        nLi = linksStructure.completeStreamLinksArray.length;
        Area_length = new double[linksStructure.completeStreamLinksArray.length];
        System.out.println("Open the file");

           newfile.write("matriz_pin,");//1
        newfile.write("order,");//2
        newfile.write("length[m],");//3
        newfile.write("TotalLength,");//4
        newfile.write("mainchannellength[m],");
        newfile.write("HillArea[km2],");//5 // hillslope area (km2 - I think)
        newfile.write("UpsArea[km2],");       //6
        newfile.write("AvehillBasedSlope,");//7
        newfile.write("Slope,");//8
        newfile.write("HillRelief,");//9
        newfile.write("Hchannel,");//10
        newfile.write("HillRelief,");//11
        newfile.write("HillReliefMin,");//12
        newfile.write("HillReliefMax,");//13
        newfile.write("FormParam,");//14
        newfile.write("AverManning,");//15
        newfile.write("minHillBasedManning,");//16
        newfile.write("maxHillBasedManning,");//17
        newfile.write("getAverK_NRCS,");//18
        newfile.write("LandUseSCS,");//19
        newfile.write("Soil_CN2(i),");//20
        newfile.write("SCS_S2(i),");//21
        newfile.write("Soil_CN1(i),");//22
        newfile.write("SCS_S1(i),");//23
        newfile.write("dist[meters],");//24
        newfile.write("vh1_1,");//25
        newfile.write("vh1_10,");//26
        newfile.write("vh1_25,");//27
        newfile.write("vh2,");//28
        newfile.write("LC,");//29
        newfile.write("vh3,");//30
        newfile.write("vh4,");//31

        newfile.write("vh4time,");//32
        newfile.write("format_flag,");//33
        newfile.write("\n");
        //
        for (int ii = 0; ii < nLi; ii++) {
            //if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
            int matrix_pin = linksStructure.completeStreamLinksArray[ii] + 1;
            int i = linksStructure.completeStreamLinksArray[ii];
            /*int yy=(int)Math.floor(linksStructure.contactsArray[i]/metaDatos.getNumCols());
            double temp=(double)metaDatos.getNumCols()*((linksStructure.contactsArray[i]/(double)metaDatos.getNumCols())-(double)yy);
            int xx=(int)(temp);
            newfile.write(xx+",");
            newfile.write(yy+",");*/
            newfile.write(matrix_pin + ","); //1
            newfile.write(thisNetworkGeom.linkOrder(i) + ","); //2
            newfile.write(thisNetworkGeom.Length(i) + ",");//3
            newfile.write(thisNetworkGeom.upStreamTotalLength(i) + ",");//4
            newfile.write(thisNetworkGeom.mainChannelLength(i)+","); //5
            newfile.write(thisHillsInfo.Area(i) + ",");//5 // hillslope area (km2 - I think)
            newfile.write(thisNetworkGeom.upStreamArea(i) + ",");       //6
            newfile.write(SCSObj.getavehillBasedSlopeMet1(i) + ",");//7
            newfile.write(thisNetworkGeom.Slope(i) + ",");//8



            double Hchannel = thisNetworkGeom.Slope(i) * thisNetworkGeom.Length(i);
            double HillRelief = SCSObj.getHillRelief(i) - Hchannel;
            double FormParam = (HillRelief) * 2 * thisNetworkGeom.Length(i) / (thisHillsInfo.Area(i) * 1000000);
            newfile.write(HillRelief + ",");//9
            newfile.write(Hchannel + ",");//10
            newfile.write(SCSObj.getHillRelief(i) + ",");//11
            newfile.write(SCSObj.getHillReliefMin(i) + ",");//12
            newfile.write(SCSObj.getHillReliefMax(i) + ",");//13
            newfile.write(FormParam + ","); //14
            newfile.write(SCSObj.getAverManning(i) + ","); //15
            newfile.write(SCSObj.getminHillBasedManning(i) + ","); //16
            newfile.write(SCSObj.getmaxHillBasedManning(i) + ","); //17
            newfile.write(SCSObj.getAverK_NRCS(i) + ","); //18
            newfile.write(thisHillsInfo.LandUseSCS(i) + ","); //19
            newfile.write(thisHillsInfo.SCS_CN2(i) + ","); //20
            newfile.write(thisHillsInfo.SCS_S2(i) + ","); //21
            newfile.write(thisHillsInfo.SCS_CN1(i) + ","); //22
            newfile.write(thisHillsInfo.SCS_S1(i) + ","); //23
            double dist = thisHillsInfo.Area(i) * 1000000 * 0.5 / (thisNetworkGeom.Length(i)); //(m)
            //double tim_run=dist/100; //hour
            newfile.write(dist + ",");  //24
            //System.out.println("thisHillsInfo.getHillslope(i)" +thisHillsInfo.getHillslope(i)+"hydCond:    " + thisHillsInfo.MinHydCond(i) +"  maxhydCond:    " +thisHillsInfo.MaxHydCond(i)+"  slope:    " +thisHillsInfo.MaxHydCond(i));
            
            double vH1_1 = (1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlopeMet1(i), 0.5) * Math.pow(1 / 1000, (2 / 3)) * 3600;
            newfile.write(vH1_1 + ",");//25
            double vH1_10 = (1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlopeMet1(i), 0.5) * Math.pow(10 / 1000, (2 / 3)) * 3600;
            newfile.write(vH1_10 + ",");//26
            double vH1_25 = (1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlopeMet1(i), 0.5) * Math.pow(10 / 1000, (2 / 3)) * 3600;
            newfile.write(vH1_25 + ",");//27

           double VH2=-9;
           String LC=null;
                if (thisHillsInfo.LandUseSCS(i) == 0) {
                    VH2 = 500.0;
                    LC="water";
                } else if (thisHillsInfo.LandUseSCS(i)== 1) {
                    VH2 = 250.0;
                    LC="urbanArea";
                } else if (thisHillsInfo.LandUseSCS(i) == 2) {
                    VH2 = 100.0;
                    LC="baren soil";// baren soil
                } else if (thisHillsInfo.LandUseSCS(i) == 3) {
                    VH2 = 10.0;
                    LC="Forest";// Forest
                } else if (thisHillsInfo.LandUseSCS(i) == 4) {
                    VH2 = 100.0;
                    LC="Shrubland";// Shrubland
                } else if (thisHillsInfo.LandUseSCS(i) == 5) {
                    VH2 = 20.0;
                    LC="Non_natural_woody_Orchards";// Non-natural woody/Orchards
                } else if (thisHillsInfo.LandUseSCS(i) == 6) {
                    VH2 = 100.0;
                    LC="Grassland";// Grassland
                } else if (thisHillsInfo.LandUseSCS(i) == 7) {
                    VH2 = 20.0;
                    LC="RowCrops";// Row Crops
                } else if (thisHillsInfo.LandUseSCS(i) == 8) {
                    VH2 = 100.0;
                    LC="Pasture_Small_Grains";// Pasture/Small Grains
                } else if (thisHillsInfo.LandUseSCS(i) == 9) {
                    VH2 = 50.0;
                    LC="Wetland";

                }

            newfile.write(VH2 + ",");//28
            newfile.write(LC + ",");//29

            double vH3 = (SCSObj.getAverK_NRCS(i))  * Math.pow((SCSObj.getavehillBasedSlopeMet1(i)*100), 0.5)  * 0.3048;
            newfile.write(vH3 + ",");//30
            double vH4 = (SCSObj.getAverK_NRCS(i)) * Math.pow((SCSObj.getavehillBasedSlopeMet1(i)/100), 0.5)  * 0.3048*3600;
            newfile.write(vH4 + ",");//31

            double tt4 = dist / vH4;
            newfile.write(tt4 + ",");//32

            double format;
            format = SCSObj.getHillReliefMin(i) + ((SCSObj.getHillReliefMax(i) - SCSObj.getHillReliefMin(i)) / 2);
            double format_flag = -9.9;
            if (format < SCSObj.getHillReliefAve(i)) {
                format_flag = 1;
            }
            if (format > SCSObj.getHillReliefAve(i)) {
                format_flag = -1;
            }
            if (format == SCSObj.getHillReliefAve(i)) {
                format_flag = 0;
            }
            newfile.write(format_flag + ","); //33
            //newfile.write(SCSObj.getTerm(i,0)+",");//25
            //newfile.write(SCSObj.getTerm(i,1)+",");//26
            //newfile.write(SCSObj.getTerm(i,2)+",");//26
            //newfile.write(SCSObj.getTerm(i,3)+",");//26
            double h=0.2;
            double dh_da2=1000/((thisHillsInfo.Area(i)*1e6/(thisNetworkGeom.Length(i)*SCSObj.getHillRelief(i)))*(SCSObj.getTerm(i,1)+2*SCSObj.getTerm(i,2)*h+3*SCSObj.getTerm(i,3)*h*h));
            double ai2=(thisHillsInfo.Area(i)*1e6/SCSObj.getHillRelief(i))*(SCSObj.getTerm(i,0)+SCSObj.getTerm(i,1)*h+SCSObj.getTerm(i,2)*h*h+SCSObj.getTerm(i,3)*h*h*h);
            double dh_dw2=thisNetworkGeom.Length(i)*2/(ai2);
            h=0.4;
            double dh_da4=1000/((thisHillsInfo.Area(i)*1e6/(thisNetworkGeom.Length(i)*SCSObj.getHillRelief(i)))*(SCSObj.getTerm(i,1)+2*SCSObj.getTerm(i,2)*h+3*SCSObj.getTerm(i,3)*h*h));
                    double ai4=(thisHillsInfo.Area(i)*1e6/SCSObj.getHillRelief(i))*(SCSObj.getTerm(i,0)+SCSObj.getTerm(i,1)*h+SCSObj.getTerm(i,2)*h*h+SCSObj.getTerm(i,3)*h*h*h);
            double dh_dw4=thisNetworkGeom.Length(i)*2/(ai2);
   
            h=0.6;
            double dh_da6=1000/((thisHillsInfo.Area(i)*1e6/(thisNetworkGeom.Length(i)*SCSObj.getHillRelief(i)))*(SCSObj.getTerm(i,1)+2*SCSObj.getTerm(i,2)*h+3*SCSObj.getTerm(i,3)*h*h));
                    double ai6=(thisHillsInfo.Area(i)*1e6/SCSObj.getHillRelief(i))*(SCSObj.getTerm(i,0)+SCSObj.getTerm(i,1)*h+SCSObj.getTerm(i,2)*h*h+SCSObj.getTerm(i,3)*h*h*h);
            double dh_dw6=thisNetworkGeom.Length(i)*2/(ai2);
   
            h=0.8;
            double dh_da8=1000/((thisHillsInfo.Area(i)*1e6/(thisNetworkGeom.Length(i)*SCSObj.getHillRelief(i)))*(SCSObj.getTerm(i,1)+2*SCSObj.getTerm(i,2)*h+3*SCSObj.getTerm(i,3)*h*h));
                    double ai7=(thisHillsInfo.Area(i)*1e6/SCSObj.getHillRelief(i))*(SCSObj.getTerm(i,0)+SCSObj.getTerm(i,1)*h+SCSObj.getTerm(i,2)*h*h+SCSObj.getTerm(i,3)*h*h*h);
            double dh_dw7=thisNetworkGeom.Length(i)*2/(ai2);
   
            //System.out.println(" Area(i) " + thisHillsInfo.Area(i)+" Length(i) " + thisNetworkGeom.Length(i)+" HillRelief(i) " + SCSObj.getHillRelief(i));
            //System.out.println(" thisNetworkGeom.Slope(i) " + thisNetworkGeom.Slope(i));
            //System.out.println(" dh_da2 " + dh_da2+" dh_da4 " + dh_da4+" dh_da6 " + dh_da6+" dh_da8 " + dh_da8);
            //System.out.println(" dh_dw2 " + dh_da2+" dh_dw4 " + dh_da4+" dh_dw6 " + dh_da6+" dh_dw8 " + dh_da8);
            
            //newfile.write(SCSObj.getHillReliefPorc(i,0)+",");//28
            //newfile.write(SCSObj.getHillReliefPorc(i,1)+",");//29
            //newfile.write(SCSObj.getHillReliefPorc(i,2)+",");//30
            //newfile.write(SCSObj.getHillReliefPorc(i,3)+",");//31
            //newfile.write(SCSObj.getHillReliefPorc(i,4)+",");//32



            newfile.write("\n");


        }


        System.out.println("Termina escritura de LandUse \n");

        newfile.close();
        bufferout.close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            subMain(args);     //Case Whitewater
            //subMain1(args);     //Case CedarRapids
            //subMain2(args);     //Case  Rio Puerco
            //subMain3(args);     //Case  11070208
            //subMain4(args);     //Case  11140102
            //subMain5(args);     //Iowa River
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void subMain(String args[]) throws java.io.IOException {

        //String pathinput = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/";
   int Prun = 6;
        int writeorder = 5;
        String pathinput = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/Aster3xPr" +Prun+"/";
            
         
        
        java.io.File DEMFile = new java.io.File(pathinput + "asterdem3x.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(DEMFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "asterdem3x.dir"));

        String formatoOriginal = metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0, DEMFile.getPath().lastIndexOf(".")) + ".magn"));
        metaModif.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        int x = 3053;
        int y = 2123;
        
        // x = 3053;
        //    y = 2123;
        x=2012; //Delaware trenton
        y=458;//Delaware trenton
        
        String Dir = "null";
        String OutputDir = pathinput + "/summary/";
        String precname = "null";
        
         //String Dir = "C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Hydrology/Land_info/LandCover2001_cliped/";
        new java.io.File(OutputDir).mkdirs();
        

     
        String LandUse = "null";
         Dir =  "null";
       
        //Dir = "C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Hydrology/soil_type/CUENCAS/";
        precname = "null";
        String SoilData = "null";
String SoilHydData = "null";
String Soil150SWAData = "null";            
  
//  String precname = "n";
//        String Dir = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover//";
//         //String Dir = "C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Hydrology/Land_info/LandCover2001_cliped/";
//        new java.io.File(Dir + "/TURKEY/").mkdirs();
//        String OutputDir = Dir + "/TURKEY/";
//
//     
//        String LandUse = Dir + precname;
//         Dir =  "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/";
//       
//        //Dir = "C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Hydrology/soil_type/CUENCAS/";
//        precname = "soil_rec90.metaVHC";
//        String SoilData = Dir + precname;
//String SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
//String Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";            
// 
new SCS(x, y, matDirs, magnitudes, metaModif, DEMFile, new java.io.File(LandUse), new java.io.File(SoilData), new java.io.File(SoilHydData), new java.io.File(Soil150SWAData),new java.io.File(OutputDir)).ExecuteSCS();


    }

    public static void subMain1(String args[]) throws java.io.IOException {

        ///// DEM DATA /////
        int BasinFlag=1;
        String Dir = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/temp/Topography/Summary/";
        new java.io.File(Dir).mkdirs();
        String SimName = "90DEMUSGS";
        String BasinName = "3Garber";

        //int[] Res = {90, 60, 30, 20, 10, 5};


        //int[] Res = {-9};

        //int[] XX = {447, 670, 1341, 2013, 4025, 8052,-9};
        //int[] YY = {27, 41, 82, 122, 244, 497,-9};
        //int[] XX = {1341};
        //int[] YY = {122};
                String ResStr = BasinName + "/" +SimName+ "/" +BasinFlag +"/";
        
        String OutputDir = "D:/usr/Delaware_luciana/CUENCASDelaware/";
        
         
            new java.io.File(OutputDir).mkdirs();

            java.io.File DEMFile;
           
            String[] StringDEM = {"error", "error", "error"};
                    StringDEM = hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ParallelSimulationToFileHelium.defineDEMxy(BasinName, SimName);
                    System.out.println("StringDEM = " + StringDEM[0]);
                    System.out.println("x = " + StringDEM[1] + "    y" + StringDEM[2]);

                    DEMFile=new java.io.File(StringDEM[0]);
                    int x = Integer.parseInt(StringDEM[1]);//   .getInteger(StringDEM[1]);
                    int y = Integer.parseInt(StringDEM[2]);
            
            
     
   //  DEMFile=new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
   //    OutputDir= "/Users/rmantill/luciana/Parallel/Res_Jan_2011_M5_3/Basin_Info/TestBasin/";
     //  new java.io.File(OutputDir).mkdirs();
    //   x = 2858;
   //   y = 742;
      //x=2885;
      //y=690;

//
      

            hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(DEMFile);
            metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0, DEMFile.getPath().lastIndexOf(".")) + ".dir"));
            metaModif.setFormat("Byte");
            byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0, DEMFile.getPath().lastIndexOf(".")) + ".magn"));
            metaModif.setFormat("Integer");
            int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0, DEMFile.getPath().lastIndexOf(".")) + ".horton"));
            metaModif.setFormat("Byte");
            byte[][] horOrders = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

 //Clear Creek - coralville
         
   
//  x = 2734;
//            y = 1069; //Cedar Rapids
            String LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
             String SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            String SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            String Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
         
        if (BasinFlag == 0) {
            LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }

        if (BasinFlag == 1) {
            LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/glomod90.metaVHC";
            SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 2) {
            LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverBaseline_90_2.metaVHC";
            SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 3) {
            LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverRestoringPastureTo10percentForest_90_2.metaVHC";
            SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 4) {
            LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverBaseline_90_2.metaVHC";
            SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 25) {
            LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverTranfrom_25_90_2.metaVHC";
            SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 50) {
            LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverTranfrom_50_90_2.metaVHC";
            SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }


        if (BasinFlag == 75) {
            LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverTranfrom_75_90_2.metaVHC";
            SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydgroup90.metaVHC";
            SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        }

            new SCS(x, y, matDirs, magnitudes, metaModif, DEMFile, new java.io.File(LandUse), new java.io.File(SoilData), new java.io.File(SoilHydData), new java.io.File(Soil150SWAData),new java.io.File(OutputDir)).ExecuteSCS();
        

    }
    
    
}
