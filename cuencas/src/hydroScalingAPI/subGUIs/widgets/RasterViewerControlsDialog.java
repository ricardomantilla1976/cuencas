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
 * RasterViewerControlsDialog.java
 *
 * Created on June 30, 2003, 6:12 PM
 */

package hydroScalingAPI.subGUIs.widgets;

/**
 * This interface handles VisAD widgets to manipulate elements of the map display
 * @author Ricardo Mantilla
 */
public class RasterViewerControlsDialog extends javax.swing.JDialog {
    
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    
    /**
     * Creates new form RasterViewerControlsDialog
     * @param parent The main GIS GUI
     * @param latMap The Latitude Map for the x-axis
     * @param lonMap The Longitude Map for the y-axis
     * @param heigthMap The Height Map for the z-axis
     * @param colorMap The Colors Map overlaying the surface
     */
    public RasterViewerControlsDialog(hydroScalingAPI.mainGUI.ParentGUI parent, visad.ScalarMap latMap, visad.ScalarMap lonMap, visad.ScalarMap heigthMap, visad.ScalarMap colorMap) {
        super(parent, true);
        mainFrame=parent;
        initComponents();
        try{
            if(latMap != null) latitudeAxisControl.add(new ScaleControlPanel(latMap));
            if(lonMap != null) longitudeAxisControl.add(new ScaleControlPanel(lonMap));
            if(heigthMap != null) altitudeAxisControl.add(new ScaleControlPanel(heigthMap));
            if(colorMap != null) colorsContainer.add(new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(colorMap));
            
        } catch (java.rmi.RemoteException RME){
            System.err.println(RME);
        } catch (visad.VisADException VisEX){
            System.err.println(VisEX);
        }
        setBounds(0,0, 650, 450);
        java.awt.Rectangle marcoParent=mainFrame.getBounds();
        java.awt.Rectangle thisMarco=this.getBounds();
        setBounds(marcoParent.x+marcoParent.width/2-thisMarco.width/2,marcoParent.y+marcoParent.height/2-thisMarco.height/2,thisMarco.width,thisMarco.height);
        
        
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        latitudeAxisControl = new javax.swing.JPanel();
        longitudeAxisControl = new javax.swing.JPanel();
        altitudeAxisControl = new javax.swing.JPanel();
        colorsContainer = new javax.swing.JPanel();

        getContentPane().setLayout(new java.awt.GridLayout(2, 2));

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Raster Viewer Controls");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        latitudeAxisControl.setLayout(new java.awt.GridLayout(1, 0));

        getContentPane().add(latitudeAxisControl);

        longitudeAxisControl.setLayout(new java.awt.GridLayout(1, 0));

        getContentPane().add(longitudeAxisControl);

        altitudeAxisControl.setLayout(new java.awt.GridLayout(1, 0));

        getContentPane().add(altitudeAxisControl);

        colorsContainer.setLayout(new java.awt.GridLayout(1, 0));

        colorsContainer.setBorder(new javax.swing.border.TitledBorder(null, "Color Table Editor", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 10)));
        getContentPane().add(colorsContainer);

        pack();
    }//GEN-END:initComponents
    
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
        tempFrame.setVisible(true);
        
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel altitudeAxisControl;
    private javax.swing.JPanel colorsContainer;
    private javax.swing.JPanel latitudeAxisControl;
    private javax.swing.JPanel longitudeAxisControl;
    // End of variables declaration//GEN-END:variables
    
}
