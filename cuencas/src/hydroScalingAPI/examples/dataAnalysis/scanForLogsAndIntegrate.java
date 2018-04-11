/*
 * scanForLogsAndIntegrate.java
 *
 * Created on March 28, 2007, 4:27 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.dataAnalysis;

import java.io.IOException;
import visad.VisADException;

/**
 *
 * @author ricardo
 */
public class scanForLogsAndIntegrate {
    
    /** Creates a new instance of scanForLogsAndIntegrate */
    public scanForLogsAndIntegrate() {
        
        java.io.File pathToScan=new java.io.File("/hidrosigDataBases/MeltonBasins_DB/Rasters/Topography/1_arcsec/");
        
        try {
            
            hydroScalingAPI.io.MetaRaster metaRaster1=new hydroScalingAPI.io.MetaRaster (new java.io.File("/hidrosigDataBases/MeltonBasins_DB/Rasters/Hydrology/I1/t1.metaVHC"));
            metaRaster1.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/MeltonBasins_DB/Rasters/Hydrology/I1/t1.vhc"));
            visad.FlatField field1=metaRaster1.getField();

            hydroScalingAPI.io.MetaRaster metaRaster2=new hydroScalingAPI.io.MetaRaster (new java.io.File("/hidrosigDataBases/MeltonBasins_DB/Rasters/Hydrology/sum_p_pet_i/sum_p_pet_i.metaVHC"));
            metaRaster2.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/MeltonBasins_DB/Rasters/Hydrology/sum_p_pet_i/sum_p_pet_i.vhc"));
            visad.FlatField field2=metaRaster2.getField();
            
            java.util.Vector carrier=new java.util.Vector();
            findLogFiles(pathToScan,carrier);
            for (int i = 0; i < carrier.size(); i++) {
                
                java.io.File theLog=(java.io.File)carrier.get(i);
                
                hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster (new java.io.File(theLog.getPath().substring(0,theLog.getPath().lastIndexOf("."))+".metaDEM"));
                metaData.setLocationBinaryFile(new java.io.File(theLog.getPath().substring(0,theLog.getPath().lastIndexOf("."))+".dir"));
                metaData.setFormat("Byte");
                byte [][] fullDirMatrix=new hydroScalingAPI.io.DataRaster(metaData).getByte();
                
                metaData.setLocationBinaryFile(new java.io.File(theLog.getPath().substring(0,theLog.getPath().lastIndexOf("."))+".ltc"));
                metaData.setFormat("Float");
                float [][] ltc=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
                
                metaData.setLocationBinaryFile(new java.io.File(theLog.getPath().substring(0,theLog.getPath().lastIndexOf("."))+".areas"));
                metaData.setFormat("Float");
                float [][] areas=new hydroScalingAPI.io.DataRaster(metaData).getFloat();
                
                String[] toPrint=new hydroScalingAPI.io.BasinsLogReader(theLog).getPresetBasins();
                for (int j = 0; j < toPrint.length; j++) {
                    String[] basinLabel=toPrint[j].split(" ; ");
                    String xlabel=(basinLabel[0].split(", "))[0];
                    String ylabel=(basinLabel[0].split(", "))[1];
                    int MatX=Integer.parseInt(xlabel.substring(2).trim());
                    int MatY=Integer.parseInt(ylabel.substring(2).trim());
                    hydroScalingAPI.util.geomorphology.objects.Basin thisBasin = new hydroScalingAPI.util.geomorphology.objects.Basin(MatX,MatY, fullDirMatrix,metaData);
                    hydroScalingAPI.io.MetaPolygon metaPolyToUse=new hydroScalingAPI.io.MetaPolygon ();
                    metaPolyToUse.setName(thisBasin.toString());
                    metaPolyToUse.setCoordinates(thisBasin.getLonLatBasinDivide());
                    System.out.println(basinLabel[1]+"("+metaData.getLocationMeta().getPath()+")"+",drainage density,"+ltc[MatY][MatX]/areas[MatY][MatX]+",t1,"+metaPolyToUse.getAverage(field1)+",sum_p_pet_i,"+metaPolyToUse.getAverage(field2)+",area,"+areas[MatY][MatX]);
                }

            }
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (VisADException ex) {
            ex.printStackTrace();
        }
    }
    
    private void findLogFiles(java.io.File iniDir,java.util.Vector carrier){
        java.io.File[] fileLog=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DotFilter("log"));
        
        if(fileLog.length > 0){
            carrier.add(fileLog[0]);
        }
        
        java.io.File[] dirsToDig=iniDir.listFiles(new hydroScalingAPI.util.fileUtilities.DirFilter());
        for (int i = 0; i < dirsToDig.length; i++) {
            findLogFiles(dirsToDig[i],carrier);
        }
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new scanForLogsAndIntegrate();
    }
    
}
