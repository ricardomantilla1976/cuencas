/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.io;

import hydroScalingAPI.util.geomorphology.objects.LinksAnalysis;

/**
 *
 * @author ricardo
 */
public class IowaBasinsWaterBalance {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{

            java.io.File theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/evaporation/eva2.metaVHC");
            hydroScalingAPI.io.MetaRaster metaModif2=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif2.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/evaporation/eva2.vhc"));
            metaModif2.setFormat("Float");
            float[][] evaporation=new hydroScalingAPI.io.DataRaster(metaModif2).getFloat();

            double minEvaLon=metaModif2.getMinLon();
            double minEvaLat=metaModif2.getMinLat();
            double resEva=metaModif2.getResLon()/3600.;

            theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/precipitation/precip2.metaVHC");
            hydroScalingAPI.io.MetaRaster metaModif1=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif1.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/precipitation/precip2.vhc"));
            metaModif1.setFormat("Float");
            float[][] rainfall=new hydroScalingAPI.io.DataRaster(metaModif1).getFloat();
            
            double minRainLon=metaModif1.getMinLon();
            double minRainLat=metaModif1.getMinLat();
            double resRain=metaModif1.getResLon()/3600.;
            
            theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/aws150.metaVHC");
            hydroScalingAPI.io.MetaRaster metaModif0=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif0.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/aws150.vhc"));
            metaModif0.setFormat("Float");
            float[][] avaStor=new hydroScalingAPI.io.DataRaster(metaModif0).getFloat();
            
            double minStorageLon=metaModif0.getMinLon();
            double minStorageLat=metaModif0.getMinLat();
            double resStorage=metaModif0.getResLon()/3600.;
            
            theFile=new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.metaDEM");
            hydroScalingAPI.io.MetaRaster metaModif=new hydroScalingAPI.io.MetaRaster(theFile);
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.dir"));

            metaModif.setFormat("Byte");
            byte [][] matDirs=new hydroScalingAPI.io.DataRaster(metaModif).getByte();

            metaModif.setFormat("Float");
            metaModif.setLocationBinaryFile(new java.io.File("/CuencasDataBases/Iowa_Rivers_DB/Rasters/Topography/4_arcsec/res.areas"));
            float [][] matAreas=new hydroScalingAPI.io.DataRaster(metaModif).getFloat();

            float [][] runoffCoefficients=new float[matAreas.length][matAreas[0].length];
            
            for (int ll = 0; ll < codes.length; ll++) {
                
                    
                    int ii=coord[ll][1];
                    int jj=coord[ll][0];
                
                    hydroScalingAPI.util.geomorphology.objects.Basin myBasin=new hydroScalingAPI.util.geomorphology.objects.Basin(jj,ii,matDirs,metaModif);

                    float[][] lonLatBasin=myBasin.getLonLatBasin();

                    float accumRain=0;
                    float accumEva=0;
                    float accumSto=0;

                    float counterSto=0,counterRain=0,counterEva=0;
                    for (int i = 0; i < lonLatBasin[0].length; i++) {


                        int x=(int)((lonLatBasin[0][i]-minRainLon)/resRain);
                        int y=(int)((lonLatBasin[1][i]-minRainLat)/resRain);
                        if(x>0 && x<rainfall[0].length && y>0 && y<rainfall.length){
                            if(rainfall[y][x] != -9999){
                                accumRain+=rainfall[y][x];
                                counterRain++;
                            }
                        }

                        x=(int)((lonLatBasin[0][i]-minEvaLon)/resEva);
                        y=(int)((lonLatBasin[1][i]-minEvaLat)/resEva);
                        if(x>0 && x<evaporation[0].length && y>0 && y<evaporation.length){
                            if(evaporation[y][x] != -99){
                                accumEva+=evaporation[y][x];
                                counterEva++;
                            }
                        }

                        x=(int)((lonLatBasin[0][i]-minStorageLon)/resStorage);
                        y=(int)((lonLatBasin[1][i]-minStorageLat)/resStorage);
                        if(avaStor[y][x] != -9999){
                            accumSto+=avaStor[y][x];
                            counterSto++;
                        }

                    }
                    accumRain/=counterRain;
                    accumEva/=counterEva;
                    accumSto/=counterSto;

                    runoffCoefficients[ii][jj]=(accumRain-accumEva)/accumRain;

                    //System.out.println("ii = "+ii+" jj = "+jj+" RC = "+runoffCoefficients[ii][jj]+" Discharge = "+3.1710E-5*(accumRain-accumEva)*matAreas[myBasin.getXYBasin()[1][0]][myBasin.getXYBasin()[0][0]]+" Rain = "+accumRain+" Evaporation = "+accumEva+" Area = "+matAreas[myBasin.getXYBasin()[1][0]][myBasin.getXYBasin()[0][0]]);
                    System.out.println("ii = "+ii+" jj = "+jj+" code = "+codes[ll]+" Storage = "+(accumSto*10)+" RC = "+runoffCoefficients[ii][jj]+" Discharge = "+3.1710E-5*(accumRain-accumEva)*matAreas[myBasin.getXYBasin()[1][0]][myBasin.getXYBasin()[0][0]]+" Rain = "+accumRain+" Evaporation = "+accumEva+" Area = "+matAreas[myBasin.getXYBasin()[1][0]][myBasin.getXYBasin()[0][0]]);
                        
                   
                
            }
            
            String fileAscSalida="/CuencasDataBases/Iowa_Rivers_DB/Rasters/Hydrology/runoffCoeff.asc";
        
            java.io.FileOutputStream        outputDir;
            java.io.OutputStreamWriter      newfile;
            java.io.BufferedOutputStream    bufferout;
            String                          retorno="\n";

            outputDir = new java.io.FileOutputStream(fileAscSalida);
            bufferout=new java.io.BufferedOutputStream(outputDir);
            newfile=new java.io.OutputStreamWriter(bufferout);

            int nc=metaModif.getNumCols();
            int nr=metaModif.getNumRows();

            float missing=Float.parseFloat(metaModif.getMissing());

            newfile.write("ncols         "+metaModif.getNumCols()+retorno);
            newfile.write("nrows         "+metaModif.getNumRows()+retorno);
            newfile.write("xllcorner     "+metaModif.getMinLon()+retorno);
            newfile.write("yllcorner     "+metaModif.getMinLat()+retorno);
            newfile.write("cellsize      "+(metaModif.getResLat()/3600.0D)+retorno);
            newfile.write("NODATA_value  "+"0"+retorno);


            for (int i=(nr-1);i>=0;i--) {
                for (int j=0;j<nc;j++) {
                    newfile.write(runoffCoefficients[i][j]+" ");
                }
                newfile.write(retorno);
            }

            newfile.close();
            bufferout.close();
            outputDir.close();
            
            System.exit(0);

        } catch (java.io.IOException IOE){
            System.out.print(IOE);
            System.exit(0);
        }

        System.exit(0);
    }
    
    public static String[] codes={   
        "05417700",
        "05463000",
        "05481950",
        "05420460",
        "05453000",
        "05482170",
        "05473450",
        "05473500",
        "05411950",
        "05422650",
        "05463500",
        "05482315",
        "05389400",
        "05480820",
        "05481000",
        "06609500",
        "05421682",
        "05489000",
        "05473400",
        "05464420",
        "05464780",
        "05463050",
        "05464500",
        "05457700",
        "05458500",
        "05457505",
        "05464315",
        "05464000",
        "05458300",
        "05457000",
        "05465000",
        "06904000",
        "06903400",
        "06904010",
        "06903900",
        "05454300",
        "05454220",
        "05460000",
        "05422470",
        "05422450",
        "06809000",
        "05455230",
        "05412041",
        "05482000",
        "05476590",
        "05476500",
        "05480500",
        "05476750",
        "05476000",
        "05490500",
        "05489500",
        "05485500",
        "05481500",
        "05479500",
        "05488110",
        "05487500",
        "05481650",
        "05481300",
        "05487520",
        "05488500",
        "05475350",
        "06484000",
        "05387490",
        "05422560",
        "05422600",
        "05422586",
        "05448290",
        "05449000",
        "06819185",
        "06819190",
        "05479000",
        "05478265",
        "05478000",
        "05483000",
        "06602298",
        "06809500",
        "06809210",
        "06809900",
        "06604215",
        "06897950",
        "05420300",
        "05488200",
        "05455500",
        "05461390",
        "06600100",
        "06600500",
        "06600300",
        "05485640",
        "05485605",
        "05464130",
        "05464137",
        "05494300",
        "05494500",
        "05483343",
        "06903500",
        "05464942",
        "06610500",
        "05464695",
        "05471200",
        "05451770",
        "05454500",
        "05453100",
        "05451500",
        "05465700",
        "05465500",
        "05453520",
        "05452500",
        "05455700",
        "05449500",
        "06805850",
        "05422584",
        "05483470",
        "05458000",
        "05414500",
        "06610657",
        "06604440",
        "06606600",
        "06605600",
        "06605850",
        "06605100",
        "06607510",
        "06606700",
        "06607500",
        "05480000",
        "05480080",
        "06607200",
        "05418000",
        "05416900",
        "05417500",
        "05417000",
        "05418500",
        "05422640",
        "05414400",
        "05483600",
        "05483450",
        "05486490",
        "06604400",
        "05416100",
        "06610505",
        "06602410",
        "06602400",
        "06610520",
        "05489190",
        "05454090",
        "05469860",
        "06808000",
        "05418110",
        "06810000",
        "06817000",
        "05418450",
        "05418400",
        "05482500",
        "05482430",
        "05482135",
        "05482300",
        "05486000",
        "05472500",
        "06605000",
        "06607000",
        "05455100",
        "05388500",
        "06600000",
        "06599950",
        "06599900",
        "05422590",
        "05476735",
        "05448150",
        "06818750",
        "05464640",
        "05484650",
        "05485000",
        "05484900",
        "05484500",
        "05484600",
        "05455000",
        "05454000",
        "05453600",
        "06903880",
        "05451900",
        "05412100",
        "06483270",
        "06483290",
        "06483500",
        "05452000",
        "05461000",
        "05460500",
        "05462000",
        "05459000",
        "05460400",
        "05421890",
        "05412060",
        "05474000",
        "05473000",
        "05473065",
        "06811875",
        "05411400",
        "06608500",
        "05471010",
        "05455010",
        "05489090",
        "06903700",
        "05451070",
        "05451080",
        "05451210",
        "05484000",
        "05487470",
        "05471050",
        "05471000",
        "05470000",
        "05471500",
        "06604000",
        "06604100",
        "05459490",
        "06808200",
        "05470500",
        "05471040",
        "06609200",
        "05491000",
        "06812000",
        "06811840",
        "06609590",
        "06898000",
        "0547101001",
        "05451700",
        "05412020",
        "05412000",
        "05412500",
        "05411600",
        "05411850",
        "05412056",
        "05387440",
        "05387500",
        "05387405",
        "05387320",
        "05388000",
        "05388250",
        "05412340",
        "05412400",
        "05484800",
        "05471012",
        "05471014",
        "05452200",
        "05471013",
        "05487540",
        "05487550",
        "0547101350",
        "05421000",
        "05421760",
        "05421740",
        "05422000",
        "05420560",
        "05420680",
        "05388310",
        "06806000",
        "06898400",
        "06600300",
        "05448500",
        "05458900",
        "06602020",
        "05448400",
        "06807410",
        "06807320",
        "06808500",
        "06808820",
        "06604200",
        "05471032",
        "05487980",
        "05488000",
        "06609600",
        "06609560",
        "05459500",
        "05464220",
        "05389000"};
    
    public static int[][] coord={
                    {5670,2004},
                    {4199,2457},
                    {3254,1707},
                    {6265,1826},
                    {4569,1759},
                    {2353,2556},
                    {5103,1130},
                    {5079,1137},
                    {5174,2742},
                    {5890,1551},
                    {4332,2317},
                    {2146,2227},
                    {5395,2854},
                    {3073,2585},
                    {3193,2338},
                    {1518,1668},
                    {5212,2141},
                    {3954,1309},
                    {5003,1060},
                    {4907,2029},
                    {5306,1792},
                    {4348,2428},
                    {5006,1949},
                    {4153,2872},
                    {4329,2521},
                    {4000,3059},
                    {4704,2116},
                    {4440,2391},
                    {4325,2597},
                    {3898,3360},
                    {5326,1470},
                    {4044,902},
                    {3656,1083},
                    {4068,863},
                    {3971,974},
                    {5063,1698},
                    {4943,1732},
                    {3542,2925},
                    {6034,1591},
                    {5955,1646},
                    {2345,1695},
                    {4421,1625},
                    {5110,2827},
                    {3349,1643},
                    {2430,2926},
                    {2313,3156},
                    {2857,2403},
                    {2841,2582},
                    {2193,3343},
                    {4758,893},
                    {4375,1134},
                    {3362,1614},
                    {3082,2037},
                    {2855,2405},
                    {3899,1431},
                    {3589,1537},
                    {3309,1701},
                    {3031,2185},
                    {3639,1538},
                    {3994,1363},
                    {2044,3574},
                    {933,2817},
                    {4885,3066},
                    {5836,1595},
                    {5975,1586},
                    {5929,1607},
                    {3257,2937},
                    {3343,2827},
                    {2421,836},
                    {2394,812},
                    {2865,2585},
                    {2833,2889},
                    {2878,2999},
                    {2714,2063},
                    {1359,2261},
                    {1975,1131},
                    {2114,1417},
                    {1696,864},
                    {2072,3137},
                    {3081,890},
                    {6251,1980},
                    {3838,1378},
                    {4965,1522},
                    {4112,2741},
                    {1332,2804},
                    {1069,2460},
                    {1183,2755},
                    {3414,1644},
                    {3393,1732},
                    {4205,2163},
                    {4247,2143},
                    {4368,928},
                    {4672,836},
                    {2201,2079},
                    {3764,1059},
                    {5274,1692},
                    {1474,1372},
                    {5053,1998},
                    {3614,1807},
                    {4187,1940},
                    {5114,1680},
                    {4668,1813},
                    {3954,2027},
                    {5517,1210},
                    {5418,1275},
                    {5122,1730},
                    {4488,1852},
                    {5165,1483},
                    {3349,2615},
                    {1572,1210},
                    {5919,1599},
                    {2704,1713},
                    {4297,2847},
                    {5785,2442},
                    {1486,1318},
                    {1979,2999},
                    {1507,2371},
                    {2144,2835},
                    {1975,2730},
                    {2058,2935},
                    {1351,1886},
                    {1321,2041},
                    {1352,1940},
                    {2735,2434},
                    {2810,2420},
                    {1494,2104},
                    {5744,2048},
                    {5190,2368},
                    {5278,2316},
                    {5206,2357},
                    {5883,2043},
                    {5897,1569},
                    {5691,2447},
                    {2713,1706},
                    {2611,1784},
                    {3377,1483},
                    {2058,3091},
                    {6064,2193},
                    {1449,1283},
                    {1325,1890},
                    {1348,1942},
                    {1786,1761},
                    {4181,1294},
                    {5090,1718},
                    {3334,2238},
                    {1676,1075},
                    {5467,2370},
                    {1633,786},
                    {2169,903},
                    {5846,2096},
                    {5802,2111},
                    {2708,1961},
                    {2413,2114},
                    {2142,2484},
                    {2188,2272},
                    {3321,1510},
                    {4550,1379},
                    {2002,2928},
                    {1857,2257},
                    {5049,1639},
                    {5314,2997},
                    {985,2425},
                    {1017,2497},
                    {1019,2559},
                    {5976,1605},
                    {2636,2654},
                    {5683,1521},
                    {2685,928},
                    {4906,1906},
                    {3279,1600},
                    {3347,1609},
                    {3330,1610},
                    {3070,1576},
                    {3213,1575},
                    {5136,1686},
                    {5157,1717},
                    {5210,1742},
                    {3972,977},
                    {4321,1886},
                    {5246,2761},
                    {1191,3190},
                    {1193,3177},
                    {1083,3001},
                    {4459,1941},
                    {4043,2731},
                    {3985,2790},
                    {4230,2575},
                    {3690,3170},
                    {3939,2824},
                    {5911,1898},
                    {5157,2838},
                    {5336,914},
                    {4963,1260},
                    {4965,1205},
                    {2071,906},
                    {5413,2776},
                    {1391,1828},
                    {3308,1945},
                    {5141,1675},
                    {4049,1312},
                    {3712,955},
                    {3336,2407},
                    {3376,2432},
                    {3746,2238},
                    {2900,1623},
                    {3463,1410},
                    {3666,1701},
                    {3372,1977},
                    {3350,2029},
                    {4166,1425},
                    {2094,3217},
                    {2094,3197},
                    {3697,3001},
                    {1669,1047},
                    {3341,1991},
                    {3646,1683},
                    {1388,1760},
                    {5160,653},
                    {1989,781},
                    {2084,1106},
                    {1495,1747},
                    {3191,820},
                    {3301,1948},
                    {4001,1978},
                    {5234,2686},
                    {5229,2696},
                    {5349,2599},
                    {4766,2995},
                    {4886,2864},
                    {5176,2853},
                    {4809,3164},
                    {4897,3077},
                    {4692,3194},
                    {4472,3215},
                    {4943,3077},
                    {5140,3177},
                    {4880,2687},
                    {5258,2610},
                    {3279,1621},
                    {3334,1933},
                    {3393,1919},
                    {4396,1832},
                    {3347,1932},
                    {3626,1633},
                    {3657,1579},
                    {3363,1926},
                    {4813,2364},
                    {5607,1948},
                    {5345,2042},
                    {5965,1774},
                    {4272,3023},
                    {4505,2680},
                    {5144,3201},
                    {1548,1026},
                    {3337,866},
                    {1183,2755},
                    {3278,2789},
                    {4262,2506},
                    {1266,2165},
                    {3211,2906},
                    {1866,1454},
                    {1915,1670},
                    {1689,1016},
                    {1672,858},
                    {2059,3140},
                    {3539,1782},
                    {3630,1334},
                    {3748,1398},
                    {1425,1659},
                    {1585,1904},
                    {3712,2959},
                    {4470,2184},
                    {5349,2915}};



}
