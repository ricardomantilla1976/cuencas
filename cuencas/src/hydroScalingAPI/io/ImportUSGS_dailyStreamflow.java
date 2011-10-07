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

/**
 * This class was developed to import daily streamflow time series from the USGS
 * website.  A Gauge-type site is created in the database. Currnently, only
 * streamflow time series can be imported,  Future implementations will include
 * flow depth and other variables
 * @author Peter Furey
 */
public class ImportUSGS_dailyStreamflow {
    
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
    public ImportUSGS_dailyStreamflow(String oDir) {
        output_dir=oDir;
    }
    
    /**
     * This method imports streamflow data and creates a Gauge-type site file in the
     * database
     * @param state The two letter code for the state (e.g. CO - Colorado, NM - New Mexico)
     * @param gaugeid The USGS gauge code (e.g. 09430500 for the Gila River at Gila gauge)
     * @param record_type "H": for historical and "R": for recent.
     */
    public void StreamFlow(String state, String gaugeid, char record_type) {
        
        try{
            
            switch (record_type) {
                case 'H' :
                    filesuffix = ".por";
                    break ;
                case 'R' :
                    filesuffix=".recent";
                    break;
            }
            
            //new java.io.File("/hidrosigDataBases/Whitewater_database/Sites/Locations/"+thisLocation.State+"/"+thisLocation.Type+"/").mkdirs();
            java.io.File theFile = new java.io.File(output_dir+gaugeid+filesuffix+".txt.gz");
            java.io.FileOutputStream outputLocal=new java.io.FileOutputStream(theFile);
            java.util.zip.GZIPOutputStream outputComprim=new java.util.zip.GZIPOutputStream(outputLocal);
            java.io.BufferedWriter dataout= new java.io.BufferedWriter(new java.io.OutputStreamWriter(outputComprim));
            
            //FileWriter dataout = new FileWriter(output_dir+gaugeid+filesuffix);
            
            // Copy site info from USGS file to new data file
            java.net.URL remotePath1=new java.net.URL("http://waterdata.usgs.gov/"+state+"/nwis/inventory?search_site_no="+gaugeid+"&search_site_no_match_type=exact&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=alt_va&column_name=drain_area_va&list_of_search_criteria=search_site_no");
            System.out.println("http://waterdata.usgs.gov/"+state+"/nwis/inventory?search_site_no="+gaugeid+"&search_site_no_match_type=exact&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=alt_va&column_name=drain_area_va&list_of_search_criteria=search_site_no");
            java.io.InputStream inputRemote1=remotePath1.openStream();
            java.io.BufferedReader buff1 = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemote1));
            
            boolean eof1 = false;
            while(!eof1){
                String line=buff1.readLine();
                if (line==null) {
                    eof1=true;
                } else {
                    if (line.charAt(0)=='U'){    // U for USGS
                        int stateindex = line.indexOf(state);
                        
                        String [] linarray=line.split("\t");
                        float area;
                        float altitude;
                        dataout.write("[code]"+'\n');
                        dataout.write(linarray[1]+'\n');
                        dataout.write('\n');
                        dataout.write("[agency]"+'\n');
                        dataout.write(linarray[0]+'\n');
                        dataout.write('\n');
                        dataout.write("[type]"+'\n');
                        dataout.write("Streamflow_"+record_type+'\n');
                        dataout.write('\n');
                        dataout.write("[site name]"+'\n');
                        dataout.write(linarray[2]+'\n');
                        dataout.write('\n');
                        dataout.write("[stream name]"+'\n');
                        dataout.write(linarray[2]+'\n');
                        dataout.write('\n');
                        dataout.write("[county]"+'\n');
                        dataout.write('\n');
                        dataout.write('\n');
                        dataout.write("[state]"+'\n');
                        dataout.write(hydroScalingAPI.tools.StateName.CodeOrNameToStandardName(state)+'\n');
                        dataout.write('\n');
                        dataout.write("[data source]"+'\n');
                        dataout.write("URL: http://nwis.waterdata.usgs.gov/nm/nwis/inventory"+'\n');
                        dataout.write('\n');
                        dataout.write("[latitude (deg:min:sec)]"+'\n');
                        String lat = linarray[3];
                        lat = lat.substring(0,2)+":"+ lat.substring(2,4)+":"+lat.substring(4,6)+" N";
                        dataout.write(lat+'\n');
                        dataout.write('\n');
                        dataout.write("[longitude (deg:min:sec)]"+'\n');
                        String lon = linarray[4];
                        lon = lon.substring(0,3)+":"+ lon.substring(3,5)+":"+lon.substring(5,7)+" W";
                        dataout.write(lon+'\n');
                        dataout.write('\n');
                        dataout.write("[altitude ASL (m)]"+'\n');    // raw USGS altitude in ft
                        //linearray = line.split();
                        //altitude = Float.parseFloat(linearray{1}); Doesn't work because spaces added to array
                        altitude = Float.parseFloat(linarray[7]);
                        altitude = altitude * 0.3048f;      // *0.3048 to m
                        dataout.write(String.valueOf(altitude)+'\n');
                        dataout.write('\n');
                        dataout.write("[drainage area (km^2)]"+'\n');   // raw USGS drainage area in mi^2
                        area = Float.parseFloat(linarray[10]);
                        area = area * 2.5899f;        // *2.5899 to km^2
                        dataout.write(String.valueOf(area)+'\n');
                        dataout.write('\n');
                        dataout.write("[data units]"+'\n');
                        dataout.write("ft^3/s"+'\n');
                        dataout.write('\n');
                        dataout.write("[data accuracy]"+'\n');
                        dataout.write('\n');
                        dataout.write('\n');
                        dataout.write("[data (yyyy.mm.dd.hh.mm.ss    value)]"+"\n");
                    }
                }
            }
            buff1.close();
            
            // Copy data from USGS file to new data file ...
            // Raw USGS discharge values in ft^3/s
            
            switch (record_type) {
                case 'H' :      // Historical data checked for quality
                    java.net.URL remotePath2=new java.net.URL("http://waterdata.usgs.gov/"+state+"/nwis/discharge?site_no="+gaugeid+"&agency_cd=USGS&begin_date=1800-01-01&end_date=2100-01-01&set_logscale_y=1&format=rdb&date_format=YYYY.MM.DD&rdb_compression=&submitted_form=brief_list");
                    System.out.println(remotePath2);
                    java.io.InputStream inputRemote2=remotePath2.openStream();
                    java.io.BufferedReader buff2 = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemote2));
                    
                    boolean eof2 = false;
                    while(!eof2){
                        String line=buff2.readLine();
                        if (line==null) {
                            eof2=true;
                            System.out.println("Created new data file ="+output_dir+gaugeid+filesuffix+".txt.gz");
                        } else {
                            if (line.charAt(0)=='U'){     // U for USGS
                                line = line.substring(14);
                                if (line.length() > 11){        // 11 is length of date + tab ... (no data)
                                    //System.out.println(line.length());
                                    try{
                                        if (line.substring(11).split("\t").length > 0){
                                            Float.parseFloat(line.substring(11).split("\t")[0]);
                                            String lineToWrite = line.substring(0,4)+"."+line.substring(5,7)+"."+line.substring(8,10)+'\t'+line.substring(11).split("\t")[0];
                                            dataout.write(lineToWrite+'\n');
                                        } else {
                                            String lineToWrite = line.substring(0,4)+"."+line.substring(5,7)+"."+line.substring(8,10)+'\t'+"-9999";
                                            dataout.write(lineToWrite+"\n");
                                        }
                                    } catch(NumberFormatException nfe){
                                        String lineToWrite = line.substring(0,4)+"."+line.substring(5,7)+"."+line.substring(8,10)+'\t'+"-9999";
                                        dataout.write(lineToWrite+"\n");
                                    }
                                }
                            }
                        }
                    }
                    buff2.close();
                    
                    break ;
                    
                case 'R' :     // Recent data NOT checked for quality
                    java.net.URL remotePath3=new java.net.URL("http://waterdata.usgs.gov/nwis/dv?dd_cd=03_00060_00003&format=rdb&period=730&site_no="+gaugeid);
                    java.io.InputStream inputRemote3=remotePath3.openStream();
                    java.io.BufferedReader buff3 = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemote3));
                    
                    boolean eof3 = false;
                    while(!eof3){
                        String line=buff3.readLine();
                        if (line==null) {
                            eof3=true;
                            System.out.println("Created new data file ="+output_dir+gaugeid+filesuffix+".txt.gz");
                        } else {
                            if (line.charAt(0)=='U'){     // U for USGS
                                line = line.substring(14);
                                if (line.length() > 11){        // 11 is length of date + tab ... (no data)
                                    //System.out.println(line.length());
                                    line = line.substring(0,4)+"."+line.substring(5,7)+"."+line.substring(8,10)+'\t'+line.substring(11);
                                    dataout.write(line+'\n');
                                }
                            }
                        }
                    }
                    buff3.close();
                    
                    break ;
                    
            }
            
            dataout.close();
            
        }catch(java.net.MalformedURLException MUE){
            System.err.println(MUE);
        }catch(java.io.IOException IOE){
            System.err.println(IOE);
        }
        
    }
    
    /**
     * Tests for the class
     * @param arguments Comand line arguments
     */
    public static void main(String[ ] arguments) { //throws java.io.IOException IOE{
        
        ImportUSGS_dailyStreamflow data = new ImportUSGS_dailyStreamflow("/scratch/");
        data.StreamFlow("NM","09430500",'H'); //Gila River, Historical
        //data.StreamFlow("NM","09430600",'H'); //Mogollon Creek, Historical
        
        //data.StreamFlow("NM","09442680",'H'); //San Francisco near Reserve, Historical
        
        //data.StreamFlow("KS","07147070",'H'); //Towanda, Historical
        //data.StreamFlow("KS","07147070",'R'); //Towanda, Recent
        //data.StreamFlow("KS","07146995",'R');  //Rock Creek, Recent
        
    }
    
}
