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
 * RsnStructure.java
 *
 * Created on July 11, 2005, 9:41 AM
 */

package hydroScalingAPI.util.randomSelfSimilarNetworks;

import java.io.IOException;

/**
 * Manages information related to the topologic and geometric information associated
 * to a Random Self-similar Network.
 * @author Ricardo Mantilla
 */
public class RsnStructure {
    
    private hydroScalingAPI.util.randomSelfSimilarNetworks.Generator rsnTree;
    private String[] rsnTreeDecoding;
    private int rsnTreeSize,rsnDepth;
    
    private int[][] connStruc;
    private int[] nextLink,completeStreamLinksArray;
    
    private boolean randomGeometry=false;
    private float[][] upAreas, linkOrders, upLength,linkAreas,linkLengths,longestLenght;
    private int[] magnitudes;
    
    private int minimumIntegerDigits=4;
    
    /**
     * Creates a new instance of RsnStructure
     * @param generations The number of generations of the RSN (number of recursive replacements)
     * @param myDis_I A random variable with and associated probability distribution of the number of descendents
     * of interior nodes
     * @param myDis_E A random variable with and associated probability distribution of the number of descendents
     * of exterioir nodes
     * @param myDis_Areas_E A random variable with and associated probability distribution of the catchment
     * area associated to each link in the RSN
     * @param myDis_Areas_I A random variable with and associated probability distribution of the geometric
     * length associated to each link in the RSN
     */
    public RsnStructure(int generations, hydroScalingAPI.util.probability.DiscreteDistribution myDis_I, 
                                         hydroScalingAPI.util.probability.DiscreteDistribution myDis_E, 
                                         hydroScalingAPI.util.probability.ContinuousDistribution myDis_Areas_E,
                                         hydroScalingAPI.util.probability.ContinuousDistribution myDis_Areas_I) {
        
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(minimumIntegerDigits);
        
        rsnDepth=generations;
        rsnTree=new hydroScalingAPI.util.randomSelfSimilarNetworks.Generator(0,rsnDepth,myDis_I,myDis_E,labelFormat.format(0));
        rsnTreeDecoding=rsnTree.decodeRsnTree();
        
        randomGeometry=true;
        linkAreas=new float[1][rsnTreeDecoding.length];
        linkLengths=new float[1][rsnTreeDecoding.length];
        
        java.util.Random rn=new java.util.Random();
        
        for(int i=0;i<rsnTreeDecoding.length;i++){
            if(rsnTreeDecoding[i].contains("E")){
                linkAreas[0][i]=myDis_Areas_E.sample();
                linkLengths[0][i]=(float)(1.31*Math.pow(linkAreas[0][i],0.63)*Math.exp(rn.nextGaussian()*0.7));//1.31,0.63,0.7
            } else{
                linkAreas[0][i]=myDis_Areas_I.sample();
                linkLengths[0][i]=(float)(1.17*Math.pow(linkAreas[0][i],0.55)*Math.exp(rn.nextGaussian()*0.5));//1.17,0.55,0.5
            }
        }
        
        grabRsnStructure();
    }
    
    /**
     * Creates a new instance of RsnStructure. It is assumed that all links have geometric lenght = 0.3 km
     * and catchment area = 0.1 km^2
     * @param generations The number of generations of the RSN (number of recursive replacements)
     * @param myDis_I A random variable with and associated probability distribution of the number of descendents
     * of interior nodes
     * @param myDis_E A random variable with and associated probability distribution of the number of descendents
     * of exterioir nodes
     */
    public RsnStructure(int generations, hydroScalingAPI.util.probability.DiscreteDistribution myDis_I, hydroScalingAPI.util.probability.DiscreteDistribution myDis_E) {
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(minimumIntegerDigits);
        
        rsnDepth=generations;
        rsnTree=new hydroScalingAPI.util.randomSelfSimilarNetworks.Generator(0,rsnDepth,myDis_I,myDis_E,labelFormat.format(0));
        rsnTreeDecoding=rsnTree.decodeRsnTree();
        
        linkAreas=new float[1][rsnTreeDecoding.length];
        linkLengths=new float[1][rsnTreeDecoding.length];
        
        java.util.Arrays.fill(linkAreas[0],0.10f);
        java.util.Arrays.fill(linkLengths[0],0.30f);
        
        grabRsnStructure();
    }
    
    /**
     * Creates a new instance of RsnStructure. It is assumed that all links have geometric lenght = 0.3 km
     * and catchment area = 0.1 km^2
     * @param generations The number of generations of the RSN (number of recursive replacements)
     * @param myDis_I A random variable with and associated probability distribution of the number of descendents
     * of interior nodes
     * @param myDis_E A random variable with and associated probability distribution of the number of descendents
     * of exterioir nodes
     */
    public RsnStructure(int generations, hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myDis_I, hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myDis_E) {
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(minimumIntegerDigits);
        
        rsnDepth=generations;
        rsnTree=new hydroScalingAPI.util.randomSelfSimilarNetworks.Generator(0,rsnDepth,myDis_I,myDis_E,labelFormat.format(0));
        rsnTreeDecoding=rsnTree.decodeRsnTree();
        
        linkAreas=new float[1][rsnTreeDecoding.length];
        linkLengths=new float[1][rsnTreeDecoding.length];
        
        java.util.Arrays.fill(linkAreas[0],0.10f);
        java.util.Arrays.fill(linkLengths[0],0.30f);
        
        grabRsnStructure();
    }
    
    /**
     * Creates a new instance of RsnStructure previously stored in a file
     * @param theFile The file where the RSN structure is stored
     */
    public RsnStructure(java.io.File theFile) {
        try{
            java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(theFile));
            
            minimumIntegerDigits=Integer.parseInt(fileMeta.readLine().split(":")[1].trim());
            rsnDepth=Integer.parseInt(fileMeta.readLine().split(":")[1].trim());
            
            String tmp=""; int treeCounter=0;
            while(tmp != null){
                if(tmp.equalsIgnoreCase("Random Geometry")) break;
                tmp=fileMeta.readLine();
                treeCounter++;
            }
            treeCounter--;
            fileMeta.close();
            
            rsnTreeDecoding=new String[treeCounter];

            fileMeta = new java.io.BufferedReader(new java.io.FileReader(theFile));
            fileMeta.readLine();
            fileMeta.readLine();
            for(int i=0;i<treeCounter;i++){
                rsnTreeDecoding[i]=fileMeta.readLine();
            }
            
            tmp=fileMeta.readLine();
            linkAreas=new float[1][treeCounter];
            linkLengths=new float[1][treeCounter];
            
            java.util.Arrays.fill(linkAreas[0],0.10f);
            java.util.Arrays.fill(linkLengths[0],0.30f);

            if(tmp != null && tmp.equalsIgnoreCase("Random Geometry")){
                randomGeometry=true;
                fileMeta.readLine();
                tmp=fileMeta.readLine();
                String[] theInfo=tmp.split(",");
                for(int i=0;i<treeCounter;i++) linkAreas[0][i]=Float.parseFloat(theInfo[i]);
                fileMeta.readLine();
                tmp=fileMeta.readLine();
                theInfo=tmp.split(",");
                for(int i=0;i<treeCounter;i++) linkLengths[0][i]=Float.parseFloat(theInfo[i]);
            }
            
            fileMeta.close();
            //rsnDepth=generations;
            //rsnTreeDecoding=rsnTree.decodeRsnTree();
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
        
        grabRsnStructure();
    }
    
    private void grabRsnStructure() {
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(minimumIntegerDigits);
        
        rsnTreeSize=rsnTreeDecoding.length;
        
        String[] linkCode;
        String[] incomingCode1,incomingCode2;
        String codeToSearch1_I,codeToSearch1_E,codeToSearch2_I,codeToSearch2_E;
        connStruc=new int[rsnTreeSize][];
        for(int i=0;i<rsnTreeSize;i++){
            connStruc[i]=new int[0];
            linkCode=rsnTreeDecoding[i].split(",");
            if(linkCode[rsnDepth+1].equalsIgnoreCase("I")) {
                connStruc[i]=new int[2];
                //Search for the conecting link
                int level=0;
                do{
                    incomingCode1=(String[])linkCode.clone();
                    incomingCode2=(String[])linkCode.clone();
                    
                    for(int j=0;j<level;j++){
                        incomingCode1[rsnDepth-j]=labelFormat.format(0);
                        incomingCode2[rsnDepth-j]=labelFormat.format(0);
                    }
                    incomingCode1[rsnDepth-level]=labelFormat.format(Integer.parseInt(linkCode[rsnDepth-level])+1);
                    incomingCode2[rsnDepth-level]=labelFormat.format(Integer.parseInt(linkCode[rsnDepth-level])+2);
                    
                    codeToSearch1_I=labelFormat.format(0);
                    codeToSearch1_E=labelFormat.format(0);
                    
                    codeToSearch2_I=labelFormat.format(0);
                    codeToSearch2_E=labelFormat.format(0);
                    
                    for(int k=1;k<incomingCode1.length-1;k++){
                        codeToSearch1_I+=(","+incomingCode1[k]);
                        codeToSearch1_E+=(","+incomingCode1[k]);
                        codeToSearch2_I+=(","+incomingCode2[k]);
                        codeToSearch2_E+=(","+incomingCode2[k]);
                    }
                    
                    codeToSearch1_I+=",I";
                    codeToSearch1_E+=",E";
                    codeToSearch2_I+=",I";
                    codeToSearch2_E+=",E";
                    
                    connStruc[i][0]=java.util.Arrays.binarySearch(rsnTreeDecoding,codeToSearch1_I);
                    connStruc[i][0]=Math.max(connStruc[i][0],java.util.Arrays.binarySearch(rsnTreeDecoding,codeToSearch1_E));
                    
                    level++;

                } while (connStruc[i][0] < 0);

                connStruc[i][1]=java.util.Arrays.binarySearch(rsnTreeDecoding,codeToSearch2_I);
                connStruc[i][1]=Math.max(connStruc[i][1],java.util.Arrays.binarySearch(rsnTreeDecoding,codeToSearch2_E));
                
                //System.out.println("Links "+connStruc[i][0]+" and "+connStruc[i][1]+" are connected to "+i);
                
            }
        }
        
        
        nextLink=new int[rsnTreeSize];
        nextLink[0]=-1;
        for(int i=0;i<rsnTreeSize;i++){
            for (int j=0;j<connStruc[i].length;j++){
                nextLink[connStruc[i][j]]=i;
            }
        }
        
        
        
        java.util.Vector trackPath=new java.util.Vector();
        upAreas=new float[1][rsnTreeSize];
        linkOrders=new float[1][rsnTreeSize];
        upLength=new float[1][rsnTreeSize];
        magnitudes=new int[rsnTreeSize];
        longestLenght=new float[1][rsnTreeSize];
        
        for(int i=0;i<rsnTreeSize;i++){
            linkCode=rsnTreeDecoding[i].split(",");
            upAreas[0][i]=linkAreas[0][i];
            upLength[0][i]=linkLengths[0][i];
            longestLenght[0][i]=linkLengths[0][i];
            if(linkCode[rsnDepth+1].equalsIgnoreCase("E")) {
                trackPath.add(""+i);
                linkOrders[0][i]=1;
                magnitudes[i]=1;
            }
        }
        
        
        while(trackPath.size() > 0){
            int i=0, nEl=trackPath.size();
            
            java.util.Vector trackPathTemp=new java.util.Vector();
            
            for(int j=0;j<nEl;j++){
                int toAssign=Integer.parseInt(trackPath.get(j).toString());
                if(nextLink[toAssign] != -1){
                    
                    upAreas[0][nextLink[toAssign]]+=upAreas[0][toAssign];
                    upLength[0][nextLink[toAssign]]+=upLength[0][toAssign];
                    longestLenght[0][nextLink[toAssign]]=Math.max(longestLenght[0][nextLink[toAssign]],longestLenght[0][toAssign]+linkLengths[0][nextLink[toAssign]]);
                    magnitudes[nextLink[toAssign]]+=magnitudes[toAssign];
                    
                    if(linkOrders[0][nextLink[toAssign]] == linkOrders[0][toAssign])
                        linkOrders[0][nextLink[toAssign]]+=1;
                    else
                        linkOrders[0][nextLink[toAssign]]=Math.max(linkOrders[0][toAssign],linkOrders[0][nextLink[toAssign]]);

                    if (magnitudes[nextLink[toAssign]] > magnitudes[toAssign]) {
                        trackPathTemp.add(""+nextLink[toAssign]);
                    }
                }
            }

            trackPath=(java.util.Vector)trackPathTemp.clone();

        }
        
        java.util.Vector linksCompletos=new java.util.Vector();
        
        int myOrder,frontOrder;
        
        linksCompletos.addElement(new int[] {0});
        for(int i=1;i<nextLink.length;i++){
            myOrder=(int)linkOrders[0][i];
            frontOrder=(int)linkOrders[0][nextLink[i]];
            if (frontOrder>myOrder){
                linksCompletos.addElement(new int[] {i});
            }
        }
        
        completeStreamLinksArray=new int[linksCompletos.size()];
        for(int i=0;i<linksCompletos.size();i++){
            completeStreamLinksArray[i]=((int[]) linksCompletos.get(i))[0];
        }
    }
    
    /**
     * Returns the Strahler order of the RSN
     * @return The Strahler order of the network
     */
    public int getNetworkOrder(){
        return rsnDepth+1;
    }
    
    /**
     * Returns the array describing the topologic connection structure of the links in
     * the network
     * @return An int[numLinks][numTributaries] 
     */
    public int[][] getConnectionStructure(){
        return connStruc;
    }
    
    /**
     * Returns an array with the index of the downstream link
     * @return An int[numLinks]
     */
    public int[] getNextLinkArray(){
        return nextLink;
    }
    
    /**
     * Returns an array with the links Shreve's magnitude
     * @return An int[numLinks]
     */
    public int[] getMagnitudes(){
        return magnitudes;
    }
    
    /**
     * Returns an array with the links catchment area
     * @return An float[1][numLinks]
     */
    public float[][] getLinkAreas(){
        return linkAreas;
    }
    
    /**
     * Returns an array with the links upstream area
     * @return An float[1][numLinks]
     */
    public float[][] getUpAreas(){
        return upAreas;
    }
    
    /**
     * Returns an array with the links geometric length
     * @return An float[1][numLinks]
     */
    public float[][] getLinkLengths(){
        return linkLengths;
    }
    
    /**
     * Returns an array with the links upstream total length
     * @return An float[1][numLinks]
     */
    public float[][] getUpLength(){
        return upLength;
    }
    
    /**
     * Returns an array with the links Strahler order
     * @return An float[1][numLinks]
     */
    public float[][] getHortonOrders(){
        return linkOrders;
    }
    
    /**
     * Returns an array with the links upstream longest channel lenght 
     * @return An float[1][numLinks]
     */
    public float[][] getLongestLength(){
        return longestLenght;
    }
    
    /**
     * Returns an array with the index of the links that correspond to the bottom of
     * complete order streams 
     * @return An int[numCompleteStreams]
     */
    public int[] getCompleteStreamLinksArray(){
        return completeStreamLinksArray;
    }
    
    /**
     * Writes the RSN topologic and geometric structure to a file for later retreival 
     * and reconstruction
     */
    public void writeRsnTreeDecoding(java.io.File theFile) throws java.io.IOException{
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

        newfile.write("Digits Per Code: "+minimumIntegerDigits+"\n");
        newfile.write("Tree Depth: "+rsnDepth+"\n");
        for(int i=0;i<rsnTreeDecoding.length;i++) newfile.write(rsnTreeDecoding[i]+"\n");
        if(randomGeometry){
            newfile.write("Random Geometry\n");
            newfile.write("Link Area [km^2]");
            for(int i=0;i<rsnTreeDecoding.length-1;i++) newfile.write(linkAreas[0][i]+",");
            newfile.write(linkAreas[0][rsnTreeDecoding.length-1]+"\n");
            newfile.write("Link Length [km]");
            for(int i=0;i<rsnTreeDecoding.length-1;i++) newfile.write(linkLengths[0][i]+",");
            newfile.write(linkLengths[0][rsnTreeDecoding.length-1]+"\n");
        }
        newfile.close();
        bufferout.close();
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        //main0(args); //Test features of this Object
        //main1(args); //Test write-read feature
        //main2(args); //Test Dd vs A relationship for RSNs
        //main3(args); //Read Info from Embeded trees
        //main4(args); //Non-self-similar trees
        main5(args); //Networks for Scott's code
    }
    
    private static void main0(String[] args) {
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeneralGeometricDistribution(0.59062127,0.25756657, 0);
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeneralGeometricDistribution(0.57253316,0.19803630, 1);
        RsnStructure myResults=new RsnStructure(3,myUD_I,myUD_E);
        
        float[][] orders=myResults.getHortonOrders();
        int[][] connSt=myResults.getConnectionStructure();
        for (int i=0;i<connSt.length;i++){
            System.out.print("["+i+","+orders[0][i]);
            if (connSt[i].length == 0) System.out.print(",0,0");
            for (int j=0;j<connSt[i].length;j++){
                System.out.print(","+connSt[i][j]);
            }
            System.out.println("],$");
        }
        
        System.exit(0);
        
        float[][] data=myResults.getHortonOrders();
        System.out.println(myResults.getNextLinkArray().length);
        for (int j=0;j<data[0].length;j++){
            if(j%50 == 0)System.out.println();
            System.out.print(data[0][j]+" ");
        }
        
        System.out.println();
        int[] data1=myResults.getCompleteStreamLinksArray();
        for (int j=0;j<data1.length;j++){
            if(j%50 == 0)System.out.println();
            System.out.print(data1[j]+" ");
        }
        System.exit(0);
    }
    
    private static void main1(String[] args) {
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeneralGeometricDistribution(0.59062127,0.25756657, 0);
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeneralGeometricDistribution(0.57253316,0.19803630, 1);
        RsnStructure myRsnStruc=new RsnStructure(15 ,myUD_I,myUD_E);
        
        java.io.File theFile=new java.io.File("/Users/ricardo/temp/testRSNdecode.rsn");
        
        try{
            myRsnStruc.writeRsnTreeDecoding(theFile);
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
        
        RsnStructure myResults=new RsnStructure(theFile);
        
        float[][] orders=myResults.getHortonOrders();
        int[][] connSt=myResults.getConnectionStructure();
        for (int i=0;i<connSt.length;i++){
            System.out.print("["+i+","+orders[0][i]);
            if (connSt[i].length == 0) System.out.print(",0,0");
            for (int j=0;j<connSt[i].length;j++){
                System.out.print(","+connSt[i][j]);
            }
            System.out.println("],$");
        }
        
        System.exit(0);
        
        System.exit(0);
    }
    
    private static void main2(String[] args) {
         
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(0.42,0);
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(0.49,1);

        float Elae=0.1f;
        float SDlae=0.2f;

        hydroScalingAPI.util.probability.ContinuousDistribution myLinkAreaDistro_E=new hydroScalingAPI.util.probability.LogGaussianDistribution(Elae,SDlae);
        hydroScalingAPI.util.probability.ContinuousDistribution myLinkAreaDistro_I=new hydroScalingAPI.util.probability.LogGaussianDistribution(0.01f+0.88f*Elae,0.04f+0.85f*SDlae);

        RsnStructure myRsnStruc=new RsnStructure(7,myUD_I,myUD_E, myLinkAreaDistro_E, myLinkAreaDistro_I);
        //RsnStructure myRsnStruc=new RsnStructure(7,myUD_I,myUD_E);
        
        float[][] upAreas=myRsnStruc.getUpAreas();
        float[][] upLength=myRsnStruc.getUpLength();
        float[][] upOrder=myRsnStruc.getHortonOrders();
        float[][] longLength=myRsnStruc.getLongestLength();
        
        int[] compLinks=myRsnStruc.getCompleteStreamLinksArray();
        
        float[] averDd=new float[myRsnStruc.getNetworkOrder()];
        float[] ordCount=new float[myRsnStruc.getNetworkOrder()];
        
        for (int i=0;i<compLinks.length;i++){
            if(upOrder[0][compLinks[i]]>0)System.out.println(i+" "+upAreas[0][compLinks[i]]+" "+upLength[0][compLinks[i]]+" "+upOrder[0][compLinks[i]]+" "+longLength[0][compLinks[i]]);
            averDd[(int)upOrder[0][compLinks[i]]-1]+=upLength[0][compLinks[i]]/upAreas[0][compLinks[i]];
            ordCount[(int)upOrder[0][compLinks[i]]-1]++;
        }
//        System.out.println("DD vs Order");
//        for (int i = 0; i < averDd.length; i++) {
//            averDd[i]/=ordCount[i];
//            System.out.println((i+1)+" "+averDd[i]);
//        }
        
    }
    
    private static void main3(String[] args) {
        
        //java.io.File theFile=new java.io.File("/Users/ricardo/workFiles/PhD Thesis Results/E1I1/ord_4/Modified_RSN_result-SN_0.0.rsn.csv");
        //java.io.File theFile=new java.io.File("/Users/ricardo/workFiles/PhD Thesis Results/E1I1/ord_4/RSN_result-SN_0.0.rsn.csv");
        
        //java.io.File theFile=new java.io.File("/Users/ricardo/workFiles/PhD Thesis Results/RSN_Data/ord_4/Modified_RSN_result-SN_14.0.rsn.csv");
        java.io.File theFile=new java.io.File("/Users/ricardo/workFiles/PhD Thesis Results/RSN_Data/ord_4/RSN_result-SN_14.0.rsn.csv");

        RsnStructure myRsnStruc=new RsnStructure(theFile);
        
        float[][] upAreas=myRsnStruc.getUpAreas();
        float[][] upLength=myRsnStruc.getUpLength();
        float[][] upOrder=myRsnStruc.getHortonOrders();
        float[][] longLength=myRsnStruc.getLongestLength();
        
        int[] compLinks=myRsnStruc.getCompleteStreamLinksArray();
        
        for (int i=0;i<compLinks.length;i++){
            System.out.println(i+" "+upAreas[0][compLinks[i]]+" "+upLength[0][compLinks[i]]+" "+upOrder[0][compLinks[i]]+" "+longLength[0][compLinks[i]]);
        }
        
        System.exit(0);
    }
    
    private static void main4(String[] args) {
        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.ScaleDependentBinaryDistribution(1,1, 1,3);
        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.ScaleDependentBinaryDistribution(1,1, 1,3);
        RsnStructure myRsnStruc=new RsnStructure(3,myUD_I,myUD_E);
        
        java.io.File theFile=new java.io.File("/Users/ricardo/temp/testRSNdecode.rsn");
        
        try{
            myRsnStruc.writeRsnTreeDecoding(theFile);
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
        
        RsnStructure myResults=new RsnStructure(theFile);
        
        float[][] orders=myResults.getHortonOrders();
        int[][] connSt=myResults.getConnectionStructure();
        for (int i=0;i<connSt.length;i++){
            System.out.print("["+i+","+orders[0][i]);
            if (connSt[i].length == 0) System.out.print(",0,0");
            for (int j=0;j<connSt[i].length;j++){
                System.out.print(","+connSt[i][j]);
            }
            System.out.println("],$");
        }
        
        System.exit(0);
        
        System.exit(0);
    }
    
    private static void main5(String[] args) throws java.io.IOException{
        
        for(double pe=0.44;pe<=0.53;pe+=0.02){
            for(double pi=0.35;pi<=0.48;pi+=0.02){
        
        
                String fileNameBase="/Users/ricardo/simulationResults/geometricRSNs/variableL/network_"+(float)pe+"_"+(float)pi+".";

                hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(pi,0);
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(pe,1);

                float Elae=0.1f;
                float SDlae=0.2f;

                hydroScalingAPI.util.probability.ContinuousDistribution myLinkAreaDistro_E=new hydroScalingAPI.util.probability.LogGaussianDistribution(Elae,SDlae);
                hydroScalingAPI.util.probability.ContinuousDistribution myLinkAreaDistro_I=new hydroScalingAPI.util.probability.LogGaussianDistribution(0.01f+0.88f*Elae,0.04f+0.85f*SDlae);

                RsnStructure myRsnStruc=new RsnStructure(8,myUD_I,myUD_E, myLinkAreaDistro_E, myLinkAreaDistro_I);

                String fileAscSalida=fileNameBase+"rvr";

                java.io.File outputMetaFile=new java.io.File(fileAscSalida);

                java.io.BufferedWriter metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                metaBuffer.write(myRsnStruc.linkAreas[0].length+"\n\n");

                int[][] connSt=myRsnStruc.getConnectionStructure();
                for (int i=0;i<connSt.length;i++){

                    metaBuffer.write((i+1)+"\n");
                    metaBuffer.write(""+connSt[i].length);
                    for (int j=0;j<connSt[i].length;j++){
                        metaBuffer.write(" "+(connSt[i][j]+1));
                    }
                    metaBuffer.write("\n\n");
                }

                metaBuffer.close();

                fileAscSalida=fileNameBase+"prm";
                outputMetaFile=new java.io.File(fileAscSalida);
                metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                metaBuffer.write(myRsnStruc.linkAreas[0].length+"\n\n");


                float[][] upAreas=myRsnStruc.getUpAreas();
                float[][] hillAreas=myRsnStruc.getLinkAreas();
                float[][] linkLenghts=myRsnStruc.getLinkLengths();

                for (int i=0;i<upAreas[0].length;i++){
                    metaBuffer.write((i+1)+"\n");
                    metaBuffer.write(upAreas[0][i]+" "+linkLenghts[0][i]+" "+hillAreas[0][i]+"\n\n");
                }

                metaBuffer.close();

                fileAscSalida=fileNameBase+"sav";
                outputMetaFile=new java.io.File(fileAscSalida);
                metaBuffer = new java.io.BufferedWriter(new java.io.FileWriter(outputMetaFile));

                int[] compLinks=myRsnStruc.getCompleteStreamLinksArray();
                float[][] linkOrder=myRsnStruc.getHortonOrders();


                int OrderToPrint=myRsnStruc.getNetworkOrder();
                for (int i=0;i<compLinks.length;i++){
                    if(linkOrder[0][compLinks[i]] == OrderToPrint) {
                        metaBuffer.write((compLinks[i]+1)+" ");
                        OrderToPrint--;
                    }
                }

                metaBuffer.close();
            }
        }
        
    }
    
}
