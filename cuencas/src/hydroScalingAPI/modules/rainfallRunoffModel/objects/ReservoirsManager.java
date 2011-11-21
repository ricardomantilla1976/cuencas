/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 *
 * @author ricardo
 */
public class ReservoirsManager {
    
    int[] resLocations;
    double[] maxStorages;
    double[] maxDepths;
    double[] weirLengths;
    double[] orficeDiameter;
    
    
    public ReservoirsManager(hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom,hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure) {
        
        java.util.Vector<String> resLoc=new java.util.Vector<String>();
        
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
                if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) == 2) {
//                    System.out.println("A reservoir at link# "+linksStructure.completeStreamLinksArray[i] +"With upstrea Area "+thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i]));
                    resLoc.add(""+linksStructure.completeStreamLinksArray[i]);
                }
        }
        
        resLocations = new int[resLoc.size()];
        for (int i = 0; i < resLocations.length; i++) {
            resLocations[i]=Integer.parseInt(resLoc.get(i));   
        }
        
//        System.out.println(java.util.Arrays.toString(resLocations));
//        System.exit(0);
        
        resLocations = new int[]{2526};//{16,34};
        maxStorages=new double[] {466663.38};
        maxDepths = new double[] {7.03};
        weirLengths = new double[] {4};
        orficeDiameter = new double[] {1};
        
    }
    
    public int[] getReservoirLocations(){
        return resLocations;
    }
    
    public double[] getReservoirMaxStorages(){
        return maxStorages;
    }
    
    public double getReservoirYield(int i,double storage, double NaturalYield){
        
        double yield;
        double deltaT = 1;
        
        double h = maxDepths[i]*Math.pow(storage/maxStorages[i],0.26);
//        System.out.println(h);
        if(h<orficeDiameter[i])
        {
           yield = NaturalYield; 
        }
        else if(h<maxDepths[i]-1)
        {
            yield = (deltaT*0.6*0.25*Math.PI*Math.pow(orficeDiameter[i], 2)*Math.sqrt(2*9.81*h));
//            if(yield > NaturalYield) yield = NaturalYield;
        }
        else if(h>maxDepths[i]-1&& h<maxDepths[i])
        {
            yield = (deltaT*0.6*0.25*Math.PI*Math.pow(orficeDiameter[i], 2)*Math.sqrt(2*9.81*h)+deltaT*3.1*weirLengths[i]*Math.pow(h-(maxDepths[i]-1), 1.5));
//            System.out.println("<<<<WEIR IS BEING USED???>>>>");
//            System.out.println(h+" "+yield);
//            System.exit(0);
        }
        else
        {
            yield = NaturalYield;
//            System.out.println("<<<<WEIR IS BEING USED???>>>>");
//            System.out.println(h);
//            System.exit(0);
        }
        
//        yield = 0;
//          yield = NaturalYield;

        
        
//        System.out.println("Comparing Flows: Yield ="+yield+" Natural Flow = "+NaturalYield+ "For H="+h);       
        
        
        
        
        return yield;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
}
