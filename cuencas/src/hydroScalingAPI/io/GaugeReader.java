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
 * gaugeReader.java
 *
 * Created on June 13, 2003, 11:33 AM
 */

package hydroScalingAPI.io;

/**
 * This class reads Gauge-type files that contain a time series.
 * @author Ricardo Mantilla
 */
public class GaugeReader{

    Object[] register;
    String[] parameters={   "[code]",
                            "[agency]",
                            "[type]",
                            "[site name]",
                            "[stream name]",
                            "[county]",
                            "[state]",
                            "[data source]",
                            "[latitude (deg:min:sec)]",
                            "[longitude (deg:min:sec)]",
                            "[altitude ASL (m)]",
                            "[drainage area (km^2)]",
                            "[data units]",
                            "[data accuracy]"};
    
    
    /**
     * Creates a new instance of gaugeReader
     * @param gaugeFile The file containing information about a given gauge.
     * @throws java.io.IOException Captures errors while reading a file.
     */
    public GaugeReader(java.io.File gaugeFile) throws java.io.IOException {
        
        register=new Object[parameters.length+1];
        
        java.io.FileInputStream inputLocal=new java.io.FileInputStream(gaugeFile);
        java.util.zip.GZIPInputStream inputComprim=new java.util.zip.GZIPInputStream(inputLocal);
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.InputStreamReader(inputComprim));

        String fullLine;
        int hemisphereFactor;

        for (int i=0;i<6;i++){
            try{
            do{
                fullLine=fileMeta.readLine();
            } while (!fullLine.equalsIgnoreCase(parameters[i]));
            register[i]=fileMeta.readLine();
            }catch(NullPointerException ne){
                System.out.println(gaugeFile);
            }
        }
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[6]));
        register[6]=hydroScalingAPI.tools.StateName.CodeOrNameToStandardName(fileMeta.readLine());
        
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase(parameters[7]));
        register[7]=fileMeta.readLine();
        
        for (int i=8;i<10;i++){
            do{
                fullLine=fileMeta.readLine();
            } while (!fullLine.equalsIgnoreCase(parameters[i]));
            fullLine=fileMeta.readLine();
            if (fullLine.equalsIgnoreCase("n/a")) register[i]=fullLine;
                else register[i]=hydroScalingAPI.tools.DMSToDegrees.getDoubleDegrees(fullLine);
        }
        for (int i=10;i<12;i++){
            do{
                fullLine=fileMeta.readLine();
            } while (!fullLine.equalsIgnoreCase(parameters[i]));
            fullLine=fileMeta.readLine();
            if (fullLine.equalsIgnoreCase("n/a") || fullLine.equalsIgnoreCase("n / a")) register[i]=fullLine;
                else register[i]=new Double(fullLine);
        }
        for (int i=12;i<parameters.length;i++){
            do{
                fullLine=fileMeta.readLine();
            } while (!fullLine.equalsIgnoreCase(parameters[i]));
            register[i]=fileMeta.readLine();
        }
        
        fileMeta.close();
        inputComprim.close();
        inputLocal.close();
        
        register[register.length-1]=gaugeFile;
        
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
     * Tests of the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
            java.io.File[] filesToRead=new java.io.File("/hidrosigDataBases/Continental_US_database/Sites/Gauges/Precipitation").listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("gz"));
            System.out.println("There are "+filesToRead.length+" files");
            for (int i=0;i<filesToRead.length;i++){
                try{
                    System.out.println("Ready to read file "+filesToRead[i].getName());
                    GaugeReader testRegister=new GaugeReader(filesToRead[i]);
                }catch(java.io.IOException IOE){
                    System.err.println("Failed trying to load File");
                    System.err.println(IOE);
                }
            }
            
        
        
    }
    
}
