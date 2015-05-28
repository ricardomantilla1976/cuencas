package hydroScalingAPI.io.swmmCoupling;

/**
 * Mandatory interface for being able to insert a progress bar into CUENCAS GUI
 * @author A. D. L. Zanchetta
 */
public interface ProgressListener {
    public abstract void onCreate(int begin_arg, int end_arg);
    public abstract void onChangeTo(int newValue_arg);
    public abstract void onFinish(boolean success_arg);
}
