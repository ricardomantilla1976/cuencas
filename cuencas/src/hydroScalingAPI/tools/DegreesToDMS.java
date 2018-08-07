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


package hydroScalingAPI.tools;

/**
 * An abstract class to manipulate tranformations of value representation of
 * geographic coordinates to nicely formated string
 * @author Ricardo Mantilla
 */
public abstract class DegreesToDMS{

    /**
     * The identifier for Latitude string
     */
    public static int LATITUDE=0;
    /**
     * The identifier for Longitude string
     */
    public static int LONGITUDE=1;
    private static String prettyString="";
    
    private static void processNumber(double degrees, int type) {
        /*
            Type defines:
            0: Latitude
            1: Longitude
        */
        if (type == 0){
            String HemisphereNS="";
            if (degrees >= 0) HemisphereNS="N";
            if (degrees < 0) HemisphereNS="S";
            
            degrees=Math.abs(degrees);
            int latGra=(int) degrees;
            int latMin=(int) ((degrees-latGra)*60);
            int latSec=(int) (((degrees-latGra)*60-latMin)*60);
            int latDec=(int) ((((degrees-latGra)*60.0f-latMin)*60.0f-latSec)*100);
            if(latDec == 99){
                latDec = 0;
                latSec ++;
                if(latSec == 60){
                    latSec =0;
                    latMin ++;
                     if(latMin == 60){
                        latMin = 0;
                        degrees ++;
                    }
                }
                
            }
            String latGraS;
            if (latGra < 10) 
                latGraS="0"+latGra; 
            else 
                latGraS=new Integer(latGra).toString();
            String latMinS;
            if (latMin < 10) 
                latMinS="0"+latMin; 
            else 
                latMinS=new Integer(latMin).toString();
            String latSecS;
            if (latSec < 10) 
                latSecS="0"+latSec; 
            else 
                latSecS=new Integer(latSec).toString();
            String latDecS;
            if (latDec < 10) latDecS="0"+latDec; 
            else 
                latDecS=new Integer(latDec).toString();
            
            prettyString=latGraS+":"+latMinS+":"+latSecS+"."+latDecS+" "+HemisphereNS;
        }
        
        else if (type == 1){
            String HemisphereWE="";
            if (degrees >= 0) HemisphereWE="E";
            if (degrees < 0) HemisphereWE="W";
            
            degrees=Math.abs(degrees);
            int lonGra=(int) degrees;
            int lonMin=(int) ((degrees-lonGra)*60);
            int lonSec=(int) (((degrees-lonGra)*60-lonMin)*60);
            int lonDec=(int) ((((degrees-lonGra)*60.0f-lonMin)*60.0f-lonSec)*100);
            if(lonDec == 99){
                lonDec = 0;
                lonSec ++;
                if(lonSec == 60){
                    lonSec =0;
                    lonMin ++;
                     if(lonMin == 60){
                        lonMin = 0;
                        degrees ++;
                    }
                }
            }
            String lonGraS;
            if (lonGra < 10) lonGraS="0"+lonGra; else lonGraS=new Integer(lonGra).toString();
            String lonMinS;
            if (lonMin < 10) lonMinS="0"+lonMin; else lonMinS=new Integer(lonMin).toString();
            String lonSecS;
            if (lonSec < 10) lonSecS="0"+lonSec; else lonSecS=new Integer(lonSec).toString();
            String lonDecS;
            if (lonDec < 10) lonDecS="0"+lonDec; else lonDecS=new Integer(lonDec).toString();
            
            prettyString=lonGraS+":"+lonMinS+":"+lonSecS+"."+lonDecS+" "+HemisphereWE;
        }
        
    }
    
    /**
     * Takes in a double in degrees units and a type (0 for Latitude or 1 for Longitudes) and
     * returns a formated string
     * @param degrees The coordinate in degrees
     * @param type The orientation of the coordinate
     * @return A formated String
     */
    public static String getprettyString(double degrees, int type){
        processNumber(degrees,type);
        return prettyString;
    }
    
    /**
     * Takes in a float in degrees units and a type (0 for Latitude or 1 for Longitudes) and
     * returns a formated string
     * @param degrees The coordinate in degrees
     * @param type The orientation of the coordinate
     * @return A formated String
     */
    public static String getprettyString(float degrees, int type){
        processNumber((double)degrees,type);
        return prettyString;
    }
    
    
    /**
     * Tests for the class
     * @param args The command line arguments
     */
    public static void main (String args[]) {
        
        System.out.println(hydroScalingAPI.tools.DegreesToDMS.getprettyString(-100.056,1));
        System.out.println(hydroScalingAPI.tools.DegreesToDMS.getprettyString(-90.056,0));
        System.exit(0);
    }

}
