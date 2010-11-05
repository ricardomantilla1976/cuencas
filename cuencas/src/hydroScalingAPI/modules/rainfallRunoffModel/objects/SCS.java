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

        SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DemFile, LandUseFile, SoilDataFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);

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
        newfile.write("totalLength[km2]");
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
        newfile.write("velocity,");//25
        newfile.write("time,");//26
        newfile.write("format_flag,");//27
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
            newfile.write(thisNetworkGeom.mainChannelLength(i)+",");
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


            double vr = (SCSObj.getAverK_NRCS(i)) * Math.pow((SCSObj.getavehillBasedSlope(i)), 0.5) * 100 * 0.3048;
            newfile.write(vr + ",");//25
            double tt = dist / vr;
            newfile.write(tt + ",");//26

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
            newfile.write(format_flag + ","); //27
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

        System.out.println("Start to run Width function");

        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "SCScompleteO" + ".csv");

        System.out.println(theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);



        nLi = linksStructure.completeStreamLinksArray.length;
        Area_length = new double[linksStructure.completeStreamLinksArray.length];
        System.out.println("Open the file");


        for (int i = 0; i < nLi; i++) {
            //if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
            int matrix_pin = linksStructure.completeStreamLinksArray[i] + 1;
            int ii = linksStructure.completeStreamLinksArray[i];
            newfile.write(matrix_pin + ","); //1
            newfile.write(thisNetworkGeom.linkOrder(ii) + ","); //2
            newfile.write(thisHillsInfo.Area(ii) + ",");//8
            newfile.write(thisNetworkGeom.upStreamArea(ii) + ",");       //7
            newfile.write(SCSObj.getavehillBasedSlope(ii) + ",");//3
            newfile.write(thisNetworkGeom.Length(ii) + ",");
            newfile.write(SCSObj.getHillRelief(ii) + ",");
            newfile.write(SCSObj.getHillReliefMin(ii) + ",");
            newfile.write(SCSObj.getHillReliefMax(ii) + ",");
            double Hchannel = thisNetworkGeom.Slope(ii) * thisNetworkGeom.Length(ii);
            double HillRelief = SCSObj.getHillRelief(ii) - Hchannel;
            double FormParam = (HillRelief) * 2 * thisNetworkGeom.Length(ii) / (thisHillsInfo.Area(ii) * 1000000);
            newfile.write(HillRelief + ",");
            newfile.write(Hchannel + ",");
            newfile.write(FormParam + ",");
            newfile.write(SCSObj.getAverManning(ii) + ",");
            newfile.write(SCSObj.getminHillBasedManning(ii) + ",");
            newfile.write(SCSObj.getmaxHillBasedManning(ii) + ",");
            newfile.write(SCSObj.getavehillBasedSlope(ii) + ",");
            newfile.write(thisNetworkGeom.Slope(ii) + ",");
            newfile.write(thisNetworkGeom.upStreamArea(ii) + ",");


            newfile.write(thisNetworkGeom.upStreamTotalLength(ii) + ",");
            newfile.write(thisHillsInfo.LandUseSCS(ii) + ",");
            newfile.write(thisHillsInfo.Soil_SCS(ii) + ",");
            newfile.write(thisHillsInfo.SCS_S1(ii) + ",");

            double dist = thisHillsInfo.Area(ii) * 1000000 * 0.5 / (thisNetworkGeom.Length(ii)); //(m)
            double tim_run = dist / 100; //hour
            newfile.write(tim_run + ",");
            double format;
            format = SCSObj.getHillReliefMin(ii) + ((SCSObj.getHillReliefMax(ii) - SCSObj.getHillReliefMin(ii)) / 2);
            double format_flag = -9.9;
            if (format < SCSObj.getHillReliefAve(ii)) {
                format_flag = 1;
            }
            if (format > SCSObj.getHillReliefAve(ii)) {
                format_flag = -1;
            }
            if (format == SCSObj.getHillReliefAve(ii)) {
                format_flag = 0;
            }
            newfile.write(format_flag + ",");

            //newfile.write(thisHillsInfo.SCS_IA2(i)+" ");
            //newfile.write(thisHillsInfo.SCS_S2(i)+" ");
            //newfile.write(thisHillsInfo.SCS_IA3(i)+" ");
            //newfile.write(thisHillsInfo.SCS_S3(i)+" ");
            //for (int j=0;j<5;j++){
            //newfile.write(SCSObj.getHillReliefClass(i,j) +" ");
            //newfile.write(SCSObj.getHillReliefPorc(i,j) +" ");
            //}

            newfile.write("\n");
//                Area_length[ii]=thisHillsInfo.Area(ii)*1000000/thisNetworkGeom.Length(ii);
//                max_rel=Math.max(max_rel,Area_length[ii]);

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
        String Dir = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/Summary/";
        new java.io.File(Dir).mkdirs();

        //int[] Res = {90, 60, 30, 20, 10, 5};


        int[] Res = {-13};

        //int[] XX = {447, 670, 1341, 2013, 4025, 8052,-9};
        //int[] YY = {27, 41, 82, 122, 244, 497,-9};
        int[] XX = {-13};
        int[] YY = {-13};
        int j = 0;
        for (int ir : Res) {

            String ResStr = ir + "meter";
            String OutputDir = Dir + "/" + ResStr + "/";
            if(ir==-9) OutputDir= Dir + "/90USGS/";
            if(ir==-10) OutputDir= Dir + "/ASTER/";
            if(ir==-11) OutputDir= Dir + "/30USGS/";
            if(ir==-12) OutputDir= Dir + "/10USGS/";
            if(ir==-13) OutputDir= Dir + "/30USGS/";
            new java.io.File(OutputDir).mkdirs();

            java.io.File DEMFile;
            DEMFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/" + ir + "meters/" + ir + "meterc1.metaDEM");
            if(ir==-9) DEMFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            if(ir==-10) DEMFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM");
            if(ir==-11) DEMFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity.metaDEM");
            if(ir==-12) DEMFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/ned_1_3.metaDEM");
            if(ir==-13) DEMFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/ned_1.metaDEM");


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


            int x = XX[j];
            int y = YY[j]; //Clear Creek - coralville
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



            String LandUse = "/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            String SoilData = "/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";

            new SCS(x, y, matDirs, magnitudes, metaModif, DEMFile, new java.io.File(LandUse), new java.io.File(SoilData), new java.io.File(OutputDir)).ExecuteSCS();
            j = j + 1;
        }

    }
}
