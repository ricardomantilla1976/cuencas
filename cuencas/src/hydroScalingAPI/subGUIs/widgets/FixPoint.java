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
 * RasterViewer.java
 *
 * Created on June 20, 2003, 12:29 PM
 */
package hydroScalingAPI.subGUIs.widgets;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import hydroScalingAPI.io.BasinsLogReader;
import hydroScalingAPI.io.MetaRaster;
import hydroScalingAPI.subGUIs.widgets.kml.ReadKML;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.beans.PropertyVetoException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import org.omg.PortableInterceptor.SYSTEM_EXCEPTION;
import visad.DisplayListener;
import visad.ThingReference;
import visad.VisADException;
import visad.java3d.DirectManipulationRendererJ3D;

/**
 * The internal GIS frame for displaying maps and overlaying vector and sites
 *
 * @author Ricardo Mantilla
 */
public abstract class FixPoint extends javax.swing.JInternalFrame implements visad.DisplayListener {

    private String myStringID;
    private hydroScalingAPI.io.BasinsLogReader localBasinsLog;

    private java.util.Hashtable relatedMapsList;

    private java.util.Vector temporalReferences = new java.util.Vector();
    private java.util.Vector basinOutlets = new java.util.Vector();

    private float[] red;
    private float[] green;
    private float[] blue;

    private hydroScalingAPI.io.MetaNetwork localNetwork;
    private java.util.Hashtable networkReferences = new java.util.Hashtable();

    private int[][] subSetCorners = new int[2][2];
    private int subSetCornersCounter = 0;
    //Aniket
    private javax.swing.JMenuItem deleteLog;
    private javax.swing.JMenuItem clearLogs;
    private ArrayList<String> basinsToDelete = new ArrayList<String>();
    private java.util.Hashtable divideBasinTable = new java.util.Hashtable();
    private java.util.Hashtable outletBasicTable = new java.util.Hashtable();
    private java.util.ArrayList traceRiverDisplayRef = new java.util.ArrayList();
    private String currentBasinLabel;
    private JButton saveButton;
    private JDialog frame;
    private JTextArea text;
    private boolean rightMouseClicked = false ; 
    /**
     * The main GIS GUI
     */
    protected hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    /**
     * The MetaRaster that describes to map to be displayed
     */
    protected hydroScalingAPI.io.MetaRaster metaData;

    /**
     * The font for the axis and the labels
     */
    protected final visad.util.HersheyFont font = new visad.util.HersheyFont("futural");

    /**
     * The visad.Display
     */
    protected visad.DisplayImpl display;

    /**
     * The visad.java3d.DisplayRendererJ3D asociated to the visad.Display
     */
    protected visad.java3d.DisplayRendererJ3D dr;
    /**
     * The Latitude Map for the x-axis
     */
    protected visad.ScalarMap latitudeMap;
    /**
     * The Longitude Map for the y-axis
     */
    protected visad.ScalarMap longitudeMap;
    /**
     * The Height Map for the z-axis
     */
    protected visad.ScalarMap heightMap;
    /**
     * The Colors Map overlaying the surface
     */
    protected visad.ScalarMap colorScaleMap;

    protected visad.ScalarMap tmap;

    /**
     * The visad.FlatField with data
     */
    protected visad.FlatField localField;

    /**
     * The active event for the mouse middle button
     */
    protected int activeEvent = 0;

    protected ArrayList<Object> fullKmlPointList = null;

    protected int indexInKmlList = 0;
    protected ArrayList<Object> kmlDisplayReferences;
    protected ArrayList<Object> displayComponent;
    //protected ArrayList<KmlPointDataPojo>  visitedKmlPoints ; 
    KmlPointListPojo kmlList;
    protected String kmlFileValue = "";
    /**
     * The direction matrix associated to the DEM (only if a processed DEM is
     * being displayed)
     */
    protected byte[][] fullDirMatrix;
    private byte[][] netMask;
    private hydroScalingAPI.util.geomorphology.objects.Basin myCuenca;
    private visad.RealTupleType domain;
    private double resultX;
    private double resultY;
    private int MatX;
    private int MatY;
    private int linkedMasked[][];
      visad.DataReferenceImpl greenPointReference ;

    /**
     * Creates new instance of RasterViewer
     *
     * @param relMaps A {@link java.util.Hashtable} with paths to the derived
     * quantities and with keys that describe the variable
     * @param parent The main GIS interface
     * @param md The MetaRaster asociated with the DEM
     */
    public FixPoint(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md, java.util.Hashtable relMaps) throws VisADException {
        mainFrame = parent;
        metaData = md;
        relatedMapsList = relMaps;

        red = mainFrame.getInfoManager().getNetworkRed();
        green = mainFrame.getInfoManager().getNetworkGreen();
        blue = mainFrame.getInfoManager().getNetworkBlue();

        try {
            domain = new visad.RealTupleType(visad.RealType.Longitude, visad.RealType.Latitude);
            localNetwork = new hydroScalingAPI.io.MetaNetwork(metaData);
            linkedMasked = localNetwork.getLinkedMasked();
            displayComponent = new ArrayList<Object>();
            //visitedKmlPoints = new ArrayList<KmlPointDataPojo>();
            // byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaData).getByte();
            //myCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(29, 360, matDirs, metaData);
            // 1) Read all Points from KML 
            readPointFromKml();
            // 2) Get Current Index of latest point from All points
            initializeKmlIndex();
            if (indexInKmlList < fullKmlPointList.size()) {

                resultX = (double) ((Coordinate) fullKmlPointList.get(indexInKmlList)).getLongitude();
                resultY = (double) ((Coordinate) fullKmlPointList.get(indexInKmlList)).getLatitude();

               //resultY =  39.74813191132535;
                //resultX =  -105.5373613981691;
                System.out.println("Value of Matx and Mat y " + resultX + " " + resultY);
                MatX = (int) ((resultX - metaData.getMinLon()) / (float) metaData.getResLon() * 3600.0f);
                MatY = (int) ((resultY - metaData.getMinLat()) / (float) metaData.getResLat() * 3600.0f);
                System.out.println("Value of Matx and Mat y " + MatX + " " + MatY);
                // 4) Create Basin net mask for Cut Subsection to get Rivers
                netMask = getBasinMask(MatX, MatY);

            // matx is long (20) mat Y
                // Create Sub Section
                MetaRaster sectionDEM = createSubSection(MatX, MatY);
                // displaySubSection(metaData);
                displaySubSection(sectionDEM);
                // Display Kml Points
                displayKmlpoints(indexInKmlList);
                // Display River Network 
                showAllOrdersActionPerformed();

                // Display Suggested Value 
               // showSuggestedValue(MatX, MatY);
                   int a[] = getSuggestedValue(MatX, MatY);
                        displaySuggestedValue(a[0], a[1]);

                if (MatX > metaData.getNumCols() || MatX < 0 || MatY > metaData.getNumRows() || MatY < 0) {
                    JOptionPane.showMessageDialog(this, "This point is outside DEM ");
                }

            } else {
                JOptionPane.showMessageDialog(this, "All Points Finished  ");

            }

        } catch (IOException ex) {
            Logger.getLogger(FixPoint.class.getName()).log(Level.SEVERE, null, ex);
        }

        initComponents();
        setSize(650, 600);
        addInternalFrameListener(mainFrame);

    }

    public int[] getSuggestedValue(int x, int y) throws VisADException, RemoteException {

        int minValue[] = new int[2];
        int rangeX = metaData.getNumCols();
        int rangeY = metaData.getNumRows();
        System.out.println("Value of Intial Matx and Y " + x + "  Y :" + y);
        /* for(int i = 1 , j= 1 ; i < rangeX  && j < rangeY; i ++ ,j++ )
         {
         if(x+i <metaData.getNumCols() && linkedMasked[y][x+i] >= 0 )
         {
         minValue[0] =  x+i;
         minValue[1] = y ;
         break;
         }
         else if(x-i>=0&&linkedMasked[y][x-i] >= 0 )
         {
         minValue[0] =  x-i;
         minValue[1] = y ;
         break;
         }
         else if(y+j < metaData.getNumRows() && linkedMasked[y+j][x] >= 0 )
         {
         minValue[0] =  x;
         minValue[1] = y+j ;
         break;
         }
         else if(y-j>=0 && linkedMasked[y-j][x] >= 0 )
         {
         minValue[0] =  x;
         minValue[1] = y-j ;
         break;
         } 
         else if(y-j>=0 &&  x-i>=0 &&linkedMasked[y-j][x-i] >= 0 )
         {
         minValue[0] =  x-i;
         minValue[1] = y-j ;
         break;
         } 
         else if(y+j < metaData.getNumRows() && x+i < metaData.getNumCols()&& linkedMasked[y+j][x+i] >= 0 )
         {
         minValue[0] =  x+i;
         minValue[1] = y+j ;
         break;
         } 
         else if(y+j < metaData.getNumRows() &&  x-i >= 0 && linkedMasked[y+j][x-i] >= 0 )
         {
         minValue[0] =  x-i;
         minValue[1] = y+j ;
         break;
         } 
         else if(y-j>=0 && x+i < metaData.getNumCols() && linkedMasked[y-j][x+i] >= 0 )
         {
         minValue[0] =  x+i;
         minValue[1] = y-j ;
         break;
         } 
         
           
         } */
        /*   int magnitude = 1;
         int i = -1 ;
        
         while(i<2)
         {
         int j = -1 ;
         while(j<2)
         {
         if(linkedMasked[y+j][x+i] >= 0)
         {
         minValue[0] =  x+i;
         minValue[1] = y+j ;
         }
         j++;
         }
         i++;
         } */

        // Search Algorithm 
        int maxRow = 1;
     ;
        outerloop:
        while (maxRow < metaData.getNumCols()) {
            int i = 0;

            while (i <= maxRow && i >= -maxRow) {
                int j = 0;
                while (j <= maxRow && j >= -maxRow) {
                    if (i == 0 && j == 0) {
                        j++;
                        continue;
                    }
                    if (i + x >= 0 && i + x < metaData.getNumCols()
                            && j + y >= 0 && j + y < metaData.getNumRows()) {
                    // we found a valid neighbor!

                        if (linkedMasked[y + j][x + i] >= 0) {
                            minValue[0] = x + i;
                            minValue[1] = y + j;
                            System.out.println("minValue " + minValue[0] + " " + minValue[1]);
                    // Break all loops 

                            break outerloop;
                        }
                    }
                    if (j < maxRow && j >= 0) {
                        j++;
                    } else if (j == maxRow) {
                        j = -1;

                    } else {
                        j--;
                    }

                }
                if (i < maxRow && i >= 0) {
                    i++;
                } else if (i == maxRow) {
                    i = -1;

                } else {
                    i--;
                }
            }
            maxRow++;
        }

       
       return minValue;

    }
    public void displaySuggestedValue(int x , int y) throws VisADException, RemoteException
    {
        
        ArrayList<Float> list = inverseOfmatx(x, y);
        float[] LonLatBasin = new float[2];
        LonLatBasin[0] = list.get(0);
        LonLatBasin[1] = list.get(1);
        //LonLatBasin[0] = minValue[0];
        //LonLatBasin[0] = minValue[1];

        visad.Real[] rtd1 = {new visad.Real(visad.RealType.Longitude, LonLatBasin[0]),
            new visad.Real(visad.RealType.Latitude, LonLatBasin[1])};
            visad.ConstantMap[] rtmaps1 = {new visad.ConstantMap(0.0, visad.Display.Red),
            new visad.ConstantMap(1, visad.Display.Green),
            new visad.ConstantMap(0, visad.Display.Blue),
            new visad.ConstantMap(10.0, visad.Display.PointSize)};

         greenPointReference = new visad.DataReferenceImpl("OutletTuple");
         greenPointReference.setData(new visad.RealTuple(rtd1));
         display.addReference(greenPointReference, rtmaps1);
         displayComponent.add(greenPointReference);
    }

    public ArrayList<Float> inverseOfmatx(int MatX, int MatY) {
        ArrayList<Float> list = new ArrayList<Float>();
        float resultX = 0, resultY = 0;

        resultX = (float) ((float) (((MatX / 3600.0f) * metaData.getResLon()) + metaData.getMinLon()) + (metaData.getResLon() / 7200.0f));
        resultY = (float) (((MatY / 3600.0f) * metaData.getResLat()) + metaData.getMinLat() + (metaData.getResLat() / 7200.0f));
        list.add(resultX);
        list.add(resultY);

        return list;

    }

    public void commonTasks() throws VisADException, IOException {
        if (indexInKmlList < fullKmlPointList.size()) {
            jButton2.setText("Accept Suggestion");
// 3) Convert Cordinates to Matrix Values
            resultX = (double) ((Coordinate) fullKmlPointList.get(indexInKmlList)).getLongitude();
            resultY = (double) ((Coordinate) fullKmlPointList.get(indexInKmlList)).getLatitude();
            //  double resultY =  39.74813191132535;
            //  double resultX =  -105.5373613981691;
            System.out.println("Value of Matx and Mat y " + resultX + " " + resultY);
            MatX = (int) ((resultX - metaData.getMinLon()) / (float) metaData.getResLon() * 3600.0f);
            MatY = (int) ((resultY - metaData.getMinLat()) / (float) metaData.getResLat() * 3600.0f);
            System.out.println("Value of Matx and Mat y " + MatX + " " + MatY);
            // 4) Create Basin net mask for Cut Subsection to get Rivers
            netMask = getBasinMask(MatX, MatY);

            if (MatX > metaData.getNumCols() || MatX < 0 || MatY > metaData.getNumRows() || MatY < 0) {
                JOptionPane.showMessageDialog(this, "This point is outside DEM ");
            }

            Runnable addStreams = new Runnable() {
                public void run() {
                    try {

                        // matx is long (20) mat Y
                        // Create Sub Section
                        MetaRaster sectionMR = createSubSection(MatX, MatY);
                        // Display SubSection 
                        // reDisplaySubSectionAfterAcceptClicked(metaData);
                        reDisplaySubSectionAfterAcceptClicked(sectionMR);
                        // Display Kml Points
                        displayKmlpoints(indexInKmlList);
                        // Display River Network 
                        showAllOrdersActionPerformed();
                       int a[] = getSuggestedValue(MatX, MatY);
                        displaySuggestedValue(a[0], a[1]);

                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing streams with order ");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing streams");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addStreams).start();

        }

    }

    public void displaySubSection(hydroScalingAPI.io.MetaRaster metaData) throws VisADException, IOException {
        setTitle(metaData.toString());
        localField = metaData.getField();
        dr = new visad.java3d.TwoDDisplayRendererJ3D();
        display = new visad.java3d.DisplayImplJ3D("disp", dr);

        visad.GraphicsModeControl dispGMC = (visad.GraphicsModeControl) display.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);

        visad.ProjectionControl pc = display.getProjectionControl();
        pc.setAspectCartesian(new double[]{1.0, (double) metaData.getNumRows() / (double) metaData.getNumCols() * metaData.getResLat() / metaData.getResLon()});

        latitudeMap = new visad.ScalarMap(visad.RealType.Latitude, visad.Display.YAxis);
        latitudeMap.getAxisScale().setFont(font);
        latitudeMap.setRange(metaData.getMinLat(), metaData.getMaxLat());
        display.addMap(latitudeMap);

        longitudeMap = new visad.ScalarMap(visad.RealType.Longitude, visad.Display.XAxis);
        longitudeMap.getAxisScale().setFont(font);
        longitudeMap.setRange(metaData.getMinLon(), metaData.getMaxLon());
        display.addMap(longitudeMap);

        colorScaleMap = new visad.ScalarMap(visad.RealType.getRealType("varColor"), visad.Display.RGB);
        colorScaleMap.setRange(0, 255);
        display.addMap(colorScaleMap);

        visad.TextType t = visad.TextType.getTextType("text");
        tmap = new visad.ScalarMap(t, visad.Display.Text);
        display.addMap(tmap);

        visad.TextControl tcontrol = (visad.TextControl) tmap.getControl();
        tcontrol.setCenter(true);
        tcontrol.setSize(0.6);
        tcontrol.setAutoSize(true);
        tcontrol.setFont(font);

        display.enableEvent(visad.DisplayEvent.MOUSE_MOVED);
        display.addDisplayListener(this);

        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display);

        /* if(displayComponent.size() > 0 && displayComponent != null )
         {
             
         this.getContentPane().remove((Component) displayComponent.get(0));
         } */
        this.getContentPane().add("Center", display.getComponent());
        displayComponent.add(display.getComponent());
        hydroScalingAPI.subGUIs.widgets.RasterPalettesManager availablePalettes = new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(colorScaleMap);

        //If DEM load the Elevations Color Table
        if (metaData.getLocationBinaryFile().getName().lastIndexOf(".dem") != -1 | metaData.getLocationBinaryFile().getName().lastIndexOf(".corrDEM") != -1) {
            availablePalettes.setSelectedTable("Elevations");
        } else {
            availablePalettes.setSelectedTable("Rainbow");
        }

        refreshReferences(mainFrame.nameOnGauges(), mainFrame.nameOnLocations());
        show();
        toFront();
        updateUI();

        System.out.println("Value of Index" + indexInKmlList);
    }

    public void reDisplaySubSectionAfterAcceptClicked(hydroScalingAPI.io.MetaRaster metaData) throws VisADException, IOException {
        setTitle(metaData.toString());
        localField = metaData.getField();
        //dr = new visad.java3d.TwoDDisplayRendererJ3D();
        //display = new visad.java3d.DisplayImplJ3D("disp", dr);
        display.removeAllReferences();
        display.removeMap(latitudeMap);
        display.removeMap(longitudeMap);
        display.removeMap(colorScaleMap);
        display.removeMap(tmap);
        display.removeAllSlaves();

        visad.GraphicsModeControl dispGMC = (visad.GraphicsModeControl) display.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);

        visad.ProjectionControl pc = display.getProjectionControl();
        pc.setAspectCartesian(new double[]{1.0, (double) metaData.getNumRows() / (double) metaData.getNumCols() * metaData.getResLat() / metaData.getResLon()});

        latitudeMap = new visad.ScalarMap(visad.RealType.Latitude, visad.Display.YAxis);
        latitudeMap.getAxisScale().setFont(font);
        latitudeMap.setRange(metaData.getMinLat(), metaData.getMaxLat());
        display.addMap(latitudeMap);

        longitudeMap = new visad.ScalarMap(visad.RealType.Longitude, visad.Display.XAxis);
        longitudeMap.getAxisScale().setFont(font);
        longitudeMap.setRange(metaData.getMinLon(), metaData.getMaxLon());
        display.addMap(longitudeMap);

        colorScaleMap = new visad.ScalarMap(visad.RealType.getRealType("varColor"), visad.Display.RGB);
        colorScaleMap.setRange(0, 255);
        display.addMap(colorScaleMap);

        visad.TextType t = visad.TextType.getTextType("text");
        tmap = new visad.ScalarMap(t, visad.Display.Text);
        display.addMap(tmap);

        visad.TextControl tcontrol = (visad.TextControl) tmap.getControl();
        tcontrol.setCenter(true);
        tcontrol.setSize(0.6);
        tcontrol.setAutoSize(true);
        tcontrol.setFont(font);

        //display.enableEvent(visad.DisplayEvent.MOUSE_MOVED);
        //display.addDisplayListener(this);
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display);
        /* System.out.println("Size of Display List " +  this.getContentPane().getComponentCount());
         if(displayComponent.size() > 0 && displayComponent != null)
         {
             
         this.getContentPane().remove((Component) displayComponent.get(0));
         } 
         this.getContentPane().add("Center", display.getComponent());
         displayComponent.add(display.getComponent()); */
        hydroScalingAPI.subGUIs.widgets.RasterPalettesManager availablePalettes = new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(colorScaleMap);

        //If DEM load the Elevations Color Table
        if (metaData.getLocationBinaryFile().getName().lastIndexOf(".dem") != -1 | metaData.getLocationBinaryFile().getName().lastIndexOf(".corrDEM") != -1) {
            availablePalettes.setSelectedTable("Elevations");
        } else {
            availablePalettes.setSelectedTable("Rainbow");
        }

        refreshReferences(mainFrame.nameOnGauges(), mainFrame.nameOnLocations());
        show();
        toFront();
        updateUI();
        display.reDisplayAll();

        System.out.println("Value of Index" + indexInKmlList);
    }

    /**
     * A required method to handle interaction with the various visad.Display
     *
     * @param DispEvt The interaction event
     * @throws visad.VisADException Errors while handling VisAD objects
     * @throws java.rmi.RemoteException Errors while assigning data to VisAD
     * objects
     */
    public void displayChanged(visad.DisplayEvent DispEvt) throws visad.VisADException, java.rmi.RemoteException {

        int id = DispEvt.getId();

        if (activeEvent == 1) {

            System.out.println("Inside" + activeEvent);
        }

        if (activeEvent == 2) {
            System.out.println("Inside" + activeEvent);
        }

        if (activeEvent == 3) {
            System.out.println("Inside" + activeEvent);
        }

        if (id == DispEvt.MOUSE_RELEASED_RIGHT) {

            System.out.println("Inside11" + activeEvent);

            visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

            float resultX = longitudeMap.inverseScaleValues(new float[]{(float) ray.position[0]})[0];
            float resultY = latitudeMap.inverseScaleValues(new float[]{(float) ray.position[1]})[0];

            int MatX = (int) ((resultX - metaData.getMinLon()) / (float) metaData.getResLon() * 3600.0f);
            int MatY = (int) ((resultY - metaData.getMinLat()) / (float) metaData.getResLat() * 3600.0f);

            System.out.println(" new Position" + hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultX, 1) + " [" + MatX + "]");
            System.out.println(" new Position " + hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultY, 0) + " [" + MatY + "]");

            for (int i = 0; i < kmlList.getvisitedKMLList().size(); i++) {
                KmlPointDataPojo kmlObject = kmlList.getvisitedKMLList().get(i);

                if (kmlObject.getIndexInArray() == indexInKmlList) {

                    int dialogButton = JOptionPane.YES_NO_OPTION;
                    int dialogResult = JOptionPane.showConfirmDialog(this, "You want to Change point Location?", "Change location", dialogButton);
                    if (dialogResult == 0) {
                        
                        rightMouseClicked = true ; 
                        jButton2.setText("Accept");
                      display.removeReference(greenPointReference);
                        kmlObject.setNewLat((double) resultY);
                        kmlObject.setNewLon((double) resultX);
                        
                        kmlObject.setLinkId(linkedMasked[MatY][MatX]);
                    }

                }

            }

            //}
        }

        try {
            if (id == DispEvt.MOUSE_MOVED) {

                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

                float resultX = longitudeMap.inverseScaleValues(new float[]{(float) ray.position[0]})[0];
                float resultY = latitudeMap.inverseScaleValues(new float[]{(float) ray.position[1]})[0];
            //    System.out.println(" Value of X and Y" + resultX + " " + resultY);

                int MatX = (int) ((resultX - metaData.getMinLon()) / (float) metaData.getResLon() * 3600.0f);
                int MatY = (int) ((resultY - metaData.getMinLat()) / (float) metaData.getResLat() * 3600.0f);
                //   System.out.println(" Value of MatX n Y " +MatX + MatY );
                if (MatX < metaData.getNumCols() && MatX >= 0 && MatY < metaData.getNumRows() && MatY >= 0) {
                    setLongitudeLabel("" + linkedMasked[MatY][MatX]);
                }

               // System.out.println("Value of Link" + link);
                //     setLongitudeLabel(hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultX, 1) + " [" + MatX + "]");
                setLatitudeLabel(hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultY, 0) + " [" + MatY + "]");

                visad.RealTuple spotValue = (visad.RealTuple) localField.evaluate(new visad.RealTuple(domain, new double[]{resultX, resultY}), visad.Data.NEAREST_NEIGHBOR, visad.Data.NO_ERRORS);

                /*java.text.NumberFormat number4 = java.text.NumberFormat.getNumberInstance();
                 java.text.DecimalFormat dpoint4 = (java.text.DecimalFormat)number4;
                 dpoint4.applyPattern("0.00000000000000000000000");*/
                setValueLabel("" + spotValue.getValues()[0]);

            }

        } catch (Exception e) {
            System.err.println(e);
        }

    }

    public MetaRaster createSubSection(int x, int y) throws VisADException, IOException {

        hydroScalingAPI.io.MetaRaster sectionMR = new hydroScalingAPI.io.MetaRaster(metaData);

        int xUpperLimit = metaData.getNumCols();
        int xLowerLimit = 0;
        int yUpperLimit = metaData.getNumRows();
        int yLowerLimit = 0;
        // Both Limits in range 
        if (x + 30 <= metaData.getNumCols() && x - 20 >= 0) {
            xUpperLimit = x + 30;
            xLowerLimit = x - 20;
        } // lower limit not in range 
        else if (x + 30 <= metaData.getNumCols() && x - 20 < 0) {
            xUpperLimit = x + 30;
            xLowerLimit = 0;
        } // upper limit not in range 
        else if (x + 30 > metaData.getNumCols() && x - 20 >= 0) {
            xUpperLimit = metaData.getNumCols();
            xLowerLimit = x - 20;
        } // both limit not in range
        else {
            xUpperLimit = metaData.getNumCols();
            xLowerLimit = 0;
        }

        if (y + 30 <= metaData.getNumRows() && y - 20 >= 0) {
            yUpperLimit = y + 30;
            yLowerLimit = y - 20;
        } else if (y + 30 <= metaData.getNumRows() && y - 20 < 0) {
            yUpperLimit = y + 30;
            yLowerLimit = 0;
        } else if (y + 30 > metaData.getNumRows() && y - 20 >= 0) {
            yUpperLimit = metaData.getNumRows();
            yLowerLimit = y - 20;
        } else {
            yUpperLimit = metaData.getNumRows();
            yLowerLimit = 0;
        }
        subSetCorners[0][0] = xLowerLimit;
        subSetCorners[0][1] = yUpperLimit;

        subSetCorners[1][0] = xUpperLimit;
        subSetCorners[1][1] = yLowerLimit;

     //  subSetCorners[0][0] = x - 20;
        //   subSetCorners[0][1] = y + 30;
     //   subSetCorners[1][0] = x + 30;
        //   subSetCorners[1][1] = y - 20;
        sectionMR.setLocationMeta(metaData.getLocationMeta());
        sectionMR.setLocationBinaryFile(metaData.getLocationBinaryFile());

        sectionMR.setNumCols(Math.abs(subSetCorners[0][0] - subSetCorners[1][0]));
        sectionMR.setNumRows(Math.abs(subSetCorners[0][1] - subSetCorners[1][1]));
        sectionMR.setResLat(metaData.getResLat());
        sectionMR.setResLon(metaData.getResLon());
        sectionMR.setMinLat(hydroScalingAPI.tools.DegreesToDMS.getprettyString(metaData.getMinLat() + metaData.getResLat() / 3600. * (int) Math.min(subSetCorners[0][1], subSetCorners[1][1]), 0));
        sectionMR.setMinLon(hydroScalingAPI.tools.DegreesToDMS.getprettyString(metaData.getMinLon() + metaData.getResLon() / 3600. * (int) Math.min(subSetCorners[0][0], subSetCorners[1][0]), 1));

        sectionMR.setFormat("float");

        return sectionMR;
        // displaySubSection(metaData);

    }

    public byte[][] getBasinMask(int x, int y) {
        byte[][] basinMask = new byte[metaData.getNumRows()][metaData.getNumCols()];
        int xUpperLimit = metaData.getNumCols();
        int xLowerLimit = 0;
        int yUpperLimit = metaData.getNumRows();
        int yLowerLimit = 0;
        // Both Limits in range 
        if (x + 30 <= metaData.getNumCols() && x - 20 >= 0) {
            xUpperLimit = x + 30;
            xLowerLimit = x - 20;
        } // lower limit not in range 
        else if (x + 30 <= metaData.getNumCols() && x - 20 < 0) {
            xUpperLimit = x + 30;
            xLowerLimit = 0;
        } // upper limit not in range 
        else if (x + 30 > metaData.getNumCols() && x - 20 >= 0) {
            xUpperLimit = metaData.getNumCols();
            xLowerLimit = x - 20;
        } // both limit not in range
        else {
            xUpperLimit = metaData.getNumCols();
            xLowerLimit = 0;
        }

        if (y + 30 <= metaData.getNumRows() && y - 20 >= 0) {
            yUpperLimit = y + 30;
            yLowerLimit = y - 20;
        } else if (y + 30 <= metaData.getNumRows() && y - 20 < 0) {
            yUpperLimit = y + 30;
            yLowerLimit = 0;
        } else if (y + 30 > metaData.getNumRows() && y - 20 >= 0) {
            yUpperLimit = metaData.getNumRows();
            yLowerLimit = y - 20;
        } else {
            yUpperLimit = metaData.getNumRows();
            yLowerLimit = 0;
        }

        for (int i = yLowerLimit; i < yUpperLimit; i++) {
            for (int j = xLowerLimit; j < xUpperLimit; j++) {
                basinMask[i][j] = 1;
            }
        }
        System.out.println(" Values of Upper and lower limit x  then Y " + xUpperLimit + " " + yUpperLimit + " " + +xLowerLimit + " " + yLowerLimit);
        return basinMask;
    }

    protected void showAllOrdersActionPerformed() {

        /*    for (int orderRequested = 1; orderRequested <= localNetwork.getLargestOrder(); orderRequested++) {
         try {
         if (networkReferences.get("order" + orderRequested) != null) {
         visad.ConstantMap[] lineCMap = {new visad.ConstantMap(red[orderRequested - 1] / 255.0f, visad.Display.Red),
         new visad.ConstantMap(green[orderRequested - 1] / 255.0f, visad.Display.Green),
         new visad.ConstantMap(blue[orderRequested - 1] / 255.0f, visad.Display.Blue),
         //new visad.ConstantMap( orderRequested/2.0+1.0, visad.Display.LineWidth)};
         new visad.ConstantMap(1.5, visad.Display.LineWidth)};
         display.addReference((visad.DataReferenceImpl) networkReferences.get("order" + orderRequested), lineCMap);
         kmlDisplayReferences.add((visad.DataReferenceImpl) networkReferences.get("order" + orderRequested));
         } else {
         addStreamsWithOrder(orderRequested);
         }
         } catch (visad.VisADException exc) {
         System.err.println("Failed showing streams with order " + orderRequested);
         System.err.println(exc);
         } catch (java.io.IOException exc) {
         System.err.println("Failed showing streams with order " + orderRequested);
         System.err.println(exc);
         }
         } */
        for (int orderRequested = 1; orderRequested <= localNetwork.getLargestOrder(); orderRequested++) {
            addStreamsWithOrder(orderRequested);

        }

    }

    private void addStreamsWithOrder(final int orderRequested) {
        Runnable addStreams = new Runnable() {
            public void run() {
                try {

                    // visad.UnionSet toPlot = localNetwork.getMaskedUnionSet(orderRequested, netMask);
                    visad.UnionSet toPlot = localNetwork.getMaskedUnionSet(orderRequested, netMask);
                    if (toPlot != null) {
                        System.out.println("Inside Thread ");
                        visad.DataReferenceImpl refeElemVec = new visad.DataReferenceImpl("order" + orderRequested);
                        refeElemVec.setData(toPlot);
                        visad.ConstantMap[] lineCMap = {new visad.ConstantMap(red[orderRequested - 1] / 255.0f, visad.Display.Red),
                            new visad.ConstantMap(green[orderRequested - 1] / 255.0f, visad.Display.Green),
                            new visad.ConstantMap(blue[orderRequested - 1] / 255.0f, visad.Display.Blue),
                            //new visad.ConstantMap( orderRequested/2.0+1.0, visad.Display.LineWidth)};
                            new visad.ConstantMap(1.5, visad.Display.LineWidth)};
                        display.addReference(refeElemVec, lineCMap);
                        kmlDisplayReferences.add(refeElemVec);
                        networkReferences.put("order" + orderRequested, refeElemVec);
                    }
                } catch (visad.VisADException exc) {
                    System.err.println("Failed showing streams with order " + orderRequested);
                    System.err.println(exc);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed showing streams with order " + orderRequested);
                    System.err.println(exc);
                }
            }
        };
        new Thread(addStreams).start();

    }

    private void plotField() {
        Runnable addField = new Runnable() {
            public void run() {
                try {

                    visad.DataReferenceImpl ref_imaget1 = new visad.DataReferenceImpl("ref_imaget1");
                    ref_imaget1.setData(localField);
                    //kmlDisplayReferences.add(ref_imaget1);
                    display.addReference(ref_imaget1);
                } catch (visad.VisADException exc) {
                    System.err.println("Failed showing the field");
                    System.err.println(exc);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed showing the field");
                    System.err.println(exc);
                }
            }
        };
        new Thread(addField).start();
    }

    public void refreshReferences(final boolean gaugesWithNames, final boolean locationsWithNames) {
        try {
            display.removeAllReferences();
        } catch (visad.VisADException exc) {
            System.err.println(exc);
        } catch (java.rmi.RemoteException rmi) {
            System.err.println(rmi);
        }
        plotField();

    }

    protected void setLatitudeLabel(String latLabel) {
        latitudeLabel.setText(latLabel);
    }

    protected void setLongitudeLabel(String lonLabel) {
        longitudeLabel.setText(lonLabel);
    }

    protected void setValueLabel(String valLabel) {
        valueLabel.setText(valLabel);
    }

    public void setIdentifier(String windowIdentifier) {
        myStringID = windowIdentifier;
    }

    public String getIdentifier() {
        return myStringID;
    }

    public void displayKmlpoints(int i) throws VisADException, RemoteException {
        if (fullKmlPointList != null && i < fullKmlPointList.size()) {

            float[] LonLatBasin = new float[2];
            LonLatBasin[0] = (float) ((Coordinate) fullKmlPointList.get(i)).getLongitude();
            LonLatBasin[1] = (float) ((Coordinate) fullKmlPointList.get(i)).getLatitude();
            visad.Real[] rtd1 = {new visad.Real(visad.RealType.Longitude, LonLatBasin[0]),
                new visad.Real(visad.RealType.Latitude, LonLatBasin[1])};
            visad.ConstantMap[] rtmaps1 = {new visad.ConstantMap(1.0, visad.Display.Red),
                new visad.ConstantMap(0, visad.Display.Green),
                new visad.ConstantMap(0, visad.Display.Blue),
                new visad.ConstantMap(7.0, visad.Display.PointSize)};

            visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("OutletTuple");
            rtref1.setData(new visad.RealTuple(rtd1));
      // display.addReference(rtref1, rtmaps1);
            //display1 = new DisplayImplJ2D("display1");

            // Get display's graphics mode control draw scales
            display.addReferences(new DirectManipulationRendererJ3D(), rtref1, rtmaps1);
            kmlDisplayReferences.add(rtref1);
            KmlPointDataPojo kmlData = new KmlPointDataPojo();
            kmlData.setOriginalLat(((Coordinate) fullKmlPointList.get(i)).getLatitude());
            kmlData.setOriginalLon(((Coordinate) fullKmlPointList.get(i)).getLongitude());
            kmlData.setIndexInArray(i);
            kmlData.setVisited(true);
            kmlList.getvisitedKMLList().add(kmlData);

        }
    }

    protected void initializeKmlIndex() {
        try {
            kmlDisplayReferences = new ArrayList<Object>();
            kmlList = new KmlPointListPojo();
            String pathToStreams = metaData.getLocationBinaryFile().getPath();
            System.out.println("Valeu of File Path" + pathToStreams);
            String pathToLink = pathToStreams.substring(0, pathToStreams.lastIndexOf(".")) + kmlFileValue + ".xml";
            java.io.File fileLink = new java.io.File(pathToLink);
            if (fileLink.exists()) {
                kmlList.setvisitedKMLList(unMarshalingExample().getvisitedKMLList());
                KmlPointListPojo pojoReturned = unMarshalingExample();
                indexInKmlList = pojoReturned.getvisitedKMLList().size();
            } else {
                kmlList.setvisitedKMLList(new ArrayList<KmlPointDataPojo>());
                System.out.println("File Not Created");
                indexInKmlList = 0;
            }
        } catch (JAXBException ex) {
            Logger.getLogger(FixPoint.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    protected void readPointFromKml() throws VisADException {
        ReadKML kml = null;
         JFileChooser fc = new JFileChooser();
         int returnVal = fc.showOpenDialog(this);

         if (returnVal == JFileChooser.APPROVE_OPTION) {
         File file = fc.getSelectedFile();
         kml = new ReadKML(file.getAbsolutePath());
         
         kmlFileValue = file.getAbsolutePath().substring(file.getAbsolutePath().lastIndexOf("\\")+1, file.getAbsolutePath().lastIndexOf("."));
         System.out.println("Value of File" + kmlFileValue);
         } 
      //  kml = new ReadKML("C:\\Users\\achitale\\Desktop\\HYD53_119\\Vectors\\CuencasFundLinks1.kml");

        if (kml != null) {
            fullKmlPointList = kml.getPointList();

        }
    }

    private KmlPointListPojo unMarshalingExample() throws JAXBException {
        String pathToStreams = metaData.getLocationBinaryFile().getPath();
        System.out.println("Valeu of File Path" + pathToStreams);
        String pathToLink = pathToStreams.substring(0, pathToStreams.lastIndexOf(".")) + kmlFileValue + ".xml";
        JAXBContext jaxbContext = JAXBContext.newInstance(KmlPointListPojo.class);
        Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
        KmlPointListPojo emps = (KmlPointListPojo) jaxbUnmarshaller.unmarshal(new File(pathToLink));

        return emps;
    }

    private void marshalingExample() throws JAXBException {
        String pathToStreams = metaData.getLocationBinaryFile().getPath();
        System.out.println("Valeu of File Path" + pathToStreams);
        String pathToLink = pathToStreams.substring(0, pathToStreams.lastIndexOf(".")) + kmlFileValue + ".xml";
        JAXBContext jaxbContext = JAXBContext.newInstance(KmlPointListPojo.class);
        Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

        jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

        jaxbMarshaller.marshal(kmlList, System.out);
        jaxbMarshaller.marshal(kmlList, new File(pathToLink));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newtorkViewerPopUp = new javax.swing.JPopupMenu();
        basinsViewerPopUp = new javax.swing.JPopupMenu();
        editLog = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        toolsPanel = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        latitudeLabel = new javax.swing.JLabel();
        jPanel61 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        longitudeLabel = new javax.swing.JLabel();
        jPanel62 = new javax.swing.JPanel();
        jLabel323 = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        layersPanel = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jButton3 = new javax.swing.JButton();

        newtorkViewerPopUp.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        newtorkViewerPopUp.setLightWeightPopupEnabled(false);

        basinsViewerPopUp.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        basinsViewerPopUp.setLightWeightPopupEnabled(false);

        editLog.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        editLog.setText("Edit Basins Log");
        basinsViewerPopUp.add(editLog);
        basinsViewerPopUp.add(jSeparator1);

        setClosable(true);
        setIconifiable(true);
        setMaximizable(true);
        setResizable(true);

        toolsPanel.setLayout(new java.awt.GridLayout(2, 1));

        jPanel2.setLayout(new java.awt.GridLayout(1, 3));

        jPanel6.setLayout(new java.awt.BorderLayout());

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel3.setText("UpStream:  ");
        jPanel6.add(jLabel3, java.awt.BorderLayout.WEST);

        latitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        latitudeLabel.setText("00:00:00.00 N [000]");
        jPanel6.add(latitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel6);

        jPanel61.setLayout(new java.awt.BorderLayout());

        jLabel31.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel31.setText("LinkID : ");
        jPanel61.add(jLabel31, java.awt.BorderLayout.WEST);

        longitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        longitudeLabel.setText("00:00:00.00 W [000]");
        jPanel61.add(longitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel61);

        jPanel62.setLayout(new java.awt.BorderLayout());

        jLabel323.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel323.setText("Area : ");
        jPanel62.add(jLabel323, java.awt.BorderLayout.WEST);

        valueLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        valueLabel.setText("0000");
        jPanel62.add(valueLabel, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel62);

        toolsPanel.add(jPanel2);

        getContentPane().add(toolsPanel, java.awt.BorderLayout.NORTH);

        layersPanel.setLayout(new java.awt.GridLayout(1, 5));

        jButton1.setText("Ignore");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                IgnoreClicked(evt);
            }
        });
        layersPanel.add(jButton1);

        jButton2.setText("Accept Suggestion");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                AcceptedClicked(evt);
            }
        });
        layersPanel.add(jButton2);

        jButton3.setText("Save");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SaveClicked(evt);
            }
        });
        layersPanel.add(jButton3);

        getContentPane().add(layersPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void IgnoreClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_IgnoreClicked
      // Remove the previous Point 
        // Update The Counter 
        try {
          if (indexInKmlList < fullKmlPointList.size()) {
                for (int i = 0; i < kmlDisplayReferences.size(); i++) {
                     display.removeReference((ThingReference) kmlDisplayReferences.get(i));
                }
                indexInKmlList++;
                //displayKmlpoints(indexInKmlList);
                commonTasks();
                //System.out.println("Value of Mat X , Mat Y" );
              
            }

            // Display The New point 
            if (indexInKmlList == fullKmlPointList.size()) {

                JOptionPane.showMessageDialog(this, "All Points are Finished");

            }
             } catch (VisADException ex) {
            Logger.getLogger(RasterViewer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(RasterViewer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FixPoint.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_IgnoreClicked

    private void AcceptedClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_AcceptedClicked

        try {
            if(jButton2.getText().equalsIgnoreCase("Accept Suggestion"))
            {
                int a[]= getSuggestedValue(MatX, MatY);
                // Assign the Suggested Pixel Id . 
              
              
              for (int i = 0; i < kmlList.getvisitedKMLList().size(); i++) {
                KmlPointDataPojo kmlObject = kmlList.getvisitedKMLList().get(i);

                if (kmlObject.getIndexInArray() == indexInKmlList) {
                    if (a[0] < metaData.getNumCols() && a[0] >= 0 && a[1] < metaData.getNumRows() && a[1] >= 0) {
                  System.out.println( " Link Id" + linkedMasked[a[1]][a[0]]);
                  kmlObject.setLinkId(linkedMasked[a[1]][a[0]]);
                        ArrayList<Float> resultArray =  inverseOfmatx(MatX, MatY);
                    kmlObject.setNewLat((double)resultArray.get(1));
                    kmlObject.setNewLon((double)resultArray.get(0));
                    break;
                }    
                    
                    
                      
                }

            }
            }
            // Else The Value Would be set when you right click on mouse.
             
            // Remove the Previous point

            //display.removeAllReferences();
            // Write the Corrected Value of previous Point in File 
            // Update The Counter 
            if (indexInKmlList < fullKmlPointList.size()) {

                for (int i = 0; i < kmlDisplayReferences.size(); i++) {

                    display.removeReference((ThingReference) kmlDisplayReferences.get(i));
                }
                indexInKmlList++;
                //displayKmlpoints(indexInKmlList);
                commonTasks();
                //System.out.println("Value of Mat X , Mat Y" );
              
                
            
            }

            // Display The New point 
            if (indexInKmlList == fullKmlPointList.size()) {

                JOptionPane.showMessageDialog(this, "All Points are Finished");

            }
        } catch (VisADException ex) {
            Logger.getLogger(RasterViewer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (RemoteException ex) {
            Logger.getLogger(RasterViewer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(FixPoint.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_AcceptedClicked


    private void SaveClicked(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SaveClicked
        try {
            /* FileWriter f2=null;
             FileWriter f1=null;
             try {
             String pathToStreams=metaData.getLocationBinaryFile().getPath();
             System.out.println("Valeu of File Path" + pathToStreams);
             String  pathToLink=pathToStreams.substring(0,pathToStreams.lastIndexOf("."))+".txt";
             String  xmlFile=pathToStreams.substring(0,pathToStreams.lastIndexOf("."))+".xml";
             java.io.File fileLink =new java.io.File(pathToLink);
             f2 = new FileWriter(fileLink, false);
             java.io.File xmlLink =new java.io.File(xmlFile);
             f1 = new FileWriter(xmlLink, true);
             f2.write(indexInKmlList);
             f2.write(System.getProperty("line.separator"));
             for (int i = 0; i <  kmlList.getvisitedKMLList().size(); i++) {
             f1.write( kmlList.getvisitedKMLList().get(i).toString());
             f1.write(System.getProperty("line.separator"));
             }
             System.out.println("Value of Index" + indexInKmlList);
             f2.close();
             f1.close();
             } catch (IOException ex) {
             Logger.getLogger(FixPoint.class.getName()).log(Level.SEVERE, null, ex);
             } finally {
             try {
             f2.close();
             } catch (IOException ex) {
             Logger.getLogger(FixPoint.class.getName()).log(Level.SEVERE, null, ex);
             }
             }
             */
            marshalingExample();
            JOptionPane.showMessageDialog(this, "Data upto this Point Saved in XML File.");

        } catch (JAXBException ex) {
            Logger.getLogger(FixPoint.class.getName()).log(Level.SEVERE, null, ex);
        }


    }//GEN-LAST:event_SaveClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu basinsViewerPopUp;
    private javax.swing.JMenuItem editLog;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel323;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JPanel layersPanel;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.JPopupMenu newtorkViewerPopUp;
    private javax.swing.JPanel toolsPanel;
    private javax.swing.JLabel valueLabel;
    // End of variables declaration//GEN-END:variables

}
