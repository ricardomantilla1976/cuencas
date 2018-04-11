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
 * This class implements a ROM version of the the downstream-travel algorithm over the network to
 * calculate important netwotk features (geomorphical and topological) for all locations
 * in the DEM.  All the methods in this class are static which makes this class a
 * toolbox that can be extended to include new analysis
 * @author Jorge Mario Ramirez and Ricardo Mantilla
 */
public class GetGeomorphologyROM extends Object {
    
    private int GEO[][];
    //private int MD[][];
    //private byte RedRas[][];
    private int llegan[][];
    private double dy;
    private double dx[];
    private double dxy[];
    private hydroScalingAPI.io.MetaRaster MR;
    private java.io.RandomAccessFile Dir;
    private java.io.RandomAccessFile Orden;
    private java.io.RandomAccessFile Ltc;
    private java.io.RandomAccessFile Lcp;
    private java.io.RandomAccessFile Magn;
    private java.io.RandomAccessFile Dtopo;
    private java.io.RandomAccessFile Red;
    private java.io.RandomAccessFile Tcd;
    private java.io.RandomAccessFile Mcd;
    private WorkRectangle M;
    
    int nr,nc;
    
    private hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule Proc;
    
    
    /**
     * Tests for the class
     * @param arguments the command line arguments
     */
    private void inicio(){
        
        try{
            System.out.println(">>> Initialization Process Starts <<<");
            
            String rutaMR = MR.getLocationBinaryFile().getPath();
            String rutaDatos = rutaMR.substring(0,rutaMR.lastIndexOf("."));
            
            String[] destinations ={".horton",".magn",".ltc",".lcp",".dtopo",".tcd",".mcd"};
                                
            java.io.BufferedOutputStream buffOuts[] = new java.io.BufferedOutputStream[destinations.length];
            java.io.DataOutputStream outs[] = new java.io.DataOutputStream[destinations.length] ;

            for(int k=0 ; k<destinations.length ; k++){
                buffOuts[k]=new java.io.BufferedOutputStream(new java.io.FileOutputStream(rutaDatos+destinations[k]));
                outs[k] = new java.io.DataOutputStream(buffOuts[k]);
            }

            float missF=new Float(MR.getMissing()).floatValue();
            int   missI=new Integer(MR.getMissing()).intValue();

            GEO= new int[nr][nc];
            llegan= new int[nr][nc];
            dy = 6378.0*MR.getResLon()*Math.PI/(3600.0*180.0);
            dx = new double[nr+1];
            dxy = new double[nr+1];
            M = new WorkRectangle(0,nr-1,0,nc-1);
            
            for(int i=0; i<nr; i++){
                for(int j=0; j<nc; j++){
                    if(Proc.DIR[i+1][j+1]==0){
                        outs[0].writeByte((byte)-10);
                        outs[1].writeInt(missI);
                        outs[2].writeFloat(missF);
                        outs[3].writeFloat(missF);
                        outs[4].writeInt(missI);
                        outs[5].writeFloat(missF);
                        outs[6].writeFloat(missF);
                    }
                    else{
                        outs[0].writeByte((byte)-1);
                        if(Proc.RedRas[i+1][j+1] == 0) outs[1].writeInt(-1); else outs[1].writeInt(0);
                        outs[2].writeFloat(0.0f);
                        outs[3].writeFloat(0.0f);
                        outs[4].writeInt(0);
                        outs[5].writeFloat(0.0f);
                        outs[6].writeFloat(0.0f);
                    }
                }
            }
            
            for(int k=0 ; k<destinations.length ; k++) buffOuts[k].close() ;

            Orden = new java.io.RandomAccessFile(rutaDatos + ".horton","rw");
            Magn = new java.io.RandomAccessFile(rutaDatos + ".magn","rw");
            Ltc = new java.io.RandomAccessFile(rutaDatos + ".ltc","rw");
            Lcp = new java.io.RandomAccessFile(rutaDatos + ".lcp","rw");
            Dtopo = new java.io.RandomAccessFile(rutaDatos + ".dtopo","rw");
            Tcd = new java.io.RandomAccessFile(rutaDatos + ".tcd","rw");
            Mcd = new java.io.RandomAccessFile(rutaDatos + ".mcd","rw");
            
            for(int i=0; i<nr; i++){
                dx[i] = 6378*Math.cos((i*MR.getResLat()/3600.0 + MR.getMinLat())*Math.PI/180.0)*MR.getResLat()*Math.PI/(3600.0*180.0);
                dxy[i] = Math.sqrt(dx[i]*dx[i] + dy*dy);
                for(int j=0; j<nc; j++){
                    int nllegan = 0;
                    for (int k=0; k <= 8; k++){
                        if(M.is_on(i+(k/3)-1,j+(k%3)-1)){
                            if (Proc.DIR[1+i+(k/3)-1][1+j+(k%3)-1]==9-k && Proc.RedRas[1+i+(k/3)-1][1+j+(k%3)-1]>0)
                                nllegan++;
                        }
                    }
                    if(Proc.RedRas[i+1][j+1]<=0)
                        GEO[i][j]= -2;
                    else if (Proc.DIR[i+1][j+1]>0){
                        GEO[i][j] = nllegan;
                    }
                    else{
                        GEO[i][j]= -3;
                    }
                    llegan[i][j]=nllegan;
                }
            }
            
        }catch (java.io.IOException e1){
            System.err.println("ERROR4** "+e1.getMessage());
        }
        System.out.println(">>> Initializtion Process Completed <<<");
    }
    
    //-------------------------------------------------------------------------------------------------------------------------
    /**
     * Creates and instance of the ROM based geomorphology class
     * @param MR1 The MetaRaster of the DEM to be processed
     */
    public GetGeomorphologyROM(hydroScalingAPI.io.MetaRaster MR1, hydroScalingAPI.modules.networkExtraction.objects.NetworkExtractionModule pr) {
        
        MR=MR1;

        Proc=pr;
        
        nr=MR.getNumRows();
        nc=MR.getNumCols();

        inicio();
        int contn;
            
        do{
            contn = 0;
            for(int i=0; i<nr ; i++){
                for (int j=0; j<nc; j++){
                    if (GEO[i][j] < 0)
                        contn++;
                    if (GEO[i][j] == 0){
                        GEOcero(i,j);
                    }//en cada cero
                }//for j
            }//for i
            
            System.out.println(">>> negatives are: "+contn+" which is: "+contn/(float)((GEO.length)*(GEO[0].length))*100+"%");
            
        }while(contn < (GEO.length)*(GEO[0].length) );
        
    }
    
    //---------------------------------------------------------------------------------------------------------------------------
    
    void GEOcero(int i, int j) {
        
                int x1=0,y1=0;
		try {
			double[] dist = { dxy[i], dy, dxy[i], dx[i], 1, dx[i], dxy[i], dy,
					dxy[i] };
			boolean needs_geocero = true;
			int count_stream_cells =0;
			while (needs_geocero) {
				GEO[i][j] = -1; // de entrada asigna -1 a la matriz GEO en esa
								// posicion -> la resuelve
				// deberia resolverla cuando este acabado todo el procedimiento
				// ??
				esc("Ltc", i, j, true, (float) dist[Proc.DIR[i + 1][j + 1] - 1]);// escribe
																					// ltc
				esc("Lcp", i, j, true, (float) dist[Proc.DIR[i + 1][j + 1] - 1]);// escribe
																					// lcp
				if (llegan[i][j] == 0) { // si no le llega ninguna (si es
											// origen) resuelve su magn, orden y
											// dtopo
					esc("Magn", i, j, false, (int) 1);
					esc("Orden", i, j, false, (byte) 1);
					esc("Dtopo", i, j, false, (int) 1);
				}
                                
                                x1 = i - 1 + (Proc.DIR[i + 1][j + 1] - 1) / 3;
				y1 = j - 1 + (Proc.DIR[i + 1][j + 1] - 1) % 3; // x1,y1 son
																	// las
																	// vecinas
																	// aguas
																	// abajo
				if (GEO[x1][y1] >= 0) { // si a la vecina aguas abajo no se le
										// ha resuelto su geomorfologia (GEO
										// >=0)
					GEO[x1][y1]--; // le descuenta uno a la GEO de la vecina,
									// producto de haber resuelto la de aguas
									// arriba
                                        
                                        long offset1=((long)4 * ((long)i * (long)nc + (long)j));
                                        long offset2=((long)4 * ((long)x1 * (long)nc + (long)y1));
                                        
					Magn.seek(offset1);
					int m2 = Magn.readInt();
					Ltc.seek(offset1);
					float lt2 = Ltc.readFloat();
					Lcp.seek(offset1);
					float lc2 = Lcp.readFloat();
					Lcp.seek(offset2);
					float lc3 = Lcp.readFloat();
					// estas cuatro lineas anteriores se pueden guardar sus
					// valores en variables para ahorrarle teimpo al seek ?
					if (GEO[x1][y1] == 0) // si a la vecina aguas abajo
											// unicamente le llegaba una,
											// entonces ya ha sido resuelta y se
											// puede escribir su magn
						esc("Magn", x1, y1, true, m2);
					else
						// si a la vecina aguas abajo aun le llegan celdas, se
						// le suma m2, lt2 y lc2/3 al archivo en la posicion
						// x1,y1
						esc("Magn", x1, y1, true, m2);
					esc("Ltc", x1, y1, true, lt2);
					esc("Lcp", x1, y1, false, Math.max(lc2, lc3));
				}

				if (llegan[i][j] > 0) { // si a la celda ij que estoy analizando
					java.util.Vector diamt = new java.util.Vector(0, 1);
					java.util.Vector ord = new java.util.Vector(0, 1);
					for (int k = 0; k <= 8; k++) {
						if (Proc.DIR[1 + i + (k / 3) - 1][1 + j + (k % 3) - 1] == 9 - k
								&& Proc.RedRas[1 + i + (k / 3) - 1][1 + j
										+ (k % 3) - 1] != 0) {
                                                    
                                                        long offset3=(((long)i + ((long)k / 3) - 1) * (long)nc + ((long)j + ((long)k % 3) - 1));
                                                        
							Orden.seek(offset3);
							
                                                        Dtopo.seek(4*offset3);
							ord.addElement(new Byte(Orden.readByte()));
							diamt.addElement(new Integer(Dtopo.readInt()));
						}
					}
					java.util.Collections.sort(ord);
					java.util.Collections.sort(diamt);
					if (llegan[i][j] > 1)
						esc("Dtopo", i, j, false,
								1 + new Integer(diamt.get(diamt.size() - 1)
										.toString()).intValue());
					if (llegan[i][j] == 1)
						esc("Dtopo", i, j, false,
								new Integer(diamt.get(diamt.size() - 1)
										.toString()).intValue());
					if (ord.size() > 1) {
						if (ord.get(ord.size() - 1).equals(
								ord.get(ord.size() - 2))) {
							esc("Orden", i, j, false,
									(byte) (1 + new Byte(ord
											.get(ord.size() - 1).toString())
											.byteValue()));
							Proc.RedRas[i + 1][j + 1] = -1;
						} else {
							esc("Orden",
									i,
									j,
									false,
									new Byte(ord.get(ord.size() - 1).toString())
											.byteValue());
						}
					}
					if (ord.size() == 1) {
						esc("Orden", i, j, false, new Byte(ord.get(0)
								.toString()).byteValue());
					}
				}
				//si aun hay celdas aguas abajo
				if(llegan[x1][y1]==1 && Proc.DIR[x1+1][y1+1]>0){
				//reinicia ij para continuar el while
				i=x1;j=y1;
				count_stream_cells++;
				}
				else{
					needs_geocero=false;
				}
				
			}// needs geocero
//			System.out.println("GEOCERO ROM non-recursive DONE "+count_stream_cells+" cells in stream");
//			if (llegan[x1][y1] == 1 && Proc.DIR[x1 + 1][y1 + 1] > 0) {
//				GEOcero(x1, y1, contn);
//			}
			
		} catch (java.io.IOException e1) {
			System.err.println("ERROR** " + e1.getMessage() +" started it "+i+" "+j+" failed at "+x1+" "+y1+ "nc:" +nc+" calculation1: "+(4 * (i * nc + j))+" calculation2: "+(4 * (x1 * nc + y1)));
		}
	}
    
    private void esc(String s, int i,int j, boolean m, byte v){
        try{
            long offset1=(((long)i * (long)nc + (long)j));
            if (s.equals("Orden")){
                Orden.seek(offset1);
                if (m){
                    byte d=Orden.readByte();
                    Orden.seek(offset1);
                    Orden.writeByte(v+d);
                }
                else  Orden.writeByte(v);
            }
        }catch (java.io.IOException e1){
            System.err.println("ERROR1** "+e1.getMessage());}
    }
    
    private void esc(String s, int i,int j, boolean m, float v){
        try{
            long offset1=((long)4 * ((long)i * (long)nc + (long)j));
            if (s.equals("Ltc")){
                Ltc.seek(offset1);
                if (m){
                    float d=Ltc.readFloat();
                    Ltc.seek(offset1);
                    Ltc.writeFloat(v+d);
                }
                else  Ltc.writeFloat(v);
            }
            if (s.equals("Lcp")){
                Lcp.seek(offset1);
                if (m){
                    float d=Lcp.readFloat();
                    Lcp.seek(offset1);
                    Lcp.writeFloat(v+d);
                }
                else  Lcp.writeFloat(v);
            }
        }catch (java.io.IOException e1){
            System.err.println("ERROR2** "+e1.getMessage());}
    }
    
    private void esc(String s, int i,int j, boolean m, int v){
        try{
            long offset1=((long)4 * ((long)i * (long)nc + (long)j));
            if (s.equals("Dtopo")){
                Dtopo.seek(offset1);
                if (m){
                    int d=Dtopo.readInt();
                    Dtopo.seek(offset1);
                    Dtopo.writeInt(v+d);
                }
                else  Dtopo.writeInt(v);
            }
            if (s.equals("Magn")){
                Magn.seek(offset1);
                if (m){
                    int d=Magn.readInt();
                    Magn.seek(offset1);
                    Magn.writeInt(v+d);
                }
                else
                    Magn.writeInt(v);
            }
        }catch (java.io.IOException e1){
            System.err.println("ERROR3** "+e1.getMessage());}
    }
    
    public static void main(String args[]){
        
        System.out.println((long)4*((long)18058*(long)29730+(long)2818)+""); 
        System.out.println((long)4*((long)18059*(long)29730+(long)2818)+""); 
        
    }
    
    
}
