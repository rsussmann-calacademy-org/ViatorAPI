//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.12 at 12:48:55 PM PST 
//


package co.launchable.api.viator.xml;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * 
 *                 Tour Availability with support for AvailabilityHold                  
 *             
 * 
 * <p>Java class for TourAvailability complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TourAvailability">
 *   &lt;complexContent>
 *     &lt;extension base="{http://toursgds.com/api/01}TourAvailabilityBase">
 *       &lt;sequence>
 *         &lt;element name="AvailabilityHold" type="{http://toursgds.com/api/01}AvailabilityHoldResponse" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TourAvailability", propOrder = {
    "availabilityHold"
})
public class TourAvailability
    extends TourAvailabilityBase
{

    @XmlElement(name = "AvailabilityHold")
    protected AvailabilityHoldResponse availabilityHold;

    /**
     * Gets the value of the availabilityHold property.
     * 
     * @return
     *     possible object is
     *     {@link AvailabilityHoldResponse }
     *     
     */
    public AvailabilityHoldResponse getAvailabilityHold() {
        return availabilityHold;
    }

    /**
     * Sets the value of the availabilityHold property.
     * 
     * @param value
     *     allowed object is
     *     {@link AvailabilityHoldResponse }
     *     
     */
    public void setAvailabilityHold(AvailabilityHoldResponse value) {
        this.availabilityHold = value;
    }

}
