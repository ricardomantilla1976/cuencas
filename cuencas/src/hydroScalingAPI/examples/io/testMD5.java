/*
 * testMD5.java
 *
 * Created on June 12, 2007, 9:48 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hydroScalingAPI.examples.io;

/**
 *
 * @author ricardo
 */


import java.security.*;


public class testMD5 {
    
    /** Creates a new instance of testMD5 */
    public testMD5() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String sessionid="1234512345678dfghjcvbndfgherty4567dfghvbnfghrty12345678923456sdfghjgfcgvkg kjhgk gkuy glku kugl jhgl \nkhgkjkgkjkjgkjhg kjgk jgkj gkj kjg j jg kjgjkgiufytxytsuydi";
        
        byte[] defaultBytes = sessionid.getBytes();
        try{
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            algorithm.reset();
            algorithm.update(defaultBytes);
            byte messageDigest[] = algorithm.digest();
            
            StringBuffer hexString = new StringBuffer();
            for (int i=0;i<messageDigest.length;i++) {
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            }
            String foo = messageDigest.toString();
            System.out.println("sessionid "+sessionid+" md5 version is "+hexString.toString() + " " +foo);
            sessionid=hexString+"";
        }catch(NoSuchAlgorithmException nsae){
            
        }
        
    }
    
}

