/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.swmmCoupling.io;

import hydroScalingAPI.modules.swmmCoupling.objects.OutletInflowConnection;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 *
 * @author work
 */
public class OutletInflowConnectionCsvImporter {
    
    public static ArrayList<OutletInflowConnection> readCsvFile(File csvFile_arg) throws FileNotFoundException, IOException {
        ArrayList<OutletInflowConnection> return_list;
        OutletInflowConnection cur_connection;
        BufferedReader buff_reader;
        String cur_line;
        
        // basic check
        if ((csvFile_arg == null)||(!csvFile_arg.exists())||(!csvFile_arg.canRead())){
            return(null);
        }
        
        buff_reader = new BufferedReader(new FileReader(csvFile_arg));
        return_list = new ArrayList<OutletInflowConnection>();
        while ((cur_line = buff_reader.readLine()) != null) {
            cur_connection = OutletInflowConnectionCsvImporter.readCsvFileLine(cur_line);
            if (cur_connection != null){
                return_list.add(cur_connection);
            }
        }
        
        return(return_list);
    }
    
    private static OutletInflowConnection readCsvFileLine(String csvFileLine_str){
        OutletInflowConnection return_object;
        String node_id, timeseries_id;
        String[] splited_line;
        int x, y;
        
        splited_line = csvFileLine_str.split(",");
        if (splited_line.length < 4){
            return null;
        }
        
        x = Integer.parseInt(splited_line[0].trim());
        y = Integer.parseInt(splited_line[1].trim());;
        node_id = splited_line[2];
        timeseries_id = splited_line[3];
        
        return_object = new OutletInflowConnection(x, y, node_id, timeseries_id);
        return(return_object);
    }
    
}
