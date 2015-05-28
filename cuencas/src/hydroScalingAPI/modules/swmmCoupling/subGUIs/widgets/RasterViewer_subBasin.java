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

package hydroScalingAPI.modules.swmmCoupling.subGUIs.widgets;

import hydroScalingAPI.io.MetaPolygon;
import hydroScalingAPI.modules.swmmCoupling.io.SubBasinsLogManager;
import hydroScalingAPI.modules.swmmCoupling.util.geomorphology.objects.SubBasin;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import visad.DataReferenceImpl;
import visad.Gridded2DSet;
import visad.VisADException;

/**
 * The internal GIS frame for displaying maps and overlaying vector and sites
 * @author Ricardo Mantilla
 */

public abstract class RasterViewer_subBasin extends javax.swing.JInternalFrame {
    
    private String myStringID;
    protected SubBasinsLogManager localBasinsLog;
    
    private java.util.Hashtable relatedMapsList;

    private java.util.Vector temporalReferences=new java.util.Vector();
    private java.util.Vector basinOutlets=new java.util.Vector();

    private float[] red;
    private float[] green;
    private float[] blue;

    private hydroScalingAPI.io.MetaNetwork localNetwork;
    private java.util.Hashtable networkReferences=new java.util.Hashtable();
    
    private int[][] subSetCorners=new int[2][2];
    private int subSetCornersCounter=0;
    
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
    
    /**
     * The visad.FlatField with data
     */
    protected visad.FlatField localField;
    
    /**
     * The active event for the mouse middle button
     */
    protected int activeEvent=0;
    
    /**
     * The direction matrix associated to the DEM (only if a processed DEM is being displayed)
     */
    protected byte[][] fullDirMatrix;
    
    /**
     * Creates new instance of RasterViewer
     * @param relMaps A {@link java.util.Hashtable} with paths to the derived quantities and with keys
     * that describe the variable
     * @param parent The main GIS interface
     * @param md The MetaRaster asociated with the DEM
     */
    public RasterViewer_subBasin(hydroScalingAPI.mainGUI.ParentGUI parent,hydroScalingAPI.io.MetaRaster md, java.util.Hashtable relMaps) {
        mainFrame=parent;
        metaData=md;
        relatedMapsList=relMaps;
        
        red=    mainFrame.getInfoManager().getNetworkRed();
        green=  mainFrame.getInfoManager().getNetworkGreen();
        blue=   mainFrame.getInfoManager().getNetworkBlue();
        
        initComponents();
        setSize(650,600);
        
        updateRelatedMaps();
        addInternalFrameListener(mainFrame);
        
    }
    
    private boolean isDEM(){
        return metaData.getLocationMeta().getName().toLowerCase().lastIndexOf(".metadem") != -1;
    }
    
    /**
     * Enables the set of tools for processed DEMs
     * @param enabled The status for the tools
     */
    protected void demToolsEnable(boolean enabled){
        showNetwork.setEnabled(enabled);
        showBasins.setEnabled(enabled);
        selectOutlet.setEnabled(enabled);
        traceStream.setEnabled(enabled);
        if(enabled){
            updateNetworkPopupMenu();
            updateBasinsPopupMenu();
        }
    }
    
    private void updateBasinsPopupMenu(){
        java.io.File originalFile=metaData.getLocationBinaryFile();
        java.io.File logFile=new java.io.File(originalFile.getParent()+"/"+originalFile.getName().substring(0,originalFile.getName().lastIndexOf("."))+".subLog");
        
        if(logFile.exists()){
            
            try{
                localBasinsLog=new SubBasinsLogManager(logFile);
            } catch(java.io.IOException IOE){
                System.err.println("Failed loading basins log. "+metaData);
                System.err.println(IOE);
                return;
            }
            
            String[] basinsPreSelected = localBasinsLog.getPresetSubBasins();
            for(int i=0;i<basinsPreSelected.length;i++){
                
                addPreselectedBasin(basinsPreSelected[i]);
                
            }
            
            
        } else {
            try{
                logFile.createNewFile();
                localBasinsLog = new hydroScalingAPI.modules.swmmCoupling.io.SubBasinsLogManager(logFile);
            } catch(java.io.IOException IOE){
                System.err.println("Failed creating new basins log. "+metaData);
                System.err.println(IOE);
                return;
            }
        }
        
    }
    
    private void addPreselectedBasin(String basinLabel){
        javax.swing.JMenuItem showBasinI = new javax.swing.JMenuItem();
        showBasinI.setFont(new java.awt.Font("Dialog", 0, 10));
        showBasinI.setText(basinLabel);
        showBasinI.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSubBasinPreSelectedActionPerformed(evt);
            }
        });

        basinsViewerPopUp.add(showBasinI);
    }
    
    private void showSubBasinPreSelectedActionPerformed(java.awt.event.ActionEvent evt) {
        HashMap subBasinHash;
        
        javax.swing.JMenuItem showOrderW = (javax.swing.JMenuItem)evt.getSource();
        
        String[] basinLabel=showOrderW.getText().split(" ; ");
        String xlabel=(basinLabel[0].split(", "))[0];
        String ylabel=(basinLabel[0].split(", "))[1];
        int matX=Integer.parseInt(xlabel.substring(2).trim());
        int matY=Integer.parseInt(ylabel.substring(2).trim());
        
        subBasinHash = this.localBasinsLog.getSubBasin(matX, matY);
        
        try{
            traceSubBasinContour(subBasinHash,false);
        } catch (visad.VisADException vie){
            System.err.println("Failed reloading basin: "+showOrderW.getText());
            System.err.println(vie);
        } catch (java.rmi.RemoteException rme){
            System.err.println("Failed reloading basin: "+showOrderW.getText());
            System.err.println(rme);
        }
        
        
    }
    
    private void updateNetworkPopupMenu(){
        
        javax.swing.JCheckBoxMenuItem showAllOrders = new javax.swing.JCheckBoxMenuItem();
        showAllOrders.setFont(new java.awt.Font("Dialog", 0, 10));
        showAllOrders.setText("Show All Orders");
        showAllOrders.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showAllOrdersActionPerformed(evt);
            }
        });
        
        addToNetworkPupUp(showAllOrders);
        
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        
        addToNetworkPupUp(jSeparator1);
        
        try{
            localNetwork=new hydroScalingAPI.io.MetaNetwork(metaData);
        } catch(java.io.IOException IOE){
            System.err.println("Failed loading network file. "+metaData);
            System.err.println(IOE);
            return;
        }
        int largestOrder=localNetwork.getLargestOrder();
        
        for (int i=0;i<largestOrder;i++){
            
            javax.swing.JCheckBoxMenuItem showOrderW = new javax.swing.JCheckBoxMenuItem();
            showOrderW.setFont(new java.awt.Font("Dialog", 0, 10));
            showOrderW.setText("Show Order "+(largestOrder-i)+"  streams");
            showOrderW.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    showOrderWActionPerformed(evt);
                }
            });

            addToNetworkPupUp(showOrderW);
            
        }
        
    }

    private void showAllOrdersActionPerformed(java.awt.event.ActionEvent evt) {
        selectAllNetworkPopup();
        javax.swing.JCheckBoxMenuItem showAllOrders = (javax.swing.JCheckBoxMenuItem)evt.getSource();
        if(showAllOrders.isSelected()){
            for(int orderRequested=1;orderRequested<=localNetwork.getLargestOrder();orderRequested++){
                try {
                    if(networkReferences.get("order"+orderRequested) != null){
                        visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( red[orderRequested-1]/255.0f, visad.Display.Red),
                                            new visad.ConstantMap( green[orderRequested-1]/255.0f, visad.Display.Green),
                                            new visad.ConstantMap( blue[orderRequested-1]/255.0f, visad.Display.Blue),
                                            //new visad.ConstantMap( orderRequested/2.0+1.0, visad.Display.LineWidth)};
                                            new visad.ConstantMap( 1.5, visad.Display.LineWidth)};
                        display.addReference((visad.DataReferenceImpl)networkReferences.get("order"+orderRequested),lineCMap);
                    } else {
                        addStreamsWithOrder(orderRequested);
                    }
                } catch (visad.VisADException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                }
            }
        } else {
            for(int orderRequested=1;orderRequested<=localNetwork.getLargestOrder();orderRequested++){
                try {
                    display.removeReference((visad.DataReferenceImpl)networkReferences.get("order"+orderRequested));
                } catch (visad.VisADException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                }
            }
        }
    }
    
    private void showOrderWActionPerformed(java.awt.event.ActionEvent evt) {
        javax.swing.JCheckBoxMenuItem showOrderW = (javax.swing.JCheckBoxMenuItem)evt.getSource();
        int orderRequested=Integer.parseInt(showOrderW.getText().substring(11,13).trim());
        if (showOrderW.isSelected())
            try {
                if(networkReferences.get("order"+orderRequested) != null){
                    visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( red[orderRequested-1]/255.0f, visad.Display.Red),
                                        new visad.ConstantMap( green[orderRequested-1]/255.0f, visad.Display.Green),
                                        new visad.ConstantMap( blue[orderRequested-1]/255.0f, visad.Display.Blue),
                                        //new visad.ConstantMap( orderRequested/2.0+1.0, visad.Display.LineWidth)};
                                        new visad.ConstantMap( 1.5, visad.Display.LineWidth)};
                    display.addReference((visad.DataReferenceImpl)networkReferences.get("order"+orderRequested),lineCMap);
                } else {
                    addStreamsWithOrder(orderRequested);
                }
            } catch (visad.VisADException exc) {
                System.err.println("Failed showing streams with order "+orderRequested);
                System.err.println(exc);
            } catch (java.io.IOException exc) {
                System.err.println("Failed showing streams with order "+orderRequested);
                System.err.println(exc);
            }
        else
            try {
                display.removeReference((visad.DataReferenceImpl)networkReferences.get("order"+orderRequested));
            } catch (visad.VisADException exc) {
                System.err.println("Failed showing streams with order "+orderRequested);
                System.err.println(exc);
            } catch (java.io.IOException exc) {
                System.err.println("Failed showing streams with order "+orderRequested);
                System.err.println(exc);
            }
        
    }
    
    private void addStreamsWithOrder(final int orderRequested){
        Runnable addStreams = new Runnable() {
            public void run() {
                try {

                    visad.UnionSet toPlot = localNetwork.getUnionSet(orderRequested);
                    if (toPlot != null){
                        visad.DataReferenceImpl refeElemVec=new visad.DataReferenceImpl("order"+orderRequested);
                        refeElemVec.setData(toPlot);
                        visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( red[orderRequested-1]/255.0f, visad.Display.Red),
                                        new visad.ConstantMap( green[orderRequested-1]/255.0f, visad.Display.Green),
                                        new visad.ConstantMap( blue[orderRequested-1]/255.0f, visad.Display.Blue),
                                        //new visad.ConstantMap( orderRequested/2.0+1.0, visad.Display.LineWidth)};
                                        new visad.ConstantMap( 1.5, visad.Display.LineWidth)};
                        display.addReference(refeElemVec,lineCMap);
                        networkReferences.put("order"+orderRequested,refeElemVec);
                    }
                } catch (visad.VisADException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                } catch (java.io.IOException exc) {
                    System.err.println("Failed showing streams with order "+orderRequested);
                    System.err.println(exc);
                }
            }
        };
        new Thread(addStreams).start();
    }
    
    private void saveImage(java.io.File f) throws visad.VisADException, java.io.IOException {
        java.awt.image.BufferedImage image = (java.awt.image.BufferedImage) display.getImage();
        
        if(!f.getName().endsWith(".jpg")){
            f= new java.io.File(f.getPath()+".jpg");
        }
        
        ij.io.FileSaver saver =new ij.io.FileSaver(new ij.ImagePlus("null",image));
        saver.saveAsJpeg(f.getPath());
        
    }
    
    /**
     * Creates a dataReference for the polygon describing a subbasin contour.
     * @param subBasinHash
     * @param isNew
     * @throws visad.VisADException
     * @throws java.rmi.RemoteException 
     */
    protected void traceSubBasinContour(final HashMap subBasinHash, boolean isNew) throws visad.VisADException, java.rmi.RemoteException{
        Integer matXBrute, matYBrute;
        final int MatX, MatY;
        
        // get brute data and basic check it
        matXBrute = (Integer)subBasinHash.get("x");
        matYBrute = (Integer)subBasinHash.get("y");
        if ((matXBrute == null) || (matYBrute == null)) return;
        
        // get integer values
        MatX = matXBrute;
        MatY = matYBrute;
        
        if (MatX >= 0 && MatY >= 0 && MatX < metaData.getNumCols() && MatY < metaData.getNumRows() ) {
            if(isNew){
                String basinLabel="x: "+MatX+", y: "+MatY+" ; Basin Code "+String.valueOf(Math.random()).substring(2);
                localBasinsLog.addNewSubBasin(basinLabel);
                addPreselectedBasin(basinLabel);
            }
            
            // load sub basin, its inlets and reshape area
            SubBasin thisBasin = new SubBasin(MatX, MatY, 
                                              this.fullDirMatrix,
                                              this.metaData);
            thisBasin.addInletIDs(subBasinHash, this.metaData);
            thisBasin.reshapeBasin(this.fullDirMatrix, this.metaData);
            
            visad.DataReferenceImpl riverRef=new visad.DataReferenceImpl("divide");
            Gridded2DSet toProt = thisBasin.getBasinDivide();
            riverRef.setData(toProt);
            visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( 100/255.0f, visad.Display.Red),
                                              new visad.ConstantMap( 100/255.0f, visad.Display.Green),
                                              new visad.ConstantMap( 100/255.0f, visad.Display.Blue),
                                              new visad.ConstantMap( 2.0f, visad.Display.LineWidth)};
            
            //temporalReferences.add(new Object[] {riverRef,lineCMap});

            display.addReference( riverRef , lineCMap);

            // trace SubBasin outlet point
            visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("OutletTuple");
            rtref1.setData(thisBasin.getOutletTuple(this.metaData));
            visad.ConstantMap[] rtmaps1 = { new visad.ConstantMap(1.0, visad.Display.Red ),
                                            new visad.ConstantMap(0, visad.Display.Green ),
                                            new visad.ConstantMap(0, visad.Display.Blue ),
                                            new visad.ConstantMap(7.0, visad.Display.PointSize )};
            display.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
            visad.CellImpl cells1 = new visad.CellImpl() {
                private boolean first = true;
                public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                    if (first) first = false;
                    else {
                        lunchBasinAnalyzer(MatX,MatY);
                    }
                }
            };
            cells1.addReference(rtref1);
            
            basinOutlets.add(new Object[] {rtref1,rtmaps1});
            
        }
    }
    
    protected void tracePolygon(MetaPolygon tracedPoly) throws visad.VisADException, java.rmi.RemoteException{
            DataReferenceImpl riverRef;
            Gridded2DSet toProt;
            
            riverRef = new DataReferenceImpl("divide");
            //riverRef = new DataReferenceImpl("polyPoly_" + tracedPoly.getName());
            toProt = tracedPoly.getPolygon();
            riverRef.setData(toProt);
            visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( 99/255.0f, visad.Display.Red),
                                              new visad.ConstantMap( 99/255.0f, visad.Display.Green),
                                              new visad.ConstantMap( 99/255.0f, visad.Display.Blue),
                                              new visad.ConstantMap( 2.0f, visad.Display.LineWidth)};
            
            temporalReferences.add(new Object[] {riverRef,lineCMap});

            display.addReference( riverRef , lineCMap);
    }
    
    /**
     * This method creates a dataReference for the line describing a river pathway
     * @param MatX The column number of the beginging of the river pathway
     * @param MatY The row number of the beginging of the river pathway
     * @throws java.rmi.RemoteException Captures remote exceptions
     * @throws visad.VisADException Captures VisAD Exeptions
     */
    protected void traceRiverPath(int MatX,int MatY) throws visad.VisADException, java.rmi.RemoteException{
        if (MatX >= 0 && MatY >= 0 && MatX < metaData.getNumCols() && MatY < metaData.getNumRows() ) {

            java.util.Vector thisRiver=new java.util.Vector();
            thisRiver.addElement(new Integer(MatX));
            thisRiver.addElement(new Integer(MatY));

            do {
                int MatXN=MatX-1+(fullDirMatrix[MatY][MatX]-1)%3;
                int MatYN=MatY-1+(fullDirMatrix[MatY][MatX]-1)/3;

                MatX=MatXN; MatY=MatYN;

                thisRiver.addElement(new Integer(MatX));
                thisRiver.addElement(new Integer(MatY));
            } while (fullDirMatrix[MatY][MatX] > 0);

            float[][] RiverPath=new float[2][thisRiver.size()/2];

            for (int j=0;j<thisRiver.size()-1;j+=2){
                
                RiverPath[0][j/2]=(float) (metaData.getMinLon()+metaData.getResLon()/3600.0*(Double.parseDouble(thisRiver.elementAt(j).toString())) + 0.5*metaData.getResLon()/3600.0f);
                RiverPath[1][j/2]=(float) (metaData.getMinLat()+metaData.getResLat()/3600.0*(Double.parseDouble(thisRiver.elementAt(j+1).toString())) + 0.5*metaData.getResLat()/3600.0f);
            }


            visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
            visad.Gridded2DSet ScreenRiverRep=new visad.Gridded2DSet(domain,RiverPath,RiverPath[0].length);

            visad.DataReferenceImpl riverRef=new visad.DataReferenceImpl("rio");
            riverRef.setData(ScreenRiverRep);
            visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( 64/255.0f, visad.Display.Red),
                                              new visad.ConstantMap( 77/255.0f, visad.Display.Green),
                                              new visad.ConstantMap( 160/255.0f, visad.Display.Blue),
                                              new visad.ConstantMap( 1.50f, visad.Display.LineWidth)};
            
            temporalReferences.add(new Object[] {riverRef,lineCMap});

            display.addReference( riverRef , lineCMap);

        }

    }
    
    /**
     * This method assigns the corners of a map section to be writen to a file
     * @param MatX The column number of the corner
     * @param MatY The row number of the corner
     * @throws java.rmi.RemoteException Captures remote exceptions
     * @throws visad.VisADException Captures VisAD Exeptions
     */
    protected void assignSubDataSet(int MatX,int MatY) throws visad.VisADException, java.rmi.RemoteException{
        if (MatX >= 0 && MatY >= 0 && MatX < metaData.getNumCols() && MatY < metaData.getNumRows() ) {

            subSetCornersCounter++;

            if(subSetCornersCounter == 1){
                subSetCorners[0][0]=MatX;
                subSetCorners[0][1]=MatY;
            } else {
                subSetCorners[1][0]=MatX;
                subSetCorners[1][1]=MatY;
                extractSubDataSet();
                subSetCornersCounter=0;
            }
            

        }

    }
    
    private void extractSubDataSet(){
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersDemPath);
            fc.setFileSelectionMode(fc.DIRECTORIES_ONLY);
            fc.setDialogTitle("Select Destination Folder");
            int result = fc.showDialog(this,"Select");
            java.io.File selectedFile = fc.getSelectedFile();

            if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
            
            hydroScalingAPI.io.MetaRaster sectionMR=new hydroScalingAPI.io.MetaRaster(metaData);

            String secMR_Name=selectedFile.getPath()+"/section_"+subSetCorners[0][0]+"_"+subSetCorners[0][1]+"_"+subSetCorners[1][0]+"_"+subSetCorners[1][1]+"_"+metaData.getLocationMeta().getName();
            String secBin_Name=selectedFile.getPath()+"/section_"+subSetCorners[0][0]+"_"+subSetCorners[0][1]+"_"+subSetCorners[1][0]+"_"+subSetCorners[1][1]+"_"+metaData.getLocationBinaryFile().getName();
            
            sectionMR.setLocationMeta(new java.io.File(secMR_Name));
            sectionMR.setLocationBinaryFile(new java.io.File(secBin_Name));
            
            sectionMR.setNumCols(Math.abs(subSetCorners[0][0]-subSetCorners[1][0]));
            sectionMR.setNumRows(Math.abs(subSetCorners[0][1]-subSetCorners[1][1]));
            
            sectionMR.setMinLat(hydroScalingAPI.tools.DegreesToDMS.getprettyString(metaData.getMinLat()+metaData.getResLat()/3600.*(int)Math.min(subSetCorners[0][1],subSetCorners[1][1]),0));
            sectionMR.setMinLon(hydroScalingAPI.tools.DegreesToDMS.getprettyString(metaData.getMinLon()+metaData.getResLon()/3600.*(int)Math.min(subSetCorners[0][0],subSetCorners[1][0]),1));
            
            sectionMR.setFormat("float");
            
            try {
                sectionMR.writeMetaRaster(sectionMR.getLocationMeta());
                java.io.FileOutputStream        salida;
                java.io.DataOutputStream        newfile;
                java.io.BufferedOutputStream    bufferout;

                salida = new java.io.FileOutputStream(sectionMR.getLocationBinaryFile());
                bufferout=new java.io.BufferedOutputStream(salida);
                newfile=new java.io.DataOutputStream(bufferout);

                float[][] matrix=metaData.getArray();
                
                int iniX=(int)Math.min(subSetCorners[0][0],subSetCorners[1][0]);
                int iniY=(int)Math.min(subSetCorners[0][1],subSetCorners[1][1]);
                int lenX=Math.abs(subSetCorners[0][0]-subSetCorners[1][0]);
                int lenY=Math.abs(subSetCorners[0][1]-subSetCorners[1][1]);
                
                for (int j=iniY;j<iniY+lenY;j++) for (int i=iniX;i<iniX+lenX;i++) {
                    newfile.writeFloat(matrix[j][i]);
                }
                newfile.close();
                bufferout.close();
                salida.close();
                
                mainFrame.setUpGUI(true);
            } catch (java.io.IOException exc) {
                System.err.println("Failed writing section of file");
                System.err.println(exc);
            }
            
    }
    
    private void plotTemporaryReferences(){
        for (int i=0;i<temporalReferences.size();i++){
            final int ii=i;
            Runnable addRef = new Runnable() {
                public void run() {
                    try {
                        Object[] refToAdd=(Object[]) temporalReferences.get(ii);
                        display.addReference((visad.DataReferenceImpl)refToAdd[0],(visad.ConstantMap[])refToAdd[1]);
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing temporary references - lines");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing temporary references - lines");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addRef).start();
        }
        
        for (int i=0;i<basinOutlets.size();i++){
            final int ii=i;
            Runnable addPoint = new Runnable() {
                public void run() {
                    try {
                        Object[] refToAdd=(Object[]) basinOutlets.get(ii);
                        display.addReferences(new visad.bom.PickManipulationRendererJ3D(),(visad.DataReferenceImpl)refToAdd[0],(visad.ConstantMap[])refToAdd[1]);
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing temporary references - points");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing temporary references - points");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addPoint).start();
        }
    }
    
    private void plotField(){
        Runnable addField = new Runnable() {
            public void run() {
                try {
                    
                    visad.DataReferenceImpl ref_imaget1 = new visad.DataReferenceImpl("ref_imaget1");
                    ref_imaget1.setData(localField);
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
    
    private void plotNetwork(){
        if(showNetwork.isEnabled()){
            for (int i=2;i<newtorkViewerPopUp.getComponentCount();i++){
                javax.swing.JCheckBoxMenuItem theMenu=(javax.swing.JCheckBoxMenuItem)newtorkViewerPopUp.getComponent(i);
                int orderRequested=Integer.parseInt(theMenu.getText().substring(11,13).trim());
                if (theMenu.isSelected()) addStreamsWithOrder(orderRequested);
            }
        }
    }
    
    private void plotVectors(){
        Object[] actVectors=mainFrame.getActiveVectors();
        for (int i=0;i<actVectors.length;i++){
            final javax.swing.JCheckBox theCheckBox=(javax.swing.JCheckBox)actVectors[i];
            if (theCheckBox.isSelected()){
                if(theCheckBox.getText().lastIndexOf(".shp") != -1){
                    if (mainFrame.getInfoManager().getDataReference("shape"+theCheckBox.getText()) == null){
                        Runnable addVector = new Runnable() {
                            public void run() {
                                try {
                                    double minX=metaData.getMinLon();
                                    double maxX=metaData.getMaxLon();
                                    double minY=metaData.getMinLat();
                                    double maxY=metaData.getMaxLat();
                                    
                                    java.awt.geom.Rectangle2D rect=new java.awt.geom.Rectangle2D.Double(minX-0.5,minY-0.5,maxX-minX+1,maxY-minY+1);
                                    ucar.unidata.gis.shapefile.EsriShapefile esri=new ucar.unidata.gis.shapefile.EsriShapefile(mainFrame.getInfoManager().dataBaseVectorsPath.getPath()+"/"+theCheckBox.getText(),rect);
                                    ucar.visad.ShapefileAdapter shF=new ucar.visad.ShapefileAdapter(esri);
                                    visad.UnionSet toPlot = shF.getData();
                                    if (toPlot != null){
                                        visad.DataReferenceImpl refeElemVec=new visad.DataReferenceImpl("shape"+theCheckBox.getText());
                                        refeElemVec.setData(toPlot);
                                        mainFrame.getInfoManager().registerDataReference("shape"+theCheckBox.getText(), refeElemVec);
                                        display.addReference(refeElemVec);
                                    }
                                } catch (visad.VisADException exc) {
                                    System.err.println("Failed showing shapes");
                                    System.err.println(exc);
                                } catch (java.io.IOException exc) {
                                    System.err.println("Failed showing shapes");
                                    System.err.println(exc);
                                }
                            }
                        };
                        new Thread(addVector).start();
                    } else {
                        Runnable addVector = new Runnable() {
                            public void run() {
                                try {
                                    display.addReference(mainFrame.getInfoManager().getDataReference("shape"+theCheckBox.getText()));
                                } catch (visad.VisADException exc) {
                                    System.err.println("Failed showing shapes");
                                    System.err.println(exc);
                                } catch (java.io.IOException exc) {
                                    System.err.println("Failed showing shapes");
                                    System.err.println(exc);
                                }
                            }
                        };
                        new Thread(addVector).start();
                    }
                }
                if(theCheckBox.getText().lastIndexOf(".dlg") != -1 || theCheckBox.getText().lastIndexOf(".opt") != -1){
                    if (mainFrame.getInfoManager().getDataReference("dlg"+theCheckBox.getText()) == null){
                        Runnable addVector = new Runnable() {
                            public void run() {
                                try {
                                    double minX=metaData.getMinLon();
                                    double maxX=metaData.getMaxLon();
                                    double minY=metaData.getMinLat();
                                    double maxY=metaData.getMaxLat();
                                    
                                    java.awt.geom.Rectangle2D rect=new java.awt.geom.Rectangle2D.Double(minX-0.5,minY-0.5,maxX-minX+1,maxY-minY+1);
                                    hydroScalingAPI.io.MetaDLG myDLG=new hydroScalingAPI.io.MetaDLG(mainFrame.getInfoManager().dataBaseVectorsPath.getPath()+"/"+theCheckBox.getText(),rect);
                                    visad.UnionSet toPlot = myDLG.getUnionSet();
                                    if (toPlot != null){
                                        visad.DataReferenceImpl refeElemVec=new visad.DataReferenceImpl("dlg"+theCheckBox.getText());
                                        refeElemVec.setData(toPlot);
                                        mainFrame.getInfoManager().registerDataReference("dlg"+theCheckBox.getText(), refeElemVec);
                                        display.addReference(refeElemVec);
                                    }
                                } catch (visad.VisADException exc) {
                                    System.err.println("Failed showing dlgs");
                                    System.err.println(exc);
                                } catch (java.io.IOException exc) {
                                    System.err.println("Failed showing dlgs");
                                    System.err.println(exc);
                                }
                            }
                        };
                        new Thread(addVector).start();
                    } else {
                        Runnable addVector = new Runnable() {
                            public void run() {
                                try {
                                    display.addReference(mainFrame.getInfoManager().getDataReference("dlg"+theCheckBox.getText()));
                                } catch (visad.VisADException exc) {
                                    System.err.println("Failed showing dlgs");
                                    System.err.println(exc);
                                } catch (java.io.IOException exc) {
                                    System.err.println("Failed showing dlgs");
                                    System.err.println(exc);
                                }
                            }
                        };
                        new Thread(addVector).start();
                    }
                }
            }
        }
    }
    
    private void plotPolygons(){

        Object[] actPolygons=mainFrame.getActivePolygons();
        for (int i=0;i<actPolygons.length;i++){
            final javax.swing.JCheckBox theCheckBox=(javax.swing.JCheckBox)actPolygons[i];
            if (theCheckBox.isSelected()){
                try{
                    String polygonFilePath = mainFrame.getInfoManager().dataBasePolygonsPath.getPath() + File.separator +theCheckBox.getText();
                    MetaPolygon thePoly=new MetaPolygon(polygonFilePath);
                    this.tracePolygon(thePoly);
                } catch (IOException exp) {
                    System.err.println("IOException: " + exp.getMessage());
                } catch (VisADException exp) {
                    System.err.println("VisADException: " + exp.getMessage());
                }
            }
        }
        
        /*
        Object[] actPolygons=mainFrame.getActivePolygons();
        for (int i=0;i<actPolygons.length;i++){
            final javax.swing.JCheckBox theCheckBox=(javax.swing.JCheckBox)actPolygons[i];
            if (theCheckBox.isSelected()){
                if (mainFrame.getInfoManager().getDataReference("polygon"+theCheckBox.getText()) == null){
                    Runnable addPolygon = new Runnable() {
                        public void run() {
                            try {
                                hydroScalingAPI.io.MetaPolygon thePoly=new hydroScalingAPI.io.MetaPolygon(mainFrame.getInfoManager().dataBasePolygonsPath.getPath()+"/"+theCheckBox.getText());
                                visad.Gridded2DSet toPlot = thePoly.getPolygon();
                                if (toPlot != null){
                                    visad.DataReferenceImpl refeElemVec=new visad.DataReferenceImpl("polygon"+theCheckBox.getText());
                                    refeElemVec.setData(toPlot);
                                    mainFrame.getInfoManager().registerDataReference("polygon"+theCheckBox.getText(), refeElemVec);
                                    
                                    visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( 75/255.0f, visad.Display.Red),
                                                                      new visad.ConstantMap( 150/255.0f, visad.Display.Green),
                                                                      new visad.ConstantMap( 75/255.0f, visad.Display.Blue),
                                                                      new visad.ConstantMap( 2.0f, visad.Display.LineWidth)};
            
                                    temporalReferences.add(new Object[] {refeElemVec,lineCMap});

                                    display.addReference(refeElemVec , lineCMap);
                                    
                                    //display.addReference(refeElemVec);
                                }
                            } catch (visad.VisADException exc) {
                                System.err.println("Failed showing polygons");
                                System.err.println(exc);
                            } catch (java.io.IOException exc) {
                                System.err.println("Failed showing polygons");
                                System.err.println(exc);
                            }
                        }
                    };
                    new Thread(addPolygon).start();
                } else {
                    Runnable addVector = new Runnable() {
                        public void run() {
                            try {
                                display.addReference(mainFrame.getInfoManager().getDataReference("polygon"+theCheckBox.getText()));
                            } catch (visad.VisADException exc) {
                                System.err.println("Failed showing polygons");
                                System.err.println(exc);
                            } catch (java.io.IOException exc) {
                                System.err.println("Failed showing polygons");
                                System.err.println(exc);
                            }
                        }
                    };
                    new Thread(addVector).start();
                }
            }
        }
        */
    }
    
    private void plotGauges(final boolean withNames){
        Object[] actGauges = mainFrame.getActiveGauges().toArray(); // TODO - check if correct
        
        for (int i=0;i<actGauges.length;i++){
            
            final hydroScalingAPI.io.MetaGauge theGauge=(hydroScalingAPI.io.MetaGauge) actGauges[i];
            Runnable addPoint = new Runnable() {
                public void run() {
                    try {
                        visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("RealTuples");
                        rtref1.setData(theGauge.getPositionTuple());
                        visad.ConstantMap[] rtmaps1 = {new visad.ConstantMap(7.0, visad.Display.PointSize )};
                        display.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
                        visad.CellImpl cells1 = new visad.CellImpl() {
                            private boolean first = true;
                            public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                                if (first) first = false;
                                else {
                                    gaugeAction(theGauge);
                                }
                            }
                        };
                        cells1.addReference(rtref1);
                        if(withNames){
                            visad.DataReferenceImpl tref = new visad.DataReferenceImpl("text");
                            tref.setData(theGauge.getTextTuple());
                            display.addReference(tref);
                        }
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
    
    private void plotLocations(final boolean withNames){
        Object[] actLocations = mainFrame.getActiveLocations().toArray(); // TODO - check if correct
        
        for (int i=0;i<actLocations.length;i++){
            
            final hydroScalingAPI.io.MetaLocation theLocation=(hydroScalingAPI.io.MetaLocation) actLocations[i];
            Runnable addPoint = new Runnable() {
                public void run() {
                    try {
                        visad.DataReferenceImpl rtref1 = new visad.DataReferenceImpl("RealTuples");
                        rtref1.setData(theLocation.getPositionTuple());
                        double grey=1.0;
                        grey=theLocation.hasImages()?0.9:0.6;
                        visad.ConstantMap[] rtmaps1 = { new visad.ConstantMap(grey, visad.Display.Red ),
                                new visad.ConstantMap(grey, visad.Display.Green ),
                                new visad.ConstantMap(grey, visad.Display.Blue ),
                                new visad.ConstantMap(5.0, visad.Display.PointSize )};
                                display.addReferences(new visad.bom.PickManipulationRendererJ3D(), rtref1, rtmaps1);
                                visad.CellImpl cells1 = new visad.CellImpl() {
                                    private boolean first = true;
                                    public void doAction() throws visad.VisADException, java.rmi.RemoteException {
                                        if (first) first = false;
                                        else {
                                            locationAction(theLocation);
                                        }
                                    }
                                };
                                cells1.addReference(rtref1);
                                
                                if(withNames){
                                    visad.DataReferenceImpl tref = new visad.DataReferenceImpl("text");
                                    tref.setData(theLocation.getTextTuple());
                                    display.addReference(tref);
                                }
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing locations");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing locations");
                        System.err.println(exc);
                    }
                }
            };
            new Thread(addPoint).start();
        }
    }
    
    /**
     * A method to externaly trigger an update of all the references in the map
     * @param gaugesWithNames A boolean flag indicating if the gauge points must have a label with the code
     * @param locationsWithNames A boolean flag indicating if the locations points must have a label with the code
     */
    public void refreshReferences(final boolean gaugesWithNames, final boolean locationsWithNames){
        try{
            display.removeAllReferences();
        } catch (visad.VisADException exc) {
            System.err.println(exc);
        } catch (java.rmi.RemoteException rmi){
            System.err.println(rmi);
        }
        plotField();
        if(showVectorLayer.isSelected()) plotVectors();
        if(showPolygonsLayer.isSelected()) plotPolygons();
        if(showSitesLayer.isSelected()) {
            plotGauges(gaugesWithNames);
            plotLocations(locationsWithNames);
        }
        if(showNetwork.isEnabled()) plotNetwork();
        
        plotTemporaryReferences();
    }
    
    private void updateRelatedMaps(){
        
        Object[] mapNames=relatedMapsList.keySet().toArray();
        java.util.Arrays.sort(mapNames);
        int currentMap=0;
        for (int i=0;i<mapNames.length;i++){
            if ((mapNames[i].toString()).lastIndexOf(metaData.getName()) != -1) currentMap=i;
            relatedMaps.addItem(mapNames[i]);
        }
        relatedMaps.setSelectedIndex(currentMap);
        
        relatedMaps.addItemListener(new java.awt.event.ItemListener() {
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                relatedMapsItemStateChanged(evt);
            }
        });
    }
    
    /**
     * Sets the value in the latitude label
     * @param latLabel The value for the label
     */
    protected void setLatitudeLabel(String latLabel){
        latitudeLabel.setText(latLabel);
    }
    /**
     * Sets the value in the longitude label
     * @param lonLabel The value for the label
     */
    protected void setLongitudeLabel(String lonLabel){
        longitudeLabel.setText(lonLabel);
    }
    
    /**
     * Sets the value in the Value label
     * @param valLabel The value for the label
     */
    protected void setValueLabel(String valLabel){
        valueLabel.setText(valLabel);
    }
    
    private void gaugeAction(hydroScalingAPI.io.MetaGauge gaugeInfo){
        hydroScalingAPI.modules.analysis_TS.widgets.TimeSeriesViewer gauView=new hydroScalingAPI.modules.analysis_TS.widgets.TimeSeriesViewer(mainFrame,gaugeInfo);
        gauView.setVisible(true);
    }
    
    private void locationAction(hydroScalingAPI.io.MetaLocation locationInfo){
        hydroScalingAPI.subGUIs.widgets.LocationsViewer locView=new hydroScalingAPI.subGUIs.widgets.LocationsViewer(mainFrame,locationInfo);
        locView.setVisible(true);
    }
    
    private void lunchBasinAnalyzer(int MatX, int MatY){
        
        new hydroScalingAPI.subGUIs.widgets.NetworkTools(mainFrame,MatX,MatY,fullDirMatrix,metaData).setVisible(true);
        display.reDisplayAll();
        
    }
    
    /**
     * A method to externaly assign a unique identifier for the RasterViewer
     * @param windowIdentifier The unique code
     */
    public void setIdentifier(String windowIdentifier){
        myStringID=windowIdentifier;
    }
    
    /**
     * Returns the unique identifier assigned to this RasterViewer
     * @return The unique identifier
     */
    public String getIdentifier(){
        return myStringID;
    }
    
    private void addToNetworkPupUp(java.awt.Component item){
        newtorkViewerPopUp.add(item);
    }
    
    private void selectAllNetworkPopup(){
        javax.swing.JCheckBoxMenuItem selectAllMenu=(javax.swing.JCheckBoxMenuItem)newtorkViewerPopUp.getComponent(0);
        for (int i=2;i<newtorkViewerPopUp.getComponentCount();i++){
            javax.swing.JCheckBoxMenuItem theMenu=(javax.swing.JCheckBoxMenuItem)newtorkViewerPopUp.getComponent(i);
            theMenu.setSelected(selectAllMenu.isSelected());
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newtorkViewerPopUp = new javax.swing.JPopupMenu();
        basinsViewerPopUp = new javax.swing.JPopupMenu();
        editLog = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        toolsPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        visualToolbar = new javax.swing.JToolBar();
        restoreDisplay = new javax.swing.JButton();
        zoomInDisplay = new javax.swing.JButton();
        zoomOutDisplay = new javax.swing.JButton();
        snapshot = new javax.swing.JButton();
        print = new javax.swing.JButton();
        showControls = new javax.swing.JButton();
        demToolbar = new javax.swing.JToolBar();
        showNetwork = new javax.swing.JButton();
        showBasins = new javax.swing.JButton();
        selectOutlet = new javax.swing.JToggleButton();
        traceStream = new javax.swing.JToggleButton();
        locationToolbar = new javax.swing.JToolBar();
        createLocation = new javax.swing.JToggleButton();
        cutSubDataSet = new javax.swing.JToggleButton();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        relatedMaps = new javax.swing.JComboBox();
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
        jLabel2 = new javax.swing.JLabel();
        showVectorLayer = new javax.swing.JCheckBox();
        showLinesLayer = new javax.swing.JCheckBox();
        showPolygonsLayer = new javax.swing.JCheckBox();
        showSitesLayer = new javax.swing.JCheckBox();

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

        jPanel3.setLayout(new java.awt.GridLayout(1, 2));

        jPanel7.setMinimumSize(new java.awt.Dimension(18, 14));
        jPanel7.setPreferredSize(new java.awt.Dimension(140, 22));
        jPanel7.setLayout(new javax.swing.BoxLayout(jPanel7, javax.swing.BoxLayout.LINE_AXIS));

        visualToolbar.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        visualToolbar.setPreferredSize(new java.awt.Dimension(130, 22));

        restoreDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Home16.gif"))); // NOI18N
        restoreDisplay.setToolTipText("Restore");
        restoreDisplay.setMaximumSize(new java.awt.Dimension(18, 18));
        restoreDisplay.setMinimumSize(new java.awt.Dimension(18, 18));
        restoreDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                restoreDisplayActionPerformed(evt);
            }
        });
        visualToolbar.add(restoreDisplay);

        zoomInDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/ZoomIn16.gif"))); // NOI18N
        zoomInDisplay.setToolTipText("Zoom In");
        zoomInDisplay.setMaximumSize(new java.awt.Dimension(18, 18));
        zoomInDisplay.setMinimumSize(new java.awt.Dimension(18, 18));
        zoomInDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomInDisplayActionPerformed(evt);
            }
        });
        visualToolbar.add(zoomInDisplay);

        zoomOutDisplay.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/ZoomOut16.gif"))); // NOI18N
        zoomOutDisplay.setToolTipText("Zoom Out");
        zoomOutDisplay.setMaximumSize(new java.awt.Dimension(18, 18));
        zoomOutDisplay.setMinimumSize(new java.awt.Dimension(18, 18));
        zoomOutDisplay.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                zoomOutDisplayActionPerformed(evt);
            }
        });
        visualToolbar.add(zoomOutDisplay);

        snapshot.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Snapshot.gif"))); // NOI18N
        snapshot.setToolTipText("Save JPG ...");
        snapshot.setMaximumSize(new java.awt.Dimension(18, 18));
        snapshot.setMinimumSize(new java.awt.Dimension(18, 18));
        snapshot.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                snapshotActionPerformed(evt);
            }
        });
        visualToolbar.add(snapshot);

        print.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Print.gif"))); // NOI18N
        print.setToolTipText("Print ...");
        print.setMaximumSize(new java.awt.Dimension(18, 18));
        print.setMinimumSize(new java.awt.Dimension(18, 18));
        print.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printActionPerformed(evt);
            }
        });
        visualToolbar.add(print);

        showControls.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/ControlPanel.gif"))); // NOI18N
        showControls.setToolTipText("Control Panel");
        showControls.setMaximumSize(new java.awt.Dimension(18, 18));
        showControls.setMinimumSize(new java.awt.Dimension(18, 18));
        showControls.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showControlsActionPerformed(evt);
            }
        });
        visualToolbar.add(showControls);

        jPanel7.add(visualToolbar);

        demToolbar.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        demToolbar.setPreferredSize(new java.awt.Dimension(130, 22));

        showNetwork.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Network.gif"))); // NOI18N
        showNetwork.setToolTipText("Plot Network");
        showNetwork.setMaximumSize(new java.awt.Dimension(18, 18));
        showNetwork.setMinimumSize(new java.awt.Dimension(18, 18));
        showNetwork.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNetworkActionPerformed(evt);
            }
        });
        demToolbar.add(showNetwork);

        showBasins.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Predef_Outlet.gif"))); // NOI18N
        showBasins.setToolTipText("Predefinded Outlets");
        showBasins.setMaximumSize(new java.awt.Dimension(18, 18));
        showBasins.setMinimumSize(new java.awt.Dimension(18, 18));
        showBasins.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBasinsActionPerformed(evt);
            }
        });
        demToolbar.add(showBasins);

        selectOutlet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/Outlet.gif"))); // NOI18N
        selectOutlet.setToolTipText("Select outlet");
        selectOutlet.setMaximumSize(new java.awt.Dimension(18, 18));
        selectOutlet.setMinimumSize(new java.awt.Dimension(18, 18));
        selectOutlet.setPreferredSize(new java.awt.Dimension(18, 18));
        selectOutlet.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/OutletActive.gif"))); // NOI18N
        selectOutlet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectOutletActionPerformed(evt);
            }
        });
        demToolbar.add(selectOutlet);

        traceStream.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/TraceRiver.gif"))); // NOI18N
        traceStream.setToolTipText("Trace River");
        traceStream.setMaximumSize(new java.awt.Dimension(18, 18));
        traceStream.setMinimumSize(new java.awt.Dimension(18, 18));
        traceStream.setPreferredSize(new java.awt.Dimension(18, 18));
        traceStream.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/TraceRiverActive.gif"))); // NOI18N
        traceStream.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                traceStreamActionPerformed(evt);
            }
        });
        demToolbar.add(traceStream);

        jPanel7.add(demToolbar);

        locationToolbar.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        locationToolbar.setPreferredSize(new java.awt.Dimension(130, 22));

        createLocation.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/NewLocation.gif"))); // NOI18N
        createLocation.setToolTipText("Create New Location");
        createLocation.setMaximumSize(new java.awt.Dimension(18, 18));
        createLocation.setMinimumSize(new java.awt.Dimension(18, 18));
        createLocation.setPreferredSize(new java.awt.Dimension(18, 18));
        createLocation.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/NewLocationActive.gif"))); // NOI18N
        createLocation.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createLocationActionPerformed(evt);
            }
        });
        locationToolbar.add(createLocation);

        cutSubDataSet.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/CutSubDem.gif"))); // NOI18N
        cutSubDataSet.setToolTipText("Cut Map Section");
        cutSubDataSet.setMaximumSize(new java.awt.Dimension(18, 18));
        cutSubDataSet.setMinimumSize(new java.awt.Dimension(18, 18));
        cutSubDataSet.setPreferredSize(new java.awt.Dimension(18, 18));
        cutSubDataSet.setSelectedIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/CutSubDemActive.gif"))); // NOI18N
        cutSubDataSet.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cutSubDataSetActionPerformed(evt);
            }
        });
        locationToolbar.add(cutSubDataSet);

        jPanel7.add(locationToolbar);

        jPanel3.add(jPanel7);

        jPanel4.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel1.setText("Switch to : ");
        jPanel4.add(jLabel1, java.awt.BorderLayout.WEST);

        relatedMaps.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        relatedMaps.setLightWeightPopupEnabled(false);
        relatedMaps.setMaximumSize(new java.awt.Dimension(32767, 18));
        relatedMaps.setMinimumSize(new java.awt.Dimension(32, 18));
        relatedMaps.setPreferredSize(new java.awt.Dimension(32, 18));
        jPanel4.add(relatedMaps, java.awt.BorderLayout.CENTER);

        jPanel3.add(jPanel4);

        toolsPanel.add(jPanel3);

        jPanel2.setLayout(new java.awt.GridLayout(1, 3));

        jPanel6.setLayout(new java.awt.BorderLayout());

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel3.setText("Latitude : ");
        jPanel6.add(jLabel3, java.awt.BorderLayout.WEST);

        latitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        latitudeLabel.setText("00:00:00.00 N [000]");
        jPanel6.add(latitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel6);

        jPanel61.setLayout(new java.awt.BorderLayout());

        jLabel31.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel31.setText("Longitude : ");
        jPanel61.add(jLabel31, java.awt.BorderLayout.WEST);

        longitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        longitudeLabel.setText("00:00:00.00 W [000]");
        jPanel61.add(longitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel61);

        jPanel62.setLayout(new java.awt.BorderLayout());

        jLabel323.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel323.setText("Value : ");
        jPanel62.add(jLabel323, java.awt.BorderLayout.WEST);

        valueLabel.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        valueLabel.setText("0000");
        jPanel62.add(valueLabel, java.awt.BorderLayout.CENTER);

        jPanel2.add(jPanel62);

        toolsPanel.add(jPanel2);

        getContentPane().add(toolsPanel, java.awt.BorderLayout.NORTH);

        layersPanel.setLayout(new java.awt.GridLayout(1, 5));

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel2.setText("Active Layers: ");
        layersPanel.add(jLabel2);

        showVectorLayer.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        showVectorLayer.setSelected(true);
        showVectorLayer.setText("Vectors");
        showVectorLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showVectorLayerActionPerformed(evt);
            }
        });
        layersPanel.add(showVectorLayer);

        showLinesLayer.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        showLinesLayer.setSelected(true);
        showLinesLayer.setText("Lines");
        showLinesLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showLinesLayerActionPerformed(evt);
            }
        });
        layersPanel.add(showLinesLayer);

        showPolygonsLayer.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        showPolygonsLayer.setSelected(true);
        showPolygonsLayer.setText("Polygons");
        showPolygonsLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showPolygonsLayerActionPerformed(evt);
            }
        });
        layersPanel.add(showPolygonsLayer);

        showSitesLayer.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        showSitesLayer.setSelected(true);
        showSitesLayer.setText("Sites");
        showSitesLayer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showSitesLayerActionPerformed(evt);
            }
        });
        layersPanel.add(showSitesLayer);

        getContentPane().add(layersPanel, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void cutSubDataSetActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cutSubDataSetActionPerformed
    if(cutSubDataSet.isSelected()) {
            selectOutlet.setSelected(false);
            traceStream.setSelected(false);
            createLocation.setSelected(false);
            activeEvent=4;
            subSetCornersCounter=0;
        } else activeEvent=0;
    }//GEN-LAST:event_cutSubDataSetActionPerformed

    private void createLocationActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createLocationActionPerformed
    if(createLocation.isSelected()) {
            selectOutlet.setSelected(false);
            traceStream.setSelected(false);
            cutSubDataSet.setSelected(false);
            activeEvent=3;
        } else activeEvent=0;
    }//GEN-LAST:event_createLocationActionPerformed

    private void showBasinsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showBasinsActionPerformed
        javax.swing.JButton theButton= (javax.swing.JButton)evt.getSource();
        basinsViewerPopUp.show(theButton, 0,theButton.getHeight());
    }//GEN-LAST:event_showBasinsActionPerformed

    private void traceStreamActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_traceStreamActionPerformed
        if(traceStream.isSelected()) {
            selectOutlet.setSelected(false);
            createLocation.setSelected(false);
            cutSubDataSet.setSelected(false);
            activeEvent=2;
        } else activeEvent=0;
    }//GEN-LAST:event_traceStreamActionPerformed

    private void selectOutletActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_selectOutletActionPerformed
        if(selectOutlet.isSelected()) {
            traceStream.setSelected(false);
            createLocation.setSelected(false);
            cutSubDataSet.setSelected(false);
            activeEvent=1; 
        } else activeEvent=0;
    }//GEN-LAST:event_selectOutletActionPerformed
    
    private void printActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printActionPerformed
        Thread t = new Thread() {
            public void run() {
                
                java.awt.print.PrinterJob printJob = java.awt.print.PrinterJob.getPrinterJob();
                printJob.setPrintable(display.getPrintable());
                if(printJob.printDialog()){
                    try {
                        printJob.print();
                    }
                    catch (Exception pe) {
                        pe.printStackTrace();
                    }
                }
            }
        };
        
        t.start();
        
    }//GEN-LAST:event_printActionPerformed
    
    private void showSitesLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showSitesLayerActionPerformed
        refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
    }//GEN-LAST:event_showSitesLayerActionPerformed
    
    private void showPolygonsLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showPolygonsLayerActionPerformed
        refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
    }//GEN-LAST:event_showPolygonsLayerActionPerformed
    
    private void showLinesLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showLinesLayerActionPerformed
        refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
    }//GEN-LAST:event_showLinesLayerActionPerformed
    
    private void showVectorLayerActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showVectorLayerActionPerformed
        refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
    }//GEN-LAST:event_showVectorLayerActionPerformed
    
    private void showNetworkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNetworkActionPerformed
        javax.swing.JButton theButton= (javax.swing.JButton)evt.getSource();
        newtorkViewerPopUp.show(theButton, 0,theButton.getHeight());
    }//GEN-LAST:event_showNetworkActionPerformed
    
    private void showControlsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showControlsActionPerformed
        new hydroScalingAPI.subGUIs.widgets.RasterViewerControlsDialog(mainFrame,latitudeMap,longitudeMap,heightMap,colorScaleMap).setVisible(true);
    }//GEN-LAST:event_showControlsActionPerformed
        
    private void snapshotActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_snapshotActionPerformed
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser();
        javax.swing.filechooser.FileFilter jpgFilter = new visad.util.ExtensionFileFilter("jpg","JPEG File");
        fc.addChoosableFileFilter(jpgFilter);
        fc.showSaveDialog(this);
        
        if(fc.getSelectedFile() == null) return;
        
        final java.io.File f=fc.getSelectedFile();
        Runnable captureImage = new Runnable() {
            public void run() {
                String msg = "Could not save image snapshot to file \"" + f.getName() +
                "\" in JPEG format. ";
                try {
                    saveImage(f);
                }
                catch (visad.VisADException exc) {
                    msg = msg + "An error occurred: " + exc.getMessage();
                    javax.swing.JOptionPane.showMessageDialog(mainFrame, msg, "Error saving data",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                }
                catch (java.io.IOException exc) {
                    msg = msg + "An I/O error occurred: " + exc.getMessage();
                    javax.swing.JOptionPane.showMessageDialog(mainFrame, msg, "Error saving data",
                    javax.swing.JOptionPane.ERROR_MESSAGE);
                }
            }
        };
        Thread t = new Thread(captureImage);
        t.start();
    }//GEN-LAST:event_snapshotActionPerformed
    
    private void zoomOutDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomOutDisplayActionPerformed
        try{
            visad.ProjectionControl pc = display.getProjectionControl();
            double[] scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                                                                     0.5, 0.5, 0.5,
                                                                     0.0, 0.0, 0.0);
            double[] currentMatrix = pc.getMatrix();
            scaleMatrix = dr.getMouseBehavior().multiply_matrix(scaleMatrix,currentMatrix);

            

            pc.setMatrix(scaleMatrix);
        } catch (visad.VisADException v){
            System.err.println(v);
        } catch (java.rmi.RemoteException r){
            System.err.println(r);
        }
    }//GEN-LAST:event_zoomOutDisplayActionPerformed
    
    private void zoomInDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_zoomInDisplayActionPerformed
        try{
            
            visad.ProjectionControl pc = display.getProjectionControl();
            double[] scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                                                                     2.0, 2.0, 2.0,
                                                                     0.0, 0.0, 0.0);
            double[] currentMatrix = pc.getMatrix();
            scaleMatrix = dr.getMouseBehavior().multiply_matrix(scaleMatrix,currentMatrix);

            

            pc.setMatrix(scaleMatrix);
        } catch (visad.VisADException v){
            System.err.println(v);
        } catch (java.rmi.RemoteException r){
            System.err.println(r);
        }
    }//GEN-LAST:event_zoomInDisplayActionPerformed
    
    private void restoreDisplayActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_restoreDisplayActionPerformed
        try{
            display.getProjectionControl().resetProjection();
        } catch (visad.VisADException v){
            System.err.println(v);
        } catch (java.rmi.RemoteException r){
            System.err.println(r);
        }
    }//GEN-LAST:event_restoreDisplayActionPerformed
    
    private void relatedMapsItemStateChanged(java.awt.event.ItemEvent evt) {

        if (evt.getStateChange() == 1){
            Object requestedMap =evt.getItem();
            if(!(requestedMap.toString().indexOf("Map Not Available") != -1)){
                java.io.File fileToLoad=new java.io.File((String)relatedMapsList.get(requestedMap));
                if (!fileToLoad.equals(metaData.getLocationBinaryFile())){
                    String extension=fileToLoad.getName();
                    extension=extension.substring(extension.lastIndexOf("."));
                    metaData.setLocationBinaryFile(fileToLoad);
                    if (metaData.getLocationBinaryFile().getName().lastIndexOf(".dem") != -1 || metaData.getLocationBinaryFile().getName().lastIndexOf(".vhc") != -1){
                        metaData.restoreOriginalFormat();
                    } else{
                        metaData.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(extension));
                    }
                    try{
                        localField=metaData.getField();
                        float[][] dataValues=localField.getFloats();
                        if(heightMap != null) {
                            hydroScalingAPI.util.statistics.Stats statsVar=new hydroScalingAPI.util.statistics.Stats(dataValues[0]);
                            heightMap.setRange(statsVar.minValue,statsVar.maxValue+2*(statsVar.maxValue-statsVar.minValue));
                        }
                    } catch (visad.VisADException ve){
                        System.err.println("Failed loading field");
                        System.err.println(ve);
                    }  catch (java.io.IOException ioe){
                        System.err.println("Failed loading field");
                        System.err.println(ioe);
                    }
                    
                    try {
                    
                        //If DEM load the Elevations Color Table
                       hydroScalingAPI.subGUIs.widgets.RasterPalettesManager availablePalettes=new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(colorScaleMap);

                       if (metaData.getLocationBinaryFile().getName().lastIndexOf(".dem") != -1 | metaData.getLocationBinaryFile().getName().lastIndexOf(".corrDEM") != -1)
                           availablePalettes.setSelectedTable("Elevations");
                       else
                           availablePalettes.setSelectedTable("Rainbow");
                    } catch (visad.VisADException exc) {
                        System.err.println("Failed showing the field");
                        System.err.println(exc);
                    } catch (java.io.IOException exc) {
                        System.err.println("Failed showing the field");
                        System.err.println(exc);
                    }

                    refreshReferences(mainFrame.nameOnGauges(),mainFrame.nameOnLocations());
                }
            }

        }

    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPopupMenu basinsViewerPopUp;
    private javax.swing.JToggleButton createLocation;
    private javax.swing.JToggleButton cutSubDataSet;
    private javax.swing.JToolBar demToolbar;
    private javax.swing.JMenuItem editLog;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel323;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JPanel layersPanel;
    private javax.swing.JToolBar locationToolbar;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.JPopupMenu newtorkViewerPopUp;
    private javax.swing.JButton print;
    private javax.swing.JComboBox relatedMaps;
    private javax.swing.JButton restoreDisplay;
    private javax.swing.JToggleButton selectOutlet;
    private javax.swing.JButton showBasins;
    private javax.swing.JButton showControls;
    private javax.swing.JCheckBox showLinesLayer;
    private javax.swing.JButton showNetwork;
    private javax.swing.JCheckBox showPolygonsLayer;
    private javax.swing.JCheckBox showSitesLayer;
    private javax.swing.JCheckBox showVectorLayer;
    private javax.swing.JButton snapshot;
    private javax.swing.JPanel toolsPanel;
    private javax.swing.JToggleButton traceStream;
    private javax.swing.JLabel valueLabel;
    private javax.swing.JToolBar visualToolbar;
    private javax.swing.JButton zoomInDisplay;
    private javax.swing.JButton zoomOutDisplay;
    // End of variables declaration//GEN-END:variables
    
}
