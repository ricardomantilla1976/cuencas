/*
 CUENCAS is a River Network Oriented GIS
 Copyright (C) 2005  Ricardo Mantilla

 This program is free software; you can redistribute it and/or
 modify it under the terms of the GNU General Public License
 as published by the Free Software Foundation; either version 2
 of the License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
/*
 * LandUseManager.java
 * Adapted from LandUseManager
 * Luciana Cunha
 * Created on October 08, 2008
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import java.io.IOException;

/**
 * This class estimates the average Curve Number for each hillslope based on
 * Land Cover and Soil information. Land Cover is obtained from the National Map
 * Seamless Server. Soil data is taken from STATSCO or SSURGO Database. This
 * class handles the physical information over a basin. It takes in a group of
 * raster files (land cover and Soil data) and calculate the average Curve
 * Number for each hilsslope.
 *
 * @author Luciana Cunha Adapted from LandUseManager.java(
 * @author Ricardo Mantilla)
 */
public class Gen_MatrixPintada {

//    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[] LandUseOnBasin;
    int[][] matrizPintada;
    private String OutputM;
    private String OutputL;

    /**
     * Creates a new instance of LandUseManager (with spatially and temporally
     * variable rainfall rates over the basin) based in a set of raster maps of
     * rainfall intensities
     *
     * @param LandUse The path to the Land Cover files
     * @param SoilPro The path to the Land Cover files
     * @param myCuenca The
     * {@link hydroScalingAPI.util.geomorphology.objects.Basin} object
     * describing the basin under consideration
     * @param linksStructure The topologic structure of the river network
     * @param metaDatos A MetaRaster describing the rainfall intensity maps
     * @param matDir The directions matrix of the DEM that contains the basin
     * @param magnitudes The magnitudes matrix of the DEM that contains the
     * basin
     */
    public Gen_MatrixPintada(String Outp, String Outl, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster metaDatos, byte[][] matDir, byte[][] HorLin, int[][] magnitudes, double[][] DEM) throws IOException {

        OutputM = Outp;
        OutputL = Outl;

        System.out.println("START THE SCS MANAGER \n");
        java.io.File OutputFileM = new java.io.File(OutputM);
        java.io.File OutputFileL = new java.io.File(OutputL);
        java.io.File directorio = OutputFileM.getParentFile();
        hydroScalingAPI.util.statistics.Stats rainStats = new hydroScalingAPI.util.statistics.Stats(DEM);
        System.out.println("    --> Stats of the File:  Max = " + rainStats.maxValue + " Min = " + rainStats.minValue + " Mean = " + rainStats.meanValue);


        int[][] matDirBox = new int[myCuenca.getMaxY() - myCuenca.getMinY() + 3][myCuenca.getMaxX() - myCuenca.getMinX() + 3];
        int[][] matHorLin = new int[myCuenca.getMaxY() - myCuenca.getMinY() + 3][myCuenca.getMaxX() - myCuenca.getMinX() + 3];
        int[][] matHorLinCode = new int[myCuenca.getMaxY() - myCuenca.getMinY() + 3][myCuenca.getMaxX() - myCuenca.getMinX() + 3];
        double[][] matDEM = new double[myCuenca.getMaxY() - myCuenca.getMinY() + 3][myCuenca.getMaxX() - myCuenca.getMinX() + 3];
        double[][] matDEMInBasin = new double[myCuenca.getMaxY() - myCuenca.getMinY() + 3][myCuenca.getMaxX() - myCuenca.getMinX() + 3];
        //hydroScalingAPI.util.statistics.Stats rainStats=new hydroScalingAPI.util.statistics.Stats(dataSnapShot,new Double(metaSNOW.getMissing()).doubleValue());       

        System.out.println("    --> Stats of the File:  Max = " + rainStats.maxValue + " Min = " + rainStats.minValue + " Mean = " + rainStats.meanValue);


        for (int i = 1; i < matDirBox.length - 1; i++) {
            for (int j = 1; j < matDirBox[0].length - 1; j++) {
                matHorLin[i][j] = (int) HorLin[i + myCuenca.getMinY() - 1][j + myCuenca.getMinX() - 1];

            }
        }

        for (int i = 1; i < matDirBox.length - 1; i++) {
            for (int j = 1; j < matDirBox[0].length - 1; j++) {
                matDEM[i][j] = DEM[i + myCuenca.getMinY() - 1][j + myCuenca.getMinX() - 1];
            }
        }

        /**
         * **** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA
         * COBERTURA DEL MAPA SOBRE LA CUENCA)****************
         */
        double minLonBasin = metaDatos.getMinLon() + myCuenca.getMinX() * metaDatos.getResLon() / 3600.0;
        double minLatBasin = metaDatos.getMinLat() + myCuenca.getMinY() * metaDatos.getResLat() / 3600.0;
        double maxLonBasin = metaDatos.getMinLon() + (myCuenca.getMaxX() + 2) * metaDatos.getResLon() / 3600.0;
        double maxLatBasin = metaDatos.getMinLat() + (myCuenca.getMaxY() + 2) * metaDatos.getResLat() / 3600.0;
        double res = metaDatos.getResLat();

        int xOulet, yOulet;
        hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;


        matrizPintada = new int[myCuenca.getMaxY() - myCuenca.getMinY() + 3][myCuenca.getMaxX() - myCuenca.getMinX() + 3];

        int nc = metaDatos.getNumCols();

         matrizPintada=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
            for (int i=0;i<linksStructure.contactsArray.length;i++){
                    xOulet=linksStructure.contactsArray[i]%metaDatos.getNumCols();
                    yOulet=linksStructure.contactsArray[i]/metaDatos.getNumCols();
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                    System.out.println("myHillActual.getXYHillSlope()[0]"+ myHillActual.getXYHillSlope()[0]);
                     for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=i+1;
                    }
              
            }

        //System.out.println("linksStructure.contactsArray.length: " + linksStructure.contactsArray.length + "  linksStructure.tailsArray.length = " + linksStructure.tailsArray.length);
        //System.out.println("matDEM.length()" + matDEM.length + "matDEM.length()" + matDEM[0].length);
        //System.out.println("matDEM.length()" + DEM.length + "matDEM.length()" + DEM[0].length);
        //System.out.println("atDirBox.length-1" + matDirBox.length + "mmatDirBox[0].length-1" + matDirBox[0].length);

        for (int i = 0; i < matDEMInBasin.length; i++) {
            for (int j = 0; j < matDEMInBasin[0].length; j++) {

                if (matHorLin[i][j] > 0) {
                    matHorLinCode[i][j] = matHorLin[i][j];
                }

                if (matrizPintada[i][j] > 0) {
                    matDEMInBasin[i][j] = DEM[i + myCuenca.getMinY() - 1][j + myCuenca.getMinX() - 1];
                } else {
                    matDEMInBasin[i][j] = -9.9;
                }
                //System.out.println(matrizPintada[i][j] + "  DEM " + matDEMInBasin[i][j]);
                //DEM[i][j]=matrizPintada[i][j];
            }
        }



        hydroScalingAPI.util.statistics.Stats rainStats2 = new hydroScalingAPI.util.statistics.Stats(matDEMInBasin, -9.9);
        System.out.println("min" + rainStats2.minValue + "max" + rainStats2.maxValue + "ave" + rainStats2.meanValue);

        java.io.File CorrDEM = new java.io.File(OutputM.substring(0, OutputM.indexOf("MP")) + "CorrDEM.asc");

        java.io.FileOutputStream outputDir;
        java.io.OutputStreamWriter newfile;
        java.io.BufferedOutputStream bufferout;
        String retorno = "\n";




        newfileASC(OutputFileM, myCuenca, matrizPintada, metaDatos);
        newfileASC(OutputFileL, myCuenca, matHorLinCode, metaDatos);
        newfileASCDouble(CorrDEM, myCuenca, matDEMInBasin, metaDatos);
        /*     for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
         if (matrizPintada[j][k] > 0){
         minLonBasin= metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0;
         minLatBasin = metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0;

         System.out.println("j "+j+"  k = "+k +"  matrizPintada[j][k] = "+matrizPintada[j][k]);
         }
         }*/
    }

    private void newfileASC(java.io.File AscFile, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, int[][] Finalmatrix, hydroScalingAPI.io.MetaRaster metaDatos) throws java.io.IOException {


        java.io.FileOutputStream outputDir;
        java.io.OutputStreamWriter newfile;
        java.io.BufferedOutputStream bufferout;
        String retorno = "\n";

        outputDir = new java.io.FileOutputStream(AscFile);
        bufferout = new java.io.BufferedOutputStream(outputDir);
        newfile = new java.io.OutputStreamWriter(bufferout);

        int nlin = (myCuenca.getMaxY() - myCuenca.getMinY() + 3);
        int ncol = (myCuenca.getMaxX() - myCuenca.getMinX() + 3);

        double minLonBasin = metaDatos.getMinLon() + myCuenca.getMinX() * metaDatos.getResLon() / 3600.0;
        double minLatBasin = metaDatos.getMinLat() + myCuenca.getMinY() * metaDatos.getResLat() / 3600.0;
        double demResLat = metaDatos.getResLat() / 3600;
        newfile.write("ncols " + ncol + retorno);
        newfile.write("nrows " + nlin + retorno);
        newfile.write("xllcorner " + minLonBasin + retorno);// Iowa river
        newfile.write("yllcorner " + minLatBasin + retorno);//Iowa river
        newfile.write("cellsize " + demResLat + retorno);
        newfile.write("NODATA_value  " + "-9" + retorno);

        //   System.out.println("OUTPUT PROGRAM - Fcolumns = " +Finalcolumns + "Frows = "+Finalrows);
        for (int i = (nlin - 1); i >= 0; i--) {
            for (int j = 0; j < ncol; j++) {
                newfile.write(Finalmatrix[i][j] + " ");
            }
            newfile.write(retorno);
        }

        newfile.close();
        bufferout.close();
        outputDir.close();
    }

    private void newfileASCDouble(java.io.File AscFile, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, double[][] Finalmatrix, hydroScalingAPI.io.MetaRaster metaDatos) throws java.io.IOException {


        java.io.FileOutputStream outputDir;
        java.io.OutputStreamWriter newfile;
        java.io.BufferedOutputStream bufferout;
        String retorno = "\n";

        outputDir = new java.io.FileOutputStream(AscFile);
        bufferout = new java.io.BufferedOutputStream(outputDir);
        newfile = new java.io.OutputStreamWriter(bufferout);

        int nlin = (myCuenca.getMaxY() - myCuenca.getMinY() + 3);
        int ncol = (myCuenca.getMaxX() - myCuenca.getMinX() + 3);

        double minLonBasin = metaDatos.getMinLon() + myCuenca.getMinX() * metaDatos.getResLon() / 3600.0;
        double minLatBasin = metaDatos.getMinLat() + myCuenca.getMinY() * metaDatos.getResLat() / 3600.0;
        double demResLat = metaDatos.getResLat() / 3600;
        newfile.write("ncols " + ncol + retorno);
        newfile.write("nrows " + nlin + retorno);
        newfile.write("xllcorner " + minLonBasin + retorno);// Iowa river
        newfile.write("yllcorner " + minLatBasin + retorno);//Iowa river
        newfile.write("cellsize " + demResLat + retorno);
        newfile.write("NODATA_value  " + "-9.9" + retorno);

        //   System.out.println("OUTPUT PROGRAM - Fcolumns = " +Finalcolumns + "Frows = "+Finalrows);
        for (int i = (nlin - 1); i >= 0; i--) {
            for (int j = 0; j < ncol; j++) {
                newfile.write(Finalmatrix[i][j] + " ");
            }
            newfile.write(retorno);
        }

        newfile.close();
        bufferout.close();
        outputDir.close();
    }

    /**
     * Returns the value of rainfall rate in mm/h for a given moment of time
     *
     * @param HillNumber The index of the desired hillslope
     * @param dateRequested The time for which the rain is desired
     * @return Returns the rainfall rate in mm/h
     */
//    public float getLandUseOnHillslope(int HillNumber,java.util.Calendar dateRequested){
//
//        return LandUseOnBasin[HillNumber].getRecord(dateRequested);
//
//    }
    /**
     * Returns the maximum value of precipitation recorded for a given hillslope
     *
     * @param HillNumber The index of the desired hillslope
     * @return The maximum rainfall rate in mm/h
     */
//    public float getMaxLandUseOnHillslope(int HillNumber){
//
//        return LandUseOnBasin[HillNumber].getMaxRecord();
//
//    }
    /**
     * Returns the maximum value of precipitation recorded for a given hillslope
     *
     * @param HillNumber The index of the desired hillslope
     * @return The average rainfall rate in mm/h
     */
//    public float getMeanLandUseOnHillslope(int HillNumber){
//
//        return LandUseOnBasin[HillNumber].getMeanRecord();
//
//    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws IOException {
        String pathinput = "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/";
        pathinput = "/D:/usr/KANSAS/CUENCAS/Rasters/Topography/";

        //String pathinput = "//scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun9/";
        //java.io.File theFile=new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".metaDEM");
        java.io.File theFile = new java.io.File(pathinput + "mainbasin" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "mainbasin" + ".dir"));
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

       

        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "mainbasin" + ".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
       

        //metaModif.setLocationBinaryFile(new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".horton"));
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "mainbasin" + ".horton"));
        formatoOriginal = metaModif.getFormat();
        System.out.println("horton" + formatoOriginal);
        metaModif.setFormat("Byte");
        byte[][] HortonLinks = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setFormat("Double");
        //metaModif.setLocationBinaryFile(new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".corrDEM"));
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "mainbasin" + ".corrDEM"));

        System.out.println("dem  " + formatoOriginal);
        //System.exit(1);

        double[][] DEM = new hydroScalingAPI.io.DataRaster(metaModif).getDouble();

        int[] x = new int[36];
        int[] y = new int[36];
        int[] Code = new int[36];
        //
        x[1] = 1223;
        y[1] = 755;
        Code[1] = 6893350; //TOMAHAWK C NR OVERLAND PARK, KS
//        x[2] = 1296;
//        y[2] = 500;
//        Code[2] = 6893100; //BLUE R AT KENNETH RD, OVERLAND PARK, KS
//        x[3] = 1084;
//        y[3] = 854;
//        Code[3] = 6893300; // INDIAN C AT OVERLAND PARK, KS
//        x[4] = 1068;
//        y[4] = 391;
//        Code[4] = 6893080; // BLUE R NR STANLEY, KS
//        x[5] = 1410;
//        y[5] = 669;
//        Code[5] = 6893150; // Blue River at Blue Ridge Blvd Ext in KC, MO
//        x[6] = 1312;
//        y[6] = 847;
//        Code[6] = 6893390; // INDIAN C AT STATE LINE RD, LEAWOOD, KS
//        x[7] = 1324;
//        y[7] = 858;
//        Code[7] = 6893400; // Indian Creek at 103rd St in Kansas City, MO
//        x[8] = 1488;
//        y[8] = 913;
//        Code[8] = 6893500; // Blue River at Kansas City, MO
//        x[9] = 1321;
//        y[9] = 1189;
//        Code[9] = 6893557; // Brush Creek at Ward Parkway in Kansas City, MO
//        x[10] = 1417;
//        y[10] = 1208;
//        Code[10] = 6893562; // Brush Creek at Rockhill Road in Kansas City, MO
//        x[11] = 1658;
//        y[11] = 1278;
//        Code[11] = 6893578; // Blue River at Stadium Drive in Kansas City, MO


        // For Iowa and Cedar River
        //x[1]=3124; y[1]=234; Code[1]=9999999; 
//        x[0]=2646; y[0]=762; Code[0]=5454220; 
//x[1]=2817; y[1]=713; Code[1]=5454300; 
//x[1]=1298; y[1]=14; Code[1]=10000; 
//x[2]=2949; y[2]=741; Code[2]=5454000; 
//x[1]=2256; y[1]=876; Code[1]=5453100; 
//x[4]=1312; y[4]=1112; Code[4]=5451700; 
//x[5]=2858; y[5]=742; Code[5]=5454090; 
//x[6]=2115; y[6]=801; Code[6]=5453000; 
//x[7]=1871; y[7]=903; Code[7]=5452200; 
//x[8]=2885; y[8]=690; Code[8]=5454500; 
//x[9]=2796; y[9]=629; Code[9]=5455100; 
//x[10]=2958; y[10]=410; Code[10]=5455700; 
//x[11]=3186; y[11]=392; Code[11]=5465000; 
//x[12]=3316; y[12]=116; Code[12]=5465500; 
//x[1]=2734; y[1]=1069; Code[1]=9999998; 
//x[14]=1770; y[14]=1987; Code[14]=5458300; 
//x[15]=2676; y[15]=465; Code[15]=5455500; 
//x[16]=2900; y[16]=768; Code[16]=5453520; 
//x[17]=1765; y[17]=981; Code[17]=5451900; 
//x[18]=1245; y[18]=1181; Code[18]=5451500; 
//x[19]=951; y[19]=1479; Code[19]=5451210; 
//x[20]=3113; y[20]=705; Code[20]=5464942; 
//x[21]=1978; y[21]=1403; Code[21]=5464220; 
//x[22]=1779; y[22]=1591; Code[22]=5463500; 
//x[23]=1932; y[23]=1695; Code[23]=5464000; 
//x[24]=1798; y[24]=1750; Code[24]=5463050; 
//x[25]=1590; y[25]=1789; Code[25]=5463000; 
//x[26]=1682; y[26]=1858; Code[26]=5458900; 
//x[27]=1634; y[27]=1956; Code[27]=5462000; 
//x[28]=1775; y[28]=1879; Code[28]=5458500; 
//x[29]=903; y[29]=2499; Code[29]=5459500; 
//x[30]=1526; y[30]=2376; Code[30]=5457700; 
//x[31]=1730; y[31]=2341; Code[31]=5458000; 
//x[32]=1164; y[32]=3066; Code[32]=5457000; 
//x[33]=1741; y[33]=1831; Code[33]=5462000; 
//x[34]=3053; y[34]=2123; Code[34]=5412020; 
//x[31]=1730; y[31]=2341; Code[31]=5458000; 
//x[32]=1164; y[32]=3066; Code[32]=5457000; 
//x[33]=1741; y[33]=1831; Code[33]=5462000; 
//x[34]=3053; y[34]=2123; Code[34]=5412020; 
        //FINISHCode[34]=5412400;   
        //FINISHCode[34]=5411850;  
        //FINISHCode[34]=5412500;      
        //for (int i=1;i<x.length-1;i++) {
        for (int i = 1; i < x.length - 1; i++) {
            System.out.println(" x " + x[i] + "  y  " + y[i]);
            String Dir = "/D:/usr/KANSAS/CUENCAS/Rasters/Topography/MatrizPint30Conc/";
            String OutputM = Dir + "/MP30Pr9_" + x[i] + "_" + y[i] + "_" + Code[i] + ".asc";
            String OutputL = Dir + "/HL30Pr9_" + x[i] + "_" + y[i] + "_" + Code[i] + ".asc";
            new java.io.File(Dir + "/").mkdirs();

            hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x[i], y[i], matDirs, metaModif);
            hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaModif, matDirs);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);

            hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada Matrix;
            Matrix = new hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada(OutputM, OutputL, myCuenca, linksStructure, metaModif, matDirs, HortonLinks, magnitudes, DEM);

            // main basin
        }
    }
}
