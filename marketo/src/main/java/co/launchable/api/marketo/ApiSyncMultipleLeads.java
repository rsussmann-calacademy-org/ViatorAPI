package co.launchable.api.marketo;

import co.launchable.api.jobs.ReportableSynchronizationObject;
import com.marketo.mktows.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.env.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * Created by michaelmcelligott on 1/17/14.
 */
public class ApiSyncMultipleLeads extends ApiBase implements Runnable, ReportableSynchronizationObject {
    Log log = LogFactory.getLog(ApiSyncMultipleLeads.class);

    private String prefixLocal = "co.launchable.api.marketo.marketo.syncMultipleLeads.";
    private String columnEmail;
    private String sql;
    private String sqlBefore;
    private String sqlBeforeSingle;
    private String sqlAfterSuccess;
    private String sqlAfterFailure;
    private String sqlInsertStatus;
    private PreparedStatement pstmtInsertStatus;
    private PreparedStatement pstmtBefore;
    private PreparedStatement pstmtBeforeSingle;
    private PreparedStatement pstmtAfterSuccess;
    private PreparedStatement pstmtAfterFailure;

    private Integer workerIndex = null;
    private int iterations = 0;
    private int fullRowsProcessed = 0;
    private boolean running = false;
    private String status = null;
    private Long contactId;
    private Integer delay;
    private int rowsToProcess = 1000;
    private int rowsMarked = 0;
    private Exception lastException;

    public Exception getLastException() {
        return lastException;
    }

    protected void setLastException(Exception lastException) {
        this.lastException = lastException;
    }

    public int getRowsMarked() {
        return rowsMarked;
    }

    public void setRowsMarked(int rowsMarked) {
        this.rowsMarked = rowsMarked;
    }

    public int getRowsToProcess() {
        return rowsToProcess;
    }

    public void setRowsToProcess(int rowsToProcess) {
        this.rowsToProcess = rowsToProcess;
    }

    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }

    public String getSqlBeforeSingle() {
        return sqlBeforeSingle;
    }

    public void setSqlBeforeSingle(String sqlBeforeSingle) {
        this.sqlBeforeSingle = sqlBeforeSingle;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public int getFullRowsProcessed() {
        return fullRowsProcessed;
    }

    public void setFullRowsProcessed(int fullRowsProcessed) {
        this.fullRowsProcessed = fullRowsProcessed;
    }

    public String getSqlInsertStatus() {
        return sqlInsertStatus;
    }

    public void setSqlInsertStatus(String sqlInsertStatus) {
        this.sqlInsertStatus = sqlInsertStatus;
    }

    public String getColumnEmail() {
        return columnEmail;
    }

    public void setColumnEmail(String columnEmail) {
        this.columnEmail = columnEmail;
    }

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSqlBefore() {
        if (contactId == null)
            return sqlBefore;
        else
            return sqlBeforeSingle;
    }

    public void setSqlBefore(String sqlBefore) {
        this.sqlBefore = sqlBefore;
    }

    public String getSqlAfterSuccess() {
        return sqlAfterSuccess;
    }

    public void setSqlAfterSuccess(String sqlAfterSuccess) {
        this.sqlAfterSuccess = sqlAfterSuccess;
    }

    public String getSqlAfterFailure() {
        return sqlAfterFailure;
    }

    public void setSqlAfterFailure(String sqlAfterFailure) {
        this.sqlAfterFailure = sqlAfterFailure;
    }

    public Integer getWorkerIndex() {
        return workerIndex;
    }

    public void setWorkerIndex(Integer workerIndex) {
        this.workerIndex = workerIndex;
    }

    public ApiSyncMultipleLeads(Environment env) {
        setColumnEmail(env.getProperty(prefixLocal + "columnEmail"));
        setSql(env.getProperty(prefixLocal + "sql"));
        setSqlBefore(env.getProperty(prefixLocal + "sqlBefore"));
        setSqlBeforeSingle(env.getProperty(prefixLocal + "sqlBeforeSingle"));
        setSqlAfterSuccess(env.getProperty(prefixLocal + "sqlAfterSuccess"));
        setSqlAfterFailure(env.getProperty(prefixLocal + "sqlAfterFailure"));
        setSqlInsertStatus(env.getProperty(prefixLocal + "sqlInsertStatus"));
        setMarketoEndpoint(env.getProperty(prefixLocal + "marketoEndpoint"));
        setReadOnlyRead(true);
        super.initialize(env);
    }

    private ParamsSyncMultipleLeads buildRequest(ResultSet rs) {
        ParamsSyncMultipleLeads request = new ParamsSyncMultipleLeads();
        ObjectFactory objectFactory = new ObjectFactory();
        JAXBElement<Boolean> dedup = objectFactory.createParamsSyncMultipleLeadsDedupEnabled(true);
        request.setDedupEnabled(dedup);
        ArrayOfLeadRecord arrayOfLeadRecords = new ArrayOfLeadRecord();

        try {
            ResultSetMetaData rsmd = rs.getMetaData();

            while (rs.next()) {
                LeadRecord rec = new LeadRecord();
                ArrayOfAttribute aoa = new ArrayOfAttribute();

                String valueEmail = rs.getString(columnEmail);
                JAXBElement<String> email = objectFactory.createLeadRecordEmail(valueEmail);
                rec.setEmail(email);

                int columns = rsmd.getColumnCount();
                for (int i = 1; i < columns; i++) {
                    String columnLabel = rsmd.getColumnLabel(i);

                    if (!columnLabel.equals(columnEmail)) {
                        Attribute attr = new Attribute();
                        attr.setAttrName(columnLabel);

                        Object value = rs.getObject(i);
                        if (value != null)
                            attr.setAttrValue(rs.getObject(i).toString());
                        else
                            attr.setAttrValue("NULL");
                        aoa.getAttributes().add(attr);
                    }
                }

                QName qname = new QName("http://www.co.launchable.api.marketo.marketo.com/mktows/", "leadAttributeList");
                JAXBElement<ArrayOfAttribute> attrList = new JAXBElement(qname, ArrayOfAttribute.class, aoa);
                rec.setLeadAttributeList(attrList);
                arrayOfLeadRecords.getLeadRecords().add(rec);
            }

            request.setLeadRecordList(arrayOfLeadRecords);
            rowsProcessed = arrayOfLeadRecords.getLeadRecords().size();
            return request;
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }

        return null;
    }

    public String getWorkerName() {
        return "Lead" + workerIndex;
    }

    private void doSqlBefore() throws SQLException {
        status = "prelim sql";
        if (pstmtBefore != null) {
            pstmtBefore.setString(1, getWorkerName());

            if (contactId != null)
                pstmtBefore.setLong(2, contactId);

            rowsMarked = pstmtBefore.executeUpdate();
        } else {
            if (sqlBefore != null && con != null && !con.isClosed()) {
                rowsMarked = con.createStatement().executeUpdate(sqlBefore);
            }
        }
    }

    private void doSqlAfterSuccess() throws Exception {
        status = "post success sql";
        boolean success = false;
        int errors = 0;

        while (!success) {
            try {
                if (pstmtAfterSuccess != null) {
                    synchronized(dataSource) {
                        pstmtAfterSuccess.setString(1, getWorkerName());
                        pstmtAfterSuccess.executeUpdate();
                        success = true;
                    }
                } else if (sqlAfterSuccess != null  && con != null && !con.isClosed()) {
                        con.createStatement().executeUpdate(sqlAfterSuccess);
                        success = true;

                } else
                    success = true;
            } catch (Exception e) {
                errors++;

                if (errors >= 3)
                    throw e;
                else
                    Thread.yield();
            }
        }
    }

    private void doSqlAfterFailure() throws Exception {
        status = "failure: " + lastException.getMessage();

        boolean success = false;
        int errors = 0;

        while (!success) {
            try {
                if (pstmtAfterFailure != null) {
                    synchronized(dataSource) {
                        pstmtAfterFailure.setString(1, getWorkerName());
                        pstmtAfterFailure.executeUpdate();
                        success = true;
                    }
                } else if (sqlAfterFailure != null && con != null && !con.isClosed()) {
                        con.createStatement().executeUpdate(sqlAfterFailure);
                        success = true;

                } else
                    success = true;
            } catch (Exception e) {
                errors++;

                if (errors >= 3)
                    throw e;
                else
                    Thread.yield();
            }
        }
    }

    protected ResultSet retrieveResultSet(String sql) throws SQLException {
        status = "waiting to retrieve";

        if (dataSource != null) {
            synchronized(dataSource) {
                status = "retrieving";

                pstmt.setString(1, getWorkerName());
                ResultSet rs = pstmt.executeQuery();
                return rs;
            }
        } else {
            if (con != null && !con.isClosed() && pstmt != null) {
                pstmt.setString(1, getWorkerName());
                ResultSet rs = pstmt.executeQuery();
                return rs;
            }
        }
        return null;
    }

    public int executeOnce() throws Exception {
        int MAX_ERRORS = 10;
        int errorCount = 0;
        boolean blnSqlBeforeCompleted = false;
        boolean blnResultSetRetrieved = false;
        boolean success = true;

        //run our prelim sql, setting aside the rows we're going to work with
        while (success && !blnSqlBeforeCompleted) {
            try {
                doSqlBefore();
                blnSqlBeforeCompleted = true;
            } catch (SQLException sqle) {
                errorCount++;

                if (errorCount > MAX_ERRORS)
                    success = false;

                sleepThroughDeadlock();
            }
        }

        ResultSet rs = null;
        ParamsSyncMultipleLeads request = null;
        //retrieve our resultset
        while (success && !blnResultSetRetrieved) {
            try {
                rs = retrieveResultSet(sql);

                if (rs != null) {
                    status = "building request";
                    request = buildRequest(rs);
                    blnResultSetRetrieved = true;
                }
            } catch (SQLException sqle) {
                errorCount++;

                if (errorCount > MAX_ERRORS)
                    success = false;

                sleepThroughDeadlock();
            }
        }

        SuccessSyncMultipleLeads result = null;
        String resultString = null;
        //at this point we have a built request object that we need to submit
        if (success && request != null && request.getLeadRecordList().getLeadRecords().size() > 0) {
            status = "generating header";

            JAXBContext contextParams = JAXBContext.newInstance(ParamsSyncMultipleLeads.class);
            Marshaller m = contextParams.createMarshaller();
            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            StringWriter stringWriter = new StringWriter();
            m.marshal(request, stringWriter);
            resultString = stringWriter.toString();
            //System.out.println(resultString);

            AuthenticationHeader header = generateAuthenticationHeader();
            status = "posting";
            result = getMarketoWebServicePort().syncMultipleLeads(request, header);

            if (result != null) {
                try {
                    JAXBContext context = JAXBContext.newInstance(SuccessSyncMultipleLeads.class);
                    m = context.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                    stringWriter = new StringWriter();
                    m.marshal(result, stringWriter);

                    resultString = stringWriter.toString();
                    //System.out.print(resultString);
                    //System.out.print("Worker: " + getWorkerName());

                    //m.marshal(result, System.out);
                } catch (JAXBException jaxbe) {
                    setLastException(jaxbe);
                    success = false;
                }
            }
        }

        //if we have a result and we're supposed to insert success records, do so
        boolean blnRecordsInserted = false;
        while (success && resultString != null && getSqlInsertStatus() != null && !blnRecordsInserted) {
            try {
                pstmtInsertStatus = con.prepareStatement(getSqlInsertStatus());
                processResults(resultString, request);
                blnRecordsInserted = true;
            } catch (Exception e) {
                errorCount++;

                if (errorCount > MAX_ERRORS) {
                    success = false;
                    setLastException(e);
                }
                sleepThroughDeadlock();
            }
        }

        if (success)
            doSqlAfterSuccess();
        else
            doSqlAfterFailure();

        return rowsProcessed;
    }

    private void processResults(String resultString, ParamsSyncMultipleLeads request) throws Exception {
        InputSource inputSource = new InputSource( new StringReader( resultString ) );
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputSource);


        pstmtInsertStatus = con.prepareStatement(getSqlInsertStatus());
        NodeList nodeList = document.getElementsByTagName("syncStatus");
        for (int i = 0; i < nodeList.getLength(); i++) {
            LeadRecord leadRecord = request.getLeadRecordList().getLeadRecords().get(i);
            processResponseNode(nodeList.item(i), leadRecord);
        }
    }

    private void processResponseNode(Node node, LeadRecord leadRecord) {
        String objectType = "Lead";
        String status = null;
        String error = null;
        String leadId = null;

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if ("status".equals(childNode.getNodeName())) {
                status = childNode.getTextContent();
            } else if ("error".equals(childNode.getNodeName())) {
                error = childNode.getTextContent();
            } else if ("leadId".equals(childNode.getNodeName())) {
                leadId = childNode.getTextContent();
            }
        }
        insertObjectStatus(objectType, leadId, status, error, leadRecord);
    }

    private void insertObjectStatus(String objectType, String leadId, String status, String error, LeadRecord leadRecord)  {
        //status = "writing history";

        if (pstmtInsertStatus != null) {
            try {
                pstmtInsertStatus.clearParameters();
                pstmtInsertStatus.setString(1, objectType);
                pstmtInsertStatus.setString(2, status);
                pstmtInsertStatus.setString(3, error);
                pstmtInsertStatus.setString(4, leadRecord.getEmail().getValue());
                pstmtInsertStatus.setString(5, leadId);

                pstmtInsertStatus.executeUpdate();

            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }


    @Override
    public void run() {
        try {
            //if a delay has been set for this instance, wait that amount of time prior to starting processing
            //(tends to alleviate some database related deadlocks)
            if (delay != null) {
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void execute() throws Exception {
        running = true;
        status = "prepping data access";

        try {
            setupDatabase();
            pstmtBefore = con.prepareStatement(getSqlBefore());
            pstmtAfterFailure = con.prepareStatement(getSqlAfterFailure());
            pstmtAfterSuccess = con.prepareStatement(getSqlAfterSuccess());
            fullRowsProcessed = 0;
            rowsProcessed = 1;

            int reportIndex = 0;
            int iterationsBetweenReports = 10;

            while (rowsProcessed != 0) {
                iterations++;
                rowsProcessed = executeOnce();
                fullRowsProcessed += rowsProcessed;

                reportIndex++;
                if (reportIndex % iterationsBetweenReports == 0) {
                    log.info(fullRowsProcessed + " rows processed by worker thread");
                    reportIndex = 0;
                }

                if (contactId != null && rowsProcessed == 1)
                    break;
                Thread.sleep(5000);
            }
        } catch (Exception e) {
            log.error(getWorkerName() + ": error occurred processing contact records, message was : " + e.getMessage());
            throw e;
        } finally {
            log.info(getWorkerName() + ": completed execution, releasing database resources for worker");
            releaseDatabase();
            running = false;
        }
    }

    public static void main(String args[]) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("src/main/webapp/WEB-INF/co.launchable.api.marketo.marketo-dispatcher-servlet.xml");
        ServiceMarketo serviceMarketo = (ServiceMarketo)ctx.getBean("serviceMarketo");
        ApiSyncOpportunities apiSyncOpportunities = (ApiSyncOpportunities)ctx.getBean("apiSyncOpportunities");

        try {
            //serviceMarketo.syncPendingEvents();

            //serviceMarketo.calculateUpdatedLeads();
            //serviceMarketo.syncPendingLeads(5);
            //serviceMarketo.syncPendingConstituents(1);
            //Thread.sleep(300000);
            //serviceMarketo.syncPendingVisitations(2);
            //Thread.sleep(300000);
            //serviceMarketo.syncPendingEvents(1);
            //Thread.sleep(300000);
            //serviceMarketo.syncPendingOrders(2);
            //Thread.sleep(300000);
            //serviceMarketo.syncPendingOrderDetails(2);

            //apiSyncOpportunities.linkFailedOpportunities();
            //serviceMarketo.syncPruneStatuses();
            serviceMarketo.sendEmailReport();
            //serviceMarketo.sendDuplicatesReport();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
