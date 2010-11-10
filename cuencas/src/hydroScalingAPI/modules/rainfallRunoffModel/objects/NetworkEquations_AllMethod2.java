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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This calss implements the set of non-linear ordinary differential equations used
 * to simulate flows along the river network.  The function is writen as a 
 * {@link hydroScalingAPI.util.ordDiffEq01lver.BasicFunction}
 * that is used by the
 * {@link hydroScalingAPI.util.ordDiffEq01lver.RKF}
 * @author Ricardo Mantilla 
 */
public class NetworkEquations_AllMethod2 implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {

    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;
    java.util.Hashtable routingParams;
    private int routingType, HillType, HillVelType;
    private float rr;
    private double q01, q12, q1c, q23, qe, Q_trib, K_Q, q31, q_in_res, q_out_res;
    private double effPrecip, qs;
    private double[] output;
    private float[][] manningArray, cheziArray, widthArray, lengthArray, slopeArray, CkArray;
    private float[][] areasHillArray, upAreasArray, landCoversArray, slopesArray, Hill_K_NRCSArray;
    private float[][] Hill_SoilDepth, Hill_HillDepth, Hill_TotalDepth, Hill_TotalVol;
    private float[][] Area_Relief_Param;
    private double So, Ts, Te, vh; // not an array because I assume uniform soil properties
    private double lamda1, lamda2;
    private double vrunoff, vsub, SM, lambdaSCS; // velocity of the direct runoff and subsurface runoff - m/s
    private float[][] Vo, Gr;
    private int connectingLink;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.StreamFlowTimeSeries[] upFlows;
    private double change = 0.0;

    /**
     * Creates new NetworkEquations_Simple
     * @param links The topologic structure of the river network
     * @param hillinf The parameters manager for the system of hillsopes
     * @param linkIn The parameters manager for links in the network
    (parallel version)
     * @param connLink The parameters that define the upstream links in the network
     * @param inputFiles The parameters that define the files related to eac upstream links
     * @param (Hashtable) rP All the other parameters necessary for the rainfall-runoff model
     *                       - depends on the hilltype being used
     * CHANNEL ROUTING SCHEME.  Available schemes in this version are <br>
     *          <p>2: Constant Velocity</p>
     *          <p>5: Velocity based on parametrization v=Ck*q^lambda1*A^lambda2</p>

     * RAINFALL-RUNOFF TRANSFORMATION.  Available schemes in this version are <br>
     *          <p>0: Runoff coefficient
     *          <p>5: SCS Method

     * HILLSOPE ROUTING SCHEME.  Available schemes in this version are <br>
     *          <p>0: with delay - constant velocity in hillslope
     *          <p>5: SCS Method

     */
    public NetworkEquations_AllMethod2(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis links, hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo hillinf, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linkIn, int connLink, java.io.File[] inputFiles, java.util.Hashtable rP) {
        linksConectionStruct = links;
        basinHillSlopesInfo = hillinf;
        linksHydraulicInfo = linkIn;
        routingParams = rP;
  
        upAreasArray = linksHydraulicInfo.getUpStreamAreaArray(); //[km]
        cheziArray = linksHydraulicInfo.getCheziArray();
        manningArray = linksHydraulicInfo.getManningArray();
        widthArray = linksHydraulicInfo.getWidthArray();
        lengthArray = linksHydraulicInfo.getLengthInKmArray(); //[km]
        areasHillArray = basinHillSlopesInfo.getAreasArray();//[km2]

        for (int i = 0; i < lengthArray[0].length; i++) {
            lengthArray[0][i] = lengthArray[0][i] * 1000; //[m]
        }
        slopeArray = linksHydraulicInfo.getSlopeArray();

        landCoversArray = new float[1][lengthArray[0].length];
        slopesArray = new float[1][lengthArray[0].length];
        Hill_K_NRCSArray = new float[1][lengthArray[0].length];
        Area_Relief_Param = new float[4][lengthArray[0].length]; //Area in km and depth in m
        Hill_SoilDepth = new float[1][lengthArray[0].length]; // mm
        Hill_HillDepth = new float[1][lengthArray[0].length]; // mm
        Hill_TotalDepth = new float[1][lengthArray[0].length]; // mm
        Hill_TotalVol  = new float[1][lengthArray[0].length]; //[m3]
        
        
        float HD=-9.f;
        if (routingParams.get("ConstSoilStorage") != null) {
               HD = ((Float) routingParams.get("ConstSoilStorage")).floatValue();
        }
        //System.out.println("ConstSoilStorage"+HD);
        // HILLSLOPE GEOMETRY RELATIONSHIP
        for (int i = 0; i < lengthArray[0].length; i++) {
            landCoversArray[0][i] = (float) basinHillSlopesInfo.LandUseSCS(i);
            slopesArray[0][i] = (float) basinHillSlopesInfo.getHillslope(i);
            Hill_K_NRCSArray[0][i] = (float) basinHillSlopesInfo.Hill_K_NRCS(i);
            if(HD<0) Hill_SoilDepth[0][i] = (float) basinHillSlopesInfo.SCS_S2(i); // [mm] maximum storage - considering basin was dry
            else Hill_SoilDepth[0][i] = HD; // [mm] maximum storage - considering basin was dry
            Hill_HillDepth[0][i] = (float) basinHillSlopesInfo.HillRelief(i)*1000; // [mm] hillslope relief
            Hill_TotalDepth[0][i] = Hill_SoilDepth[0][i] + Hill_HillDepth[0][i]; // [mm] hillslope relief
            Hill_TotalVol[0][i] = (float) ((Hill_SoilDepth[0][i]) * areasHillArray[0][i] * 1e3); // in m3
            Area_Relief_Param[0][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 0);
            Area_Relief_Param[1][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 1);
            Area_Relief_Param[2][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 2);
            Area_Relief_Param[3][i] = (float) basinHillSlopesInfo.getArea_ReliefParam(i, 3);
        }
      


        //So=basinHillSlopesInfo.So(0);
        //Ts=basinHillSlopesInfo.Ts(0);
        //Te=basinHillSlopesInfo.Te(0);
        //System.out.println("equation object 3");
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

        // SCS implementation
        



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
            rr = (int) ((Float) routingParams.get("RunoffCoefficient")).floatValue();
        }

        // Future implementation (greenroof and reservoir)
        //Vo=basinHillSlopesInfo.getVolResArray();
        //Gr=basinHillSlopesInfo.getGreenRoofAreaArray(); // area of building in the hillslope (m2)
        System.out.println("routingType" + routingType);
        //for (int i=0;i<lengthArray[0].length;i++) Gr[0][i]=Gr[0][i]/(areasHillArray[0][i]*1000000);
        float vconst = ((Float) routingParams.get("Vconst")).floatValue();
        if (routingParams.get("vssub") != null) {
            vsub = ((Float) routingParams.get("vssub")).floatValue();
        }
        if (routingParams.get("vrunoff") != null) {
            vrunoff = ((Float) routingParams.get("vrunoff")).floatValue(); //[m/h]
        }        //System.out.println("equation object 10");
        if (routingParams.get("SoilMoisture") != null) {
            SM = ((Float) routingParams.get("SoilMoisture")).floatValue();
        }
        lambdaSCS=0.0;
        if (routingParams.get("lambdaSCSMethod") != null) {
            lambdaSCS = ((Float) routingParams.get("lambdaSCSMethod")).floatValue();
        }
        //System.out.println("equation object 11");
        System.out.println("Generate object NetworkEquations_AllMethod with parameters - routingType" + routingType + "HillType" + HillType + "HillVelType" + HillVelType);
        

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

     

        int nLi = linksConectionStruct.connectionsArray.length;

        output = new double[input.length];
        /*java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(time*60.*1000.0));
        System.out.println("    "+thisDate.getTime()+" "+basinHillSlopesInfo.precipitation(0,time)+" "+input[287]);*/

        switch (HillType) {
            case 0:    /* RUNOFF = RAINFALL - NO DELAY DELLAY FUNCTION */

                for (int i = 0; i < nLi; i++) {
                    if (input[i] < 0) {
                        input[i] = 0;
                    }
                    double rrr = 1.0;
                    if (rr == 1.0f) { // for cedar river iowa simulation
                        rrr = 0.55;
//                        if (time >= 20200620 && time < 20216460) {
//                            rrr = 0.55;
//                        }
//                        if (time > 20216460) {
//                            rrr = 0.70;
//                        }
                    }

                    double hillPrecIntensity = rrr * basinHillSlopesInfo.precipitation(i, time);// for URP event apply this rule*0.143;

                    effPrecip = Math.max(hillPrecIntensity - basinHillSlopesInfo.infiltRate(i, time), 0.0);
                    q01 = 0;
                    q12 = 0;
                    q1c = effPrecip; //Uncomment to eliminate the effect of hillslope routing
                    q31 = 0.0;

                    Q_trib = 0.0;
                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }

                    K_Q = RoutingType(routingType, i, input[i]);

                    double ks = 0 / 3.6e6;

                    double chanLoss = lengthArray[0][i] * widthArray[0][i] * ks;


                    /* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * areasHillArray[0][i] * (q1c + q31)) + Q_trib - input[i] - chanLoss); //[m3/s]/min
/* hillslope reservoir*/ output[i + nLi] = 0.0;//(1/60.)*(q01-q1c); //output[mm/min] qd and qs [mm/hour]
/* soil reservoir*/ output[i + 2 * nLi] = 0.0;//(1/60.)*(q12-q31); //output[mm/min] effPrecip and qss [mm/hour]
/* runoff rate*/            output[i+3*nLi]=0.0; // runoff rate mm/min
/* subsurface flow rate*/   //output[i+4*nLi]=(1/60.)*q31; //subsurface flow mm/min
/* accum prec*/             //output[i+5*nLi]=(1/60.)*hillPrecIntensity; //accumulated precipitation mm/min


                }

                break;

            case 1:    /* RUNOFF = RAINFALL - WITH DELLAY FUNCTION */

                for (int i = 0; i < nLi; i++) {
                    if (input[i] < 0) {
                        input[i] = 0;
                    }
                    double rrr = 1.0;

                   if (rr == 1.0f) { // for cedar river iowa simulation
                       if (rr == 1.0f) { // for cedar river iowa simulation
                        rrr = 0.55;
//                        if (time >= 20200620 && time < 20216460) {
//                            rrr = 0.55;
//                        }
//                        if (time > 20216460) {
//                            rrr = 0.70;
//                        }
                    }

                    }
                    double hillPrecIntensity = rrr * basinHillSlopesInfo.precipitation(i, time);// for URP event apply this rule*0.143;

                    effPrecip = Math.max(hillPrecIntensity - basinHillSlopesInfo.infiltRate(i, time), 0.0);
                    q01 = effPrecip;
                    q12 = Math.max(0, hillPrecIntensity - effPrecip);
                    // Hillslope Velocity
                    double vr = Hillvelocity(HillVelType, i, input[i + 1 * nLi]);

                    /// (reservoir constant --- 1/hour                    )) * (if ImpArea=areasHillArray[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ImpArea/areasHillArray[0][i])
                    q1c = ((2 * lengthArray[0][i] / (1e6 * areasHillArray[0][i])) * vr) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));


                    //q1c=effPrecip; //Uncomment to eliminate the effect of hillslope routing
                    q31 = 0.0;
                    Q_trib = 0.0;
                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
                    double ks = 0 / 3.6e6;

                    double chanLoss = lengthArray[0][i] * widthArray[0][i] * ks;


                    /* the links*/ output[i] = 60 * K_Q * (((1 / 3.6) * areasHillArray[0][i] * (q1c + q31)) + Q_trib - input[i] - chanLoss); //[m3/s]/min
/* hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (q01 - q1c); //output[mm/min] qd and qs [mm/hour]
/* soil reservoir*/ output[i + 2 * nLi] = (1 / 60.) * (q12 - q31); //output[mm/min] effPrecip and qss [mm/hour]
/* soil reservoir*/ output[i + 3 * nLi] = 0.0f; //output[mm/min] effPrecip and qss [mm/hour]


                }
                break;
            case 3:
                /* Luciana - Simplified version of hillslope equations
                 * Aug-09/2010
                 * Presents a better representation of the subsurface flow
                 * Considering hillslope shape
                 * Applies SCS method to estimate infiltration
                 * This is the SCS equation, where Qacum=(Pacum-IA)^2/(Pacum-IA+St)
                 * Pe=Pacum-IA
                 * So=(SI-M)
                 * dQ/dt=(Pe*(Pe+2So)/(Pe+So)^2)*dPe/dt
                 * SI=maximum infiltration capacity = f(soil moisture, soil hydrologic group, dry soil)
                 * M=soil moisture at time t
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

                    double hillPrecIntensity = basinHillSlopesInfo.precipitation(i, time);// for URP event apply this rule*0.143;
                    double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time);
                    // HILLSLOPE GEOMETRY RELATIONSHIPS

                    double IA = lambdaSCS * Hill_SoilDepth[0][i];
                    double ImpArea = 0.0;
                    double PermArea =0;
                    double deficit=0;
                    double V3=0;
                    double V2=0;
                    double da_dh = 0.0;
                    // h cannot be lower than the channel botton = hsoil
                    double hrel = (input[i + 2 * nLi] - Hill_SoilDepth[0][i])/1000; // in m
                    if(hrel<0) hrel=Hill_SoilDepth[0][i]/1000;
                    double hrelmax =  (Hill_TotalDepth[0][i] - Hill_SoilDepth[0][i])/1000; // in m

                    // check limit of soil parameters
                    if(input[i + 3 * nLi]<0) input[i + 3 * nLi]=0;
                    if(input[i + 3 * nLi]>1) input[i + 3 * nLi]=1;
                    if(hrel<0) hrel=0;

                    if (hrel >= 0 && hrel < hrelmax) {
                        if(hrel==0) ImpArea = 0;
                        else ImpArea = Area_Relief_Param[0][i] + Area_Relief_Param[1][i] * hrel
                                + Area_Relief_Param[2][i] * Math.pow(hrel, 2) + Area_Relief_Param[3][i] * Math.pow(hrel, 3);//km2
                        if(ImpArea<0) ImpArea=0;
                        if(ImpArea>areasHillArray[0][i]) ImpArea=areasHillArray[0][i];

                        PermArea = areasHillArray[0][i] - ImpArea; //in [km2]
                        deficit = (1 - (input[i + 3 * nLi])) * (Hill_SoilDepth[0][i]); //in mm
                        V3=(ImpArea*Hill_SoilDepth[0][i]*1e3); //in m3
                        V2=(Hill_TotalVol[0][i]-V3); //in meters
                        da_dh = (Area_Relief_Param[1][i]
                                 + 2 * Area_Relief_Param[2][i] * hrel + 3 * Area_Relief_Param[3][i] * Math.pow(hrel, 2))*1e6; //[m2/m]
                        if(da_dh <0) da_dh=0;

                    } //[km]
    
                    if (hrel>=hrelmax)
                    {
                        deficit=0;
                        //input[i + 1 * nLi]=input[i + 1 * nLi]+(hrel-hrelmax)*1000;
                        hrel=hrelmax;
                        input[i + 2 * nLi]=hrelmax*1000+Hill_SoilDepth[0][i];
                        V3=Hill_TotalVol[0][i];
                        V2=0;
                        PermArea =0;
                        ImpArea=areasHillArray[0][i];
                        da_dh = (Area_Relief_Param[1][i]
                                 + 2 * Area_Relief_Param[2][i] * hrel + 3 * Area_Relief_Param[3][i] * Math.pow(hrel, 2))*1e6; //m2/m

                       if(da_dh <0) da_dh=0;
                    }

                    //Soil moisture content of the unsaturated layer (0-1) times volume of the layer 2 (Vol total-v3)



                    // Hillslope model - estimation of infiltration
                    double C = 0;
                    double Pe = hillAcumevent - IA;
                    if (hillPrecIntensity > 0.0f && (Pe) > 0) {
                        C = Pe * (Pe + 2 * deficit) / Math.pow((Pe + deficit), 2);
                        if (C > 1) {
                            C = 1;
                        }
                        if (C < 0) {
                            C = 0;
                        }
                        q01 = (ImpArea * hillPrecIntensity + PermArea * C * hillPrecIntensity) / areasHillArray[0][i];    /// this would guarantee it is in mm/h
                        q12 = (PermArea / areasHillArray[0][i]) * (1 - C) * (hillPrecIntensity);
                        if (q12 < 0) {
                            q12 = 0;
                        }
                    } else { // if qacum<IA - qd=0;
                        q01 = 0;
                        q12 = 0;
                    }
                    // Hillslope Velocity
                    double vr = Hillvelocity(HillVelType, i, input[i + 1 * nLi]);
                    if(vr>500) vr=500;
                    if(vr<1) vr=1;
                    if(Double.isNaN(vr)) vr=1;

                    double Ksat = (vsub);     //hour
                    
                    /// (reservoir constant --- 1/hour                    )) * (if ImpArea=areasHillArray[0][i], time constant is larger because
                    //it has to travel throught the hillslope, if imp area =0 the water does not flow**** I removed that(ImpArea/areasHillArray[0][i])
                    q1c = ((2 * lengthArray[0][i] / (1e6 * areasHillArray[0][i])) * vr) * (input[i + nLi]); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
                    double kunsat=Ksat*Math.exp(input[i + 3 * nLi]-1);
                    if(Double.isNaN(vr)) kunsat=Ksat;
                    q23 = kunsat *input[i + 3 * nLi]*1000; //mm/hour
                    
                    if(ImpArea>0 && hrel>0 && hrelmax>0)q31 = Ksat * 1000* (ImpArea/areasHillArray[0][i])*(input[i + 2*nLi]-Hill_SoilDepth[0][i])/(hrelmax*1000);  //mm/hour // correct units!!!!!
                    else q31 =0;
                    //if(PermArea==0)q31 = Ksat * (hrel) *(lengthArray[0][i]/(1e6*PermArea));  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);
 
                  

/* the links*/                  output[i] = 60 * K_Q * ((1 / 3.6 * areasHillArray[0][i] * (q1c)) + Q_trib - input[i]); //[m3/s]/min
                                if(Double.isNaN(output[i])) output[i]=0.0;
/*surface hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (q01 - q1c + q31); //output[mm/h/min]
                                if(Double.isNaN(output[i + nLi])) output[i + nLi]=0.0;
/* depth - saturated area*/
                                double dV3_dt=(1e12)*((areasHillArray[0][i]-ImpArea) * q23 - (areasHillArray[0][i]) * q31); //mm3/h
                                if(da_dh>0 && Hill_SoilDepth[0][i]>0) output[i + 2 * nLi] =(1 / 60.) * (dV3_dt/(1e3*Hill_SoilDepth[0][i]*da_dh)) ; //output[mm/min] effPrecip and qss [mm/hour]
                                else output[i + 2 * nLi] =0;
                                if(Double.isNaN(output[i + 2 * nLi])) output[i + 2 * nLi]=0.0;
                                if(V2!=0)
/* soil moisture*/              output[i + 3 * nLi] = (1 / 60.) * (1 / V2) * (1e3*(areasHillArray[0][i]*q12 - (areasHillArray[0][i]-ImpArea)*q23) - ((input[i + 3 * nLi]) * dV3_dt)/1e9); //output[1/min]
                                else output[i + 3 * nLi]=0;
                                if(Double.isNaN(output[i + 3 * nLi])) output[i + 3 * nLi]=0.0;


                }
                break;
            case 4:
                /* Luciana - Considering soil and soil avaliability equal to
                 * Soil storage for AMC =1 - estimated using curve number
                 * This is the SCS equation, where Qacum=(Pacum-IA)^2/(Pacum-IA+St)
                 * Pe=Pacum-IA
                 * So=(SI-M)
                 * dQ/dt=(Pe*(Pe+2So)/(Pe+So)^2)*dPe/dt
                 * SI=maximum infiltration capacity = f(soil moisture, soil hydrologic group, dry soil)
                 * M=soil moisture at time t
                 */
                for (int i = 0; i < input.length; i++) {
                    if (input[i] < 0.0) {
                        input[i] = 0.0;
                    }
                }

                for (int i = 0; i < nLi; i++) {

                    double hillPrecIntensity = basinHillSlopesInfo.precipitation(i, time);// for URP event apply this rule*0.143;
                    double hillAcumevent = basinHillSlopesInfo.precipitationacum(i, time);
                    double IA = -9.9;
                    double S = -9.9;
                    double lambda = lambdaSCS;
                    S = Hill_SoilDepth[0][i]; // maximum storage - considering basin was dry
                    double deficit = S - input[i + 2 * nLi];
                    IA = lambda * S;
                    if (IA < 0) {
                        IA = 0;
                    }
                    if (S < 0) {
                        S = 0;
                    }


                    // indication that it is raining - hillAcumPreccurr>hillAcumPrecprev
                    double C = 0;
                    double Pe = hillAcumevent - IA;
                    if (hillPrecIntensity > 0.0f && (Pe) > 0) {
                        if (deficit <= 0) {
                            C = 1; // Runoff coefficient = 1 cause soil is saturated
                        }
                        if (deficit > 0) {
                            C = Pe * (Pe + 2 * deficit) / Math.pow((Pe + deficit), 2);
                        }
                        if (C > 1) {
                            C = 1;
                        }
                        if (C < 0) {
                            C = 0;
                        }
                        q01 = C * hillPrecIntensity;    /// this would guarantee it is in mm/h
                        q12 = hillPrecIntensity - q01;
                        if (q12 < 0) {
                            q12 = 0;
                        }

                    } else { // if qacum<IA - qd=0;
                        q01 = 0;
                        q12 = 0;
                    }

                    // if (i==119)System.out.println("Link =" + i + "C= "+C+"hillAcumevent=" + hillAcumevent+" Pe ="+Pe+" S = "+S+" So = "+input[i+2*nLi]+" So/S = "+input[i+2*nLi]/S);
                    // Hillslope Velocity
                    double vr = Hillvelocity(HillVelType, i, input[i + 1 * nLi]);
                    double dist = (1e6 * areasHillArray[0][i]) / (1.6 * 2 * lengthArray[0][i]); //(m)
                    double K = (dist / (vr)); //(hour)
                    double K_subfast = dist / (vsub);     //hour
                    double K_subslow = dist / (vsub / 100);     //hour

                    q1c = (input[i + nLi] / K); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));


                    q31 = 0.4 * (input[i + 2 * nLi] / K_subfast) + 0.6 * (input[i + 2 * nLi] / K_subslow);  //mm/hour
                    Q_trib = 0.0;

                    for (int j = 0; j < linksConectionStruct.connectionsArray[i].length; j++) {
                        Q_trib += input[linksConectionStruct.connectionsArray[i][j]];
                    }


                    K_Q = RoutingType(routingType, i, input[i]);


                    double ks = 0 / 3.6e6;
                    double chanLoss = lengthArray[0][i] * widthArray[0][i] * ks;

                    //double qev=1; //1mm/h
                    //double soilLoss=areasHillArray[0][i]*ks;
                    //System.out.println("Link =" + i + "C= "+C+"hillAcumevent=" + hillAcumevent+" Pe ="+Pe+" S = "+S+" So = "+input[i+2*nLi]+" So/S = "+input[i+2*nLi]/S);
/* the links*/ output[i] = 60 * K_Q * ((1 / 3.6 * areasHillArray[0][i] * (q1c + q31)) + Q_trib - input[i] - chanLoss); //[m3/s]/min
/* hillslope reservoir*/ output[i + nLi] = (1 / 60.) * (q01 - q1c); //output[mm/min] qd and qs [mm/hour]
/* soil reservoir*/ output[i + 2 * nLi] = (1 / 60.) * (q12 - q31); //output[mm/min] effPrecip and qss [mm/hour]
/* soil reservoir*/ output[i + 3 * nLi] = 0.0; //output[mm/min] effPrecip and qss [mm/hour]

                }
                break;


        }


        // Evaluate the output flow for the upstream links - do not change with rainfall-runoff model
        for (int k = 0; k < upFlows.length; k++) {
            int i = connectingLink;

            K_Q = RoutingType(routingType, i, input[i]);

            output[i] += 60 * K_Q * (upFlows[k].evaluate(time));
        }

        //if (Math.random() > 0.99) if (maxInt>0) System.out.println("      --> The Max precipitation intensity is: "+maxInt);

        return output;
    }

    public double Hillvelocity(int method, int hilslope, double SurfDepth) {
        double vr = 1.0;
        int i = hilslope;
        double Slope = Math.max(0.005, slopesArray[0][i]);

        int nLi = linksConectionStruct.connectionsArray.length;
        switch (method) {
            case 0: //constant
                vr = vrunoff;
                break;

            case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                vr = (1 / basinHillSlopesInfo.HillManning(i)) * Math.pow(Slope, 0.5) * Math.pow(SurfDepth / 1000, (2 / 3)) * 3600; //(m/h)
                break;
            case 2: //as a function of land cover
                if (landCoversArray[0][i] == 0) {
                    vr = 500.0; // water
                } else if (landCoversArray[0][i] == 1) {
                    vr = 250.0; // urban area
                } else if (landCoversArray[0][i] == 2) {
                    vr = 100.0; // baren soil
                } else if (landCoversArray[0][i] == 3) {
                    vr = 10.0; // Forest
                } else if (landCoversArray[0][i] == 4) {
                    vr = 100.0; // Shrubland
                } else if (landCoversArray[0][i] == 5) {
                    vr = 20.0; // Non-natural woody/Orchards
                } else if (landCoversArray[0][i] == 6) {
                    vr = 100.0; // Grassland
                } else if (landCoversArray[0][i] == 7) {
                    vr = 20.0; // Row Crops
                } else if (landCoversArray[0][i] == 8) {
                    vr = 100.0; // Pasture/Small Grains
                } else if (landCoversArray[0][i] == 9) {
                    vr = 50.0; // Wetlands
                }
                break;
            case 3: //NRCS method manning roughness as a function of land cover and soil hyd group
                vr = (Hill_K_NRCSArray[0][i]) * Math.pow((Slope), 0.5) * 100 * 0.3048; //(m/h)
                if (vr > 500) {
                    vr = 500;
                }
                if (vr < 10) {
                    vr = 10;                      //m/h
                }
                break;
        }
        return vr;
    }

    public double RoutingType(int method, int hilslope, double Qchannel) {
        int i = hilslope;
        switch (method) {
            case 2:
                K_Q = CkArray[0][i] / lengthArray[0][i];
                break;
            case 5:
                K_Q = CkArray[0][i] * Math.pow(Qchannel, lamda1) * Math.pow(upAreasArray[0][i], lamda2) * Math.pow(lengthArray[0][i], -1) / (1 - lamda1);
                break;
        }

        if (Qchannel == 0) {
            K_Q = 0.1 / (lengthArray[0][i]);
        }

        return K_Q;
    }
}
