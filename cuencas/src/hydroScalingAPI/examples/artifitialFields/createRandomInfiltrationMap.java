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
 * createInfiltrationMap.java
 *
 * Created on May 27, 2004, 4:50 PM
 */

package hydroScalingAPI.examples.artifitialFields;

/**
 *
 * @author Ricardo Mantilla
 */
public class createRandomInfiltrationMap {
    
    //A group of selected events for basin 104
    /*private double[] rateToMax={    0.101531982,    0.139431000,    0.150144100,    0.156555176,    0.318721771,    0.322748184,    0.373229980,    0.380313873,    0.388904572,    0.397262573,    0.423950195,    0.437835693,    0.472072601,    0.481353760,    0.486871719,
                                    0.498489380,    0.511512756,    0.514207840,    0.553653717,    0.555084229,    0.566726685,    0.582290649,    0.596569061,    0.601547241,    0.602231979,    0.603561401,    0.603668213,    0.616333008,    0.617645264,    0.620895386,
                                    0.638240814,    0.643562317,    0.643749237,    0.648422241,    0.651542664,    0.657958984,    0.670433044,    0.670623779,    0.671890259,    0.675685883,    0.682342529,    0.690429688,    0.690773010,    0.693382263,    0.695312500,
                                    0.697792053,    0.721199036,    0.722839355,    0.728012085,    0.730209351,    0.733949661,    0.734519958,    0.736000061,    0.741554260,    0.746337891,    0.751434326,    0.764343262,    0.766632080,    0.767356873,    0.768447876,
                                    0.770996094,    0.772247314,    0.772697449,    0.779317856,    0.780334473,    0.786621094,    0.802352905,    0.816741943,    0.829204559,    0.833805084,    0.833984375,    0.840610504,    0.860595703,    0.864685059,    0.867324829,
                                    0.867919922,    0.870788574,    0.872222900,    0.875362396,    0.875701904,    0.886230469,    0.890815735,    0.900726318,    0.901987076,    0.902511597,    0.902900696,    0.906173706,    0.908203125,    0.910087585,    0.911178589,
                                    0.912353516,    0.913818359,    0.916610718,    0.919555664,    0.930534363,    0.931640625,    0.932376862,    0.937435150,    0.943794250,    0.945800781,    0.949188232,    0.955703735,    0.956069946,    0.956192017,    0.956726074,
                                    0.958766937,    0.960693359,    0.966979980,    0.968429565,    0.971046448,    0.973205566,    0.973861694,    0.974868774,    0.976642609,    0.979293823,    0.979698181,    0.980712891,    0.984359741,    0.988830566,    0.989349365,
                                    0.990097046,    0.992553711,    0.996116638,    0.997711182,    0.998107910,    0.999084473,    0.999298096,    0.999877930,    0.999908447,    0.999908447};*/
                                    
    //A group of selected events for basins 104 and 121
    private double[] rateToMax={    0.101531982,    0.139431000,    0.150144100,    0.156555176,    0.318721771,    0.322748184,    0.371444702,    0.373189926,    0.373229980,    0.380313873,    0.388904572,    0.391946793,    0.397262573,    0.423950195,    0.429566383,
                                    0.437835693,    0.467685699,    0.472072601,    0.481353760,    0.486871719,    0.498489380,    0.511512756,    0.512374878,    0.514207840,    0.519287109,    0.535867691,    0.547252655,    0.553653717,    0.554703712,    0.555084229,
                                    0.555774689,    0.558906555,    0.562133789,    0.566726685,    0.582290649,    0.596569061,    0.597911835,    0.599948883,    0.601547241,    0.602231979,    0.603561401,    0.603668213,    0.616333008,    0.617645264,    0.619171143,
                                    0.620895386,    0.620903015,    0.638240814,    0.643562317,    0.643749237,    0.645439148,    0.645660400,    0.648422241,    0.651542664,    0.653509140,    0.657958984,    0.666076660,    0.670433044,    0.670623779,    0.671890259,
                                    0.675685883,    0.675781250,    0.676757813,    0.682342529,    0.683334351,    0.686401367,    0.690429688,    0.690773010,    0.693382263,    0.695312500,    0.697257996,    0.697792053,    0.721199036,    0.722839355,    0.726799011,
                                    0.728012085,    0.728523254,    0.730209351,    0.732444763,    0.733949661,    0.734519958,    0.736000061,    0.736755371,    0.741554260,    0.746337891,    0.749633789,    0.751434326,    0.757339478,    0.762430191,    0.764343262,
                                    0.766632080,    0.767356873,    0.768447876,    0.768920898,    0.770599365,    0.770996094,    0.772247314,    0.772697449,    0.773002625,    0.773635864,    0.777381897,    0.777385712,    0.779317856,    0.779983521,    0.780334473,
                                    0.783645630,    0.786064148,    0.786621094,    0.792501450,    0.798007965,    0.800872803,    0.802352905,    0.802379608,    0.807758331,    0.812162399,    0.816741943,    0.818534851,    0.820495605,    0.823165894,    0.824632645,
                                    0.824737549,    0.829204559,    0.832214355,    0.833805084,    0.833984375,    0.836601257,    0.840610504,    0.841026306,    0.846370697,    0.848388672,    0.854370117,    0.854690552,    0.860595703,    0.863800049,    0.864517212,
                                    0.864685059,    0.867324829,    0.867919922,    0.870117187,    0.870788574,    0.872222900,    0.873908997,    0.875362396,    0.875701904,    0.876419067,    0.880950928,    0.883224487,    0.886230469,    0.887794495,    0.890815735,
                                    0.896240234,    0.899627686,    0.900726318,    0.901987076,    0.902511597,    0.902900696,    0.906173706,    0.908203125,    0.910087585,    0.910156250,    0.911178589,    0.911254883,    0.912353516,    0.913818359,    0.914306641,
                                    0.915100098,    0.915611267,    0.915832520,    0.916610718,    0.919555664,    0.919960022,    0.924232483,    0.925292969,    0.926849365,    0.930534363,    0.931640625,    0.932159424,    0.932376862,    0.935852051,    0.937435150,
                                    0.943794250,    0.945800781,    0.949188232,    0.949703217,    0.951293945,    0.951843262,    0.953659058,    0.955337524,    0.955703735,    0.956069946,    0.956192017,    0.956726074,    0.957275391,    0.957489014,    0.958496094,
                                    0.958766937,    0.959762573,    0.960693359,    0.963165283,    0.965164185,    0.966979980,    0.967773438,    0.968429565,    0.971046448,    0.971092224,    0.973022461,    0.973205566,    0.973861694,    0.974868774,    0.976074219,
                                    0.976642609,    0.978446960,    0.979293823,    0.979698181,    0.979980469,    0.980712891,    0.984359741,    0.984611511,    0.988830566,    0.989349365,    0.990097046,    0.992553711,    0.996116638,    0.997711182,    0.998107910,
                                    0.999084473,    0.999298096,    0.999877930,    0.999908447,    0.999908447};
                                    
    //All the events for basin 104 ad 121
    /*private double[] rateToMax={    0.000885010,    0.101531982,    0.107233047,    0.120670319,    0.139431000,    0.150144100,    0.156555176,    0.177520752,    0.240516663,    0.306701660,    0.314559937,    0.318721771,    0.322748184,    0.334487915,    0.342411041,
                                    0.350341797,    0.358448029,    0.371444702,    0.373189926,    0.373229980,    0.377517700,    0.380313873,    0.388904572,    0.391946793,    0.397262573,    0.408264160,    0.413391113,    0.415534973,    0.421112061,    0.423950195,
                                    0.425365448,    0.429117203,    0.429566383,    0.433181763,    0.437835693,    0.453643799,    0.464302063,    0.467685699,    0.472072601,    0.475669861,    0.481353760,    0.485038757,    0.485183716,    0.486871719,    0.498489380,
                                    0.506055832,    0.511512756,    0.512374878,    0.514207840,    0.519287109,    0.523146629,    0.533325195,    0.535867691,    0.537109375,    0.545406342,    0.547252655,    0.548742294,    0.553653717,    0.554703712,    0.555084229,
                                    0.555774689,    0.558906555,    0.562133789,    0.566726685,    0.567085266,    0.578560829,    0.582290649,    0.583890915,    0.589191437,    0.590045929,    0.595649719,    0.597911835,    0.599948883,    0.600008011,    0.601547241,
                                    0.602231979,    0.603561401,    0.603668213,    0.607243538,    0.615386963,    0.616333008,    0.617645264,    0.618743896,    0.619171143,    0.620895386,    0.620903015,    0.623950958,    0.627012253,    0.628738403,    0.636148453,
                                    0.637157440,    0.638240814,    0.643562317,    0.643749237,    0.644287109,    0.645439148,    0.645660400,    0.647460938,    0.648422241,    0.651542664,    0.651794434,    0.653509140,    0.654907227,    0.657958984,    0.660812378,
                                    0.666076660,    0.668823242,    0.670433044,    0.670623779,    0.671890259,    0.672111511,    0.673301697,    0.674663544,    0.675685883,    0.675781250,    0.676757813,    0.682342529,    0.683334351,    0.684417725,    0.685504913,
                                    0.686401367,    0.688385010,    0.690429688,    0.690773010,    0.693382263,    0.694335937,    0.695312500,    0.697257996,    0.697792053,    0.698471069,    0.700443268,    0.700634003,    0.703723907,    0.707885742,    0.715824127,
                                    0.718706131,    0.719940186,    0.721199036,    0.722839355,    0.723770142,    0.726799011,    0.728012085,    0.728523254,    0.730209351,    0.732444763,    0.733949661,    0.734519958,    0.736000061,    0.736755371,    0.741554260,
                                    0.743196487,    0.746337891,    0.749633789,    0.750656128,    0.751342773,    0.751434326,    0.757339478,    0.758697510,    0.762430191,    0.764343262,    0.766632080,    0.767356873,    0.768447876,    0.768920898,    0.770034790,
                                    0.770599365,    0.770996094,    0.771528244,    0.771759033,    0.772247314,    0.772399902,    0.772697449,    0.773002625,    0.773635864,    0.777381897,    0.777385712,    0.779317856,    0.779441833,    0.779983521,    0.780334473,
                                    0.780483246,    0.781005859,    0.783645630,    0.784141541,    0.784652710,    0.786064148,    0.786621094,    0.792037964,    0.792501450,    0.794151306,    0.798007965,    0.800872803,    0.802352905,    0.802379608,    0.807758331,
                                    0.808486938,    0.809356689,    0.812162399,    0.812576294,    0.816253662,    0.816741943,    0.817653656,    0.818534851,    0.820495605,    0.821487427,    0.823165894,    0.824249268,    0.824632645,    0.824737549,    0.825714111,
                                    0.829204559,    0.832214355,    0.833805084,    0.833984375,    0.835300446,    0.835540771,    0.836601257,    0.839523315,    0.840610504,    0.841026306,    0.844245911,    0.846370697,    0.847030640,    0.848388672,    0.849643707,
                                    0.851333618,    0.852478027,    0.853122711,    0.853790283,    0.854183197,    0.854370117,    0.854690552,    0.860595703,    0.863800049,    0.864517212,    0.864685059,    0.866744995,    0.867013931,    0.867279053,    0.867324829,
                                    0.867919922,    0.868621826,    0.870117187,    0.870788574,    0.872222900,    0.873908997,    0.875362396,    0.875701904,    0.876285553,    0.876419067,    0.878623962,    0.880249023,    0.880371094,    0.880493164,    0.880950928,
                                    0.882080078,    0.883224487,    0.883872986,    0.885581970,    0.886230469,    0.887794495,    0.890548706,    0.890815735,    0.891036987,    0.891052246,    0.896240234,    0.896812439,    0.897583008,    0.897583008,    0.898681641,
                                    0.898696899,    0.899200439,    0.899627686,    0.900726318,    0.901987076,    0.902511597,    0.902900696,    0.903015137,    0.904228210,    0.905204773,    0.905975342,    0.906173706,    0.908203125,    0.910087585,    0.910156250,
                                    0.911178589,    0.911254883,    0.911254883,    0.911499023,    0.912353516,    0.913391113,    0.913818359,    0.914306641,    0.915100098,    0.915611267,    0.915832520,    0.915863037,    0.916610718,    0.918273926,    0.918312073,
                                    0.918395996,    0.919555664,    0.919677734,    0.919960022,    0.920867920,    0.921539307,    0.922706604,    0.924232483,    0.924873352,    0.925292969,    0.926849365,    0.927959442,    0.928955078,    0.929687500,    0.930534363,
                                    0.931522369,    0.931640625,    0.932159424,    0.932376862,    0.934165955,    0.934494019,    0.935852051,    0.936412811,    0.937377930,    0.937435150,    0.937500000,    0.939208984,    0.940734863,    0.942016602,    0.943252563,
                                    0.943359375,    0.943794250,    0.944290161,    0.945495605,    0.945800781,    0.946166992,    0.946571350,    0.946655273,    0.948768616,    0.949188232,    0.949703217,    0.950805664,    0.951293945,    0.951843262,    0.952255249,
                                    0.952835083,    0.953659058,    0.955337524,    0.955581665,    0.955703735,    0.956069946,    0.956192017,    0.956726074,    0.957073212,    0.957275391,    0.957489014,    0.958496094,    0.958766937,    0.958831787,    0.958900452,
                                    0.959440231,    0.959762573,    0.960029602,    0.960632324,    0.960693359,    0.961013794,    0.962310791,    0.963165283,    0.963386536,    0.963562012,    0.964035034,    0.964119911,    0.964736938,    0.965057373,    0.965164185,
                                    0.965511322,    0.965560913,    0.966979980,    0.967773438,    0.968383789,    0.968429565,    0.968879700,    0.969482422,    0.970092773,    0.970436096,    0.970764160,    0.971046448,    0.971092224,    0.971565247,    0.972671509,
                                    0.972671509,    0.973022461,    0.973022461,    0.973205566,    0.973251343,    0.973312378,    0.973312378,    0.973617554,    0.973739624,    0.973838806,    0.973861694,    0.974868774,    0.975219727,    0.975345612,    0.975769043,
                                    0.976062775,    0.976074219,    0.976642609,    0.976787567,    0.976928711,    0.976989746,    0.976989746,    0.977531433,    0.978446960,    0.978637695,    0.979011536,    0.979293823,    0.979492188,    0.979698181,    0.979785919,
                                    0.979980469,    0.980468750,    0.980501175,    0.980712891,    0.980712891,    0.981323242,    0.981689453,    0.982326508,    0.982772827,    0.982910156,    0.983757019,    0.984359741,    0.984436035,    0.984443665,    0.984611511,
                                    0.985412598,    0.985633850,    0.985984802,    0.986618042,    0.986846924,    0.987060547,    0.987060547,    0.987289429,    0.987854004,    0.988029480,    0.988159180,    0.988739014,    0.988830566,    0.988883972,    0.989120483,
                                    0.989196777,    0.989196777,    0.989196777,    0.989227295,    0.989349365,    0.989547729,    0.989593506,    0.989730835,    0.990097046,    0.990234375,    0.990402222,    0.991102219,    0.991455078,    0.991470337,    0.991500854,
                                    0.991622925,    0.992279053,    0.992713928,    0.992858887,    0.993087769,    0.993103027,    0.993255615,    0.993377686,    0.993415833,    0.993682861,    0.993865967,    0.993865967,    0.994049072,    0.994081497,    0.994140625,
                                    0.994361877,    0.994384766,    0.994506836,    0.994720459,    0.995117188,    0.995212555,    0.995536804,    0.995700836,    0.995819092,    0.996116638,    0.996215820,    0.996284485,    0.996292114,    0.996520996,    0.996551514,
                                    0.996627808,    0.996833801,    0.996902466,    0.996948242,    0.997283936,    0.997413635,    0.997482300,    0.997497559,    0.997707367,    0.997707367,    0.997711182,    0.998046875,    0.998046875,    0.998107910,    0.998138428,
                                    0.998146057,    0.998268127,    0.998321533,    0.998352051,    0.998382568,    0.998420715,    0.998535156,    0.998657227,    0.998809814,    0.998996735,    0.999015808,    0.999027252,    0.999084473,    0.999099731,    0.999158859,
                                    0.999193192,    0.999252319,    0.999267578,    0.999298096,    0.999443054,    0.999511719,    0.999603271,    0.999664307,    0.999679565,    0.999706268,    0.999771118,    0.999774933,    0.999816895,    0.999832153,    0.999832153,
                                    0.999877930,    0.999908447,    0.999908447,    0.999916077,    0.999938965,    0.999938965,    0.999938965,    0.999938965,    0.999980927,    0.999984741};*/
                                    
    
    /** Creates a new instance of createInfiltrationMap */
    public createRandomInfiltrationMap(int x, int y, java.io.File baseMetaDEM,java.io.File stormsDir, java.io.File outputDir) throws java.io.IOException{
        
        hydroScalingAPI.io.MetaRaster metaData=new hydroScalingAPI.io.MetaRaster(baseMetaDEM);
        java.io.File originalFile=metaData.getLocationMeta();
        
        metaData.setLocationBinaryFile(new java.io.File(originalFile.getParent()+"/"+originalFile.getName().substring(0,originalFile.getName().lastIndexOf("."))+".dir"));
        metaData.setFormat("Byte");
        byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaData).getByte();
        
        metaData.setLocationBinaryFile(new java.io.File(originalFile.getPath().substring(0,originalFile.getPath().lastIndexOf("."))+".magn"));
        metaData.setFormat("Integer");
        int [][] magnitudes=new hydroScalingAPI.io.DataRaster(metaData).getInt();
        
        //Here an example of rainfall-runoff in action
        hydroScalingAPI.util.geomorphology.objects.Basin myCuenca=new hydroScalingAPI.util.geomorphology.objects.Basin(x,y,matDirs,metaData);
        hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksStructure=new hydroScalingAPI.util.geomorphology.objects.LinksAnalysis(myCuenca, metaData, matDirs);
        
        hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo thisNetworkGeom=new hydroScalingAPI.modules.rainfallRunoffModel.objects.LinksInfo(linksStructure);
        hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo thisHillsInfo=new hydroScalingAPI.modules.rainfallRunoffModel.objects.HillSlopesInfo(linksStructure);
        
        
        float evID=0.001f;
        
        int xOulet,yOulet;
        hydroScalingAPI.util.geomorphology.objects.HillSlope myHillActual;
        
        int demNumCols=metaData.getNumCols();

        int basinMinX=myCuenca.getMinX();
        int basinMinY=myCuenca.getMinY();
        
        for (int i=0;i<20;i++) {
            
            System.out.println("Loading Storm ...");
        
            String thisInputStorm=stormsDir.getPath()+java.io.File.separator+"event_"+Float.toString(evID).substring(2,4)+java.io.File.separator+"precipitation_interpolated_ev"+Float.toString(evID).substring(2,4)+".metaVHC";
            
            java.io.File stormFile=new java.io.File(thisInputStorm);
            hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager storm=new hydroScalingAPI.modules.rainfallRunoffModel.objects.StormManager(stormFile,myCuenca,linksStructure,metaData,matDirs,magnitudes);
            if (!storm.isCompleted()) return;

            thisHillsInfo.setStormManager(storm);
            
            float[][] newMatrix=new float[metaData.getNumRows()][metaData.getNumCols()];
            
            for(int j=0;j<linksStructure.contactsArray.length;j++){
                
                xOulet=linksStructure.contactsArray[j]%demNumCols;
                yOulet=linksStructure.contactsArray[j]/demNumCols;
                
                myHillActual=new hydroScalingAPI.util.geomorphology.objects.HillSlope(xOulet,yOulet,matDirs,magnitudes,metaData);
                int[][] xyHillSlope=myHillActual.getXYHillSlope();
                
                
                float meanInfiltrationRate=thisHillsInfo.maxPrecipitation(j)*(float)rateToMax[(int)(Math.random()*(rateToMax.length-1))];
                
                for(int k=0;k<xyHillSlope[0].length;k++){
                    newMatrix[xyHillSlope[1][k]][xyHillSlope[0][k]]=meanInfiltrationRate;
                }
            }
            
            String thisOutputDir=outputDir.getPath()+java.io.File.separator+"event"+Float.toString(evID).substring(2,4);
        
            new java.io.File(thisOutputDir).mkdirs();
            createMetaFile(new java.io.File(thisOutputDir),"infiltrationRandomRate_event"+Float.toString(evID).substring(2,4),metaData);

            java.io.File saveFile=new java.io.File(thisOutputDir+java.io.File.separator+"infiltrationRandomRate_event"+Float.toString(evID).substring(2,4)+".vhc");
            java.io.DataOutputStream writer = new java.io.DataOutputStream(new java.io.BufferedOutputStream(new java.io.FileOutputStream(saveFile)));

            for (int yc=0;yc<metaData.getNumRows();yc++) {
                for (int xc=0;xc<metaData.getNumCols();xc++) {
                    writer.writeFloat(newMatrix[yc][xc]);;
                }
            }
            writer.close();
            
            evID+=0.01;
            
        }
        
    }
    
    public void createMetaFile(java.io.File directory, String newMetaName, hydroScalingAPI.io.MetaRaster originalMeta) {
          try{          
              java.io.File saveFile=new java.io.File(directory.getPath()+java.io.File.separator+newMetaName+".metaVHC");
              java.io.PrintWriter writer = new java.io.PrintWriter(new java.io.FileWriter(saveFile)); 
              writer.println("[Name]");
              writer.println("Precipitation Radar Data From KICT");
              writer.println(""); 
              writer.println("[Southernmost Latitude]"); 
              writer.println(hydroScalingAPI.tools.DegreesToDMS.getprettyString(originalMeta.getMinLat(),0));
              writer.println(""); 
              writer.println("[Westernmost Longitude]");         
              writer.println(hydroScalingAPI.tools.DegreesToDMS.getprettyString(originalMeta.getMinLon(),1));
              writer.println(""); 
              writer.println("[Longitudinal Resolution (ArcSec)]");
              writer.println(originalMeta.getResLat());
              writer.println(""); 
              writer.println("[Latitudinal Resolution (ArcSec)]");
              writer.println(originalMeta.getResLon());
              writer.println(""); 
              writer.println("[# Columns]");
              writer.println(originalMeta.getNumCols());
              writer.println(""); 
              writer.println("[# Rows]");
              writer.println(originalMeta.getNumRows());
              writer.println(""); 
              writer.println("[Format]");
              writer.println("Float");
              writer.println(""); 
              writer.println("[Missing]");
              writer.println("0");
              writer.println(""); 
              writer.println("[Temporal Resolution]");
              writer.println("fixed");
              writer.println(""); 
              writer.println("[Units]");
              writer.println("mm/h");
              writer.println(""); 
              writer.println("[Information]");
              writer.println("Infiltration map desicgned to conserve mass from events");
              writer.close();
         } catch (java.io.IOException bs) {
             System.out.println("Error composing metafile: "+bs);
         }

     }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            new createRandomInfiltrationMap(82,260,
                                            new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Topography/1_ArcSec_USGS/walnutGulchUpdated.metaDEM"),
                                            new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/storms/precipitation_events/"),
                                            new java.io.File("/hidrosigDataBases/Walnut_Gulch_AZ_database/Rasters/Hydrology/infiltrationRates/"));
        } catch(java.io.IOException ioe){
            System.err.println(ioe);
        }
    }
    
}
