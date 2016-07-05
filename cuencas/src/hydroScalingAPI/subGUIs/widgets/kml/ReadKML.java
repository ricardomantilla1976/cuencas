/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.subGUIs.widgets.kml;

import de.micromata.opengis.kml.v_2_2_0.Boundary;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Feature;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.Geometry;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.LinearRing;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Point;
import de.micromata.opengis.kml.v_2_2_0.Polygon;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import visad.VisADException;
public class ReadKML {
    private visad.RealTupleType domain;
    private visad.Gridded2DSet[] streams;
    public ReadKML(String FileName) throws VisADException
    {    parseKml(FileName);
    
         // streams=new visad.Gridded2DSet[this.getPolygoList().size()];
    if(this.polygonList.size() > 0)
    {
        streams=new visad.Gridded2DSet[this.getPolygoList().size()];
        domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
         // float[][] line=new float[2][this.getPolygoList().size()];
       
          for(int i = 0 ; i < this.getPolygoList().size() ; i++)
          {
          float[][] line=new float[2][2];
          line[0][0] =  (float) ((Coordinate)getPolygoList().get(i)).getLongitude();
          line[1][0] =   (float) ((Coordinate)getPolygoList().get(i)).getLatitude(); 
          if (i < this.getPolygoList().size()-1){
              line[0][1] =  (float) ((Coordinate)getPolygoList().get(i+1)).getLongitude();
              line[1][1] =  (float) ((Coordinate)getPolygoList().get(i+1)).getLatitude();
          } else {
              line[0][1] =  (float) ((Coordinate)getPolygoList().get(0)).getLongitude();
              line[1][1] =  (float) ((Coordinate)getPolygoList().get(0)).getLatitude();
          }
          
          streams[i]=new visad.Gridded2DSet(domain,line,line[0].length);
          }
    }
        
          
           
    }
    
    public visad.UnionSet getUnionSet() throws visad.VisADException{
        if (streams == null) return null;
        return new visad.UnionSet(domain, streams);
    }
    
    ArrayList<Object> pointList = new ArrayList<Object>();
      ArrayList<Object> polygonList = new ArrayList<Object>();
    public void parseKml(String Filename) {
        System.out.println("Value of File" + Filename );
    InputStream is = getClass().getClassLoader().getResourceAsStream(Filename);
    {
         Kml kml = Kml.unmarshal(new File(Filename));
         System.out.println(kml.toString());
         Feature feature = kml.getFeature();
    System.out.println(feature);
    parseFeature(feature);
    }
   
   
}
    
      private void parseFeature(Feature feature) {
    if(feature != null) {
            System.out.println("Inside PArse Featurwe");
        if(feature instanceof Document) {
             System.out.println("Inside PArse Document");
            Document document = (Document) feature;
            List<Feature> featureList = document.getFeature();
            for(Feature documentFeature : featureList) {
                if(documentFeature instanceof Placemark) {
                     System.out.println("Inside PArse Placemaker");
                    Placemark placemark = (Placemark) documentFeature;
                    Geometry geometry = placemark.getGeometry();
                    parseGeometry(geometry);
                  
                }
                 if(documentFeature instanceof Folder) {
                  
                     System.out.println("Inside PArse Folder");
                    Folder folder = (Folder) documentFeature;
                    List<Feature> folderList = folder.getFeature();
                    for(Feature folderFeature : folderList) {
                     if(folderFeature instanceof Placemark) {
                     System.out.println("Inside PArse Placemaker");
                    Placemark placemark = (Placemark) folderFeature;
                    Geometry geometry = placemark.getGeometry();
                    parseGeometry(geometry);
                  
                }
                    
                }
            }
        }
    }
}
    }
    
    private void parseGeometry(Geometry geometry) {
        System.out.print("Inside ");
    if(geometry != null) {
        if(geometry instanceof Polygon) {
            Polygon polygon = (Polygon) geometry;
            Boundary outerBoundaryIs = polygon.getOuterBoundaryIs();
            if(outerBoundaryIs != null) {
                LinearRing linearRing = outerBoundaryIs.getLinearRing();
                if(linearRing != null) {
                    List<Coordinate> coordinates = linearRing.getCoordinates();
                    if(coordinates != null) {
                        for(Coordinate coordinate : coordinates) {
                            polygonList.add(coordinate);
                           // parseCoordinate(coordinate);
                        }
                    }
                }
            }
        }
          if(geometry instanceof Point) {
                  Point point = (Point) geometry;
                 List<Coordinate> coordinates  =  point.getCoordinates();
                  if(coordinates != null) {
                        for(Coordinate coordinate : coordinates) {
                            pointList.add(coordinate);
                            //parseCoordinate(coordinate);
                        }
          }
    }
}
    }
    
    
    private void parseCoordinate(Coordinate coordinate) {
    if(coordinate != null) {
        System.out.println("Longitude: " +  coordinate.getLongitude());
        System.out.println("Latitude : " +  coordinate.getLatitude());
        System.out.println("Altitude : " +  coordinate.getAltitude());
        System.out.println("");
    }
}
    public ArrayList<Object> getPointList()
    {
            return pointList;
    }
    
     public ArrayList<Object> getPolygoList()
    {
            return polygonList;
    }
    
     
     
}



