/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.swmmCoupling.widgets;

import java.io.File;

/**
 *
 * @author A. D. L. Zanchetta
 */
class LocalComboFile {
    private final File listedFile;
        
    protected LocalComboFile(File addedFile){
        this.listedFile = addedFile;
    }

    public File getListedFile() {
        return this.listedFile;
    }
        
    @Override
    public String toString(){
        return(listedFile.getName());
    }
}
