/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.dataAnalysis;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ricardo
 */


public class CalculateHydroPotential {
    
    private  String[]         metaInfo = new String[12];
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
    

    public CalculateHydroPotential() {
        
         try {
            java.io.File theFile=new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/precipitation/ppt_1971-2000.metaVHC");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/precipitation/ppt_1971-2000.vhc"));
            float [][] precip=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            double precip_minlat=metaModif.getMinLat();
            double precip_minlon=metaModif.getMinLon();
            double precip_res=metaModif.getResLon();
            long precip_num_rows=metaModif.getNumRows();
            long precip_num_cols=metaModif.getNumCols();
            
            
            double dy = 6378.0*metaModif.getResLat()*Math.PI/(3600.0*180.0);
            int nfila=metaModif.getNumRows();
            double[] dx = new double[nfila+1];
            double[] dxy = new double[nfila+1];

            /*Se calcula para cada fila del DEMC el valor de la distancia horizontal del pixel
              y la diagonal, dependiendo de la latitud.*/
            for (int i=1 ; i<=nfila ; i++){
                dx[i] = 6378.0*Math.cos((i*metaModif.getResLat()/3600.0 + metaModif.getMinLat())*Math.PI/180.0)*metaModif.getResLat()*Math.PI/(3600.0*180.0);
                dxy[i] = Math.sqrt(dx[i]*dx[i] + dy*dy);
            }
            
            theFile=new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/p_pet/p_pet.metaVHC");
            metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/p_pet/p_pet.vhc"));
            float [][] p_pet=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            theFile=new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Topography/120_ArcSec_GTOPO30/us_gtopo30.metaDEM");
            metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Topography/120_ArcSec_GTOPO30/us_gtopo30.dem"));
            float [][] dem=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            double dem_minlat=metaModif.getMinLat();
            double dem_minlon=metaModif.getMinLon();
            double dem_res=metaModif.getResLon();
            
            double totalPotentialEnergy=0.0;
            
            float[][] potentialEnergy=new float[p_pet.length][p_pet[0].length];
                    
            int i_ini=(int)((dem_minlat-precip_minlat)/(precip_res/3600.0));
            int j_ini=(int)((dem_minlon-precip_minlon)/(precip_res/3600.0));
            
            for (int i = 0; i < p_pet.length; i++) {
                 for (int j = 0; j < p_pet[0].length; j++) {
                     int i_dem=(int)(((double)i-i_ini)*precip_res/dem_res);  
                     int j_dem=(int)(((double)j-j_ini)*precip_res/dem_res);
                     if(i_dem <  dem.length && j_dem < dem[0].length){   
                        if(p_pet[i][j] != -9999 && precip[i][j] != -9999 && p_pet[i][j] > 0 && dem[i_dem][j_dem] != -9999){
                            double phi=1/p_pet[i][j];
                            double f_phi=(float)Math.pow(phi*(1-Math.exp(-phi))*Math.tanh(1/phi),0.5);
                            double runoff=(float)(precip[i][j]*(1-f_phi));

                            potentialEnergy[i][j]=(float)(runoff*(dx[i]*dy)*1000*9.8*dem[i_dem][j_dem]*1000/3600);
                            //potentialEnergy[i][j]=(float)(dem[i_dem][j_dem]);

   //                         System.out.println("i "+i);
   //                         System.out.println("j "+j);
   //                         System.out.println("p_pet[i][j] "+p_pet[i][j]);
   //                         System.out.println("precip[i][j] "+precip[i][j]);
   //                         System.out.println("phi "+phi);
   //                         System.out.println("f_phi "+f_phi);
   //                         System.out.println("runoff "+runoff);
   //                         System.out.println("dx[i] "+dx[i]);
   //                         System.out.println("dy "+dy);
   //                         System.out.println("i_dem "+i_dem);
   //                         System.out.println("j_dem "+j_dem);
   //                         System.out.println("dem[i_dem][j_dem] "+dem[i_dem][j_dem]);
   //                         System.out.println("potentialEnergy[i][j] "+potentialEnergy[i][j]);

                            totalPotentialEnergy+=potentialEnergy[i][j]/1E12;

                        }
                     }
                 }
             }
            
            System.out.println("totalPotentialEnergy "+totalPotentialEnergy);
            
            java.io.FileOutputStream        outputDir;
            java.io.DataOutputStream        newfile;
            java.io.BufferedOutputStream    bufferout;

            outputDir = new FileOutputStream(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/potentialEnergy/PotentialEnergy.vhc"));
            bufferout=new BufferedOutputStream(outputDir);
            newfile=new DataOutputStream(bufferout);

            for (int i=0;i<potentialEnergy.length;i++) for (int j=0;j<potentialEnergy[0].length;j++) {
                if(potentialEnergy[i][j] > 0) 
                    newfile.writeFloat(potentialEnergy[i][j]/(float)1E9);
                else
                    newfile.writeFloat(-9999);
            }
            newfile.close();
            bufferout.close();
            outputDir.close();
            
            String                          retorno="\n";
            
            metaInfo[0] = "This is an estimate of potential energy for Hydro Kinetic";
        
            String minlat = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(precip_minlat).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LATITUDE);
            metaInfo[1] = minlat;

            String minlon = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(precip_minlon).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LONGITUDE);
            metaInfo[2] = minlon;

            String cellsize = ""+precip_res;
            metaInfo[3] = cellsize;
            metaInfo[4] = cellsize;
            metaInfo[5] = ""+precip_num_cols;
            metaInfo[6] = ""+precip_num_rows;
            metaInfo[7] = "float";
            metaInfo[8] = "-9999";
            metaInfo[9] = "fix";
            metaInfo[10] = "N/A";
            metaInfo[11] = "Generated by Ricardo's Calculation";
        
            outputDir = new FileOutputStream(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/potentialEnergy/PotentialEnergy.metaVHC"));
            bufferout=new BufferedOutputStream(outputDir);
            java.io.OutputStreamWriter newfile2=new OutputStreamWriter(bufferout);

            for (int i=0;i<12;i++) {
                newfile2.write(parameters[i],0,parameters[i].length());
                newfile2.write(""+retorno,0,1);
                newfile2.write(metaInfo[i],0,metaInfo[i].length());
                newfile2.write(""+retorno,0,1);
                newfile2.write(""+retorno,0,1);
            }

            newfile2.close();
            bufferout.close();
            outputDir.close();
            
            
        } catch (IOException ex) {
            Logger.getLogger(CalculateSurfaceArea.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    
    
    
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new CalculateHydroPotential();
    }
}

