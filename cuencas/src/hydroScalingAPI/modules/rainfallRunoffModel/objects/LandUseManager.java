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

/**
 * This class handles the physical information over a basin.
 * It takes in a group of raster files that represent snapshots of physical basin
 * information and projects those fields over the hillslope map to obtain hillslope-based
 * time series.
 * @author Luciana Cunha
 * Adapted from LandUseManager.java(@author Ricardo Mantilla)
 */
public class LandUseManager {

//    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[] LandUseOnBasin;
    float[][] LandUseOnBasinArray;
     // [link][land use classification]//
    private boolean success=false,veryFirstDrop=true;
    private hydroScalingAPI.io.MetaRaster metaLandUse;
    private java.util.Calendar InfoDate;

    private float[][] totalPixelBasedLandUse;
    private int[] MaxHillBasedLandUse;
    private float[] MaxHillBasedLandUsePerc;

    private hydroScalingAPI.util.fileUtilities.ChronoFile[] arCron;

    int[][] matrizPintada;

    private double recordResolutionInMinutes;

    private String thisLandUseName;

     /**
     * Creates a new instance of LandUseManager (with spatially and temporally variable rainfall
     * rates over the basin) based in a set of raster maps of rainfall intensities
     * @param locFile The path to the raster files
     * @param myCuenca The {@link hydroScalingAPI.util.geomorphology.objects.Basin} object describing the
     * basin under consideration
     * @param linksStructure The topologic structure of the river network
     * @param metaDatos A MetaRaster describing the rainfall intensity maps
     * @param matDir The directions matrix of the DEM that contains the basin
     * @param magnitudes The magnitudes matrix of the DEM that contains the basin
     */
    public LandUseManager(java.io.File locFile, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster metaDatos, byte[][] matDir, int[][] magnitudes) {

         System.out.println(" start read land use");
        java.io.File directorio=locFile.getParentFile();
        String baseName=locFile.getName().substring(0,locFile.getName().lastIndexOf("."));
          System.out.println("baseName="+baseName + "directorio" + locFile.getParentFile());
        hydroScalingAPI.util.fileUtilities.NameDotFilter myFiltro=new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseName,"vhc");
        java.io.File[] lasQueSi=directorio.listFiles(myFiltro);

        arCron=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSi.length];

        for (int i=0;i<lasQueSi.length;i++) arCron[i]=new hydroScalingAPI.util.fileUtilities.ChronoFile(lasQueSi[i],baseName);

        java.util.Arrays.sort(arCron);

        //Una vez leidos los archivos:
        //Lleno la matriz de direcciones

        for (int i=0;i<lasQueSi.length;i++) {System.out.println("File list="+arCron[i].fileName.getName());}

    int[][] matDirBox=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];

        for (int i=1;i<matDirBox.length-1;i++) for (int j=1;j<matDirBox[0].length-1;j++){
            matDirBox[i][j]=(int) matDir[i+myCuenca.getMinY()-1][j+myCuenca.getMinX()-1];
        }

        try{
            System.out.println("THIS IS THE LOCFILE \n" + locFile);
            metaLandUse=new hydroScalingAPI.io.MetaRaster(locFile);

            /****** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA COBERTURA DEL MAPA SOBRE LA CUENCA)*****************/
            if (metaLandUse.getMinLon() > metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0 ||
                metaLandUse.getMinLat() > metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0 ||
                metaLandUse.getMaxLon() < metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0 ||
                metaLandUse.getMaxLat() < metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0) {
                    System.out.println("Not Area Coverage");
                    return;
            }

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            matrizPintada=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];

            for (int i=0;i<linksStructure.contactsArray.length;i++){
                if (linksStructure.magnitudeArray[i] < linksStructure.basinMagnitude){

                    xOulet=linksStructure.contactsArray[i]%metaDatos.getNumCols();
                    yOulet=linksStructure.contactsArray[i]/metaDatos.getNumCols();

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=i+1;
                    }
                } else {

                    xOulet=myCuenca.getXYBasin()[0][0];
                    yOulet=myCuenca.getXYBasin()[1][0];

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=i+1;
                    }
                }
            }

//            LandUseOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];

            int regInterval=metaLandUse.getTemporalScale();

            System.out.println("Time interval for this file: "+regInterval);

 //           for (int i=0;i<LandUseOnBasin.length;i++){
 //               LandUseOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries(regInterval);
 //           }

            double[] evalSpot;
            double [][] dataSnapShot, dataSection;
            int MatX,MatY;

            //System.out.println("-----------------Start of Files Reading----------------");
            int nclasses=11;
            totalPixelBasedLandUse=new float[matDirBox.length][matDirBox[0].length];
            LandUseOnBasinArray=new float[linksStructure.contactsArray.length][nclasses];
            MaxHillBasedLandUse=new int[linksStructure.contactsArray.length];
            MaxHillBasedLandUsePerc=new float[linksStructure.contactsArray.length];

            int[][] currentHillBasedLandUse=new int[linksStructure.tailsArray.length][nclasses];
            int[] currentHillNumPixels=new int[linksStructure.tailsArray.length];
            System.out.println("arCron.length: "+arCron.length);
            for (int i=0;i<arCron.length;i++){
                //Cargo cada uno
                metaLandUse.setLocationBinaryFile(arCron[i].fileName);


                System.out.println("--> Loading data from "+arCron[i].fileName.getName());
                dataSnapShot=new hydroScalingAPI.io.DataRaster(metaLandUse).getDouble();

                hydroScalingAPI.util.statistics.Stats rainStats=new hydroScalingAPI.util.statistics.Stats(dataSnapShot,new Double(metaLandUse.getMissing()).doubleValue());
                System.out.println("    --> Stats of the File:  Max = "+rainStats.maxValue+" Min = "+rainStats.minValue+" Mean = "+rainStats.meanValue);

                //recorto la seccion que esta en la cuenca (TIENE QUE CONTENERLA)

                double demMinLon=metaDatos.getMinLon();
                double demMinLat=metaDatos.getMinLat();
                double demResLon=metaDatos.getResLon();
                double demResLat=metaDatos.getResLat();

                int basinMinX=myCuenca.getMinX();
                int basinMinY=myCuenca.getMinY();

                double LandUseMinLon=metaLandUse.getMinLon();
                double LandUseMinLat=metaLandUse.getMinLat();
                double LandUseResLon=metaLandUse.getResLon();
                double LandUseResLat=metaLandUse.getResLat();

                for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){

                    evalSpot=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
                                           demMinLat+(basinMinY+j-1)*demResLat/3600.0};

                    MatX=(int) Math.floor((evalSpot[0]-LandUseMinLon)/LandUseResLon*3600.0);
                    MatY=(int) Math.floor((evalSpot[1]-LandUseMinLat)/LandUseResLat*3600.0);

                    if (matrizPintada[j][k] > 0){
///////////////////// Adapt for land use ///////////////////
                        if(dataSnapShot[MatY][MatX]>10 && dataSnapShot[MatY][MatX]<20 ) currentHillBasedLandUse[matrizPintada[j][k]-1][0]++;
                        if(dataSnapShot[MatY][MatX]>20 && dataSnapShot[MatY][MatX]<30 ) currentHillBasedLandUse[matrizPintada[j][k]-1][1]++;
                        if(dataSnapShot[MatY][MatX]>30 && dataSnapShot[MatY][MatX]<40 ) currentHillBasedLandUse[matrizPintada[j][k]-1][2]++;
                        if(dataSnapShot[MatY][MatX]>40 && dataSnapShot[MatY][MatX]<50 ) currentHillBasedLandUse[matrizPintada[j][k]-1][3]++;
                        if(dataSnapShot[MatY][MatX]>50 && dataSnapShot[MatY][MatX]<60 ) currentHillBasedLandUse[matrizPintada[j][k]-1][4]++;
                        if(dataSnapShot[MatY][MatX]>60 && dataSnapShot[MatY][MatX]<70 ) currentHillBasedLandUse[matrizPintada[j][k]-1][5]++;
                        if(dataSnapShot[MatY][MatX]>70 && dataSnapShot[MatY][MatX]<80) currentHillBasedLandUse[matrizPintada[j][k]-1][6]++;
                        if(dataSnapShot[MatY][MatX]==82 ) currentHillBasedLandUse[matrizPintada[j][k]-1][7]++;
                        if(dataSnapShot[MatY][MatX]>80 && dataSnapShot[MatY][MatX]<90  && dataSnapShot[MatY][MatX]!=82 ) currentHillBasedLandUse[matrizPintada[j][k]-1][8]++;
                        if(dataSnapShot[MatY][MatX]>90 && dataSnapShot[MatY][MatX]<100 ) currentHillBasedLandUse[matrizPintada[j][k]-1][9]++;
                        if(dataSnapShot[MatY][MatX]==2000) currentHillBasedLandUse[matrizPintada[j][k]-1][2]++;
                        if(dataSnapShot[MatY][MatX]==101 ) currentHillBasedLandUse[matrizPintada[j][k]-1][10]++;

                        currentHillNumPixels[matrizPintada[j][k]-1]++;
                    }
                }

                for (int j=0;j<linksStructure.contactsArray.length;j++){
                   for (int n=0;n<nclasses;n++){
                        LandUseOnBasinArray[j][n]= (100*currentHillBasedLandUse[j][n]/currentHillNumPixels[j]);
                        MaxHillBasedLandUse[j]=0;
                        MaxHillBasedLandUsePerc[j]=0.0f;
                        }

                    for (int n=0;n<nclasses;n++){
                        if (LandUseOnBasinArray[j][n]>MaxHillBasedLandUsePerc[j]){
                            MaxHillBasedLandUsePerc[j]=LandUseOnBasinArray[j][n];
                            MaxHillBasedLandUse[j]=n;

                        }
                        if(MaxHillBasedLandUse[j]==-9) MaxHillBasedLandUse[j]=MaxHillBasedLandUse[j-1];
                    }
              }


                //System.out.println("-----------------Done with this snap-shot----------------");

            }

            System.out.println(metaLandUse.getLocationBinaryFile().getName().lastIndexOf("."));
            thisLandUseName=metaLandUse.getLocationBinaryFile().getName().substring(0,metaLandUse.getLocationBinaryFile().getName().lastIndexOf("."));

            success=true;

            System.out.println("-----------------Done with Files Reading----------------");

            recordResolutionInMinutes=metaLandUse.getTemporalScale()/1000.0/60.0;

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
        }
    }

    /**
     * Returns the value of rainfall rate in mm/h for a given moment of time
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
     * @param HillNumber The index of the desired hillslope
     * @return The average rainfall rate in mm/h
     */
//    public float getMeanLandUseOnHillslope(int HillNumber){
//
//        return LandUseOnBasin[HillNumber].getMeanRecord();
//
//    }

    /**
     *  A boolean flag indicating if the precipitation files were fully read
     * @return A flag for the constructor success
     */
    public boolean isCompleted(){
        return success;
    }

    /**
     * Returns the name of this LandUse event
     * @return A String that describes this LandUse
     */
    public String LandUseName(){
        return thisLandUseName;
    }

    /**
     * The LandUse temporal resolution in milliseconds
     * @return A float with the temporal resolution
     */
    public float LandUseRecordResolution(){

        return metaLandUse.getTemporalScale();

    }

    /**
     * The initial LandUse time as a {@link java.util.Calendar} object
     * @return A {@link java.util.Calendar} object indicating when the first drop of water fell
     * on the basin
     */
    public java.util.Calendar LandUseInitialTime(){

        return InfoDate;
    }

    /**
     * The initial LandUse time as a double in milliseconds obtained from the method getTimeInMillis()
     * of the {@link java.util.Calendar} object
     * @return A double indicating when the first drop of water fell over the basin
     * on the basin
     */
    public double LandUseInitialTimeInMinutes(){

        return InfoDate.getTimeInMillis()/1000./60.;
    }

    /**
     * The LandUse record time resolution in minutes
     * @return A double indicating the time series time step
     */
    public double LandUseRecordResolutionInMinutes(){

        return recordResolutionInMinutes;

    }

    /**
     * The total rainfall over a given pixel of the original raster fields
     * @param i The row number of the desired location
     * @param j The column number of the desired location
     * @return The accumulated rain over the entire LandUse period
     */
    public float getTotalPixelBasedLandUse(int i, int j){

        return totalPixelBasedLandUse[i][j];

    }

    /**
     * The
     * @param HillNumber The index of the desired hillslope
     * @return The value of Land cover
     */
    public int getMaxHillSlopeLU(int HillNumber){
        return MaxHillBasedLandUse[HillNumber];

    }
    public float getMaxHillSlopeLUPerc(int HillNumber){

        return MaxHillBasedLandUsePerc[HillNumber];

    }
    /**
     * Returns the number of files that were read
     * @return An integer
     */
    public int getNumberOfFilesRead(){

        return arCron.length;

    }

    /**
     * Returns a group of {@link java.util.Date} indicating the date associated to the files
     * @return An array of {@link java.util.Date}s
     */
    public java.util.Date[] getFilesDates(){

        java.util.Date[] filesDates=new java.util.Date[arCron.length];

        for (int i=0;i<arCron.length;i++){
            filesDates[i]=arCron[i].getDate().getTime();
        }

        return filesDates;

    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
    }

}
