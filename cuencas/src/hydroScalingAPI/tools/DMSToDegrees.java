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
 * An abstract class to manipulate tranformations of string representation of
 * geographic coordinates to values in degrees
 * @author Ricardo Mantilla
 */
public abstract class DMSToDegrees{
    
    private static double Degrees=0;

    /** Creates new GMSToDegrees */
    private static void processString(String GMS){
            java.util.StringTokenizer Chain=new java.util.StringTokenizer(GMS);

            String Horas = Chain.nextToken(":");
            String Minutos=Chain.nextToken(":");
            String Segundos=Chain.nextToken(" ").trim();
            int Hora=new Integer(Horas).intValue();
            int Min=new Integer(Minutos).intValue();
            float Seg=new Float(Segundos.substring(1)).floatValue();

            String Direccion=Chain.nextToken().trim();
            Degrees = Hora;
            Degrees += Min/60d;
            Degrees += Seg/3600d;
            
            Degrees *= -1f;
            
            if(Direccion.equalsIgnoreCase("n") || Direccion.equalsIgnoreCase("e"))
                Degrees *= -1f;
 
    }
    
    /**
     * A method for tranforming a string representation into a double
     * @param GMS The string to be processed
     * @return The double value
     */
    public static double getDegrees(String GMS){
        processString(GMS);
        return Degrees;
    }
    
    /**
     * A method for tranforming a string representation into a Double object
     * @param GMS The string to be processed
     * @return The Double object
     */
    public static Double getDoubleDegrees(String GMS){
        processString(GMS);
        return new Double(Degrees);
    }
    
    /**
     * Tests for the class
     * @param args The command line arguments
     */
    public static void main (String args[]) {
        System.out.println(hydroScalingAPI.tools.DMSToDegrees.getDegrees("40:40:20.05 N"));
        System.out.println(hydroScalingAPI.tools.DMSToDegrees.getDegrees("60:30:20.05 W"));
        System.exit(0);
    }

}
