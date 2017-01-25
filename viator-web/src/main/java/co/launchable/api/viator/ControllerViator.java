package co.launchable.api.viator;


import co.launchable.api.egalaxy.*;
import co.launchable.api.email.ServiceEmail;
import co.launchable.api.viator.xml.Error;
import co.launchable.api.viator.xml.*;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.ServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * Created by michaelmcelligott on 1/10/14.
 */
@PropertySource("classpath:viator.properties")
@RequestMapping("/viator")
@Controller
public class ControllerViator extends BaseController implements InitializingBean {
    Logger log = Logger.getLogger(ControllerViator.class);

    @Autowired
    Environment env;

    @Autowired(required=false)
    ServiceGalaxy serviceGalaxy;

    @Autowired
    ServiceBooking serviceBooking;

    @Autowired
    GalaxyTicketCreator galaxyTicketCreator;

    @Autowired
    ServiceEmail serviceEmail;

    int resellerId = 1198;

    private Map<String, String> productAgeCodesToPLUPrefixes = new HashMap<String, String>();
    private Map<String, String> productCodesToEventTypeNames = new HashMap<String, String>();
    private Map<String, String> productCodesToEventNames = new HashMap<String, String>();
    private Map<String, String> productCodesToPlus = new HashMap<String, String>();

    private Map<String, String> tourCodesToProductCodes = new HashMap<String, String>();
    private Map<String, ViatorSession> keysToSessions = new HashMap<String, ViatorSession>();
    private Map<String, EventTicketHoldResponse> keysToHolds = new HashMap<String, EventTicketHoldResponse>();
    private Set<String> permittedTourNames = new HashSet<String>();
    private Map codesToProductConfigs = new HashMap();
    private String apiKey = "2I-PlSLj9OrULJ2-LkQzYUTHL24IJY7gFHMmLYiuQ2c";
    private String supplierId = "1004";
    private Thread tGalaxyTicketCreator;
    private long emailTransactionTimeout = 29000;
    private SimpleDateFormat sdfDateTime = new SimpleDateFormat("MM-dd hh:mm:ss");
    private VelocityEngine velocityEngine;
    private boolean abandonSessionImmediate = true;

    public boolean isAbandonSessionImmediate() {
        return abandonSessionImmediate;
    }

    public void setAbandonSessionImmediate(boolean abandonSessionImmediate) {
        this.abandonSessionImmediate = abandonSessionImmediate;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public int getResellerId() {
        return resellerId;
    }

    public void setResellerId(int resellerId) {
        this.resellerId = resellerId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public ControllerViator() {
        super();

        permittedTourNames.add("NightLife");
        permittedTourNames.add("GMC - General Admission");

        //set up our product configs.. these are used to map an incoming request for a particular
        //traveler type to the PLU and price for that type of ticket
        //right now we're just supporting two products, General Admission and NightLife, but in the future
        //there will be more, and we need to move this configuration to either a spring config or to a
        //database
        ProductConfig pcGeneralAdmission = new ProductConfig();
        pcGeneralAdmission.setProductCode("5072CAS");
        pcGeneralAdmission.setProductName("GMC - General Admission");
        pcGeneralAdmission.setAgeBandInsensitive(Boolean.FALSE);

        pcGeneralAdmission.getAgeBandsToConfigMaps().put("Adult", createConfigMap("60120", "10151560", 29.95));
        pcGeneralAdmission.getAgeBandsToConfigMaps().put("Child", createConfigMap("60121", "10151561", 19.95));
        pcGeneralAdmission.getAgeBandsToConfigMaps().put("Youth", createConfigMap("60122", "10151562", 24.95));
        pcGeneralAdmission.getAgeBandsToConfigMaps().put("Student", createConfigMap("60123", "10151563", 24.95));
        pcGeneralAdmission.getAgeBandsToConfigMaps().put("Senior", createConfigMap("60124", "10151564", 24.95));

        //this appears to be replaced by the above config map entries
        pcGeneralAdmission.getAgeBandsToVisualIdPrefixes().put("Child", "60121");
        pcGeneralAdmission.getAgeBandsToVisualIdPrefixes().put("Youth", "60122");
        pcGeneralAdmission.getAgeBandsToVisualIdPrefixes().put("Student", "60123");
        pcGeneralAdmission.getAgeBandsToVisualIdPrefixes().put("Senior", "60124");
        codesToProductConfigs.put(pcGeneralAdmission.getProductCode(), pcGeneralAdmission);

        ProductConfig pcNightLife = new ProductConfig();
        pcNightLife.setProductCode("5072NIGHT");
        pcNightLife.setProductName("California Academy of Sciences Nightlife");
        pcNightLife.setAgeBandInsensitive(Boolean.TRUE);
        pcNightLife.setVisualIdPrefix("60125");
        pcNightLife.getAgeBandsToConfigMaps().put("Adult", createConfigMap("60125", "10151570", 29.95));
        pcNightLife.getAgeBandsToConfigMaps().put("Senior", createConfigMap("60125", "10151570", 29.95));
        codesToProductConfigs.put(pcNightLife.getProductCode(), pcNightLife);

        //@todo externalize
        productCodesToEventTypeNames.put("5072CAS", "GMC - General Admission");
        productCodesToEventTypeNames.put("5072BTS", "Skip the line:  California Academy of Sciences Behind-the-Scenes Tour");
        productCodesToEventTypeNames.put("5072NIGHT", "NightLife");
        //productCodesToEventTypeNames.put("5072CAS", "Skip the line:  California Academy of Sciences General Admission Ticket");

        productCodesToEventNames.put("5072CAS", "GMC - General Admission");
        productCodesToEventNames.put("5072NIGHT", "NightLife");
        productCodesToPlus.put("5072CAS", null);
        productCodesToPlus.put("5072NIGHT", "10151570");

        productAgeCodesToPLUPrefixes.put("5072CAS-Adult", "60120");
        productAgeCodesToPLUPrefixes.put("5072CAS-Child", "60121");
        productAgeCodesToPLUPrefixes.put("5072CAS-Youth", "60122");
        productAgeCodesToPLUPrefixes.put("5072CAS-Student", "60123");
        productAgeCodesToPLUPrefixes.put("5072CAS-Senior", "60124");

        productAgeCodesToPLUPrefixes.put("5072NIGHT-Adult", "60125");
        productAgeCodesToPLUPrefixes.put("5072NIGHT-Child", "60125");
        productAgeCodesToPLUPrefixes.put("5072NIGHT-Youth", "60125");
        productAgeCodesToPLUPrefixes.put("5072NIGHT-Student", "60125");
        productAgeCodesToPLUPrefixes.put("5072NIGHT-Senior", "60125");

        //@todo externalize
        tourCodesToProductCodes.put("ACA1100", "5072BTS");
        tourCodesToProductCodes.put("ACA200", "5072BTS");
        tourCodesToProductCodes.put("AQU100", "5072BTS");
        tourCodesToProductCodes.put("AQU300", "5072BTS");
        tourCodesToProductCodes.put("SKLGA", "5072NIGHT");
        tourCodesToProductCodes.put("VIPEVE", "5072NIGHT");

        initializeVelocity();
    }

    private void initializeVelocity() {
        velocityEngine = new VelocityEngine();

        Properties p = new Properties();
        p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        p.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        p.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        p.setProperty("runtime.log.logsystem.log4j.category", "velocity");
        p.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
        //p.setProperty("runtime.log", env.getProperty("velocityLogLocation"));
        velocityEngine.init(p);
    }
    public Map<String, String> getProductAgeCodesToPLUPrefixes() {
        return productAgeCodesToPLUPrefixes;
    }

    public void setProductAgeCodesToPLUPrefixes(Map<String, String> productAgeCodesToPLUPrefixes) {
        this.productAgeCodesToPLUPrefixes = productAgeCodesToPLUPrefixes;
    }

    public Map<String, String> getProductCodesToEventTypeNames() {
        return productCodesToEventTypeNames;
    }

    public void setProductCodesToEventTypeNames(Map<String, String> productCodesToEventTypeNames) {
        this.productCodesToEventTypeNames = productCodesToEventTypeNames;
    }

    public Map<String, String> getProductCodesToEventNames() {
        return productCodesToEventNames;
    }

    public void setProductCodesToEventNames(Map<String, String> productCodesToEventNames) {
        this.productCodesToEventNames = productCodesToEventNames;
    }

    public Map<String, String> getProductCodesToPlus() {
        return productCodesToPlus;
    }

    public void setProductCodesToPlus(Map<String, String> productCodesToPlus) {
        this.productCodesToPlus = productCodesToPlus;
    }

    public Map<String, String> getTourCodesToProductCodes() {
        return tourCodesToProductCodes;
    }

    public void setTourCodesToProductCodes(Map<String, String> tourCodesToProductCodes) {
        this.tourCodesToProductCodes = tourCodesToProductCodes;
    }

    private Map createConfigMap(String visualIdPrefix, String plu, Double price) {
        Map map = new HashMap();
        map.put("visualIdPrefix", visualIdPrefix);
        map.put("plu", plu);
        map.put("price", price);
        return map;
    }
    @Override
    public void afterPropertiesSet() throws Exception {
        //supplierId = env.getProperty("supplierId");
        //apiKey = env.getProperty("apiKey");
        tGalaxyTicketCreator = new Thread(galaxyTicketCreator);
        tGalaxyTicketCreator.start();
    }

    private boolean requestOptionsMatch(Event event, String optionCode, String optionName) {
        if (optionCode == null)
            return true;

        String productCode = tourCodesToProductCodes.get(optionCode);
        if (productCode != null) {
            String eventTypeName = productCodesToEventTypeNames.get(productCode);
            if (eventTypeName.equals(event.getEventName()))
                return true;
        }
        return false;
    }

    private List getOptionMatchingTours(List tours, String optionCode, String optionName) {
        List returnTours = new ArrayList();

        for (int i = 0; i < tours.size(); i++) {
            ViatorTour viatorTour = (ViatorTour) tours.get(i);
            Event event = viatorTour.getEvent();

            //filter down to just the events that match the provided option code and name
            if (requestOptionsMatch(event, optionCode, optionName))
                returnTours.add(viatorTour);
        }
        return returnTours;
    }

    private boolean checkForHoliday(Date startDate) {
        return false;
    }

    private XMLGregorianCalendar getDateOnlyXmlTimestamp(Date in) {
        Calendar cal = new GregorianCalendar();
        cal.setTime(in);
        try {
            XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED);
            return xmlDate;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private XMLGregorianCalendar getXmlTimestamp(Date in) {
        GregorianCalendar cal = new GregorianCalendar();
        if (in != null)
            cal.setTime(in);

        XMLGregorianCalendar xmlTimestamp = null;
        try {
            xmlTimestamp = DatatypeFactory.newInstance().newXMLGregorianCalendar(cal);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return xmlTimestamp;
    }

    @RequestMapping(value="/Activate", method=RequestMethod.GET)
    public void activate(ServletRequest request) {
        ViatorSession viatorSession = new ViatorSession();
        viatorSession.setServiceGalaxy(serviceGalaxy);

        List bookings = serviceBooking.fetchAllBookings();
        List ticketsToActivate;

        for (int i = 0; i < bookings.size(); i++) {
            ticketsToActivate = new ArrayList();
            Booking booking = (Booking) bookings.get(i);
            if (booking.getLastAction() != null) {
                Set tickets = booking.getLastAction().getTickets();
                for (Iterator iterator = tickets.iterator(); iterator.hasNext(); ) {
                    Ticket next = (Ticket) iterator.next();
                    if (next.getStatus().equals(Ticket.STATUS_CREATED)) {
                        ticketsToActivate.add(next);
                    }
                }

                ResponseActivateTickets responseActivateTickets = serviceGalaxy.apiEventTicketsActivate(viatorSession.getSession(), ticketsToActivate);
                serviceBooking.updateBooking(booking);
            }
        }
    }

    private boolean allAgeBandsValid(AvailabilityRequest request, String productCode) {
        TravellerMix travellerMix = request.getTravellerMix();
        ProductConfig productConfig = (ProductConfig)codesToProductConfigs.get(request.getSupplierProductCode());

        if (travellerMix.getAdult() > 0 && productConfig.getAgeBandsToConfigMaps().get("Adult") == null)
            return false;
        if (travellerMix.getChild() > 0 && productConfig.getAgeBandsToConfigMaps().get("Child") == null)
            return false;
        if (travellerMix.getYouth() > 0 && productConfig.getAgeBandsToConfigMaps().get("Youth") == null)
            return false;
        if (travellerMix.getSenior() > 0 && productConfig.getAgeBandsToConfigMaps().get("Senior") == null)
            return false;
        if (travellerMix.getInfant() > 0 && productConfig.getAgeBandsToConfigMaps().get("Infant") == null)
            return false;
        return true;
    }

    @RequestMapping(value="/Availability", method=RequestMethod.POST)
    @ResponseBody
    public AvailabilityResponse availability(@RequestBody AvailabilityRequest request) {
        Date start = new Date();

        String viatorSessionId = request.getExternalReference();

        log.info(request.getExternalReference() + ": availability request received");

        AvailabilityResponse response = new AvailabilityResponse();
        response.setSupplierProductCode(request.getSupplierProductCode());
        response.setSupplierId(request.getSupplierId());
        response.setResellerId(request.getResellerId());
        response.setApiKey(apiKey);
        response.setExternalReference(viatorSessionId);

        RequestStatus requestStatus = new RequestStatus();
        requestStatus.setStatus(RequestStatusType.SUCCESS);
        response.setRequestStatus(requestStatus);
        response.setTimestamp(getXmlTimestamp(null));

        Date endDate = null;
        Date startDate = request.getStartDate().toGregorianCalendar().getTime();
        if (request.getEndDate() == null) {
            Calendar cal = request.getStartDate().toGregorianCalendar();
            cal.add(Calendar.DATE, 1);
            cal.add(Calendar.HOUR, -1);
            endDate = cal.getTime();
        } else
            endDate = request.getEndDate().toGregorianCalendar().getTime();

        //set the endDate to the end of the specified date
        Date morningMidnight = DateUtils.truncate(endDate, Calendar.DATE);
        Date midnightTonight = DateUtils.addHours(morningMidnight, 24);
        Date almostMidnight = DateUtils.addMinutes(midnightTonight, -1);

        //create a new Viator session for this request and store it in keysToSessions under the external reference id
        //that Viator generated (found in request.getExternalReference)
        ViatorSession viatorSession = new ViatorSession();
        viatorSession.setServiceGalaxy(serviceGalaxy);
        keysToSessions.put(viatorSessionId, viatorSession);

        //viator2 will send over a product code (which we are mapping to an event type name)
        //as well as (potentially) tour options that represent specific permutations (such as
        //a spanish language option, or the tour at 9am)
        log.info(request.getExternalReference() + ": searching for product code " + request.getSupplierProductCode());

        String eventTypeName = productCodesToEventTypeNames.get(request.getSupplierProductCode());

        if (eventTypeName == null) {
            log.warn(request.getExternalReference() + ": event type was null, returning error");

            Error error = new Error();
            error.setErrorCode("AV004");
            error.setErrorDetails("Supplier product code was not provided or wrong.");
            error.setErrorMessage("Availability Error");

            requestStatus.setStatus(RequestStatusType.ERROR);
            requestStatus.setError(error);
            return response;
        }

        //assumption is that tour options must be ORs, otherwise it wouldn't really make sense to support
        //multiple in a single request
        String optionCode = null;
        String optionName = null;
        if (request.getTourOptions() != null) {
            optionCode = request.getTourOptions().getSupplierOptionCode();
            optionName = request.getTourOptions().getSupplierOptionName();
            log.warn( request.getExternalReference() + ": event type was null, returning error");
        }

        List optionMatchingTours = null;
        //determine hold length
        Integer holdLength = null;
        AvailabilityHold availabilityHold = request.getAvailabilityHold();
        if (availabilityHold != null)
            holdLength = availabilityHold.getExpiry().getSeconds();

        //we send in a quantity of 0 just so we get all tours back and can use this information
        //to produce a useful response (support caching on the Viator side, presumably)
        int totalTravellersRequested = request.getTravellerMix().getTotal();
        ViatorAvailabilityRequest viatorAvailabilityRequest = viatorSession.getTours(eventTypeName, startDate, almostMidnight, totalTravellersRequested, request);
        log.info(request.getExternalReference() + ": " + viatorAvailabilityRequest.getTours().size() + "tours loaded");

        List permittedTours = new ArrayList();
        for (int i = 0; i < viatorAvailabilityRequest.getTours().size(); i++) {
            ViatorTour viatorTour = viatorAvailabilityRequest.getTours().get(i);
            if (permittedTourNames.contains(viatorTour.getEvent().getEventName())) {
                log.info(request.getExternalReference() + ": adding tour " + viatorTour);
                permittedTours.add(viatorTour);
            }
        }

        //filter down to just the option matching tours if a TourOptions object was passed in
        if (request.getTourOptions() != null)
            optionMatchingTours = getOptionMatchingTours(permittedTours, optionCode, optionName);
        else
            optionMatchingTours = viatorAvailabilityRequest.getTours();

        log.info(request.getExternalReference() + ": " + optionMatchingTours.size() + " matching tours found");
        //set the filtered tours list and then apply holds (if necessary) to them
        viatorAvailabilityRequest.setFilteredTours(optionMatchingTours);
        viatorAvailabilityRequest.applyHoldsToFilteredTours();

        //store references to any holds that were generated so that they can be looked up later (in
        //the case of a Booking request)
        for (int i = 0; i < optionMatchingTours.size(); i++) {

            ViatorTour viatorTour = (ViatorTour) optionMatchingTours.get(i);
            log.info( request.getExternalReference() + ": storing hold key for tour " + viatorTour.getTourCode());

            Event event = viatorTour.getEvent();
            EventTicketHoldResponse eventTicketHoldResponse = event.getEventTicketHoldResponse();
            if (eventTicketHoldResponse != null) {
                keysToHolds.put(eventTicketHoldResponse.getCapacityId(), eventTicketHoldResponse);
            }
        }

        Map toursByDate = getToursByDate(startDate, endDate, optionMatchingTours);
        log.info(request.getExternalReference() + ": tours mapped by date");

        List dateKeys = (List)toursByDate.get("keys");
        for (int i = 0; i < dateKeys.size(); i++) {
            Date dateKey = (Date) dateKeys.get(i);

            List toursForDate = (List)toursByDate.get(dateKey);
            TourAvailability tourAvailability = new TourAvailability();
            AvailabilityStatus availabilityStatus = new AvailabilityStatus();

            Calendar cal = new GregorianCalendar();
            cal.setTime(dateKey);
            try {
                XMLGregorianCalendar xmlDate = DatatypeFactory.newInstance().newXMLGregorianCalendarDate(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1, cal.get(Calendar.DAY_OF_MONTH), DatatypeConstants.FIELD_UNDEFINED);
                tourAvailability.setDate(xmlDate);
            } catch (Exception e) {
                e.printStackTrace();
            }

            //if the date in question is before today, just send back that it's past the cutoff date
            Date morningMidnightDate = DateUtils.truncate(dateKey, Calendar.DATE);
            Date morningMidnightToday = DateUtils.truncate(new Date(), Calendar.DATE);
            if (toursForDate.size() == 0) {
                log.info(request.getExternalReference() + ": no tour for date " + morningMidnightDate.toString());

                availabilityStatus.setStatus(AvailabilityStatusType.UNAVAILABLE);
                availabilityStatus.setUnavailabilityReason(UnavailabilityReason.NO_EVENT);
                tourAvailability.setAvailabilityStatus(availabilityStatus);
                response.getTourAvailability().add(tourAvailability);
            } else if (morningMidnightDate.before(morningMidnightToday)) {
                log.info(request.getExternalReference() + ": tour found but in past for date " + morningMidnightDate.toString());

                availabilityStatus.setStatus(AvailabilityStatusType.UNAVAILABLE);
                availabilityStatus.setUnavailabilityReason(UnavailabilityReason.PAST_CUTOFF_DATE);
                tourAvailability.setAvailabilityStatus(availabilityStatus);
                response.getTourAvailability().add(tourAvailability);
            } else {
                log.info(request.getExternalReference() + ": one or more tours found for date " + morningMidnightDate.toString());

                for (int j = 0; j < toursForDate.size(); j++) {
                    ViatorTour viatorTour = (ViatorTour) toursForDate.get(j);
                    Event event = viatorTour.getEvent();

                    try {
                        //get the tour duration as an xml element
                        Duration duration = DatatypeFactory.newInstance().newDuration(event.getDurationInSeconds() * 1000);

                        //grab the tour date from the depatureTime, just truncate to the day
                        tourAvailability.setDate(getDateOnlyXmlTimestamp(event.getStartDateTime()));

                        //we probably only need to set a TourOptions object if one is also on the request
                        if (request.getTourOptions() != null) {
                            //@todo check to see if there are any options for general admission
                            //TourOptions tourOptions = new TourOptions();
                            //tourOptions.setSupplierOptionCode(optionCode);
                            //tourOptions.setSupplierOptionName(optionName);
                            //tourOptions.setTourDepartureTime(getXmlTimestamp(event.getStartDateTime()));
                            //tourOptions.setTourDuration(duration);

                            //@todo inquire about tour languages and language options (guide, audio, written)
                            //TourLanguage tourLanguage = new TourLanguage();
                            //tourLanguage.setLanguageCode("EN");
                            //tourOptions.setLanguage(tourLanguage);
                            //tourAvailability.setTourOptions(tourOptions);
                        }

                        TravellerMixAvailability travellerMixAvailability = new TravellerMixAvailability();
                        processAvailability(event, request, availabilityStatus, travellerMixAvailability);
                        tourAvailability.setAvailabilityStatus(availabilityStatus);

                        //if a hold was sent in we should have generated a hold request and have a capacity id
                        //to send back as the hold reference
                        if (availabilityHold != null) {
                            AvailabilityHoldResponse availabilityHoldResponse = new AvailabilityHoldResponse();
                            availabilityHoldResponse.setExpiry(availabilityHold.getExpiry());
                            availabilityHoldResponse.setReference(event.getEventTicketHoldResponse().getCapacityId());
                            tourAvailability.setAvailabilityHold(availabilityHoldResponse);
                        }
                    } catch (DatatypeConfigurationException dce) {
                        log.error(request.getExternalReference() + ": data type configuration error: " + dce.getMessage());
                    }
                    response.getTourAvailability().add(tourAvailability);
                }
            }
        }

        Date end = new Date();
        sendTimeoutEmail(start, end, "Availability");
        return response;
    }

    private Map getToursByDate(Date startDate, Date endDate, List optionMatchingTours) {
        Date startDateTruncated = DateUtils.truncate(startDate, Calendar.DATE);
        Date endDateTruncated = DateUtils.truncate(endDate, Calendar.DATE);

        Map mapToursByDate = new HashMap();
        for (int i = 0; i < optionMatchingTours.size(); i++) {
            ViatorTour viatorTour = (ViatorTour) optionMatchingTours.get(i);
            Date eventDateTruncated = DateUtils.truncate(viatorTour.getEvent().getStartDateTime(), Calendar.DATE);

            List tours = (List)mapToursByDate.get(eventDateTruncated);
            if (tours == null) {
                tours = new ArrayList();
                mapToursByDate.put(eventDateTruncated, tours);
            }
            tours.add(viatorTour);
        }

        List keys = new ArrayList();
        Date tempDate = startDateTruncated;
        addTourListForDate(mapToursByDate, tempDate, keys);
        tempDate = DateUtils.addDays(tempDate, 1);

        while (!tempDate.after(endDateTruncated)) {
            addTourListForDate(mapToursByDate, tempDate, keys);
            tempDate = DateUtils.addDays(tempDate, 1);
        }
        mapToursByDate.put("keys", keys);
        return mapToursByDate;
    }

    private void addTourListForDate(Map mapToursByDate, Date tempDate, List keys) {
        List tours = (List)mapToursByDate.get(tempDate);
        if (tours == null) {
            tours = new ArrayList();
            mapToursByDate.put(tempDate, tours);
        }
        keys.add(tempDate);
    }

    private void processAvailability(Event event, AvailabilityRequest request, AvailabilityStatus availabilityStatus, TravellerMixAvailability travellerMixAvailability) {
        int available = event.getAvailable();
        TravellerMix travellerMix = request.getTravellerMix();
        boolean allAvailable = true;

        if (!allAgeBandsValid(request, request.getSupplierProductCode())) {
            availabilityStatus.setStatus(AvailabilityStatusType.UNAVAILABLE);
            availabilityStatus.setUnavailabilityReason(UnavailabilityReason.TRAVELLER_MISMATCH);
        } else {
            if (available >= travellerMix.getChild()) {
                travellerMixAvailability.setChild(true);
                available -= travellerMix.getChild();
            } else {
                allAvailable = false;
                travellerMixAvailability.setChild(false);
            }

            if (available >= travellerMix.getYouth()) {
                travellerMixAvailability.setYouth(true);
                available -= travellerMix.getYouth();
            } else {
                allAvailable = false;
                travellerMixAvailability.setYouth(false);
            }

            if (available >= travellerMix.getSenior()) {
                travellerMixAvailability.setSenior(true);
                available -= travellerMix.getSenior();
            } else {
                allAvailable = false;
                travellerMixAvailability.setSenior(false);
            }

            if (available >= travellerMix.getAdult()) {
                travellerMixAvailability.setAdult(true);
                available -= travellerMix.getAdult();
            } else {
                allAvailable = false;
                travellerMixAvailability.setAdult(false);
            }

            if (available >= travellerMix.getInfant()) {
                travellerMixAvailability.setInfant(true);
                available -= travellerMix.getInfant();
            } else {
                allAvailable = false;
                travellerMixAvailability.setInfant(false);
            }

            if (!allAvailable) {
                availabilityStatus.setStatus(AvailabilityStatusType.UNAVAILABLE);
                availabilityStatus.setUnavailabilityReason(UnavailabilityReason.SOLD_OUT);
            } else
                availabilityStatus.setStatus(AvailabilityStatusType.AVAILABLE);
        }
    }

    @RequestMapping(value="/BatchAvailability", method=RequestMethod.POST)
    @ResponseBody
    public BatchAvailabilityResponse batchAvailability(@RequestBody BatchAvailabilityRequest request) {
        BatchAvailabilityResponse response = new BatchAvailabilityResponse();
        response.setSupplierId(Integer.parseInt(supplierId));
        response.setResellerId(resellerId);
        response.setExternalReference(request.getExternalReference());
        return null;
    }

    private void commitHoldIfExists(BookingRequestBase request) {
        if (request instanceof BookingRequest) {
            BookingRequest bookingRequest = (BookingRequest)request;
            String keyHold = bookingRequest.getAvailabilityHoldReference();

            if (keyHold != null) {
                log.info("Committing hold for key " + keyHold);

                EventTicketHoldResponse eventTicketHoldResponse = (EventTicketHoldResponse) keysToHolds.get(bookingRequest.getAvailabilityHoldReference());
                //ViatorAvailabilityRequest viatorAvailabilityRequest = eventTicketHoldResponse.getViatorAvailabilityRequest();
                EventTicketCommitResponse eventTicketCommitResponse = serviceGalaxy.apiEventTicketCommit(eventTicketHoldResponse);
            } else {
                log.info("No hold for hold reference " + bookingRequest.getAvailabilityHoldReference());
            }
        }
    }

    private BookingResponseBase doBooking(BookingRequestBase request, BookingResponseBase response, Booking bookingRecord, String sessionId) {
        String keyBooking = request.getBookingReference();
        bookingRecord.setBookingReference(keyBooking);

        BookingTransactionStatus transactionStatus = new BookingTransactionStatus();
        RequestStatus requestStatus = new RequestStatus();

        //set up the basic elements of the response
        requestStatus.setStatus(RequestStatusType.SUCCESS);
        response.setRequestStatus(requestStatus);
        response.setBookingReference(keyBooking);
        response.setSupplierId(Integer.parseInt(supplierId));
        response.setResellerId(request.getResellerId());
        response.setApiKey(apiKey);
        response.setTimestamp(getXmlTimestamp(null));
        response.setExternalReference(request.getExternalReference());

        log.info(request.getBookingReference() + ": committing holds if they exist for this booking");
        //commit held tickets if a hold reference has been passed in
        commitHoldIfExists(request);

        ViatorSession viatorSession = new ViatorSession(sessionId);
        viatorSession.setServiceGalaxy(serviceGalaxy);

        //@todo make sure the booking record is being saved appropriately in this block

        Event requestedEvent = findRequestedEvent(request, sessionId);
        //if no matching event could be found, reject the booking request
        if (requestedEvent == null) {
            log.info(request.getBookingReference() + ": no event found, setting transaction status to rejected and rejection reason to not operating");

            transactionStatus.setStatus(TransactionStatusType.REJECTED);
            transactionStatus.setRejectionReason(BookingRejectionReason.NOT_OPERATING);

            //update our messaging for this booking
            bookingRecord.addToComments("Booking request rejected.  Attempted new booking, event for " + request.getSupplierProductCode() + " not found.");
            serviceBooking.updateBooking(bookingRecord);

            if (response instanceof BookingResponse) {
                log.info(request.getBookingReference() + ": response type is BookingResponse, setting confirmation number to null");

                BookingResponse bookingResponse = (BookingResponse)response;
                bookingResponse.setTransactionStatus(transactionStatus);
                bookingResponse.setSupplierConfirmationNumber(null);
            } else if (response instanceof BookingAmendmentResponse) {
                log.info(request.getBookingReference() + ": response type is BookingAmendmentResponse, setting confirmation number to null");

                BookingAmendmentResponse bookingAmendmentResponse = (BookingAmendmentResponse)response;
                bookingAmendmentResponse.setTransactionStatus(transactionStatus);
                bookingAmendmentResponse.setSupplierConfirmationNumber(null);
            }
            return response;
        }

        //if we have an event, generate some tickets
        if (requestedEvent != null) {
            log.info(request.getBookingReference() + ": event was found, generating tickets");

            List tickets = generateTickets(requestedEvent, request.getSupplierProductCode(), request, viatorSession);
            //response.setSupplierCommentCustomer();

            boolean ticketsActivated = true;

            //check to see if any of the tickets failed activation
            for (int i = 0; i < tickets.size(); i++) {
                log.info(request.getBookingReference() + ": setting ticket " + i + " status to activated");

                Ticket ticket = (Ticket) tickets.get(i);
                ticket.setEventId(requestedEvent.getEventID());
                if (!ticket.getStatus().equals(Ticket.STATUS_ACTIVATED))
                    ticketsActivated = false;
            }

            if (ticketsActivated) {
                Map<String, Integer> ageBandCounts = new HashMap<String, Integer>();

                BookingAction bookingAction = new BookingAction();
                bookingAction.generateConfirmation();
                bookingAction.setProductCode(request.getSupplierProductCode());
                bookingAction.setEventId((long)requestedEvent.getEventID());

                bookingRecord.addAction(bookingAction);
                bookingRecord.addToComments("Booking request completed.  Confirmation is " + bookingAction.getConfirmation());
                log.info(request.getBookingReference() + ": ticket creation and activation completed, generated confirmation is " + bookingAction.getConfirmation());

                //if ticket activation was successful
                transactionStatus.setStatus(TransactionStatusType.CONFIRMED);
                if (response instanceof BookingResponse) {
                    ((BookingResponse) response).setTransactionStatus(transactionStatus);
                    ((BookingResponse) response).setSupplierConfirmationNumber(bookingAction.getConfirmation());
                }
                else {
                    ((BookingAmendmentResponse) response).setTransactionStatus(transactionStatus);
                    ((BookingAmendmentResponse) response).setSupplierConfirmationNumber(bookingAction.getConfirmation());
                }
                bookingAction.setStatus(BookingAction.STATUS_CONFIRMED);

                for (int i = 0; i < tickets.size(); i++) {
                    Ticket ticket = (Ticket) tickets.get(i);
                    bookingAction.addTicket(ticket);
                    log.info(request.getBookingReference() + ": building response, adding traveler " + ticket.getTravellerIdentifier() + ", " + ticket.getVisualId());
                    ViatorUtils.addToAgeBand(ticket.getAgeBand(), ageBandCounts);

                    BookingResponse.Traveller responseTraveller = new BookingResponse.Traveller();
                    responseTraveller.setTravellerIdentifier(ticket.getTravellerIdentifier());
                    responseTraveller.setTravellerBarcode(ticket.getVisualId());
                    responseTraveller.setTravellerSupplierConfirmationNumber(bookingAction.getConfirmation());
                    response.getTraveller().add(responseTraveller);
                }

                //generate a summary string of the traveller types for this transaction
                String ageBandSummaryString = ViatorUtils.getAgeBandSummaryString(ageBandCounts);
                if (ageBandSummaryString.length() > 0)
                    response.setSupplierCommentCustomer(ageBandSummaryString);
            } else {
                log.info(request.getBookingReference() + ": building rejection response, probably an error in foreign media ticket activation");

                transactionStatus.setStatus(TransactionStatusType.REJECTED);
                response.setBookingReference(keyBooking);
                response.setSupplierId(Integer.parseInt(supplierId));
                response.setResellerId(resellerId);
                response.setExternalReference(request.getExternalReference());
                bookingRecord.addToComments("Booking request rejected.  Error in foreign media ticket activation");

                if (response instanceof BookingResponse) {
                    ((BookingResponse)response).setSupplierConfirmationNumber("");
                    ((BookingResponse)response).setTransactionStatus(transactionStatus);

                } else {
                    ((BookingAmendmentResponse)response).setSupplierConfirmationNumber("");
                    ((BookingAmendmentResponse)response).setTransactionStatus(transactionStatus);
                }
                transactionStatus.setRejectionReason(BookingRejectionReason.OTHER);
            }
        }

        log.info(request.getBookingReference() + ": updating booking record");
        serviceBooking.updateBooking(bookingRecord);
        log.info(request.getBookingReference() + ": returning generated response");
        return response;
    }

    @RequestMapping(value="/Booking", method=RequestMethod.POST)
    @ResponseBody
    public BookingResponse booking(@RequestBody BookingRequest request) {
        Date start = new Date();
        BookingResponse response = new BookingResponse();

        //check to see if we already have a booking stored under this reference number
        Booking bookingRecord = serviceBooking.getBookingByBookingReference(request.getBookingReference());
        if (bookingRecord == null) {
            log.info("Record for booking reference " + request.getBookingReference() + " not found, creating new booking");
            bookingRecord = new Booking();
        }
        else {
            log.warn("Record for booking reference " + request.getBookingReference() + " found, we should not be receiving repeat booking requests");
        }

        //if we didn't get an error, go ahead and proceed to do our booking
        String sessionId = serviceGalaxy.getSessionId(null);
        doBooking(request, response, bookingRecord, sessionId);
        runAbandonSessionImmediate(sessionId);

        try {
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext = JAXBContext.newInstance(BookingResponse.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(response, writer);
            log.info("xml response:\n" + writer.toString());
        } catch (Exception e) {
            log.info(e.getMessage());
        }
        Date end = new Date();
        sendTimeoutEmail(start, end, "Booking");
        return response;
    }

    public void runAbandonSessionImmediate(String sessionId) {
        if (abandonSessionImmediate) {
            RunAbandonSession runAbandonSession = new RunAbandonSession();
            runAbandonSession.setServiceGalaxy(serviceGalaxy);
            runAbandonSession.setSessionId(sessionId);

            Thread t = new Thread(runAbandonSession);
            t.start();
        }
    }

    public void sendTimeoutEmail(Date start, Date end, String transaction) {
        long msStart = start.getTime();
        long msEnd = end.getTime();
        long ms = msEnd - msStart;
        if (ms > emailTransactionTimeout) {
            Runnable timeoutEmailThread = new RunSendTimeoutEmail(start, end, transaction, emailTransactionTimeout, env, serviceEmail, sdfDateTime, velocityEngine);
            Thread t = new Thread(timeoutEmailThread);
            t.start();
        }
    }

    private Event findRequestedEvent(BookingRequestBase bookingRequest, String sessionId) {
        String eventTypeName = productCodesToEventTypeNames.get(bookingRequest.getSupplierProductCode());
        String eventName = productCodesToEventNames.get(bookingRequest.getSupplierProductCode());
        String plu = productCodesToPlus.get(bookingRequest.getSupplierProductCode());

        Date dateTravel = bookingRequest.getTravelDate().toGregorianCalendar().getTime();
        GregorianCalendar calEnd = bookingRequest.getTravelDate().toGregorianCalendar();
        calEnd.add(Calendar.DATE, 1);
        //calEnd.add(Calendar.HOUR, -1);
        Date dateTravelEnd = calEnd.getTime();

        log.info("Searching for events between travel dates " + dateTravel + " and " + dateTravelEnd + " with event name " + eventTypeName + " and plu " + plu);
        //this first call will get us all events of the given type for the day
        List events = serviceGalaxy.getEventsForDateRange(dateTravel, dateTravelEnd, null, eventTypeName, eventName, plu, 0, sessionId, true);
        log.info(events.size() + " events found");

        //if the request has tour options we'll have to match on tour departure time as well, but if we only have
        //a single event returned AND we have no tour options, we can just use that event
        if (events.size() == 1) {
            log.info("returning single event");

            return (Event)events.get(0);
        } else if (events.size() > 1 && bookingRequest.getTourOptions() != null) {
            log.info("more than one event, and tour options are not null, seeking correct event by departure time");

            Date tourStarts = bookingRequest.getTourOptions().getTourDepartureTime().toGregorianCalendar().getTime();
            for (int i = 0; i < events.size(); i++) {
                Event event = (Event) events.get(i);
                if (event.getStartDateTime().equals(tourStarts)) {
                    log.info("found matching event for tour start time of " + tourStarts);
                    return event;
                }
            }
        }
        log.info("no event found, returning null");
        return null;
    }

    private List generateTickets(Event requestedEvent, String productCode, BookingRequestBase bookingRequest, ViatorSession session) {
        List travellers = bookingRequest.getTraveller();
        List tickets = new ArrayList();
        Double payment = 0.0;

        for (int i = 0; i < travellers.size(); i++) {
            Traveller traveller =  (Traveller)travellers.get(i);
            AgeBandType ageBandType = traveller.getAgeBand();

            Ticket ticket = new Ticket();
            ticket.setTravellerIdentifier(traveller.getTravellerIdentifier());
            ticket.setAgeBand(traveller.getAgeBand().value());
            ticket.setGivenName(traveller.getGivenName());
            ticket.setSurname(traveller.getSurname());

            ProductConfig productConfig = (ProductConfig)codesToProductConfigs.get(productCode);
            Map configMap = null;

            ticket.setVisualId(null);
            if (ageBandType == AgeBandType.ADULT) {
                configMap = (Map)productConfig.getAgeBandsToConfigMaps().get("Adult");
            } else if (ageBandType == AgeBandType.CHILD) {
                configMap = (Map)productConfig.getAgeBandsToConfigMaps().get("Child");
            } else if (ageBandType == AgeBandType.YOUTH) {
                configMap = (Map)productConfig.getAgeBandsToConfigMaps().get("Youth");
            } else if (ageBandType == AgeBandType.SENIOR) {
                configMap = (Map)productConfig.getAgeBandsToConfigMaps().get("Senior");
            }

            if (configMap != null) {
                ticket.setPlu((String) configMap.get("plu"));
                ticket.setPrice((Double) configMap.get("price"));
                payment += ticket.getPrice();
            }

            tickets.add(ticket);
        }

        ResponseCreateTickets responseCreateTickets = serviceGalaxy.apiEventTicketsCreate(requestedEvent, session.getSession(), tickets, payment);
        //ResponseActivateTickets responseActivateTickets = serviceGalaxy.apiEventTicketsActivate(session.getSession(), tickets);
        return tickets;
    }

    private ResponseSimple doBookingCancel(Booking bookingRecord, String sessionId) {
        log.info("Looked up event by id " +  bookingRecord.getLastAction().getEventId());

        sessionId = serviceGalaxy.getSessionId(sessionId);
        List events = serviceGalaxy.getEventById(bookingRecord.getLastAction().getEventId(), sessionId);

        ResponseSimple responseSimple = null;

        if (bookingRecord != null && events.size() == 1) {
            Set<Ticket> tickets = bookingRecord.getLastAction().getTickets();
            List<Ticket> list = new ArrayList();
            list.addAll(tickets);

            responseSimple = serviceGalaxy.apiEventTicketsCancel((Event)events.get(0), sessionId, list);
        } else {
            if (events.size() != 1)
                log.info("Unexpected number of events returned,  " + events.size() + " returned");
            else if (bookingRecord == null)
                log.info("Unexpected request, booking record was null, but doBookingCancel called anyway");
        }

        runAbandonSessionImmediate(sessionId);
        log.info("Problem performing booking cancellation, returning null");
        return responseSimple;
    }

    @RequestMapping(value="/BookingCancel", method=RequestMethod.POST)
    @ResponseBody
    public BookingCancellationResponse bookingCancel(@RequestBody BookingCancellationRequest request) {
        Date start = new Date();
        BookingCancellationResponse response = new BookingCancellationResponse();
        response.setSupplierId(Integer.parseInt(supplierId));
        response.setResellerId(resellerId);
        response.setExternalReference(request.getExternalReference());
        response.setBookingReference(request.getBookingReference());
        response.setApiKey(apiKey);
        response.setTimestamp(getXmlTimestamp(null));
        response.setExternalReference(request.getExternalReference());

        ResponseSimple responseSimple = null;
        Booking bookingRecord = serviceBooking.getBookingByBookingReference(request.getBookingReference());

        if (bookingRecord == null)
            log.warn(request.getBookingReference() + ": booking cancellation called, no booking found");

        if (bookingRecord != null) {
            log.info(request.getBookingReference() + ": last action was " + bookingRecord.getLastAction());

            String sessionId = serviceGalaxy.getSessionId(null);
            BookingAction lastAction = bookingRecord.getLastAction();

            if (lastAction != null && (lastAction.getStatus().equals(BookingAction.STATUS_ACTIVATED) || lastAction.getStatus().equals(BookingAction.STATUS_CONFIRMED))) {
                log.info(request.getBookingReference() + ": last action was activated or confirmed, cancelling");

                responseSimple = doBookingCancel(bookingRecord, sessionId);

                if (responseSimple.getStatusCode().equals("0")) {
                    log.info(request.getBookingReference() + ": booking cancellation was successful, building response");

                    CancellationTransactionStatus cancellationTransactionStatus = new CancellationTransactionStatus();
                    cancellationTransactionStatus.setStatus(TransactionStatusType.CONFIRMED);
                    response.setTransactionStatus(cancellationTransactionStatus);

                    RequestStatus requestStatus = new RequestStatus();
                    requestStatus.setStatus(RequestStatusType.SUCCESS);
                    response.setRequestStatus(requestStatus);

                    bookingRecord.addToComments("Booking successfully canceled.");
                    BookingAction bookingAction = new BookingAction();
                    bookingAction.setStatus(BookingAction.STATUS_CANCELED);
                    bookingAction.transferTicketsFrom(lastAction);
                    bookingAction.generateConfirmation();
                    bookingRecord.addAction(bookingAction);
                    response.setSupplierConfirmationNumber(bookingAction.getConfirmation());

                    log.info(request.getBookingReference() + ": updating booking record with cancel action");
                    serviceBooking.updateBooking(bookingRecord);
                }
            //if the booking has already been cancelled then we can just ignore this call, as it's an error
            } else if (lastAction != null && lastAction.getStatus().equals(BookingAction.STATUS_CANCELED)) {
                log.warn(request.getBookingReference() + ": cancellation requested on already cancelled booking, building error response");

                CancellationTransactionStatus cancellationTransactionStatus = new CancellationTransactionStatus();
                cancellationTransactionStatus.setStatus(TransactionStatusType.REJECTED);
                cancellationTransactionStatus.setRejectionReason(CancellationRejectionReason.OTHER);
                response.setTransactionStatus(cancellationTransactionStatus);
                response.setSupplierConfirmationNumber("");

                RequestStatus requestStatus = new RequestStatus();
                requestStatus.setStatus(RequestStatusType.ERROR);
                response.setRequestStatus(requestStatus);

                log.warn( request.getBookingReference() + ": adding unexpected request to comments");
                bookingRecord.addToComments("Booking not canceled, last action already shows status = " + lastAction.getStatus() + ".");
                serviceBooking.updateBooking(bookingRecord);
            }

            runAbandonSessionImmediate(sessionId);
        }

        //if neither of the above cases set the transaction status, return an error (go through some use
        //cases to minimize this outcome)
        if (response.getTransactionStatus() == null) {
            log.warn( request.getBookingReference() + ": no transaction status set, building error response, cancellation probably failed at Galaxy web service cancel task");

            CancellationTransactionStatus cancellationTransactionStatus = new CancellationTransactionStatus();
            cancellationTransactionStatus.setStatus(TransactionStatusType.REJECTED);
            cancellationTransactionStatus.setRejectionReason(CancellationRejectionReason.OTHER);
            response.setTransactionStatus(cancellationTransactionStatus);
            response.setSupplierConfirmationNumber("");

            RequestStatus requestStatus = new RequestStatus();
            requestStatus.setStatus(RequestStatusType.ERROR);
            response.setRequestStatus(requestStatus);

            bookingRecord.addToComments("Booking not canceled, cancellation failed at Galaxy server (items may have been previously canceled, API server is out of sync");
            log.warn( request.getBookingReference() + ": updating booking comments with failure record");
            serviceBooking.updateBooking(bookingRecord);
        }

        Date end = new Date();
        sendTimeoutEmail(start, end, "BookingCancellation");
        return response;
    }

    @RequestMapping(value="/BookingAmendment", method=RequestMethod.POST)
    @ResponseBody
    public BookingAmendmentResponse bookingAmendment(@RequestBody BookingAmendmentRequest request) {
        Date start = new Date();

        BookingAmendmentResponse response = new BookingAmendmentResponse();
        response.setSupplierId(Integer.parseInt(supplierId));
        response.setResellerId(resellerId);
        response.setExternalReference(request.getExternalReference());

        String sessionId = serviceGalaxy.getSessionId(null);

        Booking bookingRecord = serviceBooking.getBookingByBookingReference(request.getBookingReference());
        log.info("Looked up booking record for cancellation, reference is " + request.getBookingReference() + ", record is : " + bookingRecord);
        ResponseSimple responseSimple = doBookingCancel(bookingRecord, sessionId);
        log.info("Booking cancellation response returned,  " + responseSimple);
        doBooking(request, response, bookingRecord, sessionId);
        log.info("Booking response returned,  " + response + ", returning as web service result");
        Date end = new Date();
        sendTimeoutEmail(start, end, "BookingAmendment");

        runAbandonSessionImmediate(sessionId);

        return response;
    }

    @RequestMapping(value="/BookingNotification", method=RequestMethod.POST)
    @ResponseBody
    public BookingNotificationResponse bookingNotification(@RequestBody BookingNotificationRequest request) {
        BookingNotificationResponse response = new BookingNotificationResponse();
        response.setSupplierId(Integer.parseInt(supplierId));
        response.setResellerId(resellerId);
        response.setExternalReference(request.getExternalReference());
        return null;
    }

    @RequestMapping(value="/TestMultipleSessions", method=RequestMethod.GET)
    public ModelMap testMultipleSessions(WebRequest webRequest) {
        ModelMap modelMap = new ModelMap();

        List sessions = new ArrayList();
        for (int i = 0; i < 5; i++) {
            ViatorSession viatorSession = new ViatorSession();
            viatorSession.setServiceGalaxy(serviceGalaxy);
            String sessionId = viatorSession.getSession();
            sessions.add(sessionId);
        }
        modelMap.addAttribute("sessions", sessions);
        return modelMap;
    }

    @RequestMapping(value="/TestAvailability", method=RequestMethod.GET)
    public ModelMap testAvailability(WebRequest webRequest) {
        ModelMap modelMap = new ModelMap();

        int quantity = 5;
        Date start = new Date();
        Date end = new Date(new Date().getTime() + (24 * 60 * 60 * 000));
        String eventTypeName = productCodesToEventTypeNames.get("5072CAS");
        String plu = null;
        List eventsFiltered = serviceGalaxy.getEventsForDateRange(start, end, null, eventTypeName, null, plu, quantity, null, true);
        List events = serviceGalaxy.getEventsForDateRange(start, end, null, null, null, plu, 5, null, true);

        Event event = (Event)events.get(8);
        Date holdExpires = new Date();
        holdExpires.setTime(new Date().getTime() + (300 * 1000));
        EventTicketHoldResponse eventTicketHoldResponse = serviceGalaxy.apiEventTicketHold(event, event.getSessionID(), event.getResourceID(), quantity);
        eventTicketHoldResponse.setExpires(holdExpires);

        EventTicketReleaseResponse eventTicketReleaseResponse = serviceGalaxy.apiEventTicketRelease(event, event.getSessionID(), eventTicketHoldResponse.getCapacityId(), event.getResourceID(), quantity);
        //@todo add jsp
        //modelMap.addAttribute("sessions", sessions);
        return modelMap;
    }
}
