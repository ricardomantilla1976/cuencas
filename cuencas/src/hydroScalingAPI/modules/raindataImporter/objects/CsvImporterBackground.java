package hydroScalingAPI.modules.rainDataImporter.objects;

import hydroScalingAPI.io.MetaRaster;
import hydroScalingAPI.io.swmmCoupling.PrecipitationWriter;
import hydroScalingAPI.io.swmmCoupling.ProgressListener;
import java.util.List;
import javax.swing.JProgressBar;

/**
 * This class makes the connection between the library used to write proper precipitation files and its respective interface concerning ProgressBar response
 * @author A. D. L. Zanchetta
 */
public class CsvImporterBackground extends javax.swing.SwingWorker implements ProgressListener {
    private final PrecipitationWriter usedWriter; // 
    private final JProgressBar progressBar;       //
    private MetaRaster exampleMetaRaster;         //
    private String[][] dataSequence;              // A matrix of size [n][2], where 'n' is the number of elements and [n][0] is the date and [n][1] is the respective rain data
    
    public CsvImporterBackground(PrecipitationWriter usedWriter_arg,
                                    JProgressBar progressBar_arg){
        this.usedWriter = usedWriter_arg;
        this.progressBar = progressBar_arg;
    }

    public void setDataSequence(String[][] dataSequence) {
        this.dataSequence = dataSequence;
    }

    public void setExampleMetaRaster(MetaRaster exampleMetaRaster) {
        this.exampleMetaRaster = exampleMetaRaster;
    }
    
    @Override
    protected Object doInBackground() throws Exception {
        this.usedWriter.setOwnListener(this);
        this.usedWriter.writeTimeSequence(this.dataSequence, 
                                          this.exampleMetaRaster);
        return (null);
    }

    @Override
    protected void process(List args) {
        //Integer arg;
        for (Object arg : args){
            this.progressBar.setValue((Integer)arg);
        }
    }

    @Override
    protected void done() {
        super.done();
        this.progressBar.setString("Process Done.");
    }
    
    public void onCreate(int begin_arg, int end_arg) {
        // basic check
        if(this.progressBar == null) return;
        
        this.progressBar.setMinimum(begin_arg);
        this.progressBar.setMaximum(end_arg);
        this.progressBar.setStringPainted(false);
    }

    public void onChangeTo(int newValue_arg) {
        // basic check
        if(this.progressBar == null) return;
        
        this.progressBar.setValue(newValue_arg);
        this.publish(newValue_arg);
    }

    public void onFinish(boolean success_arg) {
        // basic check
        if(this.progressBar == null) return;
        if(this.dataSequence == null) return;
        
        if (success_arg){
            this.progressBar.setString("Done - Success.");
            this.progressBar.setStringPainted(true);
            this.progressBar.setValue(this.progressBar.getMaximum() + 1);
        } else {
            this.progressBar.setString("Done - Fail.");
            this.progressBar.setStringPainted(true);
        }
    }
    
    
}
