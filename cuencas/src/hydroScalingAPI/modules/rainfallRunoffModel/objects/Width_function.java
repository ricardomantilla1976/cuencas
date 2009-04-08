/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 *
 * @author lcunha
 */
public class Width_function {

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;

    int x;
    int y;
    int[][] magnitudes;
    java.io.File LandUseFile;
    java.io.File outputDirectory;

   public Width_function(int xx, int yy, byte[][] direcc, int[][] magnitudesOR,
            hydroScalingAPI.io.MetaRaster md, java.io.File LandUseFileOR,java.io.File outputDirectoryOR) throws java.io.IOException {

        matDir=direcc;
        metaDatos=md;

        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        LandUseFile=LandUseFileOR;
        outputDirectory=outputDirectoryOR;
   }

    public void ExecuteWidthFunction() throws java.io.IOException {

        System.out.println("Start to run Width function");
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);

        hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager LandUse;

        LandUse=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LandUseManager(LandUseFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);

        thisHillsInfo.setLandUseManager(LandUse);

        //////////////////////////////////////
        System.out.println("Start to run Width function");
        java.io.File theFile;
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+"LandUse"+".csv");

        System.out.println(theFile);

        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);


 //     newfile.write("Information of precipition for each link\n");
 //     newfile.write("Links at the bottom of complete streams are:\n");
 //     newfile.write(1,");
        System.out.println("Open the file");
        newfile.write("1 ");
        double RC = -9.9;
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1){
 //               newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+" ");
                newfile.write(linksStructure.completeStreamLinksArray[i]+" ");
                newfile.write(thisNetworkGeom.Slope(linksStructure.completeStreamLinksArray[i])+" ");
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+" ");
                newfile.write(thisHillsInfo.Area(i)+" ");
                newfile.write(thisHillsInfo.LandUse(i)+" ");
                newfile.write(thisHillsInfo.LandUsePerc(i)+" ");
                if(thisHillsInfo.LandUse(i)==0) RC=1;
                if(thisHillsInfo.LandUse(i)==1) RC=0.6;
                if(thisHillsInfo.LandUse(i)==2) RC=0.2;
                if(thisHillsInfo.LandUse(i)==3) RC=0.1;
                if(thisHillsInfo.LandUse(i)==4) RC=0.2;
                if(thisHillsInfo.LandUse(i)==5) RC=0.4;
                if(thisHillsInfo.LandUse(i)==6) RC=0.4;
                if(thisHillsInfo.LandUse(i)==7) RC=0.6;
                if(thisHillsInfo.LandUse(i)==8) RC=0.45;
                if(thisHillsInfo.LandUse(i)==9) RC=1;
                newfile.write(RC+" ");
                RC=RC*thisHillsInfo.Area(i);
                newfile.write(RC+" ");
                newfile.write("\n");
            }
        }
        System.out.println("Termina escritura de LandUse");

        newfile.close();
        bufferout.close();
 ////////////////////////
        String demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+"Top_WF_LU"+".wfs.csv");


   System.out.println("Writing Width Functions - "+theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        int nclass=10;
        double[][][] wfs=linksStructure.getLandUseWidthFunctions(linksStructure.completeStreamLinksArray,thisHillsInfo,0);
        for (int l=0;l<linksStructure.completeStreamLinksArray.length;l++){
          for (int c=0;c<nclass;c++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1){
                newfile.write(linksStructure.completeStreamLinksArray[l]+" ");
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(c+" ");
                for (int j=0;j<wfs[l][c].length;j++) newfile.write(wfs[l][c][j]+" ");
                newfile.write("\n");
            }
        }
      }
    System.out.println("Finish writing the function - "+theFile);
    newfile.close();

    demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
    theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+"GeomWF_LU"+".wfs.csv");


   System.out.println("Writing Width Functions - "+theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        nclass=10;
        wfs=linksStructure.getLandUseWidthFunctions(linksStructure.completeStreamLinksArray,thisHillsInfo,1);
        for (int l=0;l<linksStructure.completeStreamLinksArray.length;l++){
          for (int c=0;c<nclass;c++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1){
                newfile.write(linksStructure.completeStreamLinksArray[l]+" ");
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(c+" ");
                for (int j=0;j<wfs[l][c].length;j++) newfile.write(wfs[l][c][j]+" ");
                newfile.write("\n");
            }
        }
      }
    System.out.println("Finish writing the function - "+theFile);
    newfile.close();

    // color code ordere
    bufferout.close();
    newfile.close();
    bufferout.close();
 ////////////////////////
    demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
    theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+"Top_WF_ORDER"+".wfs.csv");


   System.out.println("Writing Width Functions - "+theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        nclass=11;

        double[][][] wfs2=linksStructure.getCCWidthFunctions(linksStructure.completeStreamLinksArray,thisNetworkGeom.getLinkOrderArray(),0,11);
        for (int l=0;l<linksStructure.completeStreamLinksArray.length;l++){
          for (int c=0;c<nclass;c++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1){
                newfile.write(linksStructure.completeStreamLinksArray[l]+" ");
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(c+" ");
                for (int j=0;j<wfs2[l][c].length;j++) newfile.write(wfs2[l][c][j]+" ");
                newfile.write("\n");
            }
        }
      }
    System.out.println("Finish writing the function - "+theFile);
    newfile.close();
    bufferout.close();

    // color code ordere
    bufferout.close();
    newfile.close();
    bufferout.close();
 ////////////////////////
    demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
    theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+"Geom_WF_ORDER"+".wfs.csv");


   System.out.println("Writing Width Functions - "+theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        nclass=11;
        System.out.println("defining the slope classes for order- "+linksStructure.completeStreamLinksArray.length);
        wfs2=linksStructure.getCCWidthFunctions(linksStructure.completeStreamLinksArray,thisNetworkGeom.getLinkOrderArray(),1,11);
        for (int l=0;l<linksStructure.completeStreamLinksArray.length;l++){
          for (int c=0;c<nclass;c++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1){
                newfile.write(linksStructure.completeStreamLinksArray[l]+" ");
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(c+" ");
                for (int j=0;j<wfs2[l][c].length;j++) newfile.write(wfs2[l][c][j]+" ");
                newfile.write("\n");
            }
        }
      }
    System.out.println("Finish writing the function - "+theFile);
    newfile.close();
    bufferout.close();

     ////////////////////////
    demName=metaDatos.getLocationBinaryFile().getName().substring(0,metaDatos.getLocationBinaryFile().getName().lastIndexOf("."));
    theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+"Geom_WF_Slope"+".wfs.csv");


   System.out.println("Writing Width Functions - "+theFile);
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);

        float[][] Slopeinfo=thisNetworkGeom.getSlopeArray();
        float[][] Slopeclass=new float[1][Slopeinfo[0].length];
        System.out.println("Slopeinfo.length- "+Slopeinfo[0].length);
        for (int l=0;l<Slopeinfo[0].length;l++){
            Slopeclass[0][l]=9.0f;
        if (Slopeinfo[0][l]>=0.0f && Slopeinfo[0][l]<0.025f) Slopeclass[0][l]=0.0f;
        if (Slopeinfo[0][l]>=0.025f && Slopeinfo[0][l]<0.05f) Slopeclass[0][l]=1.0f;
        if (Slopeinfo[0][l]>=0.05f && Slopeinfo[0][l]<0.075f) Slopeclass[0][l]=2.0f;
        if (Slopeinfo[0][l]>=0.075f && Slopeinfo[0][l]<0.1f) Slopeclass[0][l]=3.0f;
        if (Slopeinfo[0][l]>=0.1f && Slopeinfo[0][l]<0.125f) Slopeclass[0][l]=4.0f;
        if (Slopeinfo[0][l]>=0.125f && Slopeinfo[0][l]<0.15f) Slopeclass[0][l]=5.0f;
        if (Slopeinfo[0][l]>=0.15f && Slopeinfo[0][l]<0.2f) Slopeclass[0][l]=6.0f;
        if (Slopeinfo[0][l]>=0.2f && Slopeinfo[0][l]<0.25f) Slopeclass[0][l]=7.0f;
        if (Slopeinfo[0][l]>=0.25f && Slopeinfo[0][l]<0.5f) Slopeclass[0][l]=8.0f;
        }

        nclass=10;

        wfs2=linksStructure.getCCWidthFunctions(linksStructure.completeStreamLinksArray,Slopeclass,0,10);
        for (int l=0;l<linksStructure.completeStreamLinksArray.length;l++){
          for (int c=0;c<nclass;c++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l]) > 1){
                newfile.write(linksStructure.completeStreamLinksArray[l]+" ");
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[l])+" ");
                newfile.write(c+" ");
                for (int j=0;j<wfs2[l][c].length;j++) newfile.write(wfs2[l][c][j]+" ");
                newfile.write("\n");
            }
        }
      }
    System.out.println("Finish writing the function - "+theFile);
    newfile.close();
    bufferout.close();

 }
       /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        try{
            //subMain(args);     //Case Whitewater
            //subMain1(args);     //Case 11110103
            //subMain2(args);     //Case  Rio Puerco
            //subMain3(args);     //Case  11070208
            //subMain4(args);     //Case  11140102
            //subMain5(args);     //Iowa River
            subMain6(args);     //Charlotte
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

 public static void subMain(String args[]) throws java.io.IOException {

   String pathinput = "C:/CUENCAS/Whitewater_database/Rasters/Topography/1_ArcSec_USGS_2005/";
   java.io.File theFile=new java.io.File(pathinput + "Whitewaters" + ".metaDEM");
   hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
   metaModif.setLocationBinaryFile(new java.io.File(pathinput + "Whitewaters" + ".dir"));

   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   int x = 1063;
   int y = 496;

   String precname="LC2001_cliped.metaVHC";
   String Dir="C:/CUENCAS/Whitewater_database/Rasters/Hydrology/Land_info/LandCover2001_cliped/";
   new java.io.File(Dir+"/test/").mkdirs();
   String OutputDir=Dir+"/test/";

   String LandUse = Dir+precname;

   new Width_function(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(OutputDir)).ExecuteWidthFunction();

    }

 public static void subMain1(String args[]) throws java.io.IOException {

  String pathinput = "C:/CUENCAS/11110103/Rasters/Topography/";
  java.io.File theFile=new java.io.File(pathinput + "NED_71821716" + ".metaDEM");
  hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
  metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_71821716" + ".dir"));

   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   String precname="p_lc_2001.metaVHC";
   String Dir="C:/CUENCAS/11110103/Rasters/Hydrology/LandCover2001/";
   new java.io.File(Dir+"/07196500/").mkdirs();
   String OutputDir=Dir+"/07196500/";

   int x = 497;
   int y = 773;

   String LandUse = Dir+precname;

   new Width_function(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(OutputDir)).ExecuteWidthFunction();

   new java.io.File(Dir+"/07197000/").mkdirs();
   OutputDir=Dir+"/07197000/";

   x = 804;
   y = 766;

   new Width_function(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(OutputDir)).ExecuteWidthFunction();

    }
 public static void subMain2(String args[]) throws java.io.IOException {

   String pathinput = "C:/CUENCAS/Upper Rio Puerco DB/Rasters/Topography/1_ArcSec/";
   java.io.File theFile=new java.io.File(pathinput + "NED_54212683" + ".metaDEM");
   hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
   metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_54212683" + ".dir"));

   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   int x = 381;
   int y = 221;

  //String precname="landcover1992.metaVHC";
  // String Dir="C:/CUENCAS/Upper Rio Puerco DB/Rasters/Hydrology/LandCover1992/";
   String precname="Landcover2001.metaVHC";
   String Dir="C:/CUENCAS/Upper Rio Puerco DB/Rasters/Hydrology/LandCover2001/";
   new java.io.File(Dir+"/wf/").mkdirs();
   String OutputDir=Dir+"/wf/";

   String LandUse = Dir+precname;

   new Width_function(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(OutputDir)).ExecuteWidthFunction();

    }

 public static void subMain3(String args[]) throws java.io.IOException {

   String pathinput = "C:/CUENCAS/11070208/Rasters/Topography/";
   java.io.File theFile=new java.io.File(pathinput + "NED_23370878" + ".metaDEM");
   hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
   metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_23370878" + ".dir"));

   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

  int x = 296;
  int y = 1167;


   String precname="landcover2001.metaVHC";
   String Dir="C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
   //String precname="landcover2001.metaVHC";
   //String Dir="C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
   new java.io.File(Dir+"/wf/").mkdirs();
   String OutputDir=Dir+"/wf/";

   String LandUse = Dir+precname;

   new Width_function(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(OutputDir)).ExecuteWidthFunction();

    }

  public static void subMain4(String args[]) throws java.io.IOException {

  String pathinput = "C:/CUENCAS/11140102/Rasters/Topography/";
  java.io.File theFile=new java.io.File(pathinput + "NED_20864854" + ".metaDEM");
  hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
  metaModif.setLocationBinaryFile(new java.io.File(pathinput + "NED_20864854" + ".dir"));


   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   int x = 2444;
   int y = 1610;


    String precname="landcover2001.metaVHC";
   String Dir="C:/CUENCAS/11140102/Rasters/Hydrology/LandCover2001/";
   //String precname="landcover2001.metaVHC";
   //String Dir="C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
   new java.io.File(Dir+"/wf/").mkdirs();
   String OutputDir=Dir+"/wf/";

   String LandUse = Dir+precname;

   new Width_function(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(OutputDir)).ExecuteWidthFunction();


    }

    public static void subMain5(String args[]) throws java.io.IOException {

  String pathinput = "C:/CUENCAS/Iowa_river/Rasters/Topography/DEMS-Iowa/Averaged/";
  java.io.File theFile=new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".metaDEM");
  hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
  metaModif.setLocationBinaryFile(new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".dir"));


   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   // Iowa River at Marengo x = 2256 y=876
   int x =2256;
   int y = 876;
   // Cedar river at cedar rapids - x = 2734 y=1069
   //int x =2734;
  // int y =1069;
  // Iowa River at Wapello - x = 2734 y=1069
   //int x =3316;
  // int y =116;

    String precname="landcover2001_90_2.metaVHC";
   String Dir="C:/CUENCAS/Iowa_river/Rasters/Hydrology/LandCover2001/";
   //String precname="landcover2001.metaVHC";
   //String Dir="C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
   new java.io.File(Dir+"/wfmarengo/").mkdirs();
   String OutputDir=Dir+"/wfmarengo/";

   String LandUse = Dir+precname;

   new Width_function(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(OutputDir)).ExecuteWidthFunction();


    }

        public static void subMain6(String args[]) throws java.io.IOException {

  String pathinput = "C:/CUENCAS/Charlote/Rasters/Topography/Charlotte_1AS/";
  java.io.File theFile=new java.io.File(pathinput + "charlotte" + ".metaDEM");
  hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
  metaModif.setLocationBinaryFile(new java.io.File(pathinput + "charlotte" + ".dir"));


   String formatoOriginal=metaModif.getFormat();
   metaModif.setFormat("Byte");
   byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
   metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
   metaModif.setFormat("Integer");
   int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

   int x= 764;
   int y= 168;
//int x= 638;
//int y= 605; //Basin Code 33714519239827667
//int x= 640;
//int y= 604; //Basin Code 9506327795703752
//int x= 664;
//int y= 494; //Basin Code 901512727378702
//int x= 619;
//int y= 433; //Basin Code 3347976315469169

   String precname="raster_cliped.metaVHC";
   String Dir="C:/CUENCAS/Charlote/Rasters/Land_surface_Data/LandCover2001/";
   //String precname="landcover2001.metaVHC";
   //String Dir="C:/CUENCAS/11070208/Rasters/Hydrology/LandCover2001/";
   new java.io.File(Dir+"/wf_1AS/"+x+"_"+y+"/").mkdirs();
   String OutputDir=Dir+"/wf_1AS/"+x+"_"+y+"/";

   String LandUse = Dir+precname;

   new Width_function(x,y,matDirs,magnitudes,metaModif,new java.io.File(LandUse),new java.io.File(OutputDir)).ExecuteWidthFunction();


    }
}
