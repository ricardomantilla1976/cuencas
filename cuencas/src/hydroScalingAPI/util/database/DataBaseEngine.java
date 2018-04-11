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


/*
 * DataBaseEngine.java
 *
 * Created on June 12, 2003, 12:29 PM
 */

package hydroScalingAPI.util.database;

/**
 * A Memory based implementation of a database engine.  This was created to
 * simplify the comunication between GUIs and Analysis Modules with the Gauges and
 * Locations data in CUENCAS.  However it is generic enough to handle other types
 * of registers.
 * @author Ricardo Mantilla
 */
public class DataBaseEngine {
    
    /**
     * The value used to indicate a SMALLER_THAN type of comparison
     */
    public static final int SMALLER_THAN=-1;
    /**
     * The value used to indicate a EQUAL_TO type of comparison
     */
    public static final int EQUAL_TO=0;
    /**
     * The value used to indicate a GREATER_THAN type of comparison
     */
    public static final int GREATER_THAN=1;
    
    java.util.Hashtable Tables;
    
    /** Creates a new instance of DataBaseEngine */
    public DataBaseEngine() {
        Tables = new java.util.Hashtable();
    }
    
    /**
     * Adds a DB_Table into the database engine
     * @param tableName The name associated to this table
     * @param Fields The properties of the registers in the table
     * @param Type The type of register see {@link hydroScalingAPI.util.database.DB_Table} for details
     */
    public void addTable(String tableName,String [] Fields,String [] Type){
        Tables.put(tableName,new DB_Table(Fields,Type));
    }
    
    /**
     * Adds an entry to one of the tables
     * @param tableName The name associated to this table
     * @param RegisterIndex A unique identifier for the register
     * @param Values The Values to be assigned.  Note: Values need to be in the same order than the
     * Fields variable when the table was created
     */
    public void addData(String tableName, String RegisterIndex,Object[] Values) {
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            addData(tableName,RegisterIndex,new DB_Register (thisTable.getFieldNames(),Values));
        }
    }
    
    /**
     * Adds an entry to one of the tables
     * @param tableName The name associated to this table
     * @param Field The DB_Register to add
     * @param RegisterIndex A unique identifier for the register
     */
    public void addData(String tableName,String RegisterIndex,DB_Register Field){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            thisTable.addRegister(RegisterIndex,Field);
        }
    }
    
    /**
     * Returns a DB_Register whose main index matches a predetermined keyword in a
     * specific table
     * @param tableName The name associated to this table
     * @param ValueToMatch The keyword to be matched
     * @return The DB_Register found or null value
     */
    public DB_Register getRegisterByMainIndex(String tableName,Object ValueToMatch){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.getRegisterByMainIndex(ValueToMatch);
        }
        return null;
    }
    
    /**
     * Finds registers in a table that match a set of values criteria in several of
     * its properties (e.g. in a gazetteer find all the "cities" that are in "colorado")
     * @param tableName The name associated to this table
     * @param FieldName The properties to match
     * @param ValueToMatch The values to match
     * @return The gruop of registers that match the imposed criteria
     */
    public DB_Register[] findRegister(String tableName,String[] FieldName,Object[] ValueToMatch){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.findRegister(FieldName,ValueToMatch);
        }
        return null;
    }
    
    /**
     * A more general search for registers in a table.  It compares registers with a
     * a set of criteria in several of its properties.  The criteria are:<br>
     * <p> 0: equal to</p>
     * <p>-1: smaller than</p>
     * <p> 1: greater than</p>
     * @param tableName The name associated to this table
     * @param FieldName The properties to match
     * @param ValueToCompare The values used as reference
     * @param ConditionToCheckFor The conditions to apply to the reference value
     * @return The gruop of registers that match the imposed criteria
     */
    public DB_Register[] compareFieldsTo(String tableName,String[] FieldName,Object[] ValueToCompare,int[] ConditionToCheckFor){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.compareFieldsTo(FieldName,ValueToCompare,ConditionToCheckFor);
        }
        return null;
    }
    
    /**
     * Returns a vector of Objects with unique values for a given property
     * @param tableName The name associated to this table
     * @param FieldName The property to query
     * @return The vector of unique values.  For "Numeric" type properties a int[] is returned
     * where int[0] is the minimum value and int[1] is the maximum value
     */
    public java.util.Vector getUniqueValues(String tableName,String FieldName){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.getUniqueValues(FieldName);
        }
        return null;
    }
    
    /**
     * Returns the maximum value for a given "Numeric" Property in a given table
     * @param tableName The name associated to this table
     * @param FieldName The property to query
     * @return The maximum value
     */
    public Double getMaxValue(String tableName,String FieldName){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.getMaxValue(FieldName);
        }
        return null;
    }
    
    /**
     * Returns the minimum value for a given "Numeric" Property in a given table
     * @param tableName The name associated to this table
     * @param FieldName The property to query
     * @return The minimum value
     */
    public Double getMinValue(String tableName,String FieldName){
        if (Tables.containsKey(tableName)){
            DB_Table thisTable=(DB_Table) Tables.get(tableName);
            return thisTable.getMinValue(FieldName);
        }
        return null;
    }
    
    /**
     * An extensive test for the Database engine
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
        
        hydroScalingAPI.util.database.DataBaseEngine DB=new hydroScalingAPI.util.database.DataBaseEngine();
        String[] states = {"colorado","alabama","N/A"};
        DB.addTable("Table1",new String[] {     "[code]",
                                                "[agency]",
                                                "[type]",
                                                "[site name]",
                                                "[stream name]",
                                                "[county]",
                                                "[state]",
                                                "[data source]",
                                                "[latitude (deg:min:sec)]",
                                                "[longitude (deg:min:sec)]",
                                                "[altitude ASL (m)]",
                                                "[drainage area (km^2)]",
                                                "[data units]",
                                                "[data accuracy]"},
                            new String[] {      "index",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "alpha",
                                                "numeric",
                                                "numeric",
                                                "numeric",
                                                "numeric",
                                                "alpha",
                                                "alpha"}
                            );
                                                
        
            
        double realStart=java.util.Calendar.getInstance().getTimeInMillis();
        
        for (int j=0;j<1;j++){
            double Start=java.util.Calendar.getInstance().getTimeInMillis();
            for (int i=0;i<1000;i++) {
                DB.addData("Table1","000221"+i+(j*1000),new Object[] {  "000221"+i+(j*1000),
                                                                        "N/A",
                                                                        "N/A",
                                                                        "N/A",
                                                                        "N/A",
                                                                        "N/A",
                                                                        states[i%3],
                                                                        "N/A",
                                                                        new Double(10.0*Math.random()),
                                                                        new Double(10.0),
                                                                        new Double(1.0*(i+1)),
                                                                        new Double(6.0),
                                                                        "N/A",
                                                                        "N/A"});

            }
            double End=java.util.Calendar.getInstance().getTimeInMillis();
        
            double totalTime=(End-Start)/1000.;

            System.out.println("Ends loading this chunk of data in "+totalTime+" seconds");
        
        }
        
        double realEnd=java.util.Calendar.getInstance().getTimeInMillis();
        
        double realTotalTime=(realEnd-realStart)/1000.;

        System.out.println("Ends loading all the data in "+realTotalTime+" seconds");
        
        DB.addData("Table1","000221"+999999,new Object[] {  "000221"+999999,
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "colorado",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A",
                                                            "N/A"});
        
        System.out.println("Begin with queries");
        //System.out.println(DB.getRegisterByMainIndex("Table1","000221"+3));
        /*DB_Register[] result1=DB.findRegister("Table1",new String[] {"[state]","[longitude (deg:min:sec)]"},new Object[] {"alabama",new Double(10.0)});
        if (result1 != null) for (int i=0;i<result1.length;i++) System.out.println(result1[i]);
        java.util.Vector uniqueTest=DB.getUniqueValues("Table1","[state]");
        if (uniqueTest != null) for (int i=0;i<uniqueTest.size();i++) System.out.println(uniqueTest.get(i));*/
        DB_Register[] result2=DB.compareFieldsTo("Table1",new String[] {"[state]","[altitude ASL (m)]","[altitude ASL (m)]"},new Object[] {"alabama",new Double(10),new Double(50)},new int[] {0,1,-1});
        if (result2 != null) for (int i=0;i<result2.length;i++) System.out.println(result2[i]);
        /*Double result3=DB.getMinValue("Table1","[latitude (deg:min:sec)]");
        if (result3 != null) System.out.println(result3);
        Double result4=DB.getMaxValue("Table1","[latitude (deg:min:sec)]");
        if (result4 != null) System.out.println(result4);*/
        System.out.println("Done with queries");
    }
    
}

