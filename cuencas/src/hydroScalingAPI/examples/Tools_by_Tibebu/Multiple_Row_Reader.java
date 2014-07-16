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
public class Multiple_Row_Reader {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException, IOException {
        // TODO code application logic here
        
        
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/NED_00159011_1570_127-UniformEvent_INT_112.776_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/NED_00159011_1570_127-UniformEvent_INT_140.208_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/NED_00159011_1570_127-UniformEvent_INT_161.544_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/NED_00159011_1570_127-UniformEvent_INT_198.12_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/NED_00159011_1570_127-UniformEvent_INT_228.59999_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/NED_00159011_1570_127-UniformEvent_INT_262.128_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        //100 year with runoff coefficient
//        String title = "T = 2 Year";     
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Runoff_Coefficient/2yr/NED_00159011_1570_127-UniformEvent_INT_78.9432_DUR_5.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Runoff_Coefficient/2yr/NED_00159011_1570_127-UniformEvent_INT_68.27519_DUR_10.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Runoff_Coefficient/2yr/NED_00159011_1570_127-UniformEvent_INT_59.029594_DUR_15.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Runoff_Coefficient/2yr/NED_00159011_1570_127-UniformEvent_INT_40.182796_DUR_30.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Runoff_Coefficient/2yr/NED_00159011_1570_127-UniformEvent_INT_25.603199_DUR_60.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Runoff_Coefficient/2yr/NED_00159011_1570_127-UniformEvent_INT_15.824199_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Runoff_Coefficient/2yr/NED_00159011_1570_127-UniformEvent_INT_11.556999_DUR_180.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Runoff_Coefficient/2yr/NED_00159011_1570_127-UniformEvent_INT_6.7563996_DUR_360.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Runoff_Coefficient/2yr/NED_00159011_1570_127-UniformEvent_INT_3.9115996_DUR_720.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        //100 year with phi index
//        String title = "T = 100 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_262.128_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_228.59999_DUR_10.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_196.088_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_134.112_DUR_30.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_85.09_DUR_60.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_52.578_DUR_120.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_38.607998_DUR_180.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv"
//                                           };
        //100 year with phi index = 15mm/hr
//        String title = "T = 100 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_262.128_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_228.59999_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_196.088_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_134.112_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_85.09_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_52.578_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_38.607998_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"
//                                           };
        //100 year with phi index = 15mm/hr - Old mans Creek
//        String title = "T = 100 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/NED_18879939_2308_199-UniformEvent_INT_262.128_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/NED_18879939_2308_199-UniformEvent_INT_228.59999_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/NED_18879939_2308_199-UniformEvent_INT_196.088_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/NED_18879939_2308_199-UniformEvent_INT_134.112_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/NED_18879939_2308_199-UniformEvent_INT_85.09_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/NED_18879939_2308_199-UniformEvent_INT_52.578_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/100yr/NED_18879939_2308_199-UniformEvent_INT_38.607998_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"
//                                           };
        
        //100 year with phi index = 15mm/hr - Boone River
//        String title = "T = 100 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/100yr/NED_62925931_906_88-UniformEvent_INT_259.08_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/100yr/NED_62925931_906_88-UniformEvent_INT_225.552_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/100yr/NED_62925931_906_88-UniformEvent_INT_194.056_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/100yr/NED_62925931_906_88-UniformEvent_INT_133.096_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/100yr/NED_62925931_906_88-UniformEvent_INT_84.327995_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/100yr/NED_62925931_906_88-UniformEvent_INT_52.07_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/100yr/NED_62925931_906_88-UniformEvent_INT_38.354_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/100yr/NED_62925931_906_88-UniformEvent_INT_12.9539995_DUR_720.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"
//                                          };
        
//        100 year with phi index = 0.25mm/hr
//        String title = "T = 100 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_262.128_DUR_5.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_228.59999_DUR_10.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_196.088_DUR_15.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_134.112_DUR_30.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_85.09_DUR_60.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_52.578_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_38.607998_DUR_180.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_22.605999_DUR_360.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_13.207999_DUR_720.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_9.398_DUR_1080.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_7.6200004_DUR_1440.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_4.0639997_DUR_2880.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_3.0479999_DUR_4320.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_2.0319998_DUR_7200.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_1.0159999_DUR_14400.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
//        100 year with phi index = 0.mm/hr
//        String title = "T = 100 Year";
//       String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_1901.9519_DUR_5.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_950.97595_DUR_10.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_633.98395_DUR_15.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_316.99197_DUR_30.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_158.49599_DUR_60.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_79.24799_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_52.831997_DUR_180.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_26.415998_DUR_360.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_13.207999_DUR_720.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_8.805333_DUR_1080.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_6.6039996_DUR_1440.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_3.3019998_DUR_2880.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_2.2013333_DUR_4320.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_1.3208_DUR_7200.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_0.6604_DUR_14400.0-IR_0.0-Routing_CV_params_0.3_-0.1_2.0.csv"};
        
        
        
        
        
        
        
        //50 year with phi index
//        String title = "T = 50 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_228.59999_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_199.644_DUR_10.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_171.70401_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_117.34799_DUR_30.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_74.676_DUR_60.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_45.974_DUR_120.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_33.782_DUR_180.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv"
//                                           };
        
        //50 year with phi index = 15
//        String title = "T = 50 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_228.59999_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_199.644_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_171.70401_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_117.34799_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_74.676_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_45.974_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_33.782_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"
//                                           };
        
            
           
        //50 year with phi index = 15 - Old Mans Creek
//        String title = "T = 50 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/NED_18879939_2308_199-UniformEvent_INT_228.59999_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/NED_18879939_2308_199-UniformEvent_INT_199.644_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/NED_18879939_2308_199-UniformEvent_INT_171.70401_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/NED_18879939_2308_199-UniformEvent_INT_117.34799_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/NED_18879939_2308_199-UniformEvent_INT_74.676_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/NED_18879939_2308_199-UniformEvent_INT_45.974_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/50yr/NED_18879939_2308_199-UniformEvent_INT_33.782_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"
//                                           };
        
        //50 year with phi index = 15 - Boone River
//        String title = "T = 50 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/50yr/NED_62925931_906_88-UniformEvent_INT_225.552_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/50yr/NED_62925931_906_88-UniformEvent_INT_196.596_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/50yr/NED_62925931_906_88-UniformEvent_INT_168.65599_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/50yr/NED_62925931_906_88-UniformEvent_INT_115.315994_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/50yr/NED_62925931_906_88-UniformEvent_INT_73.406_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/50yr/NED_62925931_906_88-UniformEvent_INT_45.211998_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/50yr/NED_62925931_906_88-UniformEvent_INT_33.274_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/50yr/NED_62925931_906_88-UniformEvent_INT_19.557999_DUR_360.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"
//                                           };
        
         //50 year with phi index = 0.25
//        String title = "T = 50 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_228.59999_DUR_5.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_199.644_DUR_10.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_171.70401_DUR_15.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_117.34799_DUR_30.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_74.676_DUR_60.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_45.974_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_33.782_DUR_180.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_19.811998_DUR_360.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_11.429999_DUR_720.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_8.382_DUR_1080.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_6.6039996_DUR_1440.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_3.556_DUR_2880.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_2.54_DUR_4320.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_1.778_DUR_7200.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_1.0159999_DUR_14400.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv"};
//        
        
        //25 year with phi index
//        String title = "T = 25 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_198.12_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_173.73601_DUR_10.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_148.336_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_102.108_DUR_30.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_64.77_DUR_60.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_39.878002_DUR_120.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        //25 year with phi index = 15mm/hr
//        String title = "T = 25 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_198.12_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_173.73601_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_148.336_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_102.108_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_64.77_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_39.878002_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_29.463999_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        
        //25 year with phi index = 15mm/hr - Old Mans Creek
//        String title = "T = 25 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/NED_18879939_2308_199-UniformEvent_INT_198.12_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/NED_18879939_2308_199-UniformEvent_INT_173.73601_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/NED_18879939_2308_199-UniformEvent_INT_148.336_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/NED_18879939_2308_199-UniformEvent_INT_102.108_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/NED_18879939_2308_199-UniformEvent_INT_64.77_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/NED_18879939_2308_199-UniformEvent_INT_39.878002_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/25yr/NED_18879939_2308_199-UniformEvent_INT_29.463999_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        //25 year with phi index = 15mm/hr - Boone River
//        String title = "T = 25 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/25yr/NED_62925931_906_88-UniformEvent_INT_195.07199_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/25yr/NED_62925931_906_88-UniformEvent_INT_170.68799_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/25yr/NED_62925931_906_88-UniformEvent_INT_146.304_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/25yr/NED_62925931_906_88-UniformEvent_INT_100.076_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/25yr/NED_62925931_906_88-UniformEvent_INT_63.753998_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/25yr/NED_62925931_906_88-UniformEvent_INT_39.37_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/25yr/NED_62925931_906_88-UniformEvent_INT_28.956_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/25yr/NED_62925931_906_88-UniformEvent_INT_17.018_DUR_360.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
               
        
        //25 year with phi index = 0.25mm/hr
//        String title = "T = 25 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_198.12_DUR_5.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_173.73601_DUR_10.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_148.336_DUR_15.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_102.108_DUR_30.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_64.77_DUR_60.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_39.878002_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_29.463999_DUR_180.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_17.272_DUR_360.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_9.905999_DUR_720.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_7.112_DUR_1080.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_5.842_DUR_1440.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_3.3019998_DUR_2880.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_2.286_DUR_4320.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_1.5239999_DUR_7200.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_0.76199996_DUR_14400.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        
        
        //10 year with phi index
//        String title = "T = 10 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_161.544_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_141.732_DUR_10.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_121.920006_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_83.312_DUR_30.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_53.086_DUR_60.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_32.766_DUR_120.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        //10 year with phi index = 15mm/hr
//        String title = "T = 10 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_161.544_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_141.732_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_121.920006_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_83.312_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_53.086_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_32.766_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",                                            
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_24.13_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        //10 year with phi index = 15mm/hr - Old Mans Creek
//        String title = "T = 10 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/NED_18879939_2308_199-UniformEvent_INT_161.544_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/NED_18879939_2308_199-UniformEvent_INT_141.732_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/NED_18879939_2308_199-UniformEvent_INT_121.920006_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/NED_18879939_2308_199-UniformEvent_INT_83.312_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/NED_18879939_2308_199-UniformEvent_INT_53.086_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/NED_18879939_2308_199-UniformEvent_INT_32.766_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",                                            
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/10yr/NED_18879939_2308_199-UniformEvent_INT_24.13_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
          //10 year with phi index = 15mm/hr - Boone River
//        String title = "T = 10 Year";
//        String [] inFiles = new String [] { 
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/10yr/NED_62925931_906_88-UniformEvent_INT_161.544_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/10yr/NED_62925931_906_88-UniformEvent_INT_140.208_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/10yr/NED_62925931_906_88-UniformEvent_INT_119.88799_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/10yr/NED_62925931_906_88-UniformEvent_INT_82.296_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/10yr/NED_62925931_906_88-UniformEvent_INT_52.323997_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/10yr/NED_62925931_906_88-UniformEvent_INT_32.258_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/10yr/NED_62925931_906_88-UniformEvent_INT_23.622_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};

        
        
        //10 year with phi index = 0.25mm/hr
//        String title = "T = 10 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_161.544_DUR_5.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_141.732_DUR_10.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_121.920006_DUR_15.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_83.312_DUR_30.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_53.086_DUR_60.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_32.766_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",                                            
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_24.13_DUR_180.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_14.224_DUR_360.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_8.127999_DUR_720.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_5.842_DUR_1080.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_4.8259997_DUR_1440.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_2.794_DUR_2880.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_1.778_DUR_4320.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",                                            
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_1.27_DUR_7200.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",                                            
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_0.76199996_DUR_14400.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
//        5 year with phi index
//        String title = "T = 5 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_140.208_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_123.444_DUR_10.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_105.663994_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_72.135994_DUR_30.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_45.719997_DUR_60.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        //5 year with phi index = 15mm/hr
//        String title = "T = 5 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_140.208_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_123.444_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_105.663994_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_72.135994_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_45.719997_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_28.448_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_20.828_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
       //5 year with phi index = 15mm/hr - Old Mans Creek
//        String title = "T = 5 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/NED_18879939_2308_199-UniformEvent_INT_140.208_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/NED_18879939_2308_199-UniformEvent_INT_123.444_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/NED_18879939_2308_199-UniformEvent_INT_105.663994_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/NED_18879939_2308_199-UniformEvent_INT_72.135994_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/NED_18879939_2308_199-UniformEvent_INT_45.719997_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/NED_18879939_2308_199-UniformEvent_INT_28.448_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/5yr/NED_18879939_2308_199-UniformEvent_INT_20.828_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
          
         //        //5 year with phi index = 15mm/hr - Boone River
//        String title = "T = 5 Year";
//        String [] inFiles = new String [] { 
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/5yr/NED_62925931_906_88-UniformEvent_INT_137.16_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/5yr/NED_62925931_906_88-UniformEvent_INT_118.871994_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/5yr/NED_62925931_906_88-UniformEvent_INT_101.6_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/5yr/NED_62925931_906_88-UniformEvent_INT_70.104_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/5yr/NED_62925931_906_88-UniformEvent_INT_44.45_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/5yr/NED_62925931_906_88-UniformEvent_INT_27.432001_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/5yr/NED_62925931_906_88-UniformEvent_INT_20.066_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
//         
        
        
        
        //5 year with phi index = 0.25mm/hr
//        String title = "T = 5 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_140.208_DUR_5.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_123.444_DUR_10.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_105.663994_DUR_15.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_72.135994_DUR_30.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_45.719997_DUR_60.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_28.448_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_20.828_DUR_180.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_12.191999_DUR_360.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_7.112_DUR_720.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_5.08_DUR_1080.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_4.0639997_DUR_1440.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_2.286_DUR_2880.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_1.5239999_DUR_4320.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_1.0159999_DUR_7200.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_0.76199996_DUR_14400.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv"};
//        
//        //2 year with phi index
//        String title = "T = 2 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_112.776_DUR_5.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_97.535995_DUR_10.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_84.327995_DUR_15.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_57.404_DUR_30.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_36.576_DUR_60.0-IR_30.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        //2 year with phi index = 15mm/hr
//        String title = "T = 2 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_112.776_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_97.535995_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_84.327995_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_57.404_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_36.576_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_22.605999_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_16.509998_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
         //2 year with phi index = 15mm/hr - Old Mans Creek
//        String title = "T = 2 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/NED_18879939_2308_199-UniformEvent_INT_112.776_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/NED_18879939_2308_199-UniformEvent_INT_97.535995_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/NED_18879939_2308_199-UniformEvent_INT_84.327995_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/NED_18879939_2308_199-UniformEvent_INT_57.404_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/NED_18879939_2308_199-UniformEvent_INT_36.576_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/NED_18879939_2308_199-UniformEvent_INT_22.605999_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/2yr/NED_18879939_2308_199-UniformEvent_INT_16.509998_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
               
//        //2 year with phi index = 15mm/hr - Boone River
//        String title = "T = 2 Year";
//        String [] inFiles = new String [] { 
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/2yr/NED_62925931_906_88-UniformEvent_INT_109.728004_DUR_5.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/2yr/NED_62925931_906_88-UniformEvent_INT_96.012_DUR_10.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/2yr/NED_62925931_906_88-UniformEvent_INT_81.28_DUR_15.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/2yr/NED_62925931_906_88-UniformEvent_INT_55.88_DUR_30.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/2yr/NED_62925931_906_88-UniformEvent_INT_35.559998_DUR_60.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/2yr/NED_62925931_906_88-UniformEvent_INT_22.098_DUR_120.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/2yr/NED_62925931_906_88-UniformEvent_INT_16.255999_DUR_180.0-IR_15.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
////                            
        
        //2 year with phi index = 0.25mm/hr
//        String title = "T = 2 Year";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_112.776_DUR_5.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_97.535995_DUR_10.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_84.327995_DUR_15.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_57.404_DUR_30.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_36.576_DUR_60.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_22.605999_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_16.509998_DUR_180.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_9.651999_DUR_360.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_5.588_DUR_720.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_4.0639997_DUR_1080.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_3.3019998_DUR_1440.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_1.778_DUR_2880.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_1.27_DUR_4320.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_0.76199996_DUR_7200.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_0.50799996_DUR_14400.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        //test 2yr phi 0.25
//                String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_112.776_DUR_5.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_97.535995_DUR_10.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_84.327995_DUR_15.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_57.404_DUR_30.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_36.576_DUR_60.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_22.605999_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_16.509998_DUR_180.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_9.651999_DUR_360.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_5.588_DUR_720.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_4.0639997_DUR_1080.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_3.3019998_DUR_1440.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv"};
 
         //    6hr D 2year to    100 year with phi index = 0.25mm/hr
//        String title = "Rainfall Duration = 2hr";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/ConstDur/NED_00159011_1570_127-UniformEvent_INT_1.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/ConstDur/NED_00159011_1570_127-UniformEvent_INT_5.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/ConstDur/NED_00159011_1570_127-UniformEvent_INT_10.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/ConstDur/NED_00159011_1570_127-UniformEvent_INT_15.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/ConstDur/NED_00159011_1570_127-UniformEvent_INT_20.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/2yr/NED_00159011_1570_127-UniformEvent_INT_22.605999_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/5yr/NED_00159011_1570_127-UniformEvent_INT_28.448_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/10yr/NED_00159011_1570_127-UniformEvent_INT_32.766_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/25yr/NED_00159011_1570_127-UniformEvent_INT_39.878002_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/50yr/NED_00159011_1570_127-UniformEvent_INT_45.974_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/NED_00159011_1570_127-UniformEvent_INT_52.578_DUR_120.0-IR_0.25-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/ConstDur/NED_00159011_1570_127-UniformEvent_INT_60.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/ConstDur/NED_00159011_1570_127-UniformEvent_INT_70.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/ConstDur/NED_00159011_1570_127-UniformEvent_INT_80.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/100yr/ConstDur/NED_00159011_1570_127-UniformEvent_INT_100.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
        
      //        Hillslope velocity experiment
//        String title = "Rainfall Intensity = 247mm/hr, Rainfall Duration = 5min";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_3.0E-4-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_6.0E-4-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.001-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.003-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.006-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.01-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.013-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.016-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.02-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.025-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.04-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.1-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_247.0_DUR_5.0-IR_0.0-Vh_0.2-Routing_CV_params_0.3_-0.1_0.6.csv"};
      //Hillslope velocity                                       
//       String title = "Rainfall Intensity = 119mm/hr, Rainfall Duration = 30min";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_3.0E-4-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_6.0E-4-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.001-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.003-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.006-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.01-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.013-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.016-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.02-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.025-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.04-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.1-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/ClearCreek_Database/Results/IDF_Study/Hillslope_Velocity/NED_00159011_1570_127-UniformEvent_INT_38.0_DUR_120.0-IR_0.0-Vh_0.2-Routing_CV_params_0.3_-0.1_0.6.csv"};
 
        //Boone river
//String [] inFiles = new String [] { "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_1901.9519_DUR_5.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_950.97595_DUR_10.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_633.98395_DUR_15.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_316.99197_DUR_30.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_158.49599_DUR_60.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_79.24799_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_52.831997_DUR_180.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_26.415998_DUR_360.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_13.207999_DUR_720.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_8.805333_DUR_1080.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_6.6039996_DUR_1440.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_3.3019998_DUR_2880.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_2.2013333_DUR_4320.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_1.3208_DUR_7200.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarDur/NED_62925931_906_88-UniformEvent_INT_0.6604_DUR_14400.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
          //        Constant Duration, Variable Volume, Constant Vo and VhString title = "Constant Rainfall Duration = 2hr";
//        String [] inFiles = new String [] { "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_1.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_5.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_10.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_15.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_30.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_45.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_60.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_90.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_120.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstDur_VarVol/NED_18879939_2308_199-UniformEvent_INT_150.0_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
//        Old Mans Creek
//        String [] inFiles = new String [] { "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_1901.9519_DUR_5.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_950.97595_DUR_10.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_633.98395_DUR_15.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_316.99197_DUR_30.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_158.49599_DUR_60.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_79.24799_DUR_120.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_52.831997_DUR_180.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_26.415998_DUR_360.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_13.207999_DUR_720.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_8.805333_DUR_1080.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_6.6039996_DUR_1440.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_3.3019998_DUR_2880.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "C:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarDur/NED_18879939_2308_199-UniformEvent_INT_2.2013333_DUR_4320.0-IR_0.0-Routing_CV_params_0.3_-0.1_0.6.csv"};
       
        String title = "Rainfall Depth = 158.5mm      Channel Velocity = 0.6m/s     Hillslope Velocity = 0.0001m/s";
        String [] inFiles = new String [] { "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_1901.9519_DUR_5.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_950.97595_DUR_10.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_633.98395_DUR_15.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_316.99197_DUR_30.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_158.49599_DUR_60.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_79.24799_DUR_120.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_52.831997_DUR_180.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_26.415998_DUR_360.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_13.207999_DUR_720.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_8.805333_DUR_1080.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_6.6039996_DUR_1440.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_3.3019998_DUR_2880.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_2.2013333_DUR_4320.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_1.3208_DUR_7200.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv",
                                            "E:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/NED_62925931_906_88-UniformEvent_INT_0.6604_DUR_14400.0-IR_0.0-Vh_0.05-Routing_CV_params_0.3_-0.1_0.6.csv"};
        
//        String [] inFiles = new String [] { "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_1901.9519_DUR_5.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_950.97595_DUR_10.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_633.98395_DUR_15.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_316.99197_DUR_30.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_158.49599_DUR_60.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_79.24799_DUR_120.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_52.831997_DUR_180.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_26.415998_DUR_360.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_13.207999_DUR_720.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_8.805333_DUR_1080.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_6.6039996_DUR_1440.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_3.3019998_DUR_2880.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv",
//                                            "E:/CuencasDataBases/OldMansCreek_Database/Results/IDF_Study/ConstVol_VarVh/NED_18879939_2308_199-UniformEvent_INT_2.2013333_DUR_4320.0-IR_0.0-Vh_0.03-Routing_CV_params_0.3_-0.1_0.6.csv"};
//       
//       
//       
//       
//       
        
        String outFile = "C:/CuencasDataBases/BooneRiver_Database/Results/IDF_Study/ConstVol_VarVh/BooneRiver-Const_Vol_Var_Dur_Vr0P05.csv";
       PrintWriter out = new PrintWriter(outFile);
        for (int j=0; j<inFiles.length; j++)
        {                
                BufferedReader br1 = new BufferedReader( new FileReader(inFiles[j]) ); 
                String line1 = br1.readLine();
                String label = "";
                String dummy = "";
                while( !"Maximum".equals(label))
                {
                    dummy = line1;
                    StringTokenizer st = new StringTokenizer( line1, "  ,    ", false ); 
                    if(!line1.isEmpty()){label = ((st.nextToken() ));}        
                    line1 = br1.readLine();                   
                }                
                br1.close();                
                out.println(dummy);
        }
        out.close();

    }
}
