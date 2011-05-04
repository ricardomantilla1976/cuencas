/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.*;

/**
 *
 * @author ricardo
 */
public class StreamFlowTimeSeries {

    private float regInterval;
    private java.util.Hashtable recordTimeValue;
    private double firstTime;
    private double lastTime;
    private double[] timesRecorded;
    private double[][] breakLocations;

    public StreamFlowTimeSeries(java.io.File theFile) {

        recordTimeValue = new java.util.Hashtable<String, Double>();

        try {
            System.out.println(theFile.toString());
            java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(theFile));
            String fullLine;
            fullLine = fileMeta.readLine();
            regInterval = Float.parseFloat(fullLine.split(":")[1]);
            fullLine = fileMeta.readLine();
            fullLine = fileMeta.readLine();

            String[] data = {"", ""};

            if (fullLine != null) {
                if (fullLine.contains(",")) {
                    data = fullLine.split(",");
                    firstTime = Double.parseDouble(data[0]);
                }
            }

            while (fullLine != null) {
                if (fullLine.contains(",")) {
                    data = fullLine.split(",");
                    if (data[0] != null && data[1] != null) {
                        try {
                            System.out.println("data[0]" + data[0] + "data[1]" + data[1]);
                            java.lang.Double.parseDouble(data[0]);
                            recordTimeValue.put(data[0], new Double(data[1]));
                        } catch (java.lang.NumberFormatException nfe) {
                            System.out.println("Error reading value in the outlet");
                        }


                    }

                }
                fullLine = fileMeta.readLine();
            }

            lastTime = Double.parseDouble(data[0]);

            fileMeta.close();

            Object[] timesSet = recordTimeValue.keySet().toArray();
            timesRecorded = new double[timesSet.length];
            for (int i = 0; i < timesSet.length; i++) {
                timesRecorded[i] = Double.parseDouble(timesSet[i].toString());
            }
            java.util.Arrays.sort(timesRecorded);

            breakLocations = new double[2][timesRecorded.length / 50];

            for (int i = 0; i < breakLocations[0].length; i++) {
                breakLocations[0][i] = timesRecorded.length / (breakLocations[0].length + 2) * (i + 1);
                breakLocations[1][i] = timesRecorded[(int) breakLocations[0][i]];
                //System.out.println(breakLocations[0][i]+" "+breakLocations[1][i]);
            }

        } catch (java.io.IOException ex) {
            System.out.println("<<<<<<<>>>>>>> !!!!!!! Failed While Reading " + theFile);
            Logger.getLogger(StreamFlowTimeSeries.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public double evaluate(double time) {

//    //    System.out.println("Evaluating, Limits: "+firstTime+" "+lastTime);
//        System.out.println("firsttime: "+(Double)recordTimeValue.get(""+firstTime));
//          System.out.println("lastTime: "+(Double)recordTimeValue.get(""+lastTime));
//        System.out.println("Desired Time: "+time+" Test "+(time <= timesRecorded[0]));

        if (time < firstTime) {
            return 0.0;
        }
        if (time >= lastTime) {
            return 0.0;
        }

        int k = 0;
        for (int i = 0; i < breakLocations[0].length; i++) {
            if (time > breakLocations[1][i]) {
                k = (int) breakLocations[0][i];
            }
            //    System.out.println("Evaluating, Limits: "+firstTime+" "+lastTime);
        }

        while (time > timesRecorded[k] && k < timesRecorded.length - 1) {
            k++;
        }

        while (time < timesRecorded[k]) {
            k--;
        }

        if (k == timesRecorded.length - 1) {
            return 0.0;
        }
        if (k == 0) {
            return 0.0;
        }
        double leftT = timesRecorded[k];
        double rightT = timesRecorded[k + 1];

        Double leftF = (Double) recordTimeValue.get("" + leftT);

        //  System.out.println("Indexes "+time+" "+leftT+" "+rightT);

        while (leftF == null) {
            leftF = (Double) recordTimeValue.get("" + leftT);
            //System.out.println("leftT "+leftT+" firstTime"+firstTime);
            if (leftT < firstTime) {
                leftF = (Double) recordTimeValue.get(firstTime);
                if (leftF == null) {
                    leftF = 0.0;
                }
            }
            leftT -= 0.1;
            rightT -= 0.1;
            // System.out.println("(Double)recordTimeValue.get"+(Double)recordTimeValue.get(""+leftT));

            // System.out.println("leftT "+leftT+  "  leftF" +leftF);
        }

        Double rightF = (Double) recordTimeValue.get("" + rightT);

        //System.out.println("Ready "+leftT+" "+rightT);

        if (leftT == rightT) {
            return leftF;
        }

        if (leftF != null && rightF != null) {
            return leftF + (time - leftT) / (rightT - leftT) * (rightF.doubleValue() - leftF.doubleValue());
        }

        return 0.0D;
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String name = "/Users/ricardo/simulationResults/Parallel/WalnutGulch/walnutGulchUpdated_194_281-precipitation_interpolated_ev02.001000.19.August.1971-IR_0.0-Routing_GK_params_0.3_-0.1_0.5.csv.Outlet.csv";
        StreamFlowTimeSeries timeSeries = new StreamFlowTimeSeries(new java.io.File(name));
        java.util.Date startTime = new java.util.Date();
        System.out.println("Start Time:" + startTime.toString());
        for (int i = 0; i < 100000; i++) {
            double time = 856860.0034666666 + (860914.9867999998 - 856860.0034666666) * Math.random();
            double value = timeSeries.evaluate(time);
            //System.out.println(time+" "+value);
        }
        java.util.Date endTime = new java.util.Date();
        System.out.println("End Time:" + endTime.toString());
        System.out.println("Running Time:" + (.001 * (endTime.getTime() - startTime.getTime())) + " seconds");
    }
}
