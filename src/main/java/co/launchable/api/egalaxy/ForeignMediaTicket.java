package co.launchable.api.egalaxy;

/**
 * Created by McElligott on 6/25/2014.
 */
public class ForeignMediaTicket {
    public static String STATUS_ACTIVATED = "ACTIVATED";
    public static String STATUS_FAILED_ACTIVATION = "FAILED_ACTIVATION";

    String visualId;
    String plu;
    Double price;
    Integer eventId;
    String sessionId;
    String status;

    String travellerIdentifier;
    String travellerTitle;
    String givenName;
    String surname;
    String ageBand;

    public String getVisualId() {
        return visualId;
    }

    public void setVisualId(String visualId) {
        this.visualId = visualId;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Integer getEventId() {
        return eventId;
    }

    public void setEventId(Integer eventId) {
        this.eventId = eventId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }


    public String getStatus() {
        return status;
    }

    public String getTravellerIdentifier() {
        return travellerIdentifier;
    }

    public void setTravellerIdentifier(String travellerIdentifier) {
        this.travellerIdentifier = travellerIdentifier;
    }

    public String getTravellerTitle() {
        return travellerTitle;
    }

    public void setTravellerTitle(String travellerTitle) {
        this.travellerTitle = travellerTitle;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getAgeBand() {
        return ageBand;
    }

    public void setAgeBand(String ageBand) {
        this.ageBand = ageBand;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
