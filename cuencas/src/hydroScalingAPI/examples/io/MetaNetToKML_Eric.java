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
public class MetaNetToKML_Eric {
    
    public MetaNetToKML_Eric(hydroScalingAPI.io.MetaRaster metaModif, java.io.File outputDirectory,hydroScalingAPI.util.geomorphology.objects.Basin myCuenca,byte [][] matDir,float[][] gdo,String uniqueIdentifier) throws java.io.IOException{

        String[] cityName=uniqueIdentifier.split(" \\(");
        String[] riverName=cityName[1].split("\\)");
        String[] commentName=null;

        if(riverName.length > 1) commentName=riverName[1].split(":"); else commentName=new String[] {"N/A"," [information N/A]"};

        uniqueIdentifier=cityName[0]+" ("+riverName[0]+")";
        
        float xO=myCuenca.getLonLatBasin()[0][0];
        float yO=myCuenca.getLonLatBasin()[1][0];
        
        hydroScalingAPI.io.MetaRaster metaDatos=metaModif;
        
        hydroScalingAPI.io.MetaNetwork netStructure=new hydroScalingAPI.io.MetaNetwork(metaModif);
        
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        
        java.io.File fileSalida;
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          ret="\n";

        fileSalida=new java.io.File(outputDirectory+"/BasinXY_"+uniqueIdentifier+".txt.gz");

        outputDir = new java.io.FileOutputStream(fileSalida);
        java.util.zip.GZIPOutputStream outputComprim=new java.util.zip.GZIPOutputStream(outputDir);
        bufferout=new java.io.BufferedOutputStream(outputComprim);
        newfile=new java.io.OutputStreamWriter(bufferout);

        float[][] xyBasin=myCuenca.getLonLatBasin();
        int[][] xyBasinIndex=myCuenca.getXYBasin();

        newfile.write(uniqueIdentifier+ret);
        for (int i = 0; i < xyBasin[0].length; i++) {
            newfile.write(xyBasin[0][i]+","+xyBasin[1][i]+","+(gdo[xyBasinIndex[1][i]][xyBasinIndex[0][i]]-gdo[xyBasinIndex[1][0]][xyBasinIndex[0][0]])+ret);
        }

        newfile.close();
        bufferout.close();
        outputComprim.close();
        outputDir.close();
        
        String lon=hydroScalingAPI.tools.DegreesToDMS.getprettyString(xO, 1);
        String lat=hydroScalingAPI.tools.DegreesToDMS.getprettyString(yO, 0);
        double areaUp=Math.round(thisNetworkGeom.upStreamArea(linksStructure.OuletLinkNum)*10)/10.0;
        double lengthUp=Math.round(thisNetworkGeom.mainChannelLength(linksStructure.OuletLinkNum)*100)/100.0;
        double timeUp=Math.round(thisNetworkGeom.mainChannelLength(linksStructure.OuletLinkNum)*1000/0.75/3600.0*100)/100.0;

        double areaUpSqMi=Math.round(thisNetworkGeom.upStreamArea(linksStructure.OuletLinkNum)*0.38610*10)/10.0;
        double lengthUpMi=Math.round(thisNetworkGeom.mainChannelLength(linksStructure.OuletLinkNum)*0.62137*100)/100.0;
        double timeUpDay=Math.round(thisNetworkGeom.mainChannelLength(linksStructure.OuletLinkNum)*1000/0.75/3600.0/24*100)/100.0;


        fileSalida=new java.io.File(outputDirectory+"/InfoFile_"+uniqueIdentifier+".txt");


        outputDir = new java.io.FileOutputStream(fileSalida);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);

        newfile.write(uniqueIdentifier+ret);
        newfile.write("<b>Latitude:</b> "+lat+ret);
        newfile.write("<b>Longitude:</b> "+lon+ret);
        newfile.write("<b>Upstream Area [km<sup>2</sup>]:</b> "+areaUp+ret);
        newfile.write("<b>Main Channel Length [km]:</b> "+lengthUp+ret);
        newfile.write("<b>Typical Response Time [hr]:</b> "+timeUp+ret);

        newfile.close();
        bufferout.close();
        outputDir.close();

        String timeLegend="";

        if(timeUpDay > 1) {
            timeLegend=(int)Math.floor(timeUpDay)+" Day";

            if(Math.floor(timeUpDay) > 1) timeLegend+="s";

            if((int)Math.floor(timeUp-Math.floor(timeUpDay)*24) > 0)  {
                timeLegend+=" and "+(int)Math.floor(timeUp-Math.floor(timeUpDay)*24)+" Hour";
                if(Math.floor(timeUp-Math.floor(timeUpDay)*24) >= 2) timeLegend+="s";
            }
        } else {
            timeLegend=Math.floor(timeUp)+" hours";
        }


        String myDescription =  "<h3>Name: " + uniqueIdentifier + "</h3>"
                                //+ "<b>Baisin Outlet Information</b><br><br>"
                                //+ "<b>Latitude:</b> " + lat +"<br>"
                                //+ "<b>Longitude:</b> " + lon + "<br>"
                                //+ "<b>Upstream Area:</b> " + areaUpSqMi + " [mi<sup>2</sup>] (" + areaUp + " [km<sup>2</sup>])"+ "<br>"
                                //+ "<b>Main Channel Length:</b> "+lengthUpMi+" [miles] ("+lengthUp+" [km])"+ "<br>"
                                //+ "<b>Typical Response Time:</b> "+timeUp+" [hours] ("+timeUpDay+" [days])"+ "<br>" + "<br>"
                                + "<b>Basin Area:</b> " + areaUpSqMi + " Square Miles"+ "<br>"
                                + "<b>Main River Length:</b> "+lengthUpMi+" Miles"+ "<br>"
                                + "<b>Rainfall Residence Time:</b> "+timeLegend+ "<br>" + "<br>"
                                + "<b>Comment:</b>"+ "<br>"
                                + "Monitoring rain over this area which controls potential flooding of "+riverName[0]+" that affects"+commentName[1]+".";
        
        fileSalida=new java.io.File(outputDirectory+"/Divide_"+uniqueIdentifier+".kml");

        hydroScalingAPI.io.MetaPolygon metaPolyToWrite=new hydroScalingAPI.io.MetaPolygon ();
        metaPolyToWrite.setName(myCuenca.toString());
        metaPolyToWrite.setCoordinates(myCuenca.getLonLatBasinDivide());
        metaPolyToWrite.setInformation("Basin Divide as captured by Cuencas");

        metaPolyToWrite.writeKmlPolygon(fileSalida,uniqueIdentifier);

        byte[][] basMask=myCuenca.getBasinMask();

        fileSalida=new java.io.File(outputDirectory+"/RiverNetworkLowRes_"+uniqueIdentifier+".kml");
        
        
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
        
        for(int i=Math.max(linksStructure.basinOrder-3, 1);i<=linksStructure.basinOrder;i++){
            newfile.write("  <Style id=\"linestyleO"+i+"\">"+ret);
            newfile.write("    <LineStyle>"+ret);
            //newfile.write("      <color>7f"+Integer.toHexString(i*20)+"0000</color>"+ret);
            newfile.write("      <color>FFFF0000</color>"+ret);
            newfile.write("      <width>"+Math.max(i-5,1)+"</width>"+ret);
            newfile.write("    </LineStyle>"+ret);
            newfile.write("  </Style>"+ret);
        }
        for(int i=Math.max(linksStructure.basinOrder-3, 1);i<=linksStructure.basinOrder;i++){
            newfile.write("  <Folder>"+ret);
            newfile.write("    <name>Order "+i+" Streams</name>"+ret);
            newfile.write("    <visibility>"+(i>0?1:0)+"</visibility>"+ret);
            newfile.write("    <open>0</open>"+ret);
            netStructure.getLineStringXYs(i,basMask,newfile);
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
        args=new String[] { "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_large_cities.log",
                            "/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res",
                            "/Users/ricardo/rawData/BasinMasks/large_cities/"};

        args=new String[] { "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_usgs_gauges.log",
                            "/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res",
                            "/Users/ricardo/rawData/BasinMasks/usgs_gauges/"};

//        args=new String[] { "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_medium_cities.log",
//                            "/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res",
//                            "/Users/ricardo/rawData/BasinMasks/medium_cities/"};
//
//        args=new String[] { "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_small_cities.log",
//                            "/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res",
//                            "/Users/ricardo/rawData/BasinMasks/small_cities/"};
//
//        args=new String[] { "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_sensors.log",
//                            "/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res",
//                            "/Users/ricardo/rawData/BasinMasks/ifc_sensors/"};

        main1(args);
    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main1(String[] args) {

        try{

            String[] basins=new hydroScalingAPI.io.BasinsLogReader(new java.io.File(args[0])).getPresetBasins();

            String fileName=args[1];

            java.io.File theFile=new java.io.File(fileName+".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(fileName+".dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".gdo"));
            metaModif.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".gdo"));
            float[][] gdo=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();



            for (int i = 0; i < basins.length; i++) {
            //for (int i = 0; i < 1; i++) {

                if(!basins[i].equalsIgnoreCase("")){
                    String[] basLabel = basins[i].split("; ");

                    int x=Integer.parseInt(basLabel[0].split(",")[0].split("x:")[1].trim());
                    int y=Integer.parseInt(basLabel[0].split(",")[1].split("y:")[1].trim());

                    String uniqueIdentifier=basLabel[1];

                    String[] cityName=uniqueIdentifier.split(" \\(");

                    hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

                    java.io.File outputDir=new java.io.File(args[2]+cityName[0]);
                    outputDir.mkdirs();

                    hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(laCuenca, metaModif, matDirs);
                    hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);


                    double areaUp=Math.round(thisNetworkGeom.upStreamArea(linksStructure.OuletLinkNum)*10)/10.0;


                    System.out.println(x+";"+y+";"+areaUp+";"+uniqueIdentifier);


                    MetaNetToKML_Eric exporter=new MetaNetToKML_Eric(metaModif,outputDir,laCuenca,matDirs,gdo,uniqueIdentifier);

                }
            }
        } catch (Exception IOE){
            System.out.print(IOE);
            IOE.printStackTrace();
            System.exit(0);
        }

        System.exit(0);

    }

}

