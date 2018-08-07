/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.io;

/**
 *
 * @author ricardo
 */
public class TestUSGSforRealTimeNWS {

    public TestUSGSforRealTimeNWS() {
        java.io.InputStream inputRemote1 = null;

        try{

            String testForHistorical="No data were found using your search criteria";
            testForHistorical=testForHistorical.toLowerCase();

            String testForNWS="http://water.weather.gov/";
            testForNWS=testForNWS.toLowerCase();

            String testForActionStages="Major Flood Stage";
            testForActionStages=testForActionStages.toLowerCase();

            for (int i = 0; i < list.length; i++) {

                java.net.URL remotePath1 = new java.net.URL("http://waterdata.usgs.gov/ia/nwis/uv?site_no="+list[i]+"");
                inputRemote1 = remotePath1.openStream();
                java.io.BufferedReader buff1 = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemote1));
                boolean eof1 = false;
                boolean classified=false;
                boolean isNWS=false;
                String nwsCode="";

                System.out.print(list[i]);

                while (!eof1) {
                    String line = buff1.readLine();
                    if (line==null) {
                        eof1=true;
                    } else {
                        if(line.toLowerCase().indexOf(testForHistorical)!=-1){
                            System.out.print(","+"historical");
                            eof1=true;
                            classified=true;
                        }

                        if(line.toLowerCase().indexOf(testForNWS)!=-1){

                            nwsCode=line.toLowerCase().split("gage=")[1].split("&")[0];

                            System.out.print(","+"nws ("+nwsCode+")");
                            eof1=true;
                            classified=true;
                            isNWS=true;
                        }
                    }
                }
                buff1.close();

                if(!classified)  System.out.print(","+"real-time");

                if(isNWS){

                    remotePath1 = new java.net.URL("http://water.weather.gov//ahps2/hydrograph.php?wfo=dmx&gage="+nwsCode+"&view=1,1,1,1,1,1");
                    inputRemote1 = remotePath1.openStream();
                    buff1 = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemote1));
                    eof1 = false;
                    boolean withStages=false;

                    while (!eof1) {
                        String line = buff1.readLine();
                        if (line==null) {
                            eof1=true;
                        } else {
                            if(line.toLowerCase().indexOf(testForActionStages)!=-1){

                                String fs1="";
                                if(line.split("</td>")[1].split(">").length == 2) fs1=line.split("</td>")[1].split(">")[1]; else fs1="N/A";
                                line = buff1.readLine();
                                String fs2="";
                                if(line.split("</td>")[1].split(">").length == 2) fs2=line.split("</td>")[1].split(">")[1]; else fs2="N/A";
                                line = buff1.readLine();
                                String fs3="";
                                if(line.split("</td>")[1].split(">").length == 2) fs3=line.split("</td>")[1].split(">")[1]; else fs2="N/A";
                                line = buff1.readLine();
                                String fs4="";
                                if(line.split("</td>")[1].split(">").length == 2) fs4=line.split("</td>")[1].split(">")[1]; else fs2="N/A";


                                System.out.print(","+fs4+","+fs3+","+fs2+","+fs1);
                                eof1=true;
                                withStages=true;
                            }
                        }
                    }
                    buff1.close();

                    if(!withStages) System.out.print(",N/A,N/A,N/A,N/A");

                }

                System.out.println();

            }

        }catch(java.net.MalformedURLException MUE){
            System.err.println(MUE);
        }catch(java.io.IOException IOE){
            System.err.println(IOE);
        }
    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {

            new TestUSGSforRealTimeNWS();
    }

    private String[] list={ "05453600",
"05451770",
"06609560",
"05451700",
"05421890",
"05464315",
"05420680",
"05480820",
"05388310",
"05481650",
"05422470",
"05454090",
"05464780",
"05387405",
"05463500",
"05412400",
"05473400",
"05421760",
"05389000",
"06600300",
"05420460",
"05387440",
"05454220",
"06610505",
"06808820",
"05457505",
"05451900",
"05418110",
"05471200",
"05421682",
"05453520",
"05455100",
"06602298",
"06809900",
"05470000",
"05487520",
"05459490",
"05451070",
"05460400",
"05451210",
"05464942",
"05412340",
"05482300",
"05483600",
"06903900",
"05453000",
"05488200",
"05484600",
"05483450",
"05448400",
"06805850",
"06602400",
"05452200",
"05473065",
"05452000",
"05455010",
"05488110",
"05476735",
"05464220",
"05476000",
"05418400",
"05387320",
"05389400",
"05414400",
"05484800",
"06604440",
"05475350",
"05387490",
"05454000",
"05458000",
"05485605",
"05457000",
"05473450",
"06604200",
"05422600",
"05483470",
"05455230",
"06604000",
"05480080",
"05460000",
"05411600",
"05485640",
"05422560",
"05487980",
"05454300",
"05482315",
"06811875",
"06903880",
"05470500",
"05469860",
"05494500",
"05460500",
"05464640",
"06898400",
"05478000",
"05412056",
"05473000",
"06808200",
"05417000",
"06610500",
"05471013",
"06600300",
"05479500",
"06811840",
"05461000",
"06605100",
"05491000",
"05412060",
"05417700",
"05489090",
"06897950",
"05452500",
"05471032",
"05488000",
"05464130",
"05461390",
"06604400",
"06607000",
"05471010",
"05449000",
"05422590",
"05420300",
"05464137",
"06609600",
"05487550",
"06806000",
"05417500",
"06808000",
"06604215",
"05448500",
"05422640",
"06903500",
"05422586",
"05412000",
"06904000",
"05412041",
"05459000",
"05489190",
"05482135",
"05422450",
"05481500",
"05416100",
"06812000",
"05448290",
"06809000",
"05455000",
"06606700",
"05471040",
"05473500",
"06819190",
"05422650",
"05482170",
"05448150",
"05411400",
"05412100",
"05480000",
"05414500",
"06807320",
"06610657",
"05471012",
"05418000",
"05418450",
"06609200",
"06610520",
"05420560",
"05487500",
"05422584",
"06483270",
"06602410",
"05483343",
"06484000",
"0547101001",
"05388500",
"05483000",
"05471014",
"06604100",
"06605600",
"05485000",
"0547101350",
"05451080",
"06607510",
"05388000",
"06818750",
"06609590",
"05487540",
"05411950",
"06601200",
"06810070",
"06486000",
"05420400",
"06807000",
"06609100",
"06805600",
"05388410",
"05411500",
"06610000",
"06813500"};

}
