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
 * SOILM040Manager.java
 *
 * Created on July 10, 2002, 6:00 PM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 * This class handles the SOILM040ipitation over a basin.  It takes in a group of
 * raster files that represent snapshots of the rainfall fields and projects those
 * fields over the hillslope map to obtain hillslope-based rainfall time series.
 * @author Ricardo Mantilla
 */
public class SoilMoistureManager {

    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[] SOILM040OnBasin;
    private boolean success=false,veryFirstDrop=true;
    private hydroScalingAPI.io.MetaRaster metaSOILM040;
    private java.util.Calendar firstSOILM040,lastSOILM040;
    private float[][] totalPixelBasedSOILM040;
    private float[] totalHillBasedSOILM040;
    private float[] totalHillBasedSOILM040mm;
    private hydroScalingAPI.util.fileUtilities.ChronoFile[] arCron;

    int[][] matrizPintada;

    private double recordResolutionInMinutes;

    private String thisSOILM040Name;

    private int ncol;   // create
    private int nrow;   // create
    private double xllcorner;
    private double yllcorner;
    private double cellsize;



    /**
     * Creates a new instance of SOILM040Manager (with constant rainfall rate
     * over the basin during a given period of time)
     * @param linksStructure The topologic structure of the river network
     * @param rainIntensity The uniform intensity to be applied over the basinb
     * @param rainDuration The duration of the event with the given intensity
     */
    public SoilMoistureManager(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, float rainIntensity, float rainDuration) {

        java.util.Calendar date=java.util.Calendar.getInstance();
        date.clear();
        date.set(1971, 6, 1, 6, 0, 0);

        firstSOILM040=date;
        lastSOILM040=date;

        SOILM040OnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.connectionsArray.length];
        //accumSOILM040OnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
        for (int i=0;i<SOILM040OnBasin.length;i++){
            SOILM040OnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries((int)(rainDuration*60*1000),1);
            SOILM040OnBasin[i].addDateAndValue(date,new Float(rainIntensity));
            ////// this is wrong, should be accumulated
            //accumSOILM040OnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries((int)(rainDuration*60*1000),1);
            //accumSOILM040OnBasin[i].addDateAndValue(date,new Float(rainIntensity));

        }

        recordResolutionInMinutes=rainDuration;

        thisSOILM040Name="UniformEvent_INT_"+rainIntensity+"_DUR_"+rainDuration;

        success=true;

    }

    /**
     * Creates a new instance of SOILM040Manager (with spatially and temporally variable rainfall
     * rates over the basin) based in a set of raster maps of rainfall intensities
     * @param locFile The path to the raster files
     * @param myCuenca The {@link hydroScalingAPI.util.geomorphology.objects.Basin} object describing the
     * basin under consideration
     * @param linksStructure The topologic structure of the river network
     * @param metaDatos A MetaRaster describing the rainfall intensity maps
     * @param matDir The directions matrix of the DEM that contains the basin
     * @param magnitudes The magnitudes matrix of the DEM that contains the basin
     */
    public SoilMoistureManager(java.io.File locFile, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster metaDatos, byte[][] matDir, int[][] magnitudes) {

        //System.out.println("locFile.getParentFile()" + locFile.getParentFile());
        int temp=locFile.getName().lastIndexOf(".");
        //System.out.println("temp"+temp+"locFile.getName..." + locFile.getName());
        java.io.File directorio=locFile.getParentFile();
        String baseName=locFile.getName().substring(0,locFile.getName().lastIndexOf("."));

        hydroScalingAPI.util.fileUtilities.NameDotFilter myFiltro=new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseName,"vhc");
        java.io.File[] lasQueSi=directorio.listFiles(myFiltro);

        arCron=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSi.length];

        for (int i=0;i<lasQueSi.length;i++) arCron[i]=new hydroScalingAPI.util.fileUtilities.ChronoFile(lasQueSi[i],baseName);

        java.util.Arrays.sort(arCron);

        //Una vez leidos los archivos:
        //Lleno la matriz de direcciones

   //     for (int i=0;i<lasQueSi.length;i++) {System.out.println("File list="+arCron[i].fileName.getName());}

        int[][] matDirBox=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];

        for (int i=1;i<matDirBox.length-1;i++) for (int j=1;j<matDirBox[0].length-1;j++){
            matDirBox[i][j]=(int) matDir[i+myCuenca.getMinY()-1][j+myCuenca.getMinX()-1];
        }

        try{

            metaSOILM040=new hydroScalingAPI.io.MetaRaster(locFile);
            nrow=metaSOILM040.getNumRows();
            ncol=metaSOILM040.getNumCols();
            xllcorner=metaSOILM040.getMinLon();
            yllcorner=metaSOILM040.getMinLat();
            cellsize=metaSOILM040.getResLat();


            /****** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA COBERTURA DEL MAPA SOBRE LA CUENCA)*****************/
            if (metaSOILM040.getMinLon() > metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0 ||
                metaSOILM040.getMinLat() > metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0 ||
                metaSOILM040.getMaxLon() < metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0 ||
                metaSOILM040.getMaxLat() < metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0) {
                    System.out.println("Not Area Coverage");
                    return;
            }

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            matrizPintada=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];

            int nc=metaDatos.getNumCols();

            for (int i=0;i<linksStructure.contactsArray.length;i++){
                if (linksStructure.magnitudeArray[i] < linksStructure.basinMagnitude){

                    xOulet=linksStructure.contactsArray[i]%nc;
                    yOulet=linksStructure.contactsArray[i]/nc;

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

            SOILM040OnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
            //accumSOILM040OnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
//////////////////////////////////////// stopped here - be sure accumulated is being calculated correctly
            int regInterval=metaSOILM040.getTemporalScale();
            float regIntervalmm=((float)metaSOILM040.getTemporalScale())/(1000.0f*60.0f);

            //System.out.println("Time interval for this file: "+regInterval);

            totalHillBasedSOILM040=new float[SOILM040OnBasin.length];
            totalHillBasedSOILM040mm=new float[SOILM040OnBasin.length];

                double[] currentHillBasedSOILM040=new double[SOILM040OnBasin.length];
                float[] currentHillNumPixels=new float[SOILM040OnBasin.length];

                for (int i=0;i<SOILM040OnBasin.length;i++){
                SOILM040OnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries(regInterval,arCron.length);
                //accumSOILM040OnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries(regInterval,arCron.length);
                totalHillBasedSOILM040mm[i]=0.0f;
                currentHillBasedSOILM040[i]=0.0D;
                currentHillNumPixels[i]=0.0f;
                }

            double[] evalSpot;
            double [][] dataSnapShot, dataSection;
            int MatX,MatY;

            System.out.println("-----------------Start of Files Reading----------------");

            totalPixelBasedSOILM040=new float[matDirBox.length][matDirBox[0].length];



            for (int i=0;i<arCron.length;i++){
                //Cargo cada uno
                metaSOILM040.setLocationBinaryFile(arCron[i].fileName);

                System.out.println("--> Loading data from "+arCron[i].fileName.getName());

                dataSnapShot=new hydroScalingAPI.io.DataRaster(metaSOILM040).getDouble();


                hydroScalingAPI.util.statistics.Stats rainStats=new hydroScalingAPI.util.statistics.Stats(dataSnapShot,new Double(metaSOILM040.getMissing()).doubleValue());
                System.out.println("    --> Stats of the File:  Max = "+rainStats.maxValue+" Min = "+rainStats.minValue+" Mean = "+rainStats.meanValue);


                //recorto la seccion que esta en la cuenca (TIENE QUE CONTENERLA)


                double demMinLon=metaDatos.getMinLon();
                double demMinLat=metaDatos.getMinLat();
                double demResLon=metaDatos.getResLon();
                double demResLat=metaDatos.getResLat();

                int basinMinX=myCuenca.getMinX();
                int basinMinY=myCuenca.getMinY();

                double SOILM040MinLon=metaSOILM040.getMinLon();
                double SOILM040MinLat=metaSOILM040.getMinLat();
                double SOILM040ResLon=metaSOILM040.getResLon();
                double SOILM040ResLat=metaSOILM040.getResLat();

                for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
                    
                    evalSpot=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
                                           demMinLat+(basinMinY+j-1)*demResLat/3600.0};

                    MatX=(int) Math.floor((evalSpot[0]-SOILM040MinLon)/SOILM040ResLon*3600.0);
                    MatY=(int) Math.floor((evalSpot[1]-SOILM040MinLat)/SOILM040ResLat*3600.0);

                    if (matrizPintada[j][k] > 0){
                        currentHillBasedSOILM040[matrizPintada[j][k]-1]+=dataSnapShot[MatY][MatX];
                        currentHillNumPixels[matrizPintada[j][k]-1]++;
                    }

                    totalPixelBasedSOILM040[j][k]+=(float) dataSnapShot[MatY][MatX];

                }

                for (int j=0;j<linksStructure.contactsArray.length;j++){
                    if (currentHillBasedSOILM040[j] > 0) {
                        if (veryFirstDrop){
                            firstSOILM040=arCron[i].getDate();
                            veryFirstDrop=false;
                        }
//System.out.println(arCron[i].getDate());
                        SOILM040OnBasin[j].addDateAndValue(arCron[i].getDate(),new Float(currentHillBasedSOILM040[j]/currentHillNumPixels[j])); //
                        totalHillBasedSOILM040mm[j]+=(currentHillBasedSOILM040[j]/currentHillNumPixels[j])*(regIntervalmm/60);
                        totalHillBasedSOILM040[j]+=currentHillBasedSOILM040[j]/currentHillNumPixels[j];
                        lastSOILM040=arCron[i].getDate();
                    } else{totalHillBasedSOILM040mm[j]=0.0f;}
                    

                    //accumSOILM040OnBasin[j].addDateAndValue(arCron[i].getDate(),new Float(totalHillBasedSOILM040mm[j])); //
       //             System.out.println(arCron[i].getDate()+"Rain file " + i + "link " +j + "totalHillBasedSOILM040mm[j] = " + totalHillBasedSOILM040mm[j]);
                    currentHillBasedSOILM040[j]=0.0D;
                    currentHillNumPixels[j]=0.0f;
                }

            //    System.out.println("-----------------Done with this snap-shot----------------");

            }

            for (int j=0;j<linksStructure.contactsArray.length;j++){
                totalHillBasedSOILM040[j]/=SOILM040OnBasin[j].getSize();
            }

            thisSOILM040Name=metaSOILM040.getLocationBinaryFile().getName().substring(0,metaSOILM040.getLocationBinaryFile().getName().lastIndexOf("."));

            success=true;


            System.out.println("-----------------Done with Files Reading----------------");


            recordResolutionInMinutes=metaSOILM040.getTemporalScale()/1000.0/60.0;

            if(lastSOILM040 == null){
                firstSOILM040=arCron[0].getDate();
                lastSOILM040=arCron[0].getDate();
                for (int j=0;j<linksStructure.contactsArray.length;j++){
                    SOILM040OnBasin[j].addDateAndValue(arCron[0].getDate(),0.0f); //
                    totalHillBasedSOILM040[j]=0;
                }
            }

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
    public float getSOILM040OnHillslope(int HillNumber,java.util.Calendar dateRequested){

        return SOILM040OnBasin[HillNumber].getRecord(dateRequested);

    }

       /**
     * Returns the value of rainfall rate in mm/h for a given moment of time
     * @param HillNumber The index of the desired hillslope
     * @param dateRequested The time for which the rain is desired
     * @return Returns the rainfall rate in mm/h
     */
//    public double getAcumSOILM040OnHillslope(int HillNumber,java.util.Calendar dateRequested){
//
//
//        return accumSOILM040OnBasin[HillNumber].getRecord(dateRequested);
//
////        double Acum=0.0f;
////        long dateRequestedMil=dateRequested.getTimeInMillis();
////        double timemin=dateRequestedMil/1000./60.;
////        double inc=SOILM040RecordResolutionInMinutes();
////        java.util.Calendar currtime=java.util.Calendar.getInstance();
////        currtime.clear();
////        currtime.set(1971, 6, 1, 6, 0, 0);
////        currtime.setTimeInMillis(dateRequestedMil);
////        long j=0;
////        if (timemin==SOILM040InitialTimeInMinutes()) Acum =0;
////        if (timemin>SOILM040InitialTimeInMinutes()){
////           j=(long)SOILM040InitialTimeInMinutes()*1000*60;
////           for (double i=SOILM040InitialTimeInMinutes()+inc;i<=timemin;i=i+inc)
////           {
////               j=(long)i*1000*60;
////               currtime.setTimeInMillis(j);
////               Acum = Acum + SOILM040OnBasin[HillNumber].getRecord(currtime)*(inc/60);
////           }
////
////           long dif=dateRequestedMil-j;
////           currtime.setTimeInMillis(j);
////           Acum=Acum + SOILM040OnBasin[HillNumber].getRecord(currtime)*((dif/1000./60.)/60);
////        }
////        return Acum;
//
//    }

    /**
     * Returns the maximum value of SOILM040ipitation recorded for a given hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The maximum rainfall rate in mm/h
     */
    public float getMaxSOILM040OnHillslope(int HillNumber){

        return SOILM040OnBasin[HillNumber].getMaxRecord();

    }

    /**
     * Returns the maximum value of SOILM040ipitation recorded for a given hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The average rainfall rate in mm/h
     */
    public float getMeanSOILM040OnHillslope(int HillNumber){

        return SOILM040OnBasin[HillNumber].getMeanRecord();

    }

    /**
     *  A boolean flag indicating if the SOILM040ipitation files were fully read
     * @return A flag for the constructor success
     */
    public boolean isCompleted(){
        return success;
    }

    /**
     * Returns the name of this SOILM040 event
     * @return A String that describes this SOILM040
     */
    public String SOILM040Name(){
        return thisSOILM040Name;
    }

    /**
     * The SOILM040 temporal resolution in milliseconds
     * @return A float with the temporal resolution
     */
    public float SOILM040RecordResolution(){

        return metaSOILM040.getTemporalScale();

    }

    /**
     * The initial SOILM040 time as a {@link java.util.Calendar} object
     * @return A {@link java.util.Calendar} object indicating when the first drop of water fell
     * on the basin
     */
    public void setSOILM040InitialTime(java.util.Calendar iniDate){

        firstSOILM040=iniDate;
    }

    /**
     * The initial SOILM040 time as a {@link java.util.Calendar} object
     * @return A {@link java.util.Calendar} object indicating when the first drop of water fell
     * on the basin
     */
    public java.util.Calendar SOILM040InitialTime(){

        return firstSOILM040;
    }

    /**
     * The initial SOILM040 time as a double in milliseconds obtained from the method getTimeInMillis()
     * of the {@link java.util.Calendar} object
     * @return A double indicating when the first drop of water fell over the basin
     * on the basin
     */
    public double SOILM040InitialTimeInMinutes(){

        return firstSOILM040.getTimeInMillis()/1000./60.;
    }

    /**
     * The final SOILM040 time as a double in milliseconds obtained from the method getTimeInMillis()
     * of the {@link java.util.Calendar} object
     * @return A double indicating when the last drop of water fell over the basin
     * on the basin
     */
    public double SOILM040FinalTimeInMinutes(){
        return lastSOILM040.getTimeInMillis()/1000./60.+SOILM040RecordResolutionInMinutes();
    }

    /**
     * The SOILM040 record time resolution in minutes
     * @return A double indicating the time series time step
     */
    public double SOILM040RecordResolutionInMinutes(){

        return recordResolutionInMinutes;

    }

    /**
     * The total rainfall over a given pixel of the original raster fields
     * @param i The row number of the desired location
     * @param j The column number of the desired location
     * @return The accumulated rain over the entire SOILM040 period
     */
    public float getTotalPixelBasedSOILM040(int i, int j){

        return totalPixelBasedSOILM040[i][j];

    }

      /**
     * The number of rows of the original raster
     * The number of col of the original raster
     * lat and long  of the original raster
     * cel resolution of the original raster Arcsec
     */
    public int getnrow(){return nrow;}
    public int getncol(){return ncol;}
    public double getxllcorner(){return xllcorner;}
    public double getyllcorner(){return yllcorner;}
    public double getcellsize(){return cellsize;}

    /**
     * The total rainfall over a given hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The value of rainfall intensity
     */
    public float getTotalHillSlopeBasedSOILM040(int HillNumber){

        return totalHillBasedSOILM040[HillNumber];

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
