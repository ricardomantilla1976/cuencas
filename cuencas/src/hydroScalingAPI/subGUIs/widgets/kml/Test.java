/*
VisAD Tutorial
Copyright (C) 2000-2001 Ugo Taddei
*/

package hydroScalingAPI.subGUIs.widgets.kml;

// Import needed classes

import visad.*;
import visad.java2d.DisplayImplJ2D;
import visad.java2d.DirectManipulationRendererJ2D;
import java.rmi.RemoteException;
import java.awt.*;
import javax.swing.*;

/**
  VisAD Tutorial example 6_01
  Direct Manipulation of Reals
  represented by a cursor
  Run program with java P6_01
 *
 */


public class Test{

  // Declare variables

  // The quantities to be displayed in x- and y-axes

  private RealType easting, northing;


  // A Tuple of Reals (a subclass of VisAD Data)
  // which will hold cursor data.

  private RealTuple cursorCoords;


  // The DataReference from the data to display

  private DataReferenceImpl cursorDataRef;


  // The 2D display, and its the maps

  private DisplayImpl display;
  private ScalarMap lonMap, latMap;


  public Test (String[] args)
    throws RemoteException, VisADException {

    // Create the quantities

    easting = RealType.getRealType("easting", SI.meter, null);
    northing = RealType.getRealType("northing", SI.meter, null);


    // Create the Data

    // first createa some "actual" data
    // that is, an array of Reals, quite like a pair of coordinates

    Real[] reals  = { new Real( easting, 0.50),
                      new Real( northing, 0.50) };

    // ...then pack this pair inside a RealTuple

    cursorCoords  = new RealTuple(reals);


    // Create the DataReference

    cursorDataRef = new DataReferenceImpl("cursorDataRef");


    // ...and initialize it with the RealTuple

    cursorDataRef.setData( cursorCoords );


    // Create Display and its maps

    // A 2D display

    display = new DisplayImplJ2D("display1");

    // Get display's graphics mode control draw scales

    GraphicsModeControl dispGMC = (GraphicsModeControl) display.getGraphicsModeControl();

    dispGMC.setScaleEnable(true);


    // Create the ScalarMaps

    lonMap = new ScalarMap( easting, Display.XAxis );
    latMap = new ScalarMap( northing, Display.YAxis );


    // Add maps to display

    display.addMap( lonMap );
    display.addMap( latMap );


    // Also create constant maps to define cursor size, color, etc...

    ConstantMap[] cMaps = { new ConstantMap( 1.0f, Display.Red ),
                           new ConstantMap( 0.0f, Display.Green ),
                           new ConstantMap( 0.0f, Display.Blue ),
                           new ConstantMap( 3.50f, Display.PointSize )  };


    // Now Add reference to display
    // But using a direct manipulation renderer


    display.addReferences( new DirectManipulationRendererJ2D(), cursorDataRef, cMaps );


    // Create application window, put display into it

    JFrame jframe = new JFrame("VisAD Tutorial example 6_01");
    jframe.getContentPane().add(display.getComponent());

    // Set window size and make it visible

    jframe.setSize(300, 300);
    jframe.setVisible(true);


  }


  public static void main(String[] args)
    throws RemoteException, VisADException
  {
    new Test(args);
  }

} //end of Visad Tutorial Program 6_01