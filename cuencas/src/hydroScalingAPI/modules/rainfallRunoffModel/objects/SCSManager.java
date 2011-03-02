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
 * SCSManager.java
 * Adapted from LandUseManager
 * Luciana Cunha
 * Created on October 08, 2008
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 * This class estimates for each hillslope:
 *  --- average Curve Number - f(land cover, soil hyd group)
 *  --- average manning roughness  - f(land cover, soil hyd group)
 * DEM is obtained from the Seamless system
 * Land Cover is obtained from the National Map Seamless Server.
 * Soil data is taken from STATSCO or SSURGO Database.
 * This class handles the physical information over a basin.
 * It takes in a group of raster files (DEM, land cover and Soil data)
 *
 * @author Luciana Cunha
 * Adapted from LandUseManager.java(@author Luciana Cunha)
 */
public class SCSManager {

    // Main variables for each hillslope  - average
    double[] Hyd_Group;  // [link]//
    double[] CN;  // [link]//
    double[] Manning;
    double[] HydCond;
    double[] K_NRCS;
    double[] HillslopeRelief; //[link]
    double[] avehillBasedSlope;
    // Maximum and minimum - give an idea about the variability in the hillslope
    double[] maxHillBasedCN;  // [link]//
    double[] minHillBasedCN;  // [link]//
    double[] maxHillBasedH;  // [link]//
    double[] minHillBasedH;  // [link]//
    double[] avehillBasedH;  // [link]//
    double[] maxHillBasedMan;  // [link]//
    double[] minHillBasedMan;  // [link]//
    double[] maxHillBasedHydCond;
    double[] minHillBasedHydCond;
    double[] maxHillBased_K_NRCS;  // [link]//
    double[] minHillBased_K_NRCS;  // [link]//

    // Estimate the curvature of the hillslope - 5 classes
    double[][] HillslopeReliefArea; //[link][class]
    double[][] hillclasses; //[link][class]
    double[][] terms;
    private boolean success = false, veryFirstDrop = true;
    private hydroScalingAPI.io.MetaRaster metaLandUse;
    private hydroScalingAPI.io.MetaRaster metaSoilData;
    private hydroScalingAPI.io.MetaRaster metaDemData;
    private java.util.Calendar InfoDate;
    float[][] LandUseOnBasinArray;
    private int[] MaxHillBasedLandUse;
    private float[] MaxHillBasedLandUsePerc;
    int[][] matrizPintada;
    float[][] SOILOnBasinArray;
    private int[] MaxHillBasedSOIL;
    private float[] MaxHillBasedSOILPerc;
    private double recordResolutionInMinutes;
    private String thisLandUseName;
    private String thisSoilData;
    private float hillshape;
    /**
     * Creates a new instance of SCSManager
     *
     * @param DemData The path to the DEM files - used to calculate hillslope relief
     * @param LandUse The path to the Land Cover files
     * @param SoilPro The path to the Land Cover files
     * @param myCuenca The {@link hydroScalingAPI.util.geomorphology.objects.Basin} object describing the
     * basin under consideration
     * @param linksStructure The topologic structure of the river network
     * @param metaDatos A MetaRaster describing the rainfall intensity maps
     * @param matDir The directions matrix of the DEM that contains the basin
     * @param magnitudes The magnitudes matrix of the DEM that contains the basin
     */
    public SCSManager(java.io.File DemData, java.io.File LandUse, java.io.File SoilData, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster metaDatos, byte[][] matDir, int[][] magnitudes,float HS) {

        System.out.println("START THE SCS MANAGER \n");
        java.io.File LCFile = LandUse;
        java.io.File SoilDataFile = SoilData;
        java.io.File DemDataFile = DemData;
        System.out.println("Man LandUse File = " + LandUse + "\n");
        System.out.println("Man SoilData File = " + SoilData + "\n");
        System.out.println("Man DEM File = " + DemData + "\n");
        java.io.File directorio = LandUse.getParentFile();
        String baseNameLC = directorio + "/" + (LandUse.getName().substring(0, LandUse.getName().lastIndexOf("."))) + ".vhc";
        directorio = SoilData.getParentFile();
        String baseNameSOIL = directorio + "/" + (SoilData.getName().substring(0, SoilData.getName().lastIndexOf("."))) + ".vhc";
        directorio = DemData.getParentFile();
        String baseNameDEM = directorio + "/" + (DemData.getName().substring(0, DemData.getName().lastIndexOf("."))) + ".dem";

        int[][] matDirBox = new int[myCuenca.getMaxY() - myCuenca.getMinY() + 3][myCuenca.getMaxX() - myCuenca.getMinX() + 3];

        for (int i = 1; i < matDirBox.length - 1; i++) {
            for (int j = 1; j < matDirBox[0].length - 1; j++) {
                matDirBox[i][j] = (int) matDir[i + myCuenca.getMinY() - 1][j + myCuenca.getMinX() - 1];
            }
        }

        try {
            hillshape=HS;
            // Create the metaraster for land use
            //       System.out.println("CREATE THE META RASTER _ LU \n");
            metaDemData = new hydroScalingAPI.io.MetaRaster(DemData);
            metaLandUse = new hydroScalingAPI.io.MetaRaster(LandUse);
            //       System.out.println("CREATE THE META RASTER _ SOIL DATA \n");
            metaSoilData = new hydroScalingAPI.io.MetaRaster(SoilData);
            /****** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA COBERTURA DEL MAPA SOBRE LA CUENCA)*****************/
            if (metaLandUse.getMinLon() > metaDatos.getMinLon() + myCuenca.getMinX() * metaDatos.getResLon() / 3600.0
                    || metaLandUse.getMinLat() > metaDatos.getMinLat() + myCuenca.getMinY() * metaDatos.getResLat() / 3600.0
                    || metaLandUse.getMaxLon() < metaDatos.getMinLon() + (myCuenca.getMaxX() + 2) * metaDatos.getResLon() / 3600.0
                    || metaLandUse.getMaxLat() < metaDatos.getMinLat() + (myCuenca.getMaxY() + 2) * metaDatos.getResLat() / 3600.0) {
                System.out.println("Not Area Coverage for LC");
                return;
            }

            if (metaDemData.getMinLon() > metaDatos.getMinLon() + myCuenca.getMinX() * metaDatos.getResLon() / 3600.0
                    || metaDemData.getMinLat() > metaDatos.getMinLat() + myCuenca.getMinY() * metaDatos.getResLat() / 3600.0
                    || metaDemData.getMaxLon() < metaDatos.getMinLon() + (myCuenca.getMaxX() + 2) * metaDatos.getResLon() / 3600.0
                    || metaDemData.getMaxLat() < metaDatos.getMinLat() + (myCuenca.getMaxY() + 2) * metaDatos.getResLat() / 3600.0) {
                System.out.println("Not Area Coverage for LC");
                return;
            }

            // check if soil data and LC data have the same properties (resolution, ncol, nline,...)
            if (metaSoilData.getNumRows() != metaLandUse.getNumRows()
                    || metaSoilData.getNumCols() != metaLandUse.getNumCols()
                    || metaSoilData.getResLat() != metaLandUse.getResLat()
                    || metaSoilData.getMinLat() != metaLandUse.getMinLat()
                    || metaSoilData.getMinLon() != metaLandUse.getMinLon()) {
                System.out.println("Soil and LC data have different matrix size or data resolution");
                return;
            }

            if (metaSoilData.getMinLon() > metaDatos.getMinLon() + myCuenca.getMinX() * metaDatos.getResLon() / 3600.0
                    || metaSoilData.getMinLat() > metaDatos.getMinLat() + myCuenca.getMinY() * metaDatos.getResLat() / 3600.0
                    || metaSoilData.getMaxLon() < metaDatos.getMinLon() + (myCuenca.getMaxX() + 2) * metaDatos.getResLon() / 3600.0
                    || metaSoilData.getMaxLat() < metaDatos.getMinLat() + (myCuenca.getMaxY() + 2) * metaDatos.getResLat() / 3600.0) {
                System.out.println("Not Area Coverage for Soil data");
                return;
            }


            int xOulet, yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            matrizPintada = new int[myCuenca.getMaxY() - myCuenca.getMinY() + 3][myCuenca.getMaxX() - myCuenca.getMinX() + 3];

            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
                if (linksStructure.magnitudeArray[i] < linksStructure.basinMagnitude) {

                    xOulet = linksStructure.contactsArray[i] % metaDatos.getNumCols();
                    yOulet = linksStructure.contactsArray[i] / metaDatos.getNumCols();

                    myHillActual = new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet, yOulet, matDir, magnitudes, metaDatos);
                    for (int j = 0; j < myHillActual.getXYHillSlope()[0].length; j++) {
                        matrizPintada[myHillActual.getXYHillSlope()[1][j] - myCuenca.getMinY() + 1][myHillActual.getXYHillSlope()[0][j] - myCuenca.getMinX() + 1] = i + 1;
                    }
                } else {

                    xOulet = myCuenca.getXYBasin()[0][0];
                    yOulet = myCuenca.getXYBasin()[1][0];

                    myHillActual = new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet, yOulet, matDir, magnitudes, metaDatos);
                    for (int j = 0; j < myHillActual.getXYHillSlope()[0].length; j++) {
                        matrizPintada[myHillActual.getXYHillSlope()[1][j] - myCuenca.getMinY() + 1][myHillActual.getXYHillSlope()[0][j] - myCuenca.getMinX() + 1] = i + 1;
                    }
                }
            }

            double[] evalSpotLC, evalSpotSOIL, evalSpotDem;
            double[][] dataSnapShotLC, dataSectionLC;
            double[][] dataSnapShotDem, dataSectionDem;
            double[][] dataSnapShotSOIL, dataSectionSOIL;
            int MatXLC, MatYLC;
            int MatXSOIL, MatYSOIL;
            int MatXDem, MatYDem;
            float[][] AreaHill;
            //System.out.println("-----------------Start of Files Reading - LC----------------");
            
            CN = new double[linksStructure.contactsArray.length];
            AreaHill = new float[1][linksStructure.contactsArray.length];
            Manning = new double[linksStructure.contactsArray.length];
            HydCond=new double[linksStructure.contactsArray.length];
            K_NRCS = new double[linksStructure.contactsArray.length];
            Hyd_Group = new double[linksStructure.contactsArray.length];
            maxHillBasedCN = new double[linksStructure.contactsArray.length];
            minHillBasedCN = new double[linksStructure.contactsArray.length];
            maxHillBasedMan = new double[linksStructure.contactsArray.length];
            minHillBasedMan = new double[linksStructure.contactsArray.length];
            maxHillBasedHydCond = new double[linksStructure.contactsArray.length];
            minHillBasedHydCond = new double[linksStructure.contactsArray.length];
            maxHillBased_K_NRCS = new double[linksStructure.contactsArray.length];
            minHillBased_K_NRCS = new double[linksStructure.contactsArray.length];
            int nclasses = 11;
            int[] currentHillNumPixels = new int[linksStructure.tailsArray.length];
            double[] currentHillBasedCN = new double[linksStructure.tailsArray.length];
            int[][] currentHillBasedLandUse = new int[linksStructure.tailsArray.length][nclasses];
            double[] currentHillBasedMan = new double[linksStructure.tailsArray.length];
            double[] currentHillBased_K_NRCS = new double[linksStructure.tailsArray.length];
            double[] currentHillBasedHydCond= new double[linksStructure.tailsArray.length];
            int[][] currentSoilType = new int[linksStructure.tailsArray.length][6];

            LandUseOnBasinArray = new float[linksStructure.contactsArray.length][nclasses];
            MaxHillBasedLandUse = new int[linksStructure.contactsArray.length];
            MaxHillBasedLandUsePerc = new float[linksStructure.contactsArray.length];
            SOILOnBasinArray = new float[linksStructure.contactsArray.length][6];
            MaxHillBasedSOIL = new int[linksStructure.contactsArray.length];
            MaxHillBasedSOILPerc = new float[linksStructure.contactsArray.length];

            HillslopeRelief = new double[linksStructure.contactsArray.length]; //[link]
            avehillBasedSlope = new double[linksStructure.contactsArray.length];

            minHillBasedH = new double[linksStructure.contactsArray.length];
            avehillBasedH = new double[linksStructure.contactsArray.length];

            HillslopeReliefArea = new double[linksStructure.contactsArray.length][5];
            hillclasses = new double[linksStructure.contactsArray.length][10]; //[link][class]
            HillslopeRelief = new double[linksStructure.contactsArray.length]; //[link]

            maxHillBasedH = new double[linksStructure.contactsArray.length];
            double PixelCN;
            PixelCN = -9.9;
            double PixelMan;
            PixelMan = -9.9;
            double PixelHydCond;
            PixelHydCond = -9.9;
            double PixelK_NRCS;
            PixelK_NRCS = -9.9;
            System.out.println("--> Loading data from LC = " + baseNameLC + "\n");
            metaLandUse.setLocationBinaryFile(new java.io.File((baseNameLC)));
            System.out.println("--> Loading data from LC = " + baseNameSOIL + "\n");
            metaSoilData.setLocationBinaryFile(new java.io.File((baseNameSOIL)));
            metaDemData.setLocationBinaryFile(new java.io.File((baseNameDEM)));
            System.out.println("--> Start to load the data Dem\n");
            dataSnapShotDem = new hydroScalingAPI.io.DataRaster(metaDemData).getDouble();

            System.out.println("--> Start to load the data Land cover\n");
            dataSnapShotLC = new hydroScalingAPI.io.DataRaster(metaLandUse).getDouble();

            System.out.println("--> Start to load the data Soil\n");
            dataSnapShotSOIL = new hydroScalingAPI.io.DataRaster(metaSoilData).getDouble();
            System.out.println("Finish load the data \n");
            hydroScalingAPI.util.statistics.Stats rainStats = new hydroScalingAPI.util.statistics.Stats(dataSnapShotLC, new Double(metaLandUse.getMissing()).doubleValue());
            System.out.println("    --> LC Stats of the File:  Max = " + rainStats.maxValue + " Min = " + rainStats.minValue + " Mean = " + rainStats.meanValue);
            rainStats = new hydroScalingAPI.util.statistics.Stats(dataSnapShotSOIL, new Double(metaSoilData.getMissing()).doubleValue());
            System.out.println("    --> SOIL Stats of the File:  Max = " + rainStats.maxValue + " Min = " + rainStats.minValue + " Mean = " + rainStats.meanValue);
            rainStats = new hydroScalingAPI.util.statistics.Stats(dataSnapShotDem, new Double(metaDemData.getMissing()).doubleValue());
            System.out.println("    --> DEM Stats of the File:  Max = " + rainStats.maxValue + " Min = " + rainStats.minValue + " Mean = " + rainStats.meanValue);
            //recorto la seccion que esta en la cuenca (TIENE QUE CONTENERLA)

            double demMinLon = metaDatos.getMinLon();
            double demMinLat = metaDatos.getMinLat();
            double demResLon = metaDatos.getResLon();
            double demResLat = metaDatos.getResLat();

            int basinMinX = myCuenca.getMinX();
            int basinMinY = myCuenca.getMinY();

            double LandUseMinLonLC = metaLandUse.getMinLon();
            double LandUseMinLatLC = metaLandUse.getMinLat();
            double LandUseResLonLC = metaLandUse.getResLon();
            double LandUseResLatLC = metaLandUse.getResLat();
            double LandUseMinLonSOIL = metaSoilData.getMinLon();
            double LandUseMinLatSOIL = metaSoilData.getMinLat();
            double LandUseResLonSOIL = metaSoilData.getResLon();
            double LandUseResLatSOIL = metaSoilData.getResLat();

            for (int j = 0; j < matrizPintada.length; j++) {
                for (int k = 0; k < matrizPintada[0].length; k++) {

                    evalSpotLC = new double[]{demMinLon + (basinMinX + k - 1) * demResLon / 3600.0,
                                demMinLat + (basinMinY + j - 1) * demResLat / 3600.0};

                    evalSpotSOIL = new double[]{demMinLon + (basinMinX + k - 1) * demResLon / 3600.0,
                                demMinLat + (basinMinY + j - 1) * demResLat / 3600.0};

                    MatXLC = (int) Math.floor((evalSpotLC[0] - LandUseMinLonLC) / LandUseResLonLC * 3600.0);
                    MatYLC = (int) Math.floor((evalSpotLC[1] - LandUseMinLatLC) / LandUseResLatLC * 3600.0);

                    MatXSOIL = (int) Math.floor((evalSpotSOIL[0] - LandUseMinLonSOIL) / LandUseResLonSOIL * 3600.0);
                    MatYSOIL = (int) Math.floor((evalSpotSOIL[1] - LandUseMinLatSOIL) / LandUseResLatSOIL * 3600.0);

                    if (matrizPintada[j][k] > 0) {

///////////////////// land use ///////////////////

                        if (dataSnapShotLC[MatYLC][MatXLC] > 10 && dataSnapShotLC[MatYLC][MatXLC] < 20) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][0]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] > 20 && dataSnapShotLC[MatYLC][MatXLC] < 30) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][1]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] > 30 && dataSnapShotLC[MatYLC][MatXLC] < 40) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][2]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] > 40 && dataSnapShotLC[MatYLC][MatXLC] < 50) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][3]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] > 50 && dataSnapShotLC[MatYLC][MatXLC] < 60) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][4]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] > 60 && dataSnapShotLC[MatYLC][MatXLC] < 70) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][5]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] > 70 && dataSnapShotLC[MatYLC][MatXLC] < 80) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][6]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] == 82) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][7]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] > 80 && dataSnapShotLC[MatYLC][MatXLC] < 90 && dataSnapShotLC[MatYLC][MatXLC] != 82) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][8]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] > 90 && dataSnapShotLC[MatYLC][MatXLC] < 100) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][9]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] < 10) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][1]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] == 2000) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][2]++;
                        }
                        if (dataSnapShotLC[MatYLC][MatXLC] == 101) {
                            currentHillBasedLandUse[matrizPintada[j][k] - 1][10]++; // Greenroof will be considered as grassland
                        }///////////////////// Hyd_group ///////////////////
                        if (dataSnapShotSOIL[MatYSOIL][MatXSOIL] == 1) {
                            currentSoilType[matrizPintada[j][k] - 1][0]++;
                        }
                        if (dataSnapShotSOIL[MatYSOIL][MatXSOIL] == 2) {
                            currentSoilType[matrizPintada[j][k] - 1][1]++;
                        }
                        if (dataSnapShotSOIL[MatYSOIL][MatXSOIL] == 3) {
                            currentSoilType[matrizPintada[j][k] - 1][2]++;
                        }
                        if (dataSnapShotSOIL[MatYSOIL][MatXSOIL] == 4) {
                            currentSoilType[matrizPintada[j][k] - 1][3]++;
                        }
                        if (dataSnapShotSOIL[MatYSOIL][MatXSOIL] == 5) {
                            currentSoilType[matrizPintada[j][k] - 1][4]++;
                        }
                        if (dataSnapShotSOIL[MatYSOIL][MatXSOIL] == 6) {
                            currentSoilType[matrizPintada[j][k] - 1][5]++;
                        }
                        if (dataSnapShotSOIL[MatYSOIL][MatXSOIL] <= 0) {
                            currentSoilType[matrizPintada[j][k] - 1][1]++;
                            dataSnapShotSOIL[MatYSOIL][MatXSOIL] = 2;
                        }
///////////////////// SOIL CONSERVATION NUMBER ///////////////////
                        PixelCN = EstimateSCS(dataSnapShotSOIL[MatYSOIL][MatXSOIL], dataSnapShotLC[MatYLC][MatXLC]);
                        PixelMan = EstimateManing(dataSnapShotSOIL[MatYSOIL][MatXSOIL], dataSnapShotLC[MatYLC][MatXLC]);
                        PixelK_NRCS = EstimateNRCS(dataSnapShotSOIL[MatYSOIL][MatXSOIL], dataSnapShotLC[MatYLC][MatXLC]);
                        PixelHydCond = Hyd_Conductivity(dataSnapShotSOIL[MatYSOIL][MatXSOIL]);

///////////////////// K NRCS for the hillslope ///////////////////

                        currentHillBasedCN[matrizPintada[j][k] - 1] = currentHillBasedCN[matrizPintada[j][k] - 1] + PixelCN;
                        if (PixelCN > maxHillBasedCN[matrizPintada[j][k] - 1]) {
                            maxHillBasedCN[matrizPintada[j][k] - 1] = PixelCN;
                        }
                        if (PixelCN < minHillBasedCN[matrizPintada[j][k] - 1] || PixelCN > 0) {
                            minHillBasedCN[matrizPintada[j][k] - 1] = PixelCN;
                        }

                        currentHillBasedMan[matrizPintada[j][k] - 1] = currentHillBasedMan[matrizPintada[j][k] - 1] + PixelMan;
                        if (PixelMan > maxHillBasedMan[matrizPintada[j][k] - 1]) {
                            maxHillBasedMan[matrizPintada[j][k] - 1] = PixelMan;
                        }
                        if (PixelMan < minHillBasedMan[matrizPintada[j][k] - 1] || PixelMan > 0) {
                            minHillBasedMan[matrizPintada[j][k] - 1] = PixelMan;
                        }

                        currentHillBased_K_NRCS[matrizPintada[j][k] - 1] = currentHillBased_K_NRCS[matrizPintada[j][k] - 1] + PixelK_NRCS;
                        if (PixelK_NRCS > maxHillBased_K_NRCS[matrizPintada[j][k] - 1]) {
                            maxHillBased_K_NRCS[matrizPintada[j][k] - 1] = PixelK_NRCS;
                        }
                        if (PixelK_NRCS < minHillBased_K_NRCS[matrizPintada[j][k] - 1] || PixelK_NRCS > 0) {
                            minHillBased_K_NRCS[matrizPintada[j][k] - 1] = PixelK_NRCS;
                        }

                        currentHillBasedHydCond[matrizPintada[j][k] - 1] = currentHillBasedHydCond[matrizPintada[j][k] - 1] + PixelHydCond;
                        if (PixelHydCond > maxHillBasedHydCond[matrizPintada[j][k] - 1]) {
                            maxHillBasedHydCond[matrizPintada[j][k] - 1] = PixelHydCond;
                        }
                        if (PixelHydCond < minHillBasedHydCond[matrizPintada[j][k] - 1] || PixelHydCond > 0) {
                            minHillBasedHydCond[matrizPintada[j][k] - 1] = PixelHydCond;
                        }

                        currentHillNumPixels[matrizPintada[j][k] - 1]++;

                    }

                }
            }

            java.io.File theFile;
            theFile = new java.io.File(LandUse.getParentFile() + "/" + "classes" + ".csv");

            java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
            java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
            java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

            for (int j = 0; j < linksStructure.contactsArray.length; j++) {
                newfile.write(linksStructure.contactsArray[j] + " ");
                newfile.write(currentHillNumPixels[j] + " ");
                CN[j] = currentHillBasedCN[j] / currentHillNumPixels[j];
                Manning[j] = currentHillBasedMan[j] / currentHillNumPixels[j];
                K_NRCS[j] = currentHillBased_K_NRCS[j] / currentHillNumPixels[j];
                HydCond[j] = currentHillBasedHydCond[j] / currentHillNumPixels[j];
                newfile.write(CN[j] + " " + Manning[j] + " " + K_NRCS[j] + " ");

                for (int n = 0; n < nclasses; n++) {
                    LandUseOnBasinArray[j][n] = (100 * currentHillBasedLandUse[j][n] / currentHillNumPixels[j]);
                    MaxHillBasedLandUse[j] = -9;
                    MaxHillBasedLandUsePerc[j] = 0.0f;
                    newfile.write(LandUseOnBasinArray[j][n] + " ");
                }

                for (int n = 0; n < nclasses; n++) {
                    if (LandUseOnBasinArray[j][n] > MaxHillBasedLandUsePerc[j]) {
                        MaxHillBasedLandUsePerc[j] = LandUseOnBasinArray[j][n];
                        MaxHillBasedLandUse[j] = n;
                    }
                }

                newfile.write(MaxHillBasedLandUse[j] + " ");
                newfile.write(MaxHillBasedLandUsePerc[j] + " ");

                for (int n = 0; n < 6; n++) {
                    SOILOnBasinArray[j][n] = (100 * currentSoilType[j][n] / currentHillNumPixels[j]);
                    MaxHillBasedSOIL[j] = -9;
                    MaxHillBasedSOILPerc[j] = 0.0f;
                    newfile.write(SOILOnBasinArray[j][n] + " ");
                }
                for (int n = 0; n < 6; n++) {
                    if (SOILOnBasinArray[j][n] > MaxHillBasedSOILPerc[j]) {
                        MaxHillBasedSOILPerc[j] = SOILOnBasinArray[j][n];
                        MaxHillBasedSOIL[j] = n;
                    }
                }

                newfile.write(MaxHillBasedSOIL[j] + " ");
                newfile.write(MaxHillBasedSOILPerc[j] + " ");
                newfile.write("\n");
                currentHillNumPixels[j] = 0;
                maxHillBasedH[j] = 0;
                minHillBasedH[j] = 1000000;
                avehillBasedSlope[j] = 0;
            }

            double DemMinLon = metaDemData.getMinLon();
            double DemMinLat = metaDemData.getMinLat();
            double DemResLon = metaDemData.getResLon();
            double DemResLat = metaDemData.getResLat();
            double PixelSize = 6378.0 * DemResLat * Math.PI / (3600.0 * 180.0) * 1000;
            double PixelDiag = PixelSize * Math.sqrt(2);
            System.out.println("PixelSize = " + PixelSize + " PixelDiag = " + PixelDiag);


            for (int j = 0; j < matrizPintada.length; j++) {
                for (int k = 0; k < matrizPintada[0].length; k++) {
                    evalSpotDem = new double[]{demMinLon + (basinMinX + k - 1) * demResLon / 3600.0,
                                demMinLat + (basinMinY + j - 1) * demResLat / 3600.0};

                    MatXDem = (int) Math.floor((evalSpotDem[0] - DemMinLon) / DemResLon * 3600.0);
                    MatYDem = (int) Math.floor((evalSpotDem[1] - DemMinLat) / DemResLat * 3600.0);

                    if (matrizPintada[j][k] > 0) {
                        double PixelH = dataSnapShotDem[MatYDem][MatXDem];
                        double maxslope = 0.0;
                        ///// remove the Math.abs because I just want the ones in the direction of the flow
                        // what means PixelH>neighPixel, and the value will be positive
                        // before it was considering negative and positive values in the average
                        // taking the maximum value at each time
                        // the maximum value should be smaller than 0.5
                        double slope = ((PixelH - dataSnapShotDem[MatYDem - 1][MatXDem - 1]) / PixelDiag);
                        maxslope = Math.max(maxslope, slope);
                        slope = ((PixelH - dataSnapShotDem[MatYDem - 1][MatXDem]) / PixelSize);
                        maxslope = Math.max(maxslope, slope);
                        slope = ((PixelH - dataSnapShotDem[MatYDem - 1][MatXDem + 1]) / PixelDiag);
                        maxslope = Math.max(maxslope, slope);
                        slope = ((PixelH - dataSnapShotDem[MatYDem + 1][MatXDem - 1]) / PixelDiag);
                        maxslope = Math.max(maxslope, slope);
                        slope = ((PixelH - dataSnapShotDem[MatYDem + 1][MatXDem]) / PixelSize);
                        maxslope = Math.max(maxslope, slope);
                        slope = ((PixelH - dataSnapShotDem[MatYDem + 1][MatXDem + 1]) / PixelDiag);
                        maxslope = Math.max(maxslope, slope);
                        slope = ((PixelH - dataSnapShotDem[MatYDem][MatXDem - 1]) / PixelSize);
                        maxslope = Math.max(maxslope, slope);
                        slope = ((PixelH - dataSnapShotDem[MatYDem][MatXDem + 1]) / PixelSize);
                        maxslope = Math.max(maxslope, slope);


                        avehillBasedSlope[matrizPintada[j][k] - 1] = avehillBasedSlope[matrizPintada[j][k] - 1] + maxslope;
                        //System.out.println("MatXDem = " + MatXDem +"MatYDem = " + MatYDem+"dataSnapShotDem= " + dataSnapShotDem[MatYDem][MatXDem]+"\n");
///////////////////// land use ///////////////////
                        if (PixelH > maxHillBasedH[matrizPintada[j][k] - 1]) {
                            maxHillBasedH[matrizPintada[j][k] - 1] = PixelH;
                        }
                        if (PixelH < minHillBasedH[matrizPintada[j][k] - 1] && PixelH > 0) {
                            minHillBasedH[matrizPintada[j][k] - 1] = PixelH;
                        }
                        avehillBasedH[matrizPintada[j][k] - 1] = avehillBasedH[matrizPintada[j][k] - 1] + PixelH;
                        currentHillNumPixels[matrizPintada[j][k] - 1]++;
                    }
                }
            }

            for (int j = 0; j < linksStructure.contactsArray.length; j++) {
                avehillBasedSlope[j] = avehillBasedSlope[j] / currentHillNumPixels[j];
                HillslopeRelief[j] = maxHillBasedH[j] - minHillBasedH[j];
                avehillBasedH[j] = avehillBasedH[j] / currentHillNumPixels[j];
                hillclasses[j][0] = minHillBasedH[j] + 0.2 * HillslopeRelief[j];
                hillclasses[j][1] = minHillBasedH[j] + 0.4 * HillslopeRelief[j];
                hillclasses[j][2] = minHillBasedH[j] + 0.6 * HillslopeRelief[j];
                hillclasses[j][3] = minHillBasedH[j] + 0.8 * HillslopeRelief[j];
                hillclasses[j][4] = maxHillBasedH[j];
                currentHillNumPixels[j] = 0;
            }

            for (int j = 0; j < matrizPintada.length; j++) {
                for (int k = 0; k < matrizPintada[0].length; k++) {
                    evalSpotDem = new double[]{demMinLon + (basinMinX + k - 1) * demResLon / 3600.0,
                                demMinLat + (basinMinY + j - 1) * demResLat / 3600.0};

                    MatXDem = (int) Math.floor((evalSpotDem[0] - DemMinLon) / DemResLon * 3600.0);
                    MatYDem = (int) Math.floor((evalSpotDem[1] - DemMinLat) / DemResLat * 3600.0);

                    if (matrizPintada[j][k] > 0) {
                        double PixelH = dataSnapShotDem[MatYDem][MatXDem];
///////////////////// land use ///////////////////

                        if (PixelH <= hillclasses[matrizPintada[j][k] - 1][0]) {
                            HillslopeReliefArea[matrizPintada[j][k] - 1][0]++;
                        }
                        if (PixelH > hillclasses[matrizPintada[j][k] - 1][0] && PixelH <= hillclasses[matrizPintada[j][k] - 1][1]) {
                            HillslopeReliefArea[matrizPintada[j][k] - 1][1]++;
                        }
                        if (PixelH > hillclasses[matrizPintada[j][k] - 1][1] && PixelH <= hillclasses[matrizPintada[j][k] - 1][2]) {
                            HillslopeReliefArea[matrizPintada[j][k] - 1][2]++;
                        }
                        if (PixelH > hillclasses[matrizPintada[j][k] - 1][2] && PixelH <= hillclasses[matrizPintada[j][k] - 1][3]) {
                            HillslopeReliefArea[matrizPintada[j][k] - 1][3]++;
                        }
                        if (PixelH > hillclasses[matrizPintada[j][k] - 1][3]) {
                            HillslopeReliefArea[matrizPintada[j][k] - 1][4]++;
                        }

                        currentHillNumPixels[matrizPintada[j][k] - 1]++;
                    }
                }
            }

            terms = new double[linksStructure.contactsArray.length][4];
            AreaHill = linksStructure.getVarValues(0);

            for (int j = 0; j < linksStructure.contactsArray.length; j++) {
                double accum = 0;
                double Aaccum = 0;
                double Relief = 0;

                   //1 model concave 1 (0+0.6h-1.6h^2+2.0*h^3)
                   //2 model concave 2 (0+0.5h-0.1h^2+0.6*h^3)
                   //3 model linear (h)
                  //4 model convex 1 (0+2.3h-2.1h^2+0.8*h^3)
                  //5 model convex 2 (0+3.5h-4.4h^2+1.9*h^3)

                if(hillshape==1.f){
                terms[j][0] =0.f;terms[j][1] =0.6f;terms[j][2] =-1.6f;terms[j][3] =2.f;
                }
                else if(hillshape == 2.f){
                terms[j][0] =0.f;terms[j][1] =0.5f;terms[j][2] =-0.1f;terms[j][3] =0.6f;
                }
                else if(hillshape == 3.f)
                {terms[j][0] =0.f;terms[j][1] =1.f;terms[j][2] =0.f;terms[j][3] =0.f;
                }
                else if(hillshape == 4.f)
                {terms[j][0] =0.f;terms[j][1] =2.3f;terms[j][2] =-2.1f;terms[j][3] =0.8f;
                }
                else if(hillshape == 5.f)
                {terms[j][0] =0.f;terms[j][1] =3.5f;terms[j][2] =-4.4f;terms[j][3] =1.9f;
                }

             else {
                java.util.Vector<hydroScalingAPI.util.polysolve.Pair> userDataVector;
                userDataVector = new java.util.Vector<hydroScalingAPI.util.polysolve.Pair>();
                userDataVector.add(new hydroScalingAPI.util.polysolve.Pair(0, 0));
                System.out.println("regression " + j);

                if(currentHillNumPixels[j]<5){
                        userDataVector.add(new hydroScalingAPI.util.polysolve.Pair(0.2, 0.2));
                        userDataVector.add(new hydroScalingAPI.util.polysolve.Pair(0.4, 0.4));
                        userDataVector.add(new hydroScalingAPI.util.polysolve.Pair(0.6, 0.6));
                        userDataVector.add(new hydroScalingAPI.util.polysolve.Pair(0.8, 0.8));
                        userDataVector.add(new hydroScalingAPI.util.polysolve.Pair(1.0, 1.0));}
                else {
                for (int ih = 0; ih < 5; ih++) {
                    HillslopeReliefArea[j][ih] = HillslopeReliefArea[j][ih] / currentHillNumPixels[j];
                    accum = accum + HillslopeReliefArea[j][ih];

                    Aaccum = ( AreaHill[0][j] * accum);
                    //Relief = (ih + 1) * (HillslopeRelief[j] / 5);
                    Relief = ((double)ih + 1.0) / 5.0;
                        userDataVector.add(new hydroScalingAPI.util.polysolve.Pair(Relief, accum));
              //      System.out.println("link" + j + "  n pixels"+currentHillNumPixels[j]+"  ih" + ih + "   accum"+ accum+ "  Relief " + Relief + "HillslopeReliefArea[j][ih]" +HillslopeReliefArea[j][ih]);
                   //userDataVector.add(new hydroScalingAPI.polysolve.Pair((ih+1)*0.2,accum));
                }}
                hydroScalingAPI.util.polysolve.MatrixFunctions mfunct;
                mfunct = new hydroScalingAPI.util.polysolve.MatrixFunctions();
                hydroScalingAPI.util.polysolve.Pair[] userData;
                userData = userDataVector.toArray(new hydroScalingAPI.util.polysolve.Pair[]{});
                terms[j] = mfunct.polyregress(userData, 3);
                }


                //System.out.println("A " + terms[j][0] + " B " + terms[j][1] + " C " + terms[j][2] + " D " + terms[j][3]);
            }

//     double result_cc = mfunct.corr_coeff(userData, terms[j]);
//     double result_se = mfunct.std_error(userData, terms[j]);}




        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
        }

    }

    /**
     *  A boolean flag indicating if the files were  fully read
     * @return A flag for the constructor success
     */
    public boolean isCompleted() {
        return success;
    }

    public double EstimateSCS(double soil, double LC) {
        double PixelCN = -9.9;
        if (soil == 5 || soil == 6) {
            PixelCN = 100;
        }

        if (LC == 101) {
            PixelCN = 84; // IF IT IS GREENROOF IT WILL BE CN=84 INDEPENDENTLY OF THE SOIL
        }
        if (soil == 1 && LC != 101) {
            if (LC == 82) {
                PixelCN = 64;
            } else if (LC == 41 || LC == 42 || LC == 43 || LC == 2000) {
                PixelCN = 30;
            } else if (LC == 31 || LC == 32 || LC == 51 || LC == 52
                    || LC == 71 || LC == 72 || LC == 73 || LC == 74
                    || LC == 81 || LC == 61) {
                PixelCN = 39;
            } else if (LC == 21 || LC == 83 || LC == 84 || LC == 85) {
                PixelCN = 51;
            } else if (LC == 22) {
                PixelCN = 57;
            } else if (LC == 23) {
                PixelCN = 61;
            } else if (LC == 24) {
                PixelCN = 77;
            } else if (LC == 11 || LC == 12 || LC == 90 || LC == 95 || LC == 92) {
                PixelCN = 100;
            } else {
                PixelCN = 40;
            }
        }
        if (soil == 2 && LC != 101) {
            if (LC == 82) {
                PixelCN = 75;
            } else if (LC == 41 || LC == 42 || LC == 43 || LC == 2000) {
                PixelCN = 55;
            } else if (LC == 31 || LC == 32 || LC == 51 || LC == 52
                    || LC == 71 || LC == 72 || LC == 73 || LC == 74
                    || LC == 81 || LC == 61) {
                PixelCN = 61;
            } else if (LC == 21 || LC == 83 || LC == 84 || LC == 85) {
                PixelCN = 68;
            } else if (LC == 22) {
                PixelCN = 72;
            } else if (LC == 23) {
                PixelCN = 75;
            } else if (LC == 24) {
                PixelCN = 85;
            } else if (LC == 101) {
                PixelCN = 84; // GREENROOF
            } else if (LC == 11 || LC == 12 || LC == 90 || LC == 95 || LC == 92) {
                PixelCN = 100;
            } else {
                PixelCN = 45;
            }
        }

        if (soil == 3 && LC != 101) {
            if (LC == 82) {
                PixelCN = 82;
            } else if (LC == 41 || LC == 42 || LC == 43 || LC == 2000) {
                PixelCN = 70;
            } else if (LC == 31 || LC == 32 || LC == 51 || LC == 52
                    || LC == 71 || LC == 72 || LC == 73 || LC == 74
                    || LC == 81 || LC == 61) {
                PixelCN = 74;
            } else if (LC == 21 || LC == 83 || LC == 84 || LC == 85) {
                PixelCN = 79;
            } else if (LC == 22) {
                PixelCN = 81;
            } else if (LC == 23) {
                PixelCN = 83;
            } else if (LC == 24) {
                PixelCN = 90;
            } else if (LC == 101) {
                PixelCN = 84; // GREENROOF
            } else if (LC == 11 || LC == 12 || LC == 90 || LC == 95 || LC == 92) {
                PixelCN = 100;
            } else {
                PixelCN = 50;
            }
        }
        if (soil == 4 && LC != 101) {
            if (LC == 82) {
                PixelCN = 85;
            } else if (LC == 41 || LC == 42 || LC == 2000 || LC == 43) {
                PixelCN = 77;
            } else if (LC == 31 || LC == 32 || LC == 51 || LC == 52
                    || LC == 71 || LC == 72 || LC == 73 || LC == 74
                    || LC == 81 || LC == 61) {
                PixelCN = 80;
            } else if (LC == 21 || LC == 83 || LC == 84 || LC == 85) {
                PixelCN = 84;
            } else if (LC == 22) {
                PixelCN = 86;
            } else if (LC == 23) {
                PixelCN = 87;
            } else if (LC == 24) {
                PixelCN = 92;
            } else if (LC == 101) {
                PixelCN = 84; // GREENROOF
            } else if (LC == 11 || LC == 12 || LC == 90 || LC == 95 || LC == 92) {
                PixelCN = 100;
            } else {
                PixelCN = 50;
            }
        }
        return PixelCN;
    }

        public double Hyd_Conductivity(double soil) {
        double PixelHydCond = -9.9;
        if (soil == 5 || soil == 6) {
            PixelHydCond = 0.00001;
        }

        if (soil == 1) { 
                PixelHydCond = 0.1;
        }

         if (soil == 2) {
                PixelHydCond = 0.005;
        }

         if (soil == 3) {
                PixelHydCond = 0.001;
        }

       if (soil == 4) {
                PixelHydCond = 0.0001;
        }

        return PixelHydCond;
    }

    public double EstimateManing(double soil, double LC) {
        double PixelMan = -9.9;
        if (soil == 5 && soil == 6) {
            PixelMan = 0.02;
        }
        if (LC == 101) {
            PixelMan = 0.05;
        }
        if (soil == 1) {
            if (LC == 11 || LC == 12 || LC == 90 || LC == 95 || LC == 92) {
                PixelMan = 0.01;
            } else if (LC == 21 || LC == 83 || LC == 84 || LC == 85) {
                PixelMan = 0.07;
            } else if (LC == 22) {
                PixelMan = 0.035;
            } else if (LC == 23) {
                PixelMan = 0.035;
            } else if (LC == 24) {
                PixelMan = 0.02;
            } else if (LC == 31 || LC == 32) {
                PixelMan = 0.04;
            } else if (LC == 41 || LC == 42 || LC == 2000 || LC == 43) {
                PixelMan = 0.08;
            } else if (LC == 51 || LC == 52) {
                PixelMan = 0.04;
            } else if (LC == 71 || LC == 72 || LC == 73 || LC == 74) {
                PixelMan = 0.05;
            } else if (LC == 81 || LC == 61) {
                PixelMan = 0.025;
            } else if (LC == 82) {
                PixelMan = 0.03;
            } else if (LC == 101) {
                PixelMan = 0.05; // GREENROOF
            } else {
                PixelMan = 0.03;
            }
        }

        if (soil == 2) {
            if (LC == 11 || LC == 12 || LC == 90 || LC == 95 || LC == 92) {
                PixelMan = 0.01;
            } else if (LC == 21 || LC == 83 || LC == 84 || LC == 85) {
                PixelMan = 0.07 - 0.0025;
            } else if (LC == 22) {
                PixelMan = 0.035 - 0.0025;
            } else if (LC == 23) {
                PixelMan = 0.035 - 0.0025;
            } else if (LC == 24) {
                PixelMan = 0.02 - 0.0025;
            } else if (LC == 31 || LC == 32) {
                PixelMan = 0.04 - 0.0025;
            } else if (LC == 41 || LC == 42 || LC == 2000 || LC == 43) {
                PixelMan = 0.08 - 0.002;
            } else if (LC == 51 || LC == 52) {
                PixelMan = 0.04 - 0.0025;
            } else if (LC == 71 || LC == 72 || LC == 73 || LC == 74) {
                PixelMan = 0.05 - 0.0025;
            } else if (LC == 101) {
                PixelMan = 0.05 - 0.0025;
            } else if (LC == 81 || LC == 61) {
                PixelMan = 0.025 - 0.0025;
            } else if (LC == 82) {
                PixelMan = 0.03 - 0.0025;
            } else {
                PixelMan = 0.03 - 0.0025;
            }
        }
        if (soil == 3) {
            if (LC == 11 || LC == 12 || LC == 90 || LC == 95 || LC == 92) {
                PixelMan = 0.01;
            } else if (LC == 21 || LC == 83 || LC == 84 || LC == 85) {
                PixelMan = 0.07 - 0.005;
            } else if (LC == 22) {
                PixelMan = 0.035 - 0.005;
            } else if (LC == 23) {
                PixelMan = 0.035 - 0.005;
            } else if (LC == 24) {
                PixelMan = 0.02 - 0.005;
            } else if (LC == 31 || LC == 32) {
                PixelMan = 0.04 - 0.005;
            } else if (LC == 41 || LC == 42 || LC == 2000 || LC == 43) {
                PixelMan = 0.08 - 0.005;
            } else if (LC == 51 || LC == 52) {
                PixelMan = 0.04 - 0.005;
            } else if (LC == 71 || LC == 72 || LC == 73 || LC == 74) {
                PixelMan = 0.05 - 0.005;
            } else if (LC == 101) {
                PixelMan = 0.05 - 0.005;
            } else if (LC == 81 || LC == 61) {
                PixelMan = 0.025 - 0.005;
            } else if (LC == 82) {
                PixelMan = 0.03 - 0.005;
            } else {
                PixelMan = 0.03 - 0.005;
            }
        }
        if (soil == 4) {
            if (LC == 11 || LC == 12 || LC == 90 || LC == 95 || LC == 92) {
                PixelMan = 0.01;
            } else if (LC == 21 || LC == 83 || LC == 84 || LC == 85) {
                PixelMan = 0.07 - 0.0075;
            } else if (LC == 22) {
                PixelMan = 0.035 - 0.0075;
            } else if (LC == 23) {
                PixelMan = 0.035 - 0.0075;
            } else if (LC == 24) {
                PixelMan = 0.02 - 0.0075;
            } else if (LC == 31 || LC == 32) {
                PixelMan = 0.04 - 0.0075;
            } else if (LC == 41 || LC == 42 || LC == 2000 || LC == 43) {
                PixelMan = 0.08 - 0.0075;
            } else if (LC == 51 || LC == 52) {
                PixelMan = 0.04 - 0.0075;
            } else if (LC == 71 || LC == 72 || LC == 73 || LC == 74) {
                PixelMan = 0.05 - 0.0075;
            } else if (LC == 101) {
                PixelMan = 0.05 - 0.0075;
            } else if (LC == 81 || LC == 61) {
                PixelMan = 0.025 - 0.0075;
            } else if (LC == 82) {
                PixelMan = 0.03 - 0.0075;
            } else {
                PixelMan = 0.03 - 0.0075;
            }
        }

        return PixelMan;
    }
///////////////////// K NRCS for the hillslope ///////////////////

    public double EstimateNRCS(double soil, double LC) {
        double PixelK_NRCS = -9.9;
        if (LC == 11 || LC == 12 || LC == 90 || LC == 95 || LC == 92) {
            PixelK_NRCS = 15.7;
        } else if (LC == 21 || LC == 22 || LC == 23 || LC == 24) {
            PixelK_NRCS = 20.4;
        } else if (LC == 31 || LC == 32 || LC == 33 || LC == 71
                || LC == 81 || LC == 85) {
            PixelK_NRCS = 7.035;
        } else if (LC == 41 || LC == 42 || LC == 2000 || LC == 43 || LC == 101) {
            PixelK_NRCS = 1.4;
        } else if (LC == 51 || LC == 52) {
            PixelK_NRCS = 1.4;
        } else if (LC == 82 || LC == 83) {
            PixelK_NRCS = 4.6;
        } else {
            PixelK_NRCS = 7.0;
        }
        return PixelK_NRCS;
    }

    /**
     * Returns the name of this LandUse file
     * @return A String that describes this LandUse
     */
    public String LandUseName() {
        return thisLandUseName;
    }

    /**
     * Returns the name of this Soil file
     * @return A String that describes this Soil
     */
    public String SoilName() {
        return thisSoilData;
    }

    /**
     * The LandUse time as a {@link java.util.Calendar} object
     * @return A {@link java.util.Calendar} object indicating the land cover date
     */
    public java.util.Calendar SCSInitialTime() {

        return InfoDate;
    }

    /**
     * The initial LandUse time as a double in milliseconds obtained from the method getTimeInMillis()
     * of the {@link java.util.Calendar} object
     */
    public double SCSInitialTimeInMinutes() {

        return InfoDate.getTimeInMillis() / 1000. / 60.;
    }

    /**
     * The LandUse record time resolution in minutes
     * @return A double indicating the time series time step
     */
    public double SCSRecordResolutionInMinutes() {

        return recordResolutionInMinutes;

    }

    /**
     * The average curve number over a hillslope
     * @param HillNumber
     * @return The average curve number for Antecedent moisture condition 2
     */
    public Double getAverCN2(int HillNumber) {
        return CN[HillNumber];
    }

    /**
     * The average curve number over a hillslope
     * @param HillNumber
     * @return The average curve number for Antecedent moisture condition 1
     */
    public Double getAverCN1(int HillNumber) {
        return CN[HillNumber] - (20 * (100 - CN[HillNumber]) / (100 - CN[HillNumber] + Math.exp(2.533 - 0.0636 * (100 - CN[HillNumber]))));

    }

    /**
     * The average curve number over a hillslope
     * @param HillNumber
     * @return The average curve number for Antecedent moisture condition 3
     */
    public Double getAverCN3(int HillNumber) {
        return CN[HillNumber] * Math.exp(0.00673 * (100 - CN[HillNumber]));
    }

    /**
     * The average manning roughness parameter over a hillslope
     * @param HillNumber
     * @return The average manning roughness parameter
     */
    public Double getAverManning(int HillNumber) {
        return Manning[HillNumber];
    }

    public Double getAverK_NRCS(int HillNumber) {
        return K_NRCS[HillNumber];
    }

    public Double getAverHydCond(int HillNumber) {
        return HydCond[HillNumber];
    }

    public Double getMaxHydCond(int HillNumber) {
        return maxHillBasedHydCond[HillNumber];
    }

    public Double getMinHydCond(int HillNumber) {
        return minHillBasedHydCond[HillNumber];
    }
    /**
     * The minimum manning roughness parameter over a hillslope
     * @param HillNumber
     * @return The min manning roughness parameter
     */
    public double getminHillBasedManning(int HillNumber) {
        return minHillBasedMan[HillNumber];
    }

    /**
     * The max manning roughness parameter over a hillslope
     * @param HillNumber
     * @return The max manning roughness parameter
     */
    public double getmaxHillBasedManning(int HillNumber) {
        return maxHillBasedMan[HillNumber];
    }

    public double minHillBasedCN(int HillNumber) {
        return minHillBasedCN[HillNumber];
    }

    public double maxHillBasedCN(int HillNumber) {
        return maxHillBasedCN[HillNumber];
    }

    public int getMaxHillLU(int HillNumber) {
        return MaxHillBasedLandUse[HillNumber];
    }

    public float getMaxHillLUPerc(int HillNumber) {
        return MaxHillBasedLandUsePerc[HillNumber];
    }

    public int getMaxHillSOIL(int HillNumber) {
        return MaxHillBasedSOIL[HillNumber];
    }

    public float getMaxHillSOILPerc(int HillNumber) {
        return MaxHillBasedSOILPerc[HillNumber];
    }

    public double getHillReliefPorc(int HillNumber, int HillClass) {
        if (HillClass < 5) {
            return HillslopeReliefArea[HillNumber][HillClass];
        } else {
            System.out.println("Error defining hillclass");
            return -1;
        }
    }

    public double getHillReliefClass(int HillNumber, int HillClass) {
        if (HillClass < 5) {
            return hillclasses[HillNumber][HillClass];
        } else {
            System.out.println("Error defining hillclass");
            return -1;
        }
    }

    public double getHillRelief(int HillNumber) {
        return HillslopeRelief[HillNumber];
    }

    public double getTerm(int HillNumber, int coef) {
        return terms[HillNumber][coef];
    }

    public double getHillReliefAve(int HillNumber) {
        return avehillBasedH[HillNumber];
    }

    public double getHillReliefMax(int HillNumber) {
        return maxHillBasedH[HillNumber];
    }

    public double getHillReliefMin(int HillNumber) {
        return minHillBasedH[HillNumber];
    }

    public double getavehillBasedSlope(int HillNumber) {
        return avehillBasedSlope[HillNumber];
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
    }
}
