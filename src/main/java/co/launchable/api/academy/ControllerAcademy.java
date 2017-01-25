package co.launchable.api.academy;

import co.launchable.api.jobs.JobStatusService;
import co.launchable.api.egalaxy.ServiceGalaxy;
import co.launchable.api.marketo.ReportableSynchronizationObject;
import co.launchable.api.marketo.ServiceMarketo;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mchange.v2.c3p0.PooledDataSource;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by michaelmcelligott on 1/28/14.
 */
@PropertySource("academy.properties")
@Controller
@RequestMapping("/academy")
public class ControllerAcademy {
    @Autowired
    Environment env;

    com.mchange.v2.c3p0.ComboPooledDataSource dataSource;
    com.mchange.v2.c3p0.ComboPooledDataSource dataSourceContacts;

    @Autowired
    JobStatusService jobStatusService;

    @Autowired(required=false)
    ServiceMarketo serviceMarketo;

    @Autowired
    ServiceGalaxy serviceGalaxy;

    Logger log = Logger.getLogger(ControllerAcademy.class);

    private SimpleDateFormat sdfTimestamp = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public ComboPooledDataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(ComboPooledDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public ComboPooledDataSource getDataSourceContacts() {
        return dataSourceContacts;
    }

    public void setDataSourceContacts(ComboPooledDataSource dataSourceContacts) {
        this.dataSourceContacts = dataSourceContacts;
    }

    private boolean testReachabilityContactsDatabase() {
        boolean contactsDatabaseReachable;
        Connection con = null;
        try {
            con = dataSourceContacts.getConnection();
            contactsDatabaseReachable = !con.isClosed();
        } catch (SQLException sqle) {
            log.info("Error reaching contacts database, error was : " + sqle.getMessage());
            contactsDatabaseReachable = false;
        } finally {
            try {
                con.close();
            } catch (Exception e) {
                log.info("Error closing contacts database, error was : " + e.getMessage());
            }
        }
        return contactsDatabaseReachable;
    }

    @RequestMapping(value="/health", method=RequestMethod.GET)
    public ModelMap health(WebRequest webRequest) {
        ModelMap modelMap = new ModelMap();

        boolean galaxyWebServiceReachable = serviceGalaxy.testReachabilityGalaxyWebService();
        boolean galaxyDatabaseReachable = serviceGalaxy.testReachabilityGalaxyDatabase();
        boolean paymentWebServiceReachable = serviceGalaxy.testReachabilityPaymentProcessor();
        boolean contactsDatabaseReachable = testReachabilityContactsDatabase();

        PooledDataSource pds = (PooledDataSource)serviceGalaxy.getDataSource();

        int numConnections = -1, numConnectionsIdle = -1, numConnectionsBusy = -1, numConnectionsOrphaned = -1;
        try {
            numConnections = pds.getNumConnectionsAllUsers();
            numConnectionsIdle = pds.getNumIdleConnectionsAllUsers();
            numConnectionsBusy = pds.getNumBusyConnectionsAllUsers();
            numConnectionsOrphaned = pds.getNumUnclosedOrphanedConnectionsAllUsers();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }


        modelMap.put("webServiceReachable", "true");
        modelMap.put("galaxyWebServiceReachable", galaxyWebServiceReachable ? "true" : "false");
        modelMap.put("galaxyDatabaseReachable", galaxyDatabaseReachable ? "true" : "false");
        modelMap.put("paymentWebServiceReachable", paymentWebServiceReachable ? "true" : "false");
        modelMap.put("contactsDatabaseReachable", contactsDatabaseReachable ? "true" : "false");
        modelMap.put("numConnections", numConnections);
        modelMap.put("numConnectionsIdle", numConnectionsIdle);
        modelMap.put("numConnectionsBusy", numConnectionsBusy);
        modelMap.put("numConnectionsOrphaned", numConnectionsOrphaned);
        modelMap.put("galaxyWebServiceUrl", serviceGalaxy.getServerUrl());
        modelMap.put("galaxyDataSourceUrl", ((PooledDataSource) serviceGalaxy.getDataSource()).getDataSourceName());
        modelMap.put("contactsDataSourceUrl", ((PooledDataSource) getDataSourceContacts()).getDataSourceName());

        return modelMap;
    }

    @RequestMapping(value="/events/list", method=RequestMethod.GET)
    public ModelMap eventsList(WebRequest webRequest) {
        //@todo complete this
        return new ModelMap();
    }

    @RequestMapping(value="/apiTest", method= RequestMethod.GET)
    public ModelMap apiTest(WebRequest webRequest) {
        ModelMap modelMap = new ModelMap();
        return modelMap;
    }

    @RequestMapping(value="/registerContact", method= RequestMethod.GET)
    public String registerContact(WebRequest webRequest) {
        return "forward:/academy/registerNewsletterInterests";
    }

    @RequestMapping(value="/registerNewsletterInterests", method= RequestMethod.GET)
    public ModelMap registerNewsletterInterests(WebRequest webRequest) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("lastname", webRequest.getParameter("lastname"));
        map.put("firstname", webRequest.getParameter("firstname"));
        map.put("email", webRequest.getParameter("email"));
        map.put("phone", webRequest.getParameter("phone"));
        map.put("zip", webRequest.getParameter("zip"));
        map.put("source", webRequest.getParameter("source"));

        if (webRequest.getParameter("interests") != null)
            map.put("interests", webRequest.getParameter("interests"));

        List interestsRegistered = null;
        List interestsRequested = null;

        Long contactId = null;
        String result = null;
        String message = null;

        try {
            ApiSaveSubscriptionProfile actionSaveSubscriptionProfile = new ApiSaveSubscriptionProfile();
            actionSaveSubscriptionProfile.setDataSource(dataSource);
            actionSaveSubscriptionProfile.initFromEnvironment(env);
            actionSaveSubscriptionProfile.setParameterMap(map);
            actionSaveSubscriptionProfile.run();

            interestsRegistered = actionSaveSubscriptionProfile.getInterestsRegistered();
            interestsRequested = actionSaveSubscriptionProfile.getInterestsRequested();

            contactId = actionSaveSubscriptionProfile.getContactId();

            result = "success";
            message = interestsRegistered.size() + " interests registered";
        } catch (Exception e) {
            result = "failure";
            message = e.getMessage();
        }

        //launch the marketo synchronization for the contact in a new thread
        if (contactId != null) {
            final Long finalContactId = contactId;
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    serviceMarketo.syncSingleLeadAndInterests(finalContactId);
                }
            };
            new Thread(runnable).start();
        }


//        try {
//            Resty r = new Resty();
//            JSONResource jsonResource = r.json("http://japp1.prod.calacademy.org:8080/api/marketo/syncLeadAndInterests?contactId=" + contactId);
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }

        ModelMap modelMap = new ModelMap();
        modelMap.addAttribute("contactId", contactId);
        modelMap.addAttribute("interestsRegistered", interestsRegistered);
        modelMap.addAttribute("interestsRequested", interestsRequested);
        modelMap.addAttribute("result", result);
        modelMap.addAttribute("message", message);
        modelMap.addAttribute("callback", webRequest.getParameter("callback"));
        return modelMap;
    }

    @RequestMapping(value="/jobStatus", method= RequestMethod.GET)
    public ModelMap jobStatus(HttpServletRequest request) {
        ModelMap modelMap = new ModelMap();
        if (request.getParameter("uuid") != null) {
            int fullRowsProcessed = 0;
            int rowsToProcess = 10000000;
            List workers = jobStatusService.getWorkersForUUID(request.getParameter("uuid"));
            Date jobCreation = jobStatusService.getJobCreation(request.getParameter("uuid"));
            long secondsElapsed = (new Date().getTime()) - jobCreation.getTime();
            secondsElapsed = secondsElapsed / 1000;

            for (int i = 0; i < workers.size(); i++) {
                ReportableSynchronizationObject apiSyncObjectBase = (ReportableSynchronizationObject) workers.get(i);
                fullRowsProcessed += apiSyncObjectBase.getFullRowsProcessed();
                rowsToProcess = apiSyncObjectBase.getRowsToProcess() < rowsToProcess ? apiSyncObjectBase.getRowsToProcess() : rowsToProcess;
            }

            int percentageComplete = 0;
            if ((fullRowsProcessed + rowsToProcess) > 0)
                percentageComplete = (int)((100 * fullRowsProcessed) / (fullRowsProcessed + rowsToProcess));

            float rowsPerSecond = (float)fullRowsProcessed / (float)secondsElapsed;
            int secondsToGo = (int) ((float)rowsToProcess/(float)rowsPerSecond);

            modelMap.addAttribute("secondsElapsed", secondsElapsed);
            modelMap.addAttribute("secondsToGo", secondsToGo);
            modelMap.addAttribute("rowsProcessed", fullRowsProcessed);
            modelMap.addAttribute("rowsLeft", rowsToProcess);
            modelMap.addAttribute("percentageComplete", percentageComplete);
            modelMap.addAttribute("workers", workers);
            modelMap.addAttribute("uuid", request.getParameter("uuid"));
        }
        return modelMap;
    }

    @RequestMapping(value="/serverStatus", method= RequestMethod.GET)
    public ModelMap serverStatus(WebRequest webRequest) {
        ModelMap modelMap = new ModelMap();
        if (webRequest.getParameter("callback") != null)
            modelMap.addAttribute("callback", webRequest.getParameter("callback"));

        modelMap.addAttribute("serverStatus", serviceGalaxy.getServerStatus());
        return modelMap;
    }

    private String stringOrNull(WebRequest webRequest, String param) {
        String retValue = null;
        retValue = webRequest.getParameter(param);
        return "".equals(retValue) ? null : retValue;
    }

    private Integer integerOrNull(WebRequest webRequest, String param) {
        Integer retValue = null;
        try {
            retValue = Integer.parseInt(webRequest.getParameter(param));
        } catch (Exception e) {
            //ignore it
        }
        return retValue;
    }

    @RequestMapping(value="/getEventsForDateRange", method=RequestMethod.GET)
    public ModelMap eventsForDateRange(WebRequest webRequest) {
        ModelMap modelMap = new ModelMap();
        if (webRequest.getParameter("callback") != null)
            modelMap.addAttribute("callback", webRequest.getParameter("callback"));

        try {
            String sessionId = serviceGalaxy.getSessionId(null);

            Date dateStart = sdfTimestamp.parse(webRequest.getParameter("start"));
            Date dateEnd = sdfTimestamp.parse(webRequest.getParameter("end"));
            String resourceId = stringOrNull(webRequest, "resourceId");
            String eventTypeName = stringOrNull(webRequest, "eventTypeName");
            Integer minimumAvailability = integerOrNull(webRequest, "minimumAvailability");

            List events = serviceGalaxy.getEventsForDateRange(dateStart, dateEnd, resourceId, eventTypeName, null, null, minimumAvailability, sessionId, false);

            modelMap.addAttribute("result", "success");
            modelMap.addAttribute("message", events.size() + " events retrieved");
            modelMap.addAttribute("events", events);
        } catch (ParseException pe) {
            pe.printStackTrace();
            modelMap.addAttribute("result", "failure");
            modelMap.addAttribute("message", pe.getMessage());
        }
        return modelMap;
    }
}
