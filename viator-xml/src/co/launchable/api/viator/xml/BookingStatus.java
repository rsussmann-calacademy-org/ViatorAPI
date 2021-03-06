//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.02.12 at 12:48:55 PM PST 
//


package co.launchable.api.viator.xml;

import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for bookingStatus.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="bookingStatus">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="CONFIRMED"/>
 *     &lt;enumeration value="UNAVAILABLE"/>
 *     &lt;enumeration value="PENDING"/>
 *     &lt;enumeration value="CANCELLED"/>
 *     &lt;enumeration value="AMENDED"/>
 *     &lt;enumeration value="PENDING_AMENDMENT"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "bookingStatus")
@XmlEnum
public enum BookingStatus {

    CONFIRMED,
    UNAVAILABLE,
    PENDING,
    CANCELLED,
    AMENDED,
    PENDING_AMENDMENT;

    public String value() {
        return name();
    }

    public static BookingStatus fromValue(String v) {
        return valueOf(v);
    }

}
