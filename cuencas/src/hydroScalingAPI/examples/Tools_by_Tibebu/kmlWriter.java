/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.Tools_by_Tibebu;

import java.io.FileNotFoundException;
import java.io.PrintWriter;

/**
 *
 * @author tayalew
 */
public class kmlWriter {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws FileNotFoundException {
        // TODO code application logic here
        double [] xCoordinate = new double [] {-91.64472198,-91.61083221,-91.64888763,-91.64305115,-91.67832947,-91.65916443,-91.66971588,-91.72750092,-91.72388458,-91.68999481,-91.62416077,-91.95082855,-91.70861053,-91.69416046,-91.93138885,-91.95527649,-91.66916656,-91.94277191,-91.87277222,-91.89666748,-91.96110535,-91.64055634,-91.67861176,-91.65083313,-91.74055481,-91.95111084,-91.73471832,-91.93444061,-91.96221924,-91.85971832,-91.88944244,-91.67472076,-91.70555115,-91.60666656,-91.92111206,-91.73944092,-91.89472198,-91.59111023,-91.59666443,-91.93972015,-91.78305054,-91.91360474,-91.77832794,-91.85110474,-91.91082764,-91.77416229,-91.88583374,-91.92805481,-91.61638641,-91.95111084,-91.82277679,-91.88749695,-91.81027222,-91.68027496,-91.95583344,-91.91638947,-91.75749969,-91.73138428,-91.98972321,-91.7555542,-91.61721802,-91.63777161,-91.74027252,-91.82833099,-91.66027832,-91.6416626,-91.87805176,-91.65083313,-91.80222321,-91.81749725,-91.93666077,-91.92166138,-91.92777252,-92.00054932,-91.81638336,-91.74388885,-91.62138367,-91.95999908,-91.85250092,-91.89777374,-91.97055054,-91.7563858,-91.99138641,-91.91805267,-91.86860657,-91.78333282,-91.68582916,-91.96555328,-91.66832733,-91.8841629,-91.92888641,-91.76888275,-91.94527435,-91.84638977,-91.82833099,-91.75054932,-91.70888519,-91.88999939,-91.69444275,-91.97332764,-91.68582916,-91.87444305,-91.71360779,-91.83555603,-91.73082733,-91.76499939,-91.78805542,-91.89388275,-91.79083252,-91.91221619,-91.7583313,-91.86499786,-91.97999573,-91.93943787,-91.96749878,-91.81499481,-91.89860535,-91.95527649,-91.8722229,-91.98444366,-91.87693787,-91.91805267,-91.9624939,-91.82611084,-91.85277557,-91.97138977,-91.64582825,-91.72360992,-91.73416138,-91.95610809,-91.60027313,-91.90888977,-91.62249756,-91.93305206,-91.78027344,-91.8797226,-91.94833374,-91.62749481,-91.66194153,-91.69055176,-91.88777161,-91.71027374,-91.89555359,-91.75694275,-91.79444122,-91.9541626,-91.97694397,-91.81027222,-91.86638641,-91.84499359,-91.9624939};
        double [] yCoordinate = new double [] {41.66805267,41.67749786,41.67138672,41.6697197,41.67721939,41.67666245,41.67638779,41.68027496,41.67888641,41.67944336,41.68360901,41.68166351,41.67944336,41.68110657,41.68194199,41.68166351,41.68138504,41.68499756,41.6855545,41.68582916,41.68499756,41.68333054,41.68138504,41.68277359,41.6855545,41.68805313,41.68805313,41.68888474,41.68749619,41.69527435,41.69221878,41.69971848,41.69888687,41.69027328,41.69055176,41.6966629,41.68999863,41.69527435,41.6958313,41.69777679,41.70499802,41.69805145,41.70305252,41.7013855,41.69944,41.70388794,41.70388794,41.70333099,41.70055389,41.70555115,41.70888519,41.70527649,41.70666504,41.70360947,41.70721817,41.70944214,41.70944214,41.71166229,41.70833206,41.70916367,41.71027374,41.70277405,41.71471786,41.71332932,41.71416473,41.71555328,41.71583176,41.71055222,41.71388626,41.71777344,41.71583176,41.71666336,41.71611023,41.72027588,41.72249603,41.72249603,41.71916199,41.72388458,41.71971893,41.72527313,41.72388458,41.72805405,41.7219429,41.73110962,41.7274971,41.72055435,41.72777557,41.72527313,41.72583008,41.72888565,41.73194122,41.72138596,41.7330513,41.73360825,41.7283287,41.73083115,41.73083115,41.73666382,41.72555161,41.73916245,41.72972107,41.73638535,41.71360779,41.7330513,41.72805405,41.74166489,41.73999786,41.73971939,41.74110794,41.74055099,41.73888779,41.74388504,41.74416351,41.74249649,41.74277496,41.74583054,41.74166489,41.74333191,41.74916458,41.74305344,41.7508316,41.75416565,41.7591629,41.74832916,41.75694275,41.76138687,41.67138672,41.68388748,41.68582916,41.68388748,41.68944168,41.69610977,41.69499588,41.70833206,41.70694351,41.70888519,41.70999908,41.70610809,41.70999908,41.71805191,41.72055435,41.71527481,41.73638535,41.73249817,41.7322197,41.73888779,41.74166489,41.7330513,41.74166489,41.74749756,41.76082993};
        int locID =0;
        String fileName = "151_best_loc_on_O3_and_4";
        String kmlFile =  "C:/CuencasDataBases/ClearCreek_Database/Reservoir_Locations/"+fileName+".kml";
        
        
        PrintWriter out = new PrintWriter(kmlFile);
        out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        out.println("<kml xmlns=\"http://earth.google.com/kml/2.2\">");
        out.println("   <Document>");
        out.println("       <name>"+fileName+"</name>");
        out.println("       <Style id=\"myStyle\">");
        out.println("           <IconStyle>");
        out.println("               <Icon>");
        out.println("                   <href>http://maps.google.com/mapfiles/kml/shapes/track.png</href>");
        out.println("               </Icon>");
        out.println("           </IconStyle>");
        out.println("       </Style>");
        
        
        for (int i=0; i<xCoordinate.length; i++)
        {
            locID=i+1;
            out.println("       <Placemark>");
            out.println("           <name>"+""+"</name>");
            out.println("           <styleUrl>#myStyle</styleUrl>");
            out.println("           <Point>");
            out.println("               <coordinates>"+xCoordinate[i]+","+yCoordinate[i]+","+0+"</coordinates>");
            out.println("           </Point>");
            out.println("       </Placemark>");            
            
        }
        
        out.println("   </Document>");
        out.println("</kml>");
        out.close();
    }
}
