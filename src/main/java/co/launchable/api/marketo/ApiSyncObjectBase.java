package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.env.Environment;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by michaelmcelligott on 1/23/14.
 */
public abstract class ApiSyncObjectBase extends ApiBase implements Runnable, ReportableSynchronizationObject {
    Log log = LogFactory.getLog(ApiSyncObjectBase.class);

    private String columnEmail;
    private String sql;
    private String sqlWorker;
    private String sqlBefore;
    private String sqlAfterSuccess;
    private String sqlAfterFailure;
    private String sqlInsertStatus;
    private String sqlWorkerBefore;
    private String sqlWorkerBeforeSingle;
    private String sqlWorkerAfterSuccess;
    private String sqlWorkerAfterFailure;
    private String sqlCountRecordsToProcess;

    private String marketoKeys;
    private PreparedStatement pstmtInsertStatus;
    private PreparedStatement pstmtBeforeWorker;
    private PreparedStatement pstmtAfterWorkerSuccess;
    private PreparedStatement pstmtAfterWorkerFailure;
    private PreparedStatement pstmtWorker;
    private PreparedStatement pstmtCountRecordsToProcess;

    private Integer workerIndex = null;
    private Long contactId = null;
    private Environment env;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private Exception exception;
    private Integer delay;
    private boolean running;
    private String status;
    private int rowsToProcess;
    private int iteration;
    private int rowsMarked;
    private Exception lastException;
    private long statusSet;
    private long maxThreadLifetime = 1000 * 60 * 60 * 23;
    private long started = 0;

    protected void setLastException(Exception e) {
        this.lastException = e;
    }

    public Exception getLastException() {
        return lastException;
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

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public String getStatus() {
        return status + " (" + getStatusDurationInSeconds() + ")";
    }

    public long getStatusDurationInSeconds() {
        return (System.currentTimeMillis() - statusSet)/1000;
    }

    public void setStatus(String status) {
        this.status = status;
        this.statusSet = System.currentTimeMillis();
        log.info(getWorkerName() + ": " + status);
    }

    public void setStatusDebug(String status) {
        this.status = status;
        this.statusSet = System.currentTimeMillis();
        log.debug(getWorkerName() + ": " + status);
    }


    public Integer getDelay() {
        return delay;
    }

    public void setDelay(Integer delay) {
        this.delay = delay;
    }
    int fullRowsProcessed = 0;

    public int getFullRowsProcessed() {
        return fullRowsProcessed;
    }

    public void setFullRowsProcessed(int fullRowsProcessed) {
        this.fullRowsProcessed = fullRowsProcessed;
    }

    public void initialize(Environment env) {
        this.env = env;
        super.initialize(env);
        setColumnEmail(env.getProperty(getPrefixLocal() + "columnEmail"));
        setSql(env.getProperty(getPrefixLocal() + "sql"));
        setSqlWorker(env.getProperty(getPrefixLocal() + "sqlWorker"));
        setSqlBefore(env.getProperty(getPrefixLocal() + "sqlBefore"));
        setSqlAfterSuccess(env.getProperty(getPrefixLocal() + "sqlAfterSuccess"));
        setSqlAfterFailure(env.getProperty(getPrefixLocal() + "sqlAfterFailure"));
        setSqlWorkerBefore(env.getProperty(getPrefixLocal() + "sqlWorkerBefore"));
        setSqlWorkerBeforeSingle(env.getProperty(getPrefixLocal() + "sqlWorkerBeforeSingle"));
        setSqlWorkerAfterSuccess(env.getProperty(getPrefixLocal() + "sqlWorkerAfterSuccess"));
        setSqlWorkerAfterFailure(env.getProperty(getPrefixLocal() + "sqlWorkerAfterSuccess"));
        setSqlInsertStatus(env.getProperty(getPrefixLocal() + "sqlInsertStatus"));
        setSqlCountRecordsToProcess(env.getProperty(getPrefixLocal() + "sqlCountRecordsToProcess"));

        setMarketoKeys(env.getProperty(getPrefixLocal() + "marketoKeys"));
    }

    public String getSqlWorkerBeforeSingle() {
        return sqlWorkerBeforeSingle;
    }

    public void setSqlWorkerBeforeSingle(String sqlWorkerBeforeSingle) {
        this.sqlWorkerBeforeSingle = sqlWorkerBeforeSingle;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public void setWorkerIndex(int workerIndex) {
        this.workerIndex = workerIndex;
    }

    public String getSqlWorker() {
        return sqlWorker;
    }

    public void setSqlWorker(String sqlWorker) {
        this.sqlWorker = sqlWorker;
    }

    public String getMarketoKeys() {
        return marketoKeys;
    }

    public void setMarketoKeys(String marketoKeys) {
        this.marketoKeys = marketoKeys;
    }

    public String getSqlInsertStatus() {
        return sqlInsertStatus;
    }

    public void setSqlInsertStatus(String sqlInsertStatus) {
        this.sqlInsertStatus = sqlInsertStatus;
    }

    public String getSqlWorkerBefore() {
        if (contactId == null)
            return sqlWorkerBefore;
        else
            return sqlWorkerBeforeSingle;
    }

    public void setSqlWorkerBefore(String sqlWorkerBefore) {
        this.sqlWorkerBefore = sqlWorkerBefore;
    }

    public String getSqlWorkerAfterSuccess() {
        return sqlWorkerAfterSuccess;
    }

    public void setSqlWorkerAfterSuccess(String sqlWorkerAfterSuccess) {
        this.sqlWorkerAfterSuccess = sqlWorkerAfterSuccess;
    }

    public String getSqlWorkerAfterFailure() {
        return sqlWorkerAfterFailure;
    }

    public void setSqlWorkerAfterFailure(String sqlWorkerAfterFailure) {
        this.sqlWorkerAfterFailure = sqlWorkerAfterFailure;
    }

    public String getSqlCountRecordsToProcess() {
        return sqlCountRecordsToProcess;
    }

    public void setSqlCountRecordsToProcess(String sqlCountRecordsToProcess) {
        this.sqlCountRecordsToProcess = sqlCountRecordsToProcess;
    }

    public abstract String getPrefixLocal();

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
        return sqlBefore;
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

    void setSqlAfterFailure(String sqlAfterFailure) {
        this.sqlAfterFailure = sqlAfterFailure;
    }

    void doSqlBefore() throws SQLException {
        if (sqlBefore != null && con != null && !con.isClosed()) {
            con.createStatement().executeUpdate(sqlBefore);
        }
    }

    void doSqlAfterSuccess() throws SQLException {
        setStatus(iteration + ", post success");

        boolean success = false;
        int errors = 0;

        while (!success) {
            try {
                if (workerIndex != null && pstmtAfterWorkerSuccess != null) {
                    pstmtAfterWorkerSuccess.setString(1, getWorkerName());
                    pstmtAfterWorkerSuccess.executeUpdate();
                    success = true;
                } else if (sqlAfterSuccess != null && con != null && !con.isClosed()) {
                        con.createStatement().executeUpdate(sqlAfterSuccess);
                        success = true;
                } else
                    success = true;
            } catch (SQLException e) {
                errors++;
                setStatus(iteration + ", post success (e = " + errors + ")");

                if (errors >= 3)
                    throw e;
                else
                    sleepThroughDeadlock();
            }
        }
    }

    void doSqlAfterFailure() throws SQLException {
        setStatus(iteration + ", post failure");

        boolean success = false;
        int errors = 0;

        while (!success) {
            try {
                if (workerIndex != null && pstmtAfterWorkerFailure != null) {
                    pstmtAfterWorkerFailure.setString(1, getWorkerName());
                    pstmtAfterWorkerFailure.executeUpdate();
                    success = true;
                } else if (sqlAfterFailure != null && con != null && !con.isClosed()) {
                    con.createStatement().executeUpdate(sqlAfterFailure);
                    success = true;
                } else
                    success = true;
            } catch (SQLException e) {
                errors++;
                setStatus(iteration + ", post failure (e = " + errors + ")");

                if (errors >= 3)
                    throw e;
                else
                    sleepThroughDeadlock();
            }
        }
    }

    @Override
    public void run() {
        try {
            setStatus("delayed start");
            if (delay != null) {
                try {
                    Thread.sleep(delay);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            execute();
        } catch (Exception e) {
            this.exception = e;
            setStatus(exception.getMessage());
        }
    }

    private int rowsToProcess() {
        if (pstmtCountRecordsToProcess != null) {
            try {
                ResultSet rs = pstmtCountRecordsToProcess.executeQuery();
                if (rs.next())
                    rowsToProcess = rs.getInt(1);

            } catch (Exception e) {
                //just ignore this and return 0
            }
        }
        return rowsToProcess;
    }

    private boolean prepareStatements() throws Exception {
        if (workerIndex != null && getSqlWorkerBefore() != null) {
            setStatus(iteration + ", prepping sql before");
            pstmtBeforeWorker = con.prepareStatement(getSqlWorkerBefore());
            setStatus(iteration + ", prepping sql retrieve");
            pstmtWorker = con.prepareStatement(getSqlWorker());
            setStatus(iteration + ", prepping sql success");
            pstmtAfterWorkerSuccess = con.prepareStatement(getSqlWorkerAfterSuccess());
            setStatus(iteration + ", prepping sql failure");
            pstmtAfterWorkerFailure = con.prepareStatement(getSqlWorkerAfterFailure());
            setStatus(iteration + ", prepping sql records");
            pstmtCountRecordsToProcess = con.prepareStatement(getSqlCountRecordsToProcess());
        }
        return true;
    }


    public int execute() throws Exception {
        setStatus("init database");
        running = true;
        started = System.currentTimeMillis();

        setStatus(iteration + ", initializing data source");
        setupDatabase();
        setStatus(iteration + ", preparing statements");
        prepareStatements();

        int rowsToProcess = rowsToProcess();
        int lastRowsToProcess = 0;

        do {
            log.info("iterating, worker " + getWorkerName() + ", rows to process: " + rowsToProcess);

            fullRowsProcessed += executeOnce();
            if (contactId != null && rowsProcessed == 1)
                break;
             Thread.sleep(5000);
            iteration++;

            long now = System.currentTimeMillis();
            if (now - started > maxThreadLifetime) {
                log.warn("thread lifetime timed out without fully processing all records");
                break;
            }
            lastRowsToProcess = rowsToProcess;
            rowsToProcess = rowsToProcess();
        } while (rowsToProcess > 0 && rowsToProcess < lastRowsToProcess);

        if (lastException == null)
            setStatus("exited");
        else if (rowsToProcess == lastRowsToProcess) {
            setStatus("exited: no rows successfully processed in last iteration");
            log.error("exited: no rows successfully processed in last iteration, worker: " + getWorkerName());
        } else {
            setStatus("exited: " + lastException.getMessage());
            log.error("thread exited with exception " + lastException.getMessage());
        }
        running = false;
        releaseDatabase();

        log.info(getWorkerName() + " completed, rows processed: " + fullRowsProcessed);
        return fullRowsProcessed;
    }

    public String getWorkerName() {
        return getObjectTypeName() + workerIndex;
    }

    public int executeOnceAgain() throws Exception {
        int MAX_ERRORS = 10;
        int errorCount = 0;
        boolean blnSqlBeforeCompleted = false;
        boolean blnResultSetRetrieved = false;
        boolean success = true;

        //step 1:  setup sql
        while (success && !blnSqlBeforeCompleted) {
            setStatus(iteration + ", marking records");
            try {
                if (pstmtBeforeWorker != null) {
                    synchronized(env) {
                        pstmtBeforeWorker.setString(1, getWorkerName());

                        //if the contact id is set then there should be a further condition on the sql that limits
                        //the result to a single row (using contact id)
                        if (contactId != null)
                            pstmtBeforeWorker.setLong(2, contactId);

                        rowsMarked = pstmtBeforeWorker.executeUpdate();
                    }
                }
                else {
                    doSqlBefore();
                }
                blnSqlBeforeCompleted = true;
            } catch (SQLException sqle) {
                errorCount++;
                setStatus(iteration + ", marking records (e = " + errorCount + ")");
                if (errorCount > MAX_ERRORS)
                    success = false;

                sleepThroughDeadlock();
            }
        }

        //step 2:  retrieve resultset
        ResultSet rs = null;
        ParamsSyncCustomObjects request = null;

        while (success && !blnResultSetRetrieved) {
            setStatus(iteration + ", retrieve / build");

            try {
                if (pstmtBeforeWorker != null) {
                    pstmtWorker.setString(1, getWorkerName());
                    rs = pstmtWorker.executeQuery();
                } else
                    rs = retrieveResultSet(getSql());

                if (rs != null) {
                    setStatus("building request");
                    request = (ParamsSyncCustomObjects)buildRequest(rs);
                    blnResultSetRetrieved = true;
                }
            } catch (SQLException sqle) {
                errorCount++;
                setStatus(iteration + ", retrieve / build (e = " + errorCount + ")");

                if (errorCount > MAX_ERRORS)
                    success = false;

                sleepThroughDeadlock();
            }
        }

        SuccessSyncCustomObjects result = null;
        String resultString = null;
        if (request != null && request.getCustomObjList().getCustomObjs().size() > 0) {
            setStatus(iteration + ", transmitting");

            AuthenticationHeader header = generateAuthenticationHeader();
            result = getMarketoWebServicePort().syncCustomObjects(request, header);

            try {
                JAXBContext context = JAXBContext.newInstance(SuccessSyncCustomObjects.class);
                Marshaller m = context.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                StringWriter stringWriter = new StringWriter();
                m.marshal(result, stringWriter);

                resultString = stringWriter.toString();
                //System.out.print(resultString);

            } catch (JAXBException jaxbe) {
                setStatus("transmit exception");

                setLastException(jaxbe);
                success = false;
            }
        }

        //if we have a result and we're supposed to insert success records, do so
        boolean blnRecordsInserted = false;
        while (success && resultString != null && getMarketoKeys() != null && getSqlInsertStatus() != null && !blnRecordsInserted) {
            try {
                setStatusDebug(iteration + ", processing results");
                processResults(resultString);
                blnRecordsInserted = true;
            } catch (Exception e) {
                errorCount++;
                setStatus(iteration + ", processing results (e = " + errorCount + ")");

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

        return rowsMarked;
    }

    //@todo improve this to handle recoverable error conditions better - reference
    //ApiSyncMultipleLeads class for example of breaking things out
    public int executeOnce() throws Exception {
        if (true)
            return executeOnceAgain();
        else {
            try {
                ResultSet rs = null;
                setStatus(iteration + ": setup sql");

                if (pstmtBeforeWorker != null) {
                    synchronized(env) {
                        pstmtBeforeWorker.setString(1, getWorkerName());

                        //if the contact id is set then there should be a further condition on the sql that limits
                        //the result to a single row (using contact id)
                        if (contactId != null)
                            pstmtBeforeWorker.setLong(2, contactId);

                        rowsMarked = pstmtBeforeWorker.executeUpdate();
                    }

                    pstmtWorker.setString(1, getWorkerName());
                    rs = pstmtWorker.executeQuery();
                }
                else {
                    doSqlBefore();
                    rs = retrieveResultSet(getSql());
                }

                if (rs != null) {
                    rowsProcessed = 0;
                    setStatus(iteration + ":building request");

                    ParamsSyncCustomObjects request = (ParamsSyncCustomObjects)buildRequest(rs);
                    if (request.getCustomObjList().getCustomObjs().size() > 0) {
                        setStatus(iteration + ": retrieving result");

                        AuthenticationHeader header = generateAuthenticationHeader();
                        SuccessSyncCustomObjects result = getMarketoWebServicePort().syncCustomObjects(request, header);

                        try {
                            JAXBContext context = JAXBContext.newInstance(SuccessSyncCustomObjects.class);
                            Marshaller m = context.createMarshaller();
                            m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                            StringWriter stringWriter = new StringWriter();
                            m.marshal(result, stringWriter);

                            String resultString = stringWriter.toString();
                            //System.out.print(resultString);

                            setStatus(iteration + ": processing results");
                            if (getMarketoKeys() != null && getSqlInsertStatus() != null)
                                processResults(resultString);

                        } catch (JAXBException jaxbe) {
                            jaxbe.printStackTrace();
                        }
                    }
                    doSqlAfterSuccess();
                }
                return rowsMarked;
            } catch (Exception e) {
                doSqlAfterFailure();
                throw e;
            }
        }
    }

    private void processResults(String resultString) throws Exception {
        InputSource inputSource = new InputSource( new StringReader( resultString ) );
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputSource);

        pstmtInsertStatus = con.prepareStatement(getSqlInsertStatus());

        NodeList nodeList = document.getElementsByTagName("syncCustomObjStatus");
        int listLength = nodeList.getLength();
        for (int i = 0; i < listLength; i++) {
            setStatus(iteration + ": processing result " + i + "/" + listLength);
            processResponseNode(nodeList.item(i));
        }
    }

    private void insertObjectStatus(String objectType, List keys, List keyNames, String aStatus, String error)  {
        if (pstmtInsertStatus != null) {
            setStatusDebug(iteration + ": updating status");

            try {
                pstmtInsertStatus.clearParameters();
                pstmtInsertStatus.setString(1, objectType);
                pstmtInsertStatus.setString(2, aStatus);
                pstmtInsertStatus.setString(3, error);

                String[] targetKeys = marketoKeys.split(",");
                for (int i = 0; i < targetKeys.length; i++) {
                    String targetKey = targetKeys[i].trim();

                    for (int j = 0; j < keyNames.size(); j++) {
                        String s = (String) keyNames.get(j);
                        if (targetKey.equals(s)) {
                            pstmtInsertStatus.setString(i + 4, (String)keys.get(j));
                        }
                    }
                }
                pstmtInsertStatus.executeUpdate();

            } catch (SQLException sqle) {
                sqle.printStackTrace();
            }
        }
    }

    private void processResponseNode(Node node) {
        String objectType = null;
        String status = null;
        String error = null;
        List keys = new ArrayList();
        List keyNames = new ArrayList();

        NodeList childNodes = node.getChildNodes();
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);
            if ("status".equals(childNode.getNodeName())) {
                status = childNode.getTextContent();
            } else if ("error".equals(childNode.getNodeName())) {
                error = childNode.getTextContent();
            } else if ("objTypeName".equals(childNode.getNodeName())) {
                objectType = childNode.getTextContent();
            } else if ("customObjKeyList".equals(childNode.getNodeName())) {
                NodeList attributes = childNode.getChildNodes();
                for (int j = 0; j < attributes.getLength(); j++) {
                    String name = null;
                    String value = null;

                    Node attribute = attributes.item(j);
                    NodeList attributeNodes = attribute.getChildNodes();

                    for (int k = 0; k < attributeNodes.getLength(); k++) {
                        Node attributeNode = attributeNodes.item(k);
                        if ("attrName".equals(attributeNode.getNodeName()))
                            name = attributeNode.getTextContent();
                        else if ("attrValue".equals(attributeNode.getNodeName()))
                            value = attributeNode.getTextContent();
                    }
                    keys.add(value);
                    keyNames.add(name);
                }
            }
        }
        insertObjectStatus(objectType, keys, keyNames, status, error);
    }

    public Attribute buildAttribute(String name, Object value) {
        Attribute attr = new Attribute();
        attr.setAttrName(name);
        attr.setAttrValue(value == null ? "" : getStringValueForObject(value));
        return attr;
    }

    protected String getStringValueForObject(Object o) {
        if (o instanceof String)
            return (String)o;
        else if (o instanceof Date)
            return sdf.format((java.sql.Date)o);
        else if (o instanceof Integer)
            return String.valueOf((Integer)o);
        else if (o instanceof Long)
            return String.valueOf((Long)o);
        else if (o instanceof Number) {
            return String.format("%10.2f", ((Number)o).floatValue());
        }
        return o.toString();
    }

    protected abstract Object buildRequest(ResultSet rs);
    protected abstract String getObjectTypeName();
}
