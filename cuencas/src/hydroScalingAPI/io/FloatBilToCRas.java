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
public class FloatBilToCRas {
    
    private java.io.File headerInputFile,dataInputFile;
    private hydroScalingAPI.util.fileUtilities.DotFilter myFiltro;
    
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
    public FloatBilToCRas(java.io.File inputDirectory[], java.io.File outputDirectory,int type) throws java.io.IOException {
        
        if(!checkDirectoryContents(inputDirectory)) {
            System.out.println(">> One or more of your directories contents are incomplete or incompatible");
            return;
        }
        
        double minMinLat=90,minMinLon=180,maxMaxLat=-90,maxMaxLon=-180, matRes=0;
        
        for(int ff=0;ff<inputDirectory.length;ff++){
            
            myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("hdr");
            headerInputFile=inputDirectory[ff].listFiles(myFiltro)[0];
            
            java.io.BufferedReader headerBuffer = new java.io.BufferedReader(new java.io.FileReader(headerInputFile));
            

            int nCols,nRows;
            double minLat,minLon,latRes,lonRes;

            nRows=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
            nCols=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
            minLon=Double.parseDouble(String.valueOf(headerBuffer.readLine().substring(9).trim()));
            minLat=Double.parseDouble(String.valueOf(headerBuffer.readLine().substring(9).trim()));

            latRes=Math.abs(Float.parseFloat(String.valueOf(headerBuffer.readLine().substring(8).trim())));
            lonRes=latRes;
            
            matRes=lonRes;

            headerBuffer.readLine();
            
            minMinLat=Math.min(minLat,minMinLat);
            minMinLon=Math.min(minLon,minMinLon);
            maxMaxLat=Math.max(minLat+nRows*latRes,maxMaxLat);
            maxMaxLon=Math.max(minLon+nCols*lonRes,maxMaxLon);

            headerBuffer.close();
 
        }
        
        int mosaicNRows=(int)((maxMaxLat-minMinLat)/matRes)+2;
        int mosaicNCols=(int)((maxMaxLon-minMinLon)/matRes)+2;
        
        float[][] mosaicMatrix=new float[mosaicNRows][mosaicNCols];
        
        int nCols,nRows,nBytes=1;
        double minLat,minLon,latRes,lonRes;
        String minLatStr,minLonStr,noData="-9999",byteOrderString;
    
        
        for(int ff=0;ff<inputDirectory.length;ff++){
            
            myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("hdr");
            headerInputFile=inputDirectory[ff].listFiles(myFiltro)[0];

            myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("flt");
            dataInputFile=inputDirectory[ff].listFiles(myFiltro)[0];
        
            java.io.BufferedReader headerBuffer = new java.io.BufferedReader(new java.io.FileReader(headerInputFile));
            java.io.DataInputStream dataBuffer = new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(dataInputFile)));


            nRows=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
            nCols=Integer.parseInt(String.valueOf(headerBuffer.readLine().substring(5).trim()));
            minLon=Double.parseDouble(String.valueOf(headerBuffer.readLine().substring(9).trim()));
            minLat=Double.parseDouble(String.valueOf(headerBuffer.readLine().substring(9).trim()));

            minLonStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLon,1);
            minLatStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minLat,0);

            latRes=Math.abs(Float.parseFloat(String.valueOf(headerBuffer.readLine().substring(8).trim())));
            lonRes=latRes;
            
            noData=String.valueOf(headerBuffer.readLine().substring(12).trim());
            
            byteOrderString=String.valueOf(headerBuffer.readLine().substring(9).trim());

            if(byteOrderString.equalsIgnoreCase("MSBFIRST")) nBytes=0;
            if(byteOrderString.equalsIgnoreCase("LSBFIRST")) nBytes=32;
            
            float[][] data=new float[nRows][nCols];
            
            int iOffset=(int)((minLat-minMinLat)/latRes);
            int jOffset=(int)((minLon-minMinLon)/lonRes);
            
            if (nBytes == 32){
                for (int i=0;i<nRows;i++){
                    for (int j=0;j<nCols;j++){
                        int accum = 0;
                        for ( int shiftBy=0; shiftBy<32; shiftBy+=8 )
                            {
                            accum |= ( dataBuffer.readByte () & 0xff ) << shiftBy;
                            }
                        data[i][j] = Float.intBitsToFloat( accum ); 
                        
                        mosaicMatrix[nRows-1-i+iOffset][j+jOffset]=data[i][j];
                    }
                }
            } 


            if (nBytes == 0){
                for (int i=0;i<nRows;i++){
                    for (int j=0;j<nCols;j++){
                        data[i][j] = (float)dataBuffer.readFloat();
                        mosaicMatrix[nRows-1-i+iOffset][j+jOffset]=data[i][j];
                    }
                }
            }

            dataBuffer.close();
            headerBuffer.close();
            
            if (nBytes == 1){
                System.out.println(">> Unknown ByteOrder Type");
                return;
            }

            String fileBinSalida=outputDirectory.getPath()+"/"+inputDirectory[ff].getName()+"."+extensionPairs[type][0];
            String fileAscSalida=outputDirectory.getPath()+"/"+inputDirectory[ff].getName()+"."+extensionPairs[type][1];

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
            metaBuffer.write(lonRes*3600.0f+"\n");
            metaBuffer.write("\n");
            metaBuffer.write(parameters[4]+"\n");
            metaBuffer.write(latRes*3600.0f+"\n");
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
            metaBuffer.write(noData+"\n");
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
        
        if(inputDirectory.length == 1) return;
        
        String minMinLonStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minMinLon,1);
        String minMinLatStr=hydroScalingAPI.tools.DegreesToDMS.getprettyString(minMinLat,0);
        
        String fileBinSalida=outputDirectory.getPath()+"/mosaic_"+inputDirectory[0].getName()+"."+extensionPairs[type][0];
        String fileAscSalida=outputDirectory.getPath()+"/mosaic_"+inputDirectory[0].getName()+"."+extensionPairs[type][1];

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
        metaBuffer.write(minMinLatStr+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[2]+"\n");
        metaBuffer.write(minMinLonStr+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[3]+"\n");
        metaBuffer.write(matRes*3600.0f+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[4]+"\n");
        metaBuffer.write(matRes*3600.0f+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[5]+"\n");
        metaBuffer.write(mosaicNCols+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[6]+"\n");
        metaBuffer.write(mosaicNRows+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[7]+"\n");
        metaBuffer.write("float"+"\n");
        metaBuffer.write("\n");
        metaBuffer.write(parameters[8]+"\n");
        metaBuffer.write(noData+"\n");
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

        for (int i=0;i<mosaicNRows;i++){
            for (int j=0;j<mosaicNCols;j++){
                rasterBuffer.writeFloat(mosaicMatrix[i][j]);
            }
        }

        rasterBuffer.close();
        
        
        
    }
    
    private boolean checkDirectoryContents(java.io.File inputDirectory[]){

        boolean checkDirsContents=true;
        
        
        for(int i=0;i<inputDirectory.length;i++){
            myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("hdr");
            java.io.File[] hdrQueSi=inputDirectory[i].listFiles(myFiltro);

            myFiltro=new hydroScalingAPI.util.fileUtilities.DotFilter("flt");
            java.io.File[] bilQueSi=inputDirectory[i].listFiles(myFiltro);

            checkDirsContents&=hdrQueSi.length > 0 && bilQueSi.length > 0;
        }
        
        return checkDirsContents;
        
    }
    
    /**
     * This method tests this class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            java.io.File[] dirs=new java.io.File[]{ new java.io.File("/Users/ricardo/Downloads/n10_w074_3arc_v2_bil/"),
                                                    };
            
            new hydroScalingAPI.io.FloatBilToCRas(dirs,
                                             new java.io.File("/CuencasDataBases/SierraNevadaSantaMarta/Rasters/Topography/"),0);
        }catch(java.io.IOException ioe){
            System.err.println("error");
            ioe.printStackTrace();
        }
        
    }

}
