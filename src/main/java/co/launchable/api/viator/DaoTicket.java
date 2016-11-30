package co.launchable.api.viator;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Created by McElligott on 7/8/2014.
 */
@Repository
public class DaoTicket {
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    @Autowired
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void insert(Ticket ticket) {
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        session.save(ticket);
        session.getTransaction().commit();

    }

    public List<Ticket> selectAll() {
        Session session = getSessionFactory().getCurrentSession();
        session.beginTransaction();
        Criteria criteria = session.createCriteria(Ticket.class);
        List<Ticket> tickets = (List<Ticket>) criteria.list();
        session.getTransaction().commit();
        return tickets;
    }
}
