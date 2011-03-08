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
 * MetaNetwork.java
 *
 * Created on July 10, 2003, 12:16 PM
 */

package hydroScalingAPI.io;

import java.io.IOException;

/**
 * This Class reads the set of files with extensions *.point, *.link and *.stream
 * created the the CUENCAS Network Extraction Module.  These files describe the
 * network connection structure.  See the Cuencas Developer's Guide for more
 * details.
 * @author Ricardo Mantilla
 */
public class MetaNetwork {
    
    private hydroScalingAPI.io.MetaRaster metaData;
    
    private int[][] streamRecord,linkRecord;
    private int[] pointRecord;
    
    private int largestOrder;
    
    /**
     * Creates a new instance of MetaNetwork
     * @param md A MetaRaster with information about the raster DEM from which the vectorial
     * network was derived
     * @throws java.io.IOException Captures error while reading the *.point, *.link or *.stream files
     */
    public MetaNetwork(hydroScalingAPI.io.MetaRaster md)  throws java.io.IOException{
        metaData=md;
        
        String pathToStreams=metaData.getLocationBinaryFile().getPath();
        pathToStreams=pathToStreams.substring(0,pathToStreams.lastIndexOf("."))+".stream";
        java.io.File fileStream=new java.io.File(pathToStreams);
        String pathToLink=metaData.getLocationBinaryFile().getPath();
        pathToLink=pathToLink.substring(0,pathToLink.lastIndexOf("."))+".link";
        java.io.File fileLink =new java.io.File(pathToLink);
        String pathToPoint=metaData.getLocationBinaryFile().getPath();
        pathToPoint=pathToPoint.substring(0,pathToPoint.lastIndexOf("."))+".point";
        java.io.File filePoint=new java.io.File(pathToPoint);

        try{
            java.io.FileInputStream pipeToStream=new java.io.FileInputStream(fileStream);
            java.io.BufferedInputStream bufferStream=new java.io.BufferedInputStream(pipeToStream);
            java.io.DataInputStream dataStream=new java.io.DataInputStream(bufferStream);
            
            int totalNumStreams=bufferStream.available()/4/6;
            streamRecord =new int[totalNumStreams][6];
            for (int i=0;i<totalNumStreams;i++){
                //A stream record contains: Head_ID,             Contact_ID,          Tail_ID,             Order,               Ini_link,            Num_link
                streamRecord[i]=new int[] { dataStream.readInt(),dataStream.readInt(),dataStream.readInt(),dataStream.readInt(),dataStream.readInt(),dataStream.readInt()};
                largestOrder=Math.max(streamRecord[i][3],largestOrder);
            }
            dataStream.close();
            bufferStream.close();
            pipeToStream.close();

            java.io.FileInputStream pipeToLink=new java.io.FileInputStream(fileLink);
            java.io.BufferedInputStream bufferLink=new java.io.BufferedInputStream(pipeToLink);
            java.io.DataInputStream dataLink=new java.io.DataInputStream(bufferLink);
            
            int totalNumLinks=bufferLink.available()/4/6;
            linkRecord =new int[totalNumLinks][6];
            for (int i=0;i<totalNumLinks;i++){
                //A link record contains:   Magnitude,         Head_ID,           Contact_ID,       Tail_ID,            Ini_point,         Num_point
                linkRecord[i]=new int[] {   dataLink.readInt(),dataLink.readInt(),dataLink.readInt(),dataLink.readInt(),dataLink.readInt(),dataLink.readInt()};
            }
            dataLink.close();
            bufferLink.close();
            pipeToLink.close();
            
            java.io.FileInputStream pipeToPoint=new java.io.FileInputStream(filePoint);
            java.io.BufferedInputStream bufferPoint=new java.io.BufferedInputStream(pipeToPoint);
            java.io.DataInputStream dataPoint=new java.io.DataInputStream(bufferPoint);
            
            int totalNumPoints=bufferPoint.available()/4;
            pointRecord =new int[totalNumPoints];
            for (int i=0;i<totalNumPoints;i++){
                //A link point contains:  Point_ID
                pointRecord[i]=dataPoint.readInt();
            }
            
            dataPoint.close();
            bufferPoint.close();
            pipeToPoint.close();
            
        }catch(java.io.IOException IOE){
            System.err.println("Failed loading streams file. "+fileStream);
            System.err.println(IOE);
        }
        
    }
    
    /**
     * Returns the largest order present in the DEM river network
     * @return An integer with the largest order present in the DEM river network
     */
    public int getLargestOrder(){
        return largestOrder;
    }
    
    /**
     * Returns an Array of integers int[Number of Streams][6] with information about
     * the stream.  The 6 positions per stream indicate: Head_ID, Contact_ID, Tail_ID, Order,
     * Ini_link, Num_link.  The ID is calculated from the i,j position in the
     * DEM as ID=j*nc+i, where nc is the number of columns of the DEM.  Thus the
     * Head_ID is the position where the link originates, Contact_ID is the position
     * before the junction and Tail_ID is the position of the junction itself. Order
     * refers to the Strahler order of the stream and Ini_link and Num_link are
     * pointers into the *.links file indicating the position of the first link in the
     * stream and the number of links that it contains.
     * @return The array of integers.
     */
    public int[][] getStreamRecord(){
        return streamRecord;
    }
    
    /**
     * Returns an Array of integers int[Number of Links][6] with information about
     * the links.  The 6 positions per link indicate: Magnitude, Head_ID, Contact_ID,
     * Tail_ID, Ini_point, Num_point.  The ID is calculated from the i,j position in the
     * DEM as ID=j*nc+i, where nc is the number of columns of the DEM.  Thus the
     * Head_ID is the position where the link originates, Contact_ID is the position
     * before the junction and Tail_ID is the position of the junction itself. 
     * Magnitude is the link magnitude as defined by Shreve (1969).  Ini_point and Num_point
     * are pointers into the *.point file indicating the position of the first pixel
     * where the link originates and the number of points that it contains.
     * @return An array of integers describing the links
     */
    public int[][] getLinkRecord(){
        return linkRecord;
    }
    
    /**
     * An array with indexes of points in the river network.  The value of the position
     * if the point ID calculated from the i,j position in the DEM as ID=j*nc+i, where nc
     * is the number of columns of the DEM.
     * @return An array of integers.
     */
    public int[] getPointRecord(){
        return pointRecord;
    }
    
    /**
     * Returns a visad Data Type called an {@link visad.UnionSet} which is a collection
     * of {@link visad.Gridded2DSet}.  Each gridded set is a line describing a stream. 
     * This is usefull for ploting purposes.
     * @param orderRequested The order of the streams that must be contained in the UnionSet
     * @return A visad.UnionSet ready to be added into a visad.Display
     * @throws visad.VisADException Captures errors while creating the visad.UnionSet
     */
    public visad.UnionSet getUnionSet(int orderRequested) throws visad.VisADException{
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        
        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();
        
        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];
                java.util.Vector oneStream=new java.util.Vector();
                for (int j=0;j<numLink;j++){
                    int iniPoint=linkRecord[iniLink+j][4]/4;
                    int numPoint=linkRecord[iniLink+j][5];
                    for (int k=0;k<numPoint;k++){
                        
                        float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                        float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);
                        
                        oneStream.add(new float[] {lon,lat});
                    }
                }
                allStreams.add(oneStream);
            }
        }
        
        visad.Gridded2DSet[] griddedStreams=new visad.Gridded2DSet[allStreams.size()];
        for (int i=0;i<griddedStreams.length;i++){
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                riverPath[0][j]=riverNode[0];
                riverPath[1][j]=riverNode[1];
            }
            griddedStreams[i]=new visad.Gridded2DSet(domain,riverPath,riverPath[0].length);
        }
        
        return new visad.UnionSet(domain,griddedStreams);
    }
    
    /**
     * Returns a visad Data Type called an {@link visad.UnionSet} which is a collection
     * of {@link visad.Gridded2DSet}.  Each gridded set is a line describing a stream. 
     * This is usefull for ploting purposes.  Only the streams contained inside the
     * basin Mask are returned.
     * @param orderRequested The order of the streams that must be contained in the UnionSet
     * @param basinMask A byte array with 1s in the positions contained inside the basin and 0s
     * otherwise
     * @return The visad.UnionSet filtered by the desired basin mask
     * @throws visad.VisADException Captures errors while creating the visad.UnionSet
     */
    public visad.UnionSet getUnionSet(int orderRequested, byte[][] basinMask) throws visad.VisADException{
        
        visad.RealTupleType domain=new visad.RealTupleType(visad.RealType.Longitude,visad.RealType.Latitude);
        
        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();
        
        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];
                
                int iniPoint=linkRecord[iniLink][4]/4;
                int numPoint=linkRecord[iniLink][5];
                
                if(basinMask[pointRecord[iniPoint]/demNumCols][pointRecord[iniPoint]%demNumCols] == 1){
                    java.util.Vector oneStream=new java.util.Vector();
                    for (int j=0;j<numLink;j++){
                        iniPoint=linkRecord[iniLink+j][4]/4;
                        numPoint=linkRecord[iniLink+j][5];
                        for (int k=0;k<numPoint;k++){

                            float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                            float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);

                            if(basinMask[pointRecord[iniPoint+k]/demNumCols][pointRecord[iniPoint+k]%demNumCols] == 1)
                                oneStream.add(new float[] {lon,lat});
                        }
                    }
                    allStreams.add(oneStream);
                }
            }
        }
        
        visad.Gridded2DSet[] griddedStreams=new visad.Gridded2DSet[allStreams.size()];
        for (int i=0;i<griddedStreams.length;i++){
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                riverPath[0][j]=riverNode[0];
                riverPath[1][j]=riverNode[1];
            }
            griddedStreams[i]=new visad.Gridded2DSet(domain,riverPath,riverPath[0].length);
        }
        
        return new visad.UnionSet(domain,griddedStreams);
    }
    
    /**
     * Prints to the standard ouput the sequence of i,j indexes that make up the
     * stream.
     * @param orderRequested The order of the streams to be printed
     */
    public void printXYsFile(int orderRequested){
        
        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();
        
        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];
                java.util.Vector oneStream=new java.util.Vector();
                for (int j=0;j<numLink;j++){
                    int iniPoint=linkRecord[iniLink+j][4]/4;
                    int numPoint=linkRecord[iniLink+j][5];
                    for (int k=0;k<numPoint;k++){
                        
                        float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                        float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);
                        
                        oneStream.add(new float[] {lon,lat});
                    }
                }
                allStreams.add(oneStream);
            }
        }
        
        int totalNumS=allStreams.size();
        for (int i=0;i<totalNumS;i++){
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            System.out.println();
            System.out.println("Stream "+(int)(orderRequested*1e6+i+1));
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                System.out.println(riverNode[0]+" "+riverNode[1]);
            }
        }
        
    }
    
    /**
     * Prints to the standard ouput the sequence of i,j indexes that make up the
     * stream.  Only those streams contained in the basin as determined by the basin
     * mask are printed
     * @param orderRequested The order of the streams to be printed
     * @param basinMask The byte array that will serve as filter
     */
    public void printXYsFile(int orderRequested, byte[][] basinMask){
        
        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();
        
        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];
                
                int iniPoint=linkRecord[iniLink][4]/4;
                int numPoint=linkRecord[iniLink][5];
                
                if(basinMask[pointRecord[iniPoint]/demNumCols][pointRecord[iniPoint]%demNumCols] == 1){
                    java.util.Vector oneStream=new java.util.Vector();
                    for (int j=0;j<numLink;j++){
                        iniPoint=linkRecord[iniLink+j][4]/4;
                        numPoint=linkRecord[iniLink+j][5];
                        for (int k=0;k<numPoint;k++){

                            float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                            float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);

                            if(basinMask[pointRecord[iniPoint+k]/demNumCols][pointRecord[iniPoint+k]%demNumCols] == 1)
                                oneStream.add(new float[] {lon,lat});
                        }
                    }
                    allStreams.add(oneStream);
                }
            }
        }
        
        int totalNumS=allStreams.size();
        for (int i=0;i<totalNumS;i++){
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            System.out.println();
            System.out.println("Stream "+(int)(orderRequested*1e6+i+1));
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                System.out.println(riverNode[0]+" "+riverNode[1]);
            }
        }
        
    }

    /**
     * Prints to the standard ouput the sequence of i,j indexes that make up the
     * stream.  Only those streams contained in the basin as determined by the basin
     * mask are printed
     * @param orderRequested The order of the streams to be printed
     * @param basinMask The byte array that will serve as filter
     */
    public void getLineStringXYs(int orderRequested, java.io.OutputStreamWriter newfile) throws java.io.IOException{

        String ret="\n";

        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();

        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];

                int iniPoint=linkRecord[iniLink][4]/4;
                int numPoint=linkRecord[iniLink][5];

                java.util.Vector oneStream=new java.util.Vector();
                for (int j=0;j<numLink;j++){
                    iniPoint=linkRecord[iniLink+j][4]/4;
                    numPoint=linkRecord[iniLink+j][5];
                    for (int k=0;k<numPoint;k++){

                        float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                        float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);

                        oneStream.add(new float[] {lon,lat});
                    }
                }
                allStreams.add(oneStream);
            }
        }

        int totalNumS=allStreams.size();
        for (int i=0;i<totalNumS;i++){
            newfile.write("    <Placemark>"+ret);
            newfile.write("        <name>Stream "+(int)(orderRequested*1e6+i+1)+"</name>"+ret);
            newfile.write("        <styleUrl>#linestyleO"+orderRequested+"</styleUrl>"+ret);
            newfile.write("        <visibility>"+(orderRequested>4?1:0)+"</visibility>"+ret);
            String listOfStreams="";
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            newfile.write("        <LineString>"+ret);
//            newfile.write("            <extrude>1</extrude>"+ret);
//            newfile.write("            <tessellate>1</tessellate>"+ret);
            newfile.write("            <coordinates>"+ret);
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                listOfStreams+=(riverNode[0]+","+riverNode[1]+" ");
            }
            newfile.write("                "+listOfStreams+ret);
            newfile.write("            </coordinates>"+ret);
            newfile.write("        </LineString>"+ret);
            newfile.write("    </Placemark>"+ret);
        }

    }
    
    /**
     * Prints to the standard ouput the sequence of i,j indexes that make up the
     * stream.  Only those streams contained in the basin as determined by the basin
     * mask are printed
     * @param orderRequested The order of the streams to be printed
     * @param basinMask The byte array that will serve as filter
     */
    public void getLineStringXYs(int orderRequested, byte[][] basinMask, java.io.OutputStreamWriter newfile) throws java.io.IOException{
        
        String ret="\n";
        
        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();
        
        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];
                
                int iniPoint=linkRecord[iniLink][4]/4;
                int numPoint=linkRecord[iniLink][5];
                
                if(basinMask[pointRecord[iniPoint]/demNumCols][pointRecord[iniPoint]%demNumCols] == 1){
                    java.util.Vector oneStream=new java.util.Vector();
                    for (int j=0;j<numLink;j++){
                        iniPoint=linkRecord[iniLink+j][4]/4;
                        numPoint=linkRecord[iniLink+j][5];
                        for (int k=0;k<numPoint;k++){

                            float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                            float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);

                            if(basinMask[pointRecord[iniPoint+k]/demNumCols][pointRecord[iniPoint+k]%demNumCols] == 1)
                                oneStream.add(new float[] {lon,lat});
                        }
                    }
                    allStreams.add(oneStream);
                }
            }
        }
        
        int totalNumS=allStreams.size();
        for (int i=0;i<totalNumS;i++){
            newfile.write("    <Placemark>"+ret);
            newfile.write("        <name>Stream "+(int)(orderRequested*1e6+i+1)+"</name>"+ret);
            newfile.write("        <styleUrl>#linestyleO"+orderRequested+"</styleUrl>"+ret);
            newfile.write("        <visibility>"+(orderRequested>0?1:0)+"</visibility>"+ret);
            String listOfStreams="";
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            newfile.write("        <LineString>"+ret);
//            newfile.write("            <extrude>1</extrude>"+ret);
//            newfile.write("            <tessellate>1</tessellate>"+ret);
            newfile.write("            <coordinates>"+ret);
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                listOfStreams+=(Math.round(riverNode[0]*1e5)/1e5+","+Math.round(riverNode[1]*1e5)/1e5+" ");
            }
            newfile.write("                "+listOfStreams+ret);
            newfile.write("            </coordinates>"+ret);
            newfile.write("        </LineString>"+ret);
            newfile.write("    </Placemark>"+ret);
        }

    }

    /**
     * Prints to the standard ouput the sequence of i,j indexes that make up the
     * stream.  Only those streams contained in the basin as determined by the basin
     * mask are printed
     * @param orderRequested The order of the streams to be printed
     * @param basinMask The byte array that will serve as filter
     */
    public void getLineStringXYs2(int orderRequested, byte[][] basinMask, java.io.OutputStreamWriter newfile, java.util.Hashtable idsMaxs) throws java.io.IOException{


        float[] thresholds=new float[] {0.05f,0.16f,0.6f,1.03f,3.96f,5.68f};

        String ret="\n";

        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();

        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];

                int iniPoint=linkRecord[iniLink][4]/4;
                int numPoint=linkRecord[iniLink][5];

                if(basinMask[pointRecord[iniPoint]/demNumCols][pointRecord[iniPoint]%demNumCols] == 1){
                    java.util.Vector oneStream=new java.util.Vector();
                    for (int j=0;j<numLink;j++){
                        iniPoint=linkRecord[iniLink+j][4]/4;
                        numPoint=linkRecord[iniLink+j][5];
                        for (int k=0;k<numPoint;k++){

                            float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                            float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);

                            if(basinMask[pointRecord[iniPoint+k]/demNumCols][pointRecord[iniPoint+k]%demNumCols] == 1)
                                oneStream.add(new float[] {lon,lat,pointRecord[iniPoint+k]});
                        }
                    }
                    allStreams.add(oneStream);
                }
            }
        }

        int totalNumS=allStreams.size();
        for (int i=0;i<totalNumS;i++){



            String listOfStreams="";
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];


            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                listOfStreams+=(riverNode[0]+","+riverNode[1]+" ");
            }
            float[] riverNode=(float[])oneStream.get(riverPath[0].length-2);

            float[] linkSimInfo=(float[])idsMaxs.get(""+(int)riverNode[2]);

            if(linkSimInfo != null){

                if(linkSimInfo[2] >= thresholds[orderRequested-1]*2){

                    newfile.write("    <Placemark>"+ret);

                    newfile.write("        <name>Stream "+(int)(orderRequested*1e6+i+1)+"</name>"+ret);
                    if(linkSimInfo != null){

                        if(linkSimInfo[2] < thresholds[orderRequested-1]*1){
                            newfile.write("        <styleUrl>#linestyleNormal</styleUrl>"+ret);
                        } else {
                            newfile.write("        <styleUrl>#linestyleDanger</styleUrl>"+ret);
                        }
                    } else{
                        newfile.write("        <styleUrl>#linestyleDanger</styleUrl>"+ret);
                    }

                    newfile.write("        <visibility>1</visibility>"+ret);
                    newfile.write("        <LineString>"+ret);
                    newfile.write("            <coordinates>"+ret);
                    newfile.write("                "+listOfStreams+ret);
                    newfile.write("            </coordinates>"+ret);
                    newfile.write("        </LineString>"+ret);

                    newfile.write("    </Placemark>"+ret);

                }
            }
        }

    }

    /**
     * Prints to the standard ouput the sequence of i,j indexes that make up the
     * stream.  Only those streams contained in the basin as determined by the basin
     * mask are printed
     * @param orderRequested The order of the streams to be printed
     * @param basinMask The byte array that will serve as filter
     */
    public void getLineStringXYs(int orderRequested, byte[][] basinMask, java.io.OutputStreamWriter newfile,double[] statusArray) throws java.io.IOException{

        String ret="\n";

        double demMinLon=metaData.getMinLon();
        double demMinLat=metaData.getMinLat();
        double demResLon=metaData.getResLon();
        double demResLat=metaData.getResLat();
        int demNumCols=metaData.getNumCols();

        java.util.Vector allStreams=new java.util.Vector();
        for (int i=0;i<streamRecord.length;i++){
            if(streamRecord[i][3] == orderRequested) {
                int iniLink=streamRecord[i][4]/8;
                int numLink=streamRecord[i][5];

                int iniPoint=linkRecord[iniLink][4]/4;
                int numPoint=linkRecord[iniLink][5];

                if(basinMask[pointRecord[iniPoint]/demNumCols][pointRecord[iniPoint]%demNumCols] == 1){
                    java.util.Vector oneStream=new java.util.Vector();
                    for (int j=0;j<numLink;j++){
                        iniPoint=linkRecord[iniLink+j][4]/4;
                        numPoint=linkRecord[iniLink+j][5];
                        for (int k=0;k<numPoint;k++){

                            float lon=(float)((pointRecord[iniPoint+k]%demNumCols)*demResLon/3600.+demMinLon+0.5*demResLon/3600.);
                            float lat=(float)((pointRecord[iniPoint+k]/demNumCols)*demResLat/3600.+demMinLat+0.5*demResLat/3600.);

                            if(basinMask[pointRecord[iniPoint+k]/demNumCols][pointRecord[iniPoint+k]%demNumCols] == 1)
                                oneStream.add(new float[] {lon,lat});
                        }
                    }
                    allStreams.add(oneStream);
                }
            }
        }

        int totalNumS=allStreams.size();
        for (int i=0;i<totalNumS;i++){
            newfile.write("    <Placemark>"+ret);
            newfile.write("        <name>Stream "+(int)(orderRequested*1e6+i+1)+"</name>"+ret);
            newfile.write("        <styleUrl>#linestyleO"+orderRequested+"S"+statusArray[i]+"</styleUrl>"+ret);
            newfile.write("        <visibility>"+(orderRequested>0?1:0)+"</visibility>"+ret);
            String listOfStreams="";
            java.util.Vector oneStream=(java.util.Vector)allStreams.get(i);
            float[][] riverPath=new float[2][oneStream.size()];
            newfile.write("        <LineString>"+ret);
//            newfile.write("            <extrude>1</extrude>"+ret);
//            newfile.write("            <tessellate>1</tessellate>"+ret);
            newfile.write("            <coordinates>"+ret);
            for (int j=0;j<riverPath[0].length;j++){
                float[] riverNode=(float[])oneStream.get(j);
                listOfStreams+=(riverNode[0]+","+riverNode[1]+" ");
            }
            newfile.write("                "+listOfStreams+ret);
            newfile.write("            </coordinates>"+ret);
            newfile.write("        </LineString>"+ret);
            newfile.write("    </Placemark>"+ret);
        }

    }
    
    /**
     * Various tests for the class
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        try{
            java.io.File theFile=new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/hidrosigDataBases/Whitewater_database/Rasters/Topography/1_ArcSec_USGS/Whitewaters.dir"));
        
            String formatoOriginal=metaModif.getFormat();
            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();
            
            hydroScalingAPI.util.geomorphology.objects.Basin laCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(1064,496,matDirs,metaModif);
            
            byte[][] basMask=laCuenca.getBasinMask();

            MetaNetwork test=new MetaNetwork(metaModif);

            test.printXYsFile(Integer.parseInt(args[0]),basMask);
        } catch (java.io.IOException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }/* catch (visad.VisADException e1){
            System.err.println("ERROR** "+e1.getMessage());
        }*/
        
    }
    
}
