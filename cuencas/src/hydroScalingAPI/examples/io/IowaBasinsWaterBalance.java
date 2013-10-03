/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.io;

import hydroScalingAPI.util.geomorphology.objects.LinksAnalysis;

/**
 *
 * @author ricardo
 */
public class IowaBasinsWaterBalance {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/evaporation/eva2.metaVHC");
            hydroScalingAPI.io.MetaRaster metaModif2=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif2.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/evaporation/eva2.vhc"));
            metaModif2.setFormat("Float");
            float[][] evaporation=new hydroScalingAPI.io.DataRaster(metaModif2).getFloat();

            double minEvaLon=metaModif2.getMinLon();
            double minEvaLat=metaModif2.getMinLat();
            double resEva=metaModif2.getResLon();

            theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/precipitation/precip2.metaVHC");
            hydroScalingAPI.io.MetaRaster metaModif1=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif1.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/precipitation/precip2.vhc"));
            metaModif1.setFormat("Float");
            float[][] rainfall=new hydroScalingAPI.io.DataRaster(metaModif1).getFloat();
            
            double minRainLon=metaModif1.getMinLon();
            double minRainLat=metaModif1.getMinLat();
            double resRain=metaModif1.getResLon();
            
            theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setFormat("Float");
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.areas"));
            float [][] matAreas=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();

            float [][] runoffCoefficients=new float[matAreas.length][matAreas[0].length];
            
            for (int ii = 0; ii < runoffCoefficients.length; ii++) {
                for (int jj = 0; jj < runoffCoefficients[0].length; jj++) {
                    
                    if(matDirs[ii][jj] >0){
                    
                        hydroScalingAPI.util.geomorphology.objects.Basin myBasin=new hydroScalingAPI.util.geomorphology.objects.Basin(jj,ii,matDirs,metaModif);

                        float[][] lonLatBasin=myBasin.getLonLatBasin();

                        float accumRain=0;
                        float accumEva=0;

                        for (int i = 0; i < lonLatBasin[0].length; i++) {
                            int x=(int)((lonLatBasin[0][i]-minRainLon)/resRain);
                            int y=(int)((lonLatBasin[0][i]-minRainLon)/resRain);
                            accumRain+=rainfall[y][x];

                            x=(int)((lonLatBasin[0][i]-minEvaLon)/resEva);
                            y=(int)((lonLatBasin[0][i]-minEvaLon)/resEva);
                            accumEva+=evaporation[y][x];

                        }
                        accumRain/=lonLatBasin[0].length;
                        accumEva/=lonLatBasin[0].length;

                        runoffCoefficients[ii][jj]=(accumRain-accumEva)/accumRain;

                        System.out.println("ii = "+ii+" jj = "+jj+" RC = "+runoffCoefficients[ii][jj]+" Discharge = "+3.1710E-5*(accumRain-accumEva)*matAreas[myBasin.getXYBasin()[1][0]][myBasin.getXYBasin()[0][0]]+" Rain = "+accumRain+" Evaporation = "+accumEva+" Area = "+matAreas[myBasin.getXYBasin()[1][0]][myBasin.getXYBasin()[0][0]]);
                    }
                }
            }
            
            String fileAscSalida="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/runoffCoeff.asc";
        
            java.io.FileOutputStream        outputDir;
            java.io.OutputStreamWriter      newfile;
            java.io.BufferedOutputStream    bufferout;
            String                          retorno="\n";

            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);

            int nc=metaModif.getNumCols();
            int nr=metaModif.getNumRows();

            float missing=Float.parseFloat(metaModif.getMissing());

            newfile.write("ncols         "+metaModif.getNumCols()+retorno);
            newfile.write("nrows         "+metaModif.getNumRows()+retorno);
            newfile.write("xllcorner     "+metaModif.getMinLon()+retorno);
            newfile.write("yllcorner     "+metaModif.getMinLat()+retorno);
            newfile.write("cellsize      "+(metaModif.getResLat()/3600.0D)+retorno);
            newfile.write("NODATA_value  "+"0"+retorno);


            for (int i=(nr-1);i>=0;i--) {
                for (int j=0;j<nc;j++) {
                    newfile.write(runoffCoefficients[i][j]+" ");
                }
                newfile.write(retorno);
            }

            newfile.close();
            bufferout.close();
            outputDir.close();
            
            System.exit(0);

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);
    }
}
