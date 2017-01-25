package co.launchable.api.marketo;

import co.launchable.api.DbUtils;
import co.launchable.api.jobs.JobStatusService;
import co.launchable.api.email.ServiceEmail;
import co.launchable.api.util.CommandLineCapture;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;

import javax.sql.DataSource;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by michaelmcelligott on 2/26/14.
 */
@PropertySource("co.launchable.api.marketo.marketo.properties")
@Service
public class ServiceMarketo {
    @Autowired
    Environment env;

    @Autowired
    JobStatusService jobStatusService;

    DataSource dataSource;

    @Autowired
    ServiceEmail serviceEmail;

    @Autowired
    ApiGetDeletedLeads apiGetDeletedLeads;

    @Autowired
    ApiSyncUpdatedInterests apiSyncUpdatedInterests;


    @Autowired
    ApiMergeDuplicateLeads apiMergeDuplicateLeads;
    
    @Autowired
    ApiSyncOpportunities apiSyncOpportunities;
    
    
    private int WORKERS_VISITATIONS = 2;
    private int WORKERS_ORDERS = 2;
    private int WORKERS_ORDER_DETAILS = 2;
    private int WORKERS_CONSTITUENTS = 2;
    private int WORKERS_INTERESTS = 2;
    private int WORKERS_LEADS = 3;

    private VelocityEngine velocityEngine;
    private boolean velocityInitialized = false;
    private SimpleDateFormat sdf = new SimpleDateFormat("MM-dd-yyyy");
    private String errorCaptureCommand;
    Logger log = Logger.getLogger(ServiceMarketo.class);

    public String getErrorCaptureCommand() {
        return errorCaptureCommand;
    }

    public void setErrorCaptureCommand(String errorCaptureCommand) {
        this.errorCaptureCommand = errorCaptureCommand;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void calculateUpdatedLeads() {
        log.info("calculating updated leads");
        Connection con = null;
        try {
            log.info("executing sp_buildLeadProperties");
            con = dataSource.getConnection();
            con.createStatement().execute("exec sp_buildLeadProperties");
            //log.info("executing sp_buildInterestDelta");
            //con.createStatement().execute("exec sp_buildInterestDelta");
        } catch (SQLException sqle) {
            log.warn("error calculating updated leads, message was: " + sqle.getMessage());

            String recipients = env.getProperty("marketo.emailReportRecipients");
            String[] arrayRecipients = recipients.split(",");
            String subject = "[Marketo Integration] Error calculating updated leads";
            String text = "There was an error calculating which leads to update in the nightly batch process.  This generally indicates a database accessibility issue or a change in structure to the underlying tables.  The stored procedures called are sp_buildInterestDelta and sp_buildLeadProperties.";
            String error = null;

            if (errorCaptureCommand != null) {
                //log.info("capturing tail command");
                error = CommandLineCapture.captureCommand(errorCaptureCommand);
                if (error != null) {
                    text = text + "Recent log output:<br/>" + error;
                }
                //log.info("end capture");
            }
            serviceEmail.sendHtmlEmail(subject, arrayRecipients, text);
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void calculateDuplicates() {
        log.info("calculating duplicates");
        Connection con = null;
        try {
            log.info("executing sp_calculateDuplicates");
            con = dataSource.getConnection();
            con.createStatement().execute("exec sp_calculateDuplicates");
        } catch (SQLException sqle) {
            log.warn("error calculating duplicates, message was: " + sqle.getMessage());

            String recipients = env.getProperty("marketo.duplicatesReportRecipients");
            String[] arrayRecipients = recipients.split(",");
            String subject = "[Marketo Integration] Error calculating duplicates";
            String text = "There was an error calculating duplicate leads.  This generally indicates a database " +
                    "accessibility issue or a change in structure to the underlying tables.  The stored procedure " +
                    "called is sp_calculateDuplicates.";
            String error = null;

            if (errorCaptureCommand != null) {
                error = CommandLineCapture.captureCommand(errorCaptureCommand);
                if (error != null) {
                    text = text + "Recent log output:<br/>" + error;
                }
            }
            serviceEmail.sendHtmlEmail(subject, arrayRecipients, text);
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void testTailGrab() {
        log.info("testing tail grab");

        String recipients = env.getProperty("marketo.emailReportRecipients");
        String[] arrayRecipients = recipients.split(",");
        String subject = "[Marketo Integration] Error calculating updated leads";
        String text = "There was an error calculating which leads to update in the nightly batch process.  This generally indicates a database accessibility issue or a change in structure to the underlying tables.  The stored procedures called are sp_buildInterestDelta and sp_buildLeadProperties.";
        String error = null;

        if (errorCaptureCommand != null) {
            //log.info("capturing tail command");
            error = CommandLineCapture.captureCommand(errorCaptureCommand);
            if (error != null) {
                text = text + "Recent log output:<br/>" + error;
            }
            //log.info("end capture");
        }
        serviceEmail.sendHtmlEmail(subject, arrayRecipients, text);
    }

    public ModelMap syncPendingObjects(String propertyPrefix, String objectType, int numWorkers, boolean block) {
        log.info("executing syncPendingObjects with objectType = " + objectType + ", numWorkers = " + numWorkers);

        ModelMap modelMap = new ModelMap();

        try {
            List workers = new ArrayList();

            String sqlGlobalPrepare = env.getProperty(propertyPrefix + "sqlGlobalPrepare");
            if (sqlGlobalPrepare != null) {
                Connection con = dataSource.getConnection();
                con.createStatement().executeUpdate(sqlGlobalPrepare);
                con.close();
            }

            List threads = new ArrayList();
            for (int i = 0; i < numWorkers; i++) {
                ApiSyncCustomObjectsKeysAndAttributes action =
                        new ApiSyncCustomObjectsKeysAndAttributes(propertyPrefix, objectType, env);
                action.setDataSource(dataSource);
                action.setWorkerIndex(i);
                action.setDelay(i * 1000);

                Thread t = new Thread(action);
                t.start();
                threads.add(t);

                workers.add(action);
                System.out.println("started worker [" + objectType + i + "]");
            }

            if (jobStatusService != null)
                modelMap.put("jobUUID", jobStatusService.addWorkerList(workers));

            if (block)
                blockUntilComplete(threads);

        } catch (Exception e) {
            modelMap.put("exception", e);
        }
        return modelMap;
    }

    public ModelMap syncPendingVisitations() {return syncPendingVisitations(null, true);}
    public ModelMap syncPendingConstituents() {return syncPendingConstituents(null, true);}
    public ModelMap syncPendingOrders() {return syncPendingOrders(null, true);}
    public ModelMap syncPendingOrderDetails() {return syncPendingOrderDetails(null, true);}
    public ModelMap syncPendingInterests() {return syncPendingInterests(null, true);}
    public ModelMap syncPendingEvents() {return syncPendingEvents(null, true);}
    public ModelMap syncPendingLeads() {return syncPendingLeads(null, true);}
    public ModelMap updateBadLeads() {return updateBadLeads(null, true);}
    public void syncPruneStatuses() {
        log.info("removing failures");
        Connection con = null;
        try {
            log.info("executing sp_removeFailures");
            con = dataSource.getConnection();
            con.createStatement().execute("exec sp_pruneStatuses");
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {

            if (con != null) {
                try {
                    con.close();
                } catch (Exception e) {
                    log.error(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public ModelMap syncPendingVisitations(Integer aWorkers, boolean block) {
        int numWorkers = aWorkers != null ? aWorkers : WORKERS_VISITATIONS;
        return syncPendingObjects("co.launchable.api.marketo.marketo.syncVisitations.", "Visitation", numWorkers, block);
    }

    public ModelMap syncPendingConstituents(Integer aWorkers, boolean block) {
        int numWorkers = aWorkers != null ? aWorkers : WORKERS_CONSTITUENTS;
        return syncPendingObjects("co.launchable.api.marketo.marketo.syncConstituents.", "Constituent", numWorkers, block);
    }

    public ModelMap syncPendingOrders(Integer aWorkers, boolean block) {
        int numWorkers = aWorkers != null ? aWorkers : WORKERS_ORDERS;
        return syncPendingObjects("co.launchable.api.marketo.marketo.syncOrders.", "Order", numWorkers, block);
    }

    public ModelMap syncPendingEvents(Integer aWorkers, boolean block) {
        int numWorkers = aWorkers != null ? aWorkers : WORKERS_ORDERS;
        return syncPendingObjects("co.launchable.api.marketo.marketo.syncEvents.", "Event", numWorkers, block);
    }

    public ModelMap syncPendingOrderDetails(Integer aWorkers, boolean block) {
        int numWorkers = aWorkers != null ? aWorkers : WORKERS_ORDER_DETAILS;
        return syncPendingObjects("co.launchable.api.marketo.marketo.syncOrderDetails.", "OrderDetail", numWorkers, block);
    }

    public ModelMap syncPendingInterests(Integer aWorkers, boolean block) {
        int numWorkers = aWorkers != null ? aWorkers : WORKERS_INTERESTS;
        return syncPendingObjects("co.launchable.api.marketo.marketo.syncInterests.", "Interest_In", numWorkers, block);
    }

    public ModelMap syncPendingLeads(Integer aWorkers, boolean block) {
        log.info("executing syncPendingLeads with numWorkers = " + aWorkers);
        int numWorkers = aWorkers != null ? aWorkers : WORKERS_LEADS;

        ModelMap modelMap = new ModelMap();
        List threads = new ArrayList();

        try {
            List workers = new ArrayList();

            for (int i = 0; i < numWorkers; i++) {
                ApiSyncMultipleLeads actionListImport = new ApiSyncMultipleLeads(env);
                actionListImport.setDataSource(dataSource);
                actionListImport.setWorkerIndex(i);
                actionListImport.setDelay(i * 1000);

                Thread t = new Thread(actionListImport);
                t.start();
                threads.add(t);

                workers.add(actionListImport);
            }

            if (jobStatusService != null)
                modelMap.put("jobUUID", jobStatusService.addWorkerList(workers));
        } catch (Exception e) {
            modelMap.put("exception", e);
        }

        if (block)
            blockUntilComplete(threads);

        return modelMap;
    }

    private void blockUntilComplete(List threads) {
        boolean allDead = false;

        while (!allDead) {
            allDead = true;
            for (int i = 0; i < threads.size(); i++) {
                Thread t = (Thread) threads.get(i);
                if (t.isAlive()) {
                    allDead = false;
                    break;
                }
            }
        }
    }

    public ModelMap updateBadLeads(Integer aWorkers, boolean block) {
        log.info("executing updateBadLeads with numWorkers = " + aWorkers);
        int numWorkers = aWorkers != null ? aWorkers : 10;

        ModelMap modelMap = new ModelMap();

        try {
            List workers = new ArrayList();

            for (int i = 0; i < numWorkers; i++) {
                ApiUpdateBadLeads actionListImport = new ApiUpdateBadLeads(env);
                actionListImport.setDataSource(dataSource);
                actionListImport.setWorkerIndex(i);
                actionListImport.setDelay(i * 1000);

                new Thread(actionListImport).start();
                workers.add(actionListImport);
            }
            if (jobStatusService != null)
                modelMap.put("jobUUID", jobStatusService.addWorkerList(workers));
        } catch (Exception e) {
            modelMap.put("exception", e);
        }
        return modelMap;
    }

    public ModelMap syncSingleLeadAndInterests(Long contactId) {
        log.info("executing syncSingleLeadAndInterests with contactId = " + contactId);

        Integer workerIndex = 100;
        ModelMap modelMap = new ModelMap();

        if (contactId != null) {
            try {
                //sync the lead first (setting the contactId will cause the API function to load special database statements
                //to only affect the specified contactid
                ApiSyncMultipleLeads actionListImport = new ApiSyncMultipleLeads(env);
                actionListImport.setContactId(contactId);
                actionListImport.setDataSource(dataSource);
                actionListImport.setWorkerIndex(workerIndex);
                actionListImport.execute();

                //sync the set of interests for the given contactId
                ApiSyncCustomInterests actionSyncInterests = new ApiSyncCustomInterests(env);
                actionSyncInterests.setDataSource(dataSource);
                actionSyncInterests.setContactId(contactId);
                actionSyncInterests.setWorkerIndex(workerIndex);
                actionSyncInterests.execute();

                modelMap.addAttribute("contactId", contactId);
                modelMap.addAttribute("result", "success");
                modelMap.addAttribute("message", "");

            } catch (Exception e) {
                modelMap.addAttribute("contactId", contactId);
                modelMap.addAttribute("result", "failure");
                modelMap.addAttribute("message", e.getMessage());
                e.printStackTrace();
            }
        } else {
            modelMap.addAttribute("contactId", "");
            modelMap.addAttribute("result", "failure");
            modelMap.addAttribute("message", "no contact id specified");
        }
        return modelMap;
    }

    public ModelMap syncPendingInterestsOld() {
        Date start = new Date();
        int rowsProcessed = 0;
        Exception exception = null;

        try {
            ApiSyncCustomInterests action = new ApiSyncCustomInterests(env);
            action.setDataSource(dataSource);
            rowsProcessed = action.execute();
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        Date end = new Date();

        long elapsed = end.getTime() - start.getTime();
        ModelMap modelMap = new ModelMap();
        modelMap.addAttribute("rowsProcessed", rowsProcessed);
        modelMap.addAttribute("exception", exception);
        modelMap.addAttribute("start", start);
        modelMap.addAttribute("end", end);
        modelMap.addAttribute("elapsed", elapsed);
        return modelMap;
    }

    private void initializeVelocity() {
        velocityEngine = new VelocityEngine();

        Properties p = new Properties();
        p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        p.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        p.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        p.setProperty("runtime.log.logsystem.log4j.category", "velocity");
        p.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
        p.setProperty("runtime.log", env.getProperty("velocityLogLocation"));
        velocityEngine.init(p);
        velocityInitialized = true;
    }

    public void sendEmailReport() {
        log.info("executing sendEmailReport");

        if (!velocityInitialized)
            initializeVelocity();

        String query = env.getProperty("marketo.emailReportSql");
        List rows = new ArrayList();

        try {
            String subject = "Synchronization Report, " + sdf.format(new Date());

            Connection con = dataSource.getConnection();
            DbUtils.loadObjectsFromDatabase(con, query, null, null, rows);
            VelocityContext vc = new VelocityContext();
            vc.put("rows", rows);
            vc.put("subject", subject);

            if (dataSource instanceof ComboPooledDataSource) {
                try {
                    ComboPooledDataSource cpds = (ComboPooledDataSource) dataSource;
                    vc.put("busyConnections", cpds.getNumBusyConnectionsAllUsers());
                    vc.put("allConnections", cpds.getNumConnectionsAllUsers());
                } catch (SQLException sqle) {
                    sqle.printStackTrace();
                }
            }

            //get the count of new leads available
            String sqlNewLeads = env.getProperty("marketo.emailReportLeads");
            ResultSet rs = con.createStatement().executeQuery(sqlNewLeads);
            if (rs.next()) {
                Map map = new HashMap();
                map.put("type", "New Lead");
                map.put("status", "created");
                map.put("total", rs.getInt(1));
                rows.add(map);
            }

            String sqlNewFailures = env.getProperty("marketo.emailReportNewFailures");
            ResultSet rsFailures = con.createStatement().executeQuery(sqlNewFailures);
            if (rsFailures.next()) {
                Map map = new HashMap();
                map.put("type", "New Failure");
                map.put("status", "items");
                map.put("total", rsFailures.getInt(1));
                rows.add(0, map);
            }

            StringWriter stringWriter = new StringWriter();
            Template t = velocityEngine.getTemplate("templates/co.launchable.api.marketo.marketo/tpl_emailSynchronizationReport.vtl");
            t.merge(vc, stringWriter);

            String recipients = env.getProperty("marketo.emailReportRecipients");
            String[] arrayRecipients = recipients.split(",");

            serviceEmail.sendHtmlEmail(subject, arrayRecipients, stringWriter.toString());

            con.close();
        } catch (Exception e) {
            log.error("error sending email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void sendDuplicatesReport() {
        log.info("executing sendDuplicatesReport");

        calculateDuplicates();

        if (!velocityInitialized)
            initializeVelocity();

        String query = env.getProperty("marketo.emailReportDuplicatesSql");
        List rows = new ArrayList();

        try {
            VelocityContext vc = new VelocityContext();

            String subject = "Duplicates Report, " + sdf.format(new Date());
            vc.put("subject", subject);

            Connection con = dataSource.getConnection();
            DbUtils.loadObjectsFromDatabase(con, query, null, null, rows);

            if (rows.size() > 0) {
                vc.put("rows", rows);

                StringWriter stringWriter = new StringWriter();
                Template t = velocityEngine.getTemplate("templates/co.launchable.api.marketo.marketo/tpl_emailDuplicatesReport.vtl");
                t.merge(vc, stringWriter);

                String recipients = env.getProperty("marketo.duplicatesReportRecipients");
                String[] arrayRecipients = recipients.split(",");

                serviceEmail.sendHtmlEmail(subject, arrayRecipients, stringWriter.toString());
            }

            con.close();
        } catch (Exception e) {
            log.error("error sending email : " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void executeDailyProcesses() {
        calculateUpdatedLeads();
        syncPendingLeads(null, true);
        syncPendingConstituents(null, true);
        syncPendingOrders(null, true);
        syncPendingOrderDetails(null, true);
        syncPendingVisitations(null, true);
        syncPendingEvents(null, true);
        apiSyncUpdatedInterests.execute();
        apiSyncOpportunities.execute();
        apiSyncOpportunities.linkFailedOpportunities();
        sendEmailReport();
    }

    public void executeWeeklyProcesses() {
        apiGetDeletedLeads.process();
        calculateDuplicates();
        calculateUpdatedLeads();
        syncPendingLeads(null, true);
        syncPendingConstituents(null, true);
        syncPendingOrders(null, true);
        syncPendingOrderDetails(null, true);
        syncPendingVisitations(null, true);
        syncPendingEvents(null, true);
        apiSyncUpdatedInterests.execute();
        apiMergeDuplicateLeads.execute();
        calculateUpdatedLeads();
        syncPendingLeads();
        syncPendingOrders();
        syncPendingOrderDetails();
        syncPendingVisitations();
        apiSyncOpportunities.execute();
        apiSyncOpportunities.linkFailedOpportunities();
        syncPruneStatuses();
        sendEmailReport();
    }
}
