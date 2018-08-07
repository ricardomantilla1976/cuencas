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


package hydroScalingAPI.subGUIs.widgets;

import javax.swing.ListSelectionModel;

/**
 * This dialog is designed as an advance file selector for Hydroclimatic variables
 * @author Ricardo Mantilla
 */
public class HydroOpenDialog extends javax.swing.JDialog {
    
    private int[] selectedSubMap;
    
    private hydroScalingAPI.util.fileUtilities.ChronoFile[] chronoFiles;
    private java.util.Vector fullChronFilesVector;
    private java.util.Vector movingChronFilesVector=new java.util.Vector();
    private boolean openSeveralAtOnce,gotCancel=false;
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    private hydroScalingAPI.io.MetaRaster metaData;
    private hydroScalingAPI.io.MetaRaster[] listOfMetaRasters;

    
    /**
     * Creates new form HydroOpenDialog to select a map to open
     * @param parent The main GIS interface
     * @param md The MetaRaster asociated with the DEM
     */
    public HydroOpenDialog(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md) {
        this(parent,md,"Open File");
    }
    /**
     * Creates new form HydroOpenDialog to select a map to any diferent action
     * @param actionToDo A String describing the action that will take place with the selected derived
     * map
     * @param parent The main GIS interface
     * @param md The MetaRaster asociated with the DEM
     */
    public HydroOpenDialog(hydroScalingAPI.mainGUI.ParentGUI parent, hydroScalingAPI.io.MetaRaster md, String actionToDo) {
        super(parent, true);
        initComponents();
        pack();
        
        openSingle.setText(actionToDo);
        if(!actionToDo.equalsIgnoreCase("Open File")){
            jPanel3.removeAll();
            ChronoFilesList_1.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        }

        java.io.File locFile=md.getLocationMeta();
        
        java.awt.Rectangle frameParent=parent.getBounds();
        setBounds(frameParent.x+frameParent.width/2-600/2,frameParent.y+frameParent.height/2-250/2,600,250);
        mainFrame = parent;
        metaData=md;
        
        java.io.File directorio=locFile.getParentFile();
        String baseName=locFile.getName().substring(0,locFile.getName().lastIndexOf("."));
        hydroScalingAPI.util.fileUtilities.NameDotFilter myFiltro=new hydroScalingAPI.util.fileUtilities.NameDotFilter(baseName,"vhc");
        java.io.File[] lasQueSi=directorio.listFiles(myFiltro);
        
        chronoFiles=new hydroScalingAPI.util.fileUtilities.ChronoFile[lasQueSi.length];
        
        for (int i=0;i<lasQueSi.length;i++) chronoFiles[i]=new hydroScalingAPI.util.fileUtilities.ChronoFile(lasQueSi[i],baseName);
        
        java.util.Arrays.sort(chronoFiles);
        
        fullChronFilesVector=new java.util.Vector();
        
        for (int i=0;i<chronoFiles.length;i++) fullChronFilesVector.add(chronoFiles[i].fileName.getName());
        
        ChronoFilesList_1.setListData(fullChronFilesVector);
        ChronoFilesList_1.setSelectedIndex(0);
        ChronoFilesList_2.setListData(fullChronFilesVector);
        
    }
    
    /**
     * Returns a list of CronoFiles selected by the user
     * @return An array of {@link hydroScalingAPI.util.fileUtilities.ChronoFile}s
     */
    public hydroScalingAPI.util.fileUtilities.ChronoFile[] getFilesList(){
        hydroScalingAPI.util.fileUtilities.ChronoFile[] chronoFilesSel;
        if (openSeveralAtOnce){
            chronoFilesSel=new hydroScalingAPI.util.fileUtilities.ChronoFile[movingChronFilesVector.size()];
            for (int i=0;i<chronoFilesSel.length;i++) chronoFilesSel[i]=chronoFiles[fullChronFilesVector.indexOf(movingChronFilesVector.get(i))];
        } else {
            int[] itemSel=ChronoFilesList_1.getSelectedIndices();
            chronoFilesSel=new hydroScalingAPI.util.fileUtilities.ChronoFile[itemSel.length];
            for (int i=0;i<itemSel.length;i++) chronoFilesSel[i]=chronoFiles[itemSel[i]];
        }
        return chronoFilesSel;
    }
    
    /**
     * A boolean flag indicating if if the user wants to use the Multiple Maps viewer
     * mode
     * @return The boolean for multiple maps interfaces
     */
    public boolean isMultiple(){
        return openSeveralAtOnce;
    }
    
    /**
     * The type of Multiple-map interface
     * @return An integer with the multi-map interface
     */
    public int selectedViewer(){
        return (allAtOnce.isSelected()?1:0)+(layers.isSelected()?2:0)+(animation.isSelected()?3:0);
    }
    
    /**
     * A boolean flag indicating if the user selected maps
     * @return A boolean for canceled action
     */
    public boolean mapsSelected(){
        return !gotCancel;
    }
    
    /**
     * Returns an array of MetaRaster associated to each of the maps selected by the
     * user
     * @return The list of maps selected by the user
     */
    public hydroScalingAPI.io.MetaRaster[] getSelectedMetaRasters(){
        return listOfMetaRasters;
    }
    
    /**
     * Returns a {@link java.util.Hashtable} with paths to the derived maps associated
     * to the DEM.  The keys of the Hastable correspond to the map descriptor
     * @return A {@link java.util.Hashtable}
     */
    public java.util.Hashtable getRelatedMaps(){
        java.util.Hashtable nameToFile = new java.util.Hashtable();
        
        for (int i=0;i<chronoFiles.length;i++) nameToFile.put(chronoFiles[i].getDate().getTime(),chronoFiles[i].fileName.getAbsolutePath());
        
        return nameToFile;
    }
    
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        buttonGroup1 = new javax.swing.ButtonGroup();
        jTabbedPane1 = new javax.swing.JTabbedPane();
        jPanel1 = new javax.swing.JPanel();
        jPanel4 = new javax.swing.JPanel();
        openSingle = new javax.swing.JButton();
        cancel1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        ChronoFilesList_1 = new javax.swing.JList();
        jPanel3 = new javax.swing.JPanel();
        jPanel6 = new javax.swing.JPanel();
        openMultiple = new javax.swing.JButton();
        cancel2 = new javax.swing.JButton();
        jPanel5 = new javax.swing.JPanel();
        allAtOnce = new javax.swing.JRadioButton();
        layers = new javax.swing.JRadioButton();
        animation = new javax.swing.JRadioButton();
        jPanel8 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        ChronoFilesList_2 = new javax.swing.JList();
        jPanel7 = new javax.swing.JPanel();
        addAll = new javax.swing.JButton();
        addSelected = new javax.swing.JButton();
        removeSelected = new javax.swing.JButton();
        removeAll = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        ChronoFilesList_3 = new javax.swing.JList();
        jPanel10 = new javax.swing.JPanel();
        moveUp = new javax.swing.JButton();
        moveDown = new javax.swing.JButton();

        getContentPane().setLayout(new java.awt.GridLayout(1, 1));

        setTitle("Hydroclimatic Data Loader");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel1.setLayout(new java.awt.BorderLayout());

        jPanel4.setLayout(new java.awt.GridLayout(1, 2));

        openSingle.setFont(new java.awt.Font("Dialog", 0, 12));
        openSingle.setText("Open File");
        openSingle.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openSingleActionPerformed(evt);
            }
        });

        jPanel4.add(openSingle);

        cancel1.setFont(new java.awt.Font("Dialog", 0, 12));
        cancel1.setText("Cancel");
        cancel1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel1ActionPerformed(evt);
            }
        });

        jPanel4.add(cancel1);

        jPanel1.add(jPanel4, java.awt.BorderLayout.SOUTH);

        jScrollPane1.setViewportView(ChronoFilesList_1);

        jPanel1.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Open in Individual Windows", null, jPanel1, "");

        jPanel3.setLayout(new java.awt.BorderLayout());

        jPanel6.setLayout(new java.awt.GridLayout(1, 2));

        openMultiple.setFont(new java.awt.Font("Dialog", 0, 12));
        openMultiple.setText("Open Files");
        openMultiple.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openMultipleActionPerformed(evt);
            }
        });

        jPanel6.add(openMultiple);

        cancel2.setFont(new java.awt.Font("Dialog", 0, 12));
        cancel2.setText("Cancel");
        cancel2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancel2ActionPerformed(evt);
            }
        });

        jPanel6.add(cancel2);

        jPanel3.add(jPanel6, java.awt.BorderLayout.SOUTH);

        jPanel5.setLayout(new java.awt.GridLayout(1, 3));

        buttonGroup1.add(allAtOnce);
        allAtOnce.setFont(new java.awt.Font("Dialog", 0, 12));
        allAtOnce.setSelected(true);
        allAtOnce.setText("All in One");
        jPanel5.add(allAtOnce);

        buttonGroup1.add(layers);
        layers.setFont(new java.awt.Font("Dialog", 0, 12));
        layers.setText("One per Layer");
        jPanel5.add(layers);

        buttonGroup1.add(animation);
        animation.setFont(new java.awt.Font("Dialog", 0, 12));
        animation.setText("Animation");
        jPanel5.add(animation);

        jPanel3.add(jPanel5, java.awt.BorderLayout.NORTH);

        jPanel8.setLayout(new java.awt.GridLayout(1, 2));

        jPanel2.setLayout(new java.awt.BorderLayout());

        jScrollPane2.setViewportView(ChronoFilesList_2);

        jPanel2.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel7.setLayout(new java.awt.GridLayout(4, 1));

        addAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/nextWorkspace.gif")));
        addAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addAllActionPerformed(evt);
            }
        });

        jPanel7.add(addAll);

        addSelected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/forward.gif")));
        addSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addSelectedActionPerformed(evt);
            }
        });

        jPanel7.add(addSelected);

        removeSelected.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/back.gif")));
        removeSelected.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelectedActionPerformed(evt);
            }
        });

        jPanel7.add(removeSelected);

        removeAll.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/previousWorkspace.gif")));
        removeAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllActionPerformed(evt);
            }
        });

        jPanel7.add(removeAll);

        jPanel2.add(jPanel7, java.awt.BorderLayout.EAST);

        jPanel8.add(jPanel2);

        jPanel9.setLayout(new java.awt.BorderLayout());

        ChronoFilesList_3.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        jScrollPane3.setViewportView(ChronoFilesList_3);

        jPanel9.add(jScrollPane3, java.awt.BorderLayout.CENTER);

        jPanel10.setLayout(new java.awt.GridLayout(2, 1));

        moveUp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/up.gif")));
        moveUp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveUpActionPerformed(evt);
            }
        });

        jPanel10.add(moveUp);

        moveDown.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/down.gif")));
        moveDown.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                moveDownActionPerformed(evt);
            }
        });

        jPanel10.add(moveDown);

        jPanel9.add(jPanel10, java.awt.BorderLayout.EAST);

        jPanel8.add(jPanel9);

        jPanel3.add(jPanel8, java.awt.BorderLayout.CENTER);

        jTabbedPane1.addTab("Open in Single Window", null, jPanel3, "");

        getContentPane().add(jTabbedPane1);

    }// </editor-fold>//GEN-END:initComponents
    
    private void removeAllActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllActionPerformed
        
        ChronoFilesList_3.removeAll();
        movingChronFilesVector.removeAllElements();
        ChronoFilesList_3.setListData(movingChronFilesVector);
        
    }//GEN-LAST:event_removeAllActionPerformed
    
    private void openSingleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openSingleActionPerformed

        selectedSubMap=ChronoFilesList_1.getSelectedIndices();
        listOfMetaRasters=new hydroScalingAPI.io.MetaRaster[selectedSubMap.length];
        
        for (int i=0;i<selectedSubMap.length;i++){
            try{
                listOfMetaRasters[i]=new hydroScalingAPI.io.MetaRaster(metaData.getLocationMeta());
                listOfMetaRasters[i].setName(chronoFiles[selectedSubMap[i]].getDate().getTime().toString());
                listOfMetaRasters[i].setLocationBinaryFile(chronoFiles[selectedSubMap[i]].fileName);
                listOfMetaRasters[i].setFormat(metaData.getFormat());
                listOfMetaRasters[i].setUnits(metaData.getUnits());
                listOfMetaRasters[i].setInformation(metaData.getProperty("[Information]"));
            }catch(java.io.IOException IOE){
                System.err.println("At openActionPerformed");
                System.err.println(IOE);
            }
        }
        
        openSeveralAtOnce=false;
        setVisible(false);
        dispose();
        
    }//GEN-LAST:event_openSingleActionPerformed
    
    private void openMultipleActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openMultipleActionPerformed
        
        if(movingChronFilesVector.size()==0){
            Object[] options = { "OK" };
            javax.swing.JOptionPane.showOptionDialog(mainFrame, "At least one file must be selected", "Error", javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE,null, options, options[0]);
            return;
        }
        if(movingChronFilesVector.size()==1){
            openSeveralAtOnce=false;
            ChronoFilesList_3.setSelectedIndex(0);
            ChronoFilesList_1.setSelectedValue(ChronoFilesList_3.getSelectedValue(),true);
        }
        else
            openSeveralAtOnce=true;
        setVisible(false);
        dispose();
        
    }//GEN-LAST:event_openMultipleActionPerformed
    
  private void moveDownActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveDownActionPerformed
      
      try{
          int[] selInd=ChronoFilesList_3.getSelectedIndices();
          
          if (selInd[selInd.length-1] < movingChronFilesVector.size()-1){
              Object temp=movingChronFilesVector.get(selInd[selInd.length-1]+1);
              movingChronFilesVector.removeElementAt(selInd[selInd.length-1]+1);
              movingChronFilesVector.insertElementAt(temp,selInd[0]);
              ChronoFilesList_3.setListData(movingChronFilesVector);
              ChronoFilesList_3.setSelectionInterval(selInd[0]+1,selInd[selInd.length-1]+1);
          }
      }catch(Exception ex){
      }
  }//GEN-LAST:event_moveDownActionPerformed
  
  private void moveUpActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_moveUpActionPerformed
      
      try{
          int[] selInd=ChronoFilesList_3.getSelectedIndices();
          
          if (selInd[0] > 0){
              Object temp=movingChronFilesVector.get(selInd[0]-1);
              movingChronFilesVector.removeElementAt(selInd[0]-1);
              movingChronFilesVector.insertElementAt(temp,selInd[selInd.length-1]);
              ChronoFilesList_3.setListData(movingChronFilesVector);
              ChronoFilesList_3.setSelectionInterval(selInd[0]-1,selInd[selInd.length-1]-1);
          }
      }catch(Exception ex){
          
      }
  }//GEN-LAST:event_moveUpActionPerformed
  
  private void removeSelectedActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeSelectedActionPerformed
      
      int[] indicesSelec=ChronoFilesList_3.getSelectedIndices();
      for (int i=0;i<indicesSelec.length;i++) movingChronFilesVector.removeElementAt(indicesSelec[i]-i);
      ChronoFilesList_3.setListData(movingChronFilesVector);
      
  }//GEN-LAST:event_removeSelectedActionPerformed
  
  private void addSelectedActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addSelectedActionPerformed
      
      java.util.List itemsSelec=ChronoFilesList_2.getSelectedValuesList();
      if(itemsSelec.size() > 0){
          for (int i=0;i<itemsSelec.size();i++) if (!movingChronFilesVector.contains(itemsSelec.get(i))) movingChronFilesVector.add(itemsSelec.get(i));
          ChronoFilesList_3.setListData(movingChronFilesVector);
      }
  }//GEN-LAST:event_addSelectedActionPerformed
  
  private void addAllActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addAllActionPerformed
      // Add your handling code here:
      ChronoFilesList_3.removeAll();
      movingChronFilesVector=(java.util.Vector) fullChronFilesVector.clone();
      ChronoFilesList_3.setListData(movingChronFilesVector);
  }//GEN-LAST:event_addAllActionPerformed
  
  private void cancel1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel1ActionPerformed
      // Add your handling code here:
      gotCancel=true;
      setVisible(false);
      dispose();
  }//GEN-LAST:event_cancel1ActionPerformed
  
  private void cancel2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancel2ActionPerformed
      // Add your handling code here:
      gotCancel=true;
      setVisible(false);
      dispose();
  }//GEN-LAST:event_cancel2ActionPerformed
  
  /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        gotCancel=true;
        setVisible(false);
        dispose();
    }//GEN-LAST:event_closeDialog
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        try{
            hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Demo_database/Rasters/Hydrology/precipitation_events/event_00/precipitation_interpolated_ev00.metaVHC");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster (theFile);
        
            new HydroOpenDialog(tempFrame, metaModif).setVisible(true);

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        

        System.exit(0);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList ChronoFilesList_1;
    private javax.swing.JList ChronoFilesList_2;
    private javax.swing.JList ChronoFilesList_3;
    private javax.swing.JButton addAll;
    private javax.swing.JButton addSelected;
    private javax.swing.JRadioButton allAtOnce;
    private javax.swing.JRadioButton animation;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton cancel1;
    private javax.swing.JButton cancel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel10;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTabbedPane jTabbedPane1;
    private javax.swing.JRadioButton layers;
    private javax.swing.JButton moveDown;
    private javax.swing.JButton moveUp;
    private javax.swing.JButton openMultiple;
    private javax.swing.JButton openSingle;
    private javax.swing.JButton removeAll;
    private javax.swing.JButton removeSelected;
    // End of variables declaration//GEN-END:variables
    
}
