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
import java.io.*;
/**
 * This calss implements the set of non-linear ordinary differential equations used
 * to simulate flows along the river network.  The function is writen as a
 * {@link hydroScalingAPI.util.ordDiffEqSolver.BasicFunction}
 * that is used by the
 * {@link hydroScalingAPI.util.ordDiffEqSolver.RKF}
 * @author Ricardo Mantilla
 */
public class NetworkEquationsLuciana_mod implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {
    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;


    private int routingType,HillType,HillVelType;

    private double qso, qs1, qcsup, qe, Q_trib, K_Q,qcsoil,q_in_res,q_out_res;
    private double[] output;

    private float[][] manningArray, cheziArray, widthArray, lengthArray, slopeArray, CkArray;
    private float[][] areasHillArray, upAreasArray;
    private double So,Ts,Te; // not an array because I assume uniform soil properties

    private double lamda1,lamda2;
    private double vrunoff,vsub,SM,lambdaSCS; // velocity of the direct runoff and subsurface runoff - m/s
    private float[][] Vo,Gr;
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
     *
     * HillType
     * <p>0:  RUNOFF = RAINFALL - NO DELLAY FUNCTION
     * <p>1:  SCS METHOD - Explicitly considering soil misture using AMCI, AMCII and AMCIII
     * <p>2: MISHRA-SINGH METHOD - ACCOUNT FOR SOIL CONDITION - WITH DELAY FUNCTION!!!!!!
     * <p>3: CLAUDE MICHEL,2005 -SCS CN Method: How to mend a wrong soil moisture accounting
     * <p>4: /* Luciana - Considering maximum water potential retention by the soil equal to Soil storage for AMC =1
     * estimated using curve number
                * This is the SCS equation, where Qacum=(Pacum-IA)^2/(Pacum-IA+St)
                * Pe=Pacum-IA
                * So=(SI-M)
                * dQ/dt=(Pe*(Pe+2So)/(Pe+So)^2)*dPe/dt
                * SI=maximum infiltration capacity = f(soil moisture, soil hydrologic group, dry soil)
                * M=soil moisture at time t

     * <p>5: Not completed yet
     *
     * * HillVelType
     * <0> constant for all hillslope
     * <1> manning equation as a function of the land cover
     * <2> arbitrally defined velocity as a function of land cover
     */
    public NetworkEquationsLuciana_mod(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis links, hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo hillinf,
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linkIn,int rt,int ht,int hvt,double vr,double vs,double SMM,double lSCS){
        linksConectionStruct=links;
        basinHillSlopesInfo=hillinf;
        linksHydraulicInfo=linkIn;
        routingType=rt;
        HillType=ht;
        HillVelType=hvt;
        upAreasArray=linksHydraulicInfo.getUpStreamAreaArray();
        cheziArray=linksHydraulicInfo.getCheziArray();
        manningArray=linksHydraulicInfo.getManningArray();
        widthArray=linksHydraulicInfo.getWidthArray();
        lengthArray=linksHydraulicInfo.getLengthInKmArray();
        areasHillArray=basinHillSlopesInfo.getAreasArray();
        for (int i=0;i<lengthArray[0].length;i++) lengthArray[0][i]=lengthArray[0][i]*1000;
        slopeArray=linksHydraulicInfo.getSlopeArray();
        Vo=basinHillSlopesInfo.getVolResArray();
        Gr=basinHillSlopesInfo.getGreenRoofAreaArray(); // area of building in the hillslope (m2)
        for (int i=0;i<lengthArray[0].length;i++) Gr[0][i]=Gr[0][i]/(areasHillArray[0][i]*1000000);
        
        So=basinHillSlopesInfo.So(0);
        Ts=basinHillSlopesInfo.Ts(0);
        Te=basinHillSlopesInfo.Te(0);

        lamda1=linksHydraulicInfo.getLamda1();
        lamda2=linksHydraulicInfo.getLamda2();
        CkArray=linksHydraulicInfo.getCkArray();
        vrunoff=vr; //m/h
        vsub=vs; //m/h
        SM=SMM;
       lambdaSCS=lSCS;
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

        int nLi=linksConectionStruct.connectionsArray.length;

        output=new double[input.length];

        switch (HillType) {
          case 0 :    /* RUNOFF = RAINFALL - NO DELLAY FUNCTION */
          double maxInt=0;
          for (int i=0;i<nLi;i++){

            if (input[i] < 0) input[i]=0;

            double hillPrecIntensity=basinHillSlopesInfo.precipitation(i,time);// for URP event apply this rule*0.143;

            maxInt=Math.max(maxInt,hillPrecIntensity);

            double qs=0.0;
            qso=Math.max(hillPrecIntensity-basinHillSlopesInfo.infiltRate(i,time),0.0);

            qs1=0;
           double vr=1.0;
           double Slope=-9.0;
           double vflor=-9;
           switch (HillVelType) {
                case 0: //constant
                  vr=vrunoff;
                  break;

               case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                   Slope=basinHillSlopesInfo.getHillslope(i);
                   vr=(1/basinHillSlopesInfo.HillManning(i))*Math.pow(Slope,0.5)*Math.pow(input[i+nLi]/1000,(2/3))*3600; //(m/h)
                  //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;
               case 2: //as a function of land cover
                  if(basinHillSlopesInfo.LandUseSCS(i)==0) vr=500.0; // water
                  if(basinHillSlopesInfo.LandUseSCS(i)==1) vr=250.0; // urban area
                  if(basinHillSlopesInfo.LandUseSCS(i)==2) vr=100.0; // baren soil
                  if(basinHillSlopesInfo.LandUseSCS(i)==3) vr=10.0; // Forest
                  if(basinHillSlopesInfo.LandUseSCS(i)==4) vr=100.0; // Shrubland
                  if(basinHillSlopesInfo.LandUseSCS(i)==5) vr=20.0; // Non-natural woody/Orchards
                  if(basinHillSlopesInfo.LandUseSCS(i)==6) vr=100.0; // Grassland
                  if(basinHillSlopesInfo.LandUseSCS(i)==7) vr=20.0; // Row Crops
                  if(basinHillSlopesInfo.LandUseSCS(i)==8) vr=100.0; // Pasture/Small Grains
                  if(basinHillSlopesInfo.LandUseSCS(i)==9) vr=50.0; // Wetlands
               break;
               case 3: //manning equation - manning roughness as a function of land cover and soil hyd group

                   Slope=Math.max(0.005,basinHillSlopesInfo.getHillslope(i));
                   // check this velocity!!! It should be btw 250 and 10 for forest and I just adapt the formula
                   vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]>=(0.5*25.4)) vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]<(0.5*25.4)) vr=0; //(m/h)
                   vflor=(1.4)*Math.pow(Slope,0.5)*100*0.3048;     //same as forest
                   //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;

           }
            double K=0;
            if(vr>0) K=areasHillArray[0][i]*1000000/(vr*2*lengthArray[0][i]); //(m)

            qcsup=(input[i+nLi]/K);

            qe=((input[i+nLi] > 0)?1:0)*(1/Te*(input[i+nLi]));

            Q_trib=0.0;
            for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
            }

            switch (routingType) {
                case 2:     K_Q=0.5/lengthArray[0][i];
                            break;
                case 5:     K_Q=CkArray[0][i]*Math.pow(input[i],lamda1)*Math.pow(upAreasArray[0][i],lamda2)*Math.pow(lengthArray[0][i],-1);
            }

            if (input[i] == 0) K_Q=0.1/(lengthArray[0][i]);

            double ks=0/3.6e6;

            double chanLoss=lengthArray[0][i]*widthArray[0][i]*ks;
//the links
            output[i]=60*K_Q*((1/3.6*areasHillArray[0][i]*(qcsup+qcsoil))+Q_trib-input[i]-chanLoss); //[m3/s]/min
  //the hillslopes
            output[i+nLi]=(1/60.)*(qso-qcsup-qe); //output[mm/min] qd and qs [mm/hour]

          }

       break ;

         case 1 :    /* Luciana - Considering soil misture explicitly using AMCI, AMCII and AMCIII
                * This is the SCS equation, where Qacum=(Pacum-IA)^2/(Pacum-IA+S)
                * Pe=Pacum-IA
                * S=f(land cover, soil properties, AMC)
                * dQ/dt=(Pe*(Pe+2S)/(Pe+S)^2)*dPe/dt

                */
         for (int i=0;i<input.length;i++){if (input[i] < 0.0) input[i]=0.0;}

         for (int i=0;i<nLi;i++){

            double hillPrecIntensity=basinHillSlopesInfo.precipitation(i,time);// for URP event apply this rule*0.143;
            double hillAcumPreccurr=basinHillSlopesInfo.precipitationacum(i,time);

            int iflag=-9;
            double IA=-9.9;
            double S=-9.9;
            if(SM==1) S=basinHillSlopesInfo.SCS_S1(i);
            if(SM==2) S=basinHillSlopesInfo.SCS_S2(i);
            if(SM==3) S=basinHillSlopesInfo.SCS_S3(i);
            IA=lambdaSCS*S;
            if(IA<0) IA=0;
            if(S<0) S=0;

            double ratio=0.0;
            // indication that it is raining - hillAcumPreccurr>hillAcumPrecprev

            if(hillAcumPreccurr>IA && hillPrecIntensity>0.0f)
            { /// This is the SCS equation, where qd=(Pacum-IA)^2/(Pacum-IA-S)
                ratio =(1-(((Math.pow((S-IA),2))/(Math.pow((hillAcumPreccurr+S-2*IA),2)))));
                qso=ratio*hillPrecIntensity;    /// this would guarantee it is in mm/h
                if(qso<0) qso=0;
                qs1=hillPrecIntensity-qso;
                if(qs1<0) qs1=0;
                iflag=1;
            }
            else
            { // if qacum<IA - qd=0;
                qso=0;
                qs1=0;
                if(hillPrecIntensity>0) qs1=hillPrecIntensity;
            }

           double vr=1.0;
           double Slope=-9.0;
           double vflor=-9;
           switch (HillVelType) {
                case 0: //constant
                  vr=vrunoff;
                  break;

               case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                   Slope=basinHillSlopesInfo.getHillslope(i);
                   vr=(1/basinHillSlopesInfo.HillManning(i))*Math.pow(Slope,0.5)*Math.pow(input[i+nLi]/1000,(2/3))*3600; //(m/h)
                  //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;
               case 2: //as a function of land cover
                  if(basinHillSlopesInfo.LandUseSCS(i)==0) vr=500.0; // water
                  if(basinHillSlopesInfo.LandUseSCS(i)==1) vr=250.0; // urban area
                  if(basinHillSlopesInfo.LandUseSCS(i)==2) vr=100.0; // baren soil
                  if(basinHillSlopesInfo.LandUseSCS(i)==3) vr=10.0; // Forest
                  if(basinHillSlopesInfo.LandUseSCS(i)==4) vr=100.0; // Shrubland
                  if(basinHillSlopesInfo.LandUseSCS(i)==5) vr=20.0; // Non-natural woody/Orchards
                  if(basinHillSlopesInfo.LandUseSCS(i)==6) vr=100.0; // Grassland
                  if(basinHillSlopesInfo.LandUseSCS(i)==7) vr=20.0; // Row Crops
                  if(basinHillSlopesInfo.LandUseSCS(i)==8) vr=100.0; // Pasture/Small Grains
                  if(basinHillSlopesInfo.LandUseSCS(i)==9) vr=50.0; // Wetlands
               break;
               case 3: //manning equation - manning roughness as a function of land cover and soil hyd group

                   Slope=Math.max(0.005,basinHillSlopesInfo.getHillslope(i));
                   // check this velocity!!! It should be btw 250 and 10 for forest and I just adapt the formula
                   vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]>=(0.5*25.4)) vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]<(0.5*25.4)) vr=0; //(m/h)
                   vflor=(1.4)*Math.pow(Slope,0.5)*100*0.3048;     //same as forest
                   //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;

           }
            double K=areasHillArray[0][i]*1000000/vr*2*(lengthArray[0][i]); //(m)

            double dist=areasHillArray[0][i]*1000000*0.5/(lengthArray[0][i]); //(m)
            double tim_sub=dist/vsub;     //hour
            qcsup=(input[i+nLi]/K); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
            qcsoil=(1/tim_sub)*input[i+2*nLi];  //mm/hour

            Q_trib=0.0;

            for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
            }

                switch (routingType) {
                    case 2:     K_Q=1.0/lengthArray[0][i];
                    break;
                    case 5:     K_Q=CkArray[0][i]*Math.pow(input[i],lamda1)*Math.pow(upAreasArray[0][i],lamda2)*Math.pow(lengthArray[0][i],-1);
                 }

            if (input[i] == 0) K_Q=0.1/(lengthArray[0][i]);

            double ks=0/3.6e6;
            double chanLoss=lengthArray[0][i]*widthArray[0][i]*ks;

            output[i]=60*K_Q*((1/3.6*areasHillArray[0][i]*(qcsup+qcsoil))+Q_trib-input[i]-chanLoss); //[m3/s]/min
            output[i+nLi]=(1/60.)*(qso-qcsup); //output[mm/min] qd and qs [mm/hour]
            output[i+2*nLi]=(1/60.)*(qs1-qcsoil); //output[mm/min] effPrecip and qss [mm/hour]
            }
            break ;


        case 2 :    /* MISHRA-SINGH METHOD - ACCOUNT FOR SOIL CONDITION - WITH DELAY FUNCTION*/

        for (int i=0;i<input.length;i++){if (input[i] < 0.0) input[i]=0.0;}

        for (int i=0;i<nLi;i++){

            double hillPrecIntensity=basinHillSlopesInfo.precipitation(i,time);// for URP event apply this rule*0.143;
            double hillAcumevent=basinHillSlopesInfo.precipitationacum(i,time);
            double hillAcum5days=0.0;
            int iflag=-9;
            double IA=-9.9;
            double S=-9.9;
            double lambda=0.05;
              S=basinHillSlopesInfo.SCS_S2(i);
              IA=lambda*S;
            if(IA<0) IA=0;
            if(S<0) S=0;

            double ratio=0.0;
            // indication that it is raining - hillAcumPreccurr>hillAcumPrecprev

            if(hillPrecIntensity>0.0f && (hillAcum5days+hillAcumevent)>IA)
            { /// This is the SCS equation, where qd=(Pacum-IA)^2/(Pacum-IA-S)
                double Pe=hillAcumevent-IA;
                double M=0.5*(-(1+lambda)*S+Math.pow((Math.pow((1-lambda),2)*Math.pow(S,2)+4*(Pe+hillAcum5days+IA)*S),0.5));
                double DQ_DM=(Pe-(((Pe)*(Pe+M))/(Pe+M+S)));
                double DM_DPe=S*Math.pow((Math.pow((1-lambda),2)*Math.pow(S,2)+4*(Pe+hillAcum5days+IA)*S),-0.5);
                qso=DQ_DM*DM_DPe*hillPrecIntensity;    /// this would guarantee it is in mm/h
                if(qso<0) qso=0;
                qs1=hillPrecIntensity-qso;
                if(qs1<0) qs1=0;
                if (i==119) System.out.println("qso"+qso+" S="+ S+" IA= "+IA+"M= "+M+ "DQ_DM="+DQ_DM+ " DM_DPe="+DM_DPe+ " DQ_DM*DM_DPe="+DQ_DM*DM_DPe+ "hillPrecIntensity="+hillPrecIntensity);
            }
            else
            { // if qacum<IA - qd=0;
                qso=0;
                qs1=0;
                if(hillPrecIntensity>0) qs1=hillPrecIntensity;
            }

           double vr=1.0;
           double Slope=-9.0;
           double vflor=-9;
           switch (HillVelType) {
                case 0: //constant
                  vr=vrunoff;
                  break;

               case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                   Slope=basinHillSlopesInfo.getHillslope(i);
                   vr=(1/basinHillSlopesInfo.HillManning(i))*Math.pow(Slope,0.5)*Math.pow(input[i+nLi]/1000,(2/3))*3600; //(m/h)
                  //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;
               case 2: //as a function of land cover
                  if(basinHillSlopesInfo.LandUseSCS(i)==0) vr=500.0; // water
                  if(basinHillSlopesInfo.LandUseSCS(i)==1) vr=250.0; // urban area
                  if(basinHillSlopesInfo.LandUseSCS(i)==2) vr=100.0; // baren soil
                  if(basinHillSlopesInfo.LandUseSCS(i)==3) vr=10.0; // Forest
                  if(basinHillSlopesInfo.LandUseSCS(i)==4) vr=100.0; // Shrubland
                  if(basinHillSlopesInfo.LandUseSCS(i)==5) vr=20.0; // Non-natural woody/Orchards
                  if(basinHillSlopesInfo.LandUseSCS(i)==6) vr=100.0; // Grassland
                  if(basinHillSlopesInfo.LandUseSCS(i)==7) vr=20.0; // Row Crops
                  if(basinHillSlopesInfo.LandUseSCS(i)==8) vr=100.0; // Pasture/Small Grains
                  if(basinHillSlopesInfo.LandUseSCS(i)==9) vr=50.0; // Wetlands
               break;
               case 3: //manning equation - manning roughness as a function of land cover and soil hyd group

                   Slope=Math.max(0.005,basinHillSlopesInfo.getHillslope(i));
                   // check this velocity!!! It should be btw 250 and 10 for forest and I just adapt the formula
                   vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]>=(0.5*25.4)) vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]<(0.5*25.4)) vr=0; //(m/h)
                   vflor=(1.4)*Math.pow(Slope,0.5)*100*0.3048;     //same as forest
                   //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;

           }
            double K=areasHillArray[0][i]*1000000/vr*2*(lengthArray[0][i]); //(m)

            double dist=areasHillArray[0][i]*1000000*0.5/(lengthArray[0][i]); //(m)
            double tim_sub=dist/vsub;     //hour
            qcsup=(input[i+nLi]/K); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
            qcsoil=(1/tim_sub)*input[i+2*nLi];  //mm/hour

            Q_trib=0.0;

            for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
            }

                switch (routingType) {
                    case 2:     K_Q=1.0/lengthArray[0][i];
                    break;
                    case 5:     K_Q=CkArray[0][i]*Math.pow(input[i],lamda1)*Math.pow(upAreasArray[0][i],lamda2)*Math.pow(lengthArray[0][i],-1);
                 }

            if (input[i] == 0) K_Q=0.1/(lengthArray[0][i]);

            double ks=0/3.6e6;
            double chanLoss=lengthArray[0][i]*widthArray[0][i]*ks;

            output[i]=60*K_Q*((1/3.6*areasHillArray[0][i]*(qcsup+qcsoil))+Q_trib-input[i]-chanLoss); //[m3/s]/min
            output[i+nLi]=(1/60.)*(qso-qcsup); //output[mm/min] qd and qs [mm/hour]
            output[i+2*nLi]=(1/60.)*(qs1-qcsoil); //output[mm/min] effPrecip and qss [mm/hour]

        }
            break ;

            case 3 :    /* CLAUDE MICHEL,2005 -SCS CN Method: How to mend a wrong soil moisture accounting*/

            break ;

            case 4 :
               /* Luciana - Considering soil and soil avaliability equal to
                * Soil storage for AMC =1 - estimated using curve number
                * This is the SCS equation, where Qacum=(Pacum-IA)^2/(Pacum-IA+St)
                * Pe=Pacum-IA
                * So=(SI-M)
                * dQ/dt=(Pe*(Pe+2So)/(Pe+So)^2)*dPe/dt
                * SI=maximum infiltration capacity = f(soil moisture, soil hydrologic group, dry soil)
                * M=soil moisture at time t
                */
                for (int i=0;i<input.length;i++){if (input[i] < 0.0) input[i]=0.0;}

                for (int i=0;i<nLi;i++){

                double hillPrecIntensity=basinHillSlopesInfo.precipitation(i,time);// for URP event apply this rule*0.143;
                double hillAcumevent=basinHillSlopesInfo.precipitationacum(i,time);
                double hillAcum5days=0.0;
                int iflag=-9;
                double IA=-9.9;
                double S=-9.9;
                double lambda=0.05;
                S=basinHillSlopesInfo.SCS_S1(i); // maximum storage - considering basin was dry
                IA=lambda*S;
                if(IA<0) IA=0;
                if(S<0) S=0;

                double ratio=0.0;
            // indication that it is raining - hillAcumPreccurr>hillAcumPrecprev
           double C=0;
           double Pe=hillAcumevent-IA;
           if(hillPrecIntensity>0.0f && (Pe)>0)
             {
                if(input[i+2*nLi]>=S) C=1;
                if(input[i+2*nLi]<S)  C=Pe*(Pe+2*S-2*input[i+2*nLi])/Math.pow((Pe+S-input[i+2*nLi]),2);
                  qso=C*hillPrecIntensity;    /// this would guarantee it is in mm/h
                  if(qso<0) qso=0;
                  qs1=hillPrecIntensity-qso;
                  if(qs1<0) qs1=0;
                 // System.out.println("Link =" + i + "C= "+C+"hillAcumevent=" + hillAcumevent+" Pe ="+Pe+" S = "+S+" So = "+input[i+2*nLi]+" So/S = "+input[i+2*nLi]/S);
             }
            else
             { // if qacum<IA - qd=0;
               qso=0;
               qs1=0;
               if(input[i+2*nLi]<S) qs1=hillPrecIntensity;
               if(input[i+2*nLi]>=S) qso=hillPrecIntensity;
            }

           // if (i==119)System.out.println("Link =" + i + "C= "+C+"hillAcumevent=" + hillAcumevent+" Pe ="+Pe+" S = "+S+" So = "+input[i+2*nLi]+" So/S = "+input[i+2*nLi]/S);
           double vr=1.0;
           double Slope=-9.0;
           double vflor=-9;
           switch (HillVelType) {
                case 0: //constant
                  vr=vrunoff;
                  break;

               case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                   Slope=basinHillSlopesInfo.getHillslope(i);
                   vr=(1/basinHillSlopesInfo.HillManning(i))*Math.pow(Slope,0.5)*Math.pow(input[i+nLi]/1000,(2/3))*3600; //(m/h)
                  //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;
               case 2: //as a function of land cover
                  if(basinHillSlopesInfo.LandUseSCS(i)==0) vr=500.0; // water
                  if(basinHillSlopesInfo.LandUseSCS(i)==1) vr=250.0; // urban area
                  if(basinHillSlopesInfo.LandUseSCS(i)==2) vr=100.0; // baren soil
                  if(basinHillSlopesInfo.LandUseSCS(i)==3) vr=10.0; // Forest
                  if(basinHillSlopesInfo.LandUseSCS(i)==4) vr=100.0; // Shrubland
                  if(basinHillSlopesInfo.LandUseSCS(i)==5) vr=20.0; // Non-natural woody/Orchards
                  if(basinHillSlopesInfo.LandUseSCS(i)==6) vr=100.0; // Grassland
                  if(basinHillSlopesInfo.LandUseSCS(i)==7) vr=20.0; // Row Crops
                  if(basinHillSlopesInfo.LandUseSCS(i)==8) vr=100.0; // Pasture/Small Grains
                  if(basinHillSlopesInfo.LandUseSCS(i)==9) vr=50.0; // Wetlands
               break;
               case 3: //manning equation - manning roughness as a function of land cover and soil hyd group

                   Slope=Math.max(0.005,basinHillSlopesInfo.getHillslope(i));
                   // check this velocity!!! It should be btw 250 and 10 for forest and I just adapt the formula
                   vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]>=(0.5*25.4)) vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]<(0.5*25.4)) vr=0; //(m/h)
                   vflor=(1.4)*Math.pow(Slope,0.5)*100*0.3048;     //same as forest
                   //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;

           }

            double K=areasHillArray[0][i]*1000000/(vr*2*lengthArray[0][i]); //(m)

            double dist=areasHillArray[0][i]*1000000/(2*lengthArray[0][i]); //(m)
            double tim_sub=dist/vsub;     //hour

            qcsup=(input[i+nLi]/K); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
            qcsoil=(1/tim_sub)*input[i+2*nLi];  //mm/hour
            /*if(hillPrecIntensity>0.0f && (Pe)>0){
                System.out.println("Slope/100 =" + (Slope/100) +" basinHillSlopesInfo.Hill_K_NRCS(i) ="+basinHillSlopesInfo.Hill_K_NRCS(i));
                System.out.println("Link =" + i +" Scapac ="+S+" Scond = "+input[i+2*nLi]);
                System.out.println("Prec =" + hillPrecIntensity+ "qso =" + qso + "qs1 =" + qs1);
                System.out.println("qcsup =" + qcsup+ "qcsoil =" + qcsoil +"\n");
                System.out.println("K =" + K+"tim_sub"+tim_sub+"   vr= "+ vr +"  vsub="+vsub);
            }*/
            Q_trib=0.0;

            for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
            }

                switch (routingType) {
                    case 2:     K_Q=1.0/lengthArray[0][i];
                    break;
                    case 5:     K_Q=CkArray[0][i]*Math.pow(input[i],lamda1)*Math.pow(upAreasArray[0][i],lamda2)*Math.pow(lengthArray[0][i],-1);
                 }

            if (input[i] == 0) K_Q=0.1/(lengthArray[0][i]);

            double ks=0/3.6e6;
            double chanLoss=lengthArray[0][i]*widthArray[0][i]*ks;

            output[i]=60*K_Q*((1/3.6*areasHillArray[0][i]*(qcsup+qcsoil))+Q_trib-input[i]-chanLoss); //[m3/s]/min
            output[i+nLi]=(1/60.)*(qso-qcsup); //output[mm/min] qd and qs [mm/hour]
            output[i+2*nLi]=(1/60.)*(qs1-qcsoil); //output[mm/min] effPrecip and qss [mm/hour]
            output[i+3*nLi]=(1/60.)*qcsup; // runoff rate mm/min
            output[i+4*nLi]=(1/60.)*qcsoil; //subsurface flow mm/min
            output[i+5*nLi]=(1/60.)*hillPrecIntensity; //accumulated precipitation mm/min
 //if (i==119)System.out.println("hillPrecIntensity =" + hillPrecIntensity + "    qcsup= "+qcsup+ "    qcsoil= "+qcsoil+ "    qso "+qso+ "    qs1= "+qs1 +"\n");
            //           if (i==1462)
 //          {try{

//FileWriter fstream = new FileWriter("C:/CUENCAS/Charlote/results/variableTest4.txt",true);
//BufferedWriter out = new BufferedWriter(fstream);
//out.write(time + " "+ C +" "+ Pe +" "+ hillPrecIntensity+ " " + hillAcumevent+ " " + " " + input[i] + " " +  input[i+nLi] + " " + input[i+2*nLi]  + " " + qso+ " " + qs1+ " " + output[i] + " " + output[i+nLi]+ " " +output[i+2*nLi]  + " " + qso +  " " + qs1+  " " +qcsup +  " " + qcsoil+ " " + Q_trib+ " " +IA + " " + S + "\n");
           //Close the output stream
//out.close();
//    }catch (Exception e){//Catch exception if any
//    System.err.println("Error: " + e.getMessage());
//    }}
       }
            break ;
       case 5 :    /* Luciana - Considering soil and soil avaliability equal to
                * STATSCO available water storage for depth =150cm
                * This is the SCS equation, where Qacum=(Pacum-IA)^2/(Pacum-IA+St)
                * Pe=Pacum-IA
                * St=((S-M)/S)*SI
                * dQ/dt=(Pe*(Pe+2St)/(Pe+St)^2)*dPe/dt
                * SI=maximum infiltration capacity = f(soil moisture, soil hydrologic group, dry soil)
                * S=soil potential storage = STATSCO database
                * M=soil moisture at time t
                */

                for (int i=0;i<input.length;i++){if (input[i] < 0.0) input[i]=0.0;}

                for (int i=0;i<nLi;i++){

                double hillPrecIntensity=basinHillSlopesInfo.precipitation(i,time);// for URP event apply this rule*0.143;
                double hillAcumevent=basinHillSlopesInfo.precipitationacum(i,time);
                double hillAcum5days=0.0;
                int iflag=-9;
                double IA=-9.9;
                double S=-9.9;
                double SI=-9.9;
                double lambda=0.05;
                S=basinHillSlopesInfo.SCS_S2(i); // maximum storage - change to statsco value
                SI=basinHillSlopesInfo.SCS_S1(i); // maximum infiltration when M=0
                IA=lambda*S;
                if(IA<0) IA=0;
                if(S<0) S=0;

                double ratio=0.0;
            // indication that it is raining - hillAcumPreccurr>hillAcumPrecprev
           double C=0;
           double Pe=hillAcumevent-IA;
           if(hillPrecIntensity>0.0f && (Pe)>0)
             {
               double St=((S-input[i+2*nLi])/S)*SI;
                if(input[i+2*nLi]>=S) C=1;
                if(input[i+2*nLi]<S)  C=Pe*(Pe+2*St)/Math.pow((Pe+St),2);
                  qso=C*hillPrecIntensity;    /// this would guarantee it is in mm/h
                  if(qso<0) qso=0;
                  qs1=hillPrecIntensity-qso;
                  if(qs1<0) qs1=0;
                 // System.out.println("Link =" + i + "C= "+C+"hillAcumevent=" + hillAcumevent+" Pe ="+Pe+" S = "+S+" So = "+input[i+2*nLi]+" So/S = "+input[i+2*nLi]/S);
             }
            else
             { // if qacum<IA - qd=0;
               qso=0;
               qs1=0;
               if(input[i+2*nLi]<S) qs1=hillPrecIntensity;
               if(input[i+2*nLi]>=S) qso=hillPrecIntensity;
            }

           double vr=1.0;
           double Slope=-9.0;
           double vflor=-9;
           switch (HillVelType) {
                case 0: //constant
                  vr=vrunoff;
                  break;

               case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                   Slope=basinHillSlopesInfo.getHillslope(i);
                   vr=(1/basinHillSlopesInfo.HillManning(i))*Math.pow(Slope,0.5)*Math.pow(input[i+nLi]/1000,(2/3))*3600; //(m/h)
                  //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;
               case 2: //as a function of land cover
                  if(basinHillSlopesInfo.LandUseSCS(i)==0) vr=500.0; // water
                  if(basinHillSlopesInfo.LandUseSCS(i)==1) vr=250.0; // urban area
                  if(basinHillSlopesInfo.LandUseSCS(i)==2) vr=100.0; // baren soil
                  if(basinHillSlopesInfo.LandUseSCS(i)==3) vr=10.0; // Forest
                  if(basinHillSlopesInfo.LandUseSCS(i)==4) vr=100.0; // Shrubland
                  if(basinHillSlopesInfo.LandUseSCS(i)==5) vr=20.0; // Non-natural woody/Orchards
                  if(basinHillSlopesInfo.LandUseSCS(i)==6) vr=100.0; // Grassland
                  if(basinHillSlopesInfo.LandUseSCS(i)==7) vr=20.0; // Row Crops
                  if(basinHillSlopesInfo.LandUseSCS(i)==8) vr=100.0; // Pasture/Small Grains
                  if(basinHillSlopesInfo.LandUseSCS(i)==9) vr=50.0; // Wetlands
               break;
               case 3: //NRCS method - function of land cover and soil hyd group

                   Slope=Math.max(0.005,basinHillSlopesInfo.getHillslope(i));
                   // check this velocity!!! It should be btw 250 and 10 for forest and I just adapt the formula
                   vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(vr>250) vr=500;
                   if(vr<10) vr=10;
                   // version with reservoir
                   //if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]>=(0.5*25.4)) vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   //if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]<(0.5*25.4)) vr=0; //(m/h)
                   //vflor=(1.4)*Math.pow(Slope,0.5)*100*0.3048;     //same as forest
                   //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;

           }
            double K=areasHillArray[0][i]*1000000/vr*2*(lengthArray[0][i]); //(m)

            double dist=areasHillArray[0][i]*1000000*0.5/(lengthArray[0][i]); //(m)
            double tim_sub=dist/vsub;     //hour
            qcsup=(input[i+nLi]/K); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
            qcsoil=(1/tim_sub)*input[i+2*nLi];  //mm/hour
 

            Q_trib=0.0;

            for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
            }

                switch (routingType) {
                    case 2:     K_Q=1.0/lengthArray[0][i];
                    break;
                    case 5:     K_Q=CkArray[0][i]*Math.pow(input[i],lamda1)*Math.pow(upAreasArray[0][i],lamda2)*Math.pow(lengthArray[0][i],-1);
                 }

            if (input[i] == 0) K_Q=0.1/(lengthArray[0][i]);

            double ks=0/3.6e6;
            double chanLoss=lengthArray[0][i]*widthArray[0][i]*ks;

            output[i]=60*K_Q*((1/3.6*areasHillArray[0][i]*(qcsup+qcsoil))+Q_trib-input[i]-chanLoss); //[m3/s]/min
            output[i+nLi]=(1/60.)*(qso-qcsup); //output[mm/min] qd and qs [mm/hour]
            output[i+2*nLi]=(1/60.)*(qs1-qcsoil); //output[mm/min] effPrecip and qss [mm/hour]
            output[i+3*nLi]=(1/60.)*qcsup; // runoff rate mm/min
            output[i+4*nLi]=(1/60.)*qcsoil; //subsurface flow mm/min
            output[i+5*nLi]=(1/60.)*hillPrecIntensity; //accumulated precipitation mm/min

            //if (i==1462) System.out.println("OutputDir="+OutputDir);
           //try{

//FileWriter fstream = new FileWriter("C:/CUENCAS/Charlote/results/variableTest4.txt",true);
//BufferedWriter out = new BufferedWriter(fstream);
//out.write(time + " "+ C +" "+ Pe +" "+ hillPrecIntensity+ " " + hillAcumevent+ " " + " " + input[i] + " " +  input[i+nLi] + " " + input[i+2*nLi]  + " " + qso+ " " + qs1+ " " + output[i] + " " + output[i+nLi]+ " " +output[i+2*nLi]  + " " + qso +  " " + qs1+  " " +qcsup +  " " + qcsoil+ " " + Q_trib+ " " +IA + " " + S + "\n");
           //Close the output stream
//out.close();

    //}catch (Exception e){//Catch exception if any
//    System.err.println("Error: " + e.getMessage());
//    }}
       }
            break ;
            case 6 :
               /* Luciana - Considering soil and soil avaliability equal to
                * Soil storage for AMC =1 - estimated using curve number
                * This is the SCS equation, where Qacum=(Pacum-IA)^2/(Pacum-IA+St)
                * Pe=Pacum-IA
                * So=(SI-M)
                * dQ/dt=(Pe*(Pe+2So)/(Pe+So)^2)*dPe/dt
                * SI=maximum infiltration capacity = f(soil moisture, soil hydrologic group, dry soil)
                * M=soil moisture at time t
                *The same as 4 but in this case I insert the reservoir - Vo is the amount of reservation in the hillslope
                * in [mm] - the parameter is initially given as mm in the SimulationtoFileLuciana
                */
                for (int i=0;i<input.length;i++){if (input[i] < 0.0) input[i]=0.0;}

                for (int i=0;i<nLi;i++){

                double hillPrecIntensity=basinHillSlopesInfo.precipitation(i,time);// for URP event apply this rule*0.143;
                double hillAcumevent=basinHillSlopesInfo.precipitationacum(i,time);
                double Vcapac=Vo[0][i];
                double GPorc=Gr[0][i];
                double hillAcum5days=0.0;
                int iflag=-9;
                double IA=-9.9;
                double S=-9.9;
                double lambda=0.05;
                S=basinHillSlopesInfo.SCS_S1(i); // maximum storage - considering basin was dry
                IA=lambda*S;
                if(IA<0) IA=0;
                if(S<0) S=0;


                double ratio=0.0;
                // indication that it is raining - hillAcumPreccurr>hillAcumPrecprev
                double C=0;
                double Pe=hillAcumevent-IA;

                if(hillPrecIntensity>0.0f && (Pe)>0)
                  {
                  if(input[i+2*nLi]>=S) C=1;
                  if(input[i+2*nLi]<S)  C=Pe*(Pe+2*S-2*input[i+2*nLi])/Math.pow((Pe+S-input[i+2*nLi]),2);
                  double avaWater= C*hillPrecIntensity;
                  double avaRes=Vcapac-input[i+6*nLi];

                  if(input[i+6*nLi]<Vcapac) {q_in_res=avaWater;qso=0.0;}
                  else{qso=avaWater; q_in_res=0;}    /// this would guarantee it is in mm/h
                  qs1=hillPrecIntensity-qso-q_in_res;
                  if(qs1<0) qs1=0;
                 }
            else
                 { // if qacum<IA - Pe==0;
                 qso=0;
                 qs1=0;
                 q_in_res=0;
                 if(hillPrecIntensity>0){
                    if(input[i+2*nLi]<S) {qs1=hillPrecIntensity;qso=0;}
                    if(input[i+2*nLi]>=S)
                       {
                       qs1=0;
                       double avaWater= hillPrecIntensity;
                       double avaRes=Vcapac-input[i+6*nLi];
                       if(input[i+6*nLi]<Vcapac) {q_in_res=avaWater;qso=0.0;}
                       else{qso=avaWater; q_in_res=0;}    /// this would guarantee it is in mm/h
                       if(qso<0) qso=0;
                       if(qs1<0) qs1=0;
                       }
                   }
                 }
     
           // if (i==119)System.out.println("Link =" + i + "C= "+C+"hillAcumevent=" + hillAcumevent+" Pe ="+Pe+" S = "+S+" So = "+input[i+2*nLi]+" So/S = "+input[i+2*nLi]/S);
           double vr=1.0;
           double Slope=-9.0;
           double vflor=-9;
           switch (HillVelType) {
                case 0: //constant
                  vr=vrunoff;
                  break;

               case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                   Slope=basinHillSlopesInfo.getHillslope(i);
                   vr=(1/basinHillSlopesInfo.HillManning(i))*Math.pow(Slope,0.5)*Math.pow(input[i+nLi]/1000,(2/3))*3600; //(m/h)
                  //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;
               case 2: //as a function of land cover
                  if(basinHillSlopesInfo.LandUseSCS(i)==0) vr=500.0; // water
                  if(basinHillSlopesInfo.LandUseSCS(i)==1) vr=250.0; // urban area
                  if(basinHillSlopesInfo.LandUseSCS(i)==2) vr=100.0; // baren soil
                  if(basinHillSlopesInfo.LandUseSCS(i)==3) vr=10.0; // Forest
                  if(basinHillSlopesInfo.LandUseSCS(i)==4) vr=100.0; // Shrubland
                  if(basinHillSlopesInfo.LandUseSCS(i)==5) vr=20.0; // Non-natural woody/Orchards
                  if(basinHillSlopesInfo.LandUseSCS(i)==6) vr=100.0; // Grassland
                  if(basinHillSlopesInfo.LandUseSCS(i)==7) vr=20.0; // Row Crops
                  if(basinHillSlopesInfo.LandUseSCS(i)==8) vr=100.0; // Pasture/Small Grains
                  if(basinHillSlopesInfo.LandUseSCS(i)==9) vr=50.0; // Wetlands
               break;
               case 3: //manning equation - manning roughness as a function of land cover and soil hyd group

                   Slope=Math.max(0.005,basinHillSlopesInfo.getHillslope(i));
                   // check this velocity!!! It should be btw 250 and 10 for forest and I just adapt the formula
                   vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]>=(0.5*25.4)) vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)
                   if(basinHillSlopesInfo.LandUse(i)==10 && input[i+2*nLi]<(0.5*25.4)) vr=0; //(m/h)
                   vflor=(1.4)*Math.pow(Slope,0.5)*100*0.3048;     //same as forest
                   //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;

           }
            //System.out.println("K_NRCS(i)"+basinHillSlopesInfo.Hill_K_NRCS(i)+"slope = "+Slope+"  vr =" + vr + "landuse= "+basinHillSlopesInfo.LandUse(i));
            double K=areasHillArray[0][i]*1000000/(vr*2*lengthArray[0][i]); //(m)
            double Kres=areasHillArray[0][i]*1000000/(vflor*2*lengthArray[0][i]); //(m)
            double dist=areasHillArray[0][i]*1000000/(2*lengthArray[0][i]); //(m)
            double tim_sub=dist/vsub;     //hour
            

            qcsup=(input[i+nLi]/K); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
            qcsoil=(1/tim_sub)*input[i+2*nLi];  //mm/hour
            q_out_res=(input[i+6*nLi]/Kres);  //mm/hour

            /*if(hillPrecIntensity>0.0f && (Pe)>0){
                System.out.println("Link =" + i + "Vcapac= "+Vcapac+"Vcond" + input[i+6*nLi]+" Scapac ="+S+" Scond = "+input[i+2*nLi]+"\n");
                System.out.println("Prec =" + hillPrecIntensity+ "qso =" + qso + "qs1 =" + qs1+"q_in_res =" + q_in_res+"\n");
                System.out.println("qcsup =" + qcsup+ "qcsoil =" + qcsoil + "q_out_res =" + q_out_res+"\n");
                System.out.println("Kres =" +Kres+ "K =" + K+"tim_sub"+tim_sub+"\n");
            }*/
            Q_trib=0.0;

            for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
            }

                switch (routingType) {
                    case 2:     K_Q=1.0/lengthArray[0][i];
                    break;
                    case 5:     K_Q=CkArray[0][i]*Math.pow(input[i],lamda1)*Math.pow(upAreasArray[0][i],lamda2)*Math.pow(lengthArray[0][i],-1);
                 }

            if (input[i] == 0) K_Q=0.1/(lengthArray[0][i]);

            double ks=0/3.6e6;
            double chanLoss=lengthArray[0][i]*widthArray[0][i]*ks;

            output[i]=60*K_Q*((1/3.6*areasHillArray[0][i]*(qcsup+qcsoil+q_out_res))+Q_trib-input[i]-chanLoss); //[m3/s]/min
            output[i+nLi]=(1/60.)*(qso-qcsup); //output[mm/min] qd and qs [mm/hour]
            output[i+2*nLi]=(1/60.)*(qs1-qcsoil); //output[mm/min] effPrecip and qss [mm/hour]
            output[i+3*nLi]=(1/60.)*qcsup; // runoff rate mm/min
            output[i+4*nLi]=(1/60.)*qcsoil; //subsurface flow mm/min
            output[i+5*nLi]=(1/60.)*hillPrecIntensity; //accumulated precipitation mm/min
            output[i+6*nLi]=(1/60.)*(q_in_res-q_out_res); //accumulated precipitation mm/min
            //if (i==119)System.out.println("hillPrecIntensity =" + hillPrecIntensity + "    qcsup= "+qcsup+ "    qcsoil= "+qcsoil+ "    qso "+qso+ "    qs1= "+qs1 +"\n");
            //           if (i==1462)
 //          {try{

//FileWriter fstream = new FileWriter("C:/CUENCAS/Charlote/results/variableTest4.txt",true);
//BufferedWriter out = new BufferedWriter(fstream);
//out.write(time + " "+ C +" "+ Pe +" "+ hillPrecIntensity+ " " + hillAcumevent+ " " + " " + input[i] + " " +  input[i+nLi] + " " + input[i+2*nLi]  + " " + qso+ " " + qs1+ " " + output[i] + " " + output[i+nLi]+ " " +output[i+2*nLi]  + " " + qso +  " " + qs1+  " " +qcsup +  " " + qcsoil+ " " + Q_trib+ " " +IA + " " + S + "\n");
           //Close the output stream
//out.close();
//    }catch (Exception e){//Catch exception if any
//    System.err.println("Error: " + e.getMessage());
//    }}
       }
            break ;

case 7 :
               /* Luciana - Considering soil and soil avaliability equal to
                * Soil storage for AMC =1 - estimated using curve number
                * This is the SCS equation, where Qacum=(Pacum-IA)^2/(Pacum-IA+St)
                * Pe=Pacum-IA
                * So=(SI-M)
                * dQ/dt=(Pe*(Pe+2So)/(Pe+So)^2)*dPe/dt
                * SI=maximum infiltration capacity = f(soil moisture, soil hydrologic group, dry soil)
                * M=soil moisture at time t
                *The same as 4 but in this case I insert the greenroof - Go is the amount of reservation in the hillslope
                * in [mm] - the parameter is initially given as porcentage of area
                */
                for (int i=0;i<input.length;i++){if (input[i] < 0.0) input[i]=0.0;}

                for (int i=0;i<nLi;i++){

                double hillPrecIntensity=basinHillSlopesInfo.precipitation(i,time);// for URP event apply this rule*0.143;
                double hillAcumevent=basinHillSlopesInfo.precipitationacum(i,time);
                double GPorc=Gr[0][i];
                double hillAcum5days=0.0;
                int iflag=-9;
                double IA=-9.9;
                double S=-9.9;
                double lambda=0.05;
                S=basinHillSlopesInfo.SCS_S1(i); // maximum storage - considering basin was dry
                IA=lambda*S;
                if(IA<0) IA=0;
                if(S<0) S=0;


                double ratio=0.0;
                // indication that it is raining - hillAcumPreccurr>hillAcumPrecprev
                double C=0;
                double Pe=hillAcumevent-IA;

                if(hillPrecIntensity>0.0f && (Pe)>0)
                  {
                  if(input[i+2*nLi]>=S) C=1;
                  if(input[i+2*nLi]<S)  C=Pe*(Pe+2*S-2*input[i+2*nLi])/Math.pow((Pe+S-input[i+2*nLi]),2);
                  double avaWater= C*hillPrecIntensity;


                  if(input[i+6*nLi]<500) {q_in_res=GPorc*avaWater;qso=(1-GPorc)*avaWater;} // considers that the capacity of the roof is 50cm=500mm
                  else{qso=avaWater; q_in_res=0;}    /// this would guarantee it is in mm/h
                  qs1=hillPrecIntensity-qso-q_in_res;
                  if(qs1<0) qs1=0;
                 }
            else
                 { // if qacum<IA - Pe==0;
                 qso=0;
                 qs1=0;
                 q_in_res=0;
                 if(hillPrecIntensity>0){
                    if(input[i+2*nLi]<S) {qs1=hillPrecIntensity;qso=0;}
                    if(input[i+2*nLi]>=S)
                       {
                       qs1=0;
                       double avaWater= hillPrecIntensity;

                       if(input[i+6*nLi]<500) {q_in_res=GPorc*avaWater;qso=(1-GPorc)*avaWater;} // considers that the capacity of the roof is 50cm=500mm
                       else{qso=avaWater; q_in_res=0;}    /// this would guarantee it is in mm/h
                       if(qso<0) qso=0;
                       if(qs1<0) qs1=0;
                       }
                   }
                 }

           // if (i==119)System.out.println("Link =" + i + "C= "+C+"hillAcumevent=" + hillAcumevent+" Pe ="+Pe+" S = "+S+" So = "+input[i+2*nLi]+" So/S = "+input[i+2*nLi]/S);
           double vr=1.0;
           double Slope=-9.0;
           double vflor=-9;
           switch (HillVelType) {
                case 0: //constant
                  vr=vrunoff;
                  break;

               case 1: //manning equation - manning roughness as a function of land cover and soil hyd group
                   Slope=basinHillSlopesInfo.getHillslope(i);
                   vr=(1/basinHillSlopesInfo.HillManning(i))*Math.pow(Slope,0.5)*Math.pow(input[i+nLi]/1000,(2/3))*3600; //(m/h)
                  //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;
               case 2: //as a function of land cover
                  if(basinHillSlopesInfo.LandUseSCS(i)==0) vr=500.0; // water
                  if(basinHillSlopesInfo.LandUseSCS(i)==1) vr=250.0; // urban area
                  if(basinHillSlopesInfo.LandUseSCS(i)==2) vr=100.0; // baren soil
                  if(basinHillSlopesInfo.LandUseSCS(i)==3) vr=10.0; // Forest
                  if(basinHillSlopesInfo.LandUseSCS(i)==4) vr=100.0; // Shrubland
                  if(basinHillSlopesInfo.LandUseSCS(i)==5) vr=20.0; // Non-natural woody/Orchards
                  if(basinHillSlopesInfo.LandUseSCS(i)==6) vr=100.0; // Grassland
                  if(basinHillSlopesInfo.LandUseSCS(i)==7) vr=20.0; // Row Crops
                  if(basinHillSlopesInfo.LandUseSCS(i)==8) vr=100.0; // Pasture/Small Grains
                  if(basinHillSlopesInfo.LandUseSCS(i)==9) vr=50.0; // Wetlands
               break;
               case 3: //manning equation - manning roughness as a function of land cover and soil hyd group

                   Slope=Math.max(0.005,basinHillSlopesInfo.getHillslope(i));
                   // check this velocity!!! It should be btw 250 and 10 for forest and I just adapt the formula
                   vr=(basinHillSlopesInfo.Hill_K_NRCS(i))*Math.pow((Slope),0.5)*100*0.3048; //(m/h)

                   vflor=(1.4)*Math.pow(Slope,0.5)*100*0.3048;     //same as forest
                   //m/h
                  //if(i==119) System.out.println("Link =" + i + "Manning(i)= "+basinHillSlopesInfo.HillManning(i)+"Slope" + Slope+" input[i+nLi] ="+input[i+nLi]+" vr = "+vr);
               break;

           }
            //System.out.println("K_NRCS(i)"+basinHillSlopesInfo.Hill_K_NRCS(i)+"slope = "+Slope+"  vr =" + vr + "landuse= "+basinHillSlopesInfo.LandUse(i));
            double K=areasHillArray[0][i]*1000000/(vr*2*lengthArray[0][i]); //(m)
            double Kres=areasHillArray[0][i]*1000000/(vflor*2*lengthArray[0][i]); //(m)
            double dist=areasHillArray[0][i]*1000000/(2*lengthArray[0][i]); //(m)
            double tim_sub=dist/vsub;     //hour


            qcsup=(input[i+nLi]/K); //mm/hour//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
            qcsoil=(1/tim_sub)*input[i+2*nLi];  //mm/hour
            q_out_res=(input[i+6*nLi]/Kres);  //mm/hour

            /*if(hillPrecIntensity>0.0f && (Pe)>0){
                System.out.println("Link =" + i + "Vcapac= "+Vcapac+"Vcond" + input[i+6*nLi]+" Scapac ="+S+" Scond = "+input[i+2*nLi]+"\n");
                System.out.println("Prec =" + hillPrecIntensity+ "qso =" + qso + "qs1 =" + qs1+"q_in_res =" + q_in_res+"\n");
                System.out.println("qcsup =" + qcsup+ "qcsoil =" + qcsoil + "q_out_res =" + q_out_res+"\n");
                System.out.println("Kres =" +Kres+ "K =" + K+"tim_sub"+tim_sub+"\n");
            }*/
            Q_trib=0.0;

            for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
            }

                switch (routingType) {
                    case 2:     K_Q=1.0/lengthArray[0][i];
                    break;
                    case 5:     K_Q=CkArray[0][i]*Math.pow(input[i],lamda1)*Math.pow(upAreasArray[0][i],lamda2)*Math.pow(lengthArray[0][i],-1);
                 }

            if (input[i] == 0) K_Q=0.1/(lengthArray[0][i]);

            double ks=0/3.6e6;
            double chanLoss=lengthArray[0][i]*widthArray[0][i]*ks;

            output[i]=60*K_Q*((1/3.6*areasHillArray[0][i]*(qcsup+qcsoil+q_out_res))+Q_trib-input[i]-chanLoss); //[m3/s]/min
            output[i+nLi]=(1/60.)*(qso-qcsup); //output[mm/min] qd and qs [mm/hour]
            output[i+2*nLi]=(1/60.)*(qs1-qcsoil); //output[mm/min] effPrecip and qss [mm/hour]
            output[i+3*nLi]=(1/60.)*qcsup; // runoff rate mm/min
            output[i+4*nLi]=(1/60.)*qcsoil; //subsurface flow mm/min
            output[i+5*nLi]=(1/60.)*hillPrecIntensity; //accumulated precipitation mm/min
            output[i+6*nLi]=(1/60.)*(q_in_res-q_out_res); //accumulated precipitation mm/min
            //if (i==119)System.out.println("hillPrecIntensity =" + hillPrecIntensity + "    qcsup= "+qcsup+ "    qcsoil= "+qcsoil+ "    qso "+qso+ "    qs1= "+qs1 +"\n");
            //           if (i==1462)
 //          {try{

//FileWriter fstream = new FileWriter("C:/CUENCAS/Charlote/results/variableTest4.txt",true);
//BufferedWriter out = new BufferedWriter(fstream);
//out.write(time + " "+ C +" "+ Pe +" "+ hillPrecIntensity+ " " + hillAcumevent+ " " + " " + input[i] + " " +  input[i+nLi] + " " + input[i+2*nLi]  + " " + qso+ " " + qs1+ " " + output[i] + " " + output[i+nLi]+ " " +output[i+2*nLi]  + " " + qso +  " " + qs1+  " " +qcsup +  " " + qcsoil+ " " + Q_trib+ " " +IA + " " + S + "\n");
           //Close the output stream
//out.close();
//    }catch (Exception e){//Catch exception if any
//    System.err.println("Error: " + e.getMessage());
//    }}
       }
            break ;

        }
        return output;
    }
    public double[] eval(double[] input, double time, double timeStep) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

