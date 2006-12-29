/*
 * RSNAnalysis.java
 *
 * Created on October 1, 2003, 3:07 PM
 */

package hydroScalingAPI.modules.networkAnalysis.objects;

/**
 *
 * @author Ricardo Mantilla
 */
public class RSNAnalysis {
    
    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis;
    
    private java.util.Vector[] metaLinks;
    private java.util.Vector generatorsInfo=new java.util.Vector();
    
    private int[] numFullLinks;
    
    private float[][] linksOrders;
    private int[] dsIndexes;
    private int[][] usIndexes;
    private int[] subOutlets;
    
    /** Creates a new instance of RSNAnalysis */
    public RSNAnalysis(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mla) throws java.io.IOException {
         
         mylinksAnalysis=mla;
         
         metaLinks= new java.util.Vector[mylinksAnalysis.getBasinOrder()];
         numFullLinks=new int[mylinksAnalysis.getBasinOrder()];
         
         for (int i=0;i<mylinksAnalysis.getBasinOrder();i++){
             
             metaLinks[i]=new java.util.Vector();
             
         }
         
         int outletID=mylinksAnalysis.getOutletID();
        
         linksOrders = mylinksAnalysis.getVarValues(4);
         dsIndexes = mylinksAnalysis.nextLinkArray;
         usIndexes = mylinksAnalysis.connectionsArray;
         subOutlets=mylinksAnalysis.completeStreamLinksArray;
                  
         for (int i=0;i<subOutlets.length;i++){
             
             int currIndex=subOutlets[i];
             java.util.Vector tempList=new java.util.Vector();
             tempList.add(new int[] {subOutlets[i]});

             findChain(tempList);

             metaLinks[(int)linksOrders[0][subOutlets[i]]-1].add(tempList);
             numFullLinks[(int)linksOrders[0][subOutlets[i]]-1]++;
             
         }
         
         for (int i=metaLinks.length;i>1;i--){
             java.util.Vector toBreak=metaLinks[i-1];
             for(int j=0;j<toBreak.size();j++){
                 breakMetaLink((java.util.Vector)toBreak.get(j),i);
             }
             
         }

         
    }
    
    private void findChain(java.util.Vector toFillUp){

        int toLookFor=((int[])(toFillUp.lastElement()))[0];
        int[] incoming=usIndexes[toLookFor];
  
        for (int i=0;i<incoming.length;i++){
            
            if(linksOrders[0][incoming[i]] == linksOrders[0][toLookFor]){
                toFillUp.add(new int[] {incoming[i]});
                findChain(toFillUp);
            }
        }
        
    }
    
    private void breakMetaLink(java.util.Vector currentMetaLink, int Scale){
        
        int NumBreaks=-1; //because there is always one break at the top and we don't want to count it.
        int linkOrder=0;
        
        int[] toLookFor=(int[])currentMetaLink.get(0);
        linkOrder=(int)linksOrders[0][toLookFor[0]];
        
        java.util.Vector tempMetaLink=new java.util.Vector();
        
        int[] incoming;
        
        int sizeMetaLink=currentMetaLink.size();
        for(int i=0;i<sizeMetaLink;i++){
            toLookFor=(int[])currentMetaLink.get(i);
            tempMetaLink.add(toLookFor);
            incoming=usIndexes[toLookFor[0]];
            for (int j=0;j<incoming.length;j++){
            
                if(((Scale-1) == linksOrders[0][incoming[j]]) || (i == (sizeMetaLink-1))){
                    metaLinks[Scale-2].add(tempMetaLink.clone());
                    tempMetaLink=new java.util.Vector();
                    
                    NumBreaks++;

                    break;
                    
                }
            }
        }
        
        generatorsInfo.add(new int[] {linkOrder,(Scale-1),NumBreaks});
        
    }
    
    public java.util.Vector getGeneratorsInfo(){
        return generatorsInfo;
    }
    
    public void printGeneratorsToFile(String outFileName) throws java.io.IOException{
        
        java.io.File outFile=new java.io.File(outFileName);
        
        java.io.FileOutputStream        outStream;
        java.io.OutputStreamWriter      newfile;
        java.io.BufferedOutputStream    bufferout;
        String                          returnCh="\n";
        
        outStream = new java.io.FileOutputStream(outFile);
        bufferout=new java.io.BufferedOutputStream(outStream);
        newfile=new java.io.OutputStreamWriter(bufferout);
        
        for(int i=0;i<generatorsInfo.size();i++){
            int[] tempArr=(int[])generatorsInfo.get(i);
            newfile.write(tempArr[0]+" "+tempArr[1]+" "+tempArr[2]+returnCh);
        }
        
        newfile.close();
        bufferout.close();
        
        System.out.println("done");
        
    }
    
    public int[][] getConnectionStructure(int scale){
            
        int ncols=mylinksAnalysis.localMetaRaster.getNumCols();

        int[][] connections=new int[metaLinks[scale-1].size()][];
        for(int i=0;i<connections.length;i++){
            java.util.Vector Stream=(java.util.Vector)metaLinks[scale-1].get(i);
            int thisLinkIndex=((int[])Stream.firstElement())[0];
            if(i>=numFullLinks[scale-1]) connections[i]=usIndexes[((int[])Stream.lastElement())[0]]; else connections[i]=new int[0];
        }
        return connections;

    }
    
    public int[][] getHeadsAndTails(int scale){
        
        int[][] HAndT=new int[4][metaLinks[scale-1].size()];
        for(int i=0;i<HAndT[0].length;i++){
            java.util.Vector Stream=(java.util.Vector)metaLinks[scale-1].get(i);
            HAndT[0][i]=mylinksAnalysis.contactsArray[((int[])Stream.firstElement())[0]];
            HAndT[1][i]=mylinksAnalysis.tailsArray[((int[])Stream.firstElement())[0]];
            HAndT[2][i]=mylinksAnalysis.headsArray[((int[])Stream.lastElement())[0]];
            if (i<numFullLinks[scale-1])
                HAndT[3][i]=0;
            else
                HAndT[3][i]=1;
            
        }
        return HAndT;
    }
    
    public float[][] getVarValues(int varToGet, int scale) throws java.io.IOException {
        
        
        int[][] HAndT=getHeadsAndTails(scale);
        int[][] connectionsArray=getConnectionStructure(scale);
        
        String[] extenciones={  ".areas",       /*0*/
                                ".ltc",         /*1*/
                                ".areas",       /*2*/
                                ".corrDEM",     /*3*/
                                ".horton",      /*4*/
                                ".ltc",         /*5*/
                                ".magn",        /*6*/
                                ".magn",        /*7*/
                                ".magn",        /*8*/
                                ".corrDEM",     /*9*/
                                ".corrDEM",     /*10*/
                                ".lcp",         /*11*/
                                ".magn"         /*12*/
                                };
    
        float[][] quantityArray=new float[1][HAndT[0].length];
        
        java.io.File rutaQuantity=new java.io.File(mylinksAnalysis.localMetaRaster.getLocationBinaryFile().getPath().substring(0,mylinksAnalysis.localMetaRaster.getLocationBinaryFile().getPath().lastIndexOf("."))+extenciones[varToGet]);
        java.io.RandomAccessFile fileQuantity=new java.io.RandomAccessFile(rutaQuantity,"r");
       
        int ncols=mylinksAnalysis.localMetaRaster.getNumCols();
        
        switch(varToGet){
            case 0:
                //Meta Link's Hillslope Area.  This is done by subtraction of area at head and area at incoming links head
                for (int i=0;i<quantityArray[0].length;i++){
                    
                    fileQuantity.seek(4*HAndT[0][i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    for (int j=0;j<connectionsArray[i].length;j++){
                        fileQuantity.seek(4*mylinksAnalysis.contactsArray[connectionsArray[i][j]]);
                        quantityArray[0][i]-=fileQuantity.readFloat();
                    }
                }
                
                break;
            case 1:
                //Meta Link's Length.  This is done by subtraction of total channel length at head and total channel length at incoming links head
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*HAndT[0][i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                    for (int j=0;j<connectionsArray[i].length;j++){
                        fileQuantity.seek(4*mylinksAnalysis.contactsArray[connectionsArray[i][j]]);
                        quantityArray[0][i]-=fileQuantity.readFloat();
                    }
                }
                break;
            case 2:
                //Meta Link's Upstream area.
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(4*HAndT[0][i]);
                    quantityArray[0][i]=fileQuantity.readFloat();
                }
                break;
            case 3:
                //Meta Link's drop
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(8*HAndT[2][i]);
                    double altUp=fileQuantity.readDouble();
                    fileQuantity.seek(8*HAndT[1][i]);
                    quantityArray[0][i]=(float) (altUp-fileQuantity.readDouble());
                }
                
                break;
            case 4:
                //Meta Link's order
                break;
            case 5:
                //Total Channel Length
                break;
            case 6:
                //Meta Link's Magnitude
                for (int i=0;i<quantityArray[0].length;i++){
                    
                    fileQuantity.seek(4*HAndT[0][i]);
                    quantityArray[0][i]=(float)fileQuantity.readInt();
                    for (int j=0;j<connectionsArray[i].length;j++){
                        fileQuantity.seek(4*mylinksAnalysis.contactsArray[connectionsArray[i][j]]);
                        quantityArray[0][i]-=(float)fileQuantity.readInt();
                    }
                }
                
                break;
            case 7:
                //Meta Link's Distance to Outlet
                break;
            case 8:
                //Meta Link's Topologic Distance to Outlet
                break;
            case 9:
                //Meta Link's Slope
                float[][] linkLengths=getVarValues(1,scale);
                
                for (int i=0;i<quantityArray[0].length;i++){
                    fileQuantity.seek(8*HAndT[2][i]);
                    double altUp=fileQuantity.readDouble();
                    fileQuantity.seek(8*HAndT[1][i]);
                    quantityArray[0][i]=(float) (altUp-fileQuantity.readDouble());
                    
                    quantityArray[0][i]/=linkLengths[0][i]*1000.0f;
                }
                break;
            case 10:
                //Meta Link's Elevation
                break;
            case 11:
                //Meta Link's Longest Channel Length
                break;
            case 12:
                //Meta Link's Binary Link Address
                break;
                
        }
        
        fileQuantity.close();
        
        return quantityArray;
    }
    
    public static void main(String[] args) {
        
        //All the subMains are developed to analyze the DD basins
        
        //args=new String[] {"B_26 1110 462 B_26"};
        //args=new String[] {"B_02  328  1259  31908796"};
        //args=new String[] {"B_07  1103  2323  60615818"};
        
        //args=new String[] {"WalnutCreek_KS  1309  312  walnutCreek"};
        
        //args=new String[] {"kentuckyRiver  845 2596  kentuckyRiver"};
        args=new String[] {"kentuckyRiver  845 2595  kentuckyRiver"};
        
        //subMain1(args); //writes generators and geoemtric properties to files.  This algortithm is called
                        //by runAllBasinsRSN.sh
        
        //subMain2(args); //Test for Extended Horton Laws
        
        //subMain3(args); //Basin Shape invariance as a function of generator type

        subMain4(args); //Test for divergence of areas across scales
    }
    
    /**
     * @param args the command line arguments
     */
    public static void subMain1(String[] args) {
        
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(args[0]);
        String path = "/hidrosigDataBases/Continental_US_database/Rasters/";
        String filepath = path + "Topography/Dd_Basins_30_ArcSec/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String filename = filepath + "/" + tokenizer.nextToken();
        
        try{
        
            java.io.File theFile=new java.io.File(filename + ".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(filename + ".dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x_outlet,y_outlet,matDirs,metaModif);
            
            hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(laCuenca, metaModif, matDirs);
            
            long iniTime=System.currentTimeMillis();
            RSNAnalysis myRsnGen=new RSNAnalysis(mylinksAnalysis);
            long finTime=System.currentTimeMillis();
            
            myRsnGen.printGeneratorsToFile("/home/ricardo/workFiles/ecologyWork/DD/output_rsns/"+metaModif.getLocationMeta().getName()+"_x_"+laCuenca.getXYBasin()[0][0]+"_y_"+laCuenca.getXYBasin()[1][0]+".rsnGens.txt");
            
            System.out.println((finTime-iniTime)/1000./60.);
            
            //Now write some hortonian data
            
            hydroScalingAPI.util.geomorphology.objects.HortonAnalysis myBasinResults=new hydroScalingAPI.util.geomorphology.objects.HortonAnalysis(laCuenca, metaModif, matDirs);
            
            String outFileName="/home/ricardo/workFiles/ecologyWork/DD/output_rsns/"+metaModif.getLocationMeta().getName()+"_x_"+laCuenca.getXYBasin()[0][0]+"_y_"+laCuenca.getXYBasin()[1][0]+".hortonProps.txt";
            
            java.io.File outFile=new java.io.File(outFileName);
        
            java.io.FileOutputStream        outStream;
            java.io.OutputStreamWriter      newfile;
            java.io.BufferedOutputStream    bufferout;
            String                          returnCh="\n";
            
            outStream = new java.io.FileOutputStream(outFile);
            bufferout=new java.io.BufferedOutputStream(outStream);
            newfile=new java.io.OutputStreamWriter(bufferout);
            
            float[][] topolLengthDist=myBasinResults.getLengthDistributionPerOrder(1);
            float[] ratiosC=myBasinResults.getLengthRatio(3,5,topolLengthDist);
            
            newfile.write("Topologic Length Analysis"+returnCh);
            newfile.write("Ratio = "+Math.exp(Math.abs(ratiosC[0]))+returnCh);
            newfile.write("R^2 = "+ratiosC[2]+returnCh);
            newfile.write(topolLengthDist.length+returnCh);
            
            for(int i=0;i<topolLengthDist.length;i++){
                for(int j=0;j<topolLengthDist[i].length;j++){
                    newfile.write(topolLengthDist[i][j]+" ");
                }
                newfile.write(returnCh);
            }
            
            float[][] magnDist=myBasinResults.getQuantityDistributionPerOrder(1);
            float[] ratiosM=myBasinResults.getQuantityRatio(3,5,magnDist);
            
            newfile.write("Magnitude Analysis"+returnCh);
            newfile.write("Ratio = "+Math.exp(Math.abs(ratiosM[0]))+returnCh);
            newfile.write("R^2 = "+ratiosM[2]+returnCh);
            newfile.write(magnDist.length+returnCh);
            
            for(int i=0;i<magnDist.length;i++){
                for(int j=0;j<magnDist[i].length;j++){
                    newfile.write(magnDist[i][j]+" ");
                }
                newfile.write(returnCh);
            }
            
            newfile.write("Metalink Analysis: Type, Length, Area"+returnCh);
            
            int ncols=metaModif.getNumCols();
            int scaleToTest=1;
            
            int[][] ht=myRsnGen.getHeadsAndTails(scaleToTest);
            for(int k=0;k<ht[0].length;k++) newfile.write(ht[0][k]%ncols+" ");
            newfile.write(returnCh);
            for(int k=0;k<ht[0].length;k++) newfile.write(ht[0][k]/ncols+" ");
            newfile.write(returnCh);
            for(int k=0;k<ht[0].length;k++) newfile.write(ht[3][k]+" ");
            newfile.write(returnCh);
            
            float[][] linkLength=myRsnGen.getVarValues(1,scaleToTest);
            for(int k=0;k<linkLength[0].length;k++) newfile.write(linkLength[0][k]+" ");
            newfile.write(returnCh);
            float[][] linkArea=myRsnGen.getVarValues(0,scaleToTest);
            for(int k=0;k<linkArea[0].length;k++) newfile.write(linkArea[0][k]+" ");
            newfile.write(returnCh);
            float[][] linkUpArea=myRsnGen.getVarValues(2,scaleToTest);
            for(int k=0;k<linkUpArea[0].length;k++) newfile.write(linkUpArea[0][k]+" ");
            newfile.write(returnCh);
            
            /*for(int k=0;k<mylinksAnalysis.contactsArray.length;k++) newfile.write(mylinksAnalysis.contactsArray[k]%ncols+" ");
            newfile.write(returnCh);
            for(int k=0;k<mylinksAnalysis.contactsArray.length;k++) newfile.write(mylinksAnalysis.contactsArray[k]/ncols+" ");
            newfile.write(returnCh);
            linkLength=mylinksAnalysis.getVarValues(1);
            for(int k=0;k<linkLength[0].length;k++) newfile.write(linkLength[0][k]+" ");
            newfile.write(returnCh);
            linkArea=mylinksAnalysis.getVarValues(0);
            for(int k=0;k<linkArea[0].length;k++) newfile.write(linkArea[0][k]+" ");
            newfile.write(returnCh);*/

            newfile.close();
            bufferout.close();
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain2(String[] args) {

        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(args[0]);
        String path = "/hidrosigDataBases/Continental_US_database/Rasters/";
        String filepath = path + "Topography/Dd_Basins_30_ArcSec/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String filename = filepath + "/" + tokenizer.nextToken();
        
        try{

            java.io.File theFile=new java.io.File(filename + ".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(filename + ".dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x_outlet,y_outlet,matDirs,metaModif);
            
            hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(laCuenca, metaModif, matDirs);
            
            long iniTime=System.currentTimeMillis();
            RSNAnalysis myRsnGen=new RSNAnalysis(mylinksAnalysis);
            long finTime=System.currentTimeMillis();
            
            //Testing the idea of distribution of properties at different scales
            System.out.println("Extended Horton Laws for "+theFile.getName());
            
            for(int scale=1;scale<mylinksAnalysis.basinOrder+1;scale++){
            
                int[][] ht=myRsnGen.getHeadsAndTails(scale);
                float[][] metaLinkAreas=myRsnGen.getVarValues(0,scale);

                double averageExt=0.0; double countExt=0.0;
                double averageInt=0.0; double countInt=0.0;
                
                System.out.println("OMEGA "+scale);
                
                for(int i=0;i<metaLinkAreas[0].length;i++){
                    if(ht[3][i]==0){
                        averageExt+=metaLinkAreas[0][i];
                        countExt++;
                    } else{
                        averageInt+=metaLinkAreas[0][i];
                        countInt++;
                    }
                    
                    System.out.println(metaLinkAreas[0][i]);
                }
                
                System.out.println();
                
                averageExt/=countExt;
                averageInt/=countInt;

                //System.out.println(scale+","+averageExt+","+averageInt);
            }
            
            System.out.println();
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain3(String[] args) {
        
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(args[0]);
        String path = "/hidrosigDataBases/Continental_US_database/Rasters/";
        String filepath = path + "Topography/Dd_Basins_30_ArcSec/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String filename = filepath + "/" + tokenizer.nextToken();
        
        try{
            java.io.File theFile=new java.io.File(filename + ".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(filename + ".dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x_outlet,y_outlet,matDirs,metaModif);
            
            hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(laCuenca, metaModif, matDirs);
            
            long iniTime=System.currentTimeMillis();
            RSNAnalysis myRsnGen=new RSNAnalysis(mylinksAnalysis);
            long finTime=System.currentTimeMillis();
            
            java.util.Vector generatorsInfo=myRsnGen.getGeneratorsInfo();

            int ordBasin=mylinksAnalysis.getBasinOrder();
            
            int dummyCounter=0;
            
            for(int l=ordBasin;l>1;l--){
                int[][] headsTails=myRsnGen.getHeadsAndTails(l);
                for(int i=0;i<headsTails[0].length;i++){
                    int xOutlet=headsTails[0][i]%metaModif.getNumCols();
                    int yOutlet=headsTails[0][i]/metaModif.getNumCols();

                    int xSource=headsTails[1][i]%metaModif.getNumCols();
                    int ySource=headsTails[1][i]/metaModif.getNumCols();

                    int[] tempArr=(int[])generatorsInfo.get(dummyCounter++);
                    
                    hydroScalingAPI.util.geomorphology.objects.Basin anotherCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(xOutlet,yOutlet,matDirs,metaModif);

                    float[] bf=anotherCuenca.getDivideShapeFactor();
                    System.out.println("Level: "+l+" Head: "+xOutlet+","+yOutlet+" Tail: "+xSource+","+ySource+","+tempArr[0]+","+tempArr[1]+","+tempArr[2]+","+bf[0]+","+bf[1]+","+bf[2]);
                    
                }
            }
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain4(String[] args) {

        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(args[0]);
        String path = "/hidrosigDataBases/Continental_US_database/Rasters/";
        String filepath = path + "Topography/Dd_Basins_30_ArcSec/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String filename = filepath + "/" + tokenizer.nextToken();
        
        try{

            java.io.File theFile=new java.io.File(filename + ".metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File(filename + ".dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x_outlet,y_outlet,matDirs,metaModif);
            
            hydroScalingAPI.util.geomorphology.objects.LinksAnalysis mylinksAnalysis = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(laCuenca, metaModif, matDirs);
            
            long iniTime=System.currentTimeMillis();
            RSNAnalysis myRsnGen=new RSNAnalysis(mylinksAnalysis);
            long finTime=System.currentTimeMillis();
            
            //Testing the idea of distribution of properties at different scales
            System.out.println("Divergence Analysis for "+theFile.getName());
            System.out.println("scale,averageExt,maxExt,minExt,DivExt,averageInt,maxInt,minInt,DivInt");
            
            for(int scale=1;scale<mylinksAnalysis.basinOrder+1;scale++){
            
                int[][] ht=myRsnGen.getHeadsAndTails(scale);
                float[][] metaLinkAreas=myRsnGen.getVarValues(0,scale);

                double averageExt=0.0, countExt=0.0, maxExt=Double.MIN_VALUE, minExt=Double.MAX_VALUE;
                double averageInt=0.0, countInt=0.0, maxInt=Double.MIN_VALUE, minInt=Double.MAX_VALUE;

                System.out.println("ORDER "+(scale));
                    
                for(int i=0;i<metaLinkAreas[0].length;i++){
                    
                    System.out.println(ht[3][i]+" "+metaLinkAreas[0][i]);
                    
                    if(ht[3][i]==0){
                        averageExt+=metaLinkAreas[0][i];
                        maxExt=Math.max(maxExt,metaLinkAreas[0][i]);
                        minExt=Math.min(minExt,metaLinkAreas[0][i]);
                        countExt++;
                    } else{
                        averageInt+=metaLinkAreas[0][i];
                        maxInt=Math.max(maxInt,metaLinkAreas[0][i]);
                        minInt=Math.min(minInt,metaLinkAreas[0][i]);
                        countInt++;
                    }
                }
                averageExt/=countExt;
                averageInt/=countInt;

                double DivExt=(maxExt-minExt)/averageExt;
                double DivInt=(maxInt-minInt)/averageInt;
                
                //System.out.println(scale+","+averageExt+","+maxExt+","+minExt+","+DivExt+","+averageInt+","+maxInt+","+minInt+","+DivInt);
                System.out.println();
            }
            
            //System.out.println();
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
}
