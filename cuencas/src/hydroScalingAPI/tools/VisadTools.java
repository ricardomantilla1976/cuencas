/*
 * VisadTools.java
 *
 * Created on March 10, 2007, 10:24 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.tools;

/**
 * Functionalities specific to VisAD Objects
 * @author Ricardo Mantilla
 */
public abstract class VisadTools{
    
    /**
     * Adds the zoom-in, zoom-out functionality using the mouse wheel to a visad.Display
     * @param display The visad.Display too modify
     */
    public static void addWheelFunctionality(final visad.DisplayImpl display){
        final visad.DisplayRenderer dr=display.getDisplayRenderer();
        display.getComponent().addMouseWheelListener(new java.awt.event.MouseWheelListener() {
                public void mouseWheelMoved(java.awt.event.MouseWheelEvent e) {
                    int rot = e.getWheelRotation();
                    try{
                        visad.ProjectionControl pc = display.getProjectionControl();
                        double[] scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                                1.0, 1.0, 1.0,
                                0.0, 0.0, 0.0);
                        double[] currentMatrix = pc.getMatrix();
                        // Zoom in
                        if (rot < 0){
                            scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                                                                            1.1, 1.1, 1.1,
                                                                            0.0, 0.0, 0.0);
                        }
                        // Zoom out
                        if (rot > 0){
                            scaleMatrix = dr.getMouseBehavior().make_matrix(0.0, 0.0, 0.0,
                                                                            0.9, 0.9, 0.9,
                                                                            0.0, 0.0, 0.0);
                        }
                        scaleMatrix = dr.getMouseBehavior().multiply_matrix(scaleMatrix,currentMatrix);
                        pc.setMatrix(scaleMatrix);
                    } catch (java.rmi.RemoteException re) {
                        System.out.println("Failed adding Wheel Functionality");
                        re.printStackTrace();
                    } catch (visad.VisADException ve) {
                        System.out.println("Failed adding Wheel Functionality");
                        ve.printStackTrace();
                    }
                }
            });
    }
    
}
