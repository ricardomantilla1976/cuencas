/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.swmmCoupling.objects;

import hydroScalingAPI.io.MetaPolygon;
import hydroScalingAPI.io.MetaRaster;
import java.io.File;
import java.io.FileInputStream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

/**
 * Contains methods to be included in MetaPolygon object
 * @author A. D. L. Zanchetta
 */
public abstract class MetaPolygonTools {
    
    public static boolean readKmlFile(MetaPolygon metaPolygon_arg,
                                      File kmlFile_arg, String poligonId_arg, 
                                      MetaRaster metaRaster_arg){
        
        
        // 1 - open file
        // 2 - 
        
        // basic check
        if (kmlFile_arg == null){
            System.err.println("KML file null.");
            return false;
        } else if (!kmlFile_arg.exists()){
            System.err.println("Arquivo nem existe.");
            return false;
        } else if (!kmlFile_arg.canRead()){
            System.err.println("Arquivo num pode ser lido.");
            return false;
        }
        
        int count;
        XMLInputFactory xmlInFact;
        XMLStreamReader reader;
        FileInputStream inStream;
        boolean placemarkFound, polygonFound, nameFound, coordnatesFound;
        String currentTag;
        String coordinatesContent;
        String[] coordinatesTxt;
        Double[][] coordinates;
        float[][] xyAllPointsCoordinates;
        
        xmlInFact = XMLInputFactory.newInstance();
        coordinatesContent = null;
        
        try{
            inStream = new FileInputStream(kmlFile_arg);
            reader = xmlInFact.createXMLStreamReader(inStream);
            
            placemarkFound = false;
            polygonFound = false;
            nameFound = false;
            coordnatesFound = false;
            
            while(reader.hasNext()) {
                reader.next();
                
                // fiding opening tags
                if(reader.getEventType() == XMLStreamReader.START_ELEMENT){
                    currentTag = reader.getLocalName();
                    
                    // check before "coordinates" tag
                    if (currentTag.trim().equalsIgnoreCase("Placemark")){
                        placemarkFound = true;
                        polygonFound = false;
                        nameFound = false;
                        coordnatesFound = false;
                    } else if (currentTag.trim().equalsIgnoreCase("Polygon")){
                        polygonFound = true;
                        coordnatesFound = false;
                    } else if (currentTag.trim().equalsIgnoreCase("coordinates")){
                        coordnatesFound = true;
                    } else if (currentTag.trim().equalsIgnoreCase("name")){
                        nameFound = true;
                    }
                }
                
                if (reader.getEventType() == XMLStreamReader.END_ELEMENT){
                    if (coordnatesFound == true){
                        coordnatesFound = false;
                    }
                }
                
                // reading coordinates
                if(placemarkFound && polygonFound && coordnatesFound){
                    if(reader.getEventType() == XMLStreamReader.CHARACTERS){
                        coordinatesContent = reader.getText().trim();
                    } else {
                        System.out.println("Found all but XML item now is type " + reader.getEventType());
                    }
                }
                
                // 
                if(coordinatesContent != null){
                    break;
                }
                
                reader.close();
                inStream.close();
            }
        } catch (Exception ex){
            System.err.println("Exception: " + ex.getMessage());
        }
        
        // basic check - must have found
        if (coordinatesContent == null){
            System.out.println("Coordinates not found!");
            return (false);
        }
        
        // parsing coords
        coordinatesTxt = coordinatesContent.split(",0 ");
        coordinates = new Double[coordinatesTxt.length][2];
        
        // getting pairs separated
        for(count = 0; count < coordinatesTxt.length; count++){
            String[] latLong;
            
            latLong = coordinatesTxt[count].split(",");
            
            coordinates[count][0] = Double.parseDouble(latLong[0]);
            coordinates[count][1] = Double.parseDouble(latLong[1]);
        }
        
        metaPolygon_arg.setName(poligonId_arg);
        
        // convert LatLong datas to XY values
        /*
        xyAllPointsCoordinates = new float[2][coordinates.length];
        for(count = 0; count < coordinates.length; count++){
            int curX, curY;
            
            curX = 0;
            curY = 0;
            
            curY = MetaPolygon.getYfromLatitude(coordinates[count][1], 
                                                                metaRaster_arg);
            curX = MetaPolygon.getXfromLongitude(coordinates[count][0], 
                                                                metaRaster_arg);
            
            // TODO - remove check
            System.out.println("Lat " + coordinates[count][1] + " become " + curY);
            System.out.println("Lng " + coordinates[count][0] + " become " + curX);
            
            xyAllPointsCoordinates[0][count] = curX;
            xyAllPointsCoordinates[1][count] = curY;
        }
        
        this.setCoordinates(xyAllPointsCoordinates);
        */
        
        // do not convert to XY
        xyAllPointsCoordinates = new float[2][coordinates.length];
        for(count = 0; count < coordinates.length; count++){
            xyAllPointsCoordinates[0][count] = coordinates[count][0].floatValue();
            xyAllPointsCoordinates[1][count] = coordinates[count][1].floatValue();
        }
        metaPolygon_arg.setCoordinates(xyAllPointsCoordinates);
        
        // TODO - remove
        //System.out.println("Coordinates:");
        //for (int countR = 0; countR < xyAllPointsCoordinates.length; countR++){
        //    System.out.println(" x: " + xyAllPointsCoordinates[countR][0] + "; y:" + xyAllPointsCoordinates[countR][1]);
        //}
        
        return (true);
        
    } // read kml method
    
}
