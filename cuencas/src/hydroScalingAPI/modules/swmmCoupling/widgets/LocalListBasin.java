/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.modules.swmmCoupling.widgets;


/**
 *
 * @author A. D. L. Zanchetta
 */
public class LocalListBasin{
    private final int xOutlet, yOutlet;
    
    public LocalListBasin(int x_arg, int y_arg){
        this.xOutlet = x_arg;
        this.yOutlet = y_arg;
    }

    public int getxOutlet() {
        return xOutlet;
    }

    public int getyOutlet() {
        return yOutlet;
    }
    
    public String toString(){
        return ("X: " + this.xOutlet + ", Y: " + this.yOutlet);
    }
}
