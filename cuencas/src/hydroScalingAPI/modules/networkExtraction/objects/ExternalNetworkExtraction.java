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

package hydroScalingAPI.modules.networkExtraction.objects;

/**
 * This is the Thread used by the {@link
 * hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionOptimizer} to
 * call external procedures that process pieces of the original DEM
 * @author Ricardo Mantilla
 */
public class ExternalNetworkExtraction extends Thread{
    
    private String executorName;
    private String metaFileName, demFileName;
    private String[] command;
    private boolean running=false;
    private int timeRunning=0;
    private java.lang.Process localProcess;
    
    public ExternalNetworkExtraction(String name){
        executorName=name;
    }
    
    public void setTargetFiles(String mFN, String dFN){
        
        running=true;
        metaFileName=mFN;
        demFileName=dFN;
        
    }
    
    public boolean isBusy(){
        if(timeRunning > 100){
            localProcess.destroy();
            running=false;
            System.out.println("Failed: "+metaFileName+" form "+executorName);
        }
        timeRunning++;
        return running;
    }
    
    public void run(){
        
        command=new String[] {  System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
                                "-Xmx256m",
                                "-Xrs",
                                "-cp",
                                System.getProperty("java.class.path"),
                                "hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule",
                                metaFileName,
                                demFileName};
        try{
            System.out.println("Throws the command "+metaFileName+" form "+executorName);
            System.out.println(">> Command Sintax: "+java.util.Arrays.toString(command));
            localProcess=java.lang.Runtime.getRuntime().exec(command);
            System.out.println("The command "+metaFileName+" was thrown"+" form "+executorName);
            
            localProcess.getOutputStream().flush();
            localProcess.waitFor();
            System.out.println("The command "+demFileName+" is over"+" form "+executorName);
            localProcess.destroy();
            running=false;
        }catch(java.io.IOException ioe){
            System.err.println("Failed launching external process");
            System.err.println(ioe);
        }catch(java.lang.InterruptedException ie){
            System.err.println("Failed waiting for external process");
            System.err.println(ie);
        }
        
    }
    
    
}
