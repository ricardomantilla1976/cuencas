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
 * @author Pradeep Mandapaka & Ricardo Mantilla
 */
public class WidthFunctionPradeep extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    int x;
    int y;
    int[][] magnitudes;
    java.io.File outputDirectory;    
       
    public WidthFunctionPradeep(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, hydroScalingAPI.io.MetaRaster md, java.io.File outputDirectoryOR) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        outputDirectory=outputDirectoryOR;  
        
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        thisNetworkGeom.setWidthsHG(1.0f, 0.4f,0.0f);        
         
        String demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));      
        
        double[][] wfs=linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray,0);
       
        java.io.File theFile;        
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-1063_496"+".AreasWFS");
        System.out.println("Writing Width Functions - "+theFile);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
                newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+"\t");                        
//                newfile.write(linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]+"\t");
//                newfile.write(linksStructure.headsArray[linksStructure.completeStreamLinksArray[i]]+"\t");
//                newfile.write(linksStructure.tailsArray[linksStructure.completeStreamLinksArray[i]]+"\t");
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+"\t");
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+"\t");
                newfile.write(wfs[i].length+"\t");
                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+"\t");
                newfile.write("\n");
        }        
        newfile.close();
        bufferout.close();        
    }
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{ 
        //java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM");
        java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        //metaModif.setLocationBinaryFile(new java.io.File("/usr/home/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.dir"));
        metaModif.setLocationBinaryFile(new java.io.File("/usr/home/rmantill/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/Whitewaters.dir"));

        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

//        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".areas"));
//        metaModif.setFormat("Float");
//        float [][] areas=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
//
//        for (int i = 0; i < areas.length; i+=100) {
//            for (int j = 0; j < areas[0].length; j+=100) {
//                System.out.print(areas[i][j]+",");
//            }
//            System.out.println();
//        }
//
//        System.exit(0);
       
        //new WidthFunctionPradeep(7875, 1361,matDirs,magnitudes,metaModif,new java.io.File("/usr/home/rmantill/temp"));
        new WidthFunctionPradeep(1063,496,matDirs,magnitudes,metaModif,new java.io.File("/usr/home/rmantill/temp"));
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
