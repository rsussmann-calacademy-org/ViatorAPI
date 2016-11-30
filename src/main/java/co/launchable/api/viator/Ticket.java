package co.launchable.api.viator;
import javax.persistence.*;
import java.util.Date;

/**
 * Created by McElligott on 7/8/2014.
 */
@Entity
@Table(name="ApiTickets")
public class Ticket {
    public static final String STATUS_ACTIVATED = "ACTIVATED";
    public static final String STATUS_CANCELED = "CANCELED";
    public static final String STATUS_CREATED = "CREATED";
    public static final String STATUS_USED = "USED";
    public static final String STATUS_FAILED = "FAILED";

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    private Date dateCreated = new Date();
    private Date lastUpdated;
    private String visualId;
    private String plu;
    private String travellerIdentifier;
    private String travellerTitle;
    private String givenName;
    private String surname;
    private String status;
    private String ageBand;
    private Double price;
    private Integer eventId;

    @ManyToOne
    @JoinColumn(name = "bookingActionId")
    private BookingAction action;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getVisualId() {
        return visualId;
    }

    public void setVisualId(String visualId) {
        this.visualId = visualId;
    }

    public String getTravellerIdentifier() {
        return travellerIdentifier;
    }

    public void setTravellerIdentifier(String travellerIdentifier) {
        this.travellerIdentifier = travellerIdentifier;
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

    public String getTravellerTitle() {
        return travellerTitle;
    }

    public void setTravellerTitle(String travellerTitle) {
        this.travellerTitle = travellerTitle;
    }

    public BookingAction getAction() {
        return action;
    }

    public void setAction(BookingAction action) {
        this.action = action;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public String getAgeBand() {
        return ageBand;
    }

    public void setAgeBand(String ageBand) {
        this.ageBand = ageBand;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
