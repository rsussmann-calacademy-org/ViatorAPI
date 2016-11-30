package co.launchable.api.viator;

import co.launchable.api.egalaxy.Event;
import co.launchable.api.egalaxy.EventTicketHoldResponse;
import co.launchable.api.viator.xml.AvailabilityRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by michaelmcelligott on 5/15/14.
 */
public class ViatorAvailabilityRequest {
    private AvailabilityRequest sourceRequest;
    private List<EventTicketHoldResponse> eventTicketHoldResponses = new ArrayList<EventTicketHoldResponse>();
    private List<Event> events;
    private List<ViatorTour> tours;
    private List<ViatorTour> filteredTours;
    private ViatorSession viatorSession;

    public ViatorSession getViatorSession() {
        return viatorSession;
    }

    public void setViatorSession(ViatorSession viatorSession) {
        this.viatorSession = viatorSession;
    }

    public AvailabilityRequest getSourceRequest() {
        return sourceRequest;
    }

    public void setSourceRequest(AvailabilityRequest sourceRequest) {
        this.sourceRequest = sourceRequest;
    }

    public List<EventTicketHoldResponse> getEventTicketHoldResponses() {
        return eventTicketHoldResponses;
    }

    public void setEventTicketHoldResponses(List<EventTicketHoldResponse> eventTicketHoldResponses) {
        this.eventTicketHoldResponses = eventTicketHoldResponses;
    }

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public List<ViatorTour> getTours() {
        return tours;
    }

    public void setTours(List<ViatorTour> tours) {
        this.tours = tours;
    }

    public List<ViatorTour> getFilteredTours() {
        return filteredTours;
    }

    public void setFilteredTours(List<ViatorTour> filteredTours) {
        this.filteredTours = filteredTours;
    }

    public void applyHoldsToFilteredTours() {
        if (sourceRequest.getAvailabilityHold() != null) {
            int seconds = sourceRequest.getAvailabilityHold().getExpiry().getSeconds();
            int quantity = sourceRequest.getTravellerMix().getTotal();
            viatorSession.holdEventsForFilteredTours(this, quantity, seconds);
        }
    }
}
