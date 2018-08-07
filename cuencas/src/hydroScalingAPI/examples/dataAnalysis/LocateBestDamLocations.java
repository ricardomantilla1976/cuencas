/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.dataAnalysis;

import java.io.IOException;

/**
 *
 * @author ricardo
 */
public class LocateBestDamLocations {

    public LocateBestDamLocations(int x, int y,float dh, byte[][] fullDirMatrix, float[][] dem,byte[][] horton,float[][] areas, hydroScalingAPI.io.MetaRaster metaDatos) throws java.io.IOException{
        
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,fullDirMatrix,metaDatos);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, fullDirMatrix);
        hydroScalingAPI.util.geomorphology.objects.HortonAnalysis streamsStructure=new hydroScalingAPI.util.geomorphology.objects.HortonAnalysis(myCuenca, metaDatos, fullDirMatrix);
        hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition myRsnGen = new hydroScalingAPI.util.randomSelfSimilarNetworks.RSNDecomposition(linksStructure);

        //Example asking the question to every pixel on an order 4,5 and 6 stream
        
//        byte[][] basinMask=myCuenca.getBasinMask();
//        
//        for (int i = 0; i < horton.length; i++) {
//            for (int j = 0; j < horton[0].length; j++) {
//                if(horton[i][j]>=4 && horton[i][j]<=4 && basinMask[i][j]>0){
//                    hydroScalingAPI.util.geomorphology.objects.DamInundation theDam=new hydroScalingAPI.util.geomorphology.objects.DamInundation(j, i ,5,fullDirMatrix,dem,metaDatos);
//                    System.out.println(j+" "+i+" "+theDam.getLakeArea()+" "+theDam.getLakeVolume()+" "+(theDam.getLakeVolume()/(areas[i][j]*5*25400.0)));
//                };
//                
//            }
//            
//        }
//        
//        System.exit(0);
        
        //Example of selecting the best location along streams of order 4, 5 or 6
        
        int[][] basinHillslopesMask=myCuenca.getHillslopesMask(fullDirMatrix, myRsnGen, 1);
        
        int nCols=metaDatos.getNumCols();
        
        double resLon=metaDatos.getResLon();
        double resLat=metaDatos.getResLat();
        double minLon=metaDatos.getMinLon();
        double minLat=metaDatos.getMinLat();
        
        
        //Array of stream orders to analyze and Dam and Lake parameters
        int[] ordersToConsider={3,4};
        double maxAreaAllowed=1e5;
        float damElevation=5;
        
        
        for (int i = 0; i < ordersToConsider.length; i++) {
            for (int j = 0; j < streamsStructure.headsArray[ordersToConsider[i]-1].length; j++) {
                
                int ja=streamsStructure.headsArray[ordersToConsider[i]-1][j]%nCols;
                int ia=streamsStructure.headsArray[ordersToConsider[i]-1][j]/nCols;
                
                int jaE=streamsStructure.contactsArray[ordersToConsider[i]-1][j]%nCols;
                int iaE=streamsStructure.contactsArray[ordersToConsider[i]-1][j]/nCols;
                
                int iaN=ia;
                int jaN=ja;
                
                //TO TEST BOTTOM OF STREAM
//                jaN=jaE;
//                iaN=iaE;
                
                java.util.Vector locationAreasVolumes=new java.util.Vector<double[]>();
                
                //This loops walks the stream pixel by pixel and calculates storage and area for the lake upstream of a given damElevation
                
                while(iaN-iaE != 0 || jaN-jaE != 0){    

                    hydroScalingAPI.util.geomorphology.objects.DamInundation theDam=new hydroScalingAPI.util.geomorphology.objects.DamInundation(jaN, iaN ,damElevation,fullDirMatrix,dem,metaDatos);
                    
                    double[] resCharac=new double[8];
                    
                    resCharac[0]=(float) ((jaN+0.5)*resLon/3600.0f+minLon);
                    resCharac[1]=(float) ((iaN+0.5)*resLat/3600.0f+minLat);
                    resCharac[2]=jaN;
                    resCharac[3]=iaN;
                    resCharac[4]=basinHillslopesMask[iaN][jaN]-1;
                    resCharac[5]=theDam.getLakeArea();
                    resCharac[6]=theDam.getLakeVolume();
                    resCharac[7]=areas[iaN][jaN];
                    
                    locationAreasVolumes.add(resCharac);
                    
                    //System.out.println(">> "+java.util.Arrays.toString(resCharac));
                    
                    iaN=ia+((fullDirMatrix[ia][ja]-1)/3)-1;
                    jaN=ja+((fullDirMatrix[ia][ja]-1)%3)-1;  


                    ia=iaN; ja=jaN;

                }
                
                  hydroScalingAPI.util.geomorphology.objects.DamInundation theDam=new hydroScalingAPI.util.geomorphology.objects.DamInundation(jaN, iaN ,damElevation,fullDirMatrix,dem,metaDatos);
                    
                double[] resCharac=new double[8];
                    
                resCharac[0]=(float) ((jaN+0.5)*resLon/3600.0f+minLon);
                resCharac[1]=(float) ((iaN+0.5)*resLat/3600.0f+minLat);
                resCharac[2]=jaN;//(float) (jaN*resLon/3600.0f+minLon);
                resCharac[3]=iaN;//(float) (iaN*resLat/3600.0f+minLat);
                resCharac[4]=basinHillslopesMask[iaN][jaN]-1;
                resCharac[5]=theDam.getLakeArea();
                resCharac[6]=theDam.getLakeVolume();
                resCharac[7]=areas[iaN][jaN];

                locationAreasVolumes.add(resCharac);
                
                if(locationAreasVolumes.size() > 10){
                
                    double maxVoluAvailable=Double.MIN_VALUE;
                    double minAreaAvailable=Double.MAX_VALUE;
                    int locationChosen=-1;
                    int locationDefault=-1;

                    for (int k = 5; k < locationAreasVolumes.size()-5; k++) {
                        double[] thisLake =(double[]) locationAreasVolumes.elementAt(k);

                        if(thisLake[5]<minAreaAvailable){
                            minAreaAvailable=thisLake[5];
                            locationDefault=k;
                        }

                        if(thisLake[5]<=maxAreaAllowed){
                            if(thisLake[6]>maxVoluAvailable){
                                maxVoluAvailable=thisLake[6];
                                locationChosen=k;
                            }
                        }

                    }

                    double[] thisLake;

                    if(locationChosen == -1){
                        //This case means it didn't find a location with less than the allowed area
                        thisLake =(double[]) locationAreasVolumes.elementAt(locationDefault);
                        //System.out.println(java.util.Arrays.toString(thisLake));
                    } else {
                        thisLake =(double[]) locationAreasVolumes.elementAt(locationChosen);
                        System.out.println(locationAreasVolumes.size()+" "+java.util.Arrays.toString(thisLake));
                    }
                
                }
                
            }
        }
        
        
    }

    /**
     * Tests for the class
     * @param args The command line arguments
     */
    public static void main (String args[]) {
        main0(args);
    }
    
    /**
     * Tests for the class
     * @param args The command line arguments
     */
    public static void main0 (String args[]) {
        
        
        try{
            
            //This defines the location of the topography to work with... we can stick with the 30m DEM
            
            //java.io.File theFile=new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/Resolutions/demcc_10.metaDEM");
            
            java.io.File theFile=new java.io.File("/CuencasDataBases/ClearCreek_Database/Rasters/Topography/ClearCreek/NED_00159011.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".corrDEM"));
            metaModif.setFormat("Double");
            float [][] dem=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".horton"));
            metaModif.setFormat("Byte");
            byte [][] horton=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".areas"));
            metaModif.setFormat("Float");
            float [][] areas=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();
            
            new hydroScalingAPI.examples.dataAnalysis.LocateBestDamLocations(1570, 127,5, matDirs,dem,horton,areas,metaModif);
            
           
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
    }
}
