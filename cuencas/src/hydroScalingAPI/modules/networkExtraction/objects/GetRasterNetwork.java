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

/**
 * This class implements a series of mechanisms for Network pruning. The most
 * succesfull one is the Laplace2 method.  It incorporates several criteria for
 * determination of the location of the network.  However the criteria are only
 * valid for high-resolution DEMs (30 m or less).
 * @author Jorge Mario Ramirez
 */
public abstract class GetRasterNetwork extends Object {
    
    
    /**
     * Assigns all to pixels to be part of the network.  This is a good method for low
     * resolution DEMs (250 m or more) since it is almost guarateed that a pixel
     * contains a channel
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void todo_uno(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> All cells are Network");
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if(Proc.DIR[i][j]>0) Proc.RedRas[i][j]=1;
            }
        }
    }
    
    
    /**
     * Applies the Horton order threshold criteria
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void umbral_Area_Ord(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Area-Order Threshold "+Proc.pixPodado);
        boolean critOrd = (Proc.ordenMax < Integer.MAX_VALUE);
        boolean critArea = (Proc.pixPodado > 0);
        double areaPodado=Float.MAX_VALUE;
        int ordPodado=Integer.MIN_VALUE;
        if (critOrd){
            GetRasterNetwork.todo_uno(Proc);
            GetGeomorphologyRAM.getORD(Proc);
            ordPodado = Proc.maxOrder - Proc.ordenMax;
        }
        if(critArea) areaPodado = Proc.pixPodado*Proc.dy*Proc.dxm;
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if ((Proc.Areas[i][j] > areaPodado) || (critOrd && Proc.GEO[i][j].orden > ordPodado))
                    Proc.RedRas[i][j]*=1;
                else
                    Proc.RedRas[i][j]*=0;
            }
        }
    }
    
    
    /**
     * Applies the Area threshold criteria
     * @param pixUmbral The treshold in number of pixels
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void umbral_Area(float pixUmbral, NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Area Threshold");
        double areaPodado = pixUmbral*Proc.dy*Proc.dxm;
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if ((Proc.Areas[i][j] > areaPodado))
                    Proc.RedRas[i][j]=1;
            }
        }
    }
    
    /**
     * Applies the Area Montgomery Threshold criteria
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void umbral_Montgomery(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Montgomery Area Threshold");
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                double areaPodado = Math.pow(10,-3*Proc.MaxPend[i][j]-1);
                //System.out.println(Proc.MaxPend[i][j]+" "+areaPodado);
                if ((Proc.Areas[i][j] > areaPodado))
                    Proc.RedRas[i][j]*=1;
                else
                    Proc.RedRas[i][j]*=0;
            }
        }
        persiga(Proc);
    }
    
    /**
     * Corrects extra intersections at points of change in the river network
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void fixIntersections(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Fixing Intersections");
        
        int[][] fixedDirs=new int[3][3];
        
        int[][] fork={  {0,1,2},
                        {1,2,5},
                        {2,5,8},
                        {3,0,1},
                        {4,4,4},
                        {5,8,7},
                        {6,3,0},
                        {7,6,3},
                        {8,7,6}};
                        
        int[] fixing={2,6,6,2,5,8,4,4,8};
        
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if(Proc.RedRas[i][j]==1){
                    int llegan=0;
                    for (int k=0; k <= 8; k++){
                        if (Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==1 && Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k) llegan++;
                    }
                    if(llegan > 1){
                        //System.out.println("Working on x= "+(j-1)+" y = "+(i-1));
                        for (int k=0; k <= 8; k++){
                            int k1=fork[k][0];
                            int k2=fork[k][1];
                            int k3=fork[k][2];
                            
                            boolean c1=Proc.DIR[i+(k1/3)-1][j+(k1%3)-1]==9-k1;
                            boolean c2=Proc.RedRas[i+(k2/3)-1][j+(k2%3)-1] == 0 && Proc.DIR[i+(k2/3)-1][j+(k2%3)-1]==9-k2;
                            boolean c3=Proc.DIR[i+(k3/3)-1][j+(k3%3)-1]==9-k3;
                            
                            
                            if (c1 && c2 && c3){
                                //System.out.println("    At x= "+(j+(k2%3)-2)+" y = "+(i+(k2/3)-2)+" changed "+Proc.DIR[i+(k2/3)-1][j+(k2%3)-1]+" by "+fixing[Proc.DIR[i+(k2/3)-1][j+(k2%3)-1]-1]);
                                fixedDirs[(k2/3)][(k2%3)]=fixing[Proc.DIR[i+(k2/3)-1][j+(k2%3)-1]-1];
                            } else {
                                //System.out.println("    Nothing at "+(j+(k2%3)-2)+" y = "+(i+(k2/3)-2)+" because "+c1+Proc.DIR[i+(k1/3)-1][j+(k1%3)-1]+c2+Proc.DIR[i+(k2/3)-1][j+(k2%3)-1]+c3+Proc.DIR[i+(k3/3)-1][j+(k3%3)-1]);
                                fixedDirs[(k2/3)][(k2%3)]=Proc.DIR[i+(k2/3)-1][j+(k2%3)-1];
                            }
                        }
                        for (int k=0; k <= 8; k++){
                            Proc.DIR[i+(k/3)-1][j+(k%3)-1]=fixedDirs[(k/3)][(k%3)];
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Removes short streams from the raster network.  This is because external channels
     * that are only one pixel long are usualy errors.
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void cleanShorts(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Cleaning short streams");
        
        //GetGeomorphologyRAM.getORD(Proc);
        byte[][] cleanRedRas=Proc.RedRas.clone();
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if(Proc.RedRas[i][j]==1){
                    
                    int llegan=0;
                    for (int k=0; k <= 8; k++){
                        if (Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==1 && Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k) llegan++;
                    }
                    if(llegan == 0){
                        int iPn = i-1+(Proc.DIR[i][j]-1)/3;
                        int jPn = j-1+(Proc.DIR[i][j]-1)%3;

                        if(Proc.DIR[iPn][jPn] > 0){
                            int llegan2=0;
                            for (int k=0; k <= 8; k++){
                                if (Proc.RedRas[iPn+(k/3)-1][jPn+(k%3)-1]==1 && Proc.DIR[iPn+(k/3)-1][jPn+(k%3)-1]==9-k) llegan2++;
                            }
                            
                            if(llegan2 > 1){
                                cleanRedRas[i][j]=0;
                            }else{
                            
                                int iPm = iPn-1+(Proc.DIR[iPn][jPn]-1)/3;
                                int jPm = jPn-1+(Proc.DIR[iPn][jPn]-1)%3;

                                if(Proc.DIR[iPm][jPm] > 0){
                                    int llegan3=0;
                                    for (int k=0; k <= 8; k++){
                                        if (Proc.RedRas[iPm+(k/3)-1][jPm+(k%3)-1]==1 && Proc.DIR[iPm+(k/3)-1][jPm+(k%3)-1]==9-k) llegan3++;
                                    }
                                    //if(llegan == 0 && Proc.GEO[iPm][jPm].orden > 1){
                                    if(llegan3 > 1){
                                        cleanRedRas[i][j]=0;
                                        cleanRedRas[iPn][jPn]=0;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        Proc.RedRas=cleanRedRas;
    }
    /**
     * Applies the convergence and laplacian criteria
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     * @param m The number of convergent cells required for channel initiation
     */
    public static void laplace2(NetworkExtractionModule Proc, int m){
        if (Proc.printDebug) System.out.println(">>> Calulating Laplacian - 2 "+m);
        for (int i=2; i<Proc.DIR.length-2; i++){
            for (int j=2; j<Proc.DIR[0].length-2; j++){
                
                if(Proc.RedRas[i][j] == 1){
                    int iPn = i-1+(Proc.DIR[i][j]-1)/3;
                    int jPn = j-1+(Proc.DIR[i][j]-1)%3;

                    int llegan=0;
                    int lleganForward=0;

                    for (int k=0; k <= 8; k++){
                        if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k) llegan++;
                        if (Proc.DIR[iPn+(k/3)-1][jPn+(k%3)-1]==9-k) lleganForward++;
                    }
                    
                    double pointLap;
                    
                    if(Proc.DIR[i][j] == 1 || Proc.DIR[i][j] == 3 ||Proc.DIR[i][j] == 7 ||Proc.DIR[i][j] == 9)
                        pointLap=(Proc.DEM[i-1][j]-2*Proc.DEM[i][j]+Proc.DEM[i+1][j])+(Proc.DEM[i][j-1]-2*Proc.DEM[i][j]+Proc.DEM[i][j+1]);
                    else
                        pointLap=(Proc.DEM[i-1][j-1]-2*Proc.DEM[i][j]+Proc.DEM[i+1][j+1])+(Proc.DEM[i-1][j+1]-2*Proc.DEM[i][j]+Proc.DEM[i+1][j-1]);
                    
                    if(llegan>m && lleganForward>m && pointLap > 0) 
                        Proc.RedRas[i][j] *=1;
                    else 
                        Proc.RedRas[i][j] *=0;
                    
                }
                

            }
        }
        persiga(Proc);
    }
    
    private static void persiga(NetworkExtractionModule Proc){
        for (int i=1; i<Proc.DIR.length-1; i++){
            for (int j=1; j<Proc.DIR[0].length-1; j++){
                if(Proc.RedRas[i][j]==1){
                    int iPv=i; int jPv=j;
                    int iPn=i; int jPn=j;
                    do{
                        Proc.RedRas[iPv][jPv]=1;
                        iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
                        jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
                        iPv=iPn; jPv=jPn;
                    }while(Proc.DIR[iPn][jPn]>0 && Proc.RedRas[iPn][jPn]!=1);
                }
            }
        }
    }
    
    private static void persiga(NetworkExtractionModule Proc, int inii, int inij){
        int iPv=inii; int jPv=inij;
        int iPn=inii; int jPn=inij;
        do{
            Proc.RedRas[iPv][jPv]=1;
            iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
            jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
            iPv=iPn; jPv=jPn;
        }while(Proc.DIR[iPn][jPn]>0 && Proc.RedRas[iPn][jPn]!=1);
    }
    
    
    private static void persiga(NetworkExtractionModule Proc, int inii, int inij, int endi, int endj){
        int iPv=inii; int jPv=inij;
        int iPn=inii; int jPn=inij;
        do{
            Proc.RedRas[iPv][jPv]=1;
            iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
            jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
            iPv=iPn; jPv=jPn;
        }while(iPn != endi || jPn != endj);
    }
    
    
    /**
     * Calculates the Area-Slope for points in the DEM to help the user determine
     * criteria for and Area-Slope threshold
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void getArea_Pend(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Calculating Area-Slope");
        //primero pongo ceros donde no hay red en la matriz de direcciones (me ahorra preguntas)
        /*for(int i=0; i<nr+2; i++) for (int j=0; j<nc+2; j++){
            Proc.DIR[i][j]*=Math.abs((Proc.RedRas[i][j]==-10)?0:Proc.RedRas[i][j]);
        } */
        try{
            String ruta=(Proc.metaDEM.getLocationBinaryFile()).getPath();
            java.io.BufferedOutputStream BufAP = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".ap"));
            java.io.DataOutputStream DataAP = new java.io.DataOutputStream(BufAP);
            boolean changeLink,changeDir;
            int ia, ja, iaN, jaN, arroundI, arroundJ;
            
            int nr=Proc.metaDEM.getNumRows();
            int nc=Proc.metaDEM.getNumCols();
            
            for(int i=2; i<nr-2 ; i++){
                for (int j=2; j<nc-2; j++){
                    if (Proc.RedRas[i][j]==1){
                        //informacion sobre el punto especifico
                        int myOrder=Proc.GEO[i][j].orden;
                        boolean source=true;
                        //ciclo que determina si es una fuente o un punto de cambio para perseguirlo
                        for (int k=0; k <= 8; k++){
                            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k && Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==1){
                                source=false;
                            }
                        }
                        if (source || Proc.GEO[i][j].pcambio){
                            ia=i;
                            ja=j;
                            int outOrder=0;
                            boolean cambieLink=false;
                            double cotaini = Proc.DEM[i][j];  double cotafin = 0;  double distLink =0;
                            double areaLink=0;
                            //System.out.println("inicia "+i+" "+j+" "+Proc.DEM[i][j]);
                            do{
                                if (cambieLink){
                                    cambieLink=false;
                                    distLink=0;
                                    cotaini=Proc.DEM[ia][ja];
                                }
                                iaN=ia+((Proc.DIR[ia][ja]-1)/3)-1;
                                jaN=ja+((Proc.DIR[ia][ja]-1)%3)-1;
                                double[] dist = {Proc.dxy[ia],Proc.dy,Proc.dxy[ia],Proc.dx[ia],1,Proc.dx[ia],Proc.dxy[ia],Proc.dy,Proc.dxy[ia]};
                                distLink += (dist[Proc.DIR[ia][ja]-1])*1000;
                                int lleganAca=0;
                                for (int k=0; k <= 8; k++){
                                    arroundI=iaN+(k/3)-1;
                                    arroundJ=jaN+(k%3)-1;
                                    if (arroundI>0 && arroundI < nr && arroundJ>0 && arroundJ < nc){
                                        if (Proc.DIR[iaN+(k/3)-1][jaN+(k%3)-1]==9-k && Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==1){
                                            lleganAca++;
                                        }
                                    }
                                }
                                changeLink=lleganAca>1;
                                outOrder=Proc.GEO[iaN][jaN].orden;
                                if (changeLink || outOrder <= 0){
                                    cotafin=Proc.DEM[iaN][jaN];
                                    areaLink = (Proc.Areas[ia][ja]+(Proc.dx[ia]*Proc.dy)/2)*1000000;
                                    //newfile.write(areaLink+" "+((-cotafin+cotaini)/distLink)+System.getProperty("line.separator"));
                                    
                                    int contllegan=0;
                                    for (int k=0; k <= 8; k++){
                                        if (Proc.DIR[ia+(k/3)-1][ja+(k%3)-1]==9-k){
                                            contllegan++;
                                        }
                                    }
                                    if(contllegan >3)DataAP.writeDouble(-1.0*(double) areaLink);
                                    else DataAP.writeDouble((double) areaLink);
                                    DataAP.writeDouble((double)((-cotafin+cotaini)/distLink));
                                    cambieLink=true;
                                }
                                ia=iaN; ja=jaN;
                            } while (myOrder == outOrder);
                        }
                    }
                }
            }
            BufAP.close();
            //newfile.close(); bufferout.close();
        }catch(java.io.IOException e){System.err.println(e.toString());}
    }
    
    
    
    /**
     * Bins Areas and Slopes to facilitate the data visualization
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     * @return The simplified array of areas and slopes
     */
    public static double[][] calculaPromAP(NetworkExtractionModule Proc){
        Valor[] areaPend = new Valor[0];
        double minPend = Double.MAX_VALUE;
        double[][] arregloAP;
        boolean todos=false;
        try{
            if (Proc.printDebug) System.out.println(">>> Reading *.ap");
            String ruta=(Proc.metaDEM.getLocationBinaryFile()).getPath();
            java.io.File FileAP = new java.io.File(ruta.substring(0, ruta.lastIndexOf(".")) + ".ap");
            java.io.BufferedInputStream BufAP = new java.io.BufferedInputStream(new java.io.FileInputStream(FileAP));
            java.io.DataInputStream DataAP = new java.io.DataInputStream(BufAP);
            if(Proc.npuntosAP > FileAP.length()/32){
                Object [] tmp = new Object[]{"Accept"};
                javax.swing.JOptionPane.showOptionDialog((java.awt.Component)Proc.parent, "Minimum number of points to get the average is " + (FileAP.length()/32)+", this value will be used for calculations" , "Error",javax.swing.JOptionPane.DEFAULT_OPTION, javax.swing.JOptionPane.ERROR_MESSAGE, null,tmp,tmp[0]);
                Proc.npuntosAP = (int) FileAP.length()/32;
                todos = true;
            }
            if(Proc.npuntosAP <=0) todos = true;
            areaPend = new Valor[(int) FileAP.length()/16];
            for(int k=0; k<areaPend.length; k++){
                areaPend[k]=new Valor(DataAP.readDouble(),DataAP.readDouble());
                if(areaPend[k].pendiente >0)
                    minPend = Math.min(areaPend[k].pendiente,minPend);
            }
        }catch(java.io.IOException e){e.printStackTrace(); System.exit(0);}
        
        if(!todos){
            java.util.Arrays.sort(areaPend);
            double areaProm=0; double pendProm=0;
            int nValores = areaPend.length/Proc.npuntosAP;
            if (Proc.printDebug) {
                System.out.println("areaPend.length "+areaPend.length);
                System.out.println("(Proc.npuntosAP-1) "+(Proc.npuntosAP-1));
                System.out.println("nValores "+nValores);
            }
            arregloAP = new double[2][nValores];
            int contK =0; boolean cambio_signo = false;
            int cont_neg=0;
            for(int k=0; k < areaPend.length-(areaPend.length%nValores); k++){
                if(areaPend[k].area > 0){
                    areaProm += areaPend[k].area;
                }
                else{
                    areaProm += areaPend[k].area;
                    cont_neg++;
                    if(!cambio_signo && areaPend[k+1].area > 0)
                        cambio_signo=true;
                }
                pendProm += areaPend[k].pendiente;
                if((k+1) % Proc.npuntosAP == 0 || cambio_signo){
                    try{
                        if(cambio_signo){
                            if((k+1) % Proc.npuntosAP != 0){ //Promedio en el lado negativo
                                arregloAP[0][contK] = (areaProm/Math.abs(areaProm))*Math.log(Math.abs(areaProm)/(cont_neg%Proc.npuntosAP));
                                arregloAP[1][contK] = Math.log(Math.max(pendProm,minPend)/(cont_neg%Proc.npuntosAP));
                                k += (Proc.npuntosAP - cont_neg%Proc.npuntosAP);
                            }
                            else{
                                arregloAP[0][contK] = (areaProm/Math.abs(areaProm))*Math.log(Math.abs(areaProm)/Proc.npuntosAP);
                                arregloAP[1][contK] = Math.log(Math.max(pendProm,minPend)/Proc.npuntosAP);
                            }
                            cambio_signo = false;
                        }
                        else{
                            arregloAP[0][contK] = (areaProm/Math.abs(areaProm))*Math.log(Math.abs(areaProm)/Proc.npuntosAP);
                            arregloAP[1][contK] = Math.log(Math.max(pendProm,minPend)/Proc.npuntosAP);
                        }
                    }catch(ArrayIndexOutOfBoundsException e){System.err.println("ArrayIndex "+ contK);}
                    contK++;
                    areaProm=0; pendProm=0;
                }
            }
            //System.exit(0);
        }
        else{
            java.util.Arrays.sort(areaPend);
            arregloAP = new double[2][areaPend.length];
            for(int k=0; k<areaPend.length; k++){
                if(areaPend[k].area < 0) arregloAP[0][k] = -1.0*Math.log(-1.0*areaPend[k].area);
                else arregloAP[0][k] = Math.log(areaPend[k].area);
                arregloAP[1][k] = Math.log(Math.max(areaPend[k].pendiente,minPend));
            }
        }
        return arregloAP;
    }
    
    
    /**
     * Calculates the Area-Slope analysis over the blue lines
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void getArea_Pend_Dif(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Calculating Area-Slope Relation over Blue Lines");
        int nr=Proc.metaDEM.getNumRows();
        int nc=Proc.metaDEM.getNumCols();
        try{
            String ruta=(Proc.metaDEM.getLocationBinaryFile()).getPath();
            java.io.BufferedOutputStream BufAP = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".ap"));
            java.io.DataOutputStream DataAP = new java.io.DataOutputStream(BufAP);
            boolean changeLink,changeDir;
            int ia, ja, iaN, jaN, arroundI, arroundJ;
            for(int i=2; i<nr-2 ; i++){
                for (int j=2; j<nc-2; j++){
                    if (Proc.DIR[i][j]>0){
                        //informacion sobre el punto especifico
                        int myOrder=Proc.GEO[i][j].orden;
                        boolean source=true;
                        //ciclo que determina si es una fuente o un punto de cambio para perseguirlo
                        for (int k=0; k <= 8; k++){
                            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k){
                                source=false;
                            }
                        }
                        if (source || Proc.GEO[i][j].pcambio){
                            ia=i;
                            ja=j;
                            int outOrder=0;
                            boolean cambieLink=false;
                            double cotaini = Proc.DEM[i][j];  double cotafin = 0;  double distLink =0;
                            double areaLink=0;
                            //System.out.println("inicia "+i+" "+j+" "+Proc.DEM[i][j]);
                            do{
                                if (cambieLink){
                                    cambieLink=false;
                                    distLink=0;
                                    cotaini=Proc.DEM[ia][ja];
                                }
                                iaN=ia+((Proc.DIR[ia][ja]-1)/3)-1;
                                jaN=ja+((Proc.DIR[ia][ja]-1)%3)-1;
                                double[] dist = {Proc.dxy[ia],Proc.dy,Proc.dxy[ia],Proc.dx[ia],1,Proc.dx[ia],Proc.dxy[ia],Proc.dy,Proc.dxy[ia]};
                                distLink += (dist[Proc.DIR[ia][ja]-1])*1000;
                                int lleganAca=0;
                                for (int k=0; k <= 8; k++){
                                    arroundI=iaN+(k/3)-1;
                                    arroundJ=jaN+(k%3)-1;
                                    if (arroundI>0 && arroundI < nr && arroundJ>0 && arroundJ < nc){
                                        if (Proc.DIR[iaN+(k/3)-1][jaN+(k%3)-1]==9-k){
                                            lleganAca++;
                                        }
                                    }
                                }
                                changeLink=lleganAca>1;
                                outOrder=Proc.GEO[iaN][jaN].orden;
                                if (changeLink || outOrder <= 0){
                                    cotafin=Proc.DEM[iaN][jaN];
                                    areaLink = (Proc.Areas[ia][ja]+(Proc.dx[ia]*Proc.dy)/2)*1000000;
                                    //newfile.write(areaLink+" "+((-cotafin+cotaini)/distLink)+System.getProperty("line.separator"));
                                    /*A los que son de la red se les pone el �rea negativa para colocarles otro color en GraficaAP */
                                    //if(Proc.DEM[iaN][jaN]>2400){
                                    if(Proc.RedRas[iaN][jaN]==1 && Proc.RedRas[ia][ja]==1) DataAP.writeDouble((double)-1.0 * areaLink);
                                    else DataAP.writeDouble((double)areaLink);
                                    DataAP.writeDouble((double)((-cotafin+cotaini)/distLink));
                                    //}
                                    cambieLink=true;
                                }
                                ia=iaN; ja=jaN;
                            } while (myOrder == outOrder);
                        }
                    }
                }
            }
            BufAP.close();
            //newfile.close(); bufferout.close();
        }catch(java.io.IOException e){System.err.println(e.toString());}
    }
    
    
    private static double[][] getAP_Puntos(NetworkExtractionModule Proc, int c){
        int nr=Proc.metaDEM.getNumRows();
        int nc=Proc.metaDEM.getNumCols();
        java.util.Vector vectorAP = new java.util.Vector();
        for(int i=0; i<nr; i++){
            for(int j=0; j<nc; j++){
                if (Proc.RedRas[i][j]>0 && Proc.DIR[i][j]>0){
                    boolean llegan=false;
                    int llenos =0;
                    java.util.Vector vac = new java.util.Vector(0,1);
                    loopK:
                        for (int k=0; k <= 8; k++){
                            if (Proc.RedRas[i+(k/3)-1][j+(k%3)-1]>0 && Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k){
                                llegan=true;
                            }
                            if (!llegan &&  Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==0)
                                vac.addElement(new Integer(k));
                            if(llegan) break loopK;
                        }
                        if(!llegan && vac.size()>5 && vac.size()<=7){
                            int herrad[][] ={{0,1,3,6,7},{0,1,2,3,5},{1,2,5,7,8},{3,5,6,7,8}};
                            boolean esta=true;
                            LoopG:
                                for(int g1=0; g1<=3; g1++){
                                    esta=true;
                                    for(int g2=0; g2<=4; g2++){
                                        esta = esta && vac.contains(new Integer(herrad[g1][g2]));
                                    }
                                    if (esta) break LoopG;
                                }
                                if(esta){
                                    int cont=0;
                                    int iPv = i; int jPv = j;
                                    int iPn=iPv,jPn=jPv;
                                    do{
                                        iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
                                        jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
                                        cont++;
                                        iPv=iPn; jPv=jPn;
                                        if(!(iPv>1 && iPv<nr && jPv>1 && jPv<nc))
                                            cont = c;
                                    }while(cont < c);
                                    //System.exit(0);
                                    if(iPv>1 && iPv<nr && jPv>1 && jPv<nc){
                                        //if(Proc.Areas[iPv][jPv]>0 && Proc.MaxPend[iPv][jPv]>0){
                                        vectorAP.add(new Valor((double) 1000000*Proc.Areas[iPv][jPv],Proc.MaxPend[iPv][jPv]));
                                    }
                                    //System.out.println(iPv+" "+jPv+" "+Proc.Areas[iPv][jPv]+" "+Proc.MaxPend[iPv][jPv]);
                                }
                        }
                }
            }
        }
        double dataAP[][] = new double[2][vectorAP.size()];
        for(int k=0; k<vectorAP.size(); k++){
            dataAP[0][k]= Math.log(((Valor) vectorAP.get(k)).area);
            dataAP[1][k]= Math.log(((Valor) vectorAP.get(k)).pendiente);
        }
        return dataAP;
    }
    
    
    private static double[][] getAP_Fuentes(NetworkExtractionModule Proc){
        int nr=Proc.metaDEM.getNumRows();
        int nc=Proc.metaDEM.getNumCols();
        java.util.Vector vectorAP = new java.util.Vector();
        for(int i=0; i<nr; i++){
            for(int j=0; j<nc; j++){
                if (Proc.RedRas[i][j]>0 && Proc.DIR[i][j]>0){
                    boolean llegan=false;
                    int llenos =0;
                    java.util.Vector vac = new java.util.Vector(0,1);
                    loopK:
                        for (int k=0; k <= 8; k++){
                            if (Proc.RedRas[i+(k/3)-1][j+(k%3)-1]>0 && Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k){
                                llegan=true;
                            }
                            if (!llegan &&  Proc.RedRas[i+(k/3)-1][j+(k%3)-1]==0)
                                vac.addElement(new Integer(k));
                            if(llegan) break loopK;
                        }
                        if(!llegan && vac.size()>5 && vac.size()<=7){
                            int herrad[][] ={{0,1,3,6,7},{0,1,2,3,5},{1,2,5,7,8},{3,5,6,7,8}};
                            boolean esta=true;
                            LoopG:
                                for(int g1=0; g1<=3; g1++){
                                    esta=true;
                                    for(int g2=0; g2<=4; g2++){
                                        esta = esta && vac.contains(new Integer(herrad[g1][g2]));
                                    }
                                    if (esta) break LoopG;
                                }
                                if(esta){
                                    boolean salio = false;
                                    int iPv = i , jPv = j, iPn = iPv , jPn = jPv;
                                    int iConv = -1, jConv=-1, contlleganRed=0;
                                    double distFuente =0.0; double distFuenteConv =0.0;
                                    do{
                                        iPn = iPv-1+(Proc.DIR[iPv][jPv]-1)/3;
                                        jPn = jPv-1+(Proc.DIR[iPv][jPv]-1)%3;
                                        int contllegan =0;
                                        for (int k=0; k <= 8; k++){
                                            if (Proc.DIR[iPn+(k/3)-1][jPn+(k%3)-1]==9-k){
                                                contllegan++;
                                            }
                                        }
                                        if(iConv < 0 && contllegan>=3){
                                            iConv=iPv;
                                            jConv = jPv;
                                        }
                                        double[] dist = {Proc.dxy[iPv],Proc.dy,Proc.dxy[iPv],Proc.dx[iPv],1,Proc.dx[iPv],Proc.dxy[iPv],Proc.dy,Proc.dxy[iPv]};
                                        distFuente += (dist[Proc.DIR[iPv][jPv]-1])*1000;
                                        if(iConv>=0) distFuenteConv += (dist[Proc.DIR[iPv][jPv]-1])*1000;
                                        
                                        contlleganRed=0;
                                        for (int k=0; k <= 8; k++){
                                            if (Proc.DIR[iPv+(k/3)-1][jPv+(k%3)-1]==9-k && Proc.RedRas[iPv+(k/3)-1][jPv+(k%3)-1]==1){
                                                contlleganRed++;
                                            }
                                        }
                                        if(!(iPn>1 && iPn<nr && jPn>1 && jPn<nc))
                                            salio=true;
                                        iPv=iPn; jPv=jPn;
                                    }while(contlleganRed<=1 && !salio);
                                    if(iConv < 0){ iConv=i; jConv=j; distFuenteConv = distFuente; }
                                    if(iPv>1 && iPv<nr && jPv>1 && jPv<nc){
                                        vectorAP.add(new Valor((double) 1000000*Proc.Areas[iConv][jConv],(Proc.DEM[iConv][jConv]-Proc.DEM[iPn][jPn])/distFuenteConv));
                                    }
                                }
                        }
                }
            }
        }
        double dataAP[][] = new double[2][vectorAP.size()];
        for(int k=0; k<vectorAP.size(); k++){
            dataAP[0][k]= Math.log(((Valor) vectorAP.get(k)).area);
            dataAP[1][k]= Math.log(((Valor) vectorAP.get(k)).pendiente);
        }
        return dataAP;
    }
    
    
    /**
     * Applies an Area-Slope threshold
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void umbralASalfa(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println("UMBRAL ASalfa>C");
        int nr=Proc.metaDEM.getNumRows();
        int nc=Proc.metaDEM.getNumCols();
        //primero pongo ceros donde no hay red en la matriz de direcciones (me ahorra preguntas)
        for(int i=0; i<nr+2; i++) for (int j=0; j<nc+2; j++){
            Proc.DIR[i][j]*=Math.abs((Proc.RedRas[i][j]==-10)?0:Proc.RedRas[i][j]);
        }
        boolean changeLink,changeDir;
        int ia, ja, iaN, jaN, arroundI, arroundJ;
        double areaPodado =0;
        java.util.Vector fuentes = new java.util.Vector();
        for(int i=2; i<nr-2 ; i++){
            for (int j=2; j<nc-2; j++){
                if (Proc.RedRas[i][j]==1){
                    //informacion sobre el punto especifico
                    int myOrder=Proc.GEO[i][j].orden;
                    boolean source=true;
                    //ciclo que determina si es una fuente o un punto de cambio para perseguirlo
                    for (int k=0; k <= 8; k++){
                        if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k){
                            source=false;
                        }
                    }
                    if (source || Proc.GEO[i][j].pcambio){
                        ia=i;
                        ja=j;
                        int outOrder=0;
                        boolean cambieLink=false;
                        double cotaini = Proc.DEM[i][j];  double cotafin = 0;  double distLink =0;
                        double areaLink=0;
                        do{
                            if (cambieLink){
                                cambieLink=false;
                                distLink=0;
                                cotaini=Proc.DEM[ia][ja];
                            }
                            iaN=ia+((Proc.DIR[ia][ja]-1)/3)-1;
                            jaN=ja+((Proc.DIR[ia][ja]-1)%3)-1;
                            double[] dist = {Proc.dxy[ia],Proc.dy,Proc.dxy[ia],Proc.dx[ia],1,Proc.dx[ia],Proc.dxy[ia],Proc.dy,Proc.dxy[ia]};
                            distLink += (dist[Proc.DIR[ia][ja]-1])*1000;
                            int lleganAca=0;
                            for (int k=0; k <= 8; k++){
                                arroundI=iaN+(k/3)-1;
                                arroundJ=jaN+(k%3)-1;
                                if (arroundI>0 && arroundI < nr && arroundJ>0 && arroundJ < nc){
                                    if (Proc.DIR[iaN+(k/3)-1][jaN+(k%3)-1]==9-k){
                                        lleganAca++;
                                    }
                                }
                            }
                            changeLink=lleganAca>1;
                            outOrder=Proc.GEO[iaN][jaN].orden;
                            if (changeLink || outOrder <= 0){
                                cotafin=Proc.DEM[iaN][jaN];
                                areaLink = (Proc.Areas[ia][ja]+(Proc.dx[ia]*Proc.dy)/2)*1000000;
                                areaPodado = Proc.pixPodado*Proc.dy*Proc.dx[iaN]*1000000;
                                //CRITERIO ASalfa >= C :
                                if(areaLink >= areaPodado && areaLink*Math.pow((-cotafin+cotaini)/distLink,(double)Proc.alfa) >= (double)Proc.C){
                                    fuentes.add(new Integer(ia*nc + ja));
                                    //persiga(Proc,ia,ja,iaN,jaN);
                                }
                                cambieLink=true;
                            }
                            ia=iaN; ja=jaN;
                        } while (myOrder == outOrder);
                    }
                }
            }
        }
        Proc.RedRas = new byte[Proc.DIR.length][Proc.DIR[0].length];
        if(fuentes.size()>0){
            for(int k=0; k<fuentes.size(); k++){
                int valor = new Integer(fuentes.get(k).toString()).intValue();
                persiga(Proc,valor/nc,valor%nc);
            }
        }
        
    }
    
    
    private static void probabCh_Ini(NetworkExtractionModule Proc){
        //Constantes
        double densAgua =1, densSuelo = 2.6;
        double gravedad = 9.8;
        double m1 = 0.6, n1 = 0.7;
        double tao_est = 0.045, k = 0.0474 , p = 1/6;
        double r_min = 40, r_max = 55;
        double na_min = 0.015, na_max = 0.1;
        double d50_media = 2, d50_de = 0.48;
        double tao_c, nb, d50, r, na;
        //double C[][] = new double[nr][nc];
        java.util.Random random_r = new java.util.Random(10001313);
        java.util.Random random_na = new java.util.Random(10401313);
        java.util.Random random_d50 = new java.util.Random(10601313);
        //for(int i=1; i<=nr ; i++){
        //for (int j=1; j<=nc; j++){
        for(int i=50; i<=55; i++){
            for (int j=50; j<=55; j++){
                r = (1d/(1000*24*3600d))*((random_r.nextDouble()*(r_max-r_min))+r_min);
                if (Proc.printDebug) System.out.println("r "+r);
                na = (random_na.nextDouble()*(na_max-na_min))+na_min;
                if (Proc.printDebug) System.out.println("na "+na);
                d50 = Math.exp(Math.log(d50_media)+((random_d50.nextGaussian())*Math.log(d50_de)));
                if (Proc.printDebug) System.out.println("d50 "+d50);
                tao_c = tao_est*gravedad*(densSuelo-densAgua)*d50;
                if (Proc.printDebug) System.out.println("tao_c "+tao_c);
                nb = k*Math.pow(d50/1000,p);
                if (Proc.printDebug) System.out.println("nb "+nb);
                Proc.DEM[i][j] = Math.pow(tao_c/(densAgua*gravedad*Math.pow(nb,1.5)*Math.pow(nb+na,-0.9)*Math.pow(r,m1)),1/m1);
                if (Proc.printDebug) System.out.println("C "+Proc.DEM[i][j]);
                if (Proc.printDebug) System.out.println("-----------------------------------");
            }
        }
        System.exit(0);
    }
    
}

class Valor extends Object implements Comparable{
    public double area,pendiente;
    
    Valor(double a,double p){
        area = a;
        pendiente = p;
    }
    
    public int compareTo(java.lang.Object val) {
        return (new Double(area)).compareTo(new Double(((Valor)val).area));
    }
}
