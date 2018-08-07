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
 * LocationsOpenDialog.java
 *
 * Created on June 18, 2003, 11:13 AM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 * This dialog is designed as an advance file selector of the Locations in the database
 * @author Ricardo Mantilla
 */
public class LocationsOpenDialog extends javax.swing.JDialog {
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    private hydroScalingAPI.mainGUI.objects.LocationsManager dialogManager;
    private java.util.Vector finalSelectionVector=new java.util.Vector();
    
    private boolean cancelFlag=false;
    
    /**
     * Creates new form LocationsOpenDialog to select locations to display
     * @param parent The main GIS interface
     */
    public LocationsOpenDialog(hydroScalingAPI.mainGUI.ParentGUI parent) {
        super(parent, true);
        mainFrame=parent;
        dialogManager=mainFrame.getLocationsManager();
        
        initComponents();
        
        setUpGUI();

    }
    
    private void setUpGUI(){

        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        resetSelectionRegion();
        
        processCategoryAndRegionQuery();
        
        java.util.List selValues=mainFrame.getActiveLocations();
        for (int i=0;i<selValues.size();i++) finalSelectionVector.add(selValues.get(i));
        jListFinalSelection.setListData(finalSelectionVector);
    }
    
    private void resetSelectionRegion(){
        double coordString;
        
        coordString=dialogManager.getNorthernmostLat().doubleValue();
        coordString=(Math.ceil((coordString+0.1)*1000))/1000.;
        northM.setText(new Float(coordString).toString());
        
        coordString=dialogManager.getWesternmostLat().doubleValue();
        coordString=(Math.floor((coordString-0.1)*1000))/1000.;
        westM.setText(new Float(coordString).toString());
        
        coordString=dialogManager.getSouthernmostLat().doubleValue();
        coordString=(Math.floor((coordString-0.1)*1000))/1000.;
        southM.setText(new Float(coordString).toString());
        
        coordString=dialogManager.getEasternmostLat().doubleValue();
        coordString=(Math.ceil((coordString+0.1)*1000))/1000.;
        eastM.setText(new Float(coordString).toString());
    }
    
    private void processCodeQuery(){
        
        hydroScalingAPI.io.MetaLocation[] queryResult=dialogManager.getLocation(codeField.getText());
        if (queryResult != null) {
            jListSearchResult.setListData(queryResult);
        } else {
            Object[] options = { "OK" };
            javax.swing.JOptionPane.showOptionDialog(mainFrame, "No match found for code "+codeField.getText(), "Error", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE,null, options, options[0]);
            return;
        }
    }

    private void processCategoryAndRegionQuery(){
        
        java.util.Vector categories=new java.util.Vector();
        java.util.Vector values=new java.util.Vector();
        java.util.Vector conditions=new java.util.Vector();
        
        if (agencyCombo.getSelectedIndex() != 0) {
            categories.add("[source]");
            values.add(agencyCombo.getSelectedItem());
            conditions.add(new Integer(0));
        }
        if (typeCombo.getSelectedIndex() != 0) {
            categories.add("[type]");
            values.add(typeCombo.getSelectedItem());
            conditions.add(new Integer(0));
        }
        if (countyCombo.getSelectedIndex() != 0) {
            categories.add("[county]");
            values.add(countyCombo.getSelectedItem());
            conditions.add(new Integer(0));
        }
        if (stateCombo.getSelectedIndex() != 0) {
            categories.add("[state]");
            values.add(stateCombo.getSelectedItem());
            conditions.add(new Integer(0));
        }
        try{
            Double wM_value=new Double(westM.getText());
            Double sM_value=new Double(southM.getText());
            Double eM_value=new Double(eastM.getText());
            Double nM_value=new Double(northM.getText());
            if (dialogManager.getWesternmostLat() != wM_value) {
                categories.add("[longitude (deg:min:sec)]");
                values.add(wM_value);
                conditions.add(new Integer(1));
            }
            if (dialogManager.getSouthernmostLat() != sM_value) {
                categories.add("[latitude (deg:min:sec)]");
                values.add(sM_value);
                conditions.add(new Integer(1));
            }
            if (dialogManager.getEasternmostLat() != eM_value) {
                categories.add("[longitude (deg:min:sec)]");
                values.add(eM_value);
                conditions.add(new Integer(-1));
            }
            if (dialogManager.getNorthernmostLat() != nM_value) {
                categories.add("[latitude (deg:min:sec)]");
                values.add(nM_value);
                conditions.add(new Integer(-1));
            }
            
                                     
                                     
        }catch(NumberFormatException NFE){
        }
        
        
        jListSearchResult.removeAll();
        
        Object[] queryResult=dialogManager.findLocations(categories,values,conditions);
        if (queryResult != null) {
            jListSearchResult.setListData(queryResult);
        } else {
            jListSearchResult.setListData(new java.util.Vector());
        }
     }
    
     /**
     * Returns the list of {@link hydroScalingAPI.io.MetaLocation}s selected by the user
     * @return The selected locations
     */
    public hydroScalingAPI.io.MetaLocation[] getSelectedLocations(){
         
         java.util.List matchedValues=jListFinalSelection.getSelectedValuesList();
         hydroScalingAPI.io.MetaLocation[] foundLocations=new hydroScalingAPI.io.MetaLocation[matchedValues.size()];

         for (int i=0;i<matchedValues.size();i++) foundLocations[i]=(hydroScalingAPI.io.MetaLocation)matchedValues.get(i);

         java.util.Arrays.sort(foundLocations);
         return foundLocations;

     }
     
     /**
     * A boolean flag indicating if the user wants the label associated to the location to
     * be displayed next to the location position
     * @return True if the user wants the label associated to the location
     */
    public boolean showNames(){
        return showNamesOnDisplay.isSelected();
    }
     
     /**
     * A boolean flag indicating that the user didn't make a selection
     * @return True if a selection was made
     */
    public boolean isCanceled(){
         return cancelFlag;
     }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        jLabel1 = new javax.swing.JLabel();
        parametersMode = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        propertiesPanel = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        codeField = new javax.swing.JTextField();
        jPanel31 = new javax.swing.JPanel();
        jLabel21 = new javax.swing.JLabel();
        agencyCombo = new javax.swing.JComboBox(dialogManager.getAgencies());
        jPanel311 = new javax.swing.JPanel();
        jLabel211 = new javax.swing.JLabel();
        typeCombo = new javax.swing.JComboBox(dialogManager.getTypes());
        jPanel313 = new javax.swing.JPanel();
        jLabel213 = new javax.swing.JLabel();
        countyCombo = new javax.swing.JComboBox(dialogManager.getCounties());
        jPanel314 = new javax.swing.JPanel();
        jLabel214 = new javax.swing.JLabel();
        stateCombo = new javax.swing.JComboBox(dialogManager.getStates());
        selectionPanel = new javax.swing.JPanel();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jListSearchResult = new javax.swing.JList();
        jLabel5 = new javax.swing.JLabel();
        jPanel10 = new javax.swing.JPanel();
        jPanel12 = new javax.swing.JPanel();
        addAll = new javax.swing.JButton();
        addSelected = new javax.swing.JButton();
        removeSelected = new javax.swing.JButton();
        removeAll = new javax.swing.JButton();
        showNamesOnDisplay = new javax.swing.JCheckBox();
        jLabel4 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        jListFinalSelection = new javax.swing.JList();
        jPanel2 = new javax.swing.JPanel();
        coordinatesPanel = new javax.swing.JPanel();
        jPanel8 = new javax.swing.JPanel();
        jPanel91 = new javax.swing.JPanel();
        jLabel11 = new javax.swing.JLabel();
        northM = new javax.swing.JTextField();
        jPanel101 = new javax.swing.JPanel();
        jPanel13 = new javax.swing.JPanel();
        jLabel22 = new javax.swing.JLabel();
        westM = new javax.swing.JTextField();
        jPanel14 = new javax.swing.JPanel();
        resetRegion = new javax.swing.JButton();
        jPanel15 = new javax.swing.JPanel();
        jLabel41 = new javax.swing.JLabel();
        eastM = new javax.swing.JTextField();
        jPanel16 = new javax.swing.JPanel();
        jPanel17 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        southM = new javax.swing.JTextField();
        jPanel18 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        getSelected = new javax.swing.JButton();
        cancel = new javax.swing.JButton();

        setTitle("Locations Selector");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel1.setText("Select By");
        getContentPane().add(jLabel1, java.awt.BorderLayout.NORTH);

        parametersMode.setFont(new java.awt.Font("Dialog", 0, 10));
        parametersMode.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                parametersModeStateChanged(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        propertiesPanel.setLayout(new java.awt.GridLayout(6, 1));

        propertiesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));
        propertiesPanel.setPreferredSize(new java.awt.Dimension(230, 140));
        jPanel3.setLayout(new java.awt.GridLayout(1, 2));

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel2.setText("Name");
        jPanel3.add(jLabel2);

        codeField.setFont(new java.awt.Font("Dialog", 0, 10));
        codeField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                codeFieldActionPerformed(evt);
            }
        });

        jPanel3.add(codeField);

        propertiesPanel.add(jPanel3);

        jPanel31.setLayout(new java.awt.GridLayout(1, 2));

        jLabel21.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel21.setText("Agency");
        jPanel31.add(jLabel21);

        agencyCombo.setEditable(true);
        agencyCombo.setFont(new java.awt.Font("Dialog", 0, 10));
        agencyCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                agencyComboActionPerformed(evt);
            }
        });

        jPanel31.add(agencyCombo);

        propertiesPanel.add(jPanel31);

        jPanel311.setLayout(new java.awt.GridLayout(1, 2));

        jLabel211.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel211.setText("Type");
        jPanel311.add(jLabel211);

        typeCombo.setEditable(true);
        typeCombo.setFont(new java.awt.Font("Dialog", 0, 10));
        typeCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboActionPerformed(evt);
            }
        });

        jPanel311.add(typeCombo);

        propertiesPanel.add(jPanel311);

        jPanel313.setLayout(new java.awt.GridLayout(1, 2));

        jLabel213.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel213.setText("County");
        jPanel313.add(jLabel213);

        countyCombo.setEditable(true);
        countyCombo.setFont(new java.awt.Font("Dialog", 0, 10));
        countyCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                countyComboActionPerformed(evt);
            }
        });

        jPanel313.add(countyCombo);

        propertiesPanel.add(jPanel313);

        jPanel314.setLayout(new java.awt.GridLayout(1, 2));

        jLabel214.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel214.setText("State");
        jPanel314.add(jLabel214);

        stateCombo.setEditable(true);
        stateCombo.setFont(new java.awt.Font("Dialog", 0, 10));
        stateCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                stateComboActionPerformed(evt);
            }
        });

        jPanel314.add(stateCombo);

        propertiesPanel.add(jPanel314);

        jPanel1.add(propertiesPanel, java.awt.BorderLayout.WEST);

        selectionPanel.setLayout(new java.awt.GridLayout(1, 2));

        jPanel9.setLayout(new java.awt.BorderLayout());

        jPanel9.setPreferredSize(new java.awt.Dimension(180, 140));
        jListSearchResult.setFont(new java.awt.Font("Dialog", 0, 10));
        jScrollPane1.setViewportView(jListSearchResult);

        jPanel9.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel5.setText("Search Results");
        jPanel9.add(jLabel5, java.awt.BorderLayout.NORTH);

        selectionPanel.add(jPanel9);

        jPanel10.setLayout(new java.awt.BorderLayout());

        jPanel10.setPreferredSize(new java.awt.Dimension(210, 140));
        jPanel12.setLayout(new java.awt.GridLayout(4, 1));

        addAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/nextWorkspace.gif")));
        addAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAllActionPerformed(evt);
            }
        });

        jPanel12.add(addAll);

        addSelected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/forward.gif")));
        addSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectedActionPerformed(evt);
            }
        });

        jPanel12.add(addSelected);

        removeSelected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/back.gif")));
        removeSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedActionPerformed(evt);
            }
        });

        jPanel12.add(removeSelected);

        removeAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/previousWorkspace.gif")));
        removeAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllActionPerformed(evt);
            }
        });

        jPanel12.add(removeAll);

        jPanel10.add(jPanel12, java.awt.BorderLayout.WEST);

        showNamesOnDisplay.setFont(new java.awt.Font("Dialog", 0, 10));
        showNamesOnDisplay.setSelected(true);
        showNamesOnDisplay.setText("Show Code on Display");
        jPanel10.add(showNamesOnDisplay, java.awt.BorderLayout.SOUTH);

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel4.setText("Selected Locations");
        jPanel10.add(jLabel4, java.awt.BorderLayout.NORTH);

        jListFinalSelection.setFont(new java.awt.Font("Dialog", 0, 10));
        jScrollPane2.setViewportView(jListFinalSelection);

        jPanel10.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        selectionPanel.add(jPanel10);

        jPanel1.add(selectionPanel, java.awt.BorderLayout.CENTER);

        parametersMode.addTab("Properties", jPanel1);

        jPanel2.setLayout(new java.awt.BorderLayout());

        coordinatesPanel.setLayout(new java.awt.GridLayout(3, 3));

        coordinatesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Geographic Region"));
        coordinatesPanel.setPreferredSize(new java.awt.Dimension(230, 133));
        coordinatesPanel.add(jPanel8);

        jPanel91.setLayout(new java.awt.GridLayout(2, 1));

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel11.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel11.setText("Northernmost");
        jPanel91.add(jLabel11);

        northM.setFont(new java.awt.Font("Dialog", 0, 10));
        northM.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        northM.setText("90");
        northM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                northMActionPerformed(evt);
            }
        });

        jPanel91.add(northM);

        coordinatesPanel.add(jPanel91);

        coordinatesPanel.add(jPanel101);

        jPanel13.setLayout(new java.awt.GridLayout(2, 1));

        jLabel22.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel22.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel22.setText("Westernmost");
        jPanel13.add(jLabel22);

        westM.setFont(new java.awt.Font("Dialog", 0, 10));
        westM.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        westM.setText("-180");
        westM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                westMActionPerformed(evt);
            }
        });

        jPanel13.add(westM);

        coordinatesPanel.add(jPanel13);

        jPanel14.setLayout(new java.awt.GridLayout(1, 1));

        resetRegion.setFont(new java.awt.Font("Dialog", 0, 10));
        resetRegion.setText("Reset");
        resetRegion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetRegionActionPerformed(evt);
            }
        });

        jPanel14.add(resetRegion);

        coordinatesPanel.add(jPanel14);

        jPanel15.setLayout(new java.awt.GridLayout(2, 1));

        jLabel41.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel41.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel41.setText("Easternmost");
        jPanel15.add(jLabel41);

        eastM.setFont(new java.awt.Font("Dialog", 0, 10));
        eastM.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        eastM.setText("180");
        eastM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eastMActionPerformed(evt);
            }
        });

        jPanel15.add(eastM);

        coordinatesPanel.add(jPanel15);

        coordinatesPanel.add(jPanel16);

        jPanel17.setLayout(new java.awt.GridLayout(2, 1));

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel3.setText("Southernmost");
        jPanel17.add(jLabel3);

        southM.setFont(new java.awt.Font("Dialog", 0, 10));
        southM.setHorizontalAlignment(javax.swing.JTextField.CENTER);
        southM.setText("-90");
        southM.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                southMActionPerformed(evt);
            }
        });

        jPanel17.add(southM);

        coordinatesPanel.add(jPanel17);

        coordinatesPanel.add(jPanel18);

        jPanel2.add(coordinatesPanel, java.awt.BorderLayout.WEST);

        parametersMode.addTab("Coordinates", jPanel2);

        getContentPane().add(parametersMode, java.awt.BorderLayout.CENTER);

        jPanel4.setLayout(new java.awt.GridLayout(1, 0));

        getSelected.setFont(new java.awt.Font("Dialog", 0, 10));
        getSelected.setText("Get Selected Locations");
        getSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                getSelectedActionPerformed(evt);
            }
        });

        jPanel4.add(getSelected);

        cancel.setFont(new java.awt.Font("Dialog", 0, 10));
        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        jPanel4.add(cancel);

        getContentPane().add(jPanel4, java.awt.BorderLayout.SOUTH);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void getSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_getSelectedActionPerformed
        jListFinalSelection.setSelectionInterval(0,jListFinalSelection.getModel().getSize()-1);
        if(jListFinalSelection.getSelectedValuesList().size()==0){
            Object[] options = { "OK" };
            javax.swing.JOptionPane.showOptionDialog(mainFrame, "At least one location must be selected", "Error", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE,null, options, options[0]);
            return;
        }
        setVisible(false);
        dispose();
    }//GEN-LAST:event_getSelectedActionPerformed

    private void resetRegionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetRegionActionPerformed
        resetSelectionRegion();
    }//GEN-LAST:event_resetRegionActionPerformed

    private void southMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_southMActionPerformed
        processCategoryAndRegionQuery();
    }//GEN-LAST:event_southMActionPerformed

    private void eastMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eastMActionPerformed
        processCategoryAndRegionQuery();
    }//GEN-LAST:event_eastMActionPerformed

    private void westMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_westMActionPerformed
        processCategoryAndRegionQuery();
    }//GEN-LAST:event_westMActionPerformed

    private void northMActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_northMActionPerformed
        processCategoryAndRegionQuery();
    }//GEN-LAST:event_northMActionPerformed

    private void removeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllActionPerformed
        jListFinalSelection.removeAll();
        finalSelectionVector=new java.util.Vector();
        jListFinalSelection.setListData(finalSelectionVector);
    }//GEN-LAST:event_removeAllActionPerformed

    private void removeSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedActionPerformed
        java.util.List selValues=jListFinalSelection.getSelectedValuesList();
        for (int i=0;i<selValues.size();i++) {
            finalSelectionVector.remove(selValues.get(i));
        }
        jListFinalSelection.setListData(finalSelectionVector);
    }//GEN-LAST:event_removeSelectedActionPerformed

    private void addSelectedActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSelectedActionPerformed
        jListFinalSelection.removeAll();
        java.util.List selValues=jListSearchResult.getSelectedValuesList();
        for (int i=0;i<selValues.size();i++) finalSelectionVector.add(selValues.get(i));
        jListFinalSelection.setListData(finalSelectionVector);
    }//GEN-LAST:event_addSelectedActionPerformed

    private void addAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllActionPerformed
        jListSearchResult.setSelectionInterval(0,jListSearchResult.getModel().getSize()-1);
        java.util.List selValues=jListSearchResult.getSelectedValuesList();
        for (int i=0;i<selValues.size();i++) finalSelectionVector.add(selValues.get(i));
        jListFinalSelection.setListData(finalSelectionVector);
    }//GEN-LAST:event_addAllActionPerformed

    private void codeFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_codeFieldActionPerformed
        processCodeQuery();
    }//GEN-LAST:event_codeFieldActionPerformed

    private void stateComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_stateComboActionPerformed
        processCategoryAndRegionQuery();
    }//GEN-LAST:event_stateComboActionPerformed

    private void countyComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_countyComboActionPerformed
        processCategoryAndRegionQuery();
    }//GEN-LAST:event_countyComboActionPerformed

    private void typeComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboActionPerformed
        processCategoryAndRegionQuery();
    }//GEN-LAST:event_typeComboActionPerformed

    private void agencyComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_agencyComboActionPerformed
        processCategoryAndRegionQuery();
    }//GEN-LAST:event_agencyComboActionPerformed

    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelActionPerformed

    private void parametersModeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_parametersModeStateChanged
        if (parametersMode.getSelectedIndex()==1) {
            jPanel2.add(selectionPanel);
        }
        if (parametersMode.getSelectedIndex()==0) {
            jPanel1.add(selectionPanel);
        }
    }//GEN-LAST:event_parametersModeStateChanged
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        cancelFlag=true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
        new visad.util.Delay(1000);
        new LocationsOpenDialog(tempFrame).setVisible(true);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addAll;
    private javax.swing.JButton addSelected;
    private javax.swing.JComboBox agencyCombo;
    private javax.swing.JButton cancel;
    private javax.swing.JTextField codeField;
    private javax.swing.JPanel coordinatesPanel;
    private javax.swing.JComboBox countyCombo;
    private javax.swing.JTextField eastM;
    private javax.swing.JButton getSelected;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel211;
    private javax.swing.JLabel jLabel213;
    private javax.swing.JLabel jLabel214;
    private javax.swing.JLabel jLabel22;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel41;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JList jListFinalSelection;
    private javax.swing.JList jListSearchResult;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel101;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel13;
    private javax.swing.JPanel jPanel14;
    private javax.swing.JPanel jPanel15;
    private javax.swing.JPanel jPanel16;
    private javax.swing.JPanel jPanel17;
    private javax.swing.JPanel jPanel18;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel311;
    private javax.swing.JPanel jPanel313;
    private javax.swing.JPanel jPanel314;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JPanel jPanel91;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField northM;
    private javax.swing.JTabbedPane parametersMode;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JButton removeAll;
    private javax.swing.JButton removeSelected;
    private javax.swing.JButton resetRegion;
    private javax.swing.JPanel selectionPanel;
    private javax.swing.JCheckBox showNamesOnDisplay;
    private javax.swing.JTextField southM;
    private javax.swing.JComboBox stateCombo;
    private javax.swing.JComboBox typeCombo;
    private javax.swing.JTextField westM;
    // End of variables declaration//GEN-END:variables
    
}
