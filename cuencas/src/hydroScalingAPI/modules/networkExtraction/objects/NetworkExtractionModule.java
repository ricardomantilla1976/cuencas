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


package hydroScalingAPI.modules.networkExtraction.objects;

import hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions;
import java.util.TimeZone;
/**
 * This class controls the procedures associated to River Network Extraction from
 * DEMs.  It comunicates with the GUI and the methods needed to achieve this task
 * @author Jorge Mario Ramirez and Ricardo Mantilla
 */
public class NetworkExtractionModule implements Runnable {
    
    /**
     * The MetaRaster associated with the DEM under analysis
     */
    public hydroScalingAPI.io.MetaRaster metaDEM;
    /**
     * The DEM under analyisis
     */
    public double[][] DEM;
    /**
     * An auxiliary matrix used by the
     * {@link hydroScalingAPI.modules.networkExtraction.objects.RasterNetworkBlueLines}
     * module.
     */
    public double[][] DEMrep;
    /**
     * The direction matrix associated to the DEM under analysis
     */
    public int[][] DIR;
    /**
     * The vertical size of the pixel
     */
    public double dy;
    /**
     * The average horizontal size of the pixel
     */
    public double dxm;
    /**
     * An array containing the horizontal size of the pixels
     */
    public double[] dx;
    /**
     * The diagonal size of the pixels
     */
    public double[] dxy;
    /**
     * The {@link hydroScalingAPI.modules.networkExtraction.objects.WorkRectangle}
     * represents the area over which the network extraction algorithm is currently
     * working
     */
    public hydroScalingAPI.modules.networkExtraction.objects.WorkRectangle MT;
    /**
     * A Vector containing a list of the sinks in the DEM
     */
    public java.util.Vector Sink_t;
    /**
     * The current sink under analysis
     */
    public int sinkOrder=0;
    /**
     * The maximum order of any stream in the DEM
     */
    public int maxOrder=0;
    /**
     * The GUI used to query the user for tasks to be completed by the
     * NetworkExtractionModule
     */
    public hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions OpProc;
    /**
     * The main GIS interface
     */
    public hydroScalingAPI.mainGUI.ParentGUI parent;
    /**
     * An array containing the upstream area for each pixel in the DEM
     */
    public float[][] Areas;
    /**
     * An array containing the maximum slope for each pixel in the DEM
     */
    public double[][] MaxPend;
    /**
     * An array containing 0s and 1s for each pixel in the DEM.  Each position
     * determines if there is a channel inside the pixel
     */
    public byte RedRas[][];
    /**
     * An array used by the {@link hydroScalingAPI.modules.networkExtraction.objects.RasterNetworkBlueLines}
     * to mask the areas where there are blue lines
     */
    public byte LA[][];
    /**
     * A matrix of {@link hydroScalingAPI.modules.networkExtraction.objects.GeomorphCell_1}
     */
    public GeomorphCell_1 GEO[][];
    /**
     * A matrix of {@link
     * hydroScalingAPI.modules.networkExtraction.objects.GeomorphCell_2}
     */
    public GeomorphCell_2 GEO2[][];
    /**
     * A boolean indicating if the algorithm fails to converge in an area of the DEM
     */
    public boolean convergenceAlarmType1;
    /**
     * The number of times the algorithm fails to converge
     */
    public int convergenceAlarmCounter=0;
    /**
     * A treshold that indicates a cut in the DEM if the filled number of cells is larger than
     * cCorte times the number of cells to cut
     */
    public float cCorte = 10f;
    /**
     * A treshold that indicates a cut in the DEM if more than cAltura meters have to be filled up.
     */
    public float cAltura = 20f;
    /**
     * A threshold that indicates when the user wants to intervine in the sink
     * correction algorithm based on the sink size
     */
    public float pixManual = Float.POSITIVE_INFINITY;
    /**
     * A threshold that indicates when the user wants to intervine in the sink
     * correction algorithm based on the sink's basin size
     */
    public float pixCManual = Float.POSITIVE_INFINITY;
    /**
     * The latitude of the location of the sink that the user wants to modify
     */
    public float latManual = -100;
    /**
     * The longitude of the location of the sink that the user wants to modify
     */
    public float lonManual = -100;
    /**
     * The area threshold for network pruning
     */
    public float pixPodado = 0.01f;
    /**
     * The alpha exponent in the Area-Slope based treshold
     */
    public float alfa = 0;
    /**
     * The C coefficient in the Area-Slope based treshold
     */
    public float C = 0;
    
    /**
     * A treshold for calculating the Area-Slope analysis
     */
    public float pixAP = 0;
    /**
     * A flag that indicates if the sink area threshold is given in km^2 or in number of
     * pixels
     */
    public int unPixM = 0;
    /**
     * A flag that indicates if the sink's basin area threshold is given in km^2 or in number of
     * pixels
     */
    public int unPixCM = 0;
    /**
     * A flag that indicates if the sink location is given as a latitude or a row
     * number
     */
    public int unFilaM = 1;
    /**
     * A flag that indicates if the sink location is given as a longitude or a column
     * number
     */
    public int unColM = 0;
    /**
     * The row number of the location of the sink that the user wants to modify
     */
    public int filaManual = -100;
    /**
     * The column number of the location of the sink that the user wants to modify
     */
    public int colManual = -100;
    /**
     * The Horton order to use as threshold for pruning based on order
     */
    public int ordenMax = Integer.MAX_VALUE;
    /**
     * A flag that indicates if the area threshold is given in km^2 or in number of
     * pixels
     */
    public int unPixPod = 0;
    /**
     * A flag that indicates if the Area-Slope threshold is given in km^2 or in number of
     * pixels
     */
    public int unPixAP = 0;
    /**
     * The number of pixels to use to determine the slope
     */
    public int npuntosAP = 6;
    
    /**
     * The number of cells that need to converge in order to let a river start
     */
    public int nCeldasConv=2;
    
    /**
     * A flag that indicates the cleanShortsRoutine will be called
     */
    public boolean cleanShorts = true;
    /**
     * A flag that indicates if a ROM-based or RAM-based algorithm must be used to
     * calculate the geomorphology
     */
    public boolean archPro = false;
    /**
     * Indicates if a Blue Lines Map is to be used to Guide Network Extraction
     */
    public boolean lAzules = false;
    /**
     * Indicates if a new Area-Slope analysis is to be created
     */
    public boolean areaPend_nuevo = false;
    /**
     * Indicates if an existent Area-Slope analysis is to be loaded
     */
    public boolean areaPend_cargar = false;
    /**
     * Indicates if an Area-Slope analysis is to be created using blue lines
     */
    public boolean areaPend_LA = false;
    /**
     * Indicates the montgomery method is to be used to prune the network
     */
    public boolean montgomery = false;
    /**
     * Indicates the laplace method is to be used to prune the network
     */
    public boolean laplace = true;
    /**
     * Indicates if all the points should be considered as network
     */
    public boolean todoRed = true;
    /**
     * Indicates if an Area-Slope analysis is used as threshold
     */
    public boolean umbralAP = false;
    /**
     * A boolean stating if the module runs in Debug mode.  It forces the program to
     * print additional output
     */
    public boolean printDebug = true;
    /**
     * Indicates if the user wants to perform the Network extraction algorithm
     * (determines direction matrix)
     */
    public boolean  taskDIR=true; 
    /**
     * Indicates if the user wants to perform the Network pruning algorithm
     */
    public boolean  taskRED=true; 
    /**
     * Indicates if the user wants to perform the Geomorphology algorithm
     */
    public boolean  taskGEO=true; 
    /**
     * Indicates if the user wants to perform the Vectorial network algorithm
     */
    public boolean  taskVECT=true;
    
    /**
     * The file that contains the blue lines
     */
    public java.io.File fileLAzules;
    
    /**
     * Statstics associated to the DEM
     */
    public hydroScalingAPI.util.statistics.Stats DEMstats;
    
    private boolean firstCorrection = true;
    
    /**
     * Creates an instance of the NetworkExtractionModule that uses the {@link
     * hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions} interface
     * to query for tasks to be performed
     * @param metaDEM1 The MetaRaster associated to the DEM to be processed
     * @param dataDEM1 The DataRaster associated to the data
     * @param moreArgs A String[] of arguments that Matt Luck created
     */
    public NetworkExtractionModule(hydroScalingAPI.io.MetaRaster metaDEM1, hydroScalingAPI.io.DataRaster dataDEM1,String[] moreArgs) {
        
        
        //printDebug=false;
        
        System.out.println("La fiesta comienza");
        printDebug=true;
        metaDEM=metaDEM1;
        inicio(dataDEM1);
        System.out.println("Finish inicio");
        OpProc = new hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions(this);
        //taskDIR=true; taskRED=true; taskGEO=false; taskVECT=false;
        pixPodado=Float.parseFloat(moreArgs[2])/(float)dy/(float)dxm;
        taskDIR=false; taskRED=true; taskGEO=true; taskVECT=true; archPro=true;
        corrigeDEM();
        OpProc.dispose();
    }
    
    /**
     * Creates an instance of the NetworkExtractionModule that uses the {@link
     * hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions} interface
     * to query for tasks to be performed
     * @param parent1 The main GIS interface
     * @param metaDEM1 The MetaRaster associated to the DEM to be processed
     * @param dataDEM1 The DataRaster associated to the data
     */
    public NetworkExtractionModule(hydroScalingAPI.mainGUI.ParentGUI parent1, hydroScalingAPI.io.MetaRaster metaDEM1, hydroScalingAPI.io.DataRaster dataDEM1) {
        metaDEM=metaDEM1;
        parent=parent1;
        inicio(dataDEM1);
        OpProc = new hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions(this);
        OpProc.setVisible(true);
    }
    
    /**
     * Creates an instance of the NetworkExtractionModule that uses predetermined tasks
     * to be performed
     * @param metaDEM1 The MetaRaster associated to the DEM to be processed
     * @param dataDEM1 The DataRaster associated to the data
     */
    public NetworkExtractionModule(hydroScalingAPI.io.MetaRaster metaDEM1, hydroScalingAPI.io.DataRaster dataDEM1) {
        //printDebug=false;
        printDebug=true;
        metaDEM=metaDEM1;
        inicio(dataDEM1);
        System.out.println("Finish inicio");

        OpProc = new hydroScalingAPI.modules.networkExtraction.widgets.ExtractionOptions(this);
        //taskDIR=true; taskRED=true; taskGEO=false; taskVECT=false;
        
        //taskDIR=false; taskRED=true; taskGEO=true; taskVECT=true;
        taskDIR=true; taskRED=true; taskGEO=true; taskVECT=true;
        
        pixPodado = 0.01f/(float)dy/(float)dxm;
        
        System.out.println(" start Corrige DEM");
        corrigeDEM();
        System.out.println(" finish Corrige DEM");

        OpProc.dispose();
    }
    
    /**
     * Initializes the algorithm parameters
     * @param dataDEM The DataRaster associated to the data
     */
    public void inicio(hydroScalingAPI.io.DataRaster dataDEM){
        int nfila=metaDEM.getNumRows();
        int ncol=metaDEM.getNumCols();
            
        /*Se dimensiona las matrices maxpend[][], MD[][] y DEMC[][] con el numero de filas
         y columnas del DEM original sumandole una fila y una columna para la mascara.*/
        
        DEM = new double[nfila+2][ncol+2];
        DIR = new int[nfila+2][ncol+2];

        //Se colocan -1 en la primera fila y columna (i=0, j=0) y en las ultimas (i=nfila+1, j=ncol+1).
        for (int i=0; i<nfila+2; i++){
            DEM[i][0]=-1 ; DEM[i][ncol+1]=-1;
            DIR[i][0]=-1 ; DIR[i][ncol+1]=-1;
        }
        for (int i=0; i<ncol+2; i++){
            DEM[0][i]=-1 ; DEM[nfila+1][i]=-1;
            DIR[0][i]=-1 ; DIR[nfila+1][i]=-1;
        }
        
        for (int i=1; i<=nfila; i++){
            for (int j=1; j<=ncol ;j++){
                double valueDEM=dataDEM.getDouble(i-1,j-1);
                if (valueDEM == new Double(metaDEM.getMissing()).doubleValue() || valueDEM<=0)
                    DEM[i][j] = -1;
                else
                    DEM[i][j] = valueDEM;
            }
        }
        
        DEMstats=new hydroScalingAPI.util.statistics.Stats(DEM,-1.0);

        dy = 6378.0*metaDEM.getResLat()*Math.PI/(3600.0*180.0);
        dx = new double[nfila+1];
        dxy = new double[nfila+1];
        
        /*Se calcula para cada fila del DEMC el valor de la distancia horizontal del pixel
          y la diagonal, dependiendo de la latitud.*/
        for (int i=1 ; i<=nfila ; i++){
            dx[i] = 6378.0*Math.cos((i*metaDEM.getResLat()/3600.0 + metaDEM.getMinLat())*Math.PI/180.0)*metaDEM.getResLat()*Math.PI/(3600.0*180.0);
            dxy[i] = Math.sqrt(dx[i]*dx[i] + dy*dy);
        }
        
        dxm=dx[nfila/2];
        MT = new WorkRectangle(2,nfila-1,2,ncol-1,this);
    }
    
    /**
     * The DEM correction algorithm
     */
    public void corrigeDEM(){
        
        int nr=metaDEM.getNumRows();
        int nc=metaDEM.getNumCols();
        
        java.util.Calendar iniTime=java.util.Calendar.getInstance();
        if (printDebug) System.out.println(">>> Running fixDEM()"+" - Initial Time: "+iniTime.getTime());
        if(!firstCorrection){ leeDEM(); if (printDebug) System.out.println(">>> Running readDEM()");}
        if(taskRED && lAzules){
            LA = RasterNetworkBlueLines.Lee_Incisa(this);
            OpProc.buscandoPits();
        }
        if(taskDIR || (taskRED && lAzules)){
            do{
                corrigeUniPitFullDEM();
                OpProc.buscandoPits();
                Sink_t = new java.util.Vector();
                convergenceAlarmType1=false;
                MT.corrigeWorkRectangle(OpProc.myFSc);
                corrigeSink_t();

                MT = new WorkRectangle(2,nr-1,2,nc-1,this);

                convergenceAlarmCounter++;
                
                if(convergenceAlarmCounter > 20){
                    writeCorrDem(metaDEM.getLocationBinaryFile());
                    System.exit(0);
                    OpProc.set_ready();
                    OpProc.announceFailure();
                    return;
                }
            } while(convergenceAlarmType1);
            
            MT.direcciones(-10.0);
            if (printDebug) System.out.println(">>> Final Pit Verification PASS");
            Sink_t = new java.util.Vector();
            MT.corrigeWorkRectangle(OpProc.myFSc);
            corrigeSink_t();
            
            //Scan the matrix for crossings (This should not occur and it needs to be address in the algorithm itself)
            int countSituation=0;
            
            countSituation=0;
            for (int i=1; i<DIR.length-1; i++){
                for (int j=1; j<DIR[0].length-1; j++){
                    int rnum;  //a 0 or 1 determines how to solve a crossing
                    if(DIR[i][j] == 9 && DIR[i][j+1] == 7) {
                        rnum=(DEM[i][j]-DEM[i-1][j] > DEM[i][j+1]-DEM[i-1][j+1])?0:1;
                        DIR[i][j+rnum]=8;
                        countSituation++;
                    }
                    if(DIR[i][j] == 3 && DIR[i][j+1] == 1) {
                        rnum=(DEM[i][j]-DEM[i+1][j] > DEM[i][j+1]-DEM[i+1][j+1])?0:1;
                        DIR[i][j+rnum]=2;
                        countSituation++;
                    }

                    if(DIR[i][j] == 7 && DIR[i+1][j] == 1) {
                        rnum=(DEM[i][j]-DEM[i][j+1] > DEM[i+1][j]-DEM[i+1][j+1])?0:1;
                        DIR[i+rnum][j]=4;
                        countSituation++;
                    }
                    if(DIR[i][j] == 9 && DIR[i+1][j] == 3) {
                        rnum=(DEM[i][j]-DEM[i][j-1] > DEM[i+1][j]-DEM[i+1][j-1])?0:1;
                        DIR[i+rnum][j]=6;
                        countSituation++;
                    }
                }
            }
            System.out.println(">>> Crossings Fixed: "+countSituation);
            

            if (printDebug) System.out.println(">>> Done With Sink Correction");
            
            writeCorrDem(metaDEM.getLocationBinaryFile());
            
            GetGeomorphologyRAM.getAreas(this);
            GetGeomorphologyRAM.bordes(this);
            if(taskDIR && !lAzules){
                //GetGeomorphologyRAM.getMaxPend(this);
                writePREL(metaDEM.getLocationBinaryFile());
            }
        }
        
        if(!taskDIR) leeDEM_DIR_AREA_PEND();
        
        RedRas = new byte[DIR.length][DIR[0].length];
        
        if(taskRED){
            if(todoRed) GetRasterNetwork.todo_uno(this);
            boolean red_umbrales = (ordenMax < Integer.MAX_VALUE || pixPodado > 0);
            if(lAzules){
                RasterNetworkBlueLines.cabecitas(this,LA,true);
                //GetGeomorphologyRAM.getMaxPend(this);
                writePREL(metaDEM.getLocationBinaryFile());
            }
            if(red_umbrales){
                GetRasterNetwork.umbral_Area_Ord(this);
            }
            if(umbralAP){
                GetGeomorphologyRAM.getORD(this);
                GetRasterNetwork.umbralASalfa(this);
            }
            if(montgomery){
                GetRasterNetwork.umbral_Montgomery(this);
            }
            if(laplace && !areaPend_nuevo){
                GetRasterNetwork.laplace2(this,nCeldasConv);
            }
            
            if(areaPend_nuevo){
                System.out.println("enter the area code - number of pixels"+pixAP);
                GetRasterNetwork.umbral_Area(pixAP,this);
                GetGeomorphologyRAM.getORD(this);
                GetRasterNetwork.getArea_Pend(this);
                OpProc.graficaAP(GetRasterNetwork.calculaPromAP(this),true);
            }
            if(areaPend_cargar) OpProc.graficaAP(GetRasterNetwork.calculaPromAP(this),true);
            if(areaPend_LA){
                boolean soloPuntos = false;
                if(!soloPuntos){
                    GetRasterNetwork.todo_uno(this);
                    GetGeomorphologyRAM.getORD(this);
                }
                LA = RasterNetworkBlueLines.Lee_LA(this);
                RasterNetworkBlueLines.cabecitas(this,LA,false);
                if(!soloPuntos){
                    GetRasterNetwork.getArea_Pend_Dif(this);
                    OpProc.graficaAP(GetRasterNetwork.calculaPromAP(this),true);
                }
            }
            
            if(cleanShorts) GetRasterNetwork.cleanShorts(this);
            GetRasterNetwork.fixIntersections(this);
            
            GetGeomorphologyRAM.getAreas(this);
            //GetGeomorphologyRAM.getMaxPend(this);
            
            writePREL(metaDEM.getLocationBinaryFile());
            
            if(!areaPend_cargar && !areaPend_nuevo && !areaPend_LA )
                writeRED(metaDEM.getLocationBinaryFile());
            
        }
        
        if(!taskRED) leeRED();
        
        OpProc.setMaxMinGeomorphBar(0, 5);
        OpProc.setValueGeomorphBar(1);
        
        if(taskGEO){
            
            if(!archPro){
                GetGeomorphologyRAM.getDistanceToBorder(this);
                OpProc.setValueGeomorphBar(2);
                System.gc();
                GetGeomorphologyRAM.getGEO(this);
                OpProc.setValueGeomorphBar(3);
                writeGEO(metaDEM.getLocationBinaryFile());
                OpProc.setValueGeomorphBar(4);
                GetGeomorphologyRAM.getRedVect(this);
                OpProc.setValueGeomorphBar(5);
            }else{
                if (printDebug) System.out.println(">>> Calculating Geormorphology - ROM based Algorithm");

                MaxPend=null;
                System.gc();
                
                //GetGeomorphologyRAM.getDistanceToBorder(this);
                OpProc.setValueGeomorphBar(2);
                
                
                new hydroScalingAPI.modules.networkExtraction.objects.GetGeomorphologyROM(metaDEM,this);
                OpProc.setValueGeomorphBar(3);
                leeRED();
                
                if (printDebug) System.out.println(">>> Geormorphology Completed");
                OpProc.setValueGeomorphBar(4);
                
                
            }
        }
        
        if(taskVECT){
            
            GetGeomorphologyRAM.getRedVect(this);
        }
        OpProc.setValueGeomorphBar(5);
        
        java.util.Calendar finalTime=java.util.Calendar.getInstance();
        if (printDebug) {
            System.out.println(">>> DONE <<<"+" - Final Time: "+finalTime.getTime());
            System.out.println(">>> DEM Characteristics : ");
            System.out.println(">>> DEM Size : Ncols : "+nc+", Nrows : "+nr);
            System.out.println(">>> Min Height : "+DEMstats.minValue+", Max Height : "+DEMstats.maxValue);
            System.out.println(">>> Heights Average : "+DEMstats.meanValue+", Height Std. Deviation : "+DEMstats.standardDeviation);
            System.out.println(">>> Running Time is "+(finalTime.getTimeInMillis()-iniTime.getTimeInMillis())/1000.);
        }
        firstCorrection = false;
        OpProc.set_ready();
    }
    
    
    /**
     * Loops through all the sinks in the DEM and fixes them
     */
    public void corrigeSink_t(){
        int next_pit = get_nextPit();
        
        if (Sink_t.size() != 0) OpProc.setMaxMinExtractionBar((int)((Pit) Sink_t.lastElement()).cota, (int)((Pit) Sink_t.firstElement()).cota);
        
        Pit Sn;
        
        while(next_pit!=-1){
            Sn=(Pit) Sink_t.get(next_pit);
            Sn.corrigePit(this);
            if(convergenceAlarmType1) {
                return;
            }
            next_pit = get_nextPit();
        }
        
        if (Sink_t.size() != 0) OpProc.setValueExtractionBar((int)((Pit) Sink_t.lastElement()).cota);
        
    }
    
    /**
     * Gets the next pit to be fixed
     * @return The sink to be fixed
     */
    public int get_nextPit(){
        
        if (Sink_t.size() == 0) return -1;
            
        java.util.Collections.sort(Sink_t);
        
        Pit Sn = (Pit) Sink_t.get(0);
        boolean sale = true;
        int n;
        for(n=0 ; n<Sink_t.size(); n++){
            Sn = (Pit) Sink_t.get(n);
            if(!Sn.corregido){
                return n;
            }
        }
        return -1;
    }
    
    
    /**
     * Runs the uniPit algorithm for the entire DEM
     */
    public void corrigeUniPitFullDEM(){
        
        if (printDebug) System.out.println("    >>> General UniPit Method" );
        
        int nfila=metaDEM.getNumRows();
        int ncol=metaDEM.getNumCols();

        double min_ady = Double.MAX_VALUE;
        int contAltas = 0;
        int contPlanas=0;
        int contFalt=0;

        for (int i=1; i<=nfila ; i++){
            for (int j=1; j<=ncol; j++){
                min_ady = Double.MAX_VALUE;
                contAltas = 0;
                contPlanas=0;
                contFalt=0;
                
                /*Se busca en las ocho celdas adyacentes y se cuenta con cont el numero de ellas
                 que tienen cota corregida mayor que la de la celda (i,j). Se actualiza el minimo
                 adyacente de (i,j).*/
                
                for (int k=0; k<=8; k++){
                    if (DEM[i+(k/3)-1][j+(k%3)-1]>=0 && k!=4)
                        min_ady = Math.min(min_ady,DEM[i+(k/3)-1][j+(k%3)-1]);
                    if(DEM[i+(k/3)-1][j+(k%3)-1]>DEM[i][j])
                        contAltas++;
                    if(DEM[i+(k/3)-1][j+(k%3)-1]==DEM[i][j])
                        contPlanas++;
                    if(DEM[i+(k/3)-1][j+(k%3)-1]<0)
                        contFalt++;
                }
                /*Si las ocho celdas adyacentes a (i,j) son m�s altas que ella (cont=8), se le asigna
                  la cota de la m�nima adyacente, y queda corregido el pit deuna sola celda.*/
                if (contAltas == 8)
                    DEM[i][j] = min_ady;
                //Esto es para que no hayan zonas planas ni sumideros que toquen los bordes
                //Las celdas que estan en los bordes se ponen mas bajas que la minima adyacente 
                if(DEM[i][j]>0 && contFalt>0){
                    if(contPlanas+contFalt == 9)
                        DEM[i][j]=min_ady*0.9;
                    else{
                        double min_ady_interior = Double.MAX_VALUE;
                        for (int k=0; k<=8; k++){                    
                            if (DEM[i+(k/3)-1][j+(k%3)-1]>0){ 
                                int i2 = i+(k/3)-1; int j2 = j+(k%3)-1;
                                boolean tocaFalt = false;
                                int k2=0;
                                do{
                                    tocaFalt = (DEM[i2+(k2/3)-1][j2+(k2%3)-1]<=0); 
                                    k2++;
                                }while(!tocaFalt && k2<=8);
                                if (!tocaFalt ){
                                    min_ady_interior = Math.min(min_ady_interior,DEM[i2][j2]);
                                }                            
                            }
                        }
                        if(min_ady_interior < Double.MAX_VALUE)
                            DEM[i][j]=min_ady_interior*0.9;
                    }
                }
            }
        }
        if (printDebug) System.out.println("    >>> Done with UniPit Method" );

    }//metodo corrUnipit
    
    
    //Calcula el incremento diferencial para la correccion de zonas planas.
    /**
     * Calculates the increment to be applied when the DEM flat area is being filled
     * @param F A Vector with the flat areas
     * @return The value of the increment
     */
    public double calculaIncr(java.util.Vector F){

        int max_M=0;
        double min_dif_front = 1000.0;
        for(int n=0; n<F.size(); n++){
            Pit Fn = (Pit)F.get(n);
            max_M = Math.max(max_M, Math.max(Fn.Mp.end_col-Fn.Mp.ini_col,Fn.Mp.end_row-Fn.Mp.ini_row));
            if (max_M==0) max_M=2;
            for(int i=Fn.Mp.ini_row; i<=Fn.Mp.end_row; i++){
                for(int j=Fn.Mp.ini_col ; j<=Fn.Mp.end_col; j++){
                    if(DIR[i][j]==11 + Fn.grupo*100){
                        for(int k=0; k<= 8; k++){
                            if (DEM[i+(k/3)-1][j+(k%3)-1] > DEM[i][j] )
                                min_dif_front = Math.min(min_dif_front,DEM[i+(k/3)-1][j+(k%3)-1]-DEM[i][j]) ;
                        }
                    }
                }
            }
        }

        return Math.max(Math.pow(10.0,Math.floor(Math.log(min_dif_front/max_M)/2.30258)- 1 ),1E-10);

    }
    
    /**
     * Writes the .areas, .corrDEM, .slope, an .dir files
     * @param arch The path where the files will be stored
     */
    public void writePREL(java.io.File arch){
        String path = arch.getPath();
        if (printDebug) System.out.println(">>> Writing To :"+arch.getParent());
        String[] destinations ={path.substring(0, path.lastIndexOf(".")) + ".areas",
                                path.substring(0, path.lastIndexOf(".")) + ".corrDEM",
                                path.substring(0, path.lastIndexOf(".")) + ".slope",
                                path.substring(0, path.lastIndexOf(".")) + ".dir"};
                                
        java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
        java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;
        try{
            for(int k=0 ; k<destinations.length ; k++){
                buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(destinations[k]));
                outs[k] = new java.io.DataOutputStream(buffOuts[k]);
            }
            
            int nr=metaDEM.getNumRows();
            int nc=metaDEM.getNumCols();
            
            for (int i=1; i <= nr; i++) for (int j=1; j <= nc ;j++){
                if(DEM[i][j]==-1.0){
                    outs[0].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                    outs[1].writeDouble(new Double(metaDEM.getMissing()).doubleValue());
                    outs[2].writeDouble(new Double(metaDEM.getMissing()).doubleValue());
                    outs[3].writeByte(-10);
                }
                else{
                    outs[0].writeFloat(Areas[i][j]);
                    outs[1].writeDouble(DEM[i][j]);
                    //outs[2].writeDouble(MaxPend[i][j]);
                    outs[3].writeByte((byte)DIR[i][j]);
                }
            }
            for(int k=0 ; k<destinations.length ; k++)
                buffOuts[k].close() ;
        }catch(java.io.IOException e1){
            System.err.println(e1.toString());
        }
    }
    
    /**
     * Writes the .corrDEM file
     * @param arch The path where the files will be stored
     */
    public void writeCorrDem(java.io.File arch){
        int nr=metaDEM.getNumRows();
        int nc=metaDEM.getNumCols();
            
            
        String path = arch.getPath();
        if (printDebug) System.out.println(">>> Writing To :"+arch.getParent());
        String[] destinations ={ path.substring(0, path.lastIndexOf(".")) + ".corrDEM"};
                                
        java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
        java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;
        try{
            for(int k=0 ; k<destinations.length ; k++){
                buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(destinations[k]));
                outs[k] = new java.io.DataOutputStream(buffOuts[k]);
            }
            for (int i=1; i <= nr; i++) for (int j=1; j <= nc ;j++){
                if(DEM[i][j]==-1.0){
                    outs[0].writeDouble(new Double(metaDEM.getMissing()).doubleValue());
                }
                else{
                    outs[0].writeDouble(DEM[i][j]);
                }
            }
            for(int k=0 ; k<destinations.length ; k++)
                buffOuts[k].close() ;
        }catch(java.io.IOException e1){
            System.err.println(e1.toString());
        }
    }
    
    /**
     * Writes the .redRas file
     * @param arch The path where the files will be stored
     */
    public void writeRED(java.io.File arch){
        if (printDebug) System.out.println(">>> Writing Raster Network");
        int nr=metaDEM.getNumRows();
        int nc=metaDEM.getNumCols();
            
        String path = arch.getPath();
        String[] destinations ={path.substring(0, path.lastIndexOf(".")) + ".redRas"};
        java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
        java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;
        try{
            for(int k=0 ; k<destinations.length ; k++){
                buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(destinations[k]));
                outs[k] = new java.io.DataOutputStream(buffOuts[k]);
            }
            for (int i=1; i <= nr; i++) for (int j=1; j <= nc ;j++){
                if (DIR[i][j]==0){
                    outs[0].writeByte(-10);
                }
                else{
                    outs[0].writeByte(RedRas[i][j]);
                }
            }
            for(int k=0 ; k<destinations.length ; k++)
                buffOuts[k].close() ;
        }catch(java.io.IOException e1){System.err.println(e1.toString());}
    }
    
    
    
    /**
     * Writes the .redRas, .ltc, .lcp, .horton, .magn, .dtopo, .tcd, and .mcd files
     * @param arch The path where the files will be stored
     */
    public void writeGEO(java.io.File arch){
        String path = arch.getPath();
        String[] destinations ={
            path.substring(0, path.lastIndexOf(".")) + ".redRas",
            path.substring(0, path.lastIndexOf(".")) + ".ltc",
            path.substring(0, path.lastIndexOf(".")) + ".lcp",
            path.substring(0, path.lastIndexOf(".")) + ".horton",
            path.substring(0, path.lastIndexOf(".")) + ".magn",
            path.substring(0, path.lastIndexOf(".")) + ".dtopo",
            path.substring(0, path.lastIndexOf(".")) + ".tcd",
            path.substring(0, path.lastIndexOf(".")) + ".mcd",};
            
            java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
            java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;
            
            try{
                int nr=metaDEM.getNumRows();
                int nc=metaDEM.getNumCols();
            
            
                for(int k=0 ; k<destinations.length ; k++){
                    buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(destinations[k]));
                    outs[k] = new java.io.DataOutputStream(buffOuts[k]);
                }
                for (int i=1; i <= nr; i++) for (int j=1; j <= nc ;j++){
                    if (DIR[i][j]==0){
                        outs[0].writeByte(-10);
                        outs[1].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                        outs[2].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                        outs[3].writeByte(-10);
                        outs[4].writeInt(new Integer(metaDEM.getMissing()).intValue());
                        outs[5].writeInt(new Integer(metaDEM.getMissing()).intValue());
                        outs[6].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                        outs[7].writeFloat(new Float(metaDEM.getMissing()).floatValue());
                    }
                    else{
                        outs[0].writeByte(RedRas[i][j]);
                        if(RedRas[i][j] >0){
                            outs[1].writeFloat((float)GEO2[i][j].ltc);
                            outs[2].writeFloat((float)GEO2[i][j].lcp);
                            outs[3].writeByte(GEO[i][j].orden);
                            outs[4].writeInt(GEO2[i][j].magn);
                            outs[5].writeInt(GEO2[i][j].d_topo);
                            outs[6].writeFloat((float)GEO2[i][j].tcd);
                            outs[7].writeFloat((float)GEO2[i][j].mcd);
                        } else {
                            outs[1].writeFloat(-1.0f);
                            outs[2].writeFloat(-1.0f);
                            outs[3].writeByte(-1);
                            outs[4].writeInt(-1);
                            outs[5].writeInt(-1);
                            outs[6].writeFloat(-1.0f);
                            outs[7].writeFloat(-1.0f);
                        }
                    }
                }
                for(int k=0 ; k<destinations.length ; k++)
                    buffOuts[k].close() ;
            }catch(java.io.IOException e1){System.err.println(e1.toString());}
            
    }//metodo writeGeomorphCell_1
    
    /**
     * Reads the DEM, direction matrix and upstream area files
     */
    public void leeDEM_DIR_AREA_PEND(){
        if (printDebug) System.out.println(">>> Reading DEM & DIR & AREA & SLOPES");
        Areas = new float[DIR.length][DIR[0].length];
        //MaxPend = new double[DIR.length][DIR[0].length];
        int nr=metaDEM.getNumRows();
        int nc=metaDEM.getNumCols();

        try{
            java.io.FileInputStream files[] = new java.io.FileInputStream[4];
            java.io.BufferedInputStream buffs[] = new java.io.BufferedInputStream[4];
            java.io.DataInputStream datas[] = new java.io.DataInputStream[4];
            String exts[] = {".corrDEM",".dir",".areas",".slope"};
            String path = metaDEM.getLocationBinaryFile().getPath();
            for(int k=0; k<3; k++){
                files[k] = new java.io.FileInputStream(path.substring(0, path.lastIndexOf(".")) + exts[k]);
                buffs[k] = new java.io.BufferedInputStream(files[k]);
                datas[k] = new java.io.DataInputStream(buffs[k]);
            }
            for (int i=1; i <= nr; i++){
                for (int j=1; j <= nc ;j++){
                    DEM[i][j] = datas[0].readDouble();
                    DIR[i][j] = (int) datas[1].readByte();
                    Areas[i][j] = datas[2].readFloat();
                    //MaxPend[i][j] = datas[3].readDouble();
                }
            }
            for(int k=0; k<3; k++){
                buffs[k].close(); datas[k].close();
            }
        }catch(java.io.IOException e){e.printStackTrace();}
    }
    
    /**
     * Reads the raster netowrk file
     */
    public void leeRED(){
        
        if (printDebug) System.out.println(">>> Reading Raster Network");
        
        try{
            int nr=metaDEM.getNumRows();
            int nc=metaDEM.getNumCols();
            
            java.io.FileInputStream files[] = new java.io.FileInputStream[1];
            java.io.BufferedInputStream buffs[] = new java.io.BufferedInputStream[1];
            java.io.DataInputStream datas[] = new java.io.DataInputStream[1];
            String exts[] = {".redRas"};
            String path = metaDEM.getLocationBinaryFile().getPath();
            for(int k=0; k<1; k++){
                files[k] = new java.io.FileInputStream(path.substring(0, path.lastIndexOf(".")) + exts[k]);
                buffs[k] = new java.io.BufferedInputStream(files[k]);
                datas[k] = new java.io.DataInputStream(buffs[k]);
            }
            for (int i=1; i <= nr; i++){
                for (int j=1; j <= nc ;j++){
                    RedRas[i][j] = datas[0].readByte();
                    if(RedRas[i][j] != 1) RedRas[i][j]=0;
                }
            }
            for(int k=0; k<1; k++){
                buffs[k].close(); datas[k].close();
            }
        }catch(java.io.IOException e){
            e.printStackTrace();
        }
    }
    
    
    
    /**
     * Reads the original DEM
     */
    public void leeDEM(){
        try{
            java.io.FileInputStream files[] = new java.io.FileInputStream[1];
            java.io.BufferedInputStream buffs[] = new java.io.BufferedInputStream[1];
            java.io.DataInputStream datas[] = new java.io.DataInputStream[1];
            String exts[] = {".dem"};
            String path = metaDEM.getLocationBinaryFile().getPath();
            
            
            int nr=metaDEM.getNumRows();
            int nc=metaDEM.getNumCols();
            
            for(int k=0; k<1; k++){
                files[k] = new java.io.FileInputStream(path.substring(0, path.lastIndexOf(".")) + exts[k]);
                buffs[k] = new java.io.BufferedInputStream(files[k]);
                datas[k] = new java.io.DataInputStream(buffs[k]);
            }
            if(metaDEM.getFormat().equalsIgnoreCase("Integer")){
                for (int i=1; i <= nr; i++){
                    for (int j=1; j <= nc ;j++){
                        DEM[i][j] = (double) datas[0].readInt();
                    }
                }
            }
            if(metaDEM.getFormat().equalsIgnoreCase("Double")){
                for (int i=1; i <= nr; i++){
                    for (int j=1; j <= nc ;j++){
                        DEM[i][j] = (double) datas[0].readDouble();
                    }
                }
            }
            
            if(metaDEM.getFormat().equalsIgnoreCase("Float")){
                for (int i=1; i <= nr; i++){
                    for (int j=1; j <= nc ;j++){
                        DEM[i][j] = (double) datas[0].readFloat();
                    }
                }
            }
            
            for(int k=0; k<1; k++){
                buffs[k].close(); datas[k].close();
            }
        }catch(java.io.IOException e){
            e.printStackTrace();
        }
    }
    
    
    /**
     * The run method
     */
    public void run(){
        corrigeDEM();
    }
    
    
    /**
     * Various tests for the class
     * @param Arguments The command line arguments
     */
    public static void main(String[] Arguments){
        
        System.out.println("Program starts "+Arguments.length+" "+java.util.Arrays.toString(Arguments));      
 
//System.out.print("nlda");
//        Arguments=new String[] { "//nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs250.metaDEM",
//                                 "/nfsscratch/Users/rmantill/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/USGS/usgs250.dem"};
//
//System.out.print("5meter");
//        Arguments=new String[] { "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/5meters/5meterc1.metaDEM",
//                                 "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/5meters/5meterc1.dem"};
//
//System.out.print("3meter");
//        Arguments=new String[] { "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/3meters/3meterc1.metaDEM",
//                                 "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/3meterc1.dem"};
//
//System.out.print("2meter");
//        Arguments=new String[] { "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/2meterc1.metaDEM",
//                                 "/usr/home/rmantill/CuencasDataBases/ClearCreek/Rasters/Topography/90meters/2meterc1.dem"};

        /*Arguments=new String[] { "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_02/31908796.metaDEM",
                                 "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_02/31908796.dem"};*/
        
        /*Arguments=new String[] { "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_03/12620307.metaDEM",
                                 "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_03/12620307.dem"};*/
        
        /*Arguments=new String[] { "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.metaDEM",
                                 "/hidrosigDataBases/Continental_US_database/Rasters/Topography/Dd_Basins_30_ArcSec/B_11/08975896.dem"};*/
        
        /*Arguments=new String[] { "/hidrosigDataBases/Test_DB/Rasters/Topography/58447060.metaDEM",
                                 "/hidrosigDataBases/Test_DB/Rasters/Topography/58447060.dem"};*/
                                 
        /*Arguments=new String[] { "/home/ricardo/temp/pandoraOptimizer/SpecialCases/caseFlat001.metaDEM",
                                 "/home/ricardo/temp/pandoraOptimizer/SpecialCases/caseFlat001.dem"};*/
                                 
        /*Arguments=new String[] { "/home/ricardo/temp/pandoraOptimizer/SpecialCases/casePit001.metaDEM",
                                 "/home/ricardo/temp/pandoraOptimizer/SpecialCases/casePit001.dem"};*/
        
        /*Arguments=new String[] { "/home/ricardo/temp/pandoraOptimizer/SpecialCases/casePit002.metaDEM",
                                 "/home/ricardo/temp/pandoraOptimizer/SpecialCases/casePit002.dem"};*/
        
//        Arguments=new String[] { "/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/00_Full-Iowa30m/iowa.metaDEM",
//                                 "/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/1_arcSec/00_Full-Iowa30m/iowa.dem"};
        
        try{
            hydroScalingAPI.io.MetaRaster metaRaster1= new hydroScalingAPI.io.MetaRaster(new java.io.File(Arguments[0]));
            metaRaster1.setLocationBinaryFile(new java.io.File(Arguments[1]));
            hydroScalingAPI.io.DataRaster datosRaster = new hydroScalingAPI.io.DataRaster(metaRaster1);
            if(Arguments.length == 2){
                new NetworkExtractionModule(metaRaster1, datosRaster);
            }
            if(Arguments.length > 2){
                System.out.println("Entered the right routine");
                new NetworkExtractionModule(metaRaster1, datosRaster,Arguments);
            }
            System.exit(0);
        } catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
            System.exit(0);
        }

    }
    
    
    
}
