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
 * This class implements the downstream-travel algorithm over the network to
 * calculate important netwotk features (geomorphical and topological) for all locations
 * in the DEM.  All the methods in this class are static which makes this class a
 * toolbox that can be extended to include new analysis
 * @author Jorge Mario Ramirez and Ricardo Mantilla
 */
public abstract class GetGeomorphologyRAM extends Object {
    
    private static int[][] toMark;
    
    /**
     * Uses the corrected DEM and the direction matrix to determine the steepest
     * gradient
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void getMaxPend(hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Calculating Slopes");
        if (Proc.MaxPend == null) Proc.MaxPend = new double[Proc.DIR.length][Proc.DIR[0].length];
        for (int i=1; i <= Proc.DIR.length-2 ; i++){
            double[] dist = {Proc.dxy[i],Proc.dy,Proc.dxy[i],Proc.dx[i],1,Proc.dx[i],Proc.dxy[i],Proc.dy,Proc.dxy[i]};
            for (int j=1 ; j <= Proc.DIR[0].length-2; j++){
                int dir = Proc.DIR[i][j];
                if(dir>0){
                    try{
                        Proc.MaxPend[i][j]=(Proc.DEM[i][j]-Proc.DEM[i-1+(dir-1)/3][j-1+(dir-1)%3])/(1000.0*dist[dir-1]);
                    }catch(ArrayIndexOutOfBoundsException e){
                        System.out.println("error "+i+" "+j+" "+dir);
                    }
                }else Proc.MaxPend[i][j]=new Double(Proc.metaDEM.getMissing()).doubleValue();
            }
        }
    }
    
    /**
     * Calculates the upstream area for each pixel in the DEM
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void getAreas(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Calculating Areas");
        int c=0;
        Proc.Areas = new float[Proc.DIR.length][Proc.DIR[0].length];
        int red[][] = new int[Proc.DIR.length][Proc.DIR[0].length];
        for (int i=1; i <= Proc.DIR.length-2 ; i++){
            for (int j=1 ; j <= Proc.DIR[0].length-2; j++){
                if (Proc.DIR[i][j]>0){
                    int llegan=0;
                    for (int k=0; k <= 8; k++){
                        if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k)
                            llegan++;
                    }
                    red[i][j] = llegan;
                }
                else{
                    red[i][j] = -3;
                    Proc.Areas[i][j] = -1.0f;
                }
            }
        }
        int contn; int contp;
        DO:
            do{
                contn= 0;
                for (int i=1; i <= Proc.DIR.length - 2 ; i++){
                    for (int j=1 ; j <= Proc.DIR[0].length - 2; j++){
                        if (red[i][j]<0) contn++;
                        if (red[i][j]==0){
                            Proc.Areas[i][j] += (float)Proc.dx[i]*Proc.dy;
                            red[i][j]=-1;
                            int x1=i-1+(Proc.DIR[i][j]-1)/3 ; int y1=j-1+(Proc.DIR[i][j]-1)%3;
                            try{
                                if (Proc.DIR[x1][y1]>0){
                                    red[x1][y1]--;
                                    Proc.Areas[x1][y1] += Proc.Areas[i][j];
                                }
                            }catch(ArrayIndexOutOfBoundsException e){System.err.println("error "+i+" "+j+" "+Proc.DIR[i][j]+" "+x1+" "+y1);}
                        }
                    }
                }
//                int yetToDo=(((Proc.DIR.length-1-2+1)*(Proc.DIR[0].length-1-2+1))-contn);
//                System.out.println(">>> >>> Points to visit: "+yetToDo);
//                
//                if(yetToDo <50){
//                    for (int i=1; i <= Proc.DIR.length - 2 ; i++){
//                        for (int j=1 ; j <= Proc.DIR[0].length - 2; j++){
//                            if (red[i][j]>=0) {
//                                System.out.println(">>> >>> >>> Propagating: "+j+" "+i+" Current upstream area is: "+Proc.Areas[i][j]);
//                            }
//                        }
//                    }
//                }
                
            }while(contn < (Proc.DIR.length-1-2+1)*(Proc.DIR[0].length-1-2+1) );
    }
    
    /**
     * Calculates the direction of the DEM borders and the directions of the pixels
     * surrounding a lake or ocean
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void bordes(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Calculating Borders");
        for (int i=2; i <= Proc.DIR.length-3; i++){
            for (int j=2; j <= Proc.DIR[0].length-3 ;j++){
                if(Proc.DEM[i][j]>0 && Proc.DIR[i][j]==0){
                    double minc = Proc.DEM[i][j];
                    int ds=0;
                    int dll=0;
                    int dsale=0;
                    float areall=0;
                    float areat=0;
                    boolean inercia=false;
                    for(int k=0; k <= 8; k++){
                        //Si ninguno es mas bajo, sigue la direccion del de mayor area que le llegue a el cuando eso lo saque al faltante
                        if(Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k){
                            areat += Proc.Areas[i+(k/3)-1][j+(k%3)-1];
                            if(Proc.DEM[i-1+(Proc.DIR[i+(k/3)-1][j+(k%3)-1]-1)/3][j-1+(Proc.DIR[i+(k/3)-1][j+(k%3)-1]-1)%3]<0 && Proc.Areas[i+(k/3)-1][j+(k%3)-1] > areall){
                                areall = Proc.Areas[i+(k/3)-1][j+(k%3)-1];
                                dll=Proc.DIR[i+(k/3)-1][j+(k%3)-1];
                                inercia=true;
                            }
                        }
                        if(Proc.DEM[i+(k/3)-1][j+(k%3)-1]<0) dsale=k;
                    }
                    if (inercia) Proc.DIR[i][j] = dll;
                    //Si ninguno le llega, drena hacia el faltante por azar
                    else Proc.DIR[i][j] = dsale+1 ;
                    if(areat>0) Proc.Areas[i][j]= (float) (areat + Proc.dx[i]*Proc.dy);
                    else Proc.Areas[i][j]= (float)(Proc.dx[i]*Proc.dy);
                }
            }
        }
    }
    
    /**
     * Calculates the Horton-Strahler order for every location in the DEM
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void getORD(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Calculating Horton Orders");
        Proc.GEO = new GeomorphCell_1[Proc.DIR.length][Proc.DIR[0].length];
        for (int i=1; i < Proc.DIR.length - 1 ; i++){
            for (int j=1 ; j < Proc.DIR[0].length - 1; j++){
                int llegan = 0;
                for (int k=0; k <= 8; k++){
                    if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k && Proc.RedRas[i+(k/3)-1][j+(k%3)-1]>0)
                        llegan++;
                }
                if (Proc.DIR[i][j]>0)  Proc.GEO[i][j] = new GeomorphCell_1(llegan);
                else{
                    Proc.GEO[i][j]= new GeomorphCell_1(-3);
                    Proc.GEO[i][j].orden=-1;
                }
                if (Proc.RedRas[i][j]==0 && Proc.DIR[i][j]>0){
                    Proc.GEO[i][j] = new GeomorphCell_1(-2);
                }
                Proc.GEO[i][j].llegan_red = llegan;
            }//for j
        }//for i
        int contn;
        do{
            contn = 0;
            for(int i=2; i<=Proc.DIR.length-3 ; i++){
                for (int j=2; j<=Proc.DIR[0].length-3; j++){
                    if (Proc.GEO[i][j].status < 0)
                        contn++;
                    if (Proc.GEO[i][j].status == 0){
                        ORDcero(Proc,i,j,contn);
                    }
                }//for j
            }//for i
        }while(contn < (Proc.DIR.length-3-2+1)*(Proc.DIR[0].length-3-2+1) );
    }//metodo
    
    private static void ORDcero(NetworkExtractionModule Proc, int i, int j,int contn){
        Proc.GEO[i][j].status = -1;
        if (Proc.GEO[i][j].llegan_red == 0){
            Proc.GEO[i][j].orden = 1;
        }
        int x1=i-1+(Proc.DIR[i][j]-1)/3 ; int y1=j-1+(Proc.DIR[i][j]-1)%3;
        if (Proc.GEO[x1][y1].status >= 0 ){
            Proc.GEO[x1][y1].status--  ;
        }
        if(Proc.GEO[i][j].llegan_red > 0){
            java.util.Vector ord = new java.util.Vector();
            for (int k=0; k <= 8; k++){
                if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k && Proc.RedRas[i+(k/3)-1][j+(k%3)-1]>0){
                    ord.addElement(new Integer(Proc.GEO[i+(k/3)-1][j+(k%3)-1].orden));
                }
            }
            java.util.Collections.sort(ord);
            if (ord.size()>1){
                if(ord.get(ord.size()-1).equals(ord.get(ord.size()-2))){
                    Proc.GEO[i][j].orden = 1 + new Integer(ord.get(ord.size()-1).toString()).intValue();
                    Proc.GEO[i][j].pcambio = true;
                }
                else Proc.GEO[i][j].orden = new Integer(ord.get(ord.size()-1).toString()).intValue();
            }
            if (ord.size()==1)
                Proc.GEO[i][j].orden = new Integer(ord.get(0).toString()).intValue();
            Proc.maxOrder = Math.max(Proc.GEO[i][j].orden,Proc.maxOrder);
        }
        if (Proc.GEO[x1][y1].llegan_red==1 && Proc.DIR[x1][y1]>0){
            ORDcero(Proc,x1,y1,contn);
        }
    }
    
    
    
    /**
     * Calculates several geomorphical and topological features described by the {@link
     * hydroScalingAPI.modules.networkExtraction.objects.GeomorphCell_2} cell.
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void getGEO(NetworkExtractionModule Proc){
        if (Proc.printDebug) System.out.println(">>> Calculating Geomorphology");
        Proc.GEO = new GeomorphCell_1[Proc.DIR.length][Proc.DIR[0].length];
        Proc.GEO2 = new GeomorphCell_2[Proc.DIR.length][Proc.DIR[0].length];
        for (int i=1; i < Proc.DIR.length - 1 ; i++){
            for (int j=1 ; j < Proc.DIR[0].length - 1; j++){
                int llegan = 0;
                for (int k=0; k <= 8; k++){
                    if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k && Proc.RedRas[i+(k/3)-1][j+(k%3)-1]>0)
                        llegan++;
                }
                
                if (Proc.RedRas[i][j]>0 && Proc.DIR[i][j]>0) {
                    Proc.GEO2[i][j]= new GeomorphCell_2();
                    Proc.GEO[i][j] = new GeomorphCell_1(llegan);
                    Proc.GEO[i][j].llegan_red = llegan;
                }
            }
        }
        
        int contn;
        do{
            contn = 0;
            for(int i=2; i<=Proc.DIR.length-3 ; i++){
                for (int j=2; j<=Proc.DIR[0].length-3; j++){
                    if (Proc.GEO[i][j] == null)
                        contn++;
                    else {
                        if (Proc.GEO[i][j].status < 0)
                            contn++;
                        if (Proc.GEO[i][j].status == 0)
                            GEOcero(i,j,contn,Proc);
                    }
                        
                }
            }
        }while(contn < (Proc.DIR.length-3-2+1)*(Proc.DIR[0].length-3-2+1));
    }
    
    private static void GEOcero(int i, int j,int contn,NetworkExtractionModule Proc){
        //System.out.println("error "+i+" "+j);
        double[] dist = {Proc.dxy[i],Proc.dy,Proc.dxy[i],Proc.dx[i],1,Proc.dx[i],Proc.dxy[i],Proc.dy,Proc.dxy[i]};
        Proc.GEO[i][j].status = -1;
        
        Proc.GEO2[i][j].ltc += dist[Proc.DIR[i][j]-1];
        Proc.GEO2[i][j].lcp += dist[Proc.DIR[i][j]-1];
        
        if (Proc.GEO[i][j].llegan_red == 0){
            Proc.GEO[i][j].orden = 1;
            Proc.GEO2[i][j].magn = 1;
            Proc.GEO2[i][j].d_topo = 1;
            Proc.GEO2[i][j].tcd=0.0;
            Proc.GEO2[i][j].mcd=0.0;
        }
        
        int x1=i-1+(Proc.DIR[i][j]-1)/3 ; 
        int y1=j-1+(Proc.DIR[i][j]-1)%3;
        
        if (Proc.GEO[x1][y1] != null && Proc.GEO[x1][y1].status >= 0 ){
            Proc.GEO[x1][y1].status--  ;
            if (Proc.GEO[i][j].llegan_red == 0)
                Proc.GEO2[x1][y1].magn ++;
            else 
                Proc.GEO2[x1][y1].magn += Proc.GEO2[i][j].magn;
            Proc.GEO2[x1][y1].ltc += Proc.GEO2[i][j].ltc;
            Proc.GEO2[x1][y1].lcp = Math.max(Proc.GEO2[i][j].lcp, Proc.GEO2[x1][y1].lcp);
            Proc.GEO2[x1][y1].tcd += (Proc.DEM[i][j]-Proc.DEM[x1][y1])+Proc.GEO2[i][j].tcd;
            Proc.GEO2[x1][y1].mcd = Math.max((Proc.DEM[i][j]-Proc.DEM[x1][y1])+Proc.GEO2[i][j].mcd, Proc.GEO2[x1][y1].mcd);
        }
        if(Proc.GEO[i][j].llegan_red > 0){
            java.util.Vector diamt = new java.util.Vector();
            java.util.Vector ord = new java.util.Vector();
            for (int k=0; k <= 8; k++){
                if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k && Proc.RedRas[i+(k/3)-1][j+(k%3)-1]>0){
                    ord.addElement(new Integer(Proc.GEO[i+(k/3)-1][j+(k%3)-1].orden));
                    diamt.addElement(new Integer(Proc.GEO2[i+(k/3)-1][j+(k%3)-1].d_topo));
                }
            }
            java.util.Collections.sort(ord);
            java.util.Collections.sort(diamt);
            if(Proc.GEO[i][j].llegan_red > 1)
                Proc.GEO2[i][j].d_topo = 1 + new Integer(diamt.get(diamt.size()-1).toString()).intValue();
            if(Proc.GEO[i][j].llegan_red == 1 )
                Proc.GEO2[i][j].d_topo = new Integer(diamt.get(diamt.size()-1).toString()).intValue();
            if (ord.size()>1){
                if(ord.get(ord.size()-1).equals(ord.get(ord.size()-2))){
                    Proc.GEO[i][j].orden = 1 + new Integer(ord.get(ord.size()-1).toString()).intValue();
                    Proc.GEO[i][j].pcambio = true;
                }
                else Proc.GEO[i][j].orden = new Integer(ord.get(ord.size()-1).toString()).intValue();
            }
            if (ord.size()==1)
                Proc.GEO[i][j].orden = new Integer(ord.get(0).toString()).intValue();
            Proc.maxOrder=Math.max(Proc.maxOrder,Proc.GEO[i][j].orden);
        }
        if (Proc.GEO[x1][y1] != null && Proc.GEO[x1][y1].llegan_red==1 && Proc.DIR[x1][y1]>0){
            GEOcero(x1,y1,contn,Proc);
        }
    }
    
    /**
     * Uses an upstream search algorithm to calculate the distance from the pixel to the
     * drainage point (DEM border ,internal lake or ocean).  The matrix produced by
     * this algorithm allows the rapid calculation of distances along the network
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void getDistanceToBorder(NetworkExtractionModule Proc){
        
        if (Proc.printDebug) System.out.println(">>> Calculating Distance to Border for Network Points");

        float[][] GDO=new float[Proc.metaDEM.getNumRows()+2][Proc.metaDEM.getNumCols()+2];
        int[][] TDO=new int[Proc.metaDEM.getNumRows()+2][Proc.metaDEM.getNumCols()+2];
        
        /*for(int i=2;i<Proc.DIR[0].length-2;i++){
            if(Proc.RedRas[2][i] == 1 && (Proc.DIR[2][i]-1)/3 == 0) {
                TDO[2][i]=1;
        
                toMark=new int[][] {{2,i}};
        
                while(toMark != null){
                    markDistances(Proc,toMark,GDO,TDO);
                }
            }
            if(Proc.RedRas[Proc.DIR.length-3][i] == 1 && (Proc.DIR[Proc.DIR.length-3][i]-1)/3 == 2) {
                TDO[Proc.DIR.length-3][i]=1;
                toMark=new int[][] {{Proc.DIR.length-3,i}};
        
                while(toMark != null){
                    markDistances(Proc,toMark,GDO,TDO);
                }
            }
        }

        for(int i=2;i<Proc.DIR.length-2;i++){
            if(Proc.RedRas[i][2] == 1 && (Proc.DIR[i][2]-1)%3 == 0) {
                TDO[i][2]=1;
                toMark=new int[][] {{i,2}};
        
                while(toMark != null){
                    markDistances(Proc,toMark,GDO,TDO);
                }
            }
            if(Proc.RedRas[i][Proc.DIR[0].length-3] == 1 && (Proc.DIR[i][Proc.DIR[0].length-3]-1)%3 == 2) {
                TDO[i][Proc.DIR[0].length-3]=1;
                toMark=new int[][] {{i,Proc.DIR[0].length-3}};
        
                while(toMark != null){
                    markDistances(Proc,toMark,GDO,TDO);
                }
            }
        }*/
        
        for(int i=2;i<Proc.DIR.length-2;i++) for(int j=2;j<Proc.DIR[0].length-2;j++){
            //if(Proc.RedRas[i][j] == 1 && Proc.DIR[i-1+(Proc.DIR[i][j]-1)/3][j-1+(Proc.DIR[i][j]-1)%3] <= 0) {
            if(Proc.DIR[i][j] > 0){
                if(Proc.DIR[i-1+(Proc.DIR[i][j]-1)/3][j-1+(Proc.DIR[i][j]-1)%3] <= 0) {
                    TDO[i][j]=1;
                    toMark=new int[][] {{i,j}};

                    while(toMark != null){
                        //markDistances(Proc,toMark,GDO,TDO);

                        //*****************************************************************************************
                        int[][] ijs=toMark.clone();
                        java.util.Vector tribsVector=new java.util.Vector();

                        for(int incoming=0;incoming<ijs.length;incoming++){

                            int iIndex=ijs[incoming][0];
                            int jIndex=ijs[incoming][1];

                            byte pcambio=0;
                            for (byte k=0; k <= 8; k++){
                                if (Proc.RedRas[iIndex+(k/3)-1][jIndex+(k%3)-1] == 1 && Proc.DIR[iIndex+(k/3)-1][jIndex+(k%3)-1]==9-k){
                                    pcambio++;
                                }
                            }

                            float dist = 0.0f;

                            switch (Proc.DIR[iIndex][jIndex]) {

                                case 1:     dist=(float)Proc.dxy[iIndex];
                                            break;  
                                case 2:     dist=(float)Proc.dy;
                                            break;
                                case 3:     dist=(float)Proc.dxy[iIndex];
                                            break;
                                case 4:     dist=(float)Proc.dx[iIndex];
                                            break;
                                case 5:     dist=1;
                                            break;
                                case 6:     dist=(float)Proc.dx[iIndex];
                                            break;
                                case 7:     dist=(float)Proc.dxy[iIndex];
                                            break;
                                case 8:     dist=(float)Proc.dy;
                                            break;
                                case 9:     dist=(float)Proc.dxy[iIndex];
                                            break;
                            }


                            GDO[iIndex][jIndex]=dist+GDO[iIndex-1+(Proc.DIR[iIndex][jIndex]-1)/3][jIndex-1+(Proc.DIR[iIndex][jIndex]-1)%3];
                            TDO[iIndex][jIndex]+=TDO[iIndex-1+(Proc.DIR[iIndex][jIndex]-1)/3][jIndex-1+(Proc.DIR[iIndex][jIndex]-1)%3];
                            if(pcambio > 1) {
                                TDO[iIndex][jIndex]++;
                                pcambio=0;
                            }

                            for (byte k=0; k <= 8; k++){
                                //if (Proc.RedRas[iIndex+(k/3)-1][jIndex+(k%3)-1] == 1 && Proc.DIR[iIndex+(k/3)-1][jIndex+(k%3)-1]==9-k){
                                if (Proc.DIR[iIndex+(k/3)-1][jIndex+(k%3)-1]==9-k){
                                    tribsVector.add(new int[] {iIndex+(k/3)-1,jIndex+(k%3)-1});
                                }
                            }
                        }

                        int countTribs=tribsVector.size();

                        if(countTribs != 0){
                            toMark=new int[countTribs][2];
                            for(int k=0;k<countTribs;k++){
                                toMark[k]=(int[])tribsVector.get(k);
                            }
                        } else {
                            toMark=null;
                        }

                        //****************************************************************************************************


                    }
                }
            }
        }
        
        
        
        if (Proc.printDebug) System.out.println(">>> Writing Distances to Border");
        String path=Proc.metaDEM.getLocationBinaryFile().getPath();
        String[] destinations ={path.substring(0, path.lastIndexOf(".")) + ".gdo",
                                path.substring(0, path.lastIndexOf(".")) + ".tdo"};
        java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
        java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;
        try{
            for(int k=0 ; k<destinations.length ; k++){
                buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(destinations[k]));
                outs[k] = new java.io.DataOutputStream(buffOuts[k]);
            }
            
            int nr=Proc.metaDEM.getNumRows();
            int nc=Proc.metaDEM.getNumCols();
            
            
            for (int i=1; i <= nr; i++) for (int j=1; j <= nc ;j++){
//                if (Proc.RedRas[i][j]!=1){
//                    outs[0].writeFloat(-10);
//                    outs[1].writeInt(-10);
//                }
//                else{
                    outs[0].writeFloat(GDO[i][j]);
                    outs[1].writeInt(TDO[i][j]);
//                }
            }
            for(int k=0 ; k<destinations.length ; k++)
                buffOuts[k].close() ;
        }catch(java.io.IOException e1){System.err.println(e1.toString());}
        
    }
    
    private static void markDistances(NetworkExtractionModule Proc,int[][] ijs,double[][] GDO, int[][] TDO){
        
        //System.out.println(">>> Now processing "+(i-1)+" "+(j-1));
 
        java.util.Vector tribsVector=new java.util.Vector();
        
        for(int incoming=0;incoming<ijs.length;incoming++){
            
            int iIndex=ijs[incoming][0];
            int jIndex=ijs[incoming][1];
        
            byte pcambio=0;
            for (byte k=0; k <= 8; k++){
                if (Proc.RedRas[iIndex+(k/3)-1][jIndex+(k%3)-1] == 1 && Proc.DIR[iIndex+(k/3)-1][jIndex+(k%3)-1]==9-k){
                    pcambio++;
                }
            }

            double dist = 0.0;

            switch (Proc.DIR[iIndex][jIndex]) {

                case 1:     dist=Proc.dxy[iIndex];
                            break;  
                case 2:     dist=Proc.dy;
                            break;
                case 3:     dist=Proc.dxy[iIndex];
                            break;
                case 4:     dist=Proc.dx[iIndex];
                            break;
                case 5:     dist=1;
                            break;
                case 6:     dist=Proc.dx[iIndex];
                            break;
                case 7:     dist=Proc.dxy[iIndex];
                            break;
                case 8:     dist=Proc.dy;
                            break;
                case 9:     dist=Proc.dxy[iIndex];
                            break;
            }

            GDO[iIndex][jIndex]=dist+GDO[iIndex-1+(Proc.DIR[iIndex][jIndex]-1)/3][jIndex-1+(Proc.DIR[iIndex][jIndex]-1)%3];
            TDO[iIndex][jIndex]+=TDO[iIndex-1+(Proc.DIR[iIndex][jIndex]-1)/3][jIndex-1+(Proc.DIR[iIndex][jIndex]-1)%3];
            if(pcambio > 1) {
                TDO[iIndex][jIndex]++;
                pcambio=0;
            }

            for (byte k=0; k <= 8; k++){
                if (Proc.RedRas[iIndex+(k/3)-1][jIndex+(k%3)-1] == 1 && Proc.DIR[iIndex+(k/3)-1][jIndex+(k%3)-1]==9-k){
                    tribsVector.add(new int[] {iIndex+(k/3)-1,jIndex+(k%3)-1});
                }
            }
        }
        
        int countTribs=tribsVector.size();
            
        if(countTribs != 0){
            toMark=new int[countTribs][2];
            for(int k=0;k<countTribs;k++){
                toMark[k]=(int[])tribsVector.get(k);
            }
        } else {
            toMark=null;
        }
        
    }
    
    /**
     * Calculates the vectorial network representation.  The format and uses are
     * described in the Developer's Manual.
     * @param Proc The parent {@link hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule}
     */
    public static void getRedVect(NetworkExtractionModule Proc){
        int NPoints; //para rayos4
        int NLinks;  //para rayos4
        java.io.DataOutputStream Dstream;   //para rayos4
        java.io.DataOutputStream Dlink;     //para rayos4
        java.io.DataOutputStream Dpoint;    //para rayos4
        int ncol = Proc.metaDEM.getNumCols();
        int nrows = Proc.metaDEM.getNumRows();
        
        if (Proc.printDebug) System.out.println(">>> Writing Vectorial Network Representation");
        //primero pongo ceros donde no hay red en la matriz de direcciones (me ahorra preguntas)
        
        for(int i=0; i<nrows+2; i++) for (int j=0; j<ncol+2; j++){
            Proc.DIR[i][j]*=Math.abs((Proc.RedRas[i][j]==-10)?0:Proc.RedRas[i][j]);
        }
        
        //cleaning up memory because I want this algorithm to be self contained
        Proc.GEO=null;
        Proc.GEO2=null;
        System.gc();
        
        try{
            
            //reload orders and magnitudes
            String ruta=(Proc.metaDEM.getLocationBinaryFile()).getPath();
            
            Proc.metaDEM.setLocationBinaryFile(new java.io.File(ruta.substring(0, ruta.lastIndexOf(".")) + ".horton"));
            
            String formatoOriginal=Proc.metaDEM.getFormat();
            Proc.metaDEM.setFormat("Byte");
            byte [][] orden=new hydroScalingAPI.io.DataRaster(Proc.metaDEM).getByte();
            
            Proc.metaDEM.setLocationBinaryFile(new java.io.File(ruta.substring(0, ruta.lastIndexOf(".")) + ".magn"));
            Proc.metaDEM.setFormat("Integer");
            int [][] magn=new hydroScalingAPI.io.DataRaster(Proc.metaDEM).getInt();
            Proc.metaDEM.setFormat(formatoOriginal);
            
            for(int i=0; i<nrows; i++) for (int j=0; j<ncol; j++){
                orden[i][j]*=Math.abs((Proc.RedRas[i+1][j+1]==-10)?0:Proc.RedRas[i+1][j+1]);
                magn[i][j]*=Math.abs((Proc.RedRas[i+1][j+1]==-10)?0:Proc.RedRas[i+1][j+1]);
            }
            
            java.io.BufferedOutputStream Bstream = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".stream"));
            Dstream = new java.io.DataOutputStream(Bstream);
            java.io.BufferedOutputStream Blink = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".link"));
            Dlink = new java.io.DataOutputStream(Blink);
            java.io.BufferedOutputStream Bpoint = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".point"));
            Dpoint = new java.io.DataOutputStream(Bpoint);
            java.io.BufferedOutputStream BufAP = new java.io.BufferedOutputStream(new java.io.FileOutputStream(ruta.substring(0, ruta.lastIndexOf(".")) + ".ap"));
            java.io.DataOutputStream DataAP = new java.io.DataOutputStream(BufAP);
            
            int PointerPoints=0;
            int PointerLinks=0;
            boolean changeLink,changeDir;
            
            int nr=Proc.metaDEM.getNumRows();
            int nc=Proc.metaDEM.getNumCols();
        
            int ia, ja, iaN, jaN, arroundI, arroundJ;
            for(int i=2; i<nrows; i++){
                for (int j=2; j<ncol; j++){
                    if (Proc.RedRas[i][j]==1){
                        //informacion sobre el punto especifico
                        int myOrder=orden[i-1][j-1];
                        boolean source=true;
                        
                        //ciclo que determina si es una fuente o un punto de cambio para perseguirlo
                        byte[] oIncoming=new byte[9];
                        for (int k=0; k <= 8; k++){
                            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]==9-k){
                                source=false;
                                oIncoming[k]=orden[i+(k/3)-1-1][j+(k%3)-1-1];
                            }
                        }
                        java.util.Arrays.sort(oIncoming);
                        boolean pcambio=oIncoming[7] == oIncoming[8];
                        
                        if (source || pcambio){
                            
                            NPoints=1;
                            NLinks=0;
                            ia=i;
                            ja=j;
                            int outOrder=0;
                            Dpoint.writeInt((ia-1)*ncol+(ja-1));
                            int myMagn=magn[ia-1][ja-1];
                            Dlink.writeInt(myMagn);
                            Dlink.writeInt((ia-1)*ncol+(ja-1));
                            boolean cambieLink=false;
                            double cotaini = Proc.DEM[i][j];  double cotafin = 0;  double distLink =0;
                            double areaLink=0;
                            do{
                                if (cambieLink){
                                    Dpoint.writeInt((ia-1)*ncol+(ja-1));
                                    myMagn=magn[ia-1][ja-1];
                                    Dlink.writeInt(myMagn);
                                    Dlink.writeInt((ia-1)*ncol+(ja-1));
                                    NPoints++;
                                    cambieLink=false;
                                    distLink=0;
                                    cotaini=Proc.DEM[ia][ja];
                                }
                                iaN=ia+((Proc.DIR[ia][ja]-1)/3)-1;
                                jaN=ja+((Proc.DIR[ia][ja]-1)%3)-1;
                                double[] dist = {Proc.dxy[ia],Proc.dy,Proc.dxy[ia],Proc.dx[ia],1,Proc.dx[ia],Proc.dxy[ia],Proc.dy,Proc.dxy[ia]};
                                distLink += (dist[Proc.DIR[ia][ja]-1])*1000;
                                changeDir=false;
                                try{
                                    changeDir=Proc.DIR[ia][ja] != Proc.DIR[iaN][jaN];
                                }catch(ArrayIndexOutOfBoundsException e){System.err.println("error "+"error "+i+" "+j+" "+ia+" "+ja+" "+iaN+" "+jaN); System.exit(0);}
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
                                outOrder=orden[iaN-1][jaN-1];
                                if (changeLink || outOrder <= 0){
                                    Dpoint.writeInt((ia-1)*ncol+(ja-1));
                                    Dpoint.writeInt((iaN-1)*ncol+(jaN-1));
                                    Dlink.writeInt((ia-1)*ncol+(ja-1));
                                    Dlink.writeInt((iaN-1)*ncol+(jaN-1));
                                    
                                    cotafin=Proc.DEM[iaN][jaN];
                                    areaLink = (Proc.Areas[ia][ja]+(Proc.dx[ia]*Proc.dy)/2)*1000000;
                                    //newfile.write(areaLink+" "+((-cotafin+cotaini)/distLink)+System.getProperty("line.separator"));
                                    DataAP.writeDouble((double) areaLink);
                                    DataAP.writeDouble((double)((-cotafin+cotaini)/distLink));
                                    
                                    if (myOrder != outOrder){
                                        Dstream.writeInt((i-1)*ncol+(j-1));
                                        Dstream.writeInt((ia-1)*ncol+(ja-1));
                                        Dstream.writeInt((iaN-1)*ncol+(jaN-1));
                                    }
                                    NPoints+=2;
                                    Dlink.writeInt(PointerPoints*4);  Dlink.writeInt(NPoints);
                                    NLinks++;
                                    PointerPoints+=NPoints;
                                    NPoints=0;
                                    cambieLink=true;
                                } else {
                                    if (changeDir){
                                        Dpoint.writeInt((iaN-1)*ncol+(jaN-1));
                                        NPoints++;
                                    }
                                }
                                ia=iaN; ja=jaN;
                            } while (myOrder == outOrder);
                            Dstream.writeInt(myOrder);
                            Dstream.writeInt(PointerLinks*8);
                            Dstream.writeInt(NLinks);
                            PointerLinks+=NLinks;
                        }
                    }
                }
            }
            Bstream.close(); Blink.close(); Bpoint.close();
            BufAP.close();
            //newfile.close(); bufferout.close();
        }catch(java.io.IOException e){
            System.err.println(e.toString());
        }
    }   
}
