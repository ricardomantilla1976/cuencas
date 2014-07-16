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
    
    
    public ReservoirsManager(hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom,hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, int[] resLocOR) {
        
        java.util.Vector<String> resLoc=new java.util.Vector<String>();
        java.util.Vector<String> upsArea=new java.util.Vector<String>();
        System.out.println("lat"+","+"long");
        int numCols = linksStructure.localMetaRaster.getNumCols();
        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
//                if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) == 3) {
                if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) == 4 || thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) == 5) {
                    double A1 = thisNetworkGeom.upStreamArea(linksStructure.connectionsArray[linksStructure.completeStreamLinksArray[i]][0]);
                    double A2 = thisNetworkGeom.upStreamArea(linksStructure.connectionsArray[linksStructure.completeStreamLinksArray[i]][1]);
                    int reservID=0;
                    double upArea=0.;
                    if(A1>A2){reservID=linksStructure.connectionsArray[linksStructure.completeStreamLinksArray[i]][0]; upArea=A1;} else{reservID=linksStructure.connectionsArray[linksStructure.completeStreamLinksArray[i]][1];upArea=A2;}
                    
                    int loctID = linksStructure.contactsArray[reservID];
                    int yy= loctID/numCols;
                    int xx= loctID%numCols;
                    
                    double longitude = xx*linksStructure.localMetaRaster.getResLon()/3600.+linksStructure.localMetaRaster.getMinLon();
                    double latitude = yy*linksStructure.localMetaRaster.getResLat()/3600.+linksStructure.localMetaRaster.getMinLat();
                    
                    
//                    System.out.println(latitude+","+longitude);
                    
//                    System.out.println("A reservoir at link# "+linksStructure.completeStreamLinksArray[i] +"With upstrea Area "+thisNetworkGeom.upStreamArea(linksStructure.completeStreamLinksArray[i])+"  "+A1+"  "+A2);
//                    System.out.println("A reservoir at link# "+reservID+"  "+A1+"  "+A2);
//                    resLoc.add(""+linksStructure.completeStreamLinksArray[i]);
//                    System.out.print("IC["+reservID+"]"+" + "+","+" +");
//                    System.out.print(reservID+",");
//                    System.out.print("Link-"+reservID+" + "+","+" +");
                    resLoc.add(""+reservID);
                    upsArea.add(""+upArea);
                }
        }
//        System.exit(0);
        resLocations = resLocOR;
//        resLocations = new int[resLoc.size()];
        maxStorages=new double[resLocations.length] ;
        maxDepths = new double[resLocations.length] ; //This has to always be larger than 1m plus orfice diameter
        weirLengths = new double[resLocations.length] ;
        orficeDiameter = new double[resLocations.length];
        
        for (int i = 0; i < resLocations.length; i++) {
//            resLocations[i]     =Integer.parseInt(resLoc.get(i));   
            maxStorages[i]      = 52348.97;
            maxDepths [i]       =4.2672; //This has to always be larger than 1m plus orfice diameter
            weirLengths [i]     = 4.2672;
            orficeDiameter[i]   = 0.2032;
//            orficeDiameter[i]   = 0.75+0.152685*(Double.parseDouble(upsArea.get(i))-4.329947948);
//            System.out.println(resLocations[i]);            
        }
        
//        System.out.println(java.util.Arrays.toString(resLocations));
//        System.exit(0);
//        
//        resLocations = new int[]{2526};//{16,34}2524;
//        maxStorages=new double[] {5220156.7};//3092149.6//37912829.27//3505704.58;131644.1
//        maxDepths = new double[] {7}; //This has to always be larger than 1m plus orfice diameter
//        weirLengths = new double[] {4};
//        orficeDiameter = new double[] {0};
        
    }

    public ReservoirsManager() {
         resLocations=null;
         maxStorages=null;
         maxDepths=null;
         weirLengths=null;
         orficeDiameter=null;
    }
    
    public int[] getReservoirLocations(){
        return resLocations;
    }
     public void setReservoirLocations(int[] locations){
        this.resLocations=locations;
    }
    
    public double[] getReservoirMaxStorages(){
        return maxStorages;
    }
    
    public double getReservoirYield(int i,double storage, double NaturalConductivity,double upstreamYield){
        
        double yield;
        double deltaT = 1;
        double freeBoard = 4.2672-2.4384 ;
        double alpha =0.47;//0.26
        
        double h = maxDepths[i]*Math.pow(storage/maxStorages[i],alpha);
        double NaturalYield=NaturalConductivity*storage; 
//        System.out.println(h);
        if (h < orficeDiameter[i]) {
            yield = NaturalYield;
        } else if (h < maxDepths[i] - freeBoard) {
            yield = (deltaT * 0.6 * 0.25 * Math.PI * Math.pow(orficeDiameter[i], 2) * Math.sqrt(2 * 9.81 * h));
//            if (yield > NaturalYield) {
//                yield = NaturalYield;
//            }
//            System.out.println("H: "+h);
        } else if (h < maxDepths[i]) {
            yield = (deltaT * 0.6 * 0.25 * Math.PI * Math.pow(orficeDiameter[i], 2) * Math.sqrt(2 * 9.81 * h) + deltaT * 3.1 * weirLengths[i] * Math.pow(h - ( maxDepths[i] - freeBoard), 1.5));
        } else {
            yield = NaturalConductivity*(storage-maxStorages[i]) + deltaT * 0.6 * 0.25 * Math.PI * Math.pow(orficeDiameter[i], 2) * Math.sqrt(2 * 9.81 * h)+deltaT * 3.1 * weirLengths[i] * Math.pow(h - ( maxDepths[i] - freeBoard), 1.5);
//            yield = upstreamYield + deltaT * 0.6 * 0.25 * Math.PI * Math.pow(orficeDiameter[i], 2) * Math.sqrt(2 * 9.81 * h)+deltaT * 3.1 * weirLengths[i] * Math.pow(h - ( maxDepths[i] - freeBoard), 1.5);
        }
        
//        yield = 0;
//        yield = NaturalYield;

        
        
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
