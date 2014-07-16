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

package hydroScalingAPI.util.geomorphology.objects;

/**
 * Uses a recursive algorithm to find the locations in the DEM that drain through a
 * given point in the landscape, and defines the boundary of these group of points.
 * This class implements several methods for analysis of characteristics in the
 * basin
 * @author Ricardo Mantilla
 */
public class DamInundation extends Object{

    private int[][] xyBasin;
    
    private int[][] xyContour;
    private float[][] lonLatCountour;
        
    private int minX,maxX,minY,maxY;
    private float minZ,maxZ;
    private float[] elevationsBasin;
    
    private int outletX,outletY;
    
    private hydroScalingAPI.io.MetaRaster localMetaRaster;
    
    private int[][] toMark;
    private float h_d;
    
    /**
     * The vertical size of the pixel
     */
    private double dy;
    /**
     * The average horizontal size of the pixel
     */
    private double dxm;
    /**
     * An array containing the horizontal size of the pixels
     */
    private double[] dx;
    
    private double totalArea, totalVolume;
    
    /**
     * Creates an instance of Basin
     * @param x The column number of the basin outlet
     * @param y The row number of the basin outlet
     * @param fullDirMatrix The direction matrix associated to the DEM where the basin is ebedded
     * @param mr The MetaRaster that describes the DEM
     */
    public DamInundation(int x, int y,float dh, byte[][] fullDirMatrix, float[][] dem, hydroScalingAPI.io.MetaRaster mr) {
        
        int nfila=mr.getNumRows();
        int ncol=mr.getNumCols();
        
        dy = 1000*6378.0*mr.getResLat()*Math.PI/(3600.0*180.0);
        dx = new double[nfila+1];
        
        /*Se calcula para cada fila del DEMC el valor de la distancia horizontal del pixel
          y la diagonal, dependiendo de la latitud.*/
        for (int i=1 ; i<=nfila ; i++){
            dx[i] = 1000*6378.0*Math.cos((i*mr.getResLat()/3600.0 + mr.getMinLat())*Math.PI/180.0)*mr.getResLat()*Math.PI/(3600.0*180.0);
        }
        
        h_d=dh;
        
        localMetaRaster=mr;
        
        minX=fullDirMatrix[0].length-1;
        maxX=0;
        minY=fullDirMatrix.length-1;
        maxY=0;
        
        outletX=x;
        outletY=y;
        
        java.util.Vector idsBasin=new java.util.Vector(fullDirMatrix[0].length*fullDirMatrix.length);
        
        //Step one: Identify pixels on dam
        
        int[][][] movementsKey=new int[][][]
        {
        {{-1,	-1},
         {1,	1}},
        {{-1,	0},
         {1,	0}},
        {{-1,	1},
         {1,	-1}},
        {{0,	-1},
         {0,	1}},
        {{0,	0},
         {0,	0}},
        {{0,	1},
         {0,	-1}},
        {{1,	-1},
         {-1,	-1}},
        {{1,	0},
         {-1,	0}},
        {{1,	1},
         {-1,	-1}}
        };
        
        toMark=new int[1][]; toMark[0]=new int[] {x,y};
        
        idsBasin.add(toMark[0]);
        totalArea+=dy*dx[y];
        totalVolume+=dy*dx[y]*h_d;
        
        while(toMark != null){
            getLake(fullDirMatrix, dem, idsBasin,toMark);
        }
        
//        //to the left
//        int LeftX=movementsKey[fullDirMatrix[y][x]][0][0];
//        int LeftY=movementsKey[fullDirMatrix[y][x]][0][1];
//        
//        while(dem[y+LeftY][x+LeftX]-dem[y][x] < h_d){
//            
//            System.out.println("Added the pixel to the Left "+(x+LeftX)+" "+(y+LeftY));
//            
//            toMark=new int[1][]; toMark[0]=new int[] {x+LeftX,y+LeftY};
//            idsBasin.add(toMark[0]);
//            while(toMark != null){
//                getLake(fullDirMatrix, dem, idsBasin,toMark);
//            }
//        }
//        
//        //to the right
//        int RightX=movementsKey[fullDirMatrix[y][x]][1][0];
//        int RightY=movementsKey[fullDirMatrix[y][x]][1][1];
//        
//        while(dem[y+RightY][x+RightX]-dem[y][x] < h_d){
//            toMark=new int[1][]; toMark[0]=new int[] {x+RightX,y+RightY};
//            idsBasin.add(toMark[0]);
//            while(toMark != null){
//                getLake(fullDirMatrix, dem, idsBasin,toMark);
//            }
//        }
        
        
        
        xyBasin=new int[2][idsBasin.size()];
        
        int nPoints=idsBasin.size();
        
        for (int k=0; k < nPoints; k++){
            int[] xys=(int[]) idsBasin.elementAt(k);
            xyBasin[0][k]=xys[0];
            minX=Math.min(minX,xys[0]); maxX=Math.max(maxX,xys[0]); 
            xyBasin[1][k]=xys[1];
            minY=Math.min(minY,xys[1]); maxY=Math.max(maxY,xys[1]); 
        }
        
        findBasinDivide();
        
    }
    
    /**
     * Prints a description of the Basin with respect to its location in the DEM
     * @return A String
     */
    public String toString(){
        return localMetaRaster.getLocationBinaryFile().getName()+"_Basin_x"+xyBasin[0][0]+"_y_"+xyBasin[1][0];
    }
    
    private void getLake(byte [][] fullDirMatrix, float[][] dem, java.util.Vector idsBasin,int[][] ijs){
        java.util.Vector tribsVector=new java.util.Vector();
        
        for(int incoming=0;incoming<ijs.length;incoming++){
            int j=ijs[incoming][0];
            int i=ijs[incoming][1];
            for (int k=0; k <= 8; k++){
                
                if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k && dem[i+(k/3)-1][j+(k%3)-1]-dem[outletY][outletX] <= h_d){
                //if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k){    
                    tribsVector.add(new int[] {j+(k%3)-1,i+(k/3)-1});
                    totalArea+=dy*dx[i+(k/3)-1];
                    totalVolume+=dy*dx[i+(k/3)-1]*(dem[outletY][outletX]+(h_d)-dem[i+(k/3)-1][j+(k%3)-1]);
                }
            }

        }
        int countTribs=tribsVector.size();
        //System.out.println(countTribs);
        if(countTribs != 0){
            toMark=new int[countTribs][2];
            for(int k=0;k<countTribs;k++){
                toMark[k]=(int[])tribsVector.get(k);
                idsBasin.add(toMark[k]);
                //System.out.println(">>"+toMark[k][0]+" "+toMark[k][1]);
            }
        } else {
            toMark=null;
        }
    }
    
    public double getLakeArea(){
        return totalArea;
    }
    
    public double getLakeVolume(){
        return totalVolume;
    }
    
    
    
    /*public void getBasin(int i,int j,byte [][] fullDirMatrix, java.util.Vector idsBasin){
       for (int k=0; k <= 8; k++){
           if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k){
             idsBasin.add(new int[] {j+(k%3)-1,i+(k/3)-1});
             getBasin(i+(k/3)-1,j+(k%3)-1, fullDirMatrix,idsBasin);
           }
       }
    }*/
    
    /**
     * Returns an array of floats float[2][numPointsInBasin] with the longitudes (float[0])
     * and latitudes (float[1]) for the points that belong to the basin
     * @return An array with the coordinates of the points in the landscape that belong to the
     * basin
     */
    public float[][] getLonLatBasin(){
        
        float[][] LonLatBasin=new float[2][xyBasin[0].length];
        
        double resLon=localMetaRaster.getResLon();
        double resLat=localMetaRaster.getResLat();
        double minLon=localMetaRaster.getMinLon();
        double minLat=localMetaRaster.getMinLat();
    
        for (int k=0; k < xyBasin[0].length; k++){
            LonLatBasin[0][k]=(float) (xyBasin[0][k]*resLon/3600.0f+minLon);
            LonLatBasin[1][k]=(float) (xyBasin[1][k]*resLat/3600.0f+minLat);
        }
        return LonLatBasin;
    }
    
    /**
     * Returns an array of integers int[2][numPointsInBasin] with the column number (int[0])
     * and the row number (int[1]) for the points that belong to the basin
     * @return An array of i,j position of points that belong to the basin
     */
    public int[][] getXYBasin(){
        return xyBasin;
    }
    
    /**
     * Returns an array of integers int[2][numPointsInBasin] with the column number (int[0])
     * and the row number (int[1]) for the points that belong to the basin
     * @return An array of i,j position of points that belong to the basin
     */
    public void setXYBasin(int[][] newXY){
        
        xyBasin=newXY;
        
        minX=localMetaRaster.getNumCols()-1;
        maxX=0;
        minY=localMetaRaster.getNumRows()-1;
        maxY=0;
        
        for (int k=0; k < xyBasin[0].length; k++){
            minX=Math.min(minX,xyBasin[0][k]); maxX=Math.max(maxX,xyBasin[0][k]); 
            minY=Math.min(minY,xyBasin[1][k]); maxY=Math.max(maxY,xyBasin[1][k]); 
        }
        
        findBasinDivide();
    }
    
    /**
     * Creates visad.Gridded2DSet with the basin divide polygon
     * @throws visad.VisADException Captures errors while creating the visad object
     * @return A visad.Gridded2DSet
     */
    public visad.Gridded2DSet getBasinDivide() throws visad.VisADException {
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        visad.Gridded2DSet basinCountour=new visad.Gridded2DSet(domain,lonLatCountour,lonLatCountour[0].length);
        
        return basinCountour;
    }
    
    /**
     * Uses the basin divide to determine a shape factor for the basin.  Shape factor
     * equals the ratio between the basin diameter and the basin width
     * @return An int[3] array where int[0] is the shape factor, int[1] is the basin diameter
     * and int[2] is the basin width perpendicular to the diameter line
     */
    public float[] getDivideShapeFactor(){
        
        float shapeF=1;
        
        float[][] lonLatCountourMeters=new float[2][lonLatCountour[0].length];
        
        geotransform.coords.Gdc_Coord_3d gdc = new geotransform.coords.Gdc_Coord_3d();
        geotransform.coords.Utm_Coord_3d utm = new geotransform.coords.Utm_Coord_3d();
        utm.zone=(byte)(4/27.0*lonLatCountour[1][0]+28.666);
        utm.z=0;
        utm.hemisphere_north=true;

        geotransform.transforms.Gdc_To_Utm_Converter.Init(new geotransform.ellipsoids.CD_Ellipsoid());
        
        for(int i=0;i<lonLatCountour[0].length;i++){
        
            gdc.longitude=lonLatCountour[0][i];
            gdc.latitude=lonLatCountour[1][i];

            geotransform.transforms.Gdc_To_Utm_Converter.Convert(gdc,utm);
            
            lonLatCountourMeters[0][i]=(float)utm.x;
            lonLatCountourMeters[1][i]=(float)utm.y;

            
        }
        
        float dm=Float.MIN_VALUE; int llPos=0;
        for(int i=0;i<lonLatCountourMeters[0].length;i++){
            float ddd=distancePoints(lonLatCountourMeters[0][0], lonLatCountourMeters[1][0], lonLatCountourMeters[0][i], lonLatCountourMeters[1][i]);
            if(ddd > dm) {
                dm=ddd;
                llPos=i;
            }
        }
        float dm1=Float.MIN_VALUE,dm2=Float.MIN_VALUE, dAl=Float.MIN_VALUE;
        for(int i=llPos+1;i<lonLatCountourMeters[0].length;i++){
            dm1=distancePoints(lonLatCountourMeters[0][llPos], lonLatCountourMeters[1][llPos], lonLatCountourMeters[0][i], lonLatCountourMeters[1][i]);
            dm2=distancePoints(lonLatCountourMeters[0][llPos], lonLatCountourMeters[0][llPos], lonLatCountourMeters[0][i], lonLatCountourMeters[0][i]);
            dAl=Math.max(dm1*dm2/(float)Math.sqrt(dm1*dm1+dm2*dm2),dAl);
        }
        float dAr=Float.MIN_VALUE;
        for(int i=1;i<llPos;i++){
            dm1=distancePoints(lonLatCountourMeters[0][0], lonLatCountourMeters[1][0], lonLatCountourMeters[0][i], lonLatCountourMeters[1][i]);
            dm2=distancePoints(lonLatCountourMeters[0][0], lonLatCountourMeters[0][0], lonLatCountourMeters[0][i], lonLatCountourMeters[0][i]);
            dAr=Math.max(dm1*dm2/(float)Math.sqrt(dm1*dm1+dm2*dm2),dAr);
        }
        shapeF=dm/(dAr+dAl);
        
        return new float[] {shapeF,dm,dAr+dAl};
    }
    
    private float distancePoints(float X0,float Y0, float X1,float Y1){
        return (float)Math.sqrt(Math.pow(X0-X1,2)+Math.pow(Y0-Y1,2));
    }
    
    /**
     * Returns an array of integers int[2][numPointsInDivide] with the column number (int[0])
     * and the row number (int[1]) for the points that determine the divide
     * @return An int[][] array
     */
    public int[][] getXYBasinDivide(){
        return xyContour;
    }
    
    /**
     * Returns an array of floats float[2][numPointsInDivide] with the longitudes (float[0])
     * and latitudes (float[1]) for the points that determine the divide
     * @return  A float[][] array
     */
    public float[][] getLonLatBasinDivide(){
        return lonLatCountour;
    }
    
    /**
     * Returns an visad object that describes the basin outlet
     * @throws java.rmi.RemoteException Captures errors while assigning values to VisAD data objects
     * @throws visad.VisADException Captures errors while creating VisAD objects
     * @return A visad.RealTuple of the basin outlet
     */
    public visad.RealTuple getOutletTuple() throws visad.VisADException, java.rmi.RemoteException{
        float[] LonLatBasin=new float[2];
        
        LonLatBasin[0]=(float) (xyBasin[0][0]*localMetaRaster.getResLon()/3600.0f+localMetaRaster.getMinLon());
        LonLatBasin[1]=(float) (xyBasin[1][0]*localMetaRaster.getResLat()/3600.0f+localMetaRaster.getMinLat());

        visad.Real[] rtd1 = {new visad.Real(visad.RealType.Longitude, LonLatBasin[0]),
                             new visad.Real(visad.RealType.Latitude,  LonLatBasin[1])};
        return new visad.RealTuple(rtd1);
    }
    
    private void findBasinDivide(){
        
        //Relleno la matriz con 0's y 1's
        
        int[][] matrizBusqueda=new int[maxY-minY+5][maxX-minX+5];
        for (int j=0;j<xyBasin[0].length;j++){
            matrizBusqueda[xyBasin[1][j]-minY+2][xyBasin[0][j]-minX+2]=1;
        }
        
        //Determino la direccion inicial de busqueda
        
        int dirSalida=0;
        int outX=xyBasin[0][0]-minX+2;
        int outY=xyBasin[1][0]-minY+2;
        
        if (matrizBusqueda[outY+0][outX+1]==0){
            dirSalida=180;
            outX=outX+1;
            outY=outY+0;
        } else  if (matrizBusqueda[outY+0][outX-1]==0){
                    dirSalida=0;
                    outX=outX-1;
                    outY=outY+0;
                } else  if (matrizBusqueda[outY+1][outX+0]==0){
                                dirSalida=270;
                                outX=outX+0;
                                outY=outY+1;
                        } else  if (matrizBusqueda[outY-1][outX+0]==0){
                                    dirSalida=90;
                                    outX=outX+0;
                                    outY=outY-1;
                                }
        
        int searchPointX=outX;
        int searchPointY=outY;
        
        java.util.Vector idsContorno=new java.util.Vector();
        
        int foreX;
        int foreY;
                
        do{
            do{
                dirSalida+=90;
                foreX=searchPointX+(int) Math.round(Math.cos(dirSalida*Math.PI/180.0));
                foreY=searchPointY+(int) Math.round(Math.sin(dirSalida*Math.PI/180.0));
            } while(matrizBusqueda[foreY][foreX] == 1);
            matrizBusqueda[foreY][foreX]=8;
            
            for (int dirVec=-90;dirVec<=180;dirVec+=90){
                int lookX=foreX+(int) Math.round(Math.cos((dirSalida+dirVec)*Math.PI/180.0));
                int lookY=foreY+(int) Math.round(Math.sin((dirSalida+dirVec)*Math.PI/180.0));
                if (matrizBusqueda[lookY][lookX]==1){
                    idsContorno.add(new int[] {foreX+(int) Math.round(Math.abs(Math.cos(((dirSalida+dirVec-270)-45)*Math.PI/360.0))),foreY+(int) Math.round(Math.abs(Math.cos(((dirSalida+dirVec)-45)*Math.PI/360.0)))});
                } else break;
            }
            searchPointX=foreX;
            searchPointY=foreY;
            dirSalida-=180;
        } while(foreX!=outX || foreY!=outY);
        
        idsContorno.add(idsContorno.firstElement());
        
        lonLatCountour=new float[2][idsContorno.size()];
        xyContour=new int[2][idsContorno.size()];
        
        
        double resLon=localMetaRaster.getResLon();
        double resLat=localMetaRaster.getResLat();
        double minLon=localMetaRaster.getMinLon();
        double minLat=localMetaRaster.getMinLat();
    
        for (int k=0; k <idsContorno.size(); k++){
            int[] xys=(int[]) idsContorno.elementAt(k);
            xyContour[0][k]=xys[0]+minX-2;
            xyContour[1][k]=xys[1]+minY-2;
            lonLatCountour[0][k]=(float) ((xys[0]+minX-2)*resLon/3600.0f+minLon);
            lonLatCountour[1][k]=(float) ((xys[1]+minY-2)*resLat/3600.0f+minLat);
        }
    }
    
    /**
     * The minimum column of a box encosing the basin
     * @return The column index
     */
    public int getMinX(){
        return minX;
    }
    
    /**
     * The maximum column of a box encosing the basin
     * @return The column index
     */
    public int getMaxX(){
        return maxX;
    }
    
    /**
     * The minimum row of a box encosing the basin
     * @return The column index
     */
    public int getMinY(){
        return minY;
    }
    
    /**
     * The maximum row of a box encosing the basin
     * @return The column index
     */
    public int getMaxY(){
        return maxY;
    }
    
    /**
     * Reads the corrected DEM and returns the elevations of the points contained in the basin
     * @return A float[] with the elevations of the points
     * @throws java.io.IOException Captures errors while reading from the DEM
     */
    public float[] getElevations() throws java.io.IOException{
        
        if(elevationsBasin == null){
            
            minZ=Float.MAX_VALUE;
            maxZ=Float.MIN_VALUE;
        
            elevationsBasin=new float[xyBasin[0].length];
            String currFormat=localMetaRaster.getFormat();
            java.io.File currBinLoc=localMetaRaster.getLocationBinaryFile();
            localMetaRaster.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".corrDEM"));
            localMetaRaster.setLocationBinaryFile(new java.io.File(currBinLoc.getAbsolutePath().subSequence(0, currBinLoc.getAbsolutePath().lastIndexOf("."))+".corrDEM"));
            float[][] elevations=new hydroScalingAPI.io.DataRaster(localMetaRaster).getFloat();
            localMetaRaster.restoreOriginalFormat();
            localMetaRaster.setLocationBinaryFile(currBinLoc);

            for (int k=0; k < xyBasin[0].length; k++){
                elevationsBasin[k]=elevations[xyBasin[1][k]][xyBasin[0][k]]; 
                minZ=Math.min(minZ,elevationsBasin[k]); 
                maxZ=Math.max(maxZ,elevationsBasin[k]); 
            }
        }
        
        return elevationsBasin;
    }
    
    /**
     * Returns the difference between the highest and the lowest points in the basin
     * @throws java.io.IOException Captures errors while reading from the DEM
     * @return The basin relief
     */
    public float getRelief() throws java.io.IOException{
        
        getElevations();
        
        return maxZ-minZ;
        
    }

    /**
     * Returns the elevation of the highest point in the basin
     * @throws java.io.IOException Captures errors while reading from the DEM
     * @return The highest point
     */
    public float getMaxZ() throws java.io.IOException{

        getElevations();

        return maxZ;

    }

    /**
     * Returns the elevation of the lowest point in the basin
     * @throws java.io.IOException Captures errors while reading from the DEM
     * @return The lowest point
     */
    public float getMinZ() throws java.io.IOException{

        getElevations();

        return minZ;
        
    }
            
    /**
     * Returns the ID of the basin outlet
     * @return An integer with the basin ID
     */
    public int getOutletID(){
        return xyBasin[1][0]*localMetaRaster.getNumCols()+xyBasin[0][0];
    }
    
    /**
     * Returns and array of the same size of the DEM with 1s in the positions that belong to the basin
     * @return A byte[][] with the basin mask
     */
    public  byte[][] getBasinMask(){
        byte[][] basinMask = new byte[localMetaRaster.getNumRows()][localMetaRaster.getNumCols()];
        for (int i=0;i<xyBasin[0].length;i++){
            basinMask[xyBasin[1][i]][xyBasin[0][i]]=1;
        }
        return basinMask;
    }

    /**
     * Returns and array of the same size of the DEM with and ID in the positions that belong to the basin.
     * Points with the same ID correspond to a unique pseudo-hillslope at the level Omega
     * @param myRSNAnalysis
     * @param level
     * @return A byte[][] with the basin network mask
     */
     public  int[][] getHillslopesMask(byte[][] matDir, hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRSNAnalysis, int level){
        try{
            java.io.File hortFile=new java.io.File(localMetaRaster.getLocationBinaryFile().getPath().substring(0,localMetaRaster.getLocationBinaryFile().getPath().lastIndexOf("."))+".horton");
            localMetaRaster.setLocationBinaryFile(hortFile);
            localMetaRaster.setFormat("Byte");
            byte [][] matOrders=new hydroScalingAPI.io.DataRaster(localMetaRaster).getByte();
            
            localMetaRaster.restoreOriginalFormat();

            int[][] matrizPintada=new int[localMetaRaster.getNumRows()][localMetaRaster.getNumCols()];
            int[][] headsTails=myRSNAnalysis.getHeadsAndTails(level);

            hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile myTileActual;

            int numCols=localMetaRaster.getNumCols();

            for(int i=0;i<headsTails[0].length;i++){
                int xOulet=headsTails[0][i]%numCols;
                int yOulet=headsTails[0][i]/numCols;

                int xSource=headsTails[2][i]%numCols;
                int ySource=headsTails[2][i]/numCols;

                if(headsTails[3][i] == 0){
                    xSource=-1;
                    ySource=-1;
                }

                int tileColor=i+1;
                //System.out.println("Head: "+xOulet+","+yOulet+" Tail: "+xSource+","+ySource+" Color: "+tileColor+" Scale: "+(scale+1));

                myTileActual=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile(xOulet,yOulet,xSource,ySource,matDir,matOrders,localMetaRaster,level+1);
                int elementsInTile=myTileActual.getXYRsnTile()[0].length;
                for (int j=0;j<elementsInTile;j++){
                    matrizPintada[myTileActual.getXYRsnTile()[1][j]][myTileActual.getXYRsnTile()[0][j]]=tileColor;
                }
            }

            return matrizPintada;

        }  catch (java.io.IOException ioe){
            ioe.printStackTrace();
            return null;
        }
    }
    
    /**
     * Returns and array of the same size of the DEM with 1s in the positions that are network and belong to the basin
     * @return A byte[][] with the basin network mask
     */
    public  byte[][] getEncapsulatedNetworkMask(){
        try{
            String pathToRasterNetwork=localMetaRaster.getLocationBinaryFile().getPath();
            pathToRasterNetwork=pathToRasterNetwork.subSequence(0, pathToRasterNetwork.lastIndexOf("."))+".redRas";
            localMetaRaster.setLocationBinaryFile(new java.io.File(pathToRasterNetwork));
            localMetaRaster.setFormat("Byte");

            byte [][] rasterNetwork=new hydroScalingAPI.io.DataRaster(localMetaRaster).getByte();

            byte[][] basinMask = new byte[maxY-minY+1][maxX-minX+1];
            for (int i=0;i<xyBasin[0].length;i++){
                if (rasterNetwork[xyBasin[1][i]][xyBasin[0][i]] == 1){
                    basinMask[xyBasin[1][i]-minY][xyBasin[0][i]-minX]=1;
                }
            }
            return basinMask;
        }  catch (java.io.IOException ioe){
            System.err.println("Failed loading raster network file. "+localMetaRaster);
            System.err.println(ioe);
        }
        return null;
    }
    
    /**
     * Returns and array of the same size of the DEM with and ID in the positions that belong to the basin.
     * Points with the same ID correspond to a unique pseudo-hillslope at the level Omega
     * @param myRSNAnalysis 
     * @param level 
     * @return A byte[][] with the basin network mask
     */
     public  int[][] getEncapsulatedHillslopesMask(byte[][] matDir, hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRSNAnalysis, int level){
        try{
            java.io.File hortFile=new java.io.File(localMetaRaster.getLocationBinaryFile().getPath().substring(0,localMetaRaster.getLocationBinaryFile().getPath().lastIndexOf("."))+".horton");
            localMetaRaster.setLocationBinaryFile(hortFile);
            localMetaRaster.setFormat("Byte");
            byte [][] matOrders=new hydroScalingAPI.io.DataRaster(localMetaRaster).getByte();
            
            localMetaRaster.restoreOriginalFormat();
            
            int[][] matrizPintada=new int[maxY-minY+3][maxX-minX+3];
            int[][] headsTails=myRSNAnalysis.getHeadsAndTails(level);
            
            hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile myTileActual;
            
            int numCols=localMetaRaster.getNumCols();
            
            for(int i=0;i<headsTails[0].length;i++){
                int xOulet=headsTails[0][i]%numCols;
                int yOulet=headsTails[0][i]/numCols;
                
                int xSource=headsTails[2][i]%numCols;
                int ySource=headsTails[2][i]/numCols;
                
                if(headsTails[3][i] == 0){
                    xSource=-1;
                    ySource=-1;
                }
                
                int tileColor=i+1;
                //System.out.println("Head: "+xOulet+","+yOulet+" Tail: "+xSource+","+ySource+" Color: "+tileColor+" Scale: "+(scale+1));
                
                myTileActual=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnTile(xOulet,yOulet,xSource,ySource,matDir,matOrders,localMetaRaster,level+1);
                int elementsInTile=myTileActual.getXYRsnTile()[0].length;
                for (int j=0;j<elementsInTile;j++){
                    matrizPintada[myTileActual.getXYRsnTile()[1][j]-minY+1][myTileActual.getXYRsnTile()[0][j]-minX+1]=tileColor;
                }
            }
            
            return matrizPintada;
            
        }  catch (java.io.IOException ioe){
            ioe.printStackTrace();
            return null;
        }
    }
    
    
    /**
     * Computes the hypsometric curve and some statistical parameters: area, mean, standard deviation, skewness, and kurtosis.
     * @return Vector object with: a/A, h/H, mean, sd, kurtosis, skewness
     */
    public java.util.Hashtable getHypCurve() throws java.io.IOException{
        
        java.util.Hashtable hyp = new java.util.Hashtable();
        float[] eleBasin=this.getElevations();
        java.util.Arrays.sort(eleBasin);
        float localMinElev = eleBasin[0];
        float localMaxElev = eleBasin[eleBasin.length-1];
        
        float[] keyElev = new float[100];
        float[] accumElev = new float[100];
        float[] keyElev_Dens = new float[100];
        int k=0;
        float bin = (localMaxElev-localMinElev)/99.0f;
        for(int i=0;i<eleBasin.length;i++){
            float eleToTest = localMinElev+k*bin;
            if(eleBasin[i] >= eleToTest) {
                keyElev[keyElev.length-1-k]=(eleToTest-localMinElev)/(localMaxElev-localMinElev);
                accumElev[k]=i/(float)eleBasin.length;
                k++;
                i--;
            }
        }
        
        keyElev_Dens[0] = -1f*(keyElev[1] - keyElev[0])/(accumElev[1] - accumElev[0]);        
        keyElev_Dens[keyElev_Dens.length - 1] = -1f*(keyElev[keyElev_Dens.length - 1-1] - keyElev[keyElev_Dens.length - 1])/(accumElev[keyElev_Dens.length - 1-1] - accumElev[keyElev_Dens.length - 1]);

        if(Float.isInfinite(keyElev_Dens[0]))
            keyElev_Dens[0] = -9999f;     
        if(Float.isInfinite(keyElev_Dens[keyElev_Dens.length - 1]))
            keyElev_Dens[keyElev_Dens.length - 1] = -9999f;     

        for(int i=1;i<keyElev_Dens.length-1;i++){
            keyElev_Dens[i] = -1f*(keyElev[i+1] - keyElev[i-1])/(accumElev[i+1] - accumElev[i-1]);
            if(Float.isInfinite(keyElev_Dens[i]))
                keyElev_Dens[i] = -9999f;
        }            
        
        
        float a = 0f;
        float moment_1 = 0;
        float moment_2 = 0;
        float moment_3 = 0;
        float moment_4 = 0;
        float ad = 0f;
        float moment_1d = 0;
        float moment_2d = 0;
        float moment_3d = 0;
        float moment_4d = 0;
        
        a+=(1f/99f)/2f*accumElev[0];
        a+=(1f/99f)/2f*accumElev[keyElev.length-1];
        
        if(keyElev_Dens[0] != -9999f)
            ad+= (accumElev[1] - accumElev[0])/2f*keyElev_Dens[0];
        if(keyElev.length-1 != -9999f)
            ad+=(accumElev[keyElev.length-1] - accumElev[keyElev.length-1-1])/2f*keyElev_Dens[keyElev.length-1];
        
        for (int i=1;i<keyElev.length-1;i++){

           a+=1f/99f*accumElev[i];
           moment_1 += (accumElev[i] + accumElev[i-1])*(keyElev[i] + keyElev[i-1])*(accumElev[i] - accumElev[i-1])/4f; 
           
           if(keyElev_Dens[i] != -9999f && keyElev_Dens[i-1]!=-9999f){
               ad+=(accumElev[i+1] - accumElev[i-1])/2f*keyElev_Dens[i];
               moment_1d += (accumElev[i] + accumElev[i-1])*(keyElev_Dens[i] + keyElev_Dens[i-1])*(accumElev[i] - accumElev[i-1])/4f;            
           }
           
        }

        
        moment_1 += (accumElev[keyElev.length-1] + accumElev[keyElev.length-1-1])*(keyElev[keyElev.length-1] + keyElev[keyElev.length-1-1])*(accumElev[keyElev.length-1] - accumElev[keyElev.length-1-1])/4;
        moment_1 = moment_1/a;
        
        if(keyElev_Dens[keyElev.length-1-1] != -9999f && keyElev_Dens[keyElev.length-1]!=-9999f){
            moment_1d += (accumElev[keyElev.length-1] + accumElev[keyElev.length-1-1])*(keyElev_Dens[keyElev.length-1] + keyElev_Dens[keyElev.length-1-1])*(accumElev[keyElev.length-1] - accumElev[keyElev.length-1-1])/4;
        }
        moment_1d = moment_1d/ad;
        
        for (int i=1;i<keyElev.length;i++){
            
           moment_2 += ((accumElev[i] + accumElev[i-1])/2f - moment_1)*((accumElev[i] + accumElev[i-1])/2f - moment_1)*
                   (keyElev[i] + keyElev[i-1])*(accumElev[i] - accumElev[i-1])/2f; 

           moment_3 += ((accumElev[i] + accumElev[i-1])/2f - moment_1)*((accumElev[i] + accumElev[i-1])/2f - moment_1)*
                   ((accumElev[i] + accumElev[i-1])/2f - moment_1)*(keyElev[i] + keyElev[i-1])*(accumElev[i] - accumElev[i-1])/2f;             
           
           moment_4 += ((accumElev[i] + accumElev[i-1])/2f - moment_1)*((accumElev[i] + accumElev[i-1])/2f - moment_1)*
                   ((accumElev[i] + accumElev[i-1])/2f - moment_1)*((accumElev[i] + accumElev[i-1])/2f - moment_1)*
                   (keyElev[i] + keyElev[i-1])*(accumElev[i] - accumElev[i-1])/2f;             
           
           if(keyElev_Dens[i] != -9999f && keyElev_Dens[i-1]!=-9999f){
               
               moment_2d += ((accumElev[i] + accumElev[i-1])/2f - moment_1d)*((accumElev[i] + accumElev[i-1])/2f - moment_1d)*
                       (keyElev_Dens[i] + keyElev_Dens[i-1])*(accumElev[i] - accumElev[i-1])/2f; 

               moment_3d += ((accumElev[i] + accumElev[i-1])/2f - moment_1d)*((accumElev[i] + accumElev[i-1])/2f - moment_1d)*
                       ((accumElev[i] + accumElev[i-1])/2f - moment_1d)*(keyElev_Dens[i] + keyElev_Dens[i-1])*(accumElev[i] - accumElev[i-1])/2f;             

               moment_4d += ((accumElev[i] + accumElev[i-1])/2f - moment_1d)*((accumElev[i] + accumElev[i-1])/2f - moment_1d)*
                       ((accumElev[i] + accumElev[i-1])/2f - moment_1d)*((accumElev[i] + accumElev[i-1])/2f - moment_1d)*
                       (keyElev_Dens[i] + keyElev_Dens[i-1])*(accumElev[i] - accumElev[i-1])/2f; 
               
           }
            
        }
        
        moment_2 = moment_2/a;
        moment_3 = moment_3/a;
        moment_4 = moment_4/a;

        moment_2d = moment_2d/ad;
        moment_3d = moment_3d/ad;
        moment_4d = moment_4d/ad;

        float sk = moment_3/(float)Math.pow((double)moment_2,1.5d);
        float kur = moment_4/(float)Math.pow((double)moment_2,2d);
        float dsk = moment_3d/(float)Math.pow((double)moment_2d,1.5d);
        float dkur = moment_4d/(float)Math.pow((double)moment_2d,2d);
        
        hydroScalingAPI.util.statistics.Stats s = new hydroScalingAPI.util.statistics.Stats();
        
        s.meanValue = moment_1;
        s.standardDeviation = (float)Math.pow((double)moment_2,0.5d);
        s.kurtosis = kur;
        s.skewness = sk;
        
        hyp.put("areas",accumElev);
        hyp.put("elevs",keyElev);
        hyp.put("stat",s);
        hyp.put("integral",a);
        hyp.put("density",keyElev_Dens);
        hyp.put("dsk",dsk);
        hyp.put("dkur",dkur);
        
        return hyp;
        
    }

    /**
     * Tests for the class
     * @param args The command line arguments
     */
    public static void main (String args[]) {
        //main0(args);  // basic example of usage
        main1(args);  // Code modified by Tibebu to find storage - elevation curve
    }
    
    /**
     * Tests for the class
     * @param args The command line arguments
     */
    public static void main0 (String args[]) {
        
        
        try{
            
            //This is the path to the desired DEM
            
            java.io.File theFile=new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/Resolutions/demcc_5.metaDEM");
//            java.io.File theFile=new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/ClearCreek/NED_00159011.metaDEM");
            
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".corrDEM"));
            metaModif.setFormat("Double");
            float [][] dem=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            //parameters are x, y and dam height in the corresponding DEM
            
            //hydroScalingAPI.util.geomorphology.objects.DamInundation theDam=new hydroScalingAPI.util.geomorphology.objects.DamInundation(2856, 846 , 9,matDirs,dem,metaModif);
            hydroScalingAPI.util.geomorphology.objects.DamInundation theDam=new hydroScalingAPI.util.geomorphology.objects.DamInundation(908, 292 ,9,matDirs,dem,metaModif);
            
            //Printing area and volume of the lake
            
            System.out.println(theDam.getLakeArea());
            System.out.println(theDam.getLakeVolume());
            System.out.println();
            
            //Prininting the x,y's of inundated area
            
            int[][] points=theDam.getXYBasin();
            
            for (int i = 0; i < points[0].length; i++) {
                System.out.println(points[0][i]+";"+points[1][i]+";"+dem[points[1][i]][points[0][i]]);
            }
           
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
    public static void main1 (String args[]) {
        
        
        try{
            
            //This is the path to the desired DEM
            
            java.io.File theFile=new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/Resolutions/demcc_5.metaDEM");
//            java.io.File theFile=new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/ClearCreek/NED_00159011.metaDEM");
            
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".corrDEM"));
            metaModif.setFormat("Double");
            float [][] dem=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            //parameters are x, y and dam height in the corresponding DEM
            
            //***********************************************
            //***********************************************
            //TODO: Create a loop here for different dam heights to obtain storage curve
            //***********************************************
            //***********************************************
            
//            double [][] ricardoFarm = new double[][]{ {-91.699,	41.733,	1209,	330,	0000,	000.000,	000.000}};//30m DEM
            double [][] ricardoFarm = new double[][]{ {-91.699,	41.733,	7747,	1937,	0000,	000.000,	000.000}};//5m DEM
            double [][] reservoirsData = new double[][]
                {
                    {-91.64485931396484,	41.66958236694336,	1405,	103,	5501,	59281.27961084259,	85945.97621049781},
                    {-91.72513580322266,	41.68124771118164,	1116,	145,	5506,	84264.77884380076,	161380.8626804209},
                    {-91.7370834350586,	41.68486022949219,	1073,	158,	5517,	103537.14351697615,	211396.75114765318},
                    {-91.95930480957031,	41.6854133605957,	273,	160,	5558,	85685.10980296714,	129180.53020736316},
                    {-91.59429931640625,	41.69263458251953,	1587,	186,	5565,	272724.2354574659,	709454.0722755186},
                    {-91.91097259521484,	41.697635650634766,	447,	204,	5571,	72818.38596923299,	99965.73910469614},
                    {-91.72819519042969,	41.697914123535156,	1105,	205,	5578,	253431.86983420159,	547263.5816911625},
                    {-91.61819458007812,	41.699581146240234,	1501,	211,	5584,	30696.910147850707,	52116.86430874766},
                    {-91.93096923828125,	41.70402526855469,	375,	227,	5590,	124925.6618620705,	191259.94371180475},
                    {-91.77791595458984,	41.70458221435547,	926,	229,	5597,	42830.44813156004,	75664.13852191468},
                    {-91.88597106933594,	41.705413818359375,	537,	232,	5602,	68528.1396974163,	114933.80224961537},
                    {-91.6504135131836,	41.707637786865234,	1385,	240,	5603,	122769.58704140919,	270422.19657647866},
                    {-91.95124816894531,	41.70846939086914,	302,	243,	5607,	36403.69758190325,	39242.38940146746},
                    {-91.7562484741211,	41.710693359375,	1004,	251,	5612,	102785.4801188223,	170664.66859870285},
                    {-91.71125030517578,	41.711524963378906,	1166,	254,	5613,	256947.20228348346,	444077.3952171655},
                    {-91.6209716796875,	41.7118034362793,	1491,	255,	5624,	121332.41943533333,	196910.6293585931},
                    {-91.66207885742188,	41.71208190917969,	1343,	256,	5630,	61381.33904595083,	100657.95963001334},
                    {-91.99763488769531,	41.71791458129883,	135,	277,	5655,	137027.0335643621,	281129.8898766087},
                    {-91.96458435058594,	41.72346878051758,	254,	297,	5659,	112751.26141748614,	127129.85898502014},
                    {-91.68763732910156,	41.726802825927734,	1251,	309,	5669,	179818.96065493338,	318898.53387157235},
                    {-91.88568878173828,	41.727359771728516,	538,	311,	5674,	68502.60490929127,	135586.811246588},
                    {-91.71041107177734,	41.7287483215332,	1169,	316,	5685,	44953.66797807914,	76889.62303240798},
                    {-91.65013885498047,	41.72957992553711,	1386,	319,	5715,	762781.166292064,	1353538.8428410469},
                    {-91.89347076416016,	41.73596954345703,	510,	342,	5716,	59219.62846228652,	104175.55028829667},
                    {-91.76013946533203,	41.73735809326172,	990,	347,	5729,	155531.99012869722,	288027.30352877814},
                    {-91.78985595703125,	41.73763656616211,	883,	348,	5739,	34245.831523642504,	55364.340076354434},
                    {-91.89680480957031,	41.738468170166016,	498,	351,	5740,	170510.98622907195,	305375.7228982836},
                    {-91.95429992675781,	41.740970611572266,	291,	360,	5743,	100590.59058469617,	169045.75770415473},
                    {-91.97958374023438,	41.74208068847656,	200,	364,	5756,	134834.00257612136,	219760.97608977047},
                    {-91.8187484741211,	41.74291229248047,	779,	367,	5773,	211875.02541045623,	357307.508019486},
                    {-91.8731918334961,	41.746803283691406,	583,	381,	5809,	103435.17018796528,	152749.60936204987},
                    {-91.85235595703125,	41.75513458251953,	658,	411,	5848,	79172.40758366574,	122695.79044035454},
                    {-91.96652221679688,	41.761802673339844,	247,	435,	5890,	99847.41474519833,	190117.85935904965},
                    {-91.72319030761719,	41.686248779296875,	1123,	163,	5960,	242776.05766689478,	395063.57674514427},
                    {-91.90374755859375,	41.68680191040039,	473,	165,	5997,	675472.3551626642,	1099545.2224403708},
                    {-91.71041107177734,	41.710693359375,	1169,	251,	5998,	373998.34341532935,	721637.3438849947},
                    {-91.68985748291016,	41.71458053588867,	1243,	265,	6014,	267637.97672212217,	405588.4601965111},
                    {-91.9598617553711,	41.71763610839844,	271,	276,	6058,	570243.4537181602,	922023.9448750173},
                    {-91.80097198486328,	41.72541427612305,	843,	304,	6069,	330381.66436623695,	648754.4327523742},
                    {-91.8973617553711,	41.73624801635742,	496,	343,	6075,	170518.6580745533,	283234.18232532265},
                    {-91.95429992675781,	41.73652648925781,	291,	344,	6109,	452329.5287453768,	797718.9871210791},
                    {-91.71041107177734,	41.71041488647461,	1169,	250,	6112,	1017788.8778328524,	2013274.8538038046},
                    {-91.90374755859375,	41.72819137573242,	473,	314,	6267,	311098.6399522585,	201116.0783090103},

               };
            
            for (int k=0; k<ricardoFarm.length; k++)
            {
                System.out.println("Area[m^2]"+"    "+"Volume[m^3]"+"    "+"Elevation");
                float h = 0.5f;
                for (int j=0; j<20; j++)
                {
                    hydroScalingAPI.util.geomorphology.objects.DamInundation theDam=new hydroScalingAPI.util.geomorphology.objects.DamInundation((int)reservoirsData[k][2], (int)reservoirsData[k][3] , h,matDirs,dem,metaModif);
    //                hydroScalingAPI.util.geomorphology.objects.DamInundation theDam=new hydroScalingAPI.util.geomorphology.objects.DamInundation(908, 292 ,h,matDirs,dem,metaModif);

                    //Printing area and volume of the lake
                    System.out.println(theDam.getLakeArea()+"    "+theDam.getLakeVolume()+"  "+h);
    //                System.out.println(theDam.getLakeArea());
    //                System.out.println(theDam.getLakeVolume());
    //                System.out.println();
                    h=h+0.5f;
                }
                System.out.println();
                
            }
            //Prininting the x,y's of inundated area
            
            //hydroScalingAPI.util.geomorphology.objects.DamInundation theDam=new hydroScalingAPI.util.geomorphology.objects.DamInundation(2856, 846 , 9,matDirs,dem,metaModif);
//                hydroScalingAPI.util.geomorphology.objects.DamInundation theDam=new hydroScalingAPI.util.geomorphology.objects.DamInundation(908, 292 ,9,matDirs,dem,metaModif);
//            int[][] points=theDam.getXYBasin();
//            
//            for (int i = 0; i < points[0].length; i++) {
//                System.out.println(points[0][i]+";"+points[1][i]+";"+dem[points[1][i]][points[0][i]]);
//            }
         
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
    }
    

    

}
