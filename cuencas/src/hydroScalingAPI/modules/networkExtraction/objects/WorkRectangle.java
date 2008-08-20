package hydroScalingAPI.modules.networkExtraction.objects;

/**
 *
 * @author Jorge Mario Ramirez and Ricardo Mantilla
 */
public class WorkRectangle extends Object  {
    public int ini_i;
    public int end_i;
    public int ini_j;
    public int end_j;
    public int nfilaM;
    public int ncolM;
    public int corr1[][];
    public int corr2[][];
    public boolean corrija2=true;
    NetworkExtractionModule Proc;
    
    /** Creates new WorkRectangle */
    public WorkRectangle(int ii, int ei, int ij, int ej, NetworkExtractionModule Proc1) {
        ini_i=ii;
        end_i=ei;
        ini_j=ij;
        end_j=ej;
        nfilaM = ei-ii+1;
        ncolM = ej-ij+1;
        Proc = Proc1;
    }
    
    public WorkRectangle(int ii, int ei, int ij, int ej) {
        ini_i=ii;
        end_i=ei;
        ini_j=ij;
        end_j=ej;
        nfilaM = ei-ii+1;
        ncolM = ej-ij+1;
    }
    
    public boolean equals(final java.lang.Object p1) {
        int comp;
        WorkRectangle myWorkRectangle = (WorkRectangle) p1;
        if(ini_i==myWorkRectangle.ini_i && ini_j==myWorkRectangle.ini_j && end_i==myWorkRectangle.end_i && end_j==myWorkRectangle.end_j)
            return true;
        else 
            return false;
    }
    
    public void corrigeWorkRectangle(java.util.Vector FSc){
        java.util.Vector F = (java.util.Vector) FSc.get(0);
        java.util.Vector S = (java.util.Vector) FSc.get(1);
        double c = ((Double) FSc.get(2)).doubleValue();
        int numcor=0;
        java.util.Vector F_on = F;
        java.util.Vector S_on = S;
        if(F.size()!=0){
            if(this.equals(Proc.MT))
                if (Proc.printDebug) System.out.println("    >>> FIXING FLAT ZONES");
            do{
                numcor++;
                double incr = Proc.calculaIncr(F_on)*Math.pow(0.9,2*(double)numcor);
                int numFlat = F_on.size();
                corrigeFlat(F_on, incr);
                java.util.Vector FSM2 = findPits2(c);
                F_on = (java.util.Vector)FSM2.get(0);
                S_on = (java.util.Vector)FSM2.get(1);
                if (F_on.size() >= numFlat) corrija2 = false;
            } while(F_on.size()!=0);
        }
        if(S_on.size()>0){
            Proc.Sink_t.addAll(S_on);
        }
        FSc=null; F_on=null; S_on=null;
    }
    
    public void corrigeUnipit(){
        if (Proc.printDebug) System.out.println("    >>> UniPit Method" );
        
        int ncol = Proc.metaDEM.getNumCols();
        int nfila = Proc.metaDEM.getNumRows();

        double min_ady = Double.MAX_VALUE;
        int contAltas = 0;
        int contPlanas=0;
        int contFalt=0;

        WorkRectangle M = this;
        
        for (int i=M.ini_i; i<=M.end_i; i++){
            for (int j=M.ini_j; j<=M.end_j; j++){
                min_ady = Double.MAX_VALUE;
                contAltas = 0;
                contPlanas=0;
                contFalt=0;
                
                /*Se busca en las ocho celdas adyacentes y se cuenta con cont el numero de ellas
                 que tienen cota corregida mayor que la de la celda (i,j). Se actualiza el minimo
                 adyacente de (i,j).*/
                
                for (int k=0; k<=8; k++){
                    if (Proc.DEM[i+(k/3)-1][j+(k%3)-1]>=0 && k!=4)
                        min_ady = Math.min(min_ady,Proc.DEM[i+(k/3)-1][j+(k%3)-1]);
                    if(Proc.DEM[i+(k/3)-1][j+(k%3)-1]>Proc.DEM[i][j])
                        contAltas++;
                    if(Proc.DEM[i+(k/3)-1][j+(k%3)-1]==Proc.DEM[i][j])
                        contPlanas++;
                    if(Proc.DEM[i+(k/3)-1][j+(k%3)-1]<0)
                        contFalt++;
                }
                /*Si las ocho celdas adyacentes a (i,j) son mas altas que ella (cont=8), se le asigna
                  la cota de la minima adyacente, y queda corregido el pit de una sola celda.*/
                if (contAltas == 8)
                    Proc.DEM[i][j] = min_ady;
                if(Proc.DEM[i][j]>0 && contFalt>0 && contPlanas+contFalt == 9)
                    Proc.DEM[i][j]=Proc.DEM[i][j]*0.9;
            }
        }
        if (Proc.printDebug) System.out.println("    >>> Done with UniPit Method" );

    }//metodo corrUnipit
    
    
    public java.util.Vector findPits(double c){
        corrigeUnipit();
        if (Proc.printDebug) {
            System.out.print("    >>> Calculating Directions on "); print_M();
            System.out.println("    --------------");
        }
        direcciones(c);
        
        java.util.Vector Pits = new java.util.Vector();
        java.util.Vector Flats = new java.util.Vector();
        java.util.Vector Sinks = new java.util.Vector();
        
        WorkRectangle M = this;
        int contgrupos=1;
        
        Pit P0 = new Pit(0, 0.0,new WorkRectangle(0,0,0,0,Proc)); 
        Pits.addElement(P0);
        P0.corregido=true;
        
        for (int i=M.ini_i; i<=M.end_i; i++){
            for (int j=M.ini_j; j<=M.end_j; j++){
                if (Proc.DEM[i][j]>=c && Proc.DIR[i][j]>=10 && Proc.DIR[i][j]<100){
                    Pit estePit = new Pit(contgrupos,Proc.DEM[i][j],new WorkRectangle(i,i,j,j,Proc));
                    Pits.addElement(estePit);
                    contgrupos++;
                    estePit.orden=Proc.sinkOrder; Proc.sinkOrder++;
                    estePit.cant=1; estePit.salida=0;
                    marquePit(i,j,estePit);
                }
            }
        }
        
        Pits.remove(P0);

        for (int n=0; n<Pits.size(); n++){
            Pit Pn = (Pit)Pits.get(n);
            if (Pn.salida == 1)
                Flats.addElement(Pn);
            else
                Sinks.addElement(Pn);
        }
        
        if (Proc.printDebug) System.out.println("    >>> There are  " + Flats.size() + " FLAT ZONES AND " + Sinks.size() + " SINKS" );
        
        java.util.Vector FindP = new java.util.Vector();
        FindP.addElement(Flats);
        FindP.addElement(Sinks);
        FindP.addElement(new Double(c));

        return FindP;
    }//Metodo findPits
    
    public java.util.Vector findPits2(double c){
        corrigeUnipit();
        if (Proc.printDebug) {
            System.out.print("    >>> Calculating Directions on "); print_M();
            System.out.println("    --------------");
        }
        direcciones(c);
        java.util.Vector Pits = new java.util.Vector();
        java.util.Vector Flats = new java.util.Vector();
        java.util.Vector Sinks = new java.util.Vector();
        java.util.Vector celdasPit = new java.util.Vector();
        WorkRectangle M = this;
        int contgrupos=1;
        Pit P0 = new Pit(0, 0.0,new WorkRectangle(0,0,0,0,Proc)); 
        Pits.addElement(P0); 
        P0.corregido=true;
        
        for (int i=M.ini_i; i<=M.end_i; i++){
            for (int j=M.ini_j; j<=M.end_j; j++){
                if (Proc.DEM[i][j]>=c && Proc.DIR[i][j]>=10 && Proc.DIR[i][j]<100){
                    Pit estePit = new Pit(contgrupos,Proc.DEM[i][j],new WorkRectangle(i,i,j,j,Proc));
                    Pits.addElement(estePit);
                    contgrupos++;
                    estePit.orden=Proc.sinkOrder; Proc.sinkOrder++;
                    estePit.cant=1; estePit.salida=0;
                    int nc = Proc.metaDEM.getNumCols();
                    int parIni = i*nc+j;
                    int par1 = i*nc+j; int par2 = i*nc+j; int par3 = i*nc+j;
                    celdasPit = new java.util.Vector();
                    int pos=-1; boolean devuelve = false;
                    do{
                        boolean salga = false ;
                        do{
                            if(!devuelve){
                                celdasPit.addElement(new Integer(par1));
                                pos = celdasPit.size()-1;
                                estePit.Mp.act_WorkRectangle(par1/nc,par1%nc);
                                //if(par1==16*nc+99) System.out.println("vealo1*************************** "+ (100*estePit.grupo + Proc.DIR[par1/nc][par1%nc])+" "+parIni/nc+" "+parIni%nc+" "+par1/nc+" "+par1%nc);
                                Proc.DIR[par1/nc][par1%nc] = 100*estePit.grupo + Proc.DIR[par1/nc][par1%nc];
                                estePit.cant++;
                                //if(parIni == 170*nc+442)System.out.println("a�ade1 "+ (par1/nc) +" "+par1%nc+" "+estePit.grupo +" "+ Proc.DIR[par1/nc][par1%nc]);
                            }
                            devuelve = false;
                            par2 = findCell(par1, estePit, nc);
                            salga = (par2 == 0);
                            if(!salga){
                                celdasPit.addElement(new Integer(par2));
                                pos = celdasPit.size()-1;
                                estePit.Mp.act_WorkRectangle(par2/nc,par2%nc);
                                Proc.DIR[par2/nc][par2%nc] = 100*estePit.grupo + Proc.DIR[par2/nc][par2%nc];
                                //if(parIni == 16*nc+99)System.out.println("ahnade2 *****************************"+ (par2/nc) +" "+par2%nc+" "+estePit.grupo + " "+ Proc.DIR[par2/nc][par2%nc]);
                                estePit.cant++;
                                par3 = findCell(par2, estePit, nc);
                                salga = (par3 == 0);
                            }
                            if(!salga) par1=par3;
    
                        }while(!salga);
                        pos--;
                        if(pos>=0){
                            par1 =  new Integer(celdasPit.get(pos).toString()).intValue();
                            devuelve = true;
                            //if(parIni == 170*nc+442)System.out.println("salga "+ (par1/nc)+" "+(par1%nc)+" "+pos+" "+celdasPit.size());
                        }
                        else par1 = parIni;
                    }while(par1 != parIni || findCell(parIni,estePit,nc)>0);
                    //if(parIni == 206*nc+190)System.out.println("sal�o");
                }
            }
        }
        Pits.remove(P0);
        for (int n=0; n<Pits.size(); n++){
            Pit Pn = (Pit)Pits.get(n);
            if (Pn.salida == 1)
                Flats.addElement(Pn);
            else
                Sinks.addElement(Pn);
        }
        if (Proc.printDebug) System.out.println("    >>> There are  " + Flats.size() + " FLAT ZONES AND " + Sinks.size() + " SINKS" );
        java.util.Vector FindP = new java.util.Vector();
        FindP.addElement(Flats);
        FindP.addElement(Sinks);
        FindP.addElement(new Double(c));
        return FindP;
    }//Metodo findPits2
    
    public void marquePit(int i, int j, Pit P){
        
        //System.out.println("en "+i+" "+j);
        P.Mp.act_WorkRectangle(i,j);
        Proc.DIR[i][j] = 100*P.grupo + Proc.DIR[i][j];
        P.cant++;
        
        for (int k=0; k <= 8; k++){
            if (P.salida==0 && Proc.DIR[i+(k/3)-1][j+(k%3)-1] < 10 && Proc.DEM[i+(k/3)-1][j+(k%3)-1] == Proc.DEM[i][j])
                P.salida = 1;
            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1] >=10 && Proc.DIR[i+(k/3)-1][j+(k%3)-1]<100 && Proc.DEM[i+(k/3)-1][j+(k%3)-1]==P.cota){
                marquePit(i+(k/3)-1,j+(k%3)-1,P);
            }
        }
        
    }
    
    public int findCell(int id,Pit P, int ncols){
        int j= id%ncols;
        int i = id/ncols;
        for (int k=0; k <= 8; k++){
            if (P.salida==0 && Proc.DIR[i+(k/3)-1][j+(k%3)-1] < 10 && Proc.DEM[i+(k/3)-1][j+(k%3)-1] == Proc.DEM[i][j])
                P.salida = 1;
            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1] >=10 && Proc.DIR[i+(k/3)-1][j+(k%3)-1]<100 && Proc.DEM[i+(k/3)-1][j+(k%3)-1]==P.cota){
                return((i+(k/3)-1)*ncols + (j+(k%3)-1));
            }
        }
        return 0;
    }
    
    
    public void direcciones(double c){
        /*Se inicializa el arreglo pend[] que llevar las pendientes en cada celda hacia las
        ocho direcciones posibles.*/
        if (Proc.printDebug) {
            java.util.Date interTime=new java.util.Date();
            System.out.println("    >>> DEM was last updated at : "+interTime.toString());
            System.out.print("    >>> Calculating Directions above: "+c+" m ("+((int)(100*(c-Proc.DEMstats.minValue)/(Proc.DEMstats.maxValue-Proc.DEMstats.minValue)))+"%), on frame: "); print_M();
            if (c!= -2) Proc.OpProc.setValueExtractionBar((int)c);
        }

        double pend[] = new double[9];
        /*Se copia una matriz cailey[][] de operaciones de direcciones entrantes en una celda.
        La tabla es de 10*10.*/
        int[][] cailey = {  {1,1,2,4,1,2,4,7,5},{1,2,2,1,2,3,4,5,3},{2,2,3,1,3,3,5,6,6},
                            {4,1,1,4,4,5,4,7,8},{1,2,3,4,5,6,7,8,9},{2,3,3,5,6,6,9,9,6},
                            {4,4,5,4,7,9,7,8,8},{7,5,6,7,8,9,8,8,9},{5,3,6,8,9,6,8,9,9}};
        
        /*Se crea un arrgelo de distancias en donde estan las distancias de una celda al centro de
        cada una de las ocho adyacentes. Esto depende de la fila de la celda.*/
        for (int i=ini_i; i<=end_i; i++){
            double[] dist = {Proc.dxy[i],Proc.dy,Proc.dxy[i],Proc.dx[i],1,Proc.dx[i],Proc.dxy[i],Proc.dy,Proc.dxy[i]};
            for (int j=ini_j; j<=end_j; j++){
                double maxpend;
                //Si no es un dato faltante:
                if(Proc.DEM[i][j]!=-1){
                    boolean toca_falt = false;
                    /*Se calcula cada una de las ocho pendientes de pend[] seg�n las celdas adyacntes y
                    el arreglo de sitancias dist[]. Si alguna de esas celdas es un dato faltante (Proc.DEM = -1),
                    se hace la variable toca_falt = true.*/
                    
                    for (int k=0; k<=8; k++){
//                        if(c==-10.0)  //SOLO LA ULTIMA VEZ!!!!!!!!
//                            pend[k] = (Proc.DEM[i][j] - Proc.DEM[i+(k/3)-1][j+(k%3)-1])/(1000*dist[k]);
//                        if(c!=-10.0)
                            pend[k] = Proc.DEM[i][j] - Proc.DEM[i+(k/3)-1][j+(k%3)-1];
                        if (Proc.DEM[i+(k/3)-1][j+(k%3)-1]==-1)
                            toca_falt = true;
                    }
                    
                    if (toca_falt) {
                        Proc.DIR[i][j]=0; //Si toca faltante, direccion=0.
                    } else { //No toca un dato faltante:
                        maxpend = Math.max(pend[8],Math.max(pend[7],Math.max(pend[6],Math.max(pend[5],Math.max(pend[3],Math.max(pend[2],Math.max(pend[1],pend[0])))))));
                        //se incializa un contador de bifrucaciones contb.
                        int contb = 0;
                        if (maxpend > 0){
                            /*Se crea el vector dirbif  que se va a llenar con direcciones de bifurcaci�n y
                            se busca en las ocho entradas de pend[]. Cuando alguna coincida con la maxpend[i][j]
                            se agrega el contador k+1 (que da las direcciones de 1 a 8 en las cuales hay igual
                            pendiente)  en el vector de bifurcaciones, y se cuenta cuantos van en contb.*/
                            java.util.Vector dirbif = new java.util.Vector();
                            for (int k=0; k <= 8; k++){
                                if (pend[k] == maxpend){
                                    contb++;
                                    dirbif.addElement(new Integer(k+1));
                                }
                            }
                            if (contb > 1){//Si hay bifurcacion.
                                /*Se crea un vector de direcciones entrantes a al celda (i,j) y se le coloca
                                un cero en la primera entrada por comodidad. Luego se busca en la ocho adyacntes
                                con el contador k, para ver cu�les drenen hacia (i,j), esto se averigua preguntando
                                si MD[i+(k/3)-1][j+(k%3)-1] == 9-k, en estos casos se anota la direccion de la
                                cual proviene el drenaje (k+1) en el vector dren.*/
                                java.util.Vector dren = new java.util.Vector(0,1); dren.addElement(new Integer(5));
                                for (int k=0; k<=8; k++){
                                    if(Proc.DIR[i+(k/3)-1][j+(k%3)-1] == 9-k)
                                        dren.addElement(new Integer(k+1));
                                }
                                /*Si alguna celda drena hacia (i,j) esto es dren.size()>1, se inicia un do en el
                                cual se toman la entradas cero y uno del vector dren y se reemplaza la uno por la
                                operacion 0*1 en la tabla de cailey, luego se remueve la 0, esto resula en que en
                                la entrada cero queda la operaci�n 0*1 y se reduce el tama�o del vector. Esto se
                                hace hasta que dren tenga s�lo un elemento. As� se operaron todas las direcciones
                                de las celdas que drenan hac�a (i,j).*/
                                if (dren.size()>1){
                                    do{
                                        int d1 = new Integer(dren.get(0).toString()).intValue();
                                        int d2 = new Integer(dren.get(1).toString()).intValue();
                                        dren.setElementAt(new Integer(cailey[d1-1][d2-1]), 1);
                                        dren.remove(0);
                                    }while (dren.size() != 1);
                                }
                                /*En el caso de que la direccion resultante de todas la operaciones, dren(0),
                                pertenezca al vector de direcciones de bifurcaci�n dirbif, se le asigna al
                                DIR[i][j] la direcci�n resultante.*/
                                if (dirbif.contains(dren.get(0)))
                                    Proc.DIR[i][j]=new Integer(dren.get(0).toString()).intValue();
                                /*Si no se resuelve por inercia, se busca un numero aleatorio*10 y se le saca
                                el modulo del tamano del vector de dirbif, esto decide que posicion de dirbif
                                se escoge y se le asigna a DIR[i][j] la direcci�n de esa posici�n.*/
                                else{//no se resuelve por inercia
                                    int r = (int)(Math.random()*10);
                                    Proc.DIR[i][j] = new Integer(dirbif.get(r%(dirbif.size())).toString()).intValue();
                                }
                            }//if hay bifurc
                            else//no bifurcacion
                                /*Si no hay bifurcaci�n, la direcci�n MD[i][j] sera simplemente la primera entrada
                                del vector dirbif.*/
                                Proc.DIR[i][j] = new Integer(dirbif.get(0).toString()).intValue();
                        }//maxpend>0
                        /*Si maxpend[i][j]==0 hay una zona plana, entonces se coloca una direccion de 10 en MD.
                        Ademas se busca en las ocho pendientes para ver sia alguna es neagtiva, o sea para ver
                        si la celda toca a una mas alta que ella, si esto pasa se coloca MD[i][j]=11.*/
                        if (maxpend == 0 && Proc.DEM[i][j]>=c){
                            Proc.DIR[i][j] = 10;
                            Loop1:
                                for (int k=0; k<9; k++){
                                    if (pend[k]<0){
                                        Proc.DIR[i][j] = 11;
                                        break Loop1;
                                    }
                                }
                        }
                    }//si no toca faltante
                }//si no es un faltante
            }//for(j)
        }//for(i)

        //if (this.is_on(2027,435)) printPedazoTemporal();
        
    }//metodo  direcciones
    
    
    public void corrigeFlat(java.util.Vector F, double incr){
        if (Proc.printDebug) System.out.println("    >>> FIXING FLAT ZONES");
        
        WorkRectangle M = this;
        corr1 = new int[M.nfilaM+2][M.ncolM+2];
        corr2 = new int[M.nfilaM+2][M.ncolM+2];
        for (int i=M.ini_i ; i<=M.end_i; i++){
            for (int j=M.ini_j; j<=M.end_j; j++){
                
                int ic=i-M.ini_i+1; 
                int jc=j-M.ini_j+1;
                
                if (Proc.DIR[i][j]%100 >=10){
                    corr1[ic][jc]++ ;
                    corr2[ic][jc]++ ;
                    Loop4:
                        for (int k=0; k <= 8; k++){
                            if (Proc.DIR[i+(k/3)-1][j+(k%3)-1]%100 < 10 && Proc.DEM[i+(k/3)-1][j+(k%3)-1] == Proc.DEM[i][j]){
                                corr1[ic][jc] = -1*corr1[ic][jc];
                                break Loop4;
                            }
                        }
                }
                /*Si (i,j) toca una celda mas alta que ella (Proc.DIR = 11):Se coloca negativa la entrada (i,j)
                de corr2.*/
                if (Proc.DIR[i][j]%100 == 11)
                    corr2[ic][jc] = -1*corr2[ic][jc];
            }
        }
        
        //Ahora en cada Flat de F
        for(int n=0; n<F.size(); n++){
            
            Pit Fn = (Pit)F.get(n);
            
            if (Fn.Mp.is_on(2027,435)) {
                System.out.print("    >>> Frame before corrFlat: ");
                M.print_M();
                //printPedazoTemporal();
            }
            
            boolean listo1 = false; boolean listo2 =  false;
            int faltan1; int faltan2;
            int max_corr2=0;
            boolean contado=false; int cantp=0;
            /*Se comienza un do que para cuando  (faltan2 = cantp && contado)|| (faltan1 + faltan2 = 0) esto
            es si: el numero que faltan por corregir segun distancia a las zonas altas es todo el pit
            (pasa unicamante cuando no hay zonas altas, i.e. una meseta) pero ya ha sido contado; o no falta
            por corregir ninguna celda ni en 1 ni en 2.*/
            int timeInloop=0;
            do{
                faltan1 = 0; faltan2 = 0;
                for (int i=Fn.Mp.ini_i ; i<=Fn.Mp.end_i; i++){
                    for (int j=Fn.Mp.ini_j; j<=Fn.Mp.end_j; j++){
                        /*
                         *Este if evita la confusion de zonas planas con el mismo grupo que estan a diferente nivel
                         */
                        if (Proc.DEM[i][j] == Fn.cota){
                            int ic=i-M.ini_i+1;
                            int jc=j-M.ini_j+1;

                            if (!contado && Proc.DIR[i][j]/100 == Fn.grupo)
                                cantp ++;
                            /*Si (i,j) es del grupo  y alguna de las dos matrices corr1 o corr2 son positivas en (i,j).
                            Recuerde que todas las del pit tienen +o- 1 en corr1 y corr2, y que las unicas que son
                            negativas son: en corr1 aquellas que tocan la salida, y en corr2 aquellas que tocan las altas.*/
                            if (Proc.DIR[i][j]/100 == Fn.grupo && (corr1[ic][jc]>0 || corr2[ic][jc]>0)){
                                /*Si corr1>0 o sea no toca la salida, se aumenta en 1 y se aumenta faltan1. De la misma
                                forma para corr2. Entonces todos los que no toquen salida (o alto) quedan positivos y
                                con uno mas en v.a que aquellos que tocan.*/
                                if (corr1[ic][jc] > 0){
                                    corr1[ic][jc] += 1;
                                    faltan1++;
                                }
                                if (corr2[ic][jc] > 0){
                                    corr2[ic][jc] += 1;
                                    faltan2++;
                                }
                                /*Se busca alrededor de (i,j). Si corr1[i][j] es positivo y toca alguno que sea negativo
                                y halla sido vuelto negativo en tandas anteriores (corr1[i][j] - Math.abs(corr1[i+(k/3)-1][j+(k%3)-1]) > 0),
                                se vuelve negativo. Notar que el valor con el que va a quedar sera, en v.a, mayor que los
                                que habian sido corregidos antes, porque lo primero que se hizo fue aumentarle uno.
                                Igual con corr2. Cada vez que esto pasa, se disminuye faltan1 (o faltan2), asi estas
                                variables cuentan cuantas celdas del pit siguen con corr1 (o corr2) positivos. */
                                for (int k=0; k <= 8; k++){
                                    if (corr1[ic][jc] > 0 && corr1[ic+(k/3)-1][jc+(k%3)-1] < 0 && corr1[ic][jc]-Math.abs(corr1[ic+(k/3)-1][jc+(k%3)-1])>0 ){
                                        corr1[ic][jc] = -1*corr1[ic][jc];
                                        faltan1--;
                                    }
                                    if (corr2[ic][jc] > 0 && corr2[ic+(k/3)-1][jc+(k%3)-1] < 0 && corr2[ic][jc] - Math.abs(corr2[ic+(k/3)-1][jc+(k%3)-1]) > 0 ){
                                        corr2[ic][jc] = -1*corr2[ic][jc];
                                        faltan2--;
                                    }
                                }
                            }
                            if (Proc.DIR[i][j]/100 == Fn.grupo)
                                max_corr2 = Math.max(Math.abs(corr2[ic][jc]), max_corr2);
                        }
                    }//for(j)
                }//for(i)
                contado = true;
                timeInloop++;

                if(timeInloop > 5000){
                    
                    if (Proc.printDebug) System.out.print("    >>> >>> No Convergence of Flat Zones Algorithm found at Frame: ");
                    if (Proc.printDebug) Fn.Mp.print_M();

                    /*System.out.println("Modified DEM up to this point:");
                    for (int i=Fn.Mp.end_i ; i>=Fn.Mp.ini_i; i--){
                        for (int j=Fn.Mp.ini_j ; j<=Fn.Mp.end_j; j++){
                            System.out.print(Proc.DEM[i][j]+" ");
                        }
                        System.out.println();
                    }

                    System.out.println();
                    System.out.println("Direction Matrix up to this point:");
                    for (int i=Fn.Mp.end_i ; i>=Fn.Mp.ini_i; i--){
                        for (int j=Fn.Mp.ini_j ; j<=Fn.Mp.end_j; j++){
                            System.out.print(Proc.DIR[i][j]+" ");
                        }
                        System.out.println();
                    }

                    System.out.println();
                    System.out.println("Corr1 Matrix:");
                    for (int i=Fn.Mp.end_i ; i>=Fn.Mp.ini_i; i--){
                        for (int j=Fn.Mp.ini_j ; j<=Fn.Mp.end_j; j++){
                            int ic=i-M.ini_i+1;
                            int jc=j-M.ini_j+1;
                            System.out.print(corr1[ic][jc]+" ");
                        }
                        System.out.println();
                    }

                    System.out.println();
                    System.out.println("Corr2 Matrix:");
                    for (int i=Fn.Mp.end_i ; i>=Fn.Mp.ini_i; i--){
                        for (int j=Fn.Mp.ini_j ; j<=Fn.Mp.end_j; j++){
                            int ic=i-M.ini_i+1;
                            int jc=j-M.ini_j+1;
                            System.out.print(corr2[ic][jc]+" ");
                        }
                        System.out.println();
                    }
                    
                    /*System.out.println();
                    System.out.println(Proc.DIR[16][99]);
                    System.out.println(Proc.DIR[16][98]);

                    System.exit(0);*/

                    for (int i=Fn.Mp.end_i ; i>=Fn.Mp.ini_i; i--){
                        for (int j=Fn.Mp.ini_j ; j<=Fn.Mp.end_j; j++){
                            int ic=i-M.ini_i+1;
                            int jc=j-M.ini_j+1;
                            if(corr1[ic][jc]>0){
                                corr1[ic][jc]=0;
                                corr2[ic][jc]=0;
                            }
                        }
                    }
                    break;
                }

                //}while ((faltan2 != cantp || !contado) && (faltan1 + faltan2 != 0)); EN PRUEBAS !!!!!!!
            } while ((faltan2 != cantp || faltan1 != 0) && (faltan1 + faltan2 != 0));
            
            for (int i=Fn.Mp.ini_i ; i<=Fn.Mp.end_i; i++){
                for (int j=Fn.Mp.ini_j; j<=Fn.Mp.end_j; j++){
                    int ic=i-M.ini_i+1; int jc=j-M.ini_j+1;
                    if (Proc.DIR[i][j]/100 == Fn.grupo && Proc.DEM[i][j] == Fn.cota){
                        if (corrija2) corr2[ic][jc] = (max_corr2 - Math.abs(corr2[ic][jc])+1);
                        else  corr2[ic][jc] = 0;
                    }
                }
            }
            for (int i=Fn.Mp.ini_i ; i<=Fn.Mp.end_i; i++){
                for (int j=Fn.Mp.ini_j; j<=Fn.Mp.end_j; j++){
                    int ic=i-M.ini_i+1; int jc=j-M.ini_j+1;
                    if(Proc.DIR[i][j]/100==Fn.grupo && Proc.DEM[i][j] == Fn.cota)
                        Proc.DEM[i][j]= Proc.DEM[i][j]+(incr)*(corr2[ic][jc]-corr1[ic][jc]);
                }
            }
            /*if (Fn.Mp.is_on(2027,435)) {
                System.out.print("    >>> Frame after corrFlat: ");
                M.print_M();
                printPedazoTemporal();
                System.out.println();
                System.out.println("CORR 1");
                for(int i=2030-M.ini_i;i>2020-M.ini_i;i--){
                    for(int j=430-M.ini_j;j<440-M.ini_j;j++){
                        if(i < 0 || j< 0 || i>=corr2.length || j>=corr2[0].length)
                           System.out.print("XXX ");
                        else
			   System.out.print(corr1[i][j]+" ");
                    }
                    System.out.println();
                }
                System.out.println("CORR 2");
                for(int i=2030-M.ini_i;i>2020-M.ini_i;i--){
                    for(int j=430-M.ini_j;j<440-M.ini_j;j++){
                        if(i < 0 || j< 0 || i>=corr2.length || j>=corr2[0].length)
			   System.out.print("XXX ");
			else
			   System.out.print(corr2[i][j]+" ");
                    }
                    System.out.println();
                }
            }*/

        }//for(n)
        
        
        
        
        
        corr1=null; corr2=null;
        if (Proc.printDebug) System.out.println("    >>> FLAT ZONES FIXED");
        
    }//M�todo corrFlat
    
    
    public void act_WorkRectangle(int i, int j){
        ini_i = Math.min(ini_i,i);
        ini_j = Math.min(ini_j,j);
        end_i = Math.max(end_i,i);
        end_j = Math.max(end_j,j);
        nfilaM = end_i-ini_i+1;
        ncolM = end_j-ini_j+1;
    }
    
    public void add_WorkRectangle(WorkRectangle M2){
        act_WorkRectangle(M2.ini_i,M2.ini_j);
        act_WorkRectangle(M2.end_i,M2.end_j);
    }
    
    public WorkRectangle def_WorkRectangle(int d, int nfila, int ncol){
        act_WorkRectangle(Math.max(2,ini_i-d), Math.max(2,ini_j-d));
        act_WorkRectangle(Math.min(end_i+d,nfila),Math.min(end_j+d,ncol));
        return this;
    }
    
    public void print_M(){
        if (Proc.printDebug) System.out.println("    Yi="+ini_i + "  Yf=" +end_i + "  Xi=" +ini_j + "  Xf=" +end_j);
    }
    
    public boolean is_on(int i , int j){
        if(i<=end_i && i>=ini_i && j<=end_j && j>=ini_j)
            return true;
        else
            return false;
    }
    
    /*public void printPedazoTemporal(){
        
        System.out.println("**********************");
        System.out.println("Piece of DEM and DIR up to this point");
        System.out.println();
        java.text.NumberFormat number4 = java.text.NumberFormat.getNumberInstance();
        java.text.DecimalFormat dpoint4 = (java.text.DecimalFormat)number4;
        dpoint4.applyPattern("00000.00000000000000000");

        for(int i=2030;i>2020;i--){
            for(int j=430;j<440;j++){
                System.out.print(dpoint4.format(Proc.DEM[i][j])+" ");
            }
            System.out.println();
        }
        
        System.out.println();
        for(int i=2030;i>2020;i--){
            for(int j=430;j<440;j++){
                System.out.print(Proc.DIR[i][j]+" ");
            }
            System.out.println();
        }
        System.out.println();
        
    }*/
    
    
    
    /*public void print_M(WorkRectangle M1, boolean s, int[][] D, int c){
        System.out.println(M1.ini_i + "  " +M1.end_i + "  " +M1.ini_j + "  " +M1.end_j);
        if(s){
            WorkRectangle M2 = new WorkRectangle(M1.ini_i-c,M1.end_i+c,M1.ini_j-c,M1.end_j+c,M1.Proc);
            System.out.println("This  "+M2.ini_i + "  " +M2.end_i + "  " +M2.ini_j + "  " +M2.end_j);
            for (int i=M2.ini_i; i<=M2.end_i; i++){
                System.out.print("\n" + i + " " );
                for (int j=M2.ini_j; j<=M2.end_j; j++){
                    System.out.print(D[i][j]+ "  ");
                }
            }
            System.out.print("\n");
        }
    }
     
    public void print_M(WorkRectangle M1, boolean s, double[][] D, int c){
        System.out.println(M1.ini_i + "  " +M1.end_i + "  " +M1.ini_j + "  " +M1.end_j);
        if(s){
            WorkRectangle M2 = new WorkRectangle(M1.ini_i-c,M1.end_i+c,M1.ini_j-c,M1.end_j+c,M1.Proc);
            System.out.println("This  "+M2.ini_i + "  " +M2.end_i + "  " +M2.ini_j + "  " +M2.end_j);
            for (int i=M2.ini_i; i<=M2.end_i; i++){
                System.out.print("\n" + i + " " );
                for (int j=M2.ini_j; j<=M2.end_j; j++){
                    System.out.print(D[i][j]+ "  ");
                }
            }
            System.out.print("\n");
        }
    }*/
    
}
