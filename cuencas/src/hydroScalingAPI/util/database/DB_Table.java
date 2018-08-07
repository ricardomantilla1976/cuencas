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
 * DB_Table.java
 *
 * Created on June 16, 2003, 1:34 AM
 */

package hydroScalingAPI.util.database;

/**
 * A second level building block for the database.  The DB_Table groups a number of
 * registers and performs actions on them, such as searchs, retreival, adding and
 * removing, getting value ranges, etc.
 * @author Ricardo Mantilla
 */
public class DB_Table {
    
    private String[] FieldNames;
    private String[] FieldTypes;
    private java.util.Hashtable nameToTypeMap=new java.util.Hashtable();
    private java.util.Vector indexNames=new java.util.Vector();
    private java.util.Hashtable uniqueValues=new java.util.Hashtable();
    private java.util.Hashtable FieldsInformation=new java.util.Hashtable();
    private int indexField = -1;
    private int lastIndexUsed = 0;
    
    private boolean first=true;
    
    /**
     * Creates a new DB_Table
     * @param Fields The properties that the table will handle
     * @param Type The type associated to each of the properties.  Available types are:<br>
     * <p>Index</p>
     * <p>Alpha</p>
     * <p>Numeric</p>
     * The type determines how search condition are aplied to this object and how to
     * determine the unique values for a given property
     */
    public DB_Table(String[] Fields,String[] Type) {
        
        FieldNames=Fields;
        FieldTypes=Type;
        for (int i=0;i<Fields.length;i++){
            
            nameToTypeMap.put(FieldNames[i],FieldTypes[i]);
            
            if(FieldTypes[i].equalsIgnoreCase("index")) indexField=i;
            
            if (FieldTypes[i].equalsIgnoreCase("alpha")) {
                java.util.Vector thisNewVector=new java.util.Vector();
                uniqueValues.put(FieldNames[i],thisNewVector);
            }
            
            if (FieldTypes[i].equalsIgnoreCase("numeric")) uniqueValues.put(FieldNames[i],new Double[] {new Double(Double.MAX_VALUE),new Double(-Double.MAX_VALUE)});
            
        }
        
    }
    
    /**
     * Adds a regiter to the table using a master String unique descriptor
     * @param RegisterIndex The unique descriptor for the Register to be added
     * @param Register The register to be added
     */
    public void addRegister(String RegisterIndex,DB_Register Register) {
        indexNames.addElement(RegisterIndex);
        FieldsInformation.put(RegisterIndex,Register);
        checkUniqueness(Register);
    }
    
    /**
     * Adds a regiter to the table using a pre-defined master index (defined ath the
     * time of the DB_Table construction) or an integer to identyfy the register
     * @param Register The register to be added
     */
    public void addRegister(DB_Register Register) {
        String RegisterIndex="";
        if (indexField == -1) 
            RegisterIndex=""+(lastIndexUsed++);
        else
            RegisterIndex=(String) (Register.getProperty((String)FieldNames[indexField]));
        
        addRegister(RegisterIndex,Register);
    }
    
    private void checkUniqueness(DB_Register Register) {
        double numericValue=0.0;
        for (int i=0;i<FieldNames.length;i++){
            if (i != indexField){
                Object theProperty=Register.getProperty(FieldNames[i]);
                if (FieldTypes[i].equalsIgnoreCase("alpha")){
                    if (!theProperty.toString().equalsIgnoreCase("n/a")){
                        java.util.Vector theVector=(java.util.Vector)uniqueValues.get(FieldNames[i]);
                        if (!theVector.contains(theProperty)){
                            theVector.add(theProperty);
                        }
                    }
                }
                if (FieldTypes[i].equalsIgnoreCase("numeric")){
                    double minValue=((Double[])uniqueValues.get(FieldNames[i]))[0].doubleValue();
                    double maxValue=((Double[])uniqueValues.get(FieldNames[i]))[1].doubleValue();
                    if (!theProperty.toString().equalsIgnoreCase("n/a")){
                        try{
                            numericValue = ((Double) theProperty).doubleValue();
                        } catch(java.lang.ClassCastException CE_D){
                            try{
                                numericValue=((Float) theProperty).doubleValue();
                            } catch(java.lang.ClassCastException CE_F){
                                try{
                                    numericValue=((Integer) theProperty).doubleValue();
                                } catch(java.lang.ClassCastException CE_I){
                                    
                                }
                            }
                        }
                        
                        minValue=Math.min(numericValue,minValue);
                        maxValue=Math.max(numericValue,maxValue);
                        uniqueValues.put(FieldNames[i],new Double[] {new Double(minValue),new Double(maxValue)});
                        
                    }
                }
            }
        }
    }
    
    /**
     * Returns a DB_Register whose main index matches a predetermined keyword
     * @param ValueToMatch The keyword to be matched
     * @return The DB_Register found or null value
     */
    public DB_Register getRegisterByMainIndex(Object ValueToMatch){
        return (DB_Register) FieldsInformation.get((String) ValueToMatch);
    }
    
    /**
     * Finds registers in the table that match a set of values criteria in several of
     * its properties (e.g. in a gazetteer find all the "cities" that are in "colorado")
     * @param FieldName The properties to match
     * @param ValueToCompare The values to match
     * @return The gruop of registers that match the imposed criteria
     */
    public DB_Register[] findRegister(String[] FieldName,Object[] ValueToCompare){
        
        int[] ConditionToCheckFor=new int[FieldName.length];
        java.util.Arrays.fill(ConditionToCheckFor,0);
        return compareFieldsTo(FieldName,ValueToCompare,ConditionToCheckFor);
    }
    
    /**
     * A more general search for registers in the table.  It compares registers with a
     * a set of criteria in several of its properties.  The criteria are:<br>
     * <p> 0: equal to</p>
     * <p>-1: smaller than</p>
     * <p> 1: greater than</p>
     * @param FieldName The properties to match
     * @param ValueToCompare The values to match
     * @param ConditionToCheckFor The conditions to apply to the reference value
     * @return The gruop of registers that match the imposed criteria
     */
    public DB_Register[] compareFieldsTo(String[] FieldName,Object[] ValueToCompare,int[] ConditionToCheckFor){
        for (int i=0;i<FieldName.length;i++){
             if (nameToTypeMap.get(FieldName[i]) == "alpha" && ConditionToCheckFor[i] != 0) return null; 
        }
        
        java.util.Vector serchAt=(java.util.Vector) indexNames.clone();
        for (int i=0;i<FieldName.length;i++){
            for (int j=0;j<serchAt.size();j++){
                DB_Register thisField = (DB_Register) FieldsInformation.get(serchAt.get(j));

                if (nameToTypeMap.get(FieldName[i]) == "numeric"){
                    double numericValue=0;
                    try{
                        numericValue = ((Double) thisField.getProperty(FieldName[i])).doubleValue();
                    } catch(java.lang.ClassCastException CE_D){
                        try{
                            numericValue=((Float) thisField.getProperty(FieldName[i])).doubleValue();
                        } catch(java.lang.ClassCastException CE_F){
                            try{
                                numericValue=((Integer) thisField.getProperty(FieldName[i])).doubleValue();
                            } catch(java.lang.ClassCastException CE_I){
                                
                            }
                        }
                    }
                    Double thisProperty=new Double(numericValue);
                    
                    switch (ConditionToCheckFor[i]){
                        case 0:  
                            if (!thisProperty.equals(ValueToCompare[i])){
                                serchAt.remove(j);
                                j--;
                            }
                            break;
                        case 1:  
                            if (thisProperty.compareTo((Double)ValueToCompare[i]) <= 0){
                                serchAt.remove(j);
                                j--;
                            }
                            break;
                        case -1:
                            if (thisProperty.compareTo((Double)ValueToCompare[i]) >= 0){
                                serchAt.remove(j);
                                j--;
                            }
                            break;

                    }


                } else {
                    if (!thisField.getProperty(FieldName[i]).equals(ValueToCompare[i])){
                        serchAt.remove(j);
                        j--;
                    }
                }
            }
        }
        
        if (serchAt.size() > 0){
        
            DB_Register[] results=new DB_Register[serchAt.size()];
            for (int j=0;j<serchAt.size();j++)
                    results[j]= (DB_Register) FieldsInformation.get((String)serchAt.get(j));

            return results;
        } else {
            return null;
        }
    }
    
    /**
     * Returns the maximum value for a given "Numeric" Property
     * @param FieldName The property to query
     * @return The maximum value
     */
    public Double getMaxValue(String FieldName) {
        return((Double[])uniqueValues.get(FieldName))[1];
    }
    
    /**
     * Returns the minimum value for a given "Numeric" Property
     * @param FieldName The property to query
     * @return The minimum value
     */
    public Double getMinValue(String FieldName) {
        return((Double[])uniqueValues.get(FieldName))[0];
    }
    
    /**
     * Returns a list of the available properties in the registers of the table
     * @return A String[] with the field names
     */
    public String[] getFieldNames(){
        return FieldNames;
    }
    
    /**
     * Returns a vector of Objects with unique values for a given property
     * @param FieldName The property to query
     * @return The vector of unique values.  For "Numeric" type properties a int[] is returned
     * where int[0] is the minimum value and int[1] is the maximum value
     */
    public java.util.Vector getUniqueValues(String FieldName){
        
        if (first){
            for (int i=0;i<FieldNames.length;i++){

                if (FieldTypes[i].equalsIgnoreCase("alpha")) {
                    java.util.Vector unsortedVector=(java.util.Vector)uniqueValues.get(FieldNames[i]);
                    java.util.Collections.sort(unsortedVector);
                    unsortedVector.add(0,"--------");
                }
            }
            first=false;
        }
        
        if(FieldName.equalsIgnoreCase(FieldNames[indexField])) return indexNames;
        
        return (java.util.Vector)uniqueValues.get(FieldName);
    }
    
}
