package hydroScalingAPI.modules.swmmCoupling.util.geomorphology.objects;

import hydroScalingAPI.modules.swmmCoupling.io.metaPolygonUrban.MetaPolygonUrban;
import hydroScalingAPI.io.MetaRaster;
import hydroScalingAPI.modules.swmmCoupling.objects.MetaRasterTool;
import hydroScalingAPI.util.geomorphology.objects.Basin;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * An extention of {@link hydroScalingAPI.util.geomorphology.objects.Basin} object with a "cutting" additional functionality, allowing receive input flow data
 * @author A. D. L. Zanchetta
 */
public class SubBasin extends Basin {
    
    // internal object variables
    private String basinCode;                      //
    private int streamHortoOrder;                  // 
    private int matX, matY;                        //
    private float longitudeOutlet, latitudeOutlet; // outlet position in degress
    private float lenghtTopologic;                 // highter topo lenght (km)
    private float lenghtGeometric;                 // highter geom lenght (km)
    
    private ArrayList<Integer> inletIDs;           // headID of inlet links
    private int tailID;                            // tailID of outlet link
    private double[][] inletDataA;                 // inlet data for head A (???)
    private double[][] inletDataB;                 // inlet data for head B (???)
    private SubBasin outletBasin;                 // basin which will receive outlet water
    
    // internal object variables - areas
    private int percentImpervious;                 // 0 to 100 (%)
    private float area;                            // total area (km^2)
    
    
    /**
     * 
     * @param x_arg X value of outlet point
     * @param y_arg Y value of outlet point
     * @param fullDirMatrix_arg Direction matrix of DEM related to SubBasin
     * @param metaRasterDEM_arg
     */
    public SubBasin(int x_arg, int y_arg, 
                     byte[][] fullDirMatrix_arg,
                     MetaRaster metaRasterDEM_arg){
        // Basin constructor
        super(x_arg, y_arg, fullDirMatrix_arg, metaRasterDEM_arg);
        
        float[] coordsXandY;
        
        // basic check
        if (metaRasterDEM_arg == null) 
            throw (new NullPointerException("NULL given MetaRaster object"));
        
        // set values for outlet point
        this.matX = x_arg;
        this.matY = y_arg;
        
        // set values for outlet point
        this.latitudeOutlet =  (float)MetaRasterTool.getLatFromY(metaRasterDEM_arg, y_arg);
        this.longitudeOutlet = (float)MetaRasterTool.getLonFromX(metaRasterDEM_arg, x_arg);
        
        // set 'null value' for area, inlet datas and outlet basin
        this.area = (-1);
        this.inletIDs = null;
        this.inletDataA = null;
        this.inletDataB = null;
        this.outletBasin = null;
        
        this.generateBasinCode();
    }
    
    /**
     * 
     * @param hash_arg
     * @param fullDirMatrix_arg
     * @param metaRasterDEM_arg 
     */
    public SubBasin(HashMap hash_arg,
                    byte[][] fullDirMatrix_arg,
                    MetaRaster metaRasterDEM_arg){
        this((Integer)hash_arg.get("x"), (Integer)hash_arg.get("y"),
                fullDirMatrix_arg, metaRasterDEM_arg);

        String inletXstr, inletYstr;
        int countIn, curInletID;
        Integer inletX, inletY;
        
        countIn = 1;
        do{
            // prepare labels
            inletXstr = "xIn_" + countIn;
            inletYstr = "yIn_" + countIn;
                
            // qet integer values
            inletX = (Integer)hash_arg.get(inletXstr);
            inletY = (Integer)hash_arg.get(inletYstr);
                
            // 'zuera' never stops but have limit
            if((inletX == null) || (inletY == null)) break;
                
            // add inlet
            curInletID = MetaRasterTool.getIdFromXandY(metaRasterDEM_arg,
                                                       inletX, 
                                                       inletY);
            this.addInletID(curInletID);
                
            countIn++;
        } while(true);
        
    }
    
    private void generateBasinCode(){
        this.basinCode = String.valueOf(Math.random()).substring(2);
    }
    
    /**
     * Gets geometric distance between most distant point to the basin outlet
     * @param orderConsidered_arg Minimum order to be considered
     * @param sourceBasin_arg Basin related to this BasinAutomatic object
     * @param metaRasterDEM_arg MetaRaster of any internal binary file type
     * @param directionMatrix_arg Direction matrix related to current basin topography
     * @return TRUE if it was possible to load lengths, FALSE otherwise
     */
    public boolean loadLengths(int orderConsidered_arg,
                               MetaRaster metaRasterDEM_arg,
                               byte[][] directionMatrix_arg){
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure;
        float highterTopoDist, highterGeomDist;
        float[][] totalDistances;
        float[][] ortoOrders;
        int count;
            
        // algorithm:
        // 1 - get all links of given order inside basin
        // 2 - identify which is the longest topograficaly (until basin
        //     outlet) and stores its geometic distance (in Km)
            
        // basic check
        if (orderConsidered_arg < 1) return (false);
            
        // 1 - getting internal links informations
        try{
            linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(this, metaRasterDEM_arg, directionMatrix_arg);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return(false);
        }
        totalDistances = linksStructure.getDistancesToOutlet();
        try{
            ortoOrders = linksStructure.getVarValues(4);
        } catch (IOException exp){
            System.err.println("IOException: " + exp.getMessage());
            return (false);
        }
            
        // 2 - identify which is the longest (untin basin outlet)
        // 2a - basic check
        if (totalDistances == null) return (false);
        if (totalDistances.length == 0) return (false);
        if (totalDistances[0].length == 0) return (false);
            
        // 2b - get first elements value
        highterTopoDist = totalDistances[0][0];
        highterGeomDist = totalDistances[1][0];
            
        // 2c - runs throught all values getting extremes
        for(count = 0; count < totalDistances[0].length; count++){
            // update topologic
            if(totalDistances[0][count] > highterTopoDist){
                if(ortoOrders[0][count] >= orderConsidered_arg){
                    highterTopoDist = totalDistances[0][count];
                    highterGeomDist = totalDistances[1][count];
                }
            }
        }
            
        this.lenghtTopologic = highterTopoDist;
        this.lenghtGeometric = highterGeomDist;
                    
        return (true);
    }
    
    /**
     * Identifies Horton Order of Sub Basins outlet river
     * @param metaRasterDEM_arg MetaRaster object related to generated SubBasin
     * @param directionMatrix_arg Direction matrix related to generated SubBasin
     * @return Positive number if it was possible to be identified. (-1) otherwise
     */
    public int findHortonOrder(MetaRaster metaRasterDEM_arg,
                               byte[][] directionMatrix_arg){
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure;
                
        try{
            linksStructure = new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(this, metaRasterDEM_arg, directionMatrix_arg);
        } catch (IOException exp) {
            System.err.println("IOException: " + exp.getMessage());
            return(-1);
        }
        
        return (linksStructure.basinOrder);
    }
    
        
    /**
     * 
     * @return 
     */
    public String generateBasinLabel(){
        String returnLabel;
        
        returnLabel = "x: " + this.matX + ", y: " + this.matY + " ; ";
        returnLabel += "Basin Code " + this.basinCode;
        
        if ((this.tailID != 0) && (this.tailID != -1)){
            returnLabel += " ; TailID: " + this.tailID;
        }
        
        return (returnLabel);
    }//generateBasinLabel

    /**
     * Calculates basin medium slope. Must be executed after internal lengths are defined.
     * @param metaRaster_arg DEM data related to the SubBasin
     * @return Positive un-dimensional value for slope if it was possible to be calculated. (-1) otherwise.
     */
    public double getSlope(MetaRaster metaRaster_arg){
            double basinSlope, basinHeight;
            if(this.lenghtGeometric > 0){
                
                // TODO - correct that
                //basinHeight = this.getHeight(metaRaster_arg);
                basinHeight = 0;
                
                basinSlope = (basinHeight/1000)/(this.lenghtTopologic);
            } else {
                basinSlope = (-1);
            }
            
            return (basinSlope);
        }

        /**
         * Gets difference between higher and deeper points inside basin
         * @param fixedElevationModel_arg MetaRaster with a '.corrDEM' binary content
         * @param basinMask_arg MetaRaster with a '.dir' binary content
         * @return Positive value for basin height (in meters) if it was possible to be calculated, (-1) otherwise
         */
        public float getHeight(MetaRaster fixedElevationModel_arg,
                                 byte[][] basinMask_arg){
            float highterValue, depperValue, difference;
            float[][] elevationMatrix;
            int countX, countY;
            
            // basic check
            if (basinMask_arg == null) return (-1);
            if (basinMask_arg.length == 0) return (-1);
            if (fixedElevationModel_arg == null) return (-1);
            
            try{
                elevationMatrix = fixedElevationModel_arg.getArray();
            } catch (IOException exp) {
                System.err.println("IOException: " + exp.getMessage());
                return (-1);
            }
            
            // read all values inside basin mask and get highter and smaller
            highterValue = elevationMatrix[0][0];
            depperValue = elevationMatrix[0][0];
            for(countX=0; countX < elevationMatrix.length; countX++){
                for(countY=0; countY < elevationMatrix[countX].length; countY++){
                    if(basinMask_arg[countX][countY] == 1){
                        if(elevationMatrix[countX][countY] > highterValue){
                            highterValue = elevationMatrix[countX][countY];
                        }
                        if(elevationMatrix[countX][countY] < depperValue){
                            depperValue = elevationMatrix[countX][countY];
                        }
                    }
                }
            }
            
            // gets diference
            difference = highterValue - depperValue;
            
            // TODO - remove this check
            // System.out.print("Highter: " + highterValue + "m. ");
            // System.out.print("Deeper: " + depperValue + "m. ");
            // System.out.println("dH: " + difference + "m. ");
            
            return (difference);
        }
        
    /**
     * Calculates basins medium width. Basin length and area must have been calculated.
     * @return Basin width (km) if it was possible to calculate, (-1) otherwise
     */
    public float getWidth(){
        float returnWidth;
            
        // basic check
        if (this.lenghtGeometric <= 0) return (-1);
        if (this.area <= 0) return (-1);
            
        returnWidth = this.area / this.lenghtGeometric;
            
        return (returnWidth);
    }
    
    
        
    /**
     * Set Accumulate runoff to the first subarea it finds available
     * @param newRunoff_arg (in ???)
     * @return TRUE if it was possible to set, FALSE otherwise
     */
    /*
    public boolean setRunoff(double newRunoff_arg){
        if (this.pervious != null){
            this.pervious.setCuencasAccumatedRunoff(newRunoff_arg);
            return (true);
        } else if (this.impervious0D != null) {
            this.impervious0D.setCuencasAccumatedRunoff(newRunoff_arg);
            return (true);
        } else if (this.impervious1D != null) {
            this.impervious1D.setCuencasAccumatedRunoff(newRunoff_arg);
            return (true);
        } else {
            return (false);
        }
    }
    */
        
    /**
     * 
     * @return Basin total area (in km^2) if present, (-1) otherwise
     */
    public float getArea(){
        return (this.area);
    }
    
    /**
     * 
     * @return A value from 0 to 100
     */
    public int getImperviousPercent(){
        return (this.percentImpervious);
    }
    
    /**
     * Get Horton order of main river.
     * @return 
     */
    public int getStreamHortoOrder() {
        return streamHortoOrder;
    }
    
    /**
     * 
     * @return 
     */
    public SubBasin getOutletBasin() {
        return (this.outletBasin);
    }
    
    /**
     * 
     * @return 
     */
    public int getX(){
        return (this.matX);
    }
      
    /**
     * 
     * @return 
     */
    public int getY(){
        return (this.matY);
    }
    
    /**
     * 
     * @return 
     */
    public int[] getInletIDs() {
        Iterator<Integer> tempIt;
        Integer currentID;
        int[] returnObj;
        int count;
        
        // basic check - cannot be null
        if(this.inletIDs == null) return (null);
        
        returnObj = new int[this.inletIDs.size()];
        
        //
        count = 0;
        tempIt = this.inletIDs.iterator();
        while(tempIt.hasNext()){
            currentID = tempIt.next();
            returnObj[count] = currentID;
            count++;
        }
        
        return (returnObj);
    }
    
    /**
     * 
     * @return 
     */
    public int getTailID() {
        return (this.tailID);
    }
    
    /**
     * 
     * @return 
     */
    public double[][] getInletDataA(){
        return (this.inletDataA);
    }
    
    /**
     * 
     * @return 
     */
    public double[][] getInletDataB(){
        return (this.inletDataB);
    }
    
    /**
     * 
     * @return 
     */
    public boolean hasInletDataA(){
        return (this.hasInletData('a'));
    }
    
    /**
     * 
     * @return 
     */
    public boolean hasInletDataB(){
        return (this.hasInletData('b'));
    }
    
    /**
     * 
     * @param inletLetter_arg
     * @return 
     */
    private boolean hasInletData(char inletLetter_arg){
        double[][] inletData;
        
        if (inletLetter_arg == 'a')
            inletData = this.inletDataA;
        else if (inletLetter_arg == 'b') 
            inletData = this.inletDataB;
        else
            return (false);
        
        if (inletData == null) return (false);
        if (inletData.length == 0) return (false);
        if (inletData[0].length == 0) return (false);
        
        return (true);
    }
    
    /**
     * Generates a hydrograph with all inflow data
     * @return Double matrix with size [2][N], where [0][n] is the timestamp (milliseconds) and [1][N] is the value
     */
    public double[][] getInletData(){
        double currentDate, initDate, lastDate;
        Iterator<Double[]> returnDataTmpIt;
        ArrayList<Double[]> returnDataTmp;
        Double[] temporaryRelation;
        double[][] returnData;
        double ponderedA, ponderedB;
        int countA, countB;
        int countF;
        
        // basic check
        if ((!this.hasInletDataA()) && (!this.hasInletDataB())){
            return (null);
        } else if ((!this.hasInletDataA()) && (this.hasInletDataB())) {
            return (this.inletDataB);
        } else if ((this.hasInletDataA()) && (!this.hasInletDataB())) {
            return (this.inletDataA);
        }
        
        returnDataTmp = new ArrayList<Double[]>();
        
        // define overall initial and final dates
        initDate = Math.min(this.inletDataA[0][0], this.inletDataB[0][0]);
        lastDate = Math.max(this.inletDataA[0][this.inletDataA[0].length - 1], 
                            this.inletDataB[0][this.inletDataB[0].length - 1]);
        
        // set initial variables
        currentDate = initDate;
        countA = 0;
        countB = 0;
        
        while (currentDate < lastDate){
            temporaryRelation = new Double[2];
            if (countA > (this.inletDataA[0].length - 1)) {
                ponderedA = 0;
                ponderedB = this.inletDataB[1][countB];
                countB++;
            } else if (countB > (this.inletDataB[0].length - 1)) {
                ponderedA = this.inletDataA[1][countA];
                ponderedB = 0;
                countA++;
            } else if ( (currentDate == this.inletDataA[0][countA]) && 
                 (currentDate != this.inletDataB[0][countB])){
                ponderedA = this.inletDataA[1][countA];
                ponderedB = 0;
                countA++;
            } else if ((currentDate != this.inletDataA[0][countA]) && 
                       (currentDate == this.inletDataB[0][countB])) {
                // ponderate data from A - ON
                temporaryRelation[1] = null;
                if (countA < (this.inletDataA[0].length - 1)){
                    double deltaBeg, deltaEnd, deltaTotal, pondered;
                    if ( (this.inletDataA[0][countA] < currentDate) && 
                         (this.inletDataA[0][countA + 1] > currentDate) &&
                         (countA != 0) ){
                        
                        deltaTotal = this.inletDataA[0][countA + 1] - this.inletDataA[0][countA];
                        deltaBeg = currentDate - this.inletDataA[0][countA];
                        deltaEnd = this.inletDataA[0][countA + 1] - currentDate;
                        pondered = ((this.inletDataA[1][countA] * deltaEnd) + (this.inletDataA[1][countA + 1] * deltaBeg)) 
                                        / (deltaTotal);
                        temporaryRelation[1] = this.inletDataB[1][countB] + pondered;
                    } else if ((countA != 0) && (countA != this.inletDataA[1].length)){
                        pondered = 0;
                    } else {
                        pondered = 0;
                    }
                    ponderedA = pondered;
                }
                if (temporaryRelation[1] == null) {
                    ponderedA = 0;
                }
                // ponderate data from A - OFF
                
                ponderedA = this.inletDataA[1][countA];
                ponderedB = this.inletDataB[1][countB];
                
                //System.err.println("Shouldnt have entered here! B pure.");
                //temporaryRelation[1] = this.inletDataB[1][countB];
                countB++;
            } else {
                ponderedA = this.inletDataA[1][countA];
                ponderedB = this.inletDataB[1][countB];
                countA++;
                countB++;
            }
            
            // set current date and add to return data
            temporaryRelation[0] = currentDate;
            temporaryRelation[1] = ponderedA + ponderedB;
            returnDataTmp.add(temporaryRelation);
            
            // end check - countX must be smaller than lenght X
            if ( (countA > (this.inletDataA[0].length - 1)) && 
                 (countB > (this.inletDataB[0].length - 1)) ){
                break;
            }
            
            // defining next current time
            if ((countA < this.inletDataA[0].length) && 
                (countB >= this.inletDataB[0].length)) {
                currentDate = this.inletDataA[0][countA];
            } else if ((countA >= this.inletDataA[0].length) && 
                       (countB < this.inletDataB[0].length)) {
                currentDate = this.inletDataB[0][countB];
            } else {
                currentDate = Math.min(this.inletDataA[0][countA],
                                       this.inletDataB[0][countB]);
            }
        } // while
        
        // TODO: pass to a library
        returnData = new double[2][returnDataTmp.size()];
        returnDataTmpIt = returnDataTmp.iterator();
        
        
        // convert from arraylist to vector
        countF = 0;
        while(returnDataTmpIt.hasNext()){
            try{
                temporaryRelation = returnDataTmpIt.next();
                returnData[0][countF] = temporaryRelation[0];
                returnData[1][countF] = temporaryRelation[1];
            } catch (Exception exp) {
                System.out.print("");
            }
            
            countF ++;
        }
        
        return (returnData);
    }
    
    /**
     * 
     * @param newPercent_arg
     * @return TRUE if given value is a valid percent, FALSE otherwise
     */
    public boolean setImperviousPercent(int newPercent_arg){
        // basic check
        if ((newPercent_arg > 100) || (newPercent_arg < 0)) return (false);
            
        this.percentImpervious = newPercent_arg;
        return (true);
    }
    
    /**
     * 
     * @param newArea_arg New area (in km^2)
     */
    public void setArea(float newArea_arg){
        this.area = newArea_arg;
    }
    
    /**
     * 
     * @param inletId_arg
     */
    public void addInletID(int inletId_arg) {
        Iterator<Integer> tempIt;
        int currentVal;
        
        // basic check - verify if must creat new list or not
        if (this.inletIDs == null){
            this.inletIDs = new ArrayList<Integer>();
        }
        
        // check if given inlet ID alread exists there
        tempIt = this.inletIDs.iterator();
        while(tempIt.hasNext()){
            currentVal = tempIt.next();
            if (currentVal == inletId_arg){
                return;
            }
        }
        
        // add it otherwise
        this.inletIDs.add(inletId_arg);
    }
    
    /**
     * Add all inlets present in a SubBasin HashMap object
     * @param hashedData_arg SubBasin HashMap object to be read
     * @param mRaster_arg MetaDEM MetaRaster auxiliar object
     */
    public void addInletIDs(HashMap hashedData_arg, MetaRaster mRaster_arg){
        String curXin, curYin;
        Integer curX, curY;
        int currentID;
        int count;
        
        // basic check
        if ((hashedData_arg == null) || (mRaster_arg == null)) {return;}
        
        count = 1;
        do{
            // 
            curXin = "xIn_" + count;
            curYin = "yIn_" + count;
            curX = (Integer)hashedData_arg.get(curXin);
            curY = (Integer)hashedData_arg.get(curYin);
            if ((curX == null) || (curY == null)) break;
            
            // get and add ID
            currentID = MetaRasterTool.getIdFromXandY(mRaster_arg, curX, curY);
            this.addInletID(currentID);
            
            count++;
        } while (true);
    }
    
    /**
     * 
     * @param tailId_arg 
     */
    public void setTailID(int tailId_arg) {
        this.tailID = tailId_arg;
    }
    
    /**
     * 
     * @param streamHortoOrder 
     */
    public void setStreamHortoOrder(int streamHortoOrder) {
        this.streamHortoOrder = streamHortoOrder;
    }
    
    /**
     * 
     * @param basin_arg 
     */
    public void setOutletBasin(SubBasin basin_arg){
        // TODO - remove
        if ((this.getY() == 891) && ((this.getX() == 1328) || (this.getX() == 1239))){
            System.out.print("");
        }
        
        this.outletBasin = basin_arg;
    }
    
    /**
     * 
     * @param inletData_arg
     * @return TRUE if it was possible to set data, FALSE otherwise (two slots for inlet already set)
     */
    public boolean setInletData(double[][] inletData_arg){
        
        // basic check
        if (inletData_arg == null) return (false);

        if (this.inletDataA == null){
            this.inletDataA = inletData_arg;
            return (true);
        } else if (this.inletDataB == null) {
            this.inletDataB = inletData_arg;
            return (true);
        } else {
            return (false);
        }
    }
    
    /**
     * Get values X and Y contained in a ID from a MetaNetwork object
     * @param ID_arg ID to be broke
     * @param metaData_arg MetaRader with any binary file with right size
     * @return An int[2]{X, Y} if possible doing conversion, NULL otherwise
     */
    public static int[] getXandYfromId(int ID_arg, MetaRaster metaData_arg){
        int demNumCols;
        int i, j;
           
        // basic check
        if (metaData_arg == null) return (null);
            
        // getting coordinates
        demNumCols = metaData_arg.getNumCols();
        j = ID_arg/demNumCols;
        i = ID_arg%demNumCols;
            
        return (new int[]{i,j});
    }
    
    /**
     * 
     * @param metadata_arg
     * @return 
     */
    public HashMap getHashedData(MetaRaster metadata_arg){
        HashMap returnData;
        int xValue, yValue;
        int countIDs;
        
        // basic check
        if(metadata_arg == null) return (null);
        
        returnData = new HashMap();
        
        //
        returnData.put("x", this.matX);
        returnData.put("y", this.matY);
        
        // add x's and y's inlet values
        if (this.getInletIDs() != null){
            for(countIDs = 0; countIDs < this.getInletIDs().length; countIDs++){
                xValue = MetaRasterTool.getXfromID(metadata_arg, this.getInletIDs()[countIDs]);
                yValue = MetaRasterTool.getYfromID(metadata_arg, this.getInletIDs()[countIDs]);
                returnData.put("xIn_"+(countIDs + 1), xValue);
                returnData.put("yIn_"+(countIDs + 1), yValue);
            }
        }
        
        return (returnData);
    }
    
    /**
     * Remove the area of an urban polygon from sub basin
     * @param extractedPoly_arg Polygon to have its shape extracted
     * @param directionMatrix_arg Direction matrix
     * @param theRaster_arg MetaRaster of the dem
     * @return TRUE if everything was right. False otherwise.
     */
    public boolean extract(MetaPolygonUrban extractedPoly_arg,
                           byte[][] directionMatrix_arg,
                           MetaRaster theRaster_arg){
        int[][] totalInlets;
        boolean returnBol;
        int countInlet;
        int currentID;
        
        // basic check
        if (extractedPoly_arg == null) return (false);
        if (theRaster_arg == null) return (false);
        
        // get all outlets from Urban Polygon and add it to subbasin
        MetaPolygonUrban.setAnyletConsideration(true);
        totalInlets = extractedPoly_arg.getOutletsXY(theRaster_arg);
        
        // add all outlets from Urban Polygon and add to basin
        for(countInlet = 0; countInlet < totalInlets.length; countInlet++){
            currentID = MetaRasterTool.getIdFromXandY(theRaster_arg,
                                                      totalInlets[countInlet][0], 
                                                      totalInlets[countInlet][1]);
            this.addInletID(currentID);
        }
        
        if((this.getX() == 907) && (this.getY() == 1486)){
            System.out.print("");
        }
        
        // reshape subbasin. It will exclude extra inlets
        returnBol = this.reshapeBasin(directionMatrix_arg, theRaster_arg);
        
        return (returnBol);
    }
    
    /**
     * 
     * @param directionMatrix_arg
     * @param metaDEM_arg
     * @return 
     */
    public boolean reshapeBasin(byte[][] directionMatrix_arg,
                                MetaRaster metaDEM_arg){
        int countX, countY, totalPixels, count, totalPixelsOld;
        boolean anyNeighbourFound;
        Iterator<Integer> inletIt;
        byte[][] inletBasinMask;
        byte[][] thisBasinMask;
        int inletX, inletY;
        Basin inletBasin;
        int currentInlet;
        int[][] basinXY;
        int[] inletXY;
        
        // basic check
        if (this.inletIDs == null){ return (false); }
        if (directionMatrix_arg == null){ return (false); }
        if (metaDEM_arg == null){ return (false); }
        
        if ((this.getX() == 671) && (this.getY() == 629)){
            System.out.print("");
        }
        
        // extract X and Y from inlet ID
        inletIt = this.inletIDs.iterator();
        while (inletIt.hasNext()) {
            currentInlet = inletIt.next();
        
            inletXY = SubBasin.getXandYfromId(currentInlet, metaDEM_arg);
            inletX = inletXY[0];
            inletY = inletXY[1];
            
            // basic check
            if ((inletX < 0) || (inletY < 0)) {continue;}
        
            inletBasin = new Basin(inletX, inletY, directionMatrix_arg, metaDEM_arg);
        
            // get inlet and current basin masks
            inletBasinMask = inletBasin.getBasinMask();
            thisBasinMask = this.getBasinMask();
            
            totalPixelsOld = this.getXYBasin()[0].length;
        
            // check if inlet is inside basin
            anyNeighbourFound = false;
            if (thisBasinMask[inletY-1][inletX-1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY-1][inletX] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY-1][inletX+1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY][inletX-1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY][inletX] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY][inletX+1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY+1][inletX-1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY+1][inletX] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY+1][inletX+1] == 1) anyNeighbourFound = true;
            if (!anyNeighbourFound){ 
                // TODO - remove check
                //System.out.println("Inlet ["+inletX+","+inletY+"] is outside ["+this.matX+","+this.matY+"] basin.");
                inletIt.remove();
                
                continue;
            }
            
            if (this.getX() == 1074){
                if (this.getY() == 1472){
                    System.out.print("");
                }
            }
        
            // make the NOT thing
            count = 0;
            for(countY = 0; countY < inletBasinMask.length; countY++){
                for(countX = 0; countX < inletBasinMask[countY].length; countX++){
                    if(inletBasinMask[countY][countX] == 1){
                        thisBasinMask[countY][countX] = 0;
                        count++;
                    }
                }
            }
        
            // count number of pixels
            totalPixels = 0;
            for(countY = 0; countY < thisBasinMask.length; countY++){
                for(countX = 0; countX < thisBasinMask[countY].length; countX++){
                    if(thisBasinMask[countY][countX] == 1){
                        totalPixels++;
                    }
                }
            }
            
            // if basin will have no size... whatodo!?
            if (totalPixels <= 0){
                System.out.println("Whatodo with ["+inletX+", "+inletY+"]");
                continue;
            }
        
            // transform the mask into array and insert info into basin object
            count = 0;
            basinXY = new int[2][totalPixels];
            for(countY = 0; countY < thisBasinMask.length; countY++){
                for(countX = 0; countX < thisBasinMask[countY].length; countX++){
                    if(thisBasinMask[countY][countX] == 1){
                        basinXY[0][count] = countX;
                        basinXY[1][count] = countY;
                        count++;
                    }
                }
            }
            
            // TODO - remove check
            System.out.println(" Basin previous: " + totalPixelsOld + ", and now: " + basinXY[0].length + ".");
            
            this.setXYBasin(basinXY);
        } // while anylet.hasNext
        
        // pass throught remaining anylets removing outside ones
        inletIt = this.inletIDs.iterator();
        thisBasinMask = this.getBasinMask();
        while(inletIt.hasNext()) {
            currentInlet = inletIt.next();
            
            // get inlet X and Y and basic check it
            inletXY = SubBasin.getXandYfromId(currentInlet, metaDEM_arg);
            inletX = inletXY[0];
            inletY = inletXY[1];
            if ((inletX < 0) || (inletY < 0)) {continue;}
            
            // check if inlet is inside or in the border of basin
            anyNeighbourFound = false;
            if (thisBasinMask[inletY-1][inletX-1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY-1][inletX] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY-1][inletX+1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY][inletX-1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY][inletX] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY][inletX+1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY+1][inletX-1] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY+1][inletX] == 1) anyNeighbourFound = true;
            if (thisBasinMask[inletY+1][inletX+1] == 1) anyNeighbourFound = true;
            
            // if not inside or in the border, remove it
            if (!anyNeighbourFound){
                inletIt.remove();
            }
        }
        
        return (true);
    }
    
    /**
     * Equivalent to {@link Basin.getOutletTuple()}
     * @param mRaster_arg
     * @return
     * @throws visad.VisADException
     * @throws java.rmi.RemoteException 
     */
    public visad.RealTuple getOutletTuple(MetaRaster mRaster_arg) throws visad.VisADException, 
                                                                         java.rmi.RemoteException{
        float[] LonLatBasin=new float[2];
        
        
        
        LonLatBasin[0] = (float)MetaRasterTool.getLonFromX(mRaster_arg, this.matX);
        LonLatBasin[1] = (float)MetaRasterTool.getLatFromY(mRaster_arg, this.matY);

        visad.Real[] rtd1 = {new visad.Real(visad.RealType.Longitude, LonLatBasin[0]),
                             new visad.Real(visad.RealType.Latitude,  LonLatBasin[1])};
        return new visad.RealTuple(rtd1);
    }
}
