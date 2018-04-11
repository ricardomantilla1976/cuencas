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

package hydroScalingAPI.examples.rainRunoffSimulations.RealTimeSystem;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class RealTimeSystemSimulationManager extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    private int x;
    private int y;
    private int[][] magnitudes;
    private java.io.File outputDirectory;
    private java.util.Hashtable routingParams;

    private long delay=1*60*60*1000;

    private hydroScalingAPI.util.geomorphology.objects.Basin myCuenca;
    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo;

    
    /** Creates new simulationsRep3 */
    public RealTimeSystemSimulationManager(int xx, int yy, byte[][] direcc, int[][] magnitudesOR, 
                                 hydroScalingAPI.io.MetaRaster md, java.io.File outputDirectoryOR,java.util.Hashtable rP) throws java.io.IOException, VisADException{
        
        matDir=direcc;
        metaDatos=md;
        
        x=xx;
        y=yy;
        magnitudes=magnitudesOR;
        outputDirectory=outputDirectoryOR;
        routingParams=rP;
        
        //Here an example of rainfall-runoff in action
        myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDir,metaDatos);
        linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaDatos, matDir);
        thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        int tickerTime=10*60*1000;

        while(true){

            java.text.DateFormat outdfm = new java.text.SimpleDateFormat("HHmmss.dd.MMMM.yyyy");
            outdfm.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

            java.util.Calendar currentTime=java.util.Calendar.getInstance();
            java.util.Calendar previousTime=java.util.Calendar.getInstance();
            java.util.Calendar nextTime=java.util.Calendar.getInstance();


            long currTimeMill=currentTime.getTimeInMillis();
            currTimeMill=(long)(Math.floor((double)currTimeMill/(10D*60D*1000D))*10*60*1000);
            
            currentTime.setTimeInMillis(currTimeMill-delay);
            previousTime.setTimeInMillis(currTimeMill-10*60*1000-delay);
            nextTime.setTimeInMillis(currTimeMill+10*60*1000-delay);


            String stringRainFileDate=outdfm.format(currentTime.getTime());
            String stringInicFileDate=outdfm.format(previousTime.getTime());

            System.out.println(stringRainFileDate);
            System.out.println(stringInicFileDate);

            RainfallGenerator theRain=new RainfallGenerator(stringRainFileDate);
            if(theRain.gotField()){
                
                double[] initialCondition=getInitialCondition(stringInicFileDate);

                hydroScalingAPI.examples.rainRunoffSimulations.RealTimeSystem.SimulationToAsciiFileRealTimeSystem simulator=
                    new hydroScalingAPI.examples.rainRunoffSimulations.RealTimeSystem.SimulationToAsciiFileRealTimeSystem(
                        myCuenca,
                        linksStructure,
                        thisNetworkGeom,
                        thisHillsInfo,
                        matDir, magnitudes, metaDatos,
                        initialCondition,
                        theRain.getPathToRain(),
                        0.5f,5, outputDirectory,routingParams);

                new hydroScalingAPI.examples.rainRunoffSimulations.RealTimeSystem.OutputSimulationKML(metaDatos, outputDirectory, myCuenca, matDir, "ClearCreekAtCoralville",stringRainFileDate);


                tickerTime=10*60*1000;
            } else {
                tickerTime=1*60*1000;
            }

            System.exit(0);

            System.out.println("Now waiting for next rainfall field");
            new visad.util.Delay(tickerTime);
        }
        
    }

    public double[] getInitialCondition(String label){
        double[] initialCondition=new double[linksStructure.contactsArray.length];

        java.io.File inicFile=new java.io.File(outputDirectory+"/NED_00159011_0_0-prec."+label+"-IR_0.5-Routing_GK_params_0.2_-0.1_0.28.csv");

        if(inicFile.exists()){
            try {
                java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(inicFile));
                String fullLine="";
                
                for (int i = 0; i < 11; i++) fullLine = fileMeta.readLine();

                String[] textIC=fullLine.split(",");

                for (int i = 0; i < initialCondition.length; i++) {
                    initialCondition[i]=Double.parseDouble(textIC[i+1]);
                }
                
                fileMeta.close();

            } catch (IOException ex) {
                Logger.getLogger(RealTimeSystemSimulationManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            java.util.Arrays.fill(initialCondition, 0.0);
        }

        return initialCondition;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) throws java.io.IOException, VisADException {
        
        java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/ClearCreek/NED_00159011.metaDEM");
        hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".dir"));

        metaModif.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
        
        metaModif.setLocationBinaryFile(new java.io.File(theFile.getPath().substring(0,theFile.getPath().lastIndexOf("."))+".magn"));
        metaModif.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaModif).getInt();
        
        java.util.Hashtable routingParams=new java.util.Hashtable();
        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.2f);
        routingParams.put("lambda2",-0.1f);
        routingParams.put("v_o", 0.28f);
        
        new RealTimeSystemSimulationManager(1570, 127, matDirs, magnitudes, metaModif, new java.io.File("/Users/ricardo/simulationResults/ClearCreek/RealTime"), routingParams);
        
    }
}
