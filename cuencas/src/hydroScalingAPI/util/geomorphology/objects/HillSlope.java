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
 * HillSlope.java
 *
 * Created on November 13, 2001, 11:25 AM
 */

package hydroScalingAPI.util.geomorphology.objects;

/**
 * Uses a recursive algorithm to find the locations in the DEM that drain to a network
 * link, and defines the boundary of these group of points.
 * @author Ricardo Mantilla
 */
public class HillSlope extends java.lang.Object {
    
    private java.util.Vector dToOutlet=new java.util.Vector();
    
    private int[][] xyHillSlope;
    private float[][] LonLatHillSlope;
    private float[][] xyContorno;
    private int minX,maxX,minY,maxY;
    private int thisLinkMagn;
    private hydroScalingAPI.io.MetaRaster localMetaRaster;
    
    private int[][] toMark;
    private int numPixHill=0;
    
    private int[][] idsHill;
    
    /**
     * Creates an instance of HillSlope
     * @param x The column number of the link contact
     * @param y The row number of the link contact
     * @param fullDirMatrix The direction matrix associated to the DEM where the basin is ebedded
     * @param magnitudes The magnitudes matrix associated to the DEM where the basin is ebedded
     * @param mr The MetaRaster that describes the DEM
     */
    public HillSlope(int x, int y, byte[][] fullDirMatrix, int[][] magnitudes, hydroScalingAPI.io.MetaRaster mr) {
        
        //assumes unpruned directions matrix
        
        localMetaRaster=mr;
        
        minX=fullDirMatrix[0].length-1;
        maxX=0;
        minY=fullDirMatrix.length-1;
        maxY=0;
        
        thisLinkMagn=magnitudes[y][x];
        //System.out.println("Limits: "+magnitudes.length+" "+magnitudes[0].length);
        //System.out.println("Hill Outlet: "+x+" "+y+" "+thisLinkMagn);
        
        idsHill=new int[5000][];
        
        toMark=new int[1][]; toMark[0]=new int[] {x,y};
        idsHill[numPixHill]=toMark[0];
        numPixHill++;
        while(toMark != null){
            getHill(fullDirMatrix,toMark,magnitudes);
        }
        
        LonLatHillSlope=new float[2][numPixHill];
        xyHillSlope=new int[2][numPixHill];
        
        for (int k=0; k <numPixHill; k++){
            int[] xys=idsHill[k];
            LonLatHillSlope[0][k]=(float) (xys[0]*localMetaRaster.getResLon()/3600.0f+localMetaRaster.getMinLon());
            xyHillSlope[0][k]=xys[0];
            minX=Math.min(minX,xys[0]); maxX=Math.max(maxX,xys[0]); 
            LonLatHillSlope[1][k]=(float) (xys[1]*localMetaRaster.getResLat()/3600.0f+localMetaRaster.getMinLat());
            xyHillSlope[1][k]=xys[1];
            minY=Math.min(minY,xys[1]); maxY=Math.max(maxY,xys[1]); 
        }
        
        idsHill=null;
    }
    
    private void getHill(byte [][] fullDirMatrix,int[][] ijs,int[][] magnitudes){
        
        java.util.Vector tribsVector=new java.util.Vector();
        //System.out.println(" tribvector size  "+tribsVector.size());
       
        for(int incoming=0;incoming<ijs.length;incoming++){
            int j=ijs[incoming][0];
            int i=ijs[incoming][1];
            for (int k=0; k <= 8; k++){
                if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k && (magnitudes[i+(k/3)-1][j+(k%3)-1] == thisLinkMagn || magnitudes[i+(k/3)-1][j+(k%3)-1]<=0)){
                    //System.out.println("ijs.length  "+ijs.length + "  loop k " + k + "  tribsVector " + tribsVector.size() + " i " + i);
                    tribsVector.add(new int[] {j+(k%3)-1,i+(k/3)-1});
                    //getBasin(i+(k/3)-1,j+(k%3)-1, fullDirMatrix,idsBasin);
                }
            }
        }
        int countTribs=tribsVector.size();
        //System.out.println(countTribs);
        if(countTribs != 0){
            toMark=new int[countTribs][2];
            for(int k=0;k<countTribs;k++){
                toMark[k]=(int[])tribsVector.get(k);
                //System.out.println("k " +k+ "  numPixHill" +numPixHill + "  idsHill.length" +idsHill.length +" countTribs "+countTribs);
                idsHill[numPixHill]=toMark[k];
                numPixHill++;
                
                if(numPixHill == idsHill.length){
                    //System.out.println(">> Hits the condition");
                    //System.out.println(">> Before "+idsHill.length+" "+idsHill[0][0]);
                    
                    //idsHill=java.util.Arrays.copyOf(idsHill, idsHill.length+1000);
                    int[][] tempIDS=new int[idsHill.length+1000][];
                    for (int ll = 0; ll < idsHill.length; ll++) {
                        tempIDS[ll]=idsHill[ll];
                    }
                    idsHill=tempIDS.clone();
                    
                    //System.out.println(">> After "+idsHill.length+" "+idsHill[0][0]);
                    
                    
                    
                    
                }
                //System.out.println(">>"+toMark[k][0]+" "+toMark[k][1]);
            }
        } else {
            toMark=null;
        }
    }
    
    /**
     * Returns an array of floats float[2][numPointsInHillslope] with the longitudes (float[0])
     * and latitudes (float[1]) for the points that belong to the hillslope
     * @return An array with the coordinates of the points in the landscape that belong to the
     * hillslope
     */
    public float[][] getLonLatHillSlope(){
        return LonLatHillSlope;
    }
    
    /**
     * Returns an array of integers int[2][numPointsInBasin] with the column number (int[0])
     * and the row number (int[1]) for the points that belong to the hillslope
     * @return An array of i,j position of points that belong to the hillslope
     */
    public int[][] getXYHillSlope(){
        return xyHillSlope;
    }
    
    /**
     * Tests for the class
     * @param args The command line arguments
     */
    public static void main (String args[]) {
        
        //main_0(args);
        main_1(args);
        
        
    }

    /**
     * Tests for the class
     * @param args The command line arguments
     */
    public static void main_0 (String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster (theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.magn"));
            metaModif.setFormat("Integer");
            int [][] matMagns=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            
            
            hydroScalingAPI.util.geomorphology.objects.HillSlope theHillSlope=new hydroScalingAPI.util.geomorphology.objects.HillSlope(82,260,matDirs,matMagns,metaModif);
            System.out.println(java.util.Arrays.toString(theHillSlope.xyHillSlope[0]));
            System.out.println(java.util.Arrays.toString(theHillSlope.xyHillSlope[1]));
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    /**
     * Tests for the class
     * @param args The command line arguments
     */
    public static void main_1 (String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster (theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.magn"));
            metaModif.setFormat("Integer");
            int [][] matMagns=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            
            hydroScalingAPI.util.geomorphology.objects.HillSlope theHillSlope=new hydroScalingAPI.util.geomorphology.objects.HillSlope(7875, 1361,matDirs,matMagns,metaModif);
            System.out.println(java.util.Arrays.toString(theHillSlope.xyHillSlope[0]));
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
        
    }

}
