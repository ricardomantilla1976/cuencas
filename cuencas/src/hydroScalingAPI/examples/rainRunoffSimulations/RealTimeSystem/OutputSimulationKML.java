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
 * OutputSimulationKML.java
 *
 * Created on October 10, 2008, 5:00 PM
 */

package hydroScalingAPI.examples.rainRunoffSimulations.RealTimeSystem;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo Mantilla
 */
public class OutputSimulationKML {
    
    public OutputSimulationKML(hydroScalingAPI.io.MetaRaster metaModif, java.io.File outputDirectory,hydroScalingAPI.util.geomorphology.objects.Basin myCuenca,byte [][] matDir,String uniqueIdentifier, String labelTime) throws java.io.IOException{

        float xO=myCuenca.getLonLatBasin()[0][0];
        float yO=myCuenca.getLonLatBasin()[1][0];
        
        hydroScalingAPI.io.MetaRaster metaDatos=metaModif;
        
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);

        double[] areasArray=new double[linksStructure.contactsArray.length];
        double[] maximumsArray=new double[linksStructure.contactsArray.length];

        java.io.File inicFile=new java.io.File(outputDirectory+"/NED_00159011_0_0-prec."+labelTime+"-IR_0.5-Routing_GK_params_0.2_-0.1_0.28.csv");

        if(inicFile.exists()){
            try {
                java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(inicFile));
                String fullLine="";

                for (int i = 0; i < 5; i++) fullLine = fileMeta.readLine();

                String[] textIC=fullLine.split(",");

                for (int i = 0; i < maximumsArray.length; i++) {
                    areasArray[i]=Double.parseDouble(textIC[i+1]);
                }

                for (int i = 5; i < 13; i++) fullLine = fileMeta.readLine();

                textIC=fullLine.split(",");

                for (int i = 0; i < maximumsArray.length; i++) {
                    maximumsArray[i]=Double.parseDouble(textIC[i+1]);
                    maximumsArray[i]/=(3.68*Math.pow(areasArray[i],0.54));
                    maximumsArray[i]=Math.min(maximumsArray[i], 3);
                }

                fileMeta.close();




            } catch (java.io.IOException ex) {
                Logger.getLogger(RealTimeSystemSimulationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            java.util.Arrays.fill(maximumsArray, 0.0);
        }


        java.io.File fileSalida;
        java.io.FileOutputStream        outputDir;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          ret="\n";
        
        fileSalida=new java.io.File(outputDirectory+"/LinkStatusHighRes_"+uniqueIdentifier+".kml");

        outputDir = new java.io.FileOutputStream(fileSalida);
        bufferout=new java.io.BufferedOutputStream(outputDir);
        newfile=new java.io.OutputStreamWriter(bufferout);

        newfile.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+ret);
        newfile.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        newfile.write("<Document>"+ret);
        newfile.write("  <name>RiverNetwork.kml</name>"+ret);
        newfile.write("  <open>1</open>"+ret);
        newfile.write("  <LookAt>"+ret);
        newfile.write("    <longitude>"+xO+"</longitude>"+ret);
        newfile.write("    <latitude>"+yO+"</latitude>"+ret);
        newfile.write("    <altitude>0</altitude>"+ret);
        newfile.write("    <range>1500</range>"+ret);
        newfile.write("    <tilt>30</tilt>"+ret);
        newfile.write("    <heading>0</heading>"+ret);
        newfile.write("  </LookAt>"+ret);
        newfile.write("  <Style id=\"AlertLevel1\">"+ret);
        newfile.write("    <IconStyle>"+ret);
        newfile.write("      <Icon>"+ret);
        newfile.write("         <href>http://maps.google.com/mapfiles/kml/paddle/blu-stars.png</href>"+ret);
        newfile.write("      </Icon>"+ret);
        newfile.write("    </IconStyle>"+ret);
        newfile.write("  </Style>"+ret);
        newfile.write("  <Style id=\"AlertLevel2\">"+ret);
        newfile.write("    <IconStyle>"+ret);
        newfile.write("      <Icon>"+ret);
        newfile.write("         <href>http://maps.google.com/mapfiles/kml/paddle/ylw-stars.png</href>"+ret);
        newfile.write("      </Icon>"+ret);
        newfile.write("    </IconStyle>"+ret);
        newfile.write("  </Style>"+ret);
        newfile.write("  <Style id=\"AlertLevel3\">"+ret);
        newfile.write("    <IconStyle>"+ret);
        newfile.write("      <Icon>"+ret);
        newfile.write("         <href>http://maps.google.com/mapfiles/kml/paddle/red-stars.png</href>"+ret);
        newfile.write("      </Icon>"+ret);
        newfile.write("    </IconStyle>"+ret);
        newfile.write("  </Style>"+ret);
        newfile.write("  <Folder>"+ret);
        newfile.write("    <open>0</open>"+ret);
        for(int i=0;i<linksStructure.contactsArray.length;i++){

            if(maximumsArray[i] >= 1){
                newfile.write("  <Placemark>"+ret);
                newfile.write("  <name>Link ID: "+i+"</name>"+ret);
                newfile.write("  <styleUrl>#AlertLevel"+(int)maximumsArray[i]+"</styleUrl>"+ret);
                newfile.write("     <Point>"+ret);

                double longitude=(linksStructure.contactsArray[i]%metaModif.getNumCols()+0.5)*metaModif.getResLon()/3600.0D+metaModif.getMinLon();
                double latitude=(linksStructure.contactsArray[i]/metaModif.getNumCols()+0.5)*metaModif.getResLat()/3600.0D+metaModif.getMinLat();

                newfile.write("         <coordinates>"+longitude+","+latitude+"</coordinates>"+ret);
                newfile.write("     </Point>"+ret);
                newfile.write("  </Placemark>"+ret);
            }

        }
        newfile.write("  </Folder>"+ret);

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
            
            String fileName="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011";
            int x=1570, y= 127;
            String uniqueIdentifier="ClearCreekAtCoralville";
            String outputDir="/Users/ricardo/temp/";
            
            java.io.File theFile=new java.io.File(fileName+".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(fileName+".dir"));
        
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);
            
            OutputSimulationKML exporter=new OutputSimulationKML(metaModif,new java.io.File(outputDir),laCuenca,matDirs,uniqueIdentifier,"xxxx");
            
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

                hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

                OutputSimulationKML exporter=new OutputSimulationKML(metaModif,new java.io.File("/Users/ricardo/temp/cities/"),laCuenca,matDirs,uniqueIdentifier,"xxxx");

            }
        } catch (Exception IOE){
            System.out.print(IOE);
            IOE.printStackTrace();
            System.exit(0);
        }

        System.exit(0);

    }

}

