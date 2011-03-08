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
 * MetaPolygon.java
 *
 * Created on October 10, 2003, 3:08 PM
 */

package hydroScalingAPI.io;

import java.io.IOException;
import visad.VisADException;

/**
 * This class interacts with *.poly files.  It reads their contents and produce
 * appropriate outputs for graphic and other purposes.
 * @author Ricardo Mantilla
 */
public class MetaPolygon {
    
    private java.io.BufferedReader fileMeta;

    private java.io.File locationMeta;
    private java.io.File locationBinaryFile;
    private boolean completed;
    
    private java.util.Hashtable properties;

    private String[] parameters= { 
                                    "[name]",
                                    "[coordinates lon/lat (deg)]",
                                    "[information]"
                                    
                                };
    /**
     * Creates a new instance of MetaPolygon with an empty set of properties
     */
    public MetaPolygon() {
        
        properties=new java.util.Hashtable();
        
    }
    
    /**
     *  Creates a new instance of MetaPolygon with a set of properties determined by a
     * file
     * @param file A String with the path to the file that contains the properties of the Polygon
     * @throws java.io.IOException Errors while reading the *.poly file
     */
    public MetaPolygon(String file) throws java.io.IOException{
        this(new java.io.File(file));
    }
    
    /**
     *  Creates a new instance of MetaPolygon with a set of properties determined by a
     * file
     * @param file The file that contains the properties of the Polygon
     * @throws java.io.IOException Errors while reading the *.poly file
     */
    public MetaPolygon(java.io.File file) throws java.io.IOException{

        locationMeta=file;

        checkParameters(file);
        
        properties=new java.util.Hashtable();
        
        fileMeta = new java.io.BufferedReader(new java.io.FileReader(file));

        String fullLine;
        int hemisphereFactor;

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[0]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[1]));
        String coordLabel=fullLine;
        java.util.Vector xyCoords=new java.util.Vector();
        while ((fullLine=fileMeta.readLine()) != null && fullLine.length() > 0){
            java.util.StringTokenizer xyCouple=new java.util.StringTokenizer(fullLine);;
            xyCoords.add(new float[] {Float.parseFloat(xyCouple.nextToken()),Float.parseFloat(xyCouple.nextToken())});
        }
        float[][] xyContour=new float[2][xyCoords.size()];
        for (int i=0;i<xyContour[0].length;i++){
            float[] xyPair=(float[])xyCoords.get(i);
            xyContour[0][i]=xyPair[0];
            xyContour[1][i]=xyPair[1];
        }
        properties.put(coordLabel,xyContour);

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[2]));
        properties.put(fullLine,fileMeta.readLine());

        completed=true;
        fileMeta.close();
    }

    private boolean checkParameters(java.io.File file) throws java.io.IOException{
        
        fileMeta = new java.io.BufferedReader(new java.io.FileReader(file));

        String fullLine;
        int i=0;

        do{
            fullLine=fileMeta.readLine();
            if (fullLine.equalsIgnoreCase(parameters[i])) i++;
        } while (i<parameters.length && fullLine != null);

        fileMeta.close();

        if (i == parameters.length) return true;
        else return false;

    }
    
    /**
     * Returns the value of the property.  Available properties are [name] and [information].
     * @param prop The desired property
     * @return Returns the value of the property
     */
    public String getProperty(String prop){
        return (String) properties.get(prop);
    }
    
    /**
     * Creates and returns a visad.Gridded2DSet ready to be added into a visad.Display
     * @return The Gridded2DSet
     * @throws visad.VisADException Captures errors while creating the Gidded2DSet
     */
    public visad.Gridded2DSet getPolygon() throws visad.VisADException {
        
        float[][] xyContour=(float[][])properties.get("[coordinates lon/lat (deg)]");
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        visad.Gridded2DSet polygonCountour=new visad.Gridded2DSet(domain,xyContour,xyContour[0].length);
        
        return polygonCountour;
    }
    
    /**
     * Returns a float array with the longitudes and latitudes of the points that
     * describe the polygon
     * @return A float[2][Number of Points]
     */
    public float[][] getLonLatPolygon(){
        return (float[][])properties.get("[coordinates lon/lat (deg)]");
    }
    
    /**
     * Returns the Name associated with the polygon
     * @return The Name of the polygon
     */
    public String getName(){
        return (String) properties.get("[name]");
    }
    
    /**
     * Returns the File with information used to create the MetaPolygon
     * @return A File descriptor of the origin data
     */
    public java.io.File getLocationMeta(){
        return locationMeta;
    }
    
    /**
     * Returns a string describing this MetaPolygon
     * @return A string describing the MetaPolygon
     */
    public String toString(){
        return getName()+" - "+getLocationMeta().getName();
    }
    
    /**
     * Sets the [name] property to the specified value
     * @param newName The value to assign to the [name] field
     */
    public void setName(String newName){
        properties.put("[name]",newName);
    }
    
    /**
     * Sets the coordinates of the polygon to using a float array
     * @param newXYCoords The array with coordinates
     */
    public void setCoordinates(float[][] newXYCoords){
        properties.put("[coordinates lon/lat (deg)]",newXYCoords);
    }
    
    /**
     * Sets the [information] property to the specified value
     * @param newInformation The value to assign to the [information] field
     */
    public void setInformation(String newInformation){
        properties.put("[information]",newInformation);
    }
    
    /**
     * Calculates the average value of a spatially variable field inside the polygon
     * @param localField The visad.FlatField to be integrated
     */
    public double getAverage(visad.FlatField localField){
        try {
            
            visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
            
            float[][] LonLatCoords=getLonLatPolygon();
            
            int[][] XYCoords=new int[2][LonLatCoords[0].length];
            for (int i = 0; i < LonLatCoords[0].length; i++) {
                XYCoords[0][i]=(int)(LonLatCoords[0][i]*1e7);
                XYCoords[1][i]=(int)(LonLatCoords[1][i]*1e7);
            }
            
            java.awt.Polygon intPoly=new java.awt.Polygon(XYCoords[0],XYCoords[1],XYCoords[0].length);
            
            hydroScalingAPI.util.statistics.Stats xStats=new hydroScalingAPI.util.statistics.Stats(LonLatCoords[0]);
            hydroScalingAPI.util.statistics.Stats yStats=new hydroScalingAPI.util.statistics.Stats(LonLatCoords[1]);
            
            java.util.Vector coordVector=new java.util.Vector();
            float[][] coorField=localField.getDomainSet().getSamples();
            for (int i = 0; i < coorField[0].length; i++) {
                if(intPoly.contains((int)(coorField[0][i]*1e7),(int)(coorField[1][i]*1e7)))
                    coordVector.add(new float[]{coorField[0][i],coorField[1][i]});
            }
            double accum=0;
            int nPoints=coordVector.size();
            for (int i = 0; i < nPoints; i++) {
                float[] pos=(float[])coordVector.get(i);
                visad.RealTuple spotValue=(visad.RealTuple) localField.evaluate(new visad.RealTuple(domain, new double[] {pos[0],pos[1]}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                accum+=spotValue.getValues()[0];
            }
            for (int i = 0; i < LonLatCoords[0].length; i++) {
                visad.RealTuple spotValue=(visad.RealTuple) localField.evaluate(new visad.RealTuple(domain, new double[] {LonLatCoords[0][i],LonLatCoords[1][i]}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                accum+=spotValue.getValues()[0];
            }
            
            accum/=(double)(nPoints+LonLatCoords[0].length);

            return accum;
            
            
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
        
        return -9999;
        
    }
    
    /**
     * Writes an *.poly file in the specified path
     * @param newMetaLocation The File descriptor where the information will be written.
     * @throws java.io.IOException Captures errors while writing the file
     */
    public void writePolygon(java.io.File newMetaLocation) throws java.io.IOException{
        String testName=newMetaLocation.getName();
        if(!testName.contains(".poly")) newMetaLocation=new java.io.File(newMetaLocation.getPath()+".poly");
        
        java.io.BufferedWriter writerMeta = new java.io.BufferedWriter(new java.io.FileWriter(newMetaLocation));
        
        writerMeta.write(parameters[0]+"\n");
        writerMeta.write((String)properties.get(parameters[0])+"\n");
        writerMeta.write("\n");
        
        writerMeta.write(parameters[1]+"\n");
        float[][] xyContour=(float[][])properties.get("[coordinates lon/lat (deg)]");
        for (int i=0;i<xyContour[0].length;i++){
            writerMeta.write(xyContour[0][i]+"\t"+xyContour[1][i]+"\n");
        }
        writerMeta.write("\n");

        writerMeta.write(parameters[2]+"\n");
        writerMeta.write((String)properties.get(parameters[2])+"\n");
        writerMeta.write("\n");
        
        writerMeta.close();
        
    }

    /**
     * Writes an *.kml file in the specified path
     * @param newKmlLocation The File descriptor where the information will be written.
     * @throws java.io.IOException Captures errors while writing the file
     */
    public void writeKmlPolygon(java.io.File newKmlLocation,String label) throws java.io.IOException{

        String testName=newKmlLocation.getName();
        if(!testName.contains(".kml")) newKmlLocation=new java.io.File(newKmlLocation.getPath()+".kml");

        float[][] xyContour=(float[][])properties.get("[coordinates lon/lat (deg)]");

        java.io.BufferedWriter writerKml = new java.io.BufferedWriter(new java.io.FileWriter(newKmlLocation));

        String ret="\n";



        writerKml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + ret);
        writerKml.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
        writerKml.write("<Document>" + ret);
        writerKml.write("<Folder>" + ret);
        writerKml.write("   <Placemark>" + ret);
        writerKml.write("      <styleUrl>http://ut.iihr.uiowa.edu/ifis/kml/ifc_style3.kml#polygon</styleUrl>" + ret);
        writerKml.write("      <Polygon>" + ret);
        writerKml.write("          <name>" + label + "</name>" + ret);
        writerKml.write("          <outerBoundaryIs>" + ret);
        writerKml.write("          <LinearRing>" + ret);
        writerKml.write("             <coordinates>" + ret);
        writerKml.write("             ");
        for (int i = 0; i < xyContour[0].length; i++) {
            writerKml.write(Math.round(xyContour[0][i]*1e5)/1e5 + "," + Math.round(xyContour[1][i]*1e5)/1e5 + " ");
        }
        writerKml.write(ret);
        writerKml.write("              </coordinates>" + ret);
        writerKml.write("           </LinearRing>" + ret);
        writerKml.write("        </outerBoundaryIs>" + ret);
        writerKml.write("     </Polygon>" + ret);
        writerKml.write("   </Placemark>" + ret);
        writerKml.write("</Folder>" + ret);
        writerKml.write("</Document>" + ret);
        writerKml.write("</kml>" + ret);

        writerKml.close();

    }
    /**
     * Writes an *.kml file in the specified path
     * @param newKmlLocation The File descriptor where the information will be written.
     * @throws java.io.IOException Captures errors while writing the file
     */
    public void writeKmlPolygon(java.io.File newKmlLocation,String label, String myDescription) throws java.io.IOException{
        
        String testName=newKmlLocation.getName();
        if(!testName.contains(".kml")) newKmlLocation=new java.io.File(newKmlLocation.getPath()+".kml");
        
        float[][] xyContour=(float[][])properties.get("[coordinates lon/lat (deg)]");
        
        java.io.BufferedWriter writerKml = new java.io.BufferedWriter(new java.io.FileWriter(newKmlLocation));
        
        String ret="\n";


        
        writerKml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + ret);
        writerKml.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\" xmlns:kml=\"http://www.opengis.net/kml/2.2\" xmlns:atom=\"http://www.w3.org/2005/Atom\">");
        writerKml.write("<Document>" + ret);
        writerKml.write("<Style id=\"Polygon\">" + ret);
        writerKml.write("<LabelStyle><color>00000000</color>" + ret);
        writerKml.write("<scale>0</scale></LabelStyle>" + ret);
        writerKml.write("<LineStyle><color>ff6e6e6e</color>" + ret);
        writerKml.write("<width>0.4</width></LineStyle>" + ret);
        writerKml.write("<PolyStyle><color>e6ffffff</color></PolyStyle>" + ret);
        writerKml.write("</Style>" + ret);
        writerKml.write("<Style id=\"Point\">" + ret);
        writerKml.write("<IconStyle>" + ret);
        writerKml.write("<scale>0.5</scale>" + ret);
        writerKml.write("<Icon>" + ret);
        writerKml.write("<href>http://weather.iihr.uiowa.edu/ifc/graphics/dots32/real00dot.png</href>" + ret);
        writerKml.write("</Icon>" + ret);
        writerKml.write("</IconStyle>" + ret);
        writerKml.write("</Style>" + ret);
        writerKml.write("<Folder>" + ret);
        if(myDescription.indexOf("<br><br><b>Comment") != -1)
            writerKml.write("   <Placemark><description><![CDATA[" + myDescription.substring(0, myDescription.indexOf("<br><br><b>Comment")) + "]]></description>" + ret + "<styleUrl>#Polygon</styleUrl>" + ret);
        else
            writerKml.write("   <Placemark><description><![CDATA[" + myDescription+ "]]></description>" + ret + "<styleUrl>#Polygon</styleUrl>" + ret);
        writerKml.write("      <Polygon>");
        writerKml.write("          <name>" + label + "</name>" + ret);
        writerKml.write("          <outerBoundaryIs>" + ret);
        writerKml.write("          <LinearRing>" + ret);
        writerKml.write("             <coordinates>" + ret);
        writerKml.write("             ");
        for (int i = 0; i < xyContour[0].length; i++) {
            writerKml.write(xyContour[0][i] + "," + xyContour[1][i] + " ");
        }
        writerKml.write(ret);
        writerKml.write("              </coordinates>" + ret);
        writerKml.write("           </LinearRing>" + ret);
        writerKml.write("        </outerBoundaryIs>" + ret);
        writerKml.write("     </Polygon>" + ret);
        writerKml.write("   </Placemark>" + ret);
        writerKml.write("   <Placemark><description><![CDATA[" + myDescription + "]]></description><styleUrl>#Point</styleUrl>" + ret);
        writerKml.write("      <Point>" + ret);
        writerKml.write("          <coordinates>"+xyContour[0][0] + "," + xyContour[1][0] + " "+"</coordinates>" + ret);
        writerKml.write("      </Point>" + ret);
        writerKml.write("   </Placemark>" + ret);
        writerKml.write("</Folder>" + ret);
        writerKml.write("</Document>" + ret);
        writerKml.write("</kml>" + ret);

        writerKml.close();



//        writerKml.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>"+ret);
//        writerKml.write("<kml xmlns=\"http://www.opengis.net/kml/2.2\">");
//        writerKml.write("<Document>"+ret);
////        writerKml.write("  <name>"+label+"</name>"+ret);
////        writerKml.write("  <open>0</open>"+ret);
//        writerKml.write("  <LookAt>"+ret);
//        writerKml.write("    <longitude>"+xyContour[0][0]+"</longitude>"+ret);
//        writerKml.write("    <latitude>"+xyContour[1][0]+"</latitude>"+ret);
//        writerKml.write("    <altitude>0</altitude>"+ret);
//        writerKml.write("    <range>1500</range>"+ret);
//        writerKml.write("    <tilt>30</tilt>"+ret);
//        writerKml.write("    <heading>0</heading>"+ret);
//        writerKml.write("  </LookAt>"+ret);
//        writerKml.write("  <Style id=\"polyStyleDivide\">"+ret);
//        writerKml.write("    <PolyStyle>"+ret);
//        writerKml.write("      <color>7f969696</color>"+ret);
//        writerKml.write("      <width>1</width>"+ret);
//        writerKml.write("      <fill>1</fill>"+ret);
//        writerKml.write("      <outline>1</outline>"+ret);
//        writerKml.write("    </PolyStyle>"+ret);
//        writerKml.write("    <LineStyle>"+ret);
//        writerKml.write("      <color>7f969696</color>"+ret);
//        writerKml.write("      <width>3</width>"+ret);
//        writerKml.write("    </LineStyle>"+ret);
//        writerKml.write("  </Style>"+ret);
//        writerKml.write("  <Placemark>"+ret);
//        //writerKml.write("  <name>"+(String)properties.get(parameters[0])+"</name>"+ret);
//        writerKml.write("  <name>"+label+"</name>"+ret);
//        writerKml.write("  <description>"+myDescription+"</description>"+ret);
//        writerKml.write("  <styleUrl>#polyStyleDivide</styleUrl>"+ret);
//        writerKml.write("  <Polygon>"+ret);
//        writerKml.write("      <outerBoundaryIs>"+ret);
//        writerKml.write("      <LinearRing>"+ret);
//        writerKml.write("        <coordinates>"+ret);
//        writerKml.write("           ");
//        for (int i=0;i<xyContour[0].length;i++){
//            writerKml.write(xyContour[0][i]+","+xyContour[1][i]+" ");
//        }
//        writerKml.write(ret);
//        writerKml.write("        </coordinates>"+ret);
//        writerKml.write("      </LinearRing>"+ret);
//        writerKml.write("    </outerBoundaryIs>"+ret);
//        writerKml.write("  </Polygon>"+ret);
//        writerKml.write("</Placemark>"+ret);
//        writerKml.write("</Document>"+ret);
//        writerKml.write("</kml>"+ret);
//
//        writerKml.close();
        
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            hydroScalingAPI.io.MetaPolygon metaPoly1=new hydroScalingAPI.io.MetaPolygon (new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Polygons/walnutGulch_Divide.poly"));
            System.out.println(metaPoly1.getProperty("[name]"));
            float[][] xyContour=metaPoly1.getLonLatPolygon();
            
            hydroScalingAPI.io.MetaRaster metaRaster1=new hydroScalingAPI.io.MetaRaster (new java.io.File("/hidrosigDataBases/MeltonBasins_DB/Rasters/Hydrology/I1/t1.metaVHC"));
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/MeltonBasins_DB/Rasters/Hydrology/I1/t1.vhc"));
            try {
                System.out.println(metaPoly1.getAverage(metaRaster1.getField()));
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
            
            
            javax.swing.JFrame frame=new javax.swing.JFrame();
            hydroScalingAPI.util.plot.XYJPanel panel=new hydroScalingAPI.util.plot.XYJPanel("Polygon","longitude","latitude");
            panel.addDatos(xyContour[0],xyContour[1],-9999,java.awt.Color.BLACK,0);
            frame.add(panel);
            frame.setBounds(0,0,500,500);
            frame.setVisible(true);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
    }
    
}
