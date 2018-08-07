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
 * LocationsManager.java
 *
 * Created on June 13, 2003, 2:46 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 * This class manages all the information related to Locations in the database.  It
 * runs independent threads that read the information from the Location-type files
 * @author Ricardo Mantilla
 */
public class LocationsManager {
    
    /** Creates a new instance of LocationsManager */
    private hydroScalingAPI.mainGUI.objects.GUI_InfoManager informationManager;
    private hydroScalingAPI.util.database.DataBaseEngine mainDataBase;
    
    private java.util.Hashtable threadsRegistry=new java.util.Hashtable();
    
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
    private String[] locatTagsTypes={   "alpha",
                                        "alpha",
                                        "index",
                                        "alpha",
                                        "alpha",
                                        "numeric",
                                        "numeric",
                                        "numeric",
                                        "object",
                                        "object",
                                        "object"};
    
    /**
     * Creates a new instance of LocationsManager
     * @param infoManager The {@link hydroScalingAPI.mainGUI.objects.GUI_InfoManager} to which the
     * GaugesManager is associated
     * @param dataBase The {@link hydroScalingAPI.util.database.DataBaseEngine} that stores and manages
     * the data
     */
    public LocationsManager(hydroScalingAPI.mainGUI.objects.GUI_InfoManager infoManager,hydroScalingAPI.util.database.DataBaseEngine dataBase) {
        informationManager=infoManager;
        mainDataBase=dataBase;
        mainDataBase.addTable("Locations",locatTags,locatTagsTypes);
        LocationsLoader loaderDigerThread=new LocationsLoader(informationManager.dataBaseSitesLocationsPath,this);
        loaderDigerThread.start();
    }
    
    /**
     * Pushes data into the {@link hydroScalingAPI.util.database.DataBaseEngine}
     * @param thisRegister The register to be added to the database
     */
    public void addData(Object[] thisRegister){
        String registerIndex=(String)thisRegister[2]+"."+(String)thisRegister[0];
        mainDataBase.addData("Locations",registerIndex,thisRegister);
    }
    
    /**
     * Retereives a particual Location from the {@link
     * hydroScalingAPI.util.database.DataBaseEngine}. Note that the code and the type
     * uniquely characterize a Location.
     * @param code The code of the location to retreive
     * @param type The type of the location to retreive
     * @return A {@link hydroScalingAPI.io.MetaLocation}
     */
    public hydroScalingAPI.io.MetaLocation getLocation(String code, String type) {
        hydroScalingAPI.util.database.DB_Register matchedValue = mainDataBase.getRegisterByMainIndex("Locations",code+"."+type);
        
        if (matchedValue != null) {
            hydroScalingAPI.io.MetaLocation locationResult=new hydroScalingAPI.io.MetaLocation(matchedValue);
            return locationResult;
        }
        
        return null;
        
    }
    
    /**
     * Retereives a group of locations from the {@link
     * hydroScalingAPI.util.database.DataBaseEngine}. Note that the several types
     * of Location can be associated to a given code.
     * @param code The code of the locations to retreive
     * @return A array of {@link hydroScalingAPI.io.MetaLocation}s
     */
    public hydroScalingAPI.io.MetaLocation[] getLocation(String code) {
        java.util.Vector locationsResultVector=new java.util.Vector();
        java.util.Vector localTypes=getTypes();
        int numTypes=localTypes.size();
        hydroScalingAPI.util.database.DB_Register[] matchedValues=new hydroScalingAPI.util.database.DB_Register[numTypes];
        for (int i=0;i<numTypes;i++){
            matchedValues[i] = mainDataBase.getRegisterByMainIndex("Locations",code+"."+(String)localTypes.get(i));
            if (matchedValues[i] != null) locationsResultVector.add(new hydroScalingAPI.io.MetaLocation(matchedValues[i]));
        }
        if (locationsResultVector.size() == 0) return null;
        hydroScalingAPI.io.MetaLocation[] locationsResult=new hydroScalingAPI.io.MetaLocation[locationsResultVector.size()];
        for (int i=0;i<locationsResultVector.size();i++) locationsResult[i]=(hydroScalingAPI.io.MetaLocation)locationsResultVector.get(i);
        return locationsResult;
        
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for Locations
     * that meet a specified group of conditions
     * @param categories The categories to use in the search
     * @param values The value assiciated to the category
     * @param conditions The condition to match (e.g. equal to, greater than, smaller than, etc)
     * @return The group of {@link hydroScalingAPI.util.database.DB_Register} that match the
     * query
     */
    public hydroScalingAPI.io.MetaLocation[] findLocations(java.util.Vector categories,java.util.Vector values,java.util.Vector conditions) {
        String[] cats=new String[categories.size()];
        Object[] vals=new Object[values.size()];
        int[] conds=new int[conditions.size()];
        
        for (int i=0;i<cats.length;i++){
            cats[i]=(String)categories.get(i);
            vals[i]=values.get(i);
            conds[i]=((Integer)conditions.get(i)).intValue();
        }
        
        hydroScalingAPI.util.database.DB_Register[] matchedValues = mainDataBase.compareFieldsTo("Locations",cats,vals,conds);
        if (matchedValues == null) return null;
        
        hydroScalingAPI.io.MetaLocation[] foundLocations=new hydroScalingAPI.io.MetaLocation[matchedValues.length];
        
        for (int i=0;i<matchedValues.length;i++) foundLocations[i]=new hydroScalingAPI.io.MetaLocation(matchedValues[i]);

        java.util.Arrays.sort(foundLocations);
        return foundLocations;
        
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Location names
     * @return A {@link java.util.Vector} listing unique names
     */
    public java.util.Vector getNames(){
        return mainDataBase.getUniqueValues("Locations",locatTags[2]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Location agencies
     * @return A {@link java.util.Vector} listing unique agencies
     */
    public java.util.Vector getAgencies(){
        return mainDataBase.getUniqueValues("Locations",locatTags[1]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Location types
     * @return A {@link java.util.Vector} listing unique types
     */
    public java.util.Vector getTypes(){
        return mainDataBase.getUniqueValues("Locations",locatTags[0]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Location counties
     * @return A {@link java.util.Vector} listing unique counties
     */
    public java.util.Vector getCounties(){
        return mainDataBase.getUniqueValues("Locations",locatTags[3]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Location states
     * @return A {@link java.util.Vector} listing unique states
     */
    public java.util.Vector getStates(){
        return mainDataBase.getUniqueValues("Locations",locatTags[4]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for the
     * southernmost latitude of the locations in the CUENCAS database
     * @return A Double
     */
    public Double getSouthernmostLat(){
        return mainDataBase.getMinValue("Locations",locatTags[5]);
    }
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for the
     * northernmost latitude of the locations in the CUENCAS database
     * @return A Double
     */
    public Double getNorthernmostLat(){
        return mainDataBase.getMaxValue("Locations",locatTags[5]);
    }
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for the
     * westernmost longitude of the locations in the CUENCAS database
     * @return A Double
     */
    public Double getWesternmostLat(){
        return mainDataBase.getMinValue("Locations",locatTags[6]);
    }
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for the
     * easternmost longitude of the locations in the CUENCAS database
     * @return A Double
     */
    public Double getEasternmostLat(){
        return mainDataBase.getMaxValue("Locations",locatTags[6]);
    }
    
    /**
     * Updates the status of a Thread.  Notice that each Location file is read by an
     * independent Thread.
     * @param threadName The Thread name to be checked
     * @param status the 
     */
    public void threadReport(String threadName,String status){
        threadsRegistry.put(threadName,status);
    }
    
    /**
     * Indcates if the LocationsManager has completed loading infomation
     * @return A boolean indicating the status of the data load process
     */
    public boolean isLoaded(){
        
        if ((threadsRegistry.size() != 0) && !threadsRegistry.containsValue("working")){
            threadsRegistry=null;
            return true;
        } else {
            return false;
        }
    }
    
    /**
     * Test for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        LocationsManager GM=new LocationsManager(new hydroScalingAPI.mainGUI.objects.GUI_InfoManager(),new hydroScalingAPI.util.database.DataBaseEngine());
        while(!GM.isLoaded()){
            new visad.util.Delay(1000);
            System.out.print("/");
        }
        System.out.println("/");
        java.util.Vector theVector=GM.getTypes();
        for (int i=0;i<theVector.size();i++) System.out.println(theVector.get(i));
        theVector=GM.getStates();
        for (int i=0;i<theVector.size();i++) System.out.println(theVector.get(i));
    }
    
}

class LocationsLoader extends Thread {
    
    private hydroScalingAPI.mainGUI.objects.LocationsManager threadManager;
    
    private java.io.File locationsMainDirectory;
    private hydroScalingAPI.util.fileUtilities.DirFilter directoriesFinder=new hydroScalingAPI.util.fileUtilities.DirFilter();
    private hydroScalingAPI.util.fileUtilities.DotFilter locationFileFinder=new hydroScalingAPI.util.fileUtilities.DotFilter("gz");
    
    public LocationsLoader(java.io.File directoryToDig, hydroScalingAPI.mainGUI.objects.LocationsManager theManager) {
        
        locationsMainDirectory=directoryToDig;
        threadManager=theManager;
        
    }
    
    private void loadData(java.io.File directoryToDig){
        java.io.File[] subDirectories=directoryToDig.listFiles(directoriesFinder);
        for (int i=0;i<subDirectories.length;i++){
            loadData(subDirectories[i]);
        }
        java.io.File[] filesToAdd=directoryToDig.listFiles(locationFileFinder);
        for (int i=0;i<filesToAdd.length;i++){
            //new LocationDataLoader(filesToAdd[i],threadManager).start();
            try{
                Object[] thisRegister=new hydroScalingAPI.io.LocationReader(filesToAdd[i]).getRegisterForDataBase();
                threadManager.addData(thisRegister);
            }catch(java.io.IOException IOE){
                System.err.println("Failed trying to load File "+filesToAdd[i]);
                System.err.println(IOE);
            }
        }
    }
    
    public void run() {
        threadManager.threadReport("dataDigger","working");
        loadData(locationsMainDirectory);
        threadManager.threadReport("dataDigger","done");
    }
    
}

class LocationDataLoader extends Thread {
    
    private hydroScalingAPI.mainGUI.objects.LocationsManager threadManager;
    private java.io.File fileToLoad;
    
    public LocationDataLoader(java.io.File file,hydroScalingAPI.mainGUI.objects.LocationsManager theManager) {
        
        threadManager=theManager;
        fileToLoad=file;
        
    }
    
    public void run() {
        
        try{
            threadManager.threadReport(fileToLoad.getName(),"working");
            Object[] thisRegister=new hydroScalingAPI.io.LocationReader(fileToLoad).getRegisterForDataBase();
            threadManager.addData(thisRegister);
            threadManager.threadReport(fileToLoad.getName(),"done");
        }catch(java.io.IOException IOE){
            threadManager.threadReport(fileToLoad.getName(),"failed");
            System.err.println("Failed trying to load File "+fileToLoad);
            System.err.println(IOE);
        }
        
        return;
    }
    
}
