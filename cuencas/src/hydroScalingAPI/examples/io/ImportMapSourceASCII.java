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


package hydroScalingAPI.examples.io;

/**
 * This class was created to import GPS locations from a *.cvs file created using
 * MapSource.  It also associates images.
 * @author Ricardo Mantilla
 */
public class ImportMapSourceASCII {
    
    /** Creates a new instance of importMapSourceASCII */
    public ImportMapSourceASCII(java.io.File sourceFile, java.io.File imagesDirectory) {
        
        try{
            java.io.BufferedReader bufferRemoto = new java.io.BufferedReader(new java.io.FileReader(sourceFile));
            
            for(int j=0;j<5;j++) bufferRemoto.readLine();
            
            String locationInfo=bufferRemoto.readLine();
            int i=1;
            //while (i<2) {
            while (locationInfo != null){
                
                GpsLocationProperties thisLocation=new GpsLocationProperties("KS");
                java.util.StringTokenizer tokens=new java.util.StringTokenizer(locationInfo,"\t");
                
                thisLocation.State=hydroScalingAPI.tools.StateName.CodeOrNameToStandardName("KS");
                thisLocation.Type="Width Measurement";
                tokens.nextToken();
                thisLocation.Name=tokens.nextToken();
                thisLocation.Name=Integer.toString(209+i);
                thisLocation.Images=new String[] {"N/A"};//findImages(imagesDirectory,thisLocation.Name);
                tokens.nextToken();
                thisLocation.Source="Locations where Hydraulic Geometry was surveyed";
                String position=tokens.nextToken();
                System.out.println(position);
                thisLocation.latitude=position.substring(1,9);
                if (thisLocation.latitude.equalsIgnoreCase("UNKNOWN")) {
                    thisLocation.latitude="N/A";
                } else {
                    thisLocation.latitude=hydroScalingAPI.tools.DegreesToDMS.getprettyString(new Double(position.substring(1,7)).doubleValue(),0);
                }
                thisLocation.longitude=position.substring(11);
                if (thisLocation.longitude.equalsIgnoreCase("UNKNOWN")) {
                    thisLocation.longitude="N/A";
                } else {
                    thisLocation.longitude=hydroScalingAPI.tools.DegreesToDMS.getprettyString(-(new Double(position.substring(11)).doubleValue()),1);
                }
                String elevation=tokens.nextToken();
                try{
                    double elevM=new Double(elevation.substring(0,4)).doubleValue()*0.3048;
                    thisLocation.Elevation=""+elevM;
                }catch(java.lang.NumberFormatException NE){
                    thisLocation.Elevation="N/A";
                }
                
                thisLocation.County="N/A";

                new java.io.File("/hidrosigDataBases/Whitewater_database/Sites/Locations/"+thisLocation.State+"/"+thisLocation.Type+"/").mkdirs();
                java.io.File theFile = new java.io.File("/hidrosigDataBases/Whitewater_database/Sites/Locations/"+thisLocation.State+"/"+thisLocation.Type+"/"+thisLocation.Name+".txt.gz");
                java.io.FileOutputStream inputLocal=new java.io.FileOutputStream(theFile);
                java.util.zip.GZIPOutputStream inputComprim=new java.util.zip.GZIPOutputStream(inputLocal);
                java.io.BufferedWriter bufferLocalW= new java.io.BufferedWriter(new java.io.OutputStreamWriter(inputComprim));

                System.out.println(theFile);

                bufferLocalW.write("[type]"+"\n");
                bufferLocalW.write(thisLocation.Type+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[source]"+"\n");
                bufferLocalW.write("CIRES"+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[site name]"+"\n");
                bufferLocalW.write(thisLocation.Name+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[county]"+"\n");
                bufferLocalW.write(thisLocation.County+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[state]"+"\n");
                bufferLocalW.write(thisLocation.State+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[latitude (deg:min:sec)]"+"\n");
                bufferLocalW.write(thisLocation.latitude+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[longitude (deg:min:sec)]"+"\n");
                bufferLocalW.write(thisLocation.longitude+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[altitude ASL (m)]"+"\n");
                bufferLocalW.write(thisLocation.Elevation+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[images]"+"\n");
                if (thisLocation.Images.length == 0) bufferLocalW.write("N/A"+"\n");
                for(int j=0;j<thisLocation.Images.length;j++) bufferLocalW.write(thisLocation.Images[j]+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[information]"+"\n");
                bufferLocalW.write(thisLocation.Source+"\n");
                bufferLocalW.write("\n");

                bufferLocalW.close();
                inputComprim.close();
                inputLocal.close();
                
                locationInfo=bufferRemoto.readLine();
                i++;
            }
            bufferRemoto.close();
            
            
        }catch(java.io.IOException IOE){
            System.err.println(IOE);
        }
        
    }
    
    private String[] findImages(java.io.File directory,String baseName){
        java.io.File[] files=directory.listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseName,"jpg"));
        String[] fileNames=new String[files.length];
        
        String imageDescription ="Image Description";
        for (int i=0;i<files.length;i++) {
            if (files[i].getName().indexOf("U") != -1) imageDescription="Upstream view for location "+baseName;
            if (files[i].getName().indexOf("D") != -1) imageDescription="Downstream view for location "+baseName;
            if (files[i].getName().indexOf("C") != -1) imageDescription="Cross section view for location "+baseName;
            if (files[i].getName().indexOf("Towanda") != -1) imageDescription="USGS Towanda gauge";
            if (files[i].getName().indexOf("RockCreek") != -1) imageDescription="USGS Rock Creek gauge";
        
        
            fileNames[i]="images/"+files[i].getName()+" ; "+imageDescription;
        }
        return fileNames;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ImportMapSourceASCII(new java.io.File("/home/ricardo/workFiles/myWorkingStuff/ResearchProjects/whitewaterKansas"),new java.io.File("/home/ricardo/workFiles/myWorkingStuff/ResearchProjects/whitewaterKansas/04-2004 Trip/"));
    }
    
}

class GpsLocationProperties {
    public String StateAlphaCode;
    public String State;
    public String Name;
    public String Type;
    public String County;
    public String latitude;
    public String longitude;
    public String Source="United States Geological Survey - Geographic Names Information System - Downloadable State and Topical Gazetteer Files - http://geonames.usgs.gov/geonames/stategaz/index.html";
    public String Elevation;
    public String[] Images={"N/A"};
    public String CellName;
    
    public GpsLocationProperties(String st) {
        State=st;
    }
   
}
