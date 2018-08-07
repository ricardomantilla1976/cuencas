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
 * Plot1.java
 *
 * Created on 23 de julio de 2001, 03:48 PM
 */

package hydroScalingAPI.util.plot;

/**
 *
 * @author Olver Hernandez
 */

public class Plot1 extends javax.swing.JDialog {

    /**
     * Creates new form Plot1
     */
    hydroScalingAPI.util.plot.XYJPanel Ppanel;
    public Plot1(java.awt.Frame parent,boolean modal) {
        super (parent, modal);
        initComponents ();
        pack ();
        double [] X = {1,2,3,1,2,3,1,2,3,3,4,5};
        double [] Y = {3,5,4,6,5,3,4,8,7,9,4,5};
        double [] Z = {20,4,8,7,3,5,4,6,5,-1,4,5};
        setBounds(100,50,800,600);
        int w=800;
        int h=600;
        Ppanel = new hydroScalingAPI.util.plot.XYJPanel( "Entropia", "fila ( tiempo )" , "Entropia");
        getContentPane ().add (Ppanel, java.awt.BorderLayout.CENTER);
        Ppanel.addDatos(X,X,-9999,java.awt.Color.red,5);
        Ppanel.addDatos(X,Y,-9999,java.awt.Color.blue,5);
        
        Ppanel.setXRange(0,10);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        jMenuItem1 = new javax.swing.JMenuItem();
        jMenuItem2 = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        jMenuItem3 = new javax.swing.JMenuItem();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        jMenu1.setText("Menu");
        jMenu1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenu1ActionPerformed(evt);
            }
        });

        jMenuItem1.setText("Item");
        jMenu1.add(jMenuItem1);

        jMenuItem2.setText("Item");
        jMenu1.add(jMenuItem2);

        jMenu1.add(jSeparator1);

        jMenuItem3.setText("Item");
        jMenu1.add(jMenuItem3);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

    }//GEN-END:initComponents

  private void jMenu1ActionPerformed (java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenu1ActionPerformed
    // Add your handling code here:
    jMenu1.setVisible(true);
    jMenuItem1.setVisible(true);
    jMenuItem2.setVisible(true);
    jMenuItem3.setVisible(true);
    jMenuItem3.grabFocus();
  }//GEN-LAST:event_jMenu1ActionPerformed

/** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_closeDialog
        setVisible (false);
        dispose ();
        System.exit(0);
    }//GEN-LAST:event_closeDialog

    /**
     * @param args the command line arguments
     */
    public static void main (String args[]) {
        new Plot1 (new javax.swing.JFrame (), true).setVisible(true);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JMenuItem jMenuItem3;
    private javax.swing.JSeparator jSeparator1;
    // End of variables declaration//GEN-END:variables

}
