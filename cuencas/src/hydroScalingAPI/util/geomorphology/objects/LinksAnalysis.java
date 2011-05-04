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
            fileHorton.seek(contactsArray[i]);
            myOrder=fileHorton.readByte();
            basinOrder=Math.max(basinOrder,myOrder);
            if (nextLinkArray[i] > 0){
                fileHorton.seek(contactsArray[nextLinkArray[i]]);
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
        distToInclue.add(new float[] {dToOutlet[0][outlet],dToOutlet[1][outlet]});
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
            distToInclue.add(new float[] {dToOutlet[0][connectionsArray[outlet][i]],dToOutlet[1][connectionsArray[outlet][i]]});
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
                System.out.println("outlets" + k+ " order length = " + order[0].length + "wfunclength = "+wFunc[metric].length);
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
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    for (int j=0;j<connectionsArray[i].length;j++){
                        fileQuantity.seek(4*contactsArray[connectionsArray[i][j]]);
                        quantityArray[0][i]-=fileQuantity.readFloat();
                    }
                }

                break;
            case 1:
                //Link's Length.  This is done by subtraction of tcl at head and tcl at incoming links head

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    for (int j=0;j<connectionsArray[i].length;j++){
                        fileQuantity.seek(4*contactsArray[connectionsArray[i][j]]);
                        quantityArray[0][i]-=fileQuantity.readFloat();
                    }
                }

                break;
            case 2:
                //Link's Upstream area.

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                }

                break;
            case 3:
                //Link's drop

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(8*headsArray[i]);
                    double altUp=fileQuantity.readDouble();
                    fileQuantity.seek(8*tailsArray[i]);
                    quantityArray[0][i]=(float) (altUp-fileQuantity.readDouble());
                }

                break;
            case 4:
                //Link's order

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(headsArray[i]);
                    quantityArray[0][i]=(float) fileQuantity.readByte();
                }

                break;
            case 5:
                //Total Channel Length

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=(float) fileQuantity.readFloat();
                }

                break;
            case 6:
                //Link's Magnitude

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readInt();
                }

                break;
            case 7:
                //Link's Distance to Outlet
                float GoToB;

                if(basin != null){
                    fileQuantity.seek(4*basin.getOutletID());
                    GoToB=fileQuantity.readFloat();
                } else {
                    GoToB=0;
                }
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*headsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat()-GoToB;
                }

                break;
            case 8:
                //Link's Topologic Distance to Outlet
                int ToToB;

                if(basin != null){
                    fileQuantity.seek(4*basin.getOutletID());
                    ToToB=fileQuantity.readInt();
                } else {
                    ToToB=0;
                }
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*tailsArray[i]);
                    quantityArray[0][i]=fileQuantity.readInt()-ToToB+1;
                }

                break;
            case 9:
                //Link's Slope

                float[][] linkLengths=getVarValues(1);

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(8*headsArray[i]);
                    double altUp=fileQuantity.readDouble();
                    fileQuantity.seek(8*tailsArray[i]);
                    quantityArray[0][i]=(float) (altUp-fileQuantity.readDouble());

                    quantityArray[0][i]/=linkLengths[0][i]*1000;
                }

                break;
            case 10:
                //Link's Elevation

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(8*headsArray[i]);
                    quantityArray[0][i]=(float) (fileQuantity.readDouble());
                }

                break;
            case 11:
                //Longest Channel Length

                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*contactsArray[i]);
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
                    fileQuantity.seek(4*contactsArray[i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    if(quantityArray[0][i] == 0) quantityArray[0][i]=1E-5f;
                }

                break;
            case 14:
                // Links Upstream Area at Head
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*headsArray[i]);
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

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        //main0(args);  //An anlysis of topography
        //main1(args);
        //main2(args);
        //main3(args);
        //main5(args);
        //main4(args);
        main6(args);  // Writing connectivity for a DEM and map of hillslopes
        //main6_1(args);  // Writing connectivity for a DEM and map of hillslopes
        //main7(args);  //Writing connectivity for Clear Creek to share with The Mathematicians
        //main8(args);  //Writing connectivity for Cedar River at Cedar Rapids to share with The Mathematicians
        //main9(args);  // Writing connectivity for a DEM including all the embedded basins... Multiple outlet concept
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
            //java.io.File theFile=new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.metaDEM");
            java.io.File theFile=new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/CedarRiver.metaDEM");
     
 //java.io.File theFile = new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/90meterc1.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/CedarRiver.dir"));
            // metaModif.setLocationBinaryFile(new java.io.File("/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/ASTER/astercc.dir"));
            
          //  int xOut=1596;
          //  int yOut=298;


             //int xOut=2885;
            //int yOut=690; // Iowa River at Iowa City
//            int xOut=2817;
//            int yOut=713; //90METER - Clear Creek - coralville
//            int xOut = 8052;
//            int yOut = 497;//5METERDEMClear Creek - coralville
//            int xOut = 4025;
//            int yOut = 244;//10METERDEMClear Creek - coralville
//            int xOut = 2013;
//            int yOut = 122;//20METERDEMClear Creek - coralville
//            int xOut = 1341;
//            int yOut = 82;//30METERDEMClear Creek - coralville
//            int xOut = 670;
//            int yOut = 41;//60METERDEMClear Creek - coralville
          //nt xOut = 2817;
          //  int yOut = 713;//90METERDEMClear Creek - coralville
//
            int xOut=1409;
            int yOut=356; // Cedar Rapids
//             xOut =3053;
//            yOut =2123;// turkey
//            xOut =2885;
//            yOut =690; //Iowa City
//             xOut =1932;
//            yOut =1695;// Waterloo
//                   xOut = 1775;
//            yOut = 1879; //Janesville
//                xOut = 3091;
//                    yOut = 2004; // Volga
           xOut = 5468;
                    yOut = 3237;
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(xOut,yOut,matDirs,metaModif);

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(laCuenca, metaModif, matDirs);

            java.io.File theFile1=new java.io.File("/scratch/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/linksAnalyses/"+"linksInfo" + xOut +"_" + yOut+".csv");
            java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile1);
            java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
            java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);
            System.out.println("mylinksAnalysis.contactsArray.length = " + mylinksAnalysis.contactsArray.length);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(mylinksAnalysis);

        float[][] bigDtoO=mylinksAnalysis.getDistancesToOutlet();

        int nc=metaModif.getNumCols();
        int nr=metaModif.getNumRows();
        double res=metaModif.getResLat();
        double minLat=metaModif.getMinLat();
        double minLon=metaModif.getMinLon();
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
                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_large_cities.log",
                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_medium_cities.log",
                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_small_cities.log",
                            "/Users/ricardo/workFiles/myWorkingStuff/AdvisorThesis/Eric/res_usgs_gauges.log",
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

            String outputMetaFile="/Users/ricardo/temp/NextLinkClearCreek.txt";
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

        /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main8(String args[]) {

        //int x= 2734; int y= 1069 ;// Basin Code 05464500 Cedar River at Cedar Rapids, IA
        //int x= 3316; int y= 116 ;// Basin Code 05465500 Iowa River at Wapello, IA
        int x= 2115; int y= 801 ;// Basin Code 05453000 Big Bear Creek at Ladora, IA

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

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

    public static void main9(String args[]) {

        java.text.NumberFormat number2 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint2 = (java.text.DecimalFormat)number2;
        dpoint2.applyPattern("0.00000000");

        try{

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

//            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.metaDEM");
//            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
//            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/DryCreek/NED_79047246.dir"));

            java.io.File theFile=new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
            metaModif.setFormat("Integer");
            int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();

            LinksAnalysis mylinksAnalysis=new LinksAnalysis(metaModif, matDirs);

            float[][] upAreas=mylinksAnalysis.getVarValues(2);
            float[][] areas=mylinksAnalysis.getVarValues(0);
            float[][] lenghts=mylinksAnalysis.getVarValues(1);

            //String outputMetaFile="/Users/ricardo/temp/IowaConnectivity.txt";
            String outputMetaFile="/Users/ricardo/temp/GoodwinConnectivity.txt";
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




}
