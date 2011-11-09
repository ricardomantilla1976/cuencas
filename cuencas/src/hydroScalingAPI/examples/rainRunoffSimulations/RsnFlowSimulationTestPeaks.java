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

import visad.*;

/**
 *
 * @author Ricardo Mantilla
 */
public class RsnFlowSimulationTestPeaks extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
    private double maxDischarge=Double.MIN_VALUE;
    
    /** Creates new simulationsRep3 */
    public RsnFlowSimulationTestPeaks(hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure rsns, float rainIntensity, float rainDuration, float infiltRate, int routingType) throws java.io.IOException, VisADException{
        
        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis linksStructure=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis(rsns);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        thisNetworkGeom.setWidthsHG(5.6f, 0.46f,0.0f);
        thisNetworkGeom.setCheziHG(14.2f, -1/3.0f);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
       
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
        storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure,rainIntensity,rainDuration);
        thisHillsInfo.setStormManager(storm);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infilMan;
        infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,infiltRate);
        thisHillsInfo.setInfManager(infilMan);
        /*
            Escribo en un theFile lo siguiente:
                    Numero de links
                    Numero de links Completos
                    lista de Links Completos
                    Area aguas arriba de los Links Completos
                    Orden de los Links Completos
                    maximos de la WF para los links completos
                    Longitud simulacion
                    Resulatdos

         */
        String demName="RSN_result";
        String routingString="";
        switch (routingType) {
            case 0:     routingString="VC";
                        break;
            case 1:     routingString="CC";
                        break;
            case 2:     routingString="CV";
                        break;
        }
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_Simple thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_Simple(linksStructure,thisHillsInfo,thisNetworkGeom,routingType,2);
        double[] initialCondition=new double[linksStructure.connectionsArray.length*2];
        
        float[][] areasHillArray=thisHillsInfo.getAreasArray();
        float[][] linkLengths=linksStructure.getVarValues(1);   // [1][n]
        //double ic_sum = 0.0f;
        
        for (int i=0;i<linksStructure.connectionsArray.length;i++){
            initialCondition[i]=( areasHillArray[0][i]*1.*1e3 ) / ( linkLengths[0][i] *1e3 )  ;//0.0;
            //System.out.println(areasHillArray[0][i]);
            initialCondition[i+linksStructure.connectionsArray.length]=1;
            //System.out.println{"Sum of initial " + ic_sum};
            //ic_sum = ic_sum + initialCondition[i] ;
        }
        //System.out.println("Sum of initial q = " + ic_sum);
        
        /*java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/2.0));
        System.out.println("Inicia simulacion RKF");*/
        
        double basicTimeStep=10/60.;
        
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-3,basicTimeStep);
        
        /*java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());*/
        
        double currentTime=storm.stormInitialTimeInMinutes();
        
        double[][] givenStep=new double[][] {{0.0},initialCondition};
        maxDischarge=Math.max(maxDischarge,givenStep[1][0]);

        while(givenStep[1][0] > 1e-3){
            
            //givenStep=rainRunoffRaining.step(currentTime, initialCondition , basicTimeStep,false);
            basicTimeStep=givenStep[0][0];
            currentTime+=basicTimeStep;
            givenStep[0][0]=currentTime;
            initialCondition=givenStep[1];
            
            maxDischarge=Math.max(maxDischarge,givenStep[1][0]);
            
            //System.out.println("Time = "+currentTime+" Delta_t = "+basicTimeStep+" Q = "+givenStep[1][0]);
            
        }
        
        /*System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");*/
        
        
        
    }
    
    public double getPeak(){
        return maxDischarge;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            //subMain4(args);   //Bernulli Generators, constant link-length
            subMain3(args);   //Geometric Generators, constant link-length
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain4(String args[]) throws java.io.IOException, VisADException {
        
        float prevLevel_PWF=1;
        float thisLevel_PWF=0;
        float prevLevel_A=1;
        float thisLevel_A=0;
        
        System.out.println("Predictions for this scenario: RB=4.2, RC=2.1, Theta=0.4830");
        System.out.println();
        System.out.println("Using Brent's strategy");
        System.out.println();
        
        
        double thisLevelUA=1.0;
        double ratioUAs=1.0;
        
        double thisLevelPWF=1.0;
        double ratioPWFs=1.0;
        
        double thisLevelPF=1.0;
        double ratioPFs=1.0;
        
        System.out.println("Order,Mean PF,Mean PWF,Mean UA,Ratio PF,Ratio PWF,Ratio UA");
        
        for(int pVal=1;pVal<1;pVal++){
            
            System.out.print("Order = "+(pVal+1)+" /");
            
            double probab=0.9;
            
            double numExperiments=50;
            
            double averageUA=0.0;
            double averagePWF=0.0;
            double averagePF=0.0;
            
            for(int experiment=0;experiment<numExperiments;experiment++){
        
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.BinaryDistribution(1,2,probab);
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.BinaryDistribution(2,3,probab);
                hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(pVal,myUD_I,myUD_E);
                hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis myResults=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis(myRSN);
                
                float[][] linkAreas=myResults.getVarValues(2);
                averageUA+=linkAreas[0][0];
                
                double[][] distances=myResults.getWidthFunctions(new int[] {0},0);
                int maxLinks=Integer.MIN_VALUE;
                for(int i=0;i<distances[0].length;i++){
                    maxLinks=Math.max(maxLinks,(int)distances[0][i]);
                }
                
                averagePWF+=maxLinks;
                
                RsnFlowSimulationTestPeaks theSimulation=new RsnFlowSimulationTestPeaks(myRSN,0,10,experiment,2);
                averagePF+=theSimulation.getPeak();
                System.out.print("*");
            }
            
            averageUA/=numExperiments;
            averagePWF/=numExperiments;
            averagePF/=numExperiments;
            
            ratioUAs=averageUA/thisLevelUA;
            ratioPWFs=averagePWF/thisLevelPWF;
            ratioPFs=averagePF/thisLevelPF;
            
            thisLevelUA=averageUA;
            thisLevelPWF=averagePWF;
            thisLevelPF=averagePF;
            
            System.out.println("/");
            System.out.println((pVal+1)+","+thisLevelPF+","+thisLevelPWF+","+thisLevelUA+","+ratioPFs+","+ratioPWFs+","+ratioUAs);

        }

        System.exit(0);
        
    }
    
    public static void subMain3(String args[]) throws java.io.IOException, VisADException {
        
        float prevLevel_PWF=1;
        float thisLevel_PWF=0;
        float prevLevel_A=1;
        float thisLevel_A=0;
        
        
        double thisLevelUA=1.0;
        double ratioUAs=1.0;
        
        double thisLevelPWF=1.0;
        double ratioPWFs=1.0;
        
        double thisLevelPF=1.0;
        double ratioPFs=1.0;
        
            
        System.out.println();
        System.out.println("Order,Mean PF,Mean PWF,Mean UA,Ratio PF,Ratio PWF,Ratio UA");

        for(int pVal=1;pVal<8;pVal++){

            System.out.print("Order = "+(pVal+1)+" /");

            double numExperiments=50;

            double averageUA=0.0;
            double averagePWF=0.0;
            double averagePF=0.0;

            for(int experiment=0;experiment<numExperiments;experiment++){

                hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(0.436,0);
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(0.460,1);
                hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(pVal,myUD_I,myUD_E);
                hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis myResults=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis(myRSN);

                float[][] linkAreas=myResults.getVarValues(2);
                averageUA+=linkAreas[0][0];

                double[][] distances=myResults.getWidthFunctions(new int[] {0},0);
                int maxLinks=Integer.MIN_VALUE;
                for(int i=0;i<distances[0].length;i++){
                    maxLinks=Math.max(maxLinks,(int)distances[0][i]);
                }

                averagePWF+=maxLinks;

                RsnFlowSimulationTestPeaks theSimulation=new RsnFlowSimulationTestPeaks(myRSN,0,10,experiment,2);
                averagePF+=theSimulation.getPeak();
                System.out.print("*");
            }

            averageUA/=numExperiments;
            averagePWF/=numExperiments;
            averagePF/=numExperiments;

            ratioUAs=averageUA/thisLevelUA;
            ratioPWFs=averagePWF/thisLevelPWF;
            ratioPFs=averagePF/thisLevelPF;

            thisLevelUA=averageUA;
            thisLevelPWF=averagePWF;
            thisLevelPF=averagePF;

            System.out.println("/");
            System.out.println((pVal+1)+","+thisLevelPF+","+thisLevelPWF+","+thisLevelUA+","+ratioPFs+","+ratioPWFs+","+ratioUAs);

        }

        System.exit(0);
        
    }
        
}
