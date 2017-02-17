package co.launchable.api.egalaxy;

import co.launchable.api.paymentech.Exchange;
import co.launchable.api.paymentech.PaymentechProcessor;
import co.launchable.api.viator.Ticket;
import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.io.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.*;
import java.util.*;

import com.paymentech.orbital.sdk.interfaces.ResponseIF;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.hibernate.SessionFactory;
import org.springframework.core.env.Environment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/*
To do:
1. set up order number such that it resets on a daily basis
2. make sure we're pulling in all of the input fields we need
3. trace through the code to make sure the logic is all correct
4. determine whether or not orders should be open or closed
5. update order status to be an int, which apparently it needs to be from the example
*/
@Service
public class ServiceGalaxy {

    @Autowired
    private SessionFactory sessionFactory;

    Logger log = Logger.getLogger(ServiceGalaxy.class);
    private static int RECOVERABLE_RETRIES = 2;

    private String serverUrl;
    private int serverPort = 3051;
    private String galaxyUsername;
    private String galaxyPassword;
    private String galaxySourceId;
    private String galaxyCustomerId;
    private String viatorCustomerId;
    private String viatorSalesProgramId;
    private Integer galaxyConnectionTimeoutTransaction = 120000;
    private Integer galaxyConnectionTimeoutStatusCheck = 30000;
    private String[] recoverableErrorStrings = new String[]{};

    private int maxPostAttempts = 1;
    private long timeoutBetweenAttempts = 2000;
    private int messageIndex = 1000;
    private Integer messageId = 100000;
    private int maxVisualIdRetrievalAttempts = 10;
    private int galaxyReauthenticationMinutes = -1;
    private boolean enableHeartbeatThread = true;
    private long defaultAbandonmentMs = 300000;
    private String membershipPropertiesPath;

    private VelocityEngine velocityEngine;
    PaymentechProcessor processor = new PaymentechProcessor();

    private Map<String, Product> mapProducts = new HashMap<String, Product>();
    private Map<String, FormOfPayment> mapFormsOfPayment = new HashMap<String, FormOfPayment>();
    private List listProductsNewMemberships = new ArrayList();
    private List listFormsOfPayment = new ArrayList();
    private DateFormat formatTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private DateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
    private DateFormat formatDateSimple = new SimpleDateFormat("MM-dd-yy hh:mmaa");
    private DateFormat formatWholeDateOut = new SimpleDateFormat("MM-dd-yy");

    private NumberFormat formatCurrency = NumberFormat.getCurrencyInstance();

    private boolean orbitalTesting = false;
    private int serverId;

    private int currentOrderNumber = -1;
    private Date currentDate;
    Properties orderProps;
    private DateFormat formatOrderNumber = new SimpleDateFormat("YYYYMMddHHmm");
    private long productCacheMs = 600000;
    private long productsLoaded = 0;
    private DecimalFormat dFormat = new DecimalFormat("0.00");

    private ComboPooledDataSource cpds;
    private DataSource dataSource;

    private Thread heartbeatGalaxyThread;

    private DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private DocumentBuilder documentBuilder;
    private Map mapEventTypesByName;
    private String velocityLogLocation;

    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    private Map mapSessionsToCreationTimes = new HashMap();

    public String getVelocityLogLocation() {
        return velocityLogLocation;
    }

    public void setVelocityLogLocation(String velocityLogLocation) {
        this.velocityLogLocation = velocityLogLocation;
    }

    public String getMembershipPropertiesPath() {
        return membershipPropertiesPath;
    }

    public void setMembershipPropertiesPath(String membershipPropertiesPath) {
        this.membershipPropertiesPath = membershipPropertiesPath;
    }

    public long getTimeoutBetweenAttempts() {
        return timeoutBetweenAttempts;
    }

    public void setTimeoutBetweenAttempts(long timeoutBetweenAttempts) {
        this.timeoutBetweenAttempts = timeoutBetweenAttempts;
    }

    public int getMaxPostAttempts() {
        return maxPostAttempts;
    }

    public void setMaxPostAttempts(int maxPostAttempts) {
        this.maxPostAttempts = maxPostAttempts;
    }

    public boolean isEnableHeartbeatThread() {
        return enableHeartbeatThread;
    }

    public void setEnableHeartbeatThread(boolean enableHeartbeatThread) {
        this.enableHeartbeatThread = enableHeartbeatThread;
    }

    public long getProductCacheMs() {
        return productCacheMs;
    }

    public void setProductCacheMs(long productCacheMs) {
        this.productCacheMs = productCacheMs;
    }

    public Integer getGalaxyConnectionTimeoutTransaction() {
        return galaxyConnectionTimeoutTransaction;
    }

    public void setGalaxyConnectionTimeoutTransaction(Integer galaxyConnectionTimeoutTransaction) {
        this.galaxyConnectionTimeoutTransaction = galaxyConnectionTimeoutTransaction;
    }

    public int getGalaxyReauthenticationMinutes() {
        return galaxyReauthenticationMinutes;
    }

    public void setGalaxyReauthenticationMinutes(int galaxyReauthenticationMinutes) {
        this.galaxyReauthenticationMinutes = galaxyReauthenticationMinutes;
    }

    public Integer getGalaxyConnectionTimeoutStatusCheck() {
        return galaxyConnectionTimeoutStatusCheck;
    }

    public void setGalaxyConnectionTimeoutStatusCheck(Integer galaxyConnectionTimeoutStatusCheck) {
        this.galaxyConnectionTimeoutStatusCheck = galaxyConnectionTimeoutStatusCheck;
    }

    public String getGalaxyUsername() {
        return galaxyUsername;
    }

    public void setGalaxyUsername(String galaxyUsername) {
        this.galaxyUsername = galaxyUsername;
    }

    public String getGalaxyPassword() {
        return galaxyPassword;
    }

    public void setGalaxyPassword(String galaxyPassword) {
        this.galaxyPassword = galaxyPassword;
    }

    public String getGalaxySourceId() {
        return galaxySourceId;
    }

    public void setGalaxySourceId(String galaxySourceId) {
        this.galaxySourceId = galaxySourceId;
    }

    public String getGalaxyCustomerId() {
        return galaxyCustomerId;
    }

    public void setGalaxyCustomerId(String galaxyCustomerId) {
        this.galaxyCustomerId = galaxyCustomerId;
    }

    public String getViatorCustomerId() {
        return viatorCustomerId;
    }

    public void setViatorCustomerId(String viatorCustomerId) {
        this.viatorCustomerId = viatorCustomerId;
    }

    public String getViatorSalesProgramId() {
        return viatorSalesProgramId;
    }

    public void setViatorSalesProgramId(String viatorSalesProgramId) {
        this.viatorSalesProgramId = viatorSalesProgramId;
    }

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public void setServerUrl(String serverUrl) {
        this.serverUrl = serverUrl;
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    public void setDataSource(DataSource dataSource2) {
        this.dataSource = dataSource2;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    @PostConstruct
    public void init() {
        initializeConnectionManager();
        initializeVelocity();
        initializeGalaxyHeartbeatThread();
        refreshCaches();
    }

    private void initializeConnectionManager() {
        cm.setMaxTotal(20);
        cm.setDefaultMaxPerRoute(10);
    }

    private void refreshCaches() {
        mapEventTypesByName = loadEventTypesMapByKey("Name");
    }

    private void initializeVelocity() {
        velocityEngine = new VelocityEngine();

        Properties p = new Properties();
        p.setProperty(RuntimeConstants.RESOURCE_LOADER, "classpath");
        p.setProperty("classpath.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        p.setProperty("runtime.log.logsystem.class", "org.apache.velocity.runtime.log.SimpleLog4JLogSystem");
        p.setProperty("runtime.log.logsystem.log4j.category", "velocity");
        p.setProperty("runtime.log.logsystem.log4j.logger", "velocity");
        p.setProperty("runtime.log", velocityLogLocation);
        velocityEngine.init(p);
    }

    private void initializeGalaxyHeartbeatThread() {
        try {
            HeartbeatGalaxyRunnable heartbeatGalaxyRunnable = new HeartbeatGalaxyRunnable();
            heartbeatGalaxyRunnable.setServiceGalaxy(this);
            heartbeatGalaxyRunnable.setReauthenticationMinutes(galaxyReauthenticationMinutes);
            heartbeatGalaxyThread = new Thread(heartbeatGalaxyRunnable);
            heartbeatGalaxyThread.start();
        } catch (Exception e) {
            log.warn("Heartbeat thread for galaxy web service not started, error was: " + e.getMessage());
        }
    }

    private synchronized int getNextMessageId() {
        messageId++;
        return messageId;
    }

    //@todo switch this to a generic indexDatabaseObjectsByKey method
    private Map loadEventTypesMapByKey(String key) {
        Connection con = null;
        PreparedStatement pstmt = null;
        Map returnMap = new HashMap();
        try {
            con = dataSource.getConnection();

            //@todo externalize this query
            String sql = "SELECT t.EventTypeId, t.Name FROM Galaxy_Test.dbo.RMEventTypes t";
            ResultSet rs = con.createStatement().executeQuery(sql);
            while (rs.next()) {
                Map map = new HashMap();
                map.put("EventTypeId", rs.getString("EventTypeId"));
                map.put("Name", rs.getString("Name"));

                returnMap.put(map.get(key), map);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) try {if (!con.isClosed()) con.close();} catch (Exception e) {};
        }
        return returnMap;
    }

    private List loadUserAccounts(String lastname, String lastfive) {

        Connection con = null;
        PreparedStatement pstmt = null;
        List accounts = new ArrayList();
        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement("SELECT * FROM Passes WHERE last = ? AND visualId LIKE ?");

            synchronized(pstmt) {
                //@todo switch this to the spring query template implementation for named queries
                pstmt.setString(1, lastname);
                pstmt.setString(2, "%" + lastfive);

                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Map map = new HashMap();
                    map.put("passNo", rs.getString("PassNo"));
                    map.put("first", rs.getString("First"));
                    map.put("last", rs.getString("Last"));
                    map.put("email", rs.getString("Email"));
                    map.put("visualId", rs.getString("VisualID"));
                    map.put("validFrom", rs.getDate("ValidFrom"));
                    map.put("validTo", rs.getDate("ValidUntil"));
                    accounts.add(map);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                pstmt.clearParameters();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
            if (con != null) try {if (!con.isClosed()) con.close();} catch (Exception e) {};
        }
        return accounts;
    }

    private String getSingleNodeValue(Document document, String nodeName) {
        if (document == null)
            return null;

        NodeList nl = document.getElementsByTagName(nodeName);
        if (nl.getLength() > 0) {
            Node node = nl.item(0);
            return node.getTextContent();
        }
        return null;
    }

    private ResponseServerStatus readResponseServerStatus(String xml) {
        ResponseServerStatus response = new ResponseServerStatus();
        response.setXml(xml);
        return response;
    }

    private ResponseTestRequest readResponseTestRequest(String xml) {
        ResponseTestRequest response = new ResponseTestRequest();
        response.setXml(xml);
        return response;
    }

    private Ticket findIncompleteTicketWithPlu(String plu, List tickets) {
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = (Ticket) tickets.get(i);
            if (ticket.getStatus() == null && ticket.getPlu().equals(plu))
                return ticket;
        }
        return null;
    }

    private Ticket findTicketWithVisualId(String visualId, List tickets) {
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = (Ticket) tickets.get(i);
            if (ticket.getVisualId() != null && ticket.getVisualId().equals(visualId))
                return ticket;
        }
        return null;
    }

    private ResponseActivateTickets readResponseActivateTickets(String xml, List tickets) {
        if (xml == null)
            return null;

        log.info("Ticket activation response received: \n" + xml);

        ResponseActivateTickets responseActivateTickets = new ResponseActivateTickets();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader( xml ) ));
            responseActivateTickets.setSessionId(getSingleNodeValue(document, "SessionID"));
            responseActivateTickets.setStatusCode(getSingleNodeValue(document, "StatusCode"));
            responseActivateTickets.setStatusText(getSingleNodeValue(document, "StatusText"));
            responseActivateTickets.setErrorCode(getSingleNodeValue(document, "ErrorCode"));
            responseActivateTickets.setErrorText(getSingleNodeValue(document, "ErrorText"));

            NodeList nlCreatedTickets = document.getElementsByTagName("Ticket");
            for (int i = 0; i < nlCreatedTickets.getLength(); i++) {
                Node nodeTicket = nlCreatedTickets.item(i);
                NodeList nlChildren = nodeTicket.getChildNodes();

                String visualId = null;
                String plu = null;
                for (int j = 0; j < nlChildren.getLength(); j++) {
                    Node nodeChild = nlChildren.item(j);
                    if (nodeChild.getNodeName().equals("VisualID"))
                        visualId = nodeChild.getTextContent();
                    else if (nodeChild.getNodeName().equals("PLU"))
                        plu = nodeChild.getTextContent();
                }

                //we've extracted the next visualId/plu pair, look for a ticket with this plu and
                //fill in the visual id, then set it's status to activated
                Ticket ticket = findTicketWithVisualId(plu, tickets);
                if (ticket != null) {
                    ticket.setStatus(Ticket.STATUS_ACTIVATED);
                    responseActivateTickets.getTickets().add(ticket);
                }
            }
        } catch (IOException ioe) {
            log.warn("Exception processing ticket creation: " + ioe.getMessage());
        } catch (SAXException sae) {
            log.warn("Exception processing ticket creation: " + sae.getMessage());
        } catch (ParserConfigurationException pce) {
            log.warn("Exception processing ticket creation: " + pce.getMessage());
        }
        return responseActivateTickets;
    }

    private ResponseSimple readResponseAbandonSession(String xml) {
        if (xml == null)
            return null;

        ResponseSimple response = new ResponseSimple();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader( xml ) ));
            response.setSessionId(getSingleNodeValue(document, "SessionID"));
            response.setStatusCode(getSingleNodeValue(document, "StatusCode"));
            response.setStatusText(getSingleNodeValue(document, "StatusText"));
            response.setErrorCode(getSingleNodeValue(document, "ErrorCode"));
            response.setErrorText(getSingleNodeValue(document, "ErrorText"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        return response;
    }

    private ResponseCreateTickets readResponseCreateTickets(String xml, List tickets) {
        if (xml == null)
            return null;

        ResponseCreateTickets responseCreateTickets = new ResponseCreateTickets();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader( xml ) ));
            responseCreateTickets.setSessionId(getSingleNodeValue(document, "SessionID"));
            responseCreateTickets.setStatusCode(getSingleNodeValue(document, "StatusCode"));
            responseCreateTickets.setStatusText(getSingleNodeValue(document, "StatusText"));
            responseCreateTickets.setErrorCode(getSingleNodeValue(document, "ErrorCode"));
            responseCreateTickets.setErrorText(getSingleNodeValue(document, "ErrorText"));

            NodeList nlCreatedTickets = document.getElementsByTagName("CreatedTicket");
            for (int i = 0; i < nlCreatedTickets.getLength(); i++) {
                Node nodeTicket = nlCreatedTickets.item(i);
                NodeList nlChildren = nodeTicket.getChildNodes();

                String visualId = null;
                String plu = null;
                for (int j = 0; j < nlChildren.getLength(); j++) {
                    Node nodeChild = nlChildren.item(j);
                    if (nodeChild.getNodeName().equals("VisualID"))
                        visualId = nodeChild.getTextContent();
                    else if (nodeChild.getNodeName().equals("PLU"))
                        plu = nodeChild.getTextContent();
                }

                //we've extracted the next visualId/plu pair, look for a ticket with this plu and
                //fill in the visual id, then set it's status to activated
                Ticket ticket = findIncompleteTicketWithPlu(plu, tickets);
                if (ticket != null) {
                    ticket.setStatus(Ticket.STATUS_ACTIVATED);
                    ticket.setVisualId(visualId);
                    responseCreateTickets.getTickets().add(ticket);
                }
            }
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        return responseCreateTickets;
    }

    private ResponseSimple readResponseSimple(String xml) {
        if (xml == null)
            return null;

        ResponseSimple responseSimple = new ResponseSimple();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader( xml ) ));
            responseSimple.setSessionId(getSingleNodeValue(document, "SessionID"));
            responseSimple.setStatusCode(getSingleNodeValue(document, "StatusCode"));
            responseSimple.setStatusText(getSingleNodeValue(document, "StatusText"));
            responseSimple.setErrorCode(getSingleNodeValue(document, "ErrorCode"));
            responseSimple.setErrorText(getSingleNodeValue(document, "ErrorText"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }
        return responseSimple;
    }

    private ResponseAuthentication readResponseAuthentication(String xml) {
        if (xml == null)
            return null;

        ResponseAuthentication responseAuthentication = new ResponseAuthentication();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader( xml ) ));
            responseAuthentication = new ResponseAuthentication();
            responseAuthentication.setSessionId(getSingleNodeValue(document, "SessionID"));
            responseAuthentication.setStatusCode(getSingleNodeValue(document, "StatusCode"));
            responseAuthentication.setStatusText(getSingleNodeValue(document, "StatusText"));
            responseAuthentication.setErrorCode(getSingleNodeValue(document, "ErrorCode"));
            responseAuthentication.setErrorText(getSingleNodeValue(document, "ErrorText"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }

        return responseAuthentication;
    }

    protected ResponseTestRequest testRequest() {
        StringWriter stringWriter = new StringWriter();

        VelocityContext vc = new VelocityContext();
        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("serverId", serverId);

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_authenticate.vtl");
        t.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "testRequest");
        return readResponseTestRequest(responseXml);
    }

    private EventTicketReleaseResponse readEventTicketReleaseResponse(String xml) {
        if (xml == null)
            return null;

        EventTicketReleaseResponse eventTicketReleaseResponse = new EventTicketReleaseResponse();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader( xml ) ));
            eventTicketReleaseResponse = new EventTicketReleaseResponse();
            eventTicketReleaseResponse.setSessionId(getSingleNodeValue(document, "SessionID"));
            eventTicketReleaseResponse.setStatusCode(getSingleNodeValue(document, "StatusCode"));
            eventTicketReleaseResponse.setStatusText(getSingleNodeValue(document, "StatusText"));
            eventTicketReleaseResponse.setErrorCode(getSingleNodeValue(document, "ErrorCode"));
            eventTicketReleaseResponse.setErrorText(getSingleNodeValue(document, "ErrorText"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }

        return eventTicketReleaseResponse;
    }

    public EventTicketCommitResponse apiEventTicketCommit(EventTicketHoldResponse eventTicketHoldResponse) {
        Integer sectionId = eventTicketHoldResponse.getEvent().getResourceID();
        return apiEventTicketCommit(eventTicketHoldResponse.getEvent(), eventTicketHoldResponse.getSessionId(), eventTicketHoldResponse.getCapacityId(), sectionId, eventTicketHoldResponse.getQuantity());
    }

    public EventTicketReleaseResponse apiEventTicketRelease(EventTicketHoldResponse eventTicketHoldResponse) {
        Integer sectionId = Integer.parseInt(eventTicketHoldResponse.getSectionId());
        return apiEventTicketRelease(eventTicketHoldResponse.getEvent(), eventTicketHoldResponse.getSessionId(), eventTicketHoldResponse.getCapacityId(), sectionId, eventTicketHoldResponse.getQuantity());
    }

    public EventTicketCommitResponse apiEventTicketCommit(Event event, String sessionId, String capacityId, Integer sectionId, Integer quantity) {
        log.info("committing tickets in session: " + sessionId);

        VelocityContext vc = new VelocityContext();
        StringWriter stringWriter = new StringWriter();

        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxyUsername", galaxyUsername);
        vc.put("galaxyPassword", galaxyPassword);
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("serverId", serverId);
        vc.put("eventId", event.getEventID());
        vc.put("sessionId", sessionId);
        vc.put("sectionId", sectionId);
        vc.put("capacityId", capacityId);

        vc.put("quantity", quantity);

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_eventTicketCommit.vtl");
        t.merge(vc, stringWriter);
        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "apiEventTicketCommit");
        return readEventTicketCommitResponse(responseXml);
    }

    public EventTicketReleaseResponse apiEventTicketRelease(Event event, String sessionId, String capacityId, Integer sectionId, Integer quantity) {
        log.info("releasing tickets in session: " + sessionId);

        VelocityContext vc = new VelocityContext();
        StringWriter stringWriter = new StringWriter();

        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxyUsername", galaxyUsername);
        vc.put("galaxyPassword", galaxyPassword);
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("serverId", serverId);
        vc.put("eventId", event.getEventID());
        vc.put("sessionId", sessionId);
        vc.put("sectionId", sectionId);
        vc.put("capacityId", capacityId);

        vc.put("quantity", quantity);

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_eventTicketRelease.vtl");
        t.merge(vc, stringWriter);
        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "apiEventTicketRelease");
        return readEventTicketReleaseResponse(responseXml);
    }

    private EventTicketCommitResponse readEventTicketCommitResponse(String xml) {
        if (xml == null)
            return null;

        EventTicketCommitResponse eventTicketCommitResponse = new EventTicketCommitResponse();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader( xml ) ));
            eventTicketCommitResponse = new EventTicketCommitResponse();
            eventTicketCommitResponse.setSessionId(getSingleNodeValue(document, "SessionID"));
            eventTicketCommitResponse.setCapacityId(getSingleNodeValue(document, "CapacityID"));
            eventTicketCommitResponse.setStatusCode(getSingleNodeValue(document, "StatusCode"));
            eventTicketCommitResponse.setStatusText(getSingleNodeValue(document, "StatusText"));
            eventTicketCommitResponse.setErrorCode(getSingleNodeValue(document, "ErrorCode"));
            eventTicketCommitResponse.setErrorText(getSingleNodeValue(document, "ErrorText"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }

        return eventTicketCommitResponse;
    }

    private EventTicketHoldResponse readEventTicketHoldResponse(String xml) {
        if (xml == null)
            return null;

        EventTicketHoldResponse eventTicketHoldResponse = new EventTicketHoldResponse();
        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader( xml ) ));
            eventTicketHoldResponse = new EventTicketHoldResponse();
            eventTicketHoldResponse.setSessionId(getSingleNodeValue(document, "SessionID"));
            eventTicketHoldResponse.setCapacityId(getSingleNodeValue(document, "CapacityID"));
            eventTicketHoldResponse.setStatusCode(getSingleNodeValue(document, "StatusCode"));
            eventTicketHoldResponse.setStatusText(getSingleNodeValue(document, "StatusText"));
            eventTicketHoldResponse.setErrorCode(getSingleNodeValue(document, "ErrorCode"));
            eventTicketHoldResponse.setErrorText(getSingleNodeValue(document, "ErrorText"));
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } catch (SAXException sae) {
            sae.printStackTrace();
        } catch (ParserConfigurationException pce) {
            pce.printStackTrace();
        }

        return eventTicketHoldResponse;
    }

    public EventTicketHoldResponse apiEventTicketHold(Event event, String sessionId, Integer sectionId, Integer quantity) {
        log.info("holding tickets in session: " + sessionId);

        VelocityContext vc = new VelocityContext();
        StringWriter stringWriter = new StringWriter();

        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxyUsername", galaxyUsername);
        vc.put("galaxyPassword", galaxyPassword);
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("serverId", serverId);
        vc.put("eventId", event.getEventID());
        vc.put("sessionId", sessionId);
        vc.put("sectionId", sectionId);
        vc.put("quantity", quantity);

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_eventTicketHold.vtl");
        t.merge(vc, stringWriter);
        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "apiEventTicketHold");
        EventTicketHoldResponse eventTicketHoldResponse = readEventTicketHoldResponse(responseXml);
        eventTicketHoldResponse.setQuantity(quantity);
        eventTicketHoldResponse.setEvent(event);

        return eventTicketHoldResponse;
    }

    public ResponseSimple apiEventTicketsCancel(Event event, String sessionId, List tickets) {
        log.info("canceling tickets in session: " + sessionId);

        VelocityContext vc = new VelocityContext();
        StringWriter stringWriter = new StringWriter();

        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxyUsername", galaxyUsername);
        vc.put("galaxyPassword", galaxyPassword);
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("customerId", viatorCustomerId);
        vc.put("serverId", serverId);
        vc.put("tickets", tickets);

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_activateTicketsCancel.vtl");
        t.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "apiEventTicketsCancel");
        ResponseSimple responseSimple = readResponseSimple(responseXml);

        if (responseSimple.getStatusCode().equals("0")) {
//            for (int i = 0; i < tickets.size(); i++) {
//                ForeignMediaTicket foreignMediaTicket = (ForeignMediaTicket) tickets.get(i);
//                foreignMediaTicket.setStatus(ForeignMediaTicket.STATUS_ACTIVATED);
//            }
        } else {
//            for (int i = 0; i < tickets.size(); i++) {
//                ForeignMediaTicket foreignMediaTicket = (ForeignMediaTicket) tickets.get(i);
//                foreignMediaTicket.setStatus(ForeignMediaTicket.STATUS_FAILED_ACTIVATION);
//            }
        }
        return responseSimple;
    }

    public ResponseActivateTickets apiEventTicketsActivate(String sessionId, List tickets) {
        log.info("activating tickets for session id: " + sessionId);

        VelocityContext vc = new VelocityContext();
        StringWriter stringWriter = new StringWriter();

        Double payment = 0.0;
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = (Ticket) tickets.get(i);
            payment += ticket.getPrice();
        }

        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxyUsername", galaxyUsername);
        vc.put("galaxyPassword", galaxyPassword);
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("customerId", viatorCustomerId);
        vc.put("salesProgramId", viatorSalesProgramId);
        vc.put("serverId", serverId);
        vc.put("sessionId", sessionId);
        vc.put("tickets", tickets);
        vc.put("paymentFormatted", dFormat.format(payment));

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_activateTickets.vtl");
        t.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "apiEventTicketsActivate");
        ResponseActivateTickets responseActivateTickets = readResponseActivateTickets(responseXml, tickets);

        boolean activated = responseActivateTickets.getStatusCode().equals("0");
        for (int i = 0; i < tickets.size(); i++) {
            Ticket ticket = (Ticket) tickets.get(i);
            ticket.setStatus(activated ? Ticket.STATUS_ACTIVATED : Ticket.STATUS_FAILED);
            log.info("activated ticket: " + ticket.getVisualId());
        }
        return responseActivateTickets;
    }

    public ResponseSimple apiAbandonSession(String sessionId) {
        log.info("abandoning session id: " + sessionId);

        VelocityContext vc = new VelocityContext();
        StringWriter stringWriter = new StringWriter();

        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxyUsername", galaxyUsername);
        vc.put("galaxyPassword", galaxyPassword);
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("customerId", viatorCustomerId);
        vc.put("serverId", serverId);
        vc.put("sessionId", sessionId);

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_abandonSession.vtl");
        t.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "apiAbandonSession");
        ResponseSimple responseAbandonSession = readResponseAbandonSession(responseXml);
        return responseAbandonSession;
    }

    public void closeIdleConnections() {
        if (cm != null) {
            cm.closeIdleConnections(5, TimeUnit.MINUTES);
        }
    }

    public ResponseCreateTickets apiEventTicketsCreate(Event event, String sessionId, List tickets, Double payment) {
        VelocityContext vc = new VelocityContext();
        StringWriter stringWriter = new StringWriter();

        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxyUsername", galaxyUsername);
        vc.put("galaxyPassword", galaxyPassword);
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("customerId", viatorCustomerId);
        vc.put("salesProgramId", viatorSalesProgramId);
        vc.put("serverId", serverId);
        vc.put("eventId", event.getEventID());
        vc.put("sessionId", sessionId);

        vc.put("sectionId", event.getResourceID());
        vc.put("tickets", tickets);
        vc.put("paymentFormatted", dFormat.format(payment));

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_createTickets.vtl");
        t.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "apiEventTicketsCreate");
        ResponseCreateTickets responseCreateTickets = readResponseCreateTickets(responseXml, tickets);

        if (!responseCreateTickets.getStatusCode().equals("0")) {
            for (int i = 0; i < tickets.size(); i++) {
                Ticket ticket = (Ticket) tickets.get(i);
                ticket.setStatus(Ticket.STATUS_FAILED);
            }
        }
        return responseCreateTickets;
    }

    public boolean sessionStillActive(String sessionId) {
        return mapSessionsToCreationTimes.containsKey(sessionId);
    }

    public ResponseAuthentication authenticateToGalaxy() {
        log.info("Authenticating to galaxy server");
        StringWriter stringWriter = new StringWriter();
        VelocityContext vc = new VelocityContext();

        //pre-adding currentMessageId, may have been causing problems?
        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxyUsername", galaxyUsername);
        vc.put("galaxyPassword", galaxyPassword);
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("galaxyCustomerId", galaxyCustomerId);
        vc.put("serverId", serverId);

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_authenticate.vtl");
        t.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "authenticateToGalaxy");
        return readResponseAuthentication(responseXml);
    }

    public String getServerUrl() {
        return serverUrl;
    }

    public ResponseServerStatus getServerStatus() {
        log.info("Authenticating to galaxy server");
        StringWriter stringWriter = new StringWriter();
        VelocityContext vc = new VelocityContext();

        //pre-adding currentMessageId, may have been causing problems?
        vc.put("messageId", getNextMessageId());
        vc.put("timestamp", formatTimestamp.format(new Date()));
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("serverId", serverId);

        Template t = velocityEngine.getTemplate("templates/galaxy/tpl_queryServerStatus.vtl");
        t.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "getServerStatus");
        return readResponseServerStatus(responseXml);
    }

    private synchronized OrderIdentifiers getNewOrderIdentifiers() {
        Date now = new Date();
        log.info("Generating new order identifiers");

        try {
            String nowFormatted = formatOrderNumber.format(now);
            Date nowTruncated = formatOrderNumber.parse(nowFormatted);

            if (nowTruncated.after(currentDate)) {
                currentDate = nowTruncated;
                currentOrderNumber = (serverId * 1000);
                log.info("New date, restarting counter at serverId * 1000 = " + (serverId * 1000));
            } else {
                currentOrderNumber = currentOrderNumber + 1;
                log.info("Same date, incremented current order number to " + currentOrderNumber);
            }

            int localMessageId = getNextMessageId();
            OrderIdentifiers oi = new OrderIdentifiers();
            oi.messageId = localMessageId + "";
            oi.orderNumber = nowFormatted + currentOrderNumber;
            oi.sourceId = galaxySourceId + serverId;

            //need to write out our new order number and increment the message id
            writeProps(currentOrderNumber, now, localMessageId);
            return oi;
        } catch (Exception e) {
            log.error("Problem writing new order properties to file.  Error was : " + e.getMessage());
            return null;
        }
    }

    private void writeProps(int currentOrderNumber, Date now, int currentMessageId) throws FileNotFoundException, IOException {
        log.info("Storing current properties");

        FileOutputStream fos = new FileOutputStream(new File(membershipPropertiesPath));
        synchronized(orderProps) {
            orderProps.setProperty("currentOrderNumber", currentOrderNumber + "");
            orderProps.setProperty("currentMessageId", currentMessageId + "");
            orderProps.setProperty("currentDate", formatOrderNumber.format(now));
            orderProps.store(fos, "This file is used for the mobile membership application.  Do not modify unless you know what you're doing.");
            log.info("Properties file updated");
        }
    }

    private List loadFormsOfPayment(Connection con) {
        log.info("Loading forms of payment");
        try {
            int loadCount = 0;
            ResultSet rs = con.createStatement().executeQuery("select fopid, fopcode, name from fops where FOPCode between 31 and 39");
            while (rs.next()) {
                FormOfPayment formOfPayment = new FormOfPayment(rs.getInt(1), rs.getString(2), rs.getString(3));
                listFormsOfPayment.add(formOfPayment);
                mapFormsOfPayment.put(formOfPayment.getName().trim(), formOfPayment);
                loadCount++;
            }
            log.info("Form of payment load count: " + loadCount);
            return listFormsOfPayment;
        } catch (SQLException sqle) {
            log.warn("Loading forms of payment failed, error: " + sqle.getMessage());
            sqle.printStackTrace();
        }
        log.warn("Returning empty forms of payment list");
        return new ArrayList();
    }

    private List loadNewProducts() {
        log.info("Loading new membership products");
        Connection con = null;

        try {
            con = dataSource.getConnection();
            synchronized(con) {
                ResultSet rs = con.createStatement().executeQuery(
                        "select sch.PLU, sch.Name, i.Price from SalesChannelHierarchy sch " +
                                "join items i on sch.plu = i.plu where ExternalID2 = 'MEMBERS'");

                while (rs.next())
                    listProductsNewMemberships.add(new Product(rs.getString(1) + "", rs.getString(2), rs.getString(2), rs.getDouble(3)));

                //build a map of the products
                for (int i = 0; i < listProductsNewMemberships.size(); i++) {
                    Product product = (Product) listProductsNewMemberships.get(i);
                    mapProducts.put(product.getId(), product);
                }
                productsLoaded = System.currentTimeMillis();

                loadFormsOfPayment(con);
            }
            return listProductsNewMemberships;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            if (con != null) try {if (!con.isClosed()) con.close();} catch (Exception e) {};
        }
        return new ArrayList();
    }

    private List getProductsNewMembership()  {
        log.info("Retrieved products, count of " + listProductsNewMemberships.size());
        if (listProductsNewMemberships.size() > 0)
            return listProductsNewMemberships;
        else  {
            loadNewProducts();
            return listProductsNewMemberships;
        }
    }

    private String jsonProp(String label, String value) {
        return ",\"" + label + "\": " + "\"" + value + "\"";
    }

    private String getMembershipJson(String userid) {
        StringBuffer sb = new StringBuffer();
        sb.append("{result: success");
        sb.append(jsonProp("userid", userid));
        sb.append(jsonProp("status", "expired"));
        sb.append(jsonProp("expires", "2013-12-01"));
        sb.append("}");

        return sb.toString();
    }

    /**
     * Probably don't need this to be sent out to the client at this point, just need to look
     * up which form of payment is appropriate given the passed in credit card number
     * @return
     */
    private String getFormsOfPaymentJson() {
        StringBuffer sb = new StringBuffer();

        sb.append("{\"fops\":[");
        for (int i = 0; i < listFormsOfPayment.size(); i++) {
            if (i > 0)
                sb.append(", ");
            FormOfPayment formOfPayment = (FormOfPayment)listFormsOfPayment.get(i);
            sb.append("{\"id\": \"");
            sb.append(formOfPayment.getId());
            sb.append("\", \"name\": \"");
            sb.append(formOfPayment.getName());
            sb.append("\", \"code\": \"");
            sb.append(formOfPayment.getCode());
            sb.append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private String getNewMembershipTypesJson() {
        List products;
        products = getProductsNewMembership();

        StringBuffer sb = new StringBuffer();
        sb.append("{\"membershipTypes\":[");
        for (int i = 0; i < products.size(); i++) {
            if (i > 0)
                sb.append(", ");
            Product product = (Product)products.get(i);
            sb.append("{\"id\": \"");
            sb.append(product.getId());
            sb.append("\", \"name\": \"");
            sb.append(product.getName());
            sb.append("\", \"amount\": \"");
            sb.append(product.getAmount());
            sb.append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    private void appendJsonProperty(Map in, String propertyName, StringBuffer sb) {
        sb.append(",\"");
        sb.append(propertyName);
        sb.append("\"");
        sb.append(":");
        sb.append("\"");
        Object value = in.get(propertyName);
        if (value instanceof String)
            sb.append(((String) value).trim());
        else
            sb.append(value.toString());
        sb.append("\"");
    }

    public boolean testReachabilityGalaxyWebService() {
        log.info("Testing reachability of galaxy web service using timeout of " + galaxyConnectionTimeoutStatusCheck.intValue());
        ResponseTestRequest response = testRequest();
        return response.getXml() != null;
    }

    public boolean testReachabilityGalaxyDatabase() {
        log.info("Testing reachability of galaxy database");
        Connection con = null;
        ResultSet rs = null;
        boolean reachability = false;

        try {
            con = dataSource.getConnection();
            rs = con.createStatement().executeQuery("select count(*) from passes");
            while (rs.next()) {
                reachability = true;
            }
            rs.close();
        }
        catch (SQLException sqle) {
            log.warn("Error testing reachability of Galaxy database: " + sqle.getMessage());
            reachability = false;
        } finally {
            if (rs != null) try {rs.close();} catch (Exception e) {log.warn("ResultSet was already closed: " + e.getMessage());};
            if (con != null) try {con.close();} catch (Exception e) {log.warn("Connection was already closed: " + e.getMessage());};
        }
        return reachability;
    }

    public boolean testReachabilityPaymentProcessor() {
        return processor.testReachability();
    }

    private String runStatusChecks(Integer serverId) {
        log.info("Running status checks");
        //galaxy web service
        boolean galaxyWebServiceReachable = testReachabilityGalaxyWebService();
        boolean galaxyDatabaseReachable = testReachabilityGalaxyDatabase();
        boolean paymentWebServiceReachable = processor.testReachability();

        StringBuffer sbJson = new StringBuffer();
        sbJson.append("{");
        sbJson.append("\"galaxyWebServiceReachable\": \"");
        sbJson.append(galaxyWebServiceReachable);
        sbJson.append("\", \"galaxyDatabaseReachable\": \"");
        sbJson.append(galaxyDatabaseReachable);
        sbJson.append("\", \"paymentWebServiceReachable\": \"");
        sbJson.append(paymentWebServiceReachable);
        sbJson.append("\"}");
        return sbJson.toString();
    }

    /**
     * Validate the current order, make sure everything is coherent before passing it on to the processor.
     * @param order
     * @return
     */
    private boolean orderValidate(Map order) {
        return true;
    }

    private void checkEquivalentAddresses(Map orderMap) {
        //check to see if the billing address is the same as the user's address.. if it is we can send over the ShipToContact as
        //<SameAsOrderContact>YES</SameAsOrderContact>

        //note, this isn't currently used
        if (equivalent(orderMap.get("street1"), orderMap.get("ccStreet1"))
                && equivalent(orderMap.get("street2"), orderMap.get("ccStreet2"))
                && equivalent(orderMap.get("city"), orderMap.get("ccCity"))
                && equivalent(orderMap.get("state"), orderMap.get("ccState"))
                && equivalent(orderMap.get("zip"), orderMap.get("ccZip"))
                && equivalent(orderMap.get("country"), orderMap.get("ccCountry"))) {
            orderMap.put("sameAsOrderContact", "YES");
        } else
            orderMap.put("sameAsOrderContact", "NO");
    }

    private boolean equivalent(Object oFirst, Object oSecond) {
        String first = (String)oFirst;
        String second = (String)oSecond;

        if (first != null && second != null)
            return first.equals(second);
        if (first == null && second == null) return true;
        return false;
    }

    private void lookupVisualId(Map orderMap) {
        //the new pass isn't assigned a visual id immediately, so we have to wait for 5 seconds here.. ugh

        boolean visualIdFound = false;
        Connection con = null;
        ResultSet rs = null;
        try {
            con = dataSource.getConnection();
            String sql = "select VisualID, ValidUntil from passes WHERE OrderId = " + orderMap.get("galaxyOrderId");
            int attempts = 0;
            while (!visualIdFound && attempts < maxVisualIdRetrievalAttempts) {

                rs = con.createStatement().executeQuery(sql);
                while (rs.next()) {
                    orderMap.put("visualId", rs.getString(1));
                    orderMap.put("validTo", rs.getDate(2));
                    if (orderMap.get("visualId") != null && ((String)orderMap.get("visualId")).length() > 0)
                        visualIdFound = true;
                }

                rs.close();
                //if we didn't get the visual id, increment our attempts counter and sleep for a second
                //to allow the galaxy system to catch up
                if (!visualIdFound) {
                    attempts++;

                    if (attempts > (maxVisualIdRetrievalAttempts / 3))
                        log.info("Attempt " + attempts + " to retrieve the visual ID for order " + orderMap.get("galaxyOrderId"));

                    try {
                        Thread.sleep(1000);
                    }
                    catch (InterruptedException ie) {
                        log.warn("Thread sleep was interrupted: " + ie.getMessage());
                    }
                }
            }
        }
        catch (SQLException sqle) {
            log.warn("Error retrieving visual ID from database: " + sqle.getMessage());
        } finally {
            if (rs != null) try {rs.close();} catch (Exception e) {log.warn("ResultSet was already closed: " + e.getMessage());};
            if (con != null) try {con.close();} catch (Exception e) {log.warn("Connection was already closed: " + e.getMessage());};
        }
    }

    public String getSessionId(String sessionId) {
        return getSessionId(sessionId, defaultAbandonmentMs);
    }

    public String getSessionId(String sessionId, long abandonAfterMs) {
        boolean exists = mapSessionsToCreationTimes.containsKey(sessionId);

        if (sessionId == null || !exists) {
            log.info("argument session id: " + sessionId + ", exists in expiration set: " + exists + ", generating new session id");
            ResponseAuthentication responseAuthentication = authenticateToGalaxy();
            if (responseAuthentication == null || responseAuthentication.getSessionId() == null)
                sessionId = "";
            else
                sessionId = responseAuthentication.getSessionId();
            log.info("generated session id: " + sessionId);
        }

        if (!sessionId.equals("")) {
            mapSessionsToCreationTimes.put(sessionId, System.currentTimeMillis() + abandonAfterMs);
        }
        return sessionId;
    }

    public void abandonOldSessions() {
        Iterator it = mapSessionsToCreationTimes.keySet().iterator();
        long now = System.currentTimeMillis();

        while (it.hasNext()) {
            String sessionId = (String)it.next();
            long abandonAfterTime = (Long)mapSessionsToCreationTimes.get(sessionId);

            if (now > abandonAfterTime) {
                apiAbandonSession(sessionId);
                it.remove();
            }
        }
    }

    public List getEventById(Long eventId, String sessionId) {
        sessionId = getSessionId(sessionId);

        StringWriter stringWriter = new StringWriter();
        VelocityContext vc = new VelocityContext();
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("serverId", serverId);
        vc.put("sessionId", sessionId);
        vc.put("messageId", getNextMessageId());
        vc.put("eventId", eventId);
        vc.put("timestamp", formatTimestamp.format(new Date()));

        Template template = velocityEngine.getTemplate("templates/galaxy/tpl_getEvents.vtl");
        template.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "getEventById");
        return extractEventsFromXml(responseXml, sessionId);
    }

    public List getEventsForDateRange(Date start, Date end, String resourceId, String eventTypeName, String eventName, String plu, Integer minimumAvailability, String sessionId, boolean requireEventTypeId) {
        List events = null;
        sessionId = getSessionId(sessionId);

        StringWriter stringWriter = new StringWriter();
        VelocityContext vc = new VelocityContext();
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("serverId", serverId);
        vc.put("sessionId", sessionId);
        vc.put("messageId", getNextMessageId());
        vc.put("dateStart", formatTimestamp.format(start));
        vc.put("dateEnd", formatTimestamp.format(end));


        vc.put("plu", "");
        if (plu != null)
            vc.put("plu", plu);

        vc.put("resourceId", "");
        if (resourceId != null)
            vc.put("resourceId", resourceId);

        vc.put("eventTypeId", "");
        if (eventTypeName != null) {
            Map map = (Map)mapEventTypesByName.get(eventTypeName);

            if (map != null) {
                String targetId = (String) map.get("EventTypeId");
                if (targetId != null)
                    vc.put("eventTypeId", targetId);
            }
        }

        //if we need to have an event type id just return the empty list if we couldn't find a mapping for it from
        //the passed in name
        if (requireEventTypeId) {
            if (vc.get("eventTypeId").equals(""))
                return new ArrayList();
        }

        vc.put("timestamp", formatTimestamp.format(new Date()));

        Template template = velocityEngine.getTemplate("templates/galaxy/tpl_queryEvents.vtl");
        template.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "getEventsForDateRange");
        events = extractEventsFromXml(responseXml, sessionId);

        if (eventName != null) {
            List returnList = new ArrayList();
            for (int i = 0; i < events.size(); i++) {
                Event event = (Event) events.get(i);
                if (event.getEventName().endsWith(eventName))
                //if (event.getEventName().equals(eventName))
                    returnList.add(event);
            }
            return returnList;
        }
        return events;
    }

    private List extractEventsFromXml(String xml, String sessionId) {
        List results = new ArrayList();

        if (xml == null)
            return new ArrayList();

        try {
            documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document document = documentBuilder.parse(new InputSource( new StringReader( xml ) ));

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            NodeList eventNodes = document.getElementsByTagName("Event");
            for (int i = 0; i < eventNodes.getLength(); i++) {
                Event event = new Event();
                event.setSessionID(sessionId);

                Node node = eventNodes.item(i);
                NodeList children = node.getChildNodes();
                for (int j = 0; j < children.getLength(); j++) {
                    Node child = children.item(j);
                    String nodeName = child.getNodeName();
                    String textContent = child.getTextContent();

                    //@todo maybe switch this to a string switch if we upgrade to java 7
                    if ("EventID".equals(nodeName))
                        event.setEventID(Integer.parseInt(textContent));
                    else if ("EventName".equals(nodeName))
                        event.setEventName(textContent);
                    else if ("StartDateTime".equals(nodeName))
                        event.setStartDateTime(sdf.parse(textContent));
                    else if ("EndDateTime".equals(nodeName))
                        event.setEndDateTime(sdf.parse(textContent));
                    else if ("EventTypeID".equals(nodeName))
                        event.setEventTypeID(Integer.parseInt(textContent));
                    else if ("OnSaleDateTime".equals(nodeName))
                        event.setOnSaleDateTime(sdf.parse(textContent));
                    else if ("OffSaleDateTime".equals(nodeName))
                        event.setOffSaleDateTime(sdf.parse(textContent));
                    else if ("ResourceID".equals(nodeName))
                        event.setResourceID(Integer.parseInt(textContent));
                    else if ("UserEventNumber".equals(nodeName))
                        event.setUserEventNumber(Integer.parseInt(textContent));
                    else if ("Available".equals(nodeName))
                        event.setAvailable(Integer.parseInt(textContent));
                    else if ("Status".equals(nodeName))
                        event.setStatus(Integer.parseInt(textContent));
                    else if ("HasRoster".equals(nodeName))
                        event.setHasRoster(textContent.equals("YES"));
                    else if ("PrivateEvent".equals(nodeName))
                        event.setPrivateEvent(textContent.equals("YES"));
                }
                results.add(event);
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return results;
    }

    public Map purchaseMembership(Map<String, Object> orderMap, OrderIdentifiers orderIdentifiers) {

        //we can get a null value back if the servlet was unable to write to the properties file for some reason
        //to prevent submission of multiple orders with the same id we have synchronized access to the "next" order
        //number, but in case of server restart we also need to be writing the "current" order number to file
        //each time... if writing to file fails we need to fail the whole process.. really shouldn't happen if
        //things are configured correctly and under medium load
        if (orderIdentifiers.orderNumber == null)
            return null;

        checkEquivalentAddresses(orderMap);

        boolean orderIsValid = orderValidate(orderMap);
        if (orderIsValid) {
            synchronized (processor) {
                try {
                    Exchange exchange;
                    Double amount = Double.parseDouble((String) orderMap.get("ccAmount"));
                    if (this.orbitalTesting)
                        amount = new Double(1);

                    exchange = processor.authCaptureTransaction(
                            (String)orderMap.get("orderNumber"),
                            Double.parseDouble((String)orderMap.get("ccAmount")),
                            (String)orderMap.get("ccNumber"),
                            (String)orderMap.get("ccCvv"),
                            (String)orderMap.get("ccExpiry"),
                            (String)orderMap.get("ccName"), null, null, null, null, null,
                            (String)orderMap.get("ccZip"));

                    ResponseIF responseAuth = exchange.getResponse();
                    String separationCharacter = ",";
                    log.info("Paymentech response: " + (String) orderMap.get("orderNumber")
                            + separationCharacter + responseAuth.getValue("AuthCode")
                            + separationCharacter + responseAuth.getValue("RespCode")
                            + separationCharacter + responseAuth.getValue("AVSRespCode")
                            + separationCharacter + responseAuth.getCVV2RespCode()
                            + separationCharacter + responseAuth.getValue("TxRefNum"));

                    //check the response fields to see if we had a success
                    if (authWasSuccessful(responseAuth)) {
                        ///add the authorization code and transaction id to the order map
                        orderMap.put("authCode", responseAuth.getValue("AuthCode"));
                        orderMap.put("txRef", responseAuth.getValue("TxRefNum"));

                        //create the membership
                        String responseXml = createMembership(orderMap);
                        log.info("Received response XML from membership creation: " + responseXml);

                        return processResponse(responseXml, orderMap, exchange, 0);

                    }  else {
                        //capture wasn't successful, send the information back to the browser
                        return sendPaymentFailureResponse(responseAuth);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private Map processResponse(String responseXml, Map orderMap, Exchange exchange,int tryNumber) throws Exception {
        if (membershipCreationWasSuccessful(orderMap)) {
            //lookupVisualId(orderMap);  //no longer need to do this, as updating the delivery method causes
            //the visual id to be generated synchronously

            //ResponseIF responseCapture = captureTransaction(orderMap, responseAuth);
            //send the confirmation information + the membership details back to the browser
            return sendPaymentSuccessResponse(orderMap, exchange.getResponse());
        } else {
            if (errorIsRecoverable(responseXml, tryNumber)) {
                Thread.sleep(2000);

                responseXml = createMembership(orderMap);
                return processResponse(responseXml, orderMap, exchange, ++tryNumber);
            } else {
                return failTransaction(exchange, processor, responseXml);
            }
        }
    }

    private Map failTransaction(Exchange exchange, PaymentechProcessor processor, String responseXml) throws Exception {
        log.info("Membership creation was not successful, xml response was: " + responseXml);
        Exchange exchangeVoid = processor.voidTransaction(exchange, true);
        log.info("Cancellation transaction sent, response message : " + exchangeVoid.getResponse().getMessage());
        return getMembershipCreationFailureResponse(responseXml);
    }

    /**
     * Void a previously captured transaction - this should only happen if there is a problem creating the membership in
     * the galaxy system, but if there is we need to remove that previous transaction
     * @param orderMap
     * @param response
     * @return
     */
    private Object voidTransaction(Map orderMap, ResponseIF response) {
        return null;
    }

    private Map getMembershipCreationFailureResponse(String responseXml) throws IOException {
        StringWriter stringWriter = new StringWriter();
        VelocityContext vc = new VelocityContext();
        vc.put("date", new Date());
        vc.put("code", -1);
        vc.put("reason", "unknown failure");

        Template template = velocityEngine.getTemplate("templates/galaxy/tpl_paymentFailureJson.vtl");
        template.merge(vc, stringWriter);

        //@todo return these values as a map instead of a string
        return null;
    }


    private Map sendPaymentSuccessResponse(Map orderMap, ResponseIF paymentechResponse) throws IOException {
        Date date = new Date();
        String responseCode = paymentechResponse.getValue("RespCode");
        String confirmationCode = paymentechResponse.getValue("AuthCode");
        Double amount = Double.parseDouble((String) orderMap.get("ccAmount"));
        Product product = (Product)mapProducts.get(orderMap.get("plu"));
        String productName = product.getName();

        StringWriter stringWriter = new StringWriter();
        VelocityContext vc = new VelocityContext();
        vc.put("confirmationCode", confirmationCode);
        vc.put("amount", amount);
        vc.put("amountFormatted", formatCurrency.format(amount));
        vc.put("product", productName);
        vc.put("productName", productName);
        vc.put("productId", orderMap.get("plu"));
        vc.put("date", formatDateSimple.format(date));
        vc.put("orderId", orderMap.get("orderNumber"));
        vc.put("tax", "0");
        vc.put("shipping", "0");
        vc.put("city", orderMap.get("city"));
        vc.put("state", orderMap.get("state"));
        vc.put("country", orderMap.get("country"));
        vc.put("zip", orderMap.get("zip"));

        if (orderMap.get("validTo") != null)
            vc.put("validTo", formatWholeDateOut.format(orderMap.get("validTo")));
        else
            vc.put("validTo", "");

        if (orderMap.get("visualId") != null)
            vc.put("visualId", orderMap.get("visualId"));
        else
            vc.put("visualId", "");

        vc.put("galaxyOrderId", orderMap.get("galaxyOrderId"));

        Template template = velocityEngine.getTemplate("templates/galaxy/tpl_paymentSuccessJson.vtl");
        template.merge(vc, stringWriter);

        //@todo fix this and return a map of values
        return new HashMap();
    }

    //@todo add failure codes and reasons
    private Map sendPaymentFailureResponse(ResponseIF paymentechResponse) throws IOException {
        StringWriter stringWriter = new StringWriter();
        VelocityContext vc = new VelocityContext();
        vc.put("date", formatDateSimple.format(new Date()));
        vc.put("code", paymentechResponse.getStatus());
        vc.put("reason", paymentechResponse.getMessage());

        Template template = velocityEngine.getTemplate("templates/galaxy/tpl_paymentFailureJson.vtl");
        template.merge(vc, stringWriter);

        return new HashMap();
    }

    private boolean errorIsRecoverable(String responseXml, int retries) {
        if (retries > RECOVERABLE_RETRIES)
            return false;

        for (int i = 0; i < recoverableErrorStrings.length; i++) {
            String recoverableErrorString = recoverableErrorStrings[i];
            if (responseXml.indexOf(recoverableErrorString) > 0)
                return true;
        }
        return false;
    }

    private boolean membershipCreationWasSuccessful(Map orderMap) {
        return orderMap.get("galaxyOrderId") != null;
    }

    private boolean authWasSuccessful(ResponseIF response) {
        return response.getValue("AuthCode") != null && response.getValue("TxRefNum") != null;
    }

    private ResponseIF captureTransaction(Map orderMap, ResponseIF response) throws Exception {
        String orderNumber = (String)orderMap.get("orderNumber");
        Double amount = (Double)orderMap.get("ccAmount");
        response = processor.captureTransaction(orderNumber, amount, response.getTxRefNum()).getResponse();
        return response;
    }

    private String nodeContent(String node, String xml) {
        String nodeStart = "<" + node + ">";
        String nodeEnd = "</" + node + ">";

        int start = xml.indexOf(nodeStart) + nodeStart.length();
        int end = xml.indexOf(nodeEnd);
        if (start != -1 && end > start)
            return xml.substring(start, end);
        return null;
    }

    private String createMembership(Map orderMap) {
        StringWriter stringWriter = new StringWriter();
        VelocityContext vc = new VelocityContext();
        vc.put("order", orderMap);
        vc.put("galaxySourceId", galaxySourceId);
        vc.put("galaxyCustomerId", galaxyCustomerId);
        vc.put("serverId", serverId);

        Template template = null;
        if (orderMap.containsKey("contractId")) {
            template = velocityEngine.getTemplate("templates/galaxy/tpl_orderpass_plan.vtl");
        } else
            template = velocityEngine.getTemplate("templates/galaxy/tpl_orderpass.vtl");

        template.merge(vc, stringWriter);

        String responseXml = postXml(stringWriter.toString(), galaxyConnectionTimeoutTransaction, "createMembership");
        String contentGalaxyOrderId = null;
        String contentVisualId = null;
        String contentExpirationDate = null;

        boolean testMembershipPropertyReads = false;
        if (testMembershipPropertyReads) {
            //@todo test this instead of the following few lines
            try {
                Document document = documentBuilder.parse(new InputSource( new StringReader( responseXml ) ));
                contentGalaxyOrderId = getSingleNodeValue(document, "GalaxyOrderID");
                contentVisualId = getSingleNodeValue(document, "VisualID");
                contentExpirationDate = getSingleNodeValue(document, "ExpirationDate");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            contentGalaxyOrderId = nodeContent("GalaxyOrderID", responseXml);
            contentVisualId = nodeContent("VisualID", responseXml);
            contentExpirationDate = nodeContent("ExpirationDate", responseXml);
        }

        if (contentGalaxyOrderId != null)
            orderMap.put("galaxyOrderId", contentGalaxyOrderId);
        if (contentVisualId  != null)
            orderMap.put("visualId", contentVisualId);
        if (contentExpirationDate != null) {
            try {
                orderMap.put("validTo", formatTimestamp.parse(contentExpirationDate));
            } catch (ParseException pe) {
                pe.printStackTrace();
            }
        }
        return responseXml;
    }

    private String postXml(String xml, Integer connectionTimeoutGalaxyService, String tag) {
        log.info(tag + " post xml to " + serverUrl + ":" + serverPort);
        log.info("xml:\n" + xml);
        return postXml(xml, connectionTimeoutGalaxyService, 0);
    }

    private String postXml3(String xml, Integer connectionTimeoutGalaxyService, int tryIndex) {
        //@todo switch this to a global instance of httpClient (which we have instantiated), but need to test

        if (tryIndex >= maxPostAttempts)
            return null;
        else {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            try {
                HttpPost post = new HttpPost(serverUrl + ":" + serverPort);
                HttpEntity entityOut = new StringEntity(xml);
                post.setEntity(entityOut);

                System.out.println("Executing request " + post.getRequestLine());

                if (connectionTimeoutGalaxyService != null) {
                    RequestConfig requestConfig = RequestConfig.custom().
                            setSocketTimeout(connectionTimeoutGalaxyService).build();
                    post.setConfig(requestConfig);
                }

                // Create a custom response handler
                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    public String handleResponse(
                            final HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }
                };

                String responseBody = httpclient.execute(post, responseHandler);
                System.out.println("----------------------------------------");
                System.out.println(responseBody);
                return responseBody;
            } catch (ClientProtocolException cpe) {
                log.warn("XML post had client protocol exception, message was: " + cpe.getMessage());
                cpe.printStackTrace();
            } catch (IOException ioe) {
                log.warn("XML post had io exception, message was: " + ioe.getMessage());
                ioe.printStackTrace();
            } finally {
                try {
                    httpclient.close();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            //if we got this far we had an error and we need to try again after waiting for TIMEOUT_BETWEEN_ATTEMPTS ms
            try {
                Thread.sleep(timeoutBetweenAttempts);
            } catch (InterruptedException tie) {
                log.warn("thread sleep between post attempts had InterruptedException, message was: " + tie.getMessage());
            }
            return postXml3(xml, connectionTimeoutGalaxyService, ++tryIndex);
        }
    }

    private String postXml(String xml, Integer connectionTimeoutGalaxyService, int tryIndex) {

        if (tryIndex >= maxPostAttempts)
            return null;
        else {
            CloseableHttpClient httpClient = HttpClients.custom()
                    .setConnectionManager(cm)
                    .setConnectionManagerShared(true)
                    .build();
            try {
                HttpPost post = new HttpPost(serverUrl + ":" + serverPort);
                HttpEntity entityOut = new StringEntity(xml);
                post.setEntity(entityOut);

                System.out.println("Executing request " + post.getRequestLine());
                if (connectionTimeoutGalaxyService != null) {
                    RequestConfig requestConfig = RequestConfig.custom().
                            setSocketTimeout(connectionTimeoutGalaxyService).build();
                    post.setConfig(requestConfig);

                }

                // Create a custom response handler
                ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
                    public String handleResponse(
                            final HttpResponse response) throws ClientProtocolException, IOException {
                        int status = response.getStatusLine().getStatusCode();
                        if (status >= 200 && status < 300) {
                            HttpEntity entity = response.getEntity();
                            return entity != null ? EntityUtils.toString(entity) : null;
                        } else {
                            throw new ClientProtocolException("Unexpected response status: " + status);
                        }
                    }
                };

                String responseBody = httpClient.execute(post, responseHandler);
                System.out.println("----------------------------------------");
                System.out.println(responseBody);
                return responseBody;
            } catch (ClientProtocolException cpe) {
                log.warn("XML post had client protocol exception, message was: " + cpe.getMessage());
                cpe.printStackTrace();
            } catch (IOException ioe) {
                log.warn("XML post had io exception, message was: " + ioe.getMessage());
                ioe.printStackTrace();
            } finally {
                try {
                    httpClient.close();
                } catch (IOException ioe) {
                    log.warn("Could not close HttpClient: " + ioe.getMessage());
                }
            }

            //if we got this far we had an error and we need to try again after waiting for TIMEOUT_BETWEEN_ATTEMPTS ms
            try {
                Thread.sleep(timeoutBetweenAttempts);
            } catch (InterruptedException tie) {
                log.warn("thread sleep between post attempts had InterruptedException, message was: " + tie.getMessage());
            }
            return postXml(xml, connectionTimeoutGalaxyService, ++tryIndex);


        }
    }

//
//    private String postXml2(String xml, Integer connectionTimeoutGalaxyService) {
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        InputStream instream = null;
//
//        try {
//            HttpEntity entityOut = new StringEntity(xml);
//            HttpPost httpPost = new HttpPost(serverUrl + ":" + serverPort);
//            httpPost.setEntity(entityOut);
//
//            if (connectionTimeoutGalaxyService != null) {
//                HttpParams params = httpclient.getParams();
//                params.setIntParameter("SO_TIMEOUT", connectionTimeoutGalaxyService);
//            }
//
//            CloseableHttpResponse response = httpclient.execute(httpPost);
//            try {
//                HttpEntity entityIn = response.getEntity();
//                BufferedInputStream bis;
//
//                int bufferSize = 2048;
//                byte[] buffer = new byte[bufferSize];
//
//                if (entityIn != null) {
//                    instream = entityIn.getContent();
//                    bis = new BufferedInputStream(instream);
//
//                    int count;
//                    while ((count = bis.read(buffer, 0, bufferSize)) != -1) {
//                        out.write(buffer, 0, count);
//                    }
//                    bis.close();
//                    out.flush();
//                    EntityUtils.consume(entityIn);
//                }
//            } finally {
//                response.close();
//            }
//
//            String responseXml = out.toString();
//            log.info("Returning response xml from " + serverUrl + ":" + serverPort);
//            log.info("response is: \n" + responseXml);
//
//            return responseXml;
//        } catch (Exception e) {
//            log.warn("Error writing XML to galaxy server: " + e.getMessage());
//            if (instream != null)
//                try {instream.close();} catch (Exception e2) {log.warn("Input stream close error: " + e2.getMessage());};
//        }
//        return null;
//    }
}

class HeartbeatGalaxyRunnable implements Runnable {
    private ServiceGalaxy serviceGalaxy;
    private int reauthenticationMinutes = 60;
    private boolean shouldExit = false;
    private String sessionId = null;
    Logger log = Logger.getLogger(HeartbeatGalaxyRunnable.class);

    public ServiceGalaxy getServiceGalaxy() {
        return serviceGalaxy;
    }

    public void setServiceGalaxy(ServiceGalaxy serviceGalaxy) {
        this.serviceGalaxy = serviceGalaxy;
    }

    public int getReauthenticationMinutes() {
        return reauthenticationMinutes;
    }

    public void setReauthenticationMinutes(int reauthenticationMinutes) {
        this.reauthenticationMinutes = reauthenticationMinutes;
    }

    public boolean isShouldExit() {
        return shouldExit;
    }

    public void setShouldExit(boolean shouldExit) {
        this.shouldExit = shouldExit;
    }

    public void run() {
        if (reauthenticationMinutes > 0) {
            while (!shouldExit) {
                log.info("heartbeat thread reauthenticating");
                long msUntilReauthenticate = 1000 * 60 * reauthenticationMinutes;
                long msExpireHalfway = (long)(((float)msUntilReauthenticate) * .5);

                sessionId = serviceGalaxy.getSessionId(sessionId, msUntilReauthenticate);
                try {
                    Thread.sleep(msUntilReauthenticate);
                } catch (InterruptedException ie) {
                    log.info("heartbeat thread sleep interruption, message was: " + ie.getMessage());
                }
            }
        }
    }
}