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
 * GUI_InfoManager.java
 *
 * Created on March 3, 2003, 6:34 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 * This class manages the information needed by the main CUENCAS GUI created by {@link
 * hydroScalingAPI.mainGUI.ParentGUI}
 * @author Ricardo Mantilla
 */
public class GUI_InfoManager {
    
    //Variables that need to be known by everybody
    private String version;
    /**
     * The initial horizontal position for the CUENCAS GUI
     */
    public int gui_Xpos;
    /**
     * The initial vertical position for the CUENCAS GUI
     */
    public int gui_Ypos;
    /**
     * The initial horizontal size for the CUENCAS GUI
     */
    public int gui_Xsize;
    /**
     * The initial vertical size for the CUENCAS GUI
     */
    public int gui_Ysize;
    private String navigator;
    /**
     * The language of the GUI (Currently only English has been implemented)
     */
    public String language;
    private String updates_server;
    /**
     * A {@link java.util.Vector} containing the list of files recently opened by the
     * user
     */
    public java.util.Vector recentFiles;
    /**
     * A {@link java.util.Vector} containing the list of database directories recently
     * opened by the user
     */
    public java.util.Vector recentDataBases;
    
    /**
     * A {@link java.util.Vector} containing the list of files recently created by the
     * user using the MapCalculator
     */
    public java.util.Vector recentCalcMaps;
    
    /**
     * This boolean inticates if a database directory is available
     */
    public boolean dataBaseExists=false;
    /**
     * This boolean inticates if the database directory contains the Rasters/Topography
     * subdirectory
     */
    public boolean dataBaseRastersDemExists=false;
    /**
     * This boolean inticates if the database directory contains the Rasters/Hydrology subdirectory
     */
    public boolean dataBaseRastersHydExists=false;
    /**
     * This boolean inticates if the database directory contains the Vectors subdirectory
     */
    public boolean dataBaseVectorsExists=false;
    /**
     * This boolean inticates if the database directory contains the Polygons subdirectory
     */
    public boolean dataBasePolygonsExists=false;
    /**
     * This boolean inticates if the database directory contains the Lines subdirectory
     */
    public boolean dataBaseLinesExists=false;
    /**
     * This boolean inticates if the database directory contains the Sites/Gauges subdirectory
     */
    public boolean dataBaseSitesGaugesExists=false;
    /**
     * This boolean inticates if the database directory contains the Sites/Locations subdirectory
     */
    public boolean dataBaseSitesLocationsExists=false;
    /**
     * This boolean inticates if the database directory contains the Documentation subdirectory
     */
    public boolean dataBaseDocumentationExists=false;
    /**
     * This boolean inticates if the database directory contains the logo.jpg file. 
     * The file properties are described in the Developer's Guide
     */
    public boolean dataBaseLogoExists=false;
    /**
     * This boolean inticates if the database directory contains the name.txt file. 
     * The file properties are described in the Developer's Guide
     */
    public boolean dataBaseNameExists=false;
    
    /**
     * The path to the database directory
     */
    public java.io.File dataBasePath;
    /**
     * The path to the Rasters/Topography subdirectory in the database
     */
    public java.io.File dataBaseRastersDemPath;
    /**
     * The path to the Rasters/Hydrology subdirectory in the database
     */
    public java.io.File dataBaseRastersHydPath;
    /**
     * The path to the Vectors subdirectory in the database
     */
    public java.io.File dataBaseVectorsPath;
    /**
     * The path to the Polygons subdirectory in the database
     */
    public java.io.File dataBasePolygonsPath;
    /**
     * The path to the Lines subdirectory in the database
     */
    public java.io.File dataBaseLinesPath;
    /**
     * The path to the Sites/Gauges subdirectory in the database
     */
    public java.io.File dataBaseSitesGaugesPath;
    /**
     * The path to the Sites/Locations subdirectory in the database
     */
    public java.io.File dataBaseSitesLocationsPath;
    /**
     * The path to the Documentation subdirectory in the database
     */
    public java.io.File dataBaseDocumentationPath;
    /**
     * The path to the logo.jpg in the database
     */
    public java.io.File dataBaseLogoPath;
    /**
     * The path to the name.txt file in the database
     */
    public java.io.File dataBaseNamePath;
    
    /**
     * The path to the active metaFile (Used by GUI tools)
     */
    public java.io.File metaFileActive;
    
    
    /**
     * The path to the name.txt file in the database
     */
    public String dataBaseName;
    
    //Local variables
    private java.net.URL defaultIniFile;
    private java.io.File iniFile;
    private String[] tagNames={   "[Version]",
                                  "[Main Frame Position]",
                                  "[Main Frame Size]",
                                  "[External Navigator]",
                                  "[Language]",
                                  "[Updates Server]",
                                  "[Recent Files]",
                                  "[Recent Data Bases]",
                                  "[Map Calculator]"                                  
                              };
                          
    private java.util.Vector tagsVector;
    private boolean endOfIniFile=false;
    
    private java.util.Hashtable vectorDataReferences=new java.util.Hashtable();
    
    private float[] red=    { 21, 35, 96, 37,255,237,255,249,150,255,124};
    private float[] green=  {188, 44,255,135,255,146,  0,174,150,255, 49};
    private float[] blue=   {239,211,  0, 13,  0,  9,  0,174,150,155,116};
    
    /**
     * Creates a new instance of GUI_InfoManager
     * @param predifined The path to a pre-existing .cuencas.ini file
     */
    public GUI_InfoManager(java.io.File predifined) {
        
        tagsVector = new java.util.Vector();
        for (int i=0;i<tagNames.length;i++) tagsVector.addElement(tagNames[i]);
        
        /*This method loads the preferences file*/
        loadIniFileInfo();
        /*Given that a recent data base exists this method
         *checks the database structure and creates the necessary
         information for the mainGUI to be builded
         */
        if(predifined != null) recentDataBases.add(0,predifined);
        checkDataBase();
        
    }
    
    /** Creates a new instance of GUI_InfoManager */
    public GUI_InfoManager() {
        this(null);
    }
    
    private void loadIniFileInfo(){
        try{
            
            defaultIniFile=getClass ().getResource ("/hydroScalingAPI/mainGUI/configuration/cuencas.ini");
            java.io.InputStreamReader defaultReader = new java.io.InputStreamReader(defaultIniFile.openStream());
            java.io.BufferedReader buffer=new java.io.BufferedReader(defaultReader);

            iniFile=new java.io.File(System.getProperty("user.home")+"/.cuencas.ini");
            
            if (iniFile.exists()){
                java.io.FileReader reader = new java.io.FileReader(iniFile);
                buffer=new java.io.BufferedReader(reader);
            }
            
            String headTag;

            do{
                do{
                    headTag=buffer.readLine();
                    if (headTag == null) break;
                } while (!headTag.startsWith("["));
                assignProperty(headTag,buffer);
                if (endOfIniFile) break; 
            } while (true);
            buffer.close();
        } catch (java.io.IOException IOE){
            System.err.println("An Error has ocurred while reading hydro.ini");
            System.err.println(IOE);
        }
    }
    
    /**
     * Assigns the value of a property
     * @param tag The tag of the property to be assigned
     */
    public void assignProperty(String tag){
        try{
            assignProperty(tag,null);
        } catch (java.io.IOException IOE){
            System.err.println("An Error has ocurred while assigning property");
            System.err.println(IOE);
        }
    }
    
    /**
     * Assigns the value of a property using information contained in a file
     * @param tag The tag of the property to be assigned
     * @param buffer The file where the property value resides
     * @throws java.io.IOException Captures errors while reading from the file
     */
    public void assignProperty(String tag, java.io.BufferedReader buffer) throws java.io.IOException{
        
        String temp=null;
        int tagPos=tagsVector.indexOf(tag);

        switch (tagPos) {
            case 0:
                temp=buffer.readLine();
                version=temp;
                break;
            case 1:
                temp=buffer.readLine();
                gui_Xpos=new Integer(temp.substring(0,temp.indexOf("x"))).intValue();
                gui_Ypos=new Integer(temp.substring(temp.indexOf("x")+1)).intValue();
                break;
            case 2:
                temp=buffer.readLine();
                gui_Xsize=new Integer(temp.substring(0,temp.indexOf("x"))).intValue();
                gui_Ysize=new Integer(temp.substring(temp.indexOf("x")+1)).intValue();
                break;
            case 3:
                temp=buffer.readLine();
                navigator=temp;
                break;
            case 4:
                temp=buffer.readLine();
                language=temp;
                break;
            case 5:
                temp=buffer.readLine();
                updates_server=temp;
                break;
            case 6:
                recentFiles=new java.util.Vector();
                while ((temp=buffer.readLine()) != null && temp.length() > 0){
                    recentFiles.add(new java.io.File(temp));
                }
                break;
            case 7:
                recentDataBases=new java.util.Vector();
                while ((temp=buffer.readLine()) != null && temp.length() > 0){
                    recentDataBases.add(new java.io.File(temp));
                }
                break;
            case 8:
                recentCalcMaps=new java.util.Vector();
                while ((temp=buffer.readLine()) != null && temp.length() > 0){
                    recentCalcMaps.add(temp.substring(0,temp.indexOf(";")));
                    recentCalcMaps.add(new java.io.File(temp.substring(temp.indexOf(";")+1)));
                }
                break;
        }
        endOfIniFile=temp==null;
    }
    
    /**
     * Checks the directory structure of the CUENCAS database directory to determine if
     * it follows the guidelines established.  See the Developer's Manual for more
     * information
     */
    public void checkDataBase(){
        
        dataBaseExists=recentDataBases.size() > 0;
        
        if (!dataBaseExists) return;
        
        dataBasePath=(java.io.File) recentDataBases.firstElement();
    
        dataBaseRastersDemPath=new java.io.File(dataBasePath.getPath()+"/Rasters/Topography/");
        dataBaseRastersHydPath=new java.io.File(dataBasePath.getPath()+"/Rasters/Hydrology");
        dataBaseVectorsPath=new java.io.File(dataBasePath.getPath()+"/Vectors/");
        dataBasePolygonsPath=new java.io.File(dataBasePath.getPath()+"/Polygons/");
        dataBaseLinesPath=new java.io.File(dataBasePath.getPath()+"/Lines/");
        dataBaseSitesGaugesPath=new java.io.File(dataBasePath.getPath()+"/Sites/Gauges/");
        dataBaseSitesLocationsPath=new java.io.File(dataBasePath.getPath()+"/Sites/Locations/");
        dataBaseDocumentationPath=new java.io.File(dataBasePath.getPath()+"/Documentation/");
        dataBaseLogoPath=new java.io.File(dataBasePath.getPath()+"/logo.jpg");
        dataBaseNamePath=new java.io.File(dataBasePath.getPath()+"/name.txt");
        
        dataBaseRastersDemExists=dataBaseRastersDemPath.exists();
        dataBaseRastersHydExists=dataBaseRastersHydPath.exists();
        dataBaseVectorsExists=dataBaseVectorsPath.exists();
        dataBasePolygonsExists=dataBasePolygonsPath.exists();
        dataBaseLinesExists=dataBaseLinesPath.exists();
        dataBaseSitesGaugesExists=dataBaseSitesGaugesPath.exists();
        dataBaseSitesLocationsExists=dataBaseSitesLocationsPath.exists();
        dataBaseDocumentationExists=dataBaseDocumentationPath.exists();
        dataBaseLogoExists=dataBaseLogoPath.exists();
        dataBaseNameExists=dataBaseNamePath.exists();
    
        if (dataBaseNameExists){
            try{
                java.io.FileReader reader = new java.io.FileReader(dataBaseNamePath);
                java.io.BufferedReader buffer=new java.io.BufferedReader(reader);
                dataBaseName=buffer.readLine();
                buffer.close();
            } catch (java.io.IOException IOE){
                System.err.println("An Error has ocurred while reading name.txt");
                System.err.println(IOE);
            }
        }
        
    }
    
    /**
     * Assigns the frame location and size properties
     * @param newBounds A {@link java.awt.Rectangle} containing the properties of the interface frame
     */
    public void setBounds(java.awt.Rectangle newBounds){
        gui_Xpos=newBounds.x;
        gui_Ypos=newBounds.y;
        gui_Xsize=newBounds.width;
        gui_Ysize=newBounds.height;
    }
    
    /**
     * Returns the visad.DataReferenceImpl associated to a given name from a list of
     * global references.  The references point to Vectors, Polygons and Lines.  These references
     * are stored by the GUI Manager and passed to every internal map panel.
     * @param refName The name of the desired reference
     * @return A visad.DataReferenceImpl ready to be added into a visad.Display
     */
    public visad.DataReferenceImpl getDataReference(String refName){
        if (vectorDataReferences.get(refName) != null) 
            return (visad.DataReferenceImpl)vectorDataReferences.get(refName);
        else
            return null;
    }
    
    /**
     * Return the array of intensities of red color (RGB) for the river Network.
     * @return An array of red intensities
     */
    public float[] getNetworkRed(){
        return red;
    }
    
    /**
     * Return the array of intensities of green color (RGB) for the river Network.
     * @return An array of green intensities
     */
    public float[] getNetworkGreen(){
        return green;
    }
    
    /**
     * Return the array of intensities of blue color (RGB) for the river Network.
     * @return An array of blue intensities
     */
    public float[] getNetworkBlue(){
        return blue;
    }
    
    /**
     * Add a reference to the list of global references.  These references
     * are stored by the GUI Manager and passed to every internal map panel.
     * @param refName A name for the reference
     * @param theRef The visad.DataReferenceImpl
     */
    public void registerDataReference(String refName,visad.DataReferenceImpl theRef){
        vectorDataReferences.put(refName,theRef);
    }
    
    /**
     * Writes File of preferences including list of recently openned files and
     * databases
     */
    public void writePreferences(){
        try{
            
            java.io.FileWriter writer = new java.io.FileWriter(iniFile);
            java.io.BufferedWriter buffer=new java.io.BufferedWriter(writer);
            
            buffer.write(tagNames[0]+"\n");
            buffer.write(version+"\n");
            buffer.write("\n");
            
            buffer.write(tagNames[1]+"\n");
            buffer.write(gui_Xpos+"x"+gui_Ypos+"\n");
            buffer.write("\n");
            
            buffer.write(tagNames[2]+"\n");
            buffer.write(gui_Xsize+"x"+gui_Ysize+"\n");
            buffer.write("\n");
            
            buffer.write(tagNames[3]+"\n");
            buffer.write(navigator+"\n");
            buffer.write("\n");
            
            buffer.write(tagNames[4]+"\n");
            buffer.write(language+"\n");
            buffer.write("\n");
            
            buffer.write(tagNames[5]+"\n");
            buffer.write(updates_server+"\n");
            buffer.write("\n");
            
            buffer.write(tagNames[6]+"\n");
            for (int i=0;i<recentFiles.size();i++) {
                buffer.write(((java.io.File) recentFiles.elementAt(i)).getPath()+"\n");
            }
            buffer.write("\n");
            
            buffer.write(tagNames[7]+"\n");
            for (int i=0;i<recentDataBases.size();i++) {
                buffer.write(((java.io.File) recentDataBases.elementAt(i)).getPath()+"\n");
            }
            buffer.write("\n");
            
            buffer.write(tagNames[8]+"\n");
            for (int i=0;i<recentCalcMaps.size();i+=2) {
                buffer.write(((String) recentCalcMaps.elementAt(i))+";");
                buffer.write(((java.io.File) recentCalcMaps.elementAt(i+1)).getPath()+"\n");
            }
            buffer.write("\n");
            
            buffer.close();
        } catch (java.io.IOException IOE){
            System.err.println("An Error has ocurred while reading hydro.ini");
            System.err.println(IOE);
        }
    }
    
    
    /**
     * Test for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        hydroScalingAPI.mainGUI.objects.GUI_InfoManager aManager=new hydroScalingAPI.mainGUI.objects.GUI_InfoManager();
        aManager.writePreferences();
    }
    
}
