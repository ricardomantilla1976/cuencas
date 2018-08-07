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
 * MetaDLG.java
 *
 * Created on July 9, 2003, 8:16 PM
 */

package hydroScalingAPI.io;

/**
 * This class reads DLG files downloaded from the  EROS website at 
 * <a href="http://edc.usgs.gov/geodata/" target="_blank">http://edc.usgs.gov/geodata/</a>.
 * @author Ricardo Mantilla
 */
public class MetaDLG {
    
    private int Zone;
    private java.awt.geom.Rectangle2D coveredRegionGeodetic,coveredRegionUTM;
    private float verticalCorrectionFactor,horizontalCorrectionFactor;

    private visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
    private visad.Gridded2DSet[] streams;
    
    /**
     * Creates a new instance of MetaDLG
     * @param fileName The file where the DLG resides
     * @param desiredRegion The limts of the region of interest given as a {@link java.awt.geom.Rectangle2D}.
     * @throws java.io.IOException Captures errors while reading the file.
     * @throws visad.VisADException Captures errors while creating the {@link visad.Gridded2DSet} used to visualize the
     * vector.
     */
    public MetaDLG(String fileName,java.awt.geom.Rectangle2D desiredRegion) throws java.io.IOException, visad.VisADException{
        
        java.util.StringTokenizer tokens;
        
        checkDLG(fileName);
        
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(fileName)));
        
        for(int i=0;i<3;i++)fileMeta.readLine();
        
        tokens=new java.util.StringTokenizer(fileMeta.readLine());
        tokens.nextToken(); tokens.nextToken();
        Zone=new Integer(tokens.nextToken()).intValue();
        
        for(int i=0;i<6;i++)fileMeta.readLine();
        
        tokens=new java.util.StringTokenizer(fileMeta.readLine());
        tokens.nextToken();
        float minYLL=Float.parseFloat(tokens.nextToken());
        float minXLL=Float.parseFloat(tokens.nextToken());
        
        float minXUTM=Float.parseFloat(tokens.nextToken());
        float minYUTM=Float.parseFloat(tokens.nextToken());
        
        fileMeta.readLine();
        tokens=new java.util.StringTokenizer(fileMeta.readLine());
        tokens.nextToken();
        float maxYLL=Float.parseFloat(tokens.nextToken());
        float maxXLL=Float.parseFloat(tokens.nextToken());
        
        float maxXUTM=Float.parseFloat(tokens.nextToken());
        float maxYUTM=Float.parseFloat(tokens.nextToken());
        
        coveredRegionGeodetic=new java.awt.geom.Rectangle2D.Double(minXLL,minYLL,maxXLL-minXLL,maxYLL-minYLL);
        coveredRegionUTM=new java.awt.geom.Rectangle2D.Double(minXUTM,minYUTM,maxXUTM-minXUTM,maxYUTM-minYUTM);                  
        
        if(!desiredRegion.intersects(coveredRegionGeodetic)){
            fileMeta.close();
            return;
        }
        /*System.out.println("cp "+fileName+" "+"/hidrosigDataBases/Whitewater_database/Vectors/");
        if(true){
            return;
        }*/
        
        geotransform.coords.Gdc_Coord_3d gdc = new geotransform.coords.Gdc_Coord_3d();
        geotransform.coords.Utm_Coord_3d utm = new geotransform.coords.Utm_Coord_3d();
        utm.x=minXUTM; 
        utm.y=minYUTM; 
        utm.zone=(byte)Zone;
        utm.z=0;
        utm.hemisphere_north=true;

        geotransform.transforms.Utm_To_Gdc_Converter.Init(new geotransform.ellipsoids.WE_Ellipsoid());
        geotransform.transforms.Utm_To_Gdc_Converter.Convert(utm,gdc);
        
        verticalCorrectionFactor=minYLL-(float)gdc.latitude;
        horizontalCorrectionFactor=minXLL-(float)gdc.longitude;
        
        //verticalCorrectionFactor=0.0f;
        //horizontalCorrectionFactor=0.0f;
        
        fileMeta.readLine();
        tokens=new java.util.StringTokenizer(fileMeta.readLine().substring(23));
        for (int i=0;i<8;i++) tokens.nextToken();
        int nLines=Integer.parseInt(tokens.nextToken());
        
        streams=new visad.Gridded2DSet[nLines];
        
        int iniNode, endNode, myNodes;
        String feedLine;
        
        for (int i=0;i<nLines;i++){
            
                do{
                    feedLine=fileMeta.readLine();
                } while(!feedLine.substring(0,1).equalsIgnoreCase("L"));
                
                tokens=new java.util.StringTokenizer(feedLine.substring(1));
                for (int j=0;j<5;j++) tokens.nextToken();
                myNodes = Integer.parseInt(tokens.nextToken());
                float[][] line=new float[2][myNodes];
                
                gdc = new geotransform.coords.Gdc_Coord_3d();
                utm = new geotransform.coords.Utm_Coord_3d();
                utm.zone=(byte)Zone;
                utm.z=0;
                utm.hemisphere_north=true;
                
                geotransform.transforms.Utm_To_Gdc_Converter.Init(new geotransform.ellipsoids.WE_Ellipsoid());

                for (int j=0;j<myNodes/3;j++){
                    
                    String inputLine=fileMeta.readLine();
                    
                    tokens=new java.util.StringTokenizer(inputLine);
                    
                    line[0][j*3] = Float.parseFloat(tokens.nextToken());
                    line[1][j*3] = Float.parseFloat(tokens.nextToken());
                    
                    utm.x=line[0][j*3]; utm.y=line[1][j*3]; 
                    geotransform.transforms.Utm_To_Gdc_Converter.Convert(utm,gdc);
                    
                    line[0][j*3] = (float)gdc.longitude+horizontalCorrectionFactor;
                    line[1][j*3] = (float)gdc.latitude+verticalCorrectionFactor;
                    
                    line[0][j*3+1] = Float.parseFloat(tokens.nextToken());
                    line[1][j*3+1] = Float.parseFloat(tokens.nextToken());
                    
                    utm.x=line[0][j*3+1]; utm.y=line[1][j*3+1]; 
                    geotransform.transforms.Utm_To_Gdc_Converter.Convert(utm,gdc);
                    
                    line[0][j*3+1] = (float)gdc.longitude+horizontalCorrectionFactor;
                    line[1][j*3+1] = (float)gdc.latitude+verticalCorrectionFactor;
                    
                    line[0][j*3+2] = Float.parseFloat(tokens.nextToken());
                    line[1][j*3+2] = Float.parseFloat(tokens.nextToken());
                    
                    utm.x=line[0][j*3+2]; utm.y=line[1][j*3+2]; 
                    geotransform.transforms.Utm_To_Gdc_Converter.Convert(utm,gdc);
                    
                    line[0][j*3+2] = (float)gdc.longitude+horizontalCorrectionFactor;
                    line[1][j*3+2] = (float)gdc.latitude+verticalCorrectionFactor;
                    
                }
                if(myNodes/3*3 < myNodes){
                    tokens=new java.util.StringTokenizer(fileMeta.readLine());
                    for (int j=myNodes/3*3;j<myNodes;j++){
                        line[0][j] = Float.parseFloat(tokens.nextToken());
                        line[1][j] = Float.parseFloat(tokens.nextToken());
                        
                        utm.x=line[0][j]; utm.y=line[1][j]; 
                        geotransform.transforms.Utm_To_Gdc_Converter.Convert(utm,gdc);

                        line[0][j] = (float)gdc.longitude+horizontalCorrectionFactor;
                        line[1][j] = (float)gdc.latitude+verticalCorrectionFactor;

                    }
                }
                
                streams[i]=new visad.Gridded2DSet(domain,line,line[0].length);
                
            }
        
        fileMeta.close();
        
    }
    
    private void checkDLG(String fileName) throws java.io.IOException{
        
        java.io.File imgDestin = new java.io.File(fileName);
        
        java.io.BufferedReader fileMeta = new java.io.BufferedReader(new java.io.FileReader(imgDestin));
        
        String feedLine=fileMeta.readLine();
        
        if(feedLine.length() < 90) return;
        
        fileMeta.close();
        fileMeta = new java.io.BufferedReader(new java.io.FileReader(new java.io.File(fileName)));
        
        java.io.File imgSource = new java.io.File(fileName+".tmp");
        
        java.io.BufferedWriter newFileMeta= new java.io.BufferedWriter(new java.io.FileWriter(imgSource));
        
        while (fileMeta.ready()){
            char[] logRecord=new char[80];
            fileMeta.read(logRecord);
            newFileMeta.write(new String(logRecord) + "\n");
        }
        fileMeta.close();
        newFileMeta.close();
        
        hydroScalingAPI.tools.FileManipulation.CopyFile(imgSource,imgDestin);
        
        imgSource.delete();
        
    }
    
    /**
     * Returns a {@link visad.UnionSet} ready to be used inside a {@link visad.Display}
     * @return The {@link visad.UnionSet}
     * @throws visad.VisADException Captures errors while creating the {@link visad.UnionSet}
     */
    public visad.UnionSet getUnionSet() throws visad.VisADException{
        if (streams == null) return null;
        return new visad.UnionSet(domain, streams);
    }
    
    /**
     * Tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
            //someRivers1.dlg
            //someRivers0.dlg
            //hydroScalingAPI.io.MetaDLG metaDLG1=new hydroScalingAPI.io.MetaDLG ("/hidrosigDataBases/Whitewater_database/Vectors/RiversWichitaArea2.dlg",new java.awt.geom.Rectangle2D.Double(-180,-90,360,180));
            hydroScalingAPI.io.MetaDLG metaDLG1=new hydroScalingAPI.io.MetaDLG ("/hidrosigDataBases/LColoradoRiver_AZ_database/Vectors/511378.HY.opt",new java.awt.geom.Rectangle2D.Double(-180,-90,360,180));
        } catch (java.io.IOException IOE){
            System.err.print(IOE);
            System.exit(0);
        } catch (visad.VisADException vie){
            System.err.print(vie);
            System.exit(0);
        }
        
        System.exit(0);
    }
    
}
