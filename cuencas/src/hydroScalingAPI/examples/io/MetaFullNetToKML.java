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
 * MetaNetToKML.java
 *
 * Created on October 10, 2008, 5:00 PM
 */

package hydroScalingAPI.examples.io;

/**
 *
 * @author Ricardo Mantilla
 */
public class MetaFullNetToKML {
    
    public MetaFullNetToKML(hydroScalingAPI.io.MetaRaster metaModif, java.io.File outputDirectory,byte [][] matDir,String uniqueIdentifier) throws java.io.IOException{
        
        
        hydroScalingAPI.io.MetaRaster metaDatos=metaModif;
        
        hydroScalingAPI.io.MetaNetwork netStructure=new hydroScalingAPI.io.MetaNetwork(metaModif);
        
        java.io.File fileSalida;
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          ret="\n";
        
        fileSalida=new java.io.File(outputDirectory+"/RiverNetworkHighRes_"+uniqueIdentifier+".kml");
        
        
        outputDir = new java.io.FileOutputStream(fileSalida);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);
        
        newfile.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+ret);
        newfile.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        newfile.write("<Document>"+ret);
        newfile.write("  <name>RiverNetwork_"+uniqueIdentifier+".kml</name>"+ret);
        newfile.write("  <open>0</open>"+ret);
        
        for(int i=1;i<=netStructure.getLargestOrder();i++){
            newfile.write("  <Style id=\"linestyleO"+i+"\">"+ret);
            newfile.write("    <LineStyle>"+ret);
            newfile.write("      <color>7f"+Integer.toHexString(i*20)+"0000</color>"+ret);
            newfile.write("      <width>"+i+"</width>"+ret);
            newfile.write("    </LineStyle>"+ret);
            newfile.write("  </Style>"+ret);
        }
        for(int i=1;i<=netStructure.getLargestOrder();i++){
            System.out.println("Working on order "+i+"s");
            newfile.write("  <Folder>"+ret);
            newfile.write("    <name>Order "+i+" Streams</name>"+ret);
            newfile.write("    <visibility>1</visibility>"+ret);
            newfile.write("    <open>0</open>"+ret);
            netStructure.getLineStringXYs(i,newfile);
            newfile.write("  </Folder>"+ret);
        }

        newfile.write("</Document>"+ret);
        newfile.write("</kml>"+ret);
        
        newfile.close();
        bufferout.close();
        outputDir.close();  
        
    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        main0(args);
        //main1(args);
    }
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main0(String[] args) {
        
        try{

//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/TurkeyRiverAtElkader/NED_53652717";
//            int x=4498  , y= 432;
//            String uniqueIdentifier="TurkeyRiverAtElkader";
//
//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/TurkeyRiverAtElkader/NED_53652717";
//            int x=2950,   y= 1209;
//            String uniqueIdentifier="TurkeyRiverAtElDorado";


//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions";
//            int x=1570,y=127;
//            String uniqueIdentifier="IowaRiverAtWapoello90";
            
//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011";
//            String uniqueIdentifier="ClearCreekDEM";
            
//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res";
//            String uniqueIdentifier="Iowa100mDEM";

//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/HucTiledIowa/fullIowaDem";
//            String uniqueIdentifier="Iowa30mDEM";

//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver";
//            int x=7875, y= 1361;
//            String uniqueIdentifier="CedarRiverAtCedarRapids";
            
//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/IowaRiverAtIowaCity";
//            int x=6602, y= 1539;
//            String uniqueIdentifier="IowaRiverAtIowaCity";
            
//            String fileName="/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters";
//            int x=1064, y= 496;
//            String uniqueIdentifier="WhitewaterKS";

//            String fileName="/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters";
//            int x=1007, y= 1177;
//            String uniqueIdentifier="WhitewaterZ1KS";

//            String fileName="/CuencasDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters";
//            int x=736, y= 1414;
//            String uniqueIdentifier="WhitewaterZ2KS";

//            String fileName="/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03";
//            int x=44, y= 111;
//            String uniqueIdentifier="GoodwinCreek";

//            String fileName="/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated";
//            int x=194, y= 281 ;
//            String uniqueIdentifier="WalnutGulchAZ";

//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246";
//            int x=866, y= 480 ;
//            String uniqueIdentifier="DryCreekIA";
            
//            String fileName="/CuencasDataBases/Nicaragua/Rasters/Topography/nicaragua/section_1414_3151_2105_2523_W100N40";
//            String uniqueIdentifier="Nicaragua";

//            String fileName="/CuencasDataBases/IPHEX_Database/Rasters/Topography/section_2855_4617_1633_5732_gt30w100n40_dem";
//            String uniqueIdentifier="IPHEX_dem";

            
            String fileName="/CuencasDataBases/SierraNevadaSantaMarta/Rasters/Topography/mosaic_n11_w075_3arc_v2_bil";
            String uniqueIdentifier="SierraNevadaDeSantaMarta";

//            String fileName="/CuencasDataBases/Nicaragua/Rasters/Topography/3arcsec/N12W086";
//            String uniqueIdentifier="LasLagunasBoaco";
            
            java.io.File theFile=new java.io.File(fileName+".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(fileName+".dir"));
        
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            MetaFullNetToKML exporter=new MetaFullNetToKML(metaModif,new java.io.File("/Users/ricardo/temp/"),matDirs,uniqueIdentifier);
            
        } catch (Exception IOE){
            System.out.print(IOE);
            IOE.printStackTrace();
            System.exit(0);
        }
        
        System.exit(0);
        
    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main1(String[] args) {

        try{

            String[] basins=new hydroScalingAPI.io.BasinsLogReader(new java.io.File("/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res.log")).getPresetBasins();

            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcSec/res";
            
            
            for (int i = 0; i < basins.length; i++) {
                String[] basLabel = basins[i].split(";");

                int x=Integer.parseInt(basLabel[0].split(",")[0].split("x:")[1].trim());
                int y=Integer.parseInt(basLabel[0].split(",")[1].split("y:")[1].trim());
                
                String uniqueIdentifier=basLabel[1];

                System.out.println(x+" "+y+" "+uniqueIdentifier);


                java.io.File theFile=new java.io.File(fileName+".metaDEM");
                hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
                metaModif.setLocationBinaryFile(new java.io.File(fileName+".dir"));

                metaModif.setFormat("Byte");
                byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

                MetaFullNetToKML exporter=new MetaFullNetToKML(metaModif,new java.io.File("/Users/ricardo/temp/cities/"),matDirs,uniqueIdentifier);

            }
        } catch (Exception IOE){
            System.out.print(IOE);
            IOE.printStackTrace();
            System.exit(0);
        }

        System.exit(0);

    }

}

