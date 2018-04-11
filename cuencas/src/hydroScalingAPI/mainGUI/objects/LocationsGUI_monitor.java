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
 * GaugesGUI_monitor.java
 *
 * Created on June 18, 2003, 5:05 PM
 */

package hydroScalingAPI.mainGUI.objects;

/**
 * An independent thread that waits for the {@link
 * hydroScalingAPI.mainGUI.objects.LocationsManager} to load all the information related
 * to gauges before turning on the Locations panel in the CUENCAS GUI.
 * @author Ricardo Mantilla
 */
public class LocationsGUI_monitor extends Thread{
    private hydroScalingAPI.mainGUI.ParentGUI mainFrame;
    private hydroScalingAPI.mainGUI.objects.LocationsManager dialogManager;
    
    
    /**
     * Creates new instance of LocationsGUI_monitor
     * @param parent The {@link hydroScalingAPI.mainGUI.ParentGUI} to update
     */
    public LocationsGUI_monitor(hydroScalingAPI.mainGUI.ParentGUI parent) {
        
        mainFrame=parent;
        dialogManager=mainFrame.getLocationsManager();
        
    }
    
    /**
     * Thread actions
     */
    public void run() {
        
        while(!dialogManager.isLoaded()){
            new visad.util.Delay(500);
        }
        
        mainFrame.LocationsPanelEnabled(true);
        return;
    }
    
}
