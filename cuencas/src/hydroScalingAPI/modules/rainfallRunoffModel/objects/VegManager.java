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
 * SNOWManager.java
 *
 * Created on July 10, 2002, 6:00 PM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;
import java.util.TimeZone;
/**
 * This class handles the SNOWipitation over a basin.  It takes in a group of
 * raster files that represent snapshots of the rainfall fields and projects those
 * fields over the hillslope map to obtain hillslope-based rainfall time series.
 * @author Ricardo Mantilla
 */
public class VegManager {

    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[] SNOWCoverOnBasin;
    private boolean success=false,veryFirstDrop=true;
    private hydroScalingAPI.io.MetaRaster metaSNOWCover;
    private java.util.Calendar firstSnowCover,lastSnowCover;
    private float[][] totalPixelBasedSNOWCover;
    private float[] totalHillBasedSNOWCover;
    
    private hydroScalingAPI.util.fileUtilities.ChronoFile[] arCron;

    int[][] matrizPintada;

    private double recordResolutionInMinutes;

    private String thisSNOWCoverName;

    private int ncol;   // create
    private int nrow;   // create
    private double xllcorner;
    private double yllcorner;
    private double cellsize;



    /**
     * Creates a new instance of SNOWManager (with constant rainfall rate
     * over the basin during a given period of time)
     * @param linksStructure The topologic structure of the river network
     * @param rainIntensity The uniform intensity to be applied over the basinb
     * @param rainDuration The duration of the event with the given intensity
     */
    public VegManager(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, float rainIntensity, float rainDuration) {

        java.util.Calendar date=java.util.Calendar.getInstance();
        date.clear();
        date.set(1971, 6, 1, 6, 0, 0);

        firstSnowCover=date;
        lastSnowCover=date;

        SNOWCoverOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.connectionsArray.length];
        //accumSNOWCoverOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
        for (int i=0;i<SNOWCoverOnBasin.length;i++){
            SNOWCoverOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries((int)(rainDuration*60*1000),1);
            SNOWCoverOnBasin[i].addDateAndValue(date,new Float(rainIntensity));
            ////// this is wrong, should be accumulated
            //accumSNOWOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries((int)(rainDuration*60*1000),1);
            //accumSNOWOnBasin[i].addDateAndValue(date,new Float(rainIntensity));

        }

        recordResolutionInMinutes=rainDuration;

        thisSNOWCoverName="UniformEvent_INT_"+rainIntensity+"_DUR_"+rainDuration;

        success=true;

    }

    /**
     * Creates a new instance of SNOWManager (with spatially and temporally variable rainfall
     * rates over the basin) based in a set of raster maps of rainfall intensities
     * @param locFile The path to the raster files
     * @param myCuenca The {@link hydroScalingAPI.util.geomorphology.objects.Basin} object describing the
     * basin under consideration
     * @param linksStructure The topologic structure of the river network
     * @param metaDatos A MetaRaster describing the rainfall intensity maps
     * @param matDir The directions matrix of the DEM that contains the basin
     * @param magnitudes The magnitudes matrix of the DEM that contains the basin
     */
    public VegManager(java.io.File locFile, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster metaDatos, byte[][] matDir, int[][] magnitudes) {

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

            metaSNOWCover=new hydroScalingAPI.io.MetaRaster(locFile);
            nrow=metaSNOWCover.getNumRows();
            ncol=metaSNOWCover.getNumCols();
            xllcorner=metaSNOWCover.getMinLon();
            yllcorner=metaSNOWCover.getMinLat();
            cellsize=metaSNOWCover.getResLat();


            /****** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA COBERTURA DEL MAPA SOBRE LA CUENCA)*****************/
            if (metaSNOWCover.getMinLon() > metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0 ||
                metaSNOWCover.getMinLat() > metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0 ||
                metaSNOWCover.getMaxLon() < metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0 ||
                metaSNOWCover.getMaxLat() < metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0) {
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

            SNOWCoverOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
            //accumSNOWCoverOnBasin=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[linksStructure.tailsArray.length];
//////////////////////////////////////// stopped here - be sure accumulated is being calculated correctly
            int regInterval=metaSNOWCover.getTemporalScale();
            float regIntervalmm=((float)metaSNOWCover.getTemporalScale())/(1000.0f*60.0f);

            //System.out.println("Time interval for this file: "+regInterval);

            totalHillBasedSNOWCover=new float[SNOWCoverOnBasin.length];
            

                double[] currentHillBasedSNOWCover=new double[SNOWCoverOnBasin.length];
                float[] currentHillNumPixels=new float[SNOWCoverOnBasin.length];

                for (int i=0;i<SNOWCoverOnBasin.length;i++){
                SNOWCoverOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries(regInterval,arCron.length);
                //accumSNOWCoverOnBasin[i]=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries(regInterval,arCron.length);
                
                
                currentHillNumPixels[i]=0.0f;
                }

            double[] evalSpot;
            double [][] dataSnapShot, dataSection;
            int MatX,MatY;

            System.out.println("-----------------Start of Files Reading----------------");

            totalPixelBasedSNOWCover=new float[matDirBox.length][matDirBox[0].length];



            for (int i=0;i<arCron.length;i++){
                //Cargo cada uno
                metaSNOWCover.setLocationBinaryFile(arCron[i].fileName);

                //System.out.println("--> Loading data from "+arCron[i].fileName.getName());

                dataSnapShot=new hydroScalingAPI.io.DataRaster(metaSNOWCover).getDouble();


                hydroScalingAPI.util.statistics.Stats rainStats=new hydroScalingAPI.util.statistics.Stats(dataSnapShot,new Double(metaSNOWCover.getMissing()).doubleValue());
                //System.out.println("    --> Stats of the File:  Max = "+rainStats.maxValue+" Min = "+rainStats.minValue+" Mean = "+rainStats.meanValue);


                //recorto la seccion que esta en la cuenca (TIENE QUE CONTENERLA)


                double demMinLon=metaDatos.getMinLon();
                double demMinLat=metaDatos.getMinLat();
                double demResLon=metaDatos.getResLon();
                double demResLat=metaDatos.getResLat();

                int basinMinX=myCuenca.getMinX();
                int basinMinY=myCuenca.getMinY();

                double SNOWCoverMinLon=metaSNOWCover.getMinLon();
                double SNOWCoverMinLat=metaSNOWCover.getMinLat();
                double SNOWCoverResLon=metaSNOWCover.getResLon();
                double SNOWCoverResLat=metaSNOWCover.getResLat();

                for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
                    
                    evalSpot=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
                                           demMinLat+(basinMinY+j-1)*demResLat/3600.0};

                    MatX=(int) Math.floor((evalSpot[0]-SNOWCoverMinLon)/SNOWCoverResLon*3600.0);
                    MatY=(int) Math.floor((evalSpot[1]-SNOWCoverMinLat)/SNOWCoverResLat*3600.0);

                    if (matrizPintada[j][k] > 0){
                        currentHillBasedSNOWCover[matrizPintada[j][k]-1]+=dataSnapShot[MatY][MatX];
                        currentHillNumPixels[matrizPintada[j][k]-1]++;
                    }

                    totalPixelBasedSNOWCover[j][k]+=(float) dataSnapShot[MatY][MatX];

                }

                for (int j=0;j<linksStructure.contactsArray.length;j++){
                    if (currentHillBasedSNOWCover[j] > 0) {
                        if (veryFirstDrop){
                            firstSnowCover=arCron[i].getDate();
                            veryFirstDrop=false;
                        }
//System.out.println(arCron[i].getDate());
                        SNOWCoverOnBasin[j].addDateAndValue(arCron[i].getDate(),new Float(currentHillBasedSNOWCover[j]/currentHillNumPixels[j])); //
                        
                        totalHillBasedSNOWCover[j]+=currentHillBasedSNOWCover[j]/currentHillNumPixels[j];
                        lastSnowCover=arCron[i].getDate();
                    } 
                    

                    //accumSNOWOnBasin[j].addDateAndValue(arCron[i].getDate(),new Float(totalHillBasedSNOWmm[j])); //
       //             System.out.println(arCron[i].getDate()+"Rain file " + i + "link " +j + "totalHillBasedSNOWmm[j] = " + totalHillBasedSNOWmm[j]);
                    currentHillBasedSNOWCover[j]=0.0D;
                    currentHillNumPixels[j]=0.0f;
                }

            //    System.out.println("-----------------Done with this snap-shot----------------");

            }

            for (int j=0;j<linksStructure.contactsArray.length;j++){
                totalHillBasedSNOWCover[j]/=SNOWCoverOnBasin[j].getSize();
            }

            thisSNOWCoverName=metaSNOWCover.getLocationBinaryFile().getName().substring(0,metaSNOWCover.getLocationBinaryFile().getName().lastIndexOf("."));

            success=true;


            System.out.println("-----------------Done with Files Reading----------------");


            recordResolutionInMinutes=metaSNOWCover.getTemporalScale()/1000.0/60.0;

            if(lastSnowCover == null){
                firstSnowCover=arCron[0].getDate();
                lastSnowCover=arCron[0].getDate();
                for (int j=0;j<linksStructure.contactsArray.length;j++){
                    SNOWCoverOnBasin[j].addDateAndValue(arCron[0].getDate(),0.0f); //
                    totalHillBasedSNOWCover[j]=0;
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
    public float getSNOWCoverOnHillslope(int HillNumber,java.util.Calendar dateRequested){

        return SNOWCoverOnBasin[HillNumber].getRecord(dateRequested);

    }

       /**
     * Returns the value of rainfall rate in mm/h for a given moment of time
     * @param HillNumber The index of the desired hillslope
     * @param dateRequested The time for which the rain is desired
     * @return Returns the rainfall rate in mm/h
     */
//    public double getAcumSNOWOnHillslope(int HillNumber,java.util.Calendar dateRequested){
//
//
//        return accumSNOWOnBasin[HillNumber].getRecord(dateRequested);
//
////        double Acum=0.0f;
////        long dateRequestedMil=dateRequested.getTimeInMillis();
////        double timemin=dateRequestedMil/1000./60.;
////        double inc=SNOWRecordResolutionInMinutes();
////        java.util.Calendar currtime=java.util.Calendar.getInstance();
////        currtime.clear();
////        currtime.set(1971, 6, 1, 6, 0, 0);
////        currtime.setTimeInMillis(dateRequestedMil);
////        long j=0;
////        if (timemin==SNOWInitialTimeInMinutes()) Acum =0;
////        if (timemin>SNOWInitialTimeInMinutes()){
////           j=(long)SNOWInitialTimeInMinutes()*1000*60;
////           for (double i=SNOWInitialTimeInMinutes()+inc;i<=timemin;i=i+inc)
////           {
////               j=(long)i*1000*60;
////               currtime.setTimeInMillis(j);
////               Acum = Acum + SNOWOnBasin[HillNumber].getRecord(currtime)*(inc/60);
////           }
////
////           long dif=dateRequestedMil-j;
////           currtime.setTimeInMillis(j);
////           Acum=Acum + SNOWOnBasin[HillNumber].getRecord(currtime)*((dif/1000./60.)/60);
////        }
////        return Acum;
//
//    }

    /**
     * Returns the maximum value of SNOWipitation recorded for a given hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The maximum rainfall rate in mm/h
     */
    public float getMaxSNOWCoverOnHillslope(int HillNumber){

        return SNOWCoverOnBasin[HillNumber].getMaxRecord();

    }

    /**
     * Returns the maximum value of SNOWipitation recorded for a given hillslope
     * @param HillNumber The index of the desired hillslope
     * @return The average rainfall rate in mm/h
     */
    public float getMeanSNOWCoverOnHillslope(int HillNumber){

        return SNOWCoverOnBasin[HillNumber].getMeanRecord();

    }

    /**
     *  A boolean flag indicating if the SNOWipitation files were fully read
     * @return A flag for the constructor success
     */
    public boolean isCompleted(){
        return success;
    }

    /**
     * Returns the name of this SNOW event
     * @return A String that describes this SNOW
     */
    public String SNOWCoverName(){
        return thisSNOWCoverName;
    }

    /**
     * The SNOW temporal resolution in milliseconds
     * @return A float with the temporal resolution
     */
    public float SNOWCoverRecordResolution(){

        return metaSNOWCover.getTemporalScale();

    }

    /**
     * The initial SNOW time as a {@link java.util.Calendar} object
     * @return A {@link java.util.Calendar} object indicating when the first drop of water fell
     * on the basin
     */
    public void setSNOWCoverInitialTime(java.util.Calendar iniDate){

        firstSnowCover=iniDate;
    }

    /**
     * The initial SNOW time as a {@link java.util.Calendar} object
     * @return A {@link java.util.Calendar} object indicating when the first drop of water fell
     * on the basin
     */
    public java.util.Calendar SNOWInitialTime(){

        return firstSnowCover;
    }

    /**
     * The initial SNOW time as a double in milliseconds obtained from the method getTimeInMillis()
     * of the {@link java.util.Calendar} object
     * @return A double indicating when the first drop of water fell over the basin
     * on the basin
     */
    public double SNOWInitialTimeInMinutes(){

        return firstSnowCover.getTimeInMillis()/1000./60.;
    }

    /**
     * The final SNOW time as a double in milliseconds obtained from the method getTimeInMillis()
     * of the {@link java.util.Calendar} object
     * @return A double indicating when the last drop of water fell over the basin
     * on the basin
     */
    public double SNOWFinalTimeInMinutes(){
        return lastSnowCover.getTimeInMillis()/1000./60.+SNOWRecordResolutionInMinutes();
    }

    /**
     * The SNOW record time resolution in minutes
     * @return A double indicating the time series time step
     */
    public double SNOWRecordResolutionInMinutes(){

        return recordResolutionInMinutes;

    }

    /**
     * The total rainfall over a given pixel of the original raster fields
     * @param i The row number of the desired location
     * @param j The column number of the desired location
     * @return The accumulated rain over the entire SNOW period
     */
    public float getTotalPixelBasedSNOW(int i, int j){

        return totalPixelBasedSNOWCover[i][j];

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
    public float getTotalHillSlopeBasedSNOW(int HillNumber){

        return totalHillBasedSNOWCover[HillNumber];

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
