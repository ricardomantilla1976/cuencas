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
 * ImportUSGS_StateGazetteer.java
 *
 * Created on June 14, 2003, 12:13 AM
 */

package hydroScalingAPI.io;

/**
 * This class populates the site database using data from the online source at 
 * http://cuencas.colorado.edu/
 * @author Ricardo Mantilla
 */
public class ImportUSGS_StateGazetteer {
    
    /*
        Feature_ID
        Feature_Name
        Class
        ST_alpha
        ST_num
        County
        County_num
        Primary_lat_DMS
        Primary_lon_DMS
        Primary_lat_dec
        Primary_lon_dec
        Source_lat_DMS
        Source_lon_DMS
        Source_lat_dec
        Source_lon_dec
        Elev(Meters)
        Map_Name
    */

    
    /**
     * Creates a new instance of ImportUSGS_StateGazetteer
     * @param state The two letter code for the state (e.g. CO - Colorado, NM - New Mexico)
     * @param validFeatures An String array listing the features that you wish to import.  The options are:
     * <blockquote>
     * <p><span class="bold">airport</span> - manmade facility maintained for the use
     *  of aircraft (airfield, airstrip, landing field, landing strip). </p>
     * <p><a name="arch"></a><span class="bold">arch</span> - natural arch-like opening
     *  in a rock mass (bridge, natural bridge, sea arch). </p>
     * <p><a name="area"></a><span class="bold">area</span> - any one of several areally
     *  extensive natural features not included in other categories (badlands, barren,
     *  delta, fan, garden). </p>
     * <p><a name="arroyo"></a><span class="bold">arroyo</span> - watercourse or channel
     *  through which water may occasionally flow (coulee, draw, gully, wash). </p>
     * <p><a name="bar"></a><span class="bold">bar</span> - natural accumulation of
     *  sand, gravel, or alluvium forming an underwater or exposed embankment (ledge,
     *  reef, sandbar, shoal, spit). </p>
     * <p><a name="basin"></a><span class="bold">basin</span> - natural depression or
     *  relatively low area enclosed by higher land (amphitheater, cirque, pit, sink). </p>
     * <p><a name="bay"></a><span class="bold">bay</span> - indentation of a coastline
     *  or shoreline enclosing a part of a body of water; a body of water partly surrounded
     *  by land (arm, bight, cove, estuary, gulf, inlet, sound). </p>
     * <p><a name="beach"></a><span class="bold">beach</span> - the sloping shore along
     *  a body of water that is washed by waves or tides and is usually covered by
     *  sand or gravel (coast, shore, strand). </p>
     * <p><a name="bench"></a><span class="bold">bench</span> - area of relatively level
     *  land on the flank of an elevation such as a hill, ridge, or mountain where
     *  the slope of the land rises on one side and descends on the opposite side (level). </p>
     * <p><a name="bend"></a><span class="bold">bend</span> - curve in the course of
     *  a stream and (or) the land within the curve; a curve in a linear body of water
     *  (bottom, loop, meander). </p>
     * <p><a name="bridge"></a><span class="bold">bridge</span> - manmade structure
     *  carrying a trail, road, or other transportation system across a body of water
     *  or depression (causeway, overpass, trestle). </p>
     * <p><a name="building"></a><span class="bold">building</span> - a manmade structure
     *  with walls and a roof for protection of people and (or) materials, but not
     *  including church, hospital, or school. </p>
     * <p><a name="canal"></a><span class="bold">canal</span> - manmade waterway used
     *  by watercraft or for drainage, irrigation, mining, or water power (ditch, lateral). </p>
     * <p><a name="cape"></a><span class="bold">cape</span> - projection of land extending
     *  into a body of water (lea, neck, peninsula, point). </p>
     * <p><a name="cave"></a><span class="bold">cave</span> - natural underground passageway
     *  or chamber, or a hollowed out cavity in the side of a cliff (cavern, grotto). </p>
     * <p><a name="cemetery"></a><span class="bold">cemetery</span> - a place or area
     *  for burying the dead (burial, burying ground, grave, memorial garden). </p>
     * <p><a name="channel"></a><span class="bold">channel</span> - linear deep part
     *  of a body of water through which the main volume of water flows and is frequently
     *  used as aroute for watercraft (passage, reach, strait, thoroughfare, throughfare). </p>
     * <p><a name="church"></a><span class="bold">church</span> - building used for
     *  religious worship (chapel, mosque, synagogue, tabernacle, temple). </p>
     * <p><a name="civil"></a><span class="bold">civil</span> - a political division
     *  formed for administrative purposes (borough, county, municipio, parish, town,
     *  township). </p>
     * <p><a name="cliff"></a><span class="bold">cliff</span> - very steep or vertical
     *  slope (bluff, crag, head, headland, nose, palisades, precipice, promontory,
     *  rim, rimrock). </p>
     * <p><a name="crater"></a><span class="bold">crater</span> - circular-shaped depression
     *  at the summit of a volcanic cone or one on the surface of the land caused by
     *  the impact of a meteorite; a manmade depression caused by an explosion (caldera,
     *  lua). </p>
     * <p><a name="crossing"></a><span class="bold">crossing</span> - a place where
     *  two or more routes of transportation form a junction or intersection (overpass,
     *  underpass). </p>
     * <p><a name="dam"></a><span class="bold">dam</span> - water barrier or embankment
     *  built across the course of a stream or into a body of water to control and
     *  (or) impound the flow of water (breakwater, dike, jetty). </p>
     * <p><a name="falls"></a><span class="bold">falls</span> - perpendicular or very
     *  steep fall of water in the course of a stream (cascade, cataract, waterfall). </p>
     * <p><a name="flat"></a><span class="bold">flat</span> - relative level area within
     *  a region of greater relief (clearing, glade, playa). </p>
     * <p><a name="forest"></a><span class="bold">forest</span> - bounded area of woods,
     *  forest, or grassland under the administration of a political agency (see &quot;woods&quot;)
     *  (national forest, national grasslands, State forest). </p>
     * <p><a name="gap"></a><span class="bold">gap</span> - low point or opening between
     *  hills or mountains or in a ridge or mountain range (col, notch, pass, saddle,
     *  water gap, wind gap). </p>
     * <p><a name="geyser"></a><span class="bold">geyser</span> - eruptive spring from
     *  which hot water and (or) steam and in some cases mud are periodically thrown. </p>
     * <p><a name="glacier"></a><span class="bold">glacier</span> - body or stream of
     *  ice moving outward and downslope from an area of accumulation; an area of relatively
     *  permanent snow or ice on the top or side of a mountain or mountainous area
     *  (icefield, ice patch, snow patch). </p>
     * <p><a name="gut"></a><span class="bold">gut</span> - relatively small coastal
     *  waterway connecting larger bodies of water or other waterways (creek, inlet,
     *  slough). </p>
     * <p><a name="harbor"></a><span class="bold">harbor</span> - sheltered area of
     *  water where ships or other watercraft can anchor or dock (hono, port, roads,
     *  roadstead). </p>
     * <p><a name="hospital"></a><span class="bold">hospital</span> - building where
     *  the sick or injured may receive medical or surgical attention (infirmary). </p>
     * <p><a name="island"></a><span class="bold">island</span> - area of dry or relatively
     *  dry land surrounded by water or low wetland (archipelago, atoll, cay, hammock,
     *  hummock, isla, isle, key, moku, rock). </p>
     * <p><a name="isthmus"></a><span class="bold">isthmus</span> - narrow section of
     *  land in a body of water connecting two larger land areas. </p>
     * <p><a name="lake"></a><span class="bold">lake</span> - natural body of inland
     *  water (backwater, lac, lagoon, laguna, pond, pool, resaca, waterhole). </p>
     * <p><a name="lava"></a><span class="bold">lava</span> - formations resulting from
     *  the consolidation of molten rock on the surface of the Earth (kepula, lava
     *  flow). </p>
     * <p><a name="levee"></a><span class="bold">levee</span> - natural or manmade embankment
     *  flanking a stream (bank, berm). </p>
     * <p><a name="locale"></a><span class="bold">locale</span> - place at which there
     *  is or was human activity; it does not include populated places, mines, and
     *  dams (battlefield, crossroad, camp, farm, ghost town, landing, railroad siding,
     *  ranch, ruins, site, station, windmill). </p>
     * <p><a name="mine"></a><span class="bold">mine</span> - place or area from which
     *  commercial minerals are or were removed from the Earth; not including oilfield
     *  (pit, quarry, shaft). </p>
     * <p><a name="military"></a><span class="bold">military (historical)</span> - place
     *  or facility formerly used for various aspects of or relating to military activity. </p>
     * <p><a name="oilfield"></a><span class="bold">oilfield</span> - area where petroleum
     *  is or was removed from the Earth. </p>
     * <p><a name="other"></a><span class="bold">other</span> - category for miscellaneous
     *  named entities that cannot readily be placed in the other feature classes listed
     *  here. </p>
     * <p><a name="park"></a><span class="bold">park</span> - place or area set aside
     *  for recreation or preservation of a cultural or natural resource and under
     *  some form of government administration; not including National or State forests
     *  or Reserves (national historical landmark, national park, State park, wilderness
     *  area). </p>
     * <p><a name="pillar"></a><span class="bold">pillar</span> - vertical, standing,
     *  often spire-shaped, natural rock formation (chimney, monument, pinnacle, pohaku,
     *  rock tower). </p>
     * <p><a name="plain"></a><span class="bold">plain</span> - a region of general
     *  uniform slope, comparatively level and of considerable extent (grassland, highland,
     *  kula, plateau, upland). </p>
     * <p><a name="po"></a><span class="bold">Post Office</span> - (Formerly abbreviated
     *  as PO) an official facility of the U.S. Postal Service used for processing
     *  and distributing mail and other postal material. </p>
     * <p><a name="ppl"></a><span class="bold">Populated Place</span> - (Formerly abbreviated
     *  as ppl) place or area with clustered or scattered buildings and a permanent
     *  human population (city, settlement, town, village). </p>
     * <p><a name="range"></a><span class="bold">range</span> - chain of hills or mountains;
     *  a somewhat linear, complex mountainous or hilly area (cordillera, sierra). </p>
     * <p><a name="rapids"></a><span class="bold">rapids</span> - fast-flowing section
     *  of a stream, often shallow and with exposed rock or boulders (riffle, ripple). </p>
     * <p><a name="reserve"></a><span class="bold">reserve</span> - a tract of land
     *  set aside for a specific use (does not include forests, civil divisions, parks). </p>
     * <p><a name="reservoir"></a><span class="bold">reservoir</span> - artificially
     *  impounded body of water (lake, tank). </p>
     * <p><a name="ridge"></a><span class="bold">ridge</span> - elevation with a narrow,
     *  elongated crest which can be part of a hill or mountain (crest, cuesta, escarpment,
     *  hogback, lae, rim, spur). </p>
     * <p><a name="school"></a><span class="bold">school</span> - building or group
     *  of buildings used as an institution for study, teaching, and learning (academy,
     *  college, high school, university). </p>
     * <p><a name="sea"></a><span class="bold">sea</span> - large body of salt water
     *  (gulf, ocean). </p>
     * <p><a name="slope"></a><span class="bold">slope</span> - a gently inclined part
     *  of the Earth's surface (grade, pitch). </p>
     * <p><a name="spring"></a><span class="bold">spring</span> - place where underground
     *  water flows naturally to the surface of the Earth (seep). </p>
     * <p><a name="stream"></a><span class="bold">stream</span> - linear body of water
     *  flowing on the Earth's surface (anabranch, awawa, bayou, branch, brook, creek,
     *  distributary, fork, kill, pup, rio, river, run, slough). </p>
     * <p><a name="summit"></a><span class="bold">summit</span> - prominent elevation
     *  rising above the surrounding level of the Earth's surface; does not include
     *  pillars, ridges, or ranges (ahu, berg, bald, butte, cerro, colina, cone, cumbre,
     *  dome, head, hill, horn, knob, knoll, mauna, mesa, mesita, mound, mount, mountain,
     *  peak, puu, rock, sugarloaf, table, volcano). </p>
     * <p><a name="swamp"></a><span class="bold">swamp</span> - poorly drained wetland,
     *  fresh or saltwater, wooded or grassy, possibly covered with open water (bog,
     *  cienega, marais, marsh, pocosin). </p>
     * <p><a name="tower"></a><span class="bold">tower</span> - a manmade structure,
     *  higher than its diameter, generally used for observation, storage, or electronic
     *  transmission. </p>
     * <p><a name="trail"></a><span class="bold">trail</span> - route for passage from
     *  one point to another; does not include roads or highways (jeep trail, path,
     *  ski trail). </p>
     * <p><a name="tunnel"></a><span class="bold">tunnel</span> - linear underground
     *  passageway open at both ends. </p>
     * <p><a name="unknown"></a><a name="valley"></a><span class="bold">valley</span> -
     *  linear depression in the Earth's surface that generally slopes from one end
     *  to the other (barranca, canyon, chasm, cove, draw, glen, gorge, gulch, gulf,
     *  hollow, ravine). </p>
     * <p><a name="well"></a><span class="bold">well</span> - manmade shaft or hole
     *  in the Earth's surface used to obtain fluid or gaseous materials. </p>
     * <a name="woods"></a><span class="bold"> woods</span> - small area covered with
     * a dense growth of trees; does not include an area of trees under the administration
     * of a political agency (see &quot;forest&quot;).
     * </blockquote>
     * @param outputDir The directory where the sites should be allocated
     * @param limits A four element array indicating the geographic limits for the search.  The array
     * elements are {minLatitude, maxLatitude, minLongitude, maxLongitude}.
     */
    public ImportUSGS_StateGazetteer(String state,String[] validFeatures, String outputDir, float[] limits) {
        
        java.util.Vector validFeatureTypes=new java.util.Vector();
        for (int i=0;i<validFeatures.length;i++){
            validFeatureTypes.addElement(validFeatures[i]);
        }
        
        try{
            java.net.URL remotePath=new java.net.URL("http://cuencas.colorado.edu/geonames/"+state+"_DECI.txt");
            java.io.InputStream inputRemoto=remotePath.openStream();
            java.io.BufferedReader bufferRemoto = new java.io.BufferedReader(new java.io.InputStreamReader(inputRemoto));

            String locationInfo=bufferRemoto.readLine();
            locationInfo=bufferRemoto.readLine();
            
            while (locationInfo != null){
                
                String[] locProps=locationInfo.split("\t");
                
                USGS_LocationProperties thisLocation=new USGS_LocationProperties(state);
                thisLocation.StateAlphaCode=locProps[3];
                thisLocation.State=hydroScalingAPI.tools.StateName.CodeOrNameToStandardName(state);
                thisLocation.Name=locProps[1];
                thisLocation.Type=locProps[2];
                thisLocation.County=locProps[5];
                thisLocation.latitude=locProps[7].substring(0,2)+":"+locProps[7].substring(2,4)+":"+locProps[7].substring(4,6)+".00 N";
                thisLocation.latitudeNumeric=Float.parseFloat(locProps[9]);
                if(locProps[8].length() == 8)
                    thisLocation.longitude=locProps[8].substring(0,3)+":"+locProps[8].substring(3,5)+":"+locProps[8].substring(5,7)+".00 W";
                else
                    thisLocation.longitude=locProps[8].substring(0,2)+":"+locProps[8].substring(2,4)+":"+locProps[8].substring(4,6)+".00 W";
                thisLocation.longitudeNumeric=Float.parseFloat(locProps[10]);
                thisLocation.Elevation=locProps[15];
                
                if (thisLocation.Elevation.equalsIgnoreCase("")) thisLocation.Elevation="N/A";
                
                if (validFeatureTypes.contains(thisLocation.Type)){
                    if(thisLocation.latitudeNumeric > limits[0] && thisLocation.latitudeNumeric < limits[1] && thisLocation.longitudeNumeric > limits[2] && thisLocation.longitudeNumeric < limits[3]){
                        new java.io.File(outputDir+thisLocation.State+"/"+thisLocation.Type+"/").mkdirs();
                        java.io.File theFile = new java.io.File(outputDir+thisLocation.State+"/"+thisLocation.Type+"/"+thisLocation.Name.toLowerCase()+".txt.gz");
                        java.io.FileOutputStream inputLocal=new java.io.FileOutputStream(theFile);
                        java.util.zip.GZIPOutputStream inputComprim=new java.util.zip.GZIPOutputStream(inputLocal);
                        java.io.BufferedWriter bufferLocalW= new java.io.BufferedWriter(new java.io.OutputStreamWriter(inputComprim));

                        System.out.println(theFile);

                        bufferLocalW.write("[type]"+"\n");
                        bufferLocalW.write(thisLocation.Type+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[source]"+"\n");
                        bufferLocalW.write("USGS"+"\n");
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
                        for(int j=0;j<thisLocation.Images.length;j++) bufferLocalW.write(thisLocation.Images[j]+"\n");
                        bufferLocalW.write("\n");
                        bufferLocalW.write("[information]"+"\n");
                        bufferLocalW.write(thisLocation.Source+"\n");
                        bufferLocalW.write("\n");

                        bufferLocalW.close();
                        inputComprim.close();
                        inputLocal.close();
                    }
                }
                locationInfo=bufferRemoto.readLine();
            }
            bufferRemoto.close();
            inputRemoto.close();
            
            
        }catch(java.net.MalformedURLException MUE){
            System.err.println(MUE);
        }catch(java.io.IOException IOE){
            System.err.println(IOE);
        }

    }
    
    /**
     * Tests for the class.
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        float[] limits = {25,50,-126,-66};
        new ImportUSGS_StateGazetteer("CO",new String[] {"Valley"},"/tmp/locations/",limits);
    }
    
}

class USGS_LocationProperties {
    public String StateAlphaCode;
    public String State;
    public String Name;
    public String Type;
    public String County;
    public String latitude;
    public float latitudeNumeric;
    public String longitude;
    public float longitudeNumeric;
    public String Source="United States Geological Survey - Geographic Names Information System - Downloadable State and Topical Gazetteer Files - http://geonames.usgs.gov/geonames/stategaz/index.html";
    public String Elevation;
    public String[] Images={"N/A"};
    public String CellName;
    
    public USGS_LocationProperties(String st) {
        State=st;
    }
   
}

