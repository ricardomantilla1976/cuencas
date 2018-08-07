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
 * LocationsViewer.java
 *
 * Created on June 25, 2003, 10:05 AM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 * This interface is a basic visualizator for the Locations in the database.  It
 * includes a space to display images associated to the location.  It is designed to
 * display one or several Locations
 * @author Ricardo Mantilla
 */
public class LocationsViewer extends javax.swing.JDialog {
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    
    private java.util.Vector imagesAndInfoVectors[]={new java.util.Vector(),new java.util.Vector()};
    private java.util.Vector imageViewers=new java.util.Vector();
    
    private int currentDisplayedImage=0;
    
    
    /**
     * Creates new form LocationsViewer using {@link hydroScalingAPI.io.MetaLocation}
     * @param oneLocation The {@link hydroScalingAPI.io.MetaLocation}
     * @param parent The main GIS interface
     */
    public LocationsViewer(hydroScalingAPI.mainGUI.ParentGUI parent,hydroScalingAPI.io.MetaLocation oneLocation) {
        this(parent,oneLocation,null,null);
    }
    
    /**
     * Creates new form LocationsViewer using {@link hydroScalingAPI.io.MetaLocation}
     * @param locations A set of Objects that can be casted into {@link hydroScalingAPI.io.MetaLocation}s
     * @param parent The main GIS interface
     */
    public LocationsViewer(hydroScalingAPI.mainGUI.ParentGUI parent,java.util.List locations) {
        this(parent,null,locations,null);
    }
    
    /**
     * Creates new form LocationsViewer using {@link hydroScalingAPI.io.MetaLocation}
     * @param metaLocations A set of {@link hydroScalingAPI.io.MetaLocation}s
     * @param parent The main GIS interface
     */
    public LocationsViewer(hydroScalingAPI.mainGUI.ParentGUI parent,hydroScalingAPI.io.MetaLocation[] metaLocations) {
        this(parent,null,null,metaLocations);
    }
    
    private LocationsViewer(hydroScalingAPI.mainGUI.ParentGUI parent,hydroScalingAPI.io.MetaLocation oneLocation,java.util.List locations,hydroScalingAPI.io.MetaLocation[] metaLocations) {
        super(parent, true);
        mainFrame=parent;
        
        if (oneLocation != null){
            metaLocations=new hydroScalingAPI.io.MetaLocation[1];
            metaLocations[0]=oneLocation;
        }
        
        if (locations != null){
            metaLocations=new hydroScalingAPI.io.MetaLocation[locations.size()];
            for (int i=0;i<locations.size();i++) metaLocations[i]=(hydroScalingAPI.io.MetaLocation) locations.get(i);
        }
        
        initComponents();
        
        setBounds(0,0, 600, 550);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        locationsList.setListData(metaLocations);
        locationsList.setSelectedIndex(0);
        
        updateLocationInfo();
    }
    
    private void updateLocationInfo(){
        hydroScalingAPI.io.MetaLocation oneLocation = (hydroScalingAPI.io.MetaLocation) locationsList.getSelectedValue();
        
        infoTextArea=new javax.swing.JTextArea ();
        infoTextArea.setWrapStyleWord (true);
        infoTextArea.setLineWrap (true);
        infoTextArea.setFont(new java.awt.Font("Arial", 0, 12));
        infoTextArea.removeAll();
        infoTextArea.append(oneLocation.getInformation()+"\n");
        jScrollPane2.setViewportView (infoTextArea);
        jScrollPane2.updateUI();
        
        imageDescription.setText("Image Header");
        imageContainer.removeAll();
        
        imagesAndInfoVectors = oneLocation.getImagesList();
        
        if (imagesAndInfoVectors != null){
            currentDisplayedImage=0;
            drawImage();
        } else {
            previousImage.setEnabled(false);
            nextImage.setEnabled(false);
        }
    }
    
    private void drawImage(){
        imageContainer.removeAll();
        ij.io.Opener opener = new ij.io.Opener();
        ij.ImagePlus imp = opener.openImage((String)imagesAndInfoVectors[0].get(currentDisplayedImage));
        if (imp != null){
            String imageName=opener.getName((String)imagesAndInfoVectors[0].get(currentDisplayedImage));
            imageDescription.setText(((String)imagesAndInfoVectors[1].get(currentDisplayedImage))+" ["+imageName+"]");
            ij.gui.ImageCanvas canvas=new ij.gui.ImageCanvas(imp);
            canvas.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    imageContainerMouseClicked(evt);
                }
            });
            double factor = getIdealFactor(canvas.getWidth(),canvas.getHeight());
            int picW=(int)(factor*canvas.getWidth());
            int picH=(int)(factor*canvas.getHeight());
            imageContainer.setBounds(100,0,picW,picH);
            canvas.setMagnification(factor);
            canvas.setDrawingSize(picW,picH);
            imageContainer.add(canvas);
        }
        previousImage.setEnabled(currentDisplayedImage > 0);
        nextImage.setEnabled(imagesAndInfoVectors[0].size() > (currentDisplayedImage+1));
    }
    
    private void drawImageFullScreen(){
        
        ij.io.Opener opener = new ij.io.Opener();
        ij.ImagePlus imp = opener.openImage((String)imagesAndInfoVectors[0].get(currentDisplayedImage));
        if (imp != null){
            javax.swing.JDialog fullScreenPanel=new javax.swing.JDialog(mainFrame,true);
            fullScreenPanel.getContentPane().setLayout(new java.awt.BorderLayout());
            java.awt.Rectangle marcoParent=mainFrame.getBounds();
            fullScreenPanel.setBounds(marcoParent.x,marcoParent.x, marcoParent.width, marcoParent.height);
            String imageName=opener.getName((String)imagesAndInfoVectors[0].get(currentDisplayedImage));
            fullScreenPanel.setTitle(((String)imagesAndInfoVectors[1].get(currentDisplayedImage))+" ["+imageName+"]");
            ij.gui.ImageCanvas canvas=new ij.gui.ImageCanvas(imp);
            float factorX=marcoParent.width/(float)canvas.getWidth();
            float factorY=marcoParent.height/(float)canvas.getHeight();
            canvas.setMagnification(Math.min(factorX,factorY));
            canvas.setDrawingSize(marcoParent.width,marcoParent.height);
            fullScreenPanel.getContentPane().add(canvas, java.awt.BorderLayout.CENTER);
            fullScreenPanel.setVisible(true);
        }
        
    }
    
    private double getIdealFactor(int w, int h){
        int maxW=480; int maxH=320;
        double[] goodFactors={1,0.75,2/3.,0.5,1/3.,0.25,0.125,0.0625};
        double factor=0;
        for (int i=0;i<goodFactors.length;i++){
            factor=goodFactors[i];
            if (w*factor <= maxW && h*factor <= maxH) break;
        }
        
        return factor;
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        locationsList = new javax.swing.JList();
        jScrollPane2 = new javax.swing.JScrollPane();
        infoTextArea = new javax.swing.JTextArea();
        jPanel1 = new javax.swing.JPanel();
        imageDescription = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        previousImage = new javax.swing.JButton();
        nextImage = new javax.swing.JButton();
        imageContainer = new java.awt.Panel();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        export = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        close = new javax.swing.JMenuItem();

        setTitle("Location Viewer");
        setFont(new java.awt.Font("Default", 0, 10));
        setResizable(false);
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jPanel3.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setBorder(new javax.swing.border.TitledBorder(null, "Selected Locations", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 10)));
        jScrollPane1.setMinimumSize(new java.awt.Dimension(100, 45));
        locationsList.setFont(new java.awt.Font("Dialog", 0, 10));
        locationsList.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                locationsListMouseClicked(evt);
            }
        });

        jScrollPane1.setViewportView(locationsList);

        jPanel3.add(jScrollPane1, java.awt.BorderLayout.WEST);

        jScrollPane2.setBorder(new javax.swing.border.TitledBorder(null, "Location Information", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 10)));
        jScrollPane2.setMinimumSize(new java.awt.Dimension(50, 44));
        jScrollPane2.setPreferredSize(new java.awt.Dimension(50, 38));
        infoTextArea.setFont(new java.awt.Font("Arial", 0, 12));
        infoTextArea.setLineWrap(true);
        infoTextArea.setWrapStyleWord(true);
        jScrollPane2.setViewportView(infoTextArea);

        jPanel3.add(jScrollPane2, java.awt.BorderLayout.CENTER);

        jPanel1.setLayout(new java.awt.GridLayout(1, 2));

        imageDescription.setFont(new java.awt.Font("Dialog", 0, 10));
        imageDescription.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        imageDescription.setText("Image Description");
        imageDescription.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jPanel1.add(imageDescription);

        jPanel3.add(jPanel1, java.awt.BorderLayout.SOUTH);

        getContentPane().add(jPanel3, java.awt.BorderLayout.NORTH);

        jPanel2.setLayout(new java.awt.BorderLayout());

        previousImage.setFont(new java.awt.Font("Dialog", 1, 10));
        previousImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/back.gif")));
        previousImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                previousImageActionPerformed(evt);
            }
        });

        jPanel2.add(previousImage, java.awt.BorderLayout.WEST);

        nextImage.setFont(new java.awt.Font("Dialog", 1, 10));
        nextImage.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/subGUIs/configuration/icons/forward.gif")));
        nextImage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nextImageActionPerformed(evt);
            }
        });

        jPanel2.add(nextImage, java.awt.BorderLayout.EAST);

        imageContainer.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                imageContainerMouseClicked(evt);
            }
        });

        jPanel2.add(imageContainer, java.awt.BorderLayout.CENTER);

        getContentPane().add(jPanel2, java.awt.BorderLayout.CENTER);

        jMenu1.setText("File");
        jMenu1.setFont(new java.awt.Font("Dialog", 0, 10));
        export.setFont(new java.awt.Font("Dialog", 0, 10));
        export.setText("Export");
        jMenu1.add(export);

        jMenu1.add(jSeparator1);

        close.setFont(new java.awt.Font("Dialog", 0, 10));
        close.setText("Close");
        close.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                closeActionPerformed(evt);
            }
        });

        jMenu1.add(close);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        pack();
    }//GEN-END:initComponents

    private void locationsListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_locationsListMouseClicked
        updateLocationInfo();
    }//GEN-LAST:event_locationsListMouseClicked

    private void imageContainerMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_imageContainerMouseClicked
        if (imagesAndInfoVectors != null){
            if(evt.getClickCount() == 2) {
                drawImageFullScreen();
            }
        }
    }//GEN-LAST:event_imageContainerMouseClicked

    private void previousImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_previousImageActionPerformed
        currentDisplayedImage--;
        drawImage();
    }//GEN-LAST:event_previousImageActionPerformed

    private void nextImageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nextImageActionPerformed
        currentDisplayedImage++;
        drawImage();
    }//GEN-LAST:event_nextImageActionPerformed

    private void closeActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_closeActionPerformed
        closeDialog(null);
    }//GEN-LAST:event_closeActionPerformed
    
    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
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
        java.util.Vector categories=new java.util.Vector();
        java.util.Vector values=new java.util.Vector();
        java.util.Vector conditions=new java.util.Vector();
        
        categories.add("[state]");
        values.add("Kansas");
        conditions.add(new Integer(0));
        
        hydroScalingAPI.io.MetaLocation[] queryResult = tempFrame.getLocationsManager().findLocations(categories, values, conditions);
        new LocationsViewer(tempFrame,queryResult).setVisible(true);
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem close;
    private javax.swing.JMenuItem export;
    private java.awt.Panel imageContainer;
    private javax.swing.JLabel imageDescription;
    private javax.swing.JTextArea infoTextArea;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JList locationsList;
    private javax.swing.JButton nextImage;
    private javax.swing.JButton previousImage;
    // End of variables declaration//GEN-END:variables
    
}
