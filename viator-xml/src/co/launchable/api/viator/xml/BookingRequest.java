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
 *     &lt;extension base="{http://toursgds.com/api/01}BookingRequestBase">
 *       &lt;sequence>
 *         &lt;element name="AvailabilityHoldReference" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
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
    "availabilityHoldReference"
})
@XmlRootElement(name = "BookingRequest")
public class BookingRequest
    extends BookingRequestBase
{

    @XmlElement(name = "AvailabilityHoldReference")
    protected String availabilityHoldReference;

    /**
     * Gets the value of the availabilityHoldReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getAvailabilityHoldReference() {
        return availabilityHoldReference;
    }

    /**
     * Sets the value of the availabilityHoldReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAvailabilityHoldReference(String value) {
        this.availabilityHoldReference = value;
    }

}
