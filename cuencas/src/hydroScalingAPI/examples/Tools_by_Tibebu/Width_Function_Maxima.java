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
import org.jfree.ui.RefineryUtilities;

/**
 *
 * @author Tibebu
 */
public class Width_Function_Maxima {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
   
//        String inFile = "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_SN.wfs.csv";
        String inFile = "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/2yr/NED_62925931-SN.wfs.csv";
        
        
               
//       String outFile = "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/2yr_D_all_peak_flow_data.csv";
//       PrintWriter out = new PrintWriter(outFile);
                       
                BufferedReader br1 = new BufferedReader( new FileReader(inFile) ); 
                String line1 = br1.readLine();                 
                while( line1 != null && line1.length()!=0)
                {                   
                    StringTokenizer st = new StringTokenizer( line1, ",", false ); 
                    String linkID = st.nextToken();
//                    System.out.println(st.countTokens());
                    int tokenCounter = st.countTokens();
                    float widthFunction =0.0f;
                    for (int i=0; i<tokenCounter; i++)
                    {
                      float dummy = Float.parseFloat(st.nextToken());
                      if(widthFunction<dummy) {widthFunction=dummy;}
                    }
                    System.out.println(linkID+" ,"+widthFunction);      
                    line1 = br1.readLine();                   
                }                
                br1.close();                
                
       
//        out.close();

    }
}
