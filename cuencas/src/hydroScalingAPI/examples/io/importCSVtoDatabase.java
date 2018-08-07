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
 * importCSVtoDatabase.java
 *
 * Created on April 25, 2004, 10:15 AM
 */

package hydroScalingAPI.examples.io;

/**
 *
 * @author Ricardo Mantilla
 */
public class importCSVtoDatabase {
    
    private hydroScalingAPI.mainGUI.objects.GUI_InfoManager localInfoManager;
    private hydroScalingAPI.util.database.DataBaseEngine localDatabaseEngine;
    private hydroScalingAPI.mainGUI.objects.LocationsManager localLocationsManager;
    
    /** Creates a new instance of importCSVtoDatabase */
    public importCSVtoDatabase() {
        
        //Load Ashley database
        localInfoManager=new hydroScalingAPI.mainGUI.objects.GUI_InfoManager(new java.io.File("/hidrosigDataBases/Ashley_NZ_database/"));
        localDatabaseEngine=new hydroScalingAPI.util.database.DataBaseEngine();
        localLocationsManager=new hydroScalingAPI.mainGUI.objects.LocationsManager(localInfoManager,localDatabaseEngine);
        
        while(!localLocationsManager.isLoaded()){
            new visad.util.Delay(1000);
            System.out.print("/");
        }
        System.out.println("/");
        
        try{
            String[] Separators={"\t",
                                 "\t",
                                 "\t",
                                 "\t",
                                 "\t\t",
                                 "\t",
                                 "\t\t",
                                 "\t",
                                 "\t\t",
                                 "\t",
                                 "\t",
                                 "\t\t",
                                 "\t",
                                 "\t", //flow
                                 "\t",
                                 "\t",
                                 "\t",
                                 "\t",
                                 "\t",
                                 "\t",
                                 "\t\t"};
            
            java.io.File csvData=new java.io.File("/home/ricardo/workFiles/guptaWork/newZeland/data/ashley.csv");
            java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(csvData));
            String fullLine;
            fullLine=fileMeta.readLine();
            String[] Labels=fullLine.split(",");
            
            fullLine=fileMeta.readLine();
            while (fullLine != null) {
                String[] Attributes=fullLine.split(",");
                
                String Stamp="";
                for(int i=5;i<Labels.length;i++){
                    Stamp+=Labels[i]+":"+Separators[i-5]+Attributes[i]+"\n";
                }
                
                String spotLabel=Attributes[0];
                hydroScalingAPI.io.MetaLocation locationInfo=localLocationsManager.getLocation(spotLabel.trim(),"Hydraulic Geometry");
                if(locationInfo != null) {
                    System.out.println(locationInfo.toString());
                    
                    Object[] register={ locationInfo.getProperty("[type]"),
                                        locationInfo.getProperty("[source]"),
                                        locationInfo.getProperty("[site name]"),
                                        locationInfo.getProperty("[county]"),
                                        locationInfo.getProperty("[state]"),
                                        hydroScalingAPI.tools.DegreesToDMS.getprettyString(((Double)locationInfo.getProperty("[latitude (deg:min:sec)]")).doubleValue(),0),
                                        hydroScalingAPI.tools.DegreesToDMS.getprettyString(((Double)locationInfo.getProperty("[longitude (deg:min:sec)]")).doubleValue(),1),
                                        Attributes[24],
                                        new Object[] {},
                                        Stamp};
                    
                    new hydroScalingAPI.io.LocationWriter((java.io.File)locationInfo.getProperty("[file location]"),register);
                    System.out.println((java.io.File)locationInfo.getProperty("[file location]"));
                }
                
                fullLine=fileMeta.readLine();
            }

            fileMeta.close();
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        } 
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new importCSVtoDatabase();
    }
    
}
