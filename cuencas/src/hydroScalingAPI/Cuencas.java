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


package hydroScalingAPI;

/*
 * Cuencas.java
 *
 * Created on March 3, 2003, 4:52 PM
 */

/**
 * This class is the typical entrance point to the CUENCAS GUI.  It launches a splash
 * screen and initializes some libraries. It is designed to test for the presence of Java3D.
 * It can be ommited and initilize the GUI using {@link hydroScalingAPI.mainGUI.ParentGUI}
 * @author Ricardo Mantilla
 */
public class Cuencas {
    
    /**
     * Creates an instance of the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        hydroScalingAPI.mainGUI.objects.GUI_InfoManager myInfoManager=new hydroScalingAPI.mainGUI.objects.GUI_InfoManager();
        hydroScalingAPI.mainGUI.ParentGUI mainGUI=new hydroScalingAPI.mainGUI.ParentGUI(myInfoManager);
        hydroScalingAPI.mainGUI.widgets.Splash theSP = new hydroScalingAPI.mainGUI.widgets.Splash(myInfoManager.dataBaseLogoPath);
        theSP.setVisible(true);
        theSP.animate();
        theSP.dispose();
        mainGUI.setVisible(true);
    }
    
}
