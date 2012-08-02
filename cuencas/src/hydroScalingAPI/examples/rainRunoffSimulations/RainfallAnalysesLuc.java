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


/*
 * simulationsRep3.java
 * This funcion was modified by luciana to include a different methods to estimate runoff
 * and superficial velocity in the hillslope
 * 02/12/2009 - it uses the SCSManager, that estimates Curve Number for each hillslope
 * The runoff production method includes
 * Hilltype
        = 0 ; runoff = precipitation (wiht or without delay)
        = 1 ; SCS Method - explicitly acount for soil moiusture condition
        = 2 ; Mishra - Singh Method - modified SCS method that implicity accounts for for soil moiusture condition
 * Created on April, 2009
 */

package hydroScalingAPI.examples.rainRunoffSimulations;

import visad.*;
import java.io.*;
import java.util.*;
import java.util.TimeZone;
/**
 *
 * @author Luciana Cunha
 */
public class RainfallAnalysesLuc extends java.lang.Object implements Runnable{

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;

    int x;
    int y;
    int[][] magnitudes;
    java.io.File stormFile;
    java.io.File outputDirectory;
    java.util.Hashtable routingParams;
    String Filename;

    /** Creates new simulationsRep3 */


    //public RainfallAnalysesLuc(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile,java.io.File outputDirectory,String Filename,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
    //    this(x,y,direcc,magnitudes,md,stormFile,outputDirectory,Filename,routingParams);
    //}

    public RainfallAnalysesLuc(int xx, int yy,byte[][] direcc, int[][] magnitudesOR,
        hydroScalingAPI.io.MetaRaster mdOR,
        java.io.File stormFileOR,
        java.io.File outputDirectoryOR,
        String filenameOR,
        java.util.Hashtable rP)
        throws java.io.IOException, VisADException{

        matDir=direcc;
        metaDatos=mdOR;
        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        stormFile=stormFileOR;
        outputDirectory=outputDirectoryOR;
        routingParams=rP;
        Filename=filenameOR;
   }



    public void executeSimulation() throws java.io.IOException, VisADException{

        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        /////////////SET LAND USE INFORMATION AND GENERATE COLOR CODED WIDTH FUNCTION////////////////////////////
        
        java.text.DecimalFormat fourPlaces=new java.text.DecimalFormat("0.0000");
        
         
        //////////////////////////////////////
        System.out.println("Start to open files");
        java.io.File theFile;
        java.io.File theFile_arunoff;
        java.io.File theFile_prec;
        
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"LinksInfo"+".csv");

        System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        int nLi=linksStructure.connectionsArray.length;
        double[] Area_length=new double[linksStructure.contactsArray.length];
        System.out.println("Open the file");
        newfile.write("1 ");
        double RC = -9.9;
        double max_rel=0;

        
 /*       newfile.write("link slope upsArea Length Area TotLEngth VolRes PorcGreen vr dist time landuse CN2 IA2 S2 ");
        for (int i=0;i<nLi;i++){
            //if(thisNetworkGeom.linkOrder(i) > 1){
                newfile.write(i+" ");
                newfile.write(thisNetworkGeom.Slope(i)+" ");
                newfile.write(thisNetworkGeom.upStreamArea(i)+" ");
                newfile.write(thisNetworkGeom.Length(i)+" ");
                newfile.write(thisHillsInfo.Area(i)+" ");
                newfile.write(thisNetworkGeom.upStreamTotalLength(i)+" ");               
                double slope=Math.max(thisNetworkGeom.Slope(i),0.005);  
                newfile.write("\n");
                Area_length[i]=thisHillsInfo.Area(i)*1000000/thisNetworkGeom.Length(i);
                max_rel=Math.max(max_rel,Area_length[i]);
        }*/

        //_stop calculate maximum recession time
        System.out.println("Termina escritura de Links info");

        newfile.close();
        bufferout.close();
 //////////////////////////////////////////

       hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
       System.out.println(stormFile);
       storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);

       if (!storm.isCompleted()) return;

       thisHillsInfo.setStormManager(storm);
      System.out.println("finish reading storm");
       // WRITE PRECIPITATION FILE
       java.io.File theFiler;
       java.io.File theFilea;
       theFiler=new java.io.File(outputDirectory.getAbsolutePath()+"/Statistics_it"+Filename+"Hillprec"+".csv");
       theFilea=new java.io.File(outputDirectory.getAbsolutePath()+"/Acum"+Filename+"Hillprec"+".csv");

       java.io.FileOutputStream salidar = new java.io.FileOutputStream(theFiler);
       java.io.BufferedOutputStream bufferoutr = new java.io.BufferedOutputStream(salidar);
       java.io.OutputStreamWriter newfiler = new java.io.OutputStreamWriter(bufferoutr);
       java.io.FileOutputStream salidaa = new java.io.FileOutputStream(theFilea);
       java.io.BufferedOutputStream bufferouta = new java.io.BufferedOutputStream(salidaa);
        java.io.OutputStreamWriter newfilea = new java.io.OutputStreamWriter(bufferouta);

       int numPeriods = 1;

  
     System.out.println("Start statistics in space - number of links=" + linksStructure.connectionsArray.length + "numperiod = "+numPeriods);
     numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());
     System.out.println("Start statistics in space - number of links=" + linksStructure.connectionsArray.length + "numperiod = "+numPeriods);
     float[][] dto=linksStructure.getDistancesToOutlet();
     for (int i=0;i<linksStructure.connectionsArray.length;i++){
                
                newfilea.write(i+",");
                newfilea.write(fourPlaces.format(thisNetworkGeom.upStreamArea(i))+",");
                newfilea.write(fourPlaces.format(thisNetworkGeom.upStreamTotalLength(i))+",");
                newfilea.write(dto[1][i]+",");
                double max=0;
                double min=1000000;
                double meanh=0;
                double stdev=0;
                for (int k=0;k<numPeriods;k++) {
                    double currTime=storm.stormInitialTimeInMinutes()+(k-1)*storm.stormRecordResolutionInMinutes();
                    if(thisHillsInfo.precipitation(i,currTime)>max) max=thisHillsInfo.precipitation(i,currTime);
                    if(thisHillsInfo.precipitation(i,currTime)<min) min=thisHillsInfo.precipitation(i,currTime);
                    meanh=meanh+thisHillsInfo.precipitation(i,currTime);
                }
                meanh=meanh/(numPeriods-1);
                double currTime=storm.stormInitialTimeInMinutes()+(numPeriods-1)*storm.stormRecordResolutionInMinutes();
                newfilea.write(fourPlaces.format(thisHillsInfo.precipitationacum(i,currTime))+",");
                newfilea.write(fourPlaces.format(max)+",");
                newfilea.write(fourPlaces.format(min)+",");
                newfilea.write(fourPlaces.format(meanh)+"\n");
       }
       newfilea.close();
       bufferouta.close();

       System.out.println("Start statistics in time - number of links=" + linksStructure.completeStreamLinksArray.length);
      // CALCULATE STATISTIC BY HILLSLOPE
      double[] coverage=new double[numPeriods];
      double[] mean=new double[numPeriods];
      double[] stddev=new double[numPeriods];
      double[] CV=new double[numPeriods];
      double[] CMP=new double[numPeriods];
      double[] CMB=new double[numPeriods];




       for (int k=0;k<numPeriods;k++) {
          coverage[k]=0;
          mean[k]=0;
          stddev[k]=0;
          CV[k]=0;
          CMP[k]=0;
          CMB[k]=0;
           double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
          java.util.Calendar thisDate1=java.util.Calendar.getInstance();
           java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
                thisDate1.setTimeZone(tz);
          thisDate1.setTimeInMillis((long)(currTime*60.*1000.0));
         
         for (int i=0;i<linksStructure.connectionsArray.length;i++){
             if(thisHillsInfo.precipitation(i,currTime)>0.001) coverage[k]=coverage[k]+1;
             mean[k]=mean[k]+thisHillsInfo.precipitation(i,currTime);
             CMP[k]=CMP[k]+(double)dto[1][i]*thisHillsInfo.precipitation(i,currTime);
             CMB[k]=CMB[k]+(double)dto[1][i];
             // how to calculate average area precipitation
             }
         coverage[k]=coverage[k]/linksStructure.connectionsArray.length;
         mean[k]=mean[k]/linksStructure.connectionsArray.length;
         CMP[k]=CMP[k]/CMB[k];
         for (int i=0;i<linksStructure.connectionsArray.length;i++)
             {stddev[k]=Math.pow((thisHillsInfo.precipitation(i,currTime)-mean[k]),2);}
             stddev[k]=stddev[k]/linksStructure.connectionsArray.length;
             if(mean[k]>0) CV[k]=stddev[k]/mean[k];
       }
         
       for (int k=0;k<numPeriods;k++) {
          double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
          java.util.Calendar thisDate1=java.util.Calendar.getInstance();
           java.util.TimeZone tz = java.util.TimeZone.getTimeZone("UTC");
           thisDate1.setTimeZone(tz);
          thisDate1.setTimeInMillis((long)(currTime*60.*1000.0));
          newfiler.write(currTime+",");
          newfiler.write(coverage[k]+","+mean[k]+","+stddev[k]+","+CV[k]+","+CMP[k]);
          newfiler.write("\n");
       }
       System.out.println("Finish hillslope statistics=" );
      // I wont right the series of rainfall / hillslope but the statistics
      /*for (int k=0;k<numPeriods;k++) {
          double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();
          java.util.Calendar thisDate1=java.util.Calendar.getInstance();
          thisDate1.setTimeInMillis((long)(currTime*60.*1000.0));

       for (int i=0;i<linksStructure.connectionsArray.length;i++){
                if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= 1)
                newfiler.write(fourPlaces.format((thisHillsInfo.precipitation(i,currTime)))+",");
                // how to calculate average area precipitation
          }
         newfiler.write("\n");
       }*/

        newfiler.close();
        bufferoutr.close();
      System.out.println("Termina escritura de precipitation hillslope - start DEM");
        theFilea=new java.io.File(outputDirectory.getAbsolutePath()+"/Acum"+Filename+"Pixel"+".asc");
        salidaa = new java.io.FileOutputStream(theFilea);
        bufferouta = new java.io.BufferedOutputStream(salidaa);
        newfilea = new java.io.OutputStreamWriter(bufferouta);
        //ASC FILE INFO

        newfilea.write("ncols "+storm.getncol()+"\n");
        newfilea.write("nrows "+storm.getnrow()+"\n");
        newfilea.write("xllcorner "+storm.getxllcorner()+"\n");// Iowa river
        newfilea.write("yllcorner "+storm.getyllcorner()+"\n");//Iowa river
        newfilea.write("cellsize "+storm.getcellsize()+"\n");
        newfilea.write("NODATA_value  "+"-9.0"+"\n");
        for (int il=0;il<storm.getnrow();il++)
          {for (int ic=0;ic<storm.getncol();ic++)
           {newfilea.write(storm.getTotalPixelBasedPrec(il,ic)+" ");
           }newfilea.write("\n");
        }

        newfilea.close();
        bufferouta.close();
       /*
                Escribo en un theFile lo siguiente:
                        Numero de links
                        Numero de links Completos
                        lista de Links Completos
                        Area aguas arriba de los Links Completos
                        Orden de los Links Completos
                        maximos de la WF para los links completos
                        Longitud simulacion
                        Resulatdos

             */
        
        String demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        String routingString="";


        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN.wfs.csv");
        System.out.println("Writing Width Functions - "+theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        System.out.println("Start time - space maps");
        double[][] wfs=linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray,0);

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+","+linksStructure.completeStreamLinksArray[i]+",");
                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+",");
                newfile.write("\n");
            }
        }

        newfile.close();
        System.out.println("Start time - space maps");
        int ID=linksStructure.getOutletID();
        System.out.println("linksStructure.getOutletID() "+ID);
        int new_link=-9;
        int ntime=(int)Math.ceil((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/(24*60));
        System.out.println("ntime "+ntime);
        double[][] matrix_prec=new double[linksStructure.completeStreamLinksArray.length][ntime];
        double[] lenght=new double[linksStructure.completeStreamLinksArray.length];
        double[] ttt=new double[ntime];
        int t=0;
        int n_links=0;
        for (int it=0;it<ntime;it++){ttt[it]=storm.stormInitialTimeInMinutes()+it*(24*60);}
        while(linksStructure.connectionsArray[ID].length!=0)
        {
            System.out.println("ID="+ID+"  linksStructure.connectionsArray[ID].length "+linksStructure.connectionsArray[ID].length);
            lenght[n_links]=thisNetworkGeom.upStreamTotalLength(ID);
            for (int it=0;it<ntime;it++)
            {double time=storm.stormInitialTimeInMinutes()+it*(24*60);
             double prec1=thisHillsInfo.precipitation(ID,(time+18*60));
             double prec2=thisHillsInfo.precipitation(ID,(time+12*60));
             double prec3=thisHillsInfo.precipitation(ID,(time+6*60));
             double prec4=thisHillsInfo.precipitation(ID,(time+0*60));
             double prec=(prec1+prec2+prec3+prec4)/4;
             matrix_prec[it][n_links]=prec;
            }
            n_links=n_links+1;
            // find out the next link
            double leng=0;
            for (int j=0;j<linksStructure.connectionsArray[ID].length;j++){
                if(thisNetworkGeom.upStreamTotalLength(linksStructure.connectionsArray[ID][j])>leng)
                {leng=thisNetworkGeom.upStreamTotalLength(linksStructure.connectionsArray[ID][j]);
                 new_link=linksStructure.connectionsArray[ID][j];
                }
                }
             ID=new_link;
             System.out.println("ID="+ID+"  linksStructure.connectionsArray[ID].length "+linksStructure.connectionsArray[ID].length);
        }

        theFilea=new java.io.File(outputDirectory.getAbsolutePath()+"/Matrix_time_lenght"+Filename+".csv");
        salidaa = new java.io.FileOutputStream(theFilea);
        bufferouta = new java.io.BufferedOutputStream(salidaa);
        newfilea = new java.io.OutputStreamWriter(bufferouta);
        //ASC FILE INFO
        newfilea.write("1 ");
        System.out.println("Start - n_links" + n_links + "ntime = "+ntime);
        for (int j=(n_links-1);j==0;j--){newfilea.write(lenght[j]+" ");}

        newfilea.write("\n");
        for (int it=(ntime-1);it==0;it--)
        {newfilea.write(it+" ");
         System.out.println("time = " + it);
            for (int j=(n_links-1);j==0;j--){
            newfilea.write(matrix_prec[it][j]+" ");
            }
         newfilea.write("\n");
        }
        
 
        System.out.println("Termina simulacion RKF");
        
        
    }

    public void run(){

        try{
            executeSimulation();
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
        } catch (VisADException v){
            System.out.print(v);
        }
    }


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try{

            
            subMainManning(args);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }

        System.exit(0);

    }

public static void subMainManning(String args[]) throws java.io.IOException, VisADException {

        ///// DEM DATA /////

        String pathinput = "/scratch/CuencasDataBases/Iowa_Rivers_DB//Rasters/Topography/3_arcSec/";
        java.io.File theFile=new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".dir"));
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        // main basin
        int x= 2256;
        int y= 876;


        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        
        String LandCoverName = "error";
        String Dir="error";
        String SoilName="error";
        ////RUNS PARAMETERS //////////////
        int[] space = {15};
        //int[] space = {1};
        int[] time = {180};
        //int[] time = {180};
        int new_t_res=0;
        int NDecRes=0;
        
        float missing = 0.0f;
        
        for (int is : space)
        {
          NDecRes=is;
          for (int it : time)
          {
              new_t_res=it;
 /*****DEFINE THE FOLDER WITH NEXRAD DATA AND OUTPUT FOLDER*******/           
              //String stormstring ="E:/CUENCAS/CedarRapids/Rainfall/resolution_study/Results_new/test/1_180/"+"H00070802_R1504_G_.metaVHC";
              //String stormstring ="E:/CUENCAS/CedarRapids/Rainfall/resolution_study/Results_new/WithGeomBias/"+NDecRes+"/"+new_t_res+"min/"+"/Time/Bin/"+"H00070802_R1504_G_.metaVHC";

              String stormstring ="/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010RadarLowRes/Iowa_StageV_2008/test2008/"+"NEXRAD_BC.metaVHC";
              java.io.File stormFile;
              stormFile=new java.io.File(stormstring);
//
//              LandCoverName="lcOri.metaVHC";
//              SoilName="soilorifinal.metaVHC";
//              Dir="E:/CUENCAS/Iowa_river/Rasters/Land_surface_Data/LandCover_original/";
//              new java.io.File(Dir+"/test/").mkdirs();
              String OutputDir=Dir+"/test/";
//              String LandUse = Dir+LandCoverName;
//              String Soil = Dir+SoilName;
                           
              OutputDir="/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/storms/observed_events/1996_2010RadarLowRes/Iowa_StageV_2008/";
              
              String OutputFile=NDecRes+"_"+new_t_res+"min_";
              String path = OutputDir;
              //String rain = RaininputDir+"/bin/"+precname;
                 
              new RainfallAnalysesLuc(x,y,matDirs,magnitudes,metaModif,stormFile,new java.io.File(path),OutputFile,routingParams).executeSimulation();
              path = OutputDir+"/logfile.txt";

           }

       }
             
}


}