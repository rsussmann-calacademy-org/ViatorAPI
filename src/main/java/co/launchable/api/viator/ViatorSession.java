package co.launchable.api.viator;

import co.launchable.api.egalaxy.Event;
import co.launchable.api.egalaxy.EventTicketHoldResponse;
import co.launchable.api.egalaxy.ResponseAuthentication;
import co.launchable.api.egalaxy.ServiceGalaxy;
import co.launchable.api.viator.xml.*;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.*;

/**
 * Created by michaelmcelligott on 5/15/14.
 */
public class ViatorSession implements Runnable {
    private ServiceGalaxy serviceGalaxy;
    private ResponseAuthentication responseAuthentication;
    private String session;
    private Map<String, String> productCodesToEventTypeNames = new HashMap<String, String>();
    private List availabilityRequests = new ArrayList();
    private long sleepMs = 1000;
    private List filteredTours;
    private long created = System.currentTimeMillis();
    Logger log = Logger.getLogger(ViatorAvailabilityRequest.class.toString());

    public List getFilteredTours() {
        return filteredTours;
    }

    public void setFilteredTours(List filteredTours) {
        this.filteredTours = filteredTours;
    }

    public long getSleepMs() {
        return sleepMs;
    }

    public void setSleepMs(long sleepMs) {
        this.sleepMs = sleepMs;
    }

    public ServiceGalaxy getServiceGalaxy() {
        return serviceGalaxy;
    }

    public void setServiceGalaxy(ServiceGalaxy serviceGalaxy) {
        this.serviceGalaxy = serviceGalaxy;
    }

    public void setSession(String session) {
        this.session = session;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }

    public ViatorSession() {}

    public ViatorSession(String session) {
        this.session = session;
    }

    public String getSession() {
        establishSessionIfNull();
        return session;
    }

    private void establishSessionIfNull() {
        if (session == null) {
            responseAuthentication = serviceGalaxy.authenticateToGalaxy();
            if (responseAuthentication.getSessionId() != null)
                session = responseAuthentication.getSessionId();
        }
    }

    protected void holdEventsForFilteredTours(ViatorAvailabilityRequest viatorAvailabilityRequest, Integer quantity, Integer holdSeconds) {
        //calculate when these holds should expire
        long now = new Date().getTime();
        Date holdExpires = new Date();
        holdExpires.setTime(now + (holdSeconds * 1000));

        //iterate over the set of tours in the request (that have already been returned and filtered) and
        //add a hold for each
        List filteredTours = viatorAvailabilityRequest.getFilteredTours();
        for (int i = 0; i < filteredTours.size(); i++) {
            ViatorTour viatorTour = (ViatorTour) filteredTours.get(i);
            Event event = viatorTour.getEvent();
            log.debug(viatorAvailabilityRequest.getSourceRequest().getExternalReference() + ": applying hold to event " + event.getEventID() + ", " + event.getEventName());

            EventTicketHoldResponse eventTicketHoldResponse = serviceGalaxy.apiEventTicketHold(event, session, event.getResourceID(), quantity);
            eventTicketHoldResponse.setExpires(holdExpires);
            eventTicketHoldResponse.setEvent(event);
            eventTicketHoldResponse.setQuantity(quantity);
            eventTicketHoldResponse.setViatorAvailabilityRequest(viatorAvailabilityRequest);

            event.setEventTicketHoldResponse(eventTicketHoldResponse);
            viatorAvailabilityRequest.getEventTicketHoldResponses().add(eventTicketHoldResponse);
            log.info(viatorAvailabilityRequest.getSourceRequest().getExternalReference() + ": hold response received, " + eventTicketHoldResponse.getStatusText());
        }

        //code below has been replaced with iterating over the fleshed out tour objects (which refer to
        //event objects) just because we first need to filter by option code before adding a hold

//        List events = viatorAvailabilityRequest.getEvents();
//        for (int i = 0; i < events.size(); i++) {
//            Event event =  (Event)events.get(i);
//            EventTicketHoldResponse eventTicketHoldResponse = serviceGalaxy.apiEventTicketHold(event, session, null, quantity);
//            eventTicketHoldResponse.setExpires(holdExpires);
//            eventTicketHoldResponse.setEvent(event);
//            eventTicketHoldResponse.setQuantity(quantity);
//
//            event.setEventTicketHoldResponse(eventTicketHoldResponse);
//            holdResponses.add(eventTicketHoldResponse);
//        }
//
//        viatorAvailabilityRequest.setEventTicketHoldResponses(holdResponses);
    }

    //@todo figure out how to recognize the tour option
    private String getTourOptionCode(Event event) {
        return "";
    }

    //@todo figure out how to recognize the tour option name
    private String getTourOptionName(Event event) {
        return "";
    }

    private void populateTourOptionData(ViatorTour tour, Event event) {
        tour.setTourOptionCode(getTourOptionCode(event));
        tour.setTourOptionName(getTourOptionName(event));
    }

    private void determineTourOptions(ViatorAvailabilityRequest thisRequest) {
        List tours = new ArrayList<ViatorTour>();

        List events = thisRequest.getEvents();
        for (int i = 0; i < events.size(); i++) {
            Event event = (Event) events.get(i);
            ViatorTour tour = new ViatorTour();
            tour.setEvent(event);
            tour.setTourCode(thisRequest.getSourceRequest().getSupplierProductCode());
            populateTourOptionData(tour, event);

            tours.add(tour);
        }
        thisRequest.setTours(tours);
    }


    public ViatorAvailabilityRequest getTours(String eventTypeName, Date start, Date end, Integer quantity, AvailabilityRequest sourceRequest) {
        establishSessionIfNull();

        ViatorAvailabilityRequest thisRequest = new ViatorAvailabilityRequest();
        thisRequest.setViatorSession(this);
        thisRequest.setSourceRequest(sourceRequest);

        //we want to return the full set of events regardless of quantity, so pass in a zero here
        thisRequest.setEvents(serviceGalaxy.getEventsForDateRange(start, end, null, eventTypeName, null, null, 0, null, true));

        determineTourOptions(thisRequest);

        //hold on to a reference so that we can release any holds after their time is up
        availabilityRequests.add(thisRequest);
        return thisRequest;
    }


    private void releaseHold(EventTicketHoldResponse eventTicketHoldResponse) {
        serviceGalaxy.apiEventTicketRelease(eventTicketHoldResponse);
    }

    @Override
    public void run() {
        //we're just going to iterate over our hold responses until they've all expired and release each
        //of them, exiting when we're done
        boolean keepGoing = true;
        while (keepGoing) {
            Date now = new Date();

            //iterate over our availabilityRequests and release any holds that have timed out
            for (int i = 0; i < availabilityRequests.size(); i++) {

                ViatorAvailabilityRequest viatorAvailabilityRequest = (ViatorAvailabilityRequest) availabilityRequests.get(i);
                List eventTicketHoldResponses = viatorAvailabilityRequest.getEventTicketHoldResponses();
                for (int j = 0; j < eventTicketHoldResponses.size(); j++) {
                    EventTicketHoldResponse eventTicketHoldResponse = (EventTicketHoldResponse) eventTicketHoldResponses.get(j);

                    //if the hold isn't released and isn't booked and IS expired, release it
                    if (!eventTicketHoldResponse.isReleased()
                            && !eventTicketHoldResponse.isBooked()
                            && eventTicketHoldResponse.getExpires().after(now))
                        releaseHold(eventTicketHoldResponse);

                    //having processed the current hold, update our keepGoing flag if we
                    //need to continue running this thread
                    if (eventTicketHoldResponse.isActive())
                        keepGoing = true;
                }

            }

            try {
                Thread.sleep(sleepMs);
            } catch (InterruptedException ie) {
                //just ignore this, the sleep is just to free up scarce resources for a time
            }
        }
    }
}
