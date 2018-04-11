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
 * writeUSGSdataWeb.java
 *
 * Created on July 7, 2003, 3:19 PM
 */

package hydroScalingAPI.io;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

/**
 * This class was developed to import daily streamflow time series from the USGS
 * website.  A Gauge-type site is created in the database. Currnently, only
 * streamflow time series can be imported,  Future implementations will include
 * flow depth and other variables
 * @author Peter Furey
 */
public class ImportUSGS_StremMeasuData {
    
    String output_dir;
    String state;
    String gaugeid;
    char record_type;
    String filesuffix;
    java.util.StringTokenizer str1;
    
    /**
     * Creates an instance of the ImportUSGS_waterdataWeb
     * @param oDir The directory where the ouput file will be stored
     */
    public ImportUSGS_StremMeasuData(String oDir) {
        output_dir=oDir;
    }
    
    /**
     * This method imports streamflow data and creates a Gauge-type site file in the
     * database
     * @param state The two letter code for the state (e.g. CO - Colorado, NM - New Mexico)
     * @param gaugeid The USGS gauge code (e.g. 09430500 for the Gila River at Gila gauge)
     * @param record_type "H": for historical and "R": for recent.
     */

    /**
     * This method imports streamflow data and creates a Gauge-type site file in the
     * database
     * @param state The two letter code for the state (e.g. CO - Colorado, NM - New Mexico)
     * @param gaugeid The USGS gauge code (e.g. 09430500 for the Gila River at Gila gauge)
     * @param record_type "H": for historical and "R": for recent.
     */



    public void StreamFlow(String gaugeid) throws FileNotFoundException, IOException {
        
          
            //new java.io.File("/hidrosigDataBases/Whitewater_database/Sites/Locations/"+thisLocation.State+"/"+thisLocation.Type+"/").mkdirs();
            java.io.File theFile = new java.io.File(output_dir+gaugeid+".txt.gz");
            java.io.FileOutputStream outputLocal=new java.io.FileOutputStream(theFile);
            java.util.zip.GZIPOutputStream outputComprim=new java.util.zip.GZIPOutputStream(outputLocal);
            java.io.BufferedWriter dataout= new java.io.BufferedWriter(new java.io.OutputStreamWriter(outputComprim));

           //FileWriter dataout = new FileWriter(output_dir+gaugeid+filesuffix);
            
            // Copy site info from USGS file to new data file

            java.net.URL remotePath1=new java.net.URL("http://waterdata.usgs.gov/nwis/inventory?search_site_no="+gaugeid+"&search_site_no_match_type=exact&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=alt_va&column_name=drain_area_va&list_of_search_criteria=search_site_no");


            
            //java.net.URL remotePath1=new java.net.URL("http://waterdata.usgs.gov/nwis/inventory/?site_no="+gaugeid+"&amp");
            System.out.println("http://waterdata.usgs.gov/nwis/inventory?search_site_no="+gaugeid+"&search_site_no_match_type=exact&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=alt_va&column_name=drain_area_va&list_of_search_criteria=search_site_no");

            java.io.InputStream inputRemote1=remotePath1.openStream();
            java.io.BufferedReader buff1 = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemote1));
                     String lineSite=buff1.readLine();
                     String lineSite2=buff1.readLine();
            boolean eof1 = false;
            dataout.write("#Agency "+ "," + "site_no"+ "," + "Site_name"+ "," + "lat"+ "," + "long"+ "," + "alt_va"+ "," + "alt_acu"+ "," + "drainArea" + '\n');
            double drainarea=0.0;
            int lat=0;
            int lon=0;
            while(!eof1){

                if (lineSite==null) {
                    eof1=true;
                } else {
                    if (lineSite.charAt(0)=='U'){    // U for USGS
                    lineSite2=lineSite;

                    System.out.println(lineSite);

                    String [] linarray=lineSite.split("\t");



                    try{
                    FileWriter fstream = new FileWriter("D:/CUENCAS/Illinois_river_basin/measur/AllSites.txt",true);
                    BufferedWriter out = new BufferedWriter(fstream);

                        for (int i=0;i<linarray.length;i++) {
                          out.write(linarray[i]+",");
                          dataout.write(linarray[i]+",");
                        }
                          out.write("\n");
                          dataout.write("\n");
                    drainarea=Double.parseDouble(linarray[10]);
                    lat=Integer.parseInt(linarray[3]);
                    lon=Integer.parseInt(linarray[4]);
                    //Close the output stream
                    out.close();

                     }catch (Exception e){//Catch exception if any
                     System.err.println("Error: " + e.getMessage());
                     }
                    //dataout.write(lineSite+'\n');
                    //dataout.write(linarray[0]+ "," + linarray[1]+ "," + linarray[2]+ "," + linarray[3]+ "," + linarray[4]+ "," + linarray[5]+ "," + linarray[6]+ " "
                    //        + linarray[7]+ "," + linarray[8]+ "," + linarray[9]+ "," + linarray[10]+'\n');
                   }
                }
                lineSite=buff1.readLine();
            }

            buff1.close();
            
            // Copy data from USGS file to new data file ...
            // Raw USGS discharge values in ft^3/s
           System.out.println("drainarea" + drainarea + "lat" +lat+"lon"+lon);
           java.net.URL remotePath2=new java.net.URL("http://waterdata.usgs.gov/nwis/measurements?site_no="+gaugeid+"&agency_cd=USGS&format=rdb_expanded");
           System.out.println("http://waterdata.usgs.gov/nwis/measurements?site_no="+gaugeid+"&agency_cd=USGS&format=rdb_expanded");
                                       dataout.write("#Agency "+ "," + "site_no"+ "," + "measurement_nu" + "," + "measurement_dt" + "," + "party_nm" + "," + "gage_height_va"+ "," + "discharge_va"+ "," + "current_rating_nu"+ "," + "shift_adj_va"+ "," + "diff_from_rating_pc"+ "," + "measured_rating_diff"+ "," + "gage_va_change"+ "," + "gage_va_time"+ "," + "control_type_cd"+ "," + "discharge_cd"+ "," + "chan_nu"+ "," + "chan_name"+ "," + "meas_type"+ "," + "streamflow_method"+ "," + "velocity_method"+ "," + "chan_discharge"+ "," + "chan_width"+ "," + "chan_are"+ "," + "chan_velocity"+ "," + "chan_type"+ "," + "long_vel_desc"+ "," + "horz_vel_desc"+ "," + "chan_loc_cd"+ "," + "chan_loc_dist" +'\n');

           System.out.println(remotePath2);
           java.io.InputStream inputRemote2=remotePath2.openStream();
           java.io.BufferedReader buff2 = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemote2));
           boolean eof2 = false;
            while(!eof2){
                String line=buff2.readLine();
                if (line==null) {
                    eof2=true;
                } else {
                    if (line.charAt(0)=='U'){    // U for USGS
                    System.out.println(line);
                    String [] linarray=line.split("\t");   // System.out.println(line);
                    //dataout.write(line+'\n');
                    try{

                    FileWriter fstream = new FileWriter("D:/CUENCAS/Illinois_river_basin/measur/AllMeas3.txt",true);
                    BufferedWriter out = new BufferedWriter(fstream);
                    //out.write(line+ lineSite2+ "\n");


                    out.write( 
                        linarray[1]+ // Site code
                        "," + lat+ //lat
                        "," + lon+ //lon
                        "," + drainarea+ //drainage area
                        "," + linarray[2]+ //meas_no
                        "," + linarray[3]+ // meas_year_fix
                        "," + linarray[5]+ // diachrage_VA
                        "," + linarray[6]+ // height
                        "," + linarray[10]+ // measu _quality_fix
                        "," + linarray[13]+ // control_fix
                        "," + linarray[17]+ // meas_type_fix
                        "," + linarray[18]+ // streamflow method_fix
                        "," + linarray[19]+ // velocity me
                        "," + linarray[20]+ // Discharge
                        "," + linarray[21]+ // width
                        "," + linarray[22]+ // velocity
                        "," + linarray[23]+ // area
                        "\n");
           //Close the output stream
                    out.close();

                     }catch (Exception e){//Catch exception if any
                     System.err.println("Error: " + e.getMessage());
                     }


                    dataout.write(linarray[0]+ "," + linarray[1]+ "," + linarray[2]+ "," + linarray[3]+ "," + linarray[4]+ "," + linarray[5]+ "," + linarray[6]+ "," + "," + linarray[7]+ "," + "," + linarray[8]+ "," + "," + linarray[9]+
                                  linarray[10]+ "," + linarray[11]+ "," + linarray[12]+ "," + linarray[13]+ "," + linarray[14]+ "," + linarray[15]+ "," + linarray[16]+ "," + "," + linarray[17]+ "," + "," + linarray[18]+ "," + "," + linarray[19]+
                                  linarray[20]+ "," + linarray[21]+ "," + linarray[22]+ "," + linarray[3]+ "," + linarray[4]+ "," + linarray[25]+ "," + linarray[26]+ "," + "," + linarray[27]+ "," + " "
                                  +'\n');

                }
            }
          }

            buff2.close();
           dataout.close();
            
        
    }
    
    /**
     * Tests for the class
     * @param arguments Comand line arguments
     */
    public static void main(String[ ] arguments) throws FileNotFoundException, IOException { //throws java.io.IOException IOE{
        
        ImportUSGS_StremMeasuData data = new ImportUSGS_StremMeasuData("D:/CUENCAS/Illinois_river_basin/measur/");

data.StreamFlow("07194760");
data.StreamFlow("07194790");
data.StreamFlow("07194800");
data.StreamFlow("07194809");
data.StreamFlow("071948135");
data.StreamFlow("071948140");
data.StreamFlow("071948145");
data.StreamFlow("071948150");
data.StreamFlow("071948155");
data.StreamFlow("07194816");
data.StreamFlow("07194820");
data.StreamFlow("07194875");
data.StreamFlow("07194880");
data.StreamFlow("07194905");
data.StreamFlow("07194906");
data.StreamFlow("07194908");
data.StreamFlow("07194933");
data.StreamFlow("07194950");
data.StreamFlow("07195000");
data.StreamFlow("07195200");
data.StreamFlow("07195350");
data.StreamFlow("07195400");
data.StreamFlow("07195430");
data.StreamFlow("07195450");
data.StreamFlow("07195453");
data.StreamFlow("07195500");
data.StreamFlow("07195800");
data.StreamFlow("07195855");
data.StreamFlow("07195860");
data.StreamFlow("07195865");
data.StreamFlow("07196000");
data.StreamFlow("07196320");
data.StreamFlow("07196500");
data.StreamFlow("07196513");
//data.StreamFlow("07196900");
data.StreamFlow("07196950");
data.StreamFlow("07196973");
data.StreamFlow("07197000");
data.StreamFlow("07197360");
data.StreamFlow("07198000");
    }
    
}
