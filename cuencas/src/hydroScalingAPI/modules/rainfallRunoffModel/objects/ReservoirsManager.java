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

    public ReservoirsManager() {
    }
    
    public int[] getReservoirLocations(){
        return new int[] {16,34};
    }
    
    public float[] getReservoirMaxStorages(){
        return new float[] {1000,1000};
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
}
