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
 * spalsh.java
 *
 * Created on March 4, 2003, 12:25 PM
 */

package hydroScalingAPI.mainGUI.widgets;

/**
 * Creates a Frame that displays the CUENCAS splash screen
 * @author Ricardo Mantilla
 */
public class Splash extends javax.swing.JFrame {
    
    visad.java3d.DisplayImplJ3D display;
    
    /**
     * Creates new form spalsh
     * @param logoDB A *.jpg file with the Logo associated to the database to be loaded
     */
    public Splash(java.io.File logoDB) {
        initComponents();
        if (logoDB != null)
            jLabel2.setIcon(new javax.swing.ImageIcon(logoDB.getPath()));
        java.awt.Dimension screenSize=java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        
        int myWidth=550;
        int myHeigth=330;
        
        setTitle("Welcome to CUENCAS");
        setBounds(screenSize.width/2-myWidth/2,screenSize.height/2-myHeigth/2,myWidth,myHeigth+25);
        
        try{
            
            //Load data
            hydroScalingAPI.io.MetaRaster testRaster=new hydroScalingAPI.io.MetaRaster (new java.io.File(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/testFiles/testdem.metaDEM").getPath()));
            testRaster.setLocationBinaryFile(new java.io.File(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/testFiles/testdem.dem").getPath()));
            
            visad.FlatField valuesFF=testRaster.getField();
            
            //create display       
            display = new visad.java3d.DisplayImplJ3D("Welcome");

            //Get display graphics mode control, draw scales
            visad.GraphicsModeControl dispGMC = (visad.GraphicsModeControl) display.getGraphicsModeControl();
            dispGMC.setTextureEnable(false);

            //Create ScalarMaps
            
            visad.ScalarMap latitudeMap=new visad.ScalarMap(visad.RealType.Latitude, visad.Display.YAxis);
            display.addMap(latitudeMap);

            visad.ScalarMap longitudeMap=new visad.ScalarMap(visad.RealType.Longitude, visad.Display.XAxis);
            display.addMap(longitudeMap);

            visad.ScalarMap altitudeMap=new visad.ScalarMap(visad.RealType.getRealType("varValue"), visad.Display.ZAxis);
            altitudeMap.setRange(400,600);
            display.addMap(altitudeMap);

            visad.ScalarMap colorScaleMap=new visad.ScalarMap(visad.RealType.getRealType("varColor"), visad.Display.RGB);
            colorScaleMap.setRange(0,255);
            display.addMap(colorScaleMap);
            
            new hydroScalingAPI.subGUIs.widgets.RasterPalettesManager(colorScaleMap).setSelectedTable("Elevations");
            
            //Create data reference
            visad.DataReferenceImpl dataRef = new visad.DataReferenceImpl("dataRef");
            dataRef.setData(valuesFF);
            display.addReference(dataRef);

            /*visad.java3d.ProjectionControlJ3D pc =     
                (visad.java3d.ProjectionControlJ3D) display.getProjectionControl();
            double[] matrix = pc.getMatrix();
            double[] mult = display.make_matrix(0.0, 0.0, 0.0, 0.7, 0.0, 0.0, 0.0);
            pc.setMatrix(display.multiply_matrix(mult, matrix));*/
                    
            jPanel2.add(display.getComponent());
            
        } catch (visad.VisADException vie){
        } catch (java.rmi.RemoteException re){
        } catch (java.io.IOException ioe){
        }
            
    }
        
    /**
     * Changes the projection properties of the 3D DEM shown in the splash screen
     */
    public void animate(){
        
        visad.java3d.ProjectionControlJ3D pc =     
            (visad.java3d.ProjectionControlJ3D) display.getProjectionControl();
        for (int i=0;i<60;i++){
            double[] matrix = pc.getMatrix();
            try{
                double[] mult = display.make_matrix(1.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0);
                pc.setMatrix(display.multiply_matrix(mult, matrix));
                Thread.sleep((long)(30));
            }catch(visad.VisADException Ex){
                System.err.println("An Error has ocurred while animating 3D Display");
                System.err.println(Ex);
            }catch(java.rmi.RemoteException REx){
                System.err.println("An Error has ocurred while animating 3D Display");
                System.err.println(REx);
            }catch(java.lang.InterruptedException IEx){
                System.err.println("An Error has ocurred while animating 3D Display");
                System.err.println(IEx);
            }
        }

        for (int i=0;i<60;i++){
            double[] matrix = pc.getMatrix();
            try{
                double[] mult = display.make_matrix(0.0, -1.0, 0.0, 1.0, 0.0, 0.0, 0.0);
                pc.setMatrix(display.multiply_matrix(mult, matrix));
                Thread.sleep((long)(20));
            }catch(visad.VisADException Ex){
                System.err.println("An Error has ocurred while animating 3D Display");
                System.err.println(Ex);
            }catch(java.rmi.RemoteException REx){
                System.err.println("An Error has ocurred while animating 3D Display");
                System.err.println(REx);
            }catch(java.lang.InterruptedException IEx){
                System.err.println("An Error has ocurred while animating 3D Display");
                System.err.println(IEx);
            }
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        jPanel1 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
        jPanel3 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();

        setBackground(new java.awt.Color(0, 0, 0));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        });

        jPanel1.setLayout(new java.awt.GridLayout(1, 1));

        jPanel1.setBackground(new java.awt.Color(0, 0, 0));
        jLabel1.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/images/logo.jpg")));
        jPanel1.add(jLabel1);

        getContentPane().add(jPanel1, java.awt.BorderLayout.CENTER);

        jPanel2.setLayout(new java.awt.GridLayout(1, 1));

        jPanel2.setBackground(new java.awt.Color(0, 0, 0));
        getContentPane().add(jPanel2, java.awt.BorderLayout.EAST);

        jPanel3.setLayout(new java.awt.GridLayout(1, 1));

        jPanel3.setBackground(new java.awt.Color(0, 0, 0));
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setIcon(new javax.swing.ImageIcon(getClass().getResource("/hydroScalingAPI/mainGUI/configuration/images/defaultDB.jpg")));
        jPanel3.add(jLabel2);

        getContentPane().add(jPanel3, java.awt.BorderLayout.SOUTH);

        pack();
    }//GEN-END:initComponents
    
    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit(0);
    }//GEN-LAST:event_exitForm
    
    /**
     * Test of the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        hydroScalingAPI.mainGUI.widgets.Splash theSP = new hydroScalingAPI.mainGUI.widgets.Splash(null);
        theSP.setVisible(true);
        theSP.animate();
    }
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    // End of variables declaration//GEN-END:variables
    
}
