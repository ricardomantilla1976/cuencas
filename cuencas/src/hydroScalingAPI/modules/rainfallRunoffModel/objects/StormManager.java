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
 * stormManager.java
 *
 * Created on July 10, 2002, 6:00 PM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import java.util.TimeZone;

/**
 * This class handles the precipitation over a basin.  It takes in a group of
 * raster files that represent snapshots of the rainfall fields and projects those
 * fields over the hillslope map to obtain hillslope-based rainfall time series.
 * @author Ricardo Mantilla
 */
public class StormManager {

    //private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[] precOnBasin,accumPrecOnBasin;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[] precOnBasin;
  
    private boolean success=false,veryFirstDrop=true;
    private hydroScalingAPI.io.MetaRaster metaStorm;
    private java.util.Calendar firstWaterDrop,lastWaterDrop;
    private float[][] totalPixelBasedPrec;
    private float[] totalHillBasedPrec;
    private float[] totalHillBasedPrecmm;
    private hydroScalingAPI.util.fileUtilities.ChronoFile[] arCron;

    int[][] matrizPintada;

    private double recordResolutionInMinutes;

    private String thisStormName;

    private int ncol;   // create
    private int nrow;   // create
    private double xllcorner;
    private double yllcorner;
    private double cellsize;

    private java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");

    /**
     * Creates a new instance of StormManager (with constant rainfall rate
     * over the basin during a given period of time)
     * @param linksStructure The topologic structure of the river network
     * @param rainIntensity The uniform intensity to be applied over the basinb
     * @param rainDuration The duration of the event with the given intensity
     */
    public StormManager(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, float rainIntensity, float rainDuration) {

        java.util.Calendar date=java.util.Calendar.getInstance();
        date.setTimeZone(tz);
        date.clear();
        date.set(1971, 6, 1, 0, 0, 0);
        //// CHECK RICARDO
        System.out.println("STORM INITIAL TIME" + date.getTimeInMillis());
      
        firstWaterDrop=date;
        //java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        firstWaterDrop.setTimeZone(tz); //// CHECK RICARDO - does it takes the attribute from date?
        lastWaterDrop=date;
        lastWaterDrop.setTimeZone(tz); //// CHECK RICARDO
        precOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.connectionsArray.length];
        //accumPrecOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
        for (int i=0;i<precOnBasin.length;i++){
            precOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries((int)(rainDuration*60*1000),1);
            precOnBasin[i].addDateAndValue(date,new Float(rainIntensity));
            ////// this is wrong, should be accumulated
            //accumPrecOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries((int)(rainDuration*60*1000),1);
            //accumPrecOnBasin[i].addDateAndValue(date,new Float(rainIntensity));

        }

        recordResolutionInMinutes=rainDuration;

        thisStormName="UniformEvent_INT_"+rainIntensity+"_DUR_"+rainDuration;

        success=true;

    }
    
     public StormManager(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, float rainIntensity, float rainDuration,java.util.Calendar date) {

        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        date.setTimeZone(tz);
        
          //// CHECK RICARDO
        System.out.println("STORM INITIAL TIME" + date.getTimeInMillis());
      
        firstWaterDrop=date;
        //java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        firstWaterDrop.setTimeZone(tz); //// CHECK RICARDO - does it takes the attribute from date?
        lastWaterDrop=date;
        lastWaterDrop.setTimeZone(tz); //// CHECK RICARDO
        precOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.connectionsArray.length];
        //accumPrecOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
        for (int i=0;i<precOnBasin.length;i++){
            precOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries((int)(rainDuration*60*1000),1);
            precOnBasin[i].addDateAndValue(date,new Float(rainIntensity));
            ////// this is wrong, should be accumulated
            //accumPrecOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries((int)(rainDuration*60*1000),1);
            //accumPrecOnBasin[i].addDateAndValue(date,new Float(rainIntensity));

        }

        recordResolutionInMinutes=rainDuration;

        thisStormName="UniformEvent_INT_"+rainIntensity+"_DUR_"+rainDuration;

        success=true;

    }

    /**
     * Creates a new instance of StormManager (with spatially and temporally variable rainfall
     * rates over the basin) based in a set of raster maps of rainfall intensities
     * @param locFile The path to the raster files
     * @param myCuenca The {@link hydroScalingAPI.util.geomorphology.objects.Basin} object describing the
     * basin under consideration
     * @param linksStructure The topologic structure of the river network
     * @param metaDatos A MetaRaster describing the rainfall intensity maps
     * @param matDir The directions matrix of the DEM that contains the basin
     * @param magnitudes The magnitudes matrix of the DEM that contains the basin
     */
    public StormManager(java.io.File locFile, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster metaDatos, byte[][] matDir, int[][] magnitudes) {

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

        //for (int i=0;i<lasQueSi.length;i++) {System.out.println("File list="+arCron[i].fileName.getName());}

        int[][] matDirBox=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];

        for (int i=1;i<matDirBox.length-1;i++) for (int j=1;j<matDirBox[0].length-1;j++){
            matDirBox[i][j]=(int) matDir[i+myCuenca.getMinY()-1][j+myCuenca.getMinX()-1];
        }

        try{

            metaStorm=new hydroScalingAPI.io.MetaRaster(locFile);
            nrow=metaStorm.getNumRows();
            ncol=metaStorm.getNumCols();
            xllcorner=metaStorm.getMinLon();
            yllcorner=metaStorm.getMinLat();
            cellsize=metaStorm.getResLat();


            /****** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA COBERTURA DEL MAPA SOBRE LA CUENCA)*****************/
            if (metaStorm.getMinLon() > metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0 ||
                metaStorm.getMinLat() > metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0 ||
                metaStorm.getMaxLon() < metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0 ||
                metaStorm.getMaxLat() < metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0) {
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

            precOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
            //accumPrecOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
            
            //////////////////////////////////////// stopped here - be sure accumulated is being calculated correctly
            
            int regInterval=metaStorm.getTemporalScale();
            float regIntervalmm=((float)metaStorm.getTemporalScale())/(1000.0f*60.0f);

            System.out.println("Time interval for this file: "+regInterval);

            totalHillBasedPrec=new float[precOnBasin.length];
            totalHillBasedPrecmm=new float[precOnBasin.length];

                double[] currentHillBasedPrec=new double[precOnBasin.length];
                float[] currentHillNumPixels=new float[precOnBasin.length];

                for (int i=0;i<precOnBasin.length;i++){
                precOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries(regInterval,arCron.length);
                //accumPrecOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries(regInterval,arCron.length);
                totalHillBasedPrecmm[i]=0.0f;
                currentHillBasedPrec[i]=0.0D;
                currentHillNumPixels[i]=0.0f;
                }

            double[] evalSpot;
            double [][] dataSnapShot, dataSection;
            int MatX,MatY;

            //System.out.println("-----------------Start of Files Reading----------------");

            totalPixelBasedPrec=new float[matDirBox.length][matDirBox[0].length];



            for (int i=0;i<arCron.length;i++){
                //Cargo cada uno
                metaStorm.setLocationBinaryFile(arCron[i].fileName);

                System.out.println("--> Loading data from "+arCron[i].fileName.getName());

                dataSnapShot=new hydroScalingAPI.io.DataRaster(metaStorm).getDouble();


                hydroScalingAPI.util.statistics.Stats rainStats=new hydroScalingAPI.util.statistics.Stats(dataSnapShot,new Double(metaStorm.getMissing()).doubleValue());
                System.out.println("    --> Stats of the File:  Max = "+rainStats.maxValue+" Min = "+rainStats.minValue+" Mean = "+rainStats.meanValue+" Time in Min = "+arCron[i].getDate().getTimeInMillis()/1000./60.);


                //recorto la seccion que esta en la cuenca (TIENE QUE CONTENERLA)


                double demMinLon=metaDatos.getMinLon();
                double demMinLat=metaDatos.getMinLat();
                double demResLon=metaDatos.getResLon();
                double demResLat=metaDatos.getResLat();

                int basinMinX=myCuenca.getMinX();
                int basinMinY=myCuenca.getMinY();

                double stormMinLon=metaStorm.getMinLon();
                double stormMinLat=metaStorm.getMinLat();
                double stormResLon=metaStorm.getResLon();
                double stormResLat=metaStorm.getResLat();

                for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
                    
                    evalSpot=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
                                           demMinLat+(basinMinY+j-1)*demResLat/3600.0};

                    MatX=(int) Math.floor((evalSpot[0]-stormMinLon)/stormResLon*3600.0);
                    MatY=(int) Math.floor((evalSpot[1]-stormMinLat)/stormResLat*3600.0);

                    if (matrizPintada[j][k] > 0){
                        currentHillBasedPrec[matrizPintada[j][k]-1]+=dataSnapShot[MatY][MatX];
                        currentHillNumPixels[matrizPintada[j][k]-1]++;
                    }

                    totalPixelBasedPrec[j][k]+=(float) dataSnapShot[MatY][MatX];

                }

                for (int j=0;j<linksStructure.contactsArray.length;j++){
                    if (currentHillBasedPrec[j] > 0) {
                        if (veryFirstDrop){
                            firstWaterDrop=arCron[i].getDate();
                            veryFirstDrop=false;
                            //System.out.println(" " +firstWaterDrop.getTime());
                        }

                        precOnBasin[j].addDateAndValue(arCron[i].getDate(),new Float(currentHillBasedPrec[j]/currentHillNumPixels[j])); //
                        totalHillBasedPrecmm[j]+=(currentHillBasedPrec[j]/currentHillNumPixels[j])*(regIntervalmm/60);
                        totalHillBasedPrec[j]+=currentHillBasedPrec[j]/currentHillNumPixels[j];
                        lastWaterDrop=arCron[i].getDate();
                    } else{totalHillBasedPrecmm[j]=0.0f;}
                    

                    //accumPrecOnBasin[j].addDateAndValue(arCron[i].getDate(),new Float(totalHillBasedPrecmm[j])); //
                    //System.out.println(arCron[i].getDate()+"Rain file " + i + "link " +j + "totalHillBasedPrecmm[j] = " + totalHillBasedPrecmm[j]);
                    currentHillBasedPrec[j]=0.0D;
                    currentHillNumPixels[j]=0.0f;
                }

                //System.out.println("-----------------Done with this snap-shot----------------");

            }

            for (int j=0;j<linksStructure.contactsArray.length;j++){
                totalHillBasedPrec[j]/=precOnBasin[j].getSize();
            }

            thisStormName=metaStorm.getLocationBinaryFile().getName().substring(0,metaStorm.getLocationBinaryFile().getName().lastIndexOf("."));

            success=true;


            System.out.println("-----------------Done with Files Reading----------------");


            recordResolutionInMinutes=metaStorm.getTemporalScale()/1000.0/60.0;

            if(lastWaterDrop == null){
                firstWaterDrop=arCron[0].getDate();
                lastWaterDrop=arCron[0].getDate();
                for (int j=0;j<linksStructure.contactsArray.length;j++){
                    precOnBasin[j].addDateAndValue(arCron[0].getDate(),0.0f); //
                    totalHillBasedPrec[j]=0;
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
    public float getPrecOnHillslope(int HillNumber,java.util.Calendar dateRequested){
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        dateRequested.setTimeZone(tz);  // CHECK RICARDO
        return precOnBasin[HillNumber].getRecord(dateRequested);

    }

       /**
     * Returns the value of rainfall rate in mm/h for a given moment of time
     * @param HillNumber The index of the desired hillslope
     * @param dateRequested The time for which the rain is desired
     * @return Returns the rainfall rate in mm/h
     */
    public double getAcumPrecOnHillslope(int HillNumber,java.util.Calendar dateRequested){


        return 1;//accumPrecOnBasin[HillNumber].getRecord(dateRequested);

//        double Acum=0.0f;
//        long dateRequestedMil=dateRequested.getTimeInMillis();
//        double timemin=dateRequestedMil/1000./60.;
//        double inc=stormRecordResolutionInMinutes();
//        java.util.Calendar currtime=java.util.Calendar.getInstance();
//        currtime.clear();
//        currtime.set(1971, 6, 1, 6, 0, 0);
//        currtime.setTimeInMillis(dateRequestedMil);
//        long j=0;
//        if (timemin==stormInitialTimeInMinutes()) Acum =0;
//        if (timemin>stormInitialTimeInMinutes()){
//           j=(long)stormInitialTimeInMinutes()*1000*60;
//           for (double i=stormInitialTimeInMinutes()+inc;i<=timemin;i=i+inc)
//           {
//               j=(long)i*1000*60;
//               currtime.setTimeInMillis(j);
//               Acum = Acum + precOnBasin[HillNumber].getRecord(currtime)*(inc/60);
//           }
//
//           long dif=dateRequestedMil-j;
//           currtime.setTimeInMillis(j);
//           Acum=Acum + precOnBasin[HillNumber].getRecord(currtime)*((dif/1000./60.)/60);
//        }
//        return Acum;

    }

    /**
     * Returns the maximum value of precipitation recorded for a given hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The maximum rainfall rate in mm/h
     */
    public float getMaxPrecOnHillslope(int HillNumber){

        return precOnBasin[HillNumber].getMaxRecord();

    }

    /**
     * Returns the maximum value of precipitation recorded for a given hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The average rainfall rate in mm/h
     */
    public float getMeanPrecOnHillslope(int HillNumber){

        return precOnBasin[HillNumber].getMeanRecord();

    }

    /**
     *  A boolean flag indicating if the precipitation files were fully read
     * @return A flag for the constructor success
     */
    public boolean isCompleted(){
        return success;
    }

    /**
     * Returns the name of this storm event
     * @return A String that describes this storm
     */
    public String stormName(){
        return thisStormName;
    }

    /**
     * The storm temporal resolution in milliseconds
     * @return A float with the temporal resolution
     */
    public float stormRecordResolution(){

        return metaStorm.getTemporalScale();

    }

    /**
     * The initial storm time as a {@link java.util.Calendar} object
     * @return A {@link java.util.Calendar} object indicating when the first drop of water fell
     * on the basin
     */
    public void setStormInitialTime(java.util.Calendar iniDate){
         java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        iniDate.setTimeZone(tz);  // CHECK RICARDO
        firstWaterDrop=iniDate;
    }
    public void setStormFinalTime(java.util.Calendar FinalDate){
        java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
        FinalDate.setTimeZone(tz);  // CHECK RICARDO
        
        lastWaterDrop=FinalDate;
    }
    /**
     * The initial storm time as a {@link java.util.Calendar} object
     * @return A {@link java.util.Calendar} object indicating when the first drop of water fell
     * on the basin
     */
    public java.util.Calendar stormInitialTime(){

        return firstWaterDrop;
    }

    /**
     * The initial storm time as a double in milliseconds obtained from the method getTimeInMillis()
     * of the {@link java.util.Calendar} object
     * @return A double indicating when the first drop of water fell over the basin
     * on the basin
     */
    public double stormInitialTimeInMinutes(){

        return firstWaterDrop.getTimeInMillis()/1000./60.;
    }

    /**
     * The final storm time as a double in milliseconds obtained from the method getTimeInMillis()
     * of the {@link java.util.Calendar} object
     * @return A double indicating when the last drop of water fell over the basin
     * on the basin
     */
    public double stormFinalTimeInMinutes(){
        return lastWaterDrop.getTimeInMillis()/1000./60.+stormRecordResolutionInMinutes();
    }

    /**
     * The storm record time resolution in minutes
     * @return A double indicating the time series time step
     */
    public double stormRecordResolutionInMinutes(){

        return recordResolutionInMinutes;

    }

    /**
     * The total rainfall over a given pixel of the original raster fields
     * @param i The row number of the desired location
     * @param j The column number of the desired location
     * @return The accumulated rain over the entire storm period
     */
    public float getTotalPixelBasedPrec(int i, int j){

        return totalPixelBasedPrec[i][j];

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
    public float getTotalHillSlopeBasedPrec(int HillNumber){

        return totalHillBasedPrec[HillNumber];

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
