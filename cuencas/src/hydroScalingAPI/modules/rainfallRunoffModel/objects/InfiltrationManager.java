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
 * InfiltrationManager.java
 *
 * Created on May 28, 2004, 2:48 PM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 * This is a primitive implementation of a dynamic InfiltrationManager.  This class
 * provides information for the average infiltration rate at time t for a hillslope
 * @author Ricardo Mantilla
 */
public class InfiltrationManager {
    
    private hydroScalingAPI.io.MetaRaster metaInfilt;
    private float[] infiltrationValue;
    int[][] matrizPintada;

    /**
     * Creates a new instance of InfiltrationManager (with constant infiltration rate
     * over the basin)
     * @param linksStructure The topologic structure of the river network
     * @param infiltRate A constant value of infiltration for the entire basin
     */
    public InfiltrationManager(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, float infiltRate) {
        
        infiltrationValue=new float[linksStructure.connectionsArray.length];
        java.util.Arrays.fill(infiltrationValue,infiltRate);
        
    }
    
    /**
     * Creates a new instance of InfiltrationManager (with spatially variable infiltration
     * rate over the basin)
     * @param myCuenca The {@link hydroScalingAPI.util.geomorphology.objects.Basin} object describing the
     * basin under consideration
     * @param linksStructure The topologic structure of the river network
     * @param metaInfilt A MetaRaster describing the infiltration map
     * @param matDir The directions matrix of the DEM that contains the basin
     * @param magnitudes The magnitudes matrix of the DEM that contains the basin
     */
    public InfiltrationManager(hydroScalingAPI.util.geomorphology.objects.Basin myCuenca, hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure, hydroScalingAPI.io.MetaRaster infiltMetaRasterOR, byte[][] matDir, int[][] magnitudes) {
        
        //Una vez leidos los archivos:
        //Lleno la matriz de direcciones
        
        int[][] matDirBox=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
        
        for (int i=1;i<matDirBox.length-1;i++) for (int j=1;j<matDirBox[0].length-1;j++){
            matDirBox[i][j]=(int) matDir[i+myCuenca.getMinY()-1][j+myCuenca.getMinX()-1];
        }
        
        try{
            
            metaInfilt=infiltMetaRasterOR;
            
            /****** OJO QUE ACA PUEDE HABER UN ERROR (POR LA CUESTION DE LA COBERTURA DEL MAPA SOBRE LA CUENCA)*****************/
            if (metaInfilt.getMinLon() > metaInfilt.getMinLon()+myCuenca.getMinX()*metaInfilt.getResLon()/3600.0 ||
                metaInfilt.getMinLat() > metaInfilt.getMinLat()+myCuenca.getMinY()*metaInfilt.getResLat()/3600.0 ||
                metaInfilt.getMaxLon() < metaInfilt.getMinLon()+(myCuenca.getMaxX()+2)*metaInfilt.getResLon()/3600.0 ||
                metaInfilt.getMaxLat() < metaInfilt.getMinLat()+(myCuenca.getMaxY()+2)*metaInfilt.getResLat()/3600.0) {
                    System.out.println("Not Area Coverage");
                    return;
            }
            
            int xOulet,yOulet;
            hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;
            
            matrizPintada=new int[myCuenca.getMaxY()-myCuenca.getMinY()+3][myCuenca.getMaxX()-myCuenca.getMinX()+3];
            
            for (int i=0;i<linksStructure.contactsArray.length;i++){
                if (linksStructure.magnitudeArray[i] < linksStructure.basinMagnitude){

                    xOulet=linksStructure.contactsArray[i]%metaInfilt.getNumCols();
                    yOulet=linksStructure.contactsArray[i]/metaInfilt.getNumCols();

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaInfilt);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=i+1;
                    }
                } else {
                    
                    xOulet=myCuenca.getXYBasin()[0][0];
                    yOulet=myCuenca.getXYBasin()[1][0];

                    myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDir,magnitudes,metaInfilt);
                    for (int j=0;j<myHillActual.getXYHillSlope()[0].length;j++){
                        matrizPintada[myHillActual.getXYHillSlope()[1][j]-myCuenca.getMinY()+1][myHillActual.getXYHillSlope()[0][j]-myCuenca.getMinX()+1]=i+1;
                    }
                }
            }
            
            infiltrationValue=new float[linksStructure.contactsArray.length];
            
            double[] evalSpot;
            float[][] dataSnapShot;
            int MatX,MatY;
            
            double[] dx;
            double dy;
            
            dy = 6378.0*metaInfilt.getResLat()*Math.PI/(3600.0*180.0);
            dx = new double[metaInfilt.getNumRows()];
            /*Se calcula para cada fila del DEMC el valor de la distancia horizontal del pixel 
              y la diagonal, dependiendo de la latitud.*/
            int nr=metaInfilt.getNumRows();
            for (int i=0 ; i<nr ; i++){
              dx[i] = 6378.0*Math.cos(((i+1)*metaInfilt.getResLat()/3600.0 + metaInfilt.getMinLat())*Math.PI/180.0)*metaInfilt.getResLat()*Math.PI/(3600.0*180.0);
            }
            float[][] upAreaValues=linksStructure.getVarValues(0);
            
            //for (int i=0;i<upAreaValues[0].length;i++) System.out.print(upAreaValues[0][i]+" ");
            //System.out.println("");
            
            //System.out.println("-----------------Start of Files Reading----------------");
            
            metaInfilt.setLocationBinaryFile(new java.io.File(metaInfilt.getLocationMeta().getPath().substring(0,metaInfilt.getLocationMeta().getPath().lastIndexOf(".metaVHC"))+".vhc"));
            dataSnapShot=new hydroScalingAPI.io.DataRaster(metaInfilt).getFloat();

            //recorto la seccion que esta en la cuenca (TIENE QUE CONTENERLA)

            double demMinLon=metaInfilt.getMinLon();
            double demMinLat=metaInfilt.getMinLat();
            double demResLon=metaInfilt.getResLon();
            double demResLat=metaInfilt.getResLat();

            int basinMinX=myCuenca.getMinX();
            int basinMinY=myCuenca.getMinY();

            double infiltMinLon=metaInfilt.getMinLon();
            double infiltMinLat=metaInfilt.getMinLat();
            double infiltResLon=metaInfilt.getResLon();
            double infiltResLat=metaInfilt.getResLat();


            for (int j=0;j<matrizPintada.length;j++) for (int k=0;k<matrizPintada[0].length;k++){
                evalSpot=new double[] {demMinLon+(basinMinX+k-1)*demResLon/3600.0,
                                       demMinLat+(basinMinY+j-1)*demResLat/3600.0};

                MatX=(int) Math.round((evalSpot[0]-infiltMinLon)/infiltResLon*3600.0);
                MatY=(int) Math.round((evalSpot[1]-infiltMinLat)/infiltResLat*3600.0);

                if (matrizPintada[j][k] > 0){
                    infiltrationValue[matrizPintada[j][k]-1]+=dataSnapShot[MatY][MatX]*(double)(dy*dx[j+basinMinY-1]);
                }

            }
            
            for (int j=0;j<linksStructure.contactsArray.length;j++){
                infiltrationValue[j]/=upAreaValues[0][j];
            }

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
        }
    }
    
    /**
     * Returns the value of infiltration rate in mm/h for a given moment of time
     * @param HillNumber The index of the desired hillslope
     * @return The value of infiltration intensity
     */
    public float getInfiltrationOnHillslope(int HillNumber){
        
        return infiltrationValue[HillNumber];
        
    }
    
    public void randomizeValues(){
        float baseInfiltration=infiltrationValue[0];
        for (int i = 0; i < infiltrationValue.length; i++) {
            infiltrationValue[i]=(float)(baseInfiltration+20*Math.random());

        }
    }
    
}
