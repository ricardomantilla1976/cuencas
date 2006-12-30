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
 * DB_Register.java
 *
 * Created on June 16, 2003, 1:34 AM
 */

package hydroScalingAPI.util.database;

/**
 *
 * @author Ricardo Mantilla
 */
public class DB_Register {
    
    java.util.Hashtable properties=new java.util.Hashtable();
    
    public DB_Register (String[] Fields,Object[] Values) {
        
        for (int i=0;i<Fields.length;i++){
            properties.put(Fields[i],Values[i]);
        }
        
    }
    
    public Object getProperty(String propName){
        return properties.get(propName);
    }
    
    public String toString(){
        return properties.toString();
    }
    
}
