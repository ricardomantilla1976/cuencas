/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.swmmCoupling.widgets;

import hydroScalingAPI.modules.swmmCoupling.io.SubBasinsLogManager;
import java.util.HashMap;

/**
 *
 * @author A. D. L. Zanchetta
 */
class LocalComboHash {
    private HashMap listedHash;
        
    protected LocalComboHash(HashMap listedHash_arg){
        this.listedHash = listedHash_arg;
    }
        
    @Override
    public String toString(){
        return(SubBasinsLogManager.parseHash(this.listedHash));
    }
}
