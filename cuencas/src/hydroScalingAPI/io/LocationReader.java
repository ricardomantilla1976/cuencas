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
 * LocationReader.java
 *
 * Created on June 14, 2003, 11:14 AM
 */

package hydroScalingAPI.io;

/**
 * This Class reads a Location-type site file in the database and creates a data
 * structure apropriate for
 * @author Ricardo Mantilla
 */
public class LocationReader {
    
    Object[] register;
    String[] parameters={   "[type]",
                            "[source]",
                            "[site name]",
                            "[county]",
                            "[state]",
                            "[latitude (deg:min:sec)]",
                            "[longitude (deg:min:sec)]",
                            "[altitude ASL (m)]",
                            "[images]",
                            "[information]"};
    
    
    /**
     * Creates a new instance of LocationReader
     * @param locationFile The file to be read.
     * @throws java.io.IOException CAtures errors during the file reading process
     */
    public LocationReader(java.io.File locationFile) throws java.io.IOException {
        
        register=new Object[parameters.length+1];
        
        java.io.FileInputStream inputLocal=new java.io.FileInputStream(locationFile);
        java.util.zip.GZIPInputStream inputComprim=new java.util.zip.GZIPInputStream(inputLocal);
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.InputStreamReader(inputComprim));

        String fullLine;
        int hemisphereFactor;

        for (int i=0;i<4;i++){
            do{
                fullLine=fileMeta.readLine();
            } while (!fullLine.equalsIgnoreCase(parameters[i]));
            register[i]=fileMeta.readLine();
        }
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[4]));
        register[4]=hydroScalingAPI.tools.StateName.CodeOrNameToStandardName(fileMeta.readLine());
        
        for (int i=5;i<7;i++){
            do{
                fullLine=fileMeta.readLine();
            } while (!fullLine.equalsIgnoreCase(parameters[i]));
            fullLine=fileMeta.readLine();
            if (fullLine.equalsIgnoreCase("n/a")) register[i]=fullLine;
                else register[i]=hydroScalingAPI.tools.DMSToDegrees.getDoubleDegrees(fullLine);
        }
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[7]));
        fullLine=fileMeta.readLine();
        if (fullLine.equalsIgnoreCase("n/a")) register[7]=fullLine;
            else register[7]=new Double(fullLine);
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[8]));
        java.util.Vector images=new java.util.Vector();
        while ((fullLine=fileMeta.readLine()) != null && fullLine.length() > 0){
            images.add(fullLine);
        }
        register[8]=images;
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[9]));
        
        String informationString=fileMeta.readLine();
        while ((fullLine=fileMeta.readLine()) != null && fullLine.length() > 0){
            informationString+="\n"+fullLine;
        }
        register[9]=informationString;
        
        fileMeta.close();
        inputComprim.close();
        inputLocal.close();
        
        register[register.length-1]=locationFile;
        
    }
    
    /**
     * Creates a registry to be used in a gauges database engine powered by the {@link
     * hydroScalingAPI.util.database.DataBaseEngine}
     * @return Returns a database registry {@link hydroScalingAPI.util.database.DB_Register}
     */
    public Object[] getRegisterForDataBase(){
        return register;
    }
    
    /**
     * Tests for this class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
            Object[] testRegister=new LocationReader(new java.io.File("/hidrosigDataBases/Whitewater_database/Sites/Locations/Kansas/Surveyed Location/500.txt.gz")).getRegisterForDataBase();
            for (int i=0;i<testRegister.length;i++) System.out.println(testRegister[i]);
        }catch(java.io.IOException IOE){
            System.err.println("Failed trying to load File");
            System.err.println(IOE);
        }
        
    }
    
}
