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


package hydroScalingAPI.examples.io;

/**
 *
 * @author  Ricardo Mantilla
 */
public class ImportVisitedPointsASCII {
    
    /** Creates a new instance of importMapSourceASCII */
    public ImportVisitedPointsASCII(java.io.File sourceFile) {
        
        try{
            java.io.BufferedReader bufferRemoto = new java.io.BufferedReader(new java.io.FileReader(sourceFile));
            
            for(int j=0;j<1;j++) bufferRemoto.readLine();
            
            String locationInfo=bufferRemoto.readLine();
            int i=1;
            //while (i<2) {
            while (locationInfo != null){
                
                GpsLocationProperties thisLocation=new GpsLocationProperties("KS");
                java.util.StringTokenizer tokens=new java.util.StringTokenizer(locationInfo,"\t");
                
                thisLocation.State=hydroScalingAPI.tools.StateName.CodeOrNameToStandardName("KS");
                thisLocation.Type="Hydraulic Geometry Measurement";
                thisLocation.Name="GPS-"+tokens.nextToken()+"-Order"+"-"+tokens.nextToken()+"-"+tokens.nextToken();
                System.out.println(thisLocation.Name);
                thisLocation.Images=new String[] {"images/"+tokens.nextToken()+" ; "+"Upstream view to the surveyed channel"};
                thisLocation.Source="Hydraulic Geometry was surveyed \n";
                thisLocation.Source+="WBF:\t"+tokens.nextToken()+" m \n";
                thisLocation.Source+="HBF:\t"+tokens.nextToken()+" m \n";
                thisLocation.Source+="Basin Code:\t"+tokens.nextToken()+"\n";
                thisLocation.Source+="Time:\t"+tokens.nextToken()+"\n";
                thisLocation.Source+="Gauging Site Rate:\t"+tokens.nextToken()+"\n";
                String position=tokens.nextToken();
                System.out.println(position);
                thisLocation.latitude=position.substring(1,3)+":"+position.substring(4,6)+":"+position.substring(7,11)+" N";
                thisLocation.longitude=position.substring(13,15)+":"+position.substring(16,18)+":"+position.substring(19,23)+" W";
                thisLocation.Elevation=tokens.nextToken().substring(0,4);
                if(tokens.hasMoreTokens()) thisLocation.Source+="Comments:\t"+tokens.nextToken()+"\n";
                
                System.out.println(thisLocation.latitude);
                System.out.println(thisLocation.longitude);
                System.out.println(thisLocation.Elevation);
                
                System.out.println(thisLocation.Source);
                
                thisLocation.County="N/A";

                new java.io.File("/hidrosigDataBases/Whitewater_database/Sites/Locations/"+thisLocation.State+"/"+thisLocation.Type+"/").mkdirs();
                java.io.File theFile = new java.io.File("/hidrosigDataBases/Whitewater_database/Sites/Locations/"+thisLocation.State+"/"+thisLocation.Type+"/"+thisLocation.Name+".txt.gz");
                java.io.FileOutputStream inputLocal=new java.io.FileOutputStream(theFile);
                java.util.zip.GZIPOutputStream inputComprim=new java.util.zip.GZIPOutputStream(inputLocal);
                java.io.BufferedWriter bufferLocalW= new java.io.BufferedWriter(new java.io.OutputStreamWriter(inputComprim));

                System.out.println(theFile);

                bufferLocalW.write("[type]"+"\n");
                bufferLocalW.write(thisLocation.Type+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[source]"+"\n");
                bufferLocalW.write("CIRES"+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[site name]"+"\n");
                bufferLocalW.write(thisLocation.Name+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[county]"+"\n");
                bufferLocalW.write(thisLocation.County+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[state]"+"\n");
                bufferLocalW.write(thisLocation.State+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[latitude (deg:min:sec)]"+"\n");
                bufferLocalW.write(thisLocation.latitude+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[longitude (deg:min:sec)]"+"\n");
                bufferLocalW.write(thisLocation.longitude+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[altitude ASL (m)]"+"\n");
                bufferLocalW.write(thisLocation.Elevation+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[images]"+"\n");
                if (thisLocation.Images.length == 0) bufferLocalW.write("N/A"+"\n");
                for(int j=0;j<thisLocation.Images.length;j++) bufferLocalW.write(thisLocation.Images[j]+"\n");
                bufferLocalW.write("\n");
                bufferLocalW.write("[information]"+"\n");
                bufferLocalW.write(thisLocation.Source+"\n");
                bufferLocalW.write("\n");

                bufferLocalW.close();
                
                locationInfo=bufferRemoto.readLine();
                i++;
            }
            bufferRemoto.close();
            
            
        }catch(java.io.IOException IOE){
            System.err.println(IOE);
        }
        
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        new ImportVisitedPointsASCII(new java.io.File("/home/ricardo/workFiles/myWorkingStuff/ResearchProjects/whitewaterKansas/04-2004 Trip/WidthMeasurements.txt"));
    }
    
}