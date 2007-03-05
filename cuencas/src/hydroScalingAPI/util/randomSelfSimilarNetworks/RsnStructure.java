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

/**
 *
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
    
    /** Creates a new instance of RsnStructure */
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
                linkLengths[0][i]=(float)(1.31*Math.pow(linkAreas[0][i],0.63)*Math.exp(rn.nextGaussian()*0.7));
            } else{
                linkAreas[0][i]=myDis_Areas_I.sample();
                linkLengths[0][i]=(float)(1.17*Math.pow(linkAreas[0][i],0.55)*Math.exp(rn.nextGaussian()*0.5));
            }
        }
        
        grabRsnStructure();
    }
        
    public void grabRsnStructure() {
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
    
    public int getNetworkOrder(){
        return rsnDepth+1;
    }
    
    public int[][] getConnectionStructure(){
        return connStruc;
    }
    
    public int[] getNextLinkArray(){
        return nextLink;
    }
    
    public int[] getMagnitudes(){
        return magnitudes;
    }
    
    public float[][] getLinkAreas(){
        return linkAreas;
    }
    
    public float[][] getUpAreas(){
        return upAreas;
    }
    
    public float[][] getLinkLengths(){
        return linkLengths;
    }
    
    public float[][] getUpLength(){
        return upLength;
    }
    
    public float[][] getHortonOrders(){
        return linkOrders;
    }
    
    public float[][] getLongestLength(){
        return longestLenght;
    }
    
    public int[] getCompleteStreamLinksArray(){
        return completeStreamLinksArray;
    }
    
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
            for(int i=0;i<rsnTreeDecoding.length-1;i++) newfile.write(linkAreas[i]+",");
            newfile.write(linkAreas[rsnTreeDecoding.length-1]+"\n");
            newfile.write("Link Length [km]");
            for(int i=0;i<rsnTreeDecoding.length-1;i++) newfile.write(linkLengths[i]+",");
            newfile.write(linkLengths[rsnTreeDecoding.length-1]+"\n");
        }
        newfile.close();
        bufferout.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //main0(args); //Test features of this Object
        //main1(args); //Test write-read feature
        //main2(args); //Test Dd vs A relationship for RSNs
        main3(args); //Read Info from Embeded trees
    }
    
    public static void main0(String[] args) {
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
    
    public static void main1(String[] args) {
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeneralGeometricDistribution(0.59062127,0.25756657, 0);
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeneralGeometricDistribution(0.57253316,0.19803630, 1);
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
    
    public static void main2(String[] args) {
         
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(0.42,0);
        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(0.49,1);

        float Elae=0.1f;
        float SDlae=0.2f;

        hydroScalingAPI.util.probability.ContinuousDistribution myLinkAreaDistro_E=new hydroScalingAPI.util.probability.LogGaussianDistribution(Elae,SDlae);
        hydroScalingAPI.util.probability.ContinuousDistribution myLinkAreaDistro_I=new hydroScalingAPI.util.probability.LogGaussianDistribution(0.01f+0.88f*Elae,0.04f+0.85f*SDlae);
                        
        //RsnStructure myRsnStruc=new RsnStructure(5,myUD_I,myUD_E, myLinkAreaDistro_E, myLinkAreaDistro_I);
        RsnStructure myRsnStruc=new RsnStructure(6,myUD_I,myUD_E);
        
        float[][] upAreas=myRsnStruc.getUpAreas();
        float[][] upLength=myRsnStruc.getUpLength();
        float[][] upOrder=myRsnStruc.getHortonOrders();
        float[][] longLength=myRsnStruc.getLongestLength();
        
        int[] compLinks=myRsnStruc.getCompleteStreamLinksArray();
        
        for (int i=0;i<compLinks.length;i++){
            if(upOrder[0][compLinks[i]]>2)System.out.println(i+" "+upAreas[0][compLinks[i]]+" "+upLength[0][compLinks[i]]+" "+upOrder[0][compLinks[i]]+" "+longLength[0][compLinks[i]]);
        }
        
    }
    
    public static void main3(String[] args) {
        
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
    
}