package co.launchable.api.egalaxy;

import co.launchable.api.viator.Ticket;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by McElligott on 7/25/2014.
 */
public class ResponseActivateTickets extends ResponseSimple {
    private List<Ticket> tickets = new ArrayList<Ticket>();

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }
}
