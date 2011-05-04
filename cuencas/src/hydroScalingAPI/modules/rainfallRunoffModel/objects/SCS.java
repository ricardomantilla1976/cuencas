/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

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
    java.io.File outputDirectory;

    public SCS(int xx, int yy, byte[][] direcc, int[][] magnitudesOR,
            hydroScalingAPI.io.MetaRaster md, java.io.File DemFileOR, java.io.File LandUseFileOR, java.io.File SoilDataFileOR, java.io.File outputDirectoryOR) throws java.io.IOException {

        matDir = direcc;
        metaDatos = md;

        x = xx;
        y = yy;

        DemFile = DemFileOR;
        magnitudes = magnitudesOR;
        LandUseFile = LandUseFileOR;
        SoilDataFile = SoilDataFileOR;
        outputDirectory = outputDirectoryOR;
    }

    public void ExecuteSCS() throws java.io.IOException {

        System.out.println("Start to run SCS \n");
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDir, metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;

        SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DemFile, LandUseFile, SoilDataFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes,0);

        thisHillsInfo.setSCSManager(SCSObj);


        //////////////////////////////////////
        System.out.println("Start to run Width function");
        java.io.File theFile;
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "SCSall" + ".csv");

        System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

        int linkID = 0;

        /*Some particular links relevant to Wlanut Gulch, AZ
        linkID=linksStructure.getResSimID(822,964);
        //System.out.println("Little Hope Creek:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
        System.out.println("Little Hope Creek:    "+linkID);
        linkID=linksStructure.getResSimID(95,815);
        System.out.println("Little Sugar Creek:    "+linkID+"  "+thisNetworkGeom.upStreamArea(linkID-1));
         */

        int nLi = linksStructure.contactsArray.length;
        double[] Area_length = new double[linksStructure.contactsArray.length];
        System.out.println("Open the file");

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
        newfile.write("vh5,");//33

        newfile.write("vh4time,");//34
        newfile.write("format_flag,");//35
      newfile.write("term1,");//27
        newfile.write("term2,");//27
        newfile.write("term3,");//27
        newfile.write("term4,");//27

        newfile.write("\n");
        //



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
            newfile.write(thisNetworkGeom.upStreamTotalLength(i) + ",");//4
            newfile.write(thisNetworkGeom.mainChannelLength(i)+","); //5
            newfile.write(thisHillsInfo.Area(i) + ",");//5 // hillslope area (km2 - I think)
            newfile.write(thisNetworkGeom.upStreamArea(i) + ",");       //6
            newfile.write(SCSObj.getavehillBasedSlope(i) + ",");//7
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
            System.out.println("hydCond:    " + thisHillsInfo.MinHydCond(i) +"  minhydCond:    " +thisHillsInfo.MinHydCond(i));

            double vH1_1 = (1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlope(i), 0.5) * Math.pow(1 / 1000, (2 / 3)) * 3600;
            newfile.write(vH1_1 + ",");//25
            double vH1_10 = (1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlope(i), 0.5) * Math.pow(10 / 1000, (2 / 3)) * 3600;
            newfile.write(vH1_10 + ",");//26
         double vH1_25 = (1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlope(i), 0.5) * Math.pow(25 / 1000, (2 / 3)) * 3600;
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
            
            double vH3 = (SCSObj.getAverK_NRCS(i))  * Math.pow((SCSObj.getavehillBasedSlope(i)*100), 0.5)  * 0.3048;
            newfile.write(vH3 + ",");//30
            double vH4 = (SCSObj.getAverK_NRCS(i)) * Math.pow((SCSObj.getavehillBasedSlope(i)/100), 0.5)  * 0.3048*3600;
            newfile.write(vH4 + ",");//31
            double vH5 = (SCSObj.getAverK_NRCS(i))* Math.pow((SCSObj.getavehillBasedSlope(i)), 0.5) * 0.3048*3600; //(m/h)
            double tt4 = dist / vH4;
            newfile.write(vH5 + ",");//32
            newfile.write(tt4 + ",");//33

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
            newfile.write(format_flag + ","); //34
            System.out.println("LC   " + LC + "   Slope"+ SCSObj.getavehillBasedSlope(i)); //33
            System.out.println("Manning         - vH1_1=" + vH1_1 + ", vH1_2="+vH1_10 + ", vH1_25="+vH1_10); //33
            System.out.println("Constant for LC - vH2=" + VH2); //33
            System.out.println("Hillvel3        - vH3=" + vH3); //33
            System.out.println("Hillvel4        - vH4=" + vH4); //33
            System.out.println("Hillvel correct - vH5=" + vH5); //33
            
            newfile.write(SCSObj.getTerm(i,0)+",");//25
            newfile.write(SCSObj.getTerm(i,1)+",");//26
            newfile.write(SCSObj.getTerm(i,2)+",");//26
            newfile.write(SCSObj.getTerm(i,3)+",");//26

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
            newfile.write(SCSObj.getavehillBasedSlope(i) + ",");//7
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
            System.out.println("hydCond:    " + thisHillsInfo.MinHydCond(i) +"  minhydCond:    " +thisHillsInfo.MinHydCond(i));

            double vH1_1 = (1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlope(i), 0.5) * Math.pow(1 / 1000, (2 / 3)) * 3600;
            newfile.write(vH1_1 + ",");//25
            double vH1_10 = (1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlope(i), 0.5) * Math.pow(10 / 1000, (2 / 3)) * 3600;
            newfile.write(vH1_10 + ",");//26
         double vH1_25 = (1 / SCSObj.getAverManning(i)) * Math.pow(SCSObj.getavehillBasedSlope(i), 0.5) * Math.pow(10 / 1000, (2 / 3)) * 3600;
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

            double vH3 = (SCSObj.getAverK_NRCS(i))  * Math.pow((SCSObj.getavehillBasedSlope(i)*100), 0.5)  * 0.3048;
            newfile.write(vH3 + ",");//30
            double vH4 = (SCSObj.getAverK_NRCS(i)) * Math.pow((SCSObj.getavehillBasedSlope(i)/100), 0.5)  * 0.3048*3600;
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
            //subMain(args);     //Case Whitewater
            subMain1(args);     //Case CedarRapids
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

        String pathinput = "C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/";
        java.io.File DEMFile = new java.io.File(pathinput + "Whitewaters" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(DEMFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "Whitewaters" + ".dir"));

        String formatoOriginal = metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0, DEMFile.getPath().lastIndexOf(".")) + ".magn"));
        metaModif.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        int x = 1063;
        int y = 496;

        String precname = "LC2001_cliped.metaVHC";
        String Dir = "C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Hydrology/Land_info/LandCover2001_cliped/";
        new java.io.File(Dir + "/test/").mkdirs();
        String OutputDir = Dir + "/test/";


        String LandUse = Dir + precname;
        Dir = "C:/Documents and Settings/lcunha/My Documents/CUENCAS/Whitewater_database/Rasters/Hydrology/soil_type/CUENCAS/";
        precname = "hyd_group.metaVHC";
        String SoilData = Dir + precname;

        new SCS(x, y, matDirs, magnitudes, metaModif, DEMFile, new java.io.File(LandUse), new java.io.File(SoilData), new java.io.File(OutputDir)).ExecuteSCS();


    }

    public static void subMain1(String args[]) throws java.io.IOException {

        ///// DEM DATA /////
        String Dir = "/Users/luciana-cunha/Documents/CuencasDataBases/temp/Topography/Summary/";
        new java.io.File(Dir).mkdirs();


        //int[] Res = {90, 60, 30, 20, 10, 5};


        int[] Res = {30};

        //int[] XX = {447, 670, 1341, 2013, 4025, 8052,-9};
        //int[] YY = {27, 41, 82, 122, 244, 497,-9};
        int[] XX = {1341};
        int[] YY = {122};
        int j = 0;
        for (int ir : Res) {

            String ResStr = ir + "meter";
            String OutputDir = Dir + "/" + ResStr + "/";
            if(ir==-9) OutputDir= Dir + "/90USGS/";
            if(ir==-10) OutputDir= Dir + "/ASTER/";
            if(ir==-11) OutputDir= Dir + "/30USGS/";
            if(ir==-12) OutputDir= Dir + "/10USGS/";
            if(ir==-13) OutputDir= Dir + "/30USGS/";
            if(ir==-13) OutputDir= Dir + "/30USGS/";
            new java.io.File(OutputDir).mkdirs();

            java.io.File DEMFile;
               DEMFile=new java.io.File("/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            if(ir==-9) DEMFile=new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            if(ir==-10) DEMFile=new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM");
            if(ir==-11) DEMFile=new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
            if(ir==-12) DEMFile=new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM");
            if(ir==-13) DEMFile=new java.io.File("/Users/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM");
           int x = XX[j];
            int y = YY[j]; //Clear Creek - coralville
     int BasinFlag=0;
   //  DEMFile=new java.io.File("/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
   //    OutputDir= "/Users/rmantill/luciana/Parallel/Res_Jan_2011_M5_3/Basin_Info/TestBasin/";
     //  new java.io.File(OutputDir).mkdirs();
    //   x = 2858;
   //   y = 742;
      //x=2885;
      //y=690;

//
            if(ir==-9) DEMFile=new java.io.File("/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            if(ir==-10) DEMFile=new java.io.File("/Users/luciana-cunha/Documents/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM");
            if(ir==-11) DEMFile=new java.io.File("/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
            if(ir==-12) DEMFile=new java.io.File("/Users/luciana-cunha/Documents/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM");
            if(ir==-13) DEMFile=new java.io.File("/Users/luciana-cunha/Documents/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM");



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
             x = 2372;
                            y = 791; //Cedar Rapids
            if(ir==-9) {x=2817;
            y=713;}
            if(ir==-10) {x=1596;
            y=298;}
            if(ir==-11) {x=8288;
            y=1050;}
            if(ir==-12) {x=4624;
            y=278;}
            if(ir==-13) {x=1541;
            y=92;}

 

            String LandUse = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            String SoilData = "/Users/luciana-cunha/Documents/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
                   if (BasinFlag == 0) {
            LandUse = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            SoilData = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }

        if (BasinFlag == 1) {
            LandUse = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverIntenseAgriculture_90_2.metaVHC";
            SoilData = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }


        if (BasinFlag == 2) {
            LandUse = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverRestoringCropsTo10percentGrassland_90_2.metaVHC";
            SoilData = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }


        if (BasinFlag == 3) {
            LandUse = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverRestoringPastureTo10percentForest_90_2.metaVHC";
            SoilData = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }


        if (BasinFlag == 4) {
            LandUse = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcoverBaseline_90_2.metaVHC";
            SoilData = "/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        }

            new SCS(x, y, matDirs, magnitudes, metaModif, DEMFile, new java.io.File(LandUse), new java.io.File(SoilData), new java.io.File(OutputDir)).ExecuteSCS();
            j = j + 1;
        }

    }
}
