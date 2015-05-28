package hydroScalingAPI.io.swmmCoupling;

/**
 * A very bean class for associate a time unit numeric label with a String English-name value
 * @author A. D. L. Zanchetta
 */
public class TimeIntervalOption {
    private final int timeIntervalLabel;
    private final String timeIntervalDesc;
        
    protected TimeIntervalOption(int timeIntervalLabel_arg,
                                 String timeIntervalDesc_arg){
        this.timeIntervalDesc = timeIntervalDesc_arg;
        this.timeIntervalLabel = timeIntervalLabel_arg;
    }

    public int getTimeIntervalLabel() {
        return timeIntervalLabel;
    }

    public String getTimeIntervalDesc() {
        return timeIntervalDesc;
    }
    
    @Override
    public String toString(){
        return (this.timeIntervalDesc);
    }
}
