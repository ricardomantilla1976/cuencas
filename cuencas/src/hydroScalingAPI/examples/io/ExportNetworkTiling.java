/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.io;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ricardo
 */
public class ExportNetworkTiling {
    
    public ExportNetworkTiling() throws java.io.IOException{
        java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/Squaw Creek At Ames/NED_86024003.metaDEM");
        hydroScalingAPI.io.MetaRaster metaDatos=new hydroScalingAPI.io.MetaRaster(theFile);
        metaDatos.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/Squaw Creek At Ames/NED_86024003.dir"));

        metaDatos.setFormat("Byte");
        byte [][] matDir=new hydroScalingAPI.io.DataRaster(metaDatos).getByte();

        hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(1425, 349,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(laCuenca, metaDatos, matDir);
        hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRsnGen=new hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition(mylinksAnalysis);
        
        int[][] matrizPintada=laCuenca.getHillslopesMask(matDir, myRsnGen, 1);
        
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";

        String fileAscSalida="/Users/ricardo/temp/NED_86024003.maskHillslopes.asc";

        outputDir = new java.io.FileOutputStream(fileAscSalida);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);

        int nc=metaDatos.getNumCols();
        int nr=metaDatos.getNumRows();

        float missing=Float.parseFloat(metaDatos.getMissing());

        newfile.write("ncols         "+metaDatos.getNumCols()+retorno);
        newfile.write("nrows         "+metaDatos.getNumRows()+retorno);
        newfile.write("xllcorner     "+metaDatos.getMinLon()+retorno);
        newfile.write("yllcorner     "+metaDatos.getMinLat()+retorno);
        newfile.write("cellsize      "+(metaDatos.getResLat()/3600.0D)+retorno);
        newfile.write("NODATA_value  "+metaDatos.getMissing()+retorno);


        for (int i=(nr-1);i>=0;i--) {
            for (int j=0;j<nc;j++) {
                if (matrizPintada[i][j] == missing) {
                    newfile.write(metaDatos.getMissing()+" ");
                } else {
                    newfile.write(matrizPintada[i][j]+" ");
                }
            }
            newfile.write(retorno);
        }

        newfile.close();
        bufferout.close();
        outputDir.close();
        
    }

    /**
    * @param args the command line arguments
    */
    public static void main(String[] args) {
        try {
            new ExportNetworkTiling();
        } catch (IOException ex) {
            Logger.getLogger(ExportNetworkTiling.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}


