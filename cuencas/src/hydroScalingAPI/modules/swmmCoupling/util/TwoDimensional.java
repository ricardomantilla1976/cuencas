/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.swmmCoupling.util;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.impl.CoordinateArraySequence;
import java.util.ArrayList;

/**
 * Class to solve Two Dimensional simple problems
 * @author A. D. L. Zanchetta
 * @version 0.1b (16/09/2013)
 */
public class TwoDimensional {
    
    /**
     * Verify if semi-lines M and N has a crossing point
     * @param x1m_arg Point X 1 of semi-line M
     * @param y1m_arg Point Y 1 of semi-line M
     * @param x2m_arg Point X 2 of semi-line M
     * @param y2m_arg Point Y 2 of semi-line M
     * @param x1n_arg Point X 1 of semi-line N
     * @param y1n_arg Point Y 1 of semi-line N
     * @param x2n_arg Point X 2 of semi-line N
     * @param y2n_arg Point Y 2 of semi-line N
     * @return TRUE if there is this point, FALSE otherwise
     */
    public static boolean isSemiLinesCrossing(int x1m_arg, int y1m_arg, 
                                              int x2m_arg, int y2m_arg,
                                              int x1n_arg, int y1n_arg, 
                                              int x2n_arg, int y2n_arg){
        double aLineM, bLineM;
        double aLineN, bLineN;
        double crossingX, crossingY;
        
        // determine M line
        aLineM = TwoDimensional.getLineAValue(x1m_arg, y1m_arg, x2m_arg, y2m_arg);
        bLineM = TwoDimensional.getLineBValue(x1m_arg, y1m_arg, x2m_arg, y2m_arg);
        
        // determine N line
        aLineN = TwoDimensional.getLineAValue(x1n_arg, y1n_arg, x2n_arg, y2n_arg);
        bLineN = TwoDimensional.getLineBValue(x1n_arg, y1n_arg, x2n_arg, y2n_arg);
        
        // getting crossing points
        crossingX = TwoDimensional.xPosCrossingLines(aLineM, bLineM, aLineN, bLineN);
        crossingY = TwoDimensional.yPosCrossingLines(aLineM, bLineM, aLineN, bLineN);
        
        // verify if not paralel
        if ((crossingX == Double.NaN) || (crossingY == Double.NaN)){
            return (false);
        }
        
        System.out.print("");
        if((x1m_arg == 952) && (y1m_arg == 1358)) {
            System.out.print("");
        } else if((x1m_arg == 959) && (y1m_arg == 1366)) {
            System.out.print("");
        }
        
        // verify if crossing point is between two  semi lines
        if (TwoDimensional.isBetween(crossingX, crossingY, 
                                     x1m_arg, y1m_arg, 
                                     x2m_arg, y2m_arg) &&
            TwoDimensional.isBetween(crossingX, crossingY, 
                                     x1n_arg, y1n_arg, 
                                     x2n_arg, y2n_arg)){
            return (true);
        } else {
            /*
            if((x1m_arg == 944) && (y1m_arg == 1360)) {
                System.out.println("Ponto fora:");
                System.out.println("["+x1n_arg+"; "+y1n_arg+"]");
                System.out.println("["+crossingX+"; "+crossingY+"]");
                System.out.println("["+x2n_arg+"; "+y2n_arg+"]");
                System.out.println(" ");
            }
            */
            /*
            if ( ( (x1m_arg >= 942) && (x1m_arg <= 946)) &&
                   (y1m_arg >= 1358) && (y1m_arg <= 1362) &&
                   (y1n_arg < y1m_arg) && (y2n_arg < y1m_arg) ) {
                   */
            if ( ( (x1m_arg >= 933) && (x1m_arg <= 953)) &&
                   (y1m_arg >= 1360) && (y1m_arg <= 1365) &&
                   (y1n_arg == 1360) && (y2n_arg == 1360) ) {
                System.out.println("Ponto fora:");
                System.out.println("-["+x1m_arg+"; "+y1m_arg+"]");
                System.out.println("-[...para...]");
                System.out.println("-["+x2m_arg+"; "+y2m_arg+"]");
                System.out.println("-[:]");
                System.out.println("-["+x1n_arg+"; "+y1n_arg+"]");
                System.out.println("-["+crossingX+"; "+crossingY+"]");
                System.out.println("-["+x2n_arg+"; "+y2n_arg+"]");
                System.out.println(" ");
            }
        }
        
        return (false);
    }
    
    /**
     * 
     * @param x1m_arg
     * @param y1m_arg
     * @param x2m_arg
     * @param y2m_arg
     * @param x1n_arg
     * @param y1n_arg
     * @param x2n_arg
     * @param y2n_arg
     * @return [x,y]
     */
    public static double[] semiLinesCrossingPoint(int x1m_arg, int y1m_arg, 
                                                 int x2m_arg, int y2m_arg,
                                                 int x1n_arg, int y1n_arg, 
                                                 int x2n_arg, int y2n_arg,
                                                 boolean includeExtremes_arg){
        double aLineM, bLineM;
        double aLineN, bLineN;
        double crossingX, crossingY;
        
        // determine M line
        aLineM = TwoDimensional.getLineAValue(x1m_arg, y1m_arg, x2m_arg, y2m_arg);
        bLineM = TwoDimensional.getLineBValue(x1m_arg, y1m_arg, x2m_arg, y2m_arg);
        
        // determine N line
        aLineN = TwoDimensional.getLineAValue(x1n_arg, y1n_arg, x2n_arg, y2n_arg);
        bLineN = TwoDimensional.getLineBValue(x1n_arg, y1n_arg, x2n_arg, y2n_arg);
        
        // getting crossing points
        crossingX = TwoDimensional.xPosCrossingLines(aLineM, bLineM, aLineN, bLineN);
        crossingY = TwoDimensional.yPosCrossingLines(aLineM, bLineM, aLineN, bLineN);
        
        // verify if not paralel
        if ((crossingX == Double.NaN) || (crossingY == Double.NaN)){
            return (null);
        }
        
        // verify if crossing point is between two  semi lines
        if (includeExtremes_arg){
            if (TwoDimensional.isBetweenIncluding(crossingX, crossingY, 
                                                  x1m_arg, y1m_arg, 
                                                  x2m_arg, y2m_arg) &&
                TwoDimensional.isBetweenIncluding(crossingX, crossingY, 
                                                  x1n_arg, y1n_arg, 
                                                  x2n_arg, y2n_arg)){
                return (new double[]{crossingX,crossingY});
            } else {
                return (null);
            }
        } else {
            if (TwoDimensional.isBetween(crossingX, crossingY, 
                                         x1m_arg, y1m_arg, 
                                         x2m_arg, y2m_arg) &&
                TwoDimensional.isBetween(crossingX, crossingY, 
                                         x1n_arg, y1n_arg, 
                                         x2n_arg, y2n_arg)){
                return (new double[]{crossingX,crossingY});
            } else {
                return (null);
            }
        }
    }
    
    /**
     * Verify if a point [x, y] is inside a polygon
     * @param xPoint_arg X coordinate value of evaluated point
     * @param yPoint_arg Y coordinate value of evaluated point 
     * @param polygon_arg A vector of points witch represents polygon's vertices ([n][2]:[x1,y1];[x2,y2];...)
     * @return TRUE if inside, FALSE otherwise
     */
    public static boolean isPointInsidePolygon(int xPoint_arg, int yPoint_arg,
                                               int[][] polygon_arg){
        int curX, curY, nxtX, nxtY;
        int countVerts, nextVertz;
        int countBorderCross;
        int maxDist;
        
        // TODO - remove
        boolean stops;
        if ((xPoint_arg == 890) && (yPoint_arg == 1472)){
            stops = true;
        } else {stops = false;};
        
        // basic check - must be a true polygon
        if ((polygon_arg == null) || (polygon_arg.length < 3)) return (false);
        
        // determine maximum distance
        maxDist = (int)TwoDimensional.biggestInternalDistanceInPolygon(polygon_arg);
        
        // count number of times line from the given point to the extreme number of hell existence cross an edge
        countBorderCross = 0;
        for(countVerts = 0; countVerts < polygon_arg.length; countVerts++){
            nextVertz = ((countVerts + 1) % polygon_arg.length);
            
            // getting vertices coordinates
            curX = polygon_arg[countVerts][0];
            curY = polygon_arg[countVerts][1];
            nxtX = polygon_arg[nextVertz][0];
            nxtY = polygon_arg[nextVertz][1];
            
            // check if given point cross current edge
            /*
            if (TwoDimensional.isSemiLinesCrossing(xPoint_arg, yPoint_arg, 
                                                   xPoint_arg + maxDist, yPoint_arg + 1, 
                                                   curX, curY, 
                                                   nxtX, nxtY)) {
                System.out.println("PONTO DENTRO ++!");
                countBorderCross++;
            }
            if (TwoDimensional.isSemiLinesCrossing(xPoint_arg, yPoint_arg, 
                                                          xPoint_arg - maxDist, yPoint_arg - 1, 
                                                          curX, curY, 
                                                          nxtX, nxtY)) {
                System.out.println("PONTO DENTRO --!");
                countBorderCross++;
            }
            if (TwoDimensional.isSemiLinesCrossing(xPoint_arg, yPoint_arg, 
                                                          xPoint_arg + 1, yPoint_arg + maxDist, 
                                                          curX, curY, 
                                                          nxtX, nxtY)) {
                System.out.println("PONTO DENTRO -+!");
                countBorderCross++;
            }*/
            if (TwoDimensional.isSemiLinesCrossing(xPoint_arg, yPoint_arg, 
                                                          xPoint_arg - 1, yPoint_arg - 3000, 
                                                          curX, curY, 
                                                          nxtX, nxtY)) {
                System.out.println("PONTO DENTRO +-!");
                countBorderCross++;
            }
            
            // TODO - remove
            if (stops && ((curX == 886) && (curY == 1480))){
                System.out.print("");
            } else if (stops && ((curX == 1480) && (curY == 886))) {
                System.out.print("");
            }
        }
        
        // if number of crossing border is *par*, point is outside. Otherwise, is inside
        if ((countBorderCross % 2) == 1){
            return (true);
        } else {
            return (false);
        }
    }
    
    /**
     * 
     * @param xPoint_arg X value of point
     * @param yPoint_arg Y value of point
     * @param aCoef_arg A coef. of line
     * @param bCoef_arg B coef. of line
     * @return TRUE if point is contained in line, FALSE otherwise
     */
    public static boolean isPointInLine(int xPoint_arg, int yPoint_arg,
                                        double aCoef_arg, double bCoef_arg){
        int yResult, xResult;
        
        yResult = (int)(aCoef_arg + (bCoef_arg * xPoint_arg));
        xResult = (int)((yPoint_arg - aCoef_arg) / bCoef_arg);
        
        // TODO - remove check
        if (((xPoint_arg == 905) || (xPoint_arg == 906)) && 
                ((yPoint_arg == 1401) || (yPoint_arg == 1402))){
            System.out.print("");
        }
        
        if((yResult == yPoint_arg) || (xResult == xPoint_arg)){
            return (true);
        } else {
            return (false);
        }
    }
    
    public static boolean isPointInsidePolygonJTS(int xPoint_arg, int yPoint_arg,
                                                  int[][] polygon_arg){
        int countPolyPoints;
        boolean returnVal;
        
        final GeometryFactory gf = new GeometryFactory();

        final ArrayList<Coordinate> points = new ArrayList<Coordinate>();
        for(countPolyPoints = 0; countPolyPoints < polygon_arg.length; countPolyPoints++){
            points.add(new Coordinate(polygon_arg[countPolyPoints][0],
                                      polygon_arg[countPolyPoints][1]));
        }
        final Polygon polygon = gf.createPolygon(new LinearRing(new CoordinateArraySequence(points.toArray(new Coordinate[points.size()])), gf), null);

        final Coordinate coord = new Coordinate(xPoint_arg, yPoint_arg);
        final Point point = gf.createPoint(coord);
        
        returnVal = point.within(polygon);
        
        if ((xPoint_arg >= 910) && (xPoint_arg <= 955) && 
                (yPoint_arg >= 1456) && (yPoint_arg <= 1511)) {
            System.out.print("");
        }

        return(point.within(polygon));
    }
    
    /**
     * 
     * @param polygon_arg A vector of points witch represents polygon's vertices ([n][2]:{[x1,y1];[x2,y2];...})
     * @return 
     */
    public static float biggestInternalDistanceInPolygon(int[][] polygon_arg){
        int count1, count2;
        float currentDistance;
        float biggestDistance;
        
        // basic check
        if (polygon_arg == null) return(Float.NaN);
        
        biggestDistance = (-1);
        for(count1 = 0; count1 < polygon_arg.length; count1++){
            for(count2 = 0; count2 < polygon_arg.length; count2++){
                currentDistance = TwoDimensional.distanceBetweenPoints(polygon_arg[count1][0], 
                                                                       polygon_arg[count1][1], 
                                                                       polygon_arg[count2][0], 
                                                                       polygon_arg[count2][1]);
                if(currentDistance > biggestDistance){
                    biggestDistance = currentDistance;
                }
            }
        }
        
        return(biggestDistance);
    }
    
    public static float distanceBetweenPoints(int x1_arg, int y1_arg,
                                              int x2_arg, int y2_arg){
        float returnVal;
        
        int deltaX, deltaY;
        
        deltaX = x1_arg - x2_arg;
        deltaY = y1_arg - y2_arg;
        
        returnVal = (float)Math.sqrt(Math.pow(deltaX, 2) +  Math.pow(deltaY,2));
        
        return (returnVal);
    }
    
    /**
     * 
     * @param x1_arg
     * @param y1_arg
     * @param x2_arg
     * @param y2_arg
     * @return 
     */
    public static double distanceBetweenPoints(double x1_arg, double y1_arg,
                                               double x2_arg, double y2_arg){
        double returnVal;
        
        double deltaX, deltaY;
        
        deltaX = x1_arg - x2_arg;
        deltaY = y1_arg - y2_arg;
        
        returnVal = Math.sqrt(Math.pow(deltaX, 2) +  Math.pow(deltaY,2));
        
        return (returnVal);
    }
    
    /**
     * 
     * @param x1_arg
     * @param y1_arg
     * @param x2_arg
     * @param y2_arg
     * @param polygon_arg
     * @return [x,y]
     */
    public static double[] semilineCrossingPolygon(int x1_arg, int y1_arg,
                                                   int x2_arg, int y2_arg,
                                                   int[][] polygon_arg,
                                                   boolean includeBorder_arg){
        int curX, curY, nxtX, nxtY;
        int countVerts, nextVertz;
        double[] returnTuple;
        
        for(countVerts = 0; countVerts < polygon_arg.length; countVerts++){
            nextVertz = ((countVerts + 1) % polygon_arg.length);
            
            // getting vertices coordinates
            curX = polygon_arg[countVerts][0];
            curY = polygon_arg[countVerts][1];
            nxtX = polygon_arg[nextVertz][0];
            nxtY = polygon_arg[nextVertz][1];
            
            // check if given point cross current edge
            returnTuple = TwoDimensional.semiLinesCrossingPoint(x1_arg, y1_arg, 
                                                                x2_arg, y2_arg, 
                                                                curX, curY, 
                                                                nxtX, nxtY,
                                                                includeBorder_arg);
            if (returnTuple != null) return (returnTuple);
        }
        
        return (null);
    }
    
    public static boolean isSemilineCrossingPolygon(int x1_arg, int y1_arg,
                                                    int x2_arg, int y2_arg,
                                                    int[][] polygon_arg){
        int curX, curY, nxtX, nxtY;
        int countVerts, nextVertz;
        int countBorderCross;
        
        // TODO - remove
        boolean stops;
        if ((x1_arg == 890) && (y1_arg == 1472)){
            stops = true;
        } else {stops = false;};
        
        // basic check - must be a true polygon
        if ((polygon_arg == null) || (polygon_arg.length < 3)) return (false);
        
        // count number of times line from the given point to the extreme number of hell existence cross an edge
        countBorderCross = 0;
        for(countVerts = 0; countVerts < polygon_arg.length; countVerts++){
            nextVertz = ((countVerts + 1) % polygon_arg.length);
            
            // getting vertices coordinates
            curX = polygon_arg[countVerts][0];
            curY = polygon_arg[countVerts][1];
            nxtX = polygon_arg[nextVertz][0];
            nxtY = polygon_arg[nextVertz][1];
            
            // check if given point cross current edge
            if (TwoDimensional.isSemiLinesCrossing(x1_arg, y1_arg, 
                                                   x2_arg, y2_arg,
                                                   curX, curY, 
                                                   nxtX, nxtY)) {
                countBorderCross++;
            }
            
            // TODO - remove
            if (stops && ((curX == 886) && (curY == 1480))){
                System.out.print("");
            } else if (stops && ((curX == 1480) && (curY == 886))) {
                System.out.print("");
            }
        }
        
        if (countBorderCross > 0){
            System.out.println("CRUZOU "+countBorderCross+" BORDAS!");
        }
        
        // if number of crossing border is *par*, point is outside. Otherwise, is inside
        if ((countBorderCross%2) == 1){
            return (true);
        } else {
            return (false);
        }
    }
    
    /**
     * Get X coordinate of crossing point
     * @param a1 Parameter 'a' of line 1 formula
     * @param b1 Parameter 'b' of line 1 formula
     * @param a2 Parameter 'a' of line 1 formula
     * @param b2 Parameter 'b' of line 2 formula
     * @return A numeric value if crossing point exist, Double.NaN otherwise
     */
    public static double xPosCrossingLines(double a1, double b1, 
                                           double a2, double b2){
        // basic check
        if (b2 == b1) return (Double.NaN);
        
        return ((a1 - a2)/(b2 - b1));
    }
    
    /**
     * Get Y coordinate of crossing point
     * @param a1 Parameter 'a' of line 1 formula
     * @param b1 Parameter 'b' of line 1 formula
     * @param a2 Parameter 'a' of line 2 formula
     * @param b2 Parameter 'b' of line 2 formula
     * @return A numeric value if crossing point exist, Double.NaN otherwise
     */
    public static double yPosCrossingLines(double a1, double b1, 
                                           double a2, double b2){
        // basic check
        if (b2 == b1) return (Double.NaN);
        
        return (((double)(a1*b2)-(a2*b1))/(double)(b2-b1));
    }
    
    /**
     * Being a line y = (a + (b*x)) equation
     * @param x1_arg
     * @param y1_arg
     * @param x2_arg
     * @param y2_arg
     * @return 
     */
    public static double getLineAValue(int x1_arg, int y1_arg, 
                                       int x2_arg, int y2_arg){
        if ((x2_arg-x1_arg) == 0){
            return (0);
        } else {
            return (((double)((x2_arg*y1_arg)-(x1_arg*y2_arg))) /
                    ((double)(x2_arg-x1_arg)));
        }
    }
    
    /**
     * Being a line y = (a + (b*x)) equation
     * @param x1_arg
     * @param y1_arg
     * @param x2_arg
     * @param y2_arg
     * @return 
     */
    public static double getLineBValue(int x1_arg, int y1_arg, 
                                       int x2_arg, int y2_arg){
        if ((x1_arg - x2_arg) == 0){
            return (0);
        } else {
            return (((double)(y1_arg - y2_arg)) / 
                    ((double)(x1_arg - x2_arg)));
        }
    }
    
    /**
     * Verify if point 1 is between points 2 and 3
     * @param x1_arg
     * @param y1_arg
     * @param x2_arg
     * @param y2_arg
     * @param x3_arg
     * @param y3_arg
     * @return 
     */
    private static boolean isBetween(double x1_arg, double y1_arg, 
                                     double x2_arg, double y2_arg,
                                     double x3_arg, double y3_arg){
        
        if ( ( ( (x1_arg < x2_arg) && (x1_arg > x3_arg) ) || 
               ( (x1_arg > x2_arg) && (x1_arg < x3_arg) ) )
              && 
             ( ( (y1_arg < y2_arg) && (y1_arg > y3_arg) ) || 
               ( (y1_arg > y2_arg) && (y1_arg < y3_arg) ) ) ){
            return (true);
        }
        
        return (false);
    }
    
    /**
     * 
     * @param x1_arg
     * @param y1_arg
     * @param x2_arg
     * @param y2_arg
     * @param x3_arg
     * @param y3_arg
     * @return 
     */
    private static boolean isBetweenIncluding(double x1_arg, double y1_arg, 
                                              double x2_arg, double y2_arg,
                                              double x3_arg, double y3_arg){
        
        if ( ( ( (x1_arg <= x2_arg) && (x1_arg >= x3_arg) ) || 
               ( (x1_arg >= x2_arg) && (x1_arg <= x3_arg) ) )
              && 
             ( ( (y1_arg <= y2_arg) && (y1_arg >= y3_arg) ) || 
               ( (y1_arg >= y2_arg) && (y1_arg <= y3_arg) ) ) ){
            return (true);
        }
        
        return (false);
    }
    
    public static void main(String[] args){
        //TwoDimentional.submain_01();
        TwoDimensional.submain_02();
    }
    
    public static void submain_01(){
        int x1m, y1m, x2m, y2m;
        int x1n, y1n, x2n, y2n;
        boolean returned;
        
        // case 1: TRUE
        x1m = 2; y1m = 8;
        x2m = 4; y2m = 3;
        x1n = 2; y1n = 5;
        x2n = 4; y2n = 6;
        
        returned = TwoDimensional.isSemiLinesCrossing(x1m, y1m, x2m, y2m, 
                                                      x1n, y1n, x2n, y2n);
        System.out.println("Case 1 " + returned);
        
        // case 2: FALSE
        x1m = 6; y1m = 7;
        x2m = 8; y2m = 8;
        x1n = 2; y1n = 5;
        x2n = 4; y2n = 6;
        
        returned = TwoDimensional.isSemiLinesCrossing(x1m, y1m, x2m, y2m, 
                                                      x1n, y1n, x2n, y2n);
        System.out.println("Case 2 " + returned);
        
        // case 3: FALSE
        x1m = 5; y1m = 8;
        x2m = 7; y2m = 9;
        x1n = 2; y1n = 5;
        x2n = 4; y2n = 6;
        
        returned = TwoDimensional.isSemiLinesCrossing(x1m, y1m, x2m, y2m, 
                                                      x1n, y1n, x2n, y2n);
        System.out.println("Case 3 " + returned);
        
        // case 4: FALSE
        x1m = 5; y1m = 8;
        x2m = 7; y2m = 9;
        x1n = 2; y1n = 8;
        x2n = 4; y2n = 3;
        
        returned = TwoDimensional.isSemiLinesCrossing(x1m, y1m, x2m, y2m, 
                                                      x1n, y1n, x2n, y2n);
        System.out.println("Case 4 " + returned);
    }
    
    public static void submain_02(){
        int[][] polygon;
        boolean isInside;
        
        polygon = new int[4][2];
        
        polygon[0][0] = 1;
        polygon[0][1] = 3;
        polygon[1][0] = 2;
        polygon[1][1] = 6;
        polygon[2][0] = 6;
        polygon[2][1] = 5;
        polygon[3][0] = 5;
        polygon[3][1] = 1;
        
        isInside = TwoDimensional.isPointInsidePolygon(4, 4, polygon);
        System.out.println("Case 1: " + isInside);
        
        isInside = TwoDimensional.isPointInsidePolygon(4, 7, polygon);
        System.out.println("Case 2: " + isInside);
        
        isInside = TwoDimensional.isPointInsidePolygon(1, 1, polygon);
        System.out.println("Case 3: " + isInside);
        
        isInside = TwoDimensional.isPointInsidePolygon(3, 1, polygon);
        System.out.println("Case 4: " + isInside);
    }
}
