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

import java.io.*;
import java.util.*;
import hydroScalingAPI.tools.*;

/**
 * This class takes ESRI GRID raster file and creates a CUENCAS raster, which can
 * be DEM or hydrometeorological fields.  IMPORTANT:  The Class assumes the
 * original GRID file is in the LAT-LONG projection.
 * @author Ricardo Mantilla
 * @author Matt Luck
 */
public class EsriASCIIToCRas extends Object {
    
    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  int              columns,rows;
    private  String[][]       extensionPairs = {{"dem","metaDEM"},{"vhc","metaVHC"}};
    
    private  String[] parameters = {    "[Name]",
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
                                        "[Information]"};
    /**
     * Creates an instance of the EsriASCIIToCRas
     * @param inputFile The GRID file to be imported
     * @param outputDir The directory where the CUENCAS Raster will be placed
     * @param type 0: Import a DEM (creates a *.metaDEM and a *.dem file)
     * 1: Import any other kind of field (creates a *.metaVHC and a *.vhc file)
     * @throws java.io.IOException Captures errors during the reading and/or wrtining process
     */
    public EsriASCIIToCRas(java.io.File inputFile, java.io.File outputDir, int type) throws java.io.IOException {
        
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        
        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);
        
        for (int i=0;i<6;i++) {
            linea = buffer.readLine();
            tokens = new StringTokenizer(linea);
            
            while(tokens.hasMoreTokens()) variables[i] = tokens.nextToken();
            
        }
        
        columns = Integer.parseInt(variables[0]);
        rows = Integer.parseInt(variables[1]);
        
        matrix = new float[rows][columns];
        
        for (int i=0;i<rows;i++) {
            linea = buffer.readLine();
            tokens = new StringTokenizer(linea);
            for (int j=0;j<columns;j++) {
                try{
                    matrix[i][j] = new Float(tokens.nextToken()).floatValue();
                } catch (NumberFormatException NFE){
                    matrix[i][j] = -9999;
                }
            }
        }
        buffer.close();
        
        fileName=inputFile.getName();
        String fileBinoutputDir=outputDir.getPath()+"/"+fileName.substring(0,fileName.lastIndexOf("."))+"."+extensionPairs[type][0];
        String fileAscoutputDir=outputDir.getPath()+"/"+fileName.substring(0,fileName.lastIndexOf("."))+"."+extensionPairs[type][1];
        
        calculateMetadata(variables);
        newfilebinary(new java.io.File(fileBinoutputDir));
        newfileMetadata(new java.io.File(fileAscoutputDir));
    }
    
    private void newfilebinary(java.io.File inputFile) throws java.io.IOException{
        
        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;
        
        outputDir = new FileOutputStream(inputFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);
        
        for (int i=rows-1;i>-1;i--) for (int j=0;j<columns;j++) {
            newfile.writeFloat(matrix[i][j]);
        }
        newfile.close();
        bufferout.close();
        outputDir.close();
    }
    
    private void newfileMetadata(java.io.File inputFile) throws java.io.IOException{
        
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";
        
        outputDir = new FileOutputStream(inputFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new OutputStreamWriter(bufferout);
        
        for (int i=0;i<12;i++) {
            newfile.write(parameters[i],0,parameters[i].length());
            newfile.write(""+retorno,0,1);
            newfile.write(metaInfo[i],0,metaInfo[i].length());
            newfile.write(""+retorno,0,1);
            newfile.write(""+retorno,0,1);
        }
        
        newfile.close();
        bufferout.close();
        outputDir.close();
        
    }
    
    private void calculateMetadata(String args[]) {
        
        String nombre, minlat, minlon, cols, rows, cellsize, formato, faltante, esctemp, unidades, info;
        
        metaInfo[0] = "This file was created imported from "+ fileName;
        
        minlat = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(variables[3]).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LATITUDE);
        metaInfo[1] = minlat;
        
        minlon = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(variables[2]).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LONGITUDE);
        metaInfo[2] = minlon;
                
        cellsize = String.valueOf(3600 * (new Float(variables[4]).floatValue()));
        metaInfo[3] = cellsize;
        metaInfo[4] = cellsize;
        metaInfo[5] = variables[0];
        metaInfo[6] = variables[1];
        metaInfo[7] = "float";
        metaInfo[8] = variables[5];
        metaInfo[9] = "fix";
        metaInfo[10] = "N/A";
        metaInfo[11] = "Imported from Esri - Ascii format";
    }
    
    /**
     * Test the class features.
     * @param args The arguments are not used.
     */
    public static void main(String args[]) {

      
System.out.print("2meter");

           try {
            new EsriASCIIToCRas( new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/2meterc1cliped/2meterc1cliped.asc"),
                                new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/2meterc1cliped/"),0);

        } catch (Exception IOE){
            System.out.print(IOE);
            System.exit(0);
        }

System.out.print("3meter");
           try {
            new EsriASCIIToCRas( new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/3meterc1cliped/3meterc1cliped.asc"),
                                new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/3meterc1cliped/"),0);

        } catch (Exception IOE){
            System.out.print(IOE);
            System.exit(0);
        }




    }

}
