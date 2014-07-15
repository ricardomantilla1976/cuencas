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
import java.util.Random;
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
public class RandomLinkSelection {

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
    public RandomLinkSelection(int NL,hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster metaDatos, byte[][] matDir, byte[][] HorLin, int[][] magnitudes, double[][] DEM,byte [][] matDirs,hydroScalingAPI.io.MetaRaster metaModif,byte[][] HortonLinks) throws IOException {


        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        int ncols=metaModif.getNumCols();
        int nlin=metaModif.getNumRows();
        System.out.println("START RandomLinkSelection \n");
       System.out.println("ncols" + ncols +"nlin" + nlin);
        hydroScalingAPI.util.statistics.Stats rainStats = new hydroScalingAPI.util.statistics.Stats(DEM);
        int MaxOrder=linksStructure.basinOrder;
        
        int ncount=0;
        int[] ComplLinks=linksStructure.completeStreamLinksArray;
        int[] Contac=linksStructure.contactsArray;
        int[] Heads=linksStructure.headsArray;
       
        int NComplLinks=linksStructure.completeStreamLinksArray.length;
        int[] x = new int[NComplLinks];
        int[] y = new int[NComplLinks];
        int[] Order = new int[NComplLinks];
        float[][] LinkOrder = thisNetworkGeom.getLinkOrderArray();
        float[][] LinkArea = thisNetworkGeom.getUpStreamAreaArray();
        int[] NLinksOrder= new int[MaxOrder];
         Random diceRoller = new Random();
        for (int io = 1; io <= MaxOrder; io++) {
            NLinksOrder[io-1]=0;
        }
       
        for (int io = 0; io < NComplLinks; io++) {
            int Ord=(int)LinkOrder[0][io];
            NLinksOrder[Ord-1]=NLinksOrder[Ord-1]+1;
        }
        
       //// Generate for all complete orders
          for (int io = 1; io <NComplLinks; io++) {
            
                 
                  int NO=(int) LinkOrder[0][ComplLinks[io]];
                  float Area=LinkArea[0][ComplLinks[io]];
               
             
               x[ncount]=Contac[ComplLinks[io]]%ncols;
               y[ncount]=Contac[ComplLinks[io]]/ncols;
               System.out.println(" x[ncount]" + x[ncount] + " y[ncount] " + y[ncount]  );
               Order[ncount]=NO;
              
               String Dir = "/D:/usr/KANSAS/CUENCAS/Rasters/Topography/MatrizPint30Random2/";
               
               String OutputM = Dir + "/MP30_" + Area + "_"+ Heads[ComplLinks[io]] +"_"+ x[ncount] + "_" + y[ncount] + "_" + Order[ncount] + ".asc";
               String OutputL = Dir + "/HL30_" + Area + "_"+ Heads[ComplLinks[io]] +"_"+ x[ncount] + "_" + y[ncount] + "_" + Order[ncount] + ".asc";
               hydroScalingAPI.util.geomorphology.objects.Basin myCuenca2 = new hydroScalingAPI.util.geomorphology.objects.Basin(x[ncount], y[ncount], matDirs, metaModif);
               hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure2 = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca2, metaModif, matDirs);
               hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom2 = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure2);
               hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada Matrix;
               Matrix = new hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada(OutputM, myCuenca2, linksStructure2, metaModif, matDirs, HortonLinks, magnitudes, DEM);
               ncount=ncount+1;
           
        }         
        
       /// generate random
//        for (int io = 1; io <= MaxOrder; io++) {
//            int NLG=0;
//            if(NLinksOrder[io-1]<NL) NLG=NLinksOrder[io-1];
//            else NLG=NL;
//            for (int i = 1; i <= NL; i++) {
//                int NO=1000;
//                int randomInt = 5;
//                float Area=0;
//               while (NO!=io && ComplLinks[randomInt]!=0) {
//                  randomInt = diceRoller.nextInt(NComplLinks);
//                  NO=(int) LinkOrder[0][randomInt];
//                  Area=LinkArea[0][randomInt];
//               }
//               System.out.println("Heards[ComplLinks[randomInt]]"+ Heads[ComplLinks[randomInt]] +"Cont[ComplLinks[randomInt]]"+ Contac[ComplLinks[randomInt]]);
//               System.out.println("io " + io + " ncount " + ncount + " Area " + Area +" ComplLinks[randomInt] "+ComplLinks[randomInt]);
//               x[ncount]=Contac[ComplLinks[randomInt]]%ncols;
//               y[ncount]=Contac[ComplLinks[randomInt]]/ncols;
//               System.out.println(" x[ncount]" + x[ncount] + " y[ncount] " + y[ncount]  );
//                
//               Order[ncount]=NO;
//              
//               String Dir = "/D:/usr/KANSAS/CUENCAS/Rasters/Topography/MatrizPint30Random/";
//               
//               String OutputM = Dir + "/MP30_" + Area + "_"+ Heads[ComplLinks[randomInt]] +"_"+ x[ncount] + "_" + y[ncount] + "_" + Order[ncount] + ".asc";
//               String OutputL = Dir + "/HL30_" + Area + "_"+ Heads[ComplLinks[randomInt]] +"_"+ x[ncount] + "_" + y[ncount] + "_" + Order[ncount] + ".asc";
//               hydroScalingAPI.util.geomorphology.objects.Basin myCuenca2 = new hydroScalingAPI.util.geomorphology.objects.Basin(x[ncount], y[ncount], matDirs, metaModif);
//               hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure2 = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca2, metaModif, matDirs);
//               hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom2 = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure2);
//               hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada Matrix;
//               Matrix = new hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada(OutputM, OutputL, myCuenca2, linksStructure2, metaModif, matDirs, HortonLinks, magnitudes, DEM);
//               ComplLinks[randomInt]=0;
//               ncount=ncount+1;
//            }
//        }        
       
    }

   
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

       
        //
        int x = 1658;
        int y = 1278;
       
       
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDirs, metaModif);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaModif, matDirs);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);

   
         hydroScalingAPI.modules.rainfallRunoffModel.objects.RandomLinkSelection Matrix2;
         int NL=10;
         Matrix2 = new hydroScalingAPI.modules.rainfallRunoffModel.objects.RandomLinkSelection(NL, myCuenca,linksStructure, metaModif, matDirs, HortonLinks, magnitudes, DEM,matDirs, metaModif,HortonLinks);
       
    }
}
