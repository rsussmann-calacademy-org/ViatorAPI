package co.launchable.api.viator;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by McElligott on 7/8/2014.
 */
@Entity
@Table(name="ApiBookings")
public class Booking {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    Long id;

    private String bookingReference;
    private Date dateCreated = new Date();
    private Date lastUpdated;

    @OneToMany(mappedBy="booking",cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy("dateCreated DESC")
    private List<BookingAction> actions = new ArrayList();

    @OneToMany(mappedBy="booking",cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.FALSE)
    @OrderBy("dateCreated DESC")
    private List<BookingComment> comments = new ArrayList();

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

    public String getBookingReference() {
        return bookingReference;
    }

    public void setBookingReference(String bookingReference) {
        this.bookingReference = bookingReference;
    }


    public List<BookingAction> getActions() {
        return actions;
    }

    public void setActions(List<BookingAction> actions) {
        this.actions = actions;
    }

    public BookingAction getLastAction() {
        if (actions != null && actions.size() > 0)
            return actions.get(0);
        return null;
    }

    public void addAction(BookingAction bookingAction) {
        bookingAction.setBooking(this);
        bookingAction.setBookingReference(this.bookingReference);
        actions.add(bookingAction);
    }

    public List<BookingComment> getComments() {
        return comments;
    }

    public void setComments(List<BookingComment> comments) {
        this.comments = comments;
    }

    public void addToComments(String comment) {
        BookingComment bookingComment = new BookingComment();
        bookingComment.setComment(comment);
        bookingComment.setBooking(this);
        comments.add(bookingComment);
    }

}
