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

                java.net.URL remotePath1 = new java.net.URL("http://waterdata.usgs.gov/ia/nwis/uv/?site_no="+list[i]+"&agency_cd=USGS");
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

    private String[] list={ "05387440",
                            "05387490",
                            "05387500",
                            "05388000",
                            "05388250",
                            "05388500",
                            "05389000",
                            "05389400",
                            "05411400",
                            "05411600",
                            "05411850",
                            "05411950",
                            "05412000",
                            "05412020",
                            "05412041",
                            "05412056",
                            "05412060",
                            "05412100",
                            "05412340",
                            "05412400",
                            "05412500",
                            "05414500",
                            "05416900",
                            "05417000",
                            "05417500",
                            "05417700",
                            "05418000",
                            "05418400",
                            "05418450",
                            "05418500",
                            "05420300",
                            "05420560",
                            "05420680",
                            "05421000",
                            "05421682",
                            "05421740",
                            "05421760",
                            "05422000",
                            "05422450",
                            "05422470",
                            "05422560",
                            "05422584",
                            "05422586",
                            "05422600",
                            "05448150",
                            "05448290",
                            "05448400",
                            "05448500",
                            "05449000",
                            "05449500",
                            "05451080",
                            "05451210",
                            "05451500",
                            "05451700",
                            "05451900",
                            "05452000",
                            "05452200",
                            "05452500",
                            "05453000",
                            "05453100",
                            "05453520",
                            "05454000",
                            "05454090",
                            "05454220",
                            "05454300",
                            "05454500",
                            "05455000",
                            "05455010",
                            "05455100",
                            "05455500",
                            "05455700",
                            "05457000",
                            "05457505",
                            "05457700",
                            "05458000",
                            "05458300",
                            "05458500",
                            "05458900",
                            "05459000",
                            "05459500",
                            "05460400",
                            "05460500",
                            "05461000",
                            "05461390",
                            "05462000",
                            "05463000",
                            "05463050",
                            "05463500",
                            "05464000",
                            "05464130",
                            "05464137",
                            "05464220",
                            "05464315",
                            "05464420",
                            "05464500",
                            "05464640",
                            "05464942",
                            "05465000",
                            "05465500",
                            "05465700",
                            "05470000",
                            "05470500",
                            "05471000",
                            "05471012",
                            "05471013",
                            "0547101350",
                            "05471014",
                            "05471032",
                            "05471040",
                            "05471050",
                            "05471200",
                            "05471500",
                            "05472500",
                            "05473000",
                            "05473065",
                            "05473400",
                            "05473450",
                            "05473500",
                            "05474000",
                            "05475350",
                            "05476000",
                            "05476500",
                            "05476590",
                            "05476735",
                            "05476750",
                            "05478000",
                            "05478265",
                            "05479000",
                            "05479500",
                            "05480000",
                            "05480080",
                            "05480500",
                            "05480820",
                            "05481000",
                            "05481300",
                            "05481500",
                            "05481650",
                            "05481950",
                            "05482000",
                            "05482135",
                            "05482170",
                            "05482300",
                            "05482430",
                            "05482500",
                            "05483000",
                            "05483343",
                            "05483450",
                            "05483600",
                            "05484000",
                            "05484500",
                            "05484600",
                            "05484650",
                            "05484800",
                            "05484900",
                            "05485000",
                            "05485500",
                            "05485605",
                            "05485640",
                            "05486000",
                            "05486490",
                            "05487470",
                            "05487500",
                            "05487520",
                            "05487540",
                            "05487550",
                            "05487980",
                            "05488000",
                            "05488110",
                            "05488200",
                            "05488500",
                            "05489000",
                            "05489090",
                            "05489190",
                            "05489500",
                            "05490500",
                            "05491000",
                            "05494300",
                            "05494500",
                            "06483270",
                            "06483290",
                            "06483500",
                            "06484000",
                            "06599900",
                            "06599950",
                            "06600000",
                            "06600100",
                            "06600300",
                            "06600500",
                            "06602020",
                            "06602400",
                            "06602410",
                            "06604100",
                            "06604215",
                            "06604400",
                            "06604440",
                            "06605000",
                            "06605100",
                            "06605600",
                            "06605850",
                            "06606600",
                            "06606700",
                            "06607000",
                            "06607200",
                            "06607500",
                            "06607510",
                            "06608500",
                            "06609200",
                            "06609500",
                            "06609590",
                            "06609600",
                            "06610500",
                            "06610520",
                            "06610657",
                            "06806000",
                            "06807320",
                            "06807410",
                            "06808000",
                            "06808200",
                            "06808500",
                            "06808820",
                            "06809000",
                            "06809210",
                            "06809500",
                            "06809900",
                            "06810000",
                            "06811840",
                            "06812000",
                            "06817000",
                            "06818750",
                            "06819185",
                            "06819190",
                            "06897950",
                            "06898000",
                            "06898400",
                            "06903400",
                            "06903500",
                            "06903700",
                            "06903900",
                            "06904000",
                            "06904010"};

}
