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
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * 
 *                 Tour Option.           
 *             
 * 
 * <p>Java class for TourOptionsBase complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="TourOptionsBase">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="SupplierOptionCode" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SupplierOptionName" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TourDepartureTime" type="{http://www.w3.org/2001/XMLSchema}time" minOccurs="0"/>
 *         &lt;element name="TourDuration" type="{http://www.w3.org/2001/XMLSchema}duration" minOccurs="0"/>
 *         &lt;element name="Option" type="{http://toursgds.com/api/01}OptionType" maxOccurs="unbounded" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TourOptionsBase", propOrder = {
    "supplierOptionCode",
    "supplierOptionName",
    "tourDepartureTime",
    "tourDuration",
    "option"
})
@XmlSeeAlso({
    TourOptions.class,
    AvailableTourOptions.class
})
public class TourOptionsBase {

    @XmlElement(name = "SupplierOptionCode")
    protected String supplierOptionCode;
    @XmlElement(name = "SupplierOptionName")
    protected String supplierOptionName;
    @XmlElement(name = "TourDepartureTime")
    @XmlSchemaType(name = "time")
    protected XMLGregorianCalendar tourDepartureTime;
    @XmlElement(name = "TourDuration")
    protected Duration tourDuration;
    @XmlElement(name = "Option")
    protected List<OptionType> option;

    /**
     * Gets the value of the supplierOptionCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierOptionCode() {
        return supplierOptionCode;
    }

    /**
     * Sets the value of the supplierOptionCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierOptionCode(String value) {
        this.supplierOptionCode = value;
    }

    /**
     * Gets the value of the supplierOptionName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierOptionName() {
        return supplierOptionName;
    }

    /**
     * Sets the value of the supplierOptionName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierOptionName(String value) {
        this.supplierOptionName = value;
    }

    /**
     * Gets the value of the tourDepartureTime property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTourDepartureTime() {
        return tourDepartureTime;
    }

    /**
     * Sets the value of the tourDepartureTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTourDepartureTime(XMLGregorianCalendar value) {
        this.tourDepartureTime = value;
    }

    /**
     * Gets the value of the tourDuration property.
     * 
     * @return
     *     possible object is
     *     {@link Duration }
     *     
     */
    public Duration getTourDuration() {
        return tourDuration;
    }

    /**
     * Sets the value of the tourDuration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Duration }
     *     
     */
    public void setTourDuration(Duration value) {
        this.tourDuration = value;
    }

    /**
     * Gets the value of the option property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the option property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getOption().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link OptionType }
     * 
     * 
     */
    public List<OptionType> getOption() {
        if (option == null) {
            option = new ArrayList<OptionType>();
        }
        return this.option;
    }

}