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
public class RsnFlowSimulationToAsciiFile extends java.lang.Object {
    
    hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure rsns;
    float rainIntensity;
    float rainDuration;
    float infiltRate;
    int routingType;
    java.io.File outputDirectory;
    
    hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis linksStructure;
    int basinOrder;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm;
    hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager infilMan;

    public RsnFlowSimulationToAsciiFile(hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure rsns_OR, float rainIntensity_OR, float rainDuration_OR, float infiltRate_OR, int routingType_OR, java.io.File outputDirectory_OR) throws java.io.IOException, VisADException{
        this(rsns_OR, rainIntensity_OR, rainDuration_OR, infiltRate_OR, routingType_OR, outputDirectory_OR,0.5f,-0.5f);
    }
    
    /** Creates new simulationsRep3 */
    public RsnFlowSimulationToAsciiFile(hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure rsns_OR, float rainIntensity_OR, float rainDuration_OR, float infiltRate_OR, int routingType_OR, java.io.File outputDirectory_OR,float exponentQ, float exponentA) throws java.io.IOException, VisADException{
        rsns=rsns_OR;
        rainIntensity=rainIntensity_OR;
        rainDuration=rainDuration_OR;
        infiltRate=infiltRate_OR;
        routingType=routingType_OR;
        outputDirectory=outputDirectory_OR;
        
        linksStructure=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis(rsns);
        basinOrder=linksStructure.getBasinOrder();
        
        thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        thisNetworkGeom.setWidthsHG(3.4f, 0.5f, 0.2f);
        thisNetworkGeom.setSlopesHG(0.02f, -0.5f, 0.3f);
        thisNetworkGeom.setCheziHG(14.2f, -1/3.0f);
        
        thisNetworkGeom.setVqParams((float)(1.0/Math.pow(0.01, exponentA))/300.0f/(1-exponentQ), 0.0f, exponentQ, exponentA);

        thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(linksStructure,rainIntensity,rainDuration);
        thisHillsInfo.setStormManager(storm);
        
        infilMan=new hydroScalingAPI.modules.rainfallRunoffModel.objects.InfiltrationManager(linksStructure,infiltRate);
        thisHillsInfo.setInfManager(infilMan);
        
    }
    
    public void executeSimulation() throws java.io.IOException, VisADException{
        
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
            case 3:     routingString="CM";
                        break;
            case 4:     routingString="VM";
                        break;
            case 5:     routingString="GK";
                        break;
        }
        
        java.io.File theFile1;
        
        theFile1=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+infiltRate+".rsn.csv");
        System.out.println("Writing RSN Decoding - "+theFile1);
        rsns.writeRsnTreeDecoding(theFile1);
        System.out.println("Done writing results");
        
        if(true) return;
        
        java.io.File theFile;
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+infiltRate+".wfs.csv");
        System.out.println("Writing Width Functions - "+theFile);
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

        double[][] wfs=linksStructure.getWidthFunctions(linksStructure.completeStreamLinksArray,0);
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1)){
                newfile.write("Link #"+linksStructure.completeStreamLinksArray[i]+",");
                for (int j=0;j<wfs[i].length;j++) newfile.write(wfs[i][j]+",");
                newfile.write("\n");
            }
        }
        
        newfile.close();
        bufferout.close();

        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-INT_"+rainIntensity+"-DUR_"+rainDuration+"-IR_"+infiltRate+"-Routing_"+routingString+".csv");

        System.out.println(theFile);
        
        salida = new java.io.FileOutputStream(theFile);
        bufferout = new java.io.BufferedOutputStream(salida);
        newfile = new java.io.OutputStreamWriter(bufferout);
        
        newfile.write("Information on Complete order Streams\n");
        newfile.write("Links at the bottom of complete streams are:\n");
        newfile.write("Link #,");

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1))
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
        }
        
        newfile.write("\n");
        newfile.write("Horton Order,");

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1))
                newfile.write(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i])+",");
        }

        newfile.write("\n");
        newfile.write("Upstream Area [km^2],");

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
	    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1))
                newfile.write(thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+",");
        }
        
        newfile.write("\n");
        newfile.write("Upstream Channel Lenght [km],");

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
	    if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1))
                newfile.write(thisNetworkGeom.upStreamTotalLength(linksStructure.completeStreamLinksArray[i])+",");
        }
        
        newfile.write("\n\n\n");
        newfile.write("Results of flow simulations in your basin");
        
        newfile.write("\n");
        newfile.write("Time,");

        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1))
                newfile.write("Link-"+linksStructure.completeStreamLinksArray[i]+",");
        }
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_Simple thisBasinEqSys=new hydroScalingAPI.modules.rainfallRunoffModel.objects.NetworkEquations_Simple(linksStructure,thisHillsInfo,thisNetworkGeom,routingType,1.0);
        double[] initialCondition=new double[linksStructure.connectionsArray.length*2];
        
        float[][] areasHillArray=thisHillsInfo.getAreasArray();
        float[][] linkLengths=linksStructure.getVarValues(1);   // [1][n]
        
        //double ic_sum = 0.0f;
        
        for (int i=0;i<linksStructure.connectionsArray.length;i++){
            initialCondition[i]=1.0;
            //initialCondition[i]=( areasHillArray[0][i]*100.*1e3 ) / ( linkLengths[0][i] *1e3 )  ;//0.0;
            //System.out.println(areasHillArray[0][i]);
            initialCondition[i+linksStructure.connectionsArray.length]=1;
            //System.out.println{"Sum of initial " + ic_sum};
            //ic_sum = ic_sum + initialCondition[i] ;
        }
        //System.out.println("Sum of initial q = " + ic_sum);
        
        java.util.Date startTime=new java.util.Date();
        System.out.println("Start Time:"+startTime.toString());
        System.out.println("Number of Links on this simulation: "+(initialCondition.length/2.0));
        System.out.println("Inicia simulacion RKF");
        
        hydroScalingAPI.util.ordDiffEqSolver.RKF rainRunoffRaining=new hydroScalingAPI.util.ordDiffEqSolver.RKF(thisBasinEqSys,1e-3,10/60.);
        
        int numPeriods = 1;
        
        numPeriods = (int) ((storm.stormFinalTimeInMinutes()-storm.stormInitialTimeInMinutes())/rainDuration);
        
        java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(storm.stormInitialTimeInMinutes()*60.*1000.0));
        System.out.println(thisDate.getTime());
        
        for (int k=0;k<numPeriods;k++) {
            System.out.println("Period"+(k)+" out of "+numPeriods);
            rainRunoffRaining.jumpsRunToIncompleteAsciiFile(storm.stormInitialTimeInMinutes()+k*rainDuration,storm.stormInitialTimeInMinutes()+(k+1)*rainDuration,rainDuration,initialCondition,newfile,linksStructure,thisNetworkGeom);
            initialCondition=rainRunoffRaining.finalCond;
            rainRunoffRaining.setBasicTimeStep(10/60.);
        }

        java.util.Date interTime=new java.util.Date();
        System.out.println("Intermedia Time:"+interTime.toString());
        System.out.println("Running Time:"+(.001*(interTime.getTime()-startTime.getTime()))+" seconds");

        //the discretization step should be Math.pow(2,(basinOrder-1))
        rainRunoffRaining.jumpsRunToIncompleteAsciiFile(storm.stormInitialTimeInMinutes()+numPeriods*rainDuration,(storm.stormInitialTimeInMinutes()+(numPeriods+1)*rainDuration)+20000,Math.pow(2,(basinOrder-1)),initialCondition,newfile,linksStructure,thisNetworkGeom);
 
        
        double[] maximumsAchieved=rainRunoffRaining.getMaximumAchieved();
        
        newfile.write("\n");
        newfile.write("\n");
        newfile.write("Maximum Discharge [m^3/s],");
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder-3,1))
                newfile.write(maximumsAchieved[linksStructure.completeStreamLinksArray[i]]+",");
        }
        
        
        System.out.println("Termina simulacion RKF");
        java.util.Date endTime=new java.util.Date();
        System.out.println("End Time:"+endTime.toString());
        System.out.println("Running Time:"+(.001*(endTime.getTime()-startTime.getTime()))+" seconds");
        
        newfile.close();
        bufferout.close();
        
        System.out.println("Done writing results");
        
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            //subMain1(args);   //Geometrically Distributed generators, constant link-length
            //subMain2(args);   //Geometrically Distributed generators, random link-length
            //subMain3(args);   //Geometrically Distributed generators, random link-length, random link-area
            //subMain4(args);   //Uniformly Distributed generators, constant link-length
            subMain5(args);   //Tokunaga Trees E(1,2,3,4)I(0,1,2,3,4)
            
            //subMain6(args);   //Geometrically Distributed generators, constant link-length, HG and Non-Linear Velocities
            
            //subMain7(args);   //E1I1 different lambda1 and lambda2
            
            //subMain8(args);   //Non-Self similar Trees
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        } catch (VisADException v){
            System.out.print(v);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
    public static void subMain1(String args[]) throws java.io.IOException, VisADException {
        
        String outDir="/ResultsSimulations/flowSimulations/geometricRSNs/constantV/";
        
        int iniExperiment=775;
        int finExperiments=999;
        
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumFractionDigits(2);
        
        for(double p_i=0.36;p_i<0.50;p_i+=0.02){
            for(double p_e=0.45;p_e<0.55;p_e+=0.02){
                new java.io.File(outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)).mkdir();
                for(int sofi=2;sofi<=7;sofi++){
                    new java.io.File(outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)+"/ord_"+sofi).mkdir();
                    
                    for(int experiment=iniExperiment;experiment<=finExperiments;experiment++){

                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(p_i,0);
                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(p_e,1);
                        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(sofi-1,myUD_I,myUD_E);
                        new RsnFlowSimulationToAsciiFile(myRSN,0,1,experiment,2,new java.io.File(outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)+"/ord_"+sofi)).executeSimulation();
                    }
                }
            }
        }
    }
    
    public static void subMain2(String args[]) throws java.io.IOException, VisADException {

        String outDir="/ResultsSimulations/flowSimulations/geometricRSNs/variableL/";
        
        int iniExperiment=750;
        int finExperiments=999;
        
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumFractionDigits(2);
        
        for(double p_i=0.36;p_i<0.50;p_i+=0.02){
            for(double p_e=0.45;p_e<0.55;p_e+=0.02){
                new java.io.File(outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)).mkdir();
                for(int sofi=2;sofi<=7;sofi++){
                    new java.io.File(outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)+"/ord_"+sofi).mkdir();
                    
                    for(int experiment=iniExperiment;experiment<=finExperiments;experiment++){

                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(p_i,0);
                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(p_e,1);
                        
                        float Elae=0.1f;
                        float SDlae=0.2f;
                        
                        hydroScalingAPI.util.probability.ContinuousDistribution myLinkAreaDistro_E=new hydroScalingAPI.util.probability.LogGaussianDistribution(Elae,SDlae);
                        hydroScalingAPI.util.probability.ContinuousDistribution myLinkAreaDistro_I=new hydroScalingAPI.util.probability.LogGaussianDistribution(0.01f+0.88f*Elae,0.04f+0.85f*SDlae);

                        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(sofi-1,myUD_I,myUD_E,myLinkAreaDistro_E,myLinkAreaDistro_I);
                        new RsnFlowSimulationToAsciiFile(myRSN,0,1,experiment,2,new java.io.File(outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)+"/ord_"+sofi)).executeSimulation();
                        
                    }
                }
            }
        }
    }
    
    public static void subMain3(String args[]) throws java.io.IOException, VisADException {
        
    }
    
    public static void subMain4(String args[]) throws java.io.IOException, VisADException {
        
        float prevLevel_PWF=1;
        float thisLevel_PWF=0;
        float prevLevel_A=1;
        float thisLevel_A=0;
        
        System.out.println("Predictions for this scenario: RB=4.4, RC=2.2, Theta=0.46783549");
        System.out.println();
        System.out.println("Using Brent's strategy");
        System.out.println();
        
        for(int pVal=7;pVal<=7;pVal++){
            
            System.out.print("n = "+pVal+" ");
            
            new java.io.File("/home/ricardo/workFiles/myWorkingStuff/MateriasDoctorado/PhD_Thesis/results/flowSimulations/p_"+pVal).mkdir();
            
            double probab=0.8;
            
            double numExperiments=100;
            
            for(int experiment=0;experiment<numExperiments;experiment++){
        
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.BinaryDistribution(1,2,probab);
                hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.BinaryDistribution(2,3,probab);
                hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(pVal,myUD_I,myUD_E);
                new RsnFlowSimulationToAsciiFile(myRSN,0,10,experiment,2,new java.io.File("/home/ricardo/workFiles/myWorkingStuff/MateriasDoctorado/PhD_Thesis/results/flowSimulations/p_"+pVal));
                
            }

        }

        System.exit(0);
        
    }
    
    public static void subMain5(String args[]) throws java.io.IOException, VisADException {
        
        System.out.println("Using Brent's strategy");
        System.out.println();
        
        for(int E=1;E<=4;E++){
            for(int I=0;I<=3;I++){
                for(int sofi=2;sofi<=7;sofi++){
                    
                    System.out.println("Tokunaga = E"+E+"I"+I);
                    
                    new java.io.File("/Users/ricardo/simulationResults/tokunaga/E"+E+"I"+I).mkdir();
                    new java.io.File("/Users/ricardo/simulationResults/tokunaga/E"+E+"I"+I+"/ord_"+sofi).mkdir();

                    double numExperiments=1;

                    for(int experiment=0;experiment<numExperiments;experiment++){

                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.BinaryDistribution(I,I,0.5);
                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.BinaryDistribution(E,E,0.5);
                        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(sofi,myUD_I,myUD_E);
                        RsnFlowSimulationToAsciiFile rsnfs=new RsnFlowSimulationToAsciiFile(myRSN,0,10,experiment,2,new java.io.File("/Users/ricardo/simulationResults/tokunaga/E"+E+"I"+I+"/ord_"+sofi));
                        rsnfs.executeSimulation();
                    }
                }
            }

        }

        System.exit(0);
        
    }
    
    public static void subMain6(String args[]) throws java.io.IOException, VisADException {
        
        String outDir="/home/ricardo/workFiles/myWorkingStuff/MateriasDoctorado/PhD_Thesis/results/flowSimulations/geometricRSNs/variableV/constantL/";
        
        int iniExperiment=0;
        int finExperiments=49;
        
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumFractionDigits(2);
        for(float expQ=0.3f;expQ<0.8;expQ+=0.1){
            for(float expA=-(expQ/2.0f-0.15f);expA>-(expQ/2.0f+0.1)-0.05;expA-=0.05){
                
                String fileString=outDir+"lamda1"+labelFormat.format(expQ)+"lamda2"+labelFormat.format(expA);
                new java.io.File(fileString).mkdir();
                
                for(double p_i=Float.parseFloat(args[0]);p_i<Float.parseFloat(args[0])+0.02;p_i+=0.02){
                //for(double p_i=0.36;p_i<0.50;p_i+=0.02){
                    for(double p_e=Float.parseFloat(args[1]);p_e<Float.parseFloat(args[1])+0.02;p_e+=0.02){
                    //for(double p_e=0.45;p_e<0.55;p_e+=0.02){
                        fileString=outDir+"lamda1"+labelFormat.format(expQ)+"lamda2"+labelFormat.format(expA)+"/p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e);
                        new java.io.File(fileString).mkdir();
                        for(int sofi=6;sofi<=6;sofi++){
                            
                            fileString=outDir+"lamda1"+labelFormat.format(expQ)+"lamda2"+labelFormat.format(expA)+"/p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)+"/ord_"+sofi;
                            
                            new java.io.File(fileString).mkdir();

                            for(int experiment=iniExperiment;experiment<=finExperiments;experiment++){

                                hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(p_i,0);
                                hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(p_e,1);
                                hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(sofi-1,myUD_I,myUD_E);
                                new RsnFlowSimulationToAsciiFile(myRSN,0,1,experiment,5,new java.io.File(fileString), expQ, expA).executeSimulation();

                            }
                        }
                    }
                }
            }
        }
    }
    
    public static void subMain7(String args[]) throws java.io.IOException, VisADException {
        
        String outDir="/home/ricardo/workFiles/myWorkingStuff/MateriasDoctorado/PhD_Thesis/results/flowSimulations/tokunaga/q0_is_0.5/";
        
        int iniExperiment=0;
        int finExperiments=0;
        
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumFractionDigits(2);
//        for(float expQ=0.3f;expQ<0.8;expQ+=0.1){
//            for(float expA=-(expQ/2.0f-0.15f);expA>-(expQ/2.0f+0.1)-0.05;expA-=0.05){
        for(float expQ=0.0f;expQ<0.1;expQ+=0.1){
            for(float expA=-(expQ/2.0f-0.15f);expA>-(expQ/2.0f+0.1)-0.05;expA-=0.05){
                
                String fileString=outDir+"lamda1"+labelFormat.format(expQ)+"lamda2"+labelFormat.format(expA);
                new java.io.File(fileString).mkdir();
                
                new java.io.File(fileString).mkdir();
                for(int sofi=6;sofi<=7;sofi++){

                    fileString=outDir+"lamda1"+labelFormat.format(expQ)+"lamda2"+labelFormat.format(expA)+"/ord_"+sofi;

                    new java.io.File(fileString).mkdir();

                    for(int experiment=iniExperiment;experiment<=finExperiments;experiment++){

                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.BinaryDistribution(1,1,1);
                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.BinaryDistribution(1,1,1);
                        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(sofi-1,myUD_I,myUD_E);
                        new RsnFlowSimulationToAsciiFile(myRSN,0,1,experiment,5,new java.io.File(fileString), expQ, expA).executeSimulation();

                    }
                }
            }
        }
    }
    
    public static void subMain8(String args[]) throws java.io.IOException, VisADException {
        
        int TreeScale=3;
        String outDir="/home/ricardo/simulationResults/nonSST/constantV/ord"+TreeScale;
        new java.io.File(outDir).mkdir();
        
        int[] sequence={1,3,1,3,1,3,1,3,1,3,1,3,1,3};
        
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumFractionDigits(2);
        
        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.ScaleDependentSequenceDistribution(sequence);
        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.ScaleDependentSequenceDistribution(sequence);
        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(TreeScale,myUD_I,myUD_E);
        new RsnFlowSimulationToAsciiFile(myRSN,0,1,0,2,new java.io.File(outDir)).executeSimulation();

    }
        
}
