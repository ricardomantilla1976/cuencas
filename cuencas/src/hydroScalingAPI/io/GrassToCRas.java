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

/**
 * This class takes GRASS ASCII raster file and creates a CUENCAS raster, which can
 * be DEM or hydrometeorological fields.  IMPORTANT:  The Class assumes the
 * original GRASS file is in the LAT-LONG projection.
 * @author Ricardo Mantilla
 * @author Matt Luck
 */
public class GrassToCRas extends Object {
    
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
     * Creates an instance of the GrassToCRas
     * @param type 0: Import a DEM (creates a *.metaDEM and a *.dem file)
     * 1: Import any other kind of field (creates a *.metaVHC and a *.vhc file)
     * @param inputFile The GRASS file to be imported
     * @param outputDir The directory where the CUENCAS Raster will be placed
     * @throws java.io.IOException Captures errors during the reading and/or wrtining process
     */
    public GrassToCRas(java.io.File inputFile, java.io.File outputDir, int type) throws java.io.IOException{
        
        java.io.FileReader         ruta;
        java.io.BufferedReader     buffer;

        java.util.StringTokenizer       tokens;             
        String                          linea=null,
                                        basura,nexttoken;
        
        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);
        
        linea = buffer.readLine();
        tokens = new StringTokenizer(linea);

        basura = tokens.nextToken();
        
        if (basura.equalsIgnoreCase("proj:")){
            
            variables[0] = tokens.nextToken(); 
        
            for (int i=1;i<8;i++) {
                linea = buffer.readLine();
                tokens = new StringTokenizer(linea);

                basura = tokens.nextToken();
                variables[i] = tokens.nextToken();
            }
            
            columns = Integer.parseInt(variables[6]);
            rows = Integer.parseInt(variables[7]);
            
        } else {
            
            variables[2] = tokens.nextToken(); 
            
            for (int i=3;i<8;i++) {
                linea = buffer.readLine();
                tokens = new StringTokenizer(linea);

                basura = tokens.nextToken();
                variables[i] = tokens.nextToken();
            }
            
            columns = Integer.parseInt(variables[7]);
            rows = Integer.parseInt(variables[6]);
            
        }
            
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
        
        calculaMetadatos(variables);
        newFileBinary(new java.io.File(fileBinoutputDir));
        newFileMetaInfo(new java.io.File(fileAscoutputDir));
    }
    
    private void newFileBinary(java.io.File inputFile) throws java.io.IOException{
        
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
    
    private void newFileMetaInfo(java.io.File inputFile) throws java.io.IOException{
        
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
    
    private void calculaMetadatos(String args[]) {
               
        String      nombre,minlat,minlon,esclat,esclon,
                    formato,faltante,esctemp,unidades,info;
        
        metaInfo[0] = "This file was created imported from "+ fileName;
        
        minlat = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(variables[3]).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LATITUDE);
        metaInfo[1] = minlat;
        
        minlon = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(variables[2]).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LONGITUDE);
        metaInfo[2] = minlon;
                
        esclon = String.valueOf(3600*(Float.parseFloat(variables[4])-Float.parseFloat(variables[5]))/(float)columns);
        metaInfo[3] = esclon;
        
        esclat = String.valueOf(3600*(Float.parseFloat(variables[2])-Float.parseFloat(variables[3]))/(float)rows);
        metaInfo[4] = esclat;
        
        metaInfo[5] = variables[7];       
        metaInfo[6] = variables[6];
        metaInfo[7] = "float";
        metaInfo[8] = "-9999";
        metaInfo[9] = "fix";
        metaInfo[10] = "N/A";
        metaInfo[11] = "Imported from Grass format";
    }
    
    /**
     * Test the class
     * @param args the command line arguments
     */
    public static void main (String args[]) {
        
        try{
            new GrassToCRas(new java.io.File("/Documents and Settings/ricardo/My Documents/temp/spearfish.grass"),
                           new java.io.File("/Documents and Settings/ricardo/My Documents/temp/"),0);
            
            /*new GrassToCRas(new java.io.File("/home/ricardo/garbage/testsGrass/1630327a.grass"),
                           new java.io.File("/home/ricardo/garbage/testsGrass/"));*/
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
    }

}
