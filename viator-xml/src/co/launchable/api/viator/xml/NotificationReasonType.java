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
 * <p>Java class for notificationReasonType.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * <p>
 * <pre>
 * &lt;simpleType name="notificationReasonType">
 *   &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *     &lt;enumeration value="INCORRECT_INFO"/>
 *     &lt;enumeration value="MISSING_INFO"/>
 *     &lt;enumeration value="CHANGED_MEETING_POINT"/>
 *     &lt;enumeration value="NOT_OPERATE_IN_FUTURE"/>
 *     &lt;enumeration value="NOT_OPERATE_IN_PAST"/>
 *     &lt;enumeration value="NOW_AVAILABLE"/>
 *     &lt;enumeration value="NO_SHOW"/>
 *     &lt;enumeration value="NO_SHOW_SUSPECTED_FRAUD"/>
 *     &lt;enumeration value="OTHER"/>
 *   &lt;/restriction>
 * &lt;/simpleType>
 * </pre>
 * 
 */
@XmlType(name = "notificationReasonType")
@XmlEnum
public enum NotificationReasonType {


    /**
     * Information provided is incorrect
     * 
     */
    INCORRECT_INFO,

    /**
     * Information Missing from Booking
     * 
     */
    MISSING_INFO,

    /**
     * Meeting point / departure details changed
     * 
     */
    CHANGED_MEETING_POINT,

    /**
     * Tour / Activity canceled or will not operate (future)
     * 
     */
    NOT_OPERATE_IN_FUTURE,

    /**
     * Tour / Activity canceled or did not operate (past)
     * 
     */
    NOT_OPERATE_IN_PAST,

    /**
     * Tour / Activity now available
     * 
     */
    NOW_AVAILABLE,

    /**
     * Customer did not show up for the tour/activity
     * 
     */
    NO_SHOW,

    /**
     * Customer  did not show up for the tour/activity - suspected fraud
     * 
     */
    NO_SHOW_SUSPECTED_FRAUD,

    /**
     * Other reason for notification.
     * 
     */
    OTHER;

    public String value() {
        return name();
    }

    public static NotificationReasonType fromValue(String v) {
        return valueOf(v);
    }

}
