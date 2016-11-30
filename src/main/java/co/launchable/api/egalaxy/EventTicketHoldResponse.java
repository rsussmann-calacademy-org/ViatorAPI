package co.launchable.api.egalaxy;

import co.launchable.api.viator.ViatorAvailabilityRequest;

import java.util.Date;

/**
 * Created by michaelmcelligott on 5/15/14.
 */
public class EventTicketHoldResponse {
    private Event event;
    private String statusCode;
    private String statusText;
    private String sectionId;
    private String errorCode;
    private String errorText;
    private String sessionId;
    private String capacityId;
    private Date expires;
    private boolean released = false;
    private boolean booked = false;
    private int quantity;
    private ViatorAvailabilityRequest viatorAvailabilityRequest;

    public ViatorAvailabilityRequest getViatorAvailabilityRequest() {
        return viatorAvailabilityRequest;
    }

    public void setViatorAvailabilityRequest(ViatorAvailabilityRequest viatorAvailabilityRequest) {
        this.viatorAvailabilityRequest = viatorAvailabilityRequest;
    }

    public boolean isActive() {
        if (booked) return false;
        return !released;
    }

    public boolean isBooked() {
        return booked;
    }

    public void setBooked(boolean booked) {
        this.booked = booked;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public boolean isReleased() {
        return released;
    }

    public void setReleased(boolean released) {
        this.released = released;
    }

    public Date getExpires() {
        return expires;
    }

    public void setExpires(Date expires) {
        this.expires = expires;
    }

    public String getCapacityId() {
        return capacityId;
    }

    public void setCapacityId(String capacityId) {
        this.capacityId = capacityId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusText() {
        return statusText;
    }

    public void setStatusText(String statusText) {
        this.statusText = statusText;
    }

    public String getSectionId() {
        return sectionId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }
}
