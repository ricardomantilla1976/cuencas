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
 * simulationsRep3.java
 *
 * Created on December 6, 2001, 10:34 AM
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

/**
 *
 * @author Ricardo Mantilla
 * @version 
 */

import visad.*;
import java.io.*;

/**
 *
 * @author Peter Furey
 */
public class WidthFuncToFileFurey extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
   
            
    /** Creates new simulationsRep3 */
    public WidthFuncToFileFurey(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md) throws java.io.IOException, VisADException{
        matDir=direcc;
        metaDatos=md;
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        String output_path="/home/furey/Data/goodwin_ms/dem_data/";
        //String output_path="/home/furey/Data/walnutgulch_az/dem_data/";
        //String output_path="/home/furey/Data/walnut_ks/whitewater_sb/dem_data/";
        //String output_path="/home/furey/Data/kentuckyriver_ky/lower_kentucky/dem_data/" ;
        //String output_path="/Temp/" ;
        String filename="_wfs_meanlinklen.dat";  //binsize = .meanValue
        // For lower and upper kentucky : 
        // dat1 = 4500x0,4500x1, dat2 = x1,x2, dat3 = x2,x3, dat4 = x3,x4, dat5 = x4,x5, dat6 = x5,end, etc.
        //String filename="_wfs_minlinklen.dat";           //binsize = .minValue
        
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));
        
        java.io.File archivo=new java.io.File(output_path+demName+filename);
        System.out.println("Creating file : " +output_path+demName+filename);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(archivo);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.DataOutputStream newfile = new java.io.DataOutputStream(bufferout);
        
        
        System.out.println("Starting Width Functions Calculations");
        
        hydroScalingAPI.util.geomorphology.objects.Basin itsCuenca;
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis itsLinksStructure;
        
        // Here, metric==0 geometric, metric==1 topologic
        int thisX,thisY,metric=0,numBins;      
        float binsize;       
        double[][] laWFunc;
        
        RealType numLinks= RealType.getRealType("numLinks"), distanceToOut = RealType.getRealType("distanceToOut");
        FlatField vals_ff_W,hist;
        Linear1DSet binsSet;

        System.out.println("Number of Complete Streams ="+linksStructure.completeStreamLinksArray.length);
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //error occurred at i+1 = 4791.
        //for (int i=(4500*9);i<(4500*10);i++){
        //for (int i=(4500*10);i<linksStructure.completeStreamLinksArray.length;i++){
            
            if (linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]] > 1) {
                
                System.out.println("stream= "+(i+1)+", order= "+(int) thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]));
                thisX=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]%metaDatos.getNumCols();
                thisY=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]/metaDatos.getNumCols();
                //System.out.println(thisX);
                //System.out.println(thisY);
                newfile.writeInt((int) thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]));  // order
                newfile.writeInt(thisX);    // x location on DEM
                newfile.writeInt(thisY);    // y location on DEM                
                newfile.writeFloat((float) thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i]));  // upstream area
                             
                itsCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(thisX,thisY,matDir,metaDatos);
                itsLinksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(itsCuenca, metaDatos, matDir);
                float[][] wFunc=itsLinksStructure.getDistancesToOutlet();
                //System.out.println(wFunc[metric].length);
                java.util.Arrays.sort(wFunc[metric]);
               
                float[][] varValues=itsLinksStructure.getVarValues(1);
                binsize=new hydroScalingAPI.util.statistics.Stats(varValues).meanValue;
                //binsize=new hydroScalingAPI.tools.Stats(varValues).minValue;
                //System.out.println(binsize);
                newfile.writeFloat(binsize);   // binsize for the order-n basin
                
                float[][] gWFunc=new float[1][wFunc[metric].length];   // row = 1, cols = length
                for (int j=0;j<wFunc[metric].length;j++)
                    gWFunc[0][j]=(float) wFunc[metric][j];

                //For TESTING ...
                //if (i == 243) {
                //for (int j=0;j<gWFunc[0].length;j++) System.out.println(gWFunc[0][j]);
               // }
                
                vals_ff_W = new FlatField( new FunctionType(distanceToOut, numLinks), new Linear1DSet(distanceToOut,0,gWFunc[0].length,gWFunc[0].length));
                vals_ff_W.setSamples( gWFunc );

                numBins=(int) (gWFunc[0][gWFunc[0].length-1]/binsize)+1;
                //System.out.println("numbins= "+numBins);
                //newfile.writeInt((int) numBins);    // numBins for order_n basin
                
                if (numBins > 1) {
                    newfile.writeInt((int) numBins);    // numBins for order_n basin
                    binsSet = new Linear1DSet(numLinks,binsize, gWFunc[0][gWFunc[0].length-1]+binsize,numBins);  //bin number as distance
                    //System.out.println(binsSet);
                    
                    hist = visad.math.Histogram.makeHistogram(vals_ff_W, binsSet);  //bin number, and distance to outlet
                    laWFunc=hist.getValues();               // numbins x 1
                    //if (i == 243) System.out.println(laWFunc[0][0]);
                    //if (i == 315) System.out.println(laWFunc[0][0]);
                    //System.out.println(laWFunc.length);     // row = 1
                    //System.out.println(laWFunc[0].length);  // col = numbins
                    for (int j=0;j<laWFunc[0].length;j++){
                        //System.out.println((int)laWFunc[0][j]);
                        newfile.writeInt((int) laWFunc[0][j]);
                    } 
                }
                else System.out.println("   Bin=1 ... stream ignored");
            }
        }
        
        System.out.println("Termina calculo de WFs");
 
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina escritura de Resultados");
        
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
        try{
            
            //Real Rain on Walnut Gulch
            subMain1(args);
            
            //Multifractal Rain on Peano
            //subMain2(args);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        
    }
    
    public static void subMain1 (String args[]) throws java.io.IOException, VisADException {
        
        /* Goodwin Creek, MS ... */
        String topo_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM";
        //String topo_path="/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM";
        //String topo_path = "/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.metaDEM";
        //String topo_path = "/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/WalnutCreek_KS/walnutCreek.metaDEM";
        //String topo_path="/hidrosigDataBases/Kentucky_KY_database/Rasters/Topography/1_ArcSec_USGS/kentuckyRiver.metaDEM";
        
        java.io.File theFile=new java.io.File(topo_path);

        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        
        System.out.println("Beginning ...");
        
        /* OUTLET COORDS EXAMINED BELOW (x,y) ...
           Goodwin Creek, MS ...    (44,111)
           Walnut Gulch, AZ ...     (82,260)
           Whitewater, KS ...       (1063,496)
           Walnut Creek, KS ...     (1309, 312)
           Lower Kentucky, KY ...   (845,2595)
           Upper Kentucky, KY ...   (845,2596) */
        
        new WidthFuncToFileFurey(44,111,matDirs,magnitudes,metaModif);
        
    }
    
}
