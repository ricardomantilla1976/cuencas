/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 *
 * @author lcunha
 */
public class Width_function {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    int x;
    int y;
    int[][] magnitudes;
    java.io.File LandUseFile;
    java.io.File outputDirectory;

    public Width_function(int xx, int yy, byte[][] direcc, int[][] magnitudesOR,
            hydroScalingAPI.io.MetaRaster md, java.io.File LandUseFileOR, java.io.File outputDirectoryOR) throws java.io.IOException {

        matDir = direcc;
        metaDatos = md;

        x = xx;
        y = yy;
        magnitudes = magnitudesOR;
        LandUseFile = LandUseFileOR;
        outputDirectory = outputDirectoryOR;
    }

    public void ExecuteWidthFunction() throws java.io.IOException {

        System.out.println("Start to run Width function");
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDir, metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager LandUse;

        LandUse = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager(LandUseFile, myCuenca, linksStructure, metaDatos, matDir, magnitudes);
        

        thisHillsInfo.setLandUseManager(LandUse);

        //////////////////////////////////////
        System.out.println("Start to run Width function");
        java.io.File theFile;
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + "LandUse" + ".csv");

        System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);


        //     newfile.write("Information of precipition for each link\n");
        //     newfile.write("Links at the bottom of complete streams are:\n");
        //     newfile.write(1,");
        System.out.println("Open the file");
        newfile.write("1 ");
        double RC = -9.9;
        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1) {
                //               newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
                newfile.write(linksStructure.completeStreamLinksArray[i] + ",");
                newfile.write(thisNetworkGeom.Slope(linksStructure.completeStreamLinksArray[i]) + ",");
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i]) + ",");
                newfile.write(thisHillsInfo.Area(i) + ",");
                newfile.write(thisHillsInfo.LandUse(i) + ",");
                newfile.write(thisHillsInfo.LandUsePerc(i) + ",");
                if (thisHillsInfo.LandUse(i) == 0) {
                    RC = 1;
                }
                if (thisHillsInfo.LandUse(i) == 1) {
                    RC = 0.6;
                }
                if (thisHillsInfo.LandUse(i) == 2) {
                    RC = 0.2;
                }
                if (thisHillsInfo.LandUse(i) == 3) {
                    RC = 0.1;
                }
                if (thisHillsInfo.LandUse(i) == 4) {
                    RC = 0.2;
                }
                if (thisHillsInfo.LandUse(i) == 5) {
                    RC = 0.4;
                }
                if (thisHillsInfo.LandUse(i) == 6) {
                    RC = 0.4;
                }
                if (thisHillsInfo.LandUse(i) == 7) {
                    RC = 0.6;
                }
                if (thisHillsInfo.LandUse(i) == 8) {
                    RC = 0.45;
                }
                if (thisHillsInfo.LandUse(i) == 9) {
                    RC = 1;
                }
                newfile.write(RC + ",");
                RC = RC * thisHillsInfo.Area(i);
                newfile.write(RC + ",");
                newfile.write("\n");
            }
        }
        System.out.println("Termina escritura de LandUse");

        newfile.close();
        bufferout.close();
        ////////////////////////
        String demName = metaDatos.getLocationBinaryFile().getName().substring(0, metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "-GERAL_WF" + ".wfs.csv");

        System.out.println("Writing Width Functions - " + theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);

        double[][] wfs_simple = linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray, 0);


        for (int l = 0; l < linksStructure.completeStreamLinksArray.length; l++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1) {
                    newfile.write(linksStructure.completeStreamLinksArray[l] + ",");
                    newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l]) + ",");
                    for (int j = 0; j < wfs_simple[l].length; j++) {
                    newfile.write( wfs_simple[l][j] + ",");
                    }
                    newfile.write("\n");
                }
        }
        System.out.println("Finish writing the function - " + theFile);
        newfile.close();

        demName = metaDatos.getLocationBinaryFile().getName().substring(0, metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "-SN_" + "Top_WF_LU" + ".wfs.csv");

        System.out.println("Writing Width Functions - " + theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        int nclass = 10;
        double[][][] wfs = linksStructure.getLandUseWidthFunctions(linksStructure.completeStreamLinksArray, thisHillsInfo, 0);
        for (int l = 0; l < linksStructure.completeStreamLinksArray.length; l++) {
            for (int c = 0; c < nclass; c++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1) {
                    newfile.write(linksStructure.completeStreamLinksArray[l] + ",");
                    newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(c + ",");
                    for (int j = 0; j < wfs[l][c].length; j++) {
                        newfile.write(wfs[l][c][j] + ",");
                    }
                    newfile.write("\n");
                }
            }
        }
        System.out.println("Finish writing the function - " + theFile);
        newfile.close();

        demName = metaDatos.getLocationBinaryFile().getName().substring(0, metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "-SN_" + "GeomWF_LU" + ".wfs.csv");


        System.out.println("Writing Width Functions - " + theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        nclass = 10;
        wfs = linksStructure.getLandUseWidthFunctions(linksStructure.completeStreamLinksArray, thisHillsInfo, 1);
        for (int l = 0; l < linksStructure.completeStreamLinksArray.length; l++) {
            for (int c = 0; c < nclass; c++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1) {
                    newfile.write(linksStructure.completeStreamLinksArray[l] + ",");
                    newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(c + ",");
                    for (int j = 0; j < wfs[l][c].length; j++) {
                        newfile.write(wfs[l][c][j] + ",");
                    }
                    newfile.write("\n");
                }
            }
        }
        System.out.println("Finish writing the function - " + theFile);
        newfile.close();

        // color code ordere
        bufferout.close();
        newfile.close();
        bufferout.close();
        ////////////////////////
        demName = metaDatos.getLocationBinaryFile().getName().substring(0, metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "-SN_" + "Top_WF_ORDER" + ".wfs.csv");


        System.out.println("Writing Width Functions - " + theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        nclass = 11;

        double[][][] wfs2 = linksStructure.getCCWidthFunctions(linksStructure.completeStreamLinksArray, thisNetworkGeom.getLinkOrderArray(), 0, 11);
        for (int l = 0; l < linksStructure.completeStreamLinksArray.length; l++) {
            for (int c = 0; c < nclass; c++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1) {
                    newfile.write(linksStructure.completeStreamLinksArray[l] + ",");
                    newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(c + ",");
                    for (int j = 0; j < wfs2[l][c].length; j++) {
                        newfile.write(wfs2[l][c][j] + ",");
                    }
                    newfile.write("\n");
                }
            }
        }
        System.out.println("Finish writing the function - " + theFile);
        newfile.close();
        bufferout.close();

        // color code ordere
        bufferout.close();
        newfile.close();
        bufferout.close();
        ////////////////////////
        demName = metaDatos.getLocationBinaryFile().getName().substring(0, metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "-SN_" + "Geom_WF_ORDER" + ".wfs.csv");


        System.out.println("Writing Width Functions - " + theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        nclass = 11;
        System.out.println("defining the slope classes for order- " + linksStructure.completeStreamLinksArray.length);
        wfs2 = linksStructure.getCCWidthFunctions(linksStructure.completeStreamLinksArray, thisNetworkGeom.getLinkOrderArray(), 1, 11);
        for (int l = 0; l < linksStructure.completeStreamLinksArray.length; l++) {
            for (int c = 0; c < nclass; c++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1) {
                    newfile.write(linksStructure.completeStreamLinksArray[l] + ",");
                    newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(c + ",");
                    for (int j = 0; j < wfs2[l][c].length; j++) {
                        newfile.write(wfs2[l][c][j] + ",");
                    }
                    newfile.write("\n");
                }
            }
        }
        System.out.println("Finish writing the function - " + theFile);
        newfile.close();
        bufferout.close();

        ////////////////////////
        demName = metaDatos.getLocationBinaryFile().getName().substring(0, metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        theFile = new java.io.File(outputDirectory.getAbsolutePath() + "/" + demName + "-SN_" + "Geom_WF_Slope" + ".wfs.csv");


        System.out.println("Writing Width Functions - " + theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);

        float[][] Slopeinfo = thisNetworkGeom.getSlopeArray();
        float[][] Slopeclass = new float[1][Slopeinfo[0].length];
        System.out.println("Slopeinfo.length- " + Slopeinfo[0].length);
        for (int l = 0; l < Slopeinfo[0].length; l++) {
            Slopeclass[0][l] = 9.0f;
            if (Slopeinfo[0][l] >= 0.0f && Slopeinfo[0][l] < 0.025f) {
                Slopeclass[0][l] = 0.0f;
            }
            if (Slopeinfo[0][l] >= 0.025f && Slopeinfo[0][l] < 0.05f) {
                Slopeclass[0][l] = 1.0f;
            }
            if (Slopeinfo[0][l] >= 0.05f && Slopeinfo[0][l] < 0.075f) {
                Slopeclass[0][l] = 2.0f;
            }
            if (Slopeinfo[0][l] >= 0.075f && Slopeinfo[0][l] < 0.1f) {
                Slopeclass[0][l] = 3.0f;
            }
            if (Slopeinfo[0][l] >= 0.1f && Slopeinfo[0][l] < 0.125f) {
                Slopeclass[0][l] = 4.0f;
            }
            if (Slopeinfo[0][l] >= 0.125f && Slopeinfo[0][l] < 0.15f) {
                Slopeclass[0][l] = 5.0f;
            }
            if (Slopeinfo[0][l] >= 0.15f && Slopeinfo[0][l] < 0.2f) {
                Slopeclass[0][l] = 6.0f;
            }
            if (Slopeinfo[0][l] >= 0.2f && Slopeinfo[0][l] < 0.25f) {
                Slopeclass[0][l] = 7.0f;
            }
            if (Slopeinfo[0][l] >= 0.25f && Slopeinfo[0][l] < 0.5f) {
                Slopeclass[0][l] = 8.0f;
            }
        }

        nclass = 10;

        wfs2 = linksStructure.getCCWidthFunctions(linksStructure.completeStreamLinksArray, Slopeclass, 0, 10);
        for (int l = 0; l < linksStructure.completeStreamLinksArray.length; l++) {
            for (int c = 0; c < nclass; c++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1) {
                    newfile.write(linksStructure.completeStreamLinksArray[l] + ",");
                    newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l]) + ",");
                    newfile.write(c + ",");
                    for (int j = 0; j < wfs2[l][c].length; j++) {
                        newfile.write(wfs2[l][c][j] + ",");
                    }
                    newfile.write("\n");
                }
            }
        }
        System.out.println("Finish writing the function - " + theFile);
        newfile.close();
        bufferout.close();

    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try {
            //subMain(args);     //Case Whitewater
            //subMain1(args);     //Case 11110103
            //subMain2(args);     //Case  Rio Puerco
            //subMain3(args);     //Case  11070208
            //subMain4(args);     //Case  11140102
            //subMain5(args);     //Iowa River
            //subMain6(args);     //Charlotte
            //subMainClearCreek(args);
            subMainCedar(args);
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void subMain(String args[]) throws java.io.IOException {

        String pathinput = "C:/CUENCAS/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/";
        java.io.File theFile = new java.io.File(pathinput + "Whitewaters" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "Whitewaters" + ".dir"));

        String formatoOriginal = metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
        metaModif.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        int x = 1063;
        int y = 496;

        String precname = "LC2001_cliped.metaVHC";
        String Dir = "C:/CUENCAS/Whitewater_database/Rasters/Hydrology/Land_info/LandCover2001_cliped/";
        new java.io.File(Dir + "/test/").mkdirs();
        String OutputDir = Dir + "/test/";

        String LandUse = Dir + precname;

        new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();

    }

    public static void subMain1(String args[]) throws java.io.IOException {

        String pathinput = "C:/CUENCAS/11110103/Rasters/Topography/";
        java.io.File theFile = new java.io.File(pathinput + "NED_71821716" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_71821716" + ".dir"));

        String formatoOriginal = metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
        metaModif.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        String precname = "p_lc_2001.metaVHC";
        String Dir = "C:/CUENCAS/11110103/Rasters/Hydrology/LandCover2001/";
        new java.io.File(Dir + "/07196500/").mkdirs();
        String OutputDir = Dir + "/07196500/";

        int x = 497;
        int y = 773;

        String LandUse = Dir + precname;

        new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();

        new java.io.File(Dir + "/07197000/").mkdirs();
        OutputDir = Dir + "/07197000/";

        x = 804;
        y = 766;

        new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();

    }

    public static void subMain2(String args[]) throws java.io.IOException {

        String pathinput = "C:/CUENCAS/Upper Rio Puerco DB/Rasters/Topography/1_ArcSec/";
        java.io.File theFile = new java.io.File(pathinput + "NED_54212683" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_54212683" + ".dir"));

        String formatoOriginal = metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
        metaModif.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        int x = 381;
        int y = 221;

        //String precname="landcover1992.metaVHC";
        // String Dir="C:/CUENCAS/Upper Rio Puerco DB/Rasters/Hydrology/LandCover1992/";
        String precname = "Landcover2001.metaVHC";
        String Dir = "C:/CUENCAS/Upper Rio Puerco DB/Rasters/Hydrology/LandCover2001/";
        new java.io.File(Dir + "/wf/").mkdirs();
        String OutputDir = Dir + "/wf/";

        String LandUse = Dir + precname;

        new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();

    }

    public static void subMain3(String args[]) throws java.io.IOException {

        String pathinput = "C:/CUENCAS/11070208/Rasters/Topography/";
        java.io.File theFile = new java.io.File(pathinput + "NED_23370878" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_23370878" + ".dir"));

        String formatoOriginal = metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
        metaModif.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        int x = 296;
        int y = 1167;


        String precname = "landcover2001.metaVHC";
        String Dir = "C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
        //String precname="landcover2001.metaVHC";
        //String Dir="C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
        new java.io.File(Dir + "/wf/").mkdirs();
        String OutputDir = Dir + "/wf/";

        String LandUse = Dir + precname;

        new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();

    }

    public static void subMain4(String args[]) throws java.io.IOException {

        String pathinput = "C:/CUENCAS/11140102/Rasters/Topography/";
        java.io.File theFile = new java.io.File(pathinput + "NED_20864854" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_20864854" + ".dir"));


        String formatoOriginal = metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
        metaModif.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        int x = 2444;
        int y = 1610;


        String precname = "landcover2001.metaVHC";
        String Dir = "C:/CUENCAS/11140102/Rasters/Hydrology/LandCover2001/";
        //String precname="landcover2001.metaVHC";
        //String Dir="C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
        new java.io.File(Dir + "/wf/").mkdirs();
        String OutputDir = Dir + "/wf/";

        String LandUse = Dir + precname;

        new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();


    }

    public static void subMain5(String args[]) throws java.io.IOException {

        String pathinput = "C:/CUENCAS/Iowa_river/Rasters/Topography/DEMS-Iowa/Averaged/";
        java.io.File theFile = new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".dir"));


        String formatoOriginal = metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
        metaModif.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        // Iowa River at Marengo x = 2256 y=876
        int x = 2256;
        int y = 876;
        // Cedar river at cedar rapids - x = 2734 y=1069
        //int x =2734;
        // int y =1069;
        // Iowa River at Wapello - x = 2734 y=1069
        //int x =3316;
        // int y =116;

        String precname = "landcover2001_90_2.metaVHC";
        String Dir = "C:/CUENCAS/Iowa_river/Rasters/Hydrology/LandCover2001/";
        //String precname="landcover2001.metaVHC";
        //String Dir="C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
        new java.io.File(Dir + "/wfmarengo/").mkdirs();
        String OutputDir = Dir + "/wfmarengo/";

        String LandUse = Dir + precname;

        new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();


    }

    public static void subMain6(String args[]) throws java.io.IOException {

        String pathinput = "C:/CUENCAS/Charlote/Rasters/Topography/Charlotte_1AS/";
        java.io.File theFile = new java.io.File(pathinput + "charlotte" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte" + ".dir"));


        String formatoOriginal = metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
        metaModif.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

        int x = 764;
        int y = 168;
//int x= 638;
//int y= 605; //Basin Code 33714519239827667
//int x= 640;
//int y= 604; //Basin Code 9506327795703752
//int x= 664;
//int y= 494; //Basin Code 901512727378702
//int x= 619;
//int y= 433; //Basin Code 3347976315469169

        String precname = "raster_cliped.metaVHC";
        String Dir = "C:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover2001/";
        //String precname="landcover2001.metaVHC";
        //String Dir="C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
        new java.io.File(Dir + "/wf_1AS/" + x + "_" + y + "/").mkdirs();
        String OutputDir = Dir + "/wf_1AS/" + x + "_" + y + "/";

        String LandUse = Dir + precname;

        new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();


    }

    
      public static void subMainCedar(String args[]) throws java.io.IOException {


        ///// DEM DATA /////
        String Dir = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/WidthFunctionCR2/";
        new java.io.File(Dir).mkdirs();

        //String[] AllSimName = {"90DEMUSGS"};
 
    String[] AllSimName = {"30DEMUSGSCA"};
    //"250DEMUSGS","500DEMUSGS",
   //"1000DEMUSGSCA","2000DEMUSGSCA","5000DEMUSGSCA","10000DEMUSGSCA","nldaDEMUSGSCA","90DEMUSGSCA","120DEMUSGSCA","30DEMUSGSCA","180DEMUSGSCA","250DEMUSGSCA","500DEMUSGSCA",
//   "1000DEMUSGS","2000DEMUSGS","5000DEMUSGS","10000DEMUSGS","nldaDEMUSGS","90DEMUSGS","120DEMUSGS","30DEMUSGS","180DEMUSGS",
//   "10DEMLIDAR","20DEMLIDAR","30DEMLIDAR","60DEMLIDAR","90DEMLIDAR",
//        "5DEMLIDARCA","10DEMLIDARCA","20DEMLIDARCA","30DEMLIDARCA","60DEMLIDARCA","90DEMLIDARCA",
//        "90DEMUSGS","90DEMASTER","90DEMSRTM","90DEMUSGSPrun8","90DEMUSGSPrun7",
//   "90DEMUSGSPrun6","90DEMUSGSPrun5","120DEMUSGS","150DEMUSGS","180DEMUSGS",
   

        String[] AllRain = {"3CedarRapids"};

        int nsim = AllSimName.length;
        int nbas = AllRain.length;

        for (int i = 0; i < nsim; i++) {
            for (int ib = 0; ib < nbas; ib++) {
        System.out.println("Running BASIN " + AllSimName[i]);
                System.out.println("Running BASIN " + AllRain[ib]);
                  String SimName = AllSimName[i];
                String BasinName = AllRain[ib];
                java.io.File outputDirectory;
                
                // DEFINE DEM
                String[] StringDEM = {"error", "error", "error"};
                StringDEM = hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ParallelSimulationToFileHelium.defineDEMxy(BasinName, SimName);
                System.out.println("StringDEM = " + StringDEM[0]);
                System.out.println("x = " + StringDEM[1] + "    y" + StringDEM[2]);
                String DEM = StringDEM[0];
                int x = Integer.parseInt(StringDEM[1]);//   .getInteger(StringDEM[1]);
                int y= Integer.parseInt(StringDEM[2]);
            
                String ResStr = AllSimName[i] + "_" +AllRain[ib];
            String OutputDir = Dir + "/" + ResStr + "/";
            

            new java.io.File(OutputDir).mkdirs();
            java.io.File theFile=new java.io.File(DEM);

            hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(new java.io.File(DEM));
               
            java.io.File theFile2;

            theFile2 = new java.io.File(DEM.replace(".metaDEM", ".dir"));
            metaModif.setLocationBinaryFile(theFile2);
            String formatoOriginal = metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            metaModif.setLocationBinaryFile(new java.io.File(DEM.replace(".metaDEM", ".magn")));
            metaModif.setFormat("Integer");
            int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            metaModif.setFormat("Double");
            
            metaModif.setLocationBinaryFile(new java.io.File(DEM.replace(".metaDEM", ".corrDEM")));
        
            System.out.println("dem  "+formatoOriginal);
            //System.exit(1);
        
           double [][] DEMCorr=new hydroScalingAPI.io.DataRaster(metaModif).getDouble();
            
           metaModif.setLocationBinaryFile(new java.io.File(DEM.replace(".metaDEM", ".horton")));
           formatoOriginal=metaModif.getFormat();
           System.out.println("horton"+formatoOriginal);
           metaModif.setFormat("Byte");
           byte [][] HortonLinks=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        


            String LandUse = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";

            new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();
            String SoilData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
            String SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            String Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
            
            new SCS(x, y, matDirs, magnitudes, metaModif, theFile, new java.io.File(LandUse), new java.io.File(SoilData), new java.io.File(SoilHydData), new java.io.File(Soil150SWAData), new java.io.File(OutputDir)).ExecuteSCS();
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDirs,metaModif);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaModif, matDirs);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);

             String OutputM=OutputDir+"/MP90Pr9_" + x + "_"+ y + "_"+".asc";
             String OutputL=OutputDir+"/HL90Pr9_" + x + "_"+ y + "_"+".asc";
             
     
            hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada Matrix;
            System.out.println("OutputM"+OutputM);
            Matrix=new hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada(OutputM,OutputL, myCuenca, linksStructure, metaModif, matDirs, HortonLinks, magnitudes,DEMCorr);

     
            }
        }
    }
    public static void subMainClearCreek(String args[]) throws java.io.IOException {


        ///// DEM DATA /////
        String Dir = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/WidthFunction/";
        new java.io.File(Dir).mkdirs();

        //int[] Res = {90, 60, 30, 20, 10, 5,-9};
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




            String pathinput = "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/" + ir + "meters/";
            if(ir==-9) pathinput="/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/";
            if(ir==-10) pathinput="/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/";
            if(ir==-11) pathinput="/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/";
            if(ir==-12) pathinput="/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/10USGS/";
            if(ir==-13) pathinput="/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/30USGS/";

            java.io.File theFile;

            theFile = new java.io.File(pathinput + ir + "meterc1.metaDEM");
            if(ir==-9) theFile= new java.io.File(pathinput +"AveragedIowaRiverAtColumbusJunctions.metaDEM");
            if(ir==-10)  theFile= new java.io.File(pathinput +"astercc.metaDEM");
            if(ir==-11)  theFile= new java.io.File(pathinput +"IowaRiverAtIowaCity.metaDEM");
            if(ir==-12)  theFile= new java.io.File(pathinput +"ned_1_3.metaDEM");
            if(ir==-13)  theFile= new java.io.File(pathinput +"ned_1.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);

            java.io.File theFile2;

                      theFile2 = new java.io.File(pathinput + ir + "meterc1.dir");
            if(ir==-9) theFile2= new java.io.File(pathinput +"AveragedIowaRiverAtColumbusJunctions.dir");
            if(ir==-10)  theFile2= new java.io.File(pathinput +"astercc.dir");
            if(ir==-11)  theFile2= new java.io.File(pathinput +"IowaRiverAtIowaCity.dir");
            if(ir==-12)  theFile2= new java.io.File(pathinput +"ned_1_3.dir");
            if(ir==-13)  theFile2= new java.io.File(pathinput +"ned_1.dir");
            metaModif.setLocationBinaryFile(theFile2);
            String formatoOriginal = metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
            metaModif.setFormat("Integer");
            int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();


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

            new Width_function(x, y, matDirs, magnitudes, metaModif, new java.io.File(LandUse), new java.io.File(OutputDir)).ExecuteWidthFunction();
            String SoilData = "/nfsscratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
            String SoilHydData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            String Soil150SWAData = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
            new SCS(x, y, matDirs, magnitudes, metaModif, theFile, new java.io.File(LandUse), new java.io.File(SoilData), new java.io.File(SoilHydData), new java.io.File(Soil150SWAData), new java.io.File(OutputDir)).ExecuteSCS();
            j = j + 1;
        }
    }
    
    
      

}
