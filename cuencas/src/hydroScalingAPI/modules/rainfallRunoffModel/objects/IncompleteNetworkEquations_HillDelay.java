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
 * NetworkEquations_ChannelLosses.java
 *
 * Created on July 3, 2007, 9:45 AM
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import java.io.File;

/**
 * This calss implements the set of non-linear ordinary differential equations used
 * to simulate flows along the river network.  The function is writen as a 
 * {@link hydroScalingAPI.util.ordDiffEqSolver.BasicFunction}
 * that is used by the
 * {@link hydroScalingAPI.util.ordDiffEqSolver.RKF}
 * @author Ricardo Mantilla 
 */
public class IncompleteNetworkEquations_HillDelay implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {

    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;
    private int routingType;
    private double qi, effPrecip, qs, qe, Q_trib, K_Q;
    private double[] output;
    private float[][] manningArray, cheziArray, widthArray, lengthArray, slopeArray, CkArray;
    private float[][] areasHillArray, upAreasArray;
    private double So, Ts, Te, vh; // not an array because I assume uniform soil properties
    private double lamda1, lamda2;
    private int connectingLink;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.StreamFlowTimeSeries[] upFlows;
    private double change = 0.0;

    /**
     * Creates new NetworkEquations_Simple
     * @param links The topologic structure of the river network
     * @param hillinf The parameters manager for the system of hillsopes
     * @param linkIn The parameters manager for links in the network
     * @param rt The routing scheme.  Available schemes are <br>
     * <p>0: Spatially variable Chezi coefficient </p>
     * <p>1: Spatially uniform Chezi coefficient </p>
     * <p>2: Constant Velocity</p>
     * <p>3: Spatially uniform Manning coefficient</p>
     * <p>4: Spatially variable Manning coefficient</p>
     * <p>5: Velocity based on parametrization v=Ck*q^lambda1*A^lambda2</p>
     */
    public IncompleteNetworkEquations_HillDelay(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis links, hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo hillinf, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linkIn, int rt, int connLink, java.io.File[] inputFiles) {
        linksConectionStruct = links;
        basinHillSlopesInfo = hillinf;
        linksHydraulicInfo = linkIn;
        routingType = rt;

        upAreasArray = linksHydraulicInfo.getUpStreamAreaArray();
        cheziArray = linksHydraulicInfo.getCheziArray();
        manningArray = linksHydraulicInfo.getManningArray();
        widthArray = linksHydraulicInfo.getWidthArray();
        lengthArray = linksHydraulicInfo.getLengthInKmArray();
        for (int i = 0; i < lengthArray[0].length; i++) {
            lengthArray[0][i] = lengthArray[0][i] * 1000;
        }
        slopeArray = linksHydraulicInfo.getSlopeArray();

        areasHillArray = basinHillSlopesInfo.getAreasArray();
        So = basinHillSlopesInfo.So(0);
        Ts = basinHillSlopesInfo.Ts(0);
        Te = basinHillSlopesInfo.Te(0);

        lamda1 = linksHydraulicInfo.getLamda1();
        lamda2 = linksHydraulicInfo.getLamda2();
        CkArray = linksHydraulicInfo.getCkArray();

        vh = 0.1;

        connectingLink = connLink;

        upFlows = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StreamFlowTimeSeries[inputFiles.length];
        for (int i = 0; i < inputFiles.length; i++) {
            upFlows[i] = new hydroScalingAPI.modules.rainfallRunoffModel.objects.StreamFlowTimeSeries(inputFiles[i]);
        }

        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(2008, 5, 7, 0, 0, 0);
        change = date.getTimeInMillis() / 1000.0 / 60.0;

    }

    /**
     * An empty but required evaluate method
     * @param input The values used to evaluate the function
     * @param time The time at which the function is evaluated
     * @return The value of the function
     */
    public float[] eval(float[] input, float time) {
        return null;
    }

    /**
     * An empty but required evaluate method
     * @param input The values used to evaluate the function
     * @return The value of the function
     */
    public float[] eval(float[] input) {
        return null;
    }

    /**
     * An empty but required evaluate method
     * @param input The values used to evaluate the function
     * @return The value of the function
     */
    public double[] eval(double[] input) {
        return null;
    }

    /**
     * The acual evaluate method for this function.  The equation implemented here is
     * 
     * dq/dt=sum(q_trib)-q
     * ds/dt=P-E-q_surf-q_sub
     * 
     * Where q_trib is the flow for the incoming links, s is the storage in the
     * hillslope, P is precipitation, E is evaporation, q_surf is the overland runoff
     * and q_sub is the subsurface runoff
     * @param input The values used to evaluate the function
     * @param time The time at which the function is evaluated
     * @return The value of the function
     */
    public double[] eval(double[] input, double time) {
        //the input's length is twice the number of links... the first half corresponds to links discharge and the second to hillslopes storage

        for (int i = 0; i < input.length; i++) {
            if (input[i] < 0.0) {
                input[i] = 0.0;
            }
        }

        int nLi = linksConectionStruct.connectionsArray.length;

        output = new double[input.length];
        /*java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(time*60.*1000.0));
        System.out.println("    "+thisDate.getTime()+" "+basinHillSlopesInfo.precipitation(0,time)+" "+input[287]);*/

        double maxInt = 0;

        //double rr=0.5;
        double rr = 1.0; // for cedar river iowa simulation
//
//        if (time > 20030760.014933333) {
//            rr = time > change ? 0.5 : 0.3;
//        }

        for (int i = 0; i < nLi; i++) {

            if (input[i] < 0) {
                input[i] = 0;
            }

            double hillPrecIntensity = rr * basinHillSlopesInfo.precipitation(i, time);// for URP event apply this rule*0.143;

            maxInt = Math.max(maxInt, hillPrecIntensity);

            effPrecip = Math.max(hillPrecIntensity - basinHillSlopesInfo.infiltRate(i, time), 0.0);

            qs = vh * lengthArray[0][i] / areasHillArray[0][i] * input[i + nLi] / 1e3 * 3.6;
            qs = effPrecip; //Uncomment to eliminate the effect of hillslope routing

            Q_trib = 0.0;
            for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
            }

            switch (routingType) {

                case 0:
                    K_Q = 3 / 2. * Math.pow(input[i], 1 / 3.)
                            * Math.pow(cheziArray[0][i], 2 / 3.)
                            * Math.pow(widthArray[0][i], -1 / 3.)
                            * Math.pow(lengthArray[0][i], -1)
                            * Math.pow(slopeArray[0][i], 1 / 3.);

                    break;

                case 1:
                    K_Q = 3 / 2. * Math.pow(input[i], 1 / 3.)
                            * Math.pow(200.0, 2 / 3.)
                            * Math.pow(widthArray[0][i], -1 / 3.)
                            * Math.pow(lengthArray[0][i], -1)
                            * Math.pow(slopeArray[0][i], 1 / 3.);

                    break;

                case 2:
                    K_Q = CkArray[0][i] / lengthArray[0][i];
                    break;

                case 3:
                    K_Q = 5 / 3. * Math.pow(input[i], 2 / 5.)
                            * Math.pow(0.03, -3 / 5.)
                            * Math.pow(widthArray[0][i], -2 / 5.)
                            * Math.pow(lengthArray[0][i], -1)
                            * Math.pow(slopeArray[0][i], 3 / 10.);
                    break;

                case 4:
                    K_Q = 5 / 3. * Math.pow(input[i], 2 / 5.)
                            * Math.pow(manningArray[0][i], -3 / 5.)
                            * Math.pow(widthArray[0][i], -2 / 5.)
                            * Math.pow(lengthArray[0][i], -1)
                            * Math.pow(slopeArray[0][i], 3 / 10.);
                    break;
                case 5:
                    K_Q = CkArray[0][i] * Math.pow(input[i], lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
            }

            if (input[i] == 0) {
                K_Q = 0.1 / (lengthArray[0][i]);
            }

            /*if (Math.random() > 0.99) {
            float typDisch=Math.round(input[i]*10000)/10000.0f;
            float typVel=Math.round(K_Q*lengthArray[0][i]*100)/100.0f;
            long typDepth=Math.round(input[i]/typVel/widthArray[0][i]*100);
            long typWidth=Math.round(widthArray[0][i]*100);
            System.out.println("  --> !!  When Discharge is "+typDisch+" m^3/s, A typical Velocity-Depth-Width triplet is "+typVel+" m/s - "+typDepth+" cm - "+typWidth+" cm >> for Link "+i+" with upstream area : "+linksHydraulicInfo.upStreamArea(i)+" km^2");
            }*/

            double ks = 0 / 3.6e6;

            double chanLoss = lengthArray[0][i] * widthArray[0][i] * ks;

            //the links
            output[i] = 60 * K_Q * (1 / 3.6 * areasHillArray[0][i] * qs + Q_trib - input[i] - chanLoss);

            //the surface of the hillslope
            output[i + linksConectionStruct.connectionsArray.length] = 1 / 60. * (effPrecip - qs);

        }

        for (int k = 0; k < upFlows.length; k++) {
            int i = connectingLink;

            switch (routingType) {

                case 0:
                    K_Q = 3 / 2. * Math.pow(input[i], 1 / 3.)
                            * Math.pow(cheziArray[0][i], 2 / 3.)
                            * Math.pow(widthArray[0][i], -1 / 3.)
                            * Math.pow(lengthArray[0][i], -1)
                            * Math.pow(slopeArray[0][i], 1 / 3.);

                    break;

                case 1:
                    K_Q = 3 / 2. * Math.pow(input[i], 1 / 3.)
                            * Math.pow(200.0, 2 / 3.)
                            * Math.pow(widthArray[0][i], -1 / 3.)
                            * Math.pow(lengthArray[0][i], -1)
                            * Math.pow(slopeArray[0][i], 1 / 3.);

                    break;

                case 2:
                    K_Q = CkArray[0][i] / lengthArray[0][i];
                    break;

                case 3:
                    K_Q = 5 / 3. * Math.pow(input[i], 2 / 5.)
                            * Math.pow(0.03, -3 / 5.)
                            * Math.pow(widthArray[0][i], -2 / 5.)
                            * Math.pow(lengthArray[0][i], -1)
                            * Math.pow(slopeArray[0][i], 3 / 10.);
                    break;

                case 4:
                    K_Q = 5 / 3. * Math.pow(input[i], 2 / 5.)
                            * Math.pow(manningArray[0][i], -3 / 5.)
                            * Math.pow(widthArray[0][i], -2 / 5.)
                            * Math.pow(lengthArray[0][i], -1)
                            * Math.pow(slopeArray[0][i], 3 / 10.);
                    break;
                case 5:
                    K_Q = CkArray[0][i] * Math.pow(input[i], lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
            }

            output[i] += 60 * K_Q * (upFlows[k].evaluate(time));
        }

        //if (Math.random() > 0.99) if (maxInt>0) System.out.println("      --> The Max precipitation intensity is: "+maxInt);

        return output;
    }
}
