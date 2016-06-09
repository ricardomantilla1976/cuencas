/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.subGUIs.widgets;
import java.util.List;
 
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author achitale
 */
@XmlRootElement(name = "KMLPointList")
@XmlAccessorType (XmlAccessType.FIELD)
public class KmlPointListPojo {
    @XmlElement(name = "KMLPoint")
    private List<KmlPointDataPojo> visitedKmlPoints = null;
 
    public List<KmlPointDataPojo> getvisitedKMLList() {
        return visitedKmlPoints;
    }
 
    public void setvisitedKMLList(List<KmlPointDataPojo> Kmllist) {
        this.visitedKmlPoints = Kmllist;
    }
}
