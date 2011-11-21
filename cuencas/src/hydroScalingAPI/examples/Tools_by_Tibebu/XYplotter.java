/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package hydroScalingAPI.examples.Tools_by_Tibebu;
import java.awt.Color;
import java.util.ArrayList;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

/**
 *
 * @author Tibebu
 */
public class XYplotter extends ApplicationFrame {


   public XYplotter(final String title, ArrayList<Double> storageTime, ArrayList<Double> storageValue, ArrayList<Double> dischargeTime, ArrayList<Double> dischargeValue) {

        super(title);

        final XYDataset dataset = createDataset(storageTime,storageValue,dischargeTime,dischargeValue);
        final JFreeChart chart = createChart(dataset);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        setContentPane(chartPanel);
   }
    
    /**
     * Creates the dataset.
     * @return a storage dataset.
     */
    private XYDataset createDataset(ArrayList<Double> storageTime, ArrayList<Double> storageValue, ArrayList<Double> dischargeTime, ArrayList<Double> dischargeValue) {
        
        final XYSeries series1 = new XYSeries("With Storage");
        for(int i =0; i<storageTime.size();i++){series1.add(storageTime.get(i), storageValue.get(i));}
        final XYSeries series2 = new XYSeries("No Storage");
        for(int i =0; i<dischargeTime.size();i++){series2.add(dischargeTime.get(i), dischargeValue.get(i));}
        final XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(series1);
        dataset.addSeries(series2);
            
        return dataset;
        
    }
    
    /**
     * Creates a chart.
     * @param dataset  the data for the chart.
     * @return a chart.
     */
    private JFreeChart createChart(final XYDataset dataset) {
        
        // create the chart...
        final JFreeChart chart = ChartFactory.createXYLineChart(
            "",      // chart title
            "Time",                      // x axis label
            "Discharge (cumec)",        // y axis label
            dataset,                  // data
            PlotOrientation.VERTICAL,
            true,                     // include legend
            true,                     // tooltips
            false                     // urls
        );

        // NOW DO SOME OPTIONAL CUSTOMISATION OF THE CHART...
        chart.setBackgroundPaint(Color.white);
        
        // get a reference to the plot for further customisation...
        final XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.lightGray);
    //    plot.setAxisOffset(new Spacer(Spacer.ABSOLUTE, 5.0, 5.0, 5.0, 5.0));
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        
        final XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
//        renderer.setSeriesLinesVisible(0, false);
        renderer.setSeriesShapesVisible(0, false);
        renderer.setSeriesShapesVisible(1, false);
        plot.setRenderer(renderer);

        // change the auto tick unit selection to integer units only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // OPTIONAL CUSTOMISATION COMPLETED.
                
        return chart;
        
    }
}
    

