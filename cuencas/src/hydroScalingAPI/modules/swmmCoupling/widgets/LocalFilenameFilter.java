/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.swmmCoupling.widgets;

import java.io.File;
import java.io.FilenameFilter;

/**
 *
 * @author A. D. L. Zanchetta
 */
class LocalFilenameFilter implements FilenameFilter {
    private final String requiredExt;
        
    protected LocalFilenameFilter(String extension_arg){
        if (extension_arg.startsWith(".")){
            this.requiredExt = extension_arg.substring(1);
        } else {
            this.requiredExt = extension_arg;
        }
    }
        
    public boolean accept(File dir, String name) {
        if(name.lastIndexOf('.')>0){
            // get last index for '.' char
            int lastIndex = name.lastIndexOf('.');
                  
            // get extension
            String str = name.substring(lastIndex);
                  
            // match path name extension
            if(str.equals("." + this.requiredExt)){
                 return true;
            }
       }
       return false;
    }
}
