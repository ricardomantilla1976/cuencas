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


package hydroScalingAPI.io;

/**
 * Takes a USGS BIL file and creates a CUENCAS-Raster
 * @author Ricardo Mantilla
 */
public class BilToCRas {
    
    private java.io.File headerInputFile,geoInputFile,dataInputFile;
    
    /**
     * The elements to be included in the MetaFile
     */
    public String[] parameters= { 
                                    "[Name]",
                                    "[Southernmost Latitude]",
                                    "[Westernmost Longitude]",
                                    "[Longitudinal Resolution (ArcSec)]",
                                    "[Latitudinal Resolution (ArcSec)]",
                                    "[# Columns]",
                                    "[# Rows]",
                                    "[Format]",
                                    "[Missing]",
                                    "[Temporal Resolution]",
                                    "[Units]",
                                    "[Information]"
                                };
    private  String[][]       extensionPairs = {{"dem","metaDEM"},{"vhc","metaVHC"}};
    /**
     * Creates a new instance of BilToCRas
     * @param inputDirectory The directory where the BIL files are located
     * @param outputDirectory The location where the CUENCAS-Raster will be writen
     * @param type 0 for DEM or 1 for VHC
     * @throws java.io.IOException Captures problems while reading or writing the file
     */
    public BilToCRas(java.io.File inputDirectory, java.io.File outputDirectory,int type) throws java.io.IOException {
        
        if(!checkDirectoryContents(inputDirectory)) return;
        
        java.io.BufferedReader headerBuffer = new java.io.BufferedReader(new java.io.FileReader(headerInputFile));
        java.io.BufferedReader geoBuffer = new java.io.BufferedReader(new java.io.FileReader(geoInputFile));
        java.io.DataInputStream dataBuffer = new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(dataInputFile)));
        
        int nCols,nRows,nBytes;
        
        headerBuffer.readLine();
        headerBuffer.readLine();

        nRows=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
        nCols=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
        headerBuffer.readLine();
        
        nBytes=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
        
        headerBuffer.close();

        double minLat,minLon,latRes,lonRes;
        String minLatStr,minLonStr;

        latRes=Math.abs(Float.parseFloat(String.valueOf(geoBuffer.readLine().trim()))*3600.0f);
        geoBuffer.readLine();
        geoBuffer.readLine();
        lonRes=Math.abs(Float.parseFloat(String.valueOf(geoBuffer.readLine().trim()))*3600.0f);
        
        minLon=Double.parseDouble(String.valueOf(geoBuffer.readLine().trim()))+lonRes/3600.;
        minLat=Double.parseDouble(String.valueOf(geoBuffer.readLine().trim()))-nRows*latRes/3600.0;
        
        minLonStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLon,1);
        minLatStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLat,0);
        
        geoBuffer.close();
        
        float[][] data=new float[nRows][nCols];
        
        if (nBytes == 16){
            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    //This is to swap to Little Endian
                    int low = dataBuffer.readByte() & 0xff;
                    int high = dataBuffer.readByte() & 0xff;
                    data[i][j] = (short)(high << 8 | low); 
                }
            }
        } 
        if (nBytes == 8){
            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    data[i][j] = (float)dataBuffer.readUnsignedByte(); 
                }
            }
        }
        
        dataBuffer.close();
        geoBuffer.close();
        headerBuffer.close();
        
        String fileBinSalida=outputDirectory.getPath()+"/"+inputDirectory.getName()+"."+extensionPairs[type][0];
        String fileAscSalida=outputDirectory.getPath()+"/"+inputDirectory.getName()+"."+extensionPairs[type][1];
        
        java.io.File outputMetaFile=new java.io.File(fileAscSalida);
        java.io.File outputBinaryFile=new java.io.File(fileBinSalida);
        
        java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
        java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));
        
        metaBuffer.write(parameters[0]+"\n");
        if(type == 0)
            metaBuffer.write("DEM from The National Map Seamless Data Distribution System"+"\n"); 
        else
            metaBuffer.write("Data from The National Map Seamless Data Distribution System"+"\n"); 
        metaBuffer.write("\n");
        metaBuffer.write(parameters[1]+"\n");
        metaBuffer.write(minLatStr+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[2]+"\n");
        metaBuffer.write(minLonStr+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[3]+"\n");
        metaBuffer.write(lonRes+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[4]+"\n");
        metaBuffer.write(latRes+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[5]+"\n");
        metaBuffer.write(nCols+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[6]+"\n");
        metaBuffer.write(nRows+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[7]+"\n");
        metaBuffer.write("float"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[8]+"\n");
        metaBuffer.write("-9999"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[9]+"\n");
        metaBuffer.write("fix"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[10]+"\n");
        metaBuffer.write("m"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[11]+"\n");
        if(type == 0)
            metaBuffer.write("This DEM comes from the USGS DEMs database."+"\n");
        else
            metaBuffer.write("This Data comes from the USGS DEMs database."+"\n"); 
        metaBuffer.write("\n");
        
        metaBuffer.close();
        
        for (int i=0;i<nRows;i++){
            for (int j=0;j<nCols;j++){
                rasterBuffer.writeFloat(data[nRows-i-1][j]);
            }
        }
        
        rasterBuffer.close();
        
    }
    
    private boolean checkDirectoryContents(java.io.File inputDirectory){
        
        hydroScalingAPI.util.fileUtilities.DotFilter myFiltro;
        
        myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("hdr");
        java.io.File[] hdrQueSi=inputDirectory.listFiles(myFiltro);
        
        myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("blw");
        java.io.File[] blwQueSi=inputDirectory.listFiles(myFiltro);

        myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("bil");
        java.io.File[] bilQueSi=inputDirectory.listFiles(myFiltro);
        
        if(hdrQueSi.length > 0 && blwQueSi.length > 0 && bilQueSi.length > 0){
        
            headerInputFile=hdrQueSi[0];
            geoInputFile=blwQueSi[0];
            dataInputFile=bilQueSi[0];
            
            return true;
        
        }
        return false;
        
    }
    
    /**
     * This method tests this class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            new hydroScalingAPI.io.BilToCRas(new java.io.File("/CuencasDataBases/Beatty Wash/NED_70848261/"),
                                             new java.io.File("/Users/ricardo/temp/"),0);
        }catch(java.io.IOException ioe){
            System.err.println("error");
            ioe.printStackTrace();
        }
        
    }

}
