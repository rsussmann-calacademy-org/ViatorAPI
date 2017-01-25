package co.launchable.api.viator;

import co.launchable.api.egalaxy.Event;

/**
 * Created by michaelmcelligott on 5/15/14.
 */
public class ViatorTour {
    private Event event;
    private String tourCode;
    private String tourOptionCode;
    private String tourOptionName;

    public Event getEvent() {
        return event;
    }

    public void setEvent(Event event) {
        this.event = event;
    }

    public String getTourCode() {
        return tourCode;
    }

    public void setTourCode(String tourCode) {
        this.tourCode = tourCode;
    }

    public String getTourOptionCode() {
        return tourOptionCode;
    }

    public void setTourOptionCode(String tourOptionCode) {
        this.tourOptionCode = tourOptionCode;
    }

    public String getTourOptionName() {
        return tourOptionName;
    }

    public void setTourOptionName(String tourOptionName) {
        this.tourOptionName = tourOptionName;
    }
}
