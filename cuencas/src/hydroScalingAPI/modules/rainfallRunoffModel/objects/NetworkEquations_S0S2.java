/*
 * NetworkEquations.java
 *
 * Created on November 11, 2001, 10:26 AM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 *
 * @author  ricardo
 * @version
 */
public class NetworkEquations_S0S2 implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {
    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;

    private double qd, effPrecip, qs, qe, Q_trib, Qs_trib, K_Q, K_Qhill ;
    private float hillslopeLength, flowdepth, hydrad, mannings_n ;
    //public double pcoe;

    /* PF ADDITION - START ... */
    private float[][] cheziArray, widthArray, lengthArray, slopeArray, constantvelArray;
    //private float[][] areasHillArray;

    private int hilltype, routingtype;
    private double satsurf, mst, Ku, qdh, qds, inf, re, qe2, rh, rs, rt ;
    private float [] runoffrat;

     float[] link_thresholds_mmphr;
    /* PF ADDITION - ... END */

    private double[] output;
    /** Creates new NetworkEquations */
    public NetworkEquations_S0S2(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis links, hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo hillinf, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linkIn,  float[] thresholds_mmphr){
        linksConectionStruct=links;
        basinHillSlopesInfo=hillinf;
        linksHydraulicInfo=linkIn;
        link_thresholds_mmphr=thresholds_mmphr;   // threshold values (random or mean) based on empirical results.

        cheziArray=linksHydraulicInfo.getCheziArray();
        widthArray=linksHydraulicInfo.getWidthArray();
        lengthArray=linksHydraulicInfo.getLengthInKmArray();
        for (int i=0;i<lengthArray[0].length;i++) lengthArray[0][i]=lengthArray[0][i]*1e3f;
        slopeArray=linksHydraulicInfo.getSlopeArray();
        constantvelArray=linksHydraulicInfo.getCkArray();       // velocity define as coefficient in velocity expression
        //constantvelArray=linksHydraulicInfo.getConstantVelArray();

    }



    public float[] eval(float[] input, float time) {
        return new float[0];  // dummy
    }

    public float[] eval(float[] input) {
        return new float[0];  // dummy
    }

    public double[] eval(double[] input) {
        return new double[0];  // dummy
    }

    public double[] eval(double[] input, double time) {
        //the input's length is twice the number of links... the first half corresponds to links discharge and the second to hillslopes discharge
        //System.out.println(input.length);

        int nLi=linksConectionStruct.headsArray.length;

        // Conditions for hillslope-channel processes
        hilltype = 2 ;        // 1 = no infiltration, 2 = infiltration determined by threshold
        routingtype = 1 ; // 1 = const vel w/var l, 4 = mannings, 3 = chezy
        output=new double[input.length];

        //output_vc=new double[nLi];

        switch (hilltype) {

            case 1 :    // No infiltration

                for (int i=0;i<nLi;i++){

                   /* NOTE: Initial conditions are ...
                   input[i]  for link discharge
                   input[i+nLi] for link base flow
                   input[i+2*nLi] for hillslope S0
                   input[i+3*nLi] for hillslope S2 .

                   input[] is updated for each time step in DiffEqSolver.RKF . */

                    /* CHANGE UNITS*/
                    //double prec_mmphr=Math.max(basinHillSlopesInfo.precipitation(i,time)-1.32,0.0); // 1.32 mm/hr from asae228j.pdf document, Its 50% of mean Ks for GC
                    double prec_mmphr = basinHillSlopesInfo.precipitation(i,time);   // *0.0, *0.5f, *runoffrat[i]
                    double prec_mphr = prec_mmphr / 1000.;   // FROM mm/hr TO m/hr
                    double area_km2 = basinHillSlopesInfo.Area(i);
                    double area_m2 = area_km2*1e6;
                    double prec_m3phr = area_m2 * prec_mphr; // m3phr
                    //System.out.println(i + "   " + basinHillSlopesInfo.precipitation(i,time));

                    /*HILLSLOPE FLUX CONDITIONS */
                    satsurf = basinHillSlopesInfo.S2Param(i) * input[i+3*nLi]; // dimless
                    //mst = input[i+2*nLi]/( basinHillSlopesInfo.S2max(i) - input[i+3*nLi] ); // dimless
                    //Ku = basinHillSlopesInfo.Ks(i) * ( Math.pow(mst,basinHillSlopesInfo.MstExp(i)) ); // mphr

                    rh  = (1.0D - satsurf) * prec_m3phr; // m3phr, rain on hortonian surface (unsaturated surface)
                    inf = 0.0D ; //m3phr
                    qdh = 3600.*input[i+2*nLi];              // m3phr, ........... input[] = m^3/s

                    /*HILLSLOPE S0-S2 FLUX VALUE */
                    re = inf ;

                    /*HILLSLOPE S2-SURFACE FLUX VALUES */
                    rs = satsurf * prec_m3phr; // m3phr, rain on saturated surface
                    qds = satsurf * area_m2 * prec_mphr ; // m3phr
                    qe2 = basinHillSlopesInfo.ETrate(i) * area_m2 * satsurf ; // m3phr, *0.0
                    qs = basinHillSlopesInfo.RecParam(i) * input[i+3*nLi] ; // m3phr

                    /*HILLSLOPE RAINFALL (TOTAL) FLUXES */
                    rt = rh + rs ; //m3phr

                    /*HILLSLOPE DIRECT RUNOFF (TOTAL) FLUXES */
                    qd = qdh + qds ; // m3phr

                    /*LINK FLUX ( Q )*/
                    /* Below, i=link#, j=id of connecting links, Array[i][j]=link# for connecting link */
                    Q_trib=0.0D;
                    for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                        Q_trib+=input[linksConectionStruct.connectionsArray[i][j]]; // units m^3/s
                    }

                    /*LINK FLUX ( Q SUBSURFACE, BASE FLOW )*/
                    /* Below, i=link#, j=id of connecting links, Array[i][j]=link# for connecting link */
                    Qs_trib=0.0D;
                    for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                        Qs_trib+=input[linksConectionStruct.connectionsArray[i][j]+nLi]; // units m^3/s
                    }

                    /*ROUTING RATE (K_Q) and CHANNEL VELOCITY (vc)*/
                    //System.out.println(routingtype);

                    switch (routingtype) {

                        case 0 :   /* Constant velocity and link length. For Goodwin, mean length = 221.8 and assume velocity 0.5 m/s */
                            K_Q = constantvelArray[0][i ] / 221.8 ;  // units 1/s
                            break ;

                        case 1 :    /* Constant velocity. For Goodwin, assume velocity 0.5 m/s */
                            K_Q = constantvelArray[0][i]*Math.pow(lengthArray[0][i],-1);  // units 1/s
                            //K_Q = 1.0*Math.pow(lengthArray[0][i],-1);  // units 1/s
                            //K_Q = 2.0*Math.pow(linksHydraulicInfo.Length(i),-1);  // units 1/s
                            break ;

                        case 2 :    /* No Chezi explicitly */
                            K_Q=8.796*Math.pow(input[i],1/3.)
                            *Math.pow(widthArray[0][i],-1/3.)
                            *Math.pow(lengthArray[0][i],-1)
                            *Math.pow(slopeArray[0][i],2/9.);   // units 1/s*/
                            break ;

                        case 3 :    /* Chezi explicit */
                            //System.out.println("Chezy");
                            K_Q=3/2.*Math.pow(input[i],1/3.)
                            *Math.pow(cheziArray[0][i],2/3.)
                            *Math.pow(widthArray[0][i],-1/3.)
                            *Math.pow(lengthArray[0][i],-1)
                            *Math.pow(slopeArray[0][i],1/3.); // units 1/s
                            break ;

                        case 4 :    /* Mannings equation */
                            flowdepth = (float) ((1./3.)*Math.pow(input[i],1/3.)) ;   // depth m, input m^3/s; general observed relation for gc from molnar and ramirez 1998
                            hydrad = (flowdepth*widthArray[0][i])/(2.f*flowdepth + widthArray[0][i]) ;  // m
                            mannings_n = 0.030f ;       // mannings n suggested by Jason via his observations at Whitewater for high flows. Low flows will have higher n ... up to 2x more.
                            K_Q = ( Math.pow(hydrad,2./3.)
                            *Math.pow(slopeArray[0][i],1/2.)
                            /mannings_n )                          // m/s ; this term is v from mannings eqn
                            *Math.pow(lengthArray[0][i],-1);   // 1/s
                            break;

                    }

                    // Define K_Qhill ...
                    hillslopeLength = ((float) area_m2 / lengthArray[0][i]) / 2.0f;       // length on one hillslope (one side of link) is half of link length; lengthArray in m
                    K_Qhill = (0.01*constantvelArray[0][i])*Math.pow(hillslopeLength,-1);  // units 1/s

                    if (input[i]==0.0D) K_Q=1e-10;

                   /*if (i == 62) {
                  System.out.println("   WD ratio ="+ linksHydraulicInfo.Width(i)/flowdepth);
                  System.out.println("   Mannings v (m/s) =" + (Math.pow(hydrad,2./3.)*Math.pow(linksHydraulicInfo.Slope(i),1/2.)/mannings_n) );
                  System.out.println("   K_Q =" + (Math.pow(hydrad,2./3.)*Math.pow(linksHydraulicInfo.Slope(i),1/2.)/mannings_n) *Math.pow(linksHydraulicInfo.Length(i),-1) );
                   }*/

                    /* OUTPUT */
                    //LINK dQ/dt; big () term is m^3/s, 60*K_Q is 1/min
                    output[i]= 60.0D*K_Q*((1.0D/3600.)*(qd+qs)+Q_trib-input[i]);  //units (m^3/s)/min
                    //if (i == 423) System.out.println(qd + "  "+ qs + "  " + output[i]);

                    //LINK dQs/dt
                    output[i+nLi]= 60.0D*K_Q*((1.0D/3600.)*(qs)+Qs_trib-input[i+nLi]);  //units (m^3/s)/min

                    //HILLSLOPE dS0/dt
                    //output[i+(2*nLi)]=(1.0D/60.0)*(rt-inf-qd);  //units m3pmin ...
                    //HILLSLOPE dQ0/dt
                    output[i+(2*nLi)]= 60.0D*K_Qhill*((1.0D/3600.)*(rt-inf-qd));     //units (m^3/s)/min

                    //HILLSLOPE dS2/dt
                    output[i+(3*nLi)]=(1.0D/60.0)*(re-qs-qe2); //units m3pmin

                }

                break ;

                case 2 :   // infiltration determined by threshold

                for (int i=0;i<nLi;i++){

                   /* NOTE: Initial conditions are ...
                   input[i]  for link discharge
                   input[i+nLi] for link base flow
                   input[i+2*nLi] for hillslope S0
                   input[i+3*nLi] for hillslope S2 .

                   input[] is updated for each time step in DiffEqSolver.RKF . */

                    /* CHANGE UNITS*/
                    //double prec_mmphr=Math.max(basinHillSlopesInfo.precipitation(i,time)-1.32,0.0); // 1.32 mm/hr from asae228j.pdf document, Its 50% of mean Ks for GC
                    double prec_mmphr = basinHillSlopesInfo.precipitation(i,time);   // *0.0, *0.5f, *runoffrat[i]
                    double prec_mphr = prec_mmphr / 1000.;   // FROM mm/hr TO m/hr
                    double area_km2 = basinHillSlopesInfo.Area(i);
                    double area_m2 = area_km2*1e6;
                    double prec_m3phr = area_m2 * prec_mphr; // m3phr
                    double link_threshold_mphr = link_thresholds_mmphr[i]/1000.;
                    //System.out.println(i + "   " + basinHillSlopesInfo.precipitation(i,time));

                    /*HILLSLOPE FLUX CONDITIONS */
                    satsurf = basinHillSlopesInfo.S2Param(i) * input[i+3*nLi]; // dimless
                    //mst = input[i+2*nLi]/( basinHillSlopesInfo.S2max(i) - input[i+3*nLi] ); // dimless
                    //Ku = basinHillSlopesInfo.Ks(i) * ( Math.pow(mst,basinHillSlopesInfo.MstExp(i)) ); // mphr

                    /*HILLSLOPE S0-SURFACE FLUX VALUES */
                    // Threshold retrieves a random number associated with a seed value. Same seed gives same random number.
                    if ( prec_mphr <= link_threshold_mphr ) {
                        inf = (1.0 - satsurf) * area_m2 * prec_mphr ; // m3phr
                        qdh = 0.0 ; // m3phr
                    }
                    else {
                        inf = (1.0 - satsurf) * area_m2 *link_threshold_mphr ; //m3phr
                        qdh = (1.0 - satsurf) * area_m2 *(prec_mphr-link_threshold_mphr); // m3phr
                        //System.out.println("precip OVER");
                    }

                    rh  = (1.0D - satsurf) * prec_m3phr; // m3phr, rain on hortonian surface (unsaturated surface)
                    //qdh = 3600.*input[i+2*nLi];              // m3phr, ........... input[] = m^3/s

                    /*HILLSLOPE S0-S2 FLUX VALUE */
                    re = inf ;

                    /*HILLSLOPE S2-SURFACE FLUX VALUES */
                    rs = satsurf * prec_m3phr; // m3phr, rain on saturated surface
                    qds = satsurf * area_m2 * prec_mphr ; // m3phr
                    qe2 = basinHillSlopesInfo.ETrate(i) * area_m2 * satsurf ; // m3phr, *0.0
                    qs = basinHillSlopesInfo.RecParam(i) * input[i+3*nLi] ; // m3phr

                    /*HILLSLOPE RAINFALL (TOTAL) FLUXES */
                    rt = rh + rs ; //m3phr

                    /*HILLSLOPE DIRECT RUNOFF (TOTAL) FLUXES */
                    qd = qdh + qds ; // m3phr

                    /*LINK FLUX ( Q )*/
                    /* Below, i=link#, j=id of connecting links, Array[i][j]=link# for connecting link */
                    Q_trib=0.0D;
                    for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                        Q_trib+=input[linksConectionStruct.connectionsArray[i][j]]; // units m^3/s
                    }

                    /*LINK FLUX ( Q SUBSURFACE, BASE FLOW )*/
                    /* Below, i=link#, j=id of connecting links, Array[i][j]=link# for connecting link */
                    Qs_trib=0.0D;
                    for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                        Qs_trib+=input[linksConectionStruct.connectionsArray[i][j]+nLi]; // units m^3/s
                    }

                    /*ROUTING RATE (K_Q) and CHANNEL VELOCITY (vc)*/
                    //System.out.println(routingtype);

                    switch (routingtype) {

                        case 0 :   /* Constant velocity and link length. For Goodwin, mean length = 221.8 and assume velocity 0.5 m/s */
                            K_Q = constantvelArray[0][i ] / 221.8 ;  // units 1/s
                            break ;

                        case 1 :    /* Constant velocity. For Goodwin, assume velocity 0.5 m/s */
                            K_Q = constantvelArray[0][i]*Math.pow(lengthArray[0][i],-1);  // units 1/s
                            //K_Q = 1.0*Math.pow(lengthArray[0][i],-1);  // units 1/s
                            //K_Q = 2.0*Math.pow(linksHydraulicInfo.Length(i),-1);  // units 1/s
                            break ;

                        case 2 :    /* No Chezi explicitly */
                            K_Q=8.796*Math.pow(input[i],1/3.)
                            *Math.pow(widthArray[0][i],-1/3.)
                            *Math.pow(lengthArray[0][i],-1)
                            *Math.pow(slopeArray[0][i],2/9.);   // units 1/s*/
                            break ;

                        case 3 :    /* Chezi explicit */
                            //System.out.println("Chezy");
                            K_Q=3/2.*Math.pow(input[i],1/3.)
                            *Math.pow(cheziArray[0][i],2/3.)
                            *Math.pow(widthArray[0][i],-1/3.)
                            *Math.pow(lengthArray[0][i],-1)
                            *Math.pow(slopeArray[0][i],1/3.); // units 1/s
                            break ;

                        case 4 :    /* Mannings equation */
                            flowdepth = (float) ((1./3.)*Math.pow(input[i],1/3.)) ;   // depth m, input m^3/s; general observed relation for gc from molnar and ramirez 1998
                            hydrad = (flowdepth*widthArray[0][i])/(2.f*flowdepth + widthArray[0][i]) ;  // m
                            mannings_n = 0.030f ;       // mannings n suggested by Jason via his observations at Whitewater for high flows. Low flows will have higher n ... up to 2x more.
                            K_Q = ( Math.pow(hydrad,2./3.)
                            *Math.pow(slopeArray[0][i],1/2.)
                            /mannings_n )                          // m/s ; this term is v from mannings eqn
                            *Math.pow(lengthArray[0][i],-1);   // 1/s
                            break;

                    }

                    // Define K_Qhill ...
                    hillslopeLength = ((float) area_m2 / lengthArray[0][i]) / 2.0f;       // length on one hillslope (one side of link) is half of link length; lengthArray in m
                    K_Qhill = (0.01*constantvelArray[0][i])*Math.pow(hillslopeLength,-1);  // units 1/s

                    if (input[i]==0.0D) K_Q=1e-10;

                   /*if (i == 62) {
                  System.out.println("   WD ratio ="+ linksHydraulicInfo.Width(i)/flowdepth);
                  System.out.println("   Mannings v (m/s) =" + (Math.pow(hydrad,2./3.)*Math.pow(linksHydraulicInfo.Slope(i),1/2.)/mannings_n) );
                  System.out.println("   K_Q =" + (Math.pow(hydrad,2./3.)*Math.pow(linksHydraulicInfo.Slope(i),1/2.)/mannings_n) *Math.pow(linksHydraulicInfo.Length(i),-1) );
                   }*/

                    /* OUTPUT */
                    //LINK dQ/dt; big () term is m^3/s, 60*K_Q is 1/min
                    output[i]= 60.0D*K_Q*((1.0D/3600.)*(qd+qs)+Q_trib-input[i]);  //units (m^3/s)/min
                    //if (i == 423) System.out.println(qd + "  "+ qs + "  " + output[i]);

                    //LINK dQs/dt
                    output[i+nLi]= 60.0D*K_Q*((1.0D/3600.)*(qs)+Qs_trib-input[i+nLi]);  //units (m^3/s)/min

                    //HILLSLOPE dS0/dt
                    //output[i+(2*nLi)]=(1.0D/60.0)*(rt-inf-qd);  //units m3pmin ...
                    //HILLSLOPE dQ0/dt
                    output[i+(2*nLi)]= 60.0D*K_Qhill*((1.0D/3600.)*(rt-inf-qd));     //units (m^3/s)/min

                    //HILLSLOPE dS2/dt
                    output[i+(3*nLi)]=(1.0D/60.0)*(re-qs-qe2); //units m3pmin

                }

                break ;

        }

        return output ;
    }

}
