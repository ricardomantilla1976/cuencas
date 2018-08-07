/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.io;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import visad.VisADException;

/**
 *
 * @author ricardo
 */
public class IowaUSGStoKML {

    public IowaUSGStoKML() throws java.io.IOException, visad.VisADException{
        
        String[][] cases={      {"3316","116","05465500","Iowa River at Wapello, IA"},
                                {"2646","762","05454220","Clear Creek near Oxford, IA"},
                                {"2817","713","05454300","Clear Creek near Coralville, IA"},
                                {"2676","465","05455500","English River at Kalona, IA"},
                                {"1770","1987","05458300","Cedar River at Waverly, IA"},
                                {"2900","768","05453520","Iowa River below Coralville Dam nr Coralville, IA"},
                                {"1765","981","05451900","Richland Creek near Haven, IA"},
                                {"1245","1181","05451500","Iowa River at Marshalltown, IA"},
                                {"951","1479","05451210","South Fork Iowa River NE of New Providence, IA"},
                                {"3113","705","05464942","Hoover Cr at Hoover Nat Hist Site, West Branch, IA"},
                                {"1978","1403","05464220","Wolf Creek near Dysart, IA"},
                                {"1779","1591","05463500","Black Hawk Creek at Hudson, IA"},
                                {"1932","1695","05464000","Cedar River at Waterloo, IA"},
                                {"1590","1789","05463000","Beaver Creek at New Hartford, IA"},
                                {"1682","1858","05458900","West Fork Cedar River at Finchford, IA"},
                                {"1634","1956","05462000","Shell Rock River at Shell Rock, IA"},
                                {"1775","1879","05458500","Cedar River at Janesville, IA"},
                                {"903","2499","05459500","Winnebago River at Mason City, IA"},
                                {"1526","2376","05457700","Cedar River at Charles City, IA"},
                                {"1730","2341","05458000","Little Cedar River near Ionia, IA"},
                                {"2115","801","05453000","Big Bear Creek at Ladora, IA"},
                                {"2256","876","05453100","Iowa River at Marengo, IA"},
                                {"2949","741","05454000","Rapid Creek near Iowa City, IA"},
                                {"1312","1112","05451700","Timber Creek near Marshalltown, IA"},
                                {"2858","742","05454090","Muddy Creek at Coralville, IA"},
                                {"1871","903","05452200","Walnut Creek near Hartwick, IA"},
                                {"2885","690","05454500","Iowa River at Iowa City, IA"},
                                {"2796","629","05455100","Old Mans Creek near Iowa City, IA"},
                                {"2958","410","05455700","Iowa River near Lone Tree, IA"},
                                {"3186","392","05465000","Cedar River near Conesville, IA"},
                                {"2734","1069","05464500","Cedar River at Cedar Rapids, IA"},
                                {"1164","3066","05457000","Cedar River near Austin, MN"}};
        
        hydroScalingAPI.mainGUI.objects.GaugesManager GM=new hydroScalingAPI.mainGUI.objects.GaugesManager(new hydroScalingAPI.mainGUI.objects.GUI_InfoManager(),new hydroScalingAPI.util.database.DataBaseEngine());
        while(!GM.isLoaded()){
            new visad.util.Delay(5000);
            System.out.print("/");
        }
        System.out.println("/");
        java.util.Vector theVector=GM.getCodes();
        for (int i=0;i<theVector.size();i++) System.out.println(theVector.get(i));
        
        java.io.File newKmlLocation=new java.io.File("/home/ricardo/temp/Gauges.kml");
        
        java.io.BufferedWriter writerKml = new java.io.BufferedWriter(new java.io.FileWriter(newKmlLocation));
        
        String ret="\n";
        
        hydroScalingAPI.io.MetaGauge theGauge=GM.getGauge(cases[0][2],"Streamflow_H");
        double[] coords=theGauge.getPositionTuple().getValues();
        
        writerKml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+ret);
        writerKml.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
        writerKml.write("<Document>"+ret);
        writerKml.write("  <name>USGS Gauges</name>"+ret);
        writerKml.write("  <open>1</open>"+ret);
        writerKml.write("  <StyleMap id=\"msn_placemark_square\">"+ret);
        writerKml.write("          <Pair>"+ret);
        writerKml.write("                  <key>normal</key>"+ret);
        writerKml.write("                  <styleUrl>#sn_placemark_square</styleUrl>"+ret);
        writerKml.write("          </Pair>"+ret);
        writerKml.write("          <Pair>"+ret);
        writerKml.write("                  <key>highlight</key>"+ret);
        writerKml.write("                  <styleUrl>#sh_placemark_square_highlight</styleUrl>"+ret);
        writerKml.write("          </Pair>"+ret);
        writerKml.write("  </StyleMap>"+ret);
        writerKml.write("  <Style id=\"sh_placemark_square_highlight\">"+ret);
        writerKml.write("          <IconStyle>"+ret);
        writerKml.write("                  <color>ffff0c00</color>"+ret);
        writerKml.write("                  <scale>0.7</scale>"+ret);
        writerKml.write("                  <Icon>"+ret);
        writerKml.write("                          <href>http://maps.google.com/mapfiles/kml/shapes/placemark_square_highlight.png</href>"+ret);
        writerKml.write("                  </Icon>"+ret);
        writerKml.write("          </IconStyle>"+ret);
        writerKml.write("          <ListStyle>"+ret);
        writerKml.write("          </ListStyle>"+ret);
        writerKml.write("  </Style>"+ret);
        writerKml.write("  <Style id=\"sn_placemark_square\">"+ret);
        writerKml.write("          <IconStyle>"+ret);
        writerKml.write("                  <color>ffff0c00</color>"+ret);
        writerKml.write("                  <scale>0.7</scale>"+ret);
        writerKml.write("                  <Icon>"+ret);
        writerKml.write("                          <href>http://maps.google.com/mapfiles/kml/shapes/placemark_square.png</href>"+ret);
        writerKml.write("                  </Icon>"+ret);
        writerKml.write("          </IconStyle>"+ret);
        writerKml.write("          <ListStyle>"+ret);
        writerKml.write("          </ListStyle>"+ret);
        writerKml.write("  </Style>"+ret);
        writerKml.write("  <LookAt>"+ret);
        writerKml.write("    <longitude>"+coords[0]+"</longitude>"+ret);
        writerKml.write("    <latitude>"+coords[1]+"</latitude>"+ret);
        writerKml.write("    <altitude>0</altitude>"+ret);
        writerKml.write("    <range>1500</range>"+ret);
        writerKml.write("    <tilt>30</tilt>"+ret);
        writerKml.write("    <heading>0</heading>"+ret);
        writerKml.write("  </LookAt>"+ret);
        writerKml.write("  <Folder>"+ret);
        writerKml.write("    <name>Streamflow</name>"+ret);
        writerKml.write("    <visibility>1</visibility>"+ret);
        writerKml.write("    <open>0</open>"+ret);
        for(int i=0;i<cases.length;i++){
            theGauge=GM.getGauge(cases[i][2],"Streamflow_H");
            coords=theGauge.getPositionTuple().getValues();
            writerKml.write("  <Placemark>"+ret);
            writerKml.write("  <name>"+theGauge.getProperty("[code]")+"</name>"+ret);
            writerKml.write("  <styleUrl>#msn_placemark_square</styleUrl>"+ret);
            writerKml.write("  <description>"+ret);
            writerKml.write("   <![CDATA["+ret);
            writerKml.write("   "+theGauge.toString()+"<br />"+ret);
            writerKml.write("   "+"Basin Area: "+theGauge.getProperty("[drainage area (km^2)]")+"km^2 <br />"+ret);
            writerKml.write("   "+"<img src=\"http://cires.colorado.edu/~ricardo/temp/figures/"+theGauge.getProperty("[code]")+".csv.jpg\">"+ret);
            writerKml.write("   <br />"+ret);
            writerKml.write("   "+"<img src=\"http://cires.colorado.edu/~ricardo/temp/figures/"+theGauge.getProperty("[code]")+".csv.rain.jpg\">"+ret);
            writerKml.write("   ]]>"+ret);
            writerKml.write("  </description>"+ret);
            writerKml.write("  <Point>"+ret);
            writerKml.write("        <coordinates>"+ret);
            writerKml.write("           "+coords[0]+","+coords[1]+ret);
            writerKml.write("        </coordinates>"+ret);
            writerKml.write("  </Point>"+ret);
            writerKml.write("  </Placemark>"+ret);
        }
        writerKml.write("  </Folder>"+ret);
        writerKml.write("</Document>"+ret);
        writerKml.write("</kml>"+ret);
        
        writerKml.close();
        
//        java.io.File theFile=new java.io.File("/hidrosigDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
//        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//        metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
//
//        String formatoOriginal=metaModif.getFormat();
//        metaModif.setFormat("Byte");
//        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
//
//        
//        for(int i=0;i<cases.length;i++){
//            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(Integer.parseInt(cases[i][0]),Integer.parseInt(cases[i][1]),matDirs,metaModif);
//            MetaNetToKML exporter=new MetaNetToKML(metaModif,new java.io.File("/home/ricardo/temp/"),laCuenca,matDirs,cases[i][2]);
//        }
        
            
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            new IowaUSGStoKML();
        } catch (IOException ex) {
            Logger.getLogger(IowaUSGStoKML.class.getName()).log(Level.SEVERE, null, ex);
        } catch (VisADException ex) {
            Logger.getLogger(IowaUSGStoKML.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
