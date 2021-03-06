//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.12 at 12:48:55 PM PST 
//


package co.launchable.api.viator.xml;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;extension base="{http://toursgds.com/api/01}Response">
 *       &lt;sequence>
 *         &lt;element name="BatchTourAvailability" type="{http://toursgds.com/api/01}BatchTourAvailability" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "batchTourAvailability"
})
@XmlRootElement(name = "BatchAvailabilityResponse")
public class BatchAvailabilityResponse
    extends Response
{

    @XmlElement(name = "BatchTourAvailability", required = true)
    protected List<BatchTourAvailability> batchTourAvailability;

    /**
     * Gets the value of the batchTourAvailability property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the batchTourAvailability property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getBatchTourAvailability().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link BatchTourAvailability }
     * 
     * 
     */
    public List<BatchTourAvailability> getBatchTourAvailability() {
        if (batchTourAvailability == null) {
            batchTourAvailability = new ArrayList<BatchTourAvailability>();
        }
        return this.batchTourAvailability;
    }

}
