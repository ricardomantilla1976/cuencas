/*
 * LocationWriter.java
 *
 * Created on July 9, 2003, 11:43 AM
 */

package hydroScalingAPI.io;

/**
 *
 * @author Ricardo Mantilla
 */
public class LocationWriter {
    
    String[] parameters={   "[type]",
                            "[source]",
                            "[site name]",
                            "[county]",
                            "[state]",
                            "[latitude (deg:min:sec)]",
                            "[longitude (deg:min:sec)]",
                            "[altitude ASL (m)]",
                            "[images]",
                            "[information]"};
    
    
    /** Creates a new instance of LocationWriter */
    public LocationWriter(java.io.File locationFile,Object[] register) throws java.io.IOException{
        
        if (register.length != 10) return;
        
        locationFile.getParentFile().mkdirs();
        java.io.FileOutputStream outputLocal=new java.io.FileOutputStream(locationFile);
        java.util.zip.GZIPOutputStream outputComprim=new java.util.zip.GZIPOutputStream(outputLocal);
        java.io.BufferedWriter fileMeta = new java.io.BufferedWriter(new java.io.OutputStreamWriter(outputComprim));
        
        String toWrite;
        
        fileMeta.write(parameters[0]+"\n");
        toWrite=(String)register[0];
        if(toWrite.equalsIgnoreCase("--------"))
            fileMeta.write("N/A"+"\n");
        else
            fileMeta.write(toWrite+"\n");
        fileMeta.write("\n");
        
        fileMeta.write(parameters[1]+"\n");
        toWrite=(String)register[1];
        if(toWrite.equalsIgnoreCase("--------"))
            fileMeta.write("N/A"+"\n");
        else
            fileMeta.write(toWrite+"\n");
        fileMeta.write("\n");
        
        fileMeta.write(parameters[2]+"\n");
        toWrite=(String)register[2];
        if(toWrite.equalsIgnoreCase("--------"))
            fileMeta.write("N/A"+"\n");
        else
            fileMeta.write(toWrite+"\n");
        fileMeta.write("\n");
        
        fileMeta.write(parameters[3]+"\n");
        toWrite=(String)register[3];
        if(toWrite.equalsIgnoreCase("--------"))
            fileMeta.write("N/A"+"\n");
        else
            fileMeta.write(toWrite+"\n");
        fileMeta.write("\n");
        
        fileMeta.write(parameters[4]+"\n");
        toWrite=(String)register[4];
        if(toWrite.equalsIgnoreCase("--------"))
            fileMeta.write("N/A"+"\n");
        else
            fileMeta.write(toWrite+"\n");
        fileMeta.write("\n");
        
        fileMeta.write(parameters[5]+"\n");
        toWrite=(String)register[5];
        if(toWrite.equalsIgnoreCase("--------"))
            fileMeta.write("N/A"+"\n");
        else
            fileMeta.write(toWrite+"\n");
        fileMeta.write("\n");
        
        fileMeta.write(parameters[6]+"\n");
        toWrite=(String)register[6];
        if(toWrite.equalsIgnoreCase("--------"))
            fileMeta.write("N/A"+"\n");
        else
            fileMeta.write(toWrite+"\n");
        fileMeta.write("\n");
        
        fileMeta.write(parameters[7]+"\n");
        toWrite=(String)register[7];
        if(toWrite.equalsIgnoreCase("--------"))
            fileMeta.write("N/A"+"\n");
        else
            fileMeta.write(toWrite+"\n");
        fileMeta.write("\n");
        
        fileMeta.write(parameters[8]+"\n");
        Object[] imAndDes=(Object[])register[8];
        if(imAndDes.length == 0) fileMeta.write("N/A"+"\n");
        for(int i=0;i<imAndDes.length;i++) fileMeta.write(imAndDes[i]+"\n");
        fileMeta.write("\n");
        
        fileMeta.write(parameters[9]+"\n");
        fileMeta.write((String)register[9]+"\n");
        fileMeta.write("\n");
        
        fileMeta.close();
        outputComprim.close();
        outputLocal.close();
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try{
            System.out.println(new java.io.File("c:/temp/some/other/").mkdirs()); 
            System.out.println(new java.io.File("c:/temp/some/other/stuff.yes").createNewFile());
        }catch(java.io.IOException ioe){
            System.out.println(ioe);
        }
        
    }
    
}
