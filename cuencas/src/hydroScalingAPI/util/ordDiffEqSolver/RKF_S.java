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
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
/**
 * An implementation of the Runge-Kutta-Felberg algorithm for solving non-linear ordinary
 * differential equations.  It uses a time step control algorithm to avoid numerical errors
 * while solving the equations
 * @author Ricardo Mantilla
 */
public class RKF_S extends java.lang.Object {

    hydroScalingAPI.util.ordDiffEqSolver.BasicFunction theFunction;
    /**
     * An array containing the value of the function that was last calculated by the
     * RKF algorithm
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
    public RKF_S(hydroScalingAPI.util.ordDiffEqSolver.BasicFunction fu, double eps, double basTs) {
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
        int eqTOana = IC.length * 4 / 9;

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
        for (int i = 0; i < eqTOana; i++) {
            //&& Math.abs(newY[i] - newYstar[i])>0.001

            if ((newY[i] + newYstar[i]) > 0.0) {

                double newdelta = Math.abs(2 * (newY[i] - newYstar[i]) / (newY[i] + newYstar[i]));
                if (newY[i] < 0.01 && newYstar[i] < 0.01) {
                    newdelta = 0.1 * newdelta;
                }
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
////       out.write("---------> "+" N Link " +IC.length+" Link with prob=" + problink+"\n");
////   out.write("-------------> " + "Yval   " + Yprob + "Yvalprob    " + Ystarprob+"\n");
//
//out.close();
      // System.out.println("IC.length " +IC.length +" link error  "+ Math.floor(problink) + "  Yprob  " +Yprob + "  Y*prob  "+ Ystarprob +"--> "+timeStep+" "+epsilon+" "+Delta+" "+factor);
            //System.out.println("Yprob" +Yprob + "Yprob"+ Ystarprob + "--> "+timeStep+" "+epsilon+" "+Delta+" "+factor+" "+newTimeStep+" ("+java.util.Calendar.getInstance().getTime()+")");

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

        int ndif = IC.length / 14;
        newY = new double[IC.length];
        newYstar = new double[IC.length];
        for (int i = 0; i < ndif * 14; i++) {
            newY[i] = IC[i] + timeStep * (c[0] * k0[i] + c[1] * k1[i] + c[2] * k2[i] + c[3] * k3[i] + c[4] * k4[i] + c[5] * k5[i]);
            newY[i] = Math.max(0, newY[i]);
            newYstar[i] = IC[i] + timeStep * (cStar[0] * k0[i] + cStar[1] * k1[i] + cStar[2] * k2[i] + cStar[3] * k3[i] + cStar[4] * k4[i] + cStar[5] * k5[i]);
            newYstar[i] = Math.max(0, newYstar[i]);

        }

        for (int i = ndif * 14; i < IC.length; i++) {
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
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]  );
        //for (int j=0;j<IC.length/2;j++) System.out.print(IC[j]+" ");
        //System.out.println();
        Format formatter = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        PrintWriter out = new PrintWriter("C:/Users/tayalew/Documents/CuencasDataBases/ClearCreek_Database/Results/diagnostic.csv");
//        for (int i=0;i<linksStructure.completeStreamLinksArray.length;i++){
//            if(thisNetworkGeom.linkOrder(linksStructure.completeStreamLinksArray[i]) > 0)
//                out.print(thisNetworkGeom.Length(linksStructure.completeStreamLinksArray[i])+",");
//        }
//        out.println();
//        out.println("Time"+","+"Outlet Discharge"+","+"Reservoir"+","+"ReservoirUP1"+","+"ReservoirUP2"+","+"ReservoirDown"+","+"DownUp4"+","+"DownUp2");
//        out.println("Time"+","+"Outlet Discharge"+","+"Link-41"+","+"Link-18"+","+"Link-42"+","+"Link-17"+","+"Link-43");
//        out.println("Time"+","+"Outlet Discharge"+","+"Link-2523"+","+"Link-2524"+","+"Link-2525");
        //25 best reservoir locations on order 4 and 5
//        out.println("Time"+","+"Link-5501"+","+"Link-5504"+","+"Link-5517"+","+"Link-5556"+","+"Link-5562"+","+"Link-5570"+","+"Link-5581"+","+"Link-5587"+","+"Link-5595"+","+"Link-5599"+","+"Link-5606"+","+"Link-5619"+","+"Link-5629"+","+"Link-5663"+","+"Link-5670"+","+"Link-5677"+","+"Link-5716"+","+"Link-5725"+","+"Link-5735"+","+"Link-5742"+","+"Link-5755"+","+"Link-5763"+","+"Link-5803"+","+"Link-5840"+","+"Link-5889");
        //151 best reservoir locations on order 3 and 4
//         out.println("Time"+","+"Link-4752"+","+"Link-4755"+","+"Link-4768"+","+"Link-4771"+","+"Link-4774"+","+"Link-4783"+","+"Link-4788"+","+"Link-4789"+","+"Link-4795"+","+"Link-4798"+","+"Link-4802"+","+"Link-4808"+","+"Link-4815"+","+"Link-4819"+","+"Link-4826"+","+"Link-4828"+","+"Link-4831"+","+"Link-4833"+","+"Link-4841"+","+"Link-4844"+","+"Link-4847"+","+"Link-4851"+","+"Link-4852"+","+"Link-4856"+","+"Link-4861"+","+"Link-4866"+","+"Link-4868"+","+"Link-4870"+","+"Link-4872"+","+"Link-4877"+","+"Link-4882"+","+"Link-4883"+","+"Link-4888"+","+"Link-4895"+","+"Link-4899"+","+"Link-4912"+","+"Link-4921"+","+"Link-4935"+","+"Link-4940"+","+"Link-4952"+","+"Link-4957"+","+"Link-4970"+","+"Link-4975"+","+"Link-4980"+","+"Link-4982"+","+"Link-4986"+","+"Link-4990"+","+"Link-4994"+","+"Link-4997"+","+"Link-5004"+","+"Link-5007"+","+"Link-5010"+","+"Link-5017"+","+"Link-5020"+","+"Link-5025"+","+"Link-5030"+","+"Link-5034"+","+"Link-5038"+","+"Link-5049"+","+"Link-5066"+","+"Link-5071"+","+"Link-5072"+","+"Link-5078"+","+"Link-5086"+","+"Link-5087"+","+"Link-5093"+","+"Link-5098"+","+"Link-5101"+","+"Link-5107"+","+"Link-5110"+","+"Link-5113"+","+"Link-5116"+","+"Link-5117"+","+"Link-5121"+","+"Link-5124"+","+"Link-5127"+","+"Link-5137"+","+"Link-5144"+","+"Link-5148"+","+"Link-5154"+","+"Link-5158"+","+"Link-5166"+","+"Link-5172"+","+"Link-5176"+","+"Link-5180"+","+"Link-5182"+","+"Link-5190"+","+"Link-5193"+","+"Link-5197"+","+"Link-5202"+","+"Link-5206"+","+"Link-5210"+","+"Link-5220"+","+"Link-5224"+","+"Link-5237"+","+"Link-5246"+","+"Link-5253"+","+"Link-5259"+","+"Link-5263"+","+"Link-5275"+","+"Link-5279"+","+"Link-5292"+","+"Link-5296"+","+"Link-5319"+","+"Link-5336"+","+"Link-5355"+","+"Link-5366"+","+"Link-5369"+","+"Link-5373"+","+"Link-5391"+","+"Link-5397"+","+"Link-5404"+","+"Link-5407"+","+"Link-5414"+","+"Link-5419"+","+"Link-5427"+","+"Link-5431"+","+"Link-5437"+","+"Link-5444"+","+"Link-5446"+","+"Link-5456"+","+"Link-5466"+","+"Link-5468"+","+"Link-5478"+","+"Link-5486"+","+"Link-5491"+","+"Link-5501"+","+"Link-5504"+","+"Link-5517"+","+"Link-5556"+","+"Link-5562"+","+"Link-5570"+","+"Link-5581"+","+"Link-5587"+","+"Link-5595"+","+"Link-5599"+","+"Link-5606"+","+"Link-5619"+","+"Link-5629"+","+"Link-5663"+","+"Link-5670"+","+"Link-5677"+","+"Link-5716"+","+"Link-5725"+","+"Link-5735"+","+"Link-5742"+","+"Link-5755"+","+"Link-5763"+","+"Link-5803"+","+"Link-5840"+","+"Link-5889");

         //Order 4 and 5
//         out.println("Time"+","+"Link-98"+","+"Link-498"+","+"Link-658"+","+"Link-714"+","+"Link-828"+","+"Link-899"+","+"Link-1235"+","+"Link-1568"+","+"Link-1619"+","+"Link-1765"+","+"Link-2091"+","+"Link-2142"+","+"Link-2215"+","+"Link-3071"+","+"Link-2564"+","+"Link-2708"+","+"Link-2754"+","+"Link-5066"+","+"Link-2788"+","+"Link-2804"+","+"Link-2952"+","+"Link-3174"+","+"Link-3221"+","+"Link-3606"+","+"Link-3768"+","+"Link-3889"+","+"Link-3932"+","+"Link-4220"+","+"Link-4325"+","+"Link-4821"+","+"Link-4816"+","+"Link-4877"+","+"Link-4942"+","+"Link-4982"+","+"Link-5840"+","+"Link-5291"+","+"Link-5378"+","+"Link-5447"+","+"Link-5721"+","+"Link-6175"+","+"Link-6328");
         
        // One reservoir near the outlet
        out.println("Time"+","+"Link-2525"+","+"Link-2526"+","+"Link-2527");
        // Order 3
//        out.println("Time"+","+"Link-37"+","+"Link-87"+","+"Link-90"+","+"Link-73"+","+"Link-163"+","+"Link-212"+","+"Link-144"+","+"Link-295"+","+"Link-300"+","+"Link-325"+","+"Link-340"+","+"Link-362"+","+"Link-370"+","+"Link-395"+","+"Link-418"+","+"Link-435"+","+"Link-566"+","+"Link-571"+","+"Link-584"+","+"Link-590"+","+"Link-599"+","+"Link-475"+","+"Link-622"+","+"Link-637"+","+"Link-558"+","+"Link-752"+","+"Link-953"+","+"Link-1399"+","+"Link-1026"+","+"Link-1185"+","+"Link-1169"+","+"Link-1214"+","+"Link-1267"+","+"Link-1276"+","+"Link-1296"+","+"Link-1358"+","+"Link-1363"+","+"Link-1386"+","+"Link-1409"+","+"Link-1447"+","+"Link-1459"+","+"Link-1523"+","+"Link-1510"+","+"Link-1528"+","+"Link-1533"+","+"Link-1561"+","+"Link-1479"+","+"Link-1598"+","+"Link-1638"+","+"Link-1658"+","+"Link-1795"+","+"Link-2052"+","+"Link-1896"+","+"Link-1918"+","+"Link-1933"+","+"Link-1941"+","+"Link-1972"+","+"Link-1994"+","+"Link-2034"+","+"Link-2066"+","+"Link-2133"+","+"Link-2183"+","+"Link-2272"+","+"Link-2287"+","+"Link-2292"+","+"Link-2297"+","+"Link-2330"+","+"Link-2359"+","+"Link-2381"+","+"Link-2555"+","+"Link-2647"+","+"Link-2663"+","+"Link-2670"+","+"Link-3297"+","+"Link-2913"+","+"Link-2930"+","+"Link-3055"+","+"Link-3014"+","+"Link-3070"+","+"Link-3034"+","+"Link-3080"+","+"Link-3094"+","+"Link-3188"+","+"Link-3243"+","+"Link-3257"+","+"Link-3429"+","+"Link-3373"+","+"Link-3463"+","+"Link-3656"+","+"Link-3689"+","+"Link-3706"+","+"Link-3710"+","+"Link-3834"+","+"Link-4206"+","+"Link-3908"+","+"Link-3944"+","+"Link-3955"+","+"Link-3967"+","+"Link-3969"+","+"Link-3982"+","+"Link-3988"+","+"Link-4231"+","+"Link-3600"+","+"Link-4015"+","+"Link-4184"+","+"Link-4374"+","+"Link-4335"+","+"Link-4381"+","+"Link-4401"+","+"Link-4413"+","+"Link-4580"+","+"Link-4597"+","+"Link-4622"+","+"Link-4842"+","+"Link-4688"+","+"Link-4648"+","+"Link-4720"+","+"Link-4773"+","+"Link-4800"+","+"Link-4820"+","+"Link-4957"+","+"Link-4969"+","+"Link-4995"+","+"Link-5039"+","+"Link-5065"+","+"Link-4809"+","+"Link-5128"+","+"Link-5173"+","+"Link-5200"+","+"Link-5221"+","+"Link-5304"+","+"Link-5336"+","+"Link-5390"+","+"Link-5451"+","+"Link-5474"+","+"Link-5504"+","+"Link-5556"+","+"Link-5617"+","+"Link-5633"+","+"Link-5642"+","+"Link-5733"+","+"Link-5746"+","+"Link-5839"+","+"Link-5894"+","+"Link-6054"+","+"Link-5974"+","+"Link-5989"+","+"Link-6068"+","+"Link-6078"+","+"Link-6124"+","+"Link-6192"+","+"Link-6239"+","+"Link-6279"+","+"Link-6337"+","+"Link-6354");
        //order 3 and 4 nested
//        out.println("Time"+","+"Link-37"+","+"Link-87"+","+"Link-90"+","+"Link-73"+","+"Link-163"+","+"Link-212"+","+"Link-144"+","+"Link-295"+","+"Link-300"+","+"Link-325"+","+"Link-340"+","+"Link-362"+","+"Link-370"+","+"Link-395"+","+"Link-418"+","+"Link-435"+","+"Link-566"+","+"Link-571"+","+"Link-584"+","+"Link-590"+","+"Link-599"+","+"Link-475"+","+"Link-622"+","+"Link-637"+","+"Link-558"+","+"Link-752"+","+"Link-953"+","+"Link-1399"+","+"Link-1026"+","+"Link-1185"+","+"Link-1169"+","+"Link-1214"+","+"Link-1267"+","+"Link-1276"+","+"Link-1296"+","+"Link-1358"+","+"Link-1363"+","+"Link-1386"+","+"Link-1409"+","+"Link-1447"+","+"Link-1459"+","+"Link-1523"+","+"Link-1510"+","+"Link-1528"+","+"Link-1533"+","+"Link-1561"+","+"Link-1479"+","+"Link-1598"+","+"Link-1638"+","+"Link-1658"+","+"Link-1795"+","+"Link-2052"+","+"Link-1896"+","+"Link-1918"+","+"Link-1933"+","+"Link-1941"+","+"Link-1972"+","+"Link-1994"+","+"Link-2034"+","+"Link-2066"+","+"Link-2133"+","+"Link-2183"+","+"Link-2272"+","+"Link-2287"+","+"Link-2292"+","+"Link-2297"+","+"Link-2330"+","+"Link-2359"+","+"Link-2381"+","+"Link-2555"+","+"Link-2647"+","+"Link-2663"+","+"Link-2670"+","+"Link-3297"+","+"Link-2913"+","+"Link-2930"+","+"Link-3055"+","+"Link-3014"+","+"Link-3070"+","+"Link-3034"+","+"Link-3080"+","+"Link-3094"+","+"Link-3188"+","+"Link-3243"+","+"Link-3257"+","+"Link-3429"+","+"Link-3373"+","+"Link-3463"+","+"Link-3656"+","+"Link-3689"+","+"Link-3706"+","+"Link-3710"+","+"Link-3834"+","+"Link-4206"+","+"Link-3908"+","+"Link-3944"+","+"Link-3955"+","+"Link-3967"+","+"Link-3969"+","+"Link-3982"+","+"Link-3988"+","+"Link-4231"+","+"Link-3600"+","+"Link-4015"+","+"Link-4184"+","+"Link-4374"+","+"Link-4335"+","+"Link-4381"+","+"Link-4401"+","+"Link-4413"+","+"Link-4580"+","+"Link-4597"+","+"Link-4622"+","+"Link-4842"+","+"Link-4688"+","+"Link-4648"+","+"Link-4720"+","+"Link-4773"+","+"Link-4800"+","+"Link-4820"+","+"Link-4957"+","+"Link-4969"+","+"Link-4995"+","+"Link-5039"+","+"Link-5065"+","+"Link-4809"+","+"Link-5128"+","+"Link-5173"+","+"Link-5200"+","+"Link-5221"+","+"Link-5304"+","+"Link-5336"+","+"Link-5390"+","+"Link-5451"+","+"Link-5474"+","+"Link-5504"+","+"Link-5556"+","+"Link-5617"+","+"Link-5633"+","+"Link-5642"+","+"Link-5733"+","+"Link-5746"+","+"Link-5839"+","+"Link-5894"+","+"Link-6054"+","+"Link-5974"+","+"Link-5989"+","+"Link-6068"+","+"Link-6078"+","+"Link-6124"+","+"Link-6192"+","+"Link-6239"+","+"Link-6279"+","+"Link-6337"+","+"Link-6354"+","+"Link-98"+","+"Link-498"+","+"Link-658"+","+"Link-714"+","+"Link-1235"+","+"Link-1568"+","+"Link-1619"+","+"Link-1765"+","+"Link-2091"+","+"Link-2142"+","+"Link-2215"+","+"Link-3071"+","+"Link-2564"+","+"Link-2708"+","+"Link-5066"+","+"Link-2788"+","+"Link-2804"+","+"Link-3221"+","+"Link-3606"+","+"Link-3889"+","+"Link-3932"+","+"Link-4220"+","+"Link-4325"+","+"Link-4821"+","+"Link-4942"+","+"Link-4982"+","+"Link-5840"+","+"Link-5291"+","+"Link-5378"+","+"Link-5447"+","+"Link-5721"+","+"Link-6175"+","+"Link-6328");
        //ORDER 4 
//        out.println("Time"+","+"Link-98"+","+"Link-498"+","+"Link-658"+","+"Link-714"+","+"Link-1235"+","+"Link-1568"+","+"Link-1619"+","+"Link-1765"+","+"Link-2091"+","+"Link-2142"+","+"Link-2215"+","+"Link-3071"+","+"Link-2564"+","+"Link-2708"+","+"Link-5066"+","+"Link-2788"+","+"Link-2804"+","+"Link-3221"+","+"Link-3606"+","+"Link-3889"+","+"Link-3932"+","+"Link-4220"+","+"Link-4325"+","+"Link-4821"+","+"Link-4942"+","+"Link-4982"+","+"Link-5840"+","+"Link-5291"+","+"Link-5378"+","+"Link-5447"+","+"Link-5721"+","+"Link-6175"+","+"Link-6328");
//        out.println("Time"+","+"Link-828 "+","+"Link-899 "+","+"Link-2754 "+","+"Link-2952 "+","+"Link-3174 "+","+"Link-3768 "+","+"Link-4816 "+","+"Link-4877");
//        out.println(formatter.format(thisDate.getTime()) + "," + IC[ouletID] + "," + IC[41]  + "," + IC[18]  + "," + IC[42]  + "," + IC[17]  + "," + IC[43]);
        
        //  Order 4
//        out.println(thisDate.getTime()+"," +IC[98] + "," +IC[498] + "," +IC[658] + "," +IC[714] + "," +IC[1235] + "," +IC[1568] + "," +IC[1619] + "," +IC[1765] + "," +IC[2091] + "," +IC[2142] + "," +IC[2215] + "," +IC[3071] + "," +IC[2564] + "," +IC[2708] + "," +IC[5066] + "," +IC[2788] + "," +IC[2804] + "," +IC[3221] + "," +IC[3606] + "," +IC[3889] + "," +IC[3932] + "," +IC[4220] + "," +IC[4325] + "," +IC[4821] + "," +IC[4942] + "," +IC[4982] + "," +IC[5840] + "," +IC[5291] + "," +IC[5378] + "," +IC[5447] + "," +IC[5721] + "," +IC[6175] + "," +IC[6328]);
        //  Order 3
//        out.println(thisDate.getTime()+"," +IC[37] + "," +IC[87] + "," +IC[90] + "," +IC[73] + "," +IC[163] + "," +IC[212] + "," +IC[144] + "," +IC[295] + "," +IC[300] + "," +IC[325] + "," +IC[340] + "," +IC[362] + "," +IC[370] + "," +IC[395] + "," +IC[418] + "," +IC[435] + "," +IC[566] + "," +IC[571] + "," +IC[584] + "," +IC[590] + "," +IC[599] + "," +IC[475] + "," +IC[622] + "," +IC[637] + "," +IC[558] + "," +IC[752] + "," +IC[953] + "," +IC[1399] + "," +IC[1026] + "," +IC[1185] + "," +IC[1169] + "," +IC[1214] + "," +IC[1267] + "," +IC[1276] + "," +IC[1296] + "," +IC[1358] + "," +IC[1363] + "," +IC[1386] + "," +IC[1409] + "," +IC[1447] + "," +IC[1459] + "," +IC[1523] + "," +IC[1510] + "," +IC[1528] + "," +IC[1533] + "," +IC[1561] + "," +IC[1479] + "," +IC[1598] + "," +IC[1638] + "," +IC[1658] + "," +IC[1795] + "," +IC[2052] + "," +IC[1896] + "," +IC[1918] + "," +IC[1933] + "," +IC[1941] + "," +IC[1972] + "," +IC[1994] + "," +IC[2034] + "," +IC[2066] + "," +IC[2133] + "," +IC[2183] + "," +IC[2272] + "," +IC[2287] + "," +IC[2292] + "," +IC[2297] + "," +IC[2330] + "," +IC[2359] + "," +IC[2381] + "," +IC[2555] + "," +IC[2647] + "," +IC[2663] + "," +IC[2670] + "," +IC[3297] + "," +IC[2913] + "," +IC[2930] + "," +IC[3055] + "," +IC[3014] + "," +IC[3070] + "," +IC[3034] + "," +IC[3080] + "," +IC[3094] + "," +IC[3188] + "," +IC[3243] + "," +IC[3257] + "," +IC[3429] + "," +IC[3373] + "," +IC[3463] + "," +IC[3656] + "," +IC[3689] + "," +IC[3706] + "," +IC[3710] + "," +IC[3834] + "," +IC[4206] + "," +IC[3908] + "," +IC[3944] + "," +IC[3955] + "," +IC[3967] + "," +IC[3969] + "," +IC[3982] + "," +IC[3988] + "," +IC[4231] + "," +IC[3600] + "," +IC[4015] + "," +IC[4184] + "," +IC[4374] + "," +IC[4335] + "," +IC[4381] + "," +IC[4401] + "," +IC[4413] + "," +IC[4580] + "," +IC[4597] + "," +IC[4622] + "," +IC[4842] + "," +IC[4688] + "," +IC[4648] + "," +IC[4720] + "," +IC[4773] + "," +IC[4800] + "," +IC[4820] + "," +IC[4957] + "," +IC[4969] + "," +IC[4995] + "," +IC[5039] + "," +IC[5065] + "," +IC[4809] + "," +IC[5128] + "," +IC[5173] + "," +IC[5200] + "," +IC[5221] + "," +IC[5304] + "," +IC[5336] + "," +IC[5390] + "," +IC[5451] + "," +IC[5474] + "," +IC[5504] + "," +IC[5556] + "," +IC[5617] + "," +IC[5633] + "," +IC[5642] + "," +IC[5733] + "," +IC[5746] + "," +IC[5839] + "," +IC[5894] + "," +IC[6054] + "," +IC[5974] + "," +IC[5989] + "," +IC[6068] + "," +IC[6078] + "," +IC[6124] + "," +IC[6192] + "," +IC[6239] + "," +IC[6279] + "," +IC[6337] + "," +IC[6354]);
        // Order 3 and 4 nested
//        out.println(thisDate.getTime()+"," +IC[37] + "," +IC[87] + "," +IC[90] + "," +IC[73] + "," +IC[163] + "," +IC[212] + "," +IC[144] + "," +IC[295] + "," +IC[300] + "," +IC[325] + "," +IC[340] + "," +IC[362] + "," +IC[370] + "," +IC[395] + "," +IC[418] + "," +IC[435] + "," +IC[566] + "," +IC[571] + "," +IC[584] + "," +IC[590] + "," +IC[599] + "," +IC[475] + "," +IC[622] + "," +IC[637] + "," +IC[558] + "," +IC[752] + "," +IC[953] + "," +IC[1399] + "," +IC[1026] + "," +IC[1185] + "," +IC[1169] + "," +IC[1214] + "," +IC[1267] + "," +IC[1276] + "," +IC[1296] + "," +IC[1358] + "," +IC[1363] + "," +IC[1386] + "," +IC[1409] + "," +IC[1447] + "," +IC[1459] + "," +IC[1523] + "," +IC[1510] + "," +IC[1528] + "," +IC[1533] + "," +IC[1561] + "," +IC[1479] + "," +IC[1598] + "," +IC[1638] + "," +IC[1658] + "," +IC[1795] + "," +IC[2052] + "," +IC[1896] + "," +IC[1918] + "," +IC[1933] + "," +IC[1941] + "," +IC[1972] + "," +IC[1994] + "," +IC[2034] + "," +IC[2066] + "," +IC[2133] + "," +IC[2183] + "," +IC[2272] + "," +IC[2287] + "," +IC[2292] + "," +IC[2297] + "," +IC[2330] + "," +IC[2359] + "," +IC[2381] + "," +IC[2555] + "," +IC[2647] + "," +IC[2663] + "," +IC[2670] + "," +IC[3297] + "," +IC[2913] + "," +IC[2930] + "," +IC[3055] + "," +IC[3014] + "," +IC[3070] + "," +IC[3034] + "," +IC[3080] + "," +IC[3094] + "," +IC[3188] + "," +IC[3243] + "," +IC[3257] + "," +IC[3429] + "," +IC[3373] + "," +IC[3463] + "," +IC[3656] + "," +IC[3689] + "," +IC[3706] + "," +IC[3710] + "," +IC[3834] + "," +IC[4206] + "," +IC[3908] + "," +IC[3944] + "," +IC[3955] + "," +IC[3967] + "," +IC[3969] + "," +IC[3982] + "," +IC[3988] + "," +IC[4231] + "," +IC[3600] + "," +IC[4015] + "," +IC[4184] + "," +IC[4374] + "," +IC[4335] + "," +IC[4381] + "," +IC[4401] + "," +IC[4413] + "," +IC[4580] + "," +IC[4597] + "," +IC[4622] + "," +IC[4842] + "," +IC[4688] + "," +IC[4648] + "," +IC[4720] + "," +IC[4773] + "," +IC[4800] + "," +IC[4820] + "," +IC[4957] + "," +IC[4969] + "," +IC[4995] + "," +IC[5039] + "," +IC[5065] + "," +IC[4809] + "," +IC[5128] + "," +IC[5173] + "," +IC[5200] + "," +IC[5221] + "," +IC[5304] + "," +IC[5336] + "," +IC[5390] + "," +IC[5451] + "," +IC[5474] + "," +IC[5504] + "," +IC[5556] + "," +IC[5617] + "," +IC[5633] + "," +IC[5642] + "," +IC[5733] + "," +IC[5746] + "," +IC[5839] + "," +IC[5894] + "," +IC[6054] + "," +IC[5974] + "," +IC[5989] + "," +IC[6068] + "," +IC[6078] + "," +IC[6124] + "," +IC[6192] + "," +IC[6239] + "," +IC[6279] + "," +IC[6337] + "," +IC[6354]+"," +IC[98] + "," +IC[498] + "," +IC[658] + "," +IC[714] + "," +IC[1235] + "," +IC[1568] + "," +IC[1619] + "," +IC[1765] + "," +IC[2091] + "," +IC[2142] + "," +IC[2215] + "," +IC[3071] + "," +IC[2564] + "," +IC[2708] + "," +IC[5066] + "," +IC[2788] + "," +IC[2804] + "," +IC[3221] + "," +IC[3606] + "," +IC[3889] + "," +IC[3932] + "," +IC[4220] + "," +IC[4325] + "," +IC[4821] + "," +IC[4942] + "," +IC[4982] + "," +IC[5840] + "," +IC[5291] + "," +IC[5378] + "," +IC[5447] + "," +IC[5721] + "," +IC[6175] + "," +IC[6328]);
//        out.println(thisDate.getTime()+","+IC[828] + "," +IC[899] + "," +IC[2754] + "," +IC[2952] + "," +IC[3174] + "," +IC[3768] + "," +IC[4816] + "," +IC[4877]);
        // One reservoir case near the  outlet
        out.println(thisDate.getTime()+"," +IC[2525]+"," +IC[2526]+"," +IC[2527]);
        
        //25 best reservoir locations        
//        out.println(thisDate.getTime()+"," +IC[5501] + "," +IC[5504] + "," +IC[5517] + "," +IC[5556] + "," +IC[5562] + "," +IC[5570] + "," +IC[5581] + "," +IC[5587] + "," +IC[5595] + "," +IC[5599] + "," +IC[5606] + "," +IC[5619] + "," +IC[5629] + "," +IC[5663] + "," +IC[5670] + "," +IC[5677] + "," +IC[5716] + "," +IC[5725] + "," +IC[5735] + "," +IC[5742] + "," +IC[5755] + "," +IC[5763] + "," +IC[5803] + "," +IC[5840] + "," +IC[5889]);
        
         //151 best reservoir locations on order 3 and 4
//        out.println(thisDate.getTime()+"," +IC[4752] + "," +IC[4755] + "," +IC[4768] + "," +IC[4771] + "," +IC[4774] + "," +IC[4783] + "," +IC[4788] + "," +IC[4789] + "," +IC[4795] + "," +IC[4798] + "," +IC[4802] + "," +IC[4808] + "," +IC[4815] + "," +IC[4819] + "," +IC[4826] + "," +IC[4828] + "," +IC[4831] + "," +IC[4833] + "," +IC[4841] + "," +IC[4844] + "," +IC[4847] + "," +IC[4851] + "," +IC[4852] + "," +IC[4856] + "," +IC[4861] + "," +IC[4866] + "," +IC[4868] + "," +IC[4870] + "," +IC[4872] + "," +IC[4877] + "," +IC[4882] + "," +IC[4883] + "," +IC[4888] + "," +IC[4895] + "," +IC[4899] + "," +IC[4912] + "," +IC[4921] + "," +IC[4935] + "," +IC[4940] + "," +IC[4952] + "," +IC[4957] + "," +IC[4970] + "," +IC[4975] + "," +IC[4980] + "," +IC[4982] + "," +IC[4986] + "," +IC[4990] + "," +IC[4994] + "," +IC[4997] + "," +IC[5004] + "," +IC[5007] + "," +IC[5010] + "," +IC[5017] + "," +IC[5020] + "," +IC[5025] + "," +IC[5030] + "," +IC[5034] + "," +IC[5038] + "," +IC[5049] + "," +IC[5066] + "," +IC[5071] + "," +IC[5072] + "," +IC[5078] + "," +IC[5086] + "," +IC[5087] + "," +IC[5093] + "," +IC[5098] + "," +IC[5101] + "," +IC[5107] + "," +IC[5110] + "," +IC[5113] + "," +IC[5116] + "," +IC[5117] + "," +IC[5121] + "," +IC[5124] + "," +IC[5127] + "," +IC[5137] + "," +IC[5144] + "," +IC[5148] + "," +IC[5154] + "," +IC[5158] + "," +IC[5166] + "," +IC[5172] + "," +IC[5176] + "," +IC[5180] + "," +IC[5182] + "," +IC[5190] + "," +IC[5193] + "," +IC[5197] + "," +IC[5202] + "," +IC[5206] + "," +IC[5210] + "," +IC[5220] + "," +IC[5224] + "," +IC[5237] + "," +IC[5246] + "," +IC[5253] + "," +IC[5259] + "," +IC[5263] + "," +IC[5275] + "," +IC[5279] + "," +IC[5292] + "," +IC[5296] + "," +IC[5319] + "," +IC[5336] + "," +IC[5355] + "," +IC[5366] + "," +IC[5369] + "," +IC[5373] + "," +IC[5391] + "," +IC[5397] + "," +IC[5404] + "," +IC[5407] + "," +IC[5414] + "," +IC[5419] + "," +IC[5427] + "," +IC[5431] + "," +IC[5437] + "," +IC[5444] + "," +IC[5446] + "," +IC[5456] + "," +IC[5466] + "," +IC[5468] + "," +IC[5478] + "," +IC[5486] + "," +IC[5491] + "," +IC[5501] + "," +IC[5504] + "," +IC[5517] + "," +IC[5556] + "," +IC[5562] + "," +IC[5570] + "," +IC[5581] + "," +IC[5587] + "," +IC[5595] + "," +IC[5599] + "," +IC[5606] + "," +IC[5619] + "," +IC[5629] + "," +IC[5663] + "," +IC[5670] + "," +IC[5677] + "," +IC[5716] + "," +IC[5725] + "," +IC[5735] + "," +IC[5742] + "," +IC[5755] + "," +IC[5763] + "," +IC[5803] + "," +IC[5840] + "," +IC[5889]);

        //Order 4 and 5
//        out.println(thisDate.getTime()+"," +IC[98] + "," +IC[498] + "," +IC[658] + "," +IC[714] + "," +IC[828] + "," +IC[899] + "," +IC[1235] + "," +IC[1568] + "," +IC[1619] + "," +IC[1765] + "," +IC[2091] + "," +IC[2142] + "," +IC[2215] + "," +IC[3071] + "," +IC[2564] + "," +IC[2708] + "," +IC[2754] + "," +IC[5066] + "," +IC[2788] + "," +IC[2804] + "," +IC[2952] + "," +IC[3174] + "," +IC[3221] + "," +IC[3606] + "," +IC[3768] + "," +IC[3889] + "," +IC[3932] + "," +IC[4220] + "," +IC[4325] + "," +IC[4821] + "," +IC[4816] + "," +IC[4877] + "," +IC[4942] + "," +IC[4982] + "," +IC[5840] + "," +IC[5291] + "," +IC[5378] + "," +IC[5447] + "," +IC[5721] + "," +IC[6175] + "," +IC[6328]);       
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
//                    System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID] + " Reservoir: " + IC[16]  + " ReservoirUp1: " + IC[11]  + " ReservoirUp2: " + IC[15]  + " ReservoirDown: " + IC[41]  + " DownUp4: " + IC[40]  + " DownUp2: " + IC[34] );
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
            
            for (int i = 0; i < IC.length; i++) {
                if (IC[i] > maxAchieved[i]) {
                    maxAchieved[i] = IC[i];
                    timeOfMaximumAchieved[i] = currentTime;
                }
            }

            thisDate = java.util.Calendar.getInstance();
            thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
//            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID] + " Reservoir: " + IC[16]  + " ReservoirUp1: " + IC[11]  + " ReservoirUp2: " + IC[15]  + " ReservoirDown: " + IC[41]  + " DownUp4: " + IC[40]  + " DownUp2: " + IC[34] );
            
            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]  );
//            for (int j=IC.length/2;j<IC.length;j++) System.out.print(IC[j]+" ");
//            System.out.println();
//            if(Math.random()<0.2) System.exit(0);
//            out.println(formatter.format(thisDate.getTime()) + "," + IC[ouletID] + "," + IC[16]  + "," + IC[11]  + "," + IC[15]  + "," + IC[41]  + "," + IC[40]  + "," + IC[34] );
//            out.println(thisDate.getTime() + "," + IC[ouletID] + "," + IC[41]  + "," + IC[18]  + "," + IC[42]  + "," + IC[17]  + "," + IC[43]);
//            out.println((thisDate.getTime()) + "," + IC[ouletID] + "," + IC[2523]  + "," + IC[2524]+ "," + IC[2525]);
//            out.println((thisDate.getTime()) + "," + IC[ouletID] + "," + IC[2695]  + "," + IC[2696]+ "," + IC[2697]+","+"   "+","+IC[4171]  + "," + IC[4172]+ "," + IC[4173]);
//            out.println(thisDate.getTime()+"," +IC[98] + "," +IC[498] + "," +IC[658] + "," +IC[714] + "," +IC[1235] + "," +IC[1568] + "," +IC[1619] + "," +IC[1765] + "," +IC[2091] + "," +IC[2142] + "," +IC[2215] + "," +IC[3071] + "," +IC[2564] + "," +IC[2708] + "," +IC[5066] + "," +IC[2788] + "," +IC[2804] + "," +IC[3221] + "," +IC[3606] + "," +IC[3889] + "," +IC[3932] + "," +IC[4220] + "," +IC[4325] + "," +IC[4821] + "," +IC[4942] + "," +IC[4982] + "," +IC[5840] + "," +IC[5291] + "," +IC[5378] + "," +IC[5447] + "," +IC[5721] + "," +IC[6175] + "," +IC[6328]);
           // Order 3
//            out.println(thisDate.getTime()+"," +IC[37] + "," +IC[87] + "," +IC[90] + "," +IC[73] + "," +IC[163] + "," +IC[212] + "," +IC[144] + "," +IC[295] + "," +IC[300] + "," +IC[325] + "," +IC[340] + "," +IC[362] + "," +IC[370] + "," +IC[395] + "," +IC[418] + "," +IC[435] + "," +IC[566] + "," +IC[571] + "," +IC[584] + "," +IC[590] + "," +IC[599] + "," +IC[475] + "," +IC[622] + "," +IC[637] + "," +IC[558] + "," +IC[752] + "," +IC[953] + "," +IC[1399] + "," +IC[1026] + "," +IC[1185] + "," +IC[1169] + "," +IC[1214] + "," +IC[1267] + "," +IC[1276] + "," +IC[1296] + "," +IC[1358] + "," +IC[1363] + "," +IC[1386] + "," +IC[1409] + "," +IC[1447] + "," +IC[1459] + "," +IC[1523] + "," +IC[1510] + "," +IC[1528] + "," +IC[1533] + "," +IC[1561] + "," +IC[1479] + "," +IC[1598] + "," +IC[1638] + "," +IC[1658] + "," +IC[1795] + "," +IC[2052] + "," +IC[1896] + "," +IC[1918] + "," +IC[1933] + "," +IC[1941] + "," +IC[1972] + "," +IC[1994] + "," +IC[2034] + "," +IC[2066] + "," +IC[2133] + "," +IC[2183] + "," +IC[2272] + "," +IC[2287] + "," +IC[2292] + "," +IC[2297] + "," +IC[2330] + "," +IC[2359] + "," +IC[2381] + "," +IC[2555] + "," +IC[2647] + "," +IC[2663] + "," +IC[2670] + "," +IC[3297] + "," +IC[2913] + "," +IC[2930] + "," +IC[3055] + "," +IC[3014] + "," +IC[3070] + "," +IC[3034] + "," +IC[3080] + "," +IC[3094] + "," +IC[3188] + "," +IC[3243] + "," +IC[3257] + "," +IC[3429] + "," +IC[3373] + "," +IC[3463] + "," +IC[3656] + "," +IC[3689] + "," +IC[3706] + "," +IC[3710] + "," +IC[3834] + "," +IC[4206] + "," +IC[3908] + "," +IC[3944] + "," +IC[3955] + "," +IC[3967] + "," +IC[3969] + "," +IC[3982] + "," +IC[3988] + "," +IC[4231] + "," +IC[3600] + "," +IC[4015] + "," +IC[4184] + "," +IC[4374] + "," +IC[4335] + "," +IC[4381] + "," +IC[4401] + "," +IC[4413] + "," +IC[4580] + "," +IC[4597] + "," +IC[4622] + "," +IC[4842] + "," +IC[4688] + "," +IC[4648] + "," +IC[4720] + "," +IC[4773] + "," +IC[4800] + "," +IC[4820] + "," +IC[4957] + "," +IC[4969] + "," +IC[4995] + "," +IC[5039] + "," +IC[5065] + "," +IC[4809] + "," +IC[5128] + "," +IC[5173] + "," +IC[5200] + "," +IC[5221] + "," +IC[5304] + "," +IC[5336] + "," +IC[5390] + "," +IC[5451] + "," +IC[5474] + "," +IC[5504] + "," +IC[5556] + "," +IC[5617] + "," +IC[5633] + "," +IC[5642] + "," +IC[5733] + "," +IC[5746] + "," +IC[5839] + "," +IC[5894] + "," +IC[6054] + "," +IC[5974] + "," +IC[5989] + "," +IC[6068] + "," +IC[6078] + "," +IC[6124] + "," +IC[6192] + "," +IC[6239] + "," +IC[6279] + "," +IC[6337] + "," +IC[6354]);
            // Order 3 and 4 nested
//        out.println(thisDate.getTime()+"," +IC[37] + "," +IC[87] + "," +IC[90] + "," +IC[73] + "," +IC[163] + "," +IC[212] + "," +IC[144] + "," +IC[295] + "," +IC[300] + "," +IC[325] + "," +IC[340] + "," +IC[362] + "," +IC[370] + "," +IC[395] + "," +IC[418] + "," +IC[435] + "," +IC[566] + "," +IC[571] + "," +IC[584] + "," +IC[590] + "," +IC[599] + "," +IC[475] + "," +IC[622] + "," +IC[637] + "," +IC[558] + "," +IC[752] + "," +IC[953] + "," +IC[1399] + "," +IC[1026] + "," +IC[1185] + "," +IC[1169] + "," +IC[1214] + "," +IC[1267] + "," +IC[1276] + "," +IC[1296] + "," +IC[1358] + "," +IC[1363] + "," +IC[1386] + "," +IC[1409] + "," +IC[1447] + "," +IC[1459] + "," +IC[1523] + "," +IC[1510] + "," +IC[1528] + "," +IC[1533] + "," +IC[1561] + "," +IC[1479] + "," +IC[1598] + "," +IC[1638] + "," +IC[1658] + "," +IC[1795] + "," +IC[2052] + "," +IC[1896] + "," +IC[1918] + "," +IC[1933] + "," +IC[1941] + "," +IC[1972] + "," +IC[1994] + "," +IC[2034] + "," +IC[2066] + "," +IC[2133] + "," +IC[2183] + "," +IC[2272] + "," +IC[2287] + "," +IC[2292] + "," +IC[2297] + "," +IC[2330] + "," +IC[2359] + "," +IC[2381] + "," +IC[2555] + "," +IC[2647] + "," +IC[2663] + "," +IC[2670] + "," +IC[3297] + "," +IC[2913] + "," +IC[2930] + "," +IC[3055] + "," +IC[3014] + "," +IC[3070] + "," +IC[3034] + "," +IC[3080] + "," +IC[3094] + "," +IC[3188] + "," +IC[3243] + "," +IC[3257] + "," +IC[3429] + "," +IC[3373] + "," +IC[3463] + "," +IC[3656] + "," +IC[3689] + "," +IC[3706] + "," +IC[3710] + "," +IC[3834] + "," +IC[4206] + "," +IC[3908] + "," +IC[3944] + "," +IC[3955] + "," +IC[3967] + "," +IC[3969] + "," +IC[3982] + "," +IC[3988] + "," +IC[4231] + "," +IC[3600] + "," +IC[4015] + "," +IC[4184] + "," +IC[4374] + "," +IC[4335] + "," +IC[4381] + "," +IC[4401] + "," +IC[4413] + "," +IC[4580] + "," +IC[4597] + "," +IC[4622] + "," +IC[4842] + "," +IC[4688] + "," +IC[4648] + "," +IC[4720] + "," +IC[4773] + "," +IC[4800] + "," +IC[4820] + "," +IC[4957] + "," +IC[4969] + "," +IC[4995] + "," +IC[5039] + "," +IC[5065] + "," +IC[4809] + "," +IC[5128] + "," +IC[5173] + "," +IC[5200] + "," +IC[5221] + "," +IC[5304] + "," +IC[5336] + "," +IC[5390] + "," +IC[5451] + "," +IC[5474] + "," +IC[5504] + "," +IC[5556] + "," +IC[5617] + "," +IC[5633] + "," +IC[5642] + "," +IC[5733] + "," +IC[5746] + "," +IC[5839] + "," +IC[5894] + "," +IC[6054] + "," +IC[5974] + "," +IC[5989] + "," +IC[6068] + "," +IC[6078] + "," +IC[6124] + "," +IC[6192] + "," +IC[6239] + "," +IC[6279] + "," +IC[6337] + "," +IC[6354]+"," +IC[98] + "," +IC[498] + "," +IC[658] + "," +IC[714] + "," +IC[1235] + "," +IC[1568] + "," +IC[1619] + "," +IC[1765] + "," +IC[2091] + "," +IC[2142] + "," +IC[2215] + "," +IC[3071] + "," +IC[2564] + "," +IC[2708] + "," +IC[5066] + "," +IC[2788] + "," +IC[2804] + "," +IC[3221] + "," +IC[3606] + "," +IC[3889] + "," +IC[3932] + "," +IC[4220] + "," +IC[4325] + "," +IC[4821] + "," +IC[4942] + "," +IC[4982] + "," +IC[5840] + "," +IC[5291] + "," +IC[5378] + "," +IC[5447] + "," +IC[5721] + "," +IC[6175] + "," +IC[6328]);
//            out.println(thisDate.getTime()+","+IC[828] + "," +IC[899] + "," +IC[2754] + "," +IC[2952] + "," +IC[3174] + "," +IC[3768] + "," +IC[4816] + "," +IC[4877]);
       // One reservoir case near the  outlet
        out.println(thisDate.getTime()+"," +IC[2525]+"," +IC[2526]+"," +IC[2527]);
            
            //25 best reservoir locations        
//         out.println(thisDate.getTime()+"," +IC[5501] + "," +IC[5504] + "," +IC[5517] + "," +IC[5556] + "," +IC[5562] + "," +IC[5570] + "," +IC[5581] + "," +IC[5587] + "," +IC[5595] + "," +IC[5599] + "," +IC[5606] + "," +IC[5619] + "," +IC[5629] + "," +IC[5663] + "," +IC[5670] + "," +IC[5677] + "," +IC[5716] + "," +IC[5725] + "," +IC[5735] + "," +IC[5742] + "," +IC[5755] + "," +IC[5763] + "," +IC[5803] + "," +IC[5840] + "," +IC[5889]);
        
           //151 best reservoir locations on order 3 and 4
//            out.println(thisDate.getTime()+"," +IC[4752] + "," +IC[4755] + "," +IC[4768] + "," +IC[4771] + "," +IC[4774] + "," +IC[4783] + "," +IC[4788] + "," +IC[4789] + "," +IC[4795] + "," +IC[4798] + "," +IC[4802] + "," +IC[4808] + "," +IC[4815] + "," +IC[4819] + "," +IC[4826] + "," +IC[4828] + "," +IC[4831] + "," +IC[4833] + "," +IC[4841] + "," +IC[4844] + "," +IC[4847] + "," +IC[4851] + "," +IC[4852] + "," +IC[4856] + "," +IC[4861] + "," +IC[4866] + "," +IC[4868] + "," +IC[4870] + "," +IC[4872] + "," +IC[4877] + "," +IC[4882] + "," +IC[4883] + "," +IC[4888] + "," +IC[4895] + "," +IC[4899] + "," +IC[4912] + "," +IC[4921] + "," +IC[4935] + "," +IC[4940] + "," +IC[4952] + "," +IC[4957] + "," +IC[4970] + "," +IC[4975] + "," +IC[4980] + "," +IC[4982] + "," +IC[4986] + "," +IC[4990] + "," +IC[4994] + "," +IC[4997] + "," +IC[5004] + "," +IC[5007] + "," +IC[5010] + "," +IC[5017] + "," +IC[5020] + "," +IC[5025] + "," +IC[5030] + "," +IC[5034] + "," +IC[5038] + "," +IC[5049] + "," +IC[5066] + "," +IC[5071] + "," +IC[5072] + "," +IC[5078] + "," +IC[5086] + "," +IC[5087] + "," +IC[5093] + "," +IC[5098] + "," +IC[5101] + "," +IC[5107] + "," +IC[5110] + "," +IC[5113] + "," +IC[5116] + "," +IC[5117] + "," +IC[5121] + "," +IC[5124] + "," +IC[5127] + "," +IC[5137] + "," +IC[5144] + "," +IC[5148] + "," +IC[5154] + "," +IC[5158] + "," +IC[5166] + "," +IC[5172] + "," +IC[5176] + "," +IC[5180] + "," +IC[5182] + "," +IC[5190] + "," +IC[5193] + "," +IC[5197] + "," +IC[5202] + "," +IC[5206] + "," +IC[5210] + "," +IC[5220] + "," +IC[5224] + "," +IC[5237] + "," +IC[5246] + "," +IC[5253] + "," +IC[5259] + "," +IC[5263] + "," +IC[5275] + "," +IC[5279] + "," +IC[5292] + "," +IC[5296] + "," +IC[5319] + "," +IC[5336] + "," +IC[5355] + "," +IC[5366] + "," +IC[5369] + "," +IC[5373] + "," +IC[5391] + "," +IC[5397] + "," +IC[5404] + "," +IC[5407] + "," +IC[5414] + "," +IC[5419] + "," +IC[5427] + "," +IC[5431] + "," +IC[5437] + "," +IC[5444] + "," +IC[5446] + "," +IC[5456] + "," +IC[5466] + "," +IC[5468] + "," +IC[5478] + "," +IC[5486] + "," +IC[5491] + "," +IC[5501] + "," +IC[5504] + "," +IC[5517] + "," +IC[5556] + "," +IC[5562] + "," +IC[5570] + "," +IC[5581] + "," +IC[5587] + "," +IC[5595] + "," +IC[5599] + "," +IC[5606] + "," +IC[5619] + "," +IC[5629] + "," +IC[5663] + "," +IC[5670] + "," +IC[5677] + "," +IC[5716] + "," +IC[5725] + "," +IC[5735] + "," +IC[5742] + "," +IC[5755] + "," +IC[5763] + "," +IC[5803] + "," +IC[5840] + "," +IC[5889]);
        //Order 4 and 5
//        out.println(thisDate.getTime()+"," +IC[98] + "," +IC[498] + "," +IC[658] + "," +IC[714] + "," +IC[828] + "," +IC[899] + "," +IC[1235] + "," +IC[1568] + "," +IC[1619] + "," +IC[1765] + "," +IC[2091] + "," +IC[2142] + "," +IC[2215] + "," +IC[3071] + "," +IC[2564] + "," +IC[2708] + "," +IC[2754] + "," +IC[5066] + "," +IC[2788] + "," +IC[2804] + "," +IC[2952] + "," +IC[3174] + "," +IC[3221] + "," +IC[3606] + "," +IC[3768] + "," +IC[3889] + "," +IC[3932] + "," +IC[4220] + "," +IC[4325] + "," +IC[4821] + "," +IC[4816] + "," +IC[4877] + "," +IC[4942] + "," +IC[4982] + "," +IC[5840] + "," +IC[5291] + "," +IC[5378] + "," +IC[5447] + "," +IC[5721] + "," +IC[6175] + "," +IC[6328]);       
//        
        }
        out.close();

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
//            System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);   //Tibebu

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
     * @param thisNetworkGeom The description the the hydraulic and geomorphic parameters
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
    public void jumpsRunCompleteToAsciiFileSCS(double iniTime, double finalTime, double incrementalTime, double[] IC, java.io.OutputStreamWriter outputStream, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom, java.io.OutputStreamWriter outputStream1, java.io.OutputStreamWriter outputStream2, java.io.OutputStreamWriter outputStream3, int outflat) throws java.io.IOException {

        double currentTime = iniTime, targetTime;
        DecimalFormat df5 = new DecimalFormat("###.######");
        int ouletID = linksStructure.getOutletID();
        int nlinks = linksStructure.contactsArray.length;


        java.util.Calendar thisDate = java.util.Calendar.getInstance();
        thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
        System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
        //System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " water table: " + IC[ouletID + 2 * nlinks]);
        //System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " qs_l: " + IC[ouletID + 7 * nlinks]);
        outputStream1.write(currentTime + "," + df5.format(IC[ouletID]) + "\n");
        outputStream1.flush();
        if (outflat > 0) {
            outputStream2.write(currentTime + "," + df5.format(IC[nlinks + ouletID]) + "," + df5.format(IC[2 * nlinks + ouletID]) + "," + df5.format(IC[3 * nlinks + ouletID]) + "\n");
            outputStream2.flush();
            if (outflat > 1) {
                outputStream3.write(currentTime + "," + df5.format(IC[4 * nlinks + ouletID]) + "," + df5.format(IC[5 * nlinks + ouletID]) + "," + df5.format(IC[6 * nlinks + ouletID]) + "," + df5.format(IC[7 * nlinks + ouletID]) + "," + df5.format(IC[8 * nlinks + ouletID]) + "\n");
                outputStream3.flush();
            }
        }

        double[][] givenStep;

        while (currentTime < finalTime) {
            targetTime = currentTime + incrementalTime;

            while (currentTime < targetTime) {

                givenStep = stepSCS(currentTime, IC, basicTimeStep, false);

                if (currentTime + givenStep[0][0] >= targetTime) {
                    //System.out.println("******** False Step ********");
                    break;
                }

                basicTimeStep = givenStep[0][0];
                currentTime += basicTimeStep;
                givenStep[0][0] = currentTime;
                IC = givenStep[1];

                if (currentTime <= targetTime-1/60.) {
                    for (int i = 0; i < IC.length; i++) {
                        if (IC[i] > maxAchieved[i]) {
                            maxAchieved[i] = IC[i];
                            timeOfMaximumAchieved[i] = currentTime;
                        }
                    }
                    thisDate = java.util.Calendar.getInstance();
                    thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
                    System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
                    //System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " water table: " + IC[ouletID + 2 * nlinks]);
                    //System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " qs_l: " + IC[ouletID + 7 * nlinks]);
                    outputStream1.write(currentTime + "," + df5.format(IC[ouletID]) + "\n");
                    outputStream1.flush();
                    if (outflat > 0) {
                        outputStream2.write(currentTime + "," + df5.format(IC[nlinks + ouletID]) + "," + df5.format(IC[2 * nlinks + ouletID]) + "," + df5.format(IC[3 * nlinks + ouletID]) + "\n");
                        outputStream2.flush();
                        if (outflat > 1) {
                            outputStream3.write(currentTime + "," + df5.format(IC[4 * nlinks + ouletID]) + "," + df5.format(IC[5 * nlinks + ouletID]) + "," + df5.format(IC[6 * nlinks + ouletID]) + "," + df5.format(IC[7 * nlinks + ouletID]) + "," + df5.format(IC[8 * nlinks + ouletID]) + "\n");
                            outputStream3.flush();
                        }
                    }
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

            if (currentTime + givenStep[0][0] >= finalTime-1 / 60.) {
                System.out.println("******** False Step ********");
                break;
            }

            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

        if (currentTime <= finalTime-1/60.)    
        {
                    for (int i = 0; i < IC.length; i++) {
                        if (IC[i] > maxAchieved[i]) {
                            maxAchieved[i] = IC[i];
                            timeOfMaximumAchieved[i] = currentTime;
                        }
                    }
                    thisDate = java.util.Calendar.getInstance();
                    thisDate.setTimeInMillis((long) (currentTime * 60. * 1000.0));
                    System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " Outlet Discharge: " + IC[ouletID]);
                    //System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " water table: " + IC[ouletID + 2 * nlinks]);
                    //System.out.println(thisDate.getTime() + " (" + java.util.Calendar.getInstance().getTime() + ")" + " qs_l: " + IC[ouletID + 7 * nlinks]);
                    outputStream1.write(currentTime + "," + df5.format(IC[ouletID]) + "\n");
                    outputStream1.flush();
                    if (outflat > 0) {
                        outputStream2.write(currentTime + "," + df5.format(IC[nlinks + ouletID]) + "," + df5.format(IC[2 * nlinks + ouletID]) + "," + df5.format(IC[3 * nlinks + ouletID]) + "\n");
                        outputStream2.flush();
                        if (outflat > 1) {
                            outputStream3.write(currentTime + "," + df5.format(IC[4 * nlinks + ouletID]) + "," + df5.format(IC[5 * nlinks + ouletID]) + "," + df5.format(IC[6 * nlinks + ouletID]) + "," + df5.format(IC[7 * nlinks + ouletID]) + "," + df5.format(IC[8 * nlinks + ouletID]) + "\n");
                            outputStream3.flush();
                        }
                    }
                }
//        
        }

        if (currentTime != finalTime) {
            givenStep = stepSCS(currentTime, IC, finalTime - currentTime - 1 / 60., true);
            basicTimeStep = givenStep[0][0];
            currentTime += basicTimeStep;
            givenStep[0][0] = currentTime;
            IC = givenStep[1];

          
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
                givenStep[0][0] = currentTime;
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
            givenStep[0][0] = currentTime;
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
            givenStep[0][0] = currentTime;
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
        answer = new RKF_S(funcionLorenz, 1e-4, .001).jumpsRun(0, 1000, 0.2, new double[]{-13, -12, 52});
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
