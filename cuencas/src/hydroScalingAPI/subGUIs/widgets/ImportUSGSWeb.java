package hydroScalingAPI.subGUIs.widgets;

/**
 *
 * @author Eric Osgood
 */
import java.io.*;
import java.net.*;
import java.awt.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;

/**
 *
 * @author Eric Osgood
 */
public class ImportUSGSWeb {

    private boolean connSuccess = false;

    /**
     *Creates new ImportUSGSWeb to download data in correct format.
     */
    public ImportUSGSWeb() {
        //Test connection with water.USGS.gov
        try {
            String urlName;
            urlName = "http://water.usgs.gov/";
            URL url = new URL(urlName);
            URLConnection connection = url.openConnection();
            connection.connect();
            connSuccess = true;
        } catch (IOException e) {
            System.out.println("NO INTERNET CONNECTION FOUND " + e);
        }
    }

    /**
     *Checks if machine is connected to internet.
     * @return connSuccess
     */
    public boolean connectionTest() {
        return connSuccess;
    }

    /**
     *Takes the selected state and returns a vector of codes.
     * @param statesparam
     * @return The list of site codes
     */
    public Vector<String> filterByState(String[][] statesparam) {
        //makes URL to show what data is available for sites from USGS
        Vector filteredList = new java.util.Vector(3000);
        /*statesparam = new String[][] {{"AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "DC", "FL",
        "GA", "HI", "ID", "IL", "IN", "IA", "KS", "KY", "LA", "ME",
        "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH",
        "SC", "SD", "TN", "TX", "UT", "VT", "VA", "WA", "WV", "WI",
        "WY"},{"Stream", "Gage Height", "Volume"}};*/
        try {
            for (int i = 0; i < statesparam[0].length; i++) {
                System.out.println(statesparam[0][i]);  //test
                URL sitedata = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&state_cd=" + statesparam[0][i] + "&site_tp_cd=OC&site_tp_cd=OC-CO&site_tp_cd=ES&site_tp_cd=LK&site_tp_cd=ST&site_tp_cd=ST-CA&site_tp_cd=ST-DCH&site_tp_cd=ST-TS&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=state_cd,site_tp_cd,realtime_parameter_selection");
                System.out.println("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&state_cd=" + statesparam[0][i] + "&site_tp_cd=OC&site_tp_cd=OC-CO&site_tp_cd=ES&site_tp_cd=LK&site_tp_cd=ST&site_tp_cd=ST-CA&site_tp_cd=ST-DCH&site_tp_cd=ST-TS&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=state_cd,site_tp_cd,realtime_parameter_selection");

                BufferedReader in = new BufferedReader(new InputStreamReader(sitedata.openStream()));
                //BufferedWriter fileOut = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(statesparam[0][i] + "out.txt"))));
                String str;
                while ((str = in.readLine()) != null) {
                    //fileOut.write(str);
                    //fileOut.write("\n");
                    if (str.charAt(0) == 'U') {
                        filteredList.add(str);
                    //System.out.println(str);
                    }
                }
                in.close();
            //fileOut.close();
            }
        } catch (IOException e) {
            System.out.println(e);
        }
        return filteredList;
    }

    public Vector<String> filterAllState(String[][] statesparam) {
        Vector stateList = new Vector();

        try {
            URL states = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&state_cd=al&state_cd=ak&state_cd=az&state_cd=ar&state_cd=ca&state_cd=co&state_cd=ct&state_cd=de&state_cd=dc&state_cd=fl&state_cd=ga&state_cd=hi&state_cd=id&state_cd=il&state_cd=in&state_cd=ia&state_cd=ks&state_cd=ky&state_cd=la&state_cd=me&state_cd=md&state_cd=ma&state_cd=mi&state_cd=mn&state_cd=ms&state_cd=mo&state_cd=mt&state_cd=ne&state_cd=nv&state_cd=nh&state_cd=nj&state_cd=nm&state_cd=ny&state_cd=nc&state_cd=nd&state_cd=oh&state_cd=ok&state_cd=or&state_cd=pa&state_cd=ri&state_cd=sc&state_cd=sd&state_cd=tn&state_cd=tx&state_cd=ut&state_cd=vt&state_cd=va&state_cd=wa&state_cd=wv&state_cd=wi&state_cd=wy&index_pmcode_30208=1&index_pmcode_00061=1&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=state_cd,realtime_parameter_selection");
            System.out.println("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&state_cd=al&state_cd=ak&state_cd=az&state_cd=ar&state_cd=ca&state_cd=co&state_cd=ct&state_cd=de&state_cd=dc&state_cd=fl&state_cd=ga&state_cd=hi&state_cd=id&state_cd=il&state_cd=in&state_cd=ia&state_cd=ks&state_cd=ky&state_cd=la&state_cd=me&state_cd=md&state_cd=ma&state_cd=mi&state_cd=mn&state_cd=ms&state_cd=mo&state_cd=mt&state_cd=ne&state_cd=nv&state_cd=nh&state_cd=nj&state_cd=nm&state_cd=ny&state_cd=nc&state_cd=nd&state_cd=oh&state_cd=ok&state_cd=or&state_cd=pa&state_cd=ri&state_cd=sc&state_cd=sd&state_cd=tn&state_cd=tx&state_cd=ut&state_cd=vt&state_cd=va&state_cd=wa&state_cd=wv&state_cd=wi&state_cd=wy&index_pmcode_30208=1&index_pmcode_00061=1&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=state_cd,realtime_parameter_selection");
            BufferedReader in = new BufferedReader(new InputStreamReader(states.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                if (str.charAt(0) == 'U') {
                    stateList.add(str);
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println("Error " + e);
        }
        return stateList;
    }

    /**
     *Takes latitudes and longitudes from input and returns a vector of codes.
     * @param bound
     * @return list of site codes
     */
    public Vector<String> filterByLatLon(String[] bound) {
        Vector latLonList = new Vector();
        System.out.println(bound);

        try {
            URL latlonsite = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&nw_longitude_va=" + bound[0] + "&nw_latitude_va=" + bound[1] + "&se_longitude_va=" + bound[2] + "&se_latitude_va=" + bound[3] + "&coordinate_format=decimal_degrees&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=lat_long_bounding_box,realtime_parameter_selection");
            System.out.println("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&nw_longitude_va=" + bound[0] + "&nw_latitude_va=" + bound[1] + "&se_longitude_va=" + bound[2] + "&se_latitude_va=" + bound[3] + "&coordinate_format=decimal_degrees&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=lat_va&column_name=long_va&column_name=drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=lat_long_bounding_box,realtime_parameter_selection");
            BufferedReader in = new BufferedReader(new InputStreamReader(latlonsite.openStream()));
            String str;
            while ((str = in.readLine()) != null) {
                if (str.charAt(0) == 'U') {

                    latLonList.add(str);
                }
            }
            in.close();
        } catch (IOException e) {
            System.out.println(e);
        }
        return latLonList;
    }

    /**
     *Takes a vector of codes writes the selected sites to file.
     * @param s
     * @param v
     * @param outputDirs
     * @throws java.io.IOException
     */
    public void getData(String[] s, Vector v,String[] outputDirs) throws IOException {

        for (int i = 0; i < v.size(); i++) {

            String thisCode = (String) v.get(i);
            String startDate, endDate;
            startDate = s[0];
            endDate = s[1];

            System.out.println(startDate + endDate);

            File theFile = new File(outputDirs[0] +File.separator+ thisCode + ".txt.gz");
            FileOutputStream outputLocal = new FileOutputStream(theFile);
            GZIPOutputStream outputCompressed = new java.util.zip.GZIPOutputStream(outputLocal);
            BufferedWriter dataOut = new BufferedWriter(new OutputStreamWriter(outputCompressed));

            URL siteinfo = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&search_site_no=" + thisCode + "&search_site_no_match_type=exact&index_pmcode_00065=1&index_pmcode_00060=1&index_pmcode_00042=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=site_tp_cd&column_name=lat_va&column_name=long_va&column_name=state_cd&column_name=alt_va&column_name=drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=search_site_no,realtime_parameter_selection");
            System.out.println("SITEINFO URL:  " + siteinfo);

            BufferedReader in2 = new BufferedReader(new InputStreamReader(siteinfo.openStream()));

            boolean eof1 = false;
            while (!eof1) {
                String line = in2.readLine();
                if (line == null) {
                    eof1 = true;
                } else {
                    if (line.charAt(0) == 'U') {
                        String[] linarray = line.split("\t");
                        float area;
                        float altitude;
                        dataOut.write("[code]" + '\n');
                        dataOut.write(linarray[1] + '\n');
                        dataOut.write('\n');
                        dataOut.write("[agency]" + '\n');
                        dataOut.write(linarray[0] + '\n');
                        dataOut.write('\n');
                        dataOut.write("[type]" + '\n');
                        dataOut.write("Streamflow" + '\n');
                        dataOut.write('\n');
                        dataOut.write("[site name]" + '\n');
                        dataOut.write(linarray[2] + '\n');
                        dataOut.write('\n');
                        dataOut.write("[stream name]" + '\n');
                        dataOut.write(linarray[2] + '\n');
                        dataOut.write('\n');
                        dataOut.write("[county]" + '\n');
                        dataOut.write('\n');
                        dataOut.write('\n');
                        dataOut.write("[state]" + '\n');

                        //STATE METHOD!!!
                        String state;
                        String st = linarray[8];
                        state = states(st);
                        //each state has a # assigned to it.  http://water.usgs.gov/watuse/data/ascii/codata_bystate/ma90co
                        dataOut.write(state);
                        dataOut.write('\n');



                        dataOut.write('\n');
                        dataOut.write("[data source]" + '\n');
                        dataOut.write("URL: http://nwis.waterdata.usgs.gov/nm/nwis/inventory" + '\n');
                        dataOut.write('\n');
                        dataOut.write("[latitude (deg:min:sec)]" + '\n');
                        String lat = linarray[4];
                        lat = lat.substring(0, 2) + ":" + lat.substring(2, 4) + ":" + lat.substring(4, 6) + " N";
                        dataOut.write(lat + '\n');
                        dataOut.write('\n');
                        dataOut.write("[longitude (deg:min:sec)]" + '\n');
                        String lon = linarray[5];
                        lon = lon.substring(0, 3) + ":" + lon.substring(3, 5) + ":" + lon.substring(5, 7) + " W";
                        dataOut.write(lon + '\n');
                        dataOut.write('\n');
                        dataOut.write("[altitude ASL (m)]" + '\n');

                        if (linarray.length == 10) {
                            altitude = Float.parseFloat(linarray[9]);
                            altitude = altitude * 0.3048f;      // *0.3048 to m
                            dataOut.write(String.valueOf(altitude) + '\n');
                        } else {
                            dataOut.write("N / A" + '\n');
                        }

                        dataOut.write('\n');
                        dataOut.write("[drainage area (km^2)]" + '\n');   // raw USGS drainage area in mi^2

                        if (linarray.length == 13) {
                            area = Float.parseFloat(linarray[12]);
                            area = area * 2.5899f;        // *2.5899 to km^2
                            dataOut.write(String.valueOf(area) + '\n');

                        } else {
                            dataOut.write("N / A" + '\n');
                        }


                        dataOut.write('\n');
                        dataOut.write("[data units]" + '\n');
                        dataOut.write("ft^3/s" + '\n');
                        dataOut.write('\n');
                        dataOut.write("[data accuracy]" + '\n');
                        dataOut.write('\n');
                        dataOut.write('\n');
                        dataOut.write("[data (yyyy.mm.dd.hh.mm.ss    value)]" + "\n");
                    }
                }
            }

            URL sitedata = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&search_site_no=" + thisCode + "&search_site_no_match_type=exact&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&sitefile_output_format=html_table&column_name=agency_cd&column_name=site_no&column_name=station_nm&range_selection=date_range&begin_date=" + startDate + "&end_date=" + endDate + "&format=rdb&date_format=YYYY.MM.DD&rdb_compression=value&list_of_search_criteria=search_site_no,realtime_parameter_selection");
            System.out.println("SITEDATA URL:  " + sitedata);

            BufferedReader buff2 = new BufferedReader(new InputStreamReader(sitedata.openStream()));

            boolean eof2 = false;
            while (!eof2) {
                String line = buff2.readLine();
                if (line == null) {
                    eof2 = true;
                    System.out.println("Created new data file!!! " + thisCode + ".txt.gz");
                    System.out.println('\n');
                    System.out.println('\n');
                } else {
                    if (line.charAt(0) == 'U') {
                        line = line.substring(14);
                        if (line.length() > 11) { //11 is length of data + tab....(no data)

                            try {
                                if (line.substring(11).split("\t").length > 0) {
                                    Float.parseFloat(line.substring(11).split("\t")[0]);
                                    String lineToWrite = line.substring(0, 4) + "." + line.substring(5, 7) + "." + line.substring(8, 10) + '\t' + line.substring(11).split("\t")[0];
                                    dataOut.write(lineToWrite + '\n');
                                } else {
                                    String lineToWrite = line.substring(0, 4) + "." + line.substring(5, 7) + "." + line.substring(8, 10) + '\t' + "-9999";
                                    dataOut.write(lineToWrite + "\n");
                                }
                            } catch (NumberFormatException nfe) {
                                String lineToWrite = line.substring(0, 4) + "." + line.substring(5, 7) + "." + line.substring(8, 10) + '\t' + "-9999";
                                dataOut.write(lineToWrite + "\n");
                            }
                        }
                    }
                }
            }

            in2.close();
            dataOut.close();

            URL sitedata2 = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&search_site_no=" + thisCode + "&search_site_no_match_type=exact&index_pmcode_00065=1&index_pmcode_00060=1&sort_key=site_no&group_key=NONE&sitefile_output_format=html_table&column_name=agency_cd&column_name=site_no&column_name=station_nm&range_selection=date_range&begin_date=" + startDate + "&end_date=" + endDate + "&format=rdb&date_format=YYYY.MM.DD&rdb_compression=value&list_of_search_criteria=search_site_no,realtime_parameter_selection");
            File theFile2 = new File(outputDirs[1] +File.separator+ thisCode + ".txt.gz");
            FileOutputStream outputLocal2 = new FileOutputStream(theFile2);
            GZIPOutputStream outputCompressed2 = new java.util.zip.GZIPOutputStream(outputLocal2);
            BufferedWriter dataOut2 = new BufferedWriter(new OutputStreamWriter(outputCompressed2));
            BufferedReader buff3 = new BufferedReader(new InputStreamReader(sitedata2.openStream()));

            URL siteinfo2 = new URL("http://waterdata.usgs.gov/nwis/dv?referred_module=sw&search_site_no=" + thisCode + "&search_site_no_match_type=exact&index_pmcode_00065=1&index_pmcode_00060=1&index_pmcode_00042=1&sort_key=site_no&group_key=NONE&format=sitefile_output&sitefile_output_format=rdb&column_name=agency_cd&column_name=site_no&column_name=station_nm&column_name=site_tp_cd&column_name=lat_va&column_name=long_va&column_name=state_cd&column_name=alt_va&column_name=drain_area_va&range_selection=days&period=365&date_format=YYYY-MM-DD&rdb_compression=file&list_of_search_criteria=search_site_no,realtime_parameter_selection");
            System.out.println("SITEINFO URL:  " + siteinfo);

            BufferedReader in3 = new BufferedReader(new InputStreamReader(siteinfo2.openStream()));

            eof1 = false;
            while (!eof1) {
                String line = in3.readLine();
                if (line == null) {
                    eof1 = true;
                } else {
                    if (line.charAt(0) == 'U') {
                        String[] linarray = line.split("\t");
                        float area;
                        float altitude;
                        dataOut2.write("[code]" + '\n');
                        dataOut2.write(linarray[1] + '\n');
                        dataOut2.write('\n');
                        dataOut2.write("[agency]" + '\n');
                        dataOut2.write(linarray[0] + '\n');
                        dataOut2.write('\n');
                        dataOut2.write("[type]" + '\n');
                        dataOut2.write("Streamflow" + '\n');
                        dataOut2.write('\n');
                        dataOut2.write("[site name]" + '\n');
                        dataOut2.write(linarray[2] + '\n');
                        dataOut2.write('\n');
                        dataOut2.write("[stream name]" + '\n');
                        dataOut2.write(linarray[2] + '\n');
                        dataOut2.write('\n');
                        dataOut2.write("[county]" + '\n');
                        dataOut2.write('\n');
                        dataOut2.write('\n');
                        dataOut2.write("[state]" + '\n');

                        //STATE METHOD!!!
                        String state;
                        String st = linarray[8];
                        state = states(st);
                        //each state has a # assigned to it.  http://water.usgs.gov/watuse/data/ascii/codata_bystate/ma90co
                        dataOut2.write(state);
                        dataOut2.write('\n');



                        dataOut2.write('\n');
                        dataOut2.write("[data source]" + '\n');
                        dataOut2.write("URL: http://nwis.waterdata.usgs.gov/nm/nwis/inventory" + '\n');
                        dataOut2.write('\n');
                        dataOut2.write("[latitude (deg:min:sec)]" + '\n');
                        String lat = linarray[4];
                        lat = lat.substring(0, 2) + ":" + lat.substring(2, 4) + ":" + lat.substring(4, 6) + " N";
                        dataOut2.write(lat + '\n');
                        dataOut2.write('\n');
                        dataOut2.write("[longitude (deg:min:sec)]" + '\n');
                        String lon = linarray[5];
                        lon = lon.substring(0, 3) + ":" + lon.substring(3, 5) + ":" + lon.substring(5, 7) + " W";
                        dataOut2.write(lon + '\n');
                        dataOut2.write('\n');
                        dataOut2.write("[altitude ASL (m)]" + '\n');

                        if (linarray.length == 10) {
                            altitude = Float.parseFloat(linarray[9]);
                            altitude = altitude * 0.3048f;      // *0.3048 to m
                            dataOut2.write(String.valueOf(altitude) + '\n');
                        } else {
                            dataOut2.write("N / A" + '\n');
                        }

                        dataOut2.write('\n');
                        dataOut2.write("[drainage area (km^2)]" + '\n');   // raw USGS drainage area in mi^2

                        if (linarray.length == 13) {
                            area = Float.parseFloat(linarray[12]);
                            area = area * 2.5899f;        // *2.5899 to km^2
                            dataOut2.write(String.valueOf(area) + '\n');

                        } else {
                            dataOut2.write("N / A" + '\n');
                        }


                        dataOut2.write('\n');
                        dataOut2.write("[data units]" + '\n');
                        dataOut2.write("ft^3/s" + '\n');
                        dataOut2.write('\n');
                        dataOut2.write("[data accuracy]" + '\n');
                        dataOut2.write('\n');
                        dataOut2.write('\n');
                        dataOut2.write("[data (yyyy.mm.dd.hh.mm.ss    value)]" + "\n");
                    }
                }
            }

            boolean eof3 = false;
            while (!eof3) {
                String line2 = buff3.readLine();
                if (line2 == null) {
                    eof3 = true;
                } else {
                    if (line2.charAt(0) == 'U') {
                        line2 = line2.substring(14);
                        if (line2.length() > 11) { //11 is length of data + tab....(no data)

                            try {
                                if (line2.substring(11).split("\t").length > 0 && line2.substring(11).split("\t").length > 2) {
                                    Float.parseFloat(line2.substring(11).split("\t")[2]);
                                    String lineToWrite2 = line2.substring(0, 4) + "." + line2.substring(5, 7) + "." + line2.substring(8, 10) + '\t' + line2.substring(11).split("\t")[2];
                                    dataOut2.write(lineToWrite2 + '\n');

                                } else {
                                    String lineToWrite2 = line2.substring(0, 4) + "." + line2.substring(5, 7) + "." + line2.substring(8, 10) + '\t' + "-9999";
                                    dataOut2.write(lineToWrite2 + "\n");
                                }
                            } catch (NumberFormatException nfe) {
                                String lineToWrite2 = line2.substring(0, 4) + "." + line2.substring(5, 7) + "." + line2.substring(8, 10) + '\t' + "-9999";
                                dataOut2.write(lineToWrite2 + "\n");
                            }
                        }
                    }
                }
            }


            in3.close();
            dataOut2.close();
            buff3.close();
        }
    }

    /**
     *Takes a USGS code for a state and converts it into the actual state name to be written to file.
     * @param st
     * @return The full state name.
     */
    public String states(String st) {

        int sn = Integer.parseInt(st);

        String state = null;
        String[] stABR = {"Alabama", "Alaska", "", "Arizona", "Arkansas", "California", "", "Colorado", "Connecticut", "Delaware", "District of Columbia",
            "Florida", "Georgia", "", "Hawaii", "Idaho", "Illinois", "Indiana", "Iowa", "Kansas", "Kentucky", "Massachusetts", "Maine", "MaryLand", "", "Michigan",
            "Minnesota", "Mississippi", "Missouri", "Montana", "Nebraska", "Nevada", "New Hampshire", "New Jersey", "New Mexico", "New York",
            "North Carolina", "North Dakota", "Ohio", "Oklahoma", "Oregon", "Pennsilvania", "", "Rhode Island", "South Carolina", "South Dakota", "Tennessee", "Texas",
            "Utah", "Vermont", "Virginia", "", "Washington", "West Virginia", "Wisconsin", "Wyoming"};

        state = stABR[sn - 1];

        return state;
    }

    static void main(String[] arguments) {
        ImportUSGSWeb test = new ImportUSGSWeb();

        String[][] statesparam = new String[][]{{"WI"}, {"Stream", "Gage Height", "Volume"}};


        Vector myVector = test.filterByState(statesparam);
        for (Iterator it = myVector.iterator(); it.hasNext();) {
            System.out.println(it.next());
        }
        System.out.println(test.connectionTest());
    }
}
