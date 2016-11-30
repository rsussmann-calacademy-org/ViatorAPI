package co.launchable.api.marketo;

import com.marketo.mktows.*;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.sql.DataSource;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Marshaller;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

/**
 * Created by Michael on 5/13/2015.
 */
public class ApiSyncUpdatedInterests {
    Logger log = Logger.getLogger(ApiSyncUpdatedInterests.class);

//    private String sql = "SELECT TOP 100 b.email, c.code, d.optInEnews, d.optInLectures, d.optInTeachers, d.optInNightlife from contactsInterests a  \n" +
//            "  inner join contacts b on a.contactid = b.id  \n" +
//            "  inner join interests c on a.interestid = c.id \n" +
//            "  left join interestOptOut d on b.email = d.email\n" +
//            "WHERE a.lastUploaded IS NULL AND d.optInEnews = 'no' ORDER BY b.email";

//    private String sql = "SELECT TOP 100 b.email, c.code, d.optInEnews, d.optInLectures, d.optInTeachers, d.optInNightlife from contactsInterests a  \n" +
//            "  inner join contacts b on a.contactid = b.id  \n" +
//            "  inner join interests c on a.interestid = c.id \n" +
//            "  left join interestOptOut d on b.email = d.email\n" +
//            "WHERE a.lastUploaded = '1969-01-01' ORDER BY b.email";

//    private String sql = "select top 200 a.email, c.code, d.optInEnews, d.optInLectures, d.optInTeachers, d.optInNightlife FROM contacts a\n" +
//            "LEFT JOIN contactsInterests b ON a.id = b.contactId\n" +
//            "LEFT JOIN interestOptOut d ON a.email = d.email\n" +
//            "LEFT JOIN interests c ON b.interestId = c.id\n" +
//            "WHERE a.lastUploaded = '1969-01-01' and a.luInterests is null";

    private String sql = "select top 200 a.email, c.code, d.optInEnews, d.optInLectures, d.optInTeachers, d.optInNightlife FROM interestNulls a\n" +
            "    LEFT JOIN contacts e on a.email = e.email\n" +
            "LEFT JOIN contactsInterests b ON e.id = b.contactId\n" +
            "LEFT JOIN interestOptOut d ON a.email = d.email\n" +
            "LEFT JOIN interests c ON b.interestId = c.id\n" +
            "WHERE a.lastUploaded is null and e.luInterests is null";
    private String sqlUpdate = "update contactsInterests set lastUploaded = getdate() where contactid = (select max(id) from contacts where lower(email) = ?) and lastUploaded is null";
    private String sqlInsert = "insert into MarketoStatus (objectType, status, error, key1, key2, lastUpdated) values (?, ?, ?, ?, ?, getdate())";

    public static Map mapInterestsToOptIns = new HashMap();
    private int rowsProcessed = 0;
    private List<String> emailsInOrder = new ArrayList();
    private PreparedStatement pstmtUpdateInterest;
    private PreparedStatement pstmtInsertStatus;
    private boolean calculateWithExternalOptOuts = false;
    private boolean calculateWithExternalOptIns = false;
    private boolean sendNulls = false;
    private Map mapEmailToOptInConfig;

    @Autowired
    private MarketoApiConfig marketoApiConfig;

    @Autowired
    protected DataSource dataSourceContacts;

    public String getSql() {
        return sql;
    }

    public void setSql(String sql) {
        this.sql = sql;
    }

    public String getSqlUpdate() {
        return sqlUpdate;
    }

    public void setSqlUpdate(String sqlUpdate) {
        this.sqlUpdate = sqlUpdate;
    }

    public boolean isCalculateWithExternalOptOuts() {
        return calculateWithExternalOptOuts;
    }

    public void setCalculateWithExternalOptOuts(boolean calculateWithExternalOptOuts) {
        this.calculateWithExternalOptOuts = calculateWithExternalOptOuts;
    }

    public boolean isCalculateWithExternalOptIns() {
        return calculateWithExternalOptIns;
    }

    public void setCalculateWithExternalOptIns(boolean calculateWithExternalOptIns) {
        this.calculateWithExternalOptIns = calculateWithExternalOptIns;
    }

    public boolean isSendNulls() {
        return sendNulls;
    }

    public void setSendNulls(boolean sendNulls) {
        this.sendNulls = sendNulls;
    }

    static {
        mapInterestsToOptIns.put("NonMember", "optIneNews");
        mapInterestsToOptIns.put("Nightlife", "optInNightlife");
        mapInterestsToOptIns.put("DonorChase", "optInDonor");
        mapInterestsToOptIns.put("DonorLapsed", "optInDonor");
        mapInterestsToOptIns.put("DonorFriends", "optInDonor");
        mapInterestsToOptIns.put("DonorPipeline", "optInDonor");
        mapInterestsToOptIns.put("MembershipActive", "optInMembership");
        mapInterestsToOptIns.put("MembershipEnews", "optInMembership");
        mapInterestsToOptIns.put("MembershipLapsed", "optInMembership");
        mapInterestsToOptIns.put("Lectures", "optInLectures");
        mapInterestsToOptIns.put("HomeSchool", "optInHomeschool");
        mapInterestsToOptIns.put("RockFamily", "optInRockFamily");
    }

    public void execute() {
        Connection con = null;
        try {
            mapEmailToOptInConfig = new HashMap();

            con = dataSourceContacts.getConnection();
            //pstmtUpdateInterest = con.prepareStatement(sqlUpdate);
            //pstmtUpdateInterest = con.prepareStatement("update contacts set luInterests = getdate() where email = ?");
            pstmtUpdateInterest = con.prepareStatement(sqlUpdate);
            pstmtInsertStatus = con.prepareStatement(sqlInsert);
            boolean noRecords = false;

            while (!noRecords) {
                noRecords = true;
                ResultSet rs = con.createStatement().executeQuery(sql);
                mapEmailToOptInConfig.clear();

                while (rs.next()) {
                    String recordEmail = rs.getString(1);

                    if (recordEmail == null || "".equals(recordEmail))
                        continue;

                    noRecords = false;

                    //find the config object that we're building for this email address
                    OptInConfig optInConfig = (OptInConfig) mapEmailToOptInConfig.get(recordEmail);
                    if (optInConfig == null) {
                        optInConfig = new OptInConfig();                        //create a new one if it doesn't exist
                        mapEmailToOptInConfig.put(recordEmail, optInConfig);
                    }

                    //pull out the other values from the query result
                    String recordInterest = rs.getString(2);
                    String optInEnews = rs.getString(3);
                    String optInLectures = rs.getString(4);
                    String optInTeachers = rs.getString(5);
                    String optInNightlife = rs.getString(6);

                    //if we have a record in the contactsInterests join table, set the value to 'yes' for transmission.. the following
                    //code will then cancel it
                    String optInName = (String) mapInterestsToOptIns.get(recordInterest);
                    if (optInName != null) {
                        if (optInName.equals("optIneNews")) optInConfig.optInEnews = Boolean.TRUE;
                        if (optInName.equals("optInLectures")) optInConfig.optInLectures = Boolean.TRUE;
                        if (optInName.equals("optInTeachers")) optInConfig.optInTeachers = Boolean.TRUE;
                        if (optInName.equals("optInNightlife")) optInConfig.optInNightlife = Boolean.TRUE;
                        if (optInName.equals("optInDonor")) optInConfig.optInDonor = Boolean.TRUE;
                        if (optInName.equals("optInHomeschool")) optInConfig.optInHomeschool = Boolean.TRUE;
                        if (optInName.equals("optInRockFamily")) optInConfig.optInRockFamily = Boolean.TRUE;
                        if (optInName.equals("optInMembership")) optInConfig.optInMembership = Boolean.TRUE;
                    }

                    //when we have pulled down the opt-out list from Marketo we want to make sure we update
                    //the values we're sending over to reflect whatever they have (ie, the user opts out in
                    //the Marketo system and we don't want to overwrite it)
                    //normally our data isn't current from Marketo so we ignore
                    //their opt in / opt out values and only send over the very, very latest opt-ins from our website
                    if (calculateWithExternalOptOuts) {
                        if (optInEnews != null) {
                            if ("No".equals(optInEnews)) optInConfig.optInEnews = Boolean.FALSE;
                            else if ("no".equals(optInEnews)) optInConfig.optInEnews = Boolean.FALSE;
                        }
                        if (optInLectures != null) {
                            if ("No".equals(optInLectures)) optInConfig.optInLectures = Boolean.FALSE;
                            else if ("no".equals(optInLectures)) optInConfig.optInLectures = Boolean.FALSE;
                        }
                        if (optInTeachers != null) {
                            if ("No".equals(optInTeachers)) optInConfig.optInTeachers = Boolean.FALSE;
                            else if ("no".equals(optInTeachers)) optInConfig.optInTeachers = Boolean.FALSE;
                        }
                        if (optInNightlife != null) {
                            if ("No".equals(optInNightlife)) optInConfig.optInNightlife = Boolean.FALSE;
                            if ("no".equals(optInNightlife)) optInConfig.optInNightlife = Boolean.FALSE;
                        }
                    }

                    //same thing with opt-in data.. normally our data isn't current from Marketo so we ignore
                    //their opt in / opt out values and only send over the very, very latest opt-ins from our website
                    if (calculateWithExternalOptIns) {
                        if (optInEnews != null) {
                            if ("yes".equals(optInEnews)) optInConfig.optInEnews = Boolean.TRUE;
                            else if ("Yes".equals(optInEnews)) optInConfig.optInEnews = Boolean.TRUE;
                        }
                        if (optInLectures != null) {
                            if ("yes".equals(optInEnews)) optInConfig.optInLectures = Boolean.TRUE;
                            else if ("Yes".equals(optInEnews)) optInConfig.optInLectures = Boolean.TRUE;
                        }
                        if (optInTeachers != null) {
                            if ("yes".equals(optInEnews)) optInConfig.optInTeachers = Boolean.TRUE;
                            else if ("Yes".equals(optInEnews)) optInConfig.optInTeachers = Boolean.TRUE;
                        }
                        if (optInNightlife != null) {
                            if ("yes".equals(optInEnews)) optInConfig.optInTeachers = Boolean.TRUE;
                            else if ("Yes".equals(optInEnews)) optInConfig.optInTeachers = Boolean.TRUE;
                        }
                    }
                }

                if (!noRecords) {
                    //create our jaxb objects for transmission
                    ParamsSyncMultipleLeads request = new ParamsSyncMultipleLeads();
                    ObjectFactory objectFactory = new ObjectFactory();
                    JAXBElement<Boolean> dedup = objectFactory.createParamsSyncMultipleLeadsDedupEnabled(true);
                    request.setDedupEnabled(dedup);
                    ArrayOfLeadRecord arrayOfLeadRecords = new ArrayOfLeadRecord();

                    Set<String> emailKeys = (Set<String>) mapEmailToOptInConfig.keySet();
                    for (String recordEmail : emailKeys) {
                        emailsInOrder.add(recordEmail);

                        OptInConfig emailOptInConfig = (OptInConfig) mapEmailToOptInConfig.get(recordEmail);
                        LeadRecord rec = new LeadRecord();
                        ArrayOfAttribute aoa = new ArrayOfAttribute();

                        JAXBElement<String> email = objectFactory.createLeadRecordEmail(recordEmail);
                        rec.setEmail(email);

                        //the addAttribute method will ignore stuff that isn't defined unless sendNulls is set to true, in
                        //which case it will affirmatively send a null if we have no value for that particular field
                        //this is again because we don't want to overwrite whatever has happened in Marketo except for
                        //what a user has done in our system in, say, the last 24 hours
                        addAttribute(aoa, "optIneNews", emailOptInConfig.optInEnews);
                        addAttribute(aoa, "optInLectures", emailOptInConfig.optInLectures);
                        addAttribute(aoa, "optInNightlife", emailOptInConfig.optInNightlife);
                        addAttribute(aoa, "optInTeachers", emailOptInConfig.optInTeachers);
                        addAttribute(aoa, "optInDonor", emailOptInConfig.optInDonor);
                        addAttribute(aoa, "optInMembership", emailOptInConfig.optInMembership);
                        addAttribute(aoa, "optInRockFamily", emailOptInConfig.optInRockFamily);
                        addAttribute(aoa, "optInHomeschool", emailOptInConfig.optInHomeschool);

                        QName qname = new QName("http://www.marketo.com/mktows/", "leadAttributeList");
                        JAXBElement<ArrayOfAttribute> attrList = new JAXBElement(qname, ArrayOfAttribute.class, aoa);
                        rec.setLeadAttributeList(attrList);
                        arrayOfLeadRecords.getLeadRecords().add(rec);
                    }
                    request.setLeadRecordList(arrayOfLeadRecords);

                    JAXBContext contextParams1 = JAXBContext.newInstance(ParamsSyncMultipleLeads.class);
                    Marshaller m1 = contextParams1.createMarshaller();
                    m1.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    StringWriter sw1 = new StringWriter();
                    m1.marshal(request, sw1);
                    String rs1 = sw1.toString();
                    System.out.println(rs1);

                    //our ParamsSyncMultipleLeads object now contains all of our lead updates, so send them over
                    AuthenticationHeader header = MarketoUtils.generateAuthenticationHeader(marketoApiConfig);
                    SuccessSyncMultipleLeads result = marketoApiConfig.getMarketoWebServicePort().syncMultipleLeads(request, header);

                    //process the resulting dom tree
                    JAXBContext contextParams = JAXBContext.newInstance(SuccessSyncMultipleLeads.class);
                    Marshaller m = contextParams.createMarshaller();
                    m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    StringWriter stringWriter = new StringWriter();
                    m.marshal(result, stringWriter);
                    String resultString = stringWriter.toString();
                    System.out.println(resultString);

                    processResults(resultString, request);
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            try {
                con.close();
            } catch (SQLException sqle) {
                log.error("error closing connection: " + sqle.getMessage());
                sqle.printStackTrace();
            }
        }
    }

    private void addAttribute(ArrayOfAttribute aoa, String attributeName, Boolean bool) {
        if (bool != null || sendNulls) {
            Attribute attrEnews = new Attribute();
            attrEnews.setAttrName(attributeName);
            attrEnews.setAttrValue(getValue(bool));
            aoa.getAttributes().add(attrEnews);
        }
    }

    private String getValue(Boolean val) {
        if (val == Boolean.TRUE) return "yes";
        if (val == Boolean.FALSE) return "No";
        //return "blank";
        return "NULL";
    }

    private void processResults(String resultString, ParamsSyncMultipleLeads request) throws Exception {
        InputSource inputSource = new InputSource( new StringReader( resultString ) );
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.parse(inputSource);

        NodeList nodeList = document.getElementsByTagName("syncStatus");
        for (int i = 0; i < nodeList.getLength(); i++) {
            LeadRecord leadRecord = request.getLeadRecordList().getLeadRecords().get(i);
            processResponseNode(nodeList.item(i), leadRecord);
        }
    }

    private void processResponseNode(Node node, LeadRecord leadRecord) {
        String objectType = "Interest";
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

        //update our contactsInterests table to show that we transmitted the value successfully
        try {
            System.out.println(leadRecord.getEmail().getValue() + ", " + leadId + ", " + status);
            pstmtUpdateInterest.clearParameters();
            pstmtUpdateInterest.setString(1, (String) leadRecord.getEmail().getValue().toLowerCase());
            pstmtUpdateInterest.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }

        //insert into MarketoStatus upon success (or failure) so that our nightly email works correctly
        try {
            pstmtInsertStatus.clearParameters();
            pstmtInsertStatus.setString(1, objectType);
            pstmtInsertStatus.setString(2, status);
            pstmtInsertStatus.setString(3, error);
            pstmtInsertStatus.setString(4, leadRecord.getEmail().getValue());
            pstmtInsertStatus.setString(5, leadId);
            rowsProcessed += pstmtInsertStatus.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) {
        ApplicationContext ctx = new FileSystemXmlApplicationContext("C://dev/Projects/ASViatorIntegration/src/main/webapp/WEB-INF/marketo-dispatcher-servlet.xml");
        ApiSyncUpdatedInterests apiSyncUpdatedInterests = (ApiSyncUpdatedInterests)ctx.getBean("apiSyncUpdatedInterests");
        //DataSource dataSource = (DataSource)ctx.getBean("dataSourceContacts");
        //MarketoApiConfig marketoApiConfig =(MarketoApiConfig)ctx.getBean("marketoApiConfig");
        //apiSyncUpdatedInterests.setDataSource(dataSource);

        apiSyncUpdatedInterests.setCalculateWithExternalOptIns(false);
        apiSyncUpdatedInterests.setCalculateWithExternalOptOuts(false);
        apiSyncUpdatedInterests.setSendNulls(false);
        apiSyncUpdatedInterests.execute();
    }

    public class OptInConfig {
        public String email = null;
        public String marketoId = null;
        public Boolean optInEnews = null;
        public Boolean optInTeachers = null;
        public Boolean optInLectures = null;
        public Boolean optInNightlife = null;
        public Boolean optInDonor = null;
        public Boolean optInMembership = null;
        public Boolean optInRockFamily = null;
        public Boolean optInHomeschool = null;
    }
}
