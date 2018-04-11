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
 * ArrayTools.java
 *
 * Created on September 11, 2003, 4:32 PM
 */

package hydroScalingAPI.tools;

/**
 * An abstract class with a set of tools to manipulate arrays
 * @author Ricardo Mantilla
 */
public abstract class ArrayTools {
    
    /**
     * Removes a given position from the array
     * @param theFloatArray The original array with float falues
     * @param pos The position to remove
     * @return A new array of integers
     */
    public static float[] eliminate(float[] theFloatArray,int pos){
        float[] theTrimmedArray = new float[theFloatArray.length-1];
        int ks=0;
        for (int i=0;i<theFloatArray.length;i++){
            if(i != pos) theTrimmedArray[ks++] = theFloatArray[i];
        }
        return theTrimmedArray;
    }
    
    /**
     * Concatenates two Object Arrays
     * @param StArr1 The first Object array
     * @param StArr2 The second Object array
     * @return A new concatenated Object array
     */
    public static Object[] concatentate(Object[] StArr1,Object[] StArr2){
        Object[] concat = new Object[StArr1.length+StArr2.length];
        for (int i=0;i<StArr1.length;i++){
            concat[i] = StArr1[i];
        }
        for (int i=0;i<StArr2.length;i++){
            concat[StArr1.length+i] = StArr2[i];
        }
        return concat;
    }
    
    /**
     * Casts float values to int
     * @param theFloatArray The original array with float falues
     * @return A new array of integers
     */
    public static int[] convertFloatArrayToIntArray(float[] theFloatArray){
        int[] theIntArray = new int[theFloatArray.length];
        for (int i=0;i<theFloatArray.length;i++){
            theIntArray[i] = (int) theFloatArray[i];
        }
        return theIntArray;
    }
    
    /**
     * Prints the contents of an integer array
     * @param theArray The int array to print
     */
    public static void printArray(int[] theArray){
        //method prints array values to standard output
        String arrayString = "";
        for (int i=0;i<theArray.length;i++){
            arrayString = arrayString +  theArray[i]+" ";
        }
        System.out.println(arrayString);
    }
    
    /**
     * Changes the dimensions of a two dimensional array into a one dimensional array
     * @param twoArray The original two dimensional array
     * @return A one dimensional array
     */
    public static float[] convertTwoToOne(float[][] twoArray){
        float[] oneArray = new float[twoArray[0].length];
        for (int i = 0;i<twoArray[0].length;i++){
             oneArray[i] = twoArray[0][i];
        }
        return oneArray;

    }
}