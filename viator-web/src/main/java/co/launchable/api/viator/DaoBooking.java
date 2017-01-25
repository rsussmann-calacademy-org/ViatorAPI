package co.launchable.api.viator;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by McElligott on 7/8/2014.
 */
@Repository
public class DaoBooking {
    @Autowired
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }


    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void insert(Booking booking) {
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        session.save(booking);
        session.getTransaction().commit();
    }

    public void update(Booking booking) {
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        session.saveOrUpdate(booking);
        session.getTransaction().commit();
    }

    public List<Booking> selectAll() {
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Criteria criteria = session.createCriteria(Booking.class);
        List<Booking> bookings = (List<Booking>) criteria.list();
        session.getTransaction().commit();
        return bookings;
    }

    public Booking findByBookingReference(String bookingReference) {
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Criteria criteria = session.createCriteria(Booking.class);
        criteria.add(Restrictions.eq("bookingReference", bookingReference));
        List<Booking> bookings = criteria.list();
        session.getTransaction().commit();

        if (bookings.size() >= 1)
            return bookings.get(0);
        return null;
    }

    public Booking getBookingById(String id) {
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Booking booking = (Booking)session.get(Booking.class, id);
        session.getTransaction().commit();
        return booking;
    }
}
