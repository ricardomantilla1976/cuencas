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
public class RsnWFSToAsciiFile extends java.lang.Object {
    
    private hydroScalingAPI.io.MetaRaster metaDatos;
    private byte[][] matDir;
    
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

    public RsnWFSToAsciiFile(hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure rsns_OR, float rainIntensity_OR, float rainDuration_OR, float infiltRate_OR, int routingType_OR, java.io.File outputDirectory_OR) throws java.io.IOException, VisADException{
        this(rsns_OR, rainIntensity_OR, rainDuration_OR, infiltRate_OR, routingType_OR, outputDirectory_OR,0.5f,-0.5f);
    }
    
    /** Creates new simulationsRep3 */
    public RsnWFSToAsciiFile(hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure rsns_OR, float rainIntensity_OR, float rainDuration_OR, float infiltRate_OR, int routingType_OR, java.io.File outputDirectory_OR,float exponentQ, float exponentA) throws java.io.IOException, VisADException{
        rsns=rsns_OR;
        rainIntensity=rainIntensity_OR;
        rainDuration=rainDuration_OR;
        infiltRate=infiltRate_OR;
        routingType=routingType_OR;
        outputDirectory=outputDirectory_OR;
        
        linksStructure=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnLinksAnalysis(rsns);
        basinOrder=linksStructure.getBasinOrder();
        
        
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
        java.io.File theFile;
        theFile=new java.io.File(outputDirectory.getAbsolutePath()+"/"+demName+"-SN_"+infiltRate+".wfs.csv");
        java.io.FileOutputStream salida = new java.io.FileOutputStream(theFile);
        java.io.BufferedOutputStream bufferout = new java.io.BufferedOutputStream(salida);
        java.io.OutputStreamWriter newfile = new java.io.OutputStreamWriter(bufferout);

        double[][] wfs=linksStructure.getWidthFunctions(new int[] {0},0);
        newfile.write("Link #0,");
        for (int j=0;j<wfs[0].length;j++) newfile.write(wfs[0][j]+",");
        
        newfile.close();
        bufferout.close();

    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            
            subMain1(args);   //Geometrically Distributed generators
            
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
        
        String outDir="/home/ricardo/simulationResults/widthFunctions/geometricRSNs/constantL/";
        
        int iniExperiment=500;
        int finExperiments=999;
        
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumFractionDigits(2);
        
        for(double p_i=0.40;p_i<0.50;p_i+=0.02){
            for(double p_e=0.45;p_e<0.55;p_e+=0.02){
                new java.io.File(outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)).mkdir();
                for(int sofi=8;sofi<=8;sofi++){
                    new java.io.File(outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)+"/ord_"+sofi).mkdir();
                    
                    for(int experiment=iniExperiment;experiment<=finExperiments;experiment++){

                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(p_i,0);
                        hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(p_e,1);
                        hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure myRSN=new hydroScalingAPI.util.randomSelfSimilarNetworks.RsnStructure(sofi-1,myUD_I,myUD_E);
                        new RsnWFSToAsciiFile(myRSN,0,1,experiment,2,new java.io.File(outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)+"/ord_"+sofi)).executeSimulation();
                        System.out.println("completed "+outDir+"p_i"+labelFormat.format(p_i)+"p_e"+labelFormat.format(p_e)+"/ord_"+sofi+"/Network "+experiment);
                    }
                }
            }
        }
    }
        
}
