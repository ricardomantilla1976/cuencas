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
 * ExternalNetworkExtractor.java
 *
 * Created on September 5, 2003, 4:50 PM
 */

package hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion;

import java.io.IOException;

/**
 * This is the Thread used by the {@link
 * hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionOptimizer} to
 * call external procedures that process pieces of the original DEM
 * @author Ricardo Mantilla
 */
public class ExternalTileToFile extends Thread{
    
    private String[] command;
    
    private String procName;
    public boolean completed = false; 
    public boolean executing = false;
    private java.lang.Process localProcess;
    private int managerProcIndex;

    private String outputDir;
    private int x,y;
    
    hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ParallelSimulationToFile coordinatorProc;
    
    public ExternalTileToFile(      String pn,
                                    String mFN, 
                                    int xx, 
                                    int yy, 
                                    int xxHH, 
                                    int yyHH,
                                    int scaleO,
                                    int routingType,
                                    float lambda1,
                                    float lambda2,
                                    float v_o,
                                    String stormFile,
                                    float infilRate,
                                    String outputDirectory,
                                    String connectionsO,
                                    String correctionsO,
                                    hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ParallelSimulationToFile coordinator,
                                    long iniTimeInMilliseconds,
                                    int dynaIndex,
                                    java.util.Hashtable routingParams
                                    ){

        String[] possibleTileDynamics=new String[] { "hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.TileSimulationToAsciiFile",
                                                     "hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.TileSimulationToAsciiFileLuciana",
                                                     "hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.TileSimulationToAsciiFilePradeep",
                                                     "hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.TileSimulationToAsciiFileSCSMEthod"
                                                   };
                                        
        procName=pn;
        
        System.out.println("main - dynaindex=" + "tilesim =" + dynaIndex + possibleTileDynamics[dynaIndex]);

        if(dynaIndex==3) {command=new String[] {  "/usr/bin/ssh",
                                "NODENAME",
                                System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
                                "-Xmx1500m",
                                "-Xrs",
                                "-classpath",
                                "\""+System.getProperty("java.class.path")+"\"",
                                possibleTileDynamics[dynaIndex],
                                mFN, // args[0]
                                ""+xx, // args[1]
                                ""+yy,  // args[2]
                                ""+xxHH,  // args[3]
                                ""+yyHH, // args[4]
                                ""+scaleO, // args[5]
                                ""+routingType, // args[6]
                                ""+lambda1, // args[7]
                                ""+lambda2, // args[8]
                                ""+v_o, // args[9]
                                stormFile, // args[10]
                                ""+infilRate, // args[11]
                                outputDirectory, // args[12]
                                connectionsO, // args[13]
                                correctionsO, // args[14]
                                ""+iniTimeInMilliseconds, // args[15]
                                routingParams.toString()};} // args[16]
        else  {command=new String[] {  "/usr/bin/ssh",
                                "NODENAME",
                                System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
                                "-Xmx1500m",
                                "-Xrs",
                                "-classpath",
                                System.getProperty("java.class.path"),
                                possibleTileDynamics[dynaIndex],
                                mFN, // args[0]
                                ""+xx, // args[1]
                                ""+yy,  // args[2]
                                ""+xxHH,  // args[3]
                                ""+yyHH, // args[4]
                                ""+scaleO, // args[5]
                                ""+routingType, // args[6]
                                ""+lambda1, // args[7]
                                ""+lambda2, // args[8]
                                ""+v_o, // args[9]
                                stormFile, // args[10]
                                ""+infilRate, // args[11]
                                outputDirectory, // args[12]
                                connectionsO, // args[13]
                                correctionsO, // args[14]
                                ""+iniTimeInMilliseconds};} // args[15]};} // args[16]


        coordinatorProc=coordinator;
        System.out.println(">>>> Original state: "+coordinatorProc.threadsRunning);
        
        outputDir=outputDirectory;
        x=xx;
        y=yy;

    }

    public void setComputingNode(String cn, int indexProc){
        procName=cn;
        command[1]=cn.split("-")[0];
        managerProcIndex=indexProc;
    }

    public void run(){
        
        try{
            //UNCOMMENT IF RUNNING ON SINLGE MACHINE WITH MULTIPLE PROCESSORS
            //IT KNOCKS OUT THE FIRST TWO ARGUMENTS THAT SEND PROCESSES TO AN EXTERNAL COMPUTATIONAL NODE
            //STARTING HERE
            String [] newCommand=new String[command.length-2];
            for (int i = 0; i < newCommand.length; i++) {
                newCommand[i]=command[i+2];
            }
            command=newCommand;
            //ENDING HERE

            command=new String[] {System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java -version"};

            System.out.println("Manager Created Process "+managerProcIndex+" executes: "+java.util.Arrays.toString(command));

            localProcess=java.lang.Runtime.getRuntime().exec(command);
            java.util.Date startTime=new java.util.Date();

            System.out.println("Time = " + startTime.toString()+"The command was sent" + java.util.Arrays.toString(command));

            boolean monitor = true;
            String concat="";
            while(monitor){
                String s=new String(new byte[] {Byte.parseByte(""+localProcess.getInputStream().read())});
                concat+=s;
                if(s.equalsIgnoreCase("\n")) {
                    System.out.print(concat);
                    //if(concat.substring(0, Math.min(31,concat.length())).equalsIgnoreCase("Termina escritura de Resultados")) break;
                    concat="";
                }
                if(new java.io.File(outputDir+"/Tile_"+x+"_"+y+".done").exists()) break;
            }
            
            completed=true;
            executing=false;

            coordinatorProc.compNodeNames.put(procName, false);
            System.out.println(">>>> Previous state: "+coordinatorProc.threadsRunning);
            coordinatorProc.threadsRunning--;
            System.out.println(">>>> New state: "+coordinatorProc.threadsRunning);

            localProcess.destroy();
            System.out.println("Command completed for "+procName);
            


        }catch(java.io.IOException ioe){
            System.err.println("Failed launching external process");
            System.err.println(ioe);
        }
        
    }

    public static void main(String args[]) throws java.io.IOException{
        String[] command=new String[] {System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java -version"};

        command=new String[] { "java","-version"};

        java.lang.Process localProcess=java.lang.Runtime.getRuntime().exec(command);
        boolean monitor = true;
        String concat="";
        int k=0;
        while(monitor){
            String s;
            //s=new String(new byte[] {Byte.parseByte(""+localProcess.getInputStream().read())});
            //System.out.print(s);
            s=new String(new byte[] {Byte.parseByte(""+localProcess.getErrorStream().read())});
            System.out.print(s);
            if(k++ == 1000) break;
            
        }

        System.exit(0);
    }
    
    
}
