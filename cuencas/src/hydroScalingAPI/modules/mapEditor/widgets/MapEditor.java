/*
 * MapEditor.java
 *
 * Created on February 22, 2007, 8:15 AM
 */

package hydroScalingAPI.modules.mapEditor.widgets;

import java.io.FileNotFoundException;
import java.io.IOException;
import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;

/**
 * The MapEditor form allows the user to interact with values of a map in the
 * CUENCAS data base.  The user can overwrite the map with its changes or create a
 * copy
 * @author Ricardo Mantilla
 */
public class MapEditor extends javax.swing.JDialog  implements DisplayListener{
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    private hydroScalingAPI.io.MetaRaster metaData;
    private DisplayImplJ3D display;
    private visad.java3d.DisplayRendererJ3D dr;
    private ScalarMap lonMap,latMap,varMap;
    
    private RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);;
    private RealType varRaster;
    private RealTupleType tuplarango;
    private FunctionType funcionTransfer;
    
    private float[][] matrixData;
    private visad.FlatField localField;
    
    int MatX,MatY;
    boolean hasValues;
    
    
    /**
     * Creates new form MapEditor
     * @param parent The main GIS interface
     * @param md The MetaRaster asociated with the data to be modified
     * @throws java.rmi.RemoteException Captures RemoteExpceptions
     * @throws visad.VisADException Captures VisAD expceptions
     * @throws java.io.IOException Captures errors while reading/writing information to/from the file
     */
    public MapEditor(hydroScalingAPI.mainGUI.ParentGUI parent,hydroScalingAPI.io.MetaRaster md)  throws RemoteException, VisADException, java.io.IOException{
        super(parent, true);
        initComponents();
        
        metaData=md;
        mainFrame=parent;
        
        //Set up general interface
        setBounds(0,0, 950, 700);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        dr=new visad.java3d.TwoDDisplayRendererJ3D();
        display = new DisplayImplJ3D("display",dr);
        
        GraphicsModeControl dispGMC = (GraphicsModeControl) display.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);
        
        varRaster=RealType.getRealType("varColor");
        
        lonMap = new ScalarMap( RealType.Longitude, Display.XAxis );
        latMap = new ScalarMap( RealType.Latitude, Display.YAxis );
        varMap = new ScalarMap( varRaster,  Display.RGB );
        
        display.addMap( lonMap );
        display.addMap( latMap );
        display.addMap( varMap );
        
        lonMap.setRange(metaData.getMinLon(),metaData.getMaxLon());
        latMap.setRange(metaData.getMinLat(),metaData.getMaxLat());
        
        localField=metaData.getField();
        matrixData=metaData.getArray();
        
        ProjectionControl pc = display.getProjectionControl();
        pc.setAspectCartesian(new double[] {1.0, (double) (matrixData.length/(double) matrixData[0].length)});
        
        addWheelFunctionality(display);
        display.enableEvent(visad.DisplayEvent.MOUSE_MOVED);
        display.addDisplayListener(this);
        
        visad.DataReferenceImpl ref_imaget1 = new visad.DataReferenceImpl("ref_imaget1");
        ref_imaget1.setData(localField);
        display.addReference(ref_imaget1);
        
        jPanel1.add("Center",display.getComponent());
        
        for(int i=-5;i<6;i++) dataTable.setValueAt("  "+(i>0?"+":"")+i,i+5,0);
        
    }
    
    private void addWheelFunctionality(final visad.java3d.DisplayImplJ3D display){
        final visad.java3d.DisplayRendererJ3D dr=(visad.java3d.DisplayRendererJ3D)display.getDisplayRenderer();
        display.getComponent().addMouseWheelListener(new java.awt.event.MouseWheelListener() {
            public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                int rot = e.getWheelRotation();
                try{
                    visad.ProjectionControl pc = display.getProjectionControl();
                    double[] scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                            1.0, 1.0, 1.0,
                            0.0, 0.0, 0.0);
                    double[] currentMatrix = pc.getMatrix();
                    // Zoom in
                    if (rot < 0){
                        scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                                1.1, 1.1, 1.1,
                                0.0, 0.0, 0.0);
                    }
                    // Zoom out
                    if (rot > 0){
                        scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                                0.9, 0.9, 0.9,
                                0.0, 0.0, 0.0);
                    }
                    scaleMatrix = dr.getMouseBehavior().multiply_matrix(scaleMatrix,currentMatrix);
                    pc.setMatrix(scaleMatrix);
                } catch (java.rmi.RemoteException re) {} catch (visad.VisADException ve) {}
            }
        });
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        latitudeLabel = new javax.swing.JLabel();
        jPanel61 = new javax.swing.JPanel();
        jLabel31 = new javax.swing.JLabel();
        longitudeLabel = new javax.swing.JLabel();
        jPanel62 = new javax.swing.JPanel();
        jLabel32 = new javax.swing.JLabel();
        valueLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        dataTable = new javax.swing.JTable();
        jPanel3 = new javax.swing.JPanel();
        overButton = new javax.swing.JButton();
        copyButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridLayout(1, 2));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.GridLayout(1, 3));

        jPanel6.setLayout(new java.awt.BorderLayout());

        jLabel3.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel3.setText("Latitude : ");
        jPanel6.add(jLabel3, java.awt.BorderLayout.WEST);

        latitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        latitudeLabel.setText("00:00:00.00 N [000]");
        jPanel6.add(latitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel6);

        jPanel61.setLayout(new java.awt.BorderLayout());

        jLabel31.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel31.setText("Longitude : ");
        jPanel61.add(jLabel31, java.awt.BorderLayout.WEST);

        longitudeLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        longitudeLabel.setText("00:00:00.00 W [000]");
        jPanel61.add(longitudeLabel, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel61);

        jPanel62.setLayout(new java.awt.BorderLayout());

        jLabel32.setFont(new java.awt.Font("Dialog", 1, 10));
        jLabel32.setText("Value : ");
        jPanel62.add(jLabel32, java.awt.BorderLayout.WEST);

        valueLabel.setFont(new java.awt.Font("Dialog", 0, 10));
        valueLabel.setText("0000");
        jPanel62.add(valueLabel, java.awt.BorderLayout.CENTER);

        jPanel4.add(jPanel62);

        jPanel1.add(jPanel4, java.awt.BorderLayout.NORTH);

        getContentPane().add(jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        dataTable.setFont(new java.awt.Font("Lucida Grande", 0, 8));
        dataTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null},
                {null, null, null, null, null, null, null, null, null, null, null, null}
            },
            new String [] {
                "<>", "-5", "-4", "-3", "-2", "-1", "0", "1", "2", "3", "4", "5"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Object.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class, java.lang.Float.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        dataTable.setAutoCreateColumnsFromModel(false);
        dataTable.setCellSelectionEnabled(true);
        dataTable.setGridColor(new java.awt.Color(0, 0, 0));
        dataTable.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                dataTablePropertyChange(evt);
            }
        });
        dataTable.addVetoableChangeListener(new java.beans.VetoableChangeListener() {
            public void vetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {
                dataTableVetoableChange(evt);
            }
        });

        jScrollPane1.setViewportView(dataTable);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel3.setLayout(new java.awt.GridLayout(1, 3));

        overButton.setText("Overwrite File");
        overButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                overButtonActionPerformed(evt);
            }
        });

        jPanel3.add(overButton);

        copyButton.setText("Create Copy");
        copyButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copyButtonActionPerformed(evt);
            }
        });

        jPanel3.add(copyButton);

        cancelButton.setText("Exit");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });

        jPanel3.add(cancelButton);

        jPanel2.add(jPanel3, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel2);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        setVisible(false);
        dispose();
    }//GEN-LAST:event_formWindowClosing

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        setVisible(false);
        dispose();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void copyButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_copyButtonActionPerformed
// TODO add your handling code here:
    }//GEN-LAST:event_copyButtonActionPerformed

    private void overButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_overButtonActionPerformed
        if(!hasValues) return;
        try {
            
            java.io.FileOutputStream        outputDir;
            java.io.DataOutputStream        newfile;
            java.io.BufferedOutputStream    bufferout;
            
            outputDir = new java.io.FileOutputStream(metaData.getLocationBinaryFile());
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.DataOutputStream(bufferout);
            
            if (metaData.getProperty("[Format]").equalsIgnoreCase("Byte"))
                for (int i=0;i<matrixData.length;i++) for(int j=0;j<matrixData[0].length;j++) newfile.writeByte((byte)matrixData[i][j]);
            if (metaData.getProperty("[Format]").equalsIgnoreCase("Integer"))
                for (int i=0;i<matrixData.length;i++) for(int j=0;j<matrixData[0].length;j++) newfile.writeInt((int)matrixData[i][j]);
            if (metaData.getProperty("[Format]").equalsIgnoreCase("Float"))
                for (int i=0;i<matrixData.length;i++) for(int j=0;j<matrixData[0].length;j++) newfile.writeFloat(matrixData[i][j]);
            if (metaData.getProperty("[Format]").equalsIgnoreCase("Double"))
                for (int i=0;i<matrixData.length;i++) for(int j=0;j<matrixData[0].length;j++) newfile.writeDouble((double)matrixData[i][j]);
            
            newfile.close();
            bufferout.close();
            outputDir.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
    }//GEN-LAST:event_overButtonActionPerformed

    private void dataTablePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_dataTablePropertyChange
        if(!hasValues) return;
        for(int i=MatY-5;i<MatY+6;i++){
            for(int j=MatX-5;j<MatX+6;j++){
                if(i>=0&&i<matrixData.length && j>=0&&j<matrixData[0].length){
                    matrixData[i][j]=(Float)dataTable.getValueAt(5-i+MatY,j-MatX+5+1);
                }
            }
        }
    }//GEN-LAST:event_dataTablePropertyChange

    private void dataTableVetoableChange(java.beans.PropertyChangeEvent evt)throws java.beans.PropertyVetoException {//GEN-FIRST:event_dataTableVetoableChange
        System.out.println("Something has changed");
    }//GEN-LAST:event_dataTableVetoableChange
    
    private void setLatitudeLabel(String latLabel){
        latitudeLabel.setText(latLabel);
    }
    private void setLongitudeLabel(String lonLabel){
        longitudeLabel.setText(lonLabel);
    }
    
    private void setValueLabel(String valLabel){
        valueLabel.setText(valLabel);
    }
    
    /**
     * The implementation of the displayChanged method to handle interactions with the
     * visad.Display
     * @param DispEvt The interaction event over the visad.Display
     * @throws visad.VisADException Captures errors while responding the user interaction
     * @throws java.rmi.RemoteException Captures errors while communicating with VisAD data objects
     */
    public void displayChanged(visad.DisplayEvent DispEvt) throws visad.VisADException, java.rmi.RemoteException {
        
        int id = DispEvt.getId();
        
        try {
            if (id == DispEvt.MOUSE_MOVED) {
                
                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());
                
                float resultX= lonMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                float resultY= latMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];
                
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
            
            if (id == DispEvt.MOUSE_RELEASED_RIGHT) {
                
                hasValues=true;
                
                visad.VisADRay ray = dr.getMouseBehavior().findRay(DispEvt.getX(), DispEvt.getY());
                
                float resultX= lonMap.inverseScaleValues(new float[] {(float)ray.position[0]})[0];
                float resultY= latMap.inverseScaleValues(new float[] {(float)ray.position[1]})[0];
                
                MatX=(int) ((resultX -metaData.getMinLon())/(float) metaData.getResLon()*3600.0f);
                MatY=(int) ((resultY -metaData.getMinLat())/(float) metaData.getResLat()*3600.0f);
                
                for(int i=MatY-5;i<MatY+6;i++){
                    for(int j=MatX-5;j<MatX+6;j++){
                        if(i>=0&&i<matrixData.length && j>=0&&j<matrixData[0].length){
                            dataTable.setValueAt(new Float(matrixData[i][j]),5-i+MatY,j-MatX+5+1);
                        } else dataTable.setValueAt(new Float(Float.NaN),5-i+MatY,j-MatX+5+1);
                    }
                }
                
            }
            
        } catch (Exception e) {
            System.err.println(e);
        }
        
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try{
            java.io.File theFile=new java.io.File("/Users/ricardo/Documents/databases/Smallbasin_DB/Rasters/Topography/1_Arcsec/NED_06075640.metaDEM");
            //java.io.File theFile=new java.io.File("/hidrosigDataBases/Test_DB/Rasters/Topography/58447060.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dem"));
            
            hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
            
            new MapEditor(tempFrame,metaModif).setVisible(true);
            //new TRIBS_io(tempFrame, 111,80,matDirs,magnitudes,metaModif).setVisible(true);
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
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton copyButton;
    private javax.swing.JTable dataTable;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel31;
    private javax.swing.JLabel jLabel32;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel61;
    private javax.swing.JPanel jPanel62;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel latitudeLabel;
    private javax.swing.JLabel longitudeLabel;
    private javax.swing.JButton overButton;
    private javax.swing.JLabel valueLabel;
    // End of variables declaration//GEN-END:variables
    
}
