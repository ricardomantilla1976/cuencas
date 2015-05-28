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
 * DemViewer2D.java
 *
 * Created on June 20, 2003, 2:34 PM
 */

package hydroScalingAPI.modules.swmmCoupling.subGUIs.widgets;

import hydroScalingAPI.modules.swmmCoupling.util.geomorphology.objects.SubBasin;
import java.util.HashMap;

/**
 * The extension of the {@link hydroScalingAPI.subGUIs.widgets.RasterViewer} for
 * for 2-dimensional visualization of DEMs and derived fields
 * @author Ricardo Mantilla
 */
public class DemViewer2D_subBasin extends hydroScalingAPI.modules.swmmCoupling.subGUIs.widgets.RasterViewer_subBasin implements visad.DisplayListener{
    
    private visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
    
    /**
     * Creates new instance of DemViewer2D
     * @param relMaps A {@link java.util.Hashtable} with paths to the derived quantities and with keys
     * that describe the variable
     * @param parent The main GIS interface
     * @param md The MetaRaster asociated with the DEM
     * @throws java.rmi.RemoteException Captures remote exceptions
     * @throws visad.VisADException Captures VisAD Exeptions
     * @throws java.io.IOException Captures I/O Execptions
     */
    public DemViewer2D_subBasin(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md, java.util.Hashtable relMaps) throws java.rmi.RemoteException, visad.VisADException, java.io.IOException{
        super(parent,md,relMaps);
        
        setTitle(metaData.toString());
        localField=metaData.getField();
        
        dr=new  visad.java3d.TwoDDisplayRendererJ3D();
        display = new visad.java3d.DisplayImplJ3D("disp",dr);
        
        visad.GraphicsModeControl dispGMC = (visad.GraphicsModeControl) display.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        visad.ProjectionControl pc = display.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, (double)metaData.getNumRows()/(double)metaData.getNumCols()*metaData.getResLat()/metaData.getResLon()});
        
        latitudeMap=new visad.ScalarMap(visad.RealType.Latitude, visad.Display.YAxis);
        latitudeMap.getAxisScale().setFont(font);
        latitudeMap.setRange(metaData.getMinLat(),metaData.getMaxLat());
        display.addMap(latitudeMap);
        
        longitudeMap=new visad.ScalarMap(visad.RealType.Longitude, visad.Display.XAxis);
        longitudeMap.getAxisScale().setFont(font);
        longitudeMap.setRange(metaData.getMinLon(),metaData.getMaxLon());
        display.addMap(longitudeMap);
        
        colorScaleMap=new visad.ScalarMap(visad.RealType.getRealType("varColor"), visad.Display.RGB);
        colorScaleMap.setRange(0,255);
        display.addMap(colorScaleMap);
        
        //If metaDEM & already processed enable DEM Tools
        String pathToNetwork=metaData.getLocationBinaryFile().getPath();
        pathToNetwork=pathToNetwork.substring(0,pathToNetwork.lastIndexOf("."))+".stream";

        boolean isProcessed=new java.io.File(pathToNetwork).exists();
        if (isProcessed && metaData.getLocationMeta().getName().lastIndexOf(".metaDEM") != -1){
            demToolsEnable(true);
            java.io.File originalFile=metaData.getLocationBinaryFile();
            String originalFormat=metaData.getFormat();
            
            metaData.setLocationBinaryFile(new java.io.File(originalFile.getParent()+"/"+originalFile.getName().substring(0,originalFile.getName().lastIndexOf("."))+".dir"));
            metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
            
            fullDirMatrix=new hydroScalingAPI.io.DataRaster(metaData).getByte();
            
            metaData.setLocationBinaryFile(originalFile);
            metaData.setFormat(originalFormat);
            
        } else {
            demToolsEnable(false);
        }
        
        visad.TextType t = visad.TextType.getTextType("text");
        visad.ScalarMap tmap=new visad.ScalarMap(t, visad.Display.Text);
        display.addMap(tmap);
        
        visad.TextControl tcontrol = (visad.TextControl) tmap.getControl();
        tcontrol.setCenter(true);
        tcontrol.setSize(0.6);
        tcontrol.setAutoSize(true);
        tcontrol.setFont(font);
        
        display.enableEvent(visad.DisplayEvent.MOUSE_MOVED);
        display.addDisplayListener(this);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(display);
        
        this.getContentPane().add("Center",display.getComponent());
        
        hydroScalingAPI.subGUIs.widgets.RasterPalettesManager availablePalettes=new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(colorScaleMap);
                    
        //If DEM load the Elevations Color Table
        if (metaData.getLocationBinaryFile().getName().lastIndexOf(".dem") != -1 | metaData.getLocationBinaryFile().getName().lastIndexOf(".corrDEM") != -1)
            availablePalettes.setSelectedTable("Elevations");
        else
            availablePalettes.setSelectedTable("Rainbow");
                    
        super.refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
        
        show();
        toFront();
        updateUI();
        
    }
    
    /**
     * A required method to handle interaction with the various visad.Display
     * @param DispEvt The interaction event
     * @throws visad.VisADException Errors while handling VisAD objects
     * @throws java.rmi.RemoteException Errors while assigning data to VisAD objects
     */
    public void displayChanged(visad.DisplayEvent DispEvt) throws visad.VisADException, java.rmi.RemoteException {
        HashMap currentSubBasin;
                
        int id = DispEvt.getId();
        
        if (activeEvent == 1){
            try {
                if(DispEvt.getId() == visad.DisplayEvent.MOUSE_RELEASED_CENTER){

                    visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

                    float resultX= longitudeMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                    float resultY= latitudeMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];

                    int MatX=(int) ((resultX -metaData.getMinLon())/(float) metaData.getResLon()*3600.0f);
                    int MatY=(int) ((resultY -metaData.getMinLat())/(float) metaData.getResLat()*3600.0f);
                    
                    // verify if subbasin already exists
                    currentSubBasin = super.localBasinsLog.getSubBasin(MatX, MatY);
                    if (currentSubBasin == null) {
                        SubBasin newSubBas;
                        newSubBas = new SubBasin(MatX, MatY, 
                                                 this.fullDirMatrix,
                                                 this.metaData);
                        currentSubBasin = newSubBas.getHashedData(this.metaData);
                        super.localBasinsLog.addNewSubBasin(currentSubBasin);
                    }
                    
                    traceSubBasinContour(currentSubBasin, true);
                    
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        
        if (activeEvent == 2){
            try {
                if(DispEvt.getId() == visad.DisplayEvent.MOUSE_RELEASED_CENTER){

                    visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

                    float resultX= longitudeMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                    float resultY= latitudeMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];

                    int MatX=(int) ((resultX -metaData.getMinLon())/(float) metaData.getResLon()*3600.0f);
                    int MatY=(int) ((resultY -metaData.getMinLat())/(float) metaData.getResLat()*3600.0f);
                    
                    traceRiverPath(MatX, MatY);                    
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        
        if (activeEvent == 3){
            try {
                if(DispEvt.getId() == visad.DisplayEvent.MOUSE_RELEASED_CENTER){

                    visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

                    float resultX= longitudeMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                    float resultY= latitudeMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];

                    hydroScalingAPI.subGUIs.widgets.LocationsEditor theEditor=new hydroScalingAPI.subGUIs.widgets.LocationsEditor(mainFrame);
                    theEditor.setLatLong(resultY,resultX);
                    theEditor.setVisible(true);

                    mainFrame.addNewLocationInteractively(theEditor);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }
        
        if (activeEvent == 4){
            try {
                if(DispEvt.getId() == visad.DisplayEvent.MOUSE_RELEASED_CENTER){

                    visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());

                    float resultX= longitudeMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                    float resultY= latitudeMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];

                    int MatX=(int) ((resultX -metaData.getMinLon())/(float) metaData.getResLon()*3600.0f);
                    int MatY=(int) ((resultY -metaData.getMinLat())/(float) metaData.getResLat()*3600.0f);
                    
                    assignSubDataSet(MatX,MatY);
                }
            } catch (Exception e) {
                System.err.println(e);
            }
        }

        try {
            if (id == DispEvt.MOUSE_MOVED) {
                
                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());
                
                float resultX= longitudeMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                float resultY= latitudeMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];
                
                int MatX=(int) ((resultX -metaData.getMinLon())/(float) metaData.getResLon()*3600.0f);
                int MatY=(int) ((resultY -metaData.getMinLat())/(float) metaData.getResLat()*3600.0f);
                
                
                setLongitudeLabel(hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultX,1)+" ["+MatX+"]");
                setLatitudeLabel(hydroScalingAPI.tools.DegreesToDMS.getprettyString(resultY,0)+" ["+MatY+"]");
                visad.RealTuple spotValue=(visad.RealTuple) localField.evaluate(new visad.RealTuple(domain, new double[] {resultX,resultY}),visad.Data.NEAREST_NEIGHBOR,visad.Data.NO_ERRORS);
                
                /*java.text.NumberFormat number4 = java.text.NumberFormat.getNumberInstance();
                java.text.DecimalFormat dpoint4 = (java.text.DecimalFormat)number4;
                dpoint4.applyPattern("0.00000000000000000000000");*/
                
                setValueLabel(""+spotValue.getValues()[0]);
                
            }
            
        } catch (Exception e) {
            System.err.println(e);
        }
        
    }
    
}
