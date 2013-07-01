/*
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
/* This code has the different model formulations used by Luciana Cunha
 * on her PhD thesis
 * 
 * HillT - defines the hillslope method used - it can be without (case 0) or with (all the other cases) 
 * hillslope delay, without (case 0 and 1) or with space-time variable runoff coefficient (all the other cases)
 * 
 * HillVelocityT - defines the method to estimate hillslope velocity -
 * The main ones are constant velocity (case 0) and variable velocity based on manning (case 6)
 * 
 * KunsatModelType - defines the method to estimate Kunsat -
 * The main ones are constant velocity (case 0) and variable velocity based on manning (case 6)
 * 
 * 
 * KunsatModelType
 * 
 * Updated on April 11, 2012
 */
package hydroScalingAPI.modules.rainfallRunoffModel.objects;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.TimeZone;

/**
 * This class implements the set of non-linear ordinary differential equations
 * used to simulate flows along the river network. The function is written as a
 * {@link hydroScalingAPI.util.ordDiffEqplver.BasicFunction} that is used by the
 * {@link hydroScalingAPI.util.ordDiffEqplver.RKF}
 *
 * @author Ricardo Mantilla
 */
public class NetworkEquations_AllMethodSerialMay_2012 implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {

    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;
    java.util.Hashtable routingParams;
    private int routingType, HillType, HillVelType, KunsatModelType;
    private float rr;
    private double qp, qp_u, qp_l, qu_s, qe, Q_trib, K_Q, qs_l, q_in_res, q_out_res, qs_p;
    private double effPrecip, qs, qc_f;
    private double[] output;
    private float[][] manningArray, cheziArray, widthArray, lengthArray, slopesLand, slopesLand2, CkArray;
    private double[] slopesChannel;
    private float[][] aH, upAreasArray, landCoversArray, Hill_K_NRCSArray;
    private float[][] hb, hH, Ht, Vt;
    private float[][] Area_Relief_Param;
    private float[][] HydCond;
    private double So, Ts, Te, vh; // not an array because I assume uniform soil properties
    private double lamda1, lamda2;
    private double vHunoff, vsub, EVcoef, SM, lambdaSCS; // velocity of the direct runoff and subsurface runoff - m/s
    private float[][] Vo, Gr, MaxInfRate;
    private double change = 0.0;
    private float cteKf, CQflood, EQflood, CoefAi;
    private double ev_surf, ev_u, ev_s;
    float coefvh = 1.0f;
    float coefks = 1.0f;
    float KuCte = 1.0f;
    float MinPound = 0.0f;

    /**
     * Creates new NetworkEquations_Simple
     *
     * @param links The topologic structure of the river network
     * @param hillinf The parameters manager for the system of hillsopes
     * @param linkIn The parameters manager for links in the network (parallel
     * version)
     * @param connLink The parameters that define the upstream links in the
     * network
     * @param inputFiles The parameters that define the files related to eac
     * upstream links
     * @param (Hashtable) rP All the other parameters necessary for the
     * rainfall-runoff model - depends on the hilltype being used CHANNEL
     * ROUTING SCHEME. Available schemes in this version are <br> <p>2: Constant
     * Velocity</p> <p>5: Velocity based on parametrization
     * v=Ck*q^lambda1*A^lambda2</p>
     *
     * RAINFALL-RUNOFF TRANSFORMATION. Available schemes in this version are
     * <br> <p>0: Constant runoff coefficient, no hillslope delay <p>1: Constant
     * runoff coefficient, hillslope delay <p>5: SCS Method
     *
     * HILLSOPE ROUTING SCHEME. Available schemes in this version are <br> <p>0:
     * with delay - constant velocity in hillslope <p>5: SCS Method
     *
     */
    public NetworkEquations_AllMethodSerialMay_2012(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis links, hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo hillinf, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linkIn, java.util.Hashtable rP) {
        System.out.println("Start NetworkEquations");

        linksConectionStruct = links;
        basinHillSlopesInfo = hillinf;
        linksHydraulicInfo = linkIn;
        routingParams = rP;

        upAreasArray = linksHydraulicInfo.getUpStreamAreaArray(); //[km]
        cheziArray = linksHydraulicInfo.getCheziArray();
        widthArray = linksHydraulicInfo.getWidthArray();
        lengthArray = linksHydraulicInfo.getLengthInKmArray(); //[km]
        aH = basinHillSlopesInfo.getAreasArray();//[km2]

    
        for (int i = 0; i < lengthArray[0].length; i++) {
            lengthArray[0][i] = lengthArray[0][i] * 1000; //[m]
            //System.out.println("manning   "+basinHillSlopesInfo.HillManning(i));
        }
//System.exit(1);
        slopesChannel = new double[lengthArray[0].length];
        landCoversArray = new float[1][lengthArray[0].length];
        slopesLand = new float[1][lengthArray[0].length];
        slopesLand2 = new float[1][lengthArray[0].length];
        Hill_K_NRCSArray = new float[1][lengthArray[0].length];
        Area_Relief_Param = new float[4][lengthArray[0].length]; //Area in km and depth in m
        hb = new float[1][lengthArray[0].length]; // mm
        hH = new float[1][lengthArray[0].length]; // mm
        MaxInfRate = new float[1][lengthArray[0].length];

        Ht = new float[1][lengthArray[0].length]; // mm
        Vt = new float[1][lengthArray[0].length]; //[m3]
        HydCond = new float[1][lengthArray[0].length]; //[m3]


        float HD = -9.f;
        if (routingParams.get("ConstSoilStorage") != null) {
            HD = ((Float) routingParams.get("ConstSoilStorage")).floatValue();
        }
        System.out.println("ConstSoilStorage" + HD);

        cteKf = 0.1f;
        if (routingParams.get("floodplaincte") != null) {
            cteKf = ((Float) routingParams.get("floodplaincte")).floatValue();
        }
        CQflood = 4.0f;
        if (routingParams.get("CQflood") != null) {
            CQflood = ((Float) routingParams.get("CQflood")).floatValue();
        }
        EQflood = 0.51f;
        if (routingParams.get("EQflood") != null) {
            EQflood = ((Float) routingParams.get("EQflood")).floatValue();
        }
        System.out.println("CQflood " + CQflood + "   EQflood" + EQflood + "   ctekf" + cteKf);

        // HILLSLOPE GEOMETRY RELATIONSHIP
        vsub = -9;
        if (routingParams.get("vssub") != null) {
            vsub = ((Float) routingParams.get("vssub")).floatValue();
        }

        EVcoef = -9;
        if (routingParams.get("EVcoef") != null) {
            EVcoef = ((Float) routingParams.get("EVcoef")).floatValue();
        }
        //System.out.println("equation object 8");cc
        if (routingParams.get("Coefks") != null) {
            coefks = ((Float) routingParams.get("Coefks")).floatValue();
        }

        if (routingParams.get("KunsatCte") != null) {
            KuCte = ((Float) routingParams.get("KunsatCte")).floatValue();
        }
        for (int i = 0; i < lengthArray[0].length; i++) {
            slopesChannel[i] = linksHydraulicInfo.Slope(i);
            
            landCoversArray[0][i] = (float) basinHillSlopesInfo.LandUseSCS(i);
            slopesLand[0][i] = (float) basinHillSlopesInfo.getHillslope(i);
            slopesLand2[0][i] = (float) basinHillSlopesInfo.getHillslope2(i);
            Hill_K_NRCSArray[0][i] = (float) basinHillSlopesInfo.Hill_K_NRCS(i);
            if (HD == -1) {
                hb[0][i] = (float) basinHillSlopesInfo.SCS_S1(i); // [mm] maximum storage - considering basin was dry
            } else if (HD == -2) {
                hb[0][i] = (float) basinHillSlopesInfo.SCS_S2(i);
            } else if (HD == -3) {
                hb[0][i] = (float) basinHillSlopesInfo.SWA150(i);
                if (hb[0][i] < -4) {
                    hb[0][i] = (float) basinHillSlopesInfo.SCS_S1(i);
                }
            } else {
                hb[0][i] = HD;
            } // [mm] maximum storage - considering basin was dry




            MaxInfRate[0][i] = (float) basinHillSlopesInfo.MaxInfRate(i);

            //System.out.println("MAx Inf Rate " + MaxInfRate[0][i]);
            hH[0][i] = (float) basinHillSlopesInfo.HillRelief(i) * 1000; // [mm] hillslope relief
            Ht[0][i] = hb[0][i] + hH[0][i]; // [mm] hillslope relief
            Vt[0][i] = (float) ((hb[0][i]) * aH[0][i] * 1e3); // in m3
            Area_Relief_Param[0][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 0);
            Area_Relief_Param[1][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 1);
            Area_Relief_Param[2][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 2);
            Area_Relief_Param[3][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 3);
            if (vsub == -9) {
                HydCond[0][i] = coefks * (float) basinHillSlopesInfo.AveHydCond(i);
            } else if (vsub > 0) {
                HydCond[0][i] = coefks * (float) vsub;
            } else {
                HydCond[0][i] = coefks * (float) (56. * Math.pow(basinHillSlopesInfo.AveHydCond(i), 0.28) / (1 + 0.05 * Math.exp(0.062 * hb[0][i]))) - 2;
            }
            System.out.println("Hyd cond   " + i + "   " + HydCond[0][i]);
            // System.exit(1);
        }

//System.exit(1);



        //So=basinHillSlopesInfo.So(0);
        //Ts=basinHillSlopesInfo.Ts(0);
        //Te=basinHillSlopesInfo.Te(0);
        System.out.println("equation object 3");
        lamda1 = linksHydraulicInfo.getLamda1();
        lamda2 = linksHydraulicInfo.getLamda2();
        CkArray = linksHydraulicInfo.getCkArray();

        vh = 0.1;


        java.util.Calendar date = java.util.Calendar.getInstance();
        date.set(2008, 5, 7, 0, 0, 0);
        change = date.getTimeInMillis() / 1000.0 / 60.0;

        // SCS implementation



        System.out.println("equation object 5");
        if (routingParams.get("RoutingT") != null) {
            routingType = (int) ((Float) routingParams.get("RoutingT")).floatValue();
        }
        if (routingParams.get("HillT") != null) {
            HillType = (int) ((Float) routingParams.get("HillT")).floatValue();
        }
        if (routingParams.get("HillVelocityT") != null) {
            HillVelType = (int) ((Float) routingParams.get("HillVelocityT")).floatValue();
        }
        //System.out.println("equation object 8");cc
        if (routingParams.get("RunoffCoefficient") != null) {
            rr = ((Float) routingParams.get("RunoffCoefficient")).floatValue();
        }


        //System.out.println("equation object 8");cc
        if (routingParams.get("Coefvh") != null) {
            coefvh = ((Float) routingParams.get("Coefvh")).floatValue();
        }


        // Future implementation (greenroof and reservoir)
        //Vo=basinHillSlopesInfo.getVolResArray();
        //Gr=basinHillSlopesInfo.getGreenRoofAreaArray(); // area of building in the hillslope (m2)
        System.out.println("equation object 6");
        System.out.println("routingType" + routingType);
        //for (int i=0;i<lengthArray[0].length;i++) Gr[0][i]=Gr[0][i]/(aH[0][i]*1000000);
        float vconst = ((Float) routingParams.get("Vconst")).floatValue();


        if (routingParams.get("vrunoff") != null) {
            vHunoff = ((Float) routingParams.get("vrunoff")).floatValue(); //[m/h]
        }        //System.out.println("equation object 10");
        if (routingParams.get("SoilMoisture") != null) {
            SM = ((Float) routingParams.get("SoilMoisture")).floatValue();
        }
        lambdaSCS = 0.0;
        if (routingParams.get("lambdaSCSMethod") != null) {
            lambdaSCS = ((Float) routingParams.get("lambdaSCSMethod")).floatValue();
        }
        //System.out.println("equation object 11");
        System.out.println("CkArray[0][1]   " + CkArray[0][0]);
        System.out.println("lamda1   " + lamda1);
        System.out.println("lamda2   " + lamda2);
        System.out.println("lengthArray   " + lengthArray[0][0]);
        //System.exit(1);        

    }

    /**
     * An empty but required evaluate method
     *
     * @param input The values used to evaluate the function
     * @param time The time at which the function is evaluated
     * @return The value of the function
     */
    public float[] eval(float[] input, float time) {
        return null;
    }

    /**
     * An empty but required evaluate method
     *
     * @param input The values used to evaluate the function
     * @return The value of the function
     */
    public float[] eval(float[] input) {
        return null;
    }

    /**
     * An empty but required evaluate method
     *
     * @param input The values used to evaluate the function
     * @return The value of the function
     */
    public double[] eval(double[] input) {
        return null;
    }

    /**
     * The acual evaluate method for this function. The equation implemented
     * here is
     *
     * dq/dt=sum(q_trib)-q ds/dt=P-E-q_surf-q_sub
     *
     * Where q_trib is the flow for the incoming links, s is the storage in the
     * hillslope, P is precipitation, E is evaporation, q_surf is the overland
     * runoff and q_sub is the subsurface runoff
     *
     * @param input The values used to evaluate the function
     * @param time The time at which the function is evaluated
     * @return The value of the function
     */
    public double[] eval(double[] input, double time) {
        //the input's length is twice the number of links... the first half corresponds to links discharge and the second to hillslopes storage



        int nLi = linksConectionStruct.connectionsArray.length;

        output = new double[input.length];
        /*java.util.Calendar thisDate=java.util.Calendar.getInstance();
         thisDate.setTimeInMillis((long)(time*60.*1000.0));
         System.out.println("    "+thisDate.getTime()+" "+basinHillSlopesInfo.precipitation(0,time)+" "+input[287]);*/


        switch (HillType) {
            case 0:
                /*
                 *rr - defined runoff coefficient 
                 *no hillslope delay
                 *no base flow 
                 *no evaporation 
                 */

                for (int i = 0; i < nLi; i++) {
                    if (input[i] < 0) {
                        input[i] = 0;
                    }


                    double hillPrec = basinHillSlopesInfo.precipitation(i, time);
                    double p = rr * basinHillSlopesInfo.precipitation(i, time);

                    qp = hillPrec;

                    qp_u = hillPrec - p;
                    qp_l = p;
                    qs_l = 0.0;

                    Q_trib = 0.0;
                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }

                    K_Q = RoutingType(routingType, i, input[i]);

                    ev_surf = 0;
                    ev_u = 0;
                    ev_s = 0;

                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * aH[0][i] * (qp_l + qs_l)) + Q_trib - input[i]); //[m3/s]/min
                    //System.out.println("output[i]        "+output[i] + "   qp_l  " + qp_l);
                    /* hillslope reservoir*/ output[i + nLi] = 0.0;//(1/60.)*(qp-qp_l); //output[mm/min] qd and qs [mm/hour]
/* water table*/ output[i + 2 * nLi] = 0.0;//(1/60.)*(qp_u-qs_l); //output[mm/min] effPrecip and qss [mm/hour]
/* soil volumetric moisture*/ output[i + 3 * nLi] = 0.0; // runoff rate mm/min


                    output[i + 4 * nLi] = 0.0;
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }

                break;

            case 1:
                /*
                 *rr - defined runoff coefficient 
                 **** hillslope delay - vh defined by user
                 *no base flow 
                 *no evaporation 
                 */

                for (int i = 0; i < nLi; i++) {
                    if (input[i] < 0) {
                        input[i] = 0;
                    }

                    double hillPrec = basinHillSlopesInfo.precipitation(i, time);
                    double p = rr * basinHillSlopesInfo.precipitation(i, time);
//                    if (p > 0) {
//                        System.out.println(basinHillSlopesInfo.precipitation(i, time));
//                    }
                    qp = hillPrec; //[mm/h]
                    qp_u = hillPrec - p; //[mm/h]
                    qs_l = 0.0;


                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h
                    //System.out.println("basinHillSlopesInfo.precipitation(i, time)   "+ basinHillSlopesInfo.precipitation(i, time) +"   p   " +p+  "vh   "+vH);
                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * (1 / 0.6) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));


                    Q_trib = 0.0;
                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }
                    ev_surf = 0;
                    ev_u = 0;
                    ev_s = 0;

                    K_Q = RoutingType(routingType, i, input[i]);
                    //case System.out.println("K_Q=  " + K_Q +"  aH[0][i]=  " + aH[0][i] + "  qp_l= " + qp_l);
                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * qp_l) + (aH[0][i] * qs_l))) + Q_trib - input[i]); //[m3/s]/min

                    /* hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u); //output[mm/min] qd and qs [mm/hour]
/* soil reservoir*/ output[i + 2 * nLi] = (1 / 60.) * (qp_u - qs_l); //output[mm/min] effPrecip and qss [mm/hour]
/* soil reservoir*/ output[i + 3 * nLi] = 0.0f; //output[mm/min] effPrecip and qss [mm/hour]

                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }
                break;

            case 11:
                /*
                 *rr - defined runoff coefficient 
                 **** hillslope delay - vh defined by user
                 *no base flow 
                 *no evaporation 
                 */

                for (int i = 0; i < nLi; i++) {
                    if (input[i] < 0) {
                        input[i] = 0;
                    }

                    double hillPrec = basinHillSlopesInfo.precipitation(i, time);
                    double p = rr * basinHillSlopesInfo.precipitation(i, time);



                    qp = hillPrec; //[mm/h]
                    qp_u = hillPrec - p; //[mm/h]
                    qs_l = 0.0;


                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h
                    qp_l = ((1 / lengthArray[0][i]) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));


                    Q_trib = 0.0;
                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }
                    ev_surf = 0;
                    ev_u = 0;
                    ev_s = 0;

                    K_Q = RoutingType(routingType, i, input[i]);

                    double xxx = qp - qp_l - qp_u;
                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * qp_l) + (aH[0][i] * qs_l))) + Q_trib - input[i]); //[m3/s]/min

                    /* hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u); //output[mm/min] qd and qs [mm/hour]

                    /* soil reservoir*/ output[i + 2 * nLi] = (1 / 60.) * (qp_u - qs_l); //output[mm/min] effPrecip and qss [mm/hour]
/* soil reservoir*/ output[i + 3 * nLi] = 0.0f; //output[mm/min] effPrecip and qss [mm/hour]

                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }
                break;


            case 134:
                /* Same as Hill 6 with evaporation
                 * qs_l = Ksat * 1000 * (ai / aH[0][i])* (hrel / hrelmax); - baseflow does not depend on head of water
                 */
//System.out.println(basinHillSlopesInfo.HillManning(0) + "Slope"+ slopesLand[0][0] + "Relief "+basinHillSlopesInfo.HillRelief(0));
// System.out.println(slopesChannel[0] + "land" +slopesLand[0][0] +"land2 " + slopesLand[0][0]);

//System.exit(1);
                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    //double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    double EV = EVcoef * basinHillSlopesInfo.PotEVPT(i, time);
                    // HILLSLOPE GEOMETRY RELATIONSHIPS
                    //System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);

                    double Ia = lambdaSCS * hb[0][i]; //[mm]
                    double ai = 0.0; //[km2]
                    double ap = 0; //[km2]
                    double dsoil = 0; //[mm]
                    double Vs_sat = 0; //[m3]
                    double Vs_unsat = 0; //[m3]
                    double da_dh = 100.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i]) / 1000; // [m]
                    double hrelmax = (Ht[0][i] - hb[0][i]) / 1000; // [m]
                    if (hrel < 0) {
                        hrel = 0.0;
                    }

                    // check limit of soil parameters
                    if (input[i + 3 * nLi] < 0) {
                        input[i + 3 * nLi] = 0.0; // [%]
                    }
                    if (input[i + 3 * nLi] > 1) {
                        input[i + 3 * nLi] = 1.0; // [%]
                    }
                    if (hrel >= 0.0 && hrel < hrelmax && hrelmax > 0) {
                        if (hrel == 0) {
                            ai = 0; // [km2]
                        } else {
                            ai = aH[0][i] * (Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel / hrelmax)
                                    + Area_Relief_Param[2][i] * Math.pow((hrel / hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 3));//km2
                        }
                        if (ai < 0) {
                            ai = 0.;
                        }
                        if (ai > aH[0][i]) {
                            ai = aH[0][i];
                        }
                        ap = aH[0][i] - ai; //in [km2]
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat = (ai * hb[0][i] * 1e3); //in m3
                        Vs_unsat = (Vt[0][i] - Vs_sat); //in meters3
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh <= 0) {
                            da_dh = 100.0;
                        }

                    } //[km]

                    if (hrel >= hrelmax) {
                        dsoil = 0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel = hrelmax;
                        input[i + 2 * nLi] = hrelmax * 1000 + hb[0][i];
                        Vs_sat = Vt[0][i];
                        Vs_unsat = 0;
                        ap = 0;
                        ai = aH[0][i];
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh < 0) {
                            da_dh = 100;
                        }
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)


                    if (hrelmax < 0.01) {
                        ai = aH[0][i];;
                        ap = 0; //in [km2]
                        Vs_sat = 0.0; //in m3
                        Vs_unsat = (Vt[0][i]); //in meters3
                        da_dh = 0.0; //[m2/m]
                        dsoil = 0;
                    }

                    //System.out.println("da_dh   "  + da_dh + "hrelmax" + hrelmax+ "hrel" + hrel);
                    // Hillslope model - estimation of infiltration
                    double RC = 0;
//                    double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p;
                    if (input[i + nLi] > 0.0f) {
                        if (dsoil == 0) {
                            RC = 1.0;
                        } else {
                            RC = input[i + nLi] * (input[i + nLi] + 2 * dsoil) / Math.pow((input[i + nLi] + dsoil), 2);
                        } //[%]

                        if (hrelmax < 0.01) {
                            RC = 1.0;
                        }
                        if (RC > 1) {
                            RC = 1;
                        }
                        if (RC < 0) {
                            RC = 0;
                        }
                        /// this would guarantee it is in mm/h

                        double infmax = 0.0;
                        if (input[i + nLi] > MaxInfRate[0][i]) {
                            infmax = MaxInfRate[0][i];
                        } else {
                            infmax = input[i + nLi];
                        }
                        qp_u = ap * (1 - RC) * infmax / aH[0][i]; //[mm/h]
                        //qp = (ai * p + ap * RC* p)/aH[0][i] ;    /// this would guarantee it is in mm/h
                        //qp_u = ap * (1 - RC) * p/aH[0][i];


                        if (qp_u < 0) {
                            qp_u = 0;
                        }
                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h

                    double Ksat = (HydCond[0][i]);      //m/hour

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])

                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]) * (ap * RC + ai) / aH[0][i]; //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat = Ksat * Math.exp(15 * (input[i + 3 * nLi] - 1));
                    if (Double.isNaN(kunsat)) {
                        kunsat = Ksat;
                    }
                    qu_s = kunsat * input[i + 3 * nLi] * 1000 * (ap / aH[0][i]); //mm/hour

                    //if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* (ai/aH[0][i])* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!
                    if (ai > 0 && hrel > 0 && hrelmax > 0) {
                        //qs_l = Ksat * 1000 * (hrel / hrelmax) * (ai / aH[0][i]);  //mm/hour // correct units!!!!!
                        qs_l = slopesChannel[i] * (Vs_sat / Vt[0][i]) * Ksat * 1000 * (ai / aH[0][i]);  //mm/hour // correct units!!!!!

                    } else {
                        qs_l = 0;
                    }



                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;



                    double av_depth_uns = Vs_unsat * input[i + 3 * nLi] / (aH[0][i] * 1e3);//in mm
                    double av_depth_sat = Vs_sat / (aH[0][i] * 1e3);//in mm
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;
                    double Cs = 0.0;
                    if (EV > 0) {
                        Cs = input[i + nLi] / EV;
                    }
                    double Cunsat = input[i + 3 * nLi] * (av_depth_uns / hb[0][i]) * (ap / aH[0][i]);
                    double Csat = (av_depth_sat / hb[0][i]) * (ai / aH[0][i]);
                    double SC = Cs + Cunsat + Csat;
                    double Corr = 1;
                    if (SC > 1) {
                        Corr = 1 / SC;
                    }
                    ev_surf = Corr * Cs * EV;
                    ev_u = Corr * Cunsat * EV;
                    ev_s = Corr * Csat * EV;

                    //if(i==1)  {System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);
                    //System.out.println("input[i+nLi]" + input[i+nLi] + "  ev_surf" + ev_surf + "  av_depth_uns"+av_depth_uns+ "  ev_u" + ev_u + "  av_depth_sat"+av_depth_sat+ "  ev_s" + ev_s);}


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * (aH[0][i] * (qp_l + qs_l))) + Q_trib - input[i]); //[m3/s]/min
                    if (Double.isNaN(output[i])) {
                        output[i] = 0.0;
                    }
                    /*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u - ev_surf); //output[mm/h/min]
                    if (Double.isNaN(output[i + nLi])) {
                        output[i + nLi] = 0.0;
                    }
                    /* depth - saturated area*/
                    double dVs_sat_dt = (1e3) * aH[0][i] * (qu_s - qs_l - ev_s); //m3/h

                    if (da_dh > 0 && hb[0][i] > 0) {
                        output[i + 2 * nLi] = (1 / 60.) * 1e6 * (dVs_sat_dt / (hb[0][i] * da_dh)); //output[mm/min] effPrecip and qss [mm/hour]
                    } else {
                        output[i + 2 * nLi] = 0;
                    }
                    if (Double.isNaN(output[i + 2 * nLi])) {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Vs_unsat != 0) /* soil moisture*/ {
                        output[i + 3 * nLi] = (1 / 60.) * (1 / Vs_unsat) * (1e3 * aH[0][i] * (qp_u - qu_s - ev_u) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                    } else {
                        output[i + 3 * nLi] = 0;
                    }

                    if (Double.isNaN(output[i + 3 * nLi])) {
                        output[i + 3 * nLi] = 0.0;
                    }
                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }


                break;
            case 136:
                /* Same as Hill 6 with evaporation
                 * qs_l = Ksat * 1000 * (ai / aH[0][i])* (hrel / hrelmax); - baseflow does not depend on head of water
                 */
//System.out.println(basinHillSlopesInfo.HillManning(0) + "Slope"+ slopesLand[0][0] + "Relief "+basinHillSlopesInfo.HillRelief(0));
// System.out.println(slopesChannel[0] + "land" +slopesLand[0][0] +"land2 " + slopesLand[0][0]);

//System.exit(1);
                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    //double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    double EV = EVcoef * basinHillSlopesInfo.PotEVPT(i, time);
                    // HILLSLOPE GEOMETRY RELATIONSHIPS
                    //System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);

                    double Ia = lambdaSCS * hb[0][i]; //[mm]
                    double ai = 0.0; //[km2]
                    double ap = 0; //[km2]
                    double dsoil = 0; //[mm]
                    double Vs_sat = 0; //[m3]
                    double Vs_unsat = 0; //[m3]
                    double da_dh = 100.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i]) / 1000; // [m]
                    double hrelmax = (Ht[0][i] - hb[0][i]) / 1000; // [m]
                    if (hrel < 0) {
                        hrel = 0.0;
                    }

                    // check limit of soil parameters
                    if (input[i + 3 * nLi] < 0) {
                        input[i + 3 * nLi] = 0.0; // [%]
                    }
                    if (input[i + 3 * nLi] > 1) {
                        input[i + 3 * nLi] = 1.0; // [%]
                    }
                    if (hrel >= 0.0 && hrel < hrelmax && hrelmax > 0) {
                        if (hrel == 0) {
                            ai = 0; // [km2]
                        } else {
                            ai = aH[0][i] * (Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel / hrelmax)
                                    + Area_Relief_Param[2][i] * Math.pow((hrel / hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 3));//km2
                        }
                        if (ai < 0) {
                            ai = 0.;
                        }
                        if (ai > aH[0][i]) {
                            ai = aH[0][i];
                        }
                        ap = aH[0][i] - ai; //in [km2]
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat = (ai * hb[0][i] * 1e3); //in m3
                        Vs_unsat = (Vt[0][i] - Vs_sat); //in meters3
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh <= 0) {
                            da_dh = 100.0;
                        }

                    } //[km]

                    if (hrel >= hrelmax) {
                        dsoil = 0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel = hrelmax;
                        input[i + 2 * nLi] = hrelmax * 1000 + hb[0][i];
                        Vs_sat = Vt[0][i];
                        Vs_unsat = 0;
                        ap = 0;
                        ai = aH[0][i];
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh < 0) {
                            da_dh = 100;
                        }
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)


                    if (hrelmax < 0.01) {
                        ai = aH[0][i];;
                        ap = 0; //in [km2]
                        Vs_sat = 0.0; //in m3
                        Vs_unsat = (Vt[0][i]); //in meters3
                        da_dh = 0.0; //[m2/m]
                        dsoil = 0;
                    }

                    //System.out.println("da_dh   "  + da_dh + "hrelmax" + hrelmax+ "hrel" + hrel);
                    // Hillslope model - estimation of infiltration
                    double RC = 0;
//                    double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p;
                    if (input[i + nLi] > 0.0f) {
                        if (dsoil == 0) {
                            RC = 1.0;
                        } else {
                            RC = input[i + nLi] * (input[i + nLi] + 2 * dsoil) / Math.pow((input[i + nLi] + dsoil), 2);
                        } //[%]

                        if (hrelmax < 0.01) {
                            RC = 1.0;
                        }
                        if (RC > 1) {
                            RC = 1;
                        }
                        if (RC < 0) {
                            RC = 0;
                        }
                        /// this would guarantee it is in mm/h

                        double infmax = 0.0;
                        if (input[i + nLi] > MaxInfRate[0][i]) {
                            infmax = MaxInfRate[0][i];
                        } else {
                            infmax = input[i + nLi];
                        }
                        qp_u = ap * (1 - RC) * infmax / aH[0][i]; //[mm/h]
                        //qp = (ai * p + ap * RC* p)/aH[0][i] ;    /// this would guarantee it is in mm/h
                        //qp_u = ap * (1 - RC) * p/aH[0][i];


                        if (qp_u < 0) {
                            qp_u = 0;
                        }
                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h

                    double Ksat = (HydCond[0][i]);      //m/hour

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])

                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]) * (ap * RC + ai) / aH[0][i]; //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat = Ksat * Math.exp(15 * (input[i + 3 * nLi] - 1));
                    if (Double.isNaN(kunsat)) {
                        kunsat = Ksat;
                    }
                    qu_s = kunsat * input[i + 3 * nLi] * 1000 * (ap / aH[0][i]); //mm/hour

                    //if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* (ai/aH[0][i])* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!
                    if (ai > 0 && hrel > 0 && hrelmax > 0) {
                        //qs_l = Ksat * 1000 * (hrel / hrelmax) * (ai / aH[0][i]);  //mm/hour // correct units!!!!!
                        qs_l = slopesChannel[i] * input[i + 3 * nLi] * (Vs_sat / Vt[0][i]) * Ksat * 1000 * (ai / aH[0][i]);  //mm/hour // correct units!!!!!

                    } else {
                        qs_l = 0;
                    }



                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;



                    double av_depth_uns = Vs_unsat * input[i + 3 * nLi] / (aH[0][i] * 1e3);//in mm
                    double av_depth_sat = Vs_sat / (aH[0][i] * 1e3);//in mm
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;
                    double Cs = 0.0;
                    if (EV > 0) {
                        Cs = input[i + nLi] / EV;
                    }
                    double Cunsat = input[i + 3 * nLi] * (av_depth_uns / hb[0][i]) * (ap / aH[0][i]);
                    double Csat = (av_depth_sat / hb[0][i]) * (ai / aH[0][i]);
                    double SC = Cs + Cunsat + Csat;
                    double Corr = 1;
                    if (SC > 1) {
                        Corr = 1 / SC;
                    }
                    ev_surf = Corr * Cs * EV;
                    ev_u = Corr * Cunsat * EV;
                    ev_s = Corr * Csat * EV;

                    //if(i==1)  {System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);
                    //System.out.println("input[i+nLi]" + input[i+nLi] + "  ev_surf" + ev_surf + "  av_depth_uns"+av_depth_uns+ "  ev_u" + ev_u + "  av_depth_sat"+av_depth_sat+ "  ev_s" + ev_s);}


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * (aH[0][i] * (qp_l + qs_l))) + Q_trib - input[i]); //[m3/s]/min
                    if (Double.isNaN(output[i])) {
                        output[i] = 0.0;
                    }
                    /*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u - ev_surf); //output[mm/h/min]
                    if (Double.isNaN(output[i + nLi])) {
                        output[i + nLi] = 0.0;
                    }
                    /* depth - saturated area*/
                    double dVs_sat_dt = (1e3) * aH[0][i] * (qu_s - qs_l - ev_s); //m3/h

                    if (da_dh > 0 && hb[0][i] > 0) {
                        output[i + 2 * nLi] = (1 / 60.) * 1e6 * (dVs_sat_dt / (hb[0][i] * da_dh)); //output[mm/min] effPrecip and qss [mm/hour]
                    } else {
                        output[i + 2 * nLi] = 0;
                    }
                    if (Double.isNaN(output[i + 2 * nLi])) {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Vs_unsat != 0) /* soil moisture*/ {
                        output[i + 3 * nLi] = (1 / 60.) * (1 / Vs_unsat) * (1e3 * aH[0][i] * (qp_u - qu_s - ev_u) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                    } else {
                        output[i + 3 * nLi] = 0;
                    }

                    if (Double.isNaN(output[i + 3 * nLi])) {
                        output[i + 3 * nLi] = 0.0;
                    }
                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }


                break;
            case 135:
                /* Same as Hill 6 with evaporation
                 * qs_l = Ksat * 1000 * (ai / aH[0][i])* (hrel / hrelmax); - baseflow does not depend on head of water
                 */

                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    //double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    double EV = EVcoef * basinHillSlopesInfo.PotEVPT(i, time);
                    // HILLSLOPE GEOMETRY RELATIONSHIPS
                    //System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);

                    double Ia = lambdaSCS * hb[0][i]; //[mm]
                    double ai = 0.0; //[km2]
                    double ap = 0; //[km2]
                    double dsoil = 0; //[mm]
                    double Vs_sat = 0; //[m3]
                    double Vs_unsat = 0; //[m3]
                    double da_dh = 100.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i]) / 1000; // [m]
                    double hrelmax = (Ht[0][i] - hb[0][i]) / 1000; // [m]
                    if (hrel < 0) {
                        hrel = 0.0;
                    }

                    // check limit of soil parameters
                    if (input[i + 3 * nLi] < 0) {
                        input[i + 3 * nLi] = 0.0; // [%]
                    }
                    if (input[i + 3 * nLi] > 1) {
                        input[i + 3 * nLi] = 1.0; // [%]
                    }
                    if (hrel >= 0.0 && hrel < hrelmax && hrelmax > 0) {
                        if (hrel == 0) {
                            ai = 0; // [km2]
                        } else {
                            ai = aH[0][i] * (Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel / hrelmax)
                                    + Area_Relief_Param[2][i] * Math.pow((hrel / hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 3));//km2
                        }
                        if (ai < 0) {
                            ai = 0.;
                        }
                        if (ai > aH[0][i]) {
                            ai = aH[0][i];
                        }
                        ap = aH[0][i] - ai; //in [km2]
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat = (ai * hb[0][i] * 1e3); //in m3
                        Vs_unsat = (Vt[0][i] - Vs_sat); //in meters3
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh <= 0) {
                            da_dh = 100.0;
                        }

                    } //[km]

                    if (hrel >= hrelmax) {
                        dsoil = 0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel = hrelmax;
                        input[i + 2 * nLi] = hrelmax * 1000 + hb[0][i];
                        Vs_sat = Vt[0][i];
                        Vs_unsat = 0;
                        ap = 0;
                        ai = aH[0][i];
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh < 0) {
                            da_dh = 100;
                        }
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)


                    if (hrelmax < 0.01) {
                        ai = aH[0][i];;
                        ap = 0; //in [km2]
                        Vs_sat = 0.0; //in m3
                        Vs_unsat = (Vt[0][i]); //in meters3
                        da_dh = 0.0; //[m2/m]
                        dsoil = 0;
                    }

                    //System.out.println("da_dh   "  + da_dh + "hrelmax" + hrelmax+ "hrel" + hrel);
                    // Hillslope model - estimation of infiltration
                    double RC = 0;
//                    double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p;
                    if (input[i + nLi] > 0.0f) {
                        if (dsoil == 0) {
                            RC = 1.0;
                        } else {
                            RC = input[i + nLi] * (input[i + nLi] + 2 * dsoil) / Math.pow((input[i + nLi] + dsoil), 2);
                        } //[%]

                        if (hrelmax < 0.01) {
                            RC = 1.0;
                        }
                        if (RC > 1) {
                            RC = 1;
                        }
                        if (RC < 0) {
                            RC = 0;
                        }
                        /// this would guarantee it is in mm/h

                        double infmax = 0.0;
                        //MaxInfRate[0][i]=0.01f;
                        if (input[i + nLi] > MaxInfRate[0][i]) {
                            infmax = MaxInfRate[0][i];
                        } else {
                            infmax = input[i + nLi];
                        }
                        qp_u = (1 - input[i + 3 * nLi]) * (1 - RC) * infmax;

                        //qp = (ai * p + ap * RC* p)/aH[0][i] ;    /// this would guarantee it is in mm/h
                        //qp_u = ap * (1 - RC) * p/aH[0][i];


                        if (qp_u < 0) {
                            qp_u = 0;
                        }
                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h

                    double Ksat = (HydCond[0][i]);      //m/hour

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])

                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat = Ksat * Math.exp(10 * (input[i + 3 * nLi] - 1));
                    if (Double.isNaN(kunsat)) {
                        kunsat = Ksat;
                    }
                    qu_s = kunsat * input[i + 3 * nLi] * 1000; //mm/hour

                    //if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* (ai/aH[0][i])* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!
                    if (ai > 0 && hrel > 0 && hrelmax > 0) {
                        //qs_l = Ksat * 1000 * (hrel / hrelmax) * (ai / aH[0][i]);  //mm/hour // correct units!!!!!
                        qs_l = input[i + 3 * nLi] * Ksat * 1000 * (ai / aH[0][i]) * (hrel / hrelmax);  //mm/hour // correct units!!!!!

                    } else {
                        qs_l = 0;
                    }



                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;



                    double av_depth_uns = Vs_unsat * input[i + 3 * nLi] / (aH[0][i] * 1e3);//in mm
                    double av_depth_sat = Vs_sat / (aH[0][i] * 1e3);//in mm
                    if (p <= 0.05) {
                        if (input[i + nLi] == 0) {
                            ev_surf = 0f;
                        } else if (input[i + nLi] > 0 && input[i + nLi] <= EV) {
                            ev_surf = input[i + nLi];
                        } else {
                            ev_surf = EV;
                        }
                        if (EV - ev_surf > 0) {

                            if (av_depth_uns == 0) {
                                ev_u = 0.;
                            } else {
                                ev_u = (EV - ev_surf) * input[i + 3 * nLi];
                            }
                            if (ev_surf + ev_u > EV) {
                                ev_u = EV - ev_surf;
                            }
                        }

                        if (EV - ev_surf - ev_u > 0) {

                            if (av_depth_sat == 0) {
                                ev_s = 0.;
                            } else {
                                ev_s = (EV - ev_surf - ev_u) * (av_depth_sat / hb[0][i]);
                            }
                            if (ev_surf + ev_u + ev_s > EV) {
                                ev_s = EV - ev_surf - ev_u;
                            }
                        }
                    } else {
                        ev_s = 0.0;
                        ev_u = 0.0;
                        ev_surf = 0.0;
                    }

                    //if(i==1)  {System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);
                    //System.out.println("input[i+nLi]" + input[i+nLi] + "  ev_surf" + ev_surf + "  av_depth_uns"+av_depth_uns+ "  ev_u" + ev_u + "  av_depth_sat"+av_depth_sat+ "  ev_s" + ev_s);}


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * qp_l) + (ai * qs_l))) + Q_trib - input[i]); //[m3/s]/min
                    if (Double.isNaN(output[i])) {
                        output[i] = 0.0;
                    }
                    /*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u - ev_surf); //output[mm/h/min]
                    if (Double.isNaN(output[i + nLi])) {
                        output[i + nLi] = 0.0;
                    }
                    /* depth - saturated area*/
                    double dVs_sat_dt = (1e3) * (ap * qu_s - ai * qs_l - aH[0][i] * ev_s); //m3/h

                    if (da_dh > 0 && hb[0][i] > 0) {
                        output[i + 2 * nLi] = (1 / 60.) * 1e6 * (dVs_sat_dt / (hb[0][i] * da_dh)); //output[mm/min] effPrecip and qss [mm/hour]
                    } else {
                        output[i + 2 * nLi] = 0;
                    }
                    if (Double.isNaN(output[i + 2 * nLi])) {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Vs_unsat != 0) /* soil moisture*/ {
                        output[i + 3 * nLi] = (1 / 60.) * (1 / Vs_unsat) * (1e3 * (aH[0][i] * qp_u - (ap) * qu_s - aH[0][i] * ev_u) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                    } else {
                        output[i + 3 * nLi] = 0;
                    }

                    if (Double.isNaN(output[i + 3 * nLi])) {
                        output[i + 3 * nLi] = 0.0;
                    }
                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }


                break;

            case 137:
                /* Same as Hill 6 with evaporation
                 * qs_l = Ksat * 1000 * (ai / aH[0][i])* (hrel / hrelmax); - baseflow does not depend on head of water
                 */

                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    //double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    double EV = EVcoef * basinHillSlopesInfo.PotEVPT(i, time);
                    // HILLSLOPE GEOMETRY RELATIONSHIPS
                    //System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);

                    double Ia = lambdaSCS * hb[0][i]; //[mm]
                    double ai = 0.0; //[km2]
                    double ap = 0; //[km2]
                    double dsoil = 0; //[mm]
                    double Vs_sat = 0; //[m3]
                    double Vs_unsat = 0; //[m3]
                    double da_dh = 100.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i]) / 1000; // [m]
                    double hrelmax = (Ht[0][i] - hb[0][i]) / 1000; // [m]
                    if (hrel < 0) {
                        hrel = 0.0;
                    }

                    // check limit of soil parameters
                    if (input[i + 3 * nLi] < 0) {
                        input[i + 3 * nLi] = 0.0; // [%]
                    }
                    if (input[i + 3 * nLi] > 1) {
                        input[i + 3 * nLi] = 1.0; // [%]
                    }
                    if (hrel >= 0.0 && hrel < hrelmax && hrelmax > 0) {
                        if (hrel == 0) {
                            ai = 0; // [km2]
                        } else {
                            ai = aH[0][i] * (Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel / hrelmax)
                                    + Area_Relief_Param[2][i] * Math.pow((hrel / hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 3));//km2
                        }
                        if (ai < 0) {
                            ai = 0.;
                        }
                        if (ai > aH[0][i]) {
                            ai = aH[0][i];
                        }
                        ap = aH[0][i] - ai; //in [km2]
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat = (ai * hb[0][i] * 1e3); //in m3
                        Vs_unsat = (Vt[0][i] - Vs_sat); //in meters3
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh <= 0) {
                            da_dh = 100.0;
                        }

                    } //[km]

                    if (hrel >= hrelmax) {
                        dsoil = 0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel = hrelmax;
                        input[i + 2 * nLi] = hrelmax * 1000 + hb[0][i];
                        Vs_sat = Vt[0][i];
                        Vs_unsat = 0;
                        ap = 0;
                        ai = aH[0][i];
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh < 0) {
                            da_dh = 100;
                        }
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)


                    if (hrelmax < 0.01) {
                        ai = aH[0][i];;
                        ap = 0; //in [km2]
                        Vs_sat = 0.0; //in m3
                        Vs_unsat = (Vt[0][i]); //in meters3
                        da_dh = 0.0; //[m2/m]
                        dsoil = 0;
                    }

                    //System.out.println("da_dh   "  + da_dh + "hrelmax" + hrelmax+ "hrel" + hrel);
                    // Hillslope model - estimation of infiltration
                    double RC = 0;
//                    double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p;
                    if (input[i + nLi] > 0.0f) {
                        if (dsoil == 0) {
                            RC = 1.0;
                        } else {
                            RC = input[i + nLi] * (input[i + nLi] + 2 * dsoil) / Math.pow((input[i + nLi] + dsoil), 2);
                        } //[%]

                        if (hrelmax < 0.01) {
                            RC = 1.0;
                        }
                        if (RC > 1) {
                            RC = 1;
                        }
                        if (RC < 0) {
                            RC = 0;
                        }
                        /// this would guarantee it is in mm/h

                        double infmax = 0.0;
                        //MaxInfRate[0][i]=0.01f;
                        if (input[i + nLi] > MaxInfRate[0][i]) {
                            infmax = MaxInfRate[0][i];
                        } else {
                            infmax = input[i + nLi];
                        }
                        qp_u = (1 - RC) * infmax;

                        //qp = (ai * p + ap * RC* p)/aH[0][i] ;    /// this would guarantee it is in mm/h
                        //qp_u = ap * (1 - RC) * p/aH[0][i];


                        if (qp_u < 0) {
                            qp_u = 0;
                        }
                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h

                    double Ksat = (HydCond[0][i]);      //m/hour

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])

                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat = Ksat * Math.exp(10 * (input[i + 3 * nLi] - 1));
                    if (Double.isNaN(kunsat)) {
                        kunsat = Ksat;
                    }
                    qu_s = kunsat * input[i + 3 * nLi] * 1000; //mm/hour

                    //if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* (ai/aH[0][i])* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!
                    if (ai > 0 && hrel > 0 && hrelmax > 0) {
                        //qs_l = Ksat * 1000 * (hrel / hrelmax) * (ai / aH[0][i]);  //mm/hour // correct units!!!!!
                        qs_l = Ksat * 1000 * (ai / aH[0][i]) * (hrel / hrelmax);  //mm/hour // correct units!!!!!

                    } else {
                        qs_l = 0;
                    }



                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;



                    double av_depth_uns = Vs_unsat * input[i + 3 * nLi] / (aH[0][i] * 1e3);//in mm
                    double av_depth_sat = Vs_sat / (aH[0][i] * 1e3);//in mm

                    if (input[i + nLi] == 0) {
                        ev_surf = 0f;

                    } else if (input[i + nLi] > 0 && input[i + nLi] <= EV) {
                        ev_surf = input[i + nLi];
                    } else {
                        ev_surf = EV;
                    }
                    if (EV - ev_surf > 0) {

                        if (av_depth_uns == 0) {
                            ev_u = 0.;
                        } else {
                            ev_u = (EV - ev_surf) * input[i + 3 * nLi];
                        }
                        if (ev_surf + ev_u > EV) {
                            ev_u = EV - ev_surf;
                        }
                    }

                    if (EV - ev_surf - ev_u > 0) {

                        if (av_depth_sat == 0) {
                            ev_s = 0.;
                        } else {
                            ev_s = (EV - ev_surf - ev_u) * (av_depth_sat / hb[0][i]);
                        }
                        if (ev_surf + ev_u + ev_s > EV) {
                            ev_s = EV - ev_surf - ev_u;
                        }

                    }


                    //if(i==1)  {System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);
                    //System.out.println("input[i+nLi]" + input[i+nLi] + "  ev_surf" + ev_surf + "  av_depth_uns"+av_depth_uns+ "  ev_u" + ev_u + "  av_depth_sat"+av_depth_sat+ "  ev_s" + ev_s);}


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * qp_l) + (ai * qs_l))) + Q_trib - input[i]); //[m3/s]/min
                    if (Double.isNaN(output[i])) {
                        output[i] = 0.0;
                    }
                    /*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u - ev_surf); //output[mm/h/min]
                    if (Double.isNaN(output[i + nLi])) {
                        output[i + nLi] = 0.0;
                    }
                    /* depth - saturated area*/
                    double dVs_sat_dt = (1e3) * (ap * qu_s - ai * qs_l - aH[0][i] * ev_s); //m3/h

                    if (da_dh > 0 && hb[0][i] > 0) {
                        output[i + 2 * nLi] = (1 / 60.) * 1e6 * (dVs_sat_dt / (hb[0][i] * da_dh)); //output[mm/min] effPrecip and qss [mm/hour]
                    } else {
                        output[i + 2 * nLi] = 0;
                    }
                    if (Double.isNaN(output[i + 2 * nLi])) {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Vs_unsat != 0) /* soil moisture*/ {
                        output[i + 3 * nLi] = (1 / 60.) * (1 / Vs_unsat) * (1e3 * (aH[0][i] * qp_u - (ap) * qu_s - aH[0][i] * ev_u) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                    } else {
                        output[i + 3 * nLi] = 0;
                    }

                    if (Double.isNaN(output[i + 3 * nLi])) {
                        output[i + 3 * nLi] = 0.0;
                    }
                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }


                break;

            case 138:
                /* Same as Hill 6 with evaporation
                 * qs_l = Ksat * 1000 * (ai / aH[0][i])* (hrel / hrelmax); - baseflow does not depend on head of water
                 */

                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    //double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    double EV = EVcoef * basinHillSlopesInfo.PotEVPT(i, time);
                    // HILLSLOPE GEOMETRY RELATIONSHIPS
                    //System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);

                    double Ia = lambdaSCS * hb[0][i]; //[mm]
                    double ai = 0.0; //[km2]
                    double ap = 0; //[km2]
                    double dsoil = 0; //[mm]
                    double Vs_sat = 0; //[m3]
                    double Vs_unsat = 0; //[m3]
                    double da_dh = 100.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i]) / 1000; // [m]
                    double hrelmax = (Ht[0][i] - hb[0][i]) / 1000; // [m]
                    if (hrel < 0) {
                        hrel = 0.0;
                    }

                    // check limit of soil parameters
                    if (input[i + 3 * nLi] < 0) {
                        input[i + 3 * nLi] = 0.0; // [%]
                    }
                    if (input[i + 3 * nLi] > 1) {
                        input[i + 3 * nLi] = 1.0; // [%]
                    }
                    if (hrel >= 0.0 && hrel < hrelmax && hrelmax > 0) {
                        if (hrel == 0) {
                            ai = 0; // [km2]
                        } else {
                            ai = aH[0][i] * (Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel / hrelmax)
                                    + Area_Relief_Param[2][i] * Math.pow((hrel / hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 3));//km2
                        }
                        if (ai < 0) {
                            ai = 0.;
                        }
                        if (ai > aH[0][i]) {
                            ai = aH[0][i];
                        }
                        ap = aH[0][i] - ai; //in [km2]
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat = (ai * hb[0][i] * 1e3); //in m3
                        Vs_unsat = (Vt[0][i] - Vs_sat); //in meters3
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh <= 0) {
                            da_dh = 100.0;
                        }

                    } //[km]

                    if (hrel >= hrelmax) {
                        dsoil = 0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel = hrelmax;
                        input[i + 2 * nLi] = hrelmax * 1000 + hb[0][i];
                        Vs_sat = Vt[0][i];
                        Vs_unsat = 0;
                        ap = 0;
                        ai = aH[0][i];
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh < 0) {
                            da_dh = 100;
                        }
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)


                    if (hrelmax < 0.01) {
                        ai = aH[0][i];;
                        ap = 0; //in [km2]
                        Vs_sat = 0.0; //in m3
                        Vs_unsat = (Vt[0][i]); //in meters3
                        da_dh = 0.0; //[m2/m]
                        dsoil = 0;
                    }

                    //System.out.println("da_dh   "  + da_dh + "hrelmax" + hrelmax+ "hrel" + hrel);
                    // Hillslope model - estimation of infiltration
                    double RC = 0;
//                    double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p;
                    if (input[i + nLi] > 0.0f) {
                        if (dsoil == 0) {
                            RC = 1.0;
                        } else {
                            RC = input[i + nLi] * (input[i + nLi] + 2 * dsoil) / Math.pow((input[i + nLi] + dsoil), 2);
                        } //[%]

                        if (hrelmax < 0.01) {
                            RC = 1.0;
                        }
                        if (RC > 1) {
                            RC = 1;
                        }
                        if (RC < 0) {
                            RC = 0;
                        }
                        /// this would guarantee it is in mm/h

                        double infmax = 0.0;
                        //MaxInfRate[0][i]=0.01f;
                        if (input[i + nLi] > MaxInfRate[0][i]) {
                            infmax = MaxInfRate[0][i];
                        } else {
                            infmax = input[i + nLi];
                        }
                        qp_u = (1 - RC) * infmax;

                        //qp = (ai * p + ap * RC* p)/aH[0][i] ;    /// this would guarantee it is in mm/h
                        //qp_u = ap * (1 - RC) * p/aH[0][i];


                        if (qp_u < 0) {
                            qp_u = 0;
                        }
                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h

                    double Ksat = (HydCond[0][i]);      //m/hour

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])

                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat = Ksat * Math.exp(10 * (input[i + 3 * nLi] - 1));
                    if (Double.isNaN(kunsat)) {
                        kunsat = Ksat;
                    }
                    qu_s = kunsat * input[i + 3 * nLi] * 1000; //mm/hour

                    //if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* (ai/aH[0][i])* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!
                    if (ai > 0 && hrel > 0 && hrelmax > 0) {
                        //qs_l = Ksat * 1000 * (hrel / hrelmax) * (ai / aH[0][i]);  //mm/hour // correct units!!!!!
                        qs_l = Ksat * 1000 * (ai / aH[0][i]) * (hrel / hrelmax);  //mm/hour // correct units!!!!!

                    } else {
                        qs_l = 0;
                    }



                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;



                    double av_depth_uns = Vs_unsat * input[i + 3 * nLi] / (aH[0][i] * 1e3);//in mm
                    double av_depth_sat = Vs_sat / (aH[0][i] * 1e3);//in mm

                    if (input[i + nLi] == 0) {
                        ev_surf = 0f;

                    } else if (input[i + nLi] > 0 && input[i + nLi] <= EV) {
                        ev_surf = input[i + nLi];
                    } else {
                        ev_surf = EV;
                    }
                    if (EV - ev_surf > 0) {

                        if (av_depth_uns == 0) {
                            ev_u = 0.;
                        } else {
                            ev_u = (EV - ev_surf) * input[i + 3 * nLi];
                        }
                        if (ev_surf + ev_u > EV) {
                            ev_u = EV - ev_surf;
                        }
                    }

                    if (EV - ev_surf - ev_u > 0) {

                        if (av_depth_sat == 0) {
                            ev_s = 0.;
                        } else {
                            ev_s = (EV - ev_surf - ev_u) * (av_depth_sat / hb[0][i]);
                        }
                        if (ev_surf + ev_u + ev_s > EV) {
                            ev_s = EV - ev_surf - ev_u;
                        }

                    }


                    //if(i==1)  {System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);
                    //System.out.println("input[i+nLi]" + input[i+nLi] + "  ev_surf" + ev_surf + "  av_depth_uns"+av_depth_uns+ "  ev_u" + ev_u + "  av_depth_sat"+av_depth_sat+ "  ev_s" + ev_s);}


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * qp_l) + (ai * qs_l))) + Q_trib - input[i]); //[m3/s]/min
                    if (Double.isNaN(output[i])) {
                        output[i] = 0.0;
                    }
                    /*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u - ev_surf); //output[mm/h/min]
                    if (Double.isNaN(output[i + nLi])) {
                        output[i + nLi] = 0.0;
                    }
                    /* depth - saturated area*/
                    double dVs_sat_dt = (1e3) * (ap * qu_s - ai * qs_l - aH[0][i] * ev_s); //m3/h

                    if (da_dh > 0 && hb[0][i] > 0) {
                        output[i + 2 * nLi] = (1 / 0.1) * (1 / 60.) * 1e6 * (dVs_sat_dt / (hb[0][i] * da_dh)); //output[mm/min] effPrecip and qss [mm/hour]
                    } else {
                        output[i + 2 * nLi] = 0;
                    }
                    if (Double.isNaN(output[i + 2 * nLi])) {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Vs_unsat != 0) /* soil moisture*/ {
                        output[i + 3 * nLi] = (1 / 0.1) * (1 / 60.) * (1 / Vs_unsat) * (1e3 * (aH[0][i] * qp_u - (ap) * qu_s - aH[0][i] * ev_u) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                    } else {
                        output[i + 3 * nLi] = 0;
                    }

                    if (Double.isNaN(output[i + 3 * nLi])) {
                        output[i + 3 * nLi] = 0.0;
                    }
                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }


                break;

            case 139:
                /* Same as Hill 6 with evaporation
                 * qs_l = Ksat * 1000 * (ai / aH[0][i])* (hrel / hrelmax); - baseflow does not depend on head of water
                 */

                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    //double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    double EV = EVcoef * basinHillSlopesInfo.PotEVPT(i, time);
                    // HILLSLOPE GEOMETRY RELATIONSHIPS
                    //System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);

                    double Ia = lambdaSCS * hb[0][i]; //[mm]
                    double ai = 0.0; //[km2]
                    double ap = 0; //[km2]
                    double dsoil = 0; //[mm]
                    double Vs_sat = 0; //[m3]
                    double Vs_unsat = 0; //[m3]
                    double da_dh = 100.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i]) / 1000; // [m]
                    double hrelmax = (Ht[0][i] - hb[0][i]) / 1000; // [m]
                    if (hrel < 0) {
                        hrel = 0.0;
                    }

                    // check limit of soil parameters
                    if (input[i + 3 * nLi] < 0) {
                        input[i + 3 * nLi] = 0.0; // [%]
                    }
                    if (input[i + 3 * nLi] > 1) {
                        input[i + 3 * nLi] = 1.0; // [%]
                    }
                    if (hrel >= 0.0 && hrel < hrelmax && hrelmax > 0) {
                        if (hrel == 0) {
                            ai = 0; // [km2]
                        } else {
                            ai = aH[0][i] * (Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel / hrelmax)
                                    + Area_Relief_Param[2][i] * Math.pow((hrel / hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 3));//km2
                        }
                        if (ai < 0) {
                            ai = 0.;
                        }
                        if (ai > aH[0][i]) {
                            ai = aH[0][i];
                        }
                        ap = aH[0][i] - ai; //in [km2]
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat = (ai * hb[0][i] * 1e3); //in m3
                        Vs_unsat = (Vt[0][i] - Vs_sat); //in meters3
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh <= 0) {
                            da_dh = 100.0;
                        }

                    } //[km]

                    if (hrel >= hrelmax) {
                        dsoil = 0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel = hrelmax;
                        input[i + 2 * nLi] = hrelmax * 1000 + hb[0][i];
                        Vs_sat = Vt[0][i];
                        Vs_unsat = 0;
                        ap = 0;
                        ai = aH[0][i];
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh < 0) {
                            da_dh = 100;
                        }
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)


                    if (hrelmax < 0.01) {
                        ai = aH[0][i];;
                        ap = 0; //in [km2]
                        Vs_sat = 0.0; //in m3
                        Vs_unsat = (Vt[0][i]); //in meters3
                        da_dh = 0.0; //[m2/m]
                        dsoil = 0;
                    }

                    //System.out.println("da_dh   "  + da_dh + "hrelmax" + hrelmax+ "hrel" + hrel);
                    // Hillslope model - estimation of infiltration
                    double RC = 0;
//                    double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p;
                    if (input[i + nLi] > 0.0f) {
                        if (dsoil == 0) {
                            RC = 1.0;
                        } else {
                            RC = input[i + nLi] * (input[i + nLi] + 2 * dsoil) / Math.pow((input[i + nLi] + dsoil), 2);
                        } //[%]

                        if (hrelmax < 0.01) {
                            RC = 1.0;
                        }
                        if (RC > 1) {
                            RC = 1;
                        }
                        if (RC < 0) {
                            RC = 0;
                        }
                        /// this would guarantee it is in mm/h

                        double infmax = 0.0;
                        //MaxInfRate[0][i]=0.01f;
                        if (input[i + nLi] > MaxInfRate[0][i]) {
                            infmax = MaxInfRate[0][i];
                        } else {
                            infmax = input[i + nLi];
                        }
                        qp_u = (1 - RC) * infmax;

                        //qp = (ai * p + ap * RC* p)/aH[0][i] ;    /// this would guarantee it is in mm/h
                        //qp_u = ap * (1 - RC) * p/aH[0][i];


                        if (qp_u < 0) {
                            qp_u = 0;
                        }
                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h

                    double Ksat = (HydCond[0][i]);      //m/hour

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])

                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat = Ksat * Math.exp(10 * (input[i + 3 * nLi] - 1));
                    if (Double.isNaN(kunsat)) {
                        kunsat = Ksat;
                    }
                    qu_s = Ksat * 1000; //mm/hour

                    //if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* (ai/aH[0][i])* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!
                    if (ai > 0 && hrel > 0 && hrelmax > 0) {
                        //qs_l = Ksat * 1000 * (hrel / hrelmax) * (ai / aH[0][i]);  //mm/hour // correct units!!!!!
                        qs_l = Ksat * 1000 * (ai / aH[0][i]) * (hrel / hrelmax);  //mm/hour // correct units!!!!!

                    } else {
                        qs_l = 0;
                    }



                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;



                    double av_depth_uns = Vs_unsat * input[i + 3 * nLi] / (aH[0][i] * 1e3);//in mm
                    double av_depth_sat = Vs_sat / (aH[0][i] * 1e3);//in mm

                    if (input[i + nLi] == 0) {
                        ev_surf = 0f;

                    } else if (input[i + nLi] > 0 && input[i + nLi] <= EV) {
                        ev_surf = input[i + nLi];
                    } else {
                        ev_surf = EV;
                    }
                    if (EV - ev_surf > 0) {

                        if (av_depth_uns == 0) {
                            ev_u = 0.;
                        } else {
                            ev_u = (EV - ev_surf) * input[i + 3 * nLi];
                        }
                        if (ev_surf + ev_u > EV) {
                            ev_u = EV - ev_surf;
                        }
                    }

                    if (EV - ev_surf - ev_u > 0) {

                        if (av_depth_sat == 0) {
                            ev_s = 0.;
                        } else {
                            ev_s = (EV - ev_surf - ev_u) * (av_depth_sat / hb[0][i]);
                        }
                        if (ev_surf + ev_u + ev_s > EV) {
                            ev_s = EV - ev_surf - ev_u;
                        }

                    }


                    //if(i==1)  {System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);
                    //System.out.println("input[i+nLi]" + input[i+nLi] + "  ev_surf" + ev_surf + "  av_depth_uns"+av_depth_uns+ "  ev_u" + ev_u + "  av_depth_sat"+av_depth_sat+ "  ev_s" + ev_s);}


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * qp_l) + (ai * qs_l))) + Q_trib - input[i]); //[m3/s]/min
                    if (Double.isNaN(output[i])) {
                        output[i] = 0.0;
                    }
                    /*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u - ev_surf); //output[mm/h/min]
                    if (Double.isNaN(output[i + nLi])) {
                        output[i + nLi] = 0.0;
                    }
                    /* depth - saturated area*/
                    double dVs_sat_dt = (1e3) * (ap * qu_s - ai * qs_l - aH[0][i] * ev_s); //m3/h

                    if (da_dh > 0 && hb[0][i] > 0) {
                        output[i + 2 * nLi] = (1 / 0.1) * (1 / 60.) * 1e6 * (dVs_sat_dt / (hb[0][i] * da_dh)); //output[mm/min] effPrecip and qss [mm/hour]
                    } else {
                        output[i + 2 * nLi] = 0;
                    }
                    if (Double.isNaN(output[i + 2 * nLi])) {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Vs_unsat != 0) /* soil moisture*/ {
                        output[i + 3 * nLi] = (1 / 0.1) * (1 / 60.) * (1 / Vs_unsat) * (1e3 * (aH[0][i] * qp_u - (ap) * qu_s - aH[0][i] * ev_u) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                    } else {
                        output[i + 3 * nLi] = 0;
                    }

                    if (Double.isNaN(output[i + 3 * nLi])) {
                        output[i + 3 * nLi] = 0.0;
                    }
                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }


                break;
              case 20262:
                /* CORRECT SNOW MELT TO GO TO THE HILLSLOPE!!!!!
                 * goes from soil to the river
                /* qs_l = (Ksat * hrel*lengthArray[0][i]*2/ (ai *1e3))*(ai/aH[0][i]);
                /* Same as Hill 6 with evaporation
                 */
                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {
                    double snow = 0.8 * basinHillSlopesInfo.SnowMelt(i, time);// //[mm/h]
                    double SnowCover = basinHillSlopesInfo.SnowCover(i, time);// //[mm/h]
                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    double EV = EVcoef * basinHillSlopesInfo.PotEVPT(i, time);

                    //double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    // HILLSLOPE GEOMETRY RELATIONSHIPS
                    double ai = 0.0; //[km2]
                    double ap = 0; //[km2]
                    double dsoil = 0; //[mm]
                    double Vs_sat = 0; //[m3]
                    double Vs_unsat = 0; //[m3]
                    double da_dh = 100.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i]) / 1000; // [m]
                    double hrelmax = (Ht[0][i] - hb[0][i]) / 1000; // [m]
                    if (hrel <= 0) {
                        hrel = 0.01;
                    }

                    // check limit of soil parameters
                    if (input[i + 3 * nLi] < 0) {
                        input[i + 3 * nLi] = 0.0001; // [%]
                    }
                    if (input[i + 3 * nLi] > 1) {
                        input[i + 3 * nLi] = 0.9999; // [%]
                    }
                    if (hrelmax > 0.001) {

                        ai = aH[0][i] * (Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel / hrelmax)
                                + Area_Relief_Param[2][i] * Math.pow((hrel / hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 3));//km2
                        if (ai <= 0) {
                            ai = 0.01;
                        }
                        ap = aH[0][i] - ai; //in [km2
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat = (ai * hb[0][i] * 1e3); //in m3
                        Vs_unsat = (Vt[0][i] - Vs_sat); //in meters3
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]


                    } //[km]

                    if (hrel >= hrelmax) {
                        dsoil = 0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel = hrelmax;
                        input[i + 2 * nLi] = hrelmax * 1000 + hb[0][i];
                        Vs_sat = Vt[0][i];
                        Vs_unsat = 0;
                        ap = 0.0;
                        ai = aH[0][i];
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]

                    }
                    if (da_dh <= 0) {
                        da_dh = 100.0;
                    }
                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)





                    //System.out.println("da_dh   "  + da_dh + "hrelmax" + hrelmax+ "hrel" + hrel);
                    // Hillslope model - estimation of infiltration
                    double RC = 0;
                    if (hrelmax <= 0.001) {
                        ai = aH[0][i];
                        ap = 0; //in [km2]
                        Vs_sat = 0.0; //in m3
                        Vs_unsat = (Vt[0][i]); //in meters3
                        dsoil = 0;
                        da_dh = 0.;
                        RC = 1.0;
                    }
                    //double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p;
                    if (input[i + nLi] > 0.0f && hrelmax > 0.001) {

                        RC = input[i + nLi] * (input[i + nLi] + 2 * dsoil) / Math.pow((input[i + nLi] + dsoil), 2);

                        double infmax = input[i + nLi];

                        qp_u = ap * (1 - RC) * infmax / aH[0][i]; //[mm/h]

                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h

                    double Ksat = (HydCond[0][i]);      //m/hour

                    double projarea = hrel * lengthArray[0][i] * 1e-6;
                    if (input[i + nLi] > MinPound) {
                        qp_l = ((ap * RC + ai) / aH[0][i]) * ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                        // double test=1/(2 * lengthArray[0][i] / (1e6 * aH[0][i]));
                        //System.out.println(" ah [km] " + aH[0][i] + " hill length [m] " + lengthArray[0][i] + " hill width [m] " + test + " veloc [m/h] " + vH);    
                    } else {
                        qp_l = 0.0;
                    }
                    
                    double kunsat = KunsatModel(KunsatModelType, Ksat, input[i + 3 * nLi]); //m/h

                    qu_s = kunsat * input[i + 3 * nLi] * 1000 * (ap / aH[0][i]); //mm/hour

                    if (ai >= 0 && hrel >= 0 && hrelmax > 0.001) {
                        //qs_l = (Ksat * hrel*lengthArray[0][i]*2/ (ai *1e3))*(ai/aH[0][i]);  //mm/hour // correct units!!!!!
                        double Slope = Math.max(0.01, slopesChannel[i]);

                        qs_l = (Vs_sat / Vt[0][i]) * Slope * Ksat * 1000 * (ai / aH[0][i]);  //mm/hour // correct units!!!!!


                    } else {
                        qs_l = 0;
                    }



                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;
                    double Cs = 0.0;
                    if (EV > 0) {
                        Cs = input[i + nLi] / EV;
                    }
                    double av_depth_uns = Vs_unsat * input[i + 3 * nLi] / (aH[0][i] * 1e3);//in mm
                    double av_depth_sat = Vs_sat / (aH[0][i] * 1e3);//in mm
                    //
                    double Cunsat = input[i + 3 * nLi] * (av_depth_uns / hb[0][i]) * (ap / aH[0][i]);
                    double Csat =  input[i + 3 * nLi] * (ai / aH[0][i]);
                    double SC = Cs + Cunsat + Csat;
                    double Corr = 1;
                    if (SC > 1) {
                        Corr = 1 / SC;
                    }
                    ev_surf = Corr * Cs * EV;
                    ev_u = Corr * Cunsat * EV;
                    ev_s = Corr * Csat * EV;


                    //             if(i==1)  {System.out.println("PotEV   "   + EV +" Cs " + Cs + "  Cunsat " + Cunsat + "  Csat "+Csat);
                    //            System.out.println("PotEV   "   + EV +" Corr*Cs*EV " + Corr*Cs + "  Cunsat " + Corr*Cunsat + "  Csat "+Corr*Csat);}
                    //System.out.println("hb   "   + hb[0][i] +"qs_l   "   + qs_l + " qu_s " + qu_s + "  ai "+ai + "  ap " + ap);


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * (qp_l + qs_l + snow)))) + Q_trib - input[i]); //[m3/s]/min

                    if (Double.isNaN(output[i])) {
                        output[i] = 0.0;
                    }

                    /*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u - ev_surf); //output[mm/h/min]
                    if (Double.isNaN(output[i + nLi])) {
                        output[i + nLi] = 0.0;
                    }
                    /* depth - saturated area*/
                    double dVs_sat_dt = (1e3) * (aH[0][i] * (qu_s - qs_l - ev_s)); //m3/h

                    if (da_dh > 0 && hb[0][i] > 0) {
                        output[i + 2 * nLi] = (1 / 60.) * 1e6 * (dVs_sat_dt / (hb[0][i] * da_dh)); //output[mm/min] effPrecip and qss [mm/hour]
                    } else {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Double.isNaN(output[i + 2 * nLi])) {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Vs_unsat != 0) /* soil moisture*/ {
                        output[i + 3 * nLi] = (1 / 60.) * (1 / Vs_unsat) * (1e3 * (aH[0][i] * (qp_u - qu_s - ev_u)) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                    } else {
                        output[i + 3 * nLi] = 0;
                    }

                    if (Double.isNaN(output[i + 3 * nLi])) {
                        output[i + 3 * nLi] = 0.0;
                    }
                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * (snow);

                    //output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }
                break;    
           
            case 200:
                /* Same as Hill 6 with evaporation
                 * qs_l = Ksat * 1000 * (ai / aH[0][i])* (hrel / hrelmax); - baseflow does not depend on head of water
                 */

                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    //double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    double EV = EVcoef * basinHillSlopesInfo.PotEVPT(i, time);
                    // HILLSLOPE GEOMETRY RELATIONSHIPS
                    //System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);

                    double Ia = lambdaSCS * hb[0][i]; //[mm]
                    double ai = 0.0; //[km2]
                    double ap = 0; //[km2]
                    double dsoil = 0; //[mm]
                    double Vs_sat = 0; //[m3]
                    double Vs_unsat = 0; //[m3]
                    double da_dh = 100.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i]) / 1000; // [m]
                    double hrelmax = (Ht[0][i] - hb[0][i]) / 1000; // [m]
                    if (hrel < 0) {
                        hrel = 0.0;
                    }

                    // check limit of soil parameters
                    if (input[i + 3 * nLi] < 0) {
                        input[i + 3 * nLi] = 0.0; // [%]
                    }
                    if (input[i + 3 * nLi] > 1) {
                        input[i + 3 * nLi] = 1.0; // [%]
                    }
                    if (hrel >= 0.0 && hrel < hrelmax && hrelmax > 0) {
                        if (hrel == 0) {
                            ai = 0; // [km2]
                        } else {
                            ai = aH[0][i] * (Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel / hrelmax)
                                    + Area_Relief_Param[2][i] * Math.pow((hrel / hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 3));//km2
                        }
                        if (ai < 0) {
                            ai = 0.;
                        }
                        if (ai > aH[0][i]) {
                            ai = aH[0][i];
                        }
                        ap = aH[0][i] - ai; //in [km2]
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat = (ai * hb[0][i] * 1e3); //in m3
                        Vs_unsat = (Vt[0][i] - Vs_sat); //in meters3
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh <= 0) {
                            da_dh = 100.0;
                        }

                    } //[km]

                    if (hrel >= hrelmax) {
                        dsoil = 0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel = hrelmax;
                        input[i + 2 * nLi] = hrelmax * 1000 + hb[0][i];
                        Vs_sat = Vt[0][i];
                        Vs_unsat = 0;
                        ap = 0;
                        ai = aH[0][i];
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh < 0) {
                            da_dh = 100;
                        }
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)


                    if (hrelmax < 0.01) {
                        ai = aH[0][i];;
                        ap = 0; //in [km2]
                        Vs_sat = 0.0; //in m3
                        Vs_unsat = (Vt[0][i]); //in meters3
                        da_dh = 0.0; //[m2/m]
                        dsoil = 0;
                    }

                    //System.out.println("da_dh   "  + da_dh + "hrelmax" + hrelmax+ "hrel" + hrel);
                    // Hillslope model - estimation of infiltration
                    double RC = 0;
//                    double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p;
                    if (input[i + nLi] > 0.0f) {
                        if (dsoil == 0) {
                            RC = 1.0;
                        } else {
                            RC = input[i + nLi] * (input[i + nLi] + 2 * dsoil) / Math.pow((input[i + nLi] + dsoil), 2);
                        } //[%]

                        if (hrelmax < 0.01) {
                            RC = 1.0;
                        }
                        if (RC > 1) {
                            RC = 1;
                        }
                        if (RC < 0) {
                            RC = 0;
                        }
                        /// this would guarantee it is in mm/h

                        double infmax = 0.0;
                        //MaxInfRate[0][i]=0.01f;
                        if (input[i + nLi] > MaxInfRate[0][i]) {
                            infmax = MaxInfRate[0][i];
                        } else {
                            infmax = input[i + nLi];
                        }
                        qp_u = (1 - RC) * infmax;

                        //qp = (ai * p + ap * RC* p)/aH[0][i] ;    /// this would guarantee it is in mm/h
                        //qp_u = ap * (1 - RC) * p/aH[0][i];


                        if (qp_u < 0) {
                            qp_u = 0;
                        }
                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h

                    double Ksat = (HydCond[0][i]);      //m/hour

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])

                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat = Ksat * Math.exp(10 * (input[i + 3 * nLi] - 1));
                    if (Double.isNaN(kunsat)) {
                        kunsat = Ksat;
                    }
                    if (input[i + 3 * nLi] * (Vs_unsat / aH[0][i]) / 1000 > 300) {
                        qu_s = 5; //mm/hour
                    } else {
                        qu_s = 0;
                    }

                    //if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* (ai/aH[0][i])* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!
                    if (ai > 0 && hrel > 0 && hrelmax > 0 && (Vs_sat / aH[0][i]) / 1000 > 100) {
                        //qs_l = Ksat * 1000 * (hrel / hrelmax) * (ai / aH[0][i]);  //mm/hour // correct units!!!!!
                        qs_l = Ksat * 1000 * (ai / aH[0][i]) * (hrel / hrelmax);  //mm/hour // correct units!!!!!

                    } else {
                        qs_l = 0;
                    }



                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;



                    double av_depth_uns = Vs_unsat * input[i + 3 * nLi] / (aH[0][i] * 1e3);//in mm
                    double av_depth_sat = Vs_sat / (aH[0][i] * 1e3);//in mm

                    if (input[i + nLi] == 0) {
                        ev_surf = 0f;

                    } else if (input[i + nLi] > 0 && input[i + nLi] <= EV) {
                        ev_surf = input[i + nLi];
                    } else {
                        ev_surf = EV;
                    }
                    if (EV - ev_surf > 0) {

                        if (av_depth_uns == 0) {
                            ev_u = 0.;
                        } else {
                            ev_u = (EV - ev_surf) * input[i + 3 * nLi];
                        }
                        if (ev_surf + ev_u > EV) {
                            ev_u = EV - ev_surf;
                        }
                    }

                    if (EV - ev_surf - ev_u > 0) {

                        if (av_depth_sat == 0) {
                            ev_s = 0.;
                        } else {
                            ev_s = (EV - ev_surf - ev_u) * (av_depth_sat / hb[0][i]);
                        }
                        if (ev_surf + ev_u + ev_s > EV) {
                            ev_s = EV - ev_surf - ev_u;
                        }

                    }


                    //if(i==1)  {System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);
                    //System.out.println("input[i+nLi]" + input[i+nLi] + "  ev_surf" + ev_surf + "  av_depth_uns"+av_depth_uns+ "  ev_u" + ev_u + "  av_depth_sat"+av_depth_sat+ "  ev_s" + ev_s);}


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * qp_l) + (ai * qs_l))) + Q_trib - input[i]); //[m3/s]/min
                    if (Double.isNaN(output[i])) {
                        output[i] = 0.0;
                    }
                    /*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u - ev_surf); //output[mm/h/min]
                    if (Double.isNaN(output[i + nLi])) {
                        output[i + nLi] = 0.0;
                    }
                    /* depth - saturated area*/
                    double dVs_sat_dt = (1e3) * (ap * qu_s - ai * qs_l - aH[0][i] * ev_s); //m3/h

                    if (da_dh > 0 && hb[0][i] > 0) {
                        output[i + 2 * nLi] = (1 / 0.1) * (1 / 60.) * 1e6 * (dVs_sat_dt / (hb[0][i] * da_dh)); //output[mm/min] effPrecip and qss [mm/hour]
                    } else {
                        output[i + 2 * nLi] = 0;
                    }
                    if (Double.isNaN(output[i + 2 * nLi])) {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Vs_unsat != 0) /* soil moisture*/ {
                        output[i + 3 * nLi] = (1 / 0.1) * (1 / 60.) * (1 / Vs_unsat) * (1e3 * (aH[0][i] * qp_u - (ap) * qu_s - aH[0][i] * ev_u) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                    } else {
                        output[i + 3 * nLi] = 0;
                    }

                    if (Double.isNaN(output[i + 3 * nLi])) {
                        output[i + 3 * nLi] = 0.0;
                    }
                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }


                break;
            case 201:
                /* Same as Hill 6 with evaporation
                 * qs_l = Ksat * 1000 * (ai / aH[0][i])* (hrel / hrelmax); - baseflow does not depend on head of water
                 */

                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                    if (Double.isNaN(input[i])) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double p = basinHillSlopesInfo.precipitation(i, time);// //[mm/h]
                    //double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time); //[mm]
                    double EV = EVcoef * basinHillSlopesInfo.PotEVPT(i, time);
                    // HILLSLOPE GEOMETRY RELATIONSHIPS
                    //System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);

                    double Ia = lambdaSCS * hb[0][i]; //[mm]
                    double ai = 0.0; //[km2]
                    double ap = 0; //[km2]
                    double dsoil = 0; //[mm]
                    double Vs_sat = 0; //[m3]
                    double Vs_unsat = 0; //[m3]
                    double da_dh = 100.0; //[km2/m]
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - hb[0][i]) / 1000; // [m]
                    double hrelmax = (Ht[0][i] - hb[0][i]) / 1000; // [m]
                    if (hrel < 0) {
                        hrel = 0.0;
                    }

                    // check limit of soil parameters
                    if (input[i + 3 * nLi] < 0) {
                        input[i + 3 * nLi] = 0.0; // [%]
                    }
                    if (input[i + 3 * nLi] > 1) {
                        input[i + 3 * nLi] = 1.0; // [%]
                    }
                    if (hrel >= 0.0 && hrel < hrelmax && hrelmax > 0) {
                        if (hrel == 0) {
                            ai = 0; // [km2]
                        } else {
                            ai = aH[0][i] * (Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * (hrel / hrelmax)
                                    + Area_Relief_Param[2][i] * Math.pow((hrel / hrelmax), 2) + Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 3));//km2
                        }
                        if (ai < 0) {
                            ai = 0.;
                        }
                        if (ai > aH[0][i]) {
                            ai = aH[0][i];
                        }
                        ap = aH[0][i] - ai; //in [km2]
                        dsoil = (1 - (input[i + 3 * nLi])) * (hb[0][i]); //in mm
                        Vs_sat = (ai * hb[0][i] * 1e3); //in m3
                        Vs_unsat = (Vt[0][i] - Vs_sat); //in meters3
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh <= 0) {
                            da_dh = 100.0;
                        }

                    } //[km]

                    if (hrel >= hrelmax) {
                        dsoil = 0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel = hrelmax;
                        input[i + 2 * nLi] = hrelmax * 1000 + hb[0][i];
                        Vs_sat = Vt[0][i];
                        Vs_unsat = 0;
                        ap = 0;
                        ai = aH[0][i];
                        da_dh = (aH[0][i] / hrelmax) * (Area_Relief_Param[1][i]
                                + 2 * Area_Relief_Param[2][i] * (hrel / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hrel / hrelmax), 2)) * 1e6; //[m2/m]
                        if (da_dh < 0) {
                            da_dh = 100;
                        }
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-Vs_sat)


                    if (hrelmax < 0.01) {
                        ai = aH[0][i];;
                        ap = 0; //in [km2]
                        Vs_sat = 0.0; //in m3
                        Vs_unsat = (Vt[0][i]); //in meters3
                        da_dh = 0.0; //[m2/m]
                        dsoil = 0;
                    }

                    //System.out.println("da_dh   "  + da_dh + "hrelmax" + hrelmax+ "hrel" + hrel);
                    // Hillslope model - estimation of infiltration
                    double RC = 0;
//                    double Pe = hillAcumevent - Ia;
                    //Pe = hillAcumevent - Ia;
                    qp = p;
                    if (input[i + nLi] > 0.0f) {
                        if (dsoil == 0) {
                            RC = 1.0;
                        } else {
                            RC = input[i + nLi] * (input[i + nLi] + 2 * dsoil) / Math.pow((input[i + nLi] + dsoil), 2);
                        } //[%]

                        if (hrelmax < 0.01) {
                            RC = 1.0;
                        }
                        if (RC > 1) {
                            RC = 1;
                        }
                        if (RC < 0) {
                            RC = 0;
                        }
                        /// this would guarantee it is in mm/h

                        double infmax = 0.0;
                        //MaxInfRate[0][i]=0.01f;
                        if (input[i + nLi] > MaxInfRate[0][i]) {
                            infmax = MaxInfRate[0][i];
                        } else {
                            infmax = input[i + nLi];
                        }
                        qp_u = (1 - RC) * infmax;

                        //qp = (ai * p + ap * RC* p)/aH[0][i] ;    /// this would guarantee it is in mm/h
                        //qp_u = ap * (1 - RC) * p/aH[0][i];


                        if (qp_u < 0) {
                            qp_u = 0;
                        }
                    } else { // if qacum<Ia - qd=0;
                        qp_u = 0;
                    }
                    // Hillslope Velocity
                    double vH = Hillvelocity(HillVelType, i, input[i + 1 * nLi]); //m/h

                    double Ksat = (HydCond[0][i]);      //m/hour

                    /// (reservoir constant --- 1/hour                    )) * (if ai=aH[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ai/aH[0][i])

                    qp_l = ((2 * lengthArray[0][i] / (1e6 * aH[0][i])) * vH) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat = Ksat * Math.exp(10 * (input[i + 3 * nLi] - 1));
                    if (Double.isNaN(kunsat)) {
                        kunsat = Ksat;
                    }
                    if (input[i + 3 * nLi] * (Vs_unsat / aH[0][i]) / 1000 > 300) {
                        qu_s = 5; //mm/hour
                    } else {
                        qu_s = 0;
                    }

                    //if(ai>0 && hrel>0 && hrelmax>0)qs_l = Ksat * 1000* (ai/aH[0][i])* ((hrel)/(hrelmax));  //mm/hour // correct units!!!!!
                    if (ai > 0 && hrel > 0 && hrelmax > 0 && (Vs_sat / aH[0][i]) / 1000 > 100) {
                        //qs_l = Ksat * 1000 * (hrel / hrelmax) * (ai / aH[0][i]);  //mm/hour // correct units!!!!!
                        qs_l = Ksat * 1000 * (ai / aH[0][i]) * (hrel / hrelmax);  //mm/hour // correct units!!!!!

                    } else {
                        qs_l = 0;
                    }



                    //if(ap==0)qs_l = Ksat * (hrel) *(lengthArray[0][i]/(1e6*ap));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    ev_surf = 0;
                    ev_s = 0;
                    ev_u = 0;



                    double av_depth_uns = Vs_unsat * input[i + 3 * nLi] / (aH[0][i] * 1e3);//in mm
                    double av_depth_sat = Vs_sat / (aH[0][i] * 1e3);//in mm

                    if (input[i + nLi] == 0) {
                        ev_surf = 0f;

                    } else if (input[i + nLi] > 0 && input[i + nLi] <= EV) {
                        ev_surf = input[i + nLi];
                    } else {
                        ev_surf = EV;
                    }
                    if (EV - ev_surf > 0) {

                        if (av_depth_uns == 0) {
                            ev_u = 0.;
                        } else {
                            ev_u = (EV - ev_surf) * input[i + 3 * nLi];
                        }
                        if (ev_surf + ev_u > EV) {
                            ev_u = EV - ev_surf;
                        }
                    }

                    if (EV - ev_surf - ev_u > 0) {

                        if (av_depth_sat == 0) {
                            ev_s = 0.;
                        } else {
                            ev_s = (EV - ev_surf - ev_u) * (av_depth_sat / hb[0][i]);
                        }
                        if (ev_surf + ev_u + ev_s > EV) {
                            ev_s = EV - ev_surf - ev_u;
                        }

                    }


                    //if(i==1)  {System.out.println("PotEV   "   + EV + "   coefEV" +   EVcoef);
                    //System.out.println("input[i+nLi]" + input[i+nLi] + "  ev_surf" + ev_surf + "  av_depth_uns"+av_depth_uns+ "  ev_u" + ev_u + "  av_depth_sat"+av_depth_sat+ "  ev_s" + ev_s);}


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * ((aH[0][i] * qp_l) + (ai * qs_l))) + Q_trib - input[i]); //[m3/s]/min
                    if (Double.isNaN(output[i])) {
                        output[i] = 0.0;
                    }
                    /*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (qp - qp_l - qp_u - ev_surf); //output[mm/h/min]
                    if (Double.isNaN(output[i + nLi])) {
                        output[i + nLi] = 0.0;
                    }
                    /* depth - saturated area*/
                    double dVs_sat_dt = (1e3) * (ap * qu_s - ai * qs_l - aH[0][i] * ev_s); //m3/h

                    if (da_dh > 0 && hb[0][i] > 0) {
                        output[i + 2 * nLi] = (1 / 60.) * 1e6 * (dVs_sat_dt / (hb[0][i] * da_dh)); //output[mm/min] effPrecip and qss [mm/hour]
                    } else {
                        output[i + 2 * nLi] = 0;
                    }
                    if (Double.isNaN(output[i + 2 * nLi])) {
                        output[i + 2 * nLi] = 0.0;
                    }
                    if (Vs_unsat != 0) /* soil moisture*/ {
                        output[i + 3 * nLi] = (1 / 60.) * (1 / Vs_unsat) * (1e3 * (aH[0][i] * qp_u - (ap) * qu_s - aH[0][i] * ev_u) + ((input[i + 3 * nLi]) * dVs_sat_dt)); //output[1/min]
                    } else {
                        output[i + 3 * nLi] = 0;
                    }

                    if (Double.isNaN(output[i + 3 * nLi])) {
                        output[i + 3 * nLi] = 0.0;
                    }
                    output[i + 4 * nLi] = (1 / 60.) * (ev_surf + ev_u + ev_s);
                    output[i + 5 * nLi] = (1 / 60.) * qp;
                    output[i + 6 * nLi] = (1 / 60.) * qp_l;
                    output[i + 7 * nLi] = (1 / 60.) * qs_l;
                    output[i + 8 * nLi] = (1 / 60.) * ((qp_u - qs_l - ev_u - ev_s) + (qp - qp_u - qp_l - ev_surf));

                }


                break;
        }


        //if (Math.random() > 0.99) if (maxInt>0) System.out.println("      --> The Max precipitation intensity is: "+maxInt);

        return output;
    }

    public double Hillvelocity(int method, int hilslope, double SurfDepth) {
        double vH = 1.0;
        int i = hilslope;
        double Slope = Math.max(0.0005, slopesLand[0][i]);
        double Slope2 = Math.max(0.0005, slopesLand2[0][i]);
        double Slope3 = Math.max(0.0005, slopesChannel[i]);
        int nLi = linksConectionStruct.connectionsArray.length;
        switch (method) {
            case 0: //constant
                vH = vHunoff;
                break;

            case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = (1 / 0.6) * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope / 10000.0, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0;

                break;

            case 2: //as a function of land cover
                if (landCoversArray[0][i] == 0) {
                    vH = 500.0; // water
                } else if (landCoversArray[0][i] == 1) {
                    vH = 250.0; // urban area
                } else if (landCoversArray[0][i] == 2) {
                    vH = 100.0; // baren soil
                } else if (landCoversArray[0][i] == 3) {
                    vH = 10.0; // Forest
                } else if (landCoversArray[0][i] == 4) {
                    vH = 100.0; // Shrubland
                } else if (landCoversArray[0][i] == 5) {
                    vH = 20.0; // Non-natural woody/Orchards
                } else if (landCoversArray[0][i] == 6) {
                    vH = 100.0; // Grassland
                } else if (landCoversArray[0][i] == 7) {
                    vH = 20.0; // Row Crops
                } else if (landCoversArray[0][i] == 8) {
                    vH = 100.0; // Pasture/Small Grains
                } else if (landCoversArray[0][i] == 9) {
                    vH = 50.0; // Wetlands
                }
                break;
            case 3: //NRCS method manning roughness as a function of land cover and soil hyd group
                vH = (Hill_K_NRCSArray[0][i]) * Math.pow((Slope * 100), 0.5) * 0.3048; //(m/h)

                break;

            case 4: //NRCS method manning roughness as a function of land cover and soil hyd group
                vH = (Hill_K_NRCSArray[0][i]) * Math.pow((Slope / 100), 0.5) * 0.3048 * 3600; //(m/h)
                break;

            case 5: //Consider center of mass of the hillslope (0.6)
                vH = (Hill_K_NRCSArray[0][i]) * Math.pow((Slope / 100), 0.5) * 0.3048 * 3600; //(m/h)
                break;
            case 6: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                break;

            case 7: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)

                break;
            case 75: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = 0.5 * coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                if (vH < 0.1) {
                    vH = 0.1;
                }
                break;
            case 71: //manning equation - manning roughness as a function of land cover and soil hyd group

                vH = 0.1 * coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                if (vH < 0.1) {
                    vH = 0.1;
                }
                break;
            case 711: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = 0.01 * coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)



                break;

            case 107: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                if (vH < 0.1) {
                    vH = 0.1;
                }
                break;
            case 702: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                if (vH > 200) {

                    vH = 200;
                }
                break;

            case 701: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                if (vH > 100) {

                    vH = 100;
                }
                break;

            case 703: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                if (vH > 300) {

                    vH = 300;
                }
                break;

            case 750: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                if (vH > 50) {

                    vH = 50;
                }
                break;

            case 8: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope2, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                break;
            case 85: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = 0.5 * coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope2, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)
                break;
            case 9: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope3, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)    
                if (vH < 0.1) {
                    vH = 0.1;
                }
                break;
            case 91: //manning equation - manning roughness as a function of land cover and soil hyd group
                vH = 0.1 * coefvh * (1.0 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope3, 0.5) * Math.pow(SurfDepth / 1000.0, (2.0 / 3.0)) * 3600.0; //(m/h)

                break;
        }
        if (vH > 500) {
            System.out.println("superiod limit reached," + basinHillSlopesInfo.HillManning(i) + "," + Slope + "," + "," + Slope2 + "," + Slope3 + "," + SurfDepth + "," + vH);
            vH = 500;
        }

        if (Double.isNaN(vH)) {

            System.out.println("superiod NaN reached," + basinHillSlopesInfo.HillManning(i) + "," + Slope + "," + "," + Slope2 + "," + Slope3 + "," + SurfDepth + "," + vH);

            vH = 1;
        }

        return vH;
    }

    public double RoutingType(int method, int hilslope, double Qchannel) {
        int i = hilslope;
        double Qflood = 0;
        switch (method) {
            case 2:

                //System.out.println("CkArray[0][i]   " +CkArray[0][i]  +   "lengthArray[0][i]  "+lengthArray[0][i]);
                K_Q = CkArray[0][i] / lengthArray[0][i];

                break;
            case 5:
                K_Q = CkArray[0][i] * Math.pow(Qchannel, lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                // Ricardo's version
                //case 5:
                //K_Q = CkArray[0][i]*Math.pow(input[i],   lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);


                break;

            case 6:
                Qflood = CQflood * Math.pow(upAreasArray[0][i], EQflood);
                double Schannel = (lengthArray[0][i] / (CkArray[0][i] * Math.pow(Qchannel, lamda1) * Math.pow(upAreasArray[0][i], lamda2))) * Qchannel;

                //if(Qchannel<Qflood)
                K_Q = CkArray[0][i] * Math.pow(Qchannel, lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                //else K_Q = CkArray[0][i] * Math.pow(Qflood, lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);

                break;

            case 7:
                double Alim = 100;
                double vo2 = CkArray[0][i] * Math.pow(Alim, lamda2);
                if (upAreasArray[0][i] < Alim) {
                    K_Q = vo2 * Math.pow(Qchannel, lamda1) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                } else {
                    K_Q = CkArray[0][i] * Math.pow(Qchannel, lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                }
                //System.out.println("vo2 =" + vo2 + "upAreasArray[0][i]" +upAreasArray[0][i]);
                break;

            case 8:
                Alim = 1000;
                vo2 = CkArray[0][i] * Math.pow(Alim, lamda2);
                if (upAreasArray[0][i] < Alim) {
                    K_Q = vo2 * Math.pow(Qchannel, lamda1) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                } else {
                    K_Q = CkArray[0][i] * Math.pow(Qchannel, lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                }
                //System.out.println("vo2 =" + vo2 + "upAreasArray[0][i]" +upAreasArray[0][i]);
                break;
            case 9:
                //Average velocity of v=0.3 for A<1000
                Alim = 1000;
                vo2 = CkArray[0][i] * Math.pow(Alim, lamda2);
                if (upAreasArray[0][i] < Alim) {
                    K_Q = 0.3 * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                } else {
                    K_Q = CkArray[0][i] * Math.pow(Qchannel, lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                }
                //System.out.println("vo2 =" + vo2 + "upAreasArray[0][i]" +upAreasArray[0][i]);
                break;
        }

        if (Qchannel == 0) {
            K_Q = 0.1 / (lengthArray[0][i]);
        }

        return K_Q;
    }

    public double ChannelVel(int method, int hilslope, double Qchannel) {
        int i = hilslope;
        switch (method) {
            case 2:
                K_Q = CkArray[0][i];
                break;
            case 5:
                K_Q = CkArray[0][i] * Math.pow(Qchannel, lamda1) * Math.pow(upAreasArray[0][i], lamda2);
                break;
        }

        if (Qchannel == 0) {
            K_Q = 0.1 / (lengthArray[0][i]);
        }

        return K_Q;
    }

    public double AverageSlope(int method, int hilslope, double waterTable, double hrelmax) {
        double dh_da = 0.0;
        int i = hilslope;
        double averSlope = 0;


        switch (method) {
            case 0: //constant

                double inc = waterTable / 5;
                double hw = 0;
                int nelem = 0;
                while (hw <= waterTable) {
                    dh_da = 1 / ((aH[0][i] * 1e6 / (lengthArray[0][i] * hrelmax)) * ((Area_Relief_Param[1][i]
                            + 2 * Area_Relief_Param[2][i] * (hw / hrelmax) + 3 * Area_Relief_Param[3][i] * Math.pow((hw / hrelmax), 2))));
                    averSlope = averSlope + dh_da;
                    nelem = nelem + 1;
                    hw = hw + inc;
                }
                averSlope = averSlope / nelem;
                break;
            case 1:
                averSlope = Math.max(1e-7, slopesLand[0][i]);
                //constant  
                break;
        }

        if (averSlope <= 0) {
            averSlope = 1e-7;
        }

        return averSlope;
    }

    public double KunsatModel(int method, double Ksat, double SoilVol) {
        double vH = 1.0;

        double kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
        int nLi = linksConectionStruct.connectionsArray.length;
        switch (method) {
            case 0: //constant
                kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                //System.out.println("KuCte    " + KuCte);

                break;



            case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                if (kunsat < 1e-5) {
                    kunsat = 1e-5;
                }
                break;
            case 11: //manning equation - manning roughness as a function of land cover and soil hyd group
                //System.out.println("Ksat"+Ksat);
                kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                if (kunsat < 1e-4) {
                    kunsat = 1e-4;
                }
                //  System.out.println("kunsat  "+kunsat +"  SoilVol  " +SoilVol);
                break;

            case 12: //manning equation - manning roughness as a function of land cover and soil hyd group
                // System.out.println("Ksat"+Ksat);


                kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                if (kunsat < 1e-5) {
                    kunsat = 1e-5;
                }
                //System.out.println("kunsat  "+kunsat +"  SoilVol  " +SoilVol);
                break;
                       case 123: //manning equation - manning roughness as a function of land cover and soil hyd group
                // System.out.println("Ksat"+Ksat);


                kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                if (kunsat < 1e-3) {
                    
                    kunsat = 1e-3;
                }
                //System.out.println("kunsat  "+kunsat +"  SoilVol  " +SoilVol);
                break;
            case 124: //manning equation - manning roughness as a function of land cover and soil hyd group
                // System.out.println("Ksat"+Ksat);


                kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                if (kunsat < 1e-4) {
                    kunsat = 1e-4;
                }
                //System.out.println("kunsat  "+kunsat +"  SoilVol  " +SoilVol);
                break;

            case 122: //manning equation - manning roughness as a function of land cover and soil hyd group
                // System.out.println("Ksat"+Ksat);
                kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                if (kunsat < 1e-6) {
                    kunsat = 1e-6;
                }
                //System.out.println("kunsat  "+kunsat +"  SoilVol  " +SoilVol);
                break;

            case 13: //manning equation - manning roughness as a function of land cover and soil hyd group
                if (SoilVol > 0.8) {
                    kunsat = (1.0 - kunsat) * 10 * Ksat;
                } else {
                    kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                }
                if (kunsat < 1e-5) {
                    kunsat = 1e-5;
                }
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                break;
            case 14: //manning equation - manning roughness as a function of land cover and soil hyd group
                if (SoilVol > 0.8) {
                    kunsat = Ksat;
                } // System.out.println("Ksat"+Ksat);
                else {
                    kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                }
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                if (kunsat < 1e-5) {
                    kunsat = 1e-5;
                }
                //System.out.println("kunsat  "+kunsat +"  SoilVol  " +SoilVol);
                break;


            case 15: //manning equation - manning roughness as a function of land cover and soil hyd group
                if (SoilVol > 0.8) {
                    kunsat = 10 * Ksat;
                } // System.out.println("Ksat"+Ksat);
                else {
                    kunsat = Ksat * Math.exp(KuCte * (SoilVol - 1));
                }
                if (Double.isNaN(kunsat)) {
                    kunsat = Ksat;
                }
                if (kunsat < 1e-5) {
                    kunsat = 1e-5;
                }
                //System.out.println("kunsat  "+kunsat +"  SoilVol  " +SoilVol);
                break;



        }


        return kunsat;
    }
}