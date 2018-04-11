/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.io;
import java.io.*;
/**
 *
 * @author pmandapa
 */

import java.io.*;
import java.util.*;

public class AsciiToCRas extends Object {

    private  String[]         variables = new String[8];
    private  String[]         metaInfo = new String[12];
    private  String           fileName;
    private  float[][]        matrix;
    private  int              columns,rows;

    public AsciiToCRas(java.io.File inputFile, java.io.File outputDir) throws java.io.IOException {

        java.io.FileReader ruta;
        java.io.BufferedReader buffer;

        java.util.StringTokenizer tokens;
        String linea=null, basura, nexttoken;

        ruta = new FileReader(inputFile);
        buffer=new BufferedReader(ruta);

        columns = 40;
        rows = 40;
        matrix = new float[rows][columns];

        for (int i=0;i<rows;i++) {
            linea = buffer.readLine();
            tokens = new StringTokenizer(linea);
            for (int j=0;j<columns;j++) {
                try{
                    matrix[i][j] = new Float(tokens.nextToken()).floatValue();
                } catch (NumberFormatException NFE){
                    matrix[i][j] = -9999;
                }
            }
        }
        buffer.close();

        fileName=inputFile.getName();
        String fileBinoutputDir=outputDir.getPath()+"/"+fileName.substring(0,fileName.lastIndexOf("."))+".vhc";

        newfilebinary(new java.io.File(fileBinoutputDir));
    }

    private void newfilebinary(java.io.File BinaryFile) throws java.io.IOException{

        java.io.FileOutputStream        outputDir;
        java.io.DataOutputStream        newfile;
        java.io.BufferedOutputStream    bufferout;

        outputDir = new FileOutputStream(BinaryFile);
        bufferout=new BufferedOutputStream(outputDir);
        newfile=new DataOutputStream(bufferout);

        for (int i=rows-1;i>-1;i--) for (int j=0;j<columns;j++) {
            newfile.writeFloat(matrix[i][j]);
        }
        newfile.close();
        bufferout.close();
        outputDir.close();
    }

    public static ArrayList<File> getFileList(File dir) throws FileNotFoundException{
        ArrayList<File> result = new ArrayList<File>();
        File[] files = dir.listFiles();
        List<File> tempfiles = Arrays.asList(files);
        for(File file : files){
            result.add(file);
        }
        return result;
    }

    public static void main(String[] args) throws java.io.IOException{

       /* String resStr = "";
        for (int ii=0;ii<100;ii++)
        {
            int m = ii+1;
            if (m < 10)
            {
                resStr = "00"+m;
            }
            if (m >= 10 && m < 100)
            {
                resStr = "0"+m;
            }
            if (m == 100)
            {
                resStr = String.valueOf(m);
            }*/
            java.io.File AsciiFile;
            //File folder = new File("C:/Documents and Settings/pmandapa/My Documents/ForCuencas/RRErrors/KICT_2007_06_26t30/Data");
            //File folder = new File("C:/Documents and Settings/pmandapa/My Documents/ForCuencas/RRErrors/KICT_2006_07_26t27/PED"+resStr);
            File folder = new File("D:/CUENCAS/CedarRapids/Rainfall/sintetic_rainfall/constant/1day/asc/");

 	    try{
                ArrayList<File> files = AsciiToCRas.getFileList(folder);
	        Iterator i = files.iterator();
	        while (i.hasNext()){
                    File temp = (File) i.next();
                    System.out.println(temp.getName());
                    String FileAscIn = folder.getPath()+"/"+temp.getName().substring(0,temp.getName().lastIndexOf("."))+".vhc";
                    AsciiFile = new java.io.File(FileAscIn);
                    try {
                        //new AsciiToCRas(AsciiFile,new java.io.File("C:/Documents and Settings/pmandapa/My Documents/ForCuencas/RRErrors/BinKICT_2006_07_26t27/PED"+resStr));
                        //new AsciiToCRas(AsciiFile,new java.io.File("C:/Documents and Settings/pmandapa/My Documents/ForCuencas/RRErrors/BinKICT_2007_06_26t30/Data"));
                        System.out.print(AsciiFile);
                         System.out.print(FileAscIn);
                        new AsciiToCRas(AsciiFile,new java.io.File("D:/CUENCAS/CedarRapids/Rainfall/sintetic_rainfall/constant/1day/vhc2/"));
                    } catch (Exception IOE){
                        System.out.print(IOE);
                        System.exit(0);
                    }
               }
            } catch (IOException e){
                System.err.println("problem creating file list:");
                e.printStackTrace();
            }
        //}
    }
 }




