/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.util.statistics;
/*     Example of the use of the class Regression demonstrating
       the use of the non-linear regression method, Regression.simplexPlot
       in fitting data to the function, y = a + b.exp(-c.x)
       with b fixed and a and c unknown

       Michael Thomas Flanagan
       http://www.ee.ucl.ac.uk/~mflanaga/java/Regression.html
       April 2007
*/

import flanagan.analysis.Regression;
import flanagan.analysis.RegressionFunction;

// Class to evaluate the function y = a + b.exp(-c.x1)
// where b is fixed and best estimates are required for a and  c.
class FunctOne implements RegressionFunction{

         private double b = 0.0D;

         public double function(double[ ] p, double[ ] x){
                  double y = p[0] + b*Math.exp(-p[1]*x[0]);
                  return y;
         }

         public void setB(double b){
            this.b = b;
         }
}

// Class to demonstrate non-linear regression method, Regression.simplex.
public class LinearRegExample{

         public static void main(String[] arg){

                  // x data array
                  double[] xArray = {0.0,0.5,1.0,1.5,2.0,2.5,3.0,3.5,4.0,4.5,5.0,5.5,6.0,6.5,7.0,7.5};
                  // observed y data array
                  double[] yArray = {10.9996,8.8481,7.1415,6.5376,6.466,5.1026,4.7215,3.7115,3.8383,3.4997,3.7972,3.4459,2.8564,3.291,3.0518,3.6073};
                  // estimates of the standard deviations of y
                  double[] sdArray = {0.5,0.45,0.55,0.44,0.46,0.51,0.56,0.48,0.5,0.45,0.55,0.44,0.46,0.51,0.56,0.48};


                  // Create instances of the class holding the function, y = a + b.exp(-c.x), evaluation method
                  FunctOne f1 = new FunctOne();

                  // assign value to constant b in the function
                  f1.setB(8.0D);

                  // initial estimates of a and c in y = a + b.exp(-c.x)
                  double[] start = new double[2];
                  start[0] = 6.0D;      // initial estimate of a
                  start[1] = 0.1D;      // initial estimate of c

                  // initial step sizes for a and c in y = a + b.exp(-c.x)
                  double[] step = new double[2];
                  step[0] = 0.6D;      // initial step size for a
                  step[1] = 0.05D;     // initial step size for c

                  // create an instance of Regression
                  Regression reg = new Regression(xArray, yArray, sdArray);
                  
                  // call non-linear regression using default tolerance and maximum iterations and plot display option
                  
                  reg.polynomialPlot(3, 0.0);
               
         }
}