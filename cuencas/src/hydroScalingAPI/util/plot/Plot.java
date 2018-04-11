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
 * Plot.java
 *
 * Created on 23 de julio de 2001, 11:20 AM
 */

package hydroScalingAPI.util.plot;

/**
 *
 * @author Olver Hernandez
 */
public class Plot extends java.awt.Dialog {

    /**
     * Creates new form Plot
     */
    hydroScalingAPI.util.plot.XYPlot Ppanel;
    public Plot(java.awt.Frame parent,boolean modal) {
        super (parent, modal);
        initComponents ();
        pack ();
        
        double [] X = {1,2,3,1,2,3,1,2,3};
        double [] Y = {3,5,4,6,5,3,4,8,7};
        setBounds(100,50,800,600);
        int w=800;
        int h=600;
        menu1.setEnabled(true);
        /*Ppanel = new hydroScalingAPI.util.Plot.XYPlot(w,h,this,X,Y, "Entropia", "fila ( tiempo )" , "Entropia");
        panel1.add (Ppanel);
        Ppanel.graphics(Ppanel.getGraphics());*/

    }

    /** This method is called from within the init() method to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents () {//GEN-BEGIN:initComponents
        menuBar1 = new java.awt.MenuBar ();
        menu1 = new java.awt.Menu ();
        menuItem1 = new java.awt.MenuItem ();
        menuItem2 = new java.awt.MenuItem ();
        popupMenu1 = new java.awt.PopupMenu ();
        menuItem3 = new java.awt.MenuItem ();
        panel1 = new java.awt.Panel ();

          menu1.setName ("Archivo");
          menu1.setLabel ("Menu");
  
            menuItem1.setName ("1");
            menuItem1.setLabel ("Item");
    
            menu1.add (menuItem1);
            menuItem2.setName ("2");
            menuItem2.setLabel ("Item");
    
            menu1.add (menuItem2);
          menuBar1.add (menu1);
        popupMenu1.setLabel ("PopupMenu");

          menuItem3.setLabel ("Item");
  
          popupMenu1.add (menuItem3);
        setLayout (new java.awt.BorderLayout ());
        addMouseListener (new java.awt.event.MouseAdapter () {
            public void mousePressed (java.awt.event.MouseEvent evt) {
                formMousePressed (evt);
            }
            public void mouseReleased (java.awt.event.MouseEvent evt) {
                formMouseReleased (evt);
            }
            public void mouseClicked (java.awt.event.MouseEvent evt) {
                formMouseClicked (evt);
            }
            public void mouseExited (java.awt.event.MouseEvent evt) {
                formMouseExited (evt);
            }
            public void mouseEntered (java.awt.event.MouseEvent evt) {
                formMouseEntered (evt);
            }
        }
        );
        addMouseMotionListener (new java.awt.event.MouseMotionAdapter () {
            public void mouseMoved (java.awt.event.MouseEvent evt) {
                formMouseMoved (evt);
            }
            public void mouseDragged (java.awt.event.MouseEvent evt) {
                formMouseDragged (evt);
            }
        }
        );
        addWindowListener (new java.awt.event.WindowAdapter () {
            public void windowClosing (java.awt.event.WindowEvent evt) {
                closeDialog (evt);
            }
        }
        );

        panel1.setLayout (new java.awt.GridLayout (1, 1));
        panel1.setFont (new java.awt.Font ("Dialog", 0, 11));
        panel1.setBackground (new java.awt.Color (204, 204, 204));
        panel1.setForeground (java.awt.Color.black);
        panel1.addComponentListener (new java.awt.event.ComponentAdapter () {
            public void componentResized (java.awt.event.ComponentEvent evt) {
                panel1ComponentResized (evt);
            }
        }
        );


        add (panel1, java.awt.BorderLayout.CENTER);

    }//GEN-END:initComponents

  private void formMouseReleased (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased
// Add your handling code here:
  }//GEN-LAST:event_formMouseReleased

  private void formMousePressed (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
// Add your handling code here:
  }//GEN-LAST:event_formMousePressed

  private void formMouseMoved (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseMoved
// Add your handling code here:
  }//GEN-LAST:event_formMouseMoved

  private void formMouseExited (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseExited
// Add your handling code here:
  }//GEN-LAST:event_formMouseExited

  private void formMouseEntered (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseEntered
// Add your handling code here:
  }//GEN-LAST:event_formMouseEntered

  private void formMouseDragged (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
// Add your handling code here:
  }//GEN-LAST:event_formMouseDragged

  private void formMouseClicked (java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
// Add your handling code here:
  }//GEN-LAST:event_formMouseClicked

  private void panel1ComponentResized (java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_panel1ComponentResized
// Add your handling code here:
    //System.out.println("res w: "+panel1.getWidth()+" h: "+panel1.getHeight());
  }//GEN-LAST:event_panel1ComponentResized

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
        new Plot (new java.awt.Frame (), true).setVisible(true);
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.MenuBar menuBar1;
    private java.awt.Menu menu1;
    private java.awt.MenuItem menuItem1;
    private java.awt.MenuItem menuItem2;
    private java.awt.PopupMenu popupMenu1;
    private java.awt.MenuItem menuItem3;
    private java.awt.Panel panel1;
    // End of variables declaration//GEN-END:variables

}
