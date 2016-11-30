package co.launchable.api.marketo;

import co.launchable.api.jobs.JobStatusService;
import co.launchable.api.util.QueryResult;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.sql.DataSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.*;
import java.util.Date;

/**
 * Created by michaelmcelligott on 1/15/14.
 */
@PropertySource("marketo.properties")
@Controller
@RequestMapping("/marketo")
public class ControllerMarketo {
    @Autowired
    Environment env;

    @Autowired
    DataSource dataSourceContacts;

    @Autowired
    ServiceMarketo serviceMarketo;

    @Autowired(required=false)
    ServletContext servletContext;

    @Autowired
    JobStatusService jobStatusService;

    @Autowired
    private ApplicationContext appContext;

    int WORKERS_CONSTITUENTS = 10;
    int WORKERS_VISITATIONS = 10;
    int WORKERS_ORDERS = 10;
    int WORKERS_LEADS = 10;

    //@todo convert to service usage
    Map uuidsToJobs = new HashMap();

    private String jobStatusUrlPrefix = "forward:/jobs/status";

    public DataSource getDataSource() {
        return dataSourceContacts;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSourceContacts = dataSourceContacts;
    }

    @RequestMapping(value="/importExistingLeads", method= RequestMethod.GET)
    public ModelMap importExistingLeads() {
        try {
            int count = 0;
            Connection con = dataSourceContacts.getConnection();
            BufferedReader reader = new BufferedReader(new InputStreamReader(servletContext.getResourceAsStream("/WEB-INF/leads.csv")));

            //BufferedReader reader = new BufferedReader(new FileReader(servletContext.getRealPath("/WEB-INF/leads.csv")));
            PreparedStatement pstmt = con.prepareStatement("insert into MarketoStatus (objectType, status, key1, key2, lastUpdated) values (?, ?, ?, ?, getdate())");
            String line = reader.readLine();
            while (line != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    pstmt.clearParameters();
                    pstmt.setString(1, "Lead");
                    pstmt.setString(2, "CREATED");
                    pstmt.setString(3, parts[1]);
                    pstmt.setString(4, parts[0]);
                    pstmt.executeUpdate();
                    count++;

                    if (count % 1000 == 0) {
                        System.out.print(count);
                        System.out.print(".");
                    }
                }
                line = reader.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ModelMap();
    }

    @RequestMapping(value="/importToList", method= RequestMethod.GET)
    public ModelMap baseHandler() {
        ApiImportToList actionListImport = new ApiImportToList();
        actionListImport.initFromEnvironment(env);
        boolean initializationSuccess = actionListImport.initialize();
        if (initializationSuccess) {
            int rowsProcessed = actionListImport.processAllRows();
        }
        return new ModelMap();
    }

    @RequestMapping(value="/syncInterests", method=RequestMethod.GET)
    public ModelMap handleSyncInterests() {
        Date start = new Date();
        int rowsProcessed = 0;
        Exception exception = null;

        try {
            ApiSyncCustomInterests action = new ApiSyncCustomInterests(env);
            action.setDataSource(dataSourceContacts);
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

    @RequestMapping(value="/syncVisitations", method=RequestMethod.GET)
    public ModelMap handleSyncVisitations(WebRequest webRequest) {
        Date start = new Date();
        int rowsProcessed = 0;
        Exception exception = null;

        int numWorkers = WORKERS_VISITATIONS;
        if (webRequest.getParameter("workers") != null) {
            try {
                numWorkers = Integer.parseInt(webRequest.getParameter("workers"));
            } catch (Exception e) {}        //ignore the error and use the default
        }

        try {
            for (int i = 0; i < numWorkers; i++) {
                ApiSyncCustomVisitations action = new ApiSyncCustomVisitations(env);
                action.setDataSource(dataSourceContacts);
                action.setWorkerIndex(i);
                Thread t = new Thread(action);
                t.start();
                System.out.println("started worker [Visitation" + i + "]");
            }
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

    @RequestMapping(value="/syncConstituents", method=RequestMethod.GET)
    public ModelMap handleSyncConstituents(WebRequest webRequest) {
        Date start = new Date();
        int rowsProcessed = 0;
        Exception exception = null;

        int numWorkers = WORKERS_CONSTITUENTS;
        if (webRequest.getParameter("workers") != null) {
            try {
                numWorkers = Integer.parseInt(webRequest.getParameter("workers"));
            } catch (Exception e) {}        //ignore the error and use the default
        }

        try {
            for (int i = 0; i < numWorkers; i++) {
                ApiSyncCustomConstituents action = new ApiSyncCustomConstituents(env);
                action.setDataSource(dataSourceContacts);
                action.setWorkerIndex(i);
                Thread t = new Thread(action);
                t.start();
                System.out.println("started worker [Constituent" + i + "]");
                //rowsProcessed = action.execute();
            }
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

    @RequestMapping(value="/serviceSyncConstituents", method=RequestMethod.GET)
    public String serviceSyncConstituents(WebRequest webRequest) {
        ModelMap resultMap =  serviceMarketo.syncPendingConstituents(integerParamOrNull(webRequest, "workers"), false);
        return jobStatusUrlPrefix + "?uuid=" + resultMap.get("jobUUID");
    }

    @RequestMapping(value="/serviceSyncVisitations", method=RequestMethod.GET)
    public String serviceSyncVisitations(WebRequest webRequest) {
        ModelMap resultMap =  serviceMarketo.syncPendingVisitations(integerParamOrNull(webRequest, "workers"), false);
        return jobStatusUrlPrefix + "?uuid=" + resultMap.get("jobUUID");
    }

    @RequestMapping(value="/serviceSyncOrders", method=RequestMethod.GET)
    public String serviceSyncOrders(WebRequest webRequest) {
        ModelMap resultMap =  serviceMarketo.syncPendingOrders(integerParamOrNull(webRequest, "workers"), false);
        return jobStatusUrlPrefix + "?uuid=" + resultMap.get("jobUUID");
    }
    @RequestMapping(value="/serviceSyncOrderDetails", method=RequestMethod.GET)
    public String serviceSyncOrderDetails(WebRequest webRequest) {
        ModelMap resultMap =  serviceMarketo.syncPendingOrderDetails(integerParamOrNull(webRequest, "workers"), false);
        return jobStatusUrlPrefix + "?uuid=" + resultMap.get("jobUUID");
    }

    @RequestMapping(value="/serviceSyncEvents", method=RequestMethod.GET)
    public String serviceSyncEvents(WebRequest webRequest) {
        ModelMap resultMap = serviceMarketo.syncPendingEvents(integerParamOrNull(webRequest, "workers"), false);
        return jobStatusUrlPrefix + "?uuid=" + resultMap.get("jobUUID");
    }

    @RequestMapping(value="/serviceSyncInterests", method=RequestMethod.GET)
    public String serviceSyncInterests(WebRequest webRequest) {
        ModelMap resultMap = serviceMarketo.syncPendingInterests(integerParamOrNull(webRequest, "workers"), false);
        return jobStatusUrlPrefix + "?uuid=" + resultMap.get("jobUUID");
    }

    @RequestMapping(value="/serviceSyncLeads", method=RequestMethod.GET)
    public String serviceSyncLeads(WebRequest webRequest) {
        ModelMap resultMap = serviceMarketo.syncPendingLeads(integerParamOrNull(webRequest, "workers"), false);
        return jobStatusUrlPrefix + "?uuid=" + resultMap.get("jobUUID");
    }

    @RequestMapping(value="/serviceMergeDuplicates", method=RequestMethod.GET)
    public String serviceMergeDuplicates(WebRequest webRequest) {
        ApiMergeDuplicateLeads apiMergeDuplicateLeads = (ApiMergeDuplicateLeads)appContext.getBean("apiMergeDuplicateLeads");

        try {
            apiMergeDuplicateLeads.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @RequestMapping(value="/serviceUpdateBadLeads", method=RequestMethod.GET)
    public String serviceUpdateBadLeads(WebRequest webRequest) {
        ModelMap resultMap = serviceMarketo.updateBadLeads(integerParamOrNull(webRequest, "workers"), false);
        return jobStatusUrlPrefix + "?uuid=" + resultMap.get("jobUUID");
    }

    private Integer integerParamOrNull(WebRequest webRequest, String paramName) {
        Integer returnValue = null;
        try {
            returnValue = new Integer(webRequest.getParameter(paramName));
        } catch (Exception e) {} //ignore it, return null
        return returnValue;
    }

    @RequestMapping(value="/syncOrders", method=RequestMethod.GET)
    public ModelMap handleSyncOrders(WebRequest webRequest) {
        Date start = new Date();
        int rowsProcessed = 0;
        Exception exception = null;

        int numWorkers = WORKERS_ORDERS;
        if (webRequest.getParameter("workers") != null) {
            try {
                numWorkers = Integer.parseInt(webRequest.getParameter("workers"));
            } catch (Exception e) {}        //ignore the error and use the default
        }

        try {
            for (int i = 0; i < numWorkers; i++) {
                ApiSyncCustomObjectsKeysAndAttributes action =
                        new ApiSyncCustomObjectsKeysAndAttributes("marketo.syncOrders.", "Order", env);
                action.setDataSource(dataSourceContacts);
                action.setWorkerIndex(i);
                Thread t = new Thread(action);
                t.start();
                System.out.println("started worker [Visitation" + i + "]");
            }

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


    @RequestMapping(value="/status", method=RequestMethod.GET)
    public ModelMap status(HttpServletRequest request) {
        ModelMap modelMap = new ModelMap();

        if (dataSourceContacts instanceof ComboPooledDataSource) {
            try {
                ComboPooledDataSource cpds = (ComboPooledDataSource) dataSourceContacts;
                modelMap.put("busyConnections", cpds.getNumBusyConnectionsAllUsers());
                modelMap.put("allConnections", cpds.getNumConnectionsAllUsers());
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }

        return modelMap;
    }

    @RequestMapping(value="/email", method=RequestMethod.GET)
    public ModelMap email(HttpServletRequest request) {
        ModelMap modelMap = new ModelMap();
        String email = request.getParameter("q");
        Connection con = null;

        try {
            con = dataSourceContacts.getConnection();

            List results = new ArrayList();

            if (email != null) {
                addRecords(results, con, "RE_ConstituentQuery", "select * FROM RE_ConstituentQuery WHERE email = '" + email + "'");
                addRecords(results, con, "Contacts", "select id, lastname, firstname, email, title, jobTitle, company, statusId, sourceId, dateCreated, dateModified, raiserEdgeId, galaxyId, zip, phone, city, state, country, birthdate, lastUploaded, gender, school, worker, membershipLevel, membershipStanding, expirationDate, timesRenewed, joinDate, dropDate, wealthScoreCategory, wealthScore, lastGiftAmount, highestGiftAmount, lastGiftDate, lastGiftFundDescription, totalGiftAmount, CMSMajorGiftLikelihood, CMSMidLevelGiftLikelihood, CMSPlannedGiftLikelihood, CMSTargetGiftRange, MembershipProgram, category FROM Contacts WHERE email = '" + email + "'");
                addRecords(results, con, "Marketo Status", "select id, objectType, status, key1, key2, lastUpdated FROM MarketoStatus WHERE key1 = '" + email + "' order by lastUpdated desc");
                addRecords(results, con, "Marketo Deletions", "select * FROM MarketoDeletions WHERE email = '" + email + "'");
            }

            addRecords(results, con, "Lead Standing", "select count(*) as count, standing from RE_ConstituentQuery group by standing");
            addRecords(results, con, "Membership Programs", "select count(*) as count, [Mem Program] as membershipProgram from RE_ConstituentQuery group by [Mem Program]");
            //addRecords(results, con, "Synchronization Summary", "select * from MarketoStatus where objectType = 'ADMIN' and lastUpdated > dateadd(d, -1, getdate())");
            addRecords(results, con, "Synchronization Detail (Last 24 Hours)", "select status, objectType, count(*) as count from MarketoStatus where objectType <> 'ADMIN' and lastUpdated > dateadd(d, -1, getdate()) group by objectType, status order by status desc");

            modelMap.put("results", results);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                if (con != null && !con.isClosed())
                    con.close();
            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
        return modelMap;
    }

    private void addRecords(List results, Connection con, String title, String sql) throws SQLException {
        results.add(new QueryResult(title, getRecords(con, sql)));
    }

    private List<List> getRecords(Connection con, String sql) throws SQLException {
        List result = new ArrayList();
        List columns = new ArrayList();

        Statement stmt = null;
        stmt = con.createStatement();
        ResultSet rs =
                stmt.executeQuery(sql);

        ResultSetMetaData rsmd = rs.getMetaData();
        for (int i = 1; i <= rsmd.getColumnCount(); i++) {
            String columnName = rsmd.getColumnName(i);
            columns.add(columnName);
        }
        result.add(columns);

        while(rs.next()) {
            List row = new ArrayList();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                if (rs.getObject(i) != null)
                    row.add(rs.getObject(i).toString());
                else
                    row.add("-");
            }
            result.add(row);
        }

        return result;
    }

    @RequestMapping(value="/syncMultipleLeads", method= RequestMethod.GET)
    public String handleSyncMultipleLeads(WebRequest webRequest) {

        Date start = new Date();
        int rowsProcessed = 0;
        Exception exception = null;
        UUID uuid = null;

        int numWorkers = WORKERS_LEADS;
        if (webRequest.getParameter("workers") != null) {
            try {
                numWorkers = Integer.parseInt(webRequest.getParameter("workers"));
            } catch (Exception e) {}        //ignore the error and use the default
        }

        try {
            List workers = new ArrayList();
            //@todo convert to service usage
            uuid = UUID.randomUUID();
            uuidsToJobs.put(uuid.toString(), workers);

            for (int i = 0; i < numWorkers; i++) {
                ApiSyncMultipleLeads actionListImport = new ApiSyncMultipleLeads(env);
                actionListImport.setDataSource(dataSourceContacts);
                actionListImport.setWorkerIndex(i);
                workers.add(actionListImport);
                Thread t = new Thread(actionListImport);
                t.start();
                Thread.sleep(1000);
                //rowsProcessed = actionListImport.execute();
            }

        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
        }
        return "forward:/marketo/jobStatus?uuid=" + uuid.toString();
    }

    @RequestMapping(value="/syncLeadAndInterests", method= RequestMethod.GET)
    public ModelMap handleSyncLeadAndInterests(WebRequest webRequest) {
        ModelMap modelMap = new ModelMap();
        try {
            Long contactId = new Long(webRequest.getParameter("contactId"));
            modelMap = serviceMarketo.syncSingleLeadAndInterests(contactId);
        } catch (Exception e) {
            modelMap.addAttribute("contactId", "");
            modelMap.addAttribute("result", "failure");
            modelMap.addAttribute("message", "contact id must be a valid integer");
        }
        return modelMap;
    }

    @RequestMapping(value="/describeMObject", method= RequestMethod.GET)
    public ModelMap handleDescribeMObject(WebRequest webRequest) {
        Date start = new Date();
        int rowsProcessed = 0;
        Exception exception = null;

        try {
            ApiDescribeMObject actionDescribe = new ApiDescribeMObject();
            actionDescribe.initialize(env);
            actionDescribe.setObjectType(webRequest.getParameter("type"));
            actionDescribe.execute();
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
}