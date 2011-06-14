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
 * RKF.java
 *
 * Created on November 11, 2001, 10:23 AM
 */
package hydroScalingAPI.util.ordDiffEqSolver;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * An implementation of the Runge-Kutta-Felberg algorithm for solving non-linear ordinary
 * differential equations.  It uses a time step control algorithm to avoid numerical errors
 * while solving the equations
 * @author Ricardo Mantilla
 */
public class RKF extends java.lang.Object {

    hydroScalingAPI.util.ordDiffEqSolver.BasicFunction theFunction;
    /**
     * An array containing the value of the function that was last calculated by the
     * RKF algoritm
     */
    public double[] finalCond;
    double epsilon;
    double basicTimeStep;
    //Scheme parameters
    double[] a = {0., 1 / 5., 3 / 10., 3 / 5., 1., 7 / 8.};
    double[][] b = {
        {0.},
        {1 / 5.},
        {3 / 40., 9 / 40.},
        {3 / 10., -9 / 10., 6 / 5.},
        {-11 / 54., 5 / 2., -70 / 27., 35 / 27.},
        {1631 / 55296., 175 / 512., 575 / 13824., 44275 / 110592., 253 / 4096.}
    };
    double[] c = {37 / 378., 0., 250 / 621., 125 / 594., 0., 512 / 1771.};
    double[] cStar = {2825 / 27648., 0., 18575 / 48384., 13525 / 55296., 277 / 14336., 1 / 4.};
    double[] Derivs;
    double[] carrier, k0, k1, k2, k3, k4, k5, newY, newYstar, maxAchieved, timeOfMaximumAchieved;
    double Delta, newTimeStep, factor;

    /**
     * Creates new RKF
     * @param fu The differential equation to solve described by a {@link hydroScalingAPI.util.ordDiffEqSolver.BasicFunction}
     * @param eps The value error allowed by the step forward algorithm
     * @param basTs The step size
     */
    public RKF(hydroScalingAPI.util.ordDiffEqSolver.BasicFunction fu, double eps, double basTs) {
        theFunction = fu;
        epsilon = eps;
        basicTimeStep = basTs;
    }

    /**
     * Returns the value of the function described by differential equations in the
     * next time step
     * @param currentTime The current time
     * @param IC The value of the initial condition
     * @param timeStep The desired step size
     * @param finalize A boolean indicating in the timeStep provided is final or if 
     * it needs to be refined
     * @return The value of the multivatiate function
     */
    private double[][] step(double currentTime, double[] IC, double timeStep, boolean finalize) {

        //if first time call ever define array maxAchieved
        if (maxAchieved == null) {
            maxAchieved = new double[IC.length];
            timeOfMaximumAchieved = new double[IC.length];
            java.util.Arrays.fill(maxAchieved, Double.MIN_VALUE);
        }

        carrier = new double[IC.length];

        k0 = theFunction.eval(IC, currentTime);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * b[1][0] * k0[i]);
        }
        k1 = theFunction.eval(carrier, currentTime + a[1] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[2][0] * k0[i] + b[2][1] * k1[i]));
        }
        k2 = theFunction.eval(carrier, currentTime + a[2] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[3][0] * k0[i] + b[3][1] * k1[i] + b[3][2] * k2[i]));
        }
        k3 = theFunction.eval(carrier, currentTime + a[3] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[4][0] * k0[i] + b[4][1] * k1[i] + b[4][2] * k2[i] + b[4][3] * k3[i]));
        }
        k4 = theFunction.eval(carrier, currentTime + a[4] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[5][0] * k0[i] + b[5][1] * k1[i] + b[5][2] * k2[i] + b[5][3] * k3[i] + b[5][4] * k4[i]));
        }
        k5 = theFunction.eval(carrier, currentTime + a[5] * timeStep);

        newY = new double[IC.length];
        for (int i = 0; i < IC.length; i++) {
            newY[i] = IC[i] + timeStep * (c[0] * k0[i] + c[1] * k1[i] + c[2] * k2[i] + c[3] * k3[i] + c[4] * k4[i] + c[5] * k5[i]);
            newY[i] = Math.max(0, newY[i]);
        }

        newYstar = new double[IC.length];
        for (int i = 0; i < IC.length; i++) {
            newYstar[i] = IC[i] + timeStep * (cStar[0] * k0[i] + cStar[1] * k1[i] + cStar[2] * k2[i] + cStar[3] * k3[i] + cStar[4] * k4[i] + cStar[5] * k5[i]);
            newYstar[i] = Math.max(0, newYstar[i]);
        }

        Delta = 0;
        for (int i = 0; i < IC.length; i++) {
            if ((newY[i] + newYstar[i]) > 0) {
                Delta = Math.max(Delta, Math.abs(2 * (newY[i] - newYstar[i]) / (newY[i] + newYstar[i])));
            }
        }

        newTimeStep = timeStep;

        if (finalize) {

//            for (int i = 0; i < IC.length; i++) {
//                if(newY[i] > maxAchieved[i]){
//                    maxAchieved[i] = newY[i];
//                    timeOfMaximumAchieved[i]=currentTime;
//                }
//            }
            return new double[][]{{newTimeStep}, newY};
        } else {
            if (Delta != 0.0) {
                factor = epsilon / Delta;

                if (factor >= 1) {
                    newTimeStep = timeStep * Math.pow(factor, 0.15);
                } else {
                    newTimeStep = timeStep * Math.pow(factor, 0.25);
                }
            } else {
                factor = 1e5;
                newTimeStep = timeStep * Math.pow(factor, 0.15);
                finalize = true;
            }

            //System.out.println("    --> "+timeStep+" "+epsilon+" "+Delta+" "+factor+" "+newTimeStep+" ("+java.util.Calendar.getInstance().getTime()+")");
            if (newTimeStep < 0.01 / 60.) {
                newTimeStep = 0.01 / 60.;
            }
            return step(currentTime, IC, newTimeStep, true);
        }

    }

    private double[][] stepSCS(double currentTime, double[] IC, double timeStep, boolean finalize) throws IOException {

        //if first time call ever define array maxAchieved
        if (maxAchieved == null) {
            maxAchieved = new double[IC.length];
            timeOfMaximumAchieved = new double[IC.length];
            java.util.Arrays.fill(maxAchieved, Double.MIN_VALUE);
        }

        carrier = new double[IC.length];

        k0 = theFunction.eval(IC, currentTime);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * b[1][0] * k0[i]);
        }
        k1 = theFunction.eval(carrier, currentTime + a[1] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[2][0] * k0[i] + b[2][1] * k1[i]));
        }
        k2 = theFunction.eval(carrier, currentTime + a[2] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[3][0] * k0[i] + b[3][1] * k1[i] + b[3][2] * k2[i]));
        }
        k3 = theFunction.eval(carrier, currentTime + a[3] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[4][0] * k0[i] + b[4][1] * k1[i] + b[4][2] * k2[i] + b[4][3] * k3[i]));
        }
        k4 = theFunction.eval(carrier, currentTime + a[4] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[5][0] * k0[i] + b[5][1] * k1[i] + b[5][2] * k2[i] + b[5][3] * k3[i] + b[5][4] * k4[i]));
        }
        k5 = theFunction.eval(carrier, currentTime + a[5] * timeStep);

        newY = new double[IC.length];
        for (int i = 0; i < IC.length; i++) {
            newY[i] = IC[i] + timeStep * (c[0] * k0[i] + c[1] * k1[i] + c[2] * k2[i] + c[3] * k3[i] + c[4] * k4[i] + c[5] * k5[i]);
            newY[i] = Math.max(0, newY[i]);
        }

        newYstar = new double[IC.length];
        for (int i = 0; i < IC.length; i++) {
            newYstar[i] = IC[i] + timeStep * (cStar[0] * k0[i] + cStar[1] * k1[i] + cStar[2] * k2[i] + cStar[3] * k3[i] + cStar[4] * k4[i] + cStar[5] * k5[i]);
            newYstar[i] = Math.max(0, newYstar[i]);
        }
        int problink = 0;
        double Yprob = 0, Ystarprob = 0;
        Delta = 0;
        for (int i = 0; i < IC.length; i++) {
            //&& Math.abs(newY[i] - newYstar[i])>0.001
            
            if ((newY[i] + newYstar[i]) > 0.0) {
                double newdelta = Math.abs(2 * (newY[i] - newYstar[i]) / (newY[i] + newYstar[i]));
                if (Delta < newdelta) {
                    Delta = newdelta;
                    problink = i;
                    Yprob = newY[i];
                    Ystarprob = newYstar[i];
                }
            }
            //This was included since non of the variables can be negative
            //if(newY[i]<0 ||  newYstar[i]<0) Delta =epsilon;
        }

        newTimeStep = timeStep;


        if (finalize) {

//            for (int i = 0; i < IC.length; i++) {
//                if(newY[i] > maxAchieved[i]){
//                    maxAchieved[i] = newY[i];
//                    timeOfMaximumAchieved[i]=currentTime;
//                }
//            }
            return new double[][]{{newTimeStep}, newY};
        } else {
            if (Delta != 0.0) {
                factor = epsilon / Delta;

                if (factor >= 1) {
                    newTimeStep = timeStep * Math.pow(factor, 0.15);
                } else {
                    newTimeStep = timeStep * Math.pow(factor, 0.25);
                }
            } else {
                factor = 1e5;
                newTimeStep = timeStep * Math.pow(factor, 0.15);
                finalize = true;
            }


//              java.io.FileWriter fstream;
//           fstream = new java.io.FileWriter("/usr/home/rmantill/luciana/Parallel/testcom/RKF4.txt", true);
//
//              java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
//
//           //Close the output stream
////out.close();
//
////   out.write("--> "+"Tstep = " + timeStep+"newTimeStep" + newTimeStep +" Dif"+ ( timeStep-newTimeStep)+"\n");
////   out.write("-----> "+"Delta = " + Delta+" epsilon "+epsilon+" factor "+factor+"\n");
////   out.write("---------> "+" N Link " +IC.length+" Link with prob=" + problink+"\n");
////   out.write("-------------> " + "Yval   " + Yprob + "Yvalprob    " + Ystarprob+"\n");
//
//out.close();
  //        System.out.println("nlink" +IC.length/4 + "prob"+ problink + "--> "+timeStep+" "+epsilon+" "+Delta+" "+factor+" "+newTimeStep+" ("+java.util.Calendar.getInstance().getTime()+")");
  //        System.out.println("Yprob" +Yprob + "Yprob"+ Ystarprob + "--> "+timeStep+" "+epsilon+" "+Delta+" "+factor+" "+newTimeStep+" ("+java.util.Calendar.getInstance().getTime()+")");

          if (newTimeStep < 0.01 / 60.) {
                newTimeStep = 0.01 / 60.;
            }
            return stepSCS(currentTime, IC, newTimeStep, true);
        }
    }


    private double[][] stepSCSSerial(double currentTime, double[] IC, double timeStep, boolean finalize) throws IOException {

        //if first time call ever define array maxAchieved
        if (maxAchieved == null) {
            maxAchieved = new double[IC.length];
            timeOfMaximumAchieved = new double[IC.length];
            java.util.Arrays.fill(maxAchieved, Double.MIN_VALUE);
        }

        carrier = new double[IC.length];

        k0 = theFunction.eval(IC, currentTime);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * b[1][0] * k0[i]);
        }
        k1 = theFunction.eval(carrier, currentTime + a[1] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[2][0] * k0[i] + b[2][1] * k1[i]));
        }
        k2 = theFunction.eval(carrier, currentTime + a[2] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[3][0] * k0[i] + b[3][1] * k1[i] + b[3][2] * k2[i]));
        }
        k3 = theFunction.eval(carrier, currentTime + a[3] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[4][0] * k0[i] + b[4][1] * k1[i] + b[4][2] * k2[i] + b[4][3] * k3[i]));
        }
        k4 = theFunction.eval(carrier, currentTime + a[4] * timeStep);
        for (int i = 0; i < IC.length; i++) {
            carrier[i] = Math.max(0, IC[i] + timeStep * (b[5][0] * k0[i] + b[5][1] * k1[i] + b[5][2] * k2[i] + b[5][3] * k3[i] + b[5][4] * k4[i]));
        }
        k5 = theFunction.eval(carrier, currentTime + a[5] * timeStep);

        int ndif=IC.length/14;
        newY = new double[IC.length];
        newYstar = new double[IC.length];
        for (int i = 0; i < ndif*14; i++) {
            newY[i] = IC[i] + timeStep * (c[0] * k0[i] + c[1] * k1[i] + c[2] * k2[i] + c[3] * k3[i] + c[4] * k4[i] + c[5] * k5[i]);
            newY[i] = Math.max(0, newY[i]);
            newYstar[i] = IC[i] + timeStep * (cStar[0] * k0[i] + cStar[1] * k1[i] + cStar[2] * k2[i] + cStar[3] * k3[i] + cStar[4] * k4[i] + cStar[5] * k5[i]);
            newYstar[i] = Math.max(0, newYstar[i]);

        }

        for (int i = ndif*14; i < IC.length; i++) {
            newYstar[i] = IC[i];
            newY[i] = IC[i];
        }

        int problink = 0;
        double Yprob = 0, Ystarprob = 0;
        Delta = 0;
        for (int i = 0; i < IC.length; i++) {
            //&& Math.abs(newY[i] - newYstar[i])>0.001
            if ((newY[i] + newYstar[i]) > 0.0) {
                double newdelta = Math.abs(2 * (newY[i] - newYstar[i]) / (newY[i] + newYstar[i]));
                if (Delta < newdelta) {
                    Delta = newdelta;
                    problink = i;
                    Yprob = newY[i];
                    Ystarprob = newYstar[i];
                }
            }
        }

        newTimeStep = timeStep;


        if (finalize) {

//            for (int i = 0; i < IC.length; i++) {
//                if(newY[i] > maxAchieved[i]){
//                    maxAchieved[i] = newY[i];
//                    timeOfMaximumAchieved[i]=currentTime;
//                }
//            }
            return new double[][]{{newTimeStep}, newY};
        } else {
            if (Delta != 0.0) {
                factor = epsilon / Delta;

                if (factor >= 1) {
                    newTimeStep = timeStep * Math.pow(factor, 0.15);
                } else {
                    newTimeStep = timeStep * Math.pow(factor, 0.25);
                }
            } else {
                factor = 1e5;
                newTimeStep = timeStep * Math.pow(factor, 0.15);
                finalize = true;
            }


//              java.io.FileWriter fstream;
//           fstream = new java.io.FileWriter("/usr/home/rmantill/luciana/Parallel/testcom/RKF4.txt", true);
//
//              java.io.BufferedWriter out = new java.io.BufferedWriter(fstream);
//
//           //Close the output stream
////out.close();
//
////   out.write("--> "+"Tstep = " + timeStep+"newTimeStep" + newTimeStep +" Dif"+ ( timeStep-newTimeStep)+"\n");
////   out.write("-----> "+"Delta = " + Delta+" epsilon "+epsilon+" factor "+factor+"\n");
////   out.write("---------> "+" N Link " +IC.length+" Link with prob=" + problink+"\n");
////   out.write("-------------> " + "Yval   " + Yprob + "Yvalprob    " + Ystarprob+"\n");
//
//out.close();
//            System.out.println("    --> "+timeStep+" "+epsilon+" "+Delta+" "+factor+" "+newTimeStep+" ("+java.util.Calendar.getInstance().getTime()+")");
            if (newTimeStep < 0.01 / 60.) {
                newTimeStep = 0.01 / 60.;
            }
            return stepSCSSerial(currentTime, IC, newTimeStep, true);
        }
    }
    /**
     * Returns the values of the function described by differential equations in the
     * the intermidia steps needed to go from the Initial to the Final time
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param IC The value of the initial condition
     * @return The values of the multivatiate function at different times
     */
    public double[][][] simpleRun(double iniTime, double finalTime, double[] IC) {

        double currentTime = iniTime;

        java.util.Vector corrida = new java.util.Vector();
        corrida.addElement(new double[][]{{iniTime}, IC});
        double[][] givenStep;
        while (currentTime < finalTime) {
            givenStep = step(currentTime, IC, basicTimeStep, false);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            corrida.addElement(givenStep);

            if (givenStep[0][0] + basicTimeStep > finalTime) {
                break;
            }
        }

        givenStep = step(currentTime, IC, (finalTime - currentTime), true);
        basicTimeStep = givenStep[0][0];
        currentTime += basicTimeStep;
        givenStep[0][0] = currentTime;
        IC = givenStep[1];
        corrida.addElement(givenStep);

        double[][][] runOutput = new double[corrida.size()][][];
        for (int i = 0; i < runOutput.length; i++) {
            runOutput[i] = (double[][]) corrida.elementAt(i);
        }


        return runOutput;
    }

    /**
     * Returns the values of the function described by differential equations in the
     * the intermidia steps requested to go from the Initial to the Final time
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @return The values of the multivatiate function at different times
     */
    public double[][][] jumpsRun(double iniTime, double finalTime, double incrementalTime, double[] IC) {

        double currentTime = iniTime, targetTime;

        java.util.Vector corrida = new java.util.Vector();
        corrida.addElement(new double[][]{{iniTime}, IC});
        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;
            while (currentTime < targetTime) {
                givenStep = step(currentTime, IC, basicTimeStep, false);

                if (currentTime + givenStep[0][0] > targetTime) {
                    break;
                }
                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];
            }

            givenStep = step(currentTime, IC, targetTime - currentTime, true);

            if (currentTime + givenStep[0][0] > finalTime) {
                break;
            }
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            corrida.addElement(givenStep);

        }

        if (currentTime != finalTime) {
            givenStep = step(currentTime, IC, finalTime - currentTime, true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            corrida.addElement(givenStep);
        }

        double[][][] runOutput = new double[corrida.size()][][];
        for (int i = 0; i < runOutput.length; i++) {
            runOutput[i] = (double[][]) corrida.elementAt(i);
        }


        return runOutput;
    }

    /**
     * Writes (in binary format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps needed to go from the Initial to the Final
     * time
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void simpleRunToFile(double iniTime, double finalTime, double[] IC, java.io.DataOutputStream outputStream) throws java.io.IOException {

        double currentTime = iniTime;

        outputStream.writeDouble(currentTime);
        for (int j = 0; j < IC.length; j++) {
            outputStream.writeDouble(IC[j]);
        }
        double[][] givenStep;

        while (currentTime < finalTime) {
            givenStep = step(currentTime, IC, basicTimeStep, false);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            java.util.Calendar thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")");

            outputStream.writeDouble(currentTime);
            for (int j = 0; j < IC.length; j++) {
                outputStream.writeDouble(IC[j]);
            }
            if (givenStep[0][0] + basicTimeStep > finalTime) {
                break;
            }
        }

        if (currentTime != finalTime) {
            givenStep = step(currentTime, IC, finalTime - currentTime, true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            outputStream.writeDouble(currentTime);
            for (int j = 0; j < IC.length; j++) {
                outputStream.writeDouble(IC[j]);
            }
        }

        finalCond = IC;

    }

    /**
     * Writes (in binary format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunToFile(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.DataOutputStream outputStream) throws java.io.IOException {

        outputStream.writeInt((int) Math.ceil((finalTime - iniTime) / incrementalTime) + 1);
        System.out.println(((int) Math.ceil((finalTime - iniTime) / incrementalTime) + 1));
        double currentTime = iniTime, targetTime;

        outputStream.writeDouble(currentTime);
        for (int j = 0; j < IC.length; j++) {
            outputStream.writeDouble(IC[j]);
        }
        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")");
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();

        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;
            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = step(currentTime, IC, basicTimeStep, false);

                if (currentTime + givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];


            }
            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if (targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            givenStep = step(currentTime, IC, targetTime - currentTime, true);

            if (currentTime + givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            outputStream.writeDouble(currentTime);
            for (int j = 0; j < IC.length; j++) {
                outputStream.writeDouble(IC[j]);
            }
            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")");

            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        if (currentTime != finalTime) {
            givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            outputStream.writeDouble(currentTime);
            for (int j = 0; j < IC.length; j++) {
                outputStream.writeDouble(IC[j]);
            }
            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")");
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        finalCond = IC;

    }

    /**
     * Writes (in ascii format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time.  This method is very specific for solving equations of flow in a network.  It prints output for
     * the flow component at all locations.
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @param linksStructure The structure describing the topology of the river network
     * @param thisNetworkGeom The descripion the the hydraulic and geomorphic parameters
     * of the links in the network
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunToAsciiFile(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom) throws java.io.IOException {

        double currentTime = iniTime, targetTime;

        int ouletID = linksStructure.getOutletID();

        outputStream.write("\n");
        outputStream.write(currentTime + ",");
        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0) {
                outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");
            }
        }

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();

        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;
            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = step(currentTime, IC, basicTimeStep, false);

                if (currentTime + givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];

                if (currentTime < targetTime) {
                    for (int i = 0; i < IC.length; i++) {
                        if (IC[i] > maxAchieved[i]) {
                            maxAchieved[i] = IC[i];
                            timeOfMaximumAchieved[i] = currentTime;
                        }
                    }
                    thisDate = java.util.Calendar.getInstance();
                    thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
                    System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
                }


            }
            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if (targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            givenStep = step(currentTime, IC, targetTime - currentTime, true);

            if (currentTime + givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;
            }

            if (IC[ouletID] < 1e-3) {
                //System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime + ",");
            for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0) {
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");
                }
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);

            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        if (currentTime != finalTime && IC[ouletID] > 1e-6) {
            givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime + ",");
            for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0) {
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");
                }
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        finalCond = IC;

    }

    /**
     * Writes (in ascii format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time.  This method is very specific for solving equations of flow in a network.  It prints output for
     * the flow component at all locations.
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @param linksStructure The structure describing the topology of the river network
     * @param thisNetworkGeom The descripion the the hydraulic and geomorphic parameters
     * of the links in the network
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunToAsciiFilePlusLocations(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom, int[] resSimID, java.io.OutputStreamWriter outputStream_L) throws java.io.IOException {

        double currentTime = iniTime, targetTime;

        int ouletID = linksStructure.getOutletID();

        outputStream.write("\n");
        outputStream.write(currentTime + ",");
        outputStream_L.write("\n");
        outputStream_L.write(currentTime + ",");
        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0) {
                outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");
            }
        }
        for (int i : resSimID) {
            outputStream_L.write(IC[i - 1] + ",");
        }

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();

        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;
            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = step(currentTime, IC, basicTimeStep, false);

                if (currentTime + givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];

                if (currentTime < targetTime) {
                    for (int i = 0; i < IC.length; i++) {
                        if (IC[i] > maxAchieved[i]) {
                            maxAchieved[i] = IC[i];
                            timeOfMaximumAchieved[i] = currentTime;
                        }
                    }
                    thisDate = java.util.Calendar.getInstance();
                    thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
                    System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
                }


            }
            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if (targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            givenStep = step(currentTime, IC, targetTime - currentTime, true);

            if (currentTime + givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;
            }

            if (IC[ouletID] < 1e-3) {
                //System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime + ",");
            outputStream_L.write("\n");
            outputStream_L.write(currentTime + ",");
            for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0) {
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");
                }
            }
            for (int i : resSimID) {
                outputStream_L.write(IC[i - 1] + ",");
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);

            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        if (currentTime != finalTime && IC[ouletID] > 1e-3) {
            givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime + ",");
            outputStream_L.write("\n");
            outputStream_L.write(currentTime + ",");
            for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0) {
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");
                }
            }
            for (int i : resSimID) {
                outputStream_L.write(IC[i - 1] + ",");
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        finalCond = IC;

    }

    /**
     * Writes (in ascii format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time.  This method is very specific for solving equations of flow in a network.  It prints output for
     * the flow component at all locations.
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @param linksStructure The structure describing the topology of the river network
     * @param thisNetworkGeom The descripion the the hydraulic and geomorphic parameters
     * of the links in the network
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunToAsciiFileHilltype4(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, java.io.OutputStreamWriter outputStream2, java.io.OutputStreamWriter outputStream3, java.io.OutputStreamWriter outputStream4, java.io.OutputStreamWriter outputStream5, java.io.OutputStreamWriter outputStream6, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom, int HT) throws java.io.IOException {

        double currentTime = iniTime, targetTime;

        int ouletID = linksStructure.getOutletID();
        int nLi = linksStructure.contactsArray.length;
        outputStream.write("\n" + currentTime + ",");
        outputStream2.write("\n" + currentTime + ",");
        outputStream3.write("\n" + currentTime + ",");
        outputStream4.write("\n" + currentTime + ",");
        outputStream5.write("\n" + currentTime + ",");
        outputStream6.write("\n" + currentTime + ",");
        java.text.DecimalFormat fourPlaces = new java.text.DecimalFormat("0.0000");
        for (int i = 0; i < nLi; i++) {

            outputStream.write(fourPlaces.format(IC[i]) + ",");
            outputStream2.write(fourPlaces.format(IC[i + 3 * nLi]) + ",");
            outputStream3.write(fourPlaces.format(IC[i + 4 * nLi]) + ",");
            outputStream4.write(fourPlaces.format(IC[i + 5 * nLi]) + ",");
            outputStream5.write(fourPlaces.format(IC[i + 1 * nLi]) + ",");
            outputStream6.write(fourPlaces.format(IC[i + 2 * nLi]) + ",");

        }

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + fourPlaces.format(IC[ouletID]));
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();

        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;

            while (currentTime < targetTime) {
                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                if (HT == 4) {
                    givenStep = step(currentTime, IC, basicTimeStep, false);
                } else if (HT == 6) {
                    givenStep = step(currentTime, IC, basicTimeStep, false);
                } else {
                    givenStep = step(currentTime, IC, basicTimeStep, false);
                }

                if (currentTime + givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];


            }
            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if (targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            if (HT == 4) {
                givenStep = step(currentTime, IC, targetTime - currentTime, true);
            } else if (HT == 6) {
                givenStep = step(currentTime, IC, targetTime - currentTime, true);
            } else {
                givenStep = step(currentTime, IC, targetTime - currentTime, true);
            }



            if (currentTime + givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;
            }

            if (IC[ouletID] < 1e-3) {
                //System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];
            double test = currentTime / 5;

            outputStream.write("\n" + currentTime + ",");
            outputStream2.write("\n" + currentTime + ",");
            outputStream3.write("\n" + currentTime + ",");
            outputStream4.write("\n" + currentTime + ",");
            outputStream5.write("\n" + currentTime + ",");
            outputStream6.write("\n" + currentTime + ",");

            for (int i = 0; i < nLi; i++) {

                outputStream.write(fourPlaces.format(IC[i]) + ",");
                outputStream2.write(fourPlaces.format(IC[i + 3 * nLi]) + ",");
                outputStream3.write(fourPlaces.format(IC[i + 4 * nLi]) + ",");
                outputStream4.write(fourPlaces.format(IC[i + 5 * nLi]) + ",");
                outputStream5.write(fourPlaces.format(IC[i + 1 * nLi]) + ",");
                outputStream6.write(fourPlaces.format(IC[i + 2 * nLi]) + ",");

            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + fourPlaces.format(IC[ouletID]));

            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        if (currentTime != finalTime && IC[ouletID] > 1e-3) {
            if (HT == 4) {
                givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            } else if (HT == 6) {
                givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            } else {
                givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            /*  outputStream.write("\n"+currentTime + ",");
            outputStream2.write("\n"+currentTime + ",");
            outputStream3.write("\n"+currentTime + ",");
            outputStream4.write("\n"+currentTime + ",");
            outputStream5.write("\n"+currentTime + ",");
            outputStream6.write("\n"+currentTime + ",");

            for (int i = 0; i < nLi; i++) {

            outputStream.write(fourPlaces.format(IC[i]) + ",");
            outputStream2.write(fourPlaces.format(IC[i+3*nLi]) + ",");
            outputStream3.write(fourPlaces.format(IC[i+4*nLi]) + ",");
            outputStream4.write(fourPlaces.format(IC[i+5*nLi]) + ",");
            outputStream5.write(fourPlaces.format(IC[i+1*nLi]) + ",");
            outputStream6.write(fourPlaces.format(IC[i+2*nLi]) + ",");

            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + fourPlaces.format(IC[ouletID]));
             */
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        finalCond = IC;

    }

    /**
     * Writes (in ascii format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time.  This method is very specific for solving equations of flow in a network.  It prints output for
     * the flow component at all locations.
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @param linksStructure The structure describing the topology of the river network
     * @param thisNetworkGeom The descripion the the hydraulic and geomorphic parameters
     * of the links in the network
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunToAsciiFileTabs(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom) throws java.io.IOException {

        double currentTime = iniTime, targetTime;

        int ouletID = linksStructure.getOutletID();
        // Indices corresponding to the maximum areas fo each Horton order
        int[] ind = new int[10];
        ind[0] = 1454;
        ind[1] = 1976;
        ind[2] = 4259;
        ind[3] = 4193;
        ind[4] = 87;
        ind[5] = 1788;
        ind[6] = 2397;
        ind[7] = 191;

        outputStream.write("\n");
        outputStream.write(currentTime + "\t");
        for (int i = 0; i < 8; i++) {
            outputStream.write(IC[linksStructure.completeStreamLinksArray[ind[i]]] + "\t");
        }

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();

        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;
            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = step(currentTime, IC, basicTimeStep, false);

                if (currentTime + givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];


            }
            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if (targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            givenStep = step(currentTime, IC, targetTime - currentTime, true);

            if (currentTime + givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;
            }

            if (IC[ouletID] < 1e-3) {
                //System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime + "\t");
            for (int i = 0; i < 8; i++) {
                outputStream.write(IC[linksStructure.completeStreamLinksArray[ind[i]]] + "\t");
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);

            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        if (currentTime != finalTime && IC[ouletID] > 1e-3) {
            givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime + "\t");
            for (int i = 0; i < 8; i++) {
                outputStream.write(IC[linksStructure.completeStreamLinksArray[ind[i]]] + "\t");
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }
        finalCond = IC;

    }

    /**
     * Writes (in ascii format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time.  This method is very specific for solving equations of flow in a network.  It prints output for
     * the flow component at all locations.
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @param linksStructure The structure describing the topology of the river network
     * @param thisNetworkGeom The descripion the the hydraulic and geomorphic parameters
     * of the links in the network
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunCompleteToAsciiFile(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom, java.io.OutputStreamWriter outputStream1) throws java.io.IOException {

        double currentTime = iniTime, targetTime;

        int ouletID = linksStructure.getOutletID();

//        outputStream.write("\n");
//        outputStream.write(currentTime + ",");
//        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//            outputStream.write(IC[i] + ",");
//        }

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);

        outputStream1.write(currentTime + "," + IC[ouletID] + "\n");

        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();

        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;
            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = step(currentTime, IC, basicTimeStep, false);

                if (currentTime + givenStep[0][0] >= targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];

                if (currentTime < targetTime) {
                    for (int i = 0; i < IC.length; i++) {
                        if (IC[i] > maxAchieved[i]) {
                            maxAchieved[i] = IC[i];
                            timeOfMaximumAchieved[i] = currentTime;
                        }
                    }
                    thisDate = java.util.Calendar.getInstance();
                    thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
                    System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);

                    outputStream1.write(currentTime + "," + IC[ouletID] + "\n");
                }



            }
            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println("outsideLoop" + thisDate.getTime());

            if (targetTime == finalTime) {
                System.out.println("******** I'll go to End Of Step ********");
                break;
            }
            // here the targetTime - currentTiem will be negative
            givenStep = step(currentTime, IC, targetTime - currentTime, true);

            if (currentTime + givenStep[0][0] >= finalTime) {
                System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

//            outputStream.write("\n");
//            outputStream.write(currentTime + ",");
//            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//                    outputStream.write(IC[i] + ",");
//            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            outputStream1.write(currentTime + "," + IC[ouletID] + "\n");
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        if (currentTime != finalTime) {
            givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

//            outputStream.write("\n");
//            outputStream.write(currentTime + ",");
//            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//                    outputStream.write(IC[i] + ",");
//            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            outputStream1.write(currentTime + "," + IC[ouletID] + "\n");
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        finalCond = IC;

    }

    /**
     * This version was included to make the SCS more efficient
     * It runs
     * Writes (in ascii format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time.  This method is very specific for solving equations of flow in a network.  It prints output for
     * the flow component at all locations.
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @param linksStructure The structure describing the topology of the river network
     * @param thisNetworkGeom The descripion the the hydraulic and geomorphic parameters
     * of the links in the network
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunCompleteToAsciiFileSCS(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom, java.io.OutputStreamWriter outputStream1, java.io.OutputStreamWriter outputStream2, java.io.OutputStreamWriter outputStream3) throws java.io.IOException {

        double currentTime = iniTime, targetTime;

        int ouletID = linksStructure.getOutletID();
        int nlinks = linksStructure.contactsArray.length;
//        outputStream.write("\n");
//        outputStream.write(currentTime + ",");
//        for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//            outputStream.write(IC[i] + ",");
//        }

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID] );
        //System.out.println(thisDate.getTime() + " P: " + IC[5*nlinks+ouletID] +" EVPT: " + IC[4*nlinks+ouletID] +" Qfluxes: " + IC[6*nlinks+ouletID]  +" storage: " + IC[7*nlinks+ouletID]);
  System.out.println(thisDate.getTime() + " Surface Stor: " + IC[1*nlinks+ouletID] +" Water table: " + IC[2*nlinks+ouletID] +" Soil moisture: " + IC[3*nlinks+ouletID]+" ImpeArea: " + IC[9*nlinks+ouletID]);
        //System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " soil: " + IC[54]);
        //System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
        //System.out.println(thisDate.getTime() + " Outlet Discharge: " + IC[ouletID] +" Surface Stor: " + IC[1*nlinks+ouletID] +" Water table: " + IC[2*nlinks+ouletID] +" Soil moisture: " + IC[3*nlinks+ouletID]);
        outputStream1.write(currentTime + "," + IC[ouletID] + "\n");
        //outputStream2.write(currentTime + "," + IC[nlinks + ouletID] + "," + IC[2 * nlinks + ouletID] + "," + IC[3 * nlinks + ouletID] + "\n");

        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();

        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;

            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = stepSCS(currentTime, IC, basicTimeStep, false);

                if (currentTime + givenStep[0][0] >= targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];

                if (currentTime < targetTime) {
                    for (int i = 0; i < IC.length; i++) {
                        if (IC[i] > maxAchieved[i]) {
                            maxAchieved[i] = IC[i];
                            timeOfMaximumAchieved[i] = currentTime;
                        }
                    }
                    thisDate = java.util.Calendar.getInstance();
                    thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
                    System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
           //System.out.println(thisDate.getTime() + " P: " + IC[5*nlinks+ouletID] +" EVPT: " + IC[4*nlinks+ouletID] +" Qfluxes: " + IC[6*nlinks+ouletID]  +" storage: " + IC[7*nlinks+ouletID]);
                    //System.out.println(thisDate.getTime() + " Surface Stor: " + IC[1*nlinks+ouletID] +" Water table: " + IC[2*nlinks+ouletID] +" Soil moisture: " + IC[3*nlinks+ouletID]);
                    outputStream1.write(currentTime + "," + IC[ouletID] + "\n");
                    outputStream2.write(currentTime + "," + IC[nlinks + ouletID] + "," + IC[2 * nlinks + ouletID] + "," + IC[3 * nlinks + ouletID] + "\n");
               outputStream3.write(currentTime + "," + IC[4*nlinks + ouletID] + "," + IC[5 * nlinks + ouletID] + "," + IC[6 * nlinks + ouletID]+"," + IC[7 * nlinks + ouletID]+"," + IC[8 * nlinks + ouletID]+"," + IC[9 * nlinks + ouletID] + "\n");
             }



            }
            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println("outsideLoop" + thisDate.getTime());

            if (targetTime == finalTime) {
                System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            givenStep = stepSCS(currentTime, IC, targetTime - currentTime, true);

            if (currentTime + givenStep[0][0] >= finalTime) {
                System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

//            outputStream.write("\n");
//            outputStream.write(currentTime + ",");
//            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//                    outputStream.write(IC[i] + ",");
//            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            //System.out.println(thisDate.getTime() + " P: " + IC[5*nlinks+ouletID] +" EVPT: " + IC[4*nlinks+ouletID] +" Qfluxes: " + IC[6*nlinks+ouletID]  +" storage: " + IC[7*nlinks+ouletID]);
            //System.out.println(thisDate.getTime() + " Surface Stor: " + IC[1*nlinks+ouletID] +" Water table: " + IC[2*nlinks+ouletID] +" Soil moisture: " + IC[3*nlinks+ouletID]);
            //System.out.println(thisDate.getTime() + " Outlet Discharge: " + IC[ouletID] +" Surface Stor: " + IC[1*nlinks+ouletID] +" Water table: " + IC[2*nlinks+ouletID] +" Soil moisture: " + IC[3*nlinks+ouletID]);
            outputStream1.write(currentTime + "," + IC[ouletID] + "\n");
            outputStream2.write(currentTime + "," + IC[nlinks + ouletID] + "," + IC[2 * nlinks + ouletID] + "," + IC[3 * nlinks + ouletID] + "\n");
            outputStream3.write(currentTime + "," + IC[4*nlinks + ouletID] + "," + IC[5 * nlinks + ouletID] + "," + IC[6 * nlinks + ouletID]+"," + IC[7 * nlinks + ouletID]+"," + IC[8 * nlinks + ouletID]+"," + IC[9 * nlinks + ouletID] + "\n");
          //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        if (currentTime != finalTime) {
            givenStep = stepSCS(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

//            outputStream.write("\n");
//            outputStream.write(currentTime + ",");
//            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//                    outputStream.write(IC[i] + ",");
//            }

            for (int i = 0; i < IC.length; i++) {
                if (IC[i] > maxAchieved[i]) {
                    maxAchieved[i] = IC[i];
                    timeOfMaximumAchieved[i] = currentTime;
                }
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            //System.out.println(thisDate.getTime() + " P: " + IC[5*nlinks+ouletID] +" EVPT: " + IC[4*nlinks+ouletID] +" Qfluxes: " + IC[6*nlinks+ouletID]  +" storage: " + IC[7*nlinks+ouletID]);
            //iSystem.out.println(thisDate.getTime() + " Surface Stor: " + IC[1*nlinks+ouletID] +" Water table: " + IC[2*nlinks+ouletID] +" Soil moisture: " + IC[3*nlinks+ouletID]);
            outputStream1.write(currentTime + "," + IC[ouletID] + "\n");
            outputStream2.write(currentTime + "," + IC[nlinks + ouletID] + "," + IC[2 * nlinks + ouletID] + "," + IC[3 * nlinks + ouletID] + "\n");
  outputStream3.write(currentTime + "," + IC[4*nlinks + ouletID] + "," + IC[5 * nlinks + ouletID] + "," + IC[6 * nlinks + ouletID]+"," + IC[7 * nlinks + ouletID]+"," + IC[8 * nlinks + ouletID]+"," + IC[9 * nlinks + ouletID] + "\n");
        
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        finalCond = IC;

    }

    /**
     * This version was included to make the SCS more efficient
     * It runs
     * Writes (in ascii format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time.  This method is very specific for solving equations of flow in a network.  It prints output for
     * the flow component at all locations.
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @param linksStructure The structure describing the topology of the river network
     * @param thisNetworkGeom The descripion the the hydraulic and geomorphic parameters
     * of the links in the network
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunCompleteToAsciiFileSCSSerial(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom, java.io.OutputStreamWriter outputStream1, java.io.OutputStreamWriter outputStream2, java.io.OutputStreamWriter outputStream3, java.io.OutputStreamWriter outputStream4, int writeorder) throws java.io.IOException {

        double currentTime = iniTime, targetTime;
        //System.out.println("currentTime"+currentTime +"incrementalTime"+incrementalTime);
        //DecimalFormat df = new DecimalFormat("###");
        DecimalFormat df1 = new DecimalFormat("###.#");
        DecimalFormat df2 = new DecimalFormat("###.##");
        DecimalFormat df3 = new DecimalFormat("###.###");
        DecimalFormat df4 = new DecimalFormat("###.####");

        //DecimalFormat df10 = new DecimalFormat("###.##########");

        int ouletID = linksStructure.getOutletID();
        int nlinks = linksStructure.contactsArray.length;

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID] + " Precip: " + IC[6 * nlinks + ouletID]);

        
        writeorder = Math.max(1, writeorder);
        
        outputStream1.write(df3.format(currentTime) + ",");
        outputStream2.write(df3.format(currentTime) + ",");
        outputStream3.write(df3.format(currentTime) + ",");
        outputStream4.write(df3.format(currentTime) + ",");
        // define the order to be ploted
        for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
            int nl = linksStructure.completeStreamLinksArray[i];
            if (thisNetworkGeom.linkOrder(nl) >= writeorder) {
                outputStream1.write(df4.format(IC[nl]) + ",");
                outputStream2.write(df2.format(IC[nlinks + nl]) + "," + df2.format(IC[2 * nlinks + nl]) + "," + df2.format(IC[3 * nlinks + nl]) + ",");
                outputStream3.write(df2.format(IC[4 * nlinks + nl]) + "," + df2.format(IC[5 * nlinks + nl]) + "," + df2.format(IC[6 * nlinks + nl]) + "," + df2.format(IC[7 * nlinks + nl]) + "," + df2.format(IC[8 * nlinks + nl]) + "," + df2.format(IC[9 * nlinks + nl]) + ",");
                outputStream4.write(df2.format(IC[10 * nlinks + nl]) + "," + df2.format(IC[11 * nlinks + nl]) + "," + df2.format(IC[12 * nlinks + nl]) + "," + df2.format(IC[13 * nlinks + nl]) + ",");

            }
        }
        outputStream1.write("\n");
        outputStream2.write("\n");
        outputStream3.write("\n");
        outputStream4.write("\n");
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();


        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;
            //  System.out.println("currentTime"+currentTime+"targetTime"+targetTime + "basicTimeStep" +basicTimeStep);
            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = stepSCSSerial(currentTime, IC, basicTimeStep, false);
                //    System.out.println("currentTime"+currentTime+"targetTime"+targetTime +"basicTimeStep"+ basicTimeStep+"givenStep[0][0]"+givenStep[0][0]);
                if (currentTime + givenStep[0][0] >= targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];

                if (currentTime < targetTime) {
                    for (int i = 0; i < IC.length; i++) {
                        if (IC[i] > maxAchieved[i]) {
                            maxAchieved[i] = IC[i];
                            timeOfMaximumAchieved[i] = currentTime;
                        }
                    }
                    thisDate = java.util.Calendar.getInstance();
                    thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
                    System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
                    //System.out.println(thisDate.getTime() + " Outlet Discharge: " + IC[ouletID] +" Surface Stor: " + IC[1*nlinks+ouletID] +" Water table: " + IC[2*nlinks+ouletID] +" Soil moisture: " + IC[3*nlinks+ouletID]);

                    outputStream1.write(df3.format(currentTime) + ",");
                    outputStream2.write(df3.format(currentTime) + ",");
                    outputStream3.write(df3.format(currentTime) + ",");
                    outputStream4.write(df3.format(currentTime) + ",");
                    // define the order to be ploted
                    for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
                        int nl = linksStructure.completeStreamLinksArray[i];
                        if (thisNetworkGeom.linkOrder(nl) >= writeorder) {
                    outputStream1.write(df4.format(IC[nl]) + ",");
                    outputStream2.write(df3.format(IC[nlinks + nl]) + "," + df3.format(IC[2 * nlinks + nl]) + "," + df3.format(IC[3 * nlinks + nl]) + ",");
                    outputStream3.write(df3.format(IC[4 * nlinks + nl]) + "," + df3.format(IC[5 * nlinks + nl]) + "," + df3.format(IC[6 * nlinks + nl]) + "," + df3.format(IC[7 * nlinks + nl]) + "," + df2.format(IC[8 * nlinks + nl]) + "," + df3.format(IC[9 * nlinks + nl]) + ",");
                    outputStream4.write(df3.format(IC[10 * nlinks + nl]) + "," + df3.format(IC[11 * nlinks + nl]) + "," + df3.format(IC[12 * nlinks + nl]) + "," + df3.format(IC[13 * nlinks + nl]) + ",");

                        }
                    }
                    outputStream1.write("\n");
                    outputStream2.write("\n");
                    outputStream3.write("\n");
                    outputStream4.write("\n");
                }



            }
            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println("outsideLoop" + thisDate.getTime());

            if (targetTime == finalTime) {
                System.out.println("******** I'll go to End Of Step ********");
                break;
            }

            givenStep = stepSCSSerial(currentTime, IC, targetTime - currentTime, true);

            if (currentTime + givenStep[0][0] >= finalTime) {
                System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

//            outputStream.write("\n");
//            outputStream.write(currentTime + ",");
//            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//                    outputStream.write(IC[i] + ",");
//            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            //System.out.println(thisDate.getTime() + " Outlet Discharge: " + IC[ouletID] +" Surface Stor: " + IC[1*nlinks+ouletID] +" Water table: " + IC[2*nlinks+ouletID] +" Soil moisture: " + IC[3*nlinks+ouletID]);

            outputStream1.write(df3.format(currentTime) + ",");
            outputStream2.write(df3.format(currentTime) + ",");
            outputStream3.write(df3.format(currentTime) + ",");
            outputStream4.write(df3.format(currentTime) + ",");
            // define the order to be ploted
            for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
                int nl = linksStructure.completeStreamLinksArray[i];
                if (thisNetworkGeom.linkOrder(nl) >= writeorder) {
                    outputStream1.write(df4.format(IC[nl]) + ",");
                    outputStream2.write(df3.format(IC[nlinks + nl]) + "," + df3.format(IC[2 * nlinks + nl]) + "," + df3.format(IC[3 * nlinks + nl]) + ",");
                    outputStream3.write(df3.format(IC[4 * nlinks + nl]) + "," + df3.format(IC[5 * nlinks + nl]) + "," + df3.format(IC[6 * nlinks + nl]) + "," + df3.format(IC[7 * nlinks + nl]) + "," + df2.format(IC[8 * nlinks + nl]) + "," + df3.format(IC[9 * nlinks + nl]) + ",");
                    outputStream4.write(df3.format(IC[10 * nlinks + nl]) + "," + df3.format(IC[11 * nlinks + nl]) + "," + df3.format(IC[12 * nlinks + nl]) + "," + df3.format(IC[13 * nlinks + nl]) + ",");

                }
            }
            outputStream1.write("\n");
            outputStream2.write("\n");
            outputStream3.write("\n");
            outputStream4.write("\n");       //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        if (currentTime != finalTime) {
            givenStep = stepSCSSerial(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

//            outputStream.write("\n");
//            outputStream.write(currentTime + ",");
//            for (int i = 0; i < linksStructure.contactsArray.length; i++) {
//                    outputStream.write(IC[i] + ",");
//            }

            for (int i = 0; i < IC.length; i++) {
                if (IC[i] > maxAchieved[i]) {
                    maxAchieved[i] = IC[i];
                    timeOfMaximumAchieved[i] = currentTime;
                }
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            //System.out.println(thisDate.getTime() + " Outlet Discharge: " + IC[ouletID] +" Surface Stor: " + IC[1*nlinks+ouletID] +" Water table: " + IC[2*nlinks+ouletID] +" Soil moisture: " + IC[3*nlinks+ouletID]);

//            outputStream1.write(df3.format(currentTime) + ",");
//            outputStream2.write(df3.format(currentTime) + ",");
//            outputStream3.write(df3.format(currentTime) + ",");
//            outputStream4.write(df3.format(currentTime) + ",");
//            // define the order to be ploted
//            for (int i = 0; i < linksStructure.completeStreamLinksArray.length; i++) {
//                int nl = linksStructure.completeStreamLinksArray[i];
//                if (thisNetworkGeom.linkOrder(nl) >= writeorder) {
//                    outputStream1.write(df2.format(IC[nl]) + ",");
//                    outputStream2.write(df2.format(IC[nlinks + nl]) + "," + df2.format(IC[2 * nlinks + nl]) + "," + df2.format(IC[3 * nlinks + nl]) + ",");
//                    outputStream3.write(df2.format(IC[4 * nlinks + nl]) + "," + df2.format(IC[5 * nlinks + nl]) + "," + df2.format(IC[6 * nlinks + nl]) + "," + df2.format(IC[7 * nlinks + nl]) + "," + df2.format(IC[8 * nlinks + nl]) + "," + df2.format(IC[9 * nlinks + nl]) + ",");
//                    outputStream4.write(df2.format(IC[10 * nlinks + nl]) + "," + df2.format(IC[11 * nlinks + nl]) + "," + df2.format(IC[12 * nlinks + nl]) + "," + df2.format(IC[13 * nlinks + nl]) + ",");
//
//                }
//            }
//            outputStream1.write("\n");
//            outputStream2.write("\n");
//            outputStream3.write("\n");
//            outputStream4.write("\n");       //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        finalCond = IC;

    }

    /**
     * Writes (in ascii format) to a specified file the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time.  This method is very specific for solving equations of flow in a network.  It prints output for
     * the flow component at a few locations.
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @param linksStructure The structure describing the topology of the river network
     * @param thisNetworkGeom The descripion the the hydraulic and geomorphic parameters
     * of the links in the network
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunToIncompleteAsciiFile(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom) throws java.io.IOException {

        double currentTime = iniTime, targetTime;



        int basinOrder = linksStructure.getBasinOrder();


        int ouletID = linksStructure.getOutletID();

        outputStream.write("\n");
        outputStream.write(currentTime + ",");


        for (int i = 0; i
                < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder - 3, 1)) {
                outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");


            }
        }

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")");
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();



        double[][] givenStep;



        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;


            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = step(currentTime, IC, basicTimeStep, false);



                if (currentTime + givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;


                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[

0][0] = currentTime;
                IC = givenStep[1];


            }

            double typicalStepSize = basicTimeStep;

            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/



            if (targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;


            }

            givenStep = step(currentTime, IC, targetTime - currentTime, true);



            if (currentTime + givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;


            }

            if (IC[ouletID] < 1e-1) {
                //System.out.println("******** False Step ********");
                break;


            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[

0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime + ",");


            for (int i = 0; i
                    < linksStructure.completeStreamLinksArray.length; i++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder - 3, 1)) {
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");


                }
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID] + " - Tipical Time Step: " + typicalStepSize);

            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();



        }

        if (currentTime != finalTime && IC[ouletID] > 1e-1) {
            givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[

0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");
            outputStream.write(currentTime + ",");


            for (int i = 0; i
                    < linksStructure.completeStreamLinksArray.length; i++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > Math.max(basinOrder - 3, 1)) {
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");


                }
            }

            /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println(thisDate.getTime()+" ("+java.util.Calendar.getInstance().getTime()+")");*/
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();

        }

        finalCond = IC;



    }

    /**
     * Writes to standard output the values of the function described by differential
     * equations in the the intermidia steps needed to go from the Initial to the Final
     * time
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void simpleRunToScreen(double iniTime, double finalTime, double[] IC) {

        double currentTime = iniTime;

        System.out.print(currentTime + ",");


        for (int j = 0; j
                < IC.length; j++) {
            System.out.print(IC[j] + ",");


        }
        System.out.println();


        double[][] givenStep;



        while (currentTime < finalTime) {
            givenStep = step(currentTime, IC, basicTimeStep, false);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[

0][0] = currentTime;
            IC = givenStep[1];

            java.util.Calendar thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));

            System.out.print(currentTime + ",");


            for (int j = 0; j
                    < IC.length; j++) {
                System.out.print(IC[j] + ",");


            }
            System.out.println();



            if (givenStep[0][0] + basicTimeStep > finalTime) {
                break;


            }
        }

        if (currentTime != finalTime) {
            givenStep = step(currentTime, IC, finalTime - currentTime, true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[

0][0] = currentTime;
            IC = givenStep[1];

            System.out.print(currentTime + ",");


            for (int j = 0; j
                    < IC.length; j++) {
                System.out.print(IC[j] + ",");


            }
            System.out.println();


        }

        finalCond = IC;



    }

    /**
     * Writes to standard output the values of the function described by differential
     * equations in the the intermidia steps requested to go from the Initial to the Final
     * time
     * @param iniTime The initial time of the solution
     * @param finalTime The final time of the solution
     * @param incrementalTime How often the values are desired
     * @param IC The value of the initial condition
     * @param outputStream The file to which the information will be writen
     * @throws java.io.IOException Captures errors while writing to the file
     */
    public void jumpsRunToScreen(double iniTime, double finalTime, double incrementalTime, double[] IC) {

        double currentTime = iniTime, targetTime;


        System.out.print(currentTime + ",");


        for (int j = 0; j
                < IC.length; j++) {
            System.out.print(IC[j] + ",");


        }
        System.out.println();



        double[][] givenStep;



        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;


            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = step(currentTime, IC, basicTimeStep, false);



                if (currentTime + givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;


                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[

0][0] = currentTime;
                IC = givenStep[1];




            } /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if (targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;


            }

            givenStep = step(currentTime, IC, targetTime - currentTime, true);



            if (currentTime + givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;


            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[

0][0] = currentTime;
            IC = givenStep[1];

            System.out.print(currentTime + ",");


            for (int j = 0; j
                    < IC.length; j++) {
                System.out.print(IC[j] + ",");


            }
            System.out.println();


            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();



        }

        if (currentTime != finalTime) {
            givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[

0][0] = currentTime;
            IC = givenStep[1];

            System.out.print(currentTime + ",");


            for (int j = 0; j
                    < IC.length; j++) {
                System.out.print(IC[j] + ",");


            }
            System.out.println();



        }

        finalCond = IC;



    }

    public void jumpsRunToAsciiFile_luciana(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom) throws java.io.IOException {

        double currentTime = iniTime, targetTime;



        int ouletID = linksStructure.getOutletID();

        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));

        outputStream.write("\n");
        outputStream.write(thisDate.getTime() + ",");
        //outputStream.write(currentTime+",");


        for (int i = 0; i
                < linksStructure.completeStreamLinksArray.length; i++) {
            if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1) {
                outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");


            }
        }

        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();



        double[][] givenStep;



        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;


            while (currentTime < targetTime) {

                /*thisDate=java.util.Calendar.getInstance();
                thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
                System.out.println("inLoop"+thisDate.getTime());*/

                givenStep = step(currentTime, IC, basicTimeStep, false);



                if (currentTime + givenStep[0][0] > targetTime) {
                    //System.out.println("******** False Step ********");
                    break;


                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[

0][0] = currentTime;
                IC = givenStep[1];




            } /*thisDate=java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long)(currentTime*60.*1000.0));
            System.out.println("outsideLoop"+thisDate.getTime());*/

            if (targetTime == finalTime) {
                //System.out.println("******** I'll go to End Of Step ********");
                break;


            }

            givenStep = step(currentTime, IC, targetTime - currentTime, true);



            if (currentTime + givenStep[0][0] >= finalTime) {
                //System.out.println("******** False Step ********");
                break;


            }

            if (IC[ouletID] < 1e-3) {
                //System.out.println("******** False Step ********");
                break;


            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[

0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");

            outputStream.write(thisDate.getTime() + ",");
            // outputStream.write(currentTime+",");


            for (int i = 0; i
                    < linksStructure.completeStreamLinksArray.length; i++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1) {
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");


                }
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);

            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();



        }

        if (currentTime != finalTime && IC[ouletID] > 1e-3) {
            givenStep = step(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[

0][0] = currentTime;
            IC = givenStep[1];

            outputStream.write("\n");
            outputStream.write(thisDate.getTime() + ",");
            // outputStream.write(currentTime+",");


            for (int i = 0; i
                    < linksStructure.completeStreamLinksArray.length; i++) {
                if (thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 1) {
                    outputStream.write(IC[linksStructure.completeStreamLinksArray[i]] + ",");


                }
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
            //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
            //System.out.println();



        }

        finalCond = IC;



    }

    /**
     * Sets the valuo of the algorithm time step
     * @param newBTS The time step to assign
     */
    public void setBasicTimeStep(double newBTS) {
        basicTimeStep = newBTS;


    }

    /**
     * Returns an array with the maximum value calculated during the iteration process
     * @param newBTS The time step to assign
     */
    public double[] getMaximumAchieved() {
        return maxAchieved;


    }

    /**
     * Returns an array with the time to maximum value calculated during the iteration process
     * @param newBTS The time step to assign
     */
    public double[] getTimeToMaximumAchieved() {
        return timeOfMaximumAchieved;


    }

    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String args[]) {

        hydroScalingAPI.util.ordDiffEqSolver.Lorenz funcionLorenz;


        double[][][] answer;

        funcionLorenz = new hydroScalingAPI.util.ordDiffEqSolver.Lorenz(16.0f, 45.0f, 4.0f);
        //double[][] answer1=new RKF(funcionLorenz, 1e-6, .001).step(0.0,new double[] {-13,-12, 52},.001,false);
        //System.out.print("Time: "+answer1[0][0]+" Evaluation: ");
        //for(int j=0;j<answer1[1].length;j++) System.out.print(answer1[1][j]+" ");
        //System.exit(0);

        System.out.println("starts running");
        java.util.Date startTime = new java.util.Date();
        System.out.println("Start Time: " + startTime.toString());
        answer = new RKF(funcionLorenz, 1e-4, .001).jumpsRun(0, 1000, 0.2, new double[]{-13, -12, 52});
        //answer=new RKF(funcionLorenz, 1e-4, .001).simpleRun(0,10000,new double[] {-13,-12, 52});
        java.util.Date endTime = new java.util.Date();
        System.out.println("End Time:" + endTime.toString());
        System.out.println("Running Time:" + (.001 * (endTime.getTime() - startTime.getTime())) + " seconds");

        /*System.out.println(answer.length);
        System.exit(0);
        for (int i=0;i<answer.length;i++){
        System.out.print("Time: "+answer[i][0][0]+" Evaluation: ");
        for(int j=0;j<answer[i][1].length;j++) System.out.print(answer[i][1][j]+" ");
        System.out.println("");
        }*/



    }
}
