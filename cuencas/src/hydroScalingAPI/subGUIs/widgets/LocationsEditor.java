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
 * LocationsEditor.java
 *
 * Created on July 2, 2003, 4:32 PM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 * An interface to edit the information associated to a Location-type object in the
 * database
 * @author Ricardo Mantilla
 */
public class LocationsEditor extends javax.swing.JDialog {
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    private hydroScalingAPI.mainGUI.objects.LocationsManager dialogManager;
    
    private boolean wrote=false;
    
    private java.util.Vector filesDescripVector=new java.util.Vector();
    private java.util.Hashtable imageOrigin=new java.util.Hashtable();
    
    private java.io.File lastSearchedPath=new java.io.File("/");
    
    /**
     * Creates new form LocationsEditor with no information associated
     * @param parent The main GIS interface
     */
    public LocationsEditor(hydroScalingAPI.mainGUI.ParentGUI parent) {
        this(parent,null,null);
    }
    /**
     * Creates new form LocationsEditor associated to a specific Location in the database
     * @param parent The main GIS interface
     * @param Location An object that can be casted into a {@link hydroScalingAPI.io.MetaLocation}
     */
    public LocationsEditor(hydroScalingAPI.mainGUI.ParentGUI parent, Object Location){
        this(parent,(String)((hydroScalingAPI.io.MetaLocation)Location).getProperty("[site name]"),(String)((hydroScalingAPI.io.MetaLocation)Location).getProperty("[type]"));
    }
    /**
     * Creates new form LocationsEditor associated to a specific Location in the database
     * 
     * @param parent The main GIS interface
     * @param nameToEdit The name associated to the location
     * @param typeToMatch They type associate to the location
     */
    public LocationsEditor(hydroScalingAPI.mainGUI.ParentGUI parent, String nameToEdit, String typeToMatch) {
        super(parent, true);
        
        mainFrame=parent;
        dialogManager=mainFrame.getLocationsManager();
        
        initComponents();
        setUpGUI();
        
        if (nameToEdit == null){
            setTitle("Add New Location");
            wrote=true;
        } else {
            setTitle("Edit "+nameToEdit+"'s information");
            setSelectedValues(nameToEdit,typeToMatch);
        }
        
    }
    
    private void setUpGUI(){
        
        setBounds(0,0,500,300);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
    }
    
    private void setSelectedValues(String nameToEdit, String typeToMatch){
        
        hydroScalingAPI.io.MetaLocation queryResult=dialogManager.getLocation(nameToEdit,typeToMatch);
        codeField.setText((String)queryResult.getProperty("[site name]"));
        agencyCombo.setSelectedItem(queryResult.getProperty("[source]"));
        typeCombo.setSelectedItem(queryResult.getProperty("[type]"));
        countyCombo.setSelectedItem(queryResult.getProperty("[county]"));
        stateCombo.setSelectedItem(queryResult.getProperty("[state]"));
        
        Object latObj=queryResult.getProperty("[latitude (deg:min:sec)]");
        String latString="N/A";
        if (!latObj.toString().equalsIgnoreCase("N/A")) latString=hydroScalingAPI.tools.DegreesToDMS.getprettyString(((Double)latObj).doubleValue(),0);
        latitudeField.setText(latString);
        
        Object lonObj=queryResult.getProperty("[longitude (deg:min:sec)]");
        String lonString="N/A";
        if (!latObj.toString().equalsIgnoreCase("N/A")) lonString=hydroScalingAPI.tools.DegreesToDMS.getprettyString(((Double)lonObj).doubleValue(),1);
        longitudeField.setText(lonString);
        
        Object altObj=queryResult.getProperty("[altitude ASL (m)]");
        String altString="N/A";
        if (!altObj.toString().equalsIgnoreCase("N/A")) altString=((Double)altObj).toString();
        altitudeField.setText(altString);
        filesDescripVector=(java.util.Vector)queryResult.getProperty("[images]");
        if(!((String)filesDescripVector.get(0)).equalsIgnoreCase("N/A")) 
            imagesAndDescriptionsList.setListData(filesDescripVector);
        else
            filesDescripVector.removeAllElements();
        infoTextArea.setText(queryResult.getInformation());
    }
    
    /**
     * A boolean flag indicating if the location file was writen
     * @return True if the file was succesfully wirten
     */
    public boolean wroteNewLocation(){
        return wrote;
    }
    
    /**
     * A String[] with the information of the location String[0]=code and String[1]=Type
     * @return A String[]
     */
    public String[] writtenOrModifiedLocation(){
        String[] locInfo={codeField.getText(),(String)typeCombo.getSelectedItem()};
        return locInfo;
    }
    
    private boolean checkFields(){
        
        boolean check=true;
        
        check &= (!codeField.getText().equalsIgnoreCase(""));
        check &= (!((String)agencyCombo.getSelectedItem()).equalsIgnoreCase("--------"));
        check &= (!((String)typeCombo.getSelectedItem()).equalsIgnoreCase("--------"));
        check &= (!((String)stateCombo.getSelectedItem()).equalsIgnoreCase("--------"));
        
        return check;
        
    }
    
    /**
     * Sets the coordinates for a newly created location
     * @param latitude The latitude in decimal degrees
     * @param longitude The longitude in decimal degrees
     */
    public void setLatLong(float latitude, float longitude){
        latitudeField.setText(hydroScalingAPI.tools.DegreesToDMS.getprettyString(latitude,0));
        longitudeField.setText(hydroScalingAPI.tools.DegreesToDMS.getprettyString(longitude,1));
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
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
        jPanel4 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        latitudeField = new javax.swing.JTextField();
        jPanel5 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        longitudeField = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jLabel5 = new javax.swing.JLabel();
        altitudeField = new javax.swing.JTextField();
        jPanel8 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel9 = new javax.swing.JPanel();
        saveChanges = new javax.swing.JButton();
        cancel = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        imagesAndDescriptionsList = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        importImage = new javax.swing.JButton();
        editImage = new javax.swing.JButton();
        removeImage = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        infoTextArea = new javax.swing.JTextArea();

        getContentPane().setLayout(new java.awt.GridLayout(1, 2));

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        propertiesPanel.setLayout(new java.awt.GridLayout(10, 1));

        propertiesPanel.setBorder(new javax.swing.border.TitledBorder(null, "Parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10)));
        propertiesPanel.setPreferredSize(new java.awt.Dimension(230, 140));
        jPanel3.setLayout(new java.awt.GridLayout(1, 2));

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel2.setText("Name *");
        jPanel3.add(jLabel2);

        codeField.setFont(new java.awt.Font("Dialog", 0, 10));
        jPanel3.add(codeField);

        propertiesPanel.add(jPanel3);

        jPanel31.setLayout(new java.awt.GridLayout(1, 2));

        jLabel21.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel21.setText("Source Agency *");
        jPanel31.add(jLabel21);

        agencyCombo.setEditable(true);
        agencyCombo.setFont(new java.awt.Font("Dialog", 0, 10));
        jPanel31.add(agencyCombo);

        propertiesPanel.add(jPanel31);

        jPanel311.setLayout(new java.awt.GridLayout(1, 2));

        jLabel211.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel211.setText("Type *");
        jPanel311.add(jLabel211);

        typeCombo.setEditable(true);
        typeCombo.setFont(new java.awt.Font("Dialog", 0, 10));
        jPanel311.add(typeCombo);

        propertiesPanel.add(jPanel311);

        jPanel313.setLayout(new java.awt.GridLayout(1, 2));

        jLabel213.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel213.setText("County");
        jPanel313.add(jLabel213);

        countyCombo.setEditable(true);
        countyCombo.setFont(new java.awt.Font("Dialog", 0, 10));
        jPanel313.add(countyCombo);

        propertiesPanel.add(jPanel313);

        jPanel314.setLayout(new java.awt.GridLayout(1, 2));

        jLabel214.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel214.setText("State *");
        jPanel314.add(jLabel214);

        stateCombo.setEditable(true);
        stateCombo.setFont(new java.awt.Font("Dialog", 0, 10));
        jPanel314.add(stateCombo);

        propertiesPanel.add(jPanel314);

        jPanel4.setLayout(new java.awt.GridLayout(1, 2));

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel3.setText("Latitude *");
        jPanel4.add(jLabel3);

        latitudeField.setFont(new java.awt.Font("Dialog", 0, 10));
        latitudeField.setText("00:00:00.00 N");
        jPanel4.add(latitudeField);

        propertiesPanel.add(jPanel4);

        jPanel5.setLayout(new java.awt.GridLayout(1, 2));

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel4.setText("Longitude *");
        jPanel5.add(jLabel4);

        longitudeField.setFont(new java.awt.Font("Dialog", 0, 10));
        longitudeField.setText("00:00:00.00 W");
        jPanel5.add(longitudeField);

        propertiesPanel.add(jPanel5);

        jPanel6.setLayout(new java.awt.GridLayout(1, 2));

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel5.setText("Altitude");
        jPanel6.add(jLabel5);

        altitudeField.setFont(new java.awt.Font("Dialog", 0, 10));
        altitudeField.setText("0000");
        jPanel6.add(altitudeField);

        propertiesPanel.add(jPanel6);

        jPanel8.setLayout(new java.awt.GridLayout(1, 0));

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 10));
        jLabel1.setText("Note: Fields with * are required");
        jPanel8.add(jLabel1);

        propertiesPanel.add(jPanel8);

        jPanel9.setLayout(new java.awt.GridLayout(1, 0));

        saveChanges.setFont(new java.awt.Font("Dialog", 0, 10));
        saveChanges.setText("Save Changes");
        saveChanges.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveChangesActionPerformed(evt);
            }
        });

        jPanel9.add(saveChanges);

        cancel.setFont(new java.awt.Font("Dialog", 0, 10));
        cancel.setText("Cancel");
        cancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelActionPerformed(evt);
            }
        });

        jPanel9.add(cancel);

        propertiesPanel.add(jPanel9);

        getContentPane().add(propertiesPanel);

        jPanel1.setLayout(new java.awt.GridLayout(2, 0));

        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(new javax.swing.border.TitledBorder(null, "Images List", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10)));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 45));
        imagesAndDescriptionsList.setFont(new java.awt.Font("Dialog", 0, 10));
        jScrollPane1.setViewportView(imagesAndDescriptionsList);

        jPanel2.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jPanel7.setLayout(new java.awt.GridLayout(1, 2));

        importImage.setFont(new java.awt.Font("Dialog", 0, 10));
        importImage.setText("Import");
        importImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                importImageActionPerformed(evt);
            }
        });

        jPanel7.add(importImage);

        editImage.setFont(new java.awt.Font("Dialog", 0, 10));
        editImage.setText("Edit");
        editImage.setEnabled(false);
        jPanel7.add(editImage);

        removeImage.setFont(new java.awt.Font("Dialog", 0, 10));
        removeImage.setText("Remove");
        removeImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeImageActionPerformed(evt);
            }
        });

        jPanel7.add(removeImage);

        jPanel2.add(jPanel7, java.awt.BorderLayout.SOUTH);

        jPanel1.add(jPanel2);

        jScrollPane2.setBorder(new javax.swing.border.TitledBorder(null, "Location Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 10)));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(50, 44));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(50, 38));
        infoTextArea.setFont(new java.awt.Font("Arial", 0, 12));
        infoTextArea.setLineWrap(true);
        infoTextArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(infoTextArea);

        jPanel1.add(jScrollPane2);

        getContentPane().add(jPanel1);

        pack();
    }//GEN-END:initComponents

    private void removeImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeImageActionPerformed
        java.util.List toRemove=imagesAndDescriptionsList.getSelectedValuesList();
        for (int i=0;i<toRemove.size();i++) filesDescripVector.remove(toRemove.get(i));
        imagesAndDescriptionsList.setListData(filesDescripVector);
    }//GEN-LAST:event_removeImageActionPerformed
    
    private void importImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_importImageActionPerformed
        hydroScalingAPI.subGUIs.widgets.ImportLocationImage impDialog=new hydroScalingAPI.subGUIs.widgets.ImportLocationImage(mainFrame,lastSearchedPath);
        impDialog.setVisible(true);
        
        if (impDialog.fileSelected()){
            lastSearchedPath=new java.io.File(impDialog.getFilePath()).getParentFile();
            filesDescripVector.add(impDialog.getFileName()+" ; "+impDialog.getDescription());
            imageOrigin.put(impDialog.getFileName()+" ; "+impDialog.getDescription(),impDialog.getFilePath());
            imagesAndDescriptionsList.setListData(filesDescripVector);
        }
        
    }//GEN-LAST:event_importImageActionPerformed
    
    private void cancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_cancelActionPerformed
    
    private void saveChangesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveChangesActionPerformed
        
        if(!checkFields()) {
            Object[] options = { "OK"};
            if(javax.swing.JOptionPane.showOptionDialog(mainFrame, "Please fill out all required fields", "Atention", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,null, options, options[0]) == 1) return;
            return;
        }
        
        String sep=System.getProperty("file.separator");
        java.io.File fileToWrite=new java.io.File(mainFrame.getInfoManager().dataBaseSitesLocationsPath.getPath()+sep+(String)stateCombo.getSelectedItem()+sep+(String)typeCombo.getSelectedItem()+sep+codeField.getText()+".txt.gz");
        
        if(fileToWrite.exists()){
            Object[] options = { "Yes","No"};
            if(javax.swing.JOptionPane.showOptionDialog(mainFrame, "Do you want to overwrite "+fileToWrite.getName(), "Atention", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.QUESTION_MESSAGE,null, options, options[1]) == 1) return;
        }

        imagesAndDescriptionsList.setSelectionInterval(0, imagesAndDescriptionsList.getModel().getSize()-1);
        Object[] register={ typeCombo.getSelectedItem(),
                            agencyCombo.getSelectedItem(),
                            codeField.getText(),
                            countyCombo.getSelectedItem(),
                            stateCombo.getSelectedItem(),
                            latitudeField.getText(),
                            longitudeField.getText(),
                            altitudeField.getText(),
                            imagesAndDescriptionsList.getSelectedValuesList(),
                            infoTextArea.getText()};
        
        try{
            new hydroScalingAPI.io.LocationWriter(fileToWrite,register);
            Object[] thisRegister=new hydroScalingAPI.io.LocationReader(fileToWrite).getRegisterForDataBase();
            dialogManager.addData(thisRegister);
            wrote=(wrote&&true);
            
            for (int i=0;i<filesDescripVector.size();i++){
                if (imageOrigin.get(filesDescripVector.get(i)) != null){
                    if (((String)imageOrigin.get(filesDescripVector.get(i))).indexOf("http://") == -1){
                        java.io.File imgSource=new java.io.File((String)imageOrigin.get(filesDescripVector.get(i)));
                        java.io.File imgDestin=new java.io.File(fileToWrite.getParent()+"/images/"+imgSource.getName());
                        imgDestin.getParentFile().mkdirs();
                        hydroScalingAPI.tools.FileManipulation.CopyFile(imgSource, imgDestin);
                    }
                }
            }
            
        } catch(java.io.IOException ioe){
            System.err.println("Failed creating location file "+fileToWrite);
            System.err.println(ioe);
        }
        dispose();
    }//GEN-LAST:event_saveChangesActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        wrote=false;
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
        new LocationsEditor(tempFrame).setVisible(true); //create New Site
        //new LocationsEditor(tempFrame,"001","Network Check").show(); //edit Existing Site
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox agencyCombo;
    private javax.swing.JTextField altitudeField;
    private javax.swing.JButton cancel;
    private javax.swing.JTextField codeField;
    private javax.swing.JComboBox countyCombo;
    private javax.swing.JButton editImage;
    private javax.swing.JList imagesAndDescriptionsList;
    private javax.swing.JButton importImage;
    private javax.swing.JTextArea infoTextArea;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel21;
    private javax.swing.JLabel jLabel211;
    private javax.swing.JLabel jLabel213;
    private javax.swing.JLabel jLabel214;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel31;
    private javax.swing.JPanel jPanel311;
    private javax.swing.JPanel jPanel313;
    private javax.swing.JPanel jPanel314;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField latitudeField;
    private javax.swing.JTextField longitudeField;
    private javax.swing.JPanel propertiesPanel;
    private javax.swing.JButton removeImage;
    private javax.swing.JButton saveChanges;
    private javax.swing.JComboBox stateCombo;
    private javax.swing.JComboBox typeCombo;
    // End of variables declaration//GEN-END:variables
    
}
