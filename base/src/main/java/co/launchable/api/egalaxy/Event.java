package co.launchable.api.egalaxy;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by michaelmcelligott on 3/1/14.
 */
@XmlType(name = "Event")
@XmlAccessorType(XmlAccessType.FIELD)
public class Event {
    @XmlElement(name = "EventID")
    private int eventID;
    @XmlElement(name = "EventName")
    private String eventName;
    @XmlElement(name = "StartDateTime")
    private Date startDateTime;
    @XmlElement(name = "EndDateTime")
    private Date endDateTime;
    @XmlElement(name = "EventTypeID")
    private int eventTypeID;
    @XmlElement(name = "OnSaleDateTime")
    private Date onSaleDateTime;
    @XmlElement(name = "OffSaleDateTime")
    private Date offSaleDateTime;
    @XmlElement(name = "ResourceID")
    private int resourceID;
    @XmlElement(name = "UserEventNumber")
    private int userEventNumber;
    @XmlElement(name = "Available")
    private int available;
    @XmlElement(name = "Status")
    private int status;
    @XmlElement(name = "HasRoster")
    private boolean hasRoster;
    @XmlElement(name = "PrivateEvent")
    private boolean privateEvent;

    private String sessionID;
    private EventTicketHoldResponse eventTicketHoldResponse;

    public EventTicketHoldResponse getEventTicketHoldResponse() {
        return eventTicketHoldResponse;
    }

    public void setEventTicketHoldResponse(EventTicketHoldResponse eventTicketHoldResponse) {
        this.eventTicketHoldResponse = eventTicketHoldResponse;
    }

    public long getDurationInSeconds() {
        return (endDateTime.getTime() - startDateTime.getTime()) / 1000;
    }

    public long getDurationInMinutes() {
        return getDurationInSeconds() / 60;
    }

    public String getSessionID() {
        return sessionID;
    }

    public void setSessionID(String sessionID) {
        this.sessionID = sessionID;
    }

    //private DateFormat dfGalaxyTimestamp = new SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy");
    private DateFormat dfJavascriptParseable = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z");

    public int getEventID() {
        return eventID;
    }

    public void setEventID(int eventID) {
        this.eventID = eventID;
    }

    public String getEventName() {
        return eventName;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public Date getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(Date startDateTime) {
        this.startDateTime = startDateTime;
    }

    public Date getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(Date endDateTime) {
        this.endDateTime = endDateTime;
    }

    public int getEventTypeID() {
        return eventTypeID;
    }

    public void setEventTypeID(int eventTypeID) {
        this.eventTypeID = eventTypeID;
    }

    public Date getOnSaleDateTime() {
        return onSaleDateTime;
    }

    public void setOnSaleDateTime(Date onSaleDateTime) {
        this.onSaleDateTime = onSaleDateTime;
    }

    public Date getOffSaleDateTime() {
        return offSaleDateTime;
    }

    public void setOffSaleDateTime(Date offSaleDateTime) {
        this.offSaleDateTime = offSaleDateTime;
    }

    public String getOnSaleDateTimeJs() {
        return dfJavascriptParseable.format(onSaleDateTime);
    }
    public String getOffSaleDateTimeJs() {
        return dfJavascriptParseable.format(offSaleDateTime);
    }
    public String getStartDateTimeJs() {
        return dfJavascriptParseable.format(startDateTime);
    }
    public String getEndDateTimeJs() {
        return dfJavascriptParseable.format(endDateTime);
    }

    public int getResourceID() {
        return resourceID;
    }

    public void setResourceID(int resourceID) {
        this.resourceID = resourceID;
    }

    public int getUserEventNumber() {
        return userEventNumber;
    }

    public void setUserEventNumber(int userEventNumber) {
        this.userEventNumber = userEventNumber;
    }

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isHasRoster() {
        return hasRoster;
    }

    public void setHasRoster(boolean hasRoster) {
        this.hasRoster = hasRoster;
    }

    public boolean isPrivateEvent() {
        return privateEvent;
    }

    public void setPrivateEvent(boolean privateEvent) {
        this.privateEvent = privateEvent;
    }
}
