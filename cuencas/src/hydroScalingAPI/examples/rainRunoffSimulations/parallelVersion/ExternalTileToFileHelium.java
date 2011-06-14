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
 */public class ExternalTileToFileHelium{
    
    private String[] command;
    
    private String procName;
    private String WorkDir;
    public boolean completed = false; 
    public boolean executing = false;
    public int check_in_s=0;
    
    hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ParallelSimulationToFileHelium coordinatorProc;
    
    public ExternalTileToFileHelium(      String pn,
                                    String WD,
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
                                    String EVPTFile,
                                    float infilRate,
                                    String outputDirectory,
                                    String connectionsO,
                                    String correctionsO,
                                    hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.ParallelSimulationToFileHelium coordinator,
                                    long iniTimeInMilliseconds,
                                    long endTimeInMilliseconds,
                                    int dynaIndex,
                                    java.util.Hashtable routingParams
                                    ){

        String[] possibleTileDynamics=new String[] { "hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.TileSimulationToAsciiFile",
                                                     "hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.TileSimulationToAsciiFileLuciana",
                                                     "hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.TileSimulationToAsciiFilePradeep",
                                                     "hydroScalingAPI.examples.rainRunoffSimulations.parallelVersion.TileSimulationToAsciiFileSCSMEthod"
                                                   };
                                        
        procName=pn;
        WorkDir=WD;

        System.out.println("main - dynaindex=" + "tilesim =" + dynaIndex + possibleTileDynamics[dynaIndex]);

        if(dynaIndex==3) {command=new String[] {
                                System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java",
                                "-Xmx2000m",
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
                                EVPTFile, // args[11]
                                ""+infilRate, // args[12]
                                outputDirectory, // args[13]
                                connectionsO, // args[14]
                                correctionsO, // args[15]
                                ""+iniTimeInMilliseconds, // args[16]
                                ""+endTimeInMilliseconds, // args[17]
                                routingParams.toString()};} // args[18]
        else  {command=new String[] {
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
                                ""+iniTimeInMilliseconds,
                                ""+endTimeInMilliseconds};} // args[15]};} // args[16]


        coordinatorProc=coordinator;
        System.out.println(">>>> Original state: "+coordinatorProc.threadsRunning);

    }

    public void executeCommand(){
        
        try{
            
            String procNameSH=WorkDir+procName+".sh";

            java.io.OutputStreamWriter newfile=new java.io.OutputStreamWriter(new java.io.BufferedOutputStream(new java.io.FileOutputStream(procNameSH)));


            newfile.write("#!/bin/bash"+"\n");
            
            newfile.write("#$ -o " + WorkDir + "$JOB_ID.out"+"\n");
            newfile.write("#$ -pe orte 1"+"\n");
            newfile.write("#$ -V"+"\n");
            //newfile.write("#$ -q all.q" +"\n");

           //newfile.write("#$ -q IFC"+"\n");
            
            newfile.write("#$ -ckpt user"+"\n");
            newfile.write("#$ -q all.q"+"\n");
            newfile.write("#$ -l ib=1"+"\n");
            newfile.write("#$ -cwd"+"\n");
            newfile.write("#$ -l mf=16G"+"\n");

            newfile.write("echo \"Got $NSLOTS slots.\""+"\n");
            newfile.write("echo \"TMPDIR=\" $TMPDIR"+"\n");

            newfile.write("echo \"USER=\" $USER"+"\n");
            newfile.write("echo \"PE=\" $PE"+"\n");
            newfile.write("echo \"PE_HOSTFILE=\" $PE_HOSTFILE"+"\n");

            newfile.write("HOSTFILE=\"$HOME/qsubBin/hostfile.$JOB_ID\""+"\n");
            newfile.write("FIRSTHOST=`head -1 $PE_HOSTFILE | cut -f1 -d \" \" | cut -f 1 -d \".\"`"+"\n");
            newfile.write("echo \"FIRSTHOST =\" $FIRSTHOST"+"\n");

            newfile.write("echo \"Current directory : $(pwd)\""+"\n");

            newfile.write("echo \"PATH=\" $PATH"+"\n");
            newfile.write("echo \"LD_LIBRARY_PATH=\" $LD_LIBRARY_PATH"+"\n");

            for (String partC : command) {
                newfile.write(partC+" ");
            }

            //newfile.write("> \"/Users/rmantill/qsubBin/tempScripts/logOf"+procName+".log\""+"\n");

            newfile.close();

            //String[] subCommand=new String[] {"/usr/bin/ssh","helium-login-0-1","/opt/gridengine/bin/lx26-amd64/qsub",procNameSH};

            String[] subCommand=new String[] {"/opt/gridengine/bin/lx26-amd64/qsub",procNameSH};

            //String[] subCommand=new String[] {"java","-version"};
            System.out.println("Launching the command "+java.util.Arrays.toString(subCommand));

            java.lang.Process localProcess=java.lang.Runtime.getRuntime().exec(subCommand,environmentVars,new java.io.File(WorkDir));

            System.out.println("Just Lauched the command "+java.util.Arrays.toString(subCommand));

            boolean monitor = true;
            String concat="";
            int k=0;
            while(monitor){
                String s;
                //s=new String(new byte[] {Byte.parseByte(""+localProcess.getInputStream().read())});
                //System.out.print(s);
                s=new String(new byte[] {Byte.parseByte(""+localProcess.getErrorStream().read())});
                System.out.print(s);
                if(k++ == 1000) {
                    System.out.println();
                    break;
                }

            }

            
        }catch(java.io.IOException ioe){
            System.out.println("Failed launching external process");
            System.out.println(ioe);
        }
        
    }

    public static void main(String args[]) throws java.io.IOException{
        String[] command=new String[] {System.getProperty("java.home")+System.getProperty("file.separator")+"bin"+System.getProperty("file.separator")+"java -version"};

        command=new String[] {"qsub","/Users/rmantill/qsubBin/rmantill.sh"};

        //command=new String[] {"java","-version"};

        java.lang.Process localProcess=java.lang.Runtime.getRuntime().exec(command,null,new java.io.File("/Users/rmantill/qsubBin/"));
        boolean monitor = true;
        String concat="";
        int k=0;
        while(monitor){
            String s;
            //s=new String(new byte[] {Byte.parseByte(""+localProcess.getInputStream().read())});
            //System.out.print(s);
            s=new String(new byte[] {Byte.parseByte(""+localProcess.getInputStream().read())});
            System.out.print(s);
            if(k++ == 70) {
                System.out.println();
                break;
            }
            
        }

        System.exit(0);
    }

    private String[] environmentVars=new String[]

    {
    "MODULE_VERSION_STACK=3.2.7",
    "MANPATH=/opt/modules/Modules/3.2.7/share/man:/opt/intel/cce/10.0/man:/opt/brains2/man:/usr/kerberos/man:/usr/java/latest/man:/usr/local/share/man:/usr/share/man/en:/usr/share/man:/opt/ganglia/man:/usr/man:/usr/local/man:/opt/rocks/man:/opt/sun-ct/man:/opt/gridengine/man:/opt/intel/cce/10.0/man:/opt/brains2/man:",
    "HOSTNAME=helium-login-0-1.local",
    "INTEL_LICENSE_FILE=/opt/intel/cce/10.0/licenses:/opt/intel/licenses:/Users/rmantill/intel/licenses:/Users/Shared/Library/Application Support/Intel/Licenses",
    "TERM=xterm-color",
    "SHELL=/bin/bash",
    "HISTSIZE=1000",
    "KDE_NO_IPV6=1",
    "SSH_CLIENT=128.255.31.165 53811 22",
    "FSLMULTIFILEQUIT=TRUE",
    "SGE_CELL=default",
    "SGE_ARCH=lx26-amd64",
    "MPICH_PROCESS_GROUP=no",
    "SSH_TTY=/dev/pts/4",
    "ANT_HOME=/opt/rocks",
    "USER=rmantill",
    "LS_COLORS=no=00:fi=00:di=01;34:ln=01;36:pi=40;33:so=01;35:bd=40;33;01:cd=40;33;01:or=01;05;37;41:mi=01;05;37;41:ex=01;32:*.cmd=01;32:*.exe=01;32:*.com=01;32:*.btm=01;32:*.bat=01;32:*.sh=01;32:*.csh=01;32:*.tar=01;31:*.tgz=01;31:*.arj=01;31:*.taz=01;31:*.lzh=01;31:*.zip=01;31:*.z=01;31:*.Z=01;31:*.gz=01;31:*.bz2=01;31:*.bz=01;31:*.tz=01;31:*.rpm=01;31:*.cpio=01;31:*.jpg=01;35:*.gif=01;35:*.bmp=01;35:*.xbm=01;35:*.xpm=01;35:*.png=01;35:*.tif=01;35:",
    "LD_LIBRARY_PATH=/opt/gridengine/lib/lx26-amd64:/opt/intel/cce/10.0/lib:/opt/brains2/lib/InsightToolkit:/opt/brains2/lib:",
    "TVDSVRLAUNCHCMD=/usr/bin/ssh",
    "ROCKS_ROOT=/opt/rocks",
    "KDEDIR=/usr",
    "MODULE_VERSION=3.2.7",
    "MAIL=/var/spool/mail/rmantill",
    "PATH=/opt/gridengine/bin/lx26-amd64:/opt/modules/Modules/3.2.7/bin:/usr/kerberos/bin:/usr/java/latest/bin:/opt/intel/cce/10.0/bin:/opt/fsl/bin:/opt/fips:/opt/brains2/bin:/usr/local/bin:/bin:/usr/bin:/opt/freesurfer/bin:/opt/freesurfer/bin/Linux:/opt/freesurfer/fsfast/bin:/opt/freesurfer/fsfast/bin/Linux:/opt/freesurfer/afni/Linux:/opt/freesurfer/bin/noarch:/opt/freesurfer/local/bin/Linux:/opt/ganglia/bin:/opt/ganglia/sbin:/opt/kent:/opt/ncbi/build:/opt/rocks/bin:/opt/rocks/sbin",
    "FSLMACHTYPE=linux_64-gcc4.1",
    "INPUTRC=/etc/inputrc",
    "PWD=/Users/rmantill/qsubBin",
    "_LMFILES_=/opt/modules/Modules/3.2.7/modulefiles/modules",
    "JAVA_HOME=/usr/java/latest",
    "SGE_EXECD_PORT=537",
    "LANG=en_US.iso885915",
    "KDE_IS_PRELINKED=1",
    "SGE_QMASTER_PORT=536",
    "MODULEPATH=/opt/modules/Modules/versions:/opt/modules/Modules/$MODULE_VERSION/modulefiles:/opt/modules/Modules/modulefiles",
    "FSLTCLSH=/opt/fsl/bin/fsltclsh",
    "SGE_ROOT=/opt/gridengine",
    "LOADEDMODULES=modules",
    "FSLMACHINELIST=",
    "FSLREMOTECALL=",
    "FSLCONFDIR=/opt/fsl/config",
    "FSLWISH=/opt/fsl/bin/fslwish",
    "SSH_ASKPASS=/usr/libexec/openssh/gnome-ssh-askpass",
    "SHLVL=1",
    "HOME=/Users/rmantill",
    "FREESURFER_HOME=/opt/freesurfer",
    "DYLD_LIBRARY_PATH=/opt/intel/cce/10.0/lib",
    "PYTHONPATH=:/opt/ortheus/src/python",
    "LOGNAME=rmantill",
    "FSLDIR=/opt/fsl",
    "SSH_CONNECTION=128.255.31.165 53811 128.255.1.69 22",
    "MODULESHOME=/opt/modules/Modules/3.2.7",
    "LESSOPEN=|/usr/bin/lesspipe.sh %s",
    "DISPLAY=localhost:12.0",
    "FSLLOCKDIR=",
    "FSLOUTPUTTYPE=NIFTI_GZ",
    "G_BROKEN_FILENAMES=1",
    "_=/bin/env",
    "OLDPWD=/Users/rmantill/qsubBin/tempScripts"
    };
    
    
}
