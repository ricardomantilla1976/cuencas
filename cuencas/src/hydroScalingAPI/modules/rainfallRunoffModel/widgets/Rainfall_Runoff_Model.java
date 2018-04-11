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
 * rainfall_runoff_Model.java
 *
 * Created on November 3, 2001, 11:56 AM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.widgets;

import ij.util.Java2;
import javax.swing.JViewport;
import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;
import java.awt.Font;


/**
 * A GUI for preparing a rainfall-runoff simulation
 * @author Ricardo Mantilla
 */
public class Rainfall_Runoff_Model extends javax.swing.JDialog implements DisplayListener{
    
    private float[] red;
    private float[] green;
    private float[] blue;
    
    private boolean[] firstTouchTab=new boolean[8];
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    
    private hydroScalingAPI.util.geomorphology.objects.Basin myCuenca;

    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure;
    
    private int[][] magnitudes;
    
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
    
    private DisplayImplJ3D displayMap, displayStorm,activeDisplay,displayPlot,displayNet;
    private DisplayImplJ3D displayDEM;
    private ScalarMap lonMap,latMap,varMap,lonMapDEM,latMapDEM,
                      varMapDEM,altMapDEM, lonMapStorm,latMapStorm,varMapStorm,
                      timeMapPlot,rainMapPlot;
    
    private int[][] matrizPintada;
    private float[][][] alturasCuenca;
    private int maxColor;
    private int NumMaxAltura = 15000;
    private int NumMinAltura = 0;
    
    private RealTupleType campo;
    private RealType varRaster, varRasterDEM, varRasterStorm,timeType,precType;
    private RealTupleType tuplarango;
    private FunctionType funcionTransfer,funcionTransferDEM,funcionTransferStorm,func_time_intensity;
    
    
    private javax.swing.JTextArea textArea;
    private TextAreaPrintStream redirect;
    private java.io.PrintStream originalStreamOut;
    
    /**
     * Creates new form rainfall_runoff_Model
     * @param parent The main GIS GUI
     * @param x The column number of the basin outlet location
     * @param y The row number of the basin outlet location
     * @param direcc The direction matrix associated to the DEM where the basin is embeded
     * @param magnit The magnitudes matrix associated to the DEM where the basin is embeded
     * @param md The MetaRaster associated to the DEM where the basin is embeded
     * @throws java.rmi.RemoteException Captures errors while assigning values to VisAD data objects
     * @throws visad.VisADException Captures errors while creating VisAD objects
     * @throws java.io.IOException Captures errors while reading information
     */
    public Rainfall_Runoff_Model(hydroScalingAPI.mainGUI.ParentGUI parent, int x, int y, byte[][] direcc, int[][] magnit, hydroScalingAPI.io.MetaRaster md) throws RemoteException, VisADException, java.io.IOException{
        super(parent, true);
        initComponents();
        
        originalStreamOut=new java.io.PrintStream(System.out);
        
        textArea = new javax.swing.JTextArea(5, 3);
        redirect = new TextAreaPrintStream(textArea, System.out);
        jPanel18.add(textArea, java.awt.BorderLayout.CENTER);
        
        matDir=direcc;
        metaDatos=md;
        mainFrame=parent;
        magnitudes=magnit;
        
        red=    mainFrame.getInfoManager().getNetworkRed();
        green=  mainFrame.getInfoManager().getNetworkGreen();
        blue=   mainFrame.getInfoManager().getNetworkBlue();
        
        //Set up general interface
        setBounds(0,0, 950, 700);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        
        //Get Data concerning the all Rainfall-Runoff module
        
        myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);

        thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        matrizPintada=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        alturasCuenca=new float[2][myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        
        //...Algoritmo que rellena la matriz Pintada necesita la matriz de direcciones podada y la que no esta podada... que son los input
        
        campo=new RealTupleType(RealType.Longitude,RealType.Latitude);
        varRaster=RealType.getRealType("varRaster");
        varRasterDEM=RealType.getRealType("varRasterDEM");
        tuplarango=new RealTupleType(RealType.Altitude,varRasterDEM);

        Linear2DSet dominio = new Linear2DSet(campo,metaDatos.getMinLon()+(myCuenca.getMinX()-0.5)*metaDatos.getResLon()/3600.0,metaDatos.getMinLon()+(myCuenca.getMaxX()+1.5)*metaDatos.getResLon()/3600.0,myCuenca.getMaxX()-myCuenca.getMinX()+3,
                                                    metaDatos.getMinLat()+(myCuenca.getMinY()-0.5)*metaDatos.getResLat()/3600.0,metaDatos.getMinLat()+(myCuenca.getMaxY()+1.5)*metaDatos.getResLat()/3600.0,myCuenca.getMaxY()-myCuenca.getMinY()+3);
        
        funcionTransfer = new FunctionType( campo, varRaster);
        FlatField valores = new FlatField( funcionTransfer, dominio);
        
        funcionTransferDEM = new FunctionType( campo, tuplarango);
        FlatField valoresDEM = new FlatField( funcionTransferDEM, dominio);
        
        int xOulet,yOulet;
        hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;
        
        metaDatos.setLocationBinaryFile(new java.io.File(metaDatos.getLocationBinaryFile().getPath().substring(0,metaDatos.getLocationBinaryFile().getPath().lastIndexOf("."))+".corrDEM"));
        metaDatos.setFormat("Double");
        float[][] datosDeAca=new hydroScalingAPI.io.DataRaster(metaDatos).getFloatEqualized();
        float[][] datosDeAcaAlt=new hydroScalingAPI.io.DataRaster(metaDatos).getFloat();
        
        
        int demNumCols=metaDatos.getNumCols();

        int basinMinX=myCuenca.getMinX();
        int basinMinY=myCuenca.getMinY();
        
        for (int i=0;i<linksStructure.contactsArray.length;i++){
            if (linksStructure.magnitudeArray[i] < linksStructure.basinMagnitude){
                
                xOulet=linksStructure.contactsArray[i]%demNumCols;
                yOulet=linksStructure.contactsArray[i]/demNumCols;
                
                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaDatos);
                int[][] xyHillSlope=myHillActual.getXYHillSlope();
                for (int j=0;j<xyHillSlope[0].length;j++){
                    matrizPintada[xyHillSlope[1][j]-basinMinY+1][xyHillSlope[0][j]-basinMinX+1]=i+1;
                    alturasCuenca[0][xyHillSlope[1][j]-basinMinY+1][xyHillSlope[0][j]-basinMinX+1]=datosDeAcaAlt[xyHillSlope[1][j]][xyHillSlope[0][j]];
                    alturasCuenca[1][xyHillSlope[1][j]-basinMinY+1][xyHillSlope[0][j]-basinMinX+1]=datosDeAca[xyHillSlope[1][j]][xyHillSlope[0][j]];

                }
            } else {
                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDir,magnitudes,metaDatos);
                int[][] xyHillSlope=myHillActual.getXYHillSlope();
                for (int j=0;j<xyHillSlope[0].length;j++){
                    matrizPintada[xyHillSlope[1][j]-basinMinY+1][xyHillSlope[0][j]-basinMinX+1]=i+1;
                    alturasCuenca[0][xyHillSlope[1][j]-basinMinY+1][xyHillSlope[0][j]-basinMinX+1]=datosDeAcaAlt[xyHillSlope[1][j]][xyHillSlope[0][j]];
                    alturasCuenca[1][xyHillSlope[1][j]-basinMinY+1][xyHillSlope[0][j]-basinMinX+1]=datosDeAca[xyHillSlope[1][j]][xyHillSlope[0][j]];
                }
            }
        }
        
        for (int i=0;i<matrizPintada.length;i++) for (int j=0;j<matrizPintada[0].length;j++) {
            if (matrizPintada[i][j] == 0){
                alturasCuenca[0][i][j]=Float.NaN;
                alturasCuenca[1][i][j]=Float.NaN;
            }
        }
        
        System.out.println("pintada la matriz y leidas las alturas");
        
        System.out.println("Complexity reduction:");
        System.out.println("    Number of Pixels:           "+myCuenca.getXYBasin()[0].length);
        System.out.println("    Number of Network Pixels:   "+myCuenca.getXYBasin()[0].length);
        System.out.println("    Number of Links:            "+linksStructure.contactsArray.length);
        System.out.println("        Total number of Differencial equations:"+(2*linksStructure.contactsArray.length));
        
        float[][] matrizPintadaLarga=new float[1][matrizPintada.length*matrizPintada[0].length];
        float[][] matrizAlturasLarga=new float[2][matrizPintada.length*matrizPintada[0].length];
        
        for (int i=0;i<matrizPintada.length;i++){
            for (int j=0;j<matrizPintada[0].length;j++){
                matrizPintadaLarga[0][i*matrizPintada[0].length+j]=matrizPintada[i][j];
                matrizAlturasLarga[0][i*matrizPintada[0].length+j]=alturasCuenca[0][i][j];
                matrizAlturasLarga[1][i*matrizPintada[0].length+j]=alturasCuenca[1][i][j];
            }
        }
        
        valores.setSamples( matrizPintadaLarga, false );
        valoresDEM.setSamples( matrizAlturasLarga, false );

        DataReferenceImpl data_ref_varRaster = new DataReferenceImpl("data_ref_varRaster");
        data_ref_varRaster.setData(valores);
        
        DataReferenceImpl data_ref_varDEM = new DataReferenceImpl("data_ref_varDEM");
        data_ref_varDEM.setData(valoresDEM);
        
        
        //Set up interface for DEM-3D
        
        displayDEM = new DisplayImplJ3D("displayDEM");
        
        lonMapDEM = new ScalarMap( RealType.Longitude, Display.XAxis );
        latMapDEM = new ScalarMap( RealType.Latitude, Display.YAxis );
        altMapDEM = new ScalarMap( RealType.Altitude, Display.ZAxis );
        varMapDEM = new ScalarMap( varRasterDEM,  Display.RGB );

        //texMap = new ScalarMap( texField,  Display.Text );
        displayDEM.addMap( latMapDEM );
        displayDEM.addMap( lonMapDEM );
        displayDEM.addMap( altMapDEM );
        displayDEM.addMap( varMapDEM );
        //display.addMap( texMap );

        lonMapDEM.setRange(metaDatos.getMinLon()+(myCuenca.getMinX()-1)*metaDatos.getResLon()/3600.0,metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0);
        latMapDEM.setRange(metaDatos.getMinLat()+(myCuenca.getMinY()-1)*metaDatos.getResLat()/3600.0,metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0);
        
        GraphicsModeControl dispGMCDEM = (GraphicsModeControl) displayDEM.getGraphicsModeControl();
        dispGMCDEM.setScaleEnable(true);
        //dispGMCDEM.setTextureEnable(false);
        
        ProjectionControl pcDEM = displayDEM.getProjectionControl();
        pcDEM.setAspect(new double[] {1.0, (double) (alturasCuenca[0].length/(double) alturasCuenca[0][0].length),1.0});

        hydroScalingAPI.util.statistics.Stats varStat= new hydroScalingAPI.util.statistics.Stats(alturasCuenca[0],new Float(metaDatos.getMissing()).floatValue());
        NumMinAltura=(int) varStat.minValue;
        NumMaxAltura=(int) (varStat.maxValue+2*(varStat.maxValue-varStat.minValue));
        altMapDEM.setRange(NumMinAltura,NumMaxAltura);
        
        varStat= new hydroScalingAPI.util.statistics.Stats(alturasCuenca[1],new Float(metaDatos.getMissing()).floatValue());
        NumMinAltura=(int) varStat.minValue;
        NumMaxAltura=(int) varStat.maxValue;
        varMapDEM.setRange(NumMinAltura,NumMaxAltura);
        
        displayDEM.addReference(data_ref_varDEM);
        
        hydroScalingAPI.subGUIs.widgets.RasterPalettesManager myLabeledColorWidget=new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(varMapDEM);
        jPanel7.add(myLabeledColorWidget);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(displayDEM);
        
        //Set up interface for Hill-Slope System
        
        displayMap = new DisplayImplJ3D("displayMap",new visad.java3d.TwoDDisplayRendererJ3D());
        
        GraphicsModeControl dispGMC = (GraphicsModeControl) displayMap.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        lonMap = new ScalarMap( RealType.Longitude, Display.XAxis );
        latMap = new ScalarMap( RealType.Latitude, Display.YAxis );
        varMap = new ScalarMap( varRaster,  Display.RGB );
        
        displayMap.addMap( lonMap );
        displayMap.addMap( latMap );
        displayMap.addMap( varMap );
        
        lonMap.setRange(metaDatos.getMinLon()+(myCuenca.getMinX()-1)*metaDatos.getResLon()/3600.0,metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0);
        latMap.setRange(metaDatos.getMinLat()+(myCuenca.getMinY()-1)*metaDatos.getResLat()/3600.0,metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0);
        
        
        maxColor=linksStructure.contactsArray.length+1;
        float[][] estaTabla=new float[3][maxColor];
        
        for (int i=1;i<estaTabla[0].length;i++){
            estaTabla[0][i]=(float) (.8*Math.random()+.1);
            estaTabla[1][i]=(float) (.8*Math.random()+.1);
            estaTabla[2][i]=(float) (.8*Math.random()+.1);
        }
        
        ColorControl control = (ColorControl) varMap.getControl();
        control.setTable(estaTabla);
        varMap.setRange(0,maxColor);
        
        ProjectionControl pc = displayMap.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, (double) (matrizPintada.length/(double) matrizPintada[0].length)});
        
        activeDisplay=displayMap; //Sets this Display as the active display

        displayMap.addDisplayListener(this);
        displayMap.addReference(data_ref_varRaster);
        
        
        plotNetwork(displayMap);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(displayMap);
        
        //Set up interface for Channel Network
        
        displayNet = new DisplayImplJ3D("displayNet",new visad.java3d.TwoDDisplayRendererJ3D());
        
        GraphicsModeControl dispNet = (GraphicsModeControl) displayNet.getGraphicsModeControl();
        dispNet.setScaleEnable(true);
        
        lonMap = new ScalarMap( RealType.Longitude, Display.XAxis );
        latMap = new ScalarMap( RealType.Latitude, Display.YAxis );
        
        displayNet.addMap( lonMap );
        displayNet.addMap( latMap );
        
        lonMap.setRange(metaDatos.getMinLon()+(myCuenca.getMinX()-1)*metaDatos.getResLon()/3600.0,metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0);
        latMap.setRange(metaDatos.getMinLat()+(myCuenca.getMinY()-1)*metaDatos.getResLat()/3600.0,metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0);
        
        
        ProjectionControl pcNet = displayNet.getProjectionControl();
        pcNet.setAspectCartesian(new double[] {1.0, (double) (matrizPintada.length/(double) matrizPintada[0].length)});
        
        displayNet.addDisplayListener(this);
        
        plotNetwork(displayNet);
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(displayNet);
        
        //Set up interface for Storm Analysis
        
        displayStorm = new DisplayImplJ3D("displayStorm",new visad.java3d.TwoDDisplayRendererJ3D());
        
        GraphicsModeControl dispGMCStorm = (GraphicsModeControl) displayStorm.getGraphicsModeControl();
        dispGMCStorm.setScaleEnable(true);
        
        varRasterStorm=RealType.getRealType("varRasterStorm");
        
        lonMapStorm = new ScalarMap( RealType.Longitude, Display.XAxis );
        latMapStorm = new ScalarMap( RealType.Latitude, Display.YAxis );
        varMapStorm = new ScalarMap( varRasterStorm,  Display.RGB );
        
        displayStorm.addMap( lonMapStorm );
        displayStorm.addMap( latMapStorm );
        displayStorm.addMap( varMapStorm );
        
        lonMapStorm.setRange(metaDatos.getMinLon()+(myCuenca.getMinX()-1)*metaDatos.getResLon()/3600.0,metaDatos.getMinLon()+(myCuenca.getMaxX()+2)*metaDatos.getResLon()/3600.0);
        latMapStorm.setRange(metaDatos.getMinLat()+(myCuenca.getMinY()-1)*metaDatos.getResLat()/3600.0,metaDatos.getMinLat()+(myCuenca.getMaxY()+2)*metaDatos.getResLat()/3600.0);
        
        ProjectionControl pcStorm = displayStorm.getProjectionControl();
        pcStorm.setAspectCartesian(new double[] {1.0, (double) (matrizPintada.length/(double) matrizPintada[0].length)});
        
        hydroScalingAPI.tools.VisadTools.addWheelFunctionality(displayStorm);
        
        displayPlot= new DisplayImplJ3D("displayPlot",new visad.java3d.TwoDDisplayRendererJ3D());
        
        GraphicsModeControl dispGMCPlot = (GraphicsModeControl) displayPlot.getGraphicsModeControl();
        dispGMCPlot.setScaleEnable(true);
        
        timeType=RealType.getRealType("time");
        precType=RealType.getRealType("intensity");
        
        timeMapPlot = new ScalarMap( timeType, Display.XAxis );
        rainMapPlot = new ScalarMap( precType, Display.YAxis );
        
        displayPlot.addMap( timeMapPlot );
        displayPlot.addMap( rainMapPlot );
        
        timeMapPlot.setScalarName("Time [minutes]");
        rainMapPlot.setScalarName("Rain Intensity [mm/h]");
        
        func_time_intensity=new FunctionType( timeType, precType);
        
        ProjectionControl pcPlot = displayPlot.getProjectionControl();
        pcPlot.setAspectCartesian(new double[] {1.0, 0.5});
        
        jSplitPane1.setDividerLocation(380);
        
        
        
        //Add an Initial display
        
        jPanel2.add("Center",displayDEM.getComponent());
        firstTouchTab[0]=true;
        
    }
    
    private void plotNetwork(DisplayImpl display) throws java.io.IOException, TypeException, VisADException{
        
        try {

            hydroScalingAPI.io.MetaNetwork localNetwork=new hydroScalingAPI.io.MetaNetwork(metaDatos);
            
            byte[][] basinMaskFullMap=myCuenca.getBasinMask();
            
            for(int orderRequested=linksStructure.getBasinOrder();orderRequested>0;orderRequested--){
                visad.UnionSet toPlot = localNetwork.getUnionSet(orderRequested,basinMaskFullMap);

                visad.DataReferenceImpl refeElemVec=new visad.DataReferenceImpl("order"+orderRequested);
                refeElemVec.setData(toPlot);
                visad.ConstantMap[] lineCMap = {    new visad.ConstantMap( red[orderRequested-1]/255.0f, visad.Display.Red),
                                                    new visad.ConstantMap( green[orderRequested-1]/255.0f, visad.Display.Green),
                                                    new visad.ConstantMap( blue[orderRequested-1]/255.0f, visad.Display.Blue),
                                                    new visad.ConstantMap( 1.5, visad.Display.LineWidth)};
                display.addReference(refeElemVec,lineCMap);
            }
            
            visad.DataReferenceImpl riverRef=new visad.DataReferenceImpl("divide");
            riverRef.setData(myCuenca.getBasinDivide());
            visad.ConstantMap[] lineCMap = {  new visad.ConstantMap( 100/255.0f, visad.Display.Red),
                                              new visad.ConstantMap( 100/255.0f, visad.Display.Green),
                                              new visad.ConstantMap( 100/255.0f, visad.Display.Blue),
                                              new visad.ConstantMap( 2.0f, visad.Display.LineWidth)};
            
            display.addReference( riverRef , lineCMap);
            
        } catch (visad.VisADException exc) {
            System.err.println("Failed showing streams");
            System.err.println(exc);
        } catch (java.io.IOException exc) {
            System.err.println("Failed showing streams");
            System.err.println(exc);
        }

        
    }
    
    private void plotHyetograph() throws VisADException, RemoteException{
        
        displayPlot.removeAllReferences();
        
        int nSteps=(int)((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/storm.stormRecordResolutionInMinutes())+1;
        
        float[][] dataToPlot=new float[2][nSteps];
        
        for (int i=0;i<nSteps;i++){
            double elaplsedTime=storm.stormInitialTimeInMinutes()+i*storm.stormRecordResolutionInMinutes();
            dataToPlot[0][i]=(float)elaplsedTime;
            for (int j=0;j<linksStructure.contactsArray.length;j++) dataToPlot[1][i]+=thisHillsInfo.precipitation(j,elaplsedTime)*thisHillsInfo.Area(j);
            dataToPlot[1][i]/=thisNetworkGeom.basinArea();
        }
        
        float[][] elTimeRepeted=new float[1][2*dataToPlot[0].length];
        double[][] laPrecRepeted=new double[1][2*dataToPlot[0].length];
        for (int i=0;i<elTimeRepeted[0].length;i+=2){
            elTimeRepeted[0][i]=dataToPlot[0][i/2];
            elTimeRepeted[0][i+1]=dataToPlot[0][i/2];
        }
        laPrecRepeted[0][0]=0.0f;
        for (int i=1;i<elTimeRepeted[0].length-2;i+=2){
            laPrecRepeted[0][i]=(double)dataToPlot[1][i/2];
            laPrecRepeted[0][i+1]=(double)dataToPlot[1][i/2];
        }
        
        Irregular1DSet binsRepated=new Irregular1DSet(timeType,elTimeRepeted);
        
        FlatField vals_Intensity = new FlatField( func_time_intensity, binsRepated);
        vals_Intensity.setSamples( laPrecRepeted );
        
        DataReferenceImpl data_ref_Prec = new DataReferenceImpl("data_ref_Prec");
        data_ref_Prec.setData(vals_Intensity);

        displayPlot.addReference( data_ref_Prec );
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infiltrationModel = new javax.swing.ButtonGroup();
        panelOpciones = new javax.swing.JTabbedPane();
        jPanel2 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jTextField1 = new javax.swing.JTextField();
        jButton2 = new javax.swing.JButton();
        jPanel3 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        openPrecip = new javax.swing.JButton();
        jTextField2 = new javax.swing.JTextField();
        jSplitPane1 = new javax.swing.JSplitPane();
        jPanel9 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        stormFilesList = new javax.swing.JComboBox();
        jPanel10 = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        iniDateList = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        finalDateList = new javax.swing.JComboBox();
        jPanel12 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        infilRate = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        totalVolume = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jPanel7 = new javax.swing.JPanel();
        jPanel14 = new javax.swing.JPanel();
        jPanel15 = new javax.swing.JPanel();
        jLabel6 = new javax.swing.JLabel();
        rainIntensity = new javax.swing.JTextField();
        jLabel7 = new javax.swing.JLabel();
        jLabel8 = new javax.swing.JLabel();
        rainDuration = new javax.swing.JTextField();
        jLabel9 = new javax.swing.JLabel();
        jRadioRunoffCoefficient = new javax.swing.JRadioButton();
        runoffCoefficient = new javax.swing.JTextField();
        jLabel20 = new javax.swing.JLabel();
        jRadioInfiltrationIntensity = new javax.swing.JRadioButton();
        infiltrationIntensity = new javax.swing.JTextField();
        jLabel17 = new javax.swing.JLabel();
        jLabel10 = new javax.swing.JLabel();
        flowVelocity = new javax.swing.JTextField();
        jLabel11 = new javax.swing.JLabel();
        jLabel12 = new javax.swing.JLabel();
        lambda1 = new javax.swing.JTextField();
        jLabel13 = new javax.swing.JLabel();
        jLabel14 = new javax.swing.JLabel();
        lambda2 = new javax.swing.JTextField();
        jLabel15 = new javax.swing.JLabel();
        jLabel18 = new javax.swing.JLabel();
        hillVelocity = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jPanel17 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        rainButton = new javax.swing.JButton();
        simButton = new javax.swing.JButton();
        sim3Button = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel18 = new javax.swing.JPanel();

        setTitle("Link's Based Rainfall Runoff Model");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        panelOpciones.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        panelOpciones.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                panelOpcionesStateChanged(evt);
            }
        });

        jPanel2.setLayout(new java.awt.BorderLayout());
        panelOpciones.addTab("Basin 3D View", null, jPanel2, "");

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel6.setLayout(new java.awt.GridLayout(1, 2));

        jTextField1.setText("jTextField1");
        jPanel6.add(jTextField1);

        jButton2.setText("Repaint");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });
        jPanel6.add(jButton2);

        jPanel1.add(jPanel6, java.awt.BorderLayout.NORTH);

        panelOpciones.addTab("Hillslope-Links System", null, jPanel1, "");

        jPanel3.setLayout(new java.awt.BorderLayout());
        panelOpciones.addTab("Channel Network", null, jPanel3, "");

        jPanel4.setLayout(new java.awt.BorderLayout());

        jPanel8.setLayout(new java.awt.GridLayout(1, 2));

        openPrecip.setText("Open Storm ...");
        openPrecip.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openPrecipActionPerformed(evt);
            }
        });
        jPanel8.add(openPrecip);
        jPanel8.add(jTextField2);

        jPanel4.add(jPanel8, java.awt.BorderLayout.NORTH);

        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel13.setLayout(new java.awt.BorderLayout());

        jLabel5.setText("Data Snap-Shot : ");
        jPanel13.add(jLabel5, java.awt.BorderLayout.WEST);
        jPanel13.add(stormFilesList, java.awt.BorderLayout.CENTER);

        jPanel9.add(jPanel13, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setLeftComponent(jPanel9);

        jPanel10.setLayout(new java.awt.BorderLayout());

        jPanel11.setLayout(new java.awt.GridLayout(2, 2));

        jLabel1.setText("Initial Date");
        jPanel11.add(jLabel1);
        jPanel11.add(iniDateList);

        jLabel2.setText("Final Date");
        jPanel11.add(jLabel2);
        jPanel11.add(finalDateList);

        jPanel10.add(jPanel11, java.awt.BorderLayout.NORTH);

        jPanel12.setLayout(new java.awt.GridLayout(2, 2));

        jLabel3.setText("Infiltration Rate [mm/h]");
        jPanel12.add(jLabel3);
        jPanel12.add(infilRate);

        jLabel4.setText("Total Volume [m^3]");
        jPanel12.add(jLabel4);
        jPanel12.add(totalVolume);

        jPanel10.add(jPanel12, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(jPanel10);

        jPanel4.add(jSplitPane1, java.awt.BorderLayout.CENTER);

        panelOpciones.addTab("Precipitation", null, jPanel4, "");
        panelOpciones.addTab("Evaporation", null, jPanel5, "");
        panelOpciones.addTab("Soil Properties", null, jPanel7, "");

        jPanel14.setLayout(new java.awt.GridLayout(2, 1));

        jPanel15.setBorder(javax.swing.BorderFactory.createTitledBorder("Simulation Parameters"));
        jPanel15.setLayout(new java.awt.GridLayout(8, 3));

        jLabel6.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel6.setText("Rainfall Intensity");
        jPanel15.add(jLabel6);

        rainIntensity.setText("20");
        jPanel15.add(rainIntensity);

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel7.setText(" mm / h");
        jPanel15.add(jLabel7);

        jLabel8.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel8.setText("Rainfall Duration");
        jPanel15.add(jLabel8);

        rainDuration.setText("60");
        jPanel15.add(rainDuration);

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel9.setText(" minutes");
        jPanel15.add(jLabel9);

        infiltrationModel.add(jRadioRunoffCoefficient);
        jRadioRunoffCoefficient.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jRadioRunoffCoefficient.setSelected(true);
        jRadioRunoffCoefficient.setText("Runoff Coefficient");
        jRadioRunoffCoefficient.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioRunoffCoefficientActionPerformed(evt);
            }
        });
        jPanel15.add(jRadioRunoffCoefficient);

        runoffCoefficient.setText("50");
        jPanel15.add(runoffCoefficient);

        jLabel20.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel20.setText("%");
        jPanel15.add(jLabel20);

        infiltrationModel.add(jRadioInfiltrationIntensity);
        jRadioInfiltrationIntensity.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jRadioInfiltrationIntensity.setText("Infiltration Intensity");
        jRadioInfiltrationIntensity.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioInfiltrationIntensityActionPerformed(evt);
            }
        });
        jPanel15.add(jRadioInfiltrationIntensity);

        infiltrationIntensity.setText("5");
        infiltrationIntensity.setEnabled(false);
        jPanel15.add(infiltrationIntensity);

        jLabel17.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel17.setText(" mm / h");
        jPanel15.add(jLabel17);

        jLabel10.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel10.setText("Channel Flow Velocity (vo)");
        jPanel15.add(jLabel10);

        flowVelocity.setText("0.3");
        jPanel15.add(flowVelocity);

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel11.setText(" m / s");
        jPanel15.add(jLabel11);

        jLabel12.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel12.setText("Q exponent (Lambda 1)");
        jPanel15.add(jLabel12);

        lambda1.setText("0.2");
        jPanel15.add(lambda1);

        jLabel13.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel13.setText(" m^(1/2) / s");
        jPanel15.add(jLabel13);

        jLabel14.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel14.setText("A exponent (Lambda 2)");
        jPanel15.add(jLabel14);

        lambda2.setText("-0.1");
        jPanel15.add(lambda2);

        jLabel15.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel15.setText(" m^(1/2) / s");
        jPanel15.add(jLabel15);

        jLabel18.setFont(new java.awt.Font("Dialog", 1, 10)); // NOI18N
        jLabel18.setText("Hillslope Flow Velocity (vo)");
        jPanel15.add(jLabel18);

        hillVelocity.setText("0.05");
        jPanel15.add(hillVelocity);

        jLabel19.setFont(new java.awt.Font("Dialog", 0, 10)); // NOI18N
        jLabel19.setText(" m / s");
        jPanel15.add(jLabel19);

        jPanel14.add(jPanel15);

        jPanel17.setLayout(new java.awt.BorderLayout());

        jPanel16.setLayout(new java.awt.GridLayout(1, 0));

        rainButton.setText("Variable Rainfall Simulation");
        rainButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rainButtonActionPerformed(evt);
            }
        });
        jPanel16.add(rainButton);

        simButton.setText("Uniform Rainfall Simulation");
        simButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                simButtonActionPerformed(evt);
            }
        });
        jPanel16.add(simButton);

        sim3Button.setText("Parallel Simulation");
        sim3Button.setEnabled(false);
        sim3Button.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sim3ButtonActionPerformed(evt);
            }
        });
        jPanel16.add(sim3Button);

        jPanel17.add(jPanel16, java.awt.BorderLayout.NORTH);

        jPanel18.setLayout(new java.awt.BorderLayout());
        jScrollPane1.setViewportView(jPanel18);

        jPanel17.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel14.add(jPanel17);

        panelOpciones.addTab("Sample Simulations", null, jPanel14, "");

        getContentPane().add(panelOpciones, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void rainButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rainButtonActionPerformed
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersHydPath);
        fc.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
        fc.setDialogTitle("Select Rainfall Field");
        javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("metaVHC","Hydrologic Variable");
        fc.addChoosableFileFilter(mdtFilter);
        fc.showOpenDialog(this);

        //En caso de que el usuario haya puesto cancelar
        if (fc.getSelectedFile() == null) return;
        if (!fc.getSelectedFile().isFile()) return;
        java.io.File locFile=fc.getSelectedFile();
        
        javax.swing.JFileChooser fc1=new javax.swing.JFileChooser("/");
        fc1.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        fc1.setDialogTitle("Output directory");
        int result = fc1.showDialog(this,"Select");
        java.io.File selectedFile = fc1.getSelectedFile();
        
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;
        System.out.println((result == javax.swing.JFileChooser.CANCEL_OPTION));
        
        rainButton.setEnabled(false);
        
        float rainIntensityValue=20.0f;
        float rainDurationValue=60.0f;
        float infiltrationIntensityValue=10.0f;
        float flowVel=0.3f;
        float exp_lambda1=0.2f;
        float exp_lambda2=-0.1f;
        float hillVel=0.05f;
        float runoffCoeff=1.0f;

        try{
            rainIntensityValue=new Float(rainIntensity.getText()).floatValue();
            rainDurationValue=new Float(rainDuration.getText()).floatValue();
            infiltrationIntensityValue=new Float(infiltrationIntensity.getText()).floatValue();
            if(jRadioRunoffCoefficient.isSelected()) infiltrationIntensityValue=0.0f;
            flowVel=new Float(flowVelocity.getText()).floatValue();
            exp_lambda1=new Float(lambda1.getText()).floatValue();
            exp_lambda2=new Float(lambda2.getText()).floatValue();
            hillVel=new Float(hillVelocity.getText()).floatValue();
            runoffCoeff=new Float(runoffCoefficient.getText()).floatValue()/100.0f;
            if(jRadioInfiltrationIntensity.isSelected()) runoffCoeff=1.0f;
        } catch(NumberFormatException nfe){
            System.err.println(nfe);
            return;
        }

        int xOulet,yOulet;
        xOulet=myCuenca.getOutletID()%metaDatos.getNumCols();
        yOulet=myCuenca.getOutletID()/metaDatos.getNumCols();

        System.setOut(redirect);

        try{
            java.util.Hashtable routingParams=new java.util.Hashtable();
            routingParams.put("widthCoeff",1.0f);
            routingParams.put("widthExponent",0.4f);
            routingParams.put("widthStdDev",0.0f);

            routingParams.put("chezyCoeff",14.2f);
            routingParams.put("chezyExponent",-1/3.0f);

            routingParams.put("lambda1",exp_lambda1);
            routingParams.put("lambda2",exp_lambda2);
            routingParams.put("v_o",flowVel);

            routingParams.put("v_h",hillVel);
            
            routingParams.put("runoffCoefficient",runoffCoeff);
            
            Thread t1 = new Thread(new hydroScalingAPI.modules.rainfallRunoffModel.objects.SimulationToAsciiFile(xOulet,yOulet,matDir,magnitudes,metaDatos,locFile,infiltrationIntensityValue,5,selectedFile,routingParams));
            t1.start();
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
            return;
        } catch(visad.VisADException vie){
            System.err.println(vie);
            return;
        }
        rainButton.setEnabled(true);
        
        
    }//GEN-LAST:event_rainButtonActionPerformed

    private void simButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_simButtonActionPerformed
        javax.swing.JFileChooser fc=new javax.swing.JFileChooser("/");
        fc.setFileSelectionMode(javax.swing.JFileChooser.DIRECTORIES_ONLY);
        fc.setDialogTitle("Output directory");
        int result = fc.showDialog(this,"Select");
        java.io.File selectedFile = fc.getSelectedFile();
        
        if (result == javax.swing.JFileChooser.CANCEL_OPTION) return;

        simButton.setEnabled(false);
        
        float rainIntensityValue=20.0f;
        float rainDurationValue=60.0f;
        float infiltrationIntensityValue=10.0f;
        float flowVel=0.3f;
        float exp_lambda1=0.2f;
        float exp_lambda2=-0.1f;
        float hillVel=0.05f;
        float runoffCoeff=1.0f;

        try{
            rainIntensityValue=new Float(rainIntensity.getText()).floatValue();
            rainDurationValue=new Float(rainDuration.getText()).floatValue();
            infiltrationIntensityValue=new Float(infiltrationIntensity.getText()).floatValue();
            if(jRadioRunoffCoefficient.isSelected()) infiltrationIntensityValue=0.0f;
            flowVel=new Float(flowVelocity.getText()).floatValue();
            exp_lambda1=new Float(lambda1.getText()).floatValue();
            exp_lambda2=new Float(lambda2.getText()).floatValue();
            hillVel=new Float(hillVelocity.getText()).floatValue();
            runoffCoeff=new Float(runoffCoefficient.getText()).floatValue()/100.0f;
            if(jRadioInfiltrationIntensity.isSelected()) runoffCoeff=1.0f;
        } catch(NumberFormatException nfe){
            System.err.println(nfe);
            return;
        }

        int xOulet,yOulet;
        xOulet=myCuenca.getOutletID()%metaDatos.getNumCols();
        yOulet=myCuenca.getOutletID()/metaDatos.getNumCols();

        System.setOut(redirect);

        try{
            java.util.Hashtable routingParams=new java.util.Hashtable();
            routingParams.put("widthCoeff",1.0f);
            routingParams.put("widthExponent",0.4f);
            routingParams.put("widthStdDev",0.0f);

            routingParams.put("chezyCoeff",14.2f);
            routingParams.put("chezyExponent",-1/3.0f);

            routingParams.put("lambda1",exp_lambda1);
            routingParams.put("lambda2",exp_lambda2);
            routingParams.put("v_o",flowVel);

            routingParams.put("v_h",hillVel);
            
            routingParams.put("runoffCoefficient",runoffCoeff);
        
            Thread t1 = new Thread(new hydroScalingAPI.modules.rainfallRunoffModel.objects.SimulationToAsciiFile(xOulet,yOulet,matDir,magnitudes,metaDatos,rainIntensityValue,rainDurationValue,infiltrationIntensityValue,5,selectedFile,routingParams));
            t1.start();
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        } catch(visad.VisADException vie){
            System.err.println(vie);
        }

        simButton.setEnabled(true);
        
    }//GEN-LAST:event_simButtonActionPerformed

    private void panelOpcionesStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_panelOpcionesStateChanged
        // Add your handling code here:
        
        if (firstTouchTab[0]){
          jPanel1.remove(displayMap.getComponent());
          jPanel2.remove(displayDEM.getComponent());
          jPanel3.remove(displayNet.getComponent());
          jPanel9.remove(displayStorm.getComponent());
          jPanel10.remove(displayPlot.getComponent());

          if ( panelOpciones.getSelectedIndex()==0) {
              jPanel2.add("Center",displayDEM.getComponent());
          }

          if ( panelOpciones.getSelectedIndex()==1) {
              jPanel1.add("Center",displayMap.getComponent());
              activeDisplay=displayMap;
              firstTouchTab[1]=true;
          }
          
          if ( panelOpciones.getSelectedIndex()==2) {
              jPanel3.add("Center",displayNet.getComponent());
              activeDisplay=displayNet;
              firstTouchTab[2]=true;
          }
          
          if ( panelOpciones.getSelectedIndex()==3) {
              jPanel9.add("Center",displayStorm.getComponent());
              jPanel10.add("Center",displayPlot.getComponent());
              activeDisplay=displayStorm;
              firstTouchTab[3]=true;
          }

      }
        
    }//GEN-LAST:event_panelOpcionesStateChanged

    private void openPrecipActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openPrecipActionPerformed

        javax.swing.JFileChooser fc=new javax.swing.JFileChooser(mainFrame.getInfoManager().dataBaseRastersHydPath);
        fc.setFileSelectionMode(javax.swing.JFileChooser.FILES_ONLY);
        fc.setDialogTitle("Select Rainfall Field");
        javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("metaVHC","Hydrologic Variable");
        fc.addChoosableFileFilter(mdtFilter);
        fc.showOpenDialog(this);

        //En caso de que el usuario haya puesto cancelar
        if (fc.getSelectedFile() == null) return;
        if (!fc.getSelectedFile().isFile()) return;
        java.io.File locFile=fc.getSelectedFile();
        
        storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(locFile,myCuenca,linksStructure,metaDatos,matDir,magnitudes);
        if (!storm.isCompleted()) return;
        
        java.util.Date[] filesDates=storm.getFilesDates();
        for (int i=0;i<filesDates.length;i++){
            String dateLabel=filesDates[i].toString();
            stormFilesList.addItem(dateLabel);
            iniDateList.addItem(dateLabel);
            finalDateList.addItem(dateLabel);
        }
        
        //Despliego los Graficos correspondientes
        
        try{
            
            displayStorm.removeAllReferences();
        
            Linear2DSet dominio = new Linear2DSet(campo,metaDatos.getMinLon()+(myCuenca.getMinX()-0.5)*metaDatos.getResLon()/3600.0,metaDatos.getMinLon()+(myCuenca.getMaxX()+1.5)*metaDatos.getResLon()/3600.0,myCuenca.getMaxX()-myCuenca.getMinX()+3,
                                                        metaDatos.getMinLat()+(myCuenca.getMinY()-0.5)*metaDatos.getResLat()/3600.0,metaDatos.getMinLat()+(myCuenca.getMaxY()+1.5)*metaDatos.getResLat()/3600.0,myCuenca.getMaxY()-myCuenca.getMinY()+3);
        

            funcionTransferStorm = new FunctionType( campo, varRasterStorm);
            FlatField valoresStorm = new FlatField( funcionTransferStorm, dominio);

            float[][] matrizStormLarga=new float[1][matrizPintada.length*matrizPintada[0].length];

            for (int i=0;i<matrizPintada.length;i++){
                for (int j=0;j<matrizPintada[0].length;j++){
                    if(matrizPintada[i][j] > 0) 
                        matrizStormLarga[0][i*matrizPintada[0].length+j]=storm.getTotalHillSlopeBasedPrec(matrizPintada[i][j]-1);
                    else
                        matrizStormLarga[0][i*matrizPintada[0].length+j]=-1.0f;
                }
            }

            valoresStorm.setSamples( matrizStormLarga, false );

            DataReferenceImpl data_ref_varStorm = new DataReferenceImpl("data_ref_varStorm");
            data_ref_varStorm.setData(valoresStorm);


            displayStorm.addReference(data_ref_varStorm);
            
            displayStorm.addDisplayListener(this);
            
            thisHillsInfo.setStormManager(storm);
            
            plotHyetograph();
            
        }catch(java.rmi.RemoteException Ex){
            System.err.println("error cargando precipitacion "+Ex);
        }catch(VisADException Ex){
            System.err.println("error cargando precipitacion "+Ex);
        }
        
    }//GEN-LAST:event_openPrecipActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // Add your handling code here:
        
        try{
            float[][] estaTabla=new float[3][maxColor];
        
            for (int i=1;i<estaTabla[0].length;i++){
                estaTabla[0][i]=(float) (.8*Math.random()+.1);
                estaTabla[1][i]=(float) (.8*Math.random()+.1);
                estaTabla[2][i]=(float) (.8*Math.random()+.1);
            }

            ColorControl control = (ColorControl) varMap.getControl();
            control.setTable(estaTabla);
            //varMap.setRange(0,maxColor);
        }catch(RemoteException rex){
            System.err.println(rex);
        }catch(VisADException viex){
            System.err.println(viex);
        }
    }//GEN-LAST:event_jButton2ActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        System.setOut(originalStreamOut);
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog

private void sim3ButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sim3ButtonActionPerformed
        
}//GEN-LAST:event_sim3ButtonActionPerformed

    private void jRadioInfiltrationIntensityActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioInfiltrationIntensityActionPerformed
        infiltrationIntensity.setEnabled(jRadioInfiltrationIntensity.isSelected());
        runoffCoefficient.setEnabled(!jRadioInfiltrationIntensity.isSelected());
    }//GEN-LAST:event_jRadioInfiltrationIntensityActionPerformed

    private void jRadioRunoffCoefficientActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioRunoffCoefficientActionPerformed
        runoffCoefficient.setEnabled(jRadioRunoffCoefficient.isSelected());
        infiltrationIntensity.setEnabled(!jRadioRunoffCoefficient.isSelected());
    }//GEN-LAST:event_jRadioRunoffCoefficientActionPerformed

    /**
     * Tests for the module
     * @param args the command line arguments
     */
    public static void main (String args[]) {
        
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
            
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Gila River DB/Rasters/Topography/1_ArcSec/mogollon.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Gila River DB/Rasters/Topography/1_ArcSec/mogollon.dir"));
            
//            java.io.File theFile=new java.io.File("/hidrosigDataBases/Rio Salado DB/Rasters/Topography/NED_26084992.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Rio Salado DB/Rasters/Topography/NED_26084992.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            
            hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
            
            //new Rainfall_Runoff_Model(tempFrame,2,96,matDirs,magnitudes,metaModif).setVisible(true);
            //new Rainfall_Runoff_Model(tempFrame,1063,496,matDirs,magnitudes,metaModif).show();
            //new Rainfall_Runoff_Model(tempFrame,82,260,matDirs,magnitudes,metaModif).setVisible(true);
            new Rainfall_Runoff_Model(tempFrame,282,298,matDirs,magnitudes,metaModif).setVisible(true);
            //new Rainfall_Runoff_Model(tempFrame,5173,1252,matDirs,magnitudes,metaModif).setVisible(true);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        

        System.exit(0);
        
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox finalDateList;
    private javax.swing.JTextField flowVelocity;
    private javax.swing.JTextField hillVelocity;
    private javax.swing.JTextField infilRate;
    private javax.swing.JTextField infiltrationIntensity;
    private javax.swing.ButtonGroup infiltrationModel;
    private javax.swing.JComboBox iniDateList;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel17;
    private javax.swing.JLabel jLabel18;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel20;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
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
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JRadioButton jRadioInfiltrationIntensity;
    private javax.swing.JRadioButton jRadioRunoffCoefficient;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField lambda1;
    private javax.swing.JTextField lambda2;
    private javax.swing.JButton openPrecip;
    private javax.swing.JTabbedPane panelOpciones;
    private javax.swing.JButton rainButton;
    private javax.swing.JTextField rainDuration;
    private javax.swing.JTextField rainIntensity;
    private javax.swing.JTextField runoffCoefficient;
    private javax.swing.JButton sim3Button;
    private javax.swing.JButton simButton;
    private javax.swing.JComboBox stormFilesList;
    private javax.swing.JTextField totalVolume;
    // End of variables declaration//GEN-END:variables

    
    /**
     * A required method to handle interaction with the various visad.Display
     * @param displayEvent The interaction event
     * @throws visad.VisADException Errors while handling VisAD objects
     * @throws java.rmi.RemoteException Errors while assigning data to VisAD objects
     */
    public void displayChanged(visad.DisplayEvent displayEvent) throws visad.VisADException, java.rmi.RemoteException {
        
        double[] CurPosDisD=activeDisplay.getDisplayRenderer().getCursor();
        float[] curPosDisF=new float[2];  
        curPosDisF[0]=new Double(CurPosDisD[0]).floatValue();
        curPosDisF[1]=new Double(CurPosDisD[1]).floatValue();
        
        double limitY=(double)matrizPintada.length/(double) matrizPintada[0].length;
        
        int MatX=(int) ((curPosDisF[0]+1)/2.0*matrizPintada[0].length);
        int MatY=(int) ((curPosDisF[1]+limitY)/(2.0*limitY)*matrizPintada.length);
        //jTextField1.setText(curPosDisF[0]+" "+curPosDisF[1]);
        if (MatX >= 0 && MatY >= 0 && MatX < matrizPintada[0].length && MatY < matrizPintada.length ) {
            if (storm != null){
                jTextField1.setText(MatX+" "+MatY+" "+matrizPintada[MatY][MatX]+" "+alturasCuenca[0][MatY][MatX]+" "+(matrizPintada[MatY][MatX]>0?storm.getTotalHillSlopeBasedPrec(matrizPintada[MatY][MatX]-1):-999.0));
                jTextField2.setText(MatX+" "+MatY+" "+matrizPintada[MatY][MatX]+" "+alturasCuenca[0][MatY][MatX]+" "+(matrizPintada[MatY][MatX]>0?storm.getTotalHillSlopeBasedPrec(matrizPintada[MatY][MatX]-1):-999.0));
            } else {
                jTextField1.setText(MatX+" "+MatY+" "+matrizPintada[MatY][MatX]+" "+alturasCuenca[0][MatY][MatX]);
                jTextField2.setText(" ");
            }
        } else {
            jTextField1.setText(" ");
            jTextField2.setText(" ");
        }
    }
    
    
}

//import java.io.*;
//import javax.swing.*;

class TextAreaPrintStream extends java.io.PrintStream {
    
//The JTextArea to wich the output stream will be redirected.
    private javax.swing.JTextArea textArea;
    private final static String newline = "\n";
    /**
     * Method TextAreaPrintStream
     * The constructor of the class.
     * @param the JTextArea to wich the output stream will be redirected.
     * @param a standard output stream (needed by super method)
     **/
    public TextAreaPrintStream(javax.swing.JTextArea area, java.io.OutputStream out) {
        super(out);
        textArea = area;
    }
    
    /**
     * Method println
     * @param the String to be output in the JTextArea textArea (private
     * attribute of the class).
     * After having printed such a String, prints a new line.
     **/
    public void println(String string) {
        textArea.insert( string + newline,0);
    }
    
    /**
     * Method print
     * @param the String to be output in the JTextArea textArea (private
     * attribute of the class).
     **/
    public void print(String string) {
        textArea.append(string);
    }
}
