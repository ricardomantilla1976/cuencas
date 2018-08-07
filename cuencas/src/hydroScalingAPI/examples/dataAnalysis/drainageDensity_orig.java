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
 * drainageDensity.java
 *
 * Created on June 3, 2004, 3:13 PM
 */

package hydroScalingAPI.examples.dataAnalysis;

import visad.*;
import java.util.*;

/**
 *
 * @author Matt Luck
 */
public class drainageDensity_orig {
    
    private hydroScalingAPI.io.MetaRaster metaData;
    private byte[][] matDir;
    
    /** Creates a new instance of drainageDensity */
    public drainageDensity_orig(int x, int y, byte[][] direcc, hydroScalingAPI.io.MetaRaster md) throws java.io.IOException{
        matDir=direcc;
        metaData=md;
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaData);
        //hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaData, matDir);
        
        
        /*
         *How to get addresses:
         *1) Find j-th link of Order 1.
         *2) Track its path to the outlet using linksStructure.nextLinkArray[j], then nextLinkArray[nextLinkArray[j]], then nextLinkArray[nextLinkArray[nextLinkArray[j]]], ... 
         *3) Save the orders as you move to the outlet
         *4) Remove repeted orders
         */
        
        
        java.io.File theFile=metaData.getLocationBinaryFile();
        metaData.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".areas"));
        metaData.setFormat("Float");
        float[][] areas=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        
        metaData.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".ltc"));
        metaData.setFormat("Float");
        float[][] lengths=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        
        metaData.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
        metaData.setFormat("Byte");
        int[][] orders=new hydroScalingAPI.io.DataRaster(metaData).getInt();
        
        java.io.File saveFile=new java.io.File("/home/ricardo/temp/"+theFile.getParentFile().getName()+" "+x+" "+y+".csv");
        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(saveFile));
        
        float[] LonLatBasin=new float[2];
        
        LonLatBasin[0]=(float) (x*metaData.getResLon()/3600.0f+metaData.getMinLon());
        LonLatBasin[1]=(float) (y*metaData.getResLat()/3600.0f+metaData.getMinLat());
        
        writer.println("Outlet Location: "+x+","+y+","+LonLatBasin[0]+","+LonLatBasin[1]);
        
        writer.println("A,TL,Omega,"+areas[y][x]+","+lengths[y][x]+","+orders[y][x]);
        
        /*int thisBasinOrder=linksStructure.getBasinOrder();
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            int thisX=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]%metaData.getNumCols();
            int thisY=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]/metaData.getNumCols();
            if(orders[thisY][thisX] == thisBasinOrder){
                writer.println("A,TL,Omega,"+areas[thisY][thisX]+","+lengths[thisY][thisX]+","+orders[thisY][thisX]);
            }
        }*/
        
        writer.println("Relief: "+myCuenca.getRelief());
        
        //New lines added to calculate average value of field over basin
        
        String path = "/hidrosigDataBases/Continental_US_database/Rasters/";
        
        String fieldfile[] = new String[4];
        fieldfile[0] = path + "Hydrology/precipitation/ppt_1971-2000";
        fieldfile[1] = path + "Hydrology/p_pet/p_pet";
        fieldfile[2] = path + "Hydrology/pet_swm/7488uswbm98_petsdrd0yr";
        fieldfile[3] = path + "Hydrology/sum_p_pet_i/sum_p_pet_i";
            
        
        for (int i=0; i<4; i++) {
            writer.println(fieldfile[i]);
        
            try {
                java.io.File rasterMeta = new java.io.File(fieldfile[i] + ".metaVHC");
                java.io.File rasterData = new java.io.File(fieldfile[i] + ".vhc");

                hydroScalingAPI.io.MetaRaster metaVHC=new hydroScalingAPI.io.MetaRaster(rasterMeta);
                metaVHC.setLocationBinaryFile(rasterData);
                float[][] datosDeAca=new hydroScalingAPI.io.DataRaster(metaVHC).getFloatLine();

                RealTupleType campo=new RealTupleType(RealType.Longitude,RealType.Latitude);

                Linear2DSet dominio = new Linear2DSet(campo,metaVHC.getMinLon()+metaVHC.getResLon()/3600.0/2.0,
                                                            metaVHC.getMinLon()+metaVHC.getNumCols()*metaVHC.getResLon()/3600.0-metaVHC.getResLon()/3600.0/2.0,
                                                            metaVHC.getNumCols(),
                                                            metaVHC.getMinLat()+metaVHC.getResLat()/3600.0/2.0,
                                                            metaVHC.getMinLat()+metaVHC.getNumRows()*metaVHC.getResLat()/3600.0-metaVHC.getResLat()/3600.0/2.0,
                                                            metaVHC.getNumRows());

                RealType varRaster=RealType.getRealType("varRaster");

                FunctionType funcionTransfer = new FunctionType( campo, varRaster);

                FlatField valores = new FlatField( funcionTransfer, dominio);

                valores.setSamples( datosDeAca, false );

                float[][] misCoordenadas;
                float accumValue=0;

                misCoordenadas=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaData).getLonLatBasin();

                for(int k=0;k<misCoordenadas[0].length;k++) {
                    accumValue+=Float.parseFloat(valores.evaluate(new RealTuple(campo, new double[] {misCoordenadas[0][k],misCoordenadas[1][k]})).toString());
                }

                accumValue/=(float)misCoordenadas[0].length;
                writer.println("value: " + accumValue);

            } catch (java.io.IOException IOE) {
                System.err.println(IOE);
            } catch (VisADException VisEx) {
                System.err.println(VisEx);
            }
        }
        
        writer.close();

        
        
    }
    
    /**
     * @param args the command line arguments
     */
    
    public static void main(String[] args) {
        
        //try {
            //main1(args);
            main2(args);

//        } catch(java.io.IOException IOE){
//            System.err.println(IOE);
//        }
        
    }
    public static void main1(String[] args) {
        
        StringTokenizer tokenizer = new StringTokenizer("B_26	1104	474	B_26");
        //StringTokenizer tokenizer = new StringTokenizer(args[0]);
        String path = "/hidrosigDataBases/Continental_US_database/Rasters/";
        String filepath = path + "Topography/Dd_Basins_30_ArcSec/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String filename = filepath + "/" + tokenizer.nextToken();
        
        try {
            java.io.File theFile=new java.io.File(filename + ".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(filename + ".dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            new drainageDensity_orig(x_outlet,y_outlet,matDirs,metaModif);

        } catch(java.io.IOException IOE){
            System.err.println(IOE);
        }
        
    }
    
    public static void main2(String[] args) {
        
        StringTokenizer tokenizer = new StringTokenizer("1_ArcSec	3272	1527	littleColorado");
        //StringTokenizer tokenizer = new StringTokenizer(args[0]);
        String path = "/hidrosigDataBases/Continental_US_database/Rasters/";
        String filepath = path + "Topography/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String filename = filepath + "/" + tokenizer.nextToken();
        
        try {
            java.io.File theFile=new java.io.File(filename + ".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(filename + ".dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            new drainageDensity_orig(x_outlet,y_outlet,matDirs,metaModif);

        } catch(java.io.IOException IOE){
            System.err.println(IOE);
        }
        
    }
}
