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
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import visad.*;
import java.io.*;
import java.util.*;
/**
 *
 * @author Luciana Cunha
 */
public class Inundation_map extends java.lang.Object implements Runnable{
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    private byte[][] matHor;
    int x;
    int y;
    int[][] magnitudes;
    java.io.File inputFile;
    java.io.File LandUseFile;
    java.io.File SoilFile;

    double doo,L1,L2;
    double[] AveAlt;  // [link]//
    double[] maxAlt;  // [link]//
    double[] minAlt;  // [link]//
    double[] WaterAlt;
    int TR;
    int LC;
    String outputDirectory;
    double[] Qpbankfull;
    double[] Dpbankfull;
    double[] Qp;
    double[] Dp;
    int ord;
    int[][] matrizPintada;
    private hydroScalingAPI.io.MetaRaster metaDemData;
    /** Creates new simulationsRep3 */
    String Basin;

   public Inundation_map(int ordOR, String Bas,int Tr, int Year,int xx, int yy, byte[][] direcc, int[][] magnitudesOR,
        hydroScalingAPI.io.MetaRaster md,byte[][] HorLin
        ,java.io.File inputFileOR,String outputDirectoryOR,
        java.io.File LandUseFileOR,java.io.File SoilFileOR,
        double dooOR,double L1OR,double L2OR)
        throws java.io.IOException, VisADException{
        ord=ordOR;
        matDir=direcc;
        matHor=HorLin;
        metaDatos=md;
        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        inputFile=inputFileOR;
        SoilFile=SoilFileOR;
        LandUseFile=LandUseFileOR;
        outputDirectory=outputDirectoryOR;
        doo=dooOR;
        L1=L1OR;
        L2=L2OR;
        TR=Tr;
        LC=Year;
        Basin=Bas;
   }

    public void executeSimulation() throws java.io.IOException, VisADException{
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        /////////////SET LAND USE INFORMATION AND GENERATE COLOR CODED WIDTH FUNCTION////////////////////////////

       // System.out.println("Loading lAND USE ...");
       //hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager LandUse;
        //LandUse=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager(LandUseFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        //thisHillsInfo.setLandUseManager(LandUse);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
        java.io.File DEMFile=metaDatos.getLocationMeta();
        String Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        String SoilHydData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
        SCSObj=new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile,LandUseFile,SoilFile,new java.io.File(SoilHydData),new java.io.File(Soil150SWAData),myCuenca,linksStructure,metaDatos,matDir,magnitudes,0);

        thisHillsInfo.setSCSManager(SCSObj);
        java.text.DecimalFormat fourPlaces=new java.text.DecimalFormat("0.0000");

        double [][] dataSnapShotDem;
        java.io.File directorio=DEMFile.getParentFile();
        String baseNameDEM=directorio+"\\"+(DEMFile.getName().substring(0,DEMFile.getName().lastIndexOf(".")))+".dem";

        metaDemData=new hydroScalingAPI.io.MetaRaster(DEMFile);
        metaDemData.setLocationBinaryFile(new java.io.File((baseNameDEM)));
        dataSnapShotDem=new hydroScalingAPI.io.DataRaster(metaDemData).getDouble();

        hydroScalingAPI.util.statistics.Stats rainStats=new hydroScalingAPI.util.statistics.Stats(dataSnapShotDem,new Double(metaDemData.getMissing()).doubleValue());
        System.out.println("    --> DEM Stats:  Max = "+rainStats.maxValue+" Min = "+rainStats.minValue+" Mean = "+rainStats.meanValue);
        //////////////////////////////////////
        java.io.File theFile;
        theFile=new java.io.File(outputDirectory+"/"+Basin+"_"+ord+"_"+TR+"_"+LC+"Link_info"+".csv");
        System.out.println(theFile);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
        int nLi=linksStructure.connectionsArray.length;
        double[] Area_length=new double[linksStructure.contactsArray.length];
        System.out.println("Open the file");

        double RC = -9.9;
        double max_rel=0;
        Qpbankfull= new double[linksStructure.connectionsArray.length];
        Dpbankfull= new double[linksStructure.connectionsArray.length];
        Qp= new double[linksStructure.connectionsArray.length];
        Dp= new double[linksStructure.connectionsArray.length];
        // READING THE FILE WITH PEAK DISCHARGE (M3/S)
        Qpbankfull=ReadFilesDisc(new java.io.File("D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2hVFinal/TR_2/10/C0.75/SCScharlotte_764_168-GK0.5-HillType_4.0Rain0.0mm0.0min.csv"));
        Qp=ReadFilesDisc(inputFile);
        // CALCULATE PEAK DEPTH OF WATER BASED ON THE CURVE Dw=do x A ^ L1 x Qp ^ L2
        // the curve was derived using USGS measurements
        System.out.println("CALCULATIN DEPTH ...");
        for (int i=0;i<nLi;i++){
            Dp[i]=doo*Math.pow((double)thisNetworkGeom.upStreamArea(i),L1)*Math.pow(Qp[i],L2);
            Dpbankfull[i]=doo*Math.pow((double)thisNetworkGeom.upStreamArea(i),L1)*Math.pow(Qpbankfull[i],L2);
        }
       //////////////////////////////////////////////////////
       // GENERATE MATRIZ PINTADA
        int[][] matDirBox=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        int[][] matHorLin=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        int[][] matPinLin=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        double[][] matDEM=new double[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        double[][] matOrder=new double[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        int[][] matWat=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        double[][] DepthW=new double[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        double[][] WD=new double[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        double[][] Porc_damaged=new double[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        double[][] struc_value=new double[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        double[][] damage=new double[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];

        // READING MATRIX WITH DIRECTION
        for (int i=1;i<matDirBox.length-1;i++) for (int j=0;j<matDirBox[0].length-1;j++){
            matDirBox[i][j]=(int) matDir[i+myCuenca.getMinY()-1][j+myCuenca.getMinX()-1];
        }
        // READING MATRIX WITH RIVER POSITION
        for (int i=1;i<matDirBox.length-1;i++) for (int j=0;j<matDirBox[0].length-1;j++){
            matHorLin[i][j]=(int) matHor[i+myCuenca.getMinY()-1][j+myCuenca.getMinX()-1];
        }

            /****** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA COBERTURA DEL MAPA SOBRE LA CUENCA)*****************/
            double minLonBasin= metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0;
            double minLatBasin = metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0;
            double maxLonBasin = metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0;
            double maxLatBasin = metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0;
            double res=metaDatos.getResLat();
            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;
            matrizPintada=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
            for (int i=0;i<linksStructure.contactsArray.length;i++){
                if (linksStructure.magnitudeArray[i] < linksStructure.basinMagnitude){
                //IF link's magnitude < Shreve's magnitude associated to the river network
                    xOulet=linksStructure.contactsArray[i]%metaDatos.getNumCols();
                    yOulet=linksStructure.contactsArray[i]/metaDatos.getNumCols();
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=i+1;
                    }
                } else {
                    xOulet=myCuenca.getXYBasin()[0][0];
                    yOulet=myCuenca.getXYBasin()[1][0];
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=i+1;
                    }
                }
            }
        ////////
        System.out.println("FINISH MATRIX PINTADA ...");
        System.out.println("matHorLin.length =" +matHorLin.length + "matHorLin[0].length" +matHorLin[0].length);
        System.out.println("matrizPintada.length =" +matrizPintada.length + "matrizPintada[0].length" +matrizPintada[0].length);
       double[] evalSpotDem;
       double demMinLon=metaDatos.getMinLon();
       double demMinLat=metaDatos.getMinLat();
       double demResLon=metaDatos.getResLon();
       double demResLat=metaDatos.getResLat();
       int MatXDem,MatYDem;
       double PixelSize = 6378.0*demResLat*Math.PI/(3600.0*180.0)*1000;
       int basinMinX=myCuenca.getMinX();
       int basinMinY=myCuenca.getMinY();
       int basinMaxX=myCuenca.getMaxX();
       int basinMaxY=myCuenca.getMaxY();
       // CREATE A MATRIX WITH RIVER POSITION DEFINED BY THE LINK NUMBER - AND 0 NON RIVER SPOTS
          for (int j=0;j<matHorLin.length;j++) for (int k=0;k<matHorLin[0].length;k++){
              matPinLin[j][k]=-9;
              if (matrizPintada[j][k]>0 && matHorLin[j][k] > 0){ // the river!!!
                 matPinLin[j][k]=matrizPintada[j][k];
              }
            }
      // GENERATE DEM VERSION OF MATRIZ PINTADA
       for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
            evalSpotDem=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
            demMinLat+(basinMinY+j-1)*demResLat/3600.0};
            MatXDem=(int) Math.floor((evalSpotDem[0]-demMinLon)/demResLon*3600.0);
            MatYDem=(int) Math.floor((evalSpotDem[1]-demMinLat)/demResLat*3600.0);
            double PixelH=dataSnapShotDem[MatYDem][MatXDem];
            matDEM[j][k]=PixelH;
            if (matrizPintada[j][k]-1>=0) matOrder[j][k]=thisNetworkGeom.linkOrder(matrizPintada[j][k]-1);
            else matOrder[j][k]=-1;
        }
         java.io.File OutputFileDEM=new java.io.File(outputDirectory+"/"+"DEM"+Basin+".asc");
         newfileASC(OutputFileDEM,myCuenca, matDEM,metaDatos);
   // CREATE ASC FILE WITH THE INFORMATION GENERATE UNTIL NOW
            java.io.File OutputFileM=new java.io.File(outputDirectory+"/matriz_pin"+"DEM"+Basin+".asc");
            newfileASC(OutputFileM,myCuenca,matrizPintada,metaDatos);
            java.io.File OutputFileL=new java.io.File(outputDirectory+"/River_"+Basin+".asc");
            newfileASC(OutputFileL,myCuenca,matHorLin,metaDatos);
            java.io.File OutputFileLM=new java.io.File(outputDirectory+"/River_pintada"+Basin+".asc");
            newfileASC(OutputFileLM,myCuenca, matPinLin,metaDatos);
            java.io.File OutputFileDir=new java.io.File(outputDirectory+"/Dir"+Basin+".asc");
            newfileASC(OutputFileDir,myCuenca, matDirBox,metaDatos);
            java.io.File OutputFileMatO=new java.io.File(outputDirectory+"/MatOrder"+Basin+".asc");
            newfileASC(OutputFileMatO,myCuenca, matOrder,metaDatos);

      // CALCULATE AVERAGE, MINIMUM AND MAXIMUM RIVER ALTITUDE
       maxAlt= new double[linksStructure.contactsArray.length];
       AveAlt= new double[linksStructure.contactsArray.length];
       minAlt= new double[linksStructure.contactsArray.length];
       WaterAlt=new double[linksStructure.contactsArray.length];


       int[] currentHillNumPixels=new int[linksStructure.contactsArray.length];
       for (int j=0;j<linksStructure.contactsArray.length;j++)
         {currentHillNumPixels[j]=0;
         maxAlt[j]=0;
         minAlt[j]=1000000;
         AveAlt[j]=0;
         WaterAlt[j]=0.0;}
       for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
       matWat[j][k]=-9;
       DepthW[j][k]=-9.0;
       WD[j][k]=-9.0;
       Porc_damaged[j][k]=-9.0;
       damage[j][k]=-9.0;
       struc_value[j][k]=-9.0;
       
       if (matPinLin[j][k] > 0){
          AveAlt[matrizPintada[j][k]-1]=AveAlt[matrizPintada[j][k]-1]+ matDEM[j][k];
          if(matDEM[j][k]>maxAlt[matrizPintada[j][k]-1]) maxAlt[matrizPintada[j][k]-1]= matDEM[j][k];
          if( matDEM[j][k]<minAlt[matrizPintada[j][k]-1] &&  matDEM[j][k]>0) minAlt[matrizPintada[j][k]-1]= matDEM[j][k];
          currentHillNumPixels[matrizPintada[j][k]-1]++;
       }
    }
    // CALCULATE AVERAGE WATER DEPTH
     for (int j=0;j<linksStructure.contactsArray.length;j++)
        {AveAlt[j]=AveAlt[j]/currentHillNumPixels[j];
        if(thisNetworkGeom.linkOrder(j)>=5) WaterAlt[j]=((AveAlt[j]+3*maxAlt[j])/4)+Dp[j]-Dpbankfull[j];
        if(thisNetworkGeom.linkOrder(j)==3 || thisNetworkGeom.linkOrder(j)==4) WaterAlt[j]=((minAlt[j]+2*AveAlt[j])/3)+Dp[j]-Dpbankfull[j];
        if(thisNetworkGeom.linkOrder(j)<3) WaterAlt[j]=((2*minAlt[j]+2*AveAlt[j])/4)+Dp[j]-Dpbankfull[j];
        
        //WaterAlt[j]=((2*AveAlt[j]+maxAlt[j])/3)+Dp[j]-Dpbankfull[j];
        if(WaterAlt[j]<=0) System.out.println("WaterAlt[j]<0"+WaterAlt[j]+"order="+thisNetworkGeom.linkOrder(j)+"AveAlt[j]"+AveAlt[j]+" minAlt[j]=" + minAlt[j] + " Dp[j]="+Dp[j] + " Qp[j]="+Qp[j] + "Area =" + (double)thisNetworkGeom.upStreamArea(j));
     }
   java.io.File StrucFile=new java.io.File("D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2hVFinal/inundation_map/buildvalue.asc");
   System.out.println(" StrucFile ="+StrucFile);
   struc_value=ReadFilesStruc_value(StrucFile);
   OutputFileDir=new java.io.File(outputDirectory+"/"+"Build_value_CUENCAS"+".asc");
            newfileASC(OutputFileDir,myCuenca,struc_value,metaDatos);

   // CALCULATE INUNDATION MAP AND WATER DEPTH BY PIXEL

   double damageTotal=0;
   for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
         DepthW[j][k]=0;
        //if (matrizPintada[j][k] > 0 && thisNetworkGeom.linkOrder(matrizPintada[j][k]-1) >= ord)
         if (matrizPintada[j][k] > 0)
            {double PixelH=matDEM[j][k];
             WD[j][k]=WaterAlt[matrizPintada[j][k]-1];
             DepthW[j][k]=Math.floor(WaterAlt[matrizPintada[j][k]-1]- matDEM[j][k]);
             //if(DepthW[j][k]<-10 || DepthW[j][k]>10) System.out.println("WD[j][k]"+WD[j][k]+" PixelH=" + PixelH + " DepthW[j][k]="+DepthW[j][k]);
             if(DepthW[j][k]>0)  {matWat[j][k]=1;}
             else{matWat[j][k]=0;}
             
        }
   }


          /*Flow of the water from one pixel to the next pixel if:
               - Pixel belongs to a higher order basin
               - DEM Central Pixel> DEM close pixel
               - Alt Wat Central Pixel > Alt water close pixel
               In this case:
               - change the Alt Wat close pixel = Alt Wat Central Pixel
               - Change Depth wat close pixel = New alt water - DEM close pixel
                       */
   int flag=1;
   while (flag==1)
   {
       int n=0;
       for (int j=2;j<(matrizPintada.length-2);j++) for (int k=2;k<(matrizPintada[0].length-2);k++){
          //check surrounding if the pixel is inundated!
          //if (matrizPintada[j][k] > 0 && thisNetworkGeom.linkOrder(matrizPintada[j][k]-1) >= ord && matWat[j][k]==1){
          if (matrizPintada[j][k] > 0 && matWat[j][k]==1){
            if (WD[j][k]>WD[j-1][k] && matDEM[j][k]>=matDEM[j-1][k]&& (matrizPintada[j-1][k]-1)>0)
                    if (thisNetworkGeom.linkOrder(matrizPintada[j][k]-1)>=thisNetworkGeom.linkOrder(matrizPintada[j-1][k]-1))
                    {WD[j-1][k]=WD[j][k];n=n+1;DepthW[j-1][k]=WD[j-1][k]-matDEM[j-1][k];matWat[j-1][k]=1;}
            if (WD[j][k]>WD[j+1][k] && matDEM[j][k]>=matDEM[j+1][k]&& (matrizPintada[j+1][k]-1)>0)
                    if(thisNetworkGeom.linkOrder(matrizPintada[j][k]-1)>=thisNetworkGeom.linkOrder(matrizPintada[j+1][k]-1))
                    {WD[j+1][k]=WD[j][k];n=n+1;DepthW[j+1][k]=WD[j+1][k]-matDEM[j+1][k];matWat[j+1][k]=1;}
            if (WD[j][k]>WD[j+1][k+1] && matDEM[j][k]>=matDEM[j+1][k+1]&& (matrizPintada[j+1][k+1]-1)>0)
                    if(thisNetworkGeom.linkOrder(matrizPintada[j][k]-1)>=thisNetworkGeom.linkOrder(matrizPintada[j+1][k+1]-1))
                    {WD[j+1][k+1]=WD[j][k];n=n+1;DepthW[j+1][k+1]=WD[j+1][k+1]-matDEM[j+1][k+1];matWat[j+1][k+1]=1;}
            if (WD[j][k]>WD[j-1][k-1] && matDEM[j][k]>=matDEM[j-1][k-1]&& (matrizPintada[j-1][k-1]-1)>0)
                    if(thisNetworkGeom.linkOrder(matrizPintada[j][k]-1) >= thisNetworkGeom.linkOrder(matrizPintada[j-1][k-1]-1))
                    {WD[j-1][k-1]=WD[j][k];n=n+1;DepthW[j-1][k-1]=WD[j-1][k-1]-matDEM[j-1][k-1];matWat[j-1][k-1]=1;}
            if (WD[j][k]>WD[j-1][k+1] && matDEM[j][k]>=matDEM[j-1][k+1]&& (matrizPintada[j-1][k+1]-1)>0)
                    if(thisNetworkGeom.linkOrder(matrizPintada[j][k]-1) >= thisNetworkGeom.linkOrder(matrizPintada[j-1][k+1]-1))
                    {WD[j-1][k+1]=WD[j][k];n=n+1;DepthW[j-1][k+1]=WD[j-1][k+1]-matDEM[j-1][k+1];matWat[j-1][k+1]=1;}
            if (WD[j][k]>WD[j+1][k-1] && matDEM[j][k]>=matDEM[j+1][k-1]&& (matrizPintada[j+1][k-1]-1)>0)
                    if(thisNetworkGeom.linkOrder(matrizPintada[j][k]-1) >= thisNetworkGeom.linkOrder(matrizPintada[j+1][k-1]-1))
                    {WD[j+1][k-1]=WD[j][k];n=n+1;DepthW[j+1][k-1]=WD[j+1][k-1]-matDEM[j+1][k-1];matWat[j+1][k-1]=1;}
            if (WD[j][k]>WD[j][k-1] && matDEM[j][k]>=matDEM[j][k-1]&& (matrizPintada[j][k-1]-1)>0)
                    if(thisNetworkGeom.linkOrder(matrizPintada[j][k]-1) >= thisNetworkGeom.linkOrder(matrizPintada[j][k-1]-1))
                    {WD[j][k-1]=WD[j][k];n=n+1;DepthW[j][k-1]=WD[j][k-1]-matDEM[j][k-1];matWat[j][k-1]=1;}
            if (WD[j][k]>WD[j][k+1] && matDEM[j][k]>=matDEM[j][k+1]&& (matrizPintada[j][k+1]-1)>0)
                    if(thisNetworkGeom.linkOrder(matrizPintada[j][k]-1)>=thisNetworkGeom.linkOrder(matrizPintada[j][k+1]-1))
                    {WD[j][k+1]=WD[j][k];n=n+1;DepthW[j][k+1]=WD[j][k+1]-matDEM[j][k+1];matWat[j][k+1]=1;}
             }
          if (n==0) flag=0;
          }
   }

   for (int j=1;j<(matrizPintada.length-1);j++) for (int k=1;k<(matrizPintada[0].length-1);k++){
       if (matrizPintada[j][k] > 0)
            {if(DepthW[j][k]>0)  {matWat[j][k]=1;}
             else{matWat[j][k]=0;}
             // If order >3 always colored the river too
             if (matHorLin[j][k]>=4) matWat[j][k]=1;
        }
   }
   /*for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
       evalSpotDem=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
       demMinLat+(basinMinY+j-1)*demResLat/3600.0};
       MatXDem=(int) Math.floor((evalSpotDem[0]-demMinLon)/demResLon*3600.0);
       MatYDem=(int) Math.floor((evalSpotDem[1]-demMinLat)/demResLat*3600.0);

       if (matrizPintada[j][k] > 0 && thisNetworkGeom.linkOrder(matrizPintada[j][k]-1) >= ord ){
       //System.out.println(" basinMinX "+basinMinX+" basinMaxX "+basinMaxX+" basinMinY "+basinMinY+" basinMaxX "+basinMaxX);
       //System.out.println(" matrizPintada lin "+matrizPintada.length+" matrizPintada col "+matrizPintada[0].length);
           double PixelH=dataSnapShotDem[MatYDem][MatXDem]; // Altitude pixel
           double AltWat=WaterAlt[matrizPintada[j][k]-1];

           System.out.println(" j "+j+" k "+k +"matrizPintada[j][k]"+(matrizPintada[j][k]) + " AltWater " +AltWat+ "PixelH  " + PixelH);
           //// check to the right
           // to the left
           double AltNeig=0.0;
           int colDEM=MatXDem;
           int colMat=k;
          // System.out.println("AltWater"+AltWater);
           while (AltWat>AltNeig && colDEM>basinMinX && colMat>1 && matPinLin[j][colMat] <1)
           {
               matWat[j][colMat]=1;
               colDEM--;
               colMat--;
               //System.out.println("SACO 1 colDEM"+colDEM+"colMat"+colMat);
               AltNeig=dataSnapShotDem[MatYDem][colDEM];
           }
           // to the left
           AltNeig=0.0;
           colDEM=MatXDem;
           colMat=k;
           while (AltWater>AltNeig && colDEM<basinMaxX && colMat<matrizPintada.length && matPinLin[j][colMat] <1)
           {
               matWat[j][colMat]=1;
               colDEM++;
               colMat++;
               //System.out.println("SACO 2 colDEM"+colDEM+"colMat"+colMat);
               AltNeig=dataSnapShotDem[MatYDem][colDEM];
           }
           AltNeig=0.0;
           int linDEM=MatXDem;
           int linMat=k;
           while (AltWater>AltNeig && linDEM>basinMinY && linMat>0 && matPinLin[linMat][k] <1)
           {
               matWat[linMat][k]=1;
               linDEM--;
               linMat--;
               //System.out.println("SACO 3 linDEM"+linDEM+"linMat"+linMat);
               AltNeig=dataSnapShotDem[linDEM][MatXDem];
           }
           // to the left
           AltNeig=0.0;
           linDEM=MatXDem;
           linMat=k;
           while (AltWater>AltNeig && linDEM<basinMaxY && linMat<matrizPintada[0].length && matPinLin[linMat][k] <1)
           {
               matWat[linMat][k]=1;
               linDEM++;
               linMat++;
               //System.out.println("SACO 4 linDEM  "+linDEM+"linMat  "+linMat+ "AltWater"+AltWater+"AltNeig"+AltNeig);
               AltNeig=dataSnapShotDem[linDEM][MatXDem];
           }
        }
      }*/

   /*for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
         evalSpotDem=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
         demMinLat+(basinMinY+j-1)*demResLat/3600.0};
         MatXDem=(int) Math.floor((evalSpotDem[0]-demMinLon)/demResLon*3600.0);
         MatYDem=(int) Math.floor((evalSpotDem[1]-demMinLat)/demResLat*3600.0);
         Porc_damaged[j][k]=0.0;
        if (matWat[j][k]==0 && matWat[j-1][k]==1 && matWat[j+1][k] ==1) matWat[j][k]=1;
        if (matWat[j][k]==0 && matWat[j][k-1]==1 && matWat[j][k+1] ==1) matWat[j][k]=1;
      }*/
   // CALCULATE PORCENTAGE OF DAMAGE BASED ON WATER DEPTH
   for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
         evalSpotDem=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
         demMinLat+(basinMinY+j-1)*demResLat/3600.0};
         MatXDem=(int) Math.floor((evalSpotDem[0]-demMinLon)/demResLon*3600.0);
         MatYDem=(int) Math.floor((evalSpotDem[1]-demMinLat)/demResLat*3600.0);
         Porc_damaged[j][k]=0.0;
         damage[j][k]=0;
        if (matrizPintada[j][k] > 0 && matWat[j][k]==1)
           {if(DepthW[j][k]>=-2.43 && DepthW[j][k]<=3.35) Porc_damaged[j][k]=(-0.9619*Math.pow(DepthW[j][k],3)+1.9942*Math.pow(DepthW[j][k],2)+20.717*Math.pow(DepthW[j][k],1)+25.679);
            if(DepthW[j][k]>3.35) Porc_damaged[j][k]=81.0;
            if(Porc_damaged[j][k]>0 && struc_value[j][k]>0) damage[j][k]=Porc_damaged[j][k]*struc_value[j][k]/100;
            else damage[j][k]=0;
           }
        else {
             Porc_damaged[j][k]=0.0;
             damage[j][k]=0.0;}
         if(damage[j][k]<0) System.out.println("damage[j][k]"+damage[j][k]+" = Porc_damaged[j][k]=" + Porc_damaged[j][k] + " = struc_value[j][k]="+struc_value[j][k]+" damageTotal="+damageTotal);
         damageTotal=damageTotal+damage[j][k];
       }

    System.out.println("Order=" + ord + " TR="+TR+" LC="+ LC + "damageTotal ="+damageTotal);
   String appfile=outputDirectory+"/TotalDamage.txt";
   FileWriter fstream = new FileWriter(appfile,true);
   BufferedWriter out = new BufferedWriter(fstream);
   out.write("Basin" + Basin + "Order= " + ord + " TR= "+TR+" LC= "+ LC + " " +damageTotal +"\n");
   out.close();

    java.io.File OutputFileW=new java.io.File(outputDirectory+"/"+Basin+ord+"_"+TR+"_"+LC+"_Inund_map"+".asc");
    newfileASC(OutputFileW,myCuenca,matWat,metaDatos);
   java.io.File OutputFileAW=new java.io.File(outputDirectory+"/"+Basin+ord+"_"+TR+"_"+LC+"_Alt_water"+".asc");
   newfileASC(OutputFileAW,myCuenca,DepthW,metaDatos);
   java.io.File OutputFilePD=new java.io.File(outputDirectory+"/"+Basin+ord+"_"+TR+"_"+LC+"_Por_damage"+".asc");
   newfileASC(OutputFilePD,myCuenca,Porc_damaged,metaDatos);
   java.io.File OutputFileWD=new java.io.File(outputDirectory+"/"+Basin+ord+"_"+TR+"_"+LC+"_AltitueWater"+".asc");
   newfileASC(OutputFileWD,myCuenca,WD,metaDatos);
   java.io.File OutputFileDam=new java.io.File(outputDirectory+"/"+Basin+ord+"_"+TR+"_"+LC+"_Damage"+".asc");
   newfileASC(OutputFileDam,myCuenca,damage,metaDatos);

    /*int flag=1;
    while (flag==1)
       {int flagint=0;
       for (int j=1;j<(matrizPintada.length-1);j++) for (int k=1;k<(matrizPintada[0].length-1);k++){
         evalSpotDem=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
         demMinLat+(basinMinY+j-1)*demResLat/3600.0};
         MatXDem=(int) Math.floor((evalSpotDem[0]-demMinLon)/demResLon*3600.0);
         MatYDem=(int) Math.floor((evalSpotDem[1]-demMinLat)/demResLat*3600.0);
        if (matrizPintada[j][k] > 0)
            {if(matWat[j][k]==1)
                {double PixelH=dataSnapShotDem[MatYDem][MatXDem];
                if(dataSnapShotDem[MatYDem-1][MatXDem]<=PixelH) {matWat[j-1][k]=1;
                flagint=1;}
                if(dataSnapShotDem[MatYDem-1][MatXDem+1]<=PixelH) {matWat[j-1][k+1]=1;
                flagint=1;}
                if(dataSnapShotDem[MatYDem-1][MatXDem-1]<=PixelH) {matWat[j-1][k-1]=1;
                flagint=1;}
                if(dataSnapShotDem[MatYDem+1][MatXDem]<=PixelH) {matWat[j+1][k]=1;
                flagint=1;}
                if(dataSnapShotDem[MatYDem+1][MatXDem+1]<=PixelH) {matWat[j+1][k+1]=1;
                flagint=1;}
                if(dataSnapShotDem[MatYDem+1][MatXDem-1]<=PixelH) {matWat[j+1][k-1]=1;
                flagint=1;}
                if(dataSnapShotDem[MatYDem][MatXDem+1]<=PixelH) {matWat[j][k+1]=1;
                flagint=1;}
                if(dataSnapShotDem[MatYDem][MatXDem-1]<=PixelH) {matWat[j][k-1]=1;
                flagint=1;}
             }
         }
       }
     if(flagint==1) flag=0;
     }*/
       ///////////////////////////////

        for (int i=0;i<nLi;i++){
                newfile.write(i+",");
                newfile.write(thisNetworkGeom.Slope(i)+",");
                newfile.write(thisNetworkGeom.upStreamArea(i)+",");
                newfile.write(thisNetworkGeom.Length(i)+",");
                newfile.write(thisHillsInfo.Area(i)+",");
                newfile.write(thisHillsInfo.SCS_CN2(i)+",");
                newfile.write(Qp[i]+",");
                newfile.write(Dp[i]+",");
                newfile.write(minAlt[i]+",");
                newfile.write(AveAlt[i]+",");
                newfile.write(maxAlt[i]+",");
                newfile.write(WaterAlt[i]+",");
                newfile.write(thisNetworkGeom.upStreamTotalLength(i)+" ");
                newfile.write("\n");
        }
        newfile.close();
        bufferout.close();

    }
    public static double[] ReadFilesDisc(java.io.File Disc) throws IOException{
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        ruta = new FileReader(Disc);
        buffer=new BufferedReader(ruta);
        String data = buffer.readLine();
        double[][] matrix = new double[500][20000];
        double[] Qmax = new double[20000];
        double[] timemax = new double[20000];
        double maxq = 0;
        int il=0;
        int icc=0;
        int ic=0;
        while (data != null)
        {
          tokens = new StringTokenizer(data,",");
          while (tokens.hasMoreTokens() && ic<=15142) {
              String str=tokens.nextToken();
              if (str.equals("0")) str="0.000";
 //System.out.println("ic - "+ic+"  il - "+il + "str" + str);
              matrix[il][ic]=new Double(str);

              ic=ic+1;
             }
            icc=ic;
            ic=0;
            il=il+1;
        data = buffer.readLine();
        }
        ic=icc;
        il=il-1; // just because I have the error
        System.out.println("ic - "+ic+"  il - "+il);

        int count=0;
        int maxorder=0;
        int indexmax=0;
        for (int c=0;c<(ic-1);c++){
                Qmax[c]=0;
                timemax[c]=0;
            }
        for (int i=3;i<(il);i++){
            for (int c=0;c<(ic-1);c++){
                int cm=c+1;
                if (maxorder<matrix[1][cm]) {maxorder=(int)matrix[1][cm];}
                if (maxq<matrix[i][cm])
                {maxq=matrix[i][cm];
                 indexmax=c;
                }
                if (Qmax[c]<matrix[i][cm])
                {Qmax[c]=matrix[i][cm];
                 timemax[c]=(matrix[i][0]-matrix[4][0])/(24*60);
                }
            }
         }
        return Qmax;
    }
       public static double[][] ReadFilesStruc_value(java.io.File Disc) throws IOException{
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;
        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;
        ruta = new FileReader(Disc);
        buffer=new BufferedReader(ruta);
        String data = buffer.readLine();
        data = buffer.readLine();
        data = buffer.readLine();
        data = buffer.readLine();
        data = buffer.readLine();
        data = buffer.readLine();
        data = buffer.readLine();
        double[][] matrix = new double[5000][5000];
        double maxq = 0;
        int il=1339;
        int icc=0;
        int ic=0;
        while (data != null)
        {
        //System.out.println("data - "+data);
          tokens = new StringTokenizer(data," ");
          while (tokens.hasMoreTokens()) {
            matrix[il][ic]=new Double(tokens.nextToken());
        //System.out.println("ic - "+ic+"  il - "+il + "matrix[il][ic]" + matrix[il][ic]);
            ic=ic+1;

             }
            icc=ic;
            ic=0;
            il=il-1;
        data = buffer.readLine();
        }
        //System.out.println("ic - "+ic+"  il - "+il);
        return matrix;
    }
     private void newfileASC(java.io.File AscFile, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca,int[][] Finalmatrix,hydroScalingAPI.io.MetaRaster metaDatos) throws java.io.IOException{

        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";
        outputDir = new java.io.FileOutputStream(AscFile);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);
        int nlin= (myCuenca.getMaxY()-myCuenca.getMinY()+3);
        int ncol= (myCuenca.getMaxX()-myCuenca.getMinX()+3);
        double minLonBasin= metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0;
        double minLatBasin = metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0;
        double demResLat=metaDatos.getResLat()/3600;
        newfile.write("ncols "+ncol+retorno);
        newfile.write("nrows "+nlin+retorno);
        newfile.write("xllcorner "+(minLonBasin-demResLat)+retorno);// Iowa river
        newfile.write("yllcorner "+(minLatBasin-demResLat)+retorno);//Iowa river
        newfile.write("cellsize "+demResLat+retorno);
        newfile.write("NODATA_value  "+"-9"+retorno);
     //   System.out.println("OUTPUT PROGRAM - Fcolumns = " +Finalcolumns + "Frows = "+Finalrows);
       for (int i=(nlin-1);i>=0;i--) {
        for (int j=0;j<ncol;j++) {
                    newfile.write(Finalmatrix[i][j]+" ");
                }
            newfile.write(retorno);
           }
        newfile.close();
        bufferout.close();
        outputDir.close();
        }

     private void newfileASC(java.io.File AscFile, hydroScalingAPI.util.geomorphology.objects.Basin myCuenca,double[][] Finalmatrix,hydroScalingAPI.io.MetaRaster metaDatos) throws java.io.IOException{

        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          retorno="\n";
        java.text.DecimalFormat fourPlaces=new java.text.DecimalFormat("0.00000");
        java.text.DecimalFormat twoPlaces=new java.text.DecimalFormat("0.00");
        outputDir = new java.io.FileOutputStream(AscFile);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);
        int nlin= (myCuenca.getMaxY()-myCuenca.getMinY()+3);
        int ncol= (myCuenca.getMaxX()-myCuenca.getMinX()+3);
        double minLonBasin= metaDatos.getMinLon()+myCuenca.getMinX()*metaDatos.getResLon()/3600.0;
        double minLatBasin = metaDatos.getMinLat()+myCuenca.getMinY()*metaDatos.getResLat()/3600.0;
        double demResLat=metaDatos.getResLat()/3600;
        //It seems that the data is shift one pixel to the right and down
        // I did minLonBasin - 1pixel size to fix it - but it is not the appropriate way
        newfile.write("ncols "+ncol+retorno);
        newfile.write("nrows "+nlin+retorno);
        newfile.write("xllcorner "+(minLonBasin-demResLat)+retorno);// Iowa river
        newfile.write("yllcorner "+(minLatBasin-demResLat)+retorno);//Iowa river
        newfile.write("cellsize "+demResLat+retorno);
        newfile.write("NODATA_value  "+"-9"+retorno);
     //   System.out.println("OUTPUT PROGRAM - Fcolumns = " +Finalcolumns + "Frows = "+Finalrows);
       for (int i=(nlin-1);i>=0;i--) {
        for (int j=0;j<ncol;j++) {
                    //newfile.write(fourPlaces.format(Finalmatrix[i][j]+" "));
                    newfile.write(twoPlaces.format(Finalmatrix[i][j])+" ");
                }
            newfile.write(retorno);
           }
        newfile.close();
        bufferout.close();
        outputDir.close();
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
            //subMain1(args);   //11140102 - Blue River
          // subMain2(args);   //11110103 - Illinois, Arkansas
            //subMain3(args);   //11070208 - Elk River Near Tiff
            //subMain4(args);   //Clear Creek
            //subMain5(args);   //Whitewater radar
            //subMain6(args);   //Whitewate sat
            subMainManning(args);   //Charlotte radar
           //Reservoir(args);
           // GreenRoof(args);
            //genfiles(args);
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
        String pathinput = "D:/CUENCAS/Charlote/Rasters/Topography/Charlotte_1AS/";
        java.io.File theFile=new java.io.File(pathinput + "charlotte" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte" + ".dir"));
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte" + ".horton"));
        formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] HortonLinks=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        // main basin

              ////RUNS PARAMETERS //////////////
       //int[] year_LC= {10,1992,2001,2100};
       // 48.0f,66.04f,
       int[] year_LC= {2001};
       int [] TR={100};
       //int [] TR={2,5,10,25,50,100,500};
       //int[] sub_area= {1,2,3,4,5,6,7};
      int[] sub_area= {1};
       //TR={25};
       double d=0.49;
       double L1=-0.07;
       double L2=0.38;
       //int [] TR={2};


       String LandCoverName = "error";
       String Dir="error";
       String SoilName="error";


           for (int iy:year_LC)
           {int year=iy;
            for (int tr :TR)
           {int trr=tr;
            String stormstring ="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/ras_2h/TR_"+trr+"/prec.metaVHC";
              for (int sa :sub_area){
                  int or=0;
                  int suba=sa;
                  java.io.File stormFile;
                  stormFile=new java.io.File(stormstring);
                  int x= -9;
                  int y= -9;
                  String Basin="null";
                  if(suba==1){x= 764; y= 168; Basin="Total";}
                  if(suba==2){x= 638; y= 604; Basin="Sugar";}
                  if(suba==3){x= 640; y= 604; Basin="Little_sugar";}
                  if(suba==4){x= 734; y= 727; Basin="Little_sugar_critical_area";}
                  if(suba==5){x= 828; y= 940; Basin="Little_sugar_order5_1";}
                  if(suba==6){x= 832; y= 940; Basin="Little_sugar_order5_2";}
                  if(suba==7){x= 664; y= 494; Basin="McAlpine";}
                  if(suba==8){x= 619; y= 435; Basin="West_basin";}
          ///// LANDCOVERDATA /////
                       if (year==10){
                               LandCoverName="lcOri.metaVHC";
                               SoilName="soilorifinal.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_original/";
                               }
                           if (year==2001){
                               LandCoverName="lc2001.metaVHC";
                               SoilName="soil2001.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover2001/";}
                           if (year==1992){
                               LandCoverName="lc1992.metaVHC";
                               SoilName="soil1992.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover1992/";}
                           if (year==2009){
                               LandCoverName="lc_greenway.metaVHC";
                               SoilName="soil_data.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_parks/";}
                           if (year==2100){
                               LandCoverName="lcimp.metaVHC";
                               SoilName="soil2001.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_imp/";}
                           if (year==20092){
                               LandCoverName="lc_greenway2.metaVHC";
                               SoilName="soil_data2.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_parks/";}
             new java.io.File(Dir+"/test/").mkdirs();
          String OutputDir=Dir+"/test/";
          String LandUse = Dir+LandCoverName;
                           ///// SOILDATA /////
         String Soil = Dir+SoilName;
                             //routingParams.put("SoilMoisture",vm);
                           //OutputDir="D:/CUENCAS/Charlote/results/2001/test/Mishra/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";

         String InputFolder="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2hVFinal/TR_"+trr+"/"+year+"/C0.75/";
         OutputDir="D:/CUENCAS/Charlote/Sites/Precipitation/SintPrec/results_2hVFinal/inundation_map/"+Basin+"/";

         new File(OutputDir).mkdirs();
                           //OutputDir="D:/CUENCAS/Charlote/results/1992/Param_vel_Delay0/v=1.0"+"/SM"+vm+"/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";
         new File(InputFolder).mkdirs();
         String path = OutputDir;
         String input = InputFolder+"SCScharlotte_764_168-GK0.5-HillType_4.0Rain0.0mm0.0min.csv";
         System.out.println("InputFolder="+input);
         new Inundation_map(or,Basin,trr,year,x,y,matDirs,magnitudes,metaModif,HortonLinks,new java.io.File(input),path, new java.io.File(LandUse),new java.io.File(Soil),d,L1,L2).executeSimulation();
           }
         //}
         }
       }

}
public static void GreenRoof(String args[]) throws java.io.IOException, VisADException {
        ///// DEM DATA /////
        String pathinput = "D:/CUENCAS/Charlote/Rasters/Topography/Charlotte_1AS/";
        java.io.File theFile=new java.io.File(pathinput + "charlotte" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte" + ".dir"));
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        // main basin
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte" + ".horton"));
        formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] HortonLinks=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        int x= 764;
        int y= 168;
       double d=0.49;
       double L1=-0.07;
       double L2=0.38;
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        routingParams.put("lambda1",0.33f);
        routingParams.put("lambda2",-0.17f);
        routingParams.put("vo",0.74f);
        routingParams.put("Routing Type",5.f); // check NetworkEquationsLuciana.java for definitions
        routingParams.put("Hill Type",4.f);  // check NetworkEquationsLuciana.java for definitions
        routingParams.put("Hill Velocity Type",3.f);  // check NetworkEquationsLuciana.java for definitions
        routingParams.put("vrunoff",250.f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("vssub",1.0f);
        routingParams.put("SoilMoisture",2.f);
        routingParams.put("lambdaSCSMethod",0.05f);
        routingParams.put("Vconst",0.5f); // CHANGE IN THE NETWORKEQUATION CLASS
        routingParams.put("InitialCondition",0.001f); // Porcentage of the soil filled up with water
        routingParams.put("P5Condition",-9.0f); // Porcentage of the soil filled up with water
              ////RUNS PARAMETERS //////////////
       int[] cen= {1,2,3,4};
       //int[] cen= {1};
       // 48.0f,66.04f,
       //int [] TR={100};
       int [] TR={100,2,25,500};
       //int [] TR={2};
       float[] IC_array= {0.0f}; // ANTECEDENT SOIL MOISTURE CONDICTION
       float[] vr_array= {100000.0f}; // HILLSLOPE VELOCITY
       float[] vs_array= {0.1f}; // SUBSURFACE FLOW VELOCITY
       float infiltr=0.0f;
       String LandCoverName = "error";
       String Dir="error";
       String SoilName="error";
       float vrun,vsub,IC;

         for (int tr :TR)
           {int trr=tr;
           for (int iy:cen)
           {int C=iy;
            String stormstring ="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/ras_2h/TR_"+trr+"/prec.metaVHC";
            IC=0.0f;
            /*for (int C=0;C<=1;C++)
            {
            float RC=(float)C;
            if(trr==2) IC=RC*37.338f;
            if(trr==5) IC=RC*55.372f;
            if(trr==10) IC=RC*71.374f;
            if(trr==25) IC=RC*98.044f;
            if(trr==50) IC=RC*124.206f;
            if(trr==100) IC=RC*157.48f;
            if(trr==500) IC=RC*274.32f;*/
                         java.io.File stormFile;
                         stormFile=new java.io.File(stormstring);
                           vsub=0.1f;
           //              routingParams.put("vrunoff",vrun);
                           routingParams.put("vssub",vsub);
                           routingParams.put("P5Condition",IC);
                           ///// LANDCOVERDATA /////
                          Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LCGreenRoof/";
                           if (C==1)LandCoverName="landusec1.metaVHC";
                           if (C==2)LandCoverName="landusec2.metaVHC";
                           if (C==3)LandCoverName="landusec3.metaVHC";
                           if (C==4)LandCoverName="landusec4.metaVHC";
                           new java.io.File(Dir+"/test/").mkdirs();
                           String OutputDir=Dir+"/test/";
                           String LandUse = Dir+LandCoverName;
                           ///// SOILDATA /////
                           SoilName="soil2001.metaVHC";
                           String DirSoil="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover2001/";
                           String Soil = DirSoil+SoilName;
                             //routingParams.put("SoilMoisture",vm);
                           //OutputDir="D:/CUENCAS/Charlote/results/2001/test/Mishra/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";
                           OutputDir="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2h_IC0/GreenRoof/"+"TR_"+trr+"/C"+C+"/";
                           //OutputDir="D:/CUENCAS/Charlote/results/KNRCS/"+year+"/"+intensity+"mm/"+duration+"min"+"/P5="+IC+"mm/"+"vsub="+vsub;
                           System.out.println("OutputDir="+OutputDir);
                              new File(OutputDir).mkdirs();
         String InputFolder="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2h_IC0/GreenRoof/"+"TR_"+trr+"/C"+C+"/";
         OutputDir="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2h_IC0/inundation_map/";
         new File(OutputDir).mkdirs();
                           //OutputDir="D:/CUENCAS/Charlote/results/1992/Param_vel_Delay0/v=1.0"+"/SM"+vm+"/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";
         new File(InputFolder).mkdirs();
         String path = OutputDir;
         String input = InputFolder+"SCScharlotte_764_168-GK0.5-HillType_4.0Rain0.0mm0.0min.csv";
         System.out.println("InputFolder="+input);
        String Basin="Total";
        int or=1;
        new Inundation_map(or,Basin,trr,C,x,y,matDirs,magnitudes,metaModif,HortonLinks,new java.io.File(input),path, new java.io.File(LandUse),new java.io.File(Soil),d,L1,L2).executeSimulation();
                            path = OutputDir+"/logfile.txt";
                            //Gen_format(path);
           //}
           }
       }
}
public static void Reservoir(String args[]) throws java.io.IOException, VisADException {
        ///// DEM DATA /////

        String pathinput = "D:/CUENCAS/Charlote/Rasters/Topography/Charlotte_1AS/";
        java.io.File theFile=new java.io.File(pathinput + "charlotte" + ".metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte" + ".dir"));
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte" + ".horton"));
        formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] HortonLinks=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        // main basin

              ////RUNS PARAMETERS //////////////
       //int[] year_LC= {10,1992,2001,20092,2100};
       // 48.0f,66.04f,
       int [] TR={100,2};
       float [] RV={1000.f,10000.f};
       float [] RVT={1.f};
       //int [] TR={2,5,10,25,50,100,500};
       int[] sub_area= {1,2,3,4,5,6,7,8};
       //int[] sub_area= {1};
       //TR={25};
       double d=0.49;
       double L1=-0.07;
       double L2=0.38;

        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        routingParams.put("lambda1",0.33f);
        routingParams.put("lambda2",-0.17f);
        routingParams.put("vo",0.74f);
        routingParams.put("Routing Type",5.f); // check NetworkEquationsLuciana.java for definitions
        routingParams.put("Hill Type",6.f);  // check NetworkEquationsLuciana.java for definitions
        routingParams.put("Hill Velocity Type",3.f);  // check NetworkEquationsLuciana.java for definitions
        routingParams.put("vrunoff",250.f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("vssub",1.0f);
        routingParams.put("SoilMoisture",2.f);
        routingParams.put("lambdaSCSMethod",0.05f);
        routingParams.put("Vconst",0.5f); // CHANGE IN THE NETWORKEQUATION CLASS
        routingParams.put("InitialCondition",0.001f); // Porcentage of the soil filled up with water
        routingParams.put("P5Condition",-9.0f); // Porcentage of the soil filled up with water
        routingParams.put("ReserVolume",1.0f); // m3/km2 of reservation
        routingParams.put("ReserVolumetType",1.0f); // reservoir position:
        // 1 - all the basin
        // 2 - just developed area
        ////RUNS PARAMETERS //////////////

       //int [] TR={2};
       float[] IC_array= {0.0f}; // ANTECEDENT SOIL MOISTURE CONDICTION
       float[] vr_array= {100000.0f}; // HILLSLOPE VELOCITY
       int[] year_LC= {2001};
       float[] vs_array= {0.1f}; // SUBSURFACE FLOW VELOCITY
       float infiltr=0.0f;
       String LandCoverName = "error";
       String Dir="error";
       String SoilName="error";
       float vrun,vsub,IC;

         for (int tr :TR)
           {int trr=tr;
           for (int iy:year_LC)
           {int year=iy;
            String stormstring ="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/ras_2h/TR_"+trr+"/prec.metaVHC";
            IC=0.0f;
            int RT=0;
            for (float rvt:RVT)
            {float rvolt=rvt;
            for (float rv:RV)
            {float rvol=rv;
                           for (int sa :sub_area){
                  int or=1;
                  int suba=sa;
                  java.io.File stormFile;
                  stormFile=new java.io.File(stormstring);
                  int x= -9;
                  int y= -9;
                  String Basin="null";
                  if(suba==1){x= 764; y= 168; Basin="Total";}
                  if(suba==2){x= 638; y= 604; Basin="Sugar";}
                  if(suba==3){x= 640; y= 604; Basin="Little_sugar";}
                  if(suba==4){x= 734; y= 727; Basin="Little_sugar_critical_area";}
                  if(suba==5){x= 828; y= 940; Basin="Little_sugar_order5_1";}
                  if(suba==6){x= 832; y= 940; Basin="Little_sugar_order5_2";}
                  if(suba==7){x= 664; y= 494; Basin="McAlpine";}
                  if(suba==8){x= 619; y= 435; Basin="West_basin";}
             System.out.println("Volume = " + rvol + "Reserv volume type" + rvt);
             routingParams.put("ReserVolume",rvol); // m3/km2 of reservation
              routingParams.put("ReserVolumetType",rvt); // reservoir position:
              vsub=0.1f;
           //              routingParams.put("vrunoff",vrun);
           float C=0.5f;
            /*for (int C=0;C<=1;C++)
            {*/
            float RC=(float)C;
            if(trr==2) IC=RC*37.338f;
            if(trr==5) IC=RC*55.372f;
            if(trr==10) IC=RC*71.374f;
            if(trr==25) IC=RC*98.044f;
            if(trr==50) IC=RC*124.206f;
            if(trr==100) IC=RC*157.48f;
            if(trr==500) IC=RC*274.32f;
                           routingParams.put("vssub",vsub);
                           routingParams.put("P5Condition",IC);
                           ///// LANDCOVERDATA /////
                          if (year==10){
                               LandCoverName="lcOri.metaVHC";
                               SoilName="soilorifinal.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_original/";
                               }
                           if (year==2001){
                               LandCoverName="lc2001.metaVHC";
                               SoilName="soil2001.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover2001/";}
                           if (year==1992){
                               LandCoverName="lc1992.metaVHC";
                               SoilName="soil1992.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover1992/";}
                           if (year==2009){
                               LandCoverName="lc_greenway.metaVHC";
                               SoilName="soil_data.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_parks/";}
                           if (year==20092){
                               LandCoverName="lc_greenway2.metaVHC";
                               SoilName="soil_data2.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_parks/";}
                           if (year==2100){
                               LandCoverName="lcimp.metaVHC";
                               SoilName="soil2001.metaVHC";
                               Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover_imp/";}
                           new java.io.File(Dir+"/test/").mkdirs();
                           String OutputDir=Dir+"/test/";
                           String LandUse = Dir+LandCoverName;
                           ///// SOILDATA /////
                           String Soil = Dir+SoilName;
                             //routingParams.put("SoilMoisture",vm);
                           //OutputDir="D:/CUENCAS/Charlote/results/2001/test/Mishra/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";
                           OutputDir="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2h_IC0_v2/Reserv/rtype"+rvolt+"/rvol"+rvol+"/TR_"+trr+"/"+year+"/C"+C+"/";
                           //OutputDir="D:/CUENCAS/Charlote/results/KNRCS/"+year+"/"+intensity+"mm/"+duration+"min"+"/P5="+IC+"mm/"+"vsub="+vsub;
                           System.out.println("OutputDir="+OutputDir);
         String InputFolder="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2h_IC0/TR_"+trr+"/"+year+"/";
         OutputDir="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2h_IC0/inundation_map/";
         new File(OutputDir).mkdirs();
                           //OutputDir="D:/CUENCAS/Charlote/results/1992/Param_vel_Delay0/v=1.0"+"/SM"+vm+"/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";
         new File(InputFolder).mkdirs();
         String path = OutputDir;
         String input = InputFolder+"SCScharlotte_764_168-GK0.5-HillType_4.0Rain0.0mm0.0min.csv";
         System.out.println("InputFolder="+input);
         new Inundation_map(or,Basin,trr,year,x,y,matDirs,magnitudes,metaModif,HortonLinks,new java.io.File(input),path, new java.io.File(LandUse),new java.io.File(Soil),d,L1,L2).executeSimulation();

                            //Gen_format(path);
                           }
                           }
            }
           }
       }
}
}