/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.Tools_by_Tibebu;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author Tibebu
 */
public class XYData_Reader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
       

        String storageFile =    "E:\\CUENCAS\\ClearCreek_Database\\Results\\NED_00159011_778_368-UniformEvent_INT_100.0_DUR_15.0-IR_30.0-Routing_GK_params_0.315_-0.092_0.6_storages.csv";
        String dischargeFile =  "E:\\CUENCAS\\ClearCreek_Database\\Results\\NED_00159011_778_368-UniformEvent_INT_100.0_DUR_15.0-IR_30.0-Routing_GK_params_0.315_-0.092_0.6_storages.csv";
        String simulInFile;
        int columnNumberToReadStorage = 9;
        int columnNumberToReadDischarge = 7;
        int columnNumber;
        ArrayList<Double> storageTime = new ArrayList();
        ArrayList<Double> storageValue = new ArrayList();
        ArrayList<Double> dischargeTime = new ArrayList();
        ArrayList<Double> dischargeValue = new ArrayList();
       
	for (int n=0; n<2; n++)
        {
            if(n==0){simulInFile = storageFile;} else{simulInFile = dischargeFile;}
            BufferedReader br2 = new BufferedReader( new FileReader(simulInFile) );
            for(int i=0; i<11; i++) {  String key1 = br2.readLine();}

            String line2 = br2.readLine(); 

            while( line2 != null && line2.length()!=0)
            {
                StringTokenizer st = new StringTokenizer( line2, "  ,    ", false );  
                
                double time = (Double.parseDouble(st.nextToken() ));
                if(n==0){storageTime.add(time); columnNumber =columnNumberToReadStorage;}
                else{dischargeTime.add(time);   columnNumber =columnNumberToReadDischarge;}
                
                for(int j = 0; j<columnNumber-2;j++){double RR = (Double.parseDouble(st.nextToken() ));}
                double value = (Double.parseDouble(st.nextToken() ));
                if(n==0){storageValue.add(value);}else{dischargeValue.add(value);}

//                System.out.println(time+"   "+value);
                line2 = br2.readLine(); 
            }
            br2.close();
        }  
        
        
        final XYplotter plot = new XYplotter("Storage Discharge Calculation",storageTime,storageValue,dischargeTime,dischargeValue);
        plot.pack();
        RefineryUtilities.centerFrameOnScreen(plot);
        plot.setVisible(true);

   
    }
}
