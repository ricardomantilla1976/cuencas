package hydroScalingAPI.io.swmmCoupling;

/**
 * Class containing auxiliary methods for a standardized .vhc file naming
 * @author A. D. L. Zanchetta
 */
public abstract class PrecipitationWriterAux {
    
    /**
     * Creates a filename of English-month-named type to only-number type
     * @param filename_arg Filename with date in only-number format. For instance: "something.2014.02.22.txt"
     * @return Filename with date in month-named format. For instance: "something.2014.February.22.txt"
     */
    public static String adaptToWordedMonth(String filename_arg){
        int monthNumber, count;
        String[] splited;
        String monthName;
        String retString;
                
        // basic check
        if (filename_arg == null) return (null);
        
        splited = filename_arg.split("\\.");
        
        // case where there is no month
        if (splited.length < 3) return (filename_arg);
        
        // identify month number and obtain its English name
        monthNumber = Integer.parseInt(splited[splited.length - 3]);
        monthName = PrecipitationWriterAux.monthNameFromNumber(monthNumber);
        splited[splited.length - 3] = monthName;
        
        // implode string
        retString = "";
        for(count = 0; count < splited.length - 1; count++){
            retString += splited[count] + ".";
        }
        retString += splited[splited.length - 1];
        
        return(retString);
    }
    
    /**
     * Obtain English month name from numerical number
     * @param monthNumber_arg Month number, being 1 for January up to 12 for December
     * @return English month name if given month number is valid (1 to 12), NULL otherwise
     */
    public static String monthNameFromNumber(int monthNumber_arg){
        switch(monthNumber_arg){
            case 1:
                return "January";
            case 2:
                return "February";
            case 3:
                return "March";
            case 4:
                return "April";
            case 5:
                return "May";
            case 6:
                return "June";
            case 7:
                return "July";
            case 8:
                return "August";
            case 9:
                return "September";
            case 10:
                return "October";
            case 11:
                return "November";
            case 12:
                return "December";
            default:
                return (null);
        }
    }
    
    // Just-for-check method
    protected static void main(String[] args){
        String filename, returned;
        
        filename = "1.2004.vhc";
        returned = PrecipitationWriterAux.adaptToWordedMonth(filename);
        
        System.out.println(returned);
    }
}
