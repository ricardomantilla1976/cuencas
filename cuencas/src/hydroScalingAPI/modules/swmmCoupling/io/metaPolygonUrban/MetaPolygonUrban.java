/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.modules.swmmCoupling.io.metaPolygonUrban;

import hydroScalingAPI.io.*;
import adlzanchetta.cuencasTools.cuencasCsvFileInterpreter;
import hydroScalingAPI.modules.swmmCoupling.util.TwoDimensional;
import hydroScalingAPI.modules.swmmCoupling.objects.MetaRasterTool;
import hydroScalingAPI.util.geomorphology.objects.Basin;
import hydroScalingAPI.util.geomorphology.objects.LinksAnalysis;
import hydroScalingAPI.modules.swmmCoupling.util.geomorphology.objects.SubBasin;
import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author A. D. L. Zanchetta
 */
public class MetaPolygonUrban extends MetaPolygon  {
    
    // labels
    public static final int NOID_LABEL = 0;
    protected static final int UNKNOW_LABEL = 0;
    protected static final int INLET_LABEL = 1;
    protected static final int OUTLET_LABEL = 2;
    protected static boolean includeAnylets = false;
    
    //
    private ArrayList<Anylet> anylets;
    private int polygonID;
    private float[][] rainData;
    
    public MetaPolygonUrban(){
        this.anylets = new ArrayList<Anylet>();
        this.polygonID = MetaPolygonUrban.NOID_LABEL;
        this.rainData = null;
    }
    
    public MetaPolygonUrban(String metaPolygonFilePath_arg) throws IOException {
        super(metaPolygonFilePath_arg);
        this.anylets = new ArrayList<Anylet>();
        this.polygonID = MetaPolygonUrban.NOID_LABEL;
        this.rainData = null;
    }
    
    public MetaPolygonUrban(File metaPolygonFile_arg) throws IOException {
        super(metaPolygonFile_arg);
        this.anylets = new ArrayList<Anylet>();
        this.polygonID = MetaPolygonUrban.NOID_LABEL;
        this.rainData = null;
    }
    
    public void addNewInlet(double lat_arg, double lng_arg){
        this.addNewAnylet(lat_arg, lng_arg, MetaPolygonUrban.INLET_LABEL);
    }
    
    public void addNewInlet(double lat_arg, double lng_arg, int alt_arg){
        this.addNewAnylet(lat_arg, lng_arg, alt_arg, MetaPolygonUrban.INLET_LABEL);
    }
    
    public void addNewOutlet(double lat_arg, double lng_arg){
        this.addNewAnylet(lat_arg, lng_arg, MetaPolygonUrban.OUTLET_LABEL);
    }
    
    public void addNewOutlet(double lat_arg, double lng_arg, int alt_arg){
        this.addNewAnylet(lat_arg, lng_arg, alt_arg, MetaPolygonUrban.OUTLET_LABEL);
    }
    
    private void addNewAnylet(double lat_arg, double lng_arg, int type_arg){
        Anylet newAnylet;
        
        newAnylet = new Anylet(lat_arg, lng_arg, type_arg);
        
        this.anylets.add(newAnylet);
    }
    
    private void addNewAnylet(double lat_arg, 
                              double lng_arg, 
                              int altitude_arg, 
                              int type_arg){
        Anylet newAnylet;
        
        newAnylet = new Anylet(lat_arg, lng_arg, altitude_arg, type_arg);
        
        this.anylets.add(newAnylet);
    }
    
    /**
     * Define flow data for a anylet located in a specific position
     * @param x_arg Position in X coordinate
     * @param y_arg Position in Y coordinate
     * @param newFlowSerie_arg Flow serie to be set
     * @param mraster_arg 
     * @return TRUE if an existing anylet was found in given position and data was set, FALSE otherwise
     */
    public boolean setFlowResgister(int x_arg, int y_arg,
                                    double[][] newFlowSerie_arg,
                                    MetaRaster mraster_arg){
        Iterator<Anylet> anyletsIt;
        int curX, curY;
        Anylet curAnylet;
        
        anyletsIt = this.anylets.iterator();
        while(anyletsIt.hasNext()){
            curAnylet = anyletsIt.next();
            curY = MetaRasterTool.getYfromLatitude(mraster_arg, curAnylet.latitude);
            curX = MetaRasterTool.getXfromLongitude(mraster_arg, curAnylet.longitude);
            if ((y_arg == curY) && (x_arg == curX)){
                curAnylet.flowSerie = newFlowSerie_arg;
                return (true);
            }
        }
        
        return (false);
    }

    /**
     * Define rain data, in mm
     * @param rainData Rain data, with [N][2] size, where N is the number of registers, [n][0] the timestamp (in ms) and [n][1] the rain registered (in mm)
     */
    public void setRainData(float[][] rainData) {
        this.rainData = rainData;
    }
    
    /**
     * 
     * @param mRaster_arg
     * @param directionMatrix_arg
     * @param mNetwork_arg
     * @return Vector with all tributary SubBasin if it was possible to define then. NULL otherwise
     */
    public SubBasin[] getAllTributarySubBasins(MetaRaster mRaster_arg,
                                               byte[][] directionMatrix_arg,
                                               MetaNetwork mNetwork_arg){
        Iterator<Anylet> anyletIt;
        int countInletsInPolygon;
        SubBasin currentSubBasin;
        SubBasin[] returnVector;
        Anylet currentAnylet;
        int curX, curY;
        
        // Algorithm
        // 0: basic check input
        // 1: identify number of new subbasins (number of inlets)
        // 2: alloc return vector object
        // 3: for each inlet
        //  3.1: get its X Y value
        //  3.2: create is subbasin object
        //  3.3: remove this own polygon from it
        //  3.4: add to list
        
        // 0
        if (mRaster_arg == null) return (null);
        
        // 1
        anyletIt = this.anylets.iterator();
        countInletsInPolygon = 0;
        while(anyletIt.hasNext()){
            currentAnylet = anyletIt.next();
            
            if (currentAnylet.getType() == MetaPolygonUrban.INLET_LABEL) {
                countInletsInPolygon++;
            }
        }
        
        // 2 
        returnVector = new SubBasin[countInletsInPolygon];
        
        // 3
        anyletIt = this.anylets.iterator();
        countInletsInPolygon = 0;
        while(anyletIt.hasNext()){
            currentAnylet = anyletIt.next();
            if (currentAnylet.getType() != MetaPolygonUrban.INLET_LABEL) continue;
            
            // 3.1
            curX = MetaRasterTool.getXfromLongitude(mRaster_arg, 
                                                    currentAnylet.getLongitude());
            curY = MetaRasterTool.getYfromLatitude(mRaster_arg, 
                                                   currentAnylet.getLatitude());
            
            // 3.2
            currentSubBasin = new SubBasin(curX, curY,
                                           directionMatrix_arg,
                                           mRaster_arg);
            
            // 3.3
            currentSubBasin.extract(this, directionMatrix_arg, mRaster_arg);
            
            // 3.4
            returnVector[countInletsInPolygon] = currentSubBasin;
            
            countInletsInPolygon++;
        }
        
        return (returnVector);
    }
    
    /**
     * Identifies inputs and outputs from basin considering all links structure
     * @param metaRaster_arg
     * @return TRUE if it was possible to determine inputs and outputs, FALSE otherwise
     */
    public boolean identifyBorderPointsByMask(MetaRaster metaRaster_arg,
                                              byte[][] dirMatrix_arg){
        byte[][] linksMask, polygMask;
        double anyLon, anyLat;
        int curCol, curLin;
        Anylet addedAny;
        int anyletType;
        
        // basic check
        if (metaRaster_arg == null) return (false);
        
        // ALG
        // 1 : get entire river network mask from raster and polygon mask
        // 2 : pass trought entire matrix checking where there is a crossing-point
        // 2.1 : for each crossing point, identify its type
        // 2.2 : add to list
        
        // 1 : 
        linksMask = MetaRasterTool.getLinksMask(metaRaster_arg); // in [lt,lg] or [y,x]
        polygMask = this.getPolygonMask(metaRaster_arg);         // in [lt,lg] or [y,x]
        if ((linksMask == null) || (polygMask == null)) return (false);
        
        // 2 :
        for(curLin = 0; curLin < linksMask.length; curLin++){
            for(curCol = 0; curCol < linksMask[curLin].length; curCol++){
                if ((linksMask[curLin][curCol] == 1) && 
                        (polygMask[curLin][curCol] == 1)){
                    // 2.1
                    anyletType = this.defineAnyletType(linksMask, polygMask,
                                                       dirMatrix_arg, 
                                                       metaRaster_arg,
                                                       curCol, curLin);
                    
                    anyLon = MetaRasterTool.getLonFromX(metaRaster_arg, curCol);
                    anyLat = MetaRasterTool.getLatFromY(metaRaster_arg, curLin);
                    // 2.2
                    addedAny = new Anylet(anyLat, anyLon, anyletType);
                }
            }
        }
        
        return (true);
    }
    
    /**
     * 
     * @param linksMask_arg Matrix in format [lt,lg] or [y,x] or [row, col]
     * @param polygMask_arg Matrix in format [lt,lg] or [y,x] or [row, col]
     * @param dirMatrix_arg Direction matrix
     * @param mRaster_arg Main MetaRaster
     * @param xPoint_arg X value of point to be defined
     * @param yPoint_arg Y value of point to be defined
     * @return Can be MetaPolygonUrban.INLET_LABEL, MetaPolygonUrban.OUTLET_LABEL or MetaPolygonUrban.UNKNOW_LABEL
     */
    private int defineAnyletType(byte[][] linksMask_arg, 
                                 byte[][] polygMask_arg,
                                 byte[][] dirMatrix_arg,
                                 MetaRaster mRaster_arg,
                                 int xPoint_arg, int yPoint_arg){
        int xPoint1, yPoint1, xPoint2, yPoint2;
        boolean point1Found, point2Found;
        int[] neightPoint1, neightPoint2;
        int[] nextPoint, prevPoint;
        int[] nextFlowPoint;
        int runCol, runRow;
        
        // basic check
        if ((linksMask_arg == null) || (polygMask_arg == null)) return (MetaPolygonUrban.UNKNOW_LABEL);
        
        // ALG
        // 1 : define point1 and point2
        // 2 : get next division point throught point1 direction (nP1)
        // 3 : get next division point throught point2 direction (nP2)
        // 4 : evaluate nP1 and nP2
        
        // 1
        point1Found = false;
        point2Found = false;
        xPoint1 = (-1); yPoint1 = (-1);
        xPoint2 = (-1); yPoint2 = (-1);
        for(runCol = (-1); runCol < 2; runCol++){
            for(runRow = (-1); runRow < 2; runRow++){
                if ((runCol == 0) && (runRow == 0)){
                    continue;
                } else {
                    if (linksMask_arg[yPoint_arg + runRow][xPoint_arg + runCol] == 1){
                        if(!point1Found){
                            point1Found = true;
                            xPoint1 = xPoint_arg + runCol;
                            yPoint1 = yPoint_arg + runRow;
                        } else if (!point2Found) {
                            point2Found = true;
                            xPoint2 = xPoint_arg + runCol;
                            yPoint2 = yPoint_arg + runRow;
                        } else {
                            System.out.println("TERCEIRO CAMINHO MANO!?");
                        }
                    }
                }
            }
        }
        
        // basic check of found points
        if ( (xPoint1 < 0) || (xPoint2 < 0) || (yPoint1 < 0)  || (yPoint2 < 0) ){
            return (MetaPolygonUrban.UNKNOW_LABEL);
        }
        
        // 2
        neightPoint1 = this.getNextDivisionPoint(linksMask_arg, polygMask_arg, 
                                               xPoint_arg, yPoint_arg, 
                                               xPoint1, yPoint1);
        
        // 3
        neightPoint2 = this.getNextDivisionPoint(linksMask_arg, polygMask_arg, 
                                               xPoint_arg, yPoint_arg, 
                                               xPoint2, yPoint2);
        
        nextFlowPoint = this.getNextFlowPoint(xPoint_arg, yPoint_arg, 
                                              dirMatrix_arg);
        if (nextFlowPoint == null){
            System.out.println("AFF MARIA: null point for next flow.");
            return (MetaPolygonUrban.UNKNOW_LABEL);
        } else if ((nextFlowPoint[0] == xPoint1) && (nextFlowPoint[1] == yPoint1)) {
            nextPoint = neightPoint1;
            prevPoint = neightPoint2;
        } else if ((nextFlowPoint[0] == xPoint2) && (nextFlowPoint[1] == yPoint2)) {
            nextPoint = neightPoint2;
            prevPoint = neightPoint1;
        } else {
            System.out.println("AFF MARIA: sem next flow point encontrado.");
            return (MetaPolygonUrban.UNKNOW_LABEL);
        }
        
        // 4
        if ( this.isPointInsidePolygon(nextPoint[0], nextPoint[1], mRaster_arg) 
                && !this.isPointInsidePolygon(prevPoint[0], prevPoint[1], mRaster_arg) ){
            return (MetaPolygonUrban.INLET_LABEL);
        } else if (!this.isPointInsidePolygon(nextPoint[0], nextPoint[1], mRaster_arg)
                && this.isPointInsidePolygon(prevPoint[0], prevPoint[1], mRaster_arg)) {
            return (MetaPolygonUrban.OUTLET_LABEL);
        } else {
            return (MetaPolygonUrban.UNKNOW_LABEL);
        }
    }
    
    /**
     * 
     * @param xPoint_arg
     * @param yPoint_arg
     * @param dirMatrix_arg
     * @param riversMask_arg
     * @return A [X,Y] point
     */
    private int[] getPrevFlowPoint(int xPoint_arg, int yPoint_arg, 
                                   byte[][] dirMatrix_arg,
                                   byte[][] riversMask_arg){
        int curX, curY;
        
        // eval UpLeft neightbour
        curX = xPoint_arg - 1;
        curY = yPoint_arg + 1;
        if (dirMatrix_arg[curY][curX] == 3) {
            if (riversMask_arg[curY][curX] == 1) return(new int[]{curX, curY});
        }
        
        // eval Up neightbour
        curX = xPoint_arg;
        curY = yPoint_arg + 1;
        if (dirMatrix_arg[curY][curX] == 2) {
            if (riversMask_arg[curY][curX] == 1) return(new int[]{curX, curY});
        }
        
        // eval UpRight neightbour
        curX = xPoint_arg + 1;
        curY = yPoint_arg + 1;
        if (dirMatrix_arg[curY][curX] == 1) {
            if (riversMask_arg[curY][curX] == 1) return(new int[]{curX, curY});
        }
        
        // eval Left neightbour
        curX = xPoint_arg - 1;
        curY = yPoint_arg;
        if (dirMatrix_arg[curY][curX] == 6) {
            if (riversMask_arg[curY][curX] == 1) return(new int[]{curX, curY});
        }
        
        // eval Right neightbour
        curX = xPoint_arg + 1;
        curY = yPoint_arg;
        if (dirMatrix_arg[curY][curX] == 4) {
            if (riversMask_arg[curY][curX] == 1) return(new int[]{curX, curY});
        }
        
        // eval BotLeft neightbour
        curX = xPoint_arg - 1;
        curY = yPoint_arg - 1;
        if (dirMatrix_arg[curY][curX] == 9) {
            if (riversMask_arg[curY][curX] == 1) return(new int[]{curX, curY});
        }
        
        // eval Bot neightbour
        curX = xPoint_arg;
        curY = yPoint_arg - 1;
        if (dirMatrix_arg[curY][curX] == 8) {
            if (riversMask_arg[curY][curX] == 1) return(new int[]{curX, curY});
        }
        
        // eval BotRight neightbour
        curX = xPoint_arg + 1;
        curY = yPoint_arg - 1;
        if (dirMatrix_arg[curY][curX] == 7) {
            if (riversMask_arg[curY][curX] == 1) return(new int[]{curX, curY});
        }
        
        // if not found anything, return null
        return (null);
    }
    
    /**
     * 
     * @param xPoint_arg
     * @param yPoint_arg
     * @param dirMatrix_arg
     * @return A [X,Y] point if it is possible to determine that. NULL otherwise
     */
    private int[] getNextFlowPoint(int xPoint_arg, int yPoint_arg, 
                                   byte[][] dirMatrix_arg){
        int[] returnPoint;
        byte curFlowPoint;
        int nextX, nextY;
        
        // TODO
        if (dirMatrix_arg == null) return (null);
        
        curFlowPoint = dirMatrix_arg[yPoint_arg][xPoint_arg];
        
        nextX = (-1);
        nextY = (-1);
        switch (curFlowPoint){
            case 1:                       // SW
                nextX = xPoint_arg - 1;
                nextY = yPoint_arg - 1;
                break;
            case 2:                       // S_
                nextX = xPoint_arg;
                nextY = yPoint_arg - 1;
                break;
            case 3:                       // SE
                nextX = xPoint_arg + 1;
                nextY = yPoint_arg - 1;
                break;
            case 4:                       // _W
                nextX = xPoint_arg - 1;
                nextY = yPoint_arg;
                break;
            case 6:                       // _E
                nextX = xPoint_arg + 1;
                nextY = yPoint_arg;
                break;
            case 7:                       // NW
                nextX = xPoint_arg - 1;
                nextY = yPoint_arg + 1;
                break;
            case 8:                       // N_
                nextX = xPoint_arg;
                nextY = yPoint_arg + 1;
                break;
            case 9:                       // NE
                nextX = xPoint_arg + 1;
                nextY = yPoint_arg + 1;
                break;
        }
        
        if ((nextX == (-1)) && (nextY == (-1))){
            return (null);
        } else {
            returnPoint = new int[]{nextX, nextY};
            return (returnPoint);
        }
    }
    
    /**
     * A recursive function
     * @param linksMask_arg
     * @param polygMask_arg
     * @param xPoint1_arg X value of init point
     * @param yPoint1_arg Y value of init point
     * @param xPoint2_arg X value of next point
     * @param yPoint2_arg Y value of next point
     * @return A [x,y] point if its possible to identify, NULL otherwise
     */
    private int[] getNextDivisionPoint(byte[][] linksMask_arg, 
                                       byte[][] polygMask_arg,
                                       int xPoint1_arg, int yPoint1_arg,
                                       int xPoint2_arg, int yPoint2_arg ){
        int countCol, countRow;
        int nextX, nextY;
        int evalX, evalY;
        
        // basic check - null args
        if ((linksMask_arg == null) || (polygMask_arg == null)) return (null);
        
        // basic check - same points
        if ((xPoint1_arg == xPoint2_arg) && (yPoint1_arg == yPoint2_arg)){
            return (null);
        }
        
        // ALG
        // 1 : find next point in sequence
        // 2 : check if its the final condition
        //  2.1 : if its the final condition, return point
        //  2.2 : if its not the final condition, call recursively
        
        // 1
        nextX = (-1);
        nextY = (-1);
        for(countCol = (-1); countCol < 2; countCol++){
            for(countRow = (-1); countRow < 2; countRow++){
                
                evalX = xPoint2_arg + countCol;
                evalY = yPoint2_arg + countRow;
                
                // verify if not in center or back in origin
                if((countCol == 0) && (countRow == 0)) continue;
                if ((evalX == xPoint1_arg) && (evalY == yPoint1_arg)) continue;
                
                // verify if inside matrix
                if ((evalX < 0) || (evalY < 0)){
                    continue;
                }
                
                // 
                if(linksMask_arg[evalY][evalX] == 1){
                    nextX = evalX;
                    nextY = evalY;
                    break;
                }
            }
        }
        
        // 2
        if(nextX == (-1)){
            return(new int[]{xPoint2_arg,yPoint2_arg});
        } else if(polygMask_arg[nextY][nextX] != 1){
            return(new int[]{nextX,nextY});
        } else {
            return(this.getNextDivisionPoint(linksMask_arg, polygMask_arg, 
                                             nextX, nextY, 
                                             xPoint1_arg, yPoint1_arg));
        }
    }
    
    /**
     * 
     * @param metaRaster_arg
     * @return 
     */
    public boolean identifyBorderPointsByLinks(MetaRaster metaRaster_arg){
        byte[][] directionMatrix;
        
        // TODO - CORREEEECT THAAAAT!
        directionMatrix = null;

        return (this.identifyBorderPointsByLinks(metaRaster_arg, directionMatrix));
    }
    
    /**
     * 
     * @param metaRaster_arg
     * @param directionMatrix_arg
     * @return 
     */
    public boolean identifyBorderPointsByLinks(MetaRaster metaRaster_arg, 
                                        byte[][] directionMatrix_arg){
        LinksAnalysis linksInfo_arg;
        
        try{
            linksInfo_arg = new LinksAnalysis(metaRaster_arg, 
                                              directionMatrix_arg);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }
        
        return (this.identifyBorderPointsByLinks(linksInfo_arg));
    }
    
    /**
     * Identify each river crossing the polygon border and update internal variable
     * @param linksInfo_arg
     * @return TRUE if it was possible to identify borders properly, FALSE otherwise
     */
    public boolean identifyBorderPointsByLinks(LinksAnalysis linksInfo_arg){
        boolean headInside, tailInside;
        int[][] verticesInXandY;
        int curXhead, curYhead;
        int curXtail, curYtail;
        Anylet currentAnylet;
        int countLinks;
        int[] headsId;
        int[] tailsId;
        
        // basic check
        if (linksInfo_arg == null) return (false);
        
        // get all links heads and tails, get polygon vertices, basic check it
        headsId = linksInfo_arg.headsArray;
        tailsId = linksInfo_arg.tailsArray;
        verticesInXandY = this.getVerticesInXandY(linksInfo_arg.localMetaRaster);
        if(verticesInXandY == null) return (false);
        
        for(countLinks = 0; countLinks < headsId.length; countLinks++){
            // begin variables
            headInside = false;
            tailInside = false;
            double[] crossPoint;
            
            // identify X and Y values of links head
            // CHECK
            // curXhead = linksInfo_arg.getXfromID(headsId[countLinks]);
            // curYhead = linksInfo_arg.getYfromID(headsId[countLinks]);
            curXhead = MetaRasterTool.getXfromID(linksInfo_arg.localMetaRaster, headsId[countLinks]);
            curYhead = MetaRasterTool.getYfromID(linksInfo_arg.localMetaRaster, headsId[countLinks]);
            
            // TODO - remove
            if (    ((curXhead >= 870) && (curXhead <= 880)) && 
                    ((curYhead >= 1485) && (curYhead <= 1492)) ){
                System.out.print("");
            }
            
            // verify if head is inside polygon
            headInside = this.isPointInsidePolygon(curXhead, curYhead, 
                                                   linksInfo_arg.localMetaRaster, 
                                                   verticesInXandY);
            
            // identify X and Y values of links tail
            //curXtail = linksInfo_arg.getXfromID(tailsId[countLinks]);
            //curYtail = linksInfo_arg.getYfromID(tailsId[countLinks]);
            curXtail = MetaRasterTool.getXfromID(linksInfo_arg.localMetaRaster, tailsId[countLinks]);
            curYtail = MetaRasterTool.getYfromID(linksInfo_arg.localMetaRaster, tailsId[countLinks]);
            
            // verify if tail is inside polygon
            tailInside = this.isPointInsidePolygon(curXtail, curYtail, 
                                                   linksInfo_arg.localMetaRaster, 
                                                   verticesInXandY);
            
            // testing new method
            crossPoint = TwoDimensional.semilineCrossingPolygon(curXhead, curYhead, 
                                                                curXtail, curYtail, 
                                                                verticesInXandY,
                                                                true);
            
            // if one is in and other is out, cross lines
            if (headInside && !tailInside){
                // add cross outgoing to list
                if (crossPoint != null){
                    currentAnylet = new Anylet(MetaRasterTool.getLatFromY(linksInfo_arg.localMetaRaster, (int)crossPoint[1]), 
                                               MetaRasterTool.getLonFromX(linksInfo_arg.localMetaRaster, (int)crossPoint[0]), 
                                               MetaPolygonUrban.OUTLET_LABEL);
                    this.anylets.add(currentAnylet);
                } else {
                    System.out.print("");
                }
            } else if (tailInside && !headInside) {
                // add cross incoming to list
                if (crossPoint != null){
                    currentAnylet = new Anylet(MetaRasterTool.getLatFromY(linksInfo_arg.localMetaRaster, (int)crossPoint[1]), 
                                               MetaRasterTool.getLonFromX(linksInfo_arg.localMetaRaster, (int)crossPoint[0]), 
                                               MetaPolygonUrban.INLET_LABEL);
                    this.anylets.add(currentAnylet);
                } else {
                    System.out.print("");
                }
            } else if (crossPoint != null) {
                // add undeterminated crossing line
                currentAnylet = new Anylet(MetaRasterTool.getLatFromY(linksInfo_arg.localMetaRaster, (int)crossPoint[1]), 
                                           MetaRasterTool.getLonFromX(linksInfo_arg.localMetaRaster, (int)crossPoint[0]), 
                                           MetaPolygonUrban.UNKNOW_LABEL);
                this.anylets.add(currentAnylet);
            }
        }
        
        return (true);
    }
    
    public void setId(int newId_arg){
        this.polygonID = newId_arg;
    }
    
    public int getId(){
        return (this.polygonID);
    }
    
    /**
     * 
     * @param containingSubBasin_arg SubBasin that can partially contains a PolygonUrban
     * @return TRUE if it was possible to identify points, FALSE otherwise
     */
    public boolean identifyBorderPointsByCrossing(MetaRaster mRaster_arg){
        byte[][] polygonMask, directionMatrix;
        int[] nextPoint, prevPoint;
        byte[][] fullNetworkMask;
        SubBasin currentSubBasin;
        double curLat, curLong;
        int currentX, currentY;
        Anylet currentAnylet;
        
        directionMatrix = MetaPolygonUrban.getDirectionMatrix(mRaster_arg);
        
        // get 
        fullNetworkMask = MetaRasterTool.getLinksMask(mRaster_arg);
        polygonMask = this.getPolygonMask(mRaster_arg);
        
        // TODO - ON  - remove all that
        /*
        File humanFile;
        humanFile = new File("C:\\Users\\Worker\\Desktop\\linksFile.txt");
        if (MetaRaster.printBinaryFileForHuman(humanFile, fullNetworkMask)){
            System.out.println("_-_PrintedHumanFile OKs");
        } else {
            System.out.println("_-_PrintedHumanFile BAD");
        }
        */
        // TODO - OFF - remove all that
        
        // clear previous points
        this.anylets = new ArrayList<Anylet>();
        
        for(currentY = 0; currentY < fullNetworkMask.length; currentY++){
            for(currentX = 0; currentX < fullNetworkMask[currentY].length; currentX++){
                if ( ((fullNetworkMask[currentY][currentX] == 1) && 
                      (polygonMask[currentY][currentX] == 1)) || 
                     (this.isDiagonalCrossing(polygonMask, fullNetworkMask, 
                                              currentX, currentY)) ){
                    
                    if(currentX == 908 && currentY == 1488){
                        System.out.print("");
                    }
                    
                    nextPoint = this.getNextFlowPoint(currentX, currentY, 
                                                      directionMatrix);
                    
                    prevPoint = this.getPrevFlowPoint(currentX, currentY, 
                                                      directionMatrix, 
                                                      fullNetworkMask);
                    
                    if(nextPoint != null){
                        // if it was possible to determine next point, get better basin
                        int prvLen, curLen, nxtLen, maxLen;
                        Basin p0, p1, p2;
                        
                        // get basin of previous point
                        if(prevPoint != null) {
                            p0 = new Basin(prevPoint[0],prevPoint[1],
                                       directionMatrix,mRaster_arg);
                            prvLen = p0.getXYBasin()[0].length;
                        } else {
                            prvLen = 0;
                        }
                        prvLen = 0;
                        
                        // get basin of current point
                        p1 = new Basin(currentX,currentY,
                                       directionMatrix,mRaster_arg);
                        curLen = p1.getXYBasin()[0].length;
                        
                        // get basin of next point
                        p2 = new Basin(nextPoint[0],nextPoint[1],
                                       directionMatrix,mRaster_arg);
                        nxtLen = p2.getXYBasin()[0].length;
                        nxtLen = 0;
                        
                        // define max lenght
                        maxLen = Math.max(curLen, Math.max(prvLen, nxtLen));
                        
                        if (maxLen == curLen){
                            //
                            curLat = MetaRasterTool.getLatFromY(mRaster_arg, currentY);
                            curLong = MetaRasterTool.getLonFromX(mRaster_arg, currentX);
                        } else if(maxLen == nxtLen) {
                            //
                            curLat = MetaRasterTool.getLatFromY(mRaster_arg, nextPoint[1]);
                            curLong = MetaRasterTool.getLonFromX(mRaster_arg, nextPoint[0]);
                        } else if(maxLen == prvLen) {
                            //
                            curLat = MetaRasterTool.getLatFromY(mRaster_arg, prevPoint[1]);
                            curLong = MetaRasterTool.getLonFromX(mRaster_arg, prevPoint[0]);
                        } else {
                            //
                            System.out.print("");
                            curLat = MetaRasterTool.getLatFromY(mRaster_arg, currentY);
                            curLong = MetaRasterTool.getLonFromX(mRaster_arg, currentX);
                        }
                        
                        currentAnylet = new Anylet(curLat, curLong);
                        this.anylets.add(currentAnylet);
                        
                        currentAnylet.setType(this.defineAnyletType(fullNetworkMask, 
                                                                    polygonMask, 
                                                                    directionMatrix, 
                                                                    mRaster_arg, 
                                                                    currentX, 
                                                                    currentY));
                        System.out.println(currentAnylet.toString(mRaster_arg));
                        
                        if((currentY >= 1438) && (currentY <= 1441)){
                            if((currentX >= 996) && (currentX <= 998)){
                                System.out.println("   NotaX: ["+currentAnylet.getLongitude()+", "+currentAnylet.getLatitude()+"]");
                            }
                        }
                        
                    } else {
                        this.defineAnyletType(fullNetworkMask, polygonMask, directionMatrix, mRaster_arg, currentY, currentY);
                        
                        //
                        curLat = MetaRasterTool.getLatFromY(mRaster_arg, currentY);
                        curLong = MetaRasterTool.getLonFromX(mRaster_arg, currentX);
                        currentAnylet = new Anylet(curLat, curLong);
                        this.anylets.add(currentAnylet);
                        
                        System.out.println(currentAnylet.toString(mRaster_arg));
                        
                        if((currentY >= 1438) && (currentY <= 1440)){
                            System.out.println("   NotaX: ["+currentAnylet.getLongitude()+", "+currentAnylet.getLatitude()+"]");
                        }
                    }
                }
            }
        }
        
        return (true);
    }
    
    private boolean isDiagonalCrossing(byte[][] polygMatrix_arg, 
                                       byte[][] linksMask_arg,
                                       int xValue_arg, int yValue_arg){
        if ((xValue_arg == 908) && (yValue_arg == 1488)) {
            System.out.print("");
        }
        
        if((polygMatrix_arg[yValue_arg][xValue_arg] == 1) && 
                (linksMask_arg[yValue_arg][xValue_arg] == 1)){
            // basic check - matrix size
            if (polygMatrix_arg.length <= (yValue_arg + 1)){
                return (false);
            }
            
            // very if can look clock-side-turning
            if(polygMatrix_arg[yValue_arg].length > (xValue_arg + 1)){
                if((polygMatrix_arg[yValue_arg + 1][xValue_arg + 1] == 1) && 
                        (linksMask_arg[yValue_arg][xValue_arg + 1] == 1) && 
                        (linksMask_arg[yValue_arg + 1][xValue_arg] == 1) &&
                        (linksMask_arg[yValue_arg + 1][xValue_arg + 1] == 0) &&
                        (polygMatrix_arg[yValue_arg][xValue_arg + 1] == 0) &&
                        (polygMatrix_arg[yValue_arg + 1][xValue_arg] == 0) ){
                    return (true);
                }
            }
            
            // very if can look anticlock-side-turning
            if(yValue_arg != 0){
                if((polygMatrix_arg[yValue_arg + 1][xValue_arg - 1] == 1) && 
                        (linksMask_arg[yValue_arg][xValue_arg - 1] == 1) && 
                        (linksMask_arg[yValue_arg + 1][xValue_arg] == 1) &&
                        (linksMask_arg[yValue_arg + 1][xValue_arg - 1] == 0) &&
                        (linksMask_arg[yValue_arg][xValue_arg - 1] == 0) &&
                        (polygMatrix_arg[yValue_arg + 1][xValue_arg] == 0) ){
                    return (true);
                }
            }
        }
        
        return (false);
    }
    
    /**
     * 
     * @param containingSubBasin_arg
     * @param mRaster_arg
     * @param dirMatrix_arg
     * @return 
     */
    public boolean identifyBorderPointsMixed(SubBasin containingSubBasin_arg,
                                             MetaRaster mRaster_arg,
                                             byte[][] dirMatrix_arg){
        SubBasin[] tmpVec;
        
        tmpVec = new SubBasin[]{containingSubBasin_arg};
        
        return (this.identifyBorderPointsMixed(tmpVec, mRaster_arg, dirMatrix_arg));
    }
            
    
    /**
     * 
     * @param containingSubBasin_arg
     * @param mRaster_arg
     * @param dirMatrix_arg
     * @return 
     */
    public boolean identifyBorderPointsMixed(SubBasin[] containingSubBasin_arg,
                                             MetaRaster mRaster_arg,
                                             byte[][] dirMatrix_arg){
        LinksAnalysis linksInfo_arg;
        
        try{
            linksInfo_arg = new LinksAnalysis(mRaster_arg, 
                                              dirMatrix_arg);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }
        
        return (this.identifyBorderPointsMixed(containingSubBasin_arg,
                                                  mRaster_arg,
                                                  linksInfo_arg));
    }
    
    /**
     * 
     * @param containingSubBasin_arg
     * @param mRaster_arg
     * @param linksInfo_arg
     * @return 
     */
    public boolean identifyBorderPointsMixed(SubBasin[] containingSubBasin_arg,
                                             MetaRaster mRaster_arg,
                                             LinksAnalysis linksInfo_arg){
        ArrayList<Anylet> listA, listB;
        Iterator<Anylet> itA, itB;
        boolean mustBeMiserable;
        boolean stillRunning;
        Anylet curA, curB;
        
        // 1: identify points by links
        // 2: store result in a variable listA
        // 3: identify points by crossing
        // 4: store result in a variable listB
        // 5: for each element in listA
        //   5.1: identify nearest point in list B
        //   5.2: remove identified element from list B
        //   5.3: substitute the element in A for element of B
        // 6: reset the list of inlets of MetaPolygonUrban as being A
        
        //1
        stillRunning = this.identifyBorderPointsByLinks(linksInfo_arg);
        if (!stillRunning){
            return (false);
        }
        
        //2
        listA = this.anylets;
        
        //3
        stillRunning = this.identifyBorderPointsByCrossing(mRaster_arg);
        if (!stillRunning){
            return (false);
        }
        
        //4
        listB = this.anylets;
        
        // verify if must be a miserable guy during selection (if listB < listA)
        if (listB.size() < listA.size()) {
            mustBeMiserable = true;
        } else {
            mustBeMiserable = false;
        }
        
        //5
        itA = listA.iterator();
        while(itA.hasNext()){
            curA = itA.next();
            
            // verify if it will be considered
            if(mustBeMiserable) {
                if (curA.getType() == MetaPolygonUrban.UNKNOW_LABEL) continue;
            }
            
            // 5.1
            itB = listB.iterator();
            int countPosition = 0;
            int smallerPosition = (-1);
            double smallerDistance = Double.MAX_VALUE;
            double currentDistance;
            while(itB.hasNext()){
                curB = itB.next();
                
                currentDistance = TwoDimensional.distanceBetweenPoints(
                                                            curA.getLatitude(), 
                                                            curA.getLongitude(), 
                                                            curB.getLatitude(),
                                                            curB.getLongitude());
                if(currentDistance < smallerDistance){
                    smallerDistance = currentDistance;
                    smallerPosition = countPosition;
                }
                
                countPosition++;
            }
            
            // basic check
            if (smallerPosition == (-1)){
                // TODO - remove check
                System.out.println("Manteve [x:"+ MetaRasterTool.getXfromLongitude(mRaster_arg, curA.getLongitude()) +", y:"+ MetaRasterTool.getYfromLatitude(mRaster_arg, curA.getLatitude()) +"] ");
                
                continue;
            }
            
            // 5.2
            Anylet extractedFromB;
            extractedFromB = listB.remove(smallerPosition);
            
            // TODO - remove check
            System.out.print("Mudou de [x:"+ MetaRasterTool.getXfromLongitude(mRaster_arg, curA.getLongitude()) +", y:"+ MetaRasterTool.getYfromLatitude(mRaster_arg, curA.getLatitude()) +"] ");
            System.out.print("para [x:"+ MetaRasterTool.getXfromLongitude(mRaster_arg, extractedFromB.getLongitude())+", y:"+ MetaRasterTool.getYfromLatitude(mRaster_arg, extractedFromB.getLatitude())+"]");
            if (curA.getType() == MetaPolygonUrban.INLET_LABEL){
                System.out.println("Type: INLET.");
            } else if (curA.getType() == MetaPolygonUrban.OUTLET_LABEL) {
                System.out.println("Type: OUTLET.");
            } else if (curA.getType() == MetaPolygonUrban.UNKNOW_LABEL) {
                System.out.println("Type: UNKNOW LET.");
            } else {
                System.out.println("Type: UNEXPECTED LET (" + curA.getType() + ").");
            }
            if(MetaRasterTool.getXfromLongitude(mRaster_arg, (float)curA.getLongitude()) == 995 || 
                    MetaRasterTool.getXfromLongitude(mRaster_arg, (float)curA.getLongitude()) == 996){
                System.out.print("");
            }
            
            // 5.3
            curA.latitude = extractedFromB.getLatitude();
            curA.longitude = extractedFromB.getLongitude();
            
        } // loop listA
        
        // 6
        this.anylets = listA;
        
        return (true);
    }
    
    public boolean identifyBorderPointsMixed(MetaRaster mRaster_arg,
                                             byte[][] dirMatrix_arg){
        LinksAnalysis linksInfo_arg;
        
        try{
            linksInfo_arg = new LinksAnalysis(mRaster_arg, 
                                              dirMatrix_arg);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }
        
        return (this.identifyBorderPointsMixed(mRaster_arg, linksInfo_arg));
    }
    
    public boolean identifyBorderPointsMixed(MetaRaster mRaster_arg,
                                             LinksAnalysis linksInfo_arg){
        return(this.identifyBorderPointsMixed(null, mRaster_arg, linksInfo_arg));
    }
    
    public boolean identifyBorderPointsMixedMax(SubBasin[] containingSubBasin_arg,
                                                MetaRaster mRaster_arg,
                                                LinksAnalysis linksInfo_arg){
        ArrayList<Anylet> listA, listB;
        Iterator<Anylet> itA, itB;
        boolean stillRunning;
        boolean mustBeMiserable;
        Anylet curA, curB;
        int tempID;
        
        // 1: identify points by links
        // 2: store result in a variable listA
        // 3: identify points by crossing
        // 4: store result in a variable listB
        // 5: for each element in listA
        //   5.1: identify nearest point in list B
        //   5.2: remove identified element from list B
        //   5.3: substitute the element in A for element of B
        //   5.4: get ID of new point
        //   5.5: identify link with nearest ID (headID or contactID)
        //   5.6: substitute position of the element in A for ID position
        // 6: reset the list of inlets of MetaPolygonUrban as being A
        
        //1
        stillRunning = this.identifyBorderPointsByLinks(linksInfo_arg);
        if (!stillRunning){
            return (false);
        }
        
        //2
        listA = this.anylets;
        
        //3
        stillRunning = this.identifyBorderPointsByCrossing(mRaster_arg);
        if (!stillRunning){
            return (false);
        }
        
        //4
        listB = this.anylets;
        
        // verify if must be a miserable guy during selection (if listB < listA)
        if (listB.size() < listA.size()) {
            mustBeMiserable = true;
        } else {
            mustBeMiserable = false;
        }
        
        //5
        itA = listA.iterator();
        while(itA.hasNext()){
            curA = itA.next();
            
            // verify if it will be considered
            if(mustBeMiserable) {
                if (curA.getType() == MetaPolygonUrban.UNKNOW_LABEL) continue;
            }
            
            // 5.1
            itB = listB.iterator();
            int countPosition = 0;
            int smallerPosition = (-1);
            double smallerDistance = Double.MAX_VALUE;
            double currentDistance;
            while(itB.hasNext()){
                curB = itB.next();
                
                currentDistance = TwoDimensional.distanceBetweenPoints(
                                                            curA.getLatitude(), 
                                                            curA.getLongitude(), 
                                                            curB.getLatitude(),
                                                            curB.getLongitude());
                if(currentDistance < smallerDistance){
                    smallerDistance = currentDistance;
                    smallerPosition = countPosition;
                }
                
                countPosition++;
            }
            
            // basic check
            if (smallerPosition == (-1)){
                // TODO - remove check
                System.out.print("Manteve [x:"+ MetaRasterTool.getXfromLongitude(mRaster_arg, curA.getLongitude()) +", y:"+ MetaRasterTool.getYfromLatitude(mRaster_arg, curA.getLatitude()) +"] ");
                
                continue;
            }
            
            // 5.2
            Anylet extractedFromB;
            extractedFromB = listB.remove(smallerPosition);
            
            // TODO - remove check
            System.out.print("Mudou de [x:"+ MetaRasterTool.getXfromLongitude(mRaster_arg, curA.getLongitude()) +", y:"+ MetaRasterTool.getYfromLatitude(mRaster_arg, (float)curA.getLatitude()) +"] ");
            System.out.print("para [x:" + MetaRasterTool.getXfromLongitude(mRaster_arg, extractedFromB.getLongitude())+", y:"+MetaRasterTool.getYfromLatitude(mRaster_arg, (float)extractedFromB.getLatitude())+"]");
            if (curA.getType() == MetaPolygonUrban.INLET_LABEL){
                System.out.println("Type: INLET.");
            } else if (curA.getType() == MetaPolygonUrban.OUTLET_LABEL) {
                System.out.println("Type: OUTLET.");
            } else if (curA.getType() == MetaPolygonUrban.UNKNOW_LABEL) {
                System.out.println("Type: UNKNOW LET.");
            } else {
                System.out.println("Type: UNEXPECTED LET (" + curA.getType() + ").");
            }
            
            // 5.3
            curA.latitude = extractedFromB.getLatitude();
            curA.longitude = extractedFromB.getLongitude();
            
            // 5.4
            tempID = MetaRasterTool.getIdFromLongLat(mRaster_arg,
                                                     (float)curA.getLongitude(), 
                                                     (float)curA.getLatitude());
            
            // 5.5
            smallerPosition = (-1);
            smallerDistance = Double.MAX_VALUE;
            int[] allContacts, allHeads;
            allContacts = linksInfo_arg.contactsArray;
            allHeads = linksInfo_arg.headsArray;
            for(int countz = 0; countz < allContacts.length; countz++){
                double currentDistanceA, currentDistanceB;
                currentDistanceA = MetaRasterTool.getPixelDistanceFromIDs(mRaster_arg,
                                                                          allHeads[countz], 
                                                                          tempID);
                currentDistanceB = MetaRasterTool.getPixelDistanceFromIDs(mRaster_arg,
                                                                          allContacts[countz], 
                                                                          tempID);
                
                if (currentDistanceA < currentDistanceB){
                    if (currentDistanceA < smallerDistance){
                        smallerPosition = countz;
                        smallerDistance = currentDistanceA;
                    }
                } else {
                    if (currentDistanceB < smallerDistance){
                        smallerPosition = countz;
                        smallerDistance = currentDistanceB;
                    }
                }
            }
            
            // 5.6
            if(smallerPosition != (-1)){
                
                tempID = allContacts[smallerPosition];
                
                curA.latitude = MetaRasterTool.getLatFromID(mRaster_arg, tempID);
                curA.longitude = MetaRasterTool.getLonFromID(mRaster_arg, tempID);
            }
            
        } // loop listA
        
        // 6
        this.anylets = listA;
        
        return (true);
    }
    
    /**
     * 
     * @param x_arg
     * @param y_arg
     * @param metaRst_arg
     * @return 
     */
    public boolean isPointInsidePolygon(int x_arg, int y_arg, MetaRaster metaRst_arg){
        return (this.isPointInsidePolygon(x_arg, y_arg, metaRst_arg, null));
    }
    
    /**
     * Working function. Verify if specific point is inside Polygon
     * @param x_arg
     * @param y_arg
     * @param metaRst_arg
     * @param vertices_arg
     * @return TRUE if point is inside basin, FALSE otherwise
     */
    public boolean isPointInsidePolygon(int x_arg, int y_arg, 
                                        MetaRaster metaRst_arg,
                                        int[][] vertices_arg){
        int[][] vertices;
        
        // basic check
        if (metaRst_arg == null) return (false);
        
        if (vertices_arg == null){
            vertices = this.getVerticesInXandY(metaRst_arg);
        } else {
            vertices = vertices_arg;
        }
        
        return (TwoDimensional.isPointInsidePolygonJTS(x_arg, y_arg, vertices));
    }
    
    public static void setAnyletConsideration(boolean consider_arg){
        MetaPolygonUrban.includeAnylets = consider_arg;
    }
    
    /**
     * 
     * @return 
     */
    public float getMinLatitude(){
        float[][] allVertices;
        float minimunValue;
        int count;
        
        // get all vertices and basic check it
        allVertices = super.getLonLatPolygon();
        if (allVertices == null) return (Float.NaN);
        if (allVertices[1].length == 0) return (Float.NaN);
        
        minimunValue = Float.MAX_VALUE;
        for(count = 0; count < allVertices[1].length; count++){
            if (allVertices[1][count] < minimunValue) {
                minimunValue = allVertices[1][count];
            }
        }
        
        return (minimunValue);
    }
    
    /**
     * 
     * @return 
     */
    public float getMaxLatitude(){
        float[][] allVertices;
        float maxValue;
        int count;
        
        // get all vertices and basic check it
        allVertices = super.getLonLatPolygon();
        if (allVertices == null) return (Float.NaN);
        if (allVertices[1].length == 0) return (Float.NaN);
        
        maxValue = Float.MAX_VALUE * (-1);
        for(count = 0; count < allVertices[1].length; count++){
            if (allVertices[1][count] > maxValue) {
                maxValue = allVertices[1][count];
            }
        }
        
        return (maxValue);
    }
    
    /**
     * 
     * @return 
     */
    public float getMinLongitude(){
        float[][] allVertices;
        float minValue;
        int count;
        
        // get all vertices and basic check it
        allVertices = super.getLonLatPolygon();
        if (allVertices == null) return (Float.NaN);
        if (allVertices[0].length == 0) return (Float.NaN);
        
        minValue = Float.MAX_VALUE;
        for(count = 0; count < allVertices[0].length; count++){
            if (allVertices[0][count] < minValue) {
                minValue = allVertices[0][count];
            }
        }
        
        return (minValue);
    }
    
    /**
     * 
     * @return 
     */
    public float getMaxLongitude(){
        float[][] allVertices;
        float maxValue;
        int count;
        
        // get all vertices and basic check it
        allVertices = super.getLonLatPolygon();
        if (allVertices == null) return (Float.NaN);
        if (allVertices[0].length == 0) return (Float.NaN);
        
        maxValue = Float.MAX_VALUE * (-1);
            
        for(count = 0; count < allVertices[0].length; count++){
            if (allVertices[0][count] > maxValue) {
                maxValue = allVertices[0][count];
            }
        }
        
        return (maxValue);
    }
    
    /**
     * 
     * @param metaRaster_arg
     * @return 
     */
    public int[] getAnyletsID(MetaRaster metaRaster_arg){
        Iterator<Anylet> allAnyletsIt;
        Anylet curAnylet;
        int[] retInt;
        int count;
        int curId;
        
        // basic check
        if (metaRaster_arg == null) return (null);
        
        // 
        retInt = new int [this.anylets.size()];
        
        count = 0;
        allAnyletsIt = this.anylets.iterator();
        while(allAnyletsIt.hasNext()){
            curAnylet = allAnyletsIt.next();
            curId = MetaRasterTool.getIdFromLongLat(metaRaster_arg,
                                                    (float)curAnylet.getLongitude(), 
                                                    (float)curAnylet.getLatitude());
            retInt[count] = curId;
            count++;
        }
        
        return(retInt);
    }
    
    public double[][] getInlets(){
        return(this.getAnylets(MetaPolygonUrban.INLET_LABEL));
    }
    
    public int[][] getInletsXY(MetaRaster metaRaster_arg){
        return(this.getAnyletsXY(MetaPolygonUrban.INLET_LABEL, 
                                 metaRaster_arg));
    }
    
    public int[][] getOutletsXY(MetaRaster metaRaster_arg){
        return(this.getAnyletsXY(MetaPolygonUrban.OUTLET_LABEL, 
                                 metaRaster_arg));
    }
    
    public double[][] getOulets(){
        return(this.getAnylets(MetaPolygonUrban.OUTLET_LABEL));
    }
    
    /**
     * 
     * @param anyletType_arg 
     * @return An matrix with size [N][2], where [2]=[long,lat]
     */
    private double[][] getAnylets(int anyletType_arg){
        Iterator<Anylet> anyletIt;
        Anylet currentAnylet;
        double[][] retVec;
        int countInlets;
        
        if (this.anylets == null) return (null);
        
        // count number of elements
        anyletIt = this.anylets.iterator();
        countInlets = 0;
        while(anyletIt.hasNext()) {
            currentAnylet = anyletIt.next();
            if (currentAnylet.getType() == anyletType_arg) {
                countInlets++;
            } else if (MetaPolygonUrban.includeAnylets && 
                    (currentAnylet.getType() == MetaPolygonUrban.UNKNOW_LABEL)){
                countInlets++;
            }
        }
        
        // alloc vector space
        retVec = new double[countInlets][2];
        
        // fill vector
        anyletIt = this.anylets.iterator();
        countInlets = 0;
        while(anyletIt.hasNext()) {
            currentAnylet = anyletIt.next();
            if ((currentAnylet.getType() == anyletType_arg) || 
                    (MetaPolygonUrban.includeAnylets && 
                    (currentAnylet.getType() == MetaPolygonUrban.UNKNOW_LABEL)) ) {
                retVec[countInlets][0] = currentAnylet.longitude;
                retVec[countInlets][1] = currentAnylet.latitude;
                countInlets++;
            }
        }
        
        return(retVec);
    }
    
    /**
     * 
     * @param anyletType_arg
     * @param theRaster_arg
     * @return An matrix with size [N][2], where [2]=[X,Y]
     */
    private int[][] getAnyletsXY(int anyletType_arg,
                                 MetaRaster theRaster_arg){
        double[][] anyletsLonLat;
        int[][] anyletsXY;
        int count;
        
        //
        anyletsLonLat = this.getAnylets(anyletType_arg);
        if(anyletsLonLat == null) return (null);
        
        anyletsXY = new int[anyletsLonLat.length][2];
        for(count=0; count < anyletsLonLat.length; count++){
            anyletsXY[count][0] = MetaRasterTool.getXfromLongitude(theRaster_arg,
                                                                   anyletsLonLat[count][0]);
            anyletsXY[count][1] = MetaRasterTool.getYfromLatitude(theRaster_arg, 
                                                                  anyletsLonLat[count][1]);
        }
        
        // TODO - remove
        //System.out.println("Found " + anyletsXY.length + " lets of type " + anyletType_arg);
        
        return(anyletsXY);
    }
    
    /**
     * 
     * @param metaRst_arg MetaRaster of respective MetaDEM
     * @return A matrix of size [N][2], where 2={X,Y}
     */
    private int[][] getVerticesInXandY(MetaRaster metaRst_arg){
        float curLon, curLat;
        int curX, curY;
        float[][] latLng;
        int currentItem;
        int[][] returnMtx;
        
        // basic check
        if (metaRst_arg == null) return (null);
        
        // get vertices and basic check it
        latLng = super.getLonLatPolygon();
        if (latLng == null) return (null);
        
        // 
        returnMtx = new int[latLng[0].length][2];
        
        for(currentItem = 0; currentItem < latLng[0].length; currentItem++){
            curLon = latLng[0][currentItem];
            curLat = latLng[1][currentItem];
            
            curX = MetaRasterTool.getXfromLongitude(metaRst_arg, curLon);
            curY = MetaRasterTool.getYfromLatitude(metaRst_arg, curLat);
            
            returnMtx[currentItem][0] = curX;
            returnMtx[currentItem][1] = curY;
        }
        
        return (returnMtx);
    }
    
    public String toString(boolean considerVertices_arg, 
                           boolean considerAnylets_arg){
        Iterator<Anylet> anyletIt;
        Anylet currentAnylet;
        String returnString;
        int count;
        
        returnString = "";
        
        // add vertices info if necessary
        if (considerVertices_arg){
            // TODO
        }
        
        // add anylets info necessary
        if (considerAnylets_arg){
            
            // starting vars
            returnString = "[";
            anyletIt = this.anylets.iterator();
            
            count = 1;
            while(anyletIt.hasNext()){
                currentAnylet = anyletIt.next();
                returnString += " (" + count + ")";
                returnString += currentAnylet.toString();
                
                if(anyletIt.hasNext()){
                    returnString += "; ";
                    returnString += '\n';
                }
                
                count++;
            }
            
            // closing var
            returnString += "]";
        }
        
        return (returnString);
    }
    
    // TODO - recheck place
    /**
     * 
     * @param destinationFilePath_arg
     * @return 
     */
    public boolean exportToSwmmFile(String destinationFilePath_arg){
        
        return (this.exportToSwmmFile(destinationFilePath_arg, false));
    }
    
    // TODO - recheck place
    public boolean exportToSwmmFile(String destinationFilePath_arg,
                                    boolean forceOverwrite_arg){
        File destinationFile;
        
        destinationFile = new File(destinationFilePath_arg);
        
        return (this.exportToSwmmFile(destinationFile, forceOverwrite_arg));
    }
    
    // TODO - recheck place
    public boolean exportToSwmmFile(File destinationFile_arg){
        return (this.exportToSwmmFile(destinationFile_arg, false));
    }
    
    // TODO - recheck place
    public boolean exportToSwmmFile(File destinationFile_arg, 
                                    boolean forceOverwrite_arg){
        BufferedOutputStream buffWriter;
        FileOutputStream fileWriter;
        DataOutputStream dataWriter;
        PrintWriter pWriter;
        boolean procedOk;
        
        // basic checks
        if (destinationFile_arg == null) return (false);
        if (destinationFile_arg.isDirectory()) return (false);
        if (destinationFile_arg.exists() && (!forceOverwrite_arg)) return (false);
        
        // starting variables
        procedOk = true;
        
        // if file alread exists, delete it
        if (destinationFile_arg.exists()){
            procedOk = destinationFile_arg.delete();
        }
        
        // create brand new full clean file and open writers
        /*try{
            procedOk = destinationFile_arg.createNewFile();
            fileWriter = new FileOutputStream(destinationFile_arg);
            buffWriter = new BufferedOutputStream(fileWriter);
            dataWriter = new DataOutputStream(buffWriter);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }*/
        
        try{
            pWriter = new PrintWriter(destinationFile_arg);
        } catch (FileNotFoundException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }
        
        // write file properly
        try{
            //this.writeSwmmFile(dataWriter);
            this.writeSwmmFile(pWriter);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }
        
        // close conections
        pWriter.close();
        /*try{
            dataWriter.close();
            buffWriter.close();
            fileWriter.close();
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
        }*/
        
        return (true);
    }
    
    /**
     * 
     * @param dataWriter_arg
     * @throws IOException 
     */
    private void writeSwmmFile(PrintWriter dataWriter_arg) throws IOException {
        int countInlet, countOutlet, countAnylet, countVertices, count;
        float minLon, maxLon, minLat, maxLat;
        float[][] lonLatPolygonShape;
        Iterator<Anylet> anyletIt;
        Anylet currentAnylet;
        boolean writeInflows;
        double[][] flowSerie;
        float[] mapExtremes;
        String lastDate, lastTime;
        
        // getting basic values and basic check
        minLon = this.getMinLongitude();
        maxLon = this.getMaxLongitude();
        minLat = this.getMinLatitude();
        maxLat = this.getMaxLatitude();
        if ( (minLon == Float.NaN) || (maxLon == Float.NaN) || 
                (minLat == Float.NaN) || (maxLat == Float.NaN) ){
            return;
        }
        
        // defining map limits
        mapExtremes = this.determineMapLimits(minLon, minLat, maxLon, maxLat);
        
        // TODO - inset title properly
        dataWriter_arg.println("[TITLE]");
        dataWriter_arg.println("");
        
        // writing options
        dataWriter_arg.println("[OPTIONS]");
        dataWriter_arg.println("FLOW_UNITS           CMS");
        dataWriter_arg.println("INFILTRATION         HORTON");
        dataWriter_arg.println("FLOW_ROUTING         KINWAVE");
        dataWriter_arg.println("START_DATE           09/18/2013");
        dataWriter_arg.println("START_TIME           00:00:00");
        dataWriter_arg.println("REPORT_START_DATE    09/18/2013");
        dataWriter_arg.println("REPORT_START_TIME    00:00:00");
        dataWriter_arg.println("END_DATE             09/18/2013");
        dataWriter_arg.println("END_TIME             06:00:00");
        dataWriter_arg.println("SWEEP_START          1/1");
        dataWriter_arg.println("SWEEP_END            12/31");
        dataWriter_arg.println("DRY_DAYS             0");
        dataWriter_arg.println("REPORT_STEP          00:15:00");
        dataWriter_arg.println("WET_STEP             00:05:00");
        dataWriter_arg.println("DRY_STEP             01:00:00");
        dataWriter_arg.println("ROUTING_STEP         0:00:30 ");
        dataWriter_arg.println("ALLOW_PONDING        NO");
        dataWriter_arg.println("INERTIAL_DAMPING     PARTIAL");
        dataWriter_arg.println("VARIABLE_STEP        0.75");
        dataWriter_arg.println("LENGTHENING_STEP     0");
        dataWriter_arg.println("MIN_SURFAREA         0");
        dataWriter_arg.println("NORMAL_FLOW_LIMITED  BOTH");
        dataWriter_arg.println("SKIP_STEADY_STATE    NO");
        dataWriter_arg.println("FORCE_MAIN_EQUATION  H-W");
        dataWriter_arg.println("LINK_OFFSETS         DEPTH");
        dataWriter_arg.println("MIN_SLOPE            0");
        dataWriter_arg.println("");
        
        // writing general parameter
        dataWriter_arg.println("[EVAPORATION]");
        dataWriter_arg.println(";;Type       Parameters");
        dataWriter_arg.println(";;---------- ----------");
        dataWriter_arg.println("CONSTANT     0.0");
        dataWriter_arg.println("DRY_ONLY     NO");
        
        // writing rain gauge if there is any rain data sequence
        if (this.rainData != null){
            dataWriter_arg.println("[RAINGAGES]");
            dataWriter_arg.println(";;               Rain      Time   Snow   Data  ");
            dataWriter_arg.println(";;Name           Type      Intrvl Catch  Source");
            dataWriter_arg.println(";;-------------- --------- ------ ------ ----------");
            dataWriter_arg.println("myRainGauge      INTENSITY 1:00   1.0    TIMESERIES myRainData");
        }
        
        // writing standard informations about catchment and its subareas
        dataWriter_arg.println("[SUBCATCHMENTS]");
        dataWriter_arg.println(";;                                                 Total    Pcnt.             Pcnt.    Curb     Snow    ");
        dataWriter_arg.println(";;Name           Raingage         Outlet           Area     Imperv   Width    Slope    Length   Pack    ");
        dataWriter_arg.println(";;-------------- ---------------- ---------------- -------- -------- -------- -------- -------- --------");
        dataWriter_arg.println("1                *                *                5        25       500      0.5      0                ");
        dataWriter_arg.println("");
        dataWriter_arg.println("[SUBAREAS]");
        dataWriter_arg.println(";;Subcatchment   N-Imperv   N-Perv     S-Imperv   S-Perv     PctZero    RouteTo    PctRouted ");
        dataWriter_arg.println(";;-------------- ---------- ---------- ---------- ---------- ---------- ---------- ----------");
        dataWriter_arg.println("1                0.01       0.1        0.05       0.05       25         OUTLET    ");
        dataWriter_arg.println("");
        dataWriter_arg.println("[INFILTRATION]");
        dataWriter_arg.println(";;Subcatchment   MaxRate    MinRate    Decay      DryTime    MaxInfil  ");
        dataWriter_arg.println(";;-------------- ---------- ---------- ---------- ---------- ----------");
        dataWriter_arg.println("1                3.0        0.5        4          7          0         ");
        dataWriter_arg.println("");

        // writing junctions identificators (anylets) and update internal ID
        dataWriter_arg.println("[JUNCTIONS]");
        dataWriter_arg.println(";;               Invert     Max.       Init.      Surcharge  Ponded    ");
        dataWriter_arg.println(";;Name           Elev.      Depth      Depth      Depth      Area      ");
        dataWriter_arg.println(";;-------------- ---------- ---------- ---------- ---------- ----------");
        anyletIt = this.anylets.iterator();
        countInlet = 0;
        countOutlet = 0;
        countAnylet = 0;
        writeInflows = false;
        while (anyletIt.hasNext()){
            currentAnylet = anyletIt.next();
            if(currentAnylet.getType() == MetaPolygonUrban.INLET_LABEL){
                countInlet++;
                currentAnylet.setID(countInlet);
                dataWriter_arg.println("inlet_"+countInlet+"         " + currentAnylet.getAltitude() + "       0          0          0          0         ");
                if(currentAnylet.flowSerie != null) writeInflows = true;
            } else if (currentAnylet.getType() == MetaPolygonUrban.OUTLET_LABEL) {
                countOutlet++;
                currentAnylet.setID(countOutlet);
                dataWriter_arg.println("outlet_"+countOutlet+"        " + currentAnylet.getAltitude() + "       0          0          0          0         ");
            } else {
                countAnylet++;
                currentAnylet.setID(countAnylet);
                dataWriter_arg.println("anylet_"+countAnylet+"         0          0          0          0          0         ");
            }
        }
        dataWriter_arg.println("");
        
        // writing inflows if there is any
        boolean wroteTimeSeriesTitle;
        wroteTimeSeriesTitle = false;
        if (writeInflows){
            String dateStr, hourStr;
            
            // connects timeseries with input nodes
            dataWriter_arg.println("[INFLOWS]");
            dataWriter_arg.println(";;                                                 Param    Units    Scale    Baseline Baseline");
            dataWriter_arg.println(";;Node           Parameter        Time Series      Type     Factor   Factor   Value    Pattern ");
            dataWriter_arg.println(";;-------------- ---------------- ---------------- -------- -------- -------- -------- --------");
            anyletIt = this.anylets.iterator();
            while(anyletIt.hasNext()){
                currentAnylet = anyletIt.next();
                if ((currentAnylet.getType() == MetaPolygonUrban.INLET_LABEL) && 
                        (currentAnylet.flowSerie != null)) {
                    dataWriter_arg.println("inlet_" + currentAnylet.getID() + "      FLOW             inletInflow_" + currentAnylet.getID() + "          FLOW     1.0      1.0               ");
                }
            }
            dataWriter_arg.println("");
            
            // write timeserie title
            anyletIt = this.anylets.iterator();
            if (this.anylets.size() > 0){
                dataWriter_arg.println("[TIMESERIES]");
                dataWriter_arg.println(";;Name           Date       Time       Value     ");
                dataWriter_arg.println(";;-------------- ---------- ---------- ----------");
                wroteTimeSeriesTitle = true;
            }
            
            // write each time serie
            while(anyletIt.hasNext()){
                currentAnylet = anyletIt.next();
                flowSerie = currentAnylet.flowSerie;
                if (flowSerie == null) continue;
                
                lastDate = "";
                lastTime = "";
                for(count=0; count<flowSerie.length; count++){
                    // define date and data content
                    dateStr = cuencasCsvFileInterpreter.getDateFromTime((float)flowSerie[count][0]);
                    hourStr = cuencasCsvFileInterpreter.getTimeFromTime((float)flowSerie[count][0]);
                    
                    // print date line if not same date
                    if ((lastDate.trim().equalsIgnoreCase(dateStr.trim())) && 
                            lastTime.trim().equalsIgnoreCase(hourStr.trim())){
                        System.out.println("No escrive pues " + lastDate + " = " + dateStr);
                        continue;
                    } else {
                        System.out.println("Escrive pues " + lastDate + " = " + dateStr);
                    }
                    dataWriter_arg.println("inletInflow_" + currentAnylet.getID() + "    " + dateStr + " " +  hourStr + "    " + flowSerie[count][1]);
                    lastDate = dateStr;
                    lastTime = hourStr;
                }
                dataWriter_arg.println("");
            }
        }
        
        // write precipitation data
        if (!wroteTimeSeriesTitle){
            dataWriter_arg.println("[TIMESERIES]");
            dataWriter_arg.println(";;Name           Date       Time       Value     ");
            dataWriter_arg.println(";;-------------- ---------- ---------- ----------");
        }
        if (this.rainData != null){
            String dateStr, hourStr;
            for(count = 0; count < this.rainData.length; count++){
                dateStr = cuencasCsvFileInterpreter.getDateFromTime(this.rainData[count][0]);
                hourStr = cuencasCsvFileInterpreter.getTimeFromTime(this.rainData[count][0]);
                dataWriter_arg.println("myRainData" + "    " + dateStr + " " +  hourStr + "    " + this.rainData[count][1]);
            }
        }
        
        // writing more generic info
        dataWriter_arg.println("[REPORT]");
        dataWriter_arg.println("INPUT      NO");
        dataWriter_arg.println("CONTROLS   NO");
        dataWriter_arg.println("SUBCATCHMENTS ALL");
        dataWriter_arg.println("NODES ALL");
        dataWriter_arg.println("LINKS ALL");
        dataWriter_arg.println("");
        dataWriter_arg.println("[TAGS]");
        dataWriter_arg.println("");
        
        // writing informations for map limit
        dataWriter_arg.println("[MAP]");
        // TODO - minX minY maxX maxY
        dataWriter_arg.println("DIMENSIONS "+mapExtremes[0]+" "+mapExtremes[1]+" "+mapExtremes[2]+" "+mapExtremes[3]+" ");
        dataWriter_arg.println("Units      Degrees");
        dataWriter_arg.println("");
        
        // writing junctions positions
        dataWriter_arg.println("[COORDINATES]");
        dataWriter_arg.println(";;Node           X-Coord            Y-Coord           ");
        dataWriter_arg.println(";;-------------- ------------------ ------------------");
        anyletIt = this.anylets.iterator();
        countInlet = 0;
        countOutlet = 0;
        countAnylet = 0;
        while (anyletIt.hasNext()){
            currentAnylet = anyletIt.next();
            if(currentAnylet.getType() == MetaPolygonUrban.INLET_LABEL){
                countInlet++;
                dataWriter_arg.println("inlet_"+countInlet+" "  + currentAnylet.getLongitude() + " " + currentAnylet.getLatitude() + " ");
            } else if (currentAnylet.getType() == MetaPolygonUrban.OUTLET_LABEL) {
                countOutlet++;
                dataWriter_arg.println("outlet_"+countOutlet+" " + currentAnylet.getLongitude() + " " + currentAnylet.getLatitude() + " ");
            } else {
                countAnylet++;
                dataWriter_arg.println("anylet_"+countAnylet+" " + currentAnylet.getLongitude() + " " + currentAnylet.getLatitude() + " ");
            }
        }
        dataWriter_arg.println("");
        
        //
        dataWriter_arg.println("[VERTICES]");
        dataWriter_arg.println(";;Link           X-Coord            Y-Coord           ");
        dataWriter_arg.println(";;-------------- ------------------ ------------------");
        dataWriter_arg.println("");
        
        // writing polygon shape
        dataWriter_arg.println("[Polygons]");
        dataWriter_arg.println(";;Subcatchment   X-Coord            Y-Coord           ");
        dataWriter_arg.println(";;-------------- ------------------ ------------------");
        lonLatPolygonShape = super.getLonLatPolygon();
        for(countVertices = 0; countVertices < lonLatPolygonShape[0].length; countVertices++){
            dataWriter_arg.println("1                "+lonLatPolygonShape[0][countVertices]+" "+lonLatPolygonShape[1][countVertices]+" ");
        }
        
        // writing rain gauge symbol, if there is any rain data sequence
        if (this.rainData != null){
            dataWriter_arg.println("[SYMBOLS]");
            dataWriter_arg.println(";;Gage           X-Coord            Y-Coord           ");
            dataWriter_arg.println(";;-------------- ------------------ ------------------");
            dataWriter_arg.println("myRainGauge      " + minLon + "     " + maxLat);
        }
    }
    
    /**
     * 
     * @param minLon_arg
     * @param minLat_arg
     * @param maxLon_arg
     * @param maxLat_arg
     * @return 
     */
    private float[] determineMapLimits(float minLon_arg, float minLat_arg,
                                       float maxLon_arg, float maxLat_arg){
        float minLonMap, maxLonMap, minLatMap, maxLatMap;
        float deltaLon, deltaLat;
        float[] returnVector;
        
        returnVector = new float[4];
        
        deltaLon = Math.abs(maxLon_arg - minLon_arg) * 0.05f;
        deltaLat = Math.abs(maxLat_arg - minLat_arg) * 0.05f;
        
        // determine min. long.
        /*
        if (minLon_arg > 0){
            minLonMap = minLon_arg - deltaLon;
        } else {
            minLonMap = minLon_arg + deltaLon;
        }
        returnVector[0] = minLonMap;
        */
        returnVector[0] = minLon_arg - deltaLon;
        
        // determine max. lat.
        /*
        if (minLat_arg > 0) {
            minLatMap = minLat_arg - deltaLat;
        } else {
            minLatMap = minLat_arg + deltaLat;
        }
        returnVector[1] = minLatMap;
        */
        returnVector[1] = minLat_arg - deltaLat;
        
        //
        /*
        if (maxLon_arg > 0){
            maxLonMap = maxLon_arg + deltaLon;
        } else {
            maxLonMap = maxLon_arg - deltaLon;
        }
        returnVector[2] = maxLonMap;
        */
        returnVector[2] = maxLon_arg + deltaLon;
        
        //
        /*
        if (maxLat_arg > 0) {
            maxLatMap = maxLat_arg + deltaLat;
        } else {
            maxLatMap = maxLat_arg - deltaLat;
        }
        returnVector[3] = maxLatMap;
        */
        returnVector[3] = maxLat_arg + deltaLat;
        
        return(returnVector);
    }
    
    // TODO - move to a proper place
    public static int[][] getAltitudeMatrix(MetaRaster MetaDemRaster_arg){
        int[][] intMatrix;
        
        // basic check
        if(MetaDemRaster_arg == null) return (null);

        try{
            intMatrix = new hydroScalingAPI.io.DataRaster(MetaDemRaster_arg).getInt();
        } catch (IOException exp){
            System.err.println("IOException: " + exp);
            return (null);
        }
        
        return (intMatrix);
    }
    
    // TODO - move to a proper place
    public static byte[][] getDirectionMatrix(MetaRaster directionRaster_arg){
        byte[][] directionMatrix;
        
        // basic check
        if(directionRaster_arg == null) return (null);

        try{
            directionMatrix = new hydroScalingAPI.io.DataRaster(directionRaster_arg).getByte();
        } catch (IOException exp){
            System.err.println("IOException: " + exp);
            return (null);
        }
        
        return (directionMatrix);
    }
    
    // TODO - move to a proper place
    public static MetaRaster getMetaRasterDirections(File binaryFile_arg){
        return (MetaPolygonUrban.getMetaRasterDirections(binaryFile_arg, null));
    }
    
    // TODO - move to a proper place
    public static MetaRaster getMetaRasterDirections(File binaryFile_arg, 
                                                     File metaFile_arg){
        MetaRaster returnObject;
        
        // basic check
        if (binaryFile_arg == null) return (null);
        
        if (metaFile_arg == null){
            returnObject = new MetaRaster();
        } else {
            try{
                returnObject = new MetaRaster(metaFile_arg);
            } catch (IOException exp) {
                System.err.println("IOException: " + exp.getMessage());
                return (null);
            }
        }
        
        // set data as being from flow direction file
        returnObject.setLocationBinaryFile(binaryFile_arg);
        returnObject.setFormat(hydroScalingAPI.tools.ExtensionToFormat.getFormat(".dir"));
        
        return (returnObject);
    }
    
    /**
     * 
     * @param mRaster_arg
     * @return A [row][col] matrix with 1 value in points in polygon border if it was possible to determine that, NULL otherwise
     */
    public byte[][] getPolygonMask(MetaRaster mRaster_arg){
        int countVertices, nextVert;
        int[][] verticesXY;
        double l1a, l1b;
        byte[][] retMtx;
        int dtaX, dtaY;
        int subX, subY;
        int[] v1, v2; 
        
        // get vertices and basic check it
        verticesXY = this.getVerticesInXandY(mRaster_arg);
        if (verticesXY == null) return (null);
        
        // for each vertice:
        //  1 - get current (v1) and next (v2) vertices
        //  2 - identify line equation [lE] with [v1, v2]
        //  3 - for each point (curP) inside proper rectangle
        //   3.1 - verify if point (curP) is in line equation [lE]
        //    3.1.1 - if it is, checkit
        
        retMtx = new byte[mRaster_arg.getNumRows()][mRaster_arg.getNumCols()];
        System.out.println("Entra lDV");
        for(countVertices = 0; countVertices < verticesXY.length; countVertices++){
            
            // 1
            nextVert = (countVertices + 1)%verticesXY.length;
            v1 = verticesXY[countVertices];
            v2 = verticesXY[nextVert];
            
            // 2
            l1a = TwoDimensional.getLineAValue(v1[0], v1[1], v2[0], v2[1]);
            l1b = TwoDimensional.getLineBValue(v1[0], v1[1], v2[0], v2[1]);
            
            //3
            
            // 3a - determining deltas
            if(v2[0]-v1[0] > 0){
                dtaX = 1;
            } else if (v2[0]-v1[0] < 0) {
                dtaX = (-1);
            } else {
                dtaX = 0;
            }
            if(v2[1]-v1[1] > 0){
                dtaY = 1;
            } else if (v2[1]-v1[1] < 0) {
                dtaY = (-1);
            } else {
                dtaY = 0;
            }
            
            // 3b do loops
            subX = v1[0] - dtaX;
            do{
                subX += dtaX;        // update X
                subY = v1[1] - dtaY; // update Y
                do{
                    subY += dtaY;
                    
                    // 3.1
                    if(TwoDimensional.isPointInLine(subX, subY, l1a, l1b)){
                        retMtx[subY][subX] = 1;
                    } else {
                        retMtx[subY][subX] = 0;
                    }
                }while(subY != v2[1]);
            }while(subX != v2[0]);
        }
        
        return (retMtx);
    }
    
    /**
     * 
     * @param mRaster_arg
     * @return A [row][col] matrix with 1 value in points inside polygon if it was possible to determine that, NULL otherwise
     */
    public byte[][] getPolygonMaskFilled(MetaRaster mRaster_arg){
        int currentX, currentY;
        byte[][] retMtx;
        byte[][] tmpMtx;
        int maxX, maxY;
        
        int countDots;       // REMOVE THIS CHECK
        
        // basic check
        if (mRaster_arg == null) return (null);
        
        // getting matrix dimentions
        tmpMtx = MetaRasterTool.getLinksMask(mRaster_arg);
        maxY = tmpMtx.length;
        maxX = tmpMtx[0].length;
        
        // allocs returb variable space
        retMtx = new byte[maxY][maxX];
        
        // fill returned matrix
        countDots = 0;
        for(currentY = 0; currentY < maxY; currentY++){
            for(currentX = 0; currentX < maxX; currentX++){
                if(this.isPointInsidePolygon(currentX, currentY, mRaster_arg)){
                    retMtx[currentY][currentX]=1;
                    countDots++;
                }else{
                    retMtx[currentY][currentX]=0;
                }
            }
        }
        
        // TODO: remove check
        System.out.println("_-_Polygon with " + countDots + " points of " + (maxY * maxX) + " size matrix (" + (countDots/(maxY * maxX))*100 + "%)");
        
        return (retMtx);
    }
    
    
    
    private static boolean isOutputCuencasFile(File file_arg){
        int[] xy;
        
        // basic check
        xy = MetaPolygonUrban.getOutputCuencasFileBasin(file_arg);
        
        if (xy == null)
            return (false);
        else
            return (true);
    }
    
    public static int[] getOutputCuencasFileBasin(File file_arg){
        String fileName;
        
        // basic check
        if((file_arg == null)||(!file_arg.isFile())) {
            return(null);
        } else {
            fileName = file_arg.getName();
            return (MetaPolygonUrban.getOutputCuencasFileBasin(fileName));
        }
    }
    
    protected static int[] getOutputCuencasFileBasin(String fileName_arg){
        String[] splittedFileName;
        String regulerExpression;
        Matcher regExpMatcher;
        Pattern regExPattern;
        int[] basinOutPoint;
        int xVal, yVal;
        
        // basic check
        if (fileName_arg == null) return (null);
        
        // define pattern
        //aaa = "TEXTO_NUM_NUM-TEXTO.csv";
        regulerExpression = "[^_]+_[0-9]+_[0-9]+-.+\\.csv";
        
        regExPattern = Pattern.compile(regulerExpression);
        regExpMatcher = regExPattern.matcher(fileName_arg);
        
        if(regExpMatcher.matches()){
            splittedFileName = fileName_arg.split("[_-]");
        
            xVal = Integer.parseInt(splittedFileName[1]);
            yVal = Integer.parseInt(splittedFileName[2]);
            basinOutPoint = new int[]{xVal, yVal};
        
            return(basinOutPoint);
        } else {
            return (null);
        }
    }
    
    public static Hashtable buildSimulationParams(){
        return (MetaPolygonUrban.buildSimulationParams(0.6f, 0.01f));
    }
    
    /**
     * 
     * @param channelFlowVelocity_arg In m/s
     * @param hillSlopeFlowVelocity_arg In m/s
     * @return 
     */
    public static Hashtable buildSimulationParams(float channelFlowVelocity_arg,
                                                  float hillSlopeFlowVelocity_arg){
        Hashtable routingParams;
        
        routingParams = new Hashtable();

        routingParams.put("widthCoeff",1.0f);
        routingParams.put("widthExponent",0.4f);
        routingParams.put("widthStdDev",0.0f);
        
        routingParams.put("chezyCoeff",14.2f);
        routingParams.put("chezyExponent",-1/3.0f);
        
        routingParams.put("lambda1",0.3f);
        routingParams.put("lambda2",-0.1f);
        routingParams.put("v_o", channelFlowVelocity_arg);

        routingParams.put("v_h", hillSlopeFlowVelocity_arg);
        
        return (routingParams);
    }
}
