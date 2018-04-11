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

/**
 * Uses the information on a {@link hydroScalingAPI.io.MetaRaster} to read one of
 * the binary files associated to it.  Methods contained in this class return data
 * in different formats (Byte, Integer, Float or Double) and in different
 * arrangements (matrix or vector).
 * @author Ricardo Mantilla
 */
public class DataRaster extends Object {

    private hydroScalingAPI.io.MetaRaster localMetaData;
    
    private java.io.FileInputStream dataPath;
    private java.io.BufferedInputStream dataBuffer;
    private java.io.DataInputStream dataDataStream;
    
    private byte[][] dataByte;
    private int[][] dataInteger;
    private float[][] dataFloat;
    private double[][] dataDouble;
    float[] FDA;
    double[] FDAD;
    int noFaltantes;
    int nr,nc;
    
    /**
     * Creates a DataRaster object that will read files associated with the metaRaster
     * file given as a parameter.
     * @param metaInfo The {@link hydroScalingAPI.io.MetaRaster} with information about the binary file
     * to be read.
     * @throws java.io.IOException Captures problems while reading the binary file
     */
    public DataRaster(hydroScalingAPI.io.MetaRaster metaInfo) throws java.io.IOException{
        
        localMetaData=metaInfo;
                    
        nr=localMetaData.getNumRows();
        nc=localMetaData.getNumCols();
        
        dataPath=new java.io.FileInputStream(localMetaData.getLocationBinaryFile());
        dataBuffer=new java.io.BufferedInputStream(dataPath);
        dataDataStream=new java.io.DataInputStream(dataBuffer);
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte")){
            dataByte=new byte[nr][nc];
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) dataByte[i][j]=dataDataStream.readByte();
        }
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer")){
            dataInteger=new int[nr][nc];
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) dataInteger[i][j]=dataDataStream.readInt();
        }
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float")){
            dataFloat=new float[nr][nc];
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) dataFloat[i][j]=dataDataStream.readFloat();
        }
       
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double")){
            dataDouble=new double[nr][nc];
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) dataDouble[i][j]=dataDataStream.readDouble();
        }
        
        dataBuffer.close();
        dataDataStream.close();
    }
    
    /**
     * Casts data into Byte Format and returns a Matrix.
     * @return Returns an Matrix (Array[n][m]) of Bytes.
     */
    public byte[][] getByte(){
        if (dataByte != null) return dataByte;
        
        byte[][] aDatosByte=new byte[nr][nc];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosByte[i][j]=(byte) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosByte[i][j]=(byte) dataFloat[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosByte[i][j]=(byte) dataDouble[i][j];

        return aDatosByte;
    }
    
    /**
     * Casts data into Integer Format and returns a Matrix.
     * @return Returns an Matrix (Array[n][m]) of Integers.
     */
    public int[][] getInt(){
        if (dataInteger != null) return dataInteger;
        
        int[][] aDatosInteger=new int[nr][nc];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosInteger[i][j]=(int) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosInteger[i][j]=(int) dataFloat[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosInteger[i][j]=(int) dataDouble[i][j];
        
        return aDatosInteger;
    }
    
    /**
     * Casts data into Float Format and returns a Matrix.
     * @return Returns an Matrix (Array[n][m]) of Floats.
     */
    public float[][] getFloat(){
        if (dataFloat!=null) return dataFloat;
        
        float[][] aDatosFloat=new float[nr][nc];;

        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosFloat[i][j]=(float) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosFloat[i][j]=(float) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosFloat[i][j]=(float) dataDouble[i][j];

        return aDatosFloat;
    }
    
    /**
     * Casts data into Float Format and returns a Matrix.
     * @return Returns an Matrix (Array[n][m]) of Floats.
     */
    public float[][] getFloatResampled(int factor){
        float[][] aDatosFloat=new float[nr/factor][nc/factor];;

        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<nr/factor;i++) for(int j=0;j<nc/factor;j++) aDatosFloat[i][j]=(float) dataByte[i*factor][j*factor];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<nr/factor;i++) for(int j=0;j<nc/factor;j++) aDatosFloat[i][j]=(float) dataInteger[i*factor][j*factor];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<nr/factor;i++) for(int j=0;j<nc/factor;j++) aDatosFloat[i][j]=(int) dataFloat[i*factor][j*factor];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<nr/factor;i++) for(int j=0;j<nc/factor;j++) aDatosFloat[i][j]=(float) dataDouble[i*factor][j*factor];

        return aDatosFloat;
    }
    
    /**
     * Casts data into Float Format and returns a single column Vector.  This is
     * arrangement is usefull for Visad Flatfields.
     * @return Returns an single column Vector (Array[1][n*m]) of Floats.
     */
    public float[][] getFloatLine(){
        
        float[][] aDatosFloat=new float[1][nr*nc];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosFloat[0][i*nc+j]=(float) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosFloat[0][i*nc+j]=(float) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosFloat[0][i*nc+j]=(float) dataFloat[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Double"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosFloat[0][i*nc+j]=(float) dataDouble[i][j];

        return aDatosFloat;
    }
    
    /**
     * Casts data into Float Format and returns a single column Vector.  In addition
     * data is mapped in the range [0,1] using the cumulative probability distribution
     * function.  This arrangement is useful for Visad Flatfields and is used to create
     * a coloring scheeme that better represents data variability.
     * @return Returns an single column Vector (Array[1][n*m]) of histogram equalized Floats.
     */
    public float[][] getFloatLineEqualized(){
        
        float[][] aDatosFloat=getFloatLine();
        
        float faltante=new Float(localMetaData.getProperty("[Missing]")).floatValue();
        hydroScalingAPI.util.statistics.Stats statDatos= new hydroScalingAPI.util.statistics.Stats(aDatosFloat,faltante);
        noFaltantes = statDatos.dataCount;
        FDA=new float[statDatos.dataCount];
        int conDat=0;
        
        for (int j=0;j<aDatosFloat[0].length;j++) 
            if (aDatosFloat[0][j] != faltante){ 
                FDA[conDat]=aDatosFloat[0][j];
                conDat++;
            }

        java.util.Arrays.sort(FDA);
        for (int j=0;j<aDatosFloat[0].length;j++){
            if (aDatosFloat[0][j] != faltante){
                aDatosFloat[0][j]=(float) (1+java.util.Arrays.binarySearch(FDA,aDatosFloat[0][j])/(float) (statDatos.dataCount-1)*254);
            } else {
                aDatosFloat[0][j]=Float.NaN;
            }
        }

        return aDatosFloat;
    }
    
    /**
     * Casts data into Float Format and returns a matrix (Array[n][m]).  In addition
     * data is mapped in the range [0,1] using the cumulative probability distribution
     * function.  This arrangement is useful for Visad Flatfields and is used to create
     * a coloring scheeme that better represents data variability.
     * @return Returns an single column Vector (Array[1][n*m]) of histogram equalized Floats.
     */
    public float[][] getFloatLineResampled(int factor){
        
        float[][] fullLine=getFloatResampled(factor);
        float[][] aDatosFloat=new float[1][fullLine.length*fullLine[0].length];

        for (int i = 0; i < fullLine.length; i++) for (int j = 0; j < fullLine[0].length; j++) {
            aDatosFloat[0][i*fullLine[0].length+j]=fullLine[i][j];
        }
        
        return aDatosFloat;
    }
    
    
    /**
     * Casts data into Float Format and returns a single column Vector.  In addition
     * data is mapped in the range [0,1] using the cumulative probability distribution
     * function.  This arrangement is useful for Visad Flatfields and is used to create
     * a coloring scheeme that better represents data variability.
     * @return Returns an single column Vector (Array[1][n*m]) of histogram equalized Floats.
     */
    public float[][] getFloatLineEqualizedResampled(int factor){
        
        float[][] aDatosFloat=getFloatLineResampled(factor);
        
        float faltante=new Float(localMetaData.getProperty("[Missing]")).floatValue();
        hydroScalingAPI.util.statistics.Stats statDatos= new hydroScalingAPI.util.statistics.Stats(aDatosFloat,faltante);
        noFaltantes = statDatos.dataCount;
        FDA=new float[statDatos.dataCount];
        int conDat=0;
        
        for (int j=0;j<aDatosFloat[0].length;j++) 
            if (aDatosFloat[0][j] != faltante){ 
                FDA[conDat]=aDatosFloat[0][j];
                conDat++;
            }

        java.util.Arrays.sort(FDA);
        for (int j=0;j<aDatosFloat[0].length;j++){
            if (aDatosFloat[0][j] != faltante){
                aDatosFloat[0][j]=(float) (1+java.util.Arrays.binarySearch(FDA,aDatosFloat[0][j])/(float) (statDatos.dataCount-1)*254);
            } else {
                aDatosFloat[0][j]=Float.NaN;
            }
        }

        return aDatosFloat;
    }
    
    /**
     * Casts data into Float Format and returns a matrix (Array[n][m]).  In addition
     * data is mapped in the range [0,1] using the cumulative probability distribution
     * function.  This arrangement is useful for Visad Flatfields and is used to create
     * a coloring scheeme that better represents data variability.
     * @return Returns an single column Vector (Array[1][n*m]) of histogram equalized Floats.
     */
    public float[][] getFloatEqualized(){
        
        float[][] aDatosFloat=getFloat();
        
        float faltante=new Float(localMetaData.getProperty("[Missing]")).floatValue();
        hydroScalingAPI.util.statistics.Stats statDatos= new hydroScalingAPI.util.statistics.Stats(aDatosFloat,faltante);
        noFaltantes = statDatos.dataCount;
        FDA=new float[statDatos.dataCount];
        int conDat=0;
        for (int i=0;i<aDatosFloat.length;i++) {
            for (int j=0;j<aDatosFloat[i].length;j++) {
                if (aDatosFloat[i][j] != faltante){ 
                    FDA[conDat]=aDatosFloat[i][j];
                    conDat++;
                }
            }
        }

        java.util.Arrays.sort(FDA);
        for (int i=0;i<aDatosFloat.length;i++){
            for (int j=0;j<aDatosFloat[i].length;j++){
                if (aDatosFloat[i][j] != faltante){
                    aDatosFloat[i][j]=(float) (1+java.util.Arrays.binarySearch(FDA,aDatosFloat[i][j])/(float) (statDatos.dataCount-1)*254);
                } else {
                    aDatosFloat[i][j]=Float.NaN;
                }
            }
        }

        return aDatosFloat;
    }
    
    /**
     * Maps a number into the [0,1] according to the cumulative distribution function.
     * @param f The value to be transformed
     * @return a Float in the interval (0,1)
     */
    public float getEqualized(float f){
        try{
            if(new Float(localMetaData.getProperty("[Missing]")).floatValue() == f)
                return 0f;
            int i=0;
            while(FDA[i] < f)
                i++;
            return (1+i/(float)(noFaltantes-1)*254);
        }catch(Exception ex){
            return 255;
        }
    }
    
    /**
     * Casts data into Float Format and returns a matrix (Array[n][m]).  In addition data 
     * is mapped in the range [0,1] using a predetrmined cumulative probability distribution
     * function.  This arrangement is useful for Visad Flatfields and is used to create
     * a coloring scheeme that better represents data variability.
     * @param CDF The Cumulative Distristribution Function to be used.
     * @return Returns an single column Vector (Array[1][n*m]) of histogram equalized Floats.
     */
    public float[][] getFloatLineEqualized(float[] CDF){
        
        //FDA es un vector con datos ordendados, es util cuando se va a equalizar un mapa basado en un conjunto de mapas
        
        float[][] aDatosFloat=getFloatLine();
        
        float faltante=new Float(localMetaData.getProperty("[Missing]")).floatValue();
        
        for (int j=0;j<aDatosFloat[0].length;j++){
            if (aDatosFloat[0][j] != faltante){
                aDatosFloat[0][j]=(float) (1+java.util.Arrays.binarySearch(CDF,aDatosFloat[0][j])/(float) (CDF.length-1)*254);
            } else {
                aDatosFloat[0][j]=0.0f;
            }
        }

        return aDatosFloat;
    }
    
    /**
     * Casts data into Double Format and returns a Matrix.
     * @return Returns an Matrix (Array[n][m]) of Floats.
     */
    public double[][] getDouble(){
        //  System.out.println("test");
        if (dataDouble != null) return dataDouble;
        
        double[][] aDatosDouble=new double[nr][nc];;
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosDouble[i][j]=(double) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosDouble[i][j]=(double) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            for (int i=0;i<nr;i++) for(int j=0;j<nc;j++) aDatosDouble[i][j]=(double) dataFloat[i][j];
        //System.out.println("test");
        return aDatosDouble;
    }
    
    /**
     * Reads one value from the File.
     * @return Returns the read value in double format.
     */
    public double getDouble(int i,int j){
        if (dataDouble != null) return dataDouble[i][j];
        
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
            return (double) dataByte[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
            return (double) dataInteger[i][j];
        if (localMetaData.getProperty("[Format]").equalsIgnoreCase("Float"))
            return (double) dataFloat[i][j];
        
        return 0.0;
        
    }
    
    /*public float getEqualized(double f){
        try{
            if(new Double(localMetaData.getProperty("[Missing]")).doubleValue() == f)
                return 0f;
            int i=0;
            while(FDA[i] < f)
                i++;
            return (1+i/(float)(noFaltantes-1)*254);
        }catch(Exception ex){
            return 255;
        }
    }
    
    public double[][] getDoubleEqualized(){
        
        double[][] aDatosFloat=getDouble();
        
        double faltante=new Double(localMetaData.getProperty("[Missing]")).doubleValue();
        hydroScalingAPI.tools.Stats statDatos= new hydroScalingAPI.tools.Stats(aDatosFloat,faltante);
        noFaltantes = statDatos.dataCount;
        FDAD=new double[statDatos.dataCount];
        int conDat=0;
        for (int i=0;i<aDatosFloat.length;i++) {
            for (int j=0;j<aDatosFloat[i].length;j++) {
                if (aDatosFloat[i][j] != faltante){ 
                    FDAD[conDat]=aDatosFloat[i][j];
                    conDat++;
                }
            }
        }

        java.util.Arrays.sort(FDAD);
        for (int i=0;i<aDatosFloat.length;i++){
            for (int j=0;j<aDatosFloat[i].length;j++){
                if (aDatosFloat[i][j] != faltante){
                    aDatosFloat[i][j]=(1+java.util.Arrays.binarySearch(FDAD,aDatosFloat[i][j])/(double) (statDatos.dataCount-1)*254);
                } else {
                    aDatosFloat[i][j]=Double.NaN;
                }
            }
        }

        return aDatosFloat;
    }
    
    public double[][] getDoubleLineEqualized(){
        
        double[][] aDatosFloat=getDoubleLine();
        
        double faltante=new Float(localMetaData.getProperty("[Missing]")).doubleValue();
        hydroScalingAPI.tools.Stats statDatos= new hydroScalingAPI.tools.Stats(aDatosFloat,faltante);
        noFaltantes = statDatos.dataCount;
        FDAD=new double[statDatos.dataCount];
        int conDat=0;
        
        for (int j=0;j<aDatosFloat[0].length;j++) 
            if (aDatosFloat[0][j] != faltante){ 
                FDAD[conDat]=aDatosFloat[0][j];
                conDat++;
            }

        java.util.Arrays.sort(FDAD);
        for (int j=0;j<aDatosFloat[0].length;j++){
            if (aDatosFloat[0][j] != faltante){
                aDatosFloat[0][j]=(double) (1+java.util.Arrays.binarySearch(FDAD,aDatosFloat[0][j])/(double) (statDatos.dataCount-1)*254);
            } else {
                aDatosFloat[0][j]=Double.NaN;
            }
        }

        return aDatosFloat;
    }
    
    public double[][] getDoubleLineEqualized(double[] FDAD){
        
        //FDAD es un vector con datos ordendados, es util cuando se va a equalizar un mapa basado en un conjunto de mapas
        
        double[][] aDatosFloat=getDoubleLine();
        
        double faltante=new Double(localMetaData.getProperty("[Missing]")).doubleValue();
        
        for (int j=0;j<aDatosFloat[0].length;j++){
            if (aDatosFloat[0][j] != faltante){
                aDatosFloat[0][j]=(double) (1+java.util.Arrays.binarySearch(FDAD,aDatosFloat[0][j])/(double) (FDAD.length-1)*254);
            } else {
                aDatosFloat[0][j]=0.0f;
            }
        }

        return aDatosFloat;
    }*/

    /**
     * A few tests of this class.
     * @param args Arguments are ignored.
     */
    public static void main (String args[]) {
        try{
            hydroScalingAPI.io.MetaRaster metaRaster1=new hydroScalingAPI.io.MetaRaster (new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/randomField/randomMean100.metaVHC"));
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Hydrology/storms/randomField/randomMean100.180000.01.July.1971.vhc"));

            hydroScalingAPI.io.DataRaster datosRaster=new hydroScalingAPI.io.DataRaster (metaRaster1);
            float[][] theData=datosRaster.getFloat();
            for (int i=0;i<metaRaster1.getNumRows();i++) {
                for(int j=0;j<metaRaster1.getNumCols();j++) System.out.print(theData[i][j]+" ");
                System.out.println("");
            }
            
            System.gc();
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        System.exit(0);
    }

}
