package co.launchable.api.viator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by McElligott on 7/8/2014.
 */
@Service
public class ServiceBooking {
    private DaoBooking daoBooking;

    public DaoBooking getDaoBooking() {
        return daoBooking;
    }

    @Autowired
    public void setDaoBooking(DaoBooking daoBooking) {
        this.daoBooking = daoBooking;
    }

    public void addBooking(Booking booking) {
        getDaoBooking().insert(booking);
    }

    public void updateBooking(Booking booking) {
        getDaoBooking().update(booking);
    }

    public List<Booking> fetchAllBookings() {
        return getDaoBooking().selectAll();
    }

    public Booking getBookingByBookingReference(String bookingReference) {
        return getDaoBooking().findByBookingReference(bookingReference);
    }
}
