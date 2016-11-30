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
import javax.xml.bind.annotation.XmlType;
import javax.xml.datatype.XMLGregorianCalendar;


/**
 * A tour/activity booking.Represents a booking already made
 * 
 * <p>Java class for Booking complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="Booking">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;group ref="{http://toursgds.com/api/01}BookingBase"/>
 *         &lt;element name="BookingStatus" type="{http://toursgds.com/api/01}bookingStatus"/>
 *         &lt;element name="SupplierConfirmationNumber" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Booking", propOrder = {
    "bookingReference",
    "travelDate",
    "supplierProductCode",
    "location",
    "tourOptions",
    "inclusions",
    "currencyCode",
    "amount",
    "traveller",
    "travellerMix",
    "requiredInfo",
    "specialRequirement",
    "pickupPoint",
    "supplierNote",
    "additionalRemarks",
    "contactDetail",
    "bookingStatus",
    "supplierConfirmationNumber"
})
public class Booking {

    @XmlElement(name = "BookingReference", required = true)
    protected String bookingReference;
    @XmlElement(name = "TravelDate", required = true)
    @XmlSchemaType(name = "date")
    protected XMLGregorianCalendar travelDate;
    @XmlElement(name = "SupplierProductCode", required = true)
    protected String supplierProductCode;
    @XmlElement(name = "Location")
    protected String location;
    @XmlElement(name = "TourOptions")
    protected TourOptions tourOptions;
    @XmlElement(name = "Inclusions")
    protected co.launchable.api.viator.xml.BookingRequestBase.Inclusions inclusions;
    @XmlElement(name = "CurrencyCode")
    protected String currencyCode;
    @XmlElement(name = "Amount")
    protected Double amount;
    @XmlElement(name = "Traveller", required = true)
    protected List<Traveller> traveller;
    @XmlElement(name = "TravellerMix")
    protected TravellerMix travellerMix;
    @XmlElement(name = "RequiredInfo")
    protected co.launchable.api.viator.xml.BookingRequestBase.RequiredInfo requiredInfo;
    @XmlElement(name = "SpecialRequirement")
    protected String specialRequirement;
    @XmlElement(name = "PickupPoint")
    protected String pickupPoint;
    @XmlElement(name = "SupplierNote")
    protected String supplierNote;
    @XmlElement(name = "AdditionalRemarks")
    protected co.launchable.api.viator.xml.BookingRequestBase.AdditionalRemarks additionalRemarks;
    @XmlElement(name = "ContactDetail")
    protected co.launchable.api.viator.xml.BookingRequestBase.ContactDetail contactDetail;
    @XmlElement(name = "BookingStatus", required = true)
    protected BookingStatus bookingStatus;
    @XmlElement(name = "SupplierConfirmationNumber", required = true)
    protected String supplierConfirmationNumber;

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
     * Gets the value of the travelDate property.
     * 
     * @return
     *     possible object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public XMLGregorianCalendar getTravelDate() {
        return travelDate;
    }

    /**
     * Sets the value of the travelDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link XMLGregorianCalendar }
     *     
     */
    public void setTravelDate(XMLGregorianCalendar value) {
        this.travelDate = value;
    }

    /**
     * Gets the value of the supplierProductCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierProductCode() {
        return supplierProductCode;
    }

    /**
     * Sets the value of the supplierProductCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierProductCode(String value) {
        this.supplierProductCode = value;
    }

    /**
     * Gets the value of the location property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the value of the location property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLocation(String value) {
        this.location = value;
    }

    /**
     * Gets the value of the tourOptions property.
     * 
     * @return
     *     possible object is
     *     {@link TourOptions }
     *     
     */
    public TourOptions getTourOptions() {
        return tourOptions;
    }

    /**
     * Sets the value of the tourOptions property.
     * 
     * @param value
     *     allowed object is
     *     {@link TourOptions }
     *     
     */
    public void setTourOptions(TourOptions value) {
        this.tourOptions = value;
    }

    /**
     * Gets the value of the inclusions property.
     * 
     * @return
     *     possible object is
     *     {@link co.launchable.api.viator.xml.BookingRequestBase.Inclusions }
     *     
     */
    public co.launchable.api.viator.xml.BookingRequestBase.Inclusions getInclusions() {
        return inclusions;
    }

    /**
     * Sets the value of the inclusions property.
     * 
     * @param value
     *     allowed object is
     *     {@link co.launchable.api.viator.xml.BookingRequestBase.Inclusions }
     *     
     */
    public void setInclusions(co.launchable.api.viator.xml.BookingRequestBase.Inclusions value) {
        this.inclusions = value;
    }

    /**
     * Gets the value of the currencyCode property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCurrencyCode() {
        return currencyCode;
    }

    /**
     * Sets the value of the currencyCode property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCurrencyCode(String value) {
        this.currencyCode = value;
    }

    /**
     * Gets the value of the amount property.
     * 
     * @return
     *     possible object is
     *     {@link Double }
     *     
     */
    public Double getAmount() {
        return amount;
    }

    /**
     * Sets the value of the amount property.
     * 
     * @param value
     *     allowed object is
     *     {@link Double }
     *     
     */
    public void setAmount(Double value) {
        this.amount = value;
    }

    /**
     * Gets the value of the traveller property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the traveller property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getTraveller().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Traveller }
     * 
     * 
     */
    public List<Traveller> getTraveller() {
        if (traveller == null) {
            traveller = new ArrayList<Traveller>();
        }
        return this.traveller;
    }

    /**
     * Gets the value of the travellerMix property.
     * 
     * @return
     *     possible object is
     *     {@link TravellerMix }
     *     
     */
    public TravellerMix getTravellerMix() {
        return travellerMix;
    }

    /**
     * Sets the value of the travellerMix property.
     * 
     * @param value
     *     allowed object is
     *     {@link TravellerMix }
     *     
     */
    public void setTravellerMix(TravellerMix value) {
        this.travellerMix = value;
    }

    /**
     * Gets the value of the requiredInfo property.
     * 
     * @return
     *     possible object is
     *     {@link co.launchable.api.viator.xml.BookingRequestBase.RequiredInfo }
     *     
     */
    public co.launchable.api.viator.xml.BookingRequestBase.RequiredInfo getRequiredInfo() {
        return requiredInfo;
    }

    /**
     * Sets the value of the requiredInfo property.
     * 
     * @param value
     *     allowed object is
     *     {@link co.launchable.api.viator.xml.BookingRequestBase.RequiredInfo }
     *     
     */
    public void setRequiredInfo(co.launchable.api.viator.xml.BookingRequestBase.RequiredInfo value) {
        this.requiredInfo = value;
    }

    /**
     * Gets the value of the specialRequirement property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSpecialRequirement() {
        return specialRequirement;
    }

    /**
     * Sets the value of the specialRequirement property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpecialRequirement(String value) {
        this.specialRequirement = value;
    }

    /**
     * Gets the value of the pickupPoint property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPickupPoint() {
        return pickupPoint;
    }

    /**
     * Sets the value of the pickupPoint property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPickupPoint(String value) {
        this.pickupPoint = value;
    }

    /**
     * Gets the value of the supplierNote property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getSupplierNote() {
        return supplierNote;
    }

    /**
     * Sets the value of the supplierNote property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSupplierNote(String value) {
        this.supplierNote = value;
    }

    /**
     * Gets the value of the additionalRemarks property.
     * 
     * @return
     *     possible object is
     *     {@link co.launchable.api.viator.xml.BookingRequestBase.AdditionalRemarks }
     *     
     */
    public co.launchable.api.viator.xml.BookingRequestBase.AdditionalRemarks getAdditionalRemarks() {
        return additionalRemarks;
    }

    /**
     * Sets the value of the additionalRemarks property.
     * 
     * @param value
     *     allowed object is
     *     {@link co.launchable.api.viator.xml.BookingRequestBase.AdditionalRemarks }
     *     
     */
    public void setAdditionalRemarks(co.launchable.api.viator.xml.BookingRequestBase.AdditionalRemarks value) {
        this.additionalRemarks = value;
    }

    /**
     * Gets the value of the contactDetail property.
     * 
     * @return
     *     possible object is
     *     {@link co.launchable.api.viator.xml.BookingRequestBase.ContactDetail }
     *     
     */
    public co.launchable.api.viator.xml.BookingRequestBase.ContactDetail getContactDetail() {
        return contactDetail;
    }

    /**
     * Sets the value of the contactDetail property.
     * 
     * @param value
     *     allowed object is
     *     {@link co.launchable.api.viator.xml.BookingRequestBase.ContactDetail }
     *     
     */
    public void setContactDetail(co.launchable.api.viator.xml.BookingRequestBase.ContactDetail value) {
        this.contactDetail = value;
    }

    /**
     * Gets the value of the bookingStatus property.
     * 
     * @return
     *     possible object is
     *     {@link BookingStatus }
     *     
     */
    public BookingStatus getBookingStatus() {
        return bookingStatus;
    }

    /**
     * Sets the value of the bookingStatus property.
     * 
     * @param value
     *     allowed object is
     *     {@link BookingStatus }
     *     
     */
    public void setBookingStatus(BookingStatus value) {
        this.bookingStatus = value;
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

}
