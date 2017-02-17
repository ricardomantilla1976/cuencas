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
 * parentGUI.java
 *
 * Created on March 3, 2003, 4:31 PM
 */

package hydroScalingAPI.mainGUI;

import java.io.IOException;
import java.rmi.RemoteException;
import visad.VisADException;

/**
 * This class creates the main Graphical User Iterface to the GIS component of
 * CUENCAS
 * @author Ricardo Mantilla
 */

public class ParentGUI extends javax.swing.JFrame implements javax.swing.event.InternalFrameListener{
    
    private java.util.Hashtable openWindows=new java.util.Hashtable();
    private java.util.Hashtable openWindowsMenusVector=new java.util.Hashtable();
    private java.util.Vector openWindowsIdentifiers=new java.util.Vector();
    
    private hydroScalingAPI.mainGUI.objects.GUI_InfoManager localInfoManager;
    private hydroScalingAPI.util.database.DataBaseEngine localDatabaseEngine;
    private hydroScalingAPI.mainGUI.objects.GaugesManager localGaugesManager;
    private hydroScalingAPI.mainGUI.objects.LocationsManager localLocationsManager;
    
    
    /** Creates new form parentGUI */
    public ParentGUI() {
        this(new hydroScalingAPI.mainGUI.objects.GUI_InfoManager());
    }
    
    /**
     * Creates new form parentGUI using a given GUI_InfoManager to manage the GUI
     * parameters
     * @param gui_info A {@link hydroScalingAPI.mainGUI.objects.GUI_InfoManager}
     */
    public ParentGUI(hydroScalingAPI.mainGUI.objects.GUI_InfoManager gui_info) {
        localInfoManager=gui_info;
        initComponents();
        setUpGUI(false);
    }
    
    /**
     * Updates the Database name label
     * @param label The new name for the database
     */
    public void updateDBNameLabel(String label){
        localInfoManager.dataBaseName=label;
        dbName.setText(label);
    }
    
    /**
     * Checks the information in the database (like directories, files, etc)
     * and updates the GUI
     * @param justUpdate A boolean indicating if the GUI needs to be updated (true) or created for the
     * first time (false)
     */
    public void setUpGUI(boolean justUpdate){
        
        if(!justUpdate){
            /*GUI Size*/
            setBounds(  localInfoManager.gui_Xpos,
                    localInfoManager.gui_Ypos,
                    localInfoManager.gui_Xsize,
                    localInfoManager.gui_Ysize);
            splitPane.setDividerLocation(250);
            
            /*Bulilds the recent files and recent DB menus*/
            bulidRecentFilesMenu();
            
            /*Removes leftovers from previously loaded databases*/
            activeGaugesContainer.setListData(new java.util.Vector());
            activeLocationsContainer.setListData(new java.util.Vector());
        }
        
        /*JTrees for Raster data - Topography and Hydrology*/
        javax.swing.tree.DefaultMutableTreeNode topoTreeModel=new javax.swing.tree.DefaultMutableTreeNode("Elevation Maps");
        topoTree.setModel(new javax.swing.tree.DefaultTreeModel(topoTreeModel));
        
        javax.swing.tree.DefaultMutableTreeNode hydroTreeModel=new javax.swing.tree.DefaultMutableTreeNode("Hydrological Fields");
        hydroTree.setModel(new javax.swing.tree.DefaultTreeModel(hydroTreeModel));
        
        /*If a Data Base exists use it to complete the interface*/
        if (localInfoManager.dataBaseExists){
            
            dbName.setText(localInfoManager.dataBaseName);
            
            /*Raster Files Trees*/
            if (localInfoManager.dataBaseRastersDemExists){
                new hydroScalingAPI.mainGUI.objects.BuildFilesTree(topoTreeModel,localInfoManager.dataBaseRastersDemPath,new hydroScalingAPI.util.fileUtilities.DotFilter("metaDEM")).start();
            }
            
            if (localInfoManager.dataBaseRastersHydExists){
                new hydroScalingAPI.mainGUI.objects.BuildFilesTree(hydroTreeModel,localInfoManager.dataBaseRastersHydPath,new hydroScalingAPI.util.fileUtilities.DotFilter("metaVHC")).start();
            }
            
            if(!justUpdate){
                /*Strats up the DataBase Engine*/
                localDatabaseEngine=new hydroScalingAPI.util.database.DataBaseEngine();
                
                /*Gauges and Locations*/
                GaugesPanelEnabled(false);
                if (localInfoManager.dataBaseSitesGaugesExists){
                    localGaugesManager=new hydroScalingAPI.mainGUI.objects.GaugesManager(localInfoManager,localDatabaseEngine);
                    new hydroScalingAPI.mainGUI.objects.GaugesGUI_monitor(this).start();
                } else {
                    localGaugesManager=null;
                }
                
                LocationsPanelEnabled(false);
                if (localInfoManager.dataBaseSitesLocationsExists){
                    localLocationsManager=new hydroScalingAPI.mainGUI.objects.LocationsManager(localInfoManager,localDatabaseEngine);
                    new hydroScalingAPI.mainGUI.objects.LocationsGUI_monitor(this).start();
                } else {
                    localLocationsManager=null;
                }
            }
            
            /*Vectors - Shapes and DLGs*/
            shapesContainer.removeAll();
            dlgsContainer.removeAll();
            if (localInfoManager.dataBaseVectorsExists){
                buildShapesAndDlgs();
            } else {
                localLocationsManager=null;
            }
            
            /*Polygons*/
            polygonContainer.removeAll();
            if (localInfoManager.dataBasePolygonsExists){
                buildPolygons();
            } else {
                localLocationsManager=null;
            }
            
        }
        
    }
    
    private void buildPolygons(){
        
        java.io.File[] polyFiles=localInfoManager.dataBasePolygonsPath.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("poly"));
        java.util.Arrays.sort(polyFiles);
        for (int i=0;i<polyFiles.length;i++){
            javax.swing.JCheckBox showPolyFile = new javax.swing.JCheckBox();
            showPolyFile.setFont(new java.awt.Font("Dialog", 0, 10));
            showPolyFile.setSelected(false);
            showPolyFile.setText(polyFiles[i].getName());
            showPolyFile.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showPolygonActionPerformed(evt);
                }
            });
            
            polygonContainer.add(showPolyFile);
        }
        
    }
    
    private void buildShapesAndDlgs(){
        
        java.io.File[] shapeFiles=localInfoManager.dataBaseVectorsPath.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("shp"));
        java.util.Arrays.sort(shapeFiles);
        for (int i=0;i<shapeFiles.length;i++){
            javax.swing.JCheckBox showShapeFile = new javax.swing.JCheckBox();
            showShapeFile.setFont(new java.awt.Font("Dialog", 0, 10));
            showShapeFile.setSelected(false);
            showShapeFile.setText(shapeFiles[i].getName());
            showShapeFile.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showVectorActionPerformed(evt);
                }
            });
            
            shapesContainer.add(showShapeFile);
        }
        
        java.io.File[] dlgFiles=localInfoManager.dataBaseVectorsPath.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("dlg,opt"));
        java.util.Arrays.sort(dlgFiles);
        for (int i=0;i<dlgFiles.length;i++){
            javax.swing.JCheckBox showDlgFile = new javax.swing.JCheckBox();
            showDlgFile.setFont(new java.awt.Font("Dialog", 0, 10));
            showDlgFile.setSelected(false);
            showDlgFile.setText(dlgFiles[i].getName());
            showDlgFile.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showVectorActionPerformed(evt);
                }
            });
            
            dlgsContainer.add(showDlgFile);
        }
        
    }
    
    private void bulidRecentFilesMenu(){
        
        if (localInfoManager.recentFiles.size() >0){
            jMenuRecentFiles.removeAll();
            for (int i=0;i<localInfoManager.recentFiles.size();i++){
                javax.swing.JMenuItem fileElement = new javax.swing.JMenuItem();
                fileElement.setFont(new java.awt.Font("Dialog", 0, 10));
                fileElement.setText(i+". "+((java.io.File) localInfoManager.recentFiles.elementAt(i)).getName());
                fileElement.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        openRecentFile(evt);
                    }
                }
                );
                
                jMenuRecentFiles.add(fileElement);
            }
        }
        
        if (localInfoManager.recentDataBases.size() > 0){
            jMenuRecentDB.removeAll();
            for (int i=0;i<localInfoManager.recentDataBases.size();i++){
                javax.swing.JMenuItem fileElement = new javax.swing.JMenuItem();
                fileElement.setFont(new java.awt.Font("Dialog", 0, 10));
                fileElement.setText(i+". "+((java.io.File) localInfoManager.recentDataBases.elementAt(i)).getName());
                fileElement.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                        openRecentDataBase(evt);
                    }
                }
                );
                
                jMenuRecentDB.add(fileElement);
            }
        }
    }
    
    private void addToRecentFiles(java.io.File fileToAdd){
        
        if (localInfoManager.recentFiles.size() > 10 )
            localInfoManager.recentFiles.remove(localInfoManager.recentFiles.lastElement());
        
        if(localInfoManager.recentFiles.contains(fileToAdd))
            localInfoManager.recentFiles.removeElement(fileToAdd);
        
        localInfoManager.recentFiles.add(0,fileToAdd);
        
        bulidRecentFilesMenu();
        
    }
    
    private void addToRecentDatabases(java.io.File fileToAdd){
        
        if (localInfoManager.recentDataBases.size() > 10 )
            localInfoManager.recentDataBases.remove(localInfoManager.recentDataBases.lastElement());
        
        if(localInfoManager.recentDataBases.contains(fileToAdd))
            localInfoManager.recentDataBases.removeElement(fileToAdd);
        
        localInfoManager.recentDataBases.add(0,fileToAdd);
        
    }
    
    /**
     * Returns the {@link hydroScalingAPI.mainGUI.objects.GUI_InfoManager} associated
     * with the GUI
     * @return A {@link hydroScalingAPI.mainGUI.objects.GUI_InfoManager}
     */
    public hydroScalingAPI.mainGUI.objects.GUI_InfoManager getInfoManager(){
        return localInfoManager;
    }
    
    /**
     * Returns the {@link hydroScalingAPI.mainGUI.objects.GaugesManager} used by the
     * GUI to manage Gauge-type information in the database
     * @return A {@link hydroScalingAPI.mainGUI.objects.GaugesManager}
     */
    public hydroScalingAPI.mainGUI.objects.GaugesManager getGaugesManager(){
        return localGaugesManager;
    }
    
    /**
     * Returns the {@link hydroScalingAPI.mainGUI.objects.LocationsManager} used by the
     * GUI to manage Location-type information in the database
     * @return A {@link hydroScalingAPI.mainGUI.objects.LocationsManager}
     */
    public hydroScalingAPI.mainGUI.objects.LocationsManager getLocationsManager(){
        return localLocationsManager;
    }
    
    /**
     * Activates the Locations Panel in the GUI.  This is done after the database has
     * been fully read by an independent thread
     * @param enabled A boolean used as an On/Off switch for the Locations panel
     */
    public void LocationsPanelEnabled(boolean enabled){
        launchLocationViewer.setEnabled(enabled);
        locationsToDisplay.setEnabled(enabled);
        addLocation.setEnabled(enabled);
        clearLocations.setEnabled(enabled);
        editLocation.setEnabled(enabled);
        showLocationName.setEnabled(enabled);
    }
    
    /**
     * Activates the Gauges Panel in the GUI.  This is done after the database has
     * been fully read by an independent thread
     * @param enabled A boolean used as an On/Off switch for the Gauges panel
     */
    public void GaugesPanelEnabled(boolean enabled){
        ts_analysis.setEnabled(enabled);
        launchTSAnalyzer.setEnabled(enabled);
        gaugesToDisplay.setEnabled(enabled);
        addGauge.setEnabled(enabled);
        clearGauges.setEnabled(enabled);
        editGauge.setEnabled(enabled);
        showGaugeCode.setEnabled(enabled);
    }
    
    /**
     * Returns an array containing the Gauges that the user wants to visualize over the
     * raster maps
     * @return An Object[] Array.  Each object can be casted into an {@link hydroScalingAPI.io.MetaGauge}
     */
    public java.util.List getActiveGauges(){
        activeGaugesContainer.setSelectionInterval(0,activeGaugesContainer.getModel().getSize()-1);
        java.util.List actGauges=activeGaugesContainer.getSelectedValuesList();
        activeGaugesContainer.removeSelectionInterval(0,activeGaugesContainer.getModel().getSize()-1);
        return actGauges;
    }
    
    /**
     * Returns an array containing the Locations that the user wants to visualize over the
     * raster maps
     * @return An Object[] Array.  Each object can be casted into an {@link
     * hydroScalingAPI.io.MetaLocation}
     */
    public java.util.List getActiveLocations(){
        activeLocationsContainer.setSelectionInterval(0,activeLocationsContainer.getModel().getSize()-1);
        java.util.List actLocs=activeLocationsContainer.getSelectedValuesList();
        activeLocationsContainer.removeSelectionInterval(0,activeLocationsContainer.getModel().getSize()-1);
        return actLocs;
    }
    
    /**
     * Returns an array containing the vector data in the CUENCAS database that the user
     * wants to visualize over the raster maps
     * @return An Object[] Array.  Each object can be casted into an {@link
     * javax.swing.JCheckBox}.  The name of the checkBox is the file name which can be
     * used to read the vector to visualize (The list includes both Shapefile and DLG
     * files)
     */
    public Object[] getActiveVectors(){
        Object[] theVectors=new Object[shapesContainer.getComponentCount()+dlgsContainer.getComponentCount()];
        for (int i=0;i<shapesContainer.getComponentCount();i++){
            theVectors[i]=shapesContainer.getComponent(i);
        }
        for (int i=0;i<dlgsContainer.getComponentCount();i++){
            theVectors[shapesContainer.getComponentCount()+i]=dlgsContainer.getComponent(i);
        }
        return theVectors;
    }
    
    /**
     * Returns an array containing the polygon data in the CUENCAS database that the user
     * wants to visualize over the raster maps
     * @return An Object[] Array.  Each object can be casted into an {@link
     * javax.swing.JCheckBox}.  The name of the checkBox is the file name which can be
     * used to read the polygon to visualize (The list includes *.poly files)
     */
    public Object[] getActivePolygons(){
        Object[] thePolygons=new Object[polygonContainer.getComponentCount()];
        for (int i=0;i<polygonContainer.getComponentCount();i++){
            thePolygons[i]=polygonContainer.getComponent(i);
        }
        return thePolygons;
    }
    
    /**
     * Returns a Boolean indicating whether the user wants the Gauge name to be writen
     * next to the point indicating the Gauge position.
     * @return A boolean
     */
    public boolean nameOnGauges(){
        return showGaugeCode.isSelected();
    }
    /**
     * Returns a boolean indicating whether the user wants the Location name to be writen
     * next to the point indicating the Location position.
     * @return A boolean
     */
    public boolean nameOnLocations(){
        return showLocationName.isSelected();
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mapToolsPopup = new javax.swing.JPopupMenu();
        jMenuItem2 = new javax.swing.JMenuItem();
        jMenuItem3 = new javax.swing.JMenuItem();
        jSeparator5 = new javax.swing.JSeparator();
        mapEditorItem = new javax.swing.JMenuItem();
        dbProps = new javax.swing.JPopupMenu();
        editDBName = new javax.swing.JMenuItem();
        splitPane = new javax.swing.JSplitPane();
        subGUIs_container = new javax.swing.JDesktopPane();
        jPanel2 = new javax.swing.JPanel();
        dataBaseTabs = new javax.swing.JTabbedPane();
        rastersTab = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        metaFileViewer = new javax.swing.JTextArea();
        jScrollPane1 = new javax.swing.JScrollPane();
        topoTree = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        hydroTree = new javax.swing.JTree();
        vectors = new javax.swing.JPanel();
        shapesPanel = new javax.swing.JPanel();
        jScrollPane5 = new javax.swing.JScrollPane();
        shapesContainer = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        addShapeFile = new javax.swing.JButton();
        importShapeFile = new javax.swing.JButton();
        dlgsPanel = new javax.swing.JPanel();
        jScrollPane6 = new javax.swing.JScrollPane();
        dlgsContainer = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        addDlgFile = new javax.swing.JButton();
        importDlgFile = new javax.swing.JButton();
        lines = new javax.swing.JPanel();
        polygons = new javax.swing.JPanel();
        polygonPanel = new javax.swing.JPanel();
        jScrollPane7 = new javax.swing.JScrollPane();
        polygonContainer = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        addPolygonFile = new javax.swing.JButton();
        importPolygonFile = new javax.swing.JButton();
        sites = new javax.swing.JPanel();
        gaugesPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        activeGaugesContainer = new javax.swing.JList();
        jPanel5 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        addGauge = new javax.swing.JButton();
        editGauge = new javax.swing.JButton();
        clearGauges = new javax.swing.JButton();
        jPanel4 = new javax.swing.JPanel();
        launchTSAnalyzer = new javax.swing.JButton();
        gaugesToDisplay = new javax.swing.JButton();
        showGaugeCode = new javax.swing.JCheckBox();
        locationsPanel = new javax.swing.JPanel();
        jScrollPane21 = new javax.swing.JScrollPane();
        activeLocationsContainer = new javax.swing.JList();
        jPanel51 = new javax.swing.JPanel();
        jPanel61 = new javax.swing.JPanel();
        addLocation = new javax.swing.JButton();
        editLocation = new javax.swing.JButton();
        clearLocations = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        launchLocationViewer = new javax.swing.JButton();
        locationsToDisplay = new javax.swing.JButton();
        showLocationName = new javax.swing.JCheckBox();
        documents = new javax.swing.JPanel();
        dbName = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();
        gui_info = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        openDEMButton = new javax.swing.JButton();
        openHydroButton = new javax.swing.JButton();
        openDatabaseButton = new javax.swing.JButton();
        jToolBar2 = new javax.swing.JToolBar();
        calcButton = new javax.swing.JButton();
        editorButton = new javax.swing.JButton();
        jToolBar3 = new javax.swing.JToolBar();
        helpButton = new javax.swing.JButton();
        showHelpButton = new javax.swing.JToggleButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        file = new javax.swing.JMenu();
        openFile = new javax.swing.JMenu();
        openDEM = new javax.swing.JMenuItem();
        openHydro = new javax.swing.JMenuItem();
        openVector = new javax.swing.JMenuItem();
        openDB = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        createDB = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JSeparator();
        jMenuImport = new javax.swing.JMenu();
        importDEM = new javax.swing.JMenu();
        importDemFromGRASS = new javax.swing.JMenuItem();
        importDemFromGRID = new javax.swing.JMenuItem();
        importDemFromBIL_USGS = new javax.swing.JMenuItem();
        importDemFromFloatBIL_USGS = new javax.swing.JMenuItem();
        importHydro = new javax.swing.JMenu();
        importHydroFromGRASS = new javax.swing.JMenuItem();
        importHydroFromGRID = new javax.swing.JMenuItem();
        importHydroFromNetCDF = new javax.swing.JMenuItem();
        importHydroFromBIL_USGS = new javax.swing.JMenuItem();
        importSites = new javax.swing.JMenu();
        importSitesFromGazetteer = new javax.swing.JMenuItem();
        importSitesFromWaterUSGS = new javax.swing.JMenuItem();
        jMenuExport = new javax.swing.JMenu();
        exportDEM = new javax.swing.JMenu();
        exportDemToGRASS = new javax.swing.JMenuItem();
        exportDemToGRID = new javax.swing.JMenuItem();
        exportHydro = new javax.swing.JMenu();
        exportHydroToGRASS = new javax.swing.JMenuItem();
        exportHydroToGRID = new javax.swing.JMenuItem();
        exportHydroToNetCDF = new javax.swing.JMenuItem();
        exportSites = new javax.swing.JMenu();
        exportSitesToAscii = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        jMenuRecentFiles = new javax.swing.JMenu();
        jMenuRecentDB = new javax.swing.JMenu();
        jSeparator4 = new javax.swing.JSeparator();
        quit = new javax.swing.JMenuItem();
        tools = new javax.swing.JMenu();
        mapCalc = new javax.swing.JMenuItem();
        options = new javax.swing.JMenu();
        colors = new javax.swing.JMenuItem();
        preferences = new javax.swing.JMenuItem();
        modules = new javax.swing.JMenu();
        networkExtraction = new javax.swing.JMenuItem();
        networkAnalysis = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JSeparator();
        waterBalance = new javax.swing.JMenuItem();
        jSeparator7 = new javax.swing.JSeparator();
        ts_analysis = new javax.swing.JMenuItem();
        jSeparator8 = new javax.swing.JSeparator();
        rain_simulator = new javax.swing.JMenuItem();
        rainfall_runoff_simulator = new javax.swing.JMenuItem();
        jSeparator12 = new javax.swing.JSeparator();
        jMenu1 = new javax.swing.JMenu();
        tRIBS_input = new javax.swing.JMenuItem();
        tRIBS_output = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        swmm_coupling = new javax.swing.JMenu();
        swmm_raindata_importer = new javax.swing.JMenuItem();
        jMenuItem4 = new javax.swing.JMenuItem();
        jMenuItem5 = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        jMenuItem6 = new javax.swing.JMenuItem();
        window = new javax.swing.JMenu();
        arrange = new javax.swing.JMenuItem();
        jSeparator9 = new javax.swing.JSeparator();
        help = new javax.swing.JMenu();
        contents = new javax.swing.JMenuItem();
        index = new javax.swing.JMenuItem();
        jSeparator10 = new javax.swing.JSeparator();
        license = new javax.swing.JMenuItem();
        jSeparator11 = new javax.swing.JSeparator();
        about = new javax.swing.JMenuItem();

        jMenuItem2.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jMenuItem2.setText("Copy");
        jMenuItem2.setEnabled(false);
        mapToolsPopup.add(jMenuItem2);

        jMenuItem3.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jMenuItem3.setText("Delete");
        jMenuItem3.setEnabled(false);
        mapToolsPopup.add(jMenuItem3);
        mapToolsPopup.add(jSeparator5);

        mapEditorItem.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        mapEditorItem.setText("Edit Map");
        mapEditorItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mapEditorItemActionPerformed(evt);
            }
        });
        mapToolsPopup.add(mapEditorItem);

        editDBName.setFont(new java.awt.Font("Lucida Grande", 0, 10)); // NOI18N
        editDBName.setText("Rename Database");
        editDBName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editDBNameActionPerformed(evt);
            }
        });
        dbProps.add(editDBName);

        setTitle("Multiscale Hydrology for Ungauged Basins");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        splitPane.setRightComponent(subGUIs_container);

        jPanel2.setLayout(new java.awt.BorderLayout());

        dataBaseTabs.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        rastersTab.setLayout(new java.awt.GridLayout(3, 1, 5, 7));

        jScrollPane4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Metafile", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N
        jScrollPane4.setFont(new java.awt.Font("Default", 0, 10)); // NOI18N

        metaFileViewer.setEditable(false);
        metaFileViewer.setFont(new java.awt.Font("Serif", 0, 11)); // NOI18N
        metaFileViewer.setLineWrap(true);
        metaFileViewer.setWrapStyleWord(true);
        jScrollPane4.setViewportView(metaFileViewer);

        rastersTab.add(jScrollPane4);

        jScrollPane1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Topography Database", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N
        jScrollPane1.setFont(new java.awt.Font("Default", 0, 10)); // NOI18N

        topoTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                topoTreeMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(topoTree);

        rastersTab.add(jScrollPane1);

        jScrollPane3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Hydrology Database", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N
        jScrollPane3.setFont(new java.awt.Font("Default", 0, 10)); // NOI18N

        hydroTree.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                hydroTreeMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(hydroTree);

        rastersTab.add(jScrollPane3);

        dataBaseTabs.addTab("Rasters", rastersTab);

        vectors.setLayout(new java.awt.GridLayout(2, 1));

        shapesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Available Shape Files", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N
        shapesPanel.setLayout(new java.awt.BorderLayout());

        shapesContainer.setLayout(new java.awt.GridLayout(0, 1));
        jScrollPane5.setViewportView(shapesContainer);

        shapesPanel.add(jScrollPane5, java.awt.BorderLayout.CENTER);

        jPanel8.setLayout(new java.awt.GridLayout(1, 2));

        addShapeFile.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        addShapeFile.setText("Add Shape File");
        jPanel8.add(addShapeFile);

        importShapeFile.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        importShapeFile.setText("Import To DB");
        jPanel8.add(importShapeFile);

        shapesPanel.add(jPanel8, java.awt.BorderLayout.SOUTH);

        vectors.add(shapesPanel);

        dlgsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Available DLG Files", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N
        dlgsPanel.setLayout(new java.awt.BorderLayout());

        dlgsContainer.setLayout(new java.awt.GridLayout(0, 1));
        jScrollPane6.setViewportView(dlgsContainer);

        dlgsPanel.add(jScrollPane6, java.awt.BorderLayout.CENTER);

        jPanel9.setLayout(new java.awt.GridLayout(1, 2));

        addDlgFile.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        addDlgFile.setText("Add DLG File");
        jPanel9.add(addDlgFile);

        importDlgFile.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        importDlgFile.setText("Import To DB");
        jPanel9.add(importDlgFile);

        dlgsPanel.add(jPanel9, java.awt.BorderLayout.SOUTH);

        vectors.add(dlgsPanel);

        dataBaseTabs.addTab("Vectors", vectors);
        dataBaseTabs.addTab("Lines", lines);

        polygons.setLayout(new java.awt.GridLayout(1, 0));

        polygonPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Available Polygon Files", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N
        polygonPanel.setLayout(new java.awt.BorderLayout());

        polygonContainer.setLayout(new java.awt.GridLayout(0, 1));
        jScrollPane7.setViewportView(polygonContainer);

        polygonPanel.add(jScrollPane7, java.awt.BorderLayout.CENTER);

        jPanel10.setLayout(new java.awt.GridLayout(1, 2));

        addPolygonFile.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        addPolygonFile.setText("Add Polygon File");
        jPanel10.add(addPolygonFile);

        importPolygonFile.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        importPolygonFile.setText("Import To DB");
        jPanel10.add(importPolygonFile);

        polygonPanel.add(jPanel10, java.awt.BorderLayout.SOUTH);

        polygons.add(polygonPanel);

        dataBaseTabs.addTab("Polygons", polygons);

        sites.setLayout(new java.awt.GridLayout(2, 1));

        gaugesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "GAUGES", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N
        gaugesPanel.setLayout(new java.awt.BorderLayout());

        activeGaugesContainer.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jScrollPane2.setViewportView(activeGaugesContainer);

        gaugesPanel.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel5.setLayout(new java.awt.GridLayout(3, 1));

        jPanel6.setLayout(new java.awt.GridLayout(1, 3));

        addGauge.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        addGauge.setText("New");
        addGauge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addGaugeActionPerformed(evt);
            }
        });
        jPanel6.add(addGauge);

        editGauge.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        editGauge.setText("Edit");
        editGauge.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editGaugeActionPerformed(evt);
            }
        });
        jPanel6.add(editGauge);

        clearGauges.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        clearGauges.setText("Clear");
        clearGauges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearGaugesActionPerformed(evt);
            }
        });
        jPanel6.add(clearGauges);

        jPanel5.add(jPanel6);

        jPanel4.setLayout(new java.awt.GridLayout(1, 2));

        launchTSAnalyzer.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        launchTSAnalyzer.setText("Get Time Series");
        launchTSAnalyzer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchTSAnalyzerActionPerformed(evt);
            }
        });
        jPanel4.add(launchTSAnalyzer);

        gaugesToDisplay.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        gaugesToDisplay.setText("Update List");
        gaugesToDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gaugesToDisplayActionPerformed(evt);
            }
        });
        jPanel4.add(gaugesToDisplay);

        jPanel5.add(jPanel4);

        showGaugeCode.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        showGaugeCode.setSelected(true);
        showGaugeCode.setText("Show Gauge Code on Maps");
        showGaugeCode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showGaugeCodeActionPerformed(evt);
            }
        });
        jPanel5.add(showGaugeCode);

        gaugesPanel.add(jPanel5, java.awt.BorderLayout.SOUTH);

        sites.add(gaugesPanel);

        locationsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "LOCATIONS", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10))); // NOI18N
        locationsPanel.setLayout(new java.awt.BorderLayout());

        activeLocationsContainer.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jScrollPane21.setViewportView(activeLocationsContainer);

        locationsPanel.add(jScrollPane21, java.awt.BorderLayout.CENTER);

        jPanel51.setLayout(new java.awt.GridLayout(3, 1));

        jPanel61.setLayout(new java.awt.GridLayout(1, 3));

        addLocation.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        addLocation.setText("New");
        addLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addLocationActionPerformed(evt);
            }
        });
        jPanel61.add(addLocation);

        editLocation.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        editLocation.setText("Edit");
        editLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLocationActionPerformed(evt);
            }
        });
        jPanel61.add(editLocation);

        clearLocations.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        clearLocations.setText("Clear");
        clearLocations.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                clearLocationsActionPerformed(evt);
            }
        });
        jPanel61.add(clearLocations);

        jPanel51.add(jPanel61);

        jPanel7.setLayout(new java.awt.GridLayout(1, 2));

        launchLocationViewer.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        launchLocationViewer.setText("Get Information");
        launchLocationViewer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                launchLocationViewerActionPerformed(evt);
            }
        });
        jPanel7.add(launchLocationViewer);

        locationsToDisplay.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        locationsToDisplay.setText("Update List");
        locationsToDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                locationsToDisplayActionPerformed(evt);
            }
        });
        jPanel7.add(locationsToDisplay);

        jPanel51.add(jPanel7);

        showLocationName.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        showLocationName.setSelected(true);
        showLocationName.setText("Show Location Name on Maps");
        showLocationName.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLocationNameActionPerformed(evt);
            }
        });
        jPanel51.add(showLocationName);

        locationsPanel.add(jPanel51, java.awt.BorderLayout.SOUTH);

        sites.add(locationsPanel);

        dataBaseTabs.addTab("Sites", sites);
        dataBaseTabs.addTab("Documents", documents);

        jPanel2.add(dataBaseTabs, java.awt.BorderLayout.CENTER);

        dbName.setEditable(false);
        dbName.setFont(new java.awt.Font("Verdana", 1, 12)); // NOI18N
        dbName.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        dbName.setText("Database Name");
        dbName.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        dbName.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                dbNameMouseReleased(evt);
            }
        });
        jPanel2.add(dbName, java.awt.BorderLayout.NORTH);

        splitPane.setLeftComponent(jPanel2);

        getContentPane().add(splitPane, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.BorderLayout());

        gui_info.setEditable(false);
        gui_info.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        gui_info.setText("GUI Status");
        jPanel1.add(gui_info, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel1, java.awt.BorderLayout.SOUTH);

        jPanel3.setLayout(new javax.swing.BoxLayout(jPanel3, javax.swing.BoxLayout.LINE_AXIS));

        jToolBar1.setToolTipText("System");

        openDEMButton.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        openDEMButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/icons/new.gif"))); // NOI18N
        openDEMButton.setToolTipText("Open DEM");
        openDEMButton.setAlignmentX(0.5F);
        openDEMButton.setMaximumSize(new java.awt.Dimension(25, 25));
        openDEMButton.setMinimumSize(new java.awt.Dimension(20, 20));
        openDEMButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDEMButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(openDEMButton);

        openHydroButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/icons/new.gif"))); // NOI18N
        openHydroButton.setToolTipText("Open Hydro");
        openHydroButton.setAlignmentX(0.5F);
        openHydroButton.setMaximumSize(new java.awt.Dimension(25, 25));
        openHydroButton.setMinimumSize(new java.awt.Dimension(20, 20));
        openHydroButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openHydroButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(openHydroButton);

        openDatabaseButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/icons/defaultFolderOpen.gif"))); // NOI18N
        openDatabaseButton.setToolTipText("Open Database");
        openDatabaseButton.setAlignmentX(0.5F);
        openDatabaseButton.setMaximumSize(new java.awt.Dimension(25, 25));
        openDatabaseButton.setMinimumSize(new java.awt.Dimension(20, 20));
        openDatabaseButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDatabaseButtonActionPerformed(evt);
            }
        });
        jToolBar1.add(openDatabaseButton);

        jPanel3.add(jToolBar1);

        jToolBar2.setToolTipText("Tools");

        calcButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/icons/calcMapa.gif"))); // NOI18N
        calcButton.setToolTipText("Map Calculator");
        calcButton.setAlignmentX(0.5F);
        calcButton.setMaximumSize(new java.awt.Dimension(25, 25));
        calcButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jToolBar2.add(calcButton);

        editorButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/icons/editor.gif"))); // NOI18N
        editorButton.setToolTipText("Map Editor");
        editorButton.setAlignmentX(0.5F);
        editorButton.setMaximumSize(new java.awt.Dimension(25, 25));
        editorButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jToolBar2.add(editorButton);

        jPanel3.add(jToolBar2);

        jToolBar3.setToolTipText("Help");

        helpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/icons/moduleHelp.gif"))); // NOI18N
        helpButton.setToolTipText("UB Help");
        helpButton.setMaximumSize(new java.awt.Dimension(25, 25));
        helpButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jToolBar3.add(helpButton);

        showHelpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/icons/showHelp.gif"))); // NOI18N
        showHelpButton.setToolTipText("What is this?");
        showHelpButton.setAlignmentX(0.5F);
        showHelpButton.setMaximumSize(new java.awt.Dimension(25, 25));
        showHelpButton.setMinimumSize(new java.awt.Dimension(20, 20));
        jToolBar3.add(showHelpButton);

        jPanel3.add(jToolBar3);

        getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

        jMenuBar1.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        file.setMnemonic('f');
        file.setText("File");
        file.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        openFile.setMnemonic('o');
        openFile.setText("Open File");
        openFile.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N

        openDEM.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        openDEM.setMnemonic('d');
        openDEM.setText("Open DEM");
        openDEM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDEMActionPerformed(evt);
            }
        });
        openFile.add(openDEM);

        openHydro.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        openHydro.setMnemonic('h');
        openHydro.setText("Open HydroClimatic");
        openHydro.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openHydroActionPerformed(evt);
            }
        });
        openFile.add(openHydro);

        openVector.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        openVector.setMnemonic('v');
        openVector.setText("Open Vector");
        openFile.add(openVector);

        file.add(openFile);

        openDB.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        openDB.setMnemonic('d');
        openDB.setText("Open Data Base");
        openDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openDBActionPerformed(evt);
            }
        });
        file.add(openDB);
        file.add(jSeparator1);

        createDB.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        createDB.setMnemonic('c');
        createDB.setText("Create New Data Base");
        createDB.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createDBActionPerformed(evt);
            }
        });
        file.add(createDB);
        file.add(jSeparator2);

        jMenuImport.setMnemonic('i');
        jMenuImport.setText("Import");
        jMenuImport.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        importDEM.setText("Import DEM");
        importDEM.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        importDemFromGRASS.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        importDemFromGRASS.setText("From GRASS File");
        importDemFromGRASS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importDemFromGRASSActionPerformed(evt);
            }
        });
        importDEM.add(importDemFromGRASS);

        importDemFromGRID.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        importDemFromGRID.setText("From ARC-INFO Grid File");
        importDemFromGRID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importDemFromGRIDActionPerformed(evt);
            }
        });
        importDEM.add(importDemFromGRID);

        importDemFromBIL_USGS.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        importDemFromBIL_USGS.setText("From USGS BIL Format");
        importDemFromBIL_USGS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importDemFromBIL_USGSActionPerformed(evt);
            }
        });
        importDEM.add(importDemFromBIL_USGS);

        importDemFromFloatBIL_USGS.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        importDemFromFloatBIL_USGS.setText("From USGS GridFloat Format");
        importDemFromFloatBIL_USGS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importDemFromFloatBIL_USGSActionPerformed(evt);
            }
        });
        importDEM.add(importDemFromFloatBIL_USGS);

        jMenuImport.add(importDEM);

        importHydro.setText("Import Hydroclimatic Raster");
        importHydro.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        importHydroFromGRASS.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        importHydroFromGRASS.setText("From GRASS File");
        importHydro.add(importHydroFromGRASS);

        importHydroFromGRID.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        importHydroFromGRID.setText("From ARC-INFO Grid File");
        importHydroFromGRID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importHydroFromGRIDActionPerformed(evt);
            }
        });
        importHydro.add(importHydroFromGRID);

        importHydroFromNetCDF.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        importHydroFromNetCDF.setText("From NetCDF File");
        importHydro.add(importHydroFromNetCDF);

        importHydroFromBIL_USGS.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        importHydroFromBIL_USGS.setText("From USGS BIL Format");
        importHydroFromBIL_USGS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importHydroFromBIL_USGSActionPerformed(evt);
            }
        });
        importHydro.add(importHydroFromBIL_USGS);

        jMenuImport.add(importHydro);

        importSites.setText("Import Sites");
        importSites.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        importSitesFromGazetteer.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        importSitesFromGazetteer.setText("Locations From USGS Gazetteer");
        importSites.add(importSitesFromGazetteer);

        importSitesFromWaterUSGS.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        importSitesFromWaterUSGS.setText("Streamflow Gauge From Water.USGS.gov");
        importSites.add(importSitesFromWaterUSGS);

        jMenuImport.add(importSites);

        file.add(jMenuImport);

        jMenuExport.setMnemonic('e');
        jMenuExport.setText("Export");
        jMenuExport.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        exportDEM.setText("Export DEM");
        exportDEM.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        exportDemToGRASS.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        exportDemToGRASS.setText("To GRASS File");
        exportDemToGRASS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDemToGRASSActionPerformed(evt);
            }
        });
        exportDEM.add(exportDemToGRASS);

        exportDemToGRID.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        exportDemToGRID.setText("To ARC-INFO Grid File");
        exportDemToGRID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportDemToGRIDActionPerformed(evt);
            }
        });
        exportDEM.add(exportDemToGRID);

        jMenuExport.add(exportDEM);

        exportHydro.setText("Export Hydroclimatic Raster");
        exportHydro.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        exportHydroToGRASS.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        exportHydroToGRASS.setText("To GRASS File");
        exportHydroToGRASS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportHydroToGRASSActionPerformed(evt);
            }
        });
        exportHydro.add(exportHydroToGRASS);

        exportHydroToGRID.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        exportHydroToGRID.setText("To ARC-INFO Grid File");
        exportHydroToGRID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportHydroToGRIDActionPerformed(evt);
            }
        });
        exportHydro.add(exportHydroToGRID);

        exportHydroToNetCDF.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        exportHydroToNetCDF.setText("To NetCDF File");
        exportHydro.add(exportHydroToNetCDF);

        jMenuExport.add(exportHydro);

        exportSites.setText("Export Sites");
        exportSites.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        exportSitesToAscii.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        exportSitesToAscii.setText("To ASCII File");
        exportSites.add(exportSitesToAscii);

        jMenuExport.add(exportSites);

        file.add(jMenuExport);
        file.add(jSeparator3);

        jMenuRecentFiles.setMnemonic('r');
        jMenuRecentFiles.setText("Recent Files");
        jMenuRecentFiles.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        file.add(jMenuRecentFiles);

        jMenuRecentDB.setMnemonic('b');
        jMenuRecentDB.setText("Recent Databases");
        jMenuRecentDB.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        file.add(jMenuRecentDB);
        file.add(jSeparator4);

        quit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        quit.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        quit.setText("Quit");
        quit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                quitActionPerformed(evt);
            }
        });
        file.add(quit);

        jMenuBar1.add(file);

        tools.setMnemonic('t');
        tools.setText("Tools");
        tools.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        mapCalc.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        mapCalc.setMnemonic('c');
        mapCalc.setText("Map Calculator");
        mapCalc.setEnabled(false);
        tools.add(mapCalc);

        jMenuBar1.add(tools);

        options.setMnemonic('o');
        options.setText("Options");
        options.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        colors.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        colors.setMnemonic('c');
        colors.setText("System Colors ...");
        colors.setEnabled(false);
        options.add(colors);

        preferences.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        preferences.setMnemonic('p');
        preferences.setText("System Preferences ...");
        preferences.setEnabled(false);
        options.add(preferences);

        jMenuBar1.add(options);

        modules.setMnemonic('m');
        modules.setText("Modules");
        modules.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        networkExtraction.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        networkExtraction.setMnemonic('x');
        networkExtraction.setText("Network Extraction");
        networkExtraction.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                networkExtractionActionPerformed(evt);
            }
        });
        modules.add(networkExtraction);

        networkAnalysis.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        networkAnalysis.setMnemonic('a');
        networkAnalysis.setText("Network Analysis");
        networkAnalysis.setEnabled(false);
        modules.add(networkAnalysis);
        modules.add(jSeparator6);

        waterBalance.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        waterBalance.setMnemonic('b');
        waterBalance.setText("Water Balance");
        waterBalance.setEnabled(false);
        modules.add(waterBalance);
        modules.add(jSeparator7);

        ts_analysis.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        ts_analysis.setMnemonic('t');
        ts_analysis.setText("Time Series Analysis");
        ts_analysis.setEnabled(false);
        modules.add(ts_analysis);
        modules.add(jSeparator8);

        rain_simulator.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        rain_simulator.setMnemonic('r');
        rain_simulator.setText("Rain Simulator");
        rain_simulator.setEnabled(false);
        modules.add(rain_simulator);

        rainfall_runoff_simulator.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        rainfall_runoff_simulator.setMnemonic('o');
        rainfall_runoff_simulator.setText("Rainfall Runoff Simulator");
        rainfall_runoff_simulator.setEnabled(false);
        modules.add(rainfall_runoff_simulator);
        modules.add(jSeparator12);

        jMenu1.setText("tRIBS Visualization Modules");
        jMenu1.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        tRIBS_input.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tRIBS_input.setMnemonic('o');
        tRIBS_input.setText("Input Module");
        tRIBS_input.setEnabled(false);
        jMenu1.add(tRIBS_input);

        tRIBS_output.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        tRIBS_output.setMnemonic('o');
        tRIBS_output.setText("Output Module");
        tRIBS_output.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tRIBS_outputActionPerformed(evt);
            }
        });
        jMenu1.add(tRIBS_output);

        modules.add(jMenu1);
        modules.add(jSeparator13);

        swmm_coupling.setText("SWMM coupling");
        swmm_coupling.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        swmm_raindata_importer.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        swmm_raindata_importer.setText("Rain data importer");
        swmm_raindata_importer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swmm_raindata_importerActionPerformed(evt);
            }
        });
        swmm_coupling.add(swmm_raindata_importer);

        jMenuItem4.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jMenuItem4.setText("SWMM Input File Sketch Generator");
        jMenuItem4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem4ActionPerformed(evt);
            }
        });
        swmm_coupling.add(jMenuItem4);

        jMenuItem5.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jMenuItem5.setText("SWMM Input File Node Filler");
        jMenuItem5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem5ActionPerformed(evt);
            }
        });
        swmm_coupling.add(jMenuItem5);
        swmm_coupling.add(jSeparator14);

        jMenuItem6.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        jMenuItem6.setText("SubBasin DEM Viewer");
        jMenuItem6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem6ActionPerformed(evt);
            }
        });
        swmm_coupling.add(jMenuItem6);

        modules.add(swmm_coupling);

        jMenuBar1.add(modules);

        window.setMnemonic('w');
        window.setText("Window");
        window.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        arrange.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        arrange.setMnemonic('a');
        arrange.setText("Arrange all ...");
        arrange.setEnabled(false);
        window.add(arrange);
        window.add(jSeparator9);

        jMenuBar1.add(window);

        help.setMnemonic('h');
        help.setText("Help");
        help.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N

        contents.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        contents.setMnemonic('c');
        contents.setText("Contents ...");
        contents.setEnabled(false);
        help.add(contents);

        index.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        index.setMnemonic('i');
        index.setText("Index");
        index.setEnabled(false);
        help.add(index);
        help.add(jSeparator10);

        license.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        license.setMnemonic('l');
        license.setText("License");
        license.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                licenseActionPerformed(evt);
            }
        });
        help.add(license);
        help.add(jSeparator11);

        about.setFont(new java.awt.Font("Verdana", 0, 10)); // NOI18N
        about.setMnemonic('a');
        about.setText("About");
        about.setEnabled(false);
        help.add(about);

        jMenuBar1.add(help);

        setJMenuBar(jMenuBar1);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void tRIBS_outputActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_tRIBS_outputActionPerformed
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser("/");
        fc.setFileSelectionMode(fc.DIRECTORIES_ONLY);
        fc.setDialogTitle("tRIBS Output Directory");
//        javax.swing.filechooser.FileFilter dirFilter = new hydroScalingAPI.util.fileUtilities.DirFilter();
//        fc.addChoosableFileFilter(dirFilter);
        int result = fc.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        hydroScalingAPI.mainGUI.widgets.InfoRequest ir=new hydroScalingAPI.mainGUI.widgets.InfoRequest(this);
        ir.setVisible(true);
        
        if(ir.getBaseName().equalsIgnoreCase("")) return;
        
        System.out.println(fc.getSelectedFile().getPath());
        System.out.println(ir.getBaseName());
        try {
            new hydroScalingAPI.modules.tRIBS_io.widgets.TRIBS_io(this,fc.getSelectedFile(),ir.getBaseName(),0).setVisible(true);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
        
    }//GEN-LAST:event_tRIBS_outputActionPerformed

    private void editDBNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editDBNameActionPerformed
        new hydroScalingAPI.subGUIs.widgets.ChangeDBName(this).setVisible(true);
    }//GEN-LAST:event_editDBNameActionPerformed

    private void dbNameMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_dbNameMouseReleased
        if(evt.getButton() == 3){
            dbProps.show(evt.getComponent(),evt.getX(),evt.getY());
        }
    }//GEN-LAST:event_dbNameMouseReleased

    private void launchTSAnalyzerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchTSAnalyzerActionPerformed
        java.util.List toView=activeGaugesContainer.getSelectedValuesList();
        if(toView.size() == 0) return;
        hydroScalingAPI.io.MetaGauge gaugeInfo=(hydroScalingAPI.io.MetaGauge)toView.get(0);
        hydroScalingAPI.modules.analysis_TS.widgets.TimeSeriesViewer gauView=new hydroScalingAPI.modules.analysis_TS.widgets.TimeSeriesViewer(this,gaugeInfo);
        gauView.setVisible(true);
    }//GEN-LAST:event_launchTSAnalyzerActionPerformed

    private void mapEditorItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mapEditorItemActionPerformed
        
        hydroScalingAPI.modules.mapEditor.widgets.MapEditor thisMapEditor;
        
        try {
            if(localInfoManager.metaFileActive.getName().lastIndexOf("metaDEM") != -1){
                
                hydroScalingAPI.subGUIs.widgets.DemOpenDialog openDem=new hydroScalingAPI.subGUIs.widgets.DemOpenDialog(this,new hydroScalingAPI.io.MetaRaster(localInfoManager.metaFileActive),"Edit");
                openDem.setVisible(true);
                if (openDem.mapsSelected()){
                    thisMapEditor=new hydroScalingAPI.modules.mapEditor.widgets.MapEditor(this,openDem.getSelectedMetaRasters()[0]);
                    thisMapEditor.setVisible(true);
                }
            } else {
                hydroScalingAPI.subGUIs.widgets.HydroOpenDialog openVhc=new hydroScalingAPI.subGUIs.widgets.HydroOpenDialog(this,new hydroScalingAPI.io.MetaRaster(localInfoManager.metaFileActive),"Edit");
                openVhc.setVisible(true);
                
                if (openVhc.mapsSelected()){
                    thisMapEditor=new hydroScalingAPI.modules.mapEditor.widgets.MapEditor(this,openVhc.getSelectedMetaRasters()[0]);
                    thisMapEditor.setVisible(true);
                }
            }
            
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (visad.VisADException ex){
            ex.printStackTrace();
        }
    }//GEN-LAST:event_mapEditorItemActionPerformed

    private void exportHydroToGRASSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportHydroToGRASSActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersHydPath);
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("Hydroclimatic File Selection");
        javax.swing.filechooser.FileFilter vhcFilter = new visad.util.ExtensionFileFilter("metaVHC","Hydroclimatic File");
        fcI.addChoosableFileFilter(vhcFilter);
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser();
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result=fcO.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        if (dirOutput == null) return;
        
        try{
            java.io.File theMetaFile=new java.io.File(fileInput.getPath().substring(0,fileInput.getPath().lastIndexOf("."))+".metaVHC");
            hydroScalingAPI.io.CRasToGrass exporter=new hydroScalingAPI.io.CRasToGrass(theMetaFile,dirOutput);
            hydroScalingAPI.subGUIs.widgets.HydroOpenDialog openVhc=new hydroScalingAPI.subGUIs.widgets.HydroOpenDialog(this,new hydroScalingAPI.io.MetaRaster(theMetaFile),"Export");
            openVhc.setVisible(true);
            if (openVhc.mapsSelected()){
                for (int i=0;i<openVhc.getSelectedMetaRasters().length;i++){
                    exporter.fileToExport(openVhc.getSelectedMetaRasters()[i]);
                    exporter.writeGrassFile();
                }
            }
            setUpGUI(true);
        }catch(java.io.IOException ioe){
            System.err.println("Failed during ESRI-ASCII file import");
            ioe.printStackTrace();
        }
    }//GEN-LAST:event_exportHydroToGRASSActionPerformed

    private void exportDemToGRIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDemToGRIDActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersDemPath);
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("DEM File Selection");
        javax.swing.filechooser.FileFilter demFilter = new visad.util.ExtensionFileFilter("metaDEM","DEM File");
        fcI.addChoosableFileFilter(demFilter);
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser();
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result=fcO.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        if (dirOutput == null) return;
        
        try{
            String fileName=fileInput.getPath().substring(0,fileInput.getPath().lastIndexOf("."))+".metaDEM";
            java.io.File theMetaFile=new java.io.File(fileName);
            hydroScalingAPI.io.CRasToEsriASCII exporter=new hydroScalingAPI.io.CRasToEsriASCII(theMetaFile,dirOutput);
            hydroScalingAPI.subGUIs.widgets.DemOpenDialog openDem=new hydroScalingAPI.subGUIs.widgets.DemOpenDialog(this,new hydroScalingAPI.io.MetaRaster(theMetaFile),"Export");
            openDem.setVisible(true);
            if (openDem.mapsSelected()){
                for (int i=0;i<openDem.getSelectedMetaRasters().length;i++){
                    exporter.fileToExport(openDem.getSelectedMetaRasters()[i]);
                    exporter.writeEsriFile();
                }
            }
            setUpGUI(true);
        }catch(java.io.IOException ioe){
            System.err.println("Failed during ESRI-ASCII file import");
            ioe.printStackTrace();
        }
    }//GEN-LAST:event_exportDemToGRIDActionPerformed
    
    private void exportHydroToGRIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportHydroToGRIDActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersHydPath);
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("Hydroclimatic File Selection");
        javax.swing.filechooser.FileFilter vhcFilter = new visad.util.ExtensionFileFilter("metaVHC","Hydroclimatic File");
        fcI.addChoosableFileFilter(vhcFilter);
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser();
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result=fcO.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        if (dirOutput == null) return;
        
        try{
            java.io.File theMetaFile=new java.io.File(fileInput.getPath().substring(0,fileInput.getPath().lastIndexOf("."))+".metaVHC");
            hydroScalingAPI.io.CRasToEsriASCII exporter=new hydroScalingAPI.io.CRasToEsriASCII(theMetaFile,dirOutput);
            hydroScalingAPI.subGUIs.widgets.HydroOpenDialog openVhc=new hydroScalingAPI.subGUIs.widgets.HydroOpenDialog(this,new hydroScalingAPI.io.MetaRaster(theMetaFile),"Export");
            openVhc.setVisible(true);
            if (openVhc.mapsSelected()){
                for (int i=0;i<openVhc.getSelectedMetaRasters().length;i++){
                    exporter.fileToExport(openVhc.getSelectedMetaRasters()[i]);
                    exporter.writeEsriFile();
                }
            }
            setUpGUI(true);
        }catch(java.io.IOException ioe){
            System.err.println("Failed during ESRI-ASCII file import");
            ioe.printStackTrace();
        }
    }//GEN-LAST:event_exportHydroToGRIDActionPerformed
        
    private void importHydroFromBIL_USGSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importHydroFromBIL_USGSActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser();
        fcI.setFileSelectionMode(fcI.DIRECTORIES_ONLY);
        fcI.setMultiSelectionEnabled(true);
        fcI.setDialogTitle("Select Folder(s) containing Bil Formatted Files");
        int result=fcI.showDialog(this,"Select");
        java.io.File[] dirInput = fcI.getSelectedFiles();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersDemPath);
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result = fcO.showDialog(this,"Select");
        
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        
        try{
            new hydroScalingAPI.io.BilToCRas(dirInput,dirOutput,1);
            setUpGUI(true);
        }catch(java.io.IOException ioe){
            System.err.println("Failed during BIL file import");
            System.err.println(ioe);
        }
    }//GEN-LAST:event_importHydroFromBIL_USGSActionPerformed
    
    private void licenseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_licenseActionPerformed
        new hydroScalingAPI.mainGUI.widgets.License(this).setVisible(true);
    }//GEN-LAST:event_licenseActionPerformed
    
    private void importHydroFromGRIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importHydroFromGRIDActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser();
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("ESRI-ASCII File");
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersHydPath);
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result=fcO.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        if (dirOutput == null) return;
        
        try{
            new hydroScalingAPI.io.EsriASCIIToCRas(fileInput,dirOutput,1);
            setUpGUI(true);
        }catch(java.io.IOException ioe){
            System.err.println("Failed during ESRI-ASCII file import");
            ioe.printStackTrace();
        }
    }//GEN-LAST:event_importHydroFromGRIDActionPerformed
    
    private void importDemFromGRIDActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importDemFromGRIDActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser();
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("ESRI-ASCII File");
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersDemPath);
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result=fcO.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        if (dirOutput == null) return;
        
        try{
            new hydroScalingAPI.io.EsriASCIIToCRas(fileInput,dirOutput,0);
            setUpGUI(true);
        }catch(java.io.IOException ioe){
            System.err.println("Failed during ESRI-ASCII file import");
            ioe.printStackTrace();
        }
    }//GEN-LAST:event_importDemFromGRIDActionPerformed
    
    private void exportDemToGRASSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportDemToGRASSActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersDemPath);
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("DEM File");
        javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("metaDEM","Digital Elevation Model");
        fcI.addChoosableFileFilter(mdtFilter);
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser();
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result = fcO.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        if (dirOutput == null) return;
        
        try{
            hydroScalingAPI.io.CRasToGrass exporter=new hydroScalingAPI.io.CRasToGrass(fileInput,dirOutput);
            hydroScalingAPI.subGUIs.widgets.DemOpenDialog openDem=new hydroScalingAPI.subGUIs.widgets.DemOpenDialog(this,new hydroScalingAPI.io.MetaRaster(fileInput),"Export");
            openDem.setVisible(true);
            if (openDem.mapsSelected()){
                for (int i=0;i<openDem.getSelectedMetaRasters().length;i++){
                    exporter.fileToExport(openDem.getSelectedMetaRasters()[i]);
                    exporter.writeGrassFile();
                }
            }
        }catch(java.io.IOException ioe){
            System.err.println("Failed during GRASS file import");
            ioe.printStackTrace();
        }
        
    }//GEN-LAST:event_exportDemToGRASSActionPerformed
    
    private void importDemFromGRASSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importDemFromGRASSActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser();
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("Grass File");
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersDemPath);
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result=fcO.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        if (dirOutput == null) return;
        
        try{
            new hydroScalingAPI.io.GrassToCRas(fileInput,dirOutput,0);
            setUpGUI(true);
        }catch(java.io.IOException ioe){
            System.err.println("Failed during GRASS file import");
            ioe.printStackTrace();
        }
    }//GEN-LAST:event_importDemFromGRASSActionPerformed
    
    private void openDatabaseButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDatabaseButtonActionPerformed
        openDBActionPerformed(null);
    }//GEN-LAST:event_openDatabaseButtonActionPerformed
    
    private void openHydroButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openHydroButtonActionPerformed
        openHydroActionPerformed(null);
    }//GEN-LAST:event_openHydroButtonActionPerformed
    
    private void openDEMButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDEMButtonActionPerformed
        openDEMActionPerformed(null);
    }//GEN-LAST:event_openDEMButtonActionPerformed
    
    private void createDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createDBActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser();
        fcI.setFileSelectionMode(fcI.DIRECTORIES_ONLY);
        fcI.setDialogTitle("Select Database destination (Empty Directory)");
        int result = fcI.showDialog(this,"Select");
        java.io.File dirOutput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        new java.io.File(dirOutput.getPath()+"/Rasters/Topography/").mkdirs();
        new java.io.File(dirOutput.getPath()+"/Rasters/Hydrology").mkdirs();
        new java.io.File(dirOutput.getPath()+"/Vectors/").mkdirs();
        new java.io.File(dirOutput.getPath()+"/Polygons/").mkdirs();
        new java.io.File(dirOutput.getPath()+"/Lines/").mkdirs();
        new java.io.File(dirOutput.getPath()+"/Sites/Gauges/").mkdirs();
        new java.io.File(dirOutput.getPath()+"/Sites/Locations/").mkdirs();
        new java.io.File(dirOutput.getPath()+"/Documentation/").mkdirs();
        hydroScalingAPI.tools.FileManipulation.CopyFile(new java.io.File(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/images/defaultDB.jpg").getPath()),new java.io.File(dirOutput.getPath()+"/logo.jpg"));
        try{
            java.io.File dbNameFile=new java.io.File(dirOutput.getPath()+"/name.txt");
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(dbNameFile));
            metaBuffer.write("Unamed Database"+"\n");
            metaBuffer.close();
        } catch (java.io.IOException ioe){
            System.err.println("Failed creating a new DB");
            System.err.println(ioe);
        }
        
        addToRecentDatabases(dirOutput);
        localInfoManager.checkDataBase();
        setUpGUI(false);
        
    }//GEN-LAST:event_createDBActionPerformed
    
    private void importDemFromBIL_USGSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importDemFromBIL_USGSActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser();
        fcI.setFileSelectionMode(fcI.DIRECTORIES_ONLY);
        fcI.setMultiSelectionEnabled(true);
        fcI.setDialogTitle("Select Folder(s) containing GridFloat Files");
        int result=fcI.showDialog(this,"Select");
        java.io.File[] dirInput = fcI.getSelectedFiles();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersDemPath);
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result = fcO.showDialog(this,"Select");
        
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        
        try{
            new hydroScalingAPI.io.BilToCRas(dirInput,dirOutput,0);
            setUpGUI(true);
        }catch(java.io.IOException ioe){
            System.err.println("Failed during BIL file import");
            System.err.println(ioe);
        }
    }//GEN-LAST:event_importDemFromBIL_USGSActionPerformed
    
    private void addLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addLocationActionPerformed
        hydroScalingAPI.subGUIs.widgets.LocationsEditor theEditor=new hydroScalingAPI.subGUIs.widgets.LocationsEditor(this);
        theEditor.setVisible(true);
        
        if(theEditor.wroteNewLocation()){
            String[] locInfo=theEditor.writtenOrModifiedLocation();
            java.util.List currentSelectedLocations=getActiveLocations();
            hydroScalingAPI.io.MetaLocation newLocation = localLocationsManager.getLocation(locInfo[0],locInfo[1]);
            java.util.Vector theNewVector=new java.util.Vector();
            for(int i=0;i<currentSelectedLocations.size();i++){
                theNewVector.add(currentSelectedLocations.get(i));
            }
            theNewVector.add(newLocation);
            activeLocationsContainer.setListData(theNewVector);
            refreshAllWindowsReferences();
        }
    }//GEN-LAST:event_addLocationActionPerformed
    
    private void addGaugeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addGaugeActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_addGaugeActionPerformed
    
    private void editLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLocationActionPerformed
        java.util.List toView=activeLocationsContainer.getSelectedValuesList();
        if (toView.size() > 0) {
            new hydroScalingAPI.subGUIs.widgets.LocationsEditor(this,toView.get(0)).setVisible(true);
        }
    }//GEN-LAST:event_editLocationActionPerformed
    
    private void editGaugeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editGaugeActionPerformed
        // Add your handling code here:
    }//GEN-LAST:event_editGaugeActionPerformed
    
    private void showLocationNameActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLocationNameActionPerformed
        if (activeLocationsContainer.getModel().getSize() > 0) {
            refreshAllWindowsReferences();
        }
    }//GEN-LAST:event_showLocationNameActionPerformed
    
    private void showGaugeCodeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showGaugeCodeActionPerformed
        if (activeGaugesContainer.getModel().getSize() > 0){
            refreshAllWindowsReferences();
        }
    }//GEN-LAST:event_showGaugeCodeActionPerformed
    
    private void clearLocationsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearLocationsActionPerformed
        activeLocationsContainer.setListData(new java.util.Vector());
        refreshAllWindowsReferences();
    }//GEN-LAST:event_clearLocationsActionPerformed
    
    private void clearGaugesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_clearGaugesActionPerformed
        activeGaugesContainer.setListData(new java.util.Vector());
        refreshAllWindowsReferences();
    }//GEN-LAST:event_clearGaugesActionPerformed
    
    private void networkExtractionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_networkExtractionActionPerformed
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersDemPath);
        fc.setFileSelectionMode(fc.FILES_ONLY);
        fc.setDialogTitle("DEM Selection");
        javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("metaDEM","Digital Elevation Model");
        fc.addChoosableFileFilter(mdtFilter);
        int result = fc.showOpenDialog(this);
        
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fc.getSelectedFile().isFile()) return;
        
        try{
            hydroScalingAPI.io.MetaRaster metaDEM= new hydroScalingAPI.io.MetaRaster(fc.getSelectedFile());
            String demPath=fc.getSelectedFile().getPath();
            demPath=demPath.substring(0, demPath.lastIndexOf("metaDEM"))+"dem";
            metaDEM.setLocationBinaryFile(new java.io.File(demPath));
            
            hydroScalingAPI.io.DataRaster dataDEM = new hydroScalingAPI.io.DataRaster(metaDEM);
            new hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule(this, metaDEM, dataDEM);
            openDEM(fc.getSelectedFile());
        } catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }
        
    }//GEN-LAST:event_networkExtractionActionPerformed
    
    private void launchLocationViewerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_launchLocationViewerActionPerformed
        java.util.List toView=activeLocationsContainer.getSelectedValuesList();
        if (toView.size() > 0) new hydroScalingAPI.subGUIs.widgets.LocationsViewer(this,toView).setVisible(true);
    }//GEN-LAST:event_launchLocationViewerActionPerformed
    
    private void locationsToDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_locationsToDisplayActionPerformed
        hydroScalingAPI.subGUIs.widgets.LocationsOpenDialog locationsSelector=new hydroScalingAPI.subGUIs.widgets.LocationsOpenDialog(this);
        locationsSelector.setVisible(true);
        if(locationsSelector.isCanceled()) return;
        activeLocationsContainer.setListData(locationsSelector.getSelectedLocations());
        showLocationName.setSelected(locationsSelector.showNames());
        refreshAllWindowsReferences();
    }//GEN-LAST:event_locationsToDisplayActionPerformed
    
    private void gaugesToDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gaugesToDisplayActionPerformed
        hydroScalingAPI.subGUIs.widgets.GaugesOpenDialog gaugesSelector=new hydroScalingAPI.subGUIs.widgets.GaugesOpenDialog(this);
        gaugesSelector.setVisible(true);
        if(gaugesSelector.isCanceled()) return;
        activeGaugesContainer.setListData(gaugesSelector.getSelectedGauges());
        showGaugeCode.setSelected(gaugesSelector.showNames());
        refreshAllWindowsReferences();
    }//GEN-LAST:event_gaugesToDisplayActionPerformed
    
    private void openDBActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDBActionPerformed
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser("/");
        fc.setFileSelectionMode(fc.DIRECTORIES_ONLY);
        fc.setDialogTitle("Database root directory");
        int result = fc.showDialog(this,"Select");
        java.io.File selectedFile = fc.getSelectedFile();
        
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        addToRecentDatabases(selectedFile);
        localInfoManager.checkDataBase();
        setUpGUI(false);
    }//GEN-LAST:event_openDBActionPerformed
    
    private void openHydroActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openHydroActionPerformed
        
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser("/");
        fc.setFileSelectionMode(fc.FILES_ONLY);
        fc.setDialogTitle("Hydroclimatic File Selection");
        javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("metaVHC","Hydroclimatic Information");
        fc.addChoosableFileFilter(mdtFilter);
        int result = fc.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fc.getSelectedFile().isFile()) return;
        
        openVHC(fc.getSelectedFile());
        
    }//GEN-LAST:event_openHydroActionPerformed
    
    private void openDEMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openDEMActionPerformed
        
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser("/");
        fc.setFileSelectionMode(fc.FILES_ONLY);
        fc.setDialogTitle("DEM Selection");
        javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("metaDEM","Digital Elevation Model");
        fc.addChoosableFileFilter(mdtFilter);
        int result = fc.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fc.getSelectedFile().isFile()) return;
        
        openDEM(fc.getSelectedFile());
        
    }//GEN-LAST:event_openDEMActionPerformed
    
    private void hydroTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hydroTreeMouseClicked
        
        javax.swing.tree.TreePath selPath = hydroTree.getPathForLocation(evt.getX(), evt.getY());
        if (selPath == null) return;
        String fileToDisplay=(String) selPath.getLastPathComponent().toString();
        if(fileToDisplay.lastIndexOf("metaVHC") != -1) {
            Object[] directions=selPath.getPath();
            fileToDisplay=localInfoManager.dataBaseRastersHydPath.getPath();
            for(int i=1;i<selPath.getPathCount();i++) fileToDisplay+=("/"+directions[i].toString());

            java.io.File selectedFile=new java.io.File(fileToDisplay);

            localInfoManager.metaFileActive=selectedFile;

            if(evt.getButton() == 1){

                if(evt.getClickCount() == 1) {
                    displayMetaFile(selectedFile);
                } else if(evt.getClickCount() == 2) {
                    openVHC(selectedFile);
                }
            }

            if(evt.getButton() == 3){
                mapToolsPopup.show(evt.getComponent(),evt.getX(),evt.getY());
            }

        }
        
    }//GEN-LAST:event_hydroTreeMouseClicked
    
    private void topoTreeMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_topoTreeMouseClicked
        
        javax.swing.tree.TreePath selPath = topoTree.getPathForLocation(evt.getX(), evt.getY());
        if (selPath == null) return;
        String fileToDisplay=(String) selPath.getLastPathComponent().toString();
        if(fileToDisplay.lastIndexOf("metaDEM") != -1) {
            Object[] directions=selPath.getPath();
            fileToDisplay=localInfoManager.dataBaseRastersDemPath.getPath();
            for(int i=1;i<selPath.getPathCount();i++) fileToDisplay+=("/"+directions[i].toString());

            java.io.File selectedFile=new java.io.File(fileToDisplay);
            
            localInfoManager.metaFileActive=selectedFile;

            if(evt.getButton() == 1){

                if(evt.getClickCount() == 1) {
                    displayMetaFile(selectedFile);
                } else if(evt.getClickCount() == 2) {
                    openDEM(selectedFile);
                }
            }

            if(evt.getButton() == 3){
                mapToolsPopup.show(evt.getComponent(),evt.getX(),evt.getY());
            }

        }
        
    }//GEN-LAST:event_topoTreeMouseClicked
    
    private void displayMetaFile(java.io.File selectedFile){
        
        metaFileViewer=new javax.swing.JTextArea();
        metaFileViewer.setWrapStyleWord(true);
        metaFileViewer.setLineWrap(true);
        metaFileViewer.setFont(new java.awt.Font("Arial", 0, 10));
        
        try{
            java.io.FileReader reader = new java.io.FileReader(selectedFile);
            java.io.BufferedReader buffer=new java.io.BufferedReader(reader);
            
            String line;
            metaFileViewer.removeAll();
            while ((line=buffer.readLine()) != null) {
                metaFileViewer.append(line+"\n");
            }
            buffer.close();
            
        } catch (java.io.IOException IOE){
            System.err.println("Error Loading Meta-Information for "+selectedFile);
            System.err.println(IOE);
        }
        
        jScrollPane4.setViewportView(metaFileViewer);
        
        jScrollPane4.updateUI();
        
    }
    
    private void quitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_quitActionPerformed
        
        exitForm(null);
        
    }//GEN-LAST:event_quitActionPerformed
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        localInfoManager.setBounds(getBounds());
        localInfoManager.writePreferences();
        System.exit(0);
    }//GEN-LAST:event_exitForm

    private void importDemFromFloatBIL_USGSActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importDemFromFloatBIL_USGSActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser();
        fcI.setFileSelectionMode(fcI.DIRECTORIES_ONLY);
        fcI.setMultiSelectionEnabled(true);
        fcI.setDialogTitle("Select Folder(s) containing GridFloat Files");
        int result=fcI.showDialog(this,"Select");
        java.io.File[] dirInput = fcI.getSelectedFiles();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        javax.swing.JFileChooser fcO=new javax.swing.JFileChooser(localInfoManager.dataBaseRastersDemPath);
        fcO.setFileSelectionMode(fcO.DIRECTORIES_ONLY);
        fcO.setDialogTitle("Output Directory");
        result = fcO.showDialog(this,"Select");
        
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        
        java.io.File dirOutput = fcO.getSelectedFile();
        try{
            new hydroScalingAPI.io.FloatBilToCRas(dirInput,dirOutput,0);
            setUpGUI(true);
        }catch(java.io.IOException ioe){
            System.err.println("Failed during BIL file import");
            System.err.println(ioe);
        }
    }//GEN-LAST:event_importDemFromFloatBIL_USGSActionPerformed

    private void swmm_raindata_importerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swmm_raindata_importerActionPerformed
        hydroScalingAPI.modules.rainDataImporter.widgets.ImportCSVRainfallToStormEventDialog csvImpDialog;
        java.io.File lastOpenedDataBaseFile;
        Object lastOpenedDataBase;
        
        lastOpenedDataBase = localInfoManager.recentDataBases.firstElement();
        
        if(lastOpenedDataBase instanceof java.io.File){
            lastOpenedDataBaseFile = (java.io.File)lastOpenedDataBase;
        } else {
            lastOpenedDataBaseFile = null;
        }
        
        csvImpDialog = new hydroScalingAPI.modules.rainDataImporter.widgets.ImportCSVRainfallToStormEventDialog(this, true);
        
        csvImpDialog.setVisible(true);
    }//GEN-LAST:event_swmm_raindata_importerActionPerformed

    private void jMenuItem4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem4ActionPerformed
        hydroScalingAPI.modules.swmmCoupling.widgets.SwmmFileSketcherDialog swmmCoupWindow;
        java.io.File lastOpenedDataBaseFile;
        Object lastOpenedDataBase;
        
        lastOpenedDataBase = localInfoManager.recentDataBases.firstElement();
        
        if(lastOpenedDataBase instanceof java.io.File){
            lastOpenedDataBaseFile = (java.io.File)lastOpenedDataBase;
        } else {
            lastOpenedDataBaseFile = null;
        }
        
        swmmCoupWindow = new hydroScalingAPI.modules.swmmCoupling.widgets.SwmmFileSketcherDialog(this, true, lastOpenedDataBaseFile);
        
        //subGUIs_container.add(swmmCoupWindow, javax.swing.JLayeredPane.DEFAULT_LAYER);
        
        swmmCoupWindow.setVisible(true);
    }//GEN-LAST:event_jMenuItem4ActionPerformed

    private void jMenuItem5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem5ActionPerformed
        hydroScalingAPI.modules.swmmCoupling.widgets.FlowDataImporterDialog inflowImporter;
        
        inflowImporter = new hydroScalingAPI.modules.swmmCoupling.widgets.FlowDataImporterDialog(this, true);
        
        inflowImporter.setVisible(true);
    }//GEN-LAST:event_jMenuItem5ActionPerformed

    private void jMenuItem6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem6ActionPerformed
        hydroScalingAPI.modules.swmmCoupling.subGUIs.widgets.DemViewer2D_subBasin theViewer;
        hydroScalingAPI.io.MetaRaster masterRaster;

        if (localInfoManager.metaFileActive == null){
            javax.swing.JOptionPane.showMessageDialog(this, "Must select a DEM in left tab, Raster > Topography Database.");
        }
        
        try {
            masterRaster = new hydroScalingAPI.io.MetaRaster(localInfoManager.metaFileActive);
            this.openSubBasinDEM(masterRaster);
        } catch (RemoteException exp) {
            System.err.println("RemoteException: " + exp.getMessage());
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
        }
    }//GEN-LAST:event_jMenuItem6ActionPerformed
    
    /**
     * Creates a new instance of the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        new ParentGUI().setVisible(true);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem about;
    private javax.swing.JList activeGaugesContainer;
    private javax.swing.JList activeLocationsContainer;
    private javax.swing.JButton addDlgFile;
    private javax.swing.JButton addGauge;
    private javax.swing.JButton addLocation;
    private javax.swing.JButton addPolygonFile;
    private javax.swing.JButton addShapeFile;
    private javax.swing.JMenuItem arrange;
    private javax.swing.JButton calcButton;
    private javax.swing.JButton clearGauges;
    private javax.swing.JButton clearLocations;
    private javax.swing.JMenuItem colors;
    private javax.swing.JMenuItem contents;
    private javax.swing.JMenuItem createDB;
    private javax.swing.JTabbedPane dataBaseTabs;
    private javax.swing.JTextField dbName;
    private javax.swing.JPopupMenu dbProps;
    private javax.swing.JPanel dlgsContainer;
    private javax.swing.JPanel dlgsPanel;
    private javax.swing.JPanel documents;
    private javax.swing.JMenuItem editDBName;
    private javax.swing.JButton editGauge;
    private javax.swing.JButton editLocation;
    private javax.swing.JButton editorButton;
    private javax.swing.JMenu exportDEM;
    private javax.swing.JMenuItem exportDemToGRASS;
    private javax.swing.JMenuItem exportDemToGRID;
    private javax.swing.JMenu exportHydro;
    private javax.swing.JMenuItem exportHydroToGRASS;
    private javax.swing.JMenuItem exportHydroToGRID;
    private javax.swing.JMenuItem exportHydroToNetCDF;
    private javax.swing.JMenu exportSites;
    private javax.swing.JMenuItem exportSitesToAscii;
    private javax.swing.JMenu file;
    private javax.swing.JPanel gaugesPanel;
    private javax.swing.JButton gaugesToDisplay;
    private javax.swing.JTextField gui_info;
    private javax.swing.JMenu help;
    private javax.swing.JButton helpButton;
    private javax.swing.JTree hydroTree;
    private javax.swing.JMenu importDEM;
    private javax.swing.JMenuItem importDemFromBIL_USGS;
    private javax.swing.JMenuItem importDemFromFloatBIL_USGS;
    private javax.swing.JMenuItem importDemFromGRASS;
    private javax.swing.JMenuItem importDemFromGRID;
    private javax.swing.JButton importDlgFile;
    private javax.swing.JMenu importHydro;
    private javax.swing.JMenuItem importHydroFromBIL_USGS;
    private javax.swing.JMenuItem importHydroFromGRASS;
    private javax.swing.JMenuItem importHydroFromGRID;
    private javax.swing.JMenuItem importHydroFromNetCDF;
    private javax.swing.JButton importPolygonFile;
    private javax.swing.JButton importShapeFile;
    private javax.swing.JMenu importSites;
    private javax.swing.JMenuItem importSitesFromGazetteer;
    private javax.swing.JMenuItem importSitesFromWaterUSGS;
    private javax.swing.JMenuItem index;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenu jMenuExport;
    private javax.swing.JMenu jMenuImport;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JMenuItem jMenuItem4;
    private javax.swing.JMenuItem jMenuItem5;
    private javax.swing.JMenuItem jMenuItem6;
    private javax.swing.JMenu jMenuRecentDB;
    private javax.swing.JMenu jMenuRecentFiles;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane21;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JScrollPane jScrollPane5;
    private javax.swing.JScrollPane jScrollPane6;
    private javax.swing.JScrollPane jScrollPane7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator10;
    private javax.swing.JSeparator jSeparator11;
    private javax.swing.JSeparator jSeparator12;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSeparator jSeparator5;
    private javax.swing.JSeparator jSeparator6;
    private javax.swing.JSeparator jSeparator7;
    private javax.swing.JSeparator jSeparator8;
    private javax.swing.JSeparator jSeparator9;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JToolBar jToolBar3;
    private javax.swing.JButton launchLocationViewer;
    private javax.swing.JButton launchTSAnalyzer;
    private javax.swing.JMenuItem license;
    private javax.swing.JPanel lines;
    private javax.swing.JPanel locationsPanel;
    private javax.swing.JButton locationsToDisplay;
    private javax.swing.JMenuItem mapCalc;
    private javax.swing.JMenuItem mapEditorItem;
    private javax.swing.JPopupMenu mapToolsPopup;
    private javax.swing.JTextArea metaFileViewer;
    private javax.swing.JMenu modules;
    private javax.swing.JMenuItem networkAnalysis;
    private javax.swing.JMenuItem networkExtraction;
    private javax.swing.JMenuItem openDB;
    private javax.swing.JMenuItem openDEM;
    private javax.swing.JButton openDEMButton;
    private javax.swing.JButton openDatabaseButton;
    private javax.swing.JMenu openFile;
    private javax.swing.JMenuItem openHydro;
    private javax.swing.JButton openHydroButton;
    private javax.swing.JMenuItem openVector;
    private javax.swing.JMenu options;
    private javax.swing.JPanel polygonContainer;
    private javax.swing.JPanel polygonPanel;
    private javax.swing.JPanel polygons;
    private javax.swing.JMenuItem preferences;
    private javax.swing.JMenuItem quit;
    private javax.swing.JMenuItem rain_simulator;
    private javax.swing.JMenuItem rainfall_runoff_simulator;
    private javax.swing.JPanel rastersTab;
    private javax.swing.JPanel shapesContainer;
    private javax.swing.JPanel shapesPanel;
    private javax.swing.JCheckBox showGaugeCode;
    private javax.swing.JToggleButton showHelpButton;
    private javax.swing.JCheckBox showLocationName;
    private javax.swing.JPanel sites;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JDesktopPane subGUIs_container;
    private javax.swing.JMenu swmm_coupling;
    private javax.swing.JMenuItem swmm_raindata_importer;
    private javax.swing.JMenuItem tRIBS_input;
    private javax.swing.JMenuItem tRIBS_output;
    private javax.swing.JMenu tools;
    private javax.swing.JTree topoTree;
    private javax.swing.JMenuItem ts_analysis;
    private javax.swing.JPanel vectors;
    private javax.swing.JMenuItem waterBalance;
    private javax.swing.JMenu window;
    // End of variables declaration//GEN-END:variables
    
    
    /**
     * Responds to the user menu File -> Open File -> Open DEM
     * @param selectedFile The metaDEM file describing the DEM
     */
    public void openDEM(java.io.File selectedFile){
        try{
            hydroScalingAPI.io.MetaRaster metaDEM=new  hydroScalingAPI.io.MetaRaster(selectedFile);
            hydroScalingAPI.subGUIs.widgets.DemOpenDialog openDem=new hydroScalingAPI.subGUIs.widgets.DemOpenDialog(this,metaDEM);
            openDem.setVisible(true);
            
            if (openDem.mapsSelected()){
                addToRecentFiles(selectedFile);
                
                for (int i=0;i<openDem.getSelectedMetaRasters().length;i++){

                    hydroScalingAPI.io.MetaRaster subDemMetaDEM=openDem.getSelectedMetaRasters()[i];

                    if(openDem.is3D()){
                        
                        hydroScalingAPI.subGUIs.widgets.DemViewer3D thisDemViewer=new hydroScalingAPI.subGUIs.widgets.DemViewer3D(this,subDemMetaDEM,openDem.getRelatedMaps());
                        subGUIs_container.add(thisDemViewer, javax.swing.JLayeredPane.DEFAULT_LAYER);
                        try{
                            thisDemViewer.setSelected(true);
                        } catch (java.beans.PropertyVetoException PVE){
                            System.err.println(PVE);
                        }

                        thisDemViewer.setBounds(25*(openWindows.size()-1), 25*(openWindows.size()-1), thisDemViewer.getWidth()+5, thisDemViewer.getHeight()+5);
                        
                    } else {
                    
                        hydroScalingAPI.subGUIs.widgets.DemViewer2D thisDemViewer=new hydroScalingAPI.subGUIs.widgets.DemViewer2D(this,subDemMetaDEM,openDem.getRelatedMaps());
                        subGUIs_container.add(thisDemViewer, javax.swing.JLayeredPane.DEFAULT_LAYER);
                        try{
                            thisDemViewer.setSelected(true);
                        } catch (java.beans.PropertyVetoException PVE){
                            System.err.println(PVE);
                        }

                        thisDemViewer.setBounds(25*(openWindows.size()-1), 25*(openWindows.size()-1), thisDemViewer.getWidth()+5, thisDemViewer.getHeight()+5);
                    }
                }
                
            }
        } catch (java.io.IOException ioe){
            System.err.println("Failed building DemViewer2D");
            System.err.println(ioe);
        } catch (visad.VisADException vie){
            System.err.println("Failed building DemViewer2D");
            System.err.println(vie);
        }
        
    }
    
    /**
     * Responds to the user menu File -> Open File -> Open HydroClimatic
     * @param selectedFile The metaVHC file describing the HydroClimatic variable
     */
    public void openVHC(java.io.File selectedFile){
        try{
            hydroScalingAPI.io.MetaRaster metaVHC=new  hydroScalingAPI.io.MetaRaster(selectedFile);
            hydroScalingAPI.subGUIs.widgets.HydroOpenDialog openVhc=new hydroScalingAPI.subGUIs.widgets.HydroOpenDialog(this,metaVHC);
            openVhc.setVisible(true);
            if(openVhc.mapsSelected()){
                addToRecentFiles(selectedFile);
                if(openVhc.isMultiple()){
                } else {

                    for (int i=0;i<openVhc.getSelectedMetaRasters().length;i++){
                        hydroScalingAPI.io.MetaRaster subVhcMetaDEM=openVhc.getSelectedMetaRasters()[i];

                        hydroScalingAPI.subGUIs.widgets.HydroClimateViewer2D thisVhcViewer=new hydroScalingAPI.subGUIs.widgets.HydroClimateViewer2D(this,subVhcMetaDEM,openVhc.getRelatedMaps());

                        subGUIs_container.add(thisVhcViewer, javax.swing.JLayeredPane.DEFAULT_LAYER);
                        try{
                            thisVhcViewer.setSelected(true);
                        } catch (java.beans.PropertyVetoException PVE){
                            System.err.println(PVE);
                        }

                        thisVhcViewer.setBounds(25*(openWindows.size()-1), 25*(openWindows.size()-1), thisVhcViewer.getWidth()+5, thisVhcViewer.getHeight()+5);

                    }
                }
            
            }
            
            
                       
        } catch (java.io.IOException ioe){
            System.err.println("Failed building HydroClimateViewer2D");
            System.err.println(ioe);
        } catch (visad.VisADException vie){
            System.err.println("Failed building HydroClimateViewer2D");
            System.err.println(vie);
        }
    }
    
    private void openRecentFile(java.awt.event.ActionEvent evt){
        
        int fileSelectedIndex=new Integer(((javax.swing.JMenuItem) evt.getSource()).getText().substring(0,1)).intValue();
        java.io.File selectedFile=(java.io.File) localInfoManager.recentFiles.elementAt(fileSelectedIndex);
        String tipoExtencion=selectedFile.getName().substring(selectedFile.getName().lastIndexOf("."),selectedFile.getName().length());
        if (tipoExtencion.equalsIgnoreCase(".metaDEM")){
            openDEM(selectedFile);
        } else {
            openVHC(selectedFile);
        }
    }
    
    private void openRecentDataBase(java.awt.event.ActionEvent evt){
        int dbSelectedIndex=new Integer(((javax.swing.JMenuItem) evt.getSource()).getText().substring(0,1)).intValue();
        Object theDBtoLoad=localInfoManager.recentDataBases.elementAt(dbSelectedIndex);
        localInfoManager.recentDataBases.removeElementAt(dbSelectedIndex) ;
        localInfoManager.recentDataBases.add(0,theDBtoLoad);
        localInfoManager.checkDataBase();
        setUpGUI(false);
    }
    
    private void registerWindow(String windowIdentifier,hydroScalingAPI.subGUIs.widgets.RasterViewer thisViewer){
        final String stringId=thisViewer.getIdentifier();
        openWindows.put(stringId,thisViewer);
        javax.swing.JMenuItem aWindow = new javax.swing.JMenuItem();
        aWindow.setFont(new java.awt.Font("Verdana", 0, 10));
        aWindow.setText(windowIdentifier);
        window.add(aWindow);
        aWindow.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                windowSelected(stringId);
            }
        });
        openWindowsMenusVector.put(stringId,aWindow);
        openWindowsIdentifiers.add(stringId);
    }
    
    private void windowSelected(String stringId){
        javax.swing.JInternalFrame theWindow=(javax.swing.JInternalFrame)openWindows.get(stringId);
        theWindow.toFront();
        try{
            theWindow.setSelected(true);
        } catch (java.beans.PropertyVetoException PVE){
            System.err.println(PVE);
        }
    }
    
    private void refreshAllWindowsReferences(){
        int numOpenWindows=openWindowsIdentifiers.size();
        for (int i=0;i<numOpenWindows;i++){
            hydroScalingAPI.subGUIs.widgets.RasterViewer thisViewer=(hydroScalingAPI.subGUIs.widgets.RasterViewer)openWindows.get(openWindowsIdentifiers.get(i));
            thisViewer.refreshReferences(showGaugeCode.isSelected(),showLocationName.isSelected());
        }
    }
    
    private void showVectorActionPerformed(java.awt.event.ActionEvent evt) {
        refreshAllWindowsReferences();
    }
    
    private void showPolygonActionPerformed(java.awt.event.ActionEvent evt) {
        refreshAllWindowsReferences();
    }
    
    /**
     * Informs the GUI about a newly created Location
     * @param theEditor The {@link hydroScalingAPI.subGUIs.widgets.LocationsEditor} used to create the Location
     */
    public void addNewLocationInteractively(hydroScalingAPI.subGUIs.widgets.LocationsEditor theEditor){
        if(theEditor.wroteNewLocation()){
            String[] locInfo=theEditor.writtenOrModifiedLocation();
            java.util.List currentSelectedLocations=getActiveLocations();
            hydroScalingAPI.io.MetaLocation newLocation = localLocationsManager.getLocation(locInfo[0],locInfo[1]);
            java.util.Vector theNewVector=new java.util.Vector();
            for(int i=0;i<currentSelectedLocations.size();i++){
                theNewVector.add(currentSelectedLocations.get(i));
            }
            theNewVector.add(newLocation);
            activeLocationsContainer.setListData(theNewVector);
            refreshAllWindowsReferences();
        }
    }
    
    /**
     * Unused event
     * @param internalFrameEvent Not Used
     */
    public void internalFrameActivated(javax.swing.event.InternalFrameEvent internalFrameEvent) {
        //Unused Event
    }
    
    /**
     * Unused event
     * @param internalFrameEvent Unused
     */
    public void internalFrameClosing(javax.swing.event.InternalFrameEvent internalFrameEvent) {
        //Unused Event
    }
    
    /**
     * Unused event
     * @param internalFrameEvent Unused
     */
    public void internalFrameDeactivated(javax.swing.event.InternalFrameEvent internalFrameEvent) {
        //Unused Event
    }
    
    /**
     * Unused event
     * @param internalFrameEvent Unused
     */
    public void internalFrameDeiconified(javax.swing.event.InternalFrameEvent internalFrameEvent) {
        //Unused Event
    }
    
    /**
     * Unused event
     * @param internalFrameEvent Unused
     */
    public void internalFrameIconified(javax.swing.event.InternalFrameEvent internalFrameEvent) {
        //Unused Event
    }
    
    /**
     * Informs the GUI that a an internal visualization window has closed.
     * @param internalFrameEvent The Internal frame that was closed
     */
    public void internalFrameClosed(javax.swing.event.InternalFrameEvent internalFrameEvent) {
        if (internalFrameEvent.getInternalFrame() instanceof hydroScalingAPI.subGUIs.widgets.RasterViewer){
            String stringId=((hydroScalingAPI.subGUIs.widgets.RasterViewer)internalFrameEvent.getInternalFrame()).getIdentifier();
            openWindows.remove(stringId);
            window.remove(((javax.swing.JMenuItem)openWindowsMenusVector.get(stringId)));
            openWindowsMenusVector.remove(stringId);
            openWindowsIdentifiers.remove(stringId);
        }
    }
    
    /**
     * Informs the GUI that a an internal visualization window has opened.
     * @param internalFrameEvent The internal frame that was opened
     */
    public void internalFrameOpened(javax.swing.event.InternalFrameEvent internalFrameEvent) {
        if(internalFrameEvent.getInternalFrame() instanceof hydroScalingAPI.subGUIs.widgets.RasterViewer){
            hydroScalingAPI.subGUIs.widgets.RasterViewer rv=(hydroScalingAPI.subGUIs.widgets.RasterViewer)internalFrameEvent.getInternalFrame();
            String windowIdentifier=rv.getTitle();
            rv.setIdentifier(windowIdentifier+Math.random());
        
            registerWindow(windowIdentifier,rv);
        }
    }
    
    /**
     *  <br />
     * Auxiliary method for SWMM-Coupling functionality
     * @param selectedMetaRaster 
     */
    public void openSubBasinDEM(hydroScalingAPI.io.MetaRaster selectedMetaRaster) {
        hydroScalingAPI.io.MetaRaster subDemMetaDEM, metaDEM;
        java.io.File binaryFile, selectedFile;
        java.util.Hashtable tempHashTable;
        String binaryFilePath;
        hydroScalingAPI.subGUIs.widgets.DemOpenDialog openDem;

        // open binary DEM file
        selectedFile = selectedMetaRaster.getLocationMeta();
        binaryFilePath = selectedFile.getAbsolutePath();
        binaryFilePath = binaryFilePath.replace(".metaDEM", ".dem");
        binaryFile = new java.io.File(binaryFilePath);

        try {
            openDem = new hydroScalingAPI.subGUIs.widgets.DemOpenDialog(this, selectedMetaRaster);

            // create meta binary dem
            subDemMetaDEM = new hydroScalingAPI.io.MetaRaster(selectedFile);
            subDemMetaDEM.setLocationBinaryFile(binaryFile);

            addToRecentFiles(selectedFile);

            tempHashTable = this.getRelatedMaps(binaryFilePath);

            hydroScalingAPI.modules.swmmCoupling.subGUIs.widgets.DemViewer2D_subBasin thisDemViewer = 
                    new hydroScalingAPI.modules.swmmCoupling.subGUIs.widgets.DemViewer2D_subBasin(this,
                                                                                                  subDemMetaDEM,
                                                                                                  tempHashTable);
            subGUIs_container.add(thisDemViewer, javax.swing.JLayeredPane.DEFAULT_LAYER);
            try {
                thisDemViewer.setSelected(true);
            } catch (java.beans.PropertyVetoException PVE) {
                System.err.println(PVE);
            }

            thisDemViewer.setBounds(25 * (openWindows.size() - 1), 25 * (openWindows.size() - 1), thisDemViewer.getWidth() + 5, thisDemViewer.getHeight() + 5);
        } catch (java.io.IOException ioe) {
            System.err.println("Failed building DemViewer2D");
            System.err.println(ioe);
        } catch (visad.VisADException vie) {
            System.err.println("Failed building DemViewer2D");
            System.err.println(vie);
        }
    }
    
    /**
     * <br />
     * Auxiliary method for SWMM-Coupling functionality
     * @param datasetName_arg
     * @return 
     */
    public java.util.Hashtable getRelatedMaps(String datasetName_arg) {
        java.util.Hashtable nameToFile = new java.util.Hashtable();
        hydroScalingAPI.mainGUI.objects.GUI_InfoManager infoManager;
        java.io.File demDir;
        String fileName, fileDirPath, filePath;

        final String[] extension = {".dem",
            ".corrDEM",
            ".dir",
            ".areas",
            ".horton",
            ".lcp",
            ".ltc",
            ".gdo",
            ".tdo",
            ".mcd",
            ".tcd",
            ".magn",
            ".slope",
            ".dtopo",
            ".redRas"};

        final String[] derivedName = {" Digital Elevation Model",
            " Fixed Elevation Model",
            " Drainage Directions",
            " Accumulated Area",
            " Horton Numbers",
            " Longest Channel Length",
            " Total Channels Length",
            " Geometric Distance to Border",
            " Topologic Distance to Border",
            " Maximum Channel Drop",
            " Total Channels Drop",
            " Magnitude",
            " Gradient Value",
            " Topologic Diameter",
            " Raster Drainage Network"};

        infoManager = this.getInfoManager();
        demDir = infoManager.dataBaseRastersDemPath;
        fileDirPath = demDir.getAbsolutePath();

        for (int i = 0; i < derivedName.length; i++) {
            fileName = datasetName_arg + extension[i];
            filePath = fileDirPath + java.io.File.separator + fileName;
            nameToFile.put(i, filePath);
            //nameToFile.put((String)elemDeriv.get(i),
            //               demPath+"/"+demName+extension[i]);
        }

        return nameToFile;
    }
}
