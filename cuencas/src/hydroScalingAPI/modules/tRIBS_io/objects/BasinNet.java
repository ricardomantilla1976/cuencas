/*
 * BasinNet.java
 *
 * Created on October 5, 2007, 3:30 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.tRIBS_io.objects;

/**
 *
 * @author Ricardo Mantilla
 */

import visad.*;
import java.rmi.RemoteException;

import geotransform.coords.*;
import geotransform.ellipsoids.*;
import geotransform.transforms.*;

public class BasinNet {
    private RealType    posIndex=RealType.getRealType("posIndex"),
                        xEasting =RealType.getRealType("xEasting"),
                        yNorthing=RealType.getRealType("yNorthing");
    
    private RealTupleType   domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude),
                            domainXLYL=new RealTupleType(new RealType[] {xEasting,yNorthing});
    
    
    private UnionSet allReach;
    private Gridded2DSet[] reaches;
    private int countReach=0;
    
    private String[] infoReaches;
    
    /** Creates a new instance of BasinNet */
    public BasinNet(hydroScalingAPI.mainGUI.ParentGUI mainFrame, java.io.File pathToTriang, java.io.File pathToCNTRL, String baseName, double minX, double minY) throws RemoteException, VisADException, java.io.IOException{
        
        System.out.println(">>>> Reading Reaches Network");
        java.io.File voroFile=new java.io.File(pathToTriang.getPath()+"/"+baseName+"_reach");
        java.io.BufferedReader bufferVoro = new java.io.BufferedReader(new java.io.FileReader(voroFile));
        
        String fullLine;
        fullLine=bufferVoro.readLine();
        
        while(fullLine != null){
            if(fullLine.equalsIgnoreCase("END")) countReach++;
            fullLine=bufferVoro.readLine();
        }
        countReach--;
        
        bufferVoro.close();
        
        bufferVoro = new java.io.BufferedReader(new java.io.FileReader(voroFile));
        
        int[] numNodes=new int[countReach];

        for(int j=0;j<countReach;j++){
            fullLine=bufferVoro.readLine();
            while(!fullLine.equalsIgnoreCase("END")){
                numNodes[j]++;
                fullLine=bufferVoro.readLine();
            }
            numNodes[j]--;
        }
        
        bufferVoro.close();
        
        bufferVoro = new java.io.BufferedReader(new java.io.FileReader(voroFile));
        
        reaches=new Gridded2DSet[countReach];

        for(int j=0;j<countReach;j++){

            float[][] lines = new float[2][numNodes[j]];
            
            bufferVoro.readLine();
            
            for(int i=0;i<numNodes[j];i++){
                String[] lineData=bufferVoro.readLine().split(",");
                lines[0][i]=(float)(Double.parseDouble(lineData[0])-minX);
                lines[1][i]=(float)(Double.parseDouble(lineData[1])-minY);
            }
            
            bufferVoro.readLine();
            
            reaches[j]=new Gridded2DSet(domainXLYL,lines,lines[0].length);
            
        }

        allReach=new UnionSet(domainXLYL,reaches);
        
        System.out.println(">>>> Reading Reaches Information");
        
        infoReaches=new String[countReach];
        
        java.io.BufferedReader fileRft = new java.io.BufferedReader(new java.io.FileReader(pathToCNTRL));
        
        for(int j=0;j<countReach;j++){
            infoReaches[j]="";
            for(int k=0;k<16;k++) infoReaches[j]+=fileRft.readLine()+"\n";
            fileRft.readLine();
        }
        
        fileRft.close();
        
    }
    
    public Gridded2DSet getReach(int index){
        return reaches[index];
    }
    
    public UnionSet getReachesUnionSet(){
        return allReach;
    }
    
    public int getNumReaches(){
        return countReach;
    }
    
    public String getReachInfo(int index){
        return infoReaches[index];
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
    }
    
}
