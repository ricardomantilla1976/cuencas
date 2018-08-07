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
 * BasinsLogReader.java
 *
 * Created on August 11, 2003, 3:18 PM
 */

package hydroScalingAPI.io;

/**
 * This Class reads the .log file where locations of previously extracted basins
 * are stored
 * @author Ricardo Mantilla
 */
public class BasinsLogReader {
    
    private java.util.Vector availableBasins=new java.util.Vector();
    private java.io.File basinsLog;
    
    /**
     * Creates a new instance of BasinsLogReader
     * @param bl A file pointing to the location of the .log file
     * @throws java.io.IOException Captures problems in the file format
     */
    public BasinsLogReader(java.io.File bl) throws java.io.IOException {
        basinsLog=bl;
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(basinsLog));
        String fullLine;
        fullLine=fileMeta.readLine();
        while (fullLine != null) {
            availableBasins.add(fullLine);
            fullLine=fileMeta.readLine();
        }
        
        fileMeta.close();
    }
    
    /**
     * This method delivers the elements in the .log file as an array of Strings
     * @return Returns the list of basins in the .log file.
     */
    public String[] getPresetBasins(){
        String[] names=new String[availableBasins.size()];
        for(int i=0;i<names.length;i++) names[i]=(String)availableBasins.get(i);
        return names;
    }
    
    /**
     * Appends a new x,y and description for a basin outlet to the .log file
     * @param newBasin A String containing a new x,y and description for a basin outlet to be written into the .log file
     * @throws java.io.IOException Captures errors during .log file rewriting.
     */
    public void addBasinToFile(String newBasin) throws java.io.IOException {
        java.io.OutputStreamWriter newfile=new java.io.OutputStreamWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(basinsLog)));
        availableBasins.add(newBasin);
        for(int i=0;i<availableBasins.size();i++) newfile.write((String)availableBasins.get(i)+"\n");
        newfile.close();
        
    }
    
}
