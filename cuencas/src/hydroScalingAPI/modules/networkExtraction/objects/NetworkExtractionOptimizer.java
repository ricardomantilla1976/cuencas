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
 * NetworkExtractionOptimizer.java
 *
 * Created on August 28, 2003, 4:17 PM
 */

package hydroScalingAPI.modules.networkExtraction.objects;

/**
 * This was a rough attempt to optimize the network extraction code.  However it
 * was a failure because it leaves traces of the DEM partition.
 * @author Ricardo Mantilla
 */
public class NetworkExtractionOptimizer implements Runnable {
    
    private hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions extractOptions;
    private hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule Proc;
    private int[] xChunks,yChunks;
    private int maxXIndex,maxYIndex;
    
    private ExternalNetworkExtraction[] localExtractors;
//    private ExternalNetworkExtraction ExtractorNo1, ExtractorNo2;
    
    /**
     * Creates a new instance of NetworkExtractionOptimizer
     * @param exOp The settings GUI associated to the NetworkExtractionModule
     */
    public NetworkExtractionOptimizer(hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions exOp) {
        
        extractOptions=exOp;
        Proc=extractOptions.Proc;
        
        int nr=Proc.metaDEM.getNumRows();
        int nc=Proc.metaDEM.getNumCols();
            
        xChunks=new int[(int)Math.ceil(nc/100.)];
        yChunks=new int[(int)Math.ceil(nr/100.)];
        
        for (int i=0;i<xChunks.length;i++) xChunks[i]=Math.min(100,nc-100*(i));
        for (int i=0;i<yChunks.length;i++) yChunks[i]=Math.min(100,nr-100*(i));
        
        maxXIndex=Proc.metaDEM.getNumCols()-1;
        maxYIndex=Proc.metaDEM.getNumRows()-1;
        
        extractOptions.setMaxMinOptimizerBar(0, 2*xChunks.length*yChunks.length);
        
    }
    
    public void optimize(){

        //boolean dummyVar=false;
        if(Proc.taskDIR && xChunks.length*yChunks.length > 1000){
        //if(dummyVar){
            
            int numProcessors=4;
        
            System.out.println("Otimization starts here !!!");
            
            localExtractors=new ExternalNetworkExtraction[numProcessors];
            for(int i=0;i<numProcessors;i++) localExtractors[i]=new ExternalNetworkExtraction("null");

            String metaName=Proc.metaDEM.getLocationMeta().getName();
            //String pathToTemp=System.getProperty("java.io.tmpdir");
            
            
            String tempDirectoryPath=System.getProperty("java.io.tmpdir");
            
            java.io.File tempDirectory=new java.io.File(tempDirectoryPath+System.getProperty("file.separator")+"pandoraOptimizer"+System.getProperty("file.separator")+metaName.substring(0,metaName.lastIndexOf("."))+System.getProperty("file.separator"));
            tempDirectory.mkdirs();
            
            String pathToTemp=tempDirectory.getAbsolutePath();
            
            boolean availTest=true;

            for (int i=0;i<xChunks.length;i++){
                for (int j=0;j<yChunks.length;j++){
                    int xStartIndex=(100*i);
                    int xFinalIndex=Math.min((100*i+xChunks[i]-1)+((i<(xChunks.length-1))?15:0),maxXIndex);

                    int yStartIndex=(100*j);
                    int yFinalIndex=Math.min((100*j+yChunks[j]-1)+((j<(yChunks.length-1))?15:0),maxYIndex);

                    try{

                        hydroScalingAPI.io.MetaRaster theMeta=new hydroScalingAPI.io.MetaRaster();
                        theMeta.setName("Big DEM section");
                        String newMinLat=hydroScalingAPI.tools.DegreesToDMS.getprettyString(Proc.metaDEM.getMinLat()+yStartIndex*Proc.metaDEM.getResLat()/3600.,0);
                        theMeta.setMinLat(newMinLat);
                        String newMinLon=hydroScalingAPI.tools.DegreesToDMS.getprettyString(Proc.metaDEM.getMinLon()+xStartIndex*Proc.metaDEM.getResLon()/3600.,1);
                        theMeta.setMinLon(newMinLon);
                        theMeta.setResLat(Proc.metaDEM.getResLat());
                        theMeta.setResLon(Proc.metaDEM.getResLon());
                        theMeta.setNumCols(xFinalIndex-xStartIndex+1);
                        theMeta.setNumRows(yFinalIndex-yStartIndex+1);
                        theMeta.setFormat("Double");
                        theMeta.setMissing(Proc.metaDEM.getMissing());
                        theMeta.setTemporalScale("fix");
                        theMeta.setUnits(Proc.metaDEM.getUnits());
                        theMeta.setInformation("temporary file");
                        
                        java.io.File demChunkMeta=new java.io.File(pathToTemp+System.getProperty("file.separator")+"chunk"+i+"-"+j+".metaDEM");
                        theMeta.writeMetaRaster(demChunkMeta);

                        java.io.File demChunkData=new java.io.File(pathToTemp+System.getProperty("file.separator")+"chunk"+i+"-"+j+".dem");

                        theMeta.setLocationMeta(demChunkMeta);
                        theMeta.setLocationBinaryFile(demChunkData);

                        java.io.DataOutputStream dataWriter=new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(demChunkData)));

                        System.out.println(" >>>> Limits: "+xStartIndex+" "+xFinalIndex+" "+yStartIndex+" "+yFinalIndex);
                        
                        for(int l=yStartIndex;l<=yFinalIndex;l++){
                            for(int k=xStartIndex;k<=xFinalIndex;k++){
                                dataWriter.writeDouble(Proc.DEM[l+1][k+1]);
                            }
                        }

                        dataWriter.close();
                        
                        extractOptions.increaseValueOptimizerBar();
                        
                        //new hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule(theMeta,new hydroScalingAPI.io.DataRaster(theMeta),true);
                        availTest=true;
                        for (int k = 0; k < numProcessors; k++) {
                            availTest&=localExtractors[k].isBusy();
                            System.out.print(localExtractors[k].isBusy()+" ");
                        }
                        
                        System.out.println("\nAvailability Test: "+availTest);
                        
                        while(availTest){
                            System.out.println("All Processors are Busy");
                            new visad.util.Delay(1000);
                            availTest=true;
                            for (int k = 0; k < numProcessors; k++) availTest&=localExtractors[k].isBusy();
                        }

                        for (int k = 0; k < numProcessors; k++) {
                            if(!localExtractors[k].isBusy()) {
                                localExtractors[k]=new ExternalNetworkExtraction("ExtractorNo"+k);
                                localExtractors[k].setPriority(ExternalNetworkExtraction.MAX_PRIORITY);
                                localExtractors[k].setTargetFiles(theMeta.getLocationMeta().getPath(), theMeta.getLocationBinaryFile().getPath());
                                localExtractors[k].start();
                                break;
                            }
                        }
                        
                        System.out.println("Done with "+demChunkData);

                    } catch (java.io.IOException ioe){
                        System.err.println("Failed writing a meta file");
                        System.err.println(ioe);
                    }
                }
            }
            
            availTest=false;
            for (int k = 0; k < numProcessors; k++) {
                availTest|=localExtractors[k].isBusy();
                System.out.print(localExtractors[k].isBusy()+" ");
            }
            System.out.println("\nAvailability Test: "+availTest);
            
            while(availTest){
                System.out.println("Some Processors are Busy");
                new visad.util.Delay(1000);
                availTest=false;
                for (int k = 0; k < numProcessors; k++) availTest|=localExtractors[k].isBusy();
            }
            
            extractOptions.setValueOptimizerBar(2*xChunks.length*yChunks.length);

            for (int i=0;i<xChunks.length;i++){
                for (int j=0;j<yChunks.length;j++){
                    int xStartIndex=(100*i);
                    int xFinalIndex=Math.min((100*i+xChunks[i]-1)+((i<(xChunks.length-1))?15:0),maxXIndex);

                    int yStartIndex=(100*j);
                    int yFinalIndex=Math.min((100*j+yChunks[j]-1)+((j<(yChunks.length-1))?15:0),maxYIndex);

                    try{

                        java.io.File demChunkData=new java.io.File(pathToTemp+System.getProperty("file.separator")+"chunk"+i+"-"+j+".corrDEM");
                        java.io.DataInputStream dataReader=new java.io.DataInputStream(new java.io.BufferedInputStream(new java.io.FileInputStream(demChunkData)));

                        for(int l=yStartIndex;l<=yFinalIndex;l++){
                            for(int k=xStartIndex;k<=xFinalIndex;k++){
                                double newHeight=dataReader.readDouble();
                                if (l > (yStartIndex+5) && l < (yFinalIndex-5) && k > (xStartIndex+5) && k < (xFinalIndex-5) && newHeight != Double.parseDouble(Proc.metaDEM.getMissing())) Proc.DEM[l+1][k+1]=newHeight;
                            }
                        }

                        dataReader.close();

                        System.out.println("Done with "+demChunkData);
                        
                        extractOptions.increaseValueOptimizerBar();

                    } catch (java.io.IOException ioe){
                        System.err.println("Failed reading corrDEM file");
                        System.err.println(ioe);
                    }
                }
            }
        }
        
        extractOptions.setValueOptimizerBar(2*xChunks.length*yChunks.length);
        
        Thread t = new Thread(Proc);
        t.setPriority(t.MAX_PRIORITY);
        t.start();
        
    }
    
    public void run() {
        optimize();
    }    
    
    public static void main(java.lang.String[] args) {
        
        try{
            hydroScalingAPI.io.MetaRaster metaRaster1= new hydroScalingAPI.io.MetaRaster(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM"));
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dem"));
            
            //hydroScalingAPI.io.MetaRaster metaRaster1= new hydroScalingAPI.io.MetaRaster(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.metaDEM"));
            //metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.dem"));
            
            //hydroScalingAPI.io.MetaRaster metaRaster1= new hydroScalingAPI.io.MetaRaster(new java.io.File("/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.metaDEM"));
            //metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.dem"));
            
            System.out.println(metaRaster1.getLocationBinaryFile());
            hydroScalingAPI.io.DataRaster datosRaster = new hydroScalingAPI.io.DataRaster(metaRaster1);
            new NetworkExtractionModule(new hydroScalingAPI.mainGUI.ParentGUI(), metaRaster1, datosRaster);
            //new NetworkExtractionModule(metaRaster1, datosRaster);
        } catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }
        
        System.exit(0);
        
    }    
    
}
