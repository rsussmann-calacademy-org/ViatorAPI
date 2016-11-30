package co.launchable.api.viator;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by McElligott on 7/9/2014.
 */
@Entity
@Table(name="ApiComments")
public class BookingComment {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    private Date dateCreated = new Date();
    private Date lastUpdated;
    private String comment;

    @ManyToOne
    @JoinColumn(name = "bookingId")
    private Booking booking;

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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }
}
