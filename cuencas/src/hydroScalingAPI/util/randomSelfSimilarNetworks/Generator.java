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
 * Generator.java
 *
 * Created on July 11, 2005, 10:42 AM
 */

package hydroScalingAPI.util.randomSelfSimilarNetworks;

/**
 * Implementation of the Random Self-similar Networks generation algorithm.  The
 * constructor for this class is recursive.
 * @author Ricardo Mantilla
 */
public class Generator {
    
    private String genID;
    private Generator[] subTree;
    
    /**
     * Creates a new instance of Generator
     * @param extInt A flag indicating if this is an exterior (0) or an interior generator
     * @param generation The level of the generation of this Generator.  Note: Generation = 0 indicates
     * that the members of this Generator have no descendents
     * @param myIntDis An object describing the probability distribution of the size of the descendency
     * for interior pseudo-links
     * @param myExtDis An object describing the probability distribution of the size of the descendency
     * for exterior pseudo-links
     * @param familyID An ID for the parent family
     */
    public Generator(   int extInt,
                        int generation,
                        hydroScalingAPI.util.probability.DiscreteDistribution myIntDis,
                        hydroScalingAPI.util.probability.DiscreteDistribution myExtDis,
                        String familyID) {
                            
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(4);
                            
        genID=familyID;
        
        String labelSubTree;
        
        if(generation>0){
            int numMembers;
            String GenClass="";
            if(extInt==0){
                numMembers=myExtDis.sample();
                GenClass=",E";
            } else{
                numMembers=myIntDis.sample();
                GenClass=",I";
            }
            
            subTree=new Generator[2*numMembers+1];
            if(numMembers>0){
                for(int i=0;i<2*numMembers;i++){
                    String LinkClass=",I";
                    if(1-i%2==0) LinkClass=",E";
                    
                    if (generation == 1) 
                        labelSubTree=""+labelFormat.format(i)+LinkClass;
                    else
                        labelSubTree=""+labelFormat.format(i);
                        
                    
                    subTree[i]=new Generator(1-i%2,generation-1, myIntDis,myExtDis, familyID+","+labelSubTree);
                }
            }
            if (generation == 1) 
                labelSubTree=""+labelFormat.format(2*numMembers)+GenClass;
            else
                labelSubTree=""+labelFormat.format(2*numMembers);
            subTree[2*numMembers]=new Generator(extInt, generation-1, myIntDis,myExtDis, familyID+","+labelSubTree);
        }
    }
    
    /**
     * Creates a new instance of Generator
     * @param extInt A flag indicating if this is an exterior (0) or an interior generator
     * @param generation The level of the generation of this Generator.  Note: Generation = 0 indicates
     * that the members of this Generator have no descendents
     * @param myIntDis An object describing the probability distribution of the size of the descendency
     * for interior pseudo-links
     * @param myExtDis An object describing the probability distribution of the size of the descendency
     * for exterior pseudo-links
     * @param familyID An ID for the parent family
     */
    public Generator(   int extInt,
                        int generation,
                        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myIntDis,
                        hydroScalingAPI.util.probability.ScaleDependentDiscreteDistribution myExtDis,
                        String familyID) {
                            
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(4);
                            
        genID=familyID;
        
        String labelSubTree;
        
        if(generation>0){
            int numMembers;
            String GenClass="";
            if(extInt==0){
                numMembers=myExtDis.sample(generation);
                GenClass=",E";
            } else{
                numMembers=myIntDis.sample(generation);
                GenClass=",I";
            }
            
            subTree=new Generator[2*numMembers+1];
            if(numMembers>0){
                for(int i=0;i<2*numMembers;i++){
                    String LinkClass=",I";
                    if(1-i%2==0) LinkClass=",E";
                    
                    if (generation == 1) 
                        labelSubTree=""+labelFormat.format(i)+LinkClass;
                    else
                        labelSubTree=""+labelFormat.format(i);
                        
                    
                    subTree[i]=new Generator(1-i%2,generation-1, myIntDis,myExtDis, familyID+","+labelSubTree);
                }
            }
            if (generation == 1) 
                labelSubTree=""+labelFormat.format(2*numMembers)+GenClass;
            else
                labelSubTree=""+labelFormat.format(2*numMembers);
            subTree[2*numMembers]=new Generator(extInt, generation-1, myIntDis,myExtDis, familyID+","+labelSubTree);
        }
    }
    
    /**
     * Returns a String with the tree id (family name)
     * @return The id (family name) of the tree
     */
    public String getGenID(){
        return genID;
    }
    
    /**
     * Returns a String[] with a human readable codification for the tree.
     * @return The tree codification
     */
    public String[] decodeRsnTree(){
        String baseID="";
        for(int i=0;i<subTree.length;i++){
            baseID+=decodeBranch(baseID,subTree[i]);
        }
        return baseID.split(":");
    }
    
    private String decodeBranch(String baseID,Generator currentBranch){
        Generator[] branchBranches=currentBranch.getSubTree();
        if(branchBranches == null) return currentBranch.getGenID()+":";
        String branchID="";
        for(int i=0;i<branchBranches.length;i++){
            branchID+=decodeBranch(baseID,branchBranches[i]);
        }
        return branchID;
    }
    
    private Generator[] getSubTree(){
        return subTree;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

        main0(args); //Writes IDL code with some RSNs
        
    }
    
    public static void main0(String[] args) {
        java.text.NumberFormat labelFormat = java.text.NumberFormat.getNumberInstance();
        labelFormat.setGroupingUsed(false);
        labelFormat.setMinimumIntegerDigits(4);
        
        for(int i=1;i<=5;i++){
        
            System.out.println("pro getData_for_embeding_known_RSN_"+args[0]+"_"+i);
            System.out.println("common topology,rsnTopology");
            System.out.println("");
            System.out.println("rsnTopology=[$");
            hydroScalingAPI.util.probability.DiscreteDistribution myUD_I=new hydroScalingAPI.util.probability.GeometricDistribution(0.44, 0);
            hydroScalingAPI.util.probability.DiscreteDistribution myUD_E=new hydroScalingAPI.util.probability.GeometricDistribution(0.46, 1);
            Generator testGen=new Generator(0,i,myUD_I,myUD_E,labelFormat.format(0));
            String[] decT=testGen.decodeRsnTree();
            for(int j=0;j<decT.length-1;j++){
                String[] trimedDecode=decT[j].split(",");
                for(int k=0;k<trimedDecode.length;k++) System.out.print("'"+trimedDecode[k]+"'"+",");
                System.out.println("$");
            }
            String[] trimedDecode=decT[decT.length-1].split(",");
            System.out.print("'"+trimedDecode[0]+"'");
            for(int k=1;k<trimedDecode.length;k++) System.out.print(","+"'"+trimedDecode[k]+"'");
            System.out.println("]");
            
            System.out.println("");
            System.out.println("end");
            System.out.println("");
        }
        
    }
    
}
