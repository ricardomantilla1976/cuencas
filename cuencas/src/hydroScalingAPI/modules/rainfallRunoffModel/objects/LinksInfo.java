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
 * linksInfo.java
 *
 * Created on November 11, 2001, 10:39 AM
 */

package hydroScalingAPI.modules.rainfallRunoffModel.objects;

/**
 * The purpose of this class is to be a centralized database for all the
 * inforamtion related to the system of link that compose the river network.  Note:
 * In order to implement new routing schemes this class must be updated to provide the
 * information for the links
 * @author Ricardo Mantilla
 */
public class LinksInfo extends java.lang.Object {

    /*El proposito de este progrma es ser la base de datos de gemetria hidraulica, rugosidad y en general todos los
           parametros asociados al sistema de links de la cuenca*/

    private float[][] upStreamAreaArray, lengthArray, cheziArray, manningArray, widthArray, dropArray,orderArray, slopeArray, totalChannelLengthArray,mainChannelLenghtArray;
    private float basinArea;

    private float[][] Ck;
    private float lambda1,lambda2,ChanCteVel;

    /**
     * Creates new instanace of LinksInfo
     * @param linksCon The object describing the topologic connectivity of the river network
     * @throws java.io.IOException Captures errors while retreiving information
     */
    public LinksInfo(hydroScalingAPI.util.geomorphology.objects.LinksAnalysis linksCon) throws java.io.IOException{
        lengthArray=linksCon.getVarValues(1);

        upStreamAreaArray=linksCon.getVarValues(2);
        dropArray=linksCon.getVarValues(3);
        orderArray=linksCon.getVarValues(4);
        basinArea=upStreamAreaArray[0][linksCon.getOutletID()];
        totalChannelLengthArray=linksCon.getVarValues(5);
        mainChannelLenghtArray=linksCon.getVarValues(11);

        slopeArray=new float[1][lengthArray[0].length];
        for (int LinkNumber=0;LinkNumber<slopeArray[0].length;LinkNumber++) {
            if (dropArray[0][LinkNumber] <= 0 || lengthArray[0][LinkNumber] == 0){
                slopeArray[0][LinkNumber]=(float) (0.05*Math.pow(upStreamAreaArray[0][LinkNumber]/2.0,-0.2)); //false Geometry
            } else {
                slopeArray[0][LinkNumber]=(float) (dropArray[0][LinkNumber]/lengthArray[0][LinkNumber]/1000.0);
            }
        }
    }

    /**
     * Assigns the slopes of the links using a power law.
     *
     * Slope=coefficient*UpstreamArea^exponent+NORM(sdResiduals)
     *
     * where NORM() is a normally distributed random variable.
     *
     * This method is useful when artrificial networks are being used.  For real networks
     * the slopes are taken from the terrain.
     * @param coefficient The coefficient in the power law
     * @param exponent The exponent in the power law
     * @param sdResiduals The statndard deviation of the residuals of the power law
     */
    public void setSlopesHG(float coefficient, float exponent, float sdResiduals){
        hydroScalingAPI.util.probability.GaussianDistribution ranG=new hydroScalingAPI.util.probability.GaussianDistribution(0, sdResiduals);
        for (int LinkNumber=0;LinkNumber<widthArray[0].length;LinkNumber++) {
            slopeArray[0][LinkNumber]=(float) (coefficient*Math.pow(upStreamAreaArray[0][LinkNumber],exponent)*Math.exp(ranG.sample()));
        }
    }

    /**
     * Assigns the channel widhts of the links using a power law.
     *
     * Width=coefficient*UpstreamArea^exponent+NORM(sdResiduals)
     *
     * where NORM() is a normally distributed random variable.
     *
     * @param coefficient The coefficient in the power law
     * @param exponent The exponent in the power law
     * @param sdResiduals The statndard deviation of the residuals of the power law
     */
    public void setWidthsHG(float coefficient, float exponent,float sdResiduals){
        hydroScalingAPI.util.probability.GaussianDistribution ranG=new hydroScalingAPI.util.probability.GaussianDistribution(0, sdResiduals);
        widthArray=new float[1][lengthArray[0].length];
        for (int LinkNumber=0;LinkNumber<widthArray[0].length;LinkNumber++) {
            //widthArray[0][LinkNumber]=(float) Math.pow(upStreamAreaArray[0][LinkNumber]/2.0,0.5);  // Menabde and Sivapalan
            //widthArray[0][LinkNumber]=(float) Math.pow(upStreamAreaArray[0][LinkNumber]/2.0,0.5); //false Geometry
            //widthArray[0][LinkNumber]=(float) (0.78*Math.pow(upStreamAreaArray[0][LinkNumber],.44));  // Data supported
            //widthArray[0][LinkNumber]=(float) (1.0*Math.pow(upStreamAreaArray[0][LinkNumber],.48));  // As measured in Kansas
            //widthArray[0][LinkNumber]=(float) (5.6*Math.pow(upStreamAreaArray[0][LinkNumber],.46));  // For GC, from Molnar and Ramirez 1998 using pd=0.5
            //widthArray[0][LinkNumber]=(float) (3.6*Math.pow(upStreamAreaArray[0][LinkNumber],.30));  // For Kansas measured by myself
            //widthArray[0][LinkNumber]=(float) (2.36*Math.pow(upStreamAreaArray[0][LinkNumber],0.31));  // Miller, 1995 -  with data from Walnut Gulch
            //widthArray[0][LinkNumber]=(float) (0.29*Math.pow(upStreamAreaArray[0][LinkNumber],.50));  //

            widthArray[0][LinkNumber]=(float) (coefficient*Math.pow(upStreamAreaArray[0][LinkNumber],exponent)*Math.exp(ranG.sample()));
        }
    }

    /**
     * Assigns the Chezy coefficient of the links using a power law.
     *
     * Chezi=coefficient*LinkSlope^exponent+NORM(sdResiduals)
     *
     * where NORM() is a normally distributed random variable.
     *
     * @param coefficient The coefficient in the power law
     * @param exponent The exponent in the power law
     */
    public void setCheziHG(float coefficient, float exponent){
        cheziArray=new float[1][lengthArray[0].length];
        for (int LinkNumber=0;LinkNumber<cheziArray[0].length;LinkNumber++) {
            //cheziArray[0][LinkNumber]=100.0f;
            //cheziArray[0][LinkNumber]=(float) (14.2*Math.pow(slopeArray[0][LinkNumber],-1/3.)); //From Strikler and Shields
            cheziArray[0][LinkNumber]=(float) (coefficient*Math.pow(slopeArray[0][LinkNumber],exponent));
        }
    }

    /**
     * Assigns the Manning coefficient of the links using a power law.
     *
     * Chezi=coefficient*LinkSlope^exponent+NORM(sdResiduals)
     *
     * where NORM() is a normally distributed random variable.
     *
     * @param coefficient The coefficient in the power law
     * @param exponent The exponent in the power law
     */
    public void setManningHG(float coefficient, float exponent){
        manningArray=new float[1][lengthArray[0].length];
        for (int LinkNumber=0;LinkNumber<manningArray[0].length;LinkNumber++) {
            //cheziArray[0][LinkNumber]=100.0f;
            //cheziArray[0][LinkNumber]=(float) (14.2*Math.pow(slopeArray[0][LinkNumber],-1/3.)); //From Strikler and Shields
            manningArray[0][LinkNumber]=(float) (coefficient*Math.pow(slopeArray[0][LinkNumber],exponent));
        }
    }

    /**
     * Assigns the parameters to the velocity function given by
     *
     * FlowVelocity=coefficient*exp(NORM(sdResiduals))*Discharge^exponentQ*UpstreamArea^exponentA
     *
     * where NORM() is a normally distributed random variable.
     *
     * @param coefficient The coefficient in multiple power law
     * @param exponentQ The exponent in the power law with respect to dischage
     * @param exponentA The exponent in the power law with respect to drainage area
     * @param sdResiduals The statndard deviation of the residuals of the power law
     */
    public void setVqParams(float coefficient,float sdResiduals,float exponentQ, float exponentA){
        Ck=new float[1][lengthArray[0].length];
        hydroScalingAPI.util.probability.GaussianDistribution ranG=new hydroScalingAPI.util.probability.GaussianDistribution(0, sdResiduals);
        for (int LinkNumber=0;LinkNumber<Ck[0].length;LinkNumber++) Ck[0][LinkNumber]=(float) (coefficient*Math.exp(ranG.sample()));
        lambda1=exponentQ;
        lambda2=exponentA;
    }

    public void setCteVel(float CteVel){
        ChanCteVel=CteVel;
    }

    public float getCteVel(){
        return ChanCteVel;
    }
    /**
     * Returns the exponent for flow discharge in the parametrization
     *
     * FlowVelocity=coefficient*exp(NORM(sdResiduals))*Discharge^lambda1*UpstreamArea^lambda2
     * @return The exponent lambda1
     */
    public float getLamda1(){
        return lambda1;
    }

    /**
     * Returns the exponent for upstream area in the parametrization
     *
     * FlowVelocity=coefficient*exp(NORM(sdResiduals))*Discharge^lambda1*UpstreamArea^lambda2
     * @return The exponent lambda2
     */
    public float getLamda2(){
        return lambda2;
    }

    /**
     * Returns the upstream area for a given link
     * @param LinkNumber The index of the desired link
     * @return The upstream area in km^2
     */
    public double upStreamArea(int LinkNumber){
        return (double) (upStreamAreaArray[0][LinkNumber]);
    }

    /**
     * Returns the Strahler-order for a given link
     * @param LinkNumber The index of the desired link
     * @return The strahler order
     */
    public double linkOrder(int LinkNumber){
        return (double) (orderArray[0][LinkNumber]);
    }

    /**
     * Returns the Chezi coefficient for a given link
     * @param LinkNumber The index of the desired link
     * @return The Chezi coefficient in meters^(1/2)/second
     */
    public double Chezi(int LinkNumber){
        return (double) cheziArray[0][LinkNumber];
    }

    /**
     * Returns the channel width for a given link
     * @param LinkNumber The index of the desired link
     * @return The channel width in meters
     */
    public double Width(int LinkNumber){
        return (double) widthArray[0][LinkNumber];
    }

    /**
     * Returns the length for a given link
     * @param LinkNumber The index of the desired link
     * @return The link length in meters
     */
    public double Length(int LinkNumber){
        return (double) (lengthArray[0][LinkNumber]);
    }

    public void setLength(int LinkNumber,float newLength){
        lengthArray[0][LinkNumber]=newLength;
    }

    /**
     * Returns the link average slope for a given link
     * @param LinkNumber The index of the desired link
     * @return The link slope
     */
    public double Slope(int LinkNumber){
        return (double) slopeArray[0][LinkNumber];
    }

    /**
     * Returns the upstream total channels lenght for a given link
     * @param LinkNumber The index of the desired link
     * @return The link slope
     */
    public double upStreamTotalLength(int LinkNumber){
        return (double) (totalChannelLengthArray[0][LinkNumber]);
    }

    /**
     * Returns the upstream total channels lenght for a given link
     * @param LinkNumber The index of the desired link
     * @return The link slope
     */
    public double mainChannelLength(int LinkNumber){
        return (double) (mainChannelLenghtArray[0][LinkNumber]);
    }

    /**
     * Returns the array of coefficient in the parametrization
     *
     * FlowVelocity=coefficient*exp(NORM(sdResiduals))*Discharge^lambda1*UpstreamArea^lambda2
     * @return The array of coefficients
     */
    public float[][] getCkArray(){
        return Ck;
    }

    /**
     * Returns the array of upstream areas for links in the network
     * @return The array of areas in km^2
     */
    public float[][] getUpStreamAreaArray(){
        return upStreamAreaArray;
    }

    /**
     * Returns the array of strahler orders for links in the network
     * @return The array of strahler orders
     */
    public float[][] getLinkOrderArray(){
        return orderArray;
    }

    /**
     * Returns the array of Chezi coefficients for links in the network
     * @return The array of Chezi coefficients in m^(1/2)/s
     */
    public float[][] getCheziArray(){
        return cheziArray;
    }

    /**
     * Returns the array of Manning coefficients for links in the network
     * @return The array of Manning coefficients in s/m^(1/3)
     */
    public float[][] getManningArray(){
        return manningArray;
    }

    /**
     * Returns the array of channel widhts for links in the network
     * @return The array of channel widhts in meters
     */
    public float[][] getWidthArray(){
        return widthArray;
    }

    /**
     * Returns the array of lengths for links in the network
     * @return The array of lengths in km
     */
    public float[][] getLengthInKmArray(){
        return lengthArray;
    }

    /**
     * Returns the array of average slopes for links in the network
     * @return The array of average slopes
     */
    public float[][] getSlopeArray(){
        return slopeArray;
    }

    /**
     * Returns the basin area
     * @return The basin area in km^2
     */
    public float basinArea(){

        return basinArea;

    }
}
