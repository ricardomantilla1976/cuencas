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
 * CopyFile.java
 *
 * Created on July 12, 2003, 11:26 AM
 */

package hydroScalingAPI.tools;

/**
 * An abstract class to manage files
 * @author Ricardo Mantilla
 */
public abstract class FileManipulation {
    
    /**
     * Creates a new instance of CopyFile
     * @param sourceFile The source file
     * @param destinationFile The destination file
     */
    public static void  CopyFile(java.io.File sourceFile, java.io.File destinationFile) {
        
        try {
            java.nio.channels.FileChannel sourceChannel =new java.io.FileInputStream(sourceFile).getChannel();
            java.nio.channels.FileChannel destinationChannel =new java.io.FileOutputStream(destinationFile).getChannel();
            destinationChannel.transferFrom(sourceChannel, 0, sourceChannel.size());
            sourceChannel.close();
            destinationChannel.close();
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }
        
    }
    
}
