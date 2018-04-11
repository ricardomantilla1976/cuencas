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


package hydroScalingAPI.examples.dataAnalysis;

import java.io.*;
import java.util.Random;
import java.util.*;
import visad.*;
import visad.util.*;
import visad.java3d.DisplayImplJ3D;
import visad.java2d.DisplayImplJ2D;
import java.rmi.RemoteException;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class MomentAnalysis extends Thread implements ActionListener {
    
    public double[][] data;
    public int SIZE;
    public int EFFSIZE;
    public double maxOrder, orderInc;
    public String path="G:/junk/";
    public JFrame displayContainer = new JFrame();
    public JFrame GUIContainer = new JFrame();
    
    public JButton startButton = new JButton("Go");
    public JButton stopButton = new JButton("Stop");  
    public JButton loadButton = new JButton("Load");   
    public JCheckBox showAll = new JCheckBox("Show buffers");
    public JPanel displayPanel = new JPanel();     
    public JPanel momentPlotPanel = new JPanel();
    public JTextField sizeTextField= new JTextField(5);
    public JTextField effSizeTextField= new JTextField(5);
    public JTextField mOTextField= new JTextField(5);
    public JTextField oITextField= new JTextField(5);    
    
    public JPanel controlPanel = new JPanel(); 
    public JLabel bs;
    
    private RealType xAxis,yAxis,mData;
    private RealTupleType domain, range;
    private DisplayImplJ3D display;
    private ScalarMap zMap,xMap,yMap,rgbMap,alphaMap;
    private FunctionType funcDomainRange;
    private visad.Set domainSet;
    private FlatField valuesFF;
    private DataReferenceImpl dataRef;
    private int columns, rows;
    private ColorControl colCont;
    
    public double[][] moment;
    public double[] lambda;
  
    public MomentAnalysis () {
        createGUI();
    }
    
    public MomentAnalysis(int totalSize, int effSize, double mO, double oI, String pathName) {
        SIZE=totalSize;
        EFFSIZE=effSize;
        maxOrder = mO;
        orderInc = oI;
        path=pathName;
    }
    
    public void createGUI() {
        
        startButton.addActionListener(this);
        loadButton.addActionListener(this); 
             
        GridBagConstraints gbc = new GridBagConstraints();
        
        GUIContainer.setSize(500,300);
        GUIContainer.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel inputPanel = new JPanel();
                      
        JPanel buttonHolder = new JPanel();
        JPanel inputlPanel = new JPanel(); 
        
        JLabel sizeLabel= new JLabel("Full Lattice Size");
        JLabel effSizeLabel= new JLabel("Effective Lattice Size (centered)");
        JLabel mOLabel= new JLabel("Maximum order to calculate");
        JLabel oILabel= new JLabel("Increment of orders to be calculated");
        
        sizeTextField.setText("140");
        effSizeTextField.setText("120");
        mOTextField.setText("5");
        oITextField.setText(".5");
        
        
        GUIContainer.getContentPane().setLayout(new GridBagLayout()); 
        inputPanel.setLayout(new GridBagLayout());
        buttonHolder.setLayout(new GridBagLayout());
 
        gbc.insets=new Insets(3,3,3,3);
        //gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx=0;
        gbc.gridy=0;
        buttonHolder.add(startButton,gbc);
        gbc.gridx=1;
        gbc.gridy=0;
        buttonHolder.add(loadButton,gbc);
        
       
        //add size label and textfields to appropriate panels (at top)  -> (0,0) on panel  
        gbc.gridx=0;
        gbc.gridy=0;           
        inputPanel.add(sizeLabel,gbc);
        //add size label and textfields to appropriate panels (at top)  -> (1,0) on panel          
        gbc.gridx=1;
        gbc.gridy=0;           
        inputPanel.add(sizeTextField,gbc);      
        //add effective size label and textfields to appropriate panels (below size)  -> (0,1) on panel       
        gbc.gridx=0;
        gbc.gridy=1;
        inputPanel.add(effSizeLabel,gbc);
        //add effective size label and textfields to appropriate panels (below size)  -> (1,1) on panel
        gbc.gridx=1;
        gbc.gridy=1;
        inputPanel.add(effSizeTextField,gbc);
        //add effective size label and textfields to appropriate panels (below size)  -> (0,1) on panel       
        gbc.gridx=0;
        gbc.gridy=2;
        inputPanel.add(mOLabel,gbc);
        //add effective size label and textfields to appropriate panels (below size)  -> (1,1) on panel
        gbc.gridx=1;
        gbc.gridy=2;
        inputPanel.add(mOTextField,gbc);
                //add effective size label and textfields to appropriate panels (below size)  -> (0,1) on panel       
        gbc.gridx=0;
        gbc.gridy=3;
        inputPanel.add(oILabel,gbc);
        //add effective size label and textfields to appropriate panels (below size)  -> (1,1) on panel
        gbc.gridx=1;
        gbc.gridy=3;
        inputPanel.add(oITextField,gbc);

       

        //gbc.fill = GridBagConstraints.BOTH;
        gbc.gridx=0;
        gbc.gridy=0;
        
        //add text field panel to frame at right -> (1,0) on frame
        //gbc.gridx=1;
        GUIContainer.getContentPane().add(inputPanel,gbc); 
       
        gbc.gridx=0;
        gbc.gridy=1;
        GUIContainer.getContentPane().add(buttonHolder,gbc);               
             
        GUIContainer.setVisible(true);
    }
    
  
    public void run() {
        data=new double[SIZE][SIZE];
        File testFile = new File (path);
        if (testFile.isDirectory()) {
        }
        else if (testFile.isFile()) {
            //System.out.println("Data file "+path);
            try {
              readData(path);
              analyzeMoments(maxOrder, orderInc);
              displayResults(false);
            } catch (IOException e) {
                System.out.println(e);
            } catch (NumberFormatException e) {
                System.out.println(e);
            } catch (Throwable e) {             
                System.out.println(e);
            }    
        }
        else {
            System.out.println("Invalid path. Exiting.");
            System.exit(0);
        }
    }
    
    public void updateDisplay(boolean showBuffers) {
        //displayContainer.setVisible(false);
        displayPanel.removeAll();
        if (showBuffers) createDisplay(SIZE, SIZE,data,displayPanel);
        else createDisplay(SIZE, EFFSIZE,data,displayPanel);
        

        displayPanel.invalidate();
        displayPanel.validate();
        displayPanel.repaint();

    }
    
    public void displayResults(boolean showBuffers) {
        displayContainer.setSize(800, 500);
        
        if (showBuffers) createDisplay(SIZE, SIZE,data,displayPanel);
        else createDisplay(SIZE, EFFSIZE,data,displayPanel);
        
        GridBagConstraints gbc = new GridBagConstraints();
        displayContainer.getContentPane().setLayout(new GridBagLayout());
        controlPanel.setLayout(new GridBagLayout());
        
        bs = new JLabel("Something else will be here soon.");
        showAll = new JCheckBox("Show buffers",showBuffers);
        showAll.addActionListener(this);
        gbc.gridx=0;
        gbc.gridy=0;
        controlPanel.add(bs,gbc);
        
        gbc.gridx=0;
        gbc.gridy=1;
        controlPanel.add(showAll,gbc);
        
        gbc.gridx=0;
        gbc.gridy=0;
        displayContainer.getContentPane().add(displayPanel,gbc);
        gbc.gridx=1;
        gbc.gridy=0;
        displayContainer.getContentPane().add(controlPanel,gbc);
        gbc.gridx=2;
        gbc.gridy=0;
        displayContainer.getContentPane().add(momentPlotPanel,gbc);
        
        displayContainer.setVisible(true);
        
    }
    
    public void createDisplay (int size, int effsize, double[][] rain, JPanel panel) {
	String titleStr="Rainfall Output";
        //parent=rm;
        int xcnt,ycnt;

        int buffer = (size-effsize)/2;

	try{
            double[][] flatData = new double[1][effsize*effsize];

	    //declare data types System.out.println("Here.");
	    xAxis = RealType.getRealType("X_Distance",SI.meter,null);
	    yAxis = RealType.getRealType("Y_Distance",SI.meter,null);
	    mData = RealType.getRealType("Rainfall", SI.meter, null);
	    
	    //set up the two dimensional domain
	    domain = new RealTupleType(xAxis,yAxis);
	    
	    //establish the function type (x,y)->rainfall(x,y)
	    funcDomainRange = new FunctionType(domain, mData);
	    
            	    //establish boundaries in a set
	    columns=effsize;
	    rows=effsize;
	    domainSet = new Linear2DSet(domain,0,effsize,rows,0,effsize,columns);
        
            //fill in flatdata
            for (xcnt=buffer;xcnt<size-buffer;xcnt++) for (ycnt=buffer;ycnt<size-buffer;ycnt++) flatData[0][(xcnt-buffer)*effsize+ycnt-buffer] = rain[xcnt-buffer][ycnt-buffer];

            //put flatdat in as samples
            valuesFF = new FlatField(funcDomainRange, domainSet);
            valuesFF.setSamples(flatData);
            
	    //create display       
	    display = new DisplayImplJ3D(titleStr);//"RainfallModel");
	    
	    //Get display graphics mode control, draw scales
	    GraphicsModeControl dispGMC = (GraphicsModeControl) display.getGraphicsModeControl();
	    dispGMC.setTextureEnable(false);
	    dispGMC.setScaleEnable(true);
	    
	    //Create ScalarMaps
	    xMap = new ScalarMap(xAxis,Display.XAxis);
	    yMap = new ScalarMap(yAxis,Display.YAxis);
	    zMap = new ScalarMap(mData, Display.ZAxis);
	    //rgbMap = new ScalarMap(mData,Display.RGB );
            
            //Removed alpha map (transparencies) - R
	    
	    //add maps to display
	    display.addMap(xMap);
	    display.addMap(yMap);
	    display.addMap(zMap);
	    	    
	    //Create data reference
	    dataRef = new DataReferenceImpl("dataRef");
            dataRef.setData(valuesFF);
	    display.addReference(dataRef);	    

	    //set maps ranges
	    xMap.setRange(0,effsize);
	    yMap.setRange(0,effsize);
	    zMap.setRange(0,1500);
	    
	    //create app window, add display
            
	    panel.add(display.getComponent());
	    
	}
	catch (VisADException e) {
	    System.out.println("VisAD Exception: "+e.getMessage());
	}
	catch (RemoteException e) {
	    System.out.println("Remote Exception: "+e.getMessage());
	}

    }
    
    
    public void analyzeMoments(double maxOrder, double orderIncrement) {
        
        int xc,yc,sCnt,oCnt,xcp,ycp;
        int BUFFER = (SIZE-EFFSIZE)/2;
        
        int[] scales;
        double[][] modifiedData;

              
        int nOrders = (int)(maxOrder/orderIncrement);
        double[] orders = new double[nOrders];
        int nScales=0;
        double momentOrder;
        int nPixelsToSum,nPixelsOnSide;
        
        //compute factors for scaling purposes
        for (sCnt=1;sCnt<=EFFSIZE;sCnt++) {
            if ((EFFSIZE % sCnt) == 0) nScales++;
            //System.out.println(sCnt+" "+(EFFSIZE % sCnt));
        }
        //System.out.println("Here "+nScales); 
        scales = new int[nScales];
        lambda = new double[nScales];
        nScales=0;
        for (sCnt=1;sCnt<=EFFSIZE;sCnt++) {   
            
            if ((EFFSIZE % sCnt) == 0) {
                //System.out.println("Here "+EFFSIZE+" "+nScales+" "+sCnt); 
                scales[nScales]=sCnt;
                lambda[nScales]=1/sCnt;
                nScales++;
            }            
        }
        //System.out.println("Here "); 
        moment = new double[nScales][nOrders];
        
        //orders need to be computed
        for (oCnt=0;oCnt<nOrders;oCnt++) orders[oCnt]=oCnt*orderIncrement;
        
        //have list of scales and lambda factors - now need to compute moments
        
        for (sCnt=0;sCnt<nScales;sCnt++) {              //at each scale there's an averaging procedure over scales[sCnt] pixels
            
            if (sCnt==0) {
                
                for (xcp=0;xcp<EFFSIZE;xcp++)  for (ycp=0;ycp<EFFSIZE;ycp++) if (data[xcp][ycp]>0) moment[sCnt][0]+=1; 
                for (oCnt=1;oCnt<nOrders;oCnt++) {
                    momentOrder=orders[oCnt];
                    for (xcp=0;xcp<EFFSIZE;xcp++)  for (ycp=0;ycp<EFFSIZE;ycp++) moment[sCnt][oCnt]+=Math.pow(data[xcp][ycp],momentOrder);
                }
            }
            else {
                nPixelsToSum=scales[sCnt];
                nPixelsOnSide=EFFSIZE/nPixelsToSum;
        
                modifiedData=new double[nPixelsOnSide][nPixelsOnSide];
                //System.out.println("Here "+nPixelsToSum+" "+nPixelsOnSide); 
                
                for (oCnt=0;oCnt<nOrders;oCnt++) {
                    momentOrder=orders[oCnt];
        
                    for (xc=BUFFER;xc<SIZE-BUFFER;xc++) {
                         for (yc=BUFFER;yc<SIZE-BUFFER;yc++) {
                             xcp=(xc-BUFFER)/nPixelsToSum;
                             ycp=(yc-BUFFER)/nPixelsToSum; 
                             //System.out.println("Here "+(SIZE-BUFFER)+" "+xc+" "+yc+" "+nPixelsToSum+" "+xcp+" "+ycp);                         
                             modifiedData[xcp][ycp]+=data[xc][yc];                                    
                         }
                    }
                
                    if (oCnt==0) for (xcp=0;xcp<nPixelsOnSide;xcp++)  for (ycp=0;ycp<nPixelsOnSide;ycp++) if (modifiedData[xcp][ycp]>0) moment[sCnt][0]+=1;
                    else for (xcp=0;xcp<nPixelsOnSide;xcp++)  for (ycp=0;ycp<nPixelsOnSide;ycp++) moment[sCnt][oCnt]+=Math.pow(modifiedData[xcp][ycp],momentOrder);
                }
            }
        }
        
        addPlotDisplay(nScales,nOrders);
    }
    
    public void addPlotDisplay(int nScales, int nOrders) {
        try {
            
         RealType scaleVar=RealType.getRealType("scale");
         
         RealType momVar=RealType.getRealType("Moment");
         //System.out.println("\n\n\n\n\n\n\n\n\n\n GOT HERE!!!!!!!!! xvar and yvar \n\n\n\n\n\n\n\n\n\n");

         FunctionType plottedFunction = new FunctionType(scaleVar,momVar);
         //System.out.println("\n\n\n\n\n\n\n\n\n\n GOT HERE!!!!!!!!! plottedfunctino \n\n\n\n\n\n\n\n\n\n");

         Linear1DSet timeSet = new Linear1DSet(scaleVar,0,16,16);
 
         //System.out.println("\n\n\n\n\n\n\n\n\n\n GOT HERE!!!!!!!!! \n\n\n\n\n\n\n\n\n\n");
         //for (int cnt=0;cnt<5; cnt++) timeSet[0][cnt]=maxTime-5+cnt;
         
         //double[][] yDataSet = new double[1][maxTime];
         //System.out.println("\n\n\n\n\n\n\n\n\n\n GOT HERE!!!!!!!!! \n\n\n\n\n\n\n\n\n\n");
         //for (int cnt=0; cnt<maxTime; cnt++) yDataSet[0][cnt]=quantity[cnt];
       
         

         DisplayImplJ2D plotDisplay = new DisplayImplJ2D("Moments");
         GraphicsModeControl dispGMC = (GraphicsModeControl) plotDisplay.getGraphicsModeControl(); 
         dispGMC.setScaleEnable(true);
             
         ScalarMap timeMap = new ScalarMap(scaleVar,  Display.XAxis);
         ScalarMap quantityMap = new ScalarMap(momVar, Display.YAxis);
         
         timeMap.setRange(0,16);
         quantityMap.setRange(0,2e4);
         

         
         plotDisplay.addMap(timeMap);
         plotDisplay.addMap(quantityMap);
         //DataReferenceImpl[] plotRef = new DataReferenceImpl[nOrders];
         //DataReferenceImpl plotRef;// = new DataReferenceImpl[nOrders];
         String dataRefName;
         for (int oCnt=0;oCnt<nOrders;oCnt++) {
           double[][] temp = new double[1][nScales];
           for (int sCnt=0; sCnt<16;sCnt++) {
               temp[0][sCnt]=moment[sCnt][oCnt];
               //System.out.println(temp[0][sCnt]);
           }

           FlatField dataToPlot = new FlatField(plottedFunction, timeSet);   //needs to be replaced with labda ... somehow
           dataToPlot.setSamples(temp);

           dataRefName=new String("dataRef"+Integer.toString(oCnt));
          // plotRef[oCnt] = new DataReferenceImpl(dataRefName);
           //plotRef[oCnt].setData(dataToPlot);
           //plotDisplay.addReference(plotRef[oCnt]);
           DataReferenceImpl plotRef= new DataReferenceImpl(dataRefName);
           plotRef.setData(dataToPlot);
           plotDisplay.addReference(plotRef); 
         }
          //System.out.println("DATA ADDED");
          
         
         momentPlotPanel.add(plotDisplay.getComponent());
        // momentPlotPanel.invalidate();
        // momentPlotPanel.validate();
         
          //System.out.println("ok");
         //momentPlotPanel.repaint();
        
          //System.out.println("ok");
        } catch (VisADException e) {
            System.out.println("VisADException: "+e);
        } catch (RemoteException e) {
            System.out.println("RemoteException: "+e);
        }
         
    }
  
    public int[] getSpecs() {      
        int[] results = new int[2];     
        return results;
    }
  
    public void readData(String fileName) throws IOException, NumberFormatException, Throwable {
        boolean foundSize=false;
        boolean foundFormat=false;
        BufferedReader metaFile;
        if (fileName.endsWith("data")) {   
            
            //String file, read it in
            String line;
            try {
              //read data in from files
               String temp;
               BufferedReader dataFile = new BufferedReader(new FileReader(fileName));
               System.out.println("Data file "+fileName);
               for (int xcnt=0;xcnt<SIZE;xcnt++){
                   line = dataFile.readLine();
                   StringTokenizer reader = new StringTokenizer(line," ");

                   for (int ycnt=0;ycnt<SIZE;ycnt++) {
                       temp=reader.nextToken();                     
                       data[xcnt][ycnt] = Double.valueOf(temp).doubleValue();                                  
                   } 
                  
                   //line = dataFile.readLine();
               }
            dataFile.close();
          } catch (IOException e) {
              System.out.println(e);
          }
        }
        else if (fileName.endsWith("vhc")) {
            //this is a binary file compatible with hidrosig
            //must read the metafile first to see the stats
            String line;            
            java.io.FileFilter metaFilter = new java.io.FileFilter(){
                public boolean accept(File checkFile) {
                    if (checkFile != null) {
                        if (checkFile.isDirectory()) return false;            
                        String test=checkFile.getPath();
                        if (test.endsWith("metavhc")) return true;    
                    } 
                    return false;
                } 
            };
            File fileToOpen = new File(path);
            File[] metaList;
            if (fileToOpen.isDirectory()) metaList=fileToOpen.listFiles(metaFilter);
            else metaList = fileToOpen.getParentFile().listFiles(metaFilter);
            String metaFileName = metaList[0].getPath();
            
                metaFile = new BufferedReader(new FileReader(metaFileName));
                
                String testString=metaFile.readLine();
                String compString=null;
                int startOfPhrase,endOfPhrase;
                while ((!foundSize)&& (testString!=null)) {  
                    startOfPhrase=testString.indexOf("[");
                    endOfPhrase=testString.lastIndexOf("]");
                    if ((startOfPhrase>-1)&&(endOfPhrase>-1)) {
                        compString=testString.substring(startOfPhrase,endOfPhrase+1);               
                        foundSize=(compString.equals("[# Columns]"));
                    }                    
                    testString=metaFile.readLine();
                }
                if (foundSize) {
                    //System.out.println(testString);
                    SIZE=Integer.valueOf(testString).intValue();                   
                    metaFile.close();
                } else {  
                    metaFile.close();
                    throw new Throwable("No metafile present or size unknown.");
                }   
                
                // also have to read format
                
                while ((!foundFormat)&& (testString!=null)) {  
                    startOfPhrase=testString.indexOf("[");
                    endOfPhrase=testString.lastIndexOf("]");
                    if ((startOfPhrase>-1)&&(endOfPhrase>-1)) {
                        compString=testString.substring(startOfPhrase,endOfPhrase+1);               
                        foundSize=(compString.equals("[Format]"));
                    }                    
                    testString=metaFile.readLine();
                }
                if (foundFormat) {
                    metaFile.close();
                } else {  
                    metaFile.close();
                    throw new Throwable("No metafile present or format unknown.");
                }  
                //now read in the data from the file
                               
                DataInputStream instr = new DataInputStream(new BufferedInputStream(new FileInputStream(fileName)));

                for (int xcnt=0;xcnt<SIZE;xcnt++) for (int ycnt=0;ycnt<SIZE;ycnt++) {          
                  
                    if (testString.equals("Double")) data[xcnt][ycnt]= instr.readByte() ; 
                    else data[xcnt][ycnt]= (double) instr.readByte() ; 
                }                           
                
                  
            
        }
        else System.out.println("Error opening "+fileName+": don't recognize files of type "+fileName.substring(fileName.lastIndexOf(".")));
                 
      
    }
    
    public static void main (String[] args) {
        MomentAnalysis ma = new MomentAnalysis();
        //ma.start();
    }
    
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == startButton) { 
            SIZE= Integer.valueOf(sizeTextField.getText()).intValue();
            EFFSIZE= Integer.valueOf(effSizeTextField.getText()).intValue();  
            maxOrder = Double.valueOf(mOTextField.getText()).doubleValue();  
            orderInc = Double.valueOf(oITextField.getText()).doubleValue();  
            MomentAnalysis ma = new MomentAnalysis(SIZE,EFFSIZE,maxOrder,orderInc,path);
            ma.start();
        }
        else if (e.getSource() == loadButton) { 
            Frame fileHolder = new Frame();
            JFileChooser fc=new JFileChooser("G:/junk/");
            fc.setApproveButtonMnemonic('o');
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            int fileResult = fc.showOpenDialog(fileHolder);
            if (fileResult == JFileChooser.APPROVE_OPTION) {
               File openDir = fc.getSelectedFile();
               path = openDir.getPath();
               //System.out.println(path);
            } 
        }
        else if (e.getSource() == showAll) {
            System.out.println("Button checked.");
            if (showAll.isSelected()) updateDisplay(true);
            else updateDisplay(false);
        }
    }
    
}
