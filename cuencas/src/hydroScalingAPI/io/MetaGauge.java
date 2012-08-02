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


package hydroScalingAPI.io;
import java.util.TimeZone;
/**
 * Manages the Gauge-type data object
 * @author Ricardo Mantilla
 */
public class MetaGauge extends Object implements Comparable{
    
    private hydroScalingAPI.util.database.DB_Register gaugeRegister;
    private String[] gaugeTags={"[code]",
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
                                "[data accuracy]",
                                "[file location]"};
                                
    private double firstYearInSeries=0;
    
    /**
     * Creates a new instance of MetaGauge
     * @param register Takes in a registry created by the {@link hydroScalingAPI.io.GaugeReader}
     */
    public MetaGauge(hydroScalingAPI.util.database.DB_Register register) {
        gaugeRegister=register;
    }
    
    /**
     * Returns the desired property.  Available properties are:
     * <blockquote>
     * <p>[code]</p>
     * <p>[agency]</p>
     * <p>[type]</p>
     * <p>[site name]</p>
     * <p>[stream name]</p>
     * <p>[county]</p>
     * <p>[state]</p>
     * <p>[data source]</p>
     * <p>[latitude (deg:min:sec)]</p>
     * <p>[longitude (deg:min:sec)]</p>
     * <p>[altitude ASL (m)]</p>
     * <p>[drainage area (km^2)]</p>
     * <p>[data units]</p>
     * <p>[data accuracy]</p>
     * <p>[file location]</p>
     * </blockquote>
     * Note: the brackets must be included.
     * @param key The desired property
     * @return An object that contains the information requested
     */
    public Object getProperty(String key){
        return gaugeRegister.getProperty(key);
    }
    
    /**
     * Returns a description of the Gauge.
     * @return A String describing some gauge properties
     */
    public String toString(){
        return (String)gaugeRegister.getProperty("[code]")+" - "+(String)gaugeRegister.getProperty("[site name]")+" - "+(String)gaugeRegister.getProperty("[type]");
    }
    
    /**
     * The comparison method
     * @param obj The object to be compared with
     * @return A negative integer, zero, or a positive integer as this object is less than, equal
     * to, or greater than the specified object.
     */
    public int compareTo(Object obj) {
        return (this.toString()).compareToIgnoreCase(obj.toString());
    }
    
    /**
     * The original Gauge-type file location
     * @return A {@link java.io.File} pointing to the Gauge file
     */
    public java.io.File getFileLocation(){
        return (java.io.File)gaugeRegister.getProperty("[file location]");
    }
    
    /**
     * Returns a string with an identifier code for the Station
     * @return A String with the gauge code
     */
    public String getLabel(){
        return (String)gaugeRegister.getProperty("[type]")+" ["+(String)gaugeRegister.getProperty("[data units]")+"]";
    }
    
    /**
     * Creates a {@link visad.RealTuple} to be plotted in a {@link visad.Display}
     * @return The {@link visad.RealTuple}
     * @throws visad.VisADException Captures errors while creating the {@link visad.RealTuple}
     * @throws java.rmi.RemoteException Captures errors while creating the {@link visad.RealTuple}
     */
    public visad.RealTuple getPositionTuple() throws visad.VisADException, java.rmi.RemoteException{
        double xx=((Double)gaugeRegister.getProperty("[longitude (deg:min:sec)]")).doubleValue();
        double yy=((Double)gaugeRegister.getProperty("[latitude (deg:min:sec)]")).doubleValue();
        visad.Real[] rtd1 = {new visad.Real(visad.RealType.Longitude, xx),
                             new visad.Real(visad.RealType.Latitude,  yy)};
        return new visad.RealTuple(rtd1);
    }
    
    /**
     * Creates a {@link visad.Tuple} to be plotted in a {@link visad.Display} containing a text
     * label
     * @throws visad.VisADException Captures errors while creating the {@link visad.Tuple}
     * @throws java.rmi.RemoteException Captures errors while creating the {@link visad.Tuple}
     * @return The {@link visad.Tuple}
     */
    public visad.Tuple getTextTuple()  throws visad.VisADException, java.rmi.RemoteException{
        visad.TextType t = visad.TextType.getTextType("text");
        double xx=((Double)gaugeRegister.getProperty("[longitude (deg:min:sec)]")).doubleValue();
        double yy=((Double)gaugeRegister.getProperty("[latitude (deg:min:sec)]")).doubleValue();
        visad.Data[] rtd1 = {new visad.Real(visad.RealType.Longitude, xx),
                             new visad.Real(visad.RealType.Latitude,  yy),
                             new visad.Text(t, (String)gaugeRegister.getProperty("[code]"))};
        return new visad.Tuple(rtd1);
    }
    
    /**
     * Returns a 2 by n array where n is the number of data entries for the Gauge.
     * @throws java.io.IOException Captures errors while reading the file to retrive the data
     * @return A double[2][n] with double[0] the dates and double[1] the values
     */
   public double[][] getTimeAndData() throws java.io.IOException{
        
        float[] factors={1.0f,1.0f/12.0f,1.0f/365.0f};
        
        java.util.Calendar dateComposite=java.util.Calendar.getInstance();
        dateComposite.set(00,00,00,00,00,00);
        firstYearInSeries=-dateComposite.getTimeInMillis()/1000.0/3600.0/24.0/365.25;
        
        java.io.FileInputStream inputLocal=new java.io.FileInputStream(getFileLocation());
        java.util.zip.GZIPInputStream inputComprim=new java.util.zip.GZIPInputStream(inputLocal);
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.InputStreamReader(inputComprim));

        String fullLine;
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.equalsIgnoreCase("[data (yyyy.mm.dd.hh.mm.ss    value)]"));
        
        java.util.Vector theTextData=new java.util.Vector();
        fullLine=fileMeta.readLine();
        while (fullLine != null){
            theTextData.add(fullLine);
            fullLine=fileMeta.readLine();
        }
        
        fileMeta.close();
        inputComprim.close();
        inputLocal.close();
        
        double[][] theData=new double[2][theTextData.size()];
        java.util.StringTokenizer tokens;
        for (int i=0;i<theData[0].length;i++){
            tokens=new java.util.StringTokenizer((String)theTextData.get(i));
            theData[0][i]=firstYearInSeries+new hydroScalingAPI.tools.DateToElapsedTime(tokens.nextToken()).getYears();
            theData[1][i]=Float.parseFloat(tokens.nextToken());
        }
        
        return theData;
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
    }
    
}
