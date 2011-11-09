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
    
    
    public ReservoirsManager() {
        resLocations = new int[]{16};//{16,34};
        maxStorages=new double[] {466663,466663};
        maxDepths = new double[] {7, 7};
        weirLengths = new double[] {2, 2};
        orficeDiameter = new double[] {.6, .6};
        
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
        if(h<maxDepths[i])
        {
            yield = (deltaT*0.6*0.25*Math.PI*Math.pow(orficeDiameter[i], 2)*Math.sqrt(2*9.81*h));
        }
        else
        {
            yield = (deltaT*0.6*0.25*Math.PI*Math.pow(orficeDiameter[i], 2)*Math.sqrt(2*9.81*h)+deltaT*3.1*weirLengths[i]*Math.pow(h-maxDepths[i], 1.5));
        }
        
//        yield = 0; 
        
        
//        System.out.println("Comparing Flows: Yield ="+yield+" Natural Flow = "+NaturalYield+ "For H="+h);
        
        
        if(yield > NaturalYield) yield = NaturalYield;
        
        
        
        return yield;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
}
