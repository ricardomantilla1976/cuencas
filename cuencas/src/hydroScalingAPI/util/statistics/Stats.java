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


package hydroScalingAPI.util.statistics;

/**
 * An object that calculates the principal statistics of an array or matrix of
 * doubles or floats
 * @author Ricardo Mantilla
 */
public class Stats{
    
    /**
     * The average
     */
    public float meanValue=0.0f;
    /**
     * The standard deviation
     */
    public float standardDeviation=0.0f;
    /**
     * The skewness
     */
    public float skewness=0.0f;
    /**
     * Kurtosis parameter
     */
    public float kurtosis=0.0f;
    /**
     * The maximum value
     */
    public float maxValue=-Float.MAX_VALUE;
    /**
     * The minimum
     */
    public float minValue=Float.MAX_VALUE;
    /**
     * The sum of the elements
     */
    public float total=0.0f;
    /**
     * The total number of elements
     */
    public int dataCount=0;

    /**
     * Creates new Stats
     * @param data The data array
     */
    public Stats(float[][] data) {
        this(data,Float.NaN);
    }
    
    /**
     * Creates new Stats
     * @param data The data array
     */
    public Stats(float[] data) {
        this(data,Float.NaN);
    }
    
    /**
     * Creates new Stats
     * @param missing The missing value
     * @param data The data array
     */
    public Stats(float[][] data, float missing) {
        for (int j=0;j<data.length;j++){
            for (int k=0;k<data[j].length;k++){
                if (data[j][k] != missing && !(new Float(data[j][k]).isNaN())) {
                    minValue=(float) Math.min(minValue,data[j][k]);
                    maxValue=(float) Math.max(maxValue,data[j][k]);
                    meanValue+=data[j][k];
                    standardDeviation+=(float) Math.pow(data[j][k],2);
                    dataCount++;
                }
            }
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-meanValue*meanValue);
        for (int j=0;j<data.length;j++){
            for (int k=0;k<data[j].length;k++){
                skewness+=Math.pow(data[j][k],3)-meanValue;
                kurtosis+=Math.pow(data[j][k],4)-meanValue;
            }
        }
        skewness/=dataCount*Math.pow(standardDeviation,3);
        kurtosis/=dataCount*Math.pow(standardDeviation,4);
    }
    
    /**
     * Creates a new Stats. Paramters must be assigned after.
     */
    public Stats() {
    
    }
    
    /**
     * Creates new Stats
     * @param missing The missing value
     * @param data The data array
     */
    public Stats(float[] data,float missing) {
        
        for (int j=0;j<data.length;j++){
            if (data[j] != missing && !(new Float(data[j]).isNaN())) {
                minValue = (float) Math.min(minValue,data[j]);
                maxValue = (float)Math.max(maxValue,data[j]);
                meanValue += (float)data[j];
                standardDeviation+= (float) Math.pow(data[j],2);
                dataCount++;
            }
            
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-Math.pow(meanValue,2));
        for (int j=0;j<data.length;j++){
            if (data[j] != missing && !(new Float(data[j]).isNaN())) {
                skewness+=Math.pow(data[j],3)-meanValue;
                kurtosis+=Math.pow(data[j],4)-meanValue;
            }
        }
        skewness/=dataCount*Math.pow(standardDeviation,3);
        kurtosis/=dataCount*Math.pow(standardDeviation,4);
        
    }
    
    /**
     * Creates new Stats
     * @param data The data array
     */
    public Stats(double[][] data) {
        this(data,Float.NaN);
    }
    
    /**
     * Creates new Stats
     * @param data The data array
     */
    public Stats(double[] data) {
        this(data,Float.NaN);
    }
    
    /**
     * Creates new Stats
     * @param missing The missing value
     * @param data The data array
     */
    public Stats(double[][] data, double missing) {
        for (int j=0;j<data.length;j++){
            for (int k=0;k<data[j].length;k++){
                if (data[j][k] != missing && !(new Float(data[j][k]).isNaN())) {
                    minValue=(float) Math.min(minValue,data[j][k]);
                    maxValue=(float) Math.max(maxValue,data[j][k]);
                    meanValue+=data[j][k];
                    standardDeviation+=(float) Math.pow(data[j][k],2);
                    dataCount++;
                }
            }
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-meanValue*meanValue);
        for (int j=0;j<data.length;j++){
            for (int k=0;k<data[j].length;k++){
                if (data[j][k] != missing && !(new Float(data[j][k]).isNaN())) {
                    skewness+=Math.pow(data[j][k],3)-meanValue;
                    kurtosis+=Math.pow(data[j][k],4)-meanValue;
                }
            }
        }
        skewness/=dataCount*Math.pow(standardDeviation,3);
        kurtosis/=dataCount*Math.pow(standardDeviation,4);
    }
    
    /**
     * Creates new Stats
     * @param missing The missing value
     * @param data The data array
     */
    public Stats(double[] data,float missing) {
        
        for (int j=0;j<data.length;j++){
            if (data[j] != missing && !(new Float(data[j]).isNaN())) {
                minValue = (float) Math.min(minValue,data[j]);
                maxValue = (float)Math.max(maxValue,data[j]);
                meanValue += (float)data[j];
                standardDeviation+= (float) Math.pow(data[j],2);
                dataCount++;
            }
            
        }
        total=meanValue;
        meanValue/=(float) dataCount;
        standardDeviation=(float) Math.sqrt(standardDeviation/(float) dataCount-Math.pow(meanValue,2));
        for (int j=0;j<data.length;j++){
            if (data[j] != missing && !(new Float(data[j]).isNaN())) {
                skewness+=Math.pow(data[j],3)-meanValue;
                kurtosis+=Math.pow(data[j],4)-meanValue;
            }
        }
        skewness/=dataCount*Math.pow(standardDeviation,3);
        kurtosis/=dataCount*Math.pow(standardDeviation,4);
        
    }
    
    /**
     * Composes a String with the statistics for the data
     * @return The string with statistics
     */
    public String toString(){
        String text=      "Mean Value: "+   meanValue+"\n"+
                          "Standard Deviation: "+   standardDeviation+"\n"+
                          "Skewness: "+   skewness+"\n"+
                          "Kurtosis: "+   kurtosis+"\n"+
                          "Max Value: "+   maxValue+"\n"+
                          "Min Value: "+   minValue+"\n"+
                          "Total: "+   total+"\n"+
                          "Data Count: "+   dataCount+"\n";
        return text;
    }
    
    /**
     * Tests for the class
     * @param args The command line parameters
     */
    public static void main (String args[]) {
        float[][] estat={{1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f},
                         {1.0f,2.0f,3.0f,4.0f,5.0f,6.0f,7.0f,8.0f,9.0f}};
                         
        /*float[][] estat={{0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f},
                         {0.0f,0.0f,0.0f}};*/
        hydroScalingAPI.util.statistics.Stats algo= new Stats(estat,0);
        System.out.println(algo.toString());
    }
    

}
