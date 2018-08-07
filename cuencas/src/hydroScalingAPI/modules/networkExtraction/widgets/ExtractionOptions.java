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


package hydroScalingAPI.modules.networkExtraction.widgets;

/**
 * This GUI allows the user to select the tasks that the NetworkExtractionModule
 * should perform on the current DEM
 * @author Jorge Mario Ramirez
 */
public class ExtractionOptions extends javax.swing.JDialog {
    
    private hydroScalingAPI.util.plot.XYJPanel Ppanel;
    /**
     * The {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     * associated to this interface
     */
    public hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule Proc;
    
    private double dx, dy;
    private boolean listo = false;
    /**
     * The list of flat zones and sinks detected over the entire DEM
     */
    public java.util.Vector myFSc;
    double[][] promsAP;
    double[][] APFuentes;
    
    
    /**
     * Creates new form OpProcesar but doesn't display it
     * @param Proc1 The {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     * to be associated to this interface
     */
    public ExtractionOptions(hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule Proc1) {
        super(Proc1.parent,true);
        initComponents();
        pack();
        
        if(Proc1.parent != null){
            setBounds(0,0, 550, 550);
            java.awt.Rectangle marcoParent=Proc1.parent.getBounds();
            java.awt.Rectangle thisMarco=this.getBounds();
            setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        }
        
        Proc = Proc1;
        inicio();
        //buscandoPits();
    }
    
    /**
     * Creates new form OpProcesar and displayes it
     * @param Proc1 The {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     * to be associated to this interface
     * @param b A boolean indicating that the interface must be set visible
     */
    public ExtractionOptions(hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule Proc1, boolean b) {
        super(Proc1.parent,false);
        initComponents();
        
        setBounds(0,0, 550, 550);
        java.awt.Rectangle marcoParent=Proc1.parent.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        Proc = Proc1;
        jTabbedPane1.setSelectedComponent(jPanelArea_Pend);
        graficaAP(hydroScalingAPI.modules.networkExtraction.objects.GetRasterNetwork.calculaPromAP(Proc),true);
        pack();
        setVisible(true);
    }
    
    private void inicio(){
        jComboBox1.setMaximumRowCount(2);
        jComboBox1.addItem(new String("cells"));
        jComboBox1.addItem(new String("km^2"));
        jComboBox3.setMaximumRowCount(2);
        jComboBox3.addItem(new String("cells"));
        jComboBox3.addItem(new String("km^2"));
        jComboBox4.setMaximumRowCount(2);
        jComboBox4.addItem(new String("row"));
        jComboBox4.addItem(new String("latitude"));
        jComboBox5.setMaximumRowCount(2);
        jComboBox5.addItem(new String("column"));
        jComboBox5.addItem(new String("longitude"));
        jComboBox2.setMaximumRowCount(2);
        jComboBox2.addItem(new String("cells"));
        jComboBox2.addItem(new String("km^2"));
        jComboBox2.setSelectedIndex(1);
        jComboBoxAreaAP.setMaximumRowCount(2);
        jComboBoxAreaAP.addItem(new String("cells"));
        jComboBoxAreaAP.addItem(new String("km^2"));
        jLabel14.setText(Proc.metaDEM.getUnits()+" have to be filled up");
        jTextField6.setText(new Float(Proc.cCorte).toString());
        jTextField7.setText(new Float(Proc.cAltura).toString());
        if(Proc.pixManual==Float.POSITIVE_INFINITY){
            jCheckBox4.setSelected(false);
            jTextField10.setEnabled(false);
            jComboBox1.setEnabled(false);
        }
        else{
            jTextField10.setText(new Float(Proc.pixManual).toString());
            jCheckBox4.setSelected(true);
        }
        
        if(Proc.pixCManual==Float.POSITIVE_INFINITY){
            jCheckBox6.setSelected(false);
            jTextField12.setEnabled(false);
            jComboBox3.setEnabled(false);
        }
        else{
            jTextField12.setText(new Float(Proc.pixManual).toString());
            jCheckBox6.setSelected(true);
        }
        if(Proc.pixPodado==0.0){
            jCheckBox2.setSelected(false);
            jTextField1.setEnabled(false);
            jComboBox2.setEnabled(false);
        }
        else{
            jTextField1.setText(new Float(Proc.pixPodado).toString());
            jCheckBox2.setSelected(true);
        }
        if(Proc.ordenMax==Integer.MAX_VALUE){
            jCheckBox5.setSelected(false);
            jTextField2.setEnabled(false);
            jComboBox3.setEnabled(false);
        }
        else{
            jTextField2.setText(new Integer(Proc.ordenMax).toString());
            jCheckBox5.setSelected(true);
        }
        if((Proc.unColM==0 && Proc.colManual==-100) || (Proc.unFilaM==0 && Proc.filaManual==-100)
        || (Proc.unColM==1 && Proc.lonManual==-100) || (Proc.unFilaM==1 && Proc.latManual==-100)){
            jCheckBox7.setSelected(false);
            jComboBox4.setEnabled(false);
            jTextField14.setEnabled(false);
            jLabel19.setEnabled(false);
            jComboBox5.setEnabled(false);
        }
        else{
            jCheckBox7.setSelected(true);
            if(Proc.unColM==0){
                jComboBox4.setSelectedIndex(0);
                jTextField13.setText(new Integer(Proc.colManual).toString());
            }
            else if(Proc.unColM==1){
                jComboBox4.setSelectedIndex(1);
                jTextField13.setText(new Float(Proc.lonManual).toString());
            }
            if(Proc.unFilaM==0){
                jComboBox5.setSelectedIndex(0);
                jTextField14.setText(new Integer(Proc.filaManual).toString());
            }
            else if(Proc.unFilaM==1){
                jComboBox5.setSelectedIndex(1);
                jTextField14.setText(new Float(Proc.latManual).toString());
            }
            
        }
        jCheckBoxTodoRed.setSelected(Proc.todoRed);
        jCheckBoxCleanShorts.setSelected(Proc.cleanShorts);
        jCheckBoxMontgomery.setSelected(Proc.montgomery);
        jButton4.setEnabled(jCheckBox4.isSelected() || jCheckBox6.isSelected() || jCheckBox7.isSelected());
        jRadioButton14.setSelected(Proc.archPro);
        jCheckBoxAreaPend_n.setSelected(Proc.areaPend_nuevo);
        if(Proc.areaPend_nuevo){
            jTextFieldAreaAP.setText(new Float(Proc.pixPodado).toString());
            jComboBoxAreaAP.setSelectedIndex(Proc.unPixAP);
        }
        else{
            jTextFieldAreaAP.setEnabled(false);
            jComboBoxAreaAP.setEnabled(false);
        }
        jCheckBoxAreaPend_c.setSelected(Proc.areaPend_cargar);
        jCheckBoxAP_LA.setSelected(Proc.areaPend_LA);
        jRadioButton16.setSelected(!jRadioButton14.isSelected());
        jTextFieldNPuntosAP.setText(new Integer(Proc.npuntosAP).toString());
        jCheckBoxLaplace.setSelected(Proc.laplace);
        jTextFieldCeldasConv.setEnabled(Proc.laplace);
        if(Proc.laplace){
            jTextFieldCeldasConv.setText(""+Proc.nCeldasConv);
        }
        jCheckBoxUmbralAP.setSelected(Proc.umbralAP);
        
        
        
        dy = 6378.0*Proc.metaDEM.getResLat()*Math.PI/(3600.0*180.0);
        dx = 6378.0*Proc.metaDEM.getResLon()*Math.PI/(3600.0*180.0);
        
        jCheckBoxDIR.setSelected(Proc.taskDIR); //jPanelSinks.setEnabled(Proc.taskDIR);
        jCheckBoxRED.setSelected(Proc.taskRED); //jPanelRedRas.setEnabled(Proc.taskRED);
        jCheckBoxGEO.setSelected(Proc.taskGEO); //jPanelGetGeo.setEnabled(Proc.taskGEO);
        jCheckBoxVECT.setSelected(Proc.taskVECT);
    }
    
    /**
     * Aplies the findPits() algorithm over the entire DEM and uses the prameters to
     * update information in the GUI
     */
    public void buscandoPits(){
        jLabelBarra.setText("Searching for sinks y and flat zones");

        myFSc = Proc.MT.findPits2(-2.0);

        java.util.Vector F=(java.util.Vector) myFSc.get(0); 
        java.util.Vector S=(java.util.Vector) myFSc.get(1);
        
        jLabelBarra.setText("There are " + F.size() + " flat zones and " + S.size() + "  sinks");
    }
    
    /**
     * Creates the graph for the Area-Slope analysis
     * @param promsAP1 Binned Areas and Slopes
     * @param inicial A boolean indicating if it is the first time the graph will be made
     */
    public void graficaAP(double[][] promsAP1, boolean inicial){
        // Estos valores son logaritmos de A  y P respectivamente
        promsAP = promsAP1;
        double iniA, endA, iniP, endP;
        double alfa, C;
        double[][] promsAP_red, promsAP_lad;
        if(promsAP[0][0]<=0){
            int cont_red = 0;
            for(int k=0; k<promsAP[0].length; k++){
                if(promsAP[0][k]<0) cont_red++;
            }
            promsAP_red = new double[2][cont_red];
            promsAP_lad = new double[2][(promsAP[0].length)-cont_red];
            for(int k=0; k<cont_red; k++){
                promsAP_red[0][k] = -1.0*promsAP[0][k];
                promsAP_red[1][k] = promsAP[1][k];
            }
            for(int k=cont_red; k<promsAP[0].length; k++){
                promsAP_lad[0][k-cont_red] = promsAP[0][k];
                promsAP_lad[1][k-cont_red] = promsAP[1][k];
            }
        }
        else{ promsAP_lad = promsAP; promsAP_red = null;}
        iniA = promsAP_lad[0][0];
        if(promsAP_red==null)
            endA = promsAP_lad[0][promsAP_lad[0].length-1];
        else endA = promsAP_red[0][0];
        if(inicial){
            iniP = promsAP_lad[1][0];
            endP = promsAP_lad[1][promsAP_lad[0].length-1];
            /*alfa = (iniA-endA)/(endP-iniP);
            C = Math.exp(endA + alfa*endP);*/
            alfa = 1.16; C = 200.0;
            jTextFieldAlfa.setText(""+alfa); jTextFieldC.setText(""+C);
        }
        else{
            alfa = new Double(jTextFieldAlfa.getText()).doubleValue();
            C = (new Double(jTextFieldC.getText()).doubleValue());
            System.out.println("Reading: "+alfa+" "+C);
            iniP = (1/alfa)*(Math.log(C)-iniA);
            endP = (1/alfa)*(Math.log(C)-endA);
        }
        if(jPanelArea_Pend.getComponentCount()>1)
            jPanelArea_Pend.remove(Ppanel);
        Ppanel = new hydroScalingAPI.util.plot.XYJPanel(promsAP_lad[0],promsAP_lad[1], "Area-Slope Relation for A >"+Proc.pixPodado+" pixels",
        "ln(A)" , " ln(S) ",-10d,java.awt.Color.red,8);
        if(promsAP_red != null) Ppanel.addDatos(promsAP_red[0],promsAP_red[1],-10,java.awt.Color.green,8);
        /*if(Proc.areaPend_LA){
            if(inicial)
                APFuentes = GetRasterNetwork.getAP_Fuentes(Proc);
            Ppanel.addDatos(APFuentes[0],APFuentes[1],-10,java.awt.Color.blue,8);
        }*/
        
        double pLinea[][] = {{iniA,endA},{iniP,endP}};
        Ppanel.addDatos(pLinea[0], pLinea[1],-10,java.awt.Color.blue,0);
        jPanelArea_Pend.add(Ppanel, java.awt.BorderLayout.CENTER);
        Ppanel.updateUI();
    }
    
    
    
    /**
     * Indicates to the interface that the NetworkExtractionModule has finished the
     * required tasks
     */
    public void set_ready(){
        jButtonAplicar.setEnabled(true);
        jButtonIniciar.setEnabled(true);
    }
    
    /**
     * Sets bounds for the Optimizer status bar
     * @param min The minimum value
     * @param max The maximum value
     */
    public void setMaxMinOptimizerBar(int min, int max){
        optimizationProgressBar.setMinimum(min);
        optimizationProgressBar.setMaximum(max);
        optimizationProgressBar.setValue(0);
    }
    
    /**
     * Updates the status of the Optimizer status bar
     */
    public void increaseValueOptimizerBar(){
        optimizationProgressBar.setValue(optimizationProgressBar.getValue()+1);
    }
    
    /**
     * Updates the value of the Optimizer status bar
     */
    public void setValueOptimizerBar(int value){
        optimizationProgressBar.setValue(value);
    }
    
    /**
     * Sets bounds for the Extraction status bar
     * @param min The minimum elevation of the terrain
     * @param max The maximum elevation of the terrain
     */
    public void setMaxMinExtractionBar(int min, int max){
        extractionProgressBar.setMinimum(min);
        extractionProgressBar.setMaximum(max);
        extractionProgressBar.setValue(0);
    }
    
    /**
     * Updates the status of the Extraction status bar
     * @param value The current elevation
     */
    public void setValueExtractionBar(int value){
        extractionProgressBar.setValue(extractionProgressBar.getMaximum()-value+extractionProgressBar.getMinimum());
    }
    
    /**
     * Sets bounds for the Extraction status bar
     * @param min The minimum elevation of the terrain
     * @param max The maximum elevation of the terrain
     */
    public void setMaxMinGeomorphBar(int min, int max){
        geomorphProgressBar.setMinimum(min);
        geomorphProgressBar.setMaximum(max);
        geomorphProgressBar.setValue(0);
    }
    
    /**
     * Updates the status of the Extraction status bar
     * @param value The current elevation
     */
    public void setValueGeomorphBar(int value){
        geomorphProgressBar.setValue(value);
    }
    
    /**
     * Announces that the Algorithm has failed to complete the extraction process. 
     * Manual modifications of the DEM may be needed
     */
    public void announceFailure(){
        extractionProgressBar.setValue(extractionProgressBar.getMinimum());
        Object [] tmp = new Object[]{"Accept"};
        javax.swing.JOptionPane.showOptionDialog((java.awt.Component)this, "The Network Extraction Algorithm has Failed to Converge! \n This is usually due to a noisy DEM or a pourly defined drainage system (Flat DEMs).", "Attention",javax.swing.JOptionPane.INFORMATION_MESSAGE, javax.swing.JOptionPane.OK_OPTION, null,tmp,tmp[0]);
    }
    
    
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanelTareas = new javax.swing.JPanel();
        jPanelTerasInt = new javax.swing.JPanel();
        jCheckBoxDIR = new javax.swing.JCheckBox();
        jCheckBoxRED = new javax.swing.JCheckBox();
        jCheckBoxGEO = new javax.swing.JCheckBox();
        jCheckBoxVECT = new javax.swing.JCheckBox();
        jPanelGetGeo = new javax.swing.JPanel();
        jRadioButton14 = new javax.swing.JRadioButton();
        jRadioButton16 = new javax.swing.JRadioButton();
        jPanelSinks = new javax.swing.JPanel();
        jPanel18 = new javax.swing.JPanel();
        jPanel19 = new javax.swing.JPanel();
        jTextField10 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox();
        jCheckBox4 = new javax.swing.JCheckBox();
        jPanel25 = new javax.swing.JPanel();
        jTextField12 = new javax.swing.JTextField();
        jComboBox3 = new javax.swing.JComboBox();
        jCheckBox6 = new javax.swing.JCheckBox();
        jPanel26 = new javax.swing.JPanel();
        jTextField13 = new javax.swing.JTextField();
        jComboBox4 = new javax.swing.JComboBox();
        jCheckBox7 = new javax.swing.JCheckBox();
        jComboBox5 = new javax.swing.JComboBox();
        jTextField14 = new javax.swing.JTextField();
        jLabel19 = new javax.swing.JLabel();
        jButton4 = new javax.swing.JButton();
        jPanel13 = new javax.swing.JPanel();
        jPanel16 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        jTextField6 = new javax.swing.JTextField();
        jLabel12 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        jPanel17 = new javax.swing.JPanel();
        jLabel13 = new javax.swing.JLabel();
        jTextField7 = new javax.swing.JTextField();
        jLabel14 = new javax.swing.JLabel();
        jCheckBox3 = new javax.swing.JCheckBox();
        jPanelRedRas = new javax.swing.JPanel();
        jPanelIniCanales = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jCheckBoxTodoRed = new javax.swing.JCheckBox();
        jPanel10 = new javax.swing.JPanel();
        jCheckBoxCleanShorts = new javax.swing.JCheckBox();
        jPanel3 = new javax.swing.JPanel();
        jCheckBox2 = new javax.swing.JCheckBox();
        jTextField1 = new javax.swing.JTextField();
        jComboBox2 = new javax.swing.JComboBox();
        jPanel4 = new javax.swing.JPanel();
        jCheckBox5 = new javax.swing.JCheckBox();
        jTextField2 = new javax.swing.JTextField();
        jPanel7 = new javax.swing.JPanel();
        jCheckBoxUmbralAP = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jTextFieldAlfaU = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jTextFieldCU = new javax.swing.JTextField();
        jPanel9 = new javax.swing.JPanel();
        jCheckBoxMontgomery = new javax.swing.JCheckBox();
        jPanelTopoConv = new javax.swing.JPanel();
        jCheckBoxLaplace = new javax.swing.JCheckBox();
        jTextFieldCeldasConv = new javax.swing.JTextField();
        jPaneLAzules = new javax.swing.JPanel();
        jCheckBoxLAzules = new javax.swing.JCheckBox();
        jButtonBuscarLA = new javax.swing.JButton();
        jCheckBoxAP_LA = new javax.swing.JCheckBox();
        jPanelOp_AreaPend = new javax.swing.JPanel();
        jCheckBoxAreaPend_c = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();
        jTextFieldAreaAP = new javax.swing.JTextField();
        jComboBoxAreaAP = new javax.swing.JComboBox();
        jCheckBoxAreaPend_n = new javax.swing.JCheckBox();
        jPanelArea_Pend = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldNPuntosAP = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextFieldAlfa = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jTextFieldC = new javax.swing.JTextField();
        jButtonActualizarAP = new javax.swing.JButton();
        jPanel28 = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jLabelBarra1 = new javax.swing.JLabel();
        optimizationProgressBar = new javax.swing.JProgressBar();
        jPanel5 = new javax.swing.JPanel();
        jLabelBarra = new javax.swing.JLabel();
        extractionProgressBar = new javax.swing.JProgressBar();
        jPanel11 = new javax.swing.JPanel();
        jLabelBarra2 = new javax.swing.JLabel();
        geomorphProgressBar = new javax.swing.JProgressBar();
        jPanelBotones = new javax.swing.JPanel();
        jButtonAplicar = new javax.swing.JButton();
        jButtonIniciar = new javax.swing.JButton();
        jButtonCanc_Salir = new javax.swing.JButton();

        setTitle("Network Extraction Options");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanelTareas.setPreferredSize(new java.awt.Dimension(507, 78));
        jPanelTareas.setLayout(new java.awt.BorderLayout());

        jPanelTerasInt.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelTerasInt.setLayout(new java.awt.GridLayout(4, 1));

        jCheckBoxDIR.setFont(new java.awt.Font("Arial", 0, 12));
        jCheckBoxDIR.setText("Get the directions matrix");
        jCheckBoxDIR.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxDIRActionPerformed(evt);
            }
        });
        jPanelTerasInt.add(jCheckBoxDIR);

        jCheckBoxRED.setFont(new java.awt.Font("Arial", 0, 12));
        jCheckBoxRED.setText("Prune the river network");
        jCheckBoxRED.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxREDActionPerformed(evt);
            }
        });
        jPanelTerasInt.add(jCheckBoxRED);

        jCheckBoxGEO.setFont(new java.awt.Font("Arial", 0, 12)); // NOI18N
        jCheckBoxGEO.setText("Calculate Geomorphic Properties");
        jCheckBoxGEO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxGEOActionPerformed(evt);
            }
        });
        jPanelTerasInt.add(jCheckBoxGEO);

        jCheckBoxVECT.setFont(new java.awt.Font("Arial", 0, 12));
        jCheckBoxVECT.setText("Get vectorial river network");
        jPanelTerasInt.add(jCheckBoxVECT);

        jPanelTareas.add(jPanelTerasInt, java.awt.BorderLayout.CENTER);

        jPanelGetGeo.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Geomophology Algorithm", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12))); // NOI18N
        jPanelGetGeo.setLayout(new java.awt.GridLayout(2, 1));

        jRadioButton14.setFont(new java.awt.Font("Arial", 0, 12));
        jRadioButton14.setText("Hard Disk Based Algorithm");
        jRadioButton14.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton14ActionPerformed(evt);
            }
        });
        jPanelGetGeo.add(jRadioButton14);

        jRadioButton16.setFont(new java.awt.Font("Arial", 0, 12));
        jRadioButton16.setSelected(true);
        jRadioButton16.setText("RAM Based Algorithm");
        jRadioButton16.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton16ActionPerformed(evt);
            }
        });
        jPanelGetGeo.add(jRadioButton16);

        jPanelTareas.add(jPanelGetGeo, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("Tasks", null, jPanelTareas, "");

        jPanelSinks.setLayout(new java.awt.GridLayout(2, 1));

        jPanel18.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Human Intervention on Sinks :", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12))); // NOI18N
        jPanel18.setPreferredSize(new java.awt.Dimension(507, 118));
        jPanel18.setLayout(new java.awt.GridBagLayout());

        jPanel19.setPreferredSize(new java.awt.Dimension(300, 15));
        jPanel19.setLayout(new java.awt.GridBagLayout());

        jTextField10.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextField10.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField10.setMinimumSize(new java.awt.Dimension(40, 15));
        jTextField10.setPreferredSize(new java.awt.Dimension(40, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel19.add(jTextField10, gridBagConstraints);

        jComboBox1.setFont(new java.awt.Font("Dialog", 0, 10));
        jComboBox1.setMinimumSize(new java.awt.Dimension(80, 20));
        jComboBox1.setPreferredSize(new java.awt.Dimension(100, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel19.add(jComboBox1, gridBagConstraints);

        jCheckBox4.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBox4.setText("If Sink Area is Larger Than");
        jCheckBox4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel19.add(jCheckBox4, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.VERTICAL;
        gridBagConstraints.ipady = 7;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel18.add(jPanel19, gridBagConstraints);

        jPanel25.setLayout(new java.awt.GridBagLayout());

        jTextField12.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextField12.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField12.setMinimumSize(new java.awt.Dimension(40, 15));
        jTextField12.setPreferredSize(new java.awt.Dimension(40, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        jPanel25.add(jTextField12, gridBagConstraints);

        jComboBox3.setFont(new java.awt.Font("Dialog", 0, 10));
        jComboBox3.setMinimumSize(new java.awt.Dimension(40, 20));
        jComboBox3.setPreferredSize(new java.awt.Dimension(80, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel25.add(jComboBox3, gridBagConstraints);

        jCheckBox6.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBox6.setText("If Basin Area is Greater Than");
        jCheckBox6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox6ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel25.add(jCheckBox6, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel18.add(jPanel25, gridBagConstraints);

        jPanel26.setLayout(new java.awt.GridBagLayout());

        jTextField13.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextField13.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField13.setMinimumSize(new java.awt.Dimension(40, 15));
        jTextField13.setPreferredSize(new java.awt.Dimension(40, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        jPanel26.add(jTextField13, gridBagConstraints);

        jComboBox4.setFont(new java.awt.Font("Dialog", 0, 10));
        jComboBox4.setMinimumSize(new java.awt.Dimension(50, 15));
        jComboBox4.setPreferredSize(new java.awt.Dimension(50, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        jPanel26.add(jComboBox4, gridBagConstraints);

        jCheckBox7.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBox7.setText("Containing Point");
        jCheckBox7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox7ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        jPanel26.add(jCheckBox7, gridBagConstraints);

        jComboBox5.setFont(new java.awt.Font("Dialog", 0, 10));
        jComboBox5.setMinimumSize(new java.awt.Dimension(50, 20));
        jComboBox5.setPreferredSize(new java.awt.Dimension(80, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 5;
        gridBagConstraints.gridy = 1;
        jPanel26.add(jComboBox5, gridBagConstraints);

        jTextField14.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextField14.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField14.setMinimumSize(new java.awt.Dimension(40, 15));
        jTextField14.setPreferredSize(new java.awt.Dimension(40, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 6;
        gridBagConstraints.gridy = 1;
        jPanel26.add(jTextField14, gridBagConstraints);

        jLabel19.setFont(new java.awt.Font("Dialog", 0, 12));
        jLabel19.setText(" , ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 4;
        gridBagConstraints.gridy = 1;
        jPanel26.add(jLabel19, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel18.add(jPanel26, gridBagConstraints);

        jButton4.setText("Number of Human Intervations");
        jButton4.setEnabled(false);
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        jPanel18.add(jButton4, gridBagConstraints);

        jPanelSinks.add(jPanel18);

        jPanel13.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Cut Sinks Every Time That :", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12))); // NOI18N
        jPanel13.setLayout(new java.awt.GridBagLayout());

        jPanel16.setLayout(new java.awt.GridBagLayout());

        jLabel11.setFont(new java.awt.Font("Arial", 0, 12));
        jLabel11.setText("The filled number of cells is larger than");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel16.add(jLabel11, gridBagConstraints);

        jTextField6.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextField6.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField6.setMinimumSize(new java.awt.Dimension(45, 15));
        jTextField6.setPreferredSize(new java.awt.Dimension(45, 15));
        jTextField6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField6ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        jPanel16.add(jTextField6, gridBagConstraints);

        jLabel12.setFont(new java.awt.Font("Arial", 0, 12));
        jLabel12.setText("times the number of cells to cut");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        jPanel16.add(jLabel12, gridBagConstraints);

        jCheckBox1.setSelected(true);
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel16.add(jCheckBox1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel13.add(jPanel16, gridBagConstraints);

        jPanel17.setLayout(new java.awt.GridBagLayout());

        jLabel13.setFont(new java.awt.Font("Arial", 0, 12));
        jLabel13.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel13.setText("More than ");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel17.add(jLabel13, gridBagConstraints);

        jTextField7.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextField7.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField7.setMinimumSize(new java.awt.Dimension(45, 15));
        jTextField7.setPreferredSize(new java.awt.Dimension(45, 15));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel17.add(jTextField7, gridBagConstraints);

        jLabel14.setFont(new java.awt.Font("Arial", 0, 12));
        jLabel14.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel14.setText(" meters have to be filled up.");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel17.add(jLabel14, gridBagConstraints);

        jCheckBox3.setSelected(true);
        jCheckBox3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox3ActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        jPanel17.add(jCheckBox3, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel13.add(jPanel17, gridBagConstraints);

        jPanelSinks.add(jPanel13);

        jTabbedPane1.addTab("Sinks", null, jPanelSinks, "");

        jPanelRedRas.setPreferredSize(new java.awt.Dimension(507, 78));
        jPanelRedRas.setLayout(new java.awt.GridBagLayout());

        jPanelIniCanales.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelIniCanales.setLayout(new java.awt.GridLayout(7, 0));

        jPanel2.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jCheckBoxTodoRed.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxTodoRed.setSelected(true);
        jCheckBoxTodoRed.setText("All cells are part of the river network (No Prunning)");
        jPanel2.add(jCheckBoxTodoRed);

        jPanelIniCanales.add(jPanel2);

        jPanel10.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jCheckBoxCleanShorts.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxCleanShorts.setSelected(true);
        jCheckBoxCleanShorts.setText("Prune Short Tributaries (1 pixel long)");
        jPanel10.add(jCheckBoxCleanShorts);

        jPanelIniCanales.add(jPanel10);

        jPanel3.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jCheckBox2.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBox2.setText("Prune by Area Threshold (A < )");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });
        jPanel3.add(jCheckBox2);

        jTextField1.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextField1.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField1.setText("0.01");
        jTextField1.setMinimumSize(new java.awt.Dimension(60, 15));
        jTextField1.setPreferredSize(new java.awt.Dimension(70, 20));
        jPanel3.add(jTextField1);

        jComboBox2.setFont(new java.awt.Font("Dialog", 0, 10));
        jComboBox2.setMinimumSize(new java.awt.Dimension(60, 16));
        jComboBox2.setPreferredSize(new java.awt.Dimension(60, 20));
        jPanel3.add(jComboBox2);

        jPanelIniCanales.add(jPanel3);

        jPanel4.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jCheckBox5.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBox5.setText("Prune by Horton Order (Order <)");
        jCheckBox5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox5ActionPerformed(evt);
            }
        });
        jPanel4.add(jCheckBox5);

        jTextField2.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextField2.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextField2.setMinimumSize(new java.awt.Dimension(40, 20));
        jTextField2.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel4.add(jTextField2);

        jPanelIniCanales.add(jPanel4);

        jPanel7.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jCheckBoxUmbralAP.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxUmbralAP.setText("Prune by Power Threshold (AS^a > C)");
        jCheckBoxUmbralAP.setActionCommand("areaPendiente");
        jCheckBoxUmbralAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxUmbralAPActionPerformed(evt);
            }
        });
        jPanel7.add(jCheckBoxUmbralAP);

        jLabel4.setText("a =");
        jPanel7.add(jLabel4);

        jTextFieldAlfaU.setMinimumSize(new java.awt.Dimension(20, 20));
        jTextFieldAlfaU.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel7.add(jTextFieldAlfaU);

        jLabel5.setText("   C =");
        jPanel7.add(jLabel5);

        jTextFieldCU.setMinimumSize(new java.awt.Dimension(20, 20));
        jTextFieldCU.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanel7.add(jTextFieldCU);

        jPanelIniCanales.add(jPanel7);

        jPanel9.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jCheckBoxMontgomery.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxMontgomery.setSelected(true);
        jCheckBoxMontgomery.setText("Apply the Montgomery and Ditrich criteria");
        jPanel9.add(jCheckBoxMontgomery);

        jPanelIniCanales.add(jPanel9);

        jPanelTopoConv.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));

        jCheckBoxLaplace.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxLaplace.setSelected(true);
        jCheckBoxLaplace.setText("Use Convergence Criteria. (More than X cells converge)  ");
        jCheckBoxLaplace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxLaplaceActionPerformed(evt);
            }
        });
        jPanelTopoConv.add(jCheckBoxLaplace);

        jTextFieldCeldasConv.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextFieldCeldasConv.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldCeldasConv.setText("2");
        jTextFieldCeldasConv.setMinimumSize(new java.awt.Dimension(40, 15));
        jTextFieldCeldasConv.setPreferredSize(new java.awt.Dimension(40, 20));
        jPanelTopoConv.add(jTextFieldCeldasConv);

        jPanelIniCanales.add(jPanelTopoConv);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelRedRas.add(jPanelIniCanales, gridBagConstraints);

        jPaneLAzules.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPaneLAzules.setLayout(new java.awt.GridBagLayout());

        jCheckBoxLAzules.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxLAzules.setText("Use a Blue Lines Map to Guide Network Extraction");
        jCheckBoxLAzules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxLAzulesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPaneLAzules.add(jCheckBoxLAzules, gridBagConstraints);

        jButtonBuscarLA.setText("Choose Blue Lines Map...");
        jButtonBuscarLA.setEnabled(false);
        jButtonBuscarLA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonBuscarLAActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        jPaneLAzules.add(jButtonBuscarLA, gridBagConstraints);

        jCheckBoxAP_LA.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxAP_LA.setText("Area - Slope Analyisis on Blue Lines Only");
        jCheckBoxAP_LA.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAP_LAActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPaneLAzules.add(jCheckBoxAP_LA, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelRedRas.add(jPaneLAzules, gridBagConstraints);

        jPanelOp_AreaPend.setBorder(javax.swing.BorderFactory.createTitledBorder(""));
        jPanelOp_AreaPend.setMinimumSize(new java.awt.Dimension(200, 62));
        jPanelOp_AreaPend.setLayout(new java.awt.GridLayout(2, 1));

        jCheckBoxAreaPend_c.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxAreaPend_c.setText("Load pre-existing Area-Slope Analysis");
        jCheckBoxAreaPend_c.setActionCommand("areaPendiente");
        jCheckBoxAreaPend_c.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAreaPend_cActionPerformed(evt);
            }
        });
        jPanelOp_AreaPend.add(jCheckBoxAreaPend_c);

        jPanel1.setLayout(new java.awt.GridBagLayout());

        jTextFieldAreaAP.setFont(new java.awt.Font("SansSerif", 0, 12));
        jTextFieldAreaAP.setHorizontalAlignment(javax.swing.JTextField.RIGHT);
        jTextFieldAreaAP.setMinimumSize(new java.awt.Dimension(60, 15));
        jTextFieldAreaAP.setPreferredSize(new java.awt.Dimension(70, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jTextFieldAreaAP, gridBagConstraints);

        jComboBoxAreaAP.setFont(new java.awt.Font("Dialog", 0, 10));
        jComboBoxAreaAP.setMinimumSize(new java.awt.Dimension(60, 16));
        jComboBoxAreaAP.setPreferredSize(new java.awt.Dimension(60, 20));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.EAST;
        jPanel1.add(jComboBoxAreaAP, gridBagConstraints);

        jCheckBoxAreaPend_n.setFont(new java.awt.Font("Dialog", 0, 12));
        jCheckBoxAreaPend_n.setText("Calculate a new Area - Slope Analysis with Area >");
        jCheckBoxAreaPend_n.setActionCommand("areaPendiente");
        jCheckBoxAreaPend_n.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBoxAreaPend_nActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanel1.add(jCheckBoxAreaPend_n, gridBagConstraints);

        jPanelOp_AreaPend.add(jPanel1);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        jPanelRedRas.add(jPanelOp_AreaPend, gridBagConstraints);

        jTabbedPane1.addTab("Drainage Network Pruning", null, jPanelRedRas, "");

        jPanelArea_Pend.setEnabled(false);
        jPanelArea_Pend.setLayout(new java.awt.BorderLayout());

        jLabel1.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel1.setText("# Points =");
        jPanel6.add(jLabel1);

        jTextFieldNPuntosAP.setMinimumSize(new java.awt.Dimension(30, 20));
        jTextFieldNPuntosAP.setPreferredSize(new java.awt.Dimension(50, 20));
        jTextFieldNPuntosAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextFieldNPuntosAPActionPerformed(evt);
            }
        });
        jPanel6.add(jTextFieldNPuntosAP);

        jLabel2.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel2.setText("alpha =");
        jPanel6.add(jLabel2);

        jTextFieldAlfa.setMinimumSize(new java.awt.Dimension(30, 20));
        jTextFieldAlfa.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel6.add(jTextFieldAlfa);

        jLabel3.setFont(new java.awt.Font("Arial", 0, 11));
        jLabel3.setText("C =");
        jPanel6.add(jLabel3);

        jTextFieldC.setMinimumSize(new java.awt.Dimension(30, 20));
        jTextFieldC.setPreferredSize(new java.awt.Dimension(50, 20));
        jPanel6.add(jTextFieldC);

        jButtonActualizarAP.setText("Actualizar");
        jButtonActualizarAP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonActualizarAPActionPerformed(evt);
            }
        });
        jPanel6.add(jButtonActualizarAP);

        jPanelArea_Pend.add(jPanel6, java.awt.BorderLayout.SOUTH);

        jTabbedPane1.addTab("Area-Slope Analysis", null, jPanelArea_Pend, "");

        getContentPane().add(jTabbedPane1, java.awt.BorderLayout.CENTER);

        jPanel28.setLayout(new java.awt.GridLayout(4, 1));

        jPanel8.setLayout(new java.awt.BorderLayout());

        jLabelBarra1.setFont(new java.awt.Font("Arial", 0, 11));
        jLabelBarra1.setText("Optimization Progress");
        jPanel8.add(jLabelBarra1, java.awt.BorderLayout.NORTH);

        optimizationProgressBar.setStringPainted(true);
        jPanel8.add(optimizationProgressBar, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel8);

        jPanel5.setLayout(new java.awt.BorderLayout());

        jLabelBarra.setFont(new java.awt.Font("Arial", 0, 11));
        jLabelBarra.setText("Extraction Process");
        jPanel5.add(jLabelBarra, java.awt.BorderLayout.NORTH);

        extractionProgressBar.setStringPainted(true);
        jPanel5.add(extractionProgressBar, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel5);

        jPanel11.setLayout(new java.awt.BorderLayout());

        jLabelBarra2.setFont(new java.awt.Font("Arial", 0, 11)); // NOI18N
        jLabelBarra2.setText("Calculating Geomorphology");
        jPanel11.add(jLabelBarra2, java.awt.BorderLayout.NORTH);

        geomorphProgressBar.setStringPainted(true);
        jPanel11.add(geomorphProgressBar, java.awt.BorderLayout.CENTER);

        jPanel28.add(jPanel11);

        jButtonAplicar.setText("Apply Conditions");
        jButtonAplicar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonAplicarActionPerformed(evt);
            }
        });
        jPanelBotones.add(jButtonAplicar);

        jButtonIniciar.setText("Start");
        jButtonIniciar.setEnabled(false);
        jButtonIniciar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonIniciarActionPerformed(evt);
            }
        });
        jPanelBotones.add(jButtonIniciar);

        jButtonCanc_Salir.setText("Cancel");
        jButtonCanc_Salir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCanc_SalirActionPerformed(evt);
            }
        });
        jPanelBotones.add(jButtonCanc_Salir);

        jPanel28.add(jPanelBotones);

        getContentPane().add(jPanel28, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    
  private void jTextFieldCeldasConvActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldCeldasConvActionPerformed
      // Add your handling code here:
  }//GEN-LAST:event_jTextFieldCeldasConvActionPerformed
  
  private void jButtonCanc_SalirActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCanc_SalirActionPerformed
      setVisible(false);
      dispose();
  }//GEN-LAST:event_jButtonCanc_SalirActionPerformed
  
  private void jCheckBoxLaplaceActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxLaplaceActionPerformed
      jTextFieldCeldasConv.setEnabled(jCheckBoxLaplace.isSelected());
  }//GEN-LAST:event_jCheckBoxLaplaceActionPerformed
  
  private void jCheckBoxUmbralAPActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxUmbralAPActionPerformed
      jTextFieldAlfaU.setEnabled(jCheckBoxUmbralAP.isSelected());
      jTextFieldCU.setEnabled(jCheckBoxUmbralAP.isSelected());
      if(jCheckBoxUmbralAP.isSelected()){
          jTextFieldAlfaU.setText(jTextFieldAlfa.getText());
          jTextFieldCU.setText(jTextFieldC.getText());
      }
  }//GEN-LAST:event_jCheckBoxUmbralAPActionPerformed
  
  private void jCheckBoxAP_LAActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAP_LAActionPerformed
      jButtonBuscarLA.setEnabled(jCheckBoxAP_LA.isSelected());
      //jPanelIniCanales.setEnabled(!jCheckBoxLAzules.isSelected());
      //jPanelOp_AreaPend.setEnabled(!jCheckBoxLAzules.isSelected());
      if(jCheckBoxLAzules.isSelected()){
          jCheckBox5.setSelected(false);
          jCheckBox2.setSelected(false);
          jCheckBoxAreaPend_c.setSelected(false);
          jCheckBoxAreaPend_n.setSelected(false);
      }
  }//GEN-LAST:event_jCheckBoxAP_LAActionPerformed
  
  private void jCheckBoxGEOActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxGEOActionPerformed
      //jPanelGetGeo.setEnabled(jCheckBoxGEO.isSelected());
  }//GEN-LAST:event_jCheckBoxGEOActionPerformed
  
  private void jCheckBoxAreaPend_cActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAreaPend_cActionPerformed
      //jPanelArea_Pend.setEnabled(jCheckBoxAreaPend_n.isSelected() || jCheckBoxAreaPend_c.isSelected());
  }//GEN-LAST:event_jCheckBoxAreaPend_cActionPerformed
  
  private void jCheckBoxREDActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxREDActionPerformed
      //jPanelRedRas.setEnabled(jCheckBoxRED.isSelected());
  }//GEN-LAST:event_jCheckBoxREDActionPerformed
  
  private void jCheckBoxDIRActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxDIRActionPerformed
      // Add your handling code here:
  }//GEN-LAST:event_jCheckBoxDIRActionPerformed
  
  private void jCheckBoxAreaPend_nActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxAreaPend_nActionPerformed
      //jPanelArea_Pend.setEnabled(jCheckBoxAreaPend_n.isSelected() || jCheckBoxAreaPend_c.isSelected());
      jTextFieldAreaAP.setEnabled(jCheckBoxAreaPend_n.isSelected());
      jComboBoxAreaAP.setEnabled(jCheckBoxAreaPend_n.isSelected());
  }//GEN-LAST:event_jCheckBoxAreaPend_nActionPerformed
  
  private void jTextFieldNPuntosAPActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextFieldNPuntosAPActionPerformed
      // Add your handling code here:
  }//GEN-LAST:event_jTextFieldNPuntosAPActionPerformed
  
  private void jButtonActualizarAPActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonActualizarAPActionPerformed
      double alfa, C;
      boolean cambioNP=false;
      try{
          if(Proc.npuntosAP != new Integer(jTextFieldNPuntosAP.getText()).intValue()){
              cambioNP=true;
              Proc.npuntosAP = new Integer(jTextFieldNPuntosAP.getText()).intValue();
          }
          else cambioNP = false;
      }catch(NumberFormatException e){jTextFieldNPuntosAP.setText(""); jTextFieldNPuntosAP.grabFocus(); return;}
      try{
          alfa = new Double(jTextFieldAlfa.getText()).doubleValue();
      }catch(NumberFormatException e){jTextFieldAlfa.setText(""); jTextFieldAlfa.grabFocus(); return;}
      try{
          C = new Double(jTextFieldC.getText()).doubleValue();
      }catch(NumberFormatException e){jTextFieldC.setText(""); jTextFieldC.grabFocus(); return;}
      if(C<=0){
          javax.swing.JOptionPane.showMessageDialog(Proc.parent,"The Treshold C must be greater than Zero","Error",javax.swing.JOptionPane.ERROR_MESSAGE);
          jTextFieldC.grabFocus(); return;
      }
      if(alfa<=0){
          javax.swing.JOptionPane.showMessageDialog(Proc.parent,"The exponent alpha must be greater than Zero","Error",javax.swing.JOptionPane.ERROR_MESSAGE);
          jTextFieldAlfa.grabFocus(); return;
      }
      if(cambioNP)
          graficaAP(hydroScalingAPI.modules.networkExtraction.objects.GetRasterNetwork.calculaPromAP(Proc),false);
      else graficaAP(promsAP,false);
      
  }//GEN-LAST:event_jButtonActualizarAPActionPerformed
  
  private void jButtonIniciarActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonIniciarActionPerformed
      jButtonCanc_Salir.setText("Exit");
      jButtonAplicar.setEnabled(false);
      jButtonIniciar.setEnabled(false);
      
      hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionOptimizer localOptimizer=new hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionOptimizer(this);
      Thread t1 = new Thread(localOptimizer);
      //Thread t1 = new Thread(Proc);
      t1.start();
      
  }//GEN-LAST:event_jButtonIniciarActionPerformed
  
  private void jCheckBoxLAzulesActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBoxLAzulesActionPerformed
      jButtonBuscarLA.setEnabled(jCheckBoxLAzules.isSelected());
      //jPanelIniCanales.setEnabled(!jCheckBoxLAzules.isSelected());
      //jPanelOp_AreaPend.setEnabled(!jCheckBoxLAzules.isSelected());
      if(jCheckBoxLAzules.isSelected()){
          jCheckBox5.setSelected(false);
          jCheckBox2.setSelected(false);
          jCheckBoxAreaPend_c.setSelected(false);
          jCheckBoxAreaPend_n.setSelected(false);
      }
  }//GEN-LAST:event_jCheckBoxLAzulesActionPerformed
  
  private void jButtonBuscarLAActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonBuscarLAActionPerformed
      javax.swing.JFileChooser fc=new javax.swing.JFileChooser("/");
      fc.setFileSelectionMode(fc.FILES_ONLY);
      fc.setDialogTitle("Select a Blue Lines Map");
      javax.swing.filechooser.FileFilter rasFilter = new visad.util.ExtensionFileFilter("redRas","Red de drenaje Raster");
      javax.swing.filechooser.FileFilter mdtFilter = new visad.util.ExtensionFileFilter("mdt","Modelo Digital de Terreno");
      fc.addChoosableFileFilter(rasFilter);
      fc.addChoosableFileFilter(mdtFilter);
      fc.showOpenDialog(this);
      
      //En caso de que el usuario haya puesto cancelar
      
      if (fc.getSelectedFile() == null) return;
      if (!fc.getSelectedFile().isFile()) return;
      
      boolean isMdt = fc.getSelectedFile().getName().lastIndexOf(".dem") != -1;
      boolean isRed = fc.getSelectedFile().getName().lastIndexOf(".redRas") != -1;
      
      if((isMdt && fc.getSelectedFile().length() != Proc.metaDEM.getNumRows()*Proc.metaDEM.getNumCols()*4) ||
      (isRed && fc.getSelectedFile().length() != Proc.metaDEM.getNumRows()*Proc.metaDEM.getNumCols())){
          Object [] tmp = new Object[]{"Accept"};
          javax.swing.JOptionPane.showOptionDialog((java.awt.Component)this, "The File size is different to the current DEM size. Make sure the input array's format is Integers", "Atention",javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE, null,tmp,tmp[0]);
          return;
      }
      
      Proc.fileLAzules = fc.getSelectedFile();
  }//GEN-LAST:event_jButtonBuscarLAActionPerformed
  
  private void jCheckBox2ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
      jTextField1.setEnabled(jCheckBox2.isSelected());
      jComboBox2.setEnabled(jCheckBox2.isSelected());
  }//GEN-LAST:event_jCheckBox2ActionPerformed
  
  private void jCheckBox5ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox5ActionPerformed
      jTextField2.setEnabled(jCheckBox5.isSelected());
  }//GEN-LAST:event_jCheckBox5ActionPerformed
  
  private void jButton3ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
      setVisible(false);
      dispose();
  }//GEN-LAST:event_jButton3ActionPerformed
  
  private void jButton4ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
      if(cheqManual()){
          int nPits=0;
          java.util.Vector Sums = (java.util.Vector) Proc.MT.findPits2(-2.0).get(1);
          for(int n=0; n<Sums.size(); n++){
              hydroScalingAPI.modules.networkExtraction.objects.Pit Sn = (hydroScalingAPI.modules.networkExtraction.objects.Pit)Sums.get(n);
              int cantPit = Sn.getCantPit(Proc.DIR);
              if(jCheckBox4.isSelected() && Proc.pixManual <= cantPit)
                  nPits++;
              if(jCheckBox6.isSelected() && Proc.pixCManual <= cantPit + Sn.getCantCPit(Proc.DIR))
                  nPits++;
              if(jCheckBox7.isSelected() && Sn.Mp.is_on(Proc.filaManual,Proc.colManual))
                  nPits++;
          }
          Object [] tmp = new Object[]{"Accept"};
          javax.swing.JOptionPane.showOptionDialog((java.awt.Component)this, "Human Intervention will be Required on "+nPits+" sinks", "Atention",javax.swing.JOptionPane.INFORMATION_MESSAGE, javax.swing.JOptionPane.OK_OPTION, null,tmp,tmp[0]);
          
      }
  }//GEN-LAST:event_jButton4ActionPerformed
  
  private void jButtonAplicarActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonAplicarActionPerformed
      boolean ok=true;
      if(!jCheckBox1.isSelected()) Proc.cCorte = Float.MAX_VALUE;  //nunca corte nll > MAX_VAlue nc ---> nunca
      else try{
          Proc.cCorte = Float.parseFloat(jTextField6.getText());
      }catch(NumberFormatException e){ jTextField6.setText(" "); jTextField6.grabFocus() ; ok=false;}
      
      if(!jCheckBox3.isSelected()) Proc.cAltura = Float.MAX_VALUE;
      else try{
          Proc.cAltura = Float.parseFloat(jTextField7.getText());
      }catch(NumberFormatException e){ jTextField7.setText(" "); jTextField7.grabFocus() ; ok=false;}
      if(!jCheckBox5.isSelected()) Proc.ordenMax = Integer.MAX_VALUE;
      else try{
          Proc.ordenMax = Integer.parseInt(jTextField2.getText());
      }catch(NumberFormatException e){ jTextField2.setText(" "); jTextField2.grabFocus() ; ok=false;}
      if(jCheckBoxLaplace.isSelected()){
          try{
              Proc.nCeldasConv = Integer.parseInt(jTextFieldCeldasConv.getText());
          }catch(NumberFormatException e){ jTextFieldCeldasConv.setText(" "); jTextFieldCeldasConv.grabFocus() ; ok=false;}
      }
      if(!jCheckBox2.isSelected()) Proc.pixPodado = 0f;
      else try{
          Proc.unPixPod = jComboBox2.getSelectedIndex();
          Proc.pixPodado = Float.parseFloat(jTextField1.getText());
          if(Proc.unPixPod==1)
              Proc.pixPodado=Proc.pixPodado/(float)(dx*dy);
      }catch(NumberFormatException e){ jTextField1.setText(" "); jTextField1.grabFocus() ; ok=false;}
      if(!jCheckBoxAreaPend_n.isSelected()) Proc.pixAP = 0f;
      else try{
          Proc.unPixAP = jComboBoxAreaAP.getSelectedIndex();
          Proc.pixAP = Float.parseFloat(jTextFieldAreaAP.getText());
          if(Proc.unPixAP==1)
              Proc.pixAP=Proc.pixAP/(float)(dx*dy);
      }catch(NumberFormatException e){ jTextField1.setText(" "); jTextField1.grabFocus() ; ok=false;}
      Proc.todoRed = jCheckBoxTodoRed.isSelected();
      Proc.cleanShorts = jCheckBoxCleanShorts.isSelected();
      Proc.montgomery = jCheckBoxMontgomery.isSelected();
      Proc.archPro=jRadioButton14.isSelected();
      Proc.lAzules = jCheckBoxLAzules.isSelected();
      Proc.areaPend_LA = jCheckBoxAP_LA.isSelected();
      Proc.areaPend_nuevo = jCheckBoxAreaPend_n.isSelected();
      if(jCheckBoxAreaPend_c.isSelected()){
          String ruta=(Proc.metaDEM.getLocationBinaryFile()).getPath();
          java.io.File FileAP = new java.io.File(ruta.substring(0, ruta.lastIndexOf(".")) + ".ap");
          if(!FileAP.exists()){
              javax.swing.JOptionPane.showMessageDialog(Proc.parent,"The *.ap file can't be found","Error",javax.swing.JOptionPane.ERROR_MESSAGE);
              ok=false;
          }
          else Proc.areaPend_cargar = jCheckBoxAreaPend_c.isSelected();
      }
      
      Proc.umbralAP =jCheckBoxUmbralAP.isSelected();
      if(Proc.umbralAP){
          try{
              Proc.alfa = Float.parseFloat(jTextFieldAlfaU.getText());
          }catch(NumberFormatException e){ jTextFieldAlfaU.setText(" "); jTextFieldAlfaU.grabFocus() ; ok=false;}
          try{
              Proc.C = Float.parseFloat(jTextFieldCU.getText());
          }catch(NumberFormatException e){ jTextFieldCU.setText(" "); jTextFieldCU.grabFocus() ; ok=false;}
      }
      
      
      Proc.laplace = jCheckBoxLaplace.isSelected();
      Proc.taskDIR = jCheckBoxDIR.isSelected();
      Proc.taskRED = jCheckBoxRED.isSelected();
      Proc.taskGEO = jCheckBoxGEO.isSelected();
      Proc.taskVECT = jCheckBoxVECT.isSelected();
      
      if(!Proc.taskDIR && Proc.lAzules){
          javax.swing.JOptionPane.showMessageDialog(Proc.parent,"If you want to use blue Lines the Directions Task must be inactive","Error",javax.swing.JOptionPane.ERROR_MESSAGE);
          ok=false;
      }
      if(Proc.taskGEO && (Proc.areaPend_cargar || Proc.areaPend_nuevo)){
          javax.swing.JOptionPane.showMessageDialog(Proc.parent,"If you want a new Area-Slope Analysis the Calculate Geomorphic Properties Task must be inactive","Error",javax.swing.JOptionPane.ERROR_MESSAGE);
          ok=false;
      }
    /*if(Proc.areaPend_nuevo && Proc.pixPodado==0){
        javax.swing.JOptionPane.showMessageDialog(Proc.parent,"Debe indicar un umbral inicial de area mayor o igual que cero para el an�lisis de area-pendiente","Error",javax.swing.JOptionPane.ERROR_MESSAGE);
        jTextField1.grabFocus();
        ok=false;
    }*/
      
      
      listo = ok & cheqManual();
      if(listo){
          jButtonIniciar.setEnabled(true);
          //setVisible(false);
          //dispose();
      }
  }//GEN-LAST:event_jButtonAplicarActionPerformed
  
  private boolean cheqManual(){
      boolean todoBien=true;
      
      if(!jCheckBox4.isSelected()) Proc.pixManual=Float.POSITIVE_INFINITY;
      else try{
          Proc.unPixM = jComboBox1.getSelectedIndex();
          Proc.pixManual = (new Float(jTextField10.getText())).floatValue();
          if(Proc.unPixM==1)
              Proc.pixManual=Proc.pixManual/(float)(dx*dy);
      }catch(NumberFormatException e){ jTextField10.setText(""); jTextField10.grabFocus() ; todoBien=false;}
      
      if(!jCheckBox6.isSelected()) Proc.pixCManual=Float.POSITIVE_INFINITY;
      else try{
          Proc.pixCManual = (new Float(jTextField12.getText())).floatValue();
          Proc.unPixCM = jComboBox3.getSelectedIndex();
          if(Proc.unPixCM==1)
              Proc.pixCManual=Proc.pixCManual/(float)(dx*dy);
      }catch(NumberFormatException e){ jTextField12.setText(" "); jTextField12.grabFocus() ; todoBien=false;}
      
      if(!jCheckBox7.isSelected()){
          Proc.filaManual=-100; Proc.colManual=-100;
          Proc.latManual=-100; Proc.lonManual=-100;
      }
      else{
          Proc.unFilaM=jComboBox4.getSelectedIndex();
          Proc.unColM=jComboBox5.getSelectedIndex();
          if(Proc.unFilaM==0){
              try{Proc.filaManual = (new Integer(jTextField13.getText())).intValue();
              }catch(NumberFormatException e){ jTextField13.setText(""); jTextField13.grabFocus() ; todoBien=false; }
          }
          else{
              try{Proc.latManual = (new Float(jTextField13.getText())).floatValue();
              }catch(NumberFormatException e){ jTextField13.setText(""); jTextField13.grabFocus() ; todoBien=false; }
          }
          
          if(Proc.unColM==0){
              try{Proc.colManual = (new Integer(jTextField14.getText())).intValue();
              }catch(NumberFormatException e){ jTextField14.setText(""); jTextField14.grabFocus() ; todoBien=false; }
          }
          else{
              try{Proc.lonManual = (new Float(jTextField14.getText())).floatValue();
              }catch(NumberFormatException e){ jTextField14.setText(""); jTextField14.grabFocus() ; todoBien=false; }
          }
      }
      if(todoBien && jCheckBox7.isSelected()){
          if(Proc.unFilaM==1){
              Proc.filaManual = (int) Math.round((Proc.latManual-Proc.metaDEM.getMinLat())/(Proc.metaDEM.getResLat()/3600f));
          }
          if(Proc.unColM==1){
              Proc.colManual = (int) Math.round((Proc.lonManual-Proc.metaDEM.getMinLon())/(Proc.metaDEM.getResLon()/3600f));
          }
          if(!Proc.MT.is_on(Proc.filaManual,Proc.colManual)){
              todoBien=false;
              Object [] tmp = new Object[]{"Aceptar"};
              javax.swing.JOptionPane.showOptionDialog((java.awt.Component)this, "The Point is not in the map", "Error",javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE, null,tmp,tmp[0]);
          }
      }
      
      return todoBien;
      
  }
  
  
  private void jRadioButton16ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton16ActionPerformed
      jRadioButton14.setSelected(!jRadioButton16.isSelected());
  }//GEN-LAST:event_jRadioButton16ActionPerformed
  
  private void jRadioButton14ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton14ActionPerformed
      jRadioButton16.setSelected(!jRadioButton14.isSelected());
  }//GEN-LAST:event_jRadioButton14ActionPerformed
  
  private void jCheckBox7ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox7ActionPerformed
      jTextField13.setEnabled(jCheckBox7.isSelected());
      jComboBox4.setEnabled(jCheckBox7.isSelected());
      
      jTextField14.setEnabled(jCheckBox7.isSelected());
      jLabel19.setEnabled(jCheckBox7.isSelected());
      jComboBox5.setEnabled(jCheckBox7.isSelected());
      jButton4.setEnabled(jCheckBox4.isSelected() || jCheckBox6.isSelected() || jCheckBox7.isSelected());
  }//GEN-LAST:event_jCheckBox7ActionPerformed
  
  private void jCheckBox6ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox6ActionPerformed
      jTextField12.setEnabled(jCheckBox6.isSelected());
      jComboBox3.setEnabled(jCheckBox6.isSelected());
      jButton4.setEnabled(jCheckBox4.isSelected() || jCheckBox6.isSelected() || jCheckBox7.isSelected());
  }//GEN-LAST:event_jCheckBox6ActionPerformed
  
  private void jCheckBox4ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox4ActionPerformed
      jTextField10.setEnabled(jCheckBox4.isSelected());
      jComboBox1.setEnabled(jCheckBox4.isSelected());
      jButton4.setEnabled(jCheckBox4.isSelected() || jCheckBox6.isSelected() || jCheckBox7.isSelected());
  }//GEN-LAST:event_jCheckBox4ActionPerformed
  
  private void jCheckBox3ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox3ActionPerformed
      jTextField7.setEnabled(jCheckBox3.isSelected());
  }//GEN-LAST:event_jCheckBox3ActionPerformed
  
  private void jCheckBox1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
      jTextField6.setEnabled(jCheckBox1.isSelected());
  }//GEN-LAST:event_jCheckBox1ActionPerformed
  
  private void jTextField6ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField6ActionPerformed
      // Add your handling code here:
  }//GEN-LAST:event_jTextField6ActionPerformed
  
  /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * Tests for the form
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        //new OpProcesar (new javax.swing.JFrame (), true).show ();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JProgressBar extractionProgressBar;
    private javax.swing.JProgressBar geomorphProgressBar;
    private javax.swing.JButton jButton4;
    private javax.swing.JButton jButtonActualizarAP;
    private javax.swing.JButton jButtonAplicar;
    private javax.swing.JButton jButtonBuscarLA;
    private javax.swing.JButton jButtonCanc_Salir;
    private javax.swing.JButton jButtonIniciar;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JCheckBox jCheckBox3;
    private javax.swing.JCheckBox jCheckBox4;
    private javax.swing.JCheckBox jCheckBox5;
    private javax.swing.JCheckBox jCheckBox6;
    private javax.swing.JCheckBox jCheckBox7;
    private javax.swing.JCheckBox jCheckBoxAP_LA;
    private javax.swing.JCheckBox jCheckBoxAreaPend_c;
    private javax.swing.JCheckBox jCheckBoxAreaPend_n;
    private javax.swing.JCheckBox jCheckBoxCleanShorts;
    private javax.swing.JCheckBox jCheckBoxDIR;
    private javax.swing.JCheckBox jCheckBoxGEO;
    private javax.swing.JCheckBox jCheckBoxLAzules;
    private javax.swing.JCheckBox jCheckBoxLaplace;
    private javax.swing.JCheckBox jCheckBoxMontgomery;
    private javax.swing.JCheckBox jCheckBoxRED;
    private javax.swing.JCheckBox jCheckBoxTodoRed;
    private javax.swing.JCheckBox jCheckBoxUmbralAP;
    private javax.swing.JCheckBox jCheckBoxVECT;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JComboBox jComboBox3;
    private javax.swing.JComboBox jComboBox4;
    private javax.swing.JComboBox jComboBox5;
    private javax.swing.JComboBox jComboBoxAreaAP;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel19;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabelBarra;
    private javax.swing.JLabel jLabelBarra1;
    private javax.swing.JLabel jLabelBarra2;
    private javax.swing.JPanel jPaneLAzules;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel19;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel25;
    private javax.swing.JPanel jPanel26;
    private javax.swing.JPanel jPanel28;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanelArea_Pend;
    private javax.swing.JPanel jPanelBotones;
    private javax.swing.JPanel jPanelGetGeo;
    private javax.swing.JPanel jPanelIniCanales;
    private javax.swing.JPanel jPanelOp_AreaPend;
    private javax.swing.JPanel jPanelRedRas;
    private javax.swing.JPanel jPanelSinks;
    private javax.swing.JPanel jPanelTareas;
    private javax.swing.JPanel jPanelTerasInt;
    private javax.swing.JPanel jPanelTopoConv;
    private javax.swing.JRadioButton jRadioButton14;
    private javax.swing.JRadioButton jRadioButton16;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField10;
    private javax.swing.JTextField jTextField12;
    private javax.swing.JTextField jTextField13;
    private javax.swing.JTextField jTextField14;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField6;
    private javax.swing.JTextField jTextField7;
    private javax.swing.JTextField jTextFieldAlfa;
    private javax.swing.JTextField jTextFieldAlfaU;
    private javax.swing.JTextField jTextFieldAreaAP;
    private javax.swing.JTextField jTextFieldC;
    private javax.swing.JTextField jTextFieldCU;
    private javax.swing.JTextField jTextFieldCeldasConv;
    private javax.swing.JTextField jTextFieldNPuntosAP;
    private javax.swing.JProgressBar optimizationProgressBar;
    // End of variables declaration//GEN-END:variables
    
}
