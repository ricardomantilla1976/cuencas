/*
 * NetworkEquations.java
 *
 * Created on November 11, 2001, 10:26 AM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 *
 * @author  ricardo 
 */
public class NetworkEquations_Simple implements hydroScalingAPI.util.ordDiffEqSolver.BasicFunction {
    private hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksConectionStruct;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo basinHillSlopesInfo;
    private hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linksHydraulicInfo;
    private int routingType;
    
    private double qd, effPrecip, qs, qe, Q_trib, K_Q;
    private double[] output;
    
    private float[][] manningArray, cheziArray, widthArray, lengthArray, slopeArray, CkArray;
    private float[][] areasHillArray, upAreasArray;
    private double So,Ts,Te; // not an array because I assume uniform soil properties
    
    private double lamda1,lamda2;
    
    /** Creates new NetworkEquations */
    public NetworkEquations_Simple(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis links, hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo hillinf, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo linkIn, int rt){
        linksConectionStruct=links;
        basinHillSlopesInfo=hillinf;
        linksHydraulicInfo=linkIn;
        routingType=rt;
        
        upAreasArray=linksHydraulicInfo.getUpStreamAreaArray();
        cheziArray=linksHydraulicInfo.getCheziArray();
        manningArray=linksHydraulicInfo.getManningArray();
        widthArray=linksHydraulicInfo.getWidthArray();
        lengthArray=linksHydraulicInfo.getLengthInKmArray();
        for (int i=0;i<lengthArray[0].length;i++) lengthArray[0][i]=lengthArray[0][i]*1000;
        slopeArray=linksHydraulicInfo.getSlopeArray();
        
        areasHillArray=basinHillSlopesInfo.getAreasArray();
        So=basinHillSlopesInfo.So(0);
        Ts=basinHillSlopesInfo.Ts(0);
        Te=basinHillSlopesInfo.Te(0);
        
        lamda1=linksHydraulicInfo.getLamda1();
        lamda2=linksHydraulicInfo.getLamda2();
        CkArray=linksHydraulicInfo.getCkArray();
        
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
        //the input's length is twice the number of links... the first half corresponds to links discharge and the second to hillslopes storage

        for (int i=0;i<input.length;i++){
            if (input[i] < 0.0) input[i]=0.0;
        }

        int nLi=linksConectionStruct.connectionsArray.length;

        output=new double[input.length];
        /*java.util.Calendar thisDate=java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long)(time*60.*1000.0));
        System.out.println("    "+thisDate.getTime()+" "+basinHillSlopesInfo.precipitation(0,time)+" "+input[287]);*/
        
        double maxInt=0;
        
        for (int i=0;i<nLi;i++){
            
            if (input[i] < 0) input[i]=0;
            
            double hillPrecIntensity=basinHillSlopesInfo.precipitation(i,time);
            
            maxInt=Math.max(maxInt,hillPrecIntensity);
            
            qd=Math.max(hillPrecIntensity-basinHillSlopesInfo.infiltRate(i,time),0.0);
            effPrecip=hillPrecIntensity-qd;
            
            qs=0.0;//((input[i+nLi] > So)?1:0)*(1/Ts*(input[i+nLi]-So));
            qe=((input[i+nLi] > 0)?1:0)*(1/Te*(input[i+nLi]));
            
            Q_trib=0.0;
            for (int j=0;j<linksConectionStruct.connectionsArray[i].length;j++){
                Q_trib+=input[linksConectionStruct.connectionsArray[i][j]];
            }
            
            switch (routingType) {
                
                case 0:     K_Q=3/2.*Math.pow(input[i],1/3.)
                                *Math.pow(cheziArray[0][i],2/3.)
                                *Math.pow(widthArray[0][i],-1/3.)
                                *Math.pow(lengthArray[0][i],-1)
                                *Math.pow(slopeArray[0][i],1/3.);

                            break;    

                case 1:     K_Q=3/2.*Math.pow(input[i],1/3.)
                                *Math.pow(10.0,2/3.)
                                *Math.pow(widthArray[0][i],-1/3.)
                                *Math.pow(lengthArray[0][i],-1)
                                *Math.pow(slopeArray[0][i],1/3.);

                            break;    

                case 2:     K_Q=1.0/lengthArray[0][i];
                            break;
                
                case 3:     K_Q=5/3.*Math.pow(input[i],2/5.)
                                *Math.pow(0.03,-3/5.)
                                *Math.pow(widthArray[0][i],-2/5.)
                                *Math.pow(lengthArray[0][i],-1)
                                *Math.pow(slopeArray[0][i],3/10.);
                            break;
                
                case 4:     K_Q=5/3.*Math.pow(input[i],2/5.)
                                *Math.pow(manningArray[0][i],-3/5.)
                                *Math.pow(widthArray[0][i],-2/5.)
                                *Math.pow(lengthArray[0][i],-1)
                                *Math.pow(slopeArray[0][i],3/10.);
                            break;
                case 5:     K_Q=CkArray[0][i]*Math.pow(input[i],lamda1)*Math.pow(upAreasArray[0][i],lamda2)*Math.pow(lengthArray[0][i],-1);
            }
            
            if (input[i] == 0) K_Q=0.1/(lengthArray[0][i]);
            
            /*if (Math.random() > 0.99) {
                float typDisch=Math.round(input[i]*10000)/10000.0f;
                float typVel=Math.round(K_Q*lengthArray[0][i]*100)/100.0f;
                long typDepth=Math.round(input[i]/typVel/widthArray[0][i]*100);
                long typWidth=Math.round(widthArray[0][i]*100);
                System.out.println("  --> !!  When Discharge is "+typDisch+" m^3/s, A typical Velocity-Depth-Width triplet is "+typVel+" m/s - "+typDepth+" cm - "+typWidth+" cm >> for Link "+i+" with upstream area : "+linksHydraulicInfo.upStreamArea(i)+" km^2");
            }*/
            
            //the links
            output[i]=60*K_Q*(1/3.6*areasHillArray[0][i]*(qd+qs)+Q_trib-input[i]);
            
            //the hillslopes
            output[i+linksConectionStruct.connectionsArray.length]=1/60.*(effPrecip-qs-qe);
            
        }
        
        //if (Math.random() > 0.99) if (maxInt>0) System.out.println("      --> The Max precipitation intensity is: "+maxInt);

        return output;
    }
    
}
