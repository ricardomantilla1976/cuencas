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
 * StateName.java
 *
 * Created on June 13, 2003, 11:17 PM
 */

package hydroScalingAPI.tools;

/**
 * An abstract class to handle United States convention for states
 * @author Ricardo Mantilla
 */
public abstract class StateName {
    private static String[] code={"AK","AL","AR","AS","AZ","CA","CO","CT","DC","DE","FL","GA","GU","HI","IA","ID","IL","IN","KS","KY","LA","MA","MD","ME","MI","MN","MO","MS","MT","NC","ND","NE","NH","NJ","NM","NV","NY","OH","OK","OR","PA","PR","RI","SC","SD","TN","TX","UT","VA","VI","VT","WA","WI","WV","WY"};
    private static String[] name={"Alaska","Alabama","Arkansas","American Samoa","Arizona","California","Colorado","Connecticut","District Of Columbia","Delaware","Florida","Georgia","Guam","Hawaii","Iowa","Idaho","Illinois","Indiana","Kansas","Kentucky","Louisiana","Massachusetts","Maryland","Maine","Michigan","Minnesota","Missouri","Mississippi","Montana","North Carolina","North Dakota","Nebraska","New Hampshire","New Jersey","New Mexico","Nevada","New York","Ohio","Oklahoma","Oregon","Pennsylvania","Puerto Rico","Rhode Island","South Carolina","South Dakota","Tennessee","Texas","Utah","Virginia","Virgin Islands","Vermont","Washington","Wisconsin","West Virginia","Wyoming"};

    /**
     * Creates a new instance of CodeOrNameToStandardName
     * @param state 
     * @return The state's name
     */
    public static String CodeOrNameToStandardName(String state) {
        for (int i=0;i<code.length;i++){
            if(code[i].equalsIgnoreCase(state)) return name[i];
            if(name[i].equalsIgnoreCase(state)) return name[i];
        }
        return state;
    }
    
    /**
     * Tranforms a state name to the two letter code
     * @param state The state name
     * @return The two letters code
     */
    public static String NameToCode(String state) {
        for (int i=0;i<code.length;i++){
            if(name[i].equalsIgnoreCase(state)) return code[i];
        }
        return state;
    }
    
    /**
     * Tranforms a  two letter code to the state name
     * @param state The two letters code
     * @return The state name
     */
    public static String NameToName(String state) {
        for (int i=0;i<code.length;i++){
            if(name[i].equalsIgnoreCase(state)) return name[i];
        }
        return state;
    }
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.out.println(StateName.CodeOrNameToStandardName("co"));
    }
    
}
