package hydroScalingAPI.subGUIs.widgets;

import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import hydroScalingAPI.subGUIs.widgets.kml.ReadKML;
import java.io.File;
import java.rmi.RemoteException;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import visad.DisplayImpl;
import visad.GraphicsModeControl;
import visad.VisADException;
import visad.java2d.DirectManipulationRendererJ2D;
import visad.java2d.DisplayImplJ2D;
import visad.java3d.DirectManipulationRendererJ3D;

public class FixPointViewer extends hydroScalingAPI.subGUIs.widgets.FixPoint /*implements visad.DisplayListener */{

   private visad.RealTupleType domain = new visad.RealTupleType(visad.RealType.Longitude, visad.RealType.Latitude);

    public FixPointViewer(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md, java.util.Hashtable relMaps) throws java.rmi.RemoteException, visad.VisADException, java.io.IOException {

        super(parent, md, relMaps);
/*
        setTitle(metaData.toString());
        localField = metaData.getField();

        dr = new visad.java3d.TwoDDisplayRendererJ3D();
        display = new visad.java3d.DisplayImplJ3D("disp", dr);
        // display =  new DisplayImplJ2D("display1");

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

        //If metaDEM & already processed enable DEM Tools
       // String pathToNetwork = metaData.getLocationBinaryFile().getPath();
        //pathToNetwork = pathToNetwork.substring(0, pathToNetwork.lastIndexOf(".")) + ".stream";

        visad.TextType t = visad.TextType.getTextType("text");
        visad.ScalarMap tmap = new visad.ScalarMap(t, visad.Display.Text);
        display.addMap(tmap);

        visad.TextControl tcontrol = (visad.TextControl) tmap.getControl();
        tcontrol.setCenter(true);
        tcontrol.setSize(0.6);
        tcontrol.setAutoSize(true);
        tcontrol.setFont(font);

        display.enableEvent(visad.DisplayEvent.MOUSE_MOVED);
        display.addDisplayListener(this);

        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display);

        this.getContentPane().add("Center", display.getComponent());

        hydroScalingAPI.subGUIs.widgets.RasterPalettesManager availablePalettes = new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(colorScaleMap);

        //If DEM load the Elevations Color Table
        if (metaData.getLocationBinaryFile().getName().lastIndexOf(".dem") != -1 | metaData.getLocationBinaryFile().getName().lastIndexOf(".corrDEM") != -1) {
            availablePalettes.setSelectedTable("Elevations");
        } else {
            availablePalettes.setSelectedTable("Rainbow");
        }

        super.refreshReferences(mainFrame.nameOnGauges(), mainFrame.nameOnLocations());
        show();
        toFront();
        updateUI();
        readPointFromKml();

       
        initializeKmlIndex();
        System.out.println("Value of Index" + indexInKmlList );
        displayKmlpoints(indexInKmlList);

        showAllOrdersActionPerformed(); */
    }

    /**
     * A required method to handle interaction with the various visad.Display
     *
     * @param DispEvt The interaction event
     * @throws visad.VisADException Errors while handling VisAD objects
     * @throws java.rmi.RemoteException Errors while assigning data to VisAD
     * objects
     */
    /*
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
            System.out.println("Inside" + activeEvent);

            visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

            float resultX = longitudeMap.inverseScaleValues(new float[]{(float) ray.position[0]})[0];
            float resultY = latitudeMap.inverseScaleValues(new float[]{(float) ray.position[1]})[0];

            int MatX = (int) ((resultX - metaData.getMinLon()) / (float) metaData.getResLon() * 3600.0f);
            int MatY = (int) ((resultY - metaData.getMinLat()) / (float) metaData.getResLat() * 3600.0f);

            System.out.println(" new Position" + hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultX, 1) + " [" + MatX + "]");
            System.out.println(" new Position " + hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultY, 0) + " [" + MatY + "]");

            int dialogButton = JOptionPane.YES_NO_OPTION;
            int dialogResult = JOptionPane.showConfirmDialog(this, "You want to Change point Location?", "Change location", dialogButton);
            if (dialogResult == 0) {
                for (int i = 0; i <  kmlList.getvisitedKMLList().size(); i++) {
                    KmlPointDataPojo kmlObject =  kmlList.getvisitedKMLList().get(i);

                    if (kmlObject.getIndexInArray() == indexInKmlList) {
                        kmlObject.setNewLat((double) resultY);
                        kmlObject.setNewLon((double) resultX);
                    }

                }

            }
        }

        try {
            if (id == DispEvt.MOUSE_MOVED) {

                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

                float resultX = longitudeMap.inverseScaleValues(new float[]{(float) ray.position[0]})[0];
                float resultY = latitudeMap.inverseScaleValues(new float[]{(float) ray.position[1]})[0];

                int MatX = (int) ((resultX - metaData.getMinLon()) / (float) metaData.getResLon() * 3600.0f);
                int MatY = (int) ((resultY - metaData.getMinLat()) / (float) metaData.getResLat() * 3600.0f);

                setLongitudeLabel(hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultX, 1) + " [" + MatX + "]");
                setLatitudeLabel(hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultY, 0) + " [" + MatY + "]");
                visad.RealTuple spotValue = (visad.RealTuple) localField.evaluate(new visad.RealTuple(domain, new double[]{resultX, resultY}), visad.Data.NEAREST_NEIGHBOR, visad.Data.NO_ERRORS);

                /*java.text.NumberFormat number4 = java.text.NumberFormat.getNumberInstance();
                 java.text.DecimalFormat dpoint4 = (java.text.DecimalFormat)number4;
                 dpoint4.applyPattern("0.00000000000000000000000");*/
              /*  setValueLabel("" + spotValue.getValues()[0]);

            }

        } catch (Exception e) {
            System.err.println(e);
        }

    } */

}
