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
 * GaugesManager.java
 *
 * Created on June 13, 2003, 2:28 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 * This class manages all the information related to Gauges in the database.  It
 * runs independent threads that read the information from the Gauge-type files
 * @author Ricardo Mantilla
 */
public class GaugesManager {
    
    private hydroScalingAPI.mainGUI.objects.GUI_InfoManager informationManager;
    private hydroScalingAPI.util.database.DataBaseEngine mainDataBase;
    
    private java.util.Hashtable threadsRegistry=new java.util.Hashtable();
    
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
    
    private String[] gaugeTagsTypes={   "index",
                                        "alpha",
                                        "alpha",
                                        "object",
                                        "alpha",
                                        "alpha",
                                        "alpha",
                                        "object",
                                        "numeric",
                                        "numeric",
                                        "numeric",
                                        "numeric",
                                        "object",
                                        "object",
                                        "object"};
    
    /**
     * Creates a new instance of GaugesManager
     * @param infoManager The {@link hydroScalingAPI.mainGUI.objects.GUI_InfoManager} to which the
     * GaugesManager is associated
     * @param dataBase The {@link hydroScalingAPI.util.database.DataBaseEngine} that stores and manages
     * the data
     */
    public GaugesManager(hydroScalingAPI.mainGUI.objects.GUI_InfoManager infoManager,hydroScalingAPI.util.database.DataBaseEngine dataBase) {
        informationManager=infoManager;
        mainDataBase=dataBase;
        mainDataBase.addTable("Gauges",gaugeTags,gaugeTagsTypes);
        GaugesLoader loaderDigerThread=new GaugesLoader(informationManager.dataBaseSitesGaugesPath,this);
        loaderDigerThread.start();
    }
    
    /**
     * Pushes data into the {@link hydroScalingAPI.util.database.DataBaseEngine}
     * @param thisRegister The register to be added to the database
     */
    public void addData(Object[] thisRegister){
        String registerIndex=(String)thisRegister[0]+"."+(String)thisRegister[2];
        mainDataBase.addData("Gauges",registerIndex,thisRegister);
    }
    
    /**
     * Retereives a particual Gauge from the {@link
     * hydroScalingAPI.util.database.DataBaseEngine}. Note that the code and the type
     * uniquely characterize a Gauge.
     * @param code The code of the gauge to retreive
     * @param type The type of the gauge to retreive
     * @return A {@link hydroScalingAPI.io.MetaGauge}
     */
    public hydroScalingAPI.io.MetaGauge getGauge(String code, String type) {
        hydroScalingAPI.util.database.DB_Register matchedValue = mainDataBase.getRegisterByMainIndex("Gauges",code+"."+type);
        
        if (matchedValue != null) {
            hydroScalingAPI.io.MetaGauge gaugeResult=new hydroScalingAPI.io.MetaGauge(matchedValue);
            return gaugeResult;
        }
        
        return null;
        
    }
    
    /**
     * Retereives a group of gauges from the {@link
     * hydroScalingAPI.util.database.DataBaseEngine}. Note that the several types
     * of Gauge can be associated to a given code.
     * @param code The code of the gauges to retreive
     * @return A array of {@link hydroScalingAPI.io.MetaGauge}s
     */
    public hydroScalingAPI.io.MetaGauge[] getGauge(String code) {
        java.util.Vector gaugesResultVector=new java.util.Vector();
        java.util.Vector localTypes=getTypes();
        int numTypes=localTypes.size();
        hydroScalingAPI.util.database.DB_Register[] matchedValues=new hydroScalingAPI.util.database.DB_Register[numTypes];
        for (int i=0;i<numTypes;i++){
            matchedValues[i] = mainDataBase.getRegisterByMainIndex("Gauges",code+"."+(String)localTypes.get(i));
            if (matchedValues[i] != null) gaugesResultVector.add(new hydroScalingAPI.io.MetaGauge(matchedValues[i]));
        }
        if (gaugesResultVector.size() == 0) return null;
        hydroScalingAPI.io.MetaGauge[] gaugesResult=new hydroScalingAPI.io.MetaGauge[gaugesResultVector.size()];
        for (int i=0;i<gaugesResultVector.size();i++) gaugesResult[i]=(hydroScalingAPI.io.MetaGauge)gaugesResultVector.get(i);
        return gaugesResult;
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for Gauges
     * that meet a specified group of conditions
     * @param categories The categories to use in the search
     * @param values The value assiciated to the category
     * @param conditions The condition to match (e.g. equal to, greater than, smaller than, etc)
     * @return The group of {@link hydroScalingAPI.util.database.DB_Register} that match the
     * query
     */
    public hydroScalingAPI.io.MetaGauge[] findGauges(java.util.Vector categories,java.util.Vector values,java.util.Vector conditions) {
        String[] cats=new String[categories.size()];
        Object[] vals=new Object[values.size()];
        int[] conds=new int[conditions.size()];
        
        for (int i=0;i<cats.length;i++){
            cats[i]=(String)categories.get(i);
            vals[i]=values.get(i);
            conds[i]=((Integer)conditions.get(i)).intValue();
        }
        
        hydroScalingAPI.util.database.DB_Register[] matchedValues = mainDataBase.compareFieldsTo("Gauges",cats,vals,conds);
        if (matchedValues == null) return null;
        
        hydroScalingAPI.io.MetaGauge[] foundGauges=new hydroScalingAPI.io.MetaGauge[matchedValues.length];
        
        for (int i=0;i<matchedValues.length;i++) foundGauges[i]=new hydroScalingAPI.io.MetaGauge(matchedValues[i]);

        java.util.Arrays.sort(foundGauges);
        return foundGauges;
        
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Gauge codes
     * @return A {@link java.util.Vector} listing unique codes
     */
    public java.util.Vector getCodes(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[0]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Gauge agencies
     * @return A {@link java.util.Vector} listing unique agencies
     */
    public java.util.Vector getAgencies(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[1]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Gauge Types
     * @return A {@link java.util.Vector} listing unique types
     */
    public java.util.Vector getTypes(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[2]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Gauge associated streams
     * @return A {@link java.util.Vector} listing unique streams
     */
    public java.util.Vector getStreams(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[4]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Gauge county
     * @return A {@link java.util.Vector} listing unique counties
     */
    public java.util.Vector getCounties(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[5]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for unique
     * Gauge state
     * @return A {@link java.util.Vector} listing unique states
     */
    public java.util.Vector getStates(){
        return mainDataBase.getUniqueValues("Gauges",gaugeTags[6]);
    }
    
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for the
     * southernmost latitude of the gauges in the CUENCAS database
     * @return A Double
     */
    public Double getSouthernmostLat(){
        return mainDataBase.getMinValue("Gauges",gaugeTags[8]);
    }
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for the
     * northernmost latitude of the gauges in the CUENCAS database
     * @return A Double
     */
    public Double getNorthernmostLat(){
        return mainDataBase.getMaxValue("Gauges",gaugeTags[8]);
    }
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for the
     * westernmost longitude of the gauges in the CUENCAS database
     * @return A Double
     */
    public Double getWesternmostLat(){
        return mainDataBase.getMinValue("Gauges",gaugeTags[9]);
    }
    /**
     * Query to the {@link hydroScalingAPI.util.database.DataBaseEngine} for the
     * easternmost longitude of the gauges in the CUENCAS database
     * @return A Double
     */
    public Double getEasternmostLat(){
        return mainDataBase.getMaxValue("Gauges",gaugeTags[9]);
    }
    
    /**
     * Updates the status of a Thread.  Notice that each Gauge file is read by an
     * independent Thread.
     * @param threadName The Thread name to be checked
     * @param status the 
     */
    public void threadReport(String threadName,String status){
        threadsRegistry.put(threadName,status);
    }
    
    /**
     * Indcates if the GaugesManager has completed loading infomation
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
        
        GaugesManager GM=new GaugesManager(new hydroScalingAPI.mainGUI.objects.GUI_InfoManager(),new hydroScalingAPI.util.database.DataBaseEngine());
        while(!GM.isLoaded()){
            new visad.util.Delay(5000);
            System.out.print("/");
        }
        System.out.println("/");
        java.util.Vector theVector=GM.getCodes();
        for (int i=0;i<theVector.size();i++) System.out.println(theVector.get(i));
        theVector=GM.getStates();
        for (int i=0;i<theVector.size();i++) System.out.println(theVector.get(i));
    }
    
}

class GaugesLoader extends Thread {
    
    private hydroScalingAPI.mainGUI.objects.GaugesManager threadManager;
    
    private java.io.File gaugesMainDirectory;
    private hydroScalingAPI.util.fileUtilities.DirFilter directoriesFinder=new hydroScalingAPI.util.fileUtilities.DirFilter();
    private hydroScalingAPI.util.fileUtilities.DotFilter gaugeFileFinder=new hydroScalingAPI.util.fileUtilities.DotFilter("gz");
    
    public GaugesLoader(java.io.File directoryToDig, hydroScalingAPI.mainGUI.objects.GaugesManager theManager) {
        
        gaugesMainDirectory=directoryToDig;
        threadManager=theManager;
        
    }
    
    private void loadData(java.io.File directoryToDig){
        java.io.File[] subDirectories=directoryToDig.listFiles(directoriesFinder);
        for (int i=0;i<subDirectories.length;i++){
            loadData(subDirectories[i]);
        }
        java.io.File[] filesToAdd=directoryToDig.listFiles(gaugeFileFinder);
        
        for (int i=0;i<filesToAdd.length;i++){
            //new GaugeDataLoader(filesToAdd[i],threadManager).start();
            try{
                Object[] thisRegister=new hydroScalingAPI.io.GaugeReader(filesToAdd[i]).getRegisterForDataBase();
                threadManager.addData(thisRegister);
            }catch(java.io.IOException IOE){
                System.err.println("Failed trying to load File "+filesToAdd[i]);
                System.err.println(IOE);
            }
        }
    }
    
    public void run() {
        threadManager.threadReport("dataDigger","working");
        loadData(gaugesMainDirectory);
        threadManager.threadReport("dataDigger","done");
    }
    
}

class GaugeDataLoader extends Thread {
    
    private hydroScalingAPI.mainGUI.objects.GaugesManager threadManager;
    private java.io.File fileToLoad;
    
    public GaugeDataLoader(java.io.File file,hydroScalingAPI.mainGUI.objects.GaugesManager theManager) {
        
        threadManager=theManager;
        fileToLoad=file;
        
    }
    
    public void run() {
        
        try{
            threadManager.threadReport(fileToLoad.getName(),"working");
            Object[] thisRegister=new hydroScalingAPI.io.GaugeReader(fileToLoad).getRegisterForDataBase();
            threadManager.addData(thisRegister);
            threadManager.threadReport(fileToLoad.getName(),"done");
        }catch(java.io.IOException IOE){
            threadManager.threadReport(fileToLoad.getName(),"failed");
            System.err.println("Failed trying to load File "+fileToLoad);
            IOE.printStackTrace();
        }
        
        return;
    }
    
}
