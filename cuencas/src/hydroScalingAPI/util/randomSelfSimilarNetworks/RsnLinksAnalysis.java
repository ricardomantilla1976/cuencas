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
 * RsnLinksAnalysis.java
 *
 * Created on July 11, 2005, 9:51 AM
 */

package hydroScalingAPI.util.randomSelfSimilarNetworks;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Ricardo Mantilla
 */
public class RsnLinksAnalysis extends hydroScalingAPI.util.geomorphology.objects.LinksAnalysis{
    
    private hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure rsnNS;
    
    /** Creates a new instance of RsnLinksAnalysis */
    public RsnLinksAnalysis(hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure rsns) {
        
        rsnNS=rsns;
        
        this.OuletLinkNum=0;
        this.basinOrder=rsnNS.getNetworkOrder();
        this.magnitudeArray=rsnNS.getMagnitudes();
        this.basinMagnitude=magnitudeArray[0];
        this.connectionsArray = rsnNS.getConnectionStructure();
        this.nextLinkArray = rsnNS.getNextLinkArray();
        this.completeStreamLinksArray=rsnNS.getCompleteStreamLinksArray();
        
    }
    
    public float[][] getDistancesToOutlet(){
        float[][] dToOutlet=new float[2][magnitudeArray.length];
        dToOutlet[1]=getVarValues(1)[0].clone();
        java.util.Arrays.fill(dToOutlet[0],1);
        getDistance(dToOutlet,0);
        
        return dToOutlet;
        
    }
    
//    public float[][] getDistancesToOutlet(float[][] dToOutlet,int outlet){
//        java.util.Vector ofInterest=new java.util.Vector();
//        ofInterest.add(new float[]{dToOutlet[0][0],1});
//        for(int i=0;i<magnitudeArray.length;i++) if(dToOutlet[1][i] > 1) ofInterest.add(new float[]{dToOutlet[0][i],dToOutlet[1][i]});
//        dToOutlet=new float[2][ofInterest.size()];
//        for(int i=0;i<dToOutlet[0].length;i++){
//            float[] vals=(float[])ofInterest.get(i);
//            dToOutlet[0][i]=vals[0];
//            dToOutlet[1][i]=vals[1];
//        }
//            
//        return dToOutlet;
//        
//    }
    
    public void getDistance(float[][] dToOutlet,int li){
        for(int j=0;j<connectionsArray[li].length;j++) {
            dToOutlet[0][connectionsArray[li][j]]+=dToOutlet[0][li];
            dToOutlet[1][connectionsArray[li][j]]+=dToOutlet[1][li];
            getDistance(dToOutlet,connectionsArray[li][j]);
        }
        
    }
    
    public float[][] getVarValues(int varIndex){
        float[][] quantityArray=new float[1][connectionsArray.length];
        switch(varIndex){
            case 0:
                //Link's Area.  This is done by subtraction of area at head and area at incoming links head
                //java.util.Arrays.fill(quantityArray[0],0.10f); //0.1 is the average link area
                quantityArray=rsnNS.getLinkAreas();
                break;
            case 1:
                //Link's Length.  This is done by subtraction of tcl at head and tcl at incoming links head
                //java.util.Arrays.fill(quantityArray[0],0.30f); //0.3 is the average link lenght
                //for(int i=0;i<nextLinkArray.length;i++) quantityArray[0][i]=0.30f;
                //quantityArray=linkLengthArray;
                quantityArray=rsnNS.getLinkLengths();
                break;
            case 2:
                //Link's Upstream area.
                quantityArray=rsnNS.getUpAreas();
                break;
            case 3:
                //Link's drop
                java.util.Arrays.fill(quantityArray[0],1.0f);
                break;
            case 4:
                //Link's order
                quantityArray=rsnNS.getHortonOrders();
                break;
            case 5:
                //Total Channel Length
                quantityArray=rsnNS.getUpLength();
                break;
            case 6:
                //Link's Magnitude
                for(int i=0;i<magnitudeArray.length;i++)
                    quantityArray[0][i]=(float)magnitudeArray[i];
                break;
            case 7:
                //Link's Distance to Outlet
                break;
            case 8:
                //Link's Topologic Distance to Outlet
                break;
            case 9:
                //Link's Slope
                break;
            case 10:
                //Link's Elevation
                break;
            case 11:
                //Longest Channel Length
                break;
            case 12:
                //Binary Link Address
                break;
            case 13:
                //Total Channel Drop
                break;
        }
        
        return quantityArray;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        //main0(args);
        main1(args);
    }
    /**
     * @param args the command line arguments
     */
    public static void main0(String[] args) {
        
        
        float prevLevel_PWF=1;
        float thisLevel_PWF=0;
        float prevLevel_A=1;
        float thisLevel_A=0;
        
        System.out.println("Predictions for this scenario: RB=4.4, RC=2.2, Beta=0.46783549");
        System.out.println();
        System.out.println("Using Brent's strategy");
        System.out.println();
        
        for(int pVal=6;pVal<10;pVal++){
            
            System.out.println("n = "+pVal+" ");
            
            double probab=0.8;
            
            double numExperiments=1;
            
            for(int experiment=0;experiment<numExperiments;experiment++){
        
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.BinaryDistribution(1,1,probab);
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.BinaryDistribution(2,2,probab);
                RsnStructure myRSN=new RsnStructure(pVal,myUD_I,myUD_E);
                RsnLinksAnalysis myResults=new RsnLinksAnalysis(myRSN);

                
                /*float[][] hsorderLinks=myResults.getVarValues(4);

                int[] counterOrders=new int[myResults.getBasinOrder()];
                for(int j=0;j<myResults.completeStreamLinksArray.length;j++){
                    counterOrders[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]++;
                }

                double mean_R_B=0;
                double divider=0;
                for(int j=1;j<counterOrders.length;j++){
                    mean_R_B+=(1/(double)j)*counterOrders[counterOrders.length-j-1]/(double)counterOrders[counterOrders.length-j];
                    divider+=(1/(double)j);
                }
                mean_R_B/=divider;*/

                double[][] distances=myResults.getWidthFunctions(new int[] {0,2},0);
                int maxLinks=Integer.MIN_VALUE;
                for(int i=0;i<distances[0].length;i++){
                    maxLinks=Math.max(maxLinks,(int)distances[0][i]);
                    System.out.println(i+" "+(int)distances[0][i]);
                }

                maxLinks=Integer.MIN_VALUE;
                for(int i=0;i<distances[1].length;i++){
                    maxLinks=Math.max(maxLinks,(int)distances[1][i]);
                    System.out.println(i+" "+(int)distances[1][i]);
                }

                System.exit(0);

                thisLevel_A+=(myResults.getVarValues(2))[0][0];
                thisLevel_PWF+=maxLinks;

                //System.out.println(totalAreas+" "+peakWF+" "+mean_R_B);
                
                
                System.gc();
            }
            thisLevel_PWF/=numExperiments;
            thisLevel_A/=numExperiments;
            System.out.println("meanPWF= "+thisLevel_PWF+" meanPWF(n)/meanPWF(n-1)= "+(thisLevel_PWF/prevLevel_PWF)+" meanA= "+thisLevel_A+" meanA(n)/meanA(n-1)= "+(thisLevel_A/prevLevel_A)+" meanBeta= "+Math.log(thisLevel_PWF/prevLevel_PWF)/Math.log(thisLevel_A/prevLevel_A));
            prevLevel_PWF=thisLevel_PWF;
            prevLevel_A=thisLevel_A;
            thisLevel_PWF=0;
            thisLevel_A=0;
        }
        
        System.exit(0);
        
        System.out.println();
        System.out.println("Using Individual Trees strategy");
        System.out.println();
        //Seven p values 100 experiemnts each
        double[][] MVexperiment=new double[7][100];
        
        for(int pVal=0;pVal<7;pVal++){
            double probab=0.95-0.05*pVal;
            for(int experiment=0;experiment<100;experiment++){
        
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.BinaryDistribution(1,2,probab);
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.BinaryDistribution(2,3,probab);
                RsnStructure myRSN=new RsnStructure(8,myUD_I,myUD_E);
                RsnLinksAnalysis myResults=new RsnLinksAnalysis(myRSN);


                float[] meanAreas=new float[myResults.getBasinOrder()];
                float[] meanBifur=new float[myResults.getBasinOrder()];
                float[] meanPeakWF=new float[myResults.getBasinOrder()];
                int[] counterOrders=new int[myResults.getBasinOrder()];

                
                float[][] linkAreas=myResults.getVarValues(2);
                float[][] hsorderLinks=myResults.getVarValues(4);
                double[][] distances=myResults.getWidthFunctions(myResults.completeStreamLinksArray,0);
                for(int j=0;j<distances.length;j++){
                    int maxLinks=Integer.MIN_VALUE;
                    for(int i=0;i<distances[j].length;i++){
                        maxLinks=Math.max(maxLinks,(int)distances[j][i]);
                    }
                    meanAreas[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]+=linkAreas[0][myResults.completeStreamLinksArray[j]];
                    meanPeakWF[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]+=maxLinks;
                    counterOrders[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]++;
                }
                for(int j=0;j<meanPeakWF.length;j++){
                    meanPeakWF[j]/=(float)counterOrders[j];
                    meanAreas[j]/=(float)counterOrders[j];
                    meanBifur[j]=(float)counterOrders[j];
                }

                double meanPWFS=0,meanAS=0,meanBif=0;
                double mean_R_PWFS=0,mean_R_AS=0,mean_R_B=0;

                for(int j=3;j<meanPeakWF.length-2;j++){
                    mean_R_PWFS+=meanPeakWF[j]/meanPeakWF[j-1];
                    mean_R_AS+=meanAreas[j]/meanAreas[j-1];
                    mean_R_B+=meanBifur[meanPeakWF.length-j-1]/meanBifur[meanPeakWF.length-j];
                    meanPWFS+=Math.log(meanPeakWF[j]/meanPeakWF[j-1]);
                    meanAS+=Math.log(meanAreas[j]/meanAreas[j-1]);
                    meanBif+=Math.log(meanBifur[j-1]/meanBifur[j]);
                }
                MVexperiment[pVal][experiment]=meanPWFS/meanAS;

                System.out.print("Done with experiment "+(experiment+1)
                                    +" Beta: "+MVexperiment[pVal][experiment]
                                    +" Rpwf: ");

                for(int j=1;j<meanPeakWF.length;j++) System.out.print(meanPeakWF[j]/meanPeakWF[j-1]+" ");

                System.out.print(" Ra: "+mean_R_AS/(float)(meanPeakWF.length-5)
                                    +" Rb: "+mean_R_B/(float)(meanPeakWF.length-5));
                System.out.println();
                    
                
                System.gc();
            }
        }
        
        /*double[][] MVexperiment=new double[7][100];
        
        for(int pVal=0;pVal<7;pVal++){
            double Addition_I=0.80;
            double Coeff_I=0.5+1/70.0*pVal;
            double Base_I=Addition_I-Coeff_I;
            
            double Addition_E=0.77;
            double Coeff_E=0.5+1/70.0*pVal;
            double Base_E=Addition_I-Coeff_I;
            
            for(int experiment=0;experiment<100;experiment++){
        
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeneralGeometricDistribution(Coeff_I,Base_I,0);
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeneralGeometricDistribution(Coeff_E,Base_E,1);
                RsnStructure myRSN=new RsnStructure(6,myUD_I,myUD_E);
                RsnLinksAnalysis myResults=new RsnLinksAnalysis(myRSN);


                float[] meanAreas=new float[myResults.getBasinOrder()];
                float[] meanPeakWF=new float[myResults.getBasinOrder()];
                int[] counterOrders=new int[myResults.getBasinOrder()];

                try{
                    float[][] linkAreas=myResults.getVarValues(2);;
                    float[][] hsorderLinks=myResults.getVarValues(4);
                    double[][] distances=myResults.getTopologicWidthFunctions(myResults.completeStreamLinksArray);
                    for(int j=0;j<distances.length;j++){
                        int maxLinks=Integer.MIN_VALUE;
                        for(int i=0;i<distances[j].length;i++){
                            maxLinks=Math.max(maxLinks,(int)distances[j][i]);
                        }
                        meanAreas[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]+=linkAreas[0][myResults.completeStreamLinksArray[j]];
                        meanPeakWF[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]+=maxLinks;
                        counterOrders[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]++;
                    }
                    for(int j=0;j<meanPeakWF.length;j++){
                        meanPeakWF[j]/=(float)counterOrders[j];
                        meanAreas[j]/=(float)counterOrders[j];
                    }
                    
                    double meanPWFS=0,meanAS=0;
                    
                    for(int j=1;j<meanPeakWF.length-1;j++){
                        meanPWFS+=Math.log(meanPeakWF[j]/meanPeakWF[j-1]);
                        meanAS+=Math.log(meanAreas[j]/meanAreas[j-1]);
                    }
                    MVexperiment[pVal][experiment]=meanPWFS/meanAS;

                } catch(java.io.IOException ioe){
                    System.err.println(ioe);
                } catch(visad.VisADException vie){
                    System.err.println(vie);
                }
                
                
                System.out.println("Done with experiment "+(experiment+1));
                
                System.gc();
            }
        }*/
        
        System.out.print("RC,RB,p,");
        for(int experiment=0;experiment<100;experiment++) System.out.print("Beta "+(experiment+1)+",");
        System.out.println();
        for(int pVal=0;pVal<7;pVal++){
            double probab=0.95-0.05*pVal;
            System.out.print((3-probab)+","+(6-2*probab)+","+probab);
            
            /*double Addition_I=0.80;
            double Coeff_I=0.5+1/70.0*pVal;
            double Base_I=Addition_I-Coeff_I;
            
            double Addition_E=0.77;
            double Coeff_E=0.5+1/70.0*pVal;
            double Base_E=Addition_I-Coeff_I;
            
            double miu_i=1+Base_I/(1-Coeff_I)+Coeff_I*Base_I/Math.pow(1-Coeff_I,2.0);
            double miu_o=1+Base_E/(1-Coeff_E)+Coeff_E*Base_E/Math.pow(1-Coeff_E,2.0);
            
            System.out.print(miu_i+","+(miu_i+miu_o)+","+"OOO");*/
            
            for(int experiment=0;experiment<100;experiment++){
                System.out.print(","+MVexperiment[pVal][experiment]);
            }
            System.out.println();
        }
        
        
        System.exit(0);
    }
    
    public static void main1(String[] args) {
        
        int TreeScale=7;
        
        int[] sequenceI={2,1,0,0,5,3,1,0};
        int[] sequenceE={3,1,2,1,1,2,3,1};
        
        
        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.ScaleDependentSequenceDistribution(sequenceI);
        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.ScaleDependentSequenceDistribution(sequenceE);
//        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.ScaleDependentBinaryDistribution(1,1,0.5,TreeScale+1);
//        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.ScaleDependentBinaryDistribution(1,1,0.5,TreeScale+1);
//        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.BinaryDistribution(2,2,0.5f);
//        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.BinaryDistribution(2,2,0.5f);
        RsnStructure myRSN=new RsnStructure(TreeScale,myUD_I,myUD_E);
        RsnLinksAnalysis myResults=new RsnLinksAnalysis(myRSN);
        
        java.io.File theFile=new java.io.File("/Users/ricardo/temp/testRSNdecode.rsn");
        try {

            myRSN.writeRsnTreeDecoding(theFile);
        } catch (IOException ex) {
            Logger.getLogger(RsnLinksAnalysis.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        float[] meanAreas=new float[myResults.getBasinOrder()];
        float[] meanLength=new float[myResults.getBasinOrder()];
        float[] meanBifur=new float[myResults.getBasinOrder()];
        float[] meanPeakWF=new float[myResults.getBasinOrder()];
        float[] meanDtoPeakWF=new float[myResults.getBasinOrder()];
        int[] counterOrders=new int[myResults.getBasinOrder()];


        float[][] linkAreas=myResults.getVarValues(2);
        float[][] hsorderLinks=myResults.getVarValues(4);
        double[][] distances=myResults.getWidthFunctions(myResults.completeStreamLinksArray,0);
        System.out.println(java.util.Arrays.toString(distances[0]));
        for(int j=0;j<distances.length;j++){
            int maxLinks=Integer.MIN_VALUE;
            int DtoMax=0;
            for(int i=0;i<distances[j].length;i++){
                if(distances[j][i] >= maxLinks){
                    maxLinks=(int)distances[j][i];
                    DtoMax=i;
                }
            }
            DtoMax++;
            meanAreas[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]+=linkAreas[0][myResults.completeStreamLinksArray[j]];
            meanPeakWF[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]+=maxLinks;
            meanDtoPeakWF[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]+=DtoMax;
            meanLength[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]+=distances[j].length;
            counterOrders[(int)hsorderLinks[0][myResults.completeStreamLinksArray[j]]-1]++;
        }
        for(int j=0;j<meanPeakWF.length;j++){
            meanPeakWF[j]/=(float)counterOrders[j];
            meanAreas[j]/=(float)counterOrders[j];
            meanDtoPeakWF[j]/=(float)counterOrders[j];
            meanLength[j]/=(float)counterOrders[j];
            meanBifur[j]=(float)counterOrders[j];
            
            System.out.println( meanPeakWF[j]+":"+meanAreas[j]+":"+meanBifur[j]+":"+meanLength[j]+":"+meanDtoPeakWF[j]);
            
        }

        System.exit(0);
        
        double meanPWFS=0,meanAS=0,meanBif=0;
        double mean_R_PWFS=0,mean_R_AS=0,mean_R_B=0;

        for(int j=3;j<meanPeakWF.length-2;j++){
            mean_R_PWFS+=meanPeakWF[j]/meanPeakWF[j-1];
            mean_R_AS+=meanAreas[j]/meanAreas[j-1];
            mean_R_B+=meanBifur[meanPeakWF.length-j-1]/meanBifur[meanPeakWF.length-j];
            meanPWFS+=Math.log(meanPeakWF[j]/meanPeakWF[j-1]);
            meanAS+=Math.log(meanAreas[j]/meanAreas[j-1]);
            meanBif+=Math.log(meanBifur[j-1]/meanBifur[j]);
        }

        System.out.print("Done with experiment "
                            +" Beta: "+meanPWFS/meanAS
                            +" Rpwf: ");

        for(int j=1;j<meanPeakWF.length;j++) System.out.print(meanPeakWF[j]/meanPeakWF[j-1]+" ");

        System.out.print(" Ra: "+mean_R_AS/(float)(meanPeakWF.length-5)
                            +" Rb: "+mean_R_B/(float)(meanPeakWF.length-5));
        System.out.println();
    }
    
    public static void main2(String[] args) {
        
        
        
    }
    
}
