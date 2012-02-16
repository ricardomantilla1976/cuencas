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
 * This class reads a *.metaDEM or *.metaVHC and creates an object that descibes
 * an associated binary files.
 * @author Ricardo Mantilla
 */
public class MetaRaster{
    
    private java.io.BufferedReader fileMeta;

    private java.io.File locationMeta;
    private java.io.File locationBinaryFile;
    private boolean completed;
    
    private String originalFormat;

    private java.util.Hashtable properties;
    private java.util.Hashtable categories;

    private String[] parameters= { 
                                    "[Name]",
                                    "[Southernmost Latitude]",
                                    "[Westernmost Longitude]",
                                    "[Longitudinal Resolution (ArcSec)]",
                                    "[Latitudinal Resolution (ArcSec)]",
                                    "[# Columns]",
                                    "[# Rows]",
                                    "[Format]",
                                    "[Missing]",
                                    "[Temporal Resolution]",
                                    "[Units]",
                                    "[Information]"
                                };

    /**
     * Creates new MetaRaster using another metaRaster as a template
     * @param mr The MetaRaster to be used as template
     */
                                
    public MetaRaster(MetaRaster mr){
        
        properties=mr.cloneProperties();
        originalFormat=(String)properties.get("[Format]");
        
    }
    
    /**
     * Creates an empty MetaRaster instance
     */
    public MetaRaster(){
        
        properties=new java.util.Hashtable();
        
    }
    
    /**
     * Creates a MetaRaster instance using the information on a file.  The file must
     * contain tags and information for each tag.  File formats are descibed in the
     * Developer's Manual.
     * @param file The file that contains the information that describes associated binary files
     * @throws java.io.IOException Captures errors while reading the meta file
     */
    public MetaRaster(java.io.File file) throws java.io.IOException{

        locationMeta=file;

        checkParameters(file);
        
        properties=new java.util.Hashtable();
        
        fileMeta = new java.io.BufferedReader(new java.io.FileReader(file));

        String fullLine;
        int hemisphereFactor;

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[0]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[1]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[2]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[3]));
        try{
            Double resLon=new Double(fileMeta.readLine());
            properties.put(fullLine,resLon);
        }
        catch(NumberFormatException Ex){
            System.err.println("This is not a number [Longitudinal Resolution (ArcSec)]");
            return;
        }

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[4]));
        try{
            Double resLat=new Double(fileMeta.readLine());
            properties.put(fullLine,resLat);
        }
        catch(NumberFormatException Ex){
            System.err.println("This is not a number [Latitudinal Resolution (ArcSec)]");
            return;
        }
        
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[5]));
        try{
            Integer nCols=new Integer(fileMeta.readLine());
            properties.put(fullLine,nCols);
        }
        catch(NumberFormatException Ex){
            System.err.println("This is not a number [# Columns]");
            return;
        }
        

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[6]));
        try{
            Integer nRows=new Integer(fileMeta.readLine());
            properties.put(fullLine,nRows);
        }
        catch(NumberFormatException Ex){
            System.err.println("This is not a number [# Rows]");
            return;
        }
        
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[7]));
        properties.put(fullLine,fileMeta.readLine());

        originalFormat=(String)properties.get(fullLine);
        
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[8]));
        properties.put(fullLine,fileMeta.readLine());

        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[9]));
        properties.put(fullLine,fileMeta.readLine());
       
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[10]));
        properties.put(fullLine,fileMeta.readLine());
       
        if (((String)(properties.get("[Units]"))).equalsIgnoreCase("categories")){
            categories=new java.util.Hashtable();
            while(!(fullLine=fileMeta.readLine()).equalsIgnoreCase("")){
                java.util.StringTokenizer tokens=new java.util.StringTokenizer(fullLine,":");
                if (tokens.countTokens()>1){
                    categories.put(tokens.nextToken(),tokens.nextToken());
                } else {
                    categories.put(tokens.nextToken(),"Unknown");
                }
            }
        }
        
        do{
            fullLine=fileMeta.readLine();
        } while (!fullLine.trim().equalsIgnoreCase(parameters[11]));
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
            if (fullLine.trim().equalsIgnoreCase(parameters[i])) i++;
        } while (i<parameters.length && fullLine != null);

        fileMeta.close();

        if (i == parameters.length) return true;
        else return false;

    }
    
    /**
     * Creates a String that describes the information in the meta file
     * @return A string describing the information
     */
    public String toString(){
        return getName()+" - "+getLocationMeta().getName();
    }
    
    /**
     * Sets the path where the metaFile resides.  This method is useful when an empty
     * MetaRaster is created and it is necesary to sprcify the path where the meta file
     * will be writen
     * @param file The file where the meta file resides
     */
    public void setLocationMeta(java.io.File file){
        locationMeta=file;
    }
    
    /**
     * Sets the path of the binary file assicitated with the information of the
     * MetaRaster.
     * @param file The path to the binary file
     */
    public void setLocationBinaryFile(java.io.File file){
        locationBinaryFile=file;
    }
    
    /**
     * Sets the value for the [Name] tag
     * @param newName The new name to assign to the information
     */
    public void setName(String newName){
        properties.put("[Name]",newName);
    }
    
    /**
     * Sets the value for the [Southernmost Latitude] tag
     * @param newMinLat A properly formated string DD:MM:SS.SS [N/S]. See {@link hydroScalingAPI.tools.DegreesToDMS}
     * for a static methods format geographic position descriptors.
     */
    public void setMinLat(String newMinLat){
        properties.put("[Southernmost Latitude]",newMinLat);
    }
    
    /**
     * Sets the value for the [Westernmost Longitude] tag
     * @param newMinLon A properly formated string DD:MM:SS.SS [N/S]. See {@link hydroScalingAPI.tools.DegreesToDMS}
     * for a static methods format geographic position descriptors.
     */
    public void setMinLon(String newMinLon){
        properties.put("[Westernmost Longitude]",newMinLon);
    }
    
    /**
     * Sets the value for the [Latitudinal Resolution (ArcSec)] tag
     * @param newResLat A double precision number indicating the Latitudinal resolution (vertical pixel
     * size) in ArcSeconds
     */
    public void setResLat(double newResLat){
        properties.put("[Latitudinal Resolution (ArcSec)]",new Double(newResLat));
    }
    
    /**
     * Sets the value for the [Longitudinal Resolution (ArcSec)] tag
     * @param newResLon A double precision number indicating the Longitudinal resolution (vertical pixel
     * size) in ArcSeconds
     */
    public void setResLon(double newResLon){
        properties.put("[Longitudinal Resolution (ArcSec)]",new Double(newResLon));
    }
    
    /**
     * Sets the value for the [# Columns] tag
     * @param newNumCols A integer indicating the number of Columns in the raster binary file
     */
    public void setNumCols(int newNumCols){
        properties.put("[# Columns]",new Integer(newNumCols));
    }
    
    /**
     * Sets the value for the [# Rows] tag
     * @param newNumRows A integer indicating the number of Rows in the raster binary file
     */
    public void setNumRows(int newNumRows){
        properties.put("[# Rows]",new Integer(newNumRows));
    }
    
    /**
     * Sets the value for the [Format] tag
     * @param newFormat A String indicating the format of the raster binary file.  Available formats
     * are: Byte, Integer, Float and Double.
     */
    public void setFormat(String newFormat){
        properties.put("[Format]",newFormat);
    }
    
    /**
     * Sets the value for the [Missing] tag
     * @param newMissinng A number indicating missing information in the binary file
     */
    public void setMissing(String newMissinng){
        properties.put("[Missing]",newMissinng);
    }
    
    /**
     * Restores the value of the [Format] tag to its original value if it has been
     * modified using the setFormat(String format) method
     */
    public void restoreOriginalFormat(){
        properties.put("[Format]",originalFormat);
    }
    
    /**
     * Sets the value for the [Temporal Resolution] tag
     * @param tempScale A string describing the temporal resolution.  It can be Fix for spatial
     * variables with no temporal variability (e.g. a digital elevation model) or a
     * number followed by a time descriptor (e.g. 1-minute, 20-hours, 1-year, etc)
     */
    public void setTemporalScale(String tempScale){
        properties.put("[Temporal Resolution]",tempScale);
    }
    
    /**
     * Sets the value of the [Units] tag
     * @param newUnits A string describing the units of the information
     */
    public void setUnits(String newUnits){
        properties.put("[Units]",newUnits);
    }
    
    /**
     * Sets the value of the [Information] tag
     * @param newInformation A string describing the information
     */
    public void setInformation(String newInformation){
        properties.put("[Information]",newInformation);
    }
    
    /**
     * Returns a java.io.File object pointing to the location of the meta file
     * @return A java.io.File
     */
    public java.io.File getLocationMeta(){
        return locationMeta;
    }
    
    /**
     * Returns a java.io.File object pointing to the location of the binary file
     * described by this MetaRaster
     * @return a java.io.File
     */
    public java.io.File getLocationBinaryFile(){
        return locationBinaryFile;
    }
    
    /**
     * Returs the value of a property of the MetaRaster.  Available properites are:<br>
     * <p>[Name]</p>
     * <p>[Southernmost Latitude]</p>
     * <p>[Westernmost Longitude]</p>
     * <p>[Longitudinal Resolution (ArcSec)]</p>
     * <p>[Latitudinal Resolution (ArcSec)]</p>
     * <p>[# Columns][# Rows]</p>
     * <p>[Format]</p>
     * <p>[Missing]</p>
     * <p>[Temporal Resolution]</p>
     * <p>[Units]</p>
     * <p>[Information] </p>
     * @param prop The tag of the desired property
     * @return The value of the property
     */
    public String getProperty(String prop){
        return (String) properties.get(prop);
    }
    
    /**
     * Returns the value associated to the [Missing] tag
     * @return A String that can be parsed into the same format of the data
     */
    public String getMissing(){
        return (String) properties.get("[Missing]");
    }
    
    /**
     * Returns the value associated to the [Format] tag
     * @return A string with the format of the variable
     */
    public String getFormat(){
        return (String) properties.get("[Format]");
    }
    
    /**
     * Returns the value associated to the [Units] tag
     * @return A string with the units of the variable
     */
    public String getUnits(){
        return (String) properties.get("[Units]");
    }
    
    /**
     * Returns the lable associated to a category type
     * @param catToFind The category to find
     * @return A string desciging the category type
     */
    public String getCategory(String catToFind){
        return (String) categories.get(catToFind);
    }
    
    /**
     * Returns the Hashtable of categories for this variable
     * @return A Hashtable with cagegories
     */
    public java.util.Hashtable getCategoriesTable(){
        return categories;
    }
    
    /**
     * Returns the value associated to the [# Rows] tag
     * @return An integer number with the number of Rows
     */
    public int getNumRows(){
        return ((Integer)  properties.get("[# Rows]")).intValue();
    }
    
    /**
     * Returns the value associated to the [# Columns] tag
     * @return An integer number with the number of Columns
     */
    public int getNumCols(){
        return ((Integer)  properties.get("[# Columns]")).intValue();
    }
    
    /**
     * Returns the value associated to the [Southernmost Latitude] tag.  This method
     * uses the {@link hydroScalingAPI.tools.DMSToDegrees} static method
     * @return A double with the suthermost latitude in degrees
     */
    public double getMinLat(){
        return hydroScalingAPI.tools.DMSToDegrees.getDegrees((String) properties.get("[Southernmost Latitude]"));
    }
    
    /**
     * Returns the value associated to the [Westernmost Longitude] tag.  This method
     * uses the {@link hydroScalingAPI.tools.DMSToDegrees} static method
     * @return A double with the Westernmost Longitude in degrees
     */
    public double getMinLon(){
        return hydroScalingAPI.tools.DMSToDegrees.getDegrees((String) properties.get("[Westernmost Longitude]"));
    }
    
    /**
     * Returns the value associated to the [Latitudinal Resolution (ArcSec)] tag
     * @return A double with the latitudinal resolution
     */
    public double getResLat(){
        return ((Double) properties.get("[Latitudinal Resolution (ArcSec)]")).doubleValue();
    }
    
    /**
     * Returns the value associated to the [Longitudinal Resolution (ArcSec)] tag
     * @return A double with the longitudinal resolution
     */
    public double getResLon(){
        return ((Double) properties.get("[Longitudinal Resolution (ArcSec)]")).doubleValue();
    }
    
    /**
     * Calculates and returns the maximum latitude
     * @return A double with the northermost latitude in degrees
     */
    public double getMaxLat(){
        return this.getMinLat()+this.getNumRows()*this.getResLat()/3600.0;
    }
    
    
    /**
     * Calculates and returns the maximum longitude
     * @return A double with the easternrnmost longitude in degrees
     */
    public double getMaxLon(){
        return this.getMinLon()+this.getNumCols()*this.getResLon()/3600.0;
    }
    
    /**
     * Returns the value associated to the [Name] tag
     * @return A string with map name
     */
    public String getName(){
        return (String) properties.get("[Name]");
    }
    
    /**
     * Returns the value associated to the [Temporal Resolution] tag in milliseconds or -1 if "fix"
     * @return A double indicating the time resolution in milliseconds
     */
    public int getTemporalScale(){
        int milliseconsTemporal=0;
        String temporalScale=(String) properties.get("[Temporal Resolution]");
        java.util.StringTokenizer tokensTime=new java.util.StringTokenizer(temporalScale,"-");
        if (temporalScale.indexOf("fix") != -1) milliseconsTemporal=-1;
        if (temporalScale.indexOf("day") != -1) milliseconsTemporal=new Integer(tokensTime.nextToken()).intValue()*24*60*60*1000;
        if (temporalScale.indexOf("hour") != -1) milliseconsTemporal=new Integer(tokensTime.nextToken()).intValue()*60*60*1000;
        if (temporalScale.indexOf("minute") != -1 ) milliseconsTemporal=new Integer(tokensTime.nextToken()).intValue()*60*1000;
        if (temporalScale.indexOf("second") != -1 ) milliseconsTemporal=new Integer(tokensTime.nextToken()).intValue()*1000;
        
        return milliseconsTemporal;
    }
    
    /**
     * Returns a float[][] array with the infomation contained in the binary file
     * @throws java.io.IOException Captures errors while reading the binary file
     * @return A float[numRows][numCols]
     */
    public float[][] getArray() throws java.io.IOException{
        return new hydroScalingAPI.io.DataRaster(this).getFloat();
    }
    
    /**
     * Returns visad.FlatField appropriate for a visad.Display
     * @throws visad.VisADException Captures errors while creating the visad Data object
     * @throws java.io.IOException Captures errors while reading the information
     * @return A visad.FlatField
     */
    public visad.FlatField getField() throws visad.VisADException, java.io.IOException{
        
        hydroScalingAPI.io.DataRaster theData = new hydroScalingAPI.io.DataRaster(this);
        
        float[][] valueAndColor=new float[2][];
        
        if(getNumCols()*getNumRows() < 1e7){
            valueAndColor[0]=(theData.getFloatLine())[0];
            if(locationBinaryFile.getName().lastIndexOf(".horton") == -1){
                valueAndColor[1]=(theData.getFloatLineEqualized())[0];
            } else {
                valueAndColor[1]=(theData.getFloatLine())[0];
                hydroScalingAPI.util.statistics.Stats statColors=new hydroScalingAPI.util.statistics.Stats(valueAndColor[1]);
                for (int i=0;i<valueAndColor[1].length;i++) 
                    valueAndColor[1][i]=240*(valueAndColor[1][i]-statColors.minValue)/(statColors.maxValue-statColors.minValue)+1;
            }

            visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
            visad.RealTupleType range=new visad.RealTupleType(visad.RealType.Altitude,visad.RealType.getRealType("varColor"));
            visad.FunctionType  domainToRangeFunction = new visad.FunctionType( domain, range);
            visad.Linear2DSet   domainExtent = new visad.Linear2DSet(domain,getMinLon()+getResLon()/3600.0/2.0,getMinLon()+getNumCols()*getResLon()/3600.0-getResLon()/3600.0/2.0,getNumCols(),
                                                                            getMinLat()+getResLat()/3600.0/2.0,getMinLat()+getNumRows()*getResLat()/3600.0-getResLat()/3600.0/2.0,getNumRows());
            visad.FlatField theField = new visad.FlatField( domainToRangeFunction, domainExtent);
            theField.setSamples( valueAndColor, false );

            return theField;
        } else {
            
            int xfactor=(int)(getNumCols()/1000.0f);
            int yfactor=(int)(getNumRows()/1000.0f);
            int factor = Math.max(xfactor, yfactor);
            
            System.out.println(">>>>> IS GOING TO RESAMPLE: "+factor);
            
            valueAndColor[0]=(theData.getFloatLineResampled(factor))[0];
            if(locationBinaryFile.getName().lastIndexOf(".horton") == -1){
                valueAndColor[1]=(theData.getFloatLineEqualizedResampled(factor))[0];
            } else {
                valueAndColor[1]=(theData.getFloatLineResampled(factor))[0];
                hydroScalingAPI.util.statistics.Stats statColors=new hydroScalingAPI.util.statistics.Stats(valueAndColor[1]);
                for (int i=0;i<valueAndColor[1].length;i++) 
                    valueAndColor[1][i]=240*(valueAndColor[1][i]-statColors.minValue)/(statColors.maxValue-statColors.minValue)+1;
            }

            visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
            visad.RealTupleType range=new visad.RealTupleType(visad.RealType.Altitude,visad.RealType.getRealType("varColor"));
            visad.FunctionType  domainToRangeFunction = new visad.FunctionType( domain, range);
            visad.Linear2DSet   domainExtent = new visad.Linear2DSet(domain,getMinLon()+getResLon()/3600.0/2.0/factor,getMinLon()+getNumCols()*getResLon()/3600.0-getResLon()/3600.0/2.0/factor,getNumCols()/factor,
                                                                            getMinLat()+getResLat()/3600.0/2.0/factor,getMinLat()+getNumRows()*getResLat()/3600.0-getResLat()/3600.0/2.0/factor,getNumRows()/factor);
            visad.FlatField theField = new visad.FlatField( domainToRangeFunction, domainExtent);
            theField.setSamples( valueAndColor, false );

            return theField;
        }
        
    }
    
    /**
     * Writes the information in the MetaRaster to the specified path
     * @param newMetaLocation The path where the metafile must be writen
     * @throws java.io.IOException Captures errors while writing the information
     */
    public void writeMetaRaster(java.io.File newMetaLocation) throws java.io.IOException{
        
        java.io.BufferedWriter writerMeta = new java.io.BufferedWriter(new java.io.FileWriter(newMetaLocation));
        
        for(int i=0;i<3;i++){
            writerMeta.write(parameters[i]+"\n");
            writerMeta.write((String)properties.get(parameters[i])+"\n");
            writerMeta.write("\n");
        }
        
        for(int i=3;i<5;i++){
            writerMeta.write(parameters[i]+"\n");
            writerMeta.write(((Double)properties.get(parameters[i])).toString()+"\n");
            writerMeta.write("\n");
        }
        
        for(int i=5;i<7;i++){
            writerMeta.write(parameters[i]+"\n");
            writerMeta.write(((Integer)properties.get(parameters[i])).toString()+"\n");
            writerMeta.write("\n");
        }

        for(int i=7;i<12;i++){
            writerMeta.write(parameters[i]+"\n");
            writerMeta.write((String)properties.get(parameters[i])+"\n");
            writerMeta.write("\n");
        }
        
        
        writerMeta.close();
        
    }
    
    /**
     * clones the {@link java.util.Hashtable} that contains the MetaRaster properties
     * @return A {@link java.util.Hashtable}
     */
    public java.util.Hashtable cloneProperties(){
        
        return (java.util.Hashtable) properties.clone();

    }
    
    /**
     * Tests for the class
     * @param args Command line arguments
     */
    public static void main (String args[]) {
        try{
            hydroScalingAPI.io.MetaRaster metaRaster1=new hydroScalingAPI.io.MetaRaster (new java.io.File("/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.metaDEM"));
            
            System.out.println(metaRaster1.getName());
            System.out.println(metaRaster1.getNumRows());
            System.out.println(metaRaster1.getNumCols());
            System.out.println(metaRaster1.getMinLat());
            System.out.println(metaRaster1.getMinLon());
            System.out.println(metaRaster1.getResLat());
            System.out.println(metaRaster1.getResLon());
            System.out.println(metaRaster1.getMaxLat());
            System.out.println(metaRaster1.getMaxLon());
            System.out.println(metaRaster1.getTemporalScale());
            
            System.out.println();
            metaRaster1.setFormat("Double");
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.corrDEM"));
            double[][] datosToPrint=new hydroScalingAPI.io.DataRaster(metaRaster1).getDouble();
            
            java.text.NumberFormat number4 = java.text.NumberFormat.getNumberInstance();
            java.text.DecimalFormat dpoint4 = (java.text.DecimalFormat)number4;
            dpoint4.applyPattern("0000.00000000000000000");
            
            for(int i=2030;i>2020;i--){
                for(int j=430;j<440;j++){
                    System.out.print(dpoint4.format(datosToPrint[i][j])+" ");
                }
                System.out.println();
            }
            
            System.out.println();
            metaRaster1.setFormat("Byte");
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.dir"));
            datosToPrint=new hydroScalingAPI.io.DataRaster(metaRaster1).getDouble();
            
            for(int i=2030;i>2020;i--){
                for(int j=430;j<440;j++){
                    System.out.print(dpoint4.format(datosToPrint[i][j])+" ");
                }
                System.out.println();
            }
            
            //metaRaster1.writeMetaRaster(new java.io.File("/tmp/test.metaDEM"));
            
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }/* catch (visad.VisADException vie){
            System.out.print(vie);
            System.exit(0);
        }*/
        
        System.exit(0);
    }
}
