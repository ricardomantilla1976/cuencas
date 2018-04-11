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

import java.io.*;
import java.awt.Component;
import java.awt.Window;
import java.awt.Frame;
import java.awt.FileDialog;
import javax.swing.*;
import javax.swing.filechooser.*;
import java.awt.*;
import java.awt.event.*;
import visad.*;
import visad.java2d.DisplayImplJ2D;
import java.rmi.RemoteException;

class RainfallReader extends Thread {
    public static int size=300;
    public static float[][] rain = new float[size][size];
    
    String combinedDate=null;
    
    public void setDate(int year, int month, int day) {
        
        if ((month<13)&&(month>0)) {
            if (month<10) combinedDate=Integer.toString(year)+"0"+Integer.toString(month)+Integer.toString(day);
            else combinedDate=Integer.toString(year)+Integer.toString(month)+Integer.toString(day);
            System.out.println(combinedDate);
        } else {
            System.out.println("Illegal date");
            System.exit(0);
        }
    }
    
    public RainfallReader() {
    }
    
    public File getFile() {
        File openedFile=null;
        Frame fileHolder = new JFrame();
        //fileHolder.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JFileChooser fc=new JFileChooser("/home/ricardo/workFiles/guptaWork/iowaData/Case_1997_07_16");
        fc.setApproveButtonMnemonic('o');
        //fc.setApproveButtonText("What the fuck");
        int fileResult = fc.showOpenDialog(fileHolder);
        if (fileResult == JFileChooser.APPROVE_OPTION) openedFile = fc.getSelectedFile();
        
        //System.out.println(openedPath);
        return openedFile;
    }
    
    public void plotRain(float[][] rain, int size) {
        int xcnt,ycnt;
        RealType row, column , intensity;
        RealTupleType domain;
        Integer2DSet domainValues;
        FlatField values;
        DataReferenceImpl data_ref;
        DisplayImplJ2D display;
        ScalarMap rowMap, columnMap, intensityMap;
        float[][] samples = new float[1][size*size];
        
        
        try {
            row = RealType.getRealType("X");
            column = RealType.getRealType("Y");
            intensity = RealType.getRealType("Rainfall_Intensity");
            
            domain = new RealTupleType(row,column);
            domainValues = new Integer2DSet(domain,size,size);
            
            FunctionType rainIntensity = new FunctionType(domain, intensity);
            for (xcnt=0;xcnt<size;xcnt++) for (ycnt=0;ycnt<size;ycnt++) samples[0][xcnt*size+ycnt] = rain[xcnt][ycnt];
            
            values= new FlatField(rainIntensity,domainValues);
            values.setSamples(samples);
            
            display = new DisplayImplJ2D("Rainfall");
            GraphicsModeControl dispGMC = (GraphicsModeControl) display.getGraphicsModeControl();
            dispGMC.setScaleEnable(true);
            
            rowMap= new ScalarMap(row, Display.YAxis );
            columnMap = new ScalarMap( column, Display.XAxis );
            intensityMap = new ScalarMap( intensity,  Display.RGB );
            
            display.addMap( columnMap );
            display.addMap( rowMap );
            display.addMap( intensityMap );
            
            data_ref = new DataReferenceImpl("data_ref");
            data_ref.setData( values );
            
            display.addReference( data_ref );
            
            JFrame jframe = new JFrame("Rainfall Intensity");
            jframe.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            jframe.getContentPane().add(display.getComponent());
            
            jframe.setSize(900, 900);
            jframe.setVisible(true);
            
        } catch (VisADException ve) {
            System.out.println("VisADEx: "+ve);
        } catch (RemoteException re) {
            System.out.println("RemoteEx: "+re);
        }
    }
    
    public float[][] readData(int size, File openedFile) {
        
        int xcnt,ycnt;
        float[][] result = new float[size][size];
        short[][] temp = new short[size][size];
        try {
            //System.out.println(size);
            DataInputStream instr = new DataInputStream(new BufferedInputStream(new FileInputStream( openedFile) ) );
            
            for (xcnt=0;xcnt<size;xcnt++) for (ycnt=0;ycnt<size;ycnt++) {
                int low = instr.readByte() & 0xff;
                int high = instr.readByte() & 0xff;
                result[xcnt][ycnt] = (float)(((short)(high << 8 | low))/10.0f);
                if (result[xcnt][ycnt] < 0) result[xcnt][ycnt]=-99;
                //result[xcnt][ycnt]=(float)(instr.readShort());
                //temp[xcnt][ycnt]=instr.readShort();
                //if (temp[xcnt][ycnt]<0) System.out.println(temp[xcnt][ycnt]);
            }
            // for (xcnt=0;xcnt<size;xcnt++) {
            //    for (ycnt=0;ycnt<size;ycnt++) {
            //         System.out.print(rain[xcnt][ycnt]+" ");
            //    }
            //    System.out.println();
            // }
            instr.close();
        } catch ( IOException iox ) {
            System.out.println("Problem reading " + openedFile.getPath() );
        }
        
        return result;
    }
    
    public void writeVHC(float[][] data, int year, int month, int day, int hour, int minute) {
    }
    
    public void convertR1BToVHC(File openedFile, String targetDirectory, int size) {
        float[][] data=new float[size][size];
        String[] months={"January","February","March","April","May","June","July","August","September","October","November","December"};
        
        String r1bFileName=openedFile.getPath().substring(openedFile.getPath().lastIndexOf(File.separator)+1);
        data=readData(size, openedFile);
        System.out.print("Converting "+r1bFileName);
        String[] timeStamp = new String[5];
        timeStamp[0]=r1bFileName.substring(4,8);
        timeStamp[1]=r1bFileName.substring(8,10);
        timeStamp[2]=r1bFileName.substring(10,12);
        timeStamp[3]=r1bFileName.substring(13,15);
        timeStamp[4]=r1bFileName.substring(15,17);
        String monthString=months[(Integer.parseInt(timeStamp[1])-1)];
        
        String vhcFilename="prec."+timeStamp[3]+timeStamp[4]+"00."+timeStamp[2]+"."+monthString+"."+timeStamp[0]+".vhc";
        System.out.println(" to "+vhcFilename);
        
        
        
        try {
            DataOutputStream writer = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(targetDirectory+File.separator+vhcFilename)));
            
            
            for (int xc=0;xc<size;xc++) {
                for (int yc=0;yc<size;yc++) {
                    if (data[xc][yc]!=-9.9) writer.writeFloat(data[xc][yc]);
                    else writer.writeFloat(-99);
                }
            }
            writer.close();
        } catch (IOException bs) {
            System.out.println("Failure to write vhc file: "+bs);
        }
        //need to convert to a new filename consistent with hidrosig's methodology
        
        //then save it - perhaps the function should return the file?
    }
    
    public void createMetaFile(File directory) {
        try{
            File saveFile=new File(directory.getPath()+File.separator+"prec.metaVHC");
            PrintWriter writer = new PrintWriter(new FileWriter(saveFile));
            writer.println("[Name]");
            writer.println("Precipitation Radar Data From KICT");
            writer.println("[Southernmost Latitude]");
            writer.println("35:57:02.52 N");
            writer.println("[Westernmost Longitude]");
            writer.println("98:47:30.84 W");
            writer.println("[Longitudinal Resolution (ArcSec)]");
            writer.println("32.376");
            writer.println("[Latitudinal Resolution (ArcSec)]");
            writer.println("40.896");
            writer.println("[# Columns]");
            writer.println("300");
            writer.println("[# Rows]");
            writer.println("300");
            writer.println("[Format]");
            writer.println("Float");
            writer.println("[Missing]");
            writer.println("-99");
            writer.println("[Temporal Resolution]");
            writer.println("6-minutes");
            writer.println("[Units]");
            writer.println("mm/h");
            writer.println("[Information]");
            writer.println("Precipitation data from the KICT radar near Wichita, KS");
            writer.close();
        } catch (IOException bs) {
            System.out.println("Error composing metafile: "+bs);
        }
        
    }
    
    
    public int batchConvertR1BToVHC(File directory, String targetDirectory, int size) {
        //should convert all r1b files having the same day signature to hidrosig format and create metafile - need a filter
        int numConverted=0;
        
        java.io.FileFilter rbdf = new java.io.FileFilter(){
            public boolean accept(File checkFile) {
                if (checkFile != null) {
                    if (checkFile.isDirectory()) return false;
                    String fileName=checkFile.getPath();
                    String testDate;
                    if ((fileName.compareTo("KICT")>0)) {     //has a KICT in it
                        int firstindex = fileName.indexOf("KICT");
                        testDate=fileName.substring(firstindex+4,firstindex+12);
                        if ((testDate.equals(combinedDate))&&(fileName.endsWith("r1b"))) return true;
                    }
                }
                return false;
            }
        };
        createMetaFile(directory);
        File[] filesToConvert = directory.listFiles(rbdf);
        
        for (int cnt=0;cnt<filesToConvert.length;cnt++) convertR1BToVHC(filesToConvert[cnt], targetDirectory, size);
        
        
        //using the above convert method of course ...
        return numConverted;
    }
    
    public static void main( String[] args ) {
        String fileName = "intData.dat" ;
        File openedFile;
        int numConverted;
        RainfallReader rr= new RainfallReader();
        openedFile=rr.getFile();
        System.out.println(openedFile.getParent());
        if (openedFile.isFile()) {
            rain=rr.readData(size, openedFile);
            rr.plotRain(rain,size);
            rr.createMetaFile(openedFile.getParentFile());
            rr.convertR1BToVHC(openedFile, openedFile.getParent(), 300);
        } else {
            rr.setDate(1999,4,25);
            numConverted=rr.batchConvertR1BToVHC(openedFile, openedFile.getPath(), 300);
            
        }
        
        
    }
    
}





