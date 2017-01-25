package co.launchable.api.viator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by McElligott on 7/8/2014.
 */
@Service
public class ServiceTicket {
    private DaoTicket daoTicket;

    public DaoTicket getDaoTicket() {
        return daoTicket;
    }

    @Autowired
    public void setPersonDao(DaoTicket daoTicket) {
        this.daoTicket = daoTicket;
    }

    public void addTicket(Ticket booking) {
        getDaoTicket().insert(booking);
    }

    public List<Ticket> fetchAllTickets() {
        return getDaoTicket().selectAll();
    }
}
