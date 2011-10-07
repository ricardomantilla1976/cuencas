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

/**
 *
 * @author Luciana Cunha
 */
public class SimulationToFileLuciana extends java.lang.Object implements Runnable{

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;

    int x;
    int y;
    int[][] magnitudes;
    float rainIntensity;
    float rainDuration;
    int greenroof;
    java.io.File stormFile;
    java.io.File GreenFile;

    java.io.File LandUseFile;
    java.io.File SoilFile;
    float LandUseFileFlag;
    float SoilFileFlag;

    hydroScalingAPI.io.MetaRaster infiltMetaRaster;
    float infiltRate;
    int routingType;
    int HillType;
    int HillVelType;
    float IniCondition;
    java.io.File outputDirectory;
    java.util.Hashtable routingParams;
    java.io.File SoilHydFile;
    java.io.File SoilSwa150File;
    /** Creates new simulationsRep3 */
    public SimulationToFileLuciana(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, java.io.File outputDirectory, float LandUseFileFlag, float SoilFileFlag,java.io.File SoilSwa150File, java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,0,null,outputDirectory,LandUseFileFlag,null,SoilFileFlag,SoilSwa150File,null,routingParams);
    }
    public SimulationToFileLuciana(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, java.io.File outputDirectory,float LandUseFileFlag,float SoilFileFlag,java.io.File SoilSwa150File,java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,infiltRate,0,null,outputDirectory,LandUseFileFlag,null,SoilFileFlag,SoilSwa150File,null,routingParams);
    }

    public SimulationToFileLuciana(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, java.io.File outputDirectory,java.io.File LandUseFile,java.io.File SoilFile,java.io.File SoilSwa150File, java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,infiltRate,0,null,outputDirectory,0.0f,LandUseFile,0.0f,SoilFile,SoilSwa150File,routingParams);
    }

    public SimulationToFileLuciana(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, float infiltRate, int greenroof, java.io.File GreenFile,java.io.File outputDirectory,java.io.File LandUseFile,java.io.File SoilFile,java.io.File SoilSwa150File, java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,null,infiltRate,greenroof,GreenFile,outputDirectory,0.0f,LandUseFile,0.0f,SoilFile,SoilSwa150File,routingParams);
    }

   public SimulationToFileLuciana(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, float rainIntensity, float rainDuration, float infiltRate, java.io.File outputDirectory,java.io.File LandUseFile,java.io.File SoilFile,java.io.File SoilSwa150File, java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,rainIntensity,rainDuration,null,null,infiltRate,0,null,outputDirectory,0.0f,LandUseFile,0.0f,SoilFile,SoilSwa150File,routingParams);
    }

    public SimulationToFileLuciana(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md, java.io.File stormFile, hydroScalingAPI.io.MetaRaster infiltMetaRaster, int greenroof, java.io.File GreenFile,java.io.File outputDirectory,float LandUseFileFlag,java.io.File LandUseFile,float SoilFileFlag,java.io.File SoilFile,java.io.File SoilSwa150File, java.util.Hashtable routingParams) throws java.io.IOException, VisADException{
        this(x,y,direcc,magnitudes,md,0.0f,0.0f,stormFile,infiltMetaRaster,0.0f,greenroof,GreenFile,outputDirectory,LandUseFileFlag,null,SoilFileFlag,SoilSwa150File,null,routingParams);
    }

    public SimulationToFileLuciana(int xx, int yy, byte[][] direcc, int[][] magnitudesOR,
        hydroScalingAPI.io.MetaRaster md, float rainIntensityOR, float rainDurationOR,
        java.io.File stormFileOR ,hydroScalingAPI.io.MetaRaster infiltMetaRasterOR,
        float infiltRateOR,int GreenRoofOptionOR,java.io.File GreenFileOR,
        java.io.File outputDirectoryOR,
        float LandUseFileFlagOR, java.io.File LandUseFileOR,
        float SoilFileFlagOR, java.io.File SoilFileOR,
        java.io.File SoilSwa150FileOR,
        java.util.Hashtable rP)
        throws java.io.IOException, VisADException{


        matDir=direcc;
        metaDatos=md;
        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        rainIntensity=rainIntensityOR;
        rainDuration=rainDurationOR;
        stormFile=stormFileOR;
        infiltMetaRaster=infiltMetaRasterOR;
        infiltRate=infiltRateOR;
        SoilFile=SoilFileOR;
        SoilFileFlag=SoilFileFlagOR;
        LandUseFile=LandUseFileOR;
        LandUseFileFlag=LandUseFileFlagOR;
        outputDirectory=outputDirectoryOR;
        routingParams=rP;
        greenroof= GreenRoofOptionOR;
        GreenFile=GreenFileOR;
        SoilSwa150File = SoilSwa150FileOR;
        
   }



    public void executeSimulation() throws java.io.IOException, VisADException{

        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);

        float widthCoeff=((Float)routingParams.get("widthCoeff")).floatValue();
        float widthExponent=((Float)routingParams.get("widthExponent")).floatValue();
        float widthStdDev=((Float)routingParams.get("widthStdDev")).floatValue();

        float chezyCoeff=((Float)routingParams.get("chezyCoeff")).floatValue();
        float chezyExponent=((Float)routingParams.get("chezyExponent")).floatValue();

        thisNetworkGeom.setWidthsHG(widthCoeff,widthExponent,widthStdDev);
        thisNetworkGeom.setCheziHG(chezyCoeff, chezyExponent);

        float lam1=((Float)routingParams.get("lambda1")).floatValue();
        float lam2=((Float)routingParams.get("lambda2")).floatValue();
        float v_o=((Float)routingParams.get("vo")).floatValue();
        float vconst=((Float)routingParams.get("Vconst")).floatValue();
        float vsub=((Float)routingParams.get("vssub")).floatValue();
        float vrun=((Float)routingParams.get("vrunoff")).floatValue();
        float SM=((Float)routingParams.get("SoilMoisture")).floatValue();
        float rt=((Float)routingParams.get("Routing Type")).floatValue();
        float ht=((Float)routingParams.get("Hill Type")).floatValue();
        float hvt=((Float)routingParams.get("Hill Velocity Type")).floatValue();
        float lambdaSCS=((Float)routingParams.get("lambdaSCSMethod")).floatValue();
        float P5=((Float)routingParams.get("P5Condition")).floatValue();
        IniCondition=((Float)routingParams.get("InitialCondition")).floatValue();
        float Vtype =((Float)routingParams.get("ReserVolumetType")).floatValue();
        float Voo =((Float)routingParams.get("ReserVolume")).floatValue();
        System.out.println("    Voo="+Voo);
        routingType= (int)rt;
        HillType= (int)ht;
        HillVelType= (int)hvt;
        System.out.println("Volume = " + Voo + "Reserv volume type" + Vtype);
        if(GreenFile != null) System.out.println("Green file name = " + GreenFile.getAbsolutePath());
        System.out.println("SET PARAMETERS - Hill Velocity Type"+hvt+"rt"+ "RoutingType="+rt+" Hill Type=" + ht +" vo=" + v_o +"lambda1=" +lam1 + " lambda2=" +lam2+ " lambda2=" +vconst);
        System.out.println("SET PARAMETERS vssub="+vsub+"  vrun=" + vrun +" SM=" + SM +"lambda1=" +lam1 + " lambda2=" +lam2);
        System.out.println("SET PARAMETERS Initial condition - IC="+IniCondition);
        thisNetworkGeom.setVqParams(v_o,0.0f,lam1,lam2);
        thisNetworkGeom.setCteVel(vconst);
        String SoilHydData = "Error";
        SoilHydFile= new java.io.File(SoilHydData);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        /////////////SET LAND USE INFORMATION AND GENERATE COLOR CODED WIDTH FUNCTION////////////////////////////
        System.out.println("Loading lAND USE ...");
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager LandUse;
        LandUse=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager(LandUseFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        thisHillsInfo.setLandUseManager(LandUse);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
        java.io.File DEMFile=metaDatos.getLocationMeta();
        
        SCSObj=new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile,LandUseFile,SoilFile, SoilHydFile,SoilSwa150File,myCuenca,linksStructure,metaDatos,matDir,magnitudes,0);
        thisHillsInfo.setSCSManager(SCSObj);
        java.text.DecimalFormat fourPlaces=new java.text.DecimalFormat("0.0000");


        //////////////////////////////////////
        System.out.println("Start to run Width function");
        java.io.File theFile;
        java.io.File theFile_arunoff;
        java.io.File theFile_prec;
        java.io.File theFile_asub;
        java.io.File theFile_Shill;
        java.io.File theFile_Ssoil;
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
        float[][] Vo=new float[1][linksStructure.contactsArray.length];
        float[][] Go=new float[1][linksStructure.contactsArray.length];
        // Atributing volume of reservation
        System.out.println("Volume");
        for (int i=0;i<nLi;i++){
           if(HillType==6){

               if(Vtype==1) Vo[0][i]=Voo/1000; //Voo [m3/km2] * [km2] *1000 = volume in mm
               if(Vtype==2 && SCSObj.getAverCN2(i)>=84) Vo[0][i]=Voo/1000;
               if(Vtype==2 && SCSObj.getAverCN2(i)<84) Vo[0][i]=0.0f;
               //System.out.println("Vo[i] = " + Vo + "thisHillsInfo.Area(i)" + thisHillsInfo.Area(i));
           }
           else
           {
              Vo[0][i]=0.0f;
           }
        }
        System.out.println("set Vo");
        thisHillsInfo.setVolRes(Vo);
        System.out.println("Area Green");
        // Atributes for green roof

        float[][] AreaGreen= new float[1][nLi];
        System.out.println("length[0]" + AreaGreen[0].length);
        System.out.println("length" + AreaGreen.length);
        if(HillType==7)
        {   System.out.println("greenroof" + greenroof);
            AreaGreen=ReadGreenRoofFile(GreenFile,nLi);
        }
        else {for (int i=0;i<nLi;i++)AreaGreen[0][i]=0.0f;}

        thisHillsInfo.setGreenRoof(AreaGreen);
        //System.out.println("length" + AreaGreen[0].length);
        //System.out.println("<nLi="+(nLi-5)+"    Voo="+AreaGreen[0][nLi-5]);
        newfile.write("link slope upsArea Length Area TotLEngth VolRes PorcGreen vr dist time landuse CN2 IA2 S2 ");
        for (int i=0;i<nLi;i++){
            //if(thisNetworkGeom.linkOrder(i) > 1){
                newfile.write(i+" ");
                newfile.write(thisNetworkGeom.Slope(i)+" ");
                newfile.write(thisNetworkGeom.upStreamArea(i)+" ");
                newfile.write(thisNetworkGeom.Length(i)+" ");
                newfile.write(thisHillsInfo.Area(i)+" ");
                newfile.write(thisNetworkGeom.upStreamTotalLength(i)+" ");
                newfile.write(thisHillsInfo.VolRes(i)+" ");
                float Porc=AreaGreen[0][i]/(((float)thisHillsInfo.Area(i)*1000000));
                newfile.write(Porc+" ");
                double slope=Math.max(thisNetworkGeom.Slope(i),0.005);
                double vr=(thisHillsInfo.Hill_K_NRCS(i))*Math.pow(slope,0.5)*100*0.3048;
                newfile.write(vr+" ");
                double dist=thisHillsInfo.Area(i)*1000000*0.5/(thisNetworkGeom.Length(i)); //(m)
                newfile.write(dist+" ");
                newfile.write(dist/vr+" ");
                newfile.write(thisHillsInfo.LandUse(i)+" ");
                newfile.write(thisHillsInfo.SCS_CN2(i)+" ");
                newfile.write(thisHillsInfo.SCS_IA2(i)+" ");
                newfile.write(thisHillsInfo.SCS_S2(i)+" ");

//               newfile.write(thisHillsInfo.SCS_S2(i)+" ");
//               newfile.write(thisHillsInfo.SCS_IA3(i)+" ");
//               newfile.write(thisHillsInfo.SCS_S3(i)+" ");
                newfile.write("\n");
                Area_length[i]=thisHillsInfo.Area(i)*1000000/thisNetworkGeom.Length(i);
                max_rel=Math.max(max_rel,Area_length[i]);

            //}
        }

        //_stop calculate maximum recession time

        double tim_run=max_rel/vrun; //hour
        double tim_run_sub=max_rel/(5*vsub); //at least 20% of sub flow contribute to the discharge
        System.out.println("Termina escritura de Links info");

        newfile.close();
        bufferout.close();
 //////////////////////////////////////////

       hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;

       if(stormFile == null)
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure,rainIntensity,rainDuration);
        else
            storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);

        if (!storm.isCompleted()) return;

        thisHillsInfo.setStormManager(storm);

        // WRITE PRECIPITATION FILE
        if(stormFile == null)
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"precipitation_zero"+".csv");
        else
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"precipitation_"+stormFile.getName()+".csv");

        String PrecipFile=theFile.getAbsolutePath();

        System.out.println(theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);

        newfile.write("1,");

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= 1)
                newfile.write(linksStructure.completeStreamLinksArray[i]+",");
        }

       newfile.write("\n");
        newfile.write("2,");

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= 1)
                newfile.write(fourPlaces.format(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i]))+",");
        }
       newfile.write("\n");

       int numPeriods = 1;

        if(stormFile == null)
            numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/rainDuration);
        else
            numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes());

        for (int k=0;k<numPeriods;k++) {

           double currTime=storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes();

           java.util.Calendar thisDate1=java.util.Calendar.getInstance();
           thisDate1.setTimeInMillis((long)(currTime*60.*1000.0));

            newfile.write(currTime+",");
            for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
              if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= 1)
                newfile.write(fourPlaces.format((thisHillsInfo.precipitation(i,currTime)))+",");

            }
                    newfile.write("\n");
        }

        System.out.println("Termina escritura de precipitation");

        newfile.close();
        bufferout.close();

        hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,0.0f);

        if(infiltMetaRaster == null)
            infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,infiltRate);
        else
            infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(myCuenca,linksStructure,infiltMetaRaster,matDir,magnitudes);

        thisHillsInfo.setInfManager(infilMan);

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
        switch (routingType) {
            case 0:     routingString="VC";
                        break;
            case 1:     routingString="CC";
                        break;
            case 2:     routingString="CV";
                        break;
            case 3:     routingString="CM";
                        break;
            case 4:     routingString="VM";
                        break;
            case 5:     routingString="GK";
                        break;
        }


        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN.wfs.csv");
        System.out.println("Writing Width Functions - "+theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);

        double[][] wfs=linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray,0);

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+","+linksStructure.completeStreamLinksArray[i]+",");
                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+",");
                newfile.write("\n");
            }
        }

        newfile.close();
  //      bufferout.close();

        if(rt == 2){
            theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+"CV"+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min.csv");//
            theFile_arunoff=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+"CV"+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_arunoff.csv");
            theFile_asub=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+"CV"+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_asub.csv");
            theFile_prec=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+"CV"+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_prec.csv");
            theFile_Shill=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+"CV"+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_Shill.csv");
            theFile_Ssoil=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+"CV"+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_Ssoil.csv");}
        else
            {theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+routingString+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min.csv");//
            theFile_arunoff=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+routingString+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_arunoff.csv");
            theFile_asub=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+routingString+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_asub.csv");
            theFile_prec=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+routingString+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_prec.csv");
            theFile_Shill=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+routingString+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_Shill.csv");
            theFile_Ssoil=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+routingString+vconst+"-HillType_"+ht+"Rain"+rainIntensity+"mm"+rainDuration+"min_Ssoil.csv");
        }


        //theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"SCS"+demName+"_"+x+"_"+y+"-"+storm.stormName()+"-IR_"+infiltMetaRaster.getLocationMeta().getName().substring(0,infiltMetaRaster.getLocationMeta().getName().lastIndexOf(".metaVHC"))+"-Routing_"+routingString+"_params_"+lam1+"_"+lam2+".csv");
        System.out.println(theFile);
        String DischargeFile=theFile.getAbsolutePath();
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        java.io.FileOutputStream salida2 = new java.io.FileOutputStream(theFile_arunoff);
        String RunoffFile=theFile_arunoff.getAbsolutePath();
        java.io.BufferedOutputStream bufferout2 = new java.io.BufferedOutputStream(salida2);
        java.io.OutputStreamWriter newfile2 = new java.io.OutputStreamWriter(bufferout2);

        java.io.FileOutputStream salida3 = new java.io.FileOutputStream(theFile_asub);
        String SubFile=theFile_asub.getAbsolutePath();
        java.io.BufferedOutputStream bufferout3 = new java.io.BufferedOutputStream(salida3);
        java.io.OutputStreamWriter newfile3 = new java.io.OutputStreamWriter(bufferout3);
        java.io.FileOutputStream salida4 = new java.io.FileOutputStream(theFile_prec);
        String PrecFile=theFile_prec.getAbsolutePath();
        java.io.BufferedOutputStream bufferout4 = new java.io.BufferedOutputStream(salida4);
        java.io.OutputStreamWriter newfile4 = new java.io.OutputStreamWriter(bufferout4);
        String S0File=theFile_Shill.getAbsolutePath();
        java.io.FileOutputStream salida5 = new java.io.FileOutputStream(theFile_Shill);
        java.io.BufferedOutputStream bufferout5 = new java.io.BufferedOutputStream(salida5);
        java.io.OutputStreamWriter newfile5 = new java.io.OutputStreamWriter(bufferout5);
        String S1File=theFile_Ssoil.getAbsolutePath();
        java.io.FileOutputStream salida6 = new java.io.FileOutputStream(theFile_Ssoil);
        java.io.BufferedOutputStream bufferout6 = new java.io.BufferedOutputStream(salida6);
        java.io.OutputStreamWriter newfile6 = new java.io.OutputStreamWriter(bufferout6);
        //newfile.write("Information on Complete order Streams\n");
        //newfile.write("Links at the bottom of complete streams are:\n");
        //newfile.write("Link #,");
        newfile.write("1,");
        newfile2.write("1,");
        newfile3.write("1,");
        newfile4.write("1,");
        newfile5.write("1,");
        newfile6.write("1,");
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(i+",");
                newfile2.write(i+",");
                newfile3.write(i+",");
                newfile4.write(i+",");
                newfile5.write(i+",");
                newfile6.write(i+",");
        }


        newfile.write("\n2,");
        newfile2.write("\n2,");
        newfile3.write("\n2,");
        newfile4.write("\n2,");
        newfile5.write("\n2,");
        newfile6.write("\n2,");
       // newfile.write("Horton Order,");

        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(thisNetworkGeom.linkOrder(i)+",");
                newfile2.write(thisNetworkGeom.linkOrder(i)+",");
                newfile3.write(thisNetworkGeom.linkOrder(i)+",");
                newfile4.write(thisNetworkGeom.linkOrder(i)+",");
                newfile5.write(thisNetworkGeom.linkOrder(i)+",");
                newfile6.write(thisNetworkGeom.linkOrder(i)+",");
        }

        newfile.write("\n3,");
        newfile2.write("\n3,");
        newfile3.write("\n3,");
        newfile4.write("\n3,");
        newfile5.write("\n3,");
        newfile6.write("\n3,");
       // newfile.write("Upstream Area [km^2],");
        for (int i=0;i<linksStructure.contactsArray.length;i++){
                newfile.write(fourPlaces.format(thisNetworkGeom.upStreamArea(i))+",");
                newfile2.write(fourPlaces.format(thisNetworkGeom.upStreamArea(i))+",");
                newfile3.write(fourPlaces.format(thisNetworkGeom.upStreamArea(i))+",");
                newfile4.write(fourPlaces.format(thisNetworkGeom.upStreamArea(i))+",");
                newfile5.write(fourPlaces.format(thisNetworkGeom.upStreamArea(i))+",");
                newfile6.write(fourPlaces.format(thisNetworkGeom.upStreamArea(i))+",");
        }
       // newfile.write("Slope,");
//       newfile.write("4 ");
//        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
//            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
//                newfile.write(thisNetworkGeom.Slope(linksStructure.completeStreamLinksArray[i])+" ");
//        }


  //      newfile.write("\n\n\n");
  //      newfile.write("Results of flow simulations in your basin");

        //newfile.write("\n");
       // newfile.write("Time,");

 //       for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
 //           if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
 //               newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
 //       }

//      hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquationsLuciana thisBasinEqSys=
//              new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquationsLuciana
//              (linksStructure,thisHillsInfo,thisNetworkGeom,routingType,HillType,HillVelType,vrun,vsub,1,SM,lambdaSCS);
//      hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquationsLuciana thisBasinEqSys=
//              new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquationsLuciana
//              (linksStructure,thisHillsInfo,thisNetworkGeom,routingType,HillType,vrun,vsub,SM,lambdaSCS);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquationsLuciana_mod thisBasinEqSys=
              new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquationsLuciana_mod
              (linksStructure,thisHillsInfo,thisNetworkGeom,routingType,HillType,HillVelType,vrun,vsub,SM,lambdaSCS);

        //      hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_HillDelay(linksStructure,thisHillsInfo,thisNetworkGeom,routingType);
      int nstates=linksStructure.contactsArray.length*2;

      if(HillType==1 || HillType==2 || HillType==3) nstates=linksStructure.contactsArray.length*3;
      if(HillType==4) nstates=linksStructure.contactsArray.length*6;
      if(HillType==6 || HillType==7) nstates=linksStructure.contactsArray.length*7;
      double[] initialCondition=new double[nstates];
      //double nummin=((((30*1000/1.0)/60)+(tim_run*60)+180))*1.0;
      double nummin=1440;


      if(HillType==0){for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.001;
            initialCondition[i+linksStructure.contactsArray.length]=0.001;
            }}
      if(HillType==1 || HillType==2 || HillType==3){for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.001;
            initialCondition[i+linksStructure.contactsArray.length]=0.001;
            initialCondition[i+2*linksStructure.contactsArray.length]=IniCondition*thisHillsInfo.SCS_S1(i);
            }}
      if(HillType==4){for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.001;
            initialCondition[i+linksStructure.contactsArray.length]=0.001;
            double S=0;
            initialCondition[i+3*linksStructure.contactsArray.length]=0.001;
            initialCondition[i+4*linksStructure.contactsArray.length]=0.001;
            initialCondition[i+5*linksStructure.contactsArray.length]=0.001;

            if(P5>0) {S=P5-Math.pow((P5-thisHillsInfo.SCS_IA1(i)),2)/(P5+thisHillsInfo.SCS_S1(i)-thisHillsInfo.SCS_IA1(i));}
            else {S=IniCondition;}
            if(S<thisHillsInfo.SCS_S1(i)) {initialCondition[i+2*linksStructure.contactsArray.length]=S;}
            else {initialCondition[i+2*linksStructure.contactsArray.length]=thisHillsInfo.SCS_S1(i);}
           }}
          if(HillType==6 || HillType==7){for (int i=0;i<linksStructure.contactsArray.length;i++){
            initialCondition[i]=0.001;
            initialCondition[i+linksStructure.contactsArray.length]=0.001;
            double S=0;
            initialCondition[i+3*linksStructure.contactsArray.length]=0.001;
            initialCondition[i+4*linksStructure.contactsArray.length]=0.001;
            initialCondition[i+5*linksStructure.contactsArray.length]=0.001;
            initialCondition[i+6*linksStructure.contactsArray.length]=0.0;
            if(P5>0) {S=P5-Math.pow((P5-thisHillsInfo.SCS_IA1(i)),2)/(P5+thisHillsInfo.SCS_S1(i)-thisHillsInfo.SCS_IA1(i));}
            else {S=IniCondition;}
            if(S<thisHillsInfo.SCS_S1(i)) {initialCondition[i+2*linksStructure.contactsArray.length]=S;}
            else {initialCondition[i+2*linksStructure.contactsArray.length]=thisHillsInfo.SCS_S1(i);}
           }}

        java.util.Date startTime=new java.util.Date();
        System.out.println("Start processing Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/3.0));
        System.out.println("Inicia simulacion RKF");

        double rain_per=(numPeriods+1)*rainDuration;
        System.out.println("Total running time = " + "Rain per = "+rain_per + "min + simulation time = "+ nummin + "min");
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,0.01,20/60.);


        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        System.out.println("storm.stormInitialTimeInMinutes()=" + storm.stormInitialTimeInMinutes());
        if(stormFile == null){
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period"+(k)+" out of "+numPeriods);
                 if(HillType==4 || HillType==6 || HillType==7) rainRunoffRaining.jumpsRunToAsciiFileHilltype4(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,5,initialCondition,newfile,newfile2,newfile3,newfile4,newfile5,newfile6,linksStructure,thisNetworkGeom,HillType);
                 else rainRunoffRaining.jumpsRunToAsciiFile_luciana(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,5,initialCondition,newfile,linksStructure,thisNetworkGeom);
                //rainRunoffRaining.jumpsRunToAsciiFileHilltype4(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,10,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }

            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");
            //the duration will be calculated using 30 km channel, average velocity in the channel equal to
            // 1.0m/s + runoff time in the hillslope + 3hours (*0.10 to guarantee it coverst the entire period)
            if(HillType==4|| HillType==6  || HillType==7) rainRunoffRaining.jumpsRunToAsciiFileHilltype4(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+nummin,5,initialCondition,newfile,newfile2,newfile3,newfile4,newfile5,newfile6,linksStructure,thisNetworkGeom,HillType);
            else rainRunoffRaining.jumpsRunToAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+nummin,5,initialCondition,newfile,linksStructure,thisNetworkGeom);
        } else {
            for (int k=0;k<numPeriods;k++) {
                System.out.println("Period "+(k+1)+" of "+numPeriods);
                if(HillType==4|| HillType==6 || HillType==7) rainRunoffRaining.jumpsRunToAsciiFileHilltype4(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),5,initialCondition,newfile,newfile2,newfile3,newfile4,newfile5,newfile6,linksStructure,thisNetworkGeom,HillType);
                else rainRunoffRaining.jumpsRunToAsciiFile_luciana(storm.stormInitialTimeInMinutes()+k*storm.stormRecordResolutionInMinutes(),storm.stormInitialTimeInMinutes()+(k+1)*storm.stormRecordResolutionInMinutes(),5,initialCondition,newfile,linksStructure,thisNetworkGeom);
                initialCondition=rainRunoffRaining.finalCond;
                rainRunoffRaining.setBasicTimeStep(10/60.);
            }

            java.util.Date interTime=new java.util.Date();
            System.out.println("Intermedia Time:"+interTime.toString());
            System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");

            if(HillType==4|| HillType==6 || HillType==7) rainRunoffRaining.jumpsRunToAsciiFileHilltype4(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+1440,15,initialCondition,newfile,newfile2,newfile3,newfile4,newfile5,newfile6,linksStructure,thisNetworkGeom,HillType);
            else rainRunoffRaining.jumpsRunToAsciiFile_luciana(storm.stormInitialTimeInMinutes()+numPeriods*storm.stormRecordResolutionInMinutes(),(storm.stormInitialTimeInMinutes()+(numPeriods+1)*storm.stormRecordResolutionInMinutes())+300,15,initialCondition,newfile,linksStructure,thisNetworkGeom);
        }

        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");


     //   double[] maximumsAchieved=rainRunoffRaining.getMaximumAchieved();

        //newfile.write("\n");
        //newfile.write("\n");
        //newfile.write("Maximum Discharge [m^3/s],");
        //for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1)
        //        newfile.write(maximumsAchieved[linksStructure.completeStreamLinksArray[i]]+",");
       // }


        newfile.close();
        bufferout.close();
        newfile2.close();
        bufferout2.close();
        newfile3.close();
        bufferout3.close();
        newfile4.close();
        bufferout4.close();
        newfile5.close();
        bufferout5.close();
        newfile6.close();
        bufferout6.close();
        System.out.println("Termina escritura de Resultados");


        //  Simulation log file
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"logfile.txt");
        System.out.println("Writing the log file - "+theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        newfile.write(demName + " demName \n");

        newfile.write(outputDirectory.getAbsolutePath() +  " Output_directory \n");
        newfile.write(DischargeFile +  " Discharge_File \n");
        newfile.write(RunoffFile +  " Precipitation_File \n");
        newfile.write(RunoffFile +  " Runoff_File \n");
        newfile.write(SubFile +  " Sub_File \n");
        newfile.write(PrecFile +  " AcumPrec_File \n");
        newfile.write(S0File +  " S0_File \n");
        newfile.write(S1File +  " S1_File \n");
        if(stormFile == null) {newfile.write(rainIntensity + " rain_insensity \n");
         newfile.write(rainDuration + " rain_duration \n");
         }
         else {newfile.write(stormFile.getAbsolutePath() + " Precipitation_File \n");}
         if(infiltMetaRaster == null) {newfile.write(infiltRate + " infiltRate \n");}
         else {newfile.write(infiltMetaRaster.getLocationMeta().getAbsolutePath() + " infiltrationfile \n");}

         newfile.write(routingType + " routing type \n");
         if(routingType==2) newfile.write(routingType + " routing type \n");
         if (routingType==5)
         {
         newfile.write(lam1 + " lambda1 \n");
         newfile.write(lam2 + " lambda2 \n");
         newfile.write(v_o + " vo \n");
         }

         newfile.write(HillType + " Hill type \n");
        newfile.close();
        bufferout.close();


    }

    public static void Gen_format(String Log) throws IOException{
        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        ruta = new FileReader(new java.io.File(Log));
        buffer=new BufferedReader(ruta);

         String data = buffer.readLine();
         data = buffer.readLine();
         data = buffer.readLine();
         String Disc = data.substring(0,data.lastIndexOf(" Discharge_File"));
         System.out.println("Disc = " + Disc);
         data = buffer.readLine();
         String Prec = data.substring(0,data.lastIndexOf(" Precipitation_File"));
         System.out.println("Prec = " + Prec);

         GenerateFilesPrec(Prec);
         GenerateFilesDisc(Disc);


    }

    public float[][] ReadGreenRoofFile(java.io.File GreenF,int NLI) throws IOException{

        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;

        ruta = new FileReader((GreenF));
        buffer=new BufferedReader(ruta);
        String data = buffer.readLine(); // HEADER
        data = buffer.readLine();

        float li = 0.f;
        float Co = 0.f;
        float Area =0.f;

        float[][] AreaArray=new float[1][NLI];
        for (int i=0;i<NLI;i++) {
        AreaArray[0][i]=0.0f;
        System.out.println(i+"  AreaArray[0][i]="+AreaArray[0][i]);
        }
        //String [] column1 = new String[10000];

        while (data != null)
        {
            tokens = new StringTokenizer(data,",");
            li=new Float(tokens.nextToken());
            Co=new Float(tokens.nextToken());
            Area=new Float(tokens.nextToken());

            System.out.println(NLI+"  link="+(li)+"  count="+Co+"  Area="+Area);
            int link=(int)(li-1);
            if(li>0 && (li-1)<NLI) AreaArray[0][link]=Area;
            System.out.println(AreaArray[0].length+"  link="+link+"  count="+Co+"  Area="+AreaArray[0][link]);
            data = buffer.readLine();
        }
        return AreaArray;
    }
    public static void GenerateFilesPrec(String Precip) throws IOException{

        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;

        ruta = new FileReader(new java.io.File(Precip));
        buffer=new BufferedReader(ruta);
        String data = buffer.readLine();

        double[] max = new double[20000];
        double[] aver = new double[20000];
        double[] dev = new double[20000];
        double[] min = new double[20000];
        double[][] matrix = new double[1000][20000];
        //String [] column1 = new String[10000];
        int il=0;
        int ic=0;
        int icc=0;
        while (data != null)
        {
            tokens = new StringTokenizer(data,",");

            while (tokens.hasMoreTokens()) {
             matrix[il][ic]=new Double(tokens.nextToken());
             //if (il==0) link[ic]=new Double(tokens.nextToken());
             //if (il==1) area[ic]=new Double(tokens.nextToken());
             //if(il>1)prec[il-2][ic]=new Double(tokens.nextToken());
             ic=ic+1;
             }
            icc=ic;
            ic=0;
            il=il+1;
        data = buffer.readLine();
        }
        ic=icc;
        int count=0;
        for (int i=2;i<(il);i++){
            for (int c=1;c<ic;c++){
                if(matrix[i][c]>=0){
                    aver[i]=aver[i]+matrix[i][c];
                    count=count+1;
                }
                if (max[i]<matrix[i][c]) max[i]=matrix[i][c];
                if (min[i]>matrix[i][c]&& matrix[i][c]>0) min[i]=matrix[i][c];
            }
           if (count>0) aver[i]=aver[i]/count;
           else aver[i]=-99.0;
           count=0;
         }
         count = 0;
         for (int i=2;i<(il);i++){
            for (int c=1;c<ic;c++){
                    dev[i]=dev[i]+(matrix[i][c]-aver[i])*(matrix[i][c]-aver[i]);
                    count = count +1;
            }

           if (count>0) dev[i]=Math.sqrt(dev[i]/count);
           else aver[i]=-99.0;
         }

       java.io.File theFile;

        theFile=new java.io.File(Precip.substring(0,Precip.lastIndexOf("."))+".asc");
        System.out.println("Writing Prec Functions - "+theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
         newfile.write("time "+"aver_prec "+"max_prec "+"min[i]_prec "+"dev[i]_prec \n");

         matrix[il]=matrix[il-1];
         for (int i=2;i<(il);i++){
             double time1=(matrix[i][0]-matrix[2][0])/(24*60);
             double time2=(matrix[i+1][0]-matrix[2][0])/(24*60);
             double time= time1-(1/(24*60*60));
             newfile.write(time+" "+"0.0000"+" "+"0.0000" +" "+"0.0000" +" "+ "0.0000" +"\n");
             newfile.write(time+" "+aver[i]+" "+max[i]+" "+min[i] +" "+ dev[i] +"\n");
             time= time2-(2/(24*60*60));
             newfile.write(time2+" "+aver[i]+" "+max[i]+" "+min[i] +" "+ dev[i] +"\n");
         }
        newfile.close();
        bufferout.close();

    }

    public static void GenerateFilesDisc(String Disc) throws IOException{

        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;

        ruta = new FileReader(new java.io.File(Disc));
        buffer=new BufferedReader(ruta);
        String data = buffer.readLine();

        double[][] matrix = new double[1000][20000];
        double[] Qmax = new double[20000];
        double[] timemax = new double[20000];
        double maxq = 0;

        int il=0;
        int icc=0;
        int ic=0;

        while (data != null)
        {
          tokens = new StringTokenizer(data,",");
          while (tokens.hasMoreTokens()) {
            matrix[il][ic]=new Double(tokens.nextToken());
             ic=ic+1;
            //System.out.println("ic - "+ic+"  il - "+il + "matrix[il][ic]" + matrix[il][ic]);
             }
            icc=ic;
            ic=0;
            il=il+1;
        data = buffer.readLine();
        }

        ic=icc;
        il=il-1; // just because I have the error
System.out.println("ic - "+ic+"  il - "+il);

        int[] IDindex=new int[10];
        int[] IDorder=new int[10];
        double[] IDarea=new double[10];
        String[] IDname=new String[10];

        for (int c=2;c<(ic);c++){
            if(matrix[0][c]==1462)
            {IDindex[0]=c;
             IDname[0]="Outlet";
             IDorder[05]=(int)matrix[1][c];
             IDarea[0]=matrix[2][c];}
            if(matrix[0][c]==2611)
            {IDindex[1]=c;
             IDname[1]="Mc_Alpine";
             IDorder[1]=(int)matrix[1][c];
            IDarea[1]=matrix[2][c];}
            if(matrix[0][c]==3129)
            {IDindex[2]=c;
            IDname[2]="Mc_Alpine";
            IDorder[2]=(int)matrix[1][c];
             IDarea[2]=matrix[2][c];}
            if(matrix[0][c]==12518)
            {IDindex[3]=c;
            IDname[3]="Sugar";
            IDorder[3]=(int)matrix[1][c];
             IDarea[3]=matrix[2][c];}
            if(matrix[0][c]==8236)
            {IDindex[4]=c;
             IDname[4]="Little_Sugar";
            IDorder[4]=(int)matrix[1][c];
             IDarea[4]=matrix[2][c];}
            if(matrix[0][c]==4249)
            {IDindex[5]=c;
             IDname[5]="Non_Urbanized";
             IDorder[5]=(int)matrix[1][c];
            IDarea[5]=matrix[2][c];}
        }


        int count=0;
        int maxorder=0;
        int indexmax=0;
        for (int c=1;c<ic;c++){
                Qmax[c]=0;
                timemax[c]=0;
            }
        for (int i=3;i<(il);i++){

            for (int c=1;c<ic;c++){
                if (maxorder<matrix[1][c]) {maxorder=(int)matrix[1][c];}
                if (maxq<matrix[i][c])
                {maxq=matrix[i][c];
                 indexmax=c;
                }


                if (Qmax[c]<matrix[i][c])
                {Qmax[c]=matrix[i][c];
                 timemax[c]=(matrix[i][0]-matrix[4][0])/(24*60);
                }
            }

         }
 for (int c=1;c<ic;c++){System.out.println("c - "+ c +"  Qmax[c] - "+Qmax[c]);  }
        java.io.File theFile;

        theFile=new java.io.File(Disc.substring(0,Disc.lastIndexOf("."))+"_Qmax.asc");
        System.out.println("Writing disc1 - "+theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
         newfile.write("order "+"area "+"Qmax "+"time to peak \n");
         for (int c=1;c<(ic);c++){
             newfile.write(matrix[1][c]+" "+matrix[2][c]+" "+Qmax[c]+" "+timemax[c] +"\n");
         }
        newfile.close();
        bufferout.close();


        theFile=new java.io.File(Disc.substring(0,Disc.lastIndexOf("."))+"hydrog.asc");
        System.out.println("Writing disc1 - "+theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);

        newfile = new java.io.OutputStreamWriter(bufferout);
        newfile.write("time "+IDname[0]+ " "+IDname[1]+ " "+IDname[2]+ " "+IDname[3]+ " "+IDname[4]+ " "+IDname[5]+ " "+" \n");
        newfile.write("area "+IDarea[0]+ " "+IDarea[1]+ " "+IDarea[2]+ " "+IDarea[3]+ " "+IDarea[4]+ " "+IDarea[5]+ " "+" \n");
        newfile.write("order "+IDorder[0]+ " "+IDorder[1]+ " "+IDorder[2]+ " "+IDorder[3]+ " "+IDorder[4]+ " "+IDorder[5]+ " "+" \n");
        for (int i=3;i<(il);i++){
             double time=(matrix[i][0]-matrix[3][0])/(24*60);
             newfile.write(time+" "+matrix[i][IDindex[0]]+" "+matrix[i][IDindex[1]]+matrix[i][IDindex[2]]+" "
             +" "+matrix[i][IDindex[3]]+" "+matrix[i][IDindex[4]]+" "+matrix[i][IDindex[5]]+"\n");
         }
        newfile.close();
        bufferout.close();

        double[] Qmaxaver = new double [maxorder+1];
        double[] Qmaxdev = new double [maxorder+1];
        int[] nelem = new int [maxorder+1];

        for (int io=1;io<=maxorder;io++){
            count=0;
            for (int c=1;c<ic;c++){
                if (matrix[1][c]==io) {
                    Qmaxaver[io]=Qmaxaver[io]+Qmax[c];
                    count=count+1;
                }
              }
            Qmaxaver[io]=Qmaxaver[io]/count;
            nelem[io]=count;
         }

        for (int io=1;io<=maxorder;io++){
            count=0;
            for (int c=1;c<ic;c++){
                if (matrix[1][c]==io) {
                    Qmaxdev[io]=Qmaxdev[io]+((Qmax[c]-Qmaxaver[io])*(Qmax[c]-Qmaxaver[io]));
                }
              }
            Qmaxdev[io]=Math.sqrt(Qmaxaver[io]/nelem[io]);
          }


        theFile=new java.io.File(Disc.substring(0,Disc.lastIndexOf("."))+"_Qmaxstat.asc");
        System.out.println("Writing disc2 - "+theFile);

        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
         newfile.write("order "+"nelements "+"Qmaxave "+"Qmaxstd " +"\n");
        for (int io=2;io<=maxorder;io++){
             newfile.write(io+" "+nelem[io]+" "+Qmaxaver[io]+" "+Qmaxdev[io]+"\n");
         }
        newfile.close();
        bufferout.close();

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
            subMainManning(args);
            //Reservoir(args);
            //GreenRoof(args);   //Charlotte radar
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

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        // main basin
        int x= 764;
        int y= 168;


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
        routingParams.put("ReserVolume",0.0f); // m3/km2 of reservation
        routingParams.put("ReserVolumetType",0.0f); // reservoir position:
        ////RUNS PARAMETERS //////////////
       int[] year_LC= {10};
       // 48.0f,66.04f,

       //int [] TR={100,500,50};
       int [] TR={100};

       //int [] TR={2};
       //float[] IC_array= {0.75f}; // ANTECEDENT SOIL MOISTURE CONDICTION
       float[] vr_array= {100000.0f}; // HILLSLOPE VELOCITY

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
            float C=0.75f;
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

                         java.io.File stormFile;
                         stormFile=new java.io.File(stormstring);
                           vsub=0.1f;
           //              routingParams.put("vrunoff",vrun);
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

                           OutputDir="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2h_IC0_v3/TR_"+trr+"/"+year+"/C"+C+"/";

                           //OutputDir="D:/CUENCAS/Charlote/results/KNRCS/"+year+"/"+intensity+"mm/"+duration+"min"+"/P5="+IC+"mm/"+"vsub="+vsub;
                           System.out.println("OutputDir="+OutputDir);
                              new File(OutputDir).mkdirs();

                           //OutputDir="D:/CUENCAS/Charlote/results/1992/Param_vel_Delay0/v=1.0"+"/SM"+vm+"/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";
                           new File(OutputDir).mkdirs();
                           String path = OutputDir;
                           //String rain = RaininputDir+"/bin/"+precname;
 String Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
                            new SimulationToFileLuciana(x,y,matDirs,magnitudes,metaModif,stormFile,infiltr,new java.io.File(path),new java.io.File(LandUse),new java.io.File(Soil),new java.io.File(Soil150SWAData),routingParams).executeSimulation();
                            path = OutputDir+"/logfile.txt";

                            //Gen_format(path);

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
        int x= 764;
        int y= 168;


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
        routingParams.put("Hill Type",7.f);  // check NetworkEquationsLuciana.java for definitions
        routingParams.put("Hill Velocity Type",3.f);  // check NetworkEquationsLuciana.java for definitions
        routingParams.put("vrunoff",250.f); // define a constant number or -9.9 for vel=f(land Cover)
        routingParams.put("vssub",1.0f);
        routingParams.put("SoilMoisture",2.f);
        routingParams.put("lambdaSCSMethod",0.05f);
        routingParams.put("Vconst",0.5f); // CHANGE IN THE NETWORKEQUATION CLASS
        routingParams.put("InitialCondition",0.001f); // Porcentage of the soil filled up with water
        routingParams.put("P5Condition",-9.0f); // Porcentage of the soil filled up with water
         routingParams.put("ReserVolume",100.0f); // m3/km2 of reservation
        routingParams.put("ReserVolumetType",1.0f); // reservoir position:


        ////RUNS PARAMETERS //////////////
       int[] cen= {1,2};
       //int[] cen= {3,4};
       // 48.0f,66.04f,

       //int [] TR={100};
       int [] TR={100};

       //int [] TR={2};
       float[] IC_array= {0.0f}; // ANTECEDENT SOIL MOISTURE CONDICTION
       float[] vr_array= {100000.0f}; // HILLSLOPE VELOCITY

       float[] vs_array= {0.1f}; // SUBSURFACE FLOW VELOCITY
       float infiltr=0.0f;
       String LandCoverName = "error";
       String Dir="error";
       String SoilName="error";
       float vrun,vsub,IC;
       int year=2001;

         for (int tr :TR)
           {int trr=tr;
           for (int iy:cen)
           {int CC=iy;
            String stormstring ="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/ras_2h/TR_"+trr+"/prec.metaVHC";
            IC=0.0f;

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

                         java.io.File stormFile;
                         stormFile=new java.io.File(stormstring);
                           vsub=0.1f;
           //              routingParams.put("vrunoff",vrun);
                           routingParams.put("vssub",vsub);
                           routingParams.put("P5Condition",IC);
                           ///// LANDCOVERDATA /////
                          String green;
                          Dir="D:/CUENCAS/Charlote/Rasters/Land_surface_Data/LCGreenRoof2/";
                          green=Dir+"cen" + CC +".csv";

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

                           String Soil = Dir+SoilName;

                             //routingParams.put("SoilMoisture",vm);
                           //OutputDir="D:/CUENCAS/Charlote/results/2001/test/Mishra/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";

                           OutputDir="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/results_2h_IC0_v2/GreenRoof/"+"TR_"+trr+"/C"+CC+"/test/";
                           //OutputDir="D:/CUENCAS/Charlote/results/KNRCS/"+year+"/"+intensity+"mm/"+duration+"min"+"/P5="+IC+"mm/"+"vsub="+vsub;
                           System.out.println("OutputDir="+OutputDir);
                              new File(OutputDir).mkdirs();

                           //OutputDir="D:/CUENCAS/Charlote/results/1992/Param_vel_Delay0/v=1.0"+"/SM"+vm+"/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";
                           new File(OutputDir).mkdirs();
                           String path = OutputDir;

                           //String rain = RaininputDir+"/bin/"+precname;

    String Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
                            new SimulationToFileLuciana(x,y,matDirs,magnitudes,metaModif,stormFile,infiltr,new java.io.File(path),new java.io.File(LandUse),new java.io.File(Soil),new java.io.File(Soil150SWAData),routingParams).executeSimulation();
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

        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        // main basin
        int x= 764;
        int y= 168;


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
       int[] year_LC= {2001};
       // 48.0f,66.04f,

       //int [] TR={100,500,50};
       int [] TR={100,2};
       float [] RV={1000.f,10000.f};
       float [] RVT={1.f};
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
           for (int iy:year_LC)
           {int year=iy;
            String stormstring ="D:/CUENCAS/Charlote/Sites/precipitation/SintPrec/ras_2h/TR_"+trr+"/prec.metaVHC";
            IC=0.0f;
            int RT=0;
            for (float rvt:RVT)
            {float rvolt=rvt;
            for (float rv:RV)
            {float rvol=rv;
             System.out.println("Volume = " + rvol + "Reserv volume type" + rvt);
             routingParams.put("ReserVolume",rvol); // m3/km2 of reservation
              routingParams.put("ReserVolumetType",rvt); // reservoir position:
               java.io.File stormFile;
               stormFile=new java.io.File(stormstring);
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
                              new File(OutputDir).mkdirs();

                           //OutputDir="D:/CUENCAS/Charlote/results/1992/Param_vel_Delay0/v=1.0"+"/SM"+vm+"/delay/vr="+vrun+"/vs="+vsub+"/"+intensity+"/"+duration+"/";
                           new File(OutputDir).mkdirs();
                           String path = OutputDir;
                           //String rain = RaininputDir+"/bin/"+precname;

    String Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
                            new SimulationToFileLuciana(x,y,matDirs,magnitudes,metaModif,stormFile,infiltr,new java.io.File(path),new java.io.File(LandUse),new java.io.File(Soil),new java.io.File(Soil150SWAData),routingParams).executeSimulation();
                    path = OutputDir+"/logfile.txt";

                            //Gen_format(path);
            }
            }
           }

       }

}

}