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
 * AttractorPlot.java
 *
 * Created on August 9, 2001, 4:16 PM
 */

package hydroScalingAPI.examples.ordDiffEqns;

import hydroScalingAPI.util.ordDiffEqSolver.*;
import visad.*;
import visad.java3d.DisplayImplJ3D;
import java.rmi.RemoteException;

/**
 *
 * @author Ricardo Mantilla
 */
public class AttractorPlot extends java.lang.Object {

    /**
     * Creates new AttractorPlot
     */
    public AttractorPlot() throws RemoteException, VisADException {
        //float[][] answer=new RK4(new uposearch.Lorenz(16.0f,45.0f,4.0f), new float[] {-13,-12, 52}, 0.005f, 0.0f).run(5000);
        float[][] answer=new RK4(new hydroScalingAPI.util.ordDiffEqSolver.Rossler(0.398f,2.0f,4.0f), new float[] {0,1, 1}, 0.05f, 0.0f).run(50000);
        
        RealTupleType campo=new RealTupleType(RealType.Longitude,RealType.Latitude,RealType.Altitude);
        
        DisplayImplJ3D display = new DisplayImplJ3D("display3D");
        GraphicsModeControl dispGMC = (GraphicsModeControl) display.getGraphicsModeControl();
        dispGMC.setScaleEnable(true);

        ScalarMap lonMap = new ScalarMap( RealType.Longitude, Display.XAxis );
        ScalarMap latMap = new ScalarMap( RealType.Latitude, Display.YAxis );
        ScalarMap altMap = new ScalarMap( RealType.Altitude, Display.ZAxis );

        display.addMap( latMap );
        display.addMap( lonMap );
        display.addMap( altMap );

        Gridded3DSet elemVectorial=new Gridded3DSet(campo,answer,answer[0].length);
        
        DataReferenceImpl data_ref = new DataReferenceImpl("data_ref3D");
        data_ref.setData(elemVectorial);

        //Agrego una referencia a los datos
        display.addReference( data_ref );
        
        javax.swing.JFrame jframe = new javax.swing.JFrame("Lorenz Attractor");
        jframe.getContentPane().add(display.getComponent());


        // Set window size and make it visible

        jframe.setSize(500, 500);
        jframe.setVisible(true);
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) throws RemoteException, VisADException {
        
        new AttractorPlot();
        
    }

}
