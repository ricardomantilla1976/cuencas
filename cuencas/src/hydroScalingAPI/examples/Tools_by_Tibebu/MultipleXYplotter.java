/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.Tools_by_Tibebu;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

/**
 *
 * @author Tibebu
 */
public class MultipleXYplotter extends ApplicationFrame {


   public MultipleXYplotter(final String title, double[][] data, String [] dataLabel) {

        super(title);

        final XYDataset dataset = createDataset(data, dataLabel);
        final JFreeChart chart = createChart(dataset, title);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(650, 400));
        setContentPane(chartPanel);
   }
    
    /**
     * Creates the dataset.
     * @return a storage dataset.
     */
    private XYDataset createDataset(double[][] data, String [] dataLabel) {
        final XYSeriesCollection dataset = new XYSeriesCollection();
        System.out.println("Array Length= "+data[0].length/2);
//        double time =0; double peakQ =0;
        for (int j=0; j<data[0].length/2;j++)
        {            
            final XYSeries series1 = new XYSeries(dataLabel[j]);
            for(int i =0; i<data.length;i++)
            {               
                    series1.add(data[i][2*j], data[i][2*j+1]);
//                    if (peakQ<data[i][2*j+1]&&i<data.length-1){peakQ=data[i][2*j+1];time=data[i][2*j];}
//                    else if(i==data.length-1){System.out.println(time+" "+peakQ); time=0; peakQ=0;}
                    
            }       
        
            dataset.addSeries(series1);        
        }   
        return dataset;
        
    }
    
    /**
     * Creates a chart.
     * @param dataset  the data for the chart.
     * @return a chart.
     */
    private JFreeChart createChart(final XYDataset dataset, String title) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            title,      // chart title
            "Time [hr]",                      // x axis label
            "Discharge [m^3/s]",        // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );
        
        LegendTitle legend = chart.getLegend();
        legend.setItemFont(new Font("Helvetica", Font.BOLD,24));

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);
        
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
         
                 
        
        //Controls the color of each trend
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
////        renderer.setSeriesLinesVisible(0, false);
//        renderer.setSeriesShapesVisible(0, false);
       renderer.setStroke(new BasicStroke(1.f));
//        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
//        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setTickLabelFont(new Font("Helvetica", Font.BOLD,24));
        rangeAxis.setLabelFont(new Font("Helvetica", Font.BOLD,24));
        // 
          // change the auto tick unit selection to integer units only...
        final NumberAxis xAxis = (NumberAxis) plot.getDomainAxis();        
        xAxis.setTickLabelFont(new Font("Helvetica", Font.BOLD,24));
        xAxis.setLabelFont(new Font("Helvetica", Font.BOLD,24));      
        return chart;
        
    }
}
    

