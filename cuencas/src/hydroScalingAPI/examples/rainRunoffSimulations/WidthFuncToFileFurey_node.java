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
 * simulationsRep3.java
 *
 * Created on December 6, 2001, 10:34 AM
 */

package hydroScalingAPI.examples.rainRunoffSimulations;
//package hydroScalingAPI.tools;

import visad.*;
import java.io.*;
import java.util.Vector;
import hydroScalingAPI.tools.*;

/**
 *
 * @author Peter Furey
 */
public class WidthFuncToFileFurey_node extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
   
            
    /** Creates new simulationsRep3 */
    public WidthFuncToFileFurey_node(int x, int y, byte[][] direcc, int[][] magnitudes, hydroScalingAPI.io.MetaRaster md) throws java.io.IOException, VisADException{
        matDir=direcc;
        metaDatos=md;
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        // Here, metric==0 geometric, metric==1 topologic
        int thisX,thisY,nextlinkID,numBins;
        int metric=0;
        int ord1_condition=0;   // 0 = basins of order gt 1
                                // 1 = basins eq 1; need to change if condition below 
        float binsize;       
        double[][] laWFunc;
        String path_end="", filename="";
        
        if (metric == 0) path_end="wf_HS_geometric/" ;
        if (metric == 1) path_end="wf_HS_topologic/" ;
        
        //String output_path="/tmp/";
        //String output_path="/home/furey/Data_Analysis/goodwin_ms/dem_data/"+path_end;
        //String output_path="/home/furey/Data_Analysis/walnutgulch_az/dem_data/"+path_end;
        //String output_path="/home/furey/Data_Analysis/walnut_ks/whitewater_sb/dem_data/"+path_end;
        //String output_path="/home/furey/Data_Analysis/kentuckyriver_ky/lower_kentucky/dem_data/"+path_end;
        //String output_path="/home/furey/Data_Analysis/kentuckyriver_ky/upper_kentucky/dem_data/"+path_end;
        //String output_path="/home/furey/Data_Analysis/fractal_trees/peano/dem_data/"+path_end;
        //String output_path="/home/furey/Data_Analysis/fractal_trees/mandelbrot_vicsek/dem_data/"+path_end;
        //String output_path="/Temp/" ;
        String output_path="/home/furey/Data_Analysis/current_datafiles/dem_data/"+path_end;
        //String output_path="/home/furey/Data_Analysis/littlecolorado_az/diabloriver/dem_data/"+path_end;
        
        //String filename="_wfs_linknode.dat";  //binsize = .meanValue
        
        if (ord1_condition == 0) filename="_"+String.valueOf(x)+String.valueOf(y)+"_wfs_linknode.dat";  //binsize = .meanValue
        else filename="_"+String.valueOf(x)+String.valueOf(y)+"_wfs_linknode_ord1.dat";
        
        // For lower and upper kentucky : 
        // dat1 = 4500x0,4500x1, dat2 = x1,x2, dat3 = x2,x3, dat4 = x3,x4, dat5 = x4,x5, dat6 = x5,end, etc.
        //String filename="_wfs_minlinklen.dat";           //binsize = .minValue
        
        String demName=md.getLocationBinaryFile().getName().substring(0,md.getLocationBinaryFile().getName().lastIndexOf("."));
        
        java.io.File archivo=new java.io.File(output_path+demName+filename);
        System.out.println("Creating file : " +output_path+demName+filename);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(archivo);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.DataOutputStream newfile = new java.io.DataOutputStream(bufferout);       
        
        System.out.println("Starting Width Functions Calculations");
        
        hydroScalingAPI.util.geomorphology.objects.Basin itsCuenca;
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis itsLinksStructure;
        
        RealType numLinks= RealType.getRealType("numLinks"), distanceToOut = RealType.getRealType("distanceToOut");
        FlatField vals_ff_W,hist;
        Linear1DSet binsSet;
        
        System.out.println("Number of Complete Streams ="+linksStructure.completeStreamLinksArray.length);
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
        //for (int i=0;i<25;i++){
        //error occurred at i+1 = 4791.
        //for (int i=(4500*9);i<(4500*10);i++){
        //for (int i=(4500*10);i<linksStructure.completeStreamLinksArray.length;i++){
            if (linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]] > 1){  // for no ord1 basins
            //if (linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]] == 1){  // for only ord1 basins    
            
            // Lines below never worked, some streams eliminated ....
            //if (linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]] > ordlb &&
            //    linksStructure.magnitudeArray[linksStructure.completeStreamLinksArray[i]] < ordub){ 
                
                System.out.println("stream= "+(i+1)+", order= "+(int) thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]));
                thisX=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]%metaDatos.getNumCols();
                thisY=linksStructure.contactsArray[linksStructure.completeStreamLinksArray[i]]/metaDatos.getNumCols();
                
                //System.out.println(thisX);
                //System.out.println(thisY);
                newfile.writeInt((int) thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]));  // order
                newfile.writeInt(thisX);    // x location on DEM
                newfile.writeInt(thisY);    // y location on DEM                
                newfile.writeFloat((float) thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i]));  // upstream area
                           
                nextlinkID=linksStructure.nextLinkArray[linksStructure.completeStreamLinksArray[i]];  
                if (nextlinkID >= 0) newfile.writeInt((int) thisNetworkGeom.linkOrder(nextlinkID) );   // order of downstream connecting link
                else newfile.writeInt(-1);  // nextlinkID is -1 for outlet of parent (complete-Horton) basin
                
                
                //*FOR TESTING ... */
                //if (nextlinkID >0) {
                //    System.out.println("Order next link="+thisNetworkGeom.linkOrder(nextlinkID));
                //    if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) >= thisNetworkGeom.linkOrder(nextlinkID)) 
                //        System.out.println("ERROR");
                // }
                
                itsCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(thisX,thisY,matDir,metaDatos);
                itsLinksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(itsCuenca, metaDatos, matDir);
                
                float [][] distToOutlet=itsLinksStructure.getDistancesToOutlet();  // *NEW CHANGE TO FLOAT*
                float [][] linkLengths=itsLinksStructure.getVarValues(1);   // [1][n]
                float [][] distPairsPerLink = new float[2][linkLengths[0].length];
               
                // Get sorted distance pairs (top of link to outlet, bot of link to outlet).
                Pair[] mypairs=new Pair[distToOutlet[0].length];
                
                
                if (metric==0) {
                    for (int j=0;j<distToOutlet[0].length;j++) {
                        distPairsPerLink[0][j]=(float) distToOutlet[metric][j];   //link head
                        //distPairsPerLink[1][j]=(float) distToOutlet[metric][j]-linkLengths[0][j] ;  //link tail
                        distPairsPerLink[1][j]=distPairsPerLink[0][j]-linkLengths[0][j] ;  //link tail
                        mypairs[j] = new Pair(distPairsPerLink[0][j], distPairsPerLink[1][j]);
                        //System.out.println(linkLengths[0][j]);
                        //System.out.println(distPairsPerLink[0][j]);
                    }
                }
                System.gc();
                
                if (metric==1) {
                    for (int j=0;j<distToOutlet[0].length;j++) {
                        distPairsPerLink[0][j]=(float) distToOutlet[metric][j];   //link head
                        distPairsPerLink[1][j]=(float) distToOutlet[metric][j]-1 ;                  //link tail
                        mypairs[j] = new Pair(distPairsPerLink[0][j], distPairsPerLink[1][j]);
                        //System.out.println(linkLengths[0][j]);
                    }
                }
                
                
                java.util.Arrays.sort(mypairs);
                for (int j=0;j<distToOutlet[0].length;j++) {
                    distPairsPerLink[0][j]=(float) mypairs[j].property1;
                    distPairsPerLink[1][j]=(float) mypairs[j].property2;
                }
                
                //Get distances defined by link heads that are unique
                Vector v_dist = new Vector();
                float distA = -1.0F;
                for (int j=0;j<distToOutlet[0].length;j++) {
                    if (distPairsPerLink[0][j] != distA) {
                        distA = (float) distPairsPerLink[0][j];
                        v_dist.add(String.valueOf(distA));
                    }
                }
                float [] distUnique = new float[v_dist.size()+1];
                distUnique[0] = 0F;
                for (int j=0;j<v_dist.size();j++) {
                    distUnique[j+1] = Float.parseFloat((String) v_dist.get(j));
                }
                
                //Get width function (wFunc)
                int index, num_links;
                float [] wFunc = new float[distUnique.length];
                for (int j=0;j<distUnique.length;j++) {
                    index = 0;
                    num_links = 0;
                    while (distPairsPerLink[0][index] < distUnique[j]) index = index+1;
                    //FOR TESTING...
                    //System.out.println("Check index  "+ distUnique[j]+" "+index);
                    //if (index != 0) System.out.println(distPairsPerLink[0][index-1]);
                    //System.out.println(distPairsPerLink[0][index]);
                    float [][] distPairs_sub = new float[2][distPairsPerLink[0].length-index];
                    for (int k=index;k<distPairsPerLink[0].length;k++) {
                        distPairs_sub[0][k-index]=distPairsPerLink[0][k];
                        distPairs_sub[1][k-index]=distPairsPerLink[1][k];
                    }
                    for (int k=0;k<distPairs_sub[0].length;k++) {
                    if (distPairs_sub[1][k] < distUnique[j]) num_links= num_links+1;
                    }
                    wFunc[j] = num_links;
                }
                
                //FOR TESTING ...
                //if (i == 275){
                //for (int j=0;j<wFunc.length;j++) System.out.println(distUnique[j]+" "+wFunc[j]);
                //}
                //if (i == 315){
                //for (int j=0;j<wFunc.length;j++) System.out.println(distUnique[j]+" "+wFunc[j]);
                //}
                
                binsize = -1F;
                newfile.writeFloat(binsize);            // binsize for the order-n basin (binsize is based on link nodes and varies)
                newfile.writeInt((int) wFunc.length);   // number of bins
                
                if (wFunc.length > 1) {
                    for (int j=0;j<distUnique.length;j++) newfile.writeFloat(distUnique[j]);   // distances  x
                    for (int j=0;j<wFunc.length;j++) newfile.writeInt((int) wFunc[j]);              // number of links  y
                }
                else System.out.println("   Bin=1 ... stream ignored");
                
                
            }
        }
        
        System.out.println("Termina calculo de WFs");
 
        newfile.close();
        bufferout.close();
        
        System.out.println("Termina escritura de Resultados");
        
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        
        try{
            
            //Real Rain on Walnut Gulch
            subMain1(args);
            
            //Multifractal Rain on Peano
            //subMain2(args);
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
        } catch (VisADException v){
            System.out.print(v);
        }

        System.exit(0);

        
    }
    
    public static void subMain1 (String[] args) throws java.io.IOException, VisADException {
        
        /* Basins we've processed ... */
        //String topo_path = "/hidrosigDataBases/Goodwin_Creek_MS_database/Rasters/Topography/1_ArcSec_USGS/newDEM/goodwinCreek-nov03.metaDEM";
        //String topo_path="/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM";
        //String topo_path = "/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.metaDEM";
        //String topo_path = "/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/WalnutCreek_KS/walnutCreek.metaDEM";
        //String topo_path="/hidrosigDataBases/Kentucky_KY_database/Rasters/Topography/1_ArcSec_USGS/kentuckyRiver.metaDEM";
        //String topo_path = "/hidrosigDataBases/FractalTrees_database/Rasters/Topography/peano/peano.metaDEM";
        //String topo_path = "/hidrosigDataBases/FractalTrees_database/Rasters/Topography/man-vis/man-vis.metaDEM";

        /* For basins above using shell script ...*/
        java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(args[0]);
        String filepath = "/hidrosigDataBases/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String topo_path = filepath + "/" + tokenizer.nextToken() + ".metaDEM"; 
        
        /* For basins processed by Ricardo and Matt using shell script ...*/
        //StringTokenizer tokenizer = new StringTokenizer("B_26	1110	462	B_26");
        /*java.util.StringTokenizer tokenizer = new java.util.StringTokenizer(args[0]);
        String filepath = "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/" + tokenizer.nextToken();
        int x_outlet = Integer.parseInt(tokenizer.nextToken());
        int y_outlet = Integer.parseInt(tokenizer.nextToken());
        String topo_path = filepath + "/" + tokenizer.nextToken() + ".metaDEM";  */
        
        java.io.File theFile=new java.io.File(topo_path);

        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));        
        
        String formatoOriginal=metaModif.getFormat();
        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        hydroScalingAPI.mainGUI.ParentGUI tempFrame=new hydroScalingAPI.mainGUI.ParentGUI();
         
        System.out.println("Beginning ...");
        
        /* OUTLET COORDS EXAMINED BELOW (x,y) ...
           Goodwin Creek, MS ...    (44,111)
           Walnut Gulch, AZ ...     (82,260)
           Whitewater, KS ...       (1063,496) ... from whitewater.dem, near Towanda gauge
           Walnut Creek, KS ...     (1309, 312)
           Whitewater, KS ...       (1345, 2511) ... from walnutcreek.dem at end of Whitewate River
           Walnut Creek, KS ...     (1345, 2510) ... at junction of whitewater and walnut rivers
           Lower Kentucky, KY ...   (845,2595)
           Upper Kentucky, KY ...   (845,2596)
           Peano Tree, Ord 7 ...    (64,64) 
           Mandelb-Vicsek, Ord 7 ...(64,64) 
           Diablo, AZ (Drains into L Colorado) ... (3409,3996) */ 
        
        /* No script used ... */
        //new WidthFuncToFileFurey_node(44,111,matDirs,magnitudes,metaModif);
        
        /* Using script ... */
        new WidthFuncToFileFurey_node(x_outlet,y_outlet,matDirs,magnitudes,metaModif);
        
    }
    
}
