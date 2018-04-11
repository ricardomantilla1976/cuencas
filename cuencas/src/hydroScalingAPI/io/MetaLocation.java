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
 * Manages the Gauge-type data object
 * @author Ricardo Mantilla
 */
public class MetaLocation extends Object implements Comparable{
    
    private hydroScalingAPI.util.database.DB_Register locationRegister;
    private String[] locatTags={"[type]",
                                "[source]",
                                "[site name]",
                                "[county]",
                                "[state]",
                                "[latitude (deg:min:sec)]",
                                "[longitude (deg:min:sec)]",
                                "[altitude ASL (m)]",
                                "[images]",
                                "[information]",
                                "[file location]"};
    
    /**
     * Creates a new instance of MetaLocation
     * @param register Takes in a registry created by the {@link hydroScalingAPI.io.LocationReader}
     */
    public MetaLocation(hydroScalingAPI.util.database.DB_Register register) {
        locationRegister=register;
    }
    
    /**
     * Returns the desired property.  Available properties are:
     * <blockquote>
     * <p>[type]</p>
     * <p>[site name]</p>
     * <p>[stream name]</p>
     * <p>[county]</p>
     * <p>[state]</p>
     * <p>[data source]</p>
     * <p>[latitude (deg:min:sec)]</p>
     * <p>[longitude (deg:min:sec)]</p>
     * <p>[altitude ASL (m)]</p>
     * <p>[images]</p>
     * <p>[information]</p>
     * </blockquote>
     * Note: the brackets must be included.
     * @param key The desired property
     * @return An object that contains the information requested
     */
    public Object getProperty(String key){
        return locationRegister.getProperty(key);
    }
    
    /**
     * A String describing the Location
     * @return A String describing the location
     */
    public String toString(){
        return (String)locationRegister.getProperty("[site name]")+" - "+(String)locationRegister.getProperty("[type]");
    }
    
    /**
     * The comparison criteria
     * @param obj The object to compare to
     * @return A negative integer, zero, or a positive integer as this object is less than, equal
     * to, or greater than the specified object.
     */
    public int compareTo(Object obj) {
        return (this.toString()).compareToIgnoreCase(obj.toString());
    }
    
    /**
     * Provides additional information associated with the location
     * @return A String with information describing the location
     */
    public String getInformation(){
        return (String)locationRegister.getProperty("[information]");
    }
    
    /**
     * True if it has associated images
     * @return A boolean indication whether there are images associated with the location
     */
    public boolean hasImages(){
        String locationPath=((java.io.File)locationRegister.getProperty("[file location]")).getParent();
        java.util.Vector imagesVector=(java.util.Vector)locationRegister.getProperty("[images]");
        
        if (((String)imagesVector.get(0)).equalsIgnoreCase("n/a")) return false;
        
        return true;
    }
    
    /**
     * Returns and array of {@link java.util.Vector}.  Each Vector in the array has two
     * elements.  The first element is the path or URL to the image and the second
     * element is a description of the image.
     * @return An array of Vectors with information describing the image
     */
    public java.util.Vector[] getImagesList(){
        String locationPath=((java.io.File)locationRegister.getProperty("[file location]")).getParent();
        java.util.Vector imagesVector=(java.util.Vector)locationRegister.getProperty("[images]");
        
        if (((String)imagesVector.get(0)).equalsIgnoreCase("n/a")) return null;
        
        java.util.Vector[] imagesNameAndInfo={new java.util.Vector(),new java.util.Vector()};
        
        java.util.StringTokenizer tokens;
        
        for (int i=0;i<imagesVector.size();i++){
            String pictureNameAndInfo=(String)imagesVector.get(i);
            tokens=new java.util.StringTokenizer(pictureNameAndInfo,";");
            String thisPictureName=tokens.nextToken().trim();
            String thisPictureInfo=tokens.nextToken().trim();
            if(thisPictureName.indexOf("http://") == -1){
                imagesNameAndInfo[0].add(locationPath+"/"+thisPictureName);
            } else {
                imagesNameAndInfo[0].add(thisPictureName);
            }
            imagesNameAndInfo[1].add(thisPictureInfo);
        }
        return imagesNameAndInfo;
    }
    
    /**
     * Returns an {@link visad.RealTuple}
     * @throws visad.VisADException Captures VisAD Exceptions
     * @throws java.rmi.RemoteException Captures Remote Exceptions
     * @return A visad Data type
     */
    public visad.RealTuple getPositionTuple() throws visad.VisADException, java.rmi.RemoteException{
        double xx=((Double)locationRegister.getProperty("[longitude (deg:min:sec)]")).doubleValue();
        double yy=((Double)locationRegister.getProperty("[latitude (deg:min:sec)]")).doubleValue();
        visad.Real[] rtd1 = {new visad.Real(visad.RealType.Longitude, xx),
                             new visad.Real(visad.RealType.Latitude,  yy)};
        return new visad.RealTuple(rtd1);
    }
    
    /**
     * Returns an {@link visad.Tuple}
     * @return Returns a visad Data object indicating a location and text associated to it
     * @throws visad.VisADException Captures Visad Exceptions
     * @throws java.rmi.RemoteException Captures Remote Exceptions
     */
    public visad.Tuple getTextTuple()  throws visad.VisADException, java.rmi.RemoteException{
        visad.TextType t = visad.TextType.getTextType("text");
        double xx=((Double)locationRegister.getProperty("[longitude (deg:min:sec)]")).doubleValue();
        double yy=((Double)locationRegister.getProperty("[latitude (deg:min:sec)]")).doubleValue();
        visad.Data[] rtd1 = {new visad.Real(visad.RealType.Longitude, xx),
                             new visad.Real(visad.RealType.Latitude,  yy),
                             new visad.Text(t, (String)locationRegister.getProperty("[site name]"))};
        return new visad.Tuple(rtd1);
    }
    
    
}
