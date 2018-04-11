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
 * linkAddresses.java
 *
 * Created on June 10, 2005, 9:57 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package hydroScalingAPI.examples.dataAnalysis;

import visad.*;
import java.util.*;

/**
 *
 * @author Matt Luck
 */
public class linkAddresses {
    
    private hydroScalingAPI.io.MetaRaster metaData;
    
    /** Creates a new instance of Addresses */
    public linkAddresses(int x, int y, String path, String filename) throws java.io.IOException, VisADException {
        
        java.io.File theFile = new java.io.File(filename + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaData = new hydroScalingAPI.io.MetaRaster(theFile);
        
        String formatoOriginal = metaData.getFormat();
        metaData.setLocationBinaryFile(new java.io.File(filename + ".dir"));
        metaData.setFormat("Byte");
        byte[][] matDir = new hydroScalingAPI.io.DataRaster(metaData).getByte();
        
        metaData.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaData.setFormat("Integer");
        int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaData).getInt();
        
        theFile = metaData.getLocationBinaryFile();
        metaData.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf(".")) + ".areas"));
        metaData.setFormat("Float");
        float[][] areas = new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        
        metaData.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf(".")) + ".ltc"));
        metaData.setFormat("Float");
        float[][] lengths = new hydroScalingAPI.io.DataRaster(metaData).getFloat();
        
        metaData.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf(".")) + ".horton"));
        metaData.setFormat("Byte");
        int[][] orders = new hydroScalingAPI.io.DataRaster(metaData).getInt();
        
        // Set the basin: set file locations and get arrays of areas, lengths, orders
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaData);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaData, matDir);
        
        
        //Read the Precipitaion , evaporation, pet, fields.
        String tempfieldfile[] = new String[4];
        String fieldfile;
        FlatField[] valores=new FlatField[4];
        RealTupleType campo= new RealTupleType(RealType.Longitude,RealType.Latitude);
        
        for (int l=0; l<4; l++) {
            tempfieldfile[0] = path + "Hydrology/precipitation/ppt_1971-2000";
            tempfieldfile[1] = path + "Hydrology/p_pet/p_pet";
            tempfieldfile[2] = path + "Hydrology/pet_swm/7488uswbm98_petsdrd0yr";
            tempfieldfile[3] = path + "Hydrology/sum_p_pet_i/sum_p_pet_i";
            fieldfile = tempfieldfile[l];
            
            //New lines added to calculate average value of field over basin
            java.io.File rasterMeta = new java.io.File(fieldfile + ".metaVHC");
            java.io.File rasterData = new java.io.File(fieldfile + ".vhc");
            
            hydroScalingAPI.io.MetaRaster metaVHC=new hydroScalingAPI.io.MetaRaster(rasterMeta);
            metaVHC.setLocationBinaryFile(rasterData);
            float[][] datosDeAca = new hydroScalingAPI.io.DataRaster(metaVHC).getFloatLine();
            
            Linear2DSet dominio = new Linear2DSet(campo,metaVHC.getMinLon() + metaVHC.getResLon()/3600.0/2.0,
                                                        metaVHC.getMinLon() + metaVHC.getNumCols()*metaVHC.getResLon()/3600.0-metaVHC.getResLon()/3600.0/2.0,
                                                        metaVHC.getNumCols(),
                                                        metaVHC.getMinLat() + metaVHC.getResLat()/3600.0/2.0,
                                                        metaVHC.getMinLat() + metaVHC.getNumRows()*metaVHC.getResLat()/3600.0-metaVHC.getResLat()/3600.0/2.0,
                                                        metaVHC.getNumRows());
            
            RealType varRaster = RealType.getRealType("varRaster");
            
            FunctionType funcionTransfer = new FunctionType(campo, varRaster);
            
            valores[l] = new FlatField(funcionTransfer, dominio);
            
            valores[l].setSamples(datosDeAca, false);
            
        }
        
        // Set output file
        //        java.io.File saveFile = new java.io.File("/Users/matt/Research/DD/Dd_output/" + theFile.getName().substring(0,theFile.getName().lastIndexOf(".")) + "_addresses.csv");
        //        java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(saveFile));
        
        //        writer.println("link,x,y,currentOrder,area,length,order,P,P/PET,PET,'P-E'/10");
        
        // Iterate over all subbasins
        //        int[][] addressData = new int[16][linksStructure.completeStreamLinksArray.length];
        
        int thisBasinOrder=linksStructure.getBasinOrder();
        System.out.println("thisBasinOrder=" + thisBasinOrder);

        //        for (int i=0; i<linksStructure.completeStreamLinksArray.length; i++) {
        
        
        java.util.Hashtable averagesHash = new java.util.Hashtable();

        for (int i=0; i<linksStructure.completeStreamLinksArray.length; i++) {
            int thisX = linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]%metaData.getNumCols();
            int thisY = linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]/metaData.getNumCols();
            
            
            int j = linksStructure.completeStreamLinksArray[i];
            int k=0;
            
            // find all basins of order 1
            if (orders[thisY][thisX] == 1) {
                
                System.out.println("Step "+i+" Analyzing Link "+j);
                
                
                int currentOrder = orders[thisY][thisX];
                
                // follow path to outlet
                while (currentOrder != thisBasinOrder){
                    //search for next order
                    
                    k = linksStructure.nextLinkArray[j];

                    int nextThisX = linksStructure.contactsArray[k]%metaData.getNumCols();
                    int nextThisY = linksStructure.contactsArray[k]/metaData.getNumCols();
                    
                    if (currentOrder != orders[nextThisY][nextThisX]) {
                        
                        if(!averagesHash.containsKey(""+j)){
                        
                            //                        writer.print(i + "," + thisX + "," + thisY + "," + currentOrder + "," + areas[thisY][thisX] + "," + lengths[thisY][thisX] + "," + orders[thisY][thisX]);

                            String lineToPrint=j + ", " + thisX + ", " + thisY + ", " + currentOrder + ", " + areas[thisY][thisX] + ", " + lengths[thisY][thisX];

                            float[][] misCoordenadas;
                            //                      System.out.println(thisX + "," + thisY + "," + matDir + "," + metaData);
                            misCoordenadas = new hydroScalingAPI.util.geomorphology.objects.Basin(thisX,thisY,matDir,metaData).getLonLatBasin();
                            for (int l=0; l<4; l++) {

                                float accumValue=0;

                                for(int m=0; m<misCoordenadas[0].length; m++) {
                                    accumValue += Float.parseFloat(valores[l].evaluate(new RealTuple(campo, new double[] {misCoordenadas[0][m],misCoordenadas[1][m]})).toString());
                                }

                                accumValue /= (float)misCoordenadas[0].length;
                                lineToPrint+="," + accumValue;

                            }
                            System.out.println(lineToPrint);
                            if(currentOrder > 1) averagesHash.put(""+j, lineToPrint);

                        } else {
                            System.out.println(averagesHash.get(""+j).toString());
                        }
                        
                        
                    }

                    thisX = nextThisX;
                    thisY = nextThisY;
                    
                    
                    currentOrder = orders[thisY][thisX];
                    j = k;
                }
            }
            //                System.out.println(j + ", " + linksStructure.nextLinkArray[j] + ", " + orders[thisY][thisX]);
       }
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        //StringTokenizer tokenizer = new StringTokenizer(args[0]);
        StringTokenizer tokenizer = new StringTokenizer("B_26	1110	462	B_26");
        
        String path = "/hidrosigDataBases/Continental_US_database/Rasters/";
        String filepath = path + "Topography/Dd_Basins_30_ArcSec/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String filename = filepath + "/" + tokenizer.nextToken();
        
        try {
            new linkAddresses(x_outlet, y_outlet, path, filename);
        } catch (java.io.IOException ioe) {
            System.err.println(ioe);
        } catch (VisADException VisEx) {
            System.err.println(VisEx);
        }
    }
}
