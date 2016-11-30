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
 *                 Holds details pertaining to each of the tour options (including default if no tour options exist).                
 *             
 * 
 * <p>Java class for TourOptions complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TourOptions">
 *   &lt;complexContent>
 *     &lt;extension base="{http://toursgds.com/api/01}TourOptionsBase">
 *       &lt;sequence>
 *         &lt;element name="Language" type="{http://toursgds.com/api/01}TourLanguage" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/extension>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TourOptions", propOrder = {
    "language"
})
public class TourOptions
    extends TourOptionsBase
{

    @XmlElement(name = "Language")
    protected TourLanguage language;

    /**
     * Gets the value of the language property.
     * 
     * @return
     *     possible object is
     *     {@link TourLanguage }
     *     
     */
    public TourLanguage getLanguage() {
        return language;
    }

    /**
     * Sets the value of the language property.
     * 
     * @param value
     *     allowed object is
     *     {@link TourLanguage }
     *     
     */
    public void setLanguage(TourLanguage value) {
        this.language = value;
    }

}