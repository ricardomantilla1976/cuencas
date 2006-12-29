/*
 * RsnTile.java
 *
 * Created on November 13, 2001, 11:25 AM
 */

package hydroScalingAPI.modules.networkAnalysis.objects;

/**
 *
 * @author Ricardo Mantilla
 */
public class RsnTile extends java.lang.Object {
    
    private java.util.Vector idsTile=new java.util.Vector(), dToOutlet=new java.util.Vector();
    
    private int[][] xyRsnTile;
    private float[][] xyContour;
    private float[][] LonLatRsnTile;
    private int minX,maxX,minY,maxY;
    private int thisTileOrder;
    private hydroScalingAPI.io.MetaRaster localMetaRaster;
    
    /** Creates new hillSlope */
    public RsnTile(int xT, int yT, int xH, int yH, byte[][] fullDirMatrix, byte[][] order, hydroScalingAPI.io.MetaRaster mr,int tileScale) {
        
        //assumes unpruned directions matrix
        
        localMetaRaster=mr;
        
        minX=fullDirMatrix[0].length-1;
        maxX=0;
        minY=fullDirMatrix.length-1;
        maxY=0;
        
        //System.out.println("Limits: "+magnitudes.length+" "+magnitudes[0].length);
        //System.out.println("Tile Outlet: "+xT+" "+yT+" "+thisLinkMagn);
        
        idsTile.add(new int[] {xT,yT});
        if(xT==xH && yT==yH){
        } else getTile(yT,xT,yH,xH,fullDirMatrix);
        
        if (xH != -1 && yH!= -1) {
            thisTileOrder=tileScale;
            idsTile.add(new int[] {xH,yH});
            getHill(yH,xH,fullDirMatrix, order);
        }
        LonLatRsnTile=new float[2][idsTile.size()];
        xyRsnTile=new int[2][idsTile.size()];
        
        for (int k=0; k <idsTile.size(); k++){
            int[] xys=(int[]) idsTile.elementAt(k);
            LonLatRsnTile[0][k]=(float) (xys[0]*localMetaRaster.getResLon()/3600.0f+localMetaRaster.getMinLon());
            xyRsnTile[0][k]=xys[0];
            minX=Math.min(minX,xys[0]); maxX=Math.max(maxX,xys[0]);
            LonLatRsnTile[1][k]=(float) (xys[1]*localMetaRaster.getResLat()/3600.0f+localMetaRaster.getMinLat());
            xyRsnTile[1][k]=xys[1];
            minY=Math.min(minY,xys[1]); maxY=Math.max(maxY,xys[1]);
        }
        
        idsTile=null;
        
        findRsnTileDivide();
    }
    
    public void getTile(int i,int j,int iEnd, int jEnd, byte [][] fullDirMatrix){
        for (int k=0; k <= 8; k++){
            if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k && ((i+(k/3)-1)*fullDirMatrix[0].length+(j+(k%3)-1) != iEnd*fullDirMatrix[0].length+jEnd)){
                idsTile.add(new int[] {j+(k%3)-1,i+(k/3)-1});
                getTile(i+(k/3)-1, j+(k%3)-1, iEnd, jEnd,fullDirMatrix);
            }
        }
    }
    
    public void getHill(int i,int j,byte [][] fullDirMatrix,byte[][] order){
        for (int k=0; k <= 8; k++){
            if (fullDirMatrix[i+(k/3)-1][j+(k%3)-1] == 9-k && order[i+(k/3)-1][j+(k%3)-1] < (thisTileOrder-1)){
                idsTile.add(new int[] {j+(k%3)-1,i+(k/3)-1});
                getHill(i+(k/3)-1,j+(k%3)-1, fullDirMatrix,order);
            }
        }
    }
    
    public float[][] getLonLatRsnTile(){
        return LonLatRsnTile;
    }
    
    public int[][] getXYRsnTile(){
        return xyRsnTile;
    }
    
    public visad.Gridded2DSet getRsnTileDivide() throws visad.VisADException {
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        visad.Gridded2DSet tileCountour=new visad.Gridded2DSet(domain,xyContour,xyContour[0].length);
        
        return tileCountour;
    }
    
    public void findRsnTileDivide(){
        
        //Relleno la matriz con 0's y 1's
        
        int[][] matrizBusqueda=new int[maxY-minY+5][maxX-minX+5];
        for (int j=0;j<xyRsnTile[0].length;j++){
            matrizBusqueda[xyRsnTile[1][j]-minY+2][xyRsnTile[0][j]-minX+2]=1;
        }
        
        //Determino la direccion inicial de busqueda
        
        int dirSalida=0;
        int outX=xyRsnTile[0][0]-minX+2;
        int outY=xyRsnTile[1][0]-minY+2;
        
        if (matrizBusqueda[outY+0][outX+1]==0){
            dirSalida=180;
            outX=outX+1;
            outY=outY+0;
        } else  if (matrizBusqueda[outY+0][outX-1]==0){
            dirSalida=0;
            outX=outX-1;
            outY=outY+0;
        } else  if (matrizBusqueda[outY+1][outX+0]==0){
            dirSalida=270;
            outX=outX+0;
            outY=outY+1;
        } else  if (matrizBusqueda[outY-1][outX+0]==0){
            dirSalida=90;
            outX=outX+0;
            outY=outY-1;
        }
        
        int searchPointX=outX;
        int searchPointY=outY;
        
        java.util.Vector idsContorno=new java.util.Vector();
        
        int foreX;
        int foreY;
        
        do{
            do{
                dirSalida+=90;
                foreX=searchPointX+(int) Math.round(Math.cos(dirSalida*Math.PI/180.0));
                foreY=searchPointY+(int) Math.round(Math.sin(dirSalida*Math.PI/180.0));
            } while(matrizBusqueda[foreY][foreX] == 1);
            matrizBusqueda[foreY][foreX]=8;
            
            for (int dirVec=-90;dirVec<=180;dirVec+=90){
                int lookX=foreX+(int) Math.round(Math.cos((dirSalida+dirVec)*Math.PI/180.0));
                int lookY=foreY+(int) Math.round(Math.sin((dirSalida+dirVec)*Math.PI/180.0));
                if (matrizBusqueda[lookY][lookX]==1){
                    idsContorno.add(new int[] {foreX+(int) Math.round(Math.abs(Math.cos(((dirSalida+dirVec-270)-45)*Math.PI/360.0))),foreY+(int) Math.round(Math.abs(Math.cos(((dirSalida+dirVec)-45)*Math.PI/360.0)))});
                } else break;
            }
            searchPointX=foreX;
            searchPointY=foreY;
            dirSalida-=180;
        } while(foreX!=outX || foreY!=outY);
        
        idsContorno.add(idsContorno.firstElement());
        
        xyContour=new float[2][idsContorno.size()];
        
        for (int k=0; k <idsContorno.size(); k++){
            int[] xys=(int[]) idsContorno.elementAt(k);
            xyContour[0][k]=(float) ((xys[0]+minX-2)*localMetaRaster.getResLon()/3600.0f+localMetaRaster.getMinLon());
            xyContour[1][k]=(float) ((xys[1]+minY-2)*localMetaRaster.getResLat()/3600.0f+localMetaRaster.getMinLat());
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        
        try{
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.dir"));
            
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            java.io.File magnFile=new java.io.File(metaModif.getLocationBinaryFile().getPath().substring(0,metaModif.getLocationBinaryFile().getPath().lastIndexOf("."))+".horton");
            metaModif.setLocationBinaryFile(magnFile);
            metaModif.setFormat("Byte");
            byte[][] matOrders=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            hydroScalingAPI.modules.networkAnalysis.objects.RsnTile myTileActual=new hydroScalingAPI.modules.networkAnalysis.objects.RsnTile(902,238,902,238,matDirs,matOrders,metaModif,4);
            
            
        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }
        
        System.exit(0);
        
    }
    
}
