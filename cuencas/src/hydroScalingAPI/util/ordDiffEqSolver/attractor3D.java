/*
 * attractor3D.java
 *
 * Created on August 12, 2001, 10:49 PM
 */

package hydroScalingAPI.tools.ordDiffEqSolver;

import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;

/**
 *
 * @author  Ricardo Mantilla
 * @version 
 */
public class attractor3D extends javax.swing.JFrame {

    /** Creates new form attractor3D */
    public attractor3D()  throws RemoteException, VisADException {
        initComponents ();
        pack ();
        
        float[][] answer=new hydroScalingAPI.util.ordDiffEqSolver.RK4(new hydroScalingAPI.util.ordDiffEqSolver.Lorenz(16.0f,45.0f,4.0f), new float[] {-13,-12, 52}, 0.001f, 0.0f).run(50000);
        //float[][] answer=new RK4(new uposearch.Rossler(0.398f,2.0f,4.0f), new float[] {0,1, 1}, 0.05f, 0.0f).run(10000);
        
        float[][] toPlot=new float[answer.length+1][answer[0].length];
        
        for (int i=0;i<toPlot[0].length;i++){
            toPlot[3][i]=(float) i;
            toPlot[0][i]=answer[0][i];
            toPlot[1][i]=answer[1][i];
            toPlot[2][i]=answer[2][i];
        }
        
        RealTupleType campo=new RealTupleType(RealType.Longitude,RealType.Latitude,RealType.Altitude,RealType.Generic);
        
        DisplayImplJ3D display = new DisplayImplJ3D("display3D");
        GraphicsModeControl dispGMC = (GraphicsModeControl) display.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);

        ScalarMap lonMap = new ScalarMap( RealType.Longitude, Display.XAxis );
        ScalarMap latMap = new ScalarMap( RealType.Latitude, Display.YAxis );
        ScalarMap altMap = new ScalarMap( RealType.Altitude, Display.ZAxis );
        ScalarMap colMap = new ScalarMap( RealType.Generic, Display.RGB );

        display.addMap( latMap );
        display.addMap( lonMap );
        display.addMap( altMap );
        display.addMap( colMap );

        Gridded3DSet elemVectorial=new Gridded3DSet(campo,toPlot,answer[0].length);
        
        DataReferenceImpl data_ref = new DataReferenceImpl("data_ref3D");
        data_ref.setData(elemVectorial);

        display.addReference( data_ref );
        
        getContentPane().add(display.getComponent());

        visad.util.LabeledColorWidget myLabeledColorWidget=new visad.util.LabeledColorWidget(colMap);
        getContentPane().add(myLabeledColorWidget);
        // Set window size and make it visible

        setSize(1000, 500);
        
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the FormEditor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        getContentPane().setLayout(new java.awt.GridLayout(1, 2));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                exitForm(evt);
            }
        }
        );
    }//GEN-END:initComponents

    /** Exit the Application */
    private void exitForm(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_exitForm
        System.exit (0);
    }//GEN-LAST:event_exitForm

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]){
        try{
            new hydroScalingAPI.tools.ordDiffEqSolver.attractor3D().setVisible(true);
        } catch(RemoteException rie){
            
        } catch(VisADException vie){
            
        }
        
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables

}
