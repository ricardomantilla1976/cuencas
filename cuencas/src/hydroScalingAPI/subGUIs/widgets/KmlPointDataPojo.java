/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package hydroScalingAPI.subGUIs.widgets;
import de.micromata.opengis.kml.v_2_2_0.Coordinate;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
/**
 *
 * @author achitale
 */
@XmlRootElement(name = "KMLPoint")
@XmlAccessorType (XmlAccessType.FIELD)
public class KmlPointDataPojo {
    
        private Double originalLat ;
         private Double newLat ;
        private Double newLon;
        private int indexInArray ; 
        private boolean  visited ;
        private int linkId;

    @Override
    public String toString() {
        return "KmlPointDataPojo{" + "originalLat=" + originalLat + ", originalLon=" + originalLon + ", newLat=" + newLat + ", newLon=" + newLon + ", indexInArray=" + indexInArray + ", visited=" + visited + '}';
    }
        private Double originalLon ;

    public int getLinkId() {
        return linkId;
    }

    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

        
    public void setOriginalLat(Double originalLat) {
        this.originalLat = originalLat;
    }

    public void setOriginalLon(Double originalLon) {
        this.originalLon = originalLon;
    }

    public void setNewLat(Double newLat) {
        this.newLat = newLat;
    }

    public void setNewLon(Double newLon) {
        this.newLon = newLon;
    }

    public void setIndexInArray(int indexInArray) {
        this.indexInArray = indexInArray;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public Double getOriginalLat() {
        return originalLat;
    }

    public Double getOriginalLon() {
        return originalLon;
    }

    public Double getNewLat() {
        return newLat;
    }

    public Double getNewLon() {
        return newLon;
    }

    public int getIndexInArray() {
        return indexInArray;
    }

    public boolean isVisited() {
        return visited;
    }
       

    
       
    
}
