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
 * CRasToGrass.java
 *
 * Created on September 21, 2003, 2:01 PM
 */

package hydroScalingAPI.io;

/**
 * This class takes a CUENCAS raster and creates a GRASS ascii raster inputFile, which can
 * be a DEM or a hydrometeorological field.  IMPORTANT:  The Class will write the
 * GRID inputFile using a gloabal LAT-LONG WGS-84 projection.
 * @author Ricardo Mantilla
 */
public class CRasToGrass {
    
    private hydroScalingAPI.io.MetaRaster myMetaInfo;
    private float[][] data;
    
    private java.io.File dirOut,fileName;
    
    /**
     * Creates a new instance of CRasToGrass
     * @param inputFile The MetaRaster for the file to be exported
     * @param outputDir The destination directory
     * @throws java.io.IOException Captures error in the read/write process
     */
    public CRasToGrass(java.io.File inputFile, java.io.File outputDir) throws java.io.IOException{
        
        dirOut=outputDir;
        fileName=inputFile;
        
        myMetaInfo=new hydroScalingAPI.io.MetaRaster(fileName);
    }
    
    /**
     * Provides the class with the path to the binary file to be exported using the
     * data from the MetaRaster provided in the constructor
     * @param inputFile Path to the binary file
     * @throws java.io.IOException Captures errors reading and/or writing
     */
    public void fileToExport(java.io.File inputFile) throws java.io.IOException{
        myMetaInfo.setLocationBinaryFile(inputFile);
        data=new hydroScalingAPI.io.DataRaster(myMetaInfo).getFloat();
    }
    
    /**
     * Takes in a new MetaRaster to use as template for the data to be exported
     * @param thisMetaInfo The MetaRaster that will be used to retrive binary data
     * @throws java.io.IOException Errors reading the data
     */
    public void fileToExport(hydroScalingAPI.io.MetaRaster thisMetaInfo) throws java.io.IOException{
        myMetaInfo=thisMetaInfo;
        data=new hydroScalingAPI.io.DataRaster(myMetaInfo).getFloat();
    }
    
    /**
     * Writes the data in GRASS ascii format
     * @throws java.io.IOException Captures errors while writing the ascii file
     */
    public void writeGrassFile() throws java.io.IOException{
        
        String fileAscSalida=dirOut.getPath()+"/"+myMetaInfo.getLocationBinaryFile().getName()+".grass";
        
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";
        
        outputDir = new java.io.FileOutputStream(fileAscSalida);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);
        
        int nc=myMetaInfo.getNumCols();
        int nr=myMetaInfo.getNumRows();
        
        float missing=Float.parseFloat(myMetaInfo.getMissing());
        
        newfile.write("north: "+myMetaInfo.getMaxLat()+retorno);
        newfile.write("south: "+myMetaInfo.getMinLat()+retorno);
        newfile.write("east: "+myMetaInfo.getMaxLon()+retorno);
        newfile.write("west: "+myMetaInfo.getMinLon()+retorno);
        newfile.write("rows: "+myMetaInfo.getNumRows()+retorno);
        newfile.write("cols: "+myMetaInfo.getNumCols()+retorno);

        
        for (int i=(nr-1);i>=0;i--) {
            for (int j=0;j<nc;j++) {
                if (data[i][j] == missing) {
                    newfile.write("* ");
                } else {
                    newfile.write(data[i][j]+" ");
                }
            }
            newfile.write(retorno);
        }
        
        newfile.close();
        bufferout.close();
        outputDir.close();
        
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
            CRasToGrass exporter=new CRasToGrass(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/WalnutCreek_KS/walnutCreek.metaDEM"),
                                               new java.io.File("/home/ricardo/temp/"));
            exporter.fileToExport(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/WalnutCreek_KS/walnutCreek.dem"));
            exporter.writeGrassFile();
            
            /*new GrassToHSJ(new java.io.File("/home/ricardo/garbage/testsGrass/1630327a.grass"),
                           new java.io.File("/home/ricardo/garbage/testsGrass/"));*/
           
        } catch (Exception IOE){
            System.out.print(IOE);
            IOE.printStackTrace();
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
}
