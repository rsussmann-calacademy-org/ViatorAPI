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
 *     &lt;extension base="{http://toursgds.com/api/01}Response">
 *       &lt;sequence>
 *         &lt;element name="BookingReference" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="SupplierConfirmationNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="SupplierCancellationNumber" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="TransactionStatus" type="{http://toursgds.com/api/01}CancellationTransactionStatus"/>
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
    "bookingReference",
    "supplierConfirmationNumber",
    "supplierCancellationNumber",
    "transactionStatus"
})
@XmlRootElement(name = "BookingCancellationResponse")
public class BookingCancellationResponse
    extends Response
{

    @XmlElement(name = "BookingReference")
    protected String bookingReference;
    @XmlElement(name = "SupplierConfirmationNumber", required = true)
    protected String supplierConfirmationNumber;
    @XmlElement(name = "SupplierCancellationNumber")
    protected String supplierCancellationNumber;
    @XmlElement(name = "TransactionStatus", required = true)
    protected CancellationTransactionStatus transactionStatus;

    /**
     * Gets the value of the bookingReference property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getBookingReference() {
        return bookingReference;
    }

    /**
     * Sets the value of the bookingReference property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBookingReference(String value) {
        this.bookingReference = value;
    }

    /**
     * Gets the value of the supplierConfirmationNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierConfirmationNumber() {
        return supplierConfirmationNumber;
    }

    /**
     * Sets the value of the supplierConfirmationNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierConfirmationNumber(String value) {
        this.supplierConfirmationNumber = value;
    }

    /**
     * Gets the value of the supplierCancellationNumber property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierCancellationNumber() {
        return supplierCancellationNumber;
    }

    /**
     * Sets the value of the supplierCancellationNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierCancellationNumber(String value) {
        this.supplierCancellationNumber = value;
    }

    /**
     * Gets the value of the transactionStatus property.
     * 
     * @return
     *     possible object is
     *     {@link CancellationTransactionStatus }
     *     
     */
    public CancellationTransactionStatus getTransactionStatus() {
        return transactionStatus;
    }

    /**
     * Sets the value of the transactionStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link CancellationTransactionStatus }
     *     
     */
    public void setTransactionStatus(CancellationTransactionStatus value) {
        this.transactionStatus = value;
    }

}
