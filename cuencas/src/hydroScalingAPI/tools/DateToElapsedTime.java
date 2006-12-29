/*
 * DateToSeconds.java
 *
 * Created on June 26, 2003, 11:09 AM
 */

package hydroScalingAPI.tools;

/**
 *
 * @author Ricardo Mantilla
 */
public class DateToElapsedTime {
    
    private java.util.Calendar dateComposite;
    private int yy=0, mm=0, dd=0, hh=0 ,mi=0 ,ss=0;
        
    
    /** Creates a new instance of DateToSeconds */
    public DateToElapsedTime(String date) {
        
        java.util.StringTokenizer tokens=new java.util.StringTokenizer(date,".");
        dateComposite=java.util.Calendar.getInstance();
        
        if (tokens.countTokens() > 0) yy=Integer.parseInt(tokens.nextToken());
        if (tokens.countTokens() > 0) mm=Integer.parseInt(tokens.nextToken());
        if (tokens.countTokens() > 0) dd=Integer.parseInt(tokens.nextToken());
        if (tokens.countTokens() > 0) hh=Integer.parseInt(tokens.nextToken());
        if (tokens.countTokens() > 0) mi=Integer.parseInt(tokens.nextToken());
        if (tokens.countTokens() > 0) ss=Integer.parseInt(tokens.nextToken());
        
        dateComposite.set(yy,mm,dd,hh,mi,ss);
        
    }
    
    public double getDateInYears(){
        return yy+mm/12.0+dd/365.25+hh/(365.25*24.0)+mi/(365.25*24.0*60.)+ss/(365.25*24.0*60.*60.);
    }
    
    public double getSeconds(){
        return dateComposite.getTimeInMillis()/1000.0;
    }
    
    public double getMinutes(){
        return dateComposite.getTimeInMillis()/1000.0/60.0;
    }
    
    public double getHours(){
        return dateComposite.getTimeInMillis()/1000.0/60.0/60.0;
    }
    
    public double getDays(){
        return dateComposite.getTimeInMillis()/1000.0/60.0/60.0/24.0;
    }
    
    public double getYears(){
        return dateComposite.getTimeInMillis()/1000.0/60.0/60.0/24.0/365.25;
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        DateToElapsedTime test=new DateToElapsedTime("1996.11.29.13.59.00");
        System.out.println(test.getDays());
        
        
    }
    
}
