package co.launchable.api.viator;

import co.launchable.api.egalaxy.Event;
import co.launchable.api.egalaxy.ResponseCreateTickets;
import co.launchable.api.egalaxy.ResponseSimple;
import co.launchable.api.egalaxy.ServiceGalaxy;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Michael on 1/26/2015.
 */
public class GalaxyTicketCreator implements Runnable {
    @Autowired
    private ServiceGalaxy serviceGalaxy;

    private boolean keepGoing = true;
    private boolean abandonSessionEveryTime = true;
    private int eventId;
    private int resourceId;
    private String plu;
    private long period;
    private long abandonSessionAfter = 1000 * 60 * 60;
    Logger log = Logger.getLogger(GalaxyTicketCreator.class);

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public int getEventId() {
        return eventId;
    }

    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public void setResourceId(int resourceId) {
        this.resourceId = resourceId;
    }

    public String getPlu() {
        return plu;
    }

    public void setPlu(String plu) {
        this.plu = plu;
    }

    public boolean isKeepGoing() {
        return keepGoing;
    }

    public void setKeepGoing(boolean keepGoing) {
        this.keepGoing = keepGoing;
    }

    public long getAbandonSessionAfter() {
        return abandonSessionAfter;
    }

    public void setAbandonSessionAfter(long abandonSessionAfter) {
        this.abandonSessionAfter = abandonSessionAfter;
    }

    public boolean isAbandonSessionEveryTime() {
        return abandonSessionEveryTime;
    }

    public void setAbandonSessionEveryTime(boolean abandonSessionEveryTime) {
        this.abandonSessionEveryTime = abandonSessionEveryTime;
    }

    public void run() {
        boolean sessionShouldBeAbandoned = false;
        long lastSessionCreated = 0;
        String sessionId = null;

        while (keepGoing) {
            log.debug("ticket creation iteration");

            //create a dummy event
            Event event = new Event();
            event.setResourceID(resourceId);
            event.setEventID(eventId);

            //create a dummy ticket
            Ticket ticket = new Ticket();
            ticket.setEventId(event.getEventID());
            ticket.setPlu(plu);
            ticket.setGivenName("Joe");
            ticket.setSurname("Scientist");
            ticket.setAgeBand("Adult");
            ticket.setTravellerTitle("Dr.");
            ticket.setTravellerIdentifier("Primary");

            //add it to the list
            List tickets = new ArrayList();
            tickets.add(ticket);

            try {

                if (!abandonSessionEveryTime) {
                    //check to see if we should use a new session by testing against the specified timeout
                    //if we should, use the api to abandon the current session (if defined) and then
                    //request a new one
                    log.info("periodic session abandonment, checking for session abandonment need");
                    long now = System.currentTimeMillis();
                    boolean sessionInactive = !serviceGalaxy.sessionStillActive(sessionId);
                    if (sessionInactive || now - lastSessionCreated > abandonSessionAfter) {
                        if (sessionId != null) {
                            ResponseSimple responseSimple = serviceGalaxy.apiAbandonSession(sessionId);
                        }
                        sessionId = serviceGalaxy.getSessionId(null, abandonSessionAfter);
                        lastSessionCreated = now;
                    }
                } else {
                    sessionId = serviceGalaxy.getSessionId(null, abandonSessionAfter);
                    log.info("immediate session abandonment, generated " + sessionId);
                    lastSessionCreated = System.currentTimeMillis();
                }

                //sessionId = serviceGalaxy.getSessionId(sessionId);
                long start = System.currentTimeMillis();
                ResponseCreateTickets responseCreateTickets = serviceGalaxy.apiEventTicketsCreate(event, sessionId, tickets, 0.0);
                long end = System.currentTimeMillis();

                if (abandonSessionEveryTime) {
                    log.info("immediate session abandonment, abandoning " + sessionId);

                    if (sessionId != null) {
                        ResponseSimple responseSimple = serviceGalaxy.apiAbandonSession(sessionId);
                    }
                }

                try {
                    Thread.sleep(period);
                } catch (Exception e) {
                    e.printStackTrace();
                    log.info("error in thread sleep, message was " + e.getMessage());

                }
            } catch (Exception e) {
                e.printStackTrace();
                log.info("error in dummy ticket creation, message was " + e.getMessage());
            }
        }
    }
}
