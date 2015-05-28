package hydroScalingAPI.modules.swmmCoupling.util;

import hydroScalingAPI.modules.swmmCoupling.io.metaPolygonUrban.MetaPolygonUrban;
import adlzanchetta.dataStructures.graphs.*;
import adlzanchetta.dataStructures.graphs.exceptions.IncompatibleGraphStructureException;
import hydroScalingAPI.io.MetaRaster;
import hydroScalingAPI.modules.swmmCoupling.util.geomorphology.objects.SubBasin;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Adapter class for solving basin dependencies
 * @author A. D. L. Zanchetta
 */
public class TopologicalSolver {
    private Graph topoRepresentation;
    
    public TopologicalSolver(){
        this.topoRepresentation = new Graph(Graph.DIRECTION_MANDATORY);
    }
    
    /**
     * 
     * @param addedSubBasin_arg
     * @return 
     */
    public boolean addSubBasin(SubBasin addedSubBasin_arg){
        // basic check
        if(addedSubBasin_arg == null) return (false);
        
        this.topoRepresentation.addNode(addedSubBasin_arg.getOutletID(),
                                        addedSubBasin_arg);
        return (true);
    }
    
    /**
     * 
     * @param listOfSubBasins_arg
     * @return 
     */
    public boolean addSubBasins(ArrayList<SubBasin> listOfSubBasins_arg){
        Iterator<SubBasin> subBasinsIt;
        SubBasin curAddedSubBasin;
        
        // basic check
        if(listOfSubBasins_arg == null) return (false);
        
        //
        subBasinsIt = listOfSubBasins_arg.iterator();
        while(subBasinsIt.hasNext()){
            curAddedSubBasin = subBasinsIt.next();
            this.addSubBasin(curAddedSubBasin);
        }
        
        return (true);
    }
    
    /**
     * 
     * @param addedPolygon_arg
     * @return 
     */
    public boolean addPolygonUrban(MetaPolygonUrban addedPolygon_arg){
        // basic check
        if(addedPolygon_arg == null) return false;
        
        // verify valid ID
        if(addedPolygon_arg.getId() == MetaPolygonUrban.NOID_LABEL){
            addedPolygon_arg.setId(this.getNextValidPolygonID());
        }
        
        // add note
        this.topoRepresentation.addNode(addedPolygon_arg.getId(), 
                                        addedPolygon_arg);
        return(true);
    }
    
    /**
     * 
     * @return 
     */
    private int getNextValidPolygonID(){
        int evaluatedKey;
        
        evaluatedKey = MetaPolygonUrban.NOID_LABEL - 1;
        while(this.topoRepresentation.nodeExists(evaluatedKey)){
            evaluatedKey = evaluatedKey - 1;
        }
        
        return evaluatedKey;
    }
    
    /**
     * Try to define an execution order for the simulations
     * @return A vector containing IDs all subbasins / urbanPolygons evolved if it was possible to be determined, NULL otherwise
     */
    public int[] getExecutionSequence() {
        Iterator<Comparable> execSequenceIt;
        ArrayList<Comparable> execSequence;
        Comparable currentID;
        int[] returnSequence;
        int countPos;
        
        // get return sequence in list
        try{
            execSequence = this.topoRepresentation.getDependencySequence();
        } catch (IncompatibleGraphStructureException exp) {
            System.err.println(exp.getMessage());
            return (null);
        }
        
        // prepate variables for converting list into vector
        returnSequence = new int[execSequence.size()];
        countPos = 0;
        
        // convert list into vector
        execSequenceIt = execSequence.iterator();
        while(execSequenceIt.hasNext()){
            currentID = execSequenceIt.next();
            
            if(currentID instanceof Integer) {
                returnSequence[countPos] = (Integer)currentID;
            } else {
                returnSequence[countPos] = 0;
            }
            
            countPos++;
        }
        
        return (returnSequence);
    }
    
    public ArrayList<Comparable> getIsolatedes(){
        ArrayList<Comparable> isolatedNodes;
        Comparable curComp;
        isolatedNodes = this.topoRepresentation.getIsolatedNodes();
        return (isolatedNodes);
    }
    
    /**
     * Method that defines connections between subbasins and polygons. Must be called after all SubBasin and PolygonUrban are set.
     * @param mRaster_arg MetaRaster 
     */
    public void establishLinkage(MetaRaster mRaster_arg){
        MetaPolygonUrban currentPolygon;
        Iterator<Comparable> keysIt1;
        Iterator<Comparable> keysIt2;
        Set<Comparable> allKeyNodes;
        Object currentSubBasinObj;
        Object currentPolygonObj;
        SubBasin currentSubBasin;
        Comparable currentKey1;
        Comparable currentKey2;
        int[] allAnyletsID;
        int countAnyletsID;
        int curAnyletID;
        int countLink;  // aux variable
        
        // 1 - for each node
        //  1.1 - if it is PolygonUrban
        //   1.1.1 - for each anylet
        //    1.1.1.1 - for each node
        //     1.1.1.1.1 - if it is SubBasin
        //      1.1.1.1.1.1 - if outlet = anylet
        //       1.1.1.1.1.1.1 - add as SubBasin -> PolygonUrban
        //      1.1.1.1.1.2 - if has some inlet = anylet
        //       1.1.1.1.1.2.1 - add as PolygonUrban -> SubBasin
        
        allKeyNodes = this.topoRepresentation.getNodesKey();
        
        countLink = 0;
        
        keysIt1 = allKeyNodes.iterator();
        while(keysIt1.hasNext()){                                          // 1
            currentKey1 = keysIt1.next();
            
            currentPolygonObj = this.topoRepresentation.getNodeData(currentKey1);
            
            if(!(currentPolygonObj instanceof MetaPolygonUrban)) continue; // 1.1
            
            currentPolygon = (MetaPolygonUrban)currentPolygonObj; 
                
            allAnyletsID = currentPolygon.getAnyletsID(mRaster_arg);       // 1.1.1
            for(countAnyletsID = 0; countAnyletsID < allAnyletsID.length;
                    countAnyletsID++){
                curAnyletID = allAnyletsID[countAnyletsID];
                    
                keysIt2 = allKeyNodes.iterator();                          // 1.1.1.1
                while(keysIt2.hasNext()){
                    currentKey2 = keysIt2.next();
                    
                    currentSubBasinObj = this.topoRepresentation.getNodeData(currentKey2);
                    if (!(currentSubBasinObj instanceof SubBasin)) continue;
                    
                    currentSubBasin = (SubBasin)currentSubBasinObj;        // 1.1.1.1.1
                    
                    if (currentSubBasin.getOutletID() == curAnyletID) {    // 1.1.1.1.1.1
                        this.topoRepresentation.addEdge(currentKey2, 
                                                        currentKey1);      // 1.1.1.1.1.1.1
                        countLink++;
                    } else {                                               // 1.1.1.1.1.2
                        int[] allInlets;
                        int curInletPos;
                        allInlets = currentSubBasin.getInletIDs();
                        
                        if ((currentSubBasin.getX() == 671) && (currentSubBasin.getY() == 629)){
                            System.out.print("");
                        }
                        
                        if (allInlets != null){
                            
                            for(curInletPos = 0; curInletPos < allInlets.length; curInletPos++){
                                
                                if(allInlets[curInletPos] == curAnyletID){
                                    this.topoRepresentation.addEdge(currentKey1, 
                                                        currentKey2);      // 1.1.1.1.1.2.1
                                    System.out.println("Adicionado edge da alegria!");
                                    countLink++;
                                }
                            }
                        }
                    }
                }
            }
        }
        
        System.out.println("Estabilished "+ countLink +" connections.");
    }
    
    public Object getNode(int nodeId_arg){
        Object returnObj;
        
        returnObj = this.topoRepresentation.getNodeData(nodeId_arg);
        
        return (returnObj);
    }
}
