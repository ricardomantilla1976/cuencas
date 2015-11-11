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


public class CalculateHydroKinetic {
    
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
    

    public CalculateHydroKinetic() {
        
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
            
            
            theFile=new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/p_pet/p_pet.metaVHC");
            metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/p_pet/p_pet.vhc"));
            float [][] p_pet=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            theFile=new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Topography/120_ArcSec_GTOPO30/us_gtopo30.metaDEM");
            metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Topography/120_ArcSec_GTOPO30/us_gtopo30.corrDEM"));
            metaModif.setFormat("Double");
            float [][] dem=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
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
            
            double dem_minlat=metaModif.getMinLat();
            double dem_minlon=metaModif.getMinLon();
            double dem_res=metaModif.getResLon();
            long dem_num_rows=metaModif.getNumRows();
            long dem_num_cols=metaModif.getNumCols();
            
            theFile=new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Topography/120_ArcSec_GTOPO30/us_gtopo30.metaDEM");
            metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Topography/120_ArcSec_GTOPO30/us_gtopo30.dir"));
            metaModif.setFormat("Byte");
            byte [][] dirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            //Mean Annual Flow map, calculated as accumulation of runoffs
            
            float[][] meanAnnualStreamflow=new float[dem.length][dem[0].length];
                    
            int i_ini=(int)((precip_minlat-dem_minlat)/(dem_res/3600.0));
            int j_ini=(int)((precip_minlon-dem_minlon)/(dem_res/3600.0));
            
            for (int i = 0; i < dem.length; i++) {
                 for (int j = 0; j < dem[0].length; j++) {
                     int i_precip=(int)(((double)i-i_ini)*dem_res/precip_res);  
                     int j_precip=(int)(((double)j-j_ini)*dem_res/precip_res);
                     
                     if(i_precip <  precip.length && j_precip < precip[0].length){   
                        if(p_pet[i_precip][j_precip] != -9999 && precip[i_precip][j_precip] != -9999 && p_pet[i_precip][j_precip] > 0 && dirs[i][j]>0){
                            double phi=1/p_pet[i_precip][j_precip];
                            double f_phi=(float)Math.pow(phi*(1-Math.exp(-phi))*Math.tanh(1/phi),0.5);
                            
                            meanAnnualStreamflow[i][j]=(float)((precip[i_precip][j_precip]*(1-f_phi))*dy*dx[i]*3.1710E-5);

                        }
                     }
                 }
            }
            
            runNetworkAccumulations(meanAnnualStreamflow,dirs);
            
            float[][] flowTimesHeight=new float[dem.length][dem[0].length];
            
            for (int i = 0; i < dem.length; i++) {
                 for (int j = 0; j < dem[0].length; j++) {
                     int i_precip=(int)(((double)i-i_ini)*dem_res/precip_res);  
                     int j_precip=(int)(((double)j-j_ini)*dem_res/precip_res);
                     
                     if(i_precip <  precip.length && j_precip < precip[0].length){   
                        if(p_pet[i_precip][j_precip] != -9999 && precip[i_precip][j_precip] != -9999 && p_pet[i_precip][j_precip] > 0 && dirs[i][j]>0){
                            double phi=1/p_pet[i_precip][j_precip];
                            double f_phi=(float)Math.pow(phi*(1-Math.exp(-phi))*Math.tanh(1/phi),0.5);
                            
                            flowTimesHeight[i][j]=(float)((precip[i_precip][j_precip]*(1-f_phi))*dy*dx[i]*3.1710E-5*dem[i][j]);

                        }
                     }
                 }
            }
            
            runNetworkAccumulations(flowTimesHeight,dirs);
            
            float[][] cumulativePotentialEnergy=new float[dem.length][dem[0].length];
            for (int i = 0; i < dem.length; i++) {
                 for (int j = 0; j < dem[0].length; j++) {
                     cumulativePotentialEnergy[i][j]=flowTimesHeight[i][j]-meanAnnualStreamflow[i][j]*dem[i][j];
                 }
            }
            
            float[][] localKineticEnergy=new float[dem.length][dem[0].length];
            float[][] cumulativeKineticEnergy=new float[dem.length][dem[0].length];
            
            for (int i = 0; i < dem.length; i++) {
                 for (int j = 0; j < dem[0].length; j++) {
                        int x1=i-1+(dirs[i][j]-1)/3 ; int y1=j-1+(dirs[i][j]-1)%3;
                        float drop=0;
                        if(dem[i][j] != -9999 && dem[x1][y1] != -9999) drop=(float)(dem[i][j]-dem[x1][y1])+0.001f;
                        localKineticEnergy[i][j]=meanAnnualStreamflow[i][j]*drop;
                        cumulativeKineticEnergy[i][j]=localKineticEnergy[i][j];
                 }
            }
         
            runNetworkAccumulations(cumulativeKineticEnergy,dirs);
                    
            java.io.FileOutputStream        outputDir;
            java.io.DataOutputStream        newfile;
            java.io.BufferedOutputStream    bufferout;

            outputDir = new FileOutputStream(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/potentialEnergy/ExpenditureToGainsRatio.vhc"));
            bufferout=new BufferedOutputStream(outputDir);
            newfile=new DataOutputStream(bufferout);

            for (int i=0;i<cumulativeKineticEnergy.length;i++) for (int j=0;j<cumulativeKineticEnergy[0].length;j++) {
                if(cumulativeKineticEnergy[i][j] > 0 && cumulativePotentialEnergy[i][j] > 0){
                    //System.out.println("K="+cumulativeKineticEnergy[i][j]+" P="+cumulativePotentialEnergy[i][j]+" K/P="+cumulativeKineticEnergy[i][j]/cumulativePotentialEnergy[i][j]);
                    newfile.writeFloat(cumulativeKineticEnergy[i][j]/cumulativePotentialEnergy[i][j]);
                    //newfile.writeFloat(localKineticEnergy[i][j]);
                }else{
                    newfile.writeFloat(-9999);
                 }
            }
            newfile.close();
            bufferout.close();
            outputDir.close();
            
            String                          retorno="\n";
            
            metaInfo[0] = "This is an estimate of energy expenditures to potential energy gains";
        
            String minlat = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(dem_minlat).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LATITUDE);
            metaInfo[1] = minlat;

            String minlon = hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Float(dem_minlon).floatValue(), hydroScalingAPI.tools.DegreesToDMS.LONGITUDE);
            metaInfo[2] = minlon;

            String cellsize = ""+dem_res;
            metaInfo[3] = cellsize;
            metaInfo[4] = cellsize;
            metaInfo[5] = ""+dem_num_cols;
            metaInfo[6] = ""+dem_num_rows;
            metaInfo[7] = "float";
            metaInfo[8] = "-9999";
            metaInfo[9] = "fix";
            metaInfo[10] = "N/A";
            metaInfo[11] = "Generated by Ricardo's Calculation";
        
            outputDir = new FileOutputStream(new java.io.File("/CuencasDataBases/Continental_US_database/Rasters/Hydrology/potentialEnergy/ExpenditureToGainsRatio.metaVHC"));
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
     * Calculates the upstream area for each pixel in the DEM
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public void runNetworkAccumulations(float[][] runoff, byte[][] dirs){

        int[][] red=new int[dirs.length][dirs[0].length];
        
        for (int i=1; i < dirs.length-2 ; i++){
            for (int j=1 ; j < dirs[0].length-2; j++){
                if (dirs[i][j]>0){
                    int llegan=0;
                    for (int k=0; k <= 8; k++){
                        if (dirs[i+(k/3)-1][j+(k%3)-1]==9-k)
                            llegan++;
                    }
                    red[i][j] = llegan;
                }
                else{
                    red[i][j] = -3;
                }
            }
        }
        int contn;
        DO:
            do{
                contn= 0;
                for (int i=1; i < dirs.length-2 ; i++){
                    for (int j=1 ; j < dirs[0].length-2; j++){
                        if (red[i][j]==0){
                            contn++;
                            red[i][j]--;
                            int x1=i-1+(dirs[i][j]-1)/3 ; int y1=j-1+(dirs[i][j]-1)%3;
                            try{
                                if (dirs[x1][y1]>0){
                                    red[x1][y1]--;
                                    runoff[x1][y1] += runoff[i][j];
                                }
                            }catch(ArrayIndexOutOfBoundsException e){System.err.println("error "+i+" "+j+" "+dirs[i][j]+" "+x1+" "+y1);}
                        }
                    }
                }
                
                System.out.println(contn);
                
                
            }while(contn > 0 );
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        new CalculateHydroKinetic();
    }
}

