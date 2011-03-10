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

    private String[] list={ "05490500",
                            "05489500",
                            "05465700",
                            "05488500",
                            "05465500",
                            "05488110",
                            "05487520",
                            "05487500",
                            "05485500",
                            "05465000",
                            "05464500",
                            "05464420",
                            "05482000",
                            "05464315",
                            "05481650",
                            "05481500",
                            "05481300",
                            "05464000",
                            "05463050",
                            "06485500",
                            "06602410",
                            "05474000",
                            "05455700",
                            "05480500",
                            "05485000",
                            "05484900",
                            "05484650",
                            "05484600",
                            "05484500",
                            "05454500",
                            "05473065",
                            "05453520",
                            "05473000",
                            "06810000",
                            "05453100",
                            "06606600",
                            "05452500",
                            "05422000",
                            "05476750",
                            "05421760",
                            "05462000",
                            "05458500",
                            "05476590",
                            "05482500",
                            "05471500",
                            "06808820",
                            "06483500",
                            "05421740",
                            "05458300",
                            "05412500",
                            "05418500",
                            "06605850",
                            "05451500",
                            "05476500",
                            "06605600",
                            "06808500",
                            "05461000",
                            "05479000",
                            "05460500",
                            "05482430",
                            "05460400",
                            "06809900",
                            "05457700",
                            "05421000",
                            "06605100",
                            "05484000",
                            "05412020",
                            "06809500",
                            "05412000",
                            "06600500",
                            "05478265",
                            "05411950",
                            "06609500",
                            "06483290",
                            "05458900",
                            "05457505",
                            "05481000",
                            "05418000",
                            "05471050",
                            "06483270",
                            "05388250",
                            "06607500",
                            "06817000",
                            "06904010",
                            "05472500",
                            "05482300",
                            "06904000",
                            "06898000",
                            "06607200",
                            "05411850",
                            "06807410",
                            "05455500",
                            "05388000",
                            "05471000",
                            "06903900",
                            "05473400",
                            "06604440",
                            "06602020",
                            "05418450",
                            "05418400",
                            "05486490",
                            "05387500",
                            "05459500",
                            "05478000",
                            "05479500",
                            "05487470",
                            "06809210",
                            "05449500",
                            "06605000",
                            "05480820",
                            "05483600",
                            "05387440",
                            "06608500",
                            "05488000",
                            "05489000",
                            "05481950",
                            "05483450",
                            "05412400",
                            "05486000",
                            "05417500",
                            "05463000",
                            "05420680",
                            "05487980",
                            "06602400",
                            "06807320",
                            "05470000",
                            "05417000",
                            "05459000",
                            "05463500",
                            "05458000",
                            "05464220",
                            "05471200",
                            "05416900",
                            "05480080",
                            "06600100",
                            "05482135",
                            "05480000",
                            "05451210",
                            "05389000",
                            "06818750",
                            "05470500",
                            "05455100",
                            "06812000",
                            "05452000",
                            "05421682",
                            "05453000",
                            "06903400",
                            "06600300",
                            "05464640",
                            "05411600",
                            "06903700",
                            "05494500",
                            "05449000",
                            "06604400",
                            "05414500",
                            "05412340",
                            "06609600",
                            "05461390",
                            "05451700",
                            "05491000",
                            "06898400",
                            "05473500",
                            "05454300",
                            "05420560",
                            "05476735",
                            "06819190",
                            "05488200",
                            "05485640",
                            "05448500",
                            "05494300",
                            "06819185",
                            "05482170",
                            "05484800",
                            "05489190",
                            "05452200",
                            "05412100",
                            "06600000",
                            "05473450",
                            "05454220",
                            "06604100",
                            "05485605",
                            "05417700",
                            "05422600",
                            "05451900",
                            "06897950",
                            "06811840",
                            "06484000",
                            "05388500",
                            "06607000",
                            "05448150",
                            "05389400",
                            "06604215",
                            "06599950",
                            "06610520",
                            "06806000",
                            "05411400",
                            "05483000",
                            "06809000",
                            "05454000",
                            "05387490",
                            "05448400",
                            "05487550",
                            "05464137",
                            "05422470",
                            "05471040",
                            "05471014",
                            "06607510",
                            "05422560",
                            "06599900",
                            "05451080",
                            "05464130",
                            "05420300",
                            "05471013",
                            "05489090",
                            "06903500",
                            "05471012",
                            "06808000",
                            "05448290",
                            "06609200",
                            "06606700",
                            "05454090",
                            "05422450",
                            "06808200",
                            "05483343",
                            "06609590",
                            "05487540",
                            "06610500",
                            "06610657",
                            "05412060",
                            "05455000",
                            "05455010",
                            "05464942",
                            "05471032",
                            "05412041",
                            "05412056",
                            "05422584",
                            "05422586",
                            "0547101350"};

}
