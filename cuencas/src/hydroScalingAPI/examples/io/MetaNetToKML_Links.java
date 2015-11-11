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
public class MetaNetToKML_Links {
    
    public MetaNetToKML_Links(hydroScalingAPI.io.MetaRaster metaModif, java.io.File outputDirectory,hydroScalingAPI.util.geomorphology.objects.Basin myCuenca,byte [][] matDir, int[][] magnitudes,String uniqueIdentifier) throws java.io.IOException{
        
        
        float xO=myCuenca.getLonLatBasin()[0][0];
        float yO=myCuenca.getLonLatBasin()[1][0];
        
        hydroScalingAPI.io.MetaRaster metaDatos=metaModif;
        
        hydroScalingAPI.io.MetaNetwork netStructure=new hydroScalingAPI.io.MetaNetwork(metaModif);
        
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.io.MetaPolygon metaPolyToWrite=new hydroScalingAPI.io.MetaPolygon ();
        metaPolyToWrite.setName(myCuenca.toString());
        metaPolyToWrite.setCoordinates(myCuenca.getLonLatBasinDivide());
        metaPolyToWrite.setInformation("Basin Divide as captured by Cuencas");

        java.io.File fileSalida=new java.io.File(outputDirectory+"/Divide_"+uniqueIdentifier+".kml");
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          ret="\n";
        

        metaPolyToWrite.writeKmlPolygon(fileSalida,uniqueIdentifier);

        int[][] basLinkIds=new int[metaModif.getNumRows()][metaModif.getNumCols()];

        int xOulet,yOulet;
        hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

        int demNumCols=metaModif.getNumCols();

        for (int i=0;i<linksStructure.contactsArray.length;i++){
            if (linksStructure.magnitudeArray[i] < linksStructure.basinMagnitude){

                xOulet=linksStructure.contactsArray[i]%demNumCols;
                yOulet=linksStructure.contactsArray[i]/demNumCols;

                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaModif);
                int[][] xyHillSlope=myHillActual.getXYHillSlope();
                for (int j=0;j<xyHillSlope[0].length;j++){
                    basLinkIds[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;

                }
            } else {
                int x=myCuenca.getXYBasin()[0][0];
                int y=myCuenca.getXYBasin()[1][0];
                
                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDir,magnitudes,metaModif);
                int[][] xyHillSlope=myHillActual.getXYHillSlope();
                for (int j=0;j<xyHillSlope[0].length;j++){
                    basLinkIds[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;
                }
            }
        }

        fileSalida=new java.io.File(outputDirectory+"/RiverNetworkHighRes_"+uniqueIdentifier+".kml");
        
        
        outputDir = new java.io.FileOutputStream(fileSalida);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);
        
        newfile.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+ret);
        newfile.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        newfile.write("<Document>"+ret);
        newfile.write("  <name>RiverNetwork_"+uniqueIdentifier+".kml</name>"+ret);
        newfile.write("  <open>0</open>"+ret);
        newfile.write("  <LookAt>"+ret);
        newfile.write("    <longitude>"+xO+"</longitude>"+ret);
        newfile.write("    <latitude>"+yO+"</latitude>"+ret);
        newfile.write("    <altitude>0</altitude>"+ret);
        newfile.write("    <range>1500</range>"+ret);
        newfile.write("    <tilt>30</tilt>"+ret);
        newfile.write("    <heading>0</heading>"+ret);
        newfile.write("  </LookAt>"+ret);
        
        for(int i=1;i<=linksStructure.basinOrder;i++){
            newfile.write("  <Style id=\"linestyleO"+i+"\">"+ret);
            newfile.write("    <LineStyle>"+ret);
            newfile.write("      <color>7f"+Integer.toHexString(i*20)+"0000</color>"+ret);
            newfile.write("      <width>"+i+"</width>"+ret);
            newfile.write("    </LineStyle>"+ret);
            newfile.write("  </Style>"+ret);
        }
        for(int i=1;i<=linksStructure.basinOrder;i++){
            newfile.write("  <Folder>"+ret);
            newfile.write("    <name>Order "+i+" Streams</name>"+ret);
            newfile.write("    <visibility>1</visibility>"+ret);
            newfile.write("    <open>0</open>"+ret);
            netStructure.getLinkLineStringXYs(i,basLinkIds,newfile);
            newfile.write("  </Folder>"+ret);
        }

        newfile.write("</Document>"+ret);
        newfile.write("</kml>"+ret);
        
        newfile.close();
        bufferout.close();
        outputDir.close();

//        fileSalida=new java.io.File(outputDirectory+"/LinkNumbersHighRes_"+uniqueIdentifier+".kml");
//
//
//        outputDir = new java.io.FileOutputStream(fileSalida);
//        bufferout=new java.io.BufferedOutputStream(outputDir);
//        newfile=new java.io.OutputStreamWriter(bufferout);
//
//        newfile.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+ret);
//        newfile.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
//        newfile.write("<Document>"+ret);
//        newfile.write("  <name>RiverNetwork.kml</name>"+ret);
//        newfile.write("  <open>1</open>"+ret);
//        newfile.write("  <LookAt>"+ret);
//        newfile.write("    <longitude>"+xO+"</longitude>"+ret);
//        newfile.write("    <latitude>"+yO+"</latitude>"+ret);
//        newfile.write("    <altitude>0</altitude>"+ret);
//        newfile.write("    <range>1500</range>"+ret);
//        newfile.write("    <tilt>30</tilt>"+ret);
//        newfile.write("    <heading>0</heading>"+ret);
//        newfile.write("  </LookAt>"+ret);
//        newfile.write("  <Folder>"+ret);
//        newfile.write("    <open>0</open>"+ret);
//        for(int i=0;i<linksStructure.contactsArray.length;i++){
//
//            newfile.write("  <Placemark>"+ret);
//            newfile.write("  <name>Link ID: "+i+"</name>"+ret);
//            newfile.write("     <Point>"+ret);
//
//            double longitude=(linksStructure.contactsArray[i]%metaModif.getNumCols()+0.5)*metaModif.getResLon()/3600.0D+metaModif.getMinLon();
//            double latitude=(linksStructure.contactsArray[i]/metaModif.getNumCols()+0.5)*metaModif.getResLat()/3600.0D+metaModif.getMinLat();
//
//            System.out.println(longitude+","+latitude);
//
//            newfile.write("         <coordinates>"+longitude+","+latitude+"</coordinates>"+ret);
//            newfile.write("     </Point>"+ret);
//            newfile.write("  </Placemark>"+ret);
//
//        }
//        newfile.write("  </Folder>"+ret);
//
//        newfile.write("</Document>"+ret);
//        newfile.write("</kml>"+ret);
//
//        newfile.close();
//        bufferout.close();
//        outputDir.close();
        
        
        
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

            String fileName="/CuencasDataBases/IPHEX_Database/Rasters/Topography/PeeDeeBurned_90m";
            int x=3422 , y= 814;
            String uniqueIdentifier="PeeDee";

//            String fileName="/CuencasDataBases/TurkeyRiver_DB/Rasters/Topography/turkeyburnll";
//            int x=28104  , y= 2815;
//            String uniqueIdentifier="TurkeyRiverAtMississippi5m";

//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/TurkeyRiverAtElkader/NED_53652717";
//            int x=4498  , y= 432;
//            String uniqueIdentifier="TurkeyRiverAtElkader";

//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/TurkeyRiverAtElkader/NED_53652717";
//            int x=2950,   y= 1209;
//            String uniqueIdentifier="TurkeyRiverAtElDorado";


//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions";
//            int x=1570,y=127;
//            String uniqueIdentifier="IowaRiverAtWapoello90";
            
//            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011";
//            int x=1570, y= 127;
//            String uniqueIdentifier="ClearCreekAtCoralville";
            
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
            
            java.io.File theFile=new java.io.File(fileName+".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            
            metaModif.setLocationBinaryFile(new java.io.File(fileName+".dir"));
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File(fileName+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);
            
            MetaNetToKML_Links exporter=new MetaNetToKML_Links(metaModif,new java.io.File("/Users/ricardo/temp/"),laCuenca,matDirs,magnitudes,uniqueIdentifier);
            
        } catch (Exception IOE){
            System.out.print(IOE);
            IOE.printStackTrace();
            System.exit(0);
        }
        
        System.exit(0);
        
    }

}

