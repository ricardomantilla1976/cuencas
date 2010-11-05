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
 * This class estimates the average Curve Number for each hillslope based on Land Cover and
 * Soil information.
 * Land Cover is obtained from the National Map Seamless Server.
 * Soil data is taken from STATSCO or SSURGO Database.
 * This class handles the physical information over a basin.
 * It takes in a group of raster files (land cover and Soil data) and calculate the average
 * Curve Number for each hilsslope.
 *
 * @author Luciana Cunha
 * Adapted from LandUseManager.java(@author Ricardo Mantilla)
 */


public class Gen_MatrixPintada{

//    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopeTimeSeries[] LandUseOnBasin;

    private java.util.Calendar InfoDate;

    int[][] matrizPintada;

    private double recordResolutionInMinutes;

    private String OutputM;
    private String OutputL;
     /**
     * Creates a new instance of LandUseManager (with spatially and temporally variable rainfall
     * rates over the basin) based in a set of raster maps of rainfall intensities
     * @param LandUse The path to the Land Cover files
     * @param SoilPro The path to the Land Cover files
     * @param myCuenca The {@link hydroScalingAPI.util.geomorphology.objects.Basin} object describing the
     * basin under consideration
     * @param linksStructure The topologic structure of the river network
     * @param metaDatos A MetaRaster describing the rainfall intensity maps
     * @param matDir The directions matrix of the DEM that contains the basin
     * @param magnitudes The magnitudes matrix of the DEM that contains the basin
     */



    public Gen_MatrixPintada(String Outp,String Outl, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster metaDatos, byte[][] matDir,byte[][] HorLin, int[][] magnitudes) throws IOException {

        OutputM=Outp;
        OutputL=Outl;

        System.out.println("START THE SCS MANAGER \n");
        java.io.File OutputFileM=new java.io.File(OutputM);
         java.io.File OutputFileL=new java.io.File(OutputL);
        java.io.File directorio=OutputFileM.getParentFile();


        int[][] matDirBox=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        int[][] matHorLin=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];

        for (int i=1;i<matDirBox.length-1;i++) for (int j=1;j<matDirBox[0].length-1;j++){
            matDirBox[i][j]=(int) matDir[i+myCuenca.getMinY()-1][j+myCuenca.getMinX()-1];
        }


        for (int i=1;i<matDirBox.length-1;i++) for (int j=1;j<matDirBox[0].length-1;j++){
            matHorLin[i][j]=(int) HorLin[i+myCuenca.getMinY()-1][j+myCuenca.getMinX()-1];
        }

            /****** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA COBERTURA DEL MAPA SOBRE LA CUENCA)*****************/
            double minLonBasin= metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0;
            double minLatBasin = metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0;
            double maxLonBasin = metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0;
            double maxLatBasin = metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0;
            double res=metaDatos.getResLat();

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            matrizPintada=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];

            for (int i=0;i<linksStructure.contactsArray.length;i++){
                if (linksStructure.magnitudeArray[i] < linksStructure.basinMagnitude){
                //IF link's magnitude < Shreve's magnitude associated to the river network
                    xOulet=linksStructure.contactsArray[i]%metaDatos.getNumCols();
                    yOulet=linksStructure.contactsArray[i]/metaDatos.getNumCols();

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=linksStructure.contactsArray[i];
                    }
                } else {

                    xOulet=myCuenca.getXYBasin()[0][0];
                    yOulet=myCuenca.getXYBasin()[1][0];

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=linksStructure.contactsArray[i];
                    }
                }
            }
            System.out.println("linksStructure.contactsArray.length: "+linksStructure.contactsArray.length+"  linksStructure.tailsArray.length = "+linksStructure.tailsArray.length);

            double PixelCN;
            PixelCN=-9.9;

            double demMinLon=metaDatos.getMinLon();
            double demMinLat=metaDatos.getMinLat();
            double demResLon=metaDatos.getResLon();
            double demResLat=metaDatos.getResLat();

            int basinMinX=myCuenca.getMinX();
            int basinMinY=myCuenca.getMinY();

            newfileASC(OutputFileM,myCuenca,matrizPintada,metaDatos);
            newfileASC(OutputFileL,myCuenca,matHorLin,metaDatos);
           /*     for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
                     if (matrizPintada[j][k] > 0){
                        minLonBasin= metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0;
                        minLatBasin = metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0;

                        System.out.println("j "+j+"  k = "+k +"  matrizPintada[j][k] = "+matrizPintada[j][k]);
                    }
                }*/
    }


    private void newfileASC(java.io.File AscFile, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca,int[][] Finalmatrix,hydroScalingAPI.io.MetaRaster metaDatos) throws java.io.IOException{


        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";

        outputDir = new java.io.FileOutputStream(AscFile);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);

        int nlin= (myCuenca.getMaxY()-myCuenca.getMinY()+3);
        int ncol= (myCuenca.getMaxX()-myCuenca.getMinX()+3);

        double minLonBasin= metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0;
        double minLatBasin = metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0;
        double demResLat=metaDatos.getResLat()/3600;
        newfile.write("ncols "+ncol+retorno);
        newfile.write("nrows "+nlin+retorno);
        newfile.write("xllcorner "+minLonBasin+retorno);// Iowa river
        newfile.write("yllcorner "+minLatBasin+retorno);//Iowa river
        newfile.write("cellsize "+demResLat+retorno);
        newfile.write("NODATA_value  "+"-9"+retorno);

     //   System.out.println("OUTPUT PROGRAM - Fcolumns = " +Finalcolumns + "Frows = "+Finalrows);
       for (int i=(nlin-1);i>=0;i--) {
        for (int j=0;j<ncol;j++) {
                    newfile.write(Finalmatrix[i][j]+" ");
                }
            newfile.write(retorno);
           }

        newfile.close();
        bufferout.close();
        outputDir.close();
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
    * @param args the command line arguments
    */
    public static void main (String args[]) throws IOException {

        String pathinput = "/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec//";
        java.io.File theFile=new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".dir"));
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".horton"));
        formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] HortonLinks=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();




        String Dir="/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec//";
        String OutputM=Dir+"/MatrizPintadaCR.asc";
        String OutputL=Dir+"/HortonLinksCR.asc";
        new java.io.File(Dir+"/").mkdirs();
        int x=2734;
        int y=1069; //Cedar Rapids
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDirs,metaModif);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaModif, matDirs);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada Matrix;
        Matrix=new hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada(OutputM,OutputL, myCuenca, linksStructure, metaModif, matDirs, HortonLinks, magnitudes);

        // main basin

    }

}
