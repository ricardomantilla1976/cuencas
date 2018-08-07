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

package hydroScalingAPI.examples.io;

import hydroScalingAPI.modules.rainfallRunoffModel.objects.*;
import java.io.IOException;
import visad.VisADException;

/**
 * This class handles the precipitation over a basin.  It takes in a group of
 * raster files that represent snapshots of the rainfall fields and projects those
 * fields over the hillslope map to obtain hillslope-based rainfall time series.
 * @author Ricardo Mantilla
 */
public class StormConverterForParallelCode {

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



    public StormConverterForParallelCode(int x, int y, byte[][] matDir, int[][] magnitudes, hydroScalingAPI.io.MetaRaster metaDatos, java.io.File locFile) throws java.io.IOException{

        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        System.out.println("Loading Storm ...");
        
        
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

            int regInterval=metaStorm.getTemporalScale();
            float regIntervalmm=((float)metaStorm.getTemporalScale())/(1000.0f*60.0f);

            System.out.println("Time interval for this file: "+regInterval);

            totalHillBasedPrec=new float[linksStructure.tailsArray.length];
            totalHillBasedPrecmm=new float[linksStructure.tailsArray.length];

            double[] currentHillBasedPrec=new double[linksStructure.tailsArray.length];
            float[] currentHillNumPixels=new float[linksStructure.tailsArray.length];

            double[] evalSpot;
            double [][] dataSnapShot, dataSection;
            int MatX,MatY;

            System.out.println("-----------------Start of Files Reading----------------");

            totalPixelBasedPrec=new float[matDirBox.length][matDirBox[0].length];
            
            new java.io.File("/Volumes/Macintosh HD 3/ForScott/"+locFile.getParentFile().getParentFile().getName()+"/"+locFile.getParentFile().getName()).mkdirs();

            java.io.FileOutputStream        outputDir;
            java.io.DataOutputStream        newfile;
            java.io.BufferedOutputStream    bufferout;

            
            for (int i=0;i<arCron.length;i++){
                //Cargo cada uno
                metaStorm.setLocationBinaryFile(arCron[i].fileName);

                System.out.println("--> Loading data from "+arCron[i].fileName.getName());
                System.out.println("File-"+i);

                dataSnapShot=new hydroScalingAPI.io.DataRaster(metaStorm).getDouble();


                hydroScalingAPI.util.statistics.Stats rainStats=new hydroScalingAPI.util.statistics.Stats(dataSnapShot,new Double(metaStorm.getMissing()).doubleValue());
                //System.out.println("    --> Stats of the File:  Max = "+rainStats.maxValue+" Min = "+rainStats.minValue+" Mean = "+rainStats.meanValue);


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
                
                //if(i>=1512&&i<=7272){
                if(i>=8&&i<=38){
                
                    outputDir = new java.io.FileOutputStream("/Volumes/Macintosh HD 3/ForScott/"+locFile.getParentFile().getParentFile().getName()+"/"+locFile.getParentFile().getName()+"/file-"+i);
                    bufferout=new java.io.BufferedOutputStream(outputDir);
                    newfile=new java.io.DataOutputStream(bufferout);

                    for (int j=0;j<linksStructure.contactsArray.length;j++){
                        newfile.writeFloat((float)(currentHillBasedPrec[j]/currentHillNumPixels[j]));
                        currentHillBasedPrec[j]=0.0D;
                        currentHillNumPixels[j]=0.0f;
                    }

                    newfile.close();
                    bufferout.close();
                    outputDir.close();
                }

            }

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
        }
    }

    public static void main (String args[]) throws java.io.IOException, VisADException{
    
            main3(args);
    
    }
    
    public static void main3(String args[]) {
        
        
//        x: 2817, y: 713 ; Basin Code 05454300 Clear Creek near Coralville, IA
//        x: 2646, y: 762 ; Basin Code 05454220 Clear Creek near Oxford, IA
//        x: 2949, y: 741 ; Basin Code 05454000 Rapid Creek near Iowa City, IA
//        x: 2256, y: 876 ; Basin Code 05453100 Iowa River at Marengo, IA
//        x: 1312, y: 1112 ; Basin Code 05451700 Timber Creek near Marshalltown, IA
//        x: 2858, y: 742 ; Basin Code 05454090 Muddy Creek at Coralville, IA
//        x: 2115, y: 801 ; Basin Code 05453000 Big Bear Creek at Ladora, IA
//        x: 1765, y: 981 ; Basin Code 05451900 Richland Creek near Haven, IA
//        x: 1871, y: 903 ; Basin Code 05452200 Walnut Creek near Hartwick, IA
//        x: 2885, y: 690 ; Basin Code 05454500 Iowa River at Iowa City, IA
//        x: 2796, y: 629 ; Basin Code 05455100 Old Mans Creek near Iowa City, IA
//        x: 2958, y: 410 ; Basin Code 05455700 Iowa River near Lone Tree, IA
//        x: 3186, y: 392 ; Basin Code 05465000 Cedar River near Conesville, IA
//        x: 3316, y: 116 ; Basin Code 05465500 Iowa River at Wapello, IA
//        x: 2734, y: 1069 ; Basin Code 05464500 Cedar River at Cedar Rapids, IA
//        x: 1770, y: 1987 ; Basin Code 05458300 Cedar River at Waverly, IA
//        x: 2676, y: 465 ; Basin Code 05455500 English River at Kalona, IA
//        x: 2900, y: 768 ; Basin Code 05453520 Iowa River below Coralville Dam nr Coralville, IA
//        x: 1245, y: 1181 ; Basin Code 05451500 Iowa River at Marshalltown, IA
//        x: 951, y: 1479 ; Basin Code 05451210 South Fork Iowa River NE of New Providence, IA
//        x: 3113, y: 705 ; Basin Code 05464942 Hoover Cr at Hoover Nat Hist Site, West Branch, IA
//        x: 1978, y: 1403 ; Basin Code 05464220 Wolf Creek near Dysart, IA
//        x: 1779, y: 1591 ; Basin Code 05463500 Black Hawk Creek at Hudson, IA
//        x: 1932, y: 1695 ; Basin Code 05464000 Cedar River at Waterloo, IA
//        x: 1590, y: 1789 ; Basin Code 05463000 Beaver Creek at New Hartford, IA
//        x: 1682, y: 1858 ; Basin Code 05458900 West Fork Cedar River at Finchford, IA
//        x: 1634, y: 1956 ; Basin Code 05462000 Shell Rock River at Shell Rock, IA
//        x: 1775, y: 1879 ; Basin Code 05458500 Cedar River at Janesville, IA
//        x: 903, y: 2499 ; Basin Code 05459500 Winnebago River at Mason City, IA
//        x: 1526, y: 2376 ; Basin Code 05457700 Cedar River at Charles City, IA
//        x: 1730, y: 2341 ; Basin Code 05458000 Little Cedar River near Ionia, IA
//        x: 1164, y: 3066 ; Basin Code 05457000 Cedar River near Austin, MN

        
        try{
            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            java.io.File stormFile;
            stormFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LucianaSet/DataRicardo/PET/2010/IowaPET.metaVHC");
        
//            new StormConverterForParallelCode(2646, 762,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(2949, 741,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(2885, 690,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(2817, 713,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1312, 1112,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(2858, 742,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(2115, 801,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1765, 981,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1871, 903,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(2796, 629,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(2958, 410,matDirs,magnitudes,metaModif,stormFile);
            new StormConverterForParallelCode(2734, 1069,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1770, 1987,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(2256, 876,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(3186, 392,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(3316, 116,matDirs,magnitudes,metaModif,stormFile);
//
//
//            new StormConverterForParallelCode(2676, 465,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(2900, 768,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1245, 1181,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(951, 1479,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(3113, 705,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1978, 1403,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1779, 1591,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1932, 1695,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1590, 1789,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1682, 1858,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1634, 1956,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1775, 1879,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(903, 2499,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1526, 2376,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1730, 2341,matDirs,magnitudes,metaModif,stormFile);
//            new StormConverterForParallelCode(1164, 3066,matDirs,magnitudes,metaModif,stormFile);
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
    }

}
