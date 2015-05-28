package hydroScalingAPI.modules.swmmCoupling.objects;

import hydroScalingAPI.io.MetaRaster;
import hydroScalingAPI.io.swmmCoupling.PrecipitationWriter;
import hydroScalingAPI.io.swmmCoupling.ProgressListener;
import java.util.List;
import javax.swing.JProgressBar;

/**
 * This is the thread called by @link TODO for importing spatial homogeny rainfall data
 * @author A. D. L. Zanchetta
 */
public class CsvImporterBackground extends javax.swing.SwingWorker implements ProgressListener {
    private final JProgressBar progressBar;
    private final PrecipitationWriter usedWriter;
    private String[][] dataSequence;
    private MetaRaster exampleMetaRaster;
    
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
        System.out.println("Will do doInBackground");
        this.usedWriter.setOwnListener(this);
        this.usedWriter.writeTimeSequence(this.dataSequence, 
                                          this.exampleMetaRaster);
        System.out.println("Did doInBackground");
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
        if (this.progressBar != null){
            this.progressBar.setString("Process Done.");
        }
    }
    
    public void onCreate(int begin_arg, int end_arg) {
        // basic check
        if(this.progressBar == null) return;
        
        this.progressBar.setMinimum(begin_arg);
        this.progressBar.setMaximum(end_arg);
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
        
        if (success_arg){
            this.progressBar.setString("Done - Success.");
        } else {
            this.progressBar.setString("Done - Fail.");
        }
    }
    
    
}
