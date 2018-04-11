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


package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class LonLatBasinDividePradeep extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private hydroScalingAPI.io.MetaRaster metaStorm;
    private byte[][] matDir;
    
    int x;
    int y;
    int[][] magnitudes;
    java.io.File outputDirectory;    
       
    public LonLatBasinDividePradeep(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, hydroScalingAPI.io.MetaRaster md, java.io.File outputDirectoryOR) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        outputDirectory=outputDirectoryOR;  
        
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
         
        String demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));      
        
        float[][] lonlatdivide=myCuenca.getLonLatBasinDivide();
        
        java.io.File stormFile;
//        stormFile=new java.io.File("C:/Documents and Settings/pmandapa/My Documents/ForCuencas/BinScUnifInter_0.50/prec.metaVHC");
        stormFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/simulated_events/FromPradeep/BinScUnif_25_5/prec.metaVHC");
        metaStorm=new hydroScalingAPI.io.MetaRaster(stormFile);        
       
        java.io.File theFile;        
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN"+".BasinDivide");
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        java.io.File theFile1;        
        theFile1=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN"+".BasinMask");
        java.io.FileOutputStream salida1 = new java.io.FileOutputStream(theFile1);
        java.io.BufferedOutputStream bufferout1 = new java.io.BufferedOutputStream(salida1);
        java.io.OutputStreamWriter newfile1 = new java.io.OutputStreamWriter(bufferout1);
        
        double sFileMinLon = metaStorm.getMinLon();
        double sFileMinLat = metaStorm.getMinLat();
        //System.out.println(metaStorm.getMaxLon()+","+metaStorm.getMaxLat());

        double x1,x2,y1,y2,sumx,sumy;
        int[] myArray = new int[360];
        int[][] data = new int[360][360];
        int cnt,jmin,jmax,k1;
        
        for (int i=0;i<360;i++){
            k1 = 0;
            for (int j=0;j<360;j++){
                data[i][j] = 0;
                x1 = sFileMinLon + i*metaStorm.getResLon()/3600.0;
                x2 = sFileMinLon + (i+1)*metaStorm.getResLon()/3600.0;
                y1 = sFileMinLat + j*metaStorm.getResLat()/3600.0;
                y2 = sFileMinLat + (j+1)*metaStorm.getResLat()/3600.0;
                sumx = 0.0; sumy = 0.0; cnt = 0;
                for (int k=0;k<lonlatdivide[0].length;k++){
                    if ((lonlatdivide[0][k] >= x1)&&(lonlatdivide[0][k] <= x2)&&(lonlatdivide[1][k] >= y1)&&(lonlatdivide[1][k] <= y2)){
                        sumx = sumx + lonlatdivide[0][k];
                        sumy = sumy + lonlatdivide[1][k];
                        cnt = cnt + 1;                        
                    }
                }
                if (cnt > 0){
                    newfile.write(sumx/cnt+"\t");
                    newfile.write(sumy/cnt+"\n");
                    myArray[k1] = j;
                    k1 = k1 + 1;
                }
            }
            if (k1 > 0){
                jmin = myArray[0]; jmax = myArray[0];
                for (int j=0;j<k1;j++){
                    if (myArray[j] < jmin){
                        jmin = myArray[j];
                    }
                    else {
                        jmin = jmin;                    
                    }
                    if (myArray[j] > jmax){
                        jmax = myArray[j];
                    }
                    else {
                        jmax = jmax;                    
                    } 
                }
                if (jmin!=0){
                    jmin = jmin - 1;
                }
                if (jmax!=359){
                    jmax = jmax + 1;
                }
                for (int j=jmin;j<=jmax;j++){
                    data[i][j] = 1;
                }
            }               
        }
        newfile.close();
        bufferout.close();  
        
        for (int j=0;j<360;j++){
            for (int i=0;i<360;i++){
                newfile1.write(data[i][j]+"\t");
            }
            newfile1.write("\n");
        }
        newfile1.close();
        bufferout1.close();  
              
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{ 
        java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcsec/IowaRiverAtIowaCity.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcsec/IowaRiverAtIowaCity.dir"));
        
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
       
        new LonLatBasinDividePradeep(6602,1539,matDirs,magnitudes,metaModif,new java.io.File("/usr/home/rmantill/temp"));
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }        
        System.exit(0);        
    }    
}
