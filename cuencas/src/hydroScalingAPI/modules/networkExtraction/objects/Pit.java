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
 * This class represent a DEM PIT.  A very important feature of the terrain associated to the
 * Network Extraction Process.  In escence a Pit is a region of terrain to which
 * part of the DEM drains into.  This regions are usually artifacts of the data. 
 * The network extraction algorithm assumes that there are not interal Pits in the
 * DEM, thus all pits are resolved by filling or cutting the DEM such that they
 * flow to another region of the DEM or to the DEM borders.  Notice that "actual"
 * pits can exist on the terrain, such as lakes.  These features must be properly
 * identified (by -9999 values) in order to stop the algoritm from trying to solve
 * it.
 * @author Jorge Mario Ramirez and Ricardo Mantilla
 */
public class Pit extends Object implements Comparable{
    
    /**
     * An identifier for assigned to cells that belong (drain into) the Pit.
     */
    public int grupo ;
    /**
     * An index identifying the priority of the Pit with respect to all the other Pits
     * in the DEM.  The herarchy is usually based on the Pit elevation.
     */
    public int orden;
    /**
     * A boolean flag indicating whether an outlet has been assigned for the Pit
     */
    public boolean salida;
    /**
     * A {@link hydroScalingAPI.modules.networkExtraction.objects.WorkRectangle} object
     * describing the rectangular region that encloses the Pit
     */
    public WorkRectangle Mp ;
    /**
     * A {@link hydroScalingAPI.modules.networkExtraction.objects.WorkRectangle} object
     * describing the rectangular region that encloses the Pit's basin (set of cells
     * that drain into the Pit)
     */
    public WorkRectangle Mc;
    /**
     * The elevation of the Pit.
     */
    public double cota ;
    /**
     * A boolean flag indicating whether the Pit has been resolved
     */
    public boolean corregido=false;
    private int[][] corr3;
    private double sink_minAdyacente;
    
    /**
     * Creates an instance of the Pit element
     * @param ggrupo The Pit group ID
     * @param ccota The Pit elevation
     * @param MMp A {@link hydroScalingAPI.modules.networkExtraction.objects.WorkRectangle} object
     * describing the rectangular region that encloses the Pit
     */
    public Pit(int ggrupo, double ccota, WorkRectangle MMp){
        grupo = ggrupo;
        Mp= MMp;
        cota = ccota;
    }
    
    /**
     * The comparison method for the Pit
     * @param p1 The Pit be compared against
     * @return A negative integer, zero, or a positive integer as this object
     * is less than, equal to, or greater than the specified object.
     */
    public int compareTo(java.lang.Object p1) {
        int comp;
        Pit thisPit=(Pit)p1;
        if(cota != thisPit.cota)
            comp  = (int)(( -cota + thisPit.cota)/Math.abs(cota - thisPit.cota));
        else
            comp  = (int)(( -orden + thisPit.orden)/Math.abs(orden - thisPit.orden));
        
        return comp;
    }
    
    /**
     * The equal method for the Pit
     * @param p1 The Pit be compared against
     * @return A boolean indicating if the equality holds
     */
    public boolean equals(java.lang.Object p1){
        Pit thisPit=(Pit)p1;
        return((grupo==thisPit.grupo) && (orden==thisPit.orden));
    }
    
    
    /**
     * This algorithm fixes the Pit by finding a way out for the cells iside and
     * draining to the pit
     * @param Proc The parent NetworkExtractionModule
     */
    public void corrigePit(NetworkExtractionModule Proc){
        
        Pit Sn = this;
        
        if (Proc.printDebug) {
            System.out.print("    >>> Group: "+ Sn.grupo + " level  " + Sn.cota +" frame :");
            Sn.Mp.print_M();
        }
        
        corr3 = new int[Proc.metaDEM.getNumRows()+2][Proc.metaDEM.getNumCols()+2];
        
        double fondo = Sn.cota;
        
        java.util.Vector celdas_pit = new java.util.Vector();
        
        java.util.Vector celdas_frontc = new java.util.Vector();
        java.util.Vector celdas_frontcp = new java.util.Vector();
        Sn.Mc = new WorkRectangle(Sn.Mp.ini_row,Sn.Mp.end_row,Sn.Mp.ini_col,Sn.Mp.end_col,Proc);
        for (int i=Sn.Mp.ini_row ; i<=Sn.Mp.end_row; i++){
            for (int j=Sn.Mp.ini_col; j<=Sn.Mp.end_col; j++){
                if (Proc.DIR[i][j]/100 == Sn.grupo){
                    corr3[i][j] = 1 + (Sn.grupo * 10);
                    /*A las celdas del pit que toquen altos, se les saca la cuenca, estas quedan con 3 en corr3*/
                    if (Proc.DIR[i][j]%100 == 11){
                        /*A cada una de las que tocan alto se les saca la cuenca, esto produce unas marcas
                        de 3 en corr3 y actualiza el marco de la cuenca del pit en uso */
                        
                        try{
                            Sn.marcarCuencaPit(i,j,Proc.DIR,corr3);
                        }catch(StackOverflowError e){ 
                            marcarCuencaPit2(Proc, i,j, Sn,corr3);
                        }
                        
                        /*Ahora se busca la frontera del pit, que son celdas que no estan en el pit pero
                        son adyacentes a el y tienen cota>pit.cota. Se les coloca un 2 en corr3 y se
                        actualiza el marco de la cuenca del pit para que las contenga*/
                        for (int k=0; k <= 8; k++){
                            if (Proc.DEM[i+(k/3)-1][j+(k%3)-1] > Sn.cota){
                                corr3[i+(k/3)-1][j+(k%3)-1] = 2 + (Sn.grupo * 10);
                                Sn.Mc.act_WorkRectangle(i+(k/3)-1,j+(k%3)-1);
                                Sn.Mp.act_WorkRectangle(i+(k/3)-1,j+(k%3)-1);
                            }
                        }
                    }
                    celdas_pit.addElement(new GeomorphCell_0(i,j,Proc.DEM[i][j],0));
                }
            }//for(j) marco del pit
        }//for(i) marco del pit
        
        /* Ahora, en todas las de la cuenca y la frontera del pit se buscan aquellas que toquen
        alguna celda que no esta en el grupo. Si esa que toquen fuera del grupo es mas alta
        que (i,j) entonces es un posible vertedero, luego se le coloca un 8 y se incluye en el
        marco de la cuenca.*/
        for (int i=Sn.Mc.ini_row; i<=Sn.Mc.end_row; i++){
            for (int j=Sn.Mc.ini_col; j<=Sn.Mc.end_col; j++){
                if (corr3[i][j] == 3 + (10*Sn.grupo) || corr3[i][j] == 2 + (10*Sn.grupo)  ){
                    for (int k=0; k <= 8; k++){
                        if (corr3[i+(k/3)-1][j+(k%3)-1]/10 != Sn.grupo && Proc.DEM[i+(k/3)-1][j+(k%3)-1] >= Proc.DEM[i][j]){
                            corr3[i+(k/3)-1][j+(k%3)-1] = 8 + (Sn.grupo*10);
                            Sn.Mc.act_WorkRectangle(i+(k/3)-1,j+(k%3)-1);
                        }//if toca fuera mas alto.
                        if (corr3[i][j] == 3 + (10*Sn.grupo) && corr3[i+(k/3)-1][j+(k%3)-1]/10 != Sn.grupo && Proc.DEM[i+(k/3)-1][j+(k%3)-1] < Proc.DEM[i][j]){
                            corr3[i][j] = 4 + (Sn.grupo*10);
                            Sn.Mc.act_WorkRectangle(i+(k/3)-1,j+(k%3)-1);
                        }//if toca fuera mas alto.
                    }//for (k)
                }// 3 o 2
            }//for(j)
        }//for(i)
        
        for (int i=Sn.Mp.ini_row; i<=Sn.Mp.end_row; i++){
            for (int j=Sn.Mp.ini_col; j<=Sn.Mp.end_col; j++){
                if (corr3[i][j] == 2 + (Sn.grupo*10)){
                    Loop5:
                        for (int k=0; k <= 8; k++){
                            if(corr3[i+(k/3)-1][j+(k%3)-1]/10!=Sn.grupo && Proc.DEM[i+(k/3)-1][j+(k%3)-1]<=Sn.cota){
                                corr3[i][j] = 5 + (Sn.grupo*10);
                                break Loop5;
                            }
                            if(corr3[i+(k/3)-1][j+(k%3)-1]/10!=Sn.grupo && Proc.DEM[i+(k/3)-1][j+(k%3)-1]<Proc.DEM[i][j]){
                                corr3[i][j] = 4 + (Sn.grupo*10);
                                break Loop5;
                            }
                        }//for(k)
                }
            }
        }
        
        /*Ahora se busca por los 4, 5 y 8, a cada uno se le saca la cota de la mas bajita entre
        las adyacentes que no sean del grupo. Las con 4 y 8 se llevan al vector front_c y las
        con 5 al front_cp */

        for (int i=Sn.Mc.ini_row; i<=Sn.Mc.end_row; i++){
            for (int j=Sn.Mc.ini_col; j<=Sn.Mc.end_col; j++){
                if (corr3[i][j] == 4 + (Sn.grupo*10) || corr3[i][j] == 8 + (Sn.grupo*10) || corr3[i][j] == 5 + (Sn.grupo*10)){
                    double min_ady = Proc.DEM[i][j];
                    for (int k=0; k <= 8; k++){
                        if (corr3[i+(k/3)-1][j+(k%3)-1]/10 != Sn.grupo)
                            min_ady = Math.min(min_ady , Proc.DEM[i+(k/3)-1][j+(k%3)-1]);
                    }
                    if (corr3[i][j] == 5 + (Sn.grupo*10))
                        celdas_frontcp.addElement(new GeomorphCell_0(i,j,Proc.DEM[i][j],min_ady));
                    if (corr3[i][j] == 4 + (Sn.grupo*10) || corr3[i][j] == 8 + (Sn.grupo*10))
                        celdas_frontc.addElement(new GeomorphCell_0(i,j,Proc.DEM[i][j],min_ady));
                }//celdas con 4 o 5
            }//for(j)
        }//for(i)
        
        
        /*if (Sn.Mc.is_on(2027,435)) {
                System.out.print("    >>> Frame on corrPit: Grupo:"+Sn.grupo+" Frame: ");
                Sn.Mc.print_M();
                Sn.Mc.printPedazoTemporal();
                System.out.println();
                System.out.println("CORR 3");
                for(int i=2030;i>2020;i--){
                    for(int j=430;j<440;j++){
                        System.out.print(corr3[i][j]+" ");
                    }
                    System.out.println();
                }
                System.out.println();
            }*/
        
        
        
        /*Se crea un vector de objetos GeomorphCell_0s que guardara los posibles vertederos. */
        java.util.Vector vertederos = new java.util.Vector();

        /*Si hay vertederos en la frontera del pit, se ordena por cota de menor a mayor y se nombra celda0
        a la de cota menor, que coincide con la priemra entrada del vector despues de ordenarlo.*/
        if (celdas_frontcp.size() != 0){
            
            if (Proc.printDebug) System.out.println("    >>> >>> Spill Found at Pit Border" );
            
            java.util.Collections.sort(celdas_frontcp);
            GeomorphCell_0 celda0 = (GeomorphCell_0)celdas_frontcp.get(0);
            for (int m=0; m < celdas_frontcp.size(); m++){
                GeomorphCell_0 celda = (GeomorphCell_0)celdas_frontcp.get(m);
                /* Los primeros vertederos que se corrigen son aquellos que sean los m�s bajitos y tengan
                como min_ady una celda m�s baja que el pit. */
                if(celda.height == celda0.height && celda.min_ady <= Sn.cota)
                    vertederos.addElement(celda);
            }
            /*Con estos vertederos se hace el 1er criterio que: llena en caso de que el numero de celdas
            a llenar, por el crit_cant sea menor que el n�mero de celdas que se cortarian. Si esto
            esto no ocurre simplemente se cortan todos los vertederos hasta la cota del pit. Se da por
            corregido el pit (corregido_SS = true) */

            if (vertederos.size()>0){
                int a_llenar = Sn.countMarcados(celda0.height,corr3,Proc.DEM);
                if (Proc.cCorte*a_llenar < vertederos.size() && celda0.height-Sn.cota<=Proc.cAltura){
                    Sn.fillCuencaPit(celda0.height,corr3,Proc);
                    fondo = celda0.height;
                }
                else {
                    for(int m1=0 ; m1<vertederos.size(); m1++){
                        GeomorphCell_0 vert = (GeomorphCell_0)vertederos.get(m1);
                        int i=vert.i ; int j=vert.j;
                        for (int k=0; k <= 8; k++){
                            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]>=10 && Proc.DIR[i+(k/3)-1][j+(k%3)-1]/100 != Sn.grupo && Proc.DEM[i+(k/3)-1][j+(k%3)-1]==Sn.cota){
                                Sn.Mc.act_WorkRectangle(i+(k/3)-1 , j+(k%3)-1);
                                //System.out.println("Vertedero "+vert.i+" "+vert.j+" Agrega "+(i+(k/3)-1)+" "+(j+(k%3)-1));
                            }
                        }
                        /*try{
                            Proc.MaxPend[vert.i][vert.j]=(Proc.DEM[vert.i][vert.j]-Sn.cota);
                            //Proc.newfile.write("corta1 "+vert.i+" "+vert.j+" "+Proc.DEM[vert.i][vert.j]+ " " + Sn.cota+" "+(-Proc.DEM[vert.i][vert.j]+Sn.cota)+System.getProperty("line.separator"));
                        }catch(java.io.IOException e){e.printStackTrace();}*/
                        Proc.DEM[vert.i][vert.j] = Sn.cota;
                        corr3[vert.i][vert.j] = 8+Sn.grupo*10;
                        
                        if (Proc.printDebug) System.out.println("    >>> PIT " + Sn.grupo + " WAS FIXED USING CRITERIA 1 cutting through cell " +vert.i+" "+vert.j+"  to  "+Proc.DEM[vert.i][vert.j] );
                    }
                    fondo=Sn.cota;
                }
                corregido = true;
            }
        }//if hay celdas en frontcp
        
        if(celdas_frontcp.size()==0){
            
            if (Proc.printDebug) System.out.println("    >>> >>> Spill Not Found at Pit Border" );

            java.util.Vector vertederos1 = new java.util.Vector();
            /*Se organizan las celdas de la frontera de la cuenca y se llama celda0 a la de menor cota*/
            java.util.Collections.sort(celdas_frontc);
            try{
                GeomorphCell_0 celda0 = (GeomorphCell_0)celdas_frontc.get(0);
            }catch(ArrayIndexOutOfBoundsException e){
                if (Proc.printDebug) System.out.println("    >>> >>> >>> Fix Pit Algorithm problems found");
                
                sink_minAdyacente=Double.MAX_VALUE;
                for (int ic=Sn.Mc.ini_row; ic<=Sn.Mc.end_row; ic++){
                    for (int jc=Sn.Mc.ini_col; jc<=Sn.Mc.end_col; jc++){
                        if (Proc.DIR[ic][jc]/100 == Sn.grupo){
                            for (int k=0; k <= 8; k++){
                                if (Proc.DIR[ic+(k/3)-1][jc+(k%3)-1]/100 != Sn.grupo)
                                    sink_minAdyacente = Math.min(sink_minAdyacente , Proc.DEM[ic+(k/3)-1][jc+(k%3)-1]);
                            }
                            
                        }
                    }
                }
                
                for (int ic=Sn.Mc.ini_row; ic<=Sn.Mc.end_row; ic++){
                    for (int jc=Sn.Mc.ini_col; jc<=Sn.Mc.end_col; jc++){
                        if (Proc.DIR[ic][jc]/100 == Sn.grupo) Proc.DEM[ic][jc]=sink_minAdyacente;
                    }
                }
                
                if (Proc.printDebug) System.out.println("    >>> >>> >>> PIT RAISED TO LEVEL "+sink_minAdyacente);
                if (Proc.printDebug) System.out.println("    >>> >>> >>> Cleaning Direction Matrix and restarting algorithm");
                int ncol = Proc.metaDEM.getNumCols();
                int nfila = Proc.metaDEM.getNumRows();
                Proc.DIR = new int[nfila+2][ncol+2];

                //Se colocan -1 en la primera fila y columna (i=0, j=0) y en las ultimas (i=nfila+1, j=ncol+1).
                for (int ci=0; ci<nfila+2; ci++){
                    Proc.DIR[ci][0]=-1 ; Proc.DIR[ci][ncol+1]=-1;
                }
                for (int ci=0; ci<ncol+2; ci++){
                    Proc.DIR[0][ci]=-1 ; Proc.DIR[nfila+1][ci]=-1;
                }
                
                Proc.convergenceAlarmType1=true;
                return;
                //WorkRectangle.print_Md(Sn.Mp,true,Proc.DEM,0);
            }
            /*Se busca en el vector ordenado de celdas en la frontera y a aquellas que tengan la misma cota
            que la mas bajita, se les pone var_to_compare = min_ady y se meten a un vector vertederos1. */
            GeomorphCell_0 celda0=(GeomorphCell_0)celdas_frontc.get(0);
            for (int m=0; m<celdas_frontc.size(); m++){
                GeomorphCell_0 celda4 = (GeomorphCell_0)celdas_frontc.get(m);
                if(celda4.height == celda0.height){
                    celda4.var_to_compare = celda4.min_ady;
                    vertederos1.addElement(celda4);
                }
            }
            /*Se ordena vertederos1 seg�n el min_ady y se llama vert0_m al de menor min_ady, que adem�s es
            uno de los que tiene la menor cota.*/
            java.util.Collections.sort(vertederos1);
            GeomorphCell_0 vert0_m = (GeomorphCell_0)vertederos1.get(0);
            /*Se busca en vertederos1 aquellos que tengan la misma min_ady que el vert0_m y se meten en
            un vector vertederos2*/
            for (int m=0; m<vertederos1.size(); m++){
                GeomorphCell_0 vert = (GeomorphCell_0)vertederos1.get(m);
                if(vert.var_to_compare != vert0_m.var_to_compare)
                    vertederos1.remove(vert);
            }
            /*El Vector de vertederos1 queda con las celdas mas bajas de la frontera del pit y ademas
            que tienen la min_ady mas baja. Se busca en este vector y se detrmina la menor distancia
            dist_pit hasta el pit del correspondiente grupo.*/
            if (Proc.printDebug) System.out.println("    >>> >>> >>> Number of Spills to Analyze: "+vertederos1.size());
            int ncol = Proc.metaDEM.getNumCols();
            int nfila = Proc.metaDEM.getNumRows();
            for (int m=0; m<vertederos1.size(); m++){
                if (Proc.printDebug) System.out.println("    >>> >>> >>> Analyzing Spill: "+m);
                GeomorphCell_0 vert = (GeomorphCell_0)vertederos1.get(m);
                /*Para hallar la distancia se hace una busqueda por anillos con variables r y s con
                centro en las coordenadas de cada vertedero (vert.i y vert.j). La variable paso se
                usa para saltar en las celdas que ya se buscaron en anillos anteriores.*/         
                
                
                
                
//                int i = vert.i ; int j = vert.j;
//                boolean toco_pit = false ;
//                double dist_pit = Math.sqrt((double)(ncol*ncol+ nfila*nfila));
//                int t=1;  int toca_pit_i=0;  int toca_pit_j=0 ;            
//                do{
//                    t++ ; int paso=1;
//                    for(int r=-t; r<=t ; r++){
//                        if (Math.abs((double)t)!=t)
//                            paso = 2*t;
//                        for(int s= -t ; s <= t ; s += paso){
//                            double dist_pitp = Math.sqrt((double)(r*r + s*s));
//                            /*Cuando se toca una de las celdas del pit (Proc.DIR[i+r][j+s]/100 == Sn.grupo) y la distancia euclidea  
//                            a ella es menor de la que se tenia se actualizan dist_pit, toca_pit_i y toca_pit_j.*/
//                            /*MAJOR BUG DETECTED.  IT WAS USING corr3[i+r][j+s]=1+Sn.grupo*10 TO IDENTIFY THE PIT LOCATION*/
//                            if (i+r>1 && j+s>1 && i+r<corr3.length && j+s<corr3[0].length && Proc.DIR[i+r][j+s]/100 == Sn.grupo && dist_pitp<=dist_pit){
//                                dist_pit = dist_pitp;
//                                toca_pit_i=i+r; toca_pit_j=j+s;
//                                toco_pit = true;
//                            }
//                        }
//                    }
//                    if (t == 1000) {
//                        if (Proc.printDebug) System.out.println("    >>> >>> >>> Time out in Ring Search Loop: "+t);
//                        if (Proc.printDebug) System.out.println("    >>> >>> >>> Spill Coordinates: y:"+i+" x:"+j);
//                        if (Proc.printDebug) System.out.println("    >>> >>> >>> Spill Elevation: "+vert.cota);
//                        Proc.writeCorrDem(Proc.metaDEM.getLocationBinaryFile());
//                        System.exit(0);
//                        return;
//                    }
//                    /*El loop termina en el primer anillo en donde se toque el pit. Se anota la menor distancia
//                    del anillo, y se guarda la celda que corresponde a esa distancia en toca_pit_i (y j)*/
//                }while (!toco_pit);
                
                
                
                //Se busca la celda del pit que tiene distancia minima a vert. Este metodo que se llama actualiza
                //la distancia al pit the vert, y su i_romper y j_romper. Ademas distopit se coloca como var_to_compare de vert.
                vert.findDisToPit(celdas_pit.toArray());

            }//for cada uno de los vertederos ya ordenados por cota y min_ady
            /*Se ordenan los vertederos por distancia al pit y se saca el primero  */
            java.util.Collections.sort(vertederos1);
            
            GeomorphCell_0 salida = (GeomorphCell_0)vertederos1.get(0);
            
            int i1=salida.i ; int j1 = salida.j;
            //int i2=salida.i_romper ; int j2=salida.j_romper;
            int a_llenar = Sn.countMarcados(salida.height,corr3,Proc.DEM);
            int a_cortar = 0 ;
            
            if (corr3[i1][j1] == 8 + (Sn.grupo*10)){
                int i2=i1; int j2=j1;
                double min_a = Double.MAX_VALUE;
                for(int k=0; k <= 8; k++){
                    if ((corr3[i1+(k/3)-1][j1+(k%3)-1]==4+(Sn.grupo*10) || corr3[i1+(k/3)-1][j1+(k%3)-1]==3+(Sn.grupo*10)
                    || corr3[i1+(k/3)-1][j1+(k%3)-1]==2+(Sn.grupo*10)) && Proc.DEM[i1+(k/3)-1][j1+(k%3)-1]<min_a){
                        min_a = Proc.DEM[i1+(k/3)-1][j1+(k%3)-1];
                        i2=i1+(k/3)-1; j2=j1+(k%3)-1;
                    }
                }
                if (min_a==Double.MAX_VALUE) if (Proc.printDebug) System.out.println("!!!!! couldn't find "+i1+" "+j1);
                i1=i2; j1=j2;
            }
            int in1=i1; int jn1=j1;
            do{
                a_cortar++;
                int iv=in1; int jv=jn1;
                int d=Proc.DIR[iv][jv];
                in1=iv-1+(d-1)/3;
                jn1=jv-1+(d-1)%3;
            }while(corr3[in1][jn1]==2+(Sn.grupo*10));
            /*try{
                Proc.newfile2.write("a_cortar "+a_cortar+"   a_llenar  "+a_llenar+System.getProperty("line.separator"));
            }catch(java.io.IOException e){e.printStackTrace();}*/
            //if(a_cortar>2){
            
            // no deja llenar mas de cAltura metros
            
            if (a_llenar <= Proc.cCorte*a_cortar && salida.height-Sn.cota <= Proc.cAltura){
                Sn.fillCuencaPit(salida.height,corr3,Proc);
                fondo=salida.height;
                if (Proc.printDebug) System.out.println("    >>> PIT " + Sn.grupo + " WAS FIXED USING CRITERIA 3,1 filling up to " + salida.height);
            } else{
                if (Proc.printDebug) System.out.println("    >>> PIT " + Sn.grupo + " WAS FIXED USING CRITERIA 3,2 cutting from "+ i1+ " "+j1+" to "+in1+" "+jn1+" to level "+ Math.max(salida.min_ady,Sn.cota)  );
                for (int k=0; k <= 8; k++){
                    if (Proc.DIR[i1+(k/3)-1][j1+(k%3)-1]>=10 && Proc.DIR[i1+(k/3)-1][j1+(k%3)-1]/100 != Sn.grupo && Proc.DEM[i1+(k/3)-1][j1+(k%3)-1]==Sn.cota){
                        Sn.Mc.act_WorkRectangle(i1+(k/3)-1,j1+(k%3)-1);
                    }
                }
                
                //llenar la cuenca del pit cuando la cota a la que se va a cortar (min_ady) esta por encima del sumidero
                if(salida.min_ady > Sn.cota){
                    Sn.fillCuencaPit(salida.min_ady,corr3,Proc);
                }
                
                /*try{
                    Proc.MaxPend[salida.i][salida.j]=(Proc.DEM[salida.i][salida.j]-Math.max(salida.min_ady,Sn.cota));
                    Proc.newfile.write("corta2v "+salida.i+" "+salida.j+" "+Proc.DEM[salida.i][salida.j]+ " " + Math.max(salida.min_ady,Sn.cota)+" "+(-Proc.DEM[salida.i][salida.j]+Math.max(salida.min_ady,Sn.cota))+System.getProperty("line.separator"));
                }catch(java.io.IOException e){e.printStackTrace();}*/
                
                Proc.DEM[salida.i][salida.j]=Math.max(salida.min_ady,Sn.cota);                
                int in2=i1; int jn2=j1;
                do{
                    int iv=in2; int jv=jn2;
                    /*try{
                        Proc.MaxPend[in2][jn2]=(Proc.DEM[in2][jn2]-Math.max(salida.min_ady,Sn.cota));
                        Proc.newfile.write("corta2r "+in2+" "+jn2+" "+Proc.DEM[in2][jn2]+" " + Math.max(salida.min_ady,Sn.cota)+" "+(-Proc.DEM[in2][jn2]+Math.max(salida.min_ady,Sn.cota))+System.getProperty("line.separator"));
                    }catch(java.io.IOException e){e.printStackTrace();}*/
                    Proc.DEM[in2][jn2]=Math.max(salida.min_ady,Sn.cota);
                    int d=Proc.DIR[iv][jv];
                    in2=iv-1+(d-1)/3;
                    jn2=jv-1+(d-1)%3;
                }while(corr3[in2][jn2]==2+(Sn.grupo*10));
            }
            corregido = true;
        }
        //System.out.print("(fondo==Sn.cota) "+(fondo==Sn.cota));
        //Sn.Mc.print_M();
        if(fondo==Sn.cota){
            boolean aumenta;
            int totNumSinks=Proc.Sink_t.size();
            do{
                aumenta=false;
                for (int i=Sn.Mc.ini_row; i<=Sn.Mc.end_row; i++){ 
                    for (int j=Sn.Mc.ini_col; j<=Sn.Mc.end_col; j++){                       
                        if(corr3[i][j] == 0 && Proc.DIR[i][j]>=10 && Proc.DEM[i][j]==Sn.cota){                                    
                            for(int t=0; t<totNumSinks; t++){
                                Pit St = (Pit)Proc.Sink_t.get(t);                                        
                                if(!Sn.equals(St) && St.grupo==Proc.DIR[i][j]/100 && !St.corregido && St.Mp.is_on(i,j)==true){                                            
                                    St.corregido=true;
                                    Sn.Mc.add_WorkRectangle(St.Mp);                               
                                    aumenta=true;
                                }   
                            }                     
                        }
                    }
                }           
            }while(aumenta);
        }
        
        Sn.corregido=true;
        
        corr3=null;
        //System.out.println("Pit- mem disp: "+Runtime.getRuntime().freeMemory());
        Sn.Mc.corrigeWorkRectangle((java.util.Vector) Sn.Mc.findPits2(fondo));
    }
    
    //Para OpProcesar
    /**
     * Returns the size (number of pixels) in the Pit
     * @param MD A direction matrix in which cells belonging to a Pit are labeled by the Pit
     * group ID
     * @return An integer with the Pit size
     */
    public int getCantPit(int[][] MD){
        int cant=0;
        for (int i=Mp.ini_row ; i<=Mp.end_row; i++){
            for (int j=Mp.ini_col; j<=Mp.end_col; j++){
                if (MD[i][j]/100 == grupo){
                    cant++;
                }
            }
        }
        return cant;
    }
    
    
    /**
     * Returns the size (number of pixels) in the Pit's basin
     * @param MD A direction matrix in which cells belonging to a Pit are labeled by the Pit
     * group ID
     * @return The size of the Pit's basin
     */
    public int getCantCPit(int[][] MD){
        int cant=0;
        for (int i=Mp.ini_row ; i<=Mp.end_row; i++){
            for (int j=Mp.ini_col; j<=Mp.end_col; j++){
                if (MD[i][j]/100 == grupo){
                    if (MD[i][j]%100 == 11)
                        cant += getNCuencaPit(i,j,0,MD);
                }
            }
        }
        return cant;
    }
    
    private int getNCuencaPit(int i, int j, int lleva, int[][]MD){
        int lleva2=lleva;
        for (int k=0; k <= 8; k++){
            if (MD[i+(k/3)-1][j+(k%3)-1] == 9-k){
                lleva2++;
                getNCuencaPit(i+(k/3)-1,j+(k%3)-1,lleva2,MD);
            }
        }
        return lleva2;
    }

    private void marcarCuencaPit(int i, int j, int[][] MD, int[][] corr3){
        for (int k=0; k <= 8; k++){
            if (MD[i+(k/3)-1][j+(k%3)-1] == 9-k){
                Mc.act_WorkRectangle(i+(k/3)-1,j+(k%3)-1);
                corr3[i+(k/3)-1][j+(k%3)-1] = 3+(10*grupo);
                marcarCuencaPit(i+(k/3)-1,j+(k%3)-1, MD, corr3);
                
            }
        }
    }
    
    private void marcarCuencaPit2(NetworkExtractionModule Proc, int i, int j, Pit pit, int[][] corr3){
        int nc = Proc.metaDEM.getNumCols();
        int parIni = i*nc+j;
        int par1 = i*nc+j; int par2 = i*nc+j; int par3 = i*nc+j;
        if(markCell(Proc,parIni,pit,nc)>0){
            do{
                par1 = markCell(Proc,parIni,pit,nc);
                boolean devuelve = false;
                do{
                    boolean salga = false ;
                    do{
                        if(!devuelve){
                            pit.Mc.act_WorkRectangle(par1/nc,par1%nc);
                            corr3[(par1/nc)][(par1%nc)] = 3+(10*pit.grupo);
                        }
                        devuelve = false;
                        par2 = markCell(Proc, par1, pit, nc);
                        salga = (par2 == 0);
                        if(!salga){
                            pit.Mc.act_WorkRectangle(par2/nc,par2%nc);
                            corr3[(par2/nc)][(par2%nc)] = 3+(10*pit.grupo);
                            par3 = markCell(Proc, par2, pit, nc);
                            salga = (par3 == 0);
                        }
                        if(!salga) par1=par3;
                    }while(!salga);
                    if(par2==0)
                        par1 =  ((par1/nc)-1+(Proc.DIR[(par1/nc)][(par1%nc)]-1)/3)*nc + ((par1%nc)-1+(Proc.DIR[(par1/nc)][(par1%nc)]-1)%3);
                    else if(par3 == 0)
                        par1 =  ((par2/nc)-1+(Proc.DIR[(par2/nc)][(par2%nc)]-1)/3)*nc + ((par2%nc)-1+(Proc.DIR[(par2/nc)][(par2%nc)]-1)%3);
                    devuelve = true;
                }while(par1 != parIni);
            }while(markCell(Proc,parIni,pit,nc)>0);
        }
    }
    
    private  int markCell(NetworkExtractionModule Proc, int id,Pit P, int ncols){
        int j= id%ncols;
        int i = id/ncols;
        for (int k=0; k <= 8; k++){
            //try{
            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1] == 9-k && corr3[i+(k/3)-1][j+(k%3)-1] - 10*P.grupo != 3){
                return((i+(k/3)-1)*ncols + (j+(k%3)-1));
            }
            //}catch(ArrayIndexOutOfBoundsException e){System.out.println((i+(k/3)-1)+" "+(j+(k%3)-1)+" "+i+" "+j+" "+corr3.length+" "+corr3[0].length); System.exit(0);}
        }
        return 0;
    }
    
    private void fillCuencaPit(double cotaf,int[][] corr3, NetworkExtractionModule Proc){
        for (int i=Mc.ini_row; i<=Mc.end_row; i++){
            for (int j=Mc.ini_col; j<=Mc.end_col; j++){
                if (corr3[i][j]/10 == grupo && Proc.DEM[i][j]< cotaf){
                   /*try{
                        Proc.MaxPend[i][j]=(Proc.DEM[i][j]-cotaf);
                        Proc.newfile.write("llena "+i+" "+j+" "+Proc.DEM[i][j]+ " " + cotaf+" "+(-Proc.DEM[i][j]+cotaf)+System.getProperty("line.separator"));
                   }catch(java.io.IOException e){e.printStackTrace();}*/
                    Proc.DEM[i][j]= cotaf;
                }
            }
        }
    }
    
    private int countMarcados(double cotaf, int[][] corr3, double[][] DEMC){
        int cuenca_cant = 0;
        for (int i=Mc.ini_row; i<=Mc.end_row; i++){
            for (int j=Mc.ini_col; j<=Mc.end_col; j++){
                if (corr3[i][j]/10 == grupo && DEMC[i][j]<=cotaf)
                    cuenca_cant++;
            }
        }
        return cuenca_cant;
    }

}
