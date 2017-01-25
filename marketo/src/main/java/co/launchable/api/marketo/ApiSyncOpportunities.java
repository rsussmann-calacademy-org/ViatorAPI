package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Michael on 3/17/2015.
 */
@PropertySource("co.launchable.api.marketo.marketo.properties")
public class ApiSyncOpportunities extends ApiBase implements Runnable {
    Logger log = Logger.getLogger(ApiSyncOpportunities.class);

    private SimpleDateFormat sdfY4M2D2 = new SimpleDateFormat("yyyy-MM-dd");
    private String sql = "SELECT TOP 100 a.marketoLeadId, orderDate, orderTotal, orderId, email from MarketoOpportunities a INNER JOIN MarketoLeads b ON a.marketoLeadId = b.marketoLeadId WHERE lastUploaded IS NULL";
    private String sqlUpdate = "UPDATE MarketoOpportunities SET lastUploaded = getdate(), marketoOpportunityId = ? WHERE orderId = ?";
    private String sqlUpdateFailure = "UPDATE MarketoOpportunities SET lastUploaded = '1970-01-01', marketoOpportunityId = ? WHERE orderId = ?";
    private String sqlInsertStatus = "insert into MarketoStatus (objectType, status, error, key1, key2, lastUpdated) values (?, ?, ?, ?, ?, getdate())";
    private String sqlPrepareRecords = "execute sp_queueOpportunities";

    private String sqlSelectFailures = "select  top 100 a.marketoLeadId, a.marketoOpportunityId, a.orderId, g.emailAddress, b.lastUpdated from MarketoOpportunities a inner join GalaxyOrders g ON a.orderId = g.orderId inner join MarketoStatus b on a.marketoOpportunityId = b.key2Number and a.orderid = b.key1Number AND b.lastUpdated > ? \n" +
            "AND b.objectType = 'Opportunity' and b.status = 'FAILED' LEFT JOIN MarketoStatus c on b.key1 = c.key1 and b.key2 = c.key2\n" +
            "AND c.objectType = 'Opportunity' AND c.status = 'CREATED' WHERE c.objectType IS NULL order by b.lastUpdated DESC";

    private String sqlSelectFailuresCount = "select  count(distinct a.marketoLeadId) from MarketoOpportunities a inner join GalaxyOrders g ON a.orderId = g.orderId inner join MarketoStatus b on a.marketoOpportunityId = b.key2Number and a.orderid = b.key1Number and b.lastUpdated > ? \n" +
            "AND b.objectType = 'Opportunity' and b.status = 'FAILED' LEFT JOIN MarketoStatus c on b.key1 = c.key1 and b.key2 = c.key2\n" +
            "AND c.objectType = 'Opportunity' AND c.status = 'CREATED' AND c.objectType IS NULL " +
            "LEFT JOIN MarketoStatus d on b.key1 = d.key1 and b.key2 = d.key2 AND d.objectType = 'Opportunity' and d.status = 'FAILED' and d.lastUpdated > b.lastUpdated and d.objectType IS NULL";

    private int countSuccess = 0;
    private PreparedStatement pstmtUpdate = null;
    private PreparedStatement pstmtUpdateFailure = null;
    private PreparedStatement pstmtInsertStatus = null;

    public boolean printXmlToConsole = false;

    @Autowired
    Environment env;

    public void initialize() {
        super.initialize(env);
    }

    private void addOpportunityPersonRoleObject(long leadId, long opportunityId, ArrayOfMObject array) {
        ArrayOfAttrib attributes = new ArrayOfAttrib();

        Attrib attribPerson = new Attrib();
        attribPerson.setName("PersonId");
        attribPerson.setValue(leadId + "");

        Attrib attribOpportunity = new Attrib();
        attribOpportunity.setName("OpportunityId");
        attribOpportunity.setValue(opportunityId + "");

        Attrib attribRole = new Attrib();
        attribRole.setName("Role");
        attribRole.setValue("Member");

        attributes.getAttribs().add(attribPerson);
        attributes.getAttribs().add(attribOpportunity);
        attributes.getAttribs().add(attribRole);

        //get all of our order data and issue it
        MObject mObject = new MObject();
        mObject.setType("OpportunityPersonRole");

        mObject.setAttribList(attributes);
        array.getMObjects().add(mObject);
    }


    private ParamsSyncMObjects createOpportunityPersonRoleObject(long leadId, long opportunityId) {
        ParamsSyncMObjects request = new ParamsSyncMObjects();
        request.setOperation(SyncOperationEnum.UPSERT);

        ArrayOfMObject array = new ArrayOfMObject();
        ArrayOfAttrib attributes = new ArrayOfAttrib();

        Attrib attribPerson = new Attrib();
        attribPerson.setName("PersonId");
        attribPerson.setValue(leadId + "");

        Attrib attribOpportunity = new Attrib();
        attribOpportunity.setName("OpportunityId");
        attribOpportunity.setValue(opportunityId + "");

        Attrib attribRole = new Attrib();
        attribRole.setName("Role");
        attribRole.setValue("Member");

        attributes.getAttribs().add(attribPerson);
        attributes.getAttribs().add(attribOpportunity);
        attributes.getAttribs().add(attribRole);

        //get all of our order data and issue it
        MObject mObject = new MObject();
        mObject.setType("OpportunityPersonRole");

        mObject.setAttribList(attributes);
        array.getMObjects().add(mObject);
        request.setMObjectList(array);
        return request;
    }

    private void addOpportunityObject(float amount, Date date, String email, ArrayOfMObject array) {
        MObject mObject = new MObject();
        mObject.setType(MObjectTypeEnum.OPPORTUNITY.value());
        ArrayOfAttrib attributes = new ArrayOfAttrib();

        Attrib attribName = new Attrib();
        attribName.setName("Name");
        attribName.setValue(email);

        Attrib attribTotal = new Attrib();
        attribTotal.setName("Amount");
        attribTotal.setValue(amount + "");

        Attrib attribCloseDate = new Attrib();
        attribCloseDate.setName("CloseDate");
        attribCloseDate.setValue(sdfY4M2D2.format(date));

        Attrib attribClosed = new Attrib();
        attribClosed.setName("IsClosed");
        attribClosed.setValue("true");

        Attrib attribWon = new Attrib();
        attribWon.setName("IsWon");
        attribWon.setValue("true");

        attributes.getAttribs().add(attribName);
        attributes.getAttribs().add(attribTotal);
        attributes.getAttribs().add(attribCloseDate);
        attributes.getAttribs().add(attribClosed);
        attributes.getAttribs().add(attribWon);

        mObject.setAttribList(attributes);
        array.getMObjects().add(mObject);
    }

    private ParamsSyncMObjects createOpportunityObject(float amount, Date date, String email) {
        ParamsSyncMObjects request = new ParamsSyncMObjects();
        request.setOperation(SyncOperationEnum.UPSERT);

        ArrayOfMObject array = new ArrayOfMObject();
        //get all of our order data and issue it
        MObject mObject = new MObject();
        mObject.setType(MObjectTypeEnum.OPPORTUNITY.value());


        ArrayOfAttrib attributes = new ArrayOfAttrib();

        Attrib attribName = new Attrib();
        attribName.setName("Name");
        attribName.setValue(email);

        Attrib attribTotal = new Attrib();
        attribTotal.setName("Amount");
        attribTotal.setValue(amount + "");

        Attrib attribCloseDate = new Attrib();
        attribCloseDate.setName("CloseDate");
        attribCloseDate.setValue(sdfY4M2D2.format(date));

        attributes.getAttribs().add(attribName);
        attributes.getAttribs().add(attribTotal);
        attributes.getAttribs().add(attribCloseDate);

        mObject.setAttribList(attributes);
        array.getMObjects().add(mObject);
        request.setMObjectList(array);
        return request;
    }

    protected ResultSet retrieveResultSet(String sql) throws SQLException {
        pstmt = con.prepareStatement(sql);
        pstmtUpdate = con.prepareStatement(sqlUpdate);
        pstmtUpdateFailure = con.prepareStatement(sqlUpdateFailure);

        if (dataSource != null) {
            synchronized(dataSource) {
                ResultSet rs = pstmt.executeQuery();
                return rs;
            }
        }
        return null;
    }

    private long extractOpportunityId(SuccessSyncMObjects result) {
        return 0;
    }

    private void marshallToString(Class clazz, Object result) {
        if (printXmlToConsole) {
            try {
                JAXBContext context = JAXBContext.newInstance(clazz);
                Marshaller m = context.createMarshaller();
                m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

                StringWriter stringWriter = new StringWriter();
                m.marshal(result, stringWriter);

                String resultString = stringWriter.toString();
                System.out.println(resultString);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void setupQueries() {
        try {
            pstmtInsertStatus = con.prepareStatement(sqlInsertStatus);
            con.createStatement().executeUpdate(sqlPrepareRecords);
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }
    public void execute() {
        log.info("executing ApiSyncOpportunities execute() method");
        initialize();

        try {
            con = dataSource.getConnection();
            setupQueries();
            boolean noRecords = false;

            while (!noRecords) {
                ResultSet rs = retrieveResultSet(sql);

                List<Integer> leadIds = new ArrayList<Integer>();
                List<Integer> orderIds = new ArrayList<Integer>();
                List<Integer> oppIds = new ArrayList<Integer>();

                noRecords = true; //until proven otherwise in the while loop below
                ParamsSyncMObjects request = new ParamsSyncMObjects();
                request.setOperation(SyncOperationEnum.UPSERT);
                ArrayOfMObject arrayOpportunities = new ArrayOfMObject();
                request.setMObjectList(arrayOpportunities);

                //build a list of opportunity objects to be created
                while (rs.next()) {
                    noRecords = false;
                    Integer leadId = rs.getInt(1);
                    Date date = rs.getDate(2);
                    Float amount = rs.getFloat(3);
                    Integer orderId = rs.getInt(4);
                    String email = rs.getString(5);
                    addOpportunityObject(amount, date, email, arrayOpportunities);
                    leadIds.add(leadId);
                    orderIds.add(orderId);
                }

                //submit!
                //marshallToString(ParamsSyncMObjects.class, request);
                if (!noRecords) {
                    AuthenticationHeader header = generateAuthenticationHeader();
                    SuccessSyncMObjects resultOpportunity = getMarketoWebServicePort().syncMObjects(request, header);
                    //marshallToString(SuccessSyncMObjects.class, resultOpportunity);

                    //with our results now upload an OpportunityPersonRole object
                    ArrayOfMObject arrayConnections = new ArrayOfMObject();
                    List<MObject> objects = request.getMObjectList().getMObjects();
                    List<MObjStatus> statuses = resultOpportunity.getResult().getMObjStatusList().getMObjStatuses();
                    for (int i = 0; i < statuses.size(); i++) {
                        MObjStatus mObjStatus = statuses.get(i);
                        if (mObjStatus.getStatus() == MObjStatusEnum.CREATED || mObjStatus.getStatus() == MObjStatusEnum.UPDATED) {
                            addOpportunityPersonRoleObject(leadIds.get(i), mObjStatus.getId(), arrayConnections);
                            oppIds.add(mObjStatus.getId());
                        } else {
                            oppIds.add(null);
                        }
                    }

                    //set our upload list to be the connection items
                    request.setMObjectList(arrayConnections);

                    //submit!
                    //marshallToString(ParamsSyncMObjects.class, request);
                    header = generateAuthenticationHeader();
                    SuccessSyncMObjects resultOpportunityPersonRole = getMarketoWebServicePort().syncMObjects(request, header);
                    marshallToString(SuccessSyncMObjects.class, resultOpportunityPersonRole);

                    //now with our new result object iterate over statuses and update our database
                    statuses = resultOpportunityPersonRole.getResult().getMObjStatusList().getMObjStatuses();
                    for (int i = 0; i < statuses.size(); i++) {
                        MObjStatus mObjStatus = statuses.get(i);
                        processResult(mObjStatus.getStatus().value(), mObjStatus.getError(), orderIds.get(i), oppIds.get(i));
                    }
                }
            }
        }
        catch(Exception e) {
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception e) {
                log.error("error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    PreparedStatement pstmtSelectFailuresCount = null;
    private long getLinkFailureCount(Connection con) throws SQLException {
        long count = 0;
        pstmtSelectFailuresCount.setDate(1, lastDate);
        ResultSet rs = pstmtSelectFailuresCount.executeQuery();
        if (rs.next()) {
            count = rs.getLong(1);
        }
        return count;
    }

    java.sql.Date lastDate = new java.sql.Date(new Date().getTime());
    public void linkFailedOpportunities() {
        initialize();

        long lastCount = Integer.MAX_VALUE;
        try {
            con = getDataSource().getConnection();
            pstmtUpdate = con.prepareStatement(sqlUpdate);
            pstmtUpdateFailure = con.prepareStatement(sqlUpdateFailure);
            pstmtInsertStatus = con.prepareStatement(sqlInsertStatus);
            pstmtSelectFailuresCount = con.prepareStatement(sqlSelectFailuresCount);
            pstmtSelectFailuresCount.setDate(1, lastDate);

            long currentCount = lastCount - 1;
            while (currentCount < lastCount) {
                lastCount = currentCount;
                linkFailedOpportunitiesOnce(con);
                currentCount =  getLinkFailureCount(con);
                System.out.println("Current count: " + currentCount);
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (Exception e) {
                log.error("error closing connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    public void linkFailedOpportunitiesOnce(Connection con) {
        try {

            ParamsSyncMObjects request = new ParamsSyncMObjects();
            request.setOperation(SyncOperationEnum.UPSERT);
            ArrayOfMObject arrayConnections = new ArrayOfMObject();

            List<Long> orderIds = new ArrayList<Long>();
            List<Long> oppIds = new ArrayList<Long>();

            //PreparedStatement pstmtLatestLead = con.prepareStatement("SELECT max(cast(key2 as bigint)) FROM MarketoStatus WHERE objectType = 'Lead' and status IN ('CREATED', 'UPDATED') and key1 = ?");

            PreparedStatement pstmtSelectFailures = con.prepareStatement(sqlSelectFailures);

            pstmtSelectFailures.setDate(1, lastDate);


            ResultSet rs = pstmtSelectFailures.executeQuery();
            while (rs.next()) {
                long leadId = rs.getLong(1);
                long opportunityId = rs.getLong(2);
                long orderId = rs.getLong(3);
                String email = rs.getString(4);
                lastDate = rs.getDate(5);

                //Long latestLeadId = getLatestLeadId(email, pstmtLatestLead);
                //if (latestLeadId != null)
                //    leadId = latestLeadId;

                orderIds.add(orderId);
                oppIds.add(opportunityId);
                addOpportunityPersonRoleObject(leadId, opportunityId, arrayConnections);
            }

            request.setMObjectList(arrayConnections);

            AuthenticationHeader header = generateAuthenticationHeader();
            SuccessSyncMObjects resultOpportunityPersonRole = getMarketoWebServicePort().syncMObjects(request, header);
            marshallToString(SuccessSyncMObjects.class, resultOpportunityPersonRole);

            //now with our new result object iterate over statuses and update our database
            List<MObjStatus> statuses = resultOpportunityPersonRole.getResult().getMObjStatusList().getMObjStatuses();
            for (int i = 0; i < statuses.size(); i++) {
                MObjStatus mObjStatus = statuses.get(i);
                processResult(mObjStatus.getStatus().value(), mObjStatus.getError(), orderIds.get(i), oppIds.get(i));
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    private Long getLatestLeadId(String email, PreparedStatement pstmtLatestLead) {
        try {
            pstmtLatestLead.setString(1, email);
            ResultSet rsLead = pstmtLatestLead.executeQuery();
            if (rsLead.next()) {
                return rsLead.getLong(1);
            }
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
        return null;
    }

    public void run() {
        this.execute();
    }

    private void processResult(String status, String error, long orderId, long opportunityId) {
        if ("CREATED".equals(status) || "UPDATED".equals(status)) {
            try {
                //System.out.println(countSuccess++ + ". SUCCESS");
                pstmtUpdate.setLong(1, opportunityId);
                pstmtUpdate.setLong(2, orderId);
                int rows = pstmtUpdate.executeUpdate();
                System.out.println(rows + " successes");
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("FAILURE," + orderId + ", " + opportunityId + ", " + status);
            try {
                //System.out.println(countSuccess++ + ". SUCCESS");
                pstmtUpdateFailure.setLong(1, opportunityId);
                pstmtUpdateFailure.setLong(2, orderId);
                int rows = pstmtUpdateFailure.executeUpdate();
                System.out.println(rows + " failures");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        //finally just insert this record into our status table so that it shows up in the nightly email
        //objectType, status, error, key1, key2, lastUpdated
        String objectType = "Opportunity";
        try {
            pstmtInsertStatus.setString(1, objectType);
            pstmtInsertStatus.setString(2, status);
            pstmtInsertStatus.setString(3, error);
            pstmtInsertStatus.setString(4, orderId + "");
            pstmtInsertStatus.setString(5, opportunityId + "");
            pstmtInsertStatus.executeUpdate();
        } catch (SQLException sqle) {
            sqle.printStackTrace();
        }
    }

    public static void main(String args[]) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("src/main/webapp/WEB-INF/co.launchable.api.marketo.marketo-dispatcher-servlet.xml");
        ApiSyncOpportunities apiSyncOpportunities = (ApiSyncOpportunities)ctx.getBean("apiSyncOpportunities");
        DataSource dataSource = (DataSource)ctx.getBean("dataSourceContacts");
        apiSyncOpportunities.setDataSource(dataSource);
        apiSyncOpportunities.printXmlToConsole = true;
        apiSyncOpportunities.execute();
    }
}
