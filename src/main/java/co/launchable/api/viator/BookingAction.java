package co.launchable.api.viator;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.annotation.Generated;
import javax.persistence.*;
import java.util.*;

/**
 * Created by McElligott on 7/9/2014.
 */
@Entity
@Table(name="ApiBookingActions")
public class BookingAction {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    private Date dateCreated = new Date();
    private Date lastUpdated;
    private String bookingReference;
    private String actionType;
    private String status;
    private String confirmation;
    private String productCode;
    private String optionCode;
    private Long eventId;

    @OneToMany(mappedBy="action",cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    Set<Ticket> tickets = new HashSet<Ticket>();

    @ManyToOne
    @JoinColumn(name = "bookingId")
    private Booking booking;

    public static final String STATUS_CANCELED = "CANCELED";
    public static final String STATUS_CONFIRMED = "CONFIRMED";
    public static final String STATUS_HELD = "HELD";
    public static final String STATUS_ACTIVATED = "ACTIVATED";
    public static final String STATUS_REJECTED = "REJECTED";

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }

    public String getActionType() {
        return actionType;
    }

    public void setActionType(String actionType) {
        this.actionType = actionType;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getConfirmation() {
        return confirmation;
    }

    public void setConfirmation(String confirmation) {
        this.confirmation = confirmation;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getOptionCode() {
        return optionCode;
    }

    public void setOptionCode(String optionCode) {
        this.optionCode = optionCode;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public Set<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(Set<Ticket> tickets) {
        this.tickets = tickets;
    }

    public void addTicket(Ticket ticket) {
        ticket.setAction(this);
        this.tickets.add(ticket);
    }

    public void transferTicketsFrom(BookingAction bookingAction) {
        for (java.util.Iterator iterator = bookingAction.getTickets().iterator(); iterator.hasNext(); ) {
            Ticket ticket =  (Ticket)iterator.next();
            this.addTicket(ticket);
        }
    }

    public void generateConfirmation() {
        setConfirmation(UUID.randomUUID().toString());
    }
}
