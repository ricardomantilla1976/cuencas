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
 * BasinAnalyzer.java
 *
 * Created on June 17, 2003, 10:20 AM
 */

package hydroScalingAPI.modules.networkAnalysis.widgets;

import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;
import java.io.IOException;

/**
 * Creates the Network Analysis Module.  This is one of the most important
 * modules.  It includes novel geomorphic analysis of the river network that are
 * the result of reaserch by Ricardo Mantilla.
 * @author Ricardo Mantilla
 */
public class BasinAnalyzer extends javax.swing.JDialog implements visad.DisplayListener {
    
    private float[] red;
    private float[] green;
    private float[] blue;
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    
    private hydroScalingAPI.util.geomorphology.objects.Basin myCuenca;
    
    public hydroScalingAPI.io.MetaRaster metaDatos;
    public byte[][] matDir;
    private byte[][] netMask;
    public hydroScalingAPI.util.geomorphology.objects.HortonAnalysis myHortonStructure;
    public hydroScalingAPI.util.geomorphology.objects.LinksAnalysis myLinksStructure;
    public hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRSNAnalysis;
    
    private String[] itemsToAnalize={   "Link's Hillslope Area [km^2]",
                                        "Link's Length [km]",
                                        "Upstream Drainage Area [km^2]",
                                        "Link's Drop [m]",
                                        "Link's Order [*]",
                                        "Upstream Network Length [km^2]",
                                        "Link's Magnitude [*]",
                                        "Distance to Outlet [km]",
                                        "Topologic Distance to Outlet [*]",
                                        "Link's Slope [*]",
                                        "Link's Absolute Elevation [m]",
                                        "Longest Channel Length [km]",
                                        "Binary Link Address",
                                        "Total Channel Drop",
                                        "Upstream area at links head [km^2]"};
        
        
    
    private boolean[] firstTouchTab=new boolean[9];
    
    
    private RealType    numLinks= RealType.getRealType("numLinks") ,
                        distanceToOut = RealType.getRealType("distanceToOut"),
                        streamOrder= RealType.getRealType("streamOrder"),
                        hortonVar= RealType.getRealType("hortonVar"),
                        index=RealType.getRealType("index"),
                        exedProbab=RealType.getRealType("exedProbab"),
                        varRaster=RealType.getRealType("varRaster"),
                        posVarArray=RealType.getRealType("posVarArray"),
                        bins=RealType.getRealType("bins"),
                        frequencies=RealType.getRealType("frequencies"),
                        x_var_index =RealType.getRealType("x_var_index"),
                        x_var_link =RealType.getRealType("x_var_link"),
                        y_var_link=RealType.getRealType("y_var_link"),
                        dotColor=RealType.getRealType("dotColor"),
                        logAreaIndex=RealType.getRealType("logAreaIndex"),
                        logAreaValue=RealType.getRealType("logAreaValue"),
                        drainageDensity=RealType.getRealType("drainageDensity"),
                        percentAreaValue=RealType.getRealType("percentAreaValue"),
                        elevationValue=RealType.getRealType("elevationValue");
    
    private RealTupleType espacioHorton= new RealTupleType(streamOrder,hortonVar),
                          espacioDD= new RealTupleType(logAreaValue,drainageDensity),
                          espacioXLYL=new RealTupleType(new RealType[] {x_var_link,y_var_link,dotColor});
    
    private FunctionType    func_distanceToOut_numLinks= new FunctionType(distanceToOut, numLinks),
                            func_order_hortonVar= new FunctionType(streamOrder, hortonVar),
                            func_index_hortonVar= new FunctionType(index, espacioHorton),
                            func_exProb_hortonVarOrder=new FunctionType(exedProbab, posVarArray),
                            func_bins_frequencies=new FunctionType(bins, frequencies),
                            func_xVarLink_yVarLink=new FunctionType(x_var_index, espacioXLYL),
                            func_logArea_drainageDensity_Lines=new FunctionType(logAreaValue, drainageDensity),
                            func_logArea_drainageDensity=new FunctionType(logAreaIndex, espacioDD),
                            func_elePer_eleVal=new FunctionType(percentAreaValue, elevationValue);
    
    
    private DisplayImpl displayW,
                        displayH,
                        displayCDF,
                        display_Hortonian_Means,
                        display_Hortonian_Dists,
                        displayMap_Hortonian,
                        displayMap_RSNs,
                        display_Links,
                        displayDD,
                        displayHC;
    
    private FlatField vals_ff_W, vals_ff_H, vals_ff_Li, vals_ff_DD,vals_ff_HC;
    
    private ScalarMap   disMap,
                        numLinkMap,
                        orderMap,
                        horVarMap,
                        horFitMap,
                        exedProbabMap,
                        orderMap_Hortonian,
                        horFitMap_Hortonian,
                        horVarMap_Hortonian,
                        exedProbabMap_Hortonian,
                        lonMapRSN,
                        latMapRSN,
                        varMapRSN,
                        lonMapHortonian,
                        latMapHortonian,
                        varMapHortonian,
                        posVarArrayMap,
                        posVarArrayMap_Hortonian,
                        binsMap,
                        freqMap,
                        xVarMap,
                        yVarMap,
                        dotColorMap,
                        yVarValueMap,
                        logAreaMap,
                        ddMap,
                        ddValueMap,
                        percentAreaMap,
                        elevationMap;
    
    private DataReferenceImpl data_refW, data_refH,data_ref_varRaster, data_refLi, data_refDD,data_ref_RSNTiles,data_refHC;
    
    private DataReferenceImpl[] data_refHDis,
                                data_refHDis_Hortonian,
                                data_refHDd,
                                refeElemVec;
    
    public DataReferenceImpl[] data_refSubBasins;
    
    private javax.swing.JCheckBox[] checkBoxperOrder0,checkBoxperOrder1,checkBoxperOrder2;
    
    //Data Varaibles
    
    private float binsizeWidth=1.0f, binsizeHistogram=0.05f;
    
    public float[] branchingValues;
    public float[][] geomLengthDist,
                     topolLengthDist,
                     areasDist,
                     magnDist,
                     mainGeomLengthDist,
                     mainTopolLengthDist,
                     totalGeomLengthDist,
                     spatialVarDist,
                     totalDropDist,
                     slopesDist,
                     maxDropDist,
                     streamDropDist;
    
    private int[][] matrizPintada;
    
    private float minXVarValue,maxXVarValue,minYVarValue,maxYVarValue;
    private float minXVarValueCurrent,maxXVarValueCurrent,minYVarValueCurrent,maxYVarValueCurrent;
    
    /**
     * Creates new form basinAnalyzer
     * @param parent The master GUI that launches the Module
     * @param x The x location of the basin outlet
     * @param y The x location of the basin outlet
     * @param direcc The direction Matrix associated with this basin
     * @param md The meta information
     * @throws java.rmi.RemoteException Captures remote exceptions
     * @throws visad.VisADException Captures VisAD Exeptions
     * @throws java.io.IOException Captures I/O Execptions
     */
    public BasinAnalyzer(hydroScalingAPI.mainGUI.ParentGUI parent, int x, int y, byte[][] direcc, hydroScalingAPI.io.MetaRaster md) throws RemoteException, VisADException, java.io.IOException{
        
        //elementos de la interfaz y del modulo
        super(parent, true);
        mainFrame=parent;
        matDir=direcc;
        metaDatos=md;
        
        red=    mainFrame.getInfoManager().getNetworkRed();
        green=  mainFrame.getInfoManager().getNetworkGreen();
        blue=   mainFrame.getInfoManager().getNetworkBlue();
        
        initComponents();
        pack();
        
        setBounds(0,0, 950, 700);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,direcc,metaDatos);
        
        netMask=myCuenca.getBasinMask();
        
        myHortonStructure=new hydroScalingAPI.util.geomorphology.objects.HortonAnalysis(myCuenca, metaDatos, matDir);
        
        myLinksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        minRegOrderSlider.setMinimum(1);
        minRegOrderSlider.setMaximum(myHortonStructure.getBasinOrder());
        minRegOrderSlider.setValue(1);
        minRegOrderLabel.setText(""+1);
        
        maxRegOrderSlider.setMinimum(1);
        maxRegOrderSlider.setMaximum(myHortonStructure.getBasinOrder());
        maxRegOrderSlider.setValue(myHortonStructure.getBasinOrder());
        maxRegOrderLabel.setText(""+myHortonStructure.getBasinOrder());
        
        minRegOrderHortonianSlider.setMinimum(1);
        minRegOrderHortonianSlider.setMaximum(myHortonStructure.getBasinOrder());
        minRegOrderHortonianSlider.setValue(1);
        minRegOrderHortonianLabel.setText(""+1);
        
        maxRegOrderHortonianSlider.setMinimum(1);
        maxRegOrderHortonianSlider.setMaximum(myHortonStructure.getBasinOrder());
        maxRegOrderHortonianSlider.setValue(myHortonStructure.getBasinOrder());
        maxRegOrderHortonianLabel.setText(""+myHortonStructure.getBasinOrder());
        
        //creo la estructura grafica para los graficos de Horton
        visad.java3d.DisplayRendererJ3D drI=new visad.java3d.TwoDDisplayRendererJ3D();
        displayH = new DisplayImplJ3D("displayHorton",drI);
        
        
        
        
        GraphicsModeControl dispGMC = (GraphicsModeControl) displayH.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        ProjectionControl pc = displayH.getProjectionControl();
        pc.setAspectCartesian(new double[] {.9, .9});
        
        orderMap = new ScalarMap( streamOrder, Display.XAxis );
        orderMap.setScalarName("Horton Order");
        horFitMap = new ScalarMap( hortonVar , Display.YAxis );
        horFitMap.setScalarName("Log(# Streams)");
        horVarMap = new ScalarMap( hortonVar , Display.SelectRange );
        
        displayH.addMap( orderMap );
        displayH.addMap( horFitMap );
        displayH.addMap( horVarMap );
        
        //creo la estructura grafica para los graficos de Distribuciones
        
        drI=new visad.java3d.TwoDDisplayRendererJ3D();
        displayCDF = new DisplayImplJ3D("displayCumulativeDF",drI);
        
        
        
        dispGMC = (GraphicsModeControl) displayCDF.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        pc = displayCDF.getProjectionControl();
        pc.setAspectCartesian(new double[] {1, .7});
        
        exedProbabMap = new ScalarMap( exedProbab, Display.YAxis );
        exedProbabMap.setScalarName("P(X < x)");
        
        displayCDF.addMap( exedProbabMap );
        
        posVarArrayMap=new ScalarMap( posVarArray , Display.XAxis );
        displayCDF.addMap( posVarArrayMap );
        posVarArrayMap.setScalarName("X");
        
        jPanel19.setLayout(new java.awt.GridLayout(myHortonStructure.getBasinOrder()/6+1, 5));
        
        checkBoxperOrder0=new javax.swing.JCheckBox[myHortonStructure.getBasinOrder()];
        
        for (int i=0;i<myHortonStructure.getBasinOrder();i++){
            
            checkBoxperOrder0[i]= new javax.swing.JCheckBox();
            
            checkBoxperOrder0[i].setText("Order "+(i+1));
            checkBoxperOrder0[i].setSelected(true);
            checkBoxperOrder0[i].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBoxOrder0ActionPerformed(evt);
                }
            }
            );
            checkBoxperOrder0[i].setForeground(new java.awt.Color(red[i]/255.0f,green[i]/255.0f,blue[i]/255.0f));
            jPanel19.add(checkBoxperOrder0[i]);
            
        }
        
        //creo la estructura grafica para el analisis basado en Links
        
        drI=new visad.java3d.TwoDDisplayRendererJ3D();
        display_Links = new DisplayImplJ3D("displayLinks",drI);
        
        
        
        dispGMC = (GraphicsModeControl) display_Links.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        pc = display_Links.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, 0.5});        
        binsMap = new ScalarMap( bins, Display.XAxis );
        freqMap = new ScalarMap( frequencies, Display.YAxis );
        xVarMap = new ScalarMap( x_var_link , Display.XAxis );
        yVarMap = new ScalarMap( y_var_link , Display.YAxis );
        dotColorMap = new ScalarMap( dotColor , Display.RGB );
        yVarValueMap=new ScalarMap( y_var_link , Display.SelectRange );

        display_Links.addMap(xVarMap);
        display_Links.addMap(yVarMap);
        display_Links.addMap(dotColorMap);
        display_Links.addMap(yVarValueMap);
        
        histoBinSizeSlider.setMinimum(1);
        histoBinSizeSlider.setMaximum(10);
        histoBinSizeSlider.setValue(5);
        
        java.util.Vector xvarVector=new java.util.Vector(), yvarVector=new java.util.Vector();
        for (int i=0;i<itemsToAnalize.length;i++){
            xvarVector.addElement(itemsToAnalize[i]);
            yvarVector.addElement(itemsToAnalize[i]);
        }
        
        xvarList.setListData(xvarVector); xvarList.setSelectedIndex(0);
        yvarList.setListData(yvarVector); yvarList.setSelectedIndex(0);
        
        yvarList.setEnabled(false);
        linkColoringLabel.setEnabled(false);
        nonColored.setEnabled(false);
        magnColored.setEnabled(false);
        logMagnColored.setEnabled(false);
        areaColored.setEnabled(false);
        logAreaColored.setEnabled(false);
        hortColored.setEnabled(false);
        yAxisListLabel.setEnabled(false);
        yLog.setEnabled(false);
        minYVarValueSlider.setEnabled(false);
        maxYVarValueSlider.setEnabled(false);
        
        //creo la estructura grafica para los graficos del analisis de RSNs
        
        drI=new visad.java3d.TwoDDisplayRendererJ3D();
        displayMap_RSNs = new DisplayImplJ3D("displayMap",drI);
        
        
        
        displayMap_RSNs.addDisplayListener(this);
        
        dispGMC = (GraphicsModeControl) displayMap_RSNs.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        lonMapRSN = new ScalarMap( RealType.Longitude, Display.XAxis );
        latMapRSN = new ScalarMap( RealType.Latitude, Display.YAxis );
        varMapRSN = new ScalarMap( varRaster,  Display.RGB );
        
        displayMap_RSNs.addMap( lonMapRSN );
        displayMap_RSNs.addMap( latMapRSN );
        displayMap_RSNs.addMap( varMapRSN );
        
        lonMapRSN.setRange(metaDatos.getMinLon()+(myCuenca.getMinX()-1)*metaDatos.getResLon()/3600.0,metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0);
        latMapRSN.setRange(metaDatos.getMinLat()+(myCuenca.getMinY()-1)*metaDatos.getResLat()/3600.0,metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0);
        
        rsnScaleSlider.setMinimum(1);
        rsnScaleSlider.setMaximum(myHortonStructure.getBasinOrder());
        rsnScaleSlider.setValue(myHortonStructure.getBasinOrder()-1);
        
        pc = displayMap_RSNs.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, (double) ((myCuenca.getMaxY()-myCuenca.getMinY()+3)/(double) (myCuenca.getMaxX()-myCuenca.getMinX()+3))});
        
        data_ref_RSNTiles = new DataReferenceImpl("data_ref_RSNTiles");
        displayMap_RSNs.addReference(data_ref_RSNTiles);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(displayMap_RSNs);
        
        myRSNAnalysis=new hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition(myLinksStructure);
        plotRSNTiles(myHortonStructure.getBasinOrder()-1);
        plotNetwork(displayMap_RSNs,myHortonStructure.getBasinOrder()-1);
        
        //Inicio los Hilos que cargan datos en la Interfaz
        
        CaptureDistributions silentDists=new CaptureDistributions(this);
        silentDists.start();
        
        ExtractSubBasins silentExtractor=new ExtractSubBasins(this);
        silentExtractor.start();
        
        //creo la estructura grafica para los graficos del analisis Hortoniano
        
        drI=new visad.java3d.TwoDDisplayRendererJ3D();
        display_Hortonian_Means = new DisplayImplJ3D("displayHortonianMean",drI);
        
        
        
        drI=new visad.java3d.TwoDDisplayRendererJ3D();
        display_Hortonian_Dists = new DisplayImplJ3D("displayHortonianDist",drI);
        
        
        
        dispGMC = (GraphicsModeControl) display_Hortonian_Means.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        dispGMC = (GraphicsModeControl) display_Hortonian_Dists.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        pc = display_Hortonian_Dists.getProjectionControl();
        pc.setAspectCartesian(new double[] {1, .7});
        
        orderMap_Hortonian = new ScalarMap( streamOrder, Display.XAxis );
        horFitMap_Hortonian = new ScalarMap( hortonVar , Display.YAxis );
        horVarMap_Hortonian = new ScalarMap( hortonVar , Display.SelectRange );
        
        display_Hortonian_Means.addMap( orderMap_Hortonian );
        display_Hortonian_Means.addMap( horFitMap_Hortonian );
        display_Hortonian_Means.addMap( horVarMap_Hortonian );
        
        exedProbabMap_Hortonian = new ScalarMap( exedProbab, Display.YAxis );
        posVarArrayMap_Hortonian=new ScalarMap( posVarArray, Display.XAxis );
        
        display_Hortonian_Dists.addMap( exedProbabMap_Hortonian );
        display_Hortonian_Dists.addMap( posVarArrayMap_Hortonian );
        
        jPanel42.setLayout(new java.awt.GridLayout(myHortonStructure.getBasinOrder()/6+1, 5));
        jPanel27.setLayout(new java.awt.GridLayout(myHortonStructure.getBasinOrder()/6+1, 5));
        
        checkBoxperOrder1=new javax.swing.JCheckBox[myHortonStructure.getBasinOrder()];
        checkBoxperOrder2=new javax.swing.JCheckBox[myHortonStructure.getBasinOrder()];
        
        for (int i=0;i<myHortonStructure.getBasinOrder();i++){
            
            checkBoxperOrder1[i]= new javax.swing.JCheckBox();
            
            checkBoxperOrder1[i].setText("Order "+(i+1));
            checkBoxperOrder1[i].setEnabled(false);
            checkBoxperOrder1[i].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBoxOrder1ActionPerformed(evt);
                }
            }
            );
            checkBoxperOrder1[i].setForeground(new java.awt.Color(red[i]/255.0f,green[i]/255.0f,blue[i]/255.0f));
            jPanel42.add(checkBoxperOrder1[i]);
            
            checkBoxperOrder2[i]= new javax.swing.JCheckBox();
            
            checkBoxperOrder2[i].setText("Order "+(i+1));
            checkBoxperOrder2[i].setEnabled(false);
            checkBoxperOrder2[i].addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent evt) {
                    jCheckBoxOrder2ActionPerformed(evt);
                }
            }
            );
            checkBoxperOrder2[i].setForeground(new java.awt.Color(red[i]/255.0f,green[i]/255.0f,blue[i]/255.0f));
            jPanel27.add(checkBoxperOrder2[i]);
            
        }
        
        
        drI=new visad.java3d.TwoDDisplayRendererJ3D();
        displayMap_Hortonian = new DisplayImplJ3D("displayMapHortonian",drI);
        
        
        
        dispGMC = (GraphicsModeControl) displayMap_Hortonian.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        lonMapHortonian = new ScalarMap( RealType.Longitude, Display.XAxis );
        latMapHortonian = new ScalarMap( RealType.Latitude, Display.YAxis );
        varMapHortonian = new ScalarMap( varRaster,  Display.RGB );
        
        displayMap_Hortonian.addMap( lonMapHortonian );
        displayMap_Hortonian.addMap( latMapHortonian );
        displayMap_Hortonian.addMap( varMapHortonian );
        
        jPanel25.remove(jPanel43);
        
        //creo la estructura grafica para la Hypsometric Curve
        
        drI=new visad.java3d.TwoDDisplayRendererJ3D();
        displayHC = new DisplayImplJ3D("displayHypsometricCurve",drI);
        
        dispGMC = (GraphicsModeControl) displayHC.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        pc = displayHC.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, 0.5});
        
        percentAreaMap = new ScalarMap( percentAreaValue, Display.XAxis );
        percentAreaMap.setScalarName("% of Area");
        elevationMap = new ScalarMap( elevationValue, Display.YAxis );
        elevationMap.setScalarName("Elevation [m]");
        
        displayHC.addMap( percentAreaMap );
        displayHC.addMap( elevationMap );
        
        //creo la estructura grafica para la Drainage Density
        
        drI=new visad.java3d.TwoDDisplayRendererJ3D();
        displayDD = new DisplayImplJ3D("displayDrainageDensity",drI);
        
        
        
        dispGMC = (GraphicsModeControl) displayDD.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        pc = displayDD.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, 0.5});
        
        logAreaMap = new ScalarMap( logAreaValue, Display.XAxis );
        logAreaMap.setScalarName("Log(Area) [km^2]");
        ddMap = new ScalarMap( drainageDensity, Display.YAxis );
        ddMap.setScalarName("Drainage Density");
        ddValueMap = new ScalarMap( drainageDensity, Display.SelectRange );
        
        displayDD.addMap( logAreaMap );
        displayDD.addMap( ddMap );
        displayDD.addMap( ddValueMap );
        
        //creo la estructura grafica para la width function
        
        drI=new visad.java3d.TwoDDisplayRendererJ3D();
        displayW = new DisplayImplJ3D("displayWidthFunction",drI);
        
        
        
        dispGMC = (GraphicsModeControl) displayW.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        pc = displayW.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, 0.5});
        
        disMap = new ScalarMap( distanceToOut, Display.XAxis );
        disMap.setScalarName("Distance [km]");
        numLinkMap = new ScalarMap( numLinks, Display.YAxis );
        numLinkMap.setScalarName("# of Links");
        
        displayW.addMap( disMap );
        displayW.addMap( numLinkMap );
        
        plotWidthFunction(0);

        jPanel1.add("Center",displayW.getComponent());
        
        firstTouchTab[0]=true;
        
        //Load HTML report
        htmlGeomorphometricReport.setContentType("text/html");
        htmlGeomorphometricReport.setText( "<strong>TEST</strong><br>"+
                                           "<strong>TEST1</strong><br>"+
                                           "<strong>TEST2</strong><br>"+
                                           "<strong>TEST3</strong><br>"+
                                           "<strong>TEST4</strong><br>"+
                                           "<strong>TEST5</strong><br>"+
                                           "<strong>TEST6</strong><br>"+
                                           "<strong>TEST7</strong><br>"+
                                           "<strong>TEST8</strong><br>"+
                                           "<strong>TEST9</strong><br>"+
                                           "<strong>TEST</strong><br>"+
                                           "<strong>TEST1</strong><br>"+
                                           "<strong>TEST2</strong><br>"+
                                           "<strong>TEST3</strong><br>"+
                                           "<strong>TEST4</strong><br>"+
                                           "<strong>TEST5</strong><br>"+
                                           "<strong>TEST6</strong><br>"+
                                           "<strong>TEST7</strong><br>"+
                                           "<strong>TEST8</strong><br>"+
                                           "<strong>TEST9</strong><br>"+
                                           "<strong>TEST</strong><br>"+
                                           "<strong>TEST1</strong><br>"+
                                           "<strong>TEST2</strong><br>"+
                                           "<strong>TEST3</strong><br>"+
                                           "<strong>TEST4</strong><br>"+
                                           "<strong>TEST5</strong><br>"+
                                           "<strong>TEST6</strong><br>"+
                                           "<strong>TEST7</strong><br>"+
                                           "<strong>TEST8</strong><br>"+
                                           "<strong>TEST9</strong><br>"+
                                           "<strong>TEST</strong><br>"+
                                           "<strong>TEST1</strong><br>"+
                                           "<strong>TEST2</strong><br>"+
                                           "<strong>TEST3</strong><br>"+
                                           "<strong>TEST4</strong><br>"+
                                           "<strong>TEST5</strong><br>"+
                                           "<strong>TEST6</strong><br>"+
                                           "<strong>TEST7</strong><br>"+
                                           "<strong>TEST8</strong><br>"+
                                           "<strong>TEST9</strong><br>"
                                           );
        
    }
    
    private void plotWidthFunction(int metric) throws RemoteException, VisADException,java.io.IOException{
        binsizeWidth=1.0f;
        
        binSizeSlider.setMinimum(1);
        binSizeSlider.setMaximum(10);
        
        if (metric == 0) {
            disMap.setScalarName("Topologic Distance [# Links]");
            binSizeTextField.setText(binsizeWidth+" Links");
            binSizeSlider.setValue(1);
            plotWidthFunction(metric,binsizeWidth);
        }
        
        if (metric == 1) {
            float[][] varValues=myLinksStructure.getVarValues(1);
            binsizeWidth=new hydroScalingAPI.util.statistics.Stats(varValues).meanValue;
            disMap.setScalarName("Distance [km]");
            binSizeTextField.setText(binsizeWidth+" km");
            binSizeSlider.setValue(5);
            plotWidthFunction(metric,binsizeWidth);
            binsizeWidth/=5.0f;
        }
        
        if (metric == 2) {
            disMap.setScalarName("Elevation [m]");
            binSizeTextField.setText(binsizeWidth+" m");
            binSizeSlider.setValue(1);
            plotWidthFunction(metric,binsizeWidth);
        }
    }
    
    private void plotWidthFunction(int metric,float binsize) throws RemoteException, VisADException,java.io.IOException{
        
        displayW.removeAllReferences();
        
        float[][] wFunc=new float[0][0];
        
        if(metric == 2){
            float[][] linkElev=myLinksStructure.getVarValues(10);
            wFunc=new float[1][linkElev[0].length];
            for (int i=0;i<linkElev[0].length;i++){
                wFunc[0][i]=linkElev[0][i]-linkElev[0][myLinksStructure.OuletLinkNum];
            }
            metric=0;
        } else {
            wFunc=myLinksStructure.getDistancesToOutlet();
        }
        java.util.Arrays.sort(wFunc[metric]);
        
        float[][] gWFunc=new float[1][wFunc[metric].length];
        
        for (int i=0;i<wFunc[0].length;i++)
            gWFunc[0][i]=wFunc[metric][i];
        
        vals_ff_W = new FlatField( func_distanceToOut_numLinks, new Linear1DSet(distanceToOut,1,gWFunc[0].length,gWFunc[0].length));
        vals_ff_W.setSamples( gWFunc );
        
        int numBins=(int) (gWFunc[0][gWFunc[0].length-1]/binsize)+1;
        
        Linear1DSet binsSet = new Linear1DSet(numLinks,binsize, gWFunc[0][gWFunc[0].length-1]+binsize,numBins);
        
        FlatField hist = visad.math.Histogram.makeHistogram(vals_ff_W, binsSet);
        
        float[][] laLinea=binsSet.getSamples();
        double[][] laWFunc=hist.getValues();
        
        float[][] laLineaRepeted=new float[1][2*laLinea[0].length];
        double[][] laWFuncRepeted=new double[1][2*laLinea[0].length];
        for (int i=0;i<laLineaRepeted[0].length;i+=2){
            laLineaRepeted[0][i]=laLinea[0][i/2];
            laLineaRepeted[0][i+1]=laLinea[0][i/2];
        }
        
        laWFuncRepeted[0][0]=0;
        
        for (int i=1;i<laLineaRepeted[0].length-2;i+=2){
            laWFuncRepeted[0][i]=laWFunc[0][i/2];
            laWFuncRepeted[0][i+1]=laWFunc[0][i/2];
        }
        
        Irregular1DSet binsRepated=new Irregular1DSet(numLinks,laLineaRepeted);
        
        vals_ff_W = new FlatField( func_distanceToOut_numLinks, binsRepated);
        vals_ff_W.setSamples( laWFuncRepeted );
        
        data_refW = new DataReferenceImpl("data_ref_WF");
        data_refW.setData(vals_ff_W);
        
        displayW.addReference( data_refW );
        
    }
    
    private void plotHypCurve() throws RemoteException, VisADException,java.io.IOException{
        displayHC.removeAllReferences();
        
        float[] eleBasin=myCuenca.getElevations();
        java.util.Arrays.sort(eleBasin);
        float localMinElev=eleBasin[0];
        float localMaxElev=eleBasin[eleBasin.length-1];
        
        float[][] keyElev=new float[1][100];
        float[][] accumElev=new float[1][100];
        int k=0;
        for(int i=0;i<eleBasin.length;i++){
            float eleToTest=localMinElev+k/99.0f*(localMaxElev-localMinElev);
            if(eleBasin[i] >= eleToTest) {
                keyElev[0][keyElev[0].length-1-k]=(eleToTest-localMinElev)/(localMaxElev-localMinElev);
                accumElev[0][k]=i/(float)eleBasin.length;
                k++;
                i--;
            }
        }
        
        Irregular1DSet percents=new Irregular1DSet(elevationValue,accumElev);
        
        vals_ff_HC = new FlatField( func_elePer_eleVal, percents);
        vals_ff_HC.setSamples( keyElev );
        
        data_refHC = new DataReferenceImpl("data_ref_HC");
        data_refHC.setData(vals_ff_HC);
        
        displayHC.addReference( data_refHC );
        
    }
    
    private void plotDdGraph(int mode) throws RemoteException, VisADException,java.io.IOException{
        displayDD.removeAllReferences();
        
        //Model All Links
        if(mode == 0){
            float[][] linkAccumL=myLinksStructure.getVarValues(5);
            float[][] linkAccumA=myLinksStructure.getVarValues(2);
            
            float[][] linkDD=new float[2][linkAccumL[0].length];
            for(int i=0;i<linkAccumL[0].length;i++){
                linkDD[0][i]=(float)Math.log(linkAccumA[0][i]);
                linkDD[1][i]=linkAccumL[0][i]/linkAccumA[0][i];
                linkAccumA[0][i]=linkDD[0][i];
            }
            
            Irregular1DSet logAreaValues=new Irregular1DSet(logAreaIndex,linkAccumA);
        
            vals_ff_DD = new FlatField( func_logArea_drainageDensity, logAreaValues);
            vals_ff_DD.setSamples( linkDD );

            data_refDD = new DataReferenceImpl("data_ref_DD");
            data_refDD.setData(vals_ff_DD);
            
            ConstantMap[] pointsCMap = {    new ConstantMap( 1.0f, Display.Red),
                                            new ConstantMap( 1.0f, Display.Green),
                                            new ConstantMap( 1.0f, Display.Blue),
                                            new ConstantMap( 2.5f, Display.PointSize)};

            displayDD.addReference( data_refDD, pointsCMap);
            
            int outletID=myLinksStructure.getOutletID();
            float ddMeasuredValue=linkAccumL[0][outletID]/(float)Math.exp(linkAccumA[0][outletID]);
            
            Linear1DSet extremeAreaValues=new Linear1DSet(logAreaValue,-3,linkAccumA[0][outletID],2);
            
            double[][] ddFinalValue=new double[][] {{ddMeasuredValue,ddMeasuredValue}};
            
            System.out.println("Drainage Density Converges to: "+ddMeasuredValue);
            
            FlatField vals_ff_DDLine = new FlatField( func_logArea_drainageDensity_Lines, extremeAreaValues);
            vals_ff_DDLine.setSamples( ddFinalValue );

            DataReferenceImpl data_refDDLine = new DataReferenceImpl("data_refDDLine");
            data_refDDLine.setData(vals_ff_DDLine);
            
            displayDD.addReference( data_refDDLine );
            
        }
        
        //Mode Horton
        if(mode == 1){
            data_refHDd=new DataReferenceImpl[myHortonStructure.getBasinOrder()];
            for(int j=0;j<myHortonStructure.getBasinOrder();j++){
                float[] linkAccumL=totalGeomLengthDist[j];
                float[] linkAccumA=areasDist[j];

                float[][] linkDD=new float[2][linkAccumL.length];
                float[][] linkAccumAVal=new float[1][linkAccumL.length];
                for(int i=0;i<linkAccumL.length;i++){
                    linkDD[0][i]=(float)Math.log(linkAccumA[i]);
                    linkDD[1][i]=linkAccumL[i]/linkAccumA[i];
                    linkAccumAVal[0][i]=linkDD[0][i];
                }

                Irregular1DSet logAreaValues=new Irregular1DSet(logAreaIndex,linkAccumAVal);

                FlatField vals_ff_DD_H = new FlatField( func_logArea_drainageDensity, logAreaValues);
                vals_ff_DD_H.setSamples( linkDD );

                data_refHDd[j] = new DataReferenceImpl("data_ref_DD");
                data_refHDd[j].setData(vals_ff_DD_H);

                ConstantMap[] pointsCMap = {    new ConstantMap( red[j]/255., Display.Red),
                                                new ConstantMap( green[j]/255., Display.Green),
                                                new ConstantMap( blue[j]/255., Display.Blue),
                                                new ConstantMap( 2.5f, Display.PointSize)};

                displayDD.addReference( data_refHDd[j], pointsCMap);
            }
            
            float ddMeasuredValue=totalGeomLengthDist[myHortonStructure.getBasinOrder()-1][0]/areasDist[myHortonStructure.getBasinOrder()-1][0];
            
            Linear1DSet extremeAreaValues=new Linear1DSet(logAreaValue,-3,Math.log(areasDist[myHortonStructure.getBasinOrder()-1][0]),2);
            
            double[][] ddFinalValue=new double[][] {{ddMeasuredValue,ddMeasuredValue}};
            
            FlatField vals_ff_DDLine = new FlatField( func_logArea_drainageDensity_Lines, extremeAreaValues);
            vals_ff_DDLine.setSamples( ddFinalValue );

            DataReferenceImpl data_refDDLine = new DataReferenceImpl("data_refDDLine");
            data_refDDLine.setData(vals_ff_DDLine);
            
            displayDD.addReference( data_refDDLine );
        }
        
    }
    
    private void plotHortonGraph(float[] ratios,float[] VARvsOrder,String ytitle) throws RemoteException, VisADException{
        
        displayH.removeAllReferences();
        
        ratioLabel.setText("Ratio = "+Math.round(Math.exp(Math.abs(ratios[0]))*1000)/1000.);
        rSquareLabel.setText("R^2 = "+Math.round(ratios[2]*1000)/1000.);
        
        Linear1DSet orders = new Linear1DSet(streamOrder,1, myHortonStructure.getBasinOrder(),2);
        
        float[][] linePoints=new float[1][2]; linePoints[0][0]=(float) ratios[0]*1+ratios[1];
        linePoints[0][1]=(float) ratios[0]*myHortonStructure.getBasinOrder()+ratios[1];
        
        FlatField vals_ff_LF = new FlatField( func_order_hortonVar, orders);
        vals_ff_LF.setSamples( linePoints );
        
        DataReferenceImpl refeLineFit=new DataReferenceImpl("lineFit");
        refeLineFit.setData(vals_ff_LF);
        
        float[][] NLvsOrder=new float[2][myHortonStructure.getBasinOrder()];
        NLvsOrder[1]=VARvsOrder;
        
        for (int i=0;i<NLvsOrder[0].length;i++){
            NLvsOrder[0][i]=i+1;
            NLvsOrder[1][i]=(float) Math.log(NLvsOrder[1][i]);
        }
        
        orders = new Linear1DSet(index,1, myHortonStructure.getBasinOrder(),myHortonStructure.getBasinOrder());
        
        vals_ff_H = new FlatField( func_index_hortonVar, orders);
        vals_ff_H.setSamples( NLvsOrder );
        
        data_refH = new DataReferenceImpl("data_ref_H");
        data_refH.setData(vals_ff_H);
        
        java.util.Arrays.sort(NLvsOrder[1]);
        
        horFitMap.setRange(NLvsOrder[1][0]-1,NLvsOrder[1][NLvsOrder[1].length-1]+1);
        horFitMap.setScalarName(ytitle);
        
        orderMap.setRange(0,myHortonStructure.getBasinOrder()+1);
        
        
        ConstantMap[] pointsCMap = {    new ConstantMap( 1.0f, Display.Red),
                                        new ConstantMap( 1.0f, Display.Green),
                                        new ConstantMap( 0.0f, Display.Blue),
                                        new ConstantMap( 4.50f, Display.PointSize)};
        
        displayH.addReference( refeLineFit );
        
        displayH.addReference( data_refH ,pointsCMap);
        
    }
    
    private void updateHortonPlots(){
        try{
            if (jRadioButtonBranching.isSelected()){
                float[] ratios=myHortonStructure.getBranchingRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),branchingValues);
                plotHortonGraph(ratios,(float[])branchingValues.clone(),"Log(# of Streams)");
            }
            
            if (jRadioButtonLengthGeom.isSelected()){
                float[] ratios=myHortonStructure.getLengthRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),geomLengthDist);
                float[] NLvsOrder=myHortonStructure.getLengthPerOrder(geomLengthDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Geometric Length)");
            }
            if (jRadioButtonLengthTopol.isSelected()){
                float[] ratios=myHortonStructure.getLengthRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),topolLengthDist);
                float[] NLvsOrder=myHortonStructure.getLengthPerOrder(topolLengthDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Topologic Length)");
            }
            
            if (jRadioButtonArea.isSelected()){
                float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),areasDist);
                float[] NLvsOrder=myHortonStructure.getQuantityPerOrder(areasDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Mean Stream Area)");
            }
            if (jRadioButtonMagnitude.isSelected()){
                float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),magnDist);
                float[] NLvsOrder=myHortonStructure.getQuantityPerOrder(magnDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Mean Stream Magnitude)");
            }
            if (jRadioButtonMaxLengthGeom.isSelected()){
                float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),mainGeomLengthDist);
                float[] NLvsOrder=myHortonStructure.getQuantityPerOrder(mainGeomLengthDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Mean Max Geometric Stream Length)");
            }
            if (jRadioButtonMaxLengthTopol.isSelected()){
                float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),mainTopolLengthDist);
                float[] NLvsOrder=myHortonStructure.getQuantityPerOrder(mainTopolLengthDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Mean Max Topologic Stream Length)");
            }
            if (jRadioButtonTotalLengthGeom.isSelected()){
                float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),totalGeomLengthDist);
                float[] NLvsOrder=myHortonStructure.getQuantityPerOrder(totalGeomLengthDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Mean Total Geometric Stream Length)");
            }
            
            if (jRadioButtonSlopes.isSelected()){
                float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),slopesDist);
                float[] NLvsOrder=myHortonStructure.getQuantityPerOrder(slopesDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Stream Slopes)");
            }
            
            if (jRadioButtonTotalDrop.isSelected()){
                float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),totalDropDist);
                float[] NLvsOrder=myHortonStructure.getQuantityPerOrder(totalDropDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Total Channel Drop)");
            }
            
            if (jRadioButtonMaxDrop.isSelected()){
                float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),maxDropDist);
                float[] NLvsOrder=myHortonStructure.getQuantityPerOrder(maxDropDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Maximum Channel Drop)");
            }
            
            if (jRadioButtonStreamDrop.isSelected()){
                float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),streamDropDist);
                float[] NLvsOrder=myHortonStructure.getQuantityPerOrder(streamDropDist);
                plotHortonGraph(ratios,NLvsOrder,"Log(Stream Drop)");
            }

        } catch (java.io.IOException ioe){
            System.err.print("Failed updating horton plots");
            System.err.print(ioe);
        } catch (VisADException v){
            System.err.print("Failed updating horton plots");
            System.err.print(v);
        }
    }
    
    private void plotHortonDistribution(float[][] varDistrib) throws RemoteException, VisADException{
        
        displayCDF.removeAllReferences();
        
        data_refHDis= new DataReferenceImpl[varDistrib.length];
        
        for (int i=0;i<varDistrib.length;i++){
            
            float[][] thisDist=new float[1][varDistrib[i].length];
            
            
            hydroScalingAPI.util.statistics.Stats stats=new hydroScalingAPI.util.statistics.Stats(varDistrib[i]);
            java.util.Arrays.sort(varDistrib[i]);
            
            for (int j=0;j<varDistrib[i].length;j++)
                thisDist[0][j]=(float) (varDistrib[i][j]/stats.meanValue);
            
            Linear1DSet exedProbability = new Linear1DSet(exedProbab,0, 1,varDistrib[i].length);
            
            FlatField vals_ff_CDF = new FlatField( func_exProb_hortonVarOrder, exedProbability);
            vals_ff_CDF.setSamples( thisDist );
            
            data_refHDis[i]=new DataReferenceImpl("ditrib"+i);
            data_refHDis[i].setData(vals_ff_CDF);
            
            ConstantMap[] distCMap = {      new ConstantMap( red[i]/255., Display.Red),
                                            new ConstantMap( green[i]/255., Display.Green),
                                            new ConstantMap( blue[i]/255., Display.Blue),
                                            new ConstantMap( (i+1),Display.LineWidth)
                                     };
            
            
            if (checkBoxperOrder0[i].isSelected()) displayCDF.addReference( data_refHDis[i],distCMap);
        }
        
    }
    
    private float[][] getFilteredLinkValues(boolean onlyType) throws java.io.IOException{
        float[][] varValues=myLinksStructure.getVarValues(xvarList.getSelectedIndex());
        if(xLog.isSelected()){
            for(int i=0;i<varValues[0].length;i++) varValues[0][i]=(float)Math.log(varValues[0][i]);
        }
        boolean[] valuesToKeep=new boolean[varValues[0].length];
        
        //Filter by Interior/Exteriors and Max/Min-X and Max/Min-Y
        int countFiltered=0;
        
        float[][] magnitudes=myLinksStructure.getVarValues(6);
        boolean filterVar;
        for(int i=0;i<valuesToKeep.length;i++){
            filterVar=false;
            if(includeInterior.isSelected() && magnitudes[0][i] > 1) filterVar=true;
            if(includeExterior.isSelected() && magnitudes[0][i] == 1) filterVar=true;
            if(filterVar && !onlyType){
                filterVar&=(varValues[0][i] >= minXVarValueCurrent) && (varValues[0][i] <= maxXVarValueCurrent);
            }
            valuesToKeep[i]=filterVar;
            if(filterVar) countFiltered++;
        }
        
        float[][] varValuesFiltered=new float[1][countFiltered];
        int k=0;
        for(int i=0;i<varValues[0].length;i++){
            if(valuesToKeep[i]) {
                varValuesFiltered[0][k]=varValues[0][i];
                k++;
            }
        }
        return varValuesFiltered;
    }
    
    private float[] getXYFilteredLinkValues(String varToFilter) throws java.io.IOException{
        
        float[][] xVarValues=myLinksStructure.getVarValues(xvarList.getSelectedIndex());
        float[][] yVarValues=myLinksStructure.getVarValues(yvarList.getSelectedIndex());
        
        if(xLog.isSelected()){
            for(int i=0;i<xVarValues[0].length;i++) xVarValues[0][i]=(float)Math.log(xVarValues[0][i]);
        }
        if(yLog.isSelected()){
            for(int i=0;i<yVarValues[0].length;i++) yVarValues[0][i]=(float)Math.log(yVarValues[0][i]);
        }
        
        boolean[] valuesToKeep=new boolean[xVarValues[0].length];
        
        //Filter by Interior/Exteriors and Max/Min-X and Max/Min-Y
        int countFiltered=0;
        
        float[][] magnitudes=myLinksStructure.getVarValues(6);
        boolean filterVar;
        for(int i=0;i<valuesToKeep.length;i++){
            filterVar=false;
            if(includeInterior.isSelected() && magnitudes[0][i] > 1) filterVar=true;
            if(includeExterior.isSelected() && magnitudes[0][i] == 1) filterVar=true;
            if(filterVar){
                filterVar&=(xVarValues[0][i] >= minXVarValueCurrent) && (xVarValues[0][i] <= maxXVarValueCurrent);
                filterVar&=(yVarValues[0][i] >= minYVarValueCurrent) && (yVarValues[0][i] <= maxYVarValueCurrent);
            }
            valuesToKeep[i]=filterVar;
            if(filterVar) countFiltered++;
        }
        
        float[][] varValues=new float[0][0];
        
        if(varToFilter.equalsIgnoreCase("x")){
            varValues=xVarValues;
        } else {
            if(varToFilter.equalsIgnoreCase("y")){
                varValues=yVarValues;
            } else{
                if(nonColored.isSelected()){
                    varValues=new float[1][xVarValues[0].length];
                    java.util.Arrays.fill(varValues[0],1);
                }
                if(magnColored.isSelected()){
                    varValues=myLinksStructure.getVarValues(6);
                }
                if(logMagnColored.isSelected()){
                    varValues=myLinksStructure.getVarValues(6);
                    for(int i=0;i<varValues[0].length;i++) varValues[0][i]=(float)Math.log(varValues[0][i]);
                }
                if(areaColored.isSelected()){
                    varValues=myLinksStructure.getVarValues(2);
                }
                if(logAreaColored.isSelected()){
                    varValues=myLinksStructure.getVarValues(2);
                    for(int i=0;i<varValues[0].length;i++) varValues[0][i]=(float)Math.log(varValues[0][i]);
                }
                if(hortColored.isSelected()){
                    varValues=myLinksStructure.getVarValues(4);
                }
            }
        }
        
        
        float[] varValuesFiltered=new float[countFiltered];
        int k=0;
        
        for(int i=0;i<varValues[0].length;i++){
            if(valuesToKeep[i]) {
                varValuesFiltered[k]=varValues[0][i];
                k++;
            }
        }
        
        return varValuesFiltered;
    }
    
    private void rePlotLinksVarHistogram(float binsizeHistogram){
        rePlotLinksVarHistogram(new Float(binsizeHistogram));
    }
    
    private void rePlotLinksVarHistogram(Float binsizeHistogram){
        try{
            if(histo_Mode.isSelected()){
                plotLinksVarHistogram(binsizeHistogram);
            }
        } catch (RemoteException r){
            System.err.print(r);
        } catch (VisADException v){
            System.err.print(v);
        } catch (java.io.IOException IOE){
            System.err.print(IOE);
        }
        
    }
    
    private void plotLinksVarHistogram(Float bin) throws RemoteException, VisADException, java.io.IOException{
        float binsize=0;

        
        
        if(bin == null) {
            float[][] varValues=getFilteredLinkValues(true);
            hydroScalingAPI.util.statistics.Stats varStats=new hydroScalingAPI.util.statistics.Stats(varValues);
            binsizeHistogram=(float)(varStats.standardDeviation/10.0);
            minXVarValue=varStats.minValue;
            maxXVarValue=varStats.maxValue;
            minXVarValueCurrent=minXVarValue;
            maxXVarValueCurrent=maxXVarValue;
            
            System.out.println("Mean: "+varStats.meanValue);
            System.out.println("Standard Deviation: "+varStats.standardDeviation);
            System.out.println("Min: "+varStats.minValue);
            System.out.println("Max: "+varStats.maxValue);
            
            minXVarValueSlider.setValue(0);
            maxXVarValueSlider.setValue(100);
            
            binsize=binsizeHistogram;
        } else {
            binsize=bin.floatValue();
        }
        
        float[][] varValues=getFilteredLinkValues(false);
        histoBinSizeTextField.setText(""+binsize);
        
        java.util.Arrays.sort(varValues[0]);
        
        vals_ff_Li = new FlatField( func_bins_frequencies, new Linear1DSet(bins,1,varValues[0].length,varValues[0].length));
        vals_ff_Li.setSamples( varValues );
        
        //binSizeSlider.setMinimum(0);
        //binSizeSlider.setMaximum(100);
        //binSizeSlider.setValue(Math.round(binsize/gWFunc[0][gWFunc[0].length-1]));
        
        Linear1DSet binsSet = new Linear1DSet(frequencies,varValues[0][0]-binsize, varValues[0][varValues[0].length-1]+binsize,(int) (varValues[0][varValues[0].length-1]/binsize));
        FlatField hist = visad.math.Histogram.makeHistogram(vals_ff_Li, binsSet);
        
        float[][] laLinea=binsSet.getSamples();
        double[][] laWFunc=hist.getValues();
        
        float[][] laLineaRepeted=new float[1][2*laLinea[0].length];
        double[][] laWFuncRepeted=new double[1][2*laLinea[0].length];
        for (int i=0;i<laLineaRepeted[0].length;i+=2){
            laLineaRepeted[0][i]=laLinea[0][i/2];
            laLineaRepeted[0][i+1]=laLinea[0][i/2];
        }
        
        laWFuncRepeted[0][0]=0;
        
        for (int i=1;i<laLineaRepeted[0].length-2;i+=2){
            laWFuncRepeted[0][i]=laWFunc[0][i/2];
            laWFuncRepeted[0][i+1]=laWFunc[0][i/2];
        }
        
        Irregular1DSet binsRepated=new Irregular1DSet(frequencies,laLineaRepeted);
        
        vals_ff_Li = new FlatField( func_bins_frequencies, binsRepated);
        vals_ff_Li.setSamples( laWFuncRepeted );
        
        data_refLi = new DataReferenceImpl("data_ref_LINK");
        data_refLi.setData(vals_ff_Li);
        
        
        try{
            display_Links.removeMap(xVarMap);
            display_Links.removeMap(yVarMap);
            display_Links.removeMap(yVarValueMap);
            display_Links.removeMap(dotColorMap);

            display_Links.addMap(binsMap);
            display_Links.addMap(freqMap);
        } catch(visad.VisADException vie){
            
        }
        
        //xVarMap = new ScalarMap( x_var_link , Display.XAxis );
        //yVarMap = new ScalarMap( y_var_link , Display.YAxis );
        //display_Links.addMap(xVarMap);
        //display_Links.addMap(yVarMap);
        
        
        display_Links.removeAllReferences();
        display_Links.addReference( data_refLi );
                
        
    }
    private void rePlotXYvalues(){
        rePlotXYvalues(false);
    }
    private void rePlotXYvalues(boolean reset){
        
        try{
            if(reset){
                float[][] xVarValues=myLinksStructure.getVarValues(xvarList.getSelectedIndex());
                float[][] yVarValues=myLinksStructure.getVarValues(yvarList.getSelectedIndex());
                if(xLog.isSelected())
                    for(int i=0;i<xVarValues[0].length;i++) 
                        xVarValues[0][i]=(float)Math.log(xVarValues[0][i]);
                if(yLog.isSelected())
                    for(int i=0;i<yVarValues[0].length;i++) 
                        yVarValues[0][i]=(float)Math.log(yVarValues[0][i]);
                
                hydroScalingAPI.util.statistics.Stats xVarStats=new hydroScalingAPI.util.statistics.Stats(xVarValues);
                hydroScalingAPI.util.statistics.Stats yVarStats=new hydroScalingAPI.util.statistics.Stats(yVarValues);
                
                minXVarValue=xVarStats.minValue;
                maxXVarValue=xVarStats.maxValue;
                minXVarValueCurrent=minXVarValue;
                maxXVarValueCurrent=maxXVarValue;
                
                minYVarValue=yVarStats.minValue;
                maxYVarValue=yVarStats.maxValue;
                minYVarValueCurrent=minYVarValue;
                maxYVarValueCurrent=maxYVarValue;
            }
            
            plotXYvalues();
            
        }catch (RemoteException r){
            System.err.print(r);
        } catch (VisADException v){
            System.err.print(v);
        } catch (java.io.IOException IOE){
            System.err.print(IOE);
        }
    }
    
    private void plotXYvalues() throws RemoteException, VisADException, java.io.IOException{
        
        try{
            display_Links.removeMap(binsMap);
            display_Links.removeMap(freqMap);

            display_Links.addMap(xVarMap);
            display_Links.addMap(yVarMap);
            display_Links.addMap(yVarValueMap);
            display_Links.addMap(dotColorMap);
        } catch(visad.VisADException vie){
            
        }
        
        display_Links.removeAllReferences();
        
        float[] xVarValues=getXYFilteredLinkValues("x");
        float[] yVarValues=getXYFilteredLinkValues("y");
        
        float[][] xyLinkValues=new float[3][];
        
        xyLinkValues[0]=xVarValues;
        xyLinkValues[1]=yVarValues;
        xyLinkValues[2]=getXYFilteredLinkValues("color");

        if(xLog.isSelected()){
            xVarMap.setScalarName("log["+itemsToAnalize[xvarList.getSelectedIndex()]+"]");
        } else {
            xVarMap.setScalarName(itemsToAnalize[xvarList.getSelectedIndex()]);
        }
        
        if(yLog.isSelected()){
            yVarMap.setScalarName("log["+itemsToAnalize[yvarList.getSelectedIndex()]+"]");
        } else {
            yVarMap.setScalarName(itemsToAnalize[yvarList.getSelectedIndex()]);
        }
        
        float[][] linkAccumAVal=new float[1][xyLinkValues[1].length];
        for(int i=0;i<xyLinkValues[0].length;i++){
            linkAccumAVal[0][i]=xVarValues[i];
        }
        
        Irregular1DSet xVarIndex=new Irregular1DSet(x_var_index,linkAccumAVal);
        
        vals_ff_Li = new FlatField( func_xVarLink_yVarLink, xVarIndex);
        vals_ff_Li.setSamples( xyLinkValues );
        
        data_refLi = new DataReferenceImpl("data_ref_LINK");
        data_refLi.setData(vals_ff_Li);
        
        ConstantMap[] pointsCMap = {    //new ConstantMap( 1.0f, Display.Red),
            //new ConstantMap( 1.0f, Display.Green),
            //new ConstantMap( 0.0f, Display.Blue),
            new ConstantMap( 4.50f, Display.PointSize)};
            
            
            display_Links.addReference( data_refLi,pointsCMap );
            
            
    }
    
    /**
     * Informes the Module if the independent Threads have finished calculating
     * distributions for a given spatial variable
     * @param state A boolean indicating is the action has completed
     */
    public void loadSpatialVariableState(boolean state){
        loadSpatialVariable.setEnabled(state);
    }
    
    private void plotHortonGraphHortonian(float[] ratios,float[] VARvsOrder,String ytitle) throws RemoteException, VisADException{
        
        display_Hortonian_Means.removeAllReferences();
        
        Linear1DSet orders = new Linear1DSet(streamOrder,1, myHortonStructure.getBasinOrder(),2);
        
        float[][] linePoints=new float[1][2]; linePoints[0][0]=(float) ratios[0]*1+ratios[1];
        linePoints[0][1]=(float) ratios[0]*myHortonStructure.getBasinOrder()+ratios[1];
        
        FlatField vals_ff_LF = new FlatField( func_order_hortonVar, orders);
        vals_ff_LF.setSamples( linePoints );
        
        DataReferenceImpl refeLineFit=new DataReferenceImpl("lineFit");
        refeLineFit.setData(vals_ff_LF);
        
        float[][] NLvsOrder=new float[2][myHortonStructure.getBasinOrder()];
        NLvsOrder[1]=VARvsOrder;
        
        System.out.println("Accumulated Value: "+NLvsOrder[1][NLvsOrder[0].length-1]+" Average Value: "+NLvsOrder[1][NLvsOrder[0].length-1]/(myCuenca.getXYBasin())[0].length);
        
        for (int i=0;i<NLvsOrder[0].length;i++){
            NLvsOrder[0][i]=i+1;
            NLvsOrder[1][i]=(float) Math.log(NLvsOrder[1][i]);
        }
        
        orders = new Linear1DSet(index,1, myHortonStructure.getBasinOrder(),myHortonStructure.getBasinOrder());
        
        vals_ff_H = new FlatField( func_index_hortonVar, orders);
        vals_ff_H.setSamples( NLvsOrder );
        
        data_refH = new DataReferenceImpl("data_ref_H");
        data_refH.setData(vals_ff_H);
        
        java.util.Arrays.sort(NLvsOrder[1]);
        
        horFitMap_Hortonian.setRange(NLvsOrder[1][0]-1,NLvsOrder[1][NLvsOrder[1].length-1]+1);
        horFitMap_Hortonian.setScalarName(ytitle);
        
        orderMap_Hortonian.setRange(0,myHortonStructure.getBasinOrder()+1);
        
        
        ConstantMap[] pointsCMap = {     new ConstantMap( 1.0f, Display.Red),
                new ConstantMap( 1.0f, Display.Green),
                new ConstantMap( 0.0f, Display.Blue),
                new ConstantMap( 4.50f, Display.PointSize)};
                
                display_Hortonian_Means.addReference( refeLineFit );
                
                display_Hortonian_Means.addReference( data_refH ,pointsCMap);
                
                ratioSpatialLabel.setText("Ratio = "+Math.round(Math.exp(Math.abs(ratios[0]))*1000)/1000.);
                rSquareSpatialLabel.setText("R^2 = "+Math.round(ratios[2]*1000)/1000.);
                
    }
    
    private void plotHortonDistributionHortonian(float[][] varDistrib) throws RemoteException, VisADException{
        
        display_Hortonian_Dists.removeAllReferences();
        
        data_refHDis_Hortonian= new DataReferenceImpl[varDistrib.length];
        
        for (int i=0;i<varDistrib.length;i++){
            
            float[][] thisDist=new float[1][varDistrib[i].length];
            
            
            hydroScalingAPI.util.statistics.Stats stats=new hydroScalingAPI.util.statistics.Stats(varDistrib[i]);
            java.util.Arrays.sort(varDistrib[i]);
            
            for (int j=0;j<varDistrib[i].length;j++)
                thisDist[0][j]=(float) (varDistrib[i][j]/stats.meanValue);
            
            Linear1DSet exedProbability = new Linear1DSet(exedProbab,0, 1,varDistrib[i].length);
            
            FlatField vals_ff_CDF = new FlatField( func_exProb_hortonVarOrder, exedProbability);
            vals_ff_CDF.setSamples( thisDist );
            
            data_refHDis_Hortonian[i]=new DataReferenceImpl("ditrib"+i);
            data_refHDis_Hortonian[i].setData(vals_ff_CDF);
            
            ConstantMap[] distCMap = {  new ConstantMap( red[i]/255., Display.Red),
                    new ConstantMap( green[i]/255., Display.Green),
                    new ConstantMap( blue[i]/255., Display.Blue),
                    new ConstantMap( (i+1),Display.LineWidth)};
                    
                    
                    display_Hortonian_Dists.addReference( data_refHDis_Hortonian[i],distCMap);
                    checkBoxperOrder1[i].setSelected(true);
        }
        
    }
    
    private void plotRSNTiles(int scale) throws RemoteException, VisADException{
        
        
        java.io.File hortFile=new java.io.File(metaDatos.getLocationBinaryFile().getPath().substring(0,metaDatos.getLocationBinaryFile().getPath().lastIndexOf("."))+".horton");
        metaDatos.setLocationBinaryFile(hortFile);
        metaDatos.setFormat("Byte");
        try{
            byte [][] matOrders=new hydroScalingAPI.io.DataRaster(metaDatos).getByte();
            
            metaDatos.restoreOriginalFormat();
            
            matrizPintada=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
            int[][] headsTails=myRSNAnalysis.getHeadsAndTails(scale);
            
            int maxColor=headsTails[0].length+1;
            float[][] estaTabla=new float[3][maxColor];
            
            for (int i=1;i<estaTabla[0].length;i++){
                estaTabla[0][i]=(float) (.8*Math.random()+.1);
                estaTabla[1][i]=(float) (.8*Math.random()+.1);
                estaTabla[2][i]=(float) (.8*Math.random()+.1);
            }
            
            ColorControl control = (ColorControl) varMapRSN.getControl();
            control.setTable(estaTabla);
            varMapRSN.setRange(0,maxColor);
            
            hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile myTileActual;
            
            for(int i=0;i<headsTails[0].length;i++){
                int xOulet=headsTails[0][i]%metaDatos.getNumCols();
                int yOulet=headsTails[0][i]/metaDatos.getNumCols();
                
                int xSource=headsTails[2][i]%metaDatos.getNumCols();
                int ySource=headsTails[2][i]/metaDatos.getNumCols();
                
                if(headsTails[3][i] == 0){
                    xSource=-1;
                    ySource=-1;
                }
                
                int tileColor=i+1;
                //System.out.println("Head: "+xOulet+","+yOulet+" Tail: "+xSource+","+ySource+" Color: "+tileColor+" Scale: "+(scale+1));
                
                myTileActual=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile(xOulet,yOulet,xSource,ySource,matDir,matOrders,metaDatos,scale+1);
                int elementsInTile=myTileActual.getXYRsnTile()[0].length;
                for (int j=0;j<elementsInTile;j++){
                    matrizPintada[myTileActual.getXYRsnTile()[1][j]-myCuenca.getMinY()+1][myTileActual.getXYRsnTile()[0][j]-myCuenca.getMinX()+1]=tileColor;
                }
            }
            
            float[][] matrizPintadaLarga=new float[1][matrizPintada.length*matrizPintada[0].length];
            
            for (int i=0;i<matrizPintada.length;i++){
                for (int j=0;j<matrizPintada[0].length;j++){
                    matrizPintadaLarga[0][i*matrizPintada[0].length+j]=matrizPintada[i][j];
                }
            }
            
            RealTupleType campo=new RealTupleType(RealType.Longitude,RealType.Latitude);
            FunctionType funcionTransfer = new FunctionType( campo, varRaster);
            Linear2DSet dominio = new Linear2DSet(campo,metaDatos.getMinLon()+(myCuenca.getMinX()-0.5)*metaDatos.getResLon()/3600.0,metaDatos.getMinLon()+(myCuenca.getMaxX()+1.5)*metaDatos.getResLon()/3600.0,myCuenca.getMaxX()-myCuenca.getMinX()+3,
                                                        metaDatos.getMinLat()+(myCuenca.getMinY()-0.5)*metaDatos.getResLat()/3600.0,metaDatos.getMinLat()+(myCuenca.getMaxY()+1.5)*metaDatos.getResLat()/3600.0,myCuenca.getMaxY()-myCuenca.getMinY()+3);
            
            FlatField valores = new FlatField( funcionTransfer, dominio);
            
            valores.setSamples( matrizPintadaLarga, false );
            
            data_ref_RSNTiles.setData(valores);
            
            
            
        } catch (java.io.IOException IOE){
            IOE.printStackTrace();
            return;
        }
        
    }
    
    private void exportRSNTiles(int scale,java.io.File dirOut) throws java.io.IOException{
        
        
        java.io.File hortFile=new java.io.File(metaDatos.getLocationBinaryFile().getPath().substring(0,metaDatos.getLocationBinaryFile().getPath().lastIndexOf("."))+".horton");
        metaDatos.setLocationBinaryFile(hortFile);
        metaDatos.setFormat("Byte");
        try{
            byte [][] matOrders=new hydroScalingAPI.io.DataRaster(metaDatos).getByte();
            
            metaDatos.restoreOriginalFormat();
            
            matrizPintada=new int[metaDatos.getNumRows()][metaDatos.getNumCols()];
            int[][] headsTails=myRSNAnalysis.getHeadsAndTails(scale);
            
            hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile myTileActual;
            
            for(int i=0;i<headsTails[0].length;i++){
                int xOulet=headsTails[0][i]%metaDatos.getNumCols();
                int yOulet=headsTails[0][i]/metaDatos.getNumCols();
                
                int xSource=headsTails[2][i]%metaDatos.getNumCols();
                int ySource=headsTails[2][i]/metaDatos.getNumCols();
                
                if(headsTails[3][i] == 0){
                    xSource=-1;
                    ySource=-1;
                }
                
                int tileColor=i+1;
                //System.out.println("Head: "+xOulet+","+yOulet+" Tail: "+xSource+","+ySource+" Color: "+tileColor+" Scale: "+(scale+1));
                
                myTileActual=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile(xOulet,yOulet,xSource,ySource,matDir,matOrders,metaDatos,scale+1);
                int elementsInTile=myTileActual.getXYRsnTile()[0].length;
                for (int j=0;j<elementsInTile;j++){
                    matrizPintada[myTileActual.getXYRsnTile()[1][j]][myTileActual.getXYRsnTile()[0][j]]=tileColor;
                }
            }
            
            hydroScalingAPI.io.MetaRaster maskMR=new hydroScalingAPI.io.MetaRaster(metaDatos);
            java.io.File saveFile1,saveFile2;
            saveFile1=new java.io.File(dirOut+"/"+metaDatos.getLocationMeta().getName().substring(0,metaDatos.getLocationMeta().getName().lastIndexOf("."))+"_BasinWatersheds_Level"+scale+".metaVHC");
            saveFile2=new java.io.File(dirOut+"/"+metaDatos.getLocationMeta().getName().substring(0,metaDatos.getLocationMeta().getName().lastIndexOf("."))+"_BasinWatersheds_Level"+scale+".vhc");
            
            maskMR.setLocationMeta(saveFile1);
            maskMR.setLocationBinaryFile(saveFile2);
            maskMR.setFormat("Integer");
            maskMR.writeMetaRaster(maskMR.getLocationMeta());

            java.io.DataOutputStream writer;
            writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile2)));

            for(int i=0;i<matrizPintada.length;i++) for(int j=0;j<matrizPintada[0].length;j++){
                writer.writeInt(matrizPintada[i][j]);
            }

            writer.close();
            
//            float[][] dtob=myRSNAnalysis.getVarValues(8,scale);
//            float[][] areas=myRSNAnalysis.getVarValues(0,scale);
//            float[][] lenght=myRSNAnalysis.getVarValues(1,scale);
//            
//            System.out.println("LinkID,lenght[km],area[km^2],TopoDistance[*]");
//            for (int i = 0; i < lenght[0].length; i++) {
//                System.out.println((i+1)+","+lenght[0][i]+","+areas[0][i]+","+dtob[0][i]);
//                
//            }
//            
        } catch (java.io.IOException IOE){
            IOE.printStackTrace();
            return;
        }
        
    }
    
    private void plotNetwork(DisplayImpl display,int scale) throws java.io.IOException, TypeException, VisADException{
        
        try {
            
            if(refeElemVec == null){
            
                hydroScalingAPI.io.MetaNetwork localNetwork=new hydroScalingAPI.io.MetaNetwork(metaDatos);
            
                int basOrder=myHortonStructure.getBasinOrder();

                refeElemVec=new visad.DataReferenceImpl[basOrder];
                
                for(int orderRequested=1;orderRequested<=basOrder;orderRequested++){
                    visad.UnionSet networkUnionSet = localNetwork.getUnionSet(orderRequested,netMask);

                    refeElemVec[orderRequested-1]=new visad.DataReferenceImpl("order"+orderRequested);
                    refeElemVec[orderRequested-1].setData(networkUnionSet);
                }
            } 
            
            int basOrder=myHortonStructure.getBasinOrder();
            for(int orderRequested=1;orderRequested<=basOrder;orderRequested++){
                try{
                    display.removeReference(refeElemVec[orderRequested-1]);
                } catch(visad.VisADException vie){}
            }
            for(int orderRequested=scale;orderRequested<=basOrder;orderRequested++){
                visad.ConstantMap[] lineCMap = {    new visad.ConstantMap( red[orderRequested-1]/255.0f, visad.Display.Red),
                                                    new visad.ConstantMap( green[orderRequested-1]/255.0f, visad.Display.Green),
                                                    new visad.ConstantMap( blue[orderRequested-1]/255.0f, visad.Display.Blue),
                                                    new visad.ConstantMap( 1.0, visad.Display.LineWidth)};

                display.addReference(refeElemVec[orderRequested-1],lineCMap);
            }
            
            
        } catch (visad.VisADException exc) {
            System.err.println("Failed showing streams");
            System.err.println(exc);
        } catch (java.io.IOException exc) {
            System.err.println("Failed showing streams");
            System.err.println(exc);
        }
        
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        buttonGroup3 = new javax.swing.ButtonGroup();
        buttonGroup4 = new javax.swing.ButtonGroup();
        buttonGroup5 = new javax.swing.ButtonGroup();
        buttonGroup7 = new javax.swing.ButtonGroup();
        varOptionsPopUp = new javax.swing.JPopupMenu();
        printVarValues = new javax.swing.JMenuItem();
        panelOpciones = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        widthTopolR = new javax.swing.JRadioButton();
        widthGeomR = new javax.swing.JRadioButton();
        widthElev = new javax.swing.JRadioButton();
        jPanel9 = new javax.swing.JPanel();
        jPanel10 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        binSizeTextField = new javax.swing.JTextField();
        binSizeSlider = new javax.swing.JSlider();
        jPanel11 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        HortonRatiosSplitPanel = new javax.swing.JSplitPane();
        jPanel13 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        minRegOrderLabel = new javax.swing.JLabel();
        minRegOrderSlider = new javax.swing.JSlider();
        jPanel16 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        maxRegOrderLabel = new javax.swing.JLabel();
        maxRegOrderSlider = new javax.swing.JSlider();
        jPanel22 = new javax.swing.JPanel();
        ratioLabel = new javax.swing.JLabel();
        rSquareLabel = new javax.swing.JLabel();
        jPanel20 = new javax.swing.JPanel();
        variableScrollPane = new javax.swing.JScrollPane();
        jPanel17 = new javax.swing.JPanel();
        jRadioButtonBranching = new javax.swing.JRadioButton();
        jRadioButtonLengthGeom = new javax.swing.JRadioButton();
        jRadioButtonLengthTopol = new javax.swing.JRadioButton();
        jRadioButtonArea = new javax.swing.JRadioButton();
        jRadioButtonMagnitude = new javax.swing.JRadioButton();
        jRadioButtonMaxLengthGeom = new javax.swing.JRadioButton();
        jRadioButtonMaxLengthTopol = new javax.swing.JRadioButton();
        jRadioButtonTotalLengthGeom = new javax.swing.JRadioButton();
        jRadioButtonSlopes = new javax.swing.JRadioButton();
        jRadioButtonTotalDrop = new javax.swing.JRadioButton();
        jRadioButtonMaxDrop = new javax.swing.JRadioButton();
        jRadioButtonStreamDrop = new javax.swing.JRadioButton();
        jPanel3 = new javax.swing.JPanel();
        HortonDistributionSplitPanel = new javax.swing.JSplitPane();
        jPanel18 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jPanel21 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        linksAnalysisSplitPane = new javax.swing.JSplitPane();
        jPanel30 = new javax.swing.JPanel();
        jPanel31 = new javax.swing.JPanel();
        jPanel36 = new javax.swing.JPanel();
        nonColored = new javax.swing.JRadioButton();
        magnColored = new javax.swing.JRadioButton();
        logMagnColored = new javax.swing.JRadioButton();
        areaColored = new javax.swing.JRadioButton();
        logAreaColored = new javax.swing.JRadioButton();
        hortColored = new javax.swing.JRadioButton();
        linkColoringLabel = new javax.swing.JLabel();
        jPanel37 = new javax.swing.JPanel();
        jPanel38 = new javax.swing.JPanel();
        histoBinSizeLabel = new javax.swing.JLabel();
        jPanel39 = new javax.swing.JPanel();
        histoBinSizeTextField = new javax.swing.JTextField();
        histoBinSizeSlider = new javax.swing.JSlider();
        jPanel40 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        xLog = new javax.swing.JCheckBox();
        includeInterior = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        yLog = new javax.swing.JCheckBox();
        includeExterior = new javax.swing.JCheckBox();
        jPanel7 = new javax.swing.JPanel();
        minXVarValueSlider = new javax.swing.JSlider();
        minYVarValueSlider = new javax.swing.JSlider();
        maxXVarValueSlider = new javax.swing.JSlider();
        maxYVarValueSlider = new javax.swing.JSlider();
        jPanel32 = new javax.swing.JPanel();
        jPanel34 = new javax.swing.JPanel();
        xAxisListLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        xvarList = new javax.swing.JList();
        jPanel35 = new javax.swing.JPanel();
        yAxisListLabel = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        yvarList = new javax.swing.JList();
        jPanel33 = new javax.swing.JPanel();
        histo_Mode = new javax.swing.JRadioButton();
        xy_mode = new javax.swing.JRadioButton();
        jPanel5 = new javax.swing.JPanel();
        jPanel50 = new javax.swing.JPanel();
        ddByLink = new javax.swing.JRadioButton();
        ddByOrder = new javax.swing.JRadioButton();
        jPanel6 = new javax.swing.JPanel();
        jPanel47 = new javax.swing.JPanel();
        rsnScaleSlider = new javax.swing.JSlider();
        jPanel48 = new javax.swing.JPanel();
        jPanel49 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jPanel23 = new javax.swing.JPanel();
        jSplitPane3 = new javax.swing.JSplitPane();
        jPanel24 = new javax.swing.JPanel();
        jPanel25 = new javax.swing.JPanel();
        jPanel43 = new javax.swing.JPanel();
        jPanel29 = new javax.swing.JPanel();
        jPanel44 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        minRegOrderHortonianLabel = new javax.swing.JLabel();
        minRegOrderHortonianSlider = new javax.swing.JSlider();
        jPanel45 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        maxRegOrderHortonianLabel = new javax.swing.JLabel();
        maxRegOrderHortonianSlider = new javax.swing.JSlider();
        jPanel41 = new javax.swing.JPanel();
        ratioSpatialLabel = new javax.swing.JLabel();
        rSquareSpatialLabel = new javax.swing.JLabel();
        jPanel42 = new javax.swing.JPanel();
        jPanel28 = new javax.swing.JPanel();
        meansRadioButton = new javax.swing.JRadioButton();
        distRadioButton = new javax.swing.JRadioButton();
        jPanel26 = new javax.swing.JPanel();
        jPanel27 = new javax.swing.JPanel();
        loadSpatialVariable = new javax.swing.JButton();
        jPanel51 = new javax.swing.JPanel();
        jPanel46 = new javax.swing.JPanel();
        jScrollPane4 = new javax.swing.JScrollPane();
        htmlGeomorphometricReport = new javax.swing.JEditorPane();

        printVarValues.setFont(new java.awt.Font("Lucida Grande", 0, 10));
        printVarValues.setText("Print Values");
        printVarValues.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                printVarValuesActionPerformed(evt);
            }
        });
        varOptionsPopUp.add(printVarValues);

        setTitle("River Network Structure Analysis");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        panelOpciones.setFont(new java.awt.Font("Dialog", 1, 10));
        panelOpciones.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                panelOpcionesStateChanged(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.GridLayout(1, 3));

        buttonGroup1.add(widthTopolR);
        widthTopolR.setFont(new java.awt.Font("Dialog", 0, 10));
        widthTopolR.setSelected(true);
        widthTopolR.setText("Topologic Width Function");
        widthTopolR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                widthTopolRActionPerformed(evt);
            }
        });
        jPanel8.add(widthTopolR);

        buttonGroup1.add(widthGeomR);
        widthGeomR.setFont(new java.awt.Font("Dialog", 0, 10));
        widthGeomR.setText("Geometric Width Function");
        widthGeomR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                widthGeomRActionPerformed(evt);
            }
        });
        jPanel8.add(widthGeomR);

        buttonGroup1.add(widthElev);
        widthElev.setFont(new java.awt.Font("Dialog", 0, 10));
        widthElev.setText("Link Concentration Function");
        widthElev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                widthElevActionPerformed(evt);
            }
        });
        jPanel8.add(widthElev);

        jPanel1.add(jPanel8, java.awt.BorderLayout.NORTH);

        jPanel9.setLayout(new java.awt.GridLayout(1, 2));

        jPanel10.setLayout(new java.awt.GridLayout(1, 2));

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel1.setText("Bin Size :");
        jPanel10.add(jLabel1);

        jPanel12.setLayout(new java.awt.GridLayout(2, 1));

        binSizeTextField.setFont(new java.awt.Font("Dialog", 0, 10));
        binSizeTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        binSizeTextField.setText("0.5 km");
        jPanel12.add(binSizeTextField);

        binSizeSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                binSizeSliderMouseReleased(evt);
            }
        });
        jPanel12.add(binSizeSlider);

        jPanel10.add(jPanel12);

        jPanel9.add(jPanel10);
        jPanel9.add(jPanel11);

        jPanel1.add(jPanel9, java.awt.BorderLayout.SOUTH);

        panelOpciones.addTab("Width Function", null, jPanel1, "");

        jPanel2.setLayout(new javax.swing.BoxLayout(jPanel2, javax.swing.BoxLayout.LINE_AXIS));

        jPanel13.setLayout(new java.awt.BorderLayout());

        jPanel14.setLayout(new java.awt.GridLayout(1, 2));

        jPanel15.setLayout(new java.awt.GridLayout(3, 1));

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel3.setText("Lower Order");
        jPanel15.add(jLabel3);

        minRegOrderLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        minRegOrderLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel15.add(minRegOrderLabel);

        minRegOrderSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                minRegOrderSliderStateChanged(evt);
            }
        });
        jPanel15.add(minRegOrderSlider);

        jPanel14.add(jPanel15);

        jPanel16.setLayout(new java.awt.GridLayout(3, 1));

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel5.setText("Upper Order");
        jPanel16.add(jLabel5);

        maxRegOrderLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        maxRegOrderLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jPanel16.add(maxRegOrderLabel);

        maxRegOrderSlider.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                maxRegOrderSliderStateChanged(evt);
            }
        });
        jPanel16.add(maxRegOrderSlider);

        jPanel14.add(jPanel16);

        jPanel13.add(jPanel14, java.awt.BorderLayout.SOUTH);

        jPanel22.setLayout(new java.awt.GridLayout(1, 2));

        ratioLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        ratioLabel.setText("Ratio = ");
        jPanel22.add(ratioLabel);

        rSquareLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        rSquareLabel.setText("R^2 = ");
        jPanel22.add(rSquareLabel);

        jPanel13.add(jPanel22, java.awt.BorderLayout.NORTH);

        HortonRatiosSplitPanel.setLeftComponent(jPanel13);

        jPanel20.setLayout(new java.awt.BorderLayout());

        jPanel17.setLayout(new java.awt.GridLayout(12, 1));

        buttonGroup2.add(jRadioButtonBranching);
        jRadioButtonBranching.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonBranching.setSelected(true);
        jRadioButtonBranching.setText("Branching Relation");
        jRadioButtonBranching.setEnabled(false);
        jRadioButtonBranching.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonBranchingActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonBranching);

        buttonGroup2.add(jRadioButtonLengthGeom);
        jRadioButtonLengthGeom.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonLengthGeom.setText("Geometric Length Relation");
        jRadioButtonLengthGeom.setEnabled(false);
        jRadioButtonLengthGeom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLengthGeomActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonLengthGeom);

        buttonGroup2.add(jRadioButtonLengthTopol);
        jRadioButtonLengthTopol.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonLengthTopol.setText("Topologic Length Relation");
        jRadioButtonLengthTopol.setEnabled(false);
        jRadioButtonLengthTopol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonLengthTopolActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonLengthTopol);

        buttonGroup2.add(jRadioButtonArea);
        jRadioButtonArea.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonArea.setText("Areas Relation");
        jRadioButtonArea.setEnabled(false);
        jRadioButtonArea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonAreaActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonArea);

        buttonGroup2.add(jRadioButtonMagnitude);
        jRadioButtonMagnitude.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonMagnitude.setText("Magnitude Relation");
        jRadioButtonMagnitude.setEnabled(false);
        jRadioButtonMagnitude.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMagnitudeActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonMagnitude);

        buttonGroup2.add(jRadioButtonMaxLengthGeom);
        jRadioButtonMaxLengthGeom.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonMaxLengthGeom.setText("Main Channel Gemoetric Length Relation");
        jRadioButtonMaxLengthGeom.setEnabled(false);
        jRadioButtonMaxLengthGeom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMaxLengthGeomActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonMaxLengthGeom);

        buttonGroup2.add(jRadioButtonMaxLengthTopol);
        jRadioButtonMaxLengthTopol.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonMaxLengthTopol.setText("Main Channel Topologic Length Relation");
        jRadioButtonMaxLengthTopol.setEnabled(false);
        jRadioButtonMaxLengthTopol.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMaxLengthTopolActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonMaxLengthTopol);

        buttonGroup2.add(jRadioButtonTotalLengthGeom);
        jRadioButtonTotalLengthGeom.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonTotalLengthGeom.setText("Total Channels Gemoetric Length Relation");
        jRadioButtonTotalLengthGeom.setEnabled(false);
        jRadioButtonTotalLengthGeom.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonTotalLengthGeomActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonTotalLengthGeom);

        buttonGroup2.add(jRadioButtonSlopes);
        jRadioButtonSlopes.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonSlopes.setText("Stream Slope");
        jRadioButtonSlopes.setEnabled(false);
        jRadioButtonSlopes.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonSlopesActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonSlopes);

        buttonGroup2.add(jRadioButtonTotalDrop);
        jRadioButtonTotalDrop.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonTotalDrop.setText("Total Cannels Drop");
        jRadioButtonTotalDrop.setEnabled(false);
        jRadioButtonTotalDrop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonTotalDropActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonTotalDrop);

        buttonGroup2.add(jRadioButtonMaxDrop);
        jRadioButtonMaxDrop.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonMaxDrop.setText("Maximum Drop Along Channels");
        jRadioButtonMaxDrop.setEnabled(false);
        jRadioButtonMaxDrop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonMaxDropActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonMaxDrop);

        buttonGroup2.add(jRadioButtonStreamDrop);
        jRadioButtonStreamDrop.setFont(new java.awt.Font("Dialog", 0, 10));
        jRadioButtonStreamDrop.setText("Stream Drop");
        jRadioButtonStreamDrop.setEnabled(false);
        jRadioButtonStreamDrop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButtonStreamDropActionPerformed(evt);
            }
        });
        jPanel17.add(jRadioButtonStreamDrop);

        variableScrollPane.setViewportView(jPanel17);

        jPanel20.add(variableScrollPane, java.awt.BorderLayout.CENTER);

        HortonRatiosSplitPanel.setRightComponent(jPanel20);

        jPanel2.add(HortonRatiosSplitPanel);

        panelOpciones.addTab("Horton Laws", null, jPanel2, "");

        jPanel3.setLayout(new java.awt.GridLayout(1, 1));

        jPanel18.setLayout(new java.awt.BorderLayout());

        jPanel19.setLayout(new java.awt.GridLayout(3, 4));
        jPanel18.add(jPanel19, java.awt.BorderLayout.SOUTH);

        HortonDistributionSplitPanel.setLeftComponent(jPanel18);

        jPanel21.setLayout(new java.awt.BorderLayout());
        HortonDistributionSplitPanel.setRightComponent(jPanel21);

        jPanel3.add(HortonDistributionSplitPanel);

        panelOpciones.addTab("CDFs Scaling", null, jPanel3, "");

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel30.setLayout(new java.awt.BorderLayout());

        jPanel31.setLayout(new java.awt.BorderLayout());

        buttonGroup7.add(nonColored);
        nonColored.setFont(new java.awt.Font("Dialog", 0, 10));
        nonColored.setSelected(true);
        nonColored.setText("None");
        nonColored.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nonColoredActionPerformed(evt);
            }
        });
        jPanel36.add(nonColored);

        buttonGroup7.add(magnColored);
        magnColored.setFont(new java.awt.Font("Dialog", 0, 10));
        magnColored.setText("By Magnitude");
        magnColored.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                magnColoredActionPerformed(evt);
            }
        });
        jPanel36.add(magnColored);

        buttonGroup7.add(logMagnColored);
        logMagnColored.setFont(new java.awt.Font("Dialog", 0, 10));
        logMagnColored.setText("By log-Magnitue");
        logMagnColored.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logMagnColoredActionPerformed(evt);
            }
        });
        jPanel36.add(logMagnColored);

        buttonGroup7.add(areaColored);
        areaColored.setFont(new java.awt.Font("Dialog", 0, 10));
        areaColored.setText("By Area");
        areaColored.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                areaColoredActionPerformed(evt);
            }
        });
        jPanel36.add(areaColored);

        buttonGroup7.add(logAreaColored);
        logAreaColored.setFont(new java.awt.Font("Dialog", 0, 10));
        logAreaColored.setText("By log-Area");
        logAreaColored.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                logAreaColoredActionPerformed(evt);
            }
        });
        jPanel36.add(logAreaColored);

        buttonGroup7.add(hortColored);
        hortColored.setFont(new java.awt.Font("Dialog", 0, 10));
        hortColored.setText("By Horton Order");
        hortColored.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hortColoredActionPerformed(evt);
            }
        });
        jPanel36.add(hortColored);

        jPanel31.add(jPanel36, java.awt.BorderLayout.CENTER);

        linkColoringLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        linkColoringLabel.setText("Link's Color Coding");
        jPanel31.add(linkColoringLabel, java.awt.BorderLayout.NORTH);

        jPanel30.add(jPanel31, java.awt.BorderLayout.SOUTH);

        jPanel37.setLayout(new java.awt.GridLayout(1, 2));

        jPanel38.setLayout(new java.awt.GridLayout(1, 2));

        histoBinSizeLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        histoBinSizeLabel.setText("Bin Size :");
        jPanel38.add(histoBinSizeLabel);

        jPanel39.setLayout(new java.awt.GridLayout(2, 1));

        histoBinSizeTextField.setFont(new java.awt.Font("Dialog", 0, 10));
        histoBinSizeTextField.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        histoBinSizeTextField.setText("0.1 km");
        jPanel39.add(histoBinSizeTextField);

        histoBinSizeSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                histoBinSizeSliderMouseReleased(evt);
            }
        });
        jPanel39.add(histoBinSizeSlider);

        jPanel38.add(jPanel39);

        jPanel37.add(jPanel38);

        jPanel40.setLayout(new java.awt.GridLayout(2, 3));

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel2.setText("Graphic Options");
        jPanel40.add(jLabel2);

        xLog.setFont(new java.awt.Font("Dialog", 0, 10));
        xLog.setText("Take Logs");
        xLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xLogActionPerformed(evt);
            }
        });
        jPanel40.add(xLog);

        includeInterior.setFont(new java.awt.Font("Dialog", 0, 10));
        includeInterior.setSelected(true);
        includeInterior.setText("Interiors");
        includeInterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeInteriorActionPerformed(evt);
            }
        });
        jPanel40.add(includeInterior);

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel4.setText("-");
        jPanel40.add(jLabel4);

        yLog.setFont(new java.awt.Font("Dialog", 0, 10));
        yLog.setText("y-log");
        yLog.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                yLogActionPerformed(evt);
            }
        });
        jPanel40.add(yLog);

        includeExterior.setFont(new java.awt.Font("Dialog", 0, 10));
        includeExterior.setSelected(true);
        includeExterior.setText("Exteriors");
        includeExterior.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                includeExteriorActionPerformed(evt);
            }
        });
        jPanel40.add(includeExterior);

        jPanel37.add(jPanel40);

        jPanel30.add(jPanel37, java.awt.BorderLayout.NORTH);

        jPanel7.setLayout(new java.awt.BorderLayout());

        minXVarValueSlider.setValue(0);
        minXVarValueSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                minXVarValueSliderMouseReleased(evt);
            }
        });
        jPanel7.add(minXVarValueSlider, java.awt.BorderLayout.SOUTH);

        minYVarValueSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        minYVarValueSlider.setValue(0);
        minYVarValueSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                minYVarValueSliderMouseReleased(evt);
            }
        });
        jPanel7.add(minYVarValueSlider, java.awt.BorderLayout.WEST);

        maxXVarValueSlider.setFont(new java.awt.Font("Dialog", 0, 10));
        maxXVarValueSlider.setValue(100);
        maxXVarValueSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                maxXVarValueSliderMouseReleased(evt);
            }
        });
        jPanel7.add(maxXVarValueSlider, java.awt.BorderLayout.NORTH);

        maxYVarValueSlider.setOrientation(javax.swing.JSlider.VERTICAL);
        maxYVarValueSlider.setValue(100);
        maxYVarValueSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                maxYVarValueSliderMouseReleased(evt);
            }
        });
        jPanel7.add(maxYVarValueSlider, java.awt.BorderLayout.EAST);

        jPanel30.add(jPanel7, java.awt.BorderLayout.CENTER);

        linksAnalysisSplitPane.setLeftComponent(jPanel30);

        jPanel32.setLayout(new java.awt.GridLayout(2, 1));

        jPanel34.setLayout(new java.awt.BorderLayout());

        xAxisListLabel.setText("Variable to Analize");
        jPanel34.add(xAxisListLabel, java.awt.BorderLayout.NORTH);

        xvarList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        xvarList.setSelectedIndex(0);
        xvarList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                xvarListMouseReleased(evt);
            }
        });
        jScrollPane1.setViewportView(xvarList);

        jPanel34.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel32.add(jPanel34);

        jPanel35.setLayout(new java.awt.BorderLayout());

        yAxisListLabel.setText("y-axis");
        jPanel35.add(yAxisListLabel, java.awt.BorderLayout.NORTH);

        yvarList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        yvarList.setSelectedIndex(0);
        yvarList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                yvarListMouseReleased(evt);
            }
        });
        jScrollPane2.setViewportView(yvarList);

        jPanel35.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel32.add(jPanel35);

        linksAnalysisSplitPane.setRightComponent(jPanel32);

        jPanel4.add(linksAnalysisSplitPane, java.awt.BorderLayout.CENTER);

        jPanel33.setLayout(new java.awt.GridLayout(1, 2));

        buttonGroup3.add(histo_Mode);
        histo_Mode.setFont(new java.awt.Font("Dialog", 0, 10));
        histo_Mode.setSelected(true);
        histo_Mode.setText("Histogram Mode");
        histo_Mode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                histo_ModeActionPerformed(evt);
            }
        });
        jPanel33.add(histo_Mode);

        buttonGroup3.add(xy_mode);
        xy_mode.setFont(new java.awt.Font("Dialog", 0, 10));
        xy_mode.setText("XY-Plot Mode");
        xy_mode.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                xy_modeActionPerformed(evt);
            }
        });
        jPanel33.add(xy_mode);

        jPanel4.add(jPanel33, java.awt.BorderLayout.NORTH);

        panelOpciones.addTab("Link Based Analysis", null, jPanel4, "");

        jPanel5.setLayout(new java.awt.BorderLayout());

        jPanel50.setLayout(new java.awt.GridLayout(1, 2));

        buttonGroup5.add(ddByLink);
        ddByLink.setFont(new java.awt.Font("Dialog", 0, 10));
        ddByLink.setSelected(true);
        ddByLink.setText("For Each Link");
        ddByLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ddByLinkActionPerformed(evt);
            }
        });
        jPanel50.add(ddByLink);

        buttonGroup5.add(ddByOrder);
        ddByOrder.setFont(new java.awt.Font("Dialog", 0, 10));
        ddByOrder.setText("For Each Horton Stream");
        ddByOrder.setEnabled(false);
        ddByOrder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ddByOrderActionPerformed(evt);
            }
        });
        jPanel50.add(ddByOrder);

        jPanel5.add(jPanel50, java.awt.BorderLayout.NORTH);

        panelOpciones.addTab("Drainage Density", null, jPanel5, "");

        jPanel6.setLayout(new java.awt.BorderLayout());

        jPanel47.setLayout(new java.awt.BorderLayout());

        rsnScaleSlider.setFont(new java.awt.Font("Dialog", 0, 10));
        rsnScaleSlider.setMajorTickSpacing(1);
        rsnScaleSlider.setMaximum(10);
        rsnScaleSlider.setMinimum(1);
        rsnScaleSlider.setPaintLabels(true);
        rsnScaleSlider.setPaintTicks(true);
        rsnScaleSlider.setSnapToTicks(true);
        rsnScaleSlider.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseReleased(java.awt.event.MouseEvent evt) {
                rsnScaleSliderMouseReleased(evt);
            }
        });
        jPanel47.add(rsnScaleSlider, java.awt.BorderLayout.SOUTH);

        jPanel6.add(jPanel47, java.awt.BorderLayout.CENTER);

        jPanel48.setLayout(new java.awt.GridLayout(2, 0));

        jPanel49.setLayout(new java.awt.BorderLayout());

        jButton1.setText("Export Current Watershed Partition");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });
        jPanel49.add(jButton1, java.awt.BorderLayout.NORTH);

        jPanel48.add(jPanel49);

        jList1.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jList1.setMaximumSize(new java.awt.Dimension(150, 0));
        jList1.setMinimumSize(new java.awt.Dimension(150, 0));
        jList1.setPreferredSize(new java.awt.Dimension(150, 0));
        jScrollPane3.setViewportView(jList1);

        jPanel48.add(jScrollPane3);

        jPanel6.add(jPanel48, java.awt.BorderLayout.EAST);

        panelOpciones.addTab("Self Similarity", null, jPanel6, "");

        jPanel23.setLayout(new java.awt.GridLayout(1, 1));

        jPanel24.setLayout(new java.awt.BorderLayout());

        jPanel25.setLayout(new java.awt.GridLayout(1, 1));

        jPanel43.setLayout(new java.awt.BorderLayout());

        jPanel29.setLayout(new java.awt.GridLayout(1, 2));

        jPanel44.setLayout(new java.awt.GridLayout(3, 1));

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel8.setText("Lower Order");
        jPanel44.add(jLabel8);

        minRegOrderHortonianLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        minRegOrderHortonianLabel.setEnabled(false);
        jPanel44.add(minRegOrderHortonianLabel);

        minRegOrderHortonianSlider.setEnabled(false);
        jPanel44.add(minRegOrderHortonianSlider);

        jPanel29.add(jPanel44);

        jPanel45.setLayout(new java.awt.GridLayout(3, 1));

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel10.setText("Upper Order");
        jPanel45.add(jLabel10);

        maxRegOrderHortonianLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        maxRegOrderHortonianLabel.setEnabled(false);
        jPanel45.add(maxRegOrderHortonianLabel);

        maxRegOrderHortonianSlider.setEnabled(false);
        jPanel45.add(maxRegOrderHortonianSlider);

        jPanel29.add(jPanel45);

        jPanel43.add(jPanel29, java.awt.BorderLayout.CENTER);

        jPanel41.setLayout(new java.awt.GridLayout(1, 2));

        ratioSpatialLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        ratioSpatialLabel.setText("Ratio = ");
        jPanel41.add(ratioSpatialLabel);

        rSquareSpatialLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        rSquareSpatialLabel.setText("R^2 = ");
        jPanel41.add(rSquareSpatialLabel);

        jPanel43.add(jPanel41, java.awt.BorderLayout.NORTH);

        jPanel25.add(jPanel43);

        jPanel42.setLayout(new java.awt.GridLayout(1, 0));
        jPanel25.add(jPanel42);

        jPanel24.add(jPanel25, java.awt.BorderLayout.SOUTH);

        jPanel28.setLayout(new java.awt.GridLayout(1, 2));

        buttonGroup4.add(meansRadioButton);
        meansRadioButton.setFont(new java.awt.Font("Dialog", 0, 10));
        meansRadioButton.setText("Means");
        meansRadioButton.setEnabled(false);
        meansRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                meansRadioButtonActionPerformed(evt);
            }
        });
        jPanel28.add(meansRadioButton);

        buttonGroup4.add(distRadioButton);
        distRadioButton.setFont(new java.awt.Font("Dialog", 0, 10));
        distRadioButton.setSelected(true);
        distRadioButton.setText("Distribution");
        distRadioButton.setEnabled(false);
        distRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                distRadioButtonActionPerformed(evt);
            }
        });
        jPanel28.add(distRadioButton);

        jPanel24.add(jPanel28, java.awt.BorderLayout.NORTH);

        jSplitPane3.setLeftComponent(jPanel24);

        jPanel26.setLayout(new java.awt.BorderLayout());

        jPanel27.setLayout(new java.awt.GridLayout(1, 0));
        jPanel26.add(jPanel27, java.awt.BorderLayout.SOUTH);

        loadSpatialVariable.setFont(new java.awt.Font("Dialog", 0, 10));
        loadSpatialVariable.setText("Select Variable");
        loadSpatialVariable.setEnabled(false);
        loadSpatialVariable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                loadSpatialVariableActionPerformed(evt);
            }
        });
        jPanel26.add(loadSpatialVariable, java.awt.BorderLayout.NORTH);

        jSplitPane3.setRightComponent(jPanel26);

        jPanel23.add(jSplitPane3);

        panelOpciones.addTab("Hortonian Analisis", null, jPanel23, "");

        jPanel51.setLayout(new java.awt.BorderLayout());
        panelOpciones.addTab("Hypsometric Curve", jPanel51);

        jPanel46.setLayout(new java.awt.BorderLayout());

        jScrollPane4.setViewportView(htmlGeomorphometricReport);

        jPanel46.add(jScrollPane4, java.awt.BorderLayout.CENTER);

        panelOpciones.addTab("HTML Report", jPanel46);

        getContentPane().add(panelOpciones, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void rsnScaleSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_rsnScaleSliderMouseReleased
         try{
            plotRSNTiles(rsnScaleSlider.getValue());
            plotNetwork(displayMap_RSNs, rsnScaleSlider.getValue());
        } catch (RemoteException r){
            System.err.print(r);
        } catch (VisADException v){
            System.err.print(v);
        } catch (java.io.IOException IOE){
            System.err.print(IOE);
        }
    }//GEN-LAST:event_rsnScaleSliderMouseReleased

    private void minYVarValueSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minYVarValueSliderMouseReleased
        minYVarValueCurrent=minYVarValue+minYVarValueSlider.getValue()/100.0f*(maxYVarValue-minYVarValue);
        if(histo_Mode.isSelected())
            rePlotLinksVarHistogram(binsizeHistogram);
        else
            rePlotXYvalues();
    }//GEN-LAST:event_minYVarValueSliderMouseReleased

    private void maxYVarValueSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxYVarValueSliderMouseReleased
        maxYVarValueCurrent=minYVarValue+maxYVarValueSlider.getValue()/100.0f*(maxYVarValue-minYVarValue);
        if(histo_Mode.isSelected())
            rePlotLinksVarHistogram(binsizeHistogram);
        else
            rePlotXYvalues();
    }//GEN-LAST:event_maxYVarValueSliderMouseReleased

    private void minXVarValueSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_minXVarValueSliderMouseReleased
        minXVarValueCurrent=minXVarValue+minXVarValueSlider.getValue()/100.0f*(maxXVarValue-minXVarValue);
        if(histo_Mode.isSelected())
            rePlotLinksVarHistogram(binsizeHistogram);
        else
            rePlotXYvalues();
    }//GEN-LAST:event_minXVarValueSliderMouseReleased

    private void maxXVarValueSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_maxXVarValueSliderMouseReleased
        maxXVarValueCurrent=minXVarValue+maxXVarValueSlider.getValue()/100.0f*(maxXVarValue-minXVarValue);
        if(histo_Mode.isSelected())
            rePlotLinksVarHistogram(binsizeHistogram);
        else
            rePlotXYvalues();
    }//GEN-LAST:event_maxXVarValueSliderMouseReleased

    private void includeExteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeExteriorActionPerformed
        if(!includeExterior.isSelected() && !includeInterior.isSelected()){
            try{
                display_Links.removeAllReferences();
                return;
            } catch (visad.VisADException vie){
                System.err.println(vie);
            } catch (RemoteException r){
                System.err.print(r);
            }
        }
        if(histo_Mode.isSelected())
            rePlotLinksVarHistogram(null);
        else
            rePlotXYvalues(true);
    }//GEN-LAST:event_includeExteriorActionPerformed

    private void includeInteriorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_includeInteriorActionPerformed
        if(!includeExterior.isSelected() && !includeInterior.isSelected()){
            try{
                display_Links.removeAllReferences();
                return;
            } catch (visad.VisADException vie){
                System.err.println(vie);
            } catch (RemoteException r){
                System.err.print(r);
            }
        }
        if(histo_Mode.isSelected())
            rePlotLinksVarHistogram(null);
        else
            rePlotXYvalues(true);
    }//GEN-LAST:event_includeInteriorActionPerformed

    private void logAreaColoredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logAreaColoredActionPerformed
        rePlotXYvalues();
    }//GEN-LAST:event_logAreaColoredActionPerformed

    private void areaColoredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_areaColoredActionPerformed
        rePlotXYvalues();
    }//GEN-LAST:event_areaColoredActionPerformed

    private void logMagnColoredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_logMagnColoredActionPerformed
        rePlotXYvalues();
    }//GEN-LAST:event_logMagnColoredActionPerformed

    private void widthElevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_widthElevActionPerformed
        try{
            plotWidthFunction(2);
        } catch (java.io.IOException IOE){
            System.err.println(IOE);
        } catch (VisADException v){
            System.err.println(v);
        }
    }//GEN-LAST:event_widthElevActionPerformed

    private void hortColoredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hortColoredActionPerformed
         rePlotXYvalues();
    }//GEN-LAST:event_hortColoredActionPerformed

    private void magnColoredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_magnColoredActionPerformed
         rePlotXYvalues();
    }//GEN-LAST:event_magnColoredActionPerformed

    private void nonColoredActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nonColoredActionPerformed
        rePlotXYvalues();
    }//GEN-LAST:event_nonColoredActionPerformed

    private void histoBinSizeSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_histoBinSizeSliderMouseReleased
        float binsize=histoBinSizeSlider.getValue()/5.0f*binsizeHistogram;
        
        histoBinSizeTextField.setText(""+binsize);
        
        rePlotLinksVarHistogram(binsize);

    }//GEN-LAST:event_histoBinSizeSliderMouseReleased

    private void jRadioButtonSlopesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonSlopesActionPerformed
        try{
            if (panelOpciones.getSelectedIndex()==1){
                updateHortonPlots();
            }
            
            if (panelOpciones.getSelectedIndex()==2){
                plotHortonDistribution(slopesDist);
            }
        } catch (RemoteException r){
            System.err.println(r);
        } catch (VisADException v){
            System.err.println(v);
        } catch (java.io.IOException IOE){
            System.err.println(IOE);
        }
    }//GEN-LAST:event_jRadioButtonSlopesActionPerformed

    private void yLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_yLogActionPerformed
        minYVarValueSlider.setValue(0);
        maxYVarValueSlider.setValue(100);
        minXVarValueSlider.setValue(0);
        maxXVarValueSlider.setValue(100);
        rePlotXYvalues(true);
    }//GEN-LAST:event_yLogActionPerformed

    private void xLogActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xLogActionPerformed
        minYVarValueSlider.setValue(0);
        maxYVarValueSlider.setValue(100);
        minXVarValueSlider.setValue(0);
        maxXVarValueSlider.setValue(100);
        if(histo_Mode.isSelected())
            rePlotLinksVarHistogram(null);
        else
            rePlotXYvalues(true);
    }//GEN-LAST:event_xLogActionPerformed

    private void xy_modeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_xy_modeActionPerformed
        histoBinSizeLabel.setEnabled(false);
        histoBinSizeTextField.setEnabled(false);
        histoBinSizeSlider.setEnabled(false);
        
        yvarList.setEnabled(true);
        linkColoringLabel.setEnabled(true);
        nonColored.setEnabled(true);
        magnColored.setEnabled(true);
        logMagnColored.setEnabled(true);
        areaColored.setEnabled(true);
        logAreaColored.setEnabled(true);
        hortColored.setEnabled(true);
        yAxisListLabel.setEnabled(true);
        yLog.setEnabled(true);
        xLog.setSelected(true);
        yLog.setSelected(true);
        
        minYVarValueSlider.setEnabled(true);
        maxYVarValueSlider.setEnabled(true);
        
        xLog.setText("x-log");
        xAxisListLabel.setText("x-axis");
        
        rePlotXYvalues(true);
    }//GEN-LAST:event_xy_modeActionPerformed

    private void histo_ModeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_histo_ModeActionPerformed
        histoBinSizeLabel.setEnabled(true);
        histoBinSizeTextField.setEnabled(true);
        histoBinSizeSlider.setEnabled(true);
        
        yvarList.setEnabled(false);
        linkColoringLabel.setEnabled(false);
        nonColored.setEnabled(false);
        magnColored.setEnabled(false);
        logMagnColored.setEnabled(false);
        areaColored.setEnabled(false);
        logAreaColored.setEnabled(false);
        hortColored.setEnabled(false);
        yAxisListLabel.setEnabled(false);
        yLog.setEnabled(false);
        xLog.setSelected(false);
        yLog.setSelected(false);
        minYVarValueSlider.setEnabled(false);
        maxYVarValueSlider.setEnabled(false);
        
        xLog.setText("take logs");
        xAxisListLabel.setText("Variable to Analize");
        
        rePlotLinksVarHistogram(null);

    }//GEN-LAST:event_histo_ModeActionPerformed

    private void jRadioButtonStreamDropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonStreamDropActionPerformed
        try{
            if (panelOpciones.getSelectedIndex()==1){
                updateHortonPlots();
            }
            
            if (panelOpciones.getSelectedIndex()==2){
                plotHortonDistribution(streamDropDist);
            }
        } catch (RemoteException r){
            System.err.println(r);
        } catch (VisADException v){
            System.err.println(v);
        } catch (java.io.IOException IOE){
            System.err.println(IOE);
        }
    }//GEN-LAST:event_jRadioButtonStreamDropActionPerformed

    private void jRadioButtonMaxDropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMaxDropActionPerformed
        try{
            if (panelOpciones.getSelectedIndex()==1){
                updateHortonPlots();
            }
            
            if (panelOpciones.getSelectedIndex()==2){
                plotHortonDistribution(maxDropDist);
            }
        } catch (RemoteException r){
            System.err.println(r);
        } catch (VisADException v){
            System.err.println(v);
        } catch (java.io.IOException IOE){
            System.err.println(IOE);
        }
    }//GEN-LAST:event_jRadioButtonMaxDropActionPerformed

    private void jRadioButtonTotalDropActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonTotalDropActionPerformed
        try{
            if (panelOpciones.getSelectedIndex()==1){
                updateHortonPlots();
            }
            
            if (panelOpciones.getSelectedIndex()==2){
                plotHortonDistribution(totalDropDist);
            }
        } catch (RemoteException r){
            System.err.println(r);
        } catch (VisADException v){
            System.err.println(v);
        } catch (java.io.IOException IOE){
            System.err.println(IOE);
        }
    }//GEN-LAST:event_jRadioButtonTotalDropActionPerformed

    private void ddByLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ddByLinkActionPerformed
        try{
            plotDdGraph(0);
        } catch (java.io.IOException IOE){
            System.err.println(IOE);
        } catch (VisADException v){
            System.err.println(v);
        }
    }//GEN-LAST:event_ddByLinkActionPerformed

    private void ddByOrderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ddByOrderActionPerformed
        try{
            plotDdGraph(1);
        } catch (java.io.IOException IOE){
            System.err.println(IOE);
        } catch (VisADException v){
            System.err.println(v);
        }
    }//GEN-LAST:event_ddByOrderActionPerformed
        
    private void distRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_distRadioButtonActionPerformed
        // Add your handling code here:
        jPanel24.remove(display_Hortonian_Means.getComponent());
        jPanel24.remove(jPanel25);
        jPanel25.remove(jPanel43);
        jPanel25.setLayout(new java.awt.GridLayout(1, 1));
        jPanel25.add(jPanel42);
        jPanel24.add("South",jPanel25);
        jPanel24.add("Center",display_Hortonian_Dists.getComponent());
        jSplitPane3.repaint();
        new visad.util.Delay(100);
        jSplitPane3.setDividerLocation(0.5);
    }//GEN-LAST:event_distRadioButtonActionPerformed
    
    private void meansRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_meansRadioButtonActionPerformed
        // Add your handling code here:
        
        jPanel24.remove(display_Hortonian_Dists.getComponent());
        jPanel24.remove(jPanel25);
        jPanel25.remove(jPanel42);
        jPanel25.setLayout(new java.awt.GridLayout(1, 1));
        jPanel25.add(jPanel43);
        jPanel24.add("South",jPanel25);
        jPanel24.add("Center",display_Hortonian_Means.getComponent());
        new visad.util.Delay(100);
        jSplitPane3.setDividerLocation(0.5);
    }//GEN-LAST:event_meansRadioButtonActionPerformed
    
    private void binSizeSliderMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_binSizeSliderMouseReleased
        // Add your handling code here:
        
        float binsize=binSizeSlider.getValue()*((widthTopolR.isSelected())?1:binsizeWidth);
        int metric=(widthTopolR.isSelected())?0:(widthGeomR.isSelected())?1:2;
        
        binSizeTextField.setText(binsize+" "+((widthTopolR.isSelected())?"Links":(widthGeomR.isSelected())?"km":"meters"));
        
        try{
            plotWidthFunction(metric,binsize);
        } catch (RemoteException r){
            System.err.print(r);
        } catch (VisADException v){
            System.err.print(v);
        } catch (java.io.IOException IOE){
            System.err.print(IOE);
        }
        
    }//GEN-LAST:event_binSizeSliderMouseReleased
    
    private void yvarListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_yvarListMouseReleased
        minYVarValueSlider.setValue(0);
        maxYVarValueSlider.setValue(100);
        minXVarValueSlider.setValue(0);
        maxXVarValueSlider.setValue(100);
        if(xy_mode.isSelected())
            rePlotXYvalues(true);
        
    }//GEN-LAST:event_yvarListMouseReleased
    
    private void xvarListMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_xvarListMouseReleased
        minYVarValueSlider.setValue(0);
        maxYVarValueSlider.setValue(100);
        minXVarValueSlider.setValue(0);
        maxXVarValueSlider.setValue(100);
        if(histo_Mode.isSelected())
            rePlotLinksVarHistogram(null);
        else
            rePlotXYvalues(true);
        
        if(evt.getButton() == 3){
            varOptionsPopUp.show(evt.getComponent(),evt.getX(),evt.getY());
        }
        
    }//GEN-LAST:event_xvarListMouseReleased
    
  private void loadSpatialVariableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_loadSpatialVariableActionPerformed
      
      javax.swing.JFileChooser fc=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersHydPath);
      fc.setFileSelectionMode(fc.FILES_ONLY);
      fc.setDialogTitle("Select Spatial Distributed Variable");
      javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("vhc","Hydrologic Variable");
      fc.addChoosableFileFilter(mdtFilter);
      fc.showOpenDialog(this);
      
      //En caso de que el usuario haya puesto cancelar
      
      if (fc.getSelectedFile() == null) return;
      if (!fc.getSelectedFile().isFile()) return;
      
      meansRadioButton.setEnabled(true);
      distRadioButton.setEnabled(true);
      minRegOrderHortonianLabel.setEnabled(true);
      minRegOrderHortonianSlider.setEnabled(true);
      maxRegOrderHortonianLabel.setEnabled(true);
      maxRegOrderHortonianSlider.setEnabled(true);
      
      try{
          
          displayMap_Hortonian.removeAllReferences();
          
          String theMeta=fc.getSelectedFile().getPath();
          theMeta=theMeta.substring(0,theMeta.indexOf("."))+".metaVHC";
          
          hydroScalingAPI.io.MetaRaster metaVHC=new hydroScalingAPI.io.MetaRaster(new java.io.File(theMeta));
          metaVHC.setLocationBinaryFile(fc.getSelectedFile());
          float[][] datosDeAca=new hydroScalingAPI.io.DataRaster(metaVHC).getFloatLine();
          
          RealTupleType campo=new RealTupleType(RealType.Longitude,RealType.Latitude);
          
          Linear2DSet dominio = new Linear2DSet(campo,metaVHC.getMinLon()+metaVHC.getResLon()/3600.0/2.0,metaVHC.getMinLon()+metaVHC.getNumCols()*metaVHC.getResLon()/3600.0-metaVHC.getResLon()/3600.0/2.0,metaVHC.getNumCols(),
          metaVHC.getMinLat()+metaVHC.getResLat()/3600.0/2.0,metaVHC.getMinLat()+metaVHC.getNumRows()*metaVHC.getResLat()/3600.0-metaVHC.getResLat()/3600.0/2.0,metaVHC.getNumRows());
          
          FunctionType funcionTransfer = new FunctionType( campo, varRaster);
          
          FlatField valores = new FlatField( funcionTransfer, dominio);
          
          valores.setSamples( datosDeAca, false );
          
          data_ref_varRaster = new DataReferenceImpl("data_ref_varRaster");
          data_ref_varRaster.setData(valores);
          
          displayMap_Hortonian.addReference(data_ref_varRaster);
          
          ProjectionControl pc = displayMap_Hortonian.getProjectionControl();
          pc.setAspectCartesian(new double[] {1.0, (double) (metaDatos.getNumRows()/(double) metaDatos.getNumCols())});
          
          for (int i=data_refSubBasins.length-4;i<data_refSubBasins.length;i++) {
              ConstantMap[] distCMap = {  new ConstantMap( red[i]/255., Display.Red),
              new ConstantMap( green[i]/255., Display.Green),
              new ConstantMap( blue[i]/255., Display.Blue),
              new ConstantMap( (i+2),Display.LineWidth)};
              displayMap_Hortonian.addReference(data_refSubBasins[i],distCMap);
              checkBoxperOrder1[i].setEnabled(true);
              checkBoxperOrder2[i].setEnabled(true);
              checkBoxperOrder1[i].setSelected(true);
              checkBoxperOrder2[i].setSelected(true);
          }
          
          getSpatialVarDist(valores);
          
          float[] ratios=myHortonStructure.getQuantityRatio(minRegOrderSlider.getValue(),maxRegOrderSlider.getValue(),spatialVarDist);
          float[] NLvsOrder=myHortonStructure.getLengthPerOrder(spatialVarDist);
          plotHortonDistributionHortonian(spatialVarDist);
          plotHortonGraphHortonian(ratios,NLvsOrder,"Log(Spatial Variable)");
          
      } catch (VisADException v){
          v.printStackTrace();
      } catch (java.io.IOException ioe){
          ioe.printStackTrace();
      }
      
      
  }//GEN-LAST:event_loadSpatialVariableActionPerformed
  
  private void panelOpcionesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_panelOpcionesStateChanged
      
      if (firstTouchTab[0]){
          jPanel1.remove(displayW.getComponent());
          jPanel13.remove(displayH.getComponent());
          jPanel18.remove(displayCDF.getComponent());
          jPanel5.remove(displayDD.getComponent());
          
          jPanel24.remove(display_Hortonian_Means.getComponent());
          jPanel24.remove(display_Hortonian_Dists.getComponent());
          jPanel26.remove(displayMap_Hortonian.getComponent());
          
          jPanel47.remove(displayMap_RSNs.getComponent());
          
          jPanel7.remove(display_Links.getComponent());
          
          jPanel51.remove(displayHC.getComponent());
          
          if ( panelOpciones.getSelectedIndex()==0) {
              jPanel1.add("Center",displayW.getComponent());
          }
          
          if ( panelOpciones.getSelectedIndex()==1) {
              jPanel13.add("Center",displayH.getComponent());
              jPanel20.add(variableScrollPane);
              jRadioButtonBranching.setEnabled(true);
              jRadioButtonBranching.setSelected(true);
              updateHortonPlots();
              if (!firstTouchTab[1]){
                  HortonRatiosSplitPanel.setDividerLocation(0.65);
                  firstTouchTab[1]=true;
              }
          }
          
          if ( panelOpciones.getSelectedIndex()==2) {
              
              jPanel18.add("Center",displayCDF.getComponent());
              jPanel21.add(variableScrollPane);
              
              
              jRadioButtonBranching.setEnabled(false);
              jRadioButtonLengthGeom.setSelected(true);
              
              if (!firstTouchTab[2]){
                  HortonDistributionSplitPanel.setDividerLocation(0.65);
                  try{
                      float[][] varDistrib=myHortonStructure.getLengthDistributionPerOrder(0);
                      plotHortonDistribution(varDistrib);
                  } catch (RemoteException r){
                      System.err.print(r);
                  } catch (VisADException v){
                      System.err.print(v);
                  } catch (java.io.IOException IOE){
                      System.err.print(IOE);
                  }
                  firstTouchTab[2]=true;
              }
              
          }
          
          if ( panelOpciones.getSelectedIndex()==3) {
              jPanel7.add("Center",display_Links.getComponent());
              
              if (!firstTouchTab[3]){
                  linksAnalysisSplitPane.setDividerLocation(0.7);
                  rePlotLinksVarHistogram(null);
                  firstTouchTab[3]=true;
              }
          }
          
          if ( panelOpciones.getSelectedIndex()==4) {
              jPanel5.add("Center",displayDD.getComponent());
              
              if (!firstTouchTab[4]){
                  try{
                      plotDdGraph(0);
                  } catch (RemoteException r){
                      System.err.print(r);
                  } catch (VisADException v){
                      System.err.print(v);
                  } catch (java.io.IOException IOE){
                      System.err.print(IOE);
                  }
                  firstTouchTab[4]=true;
              }
          }
          
          if ( panelOpciones.getSelectedIndex()==5) {
              
              jPanel47.add("Center",displayMap_RSNs.getComponent());
              
          }
          
          if ( panelOpciones.getSelectedIndex()==6) {
              
              if(meansRadioButton.isSelected())
                  jPanel24.add("Center",display_Hortonian_Means.getComponent());
              else jPanel24.add("Center",display_Hortonian_Dists.getComponent());
              
              jPanel26.add("Center",displayMap_Hortonian.getComponent());
              
          }
          
          if ( panelOpciones.getSelectedIndex()==7) {
              jPanel51.add("Center",displayHC.getComponent());
              if (!firstTouchTab[7]){
                  try{
                      plotHypCurve();
                  } catch (RemoteException r){
                      System.err.print(r);
                  } catch (VisADException v){
                      System.err.print(v);
                  } catch (java.io.IOException IOE){
                      System.err.print(IOE);
                  }
                  firstTouchTab[7]=true;
              }
          }
          
      }
      
  }//GEN-LAST:event_panelOpcionesStateChanged
  
  private void maxRegOrderSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_maxRegOrderSliderStateChanged
      
      if (panelOpciones.getSelectedIndex()!=1) return;
      
      maxRegOrderLabel.setText(""+maxRegOrderSlider.getValue());
      
      updateHortonPlots();
      
  }//GEN-LAST:event_maxRegOrderSliderStateChanged
  
  private void minRegOrderSliderStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_minRegOrderSliderStateChanged
      
      if (panelOpciones.getSelectedIndex()!=1) return;
      
      minRegOrderLabel.setText(""+minRegOrderSlider.getValue());
      
      updateHortonPlots();
      
  }//GEN-LAST:event_minRegOrderSliderStateChanged
  
  private void jRadioButtonTotalLengthGeomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonTotalLengthGeomActionPerformed
      
      try{
          if (panelOpciones.getSelectedIndex()==1){
              updateHortonPlots();
          }
          
          if (panelOpciones.getSelectedIndex()==2){
              plotHortonDistribution(totalGeomLengthDist);
          }
      } catch (RemoteException r){
          System.err.println(r);
      } catch (VisADException v){
          System.err.println(v);
      } catch (java.io.IOException IOE){
          System.err.println(IOE);
      }
      
  }//GEN-LAST:event_jRadioButtonTotalLengthGeomActionPerformed
  
  private void jRadioButtonMaxLengthTopolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMaxLengthTopolActionPerformed
      
      try{
          if (panelOpciones.getSelectedIndex()==1){
              updateHortonPlots();
          }
          
          if (panelOpciones.getSelectedIndex()==2){
              plotHortonDistribution(mainTopolLengthDist);
          }
      } catch (RemoteException r){
          System.err.println(r);
      } catch (VisADException v){
          System.err.println(v);
      } catch (java.io.IOException IOE){
          System.err.println(IOE);
      }
      
  }//GEN-LAST:event_jRadioButtonMaxLengthTopolActionPerformed
  
  private void jRadioButtonMaxLengthGeomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMaxLengthGeomActionPerformed
      
      try{
          if (panelOpciones.getSelectedIndex()==1){
              updateHortonPlots();
          }
          
          if (panelOpciones.getSelectedIndex()==2){
              plotHortonDistribution(mainGeomLengthDist);
          }
      } catch (RemoteException r){
          System.err.println(r);
      } catch (VisADException v){
          System.err.println(v);
      } catch (java.io.IOException IOE){
          System.err.println(IOE);
      }
      
  }//GEN-LAST:event_jRadioButtonMaxLengthGeomActionPerformed
  
  private void jRadioButtonMagnitudeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonMagnitudeActionPerformed
      
      try{
          if (panelOpciones.getSelectedIndex()==1){
              updateHortonPlots();
          }
          
          if (panelOpciones.getSelectedIndex()==2){
              plotHortonDistribution(magnDist);
          }
      } catch (RemoteException r){
          System.err.println(r);
      } catch (VisADException v){
          System.err.println(v);
      } catch (java.io.IOException IOE){
          System.err.println(IOE);
      }
      
  }//GEN-LAST:event_jRadioButtonMagnitudeActionPerformed
  
  private void jRadioButtonAreaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonAreaActionPerformed
      
      try{
          if (panelOpciones.getSelectedIndex()==1){
              updateHortonPlots();
          }
          
          if (panelOpciones.getSelectedIndex()==2){
              plotHortonDistribution(areasDist);
          }
      } catch (RemoteException r){
          System.err.println(r);
      } catch (VisADException v){
          System.err.println(v);
      } catch (java.io.IOException IOE){
          System.err.println(IOE);
      }
      
  }//GEN-LAST:event_jRadioButtonAreaActionPerformed
  
  private void jRadioButtonLengthTopolActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLengthTopolActionPerformed
      
      try{
          if (panelOpciones.getSelectedIndex()==1){
              updateHortonPlots();
          }
          
          if (panelOpciones.getSelectedIndex()==2){
              plotHortonDistribution(topolLengthDist);
          }
      } catch (RemoteException r){
          System.err.println(r);
      } catch (VisADException v){
          System.err.println(v);
      } catch (java.io.IOException IOE){
          System.err.println(IOE);
      }
      
  }//GEN-LAST:event_jRadioButtonLengthTopolActionPerformed
  
  private void jRadioButtonLengthGeomActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonLengthGeomActionPerformed
      
      try{
          if (panelOpciones.getSelectedIndex()==1){
              updateHortonPlots();
          }
          
          if (panelOpciones.getSelectedIndex()==2){
              plotHortonDistribution(geomLengthDist);
          }
      } catch (RemoteException r){
          System.err.println(r);
      } catch (VisADException v){
          System.err.println(v);
      } catch (java.io.IOException IOE){
          System.err.println(IOE);
      }
      
  }//GEN-LAST:event_jRadioButtonLengthGeomActionPerformed
  
  private void jRadioButtonBranchingActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButtonBranchingActionPerformed
      updateHortonPlots();
  }//GEN-LAST:event_jRadioButtonBranchingActionPerformed
  
  private void widthGeomRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_widthGeomRActionPerformed
      
      try{
          plotWidthFunction(1);
      } catch (java.io.IOException IOE){
          System.err.println(IOE);
      } catch (VisADException v){
          System.err.println(v);
      }
}//GEN-LAST:event_widthGeomRActionPerformed
  
  private void widthTopolRActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_widthTopolRActionPerformed
      
      try{
          plotWidthFunction(0);
      } catch (java.io.IOException IOE){
          System.err.println(IOE);
      } catch (VisADException v){
          System.err.println(v);
      }
}//GEN-LAST:event_widthTopolRActionPerformed
  
  /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

    private void printVarValuesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_printVarValuesActionPerformed
        
        
        try {

            String stringOfText="";

            stringOfText=(String)xvarList.getSelectedValue()+"<br>";

            float[][] varValues = getFilteredLinkValues(false);
            for (int i = 0; i < varValues[0].length; i++) {
               stringOfText+=varValues[0][i]+"<br>";

            }

            htmlGeomorphometricReport.setText(stringOfText);

        } catch (IOException iOException) {
            iOException.printStackTrace();
        }

}//GEN-LAST:event_printVarValuesActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        try{
            javax.swing.JFileChooser fc=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersHydPath);
            fc.setFileSelectionMode(fc.DIRECTORIES_ONLY);
            fc.setDialogTitle("Directory Selection");
            fc.showOpenDialog(this);

            if (fc.getSelectedFile() == null) return;
        
            exportRSNTiles(rsnScaleSlider.getValue(), fc.getSelectedFile());
            
            closeDialog(null);
        } catch (java.io.IOException IOE){
            System.err.println("Failed creating mask file for this basin. ");
            System.err.println(IOE);
        } 
        
            
    }//GEN-LAST:event_jButton1ActionPerformed
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            //java.io.File theFile=new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Topography/3_ArcSec/TestCases/NewHS/testdem.metaDEM");
            //hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            //metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Topography/3_ArcSec/TestCases/NewHS/testdem.dir"));
            
            //java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.metaDEM");
            //hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            //metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.dir"));
            
//            java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
            
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Gila_River_DB/Rasters/Topography/1_ArcSec/mogollon.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Gila_River_DB/Rasters/Topography/1_ArcSec/mogollon.dir"));
            
//            java.io.File theFile=new java.io.File("/hidrosigDataBases/Rio Salado DB/Rasters/Topography/NED_26084992.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Rio Salado DB/Rasters/Topography/NED_26084992.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
            
            //new BasinAnalyzer(tempFrame,2,96,matDirs,metaModif).setVisible(true);
            //new BasinAnalyzer(tempFrame,1063,496,matDirs,metaModif).show();
            //new BasinAnalyzer(tempFrame,82,260,matDirs,metaModif).setVisible(true);
            new BasinAnalyzer(tempFrame,282,298,matDirs,metaModif).setVisible(true);
            //new BasinAnalyzer(tempFrame,5173,1252,matDirs,metaModif).setVisible(true);
            
        } catch (java.io.IOException IOE){
            System.err.println(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.err.println(v);
            System.exit(0);
        }
        
        
        System.exit(0);
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane HortonDistributionSplitPanel;
    private javax.swing.JSplitPane HortonRatiosSplitPanel;
    private javax.swing.JRadioButton areaColored;
    private javax.swing.JSlider binSizeSlider;
    private javax.swing.JTextField binSizeTextField;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.ButtonGroup buttonGroup3;
    private javax.swing.ButtonGroup buttonGroup4;
    private javax.swing.ButtonGroup buttonGroup5;
    private javax.swing.ButtonGroup buttonGroup7;
    private javax.swing.JRadioButton ddByLink;
    private javax.swing.JRadioButton ddByOrder;
    private javax.swing.JRadioButton distRadioButton;
    private javax.swing.JLabel histoBinSizeLabel;
    private javax.swing.JSlider histoBinSizeSlider;
    private javax.swing.JTextField histoBinSizeTextField;
    private javax.swing.JRadioButton histo_Mode;
    private javax.swing.JRadioButton hortColored;
    private javax.swing.JEditorPane htmlGeomorphometricReport;
    private javax.swing.JCheckBox includeExterior;
    private javax.swing.JCheckBox includeInterior;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
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
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioButtonArea;
    private javax.swing.JRadioButton jRadioButtonBranching;
    private javax.swing.JRadioButton jRadioButtonLengthGeom;
    private javax.swing.JRadioButton jRadioButtonLengthTopol;
    private javax.swing.JRadioButton jRadioButtonMagnitude;
    private javax.swing.JRadioButton jRadioButtonMaxDrop;
    private javax.swing.JRadioButton jRadioButtonMaxLengthGeom;
    private javax.swing.JRadioButton jRadioButtonMaxLengthTopol;
    private javax.swing.JRadioButton jRadioButtonSlopes;
    private javax.swing.JRadioButton jRadioButtonStreamDrop;
    private javax.swing.JRadioButton jRadioButtonTotalDrop;
    private javax.swing.JRadioButton jRadioButtonTotalLengthGeom;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JSplitPane jSplitPane3;
    private javax.swing.JLabel linkColoringLabel;
    private javax.swing.JSplitPane linksAnalysisSplitPane;
    private javax.swing.JButton loadSpatialVariable;
    private javax.swing.JRadioButton logAreaColored;
    private javax.swing.JRadioButton logMagnColored;
    private javax.swing.JRadioButton magnColored;
    private javax.swing.JLabel maxRegOrderHortonianLabel;
    private javax.swing.JSlider maxRegOrderHortonianSlider;
    private javax.swing.JLabel maxRegOrderLabel;
    private javax.swing.JSlider maxRegOrderSlider;
    private javax.swing.JSlider maxXVarValueSlider;
    private javax.swing.JSlider maxYVarValueSlider;
    private javax.swing.JRadioButton meansRadioButton;
    private javax.swing.JLabel minRegOrderHortonianLabel;
    private javax.swing.JSlider minRegOrderHortonianSlider;
    private javax.swing.JLabel minRegOrderLabel;
    private javax.swing.JSlider minRegOrderSlider;
    private javax.swing.JSlider minXVarValueSlider;
    private javax.swing.JSlider minYVarValueSlider;
    private javax.swing.JRadioButton nonColored;
    private javax.swing.JTabbedPane panelOpciones;
    private javax.swing.JMenuItem printVarValues;
    private javax.swing.JLabel rSquareLabel;
    private javax.swing.JLabel rSquareSpatialLabel;
    private javax.swing.JLabel ratioLabel;
    private javax.swing.JLabel ratioSpatialLabel;
    private javax.swing.JSlider rsnScaleSlider;
    private javax.swing.JPopupMenu varOptionsPopUp;
    private javax.swing.JScrollPane variableScrollPane;
    private javax.swing.JRadioButton widthElev;
    private javax.swing.JRadioButton widthGeomR;
    private javax.swing.JRadioButton widthTopolR;
    private javax.swing.JLabel xAxisListLabel;
    private javax.swing.JCheckBox xLog;
    private javax.swing.JList xvarList;
    private javax.swing.JRadioButton xy_mode;
    private javax.swing.JLabel yAxisListLabel;
    private javax.swing.JCheckBox yLog;
    private javax.swing.JList yvarList;
    // End of variables declaration//GEN-END:variables
    
    private void getSpatialVarDist(FlatField valores){
        
        try{
            spatialVarDist=new float[myHortonStructure.getBasinOrder()][];
            
            RealTupleType campo=new RealTupleType(RealType.Longitude,RealType.Latitude);
            
            int MatX,MatY,numCols;
            float[][] misCoordenadas;
            
            numCols=metaDatos.getNumCols();
            
            for(int i=0;i<myHortonStructure.headsArray.length;i++){
                spatialVarDist[i]=new float[myHortonStructure.headsArray[i].length];
                for(int j=0;j<myHortonStructure.headsArray[i].length;j++){
                    MatX=myHortonStructure.contactsArray[i][j]%numCols;
                    MatY=myHortonStructure.contactsArray[i][j]/numCols;
                    
                    misCoordenadas=new hydroScalingAPI.util.geomorphology.objects.Basin(MatX,MatY,matDir,metaDatos).getLonLatBasin();
                    for(int k=0;k<misCoordenadas[0].length;k++) {
                        spatialVarDist[i][j]+=Float.parseFloat(valores.evaluate(new RealTuple(campo,new double[] {misCoordenadas[0][k],misCoordenadas[1][k]})).toString());
                    }
                }
                System.out.println(java.util.Arrays.toString(spatialVarDist[i]));
            }
        } catch (java.io.IOException IOE){
            IOE.printStackTrace();
        } catch (VisADException v){
            v.printStackTrace();
        }
        
    }
    
    /**
     * Allows external Threads to inform the Module if the Horton analysis for a given
     * variable has been completed
     * @param i The radioButton to activate
     */
    public void enableRadioButton(int i){
        switch(i){
            case 0:
                jRadioButtonBranching.setEnabled(true);
                break;
            case 1:
                jRadioButtonLengthGeom.setEnabled(true);
                break;
            case 2:
                jRadioButtonLengthTopol.setEnabled(true);
                break;
            case 3:
                jRadioButtonArea.setEnabled(true);
                break;
            case 4:
                jRadioButtonMagnitude.setEnabled(true);
                break;
            case 5:
                jRadioButtonMaxLengthGeom.setEnabled(true);
                break;
            case 6:
                jRadioButtonMaxLengthTopol.setEnabled(true);
                break;
            case 7:
                jRadioButtonTotalLengthGeom.setEnabled(true);
                break;
            case 8:
                jRadioButtonSlopes.setEnabled(true);
                break;
            case 9:
                jRadioButtonTotalDrop.setEnabled(true);
                break;
            case 10:
                jRadioButtonMaxDrop.setEnabled(true);
                break;
            case 11:
                jRadioButtonStreamDrop.setEnabled(true);
                break;
            case 100:
                ddByOrder.setEnabled(true);
                break;
        }
        
    }
    private void jCheckBoxOrder0ActionPerformed(java.awt.event.ActionEvent evt) {
        
        javax.swing.JCheckBox item=(javax.swing.JCheckBox) evt.getSource();
        
        int i=new Integer(item.getText().substring(6)).intValue()-1;
        
        try{
            ConstantMap[] distCMap = {new ConstantMap( red[i]/255., Display.Red),
            new ConstantMap( green[i]/255., Display.Green),
            new ConstantMap( blue[i]/255., Display.Blue),
            new ConstantMap( (i+1),Display.LineWidth)};
            
            if (item.isSelected())
                displayCDF.addReference(data_refHDis[i],distCMap);
            else
                displayCDF.removeReference(data_refHDis[i]);
        } catch (java.io.IOException IOE){
            IOE.printStackTrace();
        } catch (VisADException v){
            v.printStackTrace();
        }
    }
    
    private void jCheckBoxOrder1ActionPerformed(java.awt.event.ActionEvent evt){
        javax.swing.JCheckBox item=(javax.swing.JCheckBox) evt.getSource();
        int i=new Integer(item.getText().substring(6)).intValue()-1;
        
        try{
            ConstantMap[] distCMap = {new ConstantMap( red[i]/255., Display.Red),
            new ConstantMap( green[i]/255., Display.Green),
            new ConstantMap( blue[i]/255., Display.Blue),
            new ConstantMap( (i+1),Display.LineWidth)};
            
            if (item.isSelected())
                display_Hortonian_Dists.addReference(data_refHDis_Hortonian[i],distCMap);
            else
                display_Hortonian_Dists.removeReference(data_refHDis_Hortonian[i]);
        } catch (java.io.IOException IOE){
            IOE.printStackTrace();
        } catch (VisADException v){
            v.printStackTrace();
        }
    }
    
    private void jCheckBoxOrder2ActionPerformed(java.awt.event.ActionEvent evt){
        javax.swing.JCheckBox item=(javax.swing.JCheckBox) evt.getSource();
        int i=new Integer(item.getText().substring(6)).intValue()-1;
        
        try{
            ConstantMap[] distCMap = {new ConstantMap( red[i]/255., Display.Red),
            new ConstantMap( green[i]/255., Display.Green),
            new ConstantMap( blue[i]/255., Display.Blue),
            new ConstantMap( (i+1),Display.LineWidth)};
            
            if (item.isSelected())
                displayMap_Hortonian.addReference(data_refSubBasins[i],distCMap);
            else
                displayMap_Hortonian.removeReference(data_refSubBasins[i]);
        } catch (java.io.IOException IOE){
            IOE.printStackTrace();
        } catch (VisADException v){
            v.printStackTrace();
        }
    }

    /**
     * send a DisplayEvent to this DisplayListener
     * 
     * @param DispEvt DisplayEvent to send
     * @throws VisADException a VisAD error occurred
     * @throws RemoteException an RMI error occurred
     */
    public void displayChanged(DisplayEvent DispEvt) throws VisADException, RemoteException {
        
        int id = DispEvt.getId();
        
        try {
            if (id == DispEvt.MOUSE_RELEASED_RIGHT) {
                
                visad.VisADRay ray = displayMap_RSNs.getDisplayRenderer().getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());
                
                float resultX= lonMapRSN.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                float resultY= latMapRSN.inverseScaleValues(new float[] {(float)ray.position[1]})[0];
                
                
                
                int MatX=(int) ((resultX -(metaDatos.getMinLon()+(myCuenca.getMinX()-1)*metaDatos.getResLon()/3600.0))/(float) metaDatos.getResLon()*3600.0f);
                int MatY=(int) ((resultY -(metaDatos.getMinLat()+(myCuenca.getMinY()-1)*metaDatos.getResLat()/3600.0))/(float) metaDatos.getResLat()*3600.0f);
                
                System.out.println(matrizPintada[MatY][MatX]);
                
            }
            
        } catch (Exception e) {
            System.err.println(e);
        }
        
    }
}

class ExtractSubBasins extends Thread{
    
    hydroScalingAPI.modules.networkAnalysis.widgets.BasinAnalyzer parentAnalyzer;
    
    public ExtractSubBasins(hydroScalingAPI.modules.networkAnalysis.widgets.BasinAnalyzer parent){
        
        parentAnalyzer=parent;
        
        
    }
    
    public void run(){
        
        try{
            
            parentAnalyzer.data_refSubBasins=new DataReferenceImpl[parentAnalyzer.myHortonStructure.headsArray.length];
            
            RealTupleType campo=new RealTupleType(RealType.Longitude,RealType.Latitude);
            
            int numCols=parentAnalyzer.metaDatos.getNumCols();
            
            for (int i=0;i<parentAnalyzer.myHortonStructure.headsArray.length;i++){
                
                Gridded2DSet[] thisOrderSubBasins=new Gridded2DSet[parentAnalyzer.myHortonStructure.headsArray[i].length];
                
                for (int j=0;j<parentAnalyzer.myHortonStructure.headsArray[i].length;j++){
                    
                    int MatX=parentAnalyzer.myHortonStructure.contactsArray[i][j]%numCols;
                    int MatY=parentAnalyzer.myHortonStructure.contactsArray[i][j]/numCols;
                    
                    thisOrderSubBasins[j]=new hydroScalingAPI.util.geomorphology.objects.Basin(MatX,MatY,parentAnalyzer.matDir,parentAnalyzer.metaDatos).getBasinDivide();
                }
                
                UnionSet unionSubBasins=new UnionSet(campo, thisOrderSubBasins);
                
                parentAnalyzer.data_refSubBasins[i]=new DataReferenceImpl("subcuenca_"+i);
                parentAnalyzer.data_refSubBasins[i].setData(unionSubBasins);
                
            }
            
            parentAnalyzer.loadSpatialVariableState(true);
        } catch(java.io.IOException IOE){
            System.err.println(IOE);
        } catch(VisADException VisEx){
            System.err.println(VisEx);
        }
    }
}

class CaptureDistributions extends Thread{
    
    hydroScalingAPI.modules.networkAnalysis.widgets.BasinAnalyzer parentAnalyzer;
    
    public CaptureDistributions(hydroScalingAPI.modules.networkAnalysis.widgets.BasinAnalyzer parent){
        
        parentAnalyzer=parent;
        
        
    }
    
    public void run(){
        
        try{
            //leer distribucion, actualizar la varible del basinAnalizer y activar el boton
            
            parentAnalyzer.branchingValues=parentAnalyzer.myHortonStructure.getBranchingPerOrder();
            parentAnalyzer.enableRadioButton(0);
            
            parentAnalyzer.areasDist=parentAnalyzer.myHortonStructure.getQuantityDistributionPerOrder(0);
            parentAnalyzer.enableRadioButton(3);
            
            parentAnalyzer.magnDist=parentAnalyzer.myHortonStructure.getQuantityDistributionPerOrder(1);
            parentAnalyzer.enableRadioButton(4);
            
            parentAnalyzer.mainGeomLengthDist=parentAnalyzer.myHortonStructure.getQuantityDistributionPerOrder(2);
            parentAnalyzer.enableRadioButton(5);
            
            parentAnalyzer.mainTopolLengthDist=parentAnalyzer.myHortonStructure.getQuantityDistributionPerOrder(3);
            parentAnalyzer.enableRadioButton(6);
            
            parentAnalyzer.totalGeomLengthDist=parentAnalyzer.myHortonStructure.getQuantityDistributionPerOrder(4);
            parentAnalyzer.enableRadioButton(7);
            //Also Enable Dd Analysis
            parentAnalyzer.enableRadioButton(100);
            
            parentAnalyzer.totalDropDist=parentAnalyzer.myHortonStructure.getQuantityDistributionPerOrder(6);
            parentAnalyzer.enableRadioButton(9);
            
            parentAnalyzer.maxDropDist=parentAnalyzer.myHortonStructure.getQuantityDistributionPerOrder(7);
            parentAnalyzer.enableRadioButton(10);
            
            parentAnalyzer.streamDropDist=parentAnalyzer.myHortonStructure.getQuantityDistributionPerOrder(8);
            parentAnalyzer.enableRadioButton(11);
            
            parentAnalyzer.geomLengthDist=parentAnalyzer.myHortonStructure.getLengthDistributionPerOrder(0);
            parentAnalyzer.enableRadioButton(1);
            
            parentAnalyzer.topolLengthDist=parentAnalyzer.myHortonStructure.getLengthDistributionPerOrder(1);
            parentAnalyzer.enableRadioButton(2);
            
            parentAnalyzer.slopesDist=new float[parentAnalyzer.streamDropDist.length][];
            for(int i=0;i<parentAnalyzer.streamDropDist.length;i++){
                parentAnalyzer.slopesDist[i]=new float[parentAnalyzer.streamDropDist[i].length];
                for(int j=0;j<parentAnalyzer.streamDropDist[i].length;j++){
                    parentAnalyzer.slopesDist[i][j]=parentAnalyzer.streamDropDist[i][j]/parentAnalyzer.geomLengthDist[i][j];
                }
            }
            parentAnalyzer.enableRadioButton(8);
            
            
            
        } catch(java.io.IOException IOE){
            System.err.println("Exception at CaptureDistributions");
            System.err.println(IOE);
        }
    }
}

class ExtractTiles extends Thread{
    
    hydroScalingAPI.modules.networkAnalysis.widgets.BasinAnalyzer parentAnalyzer;
    
    public ExtractTiles(hydroScalingAPI.modules.networkAnalysis.widgets.BasinAnalyzer parent){
        
        parentAnalyzer=parent;
        
    }
    
    public void run(){
        
        try{
            
            parentAnalyzer.data_refSubBasins=new DataReferenceImpl[parentAnalyzer.myHortonStructure.headsArray.length];
            
            RealTupleType campo=new RealTupleType(RealType.Longitude,RealType.Latitude);
            
            int numCols=parentAnalyzer.metaDatos.getNumCols();
            
            for (int i=0;i<parentAnalyzer.myHortonStructure.headsArray.length;i++){
                
                Gridded2DSet[] thisOrderSubBasins=new Gridded2DSet[parentAnalyzer.myHortonStructure.headsArray[i].length];
                
                for (int j=0;j<parentAnalyzer.myHortonStructure.headsArray[i].length;j++){
                    
                    int MatX=parentAnalyzer.myHortonStructure.contactsArray[i][j]%numCols;
                    int MatY=parentAnalyzer.myHortonStructure.contactsArray[i][j]/numCols;
                    
                    thisOrderSubBasins[j]=new hydroScalingAPI.util.geomorphology.objects.Basin(MatX,MatY,parentAnalyzer.matDir,parentAnalyzer.metaDatos).getBasinDivide();
                }
                
                UnionSet unionSubBasins=new UnionSet(campo, thisOrderSubBasins);
                
                parentAnalyzer.data_refSubBasins[i]=new DataReferenceImpl("subcuenca_"+i);
                parentAnalyzer.data_refSubBasins[i].setData(unionSubBasins);
                
            }
            
            parentAnalyzer.loadSpatialVariableState(true);
        } catch(java.io.IOException IOE){
            System.err.println(IOE);
        } catch(VisADException VisEx){
            System.err.println(VisEx);
        }
    }
}
