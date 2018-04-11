/*
 * TRIBS_io.java
 *
 * Created on January 24, 2007, 1:27 PM
 */

package hydroScalingAPI.modules.tRIBS_io.widgets;


import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;

import geotransform.coords.*;
import geotransform.ellipsoids.*;
import geotransform.transforms.*;

import java.sql.*;
    
/**
 * This module is a graphical interface for creating input and analyzing output
 * for/from the tRIBS program developed at MIT
 * @author Ricardo Mantilla
 */
public class TRIBS_io extends javax.swing.JDialog  implements visad.DisplayListener{
    
    private RealType    xEasting =RealType.getRealType("xEasting"),
                        yNorthing=RealType.getRealType("yNorthing"),
                        nodeColor=RealType.getRealType("nodeColor"),
                        voiColor=RealType.getRealType("voiColor");
    
    private RealTupleType   domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude),
                            espacioXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing,nodeColor}),
                            domainXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing});
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    
    private DisplayImplJ3D display_TIN_I,display_TIN_O,display_NET,display_LULC,display_SOIL,display_GW;

    
    private visad.util.HersheyFont font = new visad.util.HersheyFont("futural");
    
    private ScalarMap eastMap_I,northMap_I,pointsMap_I,voiColorMap_I,
                      eastMap_O,northMap_O,pointsMap_O,voiColorMap_O,
                      eastMap_NET,northMap_NET,
                      eastMap_LULC,northMap_LULC,voiColorMap_LULC,
                      eastMap_SOIL,northMap_SOIL,voiColorMap_SOIL,
                      eastMap_GW,northMap_GW,voiColorMap_GW;
    
    private hydroScalingAPI.util.plot.XYJPanel PpanelRTF;
    private hydroScalingAPI.util.plot.XYJPanel PpanelQOUT;
    private hydroScalingAPI.util.plot.XYJPanel PpanelMRF;
    private hydroScalingAPI.util.plot.XYJPanel PpanelPixel;
    
    private visad.java3d.DisplayRendererJ3D drI,drO,drNET,drLULC,drSOIL,drGW;
    
    private DataReferenceImpl data_refPoints_I,data_refTr_I,data_refPoly_I,data_refFill_I,
                              data_refPoints_O,data_refTr_O,data_refPoly_O,data_refFill_O,data_refNet_O,
                              data_refFill_LULC,data_refFill_SOIL,data_refFill_GW,
                              data_refPolygon_LULC,data_refPolygon_SOIL,data_refPolygon_GW;
    
    private visad.FlatField field_LULC,field_SOIL,field_GW;
    
    private ConstantMap[] pointsCMap_I,trianglesCMap_I,polygonsCMap_I,
                          pointsCMap_O,trianglesCMap_O,polygonsCMap_O,networkCMap_O;
    
    private hydroScalingAPI.modules.tRIBS_io.objects.BasinTIN basTIN_I,basTIN_O;
    
    private hydroScalingAPI.modules.tRIBS_io.objects.BasinNet basNet;
    
    private hydroScalingAPI.modules.tRIBS_io.objects.FileOnlyNetworkManager fonm;
    private hydroScalingAPI.modules.tRIBS_io.objects.FileDynamicManager fdm;
    private hydroScalingAPI.modules.tRIBS_io.objects.FileIntegratedManager fim;
    private hydroScalingAPI.modules.tRIBS_io.objects.FileMrfManager fmrfm;
    private hydroScalingAPI.modules.tRIBS_io.objects.FilePixelManager fpm;
    private hydroScalingAPI.modules.tRIBS_io.objects.FileQoutManager fqm;
    private hydroScalingAPI.modules.tRIBS_io.objects.FileRftManager frftm;
    
    private int lastSelectedTab=0;
    private int standAlone=0;
    
    private java.util.Hashtable landCategories,soilCategories;
    
    private Connection conn;
    
    /**
     * Creates new form TRIBS_io
     * @param parent The main GIS GUI
     * @param x The column number of the basin outlet location
     * @param y The row number of the basin outlet location
     * @param direcc The direction matrix associated to the DEM where the basin is embeded
     * @param order The horton order matrix associated to the DEM where the basin is embeded
     * @param md The MetaRaster associated to the DEM where the basin is embeded
     * @param writePath Path to tRIBS implementation directory
     * @param baseName The base string after which tRIBS files are named
     * @throws java.rmi.RemoteException Captures errors while assigning values to VisAD data objects
     * @throws visad.VisADException Captures errors while creating VisAD objects
     * @throws java.io.IOException Captures errors while reading information
     */
    public TRIBS_io(hydroScalingAPI.mainGUI.ParentGUI parent, int x, int y, byte[][] direcc, byte[][] order, hydroScalingAPI.io.MetaRaster md,java.io.File writePath, String baseName) throws RemoteException, VisADException, java.io.IOException{
        this(parent);
        pathTextField.setText(writePath.getPath());
        baseNameTextField.setText(baseName);
        basTIN_I=new hydroScalingAPI.modules.tRIBS_io.objects.BasinTIN(x,y,direcc,order,md);
        initializeInputTabs();
    }
    
    /**
     * Creates new form TRIBS_io
     * @param parent The main GIS GUI
     * @param outputsDirectory Path to tRIBS output directory
     * @param baseName The base string after which tRIBS output files are named
     * @param flag 0: Called From within Cuencas 2: Only Visualize 3: Only Data Export
     * @throws java.rmi.RemoteException Captures errors while assigning values to VisAD data objects
     * @throws visad.VisADException Captures errors while creating VisAD objects
     * @throws java.io.IOException Captures errors while reading information
     */
    public TRIBS_io(hydroScalingAPI.mainGUI.ParentGUI parent, java.io.File outputsDirectory, String baseName,int flag) throws RemoteException, VisADException, java.io.IOException{
        this(parent);
        
        standAlone=flag;
        
        //Loading the Postgresql driver
        
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
        
        //Determine if a connection can be established from this machine
        
        String url = "jdbc:postgresql://edacdata1.unm.edu/test";
        java.util.Properties props = new java.util.Properties();
        props.setProperty("user","ricardo");
        props.setProperty("password","Ric@rd09");
        props.setProperty("loginTimeout","2");
        
        try {
            
            Connection connT = DriverManager.getConnection(url, props);
            connT.close();
            
        } catch (SQLException ex) {
            System.out.println("Connection to EDAC servers failed");
            if(flag == 3) {
                System.out.println("This System is not authorized to upload data direclty into the EDAC servers");
                System.exit(0);
            }
        }
        
        panel_IO.setSelectedIndex(1);
        pathTextField.setText(outputsDirectory.getAbsolutePath());
        baseNameTextField.setText(baseName);
        System.out.println(">>Loading TIN");
        basTIN_O=new hydroScalingAPI.modules.tRIBS_io.objects.BasinTIN(parent,findTriEdgNodes(outputsDirectory),baseName);
        System.out.println(">>Loading Reaches");
        basNet=new hydroScalingAPI.modules.tRIBS_io.objects.BasinNet(parent,findTriEdgNodes(outputsDirectory),findCNTRL(outputsDirectory),baseName,basTIN_O.minX,basTIN_O.minY);
        System.out.println(">>Loading Qouts");
        fqm=new hydroScalingAPI.modules.tRIBS_io.objects.FileQoutManager(findQouts(outputsDirectory),basTIN_O.getOutletNode());
        System.out.println(">>Loading MRF");
        fmrfm=new hydroScalingAPI.modules.tRIBS_io.objects.FileMrfManager(findMRF(outputsDirectory),fqm.getMaxTime());
        System.out.println(">>Loading RFT");
        frftm=new hydroScalingAPI.modules.tRIBS_io.objects.FileRftManager(findRFT(outputsDirectory),fqm.getMaxTime());
        System.out.println(">>Loading Pixel Files");
        java.io.File[] listOfPixelFiles=findPixel(outputsDirectory);
        if(listOfPixelFiles != null && listOfPixelFiles.length > 0) fpm=new hydroScalingAPI.modules.tRIBS_io.objects.FilePixelManager(listOfPixelFiles);
        System.out.println(">>Loading Integrated Files");
        fim=new hydroScalingAPI.modules.tRIBS_io.objects.FileIntegratedManager(findIntegratedOutput(outputsDirectory),basTIN_O.getNumVoi());
        System.out.println(">>Loading Dynamic Files");
        fdm=new hydroScalingAPI.modules.tRIBS_io.objects.FileDynamicManager(findDynamicOutput(outputsDirectory),basTIN_O.getNumVoi());
        System.out.println(">>Loading Only Network Files");
        fonm=new hydroScalingAPI.modules.tRIBS_io.objects.FileOnlyNetworkManager(findTriEdgNodes(outputsDirectory),basTIN_O.getNumVoi(),baseName);
        

        System.out.println(">>Testing Connection to EDAC - Database");

        try {
            
            conn = DriverManager.getConnection(url, props);
            
            initializeDataExportTab();
            
        } catch (SQLException ex) {
            System.out.println("Connection to EDAC servers failed");
            panelOutputs.remove(jPanel35);
        }
        
        if(flag != 3){
            System.out.println(">>Initializing Interface");
            initializeOutputTabs();
        } else {
            panelOutputs.remove(jPanel24);
            panelOutputs.remove(jPanel16);
            panelOutputs.remove(jPanel26);
            panelOutputs.remove(jPanel17);
            panelOutputs.remove(jPanel30);
        }
    }
    
    /**
     * Creates new form TRIBS_io
     * @param parent The main GIS GUI
     * @throws java.rmi.RemoteException Captures errors while assigning values to VisAD data objects
     * @throws visad.VisADException Captures errors while creating VisAD objects
     * @throws java.io.IOException Captures errors while reading information
     */
    public TRIBS_io(hydroScalingAPI.mainGUI.ParentGUI parent) throws RemoteException, VisADException, java.io.IOException{
        super(parent, false);
        initComponents();
        panelOutputs.remove(jPanel34);
        
        mainFrame=parent;
        
        //Set up general interface
        setTitle("tRIBS Visualization Modules");
        setBounds(0,0, 950, 730);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        //Graphical structure for aggregated response
        PpanelRTF = 
                new hydroScalingAPI.util.plot.XYJPanel( "Runoff Mechanism Response", "Simulation time [h]" , "Runoff [m^3/s]");
        PpanelQOUT = 
                new hydroScalingAPI.util.plot.XYJPanel( "Stream Discharge and Stage", "Simulation time [h]" , "Discharge [m^3/s] / Stage [m]");
        PpanelMRF = 
                new hydroScalingAPI.util.plot.XYJPanel( "Basin-Averaged Response", "Simulation time [h]" , "Value");
        PpanelPixel = 
                new hydroScalingAPI.util.plot.XYJPanel( "Voronoi Node Temporal Response", "Simulation time [h]" , "Value");
        
        rftPanel.add("Center",PpanelRTF);
        qoutPanel.add("Center",PpanelQOUT);
        mrfPanel.add("Center",PpanelMRF);
        pixelPanel.add("Center",PpanelPixel);
        
        //Graphical structure for input triangulated points
        drI=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_TIN_I = new DisplayImplJ3D("display_TIN_I",drI);
        
        GraphicsModeControl dispGMC = (GraphicsModeControl) display_TIN_I.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap_I = new ScalarMap( xEasting , Display.XAxis );
        eastMap_I.setScalarName("East Coordinate");
        northMap_I = new ScalarMap( yNorthing , Display.YAxis );
        northMap_I.setScalarName("North Coordinate");
        pointsMap_I=new ScalarMap( nodeColor , Display.RGB );

        display_TIN_I.addMap(eastMap_I);
        display_TIN_I.addMap(northMap_I);
        display_TIN_I.addMap(pointsMap_I);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display_TIN_I);
        
        jPanel12.add("Center",display_TIN_I.getComponent());
        
        
        //Graphical structure for input land cover
        drLULC=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_LULC = new DisplayImplJ3D("display_LULC",drLULC);
        
        dispGMC = (GraphicsModeControl) display_LULC.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap_LULC = new ScalarMap( xEasting , Display.XAxis );
        eastMap_LULC.setScalarName("East Coordinate");
        northMap_LULC = new ScalarMap( yNorthing , Display.YAxis );
        northMap_LULC.setScalarName("North Coordinate");
        voiColorMap_LULC=new ScalarMap( voiColor , Display.RGB );
        voiColorMap_LULC.setRange(0,10);

        display_LULC.addMap(eastMap_LULC);
        display_LULC.addMap(northMap_LULC);
        display_LULC.addMap(voiColorMap_LULC);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display_LULC);
        
        jPanel6.add("Center",display_LULC.getComponent());
        
        
        //Graphical structure for input soils
        drSOIL=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_SOIL = new DisplayImplJ3D("display_SOIL",drSOIL);
        
        dispGMC = (GraphicsModeControl) display_SOIL.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap_SOIL = new ScalarMap( xEasting , Display.XAxis );
        eastMap_SOIL.setScalarName("East Coordinate");
        northMap_SOIL = new ScalarMap( yNorthing , Display.YAxis );
        northMap_SOIL.setScalarName("North Coordinate");
        voiColorMap_SOIL=new ScalarMap( voiColor , Display.RGB );
        voiColorMap_SOIL.setRange(0,10);

        display_SOIL.addMap(eastMap_SOIL);
        display_SOIL.addMap(northMap_SOIL);
        display_SOIL.addMap(voiColorMap_SOIL);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display_SOIL);
        
        jPanel7.add("Center",display_SOIL.getComponent());

        
        //Graphical structure for input groundwater
        drGW=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_GW = new DisplayImplJ3D("display_GW",drGW);
        
        dispGMC = (GraphicsModeControl) display_GW.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap_GW = new ScalarMap( xEasting , Display.XAxis );
        eastMap_GW.setScalarName("East Coordinate");
        northMap_GW = new ScalarMap( yNorthing , Display.YAxis );
        northMap_GW.setScalarName("North Coordinate");
        voiColorMap_GW=new ScalarMap( voiColor , Display.RGB );
        voiColorMap_GW.setRange(0,10);

        display_GW.addMap(eastMap_GW);
        display_GW.addMap(northMap_GW);
        display_GW.addMap(voiColorMap_GW);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display_GW);
        
        jPanel13.add("Center",display_GW.getComponent());

        
        //Graphical structure for output triangulated points
        drO=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_TIN_O = new DisplayImplJ3D("display_TIN_O",drO);
        
        dispGMC = (GraphicsModeControl) display_TIN_O.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap_O = new ScalarMap( xEasting , Display.XAxis );
        eastMap_O.setScalarName("East Coordinate [meters]");
        northMap_O = new ScalarMap( yNorthing , Display.YAxis );
        northMap_O.setScalarName("North Coordinate [meters]");
        pointsMap_O=new ScalarMap( nodeColor , Display.RGB );
        pointsMap_O.setRange(0,4);
        voiColorMap_O=new ScalarMap( voiColor , Display.RGB );
        voiColorMap_O.setRange(0,10);
                
        display_TIN_O.addMap(eastMap_O);
        display_TIN_O.addMap(northMap_O);
        display_TIN_O.addMap(pointsMap_O);
        display_TIN_O.addMap(voiColorMap_O);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display_TIN_O);
        
        display_TIN_O.enableEvent(visad.DisplayEvent.MOUSE_MOVED);
        display_TIN_O.addDisplayListener(this);
        
        jPanel17.add("Center",display_TIN_O.getComponent());
        
        //Graphical structure for output river network
        drNET=new  visad.java3d.TwoDDisplayRendererJ3D();
        display_NET = new DisplayImplJ3D("display_NET",drNET);
        
        dispGMC = (GraphicsModeControl) display_NET.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        eastMap_NET = new ScalarMap( xEasting , Display.XAxis );
        eastMap_NET.setScalarName("East Coordinate [meters]");
        northMap_NET = new ScalarMap( yNorthing , Display.YAxis );
        northMap_NET.setScalarName("North Coordinate [meters]");
                
        display_NET.addMap(eastMap_NET);
        display_NET.addMap(northMap_NET);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display_NET);
        
        display_NET.enableEvent(visad.DisplayEvent.MOUSE_MOVED);
        display_NET.addDisplayListener(this);
        
        jPanel34.add("Center",display_NET.getComponent());
    }
    
    private void initializeInputTabs() throws RemoteException, VisADException, java.io.IOException {
        
        drI.setBackgroundColor(1,1,1);
        drI.setForegroundColor(0,0,0);
        
        ProjectionControl pc = display_TIN_I.getProjectionControl();
        pc.setAspectCartesian(basTIN_I.getAspect());        
        
        data_refPoints_I = new DataReferenceImpl("data_ref_Points");
        data_refPoints_I.setData(basTIN_I.getPointsFlatField());
        
        pointsCMap_I = new ConstantMap[] {new ConstantMap( 5.0f, Display.PointSize)};

        display_TIN_I.addReference( data_refPoints_I,pointsCMap_I );
        
        data_refTr_I = new DataReferenceImpl("data_ref_TRIANG");
        trianglesCMap_I = new ConstantMap[] {   new ConstantMap( 0.0f, Display.Red),
                                                new ConstantMap( 1.0f, Display.Green),
                                                new ConstantMap( 0.2f, Display.Blue),
                                                new ConstantMap( 0.50f, Display.LineWidth)};

        display_TIN_I.addReference( data_refTr_I,trianglesCMap_I );
        
        data_refPoly_I = new DataReferenceImpl("data_ref_poly");
        polygonsCMap_I = new ConstantMap[] {    new ConstantMap( 0.0f, Display.Red),
                                                new ConstantMap( 0.0f, Display.Green),
                                                new ConstantMap( 0.0f, Display.Blue),
                                                new ConstantMap( 1.50f, Display.LineWidth)};

        display_TIN_I.addReference( data_refPoly_I,polygonsCMap_I );
        
        numPointsLabel.setText("Number of Points : "+basTIN_I.getNumPoints());
        
        String[] levelsString=new String[basTIN_I.getBasinOrder()];
        for (int i = 0; i < levelsString.length; i++) {
            levelsString[i]="Level "+(i+1);
        }
        
        ridgeLevelCombo.setModel(new javax.swing.DefaultComboBoxModel(levelsString));
        ridgeLevelCombo.setSelectedIndex(2);
        
        String[] levelsStringN=new String[basTIN_I.getBasinOrder()+1];
        levelsStringN[0]="All Network";
        for (int i = 1; i < levelsStringN.length; i++) {
            levelsStringN[i]="Level "+i;
        }
        
        networkLevelCombo.setModel(new javax.swing.DefaultComboBoxModel(levelsStringN));
        
        drLULC.setBackgroundColor(1,1,1);
        drLULC.setForegroundColor(0,0,0);
        pc = display_LULC.getProjectionControl();
        pc.setAspectCartesian(basTIN_I.getAspect());
        data_refFill_LULC = new DataReferenceImpl("data_refFill_LCLU");
        display_LULC.addReference( data_refFill_LULC );
        
        drSOIL.setBackgroundColor(1,1,1);
        drSOIL.setForegroundColor(0,0,0);
        pc = display_SOIL.getProjectionControl();
        pc.setAspectCartesian(basTIN_I.getAspect());
        data_refFill_SOIL = new DataReferenceImpl("data_refFill_SOIL");
        display_SOIL.addReference( data_refFill_SOIL );

        drGW.setBackgroundColor(1,1,1);
        drGW.setForegroundColor(0,0,0);
        pc = display_GW.getProjectionControl();
        pc.setAspectCartesian(basTIN_I.getAspect());
        data_refFill_GW = new DataReferenceImpl("data_refFill_GW");
        display_GW.addReference( data_refFill_GW );
        
        panel_IO.remove(jPanel_O);
        jPanel15.remove(jPanel28);
        
        Object[] options = { "Continue >>>"};
        javax.swing.JOptionPane.showOptionDialog(mainFrame, "Your field has been successfully projected \n to WGS84 - UTM Zone "+basTIN_I.TIN_Zone+(basTIN_I.TIN_Hemisphere?"N":"S")+"\nSave this information for future reference", "Attention", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,null, options, options[0]);
            

        
    }
    
    private void initializeDataExportTab(){
        
        System.out.println(">> Calculating MD5 keys");
        
        md5_grid_textField.setText(basTIN_O.GridMD5());
        md5_simu_textField.setText(fmrfm.SimulationMD5());

        System.out.println(">> Obtaining Projection's list");
        
        try {
            
            Statement st = conn.createStatement();
            
            ResultSet rs = st.executeQuery("SELECT srid,srtext FROM spatial_ref_sys WHERE srtext LIKE '%UTM zone%'");
            java.util.Vector avaProjs=new java.util.Vector<String>(3000);
            avaProjs.add("Projections ...");
            String proStr;
            String[] pieces;
            while (rs.next()) {
                proStr=rs.getString(2);
                pieces=proStr.split("\"");
                avaProjs.add("["+rs.getString(1)+"]"+" "+pieces[1]);
            }
            rs.close();
            
            projsComboBox.setModel(new javax.swing.DefaultComboBoxModel(avaProjs));
            
            
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        
        Object months[] = 
           {"Month",
            "01",	"02",	"03",	"04",	"05",	"06",	"07",	"08",	"09",	"10",	"11",	"12"};
        monthComboBox.setModel(new javax.swing.DefaultComboBoxModel(months));
        
        Object days[] = 
           {"Day",
            "01",	"02",	"03",	"04",	"05",	"06",	"07",	"08",	"09",	"10",	"11",	"12",	"13",	"14",	"15",	"16",	"17",	"18",	"19",	"20",	"21",	"22",	"23",	"24",	"25",	"26",	"27",	"28",	"29",	"30",	"31"};
        dayComboBox.setModel(new javax.swing.DefaultComboBoxModel(days));
        
        Object years[] = 
           {"Year",
            "1950",	"1951",	"1952",	"1953",	"1954",	"1955",	"1956",	"1957",	"1958",	"1959",	"1960",	"1961",	"1962",	"1963",	"1964",	"1965",	"1966",	"1967",	"1968",	"1969",	"1970",	"1971",	"1972",	"1973",	"1974",	"1975",	"1976",	"1977",	"1978",	"1979",	"1980",	"1981",	"1982",	"1983",	"1984",	"1985",	"1986",	"1987",	"1988",	"1989",	"1990",	"1991",	"1992",	"1993",	"1994",	"1995",	"1996",	"1997",	"1998",	"1999",	"2000",	"2001",	"2002",	"2003",	"2004",	"2005",	"2006",	"2007",	"2008",	"2009",	"2010",	"2011",	"2012",	"2013",	"2014",	"2015",	"2016",	"2017",	"2018",	"2019",	"2020",	"2021",	"2022",	"2023",	"2024",	"2025",	"2026",	"2027",	"2028",	"2029",	"2030",	"2031",	"2032",	"2033",	"2034",	"2035",	"2036",	"2037",	"2038",	"2039",	"2040",	"2041",	"2042",	"2043",	"2044",	"2045",	"2046",	"2047",	"2048",	"2049",	"2050"};
        yearComboBox.setModel(new javax.swing.DefaultComboBoxModel(years));
        
        Object hour[] = 
           {"Hour",
            "00",   "01",	"02",	"03",	"04",	"05",	"06",	"07",	"08",	"09",	"10",	"11",	"12",	"13",	"14",	"15",	"16",	"17",	"18",	"19",	"20",	"21",	"22",	"23"};
        hourComboBox.setModel(new javax.swing.DefaultComboBoxModel(hour));
        
        Object minute[] = 
           {"Minute",
            "00",   "01",	"02",	"03",	"04",	"05",	"06",	"07",	"08",	"09",	"10",	"11",	"12",	"13",	"14",	"15",	"16",	"17",	"18",	"19",	"20",	"21",	"22",	"23",	"24",	"25",	"26",	"27",	"28",	"29",	"30",	"31",	"32",	"33",	"34",	"35",	"36",	"37",	"38",	"39",	"40",	"41",	"42",	"43",	"44",	"45",	"46",	"47",	"48",	"49",	"50",	"51",	"52",	"53",	"54",	"55",	"56",	"57",	"58",	"59",	"60"};
        minComboBox.setModel(new javax.swing.DefaultComboBoxModel(minute));
        
        Object timeZones[] = 
           {"TimeZone",
            "UTC-1200",	"UTC-1100",	"UTC-1000",	"UTC-0900",	"UTC-0800",	"UTC-0700",	"UTC-0600",	"UTC-0500",	"UTC-0400",	"UTC-0300",	"UTC-0200",	"UTC-0100",	"UTC-0000",	"UTC+0100",	"UTC+0200",	"UTC+0300",	"UTC+0400",	"UTC+0500",	"UTC+0600",	"UTC+0700",	"UTC+0800",	"UTC+0900",	"UTC+1000",	"UTC+1100",	"UTC+1200"};
        timeZoneComboBox.setModel(new javax.swing.DefaultComboBoxModel(timeZones));
        
    }
    
    private void initializeOutputTabs() throws RemoteException, VisADException, java.io.IOException {
        
        drO.setBackgroundColor(1,1,1);
        drO.setForegroundColor(0,0,0);
        
        ProjectionControl pc = display_TIN_O.getProjectionControl();
        pc.setAspectCartesian(basTIN_O.getAspect());
        
        data_refPoints_O = new DataReferenceImpl("data_ref_Points");
        data_refPoints_O.setData(basTIN_O.getPointsFlatField());
        pointsCMap_O = new ConstantMap[] {new ConstantMap( 5.0f, Display.PointSize)};
        
        
        data_refTr_O = new DataReferenceImpl("data_ref_TRIANG");
        data_refTr_O.setData(basTIN_O.getTrianglesUnionSet());
        trianglesCMap_O = new ConstantMap[] {   new ConstantMap( 0.0f, Display.Red),
                                                new ConstantMap( 1.0f, Display.Green),
                                                new ConstantMap( 0.2f, Display.Blue),
                                                new ConstantMap( 0.50f, Display.LineWidth)};

        data_refPoly_O = new DataReferenceImpl("data_ref_poly");
        data_refPoly_O.setData(basTIN_O.getPolygonsUnionSet());
        polygonsCMap_O = new ConstantMap[] {    new ConstantMap( 0.0f, Display.Red),
                                                new ConstantMap( 0.0f, Display.Green),
                                                new ConstantMap( 0.0f, Display.Blue),
                                                new ConstantMap( 1.50f, Display.LineWidth)};
        display_TIN_O.addReference( data_refPoly_O,polygonsCMap_O );
        
        data_refFill_O = new DataReferenceImpl("data_ref_Fill");
        data_refFill_O.setData(basTIN_O.getValuesFlatField());
        display_TIN_O.addReference( data_refFill_O );
        
        data_refNet_O = new DataReferenceImpl("data_ref_net");
        data_refNet_O.setData(basNet.getReachesUnionSet());
        networkCMap_O = new ConstantMap[] {     new ConstantMap( 0.0f, Display.Red),
                                                new ConstantMap( 0.3f, Display.Green),
                                                new ConstantMap( 1.0f, Display.Blue),
                                                new ConstantMap( 2.50f, Display.LineWidth)};

        display_TIN_O.addReference( data_refNet_O,networkCMap_O );
        
        if(basTIN_O.getNumVoi() > 10000) {
            display_TIN_O.disableEvent(visad.DisplayEvent.MOUSE_MOVED);
            display_NET.disableEvent(visad.DisplayEvent.MOUSE_MOVED);
            pointsCheckBox_O.setSelected(false);
            trianglesCheckBox_O.setSelected(false);
        } else {
            display_TIN_O.addReference( data_refPoints_O,pointsCMap_O );
            display_TIN_O.addReference( data_refTr_O,trianglesCMap_O );
            
        }
        
        visad.TextType t = visad.TextType.getTextType("text");
        visad.ScalarMap tmap=new visad.ScalarMap(t, visad.Display.Text);
        display_TIN_O.addMap(tmap);
        
        visad.TextControl tcontrol = (visad.TextControl) tmap.getControl();
        tcontrol.setCenter(true);
        tcontrol.setAutoSize(true);
        tcontrol.setFont(font);
        
        drNET.setBackgroundColor(1,1,1);
        drNET.setForegroundColor(0,0,0);
        
        pc = display_NET.getProjectionControl();
        pc.setAspectCartesian(basTIN_O.getAspect());
        
        int numReaches=basNet.getNumReaches();
        
        for (int i=0;i<numReaches;i++){
            final int myIndex=i;
            Runnable addReach = new Runnable() {
                public void run() {
                    try {
                        visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("Reach");
                        rtref1.setData(basNet.getReach(myIndex));
                        
                        visad.ConstantMap[] rtmaps1 = {new visad.ConstantMap(0.0, visad.Display.Red),
                                                       new visad.ConstantMap(0.3, visad.Display.Green),
                                                       new visad.ConstantMap(1.0, visad.Display.Blue),
                                                       new ConstantMap( 2.50f, Display.LineWidth)};
                        
                        display_NET.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
                        visad.CellImpl cells1 = new visad.CellImpl() {
                            private boolean first = true;
                            public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                                if (first) first = false;
                                else {
                                    reachAction(myIndex);
                                }
                            }
                        };
                        cells1.addReference(rtref1);
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing reaches");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing reaches");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addReach).start();
        }
        
        plotMrf(2);
        plotRft();
        
        qNodesCombo.setModel(new javax.swing.DefaultComboBoxModel(fqm.getKeys()));
        qNodesCombo.setSelectedItem("Outlet");
        int[] qoutIndex=fqm.getLocationIndexes(baseNameTextField.getText());
        for (int i=0;i<qoutIndex.length;i++){
            final int myIndex=qoutIndex[i];
            Runnable addPoint = new Runnable() {
                public void run() {
                    try {
                        visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("RealTuples");
                        rtref1.setData(basTIN_O.getPositionTuple(myIndex));
                        visad.ConstantMap[] rtmaps1 = {new visad.ConstantMap(10.0, visad.Display.PointSize ),
                                                       new visad.ConstantMap(0.4, visad.Display.Red),
                                                       new visad.ConstantMap(0.4, visad.Display.Green),
                                                       new visad.ConstantMap(0.4, visad.Display.Blue)};
                        
                        display_TIN_O.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
                        visad.CellImpl cells1 = new visad.CellImpl() {
                            private boolean first = true;
                            public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                                if (first) first = false;
                                else {
                                    qoutAction(myIndex);
                                }
                            }
                        };
                        cells1.addReference(rtref1);
                        
                        visad.ConstantMap[] rtmaps2 = {new visad.ConstantMap(0.0, visad.Display.Red),
                                                       new visad.ConstantMap(0.0, visad.Display.Green),
                                                       new visad.ConstantMap(0.0, visad.Display.Blue)};
                        
                        visad.DataReferenceImpl tref = new visad.DataReferenceImpl("text");
                        tref.setData(basTIN_O.getTextTuple(myIndex));
                        display_TIN_O.addReference(tref,rtmaps2);                        
                        
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing gauges");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing gauges");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addPoint).start();
        }
        
        if(fpm != null){
            pNodesCombo.setModel(new javax.swing.DefaultComboBoxModel(fpm.getKeys()));
            pNodesVarsCombo.setModel(new javax.swing.DefaultComboBoxModel(ListOfVariables.localOutput));
            pNodesCombo.setSelectedIndex(0);
            int[] pixelIndex=fpm.getLocationIndexes(baseNameTextField.getText());
            for (int i=0;i<pixelIndex.length;i++){
                final int myIndex=pixelIndex[i];
                Runnable addPoint = new Runnable() {
                    public void run() {
                        try {
                            visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("RealTuples");
                            rtref1.setData(basTIN_O.getPositionTuple(myIndex));
                            visad.ConstantMap[] rtmaps1 = {new visad.ConstantMap(10.0, visad.Display.PointSize ),
                                                           new visad.ConstantMap(0.7, visad.Display.Red),
                                                           new visad.ConstantMap(0.7, visad.Display.Green),
                                                           new visad.ConstantMap(0.7, visad.Display.Blue)};
                            display_TIN_O.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
                            visad.CellImpl cells1 = new visad.CellImpl() {
                                private boolean first = true;
                                public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                                    if (first) first = false;
                                    else {
                                        pixelAction(myIndex);
                                    }
                                }
                            };
                            cells1.addReference(rtref1);
                            
                            visad.ConstantMap[] rtmaps2 = {new visad.ConstantMap(0.0, visad.Display.Red),
                                                           new visad.ConstantMap(0.0, visad.Display.Green),
                                                           new visad.ConstantMap(0.0, visad.Display.Blue)};
                        
                            visad.DataReferenceImpl tref = new visad.DataReferenceImpl("text");
                            tref.setData(basTIN_O.getTextTuple(myIndex));
                            display_TIN_O.addReference(tref,rtmaps2);

                        } catch (visad.VisADException exc) {
                            System.err.println("Failed showing gauges");
                            System.err.println(exc);
                        } catch (java.io.IOException exc) {
                            System.err.println("Failed showing gauges");
                            System.err.println(exc);
                        }
                    }
                };
                new Thread(addPoint).start();
            }
        }
        
        spaceParamsCombo.setModel(new javax.swing.DefaultComboBoxModel(ListOfVariables.spatialParams));
        
        Object[] spatialMoments=new String[] {"Available Times","Initial Condition","Final State"};
        
        spatialMoments=hydroScalingAPI.tools.ArrayTools.concatentate(spatialMoments,fdm.getKeys());
        avaTimesCombo.setModel(new javax.swing.DefaultComboBoxModel(spatialMoments));
        System.out.println("Step done");
        
        panel_IO.remove(jPanel_I);
        
    }
    
    private java.io.File findTriEdgNodes(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseNameTextField.getText(),"nodes"));
        if(fileNodesCand.length > 0){
            return fileNodesCand[0].getParentFile();
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File resSearch=findTriEdgNodes(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private java.io.File findMRF(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseNameTextField.getText(),"mrf"));
        if(fileNodesCand.length > 0){
            return fileNodesCand[0];
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File resSearch=findMRF(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private void plotMrf(int i){
        double[] time=fmrfm.getTime();
        double[] values=fmrfm.getSeries(i);
        PpanelMRF.removeAll();
        PpanelMRF.addDatos(time,values,-9999,java.awt.Color.BLUE,1);
    }
    
    private java.io.File findRFT(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseNameTextField.getText(),"rft"));
        if(fileNodesCand.length > 0){
            return fileNodesCand[0];
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File resSearch=findRFT(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private java.io.File findCNTRL(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseNameTextField.getText(),"cntrl"));
        if(fileNodesCand.length > 0){
            return fileNodesCand[0];
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File resSearch=findCNTRL(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private void plotRft(){
        PpanelRTF.removeAll();
        if(infExBox.isSelected()) PpanelRTF.addDatos(frftm.getTime(),frftm.getSeries(1),-9999,java.awt.Color.BLUE,1);
        if(satExBox.isSelected()) PpanelRTF.addDatos(frftm.getTime(),frftm.getSeries(2),-9999,java.awt.Color.RED,1);
        if(perFlBox.isSelected()) PpanelRTF.addDatos(frftm.getTime(),frftm.getSeries(3),-9999,java.awt.Color.GREEN,1); 
        if(groFlBox.isSelected()) PpanelRTF.addDatos(frftm.getTime(),frftm.getSeries(4),-9999,java.awt.Color.YELLOW,1); 
    }
    
    private java.io.File[] findQouts(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseNameTextField.getText(),"qout"));
        if(fileNodesCand.length > 0){
            return fileNodesCand;
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File[] resSearch=findQouts(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private void plotQouts(){
        Object theKey=qNodesCombo.getSelectedItem();
        PpanelQOUT.removeAll();
        if(dischBox.isSelected()) PpanelQOUT.addDatos(fqm.getTime(theKey),fqm.getSeries(theKey,1),-9999,java.awt.Color.BLUE,1);
        if(stageBox.isSelected()) PpanelQOUT.addDatos(fqm.getTime(theKey),fqm.getSeries(theKey,2),-9999,java.awt.Color.RED,1);
    }
    
    private java.io.File[] findPixel(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseNameTextField.getText(),"pixel"));
        if(fileNodesCand.length > 0){
            return fileNodesCand;
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File[] resSearch=findPixel(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    private void plotPixel(){
        Object theKey=pNodesCombo.getSelectedItem();
        int theIndex=pNodesVarsCombo.getSelectedIndex();
        PpanelPixel.removeAll();
        PpanelPixel.addDatos(fpm.getTime(theKey),fpm.getSeries(theKey,theIndex+2),-9999,java.awt.Color.BLUE,1);
        
    }
    
    private java.io.File[] findIntegratedOutput(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameEndingFilter(baseNameTextField.getText(),"i"));
        if(fileNodesCand.length > 0){
            return fileNodesCand;
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File[] resSearch=findIntegratedOutput(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    private void paintParametersVoronoiPolygons(int locAndCol){
        if(locAndCol >= 2000){
            if(locAndCol >= 3000){
                paintParametersVoronoiPolygons(locAndCol,"Only Network");
            } else {
                paintParametersVoronoiPolygons(locAndCol,baseNameTextField.getText()+".0000_00d");
            }
        } else {
            paintParametersVoronoiPolygons(locAndCol,"Initial Condition");
        }
    }
    private void paintParametersVoronoiPolygons(int locAndCol,Object fileKey){
        
        System.out.println("It is going to plot "+fileKey+" var# "+locAndCol);
        
        float[][] values=new float[1][];
        
        if(locAndCol >= 2000){
            if(locAndCol >= 3000){
                values[0] = fonm.getValues((String)fileKey,locAndCol-3000);
            } else {
                values[0] = fdm.getValues((String)fileKey,locAndCol-2000);
            }
        } else {
            values[0] = fim.getValues((String)fileKey,locAndCol-1000);
        }

        hydroScalingAPI.util.statistics.Stats
                fieldStats=new hydroScalingAPI.util.statistics.Stats(values[0]);
        
        values[0]=basTIN_O.valuesToVoroValues(values[0]);
        
        try {
            voiColorMap_O.setRange(fieldStats.minValue,fieldStats.maxValue);
            basTIN_O.getValuesFlatField().setSamples(values);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
        
    }
    
    private java.io.File[] findDynamicOutput(java.io.File iniDir){
        java.io.File[] fileNodesCand=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.NameEndingFilter(baseNameTextField.getText(),"d"));
        if(fileNodesCand.length > 0){
            return fileNodesCand;
        }else{
            java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
            for (int i = 0; i < dirsToDig.length; i++) {
                java.io.File[] resSearch=findDynamicOutput(dirsToDig[i]);
                if (resSearch != null) return resSearch;
            }
        }
        return null;
    }
    
    
    private void plotOutputPoints() throws RemoteException, VisADException, java.io.IOException{
        java.io.File outputsDirectory=findTriEdgNodes(new java.io.File(pathTextField.getText()));
        String baseName=baseNameTextField.getText();
        
    }
    
    private void plotPoints() throws RemoteException, VisADException,java.io.IOException{
        
        System.out.println(networkLevelCombo.getSelectedIndex()+" "+pathDensityLevelCombo.getSelectedIndex()+" "+bufferTypeCombo.getSelectedIndex());
        
        basTIN_I.initializeNetworkPoints(networkLevelCombo.getSelectedIndex()-1,pathDensityLevelCombo.getSelectedIndex(),bufferTypeCombo.getSelectedIndex());
        basTIN_I.filterPoints(ridgeLevelCombo.getSelectedIndex(),zrSlider.getValue(),networkLevelCombo.getSelectedIndex()-1);
        
        data_refPoints_I.setData(basTIN_I.getPointsFlatField());
        data_refTr_I.setData(basTIN_I.getTrianglesUnionSet());
        
        numPointsLabel.setText("Number of Points : "+basTIN_I.getNumPoints());
    }
    
    private void qoutAction(int index){
        if(index != basTIN_O.getOutletNode())
            qNodesCombo.setSelectedItem(index+"");
        else
            qNodesCombo.setSelectedItem("Outlet");
        panelOutputs.setSelectedIndex(1);
    }
    
    private void reachAction(int index){
        new hydroScalingAPI.modules.tRIBS_io.widgets.ReachInfo(mainFrame,basNet.getReachInfo(index)).setVisible(true);
    }
    
    private void pixelAction(int index){
        pNodesCombo.setSelectedItem(baseNameTextField.getText()+index+".pixel");
        panelOutputs.setSelectedIndex(4);
    }
    
    /**
     * A required method to handle interaction with the various visad.Display
     * @param DispEvt The interaction event
     * @throws visad.VisADException Errors while handling VisAD objects
     * @throws java.rmi.RemoteException Errors while assigning data to VisAD objects
     */
    public void displayChanged(visad.DisplayEvent DispEvt) throws visad.VisADException, java.rmi.RemoteException {
        
        int id = DispEvt.getId();
        visad.DisplayRenderer dr=display_TIN_O.getDisplayRenderer();
        
        try {
            if(DispEvt.getId() == visad.DisplayEvent.MOUSE_PRESSED_LEFT){
                
                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());
                
                float resultX= eastMap_O.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                float resultY= northMap_O.inverseScaleValues(new float[] {(float)ray.position[1]})[0];
                
                longitudeLabel.setText(""+resultX+" (UTM: "+(resultX+basTIN_O.minX)+")");
                latitudeLabel.setText(""+resultY+" (UTM: "+(resultY+basTIN_O.minY)+")");
                if(basTIN_O.getValuesFlatField() != null) {
                    visad.Real spotValue=(visad.Real) basTIN_O.getValuesFlatField().evaluate(new visad.RealTuple(domainXLYL, new double[] {resultX,resultY}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                    valueLabel.setText(""+spotValue);
                }
                
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            if (id == DispEvt.MOUSE_MOVED) {
                
                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());
                
                float resultX= eastMap_O.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                float resultY= northMap_O.inverseScaleValues(new float[] {(float)ray.position[1]})[0];
                
                longitudeLabel.setText(""+resultX+" (UTM: "+(resultX+basTIN_O.minX)+")");
                latitudeLabel.setText(""+resultY+" (UTM: "+(resultY+basTIN_O.minY)+")");
                if(basTIN_O.getValuesFlatField() != null) {
                    visad.Real spotValue=(visad.Real) basTIN_O.getValuesFlatField().evaluate(new visad.RealTuple(domainXLYL, new double[] {resultX,resultY}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                    valueLabel.setText(""+spotValue);
                }
                
                
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mrfButtonGroup = new javax.swing.ButtonGroup();
        panel_IO = new javax.swing.JTabbedPane();
        jPanel_I = new javax.swing.JPanel();
        panelInputs = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        computeLattice = new javax.swing.JButton();
        exportPoiButton = new javax.swing.JButton();
        resetLattice = new javax.swing.JButton();
        exportTriButton = new javax.swing.JButton();
        jPanel33 = new javax.swing.JPanel();
        zrLabel = new javax.swing.JLabel();
        zrSlider = new javax.swing.JSlider();
        jPanel42 = new javax.swing.JPanel();
        jPanel43 = new javax.swing.JPanel();
        jLabel16 = new javax.swing.JLabel();
        networkLevelCombo = new javax.swing.JComboBox();
        jPanel44 = new javax.swing.JPanel();
        jLabel17 = new javax.swing.JLabel();
        pathDensityLevelCombo = new javax.swing.JComboBox();
        jPanel45 = new javax.swing.JPanel();
        jLabel18 = new javax.swing.JLabel();
        bufferTypeCombo = new javax.swing.JComboBox();
        jPanel32 = new javax.swing.JPanel();
        jLabel9 = new javax.swing.JLabel();
        ridgeLevelCombo = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        pointsCheckBox_I = new javax.swing.JCheckBox();
        trianglesCheckBox_I = new javax.swing.JCheckBox();
        numPointsLabel = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        selectLandCover = new javax.swing.JButton();
        jPanel46 = new javax.swing.JPanel();
        jLabel19 = new javax.swing.JLabel();
        exportLandCover = new javax.swing.JButton();
        jPanel7 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jLabel14 = new javax.swing.JLabel();
        selectSoils = new javax.swing.JButton();
        jPanel58 = new javax.swing.JPanel();
        jLabel20 = new javax.swing.JLabel();
        exportSoils = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jPanel41 = new javax.swing.JPanel();
        jLabel15 = new javax.swing.JLabel();
        selectGW = new javax.swing.JButton();
        jPanel59 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        exportGW = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        inFileTextArea = new javax.swing.JTextArea();
        jPanel4 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        writeInFile = new javax.swing.JButton();
        jLabel12 = new javax.swing.JLabel();
        jPanel_O = new javax.swing.JPanel();
        panelOutputs = new javax.swing.JTabbedPane();
        jPanel24 = new javax.swing.JPanel();
        mrfPanel = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jRadioButton9 = new javax.swing.JRadioButton();
        jRadioButton10 = new javax.swing.JRadioButton();
        jRadioButton11 = new javax.swing.JRadioButton();
        jPanel16 = new javax.swing.JPanel();
        rftPanel = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        infExBox = new javax.swing.JCheckBox();
        satExBox = new javax.swing.JCheckBox();
        perFlBox = new javax.swing.JCheckBox();
        groFlBox = new javax.swing.JCheckBox();
        qoutPanel = new javax.swing.JPanel();
        jPanel23 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        qNodesCombo = new javax.swing.JComboBox();
        dischBox = new javax.swing.JCheckBox();
        stageBox = new javax.swing.JCheckBox();
        jPanel26 = new javax.swing.JPanel();
        jPanel27 = new javax.swing.JPanel();
        spaceParamsCombo = new javax.swing.JComboBox();
        colorTableButton = new javax.swing.JButton();
        jPanel17 = new javax.swing.JPanel();
        jPanel20 = new javax.swing.JPanel();
        avaTimesCombo = new javax.swing.JComboBox();
        avaVariablesCombo = new javax.swing.JComboBox();
        jPanel22 = new javax.swing.JPanel();
        pointsCheckBox_O = new javax.swing.JCheckBox();
        netCheckBox_O = new javax.swing.JCheckBox();
        trianglesCheckBox_O = new javax.swing.JCheckBox();
        voronoiCheckBox_O = new javax.swing.JCheckBox();
        valuesCheckBox_O = new javax.swing.JCheckBox();
        jPanel30 = new javax.swing.JPanel();
        pixelPanel = new javax.swing.JPanel();
        jPanel31 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        pNodesCombo = new javax.swing.JComboBox();
        pNodesVarsCombo = new javax.swing.JComboBox();
        jPanel35 = new javax.swing.JPanel();
        jPanel36 = new javax.swing.JPanel();
        jPanel39 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        md5_grid_textField = new javax.swing.JTextField();
        jPanel54 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        projsComboBox = new javax.swing.JComboBox();
        jPanel47 = new javax.swing.JPanel();
        jLabel25 = new javax.swing.JLabel();
        md5_simu_textField = new javax.swing.JTextField();
        jPanel55 = new javax.swing.JPanel();
        jLabel33 = new javax.swing.JLabel();
        jPanel38 = new javax.swing.JPanel();
        monthComboBox = new javax.swing.JComboBox();
        dayComboBox = new javax.swing.JComboBox();
        yearComboBox = new javax.swing.JComboBox();
        hourComboBox = new javax.swing.JComboBox();
        minComboBox = new javax.swing.JComboBox();
        timeZoneComboBox = new javax.swing.JComboBox();
        jPanel56 = new javax.swing.JPanel();
        jLabel34 = new javax.swing.JLabel();
        jPanel40 = new javax.swing.JPanel();
        inFileTextField = new javax.swing.JTextField();
        findFileButton = new javax.swing.JButton();
        jPanel48 = new javax.swing.JPanel();
        jLabel26 = new javax.swing.JLabel();
        polyProgressBar = new javax.swing.JProgressBar();
        jPanel49 = new javax.swing.JPanel();
        jLabel27 = new javax.swing.JLabel();
        reachProgressBar = new javax.swing.JProgressBar();
        jPanel53 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        aggProgressBar = new javax.swing.JProgressBar();
        jPanel57 = new javax.swing.JPanel();
        jLabel35 = new javax.swing.JLabel();
        aggTimeProgressBar = new javax.swing.JProgressBar();
        jPanel51 = new javax.swing.JPanel();
        jLabel29 = new javax.swing.JLabel();
        hydProgressBar = new javax.swing.JProgressBar();
        jPanel52 = new javax.swing.JPanel();
        jLabel30 = new javax.swing.JLabel();
        pixelProgressBar = new javax.swing.JProgressBar();
        jPanel50 = new javax.swing.JPanel();
        jLabel28 = new javax.swing.JLabel();
        dynamicOutputProgressBar = new javax.swing.JProgressBar();
        jPanel37 = new javax.swing.JPanel();
        sendButton = new javax.swing.JButton();
        jPanel34 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jPanel28 = new javax.swing.JPanel();
        jPanel61 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        longitudeLabel = new javax.swing.JLabel();
        jPanel29 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        latitudeLabel = new javax.swing.JLabel();
        jPanel62 = new javax.swing.JPanel();
        jLabel323 = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        jPanel18 = new javax.swing.JPanel();
        changePath = new javax.swing.JButton();
        jLabel2 = new javax.swing.JLabel();
        pathTextField = new javax.swing.JTextField();
        jPanel19 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        baseNameTextField = new javax.swing.JTextField();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        panel_IO.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        jPanel_I.setLayout(new java.awt.BorderLayout());

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel2.setLayout(new java.awt.BorderLayout());

        jPanel5.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel5.setLayout(new java.awt.GridLayout(2, 1));

        computeLattice.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        computeLattice.setText("Compute Lattice");
        computeLattice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                computeLatticeActionPerformed(evt);
            }
        });
        jPanel5.add(computeLattice);

        exportPoiButton.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        exportPoiButton.setText("Export Points");
        exportPoiButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportPoiButtonActionPerformed(evt);
            }
        });
        jPanel5.add(exportPoiButton);

        resetLattice.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        resetLattice.setText("Reset Lattice Settings");
        resetLattice.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetLatticeActionPerformed(evt);
            }
        });
        jPanel5.add(resetLattice);

        exportTriButton.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        exportTriButton.setText("Export Trinangulation");
        exportTriButton.setEnabled(false);
        exportTriButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportTriButtonActionPerformed(evt);
            }
        });
        jPanel5.add(exportTriButton);

        jPanel2.add(jPanel5, java.awt.BorderLayout.EAST);

        jPanel33.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel33.setLayout(new java.awt.BorderLayout());

        zrLabel.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        zrLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        zrLabel.setText("Zr [m]");
        zrLabel.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel33.add(zrLabel, java.awt.BorderLayout.NORTH);

        zrSlider.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        zrSlider.setMajorTickSpacing(5);
        zrSlider.setMaximum(20);
        zrSlider.setMinorTickSpacing(1);
        zrSlider.setPaintLabels(true);
        zrSlider.setPaintTicks(true);
        zrSlider.setSnapToTicks(true);
        zrSlider.setValue(0);
        jPanel33.add(zrSlider, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel33, java.awt.BorderLayout.CENTER);

        jPanel42.setLayout(new java.awt.GridLayout(1, 0));

        jPanel43.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel43.setLayout(new java.awt.GridLayout(2, 0));

        jLabel16.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        jLabel16.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel16.setText("Network Level");
        jPanel43.add(jLabel16);

        networkLevelCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        networkLevelCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "All Network", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5", "Level 6" }));
        jPanel43.add(networkLevelCombo);

        jPanel42.add(jPanel43);

        jPanel44.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel44.setLayout(new java.awt.GridLayout(2, 0));

        jLabel17.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        jLabel17.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel17.setText("Path Density");
        jPanel44.add(jLabel17);

        pathDensityLevelCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pathDensityLevelCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Basic", "Dense", "Very Dense" }));
        pathDensityLevelCombo.setSelectedIndex(1);
        jPanel44.add(pathDensityLevelCombo);

        jPanel42.add(jPanel44);

        jPanel45.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel45.setLayout(new java.awt.GridLayout(2, 0));

        jLabel18.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        jLabel18.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel18.setText("River Buffer");
        jPanel45.add(jLabel18);

        bufferTypeCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        bufferTypeCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Buffer", "Simple", "Floodplain" }));
        bufferTypeCombo.setSelectedIndex(1);
        jPanel45.add(bufferTypeCombo);

        jPanel42.add(jPanel45);

        jPanel32.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel32.setLayout(new java.awt.GridLayout(2, 0));

        jLabel9.setFont(new java.awt.Font("Lucida Grande", 1, 10));
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel9.setText("Ridges Level");
        jPanel32.add(jLabel9);

        ridgeLevelCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        ridgeLevelCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "No Ridges", "Level 1", "Level 2", "Level 3", "Level 4", "Level 5", "Level 6" }));
        jPanel32.add(ridgeLevelCombo);

        jPanel42.add(jPanel32);

        jPanel2.add(jPanel42, java.awt.BorderLayout.WEST);

        jPanel1.add(jPanel2, java.awt.BorderLayout.SOUTH);

        jPanel3.setLayout(new java.awt.GridLayout(1, 3));

        pointsCheckBox_I.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pointsCheckBox_I.setSelected(true);
        pointsCheckBox_I.setText("Show Points");
        pointsCheckBox_I.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pointsCheckBox_I.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointsCheckBox_IActionPerformed(evt);
            }
        });
        jPanel3.add(pointsCheckBox_I);

        trianglesCheckBox_I.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        trianglesCheckBox_I.setSelected(true);
        trianglesCheckBox_I.setText("Show Triangles");
        trianglesCheckBox_I.setMargin(new java.awt.Insets(0, 0, 0, 0));
        trianglesCheckBox_I.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trianglesCheckBox_IActionPerformed(evt);
            }
        });
        jPanel3.add(trianglesCheckBox_I);

        numPointsLabel.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        numPointsLabel.setText("Number of Points : ");
        jPanel3.add(numPointsLabel);

        jPanel1.add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel12.setLayout(new java.awt.BorderLayout());
        jPanel1.add(jPanel12, java.awt.BorderLayout.CENTER);

        panelInputs.addTab("TIN", jPanel1);

        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.BorderLayout());

        jLabel13.setText("<<<");
        jPanel8.add(jLabel13, java.awt.BorderLayout.CENTER);

        selectLandCover.setText("Select Land Cover Map ...");
        selectLandCover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectLandCoverActionPerformed(evt);
            }
        });
        jPanel8.add(selectLandCover, java.awt.BorderLayout.WEST);

        jPanel6.add(jPanel8, java.awt.BorderLayout.NORTH);

        jPanel46.setLayout(new java.awt.BorderLayout());

        jLabel19.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel19.setText(">>>");
        jPanel46.add(jLabel19, java.awt.BorderLayout.CENTER);

        exportLandCover.setText("Export Land Cover Map ...");
        exportLandCover.setEnabled(false);
        exportLandCover.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportLandCoverActionPerformed(evt);
            }
        });
        jPanel46.add(exportLandCover, java.awt.BorderLayout.EAST);

        jPanel6.add(jPanel46, java.awt.BorderLayout.SOUTH);

        panelInputs.addTab("Land Cover", jPanel6);

        jPanel7.setLayout(new java.awt.BorderLayout());

        jPanel14.setLayout(new java.awt.BorderLayout());

        jLabel14.setText("<<<");
        jPanel14.add(jLabel14, java.awt.BorderLayout.CENTER);

        selectSoils.setText("Select Soils Map ...");
        selectSoils.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectSoilsActionPerformed(evt);
            }
        });
        jPanel14.add(selectSoils, java.awt.BorderLayout.WEST);

        jPanel7.add(jPanel14, java.awt.BorderLayout.NORTH);

        jPanel58.setLayout(new java.awt.BorderLayout());

        jLabel20.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel20.setText(">>>");
        jPanel58.add(jLabel20, java.awt.BorderLayout.CENTER);

        exportSoils.setText("Export Soils Map ...");
        exportSoils.setEnabled(false);
        exportSoils.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSoilsActionPerformed(evt);
            }
        });
        jPanel58.add(exportSoils, java.awt.BorderLayout.EAST);

        jPanel7.add(jPanel58, java.awt.BorderLayout.SOUTH);

        panelInputs.addTab("Soil Type", jPanel7);

        jPanel13.setLayout(new java.awt.BorderLayout());

        jPanel41.setLayout(new java.awt.BorderLayout());

        jLabel15.setText("<<<");
        jPanel41.add(jLabel15, java.awt.BorderLayout.CENTER);

        selectGW.setText("Select Ground Water Map ...");
        selectGW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectGWActionPerformed(evt);
            }
        });
        jPanel41.add(selectGW, java.awt.BorderLayout.WEST);

        jPanel13.add(jPanel41, java.awt.BorderLayout.NORTH);

        jPanel59.setLayout(new java.awt.BorderLayout());

        jLabel21.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel21.setText(">>>");
        jPanel59.add(jLabel21, java.awt.BorderLayout.CENTER);

        exportGW.setText("Export Ground Water Map ...");
        exportGW.setEnabled(false);
        exportGW.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportGWActionPerformed(evt);
            }
        });
        jPanel59.add(exportGW, java.awt.BorderLayout.EAST);

        jPanel13.add(jPanel59, java.awt.BorderLayout.SOUTH);

        panelInputs.addTab("Ground Water", jPanel13);

        jPanel9.setLayout(new java.awt.BorderLayout());

        inFileTextArea.setColumns(20);
        inFileTextArea.setFont(new java.awt.Font("Courier New", 1, 13));
        inFileTextArea.setRows(5);
        inFileTextArea.setText("##############################################################################\n##\n##                    tRIBS Distributed Hydrologic Model\n##\n##              TIN-based Real-time Integrated Basin Simulator\n##                       Ralph M. Parsons Laboratory\n##                  Massachusetts Institute of Technology\n## \n##\n##  template.in\t\tInput File Template for Generation of tRIBS *.in\n##                      files used for model parameters and file path names.\n##\n##############################################################################\n\n\n##=========================================================================\n##\n##\n##\t\t\tSection 1: Model Run Parameters\n##\n##\n##=========================================================================\n\n## Time Variables\n## --------------\n\nSTARTDATE:      Starting time \t\t\t \t (#MM/DD/YYYY/HH#)\nXX/XX/XXXX/XX\n\nRUNTIME:     \tRun duration\t\t\t\t (#hours#)\nXXX.X\n\nTIMESTEP:    \tUnsaturated zone computational time step (#mins#)\n3.75\n\nGWSTEP:      \tSaturated zone computational time step \t (#mins#)\n7.5\n\nMETSTEP:     \tMeteorological data time step \t\t (#mins#)\n60.0\n\nETISTEP:\tEvapotran/Interception computational time step (hours)\n60.0\n\nRAININTRVL:  \tTime interval in rainfall input \t (#hours#)\n1\n\nOPINTRVL:    \tOutput interval \t\t\t (#hours#)\n1\n\nSPOPINTRVL:   \tSpatial output interval \t\t (#hours#)\n10\n\nINTSTORMMAX: \tInterstorm interval \t\t\t (#hours#)\n8760\n\nRAINSEARCH:\tRainfall search interval (hours)\n2400\n\n\n## Routing Variables\n## -----------------\n\n\nBASEFLOW:    \tBaseflow discharge \t\t\t (#m3/s#)  \n0.01\n\nVELOCITYCOEF: \tDischarge-velocity coefficient \t\t (#m/s#)\n0.5\n\nVELOCITYRATIO: \tStream to hillslope velocity coefficient (#D/L#)\n5\n\nKINEMVELCOEF:   Coefficient in power law for non-linear routing\n0.1\n\nFLOWEXP: \tNonlinear discharge coefficient\t\t (#D/L#)\n0.0000001\n\nCHANNELROUGHNESS: Uniform channel roughness value        (#D/L#)\n0.15\n\nCHANNELWIDTH:     Uniform channel width                  (# m #)\n10\n\nCHANNELWIDTHCOEFF:\tCoefficient in width-area relationship \n1.0\n\nCHANNELWIDTHEXPNT:\tExponent in width-area relationship \n0.3\n\nCHANNELWIDTHFILE:\tFilename that contains channel widths\n\n## Radiation Variable\n## ------------------\n\n\nTLINKE:\t\t\tAtmospheric turbidity parameter\n2.5\n\n##=========================================================================\n##\n##\n##\t\t\tSection 2: Model Run Options\n##\n##\n##  OPTMESHINPUT:\t1  tMesh data\t\t5  Arc/Info *.net\n##\t\t\t2  Point file\t\t6  Arc/Info *.lin,*.pnt\n##\t\t\t3  ArcGrid (random)\t7  Scratch\n##\t\t\t4  ArcGrid (hex)\t8  Point Triangulator\n##\n##  RAINSOURCE:\t\t1  Stage III radar\n##\t\t\t2  WSI radar\n##                      3  Rain gauges\n##\n##  RAINDISTRIBUTION:\t0  Spatially-distributed Radar\n##\t\t\t1  Mean Areal Precipitation Radar\n##\n##  OPTEVAPOTRANS:\t0  Inactive evapotranspiration\n##\t\t\t1  Penman-Monteith method\n##                      2  Deardorff method\n##  \t\t\t3  Priestley-Taylor method\n##                      4  Pan evaporation measurements\n##\n##  OPTINTERCEPT:\t0  Inactive interception \n## \t\t\t1  Canopy storage method\n##\t\t\t2  Canopy water balance method\n##  \n##  GFLUXOPTION:\t0  Inactive ground heat flux\n##\t\t\t1  Temperature gradient method\n##                      2  Force_Restore method\n##\n##  METDATAOPTION:\t0  Inactive meteorological data\n##\t\t\t1  Weather station point data\n##\t\t\t2  Gridded meteorological data\n##\n##  CONVERTDATA:\t0  Inactive data preprocessing\n##\t\t\t1  Active met data preprocessing\n##     \t\t\t2  Active rain gauge data preprocessing\n##\t\t\t3  Active DMIP met data preprocessing\n##\n##  OPTBEDROCK:\t\t0  Uniform bedrock depth (double)\n##\t\t\t1  Variable bedrock depth (grid)\n##\n##  WIDTHINTERPOLATION: 0  Interpolate between measured and observed\n##\t                1  Interpolate only between measured\n##\n##  OPTGWFILE:\t\t0  Resample ASCII grid file in GWATERFILE\n##\t\t      \t1  Read in Voronoi polygon file with GW levels\n##\n##  OPTRUNON:\t\t0  No Runon\n##\t\t\t1  Runon \n##\n##=========================================================================\n\n\nOPTMESHINPUT:   Mesh input data option\n2\n\nRAINSOURCE:  \tRainfall data source option \n2\n\nOPTEVAPOTRANS: \tOption for evapoTranspiration scheme\n1\n\nOPTINTERCEPT: \tOption for interception scheme\n2\n\nGFLUXOPTION: \tOption for ground heat flux\n2\n\nMETDATAOPTION:  Option for meteorological data\n1\n\nCONVERTDATA:   \tOption to convert met data format\n0\n\nOPTBEDROCK:\tOption for uniform or variable depth\n0\n\nWIDTHINTERPOLATION:  Option for interpolating width values\n0\n\nOPTGWFILE: \tOption for groundwater initial file\n0\n\nOPTRUNON:\tOption for runon in overland flow paths\n0\n\n##=========================================================================\n##\n##\n##\t\t\tSection 3: Model Input Files and Pathnames\n##\n##\n##=========================================================================\n\n## Mesh Generation\n## -----------------\n\n\nINPUTDATAFILE:    tMesh input file base name for Mesh files\nOutput/voronoi/basename\n\nINPUTTIME:\t  tMesh input time slice (Option 1)\n\n\nARCINFOFILENAME:  tMesh input file base name Arc files\n\n\nPOINTFILENAME:    tMesh input file name Points files\nInput/basename.points\n\n\n## Resampling Grids\n## -----------------\n\n\nSOILTABLENAME:    Soil parameter reference table (*.sdt)\nInput/basename.sdt\n\nSOILMAPNAME:      Soil texture ASCII grid (*.soi)\nInput/basename.soi\n\nLANDTABLENAME:    Land use parameter reference table (*.ldt)\nInput/basename.ldt\n\nLANDMAPNAME:  \t  Land use ASCII grid (*.lan)\nInput/basename.lan\n\nGWATERFILE:       Ground water ASCII grid (*.iwt) \nInput/basename.iwt\n\nRAINFILE:         Base name of the radar ASCII grid\nRain/p\n\nRAINEXTENSION:    Extension for the radar ASCII grid \ntxt\n\nDEPTHTOBEDROCK:   Uniform depth to bedrock (meters)\n5\n\nBEDROCKFILE:      Bedrock depth ASCII grid (*.brd)\nInput/basename.brd\n\n\n## Meterological Data\n## ------------------\n\n\nHYDROMETSTATIONS:  Hydrometeorological station file (*.sdf)\nWeather/weatherGauge.sdf\n\nHYDROMETGRID:  \t   Hydrometeorological ASCII grid (*.gdf)\nWeather/\n\nHYDROMETCONVERT:   Hydrometeorological data input file (*.mdi)\nWeather/\n\nHYDROMETBASENAME:  Hydrometeorological data file (*.mdf)\nWeather/weatherField\n\nGAUGESTATIONS: \t   Rain Gauge station file (*.sdf)\nRain/rainGauge.sdf\n\nGAUGECONVERT:      Rain Gauge data conversion file (*.mdi)\n\n\nGAUGEBASENAME:\t   Rain Gauge data BASE name (*.mdf)\n\n\n\n## Output Data\n## -----------\n\n\nOUTFILENAME:\t   Base name of the tMesh and dynamic variable output\nOutput/voronoi/basename\n\nOUTHYDROFILENAME:  Base name for hydrograph output\nOutput/hyd/basename\n\nOUTHYDROEXTENSION: Extension for hydrograph output\nmrf\n\nRIBSHYDOUTPUT:     RIBS comp-ty: Opt 0: basin lumped quant, Opt 1: prev+curr \n0\n\nNODEOUTPUTLIST\nInput/Nodes/pNodes.dat\n\nHYDRONODELIST\nInput/Nodes/hNodes.dat\n\nOUTLETNODELIST\nInput/Nodes/oNodes.dat\n\n##=========================================================================\n##\n##\n##\t\t\tSection 4: Model Modes\n##\n##\n##  FORECASTMODE:\t0  No Forecasting\t\t\n##\t\t\t1  Single or Updating QPF Forecast\n##\t\t\t2  Persistence Forecast\n## \t                3  Climatological Forecast\n##\n##  RAINDISTRIBUTION:\t0  Spatially-distributed Radar\n##\t\t\t1  Mean Areal Precipitation Radar\n##\n##  STOCHASTICMODE:\t0  No Stochastic Mode\t 4  Mean+Sine Forcing\n##                      1  Mean Forcing     \t 5  Random+Sine Forcing\n##                      2  Random Forcing\t 6  Weather Generator\n##                      3  Sinusoidal Forcing\n##                      \n##=========================================================================\n\n\n## Rainfall Forecasting\n## --------------------\n\n\nFORECASTMODE:\t   Rainfall Forecasting Mode Option\n0\n\nFORECASTTIME:\t   Forecast Time (hours from start)\n\n\nFORECASTLEADTIME:  Forecast Lead Time (hours) \n\n\nFORECASTLENGTH:\t   Forecast Window Length (hours)\n\n\nFORECASTFILE:\t   Base name of the radar QPF grids\n\n\nCLIMATOLOGY:\t   Rainfall climatology (mm/hr)\n\n\nRAINDISTRIBUTION:  Distributed or MAP radar rainfall\t\n0\n\n## Stochastic Climate Forcing\n## --------------------------\n\nSTOCHASTICMODE:\t   Stochastic Climate Mode Option\n0\n\nPMEAN:\t\t   Mean rainfall intensity (mm/hr)\t\n0.0\n\nSTDUR:\t\t   Mean storm duration (hours)\n0.0\n\nISTDUR:\t\t   Mean time interval between storms (hours) \n0.0\n\nSEED:\t\t   Random seed\n11\n\nPERIOD:\t\t   Period of variation (hours) \n0\n\nMAXPMEAN:\t   Maximum value of mean rainfall intensity (mm/hr) \n0\n\nMAXSTDURMN:        Maximum value of mean storm duration (hours) \n0\n\nMAXISTDURMN: \t   Maximum value of mean interstorm period (hours) \n0\n\nWEATHERTABLENAME:  File with input parameters for weather generator\nInput/pramsWG.T\n\n##=========================================================================\n##\n##\n##\t\t\tEnd of Template.in\n##\n##\n##=========================================================================");
        jScrollPane1.setViewportView(inFileTextArea);

        jPanel9.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel11.setText(">>>");
        jPanel4.add(jLabel11, java.awt.BorderLayout.CENTER);

        writeInFile.setText("Write *.in Template");
        writeInFile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                writeInFileActionPerformed(evt);
            }
        });
        jPanel4.add(writeInFile, java.awt.BorderLayout.EAST);

        jPanel9.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jLabel12.setText("Modify the *.in template file to adjust to your specific simulation needs");
        jPanel9.add(jLabel12, java.awt.BorderLayout.NORTH);

        panelInputs.addTab("Input File", jPanel9);

        jPanel_I.add(panelInputs, java.awt.BorderLayout.CENTER);

        panel_IO.addTab("Input Options", jPanel_I);

        jPanel_O.setLayout(new java.awt.BorderLayout());

        panelOutputs.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                panelOutputsStateChanged(evt);
            }
        });

        jPanel24.setLayout(new java.awt.GridLayout(1, 0));

        mrfPanel.setLayout(new java.awt.BorderLayout());

        jPanel25.setLayout(new java.awt.GridLayout(12, 0));

        jLabel6.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel6.setText("Average Temporal Forcing (*.mrf)");
        jPanel25.add(jLabel6);

        mrfButtonGroup.add(jRadioButton1);
        jRadioButton1.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("Mean Areal Precipitation [mm/h]");
        jRadioButton1.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton1);

        mrfButtonGroup.add(jRadioButton2);
        jRadioButton2.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton2.setText("Maximum Rainfall Rate [mm/h]");
        jRadioButton2.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton2);

        mrfButtonGroup.add(jRadioButton3);
        jRadioButton3.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton3.setText("Minimum Rainfall Rate [mm/h]");
        jRadioButton3.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton3);

        mrfButtonGroup.add(jRadioButton4);
        jRadioButton4.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton4.setText("Forecast State [ ]");
        jRadioButton4.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton4);

        mrfButtonGroup.add(jRadioButton5);
        jRadioButton5.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton5.setText("Mean Surface Soil Moisture [m^3/m^3]");
        jRadioButton5.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton5ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton5);

        mrfButtonGroup.add(jRadioButton6);
        jRadioButton6.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton6.setText("Mean Soil Moisture in Root Zone [m^3/m^3]");
        jRadioButton6.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton6ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton6);

        mrfButtonGroup.add(jRadioButton7);
        jRadioButton7.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton7.setText("Mean Soil Moisture in Unsaturated Zone [m^3/m^3]");
        jRadioButton7.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton7ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton7);

        mrfButtonGroup.add(jRadioButton8);
        jRadioButton8.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton8.setText("Mean Depth to Groundwater [mm]");
        jRadioButton8.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton8ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton8);

        mrfButtonGroup.add(jRadioButton9);
        jRadioButton9.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton9.setText("Mean Evapotranspiration [mm]");
        jRadioButton9.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton9.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton9ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton9);

        mrfButtonGroup.add(jRadioButton10);
        jRadioButton10.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton10.setText("Areal Fraction of Surface Saturation [ ]");
        jRadioButton10.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton10.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton10ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton10);

        mrfButtonGroup.add(jRadioButton11);
        jRadioButton11.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jRadioButton11.setText("Areal Fraction of Rainfall [ ]");
        jRadioButton11.setMargin(new java.awt.Insets(0, 0, 0, 0));
        jRadioButton11.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton11ActionPerformed(evt);
            }
        });
        jPanel25.add(jRadioButton11);

        mrfPanel.add(jPanel25, java.awt.BorderLayout.WEST);

        jPanel24.add(mrfPanel);

        panelOutputs.addTab("Basin-Averaged Response", jPanel24);

        jPanel16.setLayout(new java.awt.GridLayout(2, 0));

        rftPanel.setLayout(new java.awt.BorderLayout());

        jPanel21.setLayout(new java.awt.GridLayout(6, 0));

        jLabel4.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel4.setText("Runoff Component Time Series (*.rft)");
        jPanel21.add(jLabel4);

        infExBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        infExBox.setSelected(true);
        infExBox.setText("Infiltration-excess Runoff");
        infExBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        infExBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infExBoxActionPerformed(evt);
            }
        });
        jPanel21.add(infExBox);

        satExBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        satExBox.setSelected(true);
        satExBox.setText("Saturation-excess Runoff");
        satExBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        satExBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                satExBoxActionPerformed(evt);
            }
        });
        jPanel21.add(satExBox);

        perFlBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        perFlBox.setSelected(true);
        perFlBox.setText("Perched Return Flow");
        perFlBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        perFlBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                perFlBoxActionPerformed(evt);
            }
        });
        jPanel21.add(perFlBox);

        groFlBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        groFlBox.setSelected(true);
        groFlBox.setText("Groundwater Exfiltration");
        groFlBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        groFlBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                groFlBoxActionPerformed(evt);
            }
        });
        jPanel21.add(groFlBox);

        rftPanel.add(jPanel21, java.awt.BorderLayout.WEST);

        jPanel16.add(rftPanel);

        qoutPanel.setLayout(new java.awt.BorderLayout());

        jPanel23.setLayout(new java.awt.GridLayout(8, 0));

        jLabel5.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel5.setText("Discharge Time Series (*.qout)");
        jLabel5.setMaximumSize(new java.awt.Dimension(179, 13));
        jLabel5.setMinimumSize(new java.awt.Dimension(179, 13));
        jLabel5.setPreferredSize(new java.awt.Dimension(179, 13));
        jPanel23.add(jLabel5);

        qNodesCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        qNodesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Outlet", "Internal Node 1", "Internal Node 2", "Internal Node 3", "Internal Node 4" }));
        qNodesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                qNodesComboActionPerformed(evt);
            }
        });
        jPanel23.add(qNodesCombo);

        dischBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        dischBox.setSelected(true);
        dischBox.setText("Discharge");
        dischBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        dischBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dischBoxActionPerformed(evt);
            }
        });
        jPanel23.add(dischBox);

        stageBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        stageBox.setSelected(true);
        stageBox.setText("Channel stage");
        stageBox.setMargin(new java.awt.Insets(0, 0, 0, 0));
        stageBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stageBoxActionPerformed(evt);
            }
        });
        jPanel23.add(stageBox);

        qoutPanel.add(jPanel23, java.awt.BorderLayout.WEST);

        jPanel16.add(qoutPanel);

        panelOutputs.addTab("Hydrograph Response", jPanel16);

        jPanel26.setLayout(new java.awt.BorderLayout());

        jPanel27.setLayout(new java.awt.GridLayout(1, 2));

        spaceParamsCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        spaceParamsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Available Variables", "Variable 01", "Variable 02", "Variable 03", "Variable 04" }));
        spaceParamsCombo.setLightWeightPopupEnabled(false);
        spaceParamsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                spaceParamsComboActionPerformed(evt);
            }
        });
        jPanel27.add(spaceParamsCombo);

        colorTableButton.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        colorTableButton.setText("Color Table");
        colorTableButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorTableButtonActionPerformed(evt);
            }
        });
        jPanel27.add(colorTableButton);

        jPanel26.add(jPanel27, java.awt.BorderLayout.NORTH);

        panelOutputs.addTab("Spatial Parameterization", jPanel26);

        jPanel17.setLayout(new java.awt.BorderLayout());

        jPanel20.setLayout(new java.awt.GridLayout(1, 3));

        avaTimesCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        avaTimesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Available Times", "Time 01", "Time 02", "Time 03", "Time 04", "Time-integrated Spatial Output" }));
        avaTimesCombo.setLightWeightPopupEnabled(false);
        avaTimesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avaTimesComboActionPerformed(evt);
            }
        });
        jPanel20.add(avaTimesCombo);

        avaVariablesCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        avaVariablesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Available Variables" }));
        avaVariablesCombo.setLightWeightPopupEnabled(false);
        avaVariablesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avaVariablesComboActionPerformed(evt);
            }
        });
        jPanel20.add(avaVariablesCombo);

        jPanel17.add(jPanel20, java.awt.BorderLayout.NORTH);

        jPanel22.setLayout(new java.awt.GridLayout(1, 5));

        pointsCheckBox_O.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pointsCheckBox_O.setSelected(true);
        pointsCheckBox_O.setText("Show Points");
        pointsCheckBox_O.setMargin(new java.awt.Insets(0, 0, 0, 0));
        pointsCheckBox_O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pointsCheckBox_OActionPerformed(evt);
            }
        });
        jPanel22.add(pointsCheckBox_O);

        netCheckBox_O.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        netCheckBox_O.setSelected(true);
        netCheckBox_O.setText("Show Network");
        netCheckBox_O.setMargin(new java.awt.Insets(0, 0, 0, 0));
        netCheckBox_O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                netCheckBox_OActionPerformed(evt);
            }
        });
        jPanel22.add(netCheckBox_O);

        trianglesCheckBox_O.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        trianglesCheckBox_O.setSelected(true);
        trianglesCheckBox_O.setText("Show Triangles");
        trianglesCheckBox_O.setMargin(new java.awt.Insets(0, 0, 0, 0));
        trianglesCheckBox_O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                trianglesCheckBox_OActionPerformed(evt);
            }
        });
        jPanel22.add(trianglesCheckBox_O);

        voronoiCheckBox_O.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        voronoiCheckBox_O.setSelected(true);
        voronoiCheckBox_O.setText("Show Voronoi Polygons");
        voronoiCheckBox_O.setMargin(new java.awt.Insets(0, 0, 0, 0));
        voronoiCheckBox_O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                voronoiCheckBox_OActionPerformed(evt);
            }
        });
        jPanel22.add(voronoiCheckBox_O);

        valuesCheckBox_O.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        valuesCheckBox_O.setSelected(true);
        valuesCheckBox_O.setText("Show Variable Values");
        valuesCheckBox_O.setMargin(new java.awt.Insets(0, 0, 0, 0));
        valuesCheckBox_O.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                valuesCheckBox_OActionPerformed(evt);
            }
        });
        jPanel22.add(valuesCheckBox_O);

        jPanel17.add(jPanel22, java.awt.BorderLayout.SOUTH);

        panelOutputs.addTab("Spatial Response", jPanel17);

        jPanel30.setLayout(new java.awt.GridLayout(1, 0));

        pixelPanel.setLayout(new java.awt.BorderLayout());

        jPanel31.setLayout(new java.awt.GridLayout(0, 3));

        jLabel8.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel8.setText("Local high resolution output (*.pixel)");
        jPanel31.add(jLabel8);

        pNodesCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pNodesCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Outlet", "Internal Node 1", "Internal Node 2", "Internal Node 3", "Internal Node 4" }));
        pNodesCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pNodesComboActionPerformed(evt);
            }
        });
        jPanel31.add(pNodesCombo);

        pNodesVarsCombo.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        pNodesVarsCombo.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Outlet", "Internal Node 1", "Internal Node 2", "Internal Node 3", "Internal Node 4" }));
        pNodesVarsCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pNodesVarsComboActionPerformed(evt);
            }
        });
        jPanel31.add(pNodesVarsCombo);

        pixelPanel.add(jPanel31, java.awt.BorderLayout.NORTH);

        jPanel30.add(pixelPanel);

        panelOutputs.addTab("Voronoi Node Output", jPanel30);

        jPanel35.setLayout(new java.awt.BorderLayout());

        jPanel36.setLayout(new java.awt.GridLayout(12, 1));

        jPanel39.setLayout(new java.awt.GridLayout(1, 0));

        jLabel1.setText("GRID HASH CODE : ");
        jPanel39.add(jLabel1);

        md5_grid_textField.setEditable(false);
        jPanel39.add(md5_grid_textField);

        jPanel36.add(jPanel39);

        jPanel54.setLayout(new java.awt.GridLayout(1, 0));

        jLabel10.setText("GRID PROJECTION : ");
        jPanel54.add(jLabel10);

        projsComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Projection ...", "Item 2", "Item 3", "Item 4" }));
        jPanel54.add(projsComboBox);

        jPanel36.add(jPanel54);

        jPanel47.setLayout(new java.awt.GridLayout(1, 0));

        jLabel25.setText("SIMULATION HASH CODE : ");
        jPanel47.add(jLabel25);

        md5_simu_textField.setEditable(false);
        jPanel47.add(md5_simu_textField);

        jPanel36.add(jPanel47);

        jPanel55.setLayout(new java.awt.GridLayout(1, 0));

        jLabel33.setText("SIMULATION INITIAL TIME : ");
        jPanel55.add(jLabel33);

        jPanel38.setLayout(new java.awt.GridLayout(1, 6));

        monthComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        monthComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Month", "Item 2", "Item 3", "Item 4" }));
        jPanel38.add(monthComboBox);

        dayComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        dayComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Day", "Item 2", "Item 3", "Item 4" }));
        jPanel38.add(dayComboBox);

        yearComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        yearComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Year", "Item 2", "Item 3", "Item 4" }));
        jPanel38.add(yearComboBox);

        hourComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        hourComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Hour", "Item 2", "Item 3", "Item 4" }));
        jPanel38.add(hourComboBox);

        minComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        minComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Minute", "Item 2", "Item 3", "Item 4" }));
        jPanel38.add(minComboBox);

        timeZoneComboBox.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        timeZoneComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "UTC", "Item 2", "Item 3", "Item 4" }));
        jPanel38.add(timeZoneComboBox);

        jPanel55.add(jPanel38);

        jPanel36.add(jPanel55);

        jPanel56.setLayout(new java.awt.GridLayout(1, 0));

        jLabel34.setText("*.in FILE LOCATION : ");
        jPanel56.add(jLabel34);

        jPanel40.setLayout(new java.awt.BorderLayout());

        inFileTextField.setText("/path/to/simulation.in");
        jPanel40.add(inFileTextField, java.awt.BorderLayout.CENTER);

        findFileButton.setText("Find");
        findFileButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                findFileButtonActionPerformed(evt);
            }
        });
        jPanel40.add(findFileButton, java.awt.BorderLayout.EAST);

        jPanel56.add(jPanel40);

        jPanel36.add(jPanel56);

        jPanel48.setLayout(new java.awt.GridLayout(1, 0));

        jLabel26.setText("Polygons Upload : ");
        jPanel48.add(jLabel26);

        polyProgressBar.setStringPainted(true);
        jPanel48.add(polyProgressBar);

        jPanel36.add(jPanel48);

        jPanel49.setLayout(new java.awt.GridLayout(1, 0));

        jLabel27.setText("Reaches Upload : ");
        jPanel49.add(jLabel27);

        reachProgressBar.setStringPainted(true);
        jPanel49.add(reachProgressBar);

        jPanel36.add(jPanel49);

        jPanel53.setLayout(new java.awt.GridLayout(1, 0));

        jLabel32.setText("Aggreagated (space and time) Response Upload : ");
        jPanel53.add(jLabel32);

        aggProgressBar.setStringPainted(true);
        jPanel53.add(aggProgressBar);

        jPanel36.add(jPanel53);

        jPanel57.setLayout(new java.awt.GridLayout(1, 0));

        jLabel35.setText("Aggreagated (time) Response Upload : ");
        jPanel57.add(jLabel35);

        aggTimeProgressBar.setStringPainted(true);
        jPanel57.add(aggTimeProgressBar);

        jPanel36.add(jPanel57);

        jPanel51.setLayout(new java.awt.GridLayout(1, 0));

        jLabel29.setText("Hydrographs Upload : ");
        jPanel51.add(jLabel29);

        hydProgressBar.setStringPainted(true);
        jPanel51.add(hydProgressBar);

        jPanel36.add(jPanel51);

        jPanel52.setLayout(new java.awt.GridLayout(1, 0));

        jLabel30.setText("Node Output Upload : ");
        jPanel52.add(jLabel30);

        pixelProgressBar.setStringPainted(true);
        jPanel52.add(pixelProgressBar);

        jPanel36.add(jPanel52);

        jPanel50.setLayout(new java.awt.GridLayout(1, 0));

        jLabel28.setText("Spatial Output Upload : ");
        jPanel50.add(jLabel28);

        dynamicOutputProgressBar.setStringPainted(true);
        jPanel50.add(dynamicOutputProgressBar);

        jPanel36.add(jPanel50);

        jPanel35.add(jPanel36, java.awt.BorderLayout.CENTER);

        jPanel37.setLayout(new java.awt.GridLayout(1, 0));

        sendButton.setText("BEGIN TRANSACTION >>>");
        sendButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendButtonActionPerformed(evt);
            }
        });
        jPanel37.add(sendButton);

        jPanel35.add(jPanel37, java.awt.BorderLayout.SOUTH);

        panelOutputs.addTab("Data Upload", jPanel35);

        jPanel34.setLayout(new java.awt.BorderLayout());
        panelOutputs.addTab("Network Structure", jPanel34);

        jPanel_O.add(panelOutputs, java.awt.BorderLayout.CENTER);

        panel_IO.addTab("Output Analysis", jPanel_O);

        getContentPane().add(panel_IO, java.awt.BorderLayout.CENTER);

        jPanel15.setLayout(new java.awt.GridLayout(3, 1, 0, 3));

        jPanel28.setLayout(new java.awt.GridLayout(1, 4));

        jPanel61.setLayout(new java.awt.BorderLayout());

        jLabel31.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel31.setText("Easting : ");
        jPanel61.add(jLabel31, java.awt.BorderLayout.WEST);

        longitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        longitudeLabel.setText("00:00:00.00 W [000]");
        jPanel61.add(longitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel61);

        jPanel29.setLayout(new java.awt.BorderLayout());

        jLabel7.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel7.setText("Northing : ");
        jPanel29.add(jLabel7, java.awt.BorderLayout.WEST);

        latitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        latitudeLabel.setText("00:00:00.00 N [000]");
        jPanel29.add(latitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel29);

        jPanel62.setLayout(new java.awt.BorderLayout());

        jLabel323.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel323.setText("Value : ");
        jPanel62.add(jLabel323, java.awt.BorderLayout.WEST);

        valueLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        valueLabel.setText("0000");
        jPanel62.add(valueLabel, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel62);

        jPanel15.add(jPanel28);

        jPanel18.setLayout(new java.awt.BorderLayout());

        changePath.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        changePath.setText("Change Reference Path");
        changePath.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changePathActionPerformed(evt);
            }
        });
        jPanel18.add(changePath, java.awt.BorderLayout.EAST);

        jLabel2.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel2.setText("Reference Path : ");
        jPanel18.add(jLabel2, java.awt.BorderLayout.WEST);

        pathTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jPanel18.add(pathTextField, java.awt.BorderLayout.CENTER);

        jPanel15.add(jPanel18);

        jPanel19.setLayout(new java.awt.BorderLayout());

        jLabel3.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jLabel3.setText("Base Name : ");
        jPanel19.add(jLabel3, java.awt.BorderLayout.WEST);

        baseNameTextField.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        jPanel19.add(baseNameTextField, java.awt.BorderLayout.CENTER);

        jPanel15.add(jPanel19);

        getContentPane().add(jPanel15, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void resetLatticeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetLatticeActionPerformed
        zrSlider.setValue(0);
        try {
            plotPoints();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_resetLatticeActionPerformed

    private void writeInFileActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_writeInFileActionPerformed
        try {
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser(pathTextField.getText());
            fc.setFileSelectionMode(fc.FILES_ONLY);
            fc.setDialogTitle("Input File Selection");
            javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("in","IN File");
            fc.addChoosableFileFilter(mdtFilter);
            fc.showSaveDialog(this);
            
            if (fc.getSelectedFile() == null) return;
            
            String fileAscSalida=fc.getSelectedFile().getAbsolutePath();//pathTextField.getText()+"/"+baseNameTextField.getText()+".in";
            
            java.io.FileOutputStream        outputDir;
            java.io.OutputStreamWriter      newfile;
            java.io.BufferedOutputStream    bufferout;
            String                          retorno="\n";
            
            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            String[] inFileText=inFileTextArea.getText().split("\n");
            for (int i = 0; i < inFileText.length; i++) {
                if(inFileText[i].contains("basename")){
                   String[] pathToWrite=inFileText[i].split("basename");
                   newfile.write(pathToWrite[0]);
                   newfile.write(baseNameTextField.getText());
                   if(pathToWrite.length > 1) newfile.write(pathToWrite[1]);
                   newfile.write(retorno);
                } else{
                    newfile.write(inFileText[i]+retorno);
                }
            }
            newfile.write(retorno);
            
            newfile.close();
            bufferout.close();
            outputDir.close();
            
            fileAscSalida=pathTextField.getText()+"/Input/Nodes/oNodes.dat";
                    
            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            newfile.write("1"+retorno);
            newfile.write("0");
            
            newfile.close();
            bufferout.close();
            outputDir.close();
            
            fileAscSalida=pathTextField.getText()+"/Input/Nodes/hNodes.dat";
                    
            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            newfile.write("1"+retorno);
            newfile.write("0");
            
            newfile.close();
            bufferout.close();
            outputDir.close();
            
            fileAscSalida=pathTextField.getText()+"/Input/Nodes/pNodes.dat";
                    
            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            newfile.write("1"+retorno);
            newfile.write("0");
            
            newfile.close();
            bufferout.close();
            outputDir.close();
            
            
            
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_writeInFileActionPerformed

    private void selectGWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectGWActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersHydPath);
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("Initial Groundwater File Selection");
        javax.swing.filechooser.FileFilter vhcFilter = new visad.util.ExtensionFileFilter("metaVHC","Initial Groundwater File");
        fcI.addChoosableFileFilter(vhcFilter);
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        try{
            java.io.File theMetaFile=new java.io.File(fileInput.getPath().substring(0,fileInput.getPath().lastIndexOf("."))+".metaVHC");
            hydroScalingAPI.subGUIs.widgets.HydroOpenDialog openVhc=new hydroScalingAPI.subGUIs.widgets.HydroOpenDialog(mainFrame,new hydroScalingAPI.io.MetaRaster(theMetaFile),"Select");
            openVhc.setVisible(true);
            if (!openVhc.mapsSelected()){
                return;
            }
            
            javax.swing.JDialog me=new javax.swing.JDialog(this,"Wait ...",false);
            me.setBounds(0,0, 200, 10);
            java.awt.Rectangle marcoParent=this.getBounds();
            java.awt.Rectangle thisMarco=me.getBounds();
            me.setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
            me.setVisible(true);
            
            hydroScalingAPI.io.MetaRaster theActualMeta=openVhc.getSelectedMetaRasters()[0];
            visad.FlatField theActualField=theActualMeta.getField();
            
            double[] er=eastMap_I.getRange();
            double[] nr=northMap_I.getRange();
            
            int ncMap=(int)Math.round((er[1]-er[0])/100)+6;
            int nrMap=(int)Math.round((nr[1]-nr[0])/100)+6;
            
            visad.FunctionType  func_xEasting_yNorthing_to_Color=new FunctionType(domainXLYL,voiColor);
            visad.Linear2DSet   domainExtent = new visad.Linear2DSet(domainXLYL,er[0]-300,er[0]+ncMap*100+300,ncMap,
                                                                                nr[0]-300,nr[0]+nrMap*100+300,nrMap);
            field_GW = new visad.FlatField( func_xEasting_yNorthing_to_Color, domainExtent);
            
            float[][] locationsEN=domainExtent.getSamples();
            
            int numPoints=locationsEN[0].length;
            Gdc_Coord_3d[] gdc=new Gdc_Coord_3d[numPoints];
            Utm_Coord_3d[] utm=new Utm_Coord_3d[numPoints];

            for(int i=0;i<numPoints;i++){
                gdc[i]=new Gdc_Coord_3d();
                utm[i]=new Utm_Coord_3d();
                    utm[i].x=locationsEN[0][i]+basTIN_I.minX;
                    utm[i].y=locationsEN[1][i]+basTIN_I.minY;
                    utm[i].zone=basTIN_I.TIN_Zone;
                    utm[i].hemisphere_north=basTIN_I.TIN_Hemisphere;
            }

            Utm_To_Gdc_Converter.Init(new WE_Ellipsoid());
            Utm_To_Gdc_Converter.Convert(utm,gdc);
            
            float[][] valueVar=new float[1][ncMap*nrMap];
            visad.RealTuple spotValue;
            
            
            for (int i = 0; i < valueVar[0].length; i++) {
                
                spotValue=(visad.RealTuple) theActualField.evaluate(new visad.RealTuple(domain, new double[] {gdc[i].longitude,gdc[i].latitude}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);

                valueVar[0][i]=(float)spotValue.getValues()[0];
                
            }
            
            field_GW.setSamples( valueVar, false );
            
            voiColorMap_GW.setRange(0,500);
            
            data_refFill_GW.setData(field_GW);
            
            exportGW.setEnabled(true);
            
            data_refPolygon_GW = new DataReferenceImpl("data_refPolygon_GW"); 
            data_refPolygon_GW.setData(basTIN_I.getPolygon());
            
            display_GW.addReference(data_refPolygon_GW);
            
            me.dispose();
            
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        } catch(java.io.IOException ex){
            ex.printStackTrace();
        }
    }//GEN-LAST:event_selectGWActionPerformed

    private void selectSoilsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectSoilsActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersHydPath);
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("Soils File Selection");
        javax.swing.filechooser.FileFilter vhcFilter = new visad.util.ExtensionFileFilter("metaVHC","Soils File");
        fcI.addChoosableFileFilter(vhcFilter);
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        try{
            java.io.File theMetaFile=new java.io.File(fileInput.getPath().substring(0,fileInput.getPath().lastIndexOf("."))+".metaVHC");
            hydroScalingAPI.subGUIs.widgets.HydroOpenDialog openVhc=new hydroScalingAPI.subGUIs.widgets.HydroOpenDialog(mainFrame,new hydroScalingAPI.io.MetaRaster(theMetaFile),"Select");
            openVhc.setVisible(true);
            if (!openVhc.mapsSelected()){
                return;
            }
            
            javax.swing.JDialog me=new javax.swing.JDialog(this,"Wait ...",false);
            me.setBounds(0,0, 200, 10);
            java.awt.Rectangle marcoParent=this.getBounds();
            java.awt.Rectangle thisMarco=me.getBounds();
            me.setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
            me.setVisible(true);
            
            hydroScalingAPI.io.MetaRaster theActualMeta=openVhc.getSelectedMetaRasters()[0];
            visad.FlatField theActualField=theActualMeta.getField();
            
            if(theActualMeta.getUnits().equalsIgnoreCase("categories"))
                soilCategories=theActualMeta.getCategoriesTable();
            
            double[] er=eastMap_I.getRange();
            double[] nr=northMap_I.getRange();
            
            int ncMap=(int)Math.round((er[1]-er[0])/100)+6;
            int nrMap=(int)Math.round((nr[1]-nr[0])/100)+6;
            
            visad.FunctionType  func_xEasting_yNorthing_to_Color=new FunctionType(domainXLYL,voiColor);
            visad.Linear2DSet   domainExtent = new visad.Linear2DSet(domainXLYL,er[0]-300,er[0]+ncMap*100+300,ncMap,
                                                                                nr[0]-300,nr[0]+nrMap*100+300,nrMap);
            field_SOIL = new visad.FlatField( func_xEasting_yNorthing_to_Color, domainExtent);
            
            float[][] locationsEN=domainExtent.getSamples();
            
            int numPoints=locationsEN[0].length;
            Gdc_Coord_3d[] gdc=new Gdc_Coord_3d[numPoints];
            Utm_Coord_3d[] utm=new Utm_Coord_3d[numPoints];

            for(int i=0;i<numPoints;i++){
                gdc[i]=new Gdc_Coord_3d();
                utm[i]=new Utm_Coord_3d();
                    utm[i].x=locationsEN[0][i]+basTIN_I.minX;
                    utm[i].y=locationsEN[1][i]+basTIN_I.minY;
                    utm[i].zone=basTIN_I.TIN_Zone;
                    utm[i].hemisphere_north=basTIN_I.TIN_Hemisphere;
            }

            Utm_To_Gdc_Converter.Init(new WE_Ellipsoid());
            Utm_To_Gdc_Converter.Convert(utm,gdc);
            
            float[][] valueVar=new float[1][ncMap*nrMap];
            visad.RealTuple spotValue;
            
            
            for (int i = 0; i < valueVar[0].length; i++) {
                
                spotValue=(visad.RealTuple) theActualField.evaluate(new visad.RealTuple(domain, new double[] {gdc[i].longitude,gdc[i].latitude}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);

                valueVar[0][i]=(float)spotValue.getValues()[0];
                
            }
            
            field_SOIL.setSamples( valueVar, false );
            
            voiColorMap_SOIL.setRange(0,500);
            
            data_refFill_SOIL.setData(field_SOIL);
            
            exportSoils.setEnabled(true);
            
            data_refPolygon_SOIL = new DataReferenceImpl("data_refPolygon_SOIL"); 
            data_refPolygon_SOIL.setData(basTIN_I.getPolygon());
            
            display_SOIL.addReference(data_refPolygon_SOIL);
            
            me.dispose();
            
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        } catch(java.io.IOException ex){
            ex.printStackTrace();
        }
    }//GEN-LAST:event_selectSoilsActionPerformed

    private void exportGWActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportGWActionPerformed
        try {
            String fileAscSalida=pathTextField.getText()+"/Input/"+baseNameTextField.getText()+".iwt";
            
            java.io.FileOutputStream        outputDir;
            java.io.OutputStreamWriter      newfile;
            java.io.BufferedOutputStream    bufferout;
            String                          retorno="\n";
            
            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            
            double[] er=eastMap_I.getRange();
            double[] nr=northMap_I.getRange();
            
            int ncMap=(int)Math.round((er[1]-er[0])/100)+6;
            int nrMap=(int)Math.round((nr[1]-nr[0])/100)+6;
            
            newfile.write("ncols         "+ncMap+retorno);
            newfile.write("nrows         "+nrMap+retorno);
            newfile.write("xllcorner     "+(er[0]+basTIN_I.minX-300)+retorno);
            newfile.write("yllcorner     "+(nr[0]+basTIN_I.minY-300)+retorno);
            newfile.write("cellsize      "+100+retorno);
            newfile.write("NODATA_value  "+"-9999"+retorno);
            
            double[][] values_GW=field_GW.getValues();
            
            int k=0;
            for (int j=0;j<values_GW[0].length;j++) {
                newfile.write(values_GW[0][nrMap*ncMap-(k+1)*ncMap+(j%ncMap)]+" ");
                if((j+1)%ncMap == 0) {
                    k++;
                    newfile.write(retorno);
                }
            }
            
            newfile.write(retorno);
            
            newfile.close();
            bufferout.close();
            outputDir.close();
            
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_exportGWActionPerformed

    private void exportSoilsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportSoilsActionPerformed
        try {
            String fileAscSalida=pathTextField.getText()+"/Input/"+baseNameTextField.getText()+".soi";
            
            java.io.FileOutputStream        outputDir;
            java.io.OutputStreamWriter      newfile;
            java.io.BufferedOutputStream    bufferout;
            String                          retorno="\n";
            
            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            
            double[] er=eastMap_I.getRange();
            double[] nr=northMap_I.getRange();
            
            int ncMap=(int)Math.round((er[1]-er[0])/100)+6;
            int nrMap=(int)Math.round((nr[1]-nr[0])/100)+6;
            
            newfile.write("ncols         "+ncMap+retorno);
            newfile.write("nrows         "+nrMap+retorno);
            newfile.write("xllcorner     "+(er[0]+basTIN_I.minX-300)+retorno);
            newfile.write("yllcorner     "+(nr[0]+basTIN_I.minY-300)+retorno);
            newfile.write("cellsize      "+100+retorno);
            newfile.write("NODATA_value  "+"-9999"+retorno);
            
            double[][] values_SOIL=field_SOIL.getValues();
            java.util.Vector uniqueValues=new java.util.Vector<Integer>();
            
            int k=0;
            for (int j=0;j<values_SOIL[0].length;j++) {
                Integer valueInteger=new Integer((int)values_SOIL[0][j]);
                if(!uniqueValues.contains(valueInteger)) uniqueValues.add(valueInteger);
            }
            
            java.util.Hashtable histogramCounter=new java.util.Hashtable<Integer,Integer>();
            
            for (Iterator it = uniqueValues.iterator(); it.hasNext();) {
                histogramCounter.put((Integer)it.next(),new Integer(0));
            }
            
            k=0;
            for (int j=0;j<values_SOIL[0].length;j++) {
                Integer valueInteger=new Integer((int)values_SOIL[0][nrMap*ncMap-(k+1)*ncMap+(j%ncMap)]);
                
                histogramCounter.put(valueInteger,new Integer(((Integer)histogramCounter.get(valueInteger)).intValue()+1));
                
                newfile.write((uniqueValues.indexOf(valueInteger)+1)+" ");
                if((j+1)%ncMap == 0) {
                    k++;
                    newfile.write(retorno);
                }
            }
            
            newfile.write(retorno);
            
            newfile.close();
            bufferout.close();
            outputDir.close();
            
            fileAscSalida=pathTextField.getText()+"/Input/"+baseNameTextField.getText()+".sdt";
            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            newfile.write(uniqueValues.size()+" 12"+retorno);
            k=1;
            for (Iterator it = uniqueValues.iterator(); it.hasNext();) {
                Integer elem = (Integer) it.next();
                String type=""+elem.intValue();
                if(soilCategories != null)
                    type=(String)soilCategories.get(type);
                newfile.write((k++)+" Corresponds to Soil Type "+type+" which covers "+(((Integer)histogramCounter.get(elem)).intValue()/((float)values_SOIL[0].length)*100)+"%. Edit this line appropriate parameter values"+retorno);
            }
            
            newfile.close();
            bufferout.close();
            outputDir.close();
            
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_exportSoilsActionPerformed

    private void exportLandCoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportLandCoverActionPerformed
        try {
            String fileAscSalida=pathTextField.getText()+"/Input/"+baseNameTextField.getText()+".lan";
            
            java.io.FileOutputStream        outputDir;
            java.io.OutputStreamWriter      newfile;
            java.io.BufferedOutputStream    bufferout;
            String                          retorno="\n";
            
            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            
            double[] er=eastMap_I.getRange();
            double[] nr=northMap_I.getRange();
            
            int ncMap=(int)Math.round((er[1]-er[0])/100)+6;
            int nrMap=(int)Math.round((nr[1]-nr[0])/100)+6;
            
            newfile.write("ncols         "+ncMap+retorno);
            newfile.write("nrows         "+nrMap+retorno);
            newfile.write("xllcorner     "+(er[0]+basTIN_I.minX-300)+retorno);
            newfile.write("yllcorner     "+(nr[0]+basTIN_I.minY-300)+retorno);
            newfile.write("cellsize      "+100+retorno);
            newfile.write("NODATA_value  "+"-9999"+retorno);
            
            double[][] values_LULC=field_LULC.getValues();
            java.util.Vector uniqueValues=new java.util.Vector<Integer>();
            
            int k=0;
            for (int j=0;j<values_LULC[0].length;j++) {
                Integer valueInteger=new Integer((int)values_LULC[0][j]);
                
                if(!uniqueValues.contains(valueInteger))uniqueValues.add(valueInteger);
            }
            
            java.util.Hashtable histogramCounter=new java.util.Hashtable<Integer,Integer>();
            
            for (Iterator it = uniqueValues.iterator(); it.hasNext();) {
                Integer elem=(Integer)it.next();
                histogramCounter.put(elem,new Integer(0));
            }
            
            k=0;
            for (int j=0;j<values_LULC[0].length;j++) {
                Integer valueInteger=new Integer((int)values_LULC[0][nrMap*ncMap-(k+1)*ncMap+(j%ncMap)]);
                histogramCounter.put(valueInteger,new Integer(((Integer)histogramCounter.get(valueInteger)).intValue()+1));
                
                newfile.write((uniqueValues.indexOf(valueInteger)+1)+" ");
                if((j+1)%ncMap == 0) {
                    k++;
                    newfile.write(retorno);
                }
            }
            
            newfile.write(retorno);
            
            newfile.close();
            bufferout.close();
            outputDir.close();
            
            fileAscSalida=pathTextField.getText()+"/Input/"+baseNameTextField.getText()+".ldt";
            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            newfile.write(uniqueValues.size()+" 12"+retorno);
            k=1;
            for (Iterator it = uniqueValues.iterator(); it.hasNext();) {
                Integer elem = (Integer) it.next();
                String type=""+elem.intValue();
                if(landCategories != null)
                    type=(String)landCategories.get(type);
                newfile.write((k++)+" Corresponds to Land Use Type "+type+" which covers "+(((Integer)histogramCounter.get(elem)).intValue()/((float)values_LULC[0].length)*100)+"%. Edit this line appropriate parameter values"+retorno);
            }
            
            newfile.close();
            bufferout.close();
            outputDir.close();
            
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_exportLandCoverActionPerformed

    private void selectLandCoverActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectLandCoverActionPerformed
        javax.swing.JFileChooser fcI=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersHydPath);
        fcI.setFileSelectionMode(fcI.FILES_ONLY);
        fcI.setDialogTitle("Land Cover File Selection");
        javax.swing.filechooser.FileFilter vhcFilter = new visad.util.ExtensionFileFilter("metaVHC","Land Cover File");
        fcI.addChoosableFileFilter(vhcFilter);
        int result=fcI.showOpenDialog(this);
        java.io.File fileInput = fcI.getSelectedFile();
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fcI.getSelectedFile().isFile()) return;
        
        try{
            
            java.io.File theMetaFile=new java.io.File(fileInput.getPath().substring(0,fileInput.getPath().lastIndexOf("."))+".metaVHC");
            hydroScalingAPI.subGUIs.widgets.HydroOpenDialog openVhc=new hydroScalingAPI.subGUIs.widgets.HydroOpenDialog(mainFrame,new hydroScalingAPI.io.MetaRaster(theMetaFile),"Select");
            openVhc.setVisible(true);
            if (!openVhc.mapsSelected()){
                return;
            }
            
            javax.swing.JDialog me=new javax.swing.JDialog(this,"Wait ...",false);
            me.setBounds(0,0, 200, 10);
            java.awt.Rectangle marcoParent=this.getBounds();
            java.awt.Rectangle thisMarco=me.getBounds();
            me.setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
            me.setVisible(true);
            
            hydroScalingAPI.io.MetaRaster theActualMeta=openVhc.getSelectedMetaRasters()[0];
            visad.FlatField theActualField=theActualMeta.getField();
            
            if(theActualMeta.getUnits().equalsIgnoreCase("categories"))
                landCategories=theActualMeta.getCategoriesTable();
            
            double[] er=eastMap_I.getRange();
            double[] nr=northMap_I.getRange();
            
            int ncMap=(int)Math.round((er[1]-er[0])/100)+6;
            int nrMap=(int)Math.round((nr[1]-nr[0])/100)+6;
            
            visad.FunctionType  func_xEasting_yNorthing_to_Color=new FunctionType(domainXLYL,voiColor);
            visad.Linear2DSet   domainExtent = new visad.Linear2DSet(domainXLYL,er[0]-300,er[0]+ncMap*100+300,ncMap,
                                                                                nr[0]-300,nr[0]+nrMap*100+300,nrMap);
            field_LULC = new visad.FlatField( func_xEasting_yNorthing_to_Color, domainExtent);
            
            float[][] locationsEN=domainExtent.getSamples();
            
            int numPoints=locationsEN[0].length;
            Gdc_Coord_3d[] gdc=new Gdc_Coord_3d[numPoints];
            Utm_Coord_3d[] utm=new Utm_Coord_3d[numPoints];

            for(int i=0;i<numPoints;i++){
                gdc[i]=new Gdc_Coord_3d();
                utm[i]=new Utm_Coord_3d();
                    utm[i].x=locationsEN[0][i]+basTIN_I.minX;
                    utm[i].y=locationsEN[1][i]+basTIN_I.minY;
                    utm[i].zone=basTIN_I.TIN_Zone;
                    utm[i].hemisphere_north=basTIN_I.TIN_Hemisphere;
            }

            Utm_To_Gdc_Converter.Init(new WE_Ellipsoid());
            Utm_To_Gdc_Converter.Convert(utm,gdc);
            
            float[][] valueVar=new float[1][ncMap*nrMap];
            visad.RealTuple spotValue;
            
            
            for (int i = 0; i < valueVar[0].length; i++) {
                
                spotValue=(visad.RealTuple) theActualField.evaluate(new visad.RealTuple(domain, new double[] {gdc[i].longitude,gdc[i].latitude}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);

                valueVar[0][i]=(float)spotValue.getValues()[0];
                
            }
            
            field_LULC.setSamples( valueVar, false );
            
            voiColorMap_LULC.setRange(0,100);
            
            data_refFill_LULC.setData(field_LULC);
            
            exportLandCover.setEnabled(true);
            
            data_refPolygon_LULC = new DataReferenceImpl("data_refPolygon_LULC"); 
            data_refPolygon_LULC.setData(basTIN_I.getPolygon());
            
            display_LULC.addReference(data_refPolygon_LULC);
            
            me.dispose();
            
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        } catch(java.io.IOException ex){
            ex.printStackTrace();
        }
        
        
    }//GEN-LAST:event_selectLandCoverActionPerformed

    private void computeLatticeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_computeLatticeActionPerformed
        
        try {

            javax.swing.JDialog me=new javax.swing.JDialog(this,"Wait ...",false);

            me.setBounds(0,0, 200, 10);
            java.awt.Rectangle marcoParent=this.getBounds();
            java.awt.Rectangle thisMarco=me.getBounds();
            me.setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);


            me.setVisible(true);
        
            plotPoints();
            
            me.dispose();
            
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        } catch (java.io.IOException ex){
            ex.printStackTrace();
        }
    }//GEN-LAST:event_computeLatticeActionPerformed

    private void findFileButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_findFileButtonActionPerformed
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser(pathTextField.getText());
        fc.setFileSelectionMode(fc.FILES_ONLY);
        fc.setDialogTitle("tRIBS *.in File Selection");
        javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("in","tRIBS Set Up File");
        fc.addChoosableFileFilter(mdtFilter);
        int result = fc.showDialog(this,"Select");
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        if (!fc.getSelectedFile().isFile()) return;
        
        inFileTextField.setText(fc.getSelectedFile().getAbsolutePath());
        
    }//GEN-LAST:event_findFileButtonActionPerformed

    private void sendButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendButtonActionPerformed
        
        sendButton.setEnabled(false);

        //Determine if projection and ini-date were given
        
//        projsComboBox.setSelectedIndex(594);
//        monthComboBox.setSelectedIndex(11);
//        dayComboBox.setSelectedIndex(1);
//        yearComboBox.setSelectedIndex(48);
//        hourComboBox.setSelectedIndex(1);
//        minComboBox.setSelectedIndex(1);
//        timeZoneComboBox.setSelectedIndex(5);
//        inFileTextField.setText("/Users/ricardo/workFiles/tribsWork/sampleTribs/SEVILLETA/smallbasin.in");
        
        
        if(projsComboBox.getSelectedIndex() == 0 ||
           monthComboBox.getSelectedIndex() == 0 ||
           dayComboBox.getSelectedIndex() == 0 ||
           yearComboBox.getSelectedIndex() == 0 ||
           hourComboBox.getSelectedIndex() == 0 ||
           minComboBox.getSelectedIndex() == 0 ||
           timeZoneComboBox.getSelectedIndex() == 0){
            
            Object[] options = {"OK"};
            if(javax.swing.JOptionPane.showOptionDialog(mainFrame, "Please select a projection and initial simulation time before initiating transaction", "Atention", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,null, options, options[0]) == 1) return;
            return;
            
        }
        
        java.io.File theInFile=new java.io.File(inFileTextField.getText());
        String fullLine="",fullFile="";
        
        if(!theInFile.exists()){
            Object[] options = { "OK"};
            if(javax.swing.JOptionPane.showOptionDialog(mainFrame, "Please select a valid *.in file", "Atention", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,null, options, options[0]) == 1) return;
            return;
        } else {
            try {
                java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(theInFile));
                fullLine=fileMeta.readLine();
                while (fullLine != null) {
                    fullFile+=fullLine+"\n";
                    fullLine=fileMeta.readLine();
                }

                fileMeta.close();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        
        
        final String TheInFileText=fullFile;
        Runnable pushData = new Runnable() {
                public void run() {
                    int gridID=0;
                    int simuID=0;

                    try {
                        
                        java.util.Date startTime=new java.util.Date();
                        System.out.println("> Database transaction starts at :"+startTime.toString());

                        //Determine if grid exists in database

                        Statement st = conn.createStatement();

                        ResultSet rs = st.executeQuery("SELECT COUNT(grid_hash) FROM nmt_model_grid WHERE grid_hash = '"+md5_grid_textField.getText()+"'");
                        rs.next();
                        if(rs.getString(1).equalsIgnoreCase("0")){
                            //Upload if necessary

                            rs = st.executeQuery("SELECT NEXTVAL('nmt_model_grid_id_seq')");
                            rs.next();
                            gridID=Integer.valueOf(rs.getString(1));

                            String epsgCode=(String)projsComboBox.getSelectedItem();
                            System.out.println(epsgCode);
                            epsgCode=epsgCode.substring(1,epsgCode.lastIndexOf("]"));

                            //First register the grid

                            st.execute("INSERT INTO nmt_model_grid (id, grid_hash, proj_epsg) VALUES ("+gridID+",'"+md5_grid_textField.getText()+"',"+epsgCode+")");

                            //Get info about the polygons and static values

                            UnionSet pol=basTIN_O.getPolygonsUnionSet();
                            SampledSet[] pols=pol.getSets();
                            
                            float[][] calcNodeLocation=basTIN_O.getPointsFlatField().getFloats(true);

                            String[] fileKey={"Initial Condition",baseNameTextField.getText()+".0000_00d","Only Network"};

                            float[] val_node_id = fdm.getValues(fileKey[1],0);
                            float[] val_z = fdm.getValues(fileKey[1],1);
                            float[] val_s = fdm.getValues(fileKey[1],2);
                            float[] val_v_ar = fim.getValues(fileKey[0],3);
                            float[] val_c_ar = fim.getValues(fileKey[0],4);
                            float[] val_curv = fim.getValues(fileKey[0],5);
                            float[] val_edg_l = fim.getValues(fileKey[0],6);
                            float[] val_tan_slp = fim.getValues(fileKey[0],7);
                            float[] val_f_width = fim.getValues(fileKey[0],8);
                            float[] val_aspect = fim.getValues(fileKey[0],9);
                            float[] val_soil_type = fdm.getValues(fileKey[1],25);
                            float[] val_land_use = fdm.getValues(fileKey[1],26);
                            float[] val_chan_w = fonm.getValues(fileKey[2],0);
                            float[] val_chan_l = fonm.getValues(fileKey[2],1);
                            float[] val_chan_s = fonm.getValues(fileKey[2],2);
                            float[] val_chan_ua = fonm.getValues(fileKey[2],3);

                            for (int j = 0; j < pols.length; j+=100) {
                                int k;
                                for (k = j; k < j+100 && k < pols.length; k++) {

                                    float[][] polsVals=((Gridded2DSet)pols[k]).getSamples();

                                    String pointString="'POINT("+(calcNodeLocation[0][(int)val_node_id[k]]+basTIN_O.minX)+","+(calcNodeLocation[1][(int)val_node_id[k]]+basTIN_O.minY)+")'";

                                    String polygonString="'POLYGON((";

                                    for (int i = 0; i < polsVals[0].length-1; i++) {
                                        polygonString+=""+(polsVals[0][i]+basTIN_O.minX)+" "+(polsVals[1][i]+basTIN_O.minY)+",";
                                    }
                                    polygonString+=""+(polsVals[0][0]+basTIN_O.minX)+" "+(polsVals[1][0]+basTIN_O.minY)+"))'";

                                    if(Float.isNaN(val_chan_w[k])) val_chan_w[k]=-9;
                                    if(Float.isNaN(val_chan_l[k])) val_chan_l[k]=-9;
                                    if(Float.isNaN(val_chan_s[k])) val_chan_s[k]=-9;
                                    if(Float.isNaN(val_chan_ua[k])) val_chan_ua[k]=-9;

                                    st.addBatch("INSERT INTO nmt_model_polygon ( id,grid_id,geom_txt_poly,epsgcode,geom_txt_pt,z,s,v_ar,c_ar,curv,edg_l,tan_slp,f_width,aspect,soil_type,land_use,chan_w,chan_l,chan_s,chan_ua)" +
                                                " VALUES("+(gridID*10000000+(int)val_node_id[k])+","+gridID+","+polygonString+","+epsgCode+","+pointString+","+val_z[k]+","+val_s[k]+","+val_v_ar[k]+","+val_c_ar[k]+","+val_curv[k]+","+val_edg_l[k]+","+val_tan_slp[k]+","+val_f_width[k]+","+val_aspect[k]+","+val_soil_type[k]+","+val_land_use[k]+","+val_chan_w[k]+","+val_chan_l[k]+","+val_chan_s[k]+","+val_chan_ua[k]+")");

                                }

                                int[] s=st.executeBatch();

                                polyProgressBar.setValue((int)(100*k/(float)pols.length));
                                
                                st.clearBatch();
                            }

                            fim.clearData();
                            
                            //Create a view for the data view_grid_00000
                            
                            java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
                            labelFormat.setGroupingUsed(false);
                            labelFormat.setMinimumIntegerDigits(5);
                            
                            st.execute("create view view_grid"+labelFormat.format(gridID)+" as select *, geometryfromtext(geom_txt_poly,"+epsgCode+") as the_geom from nmt_model_polygon where grid_id = "+gridID);
                            st.execute("insert into geometry_columns (f_table_catalog,f_table_schema,f_table_name,f_geometry_column,coord_dimension,srid,type) values ('','public','view_grid"+labelFormat.format(gridID)+"','the_geom',2,"+epsgCode+",'POLYGON')");
                            
                            //Get Reaches

                            UnionSet rivNet=basNet.getReachesUnionSet();
                            SampledSet[] reaches=rivNet.getSets();


                            for (int j = 0; j < reaches.length; j+=100) {
                                int k;
                                for (k = j; k < j+100 && k < reaches.length; k++) {

                                    float[][] polsVals=((Gridded2DSet)reaches[k]).getSamples();

                                    String lineString="'LINESTRING(";

                                    for (int i = 0; i < polsVals[0].length-1; i++) {
                                        lineString+=""+(polsVals[0][i]+basTIN_O.minX)+" "+(polsVals[1][i]+basTIN_O.minY)+",";
                                    }
                                    lineString+=""+(polsVals[0][polsVals[0].length-1]+basTIN_O.minX)+" "+(polsVals[1][polsVals[0].length-1]+basTIN_O.minY)+")'";

                                    st.addBatch("INSERT INTO nmt_model_reach ( id,grid_id,geom_txt_line)" +
                                                " VALUES("+(gridID*10000000+(int)val_node_id[k])+","+gridID+","+lineString+")");

                                }

                                int[] s=st.executeBatch();
                                
                                reachProgressBar.setValue((int)(100*k/(float)reaches.length));

                                st.clearBatch();

                            }
                            
                            st.execute("create view view_reach"+labelFormat.format(gridID)+" as select *, geometryfromtext(geom_txt_line,"+epsgCode+") as the_geom from nmt_model_reach where grid_id = "+gridID);
                            st.execute("insert into geometry_columns (f_table_catalog,f_table_schema,f_table_name,f_geometry_column,coord_dimension,srid,type) values ('','public','view_reach"+labelFormat.format(gridID)+"','the_geom',2,"+epsgCode+",'LINESTRING')");


                        } else{
                            rs = st.executeQuery("SELECT id FROM nmt_model_grid WHERE grid_hash = '"+md5_grid_textField.getText()+"'");
                            rs.next();
                            gridID=Integer.valueOf(rs.getString(1));
                            polyProgressBar.setValue(100);
                            reachProgressBar.setValue(100);
                        }
                        rs.close();
                        
                        //Determine if the simulation exists in database
                    
                        st = conn.createStatement();

                        rs = st.executeQuery("SELECT COUNT(sim_hash) FROM nmt_model_simulation WHERE sim_hash = '"+md5_simu_textField.getText()+"'");
                        rs.next();
                        if(rs.getString(1).equalsIgnoreCase("0")){
                            //Upload if necessary

                            rs = st.executeQuery("SELECT NEXTVAL('nmt_model_simulation_id_seq')");
                            rs.next();
                            simuID=Integer.valueOf(rs.getString(1));
                            
                            String time0=yearComboBox.getSelectedItem()+"-"+monthComboBox.getSelectedItem()+"-"+dayComboBox.getSelectedItem()+" "+hourComboBox.getSelectedItem()+":"+minComboBox.getSelectedItem()+":00"+((String)timeZoneComboBox.getSelectedItem()).substring(3);

                            //First register the simulation

                            st.execute("INSERT INTO nmt_model_simulation (id,sim_hash,grid_id,start_time,create_time,init_file) VALUES ("+simuID+",'"+md5_simu_textField.getText()+"',"+gridID+",'"+time0+"','now','"+TheInFileText+"')");

                            //Get info about aggregated response in space and time (mrf+rft)
                            
                            double[] times=fmrfm.getTime();
                            double[] agg_MeanAP=fmrfm.getSeries(2);
                            double[] agg_MaxAP=fmrfm.getSeries(3);
                            double[] agg_MinAP=fmrfm.getSeries(4);
                            double[] agg_FS=fmrfm.getSeries(5);
                            double[] agg_MeanSSM=fmrfm.getSeries(6);
                            double[] agg_MeanSRZM=fmrfm.getSeries(7);
                            double[] agg_MeanSUZM=fmrfm.getSeries(8);
                            double[] agg_MeanDGW=fmrfm.getSeries(9);
                            double[] agg_MeanEVAP=fmrfm.getSeries(10);
                            double[] agg_AFSS=fmrfm.getSeries(11);
                            double[] agg_AFP=fmrfm.getSeries(12);
                            double[] agg_Runoff_IE=frftm.getSeries(1);
                            double[] agg_Runoff_SE=fmrfm.getSeries(2);
                            double[] agg_Runoff_PF=fmrfm.getSeries(3);
                            double[] agg_Runoff_GE=fmrfm.getSeries(4);
                            
                            for (int j = 0; j < times.length; j+=100) {
                                int k;
                                for (k = j; k < j+100 && k < times.length; k++) {
                                    
                                    st.addBatch("INSERT INTO nmt_model_aggregatedspacetime (id,                                       grid_id,   simulation_id,time_offset,agg_meanap,    agg_maxap,   agg_minap,   agg_fs,   agg_meanssm,   agg_meansrzm,   agg_meansuzm,   agg_meandgw,   agg_meanevap,   agg_afss,   agg_afp,   agg_runoff_ie,   agg_runoff_se,   agg_runoff_pf,   agg_runoff_ge)" +
                                               " VALUES (                                  NEXTVAL('nmt_model_aggregatedspacetime_id_seq'),"+gridID+","+simuID+","+   times[k]+","+agg_MeanAP[k]+","+agg_MaxAP[k]+","+agg_MinAP[k]+","+agg_FS[k]+","+agg_MeanSSM[k]+","+agg_MeanSRZM[k]+","+agg_MeanSUZM[k]+","+agg_MeanDGW[k]+","+agg_MeanEVAP[k]+","+agg_AFSS[k]+","+agg_AFP[k]+","+agg_Runoff_IE[k]+","+agg_Runoff_SE[k]+","+agg_Runoff_PF[k]+","+agg_Runoff_GE[k]+")");
                                    
                                }
                                
                                int[] s=st.executeBatch();
                                
                                aggProgressBar.setValue((int)(100*k/(float)times.length));

                                st.clearBatch();
                            }
                            
                            //Get info about aggregated response in time (Integrated Files)
                            
                            float[] val_node_id = fim.getValues("Final State",0);
                            float[] avsm = fim.getValues("Final State",9);
                            float[] avrtm = fim.getValues("Final State",10);
                            float[] hoccr = fim.getValues("Final State",11);
                            float[] hrt = fim.getValues("Final State",12);
                            float[] sboccr = fim.getValues("Final State",13);
                            float[] sbrt = fim.getValues("Final State",14);
                            float[] poccr = fim.getValues("Final State",15);
                            float[] prt = fim.getValues("Final State",16);
                            float[] satoccr = fim.getValues("Final State",17);
                            float[] satrt = fim.getValues("Final State",18);
                            float[] soisatoccr = fim.getValues("Final State",19);
                            float[] rchdsch = fim.getValues("Final State",20);
                            float[] aveet = fim.getValues("Final State",21);
                            float[] evpfrct = fim.getValues("Final State",22);

                            
                            for (int j = 0; j < val_node_id.length; j+=100) {
                                int k;
                                for (k = j; k < j+100 && k < val_node_id.length; k++) {
                                    
                                    st.addBatch("INSERT INTO nmt_model_aggregatedtime (id,                                          simulation_id,polygon_id,                               avsm,       avrtm,       hoccr,       hrt,       sboccr,       sbrt,       poccr,       prt,       satoccr,       satrt,       soisatoccr,       rchdsch,       aveet,       evpfrct)" +
                                               " VALUES (                              NEXTVAL('nmt_model_aggregatedtime_id_seq'),"+simuID+","+(gridID*10000000+(int)val_node_id[k])+","+   avsm[k]+","+avrtm[k]+","+hoccr[k]+","+hrt[k]+","+sboccr[k]+","+sbrt[k]+","+poccr[k]+","+prt[k]+","+satoccr[k]+","+satrt[k]+","+soisatoccr[k]+","+rchdsch[k]+","+aveet[k]+","+evpfrct[k]+")");
                                    
                                }
                                
                                int[] s=st.executeBatch();
                                
                                aggTimeProgressBar.setValue((int)(100*k/(float)val_node_id.length));

                                st.clearBatch();
                            }
                            
                            fim.clearData();
                            
                            //Create a view for the data view_grid_00000_sim00000_aggregatedtime and register geometry
                            
                            java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
                            labelFormat.setGroupingUsed(false);
                            labelFormat.setMinimumIntegerDigits(5);
                            
                            rs = st.executeQuery("SELECT proj_epsg FROM nmt_model_grid  WHERE id = "+gridID);
                            rs.next();
                            String epsgCode=rs.getString(1);

                            st.execute("create view view_grid"+labelFormat.format(gridID)+"_sim"+labelFormat.format(simuID)+"_aggregatedtime as select a.*, b.id as agg_id, b.avsm, b.avrtm, b.hoccr, b.hrt, b.sboccr, b.sbrt, b.poccr, b.prt, b.satoccr,b.satrt,b.soisatoccr, b.rchdsch,b.aveet,b.evpfrct from view_grid"+labelFormat.format(gridID)+" a, nmt_model_aggregatedtime b where a.id = b.polygon_id and b.simulation_id = "+simuID);
                            st.execute("insert into geometry_columns (f_table_catalog,f_table_schema,f_table_name,f_geometry_column,coord_dimension,srid,type) values ('','public','view_grid"+labelFormat.format(gridID)+"_sim"+labelFormat.format(simuID)+"_aggregatedtime','the_geom',2,"+epsgCode+",'POLYGON')");

                            //Get info about hydrographs
                            
                            Object[] qKeys=fqm.getKeys();
                            int[] qIndex=fqm.getLocationIndexes(baseNameTextField.getText());
                            
                            for (int l = 0; l < qKeys.length; l++) {
                                
                                double[] timesQ=fqm.getTime(qKeys[l]);
                                double[] dishaQ=fqm.getSeries(qKeys[l],1);
                                double[] depthQ=fqm.getSeries(qKeys[l],2);
                                
                                

                                for (int j = 0; j < timesQ.length; j+=100) {
                                    int k;
                                    for (k = j; k < j+100 && k < timesQ.length; k++) {
                                        
                                        st.addBatch("INSERT INTO nmt_model_outputhydrograph (id,simulation_id,polygon_id,time_offset,hydrograph_q, hydrograph_depth)" +
                                                   " VALUES (NEXTVAL('nmt_model_outputhydrograph_id_seq'),"+simuID+","+(gridID*10000000+qIndex[l])+","+timesQ[k]+","+(float)dishaQ[k]+","+(float)depthQ[k]+")");

                                    }

                                    //int[] s=st.executeBatch();
                                    
                                    hydProgressBar.setValue((int)(100*(l+k/(float)timesQ.length)/(float)qKeys.length));

                                    st.clearBatch();
                                }
                            }
                            
                            //Get info about nodes
                            
                            Object[] pKeys=fpm.getKeys();
                            int[] pIndex=fpm.getLocationIndexes(baseNameTextField.getText());
                            
                            for (int l = 0; l < pKeys.length; l++) {
                                
                                double[] timesP=fpm.getTime(pKeys[l]);
                                double[] nwt=fpm.getSeries(pKeys[l],2);
                                double[] nf=fpm.getSeries(pKeys[l],3);
                                double[] nt=fpm.getSeries(pKeys[l],4);
                                double[] mu=fpm.getSeries(pKeys[l],5);
                                double[] mi=fpm.getSeries(pKeys[l],6);
                                double[] qpout=fpm.getSeries(pKeys[l],7);
                                double[] qpin=fpm.getSeries(pKeys[l],8);
                                double[] trnsm=fpm.getSeries(pKeys[l],9);
                                double[] gwflx=fpm.getSeries(pKeys[l],10);
                                double[] srf=fpm.getSeries(pKeys[l],11);
                                double[] rainfall=fpm.getSeries(pKeys[l],12);
                                double[] soil_moist=fpm.getSeries(pKeys[l],13);
                                double[] root_moist=fpm.getSeries(pKeys[l],14);
                                double[] air_t=fpm.getSeries(pKeys[l],15);
                                double[] dew_t=fpm.getSeries(pKeys[l],16);
                                double[] surf_t=fpm.getSeries(pKeys[l],17);
                                double[] soil_t=fpm.getSeries(pKeys[l],18);
                                double[] press=fpm.getSeries(pKeys[l],19);
                                double[] rel_hum=fpm.getSeries(pKeys[l],20);
                                double[] sky_cov=fpm.getSeries(pKeys[l],21);
                                double[] wind=fpm.getSeries(pKeys[l],22);
                                double[] net_rad=fpm.getSeries(pKeys[l],23);
                                double[] shrt_rad_in=fpm.getSeries(pKeys[l],24);
                                double[] shrt_rad_in_dir=fpm.getSeries(pKeys[l],25);
                                double[] shrt_rad_in_dif=fpm.getSeries(pKeys[l],26);
                                double[] shrt_absv_veg=fpm.getSeries(pKeys[l],27);
                                double[] shrt_absv_soi=fpm.getSeries(pKeys[l],28);
                                double[] lng_rad_in=fpm.getSeries(pKeys[l],29);
                                double[] lng_rad_out=fpm.getSeries(pKeys[l],30);
                                double[] pot_evp=fpm.getSeries(pKeys[l],31);
                                double[] act_evp=fpm.getSeries(pKeys[l],32);
                                double[] evp_ttrs=fpm.getSeries(pKeys[l],33);
                                double[] evp_wet_can=fpm.getSeries(pKeys[l],34);
                                double[] evp_dry_can=fpm.getSeries(pKeys[l],35);
                                double[] evp_soil=fpm.getSeries(pKeys[l],36);
                                double[] gflux=fpm.getSeries(pKeys[l],37);
                                double[] hflux=fpm.getSeries(pKeys[l],38);
                                double[] lflux=fpm.getSeries(pKeys[l],39);
                                double[] net_precip=fpm.getSeries(pKeys[l],40);
                                double[] can_storg=fpm.getSeries(pKeys[l],41);
                                double[] cum_intercept=fpm.getSeries(pKeys[l],42);
                                double[] intercept=fpm.getSeries(pKeys[l],43);
                                double[] recharge=fpm.getSeries(pKeys[l],44);
                                double[] runon=fpm.getSeries(pKeys[l],45);
                                double[] surf_hour=fpm.getSeries(pKeys[l],46);
                                double[] qstrm=fpm.getSeries(pKeys[l],47);
                                double[] hlev=fpm.getSeries(pKeys[l],48);

                                for (int j = 0; j < timesP.length; j+=100) {
                                    int k;
                                    for (k = j; k < j+100 && k < timesP.length; k++) {

                                        st.addBatch("INSERT INTO nmt_model_outputnode (id,simulation_id,polygon_id,time_offset,nwt,nf,nt,mu,mi,qpout,qpin,trnsm,gwflx,srf,rainfall,soil_moist,root_moist,air_t,dew_t,surf_t,soil_t,press,rel_hum,sky_cov,wind,net_rad,shrt_rad_in,shrt_rad_in_dir,shrt_rad_in_dif,shrt_absv_veg,shrt_absv_soi,lng_rad_in,lng_rad_out,pot_evp,act_evp,evp_ttrs,evp_wet_can,evp_dry_can,evp_soil,gflux,hflux,lflux,net_precip,can_storg,cum_intercept,intercept,recharge,runon,surf_hour,qstrm,hlev)" +
                                                   " VALUES (                                  NEXTVAL('nmt_model_outputnode_id_seq'),"+simuID+","+(gridID*10000000+pIndex[l])+","+timesP[k]+","+nwt[k]+","+nf[k]+","+nt[k]+","+mu[k]+","+mi[k]+","+qpout[k]+","+qpin[k]+","+trnsm[k]+","+gwflx[k]+","+srf[k]+","+rainfall[k]+","+soil_moist[k]+","+root_moist[k]+","+air_t[k]+","+dew_t[k]+","+surf_t[k]+","+soil_t[k]+","+press[k]+","+rel_hum[k]+","+sky_cov[k]+","+wind[k]+","+net_rad[k]+","+shrt_rad_in[k]+","+shrt_rad_in_dir[k]+","+shrt_rad_in_dif[k]+","+shrt_absv_veg[k]+","+shrt_absv_soi[k]+","+lng_rad_in[k]+","+lng_rad_out[k]+","+pot_evp[k]+","+act_evp[k]+","+evp_ttrs[k]+","+evp_wet_can[k]+","+evp_dry_can[k]+","+evp_soil[k]+","+gflux[k]+","+hflux[k]+","+lflux[k]+","+net_precip[k]+","+can_storg[k]+","+cum_intercept[k]+","+intercept[k]+","+recharge[k]+","+runon[k]+","+surf_hour[k]+","+qstrm[k]+","+hlev[k]+")");

                                    }

                                    int[] s=st.executeBatch();

                                    
                                    pixelProgressBar.setValue((int)(100*(l+k/(float)timesP.length)/(float)pKeys.length));

                                    st.clearBatch();
                                }
                                
                                
                            }
                            
                            //Get info about dynamic files
                            
                            Object[] dKeys=fdm.getKeys();

                            for (int l = 0; l < dKeys.length; l++) {
                                
                                String time_offset=dKeys[l].toString();
                                time_offset=time_offset.substring(time_offset.lastIndexOf(".")+1,time_offset.lastIndexOf("d"));
                                float val_time_offset=Float.parseFloat((time_offset.split("_"))[0])+Float.parseFloat((time_offset.split("_"))[1])/60.0f;
                                
                                val_node_id = fdm.getValues(dKeys[l],0);
                                
                                float[] nwt = fdm.getValues(dKeys[l],4);
                                float[] mu = fdm.getValues(dKeys[l],5);
                                float[] mi = fdm.getValues(dKeys[l],6);
                                float[] nf = fdm.getValues(dKeys[l],7);
                                float[] nt = fdm.getValues(dKeys[l],8);
                                float[] qpout = fdm.getValues(dKeys[l],9);
                                float[] qpin = fdm.getValues(dKeys[l],10);
                                float[] srf = fdm.getValues(dKeys[l],11);
                                float[] rain = fdm.getValues(dKeys[l],12);
                                float[] soil_moist = fdm.getValues(dKeys[l],13);
                                float[] root_moist = fdm.getValues(dKeys[l],14);
                                float[] can_storg = fdm.getValues(dKeys[l],15);
                                float[] act_evp = fdm.getValues(dKeys[l],16);
                                float[] evp_soil = fdm.getValues(dKeys[l],17);
                                float[] et = fdm.getValues(dKeys[l],18);
                                float[] gflux = fdm.getValues(dKeys[l],19);
                                float[] hflux =  fdm.getValues(dKeys[l],20);
                                float[] lflux =  fdm.getValues(dKeys[l],21);
                                float[] qstrm =  fdm.getValues(dKeys[l],22);
                                float[] hlev =  fdm.getValues(dKeys[l],23);
                                float[] flw_vlc =  fdm.getValues(dKeys[l],24);
        
        
                                for (int j = 0; j < val_node_id.length; j+=200) {
                                    int k;
                                    for (k = j; k < j+200 && k < val_node_id.length; k++) {

                                        st.addBatch("INSERT INTO nmt_model_outputpoly (id,simulation_id,polygon_id,time_offset,nwt,mu,mi,nf,nt,qpout,qpin,srf,rain,soil_moist,root_moist,can_storg,act_evp,evp_soil,et,gflux,hflux,lflux,qstrm,hlev,flw_vlc)" +
                                                   " VALUES (                                  NEXTVAL('nmt_model_outputpoly_id_seq'),"+simuID+","+(gridID*10000000+(int)val_node_id[k])+","+val_time_offset+","+nwt[k]+","+mu[k]+","+mi[k]+","+nf[k]+","+nt[k]+","+qpout[k]+","+qpin[k]+","+srf[k]+","+rain[k]+","+soil_moist[k]+","+root_moist[k]+","+can_storg[k]+","+act_evp[k]+","+evp_soil[k]+","+et[k]+","+gflux[k]+","+hflux[k]+","+lflux[k]+","+qstrm[k]+","+hlev[k]+","+flw_vlc[k]+")");

                                    }

                                    int[] s=st.executeBatch();

                                    
                                    dynamicOutputProgressBar.setValue((int)(100*(l+k/(float)val_node_id.length)/(float)dKeys.length));

                                    st.clearBatch();
                                }
                                
                                fdm.clearData(dKeys[l]);
                                
                            }
                            
                            //Create a view for the data view_grid_00000_sim00000_timeseries and register geometry
                            
                            st.execute("create view view_grid"+labelFormat.format(gridID)+"_sim"+labelFormat.format(simuID)+"_timeseries as select a.*, b.id as ts_id, b.simulation_id, (c.start_time + (b.time_offset::char(12) || ' hours')::interval) AT TIME ZONE 'UTC' as timestep, b.nwt, b.mu, b.mi, b.nf, b.nt, b.qpout, b.qpin, b.srf, b.rain, b.soil_moist, b.root_moist, b.can_storg, b.act_evp, b.evp_soil, b.et, b.gflux, b.hflux, b.lflux, b.qstrm, b.hlev, b.flw_vlc from view_grid"+labelFormat.format(gridID)+" a, nmt_model_outputpoly b, nmt_model_simulation c where a.id = b.polygon_id and b.simulation_id = "+simuID+" and c.id = b.simulation_id");
                            st.execute("insert into geometry_columns (f_table_catalog,f_table_schema,f_table_name,f_geometry_column,coord_dimension,srid,type) values ('','public','view_grid"+labelFormat.format(gridID)+"_sim"+labelFormat.format(simuID)+"_timeseries','the_geom',2,"+epsgCode+",'POLYGON')");

                            java.util.Date endTime=new java.util.Date();
                            System.out.println("> Database Transaction ends at :"+endTime.toString());
                            System.out.println("> Transaction Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");

                            Object[] options = { "Continue >>>"};
                            javax.swing.JOptionPane.showOptionDialog(mainFrame, "Your simulation has been successfully upaloaded \n GRID CODE "+labelFormat.format(gridID)+"\n SIMULATION CODE: "+labelFormat.format(simuID)+"\nSave this information for future reference", "Attention", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,null, options, options[0]);

                        } else{
                            rs = st.executeQuery("SELECT id FROM nmt_model_grid WHERE grid_hash = '"+md5_grid_textField.getText()+"'");
                            rs.next();
                            simuID=Integer.valueOf(rs.getString(1));
                            aggProgressBar.setValue(100);
                            aggTimeProgressBar.setValue(100);
                            dynamicOutputProgressBar.setValue(100);
                            hydProgressBar.setValue(100);
                            pixelProgressBar.setValue(100);
                                    
                            Object[] options = { "Continue >>>"};
                            javax.swing.JOptionPane.showOptionDialog(mainFrame, "Your simulation had already been uploaded.", "Attention", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,null, options, options[0]);

                       }
                       rs.close();
                        
                       sendButton.setEnabled(true);
                        
                    } catch (SQLException exSQL) {
                        exSQL.printStackTrace();
                    }  catch (VisADException exVis) {
                        exVis.printStackTrace();
                    }                        
                        
                }
            };
        new Thread(pushData).start();
        
    }//GEN-LAST:event_sendButtonActionPerformed

    private void netCheckBox_OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_netCheckBox_OActionPerformed
        if(netCheckBox_O.isSelected()){
            try {
                display_TIN_O.addReference(data_refNet_O,networkCMap_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_O.removeReference(data_refNet_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_netCheckBox_OActionPerformed

    private void colorTableButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorTableButtonActionPerformed
        new hydroScalingAPI.modules.tRIBS_io.widgets.ColorTableTribs(this,voiColorMap_O).setVisible(true);
    }//GEN-LAST:event_colorTableButtonActionPerformed

    private void pNodesComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pNodesComboActionPerformed
        plotPixel();
    }//GEN-LAST:event_pNodesComboActionPerformed

    private void pNodesVarsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pNodesVarsComboActionPerformed
        plotPixel();
    }//GEN-LAST:event_pNodesVarsComboActionPerformed

    private void avaVariablesComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avaVariablesComboActionPerformed
        if(avaVariablesCombo.getSelectedIndex() == 0){
            try {
                voiColorMap_O.setRange(0,0);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
        if(avaTimesCombo.getSelectedIndex() < 3){
            paintParametersVoronoiPolygons(1000+avaVariablesCombo.getSelectedIndex()+9,avaTimesCombo.getSelectedItem());
            return;
        }
        paintParametersVoronoiPolygons(2000+avaVariablesCombo.getSelectedIndex()+3,avaTimesCombo.getSelectedItem());
    }//GEN-LAST:event_avaVariablesComboActionPerformed

    private void avaTimesComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avaTimesComboActionPerformed
        if(avaTimesCombo.getSelectedIndex() == 0){
            try {
                voiColorMap_O.setRange(0,0);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
            Object[] spatialVariables=new String[] {"Available Variables"};
            avaVariablesCombo.setModel(new javax.swing.DefaultComboBoxModel(spatialVariables));
            return;
        }
        if(avaTimesCombo.getSelectedIndex() < 3){
            if(((String)avaVariablesCombo.getItemAt(0)).equalsIgnoreCase("Available Integrated Variables")){
                paintParametersVoronoiPolygons(1000+avaVariablesCombo.getSelectedIndex()+9,avaTimesCombo.getSelectedItem());
            } else {
                try {
                    voiColorMap_O.setRange(0,0);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                } catch (VisADException ex) {
                    ex.printStackTrace();
                }
                Object[] spatialVariables=new String[] {"Available Integrated Variables"};
                spatialVariables=hydroScalingAPI.tools.ArrayTools.concatentate(spatialVariables,ListOfVariables.integratedOutput);
                avaVariablesCombo.setModel(new javax.swing.DefaultComboBoxModel(spatialVariables));
            }
            return;
        }

        if(((String)avaVariablesCombo.getItemAt(0)).equalsIgnoreCase("Available Dynamic Variables")){
            paintParametersVoronoiPolygons(2000+avaVariablesCombo.getSelectedIndex()+3,avaTimesCombo.getSelectedItem());
        } else {
            try {
                voiColorMap_O.setRange(0,0);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
            Object[] spatialVariables=new String[] {"Available Dynamic Variables"};
            spatialVariables=hydroScalingAPI.tools.ArrayTools.concatentate(spatialVariables,ListOfVariables.dynamicOutput);
            avaVariablesCombo.setModel(new javax.swing.DefaultComboBoxModel(spatialVariables));
        }
        
    }//GEN-LAST:event_avaTimesComboActionPerformed

    private void spaceParamsComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_spaceParamsComboActionPerformed
        if(spaceParamsCombo.getSelectedIndex() == 0) return;
        paintParametersVoronoiPolygons(ListOfVariables.spatialVarSource[spaceParamsCombo.getSelectedIndex()]);
    }//GEN-LAST:event_spaceParamsComboActionPerformed

    private void qNodesComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_qNodesComboActionPerformed
        plotQouts();
    }//GEN-LAST:event_qNodesComboActionPerformed

    private void dischBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dischBoxActionPerformed
        plotQouts();
    }//GEN-LAST:event_dischBoxActionPerformed

    private void stageBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stageBoxActionPerformed
        plotQouts();
    }//GEN-LAST:event_stageBoxActionPerformed

    private void groFlBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_groFlBoxActionPerformed
        plotRft();
    }//GEN-LAST:event_groFlBoxActionPerformed

    private void perFlBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_perFlBoxActionPerformed
        plotRft();
    }//GEN-LAST:event_perFlBoxActionPerformed

    private void satExBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_satExBoxActionPerformed
        plotRft();
    }//GEN-LAST:event_satExBoxActionPerformed

    private void infExBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infExBoxActionPerformed
        plotRft();
    }//GEN-LAST:event_infExBoxActionPerformed

    private void jRadioButton11ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton11ActionPerformed
        plotMrf(12);
    }//GEN-LAST:event_jRadioButton11ActionPerformed

    private void jRadioButton10ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton10ActionPerformed
        plotMrf(11);
    }//GEN-LAST:event_jRadioButton10ActionPerformed

    private void jRadioButton9ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton9ActionPerformed
        plotMrf(10);
    }//GEN-LAST:event_jRadioButton9ActionPerformed

    private void jRadioButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton8ActionPerformed
        plotMrf(9);
    }//GEN-LAST:event_jRadioButton8ActionPerformed

    private void jRadioButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton7ActionPerformed
        plotMrf(8);
    }//GEN-LAST:event_jRadioButton7ActionPerformed

    private void jRadioButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton6ActionPerformed
        plotMrf(7);
    }//GEN-LAST:event_jRadioButton6ActionPerformed

    private void jRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton5ActionPerformed
        plotMrf(6);
    }//GEN-LAST:event_jRadioButton5ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        plotMrf(5);
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        plotMrf(4);
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        plotMrf(3);
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        plotMrf(2);
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void panelOutputsStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_panelOutputsStateChanged
      if ( panelOutputs.getSelectedIndex()==2 && lastSelectedTab != 2) {
          jPanel26.add("Center",display_TIN_O.getComponent());
          jPanel26.add("South",jPanel22);
          jPanel27.add(colorTableButton);
          if(spaceParamsCombo.getSelectedIndex() == 0) {
              try {
                    voiColorMap_O.setRange(0,0);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                } catch (VisADException ex) {
                    ex.printStackTrace();
                }
          } else{
                paintParametersVoronoiPolygons(ListOfVariables.spatialVarSource[spaceParamsCombo.getSelectedIndex()]);
          }
          lastSelectedTab=2;
      }

      if ( panelOutputs.getSelectedIndex()==3 && lastSelectedTab != 3) {
          jPanel17.add("Center",display_TIN_O.getComponent());
          jPanel17.add("South",jPanel22);
          jPanel20.add(colorTableButton);
          if(avaTimesCombo.getSelectedIndex() == 0) {
              try {
                  voiColorMap_O.setRange(0,0);
              } catch (RemoteException ex) {
                  ex.printStackTrace();
              } catch (VisADException ex) {
                  ex.printStackTrace();
              }
          } else{
              if(((String)avaVariablesCombo.getItemAt(0)).equalsIgnoreCase("Available Integrated Variables")){
                  paintParametersVoronoiPolygons(1000+avaVariablesCombo.getSelectedIndex()+9,avaTimesCombo.getSelectedItem());
              } else {
                  paintParametersVoronoiPolygons(2000+avaVariablesCombo.getSelectedIndex()+3,avaTimesCombo.getSelectedItem());
              }
          }
          lastSelectedTab=3;
      }
      
    }//GEN-LAST:event_panelOutputsStateChanged

    private void valuesCheckBox_OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_valuesCheckBox_OActionPerformed
        if(valuesCheckBox_O.isSelected()){
            try {
                display_TIN_O.addReference(data_refFill_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_O.removeReference(data_refFill_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_valuesCheckBox_OActionPerformed

    private void voronoiCheckBox_OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_voronoiCheckBox_OActionPerformed
        if(voronoiCheckBox_O.isSelected()){
            try {
                display_TIN_O.addReference(data_refPoly_O,polygonsCMap_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_O.removeReference(data_refPoly_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_voronoiCheckBox_OActionPerformed

    private void trianglesCheckBox_OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trianglesCheckBox_OActionPerformed
        if(trianglesCheckBox_O.isSelected()){
            try {
                display_TIN_O.addReference(data_refTr_O,trianglesCMap_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_O.removeReference(data_refTr_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_trianglesCheckBox_OActionPerformed

    private void pointsCheckBox_OActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointsCheckBox_OActionPerformed
        if(pointsCheckBox_O.isSelected()){
            try {
                display_TIN_O.addReference(data_refPoints_O,pointsCMap_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_O.removeReference(data_refPoints_O);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_pointsCheckBox_OActionPerformed

    private void changePathActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changePathActionPerformed
        try {
            plotOutputPoints();
        } catch (RemoteException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }//GEN-LAST:event_changePathActionPerformed

    private void exportPoiButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportPoiButtonActionPerformed
        try{
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser(pathTextField.getText()+"/Input/");
            fc.setFileSelectionMode(fc.FILES_ONLY);
            fc.setDialogTitle("Directory Selection");
            javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("points","Points File");
            fc.addChoosableFileFilter(mdtFilter);
            fc.showSaveDialog(this);
            
            if (fc.getSelectedFile() == null) return;
            
            basTIN_I.writePoints(fc.getSelectedFile());
        } catch (java.io.IOException IOE){
            System.err.println("Failed writing triangulation files for this basin.");
            IOE.printStackTrace();
        }
    }//GEN-LAST:event_exportPoiButtonActionPerformed

    private void trianglesCheckBox_IActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_trianglesCheckBox_IActionPerformed
        if(trianglesCheckBox_I.isSelected()){
            try {
                display_TIN_I.addReference(data_refTr_I,trianglesCMap_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_I.removeReference(data_refTr_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_trianglesCheckBox_IActionPerformed

    private void pointsCheckBox_IActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pointsCheckBox_IActionPerformed
        if(pointsCheckBox_I.isSelected()){
            try {
                display_TIN_I.addReference(data_refPoints_I,pointsCMap_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        } else{
            try {
                display_TIN_I.removeReference(data_refPoints_I);
            } catch (RemoteException ex) {
                ex.printStackTrace();
            } catch (VisADException ex) {
                ex.printStackTrace();
            }
        }
    }//GEN-LAST:event_pointsCheckBox_IActionPerformed

    private void exportTriButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exportTriButtonActionPerformed
        try{
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser();
            fc.setFileSelectionMode(fc.FILES_ONLY);
            fc.setDialogTitle("Directory Selection");
            javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("nodes","Nodes File");
            fc.addChoosableFileFilter(mdtFilter);
            fc.showSaveDialog(this);

            if (fc.getSelectedFile() == null) return;
            basTIN_I.writeTriangulation(fc.getSelectedFile());
        } catch (java.io.IOException IOE){
            System.err.println("Failed writing triangulation files for this basin.");
            IOE.printStackTrace();
        }
    }//GEN-LAST:event_exportTriButtonActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
        if(standAlone > 0) System.exit(0);
    }//GEN-LAST:event_closeDialog
    
    /**
     * Test for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {

          //Uncomment when testing the input interface
//        main0(args);
//        if(true) return;
        
//        args=new String[3];
//        args[0]="-ou";
//        args[1]="/Users/ricardo/workFiles/tribsWork/sampleTribs/SEVILLETA/simRound2/Output0/";
//        args[2]="smallbasin";

//        args=new String[2];
//        args[0]="/Users/ricardo/workFiles/tribsWork/sampleTribs/SEVILLETA/Output4/";
//        args[1]="smallbasin";
        
        System.out.println(java.util.Arrays.toString(args));
        
        
        try{

            hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();

            ///home/ricardo/workFiles/tribsWork/sampleTribs/SMALLBASIN/Output/"),"smallbasin"
            ///home/ricardo/simulationResults/SMALLBASIN/Output_Base/"),"smallbasin"
            ///home/ricardo/simulationResults/Output_Mar23a_07/"),"urp"
            ///home/ricardo/SourceCodes/benchmarkExamples/Scripts/Comparison/tRIBS_Output/Output_conv_loam_G5/Output/ conv_tt_loam
            ///home/ricardo/SourceCodes/benchmarkExamples/Scripts/Comparison/tRIBS_Output/Output_elem_loam_G5/Output/ elem_tt_loam
            ///home/ricardo/SourceCodes/benchmarkExamples/Scripts/Comparison/tRIBS_Output/Output_peach_Fall1996_G5/Output/ peach_f96_tt_dist
            ///home/ricardo/SourceCodes/benchmarkExamples/Scripts/Comparison/tRIBS_Output/Output_peach_Fall1998_G5/Output/ peach_f98_tt_dist
            ///home/ricardo/SourceCodes/benchmarkExamples/Scripts/Comparison/tRIBS_Output/Output_Summer1991_G5/Output/ peach_s91_tt_loam
            
            
            java.io.File theDirectory=new java.io.File("/Users/ricardo/workFiles/tribsWork/sampleTribs/SEVILLETA/Output0/");
            String baseName="smallbasin";
            
            if(args.length > 0){
                if(args[0].equalsIgnoreCase("-ou")){
                    theDirectory=new java.io.File(args[1]);
                    baseName=args[2];
                } else {
                    theDirectory=new java.io.File(args[0]);
                    baseName=args[1];
                }
            }
            
            new TRIBS_io(tempFrame, theDirectory,baseName,args.length).setVisible(true);

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
    
    }

    
    public static void main0(String args[]) {
        
        
        try{
            
            java.io.File writePath=new java.io.File("/home/ricardo/temp/TESTBASIN/");
            String baseName = "testbasin";
            
            //java.io.File theFile=new java.io.File("/hidrosigDataBases/Smallbasin_DB/Rasters/Topography/1_Arcsec/NED_06075640.metaDEM");
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Gila_River_DB/Rasters/Topography/1_ArcSec/mogollon.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster (theFile);
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
            
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
            metaModif.setFormat("Byte");
            byte [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();

            //Locations for Smallbasin case
            //new TRIBS_io(tempFrame, 56,79,matDirs,magnitudes,metaModif).setVisible(true);
            
            //Locations for Mogollon Case
            //new TRIBS_io(tempFrame, 282,298 ,matDirs,magnitudes,metaModif,writePath,baseName).setVisible(true);
            new TRIBS_io(tempFrame, 689,483 ,matDirs,magnitudes,metaModif,writePath,baseName).setVisible(true);
            
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
    
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar aggProgressBar;
    private javax.swing.JProgressBar aggTimeProgressBar;
    private javax.swing.JComboBox avaTimesCombo;
    private javax.swing.JComboBox avaVariablesCombo;
    private javax.swing.JTextField baseNameTextField;
    private javax.swing.JComboBox bufferTypeCombo;
    private javax.swing.JButton changePath;
    private javax.swing.JButton colorTableButton;
    private javax.swing.JButton computeLattice;
    private javax.swing.JComboBox dayComboBox;
    private javax.swing.JCheckBox dischBox;
    private javax.swing.JProgressBar dynamicOutputProgressBar;
    private javax.swing.JButton exportGW;
    private javax.swing.JButton exportLandCover;
    private javax.swing.JButton exportPoiButton;
    private javax.swing.JButton exportSoils;
    private javax.swing.JButton exportTriButton;
    private javax.swing.JButton findFileButton;
    private javax.swing.JCheckBox groFlBox;
    private javax.swing.JComboBox hourComboBox;
    private javax.swing.JProgressBar hydProgressBar;
    private javax.swing.JTextArea inFileTextArea;
    private javax.swing.JTextField inFileTextField;
    private javax.swing.JCheckBox infExBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel16;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel25;
    private javax.swing.JLabel jLabel26;
    private javax.swing.JLabel jLabel27;
    private javax.swing.JLabel jLabel28;
    private javax.swing.JLabel jLabel29;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel30;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JLabel jLabel323;
    private javax.swing.JLabel jLabel33;
    private javax.swing.JLabel jLabel34;
    private javax.swing.JLabel jLabel35;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel20;
    private javax.swing.JPanel jPanel21;
    private javax.swing.JPanel jPanel22;
    private javax.swing.JPanel jPanel23;
    private javax.swing.JPanel jPanel24;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel27;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel29;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel30;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel32;
    private javax.swing.JPanel jPanel33;
    private javax.swing.JPanel jPanel34;
    private javax.swing.JPanel jPanel35;
    private javax.swing.JPanel jPanel36;
    private javax.swing.JPanel jPanel37;
    private javax.swing.JPanel jPanel38;
    private javax.swing.JPanel jPanel39;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel40;
    private javax.swing.JPanel jPanel41;
    private javax.swing.JPanel jPanel42;
    private javax.swing.JPanel jPanel43;
    private javax.swing.JPanel jPanel44;
    private javax.swing.JPanel jPanel45;
    private javax.swing.JPanel jPanel46;
    private javax.swing.JPanel jPanel47;
    private javax.swing.JPanel jPanel48;
    private javax.swing.JPanel jPanel49;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel50;
    private javax.swing.JPanel jPanel51;
    private javax.swing.JPanel jPanel52;
    private javax.swing.JPanel jPanel53;
    private javax.swing.JPanel jPanel54;
    private javax.swing.JPanel jPanel55;
    private javax.swing.JPanel jPanel56;
    private javax.swing.JPanel jPanel57;
    private javax.swing.JPanel jPanel58;
    private javax.swing.JPanel jPanel59;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel_I;
    private javax.swing.JPanel jPanel_O;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton10;
    private javax.swing.JRadioButton jRadioButton11;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JRadioButton jRadioButton9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.JTextField md5_grid_textField;
    private javax.swing.JTextField md5_simu_textField;
    private javax.swing.JComboBox minComboBox;
    private javax.swing.JComboBox monthComboBox;
    private javax.swing.ButtonGroup mrfButtonGroup;
    private javax.swing.JPanel mrfPanel;
    private javax.swing.JCheckBox netCheckBox_O;
    private javax.swing.JComboBox networkLevelCombo;
    private javax.swing.JLabel numPointsLabel;
    private javax.swing.JComboBox pNodesCombo;
    private javax.swing.JComboBox pNodesVarsCombo;
    private javax.swing.JTabbedPane panelInputs;
    private javax.swing.JTabbedPane panelOutputs;
    private javax.swing.JTabbedPane panel_IO;
    private javax.swing.JComboBox pathDensityLevelCombo;
    private javax.swing.JTextField pathTextField;
    private javax.swing.JCheckBox perFlBox;
    private javax.swing.JPanel pixelPanel;
    private javax.swing.JProgressBar pixelProgressBar;
    private javax.swing.JCheckBox pointsCheckBox_I;
    private javax.swing.JCheckBox pointsCheckBox_O;
    private javax.swing.JProgressBar polyProgressBar;
    private javax.swing.JComboBox projsComboBox;
    private javax.swing.JComboBox qNodesCombo;
    private javax.swing.JPanel qoutPanel;
    private javax.swing.JProgressBar reachProgressBar;
    private javax.swing.JButton resetLattice;
    private javax.swing.JPanel rftPanel;
    private javax.swing.JComboBox ridgeLevelCombo;
    private javax.swing.JCheckBox satExBox;
    private javax.swing.JButton selectGW;
    private javax.swing.JButton selectLandCover;
    private javax.swing.JButton selectSoils;
    private javax.swing.JButton sendButton;
    private javax.swing.JComboBox spaceParamsCombo;
    private javax.swing.JCheckBox stageBox;
    private javax.swing.JComboBox timeZoneComboBox;
    private javax.swing.JCheckBox trianglesCheckBox_I;
    private javax.swing.JCheckBox trianglesCheckBox_O;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JCheckBox valuesCheckBox_O;
    private javax.swing.JCheckBox voronoiCheckBox_O;
    private javax.swing.JButton writeInFile;
    private javax.swing.JComboBox yearComboBox;
    private javax.swing.JLabel zrLabel;
    private javax.swing.JSlider zrSlider;
    // End of variables declaration//GEN-END:variables
    
}

class ListOfVariables{
    public static int[] spatialVarSource={  -1,
                                            2000,
                                            2001,
                                            2002,
                                            1003,
                                            1004,
                                            1005,
                                            1006,
                                            1007,
                                            1008,
                                            1009,
                                            2025,
                                            2026,
                                            3000,
                                            3001,
                                            3002,
                                            3003};

    public static String[] spatialParams={  "Available Variables",
                                            "Node Identification, ID [id]",
                                            "Elevation, Z [m]",
                                            "Slope, S [|radian|]",
                                            "Voronoi Area, VAr [m2]",
                                            "Contributing Area, CAr [km2]",
                                            "Curvature, Curv [ ]",
                                            "Flow Edge Length, EdgL [m]",
                                            "Tangent of Flow Edge Slope, tan(Slp) [ ]",
                                            "Width of Voronoi Flow Window, FWidth [m]",
                                            "Site Aspect as Angle from North, Aspect [radian]",
                                            "Soil Type []",
                                            "Land Use Type []",
                                            "Channel Width [m]",
                                            "Edge Length [m]",
                                            "Channel Slope []",
                                            "Channel Upstream Area [m^2]"};
    
    public static String[] integratedOutput={   "Average Soil Moisture, top 10 cm, AvSM [m3/m3]",
                                                "Average Root Zone Moisture, top 1 m, AvRtM [m3/m3]",
                                                "Infiltration-excess Runoff Occurences, HOccr [# of TIMESTEP]",
                                                "Infiltration-excess Runoff Average Rate, HRt [mm/hr]",
                                                "Saturation-excess Runoff Occurences, SbOccr [# of TIMESTEP]",
                                                "Saturation-excess Runoff Average Rate, SbRt 	[mm/hr]",
                                                "Perched Return Runoff Occurences, POccr [# of TIMESTEP]",
                                                "Perched Return Runoff Average Rate, PRt [mm/hr]",
                                                "Groundwater Exfiltration Runoff Occurences, SatOccr [# of GWSTEP]",
                                                "Groundwater Exfiltration Runoff Average Rate, SatRt [mm/hr]",
                                                "Soil Saturation Occurences, SoiSatOccr [# of TIMESTEP]",
                                                "Recharge-Discharge Variable, RchDsch [m]",
                                                "Average Evapotranspiration, AveET [mm/hr]",
                                                "Evaporative Fraction, EvpFrct [ ]"};
    
    public static String[] dynamicOutput={  "Depth to groundwater table, Nwt [mm]",
                                            "Total moisture above the water table, Mu [mm]",
                                            "Moisture content in the initialization profile, Mi [mm]",
                                            "Wetting front depth, Nf [mm]",
                                            "Top front depth, Nt [mm]",
                                            "Unsaturated lateral flow out from cell, Qpout [mm/hr]",
                                            "Unsaturated lateral flow into cell, Qpin [mm/hr]",
                                            "Surface Runoff, Srf [mm]",
                                            "Rainfall, Rain [mm/hr]",
                                            "Soil Moisture, top 10 cm, SoilMoist [ m3/m3]",
                                            "Root  Zone Moisture, top 1 m, RootMoist [ m3/m3]",
                                            "Canopy Storage, CanStorg [mm]",
                                            "Actual Evaporation, ActEvp [mm/hr]",
                                            "Evaporation from Bare Soil, EvpSoil [mm/hr]",
                                            "Total Evapotranspiration, ET [mm/hr]",
                                            "Ground Heat Flux, Gflux [W/m2]",
                                            "Sensible Heat Flux, Hflux [W/m2]",
                                            "Latent Heat Flux, Lflux [W/m2]",
                                            "Discharge, Qstrm 	[m3/s]",
                                            "Channel Stage, Hlev [m]",
                                            "Flow Velocity, FlwVlc [m/s]"};
    
    public static String[] localOutput={  "Depth to groundwater table, Nwt [mm]",
                                          "Wetting front depth, Nf [mm]",
                                          "Top front depth, Nt [mm]",
                                          "Total moisture above the water table, Mu [mm]",
                                          "Moisture content in the initialization profile, Mi [mm]",
                                          "Unsaturated lateral flow out from cell, Qpout [mm/hr]",
                                          "Unsaturated lateral flow into cell, Qpin [mm/hr]",
                                          "Transmissivity, Trnsm [m2/hr]",
                                          "Groundwater flux, GWflx [m3/hr]",
                                          "Surface Runoff, Srf [mm]",
                                          "Rainfall, Rain [mm/hr]",
                                          "Soil Moisture, top 10 cm, SoilMoist [m3/m3]",
                                          "Root  Zone Moisture, top 1 m, RootMoist [m3/m3]",
                                          "Air Temperature, AirT [C]",
                                          "Dew Point Temperature, DewT [C]",
                                          "Surface Temperature, SurfT [C]",
                                          "Soil Temperature, SoilT [C]",
                                          "Atmospheric Pressure, Press [Pa]",
                                          "Relative Humidity, RelHum [ ]",
                                          "Sky Cover, SkyCov [ ]",
                                          "Wind Speed, Wind [m/s]",
                                          "Net Radiation, NetRad [W/m2]",
                                          "Incoming Shortwave Radiation, ShrtRadIn [W/m2]",
                                          "Incoming Direct Shortwave Radiation, ShrtRadIn_dir [W/m2]",
                                          "Incoming Diffuse Shortwave Radiation, ShrtRadIn_dif 	[W/m2]",
                                          "Shortwave Absorbed Radition, Vegetation, ShortAbsbVeg [W/m2]",
                                          "Shortwave Absorbed Radition, Soil, ShortAbsbSoi [W/m2]",
                                          "Incoming Longwave Radiation, LngRadIn [W/m2]",
                                          "Outgoing Longwave Radiation, LngRadOut [W/m2]",
                                          "Potential Evaporation, PotEvp [mm/hr]",
                                          "Actual Evaporation, ActEvp [mm/hr]",
                                          "Total Evapotranspiration, EvpTtrs [mm/hr]",
                                          "Evaporation from Wet Canopy, EvpWetCan [mm/hr]",
                                          "Evaporation from Dry Canopy (Transpiration), EvpDryCan [mm/hr]",
                                          "Evaporation from Bare Soil, EvpSoil [mm/hr]",
                                          "Ground Heat Flux, Gflux [W/m2]",
                                          "Sensible Heat Flux, Hflux [W/m2]",
                                          "Latent Heat Flux, Lflux [W/m2]",
                                          "Net Precipitation, NetPrecip [mm/hr]",
                                          "Canopy Storage, CanStorg [mm]",
                                          "Cumulative Interception, CumIntercept [mm]",
                                          "Interception, Intercept [mm]",
                                          "Recharge [mm/hr]",
                                          "Runon [mm]",
                                          "Surface Runoff in Hour, Surf_Hour [mm]",
                                          "Discharge, Qstrm [m3/s]",
                                          "Channel Stage, Hlev"};
}
 
