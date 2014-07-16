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
 * linksAnalysis.java
 *
 * Created on October 14, 2001, 4:23 PM
 */

package hydroScalingAPI.util.geomorphology.objects;

/**
 * This class manages the topologic structure of the river network of a basin.  This
 * is the most important class in CUENCAS.  It is used by the Network Analysis module
 * and the Rainfall-Runoff module.
 * @author Ricardo Mantilla
 */
public class LinksAnalysis extends java.lang.Object {

    /**
     * The MetaRaster that described the DEM in which the basin is embeded
     */
    public hydroScalingAPI.io.MetaRaster localMetaRaster;
    private hydroScalingAPI.util.geomorphology.objects.Basin basin;
    private byte[][] dirMatrix;
    /**
     * The Horton-Strahler order of the river network
     */
    public int basinOrder = 0;
    /**
     * The link number corresponding to the basin outlet
     */
    public int OuletLinkNum;

    /**
     * Simulation ID for a given (x,y) coordinate
     */
    public int ressimID=0;

    /**
     * The Shreve's magnitude associated to the river network
     */
    public int basinMagnitude=0;

    /**
     * An array conaining the pixel ID where the link begins.  ID=i+j*numCols, where
     * i is the column number of the pixel and j is the row number of the pixel
     */
    public int[] headsArray;
    /**
     * An array conaining the pixel ID before the link merges with another link.  ID=i+j*numCols, where
     * i is the column number of the pixel and j is the row number of the pixel
     */
    public int[] contactsArray;
    /**
     * An array conaining the pixel ID where the link merges with another link (junction).  ID=i+j*numCols, where
     * i is the column number of the pixel and j is the row number of the pixel
     */
    public int[] tailsArray;

    /**
     * An array conaining the link's magnitude
     */
    public int[] magnitudeArray;

    /**
     * An array with the ID of the links that drain into it.  It provides the backbone
     * of the system of differetial equations that describe flow along the river
     * network
     */
    public int[][] connectionsArray; //In this array are listed all the links that drain to the i-th link
    /**
     * An array with the ID of the link to which it drains.  The oulet link has value
     * -1 indicating that it doesn't drain anywhere
     */
    public int[] nextLinkArray;
    /**
     * An array containing the indexes of the links at the bottom of complete order
     * streams
     */
    public int[] completeStreamLinksArray;

    private byte[][] basinMask;
    private int basinMinX,basinMinY,basinMaxX,basinMaxY,basinOutletID;

    /**
     * Creates new empty instance of LinksAnalysis. This constructor is only used by its
     * extension {@link hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis}
     */
    public LinksAnalysis(){
    }

    /**
     * Creates new linksAnalysis
     * @param bas The {@link hydroScalingAPI.util.geomorphology.objects.Basin} associated to this
     * network
     * @param metaR The MetaRaster that described the DEM in which the basin is embeded
     * @param DM The directions matrix associated to the DEM in which the basin is embeded
     * @throws java.io.IOException Captures errors while reading network related information
     */
    public LinksAnalysis(hydroScalingAPI.util.geomorphology.objects.Basin bas,hydroScalingAPI.io.MetaRaster metaR, byte[][] DM) throws java.io.IOException{

        localMetaRaster=metaR;
        basin=bas;
        basinMask=basin.getEncapsulatedNetworkMask();
        basinMinX=basin.getMinX();
        basinMinY=basin.getMinY();
        basinMaxX=basin.getMaxX();
        basinMaxY=basin.getMaxY();
        basinOutletID=basin.getOutletID();

        establishConnections(metaR,DM);
    }

    /**
     * Creates new linksAnalysis
     * @param bas The {@link hydroScalingAPI.util.geomorphology.objects.Basin} associated to this
     * network
     * @param metaR The MetaRaster that described the DEM in which the basin is embeded
     * @param DM The directions matrix associated to the DEM in which the basin is embeded
     * @throws java.io.IOException Captures errors while reading network related information
     */
    public LinksAnalysis(hydroScalingAPI.io.MetaRaster metaR, byte[][] DM) throws java.io.IOException{

        localMetaRaster=metaR;
        basinMask=new byte[localMetaRaster.getNumRows()][localMetaRaster.getNumCols()];
        byte elem=Byte.parseByte("1");
        for (byte[] bs : basinMask) java.util.Arrays.fill(bs, elem);

        basinMinX=1;
        basinMinY=1;
        basinMaxX=localMetaRaster.getNumCols()-2;
        basinMaxY=localMetaRaster.getNumRows()-2;
        basinOutletID=-1;
        basinMagnitude=Integer.MAX_VALUE;

        establishConnections(metaR,DM);
    }

    /**
     * Creates new linksAnalysis
     * @param bas The {@link hydroScalingAPI.util.geomorphology.objects.Basin} associated to this
     * network
     * @param metaR The MetaRaster that described the DEM in which the basin is embeded
     * @param DM The directions matrix associated to the DEM in which the basin is embeded
     * @throws java.io.IOException Captures errors while reading network related information
     */
    public void establishConnections(hydroScalingAPI.io.MetaRaster metaR, byte[][] DM) throws java.io.IOException{

        //abrir los archivos .stream . link .point

        hydroScalingAPI.io.MetaNetwork fullNetwork=new hydroScalingAPI.io.MetaNetwork(localMetaRaster);

        java.util.Vector<int[]> myMagnitude=new java.util.Vector<int[]>();
        java.util.Vector<int[]> myHeads=new java.util.Vector<int[]>();
        java.util.Vector<int[]> myContacts=new java.util.Vector<int[]>();
        java.util.Vector<int[]> myTails=new java.util.Vector<int[]>();

        for (int i=0;i<fullNetwork.getLinkRecord().length;i++){

            int posX=fullNetwork.getLinkRecord()[i][1]%localMetaRaster.getNumCols()-basinMinX;
            int posY=fullNetwork.getLinkRecord()[i][1]/localMetaRaster.getNumCols()-basinMinY;

            if (posY>=0 && posY<basinMask.length && posX>=0 && posX<basinMask[0].length){
                if (basinMask[posY][posX] != 0){

                    myMagnitude.add(new int[] {fullNetwork.getLinkRecord()[i][0]});

                    myHeads.add(new int[] {fullNetwork.getLinkRecord()[i][1]});

                    myContacts.add(new int[] {fullNetwork.getLinkRecord()[i][2]});

                    myTails.add(new int[] {fullNetwork.getLinkRecord()[i][3]});

                }
            }

        }

        magnitudeArray=new int[myTails.size()];
        headsArray=new int[myTails.size()];
        contactsArray=new int[myTails.size()];
        tailsArray=new int[myTails.size()];

        for (int i=0;i<magnitudeArray.length;i++){
            int[] thisMagn=(int[]) myMagnitude.get(i);
            magnitudeArray[i]=thisMagn[0];
            basinMagnitude=Math.max(basinMagnitude,thisMagn[0]);
        }

        for (int i=0;i<magnitudeArray.length;i++){

            int[] thisHead=(int[]) myHeads.get(i);
            int[] thisContact=(int[]) myContacts.get(i);
            int[] thisTail=(int[]) myTails.get(i);

            headsArray[i]=thisHead[0];

            if (magnitudeArray[i] != basinMagnitude){
                contactsArray[i]=thisContact[0];
                tailsArray[i]=thisTail[0];
            } else{
                tailsArray[i]=basinOutletID;
                contactsArray[i]=basinOutletID;
                OuletLinkNum=i;
            }

        }

        //Here the connection's array is created

        dirMatrix=new byte[basinMaxY-basinMinY+3][basinMaxX-basinMinX+3];
        for(int i=0;i<dirMatrix.length;i++){
            for(int j=0;j<dirMatrix[0].length;j++){
                dirMatrix[i][j]=DM[i+basinMinY-1][j+basinMinX-1];
            }
        }

        int xOulet,yOulet;
        int[][][] conectMask=new int[2][basinMaxY-basinMinY+3][basinMaxX-basinMinX+3];

        for(int i=0;i<contactsArray.length;i++){
            xOulet=headsArray[i]%localMetaRaster.getNumCols();
            yOulet=headsArray[i]/localMetaRaster.getNumCols();
            conectMask[0][yOulet-basinMinY+1][xOulet-basinMinX+1]=i+1;

            xOulet=contactsArray[i]%localMetaRaster.getNumCols();
            yOulet=contactsArray[i]/localMetaRaster.getNumCols();
            conectMask[1][yOulet-basinMinY+1][xOulet-basinMinX+1]=-i-1;
        }

        connectionsArray=new int[contactsArray.length][];
        java.util.Vector llegadores;
        for(int i=0;i<conectMask[0].length;i++){
            for(int j=0;j<conectMask[0][0].length;j++){
                if(conectMask[0][i][j]>0){
                    llegadores=new java.util.Vector();
                    for (int k=0; k <= 8; k++){
                        if (dirMatrix[i+(k/3)-1][j+(k%3)-1]==9-k && conectMask[1][i+(k/3)-1][j+(k%3)-1]<0){
                            llegadores.add(new int[] {-conectMask[1][i+(k/3)-1][j+(k%3)-1]-1});
                        }
                    }
                    connectionsArray[conectMask[0][i][j]-1]=new int[llegadores.size()];
                    for (int l=0; l <connectionsArray[conectMask[0][i][j]-1].length; l++) {
                        connectionsArray[conectMask[0][i][j]-1][l]=((int[]) llegadores.elementAt(l))[0];
                    }
                }
            }
        }

        nextLinkArray=new int[contactsArray.length];
        for(int i=0;i<conectMask[0].length;i++){
            for(int j=0;j<conectMask[0][0].length;j++){
                if(conectMask[1][i][j]<0){
                    nextLinkArray[-conectMask[1][i][j]-1]=conectMask[0][i+((dirMatrix[i][j]-1)/3)-1][j+((dirMatrix[i][j]-1)%3)-1]-1;
                }
            }
        }

        java.io.File rutaHorton=new java.io.File(localMetaRaster.getLocationBinaryFile().getPath().substring(0,localMetaRaster.getLocationBinaryFile().getPath().lastIndexOf("."))+".horton");
        java.io.RandomAccessFile fileHorton=new java.io.RandomAccessFile(rutaHorton,"r");

        java.util.Vector linksCompletos=new java.util.Vector();

        int myOrder,frontOrder;

        for(int i=0;i<nextLinkArray.length;i++){
            fileHorton.seek((long)contactsArray[i]);
            myOrder=fileHorton.readByte();
            basinOrder=Math.max(basinOrder,myOrder);
            if (nextLinkArray[i] > 0){
                fileHorton.seek((long)contactsArray[nextLinkArray[i]]);
                frontOrder=fileHorton.readByte();
                if (frontOrder>myOrder){
                    linksCompletos.addElement(new int[] {i});
                }
            } else {
                linksCompletos.addElement(new int[] {i});

            }
        }


        fileHorton.close();

        completeStreamLinksArray=new int[linksCompletos.size()];
        for(int i=0;i<linksCompletos.size();i++){
            completeStreamLinksArray[i]=((int[]) linksCompletos.get(i))[0];
        }

    }

    /**
     * Returns the network Horton-Strahler order.
     * @return An integer with the basin order
     */
    public int getBasinOrder(){
        return basinOrder;
    }

    /**
     * Returns the topologic and geometric distances of all links to the outlet link
     * @return A float[][] array. Where float[0] contains the array of topologic distances to
     * the outlet and float[1] contains the array of geometric distances to the outlet
     */
    public float[][] getDistancesToOutlet(){
        try{
            float[][] dToOutlet=new float[2][1];
            dToOutlet[0]=getVarValues(8)[0];
            dToOutlet[1]=getVarValues(7)[0];
            return dToOutlet;
        } catch (java.io.IOException IOE){
            System.err.println("Failed reading lengths for width Function");
            System.err.println(IOE);
        }

        return null;

    }

    /**
     * Returns the topologic and geometric distances of the upstream links to a
     * desired link in the basin
     * @param dToOutlet An array with distances to the basin outlet using function getDistancesToOutlet()
     * @param outlet A list of desired outlets
     * @return A float[][] array. Where float[0] contains the array of topologic distances to
     * the desired link and float[1] contains the array of geometric distances to the
     * desired link
     */
    public float[][] getDistancesToOutlet(float[][] dToOutlet,int outlet){

        java.util.Vector distToInclue=new java.util.Vector();
        distToInclue.add(new float[] {dToOutlet[0][outlet],dToOutlet[1][outlet],outlet});
        addIncoming(dToOutlet,distToInclue,outlet);


        float[][] shortGroupDists=new float[2][distToInclue.size()];

        for(int i=0;i<shortGroupDists[0].length;i++){
            float[] myDists=(float[])distToInclue.get(i);
            shortGroupDists[0][i]=myDists[0]-dToOutlet[0][outlet];
            shortGroupDists[1][i]=myDists[1]-dToOutlet[1][outlet];
        }

        return shortGroupDists;

    }
    
    /**
     * Returns the topologic and geometric distances of the upstream links to a
     * desired link in the basin
     * @param dToOutlet An array with distances to the basin outlet using function getDistancesToOutlet()
     * @param outlet A list of desired outlets
     * @return A float[][] array. Where float[0] contains the array of topologic distances to
     * the desired link and float[1] contains the array of geometric distances to the
     * desired link
     */
    public java.util.Vector getUpLinksDistancesToOutlet(float[][] dToOutlet,int outlet){

        java.util.Vector distToInclue=new java.util.Vector();
        distToInclue.add(new float[] {dToOutlet[0][outlet],dToOutlet[1][outlet],outlet});
        addIncoming(dToOutlet,distToInclue,outlet);

        return distToInclue;

    }


        /**
     * Returns the topologic and geometric distances of the upstream links to a
     * desired link in the basin
     * @param outlet A list of desired outlets
     * @return A float[][] array. Where float[0] contains the array of topologic distances to
     * the desired link and float[1] contains the array of geometric distances to the
     * desired link
     */
    public float[][] getDistancesLandUseToOutlet(int outlet,hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo){
        try{
            float[][] dToOutlet=new float[2][];
            dToOutlet[0]=getVarValues(8)[0];
            dToOutlet[1]=getVarValues(7)[0];

            java.util.Vector distToInclue=new java.util.Vector();
            distToInclue.add(new float[] {dToOutlet[0][outlet],dToOutlet[1][outlet]});
            addIncoming(dToOutlet,distToInclue,outlet);


            float[][] shortGroupDists=new float[3][distToInclue.size()];

            for(int i=0;i<shortGroupDists[0].length;i++){
                float[] myDists=(float[])distToInclue.get(i);

                shortGroupDists[0][i]=myDists[0]-dToOutlet[0][outlet];
                shortGroupDists[1][i]=myDists[1]-dToOutlet[1][outlet];
                shortGroupDists[2][i]= (float)thisHillsInfo.LandUse(i);
            }

            return shortGroupDists;
        } catch (java.io.IOException IOE){
            System.err.println("Failed reading lengths for width Function");
            System.err.println(IOE);
        }

        return null;

    }

    private void addIncoming(float[][] dToOutlet,java.util.Vector distToInclue,int outlet){
        for(int i=0;i<connectionsArray[outlet].length;i++){
            distToInclue.add(new float[] {dToOutlet[0][connectionsArray[outlet][i]],dToOutlet[1][connectionsArray[outlet][i]],connectionsArray[outlet][i]});
            addIncoming(dToOutlet,distToInclue,connectionsArray[outlet][i]);
        }
    }

    /**
     * Returns the Topologic or Geometric Width Functions above a specified group of
     * outlets
     * @param outlets The list of links where the width function is desired
     * @param metric 0 for topologic and 1 for geometric
     * @return A double[numOutlets][numBins] array with the Width Functions
     */
    public double[][] getWidthFunctions(int[] outlets,int metric){

        float binsize=1;

        if(metric != 0){
            binsize=0.3f;
        }
        return getWidthFunctions(outlets,metric,binsize);
    }

    /**
     * Returns the Topologic or Geometric Width Functions above a specified group of
     * outlets
     * @param outlets The list of links where the width function is desired
     * @param metric 0 for topologic and 1 for geometric
     * @return A double[numOutlets][numBins] array with the Width Functions
     */
    public double[][] getWidthFunctions(int[] outlets,int metric,float binsize){

        double[][] widthFunctions=new double[outlets.length][];

        int OriginalBasinOutlet=OuletLinkNum;

        float[][] bigDtoO=getDistancesToOutlet();

        for(int k=0;k<outlets.length;k++){
            if(magnitudeArray[outlets[k]] > 1){
                OuletLinkNum=outlets[k];

                float[][] wFunc=getDistancesToOutlet(bigDtoO,outlets[k]);

                hydroScalingAPI.util.statistics.Stats distStats=new hydroScalingAPI.util.statistics.Stats(wFunc[metric]);

                double[][] laWFunc=new double[1][1+(int)Math.ceil(distStats.maxValue/binsize)];
                for(int i=0;i<wFunc[metric].length;i++) laWFunc[0][(int)Math.ceil(wFunc[metric][i]/binsize)]++;

                widthFunctions[k]=laWFunc[0];
                widthFunctions[k][0]=1;
            } else {
                widthFunctions[k]=new double[] {1};
            }
        }

        OuletLinkNum=OriginalBasinOutlet;

        return widthFunctions;

    }

        /**
     * Returns the Topologic or Geometric Width Functions above a specified group of
     * outlets
     * @param outlets The list of links where the width function is desired
     * @param HillsInfo contain the land use information for each link
     * @param metric 0 for topologic and 1 for geometric
     * @return A double[numOutlets][numBins] array with the Width Functions
     */

       public double[][][] getLandUseWidthFunctions(int[] outlets,hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo HillsInfo,int metric,float binsize){

        int nclass=10;
        int lu=-9;
        // [link element number][link land use][number of elem for each binsize]
        double[][][] widthFunctions=new double[outlets.length][nclass][];

        int OriginalBasinOutlet=OuletLinkNum;

        float[][] bigDtoO=getDistancesToOutlet();

        for(int k=0;k<outlets.length;k++){
            if(magnitudeArray[outlets[k]] > 1){
                OuletLinkNum=outlets[k];

                float[][] wFunc=getDistancesToOutlet(bigDtoO,outlets[k]);
                float[][][] LUwFunc = new float[2][nclass][wFunc[metric].length];
                for(int l=0;l<wFunc[metric].length;l++){
                    lu=(int)HillsInfo.LandUse(l);
                    LUwFunc[0][lu][l]=wFunc[0][l];
                    LUwFunc[1][lu][l]=wFunc[1][l];
                }

                for(int c=0;c<nclass;c++){
                    int LU=c+1;
                hydroScalingAPI.util.statistics.Stats distStats=new hydroScalingAPI.util.statistics.Stats(LUwFunc[metric][c]);
                double[][][] laWFunc=new double[1][nclass][1+(int)Math.ceil(distStats.maxValue/binsize)];
                for(int i=0;i<LUwFunc[metric][c].length;i++) laWFunc[0][c][(int)Math.ceil(LUwFunc[metric][c][i]/binsize)]++;
                widthFunctions[k][c]=laWFunc[0][c];
                widthFunctions[k][c][0]=1;
                }
             }
             else {
                for(int c=0;c<nclass;c++) widthFunctions[k][c]=new double[] {1};
             }
       }

        OuletLinkNum=OriginalBasinOutlet;

        return widthFunctions;

    }

    public double[][][] getLandUseWidthFunctions(int[] outlets,hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo HillsInfo,int metric){

       float binsize=1;

       if(metric != 0){
           binsize=0.3f;
       }

        return getLandUseWidthFunctions(outlets,HillsInfo,metric,binsize);
    }


        /**
     * Returns the Topologic or Geometric Width Functions above a specified group of
     * outlets
     * @param outlets The list of links where the width function is desired
     * @param HillsInfo contain the land use information for each link
     * @param metric 0 for topologic and 1 for geometric
     * @return A double[numOutlets][numBins] array with the Width Functions
     */

       public double[][][] getCCWidthFunctions(int[] outlets,float [][] order,int metric,float binsize, int nclass){


        // [link element number][link land use][number of elem for each binsize]
        double[][][] widthFunctions=new double[outlets.length][nclass][];

        int OriginalBasinOutlet=OuletLinkNum;

        float[][] bigDtoO=getDistancesToOutlet();

        for(int k=0;k<outlets.length;k++){
            if(magnitudeArray[outlets[k]] > 1){
                OuletLinkNum=outlets[k];

                float[][] wFunc=getDistancesToOutlet(bigDtoO,outlets[k]);
                float[][][] OrderwFunc = new float[2][nclass][wFunc[metric].length];
                //System.out.println("outlets" + k+ " order length = " + order[0].length + "wfunclength = "+wFunc[metric].length);
                for(int l=0;l<wFunc[metric].length;l++){

                    int or=(int)order[0][l];
                    OrderwFunc[0][or][l]=wFunc[0][l];
                    OrderwFunc[1][or][l]=wFunc[1][l];
//if(OuletLinkNum==OriginalBasinOutlet) System.out.println(OuletLinkNum+" Order="+ or + " l="+l+ " topo="+wFunc[0][l]+" geom="+wFunc[1][l]);
                }

                for(int c=0;c<nclass;c++){
                    int OR=c;
                hydroScalingAPI.util.statistics.Stats distStats=new hydroScalingAPI.util.statistics.Stats(OrderwFunc[metric][c]);
                double[][][] laWFunc=new double[1][nclass][1+(int)Math.ceil(distStats.maxValue/binsize)];
                for(int i=0;i<OrderwFunc[metric][c].length;i++) laWFunc[0][c][(int)Math.ceil(OrderwFunc[metric][c][i]/binsize)]++;
                widthFunctions[k][c]=laWFunc[0][c];
                widthFunctions[k][c][0]=1;
                }
             }
             else {
                for(int c=0;c<nclass;c++) widthFunctions[k][c]=new double[] {1};
             }
       }

        OuletLinkNum=OriginalBasinOutlet;

        return widthFunctions;

    }

    public double[][][] getCCWidthFunctions(int[] outlets,float [][] order,int metric,int nclass){

       float binsize=1;

       if(metric != 0){
           binsize=0.3f;
       }

        return getCCWidthFunctions(outlets,order,metric,binsize,nclass);
    }

    /**
     * Returns and array with information about the link.  Available variables are:
     * <p>0: Link's Hillslope Area.  This is done by subtraction of area at head and area at incoming links head</p>
     * <p>1: Link's Length.  This is done by subtraction of tcl at head and tcl at incoming links head</p>
     * <p>2: Link's Upstream area.</p>
     * <p>3: Link's drop</p>
     * <p>4: Link's order</p>
     * <p>5: Total Channel Length</p>
     * <p>6: Link's Magnitude</p>
     * <p>7: Link's Distance to Outlet</p>
     * <p>8: Link's Topologic Distance to Outlet</p>
     * <p>9: Link's outlet Slope</p>
     * <p>10: Link's Elevation</p>
     * <p>11: Longest Channel Length</p>
     * <p>12: Binary Link Address</p>
     * <p>13: Total Channel Drop</p>
     * <p>14: Upstream area at links head</p>
     * @param varIndex An integer indicating the desired variable associated to the group of links
     * @return A float[1][numLinks]
     * @throws java.io.IOException Throws errors while reading information from raster files
     */
    public float[][] getVarValues(int varIndex) throws java.io.IOException {

        String[] extenciones={  ".areas",       /*0*/
                                ".ltc",         /*1*/
                                ".areas",       /*2*/
                                ".corrDEM",     /*3*/
                                ".horton",      /*4*/
                                ".ltc",         /*5*/
                                ".magn",        /*6*/
                                ".gdo",         /*7*/
                                ".tdo",         /*8*/
                                ".corrDEM",     /*9*/
                                ".corrDEM",     /*10*/
                                ".lcp",         /*11*/
                                ".magn",        /*12*/
                                ".tcd",         /*13*/
                                ".areas"        /*14*/
                                };

        float[][] quantityArray=new float[1][tailsArray.length];
       
        java.io.File rutaQuantity=new java.io.File(localMetaRaster.getLocationBinaryFile().getPath().substring(0,localMetaRaster.getLocationBinaryFile().getPath().lastIndexOf("."))+extenciones[varIndex]);
        java.io.RandomAccessFile fileQuantity=new java.io.RandomAccessFile(rutaQuantity,"r");

        switch(varIndex){
            case 0:
                //Link's Hillslope Area.  This is done by subtraction of area at head and area at incoming links head
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)contactsArray[i]);
                    
                    quantityArray[0][i]=fileQuantity.readFloat();
                    
                    float tempVar=quantityArray[0][i];
                    
                    float[] tempVar2=new float[10];
                    float pixelsize=6378.0f * (float)localMetaRaster.getResLon() * (float) Math.PI / (3600.0f * 180.0f) * 1000.f;
                    float pixelarea=pixelsize*pixelsize*1e-6f;
                    
                    for (int j=0;j<connectionsArray[i].length;j++){
                        fileQuantity.seek((long)4*(long)contactsArray[connectionsArray[i][j]]);
                        tempVar2[j]=fileQuantity.readFloat();
                        quantityArray[0][i]-=tempVar2[j];
                        
                        if(quantityArray[0][i]<=0)
                        {quantityArray[0][i]=pixelarea;}
                    
                    }
                    if (quantityArray[0][i]<0) {
                        System.out.println("outlet = " + getOutletID());
                        System.out.println("i = " + i);
                        System.out.println("Old Area = " + tempVar);
                        System.out.println("Contributors ="+java.util.Arrays.toString(tempVar2));
                        System.out.println("New Area = " + quantityArray[0][i]);
                        System.out.println("x,y = " +contactsArray[i]%localMetaRaster.getNumCols()+","+contactsArray[i]/localMetaRaster.getNumCols()); 
                        System.out.println("test = " +pixelsize); 
                        System.out.println("test = " +pixelarea); 
                    }
                }
                //System.exit(0);
   
                break;
            case 1:
                //Link's Length.  This is done by subtraction of tcl at head and tcl at incoming links head

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    for (int j=0;j<connectionsArray[i].length;j++){
                        fileQuantity.seek((long)4*(long)contactsArray[connectionsArray[i][j]]);
                        quantityArray[0][i]-=fileQuantity.readFloat();
                    }
                }

                break;
            case 2:
                //Link's Upstream area.

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                }

                break;
            case 3:
                //Link's drop

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)8*(long)headsArray[i]);
                    double altUp=fileQuantity.readDouble();
                    fileQuantity.seek((long)8*(long)tailsArray[i]);
                    double altDn=fileQuantity.readDouble();
                    if(altDn>0) quantityArray[0][i]=(float) (altUp-altDn);
                }

                break;
            case 4:
                //Link's order

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)headsArray[i]);
                    quantityArray[0][i]=(float) fileQuantity.readByte();
                }

                break;
            case 5:
                //Total Channel Length

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)contactsArray[i]);
                    quantityArray[0][i]=(float) fileQuantity.readFloat();
                }

                break;
            case 6:
                //Link's Magnitude

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readInt();
                }

                break;
            case 7:
                //Link's Distance to Outlet
                float GoToB;

                if(basin != null){
                    fileQuantity.seek((long)4*(long)basin.getOutletID());
                    GoToB=fileQuantity.readFloat();
                } else {
                    GoToB=0;
                }
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)headsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat()-GoToB;
                }

                break;
            case 8:
                //Link's Topologic Distance to Outlet
                int ToToB;

                if(basin != null){
                    fileQuantity.seek((long)4*(long)basin.getOutletID());
                    ToToB=fileQuantity.readInt();
                } else {
                    ToToB=0;
                }
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)tailsArray[i]);
                    quantityArray[0][i]=fileQuantity.readInt()-ToToB+1;
                }

                break;
            case 9:
                //Link's Slope

                float[][] linkLengths=getVarValues(1);

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)8*(long)headsArray[i]);
                    double altUp=fileQuantity.readDouble();
                    fileQuantity.seek((long)8*(long)tailsArray[i]);
                    quantityArray[0][i]=(float) (altUp-fileQuantity.readDouble());

                    quantityArray[0][i]/=linkLengths[0][i]*1000;
                }

                break;
            case 10:
                //Link's Elevation

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)8*(long)headsArray[i]);
                    quantityArray[0][i]=(float) (fileQuantity.readDouble());
                }

                break;
            case 11:
                //Longest Channel Length

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                }

                break;
            case 12:
                //Binary Link Address

                quantityArray[0]=getBLA();

                break;
            case 13:
                //Total Channel Drop

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    if(quantityArray[0][i] == 0) quantityArray[0][i]=1E-5f;
                }

                break;
            case 14:
                // Links Upstream Area at Head
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek((long)4*(long)headsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                }
                break;
        }

        fileQuantity.close();

        return quantityArray;
    }

    private float[] getBLA(){
        float[][] linkOrders=new float[0][0];
        try{
            linkOrders=getVarValues(4);
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            return null;
        }
        float[] BLAs= new float[connectionsArray.length];

        for(int i=0;i<BLAs.length;i++){
            if(magnitudeArray[i]==1){
                float currentOrder=1;
                //System.out.print(i+" : "+currentOrder+",");
                BLAs[i]+=Math.pow(2, currentOrder);
                int j=i;
                while(nextLinkArray[j] != -1){
                    if(currentOrder != linkOrders[0][nextLinkArray[j]]){
                        //System.out.print(linkOrders[0][nextLinkArray[j]]+",");
                        BLAs[i]+=Math.pow(2, linkOrders[0][nextLinkArray[j]]);
                    }
                    currentOrder=linkOrders[0][nextLinkArray[j]];
                    j=nextLinkArray[j];
                }
                //System.out.println(BLAs[i]);
            }

        }

        return BLAs;
    }

    /**
     * Returns the ID of the link in the basin outlet
     * @return An integer with the position of the outlet links in the different arrays of the
     * class
     */
    public int getOutletID(){

        return OuletLinkNum;

    }

    public int getResSimID(int x, int y){
        int contactsID = x+(y*localMetaRaster.getNumCols());
        ressimID=-1;
        for (int i=0;i<contactsArray.length;i++){
            if (contactsArray[i]==contactsID) ressimID=i+1;
            //Add 1 to ressimID because link ids in resSimul array in idl code start at 1.
        }
        return ressimID;
    }

    public int getLinkIDbyHead(int x, int y){
        int contactsID = x+(y*localMetaRaster.getNumCols());
        ressimID=-1;
        for (int i=0;i<headsArray.length;i++){
            if (headsArray[i]==contactsID) ressimID=i;
        }
        return ressimID;
    }
    
    public int getLinkIDbyContact(int x, int y){
        int contactsID = x+(y*localMetaRaster.getNumCols());
        ressimID=-1;
        for (int i=0;i<contactsArray.length;i++){
            if (contactsArray[i]==contactsID) ressimID=i;
        }
        return ressimID;
    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        //main0(args);  //An anlysis of topography
        //main1(args);
        //main2(args);
        //main3(args);
        //main5(args); // generate file for ReadParallel files
        //main4(args);
        //main6(args);  // Writing Link-IDs and connectivity for a DEM and map of hillslopes
        //main6_1(args);  // Writing connectivity for a DEM and map of hillslopes
        //main7(args);  //Writing connectivity for Clear Creek to share with The Mathematicians
        //main7_Brandon(args);  //Writing Info for Brandon (student) project
        //main8_1(args);  //Wrting connectivity and Full model parameters for The Mathematicians
        //main8_2(args);  //Wrting connectivity and Full model parameters for The Mathematicians
        //main8_3(args);  //Wrting connectivity and Full model parameters for The Mathematicians
        //main8_Rodica(args);
        //main8(args);  //Writing connectivity for Cedar River at Cedar Rapids to share with The Mathematicians
        //main9(args);  // Writing connectivity for Cedar River at Cedar Rapids (30 m DEM) to share with The Mathematicians
        //main_MODLU(args); //link-ids
        //main10(args);  // Writing connectivity for Equation (Evaluation by Walter)
        //main11(args);  // Writing connectivity for Scott's code directly (2 files .rvr and .prm)
        //main12(args);  // Writing connectivity for Chi's code (Clear Creek Case)
        //main12_1(args);  // Writing connectivity for Chi's code (Squaw Creek Case)
        //main12_2(args);  // Writing connectivity for Chi's code (East Nishnabotna at Atlantic Case)
        //main12_3(args);  // Writing connectivity for Chi's code (Indian Creek Case)
        //main12_4(args);  // Writing connectivity for Chi's code (Middle Raccoon Case)
        //main12_5(args);  // Writing connectivity for Chi's code (Walnut Creek Case)
        //mainScott_Type50(args);
        //IowaAllLinks(args);
        //mainScott_Type31Geral(args);
        //mainScott_Type30(args);
        //mainScott_Type31(args);
        mainTibebuIowaDEM(args);

    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void mainTibebuIowaDEM(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/HucTiledIowa/fullIowaDem.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/HucTiledIowa/fullIowaDem.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(23426,2735,matDirs,metaModif);

            LinksAnalysis linksStructure=new LinksAnalysis(laCuenca, metaModif, matDirs);

            theFile=new java.io.File("/Users/ricardo/temp/IowaRiverBasin.wfs.csv");
            System.out.println("Writing Width Functions - "+theFile);
            java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
            java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
            java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

            double[][] wfs=linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray,0);

            for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
                if(linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]] != 1){
                    newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+",");
                    double maxWF=0.0;
                    for (int j=0;j<wfs[i].length;j++) maxWF=Math.max(maxWF,wfs[i][j]);
                    newfile.write(maxWF+"\n");
                }

            }
            
            int outletID=linksStructure.getOutletID();
            
            for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
                if(linksStructure.completeStreamLinksArray[i] == outletID){
                    newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+",");
                    for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+",");
                    newfile.write("\n");
                }

            }

            newfile.close();
            bufferout.close();
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main0(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            //hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(3083,1688,matDirs,metaModif);
            //hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(2256,3499,matDirs,metaModif);
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(2220,4238,matDirs,metaModif);

            LinksAnalysis myResults=new LinksAnalysis(laCuenca, metaModif, matDirs);

            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(myResults);

            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/0.3_ArcSecUSGS/89883214.dem"));
            metaModif.restoreOriginalFormat();
            float[][] myDEM=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();

            int[] countersOrderLink=new int[myResults.getBasinOrder()];

            for (int i=0;i<myResults.completeStreamLinksArray.length;i++){
                int LinkOrder=(int)thisNetworkGeom.linkOrder(myResults.completeStreamLinksArray[i]);
                countersOrderLink[LinkOrder-1]++;
                if(LinkOrder > 1){
                    int xContact=myResults.contactsArray[myResults.completeStreamLinksArray[i]]%metaModif.getNumCols();
                    int yContact=myResults.contactsArray[myResults.completeStreamLinksArray[i]]/metaModif.getNumCols();

                    double resultX=(xContact+0.5)*metaModif.getResLon()/3600.0+metaModif.getMinLon();
                    double resultY=(yContact+0.5)*metaModif.getResLat()/3600.0+metaModif.getMinLat();

                    System.out.println(LinkOrder+dpoint2.format(new Double((double)countersOrderLink[LinkOrder-1]/10000.0)).substring(2,6)+"B\t"+resultY+"\t"+resultX+"\t"+myDEM[yContact][xContact]);

                    int xHead=myResults.headsArray[myResults.completeStreamLinksArray[i]]%metaModif.getNumCols();
                    int yHead=myResults.headsArray[myResults.completeStreamLinksArray[i]]/metaModif.getNumCols();

                    resultX=(xHead+0.5)*metaModif.getResLon()/3600.0+metaModif.getMinLon();
                    resultY=(yHead+0.5)*metaModif.getResLat()/3600.0+metaModif.getMinLat();

                    System.out.println(LinkOrder+dpoint2.format(new Double((double)countersOrderLink[LinkOrder-1]/10000.0)).substring(2,6)+"T\t"+resultY+"\t"+resultX+"\t"+myDEM[yHead][xHead]);
                }
            }
            /*System.out.println("Connection Structure");
            for (int i=0;i<myResults.headsArray.length;i++){
                System.out.print("Links connected to Link # "+i+" are: ");
                for (int j=0;j<myResults.connectionsArray[i].length;j++){
                    System.out.print(myResults.connectionsArray[i][j]+",");
                }
                System.out.println();
            }*/

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main1(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(1064, 494,matDirs,metaModif);

            LinksAnalysis myResults=new LinksAnalysis(laCuenca, metaModif, matDirs);

            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(myResults);

            float[][] orders=myResults.getVarValues(4);
            int first5=0,first4=0;
            for (int i = 0; i < orders[0].length; i++) {
                System.out.print(orders[0][i]+",");
                if(first5==0 && orders[0][i] > 5) first5=i;
                if(first4==0 && orders[0][i] > 4) first4=i;

            }
            System.out.println();

            float[][] dists=myResults.getDistancesToOutlet();
            for (int i = 0; i < dists[0].length; i++) {
                System.out.print(dists[0][i]+",");
            }
            System.out.println();

            double[][] wfs=myResults.getWidthFunctions(new int[]{myResults.getOutletID(),first5,first4},1);
            //double[][] wfs=myResults.getWidthFunctions(new int[]{first4},1);
            for (int i = 0; i < wfs.length; i++) {
                for (int j = 0; j < wfs[i].length; j++) {
                    System.out.print(wfs[i][j]+",");
                }
                System.out.println();
            }

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main2(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/hidrosigDataBases/Upper Rio Puerco DB/Rasters/Topography/1_ArcSec/NED_54212683.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Upper Rio Puerco DB/Rasters/Topography/1_ArcSec/NED_54212683.dir"));

            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(381,221,matDirs,metaModif);

            LinksAnalysis myResults=new LinksAnalysis(laCuenca, metaModif, matDirs);

            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(myResults);

            double[][] wfs=myResults.getWidthFunctions(new int[]{myResults.getOutletID()},1);
            System.out.println("Distance to Outlet, Number of Links");
            for (int i = 0; i < wfs.length; i++) {
                for (int j = 0; j < wfs[i].length; j++) {
                    System.out.println((j*0.3)+","+wfs[i][j]);
                }
            }

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main3(String args[]) {

        String[][] codesCoord={
            {"05451210","951","1479"},
            {"05451500","1245","1181"},
            {"05451700","1312","1112"},
            {"05451900","1765","981"},
            {"05452200","1871","903"},
            {"05453000","2115","801"},
            {"05453100","2256","876"},
            {"05453520","2900","768"},
            {"05454000","2949","741"},
            {"05454220","2646","762"},
            {"05454300","2817","713"},
            {"05454500","2885","690"},
            {"05455100","2796","629"},
            {"05455500","2676","465"},
            {"05455700","2958","410"},
            {"05457000","1164","3066"},
            {"05457700","1526","2376"},
            {"05458000","1730","2341"},
            {"05458300","1770","1987"},
            {"05458500","1775","1879"},
            {"05458900","1682","1858"},
            {"05459500","903","2499"},
            {"05462000","1634","1956"},
            {"05463000","1590","1789"},
            {"05463500","1779","1591"},
            {"05464000","1932","1695"},
            {"05464220","1978","1403"},
            {"05464500","2734","1069"},
            {"05464942","3113","705"},
            {"05465000","3186","392"},
            {"05465500","3316","116"}};


        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/hidrosigDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            for (int c = 0; c < codesCoord.length; c++) {
                int xx = Integer.parseInt(codesCoord[c][1]);
                int yy = Integer.parseInt(codesCoord[c][2]);

                hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(xx,yy,matDirs,metaModif);

                LinksAnalysis myResults=new LinksAnalysis(laCuenca, metaModif, matDirs);

                double[][] wfs=myResults.getWidthFunctions(new int[]{myResults.getOutletID()},0);
                for (int i = 0; i < wfs.length; i++) {
                    hydroScalingAPI.util.statistics.Stats myStats=new hydroScalingAPI.util.statistics.Stats(wfs[i]);
                    System.out.println(codesCoord[c][0]+","+myStats.maxValue);
                }

            }



        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main4(String args[]) {

        int x=1570; int y=127;

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);

            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int demNumCols=metaModif.getNumCols();

            for (int i=0;i<mylinksAnalysis.contactsArray.length;i++){
                if (mylinksAnalysis.magnitudeArray[i] < mylinksAnalysis.basinMagnitude){

                    xOulet=mylinksAnalysis.contactsArray[i]%demNumCols;
                    yOulet=mylinksAnalysis.contactsArray[i]/demNumCols;

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;

                    }
                } else {
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;
                    }
                }
            }

            String fileBinSalida="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/masks/NED_00159011_BasinHills.vhc";
            java.io.File outputBinaryFile=new java.io.File(fileBinSalida);
            java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));

            int nRows=matrizPintada.length;
            int nCols=matrizPintada[0].length;


            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    rasterBuffer.writeInt(matrizPintada[i][j]);
                }
            }

            rasterBuffer.close();

            String outputMetaFile="/Users/ricardo/workFiles/myWorkingStuff/Code/IDL_Sources/IowaFloods2008/NextLinkClearCreek.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write(mylinksAnalysis.nextLinkArray[i]+"\n");

            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write("C");
                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
                    metaBuffer.write(","+mylinksAnalysis.connectionsArray[i][j]);
                metaBuffer.write("\n");
            }

            metaBuffer.close();

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

/**
     * Tests for the class
     * @param args the command line arguments
     */
public static void main5(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{
   
       // int[] Res = {90, 60, 30, 20, 10, 5,-9,-10,-11,12,-13};
        //int[] XX = {447, 670, 1341, 2013, 4025, 8052,-9,-10,-11,12,-13};
       // int[] YY = {27, 41, 82, 122, 244, 497,-9,-10,-11,12,-13};
       String[] AllSimName = {
        "10000DEMUSGS","nldaDEMUSGS","90DEMUSGS","120DEMUSGS","30DEMUSGS","180DEMUSGS","250DEMUSGS","500DEMUSGS",
   "1000DEMUSGSCA","2000DEMUSGSCA","5000DEMUSGSCA","10000DEMUSGSCA","nldaDEMUSGSCA","90DEMUSGSCA","120DEMUSGSCA","30DEMUSGSCA","180DEMUSGSCA","250DEMUSGSCA","500DEMUSGSCA"};
   //    "90DEMUSGS","90DEMASTER","90DEMSRTM","90DEMUSGSPrun8","90DEMUSGSPrun7",
   //"90DEMUSGSPrun6","90DEMUSGSPrun5","120DEMUSGS","150DEMUSGS","180DEMUSGS",
   //"1000DEMUSGS","2000DEMUSGS","5000DEMUSGS",
       String[] AllRain = {"3CedarRapids"};
   int nsim = AllSimName.length;

        int nbas = AllRain.length;
              for (int is = 0; is < nsim; is++) {
                for (int ib = 0; ib < nbas; ib++) {

                    System.out.println("Running BASIN " + AllSimName[is]);
                    System.out.println("Running BASIN " + AllRain[ib]);

                    String SimName = AllSimName[is];
                    String BasinName = AllRain[ib];
     
                    java.io.File DEMFile;
             
                    String[] StringDEM = {"error", "error", "error"};
                    StringDEM = hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ParallelSimulationToFileHelium.defineDEMxy(BasinName, SimName);
                    System.out.println("StringDEM = " + StringDEM[0]);
                    System.out.println("x = " + StringDEM[1] + "    y" + StringDEM[2]);
                    String DEM = StringDEM[0];
                    int xOut = Integer.parseInt(StringDEM[1]);//   .getInteger(StringDEM[1]);
                    int yOut = Integer.parseInt(StringDEM[2]);

               DEMFile=new java.io.File(DEM);
            int x = xOut;
            int y = yOut;
                
//
      

            hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(DEMFile);
            metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0, DEMFile.getPath().lastIndexOf(".")) + ".dir"));
            metaModif.setFormat("Byte");
            byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0, DEMFile.getPath().lastIndexOf(".")) + ".magn"));
            metaModif.setFormat("Integer");
            int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            metaModif.setLocationBinaryFile(new java.io.File(DEMFile.getPath().substring(0, DEMFile.getPath().lastIndexOf(".")) + ".horton"));
            metaModif.setFormat("Byte");
            byte[][] horOrders = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

                
       System.out.println(DEMFile.toString());
       //DEMFile=new java.io.File("/Groups/IFC//CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun7/AveragedIowaRiverAtColumbusJunctions.metaDEM");
System.out.println("X  " +x  + "   Y  " +  y);
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
             
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);

            java.io.File theFile1=new java.io.File("/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/linksAnalyses/"+"linksInfo" + x +"_" + y+".csv");
            java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile1);
            java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
            java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
            
            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(mylinksAnalysis);

        float[][] bigDtoO=mylinksAnalysis.getDistancesToOutlet();

        int nc=metaModif.getNumCols();
        int nr=metaModif.getNumRows();
        double res=metaModif.getResLat();
        double minLat=metaModif.getMinLat();
        double minLon=metaModif.getMinLon();
        System.out.println("mylinksAverage area = " + thisNetworkGeom.basinArea()/mylinksAnalysis.contactsArray.length);
        System.out.println("nc = " + nc + "nr = " + nr + "minLat = " + minLat + "minLon = " + minLon);
        for (int i=0;i<mylinksAnalysis.contactsArray.length;i++){
                newfile.write("Link-"+i+",");
                newfile.write(mylinksAnalysis.contactsArray[i]+",");
                newfile.write(thisNetworkGeom.linkOrder(i)+",");
                newfile.write(thisNetworkGeom.upStreamArea(i)+",");
                newfile.write(thisNetworkGeom.Length(i)+",");
                int xCon=mylinksAnalysis.contactsArray[i]%nc;
                int yCon=mylinksAnalysis.contactsArray[i]/nc;
                newfile.write(bigDtoO[1][i]+",");
                newfile.write(xCon+",");
                newfile.write(yCon+",");
                double lat=minLat+yCon*res/3600.0;
                double lon=minLon+xCon*res/3600.0;
                newfile.write(lat+",");
                newfile.write(lon+",");
                newfile.write(nc+",");
                newfile.write(nr+",");
                newfile.write(res+"\n");
        }
        newfile.close();
        bufferout.close();
        
        }
              }        
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    /**
     * Tests for the class
     * @param args the command line arguments
     */



    public static void mainScott_Type50(String args[]) {

        int x = 1504;
        int y = 67;
        double vh = 0.1;
        double vr = 0.55;

//Delaware trenton
        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat) number2;
        dpoint2.applyPattern("0.00000000");
        float[] RCAr = {0.66f, 0.55f, 0.6f, 1.0f};
        float[] VRCoefAr = {0.01f, 0.1f};

        for (float RC : RCAr) {
            for (float VRCoef : VRCoefAr) {

                try {

                    //java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.metaDEM");
                    java.io.File theFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3xPr6/delusgs3arc.metaDEM");

                    hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
                    metaModif.setLocationBinaryFile(new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3xPr6/delusgs3arc.dir"));

                    metaModif.setFormat("Byte");
                    byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

                    metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
                    metaModif.setFormat("Integer");
                    int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

                    hydroScalingAPI.util.geomorphology.objects.Basin laCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDirs, metaModif);

                    LinksAnalysis mylinksAnalysis = new LinksAnalysis(laCuenca, metaModif, matDirs);
                    hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(mylinksAnalysis);
                    hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(mylinksAnalysis);
                    String LandUse = "null";
                    String SoilData = "null";
                    String SoilHydData = "null";
                    String Soil150SWAData = "null";

                    java.io.File LandUseFile = new java.io.File(LandUse);
                    java.io.File SoilFile = new java.io.File(SoilData);
                    java.io.File SoilHydFile = new java.io.File(SoilHydData);
                    java.io.File SwaFileFile = new java.io.File(Soil150SWAData);

                    hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
                    java.io.File DEMFile = metaModif.getLocationMeta();

                    SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile, SoilHydFile, SwaFileFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes, 0);
                    thisHillsInfo.setSCSManager(SCSObj);

                    float[][] upAreas = mylinksAnalysis.getVarValues(2);
                    float[][] areas = mylinksAnalysis.getVarValues(0);
                    float[][] lenghts = mylinksAnalysis.getVarValues(1);
                    java.io.File outputDirectory = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/");
                    outputDirectory.mkdirs();
                    String outputMetaFile = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del" + x + "_" + y + ".rvr";

                    java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
                    metaBuffer.write("\n");
                    for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                        metaBuffer.write("" + i + "\n");
                        metaBuffer.write("" + mylinksAnalysis.connectionsArray[i].length);

                        for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                            metaBuffer.write(" " + mylinksAnalysis.connectionsArray[i][j]);
                        }

                        metaBuffer.write("\n");
                        metaBuffer.write("\n");

                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();

                    outputMetaFile = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del_50_" + x + "_" + y + "_" + RC + "VR" + vr + "CR" + VRCoef + ".prm";
                    System.out.printf(outputMetaFile);
//L_i,A_h,A_i,h_b,h_H,max_inf_rate,K_sat,S_h,eta,b_H,c_H,d_H,invtau,epsilon,c_1,c_2,c_3,c_4,c_5,c_6
                    metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
                    metaBuffer.write("\n");

                    for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                        double vrf = vr;
                        // H==> Hydro-power     
                        if (i == 1403 || i == 1404 || i == 1450 || i == 1456 || i == 1039 || i == 1081 || i == 261) {
                            vrf = 10 * VRCoef * vr;
                        }
                        // R==> Recreation      
                        if (i == 1792 || i == 1023 || i == 46) {
                            vrf = 10 * VRCoef * vr;
                        }
                        // S==> Water Supply   
                        if (i == 1880 || i == 1766 || i == 1664 || i == 1468 || i == 359 || i == 457) {
                            vrf = 10 * VRCoef * vr;
                        }
                        // water bodies - lakes
                        if (i == 1501 || i == 1425 || i == 1369 || i == 1311 || i == 1330 || i == 1328 || i == 1491 || i == 1314 || i == 1118 || i == 1112 || i == 979 || i == 963 || i == 956 || i == 871 || i == 1051 || i == 1000 || i == 789 || i == 789) {
                            vrf = 10 * VRCoef * vr;
                        }
                        // water bodies - swamps or marsh
                        if (i == 722 || i == 480 || i == 440 || i == 618 || i == 618 || i == 889 || i == 866 || i == 771) {
                            vrf = 5 * VRCoef * vr;
                        }
                        // C==>Flood Control
                        if (i == 708 || i == 618 || i == 789 || i == 457 || i == 359 || i == 1410 || i == 1433 || i == 911 || i == 913 || i == 872 || i == 737 || i == 298 || i == 417 || i == 335 || i == 461 || i == 261) {
                            vrf = VRCoef * vr;
                        }

                        metaBuffer.write(i + "\n");
                        metaBuffer.write(upAreas[0][i] + " " + lenghts[0][i] + " " + areas[0][i] + " ");
                        metaBuffer.write(RC + " " + "0.4" + " " + thisNetworkGeom.Slope(i) + " " + vrf + "\n");
                        // OLD  thisHillsInfo.getHillslope(i)
                        metaBuffer.write("\n");
                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();

                    outputMetaFile = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del_50_" + x + "_" + y + "_" + RC + "VR" + vr + "NoDam.prm";
                    //L_i,A_h,A_i,h_b,h_H,max_inf_rate,K_sat,S_h,eta,b_H,c_H,d_H,invtau,epsilon,c_1,c_2,c_3,c_4,c_5,c_6
                    metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
                    metaBuffer.write("\n");

                    for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                        double vrf = vr;
                        // H==> Hydro-power     

                        metaBuffer.write(i + "\n");
                        metaBuffer.write(upAreas[0][i] + " " + lenghts[0][i] + " " + areas[0][i] + " ");
                        metaBuffer.write(RC + " " + "0.4" + " " + thisNetworkGeom.Slope(i) + " " + vrf + "\n");
                        // OLD  thisHillsInfo.getHillslope(i)
                        metaBuffer.write("\n");
                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();
                    outputMetaFile = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del_" + x + "_" + y + ".listComplete";
                    metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    float[][] hortonOrders = mylinksAnalysis.getVarValues(4);
                    System.out.println("mylinksAnalysis.contactsArray.length = " + mylinksAnalysis.contactsArray.length + " mylinksAnalysis.nextLinkArray.length " + mylinksAnalysis.nextLinkArray.length);
                    // metaBuffer.write("" + mylinksAnalysis.contactsArray.length + "\n");
                    // metaBuffer.write("\n");

                    for (int i = 0; i < mylinksAnalysis.contactsArray.length; i++) {

                        if (mylinksAnalysis.nextLinkArray[i] > 0) {

                            metaBuffer.write(i + "," + mylinksAnalysis.contactsArray[i] + "," + hortonOrders[0][i] + "," + mylinksAnalysis.contactsArray[mylinksAnalysis.nextLinkArray[i]] + "," + upAreas[0][i] + "," + lenghts[0][i] + "," + areas[0][i]);
                        } else {
                            metaBuffer.write(i + "," + mylinksAnalysis.contactsArray[i] + "," + hortonOrders[0][i] + " " + "-1" + "," + upAreas[0][i] + "," + lenghts[0][i] + "," + areas[0][i]);
                        }

                        for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                            metaBuffer.write("," + mylinksAnalysis.contactsArray[mylinksAnalysis.connectionsArray[i][j]]);
                        }
                        metaBuffer.write("\n");
                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();
                    outputMetaFile = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del_" + x + "_" + y + ".sav";
                    metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    //metaBuffer.write(""+mylinksAnalysis.completeStreamLinksArray.length+"\n");
                    //metaBuffer.write("\n");
                    for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                        metaBuffer.write(i + "\n");
                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();

                } catch (java.io.IOException IOE) {
                    System.out.print(IOE);
                    System.exit(0);
                }
            }
        }
        System.exit(0);

    }

    public static void mainScott_Type30(String args[]) {

        int flag = 2;
        //flag=1; Iowa 
        //flag=2; Delaware
        int x = 2949;
        int y = 741;// Basin Code 05464500 Cedar River at Cedar Rapids, IA
        if (flag == 2) {
            x = 2949;
            y = 741;
        }
        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat) number2;
        dpoint2.applyPattern("0.00000000");

        try {

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
            java.io.File theFile = new java.io.File("D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
            metaModif.setFormat("Byte");
            byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
            metaModif.setFormat("Integer");
            int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDirs, metaModif);

            LinksAnalysis mylinksAnalysis = new LinksAnalysis(laCuenca, metaModif, matDirs);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(mylinksAnalysis);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(mylinksAnalysis);

            String LandUse = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            String SoilData = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
            String SoilHydData = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            String Soil150SWAData = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";

            java.io.File LandUseFile = new java.io.File(LandUse);
            java.io.File SoilFile = new java.io.File(SoilData);
            java.io.File SoilHydFile = new java.io.File(SoilHydData);
            java.io.File SwaFileFile = new java.io.File(Soil150SWAData);

            hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
            java.io.File DEMFile = metaModif.getLocationMeta();

            SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile, SoilHydFile, SwaFileFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes, 0);

            thisHillsInfo.setSCSManager(SCSObj);

            float[][] upAreas = mylinksAnalysis.getVarValues(2);
            float[][] areas = mylinksAnalysis.getVarValues(0);
            float[][] lenghts = mylinksAnalysis.getVarValues(1);
            float[][] hb = new float[1][mylinksAnalysis.connectionsArray.length]; // mmfloat 
            float[][] MaxInf = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Hh = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] HydCond = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Terms = new float[4][mylinksAnalysis.connectionsArray.length];
            float[][] Slope = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Manning = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Area_Relief_Param = new float[4][mylinksAnalysis.connectionsArray.length]; //Area in km and depth in m

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                hb[0][i] = (float) thisHillsInfo.SWA150(i);

                if (hb[0][i] < 0) {
                    System.out.println("hb " + hb[0][i] + " thisHillsInfo.SCS_S1(i) " + thisHillsInfo.SWA150(i));
                    System.exit(1);
                }
                MaxInf[0][i] = (float) thisHillsInfo.MaxInfRate(i);
                Hh[0][i] = (float) thisHillsInfo.HillRelief(i) * 1000;
                //System.out.println("hb " + hb[0][i] + " thisHillsInfo.SCS_S1(i) " + thisHillsInfo.SCS_S1(i));

                HydCond[0][i] = (float) thisHillsInfo.AveHydCond(i);
                //System.out.println("i" + i);
                Area_Relief_Param[0][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 0);
                Area_Relief_Param[1][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 1);
                Area_Relief_Param[2][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 2);
                Area_Relief_Param[3][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 3);
                if (Hh[0][i] == 0) {
                    Hh[0][i] = 10;
                    Area_Relief_Param[0][i] = 0;
                    Area_Relief_Param[1][i] = 1;
                    Area_Relief_Param[2][i] = 0;
                    Area_Relief_Param[3][i] = 0;
                }

                Slope[0][i] = (float) thisNetworkGeom.Slope(i);
                Manning[0][i] = (float) thisHillsInfo.HillManning(i);
            }

            java.io.File outputDirectory = new java.io.File("/scratch/dataScott/");
            outputDirectory.mkdirs();
            String outputMetaFile = "D:/usr/Iowa/CUENCASCheck/Princeton/InputFilesTest/Iowa30_" + x + "_" + y + ".rvr";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                metaBuffer.write("" + mylinksAnalysis.connectionsArray[i].length);

                for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                    metaBuffer.write(" " + mylinksAnalysis.connectionsArray[i][j]);
                }

                metaBuffer.write("\n");
                metaBuffer.write("\n");

            }
            metaBuffer.write("\n");

            metaBuffer.close();
            outputMetaFile = "D:/usr/Iowa/CUENCASCheck/Princeton/InputFilesTest/Iowa30_" + x + "_" + y + ".prm";
            System.out.printf(outputMetaFile);
//L_i,A_h,A_i,h_b,h_H,max_inf_rate,K_sat,S_h,eta,b_H,c_H,d_H,invtau,epsilon,c_1,c_2,c_3,c_4,c_5,c_6
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
            metaBuffer.write("\n");

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {

                metaBuffer.write("" + i + "\n");
                metaBuffer.write(lenghts[0][i] + " " + areas[0][i] + " " + upAreas[0][i]);
                metaBuffer.write(" " + hb[0][i] + " " + Hh[0][i] + " " + MaxInf[0][i]);
                metaBuffer.write(" " + HydCond[0][i] + " " + Slope[0][i] + " " + Manning[0][i]);
                metaBuffer.write(" " + Area_Relief_Param[1][i] + " " + Area_Relief_Param[2][i] + " " + Area_Relief_Param[3][i]);
                metaBuffer.write("\n");
                //System.out.println("hb " + hb[0][i] + " Hh " + Hh[0][i] + " MaxInf " + MaxInf[0][i]);
// OLD  thisHillsInfo.getHillslope(i)
                metaBuffer.write("\n");
            }
            metaBuffer.write("\n");

            metaBuffer.close();

            outputMetaFile = "D:/usr/Iowa/CUENCASCheck/Princeton/InputFilesTest/Iowa30_" + x + "_" + y + ".listComplete";

            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            float[][] hortonOrders = mylinksAnalysis.getVarValues(4);
            System.out.println("mylinksAnalysis.contactsArray.length = " + mylinksAnalysis.contactsArray.length + " mylinksAnalysis.nextLinkArray.length " + mylinksAnalysis.nextLinkArray.length);
            // metaBuffer.write("" + mylinksAnalysis.contactsArray.length + "\n");
            // metaBuffer.write("\n");

            for (int i = 0; i < mylinksAnalysis.contactsArray.length; i++) {

                if (mylinksAnalysis.nextLinkArray[i] > 0) {

                    metaBuffer.write(i + "," + mylinksAnalysis.contactsArray[i] + "," + hortonOrders[0][i] + "," + mylinksAnalysis.contactsArray[mylinksAnalysis.nextLinkArray[i]] + "," + upAreas[0][i] + "," + lenghts[0][i] + "," + areas[0][i]);
                } else {
                    metaBuffer.write(i + "," + mylinksAnalysis.contactsArray[i] + "," + hortonOrders[0][i] + " " + "-1" + "," + upAreas[0][i] + "," + lenghts[0][i] + "," + areas[0][i]);
                }

                for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                    metaBuffer.write("," + mylinksAnalysis.contactsArray[mylinksAnalysis.connectionsArray[i][j]]);
                }
                metaBuffer.write("\n");
            }
            metaBuffer.write("\n");

            metaBuffer.close();
            outputMetaFile = "D:/usr/Iowa/CUENCASCheck/Princeton/InputFilesTest/Iowa30_" + x + "_" + y + ".sav";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write(i + "\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();

        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void mainScott_Type31(String args[]) {

        int flag = 2;
        //flag=1; Iowa 
        //flag=2; Delaware
        int x = 2949;// Iowa Test 
        int y = 741;// Iowa Test
        if (flag == 2) {
            x = 1504;
            y = 67;
        }

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat) number2;
        dpoint2.applyPattern("0.00000000");

        try {

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
            java.io.File theFile = new java.io.File("D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");

            // hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
            // metaModif.setLocationBinaryFile(new java.io.File("D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
            //java.io.File theFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3xPr6/delusgs3arc.metaDEM");
            if (flag == 2) {
                theFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3xPr6/delusgs3arc.metaDEM");
            }
            hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setFormat("Byte");
            if (flag == 1) {
                metaModif.setLocationBinaryFile(new java.io.File("D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
            } else {
                metaModif.setFormat("Byte");
                metaModif.setLocationBinaryFile(new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3xPr6/delusgs3arc.dir"));
            }

            byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            System.out.println("theFile " + new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
            System.out.println("x  " + x + " y " + y);

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
            metaModif.setFormat("Integer");
            int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDirs, metaModif);

            LinksAnalysis mylinksAnalysis = new LinksAnalysis(laCuenca, metaModif, matDirs);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(mylinksAnalysis);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(mylinksAnalysis);

            String LandUse = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            String SoilData = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
            String SoilHydData = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            String Soil150SWAData = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
            if (flag == 2) {

                LandUse = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/landcover90.metaVHC";
                SoilData = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/hydrogroup.metaVHC";
                SoilHydData = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/hydcond.metaVHC";
                Soil150SWAData = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/aws150mm.metaVHC";

            }

            java.io.File LandUseFile = new java.io.File(LandUse);
            java.io.File SoilFile = new java.io.File(SoilData);
            java.io.File SoilHydFile = new java.io.File(SoilHydData);
            java.io.File SwaFileFile = new java.io.File(Soil150SWAData);

            hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
            java.io.File DEMFile = metaModif.getLocationMeta();

            SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile, SoilHydFile, SwaFileFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes, 0);

            thisHillsInfo.setSCSManager(SCSObj);
            /////////////////////// Initial condition for 2006 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

            float[][] upAreas = mylinksAnalysis.getVarValues(2);
            float[][] areas = mylinksAnalysis.getVarValues(0);
            float[][] lenghts = mylinksAnalysis.getVarValues(1);
            float[][] hb = new float[1][mylinksAnalysis.connectionsArray.length]; // mmfloat 
            float[][] MaxInf = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Hh = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] HydCond = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Terms = new float[4][mylinksAnalysis.connectionsArray.length];
            float[][] Slope = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Manning = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Area_Relief_Param = new float[4][mylinksAnalysis.connectionsArray.length]; //Area in km and depth in m

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                hb[0][i] = (float) thisHillsInfo.SWA150(i);

                if (hb[0][i] < 0) {
                    System.out.println("hb " + hb[0][i] + " thisHillsInfo.SCS_S1(i) " + thisHillsInfo.SWA150(i));
                    System.exit(1);
                }
                MaxInf[0][i] = (float) thisHillsInfo.MaxInfRate(i);
                Hh[0][i] = (float) thisHillsInfo.HillRelief(i) * 1000;
                //System.out.println("hb " + hb[0][i] + " thisHillsInfo.SCS_S1(i) " + thisHillsInfo.SCS_S1(i));

                HydCond[0][i] = (float) thisHillsInfo.AveHydCond(i);
                //System.out.println("i" + i);
                Area_Relief_Param[0][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 0);
                Area_Relief_Param[1][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 1);
                Area_Relief_Param[2][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 2);
                Area_Relief_Param[3][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 3);
                if (Hh[0][i] == 0) {
                    Hh[0][i] = 10;
                    Area_Relief_Param[0][i] = 0;
                    Area_Relief_Param[1][i] = 1;
                    Area_Relief_Param[2][i] = 0;
                    Area_Relief_Param[3][i] = 0;
                }

                Slope[0][i] = (float) thisNetworkGeom.Slope(i);
                Manning[0][i] = (float) thisHillsInfo.HillManning(i);
            }

            java.io.File outputDirectory = new java.io.File("/scratch/dataScott/");
            outputDirectory.mkdirs();
            String Dir = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/";

            String outputMetaFile = "D:/usr/Iowa/CUENCASCheck/Princeton/InputFilesTest/Iowa31_" + x + "_" + y + ".rvr";
            if (flag == 2) {
                outputMetaFile = Dir + "Del_31_" + x + "_" + y + ".rvr";
            }
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                metaBuffer.write("" + mylinksAnalysis.connectionsArray[i].length);

                for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                    metaBuffer.write(" " + mylinksAnalysis.connectionsArray[i][j]);
                }

                metaBuffer.write("\n");
                metaBuffer.write("\n");

            }
            metaBuffer.write("\n");

            metaBuffer.close();
            outputMetaFile = "D:/usr/Iowa/CUENCASCheck/Princeton/InputFilesTest/Iowa31_NoDam" + x + "_" + y + ".prm";
            if (flag == 2) {
                outputMetaFile = Dir + "/Del_31NoDam" + x + "_" + y + ".prm";
            }
            System.out.printf(outputMetaFile);
//L_i,A_h,A_i,h_b,h_H,max_inf_rate,K_sat,S_h,eta,b_H,c_H,d_H,invtau,epsilon,c_1,c_2,c_3,c_4,c_5,c_6
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
            metaBuffer.write("\n");

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                float vrf = 1f;
                metaBuffer.write("" + i + "\n");
                metaBuffer.write(lenghts[0][i] + " " + areas[0][i] + " " + upAreas[0][i]);
                metaBuffer.write(" " + hb[0][i] + " " + Hh[0][i] + " " + MaxInf[0][i]);

                double finalSlope = Math.max(0.001, Slope[0][i]);
                metaBuffer.write(" " + HydCond[0][i] + " " + finalSlope + " " + Manning[0][i]);
                metaBuffer.write(" " + Area_Relief_Param[1][i] + " " + Area_Relief_Param[2][i] + " " + Area_Relief_Param[3][i]);
                metaBuffer.write(" " + vrf);
                metaBuffer.write("\n");
                System.out.println("Parameters hill relief" + Area_Relief_Param[0][i] + " " + Area_Relief_Param[1][i] + " " + Area_Relief_Param[2][i] + " " + Area_Relief_Param[3][i]);
                System.out.println("hb " + hb[0][i] + " Hh " + Hh[0][i] + " MaxInf " + MaxInf[0][i]);
// OLD  thisHillsInfo.getHillslope(i)
                metaBuffer.write("\n");
            }
            metaBuffer.write("\n");

            metaBuffer.close();

            outputMetaFile = "D:/usr/Iowa/CUENCASCheck/Princeton/InputFilesTest/Iowa31_" + x + "_" + y + ".prm";
            if (flag == 2) {
                outputMetaFile = Dir + "/Del_31WDam" + x + "_" + y + ".prm";
            }
            System.out.printf(outputMetaFile);
//L_i,A_h,A_i,h_b,h_H,max_inf_rate,K_sat,S_h,eta,b_H,c_H,d_H,invtau,epsilon,c_1,c_2,c_3,c_4,c_5,c_6
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
            metaBuffer.write("\n");

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {

                metaBuffer.write("" + i + "\n");
                metaBuffer.write(lenghts[0][i] + " " + areas[0][i] + " " + upAreas[0][i]);
                metaBuffer.write(" " + hb[0][i] + " " + Hh[0][i] + " " + MaxInf[0][i]);
                double finalSlope = Math.max(0.0001, Slope[0][i]);
                metaBuffer.write(" " + HydCond[0][i] + " " + finalSlope + " " + Manning[0][i]);
                metaBuffer.write(" " + Area_Relief_Param[1][i] + " " + Area_Relief_Param[2][i] + " " + Area_Relief_Param[3][i]);
                float vrf = 1f;
                if (flag == 2) {
                    // H==> Hydro-power     

                    if (i == 1403 || i == 1404 || i == 1450 || i == 1456 || i == 1039 || i == 1081 || i == 261) {
                        vrf = 10 * 0.01f;
                    }
                    // R==> Recreation      
                    if (i == 1792 || i == 1023 || i == 46) {
                        vrf = 10 * 0.01f;
                    }
                    // S==> Water Supply   
                    if (i == 1880 || i == 1766 || i == 1664 || i == 1468 || i == 359 || i == 457) {
                        vrf = 10 * 0.01f;
                    }
                    // water bodies - lakes
                    if (i == 1501 || i == 1425 || i == 1369 || i == 1311 || i == 1330 || i == 1328 || i == 1491 || i == 1314 || i == 1118 || i == 1112 || i == 979 || i == 963 || i == 956 || i == 871 || i == 1051 || i == 1000 || i == 789 || i == 789) {
                        vrf = 10 * 0.01f;
                    }
                    // water bodies - swamps or marsh
                    if (i == 722 || i == 480 || i == 440 || i == 618 || i == 618 || i == 889 || i == 866 || i == 771) {
                        vrf = 5 * 0.01f;
                    }
                    // C==>Flood Control
                    if (i == 708 || i == 618 || i == 789 || i == 457 || i == 359 || i == 1410 || i == 1433 || i == 911 || i == 913 || i == 872 || i == 737 || i == 298 || i == 417 || i == 335 || i == 461 || i == 261) {
                        vrf = 0.01f;
                    }
                }

                metaBuffer.write(" " + vrf);

                metaBuffer.write("\n");
                //System.out.println("hb " + hb[0][i] + " Hh " + Hh[0][i] + " MaxInf " + MaxInf[0][i]);
// OLD  thisHillsInfo.getHillslope(i)
                metaBuffer.write("\n");
            }
            metaBuffer.write("\n");

            metaBuffer.close();

            outputMetaFile = Dir + "/Del_31_" + x + "_" + y + ".listComplete";

            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            float[][] hortonOrders = mylinksAnalysis.getVarValues(4);
            System.out.println("mylinksAnalysis.contactsArray.length = " + mylinksAnalysis.contactsArray.length + " mylinksAnalysis.nextLinkArray.length " + mylinksAnalysis.nextLinkArray.length);
            // metaBuffer.write("" + mylinksAnalysis.contactsArray.length + "\n");
            // metaBuffer.write("\n");

            for (int i = 0; i < mylinksAnalysis.contactsArray.length; i++) {

                if (mylinksAnalysis.nextLinkArray[i] > 0) {

                    metaBuffer.write(i + "," + mylinksAnalysis.contactsArray[i] + "," + hortonOrders[0][i] + "," + mylinksAnalysis.contactsArray[mylinksAnalysis.nextLinkArray[i]] + "," + upAreas[0][i] + "," + lenghts[0][i] + "," + areas[0][i]);
                } else {
                    metaBuffer.write(i + "," + mylinksAnalysis.contactsArray[i] + "," + hortonOrders[0][i] + " " + "-1" + "," + upAreas[0][i] + "," + lenghts[0][i] + "," + areas[0][i]);
                }
                metaBuffer.write("," + mylinksAnalysis.connectionsArray[i].length);
                for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                    metaBuffer.write("," + mylinksAnalysis.contactsArray[mylinksAnalysis.connectionsArray[i][j]]);
                }
                metaBuffer.write("\n");
            }
            metaBuffer.write("\n");

            metaBuffer.close();
            outputMetaFile = Dir + "/Del_31_" + x + "_" + y + ".sav";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write(i + "\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();

            java.io.File SOILMFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/NLDAS/MSTAV0-200_VHC/2006/NLDAS2-MSTAV0-200.metavhc");
            hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager SOILM;
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(SOILMFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes);
            if (!SOILM.isCompleted()) {
                System.out.println("Problem with SOILM");
                return;
            }
            thisHillsInfo.setSoilMoistureManager(SOILM);

            outputMetaFile = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del_31_2006_" + x + "_" + y + ".ini";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            metaBuffer.write("31\n");
            metaBuffer.write(mylinksAnalysis.contactsArray.length + "\n");
            metaBuffer.write("0 \n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                double currTime = SOILM.SOILM040InitialTimeInMinutes();
                double INIC = thisHillsInfo.SoilMoisture(i, currTime);
                if (INIC < 0) {
                    System.out.println("check the current time of the file\n");
                    System.exit(1);
                }

                //System.out.println(1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, 0) + "\n");
                //System.out.println("i=" + i + " "+ 1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                metaBuffer.write("1e-6" + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                //metaBuffer.write("\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();

            SOILMFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/NLDAS/MSTAV0-200_VHC/2004/NLDAS2-MSTAV0-200.metavhc");
            //      hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager SOILM;
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(SOILMFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes);
            if (!SOILM.isCompleted()) {
                System.out.println("Problem with SOILM");
                return;
            }
            thisHillsInfo.setSoilMoistureManager(SOILM);

            outputMetaFile = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del_31_2004_" + x + "_" + y + ".ini";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            metaBuffer.write("31\n");
            metaBuffer.write(mylinksAnalysis.contactsArray.length + "\n");
            metaBuffer.write("0 \n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                double currTime = SOILM.SOILM040InitialTimeInMinutes();
                double INIC = thisHillsInfo.SoilMoisture(i, currTime);
                if (INIC < 0) {
                    System.out.println("check the current time of the file\n");
                    System.exit(1);
                }

                //System.out.println(1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, 0) + "\n");
                //System.out.println("i=" + i + " "+ 1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                metaBuffer.write("1e-6" + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                //metaBuffer.write("\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();

            SOILMFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/NLDAS/MSTAV0-200_VHC/2005/NLDAS2-MSTAV0-200.metavhc");
            //      hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager SOILM;
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(SOILMFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes);
            if (!SOILM.isCompleted()) {
                System.out.println("Problem with SOILM");
                return;
            }
            thisHillsInfo.setSoilMoistureManager(SOILM);

            outputMetaFile = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del_31_2005_" + x + "_" + y + ".ini";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            // metaBuffer.write("30\n");
            metaBuffer.write("31\n");
            metaBuffer.write(mylinksAnalysis.contactsArray.length + "\n");
            metaBuffer.write("0 \n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                double currTime = SOILM.SOILM040InitialTimeInMinutes();
                double INIC = thisHillsInfo.SoilMoisture(i, currTime);
                if (INIC < 0) {
                    System.out.println("check the current time of the file\n");
                    System.exit(1);
                }

                //System.out.println(1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, 0) + "\n");
                //System.out.println("i=" + i + " "+ 1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                metaBuffer.write("1e-6" + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                //metaBuffer.write("\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();

            SOILMFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/NLDAS/MSTAV0-200_VHC/2011/NLDAS2-MSTAV0-200.metavhc");
            //      hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager SOILM;
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(SOILMFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes);
            if (!SOILM.isCompleted()) {
                System.out.println("Problem with SOILM");
                return;
            }
            thisHillsInfo.setSoilMoistureManager(SOILM);

            outputMetaFile = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del_31_2011_" + x + "_" + y + ".ini";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            metaBuffer.write("31\n");
            metaBuffer.write(mylinksAnalysis.contactsArray.length + "\n");
            metaBuffer.write("0 \n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                double currTime = SOILM.SOILM040InitialTimeInMinutes();
                double INIC = thisHillsInfo.SoilMoisture(i, currTime);
                if (INIC < 0) {
                    System.out.println("check the current time of the file\n");
                    System.exit(1);
                }

                //System.out.println(1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, 0) + "\n");
                //System.out.println("i=" + i + " "+ 1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                metaBuffer.write("1e-6" + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                //metaBuffer.write("\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();
            //Dir = "D:/usr/ArcGisLayers/Haiti/Rasters/Topography/";
            String Output = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/Del_31_2011" + "/MPHShed" + x + "_" + y + "_";

            double[][] DEM = new hydroScalingAPI.io.DataRaster(metaModif).getDouble();
            byte[][] HortonLinks = new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada Matrix;
            Matrix = new hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada(Output, laCuenca, mylinksAnalysis, metaModif, matDirs, HortonLinks, magnitudes, DEM);

            
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void mainScott_Type31Geral(String args[]) {

        int x = 5533;// Turkey Outlet 
        int y = 2577;// Turkey Outlet
        //int x = 4764;// Turkey Spillville
        //int y = 2996;// Turkey Spillville
        String Directory = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/";
        String DEMName = "res";
        String LandUse = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
        String SoilData = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
        String SoilHydData = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
        String Soil150SWAData = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
        String DirOutput = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/ScottCodeFiles/InputFiles/Turkey/";
        String FileOutput = "TurkeySpillville" + x + "_" + y;
        FileOutput = "TurkeyOutlet" + x + "_" + y;
        String Soil = "null";

        int flag = 6; // 
        // 1 for Iowa 
        // 2 for Delaware Flag Prunning 6 
        // 3 for Delaware Flag Prunning 8;
        // 4 for Delaware Flag Prunning 8;
        // 5 for Delaware Flag Prunning 9;
        // 6 test with null;
       
    
        if(flag>=2) {
            x = 1504;// 
            y = 67;// 
            LandUse = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/Soils90mFinal/landcover90.metaVHC";
            SoilData = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/Soils90mFinal/hydrogroup.metaVHC";
           //SoilHydData = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/hydcond.metaVHC";
            SoilHydData = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/Soils90mFinal/DelawareKsatmmh.metaVHC";
            Soil150SWAData = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/Soils90mFinal/DelawareHB150mm90m.metaVHC";
            DirOutput = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/";
            Soil = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/NLDAS/MSTAV0-200_VHC/2011/NLDAS2-MSTAV0-200.metavhc";
        }
         if (flag == 2) {// Delaware 
            Directory = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3xPr6/";
            DEMName = "delusgs3arc";
            FileOutput = "DelOrder6" + x + "_" + y;
        }
         
              if (flag == 3) {// Delaware 
            Directory = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3xPr7/";
            DEMName = "delusgs3arc";
            FileOutput = "DelOrder7" + x + "_" + y;
        }
                    if (flag == 4) {// Delaware 
            Directory = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3xPr8/";
            DEMName = "delusgs3arc";
            FileOutput = "DelOrder8" + x + "_" + y;
        }
            if (flag == 5) {// Delaware 
            Directory = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3x/";
            DEMName = "delusgs3arc";
            FileOutput = "DelOrder9" + x + "_" + y;
        }
            if (flag == 6) {// Delaware 
           
            Directory = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Topography/USGS/Usgs3xPr6/";
            DEMName = "delusgs3arc";
            FileOutput = "DelOrder6" + x + "_" + y;
            LandUse = "null";
            SoilData = "null";
           //SoilHydData = "D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/hydcond.metaVHC";
            SoilHydData = "null";
            Soil150SWAData = "null";
            DirOutput = "D:/usr/Delaware_luciana/CUENCASDelaware/ScottCodeFiles/InputFiles/";
            Soil = "null";
        }
        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat) number2;
        dpoint2.applyPattern("0.00000000");

        try {

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
            java.io.File theFile = new java.io.File(Directory + DEMName + ".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(Directory + DEMName + ".dir"));
            metaModif.setFormat("Byte");
            byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(Directory + DEMName + ".magn"));
            metaModif.setFormat("Integer");
            int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDirs, metaModif);

            LinksAnalysis mylinksAnalysis = new LinksAnalysis(laCuenca, metaModif, matDirs);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(mylinksAnalysis);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(mylinksAnalysis);

            java.io.File LandUseFile = new java.io.File(LandUse);
            java.io.File SoilFile = new java.io.File(SoilData);
            java.io.File SoilHydFile = new java.io.File(SoilHydData);
            java.io.File SwaFileFile = new java.io.File(Soil150SWAData);

            hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
            java.io.File DEMFile = metaModif.getLocationMeta();

           
            SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile, SoilHydFile, SwaFileFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes, 0);

            thisHillsInfo.setSCSManager(SCSObj);
            //System.exit(1);
            /////////////////////// Initial condition for 2006 %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%

            float[][] upAreas = mylinksAnalysis.getVarValues(2);
            float[][] areas = mylinksAnalysis.getVarValues(0);
            float[][] lenghts = mylinksAnalysis.getVarValues(1);
            float[][] hb = new float[1][mylinksAnalysis.connectionsArray.length]; // mmfloat 
            float[][] MaxInf = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Hh = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] HydCond = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Terms = new float[4][mylinksAnalysis.connectionsArray.length];
            float[][] Slope = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Manning = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Area_Relief_Param = new float[4][mylinksAnalysis.connectionsArray.length]; //Area in km and depth in m
            float[][] SlopeLand1 = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] SlopeLand2 = new float[1][mylinksAnalysis.connectionsArray.length];

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                hb[0][i] = (float) thisHillsInfo.SWA150(i);

                if (hb[0][i] < 0) {
                    System.out.println("hb " + hb[0][i] + " thisHillsInfo.SCS_S1(i) " + thisHillsInfo.SWA150(i));
                    System.exit(1);
                }
                MaxInf[0][i] = (float) thisHillsInfo.MaxInfRate(i);
                Hh[0][i] = (float) thisHillsInfo.HillRelief(i) * 1000;
                //System.out.println("hb " + hb[0][i] + " thisHillsInfo.SCS_S1(i) " + thisHillsInfo.SCS_S1(i));

                HydCond[0][i] = (float) thisHillsInfo.AveHydCond(i);
                if (flag == 1) // because before I was multiplying by 0.0036 and not 0.001
                // Now I require that HydCond is in mm/h
                {
                    HydCond[0][i] = 3.6f * (float) thisHillsInfo.AveHydCond(i);
                }
                //System.out.println("i" + i);
                Area_Relief_Param[0][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 0);
                Area_Relief_Param[1][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 1);
                Area_Relief_Param[2][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 2);
                Area_Relief_Param[3][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 3);
                if (Hh[0][i] == 0) {
                    Hh[0][i] = 10;
                    Area_Relief_Param[0][i] = 0;
                    Area_Relief_Param[1][i] = 1;
                    Area_Relief_Param[2][i] = 0;
                    Area_Relief_Param[3][i] = 0;
                }

                Slope[0][i] = (float) thisNetworkGeom.Slope(i);
                Manning[0][i] = (float) thisHillsInfo.HillManning(i);
                SlopeLand1[0][i] = (float) thisHillsInfo.getHillslope(i);
                SlopeLand2[0][i] = (float) thisHillsInfo.getHillslope2(i);
            }

            java.io.File outputDirectory = new java.io.File(DirOutput);
            outputDirectory.mkdirs();

            String outputMetaFile = DirOutput + FileOutput + ".rvr";

            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                metaBuffer.write("" + mylinksAnalysis.connectionsArray[i].length);

                for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                    metaBuffer.write(" " + mylinksAnalysis.connectionsArray[i][j]);
                }

                metaBuffer.write("\n");
                metaBuffer.write("\n");

            }
            metaBuffer.write("\n");

            metaBuffer.close();
            outputMetaFile = DirOutput + FileOutput + "NoDam.prm";

            System.out.printf(outputMetaFile);
//L_i,A_h,A_i,h_b,h_H,max_inf_rate,K_sat,S_h,eta,b_H,c_H,d_H,invtau,epsilon,c_1,c_2,c_3,c_4,c_5,c_6
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
            metaBuffer.write("\n");

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                float vrf = 1f;
                metaBuffer.write("" + i + "\n");
                metaBuffer.write(lenghts[0][i] + " " + areas[0][i] + " " + upAreas[0][i]);
                metaBuffer.write(" " + hb[0][i] + " " + Hh[0][i] + " " + MaxInf[0][i]);
                System.out.println("Slope  " + Slope[0][i] + "  SlopeLand1 " + SlopeLand1[0][i] + "  SlopeLand2 " + SlopeLand2[0][i]);
                double finalSlope = Math.max(0.001, SlopeLand2[0][i]);
                metaBuffer.write(" " + HydCond[0][i] + " " + finalSlope + " " + Manning[0][i]);
                metaBuffer.write(" " + 1.0 + " " + 0.0 + " " + 0.0);
                metaBuffer.write("\n");
                metaBuffer.write(" " + vrf);
                System.out.println("Parameters hill relief" + Area_Relief_Param[0][i] + " " + Area_Relief_Param[1][i] + " " + Area_Relief_Param[2][i] + " " + Area_Relief_Param[3][i] + "\n");
                System.out.println("hb " + hb[0][i] + " Hh " + Hh[0][i] + " MaxInf " + MaxInf[0][i]);
// OLD  thisHillsInfo.getHillslope(i)
                metaBuffer.write("\n");
            }

            metaBuffer.write("\n");

            metaBuffer.close();

            outputMetaFile = DirOutput + FileOutput + "WithDam.prm";

            System.out.printf(outputMetaFile);
//L_i,A_h,A_i,h_b,h_H,max_inf_rate,K_sat,S_h,eta,b_H,c_H,d_H,invtau,epsilon,c_1,c_2,c_3,c_4,c_5,c_6
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
            metaBuffer.write("\n");

            

            String outputMetaFile2 = DirOutput + FileOutput + "reservoir.csv";

            java.io.BufferedWriter metaBuffer2 = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile2));
            metaBuffer2.write("iD,RelInd,upAreas,vrf,Lat,Long\n");
          
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                int RelInd = mylinksAnalysis.contactsArray[i];
                metaBuffer.write("" + i + "\n");
                metaBuffer.write(lenghts[0][i] + " " + areas[0][i] + " " + upAreas[0][i]);
                metaBuffer.write(" " + hb[0][i] + " " + Hh[0][i] + " " + MaxInf[0][i]);
                double finalSlope =Math.max(0.001, SlopeLand2[0][i]);
                metaBuffer.write(" " + HydCond[0][i] + " " + finalSlope + " " + Manning[0][i]);
                metaBuffer.write(" " + Area_Relief_Param[1][i] + " " + Area_Relief_Param[2][i] + " " + Area_Relief_Param[3][i]);
                float vrf = 1f;
                if (flag >= 2) {
                     int X = mylinksAnalysis.contactsArray[i] % metaModif.getNumCols();
                        int Y = mylinksAnalysis.contactsArray[i] / metaModif.getNumCols();
                        double Long = metaModif.getMinLon() + X * metaModif.getResLat() / 3600.;
                        double Lat = metaModif.getMinLat() + Y * metaModif.getResLat() / 3600.;
                    // H==> Hydro-power     
                    if (RelInd == 3588876 || RelInd == 3459314 || RelInd == 3459316 || RelInd == 3214573 || RelInd == 3236695 || RelInd == 3203834 ) {
                        //if (i == 1403 || i == 1404 || i == 1450 || i == 1456 || i == 1039 || i == 1081 || i == 261) {
                         
                        vrf = 10 * 0.01f;
                        metaBuffer2.write(i + "," + RelInd + "," + upAreas[0][i] + ","+vrf+ ","+Lat+","+Long+"\n");
                    }
                    // R==> Recreation 
                    if (RelInd == 4956011 || RelInd == 3078540) {
                        //if (i == 1792 || i == 1023 || i == 46) {
                        vrf = 10 * 0.01f;
                        metaBuffer2.write(i + "," + RelInd + "," + upAreas[0][i] + ","+vrf+ ","+Lat+","+Long+"\n");
                    }
                    // S==> Water Supply   
                    if (RelInd == 4086880 || RelInd == 1726077 || RelInd == 1759289) {
                        //if ( i == 1766 || i == 1664 || i == 1468 || i == 359 || i == 457) {
                        vrf = 10 * 0.01f;
                        metaBuffer2.write(i + "," + RelInd + "," + upAreas[0][i] + ","+vrf+ ","+Lat+","+Long+"\n");
                    }
                    // water bodies - lakes
                    if (RelInd == 3841910 || RelInd == 3765760 || RelInd == 3506690 || RelInd == 3451084 || RelInd == 3410201 || RelInd == 3303262  || RelInd == 3522375 || RelInd == 3174851
                            || RelInd == 3110906 || RelInd == 2849918 || RelInd == 2699924 || RelInd == 2628022 || RelInd == 2615675 || RelInd == 3128013 || RelInd == 2909933 ) {
                        //if (i == 1501 || i == 1425 || i == 1369 || i == 1311 || i == 1330 || i == 1328 || i == 1491 || i == 1314 || i == 1118 || i == 1112 || i == 979 || i == 963 || i == 956 || i == 871 || i == 1051 || i == 1000 || i == 789 || i == 789) {
                        vrf = 10 * 0.01f;
                        metaBuffer2.write(i + "," + RelInd + "," + upAreas[0][i] + ","+vrf+ ","+Lat+","+Long+"\n");
                        
                    }
                    // water bodies - swamps or marsh
                    if (  RelInd == 4711808 || RelInd == 2271467 || RelInd == 2198291 || RelInd == 1863152   || RelInd == 3565702 || RelInd == 2520579 || RelInd == 2664513 || RelInd == 2349805) {
                        // if (i == 1881 || i == 722 || i == 480 || i == 440 || i == 618 || i == 618 || i == 889 || i == 866 || i == 771 || i == 1410) {
                        vrf = 5 * 0.01f;
                       metaBuffer2.write(i + "," + RelInd + "," + upAreas[0][i] + ","+vrf+ ","+Lat+","+Long+"\n");
                    }
                    // C==>Flood Control
                    if ( RelInd == 4723635|| RelInd == 1764204  || RelInd == 2422634 || RelInd == 1759289 || RelInd == 1726077  || RelInd == 2689334 || RelInd == 2547499
                            || RelInd == 2533107 || RelInd == 2316518 || RelInd == 1635713 || RelInd == 1683091 || RelInd == 1618065 || RelInd == 1840563 || RelInd == 1276451) {
                        //  if (i == 708 || i == 618 || i == 789 || i == 457 || i == 359  || i == 1434 || i == 911 || i == 913 || i == 872 || i == 737 || i == 298 || i == 417 || i == 335 || i == 461 || i == 261) {
                        vrf = 0.01f;
                        metaBuffer2.write(i + "," + RelInd + "," + upAreas[0][i] + ","+vrf+ ","+Lat+","+Long+"\n");
                    }
                    // Reservoirs that were removed because they do not have any effect
                    if(RelInd == 3503922 || RelInd == 2218083 || RelInd == 692198 || RelInd == 3999836|| RelInd == 3718152 )
                    {
                          vrf = 1f;
                         metaBuffer2.write(i + "," + RelInd + "," + upAreas[0][i] + ","+vrf+ ","+Lat+","+Long+"\n");
                    }
                }

                metaBuffer.write(" " + vrf);

                metaBuffer.write("\n");
                //System.out.println("hb " + hb[0][i] + " Hh " + Hh[0][i] + " MaxInf " + MaxInf[0][i]);
// OLD  thisHillsInfo.getHillslope(i)
                metaBuffer.write("\n");
            }
            metaBuffer.write("\n");

            metaBuffer.close();
            metaBuffer2.close();
            System.out.println("Start to write the file for checking \n");
            outputMetaFile = DirOutput + FileOutput + "ForChecking.csv";

            System.out.printf(outputMetaFile);
//L_i,A_h,A_i,h_b,h_H,max_inf_rate,K_sat,S_h,eta,b_H,c_H,d_H,invtau,epsilon,c_1,c_2,c_3,c_4,c_5,c_6
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
            metaBuffer.write("\n");
            metaBuffer.write("ID,LEngth,area,UpsArea,hb,Hh,MAxInf,HydCond,Slope,SlopeLand1,SlopeLand2,Manning,b,c,d,vrf\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                int RelInd = mylinksAnalysis.contactsArray[i];
                metaBuffer.write("" + i + ",");
                metaBuffer.write(lenghts[0][i] + "," + areas[0][i] + "," + upAreas[0][i]);
                metaBuffer.write("," + hb[0][i] + "," + Hh[0][i] + "," + MaxInf[0][i]);

                metaBuffer.write("," + HydCond[0][i] + "," + Slope[0][i] + "," + SlopeLand1[0][i] + "," + SlopeLand2[0][i] + "," + Manning[0][i]);
                metaBuffer.write("," + Area_Relief_Param[1][i] + "," + Area_Relief_Param[2][i] + "," + Area_Relief_Param[3][i]);
                float vrf = 1f;
                  if (flag >= 2) {
                     int X = mylinksAnalysis.contactsArray[i] % metaModif.getNumCols();
                        int Y = mylinksAnalysis.contactsArray[i] / metaModif.getNumCols();
                        double Long = metaModif.getMinLon() + X * metaModif.getResLat() / 3600.;
                        double Lat = metaModif.getMinLat() + Y * metaModif.getResLat() / 3600.;
                    // H==> Hydro-power     
                    if (RelInd == 3588876 || RelInd == 3459314 || RelInd == 3459316 || RelInd == 3214573 || RelInd == 3236695 || RelInd == 3203834 ) {
                        //if (i == 1403 || i == 1404 || i == 1450 || i == 1456 || i == 1039 || i == 1081 || i == 261) {
                         
                        vrf = 10 * 0.01f;
                    }
                    // R==> Recreation 
                    if (RelInd == 4956011 || RelInd == 3078540) {
                        //if (i == 1792 || i == 1023 || i == 46) {
                        vrf = 10 * 0.01f;
                    }
                    // S==> Water Supply   
                    if (RelInd == 4086880 || RelInd == 1726077 || RelInd == 1759289) {
                        //if ( i == 1766 || i == 1664 || i == 1468 || i == 359 || i == 457) {
                        vrf = 10 * 0.01f;
                     }
                    // water bodies - lakes
                    if (RelInd == 3841910 || RelInd == 3765760 || RelInd == 3506690 || RelInd == 3451084 || RelInd == 3410201 || RelInd == 3303262  || RelInd == 3522375 || RelInd == 3174851
                            || RelInd == 3110906 || RelInd == 2849918 || RelInd == 2699924 || RelInd == 2628022 || RelInd == 2615675 || RelInd == 3128013 || RelInd == 2909933 ) {
                        //if (i == 1501 || i == 1425 || i == 1369 || i == 1311 || i == 1330 || i == 1328 || i == 1491 || i == 1314 || i == 1118 || i == 1112 || i == 979 || i == 963 || i == 956 || i == 871 || i == 1051 || i == 1000 || i == 789 || i == 789) {
                        vrf = 10 * 0.01f;
                     }
                    // water bodies - swamps or marsh
                    if (  RelInd == 4711808 || RelInd == 2271467 || RelInd == 2198291 || RelInd == 1863152   || RelInd == 3565702 || RelInd == 2520579 || RelInd == 2664513 || RelInd == 2349805) {
                        // if (i == 1881 || i == 722 || i == 480 || i == 440 || i == 618 || i == 618 || i == 889 || i == 866 || i == 771 || i == 1410) {
                        vrf = 5 * 0.01f;
                     }
                    // C==>Flood Control
                    if ( RelInd == 4723635|| RelInd == 1764204  || RelInd == 2422634 || RelInd == 1759289 || RelInd == 1726077  || RelInd == 2689334 || RelInd == 2547499
                            || RelInd == 2533107 || RelInd == 2316518 || RelInd == 1635713 || RelInd == 1683091 || RelInd == 1618065 || RelInd == 1840563 || RelInd == 1276451) {
                        //  if (i == 708 || i == 618 || i == 789 || i == 457 || i == 359  || i == 1434 || i == 911 || i == 913 || i == 872 || i == 737 || i == 298 || i == 417 || i == 335 || i == 461 || i == 261) {
                        vrf = 0.01f;
                       }
                    // Reservoirs that were removed because they do not have any effect
                    if(RelInd == 3503922 || RelInd == 2218083 || RelInd == 692198 || RelInd == 3999836|| RelInd == 3718152 )
                    {
                          vrf = 1f;
                           }
                }


                metaBuffer.write("," + vrf);

                metaBuffer.write("\n");
                //System.out.println("hb " + hb[0][i] + " Hh " + Hh[0][i] + " MaxInf " + MaxInf[0][i]);
// OLD  thisHillsInfo.getHillslope(i)
            }
            metaBuffer.write("\n");

            metaBuffer.close();

            outputMetaFile = DirOutput + FileOutput + ".listComplete";

            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            float[][] hortonOrders = mylinksAnalysis.getVarValues(4);
            System.out.println("mylinksAnalysis.contactsArray.length = " + mylinksAnalysis.contactsArray.length + " mylinksAnalysis.nextLinkArray.length " + mylinksAnalysis.nextLinkArray.length);
            // metaBuffer.write("" + mylinksAnalysis.contactsArray.length + "\n");
            // metaBuffer.write("\n");

            for (int i = 0; i < mylinksAnalysis.contactsArray.length; i++) {
                 int X = mylinksAnalysis.contactsArray[i] % metaModif.getNumCols();
                        int Y = mylinksAnalysis.contactsArray[i] / metaModif.getNumCols();
                        double Long = metaModif.getMinLon() + X * metaModif.getResLat() / 3600.;
                        double Lat = metaModif.getMinLat() + Y * metaModif.getResLat() / 3600.;
                        
                if (mylinksAnalysis.nextLinkArray[i] > 0) {

                    metaBuffer.write(i + "," + mylinksAnalysis.contactsArray[i] + "," + hortonOrders[0][i] + "," + Lat + "," + Long +  "," + mylinksAnalysis.contactsArray[mylinksAnalysis.nextLinkArray[i]] + "," + upAreas[0][i] + "," + lenghts[0][i] + "," + areas[0][i]);
                } else {
                    metaBuffer.write(i + "," + mylinksAnalysis.contactsArray[i] + "," + hortonOrders[0][i] + "," + Lat + "," + Long + "-1" + "," + upAreas[0][i] + "," + lenghts[0][i] + "," + areas[0][i]);
                }
                metaBuffer.write("," + mylinksAnalysis.connectionsArray[i].length);
                for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                    metaBuffer.write("," + mylinksAnalysis.contactsArray[mylinksAnalysis.connectionsArray[i][j]]);
                }
                metaBuffer.write("\n");
            }
            metaBuffer.write("\n");

            metaBuffer.close();
            outputMetaFile = DirOutput + FileOutput + ".sav";

            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write(i + "\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();
           
            

            //Dir = "D:/usr/ArcGisLayers/Haiti/Rasters/Topography/";
            String Output = DirOutput + FileOutput + "MP";
            metaModif.setLocationBinaryFile(new java.io.File(Directory + DEMName + ".horton"));
            metaModif.setFormat("Byte");
            byte[][] HortonLinks = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setFormat("Double");
            //metaModif.setLocationBinaryFile(new java.io.File(pathinput + "AveragedIowaRiverAtColumbusJunctions" + ".corrDEM"));
            metaModif.setLocationBinaryFile(new java.io.File(Directory + DEMName + ".corrDEM"));

            double[][] DEM = new hydroScalingAPI.io.DataRaster(metaModif).getDouble();
            hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada Matrix;
            Matrix = new hydroScalingAPI.modules.rainfallRunoffModel.objects.Gen_MatrixPintada(Output, laCuenca, mylinksAnalysis, metaModif, matDirs, HortonLinks, magnitudes, DEM);
            
            if(flag>=2) {
                  java.io.File SOILMFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/NLDAS/MSTAV0-200_VHC/2006/NLDAS2-MSTAV0-200.metavhc");
            hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager SOILM;
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(SOILMFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes);
            if (!SOILM.isCompleted()) {
                System.out.println("Problem with SOILM");
                return;
            }
            thisHillsInfo.setSoilMoistureManager(SOILM);

            outputMetaFile = DirOutput + FileOutput + "2006.ini";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            metaBuffer.write("31\n");
            metaBuffer.write(mylinksAnalysis.contactsArray.length + "\n");
            metaBuffer.write("0 \n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                double currTime = SOILM.SOILM040InitialTimeInMinutes();
                double INIC = thisHillsInfo.SoilMoisture(i, currTime);
                if (INIC < 0) {
                    System.out.println("check the current time of the file\n");
                    System.exit(1);
                }

                //System.out.println(1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, 0) + "\n");
                //System.out.println("i=" + i + " "+ 1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                double inidisch=0.08*Math.pow(upAreas[0][i],0.87);
                metaBuffer.write(inidisch + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                //metaBuffer.write("\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();

            SOILMFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/NLDAS/MSTAV0-200_VHC/2004/NLDAS2-MSTAV0-200.metavhc");
            //      hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager SOILM;
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(SOILMFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes);
            if (!SOILM.isCompleted()) {
                System.out.println("Problem with SOILM");
                return;
            }
            thisHillsInfo.setSoilMoistureManager(SOILM);
            outputMetaFile = DirOutput + FileOutput + "2004.ini";
           
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            metaBuffer.write("31\n");
            metaBuffer.write(mylinksAnalysis.contactsArray.length + "\n");
            metaBuffer.write("0 \n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                double currTime = SOILM.SOILM040InitialTimeInMinutes();
                double INIC = thisHillsInfo.SoilMoisture(i, currTime);
                if (INIC < 0) {
                    System.out.println("check the current time of the file\n");
                    System.exit(1);
                }

                //System.out.println(1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, 0) + "\n");
                //System.out.println("i=" + i + " "+ 1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                double inidisch=0.08*Math.pow(upAreas[0][i],0.87);
                metaBuffer.write(inidisch + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                //metaBuffer.write("\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();

            SOILMFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/NLDAS/MSTAV0-200_VHC/2005/NLDAS2-MSTAV0-200.metavhc");
            //      hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager SOILM;
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(SOILMFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes);
            if (!SOILM.isCompleted()) {
                System.out.println("Problem with SOILM");
                return;
            }
            thisHillsInfo.setSoilMoistureManager(SOILM);
            outputMetaFile = DirOutput + FileOutput + "2005.ini";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            // metaBuffer.write("30\n");
            metaBuffer.write("31\n");
            metaBuffer.write(mylinksAnalysis.contactsArray.length + "\n");
            metaBuffer.write("0 \n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                double currTime = SOILM.SOILM040InitialTimeInMinutes();
                double INIC = thisHillsInfo.SoilMoisture(i, currTime);
                if (INIC < 0) {
                    System.out.println("check the current time of the file\n");
                    System.exit(1);
                }

                //System.out.println(1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, 0) + "\n");
                //System.out.println("i=" + i + " "+ 1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                double inidisch=0.08*Math.pow(upAreas[0][i],0.87);
                metaBuffer.write(inidisch + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                //metaBuffer.write("\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();

            SOILMFile = new java.io.File("D:/usr/Delaware_luciana/CUENCASDelaware/Rasters/Hydrology/NLDAS/MSTAV0-200_VHC/2011/NLDAS2-MSTAV0-200.metavhc");
            //      hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager SOILM;
            SOILM = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SoilMoistureManager(SOILMFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes);
            if (!SOILM.isCompleted()) {
                System.out.println("Problem with SOILM");
                return;
            }
            thisHillsInfo.setSoilMoistureManager(SOILM);
           outputMetaFile = DirOutput + FileOutput + "2011.ini";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            metaBuffer.write("31\n");
            metaBuffer.write(mylinksAnalysis.contactsArray.length + "\n");
            metaBuffer.write("0 \n");
            metaBuffer.write("\n");
            for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                metaBuffer.write("" + i + "\n");
                double currTime = SOILM.SOILM040InitialTimeInMinutes();
                double INIC = thisHillsInfo.SoilMoisture(i, currTime);
                if (INIC < 0) {
                    System.out.println("check the current time of the file\n");
                    System.exit(1);
                }

                //System.out.println(1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, 0) + "\n");
                //System.out.println("i=" + i + " "+ 1e-6 + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                double inidisch=0.08*Math.pow(upAreas[0][i],0.87);
                metaBuffer.write(inidisch + " " + 0.0 + " " + 1e-6 + " " + thisHillsInfo.SoilMoisture(i, currTime) + "\n");
                //metaBuffer.write("\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();
            //Dir = "D:/usr/ArcGisLayers/Haiti/Rasters/Topography/";
         
            }
        } catch (java.io.IOException IOE) {
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void IowaAllLinks(String args[]) {

        int x = 5142;
        int y = 446;
        double vh = 0.1;
        double vr = 0.33;

//Delaware trenton
        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat) number2;
        dpoint2.applyPattern("0.00000000");
        float[] RCAr = {0.45f};
        float[] VRCoefAr = {0.01f};

        for (float RC : RCAr) {
            for (float VRCoef : VRCoefAr) {

                try {

                    //java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.metaDEM");
                    java.io.File theFile = new java.io.File("D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");

                    hydroScalingAPI.io.MetaRaster metaModif = new hydroScalingAPI.io.MetaRaster(theFile);
                    metaModif.setLocationBinaryFile(new java.io.File("D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

                    metaModif.setFormat("Byte");
                    byte[][] matDirs = new hydroScalingAPI.io.DataRaster(metaModif).getByte();

                    metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0, theFile.getPath().lastIndexOf(".")) + ".magn"));
                    metaModif.setFormat("Integer");
                    int[][] magnitudes = new hydroScalingAPI.io.DataRaster(metaModif).getInt();

                    hydroScalingAPI.util.geomorphology.objects.Basin laCuenca = new hydroScalingAPI.util.geomorphology.objects.Basin(x, y, matDirs, metaModif);

                    LinksAnalysis mylinksAnalysis = new LinksAnalysis(laCuenca, metaModif, matDirs);
                    hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(mylinksAnalysis);
                    hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(mylinksAnalysis);
                    String LandUse = "null";
                    String SoilData = "null";
                    String SoilHydData = "null";
                    String Soil150SWAData = "null";

                    java.io.File LandUseFile = new java.io.File(LandUse);
                    java.io.File SoilFile = new java.io.File(SoilData);
                    java.io.File SoilHydFile = new java.io.File(SoilHydData);
                    java.io.File SwaFileFile = new java.io.File(Soil150SWAData);

                    hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
                    java.io.File DEMFile = metaModif.getLocationMeta();

                    SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile, SoilHydFile, SwaFileFile, laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes, 0);
                    thisHillsInfo.setSCSManager(SCSObj);

                    float[][] upAreas = mylinksAnalysis.getVarValues(2);
                    float[][] areas = mylinksAnalysis.getVarValues(0);
                    float[][] lenghts = mylinksAnalysis.getVarValues(1);
                    java.io.File outputDirectory = new java.io.File("D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/ScottCodeFiles/InputFiles/");
                    outputDirectory.mkdirs();
                    String outputMetaFile = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/ScottCodeFiles/InputFiles/Del" + x + "_" + y + ".rvr";

                    java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
                    metaBuffer.write("\n");
                    for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                        metaBuffer.write("" + i + "\n");
                        metaBuffer.write("" + mylinksAnalysis.connectionsArray[i].length);

                        for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                            metaBuffer.write(" " + mylinksAnalysis.connectionsArray[i][j]);
                        }

                        metaBuffer.write("\n");
                        metaBuffer.write("\n");

                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();

                    outputMetaFile = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/ScottCodeFiles/InputFiles/Del_50_" + x + "_" + y + "_" + RC + "VR" + vr + ".prm";
                    System.out.printf(outputMetaFile);
//L_i,A_h,A_i,h_b,h_H,max_inf_rate,K_sat,S_h,eta,b_H,c_H,d_H,invtau,epsilon,c_1,c_2,c_3,c_4,c_5,c_6
                    metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    metaBuffer.write("" + mylinksAnalysis.connectionsArray.length + "\n");
                    metaBuffer.write("\n");

                    for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                        double vrf = vr;
                        // H==> Hydro-power     

                        metaBuffer.write(i + "\n");
                        metaBuffer.write(upAreas[0][i] + " " + lenghts[0][i] + " " + areas[0][i] + " ");
                        metaBuffer.write(RC + " " + "0.4" + " " + thisNetworkGeom.Slope(i) + " " + vrf + "\n");
                        // OLD  thisHillsInfo.getHillslope(i)
                        metaBuffer.write("\n");
                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();

                    outputMetaFile = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/ScottCodeFiles/InputFiles/Del_" + x + "_" + y + ".listComplete";
                    metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    float[][] hortonOrders = mylinksAnalysis.getVarValues(4);
                    System.out.println("mylinksAnalysis.contactsArray.length = " + mylinksAnalysis.contactsArray.length + " mylinksAnalysis.nextLinkArray.length " + mylinksAnalysis.nextLinkArray.length);
                    // metaBuffer.write("" + mylinksAnalysis.contactsArray.length + "\n");
                    // metaBuffer.write("\n");

                    for (int i = 0; i < mylinksAnalysis.contactsArray.length; i++) {
                        int X = mylinksAnalysis.contactsArray[i] % metaModif.getNumCols();
                        int Y = mylinksAnalysis.contactsArray[i] / metaModif.getNumCols();
                        double Long = metaModif.getMinLon() + X * metaModif.getResLat() / 3600.;
                        double Lat = metaModif.getMinLat() + Y * metaModif.getResLat() / 3600.;
                        metaBuffer.write(i + "," + mylinksAnalysis.contactsArray[i] + "," + hortonOrders[0][i] + "," + X + "," + Y + "," + Lat + "," + Long + "," + upAreas[0][i] + "," + lenghts[0][i] + "," + areas[0][i]);

                        if (mylinksAnalysis.connectionsArray[i].length > 0) {
                            metaBuffer.write("," + mylinksAnalysis.connectionsArray[i].length);
                        } else {
                            metaBuffer.write("," + "-1");
                        }

                        for (int j = 0; j < mylinksAnalysis.connectionsArray[i].length; j++) {
                            metaBuffer.write("," + mylinksAnalysis.contactsArray[mylinksAnalysis.connectionsArray[i][j]]);
                        }
                        metaBuffer.write("\n");
                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();

                    outputMetaFile = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/ScottCodeFiles/InputFiles/Del_" + x + "_" + y + ".listCompleteOrder";
                    metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    //float[][] hortonOrders = mylinksAnalysis.getVarValues(4);
                    System.out.println("mylinksAnalysis.contactsArray.length = " + mylinksAnalysis.contactsArray.length + " mylinksAnalysis.nextLinkArray.length " + mylinksAnalysis.nextLinkArray.length);
                    // metaBuffer.write("" + mylinksAnalysis.contactsArray.length + "\n");
                    // metaBuffer.write("\n");
/////////////////HERE

                    for (int i = 0; i < mylinksAnalysis.completeStreamLinksArray.length; i++) {
                        metaBuffer.write(mylinksAnalysis.completeStreamLinksArray[i] + "\n");
                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();
                    outputMetaFile = "D:/usr/Iowa/HydrologicalStudies/Iowa_Rivers_DB/ScottCodeFiles/InputFiles/Del_" + x + "_" + y + ".sav";
                    metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                    //metaBuffer.write(""+mylinksAnalysis.completeStreamLinksArray.length+"\n");
                    //metaBuffer.write("\n");
                    for (int i = 0; i < mylinksAnalysis.connectionsArray.length; i++) {
                        metaBuffer.write(i + "\n");
                    }
                    metaBuffer.write("\n");

                    metaBuffer.close();

                } catch (java.io.IOException IOE) {
                    System.out.print(IOE);
                    System.exit(0);
                }
            }
        }
        System.exit(0);

    }



    public static void main_MODLU(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(metaModif, matDirs);

            //FINDS LINK ID FOR FORECAST LOCATIONS IN IOWA

            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];
            int[] headsTails=mylinksAnalysis.contactsArray;

            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int numCols=metaModif.getNumCols();
            int numRows=metaModif.getNumRows();

            for(int i=0;i<headsTails.length;i++){
            //for(int i=0;i<10;i++){
                int xOulet=headsTails[i]%numCols;
                int yOulet=headsTails[i]/numCols;

                int tileColor=i+1;
                //System.out.println("Head: "+xOulet+","+yOulet+" Tail: "+xSource+","+ySource+" Color: "+tileColor+" Scale: "+(scale+1));

                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                int elementsInTile=myHillActual.getXYHillSlope()[0].length;
                for (int j=0;j<elementsInTile;j++){
                    matrizPintada[myHillActual.getXYHillSlope()[1][j]][myHillActual.getXYHillSlope()[0][j]]=tileColor;
                }
            }

            String[] argsX;
            argsX=new String[] {
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_large_cities.log",
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_medium_cities.log",
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_small_cities.log",
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_usgs_gauges.log",
                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_sensors.log"};

            for (int i = 0; i < argsX.length; i++) {
                String[] basins=new hydroScalingAPI.io.BasinsLogReader(new java.io.File(argsX[i])).getPresetBasins();

                System.out.println(argsX[i]);
                System.out.println();

                for (int j = 0; j < basins.length; j++) {
                

                    if(!basins[j].equalsIgnoreCase("")){
                        String[] basLabel = basins[j].split("; ");

                        int x=Integer.parseInt(basLabel[0].split(",")[0].split("x:")[1].trim());
                        int y=Integer.parseInt(basLabel[0].split(",")[1].split("y:")[1].trim());

                        System.out.println(basLabel[1].split(":")[0]+";"+x+";"+y+";"+matrizPintada[y][x]);

                    }
                }

                System.out.println();
                
            }

            System.exit(0);

            //CREATE FILE WITH CONNECTIVITY STRUCTURE

            float[][] areas=mylinksAnalysis.getVarValues(2);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] distToB=mylinksAnalysis.getVarValues(7);

            String outputMetaFile="/Users/ricardo/temp/NextLinkIowa.csv";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+(mylinksAnalysis.nextLinkArray[i]+1)+","+areas[0][i]+","+lenghts[0][i]+","+distToB[0][i]+"\n");

            metaBuffer.close();

            System.exit(0);

            //CREATE MASK FILE FOR IOWA

            java.io.FileOutputStream        outputDir;
            java.io.DataOutputStream        newfile;
            java.io.BufferedOutputStream    bufferout;

            java.io.File inputFile=new java.io.File("/Users/ricardo/temp/linksMask.vhc");

            outputDir = new java.io.FileOutputStream(inputFile);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.DataOutputStream(bufferout);

            for (int i=0;i<numRows;i++) for (int j=0;j<numCols;j++) {
                newfile.writeFloat(matrizPintada[i][j]);
            }
            newfile.close();
            bufferout.close();
            outputDir.close();

            hydroScalingAPI.io.CRasToEsriASCII exporter=new hydroScalingAPI.io.CRasToEsriASCII(new java.io.File("/Users/ricardo/temp/linksMask.metaVHC"),
                                                       new java.io.File("/Users/ricardo/temp/"));
            hydroScalingAPI.io.MetaRaster m1=new hydroScalingAPI.io.MetaRaster(new java.io.File("/Users/ricardo/temp/linksMask.metaVHC"));
            m1.setLocationBinaryFile(new java.io.File("/Users/ricardo/temp/linksMask.vhc"));

            exporter.fileToExport(m1);
            exporter.writeEsriFile();

            System.exit(0);


            

            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main6(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(metaModif, matDirs);

            //FINDS LINK ID FOR FORECAST LOCATIONS IN IOWA

            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];
            int[] headsTails=mylinksAnalysis.contactsArray;

            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int numCols=metaModif.getNumCols();
            int numRows=metaModif.getNumRows();

            for(int i=0;i<headsTails.length;i++){
            //for(int i=0;i<10;i++){
                int xOulet=headsTails[i]%numCols;
                int yOulet=headsTails[i]/numCols;

                int tileColor=i+1;
                //System.out.println("Head: "+xOulet+","+yOulet+" Tail: "+xSource+","+ySource+" Color: "+tileColor+" Scale: "+(scale+1));

                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                int elementsInTile=myHillActual.getXYHillSlope()[0].length;
                for (int j=0;j<elementsInTile;j++){
                    matrizPintada[myHillActual.getXYHillSlope()[1][j]][myHillActual.getXYHillSlope()[0][j]]=tileColor;
                }
            }

            String[] argsX;
            argsX=new String[] {
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_large_cities.log",
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_medium_cities.log",
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_small_cities.log",
                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_usgs_gauges.log",
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_sensors.log",
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_usace_gauges.log",
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/fake_res_iihr_rain_gauges.log",
//                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/fake_res_coop_rain_gauges.log"
            };

            for (int i = 0; i < argsX.length; i++) {
                String[] basins=new hydroScalingAPI.io.BasinsLogReader(new java.io.File(argsX[i])).getPresetBasins();

                System.out.println(argsX[i]);
                System.out.println();

                for (int j = 0; j < basins.length; j++) {
                

                    if(!basins[j].equalsIgnoreCase("")){
                        String[] basLabel = basins[j].split("; ");

                        int x=Integer.parseInt(basLabel[0].split(",")[0].split("x:")[1].trim());
                        int y=Integer.parseInt(basLabel[0].split(",")[1].split("y:")[1].trim());

                        System.out.println(basLabel[1].split(":")[0]+";"+x+";"+y+";"+matrizPintada[y][x]);

                    }
                }

                System.out.println();
                
            }

            //System.exit(0);

            //CREATE FILE WITH CONNECTIVITY STRUCTURE

            float[][] areas=mylinksAnalysis.getVarValues(2);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] distToB=mylinksAnalysis.getVarValues(7);

            String outputMetaFile="/Users/ricardo/temp/NextLinkIowa.csv";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+(mylinksAnalysis.nextLinkArray[i]+1)+","+areas[0][i]+","+lenghts[0][i]+","+distToB[0][i]+"\n");

            metaBuffer.close();

            System.exit(0);

            //CREATE MASK FILE FOR IOWA

            java.io.FileOutputStream        outputDir;
            java.io.DataOutputStream        newfile;
            java.io.BufferedOutputStream    bufferout;

            java.io.File inputFile=new java.io.File("/Users/ricardo/temp/linksMask.vhc");

            outputDir = new java.io.FileOutputStream(inputFile);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.DataOutputStream(bufferout);

            for (int i=0;i<numRows;i++) for (int j=0;j<numCols;j++) {
                newfile.writeFloat(matrizPintada[i][j]);
            }
            newfile.close();
            bufferout.close();
            outputDir.close();

            hydroScalingAPI.io.CRasToEsriASCII exporter=new hydroScalingAPI.io.CRasToEsriASCII(new java.io.File("/Users/ricardo/temp/linksMask.metaVHC"),
                                                       new java.io.File("/Users/ricardo/temp/"));
            hydroScalingAPI.io.MetaRaster m1=new hydroScalingAPI.io.MetaRaster(new java.io.File("/Users/ricardo/temp/linksMask.metaVHC"));
            m1.setLocationBinaryFile(new java.io.File("/Users/ricardo/temp/linksMask.vhc"));

            exporter.fileToExport(m1);
            exporter.writeEsriFile();

            System.exit(0);


            

            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main6_1(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(metaModif, matDirs);

//            float[][] upAreas=mylinksAnalysis.getVarValues(0);
//            System.out.println(new hydroScalingAPI.util.statistics.Stats(upAreas).meanValue);
//            //Returns
//            0.39465603
//            System.exit(0);


//            float[][] longestLenghts=mylinksAnalysis.getVarValues(11);
//            System.out.println(new hydroScalingAPI.util.statistics.Stats(longestLenghts).maxValue);
//            //Returns
//            906.93207
//            System.exit(0);

            //CREATE FILE OF CONVOLUTION FUNCTION

//            float[][] lenghts=mylinksAnalysis.getVarValues(1);
//            hydroScalingAPI.util.statistics.Stats infoVals=new hydroScalingAPI.util.statistics.Stats(lenghts);
//            System.out.println(infoVals.minValue);
//            System.out.println(infoVals.maxValue);
//            System.out.println(infoVals.meanValue);
//            System.out.println(infoVals.standardDeviation);
//            //Returns
//            0.09088898
//            7.19419
//            0.73578393
//            0.473324
//            System.exit(0);

            String outputMetaFile="/Users/ricardo/rawData/IowaConnectivity/NextHillslopeIowa.csv";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

//            float vc=0.5f;
//            float objLenght=vc*3.6f; float tllb=objLenght*.90f;
//            System.out.println(objLenght+" "+tllb);
//            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) {
//                float traveledLenght=lenghts[0][i];
//                int arrivalHillslope=mylinksAnalysis.nextLinkArray[i];
//                metaBuffer.write(i+"");
//                while(traveledLenght<tllb&&arrivalHillslope!=-1){
//                    metaBuffer.write(","+arrivalHillslope);
//                    traveledLenght+=lenghts[0][arrivalHillslope];
//                    arrivalHillslope=mylinksAnalysis.nextLinkArray[arrivalHillslope];
//                }
//                metaBuffer.write("\n");
//            }

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) {
                    metaBuffer.write(mylinksAnalysis.nextLinkArray[i]+"\n");
            }

            metaBuffer.close();

            System.exit(0);


        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main7(String args[]) {

        int x=1570; int y=127;

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] drops=mylinksAnalysis.getVarValues(3);
            
            String outputMetaFile="/Users/ricardo/temp/NextLinkClearCreek.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("Link-ID Num-connected-links List-of-connected-links Length[km] Area[km^2] upArea[km^2] Slope[*]"+"\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+i+" "+mylinksAnalysis.connectionsArray[i].length);
                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
                    metaBuffer.write(" "+mylinksAnalysis.connectionsArray[i][j]);
                metaBuffer.write(" "+lenghts[0][i]+" "+areas[0][i]+" "+upAreas[0][i]+" "+Math.max(0.0001,drops[0][i]/(lenghts[0][i]*1000))+"\n");
            }

            metaBuffer.close();



        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main7_Brandon(String args[]) {

        int x=1570; int y=127;

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] dToOut=mylinksAnalysis.getVarValues(8);

            String outputMetaFile="/Users/ricardo/temp/Brandon_ClearCreek.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("Link-ID Length[km] Area[km^2] upArea[km^2] TopologicalDistanceToOulet[count]"+"\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+(i+1)+" "+lenghts[0][i]+" "+areas[0][i]+" "+upAreas[0][i]+" "+dToOut[0][i]+"\n");
            }

            metaBuffer.close();



        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

        /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main8_0(String args[]) {

        //int x= 2734; int y= 1069 ;// Basin Code 05464500 Cedar River at Cedar Rapids, IA
        //int x= 3316; int y= 116 ;// Basin Code 05465500 Iowa River at Wapello, IA
        int x= 2115; int y= 801 ;// Basin Code 05453000 Big Bear Creek at Ladora, IA

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);

            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);

            String outputMetaFile="/Users/ricardo/temp/NextLinkCedarRiver3.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("Link-ID Num-connected-links List-of-connected-links Length[km] Area[km^2] upArea[km^2]"+"\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+i+" "+mylinksAnalysis.connectionsArray[i].length);
                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
                    metaBuffer.write(" "+mylinksAnalysis.connectionsArray[i][j]);
                metaBuffer.write(" "+lenghts[0][i]+" "+areas[0][i]+" "+upAreas[0][i]+"\n");
            }

            metaBuffer.close();



        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void main8_1(String args[]) {
    int flag=5;
        int x= 7876; int y= 1360 ;// Basin Code 05464500 Cedar River at Cedar Rapids, IA
    if(flag==2) {
            x=2734;y=1069;
    }
    if(flag==5) {
            x=2734;y=1069;
    }
     if(flag==4) {
            x=2885;y=690;
    }
    
         if(flag==3) {
            x=2;y=39;
        }
        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{
        
//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));

            java.io.File theFile=new java.io.File("/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM");
            if(flag==2 || flag==4) theFile=new java.io.File("/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
                if(flag==3) theFile=new java.io.File("/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Topography/dem_asc.metaDEM");
            if(flag==5) theFile=new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun6/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.dir"));
            if(flag==2|| flag==4) metaModif.setLocationBinaryFile(new java.io.File("/Groups/IFC//CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
            if(flag==3) metaModif.setLocationBinaryFile(new java.io.File("/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Topography/dem_asc.dir"));
            if(flag==5) metaModif.setLocationBinaryFile(new java.io.File("/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/90metersPrun6/AveragedIowaRiverAtColumbusJunctions.dir"));
       
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);
System.out.println("x" + x +"y" + y + "dem" + metaModif.toString());
            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
              float[][] areas=mylinksAnalysis.getVarValues(0);
              float[][] upAreas=mylinksAnalysis.getVarValues(2);
              
              
            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(mylinksAnalysis);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(mylinksAnalysis);
            
            String LandUse = "/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            String SoilData = "/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
            String SoilHydData = "/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            String Soil150SWAData = "/Groups/IFC/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
             if(flag==3) {
              LandUse = "/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Hydrology/landcoverproj.metaVHC";
              SoilData = "/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Hydrology/soilhydtype.metaVHC";
              SoilHydData = "/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Hydrology/hydcond.metaVHC";
              Soil150SWAData = "/Groups/IFC/CuencasDataBases/Shalehills/Rasters/Hydrology/swe.metaVHC";
                
                    }
            
            java.io.File LandUseFile = new java.io.File(LandUse);
            java.io.File SoilFile = new java.io.File(SoilData);
            java.io.File SoilHydFile = new java.io.File(SoilHydData);
            java.io.File SwaFileFile = new java.io.File(Soil150SWAData);
            
            hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
            java.io.File DEMFile = metaModif.getLocationMeta();
            
            
            
            SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile, SoilHydFile,SwaFileFile,laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes,0);
 
            thisHillsInfo.setSCSManager(SCSObj);
            System.out.println("x" + x +"y" + y + "dem" + metaModif.toString() + "mylinksAnalysis.connectionsArray.length" + mylinksAnalysis.connectionsArray.length);
            

//            float[][] upAreas=mylinksAnalysis.getVarValues(2);
//            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] hb = new float[1][mylinksAnalysis.connectionsArray.length]; // mmfloat 
            float[][] MaxInf = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Hh = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] HydCond = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] MaxInfRate = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Terms = new float[4][mylinksAnalysis.connectionsArray.length];
            float[][] Slope = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] width =thisNetworkGeom.getWidthArray();
            
            float[][] Drop = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Manning = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Area_Relief_Param = new float[4][mylinksAnalysis.connectionsArray.length]; //Area in km and depth in m
            float[][] Slope2 =thisNetworkGeom.getSlopeArray();
            
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
             hb[0][i]=(float) thisHillsInfo.SWA150(i);
             MaxInfRate[0][i]=(float) thisHillsInfo.MaxInfRate(i);
             System.out.println("hisHillsInfo.MaxInfRate(i)"+ thisHillsInfo.MaxInfRate(i) + "width"+width);
             Hh[0][i]=(float) thisHillsInfo.HillRelief(i)*1000;
             
             HydCond[0][i]=(float) thisHillsInfo.AveHydCond(i);
             //System.out.println("i" + i);
             Area_Relief_Param[0][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 0);
            Area_Relief_Param[1][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 1);
            Area_Relief_Param[2][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 2);
            Area_Relief_Param[3][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 3);
            
            if(Hh[0][i]==0) {Hh[0][i]=0;
            Area_Relief_Param[0][i]=0;
            Area_Relief_Param[1][i]=1;
            Area_Relief_Param[2][i]=0;
            Area_Relief_Param[3][i]=0;}
            
             Slope[0][i]=(float) thisHillsInfo.getHillslope(i);
             
             Manning[0][i]=(float) thisHillsInfo.HillManning(i);
             
            }
            Drop=mylinksAnalysis.getVarValues(3);
            java.io.File outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/dataRodica/");
            outputDirectory.mkdirs();
            
            String outputMetaFile=outputDirectory+"/NextLinkCedarRiver_30m.txt";
            System.out.println(outputMetaFile);
            
            if(flag==2) outputMetaFile=outputDirectory+"/NextLinkCedarRiver_90m.txt";
            if(flag==3) outputMetaFile=outputDirectory+"/Shalehills.txt";
            if(flag==5) outputMetaFile=outputDirectory+"/CedarPrun6.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
    
        
            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("ID Length[km] Area[km^2] upArea[km^2] ");
            metaBuffer.write("hb[mm] Hh[mm] MaxInfRate[m/h] ");
            metaBuffer.write(" HydCond[m/h] HillslopeSlope[m/m] Manning_n[] ");
            metaBuffer.write(" a b c d");
            metaBuffer.write("\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+(i+1)+" "+mylinksAnalysis.connectionsArray[i].length);
               
                metaBuffer.write(" "+lenghts[0][i]+" "+areas[0][i]+" "+upAreas[0][i]);
                metaBuffer.write(" "+hb[0][i]+" "+Hh[0][i]+" "+MaxInf[0][i]);
                metaBuffer.write(" "+HydCond[0][i]+" "+Slope[0][i]+" "+Manning[0][i]);
                metaBuffer.write(" "+Area_Relief_Param[0][i]+" "+Area_Relief_Param[1][i]+" "+Area_Relief_Param[2][i]+" "+Area_Relief_Param[3][i] + " " +Drop[0][i]);
                double sum=Area_Relief_Param[0][i]+Area_Relief_Param[1][i]+Area_Relief_Param[2][i]+Area_Relief_Param[3][i];
                
                System.out.println("areas[0][i]"+ areas[0][i] + "lenghts[0][i]" + lenghts[0][i] + "Slope[0][i]"+Slope[0][i]+" Manning[0][i] "+Manning[0][i]);
                metaBuffer.write("\n");
                if(areas[0][i]<0) System.out.println(i + "LINKwith area zero"+areas[0][i]);
        
                //System.exit(0);
            }

            metaBuffer.close();

            outputDirectory = new java.io.File("/nfsscratch/Users/rmantill/dataScott/");
            outputDirectory.mkdirs();
         outputMetaFile=outputDirectory+"/NextLinkCedarRiver_30m.txt";
         System.out.println(outputMetaFile);
            if(flag==2) outputMetaFile=outputDirectory+"/NextLinkCedarRiver_90m.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("ID Num-connected-links Conec-links Length[km] Area[km^2] upArea[km^2] ");
            metaBuffer.write("hb[mm] Hh[mm] MaxInfRate[m/h] ");
            metaBuffer.write(" HydCond[m/h] HillslopeSlope[m/m] Manning_n[] ");
            metaBuffer.write(" a b c d");
            metaBuffer.write("\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+(i+1)+" "+mylinksAnalysis.connectionsArray[i].length);
                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
                metaBuffer.write(" "+(mylinksAnalysis.connectionsArray[i][j]+1));
                
                metaBuffer.write(" "+lenghts[0][i]+" "+areas[0][i]+" "+upAreas[0][i]);
                metaBuffer.write(" "+hb[0][i]+" "+Hh[0][i]+" "+MaxInf[0][i]);
                metaBuffer.write(" "+HydCond[0][i]+" "+Slope[0][i]+" "+Manning[0][i]);
                metaBuffer.write(" "+Area_Relief_Param[0][i]+" "+Area_Relief_Param[1][i]+" "+Area_Relief_Param[2][i]+" "+Area_Relief_Param[3][i]);
                metaBuffer.write("\n");
                
                  System.out.println(" Slope[0][i] "+Slope[0][i] + " Slope2[0][i] "+Slope2[0][i] + "HydCond[0][i]+" + HydCond[0][i]);
             
            }

            metaBuffer.close();

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    public static void main8_Rodica(String args[]) {
    int flag=3;
        int x= 7875; int y= 1361 ;// Basin Code 05464500 Cedar River at Cedar Rapids, IA
        if(flag==2) {
            x=2734;y=1069;
        }
    
        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{
        
//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));

            java.io.File theFile=new java.io.File("/Groups/IFC//CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM");
            if(flag==2) theFile=new java.io.File("/Groups/IFC//CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/Groups/IFC//CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.dir"));
            if(flag==2) metaModif.setLocationBinaryFile(new java.io.File("/Groups/IFC//CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom = new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(mylinksAnalysis);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo = new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(mylinksAnalysis);
            
            String LandUse = "/Groups/IFC//CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/landcover2001_90_2.metaVHC";
            String SoilData = "/Groups/IFC//CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/soil_rec90.metaVHC";
            String SoilHydData = "/Groups/IFC//CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/hydcondint.metaVHC";
            String Soil150SWAData = "/scratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/LandCover/90/swa150int.metaVHC";
           
            java.io.File LandUseFile = new java.io.File(LandUse);
            java.io.File SoilFile = new java.io.File(SoilData);
            java.io.File SoilHydFile = new java.io.File(SoilHydData);
            java.io.File SwaFileFile = new java.io.File(Soil150SWAData);
            
            hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager SCSObj;
            java.io.File DEMFile = metaModif.getLocationMeta();
            
            
            
            SCSObj = new hydroScalingAPI.modules.rainfallRunoffModel.objects.SCSManager(DEMFile, LandUseFile, SoilFile, SoilHydFile,SwaFileFile,laCuenca, mylinksAnalysis, metaModif, matDirs, magnitudes,0);
 
            thisHillsInfo.setSCSManager(SCSObj);
        
            

            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] hb = new float[1][mylinksAnalysis.connectionsArray.length]; // mmfloat 
            float[][] MaxInf = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Hh = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] HydCond = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Terms = new float[4][mylinksAnalysis.connectionsArray.length];
            float[][] Slope = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Manning = new float[1][mylinksAnalysis.connectionsArray.length];
            float[][] Area_Relief_Param = new float[4][mylinksAnalysis.connectionsArray.length]; //Area in km and depth in m
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
             hb[0][i]=(float) thisHillsInfo.SCS_S1(i);
             MaxInf[0][i]=(float) thisHillsInfo.MaxInfRate(i);
             Hh[0][i]=(float) thisHillsInfo.HillRelief(i)*1000;
             
             HydCond[0][i]=(float) thisHillsInfo.AveHydCond(i);
             System.out.println("i" + i);
             Area_Relief_Param[0][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 0);
            Area_Relief_Param[1][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 1);
            Area_Relief_Param[2][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 2);
            Area_Relief_Param[3][i] = (float) thisHillsInfo.getArea_ReliefParam(i, 3);
            if(Hh[0][i]==0) {Hh[0][i]=10;
            Area_Relief_Param[0][i]=0;
            Area_Relief_Param[1][i]=1;
            Area_Relief_Param[2][i]=0;
            Area_Relief_Param[3][i]=0;}
            
             Slope[0][i]=(float) thisHillsInfo.getHillslope(i);
             Manning[0][i]=(float) thisHillsInfo.HillManning(i);
            }
            
            java.io.File outputDirectory = new java.io.File("/scratch/dataScott/");
            outputDirectory.mkdirs();
            String outputMetaFile="/scratch/Users/rmantill/dataRodica/NextLinkCedarRiver_30m.txt";
            if(flag==2) outputMetaFile="/scratch/Users/rmantill/dataRodica/NextLinkCedarRiver_90m.txt";
            if(flag==2) outputMetaFile="/scratch/Users/rmantill/dataRodica/Shalehill_90m.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("Link-ID Num-connected-links Length[km] Area[km^2] upArea[km^2] ");
            metaBuffer.write("hb[mm] Hh[mm] MaxInfRate[m/h] ");
            metaBuffer.write(" HydCond[m/h] HillslopeSlope[m/m] Manning_n[] ");
            metaBuffer.write(" a b c d");
            metaBuffer.write("\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+i+" "+mylinksAnalysis.connectionsArray[i].length);
                
                
                metaBuffer.write(" "+lenghts[0][i]+" "+areas[0][i]+" "+upAreas[0][i]);
                metaBuffer.write(" "+hb[0][i]+" "+Hh[0][i]+" "+MaxInf[0][i]);
                metaBuffer.write(" "+HydCond[0][i]+" "+Slope[0][i]+" "+Manning[0][i]);
                metaBuffer.write(" "+Area_Relief_Param[0][i]+" "+Area_Relief_Param[1][i]+" "+Area_Relief_Param[2][i]+" "+Area_Relief_Param[3][i]);
                metaBuffer.write("\n");
            }

            metaBuffer.close();



        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    public static void main8_2(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/HucTiledIowa/fullIowaDem.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/HucTiledIowa/fullIowaDem.dir"));

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.dir"));

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.dir"));

            
            System.out.println(">> Starting up");

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            System.out.println(">> Reading Matrixes Completed");

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(metaModif, matDirs);
            
            System.out.println(">> LinkAnalysis Completed");

//            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];
//            int[] headsTails=mylinksAnalysis.contactsArray;
//
//            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;
//
//            int numCols=metaModif.getNumCols();
//            int numRows=metaModif.getNumRows();
//            
//            for(int i=0;i<headsTails.length;i++){
//            //for(int i=0;i<10;i++){
//                int xOulet=headsTails[i]%numCols;
//                int yOulet=headsTails[i]/numCols;
//
//                int tileColor=i+2;
//                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
//                int elementsInTile=myHillActual.getXYHillSlope()[0].length;
//                for (int j=0;j<elementsInTile;j++){
//                    matrizPintada[myHillActual.getXYHillSlope()[1][j]][myHillActual.getXYHillSlope()[0][j]]=tileColor;
//                }
//                myHillActual=null;
//                
//                if(i%1000 == 0) {
//                    System.out.println(">> GC call - "+i+" out of "+headsTails.length);
//                    System.gc();
//                }
//            }
//
//            System.out.println(">> Matrix Pintada Completed");
//
//            //CREATE MASK FILE FOR IOWA (Starting at link +2)
//
            java.io.FileOutputStream        outputDir;
            java.io.DataOutputStream        newfile;
            java.io.BufferedOutputStream    bufferout;
//
//            java.io.File inputFile=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/linksMask_Iowa30m.vhc");
//
//            outputDir = new java.io.FileOutputStream(inputFile);
//            bufferout=new java.io.BufferedOutputStream(outputDir);
//            newfile=new java.io.DataOutputStream(bufferout);
//
//            for (int i=0;i<numRows;i++) for (int j=0;j<numCols;j++) {
//                newfile.writeFloat(matrizPintada[i][j]);
//            }
//            newfile.close();
//            bufferout.close();
//            outputDir.close();
//            
//            System.out.println(">> Matrix Pintada Writing Completed");
//
//            //Writing upstream connectivity
//            
            String outputMetaFile="/Users/ricardo/temp/Iowa30Connectivity.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
//
//            metaBuffer.write("Number of Links\n");
//            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
//            metaBuffer.write("Link-ID;Num-connected-links;List-of-connected-links;Length[km];Area[km^2];upArea[km^2]"+"\n");
//            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
//                metaBuffer.write(""+(i+2)+";"+mylinksAnalysis.connectionsArray[i].length);
//                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
//                    metaBuffer.write(";"+(mylinksAnalysis.connectionsArray[i][j]+2));
//            }
//            
//            metaBuffer.close();
//            
//            System.out.println(">> Upstream Connectivity Writing Completed");
//            
//            
//            //Writing downstream connectivity
//            
//            outputMetaFile="/Users/ricardo/temp/NextLinkIowa.csv";
//            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
//
//            metaBuffer.write("Number of Links\n");
//            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");
//
//            metaBuffer.write("Link-ID;DownstreamLink-ID"+"\n");
//            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+2)+";"+(mylinksAnalysis.nextLinkArray[i]+2)+"\n");
//
//            metaBuffer.close();
//
//            System.out.println(">> Downstream Connectivity Writing Completed");
            
            //Writing LinksInfo
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] dToB=mylinksAnalysis.getVarValues(7);
            float[][] longest=mylinksAnalysis.getVarValues(11);
            float[][] hortonO=mylinksAnalysis.getVarValues(4);
            
            outputMetaFile="/Users/ricardo/temp/DataLinksIowa30.txt";
            //String outputMetaFile="/Users/ricardo/temp/IowaConnectivity.txt";
            //String outputMetaFile="/Users/ricardo/temp/GoodwinConnectivity.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

//            metaBuffer.write("Number of Links\n");
//            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
//            metaBuffer.write("Link-ID Num-connected-links List-of-connected-links Length[km] Area[km^2] upArea[km^2]"+"\n");
//            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
//                metaBuffer.write(""+i+" "+mylinksAnalysis.connectionsArray[i].length);
//                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
//                    metaBuffer.write(" "+mylinksAnalysis.connectionsArray[i][j]);
//                metaBuffer.write(" "+lenghts[0][i]+" "+areas[0][i]+" "+upAreas[0][i]+"\n");
//            }
            
            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("Link-ID;Distance to Border[km];HIllArea[km^2];linkLenght[km];upArea[km^2];longestChannelPath[km];HortonOrder"+"\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+(i+2)+";"+dToB[0][i]+";"+areas[0][i]+";"+lenghts[0][i]+";"+upAreas[0][i]+";"+longest[0][i]+";"+hortonO[0][i]+"\n");
            }

            metaBuffer.close();
            
            System.out.println(">> Links Info Writing Completed");

            System.exit(0);
            
            


        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    public static void main8_3(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.dir"));

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(metaModif, matDirs);

            float[][] dToB=mylinksAnalysis.getVarValues(8);
            

            String outputMetaFile="/Users/ricardo/temp/DataLinksIowa.txt";
            //String outputMetaFile="/Users/ricardo/temp/IowaConnectivity.txt";
            //String outputMetaFile="/Users/ricardo/temp/GoodwinConnectivity.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

//            metaBuffer.write("Number of Links\n");
//            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
//            metaBuffer.write("Link-ID Num-connected-links List-of-connected-links Length[km] Area[km^2] upArea[km^2]"+"\n");
//            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
//                metaBuffer.write(""+i+" "+mylinksAnalysis.connectionsArray[i].length);
//                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
//                    metaBuffer.write(" "+mylinksAnalysis.connectionsArray[i][j]);
//                metaBuffer.write(" "+lenghts[0][i]+" "+areas[0][i]+" "+upAreas[0][i]+"\n");
//            }
            
            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("Link-ID;Distance to Border[links]"+"\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+(i+2)+";"+(int)dToB[0][i]+"\n");
            }

            metaBuffer.close();


        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main9(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");
        
        int x= 7876; int y= 1360 ;

        try{
            
            System.out.println(">> Ready to go...");

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            System.out.println(">> Information read...");
            
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            System.out.println(">> Basin Extracted...");
            
            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);

            System.out.println(">> Structure created...");
            
            //FINDS LINK ID FOR FORECAST LOCATIONS IN IOWA

            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];
            int[] headsTails=mylinksAnalysis.contactsArray;

            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int numCols=metaModif.getNumCols();
            int numRows=metaModif.getNumRows();
            
            laCuenca=null;
            System.gc();

            for(int i=0;i<headsTails.length;i++){
            //for(int i=0;i<10;i++){
                int xOulet=headsTails[i]%numCols;
                int yOulet=headsTails[i]/numCols;

                int tileColor=i+1;
                System.out.println("Head: "+xOulet+","+yOulet+" Color: "+tileColor+"("+headsTails.length+")");

                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                int elementsInTile=myHillActual.getXYHillSlope()[0].length;
                for (int j=0;j<elementsInTile;j++){
                    matrizPintada[myHillActual.getXYHillSlope()[1][j]][myHillActual.getXYHillSlope()[0][j]]=tileColor;
                }
                myHillActual=null;
                
                if(i%1000 == 0) {
                    System.out.println(">>GC");
                    System.gc();
                }
            }

            //CREATE MASK FILE FOR IOWA

            java.io.FileOutputStream        outputDir;
            java.io.DataOutputStream        newfile;
            java.io.BufferedOutputStream    bufferout;

            java.io.File inputFile=new java.io.File("/Users/ricardo/rawData/IowaConnectivity/linksMask_CedarRiver30m.vhc");

            outputDir = new java.io.FileOutputStream(inputFile);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.DataOutputStream(bufferout);

            for (int i=0;i<numRows;i++) for (int j=0;j<numCols;j++) {
                newfile.writeFloat(matrizPintada[i][j]);
            }
            newfile.close();
            bufferout.close();
            outputDir.close();
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main10(String args[]) { //For Walter and the Formula

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{
            
            System.out.println(">> Reading information");

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(metaModif, matDirs);
            
            //FINDS LINK ID FOR FORECAST LOCATIONS IN IOWA

            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];
            int[] headsTails=mylinksAnalysis.contactsArray;

            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int numCols=metaModif.getNumCols();
            int numRows=metaModif.getNumRows();

            System.out.println(">> Calculating Mask");

            for(int i=0;i<headsTails.length;i++){
            //for(int i=0;i<10;i++){
                int xOulet=headsTails[i]%numCols;
                int yOulet=headsTails[i]/numCols;

                int tileColor=i+1;
                //System.out.println("Head: "+xOulet+","+yOulet+" Tail: "+xSource+","+ySource+" Color: "+tileColor+" Scale: "+(scale+1));

                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                int elementsInTile=myHillActual.getXYHillSlope()[0].length;
                for (int j=0;j<elementsInTile;j++){
                    matrizPintada[myHillActual.getXYHillSlope()[1][j]][myHillActual.getXYHillSlope()[0][j]]=tileColor;
                }
            }
            
            
            System.out.println(matrizPintada[2319][3495]);
            System.out.println(matrizPintada[2659][4712]);
            System.out.println(matrizPintada[1932][4468]);
            System.out.println(matrizPintada[1735][1998]);
            System.out.println(matrizPintada[2671][2416]);
            
            System.exit(0);

            System.out.println(">> Aquiring distances, areas and lenghts");
            
            float[][] bigDtoO=mylinksAnalysis.getDistancesToOutlet();
            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] orders=mylinksAnalysis.getVarValues(4);

            String outputMetaFile="/Users/ricardo/temp/LinksInBasinsIowa.csv";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            String[] argsX;
            argsX=new String[] {"/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric Osgood/res_usgs_gauges.log"};
            
            int nCols=metaModif.getNumCols();
            double mRes=metaModif.getResLat();
            double minLat=metaModif.getMinLat();
            double minLon=metaModif.getMinLon();

            for (int i = 0; i < argsX.length; i++) {
                String[] basins=new hydroScalingAPI.io.BasinsLogReader(new java.io.File(argsX[i])).getPresetBasins();

                System.out.println(">> Opening: "+argsX[i]);
                

                for (int j = 0; j < basins.length; j++) {
                

                    if(!basins[j].equalsIgnoreCase("")){
                        String[] basLabel = basins[j].split("; ");

                        int x=Integer.parseInt(basLabel[0].split(",")[0].split("x:")[1].trim());
                        int y=Integer.parseInt(basLabel[0].split(",")[1].split("y:")[1].trim());

                        metaBuffer.write(basLabel[1].split(":")[0]+";"+x+";"+y+";"+matrizPintada[y][x]+"\n");
                        
                        java.util.Vector distsAndPos=mylinksAnalysis.getUpLinksDistancesToOutlet(bigDtoO,matrizPintada[y][x]-1);
                        
                        int numLinksUp=distsAndPos.size();
                        
                        System.out.println(basLabel[1].split(":")[0]+";"+x+";"+y+";"+matrizPintada[y][x]);
                        System.out.println("Total # of links on this basin;"+numLinksUp);
                        
                        metaBuffer.write("Total # of links on this basin;"+numLinksUp+"\n");
                        metaBuffer.write("Link-ID;Topological Distance to Outlet;Longitude;Latitude;hillArea[km^2];linkLength[km];HortonOrder"+"\n");
                        
                        float[] valuesOutlet=(float[])distsAndPos.get(0);
                        int dOutlet=(int)(valuesOutlet[0]);
                        
                        for (int k = 0; k < numLinksUp; k++) {
                            float[] values=(float[])distsAndPos.get(k);
                            
                            double xxx=(mylinksAnalysis.contactsArray[(int)values[2]]%nCols)*mRes/3600.+minLon;
                            double yyy=(mylinksAnalysis.contactsArray[(int)values[2]]/nCols)*mRes/3600.+minLat;
                            
                            metaBuffer.write((int)(values[2]+1)+";"+(int)(values[0]-dOutlet+1)+";"+xxx+";"+yyy+";"+areas[0][(int)values[2]]+";"+lenghts[0][(int)values[2]]+";"+orders[0][(int)values[2]]+"\n");
                        }

                    }
                    
                    
                }

            }

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    public static void main11(String args[]) {

        int x=1570; int y=127;

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);

            String outputMetaFile="/Users/ricardo/temp/ClearCreek_"+x+"_"+y+".rvr";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+i+"\n");
                metaBuffer.write(""+mylinksAnalysis.connectionsArray[i].length);
                
                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
                    metaBuffer.write(" "+mylinksAnalysis.connectionsArray[i][j]);
                
                metaBuffer.write("\n");
                metaBuffer.write("\n");
                
            }
            metaBuffer.write("\n");

            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/ClearCreek_"+x+"_"+y+".prm";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("\n");
            
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(i+"\n");
                metaBuffer.write(upAreas[0][i]+" "+lenghts[0][i]+" "+areas[0][i]+"\n");
                metaBuffer.write("\n");
            }
            metaBuffer.write("\n");

            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/ClearCreek_"+x+"_"+y+".complete";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            
            float[][] hortonOrders=mylinksAnalysis.getVarValues(4);

            metaBuffer.write(""+mylinksAnalysis.completeStreamLinksArray.length+"\n");
            metaBuffer.write("\n");
            
            for (int i=0;i<mylinksAnalysis.completeStreamLinksArray.length;i++) {
                metaBuffer.write(mylinksAnalysis.completeStreamLinksArray[i]+" "+hortonOrders[0][mylinksAnalysis.completeStreamLinksArray[i]]+"\n");
            }
            metaBuffer.write("\n");

            metaBuffer.close();



        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main12(String args[]) {

        int x=10989; int y=440; //Full Basin
        //int x=4031; int y=1281; //Sub Basin (~6km^2)

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/burned/burneddem5cc.asc.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/burned/burneddem5cc.asc.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            System.out.println(mylinksAnalysis.nextLinkArray.length);

            String outputMetaFile="/Users/ricardo/temp/NextLinkClearCreek_Chi.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+(mylinksAnalysis.nextLinkArray[i]+1)+"\n");
            
            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/LinkInfoClearCreek_Chi.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");
            
            metaBuffer.write("Link ID,Link lenght [km], Slope [*], upstreamArea [km^2], hillslopeArea [km^2]\n");
            
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] drop=mylinksAnalysis.getVarValues(3);
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] hillAreas=mylinksAnalysis.getVarValues(0);

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+lenghts[0][i]+","+drop[0][i]/lenghts[0][i]/1000.0+","+upAreas[0][i]+","+hillAreas[0][i]+"\n");
            
            metaBuffer.close();
            
            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int demNumCols=metaModif.getNumCols();

            for (int i=0;i<mylinksAnalysis.contactsArray.length;i++){
                if (mylinksAnalysis.magnitudeArray[i] < mylinksAnalysis.basinMagnitude){

                    xOulet=mylinksAnalysis.contactsArray[i]%demNumCols;
                    yOulet=mylinksAnalysis.contactsArray[i]/demNumCols;

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;

                    }
                } else {
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;
                    }
                }
            }

            String fileBinSalida="/CuencasDataBases/ClearCreek_Database/Rasters/Hydrology/burneddem5cc.asc_BasinWatershedsFull_Level1.vhc";
            java.io.File outputBinaryFile=new java.io.File(fileBinSalida);
            java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));

            int nRows=matrizPintada.length;
            int nCols=matrizPintada[0].length;


            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    rasterBuffer.writeInt(matrizPintada[i][j]);
                }
            }

            rasterBuffer.close();
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main12_1(String args[]) {

        int x=7055, y= 1242;

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Topography/5meter/ames_dem_10.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/SquawCreek/Rasters/Topography/5meter/ames_dem_10.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            System.out.println(mylinksAnalysis.nextLinkArray.length);

            String outputMetaFile="/Users/ricardo/temp/NextLinkSquawCreek_Chi.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+(mylinksAnalysis.nextLinkArray[i]+1)+"\n");
            
            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/LinkInfoSquawCreek_Chi.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");
            
            metaBuffer.write("Link ID,Link lenght [km], Slope [*], upstreamArea [km^2], hillslopeArea [km^2]\n");
            
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] drop=mylinksAnalysis.getVarValues(3);
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] hillAreas=mylinksAnalysis.getVarValues(0);

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+lenghts[0][i]+","+drop[0][i]/lenghts[0][i]/1000.0+","+upAreas[0][i]+","+hillAreas[0][i]+"\n");
            
            metaBuffer.close();
            
            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int demNumCols=metaModif.getNumCols();

            for (int i=0;i<mylinksAnalysis.contactsArray.length;i++){
                if (mylinksAnalysis.magnitudeArray[i] < mylinksAnalysis.basinMagnitude){

                    xOulet=mylinksAnalysis.contactsArray[i]%demNumCols;
                    yOulet=mylinksAnalysis.contactsArray[i]/demNumCols;

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;

                    }
                } else {
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;
                    }
                }
            }

            String fileBinSalida="/CuencasDataBases/SquawCreek/Rasters/Topography/5meter/ames_dem_10_BasinWatershedsFull_Level1.vhc";
            java.io.File outputBinaryFile=new java.io.File(fileBinSalida);
            java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));

            int nRows=matrizPintada.length;
            int nCols=matrizPintada[0].length;


            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    rasterBuffer.writeInt(matrizPintada[i][j]);
                }
            }

            rasterBuffer.close();
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main12_2(String args[]) {

        int x=3334, y= 259;

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Nishnabotna_Atlantic/Rasters/Topography/5meters/upatlantic.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Nishnabotna_Atlantic/Rasters/Topography/5meters/upatlantic.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            System.out.println(mylinksAnalysis.nextLinkArray.length);

            String outputMetaFile="/Users/ricardo/temp/NextLinkENAtlantic_Chi.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+(mylinksAnalysis.nextLinkArray[i]+1)+"\n");
            
            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/LinkInfoENAtlantic_Chi.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");
            
            metaBuffer.write("Link ID,Link lenght [km], Slope [*], upstreamArea [km^2], hillslopeArea [km^2]\n");
            
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] drop=mylinksAnalysis.getVarValues(3);
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] hillAreas=mylinksAnalysis.getVarValues(0);

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+lenghts[0][i]+","+drop[0][i]/lenghts[0][i]/1000.0+","+upAreas[0][i]+","+hillAreas[0][i]+"\n");
            
            metaBuffer.close();
            
            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int demNumCols=metaModif.getNumCols();

            for (int i=0;i<mylinksAnalysis.contactsArray.length;i++){
                if (mylinksAnalysis.magnitudeArray[i] < mylinksAnalysis.basinMagnitude){

                    xOulet=mylinksAnalysis.contactsArray[i]%demNumCols;
                    yOulet=mylinksAnalysis.contactsArray[i]/demNumCols;

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;

                    }
                } else {
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;
                    }
                }
            }

            String fileBinSalida="/CuencasDataBases/Nishnabotna_Atlantic/Rasters/Topography/5meters/upatlantic_BasinWatershedsFull_Level1.vhc";
            java.io.File outputBinaryFile=new java.io.File(fileBinSalida);
            java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));

            int nRows=matrizPintada.length;
            int nCols=matrizPintada[0].length;


            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    rasterBuffer.writeInt(matrizPintada[i][j]);
                }
            }

            rasterBuffer.close();
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main12_3(String args[]) {

        int x=3999, y= 973;

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/IndianCreek/Rasters/Topography/5_meters/indiancreek5m.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/IndianCreek/Rasters/Topography/5_meters/indiancreek5m.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            System.out.println(mylinksAnalysis.nextLinkArray.length);

            String outputMetaFile="/Users/ricardo/temp/NextLinkIndianCreek_Chi.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+(mylinksAnalysis.nextLinkArray[i]+1)+"\n");
            
            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/LinkInfoIndianCreek_Chi.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");
            
            metaBuffer.write("Link ID,Link lenght [km], Slope [*], upstreamArea [km^2], hillslopeArea [km^2]\n");
            
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] drop=mylinksAnalysis.getVarValues(3);
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] hillAreas=mylinksAnalysis.getVarValues(0);

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+lenghts[0][i]+","+drop[0][i]/lenghts[0][i]/1000.0+","+upAreas[0][i]+","+hillAreas[0][i]+"\n");
            
            metaBuffer.close();
            
            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int demNumCols=metaModif.getNumCols();

            for (int i=0;i<mylinksAnalysis.contactsArray.length;i++){
                if (mylinksAnalysis.magnitudeArray[i] < mylinksAnalysis.basinMagnitude){

                    xOulet=mylinksAnalysis.contactsArray[i]%demNumCols;
                    yOulet=mylinksAnalysis.contactsArray[i]/demNumCols;

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;

                    }
                } else {
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;
                    }
                }
            }

            String fileBinSalida="//CuencasDataBases/IndianCreek/Rasters/Hydrology/IndianCreek_BasinWatershedsFull_Level1.vhc";
            java.io.File outputBinaryFile=new java.io.File(fileBinSalida);
            java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));

            int nRows=matrizPintada.length;
            int nCols=matrizPintada[0].length;


            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    rasterBuffer.writeInt(matrizPintada[i][j]);
                }
            }

            rasterBuffer.close();
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main12_4(String args[]) {

        int x=16479, y= 649;

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/MiddleRaccoon/Rasters/Topography/raccoon5m.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/MiddleRaccoon/Rasters/Topography/raccoon5m.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            System.out.println(mylinksAnalysis.nextLinkArray.length);

            String outputMetaFile="/Users/ricardo/temp/NextLinkMiddleRaccoon_Chi.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+(mylinksAnalysis.nextLinkArray[i]+1)+"\n");
            
            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/UpLinkMiddleRaccoon_Chi.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("Link-ID Num-connected-links List-of-connected-links"+"\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+(i+1)+" "+mylinksAnalysis.connectionsArray[i].length);
                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
                    metaBuffer.write(" "+(mylinksAnalysis.connectionsArray[i][j]+1));
                metaBuffer.write("\n");
            }

            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/LinkInfoMiddleRaccoon_Chi.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");
            
            metaBuffer.write("Link ID,Link lenght [km], Slope [*], upstreamArea [km^2], hillslopeArea [km^2]\n");
            
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] drop=mylinksAnalysis.getVarValues(3);
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] hillAreas=mylinksAnalysis.getVarValues(0);

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+lenghts[0][i]+","+drop[0][i]/lenghts[0][i]/1000.0+","+upAreas[0][i]+","+hillAreas[0][i]+"\n");
            
            metaBuffer.close();
            
            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int demNumCols=metaModif.getNumCols();

            for (int i=0;i<mylinksAnalysis.contactsArray.length;i++){
                if (mylinksAnalysis.magnitudeArray[i] < mylinksAnalysis.basinMagnitude){

                    xOulet=mylinksAnalysis.contactsArray[i]%demNumCols;
                    yOulet=mylinksAnalysis.contactsArray[i]/demNumCols;

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;

                    }
                } else {
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;
                    }
                }
            }

            String fileBinSalida="/CuencasDataBases/MiddleRaccoon/Rasters/Hydrology/MiddleRaccoon_BasinWatershedsFull_Level1.vhc";
            java.io.File outputBinaryFile=new java.io.File(fileBinSalida);
            java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));

            int nRows=matrizPintada.length;
            int nCols=matrizPintada[0].length;


            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    rasterBuffer.writeInt(matrizPintada[i][j]);
                }
            }

            rasterBuffer.close();
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main12_5(String args[]) {

        int x=1883, y= 57;

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Walnut_Creek_IA/Rasters/Topography/walnut_creek_5m.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Walnut_Creek_IA/Rasters/Topography/walnut_creek_5m.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();


            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);
            
            System.out.println(mylinksAnalysis.nextLinkArray.length);

            String outputMetaFile="/Users/ricardo/temp/NextLinkWalnutCreek_Chi.txt";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+(mylinksAnalysis.nextLinkArray[i]+1)+"\n");
            
            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/UpLinkWalnutCreek_Chi.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write("Number of Links\n");
            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
            metaBuffer.write("Link-ID Num-connected-links List-of-connected-links"+"\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write(""+(i+1)+" "+mylinksAnalysis.connectionsArray[i].length);
                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++)
                    metaBuffer.write(" "+(mylinksAnalysis.connectionsArray[i][j]+1));
                metaBuffer.write("\n");
            }

            metaBuffer.close();
            
            outputMetaFile="/Users/ricardo/temp/LinkInfoWalnutCreek_Chi.txt";
            metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

            metaBuffer.write(mylinksAnalysis.nextLinkArray.length+"\n");
            
            metaBuffer.write("Link ID,Link lenght [km], Slope [*], upstreamArea [km^2], hillslopeArea [km^2]\n");
            
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
            float[][] drop=mylinksAnalysis.getVarValues(3);
            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] hillAreas=mylinksAnalysis.getVarValues(0);

            for (int i=0;i<mylinksAnalysis.nextLinkArray.length;i++) metaBuffer.write((i+1)+","+lenghts[0][i]+","+drop[0][i]/lenghts[0][i]/1000.0+","+upAreas[0][i]+","+hillAreas[0][i]+"\n");
            
            metaBuffer.close();
            
            int[][] matrizPintada=new int[metaModif.getNumRows()][metaModif.getNumCols()];

            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;

            int demNumCols=metaModif.getNumCols();

            for (int i=0;i<mylinksAnalysis.contactsArray.length;i++){
                if (mylinksAnalysis.magnitudeArray[i] < mylinksAnalysis.basinMagnitude){

                    xOulet=mylinksAnalysis.contactsArray[i]%demNumCols;
                    yOulet=mylinksAnalysis.contactsArray[i]/demNumCols;

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;

                    }
                } else {
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++){
                        matrizPintada[xyHillSlope[1][j]][xyHillSlope[0][j]]=i+1;
                    }
                }
            }

            String fileBinSalida="/CuencasDataBases/Walnut_Creek_IA/Rasters/Hydrology/WalnutCreek_BasinWatershedsFull_Level1.vhc";
            java.io.File outputBinaryFile=new java.io.File(fileBinSalida);
            java.io.DataOutputStream rasterBuffer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(outputBinaryFile)));

            int nRows=matrizPintada.length;
            int nCols=matrizPintada[0].length;


            for (int i=0;i<nRows;i++){
                for (int j=0;j<nCols;j++){
                    rasterBuffer.writeInt(matrizPintada[i][j]);
                }
            }

            rasterBuffer.close();
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
    
        public static void main11_Tibebu(String args[]) {
        
//        int x=23576; int y=2957; // Iowa River at Oakville using Iowa DEM (Ricardo)
        int x=23426; int y=2735; // Iowa River at Oakville using Iowa DEM (Tibebu)
//        x=55; y=114;
//        int x=906; int y=88; // Boone River
//        int x=2308; int y=199; // Old Mans Creek
//        int x=1570; int y=127; // Clear Creek
//        int x=7875; int y=1361; // Cedar River
//        x=2681; y=4153;//CID order 6 A=240
//        x=4882; y=3731; //CID order 8 A=2279
//        x=2100; y=4535; // CID order 5 A =64
//        int x=2480; int y=552; // Soap Creek
        
//        int flag=1;
//        int x= 7875; int y= 1361 ;// Basin Code 05464500 Cedar River at Cedar Rapids, IA
//        if(flag==2) {
//            x=2734;y=1069;
//        }

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

//            java.io.File theFile=new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/BooneRiver_Database/Rasters/Topography/NED_62925931.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/BooneRiver_Database/Rasters/Topography/NED_62925931.dir"));
            
//            java.io.File theFile=new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/OldMansCreek_Database/Rasters/Topography/NED_18879939.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/OldMansCreek_Database/Rasters/Topography/NED_18879939.dir"));
            
//            java.io.File theFile=new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/ClearCreek_Database/Rasters/Topography/ClearCreek/NED_00159011.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/ClearCreek_Database/Rasters/Topography/ClearCreek/NED_00159011.dir"));
            
//            java.io.File theFile=new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/CedarRiver_Database/Rasters/Topography/CedarRiver.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile); 
//            metaModif.setLocationBinaryFile(new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/CedarRiver_Database/Rasters/Topography/CedarRiver.dir"));
            
            java.io.File theFile=new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/IowaRiver_Database2/Rasters/Topography/fullIowaDem/fullIowaDem.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile); 
            metaModif.setLocationBinaryFile(new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/IowaRiver_Database2/Rasters/Topography/fullIowaDem/fullIowaDem.dir"));
            
//            java.io.File theFile=new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile); 
//            metaModif.setLocationBinaryFile(new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.dir"));
           
//            java.io.File theFile=new java.io.File("A:/Databases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM");
//            if(flag==2) theFile=new java.io.File("A:/Databases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("A:/Databases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.dir"));
//            if(flag==2) metaModif.setLocationBinaryFile(new java.io.File("A:/Databases/Iowa_Rivers_DB/Rasters/Topography/3_arcSec/AveragedIowaRiverAtColumbusJunctions.dir"));
//            metaModif.setFormat("Byte");
            
            
//            java.io.File theFile=new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/SoapCreek_Database/Rasters/Topography/SoapCreek.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/SoapCreek_Database/Rasters/Topography/SoapCreek.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();////
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            
//            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);
            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);            
            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] hortonOrder=mylinksAnalysis.getVarValues(4);
            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);
//            float[][] longestDist=mylinksAnalysis.getVarValues(11);
            float[][] distToOut=mylinksAnalysis.getVarValues(7);
            float[][] topoDistToOut=mylinksAnalysis.getVarValues(8);
            java.text.DecimalFormat fixDigits = new java.text.DecimalFormat("0.000000");
            
             //the following three lines are used for writing KML files
//            java.io.File outLocation = new java.io.File("C:/Users/tayalew/Documents/Tibebu_Assignments/Radar_Hydrology/GIS/KML");
//            hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDirs,metaModif);
//            hydroScalingAPI.io.MetaNetToKML exporter=new hydroScalingAPI.io.MetaNetToKML(metaModif,outLocation,myCuenca,matDirs,myCuenca.toString());
//        System.exit(0);
        
//            String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Radar_Hydrology/ASYNCH_Files/IowaRiver_"+x+"_"+y+".rvr";
//            outputMetaFile="C:/Users/tayalew/Documents/CuencasDataBases/Goodwin_Creek_MS_database/ASYNCH_Files/GoodwinCreek_"+x+"_"+y+".rvr";
////            String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Reservoir_Study/Soap_Creek/SoapCreek_"+x+"_"+y+".rvr";
////            String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Scaling_Study/Clear_Creek/ClearCreek_"+x+"_"+y+".rvr";
//            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
//            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
//            metaBuffer.write("\n");
//            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
//                metaBuffer.write(""+(i+2)+"\n");
//                metaBuffer.write(""+mylinksAnalysis.connectionsArray[i].length);                
//                for (int j=0;j<mylinksAnalysis.connectionsArray[i].length;j++) {
//                    metaBuffer.write(" "+mylinksAnalysis.connectionsArray[i][j]);
//                }                
//                metaBuffer.write("\n");
//                metaBuffer.write("\n");                
//            }
//            metaBuffer.write("\n");
//            metaBuffer.close();
//            
////            outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Reservoir_Study/Soap_Creek/SoapCreek_"+x+"_"+y+".prm";
////            outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Scaling_Study/Clear_Creek/ClearCreek_"+x+"_"+y+".prm";
//            String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Radar_Hydrology/ASYNCH_Files/IowaRiver_"+x+"_"+y+".prm";
//            outputMetaFile="C:/Users/tayalew/Documents/CuencasDataBases/Goodwin_Creek_MS_database/ASYNCH_Files/GoodwinCreek_"+x+"_"+y+".prm";
//            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
//            metaBuffer.write(""+mylinksAnalysis.connectionsArray.length+"\n");
//            metaBuffer.write("\n");
            
//            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
//                metaBuffer.write((i)+"\n");
//                metaBuffer.write(upAreas[0][i]+" "+lenghts[0][i]+" "+areas[0][i]+" "+1.0+" "+0.01+" "+1.0+"\n");
//                metaBuffer.write(fixDigits.format(upAreas[0][i])+" "+fixDigits.format(lenghts[0][i])+" "+fixDigits.format(areas[0][i])+"\n");
//                metaBuffer.write("\n");
//            }
//            metaBuffer.write("\n");

//            metaBuffer.close();
//            
//            String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Radar_Hydrology/ASYNCH_Files/IowaRiver_"+x+"_"+y+".complete";
////            outputMetaFile="C:/Users/tayalew/Documents/CuencasDataBases/Goodwin_Creek_MS_database/ASYNCH_Files/GoodwinCreek_"+x+"_"+y+".complete";
//////            outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Reservoir_Study/Soap_Creek/SoapCreek_"+x+"_"+y+".complete";
//////            outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Scaling_Study/Clear_Creek/ClearCreek_"+x+"_"+y+".complete";
//            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));////            
//            float[][] hortonOrders=mylinksAnalysis.getVarValues(4);//
//            metaBuffer.write(""+mylinksAnalysis.completeStreamLinksArray.length+"\n");
//            metaBuffer.write("\n");            
//            for (int i=0;i<mylinksAnalysis.completeStreamLinksArray.length;i++) {
//                metaBuffer.write(mylinksAnalysis.completeStreamLinksArray[i]+" "+hortonOrders[0][mylinksAnalysis.completeStreamLinksArray[i]]+"\n");
//                metaBuffer.write(mylinksAnalysis.completeStreamLinksArray[i]+"\n");
//            }
//            metaBuffer.write("\n");
//            metaBuffer.close();
////           
//           String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Radar_Hydrology/ASYNCH_Files/IowaRiver_"+x+"_"+y+".dist";
////            outputMetaFile="C:/Users/tayalew/Documents/CuencasDataBases/Goodwin_Creek_MS_database/ASYNCH_Files/GoodwinCreek_"+x+"_"+y+".dist";
//////            outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Reservoir_Study/Soap_Creek/SoapCreek_"+x+"_"+y+".dist";
//////            outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Scaling_Study2/Cedar_River/CedarRiver_"+x+"_"+y+".dist";
//            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
//            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
//                metaBuffer.write((i)+" "+longestDist[0][i]+" "+lenghts[0][i]+" "+topoDistToOut[0][i]+" "+distToOut[0][i]+" "+upAreas[0][i]+"\n");
//                metaBuffer.write("\n");
//            }
//            metaBuffer.write("\n");
//            metaBuffer.close();
            
            String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Radar_Hydrology/ASYNCH_Files/IowaRiver_"+x+"_"+y+".linksInfoCsv";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            metaBuffer.write("LinkID,hortonOrder,topoDistToOut,distToOut,upAreas,linkLength,linkArea"+ "\n");
            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
                metaBuffer.write((i)+","+hortonOrder[0][i]+","+topoDistToOut[0][i]+","+distToOut[0][i]+","+upAreas[0][i]+","+lenghts[0][i]+","+areas[0][i]+"\n");
//                metaBuffer.write("\n");
            }
            metaBuffer.write("\n");
            metaBuffer.close();
      
            
            
            //write lat long of links
//            String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Radar_Hydrology/IowaRiver_Links"+x+"_"+y+".latlong";
//            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile)); 
//            int numCols = mylinksAnalysis.localMetaRaster.getNumCols();
//            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) 
//            {            
//                int loctID  = mylinksAnalysis.contactsArray[i];                
//                int yy= loctID/numCols;
//                int xx= loctID%numCols;
//                double longitude = xx*mylinksAnalysis.localMetaRaster.getResLon()/3600.+mylinksAnalysis.localMetaRaster.getMinLon();
//                double latitude =  yy*mylinksAnalysis.localMetaRaster.getResLat()/3600.+mylinksAnalysis.localMetaRaster.getMinLat();
//                metaBuffer.write(i+","+longitude+","+latitude+","+distToOut[0][i]);
//                metaBuffer.write("\n");
//            }
//            metaBuffer.write("\n");
//            metaBuffer.close();  
         
            
//            int partitionCount = 200*200;
//            double xMin = mylinksAnalysis.localMetaRaster.getMinLon();
//            double xMax = mylinksAnalysis.localMetaRaster.getMaxLon();
//            double yMin = mylinksAnalysis.localMetaRaster.getMinLat();
//            double yMax = mylinksAnalysis.localMetaRaster.getMaxLat();
//            hydroScalingAPI.examples.Tools_by_Tibebu.partitionManager pm = new hydroScalingAPI.examples.Tools_by_Tibebu.partitionManager();
//            pm.setParams(xMin, xMax, yMin, yMax, partitionCount);
//            System.out.println(xMin+" "+xMax+" "+yMin+" "+yMax);
//            System.exit(0);
//            String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Scaling_Study2/Cedar_River/CedarRiver_"+x+"_"+y+"_"+partitionCount+"_partitions.csv";
//            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
//            
//            int numCols = mylinksAnalysis.localMetaRaster.getNumCols();
//            System.out.println("numCols= "+numCols);
////            metaBuffer.write("linkID"+","+"RainNoRain"+","+"longitude"+","+"latitude"+"\n");
//            metaBuffer.write("linkID"+","+"longitude"+","+"latitude"+","+"partitionID"+"\n");
//            for (int i=0;i<mylinksAnalysis.connectionsArray.length;i++) {
//            
//                int loctID  = mylinksAnalysis.contactsArray[i];
////                System.out.println("loctID= "+loctID+"  "+"I= "+i);
//                
//                int yy= loctID/numCols;
//                int xx= loctID%numCols;
//
//                double longitude = xx*mylinksAnalysis.localMetaRaster.getResLon()/3600.+mylinksAnalysis.localMetaRaster.getMinLon();
//                double latitude =  yy*mylinksAnalysis.localMetaRaster.getResLat()/3600.+mylinksAnalysis.localMetaRaster.getMinLat();
//                
//                
//               
////                double testYMin = mylinksAnalysis.localMetaRaster.getMinLat()+0.5*(mylinksAnalysis.localMetaRaster.getMaxLat()-mylinksAnalysis.localMetaRaster.getMinLat());
////                double testXMin = mylinksAnalysis.localMetaRaster.getMinLon()+0.5*(mylinksAnalysis.localMetaRaster.getMaxLon()-mylinksAnalysis.localMetaRaster.getMinLon());
////                metaBuffer.write(i+","+longitude+","+latitude+","+distToOut[0][i]);
//                metaBuffer.write(i+","+longitude+","+latitude+","+pm.getPartitionID(longitude, latitude));
//                metaBuffer.write("\n");
////                if(distToOut[0][i]<70){
////                    metaBuffer.write(i+1+","+1+","+longitude+","+latitude);
////                    metaBuffer.write("\n");}
////                else if(distToOut[0][i]<140){
////                    metaBuffer.write(i+1+","+2+","+longitude+","+latitude);
////                    metaBuffer.write("\n");}
////                else if(distToOut[0][i]<210){
////                    metaBuffer.write(i+1+","+3+","+longitude+","+latitude);
////                    metaBuffer.write("\n");}
////                else if(distToOut[0][i]<280){
////                    metaBuffer.write(i+1+","+4+","+longitude+","+latitude);
////                    metaBuffer.write("\n");}
////                else if(distToOut[0][i]<350){
////                    metaBuffer.write(i+1+","+5+","+longitude+","+latitude);
////                    metaBuffer.write("\n");}
////                else
////                {
////                   metaBuffer.write(i+1+","+6+","+longitude+","+latitude);
////                   metaBuffer.write("\n"); 
////                }
//            }
//            metaBuffer.write("\n");
//            metaBuffer.close();  
            
            



        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }
        
        
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main13_Tibebu(String args[]) {

        //        int x=23576; int y=2957; // Iowa River at Oakville using Iowa DEM (Ricardo)
        int x=23426; int y=2735; // Iowa River at Oakville using Iowa DEM (Tibebu)
//        x=55; y=114;
//        int x=906; int y=88; // Boone River
//        int x=2308; int y=199; // Old Mans Creek
//        int x=1570; int y=127; // Clear Creek
//        int x=7875; int y=1361; // Cedar River
//        x=2681; y=4153;//CID order 6 A=240
//        x=4882; y=3731; //CID order 8 A=2279
//        x=2100; y=4535; // CID order 5 A =64
//        int x=2480; int y=552; // Soap Creek
        
//        int flag=1;
//        int x= 7875; int y= 1361 ;// Basin Code 05464500 Cedar River at Cedar Rapids, IA
//        if(flag==2) {
//            x=2734;y=1069;
//        }

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

            
            java.io.File theFile=new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/IowaRiver_Database2/Rasters/Topography/fullIowaDem/fullIowaDem.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile); 
            metaModif.setLocationBinaryFile(new java.io.File("C:/Users/tayalew/Documents/CuencasDataBases/IowaRiver_Database2/Rasters/Topography/fullIowaDem/fullIowaDem.dir"));
            


            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();////
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x, y,matDirs,metaModif);
            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);  
            
            
            System.out.println(mylinksAnalysis.nextLinkArray.length);   
            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;
            int demNumCols=metaModif.getNumCols();

            String outputMetaFile="C:/Users/tayalew/Documents/Tibebu_Assignments/Radar_Hydrology/ASYNCH_Files/IowaRiver_"+x+"_"+y+".linkIDxy";
            java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));
            
            for (int i=0;i<mylinksAnalysis.contactsArray.length;i++){
                if (mylinksAnalysis.magnitudeArray[i] < mylinksAnalysis.basinMagnitude){

                    xOulet=mylinksAnalysis.contactsArray[i]%demNumCols;
                    yOulet=mylinksAnalysis.contactsArray[i]/demNumCols;

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++)
                    {
                    
                     
                    int yy= xyHillSlope[1][j];
                    int xx= xyHillSlope[0][j];

                    double longitude = xx*mylinksAnalysis.localMetaRaster.getResLon()/3600.+mylinksAnalysis.localMetaRaster.getMinLon();
                    double latitude =  yy*mylinksAnalysis.localMetaRaster.getResLat()/3600.+mylinksAnalysis.localMetaRaster.getMinLat();
                        
                      metaBuffer.write(i+","+longitude+","+latitude+"\n") ;

                    }
                }
                else 
                {
                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(x,y,matDirs,magnitudes,metaModif);
                    int[][] xyHillSlope=myHillActual.getXYHillSlope();
                    for (int j=0;j<xyHillSlope[0].length;j++)
                    {
                    
                     
                    int yy= xyHillSlope[1][j];
                    int xx= xyHillSlope[0][j];

                    double longitude = xx*mylinksAnalysis.localMetaRaster.getResLon()/3600.+mylinksAnalysis.localMetaRaster.getMinLon();
                    double latitude =  yy*mylinksAnalysis.localMetaRaster.getResLat()/3600.+mylinksAnalysis.localMetaRaster.getMinLat();
                        
                     metaBuffer.write(i+","+longitude+","+latitude+"\n") ;

                    }
                }
            }
            metaBuffer.close();

            
            

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);

    }    
    
    


}
