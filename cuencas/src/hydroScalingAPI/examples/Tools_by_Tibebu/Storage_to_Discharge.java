/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.Tools_by_Tibebu;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 *
 * @author Tibebu
 */
public class Storage_to_Discharge {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
       

        String storageFile =    "C:\\CuencasDataBases\\ClearCreek_Database\\Results\\NED_00159011_1570_127-UniformEvent_INT_100.0_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6_storages_Order_3_and_4_H5_D_Closed.csv";
        String dischargeFile =  "C:\\CuencasDataBases\\ClearCreek_Database\\Results\\NED_00159011_1570_127-UniformEvent_INT_100.0_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6_discharges_Order_3_and_4_H5_D_Closed.csv";
        
//        String storageFile =    "C:\\CuencasDataBases\\ClearCreek_Database\\Results\\NED_00159011_778_368-UniformEvent_INT_10000.0_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6_storages.csv";
//        String dischargeFile =    "C:\\CuencasDataBases\\ClearCreek_Database\\Results\\NED_00159011_778_368-UniformEvent_INT_10000.0_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6_discharge.csv";
        
        //GK
//        String storageFile =    "C:\\CuencasDataBases\\ClearCreek_Database\\Results\\NED_00159011_778_368-UniformEvent_INT_100.0_DUR_15.0-IR_30.0-Routing_GK_params_0.3_-0.1_0.6_storages.csv";
//        String dischargeFile =    "C:\\CuencasDataBases\\ClearCreek_Database\\Results\\NED_00159011_778_368-UniformEvent_INT_100.0_DUR_15.0-IR_30.0-Routing_GK_params_0.3_-0.1_0.6_discharge.csv";
        ArrayList<Double> linkLength = new ArrayList();
        
        double velocity = 0.6;
       
	
            BufferedReader br2 = new BufferedReader( new FileReader(storageFile) );
            PrintWriter out = new PrintWriter(dischargeFile);
            String key1 = br2.readLine();
            for(int i=0; i<11; i++)
            {
               StringTokenizer st = new StringTokenizer( key1, ",", false );  
               int colNum = st.countTokens();
               
               if(i!=5) // This bit reads and prints the link IDs
               {    
                    String [] linkLabel = new String [colNum];                              
                    for(int j = 0; j<colNum;j++)
                    {   
                        linkLabel[j]=st.nextToken();
                        out.print(linkLabel[j]+",");
                    }       
                    out.println();
               }
               
               if(i==5) // This bit reads the link lengths and store it
               {                    
                   String [] linkLabel = new String [colNum];  
                   String dummy = st.nextToken(); 
                   linkLabel[0]=dummy;
                    for(int j = 1; j<colNum;j++)
                    {   
                        double RR = (Double.parseDouble(st.nextToken() ));
                        linkLength.add(RR);
                        linkLabel[j]=Double.toString(RR);
                    }  
                    for(int j = 0; j<colNum;j++) {out.print(linkLabel[j]+",");}  
                    out.println();          
               }
               
               
               key1 = br2.readLine();
            }

            
            while( key1 != null && key1.length()!=0) //This bit reads storage, calculates and then prints discharge values
            {
                StringTokenizer st = new StringTokenizer( key1, ",", false );  
                double [] discharge = new double [st.countTokens()];
                double time = (Double.parseDouble(st.nextToken() ));
                discharge[0]=time;
                for (int i=1; i<discharge.length; i++)
                {   double storage = (Double.parseDouble(st.nextToken() ));
                    discharge[i]= velocity*storage/(1000*linkLength.get(i-1));                    
                }
                for (int i=0; i<discharge.length; i++){out.print(discharge[i]+",");}//prints the discharge values
                out.println(); 
                key1 = br2.readLine(); 
            }
            br2.close();
            out.close();
   
    }
}
