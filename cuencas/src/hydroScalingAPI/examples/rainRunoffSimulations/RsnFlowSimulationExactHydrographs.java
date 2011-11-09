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
public class RsnFlowSimulationExactHydrographs extends java.lang.Object {
    
    private double maxDischarge=Double.MIN_VALUE;
    private double[][] coefficients;
    
    /** Creates matrix of coefficients for exact hydrograph */
    public RsnFlowSimulationExactHydrographs(hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure rsns) throws java.io.IOException, VisADException{
        
        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis linksStructure=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis(rsns);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);

        coefficients=new double[linksStructure.connectionsArray.length][];

        boolean incomplete=true;
        while(incomplete){
            
            incomplete=false;
            
            for (int i = 0; i < linksStructure.connectionsArray.length; i++) {
                if(coefficients[i] == null){
                    
                    incomplete=true;
                    
                    if(linksStructure.connectionsArray[i].length == 0){
                        coefficients[i]=new double[] {1};
                    } else {
                        if(coefficients[linksStructure.connectionsArray[i][0]] != null && coefficients[linksStructure.connectionsArray[i][1]] != null){
                            
                            int numCoeff=Math.max(coefficients[linksStructure.connectionsArray[i][0]].length,coefficients[linksStructure.connectionsArray[i][1]].length);
                            
                            coefficients[i]=new double[numCoeff+1];
                            
                            coefficients[i][0]=1;
                            
                            //System.out.println("link "+i);
                            //System.out.println("parent1 "+linksStructure.connectionsArray[i][0]);
                            for(int kk=1;kk<=coefficients[linksStructure.connectionsArray[i][0]].length;kk++){
                                //System.out.println("parent1_c_ "+(kk-1)+" "+coefficients[linksStructure.connectionsArray[i][0]][kk-1]);
                                coefficients[i][kk]+=coefficients[linksStructure.connectionsArray[i][0]][kk-1]/(double)kk;
                            }
                            
                            //System.out.println("parent2 "+linksStructure.connectionsArray[i][1]);
                            for(int kk=1;kk<=coefficients[linksStructure.connectionsArray[i][1]].length;kk++){
                                //System.out.println("parent2_c_ "+(kk-1)+" "+coefficients[linksStructure.connectionsArray[i][1]][kk-1]);
                                coefficients[i][kk]+=coefficients[linksStructure.connectionsArray[i][1]][kk-1]/(double)kk;
                            }
                            
                            //System.out.println("Coefficients = "+java.util.Arrays.toString(coefficients[i]));
                        }
                        
                    }
                }
            }
        }
        
        //maxDischarge=Math.max(maxDischarge,givenStep[1][0]);
            
    }
    
    public double getPeak(){
        return maxDischarge;
    }
    
    public double[] getCoefficients(int linkID){
        return coefficients[linkID];
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            subMain4(args);   //Bernulli Generators, constant link-length
            
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
        
        double thisLevelUA=1.0;
        double ratioUAs=1.0;
        
        double thisLevelPWF=1.0;
        double ratioPWFs=1.0;
        
        double thisLevelPF=1.0;
        double ratioPFs=1.0;
        
        System.out.println("Order,Mean PF,Mean PWF,Mean UA,Ratio PF,Ratio PWF,Ratio UA");
        
        for(int pVal=2;pVal<8;pVal++){
            
            System.out.print("Order = "+(pVal)+" /");
            
            double probab=0.9;
            
            double numExperiments=50;
            
            double averageUA=0.0;
            double averagePWF=0.0;
            double averagePF=0.0;
            
            for(int experiment=0;experiment<numExperiments;experiment++){
        
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.BinaryDistribution(1,1,probab);
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.BinaryDistribution(2,2,probab);
                
                hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(pVal,myUD_I,myUD_E);
                hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis myResults=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis(myRSN);
                
                float[][] linkAreas=myResults.getVarValues(2);
                averageUA+=linkAreas[0][0];
                
                double[][] distances=myResults.getWidthFunctions(new int[] {0},1);
                int maxLinks=Integer.MIN_VALUE;
                for(int i=0;i<distances[0].length;i++){
                    maxLinks=Math.max(maxLinks,(int)distances[0][i]);
                }
                
                System.out.println("Width Function");
                System.out.println("CoefficientsWF = "+java.util.Arrays.toString(distances[0]));
                for (int i = 0; i < distances[0].length; i++) {
                    System.out.println((i+1)+" "+distances[0][i]);
                    
                }
                
                averagePWF+=maxLinks;
                
                RsnFlowSimulationExactHydrographs theSimulation=new RsnFlowSimulationExactHydrographs(myRSN);
                double[] coeffs=theSimulation.getCoefficients(0);
                System.out.println("CoefficientsHY = "+java.util.Arrays.toString(coeffs));
                
                System.out.println("Polynomium");

                System.out.print("1");
                for (int i = 1; i < coeffs.length; i++) {
                    System.out.print("+"+coeffs[i]+"*A1^"+i);
                    
                }
                System.out.println();
                System.out.println("Derivative");
                
                System.out.print((float)(coeffs[1]-1));
                for (int i = 2; i < coeffs.length; i++) {
                    System.out.print("+"+(coeffs[i]*i-coeffs[i-1])+"*A1^"+(i-1));
                    
                }
                System.out.print("-"+(coeffs[coeffs.length-1])+"*A1^"+(coeffs.length-1));
                System.out.println();
                System.exit(0);
                
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
